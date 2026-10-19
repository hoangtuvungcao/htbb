"""Kiểm "CÒN SÓT KIỂU CŨ" (0.9.7): màn nào còn vẽ kiểu Pokémon GO 0.9.6 (thẻ TRẮNG phẳng, teal, xám xanh nhạt) thay vì
giao diện NRO (gỗ / giấy da / nút khối).

    python3 tools/giao_dien_nro/kiem_kieu_cu.py <thư mục ảnh ChupTrongGame / ChupTruocGame> [--nguong-trang 1.2]

Hai lớp kiểm:
 1. ẢNH: mỗi ảnh chụp đếm tỉ lệ điểm ảnh TRẮNG gần tuyệt đối (mọi kênh >= 246), TEAL (#1FB5A8 ±24) và XÁM XANH GO
    (#DFE6EB / #EEF3F6 / #E2E9EE ±6). Bảng màu NRO không có mảng trắng / teal / xám xanh lớn (chữ trắng viền chỉ
    chiếm vài phần nghìn), nên vượt ngưỡng = màn còn kiểu cũ.
 2. MÃ: tìm trong client_jar/src mẫu vẽ kiểu cũ (khối trắng 0xFFFFFF, hằng teal 0x1FB5A8 / ChuDe.TEAL, nền navy cũ
    0x101828 / 0x0B1222) ngoài danh sách cho phép.
Thoát 1 nếu có màn / dòng mã bị gắn cờ.
"""
import os
import re
import sys

import numpy as np
from PIL import Image

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))

# màn có nội dung trắng thật (sprite Pokémon trắng, bản đồ tuyết...) - ngưỡng nới
NOI = ("pokedex", "the_gioi", "the_gioi_cuoi", "goi_y_chieu", "suc_khoe", "thong_bao_kich_hoat", "hop_vat_pham", "tai_", "may_chu",
       "dang_nhap", "dang_ky", "chon_nhan_vat", "tao_nhan_vat")

MAU_GO = [(0xDF, 0xE6, 0xEB), (0xEE, 0xF3, 0xF6), (0xE2, 0xE9, 0xEE), (0xF6, 0xF9, 0xFA)]

# mẫu mã cũ: (regex, mô tả)
MAU_MA = [
    (r"khoi(?:Doc)?\([^;]*,\s*0xFFFFFF\)\s*;", "khối TRẮNG phẳng (thẻ GO)"),
    (r"0x1FB5A8|0x2FCDBE|0x138C82|ChuDe\.TEAL", "màu teal 0.9.6"),
    (r"0x101828|0x0B1222", "nền navy cũ"),
]
# dòng được phép (định nghĩa hằng cũ giữ cho tương thích / bóng)
CHO_PHEP = ("ChuDe.java", "GiaoDien.java:3", "HuyHieuPoke.java", "NgoaiHinhVe.java")


def kiem_anh(thu_muc, nguong_trang):
    loi = []
    tong = 0
    for ten in sorted(os.listdir(thu_muc)):
        if not ten.endswith(".png"):
            continue
        tong += 1
        a = np.asarray(Image.open(os.path.join(thu_muc, ten)).convert("RGB")).astype(np.int16)
        n = a.shape[0] * a.shape[1]
        trang = np.all(a >= 246, axis=2).sum() / n * 100
        teal = (np.abs(a - np.array([0x1F, 0xB5, 0xA8])) <= 24).all(axis=2).sum() / n * 100
        xam = 0
        for m in MAU_GO:
            xam += (np.abs(a - np.array(m)) <= 6).all(axis=2).sum()
        xam = xam / n * 100
        noi = any(ten.startswith(p) for p in NOI)
        nt = nguong_trang * (4 if noi else 1)
        if trang > nt or teal > 0.4 or xam > 1.5:
            loi.append("%s: trắng %.2f%% (ngưỡng %.1f), teal %.2f%%, xám xanh GO %.2f%%" % (ten, trang, nt, teal, xam))
    return tong, loi


def kiem_ma():
    loi = []
    src = os.path.join(GOC, "client_jar", "src")
    for g, _, ts in os.walk(src):
        for t in ts:
            if not t.endswith(".java"):
                continue
            p = os.path.join(g, t)
            for i, d in enumerate(open(p, encoding="utf-8"), 1):
                for mau, mo in MAU_MA:
                    if re.search(mau, d):
                        vt = "%s:%d" % (t, i)
                        if any(vt.startswith(c) or t == c for c in CHO_PHEP):
                            continue
                        loi.append("%s: %s | %s" % (os.path.relpath(p, GOC) + ":" + str(i), mo, d.strip()[:110]))
    return loi


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    nguong = 1.2
    if "--nguong-trang" in sys.argv:
        nguong = float(sys.argv[sys.argv.index("--nguong-trang") + 1])
    bao = 0
    for tm in [a for a in sys.argv[1:] if os.path.isdir(a)]:
        tong, loi = kiem_anh(tm, nguong)
        print("ẢNH %s: %d ảnh, %d còn kiểu cũ" % (tm, tong, len(loi)))
        for l in loi:
            print("  CŨ " + l)
        bao += len(loi)
    lm = kiem_ma()
    print("MÃ: %d dòng kiểu cũ" % len(lm))
    for l in lm:
        print("  CŨ " + l)
    bao += len(lm)
    print("kiem_kieu_cu: %s (%d)" % ("ĐẠT" if bao == 0 else "HỎNG", bao))
    sys.exit(1 if bao else 0)


main()
