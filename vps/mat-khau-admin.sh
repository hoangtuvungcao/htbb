#!/bin/bash
# ============================================================================
#  MAT KHAU RIENG CHO KHU QUAN TRI (HTTP Basic Auth truoc man hinh dang nhap)
#
#      sudo /opt/kpah/vps/mat-khau-admin.sh
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
#  Basic Auth cua chinh Apache, dat NGOAI DocumentRoot, truoc ca trang dang
#  nhap admin (taikhoan.php cua khu quan tri). Ai khong biet mat khau nay thi
#  KHONG THAY duoc ca man hinh dang nhap admin, du dung IP nao / domain nao.
#  Biet mat khau nay roi van phai dang nhap dung tai khoan + mat khau game
#  (+ ma 2FA neu da bat) o lop thu hai (trang dang nhap that cua taikhoan.php)
#  - hai mat khau HOAN TOAN khac nhau, doi rieng, mat mot cai khong lo cai kia.
#
# ----------------------------------------------------------------------------
#  VI SAO LOP NAY THAY THE DUOC WHITELIST IP CUA chi-cho-server-txt.sh
# ----------------------------------------------------------------------------
#  <Directory /opt/kpah/web/admin> cu the hon <Directory /opt/kpah/web>. Theo
#  mac dinh cua Apache (AuthMerging Off), <Directory> cu the hon tu dinh nghia
#  Require rieng se THAY THE HOAN TOAN dieu kien Require cua <Directory> cha
#  cho nhung duong dan nam trong no - khong cong don voi nhau. Nghia la rieng
#  /admin/ se KHONG con bi chan theo IP nua, chi con bi chan boi Basic Auth o
#  day. Cac duong dan khac (dang ky, dang nhap, trang chu server.txt...) khong
#  doi gi, van theo dung whitelist IP nhu cu trong chi-cho-server-txt.sh.
#
#  Day la file conf-available RIENG (khac vps/chi-cho-server-txt.sh), nhung
#  KHONG dinh bay tro lai "BAY DA GAP 09/09/2026" cua file do: bay do xay ra
#  vi hai file cung dinh nghia <Directory> TRUNG DUONG DAN. O day hai file
#  dinh nghia hai duong dan KHAC NHAU (/opt/kpah/web vs /opt/kpah/web/admin),
#  Apache gop theo do cu the cua duong dan (khong theo thu tu nap file), nen
#  tach rieng an toan.
#
# ----------------------------------------------------------------------------
#  DOI MAT KHAU / THEM NGUOI
# ----------------------------------------------------------------------------
#  Chay lai script nay se HOI mat khau moi va GHI DE nguoi dung duy nhat
#  "kpah" (mot mat khau dung chung cho ca nhom, dung y ban da chon). Muon
#  nhieu nguoi dung ten rieng (de sau nay biet ai vao, xoa duoc rieng tung
#  nguoi) thi tu chay them:
#      htpasswd /etc/apache2/kpah-admin.htpasswd ten_nguoi_thu_2
#  roi neu muon GIOI HAN dung vai ten cu the (thay vi "ai co trong tep deu
#  vao duoc") thi doi "Require valid-user" trong CONF ben duoi thanh:
#      Require user kpah ten_nguoi_thu_2
#
# ----------------------------------------------------------------------------
#  GO BO NEU CO CHUYEN
# ----------------------------------------------------------------------------
#      sudo a2disconf kpah-admin-basic-auth && sudo systemctl reload apache2
#      sudo rm -f /etc/apache2/kpah-admin.htpasswd   # xoa luon mat khau da luu
# ============================================================================
set -u

CONF=/etc/apache2/conf-available/kpah-admin-basic-auth.conf
HTPASSWD_FILE=/etc/apache2/kpah-admin.htpasswd
TEN_BASIC_AUTH="kpah"
LOG_TAG="[mat-khau-admin]"

if [ "$(id -u)" != "0" ]; then
    echo "$LOG_TAG Phai chay bang root:  sudo /opt/kpah/vps/mat-khau-admin.sh"
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
cat > "$CONF" <<EOF
# Sinh boi vps/mat-khau-admin.sh - dung sua tay, chay lai script se hoi mat
# khau moi va ghi de tep nay.
#
# Ghi ca hai duong dan vi /var/www/html/kpah la symlink tro toi /opt/kpah/web
# (xem vps/bat-https.sh vi sao ghi ca hai duong dan cho chac, khong phu thuoc
# vao viec Apache tu giai symlink truoc khi so khop <Directory> hay khong).
<Directory /opt/kpah/web/admin>
    AuthType Basic
    AuthName "HTBB - Khu quan tri"
    AuthUserFile $HTPASSWD_FILE
    Require valid-user
</Directory>
<Directory /var/www/html/kpah/admin>
    AuthType Basic
    AuthName "HTBB - Khu quan tri"
    AuthUserFile $HTPASSWD_FILE
    Require valid-user
</Directory>
EOF

a2enmod auth_basic authn_file authz_user authz_core >/dev/null 2>&1
a2enconf kpah-admin-basic-auth >/dev/null 2>&1
echo "      Da bat kpah-admin-basic-auth"

echo "$LOG_TAG === 4/5 Kiem tra cau hinh"
if ! apache2ctl configtest 2>&1 | grep -qi "Syntax OK"; then
    echo "      [DUNG LAI] Cau hinh co loi, KHONG nap lai:"
    apache2ctl configtest 2>&1 | sed 's/^/        /'
    echo "      Apache van chay cau hinh cu. Go bo: a2disconf kpah-admin-basic-auth"
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
