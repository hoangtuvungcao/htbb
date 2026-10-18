#!/usr/bin/env python3
"""Sinh sql/pokemon_vat_pham_lau_dai.sql: vat pham tieu hao/do giu MOI (kieu item type 28) cho GD2b.

Doc du_lieu/noi_dung_vat_pham.csv (do sinh_noi_dung.py cua doi noi dung sinh - KHONG sua o
day): moi dong trang_thai = can_code thanh mot data_item id 1201.. (dai cua GD2b; 1001..1199 la
cua doi noi dung) va mot dong data_vat_pham_lau_dai (id_item -> ma, nhom, hieu_ung, gioi_han)
cho real.VatPhamDungLauDai doc luc khoi dong. Them 2 bua bao ho that (thay cach "dat ngoc bao
hiem 0-4"). Chi thu vien chuan, tat dinh. Chay o thu muc HTBB/:  python3 -I tools/pokemon/sinh_vat_pham_lau_dai.py

Them (luat "khong item mo coi", tools/pokemon/kiem_item_mo_coi.py):
  - data_vat_pham_lau_dai cho 2 hop dot kich 1050/1051 (nhom 'hop', mo theo bang roi nguon mo_hop);
  - data_cua_hang_lau_dai: "Cua hang & doi vat pham" o So tay (real.CuaHangLauDai). Moi dong = chi_phi
    (xu:N / luong_khoa:N / item:IDxN / thuoc:IDxN / ngoc:IDxN, ngan ';') -> thuong (cu phap
    applyItemString, CHI vat pham - khong bao gio ra tien te). Nguon cho tieu hao 1201.., trang bi
    nam 2 (Bronzong / Machamp theo noi_dung_cua_hang.csv / noi_dung_che_tao.csv), va CONG DUNG cho
    huy chuong / tin vat su kien (1039..1061) + vat pham cu khong con dung (keo EXP 10..12, bot 246..248).
"""
import csv
import os
import re

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
VAO = os.path.join(GOC, "du_lieu", "noi_dung_vat_pham.csv")
RA = os.path.join(GOC, "sql", "pokemon_vat_pham_lau_dai.sql")
ID_DAU = 1201
ID_CUOI = 1260
# Bua bao ho that: +7..+10 (10 luong khoa cu) va +11..+15 (25 luong khoa cu). Icon: Gummi he
# Thuong (doi noi dung, bang_icon.csv) - doi lai khi co icon rieng.
BUA = [
    (1258, "bua_bao_ho_1", "Bùa bảo hộ", "Giữ cấp khi nâng cấp +7..+10 thất bại", 7, 10, 14460),
    (1259, "bua_bao_ho_2", "Bùa bảo hộ cao cấp", "Giữ cấp khi nâng cấp +11..+15 thất bại", 11, 15, 14460),
]

# Vat pham TANG TOC luyen cap (18/10/2026, com.htbb.pokemon.TangToc / real.TangTocGame; docs/thiet-ke-game.md muc
# 1.8): ban CAO CAP (ban bang luong o Cua hang & Nap, nap_cua_hang trao tang_toc) va ban KHOA (kiem bang choi:
# nhiem vu tuan, lich dang nhap, ve mua, Kecleon bang luong khoa). Ca hai khoa (khong giao dich), cung tran diem.
# id 1250..1257 (sat dai bua 1258, xa dai noi dung 1201.. dang tang dan). Icon tam: bo icon cu (bang_icon.csv).
# (id, ma, ten, nhom, hieu_ung, icon)
TANG_TOC = [
    (1250, "ve_exp_15", "Vé EXP x1,5 (30 phút)", "ve_exp:150,phut:30", 14189),
    (1251, "ve_exp_15_khoa", "Vé EXP x1,5 (30 phút) - khoá", "ve_exp:150,phut:30", 14189),
    (1252, "ve_exp_2", "Vé EXP x2 (30 phút)", "ve_exp:200,phut:30", 14190),
    (1253, "ve_exp_2_khoa", "Vé EXP x2 (30 phút) - khoá", "ve_exp:200,phut:30", 14190),
    (1254, "bua_the_luc", "Bùa thể lực (+60 phút)", "nang_luong_phut:60", 14274),
    (1255, "bua_the_luc_khoa", "Bùa thể lực (+60 phút) - khoá", "nang_luong_phut:60", 14274),
    (1256, "uy_thac", "Phiếu ủy thác luyện tập (1 giờ)", "uy_thac_gio:1", 14293),
    (1257, "uy_thac_khoa", "Phiếu ủy thác luyện tập (1 giờ) - khoá", "uy_thac_gio:1", 14293),
]
# Kecleon (luong khoa): ban khoa, gioi han tuan (lk la tien choi - ho hut luong khoa). (id item, gia lk, gh tuan)
TANG_TOC_KECLEON = [(1251, 15, 2), (1257, 10, 2)]


DOC = os.path.join(GOC, "du_lieu")
# Hop dot kich: mo -> bang roi nguon 'mo_hop' (khoa_nguon = item:<id>) cua data_noi_dung_roi.
HOP = [(1050, "hop_tham_gia", "Hộp tham gia đột kích"), (1051, "hop_vang", "Hộp vàng đột kích")]
ID_CUA_HANG_TUAN = 10000  # tob_mua_tuan.id_shop = 10000 + id dong (khong trung data_shop 0..255)

# Doi tin vat / huy chuong -> vat pham (khong tien te). (nhom, chi_phi, thuong, nhan, cap, gioi_han_tuan)
DOI = [
    # Huy chuong Gym (Gym danh lai hang tuan, 3/gym/tuan): do giu (thiet ke: "boss Gym")
    ("Đổi Huy chương Gym", "item:1039x10", "ITEM:1224:1", "Thức ăn thừa", 30, 0),
    ("Đổi Huy chương Gym", "item:1039x10", "ITEM:1225:1", "Chuông vỏ sò", 40, 0),
    ("Đổi Huy chương Gym", "item:1039x15", "ITEM:1227:1", "Băng đô tập trung", 50, 0),
    ("Đổi Huy chương Gym", "item:1039x10", "ITEM:1228:1", "Kính ngắm", 30, 0),
    ("Đổi Huy chương Gym", "item:1039x5", "ITEM:1230:1", "Đá vĩnh cửu", 1, 0),
    ("Đổi Huy chương Gym", "item:1039x3", "ITEM:1206:2", "2 Thuốc chữa toàn diện", 30, 0),
    # Huy chuong mua giai (PvP)
    ("Đổi Huy chương mùa giải", "item:1055x10", "ITEM:1226:1", "Móng vuốt nhanh", 40, 0),
    ("Đổi Huy chương mùa giải", "item:1055x10", "ITEM:1229:1", "Đai lực sĩ", 30, 0),
    ("Đổi Huy chương mùa giải", "item:1055x2", "ITEM:1210:2", "2 Tăng chí mạng", 10, 0),
    # Tin vat huyen thoai (dot kich): nguyen lieu nang cap +11..+15
    ] + [("Đổi tín vật huyền thoại", "item:%dx3" % i, "ITEM:1038:1", "Mảnh tinh hoa", 1, 0) for i in range(1040, 1050)] + [
    ("Đổi tín vật huyền thoại", "item:%dx2" % i, "ITEM:1037:1", "Vảy rồng", 1, 0) for i in range(1040, 1050)] + [
    # Ngay cong dong: da tien hoa (thiet ke: "Ngày cộng đồng Eevee")
    ] + [("Đổi Huy hiệu ngày cộng đồng", "item:1053x10", "ITEM:%d:1" % i, t, 25, 0) for i, t in (
        (960, "Đá Nước"), (961, "Đá Sấm"), (962, "Đá Lửa"), (963, "Đá Mặt Trời"), (964, "Đá Mặt Trăng"),
        (965, "Đá Lá"), (966, "Đá Băng"), (967, "Ruy Băng Tiên"))] + [
    # Tem nghien cuu (Pokedex): Keo hiem (2/tuan khi dung), Trung may man
    ("Đổi Tem nghiên cứu", "item:1054x5", "ITEM:1214:1", "Kẹo hiếm", 1, 0),
    ("Đổi Tem nghiên cứu", "item:1054x20", "ITEM:1231:1", "Trứng may mắn", 20, 0),
    # Tinh the he / Co vung / qua le / phieu: tieu hao (gia tri <= gia tri tin vat)
    ("Đổi vật phẩm sự kiện", "item:1052x5", "ITEM:1206:1", "Thuốc chữa toàn diện", 30, 0),
    ("Đổi vật phẩm sự kiện", "item:1052x5", "ITEM:1208:1,ITEM:1209:1", "X Tốc độ + X Chính xác", 10, 0),
    ("Đổi vật phẩm sự kiện", "item:1056x5", "ITEM:1207:1", "Thuốc hồi sinh tối đa", 40, 0),
    ("Đổi vật phẩm sự kiện", "item:1057x1", "ITEM:1215:3,ITEM:1218:2", "Quà 30/4: 3 Quả Oran, 2 Quả Cheri", 1, 0),
    ("Đổi vật phẩm sự kiện", "item:1058x1", "ITEM:1216:2,ITEM:1223:2", "Quà 2/9: 2 Quả Sitrus, 2 Quả Leppa", 1, 0),
    ("Đổi vật phẩm sự kiện", "item:1059x1", "ITEM:1217:2,ITEM:1222:2", "Quà 20/10: 2 Quả Lum, 2 Quả Aspear", 1, 0),
    ("Đổi vật phẩm sự kiện", "item:1060x1", "ITEM:1219:2,ITEM:1220:2,ITEM:1221:2", "Quà 20/11: 6 quả mọng", 1, 0),
    ("Đổi vật phẩm sự kiện", "item:1061x1", "ITEM:1211:2", "2 Thuốc xua quái", 5, 0),
    # Vat pham cu het cong dung khi bat dai han
    ("Đổi vật phẩm cũ", "thuoc:10x5", "ITEM:1214:1", "5 Kẹo kinh nghiệm S lấy 1 Kẹo hiếm", 1, 0),
    ("Đổi vật phẩm cũ", "thuoc:11x2", "ITEM:1214:1", "2 Kẹo kinh nghiệm M lấy 1 Kẹo hiếm", 1, 0),
    ("Đổi vật phẩm cũ", "thuoc:12x1", "ITEM:1214:1", "1 Kẹo kinh nghiệm L lấy 1 Kẹo hiếm", 1, 0),
    ("Đổi vật phẩm cũ", "ngoc:246x3", "GEM:249:1", "3 Bột trắng lấy 1 Bột xanh lá", 1, 0),
    ("Đổi vật phẩm cũ", "ngoc:247x3", "GEM:249:1", "3 Bột xanh lấy 1 Bột xanh lá", 1, 0),
    ("Đổi vật phẩm cũ", "ngoc:248x3", "GEM:249:1", "3 Bột đỏ lấy 1 Bột xanh lá", 1, 0),
    ("Đổi vật phẩm cũ", "thuoc:160x10", "PT:161:1", "10 Quả bóng bạc lấy 1 Quả bóng vàng", 1, 0),
]
SO_GOI_XU = 5   # tieu hao ban theo goi 5 (1 lan bam)


def q(s):
    return "'" + str(s).replace("\\", "\\\\").replace("'", "''") + "'"


def main():
    dong = [d for d in csv.DictReader(open(VAO, encoding="utf-8")) if d["trang_thai"] == "can_code"]
    o = ["-- SINH RA boi tools/pokemon/sinh_vat_pham_lau_dai.py - KHONG SUA TAY. GD2b (docs/thiet-ke-game.md).\n",
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
        if i >= TANG_TOC[0][0]:
            raise SystemExit("het dai id (1250.. la cua tang toc)")
        gia = int(d["gia"]) if d["tien"] == "xu" else 0
        item.append("(%d,%s,28,0,0,0,%d,0,0,0,0,0,0,0,0,0,0,0,%d,-1,%d,0,-1,0,-1,%s,-1,0)" % (
            i, q(d["ten"]), int(d["cap_tu"]), gia, int(d["icon"] or 0), q(d["ten"])))
        map_.append("(%d,%s,%s,%s,%s,%s,%d,%d)" % (i, q(d["ma"]), q(d["ten"]), q(d["nhom"]), q(d["hieu_ung"]),
                                                   q(d["gioi_han"]), int(d["cap_tu"]), int(d["cap_den"])))
        i += 1
    for (tid, ma, ten, hu, icon) in TANG_TOC:
        item.append("(%d,%s,28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,%d,0,-1,0,-1,%s,-1,0)" % (tid, q(ten), icon, q(ten)))
        map_.append("(%d,%s,%s,'tang_toc',%s,%s,1,99)" % (tid, q(ma), q(ten), q(hu), q("chung 60 điểm/ngày, 210/tuần")))
    for (bid, ma, ten, mo_ta, tu, den, icon) in BUA:
        item.append("(%d,%s,28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,%d,0,-1,0,-1,%s,-1,0)" % (bid, q(ten), icon, q(ten)))
        map_.append("(%d,%s,%s,'bua',%s,'',1,100)" % (bid, q(ma), q(ten), q("bua:%d-%d" % (tu, den))))
    o.append("INSERT INTO `data_item` (`id`,`name`,`type`,`style`,`he`,`gender`,`level`,`durable`,`atb0`,`atb1`,`atb2`,"
             "`atb3`,`atb4`,`atb5`,`atb6`,`atb7`,`atb8`,`atb9`,`price`,`clazz`,`xstart`,`ystart`,`colortype`,`nloan`,"
             "`idUpLevel`,`namein`,`ideff`,`pricecu`) VALUES\n" + ",\n".join(item) + ";\n\n")
    for (hid, ma, ten) in HOP:
        map_.append("(%d,%s,%s,'hop',%s,'',1,100)" % (hid, q(ma), q(ten), q("mo_hop:%d" % hid)))
    o.append("INSERT INTO `data_vat_pham_lau_dai` VALUES\n" + ",\n".join(map_) + ";\n")
    ch = cua_hang(dong)
    o.append("\n-- Cua hang & doi vat pham (So tay huan luyen -> real.CuaHangLauDai). gioi_han_tuan: tob_mua_tuan.id_shop = %d + id.\n"
             % ID_CUA_HANG_TUAN)
    o.append("DROP TABLE IF EXISTS `data_cua_hang_lau_dai`;\n"
             "CREATE TABLE `data_cua_hang_lau_dai` (\n  `id` int NOT NULL,\n  `nhom` varchar(64) NOT NULL,\n"
             "  `ten` varchar(255) NOT NULL,\n  `chi_phi` varchar(255) NOT NULL,\n  `thuong` varchar(255) NOT NULL,\n"
             "  `cap` int NOT NULL DEFAULT 1,\n  `ngay_mo` int NOT NULL DEFAULT 0,\n  `lop` int NOT NULL DEFAULT -1,\n"
             "  `gioi_han_tuan` int NOT NULL DEFAULT 0,\n  PRIMARY KEY (`id`)\n) DEFAULT CHARSET=utf8;\n")
    o.append("INSERT INTO `data_cua_hang_lau_dai` VALUES\n" + ",\n".join(
        "(%d,%s,%s,%s,%s,%d,%d,%d,%d)" % (r[0], q(r[1]), q(r[2]), q(r[3]), q(r[4]), r[5], r[6], r[7], r[8]) for r in ch) + ";\n")
    open(RA, "w", encoding="utf-8").write("".join(o))
    print("da ghi %s: %d vat pham + %d tang toc + %d bua + %d hop, cua hang %d dong" % (RA, len(dong), len(TANG_TOC), len(BUA),
                                                                                    len(HOP), len(ch)))


def doc_csv(ten):
    return list(csv.DictReader(open(os.path.join(DOC, ten), encoding="utf-8")))


def cua_hang(dong):
    """(id, nhom, ten, chi_phi, thuong, cap, ngay_mo, lop, gioi_han_tuan)."""
    ra = []

    def them(nhom, ten, chi_phi, thuong, cap=1, ngay=0, lop=-1, gh=0):
        if re.search(r"\b(XU|LUONG|LUONGKHOA|DN|MN|DIEMDT)\b", thuong):
            raise SystemExit("cua hang khong duoc ra tien te: " + thuong)
        ra.append((len(ra) + 1, nhom, ten, chi_phi, thuong, int(cap), int(ngay), lop, gh))

    # 1. tieu hao ban bang xu (Chansey) - dung gia thiet ke (noi_dung_vat_pham.csv), goi 5
    i = ID_DAU
    id_theo_ma = {}
    for d in dong:
        id_theo_ma[d["ma"]] = i
        i += 1
    for d in dong:
        if d["tien"] == "xu" and int(d["gia"]) > 0:
            them("Thuốc và quả mọng (xu)", "%d %s" % (SO_GOI_XU, d["ten"]), "xu:%d" % (int(d["gia"]) * SO_GOI_XU),
                 "ITEM:%d:%d" % (id_theo_ma[d["ma"]], SO_GOI_XU), d["cap_tu"])
    # 2. luong khoa (Kecleon), co gioi han tuan theo thiet ke
    for d in dong:
        if d["tien"] == "luong_khoa":
            gh = int(re.match(r"(\d+)", d["gioi_han"]).group(1)) if d["gioi_han"] else 0
            them("Lượng khoá (Kecleon)", d["ten"], "luong_khoa:%d" % int(d["gia"]), "ITEM:%d:1" % id_theo_ma[d["ma"]],
                 d["cap_tu"], 0, -1, gh)
    for (bid, _ma, ten, _mt, tu, _den, _ic) in BUA:
        them("Lượng khoá (Kecleon)", ten, "luong_khoa:%d" % (10 if tu < 11 else 25), "ITEM:%d:1" % bid, 1)
    # 3. trang bi khong co nguon cu (Bronzong, xu) - noi_dung_cua_hang.csv
    tb = {int(r["id_item"]): r for r in doc_csv("noi_dung_trang_bi.csv") if r["id_item"].isdigit()}
    co_ban = set()
    for r in doc_csv("noi_dung_cua_hang.csv"):
        m = re.match(r"item:(\d+)$", r["vat_pham"])
        if not m or not r["cua_hang"].startswith("Bronzong"):
            continue
        iid = int(m.group(1))
        if iid < 600:   # bac 0..7 da co NPC ban / roi cu (MessageCreator.ALL_ID_ITEM_NPC_SELL, Map.dropItem)
            continue
        them("Trang bị (Bronzong, xu)", r["ten"], "xu:%s" % r["gia"], "ITEM:%d:1" % iid, r["cap"], r["ngay_mo"])
        co_ban.add(iid)
    # mau thu hai cung o (1005, 1020: ban nu) - cung gia voi mau dau
    for a, b in ((1004, 1005), (1019, 1020)):
        if a in co_ban:
            r = tb[a]
            them("Trang bị (Bronzong, xu)", r["ten"] + " (mẫu 2)", "xu:%s" % r["gia_xu"], "ITEM:%d:1" % b,
                 r["cap_yeu_cau"], r["ngay_mo"])
    # 4. che tao (Machamp) - noi_dung_che_tao.csv: da (ngoc 8) + nguyen lieu phu + 50% gia
    ct = doc_csv("noi_dung_che_tao.csv")
    for r in ct:
        iid = int(r["thanh_pham"].split(":")[1])
        t = tb.get(iid, {})
        them("Chế tạo (Machamp)", r["ten"], "xu:%s;ngoc:8x%s;%sx%s" % (r["xu"], r["da"], r["anh_xa_nl_phu"], r["so_nl_phu"]),
             "ITEM:%d:1" % iid, r["cap"], t.get("ngay_mo", 0) or 0)
    # Nam 2 chua co dong roi / Gym / nhiem vu chay duoc (nhiem vu chinh nam 2 la dan y GD3, dong_co_tuong_thich=0):
    # vu khi / huy hieu / ruy bang toi thuong + ruy bang than thoai -> che tao nhu bac cua mon + 1 Manh tinh hoa
    lop_dong = {"Charmander": 0, "Bulbasaur": 1, "Eevee": 2, "Squirtle": 3, "Pichu": 4}
    for iid in (1018, 1023, 1024, 1025, 1026, 1027, 1028, 1033):
        t = tb[iid]
        bc = [r for r in ct if r["bac"] == t["bac"]][0]
        them("Chế tạo (Machamp)", t["ten"], "xu:%s;ngoc:8x%s;%sx%s;item:1038x1" % (bc["xu"], bc["da"], bc["anh_xa_nl_phu"],
                                                                                   bc["so_nl_phu"]),
             "ITEM:%d:1" % iid, t["cap_yeu_cau"], t["ngay_mo"], lop_dong.get(t["dong"], -1))
    # 5. doi tin vat / vat pham cu
    for (nhom, cp, th, nhan, cap, gh) in DOI:
        them(nhom, nhan, cp, th, cap, 0, -1, gh)
    # 6. tang toc ban khoa (Kecleon, luong khoa) - O CUOI de id dong cu khong doi (tob_mua_tuan.id_shop = 10000 + id)
    ten_tt = {t[0]: t[2] for t in TANG_TOC}
    for (tid, lk, gh) in TANG_TOC_KECLEON:
        them("Lượng khoá (Kecleon)", ten_tt[tid], "luong_khoa:%d" % lk, "ITEM:%d:1" % tid, 1, 0, -1, gh)
    return ra


if __name__ == "__main__":
    main()
