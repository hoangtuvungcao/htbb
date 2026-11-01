#!/usr/bin/env bash
# Runs the Peerova Godot addon inside a real Godot 4 .NET engine, headless (Linux).
#
#   GODOT=/path/to/Godot_v4.x-stable_mono_linux.x86_64 bindings/godot/test/test.sh
#
# Needs: the Godot 4.2+ .NET build, the .NET 8 SDK (dotnet) and libpeerova.so
# (PEEROVA_LIB, default ../../../target/release/libpeerova.so = `cargo build --release -p peerova-ffi`).
# Assembles a throw-away project in a temp folder (test/ + addons/peerova + examples/net_demo + the native
# library in addons/peerova/bin/linux/), builds it with dotnet, imports it, then:
#   1. roundtrip_cs.tscn  — C#: host + client PeerovaNode, reliable order, unreliable both ways, reply, disconnect
#   2. roundtrip_gd.tscn  — the same flow from GDScript
#   3. examples/net_demo  — NetDemo.cs as host, NetDemo.cs + net_demo.gd as two clients (separate processes)
# Exit code 0 = all passed.
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
godot_dir="$(cd "$here/.." && pwd)"
GODOT="${GODOT:-godot}"
LIB="${PEEROVA_LIB:-$godot_dir/../../target/release/libpeerova.so}"
[ -f "$LIB" ] || { echo "libpeerova.so not found: $LIB (set PEEROVA_LIB)"; exit 2; }
command -v "$GODOT" >/dev/null || [ -x "$GODOT" ] || { echo "Godot not found: $GODOT (set GODOT)"; exit 2; }
GODOT="$(command -v "$GODOT" || echo "$GODOT")"

ver="$("$GODOT" --headless --version | tail -1)"            # e.g. 4.7.2.stable.mono.official.ed1daf0bf
case "$ver" in *mono*) ;; *) echo "not a .NET (mono) Godot build: $ver"; exit 2;; esac
sdk_ver="$(echo "$ver" | cut -d. -f1-3)"
case "$sdk_ver" in *.stable|*.*.*[!0-9]*) sdk_ver="$(echo "$ver" | cut -d. -f1-2).0";; esac
nupkgs="$(dirname "$(readlink -f "$GODOT")")/GodotSharp/Tools/nupkgs"

work="${PEEROVA_GODOT_WORK:-$(mktemp -d -t peerova-godot-XXXXXX)}"
proj="$work/project"
rm -rf "$proj"; mkdir -p "$proj/addons" "$proj/examples"
cp "$here"/project.godot "$here"/*.csproj "$here"/*.tscn "$here"/*.cs "$here"/*.gd "$proj/"
cp -r "$godot_dir/addons/peerova" "$proj/addons/"
cp -r "$godot_dir/examples/net_demo" "$proj/examples/"
mkdir -p "$proj/addons/peerova/bin/linux"
cp "$LIB" "$proj/addons/peerova/bin/linux/libpeerova.so"
sed -i "s|Godot.NET.Sdk/[0-9.]*|Godot.NET.Sdk/$sdk_ver|" "$proj/PeerovaGodotTest.csproj"
if [ -d "$nupkgs" ]; then
  cat > "$proj/nuget.config" <<EOF
<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <packageSources>
    <add key="godot-local" value="$nupkgs" />
    <add key="nuget.org" value="https://api.nuget.org/v3/index.json" />
  </packageSources>
</configuration>
EOF
fi
echo "== Godot $ver, Godot.NET.Sdk $sdk_ver, project $proj"

dotnet build "$proj/PeerovaGodotTest.csproj" -nologo -v q -clp:NoSummary > "$work/build.log" 2>&1 \
  || { cat "$work/build.log"; echo "dotnet build FAILED"; exit 1; }
grep -E "warning|error" "$work/build.log" | sort -u || true
"$GODOT" --headless --path "$proj" --import > "$work/import.log" 2>&1 || true

fail=0
run_scene() { # name scene
  echo "== $1"
  if timeout 60 "$GODOT" --headless --path "$proj" "$2" > "$work/$1.log" 2>&1; then
    grep -E "PASS" "$work/$1.log"
  else
    echo "-- $1 FAILED (exit $?):"; cat "$work/$1.log"; fail=1
  fi
}
run_scene roundtrip_cs res://roundtrip_cs.tscn
run_scene roundtrip_gd res://roundtrip_gd.tscn

echo "== net_demo (C# host, C# client, GDScript client)"
# --max-fps + --quit-after (frames) make every process quit cleanly (PeerovaNode._ExitTree closes the node),
# so the host also sees both clients leave.
demo() { "$GODOT" --headless --max-fps 60 --quit-after "$1" --path "$proj" "${@:2}"; }
demo 900 res://net_demo_cs.tscn > "$work/demo_host.log" 2>&1 &
host_pid=$!
trap 'kill $host_pid 2>/dev/null || true' EXIT
ticket=""
for _ in $(seq 100); do
  ticket="$(grep -o 'pvt_[A-Za-z0-9_-]*' "$work/demo_host.log" | head -1 || true)"
  [ -n "$ticket" ] && break; sleep 0.2
done
if [ -z "$ticket" ]; then echo "-- demo host printed no ticket:"; cat "$work/demo_host.log"; fail=1; else
  demo 360 res://net_demo_cs.tscn -- client "$ticket" --auto > "$work/demo_cs.log" 2>&1 &
  c1=$!
  sleep 1
  demo 240 res://net_demo_gd.tscn -- client "$ticket" --auto > "$work/demo_gd.log" 2>&1 &
  c2=$!
  wait $c1 $c2 || true
  wait $host_pid 2>/dev/null || true
  ok=1
  grep -q "hello from Godot" "$work/demo_host.log" || ok=0
  grep -q "hello from GDScript" "$work/demo_host.log" || ok=0
  grep -q "hello from GDScript" "$work/demo_cs.log" || ok=0   # forwarded by the host to the other client
  grep -q "connected" "$work/demo_gd.log" || ok=0
  [ "$(grep -c " left" "$work/demo_host.log")" = 2 ] || ok=0
  if [ $ok = 1 ]; then echo "net_demo PASS: host saw both clients' chat + both departures, chat forwarded GDScript -> C# client"
  else echo "-- net_demo FAILED"; for f in demo_host demo_cs demo_gd; do echo "--- $f"; cat "$work/$f.log"; done; fail=1; fi
fi

[ -z "${PEEROVA_GODOT_WORK:-}" ] && [ $fail = 0 ] && rm -rf "$work"
[ $fail = 0 ] && echo "ALL GODOT TESTS PASSED" || echo "GODOT TESTS FAILED (logs in $work)"
exit $fail
