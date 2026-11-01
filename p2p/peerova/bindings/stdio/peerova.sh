# peerova.sh — bash (>= 4.1) helper library for the `peerova stdio` sidecar (protocol: PROTOCOL.md).
# Uses jq when installed, otherwise bash regexes (the sidecar emits flat, one-line JSON).
#
#   source peerova.sh
#   pv_start srv server '{"port":0,"upnp":false}'   # node named "srv"; sets PV_TICKET
#   pv_start cli client "$PV_TICKET"                 # ticket, 6-character connect code, or @file
#   pv_event cli 10 && [[ $EV == connected ]] && pv_send cli "$EV_PEER" 0 true "hello"
#   pv_event srv 5   # -> EV=connected ..., then EV=message EV_PEER=... EV_CHANNEL=0 EV_RELIABLE=true EV_TEXT=hello
#   pv_stop cli; pv_stop srv
#
# Each node is one sidecar process, addressed by a NAME of your choice (letters, digits, _).
# Binary: $PEEROVA_BIN, else ../../target/release/peerova next to this file, else `peerova` on PATH.

_pv_here=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

# pv_bin — prints the sidecar binary path that pv_start will run.
pv_bin() {
  if [[ -n ${PEEROVA_BIN:-} ]]; then echo "$PEEROVA_BIN"
  elif [[ -x $_pv_here/../../target/release/peerova ]]; then echo "$_pv_here/../../target/release/peerova"
  else echo peerova; fi
}

# pv_b64e — base64url-encodes stdin (no padding) to stdout.   printf 'hi' | pv_b64e   -> aGk
pv_b64e() { base64 | tr -d '\n=' | tr '+/' '-_'; }
# pv_b64d B64URL — decodes a base64url string (padding optional) to raw bytes on stdout.
pv_b64d() {
  local s=${1//-/+}; s=${s//_//}
  case $(( ${#s} % 4 )) in 2) s+="==" ;; 3) s+="=" ;; esac
  printf '%s' "$s" | base64 -d
}

# pv_start NAME server ['<json config>']  |  pv_start NAME client TICKET|CODE|@file ['<json config>']
# Spawns the sidecar and waits (up to 10 s) for its "ready" line.
# Server: sets PV_TICKET to the server ticket. Client: then wait for EV=connected / connect_failed via pv_event.
# Returns 0 on success; on failure prints the sidecar's error (stderr) and returns 1.
pv_start() {
  local name=$1; shift
  local dir; dir=$(mktemp -d "${TMPDIR:-/tmp}/peerova-sh.XXXXXX") || return 1
  mkfifo "$dir/in" "$dir/out" || return 1
  "$(pv_bin)" stdio "$@" <"$dir/in" >"$dir/out" 2>"$dir/err" &
  local pid=$! fin fout
  exec {fin}>"$dir/in" {fout}<"$dir/out"
  declare -g "PV_${name}_PID=$pid" "PV_${name}_IN=$fin" "PV_${name}_OUT=$fout" "PV_${name}_DIR=$dir"
  pv_event "$name" 10 || { cat "$dir/err" >&2; pv_stop "$name"; return 1; }
  if [[ $EV != ready ]]; then echo "peerova: unexpected first event '$EV'" >&2; pv_stop "$name"; return 1; fi
  [[ -n $EV_TICKET ]] && PV_TICKET=$EV_TICKET   # client "ready" has no ticket: keep PV_TICKET as it was
  return 0
}

# _pv_field KEY — (internal) prints the value of "KEY" in $EV_LINE (string, number or bool).
_pv_field() {
  if command -v jq >/dev/null; then
    jq -r --arg k "$1" '.[$k] // empty' <<<"$EV_LINE"
  elif [[ $EV_LINE =~ \"$1\":\"(([^\"\\]|\\.)*)\" ]]; then
    printf '%s' "${BASH_REMATCH[1]}"
  elif [[ $EV_LINE =~ \"$1\":([0-9a-z]+) ]]; then
    printf '%s' "${BASH_REMATCH[1]}"
  fi
}

# pv_event NAME [timeout_s]  — reads the next event line (default timeout 5 s, fractions allowed).
# Returns 1 on timeout or when the sidecar has exited (EOF).
# Sets EV (ready|connected|message|disconnected|connect_failed|error), EV_PEER, EV_CHANNEL, EV_RELIABLE,
# EV_DATA (base64url), EV_TEXT (decoded data, NUL bytes dropped), EV_REASON (reason, or the error message),
# EV_TICKET, EV_LINE (raw JSON).
pv_event() {
  local fd="PV_${1}_OUT"
  EV= EV_PEER= EV_CHANNEL= EV_RELIABLE= EV_DATA= EV_TEXT= EV_REASON= EV_TICKET=
  IFS= read -r -t "${2:-5}" -u "${!fd}" EV_LINE || return 1
  if command -v jq >/dev/null; then
    # \x1f separator: tabs would collapse empty fields (IFS whitespace rule)
    IFS=$'\x1f' read -r EV EV_PEER EV_CHANNEL EV_RELIABLE EV_DATA EV_TICKET < <(
      jq -r '[.event, .peer, .channel, .reliable, .data, .ticket] | map(if . == null then "" else tostring end) | join("\u001f")' <<<"$EV_LINE")
    EV_REASON=$(jq -r '.reason // .message // empty' <<<"$EV_LINE")
  else
    EV=$(_pv_field event); EV_PEER=$(_pv_field peer); EV_CHANNEL=$(_pv_field channel)
    EV_RELIABLE=$(_pv_field reliable); EV_DATA=$(_pv_field data); EV_TICKET=$(_pv_field ticket)
    EV_REASON=$(_pv_field reason); [[ -z $EV_REASON ]] && EV_REASON=$(_pv_field message)
  fi
  [[ -n $EV_DATA ]] && EV_TEXT=$(pv_b64d "$EV_DATA" | tr -d '\0')
  return 0
}

# pv_send NAME PEER CHANNEL RELIABLE TEXT — sends TEXT to PEER on CHANNEL (0..255).
# RELIABLE is true (ordered, retransmitted; chat/RPC/files) or false (datagram, may be lost; max 1180 bytes).
# Asynchronous: a rejected send shows up later as an EV=error event.
pv_send() { pv_send_b64 "$1" "$2" "$3" "$4" "$(printf '%s' "$5" | pv_b64e)"; }
# pv_send_b64 NAME PEER CHANNEL RELIABLE B64URL — same as pv_send for binary payloads (already base64url).
pv_send_b64() {
  local fd="PV_${1}_IN"
  printf '{"cmd":"send","peer":%d,"channel":%d,"reliable":%s,"data":"%s"}\n' "$2" "$3" "$4" "$5" >&"${!fd}"
}

# pv_disconnect NAME PEER — closes the connection to PEER (both sides then get EV=disconnected).
pv_disconnect() { local fd="PV_${1}_IN"; printf '{"cmd":"disconnect","peer":%d}\n' "$2" >&"${!fd}"; }

# pv_stop NAME — sends quit, waits for the sidecar to exit, closes fds and removes the fifos. Safe to call twice.
pv_stop() {
  local fin="PV_${1}_IN" fout="PV_${1}_OUT" pid="PV_${1}_PID" dir="PV_${1}_DIR"
  [[ -n ${!pid:-} ]] || return 0
  printf '{"cmd":"quit"}\n' >&"${!fin}" 2>/dev/null
  local i=${!fin}; exec {i}>&- 2>/dev/null
  local o=${!fout}; exec {o}<&- 2>/dev/null
  wait "${!pid}" 2>/dev/null
  rm -rf "${!dir}"
  unset "PV_${1}_PID" "PV_${1}_IN" "PV_${1}_OUT" "PV_${1}_DIR"
}
