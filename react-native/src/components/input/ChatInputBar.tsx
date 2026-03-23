/**
 * ChatInputBar.tsx
 * Bottom input bar with text, voice, image, and send controls.
 */

import React, {
  memo,
  useState,
  useRef,
  useCallback,
  useEffect,
} from 'react';
import {
  View,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Image,
  Text,
  Platform,
  Keyboard,
  ActivityIndicator,
} from 'react-native';
import { useChatContext } from '../../state/ChatContext';
import { ImagePickerResult } from '../../image/ImagePickerService';
import {
  PRIMARY_GREEN,
  TEXT_SECONDARY,
  WHITE,
  DIVIDER_COLOR,
  SURFACE_COLOR,
} from '../../config/constants';

interface ChatInputBarProps {
  onMicPress: () => void;
  onCameraPress: () => void;
  selectedImage?: ImagePickerResult | null;
  onClearImage?: () => void;
  /** Called when a voice recording is complete and ready to send */
  pendingAudioResult?: { audioUri: string; base64: string };
  onPendingAudioConsumed?: () => void;
}

export const ChatInputBar = memo(function ChatInputBar({
  onMicPress,
  onCameraPress,
  selectedImage,
  onClearImage,
  pendingAudioResult,
  onPendingAudioConsumed,
}: ChatInputBarProps): React.JSX.Element {
  const { state, dispatch } = useChatContext();
  const [text, setText] = useState('');
  const inputRef = useRef<TextInput>(null);

  // Expose image setter to parent via imperative handle is not used;
  // instead PhotoInputSheet calls onImageSelected which is wired from ChatScreen.
  // We use a ref-based approach instead.

  const handleSend = useCallback(() => {
    const trimmed = text.trim();

    if (selectedImage) {
      dispatch({
        type: 'SEND_QUESTION_WITH_IMAGE',
        question: trimmed,
        imageUri: selectedImage.uri,
      });
      onClearImage?.();
      setText('');
      Keyboard.dismiss();
      return;
    }

    if (trimmed.length === 0) return;

    dispatch({ type: 'INITIALIZE_WITH_QUESTION', question: trimmed });
    setText('');
    Keyboard.dismiss();
  }, [text, selectedImage, onClearImage, dispatch]);

  // Handle completed voice recording — transcribes then sends
  useEffect(() => {
    if (pendingAudioResult) {
      dispatch({
        type: 'TRANSCRIBE_AND_SEND_AUDIO',
        audioUri: pendingAudioResult.audioUri,
        audioBase64: pendingAudioResult.base64,
      });
      onPendingAudioConsumed?.();
    }
  }, [pendingAudioResult, dispatch, onPendingAudioConsumed]);

  const canSend = text.trim().length > 0 || selectedImage !== null;
  const isDisabled = state.isLoading;

  return (
    <View style={styles.container}>
      {/* Image preview strip */}
      {selectedImage && (
        <View style={styles.imagePreviewContainer}>
          <Image
            source={{ uri: selectedImage.uri }}
            style={styles.imagePreview}
            resizeMode="cover"
          />
          <TouchableOpacity
            onPress={onClearImage}
            style={styles.removeImageButton}
            hitSlop={{ top: 4, bottom: 4, left: 4, right: 4 }}
            accessibilityLabel="Remove selected image"
          >
            <Text style={styles.removeImageText}>✕</Text>
          </TouchableOpacity>
        </View>
      )}

      <View style={styles.inputRow}>
        {/* Camera icon */}
        <TouchableOpacity
          onPress={onCameraPress}
          disabled={isDisabled}
          style={[styles.iconButton, isDisabled && styles.iconDisabled]}
          accessibilityRole="button"
          accessibilityLabel="Attach image"
          hitSlop={{ top: 8, bottom: 8, left: 4, right: 4 }}
        >
          <CameraIcon />
        </TouchableOpacity>

        {/* Text input */}
        <TextInput
          ref={inputRef}
          style={styles.textInput}
          value={text}
          onChangeText={setText}
          placeholder="Ask about your crops..."
          placeholderTextColor={TEXT_SECONDARY}
          multiline
          maxLength={2000}
          returnKeyType="default"
          editable={!isDisabled}
          onSubmitEditing={() => {
            if (canSend) handleSend();
          }}
          accessible
          accessibilityLabel="Message input"
        />

        {/* Microphone or Send */}
        {canSend ? (
          <TouchableOpacity
            onPress={handleSend}
            disabled={isDisabled}
            style={[styles.sendButton, isDisabled && styles.sendButtonDisabled]}
            accessibilityRole="button"
            accessibilityLabel="Send message"
          >
            {isDisabled ? (
              <ActivityIndicator size="small" color={WHITE} />
            ) : (
              <SendIcon />
            )}
          </TouchableOpacity>
        ) : (
          <TouchableOpacity
            onPress={onMicPress}
            disabled={isDisabled}
            style={[styles.iconButton, isDisabled && styles.iconDisabled]}
            accessibilityRole="button"
            accessibilityLabel="Record voice message"
            hitSlop={{ top: 8, bottom: 8, left: 4, right: 4 }}
          >
            <MicIcon />
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
});

// ─── Icon components ──────────────────────────────────────────────────────────

function CameraIcon() {
  return (
    <View style={iconStyles.camera}>
      <View style={iconStyles.cameraBody} />
      <View style={iconStyles.cameraLens} />
    </View>
  );
}

function MicIcon() {
  return (
    <View style={iconStyles.mic}>
      <View style={iconStyles.micBody} />
      <View style={iconStyles.micBase} />
    </View>
  );
}

function SendIcon() {
  return (
    <Text style={{ color: WHITE, fontSize: 18, fontWeight: '700' }}>↑</Text>
  );
}

const iconStyles = StyleSheet.create({
  camera: {
    width: 24,
    height: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cameraBody: {
    width: 20,
    height: 14,
    borderRadius: 3,
    borderWidth: 2,
    borderColor: TEXT_SECONDARY,
  },
  cameraLens: {
    position: 'absolute',
    width: 8,
    height: 8,
    borderRadius: 4,
    borderWidth: 1.5,
    borderColor: TEXT_SECONDARY,
  },
  mic: {
    width: 24,
    height: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  micBody: {
    width: 10,
    height: 14,
    borderRadius: 5,
    borderWidth: 2,
    borderColor: TEXT_SECONDARY,
    marginBottom: 1,
  },
  micBase: {
    width: 16,
    height: 4,
    borderTopLeftRadius: 8,
    borderTopRightRadius: 8,
    borderWidth: 1.5,
    borderColor: TEXT_SECONDARY,
    borderBottomWidth: 0,
  },
});

const styles = StyleSheet.create({
  container: {
    backgroundColor: WHITE,
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: DIVIDER_COLOR,
    paddingHorizontal: 12,
    paddingTop: 8,
    paddingBottom: Platform.OS === 'ios' ? 4 : 8,
  },
  imagePreviewContainer: {
    marginBottom: 8,
    alignSelf: 'flex-start',
  },
  imagePreview: {
    width: 80,
    height: 60,
    borderRadius: 8,
  },
  removeImageButton: {
    position: 'absolute',
    top: -6,
    right: -6,
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#757575',
    alignItems: 'center',
    justifyContent: 'center',
  },
  removeImageText: {
    color: WHITE,
    fontSize: 10,
    fontWeight: '700',
  },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 8,
    minHeight: 44,
  },
  iconButton: {
    width: 36,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 18,
    backgroundColor: SURFACE_COLOR,
    flexShrink: 0,
    marginBottom: 2,
  },
  iconDisabled: {
    opacity: 0.4,
  },
  textInput: {
    flex: 1,
    minHeight: 40,
    maxHeight: 120,
    backgroundColor: SURFACE_COLOR,
    borderRadius: 20,
    paddingHorizontal: 14,
    paddingTop: Platform.OS === 'ios' ? 10 : 8,
    paddingBottom: Platform.OS === 'ios' ? 10 : 8,
    fontSize: 15,
    color: '#212121',
    textAlignVertical: 'center',
  },
  sendButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: PRIMARY_GREEN,
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
    marginBottom: 2,
  },
  sendButtonDisabled: {
    backgroundColor: '#BDBDBD',
  },
});
