#!/bin/sh
# Build Peerova (p2p/peerova) cho HTBB: thư viện C ABI + JNI tự chứa (đã link tĩnh libpeerova.a) + CLI chẩn đoán.
#
#   sh p2p/build/build-peerova.sh [linux] [win7] [win10] [android] [all]   (mặc định: all)
#
# Ra p2p/out/dist/ (trong .gitignore):
#   linux-x86_64/            libpeerova.so  libpeerova_jni.so  peerova
#   windows-x86_64-win7/     peerova.dll    peerova_jni.dll    peerova.exe   <- Windows Server 2012 / 8 / 7 (+ chạy được trên 10/11)
#   windows-x86_64/          peerova.dll    peerova_jni.dll    peerova.exe   <- target stable, CẦN Windows 10+
#   android/<abi>/           libpeerova_jni.so (arm64-v8a, armeabi-v7a, x86_64)
#
# Server 2012: rustc stable 1.78+ bỏ Windows < 10 cho *-pc-windows-*, nên dùng target tier-3 x86_64-win7-windows-gnu
# (nightly + -Zbuild-std; cần: rustup toolchain install nightly; rustup component add rust-src --toolchain nightly;
# apt install gcc-mingw-w64-x86-64). Script kiểm import của DLL/EXE: không được có API chỉ Win10 (ProcessPrng,
# WaitOnAddress, api-ms-win-core-synch-l1-2-0 ...).
# Android: NDK (ANDROID_NDK_HOME, mặc định ~/Android/Sdk/ndk/<bản mới nhất>) + rustup target add aarch64-linux-android
# armv7-linux-androideabi x86_64-linux-android. Thiếu NDK -> bỏ qua Android, in hướng dẫn.
#
# License: mọi bản build nhúng authority pubkey + license server của website Peerova (giá trị CÔNG KHAI, không phải bí mật).
# KHÔNG BAO GIỜ đưa authority.seed hay license key (PVA-...) vào đây.
set -e
cd "$(dirname "$0")/.."
P2P=$(pwd)
SRC=$P2P/peerova
DIST=$P2P/out/dist
TGT=$P2P/out/target
# = p2p/peerova/scripts/release.env (cấu hình build phát hành của chủ dự án); biến môi trường đã đặt thì thắng.
export PEEROVA_AUTHORITY_PUBKEY=${PEEROVA_AUTHORITY_PUBKEY:-IIS5xs9LJBdu7yLGh4qS0qVPygEOJfx7BCE7oydEm3A}
export PEEROVA_LICENSE_SERVER=${PEEROVA_LICENSE_SERVER:-https://peerova.pages.dev}
export PEEROVA_RELAY_URL=${PEEROVA_RELAY_URL:-https://peerova.pages.dev}
# Khoá CÔNG KHAI ký bản phát hành (release.env: PEEROVA_RELEASE_PUBKEY_FILE) - chỉ đọc .pub, KHÔNG BAO GIỜ *.seed.
if [ -z "$PEEROVA_RELEASE_PUBKEY" ] && [ -f "$HOME/.config/peerova/release.pub" ]; then
    export PEEROVA_RELEASE_PUBKEY=$(cat "$HOME/.config/peerova/release.pub")
fi
# Như build-release.sh: bỏ đường dẫn máy build khỏi chuỗi panic / location trong binary.
export RUSTFLAGS="--remap-path-prefix=$SRC/crates=src --remap-path-prefix=$SRC=. --remap-path-prefix=$HOME/.cargo=cargo --remap-path-prefix=$HOME/.rustup=rust"
# ring (TLS cho đường dự phòng) có ít C: trình biên dịch C theo target.
export CC_x86_64_win7_windows_gnu=${CC_x86_64_win7_windows_gnu:-x86_64-w64-mingw32-gcc}
export CARGO_TARGET_X86_64_WIN7_WINDOWS_GNU_LINKER=${CARGO_TARGET_X86_64_WIN7_WINDOWS_GNU_LINKER:-x86_64-w64-mingw32-gcc}
JNI_C=$SRC/bindings/java/peerova_jni.c
INC=$SRC/crates/peerova-ffi/include
if [ -z "$JAVA_HOME" ]; then JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")"); fi
JNI_INC=$JAVA_HOME/include
[ -f "$JNI_INC/jni.h" ] || { echo "LOI: khong thay $JNI_INC/jni.h (dat JAVA_HOME toi JDK)"; exit 1; }

WHAT=${*:-all}
has() { case " $WHAT " in *" $1 "*|*" all "*) return 0 ;; esac; return 1; }

# Kiểm chuỗi license nhúng trong binary.
kiem_license() {
    grep -aq "$PEEROVA_AUTHORITY_PUBKEY" "$1" || { echo "LOI: $1 khong nhung PEEROVA_AUTHORITY_PUBKEY"; exit 1; }
    grep -aq "$PEEROVA_LICENSE_SERVER" "$1" || { echo "LOI: $1 khong nhung PEEROVA_LICENSE_SERVER"; exit 1; }
    n=$(strings "$1" | grep -cE "$HOME|$(whoami)|tunnel_p2p" || true)
    [ "$n" -eq 0 ] || { echo "LOI: $1 con $n duong dan may build"; exit 1; }
}

# Kiểm DLL/EXE Windows không import API chỉ có từ Windows 10 (Server 2012 = NT 6.2).
kiem_win8() {
    IMP=$(x86_64-w64-mingw32-objdump -p "$1" | sed -n '/The Import Tables/,/The Export Tables/p')
    BAD=$(printf '%s\n' "$IMP" | grep -Ei 'ProcessPrng|bcryptprimitives|WaitOnAddress|WakeByAddress|api-ms-win|GetSystemTimePreciseAsFileTime|SetThreadDescription|GetThreadDescription|CreateFile2|PrefetchVirtualMemory|GetCurrentThreadStackLimits' || true)
    if [ -n "$BAD" ]; then echo "LOI: $1 import API moi hon Windows 8/2012:"; echo "$BAD"; return 1; fi
    echo "  ok imports Win8/2012: $(basename "$1") ($(printf '%s\n' "$IMP" | grep -c 'DLL Name') DLL: $(printf '%s\n' "$IMP" | sed -n 's/.*DLL Name: //p' | tr '\n' ' '))"
}


if has linux; then
    echo "== linux-x86_64"
    (cd "$SRC" && CARGO_TARGET_DIR=$TGT cargo build --release -p peerova-ffi -p peerova-cli)
    O=$DIST/linux-x86_64; mkdir -p "$O"
    R=$TGT/release
    cp "$R/libpeerova.so" "$R/peerova" "$O/"
    gcc -O2 -Wall -shared -fPIC "$JNI_C" -I"$JNI_INC" -I"$JNI_INC/linux" -I"$INC" \
        "$R/libpeerova.a" -lpthread -ldl -lm -Wl,--gc-sections -Wl,--exclude-libs,ALL -s -o "$O/libpeerova_jni.so"
    kiem_license "$O/libpeerova_jni.so"; kiem_license "$O/peerova"
    ls -la "$O"
    # Ban tuong thich glibc 2.17 (CentOS 7 / Ubuntu 18.04+) cho goi may chu: can zig + cargo-zigbuild
    # (zig: python3 -m venv ~/.local/share/zigvenv && ~/.local/share/zigvenv/bin/pip install ziglang)
    if command -v zig >/dev/null 2>&1 && command -v cargo-zigbuild >/dev/null 2>&1; then
        echo "== linux-x86_64-compat (glibc 2.17)"
        LT=x86_64-unknown-linux-gnu
        (cd "$SRC" && CARGO_TARGET_DIR=$TGT-compat cargo zigbuild --release --target $LT.2.17 -p peerova-ffi -p peerova-cli)
        RC=$TGT-compat/$LT/release; OC=$DIST/linux-x86_64-compat; mkdir -p "$OC"; cp "$RC/peerova" "$OC/"
        zig cc -target x86_64-linux-gnu.2.17 -O2 -shared -fPIC "$JNI_C" -I"$JNI_INC" -I"$JNI_INC/linux" -I"$INC" \
            "$RC/libpeerova.a" -lpthread -ldl -lm -lunwind -Wl,--gc-sections -s -o "$OC/libpeerova_jni.so"
        kiem_license "$OC/libpeerova_jni.so"
        echo "  glibc toi da: $(objdump -T "$OC/libpeerova_jni.so" | grep -oE 'GLIBC_[0-9.]+' | sort -V | tail -1)"
    else
        echo "  (bo qua ban glibc 2.17: thieu zig / cargo-zigbuild)"
    fi
fi

build_win() { # $1 = win7|win10
    if [ "$1" = win7 ]; then T=x86_64-win7-windows-gnu; TC=+nightly; BS="-Zbuild-std=std,panic_abort"; O=$DIST/windows-x86_64-win7; TD=$TGT-win7
    else T=x86_64-pc-windows-gnu; TC=""; BS=""; O=$DIST/windows-x86_64; TD=$TGT-win10; fi
    echo "== $O ($T)"
    (cd "$SRC" && CARGO_TARGET_DIR=$TD cargo $TC build --release $BS --target $T -p peerova-ffi -p peerova-cli)
    R=$TD/$T/release
    mkdir -p "$O"
    cp "$R/peerova.dll" "$R/peerova.exe" "$O/"
    # JNI tự chứa: peerova_jni.c + libpeerova.a -> một DLL (không phụ thuộc peerova.dll; chỉ DLL hệ thống).
    x86_64-w64-mingw32-gcc -O2 -Wall -shared "$JNI_C" -I"$P2P/build/jni-win32" -I"$JNI_INC" -I"$INC" \
        "$R/libpeerova.a" -lws2_32 -liphlpapi -lbcrypt -ladvapi32 -luserenv -lntdll -lole32 -loleaut32 -lshell32 -lkernel32 -static-libgcc \
        -Wl,--gc-sections -Wl,--exclude-libs,ALL -Wl,--major-subsystem-version,6 -Wl,--minor-subsystem-version,0 -s \
        -o "$O/peerova_jni.dll"
    x86_64-w64-mingw32-strip "$O/peerova.dll" "$O/peerova.exe" 2>/dev/null || true
    for f in peerova.dll peerova_jni.dll peerova.exe; do kiem_license "$O/$f"; done
    if [ "$1" = win7 ]; then for f in peerova.dll peerova_jni.dll peerova.exe; do kiem_win8 "$O/$f"; done; fi
    # JNI export phải đúng tên (không có hậu tố @N của stdcall 32-bit)
    x86_64-w64-mingw32-objdump -p "$O/peerova_jni.dll" | grep -q 'Java_dev_peerova_PeerovaTunnel_nClient$' || { echo "LOI: peerova_jni.dll thieu export JNI"; exit 1; }
    ls -la "$O"
}
has win7 && build_win win7
has win10 && build_win win10

if has android; then
    NDK=${ANDROID_NDK_HOME:-$(ls -d "$HOME"/Android/Sdk/ndk/* 2>/dev/null | sort -V | tail -1)}
    if [ -z "$NDK" ] || [ ! -d "$NDK/toolchains/llvm/prebuilt" ]; then
        echo "== android: BO QUA - khong thay Android NDK. Cai qua sdkmanager \"ndk;28.2.13676358\" hoac dat ANDROID_NDK_HOME,"
        echo "   rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android, roi chay lai: $0 android"
    else
        BIN=$(ls -d "$NDK"/toolchains/llvm/prebuilt/*/bin | head -1)
        API=26   # = minSdk của client_apk/pokemon
        for abi in arm64-v8a armeabi-v7a x86_64; do
            case $abi in
                arm64-v8a) T=aarch64-linux-android; CC=$BIN/aarch64-linux-android$API-clang ;;
                armeabi-v7a) T=armv7-linux-androideabi; CC=$BIN/armv7a-linux-androideabi$API-clang ;;
                x86_64) T=x86_64-linux-android; CC=$BIN/x86_64-linux-android$API-clang ;;
            esac
            echo "== android $abi ($T)"
            V=$(echo "$T" | tr 'a-z-' 'A-Z_')
            (cd "$SRC" && env "CARGO_TARGET_${V}_LINKER=$CC" "CC_${T}=$CC" "AR_${T}=$BIN/llvm-ar" \
                CARGO_TARGET_DIR=$TGT-android cargo build --release --target $T -p peerova-ffi)
            O=$DIST/android/$abi; mkdir -p "$O"
            # 16 KB page size (Android 15+ yêu cầu với targetSdk 35)
            "$CC" -O2 -Wall -shared -fPIC "$JNI_C" -I"$INC" "$TGT-android/$T/release/libpeerova.a" -llog -ldl -lm \
                -Wl,--gc-sections -Wl,--exclude-libs,ALL -Wl,-z,max-page-size=16384 -s -o "$O/libpeerova_jni.so"
            kiem_license "$O/libpeerova_jni.so"
            ls -la "$O"
        done
    fi
fi
echo "XONG: $DIST (authority $PEEROVA_AUTHORITY_PUBKEY, license $PEEROVA_LICENSE_SERVER, relay $PEEROVA_RELAY_URL, release pubkey ${PEEROVA_RELEASE_PUBKEY:+co})"
