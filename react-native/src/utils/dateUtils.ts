/**
 * dateUtils.ts
 * Date formatting and grouping utilities for conversation history.
 */

import { ConversationListItem } from '../models/responses';

export interface ConversationGroup {
  grouping: string;
  items: ConversationListItem[];
}

/**
 * Groups conversation list items by their `grouping` field (e.g. "Today", "Yesterday", "Last Week").
 * Falls back to a formatted date string if grouping is absent.
 */
export function groupConversationsByDate(
  items: ConversationListItem[],
): ConversationGroup[] {
  const groups: Map<string, ConversationListItem[]> = new Map();

  for (const item of items) {
    const key = item.grouping ?? formatGroupKey(item.created_on);
    const existing = groups.get(key);
    if (existing) {
      existing.push(item);
    } else {
      groups.set(key, [item]);
    }
  }

  return Array.from(groups.entries()).map(([grouping, groupItems]) => ({
    grouping,
    items: groupItems,
  }));
}

/**
 * Formats a date string for display in the conversation list.
 * Returns e.g. "Today", "Yesterday", "Mar 15", "Mar 15, 2024".
 */
export function formatDate(dateString: string): string {
  const date = parseDate(dateString);
  if (!date) {
    return dateString;
  }

  const now = new Date();
  const today = startOfDay(now);
  const yesterday = new Date(today.getTime() - 86400000);
  const itemDay = startOfDay(date);

  if (itemDay.getTime() === today.getTime()) {
    return 'Today';
  }
  if (itemDay.getTime() === yesterday.getTime()) {
    return 'Yesterday';
  }

  const monthNames = [
    'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
  ];

  const month = monthNames[date.getMonth()];
  const day = date.getDate();
  const year = date.getFullYear();

  if (year === now.getFullYear()) {
    return `${month} ${day}`;
  }
  return `${month} ${day}, ${year}`;
}

/**
 * Formats a time string, e.g. "2:34 PM".
 */
export function formatTime(dateString: string): string {
  const date = parseDate(dateString);
  if (!date) {
    return '';
  }
  const hours = date.getHours();
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const ampm = hours >= 12 ? 'PM' : 'AM';
  const displayHours = hours % 12 || 12;
  return `${displayHours}:${minutes} ${ampm}`;
}

// ─── Private helpers ──────────────────────────────────────────────────────────

function parseDate(dateString: string): Date | null {
  if (!dateString) {
    return null;
  }
  const d = new Date(dateString);
  return isNaN(d.getTime()) ? null : d;
}

function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function formatGroupKey(dateString: string): string {
  const date = parseDate(dateString);
  if (!date) {
    return 'Unknown';
  }

  const now = new Date();
  const today = startOfDay(now);
  const itemDay = startOfDay(date);
  const diffDays = Math.floor(
    (today.getTime() - itemDay.getTime()) / 86400000,
  );

  if (diffDays === 0) return 'Today';
  if (diffDays === 1) return 'Yesterday';
  if (diffDays < 7) return 'This Week';
  if (diffDays < 14) return 'Last Week';
  if (diffDays < 30) return 'This Month';
  return formatDate(dateString);
}
