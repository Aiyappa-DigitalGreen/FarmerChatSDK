/**
 * FollowUpQuestionsBar.tsx
 * Vertical list of related questions — each row has question text + green Ask button.
 */

import React, { memo, useCallback } from 'react';
import {
  TouchableOpacity,
  Text,
  View,
  StyleSheet,
} from 'react-native';
import { useChatContext } from '../../state/ChatContext';
import { PRIMARY_GREEN, WHITE, TEXT_PRIMARY, TEXT_SECONDARY } from '../../config/constants';

interface FollowUpQuestionsBarProps {
  questions: string[];
  questionIds?: string[];
}

export const FollowUpQuestionsBar = memo(function FollowUpQuestionsBar({
  questions,
  questionIds,
}: FollowUpQuestionsBarProps): React.JSX.Element | null {
  const { dispatch } = useChatContext();

  const handlePress = useCallback(
    (question: string, index: number) => {
      dispatch({
        type: 'SEND_FOLLOW_UP_QUESTION',
        question,
        followUpQuestionId: questionIds?.[index],
      });
    },
    [dispatch, questionIds],
  );

  if (!questions || questions.length === 0) {
    return null;
  }

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Related questions</Text>
      {questions.map((question, index) => (
        <View
          key={questionIds?.[index] ?? `${question}_${index}`}
          style={styles.row}
        >
          <Text style={styles.questionText}>{question}</Text>
          <TouchableOpacity
            onPress={() => handlePress(question, index)}
            style={styles.askButton}
            activeOpacity={0.8}
            accessibilityRole="button"
            accessibilityLabel={`Ask: ${question}`}
          >
            <Text style={styles.askButtonText}>Ask</Text>
          </TouchableOpacity>
        </View>
      ))}
    </View>
  );
});

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 8,
  },
  header: {
    fontSize: 15,
    fontWeight: '700',
    color: TEXT_PRIMARY,
    marginBottom: 4,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F2F2F2',
    borderRadius: 12,
    paddingVertical: 12,
    paddingLeft: 14,
    paddingRight: 10,
    gap: 10,
  },
  questionText: {
    flex: 1,
    fontSize: 14,
    color: TEXT_PRIMARY,
    lineHeight: 20,
  },
  askButton: {
    backgroundColor: PRIMARY_GREEN,
    borderRadius: 8,
    paddingHorizontal: 14,
    paddingVertical: 8,
    flexShrink: 0,
  },
  askButtonText: {
    fontSize: 13,
    fontWeight: '600',
    color: WHITE,
  },
});
