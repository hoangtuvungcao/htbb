"""Vẽ icon launcher của APK Huyền Thoại Bảo Bối (adaptive icon Android 8+).

    python3 tao_icon.py <client_pc_ios/Assets/Resources/poke/0025_cd.bytes> <client_apk/pokemon/src/main/res>

Nền: Poké Ball vẽ bằng code (đỏ / trắng / đai đen). Lớp trước: chân dung Pikachu 40x40 của PMDCollab
(SpriteCollab, CC BY-NC 4.0 - ghi công trong assets GHI_CONG.txt) phóng nguyên lần láng giềng gần nhất,
đặt trong "nút" giữa quả bóng. Cần Pillow.
"""
import os
import sys
from PIL import Image, ImageDraw

chan_dung, res = sys.argv[1], sys.argv[2]
N = 432  # 108dp @ xxxhdpi


def nen(n):
    im = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, n, n // 2], fill=(226, 41, 41, 255))
    d.rectangle([0, n // 2, n, n], fill=(246, 246, 246, 255))
    # bóng đổ nhẹ nửa dưới, vệt sáng nửa trên (khối vuông kiểu pixel)
    d.rectangle([0, n * 47 // 100, n, n // 2], fill=(180, 24, 24, 255))
    d.rectangle([0, n * 86 // 100, n, n], fill=(214, 214, 222, 255))
    d.rectangle([n * 22 // 100, n * 20 // 100, n * 30 // 100, n * 26 // 100], fill=(255, 140, 140, 255))
    d.rectangle([n * 30 // 100, n * 16 // 100, n * 40 // 100, n * 20 // 100], fill=(255, 140, 140, 255))
    d.rectangle([0, n * 46 // 100, n, n * 54 // 100], fill=(28, 28, 36, 255))
    return im


def truoc(n):
    im = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    c = n // 2
    r_ngoai = n * 30 // 100     # vòng đen
    r_trang = n * 26 // 100     # vòng trắng
    r_trong = n * 22 // 100     # ô chân dung
    d.ellipse([c - r_ngoai, c - r_ngoai, c + r_ngoai, c + r_ngoai], fill=(28, 28, 36, 255))
    d.ellipse([c - r_trang, c - r_trang, c + r_trang, c + r_trang], fill=(250, 250, 250, 255))
    pk = Image.open(chan_dung).convert("RGBA")
    k = (2 * r_trong) // pk.width
    pk = pk.resize((pk.width * k, pk.height * k), Image.NEAREST)
    mat_na = Image.new("L", pk.size, 0)
    ImageDraw.Draw(mat_na).ellipse([0, 0, pk.width - 1, pk.height - 1], fill=255)
    im.paste(pk, (c - pk.width // 2, c - pk.height // 2), mat_na)
    return im


MAT_DO = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
for ten, he in MAT_DO.items():
    s = int(108 * he)
    d = os.path.join(res, "drawable-" + ten)
    os.makedirs(d, exist_ok=True)
    nen(N).resize((s, s), Image.NEAREST).save(os.path.join(d, "ic_launcher_nen.png"), optimize=True)
    truoc(N).resize((s, s), Image.NEAREST).save(os.path.join(d, "ic_launcher_truoc.png"), optimize=True)
    # icon cũ (máy không dùng adaptive): bóng tròn 48dp ghép sẵn
    l = int(48 * he)
    ghep = Image.alpha_composite(nen(N), truoc(N))
    tron = Image.new("L", (N, N), 0)
    ImageDraw.Draw(tron).ellipse([N * 9 // 100, N * 9 // 100, N * 91 // 100, N * 91 // 100], fill=255)
    ra = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    ra.paste(ghep, (0, 0), tron)
    ra = ra.crop((N * 9 // 100, N * 9 // 100, N * 91 // 100, N * 91 // 100)).resize((l, l), Image.LANCZOS)
    dm = os.path.join(res, "mipmap-" + ten)
    os.makedirs(dm, exist_ok=True)
    ra.save(os.path.join(dm, "ic_launcher.png"), optimize=True)
    ra.save(os.path.join(dm, "ic_launcher_round.png"), optimize=True)

d = os.path.join(res, "mipmap-anydpi-v26")
os.makedirs(d, exist_ok=True)
for t in ("ic_launcher", "ic_launcher_round"):
    open(os.path.join(d, t + ".xml"), "w").write(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@drawable/ic_launcher_nen"/>\n'
        '    <foreground android:drawable="@drawable/ic_launcher_truoc"/>\n'
        '</adaptive-icon>\n')
print("icon -> " + res)
