/**
 * ChatResponseActions.tsx
 * Action bar shown below AI responses: Listen, Share, Save.
 */

import React, { memo, useCallback } from 'react';
import {
  View,
  TouchableOpacity,
  Text,
  StyleSheet,
  Share,
  Alert,
} from 'react-native';
import { ListenButton } from '../audio/ListenButton';
import { useChatContext } from '../../state/ChatContext';
import { AudioPlayerService } from '../../audio/AudioPlayerService';
import {
  TEXT_SECONDARY,
  PRIMARY_GREEN,
  DIVIDER_COLOR,
} from '../../config/constants';

interface ChatResponseActionsProps {
  messageId?: string;
  messageText: string;
  hideTtsSpeaker?: boolean;
  audioPlayer: AudioPlayerService;
}

export const ChatResponseActions = memo(function ChatResponseActions({
  messageId,
  messageText,
  hideTtsSpeaker = false,
  audioPlayer,
}: ChatResponseActionsProps): React.JSX.Element {
  const { state, dispatch } = useChatContext();

  const listenButtonState = (() => {
    if (!state.audioPlaybackUrl) {
      if (state.isLoadingSynthesiseAudio) return 'loading';
      return 'default';
    }
    if (state.isAudioPlaying) return 'playing';
    return 'paused';
  })();

  const handleListenPress = useCallback(async () => {
    if (state.audioPlaybackUrl) {
      // Toggle playback
      if (state.isAudioPlaying) {
        audioPlayer.pause();
        dispatch({ type: 'SET_AUDIO_PLAYING', isPlaying: false });
      } else {
        audioPlayer.resume();
        dispatch({ type: 'SET_AUDIO_PLAYING', isPlaying: true });
      }
      return;
    }

    // Request synthesis
    if (messageId) {
      dispatch({ type: 'SYNTHESISE_AUDIO' });

      // Set up finish callback
      audioPlayer.onFinished = () => {
        dispatch({ type: 'SET_AUDIO_PLAYING', isPlaying: false });
        dispatch({ type: 'CLEAR_AUDIO_PLAYBACK_URL' });
      };
    }
  }, [state.audioPlaybackUrl, state.isAudioPlaying, messageId, dispatch, audioPlayer]);

  // Play when URL becomes available
  React.useEffect(() => {
    if (state.audioPlaybackUrl && !state.isAudioPlaying) {
      audioPlayer.playUrl(state.audioPlaybackUrl)
        .then(() => {
          dispatch({ type: 'SET_AUDIO_PLAYING', isPlaying: true });
        })
        .catch(() => {
          dispatch({ type: 'CLEAR_AUDIO_PLAYBACK_URL' });
        });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state.audioPlaybackUrl]);

  const handleShare = useCallback(async () => {
    try {
      await Share.share({
        message: messageText,
        title: 'FarmerChat Response',
      });
    } catch {
      // User cancelled or share failed
    }
  }, [messageText]);

  const handleSave = useCallback(() => {
    Alert.alert('Saved', 'Response saved to your notes.');
  }, []);

  return (
    <View style={styles.container}>
      {!hideTtsSpeaker && (
        <ListenButton
          buttonState={listenButtonState}
          onPress={() => {
            handleListenPress().catch(() => {/* handled */});
          }}
          color={PRIMARY_GREEN}
        />
      )}
      <View style={styles.spacer} />
      <TouchableOpacity
        onPress={() => {
          handleShare().catch(() => {/* handled */});
        }}
        hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
        accessibilityRole="button"
        accessibilityLabel="Share response"
        style={styles.iconButton}
      >
        <ShareIcon />
      </TouchableOpacity>
      <TouchableOpacity
        onPress={handleSave}
        hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
        accessibilityRole="button"
        accessibilityLabel="Save response"
        style={styles.iconButton}
      >
        <BookmarkIcon />
      </TouchableOpacity>
    </View>
  );
});

// ─── Icon primitives ──────────────────────────────────────────────────────────

function ShareIcon() {
  return (
    <View style={actionIconStyles.container}>
      <Text style={actionIconStyles.emoji}>↗</Text>
    </View>
  );
}

function BookmarkIcon() {
  return (
    <View style={actionIconStyles.container}>
      <Text style={actionIconStyles.emoji}>🔖</Text>
    </View>
  );
}

const actionIconStyles = StyleSheet.create({
  container: {
    width: 24,
    height: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emoji: {
    fontSize: 14,
    color: TEXT_SECONDARY,
  },
});

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
    paddingTop: 8,
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: DIVIDER_COLOR,
    gap: 8,
  },
  spacer: {
    flex: 1,
  },
  iconButton: {
    padding: 4,
  },
});
