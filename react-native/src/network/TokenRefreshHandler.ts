/**
 * TokenRefreshHandler.ts
 * Manages token refresh with a mutex to prevent concurrent refresh calls.
 *
 * Strategy:
 * 1. POST /api/user/get_new_access_token/ with {refresh_token}
 * 2. On failure, POST /api/user/send_tokens/ with {device_id, user_id} and API-Key header
 *    This re-issues tokens for the SAME guest user — conversation history is preserved.
 *    (Do NOT fall back to initialize_user — that creates a brand new user, losing all history.)
 */

import { API_REFRESH_TOKEN, API_SEND_TOKENS, GUEST_API_KEY } from '../config/constants';
import { RefreshTokenRequest } from '../models/requests';
import { RefreshTokenResponse } from '../models/responses';
import { TokenExpiredError } from './NetworkError';
import { TokenStorage } from '../storage/TokenStorage';

// ─── Mutex ───────────────────────────────────────────────────────────────────

let refreshPromise: Promise<string> | null = null;

// ─── Public ──────────────────────────────────────────────────────────────────

/**
 * Obtains a new access token.
 * All concurrent callers share a single in-flight promise to avoid duplicate requests.
 */
export async function refreshIfNeeded(baseUrl: string): Promise<string> {
  if (refreshPromise) {
    return refreshPromise;
  }

  refreshPromise = (async (): Promise<string> => {
    try {
      return await attemptPrimaryRefresh(baseUrl);
    } catch {
      // Primary refresh failed — re-issue tokens for the same guest user via send_tokens
      try {
        return await attemptSendTokens(baseUrl);
      } catch {
        throw new TokenExpiredError(API_REFRESH_TOKEN);
      }
    }
  })();

  try {
    return await refreshPromise;
  } finally {
    refreshPromise = null;
  }
}

// ─── Step 1: normal token refresh ────────────────────────────────────────────

async function attemptPrimaryRefresh(baseUrl: string): Promise<string> {
  const refreshToken = await TokenStorage.getRefreshToken();
  if (!refreshToken) throw new Error('No refresh token stored');

  const url = `${baseUrl}/${API_REFRESH_TOKEN}`;
  const body: RefreshTokenRequest = { refresh_token: refreshToken };

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    throw new Error(`Primary refresh failed: ${response.status}`);
  }

  const data: RefreshTokenResponse = await response.json();
  if (!data.access_token) throw new Error('No access_token in refresh response');

  await TokenStorage.saveTokens(
    data.access_token,
    data.refresh_token ?? refreshToken,
  );
  return data.access_token;
}

// ─── Step 2: re-issue tokens for the same guest user via send_tokens ─────────
//
// send_tokens uses the existing device_id + user_id to obtain new tokens for
// the SAME guest account. This preserves conversation history.
// Never fall back to initialize_user here — that creates a new user identity.

async function attemptSendTokens(baseUrl: string): Promise<string> {
  const deviceId = await TokenStorage.getOrCreateDeviceId();
  const userId = await TokenStorage.getUserId();
  if (!userId) throw new Error('No user_id stored — cannot call send_tokens');

  const url = `${baseUrl.replace(/\/$/, '')}/${API_SEND_TOKENS}`;

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'API-Key': GUEST_API_KEY,
    },
    body: JSON.stringify({ device_id: deviceId, user_id: userId }),
  });

  if (!response.ok) {
    throw new Error(`send_tokens failed: ${response.status}`);
  }

  const data: RefreshTokenResponse = await response.json();
  if (!data.access_token) throw new Error('No access_token in send_tokens response');

  await TokenStorage.saveTokens(
    data.access_token,
    data.refresh_token ?? (await TokenStorage.getRefreshToken()) ?? '',
  );
  return data.access_token;
}
