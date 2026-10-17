#!/usr/bin/env python3
"""Tạo ảnh nền banner (màn đăng nhập / máy chủ / chọn & tạo nhân vật) - chạy một lần, cần Pillow.

    python3 j2me/tools/tao_banner.py <ninja_full> <pk>
      <ninja_full> = HTBB/tools/pokemon/pmd_raw_tile_moi/ninja_full (tile Ninja Adventure, CC0)
      <pk>         = j2me/res/pk (sprite PMD của 5 Pokémon khởi đầu, chỉ để ghép ảnh xem trước)

Ghi:
  shared/banner/   lớp nguồn rộng 240 px (nui, doi, dat, may1, may2) + banner_1920x1080.png (ghép đủ
                   lớp + 5 Pokémon khởi đầu + tên game) để bản Android / PC dùng lại;
  j2me/res/bn/     các lớp đó, PNG bảng màu nhỏ (bản NHẸ chỉ giữ dat.png - xem build.sh).
Bầu trời (dải màu bình minh), quầng mặt trời, mây trôi, đom đóm và Pokémon đứng thở vẽ bằng code
(game.pokemon.Banner). Không dùng ảnh Pokémon chính thức: núi / đồi / mây vẽ thủ tục, cây cỏ lấy
từ bộ tile Ninja Adventure (CC0).
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFont

W = 240
GOC = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(GOC, '..', '..'))


def luu(img, duong, mau=48):
    """PNG bảng màu (giữ trong suốt) cho jar nhỏ."""
    a = img.getchannel('A')
    rgb = img.convert('RGB')
    q = rgb.quantize(colors=mau - 1, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
    pal = q.getpalette()[:3 * (mau - 1)]
    # chỉ số 'mau-1' = trong suốt
    px = q.load()
    ap = a.load()
    out = Image.new('P', img.size)
    out.putpalette(pal + [255, 0, 255] + [0] * (3 * (256 - mau)))
    op = out.load()
    for y in range(img.size[1]):
        for x in range(img.size[0]):
            op[x, y] = mau - 1 if ap[x, y] < 128 else px[x, y]
    out.info['transparency'] = mau - 1
    out.save(duong, optimize=True, transparency=mau - 1)


def tron(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def hex3(h):
    return ((h >> 16) & 255, (h >> 8) & 255, h & 255)


def bien_dang(x, cac_song):
    """Đường viền tuần hoàn chu kỳ W (tile ngang khít)."""
    y = 0.0
    for bien_do, so_song, pha in cac_song:
        y += bien_do * math.sin(2 * math.pi * so_song * x / W + pha)
    return y


def tao_nui():
    h = 60
    img = Image.new('RGBA', (W, h), (0, 0, 0, 0))
    p = img.load()
    than = hex3(0x7E6AA6)
    toi = hex3(0x5E4F8A)
    tuyet = hex3(0xF3E4F2)
    vien = hex3(0xB49BD0)
    song = [(9, 2, 0.4), (6, 3, 1.7), (4, 5, 0.2), (2, 9, 2.1)]
    dinh = [h - 30 + bien_dang(x, song) * 1.25 for x in range(W)]
    for x in range(W):
        top = int(dinh[x])
        for y in range(max(0, top), h):
            d = y - top
            # sườn khuất bóng (dốc lên bên phải -> bóng bên trái đỉnh)
            doc = dinh[(x + 4) % W] - dinh[x - 4]
            c = toi if doc < -1.5 else (tron(toi, than, 0.5) if doc < 0.5 else than)
            if d <= 1:
                c = vien
            if top < 22 and d < 22 - top and d < 6:
                c = tuyet
            # sương mù dưới chân núi
            if y > h - 14:
                c = tron(c, hex3(0xD9A0B8), (y - (h - 14)) / 16)
            p[x, y] = c + (255,)
    return img


def tao_doi():
    h = 48
    img = Image.new('RGBA', (W, h), (0, 0, 0, 0))
    p = img.load()
    co = hex3(0x4E8C6E)
    cay = hex3(0x3C6F5A)
    sang = hex3(0x6FAE84)
    song = [(5, 2, 2.2), (3, 3, 0.9), (2, 7, 1.3)]
    dinh = [h - 24 + bien_dang(x, song) for x in range(W)]
    rnd = random.Random(7)
    cac_cay = [(rnd.randrange(W), rnd.randrange(3, 7)) for _ in range(26)]
    for x in range(W):
        top = int(dinh[x])
        for y in range(max(0, top), h):
            c = sang if y - top <= 1 else co
            if y > h - 10:
                c = tron(c, hex3(0x86B48C), (y - (h - 10)) / 14)
            p[x, y] = c + (255,)
    # rặng cây tròn trên sườn đồi (vẽ tuần hoàn)
    for cx, r in cac_cay:
        cy = int(dinh[cx]) - r + 3
        for dx in range(-r, r + 1):
            for dy in range(-r, r + 1):
                if dx * dx + dy * dy <= r * r:
                    x = (cx + dx) % W
                    y = cy + dy
                    if 0 <= y < h:
                        c = cay if dy > -r // 2 else tron(cay, sang, 0.45)
                        p[x, y] = c + (255,)
    return img


def tao_may(rong, cao, seed):
    img = Image.new('RGBA', (rong, cao), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rnd = random.Random(seed)
    trang = (255, 250, 246, 255)
    bong = (240, 206, 214, 255)
    n = 4 + rong // 18
    vong = []
    for i in range(n):
        cx = int(cao / 2 + (rong - cao) * i / (n - 1))
        r = rnd.randint(cao // 4, cao // 2 - 1)
        cy = cao - r - 2 - rnd.randint(0, cao // 5)
        vong.append((cx, cy, r))
    for cx, cy, r in vong:
        d.ellipse([cx - r, cy - r + 2, cx + r, cy + r + 2], fill=bong)
    for cx, cy, r in vong:
        d.ellipse([cx - r, cy - r, cx + r, cy + r - 1], fill=trang)
    d.rectangle([cao // 3, cao - 5, rong - cao // 3, cao - 3], fill=bong)
    return img


def cat(tep, x, y, w, h):
    return tep.crop((x, y, x + w, y + h))


def tao_dat(ninja):
    """Tiền cảnh: thảm cỏ + cây, bụi, hoa từ TilesetNature (Ninja Adventure, CC0)."""
    h = 64
    nat = Image.open(os.path.join(ninja, 'TilesetNature.png')).convert('RGBA')
    img = Image.new('RGBA', (W, h), (0, 0, 0, 0))
    co = hex3(0x78C850)
    co_toi = hex3(0x4E9A3C)
    co_sang = hex3(0xA4DE6E)
    dat_toi = hex3(0x3B7A30)
    song = [(2, 3, 0.5), (1, 7, 1.1)]
    mat = [22 + bien_dang(x, song) for x in range(W)]
    # cây phía sau (đứng trên mép cỏ)
    cay_to = [cat(nat, 48, 288, 48, 48), cat(nat, 0, 0, 32, 32), cat(nat, 288, 0, 32, 32), cat(nat, 0, 288, 48, 48)]
    cho = [(14, 0), (70, 1), (118, 3), (176, 2), (214, 1)]
    for x, k in cho:
        t = cay_to[k]
        img.alpha_composite(t, (x - t.width // 2, int(mat[x % W]) + 6 - t.height))
    p = img.load()
    rnd = random.Random(11)
    for x in range(W):
        top = int(mat[x])
        for y in range(top, h):
            d = y - top
            c = co_sang if d <= 1 else (co if d < 14 else tron(co, co_toi, min(1, (d - 14) / 22)))
            p[x, y] = c + (255,)
        # ngọn cỏ lởm chởm trên mép
        if rnd.random() < 0.35 and top - 1 >= 0:
            p[x, top - 1] = co_sang + (255,)
    # vệt cỏ / bóng
    for _ in range(90):
        x = rnd.randrange(W)
        y = rnd.randrange(int(mat[x]) + 6, h - 2)
        p[x, y] = (co_toi if rnd.random() < 0.6 else co_sang) + (255,)
        p[(x + 1) % W, y - 1] = (co_toi if rnd.random() < 0.6 else co_sang) + (255,)
    for _ in range(18):
        x = rnd.randrange(W)
        y = rnd.randrange(int(mat[x]) + 18, h - 1)
        p[x, y] = dat_toi + (255,)
    # bụi + hoa trên thảm cỏ
    bui = [cat(nat, 0, 160, 16, 16), cat(nat, 32, 160, 16, 16), cat(nat, 64, 160, 16, 16), cat(nat, 112, 160, 16, 16)]
    hoa = [cat(nat, 0, 176, 16, 16), cat(nat, 16, 176, 16, 16), cat(nat, 48, 176, 16, 16)]
    for x in (40, 96, 150, 198, 232):
        t = bui[x % len(bui)]
        img.alpha_composite(t, (x - 8, int(mat[x % W]) - 9))
    for x in (8, 30, 58, 84, 108, 134, 162, 186, 206, 226):
        t = hoa[x % len(hoa)]
        y = int(mat[x % W]) + 20 + (x * 7) % 22
        if y + 16 <= h:
            img.alpha_composite(t, (x - 8, y))
    return img


def ve_bau_troi(w, h, chan_troi):
    img = Image.new('RGBA', (w, h))
    p = img.load()
    moc = [(0.0, hex3(0x26306C)), (0.35, hex3(0x6C4F9C)), (0.62, hex3(0xE07A78)), (0.82, hex3(0xFFB27A)), (1.0, hex3(0xFFE0A6))]
    for y in range(h):
        t = min(1.0, y / chan_troi)
        for i in range(len(moc) - 1):
            if moc[i][0] <= t <= moc[i + 1][0]:
                c = tron(moc[i][1], moc[i + 1][1], (t - moc[i][0]) / (moc[i + 1][0] - moc[i][0]))
                break
        for x in range(w):
            p[x, y] = c + (255,)
    return img


def anh_dung(pk, khoa):
    """Khung đứng đầu tiên (hướng xuống) của sprite PMD đã đóng gói: lấy ô có hình đầu tiên."""
    im = Image.open(os.path.join(pk, khoa + '_0.png')).convert('RGBA')
    # ảnh lưới 4 hàng hướng x N cột; hàng 0 = hướng xuống
    cao = im.height // 4
    hang = im.crop((0, 0, im.width, cao))
    bb = hang.getbbox()
    if bb is None:
        return None
    # ô đầu: chia theo khoảng trống cột
    cot = [any(hang.getpixel((x, y))[3] > 0 for y in range(cao)) for x in range(im.width)]
    x0 = cot.index(True)
    x1 = x0
    while x1 < len(cot) and cot[x1]:
        x1 += 1
    return hang.crop((x0, 0, x1, cao)).crop(hang.crop((x0, 0, x1, cao)).getbbox())


def main():
    ninja = sys.argv[1]
    pk = sys.argv[2] if len(sys.argv) > 2 else os.path.join(REPO, 'j2me', 'res', 'pk')
    ra_chung = os.path.join(REPO, 'shared', 'banner')
    ra_jar = os.path.join(REPO, 'j2me', 'res', 'bn')
    os.makedirs(ra_chung, exist_ok=True)
    os.makedirs(ra_jar, exist_ok=True)
    lop = {
        'nui': tao_nui(),
        'doi': tao_doi(),
        'dat': tao_dat(ninja),
        'may1': tao_may(56, 22, 3),
        'may2': tao_may(36, 15, 5),
    }
    for ten, im in lop.items():
        im.save(os.path.join(ra_chung, ten + '.png'))
        luu(im, os.path.join(ra_jar, ten + '.png'), 64 if ten == 'dat' else 24)
    # ---- ảnh ghép 480x270 -> phóng x4 = 1920x1080 (pixel art)
    w, h = 480, 270
    nen = ve_bau_troi(w, h, 200)
    # quầng mặt trời
    mt = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    mp = mt.load()
    sx, sy = 330, 168
    for y in range(h):
        for x in range(w):
            d = math.hypot(x - sx, y - sy)
            if d < 16:
                mp[x, y] = (255, 246, 214, 255)
            elif d < 70:
                mp[x, y] = (255, 236, 190, int(150 * (1 - (d - 16) / 54) ** 2))
    nen.alpha_composite(mt)
    for x, y, k in ((30, 34, 'may1'), (190, 22, 'may2'), (300, 60, 'may1'), (420, 40, 'may2'), (120, 80, 'may2')):
        nen.alpha_composite(lop[k], (x, y))
    for ox in (0, W):
        nen.alpha_composite(lop['nui'], (ox, 140))
        nen.alpha_composite(lop['doi'], (ox, 176))
        nen.alpha_composite(lop['dat'], (ox, h - 64))
    # 5 Pokémon khởi đầu đứng trên cỏ
    for i, khoa in enumerate(('0004', '0001', '0133', '0007', '0172')):
        try:
            a = anh_dung(pk, khoa)
        except Exception:
            a = None
        if a is None:
            continue
        cx = 120 + i * 60
        nen.alpha_composite(a, (cx - a.width // 2, h - 22 - a.height))
    to = nen.resize((1920, 1080), Image.NEAREST)
    # tên game
    d = ImageDraw.Draw(to)
    try:
        f = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 96)
    except Exception:
        f = ImageFont.load_default()
    chu = 'Huyền Thoại Bảo Bối'
    bw = d.textbbox((0, 0), chu, font=f)
    x = (1920 - (bw[2] - bw[0])) // 2
    for dx in range(-6, 7, 2):
        for dy in range(-6, 7, 2):
            d.text((x + dx, 90 + dy), chu, font=f, fill=(30, 42, 68))
    d.text((x, 90), chu, font=f, fill=(255, 203, 5))
    to.convert('RGB').save(os.path.join(ra_chung, 'banner_1920x1080.png'), optimize=True)
    nen.save(os.path.join(ra_chung, 'banner_480x270.png'))
    print('OK', ra_chung, ra_jar, sum(os.path.getsize(os.path.join(ra_jar, t)) for t in os.listdir(ra_jar)), 'byte jar')


if __name__ == '__main__':
    main()
