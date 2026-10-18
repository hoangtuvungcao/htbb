#!/bin/bash
# ============================================================
#  DEPLOY WEBSITE HTBB (Nuxt) - lenh DUY NHAT ma khoa SSH cua GitHub Actions
#  duoc phep chay (command="/opt/htbb-web/deploy/deploy-web.sh" trong
#  /root/.ssh/authorized_keys). Ca du lieu lan bi mat deu di qua STDIN.
#
#  "Viec" can lam doc tu SSH_ORIGINAL_COMMAND, chi nhan dung hai dang:
#     nhan-env           STDIN = cac dong KEY=VALUE (chi nhan khoa HTBB_* trong
#                        danh sach ben duoi). Gop vao /etc/htbb-web.env: khoa nao
#                        GitHub khong gui (secret de trong) thi GIU gia tri cu.
#     nhan-build <sha>   STDIN = tar.gz chua thu muc .output cua "nuxt build".
#                        Giai nen vao releases/<sha>, tro current -> do, restart
#                        htbb-web, kiem tra /api/site tra 200; hong thi QUAY LAI
#                        ban truoc.
#
#  Moi thu khac -> tu choi. Khong bao gio in gia tri bi mat ra log.
#  Log: /opt/htbb-web/log/deploy.log
# ============================================================
set -u

GOC=/opt/htbb-web
ENV_FILE=/etc/htbb-web.env; [ -e /etc/htbb-web.env ] || [ ! -e /etc/kpah-web.env ] || ENV_FILE=/etc/kpah-web.env  # TUONG-THICH-TEN-CU: bo sau 1 ban
SV=htbb-web; systemctl cat htbb-web >/dev/null 2>&1 || SV=kpah-web  # TUONG-THICH-TEN-CU: bo sau 1 ban
USER_WEB=htbb-web; id -u htbb-web >/dev/null 2>&1 || USER_WEB=kpah-web  # TUONG-THICH-TEN-CU: bo sau 1 ban
CONG=3000
LOG=$GOC/log/deploy.log
GIU_LAI=5   # so ban release cu giu lai de quay lui

mkdir -p "$GOC/log" "$GOC/releases"
exec 2>>"$LOG"
log() { echo "[$(date '+%F %T')] $*" | tee -a "$LOG"; }

exec 9>/tmp/htbb-web-deploy.lock
if ! flock -w 120 9; then
    log "Dang co lan deploy khac, bo qua."
    exit 1
fi

# Khoa duoc phep ghi vao file env. Them bien moi thi them o day VA trong deploy.yml.
KHOA_HOP_LE='^(HTBB_SECRET|HTBB_DB_HOST|HTBB_DB_PORT|HTBB_DB_USER|HTBB_DB_PASS|HTBB_DB_GAME|HTBB_DB_ACCOUNT|HTBB_DB_ACCOUNT_USER|HTBB_DB_ACCOUNT_PASS|HTBB_TPBANK_ACCOUNT_NO|HTBB_TPBANK_ACCOUNT_NAME|HTBB_SEPAY_WEBHOOK_APIKEY|HTBB_ADMIN_NAPTIEN_PASSWORD|HTBB_ADMIN_ACCOUNTS|HTBB_ADMIN_2FA_BAT_BUOC|HTBB_ADMIN_IDLE_TIMEOUT|HTBB_ADMIN_MAX_FAIL|HTBB_ADMIN_FAIL_WINDOW|HTBB_SERVER_DIR|HTBB_TRUSTED_PROXIES|HTBB_LINK_ANDROID|HTBB_LINK_PC|HTBB_LINK_IOS|HTBB_LINK_JAR|HTBB_LINK_GROUP|HTBB_SITE_URL|HTBB_AUTO_ACTIVATE|HTBB_NAP_BAT|NAP_DA_CO_GIAY_PHEP|HTBB_NAP_ACCOUNT_NO|HTBB_NAP_ACCOUNT_NAME|HTBB_NAP_BANK_BIN|HTBB_SEPAY_API_TOKEN|HTBB_SEPAY_DO_PHUT|HTBB_SEPAY_DO_GIO|HTBB_CASSO_WEBHOOK_SECRET|HTBB_GMAIL_CLIENT_ID|HTBB_GMAIL_CLIENT_SECRET|HTBB_GMAIL_REFRESH_TOKEN|HTBB_GMAIL_SENDER|HTBB_MAIL_FROM_NAME|HTBB_SERVER_LIST|HTBB_SERVER_LIST|HTBB_NQSH_LIST|HTBB_NQSH_LIST)$'

nhan_env() {
    local tam
    tam=$(mktemp /etc/.htbb-web.env.XXXXXX) || { log "[LOI] khong tao duoc file tam"; exit 1; }
    chmod 600 "$tam"
    [ -f "$ENV_FILE" ] && cat "$ENV_FILE" > "$tam"

    local k v so=0
    while IFS='=' read -r k v; do
        v=${v%$'\r'}
        [ -z "$k" ] && continue
        case "$k" in KPAH_*) k="HTBB_${k#KPAH_}" ;; esac  # TUONG-THICH-TEN-CU: nhan ca ten bien cu, ghi ten moi
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
    grep -q '^HTBB_SERVER_DIR=' "$tam" || echo 'HTBB_SERVER_DIR="/opt/htbb/server"' >> "$tam"
    grep -q '^NODE_ENV=' "$tam" || echo 'NODE_ENV="production"' >> "$tam"
    grep -q '^PORT=' "$tam" || echo "PORT=\"$CONG\"" >> "$tam"
    grep -q '^HOST=' "$tam" || echo 'HOST="127.0.0.1"' >> "$tam"

    if ! grep -q '^HTBB_SECRET=".\{32,\}"' "$tam"; then
        log "[CANH BAO] HTBB_SECRET chua dat hoac qua ngan (<32 ky tu). Dat secret HTBB_SECRET tren GitHub: openssl rand -hex 32"
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
