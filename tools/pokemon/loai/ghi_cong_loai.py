#!/usr/bin/env python3
"""
Sinh server/map/loai/GHI_CONG.txt: ghi công người vẽ (PMDCollab SpriteCollab, CC BY-NC 4.0) cho MỌI loài có gói tải
theo nhu cầu trong server/map/loai/<NNNN>.goi. Cùng kiểu với client_jar/res/GHI_CONG.txt (client_jar/tools/sinh_ghi_cong.py):
dòng "# ..." là tiêu đề mục, còn lại là chữ thường; tên người vẽ đổi từ mã Discord bằng
client_jar/tools/pmdcollab_credit_names.txt. Không sửa tệp ghi công của jar (tệp đó vẫn ghi đủ loài đóng trong jar).

    python3 -I tools/pokemon/loai/ghi_cong_loai.py

Nguồn: tools/pokemon/pmd_raw/<NNNN>/credits.txt (tải bởi tai_sprite_loai.py ở commit ghim).
"""
import types
import os
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
RAW = os.path.join(GOC, "tools", "pokemon", "pmd_raw")
THU_MUC_GOI = os.path.join(GOC, "server", "map", "loai")
COMMIT = "4702f10d888d19e804c5986a91170dfc92ae01b8"


def nap_sinh_ghi_cong():
    """Dung lai doc_ten / ten_nguoi_ve / sach cua client_jar/tools/sinh_ghi_cong.py (tep do goi main() o cuoi,
    khong co chan __name__: bo dong goi do truoc khi thuc thi)."""
    duong = os.path.join(GOC, "client_jar", "tools", "sinh_ghi_cong.py")
    with open(duong, encoding="utf-8") as f:
        nguon = f.read()
    nguon = "\n".join(d for d in nguon.split("\n") if d.strip() != "main()")
    m = types.ModuleType("sinh_ghi_cong")
    m.__file__ = duong
    exec(compile(nguon, duong, "exec"), m.__dict__)
    return m


def main():
    sg = nap_sinh_ghi_cong()
    ten = sg.doc_ten()
    loai = []
    for t in sorted(os.listdir(THU_MUC_GOI)):
        if not t.endswith(".goi"):
            continue
        k = t[:-4]
        ds = []
        cr = os.path.join(RAW, k, "credits.txt")
        if os.path.exists(cr):
            with open(cr, encoding="utf-8") as f:
                for d in f:
                    p = d.rstrip("\n").split("\t")
                    if len(p) >= 2:
                        n = sg.ten_nguoi_ve(p[1].strip(), ten)
                        if n not in ds:
                            ds.append(n)
        loai.append((k, ds))
    tat_ca = {}
    for _, ds in loai:
        for n in ds:
            tat_ca[n] = tat_ca.get(n, 0) + 1
    nguoi = sorted(tat_ca, key=lambda n: (n != "CHUNSOFT", n.lower()))
    r = [
        "# Huyền Thoại Bảo Bối - Ghi công hình Pokémon tải theo nhu cầu",
        "Game fan phi thương mại. Không liên kết, không được bảo trợ bởi Nintendo, Creatures, GAME FREAK hay The Pokémon Company.",
        "Pokémon (c) Nintendo / Creatures / GAME FREAK / The Pokémon Company.",
        "",
        "# Hình Pokémon",
        "PMDCollab SpriteCollab - https://github.com/PMDCollab/SpriteCollab (commit %s)" % COMMIT,
        "Giấy phép CC BY-NC 4.0 (phải ghi công, không dùng cho mục đích thương mại): https://creativecommons.org/licenses/by-nc/4.0/",
        "Hình đã được thu nhỏ / cắt khung / đổi định dạng để chạy trên điện thoại.",
        "Nhân vật dựa trên Pokémon Mystery Dungeon (Chunsoft / Spike Chunsoft).",
        "",
        "# Người vẽ (%d)" % len(nguoi),
        ", ".join(nguoi),
        "",
        "# Người vẽ theo từng loài (%d loài)" % len(loai),
    ]
    for k, ds in loai:
        r.append("#%s: %s" % (k, ", ".join(ds) if ds else "?"))
    ra = os.path.join(THU_MUC_GOI, "GHI_CONG.txt")
    with open(ra, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(sg.sach(x) if not x.startswith("#") and x else x for x in r) + "\n")
    thieu = [k for k, ds in loai if not ds]
    print("Da ghi %s: %d loai, %d nguoi ve%s" % (os.path.relpath(ra, GOC), len(loai), len(nguoi),
                                                 (", THIEU ghi cong: " + " ".join(thieu)) if thieu else ""))
    return 1 if thieu else 0


if __name__ == "__main__":
    sys.exit(main())
