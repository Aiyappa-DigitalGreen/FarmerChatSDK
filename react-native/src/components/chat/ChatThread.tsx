/**
 * ChatThread.tsx
 * The main message list. Renders user/AI bubbles, loading indicator, and errors.
 */

import React, { memo, useRef, useEffect, useCallback, useMemo } from 'react';
import {
  FlatList,
  View,
  Text,
  StyleSheet,
  ListRenderItem,
  NativeSyntheticEvent,
  NativeScrollEvent,
  RefreshControl,
} from 'react-native';
import { ChatMessage } from '../../models/chatModels';
import { UserMessageBubble } from './UserMessageBubble';
import { AIMessageBubble } from './AIMessageBubble';
import { LoadingBubble } from './LoadingBubble';
import { InlineErrorView } from './InlineErrorView';
import { useChatContext } from '../../state/ChatContext';
import { AudioPlayerService } from '../../audio/AudioPlayerService';
import {
  TEXT_SECONDARY,
  SURFACE_COLOR,
} from '../../config/constants';

interface ChatThreadProps {
  /** Called when user scrolls near top (for history pagination) */
  onNearTop?: () => void;
  audioPlayer: AudioPlayerService;
}

export const ChatThread = memo(function ChatThread({
  onNearTop,
  audioPlayer,
}: ChatThreadProps): React.JSX.Element {
  const { state, dispatch } = useChatContext();
  const flatListRef = useRef<FlatList<ChatMessage | ErrorItem>>(null);
  const prevMessageCountRef = useRef(0);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    const count = state.messages.length;
    if (count > prevMessageCountRef.current && count > 0) {
      prevMessageCountRef.current = count;
      setTimeout(() => {
        flatListRef.current?.scrollToEnd({ animated: true });
      }, 100);
    }
  }, [state.messages.length]);

  const handleScroll = useCallback(
    (event: NativeSyntheticEvent<NativeScrollEvent>) => {
      const { contentOffset } = event.nativeEvent;
      if (contentOffset.y < 80) {
        onNearTop?.();
      }
    },
    [onNearTop],
  );

  const handleRetry = useCallback(() => {
    dispatch({ type: 'RETRY_LAST_REQUEST' });
  }, [dispatch]);

  const handleDismissError = useCallback(() => {
    dispatch({ type: 'CLEAR_ERROR' });
  }, [dispatch]);

  // Combine messages with optional inline error item
  const dataWithError = useMemo<Array<ChatMessage | ErrorItem>>(() => {
    if (state.errorMessage) {
      return [
        ...state.messages,
        { _type: 'error', message: state.errorMessage } as ErrorItem,
      ];
    }
    return state.messages;
  }, [state.messages, state.errorMessage]);

  const renderItem: ListRenderItem<ChatMessage | ErrorItem> = useCallback(
    ({ item, index }) => {
      if (isErrorItem(item)) {
        return (
          <InlineErrorView
            message={item.message}
            onRetry={handleRetry}
            onDismiss={handleDismissError}
          />
        );
      }

      const isLatestAiMessage =
        item.type === 'aiResponse' &&
        // Find the last AI response index
        [...state.messages]
          .reverse()
          .findIndex((m) => m.type === 'aiResponse') === state.messages.length - 1 - index;

      switch (item.type) {
        case 'userMessage':
          return <UserMessageBubble message={item} />;
        case 'aiResponse':
          return (
            <AIMessageBubble
              message={item}
              audioPlayer={audioPlayer}
              isLatest={isLatestAiMessage}
            />
          );
        case 'loadingPlaceholder':
          return <LoadingBubble />;
        default:
          return null;
      }
    },
    [state.messages, handleRetry, handleDismissError, audioPlayer],
  );

  const keyExtractor = useCallback(
    (item: ChatMessage | ErrorItem) =>
      isErrorItem(item) ? 'error_item' : item.id,
    [],
  );

  const ListEmptyComponent = useMemo(
    () => (
      <View style={styles.emptyContainer}>
        <Text style={styles.emptyEmoji}>🌱</Text>
        <Text style={styles.emptyTitle}>How can FarmerChat help?</Text>
        <Text style={styles.emptySubtitle}>
          Ask about crops, weather, pests, fertilizers, and more.
        </Text>
      </View>
    ),
    [],
  );

  return (
    <FlatList
      ref={flatListRef}
      data={dataWithError}
      renderItem={renderItem}
      keyExtractor={keyExtractor}
      style={styles.list}
      contentContainerStyle={[
        styles.content,
        dataWithError.length === 0 && styles.emptyContent,
      ]}
      onScroll={handleScroll}
      scrollEventThrottle={200}
      ListEmptyComponent={ListEmptyComponent}
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
      refreshControl={
        state.isLoading && state.messages.length === 0 ? (
          <RefreshControl refreshing={true} colors={['#2E7D32']} />
        ) : undefined
      }
      // Performance
      removeClippedSubviews
      maxToRenderPerBatch={10}
      windowSize={10}
      initialNumToRender={15}
    />
  );
});

// ─── Inline error item type ───────────────────────────────────────────────────

interface ErrorItem {
  _type: 'error';
  message: string;
}

function isErrorItem(item: ChatMessage | ErrorItem): item is ErrorItem {
  return '_type' in item && item._type === 'error';
}

const styles = StyleSheet.create({
  list: {
    flex: 1,
    backgroundColor: SURFACE_COLOR,
  },
  content: {
    paddingTop: 12,
    paddingBottom: 8,
  },
  emptyContent: {
    flex: 1,
    justifyContent: 'center',
  },
  emptyContainer: {
    alignItems: 'center',
    paddingHorizontal: 40,
    paddingVertical: 32,
  },
  emptyEmoji: {
    fontSize: 48,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333333',
    textAlign: 'center',
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 14,
    color: TEXT_SECONDARY,
    textAlign: 'center',
    lineHeight: 20,
  },
});
