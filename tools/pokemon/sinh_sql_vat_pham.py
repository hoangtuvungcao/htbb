#!/usr/bin/env python3
"""
I1: doi ten trang bi theo bang 6.4 cua docs/kien-truc.md.

Ra: du_lieu/sql_nguon/pokemon_doi_ten_vat_pham.sql (KHONG sua tay) + du_lieu/bang_vat_pham.csv.

- CHI doi `name` va dat `gender=0` (dung chung). KHONG doi atb/price/type/level/xstart
  -> can bang va icon giu nguyen (icon PMD: viec rieng, xem ke hoach).
- gender=0 vi nhan vat Pokemon khong chon gioi tinh (client gui 0 -> server luu gender=1
  "nam"), neu giu gender=2 thi do "nu" khong mac duoc (CharTrangBi.java:886).
- Ten moi = <ten loai> <bac theo cap>. Do cung loai cung cap (ban nam/nu cu) se trung ten -
  chap nhan, vi gio chung giong het nhau ve cong dung.

Chay (o thu muc HTBB/): python3 -I tools/pokemon/sinh_sql_vat_pham.py
"""
import csv
import os
import re
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SQL = os.path.join(GOC, "du_lieu", "sql_nguon", "gamedb7_clean.sql")
RA_SQL = os.path.join(GOC, "du_lieu", "sql_nguon", "pokemon_doi_ten_vat_pham.sql")
RA_CSV = os.path.join(GOC, "du_lieu", "bang_vat_pham.csv")

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

# Do thoi trang / cai trang / skin / than khi: KHONG doi thanh "<loai> <bac>" (mat ten va mo ta
# cong dung). Chi dat gender=0; ten doi o sinh_sql_chu.py (tu dien ChuPokemon + bang rieng) de
# moi o doi deu nam trong du_lieu/bang_chu.csv. Danh sach duyet tay: docs/thiet-ke-game.md.
GIU_TEN = {
    264, 265, 266, 267, 507, 508, 573, 574, 575, 576, 577, 578, 579, 580, 581, 582, 583,
    608, 609, 610, 616, 620, 621, 622, 623, 624, 625, 626, 627, 628, 629, 671, 672, 673, 674,
    723, 724, 725, 726, 736, 737, 740, 741, 742, 743, 744, 745, 746, 747, 748, 749, 750,
    754, 755, 756, 757, 788, 789, 791, 792, 795, 796, 797, 798, 799, 800, 801, 805, 806,
    833, 834, 837, 838, 839, 841, 842, 843, 844, 845, 846, 847, 872, 888, 889, 890, 891, 892,
    903, 904, 910, 950, 951, 952, 953, 954,
}
GHI_GIU_TEN = "(giữ tên, đổi chữ ở bang_chu.csv)"

# (cap toi thieu, bac). Lay bac co cap toi thieu lon nhat <= cap item.
BAC = [(0, "sơ cấp"), (10, "đồng"), (20, "bạc"), (30, "vàng"), (40, "bạch kim"),
       (50, "kim cương"), (60, "chuyên gia"), (70, "tinh anh"), (80, "huyền thoại")]


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
            moi = GHI_GIU_TEN if id_ in GIU_TEN else "%s %s" % (TEN_LOAI[typ], bac(cap))
            dong.append((id_, ten, typ, cap, gender, moi))
    with open(RA_SQL, "w", encoding="utf8", newline="\n") as f:
        f.write("-- SINH TU tools/pokemon/sinh_sql_vat_pham.py - KHONG SUA TAY.\n")
        f.write("-- Chi doi ten + gender=0 (dung chung). atb/price/type/icon giu nguyen.\n")
        for id_, ten, typ, cap, gender, moi in dong:
            if id_ in GIU_TEN:
                f.write("UPDATE data_item SET gender=0 WHERE id=%d; -- giu ten: %s\n"
                        % (id_, ten.replace("\r", "").replace("\n", " ")))
                continue
            f.write("UPDATE data_item SET name='%s', gender=0 WHERE id=%d; -- %s\n"
                    % (moi.replace("'", "''"), id_, ten.replace("\n", " ")))
    with open(RA_CSV, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["id", "ten_cu", "type", "cap", "gender_cu", "ten_moi"])
        w.writerows(dong)
    thieu = sorted(GIU_TEN - {d[0] for d in dong})
    if thieu:
        print("LOI: GIU_TEN co id khong thuoc type trang bi: %s" % thieu)
        return 1
    print("Doc %d item, doi ten %d (giu ten %d) -> %s" % (
        len(items), len(dong) - len(GIU_TEN), len(GIU_TEN), os.path.relpath(RA_SQL, GOC)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
