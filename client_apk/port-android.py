"""Chép code mod dùng chung của bản jar sang bản Android (APK TeaMobi), đổi tên lớp cho khớp APK.

    python -I port-android.py <Jar276/src/game/render/screen> <thư mục ra>

Bản jar (Jar276) là nguồn duy nhất của các tính năng mod; Android chỉ khác:
- tên lớp / package (bảng DOI_TEN dưới đây - APK TeaMobi đổi tên khi port từ J2ME sang Android),
- chỗ khác nhau thật đi qua lớp đệm Nen (Jar276/.../Nen.java <-> Android277/src/.../Nen.java),
- KiemServer / TaiDanhSach / PhienBan: Android có bản riêng (src/ và build.sh), không chép.
"""
import os
import re
import sys

CHUNG = ["AutoGuard", "PauseMenu", "AutoUp", "AutoVanTieu", "AutoPk", "AutoVanMay", "TienIch", "LenThanh", "HieuUng"]

# (mẫu regex, thay bằng) - áp theo thứ tự
DOI_TEN = [
    (r"\bimport game\.render\.Canvas;", "import game.render.GCanvas;"),
    (r"\bgame\.render\.Canvas\b", "game.render.GCanvas"),
    (r"(?<![\w.])Canvas\b", "GCanvas"),                       # lớp Canvas của game (J2ME) = GCanvas (APK)
    (r"\bjavax\.microedition\.lcdui\.Graphics\b", "game.core.j2me.Graphics"),
    (r"\bimport game\.GameMidlet;", "import com.teamobi.kpah.GameMidlet;"),
    (r"\bgame\.GameMidlet\b", "com.teamobi.kpah.GameMidlet"),
    (r"\bimport game\.model\.ShopNew;\n", ""),                 # APK: game.render.screen.ShopNew (cùng package)
    (r"\bgame\.model\.ShopNew\b", "game.render.screen.ShopNew"),
    (r"\bFont\.arialFontW\b", "Font.arialFont11"),
]

src, out = sys.argv[1], sys.argv[2]
os.makedirs(out, exist_ok=True)
for ten in CHUNG:
    s = open(os.path.join(src, ten + ".java"), encoding="utf-8").read()
    for mau, thay in DOI_TEN:
        s = re.sub(mau, thay, s)
    open(os.path.join(out, ten + ".java"), "w", encoding="utf-8").write(s)
print("port-android: %d file -> %s" % (len(CHUNG), out))
