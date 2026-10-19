#!/usr/bin/env python3
"""Sinh sql/them_p1_du_lieu_sql.sql: MIGRATION cho CSDL dang chay (production) cua giai doan P1.

Tu P1 may chu game DOC cac bang du lieu thiet ke duoi day luc khoi dong (com.htbb.dulieu.DuLieuSql,
com.htbb.cauhinh.DanhMucSql) va TU CHOI khoi dong neu bang quan trong thieu / rong. CSDL dung tu
sql/htbb_day_du.sql truoc P1 chua co bang cau_hinh_nhom va cot cau_hinh_danh_muc.thu_tu. Tep nay chep
NGUYEN khoi DROP/CREATE/INSERT cua tung bang tu tep sinh goc (khong tu viet lai so lieu), nen chay lai
bao nhieu lan cung duoc va luon ra dung noi dung repo. Chi la bang du lieu sinh: khong dong vao du lieu
nguoi choi.

Nap (TRUOC khi deploy jar P1):  mysql htbb_db < sql/them_p1_du_lieu_sql.sql
Sinh lai (o goc repo, sau khi chay lai cac bo sinh):  python3 -I tools/pokemon/sinh_them_p1.py
Kiem (CI/test):  python3 -I tools/pokemon/sinh_them_p1.py --kiem   (ma thoat 1 neu tep lech)
test/KiemDuLieuSql.java doi chieu tung dong cua tep nay voi sql/htbb_day_du.sql.
"""
import os
import re
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RA = os.path.join(GOC, "sql", "them_p1_du_lieu_sql.sql")

# (tep sinh, bang, bo sinh) - thu tu = thu tu nap.
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


def khoi(tep, bang):
    """Moi lenh DROP / CREATE / INSERT cua `bang` trong tep, giu thu tu."""
    s = open(os.path.join(GOC, "sql", tep), encoding="utf-8").read()
    mau = re.compile(r"^(DROP TABLE IF EXISTS|CREATE TABLE(?: IF NOT EXISTS)?|INSERT(?: IGNORE)? INTO)\s+`%s`" % bang)
    ra = [l for l in lenh(s) if mau.match(l)]
    if not any(l.startswith("CREATE TABLE") for l in ra) or not any(l.startswith("INSERT") for l in ra):
        sys.exit("%s: khong thay CREATE/INSERT cua %s" % (tep, bang))
    if not ra[0].startswith("DROP TABLE"):
        ra.insert(0, "DROP TABLE IF EXISTS `%s`" % bang)
    return ra


def sinh():
    o = ["-- MIGRATION P1 (CSDL dang chay): bang du lieu thiet ke ma may chu game doc luc khoi dong.",
         "-- SINH RA boi tools/pokemon/sinh_them_p1.py - KHONG SUA TAY. Chep nguyen khoi tu tep sinh goc.",
         "-- Nap TRUOC khi deploy jar P1:  mysql htbb_db < sql/them_p1_du_lieu_sql.sql   (chay lai duoc).",
         "-- CSDL moi khong can tep nay: sql/htbb_day_du.sql da gom san. Khong dong vao du lieu nguoi choi.",
         "SET NAMES utf8mb4;", ""]
    for tep, bang, bo in BANG:
        o.append("-- %s (sql/%s, %s)" % (bang, tep, bo))
        for l in khoi(tep, bang):
            o.append(l + ";")
        o.append("")
    return "\n".join(o)


def main():
    nd = sinh()
    cu = open(RA, encoding="utf-8").read() if os.path.exists(RA) else None
    if "--kiem" in sys.argv:
        if cu != nd:
            print("TEP LECH: chay python3 -I tools/pokemon/sinh_them_p1.py")
            sys.exit(1)
        return
    if cu != nd:
        open(RA, "w", encoding="utf-8", newline="\n").write(nd)
        print("da ghi", os.path.relpath(RA, GOC))
    print("%d bang" % len(BANG))


if __name__ == "__main__":
    main()
