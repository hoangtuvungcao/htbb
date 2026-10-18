#!/usr/bin/env python3
"""Sinh sql/pokemon_vat_pham_lau_dai.sql: vat pham tieu hao/do giu MOI (kieu item type 28) cho GD2b.

Doc docs/pokemon/noi_dung_vat_pham.csv (do sinh_noi_dung.py cua doi noi dung sinh - KHONG sua o
day): moi dong trang_thai = can_code thanh mot data_item id 1201.. (dai cua GD2b; 1001..1199 la
cua doi noi dung) va mot dong data_vat_pham_lau_dai (id_item -> ma, nhom, hieu_ung, gioi_han)
cho real.VatPhamDungLauDai doc luc khoi dong. Them 2 bua bao ho that (thay cach "dat ngoc bao
hiem 0-4"). Chi thu vien chuan, tat dinh. Chay o thu muc HTBB/:  python3 -I tools/pokemon/sinh_vat_pham_lau_dai.py
"""
import csv
import os

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
VAO = os.path.join(GOC, "docs", "pokemon", "noi_dung_vat_pham.csv")
RA = os.path.join(GOC, "sql", "pokemon_vat_pham_lau_dai.sql")
ID_DAU = 1201
ID_CUOI = 1260
# Bua bao ho that: +7..+10 (10 luong khoa cu) va +11..+15 (25 luong khoa cu). Icon: Gummi he
# Thuong (doi noi dung, bang_icon.csv) - doi lai khi co icon rieng.
BUA = [
    (1258, "bua_bao_ho_1", "Bùa bảo hộ", "Giữ cấp khi nâng cấp +7..+10 thất bại", 7, 10, 14460),
    (1259, "bua_bao_ho_2", "Bùa bảo hộ cao cấp", "Giữ cấp khi nâng cấp +11..+15 thất bại", 11, 15, 14460),
]


def q(s):
    return "'" + str(s).replace("\\", "\\\\").replace("'", "''") + "'"


def main():
    dong = [d for d in csv.DictReader(open(VAO, encoding="utf-8")) if d["trang_thai"] == "can_code"]
    o = ["-- SINH RA boi tools/pokemon/sinh_vat_pham_lau_dai.py - KHONG SUA TAY. GD2b (docs/pokemon/KE_HOACH_LAU_DAI_GD2.md).\n",
         "-- Vat pham tieu hao / do giu moi = data_item type 28 id %d..%d + bang anh xa hieu ung.\n" % (ID_DAU, ID_CUOI),
         "-- Chi co tac dung khi sv.lau_dai=1 (real.VatPhamDungLauDai). SQL portable, chay lai nhieu lan nhu nhau.\n",
         "SET NAMES utf8;\n\n",
         "DELETE FROM `data_item` WHERE `id` >= %d AND `id` <= %d;\n" % (ID_DAU, ID_CUOI),
         "DROP TABLE IF EXISTS `data_vat_pham_lau_dai`;\n",
         "CREATE TABLE `data_vat_pham_lau_dai` (\n  `id_item` int NOT NULL,\n  `ma` varchar(64) NOT NULL,\n"
         "  `ten` varchar(255) NOT NULL,\n  `nhom` varchar(32) NOT NULL,\n  `hieu_ung` varchar(255) NOT NULL,\n"
         "  `gioi_han` varchar(32) NOT NULL DEFAULT '',\n  `cap_tu` int NOT NULL DEFAULT 1,\n  `cap_den` int NOT NULL DEFAULT 100,\n"
         "  PRIMARY KEY (`id_item`)\n) DEFAULT CHARSET=utf8;\n\n"]
    item = []
    map_ = []
    i = ID_DAU
    for d in dong:
        if i >= BUA[0][0]:
            raise SystemExit("het dai id")
        gia = int(d["gia"]) if d["tien"] == "xu" else 0
        item.append("(%d,%s,28,0,0,0,%d,0,0,0,0,0,0,0,0,0,0,0,%d,-1,%d,0,-1,0,-1,%s,-1,0)" % (
            i, q(d["ten"]), int(d["cap_tu"]), gia, int(d["icon"] or 0), q(d["ten"])))
        map_.append("(%d,%s,%s,%s,%s,%s,%d,%d)" % (i, q(d["ma"]), q(d["ten"]), q(d["nhom"]), q(d["hieu_ung"]),
                                                   q(d["gioi_han"]), int(d["cap_tu"]), int(d["cap_den"])))
        i += 1
    for (bid, ma, ten, mo_ta, tu, den, icon) in BUA:
        item.append("(%d,%s,28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,%d,0,-1,0,-1,%s,-1,0)" % (bid, q(ten), icon, q(ten)))
        map_.append("(%d,%s,%s,'bua',%s,'',1,100)" % (bid, q(ma), q(ten), q("bua:%d-%d" % (tu, den))))
    o.append("INSERT INTO `data_item` (`id`,`name`,`type`,`style`,`he`,`gender`,`level`,`durable`,`atb0`,`atb1`,`atb2`,"
             "`atb3`,`atb4`,`atb5`,`atb6`,`atb7`,`atb8`,`atb9`,`price`,`clazz`,`xstart`,`ystart`,`colortype`,`nloan`,"
             "`idUpLevel`,`namein`,`ideff`,`pricecu`) VALUES\n" + ",\n".join(item) + ";\n\n")
    o.append("INSERT INTO `data_vat_pham_lau_dai` VALUES\n" + ",\n".join(map_) + ";\n")
    open(RA, "w", encoding="utf-8").write("".join(o))
    print("da ghi %s: %d vat pham + %d bua" % (RA, len(dong), len(BUA)))


if __name__ == "__main__":
    main()
