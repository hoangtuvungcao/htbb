#!/bin/sh
# Chep ban do 0 + kieu tile + anh tile Pokemon cua server vao client J2ME (client_jar/res). KHONG build jar.
#   sh tools/pokemon/cai_map_j2me.sh [thu_muc_client_jar]      (mac dinh client_jar)
# Chep:
#   server/cMap/0                                   -> res/0        (ban do 0 dong goi san: nap luc khoi dong/loi)
#   server/cMap/{t,t_hang,t_thanh}.type             -> res/*.type   (kieu di duoc cua bo tile)
#   server/map/pokemon_tile/{t,t_hang,t_thanh,t_small,t_hang_s,t_thanh_s}.png -> res/pt/
# Cac map khac client nhan tu server (goi 12) nen khong can chep. Chay sau khi ve lai ban do (VeMapMoi).
set -e
HTBB=$(cd "$(dirname "$0")/../.." && pwd)
J=${1:-$HTBB/client_jar}
RES="$J/res"
[ -d "$RES" ] || { echo "khong thay $RES"; exit 1; }
mkdir -p "$RES/pt"
cp "$HTBB/server/cMap/0" "$RES/0"
for t in t t_hang t_thanh; do
    cp "$HTBB/server/cMap/$t.type" "$RES/$t.type"
done
for f in t t_hang t_thanh t_small t_hang_s t_thanh_s; do
    cp "$HTBB/server/map/pokemon_tile/$f.png" "$RES/pt/$f.png"
done
echo "da chep ban do 0 + bo tile vao $RES"
