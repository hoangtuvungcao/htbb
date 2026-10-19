#!/usr/bin/env python3
"""Soát CHỮ GIAO DIỆN còn sót (0.9.5): chữ thô / gỡ lỗi / từ cũ kiếm hiệp trong chữ người chơi thấy.

    python3 -I tools/pokemon/kiem_chuoi_ui.py [chuoi_da_ve.txt ...]

1) TĨNH: chuỗi "..." giống chữ trong mã giao diện client (client_jar/src/game/pokemon, .../render/screen,
   client_pc/src/htbb/pc, client_apk .../hd) - bỏ chú thích, dòng log, bảng từ điển (ChuPokemon, TenMapPokemon).
2) LÚC CHẠY: mọi chuỗi THẬT SỰ đã vẽ ra màn khi chụp mọi màn (client_pc/test/ChupTrongGame ghi chuoi_da_ve.txt) -
   đã qua đổi chữ của game, nên soát cả TỪ CẤM (server ChuPokemon.TU_CAM, ngoại lệ CHO_PHEP).

Mẫu lỗi: "ID:" / "#ID", "cấp …ID", chữ "null", chỗ giữ chỗ chưa thay ("%s", "%d", "{}", "{0}"), toạ độ thô ("x: 12",
"(12, 34)"). Mã thoát 1 = có lỗi.
"""
import importlib.util
import os
import re
import sys

_D = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location("chu_pokemon", os.path.join(_D, "chu_pokemon.py"))
cp = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(cp)
_sq = importlib.util.spec_from_file_location("quet_tu_cam", os.path.join(_D, "quet_tu_cam.py"))
qt = importlib.util.module_from_spec(_sq)
_sq.loader.exec_module(qt)

GOC = os.path.normpath(os.path.join(_D, "..", ".."))
THU_MUC = ["client_jar/src/game/pokemon", "client_jar/src/game/render/screen", "client_jar/shared/mod/game/render/screen",
           "client_pc/src/htbb/pc", "client_apk/pokemon/src/main/java/com/htbb/pokemon/hd"]
BO_QUA = {"ChuPokemon.java", "TenMapPokemon.java", "IconBang.java"}
# log / gỡ lỗi / khoá giao thức (không tới người chơi)
DONG_BO = re.compile(r"System\.(out|err)|printStackTrace|GiamSat\.loi|Log\.[dewi]\(|getProperty|getDeclaredField|"
                     r"Class\.forName|setProperty|\.put\(\"|\.get\(\"|\"vung\"|VungCham\.them|chamVung|dangKy|"
                     r"getResource|createImage|\"/|startsWith\(|indexOf\(|String\.format\(|equals\(")

MAU = [
    ("ID: thô", re.compile(r"\bID\s*:|#ID\b")),
    ("cấp ...ID", re.compile(r"cấp\s*\d*\s*ID", re.I)),
    ("chữ null", re.compile(r"(^|[\s:(\[])null($|[\s.,)\]])")),
    ("giữ chỗ chưa thay", re.compile(r"%[sd]\b|\{\d?\}")),
    ("toạ độ thô", re.compile(r"\b[xy]\s*[:=]\s*-?\d+|\(\s*-?\d+\s*,\s*-?\d+\s*\)")),
]


def tim_mau(t):
    return [ten for ten, m in MAU if m.search(t)]


def quet_tinh():
    loi = []
    for g in THU_MUC:
        d = os.path.join(GOC, g)
        if not os.path.isdir(d):
            continue
        for ten in sorted(os.listdir(d)):
            if not ten.endswith(".java") or ten in BO_QUA:
                continue
            p = os.path.join(d, ten)
            s = qt.bo_chu_thich(open(p, encoding="utf-8", errors="replace").read())
            for so, dong in enumerate(s.split("\n"), 1):
                if DONG_BO.search(dong):
                    continue
                for m in qt._LIT.finditer(dong):
                    t = cp.bo_thoat_java(m.group(1))
                    if not qt.giong_chu(t):
                        continue
                    for k in tim_mau(t):
                        loi.append("%s:%d [%s] %r" % (os.path.join(g, ten), so, k, t))
    return loi


def quet_chay(tep, cam):
    loi = []
    for dong in open(tep, encoding="utf-8", errors="replace"):
        t = dong.rstrip("\n")
        if not t.strip():
            continue
        for k in tim_mau(t):
            loi.append("%s [%s] %r" % (os.path.basename(tep), k, t))
        g = cam.tim(t)
        if g:
            loi.append("%s [từ cấm %s] %r" % (os.path.basename(tep), "/".join(g), t))
    return loi


def main():
    b = cp.doc_bang()
    cam = qt.CamTho(cp.tu_dien_cam(b))
    loi = quet_tinh()
    n_tinh = len(loi)
    for tep in sys.argv[1:]:
        loi += quet_chay(tep, cam)
    for l in loi:
        print("LOI: " + l)
    print("kiem_chuoi_ui: tinh %d loi, luc chay %d loi (%d tep)" % (n_tinh, len(loi) - n_tinh, len(sys.argv) - 1))
    sys.exit(1 if loi else 0)


if __name__ == "__main__":
    main()
