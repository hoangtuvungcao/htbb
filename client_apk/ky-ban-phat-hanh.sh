#!/bin/sh
# Ký APK phát hành Huyền Thoại Bảo Bối (Pokémon) bằng zipalign + apksigner, khóa nằm NGOÀI repo.
#
#   KIEU=release BAN=2 MAY_CHU=host:cong sh android/build-pokemon.sh      (không đặt KEY_DIR -> APK chưa ký)
#   KEY_DIR=~/htbb-khoa HTBB_KEYSTORE=htbb.jks HTBB_KEY_ALIAS=htbb \
#   HTBB_KS_PASS=... [HTBB_KEY_PASS=...] sh android/ky-ban-phat-hanh.sh android/out/HTBB-android-2.apk
#       -> android/out/HTBB-android-2-ky.apk (hoặc tham số thứ 2)
#
# Biến môi trường (BẮT BUỘC, thiếu là dừng):
#   KEY_DIR          thư mục chứa keystore, PHẢI nằm ngoài repo git (script từ chối nếu nằm trong repo)
#   HTBB_KEYSTORE    tên file keystore trong KEY_DIR (vd htbb.jks)
#   HTBB_KEY_ALIAS   alias khóa trong keystore
#   HTBB_KS_PASS     mật khẩu keystore (truyền cho apksigner qua env:, không hiện trên dòng lệnh / ps)
#   HTBB_KEY_PASS    mật khẩu khóa (không đặt = dùng HTBB_KS_PASS)
# Tùy chọn: ANDROID_HOME (mặc định ~/Android/Sdk), BUILD_TOOLS (thư mục build-tools cụ thể).
#
# Tạo khóa MỘT LẦN (làm tay, giữ bản sao lưu an toàn; mất khóa = người chơi phải gỡ cài đặt để lên bản mới):
#   keytool -genkeypair -v -keystore "$KEY_DIR/htbb.jks" -alias htbb -keyalg RSA -keysize 4096 \
#           -validity 10000 -dname "CN=HTBB, O=HTBB fan project"
# KHÔNG đưa keystore / mật khẩu vào git (.gitignore đã chặn *.jks, *.keystore, key.properties).
# Gradle (pokemon/build.gradle) cũng ký được nếu KEY_DIR có key.properties (keystore=, alias=, password=);
# script này dành cho khi muốn mật khẩu chỉ ở biến môi trường, không nằm trong file.
set -e
loi() { echo "LOI: $*" >&2; exit 1; }

VAO=$1
[ -n "$VAO" ] || loi "dung: sh android/ky-ban-phat-hanh.sh <apk chua ky> [apk ra]"
[ -f "$VAO" ] || loi "khong thay APK: $VAO"
RA=${2:-"${VAO%.apk}-ky.apk"}

[ -n "$KEY_DIR" ] || loi "chua dat KEY_DIR (thu muc chua keystore, ngoai repo)"
[ -d "$KEY_DIR" ] || loi "KEY_DIR khong ton tai: $KEY_DIR"
[ -n "$HTBB_KEYSTORE" ] || loi "chua dat HTBB_KEYSTORE (ten file keystore trong KEY_DIR)"
[ -n "$HTBB_KEY_ALIAS" ] || loi "chua dat HTBB_KEY_ALIAS"
[ -n "$HTBB_KS_PASS" ] || loi "chua dat HTBB_KS_PASS (mat khau keystore, qua bien moi truong)"
[ -n "$HTBB_KEY_PASS" ] || HTBB_KEY_PASS=$HTBB_KS_PASS
export HTBB_KS_PASS HTBB_KEY_PASS
KS="$KEY_DIR/$HTBB_KEYSTORE"
[ -f "$KS" ] || loi "khong thay keystore: $KS"

# Khóa không được nằm trong repo git (tránh lỡ tay commit)
KD=$(cd "$KEY_DIR" && pwd -P)
GOC=$(cd "$(dirname "$0")/.." && pwd -P)
case "$KD/" in "$GOC"/*) loi "KEY_DIR nam TRONG repo ($GOC) - chuyen keystore ra ngoai repo" ;; esac
if command -v git >/dev/null 2>&1 && (cd "$KD" && git rev-parse --is-inside-work-tree >/dev/null 2>&1); then
    loi "KEY_DIR nam trong mot repo git - chuyen keystore ra ngoai"
fi

[ -n "$ANDROID_HOME" ] || ANDROID_HOME="$HOME/Android/Sdk"
if [ -z "$BUILD_TOOLS" ]; then
    BUILD_TOOLS=$(ls -d "$ANDROID_HOME"/build-tools/* 2>/dev/null | sort -V | tail -1)
fi
[ -x "$BUILD_TOOLS/zipalign" ] && [ -x "$BUILD_TOOLS/apksigner" ] || loi "khong thay zipalign/apksigner trong BUILD_TOOLS=$BUILD_TOOLS"

TAM=$(mktemp -d)
trap 'rm -rf "$TAM"' EXIT
# zipalign TRƯỚC khi ký (ký v2/v3 xong mà align lại là hỏng chữ ký); -P 16: trang 16 KB cho .so (Android 15+)
"$BUILD_TOOLS/zipalign" -f -P 16 4 "$VAO" "$TAM/align.apk"
"$BUILD_TOOLS/apksigner" sign \
    --ks "$KS" --ks-key-alias "$HTBB_KEY_ALIAS" \
    --ks-pass env:HTBB_KS_PASS --key-pass env:HTBB_KEY_PASS \
    --out "$TAM/ky.apk" "$TAM/align.apk"
"$BUILD_TOOLS/apksigner" verify --verbose "$TAM/ky.apk" | grep -E "^Verified|Verifies" || true
mv "$TAM/ky.apk" "$RA"
echo "OK $RA ($(wc -c < "$RA") byte)"
echo "Van tay chung chi (SHA-256) - ghi lai, moi ban phat hanh sau PHAI cung khoa nay:"
"$BUILD_TOOLS/apksigner" verify --print-certs "$RA" | grep -i "SHA-256" || true
