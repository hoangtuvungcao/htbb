#!/bin/bash
# ============================================================
#  CAI DAT MOT LAN tren VPS cho website Nuxt. Chay bang root SAU khi da dung VPS
#  (user htbb, /opt/htbb = ban checkout monorepo, /etc/htbb/htbb.env, nginx - xem
#  vps/README.md va docs/trien-khai.md):
#     sudo bash /opt/htbb/web/deploy/cai-dat-vps.sh
#
#  Lam gi:
#    1. Kiem tra Node >= 22 (Nuxt 4 can). Thieu thi in huong dan, KHONG tu cai.
#    2. Tao /opt/htbb/web/{releases,uploads}, /opt/htbb/downloads, /opt/htbb/logs.
#    3. Cai deploy-web.sh vao /usr/local/sbin/htbb-deploy-web.sh (root so huu: khoa SSH bi ep
#       chay lenh nay bang root, nen KHONG tro thang vao tep user htbb ghi duoc trong checkout).
#    4. Cai htbb-web.service (chua start neu chua co ban build).
#    5. Tao khoa SSH rieng cho GitHub Actions, bi ep chay htbb-deploy-web.sh, in ra
#       private key de dan vao secret VPS_SSH_KEY_WEB.
#  KHONG dung toi nginx - xem vps/htbb-nginx.conf.
# ============================================================
set -eu

[ "$(id -u)" = 0 ] || { echo "Phai chay bang root"; exit 1; }
DIR_SCRIPT=$(cd "$(dirname "$0")" && pwd)

echo "== 1. Node"
if ! command -v node >/dev/null || [ "$(node -p 'process.versions.node.split(".")[0]')" -lt 22 ]; then
    echo "Can Node >= 22 (xem .nvmrc). Cai (Ubuntu):"
    echo "  curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && apt-get install -y nodejs"
    exit 1
fi
node -v

echo "== 2. Thu muc"
id -u htbb >/dev/null 2>&1 || { echo "Chua co user htbb - dung VPS truoc (vps/README.md)"; exit 1; }
install -d -o htbb -g htbb -m 755 /opt/htbb/web /opt/htbb/downloads
install -d -o htbb -g htbb -m 750 /opt/htbb/web/uploads /opt/htbb/logs
install -d -o root -g htbb -m 750 /opt/htbb/web/releases
[ -f /etc/htbb/htbb.env ] || echo "  [CANH BAO] chua co /etc/htbb/htbb.env - htbb-web se khong khoi dong (xem web/.env.example)"

echo "== 3. Script deploy"
install -o root -g root -m 755 "$DIR_SCRIPT/deploy-web.sh" /usr/local/sbin/htbb-deploy-web.sh

echo "== 4. systemd"
install -m 644 "$DIR_SCRIPT/htbb-web.service" /etc/systemd/system/htbb-web.service
systemctl daemon-reload
systemctl enable htbb-web >/dev/null

echo "== 5. Khoa SSH cho GitHub Actions"
KHOA=/root/.ssh/deploy_github_web
if [ ! -f "$KHOA" ]; then
    ssh-keygen -t ed25519 -N '' -f "$KHOA" -C github-actions-deploy-web >/dev/null
fi
DONG="command=\"/usr/local/sbin/htbb-deploy-web.sh\",no-port-forwarding,no-X11-forwarding,no-agent-forwarding,no-pty $(cat "$KHOA.pub")"
mkdir -p /root/.ssh && touch /root/.ssh/authorized_keys && chmod 600 /root/.ssh/authorized_keys
grep -qF "$(cat "$KHOA.pub")" /root/.ssh/authorized_keys || echo "$DONG" >> /root/.ssh/authorized_keys

echo
echo "XONG. Dan TOAN BO noi dung duoi day vao GitHub > repo htbb > Settings > Secrets and variables > Actions > New secret 'VPS_SSH_KEY_WEB':"
echo "--------------------------------------------------------------------------"
cat "$KHOA"
echo "--------------------------------------------------------------------------"
echo "Push len main -> GitHub tu build + deploy. Kiem tra: curl -fsS http://127.0.0.1:3000/api/site"
