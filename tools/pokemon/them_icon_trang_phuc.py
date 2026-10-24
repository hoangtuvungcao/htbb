#!/usr/bin/env python3
"""
Icon PMD cho trang phuc / mat na / non / vu khi / skin dang nam trong o trang bi (24/10/2026, chu game: "tu kiem di").

Truoc do moi mon nay hien icon goc cua o (khan quang do, soi day, tui nau, hat giong, cuc da, no hong) vi ten khong bat
dau bang danh tu cua o. Phan lon da co icon dung trong map/icon_pokemon (cung mon ban o cua hang: Ao Lunala, Ao dai,
Non noel, Bong da...; skin he = chan dung cung loai) - khoi SQL chi tro sang. Phan con thieu them o day, CHI THEM VAO
CUOI dai 14000+ (sau o 502), cung cach bo sinh cu tools/luu_tru_sinh_du_lieu/pokemon/sinh_icon_pokemon.py: chan dung
PMDCollab SpriteCollab 40x40 thu nho 20x20 (vien toi), icon PMDCollab RawAsset 16x16, to mau "mau:" cua bo sinh do.
Khong ve hinh moi.

Ra:
  server/map/icon_pokemon/<503..>.png      (chi ghi khi khac diem anh)
  du_lieu/bang_icon.csv                    dong n,id_anh,nguon,dung_cho (thay dong cu cung n)
  server/map/icon_pokemon/GHI_CONG.txt     nguoi ve / nguon tung icon them o day
Khoi SQL tro data_item sang icon: sql/htbb.sql phan B "-- >>> doi_icon_trang_phuc.sql" (viet tay, test KiemIconTrangBi).

Chay (o goc repo):  python3 -I tools/pokemon/them_icon_trang_phuc.py [--tai <thu muc tai ve>]
Tai ve mac dinh: tools/pokemon/pmd_raw_icon/ (khong commit, xem .gitignore).
"""
import csv
import importlib.util
import io
import os
import sys

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
BO_SINH = os.path.join(GOC, "tools", "luu_tru_sinh_du_lieu", "pokemon", "sinh_icon_pokemon.py")
RA_ANH = os.path.join(GOC, "server", "map", "icon_pokemon")
RA_CSV = os.path.join(GOC, "du_lieu", "bang_icon.csv")
RA_GHI_CONG = os.path.join(RA_ANH, "GHI_CONG.txt")
DAU = 503
SPRITECOLLAB = "https://github.com/PMDCollab/SpriteCollab portrait/%s/Normal.png"
RAWASSET = "https://github.com/PMDCollab/RawAsset Item/%s.None.png"

# (nguon, dung cho (data_item id), nguoi ve chan dung Normal theo portrait/<dex>/credits.txt, ghi chu)
MOI = [
    ("dex:0571", "item:723", "Deeshura", "Ao Zoroark"),
    ("dex:0282", "item:724", "CHUNSOFT", "Ao Gardevoir"),
    ("dex:0359", "item:725", "Emmuffin", "Ao Absol"),
    ("dex:0741", "item:726", "Emmuffin, Top_Kec", "Ao Oricorio"),
    ("dex:0350", "item:797", "CHUNSOFT", "Ao Milotic"),
    ("dex:0312", "item:798", "C_Pariah", "Ao Minun"),
    ("dex:0257", "item:799", "Noo", "Ao Blaziken"),
    ("dex:0311", "item:800", "C_Pariah", "Ao Plusle"),
    ("dex:0145", "item:801", "CHUNSOFT", "Ao Zapdos"),
    ("dex:0243", "item:805,806", "CHUNSOFT", "Ao Raikou nam / nu"),
    ("dex:0431", "item:833", "JFain", "Ao Glameow"),
    ("dex:0053", "item:834", "baronessfaron", "Ao Persian"),
    ("Scarf_2_LightBlue", "item:624-629", "", "Hoc tro cap 1..3 (khan quang dong phuc)"),
    ("dex:0093", "item:746", "PhillipsYoung", "Mat na Haunter"),
    ("dex:0229", "item:747", "CHUNSOFT", "Mat na Houndoom"),
    ("dex:0562", "item:748", "Emmuffin", "Mat na Yamask"),
    ("dex:0094", "item:749", "CHUNSOFT", "Mat na Gengar"),
    ("dex:0143", "item:872", "CHUNSOFT", "Mat na Snorlax"),
    ("Mask_Gold+xam+sang", "item:837", "", "Mat na tho (mat na vang to xam bac)"),
    ("Mask_Gold", "item:838", "", "Mat na cao"),
    ("dex:0123", "item:892", "Noo", "Luoi liem Scyther"),
    ("Wand_White", "item:796", "", "Gay Anh Trang"),
    ("Cape_Gray+mau:50B050", "item:839", "", "Giap rong (ao choang to xanh rong, cung kieu Ao Emboar / Swampert)"),
    ("dex:0105", "item:891", "Emmuffin", "Gay xuong Marowak"),
]


def nap_bo_sinh(tai):
    spec = importlib.util.spec_from_file_location("sinh_icon_pokemon", BO_SINH)
    g = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(g)
    g.TAI = tai
    return g


def main():
    tai = os.path.join(GOC, "tools", "pokemon", "pmd_raw_icon")
    if "--tai" in sys.argv:
        tai = os.path.abspath(sys.argv[sys.argv.index("--tai") + 1])
    g = nap_bo_sinh(tai)
    loi = []
    if not os.path.isfile(os.path.join(RA_ANH, "%d.png" % (DAU - 1))):
        loi.append("thieu o %d.png - dai icon_pokemon phai lien tuc" % (DAU - 1))
    dong_csv = {}
    for k, (nguon, dung, _, ghi) in enumerate(MOI):
        n = DAU + k
        a = g.anh_nguon(nguon, loi)
        if a is None:
            continue
        h, w = len(a), len(a[0])
        if w > 24 or h > 24:
            loi.append("%s %dx%d qua lon" % (nguon, w, h))
        dich = os.path.join(RA_ANH, "%d.png" % n)
        try:
            giong = g.doc_png(open(dich, "rb").read()) == [[tuple(p) for p in r] for r in a]
        except Exception:
            giong = False
        if not giong:
            open(dich, "wb").write(g.ghi_png(a))
        b = io.StringIO()
        csv.writer(b, lineterminator="").writerow([n, g.ID_DAU + n, nguon, dung])
        dong_csv[n] = b.getvalue()
    # bang_icon.csv: giu dong < DAU, thay / them dong moi
    cu = open(RA_CSV, encoding="utf-8").read().splitlines()
    giu = [cu[0]] + [d for d in cu[1:] if int(d.split(",", 1)[0]) < DAU]
    with open(RA_CSV, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(giu + [dong_csv[n] for n in sorted(dong_csv)]) + "\n")
    with open(RA_GHI_CONG, "w", encoding="utf-8", newline="\n") as f:
        f.write("Icon map/icon_pokemon/<n>.png (id anh 14000+n) them boi tools/pokemon/them_icon_trang_phuc.py.\n")
        f.write("O 0..502 sinh boi tools/luu_tru_sinh_du_lieu/pokemon/sinh_icon_pokemon.py tu cung hai nguon duoi day.\n\n")
        f.write("Chan dung: PMDCollab SpriteCollab (CC BY-NC 4.0, https://creativecommons.org/licenses/by-nc/4.0/),\n")
        f.write("  thu nho 40x40 -> 20x20 va them vien toi. Nguoi ve theo portrait/<dex>/credits.txt\n")
        f.write("  (CHUNSOFT = trich tu Pokemon Mystery Dungeon, (c) Nintendo / Chunsoft).\n")
        f.write("Icon vat pham: PMDCollab RawAsset Item (trich tu Pokemon Mystery Dungeon, (c) Nintendo / Chunsoft;\n")
        f.write("  khong co LICENSE, chi dung phi thuong mai). \"bien doi\" (xam, sang, mau:RRGGBB) = to lai mau hinh goc.\n\n")
        for k, (nguon, dung, nguoi, ghi) in enumerate(MOI):
            goc = nguon.split("+")[0]
            src = SPRITECOLLAB % goc[4:] if goc.startswith("dex:") else RAWASSET % goc
            f.write("%d.png\t%s\t%s\t%s%s\n" % (DAU + k, ghi, src, ("nguoi ve: " + nguoi + "\t") if nguoi else "",
                                                  "bien doi: " + "+".join(nguon.split("+")[1:]) if "+" in nguon else ""))
    print("Da ghi %d icon %d..%d -> map/icon_pokemon/, bang_icon.csv, GHI_CONG.txt" % (len(dong_csv), DAU, DAU + len(MOI) - 1))
    for l in loi:
        print("LOI:", l)
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
