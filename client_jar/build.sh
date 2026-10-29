#!/bin/sh
# Build bản jar J2ME (client TeaMobi 2.7.6 + mod) hoàn toàn từ source:
#   src/          code game (decompile từ jar gốc TeaMobi) + phần riêng J2ME của mod (Nen, TaiDanhSach)
#   shared/mod code dùng chung với bản Android (AutoGuard, TuDong = Tự động đánh chính thức, PauseMenu...)
#   res/          ảnh + dữ liệu game, MANIFEST.MF
#   api/          khai báo API WMA (SMS) chỉ để biên dịch - máy J2ME có sẵn, không đóng vào jar
#
#   sh build.sh 54     -> out/HTBB-v54.jar + bản phát hành ...-release.jar (ProGuard)
#   MAY_CHU=127.0.0.1:19129 sh build.sh 54
#                      -> bản THỬ ...-v54-thu.jar: MANIFEST thêm "HTBB-May-Chu: host:cong" = chỉ một server thử
#                         Không ảnh hưởng bản thường/phát hành.
#   HTBB_ASSETS=../client_pc_ios/Assets sh build.sh 54        (Assets client PC)
#   HTBB_ASSETS=pc sh build.sh 54                  (viết tắt của ../client_pc_ios/Assets)
#                      -> trước khi build, đóng gói lại sprite + hiệu ứng chiêu Pokémon (res/pk, res/pt, và bản
#                         nhẹ res_nhe/pk) bằng tools/DongGoiPokeJ2me.java (đọc thêm HTBB du_lieu/*.csv)
#   GOI=nhe sh build.sh 54
#                      -> thêm bản NHẸ cho máy thật ...-v54-nhe.jar (ProGuard + res_nhe/pk: ít loài, ít khung,
#                         chân dung 24px; loài thiếu vẽ bằng bóng đen, xem DongGoiPokeJ2me; âm thanh res_nhe/am:
#                         chỉ tiếng hiệu ứng, không nhạc)
#   Âm thanh res/am (+ res_nhe/am): python3 -P tools/lam_am_thanh.py <goi Kenney CC0> res/am res_nhe/am
#   MAY_CHU_CHINH=host:cong sh build.sh 54
#                      -> MỌI bản (thường/phát hành/nhẹ) chỉ vào server Pokémon này ("HTBB-May-Chu" trong
#                         MANIFEST), không tải danh sách server cũ. BẮT BUỘC: không có biến này thì đọc
#                         MAY_CHU_CHINH= trong ../tools/phat-hanh/may_chu.conf (mẫu may_chu.conf.example); thiếu cả hai
#                         là DỪNG. Thử trên máy: MAY_CHU_CHINH=127.0.0.1:19129.
#                         Bản DEV: chỉ đặt MAY_CHU (không MAY_CHU_CHINH, không may_chu.conf) thì vẫn build đủ
#                         (thường/thử/phát hành/nhẹ) nhưng MỌI jar vào server MAY_CHU + "HTBB-Ban-Thu: 1" - không phát hành.
#   sh build.sh        -> số bản lấy BAN= trong ../tools/phat-hanh/phien_ban.conf; MANIFEST MIDlet-Version / HTBB-Phien-Ban
#                         / HTBB-Ban theo PHIEN_BAN= của tệp đó. Ghi công (CC BY-NC 4.0) res/GHI_CONG.txt bắt buộc có.
#
# Bước: javac (--release 8) -> Down45 (class 45 + StringBuffer, kiểm API CLDC) -> jar -> ProGuard.
set -e
cd "$(dirname "$0")"
# KHOA=giatri trong ../tools/phat-hanh/<tep> (dòng cuối thắng; không có = rỗng)
doc_conf() { [ -f "../tools/phat-hanh/$1" ] && sed -n "s/^$2=//p" "../tools/phat-hanh/$1" | tail -1 | tr -d '\r" ' || true; }
PHIEN_BAN=${PHIEN_BAN:-$(doc_conf phien_ban.conf PHIEN_BAN)}
V=${1:-$(doc_conf phien_ban.conf BAN)}
case "$V" in ''|*[!0-9]*) echo "LOI: so ban '$V' khong hop le (sh build.sh <so> hoac BAN= trong tools/phat-hanh/phien_ban.conf)"; exit 1 ;; esac
[ -n "$MAY_CHU_CHINH" ] || MAY_CHU_CHINH=$(doc_conf may_chu.conf MAY_CHU_CHINH)
BAN_DEV=0
if [ -z "$MAY_CHU_CHINH" ] && [ -n "$MAY_CHU" ]; then
    # bản DEV/THỬ (chỉ MAY_CHU): mọi jar vào server thử MAY_CHU và mang dấu HTBB-Ban-Thu - KHÔNG phát hành
    BAN_DEV=1
    echo "CANH BAO: ban DEV - khong co MAY_CHU_CHINH: moi jar vao server thu $MAY_CHU (HTBB-Ban-Thu: 1), KHONG phat hanh"
elif [ -z "$MAY_CHU_CHINH" ]; then
    echo "LOI: ban phat hanh can may chu production. Dat MAY_CHU_CHINH=host:cong, hoac MAY_CHU_CHINH= trong"
    echo "     tools/phat-hanh/may_chu.conf (cp tools/phat-hanh/may_chu.conf.example tools/phat-hanh/may_chu.conf)."
    echo "     Thu tren may: MAY_CHU_CHINH=127.0.0.1:19129 sh client_jar/build.sh $V"
    exit 1
else
    case "$MAY_CHU_CHINH" in
        *nqsh-h2*) echo "LOI: MAY_CHU_CHINH=$MAY_CHU_CHINH la server NQSH-H2 cu"; exit 1 ;;
    esac
    case "$MAY_CHU_CHINH" in
        ?*:[0-9]*) ;;
        *) echo "LOI: MAY_CHU_CHINH='$MAY_CHU_CHINH' phai co dang host:cong"; exit 1 ;;
    esac
fi
[ -f res/GHI_CONG.txt ] || { echo "LOI: thieu res/GHI_CONG.txt (ghi cong PMDCollab CC BY-NC 4.0) - chay tools/sinh_ghi_cong.py"; exit 1; }
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
printf 'package game.render.screen;\n\n/** Sinh bởi build.sh - KHÔNG sửa tay. */\nfinal class PhienBan {\n    static final int BAN = %s;\n}\n' "$V" > "$B/gen/game/render/screen/PhienBan.java"

"$J/javac" -nowarn -XDsuppressNotes -encoding UTF-8 -d "$B/tool" -cp tools/asm.jar tools/Down45.java
"$J/javac" -nowarn -XDsuppressNotes --release 8 -encoding UTF-8 -d "$B/api" -cp "tools/midp.jar${S}tools/microemulator.jar" $(find api -name '*.java')
find src shared/mod "$B/gen" -name '*.java' > "$B/nguon.txt"
"$J/javac" -nowarn -XDsuppressNotes --release 8 -encoding UTF-8 -d "$B/cls" \
    -cp "tools/midp.jar${S}tools/microemulator.jar${S}$B/api" @"$B/nguon.txt"

# 0.9.7.3: kiểm TRẮNG - mọi hàm / lớp java.* / javax.microedition.* phải có trong API chuẩn CLDC 1.1 + MIDP 2.0
# (tools/cldcapi11.jar, tools/midpapi20.jar của MicroEmulator 2.0.4) + WMA khai báo ở api/ - không còn lọt
# System.nanoTime() (CoffeeCat iOS: mọi nhịp game ném lỗi, kẹt "Đang tải dữ liệu game")
"$J/java" -Dapi="tools/cldcapi11.jar${S}tools/midpapi20.jar${S}$B/api" -cp "$B/tool${S}tools/asm.jar" Down45 d "$B/cls" "$B/cls45"

# Pokémon (Huyền Thoại Bảo Bối): sprite/tile/từ điển đóng gói sẵn trong res/pk, res/pt
[ "$HTBB_ASSETS" = "pc" ] && HTBB_ASSETS=../client_pc_ios/Assets
if [ -n "$HTBB_ASSETS" ]; then
    mkdir -p "$B/dg"
    "$J/javac" -nowarn -encoding UTF-8 -d "$B/dg" src/game/pokemon/PokeDuLieu.java src/game/pokemon/PokeDongTac.java src/game/pokemon/HieuUngBang.java tools/DongGoiPokeJ2me.java
    "$J/java" -Djava.awt.headless=true -cp "$B/dg" DongGoiPokeJ2me "$HTBB_ASSETS" res res_nhe
fi
[ -f res/pk/chu.bin ] || echo "CANH BAO: thieu res/pk (sprite Pokemon) - chay voi HTBB_ASSETS=... de dong goi"
[ -f res/pk/hu.bin ] || echo "CANH BAO: thieu res/pk/hu.bin (hieu ung chieu Pokemon) - chay voi HTBB_ASSETS=... de dong goi"

# MANIFEST chung: MAY_CHU_CHINH -> "HTBB-May-Chu" cho mọi bản
# MIDlet-Version (MIDP chỉ nhận số a.b[.c]) = phần số của PHIEN_BAN; HTBB-Phien-Ban = tên đầy đủ cho người chơi
SO_PB=$(printf '%s' "$PHIEN_BAN" | sed -n 's/^\([0-9]*\.[0-9]*\(\.[0-9]*\)\{0,1\}\).*/\1/p')
sed '/^\r*$/d' MANIFEST.MF | { if [ -n "$SO_PB" ]; then sed "s/^MIDlet-Version:.*/MIDlet-Version: $SO_PB/"; else cat; fi; } > "$B/MANIFEST.MF"
[ -n "$PHIEN_BAN" ] && printf 'HTBB-Phien-Ban: %s\n' "$PHIEN_BAN" >> "$B/MANIFEST.MF"
printf 'HTBB-Ban: %s\n' "$V" >> "$B/MANIFEST.MF"
if [ "$BAN_DEV" = 1 ]; then
    printf 'HTBB-May-Chu: %s\nHTBB-Ban-Thu: 1\n' "$MAY_CHU" >> "$B/MANIFEST.MF"
else
    printf 'HTBB-May-Chu: %s\n' "$MAY_CHU_CHINH" >> "$B/MANIFEST.MF"
fi

# res/ui (ảnh PNG giao diện + icon chiêu 0.9.5) chỉ dành cho PC / APK: J2ME vẽ vector / hình 12x12 -> bỏ khỏi jar
rm -rf "$B/res_j2me" && mkdir -p "$B/res_j2me"
for e in res/*; do [ "$(basename "$e")" = ui ] || cp -r "$e" "$B/res_j2me/"; done
rm -f "$OUT"
"$J/jar" cfm "$OUT" "$B/MANIFEST.MF" -C "$B/cls45" . -C "$B/res_j2me" .
echo "OK -> $OUT"

if [ -n "$MAY_CHU" ]; then
    THU="out/HTBB-v$V-thu.jar"
    grep -v '^HTBB-May-Chu:\|^HTBB-Ban-Thu:' "$B/MANIFEST.MF" > "$B/MANIFEST-thu.MF"
    printf 'HTBB-May-Chu: %s\nHTBB-Ban-Thu: 1\n' "$MAY_CHU" >> "$B/MANIFEST-thu.MF"
    rm -f "$THU"
    "$J/jar" cfm "$THU" "$B/MANIFEST-thu.MF" -C "$B/cls45" . -C "$B/res_j2me" .
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
# 0.9.22 (Nokia E72): kiểm lại API CLDC 1.1 / MIDP 2.0 trên JAR CUỐI (sau ProGuard) - class 45, chữ ký hàm / trường
"$J/java" -Dapi="tools/cldcapi11.jar${S}tools/midpapi20.jar${S}$B/api" -cp "$B/tool${S}tools/asm.jar" Down45 k "$REL"

# Bản NHẸ (GOI=nhe): như bản phát hành nhưng res/pk thay bằng res_nhe/pk
if [ "$GOI" = nhe ]; then
    [ -f res_nhe/pk/chu.bin ] || { echo "Thieu res_nhe/pk - chay voi HTBB_ASSETS=... de dong goi ban nhe"; exit 1; }
    NHE="out/HTBB-v$V-nhe.jar"
    rm -rf "$B/res_nhe" && mkdir -p "$B/res_nhe"
    for e in res/*; do case "$(basename "$e")" in pk|am|ui) ;; *) cp -r "$e" "$B/res_nhe/" ;; esac; done
    cp -r res_nhe/pk "$B/res_nhe/pk"
    # banner bản nhẹ: chỉ lớp cỏ tiền cảnh + logo nhỏ (trời vẽ bằng code) - bỏ núi / rừng / mây / khung
    rm -f "$B/res_nhe/bn/nui.png" "$B/res_nhe/bn/rung.png" "$B/res_nhe/bn/may.png" "$B/res_nhe/bn/rong.png" "$B/res_nhe/bn/khung.png" "$B/res_nhe/bn/logo.png"
    # âm thanh bản nhẹ: chỉ tiếng hiệu ứng 8 kHz, không nhạc (res_nhe/am, sinh bởi tools/lam_am_thanh.py)
    [ -d res_nhe/am ] && cp -r res_nhe/am "$B/res_nhe/am"
    rm -f out/pg-in.jar out/pg-out.jar
    "$J/jar" cfm out/pg-in.jar "$B/MANIFEST.MF" -C "$B/cls45" . -C "$B/res_nhe" .
    "$J/java" -jar tools/proguard.jar @release.pro @out/rt.pro -injars out/pg-in.jar -outjars out/pg-out.jar -printmapping "out/mapping-v$V-nhe.txt" > out/proguard-v$V-nhe.log 2>&1     || { tail -20 out/proguard-v$V-nhe.log; exit 1; }
    rm -f "$NHE" && mv out/pg-out.jar "$NHE" && rm -f out/pg-in.jar
    "$J/java" -Dapi="tools/cldcapi11.jar${S}tools/midpapi20.jar${S}$B/api" -cp "$B/tool${S}tools/asm.jar" Down45 k "$NHE"
    echo "OK -> $NHE ($(wc -c < "$NHE") byte)"
fi
