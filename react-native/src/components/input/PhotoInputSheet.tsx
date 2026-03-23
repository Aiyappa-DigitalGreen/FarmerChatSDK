/**
 * PhotoInputSheet.tsx
 * Bottom action sheet for selecting camera or gallery as image source.
 */

import React, { memo, useCallback } from 'react';
import {
  Modal,
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Pressable,
  ActivityIndicator,
} from 'react-native';
import { pickFromGallery, takePhoto, ImagePickerResult } from '../../image/ImagePickerService';
import {
  PRIMARY_GREEN,
  TEXT_PRIMARY,
  TEXT_SECONDARY,
  WHITE,
} from '../../config/constants';

interface PhotoInputSheetProps {
  visible: boolean;
  onClose: () => void;
  onImageSelected: (result: ImagePickerResult) => void;
}

export const PhotoInputSheet = memo(function PhotoInputSheet({
  visible,
  onClose,
  onImageSelected,
}: PhotoInputSheetProps): React.JSX.Element {
  const [isLoading, setIsLoading] = React.useState(false);

  const handleCamera = useCallback(async () => {
    setIsLoading(true);
    try {
      const result = await takePhoto();
      if (result) {
        onImageSelected(result);
        onClose();
      }
    } catch (err) {
      const error = err as Error;
      console.warn('[PhotoInputSheet] Camera error:', error.message);
    } finally {
      setIsLoading(false);
    }
  }, [onImageSelected, onClose]);

  const handleGallery = useCallback(async () => {
    setIsLoading(true);
    try {
      const result = await pickFromGallery();
      if (result) {
        onImageSelected(result);
        onClose();
      }
    } catch (err) {
      const error = err as Error;
      console.warn('[PhotoInputSheet] Gallery error:', error.message);
    } finally {
      setIsLoading(false);
    }
  }, [onImageSelected, onClose]);

  return (
    <Modal
      visible={visible}
      transparent
      animationType="slide"
      onRequestClose={onClose}
    >
      <Pressable style={styles.overlay} onPress={onClose}>
        <View style={styles.sheet}>
          <View style={styles.handle} />
          <Text style={styles.title}>Add Image</Text>
          <Text style={styles.subtitle}>
            Photograph your crop or select an existing photo for AI analysis
          </Text>

          {isLoading ? (
            <ActivityIndicator
              size="large"
              color={PRIMARY_GREEN}
              style={styles.loader}
            />
          ) : (
            <View style={styles.options}>
              <TouchableOpacity
                style={styles.option}
                onPress={() => {
                  handleCamera().catch(() => {/* handled */});
                }}
                accessibilityRole="button"
                accessibilityLabel="Take photo with camera"
              >
                <View style={styles.optionIcon}>
                  <Text style={styles.optionEmoji}>📷</Text>
                </View>
                <View style={styles.optionText}>
                  <Text style={styles.optionTitle}>Take Photo</Text>
                  <Text style={styles.optionDesc}>Use camera to capture crop</Text>
                </View>
              </TouchableOpacity>

              <View style={styles.divider} />

              <TouchableOpacity
                style={styles.option}
                onPress={() => {
                  handleGallery().catch(() => {/* handled */});
                }}
                accessibilityRole="button"
                accessibilityLabel="Choose from photo gallery"
              >
                <View style={styles.optionIcon}>
                  <Text style={styles.optionEmoji}>🖼️</Text>
                </View>
                <View style={styles.optionText}>
                  <Text style={styles.optionTitle}>Choose from Gallery</Text>
                  <Text style={styles.optionDesc}>Select an existing photo</Text>
                </View>
              </TouchableOpacity>
            </View>
          )}

          <TouchableOpacity
            style={styles.cancelButton}
            onPress={onClose}
            accessibilityRole="button"
            accessibilityLabel="Cancel"
          >
            <Text style={styles.cancelText}>Cancel</Text>
          </TouchableOpacity>
        </View>
      </Pressable>
    </Modal>
  );
});

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: WHITE,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    paddingHorizontal: 20,
    paddingBottom: 40,
    paddingTop: 12,
  },
  handle: {
    width: 40,
    height: 4,
    backgroundColor: '#E0E0E0',
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: TEXT_PRIMARY,
    marginBottom: 6,
  },
  subtitle: {
    fontSize: 13,
    color: TEXT_SECONDARY,
    lineHeight: 18,
    marginBottom: 20,
  },
  loader: {
    marginVertical: 32,
  },
  options: {
    backgroundColor: '#F8F9FA',
    borderRadius: 14,
    overflow: 'hidden',
    marginBottom: 12,
  },
  option: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 16,
    paddingHorizontal: 16,
  },
  optionIcon: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#E8F5E9',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  optionEmoji: {
    fontSize: 22,
  },
  optionText: {
    flex: 1,
  },
  optionTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: TEXT_PRIMARY,
    marginBottom: 2,
  },
  optionDesc: {
    fontSize: 12,
    color: TEXT_SECONDARY,
  },
  divider: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#E0E0E0',
    marginLeft: 72,
  },
  cancelButton: {
    paddingVertical: 14,
    alignItems: 'center',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E0E0E0',
  },
  cancelText: {
    fontSize: 16,
    color: TEXT_SECONDARY,
    fontWeight: '500',
  },
});
