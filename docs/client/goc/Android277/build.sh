#!/bin/sh
# Build APK NQSH-H2 từ KPAH_277.apk gốc của TeaMobi (xem README.md).
#   sh build.sh                      -> out/KPAH277-NQSH-H2.apk
#   APK_GOC=... KEY_DIR=... sh build.sh
set -e
cd "$(dirname "$0")"

# ---- chỉnh theo máy (Windows Git Bash; trên Linux / GitHub Actions đặt qua biến môi trường) ----
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) WIN=1; S=";"; EXE=.exe; PY=python
        JMD="/c/Program Files/Java/jdk-26.0.1/bin"
        SMD="/c/Program Files/Unity/Hub/Editor/6000.5.9f1/Editor/Data/PlaybackEngines/AndroidPlayer/SDK" ;;
    *) WIN=; S=":"; EXE=; PY=python3
        JMD="$(dirname "$(command -v javac)")"
        SMD="${ANDROID_HOME:-$ANDROID_SDK_ROOT}" ;;
esac
APK_GOC=${APK_GOC:-goc/KPAH_277.apk}                                  # APK gốc TeaMobi (có trong repo)
KEY_DIR=${KEY_DIR:-"$HOME/Desktop/KPAH-All/_tmp_build/android-key"}   # key.properties + keystore, KHÔNG đưa vào git
JDK=${JDK:-$JMD}
SDK=${SDK:-$SMD}
APKTOOL=${APKTOOL:-"$HOME/Desktop/KPAH-All/_tmp_build/tools/apktool/apktool.jar"}
DEX2JAR=${DEX2JAR:-"$HOME/Desktop/KPAH-All/_tmp_build/tools/dex2jar/dex-tools-v2.4"}   # thư mục có lib/*.jar
JAR_SRC=${JAR_SRC:-../shared/mod/game/render/screen}   # code mod dùng chung (shared/mod)
PKG=${PKG:-com.nqsh.h2}
TEN=${TEN:-NQSH-H2}
BAN=${BAN:-1419}                       # versionCode: phat-hanh.sh tu tang, ban goc TeaMobi = 1418
OUT=${OUT:-KPAH277-NQSH-H2.apk}
# ------------------------

# build-tools / platform mới nhất có trong SDK (có thể ép bằng BT=... PL=...)
BT=${BT:-$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)}
PL=${PL:-$(ls -d "$SDK"/platforms/android-* | sort -V | tail -1)}
PLW=$PL
[ -n "$WIN" ] && PLW=$(cygpath -w "$PL")   # classpath "a;b" cho javac trên Windows cần đường dẫn Windows
W=work
rm -rf $W && mkdir -p $W/cls $W/dex $W/modsrc out

# 1. Lớp của APK gốc (dex2jar) làm classpath -> biên dịch mod dựa trên lớp thật của game
$PY -I -c "import zipfile,sys;zipfile.ZipFile(sys.argv[1]).extract('classes.dex',sys.argv[2])" "$APK_GOC" $W
D2J="$DEX2JAR/lib/*"
[ -n "$WIN" ] && D2J="$(cygpath -w "$DEX2JAR/lib")\\*"
"$JDK/java" -cp "$D2J" com.googlecode.dex2jar.tools.Dex2jarCmd -f -o $W/apk.jar $W/classes.dex >/dev/null 2>&1
test -f $W/apk.jar

# 2. Code mod: tính năng dùng chung chép từ bản jar (port-android.py đổi tên lớp) + phần riêng Android (src/)
$PY -I port-android.py "$JAR_SRC" $W/modsrc
printf 'package game.render.screen;\n\n/** Sinh boi build.sh - KHONG sua tay. */\nfinal class PhienBan {\n    static final int BAN = %s;\n}\n' "$BAN" > $W/modsrc/PhienBan.java
"$JDK/javac" -nowarn --release 8 -encoding UTF-8 -cp "$W/apk.jar$S$PLW/android.jar" -d $W/cls \
    $(find src $W/modsrc -name "*.java")
"$JDK/java" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --release --min-api 26 --lib "$PL/android.jar" \
    --classpath $W/apk.jar --output $W/dex $(find $W/cls -name "*.class")
(cd $W/dex && $PY -I -c "import zipfile;z=zipfile.ZipFile('../mod.apk','w');z.write('classes.dex');z.close()")
"$JDK/java" -jar "$APKTOOL" d -f --no-res -o $W/modsmali $W/mod.apk >/dev/null

# 3. giải nén APK gốc, vá smali + manifest
"$JDK/java" -jar "$APKTOOL" d -f -o $W/smali-src "$APK_GOC" >/dev/null
$PY -I patch.py $W/smali-src $W/build-src $W/modsmali "$PKG" "$TEN" "$BAN"

# 4. đóng gói, căn chỉnh, ký
"$JDK/java" -jar "$APKTOOL" b -o $W/unsigned.apk $W/build-src >/dev/null
"$BT/zipalign$EXE" -f -p 4 $W/unsigned.apk $W/aligned.apk
P=$(grep ^password= "$KEY_DIR/key.properties" | cut -d= -f2-)
A=$(grep ^alias= "$KEY_DIR/key.properties" | cut -d= -f2-)
K=$(grep ^keystore= "$KEY_DIR/key.properties" | cut -d= -f2-)
"$JDK/java" -jar "$BT/lib/apksigner.jar" sign --ks "$KEY_DIR/$K" --ks-key-alias "$A" \
    --ks-pass "pass:$P" --key-pass "pass:$P" --out "out/$OUT" $W/aligned.apk 2>/dev/null
rm -f "out/$OUT.idsig"
echo "OK out/$OUT"
