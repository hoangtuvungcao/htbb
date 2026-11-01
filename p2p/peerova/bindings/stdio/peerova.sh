# peerova.sh — bash (≥4.1) helper library for the `peerova stdio` sidecar.
# Shows that any language that can spawn a process and read/write lines can use peerova.
# Protocol: PROTOCOL.md. Uses jq when present, otherwise bash regexes (the sidecar emits flat, one-line JSON).
#
#   source bindings/stdio/peerova.sh
#   pv_start srv server '{"port":0,"upnp":false}'   # node name "srv"; sets PV_TICKET
#   pv_start cli client "$PV_TICKET"
#   pv_event cli 10 && [[ $EV == connected ]] && pv_send cli "$EV_PEER" 0 true "hello"
#   pv_event srv 5   # → EV=message EV_PEER=… EV_CHANNEL=0 EV_RELIABLE=true EV_TEXT=hello
#   pv_stop cli; pv_stop srv
#
# Binary: $PEEROVA_BIN, else <repo>/target/release/peerova, else `peerova` on PATH.

_pv_here=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
pv_bin() {
  if [[ -n ${PEEROVA_BIN:-} ]]; then echo "$PEEROVA_BIN"
  elif [[ -x $_pv_here/../../target/release/peerova ]]; then echo "$_pv_here/../../target/release/peerova"
  else echo peerova; fi
}

# base64url (no padding) <-> bytes
pv_b64e() { base64 | tr -d '\n=' | tr '+/' '-_'; }            # stdin bytes  -> stdout b64url
pv_b64d() {                                                     # $1 b64url    -> stdout bytes
  local s=${1//-/+}; s=${s//_//}
  case $(( ${#s} % 4 )) in 2) s+="==" ;; 3) s+="=" ;; esac
  printf '%s' "$s" | base64 -d
}

# pv_start NAME server ['<json>']  |  pv_start NAME client TICKET ['<json>']
# Server: blocks until ready and sets PV_TICKET. Returns non-zero on failure.
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
  PV_TICKET=$EV_TICKET
}

# _pv_field KEY  -> value of "KEY":… from $EV_LINE (string, number or bool)
_pv_field() {
  if command -v jq >/dev/null; then
    jq -r --arg k "$1" '.[$k] // empty' <<<"$EV_LINE"
  elif [[ $EV_LINE =~ \"$1\":\"(([^\"\\]|\\.)*)\" ]]; then
    printf '%s' "${BASH_REMATCH[1]}"
  elif [[ $EV_LINE =~ \"$1\":([0-9a-z]+) ]]; then
    printf '%s' "${BASH_REMATCH[1]}"
  fi
}

# pv_event NAME [timeout_s]  — reads the next event line. Returns 1 on timeout/EOF.
# Sets EV (ready|connected|message|disconnected|connect_failed|error), EV_PEER, EV_CHANNEL, EV_RELIABLE,
# EV_DATA (base64url), EV_TEXT (decoded data, NUL bytes dropped), EV_REASON, EV_TICKET, EV_LINE (raw JSON).
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

# pv_send NAME PEER CHANNEL RELIABLE(true|false) TEXT      (binary: pv_send_b64 with a b64url payload)
pv_send() { pv_send_b64 "$1" "$2" "$3" "$4" "$(printf '%s' "$5" | pv_b64e)"; }
pv_send_b64() {
  local fd="PV_${1}_IN"
  printf '{"cmd":"send","peer":%d,"channel":%d,"reliable":%s,"data":"%s"}\n' "$2" "$3" "$4" "$5" >&"${!fd}"
}

pv_disconnect() { local fd="PV_${1}_IN"; printf '{"cmd":"disconnect","peer":%d}\n' "$2" >&"${!fd}"; }

# pv_stop NAME — quit, wait for the sidecar, clean up fds and fifos.
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
