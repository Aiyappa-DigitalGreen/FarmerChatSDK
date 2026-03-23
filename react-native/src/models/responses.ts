/**
 * responses.ts
 * TypeScript interfaces for all API response payloads.
 */

// ─── Text Prompt ─────────────────────────────────────────────────────────────

export interface IntentClassificationOutput {
  intent?: string;
  confidence?: string;       // API returns "high" | "medium" | "low" — NOT a number
  asset_type?: string;
  asset_name?: string;
  asset_status?: string;
  concern?: string;
  stage?: string;
  likely_activity?: string;
  rephrased_query?: string;
  seasonal_relevance?: string;
}

export interface FollowUpQuestionOption {
  follow_up_question_id: string;
  sequence: number;
  question: string;
}

export interface TextPromptResponse {
  error: boolean;
  message?: string;
  message_id?: string;
  response?: string;
  translated_response?: string;
  follow_up_questions?: FollowUpQuestionOption[];
  section_message_id?: string;
  content_provider_logo?: string;
  hide_feedback_icons?: boolean;
  hide_follow_up_question?: boolean;
  hide_share_icon?: boolean;
  hide_tts_speaker?: boolean;
  points?: number;
  intent_classification_output?: IntentClassificationOutput;
}

// ─── Image Analysis ───────────────────────────────────────────────────────────

export interface ImageAnalysisResponse {
  error: boolean;
  message: string;
  message_id: string;
  response: string;
  follow_up_questions?: FollowUpQuestionOption[];
  content_provider_logo?: string;
  hide_tts_speaker?: boolean;
  points?: number;
}

// ─── Follow-up Questions ──────────────────────────────────────────────────────

export interface Question {
  follow_up_question_id: string;
  question: string;
  sequence: number;
}

export interface FollowUpQuestionsResponse {
  message_id: string;
  section_message_id: string;
  questions?: Question[];
}

// ─── Synthesise Audio ─────────────────────────────────────────────────────────

export interface SynthesiseAudioResponse {
  audio?: string;
  text?: string;
  error: boolean;
}

// ─── Conversation Chat History ────────────────────────────────────────────────

export interface ConversationChatHistoryQuestion {
  follow_up_question_id: string;
  question: string;
  sequence: number;
}

export interface ConversationChatHistoryMessageItem {
  /** 1 = user text, 2 = AI text, 3 = user image, etc. */
  message_type_id: number;
  message_type: string;
  message_id: string;
  query_text?: string;
  heard_query_text?: string;
  response_text?: string;
  questions?: ConversationChatHistoryQuestion[];
  query_media_file_url?: string;
  response_media_file_url?: string;
  content_provider_logo?: string;
  hide_tts_speaker?: boolean;
}

export interface ConversationChatHistoryResponse {
  conversation_id: string;
  data: ConversationChatHistoryMessageItem[];
}

// ─── Conversation List ────────────────────────────────────────────────────────

export interface ConversationListItem {
  conversation_id: string;
  conversation_title?: string;
  created_on: string;
  message_type?: string;
  grouping?: string;
  content_provider_logo?: string;
}

// API returns a plain JSON array — no wrapper object, no pagination fields.
export type ConversationListResponse = ConversationListItem[];

// ─── New Conversation ─────────────────────────────────────────────────────────

export interface NewConversationResponse {
  conversation_id: string;
  message: string;
  show_popup: boolean;
}

// ─── Voice / Transcription ────────────────────────────────────────────────────

export interface GetVoiceResponse {
  message?: string;
  heard_input_query?: string;
  confidence_score?: number;
  error: boolean;
  message_id: string;
  transcription_id?: string;
}

// ─── Token Refresh ────────────────────────────────────────────────────────────

export interface RefreshTokenResponse {
  access_token?: string;
  refresh_token?: string;
}
