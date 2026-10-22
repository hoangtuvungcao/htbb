#!/usr/bin/env python3
"""
Kiem ke sprite cho DU 1.025 loai: repo dang co gi, PMDCollab (tracker.json o commit ghim) co gi, may chu da co goi
tai theo nhu cau (server/map/loai/<NNNN>.goi) chua.

    python3 -I tools/pokemon/loai/kiem_ke_sprite_loai.py

Ra du_lieu/kiem_ke_sprite_loai.csv (commit; KHONG sua tay) + in tom tat theo the he. Khong dung mang.

Cot:
  dex, ten (ten tieng Anh theo tracker), the_he (1..9 theo dai so dex),
  trong_jar      1 = client_jar/res/pk/<NNNN>.dl co san trong jar
  trong_unity    1 = client_pc_ios/Assets/Resources/poke/<NNNN>_anh.bytes co
  pmd_sprite     sprite_complete cua tracker (0 thieu / 1 dung duoc / 2 du dong tac); 0 neu khong co Walk
  pmd_walk       1 = co dong tac Walk
  pmd_chan_dung  1 = co chan dung Normal (portrait_complete > 0)
  goi_server     1 = server/map/loai/<NNNN>.goi da dong goi (tools/pokemon/loai/DongGoiLoai.java)
  kich_thuoc_goi so byte cua goi
  ghi_cong       nguoi ve sprite chinh (ten hien thi theo client_jar/tools/pmdcollab_credit_names.txt)
"""
import csv
import json
import os
import re
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
COMMIT = "4702f10d888d19e804c5986a91170dfc92ae01b8"
TRACKER = os.path.join(GOC, "tools", "pokemon", "nguon_loai", "pmdcollab-" + COMMIT, "tracker.json")
TEN_DISCORD = os.path.join(GOC, "client_jar", "tools", "pmdcollab_credit_names.txt")
RA = os.path.join(GOC, "du_lieu", "kiem_ke_sprite_loai.csv")

# dex cuoi cua tung the he
DAI_THE_HE = [151, 251, 386, 493, 649, 721, 809, 905, 1025]


def the_he(dex):
    for i, cuoi in enumerate(DAI_THE_HE):
        if dex <= cuoi:
            return i + 1
    return 0


def doc_ten():
    ten = {}
    if not os.path.exists(TEN_DISCORD):
        return ten
    with open(TEN_DISCORD, encoding="utf-8") as f:
        for i, dong in enumerate(f):
            p = dong.rstrip("\n").split("\t")
            if i == 0 or len(p) < 2:
                continue
            ten[p[1].strip()] = p[0].strip()
    return ten


def ten_nguoi(ma, ten):
    if not ma:
        return ""
    if ma in ten:
        return ten[ma]
    m = re.match(r"<@!?(\d+)>", ma)
    if m:
        for k, v in ten.items():
            if m.group(1) in k:
                return v
        return "Discord " + m.group(1)
    return ma


def main():
    with open(TRACKER, encoding="utf8") as f:
        tracker = json.load(f)
    ten_ve = doc_ten()
    dong = []
    tk = {}
    for dex in range(1, 1026):
        k = "%04d" % dex
        e = tracker.get(k) or {}
        files = e.get("sprite_files") or {}
        walk = 1 if "Walk" in files else 0  # khoa co mat = co dong tac (gia tri bool la co khoa, khong phai co/khong)
        sprite = int(e.get("sprite_complete", 0) or 0) if walk else 0
        cd = 1 if int(e.get("portrait_complete", 0) or 0) > 0 and "Normal" in (e.get("portrait_files") or {}) else 0
        jar = 1 if os.path.isfile(os.path.join(GOC, "client_jar", "res", "pk", k + ".dl")) else 0
        unity = 1 if os.path.isfile(os.path.join(GOC, "client_pc_ios", "Assets", "Resources", "poke", k + "_anh.bytes")) else 0
        goi = os.path.join(GOC, "server", "map", "loai", k + ".goi")
        co_goi = 1 if os.path.isfile(goi) else 0
        kt = os.path.getsize(goi) if co_goi else 0
        gc = ten_nguoi((e.get("sprite_credit") or {}).get("primary", ""), ten_ve) if walk else ""
        g = the_he(dex)
        dong.append([dex, e.get("name", ""), g, jar, unity, sprite, walk, cd, co_goi, kt, gc])
        t = tk.setdefault(g, [0] * 7)
        t[0] += 1
        t[1] += 1 if (jar or unity) else 0
        t[2] += 1 if sprite >= 1 else 0
        t[3] += 1 if sprite >= 2 else 0
        t[4] += cd
        t[5] += 0 if sprite >= 1 else 1
        t[6] += co_goi
    with open(RA, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["dex", "ten", "the_he", "trong_jar", "trong_unity", "pmd_sprite", "pmd_walk", "pmd_chan_dung",
                    "goi_server", "kich_thuoc_goi", "ghi_cong"])
        w.writerows(dong)
    print("the_he so_loai repo_co pmd_dung_duoc pmd_du chan_dung thieu_pmd goi_server")
    tong = [0] * 7
    for g in sorted(tk):
        print("%6d %7d %7d %13d %6d %9d %9d %10d" % ((g,) + tuple(tk[g])))
        tong = [a + b for a, b in zip(tong, tk[g])]
    print("  tong %7d %7d %13d %6d %9d %9d %10d" % tuple(tong))
    print("Da ghi %s" % os.path.relpath(RA, GOC))
    return 0


if __name__ == "__main__":
    sys.exit(main())
