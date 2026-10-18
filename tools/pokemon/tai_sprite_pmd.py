#!/usr/bin/env python3
"""
Tai sprite PMDCollab/SpriteCollab cho MOI loai can dung + kiem ke thieu dong tac.

Danh sach loai = dex trong du_lieu/bang_quai.csv, bang_thu.csv, bang_npc.csv,
bang_npc_su_kien.csv, bang_pet_cu.csv
+ moi dex trong src/com/htbb/pokemon/LoaiPokemon.java (DU_LIEU). Khong co danh sach tay nao khac.

Ra:
  tools/pokemon/pmd_raw/<dex>[s]/   AnimData.xml, <Anim>-Anim.png, <Anim>-Shadow.png,
                                    portrait.png, credits.txt  (KHONG commit, xem .gitignore)
  du_lieu/kiem_ke_sprite.csv   bang kiem ke (commit) - nguoi lam asset doc file nay

Chay (o thu muc HTBB/):
    python3 -I tools/pokemon/tai_sprite_pmd.py           # tai + kiem ke
    python3 -I tools/pokemon/tai_sprite_pmd.py --chi-kiem # chi kiem ke tu file da tai

GIAY PHEP: sprite PMDCollab la CC BY-NC 4.0 - KHONG thuong mai, PHAI ghi cong
(credits.txt cua tung loai). Xem muc "Giay phep" trong docs/kien-truc.md.

Ma thoat 1 neu co loai thieu dong tac BAT BUOC ma khong co dong tac thay the.
"""
import csv
import os
import re
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RAW = os.path.join(GOC, "tools", "pokemon", "pmd_raw")
BANG_QUAI = os.path.join(GOC, "du_lieu", "bang_quai.csv")
LOAI_JAVA = os.path.join(GOC, "server", "src", "com", "htbb", "pokemon", "LoaiPokemon.java")
KIEM_KE = os.path.join(GOC, "du_lieu", "kiem_ke_sprite.csv")

NGUON = "https://raw.githubusercontent.com/PMDCollab/SpriteCollab/master"

# Dong tac game can -> danh sach ten anim PMD theo thu tu uu tien (cai dau co thi dung).
# Thu tu khop trang thai ve cua client (xem bang "Trang thai ve" trong ke hoach).
DONG_TAC = [
    ("dung", ["Idle", "Walk"]),
    ("chay", ["Walk"]),
    ("danh_gan", ["Attack", "Strike", "QuickStrike", "Swing"]),
    ("danh_xa", ["Shoot", "Charge", "Attack"]),
    ("trung_don", ["Hurt", "Pain"]),
    ("guc_nga", ["Faint", "Sleep", "Hurt"]),
]


def dex_can_dung():
    kq = {}  # (dex, shiny) -> nguon
    with open(BANG_QUAI, encoding="utf8") as f:
        for r in csv.DictReader(f):
            if r["dex"]:
                kq[(int(r["dex"]), int(r["shiny"]))] = "bang_quai"
    # Bang thu (thu cuoi + pet) va bang NPC: cot dex/shiny giong bang_quai.csv
    # bang_npc_su_kien.csv (NPC lenh -50, theo idImg) va bang_pet_cu.csv (pet khong itemPet,
    # theo idImg PetTemplate): viec G7.B2/B3; dong la do vat (cot dex trong) thi bo qua.
    for ten, nguon in (("bang_thu.csv", "thu"), ("bang_npc.csv", "npc"),
                       ("bang_npc_su_kien.csv", "npc_su_kien"), ("bang_pet_cu.csv", "pet_cu")):
        p = os.path.join(GOC, "du_lieu", ten)
        if os.path.exists(p):
            with open(p, encoding="utf8") as f:
                for r in csv.DictReader(f):
                    if r.get("dex", "").strip().isdigit() and int(r["dex"]) > 0:
                        kq.setdefault((int(r["dex"]), int(r.get("shiny") or 0)), nguon)
    src = open(LOAI_JAVA, encoding="utf8").read()
    khoi = src[src.index("DU_LIEU = {"):src.index("};", src.index("DU_LIEU = {"))]
    for m in re.finditer(r"\{(\d+),", khoi):
        kq[(int(m.group(1)), 0)] = "nguoi_choi"
    return kq


def tai(url, dich):
    if os.path.exists(dich):
        return True
    req = urllib.request.Request(url, headers={"User-Agent": "htbb-pokemon-tool"})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            data = r.read()
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return False
        raise
    with open(dich, "wb") as f:
        f.write(data)
    return True


def thu_muc(dex, shiny):
    return os.path.join(RAW, "%04d%s" % (dex, "s" if shiny else ""))


def duong_nguon(dex, shiny):
    return "%04d/0000/0001" % dex if shiny else "%04d" % dex


def doc_anim(xml_path):
    """ten anim -> (rong, cao, so_khung, ten_file_goc). CopyOf duoc giai ve anim goc
    (file PNG mang ten anim GOC, vd Idle CopyOf Walk -> tai Walk-Anim.png)."""
    goc = ET.parse(xml_path).getroot()
    tho = {}
    for a in goc.iter("Anim"):
        ten = a.findtext("Name")
        copy = a.findtext("CopyOf")
        if copy:
            tho[ten] = copy
        else:
            tho[ten] = (int(a.findtext("FrameWidth")), int(a.findtext("FrameHeight")),
                        len(a.findall("Durations/Duration")))
    kq = {}
    for ten, v in tho.items():
        goc_ten = ten
        while isinstance(v, str):
            goc_ten = v
            v = tho.get(v)
        if v:
            kq[ten] = v + (goc_ten,)
    return kq


def main():
    chi_kiem = "--chi-kiem" in sys.argv
    can = dex_can_dung()
    os.makedirs(RAW, exist_ok=True)
    dong = []
    loi = 0
    for (dex, shiny), nguon in sorted(can.items()):
        tm = thu_muc(dex, shiny)
        os.makedirs(tm, exist_ok=True)
        base = "%s/sprite/%s" % (NGUON, duong_nguon(dex, shiny))
        xml = os.path.join(tm, "AnimData.xml")
        if not chi_kiem:
            if not tai(base + "/AnimData.xml", xml):
                dong.append([dex, shiny, nguon, "KHONG_CO_SPRITE"] + [""] * len(DONG_TAC) + [""])
                loi += 1
                print("LOI %04d%s: khong co AnimData.xml" % (dex, "s" if shiny else ""))
                continue
            # Ban shiny co credits.txt rieng; khong co thi lay cua ban thuong (ghi cong ca hai)
            if not tai(base + "/credits.txt", os.path.join(tm, "credits.txt")):
                tai("%s/sprite/%04d/credits.txt" % (NGUON, dex), os.path.join(tm, "credits.txt"))
            tai("%s/portrait/%s/Normal.png" % (NGUON, duong_nguon(dex, shiny)), os.path.join(tm, "portrait.png"))
        if not os.path.exists(xml):
            continue
        anims = doc_anim(xml)
        o = []
        thieu = []
        for ten_game, uu_tien in DONG_TAC:
            chon = next((a for a in uu_tien if a in anims), None)
            if chon is None:
                o.append("")
                thieu.append(ten_game)
                continue
            w, h, n, ten_file = anims[chon]
            o.append("%s %dx%d x%d" % (chon, w, h, n))
            if not chi_kiem:
                # Offsets: diem dau / tay / tam tung khung (phu kien trang bi, TinhNeoPoke.java)
                for hau_to in ("Anim", "Shadow", "Offsets"):
                    tai("%s/%s-%s.png" % (base, ten_file, hau_to), os.path.join(tm, "%s-%s.png" % (ten_file, hau_to)))
        if thieu:
            loi += 1
            print("LOI %04d%s: thieu %s" % (dex, "s" if shiny else "", ",".join(thieu)))
        dong.append([dex, shiny, nguon, "OK" if not thieu else "THIEU"] + o + [
            "co" if os.path.exists(os.path.join(tm, "portrait.png")) else "khong"])
        print("%04d%s %s" % (dex, "s" if shiny else "", dong[-1][3]))

    with open(KIEM_KE, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f)
        w.writerow(["dex", "shiny", "dung_cho", "trang_thai"] + [t for t, _ in DONG_TAC] + ["portrait"])
        w.writerows(dong)
    print("Da ghi %s: %d loai, %d loi" % (os.path.relpath(KIEM_KE, GOC), len(dong), loi))
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
