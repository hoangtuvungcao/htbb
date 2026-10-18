#!/bin/sh
# Cai ban do ve lai + bo tile moi (che do Pokemon) vao client J2ME. KHONG build jar.
#   sh tools/pokemon/cai_map_j2me.sh [thu_muc_j2me]      (mac dinh ../htbb-client/j2me)
# Chep:
#   tools/pokemon/map_j2me/0                 -> res/0                (ban do 0 dong goi san: nap luc khoi dong/loi)
#   tools/pokemon/map_j2me/{t,t_hang,t_thanh}.type -> res/*.type     (kieu di duoc cua BO TILE MOI)
#   map/pokemon_tile/{t,t_hang,t_thanh,t_small,t_hang_s,t_thanh_s}.png -> res/pt/   (anh tile moi)
# Ban cu duoc cat 1 lan vao <j2me>/.truoc_map_moi/ (ngoai res/ de khong lot vao jar).
# Sau do build jar nhu thuong le (j2me/build.sh); neu build voi HTBB_ASSETS=pc (htbb-client/pc/Assets) thi
# DongGoiPokeJ2me chep lai res/pt tu Resources/poke_tile - cung noi dung, khong sao.
# Cac map khac client nhan tu server (goi 12) nen khong can chep.
set -e
HTBB=$(cd "$(dirname "$0")/../.." && pwd)
J=${1:-$HTBB/client_jar}
RES="$J/res"
[ -d "$RES" ] || { echo "khong thay $RES"; exit 1; }
SAO="$J/.truoc_map_moi"
mkdir -p "$SAO/pt" "$RES/pt"
for f in 0 t.type t_hang.type t_thanh.type; do
    [ -f "$SAO/$f" ] || [ ! -f "$RES/$f" ] || cp "$RES/$f" "$SAO/$f"
done
for f in t t_hang t_thanh t_small t_hang_s t_thanh_s; do
    [ -f "$SAO/pt/$f.png" ] || [ ! -f "$RES/pt/$f.png" ] || cp "$RES/pt/$f.png" "$SAO/pt/$f.png"
done
cp "$HTBB/tools/pokemon/map_j2me/0" "$RES/0"
for t in t t_hang t_thanh; do
    cp "$HTBB/tools/pokemon/map_j2me/$t.type" "$RES/$t.type"
done
for f in t t_hang t_thanh t_small t_hang_s t_thanh_s; do
    cp "$HTBB/server/map/pokemon_tile/$f.png" "$RES/pt/$f.png"
done
echo "da cai ban do + bo tile moi vao $RES (ban cu: $SAO)"
