#!/bin/bash
# ============================================================
#  DEPLOY WEBSITE KPAH (Nuxt) - lenh DUY NHAT ma khoa SSH cua GitHub Actions
#  duoc phep chay (command="/opt/kpah-web/deploy/deploy-web.sh" trong
#  /root/.ssh/authorized_keys). Ca du lieu lan bi mat deu di qua STDIN.
#
#  "Viec" can lam doc tu SSH_ORIGINAL_COMMAND, chi nhan dung hai dang:
#     nhan-env           STDIN = cac dong KEY=VALUE (chi nhan khoa KPAH_* trong
#                        danh sach ben duoi). Gop vao /etc/kpah-web.env: khoa nao
#                        GitHub khong gui (secret de trong) thi GIU gia tri cu.
#     nhan-build <sha>   STDIN = tar.gz chua thu muc .output cua "nuxt build".
#                        Giai nen vao releases/<sha>, tro current -> do, restart
#                        kpah-web, kiem tra /api/site tra 200; hong thi QUAY LAI
#                        ban truoc.
#
#  Moi thu khac -> tu choi. Khong bao gio in gia tri bi mat ra log.
#  Log: /opt/kpah-web/log/deploy.log
# ============================================================
set -u

GOC=/opt/kpah-web
ENV_FILE=/etc/kpah-web.env
SV=kpah-web
USER_WEB=kpah-web
CONG=3000
LOG=$GOC/log/deploy.log
GIU_LAI=5   # so ban release cu giu lai de quay lui

mkdir -p "$GOC/log" "$GOC/releases"
exec 2>>"$LOG"
log() { echo "[$(date '+%F %T')] $*" | tee -a "$LOG"; }

exec 9>/tmp/kpah-web-deploy.lock
if ! flock -w 120 9; then
    log "Dang co lan deploy khac, bo qua."
    exit 1
fi

# Khoa duoc phep ghi vao file env. Them bien moi thi them o day VA trong deploy.yml.
KHOA_HOP_LE='^(KPAH_SECRET|KPAH_DB_HOST|KPAH_DB_PORT|KPAH_DB_USER|KPAH_DB_PASS|KPAH_DB_GAME|KPAH_DB_ACCOUNT|KPAH_DB_ACCOUNT_USER|KPAH_DB_ACCOUNT_PASS|KPAH_TPBANK_ACCOUNT_NO|KPAH_TPBANK_ACCOUNT_NAME|KPAH_SEPAY_WEBHOOK_APIKEY|KPAH_ADMIN_NAPTIEN_PASSWORD|KPAH_ADMIN_ACCOUNTS|KPAH_ADMIN_2FA_BAT_BUOC|KPAH_ADMIN_IDLE_TIMEOUT|KPAH_ADMIN_MAX_FAIL|KPAH_ADMIN_FAIL_WINDOW|KPAH_SERVER_DIR|KPAH_TRUSTED_PROXIES|KPAH_LINK_ANDROID|KPAH_LINK_PC|KPAH_LINK_IOS|KPAH_LINK_JAR|KPAH_LINK_GROUP|KPAH_AUTO_ACTIVATE|KPAH_GMAIL_CLIENT_ID|KPAH_GMAIL_CLIENT_SECRET|KPAH_GMAIL_REFRESH_TOKEN|KPAH_GMAIL_SENDER|KPAH_MAIL_FROM_NAME)$'

nhan_env() {
    local tam
    tam=$(mktemp /etc/.kpah-web.env.XXXXXX) || { log "[LOI] khong tao duoc file tam"; exit 1; }
    chmod 600 "$tam"
    [ -f "$ENV_FILE" ] && cat "$ENV_FILE" > "$tam"

    local k v so=0
    while IFS='=' read -r k v; do
        v=${v%$'\r'}
        [ -z "$k" ] && continue
        if ! [[ "$k" =~ $KHOA_HOP_LE ]]; then
            log "[BO QUA] khoa khong nam trong danh sach: $k"
            continue
        fi
        # Xoa dong cu cua khoa nay roi ghi dong moi. systemd EnvironmentFile doc
        # nguyen van sau dau "=", nen boc trong nhay kep va thoat \ va ".
        grep -v "^$k=" "$tam" > "$tam.2" || true
        mv "$tam.2" "$tam"
        v=${v//\\/\\\\}
        v=${v//\"/\\\"}
        printf '%s="%s"\n' "$k" "$v" >> "$tam"
        so=$((so + 1))
    done

    # Mac dinh cho VPS neu chua ai khai.
    grep -q '^KPAH_SERVER_DIR=' "$tam" || echo 'KPAH_SERVER_DIR="/opt/kpah"' >> "$tam"
    grep -q '^NODE_ENV=' "$tam" || echo 'NODE_ENV="production"' >> "$tam"
    grep -q '^PORT=' "$tam" || echo "PORT=\"$CONG\"" >> "$tam"
    grep -q '^HOST=' "$tam" || echo 'HOST="127.0.0.1"' >> "$tam"

    if ! grep -q '^KPAH_SECRET=".\{32,\}"' "$tam"; then
        log "[CANH BAO] KPAH_SECRET chua dat hoac qua ngan (<32 ky tu). Dat secret KPAH_SECRET tren GitHub: openssl rand -hex 32"
    fi

    chown root:"$USER_WEB" "$tam"
    chmod 640 "$tam"
    if [ -f "$ENV_FILE" ] && cmp -s "$tam" "$ENV_FILE"; then
        rm -f "$tam"
        log "nhan-env: khong co gi thay doi ($so khoa nhan duoc)"
    else
        mv "$tam" "$ENV_FILE"
        log "nhan-env: da cap nhat $ENV_FILE ($so khoa nhan duoc) - se ap dung o lan restart cua nhan-build"
    fi
}

nhan_build() {
    local sha=$1
    if ! [[ "$sha" =~ ^[0-9a-f]{7,40}$ ]]; then
        log "[LOI] sha khong hop le"
        exit 1
    fi
    local dich="$GOC/releases/$sha"
    rm -rf "$dich.tmp"
    mkdir -p "$dich.tmp"
    if ! tar -xzf - -C "$dich.tmp" --no-same-owner; then
        log "[LOI] giai nen that bai"
        rm -rf "$dich.tmp"
        exit 1
    fi
    if [ ! -f "$dich.tmp/.output/server/index.mjs" ]; then
        log "[LOI] ban build thieu .output/server/index.mjs"
        rm -rf "$dich.tmp"
        exit 1
    fi
    rm -rf "$dich"
    mv "$dich.tmp" "$dich"
    chown -R root:"$USER_WEB" "$dich"
    chmod -R g+rX,o-rwx "$dich"

    local cu
    cu=$(readlink -f "$GOC/current" 2>/dev/null || true)
    ln -sfn "$dich" "$GOC/current"
    log "nhan-build: chuyen current -> $sha, restart $SV"
    systemctl restart "$SV"

    for i in $(seq 1 30); do
        sleep 1
        if curl -fsS -o /dev/null "http://127.0.0.1:$CONG/api/site"; then
            log "== XONG: website chay ban $sha"
            # Don ban cu, giu lai $GIU_LAI ban gan nhat.
            ls -1dt "$GOC"/releases/*/ 2>/dev/null | tail -n +$((GIU_LAI + 1)) | xargs -r rm -rf
            exit 0
        fi
    done

    log "[LOI] sau 30s /api/site chua tra 200. Xem: journalctl -u $SV -n 100"
    if [ -n "$cu" ] && [ -d "$cu" ] && [ "$cu" != "$dich" ]; then
        log "Quay lai ban truoc: $cu"
        ln -sfn "$cu" "$GOC/current"
        systemctl restart "$SV"
    fi
    exit 1
}

set -- ${SSH_ORIGINAL_COMMAND:-}
case "${1:-}" in
    nhan-env)   nhan_env ;;
    nhan-build) nhan_build "${2:-}" ;;
    *)
        log "[TU CHOI] lenh khong hop le"
        echo "Chi nhan: nhan-env | nhan-build <sha>" >&2
        exit 2 ;;
esac
