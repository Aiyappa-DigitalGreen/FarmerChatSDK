/**
 * ChatApiClient.ts
 * Wraps all FarmerChat REST endpoints with auth, retry-on-401, and error mapping.
 * Reads tokens from TokenStorage on every request (no in-memory token state).
 */

import { SDKConfiguration } from '../config/SDKConfig';
import {
  API_NEW_CONVERSATION,
  API_GET_TEXT_PROMPT,
  API_IMAGE_ANALYSIS,
  API_FOLLOW_UP_QUESTIONS,
  API_FOLLOW_UP_CLICK,
  API_SYNTHESISE_AUDIO,
  API_CHAT_HISTORY,
  API_CONVERSATION_LIST,
  API_TRANSCRIBE_AUDIO,
} from '../config/constants';
import {
  NewConversationRequest,
  TextPromptRequest,
  ImageAnalysisRequest,
  SynthesiseAudioRequest,
  FollowUpQuestionClickRequest,
  TranscribeAudioRequest,
} from '../models/requests';
import {
  NewConversationResponse,
  TextPromptResponse,
  ImageAnalysisResponse,
  FollowUpQuestionsResponse,
  SynthesiseAudioResponse,
  ConversationChatHistoryResponse,
  ConversationListResponse,
  GetVoiceResponse,
} from '../models/responses';
import { addAuthHeaders } from './AuthInterceptor';
import { refreshIfNeeded } from './TokenRefreshHandler';
import {
  NetworkError,
  NetworkUnavailableError,
  ApiError,
  ServerError,
} from './NetworkError';
import { buildDeviceInfoHeader } from '../utils/deviceInfo';
import { TokenStorage } from '../storage/TokenStorage';

export class ChatApiClient {
  private baseUrl: string;
  private sdkApiKey: string;

  constructor(config: SDKConfiguration) {
    this.baseUrl = config.baseUrl;
    this.sdkApiKey = config.sdkApiKey;
  }

  // ─── Public API Methods ───────────────────────────────────────────────────

  async createNewConversation(
    req: NewConversationRequest,
  ): Promise<NewConversationResponse> {
    return this.request<NewConversationResponse>(API_NEW_CONVERSATION, {
      method: 'POST',
      body: JSON.stringify(req),
    });
  }

  async sendTextPrompt(req: TextPromptRequest): Promise<TextPromptResponse> {
    return this.request<TextPromptResponse>(API_GET_TEXT_PROMPT, {
      method: 'POST',
      body: JSON.stringify(req),
    });
  }

  async sendImageAnalysis(
    req: ImageAnalysisRequest,
  ): Promise<ImageAnalysisResponse> {
    return this.request<ImageAnalysisResponse>(API_IMAGE_ANALYSIS, {
      method: 'POST',
      body: JSON.stringify(req),
    });
  }

  async getFollowUpQuestions(
    messageId: string,
    useLatestPrompt = true,
  ): Promise<FollowUpQuestionsResponse> {
    const params = new URLSearchParams({
      message_id: messageId,
      use_latest_prompt: String(useLatestPrompt),
    });
    return this.request<FollowUpQuestionsResponse>(
      `${API_FOLLOW_UP_QUESTIONS}?${params.toString()}`,
      { method: 'GET' },
    );
  }

  async trackFollowUpClick(followUpQuestion: string): Promise<void> {
    const body: FollowUpQuestionClickRequest = {
      follow_up_question: followUpQuestion,
    };
    await this.request<Record<string, unknown>>(API_FOLLOW_UP_CLICK, {
      method: 'POST',
      body: JSON.stringify(body),
    });
  }

  async synthesiseAudio(
    req: SynthesiseAudioRequest,
  ): Promise<SynthesiseAudioResponse> {
    return this.request<SynthesiseAudioResponse>(API_SYNTHESISE_AUDIO, {
      method: 'POST',
      body: JSON.stringify(req),
    });
  }

  async getChatHistory(
    conversationId: string,
    page: number,
  ): Promise<ConversationChatHistoryResponse> {
    const params = new URLSearchParams({
      conversation_id: conversationId,
      page: String(page),
    });
    return this.request<ConversationChatHistoryResponse>(
      `${API_CHAT_HISTORY}?${params.toString()}`,
      { method: 'GET' },
    );
  }

  async getConversationList(page: number): Promise<ConversationListResponse> {
    const userId = await TokenStorage.getUserId() ?? '';
    const params = new URLSearchParams({
      user_id: userId,
      page: String(page),
    });
    return this.request<ConversationListResponse>(
      `${API_CONVERSATION_LIST}?${params.toString()}`,
      { method: 'GET' },
    );
  }

  async transcribeAudio(
    req: TranscribeAudioRequest,
  ): Promise<GetVoiceResponse> {
    return this.request<GetVoiceResponse>(API_TRANSCRIBE_AUDIO, {
      method: 'POST',
      body: JSON.stringify(req),
    });
  }

  // ─── Core request with auth + 401 retry ──────────────────────────────────

  private async request<T>(
    path: string,
    options: RequestInit,
    isRetry = false,
  ): Promise<T> {
    // Read tokens from secure storage on every request
    const accessToken = (await TokenStorage.getAccessToken()) ?? '';
    const deviceId = await TokenStorage.getOrCreateDeviceId();
    const deviceInfo = buildDeviceInfoHeader(deviceId);

    const authedOptions = addAuthHeaders(options, accessToken, deviceInfo, this.sdkApiKey);
    const url = `${this.baseUrl}/${path}`;

    let response: Response;
    try {
      response = await fetch(url, authedOptions);
    } catch {
      throw new NetworkUnavailableError(path);
    }

    if (response.status === 401 && !isRetry) {
      try {
        await refreshIfNeeded(this.baseUrl);
        return this.request<T>(path, options, true);
      } catch (refreshErr) {
        if (refreshErr instanceof NetworkError) throw refreshErr;
        throw new NetworkError(401, path, 'Authentication failed');
      }
    }

    if (response.status >= 400 && response.status < 500) {
      let serverMsg: string | undefined;
      try {
        const errBody = (await response.json()) as { message?: string; detail?: string };
        serverMsg = errBody.message ?? errBody.detail;
      } catch {
        // ignore JSON parse errors on error bodies
      }
      throw new ApiError(response.status, path, serverMsg);
    }

    if (response.status >= 500) {
      throw new ServerError(response.status, path);
    }

    try {
      return (await response.json()) as T;
    } catch {
      throw new NetworkError(response.status, path, 'Failed to parse server response');
    }
  }
}
