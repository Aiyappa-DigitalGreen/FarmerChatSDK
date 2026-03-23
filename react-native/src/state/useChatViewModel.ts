/**
 * useChatViewModel.ts
 * Central orchestrator for chat logic.
 * Intercepts public ChatActions, performs async operations, then dispatches
 * internal _-prefixed actions to update state via chatReducer.
 */

import { useReducer, useCallback, useRef } from 'react';
import { ChatState, initialChatState, ChatMessage } from '../models/chatModels';
import { ChatAction } from './chatActions';
import { chatReducer } from './chatReducer';
import { ChatApiClient } from '../network/ChatApiClient';
import { FarmerChatSDK } from '../config/SDKConfig';
import { TokenStorage } from '../storage/TokenStorage';
import {
  TextPromptRequest,
  ImageAnalysisRequest,
  NewConversationRequest,
  TranscribeAudioRequest,
} from '../models/requests';
import { NetworkError } from '../network/NetworkError';
import { uriToFileName } from '../image/ImageProcessor';
import { getBase64FromUri } from '../image/ImageProcessor';
import { ConversationChatHistoryMessageItem } from '../models/responses';

// ─── Types ────────────────────────────────────────────────────────────────────

interface PendingRequest {
  type: 'text' | 'image';
  question: string;
  imageUri?: string;
  transcriptionId?: string;
  audioUri?: string;
  isRetry: boolean;
}

// ─── Hook ─────────────────────────────────────────────────────────────────────

export function useChatViewModel() {
  const [state, baseDispatch] = useReducer(chatReducer, initialChatState);

  // Stable ref to avoid stale closure in async callbacks
  const stateRef = useRef<ChatState>(state);
  stateRef.current = state;

  const pendingRequestRef = useRef<PendingRequest | null>(null);

  const getClient = useCallback((): ChatApiClient => {
    const config = FarmerChatSDK.getConfig();
    return new ChatApiClient(config);
  }, []);

  // ─── Conversation creation ─────────────────────────────────────────────────

  const ensureConversationId = useCallback(
    async (client: ChatApiClient): Promise<string> => {
      const existing = stateRef.current.conversationId;
      if (existing) return existing;

      // Ensure guest session is initialised. Throws with a user-facing message if the
      // device has hit its guest-user limit or there is no internet connection.
      await FarmerChatSDK.ensureTokens();

      const userId = await TokenStorage.getUserId();
      if (!userId) {
        throw new Error('User not authenticated — initialize_user did not return a user_id');
      }

      const config = FarmerChatSDK.getConfig();
      const req: NewConversationRequest = {
        user_id: userId,
        content_provider_id: config.contentProviderId ?? null,
      };
      const res = await client.createNewConversation(req);
      baseDispatch({ type: '_SET_CONVERSATION_ID', conversationId: res.conversation_id });
      return res.conversation_id;
    },
    [],
  );

  // ─── Generate unique IDs ───────────────────────────────────────────────────

  const genId = () => Math.random().toString(36).substr(2, 9);

  // ─── Send text prompt ──────────────────────────────────────────────────────

  const sendTextPrompt = useCallback(
    async (
      question: string,
      opts: {
        transcriptionId?: string;
        audioUri?: string;
        isRetry?: boolean;
        isFollowUp?: boolean;
        followUpQuestionId?: string;
      } = {},
    ) => {
      const messageId = genId();
      const userMsg: ChatMessage = {
        type: 'userMessage',
        id: messageId,
        text: question,
        audioUri: opts.audioUri,
      };
      const loadingMsg: ChatMessage = {
        type: 'loadingPlaceholder',
        id: genId(),
      };

      baseDispatch({ type: '_APPEND_MESSAGE', message: userMsg });
      baseDispatch({ type: '_APPEND_MESSAGE', message: loadingMsg });
      baseDispatch({ type: '_SET_LOADING', isLoading: true });
      baseDispatch({ type: '_SET_SUGGESTED_QUESTIONS', questions: [], ids: [] });

      pendingRequestRef.current = {
        type: 'text',
        question,
        transcriptionId: opts.transcriptionId,
        audioUri: opts.audioUri,
        isRetry: opts.isRetry ?? false,
      };

      try {
        const client = getClient();
        const conversationId = await ensureConversationId(client);
        const config = FarmerChatSDK.getConfig();

        const req: TextPromptRequest = {
          query: question,
          conversation_id: conversationId,
          message_id: messageId,
          weather_cta_triggered: false,
          triggered_input_type: opts.audioUri ? 'audio' : opts.isFollowUp ? 'follow_up' : 'text',
          use_entity_extraction: true,
          transcription_id: opts.transcriptionId,
          retry: opts.isRetry ?? false,
        };

        if (opts.isFollowUp && opts.followUpQuestionId) {
          await client.trackFollowUpClick(opts.followUpQuestionId).catch(() => {/* non-critical */});
        }

        const response = await client.sendTextPrompt(req);
        void config;

        if (response.error || !response.response) {
          baseDispatch({
            type: '_SET_ERROR',
            errorMessage: response.message ?? 'Failed to get a response.',
            failedMessageId: messageId,
          });
          return;
        }

        // Always fetch from the dedicated follow-up endpoint — it is the authoritative source.
        let followUpQs: string[] = [];
        let followUpIds: string[] = [];
        if (response.message_id && !response.hide_follow_up_question) {
          try {
            const fuResp = await client.getFollowUpQuestions(response.message_id);
            followUpQs = fuResp.questions?.map((q) => q.question) ?? [];
            followUpIds = fuResp.questions?.map((q) => q.follow_up_question_id) ?? [];
          } catch { /* non-critical */ }
        }
        // Fall back to inline questions from text prompt response
        if (followUpQs.length === 0) {
          followUpQs = response.follow_up_questions?.map((q) => q.question) ?? [];
          followUpIds = response.follow_up_questions?.map((q) => q.follow_up_question_id) ?? [];
        }

        const aiMsg: ChatMessage = {
          type: 'aiResponse',
          id: genId(),
          text: response.response,
          messageId: response.message_id,
          followUpQuestions: followUpQs.length ? followUpQs : undefined,
          followUpQuestionIds: followUpIds.length ? followUpIds : undefined,
          isPreGenerated: false,
          hideTtsSpeaker: response.hide_tts_speaker,
          contentProviderLogo: response.content_provider_logo,
        };

        baseDispatch({ type: '_REPLACE_LAST_LOADING_WITH', message: aiMsg });

        if (followUpQs.length) {
          baseDispatch({ type: '_SET_SUGGESTED_QUESTIONS', questions: followUpQs, ids: followUpIds });
        }
      } catch (err) {
        const errorMessage = err instanceof Error
          ? err.message
          : 'Something went wrong. Please try again.';
        baseDispatch({
          type: '_SET_ERROR',
          errorMessage,
          failedMessageId: messageId,
        });
      }
    },
    [getClient, ensureConversationId],
  );

  // ─── Send image analysis ───────────────────────────────────────────────────

  const sendImageAnalysis = useCallback(
    async (question: string, imageUri: string) => {
      const messageId = genId();
      const userMsg: ChatMessage = {
        type: 'userMessage',
        id: messageId,
        text: question || 'What can you tell me about this?',
        imageUri,
      };
      const loadingMsg: ChatMessage = {
        type: 'loadingPlaceholder',
        id: genId(),
      };

      baseDispatch({ type: '_APPEND_MESSAGE', message: userMsg });
      baseDispatch({ type: '_APPEND_MESSAGE', message: loadingMsg });
      baseDispatch({ type: '_SET_LOADING', isLoading: true });
      baseDispatch({ type: '_SET_SUGGESTED_QUESTIONS', questions: [], ids: [] });

      pendingRequestRef.current = {
        type: 'image',
        question,
        imageUri,
        isRetry: false,
      };

      try {
        const client = getClient();
        const conversationId = await ensureConversationId(client);

        const base64 = await getBase64FromUri(imageUri);
        const imageName = uriToFileName(imageUri);

        const req: ImageAnalysisRequest = {
          conversation_id: conversationId,
          image: base64,
          triggered_input_type: 'image',
          query: question || undefined,
          image_name: imageName,
          retry: false,
        };

        const response = await client.sendImageAnalysis(req);

        if (response.error || !response.response) {
          baseDispatch({
            type: '_SET_ERROR',
            errorMessage: response.message ?? 'Failed to analyze image.',
            failedMessageId: messageId,
          });
          return;
        }

        let imgFollowUpQs: string[] = [];
        let imgFollowUpIds: string[] = [];
        if (response.message_id) {
          try {
            const fuResp = await client.getFollowUpQuestions(response.message_id);
            imgFollowUpQs = fuResp.questions?.map((q) => q.question) ?? [];
            imgFollowUpIds = fuResp.questions?.map((q) => q.follow_up_question_id) ?? [];
          } catch { /* non-critical */ }
        }
        if (imgFollowUpQs.length === 0) {
          imgFollowUpQs = response.follow_up_questions?.map((q) => q.question) ?? [];
          imgFollowUpIds = response.follow_up_questions?.map((q) => q.follow_up_question_id) ?? [];
        }

        const aiMsg: ChatMessage = {
          type: 'aiResponse',
          id: genId(),
          text: response.response,
          messageId: response.message_id,
          followUpQuestions: imgFollowUpQs.length ? imgFollowUpQs : undefined,
          followUpQuestionIds: imgFollowUpIds.length ? imgFollowUpIds : undefined,
          isPreGenerated: false,
          hideTtsSpeaker: response.hide_tts_speaker,
          contentProviderLogo: response.content_provider_logo,
        };

        baseDispatch({ type: '_REPLACE_LAST_LOADING_WITH', message: aiMsg });

        if (imgFollowUpQs.length) {
          baseDispatch({ type: '_SET_SUGGESTED_QUESTIONS', questions: imgFollowUpQs, ids: imgFollowUpIds });
        }
      } catch (err) {
        const errorMessage = err instanceof Error
          ? err.message
          : 'Failed to analyze image. Please try again.';
        baseDispatch({
          type: '_SET_ERROR',
          errorMessage,
          failedMessageId: messageId,
        });
      }
    },
    [getClient, ensureConversationId],
  );

  // ─── Load chat history ─────────────────────────────────────────────────────

  const loadChatHistory = useCallback(
    async (conversationId: string, page = 1) => {
      if (page === 1) {
        baseDispatch({ type: '_SET_LOADING', isLoading: true });
      }

      try {
        // Ensure guest session before loading history
        await FarmerChatSDK.ensureTokens();

        const client = getClient();
        const response = await client.getChatHistory(conversationId, page);

        baseDispatch({
          type: '_SET_CONVERSATION_ID',
          conversationId,
        });

        // API returns newest-first; reverse section order while keeping items within
        // each section in original order (user query before AI response).
        const messages = mapHistoryToMessages(reverseHistorySections(response.data));

        if (page === 1) {
          baseDispatch({ type: '_SET_MESSAGES', messages });
          baseDispatch({ type: '_SET_INITIAL_HISTORY_LOADED' });

          // Show follow-up questions for the last AI response in history
          const lastAi = [...messages]
            .reverse()
            .find((m): m is Extract<typeof m, { type: 'aiResponse' }> => m.type === 'aiResponse');
          if (lastAi?.followUpQuestions?.length) {
            baseDispatch({
              type: '_SET_SUGGESTED_QUESTIONS',
              questions: lastAi.followUpQuestions,
              ids: lastAi.followUpQuestionIds ?? [],
            });
          }
        } else {
          baseDispatch({ type: '_PREPEND_MESSAGES', messages });
        }

        // If full page returned, assume more pages exist
        baseDispatch({
          type: '_SET_HISTORY_NEXT_PAGE',
          page: response.data.length >= 20 ? page + 1 : undefined,
        });
      } catch (err) {
        const errorMessage = err instanceof Error
          ? err.message
          : 'Failed to load chat history.';
        baseDispatch({ type: '_SET_ERROR', errorMessage });
      }
    },
    [getClient],
  );

  // ─── Synthesise audio ──────────────────────────────────────────────────────

  const synthesiseAudio = useCallback(async () => {
    const lastAiMsg = [...stateRef.current.messages]
      .reverse()
      .find((m): m is Extract<ChatMessage, { type: 'aiResponse' }> =>
        m.type === 'aiResponse',
      );

    if (!lastAiMsg?.messageId) return;

    baseDispatch({ type: '_SET_SYNTHESISE_AUDIO_LOADING', isLoading: true });

    try {
      const client = getClient();
      const config = FarmerChatSDK.getConfig();

      const userId = await TokenStorage.getUserId();
      if (!userId) return;
      const response = await client.synthesiseAudio({
        message_id: lastAiMsg.messageId,
        text: lastAiMsg.text,
        user_id: userId,
      });

      if (!response.error && response.audio) {
        baseDispatch({ type: '_SET_AUDIO_URL', url: response.audio });
      } else {
        baseDispatch({ type: '_SET_SYNTHESISE_AUDIO_LOADING', isLoading: false });
      }
    } catch {
      baseDispatch({ type: '_SET_SYNTHESISE_AUDIO_LOADING', isLoading: false });
    }
  }, [getClient]);

  // ─── Retry ─────────────────────────────────────────────────────────────────

  const retryLastRequest = useCallback(async () => {
    const pending = pendingRequestRef.current;
    if (!pending) return;

    // Remove the error state and the failed user message
    baseDispatch({ type: 'CLEAR_ERROR' });

    // Remove last user message before re-sending
    baseDispatch({
      type: '_SET_MESSAGES',
      messages: stateRef.current.messages.filter(
        (m) =>
          m.type !== 'userMessage' ||
          m.id !== stateRef.current.failedMessageId,
      ),
    });

    if (pending.type === 'text') {
      await sendTextPrompt(pending.question, {
        transcriptionId: pending.transcriptionId,
        audioUri: pending.audioUri,
        isRetry: true,
      });
    } else if (pending.type === 'image' && pending.imageUri) {
      await sendImageAnalysis(pending.question, pending.imageUri);
    }
  }, [sendTextPrompt, sendImageAnalysis]);

  // ─── Pregenerated content ──────────────────────────────────────────────────

  const initWithPregenerated = useCallback(
    (question: string, answer?: string, followUpQuestions?: string[]) => {
      const userMsg: ChatMessage = {
        type: 'userMessage',
        id: genId(),
        text: question,
      };

      if (answer) {
        const aiMsg: ChatMessage = {
          type: 'aiResponse',
          id: genId(),
          text: answer,
          followUpQuestions,
          isPreGenerated: true,
        };
        baseDispatch({ type: '_SET_MESSAGES', messages: [userMsg, aiMsg] });

        if (followUpQuestions?.length) {
          baseDispatch({
            type: '_SET_SUGGESTED_QUESTIONS',
            questions: followUpQuestions,
            ids: followUpQuestions.map(() => genId()),
          });
        }
      } else {
        baseDispatch({ type: '_SET_MESSAGES', messages: [userMsg] });
        // Fetch answer from API
        sendTextPrompt(question).catch(() => {/* handled internally */});
      }
    },
    [sendTextPrompt],
  );

  // ─── Transcribe audio then send ────────────────────────────────────────────

  const transcribeAndSendAudio = useCallback(
    async (audioUri: string, audioBase64: string) => {
      const userMsgId = genId();
      const loadingId = genId();

      const userMsg: ChatMessage = { type: 'userMessage', id: userMsgId, text: '🎤 ...', audioUri };
      const loadingMsg: ChatMessage = { type: 'loadingPlaceholder', id: loadingId };

      baseDispatch({ type: '_APPEND_MESSAGE', message: userMsg });
      baseDispatch({ type: '_APPEND_MESSAGE', message: loadingMsg });
      baseDispatch({ type: '_SET_LOADING', isLoading: true });

      try {
        const client = getClient();
        const conversationId = await ensureConversationId(client);

        const transcribeReq: TranscribeAudioRequest = {
          conversation_id: conversationId,
          query: audioBase64,
          message_reference_id: genId(),
          input_audio_encoding_format: 'OGG_OPUS',
          triggered_input_type: 'audio',
          editable_transcription: true,
        };

        const transcription = await client.transcribeAudio(transcribeReq);
        const heardText = transcription.heard_input_query;

        if (!heardText) {
          baseDispatch({ type: '_SET_ERROR', errorMessage: 'Could not understand audio. Please try again.' });
          return;
        }

        // Update user bubble with real transcribed text
        baseDispatch({ type: '_UPDATE_MESSAGE_TEXT', id: userMsgId, text: heardText });

        // Send text prompt directly (without adding another user bubble)
        const config = FarmerChatSDK.getConfig();
        void config;
        const req: TextPromptRequest = {
          query: heardText,
          conversation_id: conversationId,
          message_id: genId(),
          weather_cta_triggered: false,
          triggered_input_type: 'audio',
          use_entity_extraction: true,
          transcription_id: transcription.transcription_id,
          retry: false,
        };

        const response = await client.sendTextPrompt(req);

        if (response.error || !response.response) {
          baseDispatch({
            type: '_SET_ERROR',
            errorMessage: response.message ?? 'Failed to get a response.',
          });
          return;
        }

        let followUpQs: string[] = [];
        let followUpIds: string[] = [];
        if (response.message_id && !response.hide_follow_up_question) {
          try {
            const fuResp = await client.getFollowUpQuestions(response.message_id);
            followUpQs = fuResp.questions?.map((q) => q.question) ?? [];
            followUpIds = fuResp.questions?.map((q) => q.follow_up_question_id) ?? [];
          } catch { /* non-critical */ }
        }
        if (followUpQs.length === 0) {
          followUpQs = response.follow_up_questions?.map((q) => q.question) ?? [];
          followUpIds = response.follow_up_questions?.map((q) => q.follow_up_question_id) ?? [];
        }

        const aiMsg: ChatMessage = {
          type: 'aiResponse',
          id: genId(),
          text: response.response,
          messageId: response.message_id,
          followUpQuestions: followUpQs.length ? followUpQs : undefined,
          followUpQuestionIds: followUpIds.length ? followUpIds : undefined,
          isPreGenerated: false,
          hideTtsSpeaker: response.hide_tts_speaker,
        };

        baseDispatch({ type: '_REPLACE_LAST_LOADING_WITH', message: aiMsg });
        if (followUpQs.length) {
          baseDispatch({ type: '_SET_SUGGESTED_QUESTIONS', questions: followUpQs, ids: followUpIds });
        }
      } catch (err) {
        const errorMessage = err instanceof Error
          ? err.message
          : 'Failed to process voice message. Please try again.';
        baseDispatch({ type: '_SET_ERROR', errorMessage });
      }
    },
    [getClient, ensureConversationId],
  );

  // ─── Dispatch interceptor ──────────────────────────────────────────────────

  const dispatch = useCallback(
    (action: ChatAction) => {
      switch (action.type) {
        case 'INITIALIZE_WITH_QUESTION':
          sendTextPrompt(action.question, {
            transcriptionId: action.transcriptionId,
            audioUri: action.audioUri,
          }).catch(() => {/* handled internally */});
          break;

        case 'INITIALIZE_WITH_PREGENERATEDCONTENT':
          initWithPregenerated(
            action.question,
            action.answer,
            action.followUpQuestions,
          );
          break;

        case 'SEND_FOLLOW_UP_QUESTION':
          sendTextPrompt(action.question, {
            transcriptionId: action.transcriptionId,
            audioUri: action.audioUri,
            isFollowUp: true,
            followUpQuestionId: action.followUpQuestionId,
          }).catch(() => {/* handled internally */});
          break;

        case 'SEND_QUESTION_WITH_IMAGE':
          sendImageAnalysis(action.question, action.imageUri)
            .catch(() => {/* handled internally */});
          break;

        case 'TRANSCRIBE_AND_SEND_AUDIO':
          transcribeAndSendAudio(action.audioUri, action.audioBase64)
            .catch(() => {/* handled internally */});
          break;

        case 'LOAD_CHAT_HISTORY':
          loadChatHistory(action.conversationId, action.page)
            .catch(() => {/* handled internally */});
          break;

        case 'RETRY_LAST_REQUEST':
          retryLastRequest().catch(() => {/* handled internally */});
          break;

        case 'SYNTHESISE_AUDIO':
          synthesiseAudio().catch(() => {/* handled internally */});
          break;

        default:
          baseDispatch(action);
      }
    },
    [
      sendTextPrompt,
      initWithPregenerated,
      sendImageAnalysis,
      transcribeAndSendAudio,
      loadChatHistory,
      retryLastRequest,
      synthesiseAudio,
    ],
  );

  return {
    state,
    dispatch,
    conversationId: state.conversationId,
  };
}

// ─── History ordering ─────────────────────────────────────────────────────────

/**
 * API returns sections newest-first. Each section is [user_query, ai_response, follow_ups].
 * Reverses section order while keeping items within each section in original order.
 */
function reverseHistorySections(
  items: ConversationChatHistoryMessageItem[],
): ConversationChatHistoryMessageItem[] {
  const sections: ConversationChatHistoryMessageItem[][] = [];
  let current: ConversationChatHistoryMessageItem[] = [];
  for (const item of items) {
    // User message types (1=text, 2=audio, 11=image) start a new section
    const startsSection = [1, 2, 11].includes(item.message_type_id);
    if (startsSection && current.length > 0) {
      sections.push(current);
      current = [];
    }
    current.push(item);
  }
  if (current.length > 0) sections.push(current);
  return sections.reverse().flat();
}

// ─── History mapping ──────────────────────────────────────────────────────────

/**
 * Maps ordered history items to ChatMessage list.
 * Uses message_type_id to identify item roles:
 *   1 = user text, 2 = user audio, 3 = AI response,
 *   7 = follow-up questions block (separate item, attached to preceding AI response),
 *   11 = user image
 */
function mapHistoryToMessages(
  items: ConversationChatHistoryMessageItem[],
): ChatMessage[] {
  const messages: ChatMessage[] = [];

  for (const item of items) {
    switch (item.message_type_id) {
      case 1: // user text query
      case 2: { // user audio query
        const text = item.query_text ?? item.heard_query_text ?? '';
        if (text) {
          messages.push({
            type: 'userMessage',
            id: item.message_id + '_q',
            text,
          });
        }
        break;
      }
      case 3: { // AI text response — follow-up questions arrive via a separate type_7 item
        if (item.response_text) {
          messages.push({
            type: 'aiResponse',
            id: item.message_id + '_a',
            text: item.response_text,
            messageId: item.message_id,
            followUpQuestions: undefined,
            followUpQuestionIds: undefined,
            isPreGenerated: false,
            hideTtsSpeaker: item.hide_tts_speaker,
            contentProviderLogo: item.content_provider_logo,
          });
        }
        break;
      }
      case 7: { // follow-up questions block — attach to the preceding AI response
        const qs = item.questions;
        if (!qs?.length) break;
        const lastAiIdx = messages.reduce<number>(
          (idx, m, i) => (m.type === 'aiResponse' ? i : idx),
          -1,
        );
        if (lastAiIdx >= 0) {
          const lastAi = messages[lastAiIdx] as Extract<ChatMessage, { type: 'aiResponse' }>;
          messages[lastAiIdx] = {
            ...lastAi,
            followUpQuestions: qs.map((q) => q.question),
            followUpQuestionIds: qs.map((q) => q.follow_up_question_id),
          };
        }
        break;
      }
      case 11: { // user image query
        messages.push({
          type: 'userMessage',
          id: item.message_id + '_q',
          text: item.query_text ?? '',
          imageUri: item.query_media_file_url,
        });
        break;
      }
      default:
        break;
    }
  }

  return messages;
}
