#!/bin/sh
# Copy the canonical C# binding into the Unity package and the Godot addon
# (neither can reference files outside its own folder).
set -e
cd "$(dirname "$0")"
cp csharp/Peerova.cs unity/com.peerova.sdk/Runtime/Peerova.cs
cp csharp/Peerova.cs godot/addons/peerova/Peerova.cs
echo "synced csharp/Peerova.cs -> unity/com.peerova.sdk/Runtime/, godot/addons/peerova/"
