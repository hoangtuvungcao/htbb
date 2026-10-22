#!/bin/sh
# Gom ban phat hanh MOI NHAT vao MOT thu muc (luat chu game 0.9.8): mac dinh <thu muc cha cua repo>/../phat_hanh
# (vd /home/<user>/Downloads/pokemon/phat_hanh), XOA ban cu roi chep ban vua build + SHA256SUMS.txt. Khong co thu muc
# theo phien ban (phat_hanh_0.9.8...) nua.
#
#   sh tools/phat-hanh/gom-ban.sh            # sau khi da build: client_jar/build.sh, client_apk/build-pokemon.sh,
#                                            # client_pc/dong_goi_windows.sh (KIEU=phat-hanh)
#   RA=/duong/khac sh tools/phat-hanh/gom-ban.sh
#
# Ten tep: HTBB-<PHIEN_BAN>-android.apk, HTBB-<PHIEN_BAN>.jar, HTBB-<PHIEN_BAN>-nhe.jar, HTBB-PC-Setup-<x.y.z>.exe,
# HTBB-PC-Linux-<x.y.z>.AppImage (+ .tar.gz, Windows .zip).
set -e
GOC=$(cd "$(dirname "$0")/../.." && pwd)
. "$GOC/tools/phat-hanh/phien_ban.conf"
SO=${PHIEN_BAN%%-*}
# repo nam o <...>/pokemon/htbb (hoac worktree <...>/pokemon/htbb/.claude/worktrees/<ten>): thu muc phat_hanh canh htbb
case "$GOC" in
    */.claude/worktrees/*) CHA=$(dirname "${GOC%%/.claude/worktrees/*}") ;;
    *) CHA=$(dirname "$GOC") ;;
esac
RA=${RA:-$CHA/phat_hanh}
mkdir -p "$RA"
find "$RA" -mindepth 1 -maxdepth 1 -type f -delete
chep() {
    [ -f "$1" ] || { echo "THIEU $1 (chua build?)"; exit 1; }
    cp "$1" "$RA/$2"
    echo "  $2"
}
echo "Gom ban $PHIEN_BAN (BAN $BAN) -> $RA"
chep "$GOC/client_apk/out/HTBB-android-$BAN.apk" "HTBB-$PHIEN_BAN-android.apk"
chep "$GOC/client_jar/out/HTBB-v$BAN-release.jar" "HTBB-$PHIEN_BAN.jar"
chep "$GOC/client_jar/out/HTBB-v$BAN-nhe.jar" "HTBB-$PHIEN_BAN-nhe.jar"
chep "$GOC/client_pc/out/goi/HTBB-PC-Setup-$SO.exe" "HTBB-PC-Setup-$SO.exe"
chep "$GOC/client_pc/out/goi/HTBB-PC-Linux-$SO.AppImage" "HTBB-PC-Linux-$SO.AppImage"
chep "$GOC/client_pc/out/goi/HTBB-PC-Linux-$SO.tar.gz" "HTBB-PC-Linux-$SO.tar.gz"
chep "$GOC/client_pc/out/goi/HTBB-PC-Windows-$SO.zip" "HTBB-PC-Windows-$SO.zip"
(cd "$RA" && sha256sum $(ls | grep -v SHA256SUMS.txt) > SHA256SUMS.txt)
echo "OK $RA/SHA256SUMS.txt"
