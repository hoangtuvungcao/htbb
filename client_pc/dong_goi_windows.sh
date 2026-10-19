#!/bin/sh
# Đóng gói bản PC Huyền Thoại Bảo Bối cho WINDOWS (+ Linux) như một game PC bình thường - chạy trên Linux:
#
#   sh client_pc/dong_goi_windows.sh                 -> bản THỬ (server 127.0.0.1:19129)
#   KIEU=phat-hanh sh client_pc/dong_goi_windows.sh  -> bản phát hành (BẮT BUỘC MAY_CHU_CHINH / tools/phat-hanh/may_chu.conf)
#
# 0.9.5: KHÔNG còn tệp .jar rời của game trong gói - như một game PC bình thường:
#   - mã game làm rối bằng ProGuard (release-pc.pro, như bản jar J2ME), bản đồ tên giữ riêng out/goi/mapping-pc.txt;
#   - Java đi kèm = runtime/ TỐI GIẢN dựng bằng jlink (chỉ module client cần, theo jdeps: java.base, java.desktop,
#     jdk.management + jdk.crypto.ec, jdk.localedata vi/en; --strip-debug --no-header-files --no-man-pages
#     --compress) thay thư mục jre/ Temurin đầy đủ. jlink luôn sinh runtime/lib/jrt-fs.jar (tệp của JDK, không phải game).
#
# Ra (client_pc/out/goi/):
#   HTBB-PC-Setup-<PHIEN_BAN>.exe        bộ cài (Inno Setup qua wine; CAI_DAT=0 để bỏ): HTBB.exe (Launch4j, jar game
#                                        NHÚNG trong exe) + runtime/ + am/ + GHI_CONG.txt + DOC_TRUOC.txt + giay_phep/
#   HTBB-PC-Windows-<PHIEN_BAN>.zip      cùng bố cục, bản chạy ngay (portable)
#   HTBB-PC-Linux-<PHIEN_BAN>.AppImage   MỘT tệp (appimagetool): AppRun + runtime/ (jlink Linux) + jar trong squashfs
#   HTBB-PC-Linux-<PHIEN_BAN>.tar.gz     HTBB/HTBB (trình chạy) + runtime/ (jar game giấu trong runtime/lib/htbb/)
#   HTBB-PC-<PHIEN_BAN>.jar              jar PC (đã làm rối) - tệp riêng, chủ server quyết có phát hành không
#
# Công cụ (tải một lần vào client_pc/out/cong_cu/, KIỂM TRA mã băm, không đưa lên git):
#   - Launch4j 3.50 (BSD-3, sourceforge.net/projects/launch4j) bản Linux: tạo HTBB.exe (bọc HTBB-PC.jar + biểu tượng
#     client_jar/shared/brand/htbb.ico + thông tin phiên bản, jar game NHÚNG trong exe), JVM = runtime\ jlink đi kèm.
#   - Eclipse Temurin 21 JDK x64 Windows + Linux (adoptium.net, GPLv2 + Classpath Exception) - lấy jmods để jlink;
#     mã băm SHA-256 từ API Adoptium, lệch là DỪNG. Ghim phiên bản bằng JRE_BAN (mặc định 21.0.12.1+1).
#   - appimagetool 1.9.1 + type2-runtime 20251108 (GitHub AppImage, MIT; SHA-256 ghim bên dưới).
#   - Inno Setup 6.7.3 (miễn phí, jrsoftware.org, SHA-256 kiểm theo GitHub release) cài vào WINEPREFIX riêng out/cong_cu/wine nếu có wine.
# Dùng tệp có sẵn: LAUNCH4J_DIR=..., WINEPREFIX=... + ISCC=... (đường dẫn ISCC.exe trong wine).
#
# Trên Windows (không có Linux): xem README.md mục "Windows" (jpackage --type exe/msi + WiX, hoặc Launch4j Windows).
set -e
cd "$(dirname "$0")"
GOC=$(pwd)
doc_conf() { [ -f "../tools/phat-hanh/$1" ] && sed -n "s/^$2=//p" "../tools/phat-hanh/$1" | tail -1 | tr -d '\r" ' || true; }
PHIEN_BAN=${PHIEN_BAN:-$(doc_conf phien_ban.conf PHIEN_BAN)}
PHIEN_BAN=${PHIEN_BAN:-0.9.0}
SO_PB=$(printf '%s' "$PHIEN_BAN" | sed -n 's/^\([0-9]*\.[0-9]*\(\.[0-9]*\)\{0,1\}\).*/\1/p')
SO_PB=${SO_PB:-0.9.0}
TEN_PB=$SO_PB
JRE_BAN=${JRE_BAN:-21.0.12.1+1}
JRE_TEP=$(printf '%s' "$JRE_BAN" | tr '+' '_')
CC=out/cong_cu
GOI=out/goi
mkdir -p "$CC" "$GOI"

# ---- 1. jar ----
APP_IMAGE=0 sh build.sh

tai() { # tai <url> <tệp ra>
    [ -f "$2" ] && return 0
    echo "tai $1"
    curl -fsSL -m 900 -o "$2.tam" "$1" && mv "$2.tam" "$2"
}
kiem_sha() { # kiem_sha <tệp> <sha256>
    co=$(sha256sum "$1" | cut -d' ' -f1)
    [ "$co" = "$2" ] || { echo "LOI: SHA-256 $1 = $co, can $2 - xoa tep va tai lai"; exit 1; }
    echo "SHA-256 OK $1"
}

# ---- 2. Launch4j ----
if [ -z "$LAUNCH4J_DIR" ]; then
    LAUNCH4J_DIR=$CC/launch4j
    if [ ! -x "$LAUNCH4J_DIR/launch4j" ]; then
        tai "https://sourceforge.net/projects/launch4j/files/launch4j-3/3.50/launch4j-3.50-linux-x64.tgz/download" "$CC/launch4j-3.50-linux-x64.tgz"
        # md5 công bố trên SourceForge (RSS launch4j-3/3.50) + SHA-256 ghi lại lúc kiểm (2026-10-18)
        echo "aad881ea23e7466b21bb0dd6a36b37a9  $CC/launch4j-3.50-linux-x64.tgz" | md5sum -c -
        kiem_sha "$CC/launch4j-3.50-linux-x64.tgz" a6ad95c2fb6300410543288308b4eeae72ecd1df033928dfc524ba539bbc4d36
        tar xzf "$CC/launch4j-3.50-linux-x64.tgz" -C "$CC"
    fi
fi

# ---- 3. JDK Temurin (jmods) -> runtime jlink tối giản ----
jdk() { # jdk <os> <đuôi> -> tệp JDK (tải nếu chưa có, kiểm SHA-256 với API Adoptium)
    t=$CC/OpenJDK21U-jdk_x64_$1_hotspot_$JRE_TEP.$2
    if [ ! -f "$t" ]; then
        tai "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-$(printf '%s' "$JRE_BAN" | sed 's/+/%2B/')/OpenJDK21U-jdk_x64_$1_hotspot_$JRE_TEP.$2" "$t" >&2
    fi
    co=$(sha256sum "$t" | cut -d' ' -f1)
    can=$(curl -fsS -m 60 "https://api.adoptium.net/v3/assets/release_name/eclipse/jdk-$(printf "%s" "$JRE_BAN" | sed "s/+/%2B/")?architecture=x64&image_type=jdk&os=$1&jvm_impl=hotspot" \
        | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['binaries'][0]['package']['checksum'])" 2>/dev/null || true)
    if [ -z "$can" ]; then
        echo "CANH BAO: khong hoi duoc ma bam Adoptium (mang) - dung tep da tai $t ($co)" >&2
    elif [ "$co" != "$can" ]; then
        echo "LOI: SHA-256 $t = $co, can $can - xoa tep va tai lai" >&2
        exit 1
    fi
    echo "$t"
}
JDK_LINUX=$(jdk linux tar.gz)
JDK_WIN=$(jdk windows zip)
rm -rf "$CC/jdk_linux" "$CC/jdk_win" && mkdir -p "$CC/jdk_linux" "$CC/jdk_win"
tar xzf "$JDK_LINUX" -C "$CC/jdk_linux" --strip-components=1
(cd "$CC/jdk_win" && unzip -q "$GOC/$JDK_WIN" && d=$(ls) && mv "$d"/* . && rmdir "$d")
JL=$CC/jdk_linux/bin
MODULE=java.base,java.desktop,jdk.management,jdk.crypto.ec,jdk.localedata
# jdeps kiểm danh sách module: jar cần module nào chưa có trong MODULE thì DỪNG
CAN=$("$JL/jdeps" --multi-release 21 --print-module-deps --ignore-missing-deps out/HTBB-PC.jar)
for m in $(printf '%s' "$CAN" | tr ',' ' '); do
    case ",$MODULE," in *",$m,"*) ;; *) echo "LOI: jdeps can module $m chua co trong MODULE"; exit 1 ;; esac
done
jlink_ra() { # jlink_ra <jmods> <thư mục ra>
    rm -rf "$2"
    "$JL/jlink" --module-path "$1" --add-modules "$MODULE" --include-locales=en,vi \
        --strip-debug --no-header-files --no-man-pages --compress=zip-9 --output "$2"
}
jlink_ra "$CC/jdk_linux/jmods" "$GOI/runtime_linux"
jlink_ra "$CC/jdk_win/jmods" "$GOI/runtime_win"
echo "OK runtime jlink ($MODULE): linux $(du -sh "$GOI/runtime_linux" | cut -f1), windows $(du -sh "$GOI/runtime_win" | cut -f1)"
"$GOI/runtime_linux/bin/java" --list-modules | sed 's/@.*//' | tr '\n' ' '; echo
grep '^MODULES=' "$GOI/runtime_win/release"

# ---- 3b. ProGuard: làm rối mã game (như jar J2ME phát hành) ----
PG=../client_jar/tools/proguard.jar
"$JL/java" -jar "$PG" @release-pc.pro \
    -libraryjars "$CC/jdk_linux/jmods/java.base.jmod(!**.jar;!module-info.class)" \
    -libraryjars "$CC/jdk_linux/jmods/java.desktop.jmod(!**.jar;!module-info.class)" \
    -libraryjars "$CC/jdk_linux/jmods/java.management.jmod(!**.jar;!module-info.class)" \
    -libraryjars "$CC/jdk_linux/jmods/jdk.management.jmod(!**.jar;!module-info.class)" \
    -injars out/HTBB-PC.jar -outjars "$GOI/HTBB-PC-roi.jar" -printmapping "$GOI/mapping-pc.txt" > "$GOI/proguard-pc.log" 2>&1 \
    || { tail -30 "$GOI/proguard-pc.log"; exit 1; }
cp "$GOI/HTBB-PC-roi.jar" "$GOI/HTBB-PC-$TEN_PB.jar"
echo "OK ProGuard $GOI/HTBB-PC-$TEN_PB.jar ($(wc -c < "$GOI/HTBB-PC-roi.jar") byte; ban do ten giu rieng: $GOI/mapping-pc.txt)"

# ---- 4. HTBB.exe (Launch4j) ----
W=$GOI/windows/HTBB
rm -rf "$GOI/windows" && mkdir -p "$W"
cat > "$GOI/launch4j.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>gui</headerType>
  <jar>$GOC/$GOI/HTBB-PC-roi.jar</jar>
  <outfile>$GOC/$W/HTBB.exe</outfile>
  <errTitle>Huyen Thoai Bao Boi</errTitle>
  <cmdLine></cmdLine>
  <chdir>.</chdir>
  <priority>normal</priority>
  <downloadUrl>https://adoptium.net/</downloadUrl>
  <supportUrl></supportUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
  <icon>$GOC/../client_jar/shared/brand/htbb.ico</icon>
  <jre>
    <path>%EXEDIR%\\runtime</path>
    <requiresJdk>false</requiresJdk>
    <requires64Bit>true</requires64Bit>
    <minVersion></minVersion>
    <maxVersion></maxVersion>
    <initialHeapSize>64</initialHeapSize>
    <maxHeapSize>512</maxHeapSize>
    <opt>-Dfile.encoding=UTF-8</opt>
    <opt>-Dsun.java2d.uiScale=1</opt>
    <opt>-Dsun.java2d.d3d=true</opt>
    <opt>-Dsun.java2d.noddraw=false</opt>
  </jre>
  <versionInfo>
    <fileVersion>$SO_PB.0</fileVersion>
    <txtFileVersion>$PHIEN_BAN</txtFileVersion>
    <fileDescription>Huyen Thoai Bao Boi</fileDescription>
    <copyright>HTBB</copyright>
    <productVersion>$SO_PB.0</productVersion>
    <txtProductVersion>$PHIEN_BAN</txtProductVersion>
    <productName>Huyen Thoai Bao Boi $TEN_PB</productName>
    <companyName>HTBB</companyName>
    <internalName>HTBB</internalName>
    <originalFilename>HTBB.exe</originalFilename>
    <trademarks></trademarks>
    <language>ENGLISH_US</language>
  </versionInfo>
</launch4jConfig>
EOF
JB=java
[ -n "$JAVA_HOME" ] && JB="$JAVA_HOME/bin/java"
"$JB" -jar "$LAUNCH4J_DIR/launch4j.jar" "$GOC/$GOI/launch4j.xml" > "$GOI/launch4j.log" 2>&1 || { cat "$GOI/launch4j.log"; exit 1; }
[ -f "$W/HTBB.exe" ] || { echo "LOI: Launch4j khong tao duoc HTBB.exe"; cat "$GOI/launch4j.log"; exit 1; }
echo "OK $W/HTBB.exe ($(wc -c < "$W/HTBB.exe") byte)"

# ---- 5. thư mục chơi Windows: runtime\ (jlink) + dữ liệu + giấy phép ----
cp -r "$GOI/runtime_win" "$W/runtime"
cp -r out/am "$W/am"
cp out/GHI_CONG.txt "$W/GHI_CONG.txt"
mkdir -p "$W/giay_phep"
cp res/phong/OFL-NotoSans.txt res/phong/OFL-Baloo2.txt res/phong/GIAY_PHEP.txt "$W/giay_phep/"
cp "$LAUNCH4J_DIR/LICENSE.txt" "$W/giay_phep/Launch4j-LICENSE.txt"
cp ../client_jar/res/LICENSE-audio.txt "$W/giay_phep/LICENSE-audio.txt" 2>/dev/null || true
cat > "$W/DOC_TRUOC.txt" <<EOF
Huyền Thoại Bảo Bối - bản PC $PHIEN_BAN

CHƠI: bấm đúp HTBB.exe (không cần cài Java - Java tối giản đi kèm trong thư mục runtime\\).
Giải nén cả thư mục HTBB ra chỗ bất kỳ (vd C:\\Games\\HTBB), không chạy thẳng trong tệp zip.

Phím: WASD / mũi tên đi · Space đánh · 1-5 chiêu · Q/E bình HP/MP · Tab mục tiêu · Enter chat
      Esc menu / quay lại · M menu nhanh · F1 gợi ý phím · F10 cài đặt · F11 toàn màn · F12 chụp màn
      Con lăn / + - : thu phóng bản đồ · Chuột trái chạm/chọn · Chuột phải quay lại
Cài đặt, ảnh chụp, nhật ký: %APPDATA%\\HTBB   (F10 -> Giới thiệu -> Mở thư mục dữ liệu)
Máy chủ khác: HTBB.exe --may-chu host:cong   (hoặc may_chu=host:cong trong %APPDATA%\\HTBB\\cai_dat.properties)

Giấy phép: GHI_CONG.txt (hình Pokémon PMDCollab, CC BY-NC 4.0) · giay_phep\\ · trong game: F10 -> Giấy phép
Java: Eclipse Temurin (runtime\\legal, GPLv2 + Classpath Exception) · Nhạc MIDI soundbank: am\\GIAY_PHEP.txt
EOF
sed -i 's/$/\r/' "$W/DOC_TRUOC.txt"
ZIP=$GOI/HTBB-PC-Windows-$TEN_PB.zip
rm -f "$ZIP"
(cd "$GOI/windows" && zip -qr -9 "$GOC/$ZIP" HTBB)
echo "OK $ZIP ($(wc -c < "$ZIP") byte)"

# ---- 6. bộ cài Inno Setup (wine) ----
if [ "${CAI_DAT:-1}" = 1 ] && command -v wine >/dev/null 2>&1; then
    export WINEPREFIX="${WINEPREFIX:-$GOC/$CC/wine}"   # WINEPREFIX có sẵn (đã cài Inno Setup) thì dùng lại
    export WINEDEBUG=-all
    if [ -z "$ISCC" ]; then
        ISCC="$WINEPREFIX/drive_c/InnoSetup6/ISCC.exe"
        if [ ! -f "$ISCC" ]; then
            tai "https://github.com/jrsoftware/issrc/releases/download/is-6_7_3/innosetup-6.7.3.exe" "$CC/innosetup-6.7.3.exe"
            # SHA-256 = "digest" của tệp trên GitHub release is-6_7_3 (api.github.com, kiểm 2026-10-18)
            kiem_sha "$CC/innosetup-6.7.3.exe" 9c73c3bae7ed48d44112a0f48e66742c00090bdb5bef71d9d3c056c66e97b732
            wine "$CC/innosetup-6.7.3.exe" /VERYSILENT /SUPPRESSMSGBOXES /NORESTART /CURRENTUSER '/DIR=C:\InnoSetup6' >/dev/null 2>&1 || true
        fi
    fi
    if [ -f "$ISCC" ]; then
        cat > "$GOI/htbb.iss" <<EOF
#define AppVer "$PHIEN_BAN"
[Setup]
AppId={{6B1F8C2E-4A57-4E3B-9C0D-48544242504B}
AppName=Huyền Thoại Bảo Bối
AppVersion={#AppVer}
AppPublisher=HTBB
DefaultDirName={autopf}\\HTBB
DefaultGroupName=Huyền Thoại Bảo Bối
UninstallDisplayIcon={app}\\HTBB.exe
OutputDir=.
OutputBaseFilename=HTBB-PC-Setup-$TEN_PB
SetupIconFile=$(winepath -w "$GOC/../client_jar/shared/brand/htbb.ico" 2>/dev/null)
Compression=lzma2/ultra
SolidCompression=yes
PrivilegesRequiredOverridesAllowed=dialog
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
WizardStyle=modern

[Languages]
Name: "en"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "windows\\HTBB\\*"; DestDir: "{app}"; Flags: recursesubdirs ignoreversion

[Icons]
Name: "{group}\\Huyền Thoại Bảo Bối"; Filename: "{app}\\HTBB.exe"
Name: "{autodesktop}\\Huyền Thoại Bảo Bối"; Filename: "{app}\\HTBB.exe"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Tạo biểu tượng trên màn hình nền"

[Run]
Filename: "{app}\\HTBB.exe"; Description: "Chơi Huyền Thoại Bảo Bối"; Flags: nowait postinstall skipifsilent
EOF
        # Inno Setup đọc .iss UTF-8 khi có BOM
        printf '\357\273\277' | cat - "$GOI/htbb.iss" > "$GOI/htbb-bom.iss" && mv "$GOI/htbb-bom.iss" "$GOI/htbb.iss"
        (cd "$GOI" && wine "$ISCC" /Q htbb.iss > iscc.log 2>&1) && echo "OK $GOI/HTBB-PC-Setup-$TEN_PB.exe" \
            || { echo "CANH BAO: Inno Setup loi - xem $GOI/iscc.log (zip portable van dung)"; }
    else
        echo "CANH BAO: khong cai duoc Inno Setup trong wine - bo qua bo cai (zip portable van dung)"
    fi
else
    echo "Bo qua bo cai: can wine (hoac chay ISCC tren Windows voi $GOI/htbb.iss)"
fi

# ---- 7. bản Linux x64: AppImage (MỘT tệp) + tar.gz - runtime jlink, không có jar game nằm rời ----
DOC_LINUX=$(sed 's/\r$//' "$W/DOC_TRUOC.txt" \
    | sed "s|bấm đúp HTBB.exe|chạy HTBB-PC-Linux-$TEN_PB.AppImage (chmod +x trước) hoặc HTBB/HTBB của bản tar.gz|; s|HTBB.exe|HTBB|g; s|%APPDATA%\\\\HTBB|~/.local/share/htbb|g; s|runtime\\\\|runtime/|g")
JOPT='-Xmx512m -Dfile.encoding=UTF-8 -Dsun.java2d.uiScale=1'
# 7a. AppImage: AppRun + usr/lib/htbb/{runtime, htbb.jar, am} + desktop + biểu tượng
AD=$GOI/AppDir
rm -rf "$AD" && mkdir -p "$AD/usr/lib/htbb" "$AD/usr/share/htbb"
cp -r "$GOI/runtime_linux" "$AD/usr/lib/htbb/runtime"
cp "$GOI/HTBB-PC-roi.jar" "$AD/usr/lib/htbb/htbb.jar"
cp -r out/am "$AD/usr/lib/htbb/am"
cp out/GHI_CONG.txt "$AD/usr/share/htbb/GHI_CONG.txt"
mkdir -p "$AD/usr/share/htbb/giay_phep" && cp "$W"/giay_phep/* "$AD/usr/share/htbb/giay_phep/" && rm -f "$AD/usr/share/htbb/giay_phep/Launch4j-LICENSE.txt"
printf '%s\n' "$DOC_LINUX" > "$AD/usr/share/htbb/DOC_TRUOC.txt"
cp ../client_jar/shared/brand/icon_256.png "$AD/htbb.png"
cp ../client_jar/shared/brand/icon_256.png "$AD/.DirIcon"
cat > "$AD/htbb.desktop" <<'DESK'
[Desktop Entry]
Type=Application
Name=Huyền Thoại Bảo Bối
Comment=Game nhập vai Pokémon trực tuyến
Exec=HTBB
Icon=htbb
Terminal=false
Categories=Game;RolePlaying;
DESK
cat > "$AD/AppRun" <<APPRUN
#!/bin/sh
# Huyền Thoại Bảo Bối (Linux AppImage): Java tối giản + game trong ảnh squashfs. Dữ liệu người chơi: ~/.local/share/htbb
HERE=\$(dirname "\$(readlink -f "\$0")")
cd "\$HERE/usr/lib/htbb"
exec "\$HERE/usr/lib/htbb/runtime/bin/java" $JOPT -jar "\$HERE/usr/lib/htbb/htbb.jar" "\$@"
APPRUN
chmod +x "$AD/AppRun"
AIT=$CC/appimagetool-1.9.1-x86_64.AppImage
RT=$CC/type2-runtime-20251108-x86_64
tai "https://github.com/AppImage/appimagetool/releases/download/1.9.1/appimagetool-x86_64.AppImage" "$AIT"
tai "https://github.com/AppImage/type2-runtime/releases/download/20251108/runtime-x86_64" "$RT"
# SHA-256 = "digest" của tệp trên GitHub release (api.github.com, kiểm 2026-10-19)
kiem_sha "$AIT" ed4ce84f0d9caff66f50bcca6ff6f35aae54ce8135408b3fa33abfc3cb384eb0
kiem_sha "$RT" 2fca8b443c92510f1483a883f60061ad09b46b978b2631c807cd873a47ec260d
chmod +x "$AIT"
APPIMG=$GOI/HTBB-PC-Linux-$TEN_PB.AppImage
rm -f "$APPIMG"
ARCH=x86_64 APPIMAGE_EXTRACT_AND_RUN=1 "$AIT" --runtime-file "$RT" --no-appstream "$AD" "$APPIMG" > "$GOI/appimagetool.log" 2>&1 \
    || { tail -20 "$GOI/appimagetool.log"; exit 1; }
chmod +x "$APPIMG"
echo "OK $APPIMG ($(wc -c < "$APPIMG") byte)"
# 7b. tar.gz: HTBB/HTBB (trình chạy) + runtime/ (jar game giấu trong runtime/lib/htbb/) + am/ + giấy phép
L=$GOI/linux/HTBB
rm -rf "$GOI/linux" && mkdir -p "$L"
cp -r "$GOI/runtime_linux" "$L/runtime"
mkdir -p "$L/runtime/lib/htbb" && cp "$GOI/HTBB-PC-roi.jar" "$L/runtime/lib/htbb/htbb.jar"
cp -r out/am "$L/am"
cp out/GHI_CONG.txt "$L/"
cp ../client_jar/shared/brand/icon_256.png "$L/htbb.png"
mkdir -p "$L/giay_phep" && cp "$W"/giay_phep/* "$L/giay_phep/" && rm -f "$L/giay_phep/Launch4j-LICENSE.txt"
cat > "$L/HTBB" <<CHAY
#!/bin/sh
# Huyền Thoại Bảo Bối (Linux): chạy game bằng Java tối giản đi kèm (runtime/). Dữ liệu người chơi: ~/.local/share/htbb
D=\$(cd "\$(dirname "\$0")" && pwd)
cd "\$D"
exec "\$D/runtime/bin/java" $JOPT -jar "\$D/runtime/lib/htbb/htbb.jar" "\$@"
CHAY
chmod +x "$L/HTBB"
cat > "$L/HTBB.desktop" <<'DESK'
[Desktop Entry]
Type=Application
Name=Huyền Thoại Bảo Bối
Comment=Game nhập vai Pokémon trực tuyến
Exec=sh -c '"$(dirname "%k")/HTBB"'
Icon=htbb
Terminal=false
Categories=Game;RolePlaying;
DESK
printf '%s\n' "$DOC_LINUX" > "$L/DOC_TRUOC.txt"
TGZ=$GOI/HTBB-PC-Linux-$TEN_PB.tar.gz
rm -f "$TGZ"
tar czf "$TGZ" -C "$GOI/linux" HTBB
echo "OK $TGZ ($(wc -c < "$TGZ") byte)"

# ---- 8. chép cho người chơi thử ----
if [ -n "$CHEP_VAO" ]; then
    mkdir -p "$CHEP_VAO"
    cp "$ZIP" "$TGZ" "$APPIMG" "$CHEP_VAO/"
    [ -f "$GOI/HTBB-PC-Setup-$TEN_PB.exe" ] && cp "$GOI/HTBB-PC-Setup-$TEN_PB.exe" "$CHEP_VAO/"
    echo "Da chep vao $CHEP_VAO"
fi
