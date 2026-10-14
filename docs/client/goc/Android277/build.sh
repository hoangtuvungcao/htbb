#!/bin/sh
# Build APK NQSH-H2 từ KPAH_277.apk gốc của TeaMobi (xem README.md).
#   sh build.sh                      -> out/KPAH277-NQSH-H2.apk
#   APK_GOC=... KEY_DIR=... sh build.sh
set -e
cd "$(dirname "$0")"

# ---- chỉnh theo máy ----
APK_GOC=${APK_GOC:-"$HOME/Downloads/KPAH_277.apk"}
KEY_DIR=${KEY_DIR:-"$HOME/Desktop/KPAH-All/_tmp_build/android-key"}   # key.properties + keystore, KHÔNG đưa vào git
JDK=${JDK:-"/c/Program Files/Java/jdk-26.0.1/bin"}
SDK=${SDK:-"/c/Program Files/Unity/Hub/Editor/6000.5.9f1/Editor/Data/PlaybackEngines/AndroidPlayer/SDK"}
APKTOOL=${APKTOOL:-"$HOME/Desktop/KPAH-All/_tmp_build/tools/apktool/apktool.jar"}
PKG=${PKG:-com.nqsh.h2}
TEN=${TEN:-NQSH-H2}
BAN=${BAN:-1419}                       # versionCode: phat-hanh.sh tu tang, ban goc TeaMobi = 1418
OUT=${OUT:-KPAH277-NQSH-H2.apk}
# ------------------------

BT="$SDK/build-tools/36.0.0"
PL="$SDK/platforms/android-37.0"
PLW=$(cygpath -w "$PL" 2>/dev/null || echo "$PL")   # classpath "a;b" cho javac tren Windows can duong dan Windows
W=work
rm -rf $W && mkdir -p $W/stubcls $W/cls $W/dex out

# 1. NqshMod / NqshCapNhat -> dex -> smali (stub chỉ để biên dịch, không vào APK)
"$JDK/javac" -nowarn --release 8 -encoding UTF-8 -cp "$PL/android.jar" -d $W/stubcls $(find stub -name "*.java")
"$JDK/javac" -nowarn --release 8 -encoding UTF-8 -cp "$W/stubcls;$PLW/android.jar" -d $W/cls $(find src -name "*.java")
"$JDK/java" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --release --min-api 26 --lib "$PL/android.jar" \
    --classpath $W/stubcls --output $W/dex $(find $W/cls -name "*.class")
(cd $W/dex && python -I -c "import zipfile;z=zipfile.ZipFile('../mod.apk','w');z.write('classes.dex');z.close()")
"$JDK/java" -jar "$APKTOOL" d -f --no-res -o $W/modsmali $W/mod.apk >/dev/null

# 2. giải nén APK gốc, vá smali + manifest
"$JDK/java" -jar "$APKTOOL" d -f -o $W/smali-src "$APK_GOC" >/dev/null
python -I patch.py $W/smali-src $W/build-src $W/modsmali "$PKG" "$TEN" "$BAN"

# 3. đóng gói, căn chỉnh, ký
"$JDK/java" -jar "$APKTOOL" b -o $W/unsigned.apk $W/build-src >/dev/null
"$BT/zipalign.exe" -f -p 4 $W/unsigned.apk $W/aligned.apk
P=$(grep ^password= "$KEY_DIR/key.properties" | cut -d= -f2-)
A=$(grep ^alias= "$KEY_DIR/key.properties" | cut -d= -f2-)
K=$(grep ^keystore= "$KEY_DIR/key.properties" | cut -d= -f2-)
"$JDK/java" -jar "$BT/lib/apksigner.jar" sign --ks "$KEY_DIR/$K" --ks-key-alias "$A" \
    --ks-pass "pass:$P" --key-pass "pass:$P" --out "out/$OUT" $W/aligned.apk 2>/dev/null
rm -f "out/$OUT.idsig"
echo "OK out/$OUT"
