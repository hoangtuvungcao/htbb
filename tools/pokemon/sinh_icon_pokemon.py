#!/usr/bin/env python3
"""
Icon vat pham Pokemon tu PMDCollab RawAsset/Item (16x16, cung co icon game cu).

Ra:
  map/icon_pokemon/<n>.png              (commit - server phuc vu o dai anh 14000+n,
                                         xem DataGame.sendByteImg + com.kpah.pokemon.IconPokemon)
  sql/pokemon_icon_vat_pham.sql         UPDATE data_item SET xstart = 14000+n (KHONG sua tay)

Vi sao dai rieng ma khong ghi de map/icon/<id>.png: icon cu dung chung giua nhieu vat
pham, va nIconItem = 866 bi test cu (KiemSkinLinhThu) khoa cung.

Nguon su that DUY NHAT cua danh sach icon la bang ICON ben duoi.
Chay (o thu muc KPAH/): python3 -I tools/pokemon/sinh_icon_pokemon.py
"""
import csv
import os
import sys
import urllib.request

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RA_ANH = os.path.join(GOC, "map", "icon_pokemon")
RA_SQL = os.path.join(GOC, "sql", "pokemon_icon_vat_pham.sql")
BANG_VP = os.path.join(GOC, "docs", "pokemon", "bang_vat_pham.csv")
NGUON = "https://raw.githubusercontent.com/PMDCollab/RawAsset/master/Item/%s.None.png"
ID_DAU = 14000

# (so thu tu n, file PMD, dung cho). "type:N" = moi trang bi type N trong bang_vat_pham.csv;
# "item:ID" = mot vat pham.
ICON = [
    (0, "Scarf_Red", "type:0"),          # Khan quang
    (1, "Belt_Black", "type:1"),         # Dai
    (2, "Band_Brown", "type:2"),         # Bang do
    (3, "Stone_DarkBrown", "type:3"),    # Than lua
    (4, "Seed_Green", "type:4"),         # Hat giong than
    (5, "Silk_White", "type:5"),         # Khan lua
    (6, "Bottle_LightBlue", "type:6"),   # Nuoc than
    (7, "Iron_Ball", "type:7"),          # Nam cham
    (8, "Ring_Target", "type:8"),        # Huy hieu
    (9, "Pearl", "type:9"),              # Vong co
    (10, "Scale_LightBlue", "type:10"),  # Long vu toc do
    (11, "Claw_Gray", "type:11"),        # Gang dam
    (12, "Slab_Gray", "type:12"),        # Phien da he
    (13, "Bow_Pink", "type:19"),         # Ruy bang
    (14, "Stone_Blue", "item:960"),      # Da Nuoc
    (15, "Stone_Yellow", "item:961"),    # Da Sam
    (16, "Stone_Red", "item:962"),       # Da Lua
    (17, "Stone_Orange", "item:963"),    # Da Mat Troi
    (18, "Stone_Black", "item:964"),     # Da Mat Trang
    (19, "Stone_Green", "item:965"),     # Da La
    (20, "Stone_LightBlue", "item:966"), # Da Bang
    (21, "Bow_Fuschia", "item:967"),     # Ruy Bang Tien
]


def main():
    os.makedirs(RA_ANH, exist_ok=True)
    loi = []
    for n, ten, _ in ICON:
        dich = os.path.join(RA_ANH, "%d.png" % n)
        if not os.path.exists(dich):
            req = urllib.request.Request(NGUON % ten, headers={"User-Agent": "kpah-pokemon-tool"})
            try:
                with urllib.request.urlopen(req, timeout=60) as r:
                    data = r.read()
            except Exception as e:
                loi.append("tai %s: %s" % (ten, e))
                continue
            open(dich, "wb").write(data)
        b = open(dich, "rb").read(24)
        if b[:8] != b"\x89PNG\r\n\x1a\n":
            loi.append("%s khong phai PNG" % dich)
            continue
        w, h = int.from_bytes(b[16:20], "big"), int.from_bytes(b[20:24], "big")
        if w > 24 or h > 24:
            loi.append("%s %dx%d qua lon cho o vat pham" % (ten, w, h))
    if [n for n, _, _ in ICON] != list(range(len(ICON))):
        loi.append("so thu tu n phai lien tuc 0..%d" % (len(ICON) - 1))

    theo_type = {}
    with open(BANG_VP, encoding="utf8") as f:
        for r in csv.DictReader(f):
            theo_type.setdefault(int(r["type"]), []).append(int(r["id"]))
    with open(RA_SQL, "w", encoding="utf8", newline="\n") as f:
        f.write("-- SINH TU tools/pokemon/sinh_icon_pokemon.py - KHONG SUA TAY.\n")
        f.write("-- Icon PMD o dai anh %d+ (map/icon_pokemon/). Chay SAU pokemon_vat_pham_eevee.sql.\n" % ID_DAU)
        for n, ten, dung in ICON:
            loai, so = dung.split(":")
            if loai == "type":
                ids = theo_type.get(int(so), [])
                if not ids:
                    loi.append("type %s khong co trong bang_vat_pham.csv" % so)
                    continue
                f.write("UPDATE data_item SET xstart=%d WHERE id IN (%s); -- %s\n"
                        % (ID_DAU + n, ",".join(map(str, ids)), ten))
            else:
                f.write("UPDATE data_item SET xstart=%d WHERE id=%s; -- %s\n" % (ID_DAU + n, so, ten))
    print("Da ghi %d icon -> %s, %s" % (len(ICON), os.path.relpath(RA_ANH, GOC), os.path.relpath(RA_SQL, GOC)))
    for l in loi:
        print("LOI:", l)
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
