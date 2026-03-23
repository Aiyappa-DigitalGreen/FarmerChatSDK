/**
 * ImageProcessor.ts
 * Handles image resizing and base64 conversion before API upload.
 */

import { Image } from 'react-native';
import { fileToBase64 } from '../utils/base64';

const DEFAULT_MAX_DIMENSION = 1024;
const DEFAULT_JPEG_QUALITY = 0.7;

/**
 * Reads a local image URI, optionally resizes it, and returns a base64 string.
 *
 * Note: Full resizing requires a native image manipulation library (e.g.
 * react-native-image-manipulator). This implementation uses the base64
 * conversion directly, as react-native-image-picker already applies
 * maxWidth/maxHeight constraints when picking.
 *
 * @param uri          - Local file:// URI of the image
 * @param maxDimension - Maximum width or height in pixels (default 1024)
 * @param quality      - JPEG compression quality 0–1 (default 0.7)
 * @returns Base64-encoded image string (without data URI prefix)
 */
export async function getBase64FromUri(
  uri: string,
  maxDimension: number = DEFAULT_MAX_DIMENSION,
  quality: number = DEFAULT_JPEG_QUALITY,
): Promise<string> {
  // Validate the image is readable before encoding
  await validateImageUri(uri);

  // Convert to base64
  // react-native-image-picker already resizes to maxWidth/maxHeight
  // For further processing, integrate react-native-image-manipulator:
  //   const result = await ImageManipulator.manipulateAsync(uri,
  //     [{ resize: { width: maxDimension } }],
  //     { compress: quality, format: SaveFormat.JPEG, base64: true }
  //   );
  //   return result.base64 ?? '';
  void maxDimension;
  void quality;

  return fileToBase64(uri);
}

/**
 * Returns the dimensions of a local image URI.
 */
export function getImageDimensions(
  uri: string,
): Promise<{ width: number; height: number }> {
  return new Promise((resolve, reject) => {
    Image.getSize(
      uri,
      (width, height) => resolve({ width, height }),
      (error) => reject(error),
    );
  });
}

/**
 * Generates a safe filename for upload from a URI.
 */
export function uriToFileName(uri: string): string {
  const parts = uri.split('/');
  const last = parts[parts.length - 1];
  if (last && last.length > 0) {
    // Ensure it has an extension
    if (!last.includes('.')) {
      return `${last}.jpg`;
    }
    return last;
  }
  return `image_${Date.now()}.jpg`;
}

// ─── Private ──────────────────────────────────────────────────────────────────

function validateImageUri(uri: string): Promise<void> {
  return new Promise((resolve, reject) => {
    Image.getSize(
      uri,
      () => resolve(),
      () => reject(new Error(`ImageProcessor: cannot read image at ${uri}`)),
    );
  });
}
