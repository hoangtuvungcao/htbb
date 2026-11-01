#!/usr/bin/env bash
# Deploy peerova lên VPS ở port riêng, đo ping/throughput thật từ máy này, rồi dọn dẹp.
# KHÔNG đụng tới tiến trình nào khác: chỉ dừng đúng PID mà script này khởi động.
#
#   scripts/vps-test.sh root@180.93.57.30            # deploy + test (port UDP 47123)
#   scripts/vps-test.sh root@180.93.57.30 47123 cleanup   # dừng peerova-test + xoá rule firewall
set -euo pipefail
HOST=${1:?dùng: $0 user@host [port] [cleanup]}
PORT=${2:-47123}
MODE=${3:-test}
DIR=/opt/peerova-test
ROOT=$(cd "$(dirname "$0")/.." && pwd)
BIN=$ROOT/target/x86_64-unknown-linux-musl/release/peerova
LOCAL_BIN=$ROOT/target/release/peerova
OUT=$ROOT/target/vps-test; mkdir -p "$OUT"

if [ "$MODE" = cleanup ]; then
  ssh "$HOST" "if [ -f $DIR/peerova.pid ]; then kill \$(cat $DIR/peerova.pid) 2>/dev/null && echo 'đã dừng peerova-test'; rm -f $DIR/peerova.pid; fi;
               command -v ufw >/dev/null && ufw delete allow $PORT/udp >/dev/null 2>&1 && echo 'đã xoá rule ufw $PORT/udp' || true"
  exit 0
fi

[ -x "$BIN" ] || (cd "$ROOT" && cargo build --release --target x86_64-unknown-linux-musl -p peerova-cli)
[ -x "$LOCAL_BIN" ] || (cd "$ROOT" && cargo build --release -p peerova-cli)

echo "== 1. Kiểm tra port $PORT/udp chưa bị dùng trên VPS"
ssh "$HOST" "ss -lun | awk '{print \$4}' | grep -q ':$PORT\$' && { echo 'PORT ĐANG BẬN — chọn port khác'; exit 1; } || echo 'port trống ✔'"

echo "== 2. Upload binary (static, không cài gì vào hệ thống) → $DIR"
ssh "$HOST" "mkdir -p $DIR"
scp -q "$BIN" "$HOST:$DIR/peerova"

echo "== 3. Mở UDP $PORT trên ufw (server 'dark': không trả lời gói nào thiếu ticket) + chạy nice 10"
ssh "$HOST" "cd $DIR && chmod +x peerova && (command -v ufw >/dev/null && ufw allow $PORT/udp comment peerova-test >/dev/null || true);
  [ -f peerova.pid ] && kill \$(cat peerova.pid) 2>/dev/null || true; rm -f ticket.txt;
  nohup nice -n 10 ./peerova server --port $PORT --key $DIR/server.key --no-map --ticket-file $DIR/ticket.txt < /dev/null > $DIR/server.log 2>&1 &
  echo \$! > peerova.pid; for i in \$(seq 20); do [ -s ticket.txt ] && break; sleep 0.3; done; ./peerova doctor | tail -6"
scp -q "$HOST:$DIR/ticket.txt" "$OUT/ticket.txt"
echo "ticket: $(cut -c1-60 "$OUT/ticket.txt")..."

IP=${HOST#*@}
echo; echo "== 4. Baseline ICMP ping (hệ điều hành) tới $IP"
ping -c 20 -i 0.2 "$IP" | tail -2 | tee "$OUT/icmp.txt" || echo "(ICMP bị chặn)"

echo; echo "== 5. peerova ping (datagram mã hoá, cùng đường với traffic realtime) + upload 4 MB"
"$LOCAL_BIN" ping @"$OUT/ticket.txt" --count 200 --interval-ms 50 --bench-mb 4 | tee "$OUT/peerova-ping.txt" | grep -E "Kết nối|LATENCY|UPLOAD|✘"

echo; echo "== 6. Log server trên VPS"
ssh "$HOST" "tail -5 $DIR/server.log; echo; echo 'CPU/RAM peerova:'; ps -o pid,ni,%cpu,rss,cmd -p \$(cat $DIR/peerova.pid)"
echo
echo "Kết quả lưu ở $OUT/. Server test vẫn chạy; dọn dẹp bằng:"
echo "  $0 $HOST $PORT cleanup"
