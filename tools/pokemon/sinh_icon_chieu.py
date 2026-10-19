"""Sinh BỘ ICON CHIÊU PNG (0.9.5) - CHỈ dùng cho GIAO DIỆN (nút chiêu HUD, đòn chính, tab Kỹ năng / Ô nhanh, chọn
chiêu nhanh, gợi ý, bảng Tự động, cẩm nang web). Hiệu ứng chiêu trong trận (map / imgEffs, res/pk/hu.bin) KHÔNG đổi.

    python3 tools/pokemon/sinh_icon_chieu.py           # sinh ảnh
    python3 tools/pokemon/sinh_icon_chieu.py --kiem    # chỉ kiểm: mọi chiêu có họ hình + hệ, tên khớp client / server

Mỗi chiêu (du_lieu/noi_dung_chieu.csv, thứ tự dòng 0..4 x số hiệu chiêu = game.pokemon.PokeChieu.TEN) được gán một HỌ
HÌNH (HO bên dưới: va chạm, cào, cắn, lá, roi, hạt, tia nước, bong bóng, tàn lửa, đại hỏa, sét, ...). Huy hiệu =
khối bo góc (hoặc tròn cho nút HUD) màu HỆ (KieuO.MAU_HE): dải sáng -> tối, ánh trong, viền tối; hình trắng viền tối
ở giữa; góc phải trên có dấu LOẠI (chỉ bản bo góc) (vật lý = chớp nổ cam, đặc biệt = vòng tròn tím, trạng thái = nửa tròn xám).
Vẽ vector bằng PIL siêu mẫu (x8) rồi thu nhỏ khử răng cưa.

Ra (client_jar/res/ui/, PC + APK; J2ME giữ hình 12x12 cũ của KieuO vì giới hạn dung lượng):
  chieu_{32,64,96}.png        tấm ghép 8 cột, ô i = chiêu thứ i (PokeChieu: dòng 0 trước, rồi dòng 1...), bo góc
  chieu_tron_{32,64,96}.png   như trên, huy hiệu TRÒN (nút chiêu HUD), hình hơi cao để chừa chỗ nhãn tên
  web/public/chieu/<dong>-<so>.png   từng icon 64 px cho trang cẩm nang Chiêu thức
Giấy phép: tự vẽ cho dự án, CC0 1.0 (docs/giay-phep.md).
"""
import csv
import math
import os
import re
import sys

from PIL import Image, ImageChops, ImageDraw, ImageFilter

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
RA = os.path.join(GOC, "client_jar", "res", "ui")
RA_WEB = os.path.join(GOC, "web", "public", "chieu")
CSV = os.path.join(GOC, "du_lieu", "noi_dung_chieu.csv")
COT = 8
CO = (32, 64, 96)
SS = 8            # siêu mẫu: vẽ 768 px rồi thu
N = 96 * SS

TEN_HE = ["Thường", "Lửa", "Nước", "Điện", "Cỏ", "Băng", "Giác đấu", "Độc", "Đất",
          "Bay", "Siêu linh", "Bọ", "Đá", "Ma", "Rồng", "Bóng tối", "Thép", "Tiên"]
MAU_HE = [0xA8A878, 0xF08030, 0x6890F0, 0xF8D030, 0x78C850, 0x98D8D8, 0xC03028, 0xA040A0, 0xE0C068,
          0xA890F0, 0xF85888, 0xA8B820, 0xB8A038, 0x705898, 0x7038F8, 0x705848, 0xB8B8D0, 0xEE99AC]

# ---------------------------------------------------------------- chiêu -> họ hình, loại (V vật lý / D đặc biệt / T trạng thái)
CHIEU = {
    (0, "Cào"): ("cao", "V"), (0, "Than hồng"): ("tan_lua", "D"), (0, "Nanh lửa"): ("can", "V"),
    (0, "Xoáy lửa"): ("loc", "D"), (0, "Màn khói"): ("khoi", "T"), (0, "Múa kiếm"): ("mua_kiem", "T"),
    (0, "Súng phun lửa"): ("lua_lon", "D"), (0, "Vuốt rồng"): ("rong", "V"), (0, "Đại hỏa"): ("dai_hoa", "D"),
    (1, "Húc"): ("va", "V"), (1, "Roi dây leo"): ("roi", "V"), (1, "Lá dao"): ("la", "V"),
    (1, "Bom bùn"): ("doc", "D"), (1, "Tự vệ"): ("khien", "T"), (1, "Sinh trưởng"): ("tang", "T"),
    (1, "Bom hạt"): ("hat", "V"), (1, "Bão lá"): ("bao_la", "D"), (1, "Tia mặt trời"): ("tia_mat_troi", "D"),
    (2, "Húc"): ("va", "V"), (2, "Tấn công nhanh"): ("nhanh", "V"), (2, "Cắn"): ("can", "V"),
    (2, "Sao băng"): ("sao", "D"), (2, "Mong ước"): ("lap_lanh", "T"), (2, "Nghỉ ngơi"): ("ngu", "T"),
    (2, "Phước lành hồi sinh"): ("hoi", "T"), (2, "Chia sẻ nỗi đau"): ("ma", "T"), (2, "Đuôi sắt"): ("thep", "V"),
    (2, "Bứt phá cuối cùng"): ("no", "V"), (2, "Tia phá hủy"): ("tia", "D"),
    (3, "Húc"): ("va", "V"), (3, "Súng nước"): ("tia_nuoc", "D"), (3, "Bong bóng"): ("bong_bong", "D"),
    (3, "Xoáy nước"): ("loc", "D"), (3, "Phá mai"): ("pha_mai", "T"), (3, "Rút vào mai"): ("mai", "T"),
    (3, "Đuôi nước"): ("song", "V"), (3, "Bơm nước"): ("bom_nuoc", "D"), (3, "Đại pháo nước"): ("tia", "D"),
    (4, "Tia lửa điện"): ("tia_lua_dien", "D"), (4, "Sóng điện"): ("song_dien", "D"), (4, "Cầu điện"): ("cau", "D"),
    (4, "Tia chớp"): ("set", "D"), (4, "Nhanh nhẹn"): ("nhanh", "T"), (4, "Nạp điện"): ("nap", "T"),
    (4, "Lưới điện"): ("luoi", "D"), (4, "Sấm sét"): ("sam", "D"), (4, "Đâm sầm điện"): ("dam_sam", "V"),
}


# ---------------------------------------------------------------- công cụ vẽ (toạ độ 0..1 trong ô hình)
class Ve:
    def __init__(self, s):
        self.s = s
        self.m = Image.new("L", (s, s), 0)
        self.d = ImageDraw.Draw(self.m)

    def p(self, x, y):
        return (x * self.s, y * self.s)

    def da_giac(self, pts, v=255):
        self.d.polygon([self.p(x, y) for x, y in pts], fill=v)

    def tron(self, cx, cy, r, v=255, ry=None):
        ry = r if ry is None else ry
        s = self.s
        self.d.ellipse([(cx - r) * s, (cy - ry) * s, (cx + r) * s, (cy + ry) * s], fill=v)

    def vong(self, cx, cy, r, w, v=255):
        s = self.s
        self.d.ellipse([(cx - r) * s, (cy - r) * s, (cx + r) * s, (cy + r) * s], outline=v, width=max(1, int(w * s)))

    def net(self, pts, w, v=255):
        """Nét dày đầu tròn qua các điểm."""
        s = self.s
        q = [self.p(x, y) for x, y in pts]
        self.d.line(q, fill=v, width=max(1, int(w * s)), joint="curve")
        for x, y in q:
            r = w * s / 2
            self.d.ellipse([x - r, y - r, x + r, y + r], fill=v)

    def cung(self, cx, cy, r, a0, a1, w, v=255, n=40):
        pts = [(cx + r * math.cos(math.radians(a0 + (a1 - a0) * i / n)), cy + r * math.sin(math.radians(a0 + (a1 - a0) * i / n)))
               for i in range(n + 1)]
        self.net(pts, w, v)

    def thon(self, pts, w0, w1, v=255):
        """Nét thon (dày w0 -> w1) theo đường gấp khúc - vết cào, nhánh lá."""
        n = len(pts) - 1
        trai, phai = [], []
        for i, (x, y) in enumerate(pts):
            a = pts[max(0, i - 1)]
            b = pts[min(n, i + 1)]
            dx, dy = b[0] - a[0], b[1] - a[1]
            L = math.hypot(dx, dy) or 1
            nx, ny = -dy / L, dx / L
            w = (w0 + (w1 - w0) * i / n) / 2
            trai.append((x + nx * w, y + ny * w))
            phai.append((x - nx * w, y - ny * w))
        self.da_giac(trai + phai[::-1], v)

    def sao(self, cx, cy, r0, r1, n, xoay=-90, v=255):
        pts = []
        for i in range(2 * n):
            r = r0 if i % 2 == 0 else r1
            a = math.radians(xoay + 180.0 * i / n)
            pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
        self.da_giac(pts, v)

    def cong_thuc(self, f, t0, t1, w0, w1, n=48, v=255):
        self.thon([f(t0 + (t1 - t0) * i / n) for i in range(n + 1)], w0, w1, v)


LUA = [(0.5, 0.98), (0.3, 0.93), (0.16, 0.8), (0.12, 0.6), (0.18, 0.4), (0.27, 0.52), (0.3, 0.32), (0.4, 0.14),
       (0.47, 0.32), (0.56, 0.0), (0.66, 0.2), (0.72, 0.4), (0.79, 0.28), (0.87, 0.46), (0.88, 0.68), (0.82, 0.85),
       (0.68, 0.95)]


def muot(pts, lan=3):
    """Làm mượt đa giác kín (Chaikin)."""
    for _ in range(lan):
        q = []
        for i in range(len(pts)):
            a, b = pts[i], pts[(i + 1) % len(pts)]
            q.append((a[0] * 0.75 + b[0] * 0.25, a[1] * 0.75 + b[1] * 0.25))
            q.append((a[0] * 0.25 + b[0] * 0.75, a[1] * 0.25 + b[1] * 0.75))
        pts = q
    return pts


def lua(v, cx, cy, h, w=None, trong=True, goc=0.0):
    """Ngọn lửa (đáy tại (cx, cy), cao h), quay goc độ (0 = hướng lên, 90 = hướng phải); trong = lõi rỗng."""
    w = h if w is None else w
    a = math.radians(goc)

    def dat(pts, k, ox, oy):
        r = []
        for x, y in pts:
            dx, dy = (x - 0.5) * w * k, (y - 0.98) * h * k
            r.append((cx + ox + dx * math.cos(a) - dy * math.sin(a), cy + oy + dx * math.sin(a) + dy * math.cos(a)))
        return r
    v.da_giac(dat(muot(LUA), 1.0, 0, 0))
    if trong:
        lo = (-0.1 * h * math.sin(a), 0.1 * h * math.cos(a))
        v.da_giac(dat(muot(LUA), 0.46, -lo[0] * 0.2 - 0.12 * h * math.sin(a) * 0, -0.06 * h * math.cos(a)), 0)


# ---------------------------------------------------------------- họ hình (vẽ vào mặt nạ trắng)
def h_va(v):  # va chạm: chớp nổ + vệt tốc độ
    v.sao(0.58, 0.5, 0.36, 0.17, 8, -78)
    v.sao(0.58, 0.5, 0.2, 0.1, 8, -100, v=0)
    for i, y in enumerate((0.34, 0.5, 0.66)):
        v.net([(0.06, y), (0.2 - 0.04 * (i % 2), y)], 0.07)


def h_cao(v):  # cào: 3 vết thon cong
    for i in range(3):
        o = (i - 1) * 0.2
        v.cong_thuc(lambda t, o=o: (0.24 + o + 0.48 * t + 0.05 * math.sin(t * math.pi), 0.14 + 0.72 * t - 0.06 * math.sin(t * math.pi)),
                    0, 1, 0.03, 0.15)


def h_rong(v):  # vuốt rồng: 3 móng câu
    for i in range(3):
        o = (i - 1) * 0.21
        v.cong_thuc(lambda t, o=o: (0.5 + o + 0.16 * math.cos(t * 2.4) - 0.1, 0.84 - 0.66 * t + 0.0),
                    0, 1, 0.17, 0.02)
        v.cong_thuc(lambda t, o=o: (0.5 + o + 0.16 * math.cos(2.4) - 0.1 - 0.12 * t, 0.18 - 0.06 * math.sin(t * 2.6)), 0, 1, 0.04, 0.0)


def h_can(v):  # cắn: hai hàm răng nanh
    v.cung(0.5, 0.52, 0.34, 200, 340, 0.1)
    v.cung(0.5, 0.48, 0.34, 20, 160, 0.1)
    for x in (0.32, 0.5, 0.68):
        v.da_giac([(x - 0.08, 0.36), (x + 0.08, 0.36), (x, 0.52)])
        v.da_giac([(x - 0.08, 0.64), (x + 0.08, 0.64), (x, 0.48)])
    v.da_giac([(0.08, 0.36), (0.24, 0.38), (0.12, 0.5)])
    v.da_giac([(0.92, 0.36), (0.76, 0.38), (0.88, 0.5)])


def h_la(v, cx=0.5, cy=0.5, k=1.0, xoay=-40):
    a = math.radians(xoay)
    pts = []
    for i in range(41):
        t = i / 40.0
        x = (t - 0.5) * 0.9 * k
        y = 0.22 * k * math.sin(t * math.pi) ** 0.9
        pts.append((x, y))
    pts += [(x, -y) for x, y in pts[::-1]]
    rot = [(cx + x * math.cos(a) - y * math.sin(a), cy + x * math.sin(a) + y * math.cos(a)) for x, y in pts]
    v.da_giac(rot)
    g = [(cx + x * math.cos(a), cy + x * math.sin(a)) for x in (-0.38 * k, 0.33 * k)]
    v.net(g, 0.035 * k, v=0)
    # cuống
    e = (cx - 0.45 * k * math.cos(a), cy - 0.45 * k * math.sin(a))
    v.net([e, (e[0] - 0.08 * k * math.cos(a), e[1] - 0.08 * k * math.sin(a))], 0.05 * k)


def h_la_dao(v):
    h_la(v, 0.5, 0.5, 1.05, -40)


def h_roi(v):  # roi dây leo: chữ S + lá nhỏ
    v.cong_thuc(lambda t: (0.14 + 0.72 * t, 0.62 + 0.22 * math.sin(t * math.pi * 2.0 + 0.6) - 0.24 * t), 0, 1, 0.12, 0.05)
    h_la(v, 0.34, 0.3, 0.42, -70)
    h_la(v, 0.66, 0.78, 0.38, 20)


def h_hat(v):  # bom hạt: 3 hạt + tia nổ
    for cx, cy, r in ((0.36, 0.62, 0.15), (0.64, 0.6, 0.13), (0.5, 0.34, 0.14)):
        v.tron(cx, cy, r * 0.82, 255, r)
        v.tron(cx - r * 0.25, cy - r * 0.35, r * 0.22, 0)
    for a in (-150, -30, 90, 200, -90):
        x, y = 0.5 + 0.42 * math.cos(math.radians(a)), 0.5 + 0.42 * math.sin(math.radians(a))
        x2, y2 = 0.5 + 0.48 * math.cos(math.radians(a)), 0.5 + 0.48 * math.sin(math.radians(a))
        v.net([(x, y), (x2, y2)], 0.05)


def h_bao_la(v):  # bão lá: xoáy + 3 lá
    v.cung(0.5, 0.5, 0.36, 200, 470, 0.07)
    v.cung(0.5, 0.5, 0.2, 30, 300, 0.06)
    h_la(v, 0.24, 0.3, 0.36, -20)
    h_la(v, 0.76, 0.42, 0.34, 60)
    h_la(v, 0.46, 0.8, 0.34, 170)


def h_giot(v, cx, cy, r):
    pts = []
    for i in range(41):
        a = math.pi * 2 * i / 40
        x = cx + r * math.sin(a) * (1 - 0.0)
        y = cy + r * math.cos(a) * 0.95
        if math.cos(a) < 0:
            y = cy - r * 0.95 - (r * 1.1) * (-math.cos(a)) ** 3 * 1.0 + r * 0.95 * (1 + math.cos(a))
            x = cx + r * math.sin(a) * (1 + math.cos(a)) ** 0.8
        pts.append((x, y))
    v.da_giac(pts)


def h_tia_nuoc(v):  # súng nước: giọt đầu + vệt
    h_giot(v, 0.66, 0.52, 0.2)
    v.tron(0.6, 0.55, 0.06, 0)
    for i, (y, l) in enumerate(((0.36, 0.3), (0.52, 0.42), (0.68, 0.32))):
        v.thon([(0.08 + 0.42 - l, y), (0.46, y)], 0.03, 0.09)


def h_bom_nuoc(v):  # bơm nước: hai luồng nước lớn
    for y in (0.36, 0.64):
        v.thon([(0.08, y + 0.06), (0.4, y), (0.78, y - 0.02)], 0.06, 0.2)
        v.tron(0.82, y - 0.02, 0.1)
    v.tron(0.9, 0.2, 0.04)
    v.tron(0.9, 0.82, 0.05)


def h_bong_bong(v):
    for cx, cy, r in ((0.38, 0.6, 0.24), (0.7, 0.34, 0.16), (0.74, 0.74, 0.1)):
        v.vong(cx, cy, r, 0.07)
        v.cung(cx, cy, r * 0.62, 200, 260, 0.05)


def h_song(v):  # sóng (đuôi nước): hai ngọn sóng cuộn
    for oy, k in ((0.0, 1.0), (0.24, 0.8)):
        v.cong_thuc(lambda t, oy=oy: (0.1 + 0.8 * t, 0.5 + oy + 0.1 * math.sin(t * math.pi * 3.0)), 0, 1, 0.09 * k, 0.09 * k)
    v.cung(0.4, 0.36, 0.16, 180, 400, 0.1)
    v.tron(0.5, 0.3, 0.05, 0)


def h_tan_lua(v):  # than hồng: lửa nhỏ + tàn
    lua(v, 0.44, 0.92, 0.72, 0.62)
    v.tron(0.84, 0.26, 0.06)
    v.tron(0.86, 0.56, 0.045)
    v.tron(0.12, 0.3, 0.045)


def h_lua_lon(v):  # súng phun lửa: luồng lửa phun ngang
    v.thon([(0.04, 0.6), (0.4, 0.54)], 0.06, 0.2)
    lua(v, 0.32, 0.54, 0.62, 0.6, True, 90)
    v.tron(0.8, 0.2, 0.045)
    v.tron(0.86, 0.84, 0.04)


def h_dai_hoa(v):  # đại hỏa: chữ 大 bằng lửa
    v.net([(0.5, 0.12), (0.5, 0.44)], 0.12)
    v.net([(0.14, 0.4), (0.86, 0.4)], 0.12)
    v.thon([(0.5, 0.44), (0.32, 0.68), (0.16, 0.86)], 0.12, 0.16)
    v.thon([(0.5, 0.44), (0.68, 0.68), (0.84, 0.86)], 0.12, 0.16)
    for x, y in ((0.5, 0.12), (0.14, 0.4), (0.86, 0.4)):
        lua(v, x, y + 0.03, 0.2, 0.18, False)


def h_bolt(v, pts):
    v.da_giac(pts)


SET = [(0.58, 0.06), (0.24, 0.52), (0.46, 0.52), (0.36, 0.94), (0.78, 0.4), (0.55, 0.4), (0.7, 0.06)]


def h_set(v):  # tia chớp
    h_bolt(v, SET)


def h_tia_lua_dien(v):  # tia lửa điện: sét nhỏ + tia toả
    k = 0.72
    h_bolt(v, [(0.5 + (x - 0.5) * k, 0.5 + (y - 0.5) * k) for x, y in SET])
    for a in (-160, -110, -20, 30, 150, 200):
        r0, r1 = 0.36, 0.46
        c, s = math.cos(math.radians(a)), math.sin(math.radians(a))
        v.net([(0.5 + r0 * c, 0.5 + r0 * s), (0.5 + r1 * c, 0.5 + r1 * s)], 0.06)


def h_song_dien(v):  # sóng điện: sét + vòng sóng
    k = 0.55
    h_bolt(v, [(0.32 + (x - 0.5) * k, 0.5 + (y - 0.5) * k) for x, y in SET])
    for r in (0.24, 0.38):
        v.cung(0.34, 0.5, r, -50, 50, 0.065)
    v.cung(0.34, 0.5, 0.52, -40, 40, 0.06)


def h_cau(v):  # cầu điện: quả cầu + sét
    v.tron(0.5, 0.5, 0.34)
    v.tron(0.5, 0.5, 0.25, 0)
    k = 0.45
    h_bolt(v, [(0.5 + (x - 0.5) * k, 0.5 + (y - 0.5) * k) for x, y in SET])
    v.cung(0.5, 0.5, 0.44, 200, 250, 0.05)
    v.cung(0.5, 0.5, 0.44, 20, 70, 0.05)


def h_sam(v):  # sấm sét: mây + sét
    for cx, cy, r in ((0.32, 0.36, 0.16), (0.52, 0.27, 0.2), (0.72, 0.38, 0.15)):
        v.tron(cx, cy, r)
    v.d.rectangle([0.2 * v.s, 0.36 * v.s, 0.82 * v.s, 0.52 * v.s], fill=255)
    v.tron(0.2, 0.44, 0.08)
    v.tron(0.82, 0.44, 0.08)
    k = 0.48
    h_bolt(v, [(0.52 + (x - 0.5) * k, 0.72 + (y - 0.5) * k) for x, y in SET])
    v.net([(0.24, 0.52), (0.8, 0.52)], 0.04, v=0)


def h_nap(v):  # nạp điện: pin + sét
    v.d.rounded_rectangle([0.2 * v.s, 0.28 * v.s, 0.78 * v.s, 0.72 * v.s], radius=0.08 * v.s, fill=255)
    v.d.rectangle([0.78 * v.s, 0.42 * v.s, 0.86 * v.s, 0.58 * v.s], fill=255)
    v.d.rounded_rectangle([0.27 * v.s, 0.35 * v.s, 0.71 * v.s, 0.65 * v.s], radius=0.04 * v.s, fill=0)
    k = 0.4
    h_bolt(v, [(0.49 + (x - 0.5) * k, 0.5 + (y - 0.5) * k) for x, y in SET])


def h_luoi(v):  # lưới điện
    v.vong(0.5, 0.5, 0.36, 0.06)
    for d in (-0.18, 0.0, 0.18):
        v.net([(0.5 + d - 0.26, 0.24), (0.5 + d + 0.26, 0.76)], 0.05)
        v.net([(0.5 + d + 0.26, 0.24), (0.5 + d - 0.26, 0.76)], 0.05)
    m = Image.new("L", (v.s, v.s), 0)
    ImageDraw.Draw(m).ellipse([0.12 * v.s, 0.12 * v.s, 0.88 * v.s, 0.88 * v.s], fill=255)
    v.m = ImageChops.multiply(v.m, m)
    v.d = ImageDraw.Draw(v.m)
    v.vong(0.5, 0.5, 0.36, 0.06)
    k = 0.3
    h_bolt(v, [(0.8 + (x - 0.5) * k, 0.2 + (y - 0.5) * k) for x, y in SET])


def h_dam_sam(v):  # đâm sầm điện: sét + vệt lao
    k = 0.8
    h_bolt(v, [(0.58 + (x - 0.5) * k, 0.5 + (y - 0.5) * k) for x, y in SET])
    for y, l in ((0.3, 0.2), (0.5, 0.28), (0.7, 0.18)):
        v.net([(0.06, y), (0.06 + l, y)], 0.07)


def h_loc(v):  # xoáy (lửa / nước): phễu lốc từ các vòng elip
    for i, (y, r) in enumerate(((0.18, 0.4), (0.34, 0.31), (0.5, 0.23), (0.65, 0.15), (0.79, 0.09))):
        x = 0.5 + 0.06 * math.sin(i * 1.4)
        s = v.s
        v.d.ellipse([(x - r) * s, (y - r * 0.32) * s, (x + r) * s, (y + r * 0.32) * s], outline=255, width=int(0.075 * s))
    v.net([(0.5, 0.79), (0.47, 0.92)], 0.06)


def h_khoi(v):  # màn khói: đám mây khói
    for cx, cy, r in ((0.3, 0.58, 0.17), (0.5, 0.42, 0.22), (0.72, 0.56, 0.18), (0.5, 0.66, 0.16)):
        v.tron(cx, cy, r)
    for cx, cy, r in ((0.5, 0.42, 0.12), (0.3, 0.58, 0.08)):
        v.cung(cx, cy, r, 200, 290, 0.04, v=0)
    v.tron(0.2, 0.3, 0.06)
    v.tron(0.82, 0.28, 0.05)


def h_mua_kiem(v):  # múa kiếm: hai kiếm chéo + mũi tên lên
    for s in (1, -1):
        x0, x1 = 0.5 - 0.3 * s, 0.5 + 0.28 * s
        v.thon([(x0, 0.8), (x1, 0.14)], 0.1, 0.02)
        v.net([(x0 + 0.02 * s - 0.1, 0.66), (x0 + 0.02 * s + 0.1, 0.74)], 0.05) if s == 1 else \
            v.net([(x0 + 0.1, 0.66), (x0 - 0.1, 0.74)], 0.05)
        v.tron(x0 - 0.03 * s, 0.85, 0.045)


def h_tang(v):  # sinh trưởng / tăng: mầm + mũi tên kép
    v.thon([(0.36, 0.86), (0.36, 0.5)], 0.08, 0.06)
    h_la(v, 0.24, 0.48, 0.34, -140)
    h_la(v, 0.5, 0.42, 0.36, -40)
    for y in (0.38, 0.58):
        v.net([(0.62, y), (0.76, y - 0.14), (0.9, y)], 0.08)


def khien_hinh(v, val=255, co=1.0, cy=0.5):
    pts = []
    for i in range(41):
        t = i / 40.0
        pts.append((0.5 + (t - 0.5) * 0.66 * co, cy - 0.34 * co + 0.06 * co * math.sin(t * math.pi)))
    for i in range(41):
        t = i / 40.0
        a = math.pi * t
        pts.append((0.5 + 0.33 * co * math.cos(a), cy - 0.28 * co + 0.62 * co * math.sin(a) ** 1.4))
    v.da_giac(pts, val)


def h_khien(v):  # tự vệ: khiên + dấu chéo
    khien_hinh(v)
    khien_hinh(v, 0, 0.72, 0.5)
    khien_hinh(v, 255, 0.52, 0.5)
    v.net([(0.5, 0.32), (0.5, 0.68)], 0.06, v=0)


def h_mai(v):  # rút vào mai: mai rùa lục giác
    v.tron(0.5, 0.5, 0.38, 255, 0.33)
    for i in range(6):
        a = math.radians(60 * i)
        v.net([(0.5 + 0.13 * math.cos(a), 0.5 + 0.13 * math.sin(a)), (0.5 + 0.13 * math.cos(a + 1.047), 0.5 + 0.13 * math.sin(a + 1.047))], 0.04, v=0)
        v.net([(0.5 + 0.13 * math.cos(a), 0.5 + 0.13 * math.sin(a)), (0.5 + 0.3 * math.cos(a), 0.5 + 0.26 * math.sin(a))], 0.04, v=0)


def h_pha_mai(v):  # phá mai: mai nứt + mảnh văng
    h_mai(v)
    v.net([(0.46, 0.1), (0.56, 0.32), (0.42, 0.48), (0.58, 0.66), (0.48, 0.92)], 0.07, v=0)
    v.da_giac([(0.82, 0.12), (0.94, 0.2), (0.86, 0.28)])
    v.da_giac([(0.1, 0.78), (0.2, 0.86), (0.08, 0.9)])


def h_tia(v):  # tia (phá huỷ / pháo): luồng tia rộng + quả cầu
    v.thon([(0.2, 0.5), (0.92, 0.5)], 0.22, 0.4)
    v.thon([(0.2, 0.5), (0.92, 0.5)], 0.08, 0.16, v=0)
    v.tron(0.22, 0.5, 0.16)
    for a in (120, 180, 240):
        c, s = math.cos(math.radians(a)), math.sin(math.radians(a))
        v.net([(0.22 + 0.22 * c, 0.5 + 0.22 * s), (0.22 + 0.27 * c, 0.5 + 0.27 * s)], 0.05)


def h_tia_mat_troi(v):  # tia mặt trời: mặt trời + tia
    v.tron(0.34, 0.4, 0.17)
    for i in range(8):
        a = math.radians(45 * i)
        v.net([(0.34 + 0.24 * math.cos(a), 0.4 + 0.24 * math.sin(a)), (0.34 + 0.31 * math.cos(a), 0.4 + 0.31 * math.sin(a))], 0.055)
    v.thon([(0.46, 0.56), (0.9, 0.9)], 0.08, 0.2)


def h_nhanh(v):  # nhanh (tấn công nhanh / nhanh nhẹn): mũi tên kép + vệt
    for x in (0.38, 0.64):
        v.net([(x - 0.14, 0.24), (x + 0.1, 0.5), (x - 0.14, 0.76)], 0.11)
    for y, l in ((0.3, 0.12), (0.7, 0.12)):
        v.net([(0.04, y), (0.04 + l, y)], 0.06)


def h_sao(v):  # sao băng: sao 5 cánh + đuôi
    v.sao(0.62, 0.4, 0.3, 0.13, 5, -90)
    for y0, l in ((0.62, 0.36), (0.78, 0.26)):
        v.thon([(0.42 - l, y0 + l * 0.6), (0.46, y0 - 0.04)], 0.02, 0.08)


def h_lap_lanh(v):  # mong ước: sao lấp lánh 4 cánh (tiên)
    v.sao(0.44, 0.52, 0.36, 0.08, 4, -90)
    v.sao(0.78, 0.24, 0.15, 0.04, 4, -90)
    v.sao(0.78, 0.78, 0.1, 0.03, 4, -90)


def h_ngu(v):  # nghỉ ngơi: Z z
    for x, y, k in ((0.32, 0.6, 0.36), (0.66, 0.36, 0.24), (0.84, 0.16, 0.14)):
        w = 0.12 * k / 0.36
        v.net([(x - k / 2, y - k / 2), (x + k / 2, y - k / 2), (x - k / 2, y + k / 2), (x + k / 2, y + k / 2)], w)


def h_hoi(v):  # hồi sinh / hồi máu: tim + dấu cộng
    pts = []
    for i in range(61):
        t = math.pi * 2 * i / 60
        x = 16 * math.sin(t) ** 3
        y = 13 * math.cos(t) - 5 * math.cos(2 * t) - 2 * math.cos(3 * t) - math.cos(4 * t)
        pts.append((0.5 + x * 0.024, 0.48 - y * 0.024))
    v.da_giac(pts)
    v.net([(0.5, 0.32), (0.5, 0.6)], 0.08, v=0)
    v.net([(0.36, 0.46), (0.64, 0.46)], 0.08, v=0)


def h_ma(v):  # bóng ma (Ma): thân ma + mắt
    pts = [(0.5 + 0.3 * math.cos(math.radians(a)), 0.44 + 0.3 * math.sin(math.radians(a))) for a in range(180, 361, 6)]
    pts += [(0.8, 0.82)]
    for i in range(7):
        x = 0.8 - 0.6 * (i + 1) / 7
        pts.append((x + 0.04, 0.72 if i % 2 == 0 else 0.86))
    pts += [(0.2, 0.86)]
    v.da_giac(pts)
    v.tron(0.4, 0.44, 0.06, 0, 0.08)
    v.tron(0.6, 0.44, 0.06, 0, 0.08)
    v.tron(0.5, 0.62, 0.05, 0, 0.035)


def h_doc(v):  # bom bùn (Độc): khối bùn + bọt + giọt
    v.tron(0.5, 0.58, 0.32, 255, 0.24)
    v.tron(0.36, 0.38, 0.12)
    v.tron(0.6, 0.32, 0.14)
    v.tron(0.42, 0.52, 0.07, 0)
    v.tron(0.64, 0.6, 0.05, 0)
    v.tron(0.3, 0.88, 0.05)
    v.tron(0.7, 0.9, 0.04)
    v.vong(0.82, 0.22, 0.07, 0.04)


def h_thep(v):  # đuôi sắt (Thép): đai ốc lục giác + vệt đuôi
    pts = [(0.56 + 0.3 * math.cos(math.radians(60 * i + 30)), 0.44 + 0.3 * math.sin(math.radians(60 * i + 30))) for i in range(6)]
    v.da_giac(pts)
    v.tron(0.56, 0.44, 0.12, 0)
    v.cung(0.56, 0.44, 0.42, 110, 200, 0.07)


def h_no(v):  # bứt phá cuối cùng: vụ nổ lớn
    v.sao(0.5, 0.5, 0.46, 0.26, 12, -90)
    v.sao(0.5, 0.5, 0.26, 0.14, 8, -70, v=0)
    v.tron(0.5, 0.5, 0.09)


# (các họ khác trong danh mục, chưa có chiêu dùng - giữ sẵn cho chiêu mới)
def h_bang(v):
    for a in (0, 60, 120):
        c, s = math.cos(math.radians(a)), math.sin(math.radians(a))
        v.thon([(0.5 - 0.38 * c, 0.5 - 0.38 * s), (0.5, 0.5), (0.5 + 0.38 * c, 0.5 + 0.38 * s)], 0.06, 0.06)
    v.sao(0.5, 0.5, 0.14, 0.06, 6)


def h_da(v):
    v.da_giac([(0.2, 0.7), (0.3, 0.38), (0.56, 0.3), (0.74, 0.5), (0.66, 0.82), (0.34, 0.86)])
    v.net([(0.32, 0.5), (0.5, 0.6)], 0.04, v=0)
    v.da_giac([(0.7, 0.14), (0.86, 0.2), (0.8, 0.34), (0.66, 0.28)])


def h_dat(v):
    v.da_giac([(0.08, 0.7), (0.92, 0.7), (0.92, 0.84), (0.08, 0.84)])
    v.net([(0.2, 0.62), (0.34, 0.4), (0.46, 0.58), (0.62, 0.3), (0.78, 0.56)], 0.07)


def h_sieu_linh(v):
    for r in (0.12, 0.25, 0.38):
        v.vong(0.5, 0.5, r, 0.055)


def h_am(v):
    v.da_giac([(0.16, 0.4), (0.32, 0.4), (0.5, 0.24), (0.5, 0.76), (0.32, 0.6), (0.16, 0.6)])
    for r in (0.16, 0.28):
        v.cung(0.54, 0.5, r, -45, 45, 0.06)


HO = {
    "va": h_va, "cao": h_cao, "can": h_can, "la": h_la_dao, "roi": h_roi, "hat": h_hat, "tia_nuoc": h_tia_nuoc,
    "song": h_song, "bong_bong": h_bong_bong, "tan_lua": h_tan_lua, "lua_lon": h_lua_lon, "dai_hoa": h_dai_hoa,
    "set": h_set, "tia_lua_dien": h_tia_lua_dien, "song_dien": h_song_dien, "cau": h_cau, "sam": h_sam, "nap": h_nap,
    "luoi": h_luoi, "dam_sam": h_dam_sam, "bang": h_bang, "da": h_da, "dat": h_dat, "loc": h_loc, "sieu_linh": h_sieu_linh,
    "ma": h_ma, "doc": h_doc, "am": h_am, "tang": h_tang, "mua_kiem": h_mua_kiem, "khien": h_khien, "mai": h_mai,
    "pha_mai": h_pha_mai, "hoi": h_hoi, "ngu": h_ngu, "khoi": h_khoi, "tia": h_tia, "tia_mat_troi": h_tia_mat_troi,
    "rong": h_rong, "lap_lanh": h_lap_lanh, "thep": h_thep, "sao": h_sao, "nhanh": h_nhanh, "no": h_no,
    "bao_la": h_bao_la, "bom_nuoc": h_bom_nuoc,
}


# ---------------------------------------------------------------- huy hiệu
def rgb(c):
    return ((c >> 16) & 255, (c >> 8) & 255, c & 255)


def pha(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t + 0.5) for i in range(3))


def mat_na_khoi(tron, lo=0.0):
    m = Image.new("L", (N, N), 0)
    d = ImageDraw.Draw(m)
    a, b = (0.035 + lo) * N, (1 - 0.035 - lo) * N
    if tron:
        d.ellipse([a, a, b, b], fill=255)
    else:
        d.rounded_rectangle([a, a, b, b], radius=(0.22 - lo) * N, fill=255)
    return m


def doc_sang(mau, toi):
    """Dải dọc sáng -> màu -> tối."""
    tren, giua, duoi = pha(mau, (255, 255, 255), 0.38), mau, pha(mau, (0, 0, 0), 0.3)
    col = Image.new("RGB", (1, N))
    for y in range(N):
        t = y / (N - 1)
        c = pha(tren, giua, t / 0.5) if t < 0.5 else pha(giua, duoi, (t - 0.5) / 0.5)
        col.putpixel((0, y), c)
    return col.resize((N, N))


def huy_hieu(he, ho, loai, tron):
    mau = rgb(MAU_HE[he])
    sang = sum(mau[i] * w for i, w in enumerate((0.3, 0.59, 0.11)))
    toi = pha(mau, (0, 0, 0), 0.58)
    anh = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    # bóng đổ mềm
    bong = mat_na_khoi(tron).filter(ImageFilter.GaussianBlur(N * 0.012))
    bong = Image.eval(bong, lambda p: p * 70 // 255)
    anh.paste((20, 24, 40, 255), (0, int(N * 0.012)), bong)
    # viền tối
    anh.paste(toi + (255,), (0, 0), mat_na_khoi(tron))
    # thân dải màu
    than = mat_na_khoi(tron, 0.03)
    anh.paste(doc_sang(mau, toi), (0, 0), than)
    # viền sáng trong
    vt = ImageChops.subtract(than, mat_na_khoi(tron, 0.045))
    anh.paste(pha(mau, (255, 255, 255), 0.55) + (255,), (0, 0), Image.eval(vt, lambda p: p * 150 // 255))
    # ánh trên (elip mờ cắt theo thân)
    an = Image.new("L", (N, N), 0)
    ImageDraw.Draw(an).ellipse([0.1 * N, -0.32 * N, 0.9 * N, 0.44 * N], fill=255)
    an = ImageChops.multiply(an.filter(ImageFilter.GaussianBlur(N * 0.02)), than)
    anh.paste((255, 255, 255, 255), (0, 0), Image.eval(an, lambda p: p * 62 // 255))
    # hình chiêu: vẽ trong hộp 0.62 N, đặt giữa (tròn: lệch lên để chừa nhãn)
    k = int(N * (0.5 if tron else 0.62))
    v = Ve(k)
    HO[ho](v)
    hm = v.m
    ox = (N - k) // 2
    oy = int(N * (0.14 if tron else 0.19))
    mk = Image.new("L", (N, N), 0)
    mk.paste(hm, (ox, oy))
    # viền tối quanh hình + bóng nhỏ
    nhat = sang > 165
    vien = mk.filter(ImageFilter.MaxFilter(max(3, (int(N * (0.04 if nhat else 0.034)) // 2) * 2 + 1)))
    vien = vien.filter(ImageFilter.GaussianBlur(N * 0.004))
    vien = ImageChops.multiply(vien, than)
    anh.paste(pha(mau, (0, 0, 0), 0.72) + (255,), (0, int(N * 0.01)), Image.eval(vien, lambda p: p * 200 // 255))
    anh.paste(pha(mau, (0, 0, 0), 0.7 if nhat else 0.55) + (255,), (0, 0), Image.eval(vien, lambda p: p * (235 if nhat else 190) // 255))
    # hình trắng có ánh nhẹ phía dưới
    trang = Image.new("RGB", (N, N), (255, 255, 255))
    dd = ImageDraw.Draw(trang)
    for y in range(N):
        t = max(0.0, (y - oy) / float(k))
        c = pha((255, 255, 255), pha(mau, (255, 255, 255), 0.72), min(1.0, t) ** 1.6)
        dd.line([(0, y), (N, y)], fill=c)
    anh.paste(trang, (0, 0), mk)
    if tron:   # nút HUD: có nhãn tên bên dưới, không dấu loại (gợi ý khi giữ nút ghi đủ)
        return anh
    # dấu loại góc phải TRÊN (góc phải dưới dành cho số phím / số lượng của ô)
    r = N * 0.12
    cx, cy = N * 0.8, N * 0.2
    d = ImageDraw.Draw(anh)
    if loai == "V":
        c1, c2 = (240, 112, 40), (150, 52, 14)
    elif loai == "D":
        c1, c2 = (124, 92, 230), (58, 38, 132)
    else:
        c1, c2 = (150, 158, 172), (70, 76, 92)
    d.ellipse([cx - r - N * 0.012, cy - r - N * 0.012, cx + r + N * 0.012, cy + r + N * 0.012], fill=(255, 255, 255, 255))
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=c2 + (255,))
    d.ellipse([cx - r * 0.84, cy - r * 0.84, cx + r * 0.84, cy + r * 0.84], fill=c1 + (255,))
    if loai == "V":
        pts = []
        for i in range(8):
            rr = r * (0.66 if i % 2 == 0 else 0.26)
            a = math.radians(-90 + 45 * i)
            pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
        d.polygon(pts, fill=(255, 255, 255, 255))
    elif loai == "D":
        w = max(1, int(r * 0.2))
        d.ellipse([cx - r * 0.52, cy - r * 0.52, cx + r * 0.52, cy + r * 0.52], outline=(255, 255, 255, 255), width=w)
        d.ellipse([cx - r * 0.16, cy - r * 0.16, cx + r * 0.16, cy + r * 0.16], fill=(255, 255, 255, 255))
    else:
        d.pieslice([cx - r * 0.56, cy - r * 0.56, cx + r * 0.56, cy + r * 0.56], 90, 270, fill=(255, 255, 255, 255))
        d.ellipse([cx - r * 0.56, cy - r * 0.56, cx + r * 0.56, cy + r * 0.56], outline=(255, 255, 255, 255), width=max(1, int(r * 0.14)))
    return anh


def luu(anh, duong):
    """PNG bảng màu 256 + alpha (FASTOCTREE + khuếch tán): nhỏ ~5 lần, khác biệt không thấy ở cỡ hiển thị."""
    anh.quantize(256, method=Image.FASTOCTREE, dither=Image.FLOYDSTEINBERG).save(duong, optimize=True)


def thu(anh, s):
    """Thu nhiều bước (mỗi bước /2) cho nét mịn."""
    while anh.width // 2 >= s * 2:
        anh = anh.resize((anh.width // 2, anh.height // 2), Image.LANCZOS)
    return anh.resize((s, s), Image.LANCZOS)


# ---------------------------------------------------------------- dữ liệu + kiểm
def doc_csv():
    ds = []
    with open(CSV, encoding="utf-8") as f:
        for r in csv.DictReader(f):
            ds.append((int(r["dong"]), int(r["so_hieu"]), r["ten"], r["he"], r["kieu"]))
    ds.sort(key=lambda x: (x[0], x[1]))
    return ds


def ten_java(duong, mau):
    s = open(duong, encoding="utf-8").read()
    m = re.search(mau, s, re.S)
    if not m:
        return None
    return [re.findall(r'"([^"]*)"', hang) for hang in re.findall(r"\{([^{}]*)\}", m.group(1))]


def kiem(ds):
    loi = []
    client = ten_java(os.path.join(GOC, "client_jar", "src", "game", "pokemon", "PokeChieu.java"),
                      r"String\[\]\[\] TEN = \{(.*?)\};")
    server = ten_java(os.path.join(GOC, "server", "src", "real", "MessageCreator.java"), r"SKILL_NAME = new String\[\]\[\]\{(.*?)\};")
    he_java = open(os.path.join(GOC, "client_jar", "src", "game", "pokemon", "PokeChieu.java"), encoding="utf-8").read()
    mh = re.search(r"byte\[\]\[\] HE = \{(.*?)\};", he_java, re.S)
    he_client = [[int(x) for x in re.findall(r"-?\d+", h)] for h in re.findall(r"\{([^{}]*)\}", mh.group(1))] if mh else None
    csv_ten = {}
    for dong, so, ten, he, _ in ds:
        csv_ten.setdefault(dong, []).append(ten)
        if (dong, ten) not in CHIEU:
            loi.append("chiêu chưa có icon (họ hình): dòng %d #%d %s" % (dong, so, ten))
        elif CHIEU[(dong, ten)][0] not in HO:
            loi.append("họ hình không tồn tại: %s" % CHIEU[(dong, ten)][0])
        if he not in TEN_HE:
            loi.append("hệ lạ: %s (%s)" % (he, ten))
        elif he_client and (dong >= len(he_client) or so >= len(he_client[dong]) or he_client[dong][so] != TEN_HE.index(he)):
            loi.append("hệ client khác CSV: dòng %d #%d %s" % (dong, so, ten))
    for nguon, bang in (("client PokeChieu.TEN", client), ("server SKILL_NAME", server)):
        if bang is None:
            loi.append("không đọc được " + nguon)
            continue
        for dong, hang in enumerate(bang):
            if hang != csv_ten.get(dong, []):
                loi.append("%s dòng %d khác CSV: %s / %s" % (nguon, dong, hang, csv_ten.get(dong)))
        if len(bang) != len(csv_ten):
            loi.append("%s: số dòng %d khác CSV %d" % (nguon, len(bang), len(csv_ten)))
    return loi


def main():
    ds = doc_csv()
    loi = kiem(ds)
    if loi:
        print("\n".join("LOI: " + l for l in loi))
        sys.exit(1)
    print("KIEM icon chieu: %d chieu, %d ho hinh, 0 loi" % (len(ds), len(set(CHIEU[(d, t)][0] for d, _, t, _, _ in ds))))
    if "--kiem" in sys.argv:
        return
    os.makedirs(RA, exist_ok=True)
    os.makedirs(RA_WEB, exist_ok=True)
    hang = (len(ds) + COT - 1) // COT
    for tron in (False, True):
        tam = {s: Image.new("RGBA", (COT * s, hang * s), (0, 0, 0, 0)) for s in CO}
        for i, (dong, so, ten, he, _) in enumerate(ds):
            ho, loai = CHIEU[(dong, ten)]
            a = huy_hieu(TEN_HE.index(he), ho, loai, tron)
            for s in CO:
                tam[s].paste(thu(a, s), ((i % COT) * s, (i // COT) * s))
            if not tron:
                luu(thu(a, 64), os.path.join(RA_WEB, "%d-%d.png" % (dong, so)))
        for s in CO:
            ten_tep = "chieu_%s%d.png" % ("tron_" if tron else "", s)
            luu(tam[s], os.path.join(RA, ten_tep))
            print("ghi", os.path.relpath(os.path.join(RA, ten_tep), GOC), tam[s].size)
    print("ghi", os.path.relpath(RA_WEB, GOC), "(%d icon 64 px)" % len(ds))


if __name__ == "__main__":
    main()
