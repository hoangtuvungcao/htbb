#!/bin/sh
# ============================================================================
#  GIAI DOAN B: gop 3 repo -> monorepo htbb/ (giu lich su) + doi ten KPAH -> HTBB + doi ten lop kiem hiep
#  -> chuyen CSDL may dev -> bat stack tu monorepo -> kiem. Xem tools/doi_ten/KE_HOACH_DOI_TEN.md.
#
#    sh HTBB/tools/doi_ten/chay_giai_doan_b.sh --thu <thu muc nhap>   # DIEN TAP: khong dung stack, khong dung CSDL
#    sh HTBB/tools/doi_ten/chay_giai_doan_b.sh --that                 # THAT: ~/Downloads/pokemon/htbb
#  Tuy chon them (--that): --bo-qua-tien-trinh-khac (van chay khi con game server KHAC dung NovaDB)
#
#  3 repo cu (HTBB, htbb-client, HTBB-Web) KHONG bi sua; --that chi tao chay_novadb/DUNG_HAN trong HTBB
#  de vong chay_lap.sh khong bat lai server. Quay lai: xem cuoi tep.
# ============================================================================
set -eu
DAY=$(cd "$(dirname "$0")" && pwd -P)
GOC=$(cd "$DAY/../../.." && pwd -P)          # ~/Downloads/pokemon
CHE_DO=""; DICH=""; BO_QUA=""
while [ $# -gt 0 ]; do
    case "$1" in
        --thu) CHE_DO=thu; DICH=$2; shift ;;
        --that) CHE_DO=that; DICH=$GOC/htbb ;;
        --bo-qua-tien-trinh-khac) BO_QUA=$1 ;;
        *) echo "tuy chon la: $1"; exit 2 ;;
    esac
    shift
done
[ -n "$CHE_DO" ] || { sed -n 2,13p "$0"; exit 2; }
[ ! -e "$DICH" ] || [ -z "$(ls -A "$DICH" 2>/dev/null)" ] || { echo "$DICH da co noi dung - xoa hoac chon thu muc khac"; exit 1; }
LAM=$DICH.lam
KQ=$DICH.kiem
log() { echo "[giai_doan_b $(date +%T)] $*"; }

dung_stack_cu() {
    C=$GOC/HTBB/chay_novadb
    KHAC=""
    for p in $(pgrep -f 'server\.TeamServer|htbb-server\.jar|KPAH2_patched\.jar' 2>/dev/null || true); do
        [ "$p" = "$(cat "$C/game.pid" 2>/dev/null)" ] || KHAC="$KHAC $p"
    done
    if [ -n "$KHAC" ] && [ -z "$BO_QUA" ]; then
        log "Con game server KHAC dung NovaDB (PID:$KHAC) - tat chung truoc hoac them --bo-qua-tien-trinh-khac"; exit 1
    fi
    touch "$C/DUNG_HAN"
    for f in chay_lap game login nova; do
        p=$(cat "$C/$f.pid" 2>/dev/null || true)
        [ -n "$p" ] && kill -0 "$p" 2>/dev/null || continue
        log "dung $f PID $p"; kill "$p"
        i=0; while kill -0 "$p" 2>/dev/null && [ $i -lt 180 ]; do i=$((i+1)); sleep 1; done
        if kill -0 "$p" 2>/dev/null; then log "$f PID $p chua thoat sau 180s - DUNG"; exit 1; fi
    done
    return 0
}

if [ "$CHE_DO" = that ]; then
    log "1/6 dung stack cu (HTBB/chay_novadb)"
    dung_stack_cu
    log "2/6 gop monorepo (+ sao luu 3 repo + novadb-data)"
    python3 -I "$DAY/gop_monorepo.py" --dich "$DICH" --lam "$LAM" --sao-luu
else
    log "1/6 (dien tap) khong dung stack"
    log "2/6 gop monorepo vao $DICH"
    python3 -I "$DAY/gop_monorepo.py" --dich "$DICH" --lam "$LAM"
fi

log "3/6 doi ten lop / tep / thu muc kiem hiep -> Pokemon"
python3 -I "$DICH/tools/doi_ten/doi_ten_lop.py" --goc "$DICH" --apply
python3 -I "$DICH/tools/doi_ten/doi_ten_lop.py" --goc "$DICH" | tail -1      # lan 2: phai 0 thay doi

if [ "$CHE_DO" = that ]; then
    log "4/6 chuyen CSDL may dev + bat NovaDB/login/game tu monorepo (truoc buoc kiem dai, de server khong tat lau)"
    sh "$DICH/tools/doi_ten/chuyen_csdl.sh" --apply --khoi-dong $BO_QUA
else
    log "4/6 (dien tap) bo qua CSDL"
fi

log "5/6 kiem toan bo (so test server voi ban chep truoc gop: $LAM/cay/HTBB)"
KIEM_OK=1
sh "$DAY/kiem_tat_ca.sh" "$DICH" "$KQ" "$LAM/cay/HTBB" || { KIEM_OK=0; log "CO BUOC HONG - xem $KQ/tong_ket.txt"; }

if [ "$CHE_DO" = that ]; then
    if [ "$KIEM_OK" = 1 ]; then
        log "commit MOT lan (cuc bo, khong push)"
        git -C "$DICH" add -A
        cat > "$LAM/commit_msg.txt" <<'MSG'
Monorepo htbb: doi ten KPAH -> HTBB, doi ten lop kiem hiep -> Pokemon, bo cuc thanh phan

- Bo cuc: server/ (game + login), client_jar/ (+shared/), client_apk/, client_pc/ (Java),
  client_pc_ios/ (Unity; ClientUnity cu con test soi), web/ (Nuxt), sql/, docs/, tools/, vps/.
  Workflow .github/workflows/{server,client,web}-*.yml loc theo duong dan; .gitignore/.gitattributes gop o goc.
- Doi ten KPAH -> HTBB: CSDL htbb_db, bien HTBB_* (doc KPAH_* 1 ban), /opt/htbb, htbb-*.service (+Alias),
  htbb-server.jar, lib/htbb-core.jar; giu ten ben ngoai (ten mien, repo nqsh-h2-tai, khoa RMS, lop trong bytecode).
- Doi ten lop/tep kiem hiep theo docs/pokemon/THUAT_NGU.md (doi_ten_lop.tsv); giu lop bi htbb-core.jar tham chieu.
- Sua duong dan sau gop (tools/doi_ten/sua_sau_gop.py), README goc, so tay VPS vps/CHUYEN_TEN_HTBB.md.
- Gom ca thay doi chua commit cua 3 repo cu tai thoi diem gop.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
        git -C "$DICH" commit -q -F "$LAM/commit_msg.txt"
        log "commit: $(git -C "$DICH" log -1 --format='%h %s')"
    else
        log "KHONG commit (co buoc kiem hong) - xem $KQ/tong_ket.txt"
    fi
fi
if [ "$CHE_DO" = that ]; then log "6/6 bot dang nhap: tools/pokemon/kiem_tai/BotTai.java (README cung thu muc) vao 127.0.0.1:19129"; fi
log "Xong. Ket qua kiem: $KQ/tong_ket.txt"
# QUAY LAI (--that):
#   sh $GOC/htbb/tools/doi_ten/chuyen_csdl.sh --quay-lai          # htbb_db*.novadb -> kpah_db*, ini cu
#   rm -f $GOC/HTBB/chay_novadb/DUNG_HAN; cd $GOC/HTBB && NOVA_USER=kpah sh tools/pokemon/chay_tren_novadb.sh
#   rm -rf $GOC/htbb $GOC/htbb.lam $GOC/htbb.kiem                  # 3 repo cu khong bi sua
#   (ban sao luu day du: $GOC/sao_luu_gop_<gio>.tar)
