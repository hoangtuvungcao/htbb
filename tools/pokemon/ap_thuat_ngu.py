#!/usr/bin/env python3
"""
Ap thuat ngu chuan (docs/thiet-ke-game.md, muc Thuat ngu) vao CHUOI nguoi choi/admin thay trong ma nguon.

Truoc day chu kiem hiep chi duoc doi LUC CHAY (server ChuPokemon.doi o cua gui chu, client ve chu qua
ChuPokemon.D / chu.bin). Tool nay "nuong" ket qua do vao thang chuoi "..." trong ma nguon de khong con
thuat ngu cu nao trong nguon (tu dien luc chay van giu, lo phan dong: ten tu CSDL, chu cu con sot).

Moi chuoi "giong chu" (co dau hoac bat dau bang chu hoa; bo chu thich va dong log) co tu cam
(ChuPokemon.TU_CAM) duoc thay bang ChuPokemon.doi(chuoi) - y het chu nguoi choi thay khi bat che do.
Chuoi dung de SO SANH (equals/startsWith/indexOf/case/==...), "bang" trong chuoi KHONG DAU, co \\u, chuoi @"..." C# KHONG tu
doi: in ra muc "XEM TAY" de nguoi sua quyet (hai dau server/client phai doi cung nhau).

Chay (o thu muc HTBB/):
  python3 -I tools/pokemon/ap_thuat_ngu.py            # xem truoc (khong ghi)
  python3 -I tools/pokemon/ap_thuat_ngu.py --ghi      # ghi
  python3 -I tools/pokemon/ap_thuat_ngu.py --khu sv,j2me,pc,android,cu
Chay lai bao nhieu lan cung duoc (lan 2 khong doi gi them).
"""
import importlib.util
import os
import re
import sys
import unicodedata

_D = os.path.dirname(os.path.abspath(__file__))


def _nap(ten):
    sp = importlib.util.spec_from_file_location(ten, os.path.join(_D, ten + ".py"))
    m = importlib.util.module_from_spec(sp)
    sp.loader.exec_module(m)
    return m


cp = _nap("chu_pokemon")
dc = _nap("duong_client")
GOC = cp.GOC

KHU = {
    "sv": [(os.path.join(GOC, "server", "src"), ".java")],
    "j2me": [(os.path.join(GOC, "client_jar", "src"), ".java"), (os.path.join(GOC, "client_jar", "shared"), ".java")],
    "pc": [(dc.scripts(dc.assets()), ".cs"), (os.path.join(GOC, "client_pc", "src"), ".java")],
    "android": [(os.path.join(GOC, "client_apk", "pokemon", "src"), ".java")],
}

# Bang tu dien / bang dia danh (chua tu cu CO CHU DICH) - khong dung vao.
BO_QUA_TEN = {"ChuPokemon.java", "ChuPokemon.cs", "ChuPokemonDuLieu.cs", "TenMapPokemon.java", "TenMapPokemon.cs"}

DONG_LOG = re.compile(r"System\.(out|err)|printStackTrace|saveOrtherLog|saveLogThongKe|Debug\.Log|Logdata|LOGGER|logger\.|Log\.[dewiv]\(")
SO_SANH_TRUOC = re.compile(
    r"(?:\b(?:equals|equalsIgnoreCase|startsWith|endsWith|indexOf|lastIndexOf|contains|matches|split|replace|"
    r"replaceAll|replaceFirst|Equals|StartsWith|EndsWith|IndexOf|LastIndexOf|Contains|Replace|Split|Compare)\s*\(\s*"
    r"|\bcase\s+|[!=]=\s*)$")
SO_SANH_SAU = re.compile(r"^\s*(?:\.\s*(?:equals|equalsIgnoreCase|Equals|contentEquals)\s*\(|[!=]=)")


def giong_chu(t):
    return bool(t) and (re.search(r"[^\x00-\x7f]", t) is not None or t[0].isupper())


def thoat(s):
    return (s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("\r", "\\r")
            .replace("\t", "\\t"))


def cac_chuoi(s):
    """(dau, cuoi, noi_dung_tho) cua moi chuoi "..." KHONG nam trong chu thich. Ky tu '.' bo qua."""
    i, n = 0, len(s)
    while i < n:
        c = s[i]
        if c == '"':
            j = i + 1
            while j < n and s[j] != '"' and s[j] != "\n":
                j += 2 if s[j] == "\\" else 1
            if j < n and s[j] == '"':
                yield i, j + 1, s[i + 1:j]
            i = j + 1
            continue
        if c == "'":
            j = i + 1
            while j < n and s[j] != "'" and s[j] != "\n":
                j += 2 if s[j] == "\\" else 1
            i = j + 1
            continue
        if s.startswith("//", i):
            j = s.find("\n", i)
            i = n if j < 0 else j
            continue
        if s.startswith("/*", i):
            j = s.find("*/", i + 2)
            i = n if j < 0 else j + 2
            continue
        i += 1


def xu_ly_tep(p, td, cam, ghi, bao):
    s = open(p, encoding="utf8").read()
    rel = os.path.relpath(p, os.path.dirname(GOC))
    sua = []
    for a, b, tho in cac_chuoi(s):
        dong_dau = s.rfind("\n", 0, a) + 1
        dong_cuoi = s.find("\n", b)
        dong = s[dong_dau:dong_cuoi if dong_cuoi >= 0 else len(s)]
        if DONG_LOG.search(dong):
            continue
        t = cp.bo_thoat_java(tho)
        if not giong_chu(t):
            continue
        tn = unicodedata.normalize("NFC", t)
        tim = cam.tim(tn)
        if not tim:
            continue
        so = s.count("\n", 0, a) + 1
        truoc = s[dong_dau:a]
        sau = s[b:b + 40]
        # "bang" trong chu KHONG DAU (bảng/băng/bang?: "MAC DINH (chua doc duoc bang ...)") - khong doan.
        khong_dau = (re.search(r"[^\x00-\x7f]", tho) is None and all(g.lower() == "bang" for g in tim)
                     and t.strip() not in ("Bang", "BANG"))
        if khong_dau or "\\u" in tho or truoc.endswith("@") or SO_SANH_TRUOC.search(truoc) or SO_SANH_SAU.search(sau):
            bao.append("XEM TAY %s:%d %s  [%s]" % (rel, so, t[:120].replace("\n", "|"), "|".join(tim)))
            continue
        moi = td.doi(tn)
        if moi == tn:
            bao.append("CHUA DOI %s:%d %s  [%s]" % (rel, so, t[:120].replace("\n", "|"), "|".join(tim)))
            continue
        sua.append((a, b, '"' + thoat(moi) + '"'))
        bao.append("DOI %s:%d\n    - %s\n    + %s" % (rel, so, t[:160].replace("\n", "|"), moi[:160].replace("\n", "|")))
    if sua and ghi:
        # doc lai ngay truoc khi ghi: tep doi giua chung (viec khac dang sua) thi bo qua, bao lai
        if open(p, encoding="utf8").read() != s:
            bao.append("BO QUA (tep vua bi sua): " + rel)
            return 0
        for a, b, x in reversed(sua):
            s = s[:a] + x + s[b:]
        open(p, "w", encoding="utf8").write(s)
    return len(sua)


def main():
    ghi = "--ghi" in sys.argv
    khu = list(KHU)
    if "--khu" in sys.argv:
        khu = sys.argv[sys.argv.index("--khu") + 1].split(",")
    b = cp.doc_bang()
    td = cp.tu_dien_doi(b)
    cam = cp.tu_dien_cam(b)
    bao = []
    tong = {}
    for k in khu:
        n = 0
        for goc, duoi in KHU[k]:
            if not os.path.isdir(goc):
                continue
            for d, ds, fs in os.walk(goc):
                ds[:] = [x for x in ds if x not in ("build", "out", "bin", "obj")]
                for f in sorted(fs):
                    if f.endswith(duoi) and f not in BO_QUA_TEN:
                        n += xu_ly_tep(os.path.join(d, f), td, cam, ghi, bao)
        tong[k] = n
    for x in bao:
        print(x)
    print("Chuoi %s: %s" % ("da doi" if ghi else "se doi", ", ".join("%s %d" % kv for kv in tong.items())))
    print("Xem tay: %d, chua doi duoc: %d" % (sum(x.startswith("XEM TAY") for x in bao),
                                              sum(x.startswith("CHUA DOI") for x in bao)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
