# com.peerova.sdk (Unity)

Direct P2P networking (no relay, E2E encrypted) for Unity 2021.3+ (.NET Standard 2.1, Mono or IL2CPP).

## Install
1. Copy this folder into `Packages/com.peerova.sdk` of your project, or *Package Manager → + → Add package from disk…* and pick `package.json`.
   (Installing from a git URL makes the package immutable; Unity then warns about missing `.meta` files — use a local/embedded copy, or commit the `.meta` files Unity generates.)
2. Put the native libraries into `Plugins/` — see `Plugins/README.md`.
3. Optional: *Package Manager → peerova SDK → Samples → Chat → Import*.

## Use
```csharp
using Peerova.Unity;
var net = gameObject.AddComponent<PeerovaBehaviour>();
net.OnHostStarted.AddListener(ticket => Debug.Log("share this: " + ticket));
net.OnConnected.AddListener(peer => net.Send(peer, System.Text.Encoding.UTF8.GetBytes("hi")));
net.OnMessage.AddListener((peer, channel, data, reliable) => { /* main thread */ });
net.Host();            // or net.Join(ticket);
```
Low level API (no MonoBehaviour): `Peerova.Server.Start(json)`, `Peerova.Client.Connect(ticket)`, `Poll(out ev)`, `Send(peer, span, channel, reliable)`.
Note: `Peerova.Event` / `Peerova.EventType` share names with `UnityEngine.Event` / `UnityEngine.EventType`; qualify them
(`Peerova.Event`) or add `using PeerovaEvent = Peerova.Event;` in files that also import `UnityEngine`.

`Runtime/Peerova.cs` is a copy of `bindings/csharp/Peerova.cs` — keep them identical (`bindings/sync-csharp.sh`).

## Testing status
Not run inside Unity (no Unity here). `Tests~/StubCheck` compiles `Runtime/` + `Samples~/Chat` against a stub
`UnityEngine` for netstandard2.1 and net8.0 and drives two `PeerovaBehaviour`s (host + join) through `Update()`:
```
cd Tests~/StubCheck && dotnet build -c Release && cp ../../../../../target/release/libpeerova.so bin/Release/net8.0/ && dotnet bin/Release/net8.0/StubCheck.dll
```
