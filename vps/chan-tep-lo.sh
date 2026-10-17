#!/bin/bash
# ============================================================================
#  CHAN NHUNG TEP KHONG BAO GIO DUOC PHUC VU QUA HTTP
#
#      sudo /opt/kpah/vps/chan-tep-lo.sh
#
# ----------------------------------------------------------------------------
#  CHUYEN THAT DA XAY RA (31/08/2026)
# ----------------------------------------------------------------------------
#  Tren VPS co mot tep `web/config.local.php.save` - ban sao luu do trinh bien
#  tap nano de lai. Doc tu ngoai Internet:
#
#      GET /kpah/config.local.php.save  ->  200, 1685 byte VAN BAN THO
#
#  Apache chi dua duoi `.php` cho PHP xu ly. Duoi `.save` thi no coi la tep tinh
#  va PHUC VU NGUYEN MA NGUON - trong do co mat khau CSDL, so tai khoan ngan hang,
#  mat khau nap tien va ten tai khoan admin. Tep nam do 18 tieng.
#
#  May man la doi chieu log Apache (luu tu 17/08, phu kin ca khoang thoi gian do)
#  chi thay hai IP tung goi: chinh may cua chu server va 127.0.0.1. Khong ai ngoai
#  lay duoc. Nhung do la may man, khong phai thiet ke.
#
#  Bai hoc: don mot lan la khong du. Lan sau sua config bang nano/vim tren VPS ma
#  bam Ctrl+C giua duong la lai co `.save` / `.swp` moi. Nen chot phai la CAU HINH
#  APACHE - tep rac co xuat hien thi cung khong ai doc duoc.
#
#  KHONG dat duoc bang .htaccess: apache2.conf tren may nay dat `AllowOverride None`
#  cho khoi <Directory /var/www/>, nen tep .htaccess bi bo qua hoan toan. Dat vao
#  chi tao cam giac an toan gia.
#
#  Go bo:  sudo a2disconf kpah-chan-tep-lo && sudo systemctl reload apache2
# ============================================================================
set -u

CONF=/etc/apache2/conf-available/kpah-chan-tep-lo.conf
LOG_TAG="[chan-tep-lo]"

if [ "$(id -u)" != "0" ]; then
    echo "$LOG_TAG Phai chay bang root:  sudo /opt/kpah/vps/chan-tep-lo.sh"
    exit 1
fi

echo "$LOG_TAG === 1/3 Viet luat chan"
cat > "$CONF" <<'EOF'
# Sinh boi vps/chan-tep-lo.sh - dung sua tay, chay lai script se ghi de.
#
# Chan theo TEN TEP, ap dung cho moi thu muc duoc phuc vu.
#
# Vi sao chan ca `config.local.php`: no chi duoc PHP `require` tu dia, khong bao
# gio can lay qua HTTP. Hien tai goi vao tra ve 200 rong (PHP chay nhung khong in
# gi) - nghe thi vo hai, nhung do la vi PHP dang chay. Ngay nao module PHP hong
# hoac bi tat, Apache se phuc vu nguyen ma nguon kem mat khau. Chan san thi khong
# phu thuoc vao chuyen do nua. `Require all denied` chi chan yeu cau HTTP, KHONG
# anh huong den viec PHP doc tep bang require/include.
<FilesMatch "(?i)^config\.local\.php">
    Require all denied
</FilesMatch>

# Tep rac cua trinh bien tap va ban sao luu. Day la nhom da gay ro that.
<FilesMatch "(?i)\.(save|bak|backup|old|orig|rej|swp|swo|swn|tmp|dist|example|tmpl|inc|ini|log|sql|sh|env|key|pem|crt)$">
    Require all denied
</FilesMatch>

# nano/emacs de lai tep ket thuc bang dau ~ ; vim de lai .un~ ; mot so cong cu
# dung tien to #...#
<FilesMatch "(~|#)$">
    Require all denied
</FilesMatch>
<FilesMatch "^#">
    Require all denied
</FilesMatch>

# Moi tep bat dau bang dau cham: .env, .git*, .htpasswd, .DS_Store...
<FilesMatch "^\.">
    Require all denied
</FilesMatch>

# Thu muc siêu du lieu cua he quan ly ma nguon. Hien tai .git nam o /opt/kpah,
# tuc NGOAI web root (/opt/kpah/web) nen khong voi tới duoc - nhung chi can mot
# lan doi DocumentRoot la ca lich su ma nguon thanh cong khai.
<DirectoryMatch "/\.(git|svn|hg|idea|vscode)(/|$)">
    Require all denied
</DirectoryMatch>
EOF

a2enconf kpah-chan-tep-lo >/dev/null 2>&1
echo "      Da bat kpah-chan-tep-lo"

echo "$LOG_TAG === 2/3 Kiem tra cau hinh"
if ! apache2ctl configtest 2>&1 | grep -qi "Syntax OK"; then
    echo "      [DUNG LAI] Cau hinh co loi, KHONG nap lai:"
    apache2ctl configtest 2>&1 | sed 's/^/        /'
    echo "      Apache van chay cau hinh cu. Go bo: a2disconf kpah-chan-tep-lo"
    exit 1
fi
echo "      Syntax OK"

echo "$LOG_TAG === 3/3 Nap lai Apache"
systemctl reload apache2
sleep 1

# ---------------------------------------------------------------------------
#  Tu kiem: dung tep that de thu, roi don di
# ---------------------------------------------------------------------------
# Kiem bang mot tep THAT chu khong tin vao cau hinh: mot luat viet dung cu phap
# van co the khong khop duong nao ca (da dinh dung chuyen do voi luat rewrite o
# tang server - configtest bao OK ma luat khong he chay).
echo
echo "$LOG_TAG Tu kiem bang tep that"
GOC=/opt/kpah/web
THU="$GOC/htbb-thu-chan-$$.save"
echo "khong duoc doc thay dong nay" > "$THU"
MA=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1/kpah/$(basename "$THU")" 2>/dev/null)
rm -f "$THU"
if [ "$MA" = "403" ] || [ "$MA" = "404" ]; then
    echo "      OK  - tep .save tra ve $MA (bi chan)"
else
    echo "      [CANH BAO] tep .save tra ve $MA - luat CHUA khop!"
    echo "                 Xem lai $CONF"
    exit 1
fi

echo
echo "============================================================"
echo "  XONG. Tu gio tep .save / .bak / .swp / dotfile trong web/"
echo "  co xuat hien cung khong ai doc duoc qua HTTP."
echo "============================================================"
