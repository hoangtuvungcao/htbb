#!/usr/bin/env python3
"""BỘ SINH ĐỒ MỚI + TRANG PHỤC (0.9.8) - chạy lại được, không sửa tay các tệp ra.

Nguồn: tools/do_trang_phuc/danh_muc.py (danh mục), hinh_trang_bi.py / hinh_trang_phuc.py (hình vẽ tay bằng lưới),
tools/luu_tru_sinh_du_lieu/du_lieu/noi_dung_trang_bi.csv (món gốc mỗi ô x bậc), sql/htbb.sql phần A (cột data_item của món gốc).

Ra:
  server/map/icon_do_moi/<n>.png          icon 16x16, máy chủ phục vụ ở id ảnh 15000+n (com.htbb.pokemon.IconDoMoi)
  client_jar/res/pk/trang_phuc.png        atlas mảnh trang phục / hào quang (lớp phủ trên thân Pokémon)
  client_jar/res/pk/trang_phuc.bin        chỉ mục atlas + bộ mảnh của từng trang phục / skin / thẻ
  client_jar/res/pk/khung_hiem.png        khung độ hiếm 20x20 (6 bậc x 2 khung lấp lánh) cho ô vật phẩm
  du_lieu/do_moi_trang_bi.csv, du_lieu/do_moi_bo.csv, du_lieu/trang_phuc.csv
  sql/htbb.sql (phan B) khoi "-- >>> them_do_trang_phuc.sql"  idempotent (MariaDB 10.3, utf8)
  web/shared/data/danh-muc-do-trang-phuc.json + web/public/icon-do/<n>.png
  [--xem <thư mục>]                       ảnh xem trước (bộ sưu tập) phóng to

Chạy (ở thư mục gốc repo): python3 -I tools/do_trang_phuc/sinh.py [--xem /tmp/do_moi]
"""
import csv
import json
import os
import re
import struct
import sys

DAY = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, DAY)

import danh_muc as DM  # noqa: E402
import hinh_trang_bi as HB  # noqa: E402
import hinh_trang_phuc as HP  # noqa: E402
from pixel import anh_moi, dan, ghi_png, phong, lat_ngang, ve_luoi, mau_vien, sang, toi, to_nen, viet_so  # noqa: E402

GOC = os.path.dirname(os.path.dirname(DAY))
ID_ANH_DAU = 15000
ID_ITEM_HE_DAU = 1400        # 18 hệ x 6 ô x 5 bậc = 540 -> 1400..1939
ID_ITEM_VUNG_DAU = 1940      # 11 bậc x 8 ô = 88 -> 1940..2027
ID_ITEM_HET = 2099           # dải dành riêng 1400..2099

COT_ITEM = ["id", "name", "type", "style", "he", "gender", "level", "durable", "atb0", "atb1", "atb2", "atb3", "atb4",
            "atb5", "atb6", "atb7", "atb8", "atb9", "price", "clazz", "xstart", "ystart", "colortype", "nloan",
            "idUpLevel", "namein", "ideff", "pricecu"]


# ================================================================ đọc nguồn

def chuyen(v):
    if v is None:
        return None
    if v == "NULL":
        return None
    if re.fullmatch(r"-?\d+", v):
        return int(v)
    return v


def doc_data_item():
    s = open(os.path.join(GOC, "sql", "htbb.sql"), encoding="utf8").read()
    s = s[:s.index("\n-- ######## B. NANG CAP")]  # phan A (CSDL day du)
    m = re.search(r"INSERT INTO `data_item` VALUES (.*?);\n", s, re.S)
    # chuỗi trong dump đã thoát nháy bằng \' ; tách theo bộ
    hang = tach_bo_dump(m.group(1))
    return {h[0]: h for h in hang}


def tach_bo_dump(chuoi):
    """Tách bộ giá trị của mysqldump (chuỗi '...' thoát bằng \\)."""
    ra = []
    i, n = 0, len(chuoi)
    while i < n:
        if chuoi[i] != "(":
            i += 1
            continue
        i += 1
        bo = []
        while True:
            if chuoi[i] == "'":
                i += 1
                gt = []
                while chuoi[i] != "'":
                    if chuoi[i] == "\\":
                        gt.append({"n": "\n", "r": "\r", "t": "\t", "0": "\0"}.get(chuoi[i + 1], chuoi[i + 1]))
                        i += 2
                    else:
                        gt.append(chuoi[i])
                        i += 1
                i += 1
                bo.append("".join(gt))
            else:
                j = i
                while chuoi[j] not in ",)":
                    j += 1
                bo.append(chuyen(chuoi[i:j].strip()))
                i = j
            if chuoi[i] == ",":
                i += 1
                continue
            i += 1  # ')'
            break
        ra.append(tuple(bo))
    return ra


def doc_mon_goc():
    """khoá ô_bậc (vu_khi_<bậc>_c<k>) -> id data_item gốc, từ du_lieu/noi_dung_trang_bi.csv."""
    ra = {}
    with open(os.path.join(GOC, "tools", "luu_tru_sinh_du_lieu", "du_lieu", "noi_dung_trang_bi.csv"), encoding="utf8") as f:  # luu tru (mot tep SQL)
        for r in csv.DictReader(f):
            ra[r["ma"]] = int(r["id_item"])
    return ra


# ================================================================ danh mục đồ

def danh_sach_do():
    """List dict: id, ten, type, bac, ma_ho, loai_ho, he, vung, khoa_goc, icon_hinh(...)."""
    goc = doc_mon_goc()
    ds = []
    for he, ma, ten_bo, poke, khac in DM.BO_HE:
        for oi, (okey, typ, noun) in enumerate(DM.O_BO_HE):
            for ti, bac in enumerate(DM.BAC_BO_HE):
                ds.append(dict(id=ID_ITEM_HE_DAU + he * 30 + oi * 5 + ti, ten="%s %s" % (noun, poke[ti]), type=typ,
                               bac=bac, ma_ho="he_" + ma, loai="he", he=he, vung="", id_goc=goc["%s_%d" % (okey, bac)],
                               hinh=("dang", okey), o=okey, nguon="quai_he,boss" + (",che_tao" if okey == "vong_co" else "")))
    for bac in range(11):
        vung = [v for v in DM.VUNG if v[2] <= bac <= v[3]][0]
        base = ID_ITEM_VUNG_DAU + bac * 8
        ds.append(dict(id=base, ten="Huy hiệu " + DM.HUY_CHUONG[bac], type=8, bac=bac, ma_ho="vung_" + vung[0],
                       loai="vung", he=-1, vung=vung[0], id_goc=goc["huy_hieu_%d" % bac], hinh=("gym", bac), o="huy_hieu",
                       nguon="quai_vung,boss,gym"))
        ds.append(dict(id=base + 1, ten="Găng " + DM.TRUONG_GYM[bac], type=11, bac=bac, ma_ho="vung_" + vung[0],
                       loai="vung", he=-1, vung=vung[0], id_goc=goc["gang_dam_%d" % bac], hinh=("dang", "gang"),
                       o="gang_dam", nguon="quai_vung,boss"))
        ds.append(dict(id=base + 2, ten="Phiến đá " + DM.DIA_DANH[bac], type=12, bac=bac, ma_ho="vung_" + vung[0],
                       loai="vung", he=-1, vung=vung[0], id_goc=goc["phien_da_%d" % bac], hinh=("dang", "phien_da"),
                       o="phien_da", nguon="quai_vung,boss,che_tao"))
        for k, (typ, dong, noun, _) in enumerate(DM.DONG_TAY):
            ds.append(dict(id=base + 3 + k, ten="%s %s" % (noun, DM.DIA_DANH[bac]), type=typ, bac=bac,
                           ma_ho="vung_" + vung[0], loai="vung", he=-1, vung=vung[0],
                           id_goc=goc["vu_khi_%d_c%d" % (bac, k)], hinh=("tay", k), o="vu_khi", dong=dong,
                           nguon="quai_vung,boss,gym"))
    ds.sort(key=lambda d: d["id"])
    return ds


# Huy hiệu vùng 5x5 đóng trên găng / phiến đá: Kanto Poké Ball, Johto chuông tháp, Hoenn sóng
HUY_HIEU_VUNG = {
    "kanto": [".###.", "#####", "#.#.#", "#...#", ".###."],
    "johto": ["..#..", ".###.", ".###.", "#####", "..#.."],
    "hoenn": ["#....", "##..#", "#####", ".####", "..##."],
}


def icon_do(d):
    chat = HB.CHAT_LIEU[d["bac"]][1]
    kieu, khoa = d["hinh"]
    if d["loai"] == "he":
        nhan = HB.MAU_HE[d["he"]]
        luoi, hh = HB.DANG[khoa]
        return HB.lam_icon(luoi, chat, nhan, d["he"] if hh else None, hh, d["bac"])
    nhan = HB.MAU_VUNG[d["vung"]]
    if kieu == "gym":
        return HB.lam_icon(HB.huy_hieu_gym(khoa), chat, nhan, None, None, d["bac"])
    if kieu == "tay":
        return HB.lam_icon(HB.chuan_luoi(HB.DANG_TAY[khoa]), chat, DM.DONG_TAY[khoa][3], None, None, d["bac"])
    luoi, hh = HB.DANG[khoa]
    anh = HB.lam_icon(luoi, chat, nhan, None, None, d["bac"])
    # đóng huy hiệu vùng lên mặt khoá (dịch theo phần căn giữa của lam_icon)
    bm = HB.bang_mau(chat, nhan)
    tho = ve_luoi(luoi, bm, vien=HB.vien_cua(chat, nhan))
    ox, oy = (16 - tho[0]) // 2, (16 - tho[1]) // 2
    mat = HUY_HIEU_VUNG[d["vung"]]
    net = toi(nhan, 0.1)
    for y in range(5):
        for x in range(5):
            if mat[y][x] == "#":
                anh[2][oy + hh[1] + y + 1][ox + hh[0] + x + 1] = 0xFF000000 | net
    return anh


# ================================================================ trang trí

def danh_sach_trang_tri():
    """List dict: o, id (trong ô), ma, ten, do_hiem, nguon, tham_so, gia_luong, mo_ta, manh (list), them (dict)."""
    ds = []
    for (i, ma, ten, manh, hiem, nguon, ts, gia, mo_ta) in DM.TRANG_PHUC:
        ds.append(dict(o="trang_phuc", id=i, ma=ma, ten=ten, do_hiem=hiem, nguon=nguon, tham_so=ts, gia_luong=gia,
                       mo_ta=mo_ta, manh=manh))
    for (i, ma, ten, chinh, sang_) in HP.HAO_QUANG:
        hiem, nguon, ts, gia, mo_ta = DM.NGUON_HAO_QUANG[ma]
        ds.append(dict(o="hao_quang", id=i, ma=DM.MA_CU.get("hq_" + ma, "hq_" + ma), ten=ten, do_hiem=hiem, nguon=nguon, tham_so=ts,
                       gia_luong=gia, mo_ta=mo_ta, manh=[]))
    for (i, ma, ten, yen, day, bang) in HP.SKIN_THU:
        hiem, nguon, ts, gia, mo_ta = DM.NGUON_SKIN_THU[ma]
        ds.append(dict(o="skin_thu", id=i, ma=DM.MA_CU.get("st_" + ma, "st_" + ma), ten=ten, do_hiem=hiem, nguon=nguon, tham_so=ts,
                       gia_luong=gia, mo_ta=mo_ta, manh=[]))
    for (i, ma, ten, manh, mau_ten) in HP.THE_DONG_HANH:
        hiem, nguon, ts, gia, mo_ta = DM.NGUON_THE[ma]
        ds.append(dict(o="the_dong_hanh", id=i, ma="th_" + ma, ten=ten, do_hiem=hiem, nguon=nguon, tham_so=ts,
                       gia_luong=gia, mo_ta=mo_ta, manh=[manh]))
    # món cửa hàng cũ (0.9.8 đồng bộ) ĐỨNG CUỐI: icon / thứ tự của 61 món trước giữ nguyên số
    return [t for t in ds if t["ma"] not in DM.MON_CUA_HANG_CU] + [t for t in ds if t["ma"] in DM.MON_CUA_HANG_CU]


def icon_trang_phuc(t):
    """Icon 16x16 của một trang phục: mảnh chính (mũ / lưng) ở trên, mảnh thứ hai dưới, đồ cầm ở góc."""
    anh = anh_moi(16, 16)
    manh = [HP.MANH[m] for m in t["manh"]]
    tren = [m for m in manh if m["o"] in (HP.MU,)]
    giua = [m for m in manh if m["o"] in (HP.KHAN, HP.LUNG)]
    tay = [m for m in manh if m["o"] == HP.TAY]
    y = 0
    if tren:
        a = HP.anh_manh(tren[0], HP.XUONG)[0]
        dan(anh, a, (16 - a[0]) // 2, 0)
        y = a[1] - 1
    if giua:
        a = HP.anh_manh(giua[0], HP.XUONG)[0]
        if not tren:
            y = (16 - a[1]) // 2 - 1
        dan(anh, a, (16 - a[0]) // 2, min(y, 16 - a[1]))
    if tay:
        a = HP.anh_manh(tay[0], HP.XUONG)[0]
        dan(anh, a, 16 - a[0], 16 - a[1])
    return anh


def icon_hao_quang(hq):
    anh = anh_moi(16, 16)
    k0 = HP.khung_hao_quang(hq, 0)
    dan(anh, k0, 0, 9)
    # hạt sáng bay lên giữa vòng
    _, _, _, chinh, sang_ = hq
    for (x, y, c) in ((7, 3, sang_), (7, 2, sang(sang_, 0.5)), (4, 6, chinh), (10, 5, sang_), (11, 4, chinh)):
        anh[2][y][x] = 0xFF000000 | c
    for (x, y) in ((6, 3), (8, 3), (7, 4)):
        anh[2][y][x] = 0xFF000000 | sang(chinh, 0.3)
    return anh


def icon_yen(sk):
    _, ma, ten, yen, day, _ = sk
    a, b, c = sang(yen, 0.4), yen, toi(yen, 0.38)
    bang = {"1": a, "2": b, "3": c, "4": day, "W": sang(a, 0.6), "a": day}
    anh_y = ve_luoi(HP.YEN, bang, vien=mau_vien(yen))
    anh = anh_moi(16, 16)
    dan(anh, anh_y, (16 - anh_y[0]) // 2, (16 - anh_y[1]) // 2)
    return anh


def icon_the(th):
    _, ma, ten, id_manh, mau_ten = th
    m = HP.MANH[id_manh]
    a = phong(HP.anh_manh(m, HP.XUONG)[0], 2)
    anh = anh_moi(16, 16)
    dan(anh, a, (16 - a[0]) // 2, max(0, (12 - a[1]) // 2))
    # nhãn tên bên dưới (vạch màu nhãn)
    for x in range(3, 13):
        anh[2][14][x] = 0xFF000000 | toi(mau_ten, 0.55)
        anh[2][13][x] = 0xFF000000 | mau_ten
    anh[2][13][2] = anh[2][13][13] = 0xFF000000 | toi(mau_ten, 0.55)
    return anh


# ================================================================ khung độ hiếm (ô vật phẩm)
KHUNG_HIEM = [  # (tên, viền, sáng, có lấp lánh)
    ("Thường", 0x8A94A6, 0xD8DCE4, False),
    ("Hiếm", 0x2F9A3C, 0x8CF098, False),
    ("Quý", 0x2A6FD0, 0x9CCBFF, False),
    ("Sử thi", 0x8A3CD8, 0xE0B8FF, True),
    ("Huyền thoại", 0xE07010, 0xFFE070, True),
    ("Trang trí", 0xC89818, 0xFFF0A0, True),
]
LUOI_KHUNG = """
.aaaaaaaaaaaaaaaaaa.
abbbbb........bbbbba
ab................ba
ab................ba
ab................ba
ab................ba
a..................a
a..................a
a..................a
a..................a
a..................a
a..................a
a..................a
a..................a
ab................ba
ab................ba
ab................ba
ab................ba
abbbbb........bbbbba
.aaaaaaaaaaaaaaaaaa.
"""


def khung_hiem():
    """Ảnh 40 x (20*6): mỗi hàng một bậc, 2 khung (khung 2 dịch điểm lấp lánh)."""
    anh = anh_moi(40, 20 * len(KHUNG_HIEM))
    for i, (_, vien, sang_, lap) in enumerate(KHUNG_HIEM):
        for f in range(2):
            a = ve_luoi(LUOI_KHUNG, {"a": vien, "b": sang_})
            if lap:
                for (x, y) in ([(17, 2), (2, 17)] if f == 0 else [(2, 2), (17, 17)]):
                    for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
                        a[2][y + dy][x + dx] = 0xFF000000 | (0xFFFFFF if (dx, dy) == (0, 0) else sang_)
            dan(anh, a, f * 20, i * 20)
    return anh


# ================================================================ atlas trang phục + chỉ mục

def lam_atlas(ds_tt):
    """Xếp mọi mảnh (3 hướng) + khung hào quang vào một atlas; trả (ảnh, bin)."""
    vat = []  # (khoá, ảnh, thông tin)
    for mid in sorted(HP.MANH):
        m = HP.MANH[mid]
        for hu in (HP.XUONG, HP.LEN, HP.NGANG):
            a, ax, ay, lop, neo, dx, dy = HP.anh_manh(m, hu)
            vat.append((("m", mid, hu), a, (m["o"], hu, lop, neo, dx, dy, ax, ay)))
    for hq in HP.HAO_QUANG:
        for f in range(4):
            vat.append((("h", hq[0], f), HP.khung_hao_quang(hq, f), None))
    # xếp kệ: rộng 128
    RONG = 128
    x = y = cao_ke = 0
    vi_tri = {}
    for khoa, a, _ in vat:
        if x + a[0] > RONG:
            x = 0
            y += cao_ke + 1
            cao_ke = 0
        vi_tri[khoa] = (x, y)
        x += a[0] + 1
        cao_ke = max(cao_ke, a[1])
    cao = y + cao_ke + 1
    atlas = anh_moi(RONG, cao)
    for khoa, a, _ in vat:
        dan(atlas, a, *vi_tri[khoa])
    b = bytearray()
    b += b"T" + bytes([1])
    manh = [v for v in vat if v[0][0] == "m"]
    b += struct.pack(">H", len(manh))
    for khoa, a, (o, hu, lop, neo, dx, dy, ax, ay) in manh:
        px, py = vi_tri[khoa]
        b += struct.pack(">BBBBBbbbbHHBB", khoa[1], o, hu, lop, neo, dx, dy, ax, ay, px, py, a[0], a[1])
    tp = [t for t in ds_tt if t["o"] == "trang_phuc"]
    b += bytes([len(tp)])
    for t in tp:
        b += bytes([t["id"], len(t["manh"])]) + bytes(t["manh"])
    b += bytes([len(HP.HAO_QUANG)])
    for hq in HP.HAO_QUANG:
        b += bytes([hq[0], 4])
        for f in range(4):
            px, py = vi_tri[("h", hq[0], f)]
            b += struct.pack(">HHBB", px, py, 15, 5)
        b += struct.pack(">I", hq[3])[1:] + struct.pack(">I", hq[4])[1:]
    b += bytes([len(HP.SKIN_THU)])
    for (i, ma, ten, yen, day, (xoay, bh, sg, phu, phu_pt)) in HP.SKIN_THU:
        b += struct.pack(">BhBB", i, xoay, bh, sg)
        b += struct.pack(">I", phu)[1:] + bytes([phu_pt])
    b += bytes([len(HP.THE_DONG_HANH)])
    for (i, ma, ten, id_manh, mau_ten) in HP.THE_DONG_HANH:
        b += bytes([i, id_manh]) + struct.pack(">I", mau_ten)[1:]
    return atlas, bytes(b)


# ================================================================ SQL

def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, int):
        return str(v)
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"


def sinh_sql(ds_do, ds_tt, mon, icon_do_id, icon_tt_id):
    o = []
    w = o.append
    w("-- ============================================================================================================\n")
    w("-- them_do_trang_phuc.sql (0.9.8) - SINH BOI tools/do_trang_phuc/sinh.py, dung sua tay. Idempotent, MariaDB 10.3.\n")
    w("--\n")
    w("-- 1) Do ho moi: %d mon data_item id %d..%d (BAN SAO chi so mon goc cung o + bac; ten, icon 15000+n rieng):\n"
      % (len(ds_do), ID_ITEM_HE_DAU, max(d["id"] for d in ds_do)))
    w("--    18 bo he x 6 o x 5 bac + 3 bo vung (huy chuong Gym) x 11 bac x 8 o. Thuong bo nam TRONG tran chung.\n")
    w("--    Khong nguon vao moi: do ho THAY mon goc khi roi (quai / boss / che tao), real.DoMoiGame.\n")
    w("-- 2) Trang tri: data_trang_phuc_o (ngan sach theo o, phan nghin), data_trang_phuc (%d mon), cua hang luong\n"
      % len(ds_tt))
    w("--    (nap_cua_hang, chi HINH - cung ngan sach o voi mon mien phi), tob_trang_phuc (dong chi so / bo dem).\n")
    w("--    So huu dung chung nap_so_huu (Tu do trang tri), mon dang mac = dang_dung.\n")
    w("-- Khoi nay nam trong sql/htbb.sql phan B muc 11 (sinh.py ghi thang khoi). Chay lai duoc: chi ghi de cot cua chinh khoi nay.\n")
    w("-- ============================================================================================================\n")
    w("\n")
    # ---- bang ho
    w("CREATE TABLE IF NOT EXISTS `data_trang_bi_ho` (\n"
      "  `ma` VARCHAR(24) NOT NULL,\n  `ten` VARCHAR(64) NOT NULL DEFAULT '',\n"
      "  `loai` VARCHAR(8) NOT NULL DEFAULT 'he' COMMENT 'he = bo he (6 o vai) | vung = bo huy chuong vung',\n"
      "  `he` TINYINT NOT NULL DEFAULT -1 COMMENT 'he 0..17 (HePokemon), -1 = bo vung',\n"
      "  `he_khac` TINYINT NOT NULL DEFAULT -1 COMMENT 'he danh sieu hieu qua vao he nay (thuong khang 4 mon)',\n"
      "  `vung` VARCHAR(8) NOT NULL DEFAULT '',\n  `bac_tu` TINYINT NOT NULL DEFAULT 0,\n"
      "  `bac_den` TINYINT NOT NULL DEFAULT 10,\n  `mo_ta` VARCHAR(255) NOT NULL DEFAULT '',\n"
      "  `thu_tu` INT NOT NULL DEFAULT 0,\n  `bat` TINYINT NOT NULL DEFAULT 1,\n  PRIMARY KEY (`ma`)\n"
      ") ENGINE=MyISAM DEFAULT CHARSET=utf8;\n\n")
    rows = []
    for k, (he, ma, ten, poke, khac) in enumerate(DM.BO_HE):
        rows.append("(%s,%s,'he',%d,%d,'',2,10,%s,%d,1)" % (q("he_" + ma), q(ten), he, khac, q(
            "%s: đồ hệ %s (%s → %s). 2 món +2%% sát thương hệ %s, 4 món +3%% kháng hệ %s, 6 món thêm +3%% sát thương hệ %s."
            % (ten, DM.TEN_HE[he], poke[0], poke[-1], DM.TEN_HE[he], DM.TEN_HE[khac], DM.TEN_HE[he])), k + 1))
    for k, (ma, ten, tu, den) in enumerate(DM.VUNG):
        rows.append("(%s,%s,'vung',-1,-1,%s,%d,%d,%s,%d,1)" % (q("vung_" + ma), q(ten), q(ma), tu, den, q(
            "%s: huy hiệu, găng, phiến đá, đồ cầm tay bậc %s-%s. 2 món +1%% HP, 3 món +1%% tấn công, 4 món +1%% chí mạng "
            "(chung trần bộ 5%% / chí mạng bộ 3%%)." % (ten, DM.TEN_BAC[tu], DM.TEN_BAC[den])), 100 + k))
    w("INSERT INTO `data_trang_bi_ho` (`ma`,`ten`,`loai`,`he`,`he_khac`,`vung`,`bac_tu`,`bac_den`,`mo_ta`,`thu_tu`,`bat`) VALUES\n")
    w(",\n".join(rows))
    w("\nON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`),`loai`=VALUES(`loai`),`he`=VALUES(`he`),`he_khac`=VALUES(`he_khac`),"
      "`vung`=VALUES(`vung`),`bac_tu`=VALUES(`bac_tu`),`bac_den`=VALUES(`bac_den`),`mo_ta`=VALUES(`mo_ta`),"
      "`thu_tu`=VALUES(`thu_tu`);\n\n")
    # ---- thuong bo
    w("CREATE TABLE IF NOT EXISTS `data_trang_bi_ho_thuong` (\n"
      "  `ma_ho` VARCHAR(24) NOT NULL,\n  `so_mon` TINYINT NOT NULL,\n"
      "  `chi_so` VARCHAR(16) NOT NULL COMMENT 'st_he | khang_he_khac | hp | tan_cong | chi_mang',\n"
      "  `phan_nghin` INT NOT NULL DEFAULT 0,\n  `mo_ta` VARCHAR(128) NOT NULL DEFAULT '',\n"
      "  PRIMARY KEY (`ma_ho`,`so_mon`,`chi_so`)\n) ENGINE=MyISAM DEFAULT CHARSET=utf8;\n\n")
    rows = []
    ten_cs = {"st_he": "sát thương hệ", "khang_he_khac": "kháng hệ khắc", "hp": "HP", "tan_cong": "tấn công",
              "chi_mang": "chí mạng"}
    for he, ma, ten, poke, khac in DM.BO_HE:
        for so, cs, pn in DM.THUONG_BO_HE:
            rows.append("(%s,%d,%s,%d,%s)" % (q("he_" + ma), so, q(cs), pn, q("%d món: +%s%% %s" % (
                so, fmt_pt(pn), ten_cs[cs] + (" " + DM.TEN_HE[he] if cs == "st_he" else " " + DM.TEN_HE[khac])))))
    for ma, ten, tu, den in DM.VUNG:
        for so, cs, pn in DM.THUONG_BO_VUNG:
            rows.append("(%s,%d,%s,%d,%s)" % (q("vung_" + ma), so, q(cs), pn, q("%d món: +%s%% %s" % (
                so, fmt_pt(pn), ten_cs[cs]))))
    w("INSERT INTO `data_trang_bi_ho_thuong` (`ma_ho`,`so_mon`,`chi_so`,`phan_nghin`,`mo_ta`) VALUES\n")
    w(",\n".join(rows))
    w("\nON DUPLICATE KEY UPDATE `phan_nghin`=VALUES(`phan_nghin`),`mo_ta`=VALUES(`mo_ta`);\n\n")
    # ---- danh sach do moi
    w("CREATE TABLE IF NOT EXISTS `data_trang_bi_moi` (\n"
      "  `id_item` INT NOT NULL,\n  `ma_ho` VARCHAR(24) NOT NULL,\n  `type` TINYINT NOT NULL,\n"
      "  `bac` TINYINT NOT NULL,\n  `id_goc` INT NOT NULL COMMENT 'mon goc cung o + bac (chi so ban sao)',\n"
      "  `ten` VARCHAR(64) NOT NULL DEFAULT '',\n  `icon` INT NOT NULL DEFAULT 0,\n"
      "  `nguon` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'quai_he | quai_vung | boss | che_tao | gym',\n"
      "  PRIMARY KEY (`id_item`),\n  KEY `idx_dtbm_goc` (`type`,`bac`)\n) ENGINE=MyISAM DEFAULT CHARSET=utf8;\n\n")
    rows = ["(%d,%s,%d,%d,%d,%s,%d,%s)" % (d["id"], q(d["ma_ho"]), d["type"], d["bac"], d["id_goc"], q(d["ten"]),
                                           icon_do_id[d["id"]], q(d["nguon"])) for d in ds_do]
    w("INSERT INTO `data_trang_bi_moi` (`id_item`,`ma_ho`,`type`,`bac`,`id_goc`,`ten`,`icon`,`nguon`) VALUES\n")
    w(",\n".join(rows))
    w("\nON DUPLICATE KEY UPDATE `ma_ho`=VALUES(`ma_ho`),`type`=VALUES(`type`),`bac`=VALUES(`bac`),"
      "`id_goc`=VALUES(`id_goc`),`ten`=VALUES(`ten`),`icon`=VALUES(`icon`),`nguon`=VALUES(`nguon`);\n\n")
    # ---- data_item: ban sao mon goc
    w("-- data_item: ban sao cot cua mon goc (phan A sql/htbb.sql) - doi id, name, namein, xstart (icon 15000+n).\n")
    rows = []
    for d in ds_do:
        g = list(mon[d["id_goc"]])
        g[0] = d["id"]
        g[1] = d["ten"]
        g[COT_ITEM.index("xstart")] = icon_do_id[d["id"]]
        g[COT_ITEM.index("namein")] = d["ten"]
        rows.append("(" + ",".join(q(v) for v in g) + ")")
    w("INSERT INTO `data_item` (" + ",".join("`%s`" % c for c in COT_ITEM) + ") VALUES\n")
    w(",\n".join(rows))
    w("\nON DUPLICATE KEY UPDATE " + ",".join("`%s`=VALUES(`%s`)" % (c, c) for c in COT_ITEM[1:]) + ";\n\n")
    # ---- trang tri
    w("CREATE TABLE IF NOT EXISTS `data_trang_phuc_o` (\n"
      "  `o` VARCHAR(16) NOT NULL,\n  `ten` VARCHAR(32) NOT NULL DEFAULT '',\n"
      "  `phan_nghin` INT NOT NULL DEFAULT 0 COMMENT 'ngan sach o (10 = 1%), mon nao dang mac cung chi dung bang nay',\n"
      "  `dong` VARCHAR(80) NOT NULL DEFAULT '' COMMENT 'dong chi so chon duoc o Tu do: ma:ten,...',\n"
      "  `thu_tu` INT NOT NULL DEFAULT 0,\n  PRIMARY KEY (`o`)\n) ENGINE=MyISAM DEFAULT CHARSET=utf8;\n\n")
    rows = []
    for k, (o_, ten, pn, dong) in enumerate(DM.O_TRANG_TRI):
        rows.append("(%s,%s,%d,%s,%d)" % (q(o_), q(ten), pn, q(",".join("%s:%s" % x for x in dong)), k + 1))
    w("INSERT INTO `data_trang_phuc_o` (`o`,`ten`,`phan_nghin`,`dong`,`thu_tu`) VALUES\n" + ",\n".join(rows))
    w("\nON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`),`dong`=VALUES(`dong`),`thu_tu`=VALUES(`thu_tu`);\n"
      "-- phan_nghin KHONG ghi de khi chay lai (chu game chinh tren DB); server kep cung theo tran trong ma.\n\n")
    w("CREATE TABLE IF NOT EXISTS `data_trang_phuc` (\n"
      "  `ma` VARCHAR(40) NOT NULL COMMENT 'trung nap_so_huu.ma / nap_cua_hang.ma',\n"
      "  `o` VARCHAR(16) NOT NULL,\n  `so` TINYINT UNSIGNED NOT NULL COMMENT 'so hieu trong o (goi -102 / atlas client)',\n"
      "  `ten` VARCHAR(64) NOT NULL DEFAULT '',\n  `do_hiem` TINYINT NOT NULL DEFAULT 0,\n"
      "  `icon` INT NOT NULL DEFAULT 0,\n  `manh` VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'id manh hinh (atlas)',\n"
      "  `nguon` VARCHAR(16) NOT NULL DEFAULT '' COMMENT 'mien_phi | su_kien | thanh_tuu | cua_hang | che_tao',\n"
      "  `tham_so` VARCHAR(96) NOT NULL DEFAULT '',\n  `gia_luong` INT NOT NULL DEFAULT 0,\n"
      "  `mo_ta` VARCHAR(255) NOT NULL DEFAULT '',\n  `thu_tu` INT NOT NULL DEFAULT 0,\n"
      "  `bat` TINYINT NOT NULL DEFAULT 1,\n  PRIMARY KEY (`ma`),\n  UNIQUE KEY `uq_dtp_o_so` (`o`,`so`)\n"
      ") ENGINE=MyISAM DEFAULT CHARSET=utf8;\n\n")
    rows = []
    for k, t in enumerate(ds_tt):
        rows.append("(%s,%s,%d,%s,%d,%d,%s,%s,%s,%d,%s,%d,1)" % (
            q(t["ma"]), q(t["o"]), t["id"], q(t["ten"]), t["do_hiem"], icon_tt_id[t["ma"]],
            q(",".join(str(m) for m in t["manh"])), q(t["nguon"]), q(t["tham_so"]), t["gia_luong"], q(t["mo_ta"]),
            k + 1))
    w("INSERT INTO `data_trang_phuc` (`ma`,`o`,`so`,`ten`,`do_hiem`,`icon`,`manh`,`nguon`,`tham_so`,`gia_luong`,"
      "`mo_ta`,`thu_tu`,`bat`) VALUES\n" + ",\n".join(rows))
    w("\nON DUPLICATE KEY UPDATE `o`=VALUES(`o`),`so`=VALUES(`so`),`ten`=VALUES(`ten`),`do_hiem`=VALUES(`do_hiem`),"
      "`icon`=VALUES(`icon`),`manh`=VALUES(`manh`),`nguon`=VALUES(`nguon`),`tham_so`=VALUES(`tham_so`),"
      "`gia_luong`=VALUES(`gia_luong`),`mo_ta`=VALUES(`mo_ta`),`thu_tu`=VALUES(`thu_tu`);\n\n")
    w("CREATE TABLE IF NOT EXISTS `tob_trang_phuc` (\n"
      "  `char_id` INT NOT NULL,\n  `khoa` VARCHAR(48) NOT NULL COMMENT 'dong:<o> = ma dong chi so | sk:<ma>:<nam> = dem quai su kien',\n"
      "  `gia_tri` VARCHAR(48) NOT NULL DEFAULT '',\n  `luc` BIGINT NOT NULL DEFAULT 0,\n"
      "  PRIMARY KEY (`char_id`,`khoa`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8;\n\n")
    # ---- cua hang luong (nap_cua_hang): chi HINH
    w("-- Cua hang luong (Tu do trang tri, nap_cua_hang): chi mon nguon cua_hang. co_chi_so = 0: chi so thuoc O (nhu mon mien phi)\n"
      "-- (cung so voi mon mien phi), khong giao dich (giao_dich_duoc = 0) de giu gia tri khoe va khong rua xu.\n")
    w("CREATE TABLE IF NOT EXISTS `nap_cua_hang` (\n  `ma` varchar(40) NOT NULL,\n  `ten` varchar(100) NOT NULL,\n"
      "  `nhom` varchar(20) NOT NULL,\n  `trao` varchar(20) NOT NULL,\n  `gia_luong` int NOT NULL,\n"
      "  `han_ngay` int NOT NULL DEFAULT 0,\n  `gioi_han` int NOT NULL DEFAULT 0,\n  `nhom_xoay` int NOT NULL DEFAULT 0,\n"
      "  `giao_dich_duoc` tinyint NOT NULL DEFAULT 0,\n  `co_chi_so` tinyint NOT NULL DEFAULT 0,\n"
      "  `dang_ban` tinyint NOT NULL DEFAULT 1,\n  `thu_tu` int NOT NULL DEFAULT 0,\n"
      "  `mo_ta` varchar(255) NOT NULL DEFAULT '',\n  PRIMARY KEY (`ma`)\n) DEFAULT CHARSET=utf8;\n\n")
    rows = []
    for k, t in enumerate([t for t in ds_tt if t["nguon"] == "cua_hang"]):
        rows.append("(%s,%s,%s,'trang_tri',%d,0,1,0,0,0,1,%d,%s)" % (
            q(t["ma"]), q(t["ten"]), q(t["o"]), t["gia_luong"], 500 + k,
            q(t["mo_ta"] + " Chỉ đổi hình: chỉ số theo ô, như món miễn phí.")))
    # INSERT IGNORE: nap_cua_hang la bang van hanh (gia / dang_ban chinh tren web) - khong ghi de khi chay lai
    w("INSERT IGNORE INTO `nap_cua_hang` (`ma`,`ten`,`nhom`,`trao`,`gia_luong`,`han_ngay`,`gioi_han`,`nhom_xoay`,"
      "`giao_dich_duoc`,`co_chi_so`,`dang_ban`,`thu_tu`,`mo_ta`) VALUES\n" + ",\n".join(rows) + ";\n")
    return "".join(o)


def fmt_pt(pn):
    return ("%d" % (pn // 10)) if pn % 10 == 0 else ("%d,%d" % (pn // 10, pn % 10))


# ================================================================ xem trước / bộ sưu tập

def bo_suu_tap(thu_muc, ds_do, anh_do, ds_tt, anh_tt, atlas, khung):
    os.makedirs(thu_muc, exist_ok=True)
    k = 4
    NEN = 0x2C3446

    def luoi_anh(ds_anh, cot, ten_tep, nhan=None):
        o = 18
        hang = (len(ds_anh) + cot - 1) // cot
        an = anh_moi(cot * o * k, hang * o * k, 0xFF000000 | NEN)
        for i, a in enumerate(ds_anh):
            x, y = (i % cot) * o * k + k, (i // cot) * o * k + k
            dan(an, phong(a, k), x, y)
            if nhan:
                viet_so(an, nhan[i], x, y + 16 * k - 4, 0xFFFFFF, 1)
        ghi_png(os.path.join(thu_muc, ten_tep), an)

    # bộ hệ: mỗi hệ một bảng 6 ô x 5 bậc
    for he, ma, ten, poke, khac in DM.BO_HE:
        ds = [anh_do[d["id"]] for d in ds_do if d.get("loai") == "he" and d["he"] == he]
        luoi_anh(ds, 5, "bo_he_%02d_%s.png" % (he, ma))
    tong_he = [anh_do[d["id"]] for d in ds_do if d["loai"] == "he" and d["bac"] == 8]
    luoi_anh(tong_he, 18, "bo_he_tat_ca_bac8.png")
    vung = [anh_do[d["id"]] for d in ds_do if d["loai"] == "vung"]
    luoi_anh(vung, 8, "bo_vung_11_bac.png")
    luoi_anh([anh_tt[t["ma"]] for t in ds_tt], 8, "trang_tri_icon.png")
    # khung độ hiếm áp lên icon mẫu
    mau = [anh_do[ds_do[i]["id"]] for i in (3, 64, 130, 200, 300, 400)]
    ds = []
    for r in range(len(KHUNG_HIEM)):
        for f in range(2):
            c = anh_moi(20, 20)
            dan(c, [20, 20, [row[f * 20:f * 20 + 20] for row in khung[2][r * 20:r * 20 + 20]]], 0, 0)
            dan(c, mau[r], 2, 2)
            ds.append(c)
    o = 22
    an = anh_moi(12 * o * k, o * k, 0xFF000000 | NEN)
    for i, a in enumerate(ds):
        dan(an, phong(a, k), i * o * k + k, k)
    ghi_png(os.path.join(thu_muc, "khung_do_hiem.png"), an)
    ghi_png(os.path.join(thu_muc, "atlas_trang_phuc.png"), phong(to_nen(atlas, NEN), 4))


def main():
    xem = None
    if "--xem" in sys.argv:
        xem = sys.argv[sys.argv.index("--xem") + 1]
    # dump day du da chua mon ho (sinh lan truoc, phan B -> xuat lai phan A): bo dai cua chinh tep nay
    mon = {k: v for k, v in doc_data_item().items() if not ID_ITEM_HE_DAU <= k <= ID_ITEM_HET}
    ds_do = danh_sach_do()
    for d in ds_do:
        assert d["id_goc"] in mon, "thieu mon goc %s" % d
        assert ID_ITEM_HE_DAU <= d["id"] <= ID_ITEM_HET
        assert d["id"] not in mon, "id %d da co trong data_item" % d["id"]
    assert len({d["id"] for d in ds_do}) == len(ds_do)
    ds_tt = danh_sach_trang_tri()
    # ---- icon
    ra_icon = os.path.join(GOC, "server", "map", "icon_do_moi")
    ra_web = os.path.join(GOC, "web", "public", "icon-do")
    for t in (ra_icon, ra_web):
        os.makedirs(t, exist_ok=True)
        for f in os.listdir(t):
            if f.endswith(".png"):
                os.remove(os.path.join(t, f))
    n = 0
    icon_do_id, anh_do, icon_tt_id, anh_tt = {}, {}, {}, {}
    bang_icon = []
    for d in ds_do:
        a = icon_do(d)
        anh_do[d["id"]] = a
        icon_do_id[d["id"]] = ID_ANH_DAU + n
        for t in (ra_icon, ra_web):
            ghi_png(os.path.join(t, "%d.png" % n), a)
        bang_icon.append((n, ID_ANH_DAU + n, "item:%d" % d["id"], d["ten"]))
        n += 1
    for t in ds_tt:
        if t["o"] == "trang_phuc":
            a = icon_trang_phuc(t)
        elif t["o"] == "hao_quang":
            a = icon_hao_quang([h for h in HP.HAO_QUANG if DM.MA_CU.get("hq_" + h[1], "hq_" + h[1]) == t["ma"]][0])
        elif t["o"] == "skin_thu":
            a = icon_yen([s for s in HP.SKIN_THU if DM.MA_CU.get("st_" + s[1], "st_" + s[1]) == t["ma"]][0])
        else:
            a = icon_the([s for s in HP.THE_DONG_HANH if "th_" + s[1] == t["ma"]][0])
        anh_tt[t["ma"]] = a
        icon_tt_id[t["ma"]] = ID_ANH_DAU + n
        for tm in (ra_icon, ra_web):
            ghi_png(os.path.join(tm, "%d.png" % n), a)
        bang_icon.append((n, ID_ANH_DAU + n, t["ma"], t["ten"]))
        n += 1
    # ---- atlas + khung
    atlas, bin_ = lam_atlas(ds_tt)
    khung = khung_hiem()
    for tm in [os.path.join(GOC, "client_jar", r, "pk") for r in ("res", "res_nhe")
               if os.path.isdir(os.path.join(GOC, "client_jar", r, "pk"))]:   # bản nhẹ J2ME: chỉ hào quang + khung (không neo)
        ghi_png(os.path.join(tm, "trang_phuc.png"), atlas)
        open(os.path.join(tm, "trang_phuc.bin"), "wb").write(bin_)
        ghi_png(os.path.join(tm, "khung_hiem.png"), khung)
    # ---- CSV
    with open(os.path.join(GOC, "du_lieu", "do_moi_trang_bi.csv"), "w", encoding="utf8", newline="") as f:
        g = csv.writer(f, lineterminator="\n")
        g.writerow(["id_item", "ten", "type", "bac", "ten_bac", "ma_ho", "loai", "he", "vung", "id_goc", "icon", "nguon"])
        for d in ds_do:
            g.writerow([d["id"], d["ten"], d["type"], d["bac"], DM.TEN_BAC[d["bac"]], d["ma_ho"], d["loai"],
                        DM.TEN_HE[d["he"]] if d["he"] >= 0 else "", d["vung"], d["id_goc"], icon_do_id[d["id"]],
                        d["nguon"]])
    with open(os.path.join(GOC, "du_lieu", "do_moi_bo.csv"), "w", encoding="utf8", newline="") as f:
        g = csv.writer(f, lineterminator="\n")
        g.writerow(["ma_ho", "ten", "loai", "so_mon", "chi_so", "phan_nghin"])
        for he, ma, ten, poke, khac in DM.BO_HE:
            for so, cs, pn in DM.THUONG_BO_HE:
                g.writerow(["he_" + ma, ten, "he", so, cs + ":" + (DM.TEN_HE[he] if cs == "st_he" else DM.TEN_HE[khac]), pn])
        for ma, ten, tu, den in DM.VUNG:
            for so, cs, pn in DM.THUONG_BO_VUNG:
                g.writerow(["vung_" + ma, ten, "vung", so, cs, pn])
    with open(os.path.join(GOC, "du_lieu", "trang_phuc.csv"), "w", encoding="utf8", newline="") as f:
        g = csv.writer(f, lineterminator="\n")
        g.writerow(["ma", "o", "so", "ten", "do_hiem", "icon", "manh", "nguon", "tham_so", "gia_luong", "mo_ta"])
        for t in ds_tt:
            g.writerow([t["ma"], t["o"], t["id"], t["ten"], t["do_hiem"], icon_tt_id[t["ma"]],
                        " ".join(str(m) for m in t["manh"]), t["nguon"], t["tham_so"], t["gia_luong"], t["mo_ta"]])
    # ---- SQL
    sql = sinh_sql(ds_do, ds_tt, mon, icon_do_id, icon_tt_id)
    # mot tep SQL (sql/README.md): thay khoi "-- >>> them_do_trang_phuc.sql" cua sql/htbb.sql toi khoi ke tiep / het tep
    nc_p = os.path.join(GOC, "sql", "htbb.sql")
    moc = "-- >>> them_do_trang_phuc.sql\n"
    with open(nc_p, encoding="utf8") as f:
        cu = f.read()
    a = cu.find("\n" + moc)
    if a < 0:
        sys.exit("sql/htbb.sql thieu dong moc " + moc.strip())
    b = cu.find("\n-- >>> ", a + 1)
    open(nc_p, "w", encoding="utf8", newline="\n").write(cu[:a + 1] + moc + sql + ("" if b < 0 else cu[b + 1:]))
    # ---- web JSON
    web = dict(
        sinh="tools/do_trang_phuc/sinh.py",
        idAnhDau=ID_ANH_DAU,
        bac=DM.TEN_BAC, capBac=DM.CAP_BAC, he=DM.TEN_HE,
        bo=[dict(ma="he_" + ma, ten=ten, loai="he", he=DM.TEN_HE[he], heKhac=DM.TEN_HE[khac], pokemon=poke,
                 thuong=[dict(soMon=so, chiSo=cs, phanNghin=pn) for so, cs, pn in DM.THUONG_BO_HE])
            for he, ma, ten, poke, khac in DM.BO_HE]
        + [dict(ma="vung_" + ma, ten=ten, loai="vung", vung=ma, bacTu=tu, bacDen=den,
                thuong=[dict(soMon=so, chiSo=cs, phanNghin=pn) for so, cs, pn in DM.THUONG_BO_VUNG])
           for ma, ten, tu, den in DM.VUNG],
        do=[dict(id=d["id"], ten=d["ten"], type=d["type"], o=d["o"], bac=d["bac"], maHo=d["ma_ho"],
                 icon=icon_do_id[d["id"]] - ID_ANH_DAU, nguon=d["nguon"].split(",")) for d in ds_do],
        oTrangTri=[dict(o=o_, ten=ten, phanNghin=pn, dong=[dict(ma=a, ten=b) for a, b in dong])
                   for o_, ten, pn, dong in DM.O_TRANG_TRI],
        nganSachDanhHieu=DM.NGAN_SACH_DANH_HIEU, tranTong=DM.TRAN_TONG_TRANG_TRI,
        trangTri=[dict(ma=t["ma"], o=t["o"], so=t["id"], ten=t["ten"], doHiem=t["do_hiem"],
                       icon=icon_tt_id[t["ma"]] - ID_ANH_DAU, nguon=t["nguon"], thamSo=t["tham_so"],
                       giaLuong=t["gia_luong"], moTa=t["mo_ta"]) for t in ds_tt],
    )
    with open(os.path.join(GOC, "web", "shared", "data", "danh-muc-do-trang-phuc.json"), "w", encoding="utf8") as f:
        json.dump(web, f, ensure_ascii=False, indent=1)
        f.write("\n")
    with open(os.path.join(GOC, "server", "map", "icon_do_moi", "GHI_CONG.txt"), "w", encoding="utf8") as f:
        f.write("Icon do moi / trang phuc HTBB (map/icon_do_moi/<n>.png, id anh 15000+n): tu ve cho du an bang luoi ky tu\n"
                "(tools/do_trang_phuc/hinh_trang_bi.py, hinh_trang_phuc.py), sinh bang tools/do_trang_phuc/sinh.py.\n"
                "Giay phep: CC0 (khong dung tai san ben ngoai). %d icon.\n" % n)
    if xem:
        bo_suu_tap(xem, ds_do, anh_do, ds_tt, anh_tt, atlas, khung)
    print("do moi: %d mon (%d bo he, %d bo vung), trang tri: %d mon, icon: %d (%d..%d), atlas %dx%d, bin %d byte"
          % (len(ds_do), len(DM.BO_HE), len(DM.VUNG), len(ds_tt), n, ID_ANH_DAU, ID_ANH_DAU + n - 1, atlas[0], atlas[1],
             len(bin_)))


if __name__ == "__main__":
    main()
