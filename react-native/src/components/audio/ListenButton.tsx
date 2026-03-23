/**
 * ListenButton.tsx
 * TTS playback control button with state-based visual feedback.
 */

import React, { memo } from 'react';
import {
  TouchableOpacity,
  View,
  Text,
  ActivityIndicator,
  StyleSheet,
} from 'react-native';
import { SoundWaveAnimation } from './SoundWaveAnimation';
import {
  PRIMARY_GREEN,
  TEXT_SECONDARY,
} from '../../config/constants';

type ListenButtonState = 'default' | 'loading' | 'playing' | 'paused';

interface ListenButtonProps {
  buttonState: ListenButtonState;
  onPress: () => void;
  color?: string;
  disabled?: boolean;
}

export const ListenButton = memo(function ListenButton({
  buttonState,
  onPress,
  color = PRIMARY_GREEN,
  disabled = false,
}: ListenButtonProps): React.JSX.Element {
  const renderContent = () => {
    switch (buttonState) {
      case 'loading':
        return (
          <>
            <ActivityIndicator size="small" color={color} style={styles.icon} />
            <Text style={[styles.label, { color }]}>Loading...</Text>
          </>
        );

      case 'playing':
        return (
          <>
            <SoundWaveAnimation color={color} isActive={true} />
            <Text style={[styles.label, { color }]}>Playing</Text>
          </>
        );

      case 'paused':
        return (
          <>
            <PauseIcon color={color} />
            <Text style={[styles.label, { color }]}>Paused</Text>
          </>
        );

      default:
        return (
          <>
            <SpeakerIcon color={color} />
            <Text style={[styles.label, { color }]}>Listen</Text>
          </>
        );
    }
  };

  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled || buttonState === 'loading'}
      style={[styles.container, disabled && styles.disabled]}
      accessibilityRole="button"
      accessibilityLabel={
        buttonState === 'playing'
          ? 'Pause audio'
          : buttonState === 'paused'
          ? 'Resume audio'
          : 'Listen to response'
      }
      hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
    >
      <View style={styles.row}>{renderContent()}</View>
    </TouchableOpacity>
  );
});

// ─── Inline SVG-like icons using View primitives ──────────────────────────────

function SpeakerIcon({ color }: { color: string }) {
  return (
    <View style={[styles.iconContainer]}>
      <View style={[styles.speakerBody, { borderColor: color }]} />
      <View style={[styles.speakerWave1, { borderColor: color }]} />
      <View style={[styles.speakerWave2, { borderColor: color }]} />
    </View>
  );
}

function PauseIcon({ color }: { color: string }) {
  return (
    <View style={styles.iconContainer}>
      <View style={[styles.pauseBar, { backgroundColor: color }]} />
      <View style={[styles.pauseBar, { backgroundColor: color, marginLeft: 4 }]} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingVertical: 4,
    paddingHorizontal: 8,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: PRIMARY_GREEN,
    alignSelf: 'flex-start',
  },
  disabled: {
    opacity: 0.4,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  icon: {
    width: 20,
    height: 20,
  },
  label: {
    fontSize: 12,
    fontWeight: '500',
  },
  iconContainer: {
    width: 20,
    height: 20,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  speakerBody: {
    width: 8,
    height: 10,
    borderWidth: 1.5,
    borderRadius: 1,
  },
  speakerWave1: {
    width: 6,
    height: 8,
    borderWidth: 1.5,
    borderRadius: 4,
    borderLeftWidth: 0,
    marginLeft: 1,
  },
  speakerWave2: {
    width: 9,
    height: 13,
    borderWidth: 1.5,
    borderRadius: 6,
    borderLeftWidth: 0,
    marginLeft: -3,
    opacity: 0.5,
  },
  pauseBar: {
    width: 3,
    height: 12,
    borderRadius: 1.5,
  },
  loadingText: {
    fontSize: 12,
    color: TEXT_SECONDARY,
  },
});
