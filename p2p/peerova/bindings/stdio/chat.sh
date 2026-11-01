#!/usr/bin/env bash
# Minimal P2P chat in pure bash over the peerova stdio sidecar.
#   ./chat.sh server [port]      # (key: $PEEROVA_KEY_FILE or ./server.key) prints a ticket; every line you type goes to all connected peers
#   ./chat.sh client <TICKET>    # every line you type goes to the server
set -u
source "$(dirname "$0")/peerova.sh"

case ${1:-} in
  server) pv_start me server "{\"port\":${2:-47000},\"key_file\":\"${PEEROVA_KEY_FILE:-server.key}\"}" || exit 1; echo "ticket: $PV_TICKET" ;;
  client) [[ -n ${2:-} ]] || { echo "usage: $0 client <TICKET>"; exit 2; }
          pv_start me client "$2" || exit 1; echo "connecting..." ;;
  *) echo "usage: $0 server [port] | client <TICKET>"; exit 2 ;;
esac
trap 'pv_stop me; exit 0' INT TERM

declare -A peers=()
while true; do
  # drain events (short timeout keeps the prompt responsive)
  while pv_event me 0.05; do
    case $EV in
      connected)      peers[$EV_PEER]=1; echo "* peer $EV_PEER connected" ;;
      disconnected)   unset "peers[$EV_PEER]"; echo "* peer $EV_PEER left: $EV_REASON" ;;
      connect_failed) echo "* connect failed: $EV_REASON"; pv_stop me; exit 1 ;;
      message)        echo "<$EV_PEER> $EV_TEXT" ;;
      error)          echo "* error: $EV_REASON" ;;
    esac
  done
  if IFS= read -r -t 0.2 line; then
    for p in "${!peers[@]}"; do pv_send me "$p" 0 true "$line"; done
  elif [[ $? -lt 128 ]]; then      # stdin EOF (not a timeout)
    pv_stop me; exit 0
  fi
done
