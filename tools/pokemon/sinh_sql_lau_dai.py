#!/usr/bin/env python3
"""
Thiet ke LAU DAI (docs/thiet-ke-game.md) -> du lieu.

MOT NGUON cho moi con so cua thiet ke dai han. Sua so o day roi chay lai; KHONG sua tay
cac file sinh ra:

  du_lieu/nhiem_vu_moi.csv          danh sach nhiem vu (chinh nam 1 + dan y nam 2, phu,
                                         ngay, tuan, Pokedex, doi, thanh tuu...)
  du_lieu/su_kien_nam.csv           lich 52 tuan (nam 1 + xoay vong nam 2)
  du_lieu/bang_cap_do_lau_dai.csv   bang EXP de xuat 1..100 + gio muc tieu
  src/com/htbb/pokemon/ThongSoLauDai.java  hang so cho mo phong + giai doan 2 (CHUA noi day)
  sql/pokemon_lau_dai.sql                bang tam (staging) - gom vao xuat_sql_day_du.sh (GD2)

Chay (o thu muc HTBB/):   python3 -I tools/pokemon/sinh_sql_lau_dai.py
Sau do chay mo phong:      xem tools/pokemon/MoPhongKinhTe.java (dau file).

Chi dung thu vien chuan. Tat dinh: cung dau vao -> cung dau ra tung byte.
"""
import csv
import io
import math
import os
import re
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import lau_dai_cot_truyen as CT  # noqa: E402

DUMP = os.path.join(GOC, "sql", "htbb_day_du.sql")
RA_CSV_NV = os.path.join(GOC, "du_lieu", "nhiem_vu_moi.csv")
RA_CSV_SK = os.path.join(GOC, "du_lieu", "su_kien_nam.csv")
RA_CSV_CAP = os.path.join(GOC, "du_lieu", "bang_cap_do_lau_dai.csv")
RA_JAVA = os.path.join(GOC, "server", "src", "com", "htbb", "pokemon", "ThongSoLauDai.java")
RA_SQL = os.path.join(GOC, "sql", "pokemon_lau_dai.sql")
BANG_VP = os.path.join(GOC, "du_lieu", "bang_vat_pham.csv")

# =====================================================================================
# 1. THAM SO THIET KE (moi so trong docs/thiet-ke-game.md lay tu day)
# =====================================================================================

CAP_TOI_DA = 100

# Mo tran cap theo mua: (ngay ke tu khai truong, tran cap). TAT MAC DINH tu 18/10/2026 (server.ini
# sv.tran_cap_theo_mua=0, quyet dinh: khong tran theo mua, len cap CHAM + vat pham tang toc - TangToc.java).
# Giu bang de bat lai duoc (sv.tran_cap_theo_mua=1) va de mo phong so sanh.
TRAN_CAP_THEO_NGAY = [(0, 60), (60, 70), (150, 80), (240, 91), (365, 95), (545, 100)]

# Gio "hieu dung" (danh quai cung cap, toc do 100%) de len TU cap L -> L+1. Noi suy log giua
# cac moc => duong cong tron, EXP_LEN_CAP tang don dieu. 18/10/2026 (khong tran theo mua): cac moc tu cap 40
# nhan 0,94 (cap 1-30 giu nguyen: tien hoa dau ~1,1 gio) va nang luong 90 phut/ngay (duoi) => tong 1->100 ~ 885
# gio; mo phong: nguoi nhe khong nap (1-2 gio/ngay, dung ban khoa) toi cap 100 ~ thang 17, nguoi cay ~ thang 10.
GIO_CAP_MOC = [(1, 0.03), (10, 0.05), (16, 0.10), (20, 0.25), (30, 0.9), (40, 1.9), (50, 3.4),
               (60, 5.8), (70, 10.3), (80, 17.9), (90, 30.1), (100, 43.2)]

# EXP chuan cua 1 quai cung cap (noi suy log tu data_monster.rcvxp quai thuong da dat tren map).
MOB_EXP_MOC = [(1, 20), (10, 300), (20, 900), (30, 2000), (40, 4100), (50, 5940), (60, 9000),
               (70, 14000), (80, 24000), (86, 30000), (90, 34000), (100, 45000)]


def quai_moi_gio(L):
    """So quai ha duoc / gio danh lien tuc o bai dung cap (TTK 4-6 s + di chuyen)."""
    return int(round(360 - 1.3 * L))


def xu_moi_quai(L):
    """Xu KY VONG moi quai (ti le roi 30%, moi lan roi = gia tri/0.30)."""
    return int(round(10 * 1.05 ** L))


TI_LE_ROI_XU = 30  # %

# Nang luong huan luyen (chong cay qua muc + chong bot). Gio trong NGAY, sau gio nghi (rested).
# 18/10/2026: thu hep (90 phut) de nguoi cay 6 gio/ngay chi nhanh ~1,7 lan nguoi choi 1,5 gio (truoc: ~3 lan) khi
# khong con tran theo mua. Nguoi choi nhe (<= 90 phut danh quai) khong bi anh huong. Bua the luc (TangToc) +60 phut.
NANG_LUONG_GIO_NGAY = 1.5           # gio 100% moi ngay
NANG_LUONG_TICH_TOI_DA = 3.0        # gio 100% tich luy toi da (nghi ngoi)
BAC_MET = [(2.0, 20), (2.0, 5)]     # sau gio 100%: 2 gio 20%, 2 gio 5%, sau do 0% EXP va 0 xu
HE_SO_BAT_KIP = 150                 # % EXP khi cap < trung vi may chu - 5
KHOANG_BAT_KIP = 5

# Tinh thong sau tran cap.
TINH_THONG_PHAN_TRAM_CAP = 50       # 1 diem = 50% EXP_LEN_CAP(tran-1)
TINH_THONG_DIEM_MOI_MUA = 10
TINH_THONG_TOI_DA = 50
TINH_THONG_PHAN_NGHIN_MOI_DIEM = 2  # +0.2% tan cong/HP moi diem (toi da +10%)

# Bac trang bi: (bac, ten, cap, ngay mo).
BAC_TB = [(0, "sơ cấp", 1, 0), (1, "đồng", 10, 0), (2, "bạc", 20, 0), (3, "vàng", 30, 0),
          (4, "bạch kim", 40, 0), (5, "kim cương", 50, 0), (6, "chuyên gia", 60, 0),
          (7, "tinh anh", 70, 60), (8, "huyền thoại", 80, 150), (9, "thần thoại", 90, 365),
          (10, "tối thượng", 98, 545)]
SO_O_TRANG_BI = 11
SO_MON_MUA_MOI_BAC = 5              # phan con lai den tu nhiem vu/roi/che tao
GIA_TB_GIO_THU_NHAP = 1.5           # gia 1 mon = 1.5 gio thu nhap xu o cap cua bac

# Nang cap +1..+15.
TI_LE_NC = [100, 100, 95, 90, 85, 75, 65, 55, 45, 38, 30, 24, 18, 13, 10]
PITY_CHIA = 3                       # moi lan truot lien tiep cong ceil(ti le goc / 3) %
MAT_CAP_TU = 7                      # truot tu +7 tro len: -1 cap (tru khi co Bua bao ho)
CHI_PHI_NC_PHAN_NGHIN = 40          # lan +1 = 4% gia mon
CHI_PHI_NC_TANG = 1.30              # moi bac +
DA_NC = [1] * 5 + [2] * 5 + [4] * 5
BUA_BAO_HO_LUONG_KHOA = [0] * 6 + [10] * 4 + [25] * 5
CHI_SO_NC_PHAN_TRAM = [3] * 10 + [5] * 5   # +55% chi so mon o +15
GIU_CAP_KHI_DOI_BAC = 2             # doi sang bac moi: plus moi = plus cu - 2
DA_ROI_PHAN_NGHIN = 15              # 1.5%/quai khi |lech cap| <= 5
DA_TU_PHAN_RA = 2                   # phan ra 1 mon trung -> 2 da + 10% gia

# Ngoc he (khảm): ho hut thu 2 sau nang cap. Moi o 1 ngoc, cap 1..10, +1% chi so mon / cap.
NGOC_HE_CAP_TOI_DA = 10
NGOC_HE_GIA_GIO_PHAN_TRAM = 100     # buoc dau = 1.0 gio thu nhap
NGOC_HE_TANG_PHAN_TRAM = 145        # moi cap x1.45
TRANG_TRI_PHAN_NGHIN_NGAY = 20      # chi 2%/ngay phan xu vuot 20 gio thu nhap cho do trang tri bang xu
TRANG_TRI_NGUONG_GIO = 20

BO_TB = [(4, "HP", 5), (7, "Tấn công", 5), (11, "Chí mạng", 3)]   # (so mon cung bac, chi so, %)

# Hao phi tu dong (phan tram thu nhap moi gio choi).
TIEU_HAO_PHAN_TRAM = 12             # thuoc
SUA_DO_PHAN_TRAM = 8
DICH_CHUYEN_LAN_NGAY = 3
DICH_CHUYEN_PHAN_TRAM_GIO = 3       # moi lan = 3% thu nhap 1 gio
THUE_CHO_PHAN_TRAM = 6
PHI_DANG_BAN_PHAN_TRAM = 1
TRAN_GIAO_DICH_TRUC_TIEP_GIO = 5    # chuyen xu truc tiep <= 5 gio thu nhap/ngay

# Ngan sach phan thuong (don vi: GIO thu nhap / gio EXP tuong duong o cap nhiem vu).
NGAN_SACH = {
    # loai: (gio EXP, gio xu, luong khoa, luong)
    "huong_dan": (0.06, 0.03, 2, 0),
    "chinh": (0.30, 0.15, 3, 0),
    "chinh_moc": (0.45, 0.25, 10, 0),   # buoc cuoi chuong co huy hieu
    "phu": (0.40, 0.25, 3, 0),
    "tuyen": (0.50, 0.30, 4, 0),
    "ngay": (0.10, 0.05, 4, 0),
    "tuan": (1.00, 0.50, 20, 0),
    "pokedex": (0.25, 0.10, 5, 0),
    "doi": (0.50, 0.20, 10, 0),
    "su_pham": (0.30, 0.10, 10, 0),
    "hang_bi_an": (0.20, 0.10, 5, 0),
    "quay_lai": (0.80, 0.30, 15, 0),
    "thanh_tuu": (0.00, 0.00, 20, 10),
    # Nhom moi (tools/pokemon/sinh_nhiem_vu.py): thuong bang danh hieu/trang tri/truyen; luong khoa
    # cua chung tru vao quy thanh tuu (tong khong doi). Su kien: thuong nam trong ngan sach su kien tuan.
    "su_kien": (0.00, 0.00, 0, 0),
    "huyen_thoai": (0.00, 0.00, 5, 0),
    "an": (0.00, 0.00, 2, 0),
}
CAP_TAN_THU = 16       # duoi cap nay thuong nhiem vu x HE_SO_TAN_THU
HE_SO_TAN_THU = 3.0
GIOI_HAN_NGAY = 3      # nhiem vu ngay hoan thanh / ngay
GIOI_HAN_TUAN = 2      # nhiem vu tuan / tuan
VE_CHUONG_TUAN = 2     # ve mo chuong chinh / tuan / nhan vat
VE_CHUONG_TOI_DA = 4
VE_CHUONG_BAN_DAU = 4
GIO_MOI_CHUONG = 0.6   # thoi gian choi de lam 1 chuong (duoi CAP_TAN_THU: mot nua)
GIO_MOI_PHU = 0.25

# Dang nhap 7 ngay (luong khoa), lap lai.
LICH_DANG_NHAP = [3, 4, 5, 6, 8, 10, 15]

# Luong (giao dich duoc): khong nap. Nguon: bac xep hang tuan + thanh tuu + top su kien.
LUONG_XEP_HANG_TUAN = [(1, 50), (10, 20), (100, 8), (1000, 3)]  # (top <=, luong)

# Su kien: phan cua ngan sach tra bang xu giao dich duoc (con lai: vat pham khoa/het han).
SU_KIEN_PHAN_XU = 25
GIFTCODE_TOI_DA_THANG = 2
GIFTCODE_GIO_TOI_DA = 0.5

# Muc tieu (mo phong phai dat).
MUC_TIEU = {
    # 18/10/2026 (khong tran theo mua): cot truyen nam 1 bi chan boi cap chuong (C52 can cap 90) + ve chuong 2/tuan.
    "nhe_xong_chinh_som_nhat_thang": 9,        # nhom choi nhe (1-2h): <= 5% xong nam 1 truoc thang 9
    "nhe_xong_chinh_muon_nhat_thang": 14,      # ... trung vi <= thang 14
    "cay_xong_chinh_som_nhat_thang": 5,        # nhom cay: <= 5% truoc thang 5 (52 chuong / 2 ve tuan ~ 26 tuan)
    # Thoi gian toi cap toi da (end-game) khi KHONG tran theo mua, nguoi khong nap (dung ban khoa kiem duoc).
    "nhe_cap_toi_da_thang_min": 15, "nhe_cap_toi_da_thang_max": 18,
    "cay_cap_toi_da_thang_min": 9, "cay_cap_toi_da_thang_max": 12,
    "tang_toc_nhanh_hon_toi_da_phan_tram": 40,   # nguoi mua tang toc toi tran / khong dung gi, cung gio choi
    "luong_dot_toi_thieu_phan_tram": 95,        # luong nap bi dot (tu thang 3)
    "tang_tien_thuc_thang_toi_da_phan_tram": 5,      # xu/nguoi tinh theo GIO thu nhap o cap trung vi, TB thang 4..24
    "xu_thuc_toi_da_gio": 20,                         # xu TB/nguoi <= 20 gio thu nhap o cap trung vi
    "sink_faucet_min": 0.80, "sink_faucet_max": 1.05,
    "xu_giu_gio_thu_nhap_toi_da": 60,                # trung vi xu dang giu / thu nhap 1 gio
    "d1_toi_thieu": 40, "d7_toi_thieu": 20, "d30_toi_thieu": 10,
    "tien_hoa_dau_gio_toi_da": 1.2,
    "muc_nang_cap_nhe": 12, "muc_nang_cap_vua": 14, "muc_nang_cap_cay": 15,
}

# =====================================================================================
# 2. DOC DU LIEU GOC
# =====================================================================================


def doc_dump():
    s = open(DUMP, encoding="utf-8").read()
    cot, dong = {}, {}
    for m in re.finditer(r"CREATE TABLE `(\w+)` \((.*?)\n\)", s, re.S):
        cot[m.group(1)] = re.findall(r"^\s+`(\w+)`", m.group(2), re.M)
    for m in re.finditer(r"INSERT INTO `(\w+)` VALUES (.*?);\n", s, re.S):
        dong.setdefault(m.group(1), []).extend(_tach(m.group(2)))
    return cot, dong


def _tach(s):
    ra, i, n = [], 0, len(s)
    while i < n:
        if s[i] != "(":
            i += 1
            continue
        i += 1
        row, cur, q, isq = [], [], False, False
        while True:
            c = s[i]
            if q:
                if c == "\\":
                    cur.append(s[i + 1])
                    i += 2
                    continue
                if c == "'":
                    q = False
                    i += 1
                    continue
                cur.append(c)
                i += 1
                continue
            if c == "'":
                q, isq = True, True
                i += 1
                continue
            if c in ",)":
                v = "".join(cur)
                row.append(v if isq else (None if v == "NULL" else v))
                cur, isq = [], False
                i += 1
                if c == ")":
                    break
                continue
            cur.append(c)
            i += 1
        ra.append(row)
    return ra


def bang(cot, dong, ten):
    return [dict(zip(cot[ten], r)) for r in dong.get(ten, [])]


# Quai thuong DA DAT tren ban do (map/map<N>, kiem toan muc 1.4) - chi chon muc tieu tu day.
QUAI_DA_DAT = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
               24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 40, 41, 42, 44, 45, 47, 48, 49, 50,
               51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 62, 63, 65, 68, 71, 74, 76, 77, 80, 81, 118,
               136, 137, 138]

# Vat pham nhiem vu dung duoc (data.ItemQuest.NAME_ITEM_QUEST trong lib/htbb-core.jar).
VP_NV = {0: "Nọc bọ cạp", 2: "Khúc gỗ", 3: "Rết đỏ", 6: "Lông đuôi Hồng Kê", 7: "Móng cá sấu",
         8: "Da ếch", 9: "Cựa gà"}
VP_THU = 4  # "Lá thư"

# =====================================================================================
# 3. BANG DAN XUAT
# =====================================================================================


def noi_suy_log(moc, L):
    for (a, va), (b, vb) in zip(moc, moc[1:]):
        if a <= L <= b:
            t = (L - a) / (b - a)
            return math.exp(math.log(va) + t * (math.log(vb) - math.log(va)))
    return moc[-1][1]


def noi_suy_tt(moc, L):
    for (a, va), (b, vb) in zip(moc, moc[1:]):
        if a <= L <= b:
            return va + (L - a) / (b - a) * (vb - va)
    return moc[-1][1]


def lam_tron3(x):
    """Lam tron 3 chu so co nghia (bang EXP de doc)."""
    if x < 1000:
        return int(round(x))
    e = int(math.floor(math.log10(x))) - 2
    return int(round(x / 10 ** e) * 10 ** e)


MOB_EXP = [0] + [int(round(noi_suy_log(MOB_EXP_MOC, L))) for L in range(1, CAP_TOI_DA + 1)]
KPH = [0] + [quai_moi_gio(L) for L in range(1, CAP_TOI_DA + 1)]
XU_QUAI = [0] + [xu_moi_quai(L) for L in range(1, CAP_TOI_DA + 1)]
GIO_CAP = [0.0] + [noi_suy_log(GIO_CAP_MOC, L) for L in range(1, CAP_TOI_DA + 1)]
GIO_TL = [0.0, 0.0]
for _L in range(2, CAP_TOI_DA + 1):
    GIO_TL.append(GIO_TL[-1] + GIO_CAP[_L - 1])
EXP_GIO = [0] + [KPH[L] * MOB_EXP[L] for L in range(1, CAP_TOI_DA + 1)]
XU_GIO = [0] + [KPH[L] * XU_QUAI[L] for L in range(1, CAP_TOI_DA + 1)]
EXP_LEN_CAP = [0] * (CAP_TOI_DA + 1)
for _L in range(1, CAP_TOI_DA):
    EXP_LEN_CAP[_L] = lam_tron3(GIO_CAP[_L] * EXP_GIO[_L])


def gia_bac(b):
    L = BAC_TB[b][2]
    return int(round(GIA_TB_GIO_THU_NHAP * XU_GIO[L]))


def chi_phi_nc(b, k):
    """Xu cho MOT lan thu nang tu +(k-1) len +k o bac b."""
    return int(round(gia_bac(b) * CHI_PHI_NC_PHAN_NGHIN / 1000.0 * CHI_PHI_NC_TANG ** (k - 1)))


def ky_vong_nc(k):
    """(so lan thu ky vong, xac suat dat trong 1 chuoi) cho buoc len +k, co pity, khong tinh mat cap."""
    goc = TI_LE_NC[k - 1]
    them = -(-goc // PITY_CHIA)
    p_con, ky_vong, j = 1.0, 0.0, 0
    while p_con > 1e-9:
        p = min(100, goc + j * them) / 100.0
        ky_vong += p_con
        p_con *= (1 - p)
        j += 1
    return ky_vong


# Chi so: ngan sach theo cap (de xuat, giai doan 3). Tan cong "tran" = khong do, dồn diem.
def tan_cong_tran(L):
    return 30 + 12 * L


def ngan_sach_tc(L, bac_bonus=0.6, plus=6):
    """Tan cong ky vong: tran x (1 + do 60% x (1 + nang cap))."""
    nc = sum(CHI_SO_NC_PHAN_TRAM[:plus]) / 100.0
    return tan_cong_tran(L) * (1 + bac_bonus * (1 + nc))


def quai_de_xuat(L):
    """HP/tan cong/thu de xuat cho quai thuong cap L: TTK muc tieu + so don ha nguoi."""
    atk = ngan_sach_tc(L)
    thu = int(round(0.35 * atk))
    ttk = 4.0 if L <= 30 else (5.0 if L <= 60 else 6.0)
    dps = (atk - thu) * 1.1 * 2.2          # 2.2 = he so xoay chieu trung binh / giay
    hp = int(round(ttk * dps / 10.0) * 10)
    hp_nguoi = 70 * (25 + L) * 1.6         # HP ky vong (health x70 x do)
    thu_nguoi = 0.25 * atk
    don = 15 if L <= 30 else 12
    atk_q = int(round(hp_nguoi / don / 0.76 + thu_nguoi))
    return hp, atk_q, thu


# =====================================================================================
# 4. NHIEM VU
# =====================================================================================

COT_NV = ["ma", "id_so", "loai", "nam", "vung", "cung", "chuong", "buoc", "ten", "npc_giao",
          "ten_npc", "dia_danh", "cap_toi_thieu", "ngay_mo", "truoc", "kieu_muc_tieu", "muc_tieu_id",
          "muc_tieu_ten", "so_luong", "quai_roi", "lap_lai", "gioi_han", "exp", "xu", "luong_khoa",
          "luong", "vat_pham", "huy_hieu", "dong_co_tuong_thich", "ghi_chu"]

HUY_HIEU = {
    ("K2", 2): "Huy hiệu Đá (Pewter)", ("K3", 2): "Huy hiệu Thác (Cerulean)",
    ("K3", 4): "Huy hiệu Sấm (Vermilion)", ("K5", 4): "Huy hiệu Cầu Vồng (Celadon)",
    ("K6", 2): "Huy hiệu Đầm Lầy (Saffron)", ("K6", 4): "Huy hiệu Linh Hồn (Fuchsia)",
    ("K7", 3): "Huy hiệu Núi Lửa (Cinnabar)", ("K7", 4): "Huy hiệu Đất (Viridian)",
    ("J1", 3): "Huy hiệu Gió (Violet)", ("J2", 1): "Huy hiệu Tổ Ong (Azalea)",
    ("J2", 4): "Huy hiệu Đồng Bằng (Goldenrod)", ("J3", 1): "Huy hiệu Sương (Ecruteak)",
    ("J3", 3): "Huy hiệu Khoáng (Olivine)", ("J3", 4): "Huy hiệu Bão (Cianwood)",
    ("J4", 4): "Huy hiệu Băng (Mahogany)", ("J5", 4): "Huy hiệu Rồng (Blackthorn)",
}


class SinhNV:
    def __init__(self, quai, vat_pham):
        self.quai = quai            # id -> (ten, cap)
        self.vp = vat_pham          # [(id, type, cap, ten)]
        self.dong = []
        self.dem = {}

    def id_moi(self, loai, goc):
        self.dem[loai] = self.dem.get(loai, goc) + 1
        return self.dem[loai]

    def chon_quai(self, L, k=0, len_tren=1):
        ung = sorted([(abs(c - L), c, i) for i, (t, c) in self.quai.items()
                      if L - 3 <= c <= L + len_tren], key=lambda x: (x[0], x[2]))
        if not ung:
            ung = sorted([(abs(c - L), c, i) for i, (t, c) in self.quai.items()],
                         key=lambda x: (x[0], x[2]))
        _, c, i = ung[k % min(len(ung), 3)]
        return i, self.quai[i][0], c

    def chon_do(self, L, k):
        ung = [v for v in self.vp if v[2] <= max(1, L) and v[1] in (0, 1, 2, 8, 9, 10, 11, 12, 19)]
        ung.sort(key=lambda v: (-v[2], v[1], v[0]))
        if not ung:
            return ""
        cap_max = ung[0][2]
        cung = [v for v in ung if v[2] == cap_max]
        return str(cung[k % len(cung)][0])

    def thuong(self, loai, L, he=1.0):
        gexp, gxu, lk, lg = NGAN_SACH[loai]
        if L < CAP_TAN_THU and loai in ("huong_dan", "chinh", "phu", "tuyen"):
            he *= HE_SO_TAN_THU   # tan thu: tien hoa dau tien trong ~1 gio
        Lc = max(1, min(L, CAP_TOI_DA))
        return (int(gexp * he * EXP_GIO[Lc]), int(gxu * he * XU_GIO[Lc]), lk, lg)

    def them(self, **k):
        d = {c: "" for c in COT_NV}
        d.update({c: v for c, v in k.items()})
        d["nam"] = d["nam"] or 1
        self.dong.append(d)
        return d


def so_quai(L, phut):
    if L < CAP_TAN_THU:
        phut = phut / 2.0   # chuong tan thu ngan hon mot nua
    return max(10, int(round(KPH[max(1, min(L, CAP_TOI_DA))] * phut / 60.0 / 5) * 5))


def sinh_nhiem_vu(s):
    """Bo sinh moi (tools/pokemon/sinh_nhiem_vu.py, noi dung o nv_*.py). Ham cu ben duoi giu de doi chieu."""
    import sinh_nhiem_vu as NVM
    NVM.sinh(s)


def sinh_nhiem_vu_cu(s):
    # --- 4.1 Huong dan 30 phut dau (10 buoc, chinh, truoc chuong 1) -------------------
    hd = [("Thức giấc ở Pallet", "noi_chuyen", 5), ("Chào Kecleon", "noi_chuyen", 1),
          ("Bài học tấn công", "giet", 1), ("Bình thuốc đầu tiên", "noi_chuyen", 0),
          ("Bài học chiêu thức", "giet", 2), ("Gặp Machamp thợ chế tạo", "noi_chuyen", 2),
          ("Mặc trang bị", "nang_cap", 2), ("Thử sức ngoài đồng", "giet", 4),
          ("Sổ tay Pokédex", "pokedex_gap", 5), ("Lời hứa với Xatu", "noi_chuyen", 5)]
    truoc = ""
    for i, (ten, kieu, npc) in enumerate(hd, 1):
        L = 1 + (i - 1) // 2
        ex, xu, lk, lg = s.thuong("huong_dan", L)
        qid, qten, qc = s.chon_quai(L, i)
        ma = "HD.%02d" % i
        s.them(ma=ma, id_so=s.id_moi("hd", 900), loai="huong_dan", vung="Kanto", cung="HD", chuong=0,
               buoc=i, ten=ten, npc_giao=npc, ten_npc=CT.NPC.get(npc, "Chansey"), dia_danh="Pallet",
               cap_toi_thieu=L, ngay_mo=0, truoc=truoc, kieu_muc_tieu=kieu,
               muc_tieu_id=qid if kieu == "giet" else (5 if kieu == "pokedex_gap" else ""),
               muc_tieu_ten=qten if kieu == "giet" else ("5 loài" if kieu == "pokedex_gap" else ""),
               so_luong=10 if kieu == "giet" else (5 if kieu == "pokedex_gap" else 1),
               lap_lai="khong", exp=ex, xu=xu, luong_khoa=lk, luong=lg,
               vat_pham=s.chon_do(1, i) if kieu == "nang_cap" else "",
               dong_co_tuong_thich=1 if kieu in ("noi_chuyen", "giet") else 0,
               ghi_chu="Hướng dẫn: 10 bước ~30 phút, xong ở cấp ~8")
        truoc = ma

    # --- 4.2 Cot truyen chinh nam 1: 13 cung x 4 chuong x 4 buoc -----------------------
    stt = 0
    for (mc, tc, vung, chuongs) in CT.CUNG_NAM1:
        for ci, (tch, noi, npc, L) in enumerate(chuongs, 1):
            stt += 1
            hh = HUY_HIEU.get((mc, ci), "")
            b = [("Lời nhờ: " + tch, "dua_thu" if ci % 2 == 0 else "noi_chuyen"),
                 ("Dọn đường " + noi, "giet"),
                 ("Thu thập ở " + noi, "thu_thap"),
                 (("Thách đấu Gym: " + hh.split(" (")[0]) if hh else ("Thử thách: " + tch), "giet_tinh_anh")]
            for bi, (ten, kieu) in enumerate(b, 1):
                cuoi = bi == 4
                ex, xu, lk, lg = s.thuong("chinh_moc" if (cuoi and hh) else "chinh", L)
                qid, qten, qc = s.chon_quai(L + (3 if kieu == "giet_tinh_anh" else 0), stt + bi,
                                            len_tren=3 if kieu == "giet_tinh_anh" else 1)
                vp_nv = sorted(VP_NV)[(stt + bi) % len(VP_NV)]
                if kieu == "giet":
                    mt = dict(muc_tieu_id=qid, muc_tieu_ten=qten, so_luong=so_quai(L, 12), quai_roi="")
                elif kieu == "giet_tinh_anh":
                    mt = dict(muc_tieu_id=qid, muc_tieu_ten=qten + " (cấp %d)" % qc,
                              so_luong=so_quai(L, 6), quai_roi="")
                elif kieu == "thu_thap":
                    mt = dict(muc_tieu_id=vp_nv, muc_tieu_ten=VP_NV[vp_nv], so_luong=so_quai(L, 10) // 5,
                              quai_roi=qid)
                elif kieu == "dua_thu":
                    mt = dict(muc_tieu_id=VP_THU, muc_tieu_ten="Lá thư", so_luong=1, quai_roi="")
                else:
                    mt = dict(muc_tieu_id="", muc_tieu_ten=CT.NPC.get(npc, ""), so_luong=1, quai_roi="")
                ma = "C%02d.%d" % (stt, bi)
                s.them(ma=ma, id_so=s.id_moi("c", 1000), loai="chinh", vung=vung, cung=mc + " " + tc,
                       chuong=stt, buoc=bi, ten=ten[:45], npc_giao=npc, ten_npc=CT.NPC.get(npc, ""),
                       dia_danh=noi, cap_toi_thieu=L, ngay_mo=0, truoc=truoc, kieu_muc_tieu=kieu,
                       lap_lai="khong", exp=ex, xu=xu, luong_khoa=lk, luong=lg,
                       vat_pham=s.chon_do(L, stt) if cuoi else "", huy_hieu=hh if cuoi else "",
                       dong_co_tuong_thich=1,
                       ghi_chu=("Cần 1 vé chương (2/tuần, tích tối đa 4)" if bi == 1 else ""), **mt)
                truoc = ma

    # --- 4.3 Nam 2: dan y (khong sinh SQL) ----------------------------------------------
    for (mc, tc, vung, tens, L) in CT.CUNG_NAM2:
        for ci, tch in enumerate(tens, 1):
            stt += 1
            ex, xu, lk, lg = s.thuong("chinh", L, he=4.0)
            ma = "C%02d" % stt
            s.them(ma=ma, id_so=s.id_moi("c2", 1300), loai="chinh", nam=2, vung=vung, cung=mc + " " + tc,
                   chuong=stt, buoc=0, ten=tch, npc_giao="", ten_npc="", dia_danh=tch.split(" ")[-1],
                   cap_toi_thieu=L, ngay_mo=365 if L <= 95 else 545, truoc=truoc, kieu_muc_tieu="dan_y",
                   lap_lai="khong", exp=ex, xu=xu, luong_khoa=lk * 4, luong=0,
                   dong_co_tuong_thich=0, ghi_chu="Dàn ý năm 2: cần map/quái Hoenn + cấp >91 (GĐ3)")
            truoc = ma

    # --- 4.4 Chuoi phu theo thi tran (20 x 5) + tuyen (10 x 3) ----------------------------
    for ti, (noi, vung, L0, npc, chu_de) in enumerate(CT.THI_TRAN):
        truoc = ""
        for bi, mau in enumerate(CT.MAU_PHU, 1):
            L = L0 + (bi - 1) * 2
            ex, xu, lk, lg = s.thuong("phu", L)
            kieu = ["noi_chuyen", "giet", "thu_thap", "giet_lech_cap", "giet_tinh_anh"][bi - 1]
            qid, qten, qc = s.chon_quai(L + (2 if bi == 5 else 0), ti + bi)
            vp_nv = sorted(VP_NV)[(ti + bi) % len(VP_NV)]
            if kieu == "thu_thap":
                mt = dict(muc_tieu_id=vp_nv, muc_tieu_ten=VP_NV[vp_nv], so_luong=so_quai(L, 10) // 5, quai_roi=qid)
            elif kieu == "giet_lech_cap":
                mt = dict(muc_tieu_id="", muc_tieu_ten="quái ±5 cấp", so_luong=so_quai(L, 15), quai_roi="")
            elif kieu == "noi_chuyen":
                mt = dict(muc_tieu_id="", muc_tieu_ten=CT.NPC.get(npc, ""), so_luong=1, quai_roi="")
            else:
                mt = dict(muc_tieu_id=qid, muc_tieu_ten=qten, so_luong=so_quai(L, 10), quai_roi="")
            ma = "P-%02d.%d" % (ti + 1, bi)
            s.them(ma=ma, id_so=s.id_moi("p", 2000), loai="phu", vung=vung, cung="Thị trấn " + noi,
                   chuong="", buoc=bi, ten=mau.format(noi=noi, chu_de=chu_de)[:45], npc_giao=npc,
                   ten_npc=CT.NPC.get(npc, ""), dia_danh=noi, cap_toi_thieu=L, ngay_mo=0, truoc=truoc,
                   kieu_muc_tieu=kieu, lap_lai="khong", exp=ex, xu=xu, luong_khoa=lk, luong=lg,
                   vat_pham=s.chon_do(L, ti) if bi == 5 else "",
                   dong_co_tuong_thich=1, ghi_chu="Chuỗi 5 bước; bước 5 cho danh hiệu thị trấn", **mt)
            truoc = ma
    for ti, (noi, L0) in enumerate(CT.TUYEN):
        truoc = ""
        for bi, mau in enumerate(CT.MAU_TUYEN, 1):
            L = L0 + bi - 1
            ex, xu, lk, lg = s.thuong("tuyen", L)
            qid, qten, qc = s.chon_quai(L, ti * 3 + bi)
            ma = "R-%02d.%d" % (ti + 1, bi)
            s.them(ma=ma, id_so=s.id_moi("r", 2500), loai="tuyen", vung="Kanto" if ti < 4 else "Johto",
                   cung="Tuyến " + noi, buoc=bi, ten=mau.format(noi=noi)[:45], npc_giao=21,
                   ten_npc=CT.NPC[21], dia_danh=noi, cap_toi_thieu=L, ngay_mo=0, truoc=truoc,
                   kieu_muc_tieu="giet", muc_tieu_id=qid, muc_tieu_ten=qten, so_luong=so_quai(L, 15),
                   lap_lai="khong", exp=ex, xu=xu, luong_khoa=lk, luong=lg, dong_co_tuong_thich=1)
            truoc = ma

    # --- 4.5 Ngay (20 mau x 4 bac cap = 80; chon 3/ngay) ---------------------------------
    for bac, L in enumerate((10, 30, 50, 70)):
        for i, ten in enumerate(CT.NGAY):
            ex, xu, lk, lg = s.thuong("ngay", L)
            kieu = "giet_lech_cap" if i % 3 != 2 else "thu_thap"
            vp_nv = sorted(VP_NV)[i % len(VP_NV)]
            qid, qten, qc = s.chon_quai(L, i)
            mt = (dict(muc_tieu_id="", muc_tieu_ten="quái ±5 cấp", so_luong=so_quai(L, 5))
                  if kieu == "giet_lech_cap" else
                  dict(muc_tieu_id=vp_nv, muc_tieu_ten=VP_NV[vp_nv], so_luong=max(5, so_quai(L, 5) // 5), quai_roi=qid))
            s.them(ma="N-%d.%02d" % (bac + 1, i + 1), id_so=s.id_moi("n", 3000), loai="ngay",
                   vung="Mọi vùng", cung="Bảng nhiệm vụ ngày", ten=ten[:45], npc_giao=21, ten_npc=CT.NPC[21],
                   dia_danh="Trung tâm Pokémon", cap_toi_thieu=L, ngay_mo=0, kieu_muc_tieu=kieu,
                   lap_lai="ngay", gioi_han=GIOI_HAN_NGAY, exp=ex, xu=xu, luong_khoa=lk, luong=lg,
                   dong_co_tuong_thich=1 if kieu == "giet_lech_cap" else 0,
                   ghi_chu="Bậc cấp %d-%d; tối đa %d nhiệm vụ ngày/ngày" % (L, L + 19, GIOI_HAN_NGAY), **mt)

    # --- 4.6 Tuan (12 mau x 3 bac = 36; chon 2/tuan) ------------------------------------
    for bac, L in enumerate((20, 50, 80)):
        for i, ten in enumerate(CT.TUAN):
            ex, xu, lk, lg = s.thuong("tuan", L)
            s.them(ma="T-%d.%02d" % (bac + 1, i + 1), id_so=s.id_moi("t", 3500), loai="tuan",
                   vung="Mọi vùng", cung="Bảng nhiệm vụ tuần", ten=ten[:45], npc_giao=5, ten_npc=CT.NPC[5],
                   dia_danh="Trung tâm Pokémon", cap_toi_thieu=L, kieu_muc_tieu="giet_lech_cap",
                   muc_tieu_ten="quái ±5 cấp", so_luong=so_quai(L, 60), lap_lai="tuan", gioi_han=GIOI_HAN_TUAN,
                   exp=ex, xu=xu, luong_khoa=lk, luong=lg, dong_co_tuong_thich=0,
                   ghi_chu="Reset thứ Hai 05:00; cần bảng tuần (GĐ2)")

    # --- 4.7 Pokedex: 3 bac nghien cuu x loai (gap/ha/shiny) ------------------------------
    loai_dex = sorted(set((t, c) for i, (t, c) in s.quai.items()), key=lambda x: (x[1], x[0]))
    da = set()
    k = 0
    for ten, cap in loai_dex:
        if ten in da or "Shiny" in ten:
            continue
        da.add(ten)
        k += 1
        for bac, (kieu, sl, nhan) in enumerate((("pokedex_gap", 1, "Gặp"), ("pokedex_ha", 50, "Hạ"),
                                               ("pokedex_shiny", 1, "Thấy shiny"))):
            if bac == 2 and k % 3 != 0:
                continue
            ex, xu, lk, lg = s.thuong("pokedex", max(1, cap - 5), he=(0.5, 1.0, 2.0)[bac])
            qid = [i for i, (t, c) in s.quai.items() if t == ten][0]
            s.them(ma="D-%03d.%d" % (k, bac + 1), id_so=s.id_moi("d", 4000), loai="pokedex",
                   vung="Mọi vùng", cung="Nghiên cứu Pokédex", ten=("%s %s: %s" % ("Nghiên cứu", nhan, ten))[:45],
                   npc_giao=8, ten_npc=CT.NPC[8], dia_danh="Phòng nghiên cứu", cap_toi_thieu=max(1, cap - 5),
                   kieu_muc_tieu=kieu, muc_tieu_id=qid, muc_tieu_ten=ten, so_luong=sl, lap_lai="khong",
                   exp=ex, xu=xu, luong_khoa=lk * (bac + 1), luong=0,
                   dong_co_tuong_thich=0, ghi_chu="Đọc tob_pokedex (so_ha); shiny cần cột mới (GĐ2)")

    # --- 4.8 Doi, su pham, Hang bi an, quay lai -----------------------------------------
    for i, (ten, L) in enumerate([("Đội săn Gyarados", 38), ("Đội trinh sát Hang Tối", 50),
                                  ("Đột kích Tổ Zapdos", 60), ("Đêm Hang Darkrai", 70),
                                  ("Bão Rayquaza", 80), ("Liên minh ba vùng", 90),
                                  ("Đội hái Berry", 20), ("Đội bảo vệ Pallet", 15)], 1):
        ex, xu, lk, lg = s.thuong("doi", L)
        s.them(ma="DOI-%02d" % i, id_so=s.id_moi("doi", 5000), loai="doi", vung="Mọi vùng",
               cung="Nhiệm vụ đội", ten=ten, npc_giao=21, ten_npc=CT.NPC[21], dia_danh="Trụ sở đội",
               cap_toi_thieu=L, kieu_muc_tieu="giet_lech_cap", muc_tieu_ten="quái ±5 cấp (cả đội)",
               so_luong=so_quai(L, 40) * 3, lap_lai="tuan", gioi_han=1, exp=ex, xu=xu, luong_khoa=lk,
               luong=lg, dong_co_tuong_thich=1, ghi_chu="mainsub=2 (đội); cả đội cùng tiến độ")
    for i, ten in enumerate(["Dẫn đường người mới", "Kèm cặp tới cấp 20", "Kèm cặp tới cấp 40",
                             "Cùng nhau tiến hoá", "Người thầy tận tâm"], 1):
        L = 30 + i * 5
        ex, xu, lk, lg = s.thuong("su_pham", L)
        s.them(ma="SP-%02d" % i, id_so=s.id_moi("sp", 5100), loai="su_pham", vung="Mọi vùng",
               cung="Sư phạm", ten=ten, npc_giao=5, ten_npc=CT.NPC[5], dia_danh="Pallet",
               cap_toi_thieu=L, kieu_muc_tieu="kem_cap", muc_tieu_ten="học trò cấp thấp hơn ≥ 20",
               so_luong=1, lap_lai="tuan", gioi_han=1, exp=ex, xu=xu, luong_khoa=lk, luong=lg,
               dong_co_tuong_thich=0, ghi_chu="Thầy và trò cùng nhận; trò +20% EXP khi chung đội")
    for i, ten in enumerate(["Hầm ngục bí ẩn: tầng 1-5", "Hầm ngục bí ẩn: tầng 6-10",
                             "Hầm ngục bí ẩn: tầng 11-20", "Hầm ngục bí ẩn: hạt giống ngày",
                             "Hầm ngục bí ẩn: xếp hạng tuần"], 1):
        L = 20 + (i - 1) * 15
        ex, xu, lk, lg = s.thuong("hang_bi_an", L)
        s.them(ma="HM-%02d" % i, id_so=s.id_moi("hm", 5200), loai="hang_bi_an", vung="Mọi vùng",
               cung="Hầm ngục bí ẩn", ten=ten, npc_giao=8, ten_npc=CT.NPC[8], dia_danh="Hang động",
               cap_toi_thieu=L, kieu_muc_tieu="tang_hang", muc_tieu_ten="tầng", so_luong=5 * i,
               lap_lai="ngay", gioi_han=1, exp=ex, xu=xu, luong_khoa=lk, luong=lg,
               dong_co_tuong_thich=0, ghi_chu="Chế độ mới (GĐ3): tầng ngẫu nhiên, hạt giống theo ngày")
    for i, ten in enumerate(["Chào mừng trở lại", "Ôn lại chiêu thức", "Bắt kịp bạn bè"], 1):
        ex, xu, lk, lg = s.thuong("quay_lai", 30)
        s.them(ma="QL-%02d" % i, id_so=s.id_moi("ql", 5300), loai="quay_lai", vung="Mọi vùng",
               cung="Quay lại", ten=ten, npc_giao=5, ten_npc=CT.NPC[5], dia_danh="Pallet",
               cap_toi_thieu=30, kieu_muc_tieu="noi_chuyen" if i == 1 else "giet_lech_cap",
               muc_tieu_ten="quái ±5 cấp", so_luong=1 if i == 1 else 100, lap_lai="khong",
               exp=ex, xu=xu, luong_khoa=lk, luong=lg, dong_co_tuong_thich=0,
               ghi_chu="Chỉ hiện khi vắng ≥ 14 ngày; EXP theo cấp người nhận (GĐ2)")

    # --- 4.9 Thanh tuu -------------------------------------------------------------------
    for i, (ten, kieu, nguong) in enumerate(CT.THANH_TUU, 1):
        ex, xu, lk, lg = s.thuong("thanh_tuu", 1)
        s.them(ma="A-%02d" % i, id_so=s.id_moi("a", 6000), loai="thanh_tuu", vung="Mọi vùng",
               cung="Thành tựu", ten=ten, npc_giao="", dia_danh="", cap_toi_thieu=1,
               kieu_muc_tieu="thanh_tuu_" + kieu, so_luong=nguong, lap_lai="khong",
               exp=0, xu=0, luong_khoa=lk, luong=lg if nguong >= 20 else 0, dong_co_tuong_thich=0,
               ghi_chu="Danh hiệu (DanhHieu, tob_danh_hieu); không cộng chỉ số")


# =====================================================================================
# 5. SU KIEN 52 TUAN
# =====================================================================================

COT_SK = ["tuan", "thang", "ten_nam1", "ten_nam2", "loai", "co_che", "so_ngay", "ma_su_kien",
          "tai_dung", "ngan_sach_gio", "phan_xu_phan_tram", "phan_thuong", "moc_giu_chan"]

# (tuan bat dau, so tuan, ten nam 1, ten nam 2 (xoay vong), loai, co che, ma SuKien, tai dung, gio ngan sach/tuan, thuong, moc)
LICH = [
    (1, 1, "Tết dương lịch", "Tết dương lịch: Pokémon Tuyết", "le_vn", "Cây thông đổi 10 kẹo lấy hộp quà (khoá)", "tetduonglich", "SuKien.TET_DUONG_LICH, npc -35", 2.0, "Hộp quà khoá, khung avatar năm mới", "Đăng nhập 7 ngày"),
    (2, 1, "Tuần hệ Băng", "Tuần hệ Rồng", "tuan_he", "Quái hệ X xuất hiện ×2, chiêu hệ X +10% (thời tiết)", "moi:tuanhe", "MapNapTaiNguyen spawn + ChieuPokemon", 1.0, "Phiến đá hệ trang trí", "Mục tiêu tuần"),
    (3, 1, "Ngày cộng đồng Charmander", "Ngày cộng đồng Squirtle", "cong_dong", "3 giờ: dòng khởi đầu xuất hiện dày, shiny ×5", "moi:congdong", "SuKien + spawn", 1.0, "Trang phục khởi đầu (không chỉ số)", "Giờ vàng chung"),
    (4, 1, "Chuẩn bị Tết: gói bánh", "Chuẩn bị Tết: chợ hoa", "le_vn", "Nồi bánh chưng/bánh tét theo đội", "tet2017", "MapSuKien nấu bánh, tob_nau_banh", 1.5, "Bánh (thuốc khoá), lì xì khoá", "Hoạt động đội"),
    (5, 2, "Tết Nguyên Đán (theo âm lịch: tuần 4-7)", "Tết Nguyên Đán: Lân Litleo", "le_vn", "Cây lì xì Sudowoodo, lì xì khoá, top Tết theo điểm (không theo nạp)", "tet2017,tet123", "SuKien.TET_2017/TET_123", 3.0, "Áo Tết (không chỉ số), danh hiệu năm", "Đỉnh năm: quay lại"),
    (7, 1, "Tuần Shiny", "Tuần Shiny: Johto", "pokemon", "Tỉ lệ shiny ×10, chắc chắn gặp shiny sau 2.000 lần hạ quái", "moi:shiny", "tob_pokedex + cột shiny (GĐ2)", 1.0, "Huy hiệu shiny", "Sưu tầm"),
    (8, 1, "Valentine: Luvdisc", "Valentine: Alomomola", "le_vn", "Tặng hoa cho bạn bè (Hoa hồng bán ở cửa hàng), cặp đôi cùng đội được thêm EXP", "moi:valentine", "data_shop Hoa hồng + MapSuKien", 1.0, "Khung đôi, hiệu ứng trái tim", "Xã hội"),
    (9, 1, "Đột kích huyền thoại: Zapdos", "Đột kích: Articuno", "dot_kich", "Boss thế giới 2 lần/ngày, đi nhóm từ 5 thành viên trở lên", "moi:dotkich", "BossDotKich (Zapdos 82)", 1.5, "Lông vũ Zapdos (nguyên liệu khoá)", "Hẹn giờ chung"),
    (10, 1, "Quốc tế Phụ nữ 8/3", "8/3: Gardevoir", "le_vn", "Quà 8/3, hoa hồng; nhiệm vụ tặng hoa", "quocte83", "SuKien.QUOC_TE_8_3", 1.0, "Ruy băng trang trí", "Xã hội"),
    (11, 1, "Tuần hệ Cỏ", "Tuần hệ Tiên", "tuan_he", "Như tuần 2", "moi:tuanhe", "spawn", 1.0, "Phiến đá trang trí", "Mục tiêu tuần"),
    (12, 1, "Ngày cộng đồng Bulbasaur", "Ngày cộng đồng Pichu", "cong_dong", "Như tuần 3", "moi:congdong", "spawn", 1.0, "Trang phục", "Giờ vàng chung"),
    (13, 1, "Mùa giải PvP 1 kết thúc", "Mùa giải PvP 5 kết thúc", "pvp", "Chốt bậc xếp hạng (từ Poké đến Master), phát thưởng mùa", "moi:muapvp", "MapLienDau/ArenaData, tob_char_thach_dau", 1.0, "Khung bậc mùa, lượng theo bậc", "Cạnh tranh"),
    (14, 1, "Lễ hội Pokédex", "Lễ hội Pokédex: Johto", "pokemon", "Mốc Pokédex ×2 điểm nghiên cứu", "moi:pokedex", "tob_pokedex_thuong", 1.0, "Danh hiệu nghiên cứu", "Sưu tầm"),
    (15, 1, "Giỗ Tổ Hùng Vương", "Giỗ Tổ: bánh giày", "le_vn", "Bánh giày, pháo hoa (EXP theo % cấp, có giới hạn)", "gioto", "SuKien.GIO_TO", 1.5, "Pháo hoa trang trí", "Giờ vàng chung"),
    (16, 1, "Chiến vùng (Kanto/Johto/Hoenn)", "Chiến vùng", "chien_vung", "Thứ Bảy 20h: 3 vùng giành cờ", "moi:chienvung", "MapChienTruongMoba, Moba boss", 1.0, "Cờ vùng (trang trí)", "Đồng đội"),
    (17, 1, "30/4 - 1/5: Thống nhất", "30/4: Diễu hành Pokémon", "le_vn", "Đường đua Ponyta, nhiệm vụ tuyến", "moi:3004", "Thú cưỡi Ponyta/Rapidash", 1.5, "Yên thú cưỡi trang trí", "Nghỉ lễ"),
    (18, 1, "Tuần hệ Lửa", "Tuần hệ Ma", "tuan_he", "Như tuần 2", "moi:tuanhe", "spawn", 1.0, "Phiến đá trang trí", "Mục tiêu tuần"),
    (19, 1, "Ngày cộng đồng Squirtle", "Ngày cộng đồng Eevee", "cong_dong", "Như tuần 3", "moi:congdong", "spawn", 1.0, "Trang phục", "Giờ vàng chung"),
    (20, 1, "Đột kích: Entei", "Đột kích: Raikou", "dot_kich", "Như tuần 9", "moi:dotkich", "Entei 138", 1.5, "Nanh lửa Entei (khoá)", "Hẹn giờ chung"),
    (21, 1, "Bùng phát quần thể", "Bùng phát: Johto", "pokemon", "Mỗi ngày 1 tuyến đường có 1 loài xuất hiện ×5", "moi:bungphat", "spawn", 1.0, "Điểm Pokédex", "Bất ngờ"),
    (22, 1, "Quốc tế Thiếu nhi 1/6", "1/6: Pichu & bạn", "le_vn", "Nhiệm vụ dẫn dắt người mới (thưởng Sư phạm ×2)", "moi:thieunhi", "Sư phạm", 1.0, "Mũ trẻ em", "Người mới"),
    (23, 2, "Sự kiện Hè: biển Vermilion", "Hè: đảo Cinnabar", "mua", "Kem, dưa hấu, câu cá; thêm 50% thời gian năng lượng 100% (năng lượng hè)", "he2017", "SuKien.HE_2017", 1.5, "Đồ bơi trang trí", "Mùa hè: nhiều giờ chơi"),
    (25, 1, "Ngày cộng đồng Pichu", "Ngày cộng đồng Bulbasaur", "cong_dong", "Như tuần 3", "moi:congdong", "spawn", 1.0, "Trang phục", "Giờ vàng chung"),
    (26, 1, "Mùa giải PvP 2 kết thúc", "Mùa giải PvP 6 kết thúc", "pvp", "Như tuần 13", "moi:muapvp", "MapLienDau", 1.0, "Khung bậc mùa", "Cạnh tranh"),
    (27, 1, "Tuần hệ Nước", "Tuần hệ Đất", "tuan_he", "Như tuần 2", "moi:tuanhe", "spawn", 1.0, "Phiến đá", "Mục tiêu tuần"),
    (28, 1, "Đột kích: Darkrai", "Đột kích: Suicune", "dot_kich", "Như tuần 9", "moi:dotkich", "BossDracula (Darkrai 116)", 1.5, "Mảnh bóng tối (khoá)", "Hẹn giờ chung"),
    (29, 1, "Hè 2: Hầm ngục bí ẩn mùa hè", "Hè 2: Hầm ngục sa mạc", "mua", "Hầm ngục bí ẩn ×2 điểm xếp hạng", "moi:hamngucmua", "Hầm ngục (GĐ3)", 1.5, "Khung hầm ngục", "Endgame"),
    (30, 1, "Chiến vùng mùa hè", "Chiến vùng", "chien_vung", "Như tuần 16", "moi:chienvung", "Moba", 1.0, "Cờ vùng", "Đồng đội"),
    (31, 1, "Ngày cộng đồng Eevee", "Ngày cộng đồng Charmander", "cong_dong", "Như tuần 3 + đá tiến hoá ×2", "moi:congdong", "DaTienHoa", 1.0, "Trang phục Eevee", "Giờ vàng chung"),
    (32, 1, "Vu Lan", "Vu Lan: hoa hồng vàng", "le_vn", "Nhánh hoa hồng vàng, đổi quà khoá", "vulan", "SuKien.VU_LAN", 1.0, "Hoa cài trang trí", "Gia đình"),
    (33, 1, "Tuần hệ Điện", "Tuần hệ Thép", "tuan_he", "Như tuần 2", "moi:tuanhe", "spawn", 1.0, "Phiến đá", "Mục tiêu tuần"),
    (34, 1, "Tuần Shiny 2", "Tuần Shiny: huyền thoại", "pokemon", "Như tuần 7", "moi:shiny", "tob_pokedex", 1.0, "Huy hiệu shiny", "Sưu tầm"),
    (35, 1, "Quốc khánh 2/9", "2/9: lễ hội đèn", "le_vn", "Pháo hoa, nhiệm vụ tuyến toàn quốc", "moi:quockhanh", "MapSuKien pháo hoa", 1.5, "Pháo hoa trang trí", "Nghỉ lễ"),
    (36, 1, "Đột kích: Rayquaza", "Đột kích: Lugia", "dot_kich", "Như tuần 9", "moi:dotkich", "BossTruRong (120)", 1.5, "Vảy Rayquaza (khoá)", "Hẹn giờ chung"),
    (37, 2, "Trung Thu", "Trung Thu: Clefairy", "le_vn", "Bánh nướng/bánh dẻo, lồng đèn, thả đèn theo đội", "trungthu", "SuKien.TRUNG_THU, Shop Trung Thu", 1.5, "Lồng đèn trang trí", "Gia đình"),
    (39, 1, "Mùa giải PvP 3 kết thúc", "Mùa giải PvP 7 kết thúc", "pvp", "Như tuần 13", "moi:muapvp", "MapLienDau", 1.0, "Khung bậc mùa", "Cạnh tranh"),
    (40, 1, "Ngày cộng đồng Charmander 2", "Ngày cộng đồng Squirtle 2", "cong_dong", "Như tuần 3", "moi:congdong", "spawn", 1.0, "Trang phục", "Giờ vàng chung"),
    (41, 1, "Phụ nữ Việt Nam 20/10", "20/10: Sylveon", "le_vn", "Hoa, nhiệm vụ tặng quà", "moi:2010", "data_shop hoa", 1.0, "Ruy băng trang trí", "Xã hội"),
    (42, 1, "Tuần hệ Siêu linh", "Tuần hệ Giác đấu", "tuan_he", "Như tuần 2", "moi:tuanhe", "spawn", 1.0, "Phiến đá", "Mục tiêu tuần"),
    (43, 2, "Halloween: Gengar", "Halloween: Mimikyu", "mua", "Bí ngô, mặt nạ, boss Darkrai đêm 31/10", "halloween,demhalloween", "SuKien.HALLOWEEN", 1.5, "Mặt nạ trang trí", "Bất ngờ"),
    (45, 1, "Lễ hội Pokédex 2", "Lễ hội Pokédex: Hoenn", "pokemon", "Như tuần 14", "moi:pokedex", "tob_pokedex_thuong", 1.0, "Danh hiệu", "Sưu tầm"),
    (46, 1, "Nhà giáo 20/11", "20/11: thầy và trò", "le_vn", "Thưởng Sư phạm ×2, chổi băng/độc/điện", "sinhnhat2011", "SuKien.SINH_NHAT_2011", 1.0, "Danh hiệu người thầy", "Người mới"),
    (47, 1, "Black Friday (giảm giá cửa hàng xu)", "Black Friday", "mua", "Giảm 30% đồ trang trí mua bằng xu", "blackfriday", "SuKien.BLACK_FRIDAY", 0.5, "—", "Hố hút xu"),
    (48, 1, "Đột kích: Groudon & Kyogre", "Đột kích: Kyogre & Groudon", "dot_kich", "2 boss cùng lúc", "moi:dotkich", "BossKyogreGroudon (129/130)", 1.5, "Ngọc đỏ Groudon, Ngọc lam Kyogre (khoá)", "Hẹn giờ chung"),
    (49, 1, "Chiến vùng mùa đông", "Chiến vùng", "chien_vung", "Như tuần 16", "moi:chienvung", "Moba", 1.0, "Cờ vùng", "Đồng đội"),
    (50, 1, "Ngày cộng đồng Bulbasaur 2", "Ngày cộng đồng Pichu 2", "cong_dong", "Như tuần 3", "moi:congdong", "spawn", 1.0, "Trang phục", "Giờ vàng chung"),
    (51, 1, "Giáng sinh: Delibird", "Giáng sinh: Abomasnow", "le_vn", "Cây thông Abomasnow, quà Delibird, đêm 24/12", "noel,demnoel", "SuKien.NOEL", 2.0, "Mũ Noel trang trí", "Đỉnh năm"),
    (52, 1, "Tổng kết năm + mùa giải PvP 4", "Tổng kết năm 2", "pvp", "Bảng xếp hạng năm, kỷ niệm máy chủ", "moi:tongket", "SuKienDuaTop, tob_top_thuong", 2.0, "Danh hiệu năm", "Cạnh tranh"),
]


def sinh_su_kien():
    ra = []
    tuan_da = {}
    for (t0, n, t1, t2, loai, cc, ma, td, ns, th, moc) in LICH:
        for k in range(n):
            tuan_da[t0 + k] = (t1 + ("" if n == 1 else " (tuần %d/%d)" % (k + 1, n)), t2, loai, cc,
                              7 * n if k == 0 else 0, ma, td, ns, th, moc)
    for t in range(1, 53):
        if t in tuan_da:
            t1, t2, loai, cc, ngay, ma, td, ns, th, moc = tuan_da[t]
        else:
            t1, t2, loai, cc, ngay, ma, td, ns, th, moc = ("Tuần thường: nhiệm vụ tuần + xếp hạng", "Tuần thường",
                                                           "thuong", "Không có sự kiện, chỉ có bảng xếp hạng tuần", 7, "",
                                                           "—", 0.5, "—", "Mục tiêu tuần")
        ra.append({"tuan": t, "thang": min(12, 1 + (t - 1) * 12 // 52), "ten_nam1": t1, "ten_nam2": t2,
                   "loai": loai, "co_che": cc, "so_ngay": ngay or 7, "ma_su_kien": ma, "tai_dung": td,
                   "ngan_sach_gio": ns, "phan_xu_phan_tram": SU_KIEN_PHAN_XU, "phan_thuong": th,
                   "moc_giu_chan": moc})
    return ra


# =====================================================================================
# 6. GHI FILE
# =====================================================================================


def ghi_csv(duong, cot, dong):
    b = io.StringIO()
    w = csv.DictWriter(b, fieldnames=cot, lineterminator="\n")
    w.writeheader()
    for d in dong:
        w.writerow(d)
    open(duong, "w", encoding="utf-8", newline="").write(b.getvalue())


def mang_java(ten, kieu, gt, moi_dong=10):
    s = "    public static final %s[] %s = {\n" % (kieu, ten)
    for i in range(0, len(gt), moi_dong):
        s += "        " + ", ".join(str(x) + ("L" if kieu == "long" else "") for x in gt[i:i + moi_dong]) + ",\n"
    return s + "    };\n\n"


# Ten mang / so cua ThongSoLauDai NAP TU SQL (bang data_*_lau_dai la NGUON DUY NHAT, P1): file Java chi khai bao
# truong rong, com.htbb.dulieu.DuLieuSql dien luc khoi dong (CSDL) hoac tu sql/htbb_day_du.sql (test, cong cu).
# Doi ten o day phai doi ca DuLieuSql.apDung*.
NAP_SQL_MANG = [
    ("EXP_LEN_CAP", "long[]", "data_cap_do_lau_dai.exp_len_cap: EXP tu L len L+1 (L=1..99); [0] va [100] = 0"),
    ("MOB_EXP", "int[]", "data_cap_do_lau_dai.mob_exp: EXP 1 quai cung cap (de xuat rcvxp)"),
    ("QUAI_MOI_GIO", "int[]", "data_cap_do_lau_dai.quai_moi_gio"),
    ("XU_MOI_QUAI", "int[]", "data_cap_do_lau_dai.xu_moi_quai: xu ky vong / quai"),
    ("GIO_TICH_LUY_PHAN_NGHIN", "int[]", "data_cap_do_lau_dai.gio_tich_luy_phan_nghin: gio hieu dung tich luy de dat cap L (x1000)"),
    ("BAC_TB_CAP", "int[]", "data_nang_cap_lau_dai.cap_bac theo bac"),
    ("BAC_TB_TEN", "String[]", "data_nang_cap_lau_dai.ten_bac theo bac"),
    ("TI_LE_NC", "int[]", "data_nang_cap_lau_dai.ti_le: [k-1] = % len +k"),
    ("DA_NC", "int[]", "data_nang_cap_lau_dai.da"),
    ("BUA_BAO_HO_LUONG_KHOA", "int[]", "data_nang_cap_lau_dai.bua_luong_khoa"),
    ("CHI_SO_NC_PHAN_TRAM", "int[]", "data_nang_cap_lau_dai.chi_so_phan_tram (bang luu luy ke; day la phan them moi cap)"),
    ("CHI_PHI_NC", "long[][]", "data_nang_cap_lau_dai.xu: CHI_PHI_NC[bac][k] = xu cho 1 lan thu len +k (k=1..15), [bac][0] = 0"),
    ("BAC_MET", "int[][]", "data_kinh_te_lau_dai exp.bac_met: {so phut, % toc do} sau khi het gio 100%. Sau bac cuoi: 0"),
]
NAP_SQL_SO = [
    ("MAT_CAP_TU", "data_nang_cap_lau_dai.mat_cap: truot tu +MAT_CAP_TU tro len thi tut 1 cap"),
    ("TI_LE_ROI_XU", "data_kinh_te_lau_dai roi.xu.phan_tram"),
    ("DA_ROI_PHAN_NGHIN", "data_kinh_te_lau_dai roi.da_nang_cap.phan_nghin"),
    ("NANG_LUONG_PHUT_NGAY", "data_kinh_te_lau_dai exp.nang_luong_phut"),
    ("NANG_LUONG_PHUT_TICH_TOI_DA", "data_kinh_te_lau_dai exp.nang_luong_tich_phut"),
    ("THUE_CHO_PHAN_TRAM", "data_kinh_te_lau_dai thue.cho"),
    ("PHI_DANG_BAN_PHAN_TRAM", "data_kinh_te_lau_dai thue.dang_ban"),
    ("TIEU_HAO_PHAN_TRAM", "data_kinh_te_lau_dai tieu_hao.thuoc_phan_tram_thu_nhap"),
    ("SUA_DO_PHAN_TRAM", "data_kinh_te_lau_dai tieu_hao.sua_do_phan_tram_thu_nhap"),
    ("GIFTCODE_TOI_DA_THANG", "data_kinh_te_lau_dai giftcode.toi_da_thang"),
    ("VE_CHUONG_TUAN", "data_kinh_te_lau_dai ve_chuong.tuan"),
    ("VE_CHUONG_TOI_DA", "data_kinh_te_lau_dai ve_chuong.toi_da"),
]


def ghi_java():
    j = []
    j.append("package com.htbb.pokemon;\n\n")
    j.append("/**\n * THONG SO THIET KE DAI HAN (docs/thiet-ke-game.md).\n *\n"
             " * FILE SINH RA boi tools/pokemon/sinh_sql_lau_dai.py - KHONG SUA TAY.\n"
             " * Hai loai so:\n"
             " *   - HANG (static final): so thiet ke CHUA co bang SQL (tran cap theo mua, ngan sach nhiem vu, muc tieu\n"
             " *     mo phong...). Sinh tu bang trong tools/pokemon/sinh_sql_lau_dai.py.\n"
             " *   - NAP TU SQL (static volatile, RONG cho toi khi nap): so co bang data_cap_do_lau_dai /\n"
             " *     data_nang_cap_lau_dai / data_kinh_te_lau_dai - bang la NGUON DUY NHAT. com.htbb.dulieu.DuLieuSql nap\n"
             " *     luc khoi dong (data.DatabaseDuLieuSql, thieu bang = server tu choi chay) hoac tu sql/htbb_day_du.sql\n"
             " *     (test, cong cu mo phong). KHONG gan truong nay ngoai DuLieuSql.\n"
             " * KHONG nham voi real.LevelDetail (bang EXP game goc).\n */\n")
    j.append("public final class ThongSoLauDai {\n\n    private ThongSoLauDai() {\n    }\n\n")
    j.append("    public static final int CAP_TOI_DA = %d;\n\n" % CAP_TOI_DA)
    j.append(mang_java("TRAN_CAP_NGAY", "int", [a for a, b in TRAN_CAP_THEO_NGAY]))
    j.append(mang_java("TRAN_CAP_GIA_TRI", "int", [b for a, b in TRAN_CAP_THEO_NGAY]))
    for ten, v in [("HE_SO_BAT_KIP", HE_SO_BAT_KIP), ("KHOANG_BAT_KIP", KHOANG_BAT_KIP),
                   ("TINH_THONG_PHAN_TRAM_CAP", TINH_THONG_PHAN_TRAM_CAP),
                   ("TINH_THONG_DIEM_MOI_MUA", TINH_THONG_DIEM_MOI_MUA),
                   ("TINH_THONG_TOI_DA", TINH_THONG_TOI_DA),
                   ("TINH_THONG_PHAN_NGHIN_MOI_DIEM", TINH_THONG_PHAN_NGHIN_MOI_DIEM),
                   ("SO_O_TRANG_BI", SO_O_TRANG_BI), ("SO_MON_MUA_MOI_BAC", SO_MON_MUA_MOI_BAC),
                   ("PITY_CHIA", PITY_CHIA),
                   ("GIU_CAP_KHI_DOI_BAC", GIU_CAP_KHI_DOI_BAC),
                   ("DA_TU_PHAN_RA", DA_TU_PHAN_RA),
                   ("DICH_CHUYEN_LAN_NGAY", DICH_CHUYEN_LAN_NGAY),
                   ("DICH_CHUYEN_PHAN_TRAM_GIO", DICH_CHUYEN_PHAN_TRAM_GIO),
                   ("TRAN_GIAO_DICH_TRUC_TIEP_GIO", TRAN_GIAO_DICH_TRUC_TIEP_GIO),
                   ("GIOI_HAN_NGAY", GIOI_HAN_NGAY), ("GIOI_HAN_TUAN", GIOI_HAN_TUAN),
                   ("VE_CHUONG_BAN_DAU", VE_CHUONG_BAN_DAU),
                   ("GIO_MOI_CHUONG_PHAN_NGHIN", int(GIO_MOI_CHUONG * 1000)),
                   ("GIO_MOI_PHU_PHAN_NGHIN", int(GIO_MOI_PHU * 1000)),
                   ("SU_KIEN_PHAN_XU", SU_KIEN_PHAN_XU),
                   ("CAP_TAN_THU", CAP_TAN_THU), ("HE_SO_TAN_THU_PHAN_TRAM", int(HE_SO_TAN_THU * 100)),
                   ("NGOC_HE_CAP_TOI_DA", NGOC_HE_CAP_TOI_DA),
                   ("NGOC_HE_GIA_GIO_PHAN_TRAM", NGOC_HE_GIA_GIO_PHAN_TRAM),
                   ("NGOC_HE_TANG_PHAN_TRAM", NGOC_HE_TANG_PHAN_TRAM),
                   ("TRANG_TRI_PHAN_NGHIN_NGAY", TRANG_TRI_PHAN_NGHIN_NGAY),
                   ("TRANG_TRI_NGUONG_GIO", TRANG_TRI_NGUONG_GIO)]:
        j.append("    public static final int %s = %d;\n" % (ten, v))
    j.append("\n")
    j.append(mang_java("LICH_DANG_NHAP", "int", LICH_DANG_NHAP))
    j.append(mang_java("BAC_TB_NGAY_MO", "int", [b[3] for b in BAC_TB]))
    j.append(mang_java("BAC_TB_GIA", "long", [gia_bac(b[0]) for b in BAC_TB]))
    j.append("    /** Ngan sach thuong theo loai nhiem vu: {gio EXP x1000, gio xu x1000, luong khoa, luong}. */\n")
    j.append("    public static final String[] NGAN_SACH_LOAI = {%s};\n" % ", ".join('"%s"' % k for k in NGAN_SACH))
    j.append("    public static final int[][] NGAN_SACH = {%s};\n\n" % ", ".join(
        "{%d, %d, %d, %d}" % (int(v[0] * 1000), int(v[1] * 1000), v[2], v[3]) for v in NGAN_SACH.values()))
    j.append("    /** Luong theo bac xep hang tuan: {top <=, luong}. */\n")
    j.append("    public static final int[][] LUONG_XEP_HANG_TUAN = {%s};\n\n" % ", ".join(
        "{%d, %d}" % x for x in LUONG_XEP_HANG_TUAN))
    for k, v in MUC_TIEU.items():
        if isinstance(v, float):
            j.append("    public static final int MT_%s_PHAN_TRAM = %d;\n" % (k.upper(), int(round(v * 100))))
        else:
            j.append("    public static final int MT_%s = %d;\n" % (k.upper(), v))
    j.append("\n    // ---- NAP TU SQL (com.htbb.dulieu.DuLieuSql) - rong / 0 cho toi khi nap ----\n\n")
    for ten, kieu, mo_ta in NAP_SQL_MANG:
        rong = "new %s[0]%s" % (kieu.replace("[]", "", 1).replace("[]", ""), "[]" if kieu.count("[]") == 2 else "")
        j.append("    /** %s. */\n" % mo_ta)
        j.append("    public static volatile %s %s = %s;\n" % (kieu, ten, rong))
    j.append("\n")
    for ten, mo_ta in NAP_SQL_SO:
        j.append("    /** %s. */\n" % mo_ta)
        j.append("    public static volatile int %s;\n" % ten)
    j.append("\n    /** EXP 1 gio danh quai o cap L (100%). */\n")
    j.append("    public static long expMoiGio(final int L) {\n        return (long) QUAI_MOI_GIO[L] * MOB_EXP[L];\n    }\n\n")
    j.append("    /** Xu 1 gio danh quai o cap L (100%). */\n")
    j.append("    public static long xuMoiGio(final int L) {\n        return (long) QUAI_MOI_GIO[L] * XU_MOI_QUAI[L];\n    }\n\n")
    j.append("    /** Tran cap tai ngay {@code ngay} ke tu khai truong. */\n")
    j.append("    public static int tranCap(final int ngay) {\n        int t = TRAN_CAP_GIA_TRI[0];\n"
             "        for (int i = 0; i < TRAN_CAP_NGAY.length; i++) {\n            if (ngay >= TRAN_CAP_NGAY[i]) {\n"
             "                t = TRAN_CAP_GIA_TRI[i];\n            }\n        }\n        return t;\n    }\n\n")
    j.append("    /** Ti le thanh cong (%) len +k sau {@code truot} lan truot lien tiep (pity). */\n")
    j.append("    public static int tiLeNangCap(final int k, final int truot) {\n"
             "        final int goc = TI_LE_NC[k - 1];\n        final int them = (goc + PITY_CHIA - 1) / PITY_CHIA;\n"
             "        return Math.min(100, goc + truot * them);\n    }\n\n")
    j.append("    /** He so toc do (%) cho phut thu {@code phut} trong ngay, sau khi da dung het {@code phut100} phut 100%. */\n")
    j.append("    public static int heSoMet(final int phutSau100) {\n        int moc = 0;\n"
             "        for (final int[] b : BAC_MET) {\n            moc += b[0];\n            if (phutSau100 < moc) {\n"
             "                return b[1];\n            }\n        }\n        return 0;\n    }\n}\n")
    open(RA_JAVA, "w", encoding="utf-8", newline="\n").write("".join(j))


def sql_chuoi(s):
    return "'" + str(s).replace("\\", "\\\\").replace("'", "\\'") + "'"


TEN_KIEU = {"giet": "Hạ", "giet_tinh_anh": "Thách đấu", "thu_thap": "Thu", "dua_thu": "Đưa thư",
            "noi_chuyen": "Gặp", "giet_lech_cap": "Hạ"}


def dong_data_quest(d, mainsub):
    """Mot dong data_quest (schema hien co) cho nhiem vu bieu dien duoc bang type 0..4."""
    kieu = d["kieu_muc_tieu"]
    npc = int(d["npc_giao"] or 5)
    if kieu in ("giet", "giet_tinh_anh"):
        typ, iget, mon, nget, nmon = 0, "32000", str(d["muc_tieu_id"]), "32000", str(d["so_luong"])
        nv = "%s %s con %s" % (TEN_KIEU[kieu], d["so_luong"], d["muc_tieu_ten"])
    elif kieu == "thu_thap":
        typ, iget, mon, nget, nmon = 2, str(d["muc_tieu_id"]), str(d["quai_roi"]), str(d["so_luong"]), "32000"
        nv = "Thu %s %s" % (d["so_luong"], d["muc_tieu_ten"])
    elif kieu == "dua_thu":
        typ, iget, mon, nget, nmon = 1, str(VP_THU), "32000", "32000", "32000"
        nv = "Đưa thư"
    elif kieu == "giet_lech_cap":
        typ, iget, mon, nget, nmon = 4, "32000", "32000", "32000", str(d["so_luong"])
        nv = "Hạ %s quái ±5 cấp" % d["so_luong"]
    else:
        typ, iget, mon, nget, nmon = 3, "32000", "32000", "32000", "32000"
        nv = "Gặp %s" % (d["ten_npc"] or "NPC")
    noi = d["dia_danh"] or "—"
    thuong = "%s exp, %s xu" % (d["exp"], d["xu"]) + (", %s lượng khoá" % d["luong_khoa"] if d["luong_khoa"] else "")
    info = "Nhiệm vụ:|%s|Địa điểm:|%s|NPC nv:|%s|Phần thưởng:|%s" % (nv, noi, d["ten_npc"] or "—", thuong)
    content = "1%s>1%s>Cậu có giúp ta không ?" % (
        "Chào cháu, ta là %s ở %s." % (d["ten_npc"] or "người dân", noi), nv + ".")
    res = "1Tốt lắm, cảm ơn cháu!" if typ != 3 else "32000"
    fin = "Nhiệm vụ hoàn thành|Quay về gặp %s|Để nhận thưởng" % (d["ten_npc"] or "NPC")
    vat = d["vat_pham"] or "32000"
    gt = [d["id_so"], sql_chuoi(d["ten"][:45]), npc, npc if typ not in (1, 3) else (23 if typ == 1 else 32000),
          max(1, int(d["cap_toi_thieu"])), mainsub, sql_chuoi(content), sql_chuoi(nv + " ở " + noi),
          sql_chuoi(res), sql_chuoi(iget), sql_chuoi(mon), typ, sql_chuoi(nget), sql_chuoi(nmon),
          d["xu"], d["exp"], "'32000'", "'32000'", sql_chuoi(vat), 5 if typ == 4 else 0,
          sql_chuoi(info[:200]), sql_chuoi(fin), 0, d["luong"] or 0, d["luong_khoa"] or 0]
    return "(" + ",".join(str(x) for x in gt) + ")"


def ghi_sql(nv, sk, quai):
    o = []
    o.append("-- SINH RA boi tools/pokemon/sinh_sql_lau_dai.py - KHONG SUA TAY.\n")
    o.append("-- Thiet ke dai han (docs/thiet-ke-game.md). CHI tao TABLE tam (staging):\n")
    o.append("-- KHONG doi table dang chay (data_quest, data_shop, data_monster...). Giai doan 2 moi\n")
    o.append("-- ap dung (docs/thiet-ke-game.md). Da gom vao xuat_sql_day_du.sh (GD2, sau POKEMON).\n")
    o.append("SET NAMES utf8;\n\n")
    # 1. Cap do
    o.append("DROP TABLE IF EXISTS `data_cap_do_lau_dai`;\n")
    o.append("CREATE TABLE `data_cap_do_lau_dai` (\n  `cap` int NOT NULL,\n  `exp_len_cap` bigint NOT NULL,\n"
             "  `gio_tich_luy_phan_nghin` int NOT NULL,\n  `mob_exp` int NOT NULL,\n  `quai_moi_gio` int NOT NULL,\n"
             "  `xu_moi_quai` int NOT NULL,\n  `exp_cu` bigint NOT NULL,\n  PRIMARY KEY (`cap`)\n) DEFAULT CHARSET=utf8;\n")
    rows = ["(%d,%d,%d,%d,%d,%d,%d)" % (L, EXP_LEN_CAP[L], int(round(GIO_TL[L] * 1000)), MOB_EXP[L], KPH[L],
                                        XU_QUAI[L], EXP_CU[L] if L < len(EXP_CU) else 0)
            for L in range(1, CAP_TOI_DA + 1)]
    o.append("INSERT INTO `data_cap_do_lau_dai` VALUES\n" + ",\n".join(rows) + ";\n\n")
    # 2. Nang cap
    o.append("DROP TABLE IF EXISTS `data_nang_cap_lau_dai`;\n")
    o.append("CREATE TABLE `data_nang_cap_lau_dai` (\n  `bac` int NOT NULL,\n  `ten_bac` varchar(20) NOT NULL,\n"
             "  `cap_bac` int NOT NULL,\n  `cong` int NOT NULL,\n  `ti_le` int NOT NULL,\n  `pity_moi_lan` int NOT NULL,\n"
             "  `xu` bigint NOT NULL,\n  `da` int NOT NULL,\n  `bua_luong_khoa` int NOT NULL,\n  `mat_cap` tinyint NOT NULL,\n"
             "  `chi_so_phan_tram` int NOT NULL,\n  PRIMARY KEY (`bac`,`cong`)\n) DEFAULT CHARSET=utf8;\n")
    rows = []
    for b in BAC_TB:
        for k in range(1, 16):
            rows.append("(%d,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d)" % (
                b[0], sql_chuoi(b[1]), b[2], k, TI_LE_NC[k - 1], -(-TI_LE_NC[k - 1] // PITY_CHIA),
                chi_phi_nc(b[0], k), DA_NC[k - 1], BUA_BAO_HO_LUONG_KHOA[k - 1], 1 if k >= MAT_CAP_TU else 0,
                sum(CHI_SO_NC_PHAN_TRAM[:k])))
    o.append("INSERT INTO `data_nang_cap_lau_dai` VALUES\n" + ",\n".join(rows) + ";\n\n")
    # 3. Cau hinh roi + kinh te (khoa/gia tri) -> giai doan 2 nap vao cau_hinh
    o.append("DROP TABLE IF EXISTS `data_kinh_te_lau_dai`;\n")
    o.append("CREATE TABLE `data_kinh_te_lau_dai` (\n  `khoa` varchar(64) NOT NULL,\n  `gia_tri` varchar(64) NOT NULL,\n"
             "  `gia_tri_cu` varchar(64) NOT NULL,\n  `ghi_chu` varchar(255) NOT NULL,\n  PRIMARY KEY (`khoa`)\n) DEFAULT CHARSET=utf8;\n")
    kt = [("roi.xu.phan_tram", TI_LE_ROI_XU, "0.32", "Xu rơi mỗi quái (%); lượng = xu_moi_quai/0.3"),
          ("roi.luong.phan_tram", 0, "2.5", "Bỏ lượng rơi từ quái (Monster.java:1091-1108)"),
          ("roi.luong_khoa.phan_tram", 0, "2.5", "Bỏ lượng khoá rơi từ quái"),
          ("roi.da_nang_cap.phan_nghin", DA_ROI_PHAN_NGHIN, "0", "Đá nâng cấp khi |lệch cấp| <= 5"),
          ("roi.trang_bi.ppm", 150, "150", "Giữ; thêm phân rã đồ trùng"),
          ("exp.tang_exp", 1, "3", "Bỏ x3 toàn cục (CharCapNhat.java:127)"),
          ("exp.tile_lv50", 100, "10", "Bỏ giảm 90% từ cấp 50 (bảng EXP mới đã tính)"),
          ("exp.tile_duoi_lv50", 100, "50", "Bỏ giảm 50%"),
          ("exp.chia3_lv60", 0, "1", "Bỏ chia 3 từ cấp 60"),
          ("exp.lech_cap", "100,100,100,100,80,80,80,50,50,50,20,20,20,20,20,5", "1/5/10/20",
           "% EXP theo lệch cấp người - quái = 0..15+"),
          ("exp.nang_luong_phut", int(NANG_LUONG_GIO_NGAY * 60), "0", "Phút 100% mỗi ngày"),
          ("exp.nang_luong_tich_phut", int(NANG_LUONG_TICH_TOI_DA * 60), "0", "Tích luỹ tối đa"),
          ("exp.bac_met", ",".join("%d:%d" % (int(a * 60), b) for a, b in BAC_MET), "0",
           "Sau giờ 100%: " + ", ".join("%d phút %d%%" % (int(a * 60), b) for a, b in BAC_MET) + ", rồi 0"),
          ("thue.cho", THUE_CHO_PHAN_TRAM, "10", "Thuế chợ ký gửi (đốt)"),
          ("thue.dang_ban", PHI_DANG_BAN_PHAN_TRAM, "0", "Phí đăng bán (đốt, không hoàn)"),
          ("thue.chuyen_luong", 5, "0", "Phí chuyển lượng NPC (%)"),
          ("tieu_hao.thuoc_phan_tram_thu_nhap", TIEU_HAO_PHAN_TRAM, "-", "Giá thuốc chỉnh theo bậc"),
          ("tieu_hao.sua_do_phan_tram_thu_nhap", SUA_DO_PHAN_TRAM, "-", "Σ price/10 giữ, price theo bậc"),
          ("nang_cap.xu_moi_lan_cu", 0, "5000", "Thay bằng data_nang_cap_lau_dai.xu"),
          ("qua.cap35_xu", 0, "10000000", "Bỏ (MapXuLyLenh.java:303); thay bằng trang bị bậc vàng"),
          ("giftcode.toi_da_thang", GIFTCODE_TOI_DA_THANG, "-", "Chỉ vật phẩm khoá, ≤ 0,5 giờ thu nhập"),
          ("ve_chuong.tuan", VE_CHUONG_TUAN, "-", "Vé mở chương chính / tuần"),
          ("ve_chuong.toi_da", VE_CHUONG_TOI_DA, "-", "")]
    o.append("INSERT INTO `data_kinh_te_lau_dai` VALUES\n" + ",\n".join(
        "(%s,%s,%s,%s)" % (sql_chuoi(a), sql_chuoi(b), sql_chuoi(c), sql_chuoi(d)) for a, b, c, d in kt) + ";\n\n")
    # 4. Gia shop de xuat
    o.append("DROP TABLE IF EXISTS `data_shop_gia_lau_dai`;\n")
    o.append("CREATE TABLE `data_shop_gia_lau_dai` (\n  `id_shop` int NOT NULL,\n  `ten` varchar(100) NOT NULL,\n"
             "  `gia_cu` bigint NOT NULL,\n  `tien_cu` tinyint NOT NULL,\n  `gia_moi` bigint NOT NULL,\n"
             "  `tien_moi` tinyint NOT NULL COMMENT '0 luong,1 xu,2 luong khoa,9 ngung ban',\n"
             "  `gioi_han_tuan` int NOT NULL,\n  `ly_do` varchar(255) NOT NULL,\n  PRIMARY KEY (`id_shop`)\n) DEFAULT CHARSET=utf8;\n")
    o.append("INSERT INTO `data_shop_gia_lau_dai` VALUES\n" + ",\n".join(SHOP_ROWS) + ";\n\n")
    # 5. Quai de xuat
    o.append("DROP TABLE IF EXISTS `data_monster_lau_dai`;\n")
    o.append("CREATE TABLE `data_monster_lau_dai` (\n  `id` int NOT NULL,\n  `ten` varchar(45) NOT NULL,\n"
             "  `cap` int NOT NULL,\n  `maxhp_cu` int NOT NULL,\n  `maxhp` int NOT NULL,\n  `attack_cu` int NOT NULL,\n"
             "  `attack` int NOT NULL,\n  `defent_cu` int NOT NULL,\n  `defent` int NOT NULL,\n  `rcvxp_cu` int NOT NULL,\n"
             "  `rcvxp` int NOT NULL,\n  `rcvgold_cu` int NOT NULL,\n  `rcvgold` int NOT NULL,\n  PRIMARY KEY (`id`)\n) DEFAULT CHARSET=utf8;\n")
    rows = []
    for i in QUAI_DA_DAT:
        q = quai[i]
        L = int(q["level"])
        hp, at, th = quai_de_xuat(L)
        rows.append("(%d,%s,%d,%s,%d,%s,%d,%s,%d,%s,%d,%s,%d)" % (
            i, sql_chuoi(q["name"]), L, q["maxhp"], hp, q["attack"], at, q["defent"], th, q["rcvxp"],
            MOB_EXP[L], q["rcvgold"], int(round(XU_QUAI[L] * 100 / TI_LE_ROI_XU))))
    o.append("INSERT INTO `data_monster_lau_dai` VALUES\n" + ",\n".join(rows) + ";\n\n")
    # 6. Nhiem vu: (a) day du (moi loai) (b) dang data_quest cho cac dong bieu dien duoc
    COT_NV_CHUOI = ("ten", "ghi_chu", "cung", "muc_tieu_ten", "truoc", "huy_hieu", "dia_danh", "ten_npc", "vat_pham",
                    "vung", "ma", "loai", "kieu_muc_tieu", "lap_lai", "muc_tieu_id", "quai_roi", "npc_giao", "chuong",
                    "buoc", "gioi_han")
    o.append("DROP TABLE IF EXISTS `data_quest_lau_dai`;\n")
    o.append("CREATE TABLE `data_quest_lau_dai` (\n" + "".join(
        "  `%s` %s NOT NULL,\n" % (c, "varchar(255)" if c in ("ten", "ghi_chu", "cung", "muc_tieu_ten", "truoc",
                                                            "huy_hieu", "dia_danh", "ten_npc", "vat_pham", "vung")
                                  else ("varchar(32)" if c in ("ma", "loai", "kieu_muc_tieu", "lap_lai", "muc_tieu_id",
                                                              "quai_roi", "npc_giao", "chuong", "buoc", "gioi_han")
                                        else "bigint")) for c in COT_NV) +
             "  PRIMARY KEY (`ma`)\n) DEFAULT CHARSET=utf8;\n")
    rows = []
    for d in nv:
        # Cot bigint de trong ('') -> 0: MariaDB/MySQL strict mode tu choi '' vao cot so (GD2: chay
        # xuat_sql_day_du.sh tren MariaDB). NovaDB thi nhan ca hai.
        rows.append("(" + ",".join(
            ("0" if (d[c] == "" and c not in COT_NV_CHUOI) else (sql_chuoi(d[c]) if not isinstance(d[c], (int,)) or c in (
                "chuong", "buoc", "npc_giao", "muc_tieu_id", "quai_roi", "gioi_han") else str(d[c]))) for c in COT_NV) + ")")
    for i in range(0, len(rows), 100):
        o.append("INSERT INTO `data_quest_lau_dai` VALUES\n" + ",\n".join(rows[i:i + 100]) + ";\n")
    o.append("\n-- data_quest_moi: CUNG schema voi data_quest. Thu tu chen = thu tu chuoi chinh\n"
             "-- (server danh so lai theo thu tu doc, DatabaseXepHang.java:481). Giai doan 2: doi ten table.\n")
    # Khong dung CREATE TABLE ... LIKE (NovaDB khong ho tro): chep nguyen schema tu dump.
    goc = open(DUMP, encoding="utf-8").read()
    m = re.search(r"CREATE TABLE `data_quest` \((.*?)\n\)", goc, re.S)
    o.append("DROP TABLE IF EXISTS `data_quest_moi`;\nCREATE TABLE `data_quest_moi` (" + m.group(1)
             + "\n) DEFAULT CHARSET=utf8;\n")
    rows = []
    for d in nv:
        if str(d["dong_co_tuong_thich"]) != "1" or d["nam"] != 1:
            continue
        ms = {"huong_dan": 0, "chinh": 0, "doi": 2}.get(d["loai"], 1)
        rows.append(_dong_quest()(d, ms))
    for i in range(0, len(rows), 100):
        o.append("INSERT INTO `data_quest_moi` (`id`,`name`,`npcrcv`,`npcres`,`lv`,`mainsub`,`content`,`scontent`,"
                 "`rescontent`,`idItemget`,`idMonskill`,`type`,`nitemget`,`nmonskill`,`gold`,`exp`,`potiongift`,"
                 "`npotion`,`itemgift`,`deltaLv`,`infogift`,`infofinish`,`pactive`,`luong`,`luongLock`) VALUES\n"
                 + ",\n".join(rows[i:i + 100]) + ";\n")
    # 7. Su kien
    o.append("\nDROP TABLE IF EXISTS `data_su_kien_nam`;\n")
    o.append("CREATE TABLE `data_su_kien_nam` (\n" + "".join(
        "  `%s` %s NOT NULL,\n" % (c, "int" if c in ("tuan", "thang", "so_ngay", "phan_xu_phan_tram") else
                                 ("decimal(5,2)" if c == "ngan_sach_gio" else "varchar(255)")) for c in COT_SK) +
             "  PRIMARY KEY (`tuan`)\n) DEFAULT CHARSET=utf8;\n")
    o.append("INSERT INTO `data_su_kien_nam` VALUES\n" + ",\n".join(
        "(" + ",".join(str(r[c]) if c in ("tuan", "thang", "so_ngay", "phan_xu_phan_tram", "ngan_sach_gio")
                       else sql_chuoi(r[c]) for c in COT_SK) + ")" for r in sk) + ";\n")
    o.append("\n-- ====================== GIAI DOAN 2 (DE TRONG, CHUA CHAY) ======================\n"
             "-- UPDATE data_shop s JOIN data_shop_gia_lau_dai g ON g.id_shop=s.id\n"
             "--   SET s.price=g.gia_moi, s.type=IF(g.tien_moi=9,s.type,g.tien_moi), s.sell=IF(g.tien_moi=9,0,s.sell);\n"
             "-- UPDATE data_monster m JOIN data_monster_lau_dai q ON q.id=m.id\n"
             "--   SET m.maxhp=q.maxhp, m.attack=q.attack, m.defent=q.defent, m.rcvxp=q.rcvxp, m.rcvgold=q.rcvgold;\n"
             "-- RENAME TABLE data_quest TO data_quest_cu, data_quest_moi TO data_quest;\n")
    open(RA_SQL, "w", encoding="utf-8", newline="\n").write("".join(o))


SHOP_ROWS = []
EXP_CU = []


def lap_shop(shop):
    """Gia shop: bo do tang suc manh bang luong; ve EXP -> luong khoa gioi han tuan; thuoc theo bac."""
    for r in shop:
        i, ten, gia, typ, sell = int(r["id"]), r["name"], int(r["price"]), int(r["type"]), int(r["sell"])
        if sell == 0:
            continue
        tl = ten.lower()
        moi, tien, gh, ly = gia, typ, 0, ""
        if "giờ vàng" in tl:
            moi, tien, gh, ly = 30, 2, 1, "Vé EXP: chỉ lượng khoá, 1/tuần (không mua sức mạnh bằng lượng)"
        elif "kẹo tiềm năng" in tl or "kẹo kỹ năng" in tl:
            moi, tien, gh, ly = 20, 2, 1, "Đặt lại điểm: lượng khoá, 1/tuần"
        elif "-cộng" in tl.replace(" ", "") or "tăng 50%" in tl or "tăng 100%" in tl or "tấn công" in tl:
            moi, tien, gh, ly = gia, 9, 0, "Có chỉ số -> ngừng bán; bản trang trí bán ở shop sự kiện"
        elif "thuốc hồi máu" in tl or "ether" in tl:
            moi, tien, gh, ly = gia * 2, 2 if typ == 0 else typ, 0, "Thuốc: lượng khoá (nguồn: nhiệm vụ ngày)"
        elif typ == 1 and gia >= 100000:
            moi, tien, ly = int(gia * 0.5), 1, "Xu: giữ hố hút, giảm 50% vì thu nhập xu mới thấp hơn ở cấp thấp"
        elif typ == 0:
            moi, tien, ly = gia, 0, "Trang trí: giữ lượng (lượng chỉ từ xếp hạng/thành tựu)"
        else:
            ly = "Giữ"
        SHOP_ROWS.append("(%d,%s,%d,%d,%d,%d,%d,%s)" % (i, sql_chuoi(ten[:100]), gia, typ, moi, tien, gh, sql_chuoi(ly)))


def doc_exp_cu():
    s = open(os.path.join(GOC, "server", "src", "real", "LevelDetail.java"), encoding="utf-8").read()
    m = re.search(r"public static long\[\] expRequire = new long\[\]\{([^}]*)\}", s)
    return [int(x.strip().rstrip("L")) for x in m.group(1).split(",") if x.strip()]


def _dong_quest():
    """Dong data_quest_moi: dung ban co hoi thoai cua sinh_nhiem_vu neu co."""
    try:
        import sinh_nhiem_vu as NVM
        return NVM.dong_data_quest
    except ImportError:
        return dong_data_quest


def main():
    global EXP_CU
    cot, dong = doc_dump()
    quai = {int(q["id"]): q for q in bang(cot, dong, "data_monster")}
    EXP_CU = doc_exp_cu()
    vp = []
    with open(BANG_VP, encoding="utf-8") as f:
        for r in csv.DictReader(f):
            if r["type"].strip().isdigit() and "giữ tên" not in r["ten_moi"] and r["cap"].lstrip("-").isdigit():
                vp.append((int(r["id"]), int(r["type"]), int(r["cap"]), r["ten_moi"]))
    s = SinhNV({i: (quai[i]["name"], int(quai[i]["level"])) for i in QUAI_DA_DAT}, vp)
    s.ten_quai_tat_ca = {i: (q["name"], int(q["level"])) for i, q in quai.items()}
    sinh_nhiem_vu(s)
    import sinh_nhiem_vu as NVM
    sk = sinh_su_kien()
    lap_shop(bang(cot, dong, "data_shop"))
    ghi_csv(RA_CSV_NV, COT_NV + NVM.COT_THEM, s.dong)
    ghi_csv(RA_CSV_SK, COT_SK, sk)
    cap_rows = [{"cap": L, "exp_len_cap": EXP_LEN_CAP[L], "exp_cu": EXP_CU[L] if L < len(EXP_CU) else "",
                 "gio_tich_luy": "%.2f" % GIO_TL[L], "gio_cap": "%.2f" % ((GIO_TL[L + 1] - GIO_TL[L]) if L < CAP_TOI_DA else 0),
                 "quai_moi_gio": KPH[L], "mob_exp": MOB_EXP[L], "exp_moi_gio": EXP_GIO[L], "xu_moi_quai": XU_QUAI[L],
                 "xu_moi_gio": XU_GIO[L], "tan_cong_tran": tan_cong_tran(L),
                 "quai_hp": quai_de_xuat(L)[0], "quai_tc": quai_de_xuat(L)[1], "quai_thu": quai_de_xuat(L)[2]}
                for L in range(1, CAP_TOI_DA + 1)]
    ghi_csv(RA_CSV_CAP, list(cap_rows[0].keys()), cap_rows)
    ghi_java()
    ghi_sql(s.dong, sk, quai)
    NVM.sau_khi_ghi(s.dong)
    dem = {}
    for d in s.dong:
        dem[d["loai"]] = dem.get(d["loai"], 0) + 1
    print("nhiem vu:", len(s.dong), dem)
    print("tuong thich data_quest:", sum(1 for d in s.dong if str(d["dong_co_tuong_thich"]) == "1"))
    print("su kien:", len(sk), "tuan; shop:", len(SHOP_ROWS))
    print("EXP 1->91: %.3g  (cu %.3g)" % (sum(EXP_LEN_CAP[1:91]), sum(EXP_CU[1:91])))
    for k in (1, 7, 10, 13, 15):
        print("nang cap +%d: ky vong %.2f lan" % (k, ky_vong_nc(k)))


if __name__ == "__main__":
    main()
