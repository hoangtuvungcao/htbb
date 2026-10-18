#!/usr/bin/env python3
"""Kiem vat pham MO COI / TREO / KHONG LAY DUOC cho Huyen Thoai Bao Boi (chi thu vien chuan, tat dinh).

Luat cua chu du an: "cac item khong bi mo coi, deu su dung vao 1 viec nao do". Moi vat pham CO THE TON
TAI (co it nhat mot NGUON dang chay) phai co it nhat mot CONG DUNG that (mac/dung co hieu ung/nguyen lieu
che tao - nang cap/nop nhiem vu/doi o NPC/trang tri/suu tap). Ban cho NPC chi la duong lui (ghi "chi ban").

Doc (khong ket noi CSDL):
  sql/htbb_day_du.sql            data_item, data_potion, data_shop (trang thai seed)
  sql/pokemon_noi_dung.sql       data_item 1001..1199 (de len ban seed, giong thu tu xuat_sql_day_du.sh)
  sql/pokemon_vat_pham_lau_dai.sql  data_item 1201..1260 + data_vat_pham_lau_dai + data_cua_hang_lau_dai
  sql/pokemon_doi_chu.sql        UPDATE ten (data_item/data_potion)
  sql/pokemon_lau_dai.sql        data_quest_moi (chuoi chinh, engine cu), data_shop_gia_lau_dai
  sql/pokemon_nhiem_vu.sql       data_vat_pham_nhiem_vu (vat pham nhiem vu 10..50)
  docs/pokemon/noi_dung_roi.csv, nhiem_vu_moi.csv
  src/**.java                    ngoc (MapNapTaiNguyen), vat lieu he sinh thai (VatLieuSinhThai/KyNangSong/
                                 NhaTre), cong thuc, Pokedex + QUET so lieu cung trong code (nguon/cong dung)
Moi co che viet tay o bang QUY_TAC co "mau" (chuoi phai con trong file nguon) - code doi ma quy tac
khong doi thi script bao "QUY TAC LOI THOI" va test fail, de bang khong noi doi.

Ra: docs/pokemon/KIEM_ITEM.md (ma tran vat pham -> nguon -> cong dung + danh sach loi theo lop) va
    docs/pokemon/kiem_item.csv (ma tran day du). Ma thoat = so loi (0 = sach) khi chay voi --kiem.

Cau hinh danh gia (co server.ini):
  A: sv.lau_dai=1, sv.he_sinh_thai=0   B: sv.lau_dai=1, sv.he_sinh_thai=1   (KIEM - phai 0 loi)
  C: sv.lau_dai=0 (game cu - chi bao cao, khong kiem)
  Ca 3: sv.che_do_pokemon=1, sv.nap_tien=0, su kien theo lich (SK=1).

Chay o thu muc HTBB/:  python3 -I tools/pokemon/kiem_item_mo_coi.py [--kiem]
"""
import csv
import glob
import hashlib
import os
import re
import sys
import unicodedata

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SQL = os.path.join(GOC, "sql")
DOC = os.path.join(GOC, "docs", "pokemon")
SRC = os.path.join(GOC, "server", "src")
RA_MD = os.path.join(DOC, "KIEM_ITEM.md")
RA_CSV = os.path.join(DOC, "kiem_item.csv")

CAU_HINH = [
    ("A", {"LD": 1, "HST": 0, "NAP": 0, "CDP": 1, "SK": 1}, True),
    ("B", {"LD": 1, "HST": 1, "NAP": 0, "CDP": 1, "SK": 1}, True),
    ("C", {"LD": 0, "HST": 0, "NAP": 0, "CDP": 1, "SK": 1}, False),
]

# =====================================================================================================
# Doc SQL (kieu mysqldump va kieu INSERT co danh sach cot)
# =====================================================================================================


def _bo_thoat(s):
    ra, i = [], 0
    while i < len(s):
        c = s[i]
        if c == "\\" and i + 1 < len(s):
            ra.append({"n": "\n", "r": "\r", "t": "\t", "0": "\0", "Z": "\x1a"}.get(s[i + 1], s[i + 1]))
            i += 2
            continue
        if c == "'" and s[i + 1:i + 2] == "'":
            ra.append("'")
            i += 2
            continue
        ra.append(c)
        i += 1
    return "".join(ra)


def _gia_tri(s, i):
    while s[i] in " \n\r\t":
        i += 1
    if s[i] == "'":
        j = i + 1
        while True:
            c = s[j]
            if c == "\\":
                j += 2
                continue
            if c == "'":
                if s[j + 1:j + 2] == "'":
                    j += 2
                    continue
                break
            j += 1
        return _bo_thoat(s[i + 1:j]), j + 1
    j = i
    while s[j] not in ",)":
        j += 1
    v = s[i:j].strip()
    return (None if v.upper() == "NULL" else v), j


def doc_insert(s, bang, cot_mac_dinh=None):
    """Moi dong INSERT INTO `bang` [(cot)] VALUES (...),(...); -> list dict."""
    ra = []
    for m in re.finditer(r"INSERT INTO `%s`\s*(\(([^)]*)\))?\s*VALUES\s*" % re.escape(bang), s):
        cot = [c.strip(" `") for c in m.group(2).split(",")] if m.group(2) else cot_mac_dinh
        i = m.end()
        while True:
            while s[i] in " \n\r\t":
                i += 1
            if s[i] != "(":
                break
            i += 1
            gt = []
            while True:
                v, i = _gia_tri(s, i)
                gt.append(v)
                while s[i] in " \n\r\t":
                    i += 1
                if s[i] == ",":
                    i += 1
                    continue
                i += 1
                break
            ra.append(dict(zip(cot, gt)) if cot else gt)
            while s[i] in " \n\r\t":
                i += 1
            if s[i] == ",":
                i += 1
                continue
            break
    return ra


def cot_bang(s, bang):
    m = re.search(r"CREATE TABLE `%s` \((.*?)\n\)" % re.escape(bang), s, re.S)
    if not m:
        return None
    return re.findall(r"^\s*`(\w+)`", m.group(1), re.M)


def doc_update_ten(s, bang):
    """UPDATE bang SET name='...' WHERE id=N; -> {id: ten}."""
    ra = {}
    for m in re.finditer(r"UPDATE\s+`?%s`?\s+SET\s+`?name`?\s*=\s*'((?:[^'\\]|\\.|'')*)'\s+WHERE\s+`?id`?\s*=\s*(\d+)\s*;"
                         % re.escape(bang), s):
        ra[int(m.group(2))] = _bo_thoat(m.group(1))
    return ra


def doc(p):
    return open(p, encoding="utf-8", errors="replace").read()


def ten_dong1(t):
    return (t or "").replace("\r", "").split("\n")[0].strip()


def chuan(t):
    t = unicodedata.normalize("NFC", ten_dong1(t)).lower()
    t = re.sub(r"[.\s]+$", "", t)
    return re.sub(r"\s+", " ", t)


# =====================================================================================================
# Vu tru vat pham
# =====================================================================================================

class VP:
    __slots__ = ("khoa", "loai", "id", "ten", "hien", "type", "cap", "gia", "clazz", "nloan", "nguon", "dung", "ghi", "nhom")

    def __init__(self, loai, id_, ten, type_=-1, cap=0, gia=0, clazz=-1, nloan=0, nhom=""):
        self.loai, self.id, self.ten = loai, id_, ten
        self.hien = ten
        self.khoa = "%s:%s" % (loai, id_)
        self.type, self.cap, self.gia, self.clazz, self.nloan = type_, cap, gia, clazz, nloan
        self.nguon, self.dung, self.ghi = [], [], []
        self.nhom = nhom


class DuLieu:
    def __init__(self):
        self.vp = {}           # khoa -> VP
        self.treo = []         # (tham chieu, noi, ghi chu)
        self.quy_tac_loi = []  # quy tac co mau khong con trong file
        self.nguon_ao = []     # (khoa, noi, ly do) nguon khai bao nhung khong chay
        self.thong_tin = {}
        self.java = {}         # duong dan tuong doi -> noi dung

    def them(self, v):
        self.vp[v.khoa] = v

    def lay(self, khoa):
        return self.vp.get(khoa)

    def nguon(self, khoa, co_che, noi, cong="True", noi_tham_chieu=None):
        v = self.vp.get(khoa)
        if v is None:
            self.treo.append((khoa, noi_tham_chieu or noi, "nguồn: " + co_che))
            return
        v.nguon.append((co_che, noi, cong))

    def dung(self, khoa, co_che, noi, cong="True", manh=True, noi_tham_chieu=None, bat_buoc=False):
        v = self.vp.get(khoa)
        if v is None:
            self.treo.append((khoa, noi_tham_chieu or noi, "công dụng: " + co_che))
            return
        v.dung.append((co_che, noi, cong, manh, bat_buoc))


def nap_vu_tru(d):
    dump = doc(os.path.join(SQL, "htbb_day_du.sql"))
    d.thong_tin["dump"] = "sql/htbb_day_du.sql"
    cot_item = cot_bang(dump, "data_item")
    item = {}
    for r in doc_insert(dump, "data_item", cot_item):
        item[int(r["id"])] = r
    # cac file sinh sau ban dump (giong thu tu tools/pokemon/xuat_sql_day_du.sh): DELETE dai + INSERT
    for f in ("pokemon_noi_dung.sql", "pokemon_vat_pham_lau_dai.sql"):
        s = doc(os.path.join(SQL, f))
        for m in re.finditer(r"DELETE FROM `data_item` WHERE `id` >= (\d+) AND `id` <= (\d+)", s):
            for i in range(int(m.group(1)), int(m.group(2)) + 1):
                item.pop(i, None)
        for r in doc_insert(s, "data_item", cot_item):
            item[int(r["id"])] = r
    doi = doc(os.path.join(SQL, "pokemon_doi_chu.sql"))
    for i, t in doc_update_ten(doi, "data_item").items():
        if i in item and i < 1001:
            item[i]["name"] = t
    for i, r in item.items():
        d.them(VP("item", i, r["name"] or "", int(r["type"]), int(r["level"] or 0), int(r["price"] or 0),
                  int(r["clazz"] or -1), int(r["nloan"] or 0)))
    d.item_dong = item
    # thuoc (data_potion: dung 162 dong, Char.SO_O_THUOC)
    cot_p = cot_bang(dump, "data_potion")
    thuoc = {int(r["id"]): r for r in doc_insert(dump, "data_potion", cot_p)}
    for i, t in doc_update_ten(doi, "data_potion").items():
        if i in thuoc:
            thuoc[i]["name"] = t
    for i, r in thuoc.items():
        d.them(VP("thuoc", i, r["name"] or "", gia=int(r["price"] or 0)))
    d.thuoc_dong = thuoc
    d.thong_tin["so_thuoc"] = len(thuoc)
    # ngoc (MapNapTaiNguyen.loadGemTemplate - khoi ban Viet, khong phai Indo)
    nap_ngoc(d)
    # vat pham nhiem vu (ItemQuest): 0..9 trong jar (chi cac ten dung duoc), 10..50 data_vat_pham_nhiem_vu
    nv = doc(os.path.join(SQL, "pokemon_nhiem_vu.sql"))
    for i, t in {0: "Nọc bọ cạp", 2: "Khúc gỗ", 3: "Rết đỏ", 4: "Lá thư", 6: "Lông đuôi Hồng Kê", 7: "Móng cá sấu",
                 8: "Da ếch", 9: "Cựa gà"}.items():
        d.them(VP("nv", i, t, nhom="jar"))
    for r in doc_insert(nv, "data_vat_pham_nhiem_vu", ["id", "ten"]):
        d.them(VP("nv", int(r["id"]), r["ten"]))
    # vat lieu he sinh thai (Tui sinh thai, khong phai data_item)
    vl = d.java_doc("server/src/com/htbb/pokemon/VatLieuSinhThai.java")
    for m in re.finditer(r'dn\((\d+), "([^"]*)", (L_\w+), (\d+)\);', vl):
        d.them(VP("hst", int(m.group(1)), m.group(2), nhom=m.group(3), gia=int(m.group(4))))


def _tach_doi_so(s, i):
    """s[i] ngay sau '(' -> (danh sach doi so cap tren cung, vi tri sau ')')."""
    sau, ds, cur, q = 1, [], [], None
    while i < len(s):
        c = s[i]
        if q:
            cur.append(c)
            if c == "\\":
                cur.append(s[i + 1])
                i += 2
                continue
            if c == q:
                q = None
        elif c in "\"'":
            q = c
            cur.append(c)
        elif c in "({[":
            sau += 1
            cur.append(c)
        elif c in ")}]":
            sau -= 1
            if sau == 0:
                ds.append("".join(cur).strip())
                return ds, i + 1
            cur.append(c)
        elif c == "," and sau == 1:
            ds.append("".join(cur).strip())
            cur = []
        else:
            cur.append(c)
        i += 1
    return ds, i


def nap_ngoc(d):
    text = d.java_doc("server/src/data/Text.java")
    ten = {}
    for m in re.finditer(r"Text\.(\w+) = \"((?:[^\"\\]|\\.)*)\";", text):
        ten.setdefault(m.group(1), m.group(2))

    def bieu_thuc(e):
        ra = []
        for p in e.split("+"):
            p = p.strip()
            if p.startswith("Text."):
                ra.append(ten.get(p[5:], p))
            elif p.startswith('"'):
                ra.append(p.strip('"'))
            else:
                ra.append(p)
        return "".join(ra)

    s = d.java_doc("server/src/real/MapNapTaiNguyen.java")
    i0 = s.index("static void loadGemTemplate()")
    i1 = s.index("Map.gemTemplate = new GemTemplate[]{", s.index("} else {", i0))
    i2 = s.index("};\n", i1)
    i = i1
    while True:
        j = s.find("new GemTemplate(", i, i2)
        if j < 0:
            break
        ds, i = _tach_doi_so(s, j + len("new GemTemplate("))
        gid = int(ds[0]) if ds[0].isdigit() else {"Map.SACH_SKILL_PET_GEM": 269}.get(ds[0], -1)
        if gid < 0:
            continue
        if len(ds) == 10:
            kieu, sell = ds[7], ds[8]
        else:
            kieu, sell = ds[6], ds[8]
        kieu = kieu.replace("GemTemplate.", "")
        v = VP("ngoc", gid, bieu_thuc(ds[2]), nhom=kieu)
        v.gia = 1 if sell.strip() == "1" else 0
        d.them(v)
    d.thong_tin["so_ngoc"] = sum(1 for x in d.vp.values() if x.loai == "ngoc")


def _java_doc(self, rel):
    if rel not in self.java:
        p = os.path.join(GOC, rel)
        self.java[rel] = doc(p) if os.path.exists(p) else ""
    return self.java[rel]


DuLieu.java_doc = _java_doc


# =====================================================================================================
# Quy tac viet tay (co mau kiem trong file). cong: bieu thuc tren LD/HST/NAP/CDP/SK.
# kieu: nguon / dung (manh) / ban (chi ban NPC) / can (nguyen lieu bat buoc: thieu nguon = khong lay duoc)
# =====================================================================================================

def R(kieu, khoa, co_che, file, mau, cong="True"):
    return (kieu, khoa, co_che, file, mau, cong)


def dai(a, b):
    return list(range(a, b + 1))


QUY_TAC = [
    # ---------------- data_item: cong dung theo type (Map.doUseItem / MapMuaBan.exchangeItem)
    R("dung_type", list(range(0, 20)), "mặc trang bị (doUseItem → MapMuaBan.exchangeItem)", "server/src/real/Map.java",
      "MapMuaBan.exchangeItem(this, player, item, i);"),
    R("dung_type", [20], "rương giao hàng (doResponseTieu)", "server/src/real/Map.java", "player.doResponseTieu(player, true"),
    R("dung_type", [21], "gọi Pokémon hỗ trợ (doUseItemLinhDanhThue)", "server/src/real/Map.java", "this.doUseItemLinhDanhThue(player);"),
    R("dung_type", [22], "thẻ mua bán (dịch chuyển chợ)", "server/src/real/Map.java", "isTheBaiMuaban(t)"),
    R("dung_type", [23], "vé xe giao hàng (upSpeedNguaVanTieu)", "server/src/real/Map.java", "player.upSpeedNguaVanTieu = 2;"),
    R("dung_type", [25], "thú cưỡi Hoả kỳ lân (menu -513 nâng cấp / ảo hoá)", "server/src/real/Map.java", "isItemThanThu(t)"),
    R("dung_type", [26], "ấp trứng ra pet (doCreatePet)", "server/src/real/Map.java", "player.doCreatePet(item);"),
    R("dung_type", [27], "pet đi theo (trang bị thú)", "server/src/real/Map.java", "isWearingAnimal(item.getTemplate().type)"),
    R("dung_type", [29], "trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh)", "server/src/real/Item.java", "isManhSuuTap"),
    R("dung_type", [30], "skin linh thú (SkinThuCuoi)", "server/src/real/Map.java", "SkinThuCuoi.laItemSkin(item.getTemplate().id)"),
    # ---------------- data_item type 28 / dac biet: cong dung theo id
    R("dung", [714], "cho thú nuôi ăn (Kẹo Poké, Item.getExpPetEatfromItem)", "server/src/real/Item.java", "isTuHonDan"),
    R("dung", [720], "tiến hoá trạng thái thú nuôi (isHuyetLinhThao → Pet.doTienHoa)", "server/src/real/Map.java", "item.isHuyetLinhThao()"),
    R("dung", [721], "đổi độ thông thạo thú nuôi (isHuyetBoDe)", "server/src/real/Map.java", "item.isHuyetBoDe()"),
    R("dung", [729], "chổi lửa biến hình (isChoiLuaLanBienHinh)", "server/src/real/Map.java", "item.isChoiLuaLanBienHinh()"),
    R("dung", [735], "mở rương may mắn (doOpenRuongMayMan)", "server/src/real/Map.java", "doOpenRuongMayMan(player, item);"),
    R("dung", [752, 753], "mở rương nguyên liệu (ngọc nguyên liệu khoá)", "server/src/real/Map.java", "item.isRuongNguyenLieu()"),
    R("dung", [835], "vũ khí thời trang (isVukhiThoiTrang)", "server/src/real/Map.java", "item.isVukhiThoiTrang()"),
    R("dung", [887], "trứng rồng băng: mang tới NPC tạo thú", "server/src/real/Map.java", "item.isTrungRongBang()"),
    R("dung", dai(890, 892), "thần khí (isThanKhi, mặc)", "server/src/real/Map.java", "item.isThanKhi()"),
    R("dung", dai(960, 967), "đá tiến hoá Eevee (TienHoaPokemon.dungDaTienHoa)", "server/src/real/Map.java",
      "TienHoaPokemon.dungDaTienHoa(player, item)", "CDP"),
    R("dung", dai(1201, 1260), "dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai)", "server/src/real/Map.java",
      "VatPhamDungLauDai.dung(player, item)", "LD"),
    R("can", dai(1034, 1037), "nguyên liệu phụ nâng cấp +11..+15 (NangCapLauDaiGame.thu)", "server/src/real/NangCapLauDaiGame.java",
      "VatPhamDungLauDai.truItem", "LD"),
    R("can", [1038], "Mảnh tinh hoa: nâng cấp +11..+15 đồ Hoàn mỹ (NangCapLauDaiGame)", "server/src/real/NangCapLauDaiGame.java",
      "Mảnh tinh hoa", "LD"),
    R("dung", [1258, 1259], "bùa bảo hộ khi nâng cấp thất bại (NangCapLauDaiGame.buaChoMuc)", "server/src/real/NangCapLauDaiGame.java",
      "buaChoMuc", "LD"),
    R("ban", "item:ban", "bán cho NPC (OfflineMap.doSellItem)", "server/src/real/OfflineMap.java", "doSellItem"),
    # ---------------- data_item: nguon theo luat
    R("nguon_luat", "roi_cu", "rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2)",
      "server/src/real/Map.java", "isIdTemplateItemDrop"),
    R("nguon_luat", "boss_thu", "boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal)", "server/src/real/Monster.java",
      "dropItemAnimal"),
    R("nguon_luat", "npc_ban", "NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem)",
      "server/src/real/MessageCreator.java", "ALL_ID_ITEM_NPC_SELL = new int[]{"),
    R("nguon", dai(611, 612), "vận tiêu (MonsterVantieu trong jar)", "server/src/real/Monster.java", "isMonsterVantieu"),
    R("nguon", dai(678, 680), "quái vận tiêu / tiến giai 1% (MapCheTao.doAddNewAttBot)", "server/src/real/MapCheTao.java", "678"),
    R("nguon", [684, 685, 686, 687, 688], "trứng pet rơi sự kiện hè / boss (Monster.droptrungPet)", "server/src/real/Monster.java",
      "droptrungPet", "SK"),
    R("nguon", [631, 632, 634, 635, 637, 638], "hộp/boss sự kiện (HoVeLenh)", "server/src/real/BossTrungThu.java", "HoVeLenh", "SK"),
    R("nguon", dai(715, 719), "boss Người Tuyết / pháo hoa Tết (vũ khí thời trang có hạn)", "server/src/real/BossNguoiTuyet.java",
      "715", "SK"),
    R("nguon", [720, 721], "rương may mắn / boss (Huyết linh thảo 720, sách kỹ năng 721)", "server/src/real/Monster.java",
      "doCreateBookSkillPet"),
    R("nguon", dai(584, 597), "hộp quà Halloween / World Cup (createItemCoat có hạn)", "server/src/real/SuKienWorldCup.java",
      "createItemCoat", "SK"),
    R("nguon", dai(848, 859) + dai(898, 902), "quay số / giftcode / boss Trung thu (mảnh, cánh)",
      "server/src/real/CharSuKien.java", "loadAllItemLucky", "SK"),
    R("nguon", dai(860, 871) + [903, 904], "ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap)",
      "server/src/real/Char.java", "createItemSuuTap"),
    R("nguon", dai(890, 892), "Thợ rèn bí ẩn chế thần khí (ThoRenBiAn.CreateThanKhi)", "server/src/BaoVatHuyenThoai/ThoRenBiAn.java",
      "CreateThanKhi"),
    R("nguon", [681], "nở trứng Ponyta (doCreateHoaKyLan / NhaTre)", "server/src/real/doUsePotion.java", "doCreateHoaKyLan"),
    R("nguon", dai(682, 683), "nâng cấp Hoả kỳ lân (doNangCapHoaKyLan)", "server/src/real/Char.java", "doNangCapHoaKyLan"),
    R("nguon", dai(689, 713), "nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel)", "server/src/data/Pet.java",
      "doUpdrage"),
    R("nguon", [675], "thẻ mua bán: NPC bán + thành tựu VIP", "server/src/real/MapCheTao.java", "ID_THE_MUA_BAN"),
    R("dung", dai(678, 680), "lệnh bài vào Tháp Pokémon (Char.getLenhBaiTranYeu)", "server/src/real/Char.java", "getLenhBaiTranYeu"),
    R("nguon", [676, 677], "boss Sơn Tinh / hộp quà sự kiện", "server/src/real/Monster.java", "676", "SK"),
    R("nguon", dai(736, 737) + dai(740, 741) + dai(743, 744) + [754, 755, 791, 792], "cửa hàng lượng (data_shop slot thời trang)",
      "server/src/real/doBuySpecialItem.java", "736"),
    R("nguon", dai(745, 750) + [742, 872], "hộp quà Noel / Halloween / Vu Lan (mặt nạ, phi phong)", "server/src/real/CharVongLap.java",
      "doOpenTuiQuaMayManNoel", "SK"),
    R("nguon", [752, 753], "Shop Đặc Biệt (VIP)", "server/src/real/MapNapTaiNguyen.java", "751", "True"),
    R("nguon", [723, 724], "Shop Đặc Biệt (VIP, 3 giờ)", "server/src/real/MapNapTaiNguyen.java", "723"),
    R("nguon", dai(616, 619), "quay số / sự kiện 5 sao (chổi)", "server/src/real/CharSuKien.java", "loadAllItemLucky", "SK"),
    R("nguon", dai(905, 909) + [911], "đổi điểm thưởng (MapQuaTang case 15..19) / giftcode", "server/src/real/MapQuaTang.java",
      "traoVatPhamDoiThuong"),
    R("nguon", [714], "Kẹo Poké: cửa hàng thú nuôi / hộp", "server/src/real/Item.java", "isTuHonDan"),
    R("nguon", dai(960, 967), "Pokédex mốc 50/100 (đá ngẫu nhiên)", "server/src/com/htbb/pokemon/Pokedex.java", "ITEM:\" + da",
      "CDP"),
    R("nguon", dai(960, 967), "rơi đá tiến hoá khi hạ quái (RoiDaTienHoa.khiHaQuai)", "server/src/real/Monster.java",
      "RoiDaTienHoa.khiHaQuai", "CDP"),
]

# Ngoc: cong dung theo kieu (GemTemplate.TYPE_*) - xem MapEpNgoc/MapCheTao/OfflineMap/ItemChangeColor
DUNG_NGOC_THEO_KIEU = {
    "TYPE_IMBUE": "nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc",
    "TYPE_KHAM": "khảm vào trang bị (MapEpNgoc.doFinishKhamItem)",
    "TYPE_DUC_LO": "đục lỗ khảm (MapEpNgoc.doFinishKhamItem)",
    "TYPE_CREATE_ITEM": "nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage)",
    "TYPE_TU_BINH": "ghép bộ Tứ bình (tob_tubinh)",
    "TYPE_HOP_ITEM_ANIMAL": "đổi màu đồ thú (ItemChangeColor) / thần khí",
    "TYPE_ADD_NEW_ATTRIBUTE": "thêm thuộc tính (Map.doAddNewAttribute)",
    "TYPE_LOCK_ANIMAL": "khoá đồ thú (OfflineMap)",
    "TYPE_LOCK_ITEM": "khoá trang bị (OfflineMap)",
    "TYPE_BOT": "tiến giai (MapCheTao.doAddNewAttBot)",
    "TYPE_DA_TIEN_GIAI": "tiến giai (MapCheTao.doAddNewAttBot)",
}
DUNG_NGOC_ID = [  # (dai id, co che, file, mau)
    (dai(175, 178), "tách nguyên liệu (MapEpNgoc.doTachNguyenLieu)", "server/src/real/MapEpNgoc.java", "doTachNguyenLieu"),
    (dai(258, 259), "Hoả thạch: nâng cấp thần thú / thần khí", "server/src/BaoVatHuyenThoai/ThoRenBiAn.java", "259"),
    (dai(260, 267), "Ngọc 1-7 sao / bản đồ kho báu (MapMenuNpc)", "server/src/real/MapMenuNpc.java", "NGOC_"),
    ([268], "Thuốc hoá hình pet (RealController)", "server/src/real/RealController.java", "268"),
]


# =====================================================================================================
# Quet code: so lieu cung (nguon / tieu thu)
# =====================================================================================================

ID = r"(\d+|GemTemplate\.[A-Z_0-9]+|Potion\.[A-Z_0-9]+|Item\.[A-Z_0-9]+|Map\.[A-Z_0-9]+)"
QUET = [
    # (ma, regex, loai khoa, vai tro)
    ("createItemSoLuong", re.compile(r"createItemSoLuong\(\s*" + ID + r"\s*,"), "item", "nguon"),
    ("dropItemByIdTemplate", re.compile(r"dropItemByIdTemplate\([^,()]*,\s*" + ID + r"\s*\)"), "item", "nguon"),
    ("doAddGemItem", re.compile(r"doAddGemItem\(\s*" + ID + r"\s*,"), "ngoc", "nguon"),
    ("potions[N]+=", re.compile(r"potions\[\s*" + ID + r"\s*\]\s*(?:\+=|\+\+)"), "thuoc", "nguon"),
    ("potions[N] = potions[N] +", re.compile(r"potions\[\s*" + ID + r"\s*\]\s*=\s*\w+\.potions\[\s*\1\s*\]\s*\+"), "thuoc",
     "nguon"),
    ("new Potion", re.compile(r"new Potion\(\s*\(short\)\s*" + ID + r"\s*,"), "thuoc", "nguon"),
    ("potions[N]-=", re.compile(r"potions\[\s*" + ID + r"\s*\]\s*(?:-=|--)"), "thuoc", "dung"),
    ("potions[N] = potions[N] -", re.compile(r"potions\[\s*" + ID + r"\s*\]\s*=\s*\w+\.potions\[\s*\1\s*\]\s*-"), "thuoc",
     "dung"),
    ("listGemitem[N]-=", re.compile(r"listGemitem(?:Lock)?\[\s*" + ID + r"\s*\]\s*(?:-=|--)"), "ngoc", "dung"),
    ("removeGem", re.compile(r"removeGem(?:Item|Sell)?\(\s*" + ID + r"\s*,"), "ngoc", "dung"),
    ("kiểm số thuốc (đổi / nộp)", re.compile(r"potions\[\s*" + ID + r"\s*\]\s*(?:<|>=|<=)\s*[\w(]"), "thuoc", "dung_kiem"),
    ("kiểm số ngọc (đổi / nộp)", re.compile(r"listGemitem(?:Lock)?\[\s*" + ID + r"\s*\]\s*(?:<|>=|<=)\s*[\w(]"), "ngoc",
     "dung_kiem"),
    ("kiểm id vật phẩm", re.compile(r"(?:getTemplate\(\)\.id|tempateID|idTemplate|getIdTemplate\(\))\s*==\s*" + ID + r"\b"),
     "item", "dung_kiem"),
]
CHUOI_THUONG = re.compile(r"\b(ITEM|GEM|PT):(\d+)(?::(\d+))?")

# file -> cong (mac dinh "True"). Ten file khop dau.
CONG_FILE = [
    ("server/src/real/BossLauDai.java", "LD"), ("server/src/real/GymLauDai.java", "LD"), ("server/src/real/RoiLauDai.java", "LD"),
    ("server/src/real/NoiDungLauDai.java", "LD"), ("server/src/real/QuaHoatDong.java", "LD"), ("server/src/real/SoTayLauDai.java", "LD"),
    ("server/src/real/HamNgucGame.java", "LD"), ("server/src/real/ChiemGymLauDai.java", "LD"), ("server/src/real/NhiemVuEngine.java", "LD"),
    ("server/src/real/CuaHangLauDai.java", "LD"), ("server/src/real/VatPhamDungLauDai.java", "LD"), ("server/src/real/NangCapLauDaiGame.java", "LD"),
    ("server/src/real/GiftcodeAnToan.java", "False"),  # giftcode = quan tri phat tay, khong tinh la nguon
    ("server/src/real/GiftcodeLauDai.java", "False"),
    ("server/src/com/htbb/pokemon/HeSinhThai", "HST"), ("server/src/com/htbb/pokemon/KyNangSong.java", "HST"),
    ("server/src/com/htbb/pokemon/NhaTre.java", "HST"), ("server/src/com/htbb/pokemon/MiniGameSinhThai.java", "HST"),
    ("server/src/com/htbb/pokemon/ChoSinhThai.java", "HST"), ("server/src/com/htbb/pokemon/SuuTamSinhThai.java", "HST"),
    ("server/src/com/htbb/pokemon/Pokedex.java", "CDP"), ("server/src/real/PokedexPokemon.java", "CDP"),
    ("server/src/com/htbb/pokemon/NapTien", "NAP"), ("server/src/real/NapTien", "NAP"),
    ("server/src/real/MapSuKien.java", "SK"), ("server/src/real/CharSuKien.java", "SK"), ("server/src/real/SuKienWorldCup.java", "SK"),
    ("server/src/real/BossTrungThu.java", "SK"), ("server/src/real/BossNguoiTuyet.java", "SK"), ("server/src/real/CharVongLap.java", "SK"),
    ("server/src/data/DatabaseGiftCode.java", "False"),  # chuoi trong giftcode cu (khong doc khi LD) / qua tich luy (tat)
    ("server/src/real/plugins/GiftCode.java", "False"),
]
# Ham khong ai goi / da tat (da grep: khong co noi goi trong src; xem bao cao kiem toan).
HAM_CHET = {
    "doOpenHotMutTo", "doOpenRuongDracula", "doOpenRuongMa", "doOpenSnake", "doOpenDun", "doHopQuaNoelCayThong",
    "doOpenHopBac", "doOpenHopGo", "doOpenHopVang", "doOpenHopMutNho", "doOpenHopQuaCuongthi", "doOpenHopQuaHe2015",
    "doOpenHopQuaSinhNhatNgude", "doOpenLixi", "doOpenLixiNgude", "doOpenQuaHe", "doOpenQuaTet",
    "doOpenHopQuaNoelHong", "doOpenHopQuaTrungThu", "doOpenRuongBatBao", "doSetGiftNguDeDaiNam", "doSetGiftOfflineHtbb",
    "dosetGiftCodeTanThuNgude", "doAddQuaTichLuy", "grantMocNapReward",
}
_HAM = re.compile(r"^\s{0,8}(?:(?:public|private|protected|static|final|synchronized)\s+)*[\w<>\[\],.]+\s+(\w+)\s*\(")
_KHONG_HAM = {"if", "for", "while", "switch", "catch", "synchronized", "return", "new", "else", "throw"}


def ten_ham_theo_dong(dong):
    """Ten ham bao quanh moi dong (mot luot: dong khai bao thut <= 8 cot, khong ket thuc bang ';')."""
    ra, hien = [], ""
    for ln in dong:
        if len(ln) < 300 and not ln.rstrip().endswith(";"):
            m = _HAM.match(ln)
            if m and m.group(1) not in _KHONG_HAM:
                hien = m.group(1)
        ra.append(hien)
    return ra


def cong_cua_file(rel):
    for dau, c in CONG_FILE:
        if rel.startswith(dau):
            return c
    return "True"


def nap_hang_so(d):
    """GemTemplate.X = N (khoi static), Potion/Item/Map public static final ... X = N."""
    hs = {}
    for m in re.finditer(r"GemTemplate\.([A-Z_0-9]+) = (\d+);", d.java_doc("server/src/real/GemTemplate.java")):
        hs.setdefault("GemTemplate." + m.group(1), int(m.group(2)))
    for lop, f in (("Potion", "server/src/real/Potion.java"), ("Item", "server/src/real/Item.java"), ("Map", "server/src/real/Map.java")):
        for m in re.finditer(r"static final (?:int|short|byte) ([A-Z_0-9]+) = (\d+);", d.java_doc(f)):
            hs.setdefault(lop + "." + m.group(1), int(m.group(2)))
    d.hang_so = hs


def gia_tri_id(d, x):
    if x.isdigit():
        return int(x)
    return d.hang_so.get(x)


def quet_java(d):
    dem = 0
    for p in sorted(glob.glob(os.path.join(SRC, "**", "*.java"), recursive=True)):
        rel = os.path.relpath(p, GOC).replace(os.sep, "/")
        s = d.java_doc(rel)
        dong = s.split("\n")
        cong_f = cong_cua_file(rel)
        ham = ten_ham_theo_dong(dong)
        for i, ln in enumerate(dong):
            t = ln.strip()
            if t.startswith("//") or t.startswith("*"):
                continue
            for ma, rx, loai, vai in QUET:
                for m in rx.finditer(ln):
                    gid = gia_tri_id(d, m.group(1))
                    if gid is None:
                        continue
                    if vai == "dung_kiem":
                        # kiem id trong ham ban / cam ban / log khong phai cong dung
                        if re.search(r"(?i)sell|ban|log|khongBan|chan", ham[i]) or ("%s:%d" % (loai, gid)) not in d.vp:
                            continue
                    _them_quet(d, loai, gid, vai, ma, rel, i, ham[i], cong_f)
                    dem += 1
            for m in CHUOI_THUONG.finditer(ln):
                if '"' not in ln:
                    continue
                loai = {"ITEM": "item", "GEM": "ngoc", "PT": "thuoc"}[m.group(1)]
                _them_quet(d, loai, int(m.group(2)), "nguon", "chuỗi thưởng " + m.group(1), rel, i, ham[i], cong_f)
                dem += 1
    d.thong_tin["quet_java"] = dem


def _them_quet(d, loai, id_, vai, ma, rel, i, ham, cong_f):
    cong = cong_f
    if ham in HAM_CHET:
        cong = "False"
    noi = "%s:%d %s()" % (rel, i + 1, ham)
    if vai == "nguon":
        d.nguon("%s:%d" % (loai, id_), ma, noi, cong)
    elif vai == "dung_kiem":
        d.dung("%s:%d" % (loai, id_), ma + " (" + ham + ")", noi, cong)
    else:
        d.dung("%s:%d" % (loai, id_), ma, noi, cong)


# =====================================================================================================
# Nguon / cong dung theo du lieu
# =====================================================================================================

def ap_quy_tac(d):
    for kieu, khoa, co_che, f, mau, cong in QUY_TAC:
        if mau not in d.java_doc(f):
            d.quy_tac_loi.append((co_che, f, mau))
            continue
        noi = f
        if kieu == "dung_type":
            for v in d.vp.values():
                if v.loai == "item" and v.type in khoa:
                    d.dung(v.khoa, co_che, noi, cong)
        elif kieu in ("dung", "can"):
            for i in khoa:
                if ("item:%d" % i) in d.vp:
                    d.dung("item:%d" % i, co_che, noi, cong, bat_buoc=(kieu == "can"))
        elif kieu == "ban":
            for v in d.vp.values():
                if v.loai == "item" and v.gia > 0:
                    d.dung(v.khoa, co_che, noi, cong, manh=False)
        elif kieu == "nguon":
            for i in khoa:
                d.nguon("item:%d" % i, co_che, noi, cong)
        elif kieu == "nguon_luat":
            if khoa == "roi_cu":
                for v in d.vp.values():
                    if v.loai == "item" and (v.id < 214 or 598 <= v.id <= 607) and v.id != 2 and v.nloan == 0 \
                            and v.type <= 19:
                        d.nguon(v.khoa, co_che, noi, cong)
            elif khoa == "boss_thu":
                for v in d.vp.values():
                    if v.loai == "item" and 14 <= v.type <= 18:
                        d.nguon(v.khoa, co_che, noi, cong)
            elif khoa == "npc_ban":
                s = d.java_doc(f)
                m = re.search(r"ALL_ID_ITEM_NPC_SELL = new int\[\]\{([^}]*)\}", s)
                so = [int(x) for x in m.group(1).split(",")]
                for a, b in zip(so[0::2], so[1::2]):
                    for i in range(a, b + 1):
                        if ("item:%d" % i) in d.vp:
                            d.nguon("item:%d" % i, co_che, noi, cong)
    # ngoc
    for v in [x for x in d.vp.values() if x.loai == "ngoc"]:
        if v.nhom in DUNG_NGOC_THEO_KIEU:
            d.dung(v.khoa, DUNG_NGOC_THEO_KIEU[v.nhom], "src/real/GemTemplate.java " + v.nhom)
        if v.gia == 1:
            d.nguon(v.khoa, "cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1)", "server/src/real/OfflineMap.java")
    for ids, co_che, f, mau in DUNG_NGOC_ID:
        if mau not in d.java_doc(f):
            d.quy_tac_loi.append((co_che, f, mau))
            continue
        for i in ids:
            d.dung("ngoc:%d" % i, co_che, f)
    # thuoc: cong dung = co nhanh trong doUsePotion (switch potionType) ; nguon cua hang = price > 0
    # doUsePotion: gom nhan case lien tiep; nhom ma than la "Không thể sử dụng" + tra lai, hoac default/break, KHONG
    # phai cong dung (vat pham do chi doi o NPC - phan quet "kiểm số thuốc" se thay).
    s = d.java_doc("server/src/real/doUsePotion.java")
    truoc = s[:s.index("switch (potionType)")]
    for m in re.finditer(r"potionType >= (\d+) && potionType <= (\d+)\) \{", truoc):
        for i in range(int(m.group(1)), int(m.group(2)) + 1):
            d.dung("thuoc:%d" % i, "dùng thuốc (doUsePotion, trước switch)", "server/src/real/doUsePotion.java")
    for m in re.finditer(r"if \(potionType == (\d+)\) \{", truoc):
        d.dung("thuoc:%s" % m.group(1), "dùng thuốc (doUsePotion, trước switch)", "server/src/real/doUsePotion.java")
    if "Potion.TYPE_HOA_HONG_DO : Potion.TYPE_NHANH_HOA_HONG_VANG" in d.java_doc("server/src/real/MapSuKien.java"):
        for i in (106, 107):
            d.dung("thuoc:%d" % i, "đổi bó hoa Vu Lan (MapSuKien.doVuLanDoiHoa)", "server/src/real/MapSuKien.java", "SK")
    else:
        d.quy_tac_loi.append(("đổi bó hoa Vu Lan", "server/src/real/MapSuKien.java", "TYPE_HOA_HONG_DO : ..."))
    dong = s.split("\n")
    i = 0
    while i < len(dong):
        if not re.match(r"\s*(case [\w.]+|default)\s*:", dong[i]):
            i += 1
            continue
        nhan = []
        while i < len(dong) and (re.match(r"\s*(case [\w.]+|default)\s*:", dong[i]) or dong[i].strip().startswith("//")):
            m = re.match(r"\s*case ([\w.]+)\s*:", dong[i])
            if m:
                nhan.append(m.group(1))
            elif "default" in dong[i]:
                nhan.append("default")
            i += 1
        than = "\n".join(dong[i:i + 3])
        vo_dung = ("Không thể sử dụng\"" in than and "potions[potionType]++" in than) or \
                  ("default" in nhan and than.strip().startswith("break;"))
        if vo_dung:
            continue
        for x in nhan:
            if x == "default":
                continue
            gid = int(x) if x.isdigit() else d.hang_so.get(x if x.startswith("Potion.") else "Potion." + x)
            k = "thuoc:%s" % gid
            if k in d.vp:
                d.dung(k, "dùng thuốc (doUsePotion)", "server/src/real/doUsePotion.java")
    for v in [x for x in d.vp.values() if x.loai == "thuoc"]:
        if v.gia > 0 and v.id > 0:
            d.nguon(v.khoa, "cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0)", "server/src/real/OfflineMap.java")
    d.dung("thuoc:0", "xu (tiền)", "data_potion id 0")


def nguon_roi(d):
    """docs/pokemon/noi_dung_roi.csv (= data_noi_dung_roi). Chi cac nguon doc luc chay moi tinh."""
    p = os.path.join(DOC, "noi_dung_roi.csv")
    nd = d.java_doc("server/src/real/NoiDungLauDai.java")
    boss_doc = "roiBossTheGioi" in nd and "NoiDungLauDai.roiBossTheGioi" in d.java_doc("server/src/real/BossLauDai.java")
    hop_doc = "static String moHop" in nd and "NoiDungLauDai.moHop" in d.java_doc("server/src/real/VatPhamDungLauDai.java")
    trang_bi_doc = "static boolean phatTrangBi" in nd and "phatTrangBi(p, r," in nd
    for r in csv.DictReader(open(p, encoding="utf-8")):
        loai, _, id_ = r["vat_pham"].partition(":")
        k = {"item": "item", "ngoc": "ngoc", "thuoc": "thuoc"}.get(loai)
        if not k:
            d.treo.append((r["vat_pham"], "noi_dung_roi.csv stt " + r["stt"], "loại lạ"))
            continue
        khoa = "%s:%s" % (k, id_)
        noi = "data_noi_dung_roi stt %s (%s %s)" % (r["stt"], r["nguon"], r["khoa_nguon"])
        v = d.lay(khoa)
        if v is None:
            d.treo.append((khoa, noi, "bảng rơi"))
            continue
        ng = r["nguon"]
        if ng in ("dai", "alpha"):
            # NoiDungLauDai.phat: ngoc + item type 28; phatTrangBi: trang bi (dai: chi mon Map.dropItem cu khong rot)
            cu = int(id_) < 214 or 598 <= int(id_) <= 607
            if k == "ngoc" or (k == "item" and v.type == 28):
                d.nguon(khoa, "rơi theo dải cấp" + (" (alpha)" if ng == "alpha" else ""), noi, "LD")
            elif k == "item" and trang_bi_doc and (ng == "alpha" or not cu):
                d.nguon(khoa, "rơi trang bị theo dải cấp" + (" (alpha, Hoàn mỹ)" if ng == "alpha" else "")
                        + " (NoiDungLauDai.phatTrangBi)", noi, "LD")
            elif k == "item" and trang_bi_doc:
                d.nguon_ao.append((khoa, noi, "trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách)"))
            else:
                d.nguon_ao.append((khoa, noi, "NoiDungLauDai.phat chỉ phát ngọc / item type 28 - dòng bị bỏ qua"))
        elif ng == "gym":
            d.nguon(khoa, "Gym thắng lần đầu (GymLauDai.vuKhiHoanMy)", noi, "LD")
        elif ng == "gym_tuan":
            d.nguon(khoa, "Gym đánh lại hằng tuần (GymLauDai.khiHa)", noi, "LD")
        elif ng == "boss_the_gioi" and boss_doc and (k == "ngoc" or (k == "item" and v.type == 28)):
            d.nguon(khoa, "boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi)", noi, "LD")
            nam2 = {1040: 1041, 1042: 1043, 1044: 1045, 1046: 1047, 1048: 1049}
            if k == "item" and int(id_) in nam2 and "TIN_VAT_NAM2" in nd:
                d.nguon("item:%d" % nam2[int(id_)], "boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2)", noi, "LD")
        elif ng == "mo_hop" and hop_doc:
            d.nguon(khoa, "mở %s (NoiDungLauDai.moHop)" % r["khoa_nguon"], noi, "LD")
        else:
            d.nguon_ao.append((khoa, noi, "nguồn '%s' không có code đọc" % ng))


def nguon_nhiem_vu(d):
    """nhiem_vu_moi.csv: engine (NhiemVuEngine) cho moi loai tru chinh/huong_dan; chuoi chinh qua data_quest_moi."""
    p = os.path.join(DOC, "nhiem_vu_moi.csv")
    nv = list(csv.DictReader(open(p, encoding="utf-8")))
    xu_ly_thuoc = "\"thuoc\".equals(k)" in d.java_doc("server/src/real/NhiemVuEngine.java")
    chuoi_cu_tk = "NhiemVuEngine.khiXongChuoiCu(this, info.ma)" in d.java_doc("server/src/real/Char.java")
    nam2_doc = "laChuoiNam2" in d.java_doc("server/src/data/DatabaseLauDai.java")
    for r in nv:
        chinh = r["loai"] in ("chinh", "huong_dan")
        nam2 = chinh and r["dong_co_tuong_thich"] == "0"
        engine = not chinh or (nam2 and nam2_doc)
        if nam2 and engine and (not r["map_id"].strip() or r["kieu_muc_tieu"] == "dan_y"):
            # chuoi nam 2: engine chay khi co ban do Hoenn (doi ban do noi map_id) + tran cap mua >= 91
            for x in re.findall(r"(?:item|thuoc):\d+", r["thuong_khac"] or "") + (
                    ["item:%s" % r["vat_pham"]] if r["vat_pham"] else []):
                d.nguon_ao.append((x, "nhiệm vụ %s (chinh năm 2)" % r["ma"], "chuỗi năm 2 chờ bản đồ Hoenn (map_id trống)"))
            continue
        noi = "nhiệm vụ %s (%s)" % (r["ma"], r["loai"])
        if r["vat_pham"] and engine:
            for x in re.split(r"[,;|]", r["vat_pham"]):
                if x.strip():
                    d.nguon("item:%s" % x.strip(), "thưởng nhiệm vụ (NhiemVuEngine.traThuong)", noi, "LD")
        for m in re.finditer(r"(item|thuoc):(\d+)(?:x(\d+))?", r["thuong_khac"] or ""):
            k = "%s:%s" % (m.group(1), m.group(2))
            v = d.lay(k)
            if v is None:
                d.treo.append((k, noi, "thuong_khac"))
                continue
            if not engine and not chuoi_cu_tk:
                d.nguon_ao.append((k, noi, "thuong_khac của chuỗi chính không được phát (engine cũ)"))
            elif m.group(1) == "item" and v.type != 28:
                d.nguon_ao.append((k, noi, "thuong_khac item chỉ phát type 28"))
            elif m.group(1) == "thuoc" and not xu_ly_thuoc:
                d.nguon_ao.append((k, noi, "thuong_khac thuoc chỉ ghi log"))
            else:
                d.nguon(k, "thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac)", noi, "LD")
    # chuoi chinh: data_quest_moi (LD) - itemgift [cls0..cls4, plus] (InfoQuestTemplate.addGift)
    s = doc(os.path.join(SQL, "pokemon_lau_dai.sql"))
    for r in doc_insert(s, "data_quest_moi"):
        noi = "data_quest_moi id %s" % r["id"]
        ig = [x for x in (r["itemgift"] or "").split(",") if x.strip()]
        if ig and int(ig[0]) < 32000:
            if len(ig) < 6:
                d.nguon_ao.append(("item:%s" % ig[0], noi, "itemgift 1 giá trị: InfoQuestTemplate.addGift cần 6 "
                                                          "(5 hệ + cộng) - lớp > 0 lỗi AIOOBE"))
            else:
                for x in set(ig[:5]):
                    d.nguon("item:%s" % x, "thưởng chuỗi chính (data_quest_moi.itemgift)", noi, "LD")
        typ = int(r["type"] or 0)
        iget = [x for x in (r["idItemget"] or "").split(",") if x.strip() and int(x) < 32000]
        for x in iget:
            k = "nv:%s" % x
            if typ == 2:
                d.nguon(k, "rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest)", noi, "LD")
            elif typ == 1:
                d.nguon(k, "nhận khi nhận nhiệm vụ giao hàng (Char.OnQuest)", noi, "LD")
            d.dung(k, "nộp nhiệm vụ (Char.OnQuest xoá itemquest)", noi, "LD", bat_buoc=True)
        pg = [x for x in (r["potiongift"] or "").split(",") if x.strip() and int(x) < 32000]
        for x in pg:
            d.nguon("thuoc:%s" % x, "thưởng chuỗi chính (potiongift)", noi, "LD")
    # chuoi chinh game cu (data_quest, LD=0)
    dump = doc(os.path.join(SQL, "htbb_day_du.sql"))
    cot_q = cot_bang(dump, "data_quest")
    for r in doc_insert(dump, "data_quest", cot_q):
        noi = "data_quest id %s" % r["id"]
        ig = [x for x in (r["itemgift"] or "").split(",") if x.strip()]
        if ig and int(ig[0]) < 32000:
            for x in set(ig[:5]):
                d.nguon("item:%s" % x, "thưởng nhiệm vụ cũ (data_quest)", noi, "not LD")
        for x in [x for x in (r["idItemget"] or "").split(",") if x.strip() and int(x) < 32000]:
            if ("nv:%s" % x) in d.vp:
                d.nguon("nv:%s" % x, "nhiệm vụ cũ (data_quest)", noi, "not LD")
                d.dung("nv:%s" % x, "nộp nhiệm vụ cũ", noi, "not LD")


def nguon_cua_hang(d):
    """data_shop (doBuySpecialItem): slot thuong = potions[id+19]; slot dac biet da quet tu code. LD: chan theo
    data_shop_gia_lau_dai (tien_moi != tien_cu hoac 9 = khong ban)."""
    dump = doc(os.path.join(SQL, "htbb_day_du.sql"))
    shop = doc_insert(dump, "data_shop", cot_bang(dump, "data_shop"))
    gia = {}
    s = doc(os.path.join(SQL, "pokemon_lau_dai.sql"))
    for r in doc_insert(s, "data_shop_gia_lau_dai", ["id_shop", "ten", "gia_cu", "tien_cu", "gia_moi", "tien_moi",
                                                       "gioi_han_tuan", "ly_do"]):
        gia[int(r["id_shop"])] = (int(r["tien_cu"]), int(r["tien_moi"]))
    bs = d.java_doc("server/src/real/doBuySpecialItem.java")
    dac_biet = {int(x) for x in re.findall(r"item\.id\s*==\s*(\d+)", bs)}
    for a, b in re.findall(r"item\.id\s*>=\s*(\d+)\s*&&\s*item\.id\s*<=\s*(\d+)", bs):
        dac_biet |= set(range(int(a), int(b) + 1))
    dac_biet |= set(range(116, 129)) | {150, 176, 0, 1, 13}
    for r in shop:
        sid = int(r["id"])
        if int(r["sell"] or 0) != 1 or sid in dac_biet:
            continue
        g = gia.get(sid)
        cong = "True"
        if g and (g[1] == 9 or g[0] != g[1]):
            cong = "not LD"
        d.nguon("thuoc:%d" % (sid + 19), "cửa hàng data_shop slot %d" % sid, "data_shop id %d" % sid, cong)


def he_sinh_thai(d):
    k = d.java_doc("server/src/com/htbb/pokemon/KyNangSong.java")
    f = "server/src/com/htbb/pokemon/KyNangSong.java"
    for m in re.finditer(r"\{(\d+), \d+, \d+, \d+, \d+\}", k[k.index("BANG_CAU_CA"):k.index("BANG_DAO")]):
        d.nguon("hst:%s" % m.group(1), "câu cá (KyNangSong.BANG_CAU_CA)", f, "HST")
    for m in re.finditer(r"\{(\d+), \d+, \d+\}", k[k.index("BANG_DAO = {"):k.index("};", k.index("BANG_DAO = {"))]):
        d.nguon("hst:%s" % m.group(1), "khảo cổ (KyNangSong.BANG_DAO)", f, "HST")
    g = d.java_doc("server/src/com/htbb/pokemon/HeSinhThaiGame.java")
    if "final int ma = 30 + Math.floorMod(idCay, TEN_BERRY.length);" in g:
        for i in range(30, 38):
            d.nguon("hst:%d" % i, "thu hoạch vườn berry (HeSinhThaiGame.khiThuHoach)", "server/src/com/htbb/pokemon/HeSinhThaiGame.java",
                    "HST")
    else:
        d.quy_tac_loi.append(("vườn berry 30+idCay%8", "HeSinhThaiGame.java", "30 + "))
    d.nguon("hst:100", "nhà trẻ đẻ trứng (NhaTre)", "server/src/com/htbb/pokemon/NhaTre.java", "HST")
    d.dung("hst:100", "ấp trứng → pet (NhaTre)", "server/src/com/htbb/pokemon/NhaTre.java", "HST")
    for m in re.finditer(r"new CongThuc\((\d+), \"([^\"]*)\", \w+, \d+, new int\[\]\[\]\{(.*?)\}, ([\w.]+), (\d+), "
                         r"(null|\"[^\"]*\")\)", k):
        ct = "công thức %s %s" % (m.group(1), m.group(2))
        for a in re.finditer(r"\{(\d+), (\d+)\}", m.group(3)):
            d.dung("hst:%s" % a.group(1), "nguyên liệu " + ct, f, "HST", bat_buoc=True)
        ra = m.group(4)
        ra = {"VatLieuSinhThai.BANH_POKE": "80", "VatLieuSinhThai.MOI_THOM": "83", "VatLieuSinhThai.SUP_BERRY": "81",
              "VatLieuSinhThai.CA_RI": "82"}.get(ra, ra)
        if ra != "0":
            d.nguon("hst:%s" % ra, ct, f, "HST")
        if m.group(6) != "null":
            for x in CHUOI_THUONG.finditer(m.group(6)):
                loai = {"ITEM": "item", "GEM": "ngoc", "PT": "thuoc"}[x.group(1)]
                d.nguon("%s:%s" % (loai, x.group(2)), ct, f, "HST")
    # cong dung cua thanh pham / tui
    for v in [x for x in d.vp.values() if x.loai == "hst"]:
        if v.nhom == "L_MON_AN":
            d.dung(v.khoa, "dùng món ăn (+thể lực / buff, HeSinhThaiMenu)", "server/src/com/htbb/pokemon/HeSinhThaiMenu.java", "HST")
        elif v.nhom == "L_THUOC":
            d.dung(v.khoa, "dùng → đổi bình thuốc thật (KyNangSong.thuongDungThuoc)", f, "HST")
        elif v.nhom == "L_TRANG_TRI":
            d.dung(v.khoa, "trang trí vườn (điểm trang trí)", "server/src/com/htbb/pokemon/HeSinhThaiMenu.java", "HST")
        elif v.nhom != "L_TRUNG":
            d.dung(v.khoa, "bán NPC / chợ sinh thái", "server/src/com/htbb/pokemon/ChoSinhThai.java", "HST", manh=False)
    for m in re.finditer(r'case (\d+):\s*return "PT:(\d+):\d+";', k):
        d.nguon("thuoc:%s" % m.group(2), "dùng thuốc sinh thái %s" % m.group(1), f, "HST")
    n = d.java_doc("server/src/com/htbb/pokemon/NhaTre.java")
    a = n[n.index("LOAI_TRUNG = {"):n.index("};", n.index("LOAI_TRUNG = {"))]
    for m in re.finditer(r"\{\d+, (\d+), (-?\d+), \d+\}", a):
        d.nguon("item:%s" % m.group(1), "trứng nhà trẻ nở (NhaTre.LOAI_TRUNG)", "server/src/com/htbb/pokemon/NhaTre.java", "HST")
        if int(m.group(2)) > 0:
            d.nguon("item:%s" % m.group(2), "trứng phẩm chất S nở pet cấp 2 (NhaTre)", "server/src/com/htbb/pokemon/NhaTre.java", "HST")


def vat_pham_lau_dai(d):
    """Bang data_vat_pham_lau_dai (cong dung that cua 1201..1260 + vat pham doi/mo) va data_cua_hang_lau_dai."""
    s = doc(os.path.join(SQL, "pokemon_vat_pham_lau_dai.sql"))
    d.bang_vp_ld = {}
    for r in doc_insert(s, "data_vat_pham_lau_dai", ["id_item", "ma", "ten", "nhom", "hieu_ung", "gioi_han", "cap_tu",
                                                       "cap_den"]):
        d.bang_vp_ld[int(r["id_item"])] = r
    for i in range(1201, 1261):
        k = "item:%d" % i
        if k in d.vp and i not in d.bang_vp_ld:
            d.treo.append((k, "data_vat_pham_lau_dai", "item 1201..1260 không có dòng hiệu ứng: dùng không có tác dụng"))
    vp = d.java_doc("server/src/real/VatPhamDungLauDai.java")
    for i, r in d.bang_vp_ld.items():
        if ("item:%d" % i) not in d.vp:
            d.treo.append(("item:%d" % i, "data_vat_pham_lau_dai", "hiệu ứng trỏ vào item không có"))
            continue
        if r["nhom"] == "hop" and "\"hop\".equals(nhom)" in vp:
            d.dung("item:%d" % i, "mở hộp (VatPhamDungLauDai.moHop)", "data_vat_pham_lau_dai id %d" % i, "LD")
        elif not (1201 <= i <= 1260):
            d.dung("item:%d" % i, "dùng theo data_vat_pham_lau_dai (%s)" % r["nhom"], "data_vat_pham_lau_dai id %d" % i, "LD")
    # Tang toc (com.htbb.pokemon.TangToc, 18/10/2026): nguon trong CODE - ban cao cap ban o Cua hang & Nap
    # (nap_cua_hang trao tang_toc, NapTienGame), ban khoa tu nhiem vu tuan / lich dang nhap / ve mua (TangTocGame.thuongKhoa).
    tt = d.java_doc("server/src/com/htbb/pokemon/TangToc.java") or ""
    m_cc = re.search(r"ID_CAO_CAP = \{([^}]*)\}", tt)
    m_kh = re.search(r"ID_KHOA = \{([^}]*)\}", tt)
    if m_cc and m_kh:
        cc = [int(x) for x in m_cc.group(1).split(",") if x.strip()]
        kh = [int(x) for x in m_kh.group(1).split(",") if x.strip()]
        co_nap = "TRAO_TANG_TOC" in (d.java_doc("server/src/com/htbb/pokemon/NapTienGame.java") or "")
        co_tuan = "TangTocGame.thuongKhoa" in (d.java_doc("server/src/real/NhiemVuEngine.java") or "")
        co_dn = "TangTocGame.thuongKhoa" in (d.java_doc("server/src/real/QuaHoatDong.java") or "")
        co_ve = "TG:" in (d.java_doc("server/src/com/htbb/pokemon/NapTienLogic.java") or "")
        for i, (a_, b_) in enumerate(zip(cc, kh)):
            if a_ == b_:
                continue  # keo hiem: dung id cu, nguon da co
            if co_nap:
                d.nguon("item:%d" % a_, "Cửa hàng & Nạp (lượng, trao tang_toc; cần sv.nap_tien)", "NapTienGame.mua", "LD")
            if (co_tuan and i in (0, 3)) or (co_dn and i in (0, 2)) or (co_ve and i in (0, 1, 2)):
                d.nguon("item:%d" % b_, "bản khoá: nhiệm vụ tuần / lịch đăng nhập / vé mùa (TangTocGame.thuongKhoa)",
                        "real/TangTocGame.java", "LD")
            for k in (a_, b_):
                d.dung("item:%d" % k, "tăng tốc luyện cấp (TangTocGame.dung, chung trần điểm)", "server/src/real/TangTocGame.java", "LD")
    d.cua_hang = doc_insert(s, "data_cua_hang_lau_dai", ["id", "nhom", "ten", "chi_phi", "thuong", "cap", "ngay_mo", "lop",
                                                         "gioi_han_tuan"])
    co_menu = "CuaHangLauDai.moMenu" in d.java_doc("server/src/real/SoTayLauDai.java")
    for r in d.cua_hang:
        noi = "data_cua_hang_lau_dai id %s (%s)" % (r["id"], r["nhom"])
        cong = "LD" if co_menu else "False"
        che_tao = r["nhom"].startswith("Chế tạo")
        for c in r["chi_phi"].split(";"):
            m = re.match(r"(item|thuoc|ngoc):(\d+)x(\d+)$", c.strip())
            if m:
                d.dung("%s:%s" % (m.group(1), m.group(2)), ("nguyên liệu chế tạo: " if che_tao else "đổi tại Sổ tay → Cửa hàng: ")
                       + r["ten"], noi, cong, bat_buoc=che_tao)
            elif not re.match(r"(xu|luong_khoa):\d+$", c.strip()):
                d.treo.append((c, noi, "chi phí không đọc được"))
        for x in CHUOI_THUONG.finditer(r["thuong"]):
            loai = {"ITEM": "item", "GEM": "ngoc", "PT": "thuoc"}[x.group(1)]
            d.nguon("%s:%s" % (loai, x.group(2)), "Sổ tay → Cửa hàng & đổi vật phẩm (%s)" % r["nhom"], noi, cong)
        if re.search(r"\b(LUONG|LUONGKHOA|XU)\b", r["thuong"]):
            d.treo.append(("tiền", noi, "đổi vật phẩm ra tiền tệ (vượt ngân sách)"))


def pokedex(d):
    """Pokedex.phanThuong (CDP). Khi LD: PokedexPokemon doi keo EXP cu (PT:10..12, bi chan) sang Keo Hiem."""
    s = d.java_doc("server/src/com/htbb/pokemon/Pokedex.java")
    i0 = s.index("public static String phanThuong(")
    than = s[i0:s.index("\n    }", i0)]
    doi = "doiThuongLauDai" in d.java_doc("server/src/real/PokedexPokemon.java")
    for m in CHUOI_THUONG.finditer(than):
        loai = {"ITEM": "item", "GEM": "ngoc", "PT": "thuoc"}[m.group(1)]
        k = "%s:%s" % (loai, m.group(2))
        cong = "CDP"
        if k in ("thuoc:10", "thuoc:11", "thuoc:12") and doi:
            cong = "CDP and not LD"
        d.nguon(k, "mốc Pokédex (Pokedex.phanThuong)", "server/src/com/htbb/pokemon/Pokedex.java", cong)
    if doi and "doiThuongLauDai" in s:
        keo = re.search(r"ID_KEO_HIEM = (\d+);", s)
        trung = re.search(r"ID_TRUNG_MAY_MAN = (\d+);", s)
        if keo:
            d.nguon("item:%s" % keo.group(1), "mốc Pokédex khi bật dài hạn (Pokedex.doiThuongLauDai)",
                    "server/src/com/htbb/pokemon/Pokedex.java", "CDP and LD")
        if trung:
            d.nguon("item:%s" % trung.group(1), "mốc Pokédex 50 khi bật dài hạn (Pokedex.doiThuongLauDai)",
                    "server/src/com/htbb/pokemon/Pokedex.java", "CDP and LD")


def keo_exp_cu(d):
    """doUsePotion: keo EXP 10..12 bi tu choi khi LD (GD2b)."""
    s = d.java_doc("server/src/real/doUsePotion.java")
    if "ngừng sử dụng" in s or "ngung su dung" in s.lower():
        for i in (10, 11, 12):
            v = d.lay("thuoc:%d" % i)
            v.dung = [(c, n, "(%s) and not LD" % g if "doUsePotion" in n else g, m, b) for (c, n, g, m, b) in v.dung]


# =====================================================================================================
# Danh gia
# =====================================================================================================

def bat(cong, cfg):
    return bool(eval(cong, {"__builtins__": {}}, dict(cfg)))


def danh_gia(d, cfg):
    kq = {}
    for k, v in d.vp.items():
        ng = [x for x in v.nguon if bat(x[2], cfg)]
        dg = [x for x in v.dung if bat(x[2], cfg)]
        manh = [x for x in dg if x[3]]
        can = [x for x in v.dung if x[4] and bat(x[2], cfg)]
        nguon_tat = [x for x in v.nguon if not bat(x[2], cfg)]
        if ng and not dg:
            lop = "mo_coi"
        elif ng and not manh:
            lop = "chi_ban"
        elif not ng and can:
            lop = "khong_lay_duoc"
        elif not ng and v.khoa in d.thiet_ke_can_nguon:
            lop = "khong_lay_duoc"
        elif not ng:
            lop = "ngu"  # khong ton tai trong cau hinh nay
        else:
            lop = "ok"
        kq[k] = (lop, ng, dg, nguon_tat)
    return kq


def tap_thiet_ke(d):
    """Vat pham THIET KE de nguoi choi co (noi dung moi): thieu nguon = loi 'khong lay duoc' du chua co ai can."""
    s = set()
    for v in d.vp.values():
        if v.loai == "item" and (1001 <= v.id <= 1260 or 960 <= v.id <= 967):
            s.add(v.khoa)
    return s


def trung_ten(d, kq):
    """Ten dong 1 trung nhau giua cac vat pham DANG TON TAI nhung khac loai / khac chuc nang."""
    nhom = {}
    for k, (lop, *_r) in kq.items():
        if lop in ("ngu",):
            continue
        v = d.vp[k]
        nhom.setdefault(chuan(v.hien), []).append(v)
    # Ten nhiem vu = vat lieu sinh thai CO Y (nv_chung.VAT_SINH_THAI: nhiem vu dem dung vat lieu do) -> cung khai niem
    cung = set()
    m = re.search(r"VAT_SINH_THAI = \{(.*?)\n\}", d.java_doc("tools/pokemon/nv_chung.py"), re.S)
    if m:
        for a_, b_ in re.findall(r'"([^"]+)": "([^"]+)"', m.group(1)):
            if a_ == b_:
                cung.add(chuan(a_))
    ra = []
    for t, ds in sorted(nhom.items()):
        if len(ds) < 2 or not t:
            continue
        if t in cung and {x.loai for x in ds} == {"nv", "hst"}:
            continue
        loai = {(x.loai, x.type if x.loai == "item" and x.type in (26, 27, 28) else (x.type if x.loai == "item" else -9))
                for x in ds}
        moi = any(x.loai == "item" and x.id >= 960 for x in ds)
        if len(loai) > 1 or (moi and any(x.loai == "item" and x.type == 28 for x in ds) and len(ds) > 1
                             and len({x.type for x in ds}) > 1):
            ra.append((t, ds))
        elif moi and len({x.id for x in ds}) > 1 and all(x.type == 28 for x in ds):
            ra.append((t, ds))
    return ra


def nap_chu_pokemon():
    import importlib.util
    sp = importlib.util.spec_from_file_location("chu_pokemon", os.path.join(GOC, "tools", "pokemon", "chu_pokemon.py"))
    cp = importlib.util.module_from_spec(sp)
    sp.loader.exec_module(cp)
    b = cp.doc_bang()
    return cp.tu_dien_doi(b), cp.tu_dien_cam(b)


def ten_hien(d, doi):
    """Ten nguoi choi THAY: server gui moi ten qua ChuPokemon.doi (MessageCreator)."""
    for v in d.vp.values():
        try:
            v.hien = doi.doi(v.ten) if v.loai in ("item", "thuoc", "ngoc", "nv") else v.ten
        except Exception:
            v.hien = v.ten


def tu_cam(d, kq, cam):
    ra = []
    for k, (lop, *_r) in kq.items():
        if lop in ("ngu",):
            continue
        v = d.vp[k]
        t = cam.tim(v.hien)
        if t:
            ra.append((v, t))
    return ra


# =====================================================================================================
# Ghi bao cao
# =====================================================================================================

# File du lieu dau vao: dau van tay ghi vao KIEM_ITEM.md, test/KiemItemMoCoi.java tinh lai -> bao "chay lai script"
# khi SQL/CSV doi ma bao cao cu. (Khong gom .java: doi nhom khac sua code lien tuc.)
DAU_VAO = ["sql/htbb_day_du.sql", "sql/pokemon_noi_dung.sql", "sql/pokemon_vat_pham_lau_dai.sql", "sql/pokemon_lau_dai.sql",
           "sql/pokemon_doi_chu.sql", "sql/pokemon_nhiem_vu.sql", "docs/pokemon/noi_dung_roi.csv",
           "docs/pokemon/nhiem_vu_moi.csv"]


def dau_van_tay():
    h = hashlib.sha1()
    for f in DAU_VAO:
        h.update(f.encode())
        h.update(open(os.path.join(GOC, f), "rb").read().replace(b"\r", b""))
    return h.hexdigest()


TEN_LOP = {"mo_coi": "Mồ côi (có nguồn, không công dụng)", "chi_ban": "Chỉ bán NPC (không công dụng thật)",
           "khong_lay_duoc": "Không lấy được (có công dụng / thiết kế nhưng không nguồn nào chạy)",
           "ngu": "Không tồn tại (không nguồn, không cần)", "ok": "Đủ nguồn + công dụng"}
TEN_LOAI = {"item": "data_item", "thuoc": "data_potion", "ngoc": "ngọc", "nv": "vật phẩm nhiệm vụ", "hst": "túi sinh thái"}


def gon(ds, n=3):
    t = sorted({x[0] for x in ds})
    s = "; ".join(t[:n])
    return s + (" …(+%d)" % (len(t) - n) if len(t) > n else "")


def ghi(d, ket_qua, loi_dem):
    o = []
    o.append("# Kiểm vật phẩm mồ côi / treo / không lấy được\n")
    o.append("> **File sinh ra** bởi `tools/pokemon/kiem_item_mo_coi.py` - đừng sửa tay. Test: `test/KiemItemMoCoi.java`.\n"
             "> Luật: mọi vật phẩm có thể tồn tại phải có ≥ 1 **nguồn** đang chạy và ≥ 1 **công dụng** thật "
             "(bán NPC chỉ là đường lui).\n")
    o.append("\n## 0. Tóm tắt\n")
    o.append("| Cấu hình | " + " | ".join(TEN_LOP[x] for x in ("ok", "mo_coi", "chi_ban", "khong_lay_duoc", "ngu")) + " |\n")
    o.append("|---" * 6 + "|\n")
    for ten, cfg, kiem in CAU_HINH:
        kq = ket_qua[ten]
        dem = {x: sum(1 for v in kq.values() if v[0] == x) for x in TEN_LOP}
        o.append("| %s (`lau_dai=%d he_sinh_thai=%d`)%s | %s |\n" % (
            ten, cfg["LD"], cfg["HST"], "" if kiem else " — chỉ báo cáo",
            " | ".join(str(dem[x]) for x in ("ok", "mo_coi", "chi_ban", "khong_lay_duoc", "ngu"))))
    o.append("\nVật phẩm theo loại: " + ", ".join("%s %d" % (TEN_LOAI[l], sum(1 for v in d.vp.values() if v.loai == l))
                                                 for l in TEN_LOAI) + ".\n")
    o.append("Tham chiếu treo: **%d**. Quy tắc lỗi thời: **%d**. Trùng tên: **%d**. Tên kiếm hiệp còn lấy được: **%d**.\n"
             % (len(d.treo), len(d.quy_tac_loi), len(d.trung), len(d.cam)))
    o.append("\nLỗi bị test chặn (cấu hình A + B): **%d**.\n" % loi_dem)
    o.append("\nDấu vân tay đầu vào (SHA-1 của %s, bỏ `\\r`): `%s`\n" % (", ".join("`%s`" % f for f in DAU_VAO), dau_van_tay()))
    for ten, cfg, kiem in CAU_HINH:
        kq = ket_qua[ten]
        o.append("\n## %s. Cấu hình %s: `sv.lau_dai=%d`, `sv.he_sinh_thai=%d`%s\n" % (
            "1" if ten == "A" else ("2" if ten == "B" else "3"), ten, cfg["LD"], cfg["HST"],
            "" if kiem else " (game cũ, chỉ báo cáo)"))
        for lop in ("mo_coi", "chi_ban", "khong_lay_duoc"):
            ds = sorted([k for k, v in kq.items() if v[0] == lop], key=khoa_sx)
            o.append("\n### %s: %d\n" % (TEN_LOP[lop], len(ds)))
            if not ds:
                continue
            o.append("\n| Vật phẩm | Tên | Nguồn đang chạy | Công dụng | Nguồn bị tắt |\n|---|---|---|---|---|\n")
            for k in ds[:400]:
                v = d.vp[k]
                lop_, ng, dg, tat = kq[k]
                o.append("| `%s` | %s | %s | %s | %s |\n" % (k, ten_dong1(v.ten).replace("|", "/"), gon(ng) or "—",
                                                          gon(dg) or "—", gon(tat) or "—"))
    o.append("\n## 4. Tham chiếu treo (trỏ vào vật phẩm không có)\n\n")
    if d.treo:
        o.append("| Tham chiếu | Ở đâu | Ghi chú |\n|---|---|---|\n")
        for k, noi, g in sorted(set(d.treo)):
            o.append("| `%s` | %s | %s |\n" % (k, noi, g))
    else:
        o.append("Không có.\n")
    o.append("\n## 5. Nguồn khai báo nhưng không chạy (thiết kế / hệ thống đã tắt)\n\n")
    gom = {}
    for k, noi, ly in d.nguon_ao:
        gom.setdefault((k, ly), []).append(noi)
    if gom:
        o.append("| Vật phẩm | Lý do | Số chỗ | Ví dụ |\n|---|---|---|---|\n")
        for (k, ly), ds in sorted(gom.items(), key=lambda x: (khoa_sx(x[0][0]), x[0][1])):
            o.append("| `%s` %s | %s | %d | %s |\n" % (k, ten_dong1(d.vp[k].ten) if k in d.vp else "", ly, len(ds), ds[0]))
    else:
        o.append("Không có.\n")
    o.append("\n## 6. Trùng tên / gần trùng (khác loại hoặc khác chức năng)\n\n")
    if d.trung:
        o.append("| Tên | Vật phẩm |\n|---|---|\n")
        for t, ds in d.trung:
            o.append("| %s | %s |\n" % (t, ", ".join("`%s` (type %s)" % (x.khoa, x.type) for x in ds)))
    else:
        o.append("Không có.\n")
    o.append("\n## 7. Tên kiếm hiệp còn lấy được (từ điển cấm `chu_pokemon.tu_dien_cam`)\n\n")
    if d.cam:
        for v, t in d.cam:
            o.append("- `%s` %s (người chơi thấy: %s) — từ cấm \"%s\"\n" % (v.khoa, ten_dong1(v.ten), ten_dong1(v.hien), t))
    else:
        o.append("Không có.\n")
    o.append("\n## 8. Quy tắc viết tay lỗi thời\n\n")
    if d.quy_tac_loi:
        for c, f, m in d.quy_tac_loi:
            o.append("- %s: `%s` không còn chuỗi `%s`\n" % (c, f, m))
    else:
        o.append("Không có: mọi quy tắc trong `QUY_TAC` còn khớp mã nguồn.\n")
    o.append("\n## 9. Cách đọc, giới hạn\n\n")
    o.append("- **Nguồn** = rơi (bảng `data_noi_dung_roi` chỉ tính nguồn `dai`/`alpha`/`gym`/`gym_tuan` mà code đọc), "
             "cửa hàng (`data_shop` trừ dòng bị `data_shop_gia_lau_dai` chặn, NPC bán đồ, cửa hàng thuốc/ngọc, "
             "`data_cua_hang_lau_dai`), thưởng nhiệm vụ (engine + chuỗi chính `data_quest_moi`), Gym, Pokédex, "
             "hệ sinh thái, và số liệu cứng quét từ `src/` (`createItemSoLuong`, `doAddGemItem`, `potions[N]++`, "
             "`new Potion((short)N`, chuỗi `ITEM:/GEM:/PT:`). Giftcode không tính là nguồn (quản trị phát tay).\n")
    o.append("- **Công dụng** = mặc theo type, dùng (`doUsePotion`, `VatPhamDungLauDai`, đá tiến hoá, rương…), "
             "nguyên liệu nâng cấp / chế tạo / ngọc theo kiểu `GemTemplate.TYPE_*`, nộp nhiệm vụ, đổi ở "
             "`data_cua_hang_lau_dai`, công thức hệ sinh thái, trang trí. Bán NPC = “chỉ bán”.\n")
    o.append("- Cổng: `LD` = `sv.lau_dai`, `HST` = `sv.he_sinh_thai`, `CDP` = `sv.che_do_pokemon`, `SK` = sự kiện theo lịch "
             "(lịch 52 tuần của dài hạn bật lại các sự kiện cũ), `NAP` = `sv.nap_tien`. Hàm không ai gọi (`HAM_CHET`) "
             "không tính.\n")
    o.append("- Lớp cũ (thuốc / ngọc sự kiện kiếm hiệp) đọc qua mảng id nên phần quét là **xấp xỉ**: vật phẩm cũ "
             "không tìm thấy nguồn được xếp “không tồn tại”, không phải lỗi.\n")
    o.append("\n## 10. Ma trận (vật phẩm có nguồn ở cấu hình A hoặc B)\n\n")
    o.append("Đầy đủ: `docs/pokemon/kiem_item.csv`.\n\n| Vật phẩm | Tên | A | B | Nguồn | Công dụng |\n|---|---|---|---|---|---|\n")
    for k in sorted(d.vp, key=khoa_sx):
        a, b = ket_qua["A"][k], ket_qua["B"][k]
        if a[0] == "ngu" and b[0] == "ngu":
            continue
        ng = a[1] or b[1]
        dg = a[2] or b[2]
        o.append("| `%s` | %s | %s | %s | %s | %s |\n" % (k, ten_dong1(d.vp[k].ten).replace("|", "/")[:60], a[0], b[0],
                                                       gon(ng, 2), gon(dg, 2)))
    open(RA_MD, "w", encoding="utf-8", newline="\n").write("".join(o))
    with open(RA_CSV, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["khoa", "loai", "id", "ten", "type", "A", "B", "C", "nguon", "cong_dung"])
        for k in sorted(d.vp, key=khoa_sx):
            v = d.vp[k]
            w.writerow([k, v.loai, v.id, ten_dong1(v.ten), v.type, ket_qua["A"][k][0], ket_qua["B"][k][0],
                        ket_qua["C"][k][0], " ; ".join(sorted({"%s @ %s [%s]" % (x[0], x[1], x[2]) for x in v.nguon})[:8]),
                        " ; ".join(sorted({"%s @ %s [%s]%s" % (x[0], x[1], x[2], "" if x[3] else " (bán)") for x in v.dung})[:8])])


def khoa_sx(k):
    l, _, i = k.partition(":")
    return (list(TEN_LOAI).index(l) if l in TEN_LOAI else 9, int(i) if i.lstrip("-").isdigit() else 0, k)


def chay():
    d = DuLieu()
    nap_vu_tru(d)
    d.thiet_ke_can_nguon = tap_thiet_ke(d)
    nap_hang_so(d)
    ap_quy_tac(d)
    quet_java(d)
    nguon_roi(d)
    nguon_nhiem_vu(d)
    nguon_cua_hang(d)
    he_sinh_thai(d)
    vat_pham_lau_dai(d)
    pokedex(d)
    keo_exp_cu(d)
    ket_qua = {ten: danh_gia(d, cfg) for ten, cfg, _k in CAU_HINH}
    gop = {}
    for ten in ("A", "B"):
        for k, v in ket_qua[ten].items():
            if v[0] != "ngu" and k not in gop:
                gop[k] = v
    doi, cam = nap_chu_pokemon()
    ten_hien(d, doi)
    d.trung = trung_ten(d, gop)
    d.cam = tu_cam(d, gop, cam)
    loi = 0
    for ten, cfg, kiem in CAU_HINH:
        if kiem:
            loi += sum(1 for v in ket_qua[ten].values() if v[0] in ("mo_coi", "chi_ban", "khong_lay_duoc"))
    loi += len(set(d.treo)) + len(d.quy_tac_loi) + len(d.trung) + len(d.cam)
    ghi(d, ket_qua, loi)
    return d, ket_qua, loi


def main():
    d, kq, loi = chay()
    for ten, cfg, kiem in CAU_HINH:
        dem = {x: sum(1 for v in kq[ten].values() if v[0] == x) for x in TEN_LOP}
        print("%s lau_dai=%d hst=%d: %s" % (ten, cfg["LD"], cfg["HST"], " ".join("%s=%d" % x for x in dem.items())))
    print("treo=%d quy_tac_loi=%d trung_ten=%d tu_cam=%d nguon_ao=%d" % (len(set(d.treo)), len(d.quy_tac_loi), len(d.trung),
                                                                      len(d.cam), len(d.nguon_ao)))
    print("LOI (A+B) = %d -> %s" % (loi, os.path.relpath(RA_MD, GOC)))
    if "--kiem" in sys.argv:
        sys.exit(min(loi, 100))


if __name__ == "__main__":
    main()
