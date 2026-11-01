#!/usr/bin/env bash
# NAT lab: a mini Internet with REAL Linux NATs, no root needed (user namespaces).
#
#   scripts/nat-lab.sh                 # run all scenarios
#   scripts/nat-lab.sh cone sym        # one scenario: server-NAT client-NAT (cone|sym|cgn)
#   LAB_MODE=rdv scripts/nat-lab.sh    # website as the only rendezvous (no supernode/VPS)
#
# Topology (203.0.113.0/24 = "Internet", bridged):
#   supernode 203.0.113.10 (public, --supernode-mode)
#   core ── ispA ── routerA 198.51.100.2 ── NAT ── hostA 10.1.0.2   (peerova server, private ticket, no UPnP)
#   core ── ispB ── routerB 198.51.100.6 ── NAT ── hostB 10.2.0.2   (peerova client)
# NAT types: cone = Linux MASQUERADE (endpoint-independent mapping, port-restricted filtering)
#            sym  = MASQUERADE --random-fully (new random external port per destination)
set -uo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
BIN=${PEEROVA_BIN:-$ROOT/target/release/peerova}
if [ "${PEEROVA_LAB_INNER:-}" != 1 ]; then
  [ -x "$BIN" ] || (cd "$ROOT" && cargo build --release -p peerova-cli)
  export PEEROVA_LAB_INNER=1
  exec unshare --map-root-user --net --mount --fork bash "$0" "$@"
fi

WORK=$(mktemp -d /tmp/peerova-natlab.XXXX)
mount -t tmpfs tmpfs /run 2>/dev/null; mkdir -p /run/netns
ip link set lo up
ip link add br0 type bridge && ip addr add 203.0.113.1/24 dev br0 && ip link set br0 up
echo 1 > /proc/sys/net/ipv4/ip_forward   # the "Internet core" routes between ISPs

ns() { ip netns exec "$@"; }
attach_public() { # name ip  (directly on the core, e.g. the supernode)
  ip netns add "$1" 2>/dev/null
  ip link add "v-$1" type veth peer name wan netns "$1"
  ip link set "v-$1" master br0 up
  ns "$1" ip addr add "$2/24" dev wan; ns "$1" ip link set wan up; ns "$1" ip link set lo up
  ns "$1" ip route add default via 203.0.113.1 2>/dev/null
}
make_nat() { # router wanip(198.51.100.x) lanid host mode isp ispcore(203.0.113.x)
  # ISP router: plain routing hop between the home NAT and the Internet core (like a real ISP).
  attach_public "$6" "$7"
  ns "$6" sh -c 'echo 1 > /proc/sys/net/ipv4/ip_forward'
  ip netns add "$1" 2>/dev/null
  ns "$6" ip link add cpe type veth peer name wan netns "$1"
  local gw="${2%.*}.$(( ${2##*.} - 1 ))"
  ns "$6" ip addr add "$gw/30" dev cpe; ns "$6" ip link set cpe up
  ip route add "$2/32" via "$7" 2>/dev/null
  ns "$1" ip addr add "$2/30" dev wan; ns "$1" ip link set wan up; ns "$1" ip link set lo up
  ns "$1" ip route add default via "$gw"
  ip netns add "$4" 2>/dev/null
  ns "$1" ip link add lan type veth peer name eth0 netns "$4"
  ns "$1" ip addr add "10.$3.0.1/24" dev lan; ns "$1" ip link set lan up
  ns "$4" ip addr add "10.$3.0.2/24" dev eth0; ns "$4" ip link set eth0 up; ns "$4" ip link set lo up
  ns "$4" ip route add default via "10.$3.0.1"
  ns "$1" sh -c 'echo 1 > /proc/sys/net/ipv4/ip_forward'
  # cone: port-preserving; sym: random port per destination over the whole range;
  # cgn: carrier-grade NAT with a 1024-port block per subscriber (RFC 7422), random inside it.
  case "$5" in
    sym) ns "$1" iptables -t nat -A POSTROUTING -o wan -j MASQUERADE --random-fully ;;
    cgn) local base=$(( 20000 + 1024 * ${3} ))
         ns "$1" iptables -t nat -A POSTROUTING -o wan -p udp -j MASQUERADE --to-ports "$base-$((base + 1023))" --random-fully
         ns "$1" iptables -t nat -A POSTROUTING -o wan -j MASQUERADE --random-fully ;;
    *)   ns "$1" iptables -t nat -A POSTROUTING -o wan -j MASQUERADE ;;
  esac
  # Home router: drop unsolicited inbound, forward only established flows.
  ns "$1" iptables -A FORWARD -i wan -m conntrack --ctstate ESTABLISHED,RELATED -j ACCEPT
  ns "$1" iptables -A FORWARD -i wan -j DROP
  # Debug counters: UDP arriving from the Internet, by whether conntrack knows it.
  ns "$1" iptables -t mangle -A PREROUTING -i wan -p udp -m conntrack --ctstate ESTABLISHED -j ACCEPT
  ns "$1" iptables -t mangle -A PREROUTING -i wan -p udp -m conntrack --ctstate NEW -j ACCEPT
  ns "$1" iptables -A INPUT -i wan -p udp -j ACCEPT
}

run_case() { # server_nat client_nat
  local S=$1 C=$2 d="$WORK/$1-$2"; mkdir -p "$d"
  for n in sn ra rb ha hb ia ib; do ip netns del "$n" 2>/dev/null; done
  for l in v-sn v-ia v-ib; do ip link del "$l" 2>/dev/null; done
  ip route flush 198.51.100.0/24 2>/dev/null
  attach_public sn 203.0.113.10
  make_nat ra 198.51.100.2 1 ha "$S" ia 203.0.113.21
  make_nat rb 198.51.100.6 2 hb "$C" ib 203.0.113.31
  local CLIENT_EXTRA=""
  if [ "${LAB_MODE:-supernode}" = rdv ]; then
    # Website (Node adapter) as the ONLY rendezvous: no supernode, no VPS, no relay.
    local KG; KG=$(node "$ROOT/web/tools/keygen.mjs")
    (cd "$d" && LICENSE_SIGNING_KEY=$(echo "$KG" | sed -n 's/^LICENSE_SIGNING_KEY=\([^ ]*\).*/\1/p') \
      PEEROVA_DB=memory PORT=8788 HOST=0.0.0.0 COOKIE_SECURE=0 ADMIN_EMAILS=a@lab \
      ip netns exec sn node "$ROOT/web/server/node-server.mjs" > web.log 2>&1 &)
    for _ in $(seq 40); do ns sn curl -s -o /dev/null http://127.0.0.1:8788/api/v1/health && break; sleep 0.2; done
    (cd "$d" && ns ha "$BIN" server --port 47001 --no-map --rendezvous http://203.0.113.10:8788 --key a.key --ticket-file a.t ${LAB_SERVER_ARGS:-} < /dev/null > srv.log 2>&1 &)
    CLIENT_EXTRA="--rendezvous http://203.0.113.10:8788"
  else
  (cd "$d" && ns sn "$BIN" server --port 47000 --no-map --no-tcp --supernode-mode --key sn.key < /dev/null > sn.log 2>&1 &)
  sleep 0.5
  (cd "$d" && ns ha "$BIN" server --port 47001 --no-map --private --supernode 203.0.113.10:47000 --key a.key --ticket-file a.t < /dev/null > srv.log 2>&1 &)
  fi
  for _ in $(seq 30); do [ -s "$d/a.t" ] && break; sleep 0.2; done
  local t0=$(date +%s.%N)
  # LAB_REPEAT=N: connect N times in a row (later runs use what the first one learned: paths.cache).
  local rep
  for rep in $(seq 2 "${LAB_REPEAT:-1}"); do
    (cd "$d" && timeout $(( ${LAB_TIMEOUT:-50} + 10 )) ip netns exec hb "$BIN" ping @a.t $CLIENT_EXTRA --key c.key --timeout ${LAB_TIMEOUT:-50} --count 3 --interval-ms 20 --bench-mb 1 ${LAB_CLIENT_ARGS:-} > "cli$rep.log" 2>&1)
    sleep 0.5
  done
  (cd "$d" && timeout $(( ${LAB_TIMEOUT:-50} + 10 )) ip netns exec hb "$BIN" ping @a.t $CLIENT_EXTRA --key c.key --timeout ${LAB_TIMEOUT:-50} --count 20 --interval-ms 20 --bench-mb 2 ${LAB_CLIENT_ARGS:-} > cli.log 2>&1)
  local path rtt ok
  path=$(grep -oE '\[[a-z0-9+-]+\] trong [0-9]+ ms' "$d/cli.log" | head -1)
  rtt=$(grep -oE 'avg [0-9.]+' "$d/cli.log" | head -1)
  if grep -q "UPLOAD" "$d/cli.log" && grep -q "✔$" <(grep UPLOAD "$d/cli.log" | grep -o '✔'); then ok="✅ THÔNG"; else ok="❌ KHÔNG THÔNG"; fi
  printf "%-6s → %-6s  %-14s %-28s %s\n" "$S" "$C" "$ok" "${path:-—}" "${rtt:+RTT $rtt ms}"
  for f in "$d"/cli[0-9]*.log; do [ -e "$f" ] && printf "        %s: %s\n" "$(basename "$f" .log)" "$(grep -oE '\[[a-z0-9+-]+\] trong [0-9]+ ms' "$f" | head -1)"; done
  grep -hE "birthday|punch NAT|điểm hẹn" "$d"/*.log | sed 's/^/        /' | cut -c1-150 | head -4
  if [ -n "${LAB_DEBUG:-}" ]; then
    for r in ra rb; do echo "    --- $r FORWARD/INPUT counters"; ns $r iptables -L FORWARD -v -n -x | sed 's/^/      /'; ns $r iptables -L INPUT -v -n -x | sed 's/^/      /'; ns $r iptables -t mangle -L PREROUTING -v -n -x | sed 's/^/      /'; done
  fi
  pkill -f "$BIN" 2>/dev/null; pkill -f "web/server/node-server.mjs" 2>/dev/null; sleep 0.3
}

echo "server NAT → client NAT   kết quả        đường đi (thời gian kết nối)"
if [ $# -eq 2 ]; then run_case "$1" "$2"; else
  if [ -n "${LAB_CASES:-}" ]; then IFS=',' read -ra CASES <<< "$LAB_CASES"   # e.g. LAB_CASES="cgn cgn,sym sym"
  else CASES=("cone cone" "cone sym" "sym cone" "sym sym" "cgn cgn" "cone cgn"); fi
  for c in "${CASES[@]}"; do run_case $c; done
fi
echo "logs: $WORK"
