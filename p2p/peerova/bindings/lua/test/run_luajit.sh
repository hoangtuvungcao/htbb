#!/bin/sh
# Runs test_peerova.lua under LuaJIT: uses `luajit` if installed, else builds luajit_host.c against libluajit.
set -e
cd "$(dirname "$0")"
if command -v luajit >/dev/null 2>&1; then exec luajit test_peerova.lua; fi
OUT=${TMPDIR:-/tmp}/peerova_luajit_host
cc -O1 -o "$OUT" luajit_host.c -l:libluajit-5.1.so.2 -lm -ldl
exec "$OUT" test_peerova.lua
