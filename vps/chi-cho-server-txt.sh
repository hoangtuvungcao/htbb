#!/bin/bash
# ============================================================================
#  KHOA WEB CONG KHAI: CHI CON /server.txt DUOC PHUC VU
#
#      sudo /opt/kpah/vps/chi-cho-server-txt.sh
#
# ----------------------------------------------------------------------------
#  HAI LOP CHAN, GOP CHUNG MOT <Directory> (xem "BAY DA GAP" ben duoi vi sao)
# ----------------------------------------------------------------------------
#  1. IP LA goi THANG vao cong 80 bang IP that cua VPS (khong qua Cloudflare,
#     khong nam trong whitelist). Domain "sv.kpah-private.site" la DNS-only
#     (KHONG qua Cloudflare proxy - goi mien phi khong proxy duoc cong game
#     TCP 19129, nen domain nay phai tro thang ve IP that). Ket qua: ai cung
#     goi thang duoc vao cong 80 bang IP that, khong qua loc nao ca.
#
#  2. DOMAIN CONG KHAI "kpah-private.site" (+ "www.") - trang web chinh cho
#     nguoi choi (dang ky/dang nhap/trang chu), proxy qua Cloudflare. Them tu
#     11/09/2026: website nay DONG CUA cho khach thuong - chi /server.txt con
#     mo, moi man hinh khac (dac biet la dang ky) tra ve 403 du goi qua
#     Cloudflare. Admin/doi tac (whitelist ben duoi) van vao day du moi trang,
#     moi domain nhu cu - khong bi anh huong.
#
#  Ca hai truong hop deu chan tai tang Apache: CHI /server.txt duoc phep cho
#  khach thuong - moi thu khac (trang chu /kpah/, dang ky, dang nhap, trang
#  quan tri, moi file tinh khac) tra ve 403.
#
# ----------------------------------------------------------------------------
#  BAY DA GAP (09/09/2026) - DUNG TACH RA <Directory> RIENG
# ----------------------------------------------------------------------------
#  Apache nap conf-enabled THEO THU TU BANG CHU CAI. Neu tach rieng thanh
#  "kpah-server-txt-only.conf", no nap TRUOC file nay ("s" truoc "w") - va
#  "Require all granted" trong kpah-webroot.conf nap SAU se GHI DE mat gioi
#  han. apache2ctl configtest bao "Syntax OK" binh thuong, khong canh bao gi -
#  loi chi lo ra khi test that bang curl. Phai gop chung vao MOT
#  <Directory /opt/kpah/web> duy nhat moi an toan - ke ca luat chan domain
#  cong khai them vao ngay 11/09/2026.
#
#  Danh sach IP duoi day PHAI khop voi /etc/kpah-cloudflare-web/apply.sh (dai
#  Cloudflare + "Nova WAF origin IPs"). Doi mot ben ma quen doi ben kia la lo
#  hong hoac tu khoa nham chinh minh.
#
#  Go bo ca hai lop: sudo a2disconf kpah-webroot && sudo systemctl reload apache2
#  (luu y: kpah-webroot dang cap quyen doc cho ca DocumentRoot - tat no la
#  Apache tro ve mac dinh "Forbidden" cho toan bo /opt/kpah/web)
#
# ----------------------------------------------------------------------------
#  MUON MO LAI DANG KY CHO KHACH THUONG (chi bo lop 2, giu nguyen lop 1)
# ----------------------------------------------------------------------------
#  Sua CONF ben duoi: xoa khoi <RequireAll> boc quanh <RequireAny> Cloudflare
#  va dong "Require expr ... HTTP_HOST ..." di kem, chi giu lai <RequireAny>
#  Cloudflare nhu ban truoc 11/09/2026 (xem git log --follow tep nay), roi
#  chay lai script.
#
# ----------------------------------------------------------------------------
#  VI SAO GHI CHET TEN MIEN "kpah-private.site" TRONG CONF (khong dung bien)
# ----------------------------------------------------------------------------
#  CONF duoc sinh bang heredoc <<'EOF' (TRICH DAN) de danh sach IP dai giu
#  duoc dau "\" xuong dong y nguyen - heredoc KHONG trich dan se NUOT mat cap
#  "\<xuong dong>" (da tu kiem chung: ca "\" don va "\\" kep deu bi rut gon,
#  khong con cach nao giu duoc "\" that trong output ma van cho phep the bien).
#  Vi vay ten mien ghi thang, KHONG the dung "${BIEN}" trong khoi CONF nay.
#  Doi ten mien: sua truc tiep hai cho "kpah-private.site" trong CONF ben duoi.
# ============================================================================
set -u

CONF=/etc/apache2/conf-available/kpah-webroot.conf
LOG_TAG="[chi-cho-server-txt]"

if [ "$(id -u)" != "0" ]; then
    echo "$LOG_TAG Phai chay bang root:  sudo /opt/kpah/vps/chi-cho-server-txt.sh"
    exit 1
fi

echo "$LOG_TAG === 1/4 Viet cau hinh"
cat > "$CONF" <<'EOF'
# Sinh boi vps/chi-cho-server-txt.sh - dung sua tay, chay lai script se ghi de.
#
# Cap quyen cho DocumentRoot moi (/opt/kpah/web), thay the /var/www/ da bo dung.
#
# Cong 80 mo cho MOI IP o tang firewall (KPAH_CF_WEB chi con giu cong 443) de
# client game tai duoc /server.txt qua domain "sv.kpah-private.site" (DNS only,
# khong qua Cloudflare - Cloudflare proxy khong chuyen duoc cong game 19129 nen
# domain nay PHAI di thang, khong proxy duoc qua Cloudflare o tang HTTP nua).
# Chan lai o day: IP la CHI duoc /server.txt.
#
# Them lop thu hai (11/09/2026): domain cong khai kpah-private.site (+ www.)
# cung CHI con /server.txt cho khach thuong du goi qua Cloudflare - website
# dang dong cua, khong nhan dang ky/dang nhap moi. Admin/doi tac (whitelist
# ben duoi) khong bi anh huong, van vao duoc moi trang moi domain nhu cu.
#
# Dung tach mot <Directory> rieng ("kpah-server-txt-only.conf") - conf-enabled
# nap theo thu tu BANG CHU CAI, file do ("s") nap TRUOC file nay ("w"), nen
# "Require all granted" ben duoi se GHI DE mat quyen han che. Phai gop chung
# vao MOT <Directory> duy nhat o day moi an toan.
<Directory /opt/kpah/web>
    Options -Indexes +FollowSymLinks
    AllowOverride None
    <RequireAny>
        Require expr "%{REQUEST_URI} == '/server.txt'"

        # Whitelist admin/doi tac (Nova WAF origin IPs) - khop voi
        # /etc/kpah-cloudflare-web/apply.sh. Luon vao duoc MOI trang, MOI domain,
        # kem ca domain cong khai o duoi sau khi domain do bi dong cho khach thuong.
        Require ip 104.243.35.117 104.243.35.158 104.243.35.234 104.243.37.126 \
                   104.243.37.140 104.243.37.171 104.243.35.97 59.153.228.236 \
                   14.251.12.33 104.28.237.241 118.71.110.221 104.28.219.202 \
                   171.224.54.169 117.0.168.6 171.255.70.12 1.52.142.229 59.153.225.73 \
                   42.114.138.22

        # Dai IP Cloudflare (IPv4 + IPv6) - khop voi /etc/kpah-cloudflare-web/apply.sh.
        # CHI cho vao day du trang khi KHONG PHAI domain cong khai ben duoi - domain
        # do da dong cua cho khach thuong (chi con /server.txt, dieu kien tren cung).
        <RequireAll>
            <RequireAny>
                Require ip 173.245.48.0/20 103.21.244.0/22 103.22.200.0/22 103.31.4.0/22 \
                           141.101.64.0/18 108.162.192.0/18 190.93.240.0/20 188.114.96.0/20 \
                           197.234.240.0/22 198.41.128.0/17 162.158.0.0/15 104.16.0.0/13 \
                           104.24.0.0/14 172.64.0.0/13 131.0.72.0/22
                Require ip 2400:cb00::/32 2606:4700::/32 2803:f800::/32 2405:b500::/32 \
                           2405:8100::/32 2a06:98c0::/29 2c0f:f248::/32
            </RequireAny>
            Require expr "tolower(%{HTTP_HOST}) != 'kpah-private.site' && tolower(%{HTTP_HOST}) != 'www.kpah-private.site'"
        </RequireAll>
    </RequireAny>
</Directory>
EOF

a2enconf kpah-webroot >/dev/null 2>&1
echo "      Da bat kpah-webroot"

echo "$LOG_TAG === 2/4 Kiem tra cau hinh"
if ! apache2ctl configtest 2>&1 | grep -qi "Syntax OK"; then
    echo "      [DUNG LAI] Cau hinh co loi, KHONG nap lai:"
    apache2ctl configtest 2>&1 | sed 's/^/        /'
    echo "      Apache van chay cau hinh cu."
    exit 1
fi
echo "      Syntax OK"

echo "$LOG_TAG === 3/4 Nap lai Apache"
systemctl reload apache2
sleep 1

echo "$LOG_TAG === 4/4 Tu kiem bang IP that (127.0.0.1 khong nam trong whitelist)"
# Khong dung "/kpah/..." de kiem: RedirectMatch trong vhost chay o pha
# translate_name, TRUOC ca <Directory> Require ben tren - nen no tra ve 301 chu
# khong phai 403, du dich den sau redirect van bi chan binh thuong (khong lo noi
# dung gi). Dung "/" va mot file tinh that de kiem dung tang <Directory>.
#
# LUU Y: phep tu kiem nay CHI xac nhan lop 1 (IP la). Lop 2 (domain cong khai
# qua Cloudflare) KHONG tu kiem duoc tu localhost - curl tu chinh may khong gia
# duoc IP Cloudflare that, va Host mac dinh cua curl la "127.0.0.1" chu khong
# phai domain that. Kiem lop 2 bang tay TU BEN NGOAI sau khi chay xong:
#   mo trinh duyet vao https://kpah-private.site/register.php -> phai ra 403
#   (tru khi ban dang vao tu IP nam trong whitelist admin o tren).
MA_OK=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1/server.txt" 2>/dev/null)
MA_CHAN=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1/" 2>/dev/null)
if [ "$MA_OK" = "200" ] && [ "$MA_CHAN" = "403" ]; then
    echo "      OK  - /server.txt tra ve $MA_OK, / tra ve $MA_CHAN (bi chan dung)"
else
    echo "      [CANH BAO] /server.txt=$MA_OK (can 200), /=$MA_CHAN (can 403)"
    echo "                 Xem lai $CONF"
    exit 1
fi

echo
echo "============================================================"
echo "  XONG."
echo "  - Qua IP la (khong Cloudflare, khong whitelist): chi /server.txt con"
echo "    truy cap duoc, moi duong dan khac tra ve 403."
echo "  - Qua domain kpah-private.site / www.kpah-private.site (ke ca nguoi"
echo "    choi thuong qua Cloudflare): cung chi /server.txt, TRU IP trong"
echo "    whitelist admin/Nova WAF (van vao duoc moi trang nhu cu)."
echo "  BAT BUOC kiem lai buoc 4/4 ghi o tren TU BEN NGOAI (trinh duyet that,"
echo "  khong phai tu VPS) truoc khi coi la xong."
echo "============================================================"
