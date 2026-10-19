#!/usr/bin/env python3
"""Sinh du_lieu/sql_nguon/pokemon_he_sinh_thai.sql - du lieu the gioi cua he sinh thai (docs/thiet-ke-game.md).

Chi thu vien chuan, TAT DINH (cung dau vao -> cung tep). Chay o thu muc HTBB/:
    python3 -I tools/pokemon/sinh_sql_he_sinh_thai.py

Dau vao:
  du_lieu/thiet_ke_map.csv   : loai (vung sinh thai) cua moi ban do
  du_lieu/bang_quai.csv      : template quai (cap, phan loai, dex, he)
  map/map<N>, map/map<N> : luoi sinh quai (u8 w, u8 h, w*h u8 id mau)

Ra (bang PORTABLE, chay duoc tren MariaDB va NovaDB - giong du_lieu/sql_nguon/pokemon_lau_dai.sql):
  data_hst_vung      (map_id, vung, ten)              : vung sinh thai cua ban do
  data_hst_thay_quai (id_goc, id_thay, trong_so, hiem, thoi_tiet, ngay_dem, mat_na_thang)
                     : ung vien thay loai khi quai hoi sinh (cung dai cap +-4, quai hiem +3..+8)
  data_hst_bung_phat (map_id, id_quai)                : cap du dieu kien bung phat (loai cua chinh tuyen)
  data_hst_ho_cau    (map_id, ho_id, so_o, x, y)      : DIEM CAU CA - chi cau o ho (vung nuoc kin khong cham mep
                     ban do, 6..40% dien tich; giong BanDoSinhThai.nhanHo/diemCau). Kem du_lieu/diem_cau_ca.csv.
  data_hst_vung_loai (vung, dex)                      : loai quai cua moi vung (muc tieu "hoan thanh vung";
                     loai ca them luc chay tu KyNangSong.BANG_CAU_CA - mot nguon)
Bang tob_hst_* (du lieu nguoi choi) tao o cuoi tep va server cung tu tao neu thieu.
"""
import csv
import os
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RA = os.path.join(GOC, "du_lieu", "sql_nguon", "pokemon_he_sinh_thai.sql")

MA_VUNG = ["thi_tran", "duong", "dong_co", "rung", "ho", "bien", "nui", "tuyet", "sa_mac", "hang",
           "trong_nha", "thanh", "dau_truong", "tau"]
# Vung co quai hoang da (duoc thay loai / bung phat).
VUNG_TUYEN = {"duong", "dong_co", "rung", "ho", "bien", "nui", "tuyet", "sa_mac", "hang"}

# He (HePokemon): 0 Thuong 1 Lua 2 Nuoc 3 Dien 4 Co 5 Bang 6 GiacDau 7 Doc 8 Dat 9 Bay 10 SieuLinh
# 11 Bo 12 Da 13 Ma 14 Rong 15 BongToi 16 Thep 17 Tien. Thoi tiet: 1 nang 2 mua 3 tuyet 4 bao cat 5 suong.
HOP_THOI_TIET = {1: {1, 4}, 2: {2, 3}, 3: {5}, 4: {12, 8, 16}, 5: {13, 10}}
HE_DEM = {13, 15, 7}

# Map 40 (MOBA) va 42 (nui kho bau): vi tri quai gan logic thap/lane (docs/kien-truc.md) - khong dong vao.
MAP_KHONG_DONG = {40, 42}
SO_UNG_VIEN = 6
SO_HIEM = 2
KHOANG_CAP = 4


def bam(x):
    """SplitMix64 giong SinhThaiTheGioi.bam (de mat na thang tat dinh)."""
    m = (1 << 64) - 1
    z = (x + 0x9E3779B97F4A7C15) & m
    z = ((z ^ (z >> 30)) * 0xBF58476D1CE4E5B9) & m
    z = ((z ^ (z >> 27)) * 0x94D049BB133111EB) & m
    return z ^ (z >> 31)


def doc_map():
    ra = []
    with open(os.path.join(GOC, "du_lieu/thiet_ke_map.csv"), encoding="utf-8") as f:
        for dong in f:
            dong = dong.strip()
            if not dong or dong.startswith("#") or dong.startswith("id;"):
                continue
            p = dong.split(";")
            if len(p) < 3 or not p[0].isdigit():
                continue
            ra.append((int(p[0]), p[1].strip(), p[2].strip()))
    return ra


def doc_quai():
    q = {}
    with open(os.path.join(GOC, "du_lieu/bang_quai.csv"), encoding="utf-8") as f:
        for r in csv.DictReader(f):
            if r["phan_loai"] != "quai" or not r["dex"]:
                continue
            q[int(r["id_quai"])] = {
                "cap": int(r["cap"]), "dex": int(r["dex"]), "ten": r["ten_pokemon"],
                "he1": int(r["he1"] or -1), "he2": int(r["he2"] or -1),
            }
    return q


def doc_luoi(map_id):
    for duong in ("server/map/map%d" % map_id,):
        p = os.path.join(GOC, duong)
        if os.path.isfile(p):
            b = open(p, "rb").read()
            if len(b) < 2:
                return {}
            w, h = b[0], b[1]
            dem = {}
            for v in b[2:2 + w * h]:
                if v == 0 or v == 255:
                    continue
                if map_id == 17 and v > 81:
                    v += 3
                dem[v] = dem.get(v, 0) + 1
            return dem
    return {}


HO_TOI_THIEU = 6
# Ban do co vien 1-2 o (vach/cay) quanh mep: nuoc toi sat vien = bien / song chay ra ngoai, khong phai ho.
HO_CACH_MEP = 2
HO_TOI_DA_PHAN_TRAM = 40
HO_LON = 60
DIEM_MOI_HO = 4


def tile_nuoc():
    """bo tile -> tap chi so tile NUOC THAT (ten W*/sen, kieu 2) - giong BanDoSinhThai.tileNuoc."""
    d = {}
    with open(os.path.join(GOC, "du_lieu/bang_tile_moi.csv"), encoding="utf-8") as f:
        for r in csv.DictReader(f):
            if (r["ten"].startswith("W") or r["ten"] == "sen") and r["kieu"] == "2":
                d.setdefault(r["bo_tile"], set()).add(int(r["chi_so"]))
    return d


def bo_tile(map_id):
    return "t" if map_id < 110 else ("t_thanh" if map_id > 200 else "t_hang")


def ho_cua_map(map_id, nuoc_bo):
    """[(ho_id, so_o, [(x,y) diem cau]...)] cua map; [] neu thieu tep."""
    p = os.path.join(GOC, "server", "cMap", str(map_id))
    if not os.path.isfile(p):
        return []
    b = open(p, "rb").read()
    w, h = b[0], b[1]
    tile = b[2:2 + w * h]
    bo = bo_tile(map_id)
    kieu = open(os.path.join(GOC, "server", "cMap", bo + ".type"), "rb").read()
    tn = nuoc_bo.get(bo, set())
    nuoc = [t < 254 and t in tn for t in tile]
    di = [True if t >= 254 else ((kieu[t] & 2) == 0 if t < len(kieu) else not nuoc[i]) for i, t in enumerate(tile)]
    nhan = [-1] * (w * h)
    tham = [False] * (w * h)
    so_ho = 0
    for bd in range(w * h):
        if not nuoc[bd] or tham[bd]:
            continue
        hang = [bd]
        tham[bd] = True
        k = 0
        mep = False
        while k < len(hang):
            i = hang[k]
            k += 1
            x, y = i % w, i // w
            if x <= HO_CACH_MEP or y <= HO_CACH_MEP or x >= w - 1 - HO_CACH_MEP or y >= h - 1 - HO_CACH_MEP:
                mep = True
            for j, ok in ((i - 1, x > 0), (i + 1, x < w - 1), (i - w, y > 0), (i + w, y < h - 1)):
                if ok and nuoc[j] and not tham[j]:
                    tham[j] = True
                    hang.append(j)
        la_ho = not mep and HO_TOI_THIEU <= len(hang) <= w * h * HO_TOI_DA_PHAN_TRAM // 100
        for i in hang:
            nhan[i] = so_ho if la_ho else -1
        if la_ho:
            so_ho += 1
    tat_ca = []
    for y in range(1, h - 1):
        for x in range(1, w - 1):
            i = y * w + x
            if not di[i] or nhan[i] >= 0:
                continue
            for j in (i - 1, i + 1, i - w, i + w):
                if nhan[j] >= 0:
                    tat_ca.append((nhan[j], x, y))
                    break
    kt = [0] * so_ho
    for v in nhan:
        if v >= 0:
            kt[v] += 1
    ra = []
    for ho in range(so_ho):
        cua = [d for d in tat_ca if d[0] == ho]
        n = min(DIEM_MOI_HO, len(cua))
        ra.append((ho, kt[ho], [cua[k * len(cua) // n][1:] for k in range(n)]))
    return ra


def thoi_tiet_ua(he1, he2):
    for tt in (2, 1, 3, 4, 5):
        if he1 in HOP_THOI_TIET[tt] or he2 in HOP_THOI_TIET[tt]:
            return tt
    return -1


def main():
    maps = doc_map()
    quai = doc_quai()
    luoi = {m: doc_luoi(m) for m, _, _ in maps}
    # So ban do moi loai xuat hien (do hiem trong the gioi).
    so_map = {}
    for m, loai, _ in maps:
        for t in luoi[m]:
            so_map[t] = so_map.get(t, 0) + 1
    goc = sorted({t for m, loai, _ in maps if loai in VUNG_TUYEN and m not in MAP_KHONG_DONG
                  for t in luoi[m] if t in quai})

    thay = []
    for g in goc:
        cg = quai[g]["cap"]
        gan = sorted((abs(quai[u]["cap"] - cg), u) for u in quai
                     if u != g and abs(quai[u]["cap"] - cg) <= KHOANG_CAP and quai[u]["dex"] != quai[g]["dex"])
        da_chon = set()
        for d, u in gan[:SO_UNG_VIEN]:
            da_chon.add(u)
            mat_na = 0xFFF
            if bam(u) % 3 == 0:  # loai di cu: co mat 6 thang lien tiep, bat dau theo hat giong
                bd = bam(u * 7 + 1) % 12
                mat_na = sum(1 << ((bd + k) % 12) for k in range(6))
            thay.append((g, u, max(5, 30 - 5 * d), 0, -1, -1, mat_na))
        # Khoa chinh (id_goc, id_thay): loai da la ung vien thuong thi khong lap lai lam ung vien hiem.
        hiem = sorted((so_map.get(u, 0), quai[u]["cap"], u) for u in quai
                      if u != g and u not in da_chon and 3 <= quai[u]["cap"] - cg <= 8
                      and quai[u]["dex"] != quai[g]["dex"])
        for _, _, u in hiem[:SO_HIEM]:
            tt = thoi_tiet_ua(quai[u]["he1"], quai[u]["he2"])
            nd = 1 if (quai[u]["he1"] in HE_DEM or quai[u]["he2"] in HE_DEM) else -1
            thay.append((g, u, 2, 1, tt, nd, 0xFFF))

    khoa = [(r[0], r[1]) for r in thay]
    assert len(khoa) == len(set(khoa)), "trung khoa (id_goc, id_thay)"

    bung = []
    for m, loai, _ in maps:
        if loai not in VUNG_TUYEN or m in MAP_KHONG_DONG:
            continue
        for t, n in sorted(luoi[m].items()):
            if t in quai and n >= 3:
                bung.append((m, t))

    nuoc_bo = tile_nuoc()
    ho_cau = []
    for m, loai, ten in maps:
        if m in MAP_KHONG_DONG:
            continue
        for ho, so_o, diem in ho_cua_map(m, nuoc_bo):
            for (x, y) in diem:
                ho_cau.append((m, ho, so_o, x, y))
    with open(os.path.join(GOC, "du_lieu/diem_cau_ca.csv"), "w", encoding="utf-8", newline="\n") as f:
        f.write("# SINH RA boi tools/pokemon/sinh_sql_he_sinh_thai.py - KHONG SUA TAY. Chi cau ca o ho (docs/thiet-ke-game.md).\n")
        f.write("# loai_ho: ho_lon (>= %d o, co ca hiem) | ao (chi ca thuong). x,y = o dung cau (don vi o 16 px).\n" % HO_LON)
        f.write("map_id,ten_map,vung,ho_id,so_o,loai_ho,x,y\n")
        ten_map = {m: (loai, ten) for m, loai, ten in maps}
        for (m, ho, so_o, x, y) in ho_cau:
            f.write("%d,%s,%s,%d,%d,%s,%d,%d\n" % (m, ten_map[m][1].replace(",", " "), ten_map[m][0], ho, so_o,
                                                 "ho_lon" if so_o >= HO_LON else "ao", x, y))

    vung_loai = set()
    for m, loai, _ in maps:
        if loai not in VUNG_TUYEN or m in MAP_KHONG_DONG:
            continue
        for t in luoi[m]:
            if t in quai:
                vung_loai.add((MA_VUNG.index(loai), quai[t]["dex"]))

    out = []
    w = out.append
    w("-- SINH RA boi tools/pokemon/sinh_sql_he_sinh_thai.py - KHONG SUA TAY.")
    w("-- He sinh thai game (docs/thiet-ke-game.md). Chi doc khi sv.he_sinh_thai=1.")
    w("-- Bang data_hst_* : du lieu the gioi (vung, thay loai, bung phat). Bang tob_hst_* : du lieu nguoi choi")
    w("-- (server cung tu tao neu thieu - com.htbb.pokemon.DuLieuHeSinhThai.taoBang).")
    w("SET NAMES utf8;")
    w("")
    w("DROP TABLE IF EXISTS `data_hst_vung`;")
    w("CREATE TABLE `data_hst_vung` (\n  `map_id` int NOT NULL,\n  `vung` int NOT NULL,\n"
      "  `ten` varchar(64) NOT NULL,\n  PRIMARY KEY (`map_id`)\n) DEFAULT CHARSET=utf8;")
    w("INSERT INTO `data_hst_vung` VALUES")
    dong = []
    for m, loai, ten in maps:
        v = MA_VUNG.index(loai) if loai in MA_VUNG else 1
        dong.append("(%d,%d,'%s')" % (m, v, ten.replace("'", "")))
    w(",\n".join(dong) + ";")
    w("")
    w("DROP TABLE IF EXISTS `data_hst_thay_quai`;")
    w("CREATE TABLE `data_hst_thay_quai` (\n  `id_goc` int NOT NULL,\n  `id_thay` int NOT NULL,\n"
      "  `trong_so` int NOT NULL,\n  `hiem` int NOT NULL,\n  `thoi_tiet` int NOT NULL,\n"
      "  `ngay_dem` int NOT NULL,\n  `mat_na_thang` int NOT NULL,\n  PRIMARY KEY (`id_goc`,`id_thay`)\n"
      ") DEFAULT CHARSET=utf8;")
    w("INSERT INTO `data_hst_thay_quai` VALUES")
    w(",\n".join("(%d,%d,%d,%d,%d,%d,%d)" % r for r in thay) + ";")
    w("")
    w("DROP TABLE IF EXISTS `data_hst_bung_phat`;")
    w("CREATE TABLE `data_hst_bung_phat` (\n  `map_id` int NOT NULL,\n  `id_quai` int NOT NULL,\n"
      "  PRIMARY KEY (`map_id`,`id_quai`)\n) DEFAULT CHARSET=utf8;")
    w("INSERT INTO `data_hst_bung_phat` VALUES")
    w(",\n".join("(%d,%d)" % r for r in bung) + ";")
    w("")
    w("DROP TABLE IF EXISTS `data_hst_vung_loai`;")
    w("CREATE TABLE `data_hst_vung_loai` (\n  `vung` int NOT NULL,\n  `dex` int NOT NULL,\n"
      "  PRIMARY KEY (`vung`,`dex`)\n) DEFAULT CHARSET=utf8;")
    w("INSERT INTO `data_hst_vung_loai` VALUES")
    w(",\n".join("(%d,%d)" % r for r in sorted(vung_loai)) + ";")
    w("")
    w("DROP TABLE IF EXISTS `data_hst_ho_cau`;")
    w("CREATE TABLE `data_hst_ho_cau` (\n  `map_id` int NOT NULL,\n  `ho_id` int NOT NULL,\n  `so_o` int NOT NULL,\n"
      "  `x` int NOT NULL,\n  `y` int NOT NULL,\n  PRIMARY KEY (`map_id`,`ho_id`,`x`,`y`)\n) DEFAULT CHARSET=utf8;")
    if ho_cau:
        w("INSERT INTO `data_hst_ho_cau` VALUES")
        w(",\n".join("(%d,%d,%d,%d,%d)" % r for r in ho_cau) + ";")
    w("")
    w("-- Du lieu nguoi choi (KHONG xoa khi chay lai).")
    w("CREATE TABLE IF NOT EXISTS `tob_hst_nhan_vat` (\n  `id_char` int NOT NULL,\n  `du_lieu` text NOT NULL,\n"
      "  `cap_nhat` bigint NOT NULL,\n  PRIMARY KEY (`id_char`)\n) DEFAULT CHARSET=utf8;")
    w("CREATE TABLE IF NOT EXISTS `tob_hst_cho` (\n  `id` int NOT NULL AUTO_INCREMENT,\n  `id_ban` int NOT NULL,\n"
      "  `ten_ban` varchar(64) NOT NULL,\n  `khoa_vat` int NOT NULL,\n  `so_luong` int NOT NULL,\n"
      "  `gia_moi` bigint NOT NULL,\n  `du_lieu` varchar(64) NOT NULL,\n  `het_han` bigint NOT NULL,\n"
      "  `trang_thai` int NOT NULL,\n  PRIMARY KEY (`id`)\n) DEFAULT CHARSET=utf8;")
    w("CREATE TABLE IF NOT EXISTS `tob_hst_don` (\n  `id` int NOT NULL AUTO_INCREMENT,\n  `id_mua` int NOT NULL,\n"
      "  `ten_mua` varchar(64) NOT NULL,\n  `ma_vat` int NOT NULL,\n  `so_can` int NOT NULL,\n"
      "  `so_da_giao` int NOT NULL,\n  `gia_moi` bigint NOT NULL,\n  `het_han` bigint NOT NULL,\n"
      "  `trang_thai` int NOT NULL,\n  PRIMARY KEY (`id`)\n) DEFAULT CHARSET=utf8;")
    w("CREATE TABLE IF NOT EXISTS `tob_hst_hop_thu` (\n  `id` int NOT NULL AUTO_INCREMENT,\n  `id_char` int NOT NULL,\n"
      "  `xu` bigint NOT NULL,\n  `khoa_vat` int NOT NULL,\n  `so_luong` int NOT NULL,\n"
      "  `du_lieu` varchar(64) NOT NULL,\n  `ghi_chu` varchar(128) NOT NULL,\n  `tao_luc` bigint NOT NULL,\n"
      "  PRIMARY KEY (`id`)\n) DEFAULT CHARSET=utf8;")
    open(RA, "w", encoding="utf-8", newline="\n").write("\n".join(out) + "\n")
    print("Da ghi %s: %d vung, %d dong thay loai (%d goc), %d cap bung phat, %d loai-vung, %d diem cau (%d map co ho)"
          % (os.path.relpath(RA, GOC), len(maps), len(thay), len(goc), len(bung), len(vung_loai), len(ho_cau),
             len({r[0] for r in ho_cau})))
    return 0


if __name__ == "__main__":
    sys.exit(main())
