#!/usr/bin/env python3
"""
G7.C: quet tu cam (kiem hiep) con sot trong chu nguoi choi thay.

Quet 3 nguon:
  java  chuoi "..." trong src/**/*.java (bo chu thich, bo dong log System.out/err,
        printStackTrace, saveOrtherLog; bo tool admin src/server/AdminPanel.java, src/server/Panel/)
  cs    chuoi "..." trong <client PC>/Assets/Scripts/**/*.cs (client_pc_ios; bo Debug.Log)
  sql   CSDL SAU KHI chay dump + cac file du_lieu/sql_nguon/pokemon_*.sql (ca du_lieu/sql_nguon/pokemon_doi_chu.sql)

Chi tinh chuoi "giong chu" (co ky tu ngoai ASCII hoac bat dau bang chu hoa) - chuoi khoa/SQL/log
khong dau bi bo qua.

Moi chuoi Java/C# duoc tinh HAI lan: "tho" (nguyen van) va "sau doi" (qua tu dien
ChuPokemon). "Sau doi" la cai nguoi choi thay khi bat che do Pokemon, vi:
  - server doi o cac cua gui chu (MessageCreator: thong bao, menu, hop thoai, NPC, nhiem vu,
    ten vat pham/ngoc/shop...) va doi bang chu dung chung luc bat may (ChuPokemon.ap());
  - client doi MOI chu ve ra man hinh (MFont._drawString -> ChuPokemon.D()).
Chuoi CSDL tinh nguyen van (CSDL phai sach tu goc).

Ra: du_lieu/tu_cam_con_lai.csv - moi o CON tu cam SAU doi (cot gio_dau = 1 neu thuoc
nhom "gio choi dau tien", cot chu_so_huu = viec khac dang giu file do).

Tu 18/10/2026 (docs/thiet-ke-game.md) nguon phai sach ca TRUOC khi doi: tu cam = ChuPokemon.TU_CAM
(mo rong: phi phong, liên đấu, vũ khí, ngựa, công thành, thần thú...), ngoai le = ChuPokemon.CHO_PHEP +
"bang" trong chu khong dau (bảng/băng). Them client J2ME/Android vao bao cao. Test test/KiemThuatNgu.java quet
them web (HTBB-Web)/SQL dump (THO, phai 0).

Chay (o thu muc HTBB/): python3 -I tools/pokemon/quet_tu_cam.py [--kiem] [--tho]
  --kiem: thoat ma 1 neu nhom gio dau con tu cam.
  --tho:  thoat ma 1 neu con tu cam THO (truoc doi) o bat ky chuoi nao.
"""
import csv
import importlib.util
import os
import re
import sys

_P = os.path.join(os.path.dirname(os.path.abspath(__file__)), "chu_pokemon.py")
_spec = importlib.util.spec_from_file_location("chu_pokemon", _P)
cp = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(cp)

GOC = cp.GOC
RA = os.path.join(GOC, "du_lieu", "tu_cam_con_lai.csv")
SRC = os.path.join(GOC, "server", "src")
_DC = os.path.join(os.path.dirname(os.path.abspath(__file__)), "duong_client.py")
if not os.path.exists(_DC):
    _DC = os.path.join(GOC, "tools", "pokemon", "duong_client.py")   # 943e989b: duong_client.py o lai tools/pokemon
_sd = importlib.util.spec_from_file_location("duong_client", _DC)
dc = importlib.util.module_from_spec(_sd)
_sd.loader.exec_module(dc)
# Ma C# client Unity (client_pc_ios/Assets, doi bang HTBB_CLIENT_ASSETS).
CS = dc.scripts(dc.assets())
NHAN_CS = "client_pc_ios/Assets/Scripts/Assembly-CSharp/"
# Client J2ME / Android: chu ve ra qua chu.bin, nguon cung phai sach.
CLIENT = GOC
JAVA_CLIENT = [("client_jar", os.path.join(GOC, "client_jar", "src")), ("client_apk", os.path.join(GOC, "client_apk", "pokemon", "src"))]

# Khong quet: bang du lieu tu dien (chua tu cam co chu dich) va tool admin (khong phai nguoi choi).
BO_QUA = {
    "server/src/com/htbb/pokemon/ChuPokemon.java",
    "server/src/server/AdminPanel.java",
    "client_pc_ios/Assets/Scripts/Assembly-CSharp/ChuPokemonDuLieu.cs",
    "client_pc_ios/Assets/Scripts/Assembly-CSharp/ChuPokemon.cs",
}
BO_QUA_THU_MUC = ("server/src/server/Panel/",)
# Bang tu dien / dia danh o moi ban (server, J2ME, Android, PC) - chua tu cu co chu dich.
BO_QUA_TEN = {"ChuPokemon.java", "TenMapPokemon.java", "ChuPokemon.cs", "ChuPokemonDuLieu.cs", "TenMapPokemon.cs"}

# Dong log (khong toi nguoi choi).
DONG_LOG = re.compile(r"System\.(out|err)|printStackTrace|saveOrtherLog|saveLogThongKe|Debug\.Log|Logdata|LOGGER|logger\.")

# Nhom "gio choi dau tien": dang nhap, tao nhan vat, huong dan, NPC, nhiem vu tan thu, doi.
GIO_DAU = {
    "server/src/data/Text.java", "server/src/real/CharManager.java", "server/src/real/cmd/LoginHandler.java",
    "server/src/real/cmd/SelectCharHandler.java", "server/src/real/cmd/RequestNPCInfoHandler.java",
    "server/src/real/MapMenuNpc.java", "server/src/real/MessageCreator.java", "server/src/real/MapDoi.java",
    "server/src/real/CharVongLap.java", "server/src/real/plugins/addNPC.java", "server/src/real/NpcReceiveCard.java",
    "client_pc_ios/Assets/Scripts/Assembly-CSharp/CreateCharScr.cs", "client_pc_ios/Assets/Scripts/Assembly-CSharp/CharSelectScr.cs",
    "client_pc_ios/Assets/Scripts/Assembly-CSharp/GameScr.cs", "client_pc_ios/Assets/Scripts/Assembly-CSharp/MenuWindows.cs",
    "client_pc_ios/Assets/Scripts/Assembly-CSharp/PauseMenu.cs", "client_pc_ios/Assets/Scripts/Assembly-CSharp/Res.cs",
    "sql:data_potion", "sql:data_shop",
}
QUEST_GIO_DAU = range(1, 14)

# File dang thuoc viec khac (G7 song song) - chi bao, khong sua.
CHU_SO_HUU = {
    "server/src/real/MapDoMenu.java": "G7 khac (menu NPC)",
    "server/src/real/CharChiSo.java": "G7 khac", "server/src/real/LiveActor.java": "G7 khac",
    "server/src/real/Monster.java": "G7 khac", "server/src/real/TienHoaPokemon.java": "G7 khac",
    "client_pc_ios/Assets/Scripts/Assembly-CSharp/Tilemap.cs": "G7 khac", "client_pc_ios/Assets/Scripts/Assembly-CSharp/NPC.cs": "G7 khac",
    "client_pc_ios/Assets/Scripts/Assembly-CSharp/Npc_Server.cs": "G7 khac", "client_pc_ios/Assets/Scripts/Assembly-CSharp/Monster.cs": "G7 khac",
    "client_pc_ios/Assets/Scripts/Assembly-CSharp/Pet.cs": "G7 khac", "client_pc_ios/Assets/Scripts/Assembly-CSharp/AnimalMove.cs": "G7 khac",
    "client_pc_ios/Assets/Scripts/Assembly-CSharp/MessageHandler.cs": "G7 khac (lenh -9x)",
}

_LIT = re.compile(r'"((?:[^"\\\n]|\\.)*)"')


def bo_chu_thich(s):
    """Xoa // va /* */ (giu so dong), khong dung vao chuoi "..." va ky tu '.'."""
    ra = []
    i, n = 0, len(s)
    while i < n:
        c = s[i]
        if c == '"':
            j = i + 1
            while j < n and s[j] != '"' and s[j] != "\n":
                j += 2 if s[j] == "\\" else 1
            ra.append(s[i:j + 1])
            i = j + 1
            continue
        if c == "'":
            j = i + 1
            while j < n and s[j] != "'" and s[j] != "\n":
                j += 2 if s[j] == "\\" else 1
            ra.append(s[i:j + 1])
            i = j + 1
            continue
        if s.startswith("//", i):
            j = s.find("\n", i)
            i = n if j < 0 else j
            continue
        if s.startswith("/*", i):
            j = s.find("*/", i + 2)
            j = n if j < 0 else j + 2
            ra.append("\n" * s.count("\n", i, j))
            i = j
            continue
        ra.append(c)
        i += 1
    return "".join(ra)


class CamTho:
    """Tu dien cam + ngoai le "bang" trong chu khong dau (docs/thiet-ke-game.md)."""

    def __init__(self, cam):
        self.cam = cam

    def tim(self, t):
        g = self.cam.tim(t)
        if g and re.search(r"[^\x00-\x7f]", t) is None and t.strip() not in ("Bang", "BANG"):
            g = [x for x in g if x.lower() != "bang"]
        return g


def giong_chu(t):
    return bool(t) and (re.search(r"[^\x00-\x7f]", t) is not None or t[0].isupper())


def chuoi_trong_file(duong):
    """(so dong, chuoi da bo thoat) cua moi chuoi giong chu, bo dong log."""
    s = bo_chu_thich(open(duong, encoding="utf8", errors="replace").read())
    for so, dong in enumerate(s.split("\n"), 1):
        if DONG_LOG.search(dong):
            continue
        for m in _LIT.finditer(dong):
            t = cp.bo_thoat_java(m.group(1))
            if giong_chu(t):
                yield so, t


def quet_ma(goc_quet, duoi, nguon, td, cam, ra, dem):
    for d, _, fs in os.walk(goc_quet):
        for f in sorted(fs):
            if not f.endswith(duoi):
                continue
            p = os.path.join(d, f)
            if nguon == "cs":
                rel = NHAN_CS + os.path.relpath(p, CS).replace(os.sep, "/")
            elif nguon in ("j2me", "android"):
                rel = os.path.relpath(p, CLIENT).replace(os.sep, "/")
            else:
                rel = os.path.relpath(p, GOC).replace(os.sep, "/")
            if rel in BO_QUA or rel.startswith(BO_QUA_THU_MUC) or f in BO_QUA_TEN:
                continue
            for so, t in chuoi_trong_file(p):
                tho = cam.tim(t)
                if not tho:
                    continue
                sau = td.doi(t)
                con = cam.tim(sau)
                dem[nguon][0] += len(tho)
                dem[nguon][1] += len(con)
                if con:
                    ra.append([nguon, rel, so, "", "|".join(con), t.replace("\n", "\\n"), sau.replace("\n", "\\n"),
                               1 if rel in GIO_DAU else 0, CHU_SO_HUU.get(rel, "")])


_UPDATE_TN = re.compile(r"^UPDATE `(\w+)` SET `(\w+)`='((?:[^'\\]|\\.)*)' WHERE `id`='(-?\d+)' AND ", re.M)


def quet_sql(cam, ra, dem):
    db, _ = cp.nap_csdl(cp.SQL_POKEMON + [cp.SQL_CHU])
    # du_lieu/sql_nguon/pokemon_thuat_ngu.sql (chay cuoi, dang "WHERE `id`='N' AND cot=<chu cu>") - tools/pokemon/sinh_sql_thuat_ngu.py
    p_tn = os.path.join(GOC, "du_lieu", "sql_nguon", "pokemon_thuat_ngu.sql")
    if os.path.exists(p_tn):
        for m in _UPDATE_TN.finditer(open(p_tn, encoding="utf8").read()):
            bang, cot, gt, id_ = m.group(1), m.group(2), cp.bo_thoat_sql(m.group(3)), int(m.group(4))
            if bang in db and id_ in db[bang]:
                db[bang][id_][cot] = gt
    for bang, cot in cp.COT_CHU.items():
        for id_, r in sorted(db[bang].items()):
            for c in cot:
                t = r.get(c)
                if not t or t in ("32000", "NULL"):
                    continue
                con = cam.tim(t)
                if not con:
                    continue
                dem["sql"][0] += len(con)
                dem["sql"][1] += len(con)
                gio_dau = ("sql:" + bang) in GIO_DAU or (bang == "data_quest" and id_ in QUEST_GIO_DAU)
                ra.append(["sql", bang, id_, c, "|".join(con), t.replace("\n", "\\n"), "",
                           1 if gio_dau else 0, ""])


def main():
    b = cp.doc_bang()
    td = cp.tu_dien_doi(b)
    cam = CamTho(cp.tu_dien_cam(b))
    ra = []
    dem = {"java": [0, 0], "cs": [0, 0], "j2me": [0, 0], "android": [0, 0], "sql": [0, 0]}
    quet_ma(SRC, ".java", "java", td, cam, ra, dem)
    quet_ma(CS, ".cs", "cs", td, cam, ra, dem)
    for ten, g in JAVA_CLIENT:
        if os.path.isdir(g):
            quet_ma(g, ".java", ten, td, cam, ra, dem)
    quet_sql(cam, ra, dem)
    # Cot chu_so_huu cho CSDL: o con tu cam ma da bi file SQL khac (I1...) sua -> cua viec do.
    _, da_sua = cp.nap_csdl(cp.SQL_POKEMON)
    for r in ra:
        if r[0] == "sql" and (r[1], r[2], r[3]) in da_sua:
            r[8] = "file du_lieu/sql_nguon/pokemon_* khac (I1/T1/quai)"
    with open(RA, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["nguon", "tep_hoac_bang", "dong_hoac_id", "cot", "tu_cam", "chuoi", "sau_doi",
                    "gio_dau", "chu_so_huu"])
        w.writerows(ra)
    gio_dau = sum(1 for r in ra if r[7] == 1)
    for k, (tho, sau) in dem.items():
        print("%-4s tu cam: tho %4d -> sau doi %4d" % (k, tho, sau))
    print("Con lai %d o (%d o nhom gio dau) -> %s" % (len(ra), gio_dau, os.path.relpath(RA, GOC)))
    if "--kiem" in sys.argv and gio_dau:
        return 1
    tho = sum(v[0] for k, v in dem.items() if k != "sql")
    print("Tu cam THO (truoc doi) trong chuoi ma nguon: %d (phai 0, docs/thiet-ke-game.md)" % tho)
    if "--tho" in sys.argv and (tho or dem["sql"][0]):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
