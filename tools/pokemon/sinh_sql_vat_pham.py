#!/usr/bin/env python3
"""
I1: doi ten trang bi theo bang 6.4 cua docs/pokemon/KE_HOACH_CHUYEN_DOI.md.

Ra: sql/pokemon_doi_ten_vat_pham.sql (KHONG sua tay) + docs/pokemon/bang_vat_pham.csv.

- CHI doi `name` va dat `gender=0` (dung chung). KHONG doi atb/price/type/level/xstart
  -> can bang va icon giu nguyen (icon PMD: viec rieng, xem ke hoach).
- gender=0 vi nhan vat Pokemon khong chon gioi tinh (client gui 0 -> server luu gender=1
  "nam"), neu giu gender=2 thi do "nu" khong mac duoc (CharTrangBi.java:886).
- Ten moi = <ten loai> <bac theo cap>. Do cung loai cung cap (ban nam/nu cu) se trung ten -
  chap nhan, vi gio chung giong het nhau ve cong dung.

Chay (o thu muc KPAH/): python3 -I tools/pokemon/sinh_sql_vat_pham.py
"""
import csv
import os
import re
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SQL = os.path.join(GOC, "sql", "gamedb7_clean.sql")
RA_SQL = os.path.join(GOC, "sql", "pokemon_doi_ten_vat_pham.sql")
RA_CSV = os.path.join(GOC, "docs", "pokemon", "bang_vat_pham.csv")

# type -> ten loai moi (bang 6.4). Type khong co o day: giu nguyen.
TEN_LOAI = {
    0: "Khăn quàng",
    1: "Đai",
    2: "Băng đô",
    3: "Than lửa",
    4: "Hạt giống thần",
    5: "Khăn lụa",
    6: "Nước thần",
    7: "Nam châm",
    8: "Huy hiệu",
    9: "Vòng cổ",
    10: "Lông vũ tốc độ",
    11: "Găng đấm",
    12: "Phiến đá hệ",
    19: "Ruy băng",
}

# (cap toi thieu, bac). Lay bac co cap toi thieu lon nhat <= cap item.
BAC = [(0, "sơ cấp"), (10, "đồng"), (20, "bạc"), (30, "vàng"), (40, "bạch kim"),
       (50, "kim cương"), (60, "chuyên gia"), (70, "đại sư"), (80, "huyền thoại")]


def bac(cap):
    kq = BAC[0][1]
    for c, t in BAC:
        if cap >= c:
            kq = t
    return kq


def doc_item():
    s = open(SQL, encoding="utf8").read()
    i = s.index("INSERT INTO `data_item`")
    j = s.index(";\n", i)
    # (id,'ten',type,style,he,gender,level,...
    return [(int(a), b.replace("\\'", "'"), int(c), int(d), int(e))
            for a, b, c, d, e in re.findall(r"\((\d+),'((?:[^'\\]|\\.)*)',(\d+),\d+,-?\d+,(\d+),(-?\d+),", s[i:j])]


def main():
    items = doc_item()
    if len(items) < 900:
        print("LOI: chi doc duoc %d item tu data_item (mong doi ~915) - dinh dang SQL da doi?" % len(items))
        return 1
    dong = []
    for id_, ten, typ, gender, cap in items:
        if typ in TEN_LOAI:
            dong.append((id_, ten, typ, cap, gender, "%s %s" % (TEN_LOAI[typ], bac(cap))))
    with open(RA_SQL, "w", encoding="utf8", newline="\n") as f:
        f.write("-- SINH TU tools/pokemon/sinh_sql_vat_pham.py - KHONG SUA TAY.\n")
        f.write("-- Chi doi ten + gender=0 (dung chung). atb/price/type/icon giu nguyen.\n")
        for id_, ten, typ, cap, gender, moi in dong:
            f.write("UPDATE data_item SET name='%s', gender=0 WHERE id=%d; -- %s\n"
                    % (moi.replace("'", "''"), id_, ten.replace("\n", " ")))
    with open(RA_CSV, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["id", "ten_cu", "type", "cap", "gender_cu", "ten_moi"])
        w.writerows(dong)
    print("Doc %d item, doi ten %d -> %s" % (len(items), len(dong), os.path.relpath(RA_SQL, GOC)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
