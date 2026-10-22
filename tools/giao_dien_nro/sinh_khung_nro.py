"""Sinh BỘ KHUNG + ICON PIXEL giao diện kiểu NRO / NSO (0.9.7) cho client HTBB - tự vẽ, CC0 (docs/giay-phep.md).

    python3 tools/giao_dien_nro/sinh_khung_nro.py          # sinh lại
    python3 tools/giao_dien_nro/sinh_khung_nro.py --kiem   # chỉ kiểm tệp trong repo khớp bộ sinh (thoát 1 = lệch)

Ra:
  client_jar/res/nro/khung.png   tấm ghép mảnh 9-slice + hình cố định (1 px ảnh = 1 px logic giao diện)
  client_jar/res/nro/icon.png    icon 16x16, 8 cột, ô i = id game.pokemon.BieuTuong (icon_nro.py)
  client_jar/src/game/pokemon/KhungNroBang.java   bảng toạ độ mảnh (sinh - KHÔNG sửa tay)
  docs/anh/giao_dien_nro.png     ảnh xem trước phóng x4 (tài liệu)

Thư mục res/nro đi vào CẢ jar J2ME (nhỏ, ~10 KB), APK và PC. Mọi bản vẽ bằng một mã game.pokemon.KhungNro
(drawRegion lát cạnh / giữa), lấy mẫu láng giềng gần nhất -> pixel nét ở mọi cỡ màn.
Thiết kế: giao_dien_nro_thiet_ke (README trong tools/giao_dien_nro).
"""
import io
import os
import sys

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import icon_nro  # noqa: E402

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
RA_RES = os.path.join(GOC, "client_jar", "res", "nro")
RA_JAVA = os.path.join(GOC, "client_jar", "src", "game", "pokemon", "KhungNroBang.java")
RA_XEM = os.path.join(GOC, "docs", "anh", "giao_dien_nro.png")

# ------------------------------------------------------------------ bảng màu (khớp ChuDe)
O = 0x1B0E06       # viền ngoài
O2 = 0x2A1608      # đường tối trong
W1, W2, W3, W4 = 0x5C2E12, 0x8E4A1E, 0xB8692E, 0xDE9A52          # gỗ
G1, G2, G3, G4 = 0x8A5A12, 0xC8902A, 0xF2C54E, 0xFFE89A          # đồng / vàng
P0, P1, P2, P3 = 0xC9A46A, 0xE7CF9C, 0xF2E0B4, 0xFAEECF          # giấy da
D0, D1, D2, D3 = 0x120A06, 0x2E1E14, 0x4A3424, 0x6A4E36          # lòng ô tối
R0, R1, R2, R3, R4 = 0x5E0E06, 0x8E1A0E, 0xC83220, 0xE85A3A, 0xFFA088
NEN_TOI = 0x2B1A10

# họ màu nút: (tối 2, tối 1, thân, sáng, ánh)
HO_NUT = {
    "CAM": (0x8A3C06, 0xB85A10, 0xE8861E, 0xF8AE44, 0xFFDA8A),
    "XANH": (0x142C6A, 0x1E4296, 0x2F66C4, 0x4F8AE6, 0xA8CCFF),
    "LA": (0x134A16, 0x1E6A22, 0x36962E, 0x58BC44, 0xA6EC84),
    "DO": (0x5E0E06, 0x8E1A0E, 0xC83220, 0xE85A3A, 0xFFA088),
    "GIAY": (0x8A6236, 0xB88E58, 0xEAD3A0, 0xF6E6C0, 0xFFF8E4),
    "DA": (0x3A3A40, 0x5A5A62, 0x8A8A92, 0xA6A6AE, 0xC8C8D0),
    "GO": (0x2A1206, 0x4E260E, 0x7A3E18, 0xA05A28, 0xD08A4A),
}


class Anh:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.a = np.zeros((h, w, 4), dtype=np.uint8)

    def p(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            if c is None:
                self.a[y, x] = (0, 0, 0, 0)
            else:
                self.a[y, x] = (c >> 16 & 255, c >> 8 & 255, c & 255, 255)

    def ve_nen(self, x, y, w, h, c):
        for j in range(y, y + h):
            for i in range(x, x + w):
                self.p(i, j, c)

    def lay(self, x, y):
        v = self.a[y, x]
        return None if v[3] == 0 else (int(v[0]) << 16 | int(v[1]) << 8 | int(v[2]))


def vong(a, k, tl, br, x0=0, y0=0, w=None, h=None, mo_duoi=False):
    """Vòng thứ k (thụt k px): trên + trái màu tl, dưới + phải màu br."""
    w = a.w if w is None else w
    h = a.h if h is None else h
    l, t, r, b = x0 + k, y0 + k, x0 + w - 1 - k, y0 + h - 1 - k
    for x in range(l, r + 1):
        a.p(x, t, tl)
        if not mo_duoi:
            a.p(x, b, br)
    for y in range(t, b + 1 if not mo_duoi else y0 + h):
        a.p(l, y, tl)
        a.p(r, y, br)


def bo_goc(a, n=1, mo_duoi=False):
    """Cắt góc bậc thang n px (trong suốt) - góc pixel."""
    pts = []
    if n >= 1:
        pts += [(0, 0)]
    if n >= 2:
        pts += [(1, 0), (0, 1)]
    for (x, y) in pts:
        a.p(x, y, None)
        a.p(a.w - 1 - x, y, None)
        if not mo_duoi:
            a.p(x, a.h - 1 - y, None)
            a.p(a.w - 1 - x, a.h - 1 - y, None)


def chen_goc_vien(a, n, c):
    """Sau bo_goc: tô lại pixel viền chéo (góc tròn pixel liền nét)."""
    if n >= 2:
        for (x, y) in [(1, 1)]:
            a.p(x, y, c)
            a.p(a.w - 1 - x, y, c)
            a.p(x, a.h - 1 - y, c)
            a.p(a.w - 1 - x, a.h - 1 - y, c)


# ------------------------------------------------------------------ mảnh
MANH = []   # (tên, ảnh, l, t, r, b, kiểu) kiểu: 0 = 9-slice lát, 1 = hình cố định


def them(ten, a, l=0, t=0, r=0, b=0, kieu=0):
    MANH.append((ten, a, l, t, r, b, kieu))


def bong_poke(a, x, y):
    """Poké Ball 8x8 đinh tán góc."""
    m = ["..OOOO..",
         ".OrRRRO.",
         "ORRPRRRO",
         "OOOWWOOO",
         "OWOwwOWO",
         "OWWOOWwO",
         ".OWWWwO.",
         "..OOOO.."]
    mau = {'O': O, 'R': R2, 'r': R3, 'P': R4, 'W': 0xFFFFFF, 'w': 0xC8C8D0}
    for j, d in enumerate(m):
        for i, ch in enumerate(d):
            if ch != '.':
                a.p(x + i, y + j, mau[ch])


def tam_dong(a, x, y, n):
    """Tấm đồng n x n (góc khung) viền tối, ánh trên-trái."""
    for j in range(n):
        for i in range(n):
            c = G3
            if i == 0 or j == 0 or i == n - 1 or j == n - 1:
                c = O
            elif i == 1 or j == 1:
                c = G4
            elif i == n - 2 or j == n - 2:
                c = G1
            elif i + j > n:
                c = G2
            a.p(x + i, y + j, c)
    for (i, j) in [(0, 0), (n - 1, 0), (0, n - 1), (n - 1, n - 1)]:
        a.p(x + i, y + j, None)


def lat_goc(a, x, y, n):
    """Chép góc trái-trên (n x n) sang 3 góc còn lại (lật gương)."""
    for j in range(n):
        for i in range(n):
            v = a.a[y + j, x + i].copy()
            a.a[y + j, a.w - 1 - (x + i)] = v
            a.a[a.h - 1 - (y + j), x + i] = v
            a.a[a.h - 1 - (y + j), a.w - 1 - (x + i)] = v


def manh_tam(ten, than, vien_trong, goc=12, keo=16):
    """Tấm lớn: gỗ 4 lớp + chỉ vàng + đường tối + mép lõm, góc = tấm đồng + Poké Ball."""
    n = goc * 2 + keo
    a = Anh(n, n)
    a.ve_nen(0, 0, n, n, than)
    vong(a, 0, O, O)
    vong(a, 1, W4, W2)
    vong(a, 2, W3, W1)
    vong(a, 3, W2, W1)
    vong(a, 4, W1, W1)
    vong(a, 5, G3, G1)
    vong(a, 6, O2, O2)
    vong(a, 7, vien_trong[0], vien_trong[1])
    # vân gỗ trên cạnh (chu kỳ keo = 16 -> lát liền)
    for x in range(goc, goc + keo):
        u = x - goc
        if u in (2, 3, 9):
            a.p(x, 2, W2)
            a.p(x, n - 3, W2)
        if u in (6, 13):
            a.p(x, 3, W1)
            a.p(x, n - 4, W1)
        if u in (11,):
            a.p(x, 1, W3)
    for y in range(goc, goc + keo):
        u = y - goc
        if u in (2, 3, 9):
            a.p(2, y, W2)
            a.p(n - 3, y, W2)
        if u in (6, 13):
            a.p(3, y, W1)
            a.p(n - 4, y, W1)
    bo_goc(a, 2)
    chen_goc_vien(a, 2, O)
    # góc trang trí: tấm đồng 12x12 + Poké Ball
    tam_dong(a, 0, 0, goc)
    bong_poke(a, 2, 2)
    lat_goc(a, 0, 0, goc)
    them(ten, a, goc, goc, goc, goc)


def manh_hop(ten, than, goc=8, keo=16):
    n = goc * 2 + keo
    a = Anh(n, n)
    a.ve_nen(0, 0, n, n, than)
    vong(a, 0, O, O)
    vong(a, 1, W3, W1)
    vong(a, 2, W2, W1)
    vong(a, 3, G3, G1)
    vong(a, 4, O2, O2)
    vong(a, 5, P0, P3)
    bo_goc(a, 2)
    chen_goc_vien(a, 2, O)
    # đinh vàng 4x4 ở góc
    for (i, j, c) in [(1, 1, O), (2, 1, O), (1, 2, O), (2, 2, G4), (3, 2, G3), (2, 3, G3), (3, 3, G1),
                      (4, 2, O), (2, 4, O), (4, 3, O), (3, 4, O), (4, 4, O)]:
        a.p(i, j, c)
    lat_goc(a, 0, 0, goc)
    them(ten, a, goc, goc, goc, goc)


def manh_vong(ten, rings, than, goc, keo=8, n_bo=1, mo_duoi=False, kieu=0):
    """Mảnh chung: danh sách vòng (tl, br) từ ngoài vào, thân một màu."""
    n = goc * 2 + keo
    a = Anh(n, n)
    a.ve_nen(0, 0, n, n, than)
    for k, (tl, br) in enumerate(rings):
        vong(a, k, tl, br, mo_duoi=mo_duoi)
    bo_goc(a, n_bo, mo_duoi=mo_duoi)
    if n_bo >= 2:
        chen_goc_vien(a, 2, rings[0][0])
    them(ten, a, goc, goc, goc, 0 if mo_duoi else goc, kieu)
    return a


def manh_nut(ten, ho, nhan=False, goc=5, keo=6):
    """Nút khối: viền tối, 2 hàng ánh trên, 2 hàng tối dưới, mép trái sáng / phải tối. Nhấn = lõm."""
    t2, t1, than, sang, anh = HO_NUT[ho]
    n = goc * 2 + keo
    a = Anh(n, n)
    if not nhan:
        a.ve_nen(0, 0, n, n, than)
        for x in range(n):
            a.p(x, 1, anh)
            a.p(x, 2, sang)
            a.p(x, n - 3, t1)
            a.p(x, n - 2, t2)
        for y in range(1, n - 3):
            a.p(1, y, anh if y < 3 else sang)
            a.p(n - 2, y, t1)
        a.p(n - 2, 1, sang)
    else:
        a.ve_nen(0, 0, n, n, t1 if ho != "GIAY" else 0xDCC08A)
        for x in range(n):
            a.p(x, 1, t2)
            a.p(x, 2, t1)
            a.p(x, n - 2, t1)
        for y in range(1, n - 1):
            a.p(1, y, t2)
            a.p(n - 2, y, t1)
    vong(a, 0, O, O)
    bo_goc(a, 2)
    chen_goc_vien(a, 2, O)
    them(ten, a, goc, goc, goc, goc)


def manh_tab(ten, chon, goc=5, keo=6):
    n = goc * 2 + keo
    a = Anh(n, n)
    if chon:
        a.ve_nen(0, 0, n, n, P2)
        for x in range(n):
            a.p(x, 1, G3)
            a.p(x, 2, P3)
        for y in range(1, n):
            a.p(1, y, P3 if y > 1 else G3)
            a.p(n - 2, y, P0 if y > 1 else G1)
    else:
        a.ve_nen(0, 0, n, n, W2)
        for x in range(n):
            a.p(x, 1, W4)
            a.p(x, 2, W3)
        for y in range(1, n):
            a.p(1, y, W3)
            a.p(n - 2, y, W1)
    vong(a, 0, O, O, mo_duoi=True)
    bo_goc(a, 2, mo_duoi=True)
    a.p(1, 1, O)
    a.p(n - 2, 1, O)
    them(ten, a, goc, goc, goc, goc)


def manh_bien(ten):
    """Biển tiêu đề đỏ: dải vàng trên + dưới, thân đỏ, hai đầu bo pixel."""
    l, t, keo = 10, 6, 8
    w, h = 2 * l + keo, 2 * t + 6
    a = Anh(w, h)
    hang = [O, G4, G2, O, R3] + [R2] * (h - 10) + [R1, O, G3, G1, O]
    for y, c in enumerate(hang):
        for x in range(w):
            a.p(x, y, c)
    # đầu: viền vàng bọc dọc + bo
    for y in range(h):
        for (x, c) in [(0, O), (1, G4 if y < h // 2 else G3), (2, G2 if y < h // 2 else G1), (3, O)]:
            if 0 < y < h - 1:
                a.p(x, y, c)
                a.p(w - 1 - x, y, c)
        if 3 < y < h - 4:
            a.p(4, y, R3)
            a.p(w - 5, y, R1)
    for (x, y) in [(0, 0), (1, 0), (0, 1), (2, 0), (0, 2)]:
        a.p(x, y, None)
        a.p(w - 1 - x, y, None)
        a.p(x, h - 1 - y, None)
        a.p(w - 1 - x, h - 1 - y, None)
    for (x, y) in [(1, 1), (2, 1), (1, 2)]:
        a.p(x, y, O)
        a.p(w - 1 - x, y, O)
        a.p(x, h - 1 - y, O)
        a.p(w - 1 - x, h - 1 - y, O)
    them(ten, a, l, t, l, t)


def manh_thanh(ten, ho):
    """Dải đổ đầy thanh (cao bất kỳ): ánh trên 1 px, thân, tối dưới 1 px; lát ngang."""
    mau = {"HP": (0xFF9478, 0xE0301E, 0x8E1A0E), "PP": (0xA8CCFF, 0x3A78D8, 0x1E3E8A),
           "EXP": (0xFFF09A, 0xF2C21A, 0xA87A06), "LA": (0xB4F08A, 0x4AB03A, 0x1E6A1E)}[ho]
    a = Anh(4, 5)
    for x in range(4):
        a.p(x, 0, mau[0])
        a.p(x, 1, mau[1])
        a.p(x, 2, mau[1])
        a.p(x, 3, mau[1])
        a.p(x, 4, mau[2])
    them(ten, a, 0, 2, 0, 2)


def hinh(ten, m, mau):
    a = Anh(len(m[0]), len(m))
    for j, d in enumerate(m):
        for i, ch in enumerate(d):
            if ch != '.':
                a.p(i, j, mau[ch])
    them(ten, a, kieu=1)


def tao_manh():
    manh_tam("TAM", P2, (P0, P3))
    manh_tam("TAM_TOI", NEN_TOI, (0x160C06, 0x4A3020))
    manh_hop("HOP", P2)
    manh_vong("THE", [(0xA87E48, 0xFFF6DC), (0xD4B47E, 0xF0DDB0)], 0xE9D2A0, 4, n_bo=1)
    manh_vong("THE_TOI", [(0x0E0804, 0x5A4030), (0x1E120A, 0x3A2818)], 0x2A1C12, 4, n_bo=1)
    manh_vong("HANG", [(0x6A4422, 0x6A4422), (P3, P0)], P2, 4, n_bo=2)
    manh_vong("HANG_CHON", [(G1, G1), (G4, G2), (G3, G3)], 0xFBE9B0, 4, n_bo=2)
    manh_vong("HANG_NHAN", [(0x6A4422, 0x6A4422), (P0, P1)], P1, 4, n_bo=2)
    manh_vong("TOOLTIP", [(O, O), (G2, G1)], NEN_TOI, 3, n_bo=1)
    manh_bien("BIEN")
    for ho in ("CAM", "XANH", "LA", "DO", "GIAY", "DA", "GO"):
        manh_nut("NUT_" + ho, ho)
        manh_nut("NUT_" + ho + "_NHAN", ho, nhan=True)
    manh_tab("TAB", False)
    manh_tab("TAB_CHON", True)
    # ô đồ: hộp gỗ nổi + lòng tối lõm
    manh_vong("O", [(O, O), (W4, W1), (W3, W2), (O2, O2), (D0, D3)], D1, 5, keo=6, n_bo=2)
    manh_vong("O_CHON", [(O, O), (G4, G1), (G3, G2), (O2, O2), (D0, D3)], 0x3A2616, 5, keo=6, n_bo=2)
    manh_vong("O_TRONG", [(O, O), (W3, W1), (W2, W1), (O2, O2), (D0, D2)], 0x24170E, 5, keo=6, n_bo=2)
    manh_vong("O_KHOA", [(O, O), (0xB0B0B8, 0x55555E), (0x85858E, 0x55555E), (O2, O2), (0x2A2A2E, 0x6A6A72)],
              0x4A4A52, 5, keo=6, n_bo=2)
    manh_vong("O_SANG", [(0x6A4422, 0x6A4422), (P0, P3)], P1, 3, keo=6, n_bo=1)
    # thanh
    manh_vong("RANH", [(O, O), (D0, 0x3A2A1E)], 0x24160C, 2, keo=4, n_bo=1)
    for ho in ("HP", "PP", "EXP", "LA"):
        manh_thanh("DAY_" + ho, ho)
    manh_vong("CUON", [(O, O), (0x1E1008, 0x4A3020)], 0x3A2414, 2, keo=4, n_bo=1)
    manh_vong("CUON_NUM", [(O, O), (G4, G1)], G3, 2, keo=4, n_bo=1)
    # hình cố định
    trang = {'O': O, 'W': 0xFFFFFF, 'w': 0xE8C8C0, 'R': R2, 'r': R1, 'P': R3, 'Y': G3, 'y': G1, 'L': G4,
             'B': 0x2F66C4, 'b': 0x1E3E8A, 'C': 0x5A94EA, 'G': 0x36962E, 'g': 0x1E6A22, 'l': 0x6CCB4A,
             'k': 0x4A4A54, 'E': P2, 'e': P0, 'n': D1, 'd': D0, 'K': 0x8A8A92}
    dong = ["..OOOOOOOOOOOO..",
            ".OYLLLLLLLLLLYO.",
            "OYLPPPPPPPPPPyyO",
            "OLPOOPPPPPPOOPyO",
            "OLPOWOPPPPOWOPyO",
            "OLPPOWOPPOWOPPyO",
            "OLPPPOWOOWOPPPyO",
            "OLPPPPOWWOPPPRyO",
            "OLPPPPOWWOPPPRyO",
            "OLPPPOWOOWORRRyO",
            "OLPPOWORROWORRyO",
            "OLPOWORRRROWORyO",
            "OLROOrRRRRrOORyO",
            "OyRrrrrrrrrrrryO",
            ".OyyyyyyyyyyyyO.",
            "..OOOOOOOOOOOO.."]
    hinh("DONG", dong, trang)
    lui = [r.replace('P', 'C').replace('R', 'B').replace('r', 'b') for r in
           ["..OOOOOOOOOOOO..",
            ".OYLLLLLLLLLLYO.",
            "OYLPPPPPPPPPPyyO",
            "OLPPPPPOOPPPPPyO",
            "OLPPPPOWOPPPPPyO",
            "OLPPPOWWOOOOOPyO",
            "OLPPOWWWWWWWOPyO",
            "OLPOWWWWWWWWOPyO",
            "OLPPOWWWWWWWORyO",
            "OLPPPOWWOOOOORyO",
            "OLPPPPOWORRRRRyO",
            "OLPPPPPOORRRRRyO",
            "OLRRRRRRRRRRRRyO",
            "OyRrrrrrrrrrrryO",
            ".OyyyyyyyyyyyyO.",
            "..OOOOOOOOOOOO.."]]
    hinh("LUI", lui, trang)
    hinh("CHAM", [".OOOOO.",
                  "OWWWWWO",
                  "OWPRRWO",
                  "OWRRRWO",
                  "OWRRrWO",
                  "OWWWWWO",
                  ".OOOOO."], trang)
    hinh("DINH", [".OOOO.",
                  "OLLYYO",
                  "OLYYyO",
                  "OYYyyO",
                  "OYyyyO",
                  ".OOOO."], trang)
    hinh("NUM_TRUOT", ["..OOOOOO..",
                       ".OLLLLLLO.",
                       "OLYYYYYYyO",
                       "OLYOYYOYyO",
                       "OLYOYYOYyO",
                       "OLYOYYOYyO",
                       "OLYOYYOYyO",
                       "OLYOYYOYyO",
                       "OLYOYYOYyO",
                       "OLYYYYYYyO",
                       "OLYYYYYYyO",
                       "OyyyyyyyyO",
                       ".OyyyyyyO.",
                       "..OOOOOO.."], trang)
    hinh("CONG_TAC_TAT", ["..OOOOOOOOOOOOOOOOOOOOOO..",
                          ".OddddddddddddddddddddddO.",
                          "OdOOOOOOOOdnnnnnnnnnnnnndO",
                          "OOKKKKKKKKOnnnnnnnnnnnnnnO",
                          "OOKwwwwwwKOnnnnnnnnnnnnnnO",
                          "OOKwKKKKwKOnnnnnnnnnnnnnnO",
                          "OOKwKKKKwKOnnnnnnnnnnnnnnO",
                          "OOKwKKKKwKOnnnnnnnnnnnnnnO",
                          "OOKwKKKKwKOnnnnnnnnnnnnnnO",
                          "OOKwwwwwwKOnnnnnnnnnnnnnnO",
                          "OOKKKKKKKKOnnnnnnnnnnnnnnO",
                          "OdOOOOOOOOdnnnnnnnnnnnnndO",
                          ".OddddddddddddddddddddddO.",
                          "..OOOOOOOOOOOOOOOOOOOOOO.."], dict(trang, w=0xD4D4DC))
    hinh("CONG_TAC_BAT", ["..OOOOOOOOOOOOOOOOOOOOOO..",
                          ".OggggggggggggggggggggggO.",
                          "OglllllllllllllgOOOOOOOOgO",
                          "OGGGGGGGGGGGWGGOYYYYYYYYOO",
                          "OGGGGGGGGGGWWGGOYLLLLLLYOO",
                          "OGGGWGGGGGWWGGGOYLYYYYLYOO",
                          "OGGGWWGGGWWGGGGOYLYYYYLYOO",
                          "OGGGGWWGWWGGGGGOYLYYYYLYOO",
                          "OGGGGGWWWGGGGGGOYLYYYYLYOO",
                          "OGGGGGGWGGGGGGGOYLLLLLLYOO",
                          "OGGGGGGGGGGGGGGOYYYYYYYYOO",
                          "OgggggggggggggggOOOOOOOOgO",
                          ".OggggggggggggggggggggggO.",
                          "..OOOOOOOOOOOOOOOOOOOOOO.."], dict(trang, L=G4))
    hinh("MUI_PHAI", ["OO....",
                      "OYO...",
                      "OLYO..",
                      "OLYyO.",
                      "OLYyyO",
                      "OLYyO.",
                      "OLyO..",
                      "OyO...",
                      "OO...."], trang)


# ------------------------------------------------------------------ xếp tấm ghép (kệ)
def xep(manh, rong=192):
    x = y = h_ke = 0
    vt = []
    for (ten, a, *_r) in manh:
        if x + a.w > rong:
            x = 0
            y += h_ke + 1
            h_ke = 0
        vt.append((x, y))
        x += a.w + 1
        h_ke = max(h_ke, a.h)
    cao = y + h_ke
    tam = np.zeros((cao, rong, 4), dtype=np.uint8)
    for (ten, a, *_r), (px, py) in zip(manh, vt):
        tam[py:py + a.h, px:px + a.w] = a.a
    return tam, vt


def tam_icon():
    cot = 8
    so = max(i for (i, _, _) in icon_nro.ICON) + 1
    hang = (so + cot - 1) // cot
    tam = np.zeros((hang * 16, cot * 16, 4), dtype=np.uint8)
    loi = []
    for (i, ten, ve) in icon_nro.ICON:
        dong = [d for d in ve.split("\n") if d.strip() != "" or len(d) == 16]
        dong = [d for d in ve.strip("\n").split("\n")]
        if len(dong) != 16:
            loi.append("%s: %d dòng" % (ten, len(dong)))
        for j, d in enumerate(dong[:16]):
            if len(d) != 16:
                loi.append("%s dòng %d: %d ký tự" % (ten, j, len(d)))
            for k, ch in enumerate(d[:16]):
                if ch not in icon_nro.BANG_MAU:
                    loi.append("%s: ký tự lạ %r" % (ten, ch))
                    continue
                c = icon_nro.BANG_MAU[ch]
                if c is not None:
                    tam[(i // cot) * 16 + j, (i % cot) * 16 + k] = (c >> 16 & 255, c >> 8 & 255, c & 255, 255)
    return tam, loi, so


def java(vt, so_icon):
    d = []
    d.append("package game.pokemon;\n")
    d.append("/**\n * BẢNG MẢNH giao diện NRO (0.9.7) - SINH bởi tools/giao_dien_nro/sinh_khung_nro.py, KHÔNG sửa tay.\n"
             " * Mỗi mảnh 9 số: x, y, rộng, cao trong /nro/khung.png, lề 9-slice trái, trên, phải, dưới, kiểu (0 lát, 1 hình\n"
             " * cố định); GIUA = màu thân (RGB, -1 = lát ảnh) để tô giữa bằng fillRect.\n */\n")
    d.append("public final class KhungNroBang {\n    private KhungNroBang() {\n    }\n\n")
    d.append('    public static final String TAM_KHUNG = "/nro/khung.png", TAM_ICON = "/nro/icon.png";\n')
    d.append("    public static final int SO_ICON = %d, COT_ICON = 8;\n\n" % so_icon)
    for i, (ten, *_r) in enumerate(MANH):
        d.append("    public static final int %s = %d;\n" % (ten, i))
    d.append("\n    public static final short[] MANH = {\n")
    giua = []
    for (ten, a, l, t, r, b, kieu), (x, y) in zip(MANH, vt):
        d.append("        %d, %d, %d, %d, %d, %d, %d, %d, %d,   // %s\n" % (x, y, a.w, a.h, l, t, r, b, kieu, ten))
        g = -1
        if kieu == 0 and a.w - l - r > 0 and a.h - t - b > 0:
            khoi = a.a[t:a.h - b, l:a.w - r]
            if (khoi == khoi[0, 0]).all() and khoi[0, 0, 3] == 255:
                g = int(khoi[0, 0, 0]) << 16 | int(khoi[0, 0, 1]) << 8 | int(khoi[0, 0, 2])
        giua.append(g)
    d.append("    };\n\n    public static final int[] GIUA = {\n        ")
    d.append(", ".join(("0x%06X" % g) if g >= 0 else "-1" for g in giua))
    d.append("\n    };\n}\n")
    return "".join(d)


def png(arr):
    b = io.BytesIO()
    Image.fromarray(arr, "RGBA").save(b, "PNG", optimize=True)
    return b.getvalue()


def main():
    kiem = "--kiem" in sys.argv
    tao_manh()
    tam, vt = xep(MANH)
    ic, loi, so = tam_icon()
    if loi:
        print("LỖI icon:\n  " + "\n  ".join(loi))
        sys.exit(1)
    ra = {os.path.join(RA_RES, "khung.png"): png(tam), os.path.join(RA_RES, "icon.png"): png(ic),
          RA_JAVA: java(vt, so).encode("utf-8")}
    # ảnh xem trước: khung + icon, phóng x4 trên nền cỏ
    h = tam.shape[0] + ic.shape[0] + 4
    w = max(tam.shape[1], ic.shape[1])
    xem = np.zeros((h, w, 4), dtype=np.uint8)
    xem[:, :] = (0x3E, 0x6E, 0x34, 255)
    for src, oy in ((tam, 0), (ic, tam.shape[0] + 4)):
        m = src[:, :, 3:4].astype(np.float32) / 255
        vung = xem[oy:oy + src.shape[0], :src.shape[1]]
        vung[:, :, :3] = (src[:, :, :3] * m + vung[:, :, :3] * (1 - m)).astype(np.uint8)
    xem = np.repeat(np.repeat(xem, 4, axis=0), 4, axis=1)
    ra[RA_XEM] = png(xem)
    lech = []
    for duong, du in ra.items():
        if kiem:
            if duong.endswith(".png"):
                # so điểm ảnh (nén PNG có thể khác giữa phiên bản PIL)
                if not os.path.exists(duong) or not np.array_equal(np.asarray(Image.open(duong).convert("RGBA")),
                                                                   np.asarray(Image.open(io.BytesIO(du)).convert("RGBA"))):
                    lech.append(duong)
            elif not os.path.exists(duong) or open(duong, "rb").read() != du:
                lech.append(duong)
            continue
        os.makedirs(os.path.dirname(duong), exist_ok=True)
        open(duong, "wb").write(du)
        print("ra", os.path.relpath(duong, GOC), len(du), "byte")
    if kiem:
        if lech:
            print("LỆCH (chạy lại bộ sinh):", ", ".join(os.path.relpath(x, GOC) for x in lech))
            sys.exit(1)
        print("khung NRO: khớp (%d mảnh, %d icon)" % (len(MANH), so))


main()
