/**
 * WaveformView.tsx
 * 40 animated vertical bars representing live audio amplitude.
 */

import React, { memo, useEffect, useRef } from 'react';
import { View, StyleSheet, Animated } from 'react-native';
import { PRIMARY_GREEN, WAVEFORM_BAR_COUNT } from '../../config/constants';

interface WaveformViewProps {
  /** Array of WAVEFORM_BAR_COUNT (40) amplitude values in [0.0, 1.0] */
  amplitudes: number[];
  isActive: boolean;
  color?: string;
  barWidth?: number;
  barSpacing?: number;
  maxBarHeight?: number;
  minBarHeight?: number;
}

export const WaveformView = memo(function WaveformView({
  amplitudes,
  isActive,
  color = PRIMARY_GREEN,
  barWidth = 3,
  barSpacing = 2,
  maxBarHeight = 48,
  minBarHeight = 3,
}: WaveformViewProps): React.JSX.Element {
  const animValues = useRef<Animated.Value[]>(
    Array.from({ length: WAVEFORM_BAR_COUNT }, () => new Animated.Value(0)),
  ).current;

  useEffect(() => {
    const animations = amplitudes.map((amplitude, i) => {
      const targetHeight = isActive
        ? Math.max(minBarHeight, Math.min(maxBarHeight, amplitude * maxBarHeight))
        : minBarHeight;

      return Animated.timing(animValues[i], {
        toValue: targetHeight,
        duration: 80,
        useNativeDriver: false,
      });
    });

    Animated.parallel(animations).start();
  }, [amplitudes, isActive, animValues, maxBarHeight, minBarHeight]);

  return (
    <View style={styles.container}>
      {animValues.map((anim, i) => (
        <Animated.View
          key={i}
          style={[
            styles.bar,
            {
              width: barWidth,
              height: anim,
              marginHorizontal: barSpacing / 2,
              backgroundColor: color,
              opacity: isActive ? 1 : 0.3,
              borderRadius: barWidth / 2,
            },
          ]}
        />
      ))}
    </View>
  );
});

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    height: 56,
  },
  bar: {},
});
