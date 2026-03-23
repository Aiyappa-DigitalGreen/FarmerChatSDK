/**
 * SDKConfig.ts
 * Central configuration singleton for FarmerChat SDK.
 *
 * The SDK internally manages authentication by calling the guest user
 * initialization endpoint on first install. No tokens or user IDs need
 * to be provided by the host app.
 *
 * Call FarmerChatSDK.configure() once at app startup before using any components.
 */

import { TokenStorage } from '../storage/TokenStorage';
import { initializeGuestUser } from '../network/GuestApiClient';

export interface SDKConfiguration {
  /**
   * Your SDK API key from the FarmerChat developer portal.
   * Format: fc_live_xxx (production) or fc_test_xxx (sandbox).
   * Sent as X-SDK-Key on every API request so the backend can identify your integration.
   */
  sdkApiKey: string;
  /** Base URL of the FarmerChat backend, e.g. "https://api.farmerchat.com" */
  baseUrl: string;
  /** Optional content provider identifier for multi-tenant setups */
  contentProviderId?: string;
}

class FarmerChatSDKClass {
  private config: SDKConfiguration | null = null;
  private tokenInitPromise: Promise<void> | null = null;

  /**
   * Initialize the SDK. Must be called once at app startup (e.g. App.tsx).
   * Tokens are fetched automatically from the backend using a stable device ID.
   * On subsequent launches the stored tokens are reused — no network call.
   */
  configure(config: SDKConfiguration): void {
    if (!config.baseUrl) {
      throw new Error('[FarmerChatSDK] baseUrl is required');
    }
    if (!isValidSdkApiKey(config.sdkApiKey)) {
      throw new Error(
        `[FarmerChatSDK] Invalid SDK API key '${config.sdkApiKey?.slice(0, 12) ?? ''}...'. ` +
        "Keys must start with 'fc_live_' or 'fc_test_' followed by at least 16 alphanumeric characters. " +
        'Obtain your key from the FarmerChat developer portal.',
      );
    }

    this.config = {
      ...config,
      baseUrl: config.baseUrl.replace(/\/$/, ''),
    };

    // Start token initialization in background — will be awaited before chat opens
    this.tokenInitPromise = this.initializeTokensIfNeeded();
  }

  /**
   * Returns the current configuration.
   * Throws if SDK has not been configured yet.
   */
  getConfig(): SDKConfiguration {
    if (!this.config) {
      throw new Error(
        '[FarmerChatSDK] SDK is not configured. Call FarmerChatSDK.configure() first.',
      );
    }
    return this.config;
  }

  /**
   * Ensures tokens are available. Awaited internally before making any API call.
   * If configure() already started a fetch, this joins that promise.
   * Throws with a user-facing message if initialization fails, so ViewModels can
   * show a meaningful error instead of a cryptic 401 / API failure.
   */
  async ensureTokens(): Promise<void> {
    // Join the background init started by configure(), if still in-flight
    if (this.tokenInitPromise) {
      try { await this.tokenInitPromise; } catch { /* will retry below */ }
      this.tokenInitPromise = null;
    }

    const hasTokens = await TokenStorage.hasTokens();
    if (hasTokens) return;

    // Actively attempt initialization now
    if (!this.config) return;
    try {
      const deviceId = await TokenStorage.getOrCreateDeviceId();
      const response = await initializeGuestUser(this.config.baseUrl, deviceId);
      await TokenStorage.saveTokens(
        response.access_token,
        response.refresh_token,
        response.user_id,
      );
    } catch (e) {
      throw new Error(buildAuthErrorMessage(e));
    }
  }

  /**
   * Returns true if the SDK has been configured.
   */
  isConfigured(): boolean {
    return this.config !== null;
  }

  /**
   * Clears stored auth tokens (e.g. on user logout).
   * Device ID is preserved — it's stable SDK-internal identity, not user data.
   * Next call to ensureTokens() will re-fetch via initialize_user.
   */
  async clearSession(): Promise<void> {
    await TokenStorage.clear();
  }

  // ─── Private ──────────────────────────────────────────────────────────────

  private async initializeTokensIfNeeded(): Promise<void> {
    if (!this.config) return;

    const hasTokens = await TokenStorage.hasTokens();
    if (hasTokens) return;

    // Best-effort background attempt — errors are intentionally swallowed here.
    // ensureTokens() will actively retry and throw a user-facing error if needed.
    try {
      const deviceId = await TokenStorage.getOrCreateDeviceId();
      const response = await initializeGuestUser(this.config.baseUrl, deviceId);
      await TokenStorage.saveTokens(
        response.access_token,
        response.refresh_token,
        response.user_id,
      );
    } catch {
      // Will be retried by ensureTokens() when the user opens the chat
    }
  }
}

/**
 * Validates the SDK API key format.
 * Valid: fc_live_<16+ alphanumeric> or fc_test_<16+ alphanumeric>
 */
export function isValidSdkApiKey(key: string): boolean {
  if (!key) return false;
  const prefix = key.startsWith('fc_live_') ? 'fc_live_' :
                 key.startsWith('fc_test_') ? 'fc_test_' : null;
  if (!prefix) return false;
  const suffix = key.slice(prefix.length);
  return suffix.length >= 16 && /^[a-zA-Z0-9]+$/.test(suffix);
}

function buildAuthErrorMessage(e: unknown): string {
  const msg = e instanceof Error ? e.message : String(e);
  if ((msg.includes('HTTP 4') || msg.includes('status: 4')) &&
      (msg.toLowerCase().includes('device') || msg.toLowerCase().includes('limit'))) {
    return 'This device has reached its guest account limit. Please contact support.';
  }
  if (msg.includes('HTTP 4') || msg.includes('status: 4')) {
    return 'Unable to start a chat session. Please contact support if this persists.';
  }
  return 'Please check your internet connection and try again.';
}

export const FarmerChatSDK = new FarmerChatSDKClass();
