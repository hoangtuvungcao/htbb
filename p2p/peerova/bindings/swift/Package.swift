// swift-tools-version:5.7
// Peerova Swift package — wraps the C ABI (Sources/CPeerova/include/peerova.h).
//
// Linking: the package links `-lpeerova`. Point the linker at the native lib with
//   PEEROVA_LIB_DIR=/abs/dir/with/libpeerova swift build      (dir is also added as rpath)
// When PEEROVA_LIB_DIR is unset and ../../target/release exists (inside the repo), that
// directory is used. Outside the repo / on iOS, prefer the XCFramework binaryTarget
// described in README.md (no unsafe flags needed, so the package can be a dependency).
import PackageDescription
import Foundation

let packageDir = URL(fileURLWithPath: #filePath).deletingLastPathComponent()
let libDir: String? = {
    if let d = ProcessInfo.processInfo.environment["PEEROVA_LIB_DIR"], !d.isEmpty { return d }
    let repo = packageDir.appendingPathComponent("../../target/release").standardizedFileURL.path
    return FileManager.default.fileExists(atPath: repo) ? repo : nil
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
        // C module exposing peerova.h (copy kept in sync by bindings/sync-headers.sh).
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
