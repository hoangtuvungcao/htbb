# peerova for Kotlin / Android

Kotlin API (`dev.peerova.kt`) over the Java JNI binding in [`../java`](../java) (`dev.peerova.Peerova` +
`peerova_jni.c`). Same code runs on the JVM (desktop/server Kotlin) and on Android (API 21+).

```
kotlin-android/
├── test.sh                         JVM test (kotlinc + JDK)          → "kotlin PASS: ..."
├── build-android-libs.sh           cargo-ndk → jniLibs/<abi>/libpeerova.so
├── test/PeerovaKtTest.kt
└── peerova/                        Android library module (copy or include it)
    ├── build.gradle.kts            AGP + CMake + jniLibs + Java sources from ../../java/src
    ├── consumer-rules.pro          R8 keep rules for the JNI-constructed Event class
    └── src/main/
        ├── AndroidManifest.xml     INTERNET permission
        ├── kotlin/dev/peerova/kt/Peerova.kt       PeerovaServer / PeerovaClient / PeerovaEvent
        ├── kotlin/dev/peerova/kt/PeerovaFlow.kt   optional coroutines: eventFlow(), startAsync()
        ├── cpp/CMakeLists.txt      builds libpeerova_jni.so per ABI, links prebuilt libpeerova.so
        └── jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/libpeerova.so   (you provide; see below)
```

## API

```kotlin
import dev.peerova.kt.*

PeerovaServer.start(mapOf("port" to 47000, "key_file" to "$filesDir/server.key")).use { srv ->  // blocks <= ~3 s
    println(srv.ticket)                                   // pvt_...
    val loop = srv.startPolling(ContextCompat.getMainExecutor(context)) { ev ->   // listener on UI thread
        when (ev) {
            is PeerovaEvent.Connected     -> srv.send(ev.peer, "welcome")
            is PeerovaEvent.Message       -> Log.d("p2p", "${ev.peer} ch${ev.channel}: ${ev.text}")
            is PeerovaEvent.Disconnected  -> Log.d("p2p", "left: ${ev.reason}")
            is PeerovaEvent.ConnectFailed -> {}
        }
    }
    // ... srv.close() (via use{}) stops the loop first, then frees the node
}

PeerovaClient.connect(ticket).use { cli ->
    val ev = cli.poll(10_000)                              // blocking poll, null on timeout
    if (ev is PeerovaEvent.Connected) {
        cli.send(ev.peer, "hello", channel = 0)                       // reliable, ordered per channel
        cli.send(ev.peer, byteArrayOf(1, 2), channel = 1, reliable = false)   // datagram <= 1180 B
    }
    for (e in cli.events(idleMs = 1000)) println(e)        // Sequence until 1 s idle
}

// Coroutines (PeerovaFlow.kt):
lifecycleScope.launch {
    val srv = PeerovaServer.startAsync(mapOf("port" to 0))
    srv.eventFlow().collect { ev -> /* on Main */ }
}
```

Threading: `send`/`poll` are thread-safe; do not run two poll loops on one node; cancel a collecting
`eventFlow()` before `close()`. `PeerovaServer.start` blocks up to ~3 s — never on the main thread
(`startAsync` or a background thread). Event payloads are copied into `ByteArray`s.

## Android setup

1. Native core per ABI (needs `cargo install cargo-ndk` and the NDK):
   ```sh
   bindings/kotlin-android/build-android-libs.sh      # → peerova/src/main/jniLibs/<abi>/libpeerova.so
   ```
   Final APK layout (`lib/<abi>/`): `libpeerova.so` (Rust core, prebuilt) + `libpeerova_jni.so` (CMake, built by Gradle).
2. Include the module:
   ```kotlin
   // settings.gradle.kts
   include(":peerova")
   project(":peerova").projectDir = file("../peerova/bindings/kotlin-android/peerova")
   // app/build.gradle.kts
   dependencies { implementation(project(":peerova")) }
   ```
   Without the module: copy `../java/src/dev/peerova/Peerova.java` + the two `.kt` files into your app, add
   the `externalNativeBuild { cmake { path = file(".../CMakeLists.txt") } }` block and the `jniLibs` folder.
3. `INTERNET` permission is merged from the module manifest. R8 keep rules ship as `consumer-rules.pro`.

Status here: JVM test **PASS** (Kotlin 2.x bundled with Android Studio, JDK 21, kotlinx-coroutines 1.9.0);
`peerova_jni.c` **compile-checked** with NDK r28 clang for `aarch64-linux-android21`, `armv7a-linux-androideabi21`,
`x86_64-linux-android21`; `CMakeLists.txt` configured + built with the NDK toolchain (arm64-v8a) against a
stub `libpeerova.so` (verified `DT_NEEDED libpeerova.so`, no absolute path). Not done here: building `libpeerova.so` for Android (Rust Android targets not
installed), the Gradle/AGP build, and running on a device/emulator — **UNTESTED**.
