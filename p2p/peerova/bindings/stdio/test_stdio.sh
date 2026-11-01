#!/usr/bin/env bash
# Round-trip test of the stdio sidecar from plain bash.
#   bindings/stdio/test_stdio.sh            (uses jq if installed)
#   PV_NO_JQ=1 bindings/stdio/test_stdio.sh (force the pure-bash JSON path)
set -u
cd "$(dirname "$0")"
if [[ -n ${PV_NO_JQ:-} ]]; then command() { [[ $1 == -v && $2 == jq ]] && return 1; builtin command "$@"; }; fi
source ./peerova.sh
N=100
fail() { echo "bash FAIL: $*"; pv_stop cli; pv_stop srv; exit 1; }

key=$(mktemp -d)/sh.key
t0=$(date +%s%N)
pv_start srv server "{\"port\":0,\"upnp\":false,\"key_file\":\"$key\"}" || fail "server start"
[[ $PV_TICKET == pvt_* ]] || fail "bad ticket '$PV_TICKET'"
pv_start cli client "$PV_TICKET" '{"timeout_ms":10000}' || fail "client start"

pv_event cli 10 && [[ $EV == connected ]] || fail "client not connected ($EV)"
peer=$EV_PEER
for ((i = 0; i < N; i++)); do pv_send cli "$peer" 1 true "msg $i"; done
pv_send cli "$peer" 2 false "realtime"

got=0 unrel=0 sp=
while pv_event srv 5; do
  case $EV in
    connected) sp=$EV_PEER ;;
    message)
      if [[ $EV_RELIABLE == true ]]; then
        [[ $EV_CHANNEL == 1 && $EV_TEXT == "msg $got" ]] || fail "out of order: got '$EV_TEXT' expected 'msg $got'"
        ((++got == N)) && break
      else
        ((unrel++))
      fi ;;
  esac
done
((got == N)) || fail "only $got/$N reliable messages"
# the datagram may land after the last reliable message (on loopback it is never lost)
while ((unrel == 0)) && pv_event srv 1; do [[ $EV == message && $EV_RELIABLE == false && $EV_TEXT == realtime ]] && ((unrel++)); done
((unrel == 1)) || fail "unreliable datagram not received"

# binary reply (00 ff inside) server -> client
pv_send_b64 srv "$sp" 0 true "$(printf 'pong\0\377' | pv_b64e)"
pv_event cli 5 && [[ $EV == message ]] || fail "no reply ($EV)"
[[ $(pv_b64d "$EV_DATA" | od -An -tx1 | tr -d ' \n') == 706f6e6700ff ]] || fail "reply bytes"

pv_disconnect srv "$sp"
pv_event cli 5 && [[ $EV == disconnected ]] || fail "no disconnect ($EV)"
pv_stop cli; pv_stop srv
mode=$(command -v jq >/dev/null && echo jq || echo pure-bash)
echo "bash PASS ($mode): $N reliable msgs in order, unreliable received=$unrel, binary reply ok, disconnect ok, $(( ($(date +%s%N) - t0) / 1000000 )) ms"
