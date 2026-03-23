/**
 * UserMessageBubble.tsx
 * Right-aligned bubble for user messages with optional image/audio indicators.
 */

import React, { memo } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Image,
  TouchableOpacity,
} from 'react-native';
import { UserMessage } from '../../models/chatModels';
import { PRIMARY_GREEN, WHITE } from '../../config/constants';

interface UserMessageBubbleProps {
  message: UserMessage;
  onImagePress?: (uri: string) => void;
}

export const UserMessageBubble = memo(function UserMessageBubble({
  message,
  onImagePress,
}: UserMessageBubbleProps): React.JSX.Element {
  return (
    <View style={styles.wrapper}>
      <View style={styles.bubble}>
        {/* Image thumbnail */}
        {message.imageUri && (
          <TouchableOpacity
            onPress={() => onImagePress?.(message.imageUri!)}
            activeOpacity={0.85}
            accessibilityRole="imagebutton"
            accessibilityLabel="View attached image"
          >
            <Image
              source={{ uri: message.imageUri }}
              style={styles.image}
              resizeMode="cover"
            />
          </TouchableOpacity>
        )}

        {/* Audio indicator */}
        {message.audioUri && !message.imageUri && (
          <View style={styles.audioIndicator}>
            <Text style={styles.audioIcon}>🎤</Text>
            <Text style={styles.audioLabel}>Voice message</Text>
          </View>
        )}

        {/* Text */}
        {message.text ? (
          <Text style={styles.text}>{message.text}</Text>
        ) : null}
      </View>
    </View>
  );
});

const styles = StyleSheet.create({
  wrapper: {
    alignSelf: 'flex-end',
    maxWidth: '80%',
    paddingHorizontal: 16,
    paddingVertical: 4,
    marginBottom: 4,
  },
  bubble: {
    backgroundColor: PRIMARY_GREEN,
    borderRadius: 18,
    borderBottomRightRadius: 4,
    paddingHorizontal: 14,
    paddingVertical: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.12,
    shadowRadius: 3,
    elevation: 2,
  },
  text: {
    color: WHITE,
    fontSize: 15,
    lineHeight: 22,
  },
  image: {
    width: 200,
    height: 150,
    borderRadius: 10,
    marginBottom: 6,
  },
  audioIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 4,
  },
  audioIcon: {
    fontSize: 16,
  },
  audioLabel: {
    color: 'rgba(255,255,255,0.85)',
    fontSize: 13,
    fontStyle: 'italic',
  },
});
