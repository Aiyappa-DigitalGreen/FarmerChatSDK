/**
 * ImagePickerService.ts
 * Wraps react-native-image-picker for camera and gallery access.
 */

import {
  launchCamera,
  launchImageLibrary,
  ImagePickerResponse,
  CameraOptions,
  ImageLibraryOptions,
} from 'react-native-image-picker';
import { Platform, PermissionsAndroid } from 'react-native';

export interface ImagePickerResult {
  uri: string;
  base64?: string;
  width: number;
  height: number;
  fileName?: string;
  fileSize?: number;
  type?: string;
}

const sharedOptions: CameraOptions & ImageLibraryOptions = {
  mediaType: 'photo',
  includeBase64: true,
  quality: 0.7,
  maxWidth: 1024,
  maxHeight: 1024,
  presentationStyle: 'pageSheet',
};

/**
 * Opens the device image gallery.
 * Returns null if user cancels or an error occurs.
 */
export async function pickFromGallery(): Promise<ImagePickerResult | null> {
  if (Platform.OS === 'android') {
    await requestReadStoragePermission();
  }

  return new Promise((resolve) => {
    launchImageLibrary(sharedOptions, (response: ImagePickerResponse) => {
      resolve(parsePickerResponse(response));
    });
  });
}

/**
 * Opens the device camera.
 * Returns null if user cancels or permission denied.
 */
export async function takePhoto(): Promise<ImagePickerResult | null> {
  if (Platform.OS === 'android') {
    await requestCameraPermission();
  }

  return new Promise((resolve) => {
    launchCamera(
      { ...sharedOptions, saveToPhotos: false },
      (response: ImagePickerResponse) => {
        resolve(parsePickerResponse(response));
      },
    );
  });
}

// ─── Private helpers ──────────────────────────────────────────────────────────

function parsePickerResponse(
  response: ImagePickerResponse,
): ImagePickerResult | null {
  if (response.didCancel || response.errorCode || !response.assets?.length) {
    return null;
  }

  const asset = response.assets[0];
  if (!asset.uri) {
    return null;
  }

  return {
    uri: asset.uri,
    base64: asset.base64,
    width: asset.width ?? 0,
    height: asset.height ?? 0,
    fileName: asset.fileName,
    fileSize: asset.fileSize,
    type: asset.type,
  };
}

async function requestCameraPermission(): Promise<void> {
  const granted = await PermissionsAndroid.request(
    PermissionsAndroid.PERMISSIONS.CAMERA,
    {
      title: 'Camera Permission',
      message: 'FarmerChat needs camera access to photograph crops.',
      buttonPositive: 'Allow',
      buttonNegative: 'Deny',
    },
  );
  if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
    throw new Error('Camera permission denied');
  }
}

async function requestReadStoragePermission(): Promise<void> {
  if (Number(Platform.Version) >= 33) {
    // Android 13+ uses READ_MEDIA_IMAGES, no runtime prompt needed from app side
    return;
  }
  const granted = await PermissionsAndroid.request(
    PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
    {
      title: 'Storage Permission',
      message: 'FarmerChat needs storage access to select crop images.',
      buttonPositive: 'Allow',
      buttonNegative: 'Deny',
    },
  );
  if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
    throw new Error('Storage permission denied');
  }
}
