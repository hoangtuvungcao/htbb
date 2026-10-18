#!/bin/bash
# ============================================================
#  DEPLOY WEBSITE HTBB (Nuxt) - lenh DUY NHAT ma khoa SSH cua GitHub Actions
#  duoc phep chay (command="/usr/local/sbin/htbb-deploy-web.sh" trong
#  /root/.ssh/authorized_keys; cai bang web/deploy/cai-dat-vps.sh). Ca du lieu lan
#  bi mat deu di qua STDIN.
#
#  "Viec" can lam doc tu SSH_ORIGINAL_COMMAND, chi nhan dung hai dang:
#     nhan-env           STDIN = cac dong KEY=VALUE (chi nhan khoa trong danh sach
#                        ben duoi). Gop vao /etc/htbb/htbb.env: khoa nao GitHub khong
#                        gui (secret de trong) thi GIU gia tri cu (vd. mat khau CSDL do
#                        luc dung VPS sinh ra).
#     nhan-build <sha>   STDIN = tar.gz chua thu muc .output cua "nuxt build".
#                        Giai nen vao /opt/htbb/web/releases/<sha>, tro symlink
#                        /opt/htbb/web/.output -> releases/<sha>/.output, restart
#                        htbb-web, kiem tra /api/site tra 200; hong thi QUAY LAI ban truoc.
#
#  Moi thu khac -> tu choi. Khong bao gio in gia tri bi mat ra log.
#  Log: /opt/htbb/logs/deploy-web.log
# ============================================================
set -u

GOC=/opt/htbb/web
RELEASES=$GOC/releases
ENV_FILE=/etc/htbb/htbb.env
SV=htbb-web
USER_WEB=htbb
CONG=3000
LOG=/opt/htbb/logs/deploy-web.log
GIU_LAI=5   # so ban release cu giu lai de quay lui

mkdir -p "$(dirname "$LOG")" "$RELEASES"
exec 2>>"$LOG"
log() { echo "[$(date '+%F %T')] $*" | tee -a "$LOG"; }

exec 9>/tmp/htbb-web-deploy.lock
if ! flock -w 120 9; then
    log "Dang co lan deploy khac, bo qua."
    exit 1
fi

# Khoa duoc phep ghi vao file env. Them bien moi thi them o day VA trong .github/workflows/web-deploy.yml.
KHOA_HOP_LE='^(HTBB_SECRET|HTBB_DB_HOST|HTBB_DB_PORT|HTBB_DB_USER|HTBB_DB_PASS|HTBB_DB_GAME|HTBB_DB_ACCOUNT|HTBB_DB_ACCOUNT_USER|HTBB_DB_ACCOUNT_PASS|HTBB_TPBANK_ACCOUNT_NO|HTBB_TPBANK_ACCOUNT_NAME|HTBB_SEPAY_WEBHOOK_APIKEY|HTBB_ADMIN_NAPTIEN_PASSWORD|HTBB_ADMIN_ACCOUNTS|HTBB_ADMIN_2FA_BAT_BUOC|HTBB_ADMIN_IDLE_TIMEOUT|HTBB_ADMIN_MAX_FAIL|HTBB_ADMIN_FAIL_WINDOW|HTBB_ADMIN_IP_CHO_PHEP|HTBB_SERVER_DIR|HTBB_LOG_DIR|HTBB_TRUSTED_PROXIES|HTBB_LINK_ANDROID|HTBB_LINK_PC|HTBB_LINK_IOS|HTBB_LINK_JAR|HTBB_LINK_GROUP|HTBB_SITE_URL|HTBB_AUTO_ACTIVATE|HTBB_NAP_BAT|HTBB_NAP_SANDBOX|NAP_DA_CO_GIAY_PHEP|HTBB_NAP_ACCOUNT_NO|HTBB_NAP_ACCOUNT_NAME|HTBB_NAP_BANK_BIN|HTBB_SEPAY_API_TOKEN|HTBB_SEPAY_DO_PHUT|HTBB_SEPAY_DO_GIO|HTBB_CASSO_WEBHOOK_SECRET|HTBB_GMAIL_CLIENT_ID|HTBB_GMAIL_CLIENT_SECRET|HTBB_GMAIL_REFRESH_TOKEN|HTBB_GMAIL_SENDER|HTBB_MAIL_FROM_NAME|HTBB_SERVER_LIST|HTBB_NQSH_LIST|HTBB_UPLOAD_DIR|HTBB_TAI_GAME_DIR|HTBB_TAI_GAME_TOI_DA_MB|HTBB_CSP|HTBB_HSTS)$'

nhan_env() {
    local tam
    mkdir -p "$(dirname "$ENV_FILE")"
    chmod 700 "$(dirname "$ENV_FILE")"
    tam=$(mktemp "$(dirname "$ENV_FILE")/.htbb.env.XXXXXX") || { log "[LOI] khong tao duoc file tam"; exit 1; }
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

    # Mac dinh cho VPS neu chua ai khai (NODE_ENV/HOST/PORT do htbb-web.service dat).
    grep -q '^HTBB_SERVER_DIR=' "$tam" || echo 'HTBB_SERVER_DIR=/opt/htbb/server' >> "$tam"

    if ! grep -qE '^HTBB_SECRET="?.{32,}' "$tam"; then
        log "[CANH BAO] HTBB_SECRET chua dat hoac qua ngan (<32 ky tu). Dat secret HTBB_SECRET tren GitHub: openssl rand -hex 32"
    fi

    chown root:root "$tam"
    chmod 600 "$tam"
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
    local dich="$RELEASES/$sha"
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

    # /opt/htbb/web/.output la symlink. Lan dau co the la thu muc that (build tai cho):
    # cat sang releases/ de "ln -sfn" khong tao link BEN TRONG thu muc do.
    if [ -d "$GOC/.output" ] && [ ! -L "$GOC/.output" ]; then
        local cho="$RELEASES/tai-cho-$(date +%Y%m%d%H%M%S)"
        mkdir -p "$cho" && mv "$GOC/.output" "$cho/.output"
        log "nhan-build: chuyen ban build tai cho sang $cho"
    fi
    local cu
    cu=$(readlink -f "$GOC/.output" 2>/dev/null || true)
    ln -sfn "$dich/.output" "$GOC/.output"
    log "nhan-build: .output -> $sha, restart $SV"
    systemctl restart "$SV"

    for _ in $(seq 1 30); do
        sleep 1
        if curl -fsS -o /dev/null "http://127.0.0.1:$CONG/api/site"; then
            log "== XONG: website chay ban $sha"
            # Don ban cu, giu lai $GIU_LAI ban gan nhat.
            ls -1dt "$RELEASES"/*/ 2>/dev/null | tail -n +$((GIU_LAI + 1)) | xargs -r rm -rf
            exit 0
        fi
    done

    log "[LOI] sau 30s /api/site chua tra 200. Xem: journalctl -u $SV -n 100"
    if [ -n "$cu" ] && [ -d "$cu" ] && [ "$cu" != "$dich/.output" ]; then
        log "Quay lai ban truoc: $cu"
        ln -sfn "$cu" "$GOC/.output"
        systemctl restart "$SV"
    fi
    exit 1
}

# shellcheck disable=SC2086  # tach tu co chu dich: "nhan-build <sha>"
set -- ${SSH_ORIGINAL_COMMAND:-}
case "${1:-}" in
    nhan-env)   nhan_env ;;
    nhan-build) nhan_build "${2:-}" ;;
    *)
        log "[TU CHOI] lenh khong hop le"
        echo "Chi nhan: nhan-env | nhan-build <sha>" >&2
        exit 2 ;;
esac
