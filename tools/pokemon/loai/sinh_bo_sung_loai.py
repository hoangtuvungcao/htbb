#!/usr/bin/env python3
"""
Sinh khoi "them_loai_bo_sung.sql" trong sql/htbb.sql (phan B) (ngay sau khoi them_loai_day_du.sql): bo sung cho 1.025 loai
  - data_loai.mo_ta      mo ta Pokedex tieng Viet TU VIET (tools/pokemon/loai/mo_ta_loai.json, nguon duy nhat);
                         chi ghi o dong con trong (khong ghi de mo ta nguoi van hanh da sua tren web);
  - data_loai_sprite     cot moi: shiny (co goi <NNNN>s.goi ban sac mau PMDCollab), kich_thuoc_shiny, ghi_cong_shiny,
                         neo (goi co khoi neo phu kien PMD -> trang phuc / the ban dong hanh ve duoc), va kich_thuoc
                         cap nhat theo goi hien tai (goi them khoi neo).

    python3 -I tools/pokemon/loai/sinh_bo_sung_loai.py           # ghi sql/htbb.sql
    python3 -I tools/pokemon/loai/sinh_bo_sung_loai.py --kiem    # chi kiem khoi hien tai khop (thoat 1 neu lech)

Idempotent, MariaDB 10.3, utf8: ALTER ... ADD COLUMN IF NOT EXISTS, bang tam + UPDATE JOIN (khong INSERT...SELECT...ON
DUPLICATE KEY UPDATE). Chay sau khoi them_loai_day_du.sql (khoi do tao bang + dong). Sau khi chay tren CSDL may nha:
vps/xuat_csdl_day_du.sh de cap nhat phan A cua sql/htbb.sql.
"""
import json
import os
import re
import sys
import unicodedata

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
SQL = os.path.join(GOC, "sql", "htbb.sql")
MO_TA = os.path.join(GOC, "tools", "pokemon", "loai", "mo_ta_loai.json")
GOI = os.path.join(GOC, "server", "map", "loai")
RAW = os.path.join(GOC, "tools", "pokemon", "pmd_raw")
KHOI = "them_loai_bo_sung.sql"
SAU = "them_loai_day_du.sql"
TOI_DA_MO_TA = 255


def q(s):
    return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'"


def nap_ghi_cong():
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import ghi_cong_loai  # noqa: E402  (cung thu muc; khong chay main)
    sg = ghi_cong_loai.nap_sinh_ghi_cong()
    return sg, sg.doc_ten()


def nguoi_ve(sg, ten, k):
    ds = []
    cr = os.path.join(RAW, k, "credits.txt")
    if os.path.exists(cr):
        with open(cr, encoding="utf-8") as f:
            for d in f:
                p = d.rstrip("\n").split("\t")
                if len(p) >= 2:
                    n = sg.ten_nguoi_ve(p[1].strip(), ten)
                    if n not in ds:
                        ds.append(n)
    return ", ".join(ds)


def ghi_cong_cu():
    """Ghi cong shiny da sinh lan truoc (giu khi may khong co pmd_raw/<NNNN>s/credits.txt)."""
    cu = {}
    try:
        s = open(SQL, encoding="utf-8").read()
    except OSError:
        return cu
    a = s.find("-- >>> " + KHOI + "\n")
    if a < 0:
        return cu
    b = s.find("\n-- >>> ", a + 1)
    for m in re.finditer(r"^\((\d+),(\d),(\d+),(\d+),(\d),'((?:[^'\\]|\\.)*)'\)", s[a:b], re.M):
        cu[int(m.group(1))] = m.group(6).replace("\\'", "'").replace("\\\\", "\\")
    return cu


def sinh():
    with open(MO_TA, encoding="utf-8") as f:
        mt = json.load(f)["mo_ta"]
    loi = []
    for d in range(1, 1026):
        s = unicodedata.normalize("NFC", mt.get(str(d), "")).strip()
        if not s:
            loi.append("thieu mo ta %d" % d)
        elif len(s) > TOI_DA_MO_TA:
            loi.append("mo ta %d dai %d > %d" % (d, len(s), TOI_DA_MO_TA))
        mt[str(d)] = s
    sg, ten = nap_ghi_cong()
    cu = ghi_cong_cu()
    sp = []
    for d in range(1, 1026):
        k = "%04d" % d
        f, fs = os.path.join(GOI, k + ".goi"), os.path.join(GOI, k + "s.goi")
        if not os.path.isfile(f):
            continue
        sh = os.path.isfile(fs)
        gc = nguoi_ve(sg, ten, k + "s") if sh else ""
        if sh and not gc:
            gc = cu.get(d, "")
        if sh and not gc:
            loi.append("thieu ghi cong shiny %s (chay tai_sprite_loai.py --shiny)" % k)
        sp.append((d, 1 if sh else 0, os.path.getsize(f), os.path.getsize(fs) if sh else 0, 1, gc))
    # ban nhap / dang khac (du_lieu/loai/sprite_nhap.csv, tai_sprite_nhap.py)
    nhap = {}
    fn = os.path.join(GOC, "du_lieu", "loai", "sprite_nhap.csv")
    if os.path.isfile(fn):
        for x in open(fn, encoding="utf-8").read().splitlines()[1:]:
            p = x.split(",")
            if len(p) >= 4 and not p[0].endswith("s"):
                nhap[int(p[0])] = (1 if p[3] == "1" else 0, (p[1] if p[3] == "1" else "dang " + p[2] + " " + p[1])[:64])
    # loai mo moi (Gen 3 + vong xoay Vung hoang da): bat chi ghi lan dau o khoi them_loai_day_du -> cap nhat o day
    import csv as _csv
    mo_moi = []
    with open(os.path.join(GOC, "du_lieu", "loai", "bang_loai.csv"), encoding="utf-8") as f:
        for x in _csv.DictReader(f):
            if x["bat"] == "1" and int(x["dex"]) > 251:
                mo_moi.append(int(x["dex"]))
    r = [
        "-- >>> " + KHOI,
        "-- " + "=" * 117,
        "-- HTBB - BO SUNG 1.025 LOAI (loai-bo-sung): mo ta Pokedex tieng Viet, ban sac mau (shiny), neo phu kien goi tai.",
        "-- TEP SINH (dung sua tay): python3 -I tools/pokemon/loai/sinh_bo_sung_loai.py tu tools/pokemon/loai/mo_ta_loai.json",
        "--   (mo ta TU VIET tu su kien he / noi song / dac diem / tien hoa, khong chep Pokedex chinh thuc) va server/map/loai",
        "--   (<NNNN>.goi + <NNNN>s.goi: PMDCollab SpriteCollab 4702f10d888d19e804c5986a91170dfc92ae01b8, CC BY-NC 4.0).",
        "-- Idempotent, MariaDB 10.3+, utf8. Chay SAU khoi them_loai_day_du.sql. mo_ta chi ghi o dong dang rong (giu mo ta",
        "-- nguoi van hanh da sua tren web). Kiem: server/test/KiemLoaiBoSung.",
        "--   data_loai_sprite.shiny            1 = co goi ban sac mau <NNNN>s.goi (loai do moi duoc tung shiny ngoai troi)",
        "--   data_loai_sprite.neo              1 = goi co khoi neo phu kien PMD (trang phuc / the ban dong hanh tren loai tai)",
        "-- " + "=" * 117,
        "",
        "ALTER TABLE `data_loai_sprite`",
        "  ADD COLUMN IF NOT EXISTS `shiny` TINYINT NOT NULL DEFAULT 0 COMMENT '1 = co goi ban sac mau server/map/loai/<NNNN>s.goi',",
        "  ADD COLUMN IF NOT EXISTS `kich_thuoc_shiny` INT NOT NULL DEFAULT 0,",
        "  ADD COLUMN IF NOT EXISTS `ghi_cong_shiny` VARCHAR(255) NOT NULL DEFAULT '',",
        "  ADD COLUMN IF NOT EXISTS `neo` TINYINT NOT NULL DEFAULT 0 COMMENT '1 = goi co khoi neo phu kien PMD',",
        "  ADD COLUMN IF NOT EXISTS `ban_nhap` TINYINT NOT NULL DEFAULT 0 COMMENT '1 = sprite PMDCollab chua hoan thien',",
        "  ADD COLUMN IF NOT EXISTS `nguon_sprite` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'ban nhap / dang khac: nhanh PMDCollab';",
        "",
        "DROP TEMPORARY TABLE IF EXISTS `tam_loai_sprite_bo_sung`;",
        "CREATE TEMPORARY TABLE `tam_loai_sprite_bo_sung` (`dex` SMALLINT NOT NULL PRIMARY KEY, `shiny` TINYINT NOT NULL,"
        " `kich_thuoc` INT NOT NULL, `kich_thuoc_shiny` INT NOT NULL, `neo` TINYINT NOT NULL,"
        " `ghi_cong_shiny` VARCHAR(255) NOT NULL) ENGINE=MEMORY DEFAULT CHARSET=utf8;",
        "INSERT INTO `tam_loai_sprite_bo_sung` (`dex`, `shiny`, `kich_thuoc`, `kich_thuoc_shiny`, `neo`, `ghi_cong_shiny`) VALUES",
    ]
    r.append(",\n".join("(%d,%d,%d,%d,%d,%s)" % (d, sh, kt, kts, neo, q(gc)) for d, sh, kt, kts, neo, gc in sp) + ";")
    r += [
        "UPDATE `data_loai_sprite` s JOIN `tam_loai_sprite_bo_sung` t ON t.`dex` = s.`dex`",
        "  SET s.`shiny` = t.`shiny`, s.`kich_thuoc` = t.`kich_thuoc`, s.`kich_thuoc_shiny` = t.`kich_thuoc_shiny`,",
        "      s.`neo` = t.`neo`, s.`ghi_cong_shiny` = t.`ghi_cong_shiny`;",
        "DROP TEMPORARY TABLE IF EXISTS `tam_loai_sprite_bo_sung`;",
        "UPDATE `data_loai_sprite` SET `ban_nhap` = 0, `nguon_sprite` = '';",
    ]
    for d_, (bn, ng) in sorted(nhap.items()):
        r.append("UPDATE `data_loai_sprite` SET `ban_nhap` = %d, `nguon_sprite` = %s WHERE `dex` = %d;" % (bn, q(ng), d_))
    r += [
        "",
        "-- Mo loai moi (dot B Gen 3 Hoenn + Vung hoang da xoay vong Gen 4-9): bat chi ghi lan dau o khoi them_loai_day_du.",
        "UPDATE `data_loai` SET `bat` = 1 WHERE `bat` = 0 AND `dex` IN (%s);" % ",".join(map(str, mo_moi)),
        "",
        "DROP TEMPORARY TABLE IF EXISTS `tam_loai_mo_ta`;",
        "CREATE TEMPORARY TABLE `tam_loai_mo_ta` (`dex` SMALLINT NOT NULL PRIMARY KEY, `mo_ta` VARCHAR(255) NOT NULL)"
        " ENGINE=MEMORY DEFAULT CHARSET=utf8;",
    ]
    for i in range(0, 1025, 205):
        r.append("INSERT INTO `tam_loai_mo_ta` (`dex`, `mo_ta`) VALUES")
        r.append(",\n".join("(%d,%s)" % (d, q(mt[str(d)])) for d in range(i + 1, min(1025, i + 205) + 1)) + ";")
    r += [
        "UPDATE `data_loai` l JOIN `tam_loai_mo_ta` t ON t.`dex` = l.`dex` SET l.`mo_ta` = t.`mo_ta` WHERE l.`mo_ta` = '';",
        "DROP TEMPORARY TABLE IF EXISTS `tam_loai_mo_ta`;",
        "",
        "-- Het khoi " + KHOI + " (%d mo ta, %d goi, %d goi shiny)." % (1025, len(sp), sum(1 for x in sp if x[1])),
    ]
    return "\n".join(r) + "\n", loi, sp


def main(argv):
    khoi, loi, sp = sinh()
    for l in loi:
        print("LOI", l)
    with open(SQL, encoding="utf-8") as f:
        s = f.read()
    a = s.find("-- >>> " + KHOI + "\n")
    if a >= 0:
        b = s.find("\n-- >>> ", a + 1)
        moi = s[:a] + khoi + (s[b + 1:] if b >= 0 else "")
    else:
        a = s.find("-- >>> " + SAU + "\n")
        b = s.find("\n-- >>> ", a + 1)
        if a < 0 or b < 0:
            print("LOI khong thay khoi", SAU)
            return 1
        moi = s[:b + 1] + khoi + s[b + 1:]
    if "--kiem" in argv:
        ok = moi == s and not loi
        print("Khoi %s %s" % (KHOI, "khop" if ok else "LECH (chay lai bo sinh)"))
        return 0 if ok else 1
    with open(SQL, "w", encoding="utf-8", newline="\n") as f:
        f.write(moi)
    print("Da ghi khoi %s: 1025 mo ta, %d goi (%d shiny)%s" % (KHOI, len(sp), sum(1 for x in sp if x[1]),
                                                                 ", LOI %d" % len(loi) if loi else ""))
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
