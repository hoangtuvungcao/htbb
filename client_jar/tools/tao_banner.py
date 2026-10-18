#!/usr/bin/env python3
"""Key art "Huyền Thoại Bảo Bối" - cảnh hoàng hôn nhiều lớp dùng chung cho MỌI bản (J2ME, Android, PC, web).

    python3 client_jar/tools/tao_banner.py [--ninja <ninja_full>] [--pmd <pmd_raw>] [--web <HTBB-Web>] [--xem <thư mục xem trước>]
      <ninja_full> mặc định ../HTBB/tools/pokemon/pmd_raw_tile_moi/ninja_full (tile Ninja Adventure, CC0)
      <pmd_raw>    mặc định ../HTBB/tools/pokemon/pmd_raw (sprite PMDCollab SpriteCollab, CC BY-NC 4.0)
      cần: Pillow, numpy. Chạy tao_logo.py TRƯỚC (logo ghép vào ảnh poster / OG).

Cảnh pixel art vẽ ở độ phân giải gốc (1 px gốc = 3 px ở 1080p): trời hoàng hôn (dải màu + dither), sao, mặt
trời, mây sáng viền dưới, bóng Rayquaza mờ trên trời, hai dãy núi xa (sương mù - phối cảnh khí quyển), rừng
(cây Ninja Adventure tô lại theo ánh ngược sáng), hồ phản chiếu hoàng hôn, thảm cỏ có lối mòn dẫn vào cảnh,
khung tiền cảnh tối hai bên, 5 Pokémon khởi đầu (sprite PMD phóng nguyên lần) + vài Pokémon hoang dã.

Ghi:
  client_jar/shared/banner/lop/      các lớp 880x400 (RGBA, xếp chồng thẳng) + lop.txt (PC BannerParallax, web hero)
  client_jar/shared/banner/doc/      lớp cho màn dọc 360x640 (web điện thoại)
  client_jar/shared/banner/          keyart_*.png (không chữ) + poster_*.png (có logo) các cỡ, og_1200x630.png,
                          android_*.png, tai_*.png (màn tải), j2me_*.png (bản ghép mô phỏng màn J2ME)
  client_jar/shared/banner/res/bn/hd APK: nen.png (cảnh tĩnh ghép sẵn) + may.png + rong.png + khung.png (Banner.java chế độ HD)
  client_jar/res/bn/            dải lớp rộng 320 px bảng màu nhỏ: nui, rung, dat, khung, may, rong (+ logo* từ tao_logo)
  --web                   HTBB-Web/public/keyart/ (lớp + ảnh màn dọc), public/og/htbb.png
"""
import json
import math
import os
import random
import shutil
import sys
import xml.etree.ElementTree as ET

import numpy as np
from PIL import Image, ImageFilter

GOC = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(GOC, '..', '..'))
NINJA = os.path.abspath(os.path.join(REPO, '..', 'HTBB', 'tools', 'pokemon', 'pmd_raw_tile_moi', 'ninja_full'))
PMD = os.path.abspath(os.path.join(REPO, '..', 'HTBB', 'tools', 'pokemon', 'pmd_raw'))
BANNER = os.path.join(REPO, 'shared', 'banner')
BRAND = os.path.join(REPO, 'shared', 'brand')

MW, MH = 880, 400            # khung gốc (21:9 + lề parallax); 16:9 = cắt giữa 640x360
C169 = (120, 20, 640, 360)   # x, y, w, h vùng 16:9 trong khung gốc

# 5 Pokémon khởi đầu theo thứ tự class 0..4 (LoaiPokemonClient.khoiDau): Charmander, Bulbasaur, Eevee, Squirtle, Pichu
KHOI_DAU = ['0004', '0001', '0133', '0007', '0172']
# hướng: 0 xuống, 1 lên, 2 trái, 3 phải (thứ tự hàng ảnh J2ME); PMD: xuống 0, phải 2, trái 6
PMD_HUONG = {0: 0, 1: 4, 2: 6, 3: 2}

# Đội hình (đơn vị: độ lệch theo bề ngang sprite 1x, tính từ tâm cảnh; y = độ lùi về sau, px 1x).
# Dùng chung: tao_banner (ảnh), Banner.java (J2ME/Android), BannerParallax.cs (PC), web (khoi_dau.json).
DOI_HINH = [
    # class, dx, lùi, hướng, động tác, khung
    (0, -64, 9, 3, 'Idle', 0),     # Charmander: trái, nhìn sang phải (vào giữa)
    (1, -124, 20, 0, 'Idle', 1),   # Bulbasaur: ngoài cùng trái, lùi sau, nhìn người xem
    (2, 0, 0, 0, 'Idle', 0),       # Eevee: giữa, gần nhất (mũi chữ V)
    (3, 64, 9, 2, 'Idle', 2),      # Squirtle: phải, nhìn sang trái (vào giữa)
    (4, 122, 20, 0, 'Idle', 1),    # Pichu: ngoài cùng phải, lùi sau
]

# Bảng màu trời (vị trí 0..1 từ đỉnh tới chân trời) - Banner.java dùng đúng các mốc này
TROI = [(0.00, 0x12163D), (0.22, 0x272462), (0.42, 0x553384), (0.60, 0xA4457F),
        (0.74, 0xDE666B), (0.87, 0xF99B5A), (1.00, 0xFFD480)]


def hx(h):
    return np.array([(h >> 16) & 255, (h >> 8) & 255, h & 255], np.float32)


BAYER = np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]], np.float32) / 16 + 1 / 32


def bayer(h, w, ox=0, oy=0):
    return np.tile(BAYER, (h // 4 + 2, w // 4 + 2))[oy % 4:oy % 4 + h, ox % 4:ox % 4 + w]


# --------------------------------------------------------------------------- lớp ảnh (numpy RGBA float 0..1)

class Lop:
    def __init__(self, w, h):
        self.a = np.zeros((h, w, 4), np.float32)
        self.w, self.h = w, h

    def to(self, mask, mau, alpha=1.0):
        """Tô màu (rgb 0..255) lên vùng mask (bool hoặc float 0..1) với độ phủ alpha."""
        m = mask.astype(np.float32) * alpha
        if m.ndim == 2:
            m = m[:, :, None]
        c = np.asarray(mau, np.float32) / 255
        if c.ndim == 1:
            c = c[None, None, :]
        self.a[:, :, :3] = self.a[:, :, :3] * (1 - m) + c * m
        self.a[:, :, 3:] = self.a[:, :, 3:] * (1 - m) + m

    def dan(self, img, x, y):
        """Dán ảnh PIL RGBA (alpha 1-bit) tại x, y (cắt mép)."""
        arr = np.asarray(img.convert('RGBA'), np.float32) / 255
        h, w = arr.shape[:2]
        x0, y0 = max(0, x), max(0, y)
        x1, y1 = min(self.w, x + w), min(self.h, y + h)
        if x1 <= x0 or y1 <= y0:
            return
        s = arr[y0 - y:y1 - y, x0 - x:x1 - x]
        m = s[:, :, 3:]
        d = self.a[y0:y1, x0:x1]
        d[:, :, :3] = d[:, :, :3] * (1 - m) + s[:, :, :3] * m
        d[:, :, 3:] = d[:, :, 3:] * (1 - m) + m

    def anh(self):
        return Image.fromarray((np.clip(self.a, 0, 1) * 255 + 0.5).astype(np.uint8), 'RGBA')


def chong(*lops):
    out = Image.new('RGBA', lops[0].size, (0, 0, 0, 0))
    for l in lops:
        if l is not None:
            out.alpha_composite(l)
    return out


def tron(a, b, t):
    return a + (b - a) * t


# --------------------------------------------------------------------------- sprite PMD

def pmd_khung(dex, anim, huong, f):
    d = os.path.join(PMD, dex)
    t = ET.parse(os.path.join(d, 'AnimData.xml')).getroot()
    for a in t.find('Anims'):
        if a.findtext('Name') == anim:
            if a.find('CopyOf') is not None:
                return pmd_khung(dex, a.findtext('CopyOf'), huong, f)
            fw, fh = int(a.findtext('FrameWidth')), int(a.findtext('FrameHeight'))
            n = len(a.find('Durations'))
            im = Image.open(os.path.join(d, anim + '-Anim.png')).convert('RGBA')
            hang = min(PMD_HUONG.get(huong, huong) if isinstance(huong, int) and huong < 4 else huong, im.height // fh - 1)
            return im.crop(((f % n) * fw, hang * fh, (f % n + 1) * fw, (hang + 1) * fh))
    raise KeyError(dex + anim)


def chan_sprite(dex, anim, huong, f):
    """Khung đã cắt sát + toạ độ chân (giữa đáy) trong khung cắt."""
    k = pmd_khung(dex, anim, huong, f)
    bb = k.getbbox()
    c = k.crop(bb)
    return c


def anh_sang_vien(img, huong_sang=(1, -1), mau=(255, 214, 150), muc=0.55):
    """Viền sáng ấm phía mặt trời (pixel có lân cận phía nắng trong suốt)."""
    a = np.asarray(img, np.float32).copy()
    al = a[:, :, 3] > 0
    dx, dy = huong_sang
    lan = np.zeros_like(al)
    sh = np.roll(np.roll(al, -dy, 0), -dx, 1)
    lan = al & ~sh
    m = lan[:, :, None] * muc
    a[:, :, :3] = a[:, :, :3] * (1 - m) + np.array(mau, np.float32) * m
    return Image.fromarray(a.astype(np.uint8), 'RGBA')


def phong(img, k):
    return img.resize((img.width * k, img.height * k), Image.NEAREST)


def bong_den(img, mau, alpha=1.0):
    """Hình bóng một màu (silhouette)."""
    a = np.asarray(img, np.float32)
    out = np.zeros_like(a)
    out[:, :, :3] = mau
    out[:, :, 3] = (a[:, :, 3] > 0) * 255 * alpha
    return Image.fromarray(out.astype(np.uint8), 'RGBA')


# --------------------------------------------------------------------------- tile Ninja Adventure

_ninja = {}


def ninja(tep, x, y, w, h):
    if tep not in _ninja:
        _ninja[tep] = Image.open(os.path.join(NINJA, tep)).convert('RGBA')
    return _ninja[tep].crop((x, y, x + w, y + h))


def cay_mau():
    """Các kiểu cây (đủ dáng: thông cụm, cây tròn, cây to, cây có gốc) từ TilesetNature."""
    N = 'TilesetNature.png'
    return {
        'thong3': ninja(N, 0, 32, 64, 48), 'thong': ninja(N, 32, 0, 32, 32), 'tron': ninja(N, 0, 0, 32, 32),
        'cum': ninja(N, 64, 32, 64, 48), 'cum2': ninja(N, 256, 32, 64, 48), 'goc': ninja(N, 96, 0, 32, 32),
        'to': ninja(N, 48, 288, 48, 48), 'nho': ninja(N, 96, 128, 32, 32), 'tron2': ninja(N, 256, 0, 32, 32),
        'thu': ninja(N, 144, 288, 48, 48),
    }


def bui_mau():
    N = 'TilesetNature.png'
    return [ninja(N, x, 160, 16, 16) for x in (0, 16, 32, 48, 64, 80, 96, 112, 128, 144)]


def hoa_mau():
    N = 'TilesetNature.png'
    return [ninja(N, 0, 176, 16, 16), ninja(N, 16, 176, 16, 16), ninja(N, 48, 176, 16, 16), ninja(N, 96, 176, 16, 16)]


def to_lai(img, ram, vien=None, huong=(1, -1), vien_muc=0.9, sat=None):
    """Tô lại tile theo dải màu ram (tối -> sáng) theo độ sáng gốc; vien = màu viền sáng phía nắng."""
    a = np.asarray(img, np.float32)
    al = a[:, :, 3] > 0
    lum = (a[:, :, 0] * 0.3 + a[:, :, 1] * 0.55 + a[:, :, 2] * 0.15) / 255
    if al.any():
        lo, hi = np.percentile(lum[al], 5), np.percentile(lum[al], 95)
        lum = np.clip((lum - lo) / max(1e-3, hi - lo), 0, 0.999)
    idx = (lum * len(ram)).astype(int)
    out = np.zeros_like(a)
    for i, c in enumerate(ram):
        out[(idx == i) & al, :3] = hx(c)
    out[:, :, 3] = al * 255
    if vien is not None:
        dx, dy = huong
        sh = np.roll(np.roll(al, -dy, 0), -dx, 1)
        e = al & ~sh
        out[e, :3] = out[e, :3] * (1 - vien_muc) + hx(vien) * vien_muc
    return Image.fromarray(out.astype(np.uint8), 'RGBA')


# --------------------------------------------------------------------------- bầu trời

def mau_troi(t):
    t = np.clip(t, 0, 1)
    out = np.zeros(t.shape + (3,), np.float32)
    for i in range(len(TROI) - 1):
        a, ca = TROI[i]
        b, cb = TROI[i + 1]
        m = (t >= a) & (t <= b)
        k = ((t - a) / (b - a))[..., None]
        out[m] = (hx(ca) + (hx(cb) - hx(ca)) * k)[m]
    return out


def ve_troi(W, H, chan, mt, rnd, so_dai=22):
    """Trời: dải màu bậc thang có dither Bayer + quầng mặt trời + sao. chan = y chân trời."""
    l = Lop(W, H)
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    t = yy / chan
    # quầng nắng: kéo màu về phía chân trời quanh mặt trời
    sx, sy, sr = mt
    d = np.sqrt(((xx - sx) / 1.6) ** 2 + (yy - sy) ** 2)
    quang = np.clip(1 - d / (W * 0.42), 0, 1) ** 2
    t2 = np.clip(t + quang * 0.22, 0, 1)
    # lượng tử hoá thành dải + dither
    q = t2 * so_dai
    lo = np.floor(q)
    frac = q - lo
    qq = (lo + (frac > bayer(H, W))) / so_dai
    c = mau_troi(qq)
    # lõi sáng quanh mặt trời (cũng theo bậc)
    g = np.clip(1 - d / (sr * 6.5), 0, 1) ** 1.6
    gq = np.floor(g * 5 + bayer(H, W)) / 5
    c = c + (hx(0xFFF1C8) - c) * gq[..., None] * 0.75
    l.a[:, :, :3] = c / 255
    l.a[:, :, 3] = 1
    # sao: thưa dần về chân trời, tránh quầng nắng
    for _ in range(int(W * H / 900)):
        x, y = rnd.randrange(W), int(rnd.random() ** 1.8 * chan * 0.62)
        if quang[y, x] > 0.15:
            continue
        sang = 1 - y / (chan * 0.62)
        if rnd.random() > sang:
            continue
        mau = (255, 250, 230) if rnd.random() < 0.8 else (190, 210, 255)
        a = 0.45 + 0.55 * sang * rnd.random()
        l.a[y, x, :3] = l.a[y, x, :3] * (1 - a) + np.array(mau) / 255 * a
        if rnd.random() < 0.08 and 1 <= x < W - 1 and 1 <= y:
            for ex, ey in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                l.a[y + ey, x + ex, :3] = l.a[y + ey, x + ex, :3] * 0.6 + np.array(mau) / 255 * 0.4
    # mặt trời: đĩa + vạch ngang kiểu retro ở nửa dưới
    dm = np.sqrt((xx - sx) ** 2 + (yy - sy) ** 2)
    dia = dm <= sr
    l.to(dia, (255, 236, 186))
    l.to(dm <= sr - 2, (255, 248, 222))
    for k in range(4):
        yv = int(sy + sr * (0.15 + 0.22 * k))
        day = 1 + k // 2
        vach = dia & (yy >= yv) & (yy < yv + day)
        l.a[vach, :3] = mau_troi(np.full(vach.sum(), 0.93)) / 255
    return l


def may_hinh(w, h, rnd, kieu):
    """Mặt nạ 1 đám mây (bool h x w). kieu: 'tich' (mây bông, đáy phẳng) / 'dai' (mây dải mỏng)."""
    m = np.zeros((h, w), bool)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
    if kieu == 'dai':
        for i in range(max(3, w // 12)):
            cx = rnd.uniform(w * 0.08, w * 0.92)
            cy = rnd.uniform(h * 0.4, h * 0.65)
            rx = rnd.uniform(w * 0.1, w * 0.3) * (1 - abs(cx / w - 0.5))
            ry = rnd.uniform(h * 0.2, h * 0.36)
            m |= ((xx - cx) / max(rx, 2)) ** 2 + ((yy - cy) / ry) ** 2 <= 1
        return m & (yy < h * 0.7)
    day = h * 0.86
    # thân dưới bẹt
    m |= ((xx - w / 2) / (w * 0.48)) ** 2 + ((yy - day) / (h * 0.28)) ** 2 <= 1
    n = rnd.randint(3, 6)
    for i in range(n):
        u = (i + 0.5) / n
        cx = w * (0.14 + 0.72 * u) + rnd.uniform(-w * 0.04, w * 0.04)
        r = h * (0.2 + 0.36 * math.sin(math.pi * u) ** 0.8 * rnd.uniform(0.75, 1.1))
        cy = day - r * rnd.uniform(0.55, 0.85)
        m |= (xx - cx) ** 2 + (yy - cy) ** 2 <= r * r
    return m & (yy < day)


def to_may(l, m, x0, y0, sang, ram):
    """Tô mây ngược sáng: thân tím, đỉnh nhạt, bóng trong gần đáy, mép đáy bừng cam (sang 0..1 = gần mặt trời)."""
    h, w = m.shape
    H, W = l.h, l.w
    duoi = m & ~np.roll(m, -1, 0)
    duoi2 = m & ~np.roll(m, -2, 0) & ~duoi
    bong = m & ~np.roll(m, -5, 0) & ~duoi & ~duoi2
    tren = m & ~np.roll(m, 2, 0)
    c0 = hx(ram[0]) + (hx(ram[2]) - hx(ram[0])) * sang * 0.3
    lop_mau = ((m, c0), (tren, hx(ram[1])), (bong, c0 * 0.86), (duoi2, hx(ram[2])), (duoi, hx(ram[3])))
    for mask, c in lop_mau:
        ys, xs = np.nonzero(mask)
        X = (xs + x0) % W
        Y = ys + y0
        ok = (Y >= 0) & (Y < H)
        l.a[Y[ok], X[ok], :3] = c / 255
        l.a[Y[ok], X[ok], 3] = 1


def ve_may(W, H, chan, sx, rnd, so, cao_max, tile=True):
    """Lớp mây (lặp ngang khít nếu tile)."""
    l = Lop(W, H)
    ram_xa = [0x5B3F84, 0x7A5598, 0xE58A86, 0xFFC08A]
    ram_dai = [0x7B4A8E, 0x9A5E9A, 0xF09A80, 0xFFD39A]
    for i in range(so):
        kieu = 'dai' if rnd.random() < 0.45 else 'tich'
        w = rnd.randint(50, 130) if kieu == 'tich' else rnd.randint(80, 200)
        h = int(w * rnd.uniform(0.3, 0.42)) if kieu == 'tich' else rnd.randint(6, 10)
        x = int(W * (i + rnd.random() * 0.6) / so)
        y = rnd.randint(int(chan * 0.12), int(chan * cao_max))
        m = may_hinh(w, h, rnd, kieu)
        gan = max(0.0, 1 - abs(x + w / 2 - sx) / (W * 0.35))
        to_may(l, m, x, y, gan, ram_dai if y > chan * 0.45 else ram_xa)
    return l


# --------------------------------------------------------------------------- núi, rừng, hồ, cỏ

def song(x, cac, W):
    y = np.zeros_like(x, dtype=np.float32)
    for bd, sw, pha in cac:
        y += bd * np.sin(2 * np.pi * sw * x / W + pha)
    return y


def gay(n, rnd, do_nham=1.0, buoc=5):
    """Nhiễu răng cưa (midpoint đơn giản)."""
    p = np.zeros(n, np.float32)
    k = 64
    amp = do_nham
    while k >= 2:
        diem = np.array([rnd.uniform(-amp, amp) for _ in range(n // k + 2)], np.float32)
        p += np.interp(np.arange(n) / k, np.arange(len(diem)), diem)
        k //= 2
        amp *= 0.55
    return p


def nhieu_muot(n, rnd, k0=160, tat=0.5):
    """Nhiễu 1 chiều mượt nhiều tầng (giá trị ~ -1..1)."""
    p = np.zeros(n, np.float32)
    k, amp = k0, 1.0
    while k >= 3:
        diem = np.array([rnd.uniform(-1, 1) for _ in range(n // k + 3)], np.float32)
        xs = np.arange(n) / k
        i = np.floor(xs).astype(int)
        f = xs - i
        f = f * f * (3 - 2 * f)
        p += amp * (diem[i] * (1 - f) + diem[i + 1] * f)
        k //= 2
        amp *= tat
    return p


def ve_nui(W, H, chan, sx, rnd, cao=1.0):
    """Hai dãy núi xa: dãy sau nhạt gần màu sương (phối cảnh khí quyển), dãy trước tím xanh; thung lũng ở
    dưới mặt trời; sườn hướng nắng sáng hơn; viền sáng trên sống núi; mũ tuyết hồng trên đỉnh cao."""
    l = Lop(W, H)
    x = np.arange(W, dtype=np.float32)
    yy = np.arange(H, dtype=np.float32)[:, None]
    thung = np.clip(1 - np.abs(x - sx) / (W * 0.16), 0, 1) ** 1.4
    for lop_i, (cao_dinh, ram, vien, tuyet, suong, tl) in enumerate((
            (78 * cao, (0x8F64A6, 0xB07FB4), 0xFFD0B4, 0xFFE2D6, 0xEDA3A8, 0.9),
            (46 * cao, (0x4F4285, 0x6A5598), 0xFFB48E, None, 0xC07E9E, 0.75))):
        r = nhieu_muot(W, rnd, 200 if lop_i == 0 else 120, 0.52)
        nhon = np.abs(nhieu_muot(W, rnd, 40, 0.5))
        hdn = (0.55 + 0.45 * r) * cao_dinh + nhon * cao_dinh * 0.28
        hdn = hdn * (1 - thung * (0.92 if lop_i == 0 else 0.97))
        dinh = chan - np.maximum(hdn, 2)
        tron = np.convolve(np.pad(dinh, 6, mode='edge'), np.ones(13) / 13, 'same')[6:-6]
        doc = np.gradient(tron)
        m = yy >= dinh[None, :]
        phia = np.sign(sx - x)
        nang = np.clip(-doc * phia * 1.6, 0, 1)            # 0 = khuất, 1 = hướng nắng
        nq = np.floor(nang * 3 + bayer(1, W, lop_i)[0] * 0.999) / 3
        mau_x = hx(ram[0])[None, :] + (hx(ram[1]) - hx(ram[0]))[None, :] * nq[:, None]
        than = np.broadcast_to(mau_x[None, :, :], (H, W, 3)).copy()
        t = np.clip((yy - dinh[None, :]) / np.maximum(chan - dinh[None, :], 1), 0, 1)
        tq = np.floor(t * 5 + bayer(H, W, 3 * lop_i)) / 5
        than = than + (hx(suong) - than) * (tq[..., None] * tl)
        a = m.astype(np.float32)
        l.a[:, :, :3] = l.a[:, :, :3] * (1 - a[..., None]) + than / 255 * a[..., None]
        l.a[:, :, 3] = np.maximum(l.a[:, :, 3], a)
        gan = np.clip(1 - np.abs(x - sx) / (W * 0.5), 0.2, 1)
        yi = np.clip(dinh.astype(int), 0, H - 1)
        for xi in range(W):
            c = l.a[yi[xi], xi, :3]
            l.a[yi[xi], xi, :3] = c + (hx(vien) / 255 - c) * gan[xi] * 0.9
        if tuyet is not None:
            nguong = np.percentile(dinh, 10)
            for xi in range(W):
                if dinh[xi] < nguong:
                    sau = int((nguong - dinh[xi]) * 0.8) + 2
                    for k in range(1, sau):
                        yv = int(dinh[xi]) + k
                        if 0 <= yv < H and (k < sau - 1 or rnd.random() < 0.5):
                            mt = hx(tuyet) if nang[xi] > 0.3 else hx(0xC9A2C8)
                            l.a[yv, xi, :3] = mt / 255
    return l


def ve_rung(W, H, chan, sx, rnd, cay, k_cay=1, day_hang=(0, 1), ho=None):
    """Đồi rừng ngược sáng hai hàng (sau: cây nhỏ, pha sương; trước: cây to, gần như bóng tối) với viền
    nắng ấm trên tán phía mặt trời; quanh hồ (ho = (x0, x1)) rừng thưa và thấp để thấy chân trời."""
    l = Lop(W, H)
    x = np.arange(W, dtype=np.float32)
    yy = np.arange(H, dtype=np.float32)[:, None]
    hang = [
        dict(nen=chan - 3, bd=4, ram=(0x3A3468, 0x4A417A, 0x5D518A, 0x75659C), vien=0xFFB494, mat=0.4,
             ds=['thong', 'tron', 'nho', 'thong', 'tron2', 'goc'], suong=0xC98FA6, thua=0.62),
        dict(nen=chan + 3, bd=3, ram=(0x121731, 0x1A2140, 0x242E4E, 0x34405E), vien=0xFFB070, mat=0.55,
             ds=['thong3', 'to', 'cum', 'thong', 'thu', 'tron', 'cum2', 'thong3'], suong=None, thua=0.5),
    ]
    for hi in day_hang:
        hg = hang[hi]
        dinh = hg['nen'] + song(x, [(hg['bd'], 1.3, rnd.uniform(0, 6)), (hg['bd'] * 0.5, 3.1, rnd.uniform(0, 6))], W)
        ho_tam = ho_nua = None
        if ho is not None:
            ho_tam, ho_nua = (ho[0] + ho[1]) / 2, (ho[1] - ho[0]) / 2
            gx = np.clip(1 - np.abs(x - ho_tam) / (ho_nua + 20), 0, 1)
            dinh = dinh + gx ** 1.2 * (3 if hi == 0 else 6)
        m = yy >= dinh[None, :]
        l.a[:, :, :3] = np.where(m[..., None], hx(hg['ram'][1]) / 255, l.a[:, :, :3])
        l.a[:, :, 3] = np.maximum(l.a[:, :, 3], m)
        tiles = []
        xc = rnd.uniform(-20, 0)
        while xc < W + 20:
            ten = rnd.choice(hg['ds'])
            t = cay[ten]
            if k_cay > 1:
                t = phong(t, k_cay)
            if ho is not None:
                d = abs(xc - ho_tam) / ho_nua
                # gần giữa hồ: chỉ hàng sau, cây nhỏ, thưa
                if d < 0.75 and (hi == 1 or rnd.random() < 0.55 or t.height > 32):
                    xc += 10 + rnd.random() * 14
                    continue
            gan = max(0.0, 1 - abs(xc - sx) / (W * 0.45))
            tt = to_lai(t, hg['ram'], hg['vien'], (1 if xc < sx else -1, -1), 0.35 + 0.6 * gan)
            yv = int(dinh[int(np.clip(xc, 0, W - 1))]) - t.height + 5 + rnd.randint(0, 4)
            tiles.append((yv + t.height, int(xc - t.width / 2), tt))
            xc += t.width * rnd.uniform(0.3, hg['thua'])
        for _, xv, tt in sorted(tiles, key=lambda q: q[0]):
            l.dan(tt, xv, _ - tt.height)
        if hg['suong'] is not None:
            t = np.clip((yy - (hg['nen'] - 34)) / 44, 0, 1)
            tq = np.floor(t * 4 + bayer(H, W, 1, 2)) / 4 * hg['mat']
            al = l.a[:, :, 3:] > 0
            l.a[:, :, :3] = np.where(al, l.a[:, :, :3] + (hx(hg['suong']) / 255 - l.a[:, :, :3]) * tq[..., None], l.a[:, :, :3])
    return l


def phan_chieu(nen, chan, y0, y1, x0, x1, W, rnd, sx, mat_bo):
    """Hồ: phản chiếu lật ngược mọi thứ phía trên mặt nước (nen = ảnh RGBA đã ghép tới hàng rừng),
    tối + xanh hơn, gợn sóng ngang, vệt nắng lấp lánh dưới mặt trời. mat_bo = mặt nạ mặt hồ."""
    a = np.asarray(nen, np.float32) / 255
    H = a.shape[0]
    out = np.zeros_like(a)
    for y in range(y0, y1):
        d = y - chan
        ys = int(np.clip(chan - 1 - d * 1.0, 0, H - 1))
        song_x = int(round(math.sin(y * 0.9) * (1 + (y - y0) / 10)))
        out[y] = np.roll(a[ys], song_x, axis=0)
    out[:, :, :3] = out[:, :, :3] * 0.72 + hx(0x2A2C6A) / 255 * 0.28
    # dải gợn tối / sáng xen kẽ
    for y in range(y0, y1):
        if (y - y0) % 3 == 2:
            out[y, :, :3] *= 0.86
    # vệt nắng
    for y in range(y0, y1):
        t = (y - y0) / max(1, (y1 - y0))
        rong = 6 + t * 46
        for _ in range(int(3 + t * 6)):
            if rnd.random() < 0.8:
                cx = sx + rnd.gauss(0, rong * 0.45)
                dl = rnd.randint(2, 4 + int(t * 9))
                xs = int(cx - dl / 2)
                mau = hx(0xFFF4C8 if rnd.random() < 0.4 else 0xFFC27A) / 255
                out[y, max(0, xs):max(0, xs + dl), :3] = mau
    out[:, :, 3] = mat_bo
    return out


def ve_lop_ho(W, H, chan, sx, rnd, nen_tren, ho):
    """Lớp hồ (mặt nước + mép bờ sáng)."""
    x0, x1, y1 = ho
    l = Lop(W, H)
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    # bờ hồ: elip bẹt có răng cưa
    cx = (x0 + x1) / 2
    rx = (x1 - x0) / 2
    nh = gay(W, rnd, 2.0)
    bo = chan + (y1 - chan) * np.sqrt(np.clip(1 - ((np.arange(W) - cx) / rx) ** 2, 0, 1)) + nh * 0.8
    mat = (yy >= chan) & (yy <= bo[None, :])
    pc = phan_chieu(nen_tren, chan, chan, int(bo.max()) + 2, x0, x1, W, rnd, sx, mat.astype(np.float32))
    l.a = pc
    # mép nước sáng ở chân trời + mép bờ
    l.to(mat & (yy == chan), (255, 214, 160))
    duoi = mat & ~np.roll(mat, -1, 0)
    l.to(duoi, (245, 186, 140), 0.8)
    return l, bo


def ve_co(W, H, chan_co, rnd, loi, sx, k_hoa=1, chi_tiet=1.0):
    """Thảm cỏ: dải màu theo độ sâu (xa: vàng ấm bắt nắng, gần: xanh tối lạnh), mảng sáng tối lớn,
    ngọn cỏ thưa to dần về gần, lối mòn đất dẫn vào cảnh (loi = hàm y -> (x_tam, nua_rong))."""
    l = Lop(W, H)
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    m = yy >= chan_co[None, :]
    y0 = chan_co.min()
    sau = np.clip((yy - y0) / (H - y0), 0, 1) ** 0.8
    ram = [0xE2B566, 0xB4A453, 0x7F9449, 0x587E42, 0x3C6539, 0x284C31, 0x1A3528]
    # mảng sáng / tối lớn (nhiễu 2 chiều thô)
    r2 = random.Random(rnd.random())
    th = np.array([[r2.uniform(-1, 1) for _ in range(W // 24 + 3)] for _ in range(H // 12 + 3)], np.float32)
    ty = np.clip(((yy - y0) / 12), 0, th.shape[0] - 2)
    tx = xx / 24
    iy, ix = ty.astype(int), tx.astype(int)
    fy, fx = ty - iy, tx - ix
    n2 = (th[iy, ix] * (1 - fx) + th[iy, ix + 1] * fx) * (1 - fy) + (th[iy + 1, ix] * (1 - fx) + th[iy + 1, ix + 1] * fx) * fy
    q = np.clip(sau * (len(ram) - 1) + n2 * 0.5, 0, len(ram) - 1.001)
    lo = np.floor(q)
    f = q - lo
    # chỉ dither ở dải chuyển màu hẹp (giữa hai bậc) -> mảng màu lớn, yên
    lo = np.clip(lo + (f > 0.5 + (bayer(H, W) - 0.5) * 0.35), 0, len(ram) - 1).astype(int)
    c = np.zeros((H, W, 3), np.float32)
    for i, mau in enumerate(ram):
        c[lo == i] = hx(mau)
    l.a[:, :, :3] = np.where(m[..., None], c / 255, 0)
    l.a[:, :, 3] = m
    mep = m & ~np.roll(m, 1, 0)
    l.to(mep, (255, 226, 160))
    if loi is not None:
        for y in range(H):
            r = loi(y)
            if r is None:
                continue
            xc, nua = r
            t = np.clip((y - y0) / (H - y0), 0, 1)
            xa = np.arange(W)
            d = np.abs(xa - xc)
            trong = (d <= nua) & m[y]
            mau = tron(hx(0xF6CB8E), hx(0x8E6448), t ** 0.8)
            # nửa phía mặt trời sáng hơn
            mau2 = tron(hx(0xFFE0A8), hx(0xA87656), t ** 0.8)
            mep_c = tron(hx(0xB98660), hx(0x4A3430), t ** 0.8)
            l.a[y, trong, :3] = mau / 255
            l.a[y, trong & (xa > xc + nua * 0.15), :3] = mau2 / 255
            l.a[y, trong & (d > nua - max(1, nua * 0.16)), :3] = mep_c / 255
            if nua > 4 and rnd.random() < 0.3 * chi_tiet:
                xp = int(xc + rnd.uniform(-nua * 0.6, nua * 0.6))
                if 0 <= xp < W - 1:
                    l.a[y, xp:xp + 1 + int(t * 2), :3] = tron(hx(0xD8A878), hx(0x5A4036), t) / 255
    # ngọn cỏ: chùm 3 nét, thưa; đầu ngọn sáng phía nắng
    so = int(W * (H - y0) / 60 * chi_tiet)
    for _ in range(so):
        x = rnd.randrange(2, W - 2)
        y = rnd.randrange(int(chan_co[x]) + 2, H)
        t = (y - chan_co[x]) / (H - chan_co[x] + 1)
        if loi is not None:
            r = loi(y)
            if r is not None and abs(x - r[0]) < r[1] + 2:
                continue
        cao = 1 + int(t * 4.5)
        toi = tron(hx(0x9AA04E), hx(0x10261C), t ** 0.9) / 255
        sang = tron(hx(0xFFE59A), hx(0x5C9A52), t ** 0.9) / 255
        for dx, dc in ((0, 0), (-1, -1 - int(t * 2)), (1, -int(t * 2))):
            hh = cao + dc
            for kk in range(max(1, hh)):
                yv = y - kk
                if yv >= chan_co[x + dx]:
                    l.a[yv, x + dx, :3] = sang if (kk == hh - 1 and dx >= 0) else toi
    return l


def ve_tien_canh(W, H, rnd, cay, bui, k=1, goc_tren=True, vung=None, so_bui=6, co_lon=(3, 3, 4)):
    """Khung tiền cảnh tối ở hai mép vùng nhìn thấy vung=(x0, x1): tán cây rủ ở hai góc trên, bụi + cỏ cao ở
    hai góc dưới; viền sáng mảnh phía mặt trời. Lặp lại ở mép khung gốc cho màn rộng."""
    l = Lop(W, H)
    ram = (0x0A0E22, 0x121934, 0x1C2444, 0x2A3456)
    vien = 0xF4A27A
    x0, x1 = vung if vung else (0, W)
    if goc_tren:
        for xt, huong, cao_than in ((x0 + 3 * k, 1, 0.80), (x1 - 2 * k, -1, 0.68)):
            # thân cây cong nhẹ từ đáy lên tán
            top = int(H * (1 - cao_than))
            for y in range(top, H):
                u = (y - top) / (H - top)
                nua = (9 + 8 * u ** 2) * k
                xc = xt + huong * (-8 * k * (1 - u) ** 2)
                xa = np.arange(int(xc - nua), int(xc + nua) + 1)
                xa = xa[(xa >= 0) & (xa < W)]
                l.a[y, xa, :3] = hx(ram[1]) / 255
                l.a[y, xa, 3] = 1
                xs = int(xc + nua) if huong > 0 else int(xc - nua)
                if 0 <= xs < W:
                    l.a[y, xs, :3] = hx(0x6B4A5E) / 255
            # tán: cụm tile phóng to quanh ngọn
            for ten, s_, dx, dy in (('to', 3, -50, -64), ('cum', 2, 6, -34), ('tron2', 2, 30, -58), ('cum2', 2, -60, -14)):
                t = phong(cay[ten], s_ * k)
                tt = to_lai(t, ram, vien, (huong, -1), 0.5)
                l.dan(tt, int(xt + huong * dx * k - t.width / 2), int(top + dy * k - t.height / 2 + 30 * k))
    for gx, huong in ((x0, 1), (x1, -1)):
        x = gx - huong * 6 * k
        n = 0
        while n < so_bui:
            b = rnd.choice(bui)
            s = rnd.choice(co_lon) * k if n < 2 else 2 * k
            bb = to_lai(phong(b, s), ram, vien, (huong, -1), 0.3)
            xv = int(x - (bb.width if huong < 0 else 0))
            yv = H - bb.height + rnd.randint(int(3 * k), int(9 * k)) + n * k
            l.dan(bb, xv, yv)
            x += huong * bb.width * rnd.uniform(0.35, 0.55)
            n += 1
    return l


# --------------------------------------------------------------------------- Pokémon

def ve_khoi_dau(W, H, cx, y_chan, k, rnd, so_huong=4, rim=True, k_lui=1.0):
    """5 Pokémon khởi đầu theo DOI_HINH, phóng k lần (nguyên), chân trên đường y_chan. Trả (lớp, vị trí)."""
    l = Lop(W, H)
    vt = []
    ds = sorted(DOI_HINH, key=lambda d: -d[2])  # sau trước, gần sau
    for cls, dx, lui, huong, anim, f in ds:
        sp = chan_sprite(KHOI_DAU[cls], anim, huong, f)
        if rim:
            sp = anh_sang_vien(sp, (1, -1), (255, 220, 160), 0.45)
        sp = phong(sp, k)
        x = int(cx + dx * k * 0.5 * k_lui)
        y = int(y_chan - lui * k * 0.5 * k_lui)
        # bóng chân
        yy, xx = np.mgrid[0:H, 0:W]
        rx, ry = sp.width * 0.42, max(2, 3 * k * 0.5)
        e = ((xx - x) / rx) ** 2 + ((yy - y) / ry) ** 2 <= 1
        l.to(e, (14, 22, 30), 0.38)
        l.dan(sp, x - sp.width // 2, y - sp.height + int(k * 0.5))
        vt.append((cls, x, y, huong))
    return l, vt


def ve_hoang_da(l, rnd, cac, k=1):
    """Pokémon hoang dã (cảnh giữa) - pha sương nhẹ cho đúng độ xa."""
    for dex, anim, huong, f, x, y, suong in cac:
        sp = chan_sprite(dex, anim, huong, f)
        if k != 1:
            sp = phong(sp, k)
        a = np.asarray(sp, np.float32)
        a[:, :, :3] = a[:, :, :3] * (1 - suong) + np.array([150, 110, 150]) * suong
        sp = Image.fromarray(a.astype(np.uint8), 'RGBA')
        l.dan(sp, x - sp.width // 2, y - sp.height)


def ve_huyen_thoai(W, H, x, y, k, alpha=0.5):
    """Bóng Rayquaza lượn trên trời: tối, hơi trong, viền nắng rất mảnh."""
    l = Lop(W, H)
    sp = chan_sprite('0384', 'Shoot', 3, 2)
    sp = phong(sp, k)
    b = anh_sang_vien(bong_den(sp, (34, 24, 70), 1.0), (1, 1), (255, 170, 140), 0.5)
    l.dan(b, x - b.width // 2, y - b.height // 2)
    l.a[:, :, 3] *= alpha
    return l


def ve_chim(l, rnd, cum, mau=(40, 28, 70)):
    for x, y, n in cum:
        for i in range(n):
            bx = int(x + rnd.uniform(-22, 22))
            by = int(y + rnd.uniform(-9, 9))
            s = rnd.choice((1, 1, 2))
            for dx, dy in ((-s, -1), (0, 0), (s, -1), (-2 * s, -1 - (s > 1)), (2 * s, -1 - (s > 1))):
                if 0 <= by + dy < l.h and 0 <= bx + dx < l.w:
                    l.a[by + dy, bx + dx, :3] = np.array(mau) / 255
                    l.a[by + dy, bx + dx, 3] = 1


def anh_sang(W, H, sx, sy, chan):
    """Lớp ánh sáng mềm (không pixel): quầng nắng + tia sáng chéo + tối góc. RGBA, dùng chế độ phủ thường."""
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    d = np.sqrt((xx - sx) ** 2 + ((yy - sy) * 1.3) ** 2)
    g = np.clip(1 - d / (W * 0.5), 0, 1) ** 2.2
    ang = np.arctan2(yy - sy, xx - sx)
    tia = (0.5 + 0.5 * np.sin(ang * 13 + 1.3)) ** 6 * np.clip(1 - d / (W * 0.6), 0, 1) * (yy < chan + 30)
    a = np.clip(g * 0.42 + tia * 0.10, 0, 0.6)
    out = np.zeros((H, W, 4), np.float32)
    out[:, :, :3] = hx(0xFFD9A0) / 255
    out[:, :, 3] = a
    # tối góc (vignette) - lớp riêng ghép cùng
    v = np.sqrt(((xx - W / 2) / (W * 0.62)) ** 2 + ((yy - H * 0.45) / (H * 0.75)) ** 2)
    vg = np.clip((v - 0.62) / 0.5, 0, 1) ** 1.5 * 0.55
    mix = out[:, :, 3] + vg * (1 - out[:, :, 3])
    rgb = (out[:, :, :3] * out[:, :, 3:] + hx(0x0A0C22) / 255 * vg[..., None] * (1 - out[:, :, 3:])) / np.maximum(mix[..., None], 1e-4)
    o = np.zeros_like(out)
    o[:, :, :3] = rgb
    o[:, :, 3] = mix
    return Image.fromarray((np.clip(o, 0, 1) * 255).astype(np.uint8), 'RGBA')


# --------------------------------------------------------------------------- dựng cảnh

def dung_canh(W, H, k=2, seed=7, chan=None, ho=None, cay_k=1, khung_goc=True, kieu='ngang', vung=None):
    """Dựng đủ lớp cho khung W x H. k = hệ số phóng Pokémon khởi đầu. Trả dict tên -> Image RGBA (W x H)
    + thông tin bố cục."""
    rnd = random.Random(seed)
    cay = cay_mau()
    bui = bui_mau()
    hoa = hoa_mau()
    if chan is None:
        chan = int(H * (0.585 if kieu == 'ngang' else 0.60))
    sx = int(W * 0.56)
    sr = max(6, int(min(W, H) * 0.04))
    sy = chan - int(sr * 0.35)
    lop = {}
    lop['troi'] = ve_troi(W, H, chan, (sx, sy, sr), rnd).anh()
    lop['may'] = ve_may(W, H, chan, sx, random.Random(seed + 1), max(4, W // 110), 0.62).anh()
    # huyền thoại: trên trời phía trái, tránh logo giữa
    rx = int(W * 0.255) if kieu == 'ngang' else int(W * 0.28)
    ry = int(chan * 0.37) if kieu == 'ngang' else int(chan * 0.50)
    lop['huyen_thoai'] = ve_huyen_thoai(W, H, rx, ry, max(1, k // 2), 0.5).anh()
    lop['nui'] = ve_nui(W, H, chan, sx, random.Random(seed + 2), cao={'ngang': H / 400, 'doc': H / 560, 'j2me': 0.95}[kieu]).anh()
    if ho is None:
        ho = (int(W * 0.30), int(W * 0.80), chan + int(H * 0.085))
    rung = ve_rung(W, H, chan, sx, random.Random(seed + 3), cay, cay_k, (0, 1), (ho[0], ho[1]))
    nen_tren = chong(lop['troi'], lop['nui'], rung.anh())
    l_ho, bo = ve_lop_ho(W, H, chan, sx, random.Random(seed + 4), nen_tren, ho)
    # cỏ: mép cỏ = bờ hồ (trong khoảng hồ) hoặc chân rừng
    x = np.arange(W, dtype=np.float32)
    chan_co = np.where((x > ho[0] - 2) & (x < ho[1] + 2), np.maximum(bo + 1, chan + 3), chan + 6 + 2 * np.sin(x / 23))
    chan_co = np.minimum(chan_co, np.maximum(bo + 1, chan + 6))
    y_khoi = int(H * 0.86) if kieu == 'ngang' else int(H * 0.84)
    cx = W // 2

    def loi(y):
        y0 = int(np.interp(cx + 30, x, chan_co)) + 1
        if y < y0:
            return None
        t = (y - y0) / (H - y0)
        xc = cx + 30 * (1 - t) ** 1.5 - 24 * math.sin(t * math.pi) * (W / 880 if kieu == 'ngang' else 0.6)
        return xc, 1.5 + t ** 1.3 * W * 0.07
    co = ve_co(W, H, chan_co, random.Random(seed + 5), loi, sx)
    # hoang dã
    hd = Lop(W, H)
    if kieu == 'ngang':
        ve_hoang_da(hd, rnd, [('0131', 'Idle', 2, 0, int(ho[1] - (ho[1] - ho[0]) * 0.18), chan + int(H * 0.05), 0.35),
                              ('0012', 'Idle', 3, 1, int(W * 0.255), chan - int(H * 0.11), 0.15),
                              ('0179', 'Idle', 2, 2, int(W * 0.83), chan + int(H * 0.13), 0.12)])
    elif kieu == 'doc':
        ve_hoang_da(hd, rnd, [('0131', 'Idle', 2, 0, int(ho[1] - (ho[1] - ho[0]) * 0.22), chan + int(H * 0.04), 0.35),
                              ('0012', 'Idle', 3, 1, int(W * 0.2), chan - int(H * 0.06), 0.15)])
    else:  # j2me: dải thấp, chỉ Lapras trên hồ
        ve_hoang_da(hd, rnd, [('0131', 'Idle', 2, 0, int(ho[1] - (ho[1] - ho[0]) * 0.2), chan + int(H * 0.06), 0.3)])
    ht = Lop(W, H)
    ve_chim(ht, random.Random(seed + 9), [(int(W * 0.70), int(chan * 0.58), 3), (int(W * 0.78), int(chan * 0.48), 2)])
    lop['huyen_thoai'] = chong(lop['huyen_thoai'], ht.anh())
    # hoa: chấm nhỏ xa, cụm hoa tile 1x ở hai bên gần (ngoài lối mòn, ngoài chỗ 5 khởi đầu)
    hc = Lop(W, H)
    r2 = random.Random(seed + 6)
    vx0, vx1 = vung if vung else (0, W)
    for _ in range(int(W / 6)):
        xh = r2.randrange(W)
        yh = r2.randrange(int(chan_co[xh]) + 3, int(chan_co[xh] + (H - chan_co[xh]) * 0.45))
        r = loi(yh)
        if r is not None and abs(xh - r[0]) < r[1] + 3:
            continue
        c = r2.choice((0xFFE6F0, 0xFFD15A, 0xFF8FA8, 0xFFFFFF, 0xFFB0D0))
        hc.a[yh, xh, :3] = hx(c) / 255
        hc.a[yh, xh, 3] = 1
    for phia in (-1, 1):
        for _ in range(9):
            xh = int(cx + phia * r2.uniform((vx1 - vx0) * 0.26, (vx1 - vx0) * 0.47))
            yh = int(r2.uniform(chan + (H - chan) * 0.45, H - 4))
            t_img = r2.choice(hoa)
            arr = np.asarray(t_img, np.float32).copy()
            t = (yh - chan) / (H - chan)
            arr[:, :, :3] *= 1.05 - 0.35 * t
            hc.dan(Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), 'RGBA'), xh - 8, yh - 16)
    lop['rung'] = chong(rung.anh(), l_ho.anh(), hd.anh())
    lop['dat'] = chong(co.anh(), hc.anh())
    kd, vt = ve_khoi_dau(W, H, cx, y_khoi, k, rnd, k_lui=0.74 if kieu == 'doc' else 1.0)
    lop['khoi_dau'] = kd.anh()
    lop['khung'] = ve_tien_canh(W, H, random.Random(seed + 7), cay, bui, max(1, k // 2), khung_goc, vung,
                                *((3, (2, 2, 2)) if kieu == 'j2me' else (6, (3, 3, 4)))).anh()
    lop['anh_sang'] = anh_sang(W, H, sx, sy, chan)
    info = dict(W=W, H=H, chan=chan, mat_troi=[sx, sy, sr], y_khoi=y_khoi, cx=cx, k=k, khoi_dau=vt, ho=list(ho))
    return lop, info


THU_TU = ['troi', 'may', 'huyen_thoai', 'nui', 'rung', 'dat', 'khoi_dau', 'khung', 'anh_sang']


def ghep(lop, bo=()):
    return chong(*[lop[t] for t in THU_TU if t in lop and t not in bo])


def cat(img, x, y, w, h):
    return img.crop((x, y, x + w, y + h))


def phong_to(img, k):
    return img.resize((img.width * k, img.height * k), Image.NEAREST)


def ghep_hd(lop, x, y, w, h, k, logo=None, y_logo=None, rong_logo=None):
    """Ảnh hoàn chỉnh độ phân giải cao: các lớp pixel phóng k (láng giềng gần) + ánh sáng mềm phóng mượt."""
    px = ghep(lop, ('anh_sang',))
    out = phong_to(cat(px, x, y, w, h), k)
    sang = cat(lop['anh_sang'], x, y, w, h).resize((w * k, h * k), Image.BICUBIC)
    out.alpha_composite(sang)
    if logo is not None:
        lw = rong_logo or int(w * k * 0.44)
        lh = int(logo.height * lw / logo.width)
        lg = logo.resize((lw, lh), Image.LANCZOS)
        out.alpha_composite(lg, ((w * k - lw) // 2, y_logo if y_logo is not None else int(h * k * 0.04)))
    return out.convert('RGB')


def luu_bang_mau(img, duong, mau=48):
    a = np.asarray(img.getchannel('A'))
    q = img.convert('RGB').quantize(colors=mau - 1, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
    arr = np.asarray(q).copy()
    arr[a < 128] = mau - 1
    out = Image.fromarray(arr.astype(np.uint8), 'P')
    pal = q.getpalette()[:3 * (mau - 1)]
    out.putpalette(pal + [255, 0, 255] + [0] * (3 * (256 - mau)))
    out.save(duong, optimize=True, transparency=mau - 1)


def cat_dai(img, y0, y1):
    """Cắt dải ngang [y0, y1) rồi bỏ hàng trống phía trên."""
    d = img.crop((0, y0, img.width, y1))
    bb = d.getbbox()
    if bb is None:
        return d, 0
    return d.crop((0, bb[1], d.width, d.height)), bb[1]


def main():
    arg = sys.argv
    global NINJA, PMD

    def lay(ten, md=None):
        return arg[arg.index(ten) + 1] if ten in arg else md
    NINJA = lay('--ninja', NINJA)
    PMD = lay('--pmd', PMD)
    web = lay('--web')
    xem = lay('--xem')
    lop_dir = os.path.join(BANNER, 'lop')
    doc_dir = os.path.join(BANNER, 'doc')
    hd_dir = os.path.join(BANNER, 'res', 'bn', 'hd')
    bn = os.path.join(REPO, 'j2me', 'res', 'bn')
    for d in (lop_dir, doc_dir, hd_dir, bn):
        os.makedirs(d, exist_ok=True)
    logo = Image.open(os.path.join(BRAND, 'logo_1024.png')).convert('RGBA') if os.path.exists(os.path.join(BRAND, 'logo_1024.png')) else None
    logo_gon = None

    # ---- khung gốc 880x400
    lop, info = dung_canh(MW, MH, k=2, vung=(C169[0], C169[0] + C169[2]))
    for t in THU_TU:
        lop[t].save(os.path.join(lop_dir, t + '.png'), optimize=True)
    with open(os.path.join(lop_dir, 'lop.txt'), 'w', encoding='utf-8') as f:
        f.write('# ten,toc_do_px_giay,do_sau(0..1),kieu  - sinh boi client_jar/tools/tao_banner.py\n'
                '# kieu: phu = phu man (cat giua, khong lap) | lap = lap ngang + troi | sang = lop anh sang (loc muot)\n'
                '# khung goc %dx%d; vung 16:9 = x%d y%d %dx%d (chieu cao hien = 360 px goc)\n' % ((MW, MH) + C169))
        for t, toc, sau, kieu in (('troi', 0, 0, 'phu'), ('may', 4, 0.08, 'lap'), ('huyen_thoai', 0, 0.12, 'phu'),
                                  ('nui', 0, 0.2, 'phu'), ('rung', 0, 0.4, 'phu'), ('dat', 0, 0.7, 'phu'),
                                  ('khoi_dau', 0, 0.85, 'phu'), ('khung', 0, 1.0, 'phu'), ('anh_sang', 0, 0, 'sang')):
            f.write('%s,%s,%s,%s\n' % (t, toc, sau, kieu))
    with open(os.path.join(lop_dir, 'bo_cuc.json'), 'w', encoding='utf-8') as f:
        json.dump(dict(info, khung_169=C169, doi_hinh=DOI_HINH, khoi_dau_dex=[int(d) for d in KHOI_DAU]), f, ensure_ascii=False, indent=1)
    x, y, w, h = C169
    # ảnh hoàn chỉnh (không chữ) + poster (có logo)
    for (W, H), k in (((1920, 1080), 3), ((2560, 1440), 4), ((1280, 720), 2)):
        ghep_hd(lop, x, y, w, h, k).save(os.path.join(BANNER, 'keyart_%dx%d.png' % (W, H)), optimize=True)
        if logo is not None:
            ghep_hd(lop, x, y, w, h, k, logo, int(H * 0.035), int(W * 0.40)).save(os.path.join(BANNER, 'poster_%dx%d.png' % (W, H)), optimize=True)
    # Android ngang: 20:9 (2400x1080 = 800x360 x3) và 16:9
    ghep_hd(lop, (MW - 800) // 2, y, 800, 360, 3, logo, 40, 760).save(os.path.join(BANNER, 'android_2400x1080.png'), optimize=True)
    ghep_hd(lop, x, y, w, h, 3, logo, 40, 760).save(os.path.join(BANNER, 'android_1920x1080.png'), optimize=True)
    # OG 1200x630 = 600x315 x2
    if logo is not None:
        og = ghep_hd(lop, (MW - 600) // 2, MH - 315 - 6, 600, 315, 2, logo, 22, 560)
        og.save(os.path.join(BANNER, 'og_1200x630.png'), optimize=True)
    # màn tải: không có 5 khởi đầu, logo giữa
    for (W, H), k in (((1920, 1080), 3), ((1280, 720), 2)):
        ghep_hd({t: v for t, v in lop.items() if t != 'khoi_dau'}, x, y, w, h, k, logo, int(H * 0.2), int(W * 0.46)) \
            .save(os.path.join(BANNER, 'tai_%dx%d.png' % (W, H)), optimize=True)
    # tương thích cũ
    shutil.copy(os.path.join(BANNER, 'poster_1920x1080.png' if logo is not None else 'keyart_1920x1080.png'), os.path.join(BANNER, 'banner_1920x1080.png'))
    ghep(lop).crop((x, y, x + w, y + h)).convert('RGB').save(os.path.join(BANNER, 'banner_640x360.png'), optimize=True)
    for cu in ('dat', 'doi', 'may1', 'may2', 'nui', 'banner_480x270'):
        p = os.path.join(BANNER, cu + '.png')
        if os.path.exists(p):
            os.remove(p)

    # ---- APK (chế độ HD của Banner.java): cảnh tĩnh ghép sẵn + mây trôi + khung
    nen = chong(lop['troi'], lop['huyen_thoai'], lop['nui'], lop['rung'], lop['dat'])
    sang = lop['anh_sang']
    nen_s = nen.copy()
    nen_s.alpha_composite(sang)
    nen_s.convert('RGB').save(os.path.join(hd_dir, 'nen.png'), optimize=True)
    m_bb = lop['may'].getbbox()
    lop['may'].crop((0, m_bb[1], MW, m_bb[3])).save(os.path.join(hd_dir, 'may.png'), optimize=True)
    kh = lop['khung']
    kh.save(os.path.join(hd_dir, 'khung.png'), optimize=True)
    with open(os.path.join(hd_dir, 'bo_cuc.txt'), 'w') as f:
        # Banner.java đọc: rộng cao chan y_khoi cx k mayY
        f.write('%d %d %d %d %d %d %d\n' % (MW, MH, info['chan'], info['y_khoi'], info['cx'], 2, m_bb[1]))

    # ---- màn dọc (web điện thoại) 360x640, Pokémon x2
    lop_d, info_d = dung_canh(360, 640, k=2, seed=11, kieu='doc')
    for t in THU_TU:
        lop_d[t].save(os.path.join(doc_dir, t + '.png'), optimize=True)
    with open(os.path.join(doc_dir, 'bo_cuc.json'), 'w', encoding='utf-8') as f:
        json.dump(info_d, f, ensure_ascii=False, indent=1)
    ghep_hd(lop_d, 0, 0, 360, 640, 3, logo, 60, 960).save(os.path.join(BANNER, 'poster_doc_1080x1920.png'), optimize=True)

    # ---- J2ME: dải rộng 320, Pokémon 1x (vẽ bằng code trên lớp tĩnh)
    lop_j, info_j = dung_canh(320, 240, k=1, seed=5, cay_k=1, khung_goc=False, ho=(96, 268, 152), kieu='j2me')
    chan_j = info_j['chan']
    yk = info_j['y_khoi']
    # nui: từ đỉnh núi tới chân trời; rung: từ đỉnh rừng tới mép cỏ xa nhất; dat: tới đáy
    nui_j, y_nui = cat_dai(lop_j['nui'], 0, chan_j + 2)
    rung_j, y_rung = cat_dai(lop_j['rung'], 0, MH)
    dat_j, y_dat = cat_dai(lop_j['dat'], 0, 240)
    may_j = lop_j['may']
    bbm = may_j.getbbox()
    may_j = may_j.crop((0, bbm[1], 320, bbm[3]))
    rong_j = lop_j['huyen_thoai'].crop(lop_j['huyen_thoai'].getbbox())
    khung_j = lop_j['khung'].crop((0, 240 - 40, 320, 240))
    luu_bang_mau(nui_j, os.path.join(bn, 'nui.png'), 32)
    luu_bang_mau(rung_j, os.path.join(bn, 'rung.png'), 64)
    luu_bang_mau(dat_j, os.path.join(bn, 'dat.png'), 48)
    luu_bang_mau(may_j, os.path.join(bn, 'may.png'), 16)
    # bóng Rayquaza: ghép sẵn với màu trời trung bình (J2ME không có alpha nửa trong) -> 1 màu tối
    luu_bang_mau(bong_den(rong_j, (74, 50, 120)), os.path.join(bn, 'rong.png'), 4)
    luu_bang_mau(khung_j, os.path.join(bn, 'khung.png'), 16)
    for cu in ('doi', 'may1', 'may2'):
        p = os.path.join(bn, cu + '.png')
        if os.path.exists(p):
            os.remove(p)
    with open(os.path.join(bn, 'bo_cuc.txt'), 'w') as f:
        # Banner.java đọc: y (tính từ chân trời) của đỉnh dải nui/rung/dat, khoảng chân 5 khởi đầu trên chân trời
        f.write('%d %d %d %d\n' % (y_nui - chan_j, y_rung - chan_j, y_dat - chan_j, yk - chan_j))
    tong = sum(os.path.getsize(os.path.join(bn, t)) for t in os.listdir(bn))
    # ---- web
    if web:
        pub = os.path.join(web, 'public', 'keyart')
        os.makedirs(os.path.join(pub, 'doc'), exist_ok=True)
        for t in THU_TU:
            shutil.copy(os.path.join(lop_dir, t + '.png'), os.path.join(pub, t + '.png'))
            shutil.copy(os.path.join(doc_dir, t + '.png'), os.path.join(pub, 'doc', t + '.png'))
        shutil.copy(os.path.join(lop_dir, 'bo_cuc.json'), os.path.join(pub, 'bo_cuc.json'))
        shutil.copy(os.path.join(doc_dir, 'bo_cuc.json'), os.path.join(pub, 'doc', 'bo_cuc.json'))
        ghep_hd(lop, x, y, w, h, 2).save(os.path.join(pub, 'keyart_1280x720.jpg'), quality=88)
        # ảnh tĩnh ĐỦ khung gốc (cùng hình học với các lớp: website vẽ ảnh này trước, lớp parallax phủ lên sau)
        ghep_hd(lop, 0, 0, MW, MH, 2).save(os.path.join(pub, 'nen_ngang.jpg'), quality=86, optimize=True, progressive=True)
        ghep_hd(lop_d, 0, 0, 360, 640, 2).save(os.path.join(pub, 'nen_doc.jpg'), quality=86, optimize=True, progressive=True)
        if logo is not None:
            shutil.copy(os.path.join(BANNER, 'og_1200x630.png'), os.path.join(web, 'public', 'og', 'htbb.png'))
    print('OK', BANNER, 'jar bn:', tong, 'byte')


if __name__ == '__main__':
    main()
