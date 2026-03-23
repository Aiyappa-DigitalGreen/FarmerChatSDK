/**
 * ChatScreen.tsx
 * Main chat screen with app bar, message thread, follow-up bar, and input bar.
 */

import React, {
  memo,
  useState,
  useCallback,
  useRef,
  useMemo,
} from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ChatThread } from '../chat/ChatThread';
import { ChatInputBar } from '../input/ChatInputBar';
import { VoiceInputOverlay } from '../input/VoiceInputOverlay';
import { PhotoInputSheet } from '../input/PhotoInputSheet';
import { FollowUpQuestionsBar } from '../chat/FollowUpQuestionsBar';
import { useChatContext } from '../../state/ChatContext';
import { AudioPlayerService } from '../../audio/AudioPlayerService';
import { ImagePickerResult } from '../../image/ImagePickerService';
import {
  PRIMARY_GREEN,
  TEXT_PRIMARY,
  WHITE,
  DIVIDER_COLOR,
  SURFACE_COLOR,
} from '../../config/constants';

interface ChatScreenProps {
  onOpenHistory: () => void;
  onClose: () => void;
}

export const ChatScreen = memo(function ChatScreen({
  onOpenHistory,
  onClose,
}: ChatScreenProps): React.JSX.Element {
  const { state } = useChatContext();

  const [showVoiceOverlay, setShowVoiceOverlay] = useState(false);
  const [showPhotoSheet, setShowPhotoSheet] = useState(false);
  const [selectedImage, setSelectedImage] = useState<ImagePickerResult | null>(null);
  const [pendingAudio, setPendingAudio] = useState<
    { audioUri: string; base64: string } | undefined
  >(undefined);

  // Stable AudioPlayerService instance for this screen
  const audioPlayer = useRef(new AudioPlayerService()).current;

  // Clean up audio on unmount
  React.useEffect(() => {
    return () => {
      audioPlayer.stop().catch(() => {/* ignore */});
      audioPlayer.destroy();
    };
  }, [audioPlayer]);

  const handleMicPress = useCallback(() => {
    setShowVoiceOverlay(true);
  }, []);

  const handleCameraPress = useCallback(() => {
    setShowPhotoSheet(true);
  }, []);

  const handleVoiceConfirm = useCallback(
    (result: { audioUri: string; base64: string }) => {
      setShowVoiceOverlay(false);
      setPendingAudio(result);
    },
    [],
  );

  const handleVoiceCancel = useCallback(() => {
    setShowVoiceOverlay(false);
  }, []);

  const handleImageSelected = useCallback(
    (result: ImagePickerResult) => {
      setShowPhotoSheet(false);
      setSelectedImage(result);
    },
    [],
  );

  const handleAudioConsumed = useCallback(() => {
    setPendingAudio(undefined);
  }, []);

  const hasFollowUps =
    (state.suggestedQuestions?.length ?? 0) > 0;

  const followUpBar = useMemo(() => {
    if (!hasFollowUps) return null;
    return (
      <FollowUpQuestionsBar
        questions={state.suggestedQuestions!}
        questionIds={state.suggestedQuestionIds}
      />
    );
  }, [hasFollowUps, state.suggestedQuestions, state.suggestedQuestionIds]);

  return (
    <SafeAreaView style={styles.screen} edges={['top', 'left', 'right']}>
      {/* App Bar */}
      <View style={styles.appBar}>
        <View style={styles.appBarLeft}>
          <View style={styles.logoMark}>
            <Text style={styles.logoMarkText}>🌱</Text>
          </View>
          <Text style={styles.appBarTitle}>FarmerChat</Text>
        </View>
        <View style={styles.appBarActions}>
          <TouchableOpacity
            onPress={onOpenHistory}
            style={styles.appBarButton}
            hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
            accessibilityRole="button"
            accessibilityLabel="Open chat history"
          >
            <HistoryIcon />
          </TouchableOpacity>
          <TouchableOpacity
            onPress={onClose}
            style={styles.appBarButton}
            hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
            accessibilityRole="button"
            accessibilityLabel="Close chat"
          >
            <Text style={styles.closeIcon}>✕</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Main content area */}
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        keyboardVerticalOffset={0}
      >
        {/* Message thread */}
        <ChatThread
          audioPlayer={audioPlayer}
          onNearTop={() => {
            if (
              state.conversationId &&
              state.historyNextPage !== undefined
            ) {
              // Load more history
            }
          }}
        />

        {/* Follow-up questions */}
        {followUpBar}

        {/* Input bar — hidden while voice overlay is active */}
        {!showVoiceOverlay && (
          <SafeAreaView edges={['bottom']} style={styles.inputSafeArea}>
            <ChatInputBar
              onMicPress={handleMicPress}
              onCameraPress={handleCameraPress}
              selectedImage={selectedImage}
              onClearImage={() => setSelectedImage(null)}
              pendingAudioResult={pendingAudio}
              onPendingAudioConsumed={handleAudioConsumed}
            />
          </SafeAreaView>
        )}
      </KeyboardAvoidingView>

      {/* Voice recording overlay */}
      <VoiceInputOverlay
        visible={showVoiceOverlay}
        onCancel={handleVoiceCancel}
        onConfirm={handleVoiceConfirm}
      />

      {/* Photo source action sheet */}
      <PhotoInputSheet
        visible={showPhotoSheet}
        onClose={() => setShowPhotoSheet(false)}
        onImageSelected={handleImageSelected}
      />
    </SafeAreaView>
  );
});

// ─── Icon primitives ──────────────────────────────────────────────────────────

function HistoryIcon() {
  return (
    <View style={iconStyles.container}>
      <Text style={iconStyles.emoji}>📋</Text>
    </View>
  );
}

const iconStyles = StyleSheet.create({
  container: {
    width: 32,
    height: 32,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emoji: {
    fontSize: 18,
  },
});

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: SURFACE_COLOR,
  },
  flex: {
    flex: 1,
  },
  appBar: {
    height: 56,
    backgroundColor: WHITE,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: DIVIDER_COLOR,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 3,
  },
  appBarLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  logoMark: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: '#E8F5E9',
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoMarkText: {
    fontSize: 18,
  },
  appBarTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: TEXT_PRIMARY,
  },
  appBarActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  appBarButton: {
    width: 40,
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 20,
  },
  closeIcon: {
    fontSize: 16,
    color: TEXT_PRIMARY,
    fontWeight: '600',
  },
  inputSafeArea: {
    backgroundColor: WHITE,
  },
  primaryColor: {
    color: PRIMARY_GREEN,
  },
});
