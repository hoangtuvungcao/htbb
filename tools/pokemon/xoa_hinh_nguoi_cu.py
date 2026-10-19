#!/usr/bin/env python3
"""
P2: xoa HINH (giu vo chua) cua bo phan nguoi kiem hiep cu - "paper doll": chan / than / dau / non / ao
choang / co (c/), vu khi (wpa/, wpsplash/) va trang phuc / vu khi thoi trang / canh trong d_effect.

Client Pokemon ve than Pokemon nguyen con (Char.paintPokemon) nen khong ve cac bo phan nay; chung chi
con duoc NAP o duong du phong (truoc khi lenh -90 toi, Unity nap bo mac dinh luc khoi dong) - xoa han
tep thi duong nap cu nem loi (Unity tung crash). Vi vay giu NGUYEN dinh dang, kich thuoc anh va du lieu
khung, chi thay diem anh bang trong suot: khong con mot diem anh kiem hiep nao, khong doi mot dong ma.

  dinh dang "c"  : u16 n + PNG (n byte) + u16 m + du lieu khung     client_jar/res/c, Unity res/c,
                                                                     server/map/c (lenh -47)
  bo PNG tach doi: <k>_h (dau PNG den het PLTE + 't') + data (tRNS + IDAT + IEND dung chung)
                   server/map/wpa/<n>/, goi wpsplash/<ten>/<j> (jar FilePack, Unity .bytes)
  goi FilePack   : wpsplash/<i>.wp (0.png, 1.png + .d)
  PNG d_effect   : id chi duong paper-doll ve (vu khi thoi trang, canh, toc / ao / non...).
Xoa: server/map/coat/ (khong loader nao doc).

Chay lai khong doi gi (ghi khi khac). Can Pillow: python3 tools/pokemon/xoa_hinh_nguoi_cu.py
"""
import io
import os
import shutil
import struct
import sys
import zlib

from PIL import Image

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.join(GOC, "tools", "pokemon"))

CODE = bytes([78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104])

# d_effect chi ve qua paper-doll (Char.paint khi paintPokemon() that bai): vu khi thoi trang 122-152,
# vu khi cai trang Gioi / Sa Tang / Mi Nuong / Son Tinh 254 255 297 298, quat / xuong / liem / canh
# 267-274, toc / ao / non / phi phong (8783, 8784, 8792-8797, 8806-8815). Xem bao cao fork hieu ung P2.
D_EFFECT_PAPER_DOLL = (list(range(122, 153)) + [254, 255, 297, 298] + list(range(267, 275))
                       + [83, 84] + list(range(92, 98)) + list(range(106, 116)))


def p(*a):
    return os.path.join(GOC, *a)


def png_trong(w, h):
    """PNG RGBA w x h trong suot hoan toan (nho nhat co the)."""
    b = io.BytesIO()
    Image.new("RGBA", (max(1, w), max(1, h)), (0, 0, 0, 0)).save(b, format="PNG", optimize=True)
    return b.getvalue()


def la_trong(png):
    try:
        im = Image.open(io.BytesIO(png)).convert("RGBA")
        return im.getextrema()[3][1] == 0
    except Exception:
        return False


def kich_thuoc(png):
    return Image.open(io.BytesIO(png)).size


def ghi(duong, moi):
    if open(duong, "rb").read() == moi:
        return False
    open(duong, "wb").write(moi)
    return True


# ------------------------------------------------------------------ dinh dang c

def lam_trong_c(b):
    n = struct.unpack(">H", b[:2])[0]
    png = b[2:2 + n]
    if la_trong(png):
        return b
    moi = png_trong(*kich_thuoc(png))
    return struct.pack(">H", len(moi)) + moi + b[2 + n:]


# ------------------------------------------------------------------ PNG tach doi (k_h + data)

def chunk(typ, data):
    return struct.pack(">I", len(data)) + typ + data + struct.pack(">I", zlib.crc32(typ + data) & 0xffffffff)


def lam_trong_tach_doi(headers, data):
    """headers: {ten: bytes}; tra (headers moi, data moi) cung kich thuoc anh / so mau, moi mau alpha 0."""
    ten0 = sorted(headers)[0]
    full = headers[ten0] + data
    try:
        if la_trong(full):
            return headers, data
        w, h = kich_thuoc(full)
    except Exception:
        return headers, data
    # IHDR / PLTE cua tung bien the giu nguyen; tRNS: alpha 0 cho moi mau; IDAT: chi so 0 moi diem.
    moi_h = {}
    trns_len = None
    for ten, hd in headers.items():
        i = 8
        out = hd[:8]
        bit = 8
        while i + 8 <= len(hd):
            ln = struct.unpack(">I", hd[i:i + 4])[0]
            typ = hd[i + 4:i + 8]
            if typ == b"IHDR":
                bit = hd[i + 8 + 8]
                if hd[i + 8 + 9] != 3:   # chi PNG bang mau (palette) - kieu khac giu nguyen
                    return headers, data
            if i + 12 + ln > len(hd):
                break
            if typ == b"IHDR" and hd[i + 8 + 12] != 0:   # bo xen ke (Adam7): IDAT moi ghi theo dong lien
                ihdr = hd[i + 8:i + 8 + 12] + b"\x00"
                out += chunk(b"IHDR", ihdr)
            elif typ not in (b"tIME", b"pHYs", b"tEXt"):
                out += hd[i:i + 12 + ln]
            i += 12 + ln
        npal = 1
        j = 8
        while j + 8 <= len(out):
            ln = struct.unpack(">I", out[j:j + 4])[0]
            if out[j + 4:j + 8] == b"PLTE":
                npal = ln // 3
            j += 12 + ln
        trns_len = npal if trns_len is None else min(trns_len, npal)
        moi_h[ten] = (out, bit)
    hdr = {}
    for ten, (out, bit) in moi_h.items():
        hdr[ten] = out + struct.pack(">I", trns_len) + b"t"
    bit = list(moi_h.values())[0][1]
    dong = (w * bit + 7) // 8
    idat = zlib.compress(b"".join(b"\x00" + b"\x00" * dong for _ in range(h)), 9)
    trns = b"\x00" * trns_len
    du = b"RNS" + trns + struct.pack(">I", zlib.crc32(b"tRNS" + trns) & 0xffffffff) + chunk(b"IDAT", idat) + chunk(b"IEND", b"")
    for ten in hdr:   # doc lai: dung kich thuoc, trong suot - khong thi giu nguyen
        anh = hdr[ten] + du
        if kich_thuoc(anh) != (w, h) or not la_trong(anh):
            raise ValueError("PNG tach doi %s doc lai sai" % ten)
    return hdr, du


# ------------------------------------------------------------------ FilePack

def xr(b):
    return bytes(c ^ CODE[i % len(CODE)] for i, c in enumerate(b))


def doc_goi(b):
    i = 0
    n = b[i]
    i += 1
    ent = []
    for _ in range(n):
        ln = b[i]
        i += 1
        name = xr(b[i:i + ln]).decode("latin1")
        i += ln
        L = struct.unpack(">H", b[i:i + 2])[0]
        i += 2
        ent.append((name, L))
    data = xr(b[i:])
    out = []
    q = 0
    for name, L in ent:
        out.append([name, data[q:q + L]])
        q += L
    return out


def ghi_goi(ent):
    h = bytearray([len(ent)])
    body = b""
    for name, b in ent:
        nb = xr(name.encode("latin1"))
        h.append(len(nb))
        h += nb
        h += struct.pack(">H", len(b))
        body += b
    return bytes(h) + xr(body)


def la_goi(b):
    try:
        ent = doc_goi(b)
        return bool(ent) and ghi_goi(ent) == b and any(e[0].endswith(".png") or e[0].endswith("_h") for e in ent)
    except Exception:
        return False


def lam_trong_goi(b):
    ent = doc_goi(b)
    if ghi_goi(ent) != b:
        raise ValueError("goi khong doc lai dung")
    ten = {e[0]: e for e in ent}
    hs = {k: v[1] for k, v in ten.items() if k.endswith("_h")}
    if hs and "data" in ten:
        moi_h, du = lam_trong_tach_doi(hs, ten["data"][1])
        for k, v in moi_h.items():
            ten[k][1] = v
        ten["data"][1] = du
    for e in ent:
        if e[0].endswith(".png") and not la_trong(e[1]):
            e[1] = png_trong(*kich_thuoc(e[1]))
    return ghi_goi(ent)


# ------------------------------------------------------------------ chay

def main():
    doi = 0
    # 1. dinh dang c
    for thu_muc, duoi in ((p("client_jar", "res", "c"), ""), (p("client_pc_ios", "Assets", "Resources", "res", "c"), ".bytes"),
                          (p("server", "map", "c"), "")):
        for goc, _, tep in os.walk(thu_muc):
            for t in tep:
                if t.endswith(".meta") or (duoi and not t.endswith(duoi)) or (not duoi and "." in t):
                    continue
                d = os.path.join(goc, t)
                b = open(d, "rb").read()
                if len(b) > 10 and b[2:6] == b"\x89PNG":
                    doi += ghi(d, lam_trong_c(b))
                elif la_goi(b):   # non (Unity c/hat), co (server c/flag): goi FilePack "0.png" + "0.d"
                    doi += ghi(d, lam_trong_goi(b))
    # 2. server/map/wpa/<n>/
    xoa_png = []
    wpa = p("server", "map", "wpa")
    for n in sorted(os.listdir(wpa)):
        tm = os.path.join(wpa, n)
        if not os.path.isfile(os.path.join(tm, "data")):
            continue
        hs = {t: open(os.path.join(tm, t), "rb").read() for t in os.listdir(tm) if t.endswith("_h")}
        moi_h, du = lam_trong_tach_doi(hs, open(os.path.join(tm, "data"), "rb").read())
        for t, v in moi_h.items():
            doi += ghi(os.path.join(tm, t), v)
        doi += ghi(os.path.join(tm, "data"), du)
        for t in os.listdir(tm):   # <k>.png: anh xem truoc goc, MapNapTaiNguyen chi doc <k>_h + data + wpa_data
            if t.endswith(".png"):
                os.remove(os.path.join(tm, t))
                xoa_png.append(os.path.join("server/map/wpa", n, t))
    # 2b. thu cuoi kieu cu gui trong -8 (OtherHandle.loadImgHorse): <X>_data + <X><j>_header (PNG tach doi)
    for ten in ("horse", "trau", "cop", "soi", "hac", "hactho", "tuanloc", "ph"):
        for goc, _, tep in os.walk(p("server", "map", ten)):
            for t in tep:
                if not t.endswith("_data"):
                    continue
                tien_to = t[:-len("_data")]
                hs = {h: open(os.path.join(goc, h), "rb").read() for h in tep
                      if h.startswith(tien_to) and h.endswith("_header") and h[len(tien_to):-len("_header")].isdigit()}
                if not hs:
                    continue
                moi_h, du = lam_trong_tach_doi(hs, open(os.path.join(goc, t), "rb").read())
                for h, v in moi_h.items():
                    doi += ghi(os.path.join(goc, h), v)
                doi += ghi(os.path.join(goc, t), du)
    arrow = p("server", "map", "imgArrow", "18__.png")   # DataGame chi nap imgArrow/0..19.png
    if os.path.isfile(arrow):
        os.remove(arrow)
        xoa_png.append("server/map/imgArrow/18__.png")
    # 3. goi wpsplash (jar + Unity)
    for thu_muc in (p("client_jar", "res", "wpsplash"), p("client_pc_ios", "Assets", "Resources", "res", "wpsplash")):
        for goc, _, tep in os.walk(thu_muc):
            for t in tep:
                if t.endswith(".meta"):
                    continue
                d = os.path.join(goc, t)
                doi += ghi(d, lam_trong_goi(open(d, "rb").read()))
    # 4. d_effect paper-doll
    for i in D_EFFECT_PAPER_DOLL:
        d = p("server", "map", "d_effect", "%d.png" % i)
        if os.path.isfile(d):
            b = open(d, "rb").read()
            if not la_trong(b):
                doi += ghi(d, png_trong(*kich_thuoc(b)))
    xoa = 0
    coat = p("server", "map", "coat")
    if os.path.isdir(coat):
        xoa = len(os.listdir(coat))
        shutil.rmtree(coat)
    print("lam trong %d tep, xoa %d tep (server/map/coat) + %d anh xem truoc wpa" % (doi, xoa, len(xoa_png)))


if __name__ == "__main__":
    main()
