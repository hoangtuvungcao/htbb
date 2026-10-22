#!/usr/bin/env python3
"""
Sinh lai anh du phong NPC tinh (client_jar/res/npc.sh = Unity Resources/res/npc.bytes) bang Pokemon.

NPC tinh (NPC.java / NPC.cs, type 0..31) ve bang PokeBody theo POKE_DEX_THEO_TYPE; npc.sh chi la duong
du phong khi thieu sprite. Ban goc la anh nguoi kiem hiep, nay moi muc npc<idImg>1.png (than, neo day)
la khung dung dau tien (huong xuong) cua Pokemon dau tien dung idImg do, npc<idImg>0.png (dau, ve chong
len than) la PNG 1x1 trong suot. Giu nguyen DANH SACH TEN muc de loader khong bao gio thieu anh.

Dinh dang goi .sh (FilePack.java): u8 soFile; moi file: u8 doDaiTen, ten XOR khoa, u16 doDai; roi toan bo
du lieu XOR khoa (chi so tinh tu 0). Anh lay tu client_jar/res/pk/<dex>.dl + <dex>_0.png (sprite PMDCollab
da dong goi, CC BY-NC 4.0 - ghi cong o GHI_CONG.txt).

Chay o goc repo:  python3 tools/pokemon/sinh_npc_sh.py   (can Pillow trong site-packages nguoi dung, nen khong dung -I)
Kiem: server/test/KiemNpcSprite.java.
"""
import io
import os
import re
import struct
import sys

from PIL import Image

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PK = os.path.join(GOC, "client_jar", "res", "pk")
NPC_JAVA = os.path.join(GOC, "client_jar", "src", "game", "model", "NPC.java")
DICH = [os.path.join(GOC, "client_jar", "res", "npc.sh"),
        os.path.join(GOC, "client_pc_ios", "Assets", "Resources", "res", "npc.bytes")]
KHOA = bytes([78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104])


def xor(b):
    return bytes(c ^ KHOA[i % len(KHOA)] for i, c in enumerate(b))


def doc_goi(p):
    d = open(p, "rb").read()
    i = 1
    muc = []
    for _ in range(d[0]):
        n = d[i]
        i += 1
        ten = xor(d[i:i + n]).decode("latin1")
        i += n
        muc.append((ten, struct.unpack(">H", d[i:i + 2])[0]))
        i += 2
    du = xor(d[i:])
    kq, p0 = [], 0
    for ten, n in muc:
        kq.append((ten, du[p0:p0 + n]))
        p0 += n
    return kq


def ghi_goi(muc):
    dau, than = bytearray([len(muc)]), b""
    for ten, b in muc:
        t = xor(ten.encode("latin1"))
        dau.append(len(t))
        dau += t
        dau += struct.pack(">H", len(b))
        than += b
    return bytes(dau) + xor(than)


def mang(ten, kieu):
    s = open(NPC_JAVA, encoding="utf8").read()
    m = re.search(r"%s\[\]\s+%s\s*=\s*\{([^}]*)\}" % (kieu, ten), s)
    return [int(x) for x in m.group(1).replace(" ", "").split(",") if x]


def png(im):
    o = io.BytesIO()
    im.save(o, "PNG", optimize=True)
    return o.getvalue()


def than(dex):
    dl = open(os.path.join(PK, "%04d.dl" % dex), "rb").read()
    w, h = struct.unpack(">HH", dl[2:6])
    k = Image.open(os.path.join(PK, "%04d_0.png" % dex)).convert("RGBA").crop((0, 0, w, h))
    return png(k.crop(k.getbbox()))


def main():
    id_img = mang("idImg", "byte")
    dex = mang("POKE_DEX_THEO_TYPE", "short")
    dex_theo_anh = {}
    for t, i in enumerate(id_img):
        dex_theo_anh.setdefault(i, dex[t])
    trong = png(Image.new("RGBA", (1, 1), (0, 0, 0, 0)))
    muc = []
    for ten, _ in doc_goi(DICH[0]):
        i = int(ten[3:-5])
        muc.append((ten, than(dex_theo_anh[i]) if ten[-5] == "1" else trong))
    b = ghi_goi(muc)
    for p in DICH:
        if open(p, "rb").read() != b:
            open(p, "wb").write(b)
            print("ghi", os.path.relpath(p, GOC))
    print(len(muc), "muc,", len(b), "byte")


if __name__ == "__main__":
    sys.exit(main())
