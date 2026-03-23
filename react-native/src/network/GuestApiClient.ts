/**
 * GuestApiClient.ts
 * Standalone fetch client for guest user initialization.
 * Operates without auth tokens — uses API-Key header only.
 */

import { API_INITIALIZE_USER, GUEST_API_KEY } from '../config/constants';

export interface InitializeGuestUserResponse {
  access_token: string;
  refresh_token: string;
  user_id?: string;
  created_now?: boolean;  // true = new user created, false = existing user returned for this device_id
  country_code?: string;
  country?: string;
  state?: string;
}

/**
 * POST api/user/initialize_user/
 * Returns access_token, refresh_token and user_id for a new guest device.
 * Throws on network error or non-2xx response.
 */
export async function initializeGuestUser(
  baseUrl: string,
  deviceId: string,
): Promise<InitializeGuestUserResponse> {
  const url = `${baseUrl.replace(/\/$/, '')}/${API_INITIALIZE_USER}`;

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Api-Key ${GUEST_API_KEY}`,
    },
    body: JSON.stringify({ device_id: deviceId }),
  });

  if (!response.ok) {
    const body = await response.text().catch(() => '');
    throw new Error(`initialize_user failed: HTTP ${response.status} — ${body}`);
  }

  return response.json() as Promise<InitializeGuestUserResponse>;
}
