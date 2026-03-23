/**
 * AuthInterceptor.ts
 * Adds required authentication and metadata headers to fetch requests.
 */

import { BUILD_VERSION } from '../config/constants';

/**
 * Merges authentication headers into the provided RequestInit.
 *
 * @param init        - Base RequestInit (method, body, existing headers, etc.)
 * @param accessToken - JWT access token
 * @param deviceInfo  - URL-encoded JSON string of device metadata
 * @param sdkApiKey   - SDK API key (X-SDK-Key header) that identifies this integration
 * @returns           New RequestInit with auth headers merged in
 */
export function addAuthHeaders(
  init: RequestInit,
  accessToken: string,
  deviceInfo: string,
  sdkApiKey: string,
): RequestInit {
  const existingHeaders =
    init.headers instanceof Headers
      ? Object.fromEntries((init.headers as Headers).entries())
      : (init.headers as Record<string, string>) ?? {};

  const authHeaders: Record<string, string> = {
    'X-SDK-Key': sdkApiKey,           // identifies this integration to the FarmerChat backend
    'Authorization': `Bearer ${accessToken}`,
    'Build-Version': BUILD_VERSION,
    'Device-Info': deviceInfo,
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };

  return {
    ...init,
    headers: {
      ...existingHeaders,
      ...authHeaders,
    },
  };
}

/**
 * Builds a RequestInit with guest API-Key header for fallback token requests.
 */
export function addGuestApiKeyHeader(
  init: RequestInit,
  apiKey: string,
): RequestInit {
  const existingHeaders =
    init.headers instanceof Headers
      ? Object.fromEntries((init.headers as Headers).entries())
      : (init.headers as Record<string, string>) ?? {};

  return {
    ...init,
    headers: {
      ...existingHeaders,
      'API-Key': apiKey,
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
  };
}
