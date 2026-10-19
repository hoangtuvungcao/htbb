#!/usr/bin/env python3
"""
Sinh du_lieu/sql_nguon/pokemon_thuat_ngu.sql: migration UPDATE doi THUAT NGU CU con sot trong du lieu goc (chu nguoi
choi/admin thay) sang thuat ngu chuan (docs/thiet-ke-game.md, tu dien ChuPokemon.DOI).

Quet MOI cot chu (varchar/char/text) cua MOI bang co khoa chinh trong htbb_db + account cua mot CSDL
dung tu dau BANG xuat_sql_day_du.sh nhung CHUA chay migration nay (BO_THUAT_NGU=1). O nao con tu cam
(ChuPokemon.TU_CAM) -> UPDATE ... SET cot=<doi(chu)> WHERE <khoa chinh> AND cot=<chu cu>;
  - chay lai bao nhieu lan cung duoc (lan 2 khong khop "cot=<chu cu>" nua);
  - KHONG dung vao o admin/nguoi choi da sua (chu khac chu cu thi khong khop);
  - chi cham dung hang du lieu goc (khoa chinh lay tu CSDL sach), khong dung du lieu nguoi choi.
Khong doi cot khoa chinh, khong doi chu khong dau co "bang" (bảng/băng: "dong bang 5 muc tieu").

Chay (o thu muc HTBB/, CSDL rieng KHONG phai CSDL that - xuat_sql_day_du.sh xoa va tao lai):
  BO_THUAT_NGU=1 MYSQL="mysql -uroot -h127.0.0.1 -P3307" MYSQLDUMP="mysqldump ..." sh tools/pokemon/xuat_sql_day_du.sh
  MYSQL="mysql -uroot -h127.0.0.1 -P3307" python3 -I tools/pokemon/sinh_sql_thuat_ngu.py
  MYSQL=... MYSQLDUMP=... sh tools/pokemon/xuat_sql_day_du.sh      # dump co ca migration nay
Them CSDL_CU=<csdl nap tu dump cu> de sinh them phan "nang cap may chu dang chay" (giu lai khi sinh lai khong co CSDL_CU).
"""
import importlib.util
import os
import re
import shlex
import subprocess
import sys
import unicodedata

_D = os.path.dirname(os.path.abspath(__file__))
_sp = importlib.util.spec_from_file_location("chu_pokemon", os.path.join(_D, "chu_pokemon.py"))
cp = importlib.util.module_from_spec(_sp)
_sp.loader.exec_module(cp)

GOC = cp.GOC
RA = os.path.join(GOC, "du_lieu", "sql_nguon", "pokemon_thuat_ngu.sql")
CSDL = ("htbb_db", "account")


def mysql(sql):
    lenh = shlex.split(os.environ.get("MYSQL", "mysql -uroot")) + [
        "--default-character-set=utf8", "-B", "-N", "-e", sql]
    ra = subprocess.run(lenh, check=True, stdout=subprocess.PIPE).stdout.decode("utf8")
    return [d.split("\t") for d in ra.split("\n") if d != ""]


def bo_thoat_batch(s):
    """Gia tri trong che do -B cua mysql (\\n \\t \\\\ \\0) -> chu that."""
    ra, i = [], 0
    while i < len(s):
        c = s[i]
        if c == "\\" and i + 1 < len(s):
            ra.append({"n": "\n", "t": "\t", "0": "\0", "\\": "\\"}.get(s[i + 1], s[i + 1]))
            i += 2
            continue
        ra.append(c)
        i += 1
    return "".join(ra)


def con_tu_cam(cam, t):
    t = unicodedata.normalize("NFC", t)
    khong_dau = re.search(r"[^\x00-\x7f]", t) is None
    return [g for g in cam.tim(t) if not (khong_dau and g.lower() == "bang" and t.strip() not in ("Bang", "BANG"))]


def main():
    b = cp.doc_bang()
    td = cp.tu_dien_doi(b)
    cam = cp.tu_dien_cam(b)
    khoa = {}
    for db, bang, cot in mysql("SELECT table_schema, table_name, column_name FROM information_schema.key_column_usage "
                               "WHERE constraint_name='PRIMARY' AND table_schema IN ('htbb_db','account') "
                               "ORDER BY table_schema, table_name, ordinal_position"):
        khoa.setdefault((db, bang), []).append(cot)
    cot_chu = {}
    for db, bang, cot in mysql("SELECT table_schema, table_name, column_name FROM information_schema.columns "
                               "WHERE table_schema IN ('htbb_db','account') AND data_type IN "
                               "('varchar','char','text','tinytext','mediumtext','longtext') "
                               "ORDER BY table_schema, table_name, ordinal_position"):
        if (db, bang) in khoa and cot not in khoa[(db, bang)]:
            cot_chu.setdefault((db, bang), []).append(cot)
    cau = []
    dem = {}
    for (db, bang), cs in sorted(cot_chu.items()):
        k = khoa[(db, bang)]
        chon = ", ".join("`%s`" % x for x in k + cs)
        for r in mysql("SELECT %s FROM `%s`.`%s` ORDER BY %s" % (chon, db, bang, ", ".join("`%s`" % x for x in k))):
            r = [bo_thoat_batch(x) for x in r]
            gk = r[:len(k)]
            for c, v in zip(cs, r[len(k):]):
                if v == "NULL" or not v:
                    continue
                tim = con_tu_cam(cam, v)
                if not tim:
                    continue
                moi = td.doi(unicodedata.normalize("NFC", v))
                if moi == v:
                    continue
                dk = " AND ".join("`%s`='%s'" % (x, cp.thoat_sql(y)) for x, y in zip(k, gk))
                ten = "`%s`" % bang if db == "htbb_db" else "`%s`.`%s`" % (db, bang)
                cau.append("-- %s.%s.%s [%s]\nUPDATE %s SET `%s`='%s' WHERE %s AND `%s`='%s';" % (
                    db, bang, c, "|".join(tim), ten, c, cp.thoat_sql(moi), dk, c, cp.thoat_sql(v)))
                dem["%s.%s.%s" % (db, bang, c)] = dem.get("%s.%s.%s" % (db, bang, c), 0) + 1
    # Nang cap may chu DANG CHAY: CSDL_CU = ten CSDL nap tu dump CU (truoc dot doi thuat ngu, vd
    # sql/htbb_day_du.sql ban truoc doi ten `htbb_db` -> `htbb_cu`). O nao o ban cu con thuat ngu cu (hoac ten
    # da bo nhu "Tháp Pokémon" cua Van Tien Tran) ma ban moi da doi -> UPDATE ... WHERE <khoa> AND cot=<chu cu>.
    # Tren CSDL moi dung tu dau khong khop o nao (chu cu khong con) nen vo hai.
    cu = os.environ.get("CSDL_CU")
    nang_cap = []
    if cu:
        bo_cu = set(r[0] for r in mysql("SELECT table_name FROM information_schema.tables WHERE table_schema='%s'" % cu))
        da_co = set(c.split("\n")[1] for c in cau)
        for (db, bang), cs in sorted(cot_chu.items()):
            if db != "htbb_db" or bang not in bo_cu:
                continue
            k = khoa[(db, bang)]
            cot_cu = set(r[0] for r in mysql("SELECT column_name FROM information_schema.columns WHERE table_schema='%s' "
                                             "AND table_name='%s'" % (cu, bang)))
            cs2 = [c for c in cs if c in cot_cu]
            if not cs2 or not all(x in cot_cu for x in k):
                continue
            chon = ", ".join("n.`%s`" % x for x in k) + ", " + ", ".join("n.`%s`, o.`%s`" % (c, c) for c in cs2)
            noi = " AND ".join("n.`%s` = o.`%s`" % (x, x) for x in k)
            for r in mysql("SELECT %s FROM `htbb_db`.`%s` n JOIN `%s`.`%s` o ON %s" % (chon, bang, cu, bang, noi)):
                r = [bo_thoat_batch(x) for x in r]
                gk = r[:len(k)]
                for i, c in enumerate(cs2):
                    moi, cu_v = r[len(k) + 2 * i], r[len(k) + 2 * i + 1]
                    if moi == "NULL" or cu_v == "NULL" or moi == cu_v:
                        continue
                    if con_tu_cam(cam, moi):
                        moi = td.doi(unicodedata.normalize("NFC", moi))
                    if moi == cu_v or not (con_tu_cam(cam, cu_v) or "Tháp Pokémon" in cu_v):
                        continue
                    if "Tháp Pokémon" in cu_v and "Tháp Pokémon" in moi:
                        continue
                    dk = " AND ".join("`%s`='%s'" % (x, cp.thoat_sql(y)) for x, y in zip(k, gk))
                    st = "UPDATE `%s` SET `%s`='%s' WHERE %s AND `%s`='%s';" % (
                        bang, c, cp.thoat_sql(moi), dk, c, cp.thoat_sql(cu_v))
                    if st not in da_co:
                        nang_cap.append("-- nang cap: %s.%s\n%s" % (bang, c, st))
        print("  nang cap may chu dang chay (CSDL_CU=%s): %d cau" % (cu, len(nang_cap)))
    else:
        # giu phan nang cap da sinh truoc do (khong co CSDL cu de tinh lai)
        if os.path.exists(RA):
            cu_txt = open(RA, encoding="utf8").read().split("\n")
            for i, d in enumerate(cu_txt):
                if d.startswith("-- nang cap: ") and i + 1 < len(cu_txt):
                    nang_cap.append(d + "\n" + cu_txt[i + 1])
    with open(RA, "w", encoding="utf8", newline="\n") as f:
        f.write("-- Thuat ngu chuan (docs/thiet-ke-game.md): doi thuat ngu cu con sot trong DU LIEU GOC.\n")
        f.write("-- SINH TU tools/pokemon/sinh_sql_thuat_ngu.py - KHONG SUA TAY (sua tu dien ChuPokemon.DOI roi sinh lai).\n")
        f.write("-- Chay SAU moi file du_lieu/sql_nguon/pokemon_* khac (xuat_sql_day_du.sh: buoc THUAT_NGU, cuoi cung).\n")
        f.write("-- Chay lai an toan: moi cau chi khop o con dung chu cu (WHERE ... AND cot=<chu cu>).\n")
        f.write("SET NAMES utf8;\n")
        for c in cau:
            f.write(c + "\n")
        if nang_cap:
            f.write("-- ===== Nang cap may chu dang chay (ten da doi trong dot nay; CSDL moi khong khop o nao) =====\n")
            for c in nang_cap:
                f.write(c + "\n")
    for k, v in sorted(dem.items()):
        print("  %-50s %d" % (k, v))
    print("Da ghi %s (%d cau UPDATE)" % (os.path.relpath(RA, GOC), len(cau)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
