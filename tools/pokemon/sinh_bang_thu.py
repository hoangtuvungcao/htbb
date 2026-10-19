#!/usr/bin/env python3
"""
T1/T3: bang thu cuoi (linh thu, src/data/Animal.java) + pet (item type 25/26/27) -> Pokemon.

Ra:
  du_lieu/bang_thu.csv               (KHONG sua tay - sua bang ben duoi roi chay lai)
  sql/pokemon_doi_ten_thu.sql             doi ten item pet/trung/ky lan (chi `name`)
  sql/pokemon_thu_pokemon.sql             bang data_thu_pokemon: MOI anh xa thu cuoi / pet / skin /
                                          hoa hinh / pet cu (bang_pet_cu.csv) -> dex Pokemon (P2)

Bao loi (ma thoat 1) neu loai chon khong co sprite PMD (tracker.json),
hoac ANIMAL khong du 21 loai (Animal.java co 21 hang so 0..20).

Chay (o thu muc HTBB/): python3 -I tools/pokemon/sinh_bang_thu.py
Sau do chay tai_sprite_pmd.py de tai them sprite (no doc ca bang_thu.csv).
"""
import csv
import json
import os
import re
import sys
import urllib.request

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RA = os.path.join(GOC, "du_lieu", "bang_thu.csv")
RA_SQL = os.path.join(GOC, "sql", "pokemon_doi_ten_thu.sql")
RA_SQL_ANH_XA = os.path.join(GOC, "sql", "pokemon_thu_pokemon.sql")
PET_CU = os.path.join(GOC, "du_lieu", "bang_pet_cu.csv")
ANIMAL_JAVA = os.path.join(GOC, "server", "src", "data", "Animal.java")
BO_NHO = os.path.join(GOC, "tools", "pokemon", ".cache")
TRACKER = "https://raw.githubusercontent.com/PMDCollab/SpriteCollab/master/tracker.json"

# Hang so trong Animal.java -> (dex, shiny, ten Pokemon)
ANIMAL = {
    "BACH_MA": (78, 0, "Rapidash"),
    "HO": (59, 0, "Arcanine"),
    "TRAU": (128, 0, "Tauros"),
    "SOI": (262, 0, "Mightyena"),
    "HAC": (18, 0, "Pidgeot"),
    "PHUONG_HOANG": (146, 0, "Moltres"),
    "HAC_THO": (428, 1, "Lopunny Shiny"),
    "TUAN_LOC": (234, 0, "Stantler"),
    "PHUONG_HOANG_BANG": (144, 0, "Articuno"),
    "PORO": (831, 0, "Wooloo"),
    "LAN": (245, 0, "Suicune"),
    "HEO": (221, 0, "Piloswine"),
    "NGUA_XUONG": (897, 0, "Spectrier"),
    "RONG_LON": (149, 0, "Dragonite"),
    "PHUONG_HOANG_7MAU": (250, 0, "Ho-Oh"),
    "PHUONG_HOANG_MOC": (357, 0, "Tropius"),
    "PHUONG_HOANG_KIM": (227, 0, "Skarmory"),
    "PHUONG_HOANG_THO": (330, 0, "Flygon"),
    "HEO_BANG": (473, 0, "Mamoswine"),
    "SU_TU": (244, 0, "Entei"),
    "SU_TU_BANG": (896, 0, "Glastrier"),
}

# id item (data_item) -> (dex, shiny, ten moi). Pet 5 cap = tien hoa theo cap pet.
PET = {
    681: (77, 0, "Ponyta lửa"), 682: (78, 0, "Rapidash lửa"), 683: (78, 1, "Rapidash lửa Shiny"),
    684: (163, 0, "Trứng Hoothoot"), 685: (147, 0, "Trứng Dratini"), 686: (173, 0, "Trứng Cleffa"),
    687: (41, 0, "Trứng Zubat"), 688: (627, 0, "Trứng Rufflet"),
    689: (163, 0, "Hoothoot cấp 1"), 690: (163, 0, "Hoothoot cấp 2"), 691: (164, 0, "Noctowl cấp 3"),
    692: (164, 0, "Noctowl cấp 4"), 693: (164, 1, "Noctowl Shiny cấp 5"),
    694: (147, 0, "Dratini cấp 1"), 695: (147, 0, "Dratini cấp 2"), 696: (148, 0, "Dragonair cấp 3"),
    697: (148, 0, "Dragonair cấp 4"), 698: (149, 0, "Dragonite cấp 5"),
    699: (173, 0, "Cleffa cấp 1"), 700: (35, 0, "Clefairy cấp 2"), 701: (35, 0, "Clefairy cấp 3"),
    702: (36, 0, "Clefable cấp 4"), 703: (36, 1, "Clefable Shiny cấp 5"),
    704: (41, 0, "Zubat cấp 1"), 705: (41, 0, "Zubat cấp 2"), 706: (42, 0, "Golbat cấp 3"),
    707: (42, 0, "Golbat cấp 4"), 708: (169, 0, "Crobat cấp 5"),
    709: (627, 0, "Rufflet cấp 1"), 710: (627, 0, "Rufflet cấp 2"), 711: (628, 0, "Braviary cấp 3"),
    712: (628, 0, "Braviary cấp 4"), 713: (628, 1, "Braviary Shiny cấp 5"),
    727: (417, 0, "Pachirisu"), 728: (225, 0, "Delibird"), 790: (100, 1, "Voltorb vàng"),
    802: (403, 1, "Shinx vàng"), 803: (667, 0, "Litleo"), 804: (58, 0, "Growlithe"),
    832: (585, 0, "Deerling"), 873: (399, 0, "Bidoof"),
}

# Skin linh thu (real.SkinThuCuoi 0..5, ten TEN[]) -> Pokemon thu cuoi ve khi skin dang hien (P2).
SKIN = {
    0: (879, 0, "Copperajah"), 1: (675, 0, "Pangoro"), 2: (716, 0, "Xerneas"),
    3: (323, 0, "Camerupt"), 4: (745, 0, "Lycanroc"), 5: (483, 0, "Dialga"),
}

# Hoa hinh pet (data.Pet capHoaHinh 1..5, ten cu "Lan cap n") -> Pokemon (P2). Cap 1-3 dung chung.
HOA_HINH_PET = {
    1: (58, 0, "Growlithe"), 2: (58, 0, "Growlithe"), 3: (58, 0, "Growlithe"),
    4: (59, 0, "Arcanine"), 5: (244, 0, "Entei"),
}

# Thu cuoi dang hoa hinh (data.Animal.isHoaHinh == 1, anh cu Ky Lan 8965/8966) -> Pokemon (P2).
HOA_HINH_THU = {0: (244, 0, "Entei")}

# Loai chi dung lam thu cuoi (chi ve dung / chay): PMDCollab chi co Idle + Walk, chap nhan sprite
# chua du (tai_sprite_pmd.py / DongGoiSprite dung Walk/Idle thay cho dong tac thieu).
CHI_CAN_DUNG_CHAY = {879}


def tracker():
    os.makedirs(BO_NHO, exist_ok=True)
    p = os.path.join(BO_NHO, "tracker.json")
    if not os.path.exists(p):
        req = urllib.request.Request(TRACKER, headers={"User-Agent": "htbb-pokemon-tool"})
        with urllib.request.urlopen(req, timeout=60) as r:
            open(p, "wb").write(r.read())
    return json.load(open(p, encoding="utf8"))


def hang_so_animal():
    s = open(ANIMAL_JAVA, encoding="utf8").read()
    return {m.group(1): int(m.group(2))
            for m in re.finditer(r"public (?:final )?static (?:final )?byte ([A-Z_0-9]+) = (\d+);", s)}


def main():
    t = tracker()
    hs = hang_so_animal()
    loi = []
    for ten in hs:
        if ten not in ANIMAL:
            loi.append("Animal.%s (=%d) chua anh xa" % (ten, hs[ten]))
    dong = []
    for ten, (dex, shiny, ten_pk) in ANIMAL.items():
        if ten not in hs:
            loi.append("ANIMAL co %s nhung Animal.java khong co" % ten)
            continue
        dong.append(["thu_cuoi", hs[ten], ten, dex, shiny, ten_pk])
    for id_, (dex, shiny, ten_pk) in sorted(PET.items()):
        dong.append(["pet_item", id_, "", dex, shiny, ten_pk])
    for loai, bang in (("skin", SKIN), ("hoa_hinh_pet", HOA_HINH_PET), ("hoa_hinh_thu", HOA_HINH_THU)):
        for id_, (dex, shiny, ten_pk) in sorted(bang.items()):
            dong.append([loai, id_, "", dex, shiny, ten_pk])
    for d in dong:
        if d[0] == "skin" and d[3] in CHI_CAN_DUNG_CHAY:
            continue
        if t.get("%04d" % d[3], {}).get("sprite_complete", 0) < 1:
            loi.append("dex %d (%s) KHONG co sprite PMD" % (d[3], d[5]))
    dong.sort(key=lambda d: (d[0], d[1]))
    with open(RA, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["loai", "id", "hang_so_cu", "dex", "shiny", "ten_moi"])
        w.writerows(dong)
    with open(RA_SQL, "w", encoding="utf8", newline="\n") as f:
        f.write("-- SINH TU tools/pokemon/sinh_bang_thu.py - KHONG SUA TAY. Chi doi ten item pet/trung.\n")
        for id_, (dex, shiny, ten_pk) in sorted(PET.items()):
            f.write("UPDATE data_item SET name='%s' WHERE id=%d;\n" % (ten_pk.replace("'", "''"), id_))
    ghi_sql_anh_xa(dong)
    print("Da ghi %s (%d dong), %s, %s" % (os.path.relpath(RA, GOC), len(dong), os.path.relpath(RA_SQL, GOC),
                                          os.path.relpath(RA_SQL_ANH_XA, GOC)))
    for l in loi:
        print("LOI:", l)
    return 1 if loi else 0


def ghi_sql_anh_xa(dong):
    """sql/pokemon_thu_pokemon.sql: bang anh xa (idempotent: CREATE IF NOT EXISTS + REPLACE INTO).

    Server van doc mang Java (ThuPokemon / PetPokemon, test KiemThuPetSql doi chieu bang nay voi Java
    va CSV); bang SQL la nguon tra cuu cho web / quan tri va la ghi chep chinh thuc cua anh xa.
    """
    hang = [(d[0], int(d[1]), int(d[3]), int(d[4]), d[5]) for d in dong]
    with open(PET_CU, encoding="utf8") as f:
        for r in csv.DictReader(f):
            hang.append(("pet_cu", int(r["id_img"]), int(r["dex"]), int(r["shiny"] or 0), r["ten_moi"]))
    hang.sort(key=lambda h: (h[0], h[1]))
    with open(RA_SQL_ANH_XA, "w", encoding="utf8", newline="\n") as f:
        f.write("-- SINH TU tools/pokemon/sinh_bang_thu.py - KHONG SUA TAY (sua bang trong tool roi chay lai).\n")
        f.write("-- P2: anh xa thu cuoi / pet / skin linh thu / hoa hinh -> Pokemon (dex PMDCollab, shiny).\n")
        f.write("-- loai: thu_cuoi (id = data.Animal.idImg), pet_item (id = data_item.id), pet_cu (id = PetTemplate.idImg),\n")
        f.write("--       skin (id = real.SkinThuCuoi 0..5), hoa_hinh_pet (id = cap 1..5), hoa_hinh_thu (id 0).\n")
        f.write("-- Chay lai bao nhieu lan cung duoc (CREATE IF NOT EXISTS + REPLACE INTO, khong INSERT...SELECT).\n")
        f.write("-- SO_DONG: %d (test KiemThuPetSql dem lai)\n" % len(hang))
        f.write("CREATE TABLE IF NOT EXISTS `data_thu_pokemon` (\n"
                "  `loai` varchar(16) NOT NULL,\n"
                "  `id` int(11) NOT NULL,\n"
                "  `dex` smallint(6) NOT NULL,\n"
                "  `shiny` tinyint(1) NOT NULL DEFAULT 0,\n"
                "  `ten` varchar(64) NOT NULL,\n"
                "  PRIMARY KEY (`loai`, `id`)\n"
                ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;\n")
        for loai, id_, dex, shiny, ten in hang:
            f.write("REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('%s', %d, %d, %d, '%s');\n"
                    % (loai, id_, dex, shiny, ten.replace("'", "''")))


if __name__ == "__main__":
    sys.exit(main())
