#!/usr/bin/env python3
"""Build customer download packages into web/public/downloads/ (+ manifest.json, SHA256SUMS.txt).

Prerequisite (production build, from repo root):
  export PEEROVA_LICENSE_SERVER=https://peerova.pages.dev PEEROVA_AUTHORITY_PUBKEY=$(cat ~/.config/peerova/authority.pub)
  cargo build --release -p peerova-ffi -p peerova-cli
  cargo build --release --target x86_64-pc-windows-gnu -p peerova-cli -p peerova-ffi
  cargo build --release --target x86_64-unknown-linux-musl -p peerova-cli
  CARGO_TARGET_X86_64_WIN7_WINDOWS_GNU_LINKER=x86_64-w64-mingw32-gcc \\
    cargo +nightly build --release -Zbuild-std=std,panic_abort --target x86_64-win7-windows-gnu -p peerova-cli -p peerova-ffi

Customer packages must not contain internal details: source-tree paths, build-from-source
instructions, developer scripts or the Rust example. A final scan fails the build if any slip in.
"""
import glob, hashlib, io, json, os, re, sys, tarfile, zipfile

R = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = f"{R}/web/public/downloads"
W = f"{R}/target/x86_64-pc-windows-gnu/release"
W7 = f"{R}/target/x86_64-win7-windows-gnu/release"
MUSL = f"{R}/target/x86_64-unknown-linux-musl/release"
REL = f"{R}/target/release"
H = f"{R}/crates/peerova-ffi/include/peerova.h"
# Wintun 0.14.1 (amd64), unmodified from wintun.net/builds; needed by `peerova --vpn` on Windows.
WINTUN = f"{R}/third_party/wintun/wintun.dll"

README = """Peerova — kết nối trực tiếp mọi dịch vụ sau NAT, mã hoá đầu-cuối
==================================================================
Bắt đầu:
  peerova doctor                                  kiểm tra mạng
  peerova server --expose https:443               máy chủ (không cần mở port) → in TICKET
  peerova client <TICKET> --forward tcp:8443=443  máy người dùng
  peerova --help                                  toàn bộ lệnh

Mã kết nối 6 ký tự, license, tài liệu: https://peerova.pages.dev
Kiểm tra file tải về: so sánh SHA256 với trang Tải về.

Peerova — direct, end-to-end encrypted connectivity for any service behind NAT.
Docs & license: https://peerova.pages.dev
"""
ANDROID = "liên hệ Peerova để nhận bản Android"
SUBS = [
    (r"(?ms)^#+ [^\n]*\bRust\b[^\n]*\n.*?(?=^#+ |\Z)", ""),           # Rust section in READMEs
    (r"(?m)^\|\s*\*\*Rust\*\*.*\n", ""),                                # Rust row in tables
    (r"(?m)^.*(chưa được build hay test trong repo|Rust/cargo).*\n", "Thư viện cho Android / iOS / macOS: liên hệ Peerova.\n"),
    (r"`RUSTFLAGS=[^`]*`", ANDROID),
    (r"`[^`]*cargo[ -]ndk[^`]*`", ANDROID),
    (r"(?m)^.*cargo install.*\n", ""),
    (r"(?m)^#\s*cargo cdylibs.*\n", "# libpeerova.so ships with SONAME libpeerova.so\n"),
    (r"(\.\./)*crates/peerova-ffi/include", lambda m: m.group(0).replace("crates/peerova-ffi/include", "include")),
    (r"(\.\./)*target/release", lambda m: m.group(0).replace("target/release", "lib/linux-x86_64")),
    (r"cargo build[^\n`]*", "(thư viện có sẵn trong lib/ của SDK)"),
]
DENY = re.compile(r"crates/|target/release|Cargo\.toml|/home/|tunnel_p2p|\bcargo\b|không relay|no relay", re.I)
SKIP_DIRS = {"bin", "obj", "build", "target", ".dart_tool", "node_modules", "__pycache__", "compile-check", "Tests~", "rust"}
SKIP_FILES = {"sync-headers.sh", "build-android-libs.sh", "compile_flags.txt", "PUT_LIB_HERE.txt"}
BIN_EXT = (".key", ".so", ".dll", ".dylib", ".a", ".jar", ".class", ".pyc")


def clean_text(data: bytes) -> bytes:
    try:
        t = data.decode("utf-8")
    except UnicodeDecodeError:
        return data
    for pat, rep in SUBS:
        t = re.sub(pat, rep, t)
    return t.encode("utf-8")


def common():
    notices = open(f"{R}/THIRD_PARTY_NOTICES.txt", "rb").read()
    example = open(f"{R}/deploy/peerova.example.json", "rb").read()
    return [("README.txt", README.encode()), ("THIRD_PARTY_NOTICES.txt", notices), ("peerova.example.json", example)]


def zip_pkg(name, items):
    p = f"{OUT}/{name}"
    with zipfile.ZipFile(p, "w", zipfile.ZIP_DEFLATED) as z:
        for arc, data, mode in items:
            info = zipfile.ZipInfo(arc)
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = mode << 16
            z.writestr(info, data)
    return p


def tar_pkg(name, items):
    p = f"{OUT}/{name}"
    with tarfile.open(p, "w:gz") as t:
        for arc, data, mode in items:
            ti = tarfile.TarInfo("peerova/" + arc)
            ti.size, ti.mode = len(data), mode
            t.addfile(ti, io.BytesIO(data))
    return p


def rd(path):
    return open(path, "rb").read()


def main():
    os.makedirs(OUT, exist_ok=True)
    for f in glob.glob(f"{OUT}/*"):
        os.remove(f)
    base = [(a, d, 0o644) for a, d in common()]
    hdr = ("peerova.h", rd(H), 0o644)
    ps1 = ("install-windows.ps1", rd(f"{R}/deploy/install-windows.ps1"), 0o644)
    wintun = ("wintun.dll", rd(WINTUN), 0o644)
    arts = [
        ("Windows", "x86_64", "Windows 10 / 11 · Server 2016–2025",
         zip_pkg("peerova-windows-x86_64.zip", [("peerova.exe", rd(f"{W}/peerova.exe"), 0o755), ("peerova.dll", rd(f"{W}/peerova.dll"), 0o644), wintun, hdr, ps1] + base)),
        ("Windows", "x86_64", "Windows Server 2012 / 2012 R2 · 8.1 · 7 (legacy)",
         zip_pkg("peerova-windows-legacy-x86_64.zip", [("peerova.exe", rd(f"{W7}/peerova.exe"), 0o755), ("peerova.dll", rd(f"{W7}/peerova.dll"), 0o644), wintun, hdr, ps1] + base)),
        ("Linux", "x86_64", "Mọi bản Linux 64-bit (static)",
         tar_pkg("peerova-linux-x86_64.tar.gz", [("peerova", rd(f"{MUSL}/peerova"), 0o755), ("libpeerova.so", rd(f"{REL}/libpeerova.so"), 0o755),
                                                  ("libpeerova.a", rd(f"{REL}/libpeerova.a"), 0o644), hdr,
                                                  ("peerova.service", rd(f"{R}/deploy/peerova.service"), 0o644)] + base)),
    ]
    sdk = [("include/peerova.h", rd(H), 0o644),
           ("lib/linux-x86_64/libpeerova.so", rd(f"{REL}/libpeerova.so"), 0o755),
           ("lib/linux-x86_64/libpeerova.a", rd(f"{REL}/libpeerova.a"), 0o644),
           ("lib/linux-x86_64/peerova", rd(f"{MUSL}/peerova"), 0o755),
           ("lib/windows-x86_64/peerova.dll", rd(f"{W}/peerova.dll"), 0o644),
           ("lib/windows-x86_64/peerova.exe", rd(f"{W}/peerova.exe"), 0o755),
           ("lib/windows-x86_64/wintun.dll", rd(WINTUN), 0o644),
           ("lib/windows-legacy-x86_64/peerova.dll", rd(f"{W7}/peerova.dll"), 0o644),
           ("lib/windows-legacy-x86_64/wintun.dll", rd(WINTUN), 0o644)] + base
    for dp, dns, fns in os.walk(f"{R}/bindings"):
        dns[:] = [d for d in dns if d not in SKIP_DIRS]
        for fn in fns:
            if fn in SKIP_FILES or fn.endswith(BIN_EXT):
                continue
            src = os.path.join(dp, fn)
            sdk.append(("bindings/" + os.path.relpath(src, f"{R}/bindings"), clean_text(rd(src)), 0o755 if fn.endswith(".sh") else 0o644))
    # Final guard: no internal details in any text file of any package.
    leaks = []
    for arc, data, _ in sdk + base:
        if not arc.startswith("lib/") and DENY.search(data.decode("utf-8", "ignore")):
            leaks.append(arc)
    if leaks:
        sys.exit("internal details found in: " + ", ".join(sorted(set(leaks))))
    arts.append(("SDK", "all", "SDK: C/C++, C#, Unity, Godot, Unreal, Java/Kotlin, Python, Go, Node.js, Dart, PHP, Lua, Swift…",
                 zip_pkg("peerova-sdk.zip", sdk)))
    man = []
    for os_, arch, label, p in arts:
        man.append({"os": os_, "arch": arch, "label": label, "file": os.path.basename(p),
                    "size": os.path.getsize(p), "sha256": hashlib.sha256(rd(p)).hexdigest()})
    json.dump(man, open(f"{OUT}/manifest.json", "w"), ensure_ascii=False, indent=1)
    open(f"{OUT}/SHA256SUMS.txt", "w").write("".join(f"{m['sha256']}  {m['file']}\n" for m in man))
    for m in man:
        print(f"{m['file']:38} {m['size'] / 1048576:6.2f} MB  {m['sha256'][:16]}…  {m['label']}")


if __name__ == "__main__":
    main()
