/**
 * AIMessageBubble.tsx
 * Left-aligned bubble for AI responses with markdown rendering and action bar.
 */

import React, { memo, useMemo } from 'react';
import {
  View,
  StyleSheet,
  Image,
} from 'react-native';
import { AIResponseMessage } from '../../models/chatModels';
import { MarkdownMessage } from '../markdown/MarkdownMessage';
import { ChatResponseActions } from './ChatResponseActions';
import { AudioPlayerService } from '../../audio/AudioPlayerService';
import { BUBBLE_AI_BACKGROUND, DIVIDER_COLOR } from '../../config/constants';

interface AIMessageBubbleProps {
  message: AIResponseMessage;
  audioPlayer: AudioPlayerService;
  isLatest: boolean;
}

export const AIMessageBubble = memo(function AIMessageBubble({
  message,
  audioPlayer,
  isLatest,
}: AIMessageBubbleProps): React.JSX.Element {
  // Memoize content to avoid unnecessary re-renders
  const content = useMemo(
    () => (
      <MarkdownMessage content={message.text} fontSize={15} />
    ),
    [message.text],
  );

  return (
    <View style={styles.wrapper}>
      {/* Avatar / Logo */}
      <View style={styles.avatarContainer}>
        {message.contentProviderLogo ? (
          <Image
            source={{ uri: message.contentProviderLogo }}
            style={styles.logo}
            resizeMode="contain"
          />
        ) : (
          <View style={styles.defaultAvatar}>
            <View style={styles.avatarLeaf} />
          </View>
        )}
      </View>

      <View style={styles.contentColumn}>
        <View style={styles.bubble}>
          {content}
        </View>

        {/* Action bar shown only for real AI responses */}
        {!message.isPreGenerated && isLatest && (
          <ChatResponseActions
            messageId={message.messageId}
            messageText={message.text}
            hideTtsSpeaker={message.hideTtsSpeaker}
            audioPlayer={audioPlayer}
          />
        )}
      </View>
    </View>
  );
});

const styles = StyleSheet.create({
  wrapper: {
    flexDirection: 'row',
    alignSelf: 'flex-start',
    maxWidth: '92%',
    paddingHorizontal: 12,
    paddingVertical: 4,
    marginBottom: 4,
  },
  avatarContainer: {
    width: 32,
    height: 32,
    marginRight: 8,
    marginTop: 4,
    flexShrink: 0,
  },
  defaultAvatar: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#C8E6C9',
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  avatarLeaf: {
    width: 16,
    height: 20,
    backgroundColor: '#2E7D32',
    borderRadius: 8,
    transform: [{ rotate: '30deg' }],
  },
  logo: {
    width: 32,
    height: 32,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: DIVIDER_COLOR,
  },
  contentColumn: {
    flex: 1,
  },
  bubble: {
    backgroundColor: BUBBLE_AI_BACKGROUND,
    borderRadius: 18,
    borderTopLeftRadius: 4,
    paddingHorizontal: 14,
    paddingVertical: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.06,
    shadowRadius: 2,
    elevation: 1,
  },
});
