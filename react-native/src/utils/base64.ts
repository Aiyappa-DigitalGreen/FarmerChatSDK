/**
 * base64.ts
 * Utility for converting file URIs to base64 strings.
 */

/**
 * Reads a local file URI and returns its contents as a base64 string.
 *
 * On React Native the built-in fetch API can read file:// URIs and
 * return an ArrayBuffer which we then convert to base64.
 */
export async function fileToBase64(uri: string): Promise<string> {
  // React Native's fetch supports file:// URIs
  const response = await fetch(uri);
  const blob = await response.blob();

  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onloadend = () => {
      if (typeof reader.result === 'string') {
        // result is "data:<mime>;base64,<data>" – strip the prefix
        const base64 = reader.result.split(',')[1];
        if (base64 !== undefined) {
          resolve(base64);
        } else {
          reject(new Error('base64: could not extract data portion'));
        }
      } else {
        reject(new Error('base64: unexpected result type'));
      }
    };
    reader.onerror = () => reject(new Error('base64: FileReader error'));
    reader.readAsDataURL(blob);
  });
}

/**
 * Returns the byte length of a base64 string (approx decoded size).
 */
export function base64ByteLength(base64: string): number {
  const padding = (base64.match(/=+$/) ?? []).length;
  return Math.floor((base64.length * 3) / 4) - padding;
}
