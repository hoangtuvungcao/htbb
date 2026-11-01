#!/usr/bin/env bash
# Production release: all platforms, no machine paths inside binaries, then customer packages.
#   scripts/build-release.sh
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd); cd "$ROOT"
# Build configuration (URLs, key files): scripts/release.env. Environment overrides it.
while IFS='=' read -r k v; do
  [[ -z "$k" || "$k" == \#* ]] && continue
  [[ -n "${!k:-}" ]] || export "$k=$(eval echo "$v")"
done < scripts/release.env
export PEEROVA_AUTHORITY_PUBKEY=${PEEROVA_AUTHORITY_PUBKEY:-$(cat "$PEEROVA_AUTHORITY_PUBKEY_FILE")}
echo "build: license=$PEEROVA_LICENSE_SERVER relay=$PEEROVA_RELAY_URL"
# Release-signing key (separate from the license authority). Create once:
#   node web/tools/sign-release.mjs --new ~/.config/peerova/release.seed > ~/.config/peerova/release.pub
export PEEROVA_RELEASE_PUBKEY=${PEEROVA_RELEASE_PUBKEY:-$(cat "$PEEROVA_RELEASE_PUBKEY_FILE")}
# Strip build-machine paths (user name, source tree, toolchains) from panic/location strings.
export RUSTFLAGS="--remap-path-prefix=$ROOT/crates=src --remap-path-prefix=$ROOT=. --remap-path-prefix=$HOME/.cargo=cargo --remap-path-prefix=$HOME/.rustup=rust"
cargo build --release -p peerova-ffi -p peerova-cli
cargo build --release --target x86_64-pc-windows-gnu -p peerova-cli -p peerova-ffi
# ring (TLS for the fallback route) has a little freestanding C: plain gcc builds it for musl.
CC_x86_64_unknown_linux_musl=${CC_x86_64_unknown_linux_musl:-gcc} cargo build --release --target x86_64-unknown-linux-musl -p peerova-cli
CARGO_TARGET_X86_64_WIN7_WINDOWS_GNU_LINKER=x86_64-w64-mingw32-gcc \
  cargo +nightly build --release -Zbuild-std=std,panic_abort --target x86_64-win7-windows-gnu -p peerova-cli -p peerova-ffi
leak=0
for f in target/release/libpeerova.so target/x86_64-unknown-linux-musl/release/peerova \
         target/x86_64-pc-windows-gnu/release/peerova.{exe,dll} target/x86_64-win7-windows-gnu/release/peerova.{exe,dll}; do
  n=$(strings "$f" | grep -cE "$HOME|$(whoami)|tunnel_p2p" || true)
  echo "$f: $n đường dẫn máy build"; [ "$n" -eq 0 ] || leak=1
done
[ $leak -eq 0 ] || { echo "LỘ đường dẫn máy build — dừng"; exit 1; }
python3 scripts/package-downloads.py
node web/tools/sign-release.mjs ~/.config/peerova/release.seed web/public/downloads/SHA256SUMS.txt
# Self-check: the freshly built CLI must accept every package.
for f in web/public/downloads/*.zip web/public/downloads/*.tar.gz; do target/release/peerova verify "$f"; done
