/**
 * useWaveformSampler.ts
 * Provides a rolling buffer of amplitude samples for waveform visualization.
 */

import { useState, useEffect, useRef, useCallback } from 'react';
import { WAVEFORM_BAR_COUNT, WAVEFORM_UPDATE_INTERVAL_MS } from '../config/constants';
import { AudioRecorderService } from './AudioRecorderService';

/**
 * Returns an array of `WAVEFORM_BAR_COUNT` (40) amplitude values in range [0, 1].
 * When `isActive` is false, all bars return 0.
 *
 * @param isActive   - Whether recording is currently active
 * @param recorder   - Optional shared AudioRecorderService instance
 */
export function useWaveformSampler(
  isActive: boolean,
  recorder?: AudioRecorderService,
): number[] {
  const [amplitudes, setAmplitudes] = useState<number[]>(
    Array(WAVEFORM_BAR_COUNT).fill(0),
  );

  // Ring buffer of raw samples
  const bufferRef = useRef<number[]>(Array(WAVEFORM_BAR_COUNT).fill(0));
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const latestSampleRef = useRef<number>(0);

  const handleAmplitude = useCallback((amplitude: number) => {
    latestSampleRef.current = amplitude;
  }, []);

  useEffect(() => {
    if (!isActive) {
      // Clear to zeros
      const zeros = Array(WAVEFORM_BAR_COUNT).fill(0);
      bufferRef.current = zeros;
      setAmplitudes(zeros);

      if (intervalRef.current !== null) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
      return;
    }

    // Attach amplitude listener
    if (recorder) {
      recorder.setAmplitudeCallback(handleAmplitude);
    }

    // Poll and push new sample into ring buffer
    intervalRef.current = setInterval(() => {
      const newSample = recorder
        ? latestSampleRef.current
        : generateFakeSample();

      bufferRef.current = [
        ...bufferRef.current.slice(1),
        newSample,
      ];

      setAmplitudes([...bufferRef.current]);
    }, WAVEFORM_UPDATE_INTERVAL_MS);

    return () => {
      if (intervalRef.current !== null) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [isActive, recorder, handleAmplitude]);

  return amplitudes;
}

// ─── Demo mode: fake amplitude when no recorder attached ─────────────────────

let fakePhase = 0;

function generateFakeSample(): number {
  fakePhase += 0.15;
  const base = 0.3 + 0.25 * Math.sin(fakePhase);
  const noise = (Math.random() - 0.5) * 0.2;
  return Math.max(0.05, Math.min(1, base + noise));
}
