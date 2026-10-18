#!/bin/bash
# ============================================================
#  CAI DAT MOT LAN tren VPS cho website Nuxt. Chay bang root:
#     sudo bash deploy/cai-dat-vps.sh
#  (chep thu muc deploy/ len VPS truoc, vd: scp -r deploy root@180.93.57.30:/root/htbb-web-deploy)
#
#  Lam gi:
#    1. Kiem tra Node >= 20 (Nuxt 4 can). Thieu thi in huong dan, KHONG tu cai.
#    2. Tao user he thong htbb-web, them vao nhom htbb (doc server.ini + log game).
#    3. Tao /opt/htbb-web/{releases,log,deploy}, chep deploy-web.sh vao.
#    4. Cai htbb-web.service (chua start - chua co ban build nao).
#    5. Tao khoa SSH rieng cho GitHub Actions, bi ep chay deploy-web.sh, in ra
#       private key de dan vao secret VPS_SSH_KEY_WEB.
#  KHONG dung toi Apache - bat proxy la buoc rieng, xem deploy/apache-htbb-web.conf.
# ============================================================
set -eu

[ "$(id -u)" = 0 ] || { echo "Phai chay bang root"; exit 1; }
DIR_SCRIPT=$(cd "$(dirname "$0")" && pwd)

echo "== 1. Node"
if ! command -v node >/dev/null || [ "$(node -p 'process.versions.node.split(".")[0]')" -lt 22 ]; then
    echo "Can Node >= 22 (xem .nvmrc). Cai (Debian/Ubuntu):"
    echo "  curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && apt-get install -y nodejs"
    exit 1
fi
node -v

echo "== 2. User htbb-web"
id htbb-web >/dev/null 2>&1 || useradd --system --no-create-home --shell /usr/sbin/nologin htbb-web
getent group htbb >/dev/null && usermod -a -G htbb htbb-web || echo "  (khong co nhom htbb - web se khong doc duoc server.ini, phai khai HTBB_DB_* qua GitHub Secrets)"

echo "== 3. Thu muc"
install -d -m 755 /opt/htbb-web /opt/htbb-web/releases /opt/htbb-web/log /opt/htbb-web/deploy
install -m 755 "$DIR_SCRIPT/deploy-web.sh" /opt/htbb-web/deploy/deploy-web.sh
[ -f /etc/htbb-web.env ] || { install -m 640 -o root -g htbb-web /dev/null /etc/htbb-web.env; }

echo "== 4. systemd"
install -m 644 "$DIR_SCRIPT/htbb-web.service" /etc/systemd/system/htbb-web.service
systemctl daemon-reload
systemctl enable htbb-web >/dev/null

echo "== 5. Khoa SSH cho GitHub Actions"
KHOA=/root/.ssh/deploy_github_web
if [ ! -f "$KHOA" ]; then
    ssh-keygen -t ed25519 -N '' -f "$KHOA" -C github-actions-deploy-web >/dev/null
fi
DONG="command=\"/opt/htbb-web/deploy/deploy-web.sh\",no-port-forwarding,no-X11-forwarding,no-agent-forwarding,no-pty $(cat "$KHOA.pub")"
mkdir -p /root/.ssh && touch /root/.ssh/authorized_keys && chmod 600 /root/.ssh/authorized_keys
grep -qF "$(cat "$KHOA.pub")" /root/.ssh/authorized_keys || echo "$DONG" >> /root/.ssh/authorized_keys

echo
echo "XONG. Dan TOAN BO noi dung duoi day vao GitHub > KPAH-Web > Settings > Secrets and variables > Actions > New secret 'VPS_SSH_KEY_WEB':"
echo "--------------------------------------------------------------------------"
cat "$KHOA"
echo "--------------------------------------------------------------------------"
echo "Them cac secret HTBB_* (xem README). Push len main -> GitHub tu build + deploy."
echo "Sau lan deploy dau tien chay duoc (curl http://127.0.0.1:3000/api/site), bat proxy Apache: xem deploy/apache-htbb-web.conf"
