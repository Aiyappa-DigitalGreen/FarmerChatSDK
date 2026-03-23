/**
 * TokenStorage.ts
 * Persists tokens and device identity securely using iOS Keychain / Android Keystore
 * via react-native-keychain.
 */

import * as Keychain from 'react-native-keychain';

const SERVICE_NAME = 'com.farmerchat.sdk';
const SERVICE_DEVICE = 'com.farmerchat.sdk.device';
const USERNAME_TOKENS = 'farmerchat_tokens';
const USERNAME_DEVICE = 'farmerchat_device';

interface StoredTokens {
  accessToken: string;
  refreshToken: string;
  userId?: string;
}

interface StoredDevice {
  deviceId: string;
}

export const TokenStorage = {

  // ─── Tokens ────────────────────────────────────────────────────────────────

  async saveTokens(
    accessToken: string,
    refreshToken: string,
    userId?: string,
  ): Promise<void> {
    const current = await TokenStorage.getStoredTokens();
    const value: StoredTokens = {
      accessToken,
      refreshToken,
      userId: userId ?? current?.userId,
    };
    await Keychain.setGenericPassword(USERNAME_TOKENS, JSON.stringify(value), {
      service: SERVICE_NAME,
      accessible: Keychain.ACCESSIBLE.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
    });
  },

  async getAccessToken(): Promise<string | null> {
    return (await TokenStorage.getStoredTokens())?.accessToken ?? null;
  },

  async getRefreshToken(): Promise<string | null> {
    return (await TokenStorage.getStoredTokens())?.refreshToken ?? null;
  },

  async getUserId(): Promise<string | null> {
    return (await TokenStorage.getStoredTokens())?.userId ?? null;
  },

  async hasTokens(): Promise<boolean> {
    const tokens = await TokenStorage.getStoredTokens();
    return !!(tokens?.accessToken && tokens?.refreshToken && tokens?.userId);
  },

  // ─── Device ID ─────────────────────────────────────────────────────────────

  /**
   * Returns the stable device ID stored in Keychain, or generates and stores one
   * on first call. Using Keychain means the ID survives app restarts (but resets
   * on app reinstall — same semantics as iOS identifierForVendor).
   */
  async getOrCreateDeviceId(): Promise<string> {
    const existing = await TokenStorage.getDeviceId();
    if (existing) return existing;

    // Generate once and persist — this is the SDK's internal device identifier
    const newId = generateStableId();
    await TokenStorage.saveDeviceId(newId);
    return newId;
  },

  async getDeviceId(): Promise<string | null> {
    try {
      const result = await Keychain.getGenericPassword({ service: SERVICE_DEVICE });
      if (!result || typeof result === 'boolean') return null;
      const parsed = JSON.parse(result.password) as StoredDevice;
      return parsed.deviceId ?? null;
    } catch {
      return null;
    }
  },

  async saveDeviceId(deviceId: string): Promise<void> {
    const value: StoredDevice = { deviceId };
    await Keychain.setGenericPassword(USERNAME_DEVICE, JSON.stringify(value), {
      service: SERVICE_DEVICE,
      accessible: Keychain.ACCESSIBLE.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
    });
  },

  // ─── Clear ─────────────────────────────────────────────────────────────────

  async clear(): Promise<void> {
    await Keychain.resetGenericPassword({ service: SERVICE_NAME });
    // Intentionally keep deviceId — it's the stable device identity, not auth
  },

  // ─── Private ───────────────────────────────────────────────────────────────

  async getStoredTokens(): Promise<StoredTokens | null> {
    try {
      const credentials = await Keychain.getGenericPassword({ service: SERVICE_NAME });
      if (!credentials || typeof credentials === 'boolean') return null;
      return JSON.parse(credentials.password) as StoredTokens;
    } catch {
      return null;
    }
  },
};

/**
 * Generates a stable ID using timestamp + random components.
 * Called exactly ONCE per SDK install and persisted in Keychain.
 * The host app never sees or provides this — it is entirely SDK-internal.
 */
function generateStableId(): string {
  const timestamp = Date.now().toString(36);
  const random1 = Math.random().toString(36).slice(2, 9);
  const random2 = Math.random().toString(36).slice(2, 9);
  return `${timestamp}-${random1}-${random2}`;
}
