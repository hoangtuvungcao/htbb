#!/bin/sh
# Kiem cach client J2ME ve moi ban do Pokemon (headless). Dung: sh chay.sh [thu_muc_ra] [id ...]
# Bien: HTBB (thu muc server, mac dinh ../server cua repo; du_lieu/ tim o goc repo), RES (tai nguyen jar, mac dinh client_jar/res).
D=$(cd "$(dirname "$0")" && pwd); J=$(cd "$D/../.." && pwd)
HTBB=${HTBB:-$(cd "$J/../server" && pwd)}
RA=${1:-${TMPDIR:-/tmp}/kiem_ve_map}; [ $# -gt 0 ] && shift
RES=${RES:-$J/res}
B=$RA/lop; rm -rf "$B"; mkdir -p "$B"
javac -nowarn -encoding UTF-8 -d "$B" -sourcepath "$D/stub" "$D"/stub/javax/microedition/lcdui/*.java \
  "$J/src/game/pokemon/PokeBody.java" "$J/src/game/pokemon/PokeDuLieu.java" "$J/src/game/pokemon/PokeDongTac.java" \
  "$J/src/game/pokemon/VatThePokemon.java" "$J/src/game/pokemon/HuyHieuPoke.java" "$J/src/game/pokemon/PhuKien.java" "$D/stub/game/pokemon/KieuO.java" "$D/KiemVeMap.java" || exit 2
java -Djava.awt.headless=true -Dj2me.src="$J/src" -Xmx512m -cp "$B:$RES" KiemVeMap "$HTBB" "$RES" "$RA" "$@"
