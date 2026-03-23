/**
 * AudioRecorderService.ts
 * Wraps react-native-audio-recorder-player for voice recording.
 */

import AudioRecorderPlayer, {
  AudioEncoderAndroidType,
  AudioSourceAndroidType,
  AVEncoderAudioQualityIOSType,
  AVEncodingOption,
  OutputFormatAndroidType,
  RecordBackType,
} from 'react-native-audio-recorder-player';
import { Platform, PermissionsAndroid } from 'react-native';
import { fileToBase64 } from '../utils/base64';

export class AudioRecorderService {
  private recorder: AudioRecorderPlayer;
  private _isRecording = false;
  private _durationSeconds = 0;
  private _currentUri: string | null = null;
  private _amplitudeCallback?: (amplitude: number) => void;

  constructor() {
    this.recorder = new AudioRecorderPlayer();
    this.recorder.setSubscriptionDuration(0.1); // 100ms update interval
  }

  get isRecording(): boolean {
    return this._isRecording;
  }

  /** Returns elapsed duration in seconds */
  getDuration(): number {
    return this._durationSeconds;
  }

  /** Register a callback to receive amplitude samples (0.0 – 1.0) */
  setAmplitudeCallback(cb: (amplitude: number) => void): void {
    this._amplitudeCallback = cb;
  }

  /**
   * Requests microphone permission (Android), then starts recording.
   * Throws if permission is denied.
   */
  async startRecording(): Promise<void> {
    if (this._isRecording) {
      return;
    }

    if (Platform.OS === 'android') {
      await this.requestAndroidPermissions();
    }

    const path = Platform.select({
      ios: 'farmerchat_voice.m4a',
      android: `${Date.now()}_farmerchat_voice.mp4`,
    });

    const audioSet = {
      AudioEncoderAndroid: AudioEncoderAndroidType.AAC,
      AudioSourceAndroid: AudioSourceAndroidType.MIC,
      AVEncoderAudioQualityKeyIOS: AVEncoderAudioQualityIOSType.high,
      AVNumberOfChannelsKeyIOS: 1,
      AVFormatIDKeyIOS: AVEncodingOption.aac,
      OutputFormatAndroid: OutputFormatAndroidType.MPEG_4,
    };

    await this.recorder.startRecorder(path, audioSet, true);

    this.recorder.addRecordBackListener((e: RecordBackType) => {
      this._durationSeconds = e.currentPosition / 1000;

      if (this._amplitudeCallback) {
        // currentMetering is in dBFS (typically -160 to 0)
        // Normalize to 0.0 – 1.0
        const db = e.currentMetering ?? -60;
        const normalized = Math.max(0, Math.min(1, (db + 60) / 60));
        this._amplitudeCallback(normalized);
      }
    });

    this._isRecording = true;
  }

  /**
   * Stops recording and returns the local file URI.
   * Throws if not currently recording.
   */
  async stopRecording(): Promise<string> {
    if (!this._isRecording) {
      throw new Error('AudioRecorderService: not currently recording');
    }

    const uri = await this.recorder.stopRecorder();
    this.recorder.removeRecordBackListener();
    this._isRecording = false;
    this._currentUri = uri;
    this._durationSeconds = 0;
    return uri;
  }

  /**
   * Converts a local file URI to a base64 string.
   */
  async getBase64(fileUri: string): Promise<string> {
    return fileToBase64(fileUri);
  }

  /**
   * Cancels ongoing recording without returning audio.
   */
  async cancelRecording(): Promise<void> {
    if (!this._isRecording) {
      return;
    }
    try {
      await this.recorder.stopRecorder();
    } catch {
      // ignore stop errors during cancel
    }
    this.recorder.removeRecordBackListener();
    this._isRecording = false;
    this._durationSeconds = 0;
  }

  // ─── Android permissions ─────────────────────────────────────────────────

  private async requestAndroidPermissions(): Promise<void> {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
      {
        title: 'Microphone Permission',
        message: 'FarmerChat needs access to your microphone to record voice questions.',
        buttonPositive: 'Allow',
        buttonNegative: 'Deny',
      },
    );

    if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
      throw new Error('Microphone permission denied');
    }

    if (Number(Platform.Version) < 33) {
      const storageGranted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
        {
          title: 'Storage Permission',
          message: 'FarmerChat needs storage access to save voice recordings.',
          buttonPositive: 'Allow',
          buttonNegative: 'Deny',
        },
      );
      if (storageGranted !== PermissionsAndroid.RESULTS.GRANTED) {
        throw new Error('Storage permission denied');
      }
    }
  }
}
