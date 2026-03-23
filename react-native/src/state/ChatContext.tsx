/**
 * ChatContext.tsx
 * React context that provides the chat view model to the component tree.
 */

import React, {
  createContext,
  useContext,
  useMemo,
  type ReactNode,
} from 'react';
import { useChatViewModel } from './useChatViewModel';
import { ChatState } from '../models/chatModels';
import { ChatAction } from './chatActions';

// ─── Context shape ────────────────────────────────────────────────────────────

interface ChatContextValue {
  state: ChatState;
  dispatch: (action: ChatAction) => void;
  conversationId?: string;
}

const ChatContext = createContext<ChatContextValue | null>(null);

// ─── Provider ─────────────────────────────────────────────────────────────────

interface ChatProviderProps {
  children: ReactNode;
  /** Optional: pre-set a conversation ID (e.g. to resume an existing chat) */
  initialConversationId?: string;
}

export function ChatProvider({
  children,
  initialConversationId,
}: ChatProviderProps): React.JSX.Element {
  const { state, dispatch, conversationId } = useChatViewModel();

  // Kick off history load if a conversation ID was provided
  const hasLoadedRef = React.useRef(false);
  React.useEffect(() => {
    if (
      initialConversationId &&
      !hasLoadedRef.current &&
      !state.isInitialHistoryLoaded
    ) {
      hasLoadedRef.current = true;
      dispatch({
        type: 'LOAD_CHAT_HISTORY',
        conversationId: initialConversationId,
      });
    }
  }, [initialConversationId, dispatch, state.isInitialHistoryLoaded]);

  const value = useMemo<ChatContextValue>(
    () => ({ state, dispatch, conversationId }),
    [state, dispatch, conversationId],
  );

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
}

// ─── Consumer hook ────────────────────────────────────────────────────────────

export function useChatContext(): ChatContextValue {
  const ctx = useContext(ChatContext);
  if (!ctx) {
    throw new Error('useChatContext must be used inside <ChatProvider>');
  }
  return ctx;
}
