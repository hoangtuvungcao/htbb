# Peerova Unreal Engine plugin (UE 5.x)

Status: **skeleton, not compiled inside Unreal** — Unreal Engine is not installed on the machine
where this was written. The engine-agnostic core (`Public/PeerovaCore.h`) **is** compiled with
g++/clang++ and run (`compile-check/run.sh`: 100 reliable messages in order + 1 unreliable + reply).

```
Peerova/                                    copy this folder to <YourProject>/Plugins/Peerova
  Peerova.uplugin
  Source/Peerova/Peerova.Build.cs           per-platform lib paths, delay-load, RuntimeDependencies
  Source/Peerova/Peerova_APL.xml            Android: packages lib/<abi>/libpeerova.so, INTERNET permission
  Source/Peerova/Public/PeerovaCore.h       header-only C++17 RAII wrapper (no UE types)
  Source/Peerova/Public/PeerovaSubsystem.h  UPeerovaSubsystem (UGameInstanceSubsystem + FTickableGameObject)
  Source/Peerova/Public/PeerovaModule.h     module: loads peerova.dll / libpeerova.dylib
  Source/ThirdParty/PeerovaLibrary/         include/peerova.h + lib/<Platform>/ (see its README)
compile-check/                              g++ test of PeerovaCore.h (no Unreal needed)
```

## Install

1. Copy `Peerova/` to `<Project>/Plugins/Peerova/`.
2. Put the native libraries in `Source/ThirdParty/PeerovaLibrary/lib/<Platform>/` (table in
   `Source/ThirdParty/PeerovaLibrary/README.md`; Win64 needs the **MSVC** build → `peerova.dll` + `peerova.dll.lib`).
3. Regenerate project files, build, enable the plugin. C++ users: add `"Peerova"` to your module's dependencies.

## Blueprint

`Get Game Instance → Get Subsystem (Peerova Subsystem)`, then:

* host: bind **OnHostStarted** (Ticket) → share the ticket; call **Host** (Port 47000, ExtraConfigJson `{"license":"PVA-..."}` optional)
* join: call **Join** (Ticket); bind **OnConnected** (Peer)
* **OnMessage** (Peer, Data, Channel, bReliable) — use **Bytes To Utf8 String** for text
* **Send** / **SendString** (Peer, …, Channel 0..255, bReliable), **Disconnect**, **Shutdown**
* **OnDisconnected** (Peer, Reason), **OnConnectFailed** (Reason — also fired when Host fails)

## C++

```cpp
#include "PeerovaSubsystem.h"

UPeerovaSubsystem* Net = GetGameInstance()->GetSubsystem<UPeerovaSubsystem>();
Net->OnHostStarted.AddDynamic(this, &AMyActor::HandleHostStarted);   // UFUNCTION void(const FString&)
Net->OnMessage.AddDynamic(this, &AMyActor::HandleMessage);           // UFUNCTION void(int32, const TArray<uint8>&, int32, bool)
Net->Host(47000);                 // server start runs on a background thread (blocks ≤ ~3 s)
// client: Net->Join(Ticket); then in OnConnected: Net->SendString(Peer, TEXT("hello"), 0, true);
```

Without Unreal, use `PeerovaCore.h` directly:

```cpp
auto srv = peerova::Node::server(R"({"port":47000})");
auto cli = peerova::Node::client(srv.ticket());
peerova::Event ev;
while (cli.poll(ev, 100)) if (ev.type == peerova::EventType::Connected) cli.send(ev.peer, "hi");
```

## Notes

* Events are polled with a non-blocking `poll` from the subsystem's tick (game thread), so all
  delegates fire on the game thread; up to `MaxEventsPerTick` per frame. Ticking continues while paused.
* Server key (identity) defaults to `Saved/Peerova/server.key`; override with `"key_file"` in ExtraConfigJson.
* Peer ids are exposed as `int32` (Blueprint has no `uint32`).
* Dedicated servers: the same subsystem works; `Host()` on a headless build is fine.
* Without a license the host accepts 3 clients (dev mode).

## Compile-check (no Unreal)

```sh
cargo build --release -p peerova-ffi      # repo root
bindings/unreal/compile-check/run.sh
```
