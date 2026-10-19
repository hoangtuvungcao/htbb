#!/usr/bin/env python3
"""
P2: ve lai TAI CHO cac anh giao dien kiem hiep / thuong hieu cu ma client Pokemon con nap.

Giu nguyen ten muc, kich thuoc anh va bo khung (khong can sua code). Chi ghi khi diem anh khac
(chay lai = 0 thay doi). Jar client_jar/res/main.sh va Unity main.bytes giu giong het nhau.

  Unity client_pc_ios/Assets/Resources/res:
    other.bytes  logo, logo1 (chu "Khi Phach Anh Hung", "Ngu De")  -> chu HTBB (client_jar/shared/brand)
                 icon_sk_kiem/dao/ps/bua/cung (icon chieu vu khi)   -> o icon chieu Pokemon (server/map/sk0..4.png)
                 info (khung mau co rong)                          -> khung Poke Ball, giu thanh mau
                 swap (am duong)                                   -> Poke Ball + mui ten doi
                 swordskill_all1 / swordskill_kiem01 (kiem)        -> chep anh jar da ve lai (sword skill/)
                 rong_big0 / rong_shadow (rong vang cu)            -> chep anh jar da ve lai (rong/)
    items.bytes  dosat (kiem cheo), xuongngua (xuong ngua)         -> Poke Ball dau / xuong thu cuoi
    swapclothes.bytes (ao giap vang)                              -> mu huan luyen vien
    nation.bytes + client_jar/res/nation (huy hieu 3 phe)          -> huy hieu Kanto / Johto / Hoenn
    waiting.bytes + client_jar/res/waiting.png (cham xoay)         -> Poke Ball xoay 12 khung
  Jar client_jar/res/main.sh = Unity main.bytes: waypoint (ngu giac phap tran) -> vong dich den Poke Ball.
Giu nguyen (khung giao dien chung, khong kiem hiep): nut, bang, thanh mau, khung tui, cooldown, chat,
map_mini, msg0, skill_da/ice, swordskill_t0/t1, change/chat/dangxuat/exit/napxu va moi muc main.sh khac.

    python3 tools/pokemon/ve_giao_dien_thay_cu.py      (can Pillow)
"""
import io
import math
import os
import struct

from PIL import Image, ImageDraw

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
UR = os.path.join(GOC, "client_pc_ios", "Assets", "Resources", "res")
JR = os.path.join(GOC, "client_jar", "res")
SS = 8  # ve to roi thu nho cho mem

CODE = bytes([78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104])

DO, DO_T, TRANG, DEN, VANG = (230, 40, 45), (150, 20, 30), (250, 250, 250), (28, 28, 36), (250, 205, 60)


# ----------------------------------------------------------------------------- goi .sh / .bytes

def xr(b):
    return bytes(c ^ CODE[i % len(CODE)] for i, c in enumerate(b))


def doc_goi(p):
    d = open(p, "rb").read()
    i = 1
    ent = []
    for _ in range(d[0]):
        ln = d[i]
        i += 1
        name = xr(d[i:i + ln]).decode("latin1")
        i += ln
        ent.append((name, struct.unpack(">H", d[i:i + 2])[0]))
        i += 2
    data = xr(d[i:])
    out = []
    q = 0
    for name, ln in ent:
        out.append([name, data[q:q + ln]])
        q += ln
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


def png(im):
    bo = io.BytesIO()
    im.save(bo, format="PNG", optimize=True)
    return bo.getvalue()


def cung_diem(b, im):
    try:
        cu = Image.open(io.BytesIO(b)).convert("RGBA")
        return cu.size == im.size and cu.tobytes() == im.convert("RGBA").tobytes()
    except Exception:
        return False


def sua_goi(duong_ds, thay):
    """thay: ten muc -> ham(anh cu RGBA) -> anh moi. Ghi moi duong trong duong_ds (giong het nhau)."""
    ent = doc_goi(duong_ds[0])
    doi = []
    for e in ent:
        if e[0] in thay:
            cu = Image.open(io.BytesIO(e[1])).convert("RGBA")
            moi = thay[e[0]](cu)
            assert moi.size == cu.size, (e[0], moi.size, cu.size)
            if not cung_diem(e[1], moi):
                e[1] = png(moi)
                doi.append(e[0])
    if doi:
        b = ghi_goi(ent)
        for d in duong_ds:
            open(d, "wb").write(b)
    return doi


def sua_anh(duong_ds, ham):
    cu = Image.open(duong_ds[0]).convert("RGBA")
    moi = ham(cu)
    assert moi.size == cu.size
    if cung_diem(open(duong_ds[0], "rb").read(), moi):
        return False
    b = png(moi)
    for d in duong_ds:
        open(d, "wb").write(b)
    return True


# ----------------------------------------------------------------------------- ve

def nho(big, w, h):
    return big.resize((w, h), Image.LANCZOS)


def poke_ball(d, cx, cy, r, vien=None, goc=0.0, tren=DO):
    """Poke Ball tam (cx, cy) ban kinh r (toa do ve to), xoay goc do."""
    v = vien if vien is not None else max(1, r * 0.14)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=DEN)
    ri = r - v
    a0 = 180 + goc
    d.pieslice([cx - ri, cy - ri, cx + ri, cy + ri], a0, a0 + 180, fill=tren)
    d.pieslice([cx - ri, cy - ri, cx + ri, cy + ri], a0 + 180, a0 + 360, fill=TRANG)
    t = math.radians(goc)
    dx, dy = math.cos(t) * ri, math.sin(t) * ri
    d.line([cx - dx, cy - dy, cx + dx, cy + dy], fill=DEN, width=int(max(1, v * 1.2)))
    rc = r * 0.32
    d.ellipse([cx - rc, cy - rc, cx + rc, cy + rc], fill=DEN)
    rc2 = rc - v
    d.ellipse([cx - rc2, cy - rc2, cx + rc2, cy + rc2], fill=TRANG)
    # dom sang
    hs = r * 0.18
    hx, hy = cx - r * 0.45, cy - r * 0.5
    d.ellipse([hx - hs, hy - hs * 0.7, hx + hs, hy + hs * 0.7], fill=(255, 255, 255, 170))


def mui_ten(d, pts, mau, rong):
    d.line(pts, fill=mau, width=int(rong), joint="curve")
    (x0, y0), (x1, y1) = pts[-2], pts[-1]
    t = math.atan2(y1 - y0, x1 - x0)
    L = rong * 2.2
    a = [(x1 + math.cos(t) * L * 0.6, y1 + math.sin(t) * L * 0.6),
         (x1 + math.cos(t + 2.4) * L, y1 + math.sin(t + 2.4) * L),
         (x1 + math.cos(t - 2.4) * L, y1 + math.sin(t - 2.4) * L)]
    d.polygon(a, fill=mau)


def ve_logo(cu):
    w, h = cu.size
    logo = Image.open(os.path.join(GOC, "client_jar", "shared", "brand", "logo_256.png")).convert("RGBA")
    bb = logo.getbbox()
    if bb:
        logo = logo.crop(bb)
    s = min((w - 2) / logo.width, (h - 2) / logo.height)
    t = logo.resize((max(1, int(logo.width * s)), max(1, int(logo.height * s))), Image.LANCZOS)
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    out.alpha_composite(t, ((w - t.width) // 2, (h - t.height) // 2))
    return out


def ve_icon_sk(nguon):
    """Dai icon chieu: chep cac o 20x20 cua server/map/sk<n>.png (icon chieu Pokemon), cat/dem cho vua."""
    sk = Image.open(os.path.join(GOC, "server", "map", nguon)).convert("RGBA")

    def ham(cu):
        out = Image.new("RGBA", cu.size, (0, 0, 0, 0))
        out.alpha_composite(sk.crop((0, 0, cu.width, min(cu.height, sk.height))), (0, 0))
        return out
    return ham


def ve_info(cu):
    """Khung mau goc trai: bo con rong, ve vong Poke Ball quanh o tron; giu thanh mau ben phai."""
    w, h = cu.size
    out = cu.copy()
    px = out.load()
    for y in range(h):
        for x in range(w):
            if x < 54 or (y < 20 and x < 70):
                px[x, y] = (0, 0, 0, 0)
    big = Image.new("RGBA", (54 * SS, h * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    cx, cy, r = 28 * SS, 33 * SS, 25 * SS
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=DEN)
    r1 = r - 2 * SS
    d.pieslice([cx - r1, cy - r1, cx + r1, cy + r1], 180, 360, fill=DO)
    d.pieslice([cx - r1, cy - r1, cx + r1, cy + r1], 0, 180, fill=TRANG)
    d.rectangle([cx - r1, cy - 1.2 * SS, cx + r1, cy + 1.2 * SS], fill=DEN)
    r2 = r1 - 4 * SS
    d.ellipse([cx - r2 - SS, cy - r2 - SS, cx + r2 + SS, cy + r2 + SS], fill=DEN)
    for i in range(int(r2), 0, -SS // 2):    # o tron xanh ngoc (cho chan dung), sang dan vao tam
        k = 1 - i / r2
        mau = (int(20 + 90 * k), int(120 + 110 * k), int(120 + 100 * k), 255)
        d.ellipse([cx - i - 0.25 * r2 * k, cy - i - 0.3 * r2 * k, cx + i - 0.25 * r2 * k, cy + i - 0.3 * r2 * k], fill=mau)
    d.ellipse([cx - r * 0.55, cy - r * 0.8, cx - r * 0.25, cy - r * 0.6], fill=(255, 255, 255, 200))
    # noi vong voi khung thanh mau
    d.rectangle([cx + r - 2 * SS, cy - 7 * SS, 54 * SS, cy - 5 * SS], fill=VANG)
    out.alpha_composite(nho(big, 54, h), (0, 0))
    return out


def ve_swap(cu):
    w, h = cu.size
    big = Image.new("RGBA", (w * SS, h * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    c = w * SS / 2
    poke_ball(d, c, c, 6.2 * SS)
    xanh = (70, 170, 255)
    cam = (255, 160, 40)
    pts1 = [(c + 8.6 * SS * math.cos(math.radians(a)), c + 8.6 * SS * math.sin(math.radians(a))) for a in range(200, 330, 10)]
    pts2 = [(c + 8.6 * SS * math.cos(math.radians(a)), c + 8.6 * SS * math.sin(math.radians(a))) for a in range(20, 150, 10)]
    mui_ten(d, pts1, xanh, 1.6 * SS)
    mui_ten(d, pts2, cam, 1.6 * SS)
    return nho(big, w, h)


def chep(duong):
    anh = Image.open(duong).convert("RGBA")

    def ham(cu):
        assert anh.size == cu.size, (duong, anh.size, cu.size)
        return anh
    return ham


def nen_giay(cu):
    """Nen o giay da: vien + giay lay tu o "chat.png" (giu nguyen) cua items.bytes, bo hinh ve giua."""
    w, h = cu.size
    for ten, b in doc_goi(os.path.join(UR, "items.bytes")):
        if ten == "chat.png":
            cu = Image.open(io.BytesIO(b)).convert("RGBA")
    out = cu.copy()
    px = out.load()
    giay = px[w // 2, 4]
    for y in range(6, h - 6):
        for x in range(6, w - 6):
            px[x, y] = giay
    return out


def ve_dosat(cu):
    w, h = cu.size
    out = nen_giay(cu)
    big = Image.new("RGBA", (w * SS, h * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    c = w * SS / 2
    # tia lua dau hai ben + Poke Ball giua = "danh nhau"
    for s in (-1, 1):
        x0 = c + s * 13 * SS
        d.polygon([(x0, 9 * SS), (x0 - s * 4 * SS, 20 * SS), (x0 + s * 1 * SS, 20 * SS), (x0 - s * 3 * SS, 31 * SS),
                   (x0 + s * 5 * SS, 17 * SS), (x0, 17 * SS)], fill=(255, 215, 40), outline=(150, 90, 10))
    poke_ball(d, c, c + SS, 9.5 * SS)
    out.alpha_composite(nho(big, w, h))
    return out


def ve_xuongngua(cu):
    w, h = cu.size
    out = nen_giay(cu)
    big = Image.new("RGBA", (w * SS, h * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    c = w * SS / 2
    poke_ball(d, c - 3 * SS, 15 * SS, 8 * SS)
    mui_ten(d, [(c + 9 * SS, 9 * SS), (c + 9 * SS, 27 * SS)], (60, 40, 25), 2.6 * SS)
    d.rectangle([9 * SS, 30 * SS, 31 * SS, 32 * SS], fill=(60, 40, 25))
    out.alpha_composite(nho(big, w, h))
    return out


def ve_mu(cu):
    """Mu luoi trai huan luyen vien: do / trang, logo Poke Ball."""
    w, h = cu.size
    big = Image.new("RGBA", (w * SS, h * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    d.chord([2.5 * SS, 3 * SS, 20.5 * SS, 21 * SS], 180, 360, fill=DO, outline=DEN, width=SS)
    d.rectangle([3 * SS, 11.5 * SS, 20 * SS, 13 * SS], fill=TRANG)
    d.ellipse([1 * SS, 11 * SS, 22 * SS, 17 * SS], fill=DO_T, outline=DEN, width=SS)
    d.chord([2.5 * SS, 3 * SS, 20.5 * SS, 21 * SS], 180, 360, outline=DEN, width=SS)
    poke_ball(d, 11.5 * SS, 7.5 * SS, 3.4 * SS, vien=0.7 * SS)
    return nho(big, w, h)


def ve_nation(cu):
    """3 o 11x11 xep doc: huy hieu Kanto (do), Johto (vang), Hoenn (xanh luc)."""
    w, h = cu.size
    o = h // 3
    big = Image.new("RGBA", (w * SS, h * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    mau = [(DO, (255, 150, 150)), ((235, 175, 30), (255, 235, 140)), ((40, 170, 90), (150, 240, 170))]
    for i, (m, s) in enumerate(mau):
        y0 = i * o * SS
        cx = w * SS / 2
        khien = [(0.6 * SS, y0 + 0.8 * SS), (w * SS - 0.6 * SS, y0 + 0.8 * SS), (w * SS - 0.6 * SS, y0 + 5.5 * SS),
                 (cx, y0 + (o - 0.4) * SS), (0.6 * SS, y0 + 5.5 * SS)]
        d.polygon(khien, fill=m, outline=DEN)
        d.line([(0.6 * SS, y0 + 3.9 * SS), (w * SS - 0.6 * SS, y0 + 3.9 * SS)], fill=DEN, width=int(0.9 * SS))
        d.ellipse([cx - 1.9 * SS, y0 + 2 * SS, cx + 1.9 * SS, y0 + 5.8 * SS], fill=DEN)
        d.ellipse([cx - 1.1 * SS, y0 + 2.8 * SS, cx + 1.1 * SS, y0 + 5.0 * SS], fill=s)
    return nho(big, w, h)


def ve_waiting(cu):
    w, h = cu.size
    n = max(1, h // w)
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for i in range(n):
        big = Image.new("RGBA", (w * SS, w * SS), (0, 0, 0, 0))
        d = ImageDraw.Draw(big)
        poke_ball(d, w * SS / 2, w * SS / 2, (w / 2 - 1.5) * SS, goc=-i * 360.0 / n)
        out.alpha_composite(nho(big, w, w), (0, i * w))
    return out


def ve_waypoint(cu):
    """2 khung 40x24 (xanh, cam): vong dich den det + 4 dau mui ten + Poke Ball nho giua."""
    w, h = cu.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for i, (m, s) in enumerate((((80, 170, 255), (200, 235, 255)), ((255, 170, 40), (255, 235, 160)))):
        fh = h // 2
        big = Image.new("RGBA", (w * SS, fh * SS), (0, 0, 0, 0))
        d = ImageDraw.Draw(big)
        cx, cy = w * SS / 2, fh * SS / 2
        rx, ry = 19 * SS, 10.5 * SS
        d.ellipse([cx - rx, cy - ry, cx + rx, cy + ry], outline=m + (255,), width=int(1.3 * SS))
        d.ellipse([cx - rx * 0.7, cy - ry * 0.7, cx + rx * 0.7, cy + ry * 0.7], outline=s + (220,), width=SS)
        for a in (0, 90, 180, 270):
            t = math.radians(a)
            ex, ey = cx + math.cos(t) * rx * 0.86, cy + math.sin(t) * ry * 0.86
            ix, iy = cx + math.cos(t) * rx * 0.55, cy + math.sin(t) * ry * 0.55
            nx, ny = -math.sin(t), math.cos(t)
            d.polygon([(ix, iy), (ex + nx * 2.2 * SS, ey + ny * 1.4 * SS), (ex - nx * 2.2 * SS, ey - ny * 1.4 * SS)], fill=m + (255,))
        # Poke Ball det (nhin nghieng tren mat dat)
        bb = Image.new("RGBA", (12 * SS, 12 * SS), (0, 0, 0, 0))
        poke_ball(ImageDraw.Draw(bb), 6 * SS, 6 * SS, 5.5 * SS)
        bb = bb.resize((12 * SS, 7 * SS), Image.LANCZOS)
        big.alpha_composite(bb, (int(cx - 6 * SS), int(cy - 3.5 * SS)))
        out.alpha_composite(nho(big, w, fh), (0, i * fh))
    return out


def main():
    doi = []
    ds = sua_goi([os.path.join(UR, "other.bytes")], {
        "logo.png": ve_logo, "logo1.png": ve_logo,
        "icon_sk_kiem.png": ve_icon_sk("sk0.png"), "icon_sk_dao.png": ve_icon_sk("sk1.png"),
        "icon_sk_ps.png": ve_icon_sk("sk2.png"), "icon_sk_bua.png": ve_icon_sk("sk3.png"),
        "icon_sk_cung.png": ve_icon_sk("sk4.png"),
        "info.png": ve_info, "swap.png": ve_swap,
        "swordskill_all1.png": chep(os.path.join(JR, "sword skill", "all1.png")),
        "swordskill_kiem01.png": chep(os.path.join(JR, "sword skill", "kiem01.png")),
        "rong_big0.png": chep(os.path.join(JR, "rong", "Big0.png")),
        "rong_shadow.png": chep(os.path.join(JR, "rong", "shadow.png")),
    })
    doi += ["other.bytes:" + x for x in ds]
    doi += ["items.bytes:" + x for x in sua_goi([os.path.join(UR, "items.bytes")],
                                                {"dosat.png": ve_dosat, "xuongngua.png": ve_xuongngua})]
    doi += ["nation:" + x for x in sua_goi([os.path.join(JR, "nation"), os.path.join(UR, "nation.bytes")],
                                           {"icon.png": ve_nation})]
    doi += ["main.sh:" + x for x in sua_goi([os.path.join(JR, "main.sh"), os.path.join(UR, "main.bytes")],
                                            {"waypoint.png": ve_waypoint})]
    if sua_anh([os.path.join(UR, "swapclothes.bytes")], ve_mu):
        doi.append("swapclothes.bytes")
    if sua_anh([os.path.join(JR, "waiting.png"), os.path.join(UR, "waiting.bytes")], ve_waiting):
        doi.append("waiting")
    print("doi %d muc" % len(doi))
    for x in doi:
        print("  " + x)


if __name__ == "__main__":
    main()
