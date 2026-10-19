"""Sinh BỘ ICON CHIÊU PNG (0.9.6) - CHỈ dùng cho GIAO DIỆN (nút chiêu HUD, đòn chính, tab Kỹ năng / Ô nhanh, chọn
chiêu nhanh, gợi ý, bảng Tự động, cẩm nang web). Hiệu ứng chiêu trong trận (map / imgEffs, res/pk/hu.bin) KHÔNG đổi.

    python3 tools/pokemon/sinh_icon_chieu.py           # sinh ảnh (cần ImageMagick `convert` để vẽ SVG)
    python3 tools/pokemon/sinh_icon_chieu.py --kiem    # chỉ kiểm: mọi chiêu có hình riêng + hệ, tên khớp client / server

0.9.6: HÌNH = bóng (silhouette) game-icons.net (CC BY 3.0, tác giả theo từng tệp - tools/pokemon/icon_chieu_svg/,
nguon.txt ghi URL ghim commit). MỖI CHIÊU MỘT HÌNH RIÊNG (CHIEU bên dưới); hai chiêu chung hình chỉ khi có lý do ghi trong
CHUNG_HINH (vd "Húc" là cùng một chiêu ở ba dòng) - --kiem báo lỗi nếu trùng mà không có lý do.

Kiểu PIXEL-ART cho hợp sprite của game: huy hiệu = đĩa màu HỆ (Thường = kem, hình tối) viền tối 1-2 px, đổ bóng 2 tông
(nửa dưới phải tối hơn), hình trắng viền tối; vẽ ở độ phân giải cao rồi thu BOX + cắt ngưỡng ở đúng cỡ 32 / 64 / 96 -> mép
sắc như vẽ tay, không mờ vector, không ánh bóng kính. Góc phải trên: chấm LOẠI (vật lý = chớp nổ cam, đặc biệt = vòng xanh
dương, trạng thái = tròn xám) như game chính.

Ra (client_jar/res/ui/, PC + APK; J2ME giữ hình 12x12 cũ của KieuO vì giới hạn dung lượng):
  chieu_{32,64,96}.png        tấm ghép 8 cột, ô i = chiêu thứ i (PokeChieu: dòng 0 trước, rồi dòng 1...), bo góc
  chieu_tron_{32,64,96}.png   như trên, huy hiệu TRÒN (nút chiêu HUD), hình hơi cao để chừa chỗ nhãn tên
  web/public/chieu/<dong>-<so>.png   từng icon 64 px cho trang cẩm nang Chiêu thức
Giấy phép: hình game-icons.net CC BY 3.0 (docs/giay-phep.md, GHI_CONG.txt); phần huy hiệu tự vẽ CC0.
"""
import csv
import math
import os
import re
import subprocess
import sys
import tempfile

from PIL import Image, ImageChops, ImageDraw, ImageFilter

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
RA = os.path.join(GOC, "client_jar", "res", "ui")
RA_WEB = os.path.join(GOC, "web", "public", "chieu")
SVG = os.path.join(GOC, "tools", "pokemon", "icon_chieu_svg")
CSV = os.path.join(GOC, "du_lieu", "noi_dung_chieu.csv")
COT = 8
CO = (32, 64, 96)

TEN_HE = ["Thường", "Lửa", "Nước", "Điện", "Cỏ", "Băng", "Giác đấu", "Độc", "Đất",
          "Bay", "Siêu linh", "Bọ", "Đá", "Ma", "Rồng", "Bóng tối", "Thép", "Tiên"]
MAU_HE = [0xA8A878, 0xF08030, 0x6890F0, 0xF8D030, 0x78C850, 0x98D8D8, 0xC03028, 0xA040A0, 0xE0C068,
          0xA890F0, 0xF85888, 0xA8B820, 0xB8A038, 0x705898, 0x7038F8, 0x705848, 0xB8B8D0, 0xEE99AC]

# ---------------------------------------------------------------- chiêu -> hình (tệp SVG game-icons), loại (V vật lý / D đặc biệt / T trạng thái)
# phụ: "mui_len" = thêm mũi tên lên nhỏ (chiêu tăng chỉ số)
CHIEU = {
    (0, "Cào"): ("lorc_claw-slashes", "V"), (0, "Than hồng"): ("lorc_fluffy-flame", "D"),
    (0, "Nanh lửa"): ("lorc_bestial-fangs", "V"), (0, "Xoáy lửa"): ("lorc_flame-spin", "D"),
    (0, "Màn khói"): ("darkzaitzev_smoke-bomb", "T"), (0, "Vũ điệu kiếm"): ("lorc_crossed-swords+mui_len", "T"),
    (0, "Súng phun lửa"): ("lorc_fire-breath", "D"), (0, "Vuốt rồng"): ("lorc_triple-claws", "V"),
    (0, "Đại hỏa"): ("lorc_fire-ring", "D"),
    (1, "Húc"): ("lorc_impact-point", "V"), (1, "Roi dây leo"): ("lorc_vine-whip", "V"), (1, "Lá dao"): ("lorc_zigzag-leaf", "V"),
    (1, "Bom bùn"): ("lorc_goo-explosion", "D"), (1, "Tự vệ"): ("lorc_checked-shield", "T"), (1, "Sinh trưởng"): ("lorc_sprout+mui_len", "T"),
    (1, "Bom hạt"): ("delapouite_plant-seed", "V"), (1, "Bão lá"): ("lorc_leaf-swirl", "D"), (1, "Tia mặt trời"): ("lorc_sunbeams", "D"),
    (2, "Húc"): ("lorc_impact-point", "V"), (2, "Tấn công nhanh"): ("delapouite_quick-man", "V"), (2, "Cắn"): ("skoll_fangs", "V"),
    (2, "Sao băng"): ("delapouite_falling-star", "D"), (2, "Mong ước"): ("delapouite_sparkles", "T"), (2, "Nghỉ ngơi"): ("lorc_sleepy", "T"),
    (2, "Phước lành hồi sinh"): ("lorc_angel-wings", "T"), (2, "Chia sẻ nỗi đau"): ("lorc_broken-heart", "T"),
    (2, "Đuôi sắt"): ("delapouite_fox-tail", "V"), (2, "Bứt phá cuối cùng"): ("lorc_bright-explosion", "V"),
    (2, "Tia phá hủy"): ("lorc_laser-blast", "D"),
    (3, "Húc"): ("lorc_impact-point", "V"), (3, "Súng nước"): ("lorc_water-bolt", "D"), (3, "Bong bóng"): ("lorc_bubbles", "D"),
    (3, "Xoáy nước"): ("lorc_vortex", "D"), (3, "Phá mai"): ("lorc_opening-shell", "T"), (3, "Rút vào mai"): ("lorc_turtle-shell", "T"),
    (3, "Đuôi nước"): ("delapouite_whale-tail", "V"), (3, "Bơm nước"): ("delapouite_water-fountain", "D"),
    (3, "Đại pháo nước"): ("lorc_cannon-shot", "D"),
    (4, "Tia lửa điện"): ("lorc_circle-sparks", "D"), (4, "Sóng điện"): ("lorc_static-waves", "D"), (4, "Cầu điện"): ("lorc_thunderball", "D"),
    (4, "Tia chớp"): ("lorc_focused-lightning", "D"), (4, "Nhanh nhẹn"): ("lorc_sprint+mui_len", "T"), (4, "Nạp điện"): ("sbed_battery-pack", "T"),
    (4, "Lưới điện"): ("lorc_spider-web", "D"), (4, "Sấm sét"): ("lorc_lightning-storm", "D"), (4, "Đâm sầm điện"): ("lorc_power-lightning", "V"),
}
# Hình dùng chung có lý do (tên chiêu -> lý do); --kiem: mọi cặp trùng hình khác phải có ở đây.
CHUNG_HINH = {"Húc": "cùng một chiêu Tackle ở ba dòng Pokémon"}

# ---------------------------------------------------------------- vẽ
SS = 8   # độ phân giải cao (ô 96 x 8) trước khi thu


def rgb(c):
    return ((c >> 16) & 255, (c >> 8) & 255, c & 255)


def pha(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


_HINH = {}


def mat_na_svg(ten, s):
    """Mặt nạ L cỡ s x s của hình SVG (game-icons: nền đen + hình trắng) - vẽ bằng ImageMagick."""
    k = (ten, s)
    if k in _HINH:
        return _HINH[k]
    duong = os.path.join(SVG, ten + ".svg")
    with tempfile.TemporaryDirectory() as d:
        ra = os.path.join(d, "a.png")
        subprocess.run(["convert", "-background", "black", "-density", str(max(72, s * 72 // 512 * 2)), duong,
                        "-resize", "%dx%d!" % (s, s), "-colorspace", "Gray", ra], check=True)
        m = Image.open(ra).convert("L")
    _HINH[k] = m
    return m


def mui_len(s):
    """Mũi tên lên nhỏ (chiêu tăng chỉ số) - mặt nạ s x s, góc trái dưới."""
    m = Image.new("L", (s, s), 0)
    d = ImageDraw.Draw(m)
    w = s * 0.3
    x0, y0 = s * 0.04, s * 0.62
    d.polygon([(x0 + w / 2, y0 - w * 0.55), (x0 + w, y0), (x0 + w * 0.68, y0), (x0 + w * 0.68, y0 + w * 0.6),
               (x0 + w * 0.32, y0 + w * 0.6), (x0 + w * 0.32, y0), (x0, y0)], fill=255)
    return m


def nguong(m, t=128):
    return m.point(lambda p: 255 if p >= t else 0)


def gian(m, px):
    """Nới mặt nạ px điểm ảnh (viền)."""
    for _ in range(px):
        m = m.filter(ImageFilter.MaxFilter(3))
    return m


def huy_hieu(he, hinh, loai, tron, s):
    """Huy hiệu pixel-art cỡ s (32/64/96): vẽ mặt nạ ở cỡ cao, thu BOX rồi cắt ngưỡng ở đúng cỡ s."""
    H = s * SS
    vien = 1 if s <= 32 else 2
    thuong = he == 0
    mau = (240, 230, 200) if thuong else rgb(MAU_HE[he])
    toi = (92, 78, 54) if thuong else pha(mau, (0, 0, 0), 0.62)
    toi2 = pha(mau, (0, 0, 0), 0.16)
    # khối (tròn / bo góc) ở cỡ cao -> cỡ s, cắt ngưỡng
    kh = Image.new("L", (H, H), 0)
    dk = ImageDraw.Draw(kh)
    if tron:
        dk.ellipse([0, 0, H - 1, H - 1], fill=255)
    else:
        dk.rounded_rectangle([0, 0, H - 1, H - 1], radius=int(H * 0.22), fill=255)
    khoi = nguong(kh.resize((s, s), Image.BOX))
    than = nguong(kh.resize((s - 2 * vien, s - 2 * vien), Image.BOX))
    than_d = Image.new("L", (s, s), 0)
    than_d.paste(than, (vien, vien))
    # hai tông: nửa dưới phải (đường chéo) tối hơn
    cheo = Image.new("L", (s, s), 0)
    ImageDraw.Draw(cheo).polygon([(s, s * 0.35), (s, s), (s * 0.35, s)], fill=255)
    anh = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    anh.paste(toi + (255,), (0, 0), khoi)
    anh.paste(mau + (255,), (0, 0), than_d)
    anh.paste(toi2 + (255,), (0, 0), ImageChops.multiply(than_d, cheo))
    # hình: hộp 0.6 s (tròn: lệch lên chừa nhãn)
    ten, _, phu = hinh.partition("+")
    k = int(s * (0.56 if tron else 0.62))
    hm = mat_na_svg(ten, k * SS).copy()
    if phu == "mui_len":
        mm = mui_len(k * SS)
        # khoảng hở quanh mũi tên
        hm = ImageChops.subtract(hm, gian(mm.resize((k, k), Image.BOX), 2).resize((k * SS, k * SS)))
        hm = ImageChops.lighter(hm, mm)
    hk = nguong(hm.resize((k, k), Image.BOX), 110)
    ox = (s - k) // 2
    oy = int(s * (0.12 if tron else 0.19))
    mk = Image.new("L", (s, s), 0)
    mk.paste(hk, (ox, oy))
    mk = ImageChops.multiply(mk, than_d)
    vk = ImageChops.multiply(gian(mk, vien), than_d)
    mau_hinh = (58, 48, 32) if thuong else (255, 255, 255)
    vien_hinh = (255, 250, 236) if thuong else toi
    anh.paste(vien_hinh + (255,), (0, 0), vk)
    anh.paste(mau_hinh + (255,), (0, 0), mk)
    if tron:
        return anh
    # chấm LOẠI góc phải trên (pixel): vật lý cam chớp nổ, đặc biệt xanh vòng, trạng thái xám tròn
    r = max(4, int(s * 0.15))
    cx, cy = s - r - 1, r + 1
    p = Image.new("RGBA", (s * SS, s * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(p)
    R = r * SS
    CX, CY = cx * SS, cy * SS
    c1, c2 = {"V": ((244, 128, 40), (122, 50, 12)), "D": ((72, 140, 232), (26, 58, 128)), "T": ((168, 174, 186), (70, 76, 90))}[loai]
    d.ellipse([CX - R, CY - R, CX + R, CY + R], fill=c2 + (255,))
    d.ellipse([CX - R * 0.78, CY - R * 0.78, CX + R * 0.78, CY + R * 0.78], fill=c1 + (255,))
    if loai == "V":
        pts = []
        for i in range(8):
            rr = R * (0.7 if i % 2 == 0 else 0.28)
            a = math.radians(-90 + 45 * i)
            pts.append((CX + rr * math.cos(a), CY + rr * math.sin(a)))
        d.polygon(pts, fill=(255, 255, 255, 255))
    elif loai == "D":
        d.ellipse([CX - R * 0.55, CY - R * 0.55, CX + R * 0.55, CY + R * 0.55], outline=(255, 255, 255, 255), width=max(1, int(R * 0.18)))
        d.ellipse([CX - R * 0.18, CY - R * 0.18, CX + R * 0.18, CY + R * 0.18], fill=(255, 255, 255, 255))
    else:
        d.ellipse([CX - R * 0.36, CY - R * 0.36, CX + R * 0.36, CY + R * 0.36], fill=(255, 255, 255, 255))
    p = p.resize((s, s), Image.BOX)
    a = p.split()[3].point(lambda v: 255 if v >= 128 else 0)
    anh.paste(p.convert("RGB"), (0, 0), a)
    return anh


def luu(anh, duong):
    """PNG bảng màu (ảnh pixel-art: ít màu, không khuếch tán)."""
    anh.quantize(256, method=Image.FASTOCTREE, dither=Image.NONE).save(duong, optimize=True)


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
    dung = {}
    for dong, so, ten, he, _ in ds:
        csv_ten.setdefault(dong, []).append(ten)
        if (dong, ten) not in CHIEU:
            loi.append("chiêu chưa có hình: dòng %d #%d %s" % (dong, so, ten))
            continue
        hinh = CHIEU[(dong, ten)][0]
        if not os.path.isfile(os.path.join(SVG, hinh.partition("+")[0] + ".svg")):
            loi.append("thiếu SVG: %s (%s)" % (hinh, ten))
        dung.setdefault(hinh, set()).add(ten)
    for hinh, cac in dung.items():
        if len(cac) > 1:
            loi.append("hai chiêu khác nhau chung một hình %s: %s" % (hinh, ", ".join(sorted(cac))))
        elif len([1 for (d, t), v in CHIEU.items() if v[0] == hinh]) > 1 and list(cac)[0] not in CHUNG_HINH:
            loi.append("chiêu %s dùng chung hình %s ở nhiều dòng mà không có lý do (CHUNG_HINH)" % (list(cac)[0], hinh))
    # SVG thừa (giữ đúng tệp đang dùng)
    co = set(f[:-4] for f in os.listdir(SVG) if f.endswith(".svg")) if os.path.isdir(SVG) else set()
    for f in sorted(co - set(h.partition("+")[0] for h in dung)):
        loi.append("SVG không dùng (xoá): %s.svg" % f)
    for dong, so, ten, he, _ in ds:
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
    print("KIEM icon chieu: %d chieu, %d hinh rieng, 0 loi" % (len(ds), len(set(CHIEU[(d, t)][0] for d, _, t, _, _ in ds))))
    if "--kiem" in sys.argv:
        return
    os.makedirs(RA, exist_ok=True)
    os.makedirs(RA_WEB, exist_ok=True)
    hang = (len(ds) + COT - 1) // COT
    for tron in (False, True):
        tam = {s: Image.new("RGBA", (COT * s, hang * s), (0, 0, 0, 0)) for s in CO}
        for i, (dong, so, ten, he, _) in enumerate(ds):
            hinh, loai = CHIEU[(dong, ten)]
            for s in CO:
                a = huy_hieu(TEN_HE.index(he), hinh, loai, tron, s)
                tam[s].paste(a, ((i % COT) * s, (i // COT) * s))
                if s == 64 and not tron:
                    luu(a, os.path.join(RA_WEB, "%d-%d.png" % (dong, so)))
        for s in CO:
            ten_tep = "chieu_%s%d.png" % ("tron_" if tron else "", s)
            luu(tam[s], os.path.join(RA, ten_tep))
            print("ghi", os.path.relpath(os.path.join(RA, ten_tep), GOC), tam[s].size)
    print("ghi", os.path.relpath(RA_WEB, GOC), "(%d icon 64 px)" % len(ds))


if __name__ == "__main__":
    main()
