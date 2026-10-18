#!/usr/bin/env python3
"""
NOI DUNG DAY DU cho 1-2 nam choi (docs/pokemon/NOI_DUNG_DAY_DU.md): trang bi 11 bac, nguyen
lieu nang cap, ngoc he, phan ra, vat pham tieu hao, chieu theo dong/giai doan, quai theo dai
cap, quai tinh anh (alpha), 16 Gym, boss the gioi hang tuan, bang roi, vat pham su kien 52
tuan, do trang tri theo mua, cua hang, che tao.

MOT NGUON: moi con so lay tu tools/pokemon/sinh_sql_lau_dai.py (ngan sach kinh te, gia bac,
tran cap theo mua, chi phi nang cap) + bang da co (sql/htbb_day_du.sql, docs/pokemon/*.csv).
Sua so O DAY roi chay lai; KHONG sua tay cac file sinh ra:

  sql/pokemon_noi_dung.sql            bang staging data_noi_dung_* (DROP/CREATE/INSERT) + phan
                                      NAP DUOC NGAY: data_item id 1001..1199 (DELETE + INSERT,
                                      chay lai nhieu lan van dung). Gom vao xuat_sql_day_du.sh.
  docs/pokemon/noi_dung_*.csv         ban noi bo (co id, cong thuc, nguon)
  docs/pokemon/web/*.csv              ban cho website: chi cot nguoi choi can (khong id/cong thuc)

Chay (o thu muc HTBB/):  python3 -I tools/pokemon/sinh_noi_dung.py
Kiem:                     test/KiemNoiDung.java (trong test/Kiem.java)

Chi dung thu vien chuan. Tat dinh: cung dau vao -> cung dau ra tung byte.

Rang buoc engine (doc ky truoc khi sua):
  - data_potion PHAI du dung Char.SO_O_THUOC (162) dong -> KHONG them thuoc moi vao data_potion.
    Thuoc/trang thai/xua quai moi nam o bang staging voi trang_thai = 'can_code'.
  - Ngoc (gem) la mang cung trong MapNapTaiNguyen.loadGemTemplate -> chi anh xa id ngoc.
  - Chieu: so o chieu + ten theo client (MessageCreator/SkillManager.SKILL_NAME, bang_chieu.csv)
    -> giu nguyen so hieu va ten; chi them so lieu (cap mo, % sat thuong, hoi chieu, PP).
  - data_item dong 268..466 / 639..670 / 715..787 chi so 0 la KHUNG CHE TAO (chi so do code
    CharManager.ARMOR_POINT / CreateItemTemplate gan) -> KHONG sua dong cu, chi THEM dong moi.
  - Icon (xstart) cua dong moi lay lai icon dang co (test KiemIconPokemon: moi xstart co file).
"""
import csv
import io
import math
import os
import re
import sys

DIR = os.path.dirname(os.path.abspath(__file__))
GOC = os.path.dirname(os.path.dirname(DIR))
sys.path.insert(0, DIR)
import sinh_sql_lau_dai as LD  # noqa: E402

RA_SQL = os.path.join(GOC, "sql", "pokemon_noi_dung.sql")
RA_DOC = os.path.join(GOC, "docs", "pokemon")
RA_WEB = os.path.join(GOC, "docs", "pokemon", "web")
F_CHIEU = os.path.join(GOC, "docs", "pokemon", "bang_chieu.csv")
F_QUAI = os.path.join(GOC, "docs", "pokemon", "bang_quai.csv")
F_SK = os.path.join(GOC, "docs", "pokemon", "su_kien_nam.csv")
F_NV = os.path.join(GOC, "docs", "pokemon", "nhiem_vu_moi.csv")

# =====================================================================================
# 0. THAM SO CHUNG
# =====================================================================================

ID_MOI_TU, ID_MOI_DEN = 1001, 1199     # dai id data_item danh rieng cho noi dung moi
SO_O_THUOC = 162                       # Char.SO_O_THUOC (data_potion id 0..161)
SO_NGOC = 67                           # MapNapTaiNguyen.loadGemTemplate (id 0..66)
NGOC_DA_NANG_CAP = 8                   # RoiLauDai.ID_DA (giai doan 2)
NGOC_BUA = [0, 1, 2, 3, 4]             # ngoc bao hiem 0-4 = Bua bao ho (NangCapLauDaiGame)

BAC = LD.BAC_TB                        # (bac, ten, cap, ngay mo)
SO_BAC = len(BAC)


F_ICON = os.path.join(GOC, "docs", "pokemon", "bang_icon.csv")
_ICON_ND = None


def icon_nd(khoa, mac_dinh=0):
    """Id anh 14000+n cua icon 'nd:<khoa>' trong docs/pokemon/bang_icon.csv (tools/pokemon/
    sinh_icon_pokemon.py, anh map/icon_pokemon/<n>.png); chua co -> mac_dinh (icon cu dung lai)."""
    global _ICON_ND
    if _ICON_ND is None:
        _ICON_ND = {}
        if os.path.exists(F_ICON):
            for r in doc_csv(F_ICON):
                for d in r["dung_cho"].split():
                    if d.startswith("nd:"):
                        _ICON_ND[d[3:]] = int(r["id_anh"])
    return _ICON_ND.get(khoa, mac_dinh)


def khoa_ten(t):
    """'Mảnh thiên thạch' -> 'manh_thien_thach' (khoa icon nd:)."""
    import unicodedata
    t = unicodedata.normalize("NFD", t.replace("đ", "d").replace("Đ", "D"))
    t = "".join(c for c in t if unicodedata.category(c) != "Mn")
    return re.sub(r"[^a-z0-9]+", "_", t.lower()).strip("_")


def tran_cap_ngay(ngay):
    t = 0
    for d, c in LD.TRAN_CAP_THEO_NGAY:
        if ngay >= d:
            t = c
    return t


def xu_gio(L):
    return LD.XU_GIO[max(1, min(LD.CAP_TOI_DA, L))]


def tc_goc(L):
    """Tan cong 'tran' (khong do) o cap L - THIET_KE muc 3.6."""
    return LD.tan_cong_tran(L)


# =====================================================================================
# 1. TRANG BI: 11 bac x 11 o (THIET_KE muc 3)
# =====================================================================================

# Do hiem (giu colorName cu): he so chi so (phan tram), nguon duoc phep
DO_HIEM = [("trang", "Trắng", 100, "mọi nguồn"), ("xanh", "Xanh", 110, "rơi, nhiệm vụ, chế tạo"),
           ("do", "Đỏ", 120, "rơi, nhiệm vụ, chế tạo"),
           ("hoan_my", "Hoàn mỹ", 130, "chỉ boss Gym, boss thế giới, quái tinh anh"),
           ("vang", "Vàng", 100, "sự kiện, chỉ trang trí")]

# Phan chia ngan sach cua bac (tan cong / phong thu / HP) cho tung loai o. Tong moi cot = 1.
# (ma, type data_item, ten goc, so o tren nguoi, tc, thu, hp, chi so phu, nguon)
LOAI_O = [
    ("khan_quang", 0, "Khăn quàng", 1, 0.00, 0.35, 0.25, "", "cua_hang,roi"),
    ("dai", 1, "Đai", 1, 0.00, 0.25, 0.20, "", "cua_hang,roi"),
    ("bang_do", 2, "Băng đô", 1, 0.00, 0.20, 0.15, "", "cua_hang,roi"),
    ("vu_khi", -1, "", 1, 0.40, 0.00, 0.00, "he_chieu", "nhiem_vu,roi,gym"),
    ("huy_hieu", 8, "Huy hiệu", 2, 0.10, 0.00, 0.00, "", "gym,nhiem_vu"),
    ("vong_co", 9, "Vòng cổ", 1, 0.15, 0.00, 0.10, "", "che_tao,roi"),
    ("long_vu", 10, "Lông vũ tốc độ", 1, 0.00, 0.10, 0.10, "toc_do", "cua_hang,roi"),
    ("gang_dam", 11, "Găng đấm", 1, 0.15, 0.00, 0.00, "chi_mang", "cua_hang,roi"),
    ("phien_da", 12, "Phiến đá hệ", 1, 0.10, 0.00, 0.00, "he_phien_da", "che_tao,su_kien"),
    ("ruy_bang", 19, "Ruy băng", 1, 0.00, 0.10, 0.20, "", "nhiem_vu,boss_the_gioi"),
]
# Vu khi theo dong (held item Pokemon) - type data_item 3..7, clazz = dong
VU_KHI = [(0, 3, "Than lửa", "Lửa"), (1, 4, "Hạt giống thần", "Cỏ"), (2, 5, "Khăn lụa", "Thường"),
          (3, 6, "Nước thần", "Nước"), (4, 7, "Nam châm", "Điện")]
DONG = ["Charmander", "Bulbasaur", "Eevee", "Squirtle", "Pichu"]

# Bo trang bi (set) theo vung - thuong GIONG NHAU moi bac (THIET_KE 3.4) nen khong leo thang.
TEN_BO = ["Bộ Pallet", "Bộ Pewter", "Bộ Cerulean", "Bộ Vermilion", "Bộ Celadon", "Bộ Saffron",
          "Bộ Cinnabar", "Bộ Ecruteak", "Bộ Blackthorn", "Bộ Hoenn", "Bộ Rayquaza"]
VUNG_BO = ["Kanto", "Kanto", "Kanto", "Kanto", "Kanto", "Kanto", "Kanto", "Johto", "Johto", "Hoenn",
           "Hoenn"]


HE_SO_NC15 = 1 + sum(LD.CHI_SO_NC_PHAN_TRAM) / 100.0   # do +15 = +55% chi so mon
TRAN_BAC_MOI = 1.10                                   # bac moi +0 <= 1,1 x bac cu +15 (THIET_KE 3.6)


def ngan_sach_bac(b):
    """(tan cong, phong thu, HP) do (Trang +0) cua bac b o cap cua bac (don vi thiet ke).

    Chuan: do = 60% chi so 'tran' (tan cong 30+12L, thu 25% tan cong, HP 70(25+L)) o cap bac.
    Chan tren: tong chi so (tran + do bac b) <= 1,1 x (tran + do bac b-1 o +15) cung cap ->
    chi bac dong/bac (cap 10/20, chenh lech cap lon so voi bac so cap) bi chan."""
    L = BAC[b][2]
    goc = (tc_goc(L), 0.25 * tc_goc(L), 70.0 * (25 + L))
    chuan = [0.6 * goc[0], 0.6 * goc[1], 0.6 * goc[2]]
    if b == 0:
        return tuple(chuan)
    truoc = ngan_sach_bac(b - 1)
    # bac cu tinh theo TONG DA LAM TRON (int) cua tung o - dung cai nguoi choi thuc su mac
    tr = [tong_o(truoc[i], i) for i in range(3)]
    return tuple(min(chuan[i], math.floor(TRAN_BAC_MOI * (goc[i] + HE_SO_NC15 * tr[i]) - goc[i]) - 1)
                 for i in range(3))


def tong_o(T, i):
    """Tong chi so (cot i: 0 tan cong, 1 thu, 2 HP) cua 11 o khi chia T theo LOAI_O, lam tron xuong."""
    return sum(int(T * o[4 + i]) * o[3] for o in LOAI_O)


def chi_so_phu(ma, b):
    if ma == "vu_khi":
        return "Chiêu cùng hệ +%d%%" % min(20, 10 + b)
    if ma == "long_vu":
        return "Tốc chạy +%d" % (1 + b // 2)
    if ma == "gang_dam":
        pn = min(15, 5 + b)   # phan nghin -> "0,5%".."1,5%"
        return "Chí mạng +%d,%d%%" % (pn // 10, pn % 10) if pn % 10 else "Chí mạng +%d%%" % (pn // 10)
    if ma == "phien_da":
        return "Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào"
    return ""


# ---- Chi so engine (data_item.atb) cho dong MOI: noi suy tuyen tinh theo cap tu dong THAT
# (khac 0) cung type -> giu dung thang chi so dang chay (che do cu), khong lam lech can bang.
COT_ATB = ["atb%d" % i for i in range(10)]


def la_khung(r):
    """Dong trang bi chi so 0/1 = khung che tao (chi so do code gan) hoac trang tri."""
    return all(int(r[c]) <= 1 for c in COT_ATB[:6])


def hoi_quy(rows, c):
    """(a, b) cua atb = a + b*cap (binh phuong toi thieu) tren cac dong co atb > 0."""
    pts = [(int(r["level"]), int(r[c])) for r in rows if int(r[c]) > 0]
    if len(pts) < 2:
        return None
    n = len(pts)
    mx = sum(p[0] for p in pts) / n
    my = sum(p[1] for p in pts) / n
    sxx = sum((p[0] - mx) ** 2 for p in pts)
    if sxx == 0:
        return (my, 0.0)
    sxy = sum((p[0] - mx) * (p[1] - my) for p in pts)
    return (my - sxy / sxx * mx, sxy / sxx)


def atb_moi(item_cu, typ, L):
    """atb0..9 cho dong moi type `typ` cap L: chep 'mau' (dong that cap cao nhat) va ngoai suy."""
    that = [r for r in item_cu.values() if int(r["type"]) == typ and not la_khung(r)
            and int(r["level"]) >= 2 and int(r["atb9"]) == 0 and int(r["id"]) < ID_MOI_TU]
    mau = max(that, key=lambda r: (int(r["level"]), int(r["id"])))
    ra = []
    for c in COT_ATB:
        v = int(mau[c])
        if v > 0 and c != "atb9":
            hq = hoi_quy(that, c)
            if hq and hq[1] > 0:
                v = int(round(hq[0] + hq[1] * L))
            else:
                v = int(round(v * max(1, L) / max(1, int(mau["level"])))) if L < int(mau["level"]) else v
            v = max(1, v)
        ra.append(min(v, 32000))
    return mau, ra


# =====================================================================================
# 2. NGUYEN LIEU NANG CAP, NGOC HE, PHAN RA (THIET_KE 3.2, 3.3, 3.5)
# =====================================================================================

# Nguyen lieu phu tu +11 (1 cai moi lan thu) theo nhom bac; ten Pokemon (Stardust, Star Piece...)
NL_PHU = [((0, 1, 2), "bui_sao", "Bụi sao"), ((3, 4, 5), "manh_sao", "Mảnh sao"),
          ((6, 7, 8), "manh_thien_thach", "Mảnh thiên thạch"), ((9, 10), "vay_rong", "Vảy rồng")]


def nl_phu(b):
    for bacs, ma, ten in NL_PHU:
        if b in bacs:
            return ma, ten
    raise ValueError(b)


HE18 = ["Thường", "Lửa", "Nước", "Điện", "Cỏ", "Băng", "Giác đấu", "Độc", "Đất", "Bay", "Siêu linh",
        "Bọ", "Đá", "Ma", "Rồng", "Bóng tối", "Thép", "Tiên"]

# Ngoc he: cap n can (1.45^(n-1)) gio thu nhap o cap nguoi khảm. Mo phong dung cap 60 lam vi du.
CAP_VI_DU_NGOC = 60


def gio_ngoc(n):
    return LD.NGOC_HE_GIA_GIO_PHAN_TRAM / 100.0 * (LD.NGOC_HE_TANG_PHAN_TRAM / 100.0) ** (n - 1)


# =====================================================================================
# 3. VAT PHAM TIEU HAO (THIET_KE 2.2: thuoc + sua do = 20% thu nhap moi gio choi)
# =====================================================================================

# Gia thuoc: moi gio choi dung ~so_dung_gio lo; tong thuoc = TIEU_HAO_PHAN_TRAM% thu nhap.
#   gia = xu_gio(cap giua dai) * phan_ngan_sach / so_dung_gio
# (ma, ten, nhom, mo ta, cap tu, cap den, hieu ung, so dung/gio, phan ngan sach thuoc,
#  tien, nguon, gioi han, anh xa (thuoc:id | item:id | ngoc:id | moi), trang thai)
TIEU_HAO = [
    # --- hoi HP (Potion -> Full Restore)
    ("thuoc_hp_1", "Thuốc hồi máu", "hoi_hp", "Hồi 25% HP tối đa", 1, 20, "hp:25", 10, 0.38, "xu", "cửa hàng, nhiệm vụ ngày", "", "thuoc:1", "co_san"),
    ("thuoc_hp_2", "Siêu thuốc hồi máu", "hoi_hp", "Hồi 35% HP tối đa", 21, 40, "hp:35", 10, 0.38, "xu", "cửa hàng, nhiệm vụ ngày", "", "thuoc:2", "co_san"),
    ("thuoc_hp_3", "Thuốc hồi máu cực mạnh", "hoi_hp", "Hồi 45% HP tối đa", 41, 60, "hp:45", 10, 0.38, "xu", "cửa hàng, nhiệm vụ ngày", "", "thuoc:3", "co_san"),
    ("thuoc_hp_4", "Thuốc hồi máu tối đa", "hoi_hp", "Hồi 60% HP tối đa", 61, 80, "hp:60", 10, 0.38, "xu", "cửa hàng, nhiệm vụ ngày", "", "thuoc:22", "co_san"),
    ("thuoc_hp_5", "Thuốc hồi phục toàn diện", "hoi_hp", "Hồi 80% HP và gỡ mọi trạng thái", 81, 100, "hp:80,trang_thai:tat_ca", 10, 0.38, "xu", "cửa hàng, nhiệm vụ ngày", "", "thuoc:26", "co_san"),
    # --- hoi PP (Ether/Elixir)
    ("thuoc_pp_1", "Thuốc hồi PP (Ether)", "hoi_pp", "Hồi 30% MP (PP chiêu)", 1, 30, "mp:30", 6, 0.20, "xu", "cửa hàng", "", "thuoc:4", "co_san"),
    ("thuoc_pp_2", "Thuốc hồi PP lớn (Ether lớn)", "hoi_pp", "Hồi 45% MP", 31, 60, "mp:45", 6, 0.20, "xu", "cửa hàng", "", "thuoc:5", "co_san"),
    ("thuoc_pp_3", "Thuốc hồi PP tối đa (Ether tối đa)", "hoi_pp", "Hồi 60% MP", 61, 100, "mp:60", 6, 0.20, "xu", "cửa hàng", "", "thuoc:6", "co_san"),
    ("thuoc_pp_doi", "Thuốc hồi PP cả nhóm (Elixir)", "hoi_pp", "Hồi 30% MP cho cả nhóm ở gần", 30, 100, "mp_doi:30", 1, 0.04, "xu", "cửa hàng, boss Gym", "", "thuoc:23", "co_san"),
    ("thuoc_pp_doi_2", "Thuốc hồi PP cả nhóm tối đa (Elixir tối đa)", "hoi_pp", "Hồi 60% MP cho cả nhóm ở gần", 60, 100, "mp_doi:60", 1, 0.05, "luong_khoa", "nhiệm vụ tuần", "2/tuần", "thuoc:24", "co_san"),
    # --- chua trang thai (TrangThaiPokemon: 1 bong, 2 doc, 3 te, 4 ngu, 5 bang)
    ("chua_doc", "Thuốc giải độc", "trang_thai", "Gỡ trạng thái Độc", 1, 100, "go:2", 2, 0.02, "xu", "cửa hàng", "", "moi", "can_code"),
    ("chua_bong", "Thuốc trị bỏng", "trang_thai", "Gỡ trạng thái Bỏng", 1, 100, "go:1", 2, 0.02, "xu", "cửa hàng", "", "moi", "can_code"),
    ("chua_te", "Thuốc trị tê liệt", "trang_thai", "Gỡ trạng thái Tê liệt", 1, 100, "go:3", 2, 0.02, "xu", "cửa hàng", "", "moi", "can_code"),
    ("chua_ngu", "Thuốc đánh thức", "trang_thai", "Gỡ trạng thái Ngủ", 1, 100, "go:4", 2, 0.02, "xu", "cửa hàng", "", "moi", "can_code"),
    ("chua_bang", "Thuốc rã đông", "trang_thai", "Gỡ trạng thái Đóng băng", 1, 100, "go:5", 2, 0.02, "xu", "cửa hàng", "", "moi", "can_code"),
    ("chua_tat_ca", "Thuốc chữa toàn diện", "trang_thai", "Gỡ mọi trạng thái và miễn nhiễm trạng thái trong 5 giây", 30, 100, "go:tat_ca,mien:5", 1, 0.03, "xu", "cửa hàng, boss Gym", "", "moi", "can_code"),
    # --- hoi sinh
    ("hoi_sinh", "Thuốc hồi sinh", "hoi_sinh", "Hồi sinh tại chỗ với 30% HP", 1, 100, "hoi_sinh:30", 0.2, 0.02, "xu", "cửa hàng, nhiệm vụ", "", "thuoc:119", "co_san"),
    ("hoi_sinh_2", "Thuốc hồi sinh tối đa", "hoi_sinh", "Hồi sinh tại chỗ với 100% HP", 40, 100, "hoi_sinh:100", 0.05, 0.01, "luong_khoa", "boss thế giới, nhiệm vụ tuần", "3/tuần", "moi", "can_code"),
    # --- X (buff ngan, trong tran buff +100% cua THIET_KE 1.2 khong ap: day la chi so)
    ("x_tan_cong", "X Tấn công", "x_tang_cuong", "+20% tấn công trong 90 giây", 10, 100, "tc:20,giay:90", 0.5, 0.02, "xu", "cửa hàng", "", "thuoc:130", "co_san"),
    ("x_phong_thu", "X Phòng thủ", "x_tang_cuong", "+20% phòng thủ trong 90 giây", 10, 100, "thu:20,giay:90", 0.5, 0.02, "xu", "cửa hàng", "", "thuoc:129", "co_san"),
    ("x_toc_do", "X Tốc độ", "x_tang_cuong", "+15% tốc đánh trong 90 giây", 10, 100, "toc_danh:15,giay:90", 0.3, 0.02, "xu", "cửa hàng", "", "moi", "can_code"),
    ("x_chinh_xac", "X Chính xác", "x_tang_cuong", "+15% chính xác trong 90 giây", 10, 100, "chinh_xac:15,giay:90", 0.3, 0.02, "xu", "cửa hàng", "", "moi", "can_code"),
    ("x_chi_mang", "Tăng chí mạng", "x_tang_cuong", "+5% chí mạng trong 90 giây", 10, 100, "chi_mang:5,giay:90", 0.3, 0.02, "xu", "cửa hàng", "", "moi", "can_code"),
    ("x_san_trum", "Thuốc săn trùm", "x_tang_cuong", "+30% sát thương lên boss trong 60 giây", 20, 100, "boss:30,giay:60", 0.1, 0.02, "luong_khoa", "boss Gym, nhiệm vụ tuần", "5/tuần", "thuoc:131", "co_san"),
    # --- xua quai (Repel): quai thap hon >= 5 cap khong chu dong danh
    ("xua_quai_1", "Thuốc xua quái", "xua_quai", "Quái thấp hơn 5 cấp không chủ động tấn công trong 5 phút", 5, 100, "xua:5,phut:5", 0.3, 0.01, "xu", "cửa hàng", "", "moi", "can_code"),
    ("xua_quai_2", "Siêu thuốc xua quái", "xua_quai", "Như Thuốc xua quái, kéo dài 10 phút", 20, 100, "xua:5,phut:10", 0.2, 0.01, "xu", "cửa hàng", "", "moi", "can_code"),
    ("xua_quai_3", "Thuốc xua quái tối đa", "xua_quai", "Như Thuốc xua quái, kéo dài 20 phút", 40, 100, "xua:5,phut:20", 0.1, 0.01, "xu", "cửa hàng", "", "moi", "can_code"),
    # --- keo EXP (gioi han boi nang luong huan luyen: KHONG vuot gio 100% cua ngay)
    ("keo_hiem", "Kẹo hiếm", "keo_exp", "Nhận EXP bằng 30 phút đánh quái ở cấp hiện tại; trừ 30 phút năng lượng 100% nên không vượt mức EXP tối đa trong ngày", 1, 100, "exp_phut:30,tru_nang_luong_phut:30", 0, 0.0, "khong_ban", "nhiệm vụ tuần, Pokédex, sự kiện", "2/tuần", "moi", "can_code"),
    # --- da tien hoa (co san 960..967)
    ("da_nuoc", "Đá Nước", "da_tien_hoa", "Tiến hoá Eevee thành Vaporeon", 25, 100, "nhanh:134", 0, 0.0, "khong_ban", "Ngày cộng đồng Eevee, chương J2, Pokédex", "", "item:960", "co_san"),
    ("da_sam", "Đá Sấm", "da_tien_hoa", "Tiến hoá Eevee thành Jolteon", 25, 100, "nhanh:135", 0, 0.0, "khong_ban", "Ngày cộng đồng Eevee, chương K3, Pokédex", "", "item:961", "co_san"),
    ("da_lua", "Đá Lửa", "da_tien_hoa", "Tiến hoá Eevee thành Flareon", 25, 100, "nhanh:136", 0, 0.0, "khong_ban", "Ngày cộng đồng Eevee, chương K7, Pokédex", "", "item:962", "co_san"),
    ("da_mat_troi", "Đá Mặt Trời", "da_tien_hoa", "Tiến hoá Eevee thành Espeon (ban ngày)", 25, 100, "nhanh:196", 0, 0.0, "khong_ban", "Ngày cộng đồng Eevee, Tháp Sprout", "", "item:963", "co_san"),
    ("da_mat_trang", "Đá Mặt Trăng", "da_tien_hoa", "Tiến hoá Eevee thành Umbreon (ban đêm)", 25, 100, "nhanh:197", 0, 0.0, "khong_ban", "Ngày cộng đồng Eevee, Núi Mặt Trăng", "", "item:964", "co_san"),
    ("da_la", "Đá Lá", "da_tien_hoa", "Tiến hoá Eevee thành Leafeon", 25, 100, "nhanh:470", 0, 0.0, "khong_ban", "Ngày cộng đồng Eevee, Rừng Ilex", "", "item:965", "co_san"),
    ("da_bang", "Đá Băng", "da_tien_hoa", "Tiến hoá Eevee thành Glaceon", 25, 100, "nhanh:471", 0, 0.0, "khong_ban", "Ngày cộng đồng Eevee, Hồ Phẫn Nộ", "", "item:966", "co_san"),
    ("ruy_bang_tien", "Ruy Băng Tiên", "da_tien_hoa", "Tiến hoá Eevee thành Sylveon", 25, 100, "nhanh:700", 0, 0.0, "khong_ban", "Ngày cộng đồng Eevee, sự kiện 20/10", "", "item:967", "co_san"),
    # --- qua mong (tu dung khi dieu kien thoa; nguon trang trai tob_fruit + nhiem vu)
    ("qua_oran", "Quả Oran", "qua_mong", "Tự dùng khi HP dưới 50%: hồi 10% HP", 1, 100, "tu_dung:hp50,hp:10", 3, 0.02, "xu", "trang trại, quái đồng cỏ", "", "moi", "can_code"),
    ("qua_sitrus", "Quả Sitrus", "qua_mong", "Tự dùng khi HP dưới 50%: hồi 25% HP", 30, 100, "tu_dung:hp50,hp:25", 2, 0.02, "xu", "trang trại, quái rừng", "", "moi", "can_code"),
    ("qua_lum", "Quả Lum", "qua_mong", "Tự gỡ trạng thái xấu đầu tiên dính phải", 20, 100, "tu_dung:trang_thai,go:tat_ca", 1, 0.02, "xu", "trang trại", "", "moi", "can_code"),
    ("qua_cheri", "Quả Cheri", "qua_mong", "Tự gỡ Tê liệt", 1, 100, "tu_dung:te,go:3", 1, 0.005, "xu", "trang trại", "", "moi", "can_code"),
    ("qua_chesto", "Quả Chesto", "qua_mong", "Tự gỡ Ngủ", 1, 100, "tu_dung:ngu,go:4", 1, 0.005, "xu", "trang trại", "", "moi", "can_code"),
    ("qua_pecha", "Quả Pecha", "qua_mong", "Tự gỡ Độc", 1, 100, "tu_dung:doc,go:2", 1, 0.005, "xu", "trang trại", "", "moi", "can_code"),
    ("qua_rawst", "Quả Rawst", "qua_mong", "Tự gỡ Bỏng", 1, 100, "tu_dung:bong,go:1", 1, 0.005, "xu", "trang trại", "", "moi", "can_code"),
    ("qua_aspear", "Quả Aspear", "qua_mong", "Tự gỡ Đóng băng", 1, 100, "tu_dung:dong_bang,go:5", 1, 0.005, "xu", "trang trại", "", "moi", "can_code"),
    ("qua_leppa", "Quả Leppa", "qua_mong", "Tự dùng khi MP dưới 20%: hồi 15% MP", 1, 100, "tu_dung:mp20,mp:15", 2, 0.01, "xu", "trang trại", "", "moi", "can_code"),
    # --- do giu (held item): KHONG phai tieu hao; hieu ung thay chi so phu cua o Phien da he,
    #     gia tri <= ngan sach o do (test kiem nhom do_giu co o = phien_da).
    ("giu_thuc_an_thua", "Thức ăn thừa", "do_giu", "Hồi 1% HP mỗi 3 giây (thay chỉ số phụ Phiến đá hệ)", 30, 100, "o:phien_da,hoi_hp_3s:1", 0, 0.0, "khong_ban", "boss Gym Celadon", "", "moi", "can_code"),
    ("giu_chuong_vo_so", "Chuông vỏ sò", "do_giu", "Hút 3% sát thương gây ra thành HP", 40, 100, "o:phien_da,hut:3", 0, 0.0, "khong_ban", "boss Gym Cerulean", "", "moi", "can_code"),
    ("giu_mong_nhanh", "Móng vuốt nhanh", "do_giu", "10% cơ hội ra đòn nhanh: giảm 10% thời gian hồi chiêu trong 3 giây", 40, 100, "o:phien_da,toc_danh_ngau_nhien:10", 0, 0.0, "khong_ban", "boss Gym Violet", "", "moi", "can_code"),
    ("giu_bang_tap_trung", "Băng đô tập trung", "do_giu", "10% cơ hội trụ lại với 1 HP khi trúng đòn hạ gục (120 giây mới kích hoạt lại)", 50, 100, "o:phien_da,song_sot:10,hoi:120", 0, 0.0, "khong_ban", "boss Gym Cianwood", "", "moi", "can_code"),
    ("giu_kinh_ngam", "Kính ngắm", "do_giu", "+3% chí mạng", 30, 100, "o:phien_da,chi_mang:3", 0, 0.0, "khong_ban", "boss Gym Saffron", "", "moi", "can_code"),
    ("giu_dai_luc", "Đai lực sĩ", "do_giu", "+4% sát thương đòn đánh thường", 30, 100, "o:phien_da,don_thuong:4", 0, 0.0, "khong_ban", "boss Gym Viridian", "", "moi", "can_code"),
    ("giu_da_vinh_cuu", "Đá vĩnh cửu", "do_giu", "Chặn tiến hoá (giữ dạng hiện tại), không cộng chỉ số", 1, 100, "o:phien_da,chan_tien_hoa:1", 0, 0.0, "khong_ban", "nhiệm vụ Pewter", "", "moi", "can_code"),
    ("giu_trung_may_man", "Trứng may mắn", "do_giu", "+10% EXP (tính chung vào mức tăng EXP tối đa +100%)", 20, 100, "o:phien_da,exp:10", 0, 0.0, "khong_ban", "Pokédex 50 loài", "", "moi", "can_code"),
]

# =====================================================================================
# 4. CHIEU: cay chieu theo dong + giai doan tien hoa (THIET_KE 4.4, 4.5)
# =====================================================================================

# Chi so tuong doi (HP, tan cong, thu) - THIET_KE 4.4
CHI_SO_DONG = {0: (100, 105, 90), 1: (115, 90, 115), 2: (90, 100, 95), 3: (120, 85, 125), 4: (85, 110, 85)}
VAI_TRO_DONG = {0: "Tấn công cận chiến (bỏng, chí mạng)", 1: "Chịu đòn + khống chế (ngủ, độc)",
                2: "Linh hoạt theo nhánh", 3: "Chịu đòn chính (khiên, khiêu khích)",
                4: "Đánh xa, khống chế (tê liệt, lan)"}
# Cap tien hoa (LoaiPokemon): dang 2, dang 3. Eevee: 1 lan tien hoa o 25 (dang 2 = Eevee truong thanh 16).
CAP_TIEN_HOA = {0: (16, 36), 1: (16, 32), 2: (16, 25), 3: (16, 36), 4: (10, 30)}

# Loai chieu theo so hieu (bang_chieu.csv). kieu: thuong | tan_cong | nhieu_don | toi_thuong |
#   bi_dong | buff | khien | hoi | hoi_sinh | khong_che
# (so hieu, kieu, giai doan mo, vai tro, trong so % sat thuong, hoi chieu (giay), PP, trang thai)
KHUNG_CHIEU = {
    0: [(0, "thuong", 1, "tấn công", 1.00, 0.7, 0, ""), (1, "tan_cong", 1, "tấn công", 1.55, 1.3, 35, "bỏng 10%"),
        (2, "tan_cong", 1, "tấn công", 1.85, 1.7, 25, "bỏng 10%"), (3, "nhieu_don", 2, "tấn công lan", 5.50, 6.0, 15, "bỏng 10%"),
        (4, "buff", 2, "né tránh (Màn khói: −20% chính xác quái quanh)", 0, 30.0, 10, ""),
        (5, "bi_dong", 1, "+5..+20% tấn công 30 giây (Múa kiếm)", 0, 60.0, 10, ""),
        (6, "tan_cong", 3, "tấn công", 3.00, 6.0, 15, "bỏng 10%"), (7, "tan_cong", 3, "tấn công (hệ Rồng)", 3.30, 8.0, 15, ""),
        (8, "toi_thuong", 3, "tối thượng", 4.60, 15.0, 5, "bỏng 30%")],
    1: [(0, "thuong", 1, "tấn công", 1.00, 0.7, 0, ""), (1, "tan_cong", 1, "khống chế", 1.50, 1.3, 35, ""),
        (2, "tan_cong", 1, "tấn công", 1.80, 1.7, 25, ""), (3, "nhieu_don", 2, "độc lan", 5.20, 6.0, 15, "độc 15%"),
        (4, "khien", 2, "giảm 30% sát thương nhận 8 giây (Tự vệ)", 0, 40.0, 10, ""),
        (5, "bi_dong", 1, "+5..+20% tấn công và thủ 30 giây (Sinh trưởng)", 0, 60.0, 10, ""),
        (6, "khong_che", 3, "ngủ 15% (Bom hạt)", 2.80, 6.0, 15, "ngủ 15%"), (7, "tan_cong", 3, "tấn công lan", 3.10, 6.0, 15, ""),
        (8, "toi_thuong", 3, "tối thượng + hồi 5% HP", 4.40, 15.0, 5, "")],
    2: [(0, "thuong", 1, "tấn công", 1.00, 0.7, 0, ""), (1, "tan_cong", 1, "tấn công nhanh", 1.50, 1.2, 35, ""),
        (2, "tan_cong", 1, "tấn công (hệ Bóng tối)", 1.80, 1.6, 25, ""), (3, "nhieu_don", 2, "tấn công lan", 5.30, 6.0, 15, ""),
        (4, "hoi", 2, "hồi 10% HP cả nhóm sau 3 giây (Mong ước)", 0, 30.0, 10, ""),
        (5, "bi_dong", 1, "hồi 30% HP bản thân, ngủ 2 giây (Nghỉ ngơi)", 0, 90.0, 5, ""),
        (6, "hoi_sinh", 3, "hồi sinh đồng đội 30% HP", 0, 120.0, 3, ""),
        (7, "buff", 3, "cân bằng HP với mục tiêu (Chia sẻ nỗi đau)", 0, 60.0, 5, ""),
        (8, "tan_cong", 3, "tấn công (hệ Thép)", 3.00, 6.0, 15, ""), (9, "tan_cong", 3, "bùng nổ", 3.30, 8.0, 10, ""),
        (10, "toi_thuong", 3, "tối thượng, sau đó nghỉ 1 giây", 4.80, 15.0, 5, "")],
    3: [(0, "thuong", 1, "tấn công", 1.00, 0.7, 0, ""), (1, "tan_cong", 1, "tấn công", 1.50, 1.3, 35, ""),
        (2, "tan_cong", 1, "làm chậm 15%", 1.75, 1.7, 25, ""), (3, "nhieu_don", 2, "khiêu khích quái quanh", 5.00, 6.0, 15, ""),
        (4, "buff", 2, "+25% tấn công, −15% thủ 20 giây (Phá mai)", 0, 45.0, 10, ""),
        (5, "khien", 1, "khiên 15% HP, giảm 10% sát thương cho cả nhóm 10 giây (Rút vào mai)", 0, 40.0, 10, ""),
        (6, "tan_cong", 3, "tấn công", 2.80, 6.0, 15, ""), (7, "tan_cong", 3, "đẩy lùi", 3.00, 6.0, 15, ""),
        (8, "toi_thuong", 3, "tối thượng", 4.30, 15.0, 5, "")],
    4: [(0, "thuong", 1, "tấn công xa", 1.00, 0.7, 0, "tê 5%"), (1, "tan_cong", 1, "tấn công xa", 1.55, 1.3, 35, "tê 10%"),
        (2, "tan_cong", 1, "tấn công xa", 1.85, 1.7, 25, "tê 10%"), (3, "nhieu_don", 2, "sét lan", 5.60, 6.0, 15, "tê 10%"),
        (4, "buff", 2, "+20% tốc chạy và né 10 giây (Nhanh nhẹn)", 0, 40.0, 10, ""),
        (5, "bi_dong", 1, "đòn Điện kế tiếp +50% (Nạp điện)", 0, 30.0, 10, ""),
        (6, "khong_che", 3, "tê liệt 25% (Lưới điện)", 2.90, 6.0, 15, "tê 25%"), (7, "tan_cong", 3, "tấn công xa", 3.30, 8.0, 15, "tê 10%"),
        (8, "toi_thuong", 3, "tối thượng, tự chịu 10% sát thương", 4.70, 15.0, 5, "tê 30%")],
}
# Moc kiem TTK: (giai doan, cap dai dien, TTK muc tieu giay) - THIET_KE 4.3
MOC_TTK = [(1, 10, 4.0), (2, 30, 4.0), (3, 60, 5.0), (4, 95, 6.0)]
TANG_CAP_CHIEU = 0.02   # +2% sat thuong moi cap chieu (cap 10 = +18%)
# TTK muc tieu theo giai doan x (chi so dong): dang 2/3 nhanh hon 5/15% (cam giac tien hoa)
TTK_THEO_GD = {1: 1.00, 2: 0.92, 3: 0.80}
GCD = 0.7               # don thuong = 7 x 100 ms (CharManager.SKILL_COOLDOWN[.][0])
CUA_SO_GIAY = 60.0
HE_SO_TTK = 2.2          # he so xoay chieu trung binh cua quai_de_xuat (sinh_sql_lau_dai)

# Nhanh Eevee (LoaiPokemon.NHANH_EEVEE) - chieu dac trung thay o 2/8/10 (ten hien thi THIET KE;
# client van dung ten cua o chieu - xem ap_ten_chieu.py).
NHANH_EEVEE = [
    (134, "Vaporeon", "Nước", "Hỗ trợ hồi máu nhóm", "Súng nước", "Vòng nước", "Thủy triều"),
    (135, "Jolteon", "Điện", "Tốc độ", "Phóng điện", "Gai sấm", "Sấm sét"),
    (136, "Flareon", "Lửa", "Bùng nổ", "Nanh lửa", "Hỏa diệm", "Đại hỏa"),
    (196, "Espeon", "Siêu linh", "Phép", "Tâm niệm", "Ảo ảnh tâm linh", "Siêu năng"),
    (197, "Umbreon", "Bóng tối", "Chịu đòn", "Đòn hiểm", "Ánh trăng", "Hồn tối"),
    (470, "Leafeon", "Cỏ", "Khống chế", "Lưỡi lá", "Lá dao", "Tia mặt trời"),
    (471, "Glaceon", "Băng", "Khống chế", "Gió băng", "Tia băng", "Bão tuyết"),
    (700, "Sylveon", "Tiên", "Hỗ trợ", "Hôn tiên", "Khí tiên", "Ánh trăng tiên"),
]

# =====================================================================================
# 5. QUAI, ALPHA, GYM, BOSS THE GIOI (THIET_KE 4.6)
# =====================================================================================

MOI_TRUONG = {  # he chinh -> vung sinh thai
    0: "Đồng cỏ", 4: "Rừng", 11: "Rừng", 7: "Rừng", 12: "Hang", 8: "Hang", 13: "Tháp", 10: "Tháp",
    2: "Bờ biển", 9: "Bờ biển", 1: "Núi lửa", 5: "Hồ", 3: "Đồng cỏ", 6: "Hang", 14: "Núi", 15: "Rừng đêm",
    16: "Hang", 17: "Đồng cỏ"}

# De xuat loai moi cho dai thieu (nam 2: Hoenn). Can sprite PMD dong goi cho client (tai_sprite_pmd.py).
# (dex, ten, he1, he2, cap)
DE_XUAT_QUAI = [
    (261, "Poochyena", 15, -1, 80), (262, "Mightyena", 15, -1, 86), (309, "Electrike", 3, -1, 81),
    (310, "Manectric", 3, -1, 88), (322, "Numel", 1, 8, 82), (323, "Camerupt", 1, 8, 89),
    (333, "Swablu", 0, 9, 83), (334, "Altaria", 14, 9, 94), (304, "Aron", 16, 12, 85),
    (305, "Lairon", 16, 12, 90), (306, "Aggron", 16, 12, 97), (318, "Carvanha", 2, 15, 84),
    (321, "Wailord", 2, -1, 91), (343, "Baltoy", 8, 10, 86), (344, "Claydol", 8, 10, 93),
    (337, "Lunatone", 12, 10, 87), (354, "Banette", 13, -1, 92), (357, "Tropius", 4, 9, 88),
    (359, "Absol", 15, -1, 95), (361, "Snorunt", 5, -1, 87), (362, "Glalie", 5, -1, 94),
    (363, "Spheal", 5, 2, 89), (365, "Walrein", 5, 2, 98), (371, "Bagon", 14, -1, 90),
    (372, "Shelgon", 14, -1, 95), (375, "Metang", 16, 10, 96), (376, "Metagross", 16, 10, 100),
    (335, "Zangoose", 0, -1, 91), (326, "Grumpig", 10, -1, 92), (330, "Flygon", 8, 14, 99),
    (346, "Cradily", 12, 4, 97), (348, "Armaldo", 12, 11, 98), (279, "Pelipper", 2, 9, 85),
    (288, "Vigoroth", 0, -1, 83), (297, "Hariyama", 6, -1, 93), (302, "Sableye", 15, 13, 88),
]

# Template quai MOI (data_monster) cho dai 86-100 + huyen thoai nam 2. Id PHAI lien tiep sau id
# lon nhat dang co (138): client cap mang Res.monsterTemplates theo SO LUONG template (goi 26,
# readUnsignedByte) roi tra theo id -> co lo hong la tran mang. Tong <= 255 (1 byte).
ID_QUAI_MOI_TU = 139
ID_QUAI_MOI_DEN = 199
QUAI_MAU_THUONG = 118      # Galvantula: cot hien thi/di chuyen cho quai thuong moi (khong phai chi so)
QUAI_MAU_BOSS = 82         # Zapdos: cot hien thi cho boss moi
# (dex, ten, he1, he2, tuan dot kich nam 2)
HUYEN_THOAI_MOI = [(144, "Articuno", 5, 9, 9), (243, "Raikou", 3, -1, 20), (245, "Suicune", 2, -1, 28),
                   (249, "Lugia", 10, 9, 36)]

# 16 Gym: (ma chuong buoc Gym trong nhiem_vu_moi, ten huy hieu, id template quai dung hinh, he Gym)
GYM = [
    ("C06.4", "Huy hiệu Đá (Pewter)", 74, "Đá"), ("C10.4", "Huy hiệu Thác (Cerulean)", 38, "Nước"),
    ("C12.4", "Huy hiệu Sấm (Vermilion)", 137, "Điện"), ("C20.4", "Huy hiệu Cầu Vồng (Celadon)", 58, "Cỏ"),
    ("C22.4", "Huy hiệu Đầm Lầy (Saffron)", 67, "Đất"), ("C24.4", "Huy hiệu Linh Hồn (Fuchsia)", 93, "Ma"),
    ("C27.4", "Huy hiệu Núi Lửa (Cinnabar)", 76, "Lửa"), ("C28.4", "Huy hiệu Đất (Viridian)", 60, "Đất"),
    ("C31.4", "Huy hiệu Gió (Violet)", 22, "Bay"), ("C33.4", "Huy hiệu Tổ Ong (Azalea)", 72, "Bọ"),
    ("C36.4", "Huy hiệu Đồng Bằng (Goldenrod)", 33, "Thường"), ("C37.4", "Huy hiệu Sương (Ecruteak)", 94, "Ma"),
    ("C39.4", "Huy hiệu Khoáng (Olivine)", 46, "Thép"), ("C40.4", "Huy hiệu Bão (Cianwood)", 65, "Giác đấu"),
    ("C44.4", "Huy hiệu Băng (Mahogany)", 90, "Băng"), ("C48.4", "Huy hiệu Rồng (Blackthorn)", 39, "Rồng"),
]
TTK_GYM_GIAY = 105        # 90-120 giay danh mot minh dung cap, do +6
TTK_BOSS_PHUT = 10        # 8-12 phut voi 20 nguoi
NGUOI_BOSS = 20

# Boss the gioi: huyen thoai dot kich theo lich (template co san) + boss xoay vong tuan thuong.
# (ten, id template, he, nam: tuan dot kich nam 1 / ten nam 2 (can sprite))
HUYEN_THOAI = {9: ("Zapdos", 82, "Điện"), 20: ("Entei", 138, "Lửa"), 28: ("Darkrai", 116, "Bóng tối"),
               36: ("Rayquaza", 120, "Rồng"), 48: ("Groudon & Kyogre", 130, "Đất/Nước")}
HUYEN_THOAI_NAM2 = {9: ("Articuno", 144), 20: ("Raikou", 243), 28: ("Suicune", 245), 36: ("Lugia", 249),
                    48: ("Kyogre & Groudon", 382)}
BOSS_XOAY_VONG = [("Gyarados", 38, "Nước"), ("Salamence", 39, "Rồng"), ("Sudowoodo", 43, "Đá"),
                  ("Abomasnow", 90, "Băng"), ("Gengar", 93, "Ma"), ("Dusknoir", 94, "Ma"),
                  ("Serperior", 91, "Cỏ"), ("Ursaring", 115, "Thường"), ("Gourgeist", 117, "Ma"),
                  ("Bronzong", 36, "Thép"), ("Lopunny", 113, "Thường")]
TOKEN_HUYEN_THOAI = {"Zapdos": "Lông vũ Zapdos", "Entei": "Nanh lửa Entei", "Darkrai": "Mảnh bóng tối",
                     "Rayquaza": "Vảy Rayquaza", "Groudon & Kyogre": "Ngọc đỏ Groudon",
                     "Articuno": "Lông vũ Articuno", "Raikou": "Lông tơ Raikou", "Suicune": "Pha lê Suicune",
                     "Lugia": "Lông bạc Lugia", "Kyogre & Groudon": "Ngọc lam Kyogre"}

# =====================================================================================
# 6. ROI (THIET_KE 2.3, 4.6; data_kinh_te_lau_dai)
# =====================================================================================

PPM_TRANG_BI = 150                      # roi.trang_bi.ppm (giu)
PPM_DA = LD.DA_ROI_PHAN_NGHIN * 1000    # roi.da_nang_cap.phan_nghin = 15 -> 15000 ppm
PPM_NL_PHU = 4000                       # 0,4%/quai nguyen lieu phu cua bac dai (cho +11..+15)
PPM_THUOC = 20000                       # 2% thuoc HP cua dai
NHAN_ALPHA = 3
PPM_HOAN_MY_ALPHA = 20000               # 2%
NGUON_HOAN_MY = ("alpha", "gym", "boss_the_gioi")

# =====================================================================================
# 7. SU KIEN 52 TUAN + TRANG TRI THEO MUA
# =====================================================================================

# Vat pham su kien co san (thuoc:id = data_potion, item:id = data_item) theo tu khoa ten tuan.
SK_CO_SAN = [
    ("Tết dương lịch", [("Kẹo", "thuoc:137"), ("Hộp quà", "thuoc:139")]),
    ("gói bánh", [("Lá dong", "thuoc:150"), ("Gạo nếp", "thuoc:148"), ("Bánh chưng", "thuoc:151")]),
    ("Tết Nguyên Đán", [("Bao lì xì", "thuoc:159"), ("Bánh tét", "thuoc:152"), ("Pháo hoa tết", "thuoc:155")]),
    ("Valentine", [("Hoa hồng", "thuoc:58"), ("Vảy trái tim", "thuoc:98")]),
    ("8/3", [("Nhánh hoa hồng", "thuoc:106"), ("Hoa hồng đỏ", "thuoc:107")]),
    ("Giỗ Tổ", [("Đậu xanh", "thuoc:122"), ("Pháo hoa", "thuoc:99")]),
    ("Hè", [("Dưa Hấu", "thuoc:145"), ("Kem", "thuoc:146")]),
    ("Vu Lan", [("Nhánh hoa hồng vàng", "thuoc:106")]),
    ("Trung Thu", [("Bánh dẻo", "thuoc:124"), ("Bánh nướng", "thuoc:125"), ("Nến", "thuoc:136")]),
    ("Halloween", [("Bí ngô", "thuoc:138"), ("Hộp quà halloween", "thuoc:140")]),
    ("Giáng sinh", [("Hộp quà", "thuoc:139"), ("Sao vàng", "thuoc:144")]),
    ("Thiếu nhi", [("Kẹo", "thuoc:137")]),
]
# Vat pham su kien MOI (data_item type 28, khoa, het han khi su kien ket thuc) theo loai tuan.
SK_MOI_THEO_LOAI = {
    "tuan_he": "Tinh thể hệ", "cong_dong": "Huy hiệu ngày cộng đồng", "pokemon": "Tem nghiên cứu",
    "pvp": "Huy chương mùa giải", "chien_vung": "Cờ vùng", "mua": "Phiếu lễ hội mùa", "thuong": "Tem tuần",
}

# Ten vat pham "Qua le ..." qua dai cho o vat pham J2ME (do_rong_j2me.py, nen <= ~120 px).
TEN_QUA_LE_GON = {"Quà lễ Phụ nữ Việt Nam 20/10": "Quà lễ Phụ nữ 20/10"}

SK_SO_LUONG_TUAN = 20      # moi vat pham su kien: toi da 20 cai / tuan / nhan vat

# Mua: (ma, ten, ngay mo, tran) - lay tu TRAN_CAP_THEO_NGAY
TEN_MUA = ["Mùa Kanto", "Mùa Johto giữa", "Mùa Johto cuối", "Mùa Đường Victory", "Mùa Hoenn 1",
           "Mùa Hoenn 2"]
LINH_VAT_MUA = ["Pikachu", "Togepi", "Ho-Oh", "Lugia", "Latias", "Rayquaza"]

# =====================================================================================
# DOC DU LIEU
# =====================================================================================


def doc_csv(p):
    with open(p, encoding="utf-8") as f:
        return list(csv.DictReader(f))


def nap():
    cot, dong = LD.doc_dump()
    item = {int(r["id"]): r for r in LD.bang(cot, dong, "data_item")}
    # Bo dong do chinh script nay sinh (neu dump da duoc sinh lai) -> dau ra on dinh.
    item = {k: v for k, v in item.items() if not (ID_MOI_TU <= k <= ID_MOI_DEN)}
    quai = {int(r["id"]): r for r in LD.bang(cot, dong, "data_monster")}
    quai = {k: v for k, v in quai.items() if not (ID_QUAI_MOI_TU <= k <= ID_QUAI_MOI_DEN)}
    thuoc = {int(r["id"]): r for r in LD.bang(cot, dong, "data_potion")}
    return item, quai, thuoc


# =====================================================================================
# SINH
# =====================================================================================


class IdMoi:
    def __init__(self):
        self.k = ID_MOI_TU

    def lay(self):
        v = self.k
        self.k += 1
        assert v <= ID_MOI_DEN, "het dai id noi dung"
        return v


def sinh_trang_bi(item, ids):
    """-> (catalog rows, data_item rows moi, bo rows)."""
    cat, moi = [], []

    def tim_co_san(typ, ten_bac, L, clazz=None):
        ung = [r for r in item.values() if int(r["type"]) == typ and r["name"].strip().endswith(ten_bac)
               and not la_khung(r) and int(r["atb9"]) == 0 and (clazz is None or int(r["clazz"]) == clazz)]
        if not ung:
            ung = [r for r in item.values() if int(r["type"]) == typ and r["name"].strip().endswith(ten_bac)
                   and int(r["atb9"]) == 0 and (clazz is None or int(r["clazz"]) == clazz)]
            khung = True
        else:
            khung = False
        if not ung:
            return None, False
        r = min(ung, key=lambda r: (abs(int(r["level"]) - L), int(r["id"])))
        return r, khung

    def them_moi(typ, ten, L, b, clazz=-1, so_gioi=1):
        mau, atb = atb_moi(item, typ, L)
        out = []
        styles = [int(mau["style"])]
        if so_gioi == 2:
            cap = [r for r in item.values() if int(r["type"]) == typ and int(r["level"]) == int(mau["level"])
                   and not la_khung(r) and int(r["atb9"]) == 0]
            styles = sorted({int(r["style"]) for r in cap}, reverse=True)[:2] or styles
        for st in styles:
            i = ids.lay()
            out.append({"id": i, "name": ten, "type": typ, "style": st, "he": int(mau["he"]), "gender": 0,
                        "level": L, "durable": int(mau["durable"]) + 10 * (b - 7 if b > 7 else 0),
                        **{c: atb[k] for k, c in enumerate(COT_ATB)},
                        "price": LD.gia_bac(b), "clazz": clazz, "xstart": int(mau["xstart"]),
                        "ystart": int(mau["ystart"]), "colortype": int(mau["colortype"]), "nloan": 0,
                        "idUpLevel": -1, "namein": ten, "ideff": -1, "pricecu": 0})
        moi.extend(out)
        return out[0]["id"], [o["id"] for o in out]

    for b, ten_bac, L, ngay in BAC:
        tc, thu, hp = ngan_sach_bac(b)
        for (ma, typ, ten, so_o, s_tc, s_thu, s_hp, phu, nguon) in LOAI_O:
            ds = [(c, t, n, he) for (c, t, n, he) in VU_KHI] if ma == "vu_khi" else [(None, typ, ten, "")]
            for (clazz, t, ten_o, he) in ds:
                ten_mon = "%s %s" % (ten_o, ten_bac)
                r, khung = tim_co_san(t, ten_bac, L, clazz)
                if r is None or b >= 9:
                    id_chinh, id_ds = them_moi(t, ten_mon, L, b, clazz if clazz is not None else -1,
                                               so_gioi=2 if t in (0, 1, 2) else 1)
                    trang_thai, nguon_cs = "moi", "sinh mới (id %s)" % "/".join(map(str, id_ds))
                    cap_item = L
                else:
                    id_chinh, trang_thai = int(r["id"]), ("khung_che_tao" if khung else "co_san")
                    nguon_cs = "data_item có sẵn"
                    cap_item = int(r["level"])
                cat.append({
                    "ma": "%s_%s%s" % (ma, b, "" if clazz is None else "_c%d" % clazz),
                    "bac": b, "ten_bac": ten_bac, "o": ma, "type_item": t,
                    "dong": "" if clazz is None else DONG[clazz], "ten": ten_mon, "id_item": id_chinh,
                    "trang_thai": trang_thai, "cap_yeu_cau": L, "cap_item": cap_item, "ngay_mo": ngay,
                    "tran_cap_khi_mo": tran_cap_ngay(ngay), "so_o": so_o,
                    "tan_cong": int(tc * s_tc), "phong_thu": int(thu * s_thu),
                    "hp": int(hp * s_hp), "chi_so_phu": chi_so_phu(ma, b), "he": he,
                    "gia_xu": LD.gia_bac(b), "ban_npc_xu": int(LD.gia_bac(b) * 0.05),
                    "phan_ra": "%d Đá nâng cấp + %d xu" % (LD.DA_TU_PHAN_RA, int(LD.gia_bac(b) * 0.10)),
                    "nguon": nguon, "ghi_chu": nguon_cs})
    bo = []
    for b, ten_bac, L, ngay in BAC:
        for (so, cs, pt) in LD.BO_TB:
            bo.append({"bac": b, "ten_bo": TEN_BO[b], "vung": VUNG_BO[b], "so_mon": so, "chi_so": cs,
                       "phan_tram": pt, "mo_ta": "%s: mặc %d món cùng bậc %s được +%d%% %s" % (TEN_BO[b], so, ten_bac, pt, cs if cs == "HP" else cs.lower())})
    return cat, moi, bo


def sinh_vat_lieu(ids, item_moi):
    """Nguyen lieu nang cap theo bac + bua + ngoc he + phan ra + che tao. Them item type 28 moi."""
    mau28 = {"type": 28, "style": 0, "he": 0, "gender": 0, "level": 1, "durable": 0,
             **{c: 0 for c in COT_ATB}, "price": 0, "clazz": -1, "ystart": 0, "colortype": -1,
             "nloan": 0, "idUpLevel": -1, "ideff": -1, "pricecu": 0}
    ten_id = {}

    def item28(ten, icon):
        if ten in ten_id:
            return ten_id[ten]
        i = ids.lay()
        d = dict(mau28)
        d.update({"id": i, "name": ten, "namein": ten, "xstart": icon_nd(khoa_ten(ten), icon)})
        item_moi.append(d)
        ten_id[ten] = i
        return i

    icon_nl = {"bui_sao": 14125, "manh_sao": 14137, "manh_thien_thach": 14139, "vay_rong": 14122}
    nl = []
    for bacs, ma, ten in NL_PHU:
        i = item28(ten, icon_nl[ma])
        nl.append({"ma": ma, "ten": ten, "nhom": "nguyen_lieu_phu", "anh_xa": "item:%d" % i,
                   "dung_cho": "Nâng cấp +11 đến +15 cho đồ bậc %s (1 cái mỗi lần thử)" % ", ".join(BAC[b][1] for b in bacs),
                   "nguon": "quái ở dải cấp của bậc rơi (0,4%), phân rã đồ bậc đó, chế tạo",
                   "gia": 0, "tien": "khong_ban"})
    i = item28("Mảnh tinh hoa", 14138)
    nl.append({"ma": "manh_tinh_hoa", "ten": "Mảnh tinh hoa", "nhom": "nguyen_lieu_hoan_my", "anh_xa": "item:%d" % i,
               "dung_cho": "Chuyển đồ Hoàn mỹ sang bậc mới (cần 3 mảnh)", "nguon": "phân rã đồ Hoàn mỹ",
               "gia": 0, "tien": "khong_ban"})
    nl.append({"ma": "da_nang_cap", "ten": "Đá nâng cấp", "nhom": "nguyen_lieu_chinh", "anh_xa": "ngoc:%d" % NGOC_DA_NANG_CAP,
               "dung_cho": "Mọi lần nâng cấp: lên +1 đến +5 cần 1 viên, +6 đến +10 cần 2 viên, +11 đến +15 cần 4 viên",
               "nguon": "quái chênh tối đa 5 cấp rơi 1,5%, quái tinh anh chắc chắn rơi, phân rã 1 món được 2 viên",
               "gia": 0, "tien": "khong_ban"})
    nl.append({"ma": "bua_bao_ho", "ten": "Bùa bảo hộ", "nhom": "bua", "anh_xa": "ngoc:%s" % "/".join(map(str, NGOC_BUA[:3])),
               "dung_cho": "Giữ cấp khi nâng cấp +7 đến +10 thất bại", "nguon": "cửa hàng lượng khoá",
               "gia": 10, "tien": "luong_khoa"})
    nl.append({"ma": "bua_bao_ho_cao", "ten": "Bùa bảo hộ cao cấp", "nhom": "bua", "anh_xa": "ngoc:%s" % "/".join(map(str, NGOC_BUA[3:])),
               "dung_cho": "Giữ cấp khi nâng cấp +11 đến +15 thất bại", "nguon": "cửa hàng lượng khoá",
               "gia": 25, "tien": "luong_khoa"})
    # Vat lieu theo bac x +k
    vl = []
    for b, ten_bac, L, ngay in BAC:
        ma_phu, ten_phu = nl_phu(b)
        for k in range(1, 16):
            vl.append({"bac": b, "ten_bac": ten_bac, "cong": k, "ti_le": LD.TI_LE_NC[k - 1],
                       "da": LD.DA_NC[k - 1], "nguyen_lieu_phu": ten_phu if k >= 11 else "",
                       "so_nl_phu": 1 if k >= 11 else 0, "xu": LD.chi_phi_nc(b, k),
                       "bua_luong_khoa": LD.BUA_BAO_HO_LUONG_KHOA[k - 1],
                       "ky_vong_lan": round(LD.ky_vong_nc(k), 2)})
    # Ngoc he 18 x 10
    ngoc = []
    for h, ten_he in enumerate(HE18):
        for n in range(1, LD.NGOC_HE_CAP_TOI_DA + 1):
            g = gio_ngoc(n)
            ngoc.append({"he": h, "ten": "Ngọc hệ %s cấp %d" % (ten_he, n), "cap": n, "chi_so_phan_tram": n,
                         "gio_thu_nhap": round(g, 3), "xu_vi_du_cap60": int(round(g * xu_gio(CAP_VI_DU_NGOC))),
                         "phan_ra_da": n, "anh_xa": "moi", "trang_thai": "can_code",
                         "icon": icon_nd("ngoc_he_%d" % h)})
    # Phan ra
    pr = []
    for b, ten_bac, L, ngay in BAC:
        pr.append({"nguon": "Đồ bậc %s (trùng/cũ)" % ten_bac, "bac": b, "da": LD.DA_TU_PHAN_RA,
                   "xu": int(LD.gia_bac(b) * 0.10), "them": ""})
        pr.append({"nguon": "Đồ Hoàn mỹ bậc %s" % ten_bac, "bac": b, "da": LD.DA_TU_PHAN_RA,
                   "xu": int(LD.gia_bac(b) * 0.10), "them": "1 Mảnh tinh hoa + 1 %s" % nl_phu(b)[1]})
    for n in range(1, 11):
        pr.append({"nguon": "Ngọc hệ cấp %d" % n, "bac": -1, "da": n, "xu": 0, "them": ""})
    return nl, vl, ngoc, pr, ten_id


def sinh_tieu_hao():
    ra = []
    for (ma, ten, nhom, mo_ta, c1, c2, hu, so_gio, phan, tien, nguon, gh, ax, tt) in TIEU_HAO:
        giua = (c1 + c2) // 2
        if tien == "xu" and so_gio > 0:
            # nhom thuoc chia ngan sach TIEU_HAO; cac nhom phu (trang thai, x, xua, qua) lay phan rieng
            gia = int(round(xu_gio(giua) * LD.TIEU_HAO_PHAN_TRAM / 100.0 * phan / so_gio))
        elif tien == "luong_khoa":
            gia = {"thuoc_pp_doi_2": 5, "hoi_sinh_2": 5, "x_san_trum": 3}.get(ma, 5)
        else:
            gia = 0
        ra.append({"ma": ma, "ten": ten, "nhom": nhom, "mo_ta": mo_ta, "cap_tu": c1, "cap_den": c2,
                   "hieu_ung": hu, "so_dung_moi_gio": so_gio, "phan_ngan_sach": phan, "gia": gia, "tien": tien,
                   "gia_tren_thu_nhap_gio": round(gia / xu_gio(giua), 4) if tien == "xu" else 0,
                   "nguon": nguon, "gioi_han": gh, "anh_xa": ax, "trang_thai": tt,
                   "icon": icon_nd(ma) if tt == "can_code" else 0})
    return ra


def cap_moc(c, gd, L):
    """Cap kiem TTK cua giai doan gd: truoc khi mo giai doan sau (Pichu tien hoa som o 10/30)."""
    a, s = CAP_TIEN_HOA[c]
    return min(L, a - 1) if gd == 1 else (min(L, s - 1) if gd == 2 else L)


def xoay_chieu(ds):
    """He so sat thuong / giay (1 = 100% tan cong / giay) cua xoay chieu tham lam trong 60 giay.
    ds = [(phan_tram, hoi_chieu_giay)] cac chieu sat thuong da mo."""
    san = [0.0] * len(ds)
    t, tong = 0.0, 0.0
    while t < CUA_SO_GIAY - 1e-9:
        best = -1
        for i, (pt, cd) in enumerate(ds):
            if san[i] <= t + 1e-9 and (best < 0 or pt > ds[best][0]):
                best = i
        if best >= 0:
            tong += ds[best][0] / 100.0
            san[best] = t + max(ds[best][1], GCD)
        t += GCD
    return tong / CUA_SO_GIAY


def sinh_chieu():
    bang = doc_csv(F_CHIEU)
    ten = {}
    for r in bang:
        ten[(int(r["class"]), int(r["so_hieu_skill"]))] = (r["ten_moi"], r["he"])
    ra, moc = [], []
    for c in range(5):
        a, s = CAP_TIEN_HOA[c]
        hp, atk, thu = CHI_SO_DONG[c]
        # He so sat thuong cua dong de chi so suc manh (HP x Tan cong x Thu x he so) bang nhau.
        chi_so = hp * atk * thu / 1e6
        nhan = 1.0 / chi_so
        dong = []
        for (so, kieu, gd, vai, w, cd, pp, tt) in KHUNG_CHIEU[c]:
            mo = {1: 1 if so == 0 else (1 + 3 * so if so <= 2 else 3), 2: a, 3: s}[gd]
            if so == 5 and gd == 1:
                mo = 6
            cap_k = []
            for k in range(1, 11):
                if k == 10:
                    cap_k.append(100 if kieu == "toi_thuong" else 95)
                else:
                    cap_k.append(int(round(mo + (90 - mo) * (k - 1) / 8.0)))
            dong.append([so, kieu, gd, vai, w, cd, pp, tt, mo, cap_k])
        # He so tung giai doan (s1, s2, s3) chon LAN LUOT (chia doi) de TTK o moc giai doan do =
        # muc tieu x chi_so x TTK_THEO_GD[gd]: chieu giai doan sau chi lam nhanh hon mot chut
        # (cam giac manh len khi tien hoa) ma suc manh cac dong van bang nhau o moi moc.
        he = {1: 1.0, 2: 1.0, 3: 1.0}

        def pt_chieu(w, gd, k):
            return int(round(w * he[gd] * 100)) * (1 + TANG_CAP_CHIEU * (k - 1))

        def r_tai(L):
            ds = []
            for (so, kieu, gd, vai, w, cd, pp, tt, mo, cap_k) in dong:
                if w <= 0 or L < mo:
                    continue
                k = max(i + 1 for i, cl in enumerate(cap_k) if cl <= L)
                ds.append((pt_chieu(w, gd, k), cd))
            return xoay_chieu(ds)

        def ttk_tai(L):
            hpq, atkq, thuq = LD.quai_de_xuat(L)
            return hpq / ((LD.ngan_sach_tc(L) * atk / 100.0 - thuq) * 1.1 * r_tai(L))

        for (gd, L0, ttk_mt) in MOC_TTK[:3]:
            L = cap_moc(c, gd, L0)
            dich = ttk_mt * chi_so * TTK_THEO_GD[gd]
            lo, hi = 0.05, 20.0
            for _ in range(60):
                he[gd] = (lo + hi) / 2
                if ttk_tai(L) > dich:
                    lo = he[gd]
                else:
                    hi = he[gd]
            he[gd] = (lo + hi) / 2
        for (so, kieu, gd, vai, w, cd, pp, tt, mo, cap_k) in dong:
            t, h = ten[(c, so)]
            pt = [int(round(pt_chieu(w, gd, k))) if w > 0 else 0 for k in range(1, 11)]
            pt1 = pt[0]
            so_don = 5 if kieu == "nhieu_don" else 1
            ra.append({"dong": c, "ten_dong": DONG[c], "so_hieu": so, "ten": t, "he": h, "kieu": kieu,
                       "vai_tro": vai, "giai_doan": gd, "cap_mo": mo,
                       "cap_moi_cap_chieu": "/".join(map(str, cap_k)),
                       "phan_tram_cap1": pt[0], "phan_tram_cap10": pt[9], "so_don": so_don,
                       "phan_tram_moi_don_cap10": int(round(pt[9] / so_don)) if pt[9] else 0,
                       "hoi_chieu_giay": cd, "pp": pp, "mp_phan_tram": {0: 0, 35: 2, 25: 3, 15: 6, 10: 8, 5: 12, 3: 15}.get(pp, 5),
                       "trang_thai": tt})
        for (gd, L0, ttk_mt) in MOC_TTK:
            L = cap_moc(c, gd, L0)
            R = r_tai(L)
            hpq, atkq, thuq = LD.quai_de_xuat(L)
            a_ng = LD.ngan_sach_tc(L) * atk / 100.0
            ttk = hpq / ((a_ng - thuq) * 1.1 * R)
            moc.append({"dong": c, "ten_dong": DONG[c], "giai_doan": gd, "cap": L, "he_so_xoay_chieu": round(R, 3),
                        "ttk_giay": round(ttk, 2), "ttk_muc_tieu": ttk_mt, "ti_le": round(ttk / ttk_mt, 3),
                        "chi_so_suc_manh": round(chi_so * atk / 100.0 * R / HE_SO_TTK * 100.0 / atk * atk / 100.0, 3)})
    nhanh = []
    for (dex, t, h, vai, s2, s8, s10) in NHANH_EEVEE:
        nhanh.append({"dex": dex, "ten": t, "he": h, "vai_tro": vai, "chieu_o2": s2, "chieu_o8": s8,
                      "chieu_o10": s10, "cap_mo": CAP_TIEN_HOA[2][1],
                      "ghi_chu": "Đổi hệ ô 2/8/10 theo nhánh (STAB); số liệu giữ như cây Eevee"})
    return ra, moc, nhanh


def sinh_quai(quai, ten_id, ids, item_moi):
    bq = {int(r["id_quai"]): r for r in doc_csv(F_QUAI)}
    thuong = [i for i in LD.QUAI_DA_DAT if bq.get(i, {}).get("phan_loai") == "quai"]
    moi = quai_moi(quai)
    dai = []
    for d in range(20):
        c1, c2 = 5 * d + 1, 5 * d + 5
        loai = sorted([i for i in thuong if c1 - 2 <= int(quai[i]["level"]) <= c2 + 2],
                      key=lambda i: (int(quai[i]["level"]), i))
        he = sorted({int(quai[i]["he"]) for i in loai} | {int(quai[i]["he2"]) for i in loai if int(quai[i]["he2"]) >= 0})
        de_xuat = [x for x in DE_XUAT_QUAI if c1 - 2 <= x[4] <= c2 + 2]
        tpl = [q for q in moi if q["phan_loai"] == "quai" and c1 - 2 <= q["level"] <= c2 + 2]
        he_tong = set(he) | {q["he"] for q in tpl} | {q["he2"] for q in tpl if q["he2"] >= 0}
        du = len(loai) + len(tpl) >= 3 and len(he_tong) >= 2
        dai.append({"dai": "%d-%d" % (c1, c2), "cap_tu": c1, "cap_den": c2,
                    "loai_co_san": "|".join("%d:%s(%d)" % (i, quai[i]["name"], int(quai[i]["level"])) for i in loai),
                    "so_loai": len(loai), "so_he": len(he), "he": "|".join(HE18[h] for h in he),
                    "moi_truong": "|".join(sorted({MOI_TRUONG.get(int(quai[i]["he"]), "Đồng cỏ") for i in loai})),
                    "du": 1 if du else 0,
                    "de_xuat_moi": "|".join("%d:%s(%s%s,%d)" % (x[0], x[1], HE18[x[2]], "/" + HE18[x[3]] if x[3] >= 0 else "", x[4]) for x in de_xuat),
                    "so_de_xuat": len(de_xuat),
                    "template_nam2": "|".join("%d:%s(%d)" % (q["id"], q["name"], q["level"]) for q in tpl),
                    "so_template_nam2": len(tpl)})
    alpha = []
    for i in thuong:
        L = int(quai[i]["level"])
        hp, at, th = LD.quai_de_xuat(L)
        alpha.append({"id_goc": i, "ten": "Alpha %s" % quai[i]["name"], "cap": L, "ti_le_xuat_hien": "1/50",
                      "kich_thuoc_phan_tram": 130, "hp": hp * 4, "tan_cong": int(round(at * 1.3)), "phong_thu": th,
                      "exp": LD.MOB_EXP[L] * NHAN_ALPHA, "nhan_roi": NHAN_ALPHA,
                      "ttk_giay": round(hp * 4 / ((LD.ngan_sach_tc(L) - th) * 1.1 * HE_SO_TTK), 1)})
    # Gym
    nv = {r["ma"]: r for r in doc_csv(F_NV)}
    gym = []
    token_gym = ids_item28(ten_id, ids, item_moi, "Huy chương Gym", 14124)
    for k, (ma, huy, tpl, he) in enumerate(GYM, 1):
        # Buoc Gym tim theo huy hieu (chuong nay co 6-10 buoc, ma buoc Gym khong con co dinh la .4;
        # tools/pokemon/sinh_nhiem_vu.py). Giu ma trong GYM lam du phong.
        r = next((x for x in nv.values() if x.get("huy_hieu") == huy), None) or nv[ma]
        ma = r["ma"]
        L = int(r["cap_toi_thieu"])
        hpq, atq, thq = LD.quai_de_xuat(L)
        dps = (LD.ngan_sach_tc(L) - thq) * 1.1 * HE_SO_TTK
        b = max(x[0] for x in BAC if x[2] <= max(1, L))
        gym.append({"stt": k, "ma_nhiem_vu": ma, "huy_hieu": huy, "dia_danh": r["dia_danh"], "he": he,
                    "cap": L, "template_quai": tpl, "ten_boss": "Trưởng Gym %s: %s" % (r["dia_danh"], quai[tpl]["name"]),
                    "hp": int(round(TTK_GYM_GIAY * dps / 100.0) * 100), "tan_cong": int(round(atq * 1.5)),
                    "phong_thu": int(round(thq * 1.2)), "ttk_giay": TTK_GYM_GIAY, "no_sau_phut": 5,
                    "thuong_lan_dau": "%s + 1 món Hoàn mỹ bậc %s (đồ cầm tay theo dòng)" % (huy, BAC[b][1]),
                    "thuong_tuan": "3 Huy chương Gym (item:%d)" % token_gym, "bac_thuong": b})
    # Boss the gioi 52 tuan
    boss = []
    sk = doc_csv(F_SK)
    for r in sk:
        t = int(r["tuan"])
        L = 40 + int(round(50 * (t - 1) / 51.0))  # dai cap trung binh may chu theo mua (40 -> 90)
        if t in HUYEN_THOAI:
            ten, tpl, he = HUYEN_THOAI[t]
            loai, ten2 = "dot_kich", HUYEN_THOAI_NAM2[t][0]
        else:
            ten, tpl, he = BOSS_XOAY_VONG[(t - 1) % len(BOSS_XOAY_VONG)]
            loai, ten2 = "hang_tuan", ten
        hpq, atq, thq = LD.quai_de_xuat(L)
        dps = (LD.ngan_sach_tc(L) - thq * 1.5) * 1.1 * HE_SO_TTK
        hp = int(round(TTK_BOSS_PHUT * 60 * NGUOI_BOSS * dps / 1000.0) * 1000)
        tk = TOKEN_HUYEN_THOAI.get(ten)
        tk_id = ids_item28(ten_id, ids, item_moi, tk, 14166 + (t % 5)) if tk else None
        tk2 = TOKEN_HUYEN_THOAI.get(ten2) if loai == "dot_kich" else None
        tk2_id = ids_item28(ten_id, ids, item_moi, tk2, 14166 + ((t + 2) % 5)) if tk2 else None
        boss.append({"tuan": t, "loai": loai, "ten_nam1": ten, "ten_nam2": ten2, "template_quai": tpl, "he": he,
                     "cap": L, "hp": hp, "tan_cong": int(round(atq * 2.0)), "phong_thu": int(round(thq * 1.5)),
                     "so_nguoi_chuan": NGUOI_BOSS, "phut_muc_tieu": TTK_BOSS_PHUT, "no_sau_phut": 15,
                     "lan_moi_ngay": 2 if loai == "dot_kich" else 1,
                     "vat_pham_dac_trung": "item:%d" % tk_id if tk_id else "",
                     "vat_pham_dac_trung_nam2": "item:%d" % tk2_id if tk2_id else "",
                     "template_nam2": next((q["id"] for q in moi if q["dex"] == HUYEN_THOAI_NAM2[t][1]),
                                           next((k for k in sorted(quai) if int(quai[k]["dex"]) == HUYEN_THOAI_NAM2[t][1]), tpl))
                     if loai == "dot_kich" else tpl})
    return dai, alpha, gym, boss, moi


def quai_moi(quai):
    """Dong data_monster moi (dict theo cot) - quai thuong nam 2 + huyen thoai dot kich nam 2."""
    ra = []
    i = ID_QUAI_MOI_TU
    for (dex, ten, h1, h2, L) in sorted(DE_XUAT_QUAI, key=lambda x: (x[4], x[0])):
        hp, at, th = LD.quai_de_xuat(L)
        ra.append(dong_quai(quai[QUAI_MAU_THUONG], i, ten, dex, h1, h2, L, hp, at, th, LD.MOB_EXP[L],
                            int(round(LD.XU_QUAI[L] * 100 / LD.TI_LE_ROI_XU)), "quai"))
        i += 1
    for (dex, ten, h1, h2, t) in HUYEN_THOAI_MOI:
        ngay = 364 + 7 * t
        L = min(LD.CAP_TOI_DA, tran_cap_ngay(ngay)) - 5
        hpq, atq, thq = LD.quai_de_xuat(L)
        dps = (LD.ngan_sach_tc(L) - thq * 1.5) * 1.1 * HE_SO_TTK
        hp = int(round(TTK_BOSS_PHUT * 60 * NGUOI_BOSS * dps / 1000.0) * 1000)
        ra.append(dong_quai(quai[QUAI_MAU_BOSS], i, ten, dex, h1, h2, L, hp, int(round(atq * 2.0)),
                            int(round(thq * 1.5)), LD.MOB_EXP[L] * 50, 0, "boss"))
        i += 1
    assert i - 1 <= ID_QUAI_MOI_DEN
    return ra


COT_QUAI = ["id", "name", "style", "he", "level", "maxhp", "attack", "defent", "accurate", "dodge", "rcvxp",
            "rcvgold", "active", "palate", "spalate", "isuse", "move", "speed", "h", "w", "xcenter", "ycenter",
            "height", "isnew", "dex", "shiny", "he2"]


def ghi_bang_quai(moi):
    """docs/pokemon/bang_quai.csv: giu nguyen dong cua sinh_bang_quai.py (id <= 138), thay dong
    noi dung (id ID_QUAI_MOI_TU..ID_QUAI_MOI_DEN). Cung ghi noi_dung_quai_moi_bang.csv de sinh_bang_quai.py noi lai khi chay
    lai. pmd_sprite: lay tu docs/pokemon/kiem_ke_sprite.csv (2 = du dong tac, 1 = thieu, '' = chua tai)."""
    kk = {}
    p_kk = os.path.join(RA_DOC, "kiem_ke_sprite.csv")
    if os.path.exists(p_kk):
        for r in doc_csv(p_kk):
            if r["shiny"] == "0":
                kk[int(r["dex"])] = {"OK": 2, "THIEU": 1}.get(r["trang_thai"], "")
    with open(F_QUAI, encoding="utf-8", newline="") as f:
        tho = list(csv.reader(f))
    dau, cu = tho[0], [r for r in tho[1:] if not (ID_QUAI_MOI_TU <= int(r[0]) <= ID_QUAI_MOI_DEN)]
    them = []
    for q in moi:
        nhan = "Hoenn năm 2" if q["phan_loai"] == "quai" else "Đột kích năm 2"
        them.append([q["id"], "(%s)" % nhan, q["level"], q["maxhp"], q["phan_loai"], q["dex"],
                     re.sub(r"[^a-z0-9]", "", q["name"].lower()), 0, q["he"], q["he2"], kk.get(q["dex"], ""),
                     "%s lv%d -> %s" % (nhan, q["level"], q["name"])])
    b = io.StringIO()
    w = csv.writer(b)   # giong sinh_bang_quai.py (ket thuc dong CRLF mac dinh)
    w.writerow(dau)
    w.writerows(cu + [[str(x) for x in r] for r in them])
    open(F_QUAI, "w", encoding="utf-8", newline="").write(b.getvalue())
    b = io.StringIO()
    w = csv.writer(b)
    w.writerow(dau)
    w.writerows([[str(x) for x in r] for r in them])
    open(os.path.join(RA_DOC, "noi_dung_quai_moi_bang.csv"), "w", encoding="utf-8", newline="").write(b.getvalue())


def dong_quai(mau, i, ten, dex, h1, h2, L, hp, at, th, xp, xu, loai):
    d = {c: (int(mau[c]) if c not in ("name",) else mau[c]) for c in COT_QUAI}
    d.update({"id": i, "name": ten, "he": h1, "he2": h2, "level": L, "maxhp": hp, "attack": at, "defent": th,
              "rcvxp": xp, "rcvgold": xu, "dex": dex, "shiny": 0, "phan_loai": loai})
    return d


def ids_item28(ten_id, ids, item_moi, ten, icon, khoa_icon=None):
    """khoa_icon: khoa nd: trong bang_icon.csv khi ten hien thi da doi (giu icon cu)."""
    if ten in ten_id:
        return ten_id[ten]
    i = ids.lay()
    item_moi.append({"id": i, "name": ten, "type": 28, "style": 0, "he": 0, "gender": 0, "level": 1,
                     "durable": 0, **{c: 0 for c in COT_ATB}, "price": 0, "clazz": -1,
                     "xstart": icon_nd(khoa_icon or khoa_ten(ten), icon),
                     "ystart": 0, "colortype": -1, "nloan": 0, "idUpLevel": -1, "namein": ten, "ideff": -1,
                     "pricecu": 0})
    ten_id[ten] = i
    return i


def sinh_roi(cat, nl, gym, boss, tieu_hao, ten_id, ids, item_moi):
    ra = []
    th = {r["ma"]: r for r in tieu_hao}
    for d in range(20):
        c1, c2 = 5 * d + 1, 5 * d + 5
        b = max(x[0] for x in BAC if x[2] <= c1)
        mon = [r for r in cat if r["bac"] == b and "roi" in r["nguon"]]
        moi = PPM_TRANG_BI // len(mon)
        du = PPM_TRANG_BI - moi * len(mon)
        for k, r in enumerate(mon):
            ra.append({"nguon": "dai", "khoa_nguon": "%d-%d" % (c1, c2), "vat_pham": "item:%d" % r["id_item"],
                       "ten": r["ten"], "ppm": moi + (1 if k < du else 0), "sl_min": 1, "sl_max": 1,
                       "do_hiem": "trang/xanh/do", "khoa": 0, "het_han": 0})
        ra.append({"nguon": "dai", "khoa_nguon": "%d-%d" % (c1, c2), "vat_pham": "ngoc:%d" % NGOC_DA_NANG_CAP,
                   "ten": "Đá nâng cấp", "ppm": PPM_DA, "sl_min": 1, "sl_max": 1, "do_hiem": "", "khoa": 0, "het_han": 0})
        ma_phu, ten_phu = nl_phu(b)
        ra.append({"nguon": "dai", "khoa_nguon": "%d-%d" % (c1, c2), "vat_pham": "item:%d" % ten_id[ten_phu],
                   "ten": ten_phu, "ppm": PPM_NL_PHU if c1 >= 40 else 0, "sl_min": 1, "sl_max": 1, "do_hiem": "",
                   "khoa": 0, "het_han": 0})
        thuoc = [r for r in th.values() if r["nhom"] == "hoi_hp" and r["cap_tu"] <= c1 <= r["cap_den"]]
        for r in thuoc:
            ra.append({"nguon": "dai", "khoa_nguon": "%d-%d" % (c1, c2), "vat_pham": r["anh_xa"], "ten": r["ten"],
                       "ppm": PPM_THUOC, "sl_min": 1, "sl_max": 2, "do_hiem": "", "khoa": 0, "het_han": 0})
        ra.append({"nguon": "alpha", "khoa_nguon": "%d-%d" % (c1, c2), "vat_pham": "ngoc:%d" % NGOC_DA_NANG_CAP,
                   "ten": "Đá nâng cấp", "ppm": 1000000, "sl_min": 1, "sl_max": 2, "do_hiem": "", "khoa": 0, "het_han": 0})
        vk = [r for r in mon if r["o"] == "vu_khi"]
        for r in vk:
            ra.append({"nguon": "alpha", "khoa_nguon": "%d-%d" % (c1, c2), "vat_pham": "item:%d" % r["id_item"],
                       "ten": r["ten"] + " (Hoàn mỹ)", "ppm": PPM_HOAN_MY_ALPHA // len(vk), "sl_min": 1, "sl_max": 1,
                       "do_hiem": "hoan_my", "khoa": 1, "het_han": 0})
    for g in gym:
        b = g["bac_thuong"]
        for r in [x for x in cat if x["bac"] == b and x["o"] == "vu_khi"]:
            ra.append({"nguon": "gym", "khoa_nguon": g["ma_nhiem_vu"], "vat_pham": "item:%d" % r["id_item"],
                       "ten": r["ten"] + " (Hoàn mỹ, theo dòng)", "ppm": 1000000, "sl_min": 1, "sl_max": 1,
                       "do_hiem": "hoan_my", "khoa": 1, "het_han": 0})
        ra.append({"nguon": "gym_tuan", "khoa_nguon": g["ma_nhiem_vu"], "vat_pham": "item:%d" % ten_id["Huy chương Gym"],
                   "ten": "Huy chương Gym", "ppm": 1000000, "sl_min": 3, "sl_max": 3, "do_hiem": "", "khoa": 1, "het_han": 0})
    hop_tg = ids_item28(ten_id, ids, item_moi, "Hộp tham gia đột kích", 14117)
    hop_v = ids_item28(ten_id, ids, item_moi, "Hộp vàng đột kích", 14120)
    for bs in boss:
        k = "tuan_%02d" % bs["tuan"]
        ra.append({"nguon": "boss_the_gioi", "khoa_nguon": k + ":dong_gop>=0.5%", "vat_pham": "item:%d" % hop_tg,
                   "ten": "Hộp tham gia đột kích", "ppm": 1000000, "sl_min": 1, "sl_max": 1, "do_hiem": "", "khoa": 1, "het_han": 1})
        ra.append({"nguon": "boss_the_gioi", "khoa_nguon": k + ":top10", "vat_pham": "item:%d" % hop_v,
                   "ten": "Hộp vàng đột kích", "ppm": 1000000, "sl_min": 1, "sl_max": 1, "do_hiem": "", "khoa": 1, "het_han": 1})
        if bs["vat_pham_dac_trung"]:
            ra.append({"nguon": "boss_the_gioi", "khoa_nguon": k + ":dong_gop>=0.5%", "vat_pham": bs["vat_pham_dac_trung"],
                       "ten": TOKEN_HUYEN_THOAI[bs["ten_nam1"]], "ppm": 1000000, "sl_min": 1, "sl_max": 3,
                       "do_hiem": "", "khoa": 1, "het_han": 1})
        b = max(x[0] for x in BAC if x[2] <= bs["cap"])
        rb = [x for x in cat if x["bac"] == b and x["o"] == "ruy_bang"][0]
        ra.append({"nguon": "boss_the_gioi", "khoa_nguon": k + ":top10", "vat_pham": "item:%d" % rb["id_item"],
                   "ten": rb["ten"] + " (Hoàn mỹ)", "ppm": 100000, "sl_min": 1, "sl_max": 1,
                   "do_hiem": "hoan_my", "khoa": 1, "het_han": 0})
    # Mo hop
    for (hop, ten_hop, ds) in [(hop_tg, "Hộp tham gia đột kích", [("ngoc:%d" % NGOC_DA_NANG_CAP, "Đá nâng cấp", 1000000, 2, 4),
                                                                ("thuoc:131", "Thuốc săn trùm", 300000, 1, 1)]),
                               (hop_v, "Hộp vàng đột kích", [("ngoc:%d" % NGOC_DA_NANG_CAP, "Đá nâng cấp", 1000000, 4, 8),
                                                            ("item:%d" % ten_id["Mảnh tinh hoa"], "Mảnh tinh hoa", 250000, 1, 1),
                                                            ("item:%d" % ten_id["Vảy rồng"], "Vảy rồng", 200000, 1, 2)])]:
        for (vp, t, ppm, a, b2) in ds:
            ra.append({"nguon": "mo_hop", "khoa_nguon": "item:%d" % hop, "vat_pham": vp, "ten": t, "ppm": ppm,
                       "sl_min": a, "sl_max": b2, "do_hiem": "", "khoa": 1, "het_han": 0})
    return ra


def sinh_su_kien(ten_id, ids, item_moi):
    sk = doc_csv(F_SK)
    ra = []
    icon_loai = {"tuan_he": 14134, "cong_dong": 14123, "pokemon": 14141, "pvp": 14124, "chien_vung": 14114,
                 "mua": 14113, "thuong": 14135, "dot_kich": 14166, "le_vn": 14140}
    for r in sk:
        t = int(r["tuan"])
        ten1 = r["ten_nam1"]
        vp = []
        for kh, ds in SK_CO_SAN:
            if kh in ten1:
                vp = ds
                break
        if not vp and r["loai"] == "dot_kich":
            tk = TOKEN_HUYEN_THOAI[HUYEN_THOAI[t][0]]
            vp = [(tk, "item:%d" % ten_id[tk])]
        if not vp:
            ten_tk = SK_MOI_THEO_LOAI.get(r["loai"], "Tem tuần")
            khoa_icon = None
            if "Black Friday" in ten1:
                ten_tk = "Phiếu giảm giá trang trí"
            elif r["loai"] == "le_vn":
                ten_tk = "Quà lễ " + re.sub(r"[:(].*$", "", ten1).strip()
                if ten_tk in TEN_QUA_LE_GON:   # ten dai qua ~120 px tren J2ME -> rut gon, giu icon cu
                    khoa_icon = khoa_ten(ten_tk)
                    ten_tk = TEN_QUA_LE_GON[ten_tk]
            i = ids_item28(ten_id, ids, item_moi, ten_tk, icon_loai.get(r["loai"], 14135), khoa_icon)
            vp = [(ten_tk, "item:%d" % i)]
        ns = float(r["ngan_sach_gio"])
        for (ten_vp, ax) in vp:
            sl = SK_SO_LUONG_TUAN
            ra.append({"tuan": t, "su_kien": ten1, "su_kien_nam2": r["ten_nam2"], "loai": r["loai"], "vat_pham": ten_vp,
                       "anh_xa": ax, "so_luong_toi_da_tuan": sl,
                       "gio_thu_nhap_moi_cai": round(ns / len(vp) / sl, 4),
                       "ngan_sach_gio": ns, "phan_xu_phan_tram": int(r["phan_xu_phan_tram"]), "khoa": 1,
                       "het_han_khi_ket_thuc": 1, "doi_lay": r["phan_thuong"]})
    return ra


def sinh_trang_tri():
    ra = []
    for m, (ngay, tran) in enumerate(LD.TRAN_CAP_THEO_NGAY):
        ten = TEN_MUA[m]
        ten_giua = "m" + ten[1:]   # "Mùa Kanto" giua ten -> "mùa Kanto"
        lv = LINH_VAT_MUA[m]
        for (kieu, t, gia, nguon) in [
                ("danh_hieu", "Nhà vô địch %s" % ten_giua, 0, "Top 100 PvP cuối mùa"),
                ("danh_hieu", "Nhà nghiên cứu %s" % ten_giua, 0, "Pokédex +25 loài trong mùa"),
                ("khung", "Khung ảnh %s" % lv, 60, "Cửa hàng lượng (chỉ trang trí)"),
                ("khung", "Khung bậc Master %s" % ten_giua, 0, "Bậc Master mùa PvP"),
                ("trang_phuc", "Trang phục %s" % lv, 150, "Cửa hàng lượng (chỉ trang trí)"),
                ("trang_phuc", "Áo choàng Gym %s" % ten_giua, 0, "Đổi 40 Huy chương Gym"),
                ("hieu_ung", "Hào quang %s" % lv, 0, "Hoàn thành 8 Gym của vùng"),
        ]:
            ra.append({"mua": m + 1, "ten_mua": ten, "ngay_mo": ngay, "tran_cap": tran, "kieu": kieu, "ten": t,
                       "gia": gia, "tien": "luong" if gia else "khong_ban", "nguon": nguon, "chi_so": 0,
                       "trang_thai": "can_code", "icon": icon_nd("trang_tri_%s_%d" % (kieu, m + 1))})
    return ra


def sinh_cua_hang(cat, tieu_hao, nl, trang_tri, ten_id):
    ra = []
    for r in cat:
        if "cua_hang" in r["nguon"]:
            ra.append({"cua_hang": "Bronzong (trang bị)", "vat_pham": "item:%d" % r["id_item"], "ten": r["ten"],
                       "gia": r["gia_xu"], "tien": "xu", "cap": r["cap_yeu_cau"], "ngay_mo": r["ngay_mo"],
                       "gioi_han": "", "gio_thu_nhap": round(r["gia_xu"] / xu_gio(r["cap_yeu_cau"]), 3)})
    for r in tieu_hao:
        if r["tien"] in ("xu", "luong_khoa") and r["gia"] > 0:
            ra.append({"cua_hang": "Chansey (thuốc)" if r["tien"] == "xu" else "Kecleon (lượng khoá)",
                       "vat_pham": r["anh_xa"] if r["anh_xa"] != "moi" else "moi:" + r["ma"], "ten": r["ten"],
                       "gia": r["gia"], "tien": r["tien"], "cap": r["cap_tu"], "ngay_mo": 0, "gioi_han": r["gioi_han"],
                       "gio_thu_nhap": round(r["gia"] / xu_gio((r["cap_tu"] + r["cap_den"]) // 2), 4) if r["tien"] == "xu" else 0})
    for r in nl:
        if r["tien"] == "luong_khoa":
            ra.append({"cua_hang": "Kecleon (lượng khoá)", "vat_pham": r["anh_xa"], "ten": r["ten"], "gia": r["gia"],
                       "tien": "luong_khoa", "cap": 1, "ngay_mo": 0, "gioi_han": "", "gio_thu_nhap": 0})
    for r in trang_tri:
        if r["tien"] == "luong":
            ra.append({"cua_hang": "Delcatty (trang trí)", "vat_pham": "moi:trang_tri", "ten": r["ten"], "gia": r["gia"],
                       "tien": "luong", "cap": 1, "ngay_mo": r["ngay_mo"], "gioi_han": "", "gio_thu_nhap": 0})
    ra.append({"cua_hang": "Huy chương Gym", "vat_pham": "item:%d" % ten_id["Huy chương Gym"], "ten": "Áo choàng Gym (đổi 40 huy chương)",
               "gia": 40, "tien": "huy_chuong_gym", "cap": 16, "ngay_mo": 0, "gioi_han": "", "gio_thu_nhap": 0})
    return ra


def sinh_che_tao(cat, ten_id):
    ra = []
    for r in cat:
        if "che_tao" not in r["nguon"]:
            continue
        b = r["bac"]
        ma_phu, ten_phu = nl_phu(b)
        ra.append({"thanh_pham": "item:%d" % r["id_item"], "ten": r["ten"], "bac": b, "cap": r["cap_yeu_cau"],
                   "da": 2 + b, "nguyen_lieu_phu": ten_phu, "so_nl_phu": 1 + b // 3,
                   "anh_xa_nl_phu": "item:%d" % ten_id[ten_phu], "xu": int(LD.gia_bac(b) * 0.5),
                   "npc": "Machamp (Xưởng chế tạo Pewter)"})
    return ra


# =====================================================================================
# GHI FILE
# =====================================================================================


def ghi_csv(ten, cot, dong, thu_muc=RA_DOC):
    os.makedirs(thu_muc, exist_ok=True)
    b = io.StringIO()
    w = csv.DictWriter(b, fieldnames=cot, lineterminator="\n", extrasaction="ignore")
    w.writeheader()
    for d in dong:
        w.writerow(d)
    open(os.path.join(thu_muc, ten), "w", encoding="utf-8", newline="").write(b.getvalue())


def sql_gt(v):
    if isinstance(v, bool):
        return "1" if v else "0"
    if isinstance(v, int):
        return str(v)
    if isinstance(v, float):
        return repr(round(v, 6))
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"


def bang_sql(ten, cot, dong, khoa):
    o = ["DROP TABLE IF EXISTS `%s`;\n" % ten]
    dn = []
    for c in cot:
        vs = [d[c] for d in dong]
        if all(isinstance(v, int) and not isinstance(v, bool) for v in vs):
            kieu = "bigint NOT NULL"
        elif all(isinstance(v, (int, float)) and not isinstance(v, bool) for v in vs):
            kieu = "double NOT NULL"
        else:
            kieu = "varchar(%d) NOT NULL" % (255 if max(len(str(v)) for v in vs) <= 255 else 1000)
        dn.append("  `%s` %s" % (c, kieu))
    o.append("CREATE TABLE `%s` (\n%s,\n  PRIMARY KEY (%s)\n) DEFAULT CHARSET=utf8;\n" % (
        ten, ",\n".join(dn), ",".join("`%s`" % k for k in khoa)))
    for i in range(0, len(dong), 200):
        o.append("INSERT INTO `%s` (%s) VALUES\n%s;\n" % (
            ten, ",".join("`%s`" % c for c in cot),
            ",\n".join("(" + ",".join(sql_gt(d[c]) for c in cot) + ")" for d in dong[i:i + 200])))
    o.append("\n")
    return "".join(o)


COT_ITEM = ["id", "name", "type", "style", "he", "gender", "level", "durable"] + COT_ATB + [
    "price", "clazz", "xstart", "ystart", "colortype", "nloan", "idUpLevel", "namein", "ideff", "pricecu"]


def kiem_tu_cam(cac_chuoi):
    """Dung tu dien tu cam cua tools/pokemon/chu_pokemon.py (cung nguon voi quet_tu_cam.py)."""
    import importlib.util
    sp = importlib.util.spec_from_file_location("chu_pokemon", os.path.join(DIR, "chu_pokemon.py"))
    cp = importlib.util.module_from_spec(sp)
    sp.loader.exec_module(cp)
    cam = cp.tu_dien_cam(cp.doc_bang())
    loi = []
    for s in cac_chuoi:
        t = cam.tim(s)
        if t:
            loi.append((s, t))
    return loi


def main():
    item, quai, thuoc = nap()
    assert len(thuoc) == SO_O_THUOC, "data_potion phai co %d dong" % SO_O_THUOC
    ids = IdMoi()
    cat, item_moi, bo = sinh_trang_bi(item, ids)
    nl, vl, ngoc, pr, ten_id = sinh_vat_lieu(ids, item_moi)
    tieu_hao = sinh_tieu_hao()
    chieu, moc_ttk, nhanh = sinh_chieu()
    dai, alpha, gym, boss, quai_moi_ds = sinh_quai(quai, ten_id, ids, item_moi)
    ghi_bang_quai(quai_moi_ds)
    roi = sinh_roi(cat, nl, gym, boss, tieu_hao, ten_id, ids, item_moi)
    sk = sinh_su_kien(ten_id, ids, item_moi)
    tt = sinh_trang_tri()
    ch = sinh_cua_hang(cat, tieu_hao, nl, tt, ten_id)
    ct = sinh_che_tao(cat, ten_id)

    # ---- tu cam (chu nguoi choi thay)
    chuoi = [d["name"] for d in item_moi] + [r["ten"] for r in cat] + [r["ten"] for r in tieu_hao] + \
            [r["mo_ta"] for r in tieu_hao] + [r["ten"] for r in chieu] + [r["vai_tro"] for r in chieu] + \
            [r["ten"] for r in nl] + [r["ten"] for r in tt] + [r["ten_boss"] for r in gym] + \
            [r["vat_pham"] for r in sk] + [r["mo_ta"] for r in bo]
    loi = kiem_tu_cam(chuoi)
    if loi:
        for s, t in loi:
            print("TU CAM:", s, t)
        sys.exit(1)

    # ---- CSV noi bo
    B = {}
    B["trang_bi"] = (list(cat[0].keys()), cat, ["ma"])
    B["bo"] = (list(bo[0].keys()), bo, ["bac", "so_mon"])
    B["nguyen_lieu"] = (list(nl[0].keys()), nl, ["ma"])
    B["vat_lieu_nang_cap"] = (list(vl[0].keys()), vl, ["bac", "cong"])
    B["ngoc_he"] = (list(ngoc[0].keys()), ngoc, ["he", "cap"])
    B["phan_ra"] = (list(pr[0].keys()), pr, ["nguon"])
    B["vat_pham"] = (list(tieu_hao[0].keys()), tieu_hao, ["ma"])
    B["chieu"] = (list(chieu[0].keys()), chieu, ["dong", "so_hieu"])
    B["chieu_ttk"] = (list(moc_ttk[0].keys()), moc_ttk, ["dong", "giai_doan"])
    B["chieu_nhanh_eevee"] = (list(nhanh[0].keys()), nhanh, ["dex"])
    B["quai_dai_cap"] = (list(dai[0].keys()), dai, ["dai"])
    B["quai_tinh_anh"] = (list(alpha[0].keys()), alpha, ["id_goc"])
    B["gym"] = (list(gym[0].keys()), gym, ["stt"])
    B["boss_the_gioi"] = (list(boss[0].keys()), boss, ["tuan"])
    B["quai_moi"] = (COT_QUAI + ["phan_loai"], quai_moi_ds, ["id"])
    for k, r in enumerate(roi):
        r["stt"] = k + 1
    B["roi"] = (["stt"] + [c for c in roi[0].keys() if c != "stt"], roi, ["stt"])
    for k, r in enumerate(sk):
        r["stt"] = k + 1
    B["su_kien_vat_pham"] = (["stt"] + [c for c in sk[0].keys() if c != "stt"], sk, ["stt"])
    for k, r in enumerate(tt):
        r["stt"] = k + 1
    B["trang_tri"] = (["stt"] + [c for c in tt[0].keys() if c != "stt"], tt, ["stt"])
    for k, r in enumerate(ch):
        r["stt"] = k + 1
    B["cua_hang"] = (["stt"] + [c for c in ch[0].keys() if c != "stt"], ch, ["stt"])
    B["che_tao"] = (list(ct[0].keys()), ct, ["thanh_pham"])
    for ten, (cot, dong, _) in B.items():
        ghi_csv("noi_dung_%s.csv" % ten, cot, dong)
    ghi_csv("noi_dung_item_moi.csv", COT_ITEM, item_moi)

    # ---- CSV web (chi cot nguoi choi can; khong id/cong thuc)
    ghi_web(cat, bo, nl, vl, ngoc, tieu_hao, chieu, nhanh, dai, gym, boss, sk, tt, ch, ct)

    # ---- SQL
    o = ["-- SINH RA boi tools/pokemon/sinh_noi_dung.py - KHONG SUA TAY.\n",
         "-- Noi dung day du 1-2 nam (docs/pokemon/NOI_DUNG_DAY_DU.md). SQL portable (MySQL/MariaDB/NovaDB):\n",
         "-- chay lai nhieu lan van cho cung ket qua. Gom vao tools/pokemon/xuat_sql_day_du.sh (sau LAU_DAI).\n",
         "-- PHAN A: bang staging data_noi_dung_* (so lieu thiet ke; giai doan 2/3 doc). Khong doi bang dang chay.\n",
         "-- PHAN B: data_item id %d..%d (trang bi bac than thoai/toi thuong, ruy bang bac thap,\n" % (ID_MOI_TU, ID_MOI_DEN),
         "--         nguyen lieu, vat pham su kien/dot kich). Server nap luc khoi dong (Map.loadAllTemplate)\n",
         "--         va gui xuong client (MessageCreator: gui du id 0..max). Chua co nguon roi/ban nao tro toi\n",
         "--         khi sv.lau_dai=0 nen KHONG doi hanh vi game cu.\n",
         "SET NAMES utf8;\n\n"]
    for ten, (cot, dong, khoa) in B.items():
        o.append(bang_sql("data_noi_dung_" + ten, cot, dong, khoa))
    o.append("-- ====================== PHAN B: data_item moi ======================\n")
    o.append("DELETE FROM `data_item` WHERE `id` >= %d AND `id` <= %d;\n" % (ID_MOI_TU, ID_MOI_DEN))
    o.append("INSERT INTO `data_item` (%s) VALUES\n%s;\n" % (
        ", ".join("`%s`" % c for c in COT_ITEM),
        ",\n".join("(" + ", ".join(sql_gt(d[c]) for c in COT_ITEM) + ")" for d in item_moi)))
    o.append("\n-- ====================== PHAN C: data_monster moi (nam 2) ======================\n"
             "-- Id lien tiep %d.. (client cap mang template theo so luong). 139..174 moc tren ban do\n"
             "-- vung Hoenn (BanDoHoenn, map/pokemon_spawn/map43..282); 175..178 = boss dot kich nam 2.\n" % ID_QUAI_MOI_TU)
    o.append("DELETE FROM `data_monster` WHERE `id` >= %d AND `id` <= %d;\n" % (ID_QUAI_MOI_TU, ID_QUAI_MOI_DEN))
    o.append("INSERT INTO `data_monster` (%s) VALUES\n%s;\n" % (
        ", ".join("`%s`" % c for c in COT_QUAI),
        ",\n".join("(" + ", ".join(sql_gt(d[c]) for c in COT_QUAI) + ")" for d in quai_moi_ds)))
    open(RA_SQL, "w", encoding="utf-8", newline="\n").write("".join(o))

    dem = {k: len(v[1]) for k, v in B.items()}
    print("data_item moi:", len(item_moi), "(id %d..%d)" % (item_moi[0]["id"], item_moi[-1]["id"]))
    for k, v in dem.items():
        print("  %-20s %d" % (k, v))
    for r in moc_ttk:
        print("  TTK %-10s gd%d L%-3d R=%.2f ttk=%.2fs (muc tieu %.0f, ti le %.2f)" % (
            r["ten_dong"], r["giai_doan"], r["cap"], r["he_so_xoay_chieu"], r["ttk_giay"], r["ttk_muc_tieu"], r["ti_le"]))


def ghi_web(cat, bo, nl, vl, ngoc, tieu_hao, chieu, nhanh, dai, gym, boss, sk, tt, ch, ct):
    TEN_O = {r[0]: (r[2] or "Đồ cầm tay theo dòng") for r in LOAI_O}
    TEN_NGUON = {"cua_hang": "Cửa hàng", "roi": "Quái rơi", "nhiem_vu": "Nhiệm vụ", "gym": "Gym",
                 "che_tao": "Chế tạo", "su_kien": "Sự kiện", "boss_the_gioi": "Boss thế giới"}
    w = []
    for r in cat:
        w.append({"Tên": r["ten"], "Bậc": r["ten_bac"], "Ô": TEN_O[r["o"]], "Dòng": r["dong"],
                  "Cấp yêu cầu": r["cap_yeu_cau"], "Tấn công": r["tan_cong"], "Phòng thủ": r["phong_thu"],
                  "HP": r["hp"], "Chỉ số phụ": r["chi_so_phu"], "Giá (xu)": r["gia_xu"],
                  "Nguồn": ", ".join(TEN_NGUON[x] for x in r["nguon"].split(","))})
    ghi_csv("trang_bi.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Bộ": r["ten_bo"], "Vùng": r["vung"], "Số món": r["so_mon"], "Thưởng": "+%d%% %s" % (r["phan_tram"], r["chi_so"])} for r in bo]
    ghi_csv("bo_trang_bi.csv", list(w[0].keys()), w, RA_WEB)
    TEN_TIEN = {"xu": "xu", "luong_khoa": "lượng khoá", "luong": "lượng", "khong_ban": "không bán",
                "huy_chuong_gym": "Huy chương Gym"}
    TEN_NHOM = {"hoi_hp": "Hồi máu", "hoi_pp": "Hồi PP", "trang_thai": "Chữa trạng thái", "hoi_sinh": "Hồi sinh",
                "x_tang_cuong": "Tăng cường X", "xua_quai": "Xua quái", "keo_exp": "Kẹo kinh nghiệm",
                "da_tien_hoa": "Đá tiến hoá", "qua_mong": "Quả mọng", "do_giu": "Đồ giữ"}
    w = [{"Tên": r["ten"], "Nhóm": TEN_NHOM[r["nhom"]], "Công dụng": r["mo_ta"], "Cấp": "%d–%d" % (r["cap_tu"], r["cap_den"]),
          "Giá": r["gia"] if r["gia"] else "", "Tiền": TEN_TIEN[r["tien"]], "Nguồn": r["nguon"], "Giới hạn": r["gioi_han"],
          "Sắp có": "có" if r["trang_thai"] == "can_code" else ""} for r in tieu_hao]
    w += [{"Tên": r["ten"], "Nhóm": "Nguyên liệu", "Công dụng": r["dung_cho"], "Cấp": "", "Giá": r["gia"] or "",
           "Tiền": TEN_TIEN[r["tien"]], "Nguồn": r["nguon"], "Giới hạn": "", "Sắp có": ""} for r in nl]
    ghi_csv("vat_pham.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Bậc": r["ten_bac"], "Nâng lên": "+%d" % r["cong"], "Tỉ lệ (%)": r["ti_le"], "Đá": r["da"],
          "Nguyên liệu phụ": r["nguyen_lieu_phu"], "Xu": r["xu"], "Bùa (lượng khoá)": r["bua_luong_khoa"] or ""} for r in vl]
    ghi_csv("nang_cap.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Ngọc": "Ngọc hệ (18 hệ) cấp %d" % r["cap"], "Cấp": r["cap"], "Chỉ số món": "+%d%%" % r["chi_so_phan_tram"],
          "Giá (giờ thu nhập)": r["gio_thu_nhap"]} for r in ngoc if r["he"] == 0]
    ghi_csv("ngoc_he.csv", list(w[0].keys()), w, RA_WEB)
    TEN_GD = {1: "Dạng 1", 2: "Dạng 2", 3: "Dạng 3"}
    w = [{"Dòng": r["ten_dong"], "Chiêu": r["ten"], "Hệ": r["he"], "Vai trò": r["vai_tro"], "Mở ở": TEN_GD[r["giai_doan"]],
          "Cấp mở": r["cap_mo"], "Sát thương cấp 1 (%)": r["phan_tram_cap1"] or "", "Sát thương cấp 10 (%)": r["phan_tram_cap10"] or "",
          "Số đòn": r["so_don"], "Hồi chiêu (giây)": r["hoi_chieu_giay"], "PP": r["pp"] or "",
          "Hiệu ứng": r["trang_thai"]} for r in chieu]
    ghi_csv("chieu.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Nhánh": r["ten"], "Hệ": r["he"], "Vai trò": r["vai_tro"], "Chiêu đặc trưng": "%s, %s, %s" % (r["chieu_o2"], r["chieu_o8"], r["chieu_o10"]),
          "Cấp": r["cap_mo"]} for r in nhanh]
    ghi_csv("nhanh_eevee.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Dải cấp": r["dai"], "Loài": ", ".join(re.sub(r"^\d+:", "", x) for x in r["loai_co_san"].split("|") if x),
          "Hệ": r["he"].replace("|", ", "), "Môi trường": r["moi_truong"].replace("|", ", ")} for r in dai]
    ghi_csv("quai.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Gym": r["stt"], "Huy hiệu": r["huy_hieu"], "Nơi": r["dia_danh"], "Hệ": r["he"], "Cấp": r["cap"],
          "Trưởng Gym": r["ten_boss"], "Thưởng": "Huy hiệu + 1 đồ cầm tay Hoàn mỹ; đánh lại hằng tuần: Huy chương Gym"} for r in gym]
    ghi_csv("gym.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Tuần": r["tuan"], "Boss năm 1": r["ten_nam1"], "Boss năm 2": r["ten_nam2"], "Hệ": r["he"],
          "Loại": "Đột kích huyền thoại" if r["loai"] == "dot_kich" else "Boss tuần",
          "Lượt/ngày": r["lan_moi_ngay"]} for r in boss]
    ghi_csv("boss_the_gioi.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Tuần": r["tuan"], "Sự kiện": r["su_kien"], "Năm 2": r["su_kien_nam2"], "Vật phẩm": r["vat_pham"],
          "Tối đa/tuần": r["so_luong_toi_da_tuan"], "Đổi lấy": r["doi_lay"]} for r in sk]
    ghi_csv("su_kien.csv", list(w[0].keys()), w, RA_WEB)
    TEN_KIEU = {"danh_hieu": "Danh hiệu", "khung": "Khung", "trang_phuc": "Trang phục", "hieu_ung": "Hiệu ứng"}
    w = [{"Mùa": r["ten_mua"], "Loại": TEN_KIEU[r["kieu"]], "Tên": r["ten"], "Giá": r["gia"] or "",
          "Tiền": TEN_TIEN[r["tien"]], "Nguồn": r["nguon"]} for r in tt]
    ghi_csv("trang_tri.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Cửa hàng": r["cua_hang"], "Vật phẩm": r["ten"], "Giá": r["gia"], "Tiền": TEN_TIEN[r["tien"]],
          "Cấp": r["cap"], "Giới hạn": r["gioi_han"]} for r in ch]
    ghi_csv("cua_hang.csv", list(w[0].keys()), w, RA_WEB)
    w = [{"Thành phẩm": r["ten"], "Cấp": r["cap"], "Đá nâng cấp": r["da"], "Nguyên liệu phụ": "%d %s" % (r["so_nl_phu"], r["nguyen_lieu_phu"]),
          "Xu": r["xu"], "Nơi chế tạo": r["npc"]} for r in ct]
    ghi_csv("che_tao.csv", list(w[0].keys()), w, RA_WEB)


if __name__ == "__main__":
    main()
