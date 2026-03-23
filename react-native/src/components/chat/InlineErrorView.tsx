/**
 * InlineErrorView.tsx
 * Error banner displayed inline in the chat thread.
 */

import React, { memo } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import { ERROR_COLOR, TEXT_SECONDARY } from '../../config/constants';

interface InlineErrorViewProps {
  message: string;
  onRetry?: () => void;
  onDismiss?: () => void;
}

export const InlineErrorView = memo(function InlineErrorView({
  message,
  onRetry,
  onDismiss,
}: InlineErrorViewProps): React.JSX.Element {
  return (
    <View style={styles.container}>
      <View style={styles.errorBadge}>
        <Text style={styles.errorIcon}>!</Text>
      </View>
      <Text style={styles.message} numberOfLines={3}>
        {message}
      </Text>
      <View style={styles.actions}>
        {onRetry && (
          <TouchableOpacity
            onPress={onRetry}
            style={[styles.button, styles.retryButton]}
            accessibilityRole="button"
            accessibilityLabel="Retry request"
          >
            <Text style={styles.retryText}>Retry</Text>
          </TouchableOpacity>
        )}
        {onDismiss && (
          <TouchableOpacity
            onPress={onDismiss}
            style={[styles.button]}
            accessibilityRole="button"
            accessibilityLabel="Dismiss error"
          >
            <Text style={styles.dismissText}>Dismiss</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
});

const styles = StyleSheet.create({
  container: {
    marginHorizontal: 16,
    marginVertical: 8,
    backgroundColor: '#FFEBEE',
    borderRadius: 12,
    borderLeftWidth: 4,
    borderLeftColor: ERROR_COLOR,
    padding: 12,
    flexDirection: 'column',
  },
  errorBadge: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: ERROR_COLOR,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 6,
  },
  errorIcon: {
    color: '#FFFFFF',
    fontWeight: '800',
    fontSize: 12,
  },
  message: {
    fontSize: 13,
    color: '#B71C1C',
    lineHeight: 18,
    flex: 1,
  },
  actions: {
    flexDirection: 'row',
    marginTop: 8,
    gap: 8,
  },
  button: {
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 8,
  },
  retryButton: {
    backgroundColor: ERROR_COLOR,
  },
  retryText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 12,
  },
  dismissText: {
    color: TEXT_SECONDARY,
    fontSize: 12,
  },
});
