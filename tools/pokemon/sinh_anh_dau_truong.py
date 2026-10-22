"""Sinh ẢNH ĐẤU TRƯỜNG BẢO BỐI (0.9.8, D12) - chỉ PC / APK (res/ui không vào jar J2ME: J2ME vẽ nền khối phẳng).

    python3 tools/pokemon/sinh_anh_dau_truong.py          # ghi client_jar/res/ui/dau_truong_san_<n>.png + chieu_loai_{32,64}.png
    python3 tools/pokemon/sinh_anh_dau_truong.py --kiem   # chỉ kiểm tệp trong repo khớp bộ sinh

1. dau_truong_san_<n>.png (600 x 240, rộng cho màn 20:9 / 21:9, pixel-art, phóng nguyên lúc chạy): 8 sân kiểu Pokémon theo thứ tự
   DauTruongLogic.TEN_SAN (cỏ, đá, hồ nước bục nổi, gym điện, băng, núi lửa, đỉnh rồng, đêm): nền sau theo chủ đề, viền
   sân, mặt sân, biểu tượng POKÉ BALL phối cảnh (nửa trên phớt đỏ, vạch ngang + nút giữa), hai bục nổi có bóng (đối thủ
   phải trên, mình trái dưới - toạ độ ảnh BUC_THU / BUC_CONG, ManTranDauTruong dùng cùng số). Tự vẽ (CC0).
2. chieu_loai_{32,64}.png: icon CHIÊU LOÀI cho bảng chiêu chế độ tự chọn (data_chieu): tấm 10 cột (vai trò, thứ tự
   DauTruongDuLieu.VAI) x 18 hàng (hệ), cùng kiểu ô vuông NRO của sinh_icon_chieu.py (hàm huy_hieu, hình game-icons.net
   CC BY 3.0 trong tools/pokemon/icon_chieu_svg). Ô = hệ x 10 + vai trò.
"""
import importlib.util
import os
import random
import sys

from PIL import Image, ImageDraw

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
RA = os.path.join(GOC, "client_jar", "res", "ui")
W, H = 600, 240
BUC_THU = (390, 112)
BUC_CONG = (210, 196)
VAI = ["thuong", "tan_cong", "nhieu_don", "xa", "vung", "toi_thuong", "dac_trung", "khong_che", "buff", "khien"]
HINH_VAI = {
    "thuong": ("lorc_impact-point", "V"), "tan_cong": ("lorc_bright-explosion", "V"), "nhieu_don": ("lorc_triple-claws", "V"),
    "xa": ("lorc_laser-blast", "D"), "vung": ("lorc_static-waves", "D"), "toi_thuong": ("lorc_cannon-shot", "D"),
    "dac_trung": ("delapouite_falling-star", "D"), "khong_che": ("lorc_spider-web", "T"),
    "buff": ("lorc_crossed-swords+mui_len", "T"), "khien": ("lorc_checked-shield", "T"),
}


def nap_icon():
    spec = importlib.util.spec_from_file_location("sinh_icon_chieu", os.path.join(GOC, "tools", "pokemon", "sinh_icon_chieu.py"))
    m = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(m)
    return m


def icon_loai(m, s):
    tam = Image.new("RGBA", (len(VAI) * s, 18 * s), (0, 0, 0, 0))
    for he in range(18):
        for i, vai in enumerate(VAI):
            hinh, loai = HINH_VAI[vai]
            tam.paste(m.huy_hieu(he, hinh, loai, False, s), (i * s, he * s))
    return tam


def pha(a, b, t):
    return tuple(int(a[k] + (b[k] - a[k]) * t) for k in range(3))


# chủ đề: (tên, nền sân, sân sọc, vạch, nền sau trên, nền sau dưới)
SAN = [
    ("co", (98, 170, 74), (90, 158, 66), (236, 244, 220), (128, 190, 238), (160, 210, 244)),
    ("da", (198, 162, 108), (186, 150, 98), (250, 236, 206), (178, 196, 222), (206, 214, 228)),
    ("nuoc", (70, 134, 206), (62, 124, 196), (226, 240, 252), (136, 196, 240), (176, 218, 246)),
    ("dien", (118, 126, 142), (108, 116, 132), (250, 216, 72), (36, 40, 54), (52, 58, 76)),
    ("bang", (184, 220, 250), (170, 208, 244), (96, 150, 208), (150, 196, 240), (196, 226, 252)),
    ("lua", (92, 54, 42), (82, 46, 36), (250, 170, 70), (120, 40, 30), (168, 70, 40)),
    ("rong", (140, 124, 142), (128, 112, 132), (236, 220, 250), (98, 84, 150), (156, 120, 170)),
    ("dem", (58, 46, 86), (50, 40, 76), (190, 170, 240), (14, 12, 34), (34, 26, 60)),
]
EM = (300, 152, 120, 40)   # tâm + bán trục biểu tượng Poké Ball (phối cảnh)
Y_SAN = 56


def trong_elip(x, y, cx, cy, rx, ry):
    return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0


def nen_sau(d, a, ten, c1, c2, r):
    for y in range(0, Y_SAN):
        d.line([(0, y), (W, y)], fill=pha(c1, c2, y / Y_SAN))
    if ten in ("co", "nuoc", "da", "bang"):
        for cx, cy in ((44, 10), (160, 6), (270, 13), (400, 9), (530, 12)):
            for dx, rr in ((0, 8), (9, 6), (-9, 6)):
                d.ellipse([cx + dx - rr, cy - rr // 2, cx + dx + rr, cy + rr // 2], fill=(255, 250, 236))
    if ten in ("co", "dien"):
        khan_dai(d, 10 if ten != "dien" else 14, Y_SAN - 12, r, {"co": (120, 64, 36), "dien": (48, 52, 70)}[ten])
    if ten == "dien":
        for x in range(30, W, 90):
            d.ellipse([x - 5, 1, x + 5, 7], fill=(255, 248, 200))
    if ten == "co":
        for x in range(-10, W + 10, 14):
            h = 12 + (x * 7) % 9
            d.ellipse([x, 40 - h // 3, x + 22, Y_SAN - 4], fill=(52, 112, 52))
            d.ellipse([x + 4, 42 - h // 3, x + 16, Y_SAN - 8], fill=(70, 136, 62))
    elif ten in ("da", "bang", "rong"):
        mau = {"da": ((150, 118, 86), (176, 142, 104)), "bang": ((132, 176, 220), (230, 244, 255)), "rong": ((76, 62, 104), (112, 92, 140))}[ten]
        for cx, h in ((30, 30), (96, 22), (150, 34), (214, 26), (288, 32), (350, 24), (420, 30), (476, 22), (540, 30), (596, 24)):
            d.polygon([(cx - 46, Y_SAN), (cx, Y_SAN - h - 8), (cx + 46, Y_SAN)], fill=mau[0])
            d.polygon([(cx - 10, Y_SAN - h + 2), (cx, Y_SAN - h - 8), (cx + 10, Y_SAN - h + 2)], fill=mau[1])
    elif ten == "nuoc":
        d.rectangle([0, 34, W, Y_SAN], fill=(92, 156, 216))
        d.rectangle([0, 30, W, 34], fill=(214, 196, 140))
    elif ten == "dien":
        for x in range(8, W, 40):
            d.rectangle([x, 8, x + 24, 40], fill=(60, 66, 86))
            d.polygon([(x + 14, 12), (x + 7, 25), (x + 12, 25), (x + 9, 36), (x + 18, 21), (x + 13, 21), (x + 17, 12)], fill=(250, 214, 64))
        for x in range(0, W, 20):
            d.rectangle([x + 8, 2, x + 11, 4], fill=(255, 246, 200))
    elif ten == "lua":
        d.polygon([(250, Y_SAN), (290, 14), (312, 14), (354, Y_SAN)], fill=(70, 40, 34))
        d.polygon([(290, 14), (301, 4), (312, 14)], fill=(250, 140, 50))
        for k in range(30):
            x, y = r.randrange(0, W), r.randrange(0, 40)
            d.point((x, y), fill=(250, 180, 90))
    elif ten == "dem":
        for k in range(60):
            d.point((r.randrange(0, W), r.randrange(0, 40)), fill=(230, 230, 255))
        d.ellipse([396, 6, 416, 26], fill=(246, 238, 206))
        d.ellipse([402, 4, 422, 24], fill=c1)
        for x in range(6, W, 34):
            d.rounded_rectangle([x, 36, x + 12, 52], radius=5, fill=(40, 34, 60))
            d.rectangle([x + 5, 40, x + 7, 47], fill=(70, 62, 96))
            d.rectangle([x + 3, 42, x + 9, 44], fill=(70, 62, 96))


def vien_san(d, ten, r):
    y0 = Y_SAN - 6
    if ten == "co":
        d.rectangle([0, y0, W, Y_SAN], fill=(44, 98, 46))
        for x in range(0, W, 6):
            d.rectangle([x, y0 - 2, x + 3, y0], fill=(60, 120, 56))
    elif ten == "da":
        for x in range(-6, W, 18):
            d.ellipse([x, y0 - 4, x + 20, Y_SAN + 2], fill=(132, 106, 82))
            d.ellipse([x + 4, y0 - 3, x + 14, y0 + 3], fill=(166, 138, 108))
    elif ten == "nuoc":
        d.rectangle([0, y0, W, Y_SAN], fill=(140, 96, 56))
        for x in range(0, W, 12):
            d.rectangle([x, y0 - 6, x + 3, Y_SAN], fill=(110, 72, 40))
    elif ten == "dien":
        for x in range(0, W, 12):
            d.polygon([(x, Y_SAN), (x + 6, y0), (x + 12, y0), (x + 6, Y_SAN)], fill=(250, 210, 60))
            d.polygon([(x + 6, Y_SAN), (x + 12, y0), (x + 18, y0), (x + 12, Y_SAN)], fill=(30, 30, 36))
    elif ten == "bang":
        for x in range(0, W, 16):
            d.rectangle([x, y0 - 4, x + 14, Y_SAN], fill=(176, 214, 240))
            d.line([(x, y0 - 4), (x + 14, y0 - 4)], fill=(228, 246, 255))
    elif ten == "lua":
        for x in range(-4, W, 16):
            d.ellipse([x, y0 - 4, x + 18, Y_SAN + 2], fill=(54, 30, 26))
            d.line([(x + 4, y0 + 2), (x + 12, y0)], fill=(250, 130, 40))
    elif ten == "rong":
        for x in range(4, W, 40):
            d.rectangle([x, y0 - 16, x + 10, Y_SAN], fill=(104, 90, 110))
            d.rectangle([x - 2, y0 - 18, x + 12, y0 - 15], fill=(128, 112, 134))
        d.rectangle([0, y0, W, Y_SAN], fill=(90, 78, 98))
    elif ten == "dem":
        d.rectangle([0, Y_SAN - 2, W, Y_SAN], fill=(28, 24, 40))
        for x in range(0, W, 7):
            d.rectangle([x, y0 - 8, x + 1, Y_SAN], fill=(28, 24, 40))
            d.polygon([(x - 1, y0 - 8), (x + 0.5, y0 - 11), (x + 2, y0 - 8)], fill=(28, 24, 40))


def mat_san(a, ten, c, c2, r):
    """Mặt sân theo chủ đề (không ô cờ): cỏ sọc chéo + hoa, đất sỏi, nước gợn, sàn kim loại gym có tia sét, băng nứt,
    đá núi lửa nứt dung nham, lát đá đỉnh rồng, sân đêm sương."""
    d = ImageDraw.Draw(a)
    for y in range(Y_SAN, H):
        for x in range(W):
            m = c
            if ten == "co":
                m = c if ((x + y) // 18) % 2 else c2
                if (x * 7 + y * 13) % 41 == 0:
                    m = pha(m, (40, 90, 30), 0.35)
            elif ten == "da":
                m = c if ((x * 3 + y * 5) // 23) % 3 else c2
                if (x * 13 + y * 7) % 31 == 0:
                    m = pha(m, (0, 0, 0), 0.22)
            elif ten == "nuoc":
                m = c if ((x + (y * 3)) // 9) % 7 else pha(c, (230, 244, 255), 0.30)
            elif ten == "dien":
                m = c if ((x // 48) + (y - Y_SAN) // 30) % 2 else c2
                if x % 48 == 0 or (y - Y_SAN) % 30 == 0:
                    m = pha(c, (0, 0, 0), 0.30)
                elif x % 48 == 1 or (y - Y_SAN) % 30 == 1:
                    m = pha(c, (255, 255, 255), 0.18)
            elif ten == "bang":
                m = c if ((x + y // 2) // 26) % 2 else c2
                if (x * 5 + y * 11) % 53 == 0:
                    m = (236, 248, 255)
            elif ten == "lua":
                m = c if ((x * 2 + y) // 20) % 3 else c2
                if (x * 13 + y * 7) % 37 == 0:
                    m = pha(m, (0, 0, 0), 0.25)
            elif ten == "rong":
                m = c if ((x // 30) + ((y - Y_SAN) // 15)) % 2 else c2
                if x % 30 == 0 or (y - Y_SAN) % 15 == 0:
                    m = pha(c, (0, 0, 0), 0.22)
            elif ten == "dem":
                m = c if ((x + y) // 24) % 2 else c2
            a.putpixel((x, y), m)
    if ten == "co":
        for k in range(70):
            x, y = r.randrange(0, W), r.randrange(Y_SAN + 3, H)
            d.point((x, y), fill=r.choice([(250, 240, 120), (250, 250, 250), (240, 140, 160)]))
    if ten == "dien":
        for cx, cy, sc in ((178, 170, 1.0), (422, 130, -1.0)):
            pts = [(0, -26), (10, -26), (2, -4), (14, -4), (-8, 26), (-2, 4), (-14, 4)]
            d.polygon([(cx + px * sc * 1.6, cy + py * 0.9) for px, py in pts], fill=(214, 180, 52))
        for x in range(0, W, 24):
            d.rectangle([x, H - 5, x + 11, H - 1], fill=(250, 210, 60))
            d.rectangle([x + 12, H - 5, x + 23, H - 1], fill=(30, 30, 36))
    if ten == "lua":
        for k in range(16):
            x, y = r.randrange(0, W), r.randrange(Y_SAN + 4, H)
            pts = [(x, y)]
            for _ in range(4):
                x += r.randrange(-8, 9)
                y += r.randrange(-3, 4)
                pts.append((x, y))
            d.line(pts, fill=(250, 120, 40), width=1)
    if ten == "bang":
        for k in range(12):
            x, y = r.randrange(0, W), r.randrange(Y_SAN + 4, H)
            d.line([(x, y), (x + r.randrange(-12, 13), y + r.randrange(2, 8))], fill=(150, 196, 230))
    if ten == "dem":
        for k in range(22):
            x, y = r.randrange(0, W), r.randrange(Y_SAN, H)
            d.ellipse([x - 16, y - 3, x + 16, y + 3], fill=pha(c, (120, 110, 160), 0.22))
    if ten == "nuoc":
        for k in range(24):
            x, y = r.randrange(0, W), r.randrange(Y_SAN + 4, H)
            d.arc([x - 10, y - 3, x + 10, y + 3], 200, 340, fill=(214, 236, 252))


def anh_sang(a):
    """Ánh sáng phối cảnh: sân xa tối hơn, gần sáng hơn, hai mép trái / phải tối dần."""
    for y in range(Y_SAN, H):
        t = (y - Y_SAN) / (H - Y_SAN)
        for x in range(W):
            p = a.getpixel((x, y))
            k = 0.80 + 0.26 * t
            ex = min(x, W - 1 - x) / 70.0
            if ex < 1:
                k *= 0.78 + 0.22 * ex
            a.putpixel((x, y), tuple(max(0, min(255, int(v * k))) for v in p))


def khan_dai(d, y0, y1, r, nen):
    """Khán đài nhiều bậc có khán giả (sân vận động / gym)."""
    for i, yy in enumerate(range(y0, y1, 7)):
        d.rectangle([0, yy, W, yy + 6], fill=pha(nen, (0, 0, 0), 0.12 * (i % 2)))
        d.line([(0, yy), (W, yy)], fill=pha(nen, (0, 0, 0), 0.45))
        for x in range(2 + (i % 2) * 2, W, 5):
            if r.random() < 0.75:
                ao = r.choice([(232, 64, 48), (56, 112, 216), (248, 200, 64), (80, 176, 72), (240, 240, 232), (168, 96, 200)])
                d.rectangle([x, yy + 3, x + 2, yy + 5], fill=ao)
                d.rectangle([x, yy + 1, x + 2, yy + 2], fill=(232, 184, 140))


def bieu_tuong(a, ten, vach):
    """Biểu tượng Poké Ball trên sân (phối cảnh): nửa trên phớt đỏ, nửa dưới phớt trắng, viền + vạch ngang + nút giữa."""
    cx, cy, rx, ry = EM
    for y in range(cy - ry, cy + ry + 1):
        for x in range(cx - rx, cx + rx + 1):
            if trong_elip(x, y, cx, cy, rx, ry):
                p = a.getpixel((x, y))
                a.putpixel((x, y), pha(p, (220, 48, 40), 0.12) if y < cy else pha(p, (255, 255, 255), 0.08))
    # vạch sơn mờ hoà vào mặt sân, nút giữa nhỏ
    lop = Image.new("RGBA", a.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lop)
    v = vach + (150,)
    d.ellipse([cx - rx, cy - ry, cx + rx, cy + ry], outline=v, width=2)
    d.rectangle([cx - rx, cy - 1, cx + rx, cy], fill=v)
    d.ellipse([cx - 15, cy - 6, cx + 15, cy + 6], fill=v)
    d.ellipse([cx - 10, cy - 4, cx + 10, cy + 4], fill=(250, 250, 246, 200))
    a.paste(Image.alpha_composite(a.convert("RGBA"), lop).convert("RGB"))


def buc(a, cx, cy, rx, ry, ten, vien):
    d = ImageDraw.Draw(a)
    nuoc = ten == "nuoc"
    # bóng mềm
    d.ellipse([cx - rx + 4, cy - ry + 6, cx + rx + 6, cy + ry + 6], fill=pha(a.getpixel((cx, min(H - 1, cy + ry + 3))), (0, 0, 0), 0.30))
    if nuoc:
        for k in (10, 18):
            d.ellipse([cx - rx - k, cy - ry - k // 3, cx + rx + k, cy + ry + k // 3], outline=(214, 236, 252))
    mat = {"co": (214, 196, 150), "da": (170, 150, 124), "nuoc": (170, 160, 150), "dien": (150, 158, 174), "bang": (214, 236, 255),
           "lua": (110, 80, 70), "rong": (172, 160, 176), "dem": (98, 88, 120)}[ten]
    than = pha(mat, (0, 0, 0), 0.32)
    d.ellipse([cx - rx, cy - ry + 4, cx + rx, cy + ry + 4], fill=than)
    d.rectangle([cx - rx, cy, cx + rx, cy + 4], fill=than)
    d.ellipse([cx - rx, cy - ry, cx + rx, cy + ry], fill=mat)
    d.ellipse([cx - rx + 4, cy - ry + 2, cx + rx - 4, cy + ry - 2], fill=pha(mat, (255, 255, 255), 0.18))
    d.arc([cx - rx + 2, cy - ry + 1, cx + rx - 2, cy + ry - 1], 190, 350, fill=vien, width=2)
    d.arc([cx - rx + 2, cy - ry + 1, cx + rx - 2, cy + ry - 1], 10, 170, fill=pha(vien, (0, 0, 0), 0.3), width=1)


def nen(i):
    ten, c, c2, vach, n1, n2 = SAN[i]
    r = random.Random(2026 + i)
    a = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(a)
    nen_sau(d, a, ten, n1, n2, r)
    mat_san(a, ten, c, c2, r)
    vien_san(ImageDraw.Draw(a), ten, r)
    bieu_tuong(a, ten, vach)
    anh_sang(a)
    buc(a, BUC_THU[0], BUC_THU[1], 30, 8, ten, (214, 72, 56))
    buc(a, BUC_CONG[0], BUC_CONG[1], 36, 10, ten, (64, 120, 214))
    return a


def main():
    ra = {"dau_truong_san_%d.png" % i: nen(i) for i in range(len(SAN))}
    m = nap_icon()
    for s in (32, 64):
        ra["chieu_loai_%d.png" % s] = icon_loai(m, s)
    kiem = "--kiem" in sys.argv
    loi = 0
    for ten, anh in ra.items():
        duong = os.path.join(RA, ten)
        if kiem:
            if not os.path.isfile(duong):
                print("LOI thieu", ten)
                loi += 1
                continue
            import io
            buf = io.BytesIO()
            anh.quantize(256, method=Image.FASTOCTREE, dither=Image.NONE).save(buf, format="PNG", optimize=True)
            moi = Image.open(io.BytesIO(buf.getvalue())).convert("RGBA").tobytes()
            cu = Image.open(duong).convert("RGBA")
            if cu.size != anh.size or cu.tobytes() != moi:
                print("LOI khac bo sinh", ten)
                loi += 1
            continue
        anh.quantize(256, method=Image.FASTOCTREE, dither=Image.NONE).save(duong, optimize=True)
        print("ghi", os.path.relpath(duong, GOC), anh.size, os.path.getsize(duong), "byte")
    if kiem:
        print("kiem anh dau truong:", "DAT" if loi == 0 else "HONG %d" % loi)
        sys.exit(1 if loi else 0)


if __name__ == "__main__":
    main()
