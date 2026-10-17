#!/bin/sh
# Build bản jar J2ME (client TeaMobi 2.7.6 + mod) hoàn toàn từ source:
#   src/          code game (decompile từ jar gốc TeaMobi) + phần riêng J2ME của mod (Nen, KiemServer, TaiDanhSach)
#   ../shared/mod code mod dùng chung với bản Android (AutoGuard, AutoUp, PauseMenu...)
#   res/          ảnh + dữ liệu game, MANIFEST.MF
#   api/          khai báo API WMA (SMS) chỉ để biên dịch - máy J2ME có sẵn, không đóng vào jar
#
#   sh build.sh 54     -> out/HTBB-v54.jar + bản phát hành ...-release.jar (ProGuard)
#   MAY_CHU=127.0.0.1:19129 sh build.sh 54
#                      -> bản THỬ ...-v54-thu.jar: MANIFEST thêm "HTBB-May-Chu: host:cong" = chỉ một server thử
#                         (không tải danh sách, bỏ xác thực -88). Không ảnh hưởng bản thường/phát hành.
#   HTBB_ASSETS=<HTBB>/ClientUnity/Assets sh build.sh 54
#                      -> trước khi build, đóng gói lại sprite + hiệu ứng chiêu Pokémon (res/pk, res/pt, và bản
#                         nhẹ res_nhe/pk) bằng tools/DongGoiPokeJ2me.java (đọc thêm HTBB docs/pokemon/*.csv)
#   GOI=nhe sh build.sh 54
#                      -> thêm bản NHẸ cho máy thật ...-v54-nhe.jar (ProGuard + res_nhe/pk: ít loài, ít khung,
#                         chân dung 24px; loài thiếu vẽ bằng bóng đen, xem DongGoiPokeJ2me)
#   MAY_CHU_CHINH=host:cong sh build.sh 54
#                      -> MỌI bản (thường/phát hành/nhẹ) chỉ vào server Pokémon này ("HTBB-May-Chu" trong
#                         MANIFEST), không tải danh sách server cũ.
#   XAC_THUC=1 sh build.sh 54
#                      -> bật lại xác thực -88 (chỉ server NQSH cũ có lệnh này; server Pokémon HTBB KHÔNG có:
#                         mặc định TẮT để bản thường/phát hành đăng nhập được server Pokémon)
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
OUT="out/HTBB-v$V.jar"
B=out/build

rm -rf "$B" && mkdir -p "$B/tool" "$B/api" "$B/gen/game/render/screen" "$B/cls" "$B/cls45" out

# số bản jar gửi kèm khi đăng nhập (server real.BanMoiNhat báo khi có bản mới hơn)
# XAC_THUC: lệnh -88 (KiemServer) - mặc định tắt trên nhánh Pokémon (server HTBB không trả lời -88)
case "${XAC_THUC:-0}" in 1|bat|true) XT=true ;; *) XT=false ;; esac
printf 'package game.render.screen;\n\n/** Sinh bởi build.sh - KHÔNG sửa tay. */\nfinal class PhienBan {\n    static final int BAN = %s;\n    static final boolean XAC_THUC = %s;\n}\n' "$V" "$XT" > "$B/gen/game/render/screen/PhienBan.java"

"$J/javac" -nowarn -XDsuppressNotes -encoding UTF-8 -d "$B/tool" -cp tools/asm.jar tools/Down45.java
"$J/javac" -nowarn -XDsuppressNotes --release 8 -encoding UTF-8 -d "$B/api" -cp "tools/midp.jar${S}tools/microemulator.jar" $(find api -name '*.java')
find src ../shared/mod "$B/gen" -name '*.java' > "$B/nguon.txt"
"$J/javac" -nowarn -XDsuppressNotes --release 8 -encoding UTF-8 -d "$B/cls" \
    -cp "tools/midp.jar${S}tools/microemulator.jar${S}$B/api" @"$B/nguon.txt"

"$J/java" -cp "$B/tool${S}tools/asm.jar" Down45 d "$B/cls" "$B/cls45"

# Pokémon (Huyền Thoại Bảo Bối): sprite/tile/từ điển đóng gói sẵn trong res/pk, res/pt
if [ -n "$HTBB_ASSETS" ]; then
    mkdir -p "$B/dg"
    "$J/javac" -nowarn -encoding UTF-8 -d "$B/dg" src/game/pokemon/PokeDuLieu.java src/game/pokemon/PokeDongTac.java src/game/pokemon/HieuUngBang.java tools/DongGoiPokeJ2me.java
    "$J/java" -Djava.awt.headless=true -cp "$B/dg" DongGoiPokeJ2me "$HTBB_ASSETS" res res_nhe
fi
[ -f res/pk/chu.bin ] || echo "CANH BAO: thieu res/pk (sprite Pokemon) - chay voi HTBB_ASSETS=... de dong goi"
[ -f res/pk/hu.bin ] || echo "CANH BAO: thieu res/pk/hu.bin (hieu ung chieu Pokemon) - chay voi HTBB_ASSETS=... de dong goi"

# MANIFEST chung: MAY_CHU_CHINH -> "HTBB-May-Chu" cho mọi bản
sed '/^\r*$/d' MANIFEST.MF > "$B/MANIFEST.MF"   # bỏ dòng trống cuối (hết phần chính)
if [ -n "$MAY_CHU_CHINH" ]; then
    printf 'HTBB-May-Chu: %s\n' "$MAY_CHU_CHINH" >> "$B/MANIFEST.MF"
else
    echo "CANH BAO: chua dat MAY_CHU_CHINH=host:cong - ban thuong/phat hanh dung danh sach server tai ve"
fi

rm -f "$OUT"
"$J/jar" cfm "$OUT" "$B/MANIFEST.MF" -C "$B/cls45" . -C res .
echo "OK -> $OUT"

if [ -n "$MAY_CHU" ]; then
    THU="out/HTBB-v$V-thu.jar"
    grep -v '^HTBB-May-Chu:' "$B/MANIFEST.MF" > "$B/MANIFEST-thu.MF"
    printf 'HTBB-May-Chu: %s\nHTBB-Ban-Thu: 1\n' "$MAY_CHU" >> "$B/MANIFEST-thu.MF"
    rm -f "$THU"
    "$J/jar" cfm "$THU" "$B/MANIFEST-thu.MF" -C "$B/cls45" . -C res .
    echo "OK -> $THU (server thu: $MAY_CHU)"
fi

# Bản phát hành: ProGuard (release.pro) đổi tên toàn bộ class/hàm/biến thành a, b, c...
# mapping-vNN.txt = bảng tên cũ <-> mới: GIỮ RIÊNG, KHÔNG phát kèm, KHÔNG đưa lên git.
REL="out/HTBB-v$V-release.jar"
rm -f "$REL"
# ProGuard không chịu tên file có dấu cách -> làm trên tên tạm
cp "$OUT" out/pg-in.jar && rm -f out/pg-out.jar
printf -- '-libraryjars "%s"(java/**)\n' "$RT_JAR" > out/rt.pro
"$J/java" -jar tools/proguard.jar @release.pro @out/rt.pro -injars out/pg-in.jar -outjars out/pg-out.jar -printmapping "out/mapping-v$V.txt" > out/proguard-v$V.log 2>&1     || { tail -20 out/proguard-v$V.log; exit 1; }
mv out/pg-out.jar "$REL" && rm -f out/pg-in.jar
echo "OK -> $REL (mapping: out/mapping-v$V.txt)"

# Bản NHẸ (GOI=nhe): như bản phát hành nhưng res/pk thay bằng res_nhe/pk
if [ "$GOI" = nhe ]; then
    [ -f res_nhe/pk/chu.bin ] || { echo "Thieu res_nhe/pk - chay voi HTBB_ASSETS=... de dong goi ban nhe"; exit 1; }
    NHE="out/HTBB-v$V-nhe.jar"
    rm -rf "$B/res_nhe" && mkdir -p "$B/res_nhe"
    for e in res/*; do [ "$(basename "$e")" = pk ] || cp -r "$e" "$B/res_nhe/"; done
    cp -r res_nhe/pk "$B/res_nhe/pk"
    rm -f out/pg-in.jar out/pg-out.jar
    "$J/jar" cfm out/pg-in.jar "$B/MANIFEST.MF" -C "$B/cls45" . -C "$B/res_nhe" .
    "$J/java" -jar tools/proguard.jar @release.pro @out/rt.pro -injars out/pg-in.jar -outjars out/pg-out.jar -printmapping "out/mapping-v$V-nhe.txt" > out/proguard-v$V-nhe.log 2>&1     || { tail -20 out/proguard-v$V-nhe.log; exit 1; }
    rm -f "$NHE" && mv out/pg-out.jar "$NHE" && rm -f out/pg-in.jar
    echo "OK -> $NHE ($(wc -c < "$NHE") byte)"
fi
