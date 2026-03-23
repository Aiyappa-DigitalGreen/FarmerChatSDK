/**
 * MarkdownMessage.tsx
 * Renders AI response text with Markdown support.
 */

import React, { memo } from 'react';
import { StyleSheet } from 'react-native';
import Markdown from 'react-native-markdown-display';
import {
  TEXT_PRIMARY,
  TEXT_SECONDARY,
  DIVIDER_COLOR,
  PRIMARY_GREEN,
} from '../../config/constants';

interface MarkdownMessageProps {
  content: string;
  fontSize?: number;
}

export const MarkdownMessage = memo(function MarkdownMessage({
  content,
  fontSize = 14,
}: MarkdownMessageProps): React.JSX.Element {
  const styles = buildStyles(fontSize);

  return (
    <Markdown style={styles}>
      {content}
    </Markdown>
  );
});

function buildStyles(fontSize: number) {
  return StyleSheet.create({
    // ─── Block elements ────────────────────────────────────────────────────
    body: {
      fontSize,
      color: TEXT_PRIMARY,
      lineHeight: fontSize * 1.6,
    },
    heading1: {
      fontSize: fontSize + 8,
      fontWeight: '700',
      color: TEXT_PRIMARY,
      marginBottom: 8,
      marginTop: 12,
    },
    heading2: {
      fontSize: fontSize + 5,
      fontWeight: '700',
      color: TEXT_PRIMARY,
      marginBottom: 6,
      marginTop: 10,
    },
    heading3: {
      fontSize: fontSize + 2,
      fontWeight: '600',
      color: TEXT_PRIMARY,
      marginBottom: 4,
      marginTop: 8,
    },
    heading4: {
      fontSize: fontSize + 1,
      fontWeight: '600',
      color: TEXT_PRIMARY,
      marginBottom: 4,
      marginTop: 6,
    },
    heading5: {
      fontSize,
      fontWeight: '600',
      color: TEXT_PRIMARY,
      marginBottom: 2,
      marginTop: 4,
    },
    heading6: {
      fontSize: fontSize - 1,
      fontWeight: '600',
      color: TEXT_SECONDARY,
      marginBottom: 2,
      marginTop: 4,
    },
    paragraph: {
      marginTop: 0,
      marginBottom: 8,
    },
    // ─── Inline elements ───────────────────────────────────────────────────
    strong: {
      fontWeight: '700',
    },
    em: {
      fontStyle: 'italic',
    },
    s: {
      textDecorationLine: 'line-through',
    },
    code_inline: {
      backgroundColor: '#F5F5F5',
      paddingHorizontal: 4,
      paddingVertical: 2,
      borderRadius: 4,
      fontFamily: 'monospace',
      fontSize: fontSize - 1,
      color: '#C62828',
    },
    link: {
      color: PRIMARY_GREEN,
      textDecorationLine: 'underline',
    },
    // ─── Lists ─────────────────────────────────────────────────────────────
    bullet_list: {
      marginBottom: 8,
    },
    ordered_list: {
      marginBottom: 8,
    },
    bullet_list_item: {
      flexDirection: 'row',
      marginBottom: 4,
      paddingLeft: 5,
    },
    ordered_list_item: {
      flexDirection: 'row',
      marginBottom: 4,
      paddingLeft: 20,
    },
    bullet_list_icon: {
      color: PRIMARY_GREEN,
      marginRight: 8,
      fontSize,
      lineHeight: fontSize * 1.6,
    },
    ordered_list_icon: {
      color: TEXT_SECONDARY,
      marginRight: 8,
      fontSize,
      lineHeight: fontSize * 1.6,
      minWidth: 20,
    },
    list_item: {
      flex: 1,
    },
    // ─── Code blocks ───────────────────────────────────────────────────────
    fence: {
      backgroundColor: '#263238',
      borderRadius: 8,
      padding: 12,
      marginBottom: 8,
    },
    code_block: {
      backgroundColor: '#263238',
      borderRadius: 8,
      padding: 12,
      marginBottom: 8,
      fontFamily: 'monospace',
      fontSize: fontSize - 2,
      color: '#ECEFF1',
    },
    // ─── Block quote ───────────────────────────────────────────────────────
    blockquote: {
      backgroundColor: '#F1F8E9',
      borderLeftWidth: 4,
      borderLeftColor: PRIMARY_GREEN,
      paddingHorizontal: 12,
      paddingVertical: 8,
      marginBottom: 8,
      borderRadius: 4,
    },
    // ─── Horizontal rule ───────────────────────────────────────────────────
    hr: {
      backgroundColor: DIVIDER_COLOR,
      height: 1,
      marginVertical: 12,
    },
    // ─── Tables ────────────────────────────────────────────────────────────
    table: {
      borderWidth: 1,
      borderColor: DIVIDER_COLOR,
      borderRadius: 4,
      marginBottom: 8,
    },
    thead: {
      backgroundColor: '#F5F5F5',
    },
    th: {
      padding: 8,
      fontWeight: '600',
      fontSize: fontSize - 1,
      color: TEXT_PRIMARY,
      borderRightWidth: 1,
      borderRightColor: DIVIDER_COLOR,
    },
    td: {
      padding: 8,
      fontSize: fontSize - 1,
      color: TEXT_PRIMARY,
      borderRightWidth: 1,
      borderRightColor: DIVIDER_COLOR,
    },
    tr: {
      borderBottomWidth: 1,
      borderBottomColor: DIVIDER_COLOR,
    },
    // ─── Images ────────────────────────────────────────────────────────────
    image: {
      borderRadius: 8,
      maxWidth: '100%',
    },
  });
}
