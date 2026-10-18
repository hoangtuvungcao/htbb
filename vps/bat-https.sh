#!/bin/bash
# ============================================================================
#  BAT HTTPS CHO TRANG QUAN TRI
#
#      sudo /opt/htbb/vps/bat-https.sh                  # chung chi tu ky (theo IP)
#      sudo /opt/htbb/vps/bat-https.sh admin.abc.com    # chung chi that (Let's Encrypt)
#
# ----------------------------------------------------------------------------
#  VI SAO CAN
# ----------------------------------------------------------------------------
#  Truoc script nay may chu chi mo cong 80 va module ssl cua Apache con chua cai.
#  Nghia la moi lan dang nhap Trang quan tri, ten va mat khau admin di qua Internet
#  duoi dang CHU THUONG - ai dung tren duong truyen (wifi cong cong, nha mang, nha
#  cung cap may chu) doc duoc nguyen van. Do la duong ro that, khong phai viec
#  "duong dan /admin bi lo": doi duong dan hay doi sang app desktop deu khong sua
#  duoc chuyen nay, vi goi tin van la HTTP tran.
#
# ----------------------------------------------------------------------------
#  SCRIPT NAY LAM GI (va CO Y khong lam gi)
# ----------------------------------------------------------------------------
#   1. Bat module ssl, rewrite, headers.
#   2. Tao chung chi:
#        - Co ten mien  -> Let's Encrypt qua certbot, trinh duyet khong canh bao.
#        - Khong ten mien -> tu ky, hop le 10 nam, theo dung IP cua may.
#          Let's Encrypt KHONG cap chung chi cho IP tran, nen day la lua chon duy
#          nhat khi chua co ten mien. Chung chi tu ky MA HOA DAY DU (mat khau khong
#          con di dang chu thuong), chi la trinh duyet canh bao lan dau vi khong ai
#          bao dam ho danh tinh may chu - bam "van tiep tuc" mot lan la xong.
#   3. Them mot VirtualHost cong 443, giu NGUYEN cong 80. Website la HTBB-Web
#      (Nuxt) sau reverse proxy: vhost nay KHONG co DocumentRoot rieng, ProxyPass
#      cua htbb-web-proxy.conf (HTBB-Web/deploy/apache-htbb-web.conf, tang server)
#      duoc moi vhost ke thua. Web PHP cu (/opt/htbb/web) da bo 19/10/2026.
#   4. Ep HTTPS CHI cho /admin (manh 20-admin-https.conf, xem
#      vps/chi-cho-server-txt.sh vi sao luat nam trong /etc/apache2/htbb-web-bao-ve.d).
#
#  KHONG ep HTTPS cho ca site. Trang gioi thieu / dang ky / nap tien cua nguoi choi
#  van chay cong 80 nhu cu. Ly do: voi chung chi tu ky, ep ca site la MOI nguoi choi
#  deu gap man hinh canh bao do cua trinh duyet - phan lon se tuong server bi hack va
#  bo di. Doi mot chut an toan cho trang cong khai de khong mat nguoi choi la danh
#  doi dung; cai can bao ve la duong dang nhap admin.
#
#  KHONG dat HSTS khi dung chung chi tu ky. HSTS bat trinh duyet cam tiet HTTP cho ca
#  ten may chu trong nhieu thang, khong go duoc tu phia may chu. Dat kem chung chi
#  tu ky la mot cach tu khoa minh ra ngoai rat kho sua. Co ten mien that thi script
#  tu dat HSTS.
#
# ----------------------------------------------------------------------------
#  GO BO NEU CO CHUYEN
# ----------------------------------------------------------------------------
#      sudo rm /etc/apache2/htbb-web-bao-ve.d/20-admin-https.conf
#      sudo a2dissite htbb-ssl
#      sudo systemctl reload apache2
#  Cong 80 chua bao gio bi tat nen trang web van chay trong suot qua trinh nay.
# ============================================================================
set -u

TEN_MIEN="${1:-}"
CONF_SSL=/etc/apache2/sites-available/htbb-ssl.conf
THU_MUC_BAO_VE=/etc/apache2/htbb-web-bao-ve.d
CONF_EP="$THU_MUC_BAO_VE/20-admin-https.conf"
CONF_NAP=/etc/apache2/conf-available/htbb-web-bao-ve.conf
CHUNG_CHI_DIR=/etc/ssl/htbb
LOG_TAG="[bat-https]"

if [ "$(id -u)" != "0" ]; then
    echo "$LOG_TAG Phai chay bang root:  sudo /opt/htbb/vps/bat-https.sh"
    exit 1
fi
if ! command -v apache2 >/dev/null 2>&1; then
    echo "$LOG_TAG Khong thay apache2 tren may nay."
    exit 1
fi

IP_MAY=$(hostname -I 2>/dev/null | awk '{print $1}')
[ -z "$IP_MAY" ] && IP_MAY=$(curl -s --max-time 5 https://api.ipify.org 2>/dev/null)
[ -z "$IP_MAY" ] && IP_MAY=127.0.0.1

echo "$LOG_TAG === 1/5 Bat cac module can thiet"
a2enmod ssl rewrite headers >/dev/null 2>&1
echo "      ssl, rewrite, headers: xong"

# ---------------------------------------------------------------------------
#  2. Chung chi
# ---------------------------------------------------------------------------
DUNG_CERTBOT=0
if [ -n "$TEN_MIEN" ]; then
    echo "$LOG_TAG === 2/5 Xin chung chi Let's Encrypt cho $TEN_MIEN"
    if ! command -v certbot >/dev/null 2>&1; then
        echo "      Dang cai certbot..."
        apt-get update -qq && apt-get install -y -qq certbot python3-certbot-apache
    fi
    # Kiem tra ten mien co tro ve may nay chua. Certbot that bai giua duong hay de
    # lai cau hinh nua voi, kho don hon la dung truoc.
    IP_MIEN=$(getent hosts "$TEN_MIEN" 2>/dev/null | awk '{print $1}' | head -1)
    if [ -z "$IP_MIEN" ]; then
        echo "      [DUNG LAI] '$TEN_MIEN' khong phan giai ra dia chi nao."
        echo "                 Tro ten mien ve $IP_MAY roi cho vai phut, sau do chay lai."
        exit 1
    fi
    if [ "$IP_MIEN" != "$IP_MAY" ]; then
        echo "      [CANH BAO] '$TEN_MIEN' dang tro ve $IP_MIEN, khong phai $IP_MAY."
        echo "                 Let's Encrypt se tu choi. Sua DNS truoc roi chay lai."
        exit 1
    fi
    if certbot --apache -d "$TEN_MIEN" --non-interactive --agree-tos \
               --register-unsafely-without-email --redirect >/dev/null 2>&1; then
        DUNG_CERTBOT=1
        echo "      Da co chung chi that cho $TEN_MIEN, tu gia han qua certbot.timer."
    else
        echo "      [LOI] certbot that bai. Xem /var/log/letsencrypt/letsencrypt.log"
        echo "            Van tiep tuc bang chung chi tu ky."
        TEN_MIEN=""
    fi
fi

if [ "$DUNG_CERTBOT" = "0" ]; then
    echo "$LOG_TAG === 2/5 Tao chung chi tu ky cho $IP_MAY"
    mkdir -p "$CHUNG_CHI_DIR"
    chmod 700 "$CHUNG_CHI_DIR"
    if [ -f "$CHUNG_CHI_DIR/htbb.crt" ]; then
        echo "      Da co san $CHUNG_CHI_DIR/htbb.crt - dung lai, khong tao moi."
    else
        # subjectAltName la BAT BUOC. Trinh duyet hien dai bo qua hoan toan truong
        # CN cu; thieu SAN thi chung chi bi tu choi thang chu khong chi canh bao.
        openssl req -x509 -nodes -newkey rsa:2048 -days 3650 \
            -keyout "$CHUNG_CHI_DIR/htbb.key" \
            -out "$CHUNG_CHI_DIR/htbb.crt" \
            -subj "/CN=$IP_MAY" \
            -addext "subjectAltName=IP:$IP_MAY,DNS:localhost" >/dev/null 2>&1
        if [ ! -f "$CHUNG_CHI_DIR/htbb.crt" ]; then
            echo "      [LOI] Khong tao duoc chung chi."
            exit 1
        fi
        chmod 600 "$CHUNG_CHI_DIR/htbb.key"
        echo "      Da tao, han 10 nam: $CHUNG_CHI_DIR/htbb.crt"
    fi

    cat > "$CONF_SSL" <<EOF
# Sinh boi vps/bat-https.sh - dung sua tay, chay lai script se ghi de.
<IfModule mod_ssl.c>
<VirtualHost *:443>
    ServerAdmin webmaster@localhost
    # Khong DocumentRoot: moi request do ProxyPass (htbb-web-proxy.conf, tang
    # server, duoc vhost ke thua) chuyen sang Nuxt 127.0.0.1:3000.

    SSLEngine on
    SSLCertificateFile    $CHUNG_CHI_DIR/htbb.crt
    SSLCertificateKeyFile $CHUNG_CHI_DIR/htbb.key

    # Chi cho phep TLS 1.2 tro len. TLS 1.0/1.1 da bi coi la hong tu 2021.
    SSLProtocol -all +TLSv1.2 +TLSv1.3

    # KHONG dat Strict-Transport-Security o day: chung chi tu ky + HSTS = tu khoa
    # minh ra ngoai theo cach khong go duoc tu phia may chu. Xem dau tep.
    Header always set X-Content-Type-Options "nosniff"
    Header always set X-Frame-Options "SAMEORIGIN"
    Header always set Referrer-Policy "same-origin"

    ErrorLog \${APACHE_LOG_DIR}/htbb-ssl-error.log
    CustomLog \${APACHE_LOG_DIR}/htbb-ssl-access.log combined
</VirtualHost>
</IfModule>
EOF
    a2ensite htbb-ssl >/dev/null 2>&1
    echo "      Da them VirtualHost cong 443 (cong 80 giu nguyen)."
fi

# ---------------------------------------------------------------------------
#  3. Ep HTTPS chi cho /admin
# ---------------------------------------------------------------------------
echo "$LOG_TAG === 3/5 Ep HTTPS cho /admin"
mkdir -p "$THU_MUC_BAO_VE"
[ -f "$CONF_NAP" ] || printf '# Sinh boi vps/chi-cho-server-txt.sh / bat-https.sh / mat-khau-admin.sh.\n# Nap cac manh bao ve website (Nuxt sau reverse proxy) THEO SO THU TU - xem vps/chi-cho-server-txt.sh.\nIncludeOptional %s/*.conf\n' \
    "$THU_MUC_BAO_VE" > "$CONF_NAP"
cat > "$CONF_EP" <<'EOF'
# Sinh boi vps/bat-https.sh - dung sua tay, chay lai script se ghi de.
#
# Ep HTTPS CHI cho khu quan tri (/admin cua Nuxt). Trang cong khai cua nguoi choi
# van chay cong 80 nhu cu: voi chung chi tu ky, ep ca site la moi nguoi choi deu
# gap man hinh canh bao do cua trinh duyet.
#
# VI SAO <Location> + <If> + Redirect (khong phai <Directory> + RewriteRule nhu ban
# web PHP cu): request di qua ProxyPass khong gan voi thu muc nao tren dia, nen
# <Directory> khong bao gio khop; RewriteRule o tang server thi vhost KHONG ke
# thua (da vap: Syntax OK ma luat khong chay). <Location> thi ap dung ca cho
# request da proxy va duoc gop vao moi vhost. Redirect trong <Location> nhan bieu
# thuc ap_expr cho URL dich (Apache >= 2.4.19).
#
# Dieu kien %{HTTPS} != 'on' chan vong lap chuyen huong vo tan: vhost 443 cung
# gop tep nay. HAI NGOAI LE:
#  - Ket noi TCP tu IP goc cua Nova WAF (104.243.35.x / 104.243.37.x): Nova WAF
#    giai ma TLS o bien roi noi ve origin bang HTTP THUONG, ep https o day se tao
#    vong lap cho chinh no. Xet CONN_REMOTE_ADDR (IP cua ket noi) vi mod_remoteip
#    da thay REMOTE_ADDR bang IP that cua khach.
#  - HTTP_HOST bang dung IP may chu (180.93.57.30): duong vao thang bang IP tren
#    cong 80, ne Nova WAF, dung khi Nova WAF hong. Cong 443 chi mo cho Cloudflare/
#    Nova WAF/whitelist (xem /etc/htbb-cloudflare-web/apply.sh) nen ep ve https o
#    day se dua nguoi dung vao dung cai tuong lua do - mat luon con duong thoat.
<Location /admin>
    <If "%{HTTPS} != 'on' && req('X-Forwarded-Proto') != 'https' && ! (%{CONN_REMOTE_ADDR} =~ /^104\.243\.(35|37)\./) && %{HTTP_HOST} != '180.93.57.30'">
        Redirect permanent "https://%{HTTP_HOST}%{REQUEST_URI}"
    </If>
</Location>
EOF
# Conf cu thoi web PHP (<Directory /opt/htbb/web/admin>) - bo, thay bang manh o tren.
a2disconf htbb-admin-https >/dev/null 2>&1 || true
a2enconf htbb-web-bao-ve >/dev/null 2>&1
echo "      Xong ($CONF_EP). Trang cong khai cong 80 khong bi anh huong."

# ---------------------------------------------------------------------------
#  3b. Mo cong 443 tren tuong lua
# ---------------------------------------------------------------------------
# Thieu buoc nay thi Apache lang nghe 443 nhung khong ai ngoai Internet vao duoc,
# va trieu chung rat de doan sai: tu TRONG may curl https://localhost tra ve 200
# nen moi thu "trong nhu da chay". Da dinh dung cai nay mot lan.
if command -v ufw >/dev/null 2>&1 && ufw status 2>/dev/null | grep -q "^Status: active"; then
    if ufw status 2>/dev/null | grep -qE "^443/tcp\s+ALLOW"; then
        echo "      Tuong lua: 443/tcp da mo san."
    else
        ufw allow 443/tcp >/dev/null 2>&1
        echo "      Tuong lua: da mo 443/tcp."
    fi
fi

# ---------------------------------------------------------------------------
#  4. Kiem tra cau hinh TRUOC khi nap lai
# ---------------------------------------------------------------------------
echo "$LOG_TAG === 4/5 Kiem tra cau hinh Apache"
if ! apache2ctl configtest 2>&1 | grep -qi "Syntax OK"; then
    echo "      [DUNG LAI] Cau hinh Apache co loi, KHONG nap lai:"
    apache2ctl configtest 2>&1 | sed 's/^/        /'
    echo
    echo "      Apache van dang chay cau hinh cu, trang web khong bi anh huong."
    echo "      Go bo: rm $CONF_EP; a2dissite htbb-ssl"
    exit 1
fi
echo "      Syntax OK"

echo "$LOG_TAG === 5/5 Nap lai Apache (reload, khong dut ket noi)"
systemctl reload apache2
sleep 1

# ---------------------------------------------------------------------------
#  Bao ket qua
# ---------------------------------------------------------------------------
echo
echo "============================================================"
if ss -lnt | grep -q ':443 '; then
    echo "  XONG - cong 443 dang lang nghe"
else
    echo "  [CANH BAO] Chua thay cong 443. Xem: journalctl -u apache2 -n 30"
fi
echo "============================================================"
if [ "$DUNG_CERTBOT" = "1" ]; then
    echo "  Vao admin:  https://$TEN_MIEN/admin/"
    echo "  Chung chi that, trinh duyet khong canh bao, tu gia han."
else
    echo "  Vao admin:  https://$IP_MAY/admin/"
    echo
    echo "  LAN DAU trinh duyet se canh bao \"ket noi khong an toan\" - do la vi"
    echo "  chung chi tu ky, KHONG phai vi bi tan cong. Bam Nang cao -> Tiep tuc."
    echo "  Tu do mat khau di qua duong da ma hoa."
    echo
    echo "  Muon het canh bao: tro mot ten mien ve $IP_MAY (ten mien mien phi nhu"
    echo "  DuckDNS cung duoc) roi chay lai:"
    echo "      sudo /opt/htbb/vps/bat-https.sh ten.mien.cua.ban"
fi
echo
echo "  Trang cong khai cho nguoi choi: http://$IP_MAY/  (khong doi gi)"
echo "============================================================"
