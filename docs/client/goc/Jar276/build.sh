#!/bin/sh
# Build KPAH_276 (1)-automenu-vNN.jar tu base-v53.jar + src/ (AutoGuard, PauseMenu, KiemServer)
# + ban phat hanh -release.jar da ProGuard (xem cuoi file).
#   sh build.sh 54
# Buoc: javac (--release 8) -> Down45 (class version 45 cho J2ME) -> InjectGuards chen
# "if (AutoGuard.dapGuard()) return;" vao dau nut "Dap" (ScreenDapDo$7.perform)
# -> ghi de cac class do vao ban sao cua base-v53.jar.
set -e
cd "$(dirname "$0")"
V=${1:-54}
# Windows (Git Bash) hoac Linux (GitHub Actions): JDK_BIN / RT_JAR qua bien moi truong
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) S=";"; JMD="/c/Program Files/Java/jdk-26.0.1/bin"; RMD="C:/Program Files/Java/jre1.8.0_503/lib/rt.jar" ;;
    *) S=":"; JMD="$(dirname "$(command -v javac)")"; RMD="${JAVA8_HOME:-/usr/lib/jvm/java-8}/jre/lib/rt.jar" ;;
esac
J=${JDK_BIN:-$JMD}
RT_JAR=${RT_JAR:-$RMD}    # rt.jar cua JRE 8: thu vien cho ProGuard (khong doc duoc jmods JDK moi)
[ -f "$RT_JAR" ] || { echo "Khong thay rt.jar cua JRE 8: $RT_JAR (dat bien RT_JAR)"; exit 1; }
CP="base-v53.jar${S}tools/midp.jar${S}tools/microemulator.jar"
OUT="out/KPAH_276 (1)-automenu-v$V.jar"

rm -rf tcls cls45 stage && mkdir -p tcls cls45 stage out

"$J/javac" -nowarn -encoding UTF-8 -d tcls -cp tools/asm.jar src/Down45.java src/InjectGuards.java src/InjectReplaceCall.java
"$J/javac" -nowarn --release 8 -encoding UTF-8 -d stage -cp "$CP" \
    src/game/render/screen/AutoGuard.java src/game/render/screen/PauseMenu.java src/game/render/screen/KiemServer.java src/game/render/screen/TaiDanhSach.java src/game/render/screen/AutoUp.java src/game/render/screen/AutoVanTieu.java src/game/render/screen/AutoVanMay.java src/game/render/screen/TienIch.java src/game/render/screen/LenThanh.java

# Down45 nhan cap (vao, ra) - doi tung class moi build sang version 45
ARGS=""
for f in $(cd stage && find . -name '*.class' | sed 's#^\./##'); do
    mkdir -p "cls45/$(dirname "$f")"
    ARGS="$ARGS stage/$f cls45/$f"
done
"$J/java" -cp "tcls${S}tools/asm.jar" Down45 x $ARGS

# Nut "Dap" goc lay tu base-v53 (da co san hook themDong cua v53)
mkdir -p base && (cd base && "$J/jar" xf ../base-v53.jar 'game/render/screen/ScreenDapDo$7.class' game/render/screen/GameScr.class game/networklogic/GameService.class game/networklogic/MessageHandler.class game/render/screen/ServerListScr.class)
# Ghi nho tai khoan moi lan gui lenh dang nhap (auto nuoi pet tu dang nhap lai)
mkdir -p cls45/game/networklogic
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards     base/game/networklogic/GameService.class cls45/game/networklogic/GameService.class     login '(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V' loginGuard '(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z'
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards \
    'base/game/render/screen/ScreenDapDo$7.class' 'cls45/game/render/screen/ScreenDapDo$7.class' \
    perform '()V' dapGuard '()Z'
# Menu NPC tu server (-30): auto nuoi pet doc danh sach "Cho pet an" ma khong hien menu
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards \
    base/game/render/screen/GameScr.class tmp-GameScr.class \
    onMenuOption '(IB[Ljava/lang/String;)V' menuGuard '(IB[Ljava/lang/String;)Z'
# Cua so che do (showWindow tab 28): auto nuoi pet che thang, khong mo cua so
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards \
    tmp-GameScr.class cls45/game/render/screen/GameScr.class \
    showWindow '(IZ[B)V' cuaSoGuard '(IZ[B)Z'
mv cls45/game/render/screen/GameScr.class tmp-GameScr.class
# Lenh 4 (vi tri) server gui cho chinh minh = buoc luot bi tu choi -> auto dat lai vi tri
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards     tmp-GameScr.class tmp2-GameScr.class     onActorMove '(BSSSSBIBBZ)V' viTriGuard '(BSSSS)Z'
# Loi moi vao nhom: auto up tu dong y / bo qua nguoi la
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards     tmp2-GameScr.class tmp3-GameScr.class     onAdd2Party '(Lnetwork/Message;)V' nhomGuard '(Lnetwork/Message;)Z'
# Lenh vao map cung id dang dung (dang nhap lai, doi khu, hoa tieu) -> ep nap lai map de khong mat NPC
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards     tmp3-GameScr.class cls45/game/render/screen/GameScr.class     onMap '(SSSSLjava/lang/String;[B)V' mapGuard '(S)Z'
rm -f tmp-GameScr.class tmp2-GameScr.class tmp3-GameScr.class
# Danh sach server tu GitHub (TaiDanhSach): tai khi mo man chon server + nut "Cap nhat"
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards     base/game/render/screen/ServerListScr.class tmp-SL.class     switchToMe '()V' dsGuard '()Z'
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards     tmp-SL.class cls45/game/render/screen/ServerListScr.class     doUpdateServer '()V' capNhatGuard '()Z'
rm -f tmp-SL.class
# Goi -88: server ky xac thuc (KiemServer) - chi dang nhap khi dung server that
"$J/java" -cp "tcls${S}tools/asm.jar" InjectGuards     base/game/networklogic/MessageHandler.class cls45/game/networklogic/MessageHandler.class     onMessage '(Lnetwork/Message;)V' goiGuard '(Lnetwork/Message;)Z'

cp base-v53.jar "$OUT"
(cd cls45 && "$J/jar" uf "../$OUT" $(find . -name '*.class' | sed 's#^\./##'))
rm -rf base
echo "OK -> $OUT"

# Ban phat hanh da lam roi bang ProGuard (release.pro). mapping-vNN.txt = bang ten cu <-> moi:
# GIU RIENG, KHONG phat kem (co no la dich nguoc lai duoc ten goc).
REL="out/KPAH_276 (1)-automenu-v$V-release.jar"
rm -f "$REL"
# ProGuard khong chiu ten file co dau cach -> lam tren ten tam
cp "$OUT" out/pg-in.jar && rm -f out/pg-out.jar
printf -- '-libraryjars "%s"(java/**)\n' "$RT_JAR" > out/rt.pro
"$J/java" -jar tools/proguard.jar @release.pro @out/rt.pro -injars out/pg-in.jar -outjars out/pg-out.jar -printmapping "out/mapping-v$V.txt" > out/proguard-v$V.log 2>&1     || { tail -20 out/proguard-v$V.log; exit 1; }
mv out/pg-out.jar "$REL" && rm -f out/pg-in.jar
echo "OK -> $REL (mapping: out/mapping-v$V.txt)"
