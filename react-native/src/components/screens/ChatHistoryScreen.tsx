/**
 * ChatHistoryScreen.tsx
 * Shows paginated conversation history grouped by date.
 */

import React, { memo, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  RefreshControl,
  ListRenderItem,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useHistoryViewModel } from '../../state/useHistoryViewModel';
import { groupConversationsByDate, formatDate, formatTime } from '../../utils/dateUtils';
import { ConversationListItem } from '../../models/responses';
import {
  PRIMARY_GREEN,
  TEXT_PRIMARY,
  TEXT_SECONDARY,
  WHITE,
  SURFACE_COLOR,
  DIVIDER_COLOR,
} from '../../config/constants';

interface ChatHistoryScreenProps {
  onBack: () => void;
  onSelectConversation: (conversationId: string) => void;
}

export const ChatHistoryScreen = memo(function ChatHistoryScreen({
  onBack,
  onSelectConversation,
}: ChatHistoryScreenProps): React.JSX.Element {
  const vm = useHistoryViewModel();

  useEffect(() => {
    vm.loadInitial();   // uses isLoading, not isRefreshing — no auto pull-to-refresh flash
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleEndReached = useCallback(() => {
    if (vm.hasMore && !vm.isLoading) {
      vm.loadNextPage();
    }
  }, [vm]);

  const grouped = groupConversationsByDate(vm.items);

  // Flatten groups into a list of section headers and items
  type ListItem =
    | { _kind: 'header'; label: string }
    | { _kind: 'item'; data: ConversationListItem };

  const flatData: ListItem[] = grouped.flatMap(({ grouping, items }) => [
    { _kind: 'header' as const, label: grouping },
    ...items.map((item) => ({ _kind: 'item' as const, data: item })),
  ]);

  const renderItem: ListRenderItem<ListItem> = useCallback(
    ({ item }) => {
      if (item._kind === 'header') {
        return (
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionHeaderText}>{item.label}</Text>
          </View>
        );
      }

      const conv = item.data;
      return (
        <TouchableOpacity
          style={styles.conversationItem}
          onPress={() => onSelectConversation(conv.conversation_id)}
          activeOpacity={0.7}
          accessibilityRole="button"
          accessibilityLabel={`Open conversation: ${conv.conversation_title ?? 'Untitled'}`}
        >
          <View style={styles.itemIcon}>
            <Text style={styles.itemIconText}>
              {getMessageTypeIcon(conv.message_type)}
            </Text>
          </View>
          <View style={styles.itemContent}>
            <Text style={styles.itemTitle} numberOfLines={2}>
              {conv.conversation_title ?? 'New Conversation'}
            </Text>
            <Text style={styles.itemDate}>
              {formatDate(conv.created_on)} · {formatTime(conv.created_on)}
            </Text>
          </View>
          <Text style={styles.chevron}>›</Text>
        </TouchableOpacity>
      );
    },
    [onSelectConversation],
  );

  const keyExtractor = useCallback((item: ListItem, index: number) => {
    if (item._kind === 'header') return `header_${item.label}_${index}`;
    return item.data.conversation_id;
  }, []);

  return (
    <SafeAreaView style={styles.screen} edges={['bottom', 'left', 'right']}>
      {/* Top app bar */}
      <View style={styles.appBar}>
        <TouchableOpacity
          onPress={onBack}
          style={styles.backButton}
          hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
          accessibilityRole="button"
          accessibilityLabel="Go back"
        >
          <Text style={styles.backIcon}>‹</Text>
        </TouchableOpacity>
        <Text style={styles.appBarTitle}>Chat History</Text>
        <View style={styles.appBarRight} />
      </View>

      {/* Content */}
      {vm.isLoading && vm.items.length === 0 ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={PRIMARY_GREEN} />
          <Text style={styles.loadingText}>Loading conversations...</Text>
        </View>
      ) : vm.error && vm.items.length === 0 ? (
        <View style={styles.errorContainer}>
          <Text style={styles.errorEmoji}>⚠️</Text>
          <Text style={styles.errorText}>{vm.error}</Text>
          <TouchableOpacity
            style={styles.retryButton}
            onPress={vm.refresh}
          >
            <Text style={styles.retryButtonText}>Try Again</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={flatData}
          renderItem={renderItem}
          keyExtractor={keyExtractor}
          style={styles.list}
          contentContainerStyle={[
            styles.listContent,
            flatData.length === 0 && styles.emptyContent,
          ]}
          refreshControl={
            <RefreshControl
              refreshing={vm.isRefreshing}
              onRefresh={vm.refresh}
              colors={[PRIMARY_GREEN]}
              tintColor={PRIMARY_GREEN}
            />
          }
          onEndReached={handleEndReached}
          onEndReachedThreshold={0.3}
          ListEmptyComponent={
            <View style={styles.emptyContainer}>
              <Text style={styles.emptyEmoji}>💬</Text>
              <Text style={styles.emptyTitle}>No conversations yet</Text>
              <Text style={styles.emptySubtitle}>
                Start chatting and your history will appear here.
              </Text>
            </View>
          }
          ListFooterComponent={
            vm.isLoading && vm.items.length > 0 ? (
              <ActivityIndicator
                size="small"
                color={PRIMARY_GREEN}
                style={styles.footerLoader}
              />
            ) : null
          }
          showsVerticalScrollIndicator={false}
        />
      )}
    </SafeAreaView>
  );
});

function getMessageTypeIcon(messageType?: string): string {
  switch (messageType?.toLowerCase()) {
    case 'image':
      return '🖼️';
    case 'voice':
    case 'audio':
      return '🎤';
    default:
      return '💬';
  }
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: SURFACE_COLOR,
  },
  appBar: {
    height: 56,
    backgroundColor: WHITE,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: DIVIDER_COLOR,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 2,
  },
  backButton: {
    width: 40,
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
  },
  backIcon: {
    fontSize: 30,
    color: PRIMARY_GREEN,
    lineHeight: 34,
  },
  appBarTitle: {
    flex: 1,
    fontSize: 18,
    fontWeight: '700',
    color: TEXT_PRIMARY,
    textAlign: 'center',
  },
  appBarRight: {
    width: 40,
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
  },
  loadingText: {
    color: TEXT_SECONDARY,
    fontSize: 14,
  },
  errorContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 32,
    gap: 12,
  },
  errorEmoji: {
    fontSize: 40,
  },
  errorText: {
    fontSize: 14,
    color: TEXT_SECONDARY,
    textAlign: 'center',
    lineHeight: 20,
  },
  retryButton: {
    paddingVertical: 10,
    paddingHorizontal: 24,
    backgroundColor: PRIMARY_GREEN,
    borderRadius: 20,
    marginTop: 8,
  },
  retryButtonText: {
    color: WHITE,
    fontWeight: '600',
    fontSize: 14,
  },
  list: {
    flex: 1,
  },
  listContent: {
    paddingBottom: 20,
  },
  emptyContent: {
    flex: 1,
    justifyContent: 'center',
  },
  emptyContainer: {
    alignItems: 'center',
    paddingHorizontal: 40,
    paddingVertical: 60,
  },
  emptyEmoji: {
    fontSize: 48,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: TEXT_PRIMARY,
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 14,
    color: TEXT_SECONDARY,
    textAlign: 'center',
    lineHeight: 20,
  },
  sectionHeader: {
    backgroundColor: SURFACE_COLOR,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  sectionHeaderText: {
    fontSize: 12,
    fontWeight: '600',
    color: TEXT_SECONDARY,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  conversationItem: {
    backgroundColor: WHITE,
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    paddingHorizontal: 16,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: DIVIDER_COLOR,
  },
  itemIcon: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#E8F5E9',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  itemIconText: {
    fontSize: 18,
  },
  itemContent: {
    flex: 1,
  },
  itemTitle: {
    fontSize: 14,
    fontWeight: '500',
    color: TEXT_PRIMARY,
    marginBottom: 3,
    lineHeight: 20,
  },
  itemDate: {
    fontSize: 12,
    color: TEXT_SECONDARY,
  },
  chevron: {
    fontSize: 22,
    color: TEXT_SECONDARY,
    marginLeft: 8,
  },
  footerLoader: {
    paddingVertical: 16,
  },
});
