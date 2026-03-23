/**
 * VoiceInputOverlay.tsx
 * Full-screen recording overlay with waveform, timer, cancel and confirm.
 */

import React, {
  memo,
  useState,
  useEffect,
  useRef,
  useCallback,
} from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Animated,
  SafeAreaView,
  ActivityIndicator,
} from 'react-native';
import { WaveformView } from './WaveformView';
import { useWaveformSampler } from '../../audio/useWaveformSampler';
import { AudioRecorderService } from '../../audio/AudioRecorderService';
import {
  PRIMARY_GREEN,
  ERROR_COLOR,
  WHITE,
  TEXT_SECONDARY,
} from '../../config/constants';

type RecordingState = 'idle' | 'recording' | 'processing';

interface VoiceInputOverlayProps {
  visible: boolean;
  onCancel: () => void;
  /** Called with {audioUri, base64} when recording confirmed */
  onConfirm: (result: { audioUri: string; base64: string }) => void;
}

export const VoiceInputOverlay = memo(function VoiceInputOverlay({
  visible,
  onCancel,
  onConfirm,
}: VoiceInputOverlayProps): React.JSX.Element | null {
  const [recordingState, setRecordingState] = useState<RecordingState>('idle');
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const recorderRef = useRef(new AudioRecorderService());
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const fadeAnim = useRef(new Animated.Value(0)).current;

  const amplitudes = useWaveformSampler(
    recordingState === 'recording',
    recorderRef.current,
  );

  // Fade in/out
  useEffect(() => {
    Animated.timing(fadeAnim, {
      toValue: visible ? 1 : 0,
      duration: 200,
      useNativeDriver: true,
    }).start();
  }, [visible, fadeAnim]);

  // Auto-start recording when overlay appears
  useEffect(() => {
    if (visible && recordingState === 'idle') {
      startRecording().catch((e) => {
        const err = e as Error;
        setErrorMessage(err.message);
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visible]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      clearTimer();
      recorderRef.current.cancelRecording().catch(() => {/* ignore */});
    };
  }, []);

  const startRecording = useCallback(async () => {
    setErrorMessage(null);
    setElapsedSeconds(0);
    await recorderRef.current.startRecording();
    setRecordingState('recording');

    timerRef.current = setInterval(() => {
      setElapsedSeconds((prev) => prev + 1);
    }, 1000);
  }, []);

  const clearTimer = () => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  };

  const handleCancel = useCallback(async () => {
    clearTimer();
    setRecordingState('idle');
    setElapsedSeconds(0);
    await recorderRef.current.cancelRecording();
    onCancel();
  }, [onCancel]);

  const handleConfirm = useCallback(async () => {
    if (recordingState !== 'recording') return;
    clearTimer();
    setRecordingState('processing');

    try {
      const uri = await recorderRef.current.stopRecording();
      const base64 = await recorderRef.current.getBase64(uri);
      setRecordingState('idle');
      setElapsedSeconds(0);
      onConfirm({ audioUri: uri, base64 });
    } catch (err) {
      const error = err as Error;
      setErrorMessage(error.message);
      setRecordingState('idle');
    }
  }, [recordingState, onConfirm]);

  const formatTime = (seconds: number): string => {
    const m = Math.floor(seconds / 60).toString().padStart(2, '0');
    const s = (seconds % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  };

  if (!visible) return null;

  return (
    <Animated.View style={[StyleSheet.absoluteFill, styles.container, { opacity: fadeAnim }]}>
      <SafeAreaView style={styles.safeArea}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.headerTitle}>
            {recordingState === 'processing' ? 'Processing...' : 'Voice Input'}
          </Text>
          <Text style={styles.headerSubtitle}>
            {recordingState === 'recording'
              ? 'Speak your question clearly'
              : 'Preparing microphone...'}
          </Text>
        </View>

        {/* Waveform + Timer */}
        <View style={styles.waveformSection}>
          {recordingState === 'processing' ? (
            <ActivityIndicator size="large" color={PRIMARY_GREEN} />
          ) : (
            <>
              <WaveformView
                amplitudes={amplitudes}
                isActive={recordingState === 'recording'}
                color={PRIMARY_GREEN}
                maxBarHeight={60}
              />
              <Text style={styles.timer}>{formatTime(elapsedSeconds)}</Text>
            </>
          )}
        </View>

        {/* Error message */}
        {errorMessage && (
          <Text style={styles.errorText}>{errorMessage}</Text>
        )}

        {/* Controls */}
        <View style={styles.controls}>
          {/* Cancel */}
          <TouchableOpacity
            style={[styles.controlButton, styles.cancelButton]}
            onPress={() => {
              handleCancel().catch(() => {/* handled */});
            }}
            disabled={recordingState === 'processing'}
            accessibilityRole="button"
            accessibilityLabel="Cancel recording"
          >
            <Text style={styles.cancelIcon}>✕</Text>
          </TouchableOpacity>

          {/* Recording indicator dot */}
          <RecordingPulse isRecording={recordingState === 'recording'} />

          {/* Confirm */}
          <TouchableOpacity
            style={[
              styles.controlButton,
              styles.confirmButton,
              recordingState !== 'recording' && styles.confirmButtonDisabled,
            ]}
            onPress={() => {
              handleConfirm().catch(() => {/* handled */});
            }}
            disabled={recordingState !== 'recording'}
            accessibilityRole="button"
            accessibilityLabel="Confirm and send voice message"
          >
            <Text style={styles.confirmIcon}>✓</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    </Animated.View>
  );
});

// ─── Recording pulse dot ──────────────────────────────────────────────────────

function RecordingPulse({ isRecording }: { isRecording: boolean }) {
  const pulseAnim = useRef(new Animated.Value(1)).current;

  useEffect(() => {
    if (!isRecording) {
      pulseAnim.setValue(1);
      return;
    }

    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(pulseAnim, {
          toValue: 1.4,
          duration: 600,
          useNativeDriver: true,
        }),
        Animated.timing(pulseAnim, {
          toValue: 1,
          duration: 600,
          useNativeDriver: true,
        }),
      ]),
    );
    loop.start();
    return () => loop.stop();
  }, [isRecording, pulseAnim]);

  return (
    <Animated.View
      style={[
        pulseStyles.dot,
        {
          backgroundColor: isRecording ? ERROR_COLOR : TEXT_SECONDARY,
          transform: [{ scale: pulseAnim }],
        },
      ]}
    />
  );
}

const pulseStyles = StyleSheet.create({
  dot: {
    width: 16,
    height: 16,
    borderRadius: 8,
  },
});

const styles = StyleSheet.create({
  container: {
    backgroundColor: 'rgba(255,255,255,0.97)',
    zIndex: 9999,
  },
  safeArea: {
    flex: 1,
    justifyContent: 'space-between',
    paddingHorizontal: 24,
    paddingVertical: 20,
  },
  header: {
    alignItems: 'center',
    paddingTop: 16,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#212121',
    marginBottom: 6,
  },
  headerSubtitle: {
    fontSize: 14,
    color: TEXT_SECONDARY,
  },
  waveformSection: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 20,
  },
  timer: {
    fontSize: 36,
    fontWeight: '300',
    color: '#212121',
    fontVariant: ['tabular-nums'],
    letterSpacing: 2,
  },
  errorText: {
    color: ERROR_COLOR,
    textAlign: 'center',
    fontSize: 13,
    marginBottom: 8,
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 24,
    paddingBottom: 24,
  },
  controlButton: {
    width: 64,
    height: 64,
    borderRadius: 32,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 3,
  },
  cancelButton: {
    backgroundColor: ERROR_COLOR,
  },
  confirmButton: {
    backgroundColor: PRIMARY_GREEN,
  },
  confirmButtonDisabled: {
    backgroundColor: '#BDBDBD',
  },
  cancelIcon: {
    color: WHITE,
    fontSize: 22,
    fontWeight: '700',
  },
  confirmIcon: {
    color: WHITE,
    fontSize: 26,
    fontWeight: '700',
  },
});
