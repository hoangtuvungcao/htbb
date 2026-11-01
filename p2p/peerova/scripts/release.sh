#!/usr/bin/env bash
# Build release artifacts for Linux (static), Windows, with SDK libs, notices and SHA256 checksums.
#   PEEROVA_AUTHORITY_PUBKEY=<b64> PEEROVA_LICENSE_SERVER=https://your.site scripts/release.sh
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd); cd "$ROOT"
VER=$(grep -m1 '^version' Cargo.toml | cut -d'"' -f2)
OUT=dist/peerova-$VER; rm -rf "$OUT"; mkdir -p "$OUT"
[ -n "${PEEROVA_AUTHORITY_PUBKEY:-}" ] || echo "⚠ PEEROVA_AUTHORITY_PUBKEY chưa đặt: bản build sẽ cần --authority-pubkey khi dùng license"
cargo test --release --workspace -q
cargo build --release --target x86_64-unknown-linux-musl -p peerova-cli
cargo build --release -p peerova-ffi
cargo build --release --target x86_64-pc-windows-gnu -p peerova-cli -p peerova-ffi
mkdir -p "$OUT/linux-x64" "$OUT/windows-x64" "$OUT/sdk/include"
cp target/x86_64-unknown-linux-musl/release/peerova "$OUT/linux-x64/"
cp target/release/libpeerova.so target/release/libpeerova.a "$OUT/linux-x64/"
cp target/x86_64-pc-windows-gnu/release/peerova.exe target/x86_64-pc-windows-gnu/release/peerova.dll "$OUT/windows-x64/"
cp crates/peerova-ffi/include/peerova.h "$OUT/sdk/include/"
cp -r bindings "$OUT/sdk/bindings"
find "$OUT/sdk/bindings" \( -name bin -o -name obj -o -name build -o -name '*.key' \) -prune -exec rm -rf {} +
cp deploy/* "$OUT/"; cp README.md THIRD_PARTY_NOTICES.txt "$OUT/"
cp deploy/install-windows.ps1 deploy/peerova.example.json "$OUT/windows-x64/"
( cd dist && tar czf "peerova-$VER.tar.gz" "peerova-$VER" && sha256sum "peerova-$VER.tar.gz" > "peerova-$VER.tar.gz.sha256" )
( cd "$OUT" && find . -type f ! -name SHA256SUMS -exec sha256sum {} + | sort -k2 > SHA256SUMS )
echo "✔ $OUT  ($(du -sh "$OUT" | cut -f1))  ·  dist/peerova-$VER.tar.gz"
echo "Bước tiếp theo khi phát hành công khai: ký số (Windows Authenticode, minisign/cosign cho tarball)."
