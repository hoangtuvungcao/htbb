#!/bin/bash
# ============================================================================
#  MAT KHAU RIENG CHO KHU QUAN TRI (HTTP Basic Auth truoc man hinh dang nhap)
#
#      sudo /opt/htbb/vps/mat-khau-admin.sh
#
# ----------------------------------------------------------------------------
#  VI SAO CAN
# ----------------------------------------------------------------------------
#  IP cua nguoi quan tri doi lien tuc (mang di dong/gia dinh), nen KHONG the
#  dung whitelist theo IP (vps/chi-cho-server-txt.sh) de gioi han ai vao duoc
#  /admin/ - IP doi la tu khoa chinh minh, con IP cu thi ai dung lai cung vao
#  duoc, khong con la "chi minh".
#
#  Doi sang chan THEO NGUOI thay vi THEO NOI DUNG (IP): them mot lop mat khau
#  Basic Auth cua chinh Apache (reverse proxy dung truoc Nuxt), truoc ca trang
#  dang nhap admin (/admin cua HTBB-Web). Ai khong biet mat khau nay thi
#  KHONG THAY duoc ca man hinh dang nhap admin, du dung IP nao / domain nao.
#  Biet mat khau nay roi van phai dang nhap dung tai khoan + mat khau game
#  (+ ma 2FA neu da bat) o lop thu hai (trang dang nhap that cua /admin)
#  - hai mat khau HOAN TOAN khac nhau, doi rieng, mat mot cai khong lo cai kia.
#
# ----------------------------------------------------------------------------
#  VI SAO LOP NAY THAY THE DUOC WHITELIST IP CUA chi-cho-server-txt.sh
# ----------------------------------------------------------------------------
#  Mac dinh Apache (AuthMerging Off): <Location /admin> nap SAU <Location /> va
#  tu dinh nghia Require rieng se THAY THE HOAN TOAN Require cua "/" cho moi
#  duong dan /admin/... - khong cong don. Nghia la rieng /admin se KHONG con bi
#  chan theo IP, chi con Basic Auth o day. Cac duong dan khac van theo whitelist
#  cua chi-cho-server-txt.sh.
#
#  <Location> gop THEO THU TU NAP (khong theo do cu the nhu <Directory> cua ban
#  web PHP cu, da bo 19/10/2026), nen tep nay ghi MANH 30-admin-basic-auth.conf
#  vao /etc/apache2/htbb-web-bao-ve.d/ - so 30 bao dam nap SAU
#  10-chi-cho-server-txt.conf (xem "BAY DA GAP" trong vps/chi-cho-server-txt.sh).
#
#  CHI /admin (trang HTML cua Nuxt), KHONG dat cho /api/admin: API admin dung
#  header "Authorization: Bearer <token>", trung header voi Basic Auth nen se bi
#  Apache tu choi (xem HTBB-Web/deploy/apache-htbb-web.conf). Quyen that cua API
#  admin do Node kiem (token admin + HTBB_ADMIN_ACCOUNTS).
#
# ----------------------------------------------------------------------------
#  DOI MAT KHAU / THEM NGUOI
# ----------------------------------------------------------------------------
#  Chay lai script nay se HOI mat khau moi va GHI DE nguoi dung duy nhat
#  "htbb" (mot mat khau dung chung cho ca nhom, dung y ban da chon). Muon
#  nhieu nguoi dung ten rieng (de sau nay biet ai vao, xoa duoc rieng tung
#  nguoi) thi tu chay them:
#      htpasswd /etc/apache2/htbb-admin.htpasswd ten_nguoi_thu_2
#  roi neu muon GIOI HAN dung vai ten cu the (thay vi "ai co trong tep deu
#  vao duoc") thi doi "Require valid-user" trong CONF ben duoi thanh:
#      Require user htbb ten_nguoi_thu_2
#
# ----------------------------------------------------------------------------
#  GO BO NEU CO CHUYEN
# ----------------------------------------------------------------------------
#      sudo rm /etc/apache2/htbb-web-bao-ve.d/30-admin-basic-auth.conf && sudo systemctl reload apache2
#      sudo rm -f /etc/apache2/htbb-admin.htpasswd   # xoa luon mat khau da luu
# ============================================================================
set -u

THU_MUC_BAO_VE=/etc/apache2/htbb-web-bao-ve.d
CONF="$THU_MUC_BAO_VE/30-admin-basic-auth.conf"
CONF_NAP=/etc/apache2/conf-available/htbb-web-bao-ve.conf
HTPASSWD_FILE=/etc/apache2/htbb-admin.htpasswd
TEN_BASIC_AUTH="htbb"
LOG_TAG="[mat-khau-admin]"

if [ "$(id -u)" != "0" ]; then
    echo "$LOG_TAG Phai chay bang root:  sudo /opt/htbb/vps/mat-khau-admin.sh"
    exit 1
fi

echo "$LOG_TAG === 1/5 Kiem tra / cai htpasswd"
if ! command -v htpasswd >/dev/null 2>&1; then
    echo "      Dang cai apache2-utils..."
    apt-get update -qq && apt-get install -y -qq apache2-utils
fi

echo "$LOG_TAG === 2/5 Dat mat khau cho '$TEN_BASIC_AUTH'"
echo "      Mat khau nay KHAC HOAN TOAN mat khau tai khoan game - dung rieng,"
echo "      chi de QUA DUOC man hinh dang nhap admin, chua phai dang nhap that."
while true; do
    read -r -s -p "      Nhap mat khau moi: " MK1; echo
    read -r -s -p "      Go lai lan nua:    " MK2; echo
    if [ -z "$MK1" ]; then
        echo "      Mat khau rong, nhap lai."
        continue
    fi
    if [ "$MK1" != "$MK2" ]; then
        echo "      Hai lan go khong khop, nhap lai."
        continue
    fi
    break
done
printf '%s' "$MK1" | htpasswd -ci "$HTPASSWD_FILE" "$TEN_BASIC_AUTH"
unset MK1 MK2
chown root:www-data "$HTPASSWD_FILE"
chmod 640 "$HTPASSWD_FILE"
echo "      Da luu vao $HTPASSWD_FILE (chi root va www-data doc duoc)"

echo "$LOG_TAG === 3/5 Viet cau hinh"
mkdir -p "$THU_MUC_BAO_VE"
[ -f "$CONF_NAP" ] || printf '# Sinh boi vps/chi-cho-server-txt.sh / bat-https.sh / mat-khau-admin.sh.\n# Nap cac manh bao ve website (Nuxt sau reverse proxy) THEO SO THU TU - xem vps/chi-cho-server-txt.sh.\nIncludeOptional %s/*.conf\n' \
    "$THU_MUC_BAO_VE" > "$CONF_NAP"
cat > "$CONF" <<EOF
# Sinh boi vps/mat-khau-admin.sh - dung sua tay, chay lai script se hoi mat
# khau moi va ghi de tep nay. Nap SAU 10-chi-cho-server-txt.conf (xem dau
# vps/chi-cho-server-txt.sh vi sao thu tu quan trong).
<Location /admin>
    AuthType Basic
    AuthName "HTBB - Khu quan tri"
    AuthUserFile $HTPASSWD_FILE
    Require valid-user
</Location>
EOF

a2enmod auth_basic authn_file authz_user authz_core >/dev/null 2>&1
# Conf cu thoi web PHP (<Directory /opt/htbb/web/admin>) - bo, thay bang manh o tren.
a2disconf htbb-admin-basic-auth >/dev/null 2>&1 || true
a2enconf htbb-web-bao-ve >/dev/null 2>&1
echo "      Da bat $CONF"

echo "$LOG_TAG === 4/5 Kiem tra cau hinh"
if ! apache2ctl configtest 2>&1 | grep -qi "Syntax OK"; then
    echo "      [DUNG LAI] Cau hinh co loi, KHONG nap lai:"
    apache2ctl configtest 2>&1 | sed 's/^/        /'
    echo "      Apache van chay cau hinh cu. Go bo: rm $CONF"
    exit 1
fi
echo "      Syntax OK"

echo "$LOG_TAG === 5/5 Nap lai Apache"
systemctl reload apache2
sleep 1

echo
echo "$LOG_TAG Tu kiem: goi /admin/ khong kem mat khau phai bi hoi (401)"
MA=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1/admin/" 2>/dev/null)
if [ "$MA" = "401" ]; then
    echo "      OK  - /admin/ tra ve 401 (dang doi Basic Auth)"
else
    echo "      [CANH BAO] /admin/ tra ve $MA (can 401) - xem lai $CONF"
    exit 1
fi

echo
echo "============================================================"
echo "  XONG. Tu gio vao /admin/ tu BAT KY IP nao cung se bi trinh duyet hoi"
echo "  mot o dang nhap rieng (khac han dang nhap tai khoan game) - go dung"
echo "  '$TEN_BASIC_AUTH' / mat khau vua dat thi moi THAY duoc man hinh dang"
echo "  nhap admin that, roi dang nhap tiep bang tai khoan game nhu cu."
echo
echo "  Whitelist IP trong chi-cho-server-txt.sh KHONG con anh huong den"
echo "  /admin/ nua (van con hieu luc cho cac trang khac nhu dang ky, trang chu)."
echo "============================================================"
