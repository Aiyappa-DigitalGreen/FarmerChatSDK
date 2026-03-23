/**
 * SoundWaveAnimation.tsx
 * 14 animated bars with a continuous sine-wave stagger for TTS playback.
 */

import React, { useEffect, useRef, memo } from 'react';
import { View, StyleSheet, Animated, Easing } from 'react-native';
import { PRIMARY_GREEN } from '../../config/constants';

const BAR_COUNT = 14;
const BAR_WIDTH = 3;
const BAR_SPACING = 2;
const MIN_HEIGHT = 4;
const MAX_HEIGHT = 20;
const BASE_DURATION = 500;

interface SoundWaveAnimationProps {
  color?: string;
  isActive?: boolean;
}

export const SoundWaveAnimation = memo(function SoundWaveAnimation({
  color = PRIMARY_GREEN,
  isActive = true,
}: SoundWaveAnimationProps): React.JSX.Element {
  const animations = useRef<Animated.Value[]>(
    Array.from({ length: BAR_COUNT }, () => new Animated.Value(0.2)),
  ).current;

  useEffect(() => {
    if (!isActive) {
      animations.forEach((anim) => {
        Animated.timing(anim, {
          toValue: 0.2,
          duration: 200,
          useNativeDriver: false,
        }).start();
      });
      return;
    }

    const loops = animations.map((anim, i) => {
      // Staggered phase offset using sine wave pattern
      const phaseDelay = (i / BAR_COUNT) * BASE_DURATION;
      const duration = BASE_DURATION + (i % 3) * 100;

      return Animated.loop(
        Animated.sequence([
          Animated.delay(phaseDelay),
          Animated.timing(anim, {
            toValue: 0.2 + 0.3 * Math.abs(Math.sin((i * Math.PI) / BAR_COUNT)),
            duration: duration * 0.3,
            easing: Easing.linear,
            useNativeDriver: false,
          }),
          Animated.timing(anim, {
            toValue: 1,
            duration: duration * 0.4,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: false,
          }),
          Animated.timing(anim, {
            toValue: 0.2,
            duration: duration * 0.3,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: false,
          }),
        ]),
      );
    });

    loops.forEach((loop) => loop.start());

    return () => {
      loops.forEach((loop) => loop.stop());
    };
  }, [isActive, animations]);

  return (
    <View style={styles.container}>
      {animations.map((anim, i) => {
        const height = anim.interpolate({
          inputRange: [0, 1],
          outputRange: [MIN_HEIGHT, MAX_HEIGHT],
        });
        return (
          <Animated.View
            key={i}
            style={[
              styles.bar,
              {
                height,
                backgroundColor: color,
              },
            ]}
          />
        );
      })}
    </View>
  );
});

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    height: MAX_HEIGHT,
  },
  bar: {
    width: BAR_WIDTH,
    marginHorizontal: BAR_SPACING / 2,
    borderRadius: BAR_WIDTH / 2,
  },
});
