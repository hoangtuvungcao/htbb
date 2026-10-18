#!/bin/bash
# ============================================================================
#  KHOA WEB CONG KHAI: KHACH LA (IP thang, khong qua Nova WAF/Cloudflare/
#  whitelist) CHI CON DUOC /server.txt
#
#      sudo /opt/htbb/vps/chi-cho-server-txt.sh
#
#  Website la HTBB-Web (Nuxt, htbb-web.service tren 127.0.0.1:3000), Apache CHI
#  con lam reverse proxy (HTBB-Web/deploy/apache-htbb-web.conf). Web PHP cu
#  (/opt/htbb/web) da bo ngay 19/10/2026 - khong con DocumentRoot/<Directory>
#  nao de chan, nen luat o day la <Location /> (ap dung ca cho request da
#  proxy). /server.txt (+ /NQSH2.txt, /server/NQSH2.txt cho client cu) do chinh
#  Nuxt tra ve (server/middleware/duong-dan-cu.ts, doc bien HTBB_SERVER_LIST /
#  HTBB_NQSH_LIST trong /etc/htbb-web.env) - client Unity tai danh sach may chu
#  o http://<may chu>/server.txt.
#
# ----------------------------------------------------------------------------
#  HAI LOP CHAN, GOP CHUNG MOT <Location /> (xem "BAY DA GAP" ben duoi)
# ----------------------------------------------------------------------------
#  1. IP LA goi THANG vao cong 80 bang IP that cua VPS (khong qua Cloudflare,
#     khong nam trong whitelist). Domain "sv.kpah-private.site" la DNS-only
#     (goi mien phi Cloudflare khong proxy duoc cong game TCP 19129, nen domain
#     nay phai tro thang ve IP that). Ai cung goi thang duoc vao cong 80 bang IP
#     that - lop nay chi cho ho /server.txt.
#
#  2. DOMAIN CONG KHAI "kpah-private.site" (+ "www.") qua Nova WAF / Cloudflare:
#     MO moi trang cho khach thuong (tu 26/09/2026). Admin/doi tac (whitelist IP
#     ben duoi) vao moi trang, moi domain.
#
# ----------------------------------------------------------------------------
#  BAY DA GAP - THU TU NAP
# ----------------------------------------------------------------------------
#  <Location> duoc gop THEO THU TU XUAT HIEN trong cau hinh (khong theo do cu the
#  cua duong dan nhu <Directory>), va mac dinh AuthMerging Off: section nap SAU
#  co Require se THAY HAN section truoc. conf-enabled nap theo bang chu cai, nen
#  neu de <Location /> cua script nay va <Location /admin> cua mat-khau-admin.sh
#  thanh hai conf rieng thi "htbb-admin-..." nap TRUOC "htbb-webroot..." va
#  Require cua "/" ghi de mat Basic Auth cua /admin. Vi vay ca ba script
#  (chi-cho-server-txt.sh, bat-https.sh, mat-khau-admin.sh) chi ghi MANH vao
#  /etc/apache2/htbb-web-bao-ve.d/NN-*.conf, va MOT conf duy nhat
#  htbb-web-bao-ve.conf nap cac manh do theo so thu tu:
#      10-chi-cho-server-txt.conf   <Location />        (tep nay)
#      20-admin-https.conf          <Location /admin>   (bat-https.sh)
#      30-admin-basic-auth.conf     <Location /admin>   (mat-khau-admin.sh)
#  (Ban PHP cu cung tung vap: tach "htbb-server-txt-only.conf" rieng thi bi
#  "Require all granted" cua conf nap sau ghi de - 09/09/2026.)
#
#  Danh sach IP duoi day PHAI khop voi /etc/htbb-cloudflare-web/apply.sh (dai
#  Cloudflare + "Nova WAF origin IPs"). Doi mot ben ma quen doi ben kia la lo
#  hong hoac tu khoa nham chinh minh.
#
#  Go bo lop nay: sudo rm /etc/apache2/htbb-web-bao-ve.d/10-chi-cho-server-txt.conf
#                 && sudo systemctl reload apache2
#
# ----------------------------------------------------------------------------
#  VI SAO CONF DUNG HEREDOC TRICH DAN (<<'EOF')
# ----------------------------------------------------------------------------
#  De danh sach IP dai giu duoc dau "\" xuong dong y nguyen - heredoc KHONG
#  trich dan nuot mat cap "\<xuong dong>". Vi vay khong the dung bien trong khoi
#  CONF; doi IP/ten mien thi sua thang trong CONF ben duoi.
# ============================================================================
set -u

THU_MUC_BAO_VE=/etc/apache2/htbb-web-bao-ve.d
CONF="$THU_MUC_BAO_VE/10-chi-cho-server-txt.conf"
CONF_NAP=/etc/apache2/conf-available/htbb-web-bao-ve.conf
LOG_TAG="[chi-cho-server-txt]"

if [ "$(id -u)" != "0" ]; then
    echo "$LOG_TAG Phai chay bang root:  sudo /opt/htbb/vps/chi-cho-server-txt.sh"
    exit 1
fi

echo "$LOG_TAG === 1/4 Viet cau hinh"
mkdir -p "$THU_MUC_BAO_VE"
printf '# Sinh boi vps/chi-cho-server-txt.sh / bat-https.sh / mat-khau-admin.sh.\n# Nap cac manh bao ve website (Nuxt sau reverse proxy) THEO SO THU TU - xem vps/chi-cho-server-txt.sh.\nIncludeOptional %s/*.conf\n' \
    "$THU_MUC_BAO_VE" > "$CONF_NAP"
cat > "$CONF" <<'EOF'
# Sinh boi vps/chi-cho-server-txt.sh - dung sua tay, chay lai script se ghi de.
#
# Cong 80 mo cho MOI IP o tang firewall (HTBB_CF_WEB chi con giu cong 443) de
# client game tai duoc /server.txt qua domain "sv.kpah-private.site" (DNS only).
# Chan lai o day: IP la CHI duoc /server.txt.
#
# Nova WAF la proxy DUNG CHUNG cho MOI khach cong khai (12/09/2026: whitelist ghi
# thang 7 IP goc cua Nova WAF da vo tinh mo toang ca site). Nen dung mod_remoteip
# doc lai IP THAT cua khach tu X-Forwarded-For, CHI khi ket noi TCP den tu dung 7
# IP goc cua Nova WAF; sau do "Require ip" ben duoi thay IP that. mod_proxy ghi IP
# DA khoi phuc vao cuoi X-Forwarded-For gui sang Nuxt (server/utils/ip.ts).
RemoteIPHeader X-Forwarded-For
RemoteIPInternalProxy 104.243.35.117 104.243.35.158 104.243.35.234 104.243.37.126 \
                      104.243.37.140 104.243.37.171 104.243.35.97

<Location />
    <RequireAny>
        # Danh sach may chu cho client (Unity: /server.txt; client cu: /NQSH2.txt,
        # /server/NQSH2.txt; ca tien to /kpah cu) - Nuxt tra thang noi dung.
        Require expr "%{REQUEST_URI} =~ m#^(/kpah)?/(server\.txt|server/NQSH2\.txt|NQSH2\.txt)$#"

        # Whitelist admin/doi tac - IP THAT (sau mod_remoteip). Vao duoc MOI trang,
        # MOI domain.
        Require ip 59.153.228.236 14.251.12.33 104.28.237.241 118.71.110.221 \
                   104.28.219.202 171.224.54.169 117.0.168.6 171.255.70.12 \
                   1.52.142.229 59.153.225.73 42.114.138.22

        # MO WEB CHO KHACH THUONG (26/09/2026): khach di qua Nova WAF (domain
        # kpah-private.site) vao duoc moi trang. Khong dung "Require ip" duoc:
        # mod_remoteip da thay REMOTE_ADDR bang IP THAT cua khach, nen phai xet IP
        # cua KET NOI TCP (CONN_REMOTE_ADDR) - chi Nova WAF moi co. Dang ky van
        # KHONG tu kich hoat (HTBB_AUTO_ACTIVATE=false trong /etc/htbb-web.env) - admin kich hoat o
        # trang quan tri > Tai khoan. Khu /admin con Basic Auth rieng.
        Require expr "%{CONN_REMOTE_ADDR} in {'104.243.35.117', '104.243.35.158', '104.243.35.234', '104.243.37.126', '104.243.37.140', '104.243.37.171', '104.243.35.97'}"

        # Dai IP Cloudflare (IPv4 + IPv6) - khop voi /etc/htbb-cloudflare-web/apply.sh.
        Require ip 173.245.48.0/20 103.21.244.0/22 103.22.200.0/22 103.31.4.0/22 \
                   141.101.64.0/18 108.162.192.0/18 190.93.240.0/20 188.114.96.0/20 \
                   197.234.240.0/22 198.41.128.0/17 162.158.0.0/15 104.16.0.0/13 \
                   104.24.0.0/14 172.64.0.0/13 131.0.72.0/22
        Require ip 2400:cb00::/32 2606:4700::/32 2803:f800::/32 2405:b500::/32 \
                   2405:8100::/32 2a06:98c0::/29 2c0f:f248::/32
    </RequireAny>
</Location>
EOF

a2enmod remoteip >/dev/null 2>&1
echo "      Da bat mod_remoteip (doc IP that tu header Nova WAF gui)"
# Conf cu thoi web PHP (<Directory /opt/htbb/web>) - bo, thay bang manh o tren.
a2disconf htbb-webroot >/dev/null 2>&1 || true
a2enconf htbb-web-bao-ve >/dev/null 2>&1
echo "      Da bat htbb-web-bao-ve ($CONF)"

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
# LUU Y: chi xac nhan lop 1 (IP la). Lop 2 (domain cong khai qua Nova WAF/
# Cloudflare) khong tu kiem duoc tu localhost. Kiem bang tay TU BEN NGOAI:
#   mo trinh duyet vao https://kpah-private.site/register -> phai ra trang dang ky.
# /server.txt can 200: Nuxt dang chay (htbb-web) + proxy da bat + HTBB_SERVER_LIST
# da khai trong /etc/htbb-web.env (thieu bien -> Nuxt tra 503).
MA_OK=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1/server.txt" 2>/dev/null)
MA_CHAN=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1/" 2>/dev/null)
if [ "$MA_OK" = "200" ] && [ "$MA_CHAN" = "403" ]; then
    echo "      OK  - /server.txt tra ve $MA_OK, / tra ve $MA_CHAN (bi chan dung)"
else
    echo "      [CANH BAO] /server.txt=$MA_OK (can 200), /=$MA_CHAN (can 403)"
    echo "                 Xem lai $CONF, htbb-web.service va HTBB_SERVER_LIST"
    exit 1
fi

echo
echo "============================================================"
echo "  XONG."
echo "  - Qua IP la (khong Cloudflare, khong whitelist): chi /server.txt con"
echo "    truy cap duoc, moi duong dan khac tra ve 403."
echo "  - Qua domain kpah-private.site (Nova WAF) hoac Cloudflare: MO moi trang"
echo "    cho khach thuong. Dang ky KHONG tu kich hoat - admin kich hoat o"
echo "    trang quan tri > Tai khoan."
echo "  BAT BUOC kiem lai lop 2 TU BEN NGOAI (trinh duyet that) truoc khi coi"
echo "  la xong."
echo "============================================================"
