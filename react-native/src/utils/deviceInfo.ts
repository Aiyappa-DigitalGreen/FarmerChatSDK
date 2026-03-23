/**
 * deviceInfo.ts
 * Builds the Device-Info header value used on every authenticated request.
 */

import { Platform } from 'react-native';
import { BUILD_VERSION } from '../config/constants';

export interface DeviceInfoPayload {
  'Build-Version': string;
  manufacturer: string;
  model: string;
  os_version: string;
  device_id: string;
  platform: string;
  app_version: string;
}

/**
 * Returns a URL-encoded JSON string suitable for the Device-Info header.
 * Example output: %7B%22Build-Version%22%3A%22v2%22%2C...%7D
 */
export function buildDeviceInfoHeader(deviceId: string): string {
  const payload: DeviceInfoPayload = {
    'Build-Version': BUILD_VERSION,
    manufacturer: getManufacturer(),
    model: getModel(),
    os_version: Platform.Version.toString(),
    device_id: deviceId,
    platform: Platform.OS,
    app_version: '1.0.0',
  };

  return encodeURIComponent(JSON.stringify(payload));
}

function getManufacturer(): string {
  if (Platform.OS === 'android') {
    // react-native doesn't expose manufacturer natively without a library,
    // return a sensible default that can be overridden with a real device-info lib
    return 'Android';
  }
  return 'Apple';
}

function getModel(): string {
  if (Platform.OS === 'android') {
    return 'Android Device';
  }
  // For iOS, Platform.constants.systemName is available
  const constants = Platform.constants as { Model?: string };
  return constants?.Model ?? 'iPhone';
}
