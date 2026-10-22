#!/usr/bin/env python3
"""
Sinh server/map/loai/GHI_CONG.txt: ghi công người vẽ (PMDCollab SpriteCollab, CC BY-NC 4.0) cho MỌI loài có gói tải
theo nhu cầu trong server/map/loai/<NNNN>.goi. Cùng kiểu với client_jar/res/GHI_CONG.txt (client_jar/tools/sinh_ghi_cong.py):
dòng "# ..." là tiêu đề mục, còn lại là chữ thường; tên người vẽ đổi từ mã Discord bằng
client_jar/tools/pmdcollab_credit_names.txt. Không sửa tệp ghi công của jar (tệp đó vẫn ghi đủ loài đóng trong jar).

    python3 -I tools/pokemon/loai/ghi_cong_loai.py

Nguồn: tools/pokemon/pmd_raw/<NNNN>/credits.txt, <NNNN>s/credits.txt (bản sắc màu) (tai_sprite_loai.py ở commit ghim)
và tools/pokemon/pmd_raw/chan_dung/<NNNN>/credits.txt (chân dung tĩnh, tai_chan_dung_loai.py).
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


def doc_nguoi_ve(sg, ten, cr):
    ds = []
    if os.path.exists(cr):
        with open(cr, encoding="utf-8") as f:
            for d in f:
                p = d.rstrip("\n").split("\t")
                if len(p) >= 2:
                    n = sg.ten_nguoi_ve(p[1].strip(), ten)
                    if n not in ds:
                        ds.append(n)
    return ds


def doc_nhap():
    """khoa -> nhan ("bản nháp" / "dạng <ten>") tu du_lieu/loai/sprite_nhap.csv (tai_sprite_nhap.py)."""
    kq = {}
    f = os.path.join(GOC, "du_lieu", "loai", "sprite_nhap.csv")
    if os.path.isfile(f):
        for d in open(f, encoding="utf-8").read().splitlines()[1:]:
            p = d.split(",")
            if len(p) >= 4:
                if p[1] == "smogon":
                    kq[p[0]] = "Smogon Sprite Project, sprite trận đấu đổi sang 4 hướng"
                elif p[1] == "bw_goc":
                    kq[p[0]] = "sprite gốc Pokémon Black / White (Game Freak), đổi sang 4 hướng"
                elif p[1] == "cong_dong":
                    kq[p[0]] = "sprite cộng đồng, đổi sang 4 hướng"
                else:
                    kq[p[0]] = "bản nháp PMDCollab, " + p[1] if p[3] == "1" else "dạng %s, %s" % (p[2], p[1])
    return kq


def main():
    sg = nap_sinh_ghi_cong()
    nhap = doc_nhap()
    ten = sg.doc_ten()
    loai, shiny, chan_dung = [], [], []
    for t in sorted(os.listdir(THU_MUC_GOI)):
        if not t.endswith(".goi"):
            continue
        k = t[:-4]
        (shiny if k.endswith("s") else loai).append((k, doc_nguoi_ve(sg, ten, os.path.join(RAW, k, "credits.txt"))))
    # chan dung Pokedex tinh (loai chua co goi: tools/pokemon/loai/tai_chan_dung_loai.py)
    tm_cd = os.path.join(RAW, "chan_dung")
    for k in sorted(os.listdir(tm_cd)) if os.path.isdir(tm_cd) else []:
        if os.path.isfile(os.path.join(THU_MUC_GOI, k + ".goi")) or not os.path.isfile(
                os.path.join(GOC, "web", "public", "pokemon", "loai", "%d.png" % int(k))):
            continue   # chi ghi cong chan dung tinh dang dung (co goi thi da ghi o tren)
        chan_dung.append((k, doc_nguoi_ve(sg, ten, os.path.join(tm_cd, k, "credits.txt"))))
    tat_ca = {}
    for _, ds in loai + shiny + chan_dung:
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
        "Bản sắc màu (shiny) là sprite sắc màu của chính PMDCollab, không tự đổi màu.",
        "Loài ghi (bản nháp ...) dùng sprite CHƯA HOÀN THIỆN của PMDCollab; (dạng ...) dùng một dạng khác của chính loài đó.",
        "Động tác còn thiếu dùng lại khung của chính người vẽ (CopyOf), không vẽ thêm.",
        "",
        "# Sprite ngoài PMDCollab (42 loài PMDCollab chưa có; chủ game chấp nhận, đang xin giấy phép; du_lieu/loai/sprite_ngoai.csv)",
        "Smogon Sprite Project - https://www.smogon.com/forums/threads/smogon-sprite-project.3647722/ (dùng phi lợi nhuận, ghi công người vẽ / dự án)",
        "Bảng ghi công: Gen 6 https://docs.google.com/spreadsheets/d/1Gn0UORn-unvcbUeQhQdEBz0ADNcH49BZZqQ1dpXm9eo ; Gen 7 https://docs.google.com/spreadsheets/d/1FMcHbSKEWZc7v2Ur4cyJjT_NhO0gqXyU9kDhsOQhlBQ ; Gen 8 https://docs.google.com/spreadsheets/d/1acgzAjh0dnFRQnjZu8kSjS177rKCzpFfEHRLtwuuXRU ; Gen 9 https://docs.google.com/spreadsheets/d/1MCjDktTOOFjLKM5C-RW6SfBQGkjlxDSCZAZDma_ItuA",
        "Gen 5: sprite gốc Pokémon Black / White (Game Freak / Nintendo), bản lưu trên https://play.pokemonshowdown.com/sprites/gen5/",
        "Ảnh trận đấu (trước / sau) giữ nguyên điểm ảnh, thu nhỏ lân cận gần nhất, lật gương cho hướng phải / trái-sau.",
        "Nhân vật dựa trên Pokémon Mystery Dungeon (Chunsoft / Spike Chunsoft).",
        "",
        "# Người vẽ (%d)" % len(nguoi),
        ", ".join(nguoi),
        "",
        "# Người vẽ theo từng loài (%d loài)" % len(loai),
    ]
    for k, ds in loai:
        r.append("#%s: %s%s" % (k, ", ".join(ds) if ds else "?", " (%s)" % nhap[k] if k in nhap else ""))
    if shiny:
        r += ["", "# Người vẽ bản sắc màu theo từng loài (%d loài)" % len(shiny)]
        for k, ds in shiny:
            r.append("#%s: %s%s" % (k, ", ".join(ds) if ds else "?", " (%s)" % nhap[k] if k in nhap else ""))
    if chan_dung:
        r += ["", "# Người vẽ chân dung Pokédex tĩnh (loài chưa có gói sprite, %d loài; web/public/pokemon/loai)" % len(chan_dung)]
        for k, ds in chan_dung:
            r.append("#%s: %s" % (k, ", ".join(ds) if ds else "?"))
    ra = os.path.join(THU_MUC_GOI, "GHI_CONG.txt")
    with open(ra, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(sg.sach(x) if not x.startswith("#") and x else x for x in r) + "\n")
    thieu = [k for k, ds in loai + shiny + chan_dung if not ds]
    print("Da ghi %s: %d loai, %d ban sac mau, %d chan dung tinh, %d nguoi ve%s" % (
        os.path.relpath(ra, GOC), len(loai), len(shiny), len(chan_dung), len(nguoi),
        (", THIEU ghi cong: " + " ".join(thieu)) if thieu else ""))
    return 1 if thieu else 0


if __name__ == "__main__":
    sys.exit(main())
