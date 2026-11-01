// swift-tools-version:5.7
// How to run (from this folder):  swift run PeerovaDemo server [seconds]
//                                 swift run PeerovaDemo client <TICKET|CODE> [seconds]
// Uses the Peerova package two folders up; the native library is found like in that package
// (PEEROVA_LIB_DIR, or the SDK's library folder).
import PackageDescription

let package = Package(
    name: "PeerovaDemo",
    platforms: [.macOS(.v10_15)],
    // A local package's identity is its folder name: "swift" (bindings/swift).
    dependencies: [.package(path: "../..")],
    targets: [
        .executableTarget(name: "PeerovaDemo", dependencies: [.product(name: "Peerova", package: "swift")]),
    ]
)
