/**
 * useHistoryViewModel.ts
 * Manages paginated conversation list fetching.
 */

import { useState, useCallback, useRef } from 'react';
import { ConversationListItem } from '../models/responses';
import { ChatApiClient } from '../network/ChatApiClient';
import { FarmerChatSDK } from '../config/SDKConfig';
import { NetworkError } from '../network/NetworkError';

export interface HistoryViewModelState {
  items: ConversationListItem[];
  isLoading: boolean;
  isRefreshing: boolean;
  error?: string;
  hasMore: boolean;
}

export interface HistoryViewModel extends HistoryViewModelState {
  loadNextPage: () => void;
  refresh: () => void;
  loadInitial: () => void;
}

export function useHistoryViewModel(): HistoryViewModel {
  const [items, setItems] = useState<ConversationListItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState<string | undefined>(undefined);
  const [hasMore, setHasMore] = useState(true);

  const currentPageRef = useRef(1);
  const isFetchingRef = useRef(false);

  const getClient = useCallback((): ChatApiClient => {
    const config = FarmerChatSDK.getConfig();
    return new ChatApiClient(config);
  }, []);

  const fetchPage = useCallback(
    async (page: number, isRefresh: boolean) => {
      if (isFetchingRef.current) return;
      isFetchingRef.current = true;

      if (isRefresh) {
        setIsRefreshing(true);
      } else {
        setIsLoading(true);
      }
      setError(undefined);

      try {
        const client = getClient();
        // API returns a plain array — no wrapper, no pagination fields
        const newItems = await client.getConversationList(page);

        setItems((prev) => (isRefresh ? newItems : [...prev, ...newItems]));
        currentPageRef.current = page;

        // Empty page signals end of results
        setHasMore(newItems.length > 0);
      } catch (err) {
        const message =
          err instanceof NetworkError
            ? err.message
            : 'Failed to load conversations.';
        setError(message);
      } finally {
        setIsLoading(false);
        setIsRefreshing(false);
        isFetchingRef.current = false;
      }
    },
    [getClient],
  );

  const loadNextPage = useCallback(() => {
    if (!hasMore || isFetchingRef.current) return;
    const nextPage = currentPageRef.current + 1;
    fetchPage(nextPage, false).catch(() => {/* handled internally */});
  }, [hasMore, fetchPage]);

  const refresh = useCallback(() => {
    currentPageRef.current = 1;
    setHasMore(true);
    fetchPage(1, true).catch(() => {/* handled internally */});
  }, [fetchPage]);

  /** Initial load — uses isLoading (not isRefreshing), so the pull-to-refresh
   *  indicator does NOT appear automatically on mount. */
  const loadInitial = useCallback(() => {
    currentPageRef.current = 1;
    setHasMore(true);
    fetchPage(1, false).catch(() => {/* handled internally */});
  }, [fetchPage]);

  return {
    items,
    isLoading,
    isRefreshing,
    error,
    hasMore,
    loadNextPage,
    refresh,
    loadInitial,
  };
}
