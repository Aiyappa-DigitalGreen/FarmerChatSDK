// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "FarmerChatSDK",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "FarmerChatSDK", targets: ["FarmerChatSDK"])
    ],
    targets: [
        .target(
            name: "FarmerChatSDK",
            path: "Sources/FarmerChatSDK",
            swiftSettings: [
                .enableExperimentalFeature("StrictConcurrency")
            ]
        )
    ]
)
