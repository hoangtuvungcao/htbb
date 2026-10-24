#!/bin/sh
# Phan CLIENT cua bo hoi quy (chay_hoi_quy.sh CHUP=1): nhung thu bot may chu khong thay - tieu de menu (khong ten quai),
# nhan nut (Huấn luyện o che do production), tien te, menu canh giua, nut HUD, icon vat pham (khong hop qua cho), ten danh
# hieu dai khong bi cat. Dung lai ChupTrongGame + KiemChamHud (headless, 11 co + duong ve J2ME), khong viet lai.
#
#   sh tools/pokemon/kiem_tai/kiem_client_hoi_quy.sh [bao_cao.md]   (them ket qua vao cuoi bao cao neu co)
set -u
GOC=$(cd "$(dirname "$0")/../../.." && pwd)
RA=${1:-}
TAM=$(mktemp -d "${TMPDIR:-/tmp}/hoi_quy_client.XXXXXX")
trap 'rm -rf "$TAM"' EXIT
cd "$GOC" || exit 2
APP_IMAGE=0 timeout -k 10 900 sh client_pc/build.sh > "$TAM/build.log" 2>&1 || { tail -20 "$TAM/build.log"; exit 2; }
mkdir -p "$TAM/t"
javac -encoding UTF-8 -nowarn -cp client_pc/out/build/cls -d "$TAM/t" client_pc/test/ChupTrongGame.java client_pc/test/KiemChamHud.java \
    server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java server/src/com/htbb/pokemon/IconTrangBi.java 2>&1 | grep -v '^Note' | head
CP="client_pc/out/build/cls:client_pc/out/build/res:$TAM/t"
KQ=0
chay() { # ten, lop, tuy chon...
    ten=$1; lop=$2; shift 2
    mkdir -p "$TAM/$ten"
    # shellcheck disable=SC2086
    timeout -k 10 1800 java -Djava.awt.headless=true -Dhtbb.mayChu="$GOC/server" "$@" -cp "$CP" "$lop" "$TAM/$ten" > "$TAM/$ten.log" 2>&1
    k=$?
    hong=$(grep -c '^HONG' "$TAM/$ten.log")
    dong="| client $ten | $lop $* | thoat $k, HONG $hong | $(grep -m3 '^HONG' "$TAM/$ten.log" | tr '\n|' ' /') |"
    echo "$dong"
    [ -n "$RA" ] && echo "$dong" >> "$RA"
    [ $k -eq 0 ] || KQ=1
}
[ -n "$RA" ] && printf '\n## Client (headless)\n\n| Phần | Lệnh | Kết quả | HỎNG đầu |\n|---|---|---|---|\n' >> "$RA"
chay chup_pc ChupTrongGame
chay chup_j2me ChupTrongGame -Dchup.j2me=1
chay hud KiemChamHud
chay hud_j2me KiemChamHud -Dchup.j2me=1
# toc do game / giat lui khi danh tay (nhanh sua-slow-motion): chay khi da gop
if [ -f client_pc/test/KiemTocDoGame.java ]; then
    javac -encoding UTF-8 -nowarn -cp client_pc/out/build/cls -sourcepath client_pc/test:server/src -d "$TAM/t" client_pc/test/KiemTocDoGame.java 2>&1 | grep -v '^Note' | head
    chay toc_do KiemTocDoGame
fi
exit $KQ
