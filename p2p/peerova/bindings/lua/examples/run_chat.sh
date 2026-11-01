#!/bin/sh
# How to run: examples/run_chat.sh server | examples/run_chat.sh client <ticket|code> [seconds]
# Runs chat.lua with `luajit`, or (if no luajit binary is installed) with a tiny host embedding libluajit.
set -e
here=$(cd "$(dirname "$0")" && pwd)
if command -v luajit >/dev/null 2>&1; then exec luajit "$here/chat.lua" "$@"; fi
host=${TMPDIR:-/tmp}/peerova_luajit_host
[ -x "$host" ] || cc -O1 -o "$host" "$here/../test/luajit_host.c" -l:libluajit-5.1.so.2 -lm -ldl
exec "$host" "$here/chat.lua" "$@"
