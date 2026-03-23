/**
 * ChatModal.tsx  (PUBLIC API)
 * Full-screen modal that wraps the FarmerChat UI.
 * Hosts ChatProvider and routes between ChatScreen and ChatHistoryScreen.
 */

import React, { useState, useCallback, useEffect, memo } from 'react';
import { Modal, StyleSheet, View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { ChatProvider } from '../state/ChatContext';
import { ChatScreen } from './screens/ChatScreen';
import { ChatHistoryScreen } from './screens/ChatHistoryScreen';
import { useChatContext } from '../state/ChatContext';

// ─── Public Props ─────────────────────────────────────────────────────────────

export interface ChatModalProps {
  /** Controls whether the modal is displayed */
  visible: boolean;
  /** Called when the user closes the modal */
  onClose: () => void;
  /** Optional: resume an existing conversation */
  conversationId?: string;
}

// ─── Inner navigator (needs context) ─────────────────────────────────────────

type Screen = 'chat' | 'history';

function ChatModalNavigator({
  onClose,
  currentScreen,
  setCurrentScreen,
}: {
  onClose: () => void;
  currentScreen: Screen;
  setCurrentScreen: (s: Screen) => void;
}): React.JSX.Element {
  const { dispatch } = useChatContext();

  const handleOpenHistory = useCallback(() => {
    setCurrentScreen('history');
  }, [setCurrentScreen]);

  const handleBackFromHistory = useCallback(() => {
    setCurrentScreen('chat');
  }, [setCurrentScreen]);

  const handleSelectConversation = useCallback(
    (conversationId: string) => {
      setCurrentScreen('chat');
      dispatch({ type: 'LOAD_CHAT_HISTORY', conversationId });
    },
    [dispatch, setCurrentScreen],
  );

  if (currentScreen === 'history') {
    return (
      <ChatHistoryScreen
        onBack={handleBackFromHistory}
        onSelectConversation={handleSelectConversation}
      />
    );
  }

  return (
    <ChatScreen
      onOpenHistory={handleOpenHistory}
      onClose={onClose}
    />
  );
}

// ─── Public component ─────────────────────────────────────────────────────────

export const ChatModal = memo(function ChatModal({
  visible,
  onClose,
  conversationId,
}: ChatModalProps): React.JSX.Element {
  const [currentScreen, setCurrentScreen] = useState<Screen>('chat');

  // Reset to chat screen whenever the modal is closed
  useEffect(() => {
    if (!visible) {
      setCurrentScreen('chat');
    }
  }, [visible]);

  // Hardware back button (Android): go from history → chat, or close from chat
  const handleRequestClose = useCallback(() => {
    if (currentScreen === 'history') {
      setCurrentScreen('chat');
    } else {
      onClose();
    }
  }, [currentScreen, onClose]);

  return (
    <Modal
      visible={visible}
      animationType="slide"
      presentationStyle="pageSheet"
      onRequestClose={handleRequestClose}
      statusBarTranslucent={false}
    >
      <SafeAreaProvider>
        <View style={styles.container}>
          <ChatProvider initialConversationId={conversationId}>
            <ChatModalNavigator
              onClose={onClose}
              currentScreen={currentScreen}
              setCurrentScreen={setCurrentScreen}
            />
          </ChatProvider>
        </View>
      </SafeAreaProvider>
    </Modal>
  );
});

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
});
