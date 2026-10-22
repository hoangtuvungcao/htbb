#!/usr/bin/env python3
"""
P2: ve lai 8Chars.png trong goi vong quay may man (client_jar/res/qs/qs = Unity res/qs/qs.bytes, DialLuckyScr):
mot phan tu vong da khac chu kieu bat quai -> phan tu vong quay Poke Ball (do / trang, viet sau theo ban kinh,
hai Poke Ball nho tren vanh). Giu nguyen kich thuoc, mat na alpha va o vat pham (mau xanh dam) cua anh cu.
Can Pillow; chay lai khong doi gi.   python3 tools/pokemon/ve_vong_quay.py
"""
import math
import os
import sys

from PIL import Image, ImageDraw

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.join(GOC, "tools", "pokemon"))
import xoa_hinh_nguoi_cu as goi   # noqa: E402  (doc / ghi FilePack)

DICH = [os.path.join(GOC, "client_jar", "res", "qs", "qs"),
        os.path.join(GOC, "client_pc_ios", "Assets", "Resources", "res", "qs", "qs.bytes")]
DO, TRANG, VIEN = (227, 53, 13), (245, 245, 240), (40, 30, 30)


def ve(cu):
    """Anh da ve (co diem do Poke Ball) thi tra nguyen - chay lai khong doi."""
    if any(r > 150 and g < 90 and b < 60 and a for r, g, b, a in (cu.get_flattened_data() if hasattr(cu, "get_flattened_data") else cu.getdata())):
        return cu
    w, h = cu.size
    px = cu.load()
    cx, cy = w, h   # tam vong = goc duoi phai (DialLuckyScr ve neo BOTTOM|RIGHT, lat ra 4 phan)
    da = {}
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a and abs(r - g) < 20 and abs(g - b) < 20 and r > 50:   # da xam (khong phai o xanh / vien toi)
                k = int(math.hypot(x + 0.5 - cx, y + 0.5 - cy))
                da.setdefault(k, []).append((r + g + b) / 3)
    if not da:
        return cu
    rmin, rmax = min(da), max(da)
    sang = {k: sum(v) / len(v) for k, v in da.items()}
    moi = cu.copy()
    q = moi.load()
    giua = (rmin + rmax) / 2.0
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if not (a and abs(r - g) < 20 and abs(g - b) < 20 and r > 50):
                continue
            k = int(math.hypot(x + 0.5 - cx, y + 0.5 - cy))
            t = sang.get(k, 128) / 160.0
            nen = DO if k >= giua else TRANG
            if abs(k - giua) < 1.2 or k >= rmax - 1 or k <= rmin:
                nen = VIEN
            q[x, y] = tuple(max(0, min(255, int(c * min(1.25, max(0.55, t))))) for c in nen) + (a,)
    d = ImageDraw.Draw(moi)
    for goc in (22.5, 67.5):   # hai Poke Ball nho tren vanh do
        rr = (giua + rmax) / 2.0
        x = cx - rr * math.cos(math.radians(goc))
        y = cy - rr * math.sin(math.radians(goc))
        s = max(2.5, (rmax - giua) / 2.0 - 0.5)
        d.ellipse([x - s, y - s, x + s, y + s], fill=TRANG, outline=VIEN)
        d.chord([x - s, y - s, x + s, y + s], 180, 360, fill=DO, outline=VIEN)
        d.line([x - s, y, x + s, y], fill=VIEN)
        d.point((round(x), round(y)), fill=TRANG)
    return moi


def main():
    import io
    doi = 0
    moi_png = None
    for f in DICH:
        b = open(f, "rb").read()
        ent = goi.doc_goi(b)
        for e in ent:
            if e[0] == "8Chars.png":
                if moi_png is None:
                    anh = ve(Image.open(io.BytesIO(e[1])).convert("RGBA"))
                    bo = io.BytesIO()
                    anh.save(bo, format="PNG", optimize=True)
                    moi_png = bo.getvalue()
                if Image.open(io.BytesIO(e[1])).convert("RGBA").tobytes() != Image.open(io.BytesIO(moi_png)).convert("RGBA").tobytes():
                    e[1] = moi_png
        moi = goi.ghi_goi(ent)
        if moi != b:
            open(f, "wb").write(moi)
            doi += 1
    print("vong quay: ghi %d tep" % doi)


if __name__ == "__main__":
    main()
