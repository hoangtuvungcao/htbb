#!/bin/sh
# Build bản jar J2ME (KPAH 2.7.6 + mod) hoàn toàn từ source:
#   src/          code game (decompile từ jar gốc TeaMobi) + phần riêng J2ME của mod (Nen, KiemServer, TaiDanhSach)
#   ../shared/mod code mod dùng chung với bản Android (AutoGuard, AutoUp, PauseMenu...)
#   res/          ảnh + dữ liệu game, MANIFEST.MF
#   api/          khai báo API WMA (SMS) chỉ để biên dịch - máy J2ME có sẵn, không đóng vào jar
#
#   sh build.sh 54     -> out/KPAH_276 (1)-automenu-v54.jar + bản phát hành ...-release.jar (ProGuard)
#
# Bước: javac (--release 8) -> Down45 (class 45 + StringBuffer, kiểm API CLDC) -> jar -> ProGuard.
set -e
cd "$(dirname "$0")"
V=${1:-54}
# Windows (Git Bash) hoặc Linux (GitHub Actions): JDK_BIN / RT_JAR qua biến môi trường
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) S=";"; JMD="/c/Program Files/Java/jdk-26.0.1/bin"; RMD="C:/Program Files/Java/jre1.8.0_503/lib/rt.jar" ;;
    *) S=":"; JMD="$(dirname "$(command -v javac)")"; RMD="${JAVA8_HOME:-/usr/lib/jvm/java-8}/jre/lib/rt.jar" ;;
esac
J=${JDK_BIN:-$JMD}
RT_JAR=${RT_JAR:-$RMD}    # rt.jar của JRE 8: thư viện cho ProGuard (không đọc được jmods JDK mới)
[ -f "$RT_JAR" ] || { echo "Khong thay rt.jar cua JRE 8: $RT_JAR (dat bien RT_JAR)"; exit 1; }
OUT="out/KPAH_276 (1)-automenu-v$V.jar"
B=out/build

rm -rf "$B" && mkdir -p "$B/tool" "$B/api" "$B/gen/game/render/screen" "$B/cls" "$B/cls45" out

# số bản jar gửi kèm khi đăng nhập (server real.BanMoiNhat báo khi có bản mới hơn)
printf 'package game.render.screen;\n\n/** Sinh bởi build.sh - KHÔNG sửa tay. */\nfinal class PhienBan {\n    static final int BAN = %s;\n}\n' "$V" > "$B/gen/game/render/screen/PhienBan.java"

"$J/javac" -nowarn -encoding UTF-8 -d "$B/tool" -cp tools/asm.jar tools/Down45.java
"$J/javac" -nowarn --release 8 -encoding UTF-8 -d "$B/api" -cp "tools/midp.jar${S}tools/microemulator.jar" $(find api -name '*.java')
find src ../shared/mod "$B/gen" -name '*.java' > "$B/nguon.txt"
"$J/javac" -nowarn --release 8 -encoding UTF-8 -d "$B/cls" \
    -cp "tools/midp.jar${S}tools/microemulator.jar${S}$B/api" @"$B/nguon.txt"

"$J/java" -cp "$B/tool${S}tools/asm.jar" Down45 d "$B/cls" "$B/cls45"

rm -f "$OUT"
"$J/jar" cfm "$OUT" MANIFEST.MF -C "$B/cls45" . -C res .
echo "OK -> $OUT"

# Bản phát hành: ProGuard (release.pro) đổi tên toàn bộ class/hàm/biến thành a, b, c...
# mapping-vNN.txt = bảng tên cũ <-> mới: GIỮ RIÊNG, KHÔNG phát kèm, KHÔNG đưa lên git.
REL="out/KPAH_276 (1)-automenu-v$V-release.jar"
rm -f "$REL"
# ProGuard không chịu tên file có dấu cách -> làm trên tên tạm
cp "$OUT" out/pg-in.jar && rm -f out/pg-out.jar
printf -- '-libraryjars "%s"(java/**)\n' "$RT_JAR" > out/rt.pro
"$J/java" -jar tools/proguard.jar @release.pro @out/rt.pro -injars out/pg-in.jar -outjars out/pg-out.jar -printmapping "out/mapping-v$V.txt" > out/proguard-v$V.log 2>&1     || { tail -20 out/proguard-v$V.log; exit 1; }
mv out/pg-out.jar "$REL" && rm -f out/pg-in.jar
echo "OK -> $REL (mapping: out/mapping-v$V.txt)"
