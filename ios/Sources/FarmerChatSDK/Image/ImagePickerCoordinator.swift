import SwiftUI
import PhotosUI
import UIKit

// MARK: - ImagePickerView

/// Unified SwiftUI wrapper that presents either `UIImagePickerController` (camera)
/// or `PHPickerViewController` (photo library) depending on `sourceType`.
struct ImagePickerView: UIViewControllerRepresentable {

    enum SourceType {
        case camera
        case photoLibrary
    }

    let sourceType: SourceType
    let onImageSelected: (UIImage) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        switch sourceType {
        case .camera:
            let picker = UIImagePickerController()
            picker.sourceType = .camera
            picker.cameraCaptureMode = .photo
            picker.allowsEditing = false
            picker.delegate = context.coordinator
            return picker

        case .photoLibrary:
            var config = PHPickerConfiguration()
            config.selectionLimit = 1
            config.filter = .images
            let picker = PHPickerViewController(configuration: config)
            picker.delegate = context.coordinator
            return picker
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onImageSelected: onImageSelected)
    }

    // MARK: - Coordinator

    final class Coordinator: NSObject,
                             UIImagePickerControllerDelegate,
                             UINavigationControllerDelegate,
                             PHPickerViewControllerDelegate {

        private let onImageSelected: (UIImage) -> Void

        init(onImageSelected: @escaping (UIImage) -> Void) {
            self.onImageSelected = onImageSelected
        }

        // MARK: UIImagePickerControllerDelegate

        func imagePickerController(
            _ picker: UIImagePickerController,
            didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
        ) {
            picker.dismiss(animated: true)
            guard let image = info[.originalImage] as? UIImage else { return }
            onImageSelected(image)
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            picker.dismiss(animated: true)
        }

        // MARK: PHPickerViewControllerDelegate

        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            picker.dismiss(animated: true)

            guard let result = results.first else { return }

            result.itemProvider.loadObject(ofClass: UIImage.self) { [weak self] item, _ in
                guard let image = item as? UIImage else { return }
                DispatchQueue.main.async {
                    self?.onImageSelected(image)
                }
            }
        }
    }
}
