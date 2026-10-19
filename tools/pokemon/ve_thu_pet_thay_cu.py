#!/usr/bin/env python3
"""
P2: ve lai TAI CHO cac o anh du phong cua thu cuoi / pet / skin linh thu con la hinh kiem hiep cu
(Cu, Rong, Doi, Dai bang, Ky Lan, Poro, Heo rung, Ngua xuong, Su tu...) bang hinh Pokemon PMDCollab.

Client Pokemon (jar, APK, PC, Unity) ve thu cuoi / pet bang PokeBody theo lenh -93 / -94; cac anh duoi
day chi con la DUONG DU PHONG (client cu, dex 0, thieu sprite). Du vay khong de sot anh cu nao:
moi o anh giu NGUYEN kich thuoc anh va vi tri / kich thuoc tung o nho (bang khung khong doi), chi
thay diem anh ben trong bang khung hinh Pokemon tuong ung (anh xa giong du_lieu/bang_thu.csv).

  server/map/d_effect/<id>.png  pet-tool (24-28, 35-44, 60), Lan hoa hinh (67-69), thu cuoi tool
                                (56, 62, 64, 65, 234, 236, 275, 276) va thu hoa hinh (265, 266):
                                ve vao tung o nho cua bang khung <id> (+ <id>.pet neu co).
  server/map/pet/<idImg>.png    pet kieu cu (5200+): dai doc nFrame khung (Map.petTemplate).
  server/map/avtathu/<i>.png    anh thu o bang "Trang bi thu" (Animal.fr[i] khung doc).
  server/map/skinthu/<n>.png    atlas skin linh thu 0..5 (o nho theo bang khung <n>).
Xoa: anh khong co loader nao doc (pet/44, 81, 444; avtathu/9_, soi; skinthu/6.png; anh .png trong
horse/ trau/ cop/ soi/ hac/ hactho/ tuanloc/ ph/ - OtherHandle chi doc tep _data/_header).

Nguon hinh: tools/pokemon/pmd_raw/<dex>[s]/ (tai_sprite_pmd.py; dat HTBB_PMD_RAW neu o noi khac).
Can Pillow (chay KHONG kem -I neu Pillow nam trong user site). Chay lai khong doi gi (ghi khi khac).

    python3 tools/pokemon/ve_thu_pet_thay_cu.py
"""
import os
import re
import struct
import sys
import xml.etree.ElementTree as ET

from PIL import Image, ImageDraw

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RAW = os.environ.get("HTBB_PMD_RAW") or os.path.join(GOC, "tools", "pokemon", "pmd_raw")
MAP = os.path.join(GOC, "server", "map")

# id d_effect -> khoa PMD (dex, "s" = shiny). Theo data.Pet.getIdEffPet / data.Animal.getIdImageTool.
D_EFFECT = {
    # pet trung trang thai 1 (atb1 = 24..28): Cu, Rong, Yeu tinh, Doi, Dai bang
    24: "0163", 25: "0147", 26: "0173", 27: "0041", 28: "0627",
    # level 1 / level >= 2 cua tung dong
    37: "0164", 38: "0164s",      # Cu -> Noctowl
    36: "0148", 35: "0149",       # Rong -> Dragonair / Dragonite
    39: "0035", 40: "0036s",      # Yeu tinh -> Clefairy / Clefable
    41: "0042", 42: "0169",       # Doi -> Golbat / Crobat
    43: "0628", 44: "0628s",      # Dai bang -> Braviary
    60: "0417",                   # Bach Thu (727) -> Pachirisu
    67: "0058", 68: "0059", 69: "0244",   # Lan hoa hinh cap 1-3 / 4 / 5 (PetPokemon.DEX_HOA_HINH)
    # thu cuoi tool (8700 + id = Animal.getIdImageTool) -> ThuPokemon.DEX
    56: "0831", 62: "0245", 64: "0221", 65: "0897", 234: "0149", 236: "0473", 275: "0244", 276: "0896",
    265: "0244", 266: "0244",     # thu hoa hinh (ThuPokemon.DEX_HOA_HINH)
}

# Animal.idImg -> khoa (ThuPokemon.DEX / SHINY), so khung = Animal.fr
THU = ["0078", "0059", "0128", "0262", "0018", "0146", "0428s", "0234", "0144", "0831", "0245",
       "0221", "0897", "0149", "0250", "0357", "0227", "0330", "0473", "0244", "0896"]
FR = [3, 2, 2, 2, 3, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 5, 5]

# skin linh thu 0..5 -> khoa (ThuPokemon.DEX_SKIN)
SKIN = ["0879", "0675", "0716", "0323", "0745", "0483"]

XOA = ["pet/44.png", "pet/81.png", "pet/444.png", "avtathu/9_.png", "avtathu/soi.png", "skinthu/6.png"]
THU_MUC_NGUA = ["horse", "trau", "cop", "soi", "hac", "hactho", "tuanloc", "ph"]


def p(*a):
    return os.path.join(MAP, *a)


# ----------------------------------------------------------------------------- PMD

_bo_nho = {}


def anim(khoa, ten):
    """(anh, rong khung, cao khung, so khung) cua dong tac ten (CopyOf giai ve anim goc)."""
    k = (khoa, ten)
    if k in _bo_nho:
        return _bo_nho[k]
    goc = ET.parse(os.path.join(RAW, khoa, "AnimData.xml")).getroot()
    ds = {a.findtext("Name"): a for a in goc.iter("Anim")}
    a = ds.get(ten)
    while a is not None and a.findtext("CopyOf"):
        a = ds.get(a.findtext("CopyOf"))
    if a is None:
        _bo_nho[k] = None
        return None
    fw, fh = int(a.findtext("FrameWidth")), int(a.findtext("FrameHeight"))
    anh = Image.open(os.path.join(RAW, khoa, a.findtext("Name") + "-Anim.png")).convert("RGBA")
    kq = (anh, fw, fh, anh.width // fw)
    _bo_nho[k] = kq
    return kq


def khung(khoa, i, hang=0, ten="Walk"):
    """Khung thu i (vong lap) cua dong tac, hang PMD (0 xuong, 2 phai, 4 len, 6 trai), cat sat hinh."""
    a = anim(khoa, ten) or anim(khoa, "Idle")
    anh, fw, fh, n = a
    hang = min(hang, anh.height // fh - 1)
    im = anh.crop(((i % n) * fw, hang * fh, (i % n) * fw + fw, hang * fh + fh))
    bb = im.getbbox()
    return im.crop(bb) if bb else im


def vua(im, w, h):
    """Thu phong de VUA o w x h (giu ti le), phong to theo so nguyen (pixel art), thu nho BOX."""
    if w <= 0 or h <= 0:
        return Image.new("RGBA", (max(1, w), max(1, h)), (0, 0, 0, 0))
    s = min(w / im.width, h / im.height)
    if s >= 1:
        k = max(1, int(s))
        return im.resize((im.width * k, im.height * k), Image.NEAREST)
    return im.resize((max(1, int(im.width * s)), max(1, int(im.height * s))), Image.BOX)


def dan(nen, im, x, y, w, h):
    """Dan im vao o (x, y, w, h) cua nen: can giua ngang, sat day."""
    t = vua(im, w, h)
    nen.alpha_composite(t, (x + (w - t.width) // 2, y + h - t.height))


def la_bong(w, h):
    return h <= 12 and w >= 1.6 * h


def ve_bong(nen, x, y, w, h):
    d = ImageDraw.Draw(nen)
    d.ellipse([x, y, x + w - 1, y + h - 1], fill=(0, 0, 0, 90))


def o_nho(duong):
    """Danh sach o nho (id, x, y, w, h) o dau bang khung (DataSkillEff / bang pet / skin)."""
    if not os.path.isfile(duong):
        return []
    b = open(duong, "rb").read()
    n = b[0]
    return [tuple(b[1 + i * 5:6 + i * 5]) for i in range(n) if len(b) >= 6 + i * 5]


def luu(im, duong):
    """Ghi khi diem anh khac (chay lai khong doi gi)."""
    if os.path.isfile(duong):
        try:
            cu = Image.open(duong).convert("RGBA")
            if cu.size == im.size and cu.tobytes() == im.tobytes():
                return False
        except Exception:
            pass
    im.save(duong, optimize=True)
    return True


# ----------------------------------------------------------------------------- tung loai

def ve_theo_o(duong_png, cac_bang, khoa):
    """Ve lai atlas: moi o nho cua cac bang khung (gop, bo trung) nhan mot khung Pokemon."""
    anh = Image.open(duong_png)
    W, H = anh.size
    nen = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    da = set()
    k = 0
    for bang in cac_bang:
        for (_, x, y, w, h) in o_nho(bang):
            if (x, y, w, h) in da or w == 0 or h == 0 or x + w > W or y + h > H:
                continue
            da.add((x, y, w, h))
            if la_bong(w, h):
                ve_bong(nen, x, y, w, h)
                continue
            hang = 2 if w > 1.3 * h else 0
            dan(nen, khung(khoa, k, hang), x, y, w, h)
            k += 1
    return luu(nen, duong_png)


def ve_dai_doc(duong_png, n, khoa, ten="Walk"):
    """Dai n khung xep doc (rong = rong anh, cao = cao anh / n)."""
    anh = Image.open(duong_png)
    W, H = anh.size
    n = max(1, n)
    ch = H // n
    nen = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    for i in range(n):
        dan(nen, khung(khoa, i, 0, ten), 0, i * ch, W, ch)
    return luu(nen, duong_png)


def mau_pet():
    """idImg -> nFrame tu Map.loadPetTemplate (mau dau tien dung idImg do)."""
    s = open(os.path.join(GOC, "server", "src", "real", "Map.java"), encoding="utf8").read()
    kq = {}
    for m in re.finditer(r"new PetTemplate\(\(byte\) (\d+), \"[^\"]*\", \d+, \(byte\) (\d+), \(byte\) (\d+),", s):
        kq.setdefault(int(m.group(2)), int(m.group(3)))
    return kq


def main():
    if not os.path.isdir(RAW):
        sys.exit("thieu %s (chay tai_sprite_pmd.py hoac dat HTBB_PMD_RAW)" % RAW)
    doi = []
    for idd, khoa in sorted(D_EFFECT.items()):
        png = p("d_effect", "%d.png" % idd)
        if ve_theo_o(png, [p("d_effect", str(idd)), p("d_effect", "%d.pet" % idd)], khoa):
            doi.append(png)
    pet = mau_pet()
    import csv
    with open(os.path.join(GOC, "du_lieu", "bang_pet_cu.csv"), encoding="utf8") as f:
        for r in csv.DictReader(f):
            i = int(r["id_img"])
            png = p("pet", "%d.png" % i)
            if os.path.isfile(png) and ve_dai_doc(png, pet.get(i, 2), "%04d" % int(r["dex"])):
                doi.append(png)
    for i, khoa in enumerate(THU):
        png = p("avtathu", "%d.png" % i)
        if ve_dai_doc(png, FR[i], khoa, "Idle"):
            doi.append(png)
    for n, khoa in enumerate(SKIN):
        png = p("skinthu", "%d.png" % n)
        if ve_theo_o(png, [p("skinthu", str(n))], khoa):
            doi.append(png)
    xoa = []
    for r in XOA:
        if os.path.isfile(p(r)):
            os.remove(p(r))
            xoa.append(r)
    for tm in THU_MUC_NGUA:
        for goc, _, tep in os.walk(p(tm)):
            for t in tep:
                if t.lower().endswith(".png"):
                    os.remove(os.path.join(goc, t))
                    xoa.append(os.path.relpath(os.path.join(goc, t), MAP))
    print("ve lai %d anh, xoa %d anh" % (len(doi), len(xoa)))
    for d in doi:
        print("  ve ", os.path.relpath(d, GOC))
    for x in xoa:
        print("  xoa", x)


if __name__ == "__main__":
    main()
