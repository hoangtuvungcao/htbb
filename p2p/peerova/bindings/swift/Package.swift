// swift-tools-version:5.7
// Peerova Swift package — wraps the Peerova C API (Sources/CPeerova/include/peerova.h).
//
// Linking: the package links `-lpeerova`. Point the linker at the native library with
//   PEEROVA_LIB_DIR=/abs/dir/with/libpeerova swift build      (the dir is also added as rpath)
// When PEEROVA_LIB_DIR is unset, the SDK's library folder ../../lib/linux-x86_64 is used if it
// exists, else ../../target/release (a source checkout). For apps / iOS, prefer the XCFramework binaryTarget described in
// README.md (no unsafe flags needed, so the package can be consumed as a dependency).
import PackageDescription
import Foundation

let packageDir = URL(fileURLWithPath: #filePath).deletingLastPathComponent()
let libDir: String? = {
    if let d = ProcessInfo.processInfo.environment["PEEROVA_LIB_DIR"], !d.isEmpty { return d }
    for rel in ["../../lib/linux-x86_64", "../../target/release"] {
        let dir = packageDir.appendingPathComponent(rel).standardizedFileURL.path
        if FileManager.default.fileExists(atPath: dir) { return dir }
    }
    return nil
}()

var cLinker: [LinkerSetting] = [.linkedLibrary("peerova")]
if let d = libDir {
    // unsafeFlags are only allowed for root/local packages — fine for development.
    cLinker.append(.unsafeFlags(["-L", d, "-Xlinker", "-rpath", "-Xlinker", d]))
}

let package = Package(
    name: "Peerova",
    platforms: [.macOS(.v10_15), .iOS(.v13)],
    products: [
        .library(name: "Peerova", targets: ["Peerova"]),
    ],
    targets: [
        // C module exposing peerova.h (identical copy of the SDK's include/peerova.h).
        .target(
            name: "CPeerova",
            path: "Sources/CPeerova",
            publicHeadersPath: "include",
            linkerSettings: cLinker
        ),
        .target(name: "Peerova", dependencies: ["CPeerova"], path: "Sources/Peerova"),
        .testTarget(name: "PeerovaTests", dependencies: ["Peerova"], path: "Tests/PeerovaTests"),
    ]
)
