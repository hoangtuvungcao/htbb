#!/usr/bin/env python3
"""Build customer download packages into web/public/downloads/ (+ manifest.json, SHA256SUMS.txt).

Prerequisite (production build, from repo root):
  export PEEROVA_LICENSE_SERVER=https://peerova.pages.dev PEEROVA_AUTHORITY_PUBKEY=$(cat ~/.config/peerova/authority.pub)
  cargo build --release -p peerova-ffi -p peerova-cli
  cargo build --release --target x86_64-pc-windows-gnu -p peerova-cli -p peerova-ffi
  cargo build --release --target x86_64-unknown-linux-musl -p peerova-cli
  CARGO_TARGET_X86_64_WIN7_WINDOWS_GNU_LINKER=x86_64-w64-mingw32-gcc \\
    cargo +nightly build --release -Zbuild-std=std,panic_abort --target x86_64-win7-windows-gnu -p peerova-cli -p peerova-ffi

The single-file Java SDK (java/peerova.jar: Java API + Windows / Windows legacy / Linux natives) is built here
from those static libraries with `bindings/java/build.sh dist` (needs a JDK and x86_64-w64-mingw32-gcc).

Customer packages must not contain internal details: source-tree paths, build-from-source
instructions, developer scripts or the Rust example. A final scan fails the build if any slip in.
"""
import glob, hashlib, io, json, os, re, subprocess, sys, tarfile, zipfile

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


def build_java_jar():
    """bindings/java/build.sh dist → peerova.jar bytes; fails unless every desktop native is inside."""
    ver = re.search(r'(?m)^version\s*=\s*"([^"]+)"', open(f"{R}/Cargo.toml").read()).group(1)
    env = dict(os.environ, PEEROVA_LIB_DIR=REL, PEEROVA_WIN_LIB_DIR=W, PEEROVA_WIN7_LIB_DIR=W7, PEEROVA_VERSION=ver)
    subprocess.run(["sh", f"{R}/bindings/java/build.sh", "dist"], check=True, stdout=subprocess.DEVNULL, env=env)
    data = rd(f"{R}/bindings/java/build/peerova.jar")
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        names = set(z.namelist())
        need = {f"dev/peerova/native/{p}" for p in ("linux-x86_64/libpeerova_java.so", "windows-x86_64/peerova_java.dll",
                                                     "windows-x86_64-legacy/peerova_java.dll", "natives.properties")}
        if need - names:
            sys.exit("peerova.jar is missing: " + ", ".join(sorted(need - names)))
        # Same rule as the release binaries: no build-machine paths inside the natives.
        home, user = os.path.expanduser("~"), os.environ.get("USER", "")
        for n in sorted(names):
            if n.startswith("dev/peerova/native/") and not n.endswith(".properties"):
                blob = z.read(n)
                bad = [w for w in (home, "tunnel_p2p", user) if w and w.encode() in blob]
                if bad:
                    sys.exit(f"build-machine paths in peerova.jar:{n}: {bad} (build with the release RUSTFLAGS)")
    return data


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
           ("lib/windows-legacy-x86_64/wintun.dll", rd(WINTUN), 0o644),
           ("java/peerova.jar", build_java_jar(), 0o644)] + base
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
        if not arc.startswith("lib/") and not arc.endswith(BIN_EXT) and DENY.search(data.decode("utf-8", "ignore")):
            leaks.append(arc)
    if leaks:
        sys.exit("internal details found in: " + ", ".join(sorted(set(leaks))))
    # Per-platform SDK packages: only that binding + the native files it needs.
    by_path = {arc: (arc, d, m) for arc, d, m in sdk}
    # Shared libraries only; the large static lib (libpeerova.a) ships in the C/C++ package.
    libs = [by_path[k] for k in ("include/peerova.h", "lib/linux-x86_64/libpeerova.so",
                                  "lib/windows-x86_64/peerova.dll", "lib/windows-legacy-x86_64/peerova.dll")]
    static = [by_path["lib/linux-x86_64/libpeerova.a"]]
    cli = [by_path[k] for k in ("lib/linux-x86_64/peerova", "lib/windows-x86_64/peerova.exe")]
    guides = [e for e in sdk if e[0] in ("bindings/README.md", "bindings/DEVELOPER-GUIDE.md")]
    PLATFORMS = [
        ("unity", "Unity (C#)", ["unity"], libs),
        ("godot", "Godot 4 (.NET / GDScript)", ["godot"], libs),
        ("unreal", "Unreal Engine 5", ["unreal"], libs),
        ("java", "Java · Kotlin · Android (peerova.jar)", ["java", "kotlin-android"], [by_path["java/peerova.jar"]]),
        ("dotnet", "C# / .NET", ["csharp"], libs),
        ("c", "C / C++", ["c"], libs + static),
        ("python", "Python", ["python"], libs),
        ("nodejs", "Node.js · Electron", ["nodejs", "stdio"], cli),
        ("deno-bun", "Deno · Bun", ["deno", "bun"], libs),
        ("go", "Go", ["go"], libs),
        ("dart", "Dart · Flutter", ["dart"], libs),
        ("php", "PHP", ["php", "stdio"], libs + cli),
        ("lua", "Lua · LuaJIT", ["lua", "stdio"], libs + cli),
        ("ruby", "Ruby", ["ruby"], libs),
        ("swift", "Swift", ["swift"], libs),
    ]
    for key, label, folders, natives in PLATFORMS:
        files = [e for e in sdk if any(e[0].startswith(f"bindings/{f}/") for f in folders)]
        if not files:
            sys.exit(f"SDK package {key}: no files for {folders}")
        arts.append(("SDK", key, label, zip_pkg(f"peerova-sdk-{key}.zip", natives + guides + files + base)))
    # Full bundle without the static lib (it's in the C/C++ package): keeps it under
    # the 25 MiB per-file limit of static hosting.
    arts.append(("SDK", "all", "SDK đầy đủ: mọi ngôn ngữ và engine",
                 zip_pkg("peerova-sdk.zip", [e for e in sdk if e[0] != "lib/linux-x86_64/libpeerova.a"])))
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
