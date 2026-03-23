/**
 * AudioPlayerService.ts
 * Handles TTS audio URL playback via react-native-audio-recorder-player.
 */

import AudioRecorderPlayer, {
  PlayBackType,
} from 'react-native-audio-recorder-player';

export class AudioPlayerService {
  private player: AudioRecorderPlayer;
  private _isPlaying = false;
  private _isPaused = false;

  /** Called when playback completes naturally */
  onFinished?: () => void;

  constructor() {
    this.player = new AudioRecorderPlayer();
  }

  get isPlaying(): boolean {
    return this._isPlaying;
  }

  get isPaused(): boolean {
    return this._isPaused;
  }

  /**
   * Begin playback of an audio URL (remote http/https or local file://).
   */
  async playUrl(url: string): Promise<void> {
    if (this._isPlaying) {
      await this.stop();
    }

    await this.player.startPlayer(url);

    this.player.addPlayBackListener((e: PlayBackType) => {
      if (e.currentPosition >= e.duration && e.duration > 0) {
        // Playback complete
        this._isPlaying = false;
        this._isPaused = false;
        this.player.removePlayBackListener();
        this.onFinished?.();
      }
    });

    this._isPlaying = true;
    this._isPaused = false;
  }

  /**
   * Pause current playback.
   */
  pause(): void {
    if (this._isPlaying && !this._isPaused) {
      this.player.pausePlayer().catch(() => {
        // ignore
      });
      this._isPaused = true;
      this._isPlaying = false;
    }
  }

  /**
   * Resume paused playback.
   */
  resume(): void {
    if (this._isPaused) {
      this.player.resumePlayer().catch(() => {
        // ignore
      });
      this._isPaused = false;
      this._isPlaying = true;
    }
  }

  /**
   * Stop playback and reset state.
   */
  async stop(): Promise<void> {
    if (this._isPlaying || this._isPaused) {
      try {
        await this.player.stopPlayer();
      } catch {
        // ignore errors on stop
      }
      this.player.removePlayBackListener();
      this._isPlaying = false;
      this._isPaused = false;
    }
  }

  /**
   * Release native resources.
   */
  destroy(): void {
    this.player.removePlayBackListener();
  }
}
