#!/bin/sh
# Build bản PC (Java SE) Huyền Thoại Bảo Bối = CODE BẢN JAR J2ME chạy trên lớp tương thích MIDP cho máy tính.
#
#   sh client_pc/build.sh                       -> out/HTBB-PC.jar (bản THỬ: server 127.0.0.1:19129, "HTBB-Ban-Thu")
#   MAY_CHU=192.168.1.5:19129 sh client_pc/build.sh   -> bản thử nối server khác
#   KIEU=phat-hanh sh client_pc/build.sh        -> bản phát hành: BẮT BUỘC có máy chủ MAY_CHU_CHINH=host:cong hoặc
#                                                MAY_CHU_CHINH= trong ../tools/phat-hanh/may_chu.conf (thiếu là DỪNG)
#   BAN=901 / PHIEN_BAN=0.9.1 ...             -> mặc định đọc ../tools/phat-hanh/phien_ban.conf
#   APP_IMAGE=0                               -> bỏ bước jpackage (app-image Linux/macOS)
#
# Ra (client_pc/out/):
#   HTBB-PC.jar         fat jar (game + tài nguyên client_jar/res + phông chữ + biểu tượng), Java 8+ chạy được
#   HTBB.sh / HTBB.bat / HTBB.command   trình chạy (Linux / Windows / macOS) - để cạnh HTBB-PC.jar
#   app-image/HTBB/     (nếu có jpackage) ứng dụng Linux kèm JRE riêng: app-image/HTBB/bin/HTBB
# Windows .exe + JRE đi kèm: sh client_pc/dong_goi_windows.sh (xem README.md).
#
# Một nguồn duy nhất: port-j2me.py chép client_jar/src + client_jar/shared/mod vào out/build/gen mỗi lần build (vá tối thiểu,
# mẫu vá lệch là DỪNG). Lớp MIDP + cửa sổ / HD: client_pc/src. Cần: JDK 9+ (javac --release 8), python3.
set -e
cd "$(dirname "$0")"
GOC=$(pwd)
J2ME=../client_jar
doc_conf() { [ -f "../tools/phat-hanh/$1" ] && sed -n "s/^$2=//p" "../tools/phat-hanh/$1" | tail -1 | tr -d '\r" ' || true; }
KIEU=${KIEU:-thu}
PHIEN_BAN=${PHIEN_BAN:-$(doc_conf phien_ban.conf PHIEN_BAN)}
BAN=${BAN:-$(doc_conf phien_ban.conf BAN)}
BAN=${BAN:-1}
case "$BAN" in ''|*[!0-9]*) echo "LOI: BAN '$BAN' khong hop le"; exit 1 ;; esac

# ---- máy chủ ----
if [ "$KIEU" = phat-hanh ] || [ "$KIEU" = release ]; then
    [ -n "$MAY_CHU_CHINH" ] || MAY_CHU_CHINH=$(doc_conf may_chu.conf MAY_CHU_CHINH)
    case "$MAY_CHU_CHINH" in
        ?*:[0-9]*) ;;
        *) echo "LOI: ban phat hanh can may chu production host:cong. Dat MAY_CHU_CHINH=host:cong hoac"
           echo "     MAY_CHU_CHINH= trong tools/phat-hanh/may_chu.conf (cp tools/phat-hanh/may_chu.conf.example tools/phat-hanh/may_chu.conf)."
           exit 1 ;;
    esac
    case "$MAY_CHU_CHINH" in *nqsh-h2*) echo "LOI: $MAY_CHU_CHINH la server NQSH-H2"; exit 1 ;; esac
    MC=$MAY_CHU_CHINH
    THU=0
else
    MC=${MAY_CHU:-127.0.0.1:19129}
    THU=1
fi

# ---- JDK ----
if [ -z "$JAVA_HOME" ]; then
    for j in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/java-17-openjdk-amd64 /usr/lib/jvm/default-java; do
        [ -x "$j/bin/javac" ] && JAVA_HOME="$j" && break
    done
fi
if [ -n "$JAVA_HOME" ]; then JB="$JAVA_HOME/bin/"; else JB=""; fi
"${JB}javac" --release 8 -version >/dev/null 2>&1 || { echo "LOI: can JDK 9+ (javac --release 8) - dat JAVA_HOME"; exit 1; }
PY=${PYTHON:-python3}

OUT=${HTBB_PC_OUT:-out}   # HTBB_PC_OUT=<thư mục tuyệt đối>: build vào chỗ khác (nhiều người build song song)
B=$OUT/build
rm -rf "$B/cls" "$B/res" && mkdir -p "$B/gen" "$B/cls" "$B/res" "$OUT"

# ---- 1. code game từ bản jar ----
"$PY" -I port-j2me.py "$J2ME/src" ../client_jar/shared/mod "$B/gen" "$BAN"

# ---- 2. biên dịch (Java 8) ----
# client_jar/shared/se: mã Java SE dùng chung PC + Android (bộ cập nhật htbb.capnhat) - không vào bản jar J2ME
find src "$B/gen" ../client_jar/shared/se -name '*.java' > "$B/nguon.txt"
if ! "${JB}javac" -nowarn -XDsuppressNotes --release 8 -encoding UTF-8 -d "$B/cls" @"$B/nguon.txt" > "$B/javac.log" 2>&1; then
    grep -v '^Note:\|^warning: \[options\]\|^1 warning' "$B/javac.log" | head -60
    echo "LOI: bien dich that bai"; exit 1
fi
[ -f "$B/cls/htbb/pc/Chay.class" ] && [ -f "$B/cls/game/GameMidlet.class" ] || { echo "LOI: bien dich that bai"; exit 1; }

# ---- 3. tài nguyên ----
cp -r "$J2ME/res/." "$B/res/"
# banner dùng chung (agent thương hiệu): client_jar/shared/banner/res đè lên client_jar/res nếu có tệp
if [ -d ../client_jar/shared/banner/res ] && [ -n "$(find ../client_jar/shared/banner/res -type f 2>/dev/null | head -1)" ]; then
    cp -r ../client_jar/shared/banner/res/. "$B/res/"
fi
[ -f "$B/res/GHI_CONG.txt" ] || { echo "LOI: thieu client_jar/res/GHI_CONG.txt (ghi cong PMDCollab CC BY-NC 4.0)"; exit 1; }
mkdir -p "$B/res/phong" "$B/res/htbb-pc" "$B/res/am_hq"
cp res/phong/*.ttf res/phong/*.txt "$B/res/phong/"
cp res/am_hq/*.wav "$B/res/am_hq/"     # tiếng 44.1 kHz/16 bit (tools/am_thanh_hq.py)
for n in 16 24 32 48 64 128 256 512; do
    [ -f "../client_jar/shared/brand/icon_$n.png" ] && cp "../client_jar/shared/brand/icon_$n.png" "$B/res/htbb-pc/icon_$n.png"
done
# thuộc tính MIDlet (MANIFEST.MF bản jar) + máy chủ + phiên bản -> /htbb-app.properties
SO_PB=$(printf '%s' "$PHIEN_BAN" | sed -n 's/^\([0-9]*\.[0-9]*\(\.[0-9]*\)\{0,1\}\).*/\1/p')
{
    echo "# Sinh boi client_pc/build.sh - KHONG sua tay"
    sed '/^\r*$/d; s/\r$//' "$J2ME/MANIFEST.MF" | grep ':' | { if [ -n "$SO_PB" ]; then sed "s/^MIDlet-Version:.*/MIDlet-Version: $SO_PB/"; else cat; fi; } \
        | sed 's/^\([^:]*\): *\(.*\)$/\1=\2/'
    [ -n "$PHIEN_BAN" ] && echo "HTBB-Phien-Ban=$PHIEN_BAN"
    echo "HTBB-Ban=$BAN"
    echo "HTBB-May-Chu=$MC"
    [ "$THU" = 1 ] && echo "HTBB-Ban-Thu=1"
    echo "HTBB-Nen=pc"
} > "$B/res/htbb-app.properties"

# ---- 4. fat jar ----
printf 'Manifest-Version: 1.0\nMain-Class: htbb.pc.Chay\nImplementation-Title: Huyen Thoai Bao Boi (PC)\nImplementation-Version: %s\n' "${PHIEN_BAN:-$BAN}" > "$B/MANIFEST.MF"
rm -f "$OUT/HTBB-PC.jar"
"${JB}jar" cfm "$OUT/HTBB-PC.jar" "$B/MANIFEST.MF" -C "$B/cls" . -C "$B/res" .
cp ../client_jar/res/GHI_CONG.txt "$OUT/GHI_CONG.txt"
# soundbank MIDI (GPL-2) để RIÊNG cạnh jar: out/am/
mkdir -p "$OUT/am" && cp res/soundbank/TimGM6mb.sf2 res/soundbank/GPL-2.txt res/soundbank/GIAY_PHEP.txt "$OUT/am/"

# ---- 5. trình chạy ----
cat > "$OUT/HTBB.sh" <<'EOF'
#!/bin/sh
# Huyền Thoại Bảo Bối (PC) - Linux. Cần Java 8+ (hoặc thư mục jre/ cạnh tệp này). Tham số: --may-chu host:cong ...
D=$(cd "$(dirname "$0")" && pwd)
J=java
[ -x "$D/jre/bin/java" ] && J="$D/jre/bin/java"
exec "$J" -Xmx512m -Dfile.encoding=UTF-8 -Dsun.java2d.uiScale=1 -jar "$D/HTBB-PC.jar" "$@"
EOF
cp "$OUT/HTBB.sh" "$OUT/HTBB.command"
sed -i.bak 's/ - Linux\./ - macOS (bấm đúp trong Finder)./; s|exec "\$J" |exec "$J" -Xdock:name=HTBB |' "$OUT/HTBB.command" && rm -f "$OUT/HTBB.command.bak"
chmod +x "$OUT/HTBB.sh" "$OUT/HTBB.command"
printf '@echo off\r\nrem Huyen Thoai Bao Boi (PC) - Windows. Can Java 8+ (hoac thu muc jre\\ canh tep nay).\r\nset "J=javaw"\r\nif exist "%%~dp0jre\\bin\\javaw.exe" set "J=%%~dp0jre\\bin\\javaw.exe"\r\nstart "" "%%J%%" -Xmx512m -Dfile.encoding=UTF-8 -Dsun.java2d.uiScale=1 -jar "%%~dp0HTBB-PC.jar" %%*\r\n' > "$OUT/HTBB.bat"
echo "OK $OUT/HTBB-PC.jar ($(wc -c < "$OUT/HTBB-PC.jar") byte) - may chu $MC$([ "$THU" = 1 ] && echo ' (ban THU)'), phien ban ${PHIEN_BAN:-?} ($BAN)"

# ---- 6. app-image (jpackage, JRE riêng) ----
if [ "${APP_IMAGE:-1}" = 1 ] && [ -x "${JB}jpackage" -o -n "$(command -v jpackage 2>/dev/null)" ]; then
    JP="${JB}jpackage"; [ -x "$JP" ] || JP=jpackage
    rm -rf "$B/jp-in" "$OUT/app-image" && mkdir -p "$B/jp-in" "$OUT/app-image"
    cp -r "$OUT/HTBB-PC.jar" "$OUT/GHI_CONG.txt" "$OUT/am" "$B/jp-in/"
    IC=""
    [ -f "$B/res/htbb-pc/icon_256.png" ] && IC="--icon $B/res/htbb-pc/icon_256.png"
    if "$JP" --type app-image --input "$B/jp-in" --main-jar HTBB-PC.jar --main-class htbb.pc.Chay \
        --name HTBB --app-version "${SO_PB:-1.0}" --dest "$OUT/app-image" $IC \
        --java-options "-Xmx512m -Dfile.encoding=UTF-8 -Dsun.java2d.uiScale=1" \
        --add-modules java.desktop,java.logging,java.datatransfer > "$B/jpackage.log" 2>&1; then
        echo "OK $OUT/app-image/HTBB (jpackage: chay $OUT/app-image/HTBB/bin/HTBB)"
    else
        echo "CANH BAO: jpackage loi - xem $B/jpackage.log (jar van dung duoc)"
    fi
fi
