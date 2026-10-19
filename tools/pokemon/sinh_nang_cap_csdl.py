#!/usr/bin/env python3
"""Sinh vps/nang_cap_csdl.sql: MOT tep nang cap CSDL dang chay (production) len schema + du lieu thiet ke hien tai.

Dung cho moi CSDL HTBB dung tu sql/htbb_day_du.sql cu (ke ca production 0.9.2). CSDL moi KHONG can: nap thang
sql/htbb_day_du.sql. Chay qua vps/nang_cap_csdl.sh (sao luu truoc, roi nap tep nay, roi kiem).

Ba phan, theo thu tu:
  1. Schema (khong pha): CREATE TABLE IF NOT EXISTS cua cac tep them_bang_*.sql, cot moi (ADD COLUMN IF NOT EXISTS,
     viet lai tu them_cot_*.sql), noi cot tob_char_quest.itemquest (sua_cot_itemquest.sql). Bang nguoi choi chi duoc
     THEM bang / THEM cot / NOI cot - khong DROP, DELETE, UPDATE.
  2. Du lieu thiet ke ma may chu doc luc khoi dong (P1): chep NGUYEN khoi DROP/CREATE/INSERT cua 8 bang tu tep sinh goc
     (du_lieu/sql_nguon/pokemon_*.sql) - gom danh muc cau hinh trung tam (cau_hinh_danh_muc + cau_hinh_nhom, ca khoa
     tu_dong.* cua 0.9.2). Day chi la bang sinh, khong chua du lieu nguoi choi.
  3. Chi muc bang nong (them_chi_muc_production.sql, CREATE INDEX IF NOT EXISTS).
Moi lenh deu chay lai duoc: nap hai lan cho cung ket qua.

Phan 4: lenh cau hinh he thong chay lai duoc (5h_systems.tangnap = 1 cua 0.9.4).

KHONG gom (co ly do): DELETE FROM board_created trong sua_he_so_nap_va_qua_tao_nv.sql (xoa qua tao nhan vat chua
nhan cua nguoi choi - quyet dinh kinh te cua chu game, production da chay; nang cap khong xoa du lieu nguoi choi),
vo_hieu_giftcode_cu.sql (khoa giftcode cu - thao tac cua chu game), them_bang_top_boss.sql (server tu tao khi can, htbb_day_du.sql cung khong co),
them_khoa_duy_nhat_giftcode_log.sql (xoa dong trung - server tu them khoa luc khoi dong),
cac migration du lieu cu (skin, world cup, phi phong, vo hieu giftcode, xoa su kien trung) - da nam trong moi CSDL
dung tu htbb_day_du.sql tu 09/2026, va UPDATE/INSERT du lieu khong chay lai an toan duoc.

Sinh lai (o goc repo, sau khi chay lai cac bo sinh):  python3 -I tools/pokemon/sinh_nang_cap_csdl.py
Kiem (CI/test):  python3 -I tools/pokemon/sinh_nang_cap_csdl.py --kiem   (ma thoat 1 neu tep lech)
test/KiemDuLieuSql.java doi chieu tung dong phan 2 voi sql/htbb_day_du.sql va kiem phan 1 khong pha bang nguoi choi.
"""
import os
import re
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
NGUON = os.path.join(GOC, "du_lieu", "sql_nguon")
RA = os.path.join(GOC, "vps", "nang_cap_csdl.sql")

# Phan 1: tep schema (thu tu = thu tu trong tools/pokemon/xuat_sql_day_du.sh).
SCHEMA = ["them_bang_2buoc.sql", "them_bang_cam_may.sql", "them_bang_dieu_khien_server.sql", "them_bang_su_kien.sql",
          "them_bang_tob_log_online.sql", "them_bang_tob_top_thuong.sql",
          "them_bang_web_admin_log.sql", "them_bang_web_chan_spam.sql", "them_bang_web_lenh_nhan_vat.sql",
          "them_bang_tob_client_dang_nhap.sql",
          "them_cot_kho_xu_luong.sql", "them_cot_pokemon.sql", "them_bang_pokedex.sql", "them_bang_lau_dai.sql",
          "sua_cot_itemquest.sql"]

# Phan 2: (tep sinh, bang, bo sinh).
BANG = [
    ("pokemon_lau_dai.sql", "data_cap_do_lau_dai", "tools/pokemon/sinh_sql_lau_dai.py"),
    ("pokemon_lau_dai.sql", "data_nang_cap_lau_dai", "tools/pokemon/sinh_sql_lau_dai.py"),
    ("pokemon_lau_dai.sql", "data_kinh_te_lau_dai", "tools/pokemon/sinh_sql_lau_dai.py"),
    ("pokemon_lau_dai.sql", "data_su_kien_nam", "tools/pokemon/sinh_sql_lau_dai.py"),
    ("pokemon_he_sinh_thai.sql", "data_hst_ho_cau", "tools/pokemon/sinh_sql_he_sinh_thai.py"),
    ("pokemon_noi_dung.sql", "data_noi_dung_trang_bi", "tools/pokemon/sinh_noi_dung.py"),
    ("pokemon_cau_hinh_trung_tam.sql", "cau_hinh_danh_muc", "tools/cauhinh/sinh_cau_hinh.py"),
    ("pokemon_cau_hinh_trung_tam.sql", "cau_hinh_nhom", "tools/cauhinh/sinh_cau_hinh.py"),
]

# Phan 3.
CHI_MUC = ["them_chi_muc_production.sql"]

# Phan 4: cau hinh he thong (khong phai du lieu nguoi choi) - chi lay DUNG lenh liet ke, lenh khac trong tep bo qua.
HE_THONG = [("sua_he_so_nap_va_qua_tao_nv.sql",
             "UPDATE `5h_systems` SET status = 1 WHERE command = 'tangnap' AND status <> 1")]


def lenh(sql):
    """Tach cau lenh theo ';' ngoai chuoi (chuoi '...' thoat bang \\ hoac '')."""
    ra, buf, i, n = [], [], 0, len(sql)
    while i < n:
        c = sql[i]
        if c == "'":
            j = i + 1
            while j < n:
                if sql[j] == "\\":
                    j += 2
                    continue
                if sql[j] == "'":
                    if j + 1 < n and sql[j + 1] == "'":
                        j += 2
                        continue
                    break
                j += 1
            buf.append(sql[i:j + 1])
            i = j + 1
            continue
        if c == "-" and sql.startswith("--", i):
            k = sql.find("\n", i)
            k = n if k < 0 else k + 1
            if not "".join(buf).strip():
                i = k
                continue
            buf.append(sql[i:k])
            i = k
            continue
        if c == ";":
            ra.append("".join(buf).strip())
            buf = []
            i += 1
            continue
        buf.append(c)
        i += 1
    if "".join(buf).strip():
        ra.append("".join(buf).strip())
    return ra


def doc(tep):
    return open(os.path.join(NGUON, tep), encoding="utf-8").read()


def khoi(tep, bang):
    """Moi lenh DROP / CREATE / INSERT cua `bang` trong tep, giu thu tu."""
    mau = re.compile(r"^(DROP TABLE IF EXISTS|CREATE TABLE(?: IF NOT EXISTS)?|INSERT(?: IGNORE)? INTO)\s+`%s`" % bang)
    ra = [l for l in lenh(doc(tep)) if mau.match(l)]
    if not any(l.startswith("CREATE TABLE") for l in ra) or not any(l.startswith("INSERT") for l in ra):
        sys.exit("%s: khong thay CREATE/INSERT cua %s" % (tep, bang))
    if not ra[0].startswith("DROP TABLE"):
        ra.insert(0, "DROP TABLE IF EXISTS `%s`" % bang)
    return ra


def bo_chu_thich(l):
    """Bo dong chu thich '--' o dau lenh (lenh() giu chu thich dinh lien sau mot lenh)."""
    return "\n".join(d for d in l.split("\n") if not d.lstrip().startswith("--")).strip()


def lenh_schema(tep):
    """Lenh cua mot tep schema, viet lai cho chay lai duoc; lenh loai khac = dung (khong doan)."""
    ra = []
    for l in lenh(doc(tep)):
        l = bo_chu_thich(l)
        if not l or re.match(r"(?i)^(SELECT|SET NAMES)\s", l):  # SELECT kiem tay / SET NAMES trong tep goc: bo
            continue
        if re.match(r"(?i)^CREATE TABLE IF NOT EXISTS\s", l) or re.match(r"(?i)^INSERT IGNORE INTO\s", l):
            ra.append(l)
        elif re.match(r"(?i)^ALTER TABLE\s", l):
            if re.search(r"(?i)\b(DROP|RENAME|CHANGE)\b", l):
                sys.exit("%s: ALTER pha cot/bang - khong dua vao nang cap: %s" % (tep, l[:120]))
            l2 = re.sub(r"(?i)\bADD COLUMN\s+(?!IF NOT EXISTS)", "ADD COLUMN IF NOT EXISTS ", l)
            if not re.search(r"(?i)ADD COLUMN IF NOT EXISTS|\sMODIFY\s", l2):
                sys.exit("%s: ALTER khong phai ADD COLUMN / MODIFY: %s" % (tep, l[:120]))
            ra.append(l2)
        else:
            sys.exit("%s: lenh khong chay lai an toan duoc: %s" % (tep, l[:120]))
    if not ra:
        sys.exit("%s: khong co lenh" % tep)
    return ra


def lenh_chi_muc(tep):
    ra = []
    for l in lenh(doc(tep)):
        l = bo_chu_thich(l)
        if not l or re.match(r"(?i)^SELECT\s", l):
            continue
        if not re.match(r"(?i)^CREATE (UNIQUE )?INDEX IF NOT EXISTS\s", l):
            sys.exit("%s: lenh khong phai CREATE INDEX IF NOT EXISTS: %s" % (tep, l[:120]))
        ra.append(l)
    return ra


def sinh():
    o = ["-- NANG CAP CSDL dang chay (htbb_db) len schema + du lieu thiet ke hien tai. CHAY LAI DUOC.",
         "-- SINH RA boi tools/pokemon/sinh_nang_cap_csdl.py - KHONG SUA TAY. Nguon: du_lieu/sql_nguon/.",
         "-- Chay:  sudo bash /opt/htbb/vps/nang_cap_csdl.sh   (sao luu -> nap tep nay -> kiem)",
         "--    hoac:  mysql -u root htbb_db < vps/nang_cap_csdl.sql",
         "-- CSDL moi khong can tep nay: sql/htbb_day_du.sql da gom san.",
         "-- Bang nguoi choi chi duoc THEM bang / THEM cot / NOI cot; khong DROP / DELETE / UPDATE du lieu nguoi choi.",
         "SET NAMES utf8mb4;", "",
         "-- ======== 1. Schema (khong pha, chay lai duoc)", ""]
    for tep in SCHEMA:
        o.append("-- du_lieu/sql_nguon/%s" % tep)
        for l in lenh_schema(tep):
            o.append(l + ";")
        o.append("")
    o += ["-- ======== 2. Du lieu thiet ke may chu doc luc khoi dong (P1): bang sinh, DROP + CREATE + INSERT", ""]
    for tep, bang, bo in BANG:
        o.append("-- %s (du_lieu/sql_nguon/%s, %s)" % (bang, tep, bo))
        for l in khoi(tep, bang):
            o.append(l + ";")
        o.append("")
    o += ["-- ======== 3. Chi muc bang nong", ""]
    for tep in CHI_MUC:
        o.append("-- du_lieu/sql_nguon/%s" % tep)
        for l in lenh_chi_muc(tep):
            o.append(l + ";")
        o.append("")
    o += ["-- ======== 4. Cau hinh he thong (0.9.4: he so nap co dinh = 1)", ""]
    for tep, can in HE_THONG:
        ds = [bo_chu_thich(l) for l in lenh(doc(tep))]
        if can not in ds:
            sys.exit("%s: khong con lenh '%s'" % (tep, can))
        o.append("-- du_lieu/sql_nguon/%s (chi lenh nay; DELETE board_created la quyet dinh rieng cua chu game)" % tep)
        o.append(can + ";")
        o.append("")
    return "\n".join(o)


def main():
    nd = sinh()
    cu = open(RA, encoding="utf-8").read() if os.path.exists(RA) else None
    if "--kiem" in sys.argv:
        if cu != nd:
            print("TEP LECH: chay python3 -I tools/pokemon/sinh_nang_cap_csdl.py")
            sys.exit(1)
        return
    if cu != nd:
        open(RA, "w", encoding="utf-8", newline="\n").write(nd)
        print("da ghi", os.path.relpath(RA, GOC))
    print("%d tep schema, %d bang du lieu, %d tep chi muc" % (len(SCHEMA), len(BANG), len(CHI_MUC)))


if __name__ == "__main__":
    main()
