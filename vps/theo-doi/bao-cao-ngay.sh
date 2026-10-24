#!/bin/bash
# ============================================================================
#  BAO CAO BAT THUONG PRODUCTION hang ngay (doc log, KHONG cham CSDL / game).
#
#      bash /opt/htbb/vps/theo-doi/bao-cao-ngay.sh                    # hom nay, in + ghi tep
#      bash /opt/htbb/vps/theo-doi/bao-cao-ngay.sh --hom-qua          # cron 00:10
#      bash /opt/htbb/vps/theo-doi/bao-cao-ngay.sh --ngay 2026-10-24 --in   # chi in, khong ghi tep
#
#  Cai cron (root):  /etc/cron.d/htbb-bao-cao
#      10 0 * * * root bash /opt/htbb/vps/theo-doi/bao-cao-ngay.sh --hom-qua >> /opt/htbb/logs/bao-cao.log 2>&1
#
#  Doc:  <log>/nhat_ky/may_chu-YYYYMMDD*.log (nhat ky may chu), <log>/game.out (dong cung ngay), <log>/gc.log*
#  Ghi:  <log>/bao_cao/YYYY-MM-DD.txt (tieng Viet, ngan). Tim:
#    1. Exception Java - dem theo lop + noi xay ra (khung stack dau)
#    2. nap lai ban do lien tuc: dong [NAP_MAP_LAP] (may chu >= 0.9.18: >= 6 lan goi -500 / 30 giay / nhan vat,
#       "cung_map" = ket mot ban do) + chum [KHOA_MAP] cung nhan vat (>= 5 dong / 10 giay)
#    3. vi tri bi keo ve: [KEO_VE] (>= 20 lan / 60 giay / nhan vat, ly do toc_do / o_cam / vat_can)
#    4. thao tac bi chan theo gio: KHOA_MAP, THUDAY, "Không thể" - dinh bat thuong (>= 3 lan trung vi, >= 30 dong)
#    5. dang nhap loi: [DANG_NHAP_LOI] ma -1/-2/-3, mat ket noi login server, hang doi login day
#    6. GC dung > 200 ms (gc.log)        7. nhip may chu max > 100 ms ([NHIP] max=)
#  Dong bi gop ("gop dong lap xN ... (bo bot M)", "loai X vuot tran ... bo bot M", "loi lap lai N lan") duoc cong lai
#  vao so dem nen so lieu la so THAT, khong phai so dong.
#
#  Webhook (tuy chon, chua bat): dat HTBB_BAO_CAO_WEBHOOK=https://... trong /etc/htbb/theo-doi.env -> POST JSON
#  {"text": "<bao cao>"} (Discord / Slack / Telegram qua cau noi). Trong = khong gui.
#  Ma thoat: 0; --ma-thoat: 3 khi co muc CAN XEM (de cron / giam sat bao dong).
# ============================================================================
set -uo pipefail
LOG_DIR=${HTBB_LOG_DIR:-/opt/htbb/logs}
ENV_TD=${HTBB_THEO_DOI_ENV:-/etc/htbb/theo-doi.env}
NGAY=$(date +%F); IN_THOI=0; MA_THOAT=0
while [ $# -gt 0 ]; do
    case "$1" in
        --ngay) NGAY=$2; shift 2 ;;
        --hom-qua) NGAY=$(date -d yesterday +%F); shift ;;
        --log) LOG_DIR=$2; shift 2 ;;
        --in) IN_THOI=1; shift ;;
        --ma-thoat) MA_THOAT=1; shift ;;
        -h|--help) sed -n '2,30p' "$0"; exit 0 ;;
        *) echo "tuy chon la: $1"; exit 2 ;;
    esac
done
date -d "$NGAY" +%F >/dev/null 2>&1 || { echo "ngay sai: $NGAY"; exit 2; }
command -v python3 >/dev/null 2>&1 || { echo "can python3"; exit 2; }

RA_DIR="$LOG_DIR/bao_cao"
BAO_CAO=$(python3 -I - "$NGAY" "$LOG_DIR" <<'PY'
import glob, os, re, sys, statistics
from collections import Counter, defaultdict

ngay, log_dir = sys.argv[1], sys.argv[2]
y, m, d = ngay.split('-')
mmdd = f"{m}-{d}"
E = lambda k, v: int(os.environ.get(k, v))
NGUONG_GC_MS = E('NGUONG_GC_MS', 200)
NGUONG_NHIP_MS = E('NGUONG_NHIP_MS', 100)

DONG = re.compile(r'^(\d\d-\d\d) (\d\d):(\d\d):(\d\d)\.\d+ ([A-Z]) (?:\. )?(.*)$')
LOAI = re.compile(r'^\[([A-Z0-9_]+)\] ?(.*)$')
LOP_LOI = re.compile(r'\b((?:[a-zA-Z_$][\w$]*\.)+[A-Z][\w$]*(?:Exception|Error))\b|\b([A-Z][\w$]*(?:Exception|Error))\b')
KHUNG = re.compile(r'^\s+at (\S+?\(.*?\))')
GOP = re.compile(r'gop dong lap x(\d+) lan trong \d+s \(bo bot (\d+)\): ([A-Z0-9_]+)\|(.*)$')
GOP_LOAI = re.compile(r'gop dong lap loai ([A-Z0-9_]+): them \d+ mau khac, bo bot (\d+) dong')
TRAN = re.compile(r'loai ([A-Z0-9_]+) vuot tran \d+ dong/\d+s, bo bot (\d+) dong')
STACK_LAP = re.compile(r'loi lap lai (\d+) lan trong \d+s \(stack chi in lan dau\): (.*)$')

dem_loai = Counter()          # loai -> so lan (ke ca phan bi gop)
theo_gio = defaultdict(Counter)  # nhom chan -> gio -> so
loi_java = Counter()          # (lop, noi) -> so
vi_du_loi = {}
nap_map = {}                  # ten -> (lan max, chi tiet, so lan bao)
keo_ve = {}
khoa_map_nv = defaultdict(list)  # ten -> [giay trong ngay]
dn_ma = Counter(); dn_ip = Counter(); dn_tk = Counter()
login_mat = 0; login_day = 0
nhip_max = []                 # (ms, noi, gio)
so_dong = 0
tep_doc = []

def giay(h, mi, s): return int(h) * 3600 + int(mi) * 60 + int(s)

def chan_nhom(loai, text):
    if loai == 'KHOA_MAP': return 'KHOA_MAP'
    if loai == 'THUDAY': return 'THUDAY'
    if 'Không thể' in text or 'Khong the' in text: return 'Không thể…'
    return None

def doc_tep(f, chi_ngay):
    global so_dong, login_mat, login_day
    cho = None  # exception dang cho khung stack: [lop, so lan]
    try:
        fh = open(f, encoding='utf-8', errors='replace')
    except OSError:
        return
    tep_doc.append(os.path.basename(f))
    gio_cuoi = None
    with fh:
        for raw in fh:
            line = raw.rstrip('\n')
            k = KHUNG.match(line)
            if k:
                if cho is not None and gio_cuoi is not None:
                    key = (cho[0], k.group(1)); loi_java[key] += cho[1]; vi_du_loi.setdefault(key, cho[2]); cho = None
                continue
            if cho is not None:
                key = (cho[0], '?'); loi_java[key] += cho[1]; vi_du_loi.setdefault(key, cho[2]); cho = None
            mt = DONG.match(line)
            if not mt:
                # game.out: dong khong co gio (JVM / stderr tran) - chi tinh neu da o trong ngay
                if gio_cuoi is not None and ('Exception in thread' in line or 'OutOfMemoryError' in line or 'A fatal error' in line):
                    l = LOP_LOI.search(line)
                    cho = [(l.group(1) or l.group(2)) if l else line[:60], 1, line[:200]]
                continue
            if mt.group(1) != mmdd:
                gio_cuoi = None
                continue
            so_dong += 1
            h, mi, s, muc, msg = mt.group(2), mt.group(3), mt.group(4), mt.group(5), mt.group(6)
            gio_cuoi = int(h)
            lt = LOAI.match(msg)
            loai, text = (lt.group(1), lt.group(2)) if lt else ('CU', msg)
            # ---- dong tong ket cua bo gop ----
            if loai == 'NHAT':
                g = GOP.search(text)
                if g:
                    bo, lo, mau = int(g.group(2)), g.group(3), g.group(4)
                    dem_loai[lo] += bo
                    n = chan_nhom(lo, mau)
                    if n: theo_gio[n][int(h)] += bo
                    if 'CONNECT TO SERVER LOG IN FAIL' in mau or 'LOG IN SERVER DISCONNECTED' in mau: login_mat += bo
                    if 'LOG IN QUEUE FULL' in mau: login_day += bo
                    continue
                g = GOP_LOAI.search(text) or TRAN.search(text)
                if g:
                    dem_loai[g.group(1)] += int(g.group(2))
                    n = chan_nhom(g.group(1), '')
                    if n: theo_gio[n][int(h)] += int(g.group(2))
                    continue
                g = STACK_LAP.search(text)
                if g:
                    so, kh = int(g.group(1)), g.group(2)
                    lop, _, noi = kh.partition(' @ ')
                    lop = lop.split(':')[0].strip()
                    key = (lop, noi.strip() or '?'); loi_java[key] += max(0, so - 1); vi_du_loi.setdefault(key, kh[:200])
                    continue
            dem_loai[loai] += 1
            n = chan_nhom(loai, text)
            if n: theo_gio[n][int(h)] += 1
            # ---- Exception ----
            if 'stack da in, lap lai' in text:
                pass  # da tinh qua STACK_LAP
            else:
                l = LOP_LOI.search(text)
                if l and (muc == 'E' or 'Exception' in text or 'Error' in text) and loai != 'MANG':
                    cho = [l.group(1) or l.group(2), 1, (loai + ' ' + text)[:200]]
            # ---- tung loai ----
            if loai == 'NAP_MAP_LAP' or loai == 'KEO_VE':
                ten = text.split(' ', 1)[0]
                lan = re.search(r'lan=(\d+)/(\d+)s', text)
                bang = nap_map if loai == 'NAP_MAP_LAP' else keo_ve
                cu = bang.get(ten, (0, '', 0))
                n_lan = int(lan.group(1)) if lan else 0
                bang[ten] = (max(cu[0], n_lan), text if n_lan >= cu[0] else cu[1], cu[2] + 1)
            elif loai == 'KHOA_MAP':
                mk = re.match(r'chan (\S+)', text)
                if mk: khoa_map_nv[mk.group(1)].append(giay(h, mi, s))
            elif loai == 'DANG_NHAP_LOI':
                mm = re.search(r'ma=(-?\d+) tk=(\S*) ip=(\S*)', text)
                if mm: dn_ma[mm.group(1)] += 1; dn_tk[mm.group(2)] += 1; dn_ip[mm.group(3)] += 1
            elif loai == 'NHIP':
                mx = re.search(r'max=([\d.]+)ms(?:\(([^)]*)\))?', text)
                if mx and float(mx.group(1)) > NGUONG_NHIP_MS:
                    nhip_max.append((float(mx.group(1)), mx.group(2) or '?', f"{h}:{mi}"))
            if 'CONNECT TO SERVER LOG IN FAIL' in text or 'LOG IN SERVER DISCONNECTED' in text: login_mat += 1
            if 'LOG IN QUEUE FULL' in text: login_day += 1
        if cho is not None:
            key = (cho[0], '?'); loi_java[key] += cho[1]; vi_du_loi.setdefault(key, cho[2])

nk = sorted(glob.glob(os.path.join(log_dir, 'nhat_ky', f'may_chu-{y}{m}{d}*.log')))
for f in nk: doc_tep(f, True)
# game.out chi them khi KHONG co nhat ky tep (may chu cu / sv.log.tep=0) - tranh dem doi.
if not nk: doc_tep(os.path.join(log_dir, 'game.out'), True)

# GC
gc = []
for f in sorted(glob.glob(os.path.join(log_dir, 'gc.log*'))):
    try:
        with open(f, encoding='utf-8', errors='replace') as fh:
            for line in fh:
                if not line.startswith(f'[{ngay}T') or 'Pause' not in line: continue
                ms = re.search(r'([\d.]+)ms\s*$', line)
                if ms and float(ms.group(1)) > NGUONG_GC_MS:
                    gc.append((float(ms.group(1)), line[12:20], re.sub(r'^\[[^\]]*\]\s*', '', line.strip())[:110]))
    except OSError:
        pass

# KHOA_MAP chum: >= 5 dong / 10 giay cung nhan vat
chum = {}
for ten, ts in khoa_map_nv.items():
    ts.sort(); j = 0; best = 0
    for i in range(len(ts)):
        while ts[i] - ts[j] > 10: j += 1
        best = max(best, i - j + 1)
    if best >= 5: chum[ten] = (best, len(ts))

# ---- in ----
out = []; can_xem = []
P = out.append
P(f"BÁO CÁO VẬN HÀNH HTBB — ngày {ngay}")
P(f"Nguồn: {', '.join(tep_doc) or '(không có nhật ký)'}; {so_dong} dòng trong ngày; gc.log: {len(gc)} lần dừng > {NGUONG_GC_MS} ms")
P("")
tong_loi = sum(loi_java.values())
P(f"1. Lỗi Java (Exception): {tong_loi} lần, {len(loi_java)} loại")
for (lop, noi), n in loi_java.most_common(10):
    P(f"   {n:6d} × {lop} @ {noi}")
if tong_loi: can_xem.append(f"{tong_loi} Exception ({len(loi_java)} loại)")

P(f"2. Nạp lại bản đồ liên tục: {len(nap_map)} nhân vật [NAP_MAP_LAP]; {len(chum)} nhân vật bị khoá bản đồ dồn dập [KHOA_MAP]")
for ten, (lan, ct, bao) in sorted(nap_map.items(), key=lambda x: -x[1][0])[:10]:
    ket = ' — KẸT CÙNG BẢN ĐỒ' if 'cung_map=' in ct else ''
    P(f"   {ten}: {ct.split(' ', 1)[1] if ' ' in ct else ct} (báo {bao} lần){ket}")
for ten, (best, tong) in sorted(chum.items(), key=lambda x: -x[1][0])[:10]:
    P(f"   {ten}: {best} lần chặn trong 10 giây, {tong} dòng KHOA_MAP cả ngày")
if nap_map or chum: can_xem.append(f"{len(nap_map) + len(chum)} nhân vật nạp/chặn bản đồ lặp")

P(f"3. Vị trí bị kéo về [KEO_VE]: {len(keo_ve)} nhân vật")
for ten, (lan, ct, bao) in sorted(keo_ve.items(), key=lambda x: -x[1][0])[:10]:
    P(f"   {ten}: {ct.split(' ', 1)[1] if ' ' in ct else ct} (báo {bao} lần)")
if len(keo_ve) >= E('NGUONG_KEO_VE_NV', 5): can_xem.append(f"{len(keo_ve)} nhân vật bị kéo vị trí liên tục")

P("4. Thao tác bị chặn (theo giờ):")
for nhom in ('KHOA_MAP', 'THUDAY', 'Không thể…'):
    c = theo_gio.get(nhom, Counter())
    tong = sum(c.values())
    if not tong:
        P(f"   {nhom}: 0"); continue
    tv = statistics.median([v for v in c.values() if v > 0])
    dinh = [(g, v) for g, v in sorted(c.items()) if v >= max(30, 3 * tv)]
    gio = ' '.join(f"{g:02d}h:{v}" for g, v in sorted(c.items()))
    P(f"   {nhom}: {tong} (trung vị {tv:g}/giờ) — {gio}")
    if dinh:
        P(f"      ĐỈNH: {', '.join(f'{g:02d}h={v}' for g, v in dinh)}")
        can_xem.append(f"đỉnh {nhom} lúc {', '.join(f'{g:02d}h' for g, _ in dinh)}")

P(f"5. Đăng nhập lỗi: {dem_loai.get('DANG_NHAP_LOI', 0)} lần"
  f" (sai mật khẩu {dn_ma.get('-1', 0)}, bị khoá {dn_ma.get('-2', 0)}, lỗi login server {dn_ma.get('-3', 0)}; phần bị gộp không rõ mã);"
  f" mất kết nối login server {login_mat}; hàng đợi login đầy {login_day}")
if dn_ip: P(f"   IP nhiều nhất: {', '.join(f'{k}×{v}' for k, v in dn_ip.most_common(5))}")
if dn_tk: P(f"   Tài khoản nhiều nhất: {', '.join(f'{k}×{v}' for k, v in dn_tk.most_common(5))}")
if login_mat or login_day or dn_ma.get('-3', 0): can_xem.append("login server lỗi / mất kết nối")
if dn_ip and dn_ip.most_common(1)[0][1] >= E('NGUONG_DN_IP', 50): can_xem.append(f"IP {dn_ip.most_common(1)[0][0]} đăng nhập sai {dn_ip.most_common(1)[0][1]} lần")

P(f"6. GC dừng > {NGUONG_GC_MS} ms: {len(gc)} lần" + (f", lâu nhất {max(gc)[0]:.0f} ms" if gc else ""))
for ms, t, l in sorted(gc, reverse=True)[:5]:
    P(f"   {t} {ms:.0f} ms  {l}")
if gc: can_xem.append(f"{len(gc)} lần GC > {NGUONG_GC_MS} ms")

P(f"7. Nhịp máy chủ max > {NGUONG_NHIP_MS} ms: {len(nhip_max)} phút" + (f", lâu nhất {max(nhip_max)[0]:.0f} ms" if nhip_max else ""))
noi = Counter(n for _, n, _ in nhip_max)
for n, c in noi.most_common(5):
    P(f"   {c:4d} × {n}  (lâu nhất {max(ms for ms, x, _ in nhip_max if x == n):.0f} ms)")
if nhip_max: can_xem.append(f"{len(nhip_max)} phút nhịp > {NGUONG_NHIP_MS} ms")

P("")
P("KẾT LUẬN: " + ("ỔN — không có mục nào vượt ngưỡng." if not can_xem else "CẦN XEM — " + "; ".join(can_xem) + "."))
print("\n".join(out))
sys.exit(3 if can_xem else 0)
PY
)
KQ=$?
[ "$KQ" = 0 ] || [ "$KQ" = 3 ] || { echo "$BAO_CAO"; echo "[LOI] bo phan tich hong (ma $KQ)"; exit 1; }

if [ "$IN_THOI" = 1 ]; then
    printf '%s\n' "$BAO_CAO"
else
    mkdir -p "$RA_DIR"
    F="$RA_DIR/$NGAY.txt"
    printf '%s\n(tạo lúc %s bởi vps/theo-doi/bao-cao-ngay.sh)\n' "$BAO_CAO" "$(date '+%F %T')" > "$F"
    printf '%s\n' "$BAO_CAO"
    echo "-> $F"
    find "$RA_DIR" -maxdepth 1 -name '*.txt' -mtime +90 -delete 2>/dev/null || true
fi

WEBHOOK=${HTBB_BAO_CAO_WEBHOOK:-}
if [ -z "$WEBHOOK" ] && [ -r "$ENV_TD" ]; then
    WEBHOOK=$(sed -n -E 's/^HTBB_BAO_CAO_WEBHOOK=(.*)$/\1/p' "$ENV_TD" | tail -n 1 | sed -E "s/^[\"']//; s/[\"']\$//")
fi
if [ -n "$WEBHOOK" ] && [ "$IN_THOI" = 0 ]; then
    python3 -I -c 'import json,sys; print(json.dumps({"text": sys.stdin.read()[:1900]}))' <<< "$BAO_CAO" \
        | curl -s -m 15 -o /dev/null -w '   webhook: HTTP %{http_code}\n' -H 'Content-Type: application/json' --data-binary @- "$WEBHOOK" \
        || echo "   [CANH BAO] gui webhook that bai"
fi
[ "$MA_THOAT" = 1 ] && [ "$KQ" = 3 ] && exit 3
exit 0
