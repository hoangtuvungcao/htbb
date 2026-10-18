#!/usr/bin/env python3
"""Logo + biểu tượng thương hiệu "Huyền Thoại Bảo Bối" (một nguồn cho mọi bản: J2ME, Android, PC, web).

    python3 client_jar/tools/tao_logo.py [--web <HTBB-Web>]
      cần: Pillow, numpy, fontTools (pip install fonttools), google-chrome (hoặc CHROME=...) để vẽ SVG -> PNG

Chữ: font Baloo 2 ExtraBold (SIL OFL 1.1, client_jar/tools/phong/) đổi thành đường path -> SVG không phụ thuộc
font máy xem. Không dùng logo / font Pokémon chính thức: wordmark tự dựng (chữ vàng -> cam viền xanh navy,
Poké Ball thay chữ O của "BẢO", dải ruy băng "THẾ GIỚI POKÉMON MMORPG").

Ghi:
  client_jar/shared/brand/logo.svg                 wordmark đầy đủ (web, in ấn)
  client_jar/shared/brand/logo_{1024,512,256,128}.png
  client_jar/shared/brand/logo_px_{240,160,120}.png  bản pixel art (tô theo dải màu, viền 1-2 px) cho màn nhỏ
  client_jar/shared/brand/bieu_tuong.svg + icon_{16..1024}.png + favicon.ico + htbb.ico; client_pc_ios/Assets/Icon/app_icon.png (256)
  client_jar/res/bn/logo.png (160 px) + logo_nho.png (120 px)      -> jar (bảng màu nhỏ)
  client_jar/shared/banner/res/bn/hd/logo.png (240 px)                 -> APK (màn logic ~640x360)
  client_apk/pokemon/src/main/res/{mipmap,drawable}-*/ic_launcher*  (adaptive icon) + drawable-nodpi/man_cho_logo.png (màn chờ)
  --web: public/logo.svg, logo-gon.svg, logo-mark.svg, favicon.ico, favicon-32.png, apple-touch-icon.png, icon-192/512.png
"""
import io
import math
import os
import shutil
import subprocess
import sys
import tempfile

import numpy as np
from PIL import Image, ImageDraw

GOC = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(GOC, '..', '..'))
FONT = os.path.join(GOC, 'phong', 'Baloo2-wght.ttf')
RA = os.path.join(REPO, 'shared', 'brand')

# ---- bảng màu thương hiệu (dùng chung với tao_banner.py)
NAVY = '#141a3c'
XANH = '#2b5fd0'
XANH_SANG = '#5d93ff'
BONG = '#0a0f2c'
VANG = ['#fff7b0', '#ffe452', '#ffc824', '#ff9f1c', '#f26a1b']
DO = '#e8312b'
DO_SANG = '#ff6a4d'
DO_TOI = '#9e1520'
TRANG = '#ffffff'
XAM = '#d6dcef'

TEN1 = 'HUYỀN THOẠI'
TEN2 = 'BẢO BỐI'
PHU_DE = 'THẾ GIỚI POKÉMON MMORPG'


def hex2rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


# --------------------------------------------------------------------------- font -> path

_font = None


def font():
    global _font
    if _font is None:
        from fontTools.ttLib import TTFont
        from fontTools.varLib import instancer
        f = TTFont(FONT)
        _font = instancer.instantiateVariableFont(f, {'wght': 800})
    return _font


def chu_path(s, co, x0, y_chan, gian=0.0):
    """Path SVG của chuỗi s (cỡ co), gốc trái x0, đường chân y_chan. Trả (d, rong, [(x, rong_ky_tu)])."""
    from fontTools.pens.svgPathPen import SVGPathPen
    from fontTools.pens.transformPen import TransformPen
    f = font()
    cmap = f.getBestCmap()
    gs = f.getGlyphSet()
    hmtx = f['hmtx']
    k = co / f['head'].unitsPerEm
    x = x0
    ds = []
    vt = []
    for ch in s:
        g = cmap.get(ord(ch))
        if g is None:
            continue
        adv = hmtx[g][0] * k
        if ch != ' ':
            pen = SVGPathPen(gs, ntos=lambda v: ('%.2f' % v).rstrip('0').rstrip('.'))
            gs[g].draw(TransformPen(pen, (k, 0, 0, -k, x, y_chan)))
            ds.append(pen.getCommands())
        vt.append((x, adv))
        x += adv + gian * co
    return ' '.join(ds), x - x0 - gian * co, vt


def rong_chu(s, co, gian=0.0):
    return chu_path(s, co, 0, 0, gian)[1]


# --------------------------------------------------------------------------- bố cục wordmark

def bo_cuc(phu_de=True):
    """Toạ độ mọi phần của logo trong hệ đơn vị (khung W x H). Trả dict."""
    co1, co2 = 118, 196
    gian1, gian2 = 0.02, 0.01
    hoa = 0.602  # chiều cao chữ hoa / cỡ chữ (Baloo 2)
    # dòng 2: "BẢ" + bóng + " BỐI"
    w_ba = rong_chu('BẢ', co2, gian2)
    d_bong = hoa * co2 * 1.12
    w_khoang = rong_chu(' ', co2) * 0.55
    w_boi = rong_chu('BỐI', co2, gian2)
    w2 = w_ba + co2 * 0.03 + d_bong + w_khoang + w_boi
    w1 = rong_chu(TEN1, co1, gian1)
    W = int(max(w1, w2, rong_chu(PHU_DE, 44, 0.06) + 90 + 2 * 74 if phu_de else 0) + 110)
    cx = W / 2
    y1 = 40 + co1 * 0.62 + hoa * co1        # chân dòng 1 (chừa dấu chồng Ề / Ạ)
    y2 = y1 + 26 + co2 * 0.40 + hoa * co2   # chân dòng 2 (dấu hỏi Ả, dấu sắc + mũ Ố)
    x2 = cx - w2 / 2
    p = {
        'W': W, 'wnoi': max(w1, w2), 'co1': co1, 'co2': co2, 'y1': y1, 'y2': y2,
        'l1': chu_path(TEN1, co1, cx - w1 / 2, y1, gian1)[0],
        'l2a': chu_path('BẢ', co2, x2, y2, gian2)[0],
        'l2b': chu_path('BỐI', co2, x2 + w_ba + co2 * 0.03 + d_bong + w_khoang, y2, gian2)[0],
        'bong': (x2 + w_ba + co2 * 0.015 + d_bong / 2, y2 - hoa * co2 / 2 - 2, d_bong / 2),
        'cap1': (y1 - hoa * co1, y1), 'cap2': (y2 - hoa * co2, y2),
    }
    H = y2 + 54
    if phu_de:
        co3 = 44
        w3 = rong_chu(PHU_DE, co3, 0.06)
        rb_w = max(w3 + 90, w2 * 0.9)
        rb_y = y2 + 30
        rb_h = 74
        p['rb'] = (cx - rb_w / 2, rb_y, rb_w, rb_h)
        p['l3'] = chu_path(PHU_DE, co3, cx - w3 / 2, rb_y + rb_h / 2 + hoa * co3 / 2 + 1, 0.06)[0]
        H = rb_y + rb_h * 1.35 + 30
    p['H'] = int(H)
    return p


def svg_bong(cx, cy, r, net, mau_net, mau_ngoai=None, chi_bong=False):
    """Poké Ball. chi_bong: chỉ hình tròn (lớp viền)."""
    if chi_bong:
        return f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r:.1f}"/>'
    s = []
    s.append(f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r:.1f}" fill="url(#gTrang)"/>')
    s.append(f'<path d="M{cx - r:.1f} {cy:.1f}A{r:.1f} {r:.1f} 0 0 1 {cx + r:.1f} {cy:.1f}Z" fill="url(#gDo)"/>')
    # vệt sáng
    s.append(f'<path d="M{cx - r * 0.68:.1f} {cy - r * 0.32:.1f}A{r * 0.74:.1f} {r * 0.74:.1f} 0 0 1 {cx - r * 0.18:.1f} {cy - r * 0.72:.1f}" '
             f'fill="none" stroke="{TRANG}" stroke-opacity=".7" stroke-width="{r * 0.13:.1f}" stroke-linecap="round"/>')
    s.append(f'<rect x="{cx - r:.1f}" y="{cy - r * 0.11:.1f}" width="{2 * r:.1f}" height="{r * 0.22:.1f}" fill="{mau_net}"/>')
    s.append(f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r * 0.36:.1f}" fill="{mau_net}"/>')
    s.append(f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r * 0.22:.1f}" fill="{TRANG}"/>')
    s.append(f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r * 0.12:.1f}" fill="{XAM}"/>')
    s.append(f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r:.1f}" fill="none" stroke="{mau_net}" stroke-width="{net:.1f}"/>')
    return ''.join(s)


def sao4(cx, cy, r, mau):
    k = r * 0.28
    return (f'<path d="M{cx:.1f} {cy - r:.1f}L{cx + k:.1f} {cy - k:.1f}L{cx + r:.1f} {cy:.1f}L{cx + k:.1f} {cy + k:.1f}'
            f'L{cx:.1f} {cy + r:.1f}L{cx - k:.1f} {cy + k:.1f}L{cx - r:.1f} {cy:.1f}L{cx - k:.1f} {cy - k:.1f}Z" fill="{mau}"/>')


def ruy_bang(x, y, w, h, kieu):
    """kieu: 'than' (thân), 'duoi' (hai đuôi gấp sau thân)."""
    if kieu == 'duoi':
        d = h * 0.55
        e = h * 0.35
        trai = f'M{x + 10:.1f} {y + e:.1f}H{x - d:.1f}L{x - d + e:.1f} {y + e + h / 2:.1f}L{x - d:.1f} {y + e + h:.1f}H{x + 10:.1f}Z'
        phai = f'M{x + w - 10:.1f} {y + e:.1f}H{x + w + d:.1f}L{x + w + d - e:.1f} {y + e + h / 2:.1f}L{x + w + d:.1f} {y + e + h:.1f}H{x + w - 10:.1f}Z'
        return trai + phai
    return f'M{x:.1f} {y:.1f}H{x + w:.1f}V{y + h:.1f}H{x:.1f}Z'


def logo_svg(phu_de=True, chi_mat_na=None, dam=0.0):
    """SVG wordmark. chi_mat_na: None = đủ màu; 'chu' = chỉ phần tô chữ (trắng, để làm bản pixel);
    'bong' = chỉ hình tròn bóng; 'ngoai' = cả khối (chữ + bóng) để đo."""
    p = bo_cuc(phu_de)
    W, H = p['W'], p['H']
    bx, by, br = p['bong']
    chu = p['l1'] + ' ' + p['l2a'] + ' ' + p['l2b']
    if chi_mat_na == 'chu':
        body = f'<path d="{chu}" fill="#fff" stroke="#fff" stroke-width="{dam:.2f}" stroke-linejoin="round"/>'
        return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">{body}</svg>', W, H
    if chi_mat_na == 'bong':
        body = f'<circle cx="{bx:.1f}" cy="{by:.1f}" r="{br:.1f}" fill="#fff"/>'
        return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">{body}</svg>', W, H
    c1, c2 = p['cap1'], p['cap2']

    def grad(ten, y0, y1):
        cac = [(0.00, VANG[0]), (0.16, VANG[0]), (0.16, VANG[1]), (0.44, VANG[1]), (0.44, VANG[2]),
               (0.68, VANG[2]), (0.68, VANG[3]), (0.86, VANG[3]), (0.86, VANG[4]), (1, VANG[4])]
        st = ''.join(f'<stop offset="{o}" stop-color="{c}"/>' for o, c in cac)
        return f'<linearGradient id="{ten}" gradientUnits="userSpaceOnUse" x1="0" y1="{y0:.1f}" x2="0" y2="{y1:.1f}">{st}</linearGradient>'

    defs = [grad('g1', c1[0], c1[1]), grad('g2', c2[0], c2[1] + 4),
            f'<linearGradient id="gDo" gradientUnits="userSpaceOnUse" x1="0" y1="{by - br:.1f}" x2="0" y2="{by:.1f}">'
            f'<stop offset="0" stop-color="{DO_SANG}"/><stop offset=".45" stop-color="{DO_SANG}"/><stop offset=".45" stop-color="{DO}"/>'
            f'<stop offset=".8" stop-color="{DO}"/><stop offset=".8" stop-color="{DO_TOI}"/></linearGradient>',
            f'<linearGradient id="gTrang" gradientUnits="userSpaceOnUse" x1="0" y1="{by:.1f}" x2="0" y2="{by + br:.1f}">'
            f'<stop offset="0" stop-color="#fff"/><stop offset=".6" stop-color="#fff"/><stop offset=".6" stop-color="{XAM}"/></linearGradient>',
            f'<linearGradient id="gRb" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{DO_SANG}"/><stop offset=".3" stop-color="{DO_SANG}"/>'
            f'<stop offset=".3" stop-color="{DO}"/><stop offset=".78" stop-color="{DO}"/><stop offset=".78" stop-color="{DO_TOI}"/></linearGradient>']
    # mỗi đường chữ khai báo MỘT lần (defs) rồi dùng lại bằng <use> -> SVG nhỏ
    defs.append(f'<path id="c1" d="{p["l1"]}"/><path id="c2" d="{p["l2a"]} {p["l2b"]}"/>')
    nen = '<use href="#c1"/><use href="#c2"/>' + svg_bong(bx, by, br, 0, '', chi_bong=True)
    s = []
    # bóng đổ + viền xanh ngoài + viền navy
    s.append(f'<g transform="translate(0 12)" fill="{BONG}" stroke="{BONG}" stroke-width="44" stroke-linejoin="round">{nen}</g>')
    s.append(f'<g fill="{XANH}" stroke="{XANH}" stroke-width="44" stroke-linejoin="round">{nen}</g>')
    s.append(f'<g fill="{NAVY}" stroke="{NAVY}" stroke-width="20" stroke-linejoin="round">{nen}</g>')
    # mép sáng trên viền xanh (vệt 3D)
    s.append(f'<g transform="translate(0 -5)" fill="none" stroke="{XANH_SANG}" stroke-width="6" stroke-linejoin="round" opacity=".55">'
             '<use href="#c1"/><use href="#c2"/></g>')
    s.append(f'<g fill="{NAVY}" stroke="{NAVY}" stroke-width="20" stroke-linejoin="round">{nen}</g>')
    # tô chữ (dải màu bậc thang kiểu pixel)
    s.append('<use href="#c1" fill="url(#g1)"/>')
    s.append('<use href="#c2" fill="url(#g2)"/>')
    s.append(svg_bong(bx, by, br, 9, NAVY))
    # lấp lánh
    s.append(sao4(bx + br * 1.02, by - br * 0.98, br * 0.34, '#fffbe0'))
    s.append(sao4(bx + br * 1.42, by - br * 0.58, br * 0.16, '#ffe452'))
    if phu_de:
        x, y, w, h = p['rb']
        s.insert(0, f'<g stroke="{NAVY}" stroke-width="14" stroke-linejoin="round"><path d="{ruy_bang(x, y, w, h, "duoi")}" fill="{DO_TOI}"/>'
                    f'<path d="{ruy_bang(x, y, w, h, "than")}" fill="url(#gRb)"/></g>'
                    f'<path transform="translate(0 8)" d="{ruy_bang(x, y, w, h, "than")}" fill="{BONG}" opacity=".35"/>')
        # thân ruy băng nằm DƯỚI chữ dòng 2 nhưng trên đuôi: vẽ lại thân + chữ phụ đề ở cuối
        s.append(f'<path d="{ruy_bang(x, y, w, h, "than")}" fill="url(#gRb)" stroke="{NAVY}" stroke-width="10" stroke-linejoin="round"/>')
        defs.append(f'<path id="c3" d="{p["l3"]}"/>')
        s.append(f'<use href="#c3" fill="{NAVY}" stroke="{NAVY}" stroke-width="10" stroke-linejoin="round"/>')
        s.append(f'<use href="#c3" fill="{TRANG}"/>')
    body = ''.join(s)
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}" role="img" '
           f'aria-label="Huyền Thoại Bảo Bối">'
           f'<title>Huyền Thoại Bảo Bối</title><defs>{"".join(defs)}</defs>{body}</svg>')
    return svg, W, H


def bieu_tuong_svg(nen_troi=True, chi_nen=False):
    """Biểu tượng vuông 512: nền hoàng hôn (giống key art) + Poké Ball viền navy/xanh như logo."""
    N = 512
    s = []
    defs = ''
    if nen_troi:
        defs = ('<linearGradient id="gT" x1="0" y1="0" x2="0" y2="1">'
                '<stop offset="0" stop-color="#1d2154"/><stop offset=".35" stop-color="#4b3482"/>'
                '<stop offset=".62" stop-color="#c2507a"/><stop offset=".82" stop-color="#ff9a52"/>'
                '<stop offset="1" stop-color="#ffd27a"/></linearGradient>'
                '<radialGradient id="gS" cx=".5" cy=".86" r=".5"><stop offset="0" stop-color="#fff3c4" stop-opacity=".95"/>'
                '<stop offset="1" stop-color="#ffb35c" stop-opacity="0"/></radialGradient>')
        s.append(f'<rect width="{N}" height="{N}" fill="url(#gT)"/>')
        s.append(f'<rect width="{N}" height="{N}" fill="url(#gS)"/>')
        # dãy núi + đồi
        s.append('<path d="M0 420L70 352L120 384L196 300L262 372L318 330L388 380L452 318L512 360V512H0Z" fill="#5a4b8f" opacity=".85"/>')
        s.append('<path d="M0 452Q90 410 170 440T340 432T512 428V512H0Z" fill="#26304f"/>')
        for x, y, r in ((70, 70, 3), (120, 40, 2), (430, 60, 3), (470, 120, 2), (380, 30, 2), (40, 150, 2)):
            s.append(f'<circle cx="{x}" cy="{y}" r="{r}" fill="#fff" opacity=".8"/>')
    if chi_nen:
        return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {N} {N}" width="{N}" height="{N}">'
                f'<defs>{defs}</defs>{"".join(s)}</svg>')
    cx, cy, r = N / 2, N * 0.47, N * 0.33
    defs += (f'<linearGradient id="gDo" gradientUnits="userSpaceOnUse" x1="0" y1="{cy - r}" x2="0" y2="{cy}">'
             f'<stop offset="0" stop-color="{DO_SANG}"/><stop offset=".45" stop-color="{DO_SANG}"/><stop offset=".45" stop-color="{DO}"/>'
             f'<stop offset=".8" stop-color="{DO}"/><stop offset=".8" stop-color="{DO_TOI}"/></linearGradient>'
             f'<linearGradient id="gTrang" gradientUnits="userSpaceOnUse" x1="0" y1="{cy}" x2="0" y2="{cy + r}">'
             f'<stop offset="0" stop-color="#fff"/><stop offset=".6" stop-color="#fff"/><stop offset=".6" stop-color="{XAM}"/></linearGradient>')
    s.append(f'<circle cx="{cx}" cy="{cy + 14}" r="{r + 22}" fill="{BONG}" opacity=".55"/>')
    s.append(f'<circle cx="{cx}" cy="{cy}" r="{r + 22}" fill="{XANH}"/>')
    s.append(f'<circle cx="{cx}" cy="{cy}" r="{r + 10}" fill="{NAVY}"/>')
    s.append(svg_bong(cx, cy, r, 4, NAVY))
    s.append(sao4(cx + r * 0.95, cy - r * 0.95, r * 0.3, '#fffbe0'))
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {N} {N}" width="{N}" height="{N}">'
            f'<defs>{defs}</defs>{"".join(s)}</svg>')


# --------------------------------------------------------------------------- SVG -> PNG (Chrome)

def ve_svg(svg, w, h, ra):
    """Vẽ SVG ra PNG trong suốt w x h bằng Chrome headless."""
    chrome = os.environ.get('CHROME', 'google-chrome')
    d = tempfile.mkdtemp(prefix='logo-')
    try:
        html = os.path.join(d, 'a.html')
        svg2 = svg.replace('<svg ', f'<svg style="display:block;width:{w}px;height:{h}px" preserveAspectRatio="xMidYMid meet" ', 1)
        with open(html, 'w', encoding='utf-8') as f:
            f.write(f'<!doctype html><html><body style="margin:0;background:transparent">{svg2}</body></html>')
        subprocess.run([chrome, '--headless=new', '--disable-gpu', '--no-sandbox', '--hide-scrollbars',
                        '--default-background-color=00000000', f'--window-size={w},{h + 240}', f'--user-data-dir={d}/u',
                        f'--screenshot={ra}', 'file://' + html], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=120)
    finally:
        shutil.rmtree(d, ignore_errors=True)
    # khung cửa sổ headless cao hơn vùng nhìn: chụp dư rồi cắt đúng w x h
    im = Image.open(ra).convert('RGBA').crop((0, 0, w, h))
    im.save(ra)
    return im


def png_tu_svg(svg, W, H, rong, ra):
    cao = round(H * rong / W)
    t = os.path.join(tempfile.gettempdir(), 'htbb_logo_%d.png' % os.getpid())
    S = max(2, -(-1600 // rong))
    im = ve_svg(svg, rong * S, cao * S, t)
    im = im.resize((rong, cao), Image.LANCZOS)
    os.remove(t)
    if ra:
        im.save(ra, optimize=True)
    return im


# --------------------------------------------------------------------------- bản pixel art

def mat_na(svg, W, H, rong, nguong=0.5):
    """Mặt nạ 0/1 (numpy bool) của SVG trắng ở cỡ rong: vẽ lớn (>= 1200 px, Chrome headless vẽ sai khung
    nhỏ) rồi lấy trung bình từng ô S x S và lấy ngưỡng."""
    cao = round(H * rong / W)
    S = max(4, -(-1200 // rong))
    t = os.path.join(tempfile.gettempdir(), 'htbb_mn_%d.png' % os.getpid())
    im = ve_svg(svg, rong * S, cao * S, t)
    os.remove(t)
    a = np.asarray(im.getchannel('A'), dtype=np.float32) / 255
    a = a[:cao * S, :rong * S].reshape(cao, S, rong, S).mean(axis=(1, 3))
    return a >= nguong


def gian(m, r=1, cheo=True):
    """Giãn mặt nạ r px (8 hướng nếu cheo, 4 hướng nếu không)."""
    out = m.copy()
    for _ in range(r):
        n = out.copy()
        n[1:, :] |= out[:-1, :]
        n[:-1, :] |= out[1:, :]
        n[:, 1:] |= out[:, :-1]
        n[:, :-1] |= out[:, 1:]
        if cheo:
            n[1:, 1:] |= out[:-1, :-1]
            n[1:, :-1] |= out[:-1, 1:]
            n[:-1, 1:] |= out[1:, :-1]
            n[:-1, :-1] |= out[1:, 1:]
        out = n
    return out


def logo_pixel(rong, vien=1):
    """Logo pixel art bề ngang ~rong (không phụ đề): chữ tô dải vàng -> cam theo hàng, viền navy vien px,
    viền xanh 1 px, bóng đổ 1 px, Poké Ball vẽ từng pixel. Trả ảnh RGBA đã cắt sát."""
    p = bo_cuc(False)
    W, H = p['W'], p['H']
    rong = int(round(rong * W / (p['wnoi'] + 2 * (vien + 1) * W / rong)))
    # làm đậm nét ~0.5 px ở cỡ đích (giữ nét ngang mảnh của B, Ả... khi lấy ngưỡng)
    svg_c, _, _ = logo_svg(False, 'chu', 0.5 * W / rong)
    svg_b, _, _ = logo_svg(False, 'bong')
    chu = mat_na(svg_c, W, H, rong, 0.45)
    bong = mat_na(svg_b, W, H, rong, 0.5)
    k = rong / W
    h, w = chu.shape
    pad = 4
    chu = np.pad(chu, pad)
    bong = np.pad(bong, pad)
    h, w = chu.shape
    khoi = chu | bong
    v_navy = gian(khoi, vien)
    v_xanh = gian(v_navy, 1)
    v_bong = np.zeros_like(v_xanh)
    v_bong[1:, :] = v_xanh[:-1, :]
    v_bong &= ~v_xanh
    img = np.zeros((h, w, 4), np.uint8)

    def to(mask, mau):
        c = hex2rgb(mau)
        img[mask] = (c[0], c[1], c[2], 255)

    to(v_bong, BONG)
    to(v_xanh, XANH)
    # mép sáng trên viền xanh
    tren = v_xanh & ~v_navy
    sang = tren.copy()
    sang[1:, :] &= ~v_xanh[:-1, :]  # pixel xanh không có xanh phía trên
    sang[0, :] = tren[0, :]
    to(sang, XANH_SANG)
    to(v_navy, NAVY)
    # chữ: dải màu theo chiều cao chữ hoa của từng dòng
    yy = np.arange(h)[:, None].repeat(w, 1)
    tach = p['y1'] + 0.18 * p['co1']  # dưới dấu nặng của Ạ, trên dấu sắc của Ố
    for (c0, c1), (dong0, dong1) in ((p['cap1'], (0, tach)), (p['cap2'], (tach, H))):
        y0 = c0 * k + pad
        y1 = c1 * k + pad
        t = (yy - y0) / max(1, (y1 - y0))
        vung = chu & (yy >= dong0 * k + pad) & (yy < dong1 * k + pad)
        for i, (a, b) in enumerate(((-9, 0.16), (0.16, 0.44), (0.44, 0.68), (0.68, 0.86), (0.86, 9))):
            to(vung & (t >= a) & (t < b), VANG[i])
    # mép sáng trên của chữ
    dinh = chu.copy()
    dinh[1:, :] &= ~chu[:-1, :]
    to(dinh & (yy > 0), VANG[0])
    # Poké Ball
    bx, by, br = p['bong']
    bx, by, br = bx * k + pad, by * k + pad, br * k
    xx = np.arange(w)[None, :].repeat(h, 0)
    dx = xx + 0.5 - bx
    dy = yy + 0.5 - by
    r = np.sqrt(dx * dx + dy * dy)
    tren_b = bong & (dy < 0)
    to(tren_b, DO)
    to(tren_b & (dy < -br * 0.55), DO_SANG)
    to(tren_b & (dx < -br * 0.15) & (dy < -br * 0.2) & (r > br * 0.45) & (r < br * 0.75), '#ffb4a0')
    to(bong & (dy >= 0), TRANG)
    to(bong & (dy >= br * 0.55), XAM)
    to(bong & (np.abs(dy) < max(1.0, br * 0.13)), NAVY)
    to(bong & (r < br * 0.42), NAVY)
    to(bong & (r < br * 0.42 - max(1.0, br * 0.12)), TRANG)
    # cắt sát
    a = img[:, :, 3] > 0
    ys, xs = np.where(a)
    img = img[ys.min():ys.max() + 1, xs.min():xs.max() + 1]
    return Image.fromarray(img, 'RGBA')


def luu_bang_mau(img, duong, mau=32):
    """PNG bảng màu + 1 màu trong suốt (cho jar J2ME)."""
    a = np.asarray(img.getchannel('A'))
    q = img.convert('RGB').quantize(colors=mau - 1, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
    arr = np.asarray(q).copy()
    arr[a < 128] = mau - 1
    out = Image.fromarray(arr.astype(np.uint8), 'P')
    pal = q.getpalette()[:3 * (mau - 1)]
    out.putpalette(pal + [255, 0, 255] + [0] * (3 * (256 - mau)))
    out.save(duong, optimize=True, transparency=mau - 1)


# --------------------------------------------------------------------------- icon

def icon_tu_svg(svg, n):
    t = os.path.join(tempfile.gettempdir(), 'htbb_ic_%d.png' % os.getpid())
    im = ve_svg(svg, 512, 512, t)
    os.remove(t)
    return im.resize((n, n), Image.LANCZOS)


def icon_nho(n):
    """Icon 16/24/32/48: Poké Ball pixel tự vẽ (không co ảnh lớn -> không nhoè)."""
    s = 8
    N = n * s
    im = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    m = N // 16
    d.ellipse([m, m, N - m - 1, N - m - 1], fill=NAVY)
    v = max(s, N // 12)
    d.ellipse([m + v, m + v, N - m - v - 1, N - m - v - 1], fill=TRANG)
    d.pieslice([m + v, m + v, N - m - v - 1, N - m - v - 1], 180, 360, fill=DO)
    d.rectangle([m, N // 2 - v // 2 - s // 2, N - m, N // 2 + v // 2], fill=NAVY)
    r = N // 6
    d.ellipse([N // 2 - r, N // 2 - r, N // 2 + r, N // 2 + r], fill=NAVY)
    r2 = r - v
    d.ellipse([N // 2 - r2, N // 2 - r2, N // 2 + r2, N // 2 + r2], fill=TRANG)
    return im.resize((n, n), Image.BOX)


def adaptive_android(res_dir, svg_nen, svg_truoc):
    """Adaptive icon: nền (trời hoàng hôn + núi) + lớp trước (Poké Ball) - 108dp, vùng an toàn 66dp."""
    mat_do = {'mdpi': 1, 'hdpi': 1.5, 'xhdpi': 2, 'xxhdpi': 3, 'xxxhdpi': 4}
    nen = icon_tu_svg(svg_nen, 432)
    truoc_goc = icon_tu_svg(svg_truoc, 432)
    # lớp trước co vào vùng an toàn (66/108)
    k = 0.80
    tr = Image.new('RGBA', (432, 432), (0, 0, 0, 0))
    t2 = truoc_goc.resize((int(432 * k), int(432 * k)), Image.LANCZOS)
    tr.alpha_composite(t2, ((432 - t2.width) // 2, (432 - t2.height) // 2))
    ghep = Image.alpha_composite(nen, tr)
    for ten, he in mat_do.items():
        s = int(108 * he)
        d = os.path.join(res_dir, 'drawable-' + ten)
        os.makedirs(d, exist_ok=True)
        nen.resize((s, s), Image.LANCZOS).save(os.path.join(d, 'ic_launcher_nen.png'), optimize=True)
        tr.resize((s, s), Image.LANCZOS).save(os.path.join(d, 'ic_launcher_truoc.png'), optimize=True)
        l = int(48 * he)
        dm = os.path.join(res_dir, 'mipmap-' + ten)
        os.makedirs(dm, exist_ok=True)
        # icon cũ: vuông bo góc + tròn (cắt 18/108 mỗi cạnh như launcher)
        cat = ghep.crop((72, 72, 360, 360)).resize((l, l), Image.LANCZOS)
        bo = Image.new('L', (l * 4, l * 4), 0)
        ImageDraw.Draw(bo).rounded_rectangle([0, 0, l * 4 - 1, l * 4 - 1], radius=l * 4 // 5, fill=255)
        vuong = Image.new('RGBA', (l, l), (0, 0, 0, 0))
        vuong.paste(cat, (0, 0), bo.resize((l, l), Image.LANCZOS))
        vuong.save(os.path.join(dm, 'ic_launcher.png'), optimize=True)
        tron = Image.new('L', (l * 4, l * 4), 0)
        ImageDraw.Draw(tron).ellipse([0, 0, l * 4 - 1, l * 4 - 1], fill=255)
        t = Image.new('RGBA', (l, l), (0, 0, 0, 0))
        t.paste(cat, (0, 0), tron.resize((l, l), Image.LANCZOS))
        t.save(os.path.join(dm, 'ic_launcher_round.png'), optimize=True)
    return ghep


def cat_sat(svg, W, H, le=6):
    """Thu viewBox sát nét vẽ (chừa lề le đơn vị) -> logo đặt trong trang không có khoảng trống thừa."""
    t = os.path.join(tempfile.gettempdir(), 'htbb_bb_%d.png' % os.getpid())
    S = 2
    im = ve_svg(svg, W * S, H * S, t)
    os.remove(t)
    x0, y0, x1, y1 = im.getbbox()
    x0, y0 = max(0, x0 / S - le), max(0, y0 / S - le)
    x1, y1 = min(W, x1 / S + le), min(H, y1 / S + le)
    w, h = round(x1 - x0), round(y1 - y0)
    svg = svg.replace(f'viewBox="0 0 {W} {H}" width="{W}" height="{H}"', f'viewBox="{x0:.0f} {y0:.0f} {w} {h}" width="{w}" height="{h}"', 1)
    return svg, w, h


def main():
    web = None
    if '--web' in sys.argv:
        web = sys.argv[sys.argv.index('--web') + 1]
    os.makedirs(RA, exist_ok=True)
    svg, W, H = cat_sat(*logo_svg(True))
    with open(os.path.join(RA, 'logo.svg'), 'w', encoding='utf-8') as f:
        f.write(svg)
    svg_k, Wk, Hk = cat_sat(*logo_svg(False))
    with open(os.path.join(RA, 'logo_gon.svg'), 'w', encoding='utf-8') as f:
        f.write(svg_k)
    for n in (1024, 512, 256, 128):
        png_tu_svg(svg, W, H, n, os.path.join(RA, 'logo_%d.png' % n))
    # bản pixel
    px = {}
    for n, vien in ((240, 2), (160, 1), (120, 1)):
        im = logo_pixel(n, vien)
        im.save(os.path.join(RA, 'logo_px_%d.png' % n), optimize=True)
        px[n] = im
    bn = os.path.join(REPO, 'j2me', 'res', 'bn')
    os.makedirs(bn, exist_ok=True)
    luu_bang_mau(px[160], os.path.join(bn, 'logo.png'), 24)
    luu_bang_mau(px[120], os.path.join(bn, 'logo_nho.png'), 24)
    hd = os.path.join(REPO, 'shared', 'banner', 'res', 'bn', 'hd')
    os.makedirs(hd, exist_ok=True)
    px[240].save(os.path.join(hd, 'logo.png'), optimize=True)
    # biểu tượng
    bt = bieu_tuong_svg(True)
    with open(os.path.join(RA, 'bieu_tuong.svg'), 'w', encoding='utf-8') as f:
        f.write(bt)
    bt_trong = bieu_tuong_svg(False)
    with open(os.path.join(RA, 'bieu_tuong_trong.svg'), 'w', encoding='utf-8') as f:
        f.write(bt_trong)
    lon = icon_tu_svg(bt, 1024)
    cac = {}
    for n in (1024, 512, 256, 192, 180, 128, 64):
        cac[n] = lon.resize((n, n), Image.LANCZOS)
        cac[n].save(os.path.join(RA, 'icon_%d.png' % n), optimize=True)
    for n in (48, 32, 24, 16):
        cac[n] = icon_nho(n)
        cac[n].save(os.path.join(RA, 'icon_%d.png' % n), optimize=True)
    cac[256].save(os.path.join(RA, 'htbb.ico'), sizes=[(256, 256), (128, 128), (64, 64), (48, 48), (32, 32), (16, 16)],
                  append_images=[cac[128], cac[64], cac[48], cac[32], cac[16]])
    cac[48].save(os.path.join(RA, 'favicon.ico'), sizes=[(48, 48), (32, 32), (16, 16)], append_images=[cac[32], cac[16]])
    # Android
    res = os.path.join(REPO, 'android', 'pokemon', 'src', 'main', 'res')
    if os.path.isdir(res):
        adaptive_android(res, bieu_tuong_svg(True, chi_nen=True), bt_trong)
        # màn chờ (res/drawable/man_cho.xml): logo trên nền trời
        os.makedirs(os.path.join(res, 'drawable-nodpi'), exist_ok=True)
        shutil.copy(os.path.join(RA, 'logo_512.png'), os.path.join(res, 'drawable-nodpi', 'man_cho_logo.png'))
    # PC (Unity PlayerSettings.SetIcons đọc Assets/Icon/app_icon.png - BuildCli)
    pc_icon = os.path.join(REPO, 'pc', 'Assets', 'Icon', 'app_icon.png')
    if os.path.isdir(os.path.dirname(pc_icon)):
        cac[256].save(pc_icon, optimize=True)
    if web:
        pub = os.path.join(web, 'public')
        shutil.copy(os.path.join(RA, 'logo.svg'), os.path.join(pub, 'logo.svg'))
        shutil.copy(os.path.join(RA, 'bieu_tuong_trong.svg'), os.path.join(pub, 'logo-mark.svg'))
        shutil.copy(os.path.join(RA, 'logo_gon.svg'), os.path.join(pub, 'logo-gon.svg'))
        shutil.copy(os.path.join(RA, 'favicon.ico'), os.path.join(pub, 'favicon.ico'))
        cac[32].save(os.path.join(pub, 'favicon-32.png'), optimize=True)
        cac[180].save(os.path.join(pub, 'apple-touch-icon.png'), optimize=True)
        cac[192].save(os.path.join(pub, 'icon-192.png'), optimize=True)
        cac[512].save(os.path.join(pub, 'icon-512.png'), optimize=True)
        shutil.copy(os.path.join(RA, 'logo_512.png'), os.path.join(pub, 'logo-512.png'))
    print('OK', RA, 'logo', W, 'x', H, '- gon', Wk, 'x', Hk)


if __name__ == '__main__':
    main()
