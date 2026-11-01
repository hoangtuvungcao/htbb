# Peerova for Swift (macOS / iOS / Linux)

Status: **UNTESTED** — no Swift toolchain on the machine where this was written. The code
targets Swift 5.7+ and only uses the C ABI in `Sources/CPeerova/include/peerova.h`
(a copy of `crates/peerova-ffi/include/peerova.h`; refresh with `bindings/sync-headers.sh`).

Layout:

```
Package.swift
Sources/CPeerova/include/{peerova.h,module.modulemap}   C module, links -lpeerova
Sources/CPeerova/shim.c                                 (SwiftPM needs one C source)
Sources/Peerova/Peerova.swift                           PeerovaServer / PeerovaClient / PeerovaEvent
Tests/PeerovaTests/PeerovaTests.swift                   100 reliable in order + 1 unreliable + reply
```

## Test (macOS / Linux)

```sh
cargo build --release -p peerova-ffi            # repo root → target/release/libpeerova.{dylib,so}
cd bindings/swift && swift test                 # uses ../../target/release automatically
# elsewhere: PEEROVA_LIB_DIR=/abs/dir/with/lib swift test
```

## Usage

```swift
import Peerova

let server = try await PeerovaServer.start(config: ["port": 47000])   // off the main thread
print(server.ticket)

let client = try PeerovaClient(ticket: server.ticket)
for await ev in client.events() {
    switch ev {
    case .connected(let peer):            try client.send("hello", to: peer, channel: 0, reliable: true)
    case .message(_, _, _, let data):     print(String(decoding: data, as: UTF8.self))
    case .disconnected, .connectFailed:   client.close()
    }
}
```

Synchronous loop (games): call `node.poll()` (non-blocking) once per frame until it returns `nil`.
`poll(timeout:)` holds the node's internal mutex while waiting, so keep timeouts short when
another thread also sends on that node.

## macOS app

Build both arches and merge: `cargo build --release -p peerova-ffi --target aarch64-apple-darwin`
and `--target x86_64-apple-darwin`, then `lipo -create -output libpeerova.dylib <arm64> <x86_64>`.
Copy it into `Contents/Frameworks/`, set the dylib id to `@rpath/libpeerova.dylib`
(`install_name_tool -id @rpath/libpeerova.dylib libpeerova.dylib`), add `@executable_path/../Frameworks`
to *Runpath Search Paths*, and codesign it (required for notarization). App Sandbox needs the
*Outgoing/Incoming Connections (Client/Server)* entitlements.

## iOS (static library via XCFramework)

iOS apps can't ship a loose dylib; use the static `libpeerova.a`:

```sh
cargo build --release -p peerova-ffi --target aarch64-apple-ios
cargo build --release -p peerova-ffi --target aarch64-apple-ios-sim
cargo build --release -p peerova-ffi --target x86_64-apple-ios          # optional: Intel simulator
lipo -create -output libpeerova-sim.a target/aarch64-apple-ios-sim/release/libpeerova.a target/x86_64-apple-ios/release/libpeerova.a
mkdir -p hdr && cp crates/peerova-ffi/include/peerova.h hdr/ && printf 'module CPeerova {\n  header "peerova.h"\n  export *\n}\n' > hdr/module.modulemap
xcodebuild -create-xcframework \
  -library target/aarch64-apple-ios/release/libpeerova.a -headers hdr \
  -library libpeerova-sim.a -headers hdr \
  -output CPeerova.xcframework
```

Then in `Package.swift` replace the `CPeerova` C target with a binary target (no unsafe flags,
so the package can be consumed as a remote dependency):

```swift
.binaryTarget(name: "CPeerova", path: "CPeerova.xcframework"),
// or .binaryTarget(name: "CPeerova", url: "https://…/CPeerova.xcframework.zip", checksum: "…")
```

and add to the `Peerova` target: `linkerSettings: [.linkedFramework("Security"), .linkedLibrary("resolv")]`
if the linker reports missing `Sec*` / `res_*` symbols. A macOS slice can be added to the same
XCFramework (`-library <universal macOS libpeerova.a> -headers hdr`).

The static lib is not a separate binary, so no extra signing is needed. Mobile radios: call
`peerova` networking from a background thread and expect the OS to suspend sockets when the app
is backgrounded — reconnect on `.disconnected`.
