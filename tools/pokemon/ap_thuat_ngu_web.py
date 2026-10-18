#!/usr/bin/env python3
"""
Ap thuat ngu chuan (docs/pokemon/THUAT_NGU.md) vao CHU NGUOI CHOI/ADMIN THAY cua website HTBB-Web
(repo canh HTBB/). Ban web cua tools/pokemon/ap_thuat_ngu.py: web khong co tu dien luc chay nen chu
phai sach ngay trong nguon.

Doi (bang ChuPokemon.doi, y het game) moi doan chu co tu cam (ChuPokemon.TU_CAM):
  .ts/.js/.mjs + <script> cua .vue : chuoi '...', "...", `...` (phan chu ngoai ${...});
  <template> cua .vue              : chu giua the, gia tri thuoc tinh "...", chuoi '...' trong
                                     bieu thuc ({{ }}, :prop, v-if...);
  .sql (server/sql)                : chuoi '...';
  (web PHP cu HTBB/web da bo 19/10/2026 - khong quet .php nua.)
Bo qua chu thich, chu KHONG dau co "bang" (bảng/băng/route bang-...), va chuoi dung de SO SANH /
lam KHOA (=== 'x', includes('x'), case 'x':, { 'x': ... }) - in ra "XEM TAY".
Khong dung vao tep sinh tu dong tu nguon khac (shared/data/*.json: sinh lai bang scripts/*.mjs).

Chay (o thu muc HTBB/):
  python3 -I tools/pokemon/ap_thuat_ngu_web.py            # xem truoc
  python3 -I tools/pokemon/ap_thuat_ngu_web.py --ghi      # ghi
"""
import importlib.util
import os
import re
import sys
import unicodedata

_D = os.path.dirname(os.path.abspath(__file__))
_sp = importlib.util.spec_from_file_location("chu_pokemon", os.path.join(_D, "chu_pokemon.py"))
cp = importlib.util.module_from_spec(_sp)
_sp.loader.exec_module(cp)

WEB = os.path.normpath(os.path.join(cp.GOC, "..", "HTBB-Web"))
THU_MUC = ("app", "server", "shared", "scripts", "tests")
DUOI = (".vue", ".ts", ".js", ".mjs", ".sql")
BO_THU_MUC = {"node_modules", ".nuxt", ".output", "dist", ".data"}

SO_SANH_TRUOC = re.compile(r"(?:[!=]==?\s*|\b(?:includes|startsWith|endsWith|indexOf|lastIndexOf|has|get|set|"
                           r"delete|split|replace|replaceAll|match|test|localeCompare)\s*\(\s*|\bcase\s+)$")
SO_SANH_SAU = re.compile(r"^\s*(?:[!=]==?|\]\s*[!=]==?)")
KHOA_TRUOC = re.compile(r"(?:^|[{,])\s*$")
KHOA_SAU = re.compile(r"^\s*:")


def can_doi(cam, t):
    t = unicodedata.normalize("NFC", t)
    khong_dau = re.search(r"[^\x00-\x7f]", t) is None
    return [g for g in cam.tim(t) if not (khong_dau and g.lower() == "bang" and t.strip() not in ("Bang", "BANG"))]


class Tep:
    def __init__(self, p, td, cam, bao):
        self.p, self.td, self.cam, self.bao = p, td, cam, bao
        self.s = open(p, encoding="utf8").read()
        self.sua = []  # (dau, cuoi, chu_moi)
        self.rel = os.path.relpath(p, os.path.dirname(WEB))

    def dong(self, i):
        return self.s.count("\n", 0, i) + 1

    def xet(self, a, b, kieu, truoc="", sau="", q=""):
        """Doan chu tho self.s[a:b] (noi dung chua bo thoat; q = dau nhay cua chuoi JS)."""
        tho = self.s[a:b]
        if re.search(r"\\[ux0-9]", tho):
            if can_doi(self.cam, tho):
                self.bao.append("XEM TAY (\\u) %s:%d %s" % (self.rel, self.dong(a), tho[:120]))
            return
        if kieu == "sql":
            tho = tho.replace("''", "'").replace("\\'", "'")
        if kieu in ("js", "tpl"):
            tho = re.sub(r"\\(.)", lambda m: {"n": "\n", "t": "\t"}.get(m.group(1), m.group(1)), tho)
        tim = can_doi(self.cam, tho)
        if not tim:
            return
        if kieu == "js" and (SO_SANH_TRUOC.search(truoc) or SO_SANH_SAU.search(sau)
                             or (KHOA_TRUOC.search(truoc) and KHOA_SAU.search(sau))):
            self.bao.append("XEM TAY %s:%d %s  [%s]" % (self.rel, self.dong(a), tho[:120].replace("\n", "|"), "|".join(tim)))
            return
        moi = self.td.doi(unicodedata.normalize("NFC", tho))
        if moi == unicodedata.normalize("NFC", tho):
            return
        ghi = moi
        if kieu in ("js", "tpl"):
            dau_nhay = q if kieu == "js" else "`"
            ghi = moi.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t").replace(dau_nhay, "\\" + dau_nhay)
            if kieu == "tpl":
                ghi = ghi.replace("${", "\\${")
        elif kieu == "sql":
            ghi = moi.replace("'", "''")
        elif kieu == "attr" and '"' in moi:
            self.bao.append("XEM TAY (nhay kep) %s:%d %s" % (self.rel, self.dong(a), moi[:120]))
            return
        self.sua.append((a, b, ghi))
        self.bao.append("DOI %s:%d\n    - %s\n    + %s" % (self.rel, self.dong(a), tho[:160].replace("\n", "|"),
                                                        moi[:160].replace("\n", "|")))

    # ---------------- JS/TS ----------------
    def js(self, i, n):
        """Quet ma JS/TS trong self.s[i:n]."""
        s = self.s
        while i < n:
            c = s[i]
            if s.startswith("//", i):
                j = s.find("\n", i)
                i = n if j < 0 or j > n else j
                continue
            if s.startswith("/*", i):
                j = s.find("*/", i + 2)
                i = n if j < 0 else j + 2
                continue
            if c in "'\"":
                j = i + 1
                while j < n and s[j] != c and s[j] != "\n":
                    j += 2 if s[j] == "\\" else 1
                dau = s.rfind("\n", 0, i) + 1
                self.xet(i + 1, j, "js", s[dau:i], s[j + 1:j + 40], c)
                i = j + 1
                continue
            if c == "`":
                i = self.mau_chuoi(i + 1, n)
                continue
            if c == "/" and self.co_the_regex(i):
                j = i + 1
                trong_lop = False
                while j < n and s[j] != "\n":
                    if s[j] == "\\":
                        j += 2
                        continue
                    if s[j] == "[":
                        trong_lop = True
                    elif s[j] == "]":
                        trong_lop = False
                    elif s[j] == "/" and not trong_lop:
                        break
                    j += 1
                i = j + 1
                continue
            i += 1
        return i

    def co_the_regex(self, i):
        k = i - 1
        while k >= 0 and self.s[k] in " \t":
            k -= 1
        return k < 0 or self.s[k] in "(,=:[!&|?{};\n" or self.s[max(0, k - 5):k + 1].endswith("return")

    def mau_chuoi(self, i, n):
        """Chuoi `...` bat dau o i (sau dau `): xet tung doan chu, bieu thuc ${} quet nhu JS."""
        s = self.s
        dau = i
        while i < n:
            if s[i] == "\\":
                i += 2
                continue
            if s[i] == "`":
                self.xet(dau, i, "tpl")
                return i + 1
            if s.startswith("${", i):
                self.xet(dau, i, "tpl")
                sau = 1
                j = i + 2
                k0 = j
                while j < n and sau:
                    if s[j] == "{":
                        sau += 1
                    elif s[j] == "}":
                        sau -= 1
                    elif s[j] in "'\"`":
                        q = s[j]
                        j += 1
                        while j < n and s[j] != q:
                            j += 2 if s[j] == "\\" else 1
                    j += 1
                self.js(k0, j - 1)
                i = j
                dau = i
                continue
            i += 1
        return i

    # ---------------- Vue template ----------------
    def template(self, i, n):
        s = self.s
        while i < n:
            if s.startswith("<!--", i):
                j = s.find("-->", i)
                i = n if j < 0 else j + 3
                continue
            m = re.match(r"<(script|style)\b[^>]*>", s[i:min(n, i + 200)], re.I)
            if m:
                e = s.lower().find("</%s>" % m.group(1).lower(), i)
                e = n if e < 0 or e > n else e
                if m.group(1).lower() == "script":
                    self.js(i + m.end(), e)
                i = e + len(m.group(1)) + 3
                continue
            if s[i] == "<":
                j = i + 1
                # the: <ten thuoc="..." :x="..." ...>
                while j < n and s[j] != ">":
                    if s[j] in "\"'":
                        q = s[j]
                        k = s.find(q, j + 1)
                        k = n if k < 0 else k
                        m = re.search(r"([:@#\w.-]+)\s*=\s*$", s[i:j])
                        ten = m.group(1) if m else ""
                        if ten.startswith((":", "@", "v-", "#")):
                            self.js(j + 1, k)
                        else:
                            self.xet(j + 1, k, "attr")
                        j = k + 1
                        continue
                    j += 1
                i = j + 1
                continue
            # chu giua the (co the co {{ bieu thuc }})
            j = s.find("<", i)
            j = n if j < 0 or j > n else j
            k = i
            while k < j:
                m = s.find("{{", k, j)
                if m < 0:
                    self.xet(k, j, "text")
                    break
                self.xet(k, m, "text")
                e = s.find("}}", m + 2, j)
                e = j if e < 0 else e
                self.js(m + 2, e)
                k = e + 2
            i = j

    def chay(self):
        s = self.s
        if self.p.endswith(".vue"):
            for m in re.finditer(r"<script\b[^>]*>(.*?)</script>", s, re.S):
                self.js(m.start(1), m.end(1))
            # khoi <template> GOC: the o dau dong (khong lay "<template" trong chu thich cua <script>)
            m = re.search(r"^<template\b[^>]*>(.*)^</template>", s, re.S | re.M)
            if m:
                self.template(m.start(1), m.end(1))
        elif self.p.endswith(".sql"):
            i = 0
            while i < len(s):
                if s.startswith("--", i):
                    j = s.find("\n", i)
                    i = len(s) if j < 0 else j
                    continue
                if s[i] == "'":
                    j = i + 1
                    while j < len(s):
                        if s[j] == "\\":
                            j += 2
                            continue
                        if s[j] == "'":
                            if s[j + 1:j + 2] == "'":
                                j += 2
                                continue
                            break
                        j += 1
                    self.xet(i + 1, j, "sql")
                    i = j + 1
                    continue
                i += 1
        else:
            self.js(0, len(s))
        return self.sua


def main():
    ghi = "--ghi" in sys.argv
    b = cp.doc_bang()
    td = cp.tu_dien_doi(b)
    cam = cp.tu_dien_cam(b)
    bao = []
    tong = 0
    goc = [os.path.join(WEB, g) for g in THU_MUC]
    for g in goc:
        for d, ds, fs in os.walk(g):
            ds[:] = [x for x in ds if x not in BO_THU_MUC]
            for f in sorted(fs):
                if not f.endswith(DUOI):
                    continue
                p = os.path.join(d, f)
                t = Tep(p, td, cam, bao)
                sua = t.chay()
                if not sua:
                    continue
                tong += len(sua)
                if ghi:
                    if open(p, encoding="utf8").read() != t.s:
                        bao.append("BO QUA (tep vua bi sua): " + t.rel)
                        continue
                    s = t.s
                    for a, b2, x in sorted(sua, reverse=True):
                        s = s[:a] + x + s[b2:]
                    open(p, "w", encoding="utf8").write(s)
    for x in bao:
        print(x)
    print("Doan chu %s: %d. Xem tay: %d" % ("da doi" if ghi else "se doi", tong,
                                            sum(x.startswith("XEM TAY") for x in bao)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
