/**
 * LoadingBubble.tsx
 * Animated typing indicator shown while waiting for AI response.
 */

import React, { useEffect, useRef, memo } from 'react';
import {
  View,
  StyleSheet,
  Animated,
  Easing,
} from 'react-native';
import { BUBBLE_AI_BACKGROUND, PRIMARY_GREEN } from '../../config/constants';

const DOT_SIZE = 8;
const DOT_SPACING = 6;
const ANIMATION_DURATION = 600;
const STAGGER_DELAY = 200;

export const LoadingBubble = memo(function LoadingBubble(): React.JSX.Element {
  const dot1 = useRef(new Animated.Value(0)).current;
  const dot2 = useRef(new Animated.Value(0)).current;
  const dot3 = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const createBounce = (value: Animated.Value, delay: number) =>
      Animated.loop(
        Animated.sequence([
          Animated.delay(delay),
          Animated.timing(value, {
            toValue: 1,
            duration: ANIMATION_DURATION / 2,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: true,
          }),
          Animated.timing(value, {
            toValue: 0,
            duration: ANIMATION_DURATION / 2,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: true,
          }),
        ]),
      );

    const anim1 = createBounce(dot1, 0);
    const anim2 = createBounce(dot2, STAGGER_DELAY);
    const anim3 = createBounce(dot3, STAGGER_DELAY * 2);

    anim1.start();
    anim2.start();
    anim3.start();

    return () => {
      anim1.stop();
      anim2.stop();
      anim3.stop();
    };
  }, [dot1, dot2, dot3]);

  const translateY = (anim: Animated.Value) =>
    anim.interpolate({
      inputRange: [0, 1],
      outputRange: [0, -8],
    });

  const opacity = (anim: Animated.Value) =>
    anim.interpolate({
      inputRange: [0, 0.5, 1],
      outputRange: [0.5, 1, 0.5],
    });

  return (
    <View style={styles.container}>
      <View style={styles.bubble}>
        {([dot1, dot2, dot3] as Animated.Value[]).map((anim, i) => (
          <Animated.View
            key={i}
            style={[
              styles.dot,
              {
                transform: [{ translateY: translateY(anim) }],
                opacity: opacity(anim),
              },
            ]}
          />
        ))}
      </View>
    </View>
  );
});

const styles = StyleSheet.create({
  container: {
    alignSelf: 'flex-start',
    maxWidth: '80%',
    paddingHorizontal: 16,
    paddingVertical: 4,
    marginBottom: 4,
  },
  bubble: {
    backgroundColor: BUBBLE_AI_BACKGROUND,
    borderRadius: 18,
    borderBottomLeftRadius: 4,
    paddingHorizontal: 16,
    paddingVertical: 14,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 2,
    elevation: 1,
  },
  dot: {
    width: DOT_SIZE,
    height: DOT_SIZE,
    borderRadius: DOT_SIZE / 2,
    backgroundColor: PRIMARY_GREEN,
    marginHorizontal: DOT_SPACING / 2,
  },
});
