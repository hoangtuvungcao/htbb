#!/bin/bash
# ============================================================================
#  BAT HTTPS CHO TRANG QUAN TRI
#
#      sudo /opt/kpah/vps/bat-https.sh                  # chung chi tu ky (theo IP)
#      sudo /opt/kpah/vps/bat-https.sh admin.abc.com    # chung chi that (Let's Encrypt)
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
#   3. Them mot VirtualHost cong 443, giu NGUYEN cong 80.
#   4. Ep HTTPS CHI cho /kpah/admin.
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
#      sudo a2disconf kpah-admin-https
#      sudo a2dissite kpah-ssl
#      sudo systemctl reload apache2
#  Cong 80 chua bao gio bi tat nen trang web van chay trong suot qua trinh nay.
# ============================================================================
set -u

TEN_MIEN="${1:-}"
CONF_SSL=/etc/apache2/sites-available/kpah-ssl.conf
CONF_EP=/etc/apache2/conf-available/kpah-admin-https.conf
CHUNG_CHI_DIR=/etc/ssl/kpah
LOG_TAG="[bat-https]"

if [ "$(id -u)" != "0" ]; then
    echo "$LOG_TAG Phai chay bang root:  sudo /opt/kpah/vps/bat-https.sh"
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
    if [ -f "$CHUNG_CHI_DIR/kpah.crt" ]; then
        echo "      Da co san $CHUNG_CHI_DIR/kpah.crt - dung lai, khong tao moi."
    else
        # subjectAltName la BAT BUOC. Trinh duyet hien dai bo qua hoan toan truong
        # CN cu; thieu SAN thi chung chi bi tu choi thang chu khong chi canh bao.
        openssl req -x509 -nodes -newkey rsa:2048 -days 3650 \
            -keyout "$CHUNG_CHI_DIR/kpah.key" \
            -out "$CHUNG_CHI_DIR/kpah.crt" \
            -subj "/CN=$IP_MAY" \
            -addext "subjectAltName=IP:$IP_MAY,DNS:localhost" >/dev/null 2>&1
        if [ ! -f "$CHUNG_CHI_DIR/kpah.crt" ]; then
            echo "      [LOI] Khong tao duoc chung chi."
            exit 1
        fi
        chmod 600 "$CHUNG_CHI_DIR/kpah.key"
        echo "      Da tao, han 10 nam: $CHUNG_CHI_DIR/kpah.crt"
    fi

    cat > "$CONF_SSL" <<EOF
# Sinh boi vps/bat-https.sh - dung sua tay, chay lai script se ghi de.
<IfModule mod_ssl.c>
<VirtualHost *:443>
    ServerAdmin webmaster@localhost
    DocumentRoot /var/www/html

    SSLEngine on
    SSLCertificateFile    $CHUNG_CHI_DIR/kpah.crt
    SSLCertificateKeyFile $CHUNG_CHI_DIR/kpah.key

    # Chi cho phep TLS 1.2 tro len. TLS 1.0/1.1 da bi coi la hong tu 2021.
    SSLProtocol -all +TLSv1.2 +TLSv1.3

    # KHONG dat Strict-Transport-Security o day: chung chi tu ky + HSTS = tu khoa
    # minh ra ngoai theo cach khong go duoc tu phia may chu. Xem dau tep.
    Header always set X-Content-Type-Options "nosniff"
    Header always set X-Frame-Options "SAMEORIGIN"
    Header always set Referrer-Policy "same-origin"

    ErrorLog \${APACHE_LOG_DIR}/kpah-ssl-error.log
    CustomLog \${APACHE_LOG_DIR}/kpah-ssl-access.log combined
</VirtualHost>
</IfModule>
EOF
    a2ensite kpah-ssl >/dev/null 2>&1
    echo "      Da them VirtualHost cong 443 (cong 80 giu nguyen)."
fi

# ---------------------------------------------------------------------------
#  3. Ep HTTPS chi cho /kpah/admin
# ---------------------------------------------------------------------------
echo "$LOG_TAG === 3/5 Ep HTTPS cho /kpah/admin"
cat > "$CONF_EP" <<'EOF'
# Sinh boi vps/bat-https.sh - dung sua tay, chay lai script se ghi de.
#
# Ep HTTPS CHI cho khu quan tri. Trang cong khai cua nguoi choi van chay cong 80
# nhu cu: voi chung chi tu ky, ep ca site la moi nguoi choi deu gap man hinh canh
# bao do cua trinh duyet va phan lon se tuong server bi hack roi bo di.
#
# ---------------------------------------------------------------------------
#  VI SAO LUAT NAY NAM TRONG <Directory> CHU KHONG O TANG SERVER
# ---------------------------------------------------------------------------
#  Ban dau viet o tang server (RewriteRule ^/kpah/admin ...). Apache nap tep,
#  configtest bao Syntax OK, khong mot dong canh bao nao - va luat KHONG HE CHAY.
#  Ly do: luat rewrite o tang server MAC DINH KHONG duoc VirtualHost ke thua
#  (phai co RewriteOptions Inherit trong tung vhost). Moi yeu cau that deu do
#  <VirtualHost *:80> phuc vu, nen luat o ngoai bi bo qua hoan toan.
#
#  <Directory> thi khac: no duoc gop vao moi vhost, nen dat o day la chay that.
#  Da kiem tu ngoai Internet: http .../kpah/admin/ phai tra ve 301.
#
#  Ghi ca hai duong dan vi /var/www/html/kpah la symlink tro toi /opt/kpah/web:
#  Apache so khop <Directory> theo duong dan THAT sau khi giai symlink, nhung ghi
#  ca hai thi khong phu thuoc vao chuyen do nua.
#
#  Trong <Directory>, mau "^" khop moi thu, va %{REQUEST_URI} van la duong dan
#  day du ke ca phan thu muc - nen khong phai dung lai tien to bang tay.
#
#  Dieu kien %{HTTPS} != on chan vong lap chuyen huong vo tan: VirtualHost 443
#  cung gop tep nay.
<IfModule mod_rewrite.c>
    <Directory /opt/kpah/web/admin>
        RewriteEngine On
        RewriteCond %{HTTPS} !=on
        RewriteRule ^ https://%{HTTP_HOST}%{REQUEST_URI} [R=301,L]
    </Directory>
    <Directory /var/www/html/kpah/admin>
        RewriteEngine On
        RewriteCond %{HTTPS} !=on
        RewriteRule ^ https://%{HTTP_HOST}%{REQUEST_URI} [R=301,L]
    </Directory>
</IfModule>
EOF
a2enconf kpah-admin-https >/dev/null 2>&1
echo "      Xong. Trang cong khai cong 80 khong bi anh huong."

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
    echo "      Go bo: a2disconf kpah-admin-https; a2dissite kpah-ssl"
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
    echo "  Vao admin:  https://$TEN_MIEN/kpah/admin/"
    echo "  Chung chi that, trinh duyet khong canh bao, tu gia han."
else
    echo "  Vao admin:  https://$IP_MAY/kpah/admin/"
    echo
    echo "  LAN DAU trinh duyet se canh bao \"ket noi khong an toan\" - do la vi"
    echo "  chung chi tu ky, KHONG phai vi bi tan cong. Bam Nang cao -> Tiep tuc."
    echo "  Tu do mat khau di qua duong da ma hoa."
    echo
    echo "  Muon het canh bao: tro mot ten mien ve $IP_MAY (ten mien mien phi nhu"
    echo "  DuckDNS cung duoc) roi chay lai:"
    echo "      sudo /opt/kpah/vps/bat-https.sh ten.mien.cua.ban"
fi
echo
echo "  Trang cong khai cho nguoi choi: http://$IP_MAY/kpah/  (khong doi gi)"
echo "============================================================"
