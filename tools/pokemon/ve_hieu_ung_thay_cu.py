#!/usr/bin/env python3
"""
Ve lai cac hieu ung KIEM HIEP con lot (P2 muc 7): kiem bay, kiem roi, bang icon skill
co hinh vu khi, khung danh hieu chu kiem hiep, hao quang "khi" quanh nguoi.

Chu nhan dat: GIU NGUYEN hieu ung chien dau hien co, CHI thay cai ro rang kiem hiep. Moi tep
ve lai TAI CHO (cung ten, cung kich thuoc khung) nen khong can sua ma:

  client_jar/res/sword skill/kiem.png       28x84  (3 khung 28x28: 0, -30, -60 do)  -> manh bang
  server/map/imgArrow/7.png                 28x84  (anh 10007, ban server cua kiem.png) -> manh bang
  client_jar/res/sword skill/kiem01.png     12x78  (2 khung 12x39, kiem roi mui xuong) -> bang nhon
  client_jar/res/sword skill/all1.png       13x27  (kiem cam dat) -> cum bang
    + ban Unity client_pc_ios/Assets/Resources/res/sword skill/{kiem01,all1}.bytes (giong het jar)
  server/map/sk0..sk4.png                   20x(20n+1) icon skill theo dong (goi -8 loai 1, Unity
                                            ve o HudPokemon/BangAuto/ShopNew) -> o he Pokemon theo
                                            du_lieu/bang_chieu.csv (ten chieu moi, he)
  server/map/d_effect/<id>.png + <id>       khung danh hieu (dai 8700+id, ve qua Char.paintListEff
                                            ngay ca o duong ve Pokemon): ve lai CA anh LAN bang khung
                                            (dinh dang DataSkillEff.loadData) - bang ruy bang Poke
                                            Ball + ten danh hieu moi (data.DanhHieu.getName)
  server/map/d_effect/186..190.png          hao quang khi (ideff 8886..8890 cua ao Milotic/Minun/...)
                                            -> vang sang + hat nang len, ve TRONG tung o cu (bang khung
                                            giu nguyen)

Khong dung anh goc nao lam nguon: chay lai cho ra dung cac tep do (idempotent). Phong chu:
Noto Sans Bold (SIL OFL 1.1) cua he thong - chi de ve chu len anh, khong dong goi phong.

Chay o goc repo:  python3 tools/pokemon/ve_hieu_ung_thay_cu.py   (can Pillow; khong dung -I vi Pillow
nam o user site)
"""
import math
import os
import struct
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PHONG = "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf"
SS = 4  # ve phong to roi thu nho


def p(*a):
    return os.path.join(GOC, *a)


def luu(im, duong):
    """Ghi PNG; khong doi byte neu pixel nhu cu (de chay lai khong lam ban git)."""
    im = im.convert("RGBA")
    if os.path.isfile(duong):
        try:
            cu = Image.open(duong).convert("RGBA")
            if cu.size == im.size and cu.tobytes() == im.tobytes():
                return False
        except Exception:
            pass
    im.save(duong, optimize=True)
    return True


def thu_nho(big, w, h, cat_alpha=True):
    im = big.resize((w, h), Image.LANCZOS)
    if cat_alpha:
        px = im.load()
        for y in range(h):
            for x in range(w):
                r, g, b, a = px[x, y]
                if a < 40:
                    px[x, y] = (0, 0, 0, 0)
                elif a > 200:
                    px[x, y] = (r, g, b, 255)
    return im


# ----------------------------------------------------------------- manh bang (thay kiem)

BANG = [(18, 40, 96), (40, 110, 200), (110, 200, 245), (215, 248, 255), (255, 255, 255)]


def ve_manh(d, cx, cy, goc, dai, rong, s=SS):
    """Manh bang hinh thoi dai, mui theo huong goc (do, 0 = sang phai, am = len tren)."""
    a = math.radians(goc)
    ux, uy = math.cos(a), math.sin(a)
    vx, vy = -uy, ux
    cx, cy = cx * s, cy * s
    L, R = dai * s / 2.0, rong * s / 2.0

    def pt(t, q):
        return (cx + ux * t + vx * q, cy + uy * t + vy * q)

    vien = [pt(L, 0), pt(L * 0.1, R), pt(-L * 0.55, R * 0.7), pt(-L, 0), pt(-L * 0.55, -R * 0.7), pt(L * 0.1, -R)]
    d.polygon(vien, fill=BANG[0] + (255,))
    k = 0.72
    trong = [pt(L * k, 0), pt(L * 0.1 * k, R * k), pt(-L * 0.5 * k, R * 0.68 * k), pt(-L * k, 0),
             pt(-L * 0.5 * k, -R * 0.68 * k), pt(L * 0.1 * k, -R * k)]
    d.polygon(trong, fill=BANG[1] + (255,))
    sang = [pt(L * k, 0), pt(L * 0.1 * k, -R * k), pt(-L * 0.5 * k, -R * 0.68 * k), pt(-L * k, 0)]
    d.polygon(sang, fill=BANG[2] + (255,))
    d.line([pt(L * 0.7, -R * 0.05), pt(-L * 0.35, -R * 0.35)], fill=BANG[3] + (255,), width=max(1, s))
    d.ellipse([pt(L * 0.55, 0)[0] - s, pt(L * 0.55, 0)[1] - s, pt(L * 0.55, 0)[0] + s, pt(L * 0.55, 0)[1] + s],
              fill=BANG[4] + (255,))
    # vet lap lanh phia sau
    for t, q, r in ((-L * 1.25, R * 0.4, 1.0), (-L * 1.45, -R * 0.5, 0.8), (-L * 1.65, R * 0.1, 0.7)):
        x, y = pt(t, q)
        rr = r * s
        d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=BANG[3] + (230,))


def anh_manh_bay(w, fh, goc_ds):
    out = Image.new("RGBA", (w, fh * len(goc_ds)), (0, 0, 0, 0))
    for k, goc in enumerate(goc_ds):
        big = Image.new("RGBA", (w * SS, fh * SS), (0, 0, 0, 0))
        ve_manh(ImageDraw.Draw(big), w / 2.0 + 1, fh / 2.0, goc, fh * 0.62, fh * 0.26)
        out.alpha_composite(thu_nho(big, w, fh), (0, k * fh))
    return out


def anh_bang_roi(w, fh, n):
    out = Image.new("RGBA", (w, fh * n), (0, 0, 0, 0))
    for k in range(n):
        big = Image.new("RGBA", (w * SS, fh * SS), (0, 0, 0, 0))
        d = ImageDraw.Draw(big)
        ve_manh(d, w / 2.0, fh * 0.55, 90, fh * 0.8, w * 0.75)
        out.alpha_composite(thu_nho(big, w, fh), (0, k * fh))
    return out


def anh_cum_bang(w, h):
    big = Image.new("RGBA", (w * SS, h * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    ve_manh(d, w * 0.5, h * 0.52, 90, h * 0.85, w * 0.62)
    ve_manh(d, w * 0.22, h * 0.72, 70, h * 0.42, w * 0.34)
    ve_manh(d, w * 0.8, h * 0.74, 110, h * 0.38, w * 0.32)
    im = thu_nho(big, w, h)
    # tuyet duoi chan
    px = im.load()
    for x in range(1, w - 1):
        if px[x, h - 1][3] == 0:
            px[x, h - 1] = (225, 245, 255, 255)
    return im


# ----------------------------------------------------------------- icon skill theo he

MAU_HE = {  # mau nen o icon: (sang, toi)
    "Thường": ((200, 196, 170), (120, 116, 92)),
    "Lửa": ((255, 150, 70), (190, 50, 20)),
    "Nước": ((110, 170, 255), (30, 80, 190)),
    "Điện": ((255, 225, 70), (200, 140, 0)),
    "Cỏ": ((130, 215, 90), (40, 130, 40)),
    "Độc": ((200, 120, 210), (110, 40, 130)),
    "Siêu linh": ((255, 120, 170), (180, 40, 100)),
    "Ma": ((150, 120, 200), (70, 50, 120)),
    "Rồng": ((140, 110, 255), (60, 30, 180)),
    "Bóng tối": ((140, 115, 100), (60, 45, 40)),
    "Thép": ((200, 205, 220), (110, 115, 140)),
}


def o_nen(d, s, he):
    sa, to = MAU_HE.get(he, MAU_HE["Thường"])
    W = 20 * s
    for y in range(W):
        t = y / (W - 1)
        c = tuple(int(sa[i] * (1 - t) + to[i] * t) for i in range(3))
        d.line([(0, y), (W, y)], fill=c + (255,))
    # vien bo goc
    mask = Image.new("L", (W, W), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, W - 1, W - 1], radius=4 * s, fill=255)
    return mask


def _sao(d, cx, cy, r, mau, nhon=5, trong=0.45, xoay=-90):
    pts = []
    for i in range(nhon * 2):
        rr = r if i % 2 == 0 else r * trong
        a = math.radians(xoay + i * 180.0 / nhon)
        pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
    d.polygon(pts, fill=mau)


def _lua(d, cx, cy, r, s):
    d.polygon([(cx, cy - r * 1.3), (cx + r * 0.9, cy + r * 0.2), (cx + r * 0.6, cy + r), (cx - r * 0.6, cy + r),
               (cx - r * 0.9, cy + r * 0.2)], fill=(255, 90, 20, 255))
    d.ellipse([cx - r * 0.75, cy - r * 0.2, cx + r * 0.75, cy + r], fill=(255, 90, 20, 255))
    d.polygon([(cx, cy - r * 0.5), (cx + r * 0.5, cy + r * 0.4), (cx, cy + r * 0.85), (cx - r * 0.5, cy + r * 0.4)],
              fill=(255, 220, 80, 255))


def _giot(d, cx, cy, r, mau=(80, 170, 255, 255), sang=(220, 245, 255, 255)):
    d.polygon([(cx, cy - r * 1.4), (cx + r * 0.85, cy), (cx - r * 0.85, cy)], fill=mau)
    d.ellipse([cx - r, cy - r * 0.5, cx + r, cy + r * 1.1], fill=mau)
    d.ellipse([cx - r * 0.5, cy - r * 0.1, cx - r * 0.1, cy + r * 0.4], fill=sang)


def _set(d, cx, cy, r, s, mau=(255, 240, 60, 255)):
    pts = [(cx + r * 0.2, cy - r * 1.2), (cx - r * 0.6, cy + r * 0.1), (cx - r * 0.05, cy + r * 0.1),
           (cx - r * 0.3, cy + r * 1.2), (cx + r * 0.7, cy - r * 0.25), (cx + r * 0.1, cy - r * 0.25)]
    d.polygon(pts, fill=(90, 60, 0, 255))
    k = 0.75
    d.polygon([(cx + (x - cx) * k, cy + (y - cy) * k) for x, y in pts], fill=mau)


def _la(d, cx, cy, r, goc, mau=(90, 200, 70, 255)):
    a = math.radians(goc)
    ux, uy = math.cos(a), math.sin(a)
    vx, vy = -uy, ux
    pts = []
    for i in range(13):
        t = -1 + i / 6.0
        q = (1 - t * t) * 0.45
        pts.append((cx + ux * t * r + vx * q * r, cy + uy * t * r + vy * q * r))
    for i in range(12, -1, -1):
        t = -1 + i / 6.0
        q = (1 - t * t) * 0.45
        pts.append((cx + ux * t * r - vx * q * r, cy + uy * t * r - vy * q * r))
    d.polygon(pts, fill=mau)
    d.line([(cx - ux * r, cy - uy * r), (cx + ux * r, cy + uy * r)], fill=(30, 110, 30, 255), width=2)


def glyph(d, s, ten, he):
    c = 10 * s
    W = (255, 255, 255, 255)
    t = ten.lower()
    if "cào" in t or "vuốt" in t:
        mau = (210, 190, 255, 255) if he == "Rồng" else W
        for k in (-4, 0, 4):
            d.line([(c + (k - 3) * s, c - 6 * s), (c + (k + 3) * s, c + 6 * s)], fill=mau, width=int(1.6 * s))
    elif "húc" in t or "đâm sầm" in t or "bứt phá" in t:
        _sao(d, c, c, 7 * s, (255, 250, 210, 255), nhon=8, trong=0.5)
        _sao(d, c, c, 3.5 * s, (255, 200, 60, 255), nhon=8, trong=0.5)
        if "điện" in t:
            _set(d, c, c, 4 * s, s)
    elif "khói" in t:
        for x, y, r in ((7, 11, 4), (12, 10, 4.5), (10, 7, 3.5), (14, 13, 3)):
            d.ellipse([(x - r) * s, (y - r) * s, (x + r) * s, (y + r) * s], fill=(235, 235, 235, 255))
    elif "múa" in t or "sinh trưởng" in t or "nạp" in t:
        # mui ten tang chi so (khong ve kiem)
        mau = (255, 90, 60, 255) if "múa" in t else ((120, 230, 90, 255) if "sinh" in t else (255, 240, 60, 255))
        for x0 in (6, 13):
            d.polygon([(x0 * s, 4 * s), ((x0 + 4) * s, 9 * s), ((x0 + 1.6) * s, 9 * s), ((x0 + 1.6) * s, 16 * s),
                       ((x0 - 1.6) * s, 16 * s), ((x0 - 1.6) * s, 9 * s), ((x0 - 4) * s, 9 * s)], fill=mau)
    elif "tự vệ" in t:
        d.ellipse([3 * s, 3 * s, 17 * s, 17 * s], outline=(150, 255, 160, 255), width=2 * s)
        d.ellipse([6 * s, 6 * s, 14 * s, 14 * s], outline=(220, 255, 220, 255), width=s)
    elif "nhanh" in t:
        for y, x0 in ((6, 3), (10, 1), (14, 4)):
            d.line([(x0 * s, y * s), ((x0 + 9) * s, y * s)], fill=W, width=int(1.5 * s))
        _sao(d, 14 * s, 10 * s, 4 * s, (255, 240, 120, 255), nhon=4, trong=0.4)
    elif "sao băng" in t or "mong ước" in t:
        _sao(d, c, c, 7.5 * s, (255, 230, 60, 255))
        _sao(d, c, c, 4 * s, (255, 250, 200, 255))
    elif "hồi sinh" in t:
        d.ellipse([4 * s, 5 * s, 10.5 * s, 11.5 * s], fill=(255, 110, 150, 255))
        d.ellipse([9.5 * s, 5 * s, 16 * s, 11.5 * s], fill=(255, 110, 150, 255))
        d.polygon([(4.3 * s, 9.5 * s), (15.7 * s, 9.5 * s), (10 * s, 16 * s)], fill=(255, 110, 150, 255))
        d.rectangle([9 * s, 6.5 * s, 11 * s, 12.5 * s], fill=W)
        d.rectangle([7 * s, 8.5 * s, 13 * s, 10.5 * s], fill=W)
    elif "phá hủy" in t:
        d.rectangle([2 * s, 8 * s, 18 * s, 12 * s], fill=(255, 230, 140, 255))
        d.rectangle([2 * s, 9 * s, 18 * s, 11 * s], fill=W)
        d.ellipse([1 * s, 6 * s, 7 * s, 14 * s], fill=(255, 200, 80, 255))
    elif "phá mai" in t or "rút vào mai" in t:
        d.chord([3 * s, 4 * s, 17 * s, 18 * s], 180, 360, fill=(170, 110, 60, 255))
        d.rectangle([3 * s, 11 * s, 17 * s, 13 * s], fill=(240, 220, 160, 255))
        for x in (7, 10, 13):
            d.line([(x * s, 5 * s), (x * s, 11 * s)], fill=(110, 70, 30, 255), width=s)
        if "phá" in t:
            d.line([(9 * s, 4 * s), (11 * s, 8 * s), (9 * s, 11 * s)], fill=W, width=s)
    elif "cắn" in t or "nanh" in t:
        mau = (255, 255, 255, 255)
        for x in (5, 9, 13):
            d.polygon([(x * s, 5 * s), ((x + 3) * s, 5 * s), ((x + 1.5) * s, 9.5 * s)], fill=mau)
            d.polygon([(x * s, 15 * s), ((x + 3) * s, 15 * s), ((x + 1.5) * s, 10.5 * s)], fill=mau)
        if "lửa" in t:
            _lua(d, 15 * s, 13 * s, 3 * s, s)
    elif "nghỉ" in t:
        f = ImageFont.truetype(PHONG, 7 * s)
        d.text((4 * s, 1 * s), "z", font=f, fill=W)
        d.text((9 * s, 6 * s), "Z", font=f, fill=W)
    elif "nỗi đau" in t:
        d.chord([5 * s, 3 * s, 15 * s, 13 * s], 180, 360, fill=(220, 210, 255, 255))
        d.polygon([(5 * s, 8 * s), (15 * s, 8 * s), (15 * s, 16 * s), (12.5 * s, 14 * s), (10 * s, 16 * s),
                   (7.5 * s, 14 * s), (5 * s, 16 * s)], fill=(220, 210, 255, 255))
        d.ellipse([7 * s, 7 * s, 9 * s, 9.5 * s], fill=(60, 40, 110, 255))
        d.ellipse([11 * s, 7 * s, 13 * s, 9.5 * s], fill=(60, 40, 110, 255))
    elif "đuôi sắt" in t:
        d.arc([3 * s, 3 * s, 17 * s, 17 * s], 200, 340, fill=(240, 245, 255, 255), width=3 * s)
        _sao(d, 15 * s, 13 * s, 3.5 * s, W, nhon=4, trong=0.4)
    elif he == "Lửa":
        if "xoáy" in t:
            d.arc([3 * s, 3 * s, 17 * s, 17 * s], 0, 300, fill=(255, 200, 60, 255), width=2 * s)
            d.arc([6 * s, 6 * s, 14 * s, 14 * s], 120, 420, fill=(255, 110, 30, 255), width=2 * s)
        elif "đại" in t:
            _sao(d, c, c, 8 * s, (255, 100, 20, 255), nhon=6, trong=0.55)
            _lua(d, c, c + s, 4 * s, s)
        else:
            r = 5.5 if "phun" in t else 4.5
            _lua(d, c, c + s, r * s, s)
            if "than" in t:
                _lua(d, 5 * s, 13 * s, 2 * s, s)
                _lua(d, 15 * s, 13 * s, 2 * s, s)
    elif he == "Cỏ":
        if "roi" in t:
            d.arc([2 * s, 3 * s, 14 * s, 17 * s], 270, 450, fill=(60, 160, 50, 255), width=2 * s)
            d.arc([6 * s, 3 * s, 18 * s, 17 * s], 90, 270, fill=(110, 220, 80, 255), width=2 * s)
        elif "hạt" in t:
            for x, y in ((7, 8), (13, 8), (10, 13)):
                d.ellipse([(x - 3) * s, (y - 3) * s, (x + 3) * s, (y + 3) * s], fill=(160, 110, 50, 255))
                d.ellipse([(x - 2) * s, (y - 2.2) * s, (x - 0.5) * s, (y - 0.7) * s], fill=(230, 200, 140, 255))
        elif "mặt trời" in t:
            _sao(d, c, c, 8 * s, (255, 230, 80, 255), nhon=10, trong=0.65)
            d.ellipse([6 * s, 6 * s, 14 * s, 14 * s], fill=(255, 250, 200, 255))
        else:
            _la(d, 8 * s, 8 * s, 5 * s, -40)
            _la(d, 12.5 * s, 12.5 * s, 5 * s, -40, (150, 230, 100, 255))
            if "bão" in t:
                _la(d, 14 * s, 5 * s, 3 * s, 30)
    elif he == "Độc":
        d.ellipse([4 * s, 6 * s, 16 * s, 16 * s], fill=(150, 70, 170, 255))
        d.ellipse([7 * s, 4 * s, 12 * s, 9 * s], fill=(150, 70, 170, 255))
        d.ellipse([6 * s, 8 * s, 9 * s, 11 * s], fill=(220, 170, 240, 255))
    elif he == "Nước":
        if "bong" in t:
            for x, y, r in ((7, 11, 4), (13, 7, 3), (13.5, 14, 2.5)):
                d.ellipse([(x - r) * s, (y - r) * s, (x + r) * s, (y + r) * s], outline=(225, 245, 255, 255), width=s)
                d.ellipse([(x - r * 0.5) * s, (y - r * 0.6) * s, (x - r * 0.1) * s, (y - r * 0.2) * s], fill=W)
        elif "xoáy" in t:
            for k, r in enumerate((7, 5, 3)):
                d.arc([(10 - r) * s, (10 - r) * s, (10 + r) * s, (10 + r) * s], 30 + k * 90, 300 + k * 90,
                      fill=(220, 245, 255, 255), width=int(1.5 * s))
        elif "đuôi" in t:
            d.arc([2 * s, 4 * s, 18 * s, 20 * s], 190, 350, fill=(220, 245, 255, 255), width=3 * s)
            _giot(d, 15 * s, 8 * s, 2.5 * s)
        elif "bơm" in t or "pháo" in t:
            d.rectangle([2 * s, 8 * s, 13 * s, 12 * s], fill=(180, 225, 255, 255))
            r = 5 if "pháo" in t else 4
            d.ellipse([(15 - r) * s, (10 - r) * s, (15 + r) * s, (10 + r) * s], fill=(120, 200, 255, 255))
            d.ellipse([(14 - r * 0.4) * s, (9 - r * 0.4) * s, (14) * s, (9) * s], fill=W)
        else:
            _giot(d, c, c + s, 5 * s)
    elif he == "Điện":
        if "cầu" in t:
            d.ellipse([4 * s, 4 * s, 16 * s, 16 * s], fill=(255, 240, 120, 255))
            _set(d, c, c, 4.5 * s, s, (255, 255, 255, 255))
        elif "lưới" in t:
            for k in range(4):
                a = math.radians(k * 45)
                d.line([(c - 7 * s * math.cos(a), c - 7 * s * math.sin(a)),
                        (c + 7 * s * math.cos(a), c + 7 * s * math.sin(a))], fill=(255, 245, 150, 255), width=s)
            for r in (3, 6):
                d.ellipse([c - r * s, c - r * s, c + r * s, c + r * s], outline=(255, 245, 150, 255), width=s)
        elif "sóng" in t:
            for k in (0, 5):
                d.line([(2 * s, (7 + k) * s), (6 * s, (4 + k) * s), (10 * s, (7 + k) * s), (14 * s, (4 + k) * s),
                        (18 * s, (7 + k) * s)], fill=(255, 245, 120, 255), width=int(1.6 * s))
        else:
            _set(d, c, c, 7 * s, s)
            if "sấm" in t or "chớp" in t:
                _set(d, 15 * s, 6 * s, 3 * s, s, (255, 255, 200, 255))
    elif he == "Siêu linh":
        for x0 in (3, 9):
            d.line([(x0 * s, 7 * s), ((x0 + 8) * s, 7 * s)], fill=W, width=s)
        _sao(d, 14 * s, 13 * s, 4 * s, (255, 220, 240, 255), nhon=4, trong=0.4)
    else:
        _sao(d, c, c, 7 * s, (255, 255, 255, 255), nhon=4, trong=0.4)


def anh_icon_skill(n_o, ds_chieu, he_dong):
    W, H = 20, 20 * n_o + 1
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    for k in range(n_o):
        ten, he = ds_chieu[k] if k < len(ds_chieu) else ("", he_dong)
        big = Image.new("RGBA", (20 * SS, 20 * SS), (0, 0, 0, 0))
        d = ImageDraw.Draw(big)
        mask = o_nen(d, SS, he)
        lop = Image.new("RGBA", big.size, (0, 0, 0, 0))
        glyph(ImageDraw.Draw(lop), SS, ten, he)
        # vien toi cho glyph
        bong = lop.split()[3].filter(ImageFilter.MaxFilter(5))
        den = Image.new("RGBA", big.size, (20, 20, 30, 200))
        big.paste(den, (0, 0), bong)
        big.alpha_composite(lop)
        nen = Image.new("RGBA", big.size, (0, 0, 0, 0))
        nen.paste(big, (0, 0), mask)
        o = thu_nho(nen, 20, 20, cat_alpha=False)
        dd = ImageDraw.Draw(o)
        dd.rounded_rectangle([0, 0, 19, 19], radius=4, outline=(30, 30, 40, 255))
        dd.line([(4, 1), (15, 1)], fill=(255, 255, 255, 120))
        out.alpha_composite(o, (0, 20 * k))
    return out


def doc_bang_chieu():
    import csv
    kq = {}
    with open(p("du_lieu", "bang_chieu.csv"), encoding="utf8") as f:
        for r in csv.DictReader(f):
            kq.setdefault(int(r["class"]), []).append((int(r["so_hieu_skill"]), r["ten_moi"], r["he"]))
    return {k: [(t, h) for _, t, h in sorted(v)] for k, v in kq.items()}


HE_DONG = ["Lửa", "Cỏ", "Thường", "Nước", "Điện"]

# ----------------------------------------------------------------- d_effect: dinh dang DataSkillEff


def doc_deff(b):
    i = [0]

    def rd(fmt):
        v = struct.unpack_from(">" + fmt, b, i[0])
        i[0] += struct.calcsize(">" + fmt)
        return v
    n = rd("b")[0]
    small = [list(rd("BBBBB")) for _ in range(n)]
    nf = rd("h")[0]
    frames = []
    for _ in range(nf):
        k = rd("b")[0]
        frames.append([list(rd("hhbbb")) for _ in range(k)])
    ns = rd("B")[0]
    seq = [rd("h")[0] for _ in range(ns)]
    start = rd("b")[0]
    fc = []
    for _ in range(3):
        k = rd("b")[0]
        fc.append(list(rd("b" * k)) if k else [])
    return dict(small=small, frames=frames, seq=seq, start=start, fc=fc, rest=b[i[0]:])


def ghi_deff(d):
    o = bytearray(struct.pack(">b", len(d["small"])))
    for s in d["small"]:
        o += struct.pack(">BBBBB", *s)
    o += struct.pack(">h", len(d["frames"]))
    for f in d["frames"]:
        o += struct.pack(">b", len(f))
        for q in f:
            o += struct.pack(">hhbbb", *q)
    o += struct.pack(">B", len(d["seq"]))
    for s in d["seq"]:
        o += struct.pack(">h", s)
    o += struct.pack(">b", d["start"])
    for k in d["fc"]:
        o += struct.pack(">b", len(k)) + bytes((x & 255) for x in k)
    return bytes(o) + d["rest"]


# ----------------------------------------------------------------- danh hieu

# id d_effect -> (ten = data.DanhHieu.getName(8700 + id), bo mau). 67..69 (dung chung anh voi pet
# Lan hoa hinh) va 87 ACE / 88 Fan cung (chu trung lap, khong kiem hiep) KHONG ve lai o day.
DANH_HIEU = {
    70: ("Đại gia", "cam"), 71: ("Đệ nhất", "do"), 72: ("Tứ Thiên Vương", "vang"),
    73: ("Nhà thám hiểm", "luc"), 74: ("Người dũng cảm", "do"), 75: ("Huyền thoại", "vang"),
    76: ("Bậc thầy Pokémon", "lam"), 77: ("Uy phong", "cam"), 78: ("Vô địch", "do"),
    79: ("Nhà vô địch Pokémon", "vang"), 85: ("Tia Mặt Trời", "vang"), 86: ("Có một không hai", "tim"),
    200: ("Angle Of Death", "tim"), 201: ("Bất Chấp Cuộc Chơi", "cam"), 202: ("Huyền Thoại Bất Diệt", "vang"),
    203: ("Rayquaza Chấn Thiên", "luc"), 204: ("Kẻ Thách Thức Định Mệnh", "lam"), 205: ("Gươm Aegislash", "bac"),
    206: ("Nhà Vô Địch Mọi Vùng", "vang"), 207: ("Vô Địch Mọi Vùng", "cam"), 208: ("Bảo Vật Bất Bại", "vang"),
    209: ("Bá Chủ Một Vùng", "do"), 210: ("Tung Hoành Mọi Vùng", "do"), 211: ("Tinh Anh Tuyệt Đỉnh", "do"),
    224: ("Độc Nhất Vô Nhị", "luc"), 225: ("Tia Chớp Phá Trời", "tim"), 235: ("Vinh Danh Đội", "lam"),
    256: ("Người Được Chọn", "lam"), 257: ("Nhà Vô Địch Pokémon 2", "xanh"),
    283: ("Huyền Thoại Bất Diệt 2", "lam"), 284: ("Truyền Nhân Huyền Thoại", "xanh"),
    285: ("Sức Mạnh Hủy Diệt", "do"), 286: ("Kinh Tài Tuyệt Diễm", "tim"), 287: ("Nữ Thần Tuyệt Sắc", "hong"),
    288: ("Phong Vân Tùy Bút", "do"), 289: ("Lão Làng Pokémon", "vang"), 290: ("Số Một Mọi Vùng", "xanh"),
    291: ("Vô Song Mọi Vùng", "vang"), 292: ("Tung Hoành Mọi Vùng", "tim"), 293: ("Có Một Không Hai", "xanh"),
    294: ("Vinh Hoa Phú Qúy", "vang"), 295: ("Chúa Tể Mọi Vùng", "cam"), 296: ("Tuyệt Thế Giai Nhân", "hong"),
}

BO_MAU = {  # (sang, giua, toi, vien chu)
    "vang": ((255, 236, 120), (240, 180, 30), (170, 100, 10), (110, 55, 0)),
    "cam": ((255, 200, 120), (245, 120, 40), (170, 60, 15), (100, 30, 5)),
    "do": ((255, 140, 130), (215, 40, 40), (130, 15, 25), (80, 5, 10)),
    "lam": ((150, 200, 255), (50, 110, 230), (20, 50, 140), (10, 25, 80)),
    "xanh": ((150, 245, 240), (30, 180, 200), (10, 95, 120), (5, 50, 70)),
    "tim": ((215, 160, 255), (140, 70, 220), (70, 25, 130), (40, 10, 80)),
    "luc": ((170, 245, 140), (60, 170, 60), (20, 95, 35), (10, 55, 20)),
    "hong": ((255, 180, 220), (235, 90, 160), (150, 30, 95), (90, 10, 55)),
    "bac": ((240, 245, 255), (165, 175, 200), (90, 95, 125), (45, 45, 70)),
}


def ve_poke_ball(d, cx, cy, r):
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(30, 30, 40, 255))
    r2 = r - SS
    d.pieslice([cx - r2, cy - r2, cx + r2, cy + r2], 180, 360, fill=(235, 50, 50, 255))
    d.pieslice([cx - r2, cy - r2, cx + r2, cy + r2], 0, 180, fill=(250, 250, 250, 255))
    d.rectangle([cx - r2, cy - SS * 0.8, cx + r2, cy + SS * 0.8], fill=(30, 30, 40, 255))
    d.ellipse([cx - r * 0.38, cy - r * 0.38, cx + r * 0.38, cy + r * 0.38], fill=(30, 30, 40, 255))
    d.ellipse([cx - r * 0.24, cy - r * 0.24, cx + r * 0.24, cy + r * 0.24], fill=(255, 255, 255, 255))
    d.ellipse([cx - r * 0.62, cy - r * 0.7, cx - r * 0.3, cy - r * 0.4], fill=(255, 200, 200, 255))


def ve_danh_hieu(ten, mau, sang_trong):
    """Mot khung ruy bang (anh tinh) + danh sach 6 khung lap lanh."""
    sa, gi, to, vc = BO_MAU[mau]
    cao = 20 if sang_trong else 16
    fsz = 11
    f = ImageFont.truetype(PHONG, fsz * SS)
    tw = int(math.ceil(f.getlength(ten) / SS))
    bong = 7 if sang_trong else 6   # ban kinh Poke Ball (px)
    canh = 7 if sang_trong else 0   # dai "canh" ruy bang hai ben
    w = canh * 2 + bong * 2 + 2 + tw + 8
    w = min(w, 254)
    big = Image.new("RGBA", (w * SS, cao * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    y0, y1 = (3 if sang_trong else 2) * SS, (cao - (3 if sang_trong else 2)) * SS
    x0, x1 = (canh + bong) * SS, (w - canh - 1) * SS
    if sang_trong:  # duoi ruy bang hai ben (hinh chu V)
        for trai in (True, False):
            if trai:
                pts = [(x0 + 2 * SS, y0 + 2 * SS), (1 * SS, y0), (canh * SS * 0.55, (y0 + y1) / 2), (1 * SS, y1),
                       (x0 + 2 * SS, y1 - 2 * SS)]
            else:
                X = w * SS
                pts = [(x1 - 2 * SS, y0 + 2 * SS), (X - 1 * SS, y0), (X - canh * SS * 0.55, (y0 + y1) / 2),
                       (X - 1 * SS, y1), (x1 - 2 * SS, y1 - 2 * SS)]
            d.polygon(pts, fill=vc + (255,))
            k = [(x + (1 if trai else -1) * SS * 0.8, y) for x, y in pts]
            d.polygon(k, fill=to + (255,))
    # than ruy bang: vien toi, nen chuyen mau
    d.rounded_rectangle([x0 - bong * SS, y0, x1, y1], radius=(y1 - y0) / 2, fill=vc + (255,))
    nen = Image.new("RGBA", big.size, (0, 0, 0, 0))
    nd = ImageDraw.Draw(nen)
    for y in range(int(y0 + SS), int(y1 - SS)):
        t = (y - y0) / float(y1 - y0)
        c = tuple(int(sa[i] * (1 - t) * 1.0 + gi[i] * t) if t < 0.5 else int(gi[i] * (1.5 - t) + to[i] * (t - 0.5))
                  for i in range(3))
        nd.line([(x0 - bong * SS + SS, y), (x1 - SS, y)], fill=c + (255,))
    m = Image.new("L", big.size, 0)
    ImageDraw.Draw(m).rounded_rectangle([x0 - bong * SS + SS, y0 + SS, x1 - SS, y1 - SS],
                                        radius=(y1 - y0) / 2 - SS, fill=255)
    big.paste(nen, (0, 0), m)
    d.line([(x0, y0 + 2 * SS), (x1 - 3 * SS, y0 + 2 * SS)], fill=(255, 255, 255, 140), width=SS)
    # chu: trang, vien toi
    tx = x0 + (bong + 3) * SS
    ty = (cao * SS - fsz * SS * 1.36) / 2
    for ox in (-SS, 0, SS):
        for oy in (-SS, 0, SS):
            if ox or oy:
                d.text((tx + ox, ty + oy), ten, font=f, fill=vc + (255,))
    d.text((tx, ty), ten, font=f, fill=(255, 255, 255, 255))
    ve_poke_ball(d, x0, (y0 + y1) / 2, bong * SS)
    goc = thu_nho(big, w, cao)
    khung = [goc]
    # 6 khung lap lanh: vet sang cheo quet qua + sao nho
    for k in range(6):
        im = goc.copy()
        lop = Image.new("RGBA", im.size, (0, 0, 0, 0))
        ld = ImageDraw.Draw(lop)
        xs = int((canh + bong) + (w - 2 * canh - bong) * (k + 0.5) / 6.0)
        ld.polygon([(xs - 2, cao), (xs + 2, cao), (xs + 7, 0), (xs + 3, 0)], fill=(255, 255, 255, 110))
        a = im.split()[3]
        lop.putalpha(Image.composite(lop.split()[3], Image.new("L", im.size, 0), a))
        im.alpha_composite(lop)
        if sang_trong or k % 2 == 0:
            sd = ImageDraw.Draw(im)
            for (sx, sy) in ((w - canh - 4 - (k * 7) % max(1, w // 3), 1), (canh + bong + 2 + (k * 11) % max(1, w // 2), cao - 2)):
                sx = max(1, min(w - 2, sx))
                sd.point((sx, sy), fill=(255, 255, 230, 255))
                for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if 0 <= sx + ox < w and 0 <= sy + oy < cao:
                        sd.point((sx + ox, sy + oy), fill=(255, 250, 200, 170))
        khung.append(im)
    return khung


def ve_lai_danh_hieu(idd, ten, mau):
    duong = p("server", "map", "d_effect", str(idd))
    d = doc_deff(open(duong, "rb").read())
    # day khung giu nguyen vi tri cu (day bounding box khung 0)
    day = -40
    f0 = d["frames"][0] if d["frames"] else []
    if f0:
        day = max(q[1] + d["small"][q[2]][4] for q in f0)
    khung = ve_danh_hieu(ten, mau, idd >= 200)
    w, h = khung[0].size
    atlas = Image.new("RGBA", (w, h * len(khung)), (0, 0, 0, 0))
    for k, im in enumerate(khung):
        atlas.alpha_composite(im, (0, k * h))
    d["small"] = [[k, 0, k * h, w, h] for k in range(len(khung))]
    thu_tu = [0] * 6 + list(range(1, len(khung)))
    d["frames"] = [[[-(w // 2), day - h, i, 0, 0]] for i in thu_tu]
    d["seq"] = list(range(len(d["frames"]))) if d["seq"] else []
    moi = ghi_deff(d)
    doi = luu(atlas, duong + ".png")
    if open(duong, "rb").read() != moi:
        open(duong, "wb").write(moi)
        doi = True
    return doi


# ----------------------------------------------------------------- hao quang

MAU_HAO_QUANG = {186: (235, 245, 255), 187: (255, 230, 90), 188: (110, 220, 255), 189: (240, 120, 255), 190: (110, 255, 160)}


def ve_hao_quang(idd):
    duong = p("server", "map", "d_effect", str(idd))
    d = doc_deff(open(duong, "rb").read())
    anh = Image.open(duong + ".png").convert("RGBA")
    W, H = anh.size
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    mau = MAU_HAO_QUANG[idd]
    sang = tuple(min(255, c + 60) for c in mau)
    for s in d["small"]:
        k, x, y, w, h = s
        if w <= 0 or h <= 0:
            continue
        big = Image.new("RGBA", (w * SS, h * SS), (0, 0, 0, 0))
        g = ImageDraw.Draw(big)
        pha = k * 0.9
        if h >= 30:   # lop sau: vang sang bau duc + hat sang bay len
            for r, a in ((0, 70), (SS, 110), (2 * SS, 170)):
                g.ellipse([w * SS * 0.12 + r, h * SS * 0.06 + r, w * SS * 0.88 - r, h * SS * 0.98 - r],
                          outline=mau + (a,), width=SS)
            for i in range(7):
                t = (i / 7.0 + pha / 7.0) % 1.0
                ax = w * SS * (0.5 + 0.36 * math.cos(i * 2.4 + pha))
                ay = h * SS * (0.95 - 0.85 * t)
                rr = SS * (1.6 - t)
                g.ellipse([ax - rr, ay - rr, ax + rr, ay + rr], fill=sang + (255,))
            for i in range(3):
                ax = w * SS * (0.2 + 0.3 * i + 0.05 * math.sin(pha + i))
                ay = h * SS * (0.18 + 0.25 * ((i + k) % 3))
                _sao(g, ax, ay, 2.2 * SS, (255, 255, 255, 255), nhon=4, trong=0.35)
        else:         # lop truoc duoi chan: vong sang det
            g.ellipse([SS, SS, w * SS - SS, h * SS - SS], outline=mau + (220,), width=SS)
            g.ellipse([3 * SS, h * SS * 0.3, w * SS - 3 * SS, h * SS * 0.8], outline=sang + (160,), width=SS)
        out.alpha_composite(thu_nho(big, w, h, cat_alpha=False), (x, y))
    return luu(out, duong + ".png")


def main():
    if not os.path.isfile(PHONG):
        sys.exit("thieu phong " + PHONG)
    doi = []
    sk = p("client_jar", "res", "sword skill")
    u = p("client_pc_ios", "Assets", "Resources", "res", "sword skill")
    viec = [
        (os.path.join(sk, "kiem.png"), anh_manh_bay(28, 28, [0, -30, -60])),
        (p("server", "map", "imgArrow", "7.png"), anh_manh_bay(28, 28, [0, -30, -60])),
        (os.path.join(sk, "kiem01.png"), anh_bang_roi(12, 39, 2)),
        (os.path.join(sk, "all1.png"), anh_cum_bang(13, 27)),
    ]
    for duong, im in viec:
        if luu(im, duong):
            doi.append(duong)
    for ten in ("kiem01", "all1"):   # Unity: cung byte voi jar
        src, dst = os.path.join(sk, ten + ".png"), os.path.join(u, ten + ".bytes")
        if os.path.isfile(dst) and open(dst, "rb").read() != open(src, "rb").read():
            open(dst, "wb").write(open(src, "rb").read())
            doi.append(dst)
    chieu = doc_bang_chieu()
    for lop in range(5):
        duong = p("server", "map", "sk%d.png" % lop)
        n = Image.open(duong).size[1] // 20
        if luu(anh_icon_skill(n, chieu.get(lop, []), HE_DONG[lop]), duong):
            doi.append(duong)
    for idd, (ten, mau) in sorted(DANH_HIEU.items()):
        if ve_lai_danh_hieu(idd, ten, mau):
            doi.append("d_effect/%d" % idd)
    for idd in sorted(MAU_HAO_QUANG):
        if ve_hao_quang(idd):
            doi.append("d_effect/%d.png" % idd)
    print("doi %d tep" % len(doi))
    for x in doi:
        print("  ", os.path.relpath(x, GOC) if x.startswith(GOC) else x)


if __name__ == "__main__":
    main()
