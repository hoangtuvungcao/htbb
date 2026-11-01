# peerova for Godot 4

`addons/peerova/` is a Godot 4 addon built on the C# binding (`bindings/csharp/Peerova.cs`, copied as `addons/peerova/Peerova.cs`).

## Requirements
- Godot **4.2+ .NET edition** (the standard build has no C#). The project needs a C# solution
  (*Project → Tools → C# → Create C# solution*).
- Native library in `addons/peerova/bin/<os>/`:
  `bin/linux/libpeerova.so`, `bin/windows/peerova.dll`, `bin/macos/libpeerova.dylib`.

## Install
1. Copy `addons/peerova` into your project's `addons/`.
2. Put the native libs into `addons/peerova/bin/...` (see `bindings/README.md` for build commands).
3. Build the C# project once (*Build* button), then *Project Settings → Plugins → peerova → Enable*.
4. Add a **PeerovaNode** to the scene (it is a `[GlobalClass]`, so it appears in *Add Node*).

## Export
Exported games can't `dlopen` files inside the `.pck`. Copy the native lib **next to the exported executable**
(Windows: `peerova.dll` beside `game.exe`; Linux: `libpeerova.so` beside the binary; macOS: into `Contents/MacOS/` or `Contents/Frameworks/`).
`PeerovaNode.ConfigureNativeLibrary()` looks in `res://addons/peerova/bin/<os>/` (editor) and then the executable's folder;
otherwise the normal `DllImport` probing (`PATH`, `LD_LIBRARY_PATH`, rpath) applies, or set the `PEEROVA_LIB` env var.
Android/iOS C# export is experimental in Godot 4 and is **untested** with peerova.

## C# usage
```csharp
var net = GetNode<Peerova.GodotAddon.PeerovaNode>("PeerovaNode");
net.HostStarted += ticket => GD.Print("ticket: ", ticket);
net.Connected += peer => net.SendText(peer, "hello", 0, true);
net.Message += (peer, channel, data, reliable) => GD.Print(System.Text.Encoding.UTF8.GetString(data));
net.Host();   // or net.Join(ticket)
```
You can also use the low-level API directly (`Peerova.Server.Start`, `Peerova.Client.Connect`, `Poll`, `Send`) —
call `PeerovaNode.ConfigureNativeLibrary()` first so the native lib is found.

## GDScript users
GDScript cannot call the C ABI directly. Options:

1. **Call through the C# node (works today).** The project must be the .NET edition, but your game code can be
   100% GDScript — C# signals and methods are visible from GDScript under their C# names:
   ```gdscript
   @onready var net = $PeerovaNode
   func _ready():
       net.connect("HostStarted", func(ticket): print("ticket: ", ticket))
       net.connect("Connected", func(peer): net.SendText(peer, "hi", 0, true))
       net.connect("Message", func(peer, channel, data: PackedByteArray, reliable): print(data.get_string_from_utf8()))
       net.connect("Disconnected", func(peer): print("left ", peer))
       net.connect("ConnectFailed", func(reason): push_error(reason))
       net.Host()          # or net.Join(ticket)
   ```
   Methods callable from GDScript: `Host()`, `Join(ticket)`, `JoinWithToken(ticket, token)`, `SendBytes(peer, bytes, channel, reliable)`,
   `SendText(peer, text, channel, reliable)`, `Broadcast(bytes, channel, reliable)`, `DisconnectPeer(peer)`, `Shutdown()`,
   `GetTicket()`, `GetPeerCount()`, `GetPeers()`.
2. **GDExtension (future).** A small C/C++ (godot-cpp) or Rust (gdext) GDExtension wrapping `peerova.h` would work in the
   standard (non-.NET) editor, on web-less platforms incl. Android/iOS, with the same signals. Not implemented yet.
3. **MultiplayerPeerExtension (future).** Implementing Godot's `MultiplayerPeer` on top of peerova would make
   `@rpc` / `MultiplayerSynchronizer` work transparently. Not implemented yet.

## Testing status
Not run inside Godot (no Godot here). `compile-check/` compiles `addons/peerova/*.cs` against a stub of the Godot API
(including the `SignalName` class Godot's source generator would emit) and runs a host + join `PeerovaNode` through `_Process()`:
```
cd compile-check && dotnet run -c Release     # prints PASS
```
