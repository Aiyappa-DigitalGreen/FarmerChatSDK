/**
 * ChatFAB.tsx  (PUBLIC API)
 * Circular floating action button that opens ChatModal on press.
 */

import React, { memo, useState, useRef, useEffect } from 'react';
import {
  TouchableOpacity,
  StyleSheet,
  ViewStyle,
  Animated,
  View,
  Text,
} from 'react-native';
import { ChatModal } from './ChatModal';
import { PRIMARY_GREEN, WHITE } from '../config/constants';

// ─── Public Props ─────────────────────────────────────────────────────────────

export interface ChatFABProps {
  /** Optional: resume an existing conversation on open */
  conversationId?: string;
  /** Additional styles for the FAB container */
  style?: ViewStyle;
  /** Background color of the FAB (default: '#2E7D32') */
  tintColor?: string;
  /** Diameter of the FAB in dp (default: 56) */
  fabSize?: number;
}

// ─── Component ────────────────────────────────────────────────────────────────

export const ChatFAB = memo(function ChatFAB({
  conversationId,
  style,
  tintColor = PRIMARY_GREEN,
  fabSize = 56,
}: ChatFABProps): React.JSX.Element {
  const [modalVisible, setModalVisible] = useState(false);
  const scaleAnim = useRef(new Animated.Value(1)).current;
  const appearAnim = useRef(new Animated.Value(0)).current;

  // Entrance animation
  useEffect(() => {
    Animated.spring(appearAnim, {
      toValue: 1,
      tension: 50,
      friction: 7,
      useNativeDriver: true,
    }).start();
  }, [appearAnim]);

  const handlePress = () => {
    // Press feedback
    Animated.sequence([
      Animated.timing(scaleAnim, {
        toValue: 0.88,
        duration: 80,
        useNativeDriver: true,
      }),
      Animated.spring(scaleAnim, {
        toValue: 1,
        tension: 60,
        friction: 5,
        useNativeDriver: true,
      }),
    ]).start(() => {
      setModalVisible(true);
    });
  };

  const iconSize = Math.round(fabSize * 0.45);

  return (
    <>
      <Animated.View
        style={[
          styles.fabContainer,
          {
            transform: [
              { scale: Animated.multiply(scaleAnim, appearAnim) },
            ],
          },
          style,
        ]}
      >
        <TouchableOpacity
          onPress={handlePress}
          activeOpacity={0.85}
          style={[
            styles.fab,
            {
              width: fabSize,
              height: fabSize,
              borderRadius: fabSize / 2,
              backgroundColor: tintColor,
            },
          ]}
          accessibilityRole="button"
          accessibilityLabel="Open FarmerChat"
          accessibilityHint="Opens the FarmerChat AI assistant"
        >
          <ChatIcon size={iconSize} color={WHITE} />
        </TouchableOpacity>

        {/* Notification badge placeholder */}
        <View style={styles.badge} pointerEvents="none">
          {/* Extend here to show unread count */}
        </View>
      </Animated.View>

      {/* Modal */}
      <ChatModal
        visible={modalVisible}
        onClose={() => setModalVisible(false)}
        conversationId={conversationId}
      />
    </>
  );
});

// ─── Chat icon using View primitives ─────────────────────────────────────────

function ChatIcon({
  size,
  color,
}: {
  size: number;
  color: string;
}): React.JSX.Element {
  return (
    <View style={{ width: size, height: size, alignItems: 'center', justifyContent: 'center' }}>
      <Text style={{ fontSize: size * 0.9, color, lineHeight: size }}>💬</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  fabContainer: {
    // Caller provides absolute position via `style` prop
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 8,
  },
  fab: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  badge: {
    position: 'absolute',
    top: 0,
    right: 0,
  },
});
