#!/usr/bin/env python3
"""
Thay tai cho anh quai / boss / than thu KIEM HIEP CU con phai giu tep (loader van doc) bang anh
Pokemon CUNG KICH THUOC, CUNG BO CUC KHUNG. Cac duong ve nay chi la duong du phong: client Pokemon
(jar, Unity) ve moi quai bang PokeBody / VatThePokemon (test KiemQuaiPokemon), nen anh nay gan nhu
khong bao gio hien - nhung neu co hien thi cung la Pokemon, khong con hinh cu.

  server/map/thanthu/<0..2>.png   (dai anh 12000, than thu Hoa Ky Lan cap 1..3) -> Ponyta / Rapidash / Rapidash shiny
  server/map/monster/<0..9>.png   (lenh -57 bien hinh, khung doc cao hFrameImgCharMonster) -> Pokemon theo BIEN_HINH
  client_jar/res/rong/Big0.png + shadow.png, Unity res/rong/Big0.bytes, shadow.bytes, shadow.png
                                  (Boss_Dragon: lop chet, chi nap luc khoi dong) -> cac manh Rayquaza theo dragonData

Nguon hinh: PMDCollab SpriteCollab (CC BY-NC 4.0, ghi cong trong GHI_CONG.txt nhu moi sprite khac),
doc tu tools/pokemon/pmd_raw/<dex>/ (tai bang tai_sprite_pmd.py; dat HTBB_PMD_RAW neu o cho khac).

Chay o goc repo (can Pillow, nen khong dung -I):  python3 tools/pokemon/sinh_anh_quai_cu.py
Chay lai cho ra cung byte (PNG khong metadata, PIL).
"""
import os
import sys
import xml.etree.ElementTree as ET

from PIL import Image, ImageDraw

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RAW = os.environ.get("HTBB_PMD_RAW", os.path.join(GOC, "tools", "pokemon", "pmd_raw"))

# server/map/monster/<i>.png: hFrame lay tu DataGame.hFrameImgCharMonster, dex thay the.
H_FRAME_BIEN_HINH = [60, 41, 41, 61, 64, 64, 31, 64, 64, 57]
BIEN_HINH = {
    0: "0262",  # nguoi soi -> Mightyena
    1: "0361",  # nguoi tuyet -> Snorunt
    2: "0362",  # nguoi tuyet lon -> Glalie
    3: "0711",  # bu nhin dau bi ngo -> Gourgeist
    4: "0225",  # -> Delibird
    5: "0288",  # Ton Ngo Khong -> Vigoroth
    6: "0399",  # gau meo -> Bidoof
    7: "0288",
    8: "0288",
    9: "0667",  # mua lan -> Litleo
}
THAN_THU = {0: "0077", 1: "0078", 2: "0078s"}
RONG = "0384"  # Rayquaza

# Hang huong PMD (8 huong) cho tung khoi 4 huong cua anh cu: khoi 0 trai, 1 phai, 2 xuong, 3 len
# (MessageCreator.frameRun: huong client 0 xuong dung khung 8.., 1 len 12.., 2 trai 0.., 3 phai 4..).
HANG_KHOI = [6, 2, 0, 4]


def anim(dex, ten):
    d = os.path.join(RAW, dex)
    goc = ET.parse(os.path.join(d, "AnimData.xml")).getroot()
    for a in goc.iter("Anim"):
        if a.findtext("Name") == ten:
            if a.find("CopyOf") is not None:
                return anim(dex, a.findtext("CopyOf"))
            w = int(a.findtext("FrameWidth"))
            h = int(a.findtext("FrameHeight"))
            im = Image.open(os.path.join(d, ten + "-Anim.png")).convert("RGBA")
            return im, w, h
    raise SystemExit("thieu anim %s cua %s" % (ten, dex))


def khung(dex, ten, hang, k):
    im, w, h = anim(dex, ten)
    n = im.width // w
    hang = min(hang, im.height // h - 1)
    f = im.crop(((k % n) * w, hang * h, (k % n) * w + w, hang * h + h))
    bb = f.getbbox()
    return f.crop(bb) if bb else f


def dat_vua(o, f, x, y, w, h, day=True):
    """Thu nho (giu ti le, nearest) cho vua o w x h, dat chan xuong day o."""
    s = min(1.0, w / f.width, h / f.height)
    if s < 1.0:
        f = f.resize((max(1, int(f.width * s)), max(1, int(f.height * s))), Image.NEAREST)
    dx = x + (w - f.width) // 2
    dy = y + (h - f.height) if day else y + (h - f.height) // 2
    o.alpha_composite(f, (dx, dy))


def luu(im, duong):
    os.makedirs(os.path.dirname(duong), exist_ok=True)
    im.save(duong, optimize=True)
    print("ghi", os.path.relpath(duong, GOC), im.size)


def bien_hinh():
    for i, dex in BIEN_HINH.items():
        p = os.path.join(GOC, "server", "map", "monster", "%d.png" % i)
        cu = Image.open(p)
        W, H = cu.size
        h = H_FRAME_BIEN_HINH[i]
        n = max(1, H // h)
        o = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        moi_khoi = max(1, n // 4)
        for k in range(n):
            hang = HANG_KHOI[min(3, k // moi_khoi)] if n >= 4 else 0
            dat_vua(o, khung(dex, "Walk", hang, k % moi_khoi), 0, k * h, W, h)
        luu(o, p)


def than_thu():
    for i, dex in THAN_THU.items():
        p = os.path.join(GOC, "server", "map", "thanthu", "%d.png" % i)
        W, H = Image.open(p).size
        # luoi khung Walk 4 huong x 4 buoc phu kin tam anh (bang khung cu cat o bat ky dau cung ra Pokemon)
        o = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        cw, ch = W // 4, H // 4
        for r in range(4):
            for c in range(4):
                dat_vua(o, khung(dex, "Walk", HANG_KHOI[r], c), c * cw, r * ch, cw, ch)
        luu(o, p)


def rong():
    du = os.path.join(GOC, "client_jar", "res", "rong", "dragonData")
    d = open(du, "rb").read()
    n = d[0]
    cu = Image.open(os.path.join(GOC, "client_jar", "res", "rong", "Big0.png"))
    o = Image.new("RGBA", cu.size, (0, 0, 0, 0))
    ve = ImageDraw.Draw(o)
    for i in range(n):
        idx, x, y, w, h = d[1 + i * 5:6 + i * 5]
        if idx <= 4:
            # dau rong 5 huong -> Rayquaza nhin theo huong do
            dat_vua(o, khung(RONG, "Walk", [0, 1, 2, 3, 4][idx], 0), x, y, w, h, day=False)
        else:
            # khuc than: vong xanh Rayquaza, soc vang
            ve.ellipse((x, y, x + w - 1, y + h - 1), fill=(46, 139, 87, 255), outline=(20, 70, 40, 255))
            cx = x + w // 2
            ve.line((cx, y + 2, cx, y + h - 3), fill=(240, 200, 40, 255), width=max(1, w // 6))
    luu(o, os.path.join(GOC, "client_jar", "res", "rong", "Big0.png"))
    bong = Image.new("RGBA", (16, 8), (0, 0, 0, 0))
    ImageDraw.Draw(bong).ellipse((0, 1, 15, 6), fill=(0, 0, 0, 90))
    luu(bong, os.path.join(GOC, "client_jar", "res", "rong", "shadow.png"))
    u = os.path.join(GOC, "client_pc_ios", "Assets", "Resources", "res", "rong")
    for ten, nguon in (("Big0.bytes", "Big0.png"), ("shadow.bytes", "shadow.png"), ("shadow.png", "shadow.png")):
        b = open(os.path.join(GOC, "client_jar", "res", "rong", nguon), "rb").read()
        open(os.path.join(u, ten), "wb").write(b)
        print("chep", ten)


if __name__ == "__main__":
    if not os.path.isdir(RAW):
        sys.exit("khong thay %s (chay tai_sprite_pmd.py hoac dat HTBB_PMD_RAW)" % RAW)
    bien_hinh()
    than_thu()
    rong()
