// API endpoint path constants (relative to base URL — farmerchat.farmstack.co requires "api/" prefix)
export const API_NEW_CONVERSATION = 'api/chat/new_conversation/';
export const API_GET_TEXT_PROMPT = 'api/chat/get_answer_for_text_query/';
export const API_IMAGE_ANALYSIS = 'api/chat/image_analysis/';
export const API_FOLLOW_UP_QUESTIONS = 'api/chat/follow_up_questions/';
export const API_FOLLOW_UP_CLICK = 'api/chat/follow_up_question_click/';
export const API_SYNTHESISE_AUDIO = 'api/chat/synthesise_audio/';
export const API_CHAT_HISTORY = 'api/chat/conversation_chat_history/';
export const API_CONVERSATION_LIST = 'api/chat/conversation_list/';
export const API_TRANSCRIBE_AUDIO = 'api/chat/transcribe_audio/';
export const API_REFRESH_TOKEN = 'api/user/get_new_access_token/';
export const API_SEND_TOKENS = 'api/user/send_tokens/';
export const API_INITIALIZE_USER = 'api/user/initialize_user/';

// API Key used for guest user initialization and fallback token refresh
export const GUEST_API_KEY = 'Y2K3kW5R9uQ0fL2X8zI7hT3aJ7';

// Build version header value
export const BUILD_VERSION = 'v2';

// Default UI constants
export const PRIMARY_GREEN = '#2E7D32';
export const LIGHT_GREEN = '#4CAF50';
export const SURFACE_COLOR = '#F5F5F5';
export const WHITE = '#FFFFFF';
export const TEXT_PRIMARY = '#212121';
export const TEXT_SECONDARY = '#757575';
export const DIVIDER_COLOR = '#E0E0E0';
export const ERROR_COLOR = '#D32F2F';
export const BUBBLE_AI_BACKGROUND = '#F1F8E9';

// Waveform
export const WAVEFORM_BAR_COUNT = 40;
export const WAVEFORM_UPDATE_INTERVAL_MS = 60;

// Pagination
export const DEFAULT_PAGE_SIZE = 20;

// Audio
export const AUDIO_SAMPLE_RATE = 16000;
export const AUDIO_ENCODING = 'LINEAR16';
