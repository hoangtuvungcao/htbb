"""Sinh biểu tượng tiền tệ (game.pokemon.TienTe) - CÙNG thiết kế với TienTe.veVector (vẽ tay, không dùng icon có bản quyền).

    python3 client_jar/tools/tao_icon_tien.py

Ra:
  client_jar/res/tien/<ten>_<8|12|16|24|32>.png (0.9.7: 8 / 12 cho hàng ví thẻ HUD - icon không to hơn dòng chữ)   bản J2ME (jar/APK/PC chép client_jar/res; bản HD vẽ vector)
  web/public/tien/<ten>_<32|64>.png          website (1x / 2x)
ten: xu, luong, luong_khoa, su_kien, vinh_du.

Vẽ ở 8 lần cỡ rồi thu nhỏ (khử răng cưa) -> nét ở 16 px.
"""
import os
import sys

from PIL import Image, ImageDraw

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SS = 8


def mau(h):
    return ((h >> 16) & 255, (h >> 8) & 255, h & 255, 255)


def arc(d, x, y, w, h, bat_dau, quet, m):
    """fillArc kiểu J2ME (góc tính ngược chiều kim đồng hồ từ 3 giờ) -> PIL pieslice (chiều kim đồng hồ)."""
    if w <= 0 or h <= 0:
        return
    if quet >= 360:
        d.ellipse([x, y, x + w, y + h], fill=mau(m))
        return
    d.pieslice([x, y, x + w, y + h], -(bat_dau + quet), -bat_dau, fill=mau(m))


def tri(d, a, b, c, m):
    d.polygon([a, b, c], fill=mau(m))


def rect(d, x, y, w, h, m):
    d.rectangle([x, y, x + w, y + h], fill=mau(m))


def xu(d, s):
    arc(d, 0, 0, s, s, 0, 360, 0x7A4A00)
    v = s / 12 + SS
    arc(d, v, v, s - 2 * v, s - 2 * v, 0, 360, 0xF2B11E)
    arc(d, v, v, s - 2 * v, s - 2 * v, 60, 150, 0xFFDB63)
    r = s * 9 / 40
    cx = cy = s / 2
    arc(d, cx - r - SS, cy - r - SS, 2 * r + 2 * SS, 2 * r + 2 * SS, 0, 360, 0x7A4A00)
    arc(d, cx - r, cy - r, 2 * r, 2 * r, 0, 180, 0xD08A10)
    arc(d, cx - r, cy - r, 2 * r, 2 * r, 180, 180, 0xFFE58A)
    dd = s / 16 + SS
    rect(d, cx - r, cy - dd / 2, 2 * r, dd, 0x7A4A00)
    n = r * 2 / 3 + SS
    arc(d, cx - n / 2 - SS, cy - n / 2 - SS, n + 2 * SS, n + 2 * SS, 0, 360, 0x7A4A00)
    arc(d, cx - n / 2, cy - n / 2, n, n, 0, 360, 0xFFF8E0)


def pha(a, b):
    return (((a >> 16 & 255) + (b >> 16 & 255)) // 2) << 16 | (((a >> 8 & 255) + (b >> 8 & 255)) // 2) << 8 | ((a & 255) + (b & 255)) // 2


def ngoc(d, s, vien, dam, sang, lap):
    t, vai, day = s * 3 / 20, s * 8 / 20, s * 19 / 20
    l, r = s / 20, s * 19 / 20
    tl, tr, cx = s * 6 / 20, s * 14 / 20, s / 2
    o = SS
    tri(d, (tl, t - o), (tr, t - o), (r + o, vai), vien)
    tri(d, (tl, t - o), (l - o, vai), (r + o, vai), vien)
    tri(d, (l - o, vai), (r + o, vai), (cx, day + o), vien)
    tri(d, (l + o, vai), (r - o, vai), (cx, day - o), dam)
    tri(d, (tl + o, t), (tr - o, t), (r - o, vai), sang)
    tri(d, (tl + o, t), (l + o, vai), (r - o, vai), sang)
    tri(d, (tl + s / 20, vai), (tr - s / 20, vai), (cx, day - 2 * o), pha(dam, sang))
    tri(d, (tl + o, t + o), (cx, t + o), (tl + s / 10, vai - o), lap)


def khoa(d, x, y, s):
    bw, bh = s * 7 / 10, s * 5 / 10
    bx, by = x + (s - bw) / 2, y + s - bh
    qw, qh = bw * 6 / 10, s * 6 / 10
    qx = bx + (bw - qw) / 2
    day = s / 8 + SS
    arc(d, qx - SS, by - qh / 2 - SS, qw + 2 * SS, qh + 2 * SS, 0, 180, 0x1D2633)
    arc(d, qx, by - qh / 2, qw, qh, 0, 180, 0xD9DEE6)
    arc(d, qx + day, by - qh / 2 + day, qw - 2 * day, qh - 2 * day, 0, 180, 0x1D2633)
    rect(d, bx - SS, by - SS, bw + 2 * SS, bh + 2 * SS, 0x1D2633)
    rect(d, bx, by, bw, bh, 0xF2B11E)
    rect(d, bx, by, bw, max(SS, bh / 3), 0xFFDB63)
    k = bw / 5 + SS
    rect(d, bx + bw / 2 - k / 2, by + bh / 3, k, max(SS, bh / 2), 0x5A3A00)


def sao(d, cx, cy, r, m):
    c, s2, c2, s3 = r * 0.31, r * 0.95, r * 0.81, r * 0.59
    tri(d, (cx, cy - r), (cx - s3, cy + c2), (cx + s3 / 3, cy + c / 3), m)
    tri(d, (cx, cy - r), (cx + s3, cy + c2), (cx - s3 / 3, cy + c / 3), m)
    tri(d, (cx - s2, cy - c), (cx + s2, cy - c), (cx, cy + c), m)


def ve_su_kien(d, s):
    t, h = s / 5, s * 3 / 5
    rect(d, 0, t - SS, s, h + 2 * SS, 0x7A2E00)
    rect(d, SS, t, s - 2 * SS, h, 0xFF8A2A)
    rect(d, SS, t, s - 2 * SS, h / 3, 0xFFB566)
    arc(d, -s / 10, s / 2 - s / 10, s / 5, s / 5, 0, 360, 0x7A2E00)
    arc(d, s - s / 10, s / 2 - s / 10, s / 5, s / 5, 0, 360, 0x7A2E00)
    sao(d, s / 2, s / 2, s * 2 / 7, 0xFFE14D)


def vinh_du(d, s):
    tri(d, (s / 4, 0), (s / 2, 0), (s / 2 - SS, s / 2), 0xB3261E)
    tri(d, (s / 2, 0), (s * 3 / 4, 0), (s / 2 + SS, s / 2), 0x2B5DBF)
    dd = s * 13 / 20
    cx, cy = s / 2, s - dd / 2 - SS
    arc(d, cx - dd / 2 - SS, cy - dd / 2 - SS, dd + 2 * SS, dd + 2 * SS, 0, 360, 0x4A5466)
    arc(d, cx - dd / 2, cy - dd / 2, dd, dd, 0, 360, 0xD9DEE6)
    sao(d, cx, cy, dd * 2 / 5, 0xF2B11E)


VE = {
    "xu": xu,
    "luong": lambda d, s: ngoc(d, s, 0x0E3A78, 0x2F8FE6, 0x8FDBFF, 0xE6F8FF),
    "luong_khoa": lambda d, s: (ngoc(d, s, 0x2E3F57, 0x6E86A6, 0xB4C6DC, 0xEEF3FA), khoa(d, s * 9 / 20, s * 9 / 20, s * 11 / 20)),
    "su_kien": ve_su_kien,
    "vinh_du": vinh_du,
}


def tao(ten, co):
    s = co * SS
    a = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    VE[ten](ImageDraw.Draw(a), s)
    return a.resize((co, co), Image.LANCZOS)


def main():
    ra_jar = os.path.join(GOC, "client_jar", "res", "tien")
    ra_web = os.path.join(GOC, "web", "public", "tien")
    os.makedirs(ra_jar, exist_ok=True)
    os.makedirs(ra_web, exist_ok=True)
    for ten in VE:
        for co in (8, 12, 16, 24, 32):
            tao(ten, co).save(os.path.join(ra_jar, "%s_%d.png" % (ten, co)), optimize=True)
        for co in (32, 64):
            tao(ten, co).save(os.path.join(ra_web, "%s_%d.png" % (ten, co)), optimize=True)
    print("tao_icon_tien: %d bieu tuong -> %s, %s" % (len(VE), ra_jar, ra_web))


if __name__ == "__main__":
    sys.exit(main())
