#!/usr/bin/env python3
"""Sinh than sql/sua_tuyen_nhiem_vu.sql (nhanh sua-tuyen) tu CSDL dang chay + server/cMap + ma nguon bang cong.

Chi DOC CSDL, in SQL ra stdout (idempotent). Hai khoi:

  KHU VUC  vung dich cho moi buoc "Đứng tại điểm đánh dấu..." (dung_toa_do) / "Đến ..." (den_noi) - ca chuoi -64
           (data_quest_moi, server real.KhuVucNhiemVuGame) lan nhiem vu So tay (NhiemVuEngine): bang
           data_nhiem_vu_khu_vuc (ma, stt, map_id, khung o x1..y2, noi_chuyen_sau, bat, ten_noi, ghi_chu).
             den_noi        -> ca ban do (0,0)-(255,255) (vao ban do la toi).
             dung_toa_do    -> o (x,y) cua thiet ke +- ban_kinh_o (mac dinh 3); o khong di duoc / khong toi duoc tu cong
                               -> doi sang o di duoc gan nhat (va sua "(ô x,y)" trong chu).
             lang khoi dau  -> ban do 0 / 70 / 80 (va nha trong lang) chi vao duoc lang cua phe minh: mot dong cho MOI lang
                               (vao lang nao cung duoc), chu "trên Pewter (ô x,y)" -> "trong làng khởi đầu".
             ban do khong toi duoc (khong cong / tau / Pidgeot nao dan vao, theo bang cong cua RealController, Hoenn
             BanDoHoenn, ben Lapras) -> bat = 0 (buoc giu nhu cu: noi chuyen la xong) + ghi_chu ly do.
           O di duoc = giong server/test (KiemHoiSinh.diDuoc): tile khong rong va (type & 2) == 0; "toi duoc" = cung vung
           lien thong 4 huong voi o canh mot cong cua ban do.
  THUONG   data_quest_moi.infogift: noi cot len varchar(1000) (cu 200 -> 14 dong bi cat giua chu) va VIET LAI doan sau
           "Phần thưởng:|" tu CHINH dong thuong (luat com.htbb.pokemon.ChuThuongNhiemVu: exp, xu, luong, luong khoa, vat
           pham theo dong, thuoc, thuong_khac chuoi chinh item/thuoc/danh_hieu); moi lenh co dieu kien cot thuong =
           gia tri luc sinh (CSDL khac thi khong ghi chu sai - server van sinh chu dung luc gui).

Chay o goc repo:  MYSQL="mysql -h127.0.0.1 -P3401 -uroot" python3 -I tools/pokemon/sinh_sua_tuyen.py > /tmp/than.sql
(CSDL = sql/htbb_day_du.sql + them_danh_hieu.sql + them_huong_dan_tan_thu.sql, CHUA chay sua_tuyen_nhiem_vu.sql)
roi dan vao giua hai dong danh dau SINH TU DONG cua sql/sua_tuyen_nhiem_vu.sql. Kiem: server/test/KiemSuaTuyen.
"""
import os
import re
import shlex
import subprocess
import sys
from collections import deque

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SV = os.path.join(GOC, "server")
LANG = (0, 70, 80)
# nha trong lang theo thu tu cong cua lang 0 / 70 / 80 (cung vi tri, cung cong dung)
NHA_LANG = [(29, 31, 30), (102, 72, 82), (103, 73, 83), (104, 74, 84), (108, 78, 88), (109, 79, 89)]
CA_BAN_DO = (0, 0, 255, 255)
TEN_LANG = "làng khởi đầu"
# cong co nhung server chan: 111 Hang Darkrai chi mo trong su kien He (MapXuLyLenh: toMapID == 111 && !isSuKienHe2017),
# 206 chi thanh vien doi
CHAN = {111, 206}


def sql(q):
    lenh = shlex.split(os.environ.get("MYSQL", "mysql")) + ["--default-character-set=utf8", "-N", "-B", "htbb_db", "-e", q]
    out = subprocess.run(lenh, check=True, capture_output=True).stdout.decode("utf-8")
    return [l.split("\t") for l in out.splitlines() if l != ""]


def b64(s):
    """TO_BASE64 cua MariaDB (xuong dong moi 76 ky tu, -B in thanh \\n) -> chuoi."""
    import base64
    return base64.b64decode(s.replace("\\n", "").replace("\n", "")).decode("utf-8")


def q(s):
    return "'" + str(s).replace("\\", "\\\\").replace("'", "\\'") + "'"


# ---------------------------------------------------------------- ban do

class BanDo:
    """cMap (com.htbb.pokemon.DinhDangBanDo): w h tile[w*h] | khoi top | top le | cay | offline | cong | npc."""

    def __init__(self, id_):
        b = open(os.path.join(SV, "cMap", str(id_)), "rb").read()
        self.id, self.w, self.h = id_, b[0], b[1]
        self.tile = list(b[2:2 + self.w * self.h])
        p = 2 + self.w * self.h
        while b[p] != 255:
            p += 4 + b[p + 2] * b[p + 3]
        p += 1
        while b[p] != 255:
            p += 3
        p += 1
        while b[p] not in (255, 254):
            p += 3
        p += 1
        self.offline = b[p] == 1
        p += 1
        n = b[p]
        p += 1
        self.cong = []
        for _ in range(n):
            self.cong.append((b[p], b[p + 1], b[p + 2] | (b[p + 3] << 8), b[p + 4], b[p + 5]))
            p += 6
        bo = "t" if id_ < 110 else ("t_thanh" if id_ > 200 else "t_hang")
        k = open(os.path.join(SV, "cMap", bo + ".type"), "rb").read()
        self.di = [t not in (254, 255) and t < len(k) and (k[t] & 2) == 0 for t in self.tile]
        self._vao = None

    def diDuoc(self, x, y):
        return 0 <= x < self.w and 0 <= y < self.h and self.di[y * self.w + x]

    def vao(self):
        """Tap o di duoc lien thong (4 huong) voi o canh (<= 2) mot cong; khong co cong -> vung lon nhat."""
        if self._vao is not None:
            return self._vao
        hat = set()
        for (x, y, _t, _a, _b) in self.cong:
            for dx in range(-2, 3):
                for dy in range(-2, 3):
                    if self.diDuoc(x + dx, y + dy):
                        hat.add((x + dx, y + dy))
        if not hat:
            tot = set()
            tham = set()
            for y in range(self.h):
                for x in range(self.w):
                    if self.diDuoc(x, y) and (x, y) not in tham:
                        v = self.lan({(x, y)})
                        tham |= v
                        if len(v) > len(tot):
                            tot = v
            self._vao = tot
        else:
            self._vao = self.lan(hat)
        return self._vao

    def lan(self, hat):
        seen = set(hat)
        dq = deque(hat)
        while dq:
            x, y = dq.popleft()
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                n = (x + dx, y + dy)
                if n not in seen and self.diDuoc(*n):
                    seen.add(n)
                    dq.append(n)
        return seen

    def ganNhat(self, x, y):
        """O 'vao' gan (x, y) nhat (Chebyshev, roi Manhattan, roi y, x)."""
        v = self.vao()
        if (x, y) in v:
            return (x, y)
        return min(v, key=lambda o: (max(abs(o[0] - x), abs(o[1] - y)), abs(o[0] - x) + abs(o[1] - y), o[1], o[0]))


_BD = {}


def bd(i):
    if i not in _BD:
        try:
            _BD[i] = BanDo(i)
        except (OSError, IndexError):
            _BD[i] = None
    return _BD[i]


# ---------------------------------------------------------------- the gioi: ban do toi duoc

def khoi(s, dau):
    i = s.index(dau) + len(dau)
    while s[i] != "{":
        i += 1
    d = 0
    for k in range(i, len(s)):
        if s[k] == "{":
            d += 1
        elif s[k] == "}":
            d -= 1
            if d == 0:
                return s[i:k + 1]
    raise ValueError(dau)


def nhom(b):
    out, d, st = [], 0, None
    inner = b[1:-1]
    for k, c in enumerate(inner):
        if c == "{":
            if d == 0:
                st = k
            d += 1
        elif c == "}":
            d -= 1
            if d == 0:
                out.append(inner[st:k + 1])
    return out


def so_mang(t):
    return [[int(x) for x in a.split(",") if x.strip()] for a in re.findall(r"\{([-0-9, ]+)\}", t)]


def toi_duoc():
    """Ban do (nap) nguoi choi Kanto / Johto toi duoc: lang + nha cua lang, cong (bang RealController), Pidgeot, Lapras,
    Hoenn (BanDoHoenn tu Cinnabar 9). Lang cua phe khac KHONG (server ep cong ve lang theo phe)."""
    rc = open(os.path.join(SV, "src", "real", "RealController.java"), encoding="utf-8").read()
    loc = [so_mang(g) for g in nhom(khoi(rc, "Map.locationMap = new short[][][]"))]
    mid = so_mang(khoi(rc, "Map.mapID = new short[][]"))
    grp = {}
    for i, m in enumerate(mid):
        for x in m:
            grp.setdefault(x, i)
    co = {int(f) for f in os.listdir(os.path.join(SV, "cMap")) if f.isdigit()}

    def ke(m):
        b = bd(m)
        out = set()
        if b is None:
            return out
        g = grp.get(m)
        for (_x, _y, t, _a, _b) in b.cong:
            if g is not None and g < len(loc) and t < len(loc[g]) and loc[g][t]:
                out.add(loc[g][t][0])
            elif t in co and bd(t) is not None and bd(t).offline:
                out.add(t)
        return out

    hoenn_src = open(os.path.join(SV, "src", "com", "htbb", "pokemon", "BanDoHoenn.java"), encoding="utf-8").read()
    hoenn = {}
    for m in re.finditer(r"them\((\d+),[^;]*;", hoenn_src, re.S):
        hoenn[int(m.group(1))] = [int(x) for x in re.findall(r"\.c\('[NSEW]', \d+, (\d+)\)", m.group(0))]
    lapras = set()
    for r in sql("SELECT ban_do FROM data_npc WHERE type = 10"):
        lapras |= {int(x) for x in r[0].split(",") if x.strip().isdigit()}
    pidgeot = set()
    xp = re.search(r"xaphu = new XaphuTemplate\[\]\{(.*?)\};", rc, re.S)
    for a in re.findall(r"new XaphuTemplate\(new short\[\]\{([0-9, ]+)\}", xp.group(1)):
        pidgeot |= {int(x) for x in a.split(",")}
    pidgeot.add(24)  # XaphuTemplate(new short[1] ...) -> Olivine 24
    kq = {}
    for lang, nha in ((0, [n[0] for n in NHA_LANG]), (70, [n[1] for n in NHA_LANG])):
        seen = {lang, *nha}
        dq = deque(seen)
        while dq:
            m = dq.popleft()
            nxt = set(ke(m))
            if m in hoenn:
                nxt |= set(hoenn[m])
            if m == 9:
                nxt.add(43)  # cong vao Hoenn (BanDoHoenn.MAP_CONG_VAO)
            if m in lapras:
                nxt |= lapras
            if m in (0, 70, 80):
                nxt |= pidgeot
            for d in nxt:
                if d in LANG or any(d in n for n in NHA_LANG):
                    continue  # lang / nha cua phe khac: cong ep ve lang minh
                if d not in seen and d in co:
                    seen.add(d)
                    dq.append(d)
        kq[lang] = seen - CHAN
    return kq


# ---------------------------------------------------------------- khu vuc

def lop_lang(m):
    """Ban do tuong duong o ca ba lang (lang / nha trong lang), None neu khong phai."""
    if m in LANG:
        return list(LANG)
    for n in NHA_LANG:
        if m in n:
            return list(n)
    return None


def khu_vuc(ma, kieu, map_id, toa, dk, ten_map, toi):
    """-> (danh sach dong {map,x1,y1,x2,y2,ten}, bat, ghi_chu, (x,y) moi cua o thiet ke hoac None, la_lang)."""
    r = 3
    m = re.search(r"ban_kinh_o=(\d+)", dk or "")
    if m:
        r = int(m.group(1))
    ds_map = lop_lang(map_id) or [map_id]
    la_lang = len(ds_map) > 1
    dong, ghi, moi = [], [], None
    for mp in ds_map:
        b = bd(mp)
        if b is None or not b.vao():
            ghi.append("ban do %d khong co / khong co o di duoc" % mp)
            continue
        if not la_lang:
            ten = ten_map.get(mp, str(mp))
        elif map_id in LANG:
            ten = TEN_LANG
        else:  # nha / khu sau cong trong lang: ten cua thiet ke, o lang nao cung duoc
            ten = ten_map.get(map_id, str(map_id)) + " (cạnh " + TEN_LANG + ")"

        if kieu == "den_noi" or toa is None:
            dong.append((mp,) + CA_BAN_DO + (ten,))
            continue
        x, y = toa
        if la_lang and mp != map_id:
            # cung vi tri tuong doi trong lang kia (cMap lang khac co); kep vao ban do
            x = min(max(0, x), b.w - 1)
            y = min(max(0, y), b.h - 1)
        gx, gy = b.ganNhat(x, y)
        if mp == map_id and (gx, gy) != toa:
            moi = (gx, gy)
            ghi.append("o %d,%d khong toi duoc -> %d,%d" % (toa[0], toa[1], gx, gy))
        dong.append((mp, max(0, gx - r), max(0, gy - r), min(b.w - 1, gx + r), min(b.h - 1, gy + r), ten))
    ok = bool(dong) and any(d[0] in toi[0] or d[0] in toi[70] or d[0] in LANG for d in dong)
    if not ok:
        ghi.append("ban do %s khong toi duoc (khong cong / tau / Pidgeot dan vao)" % ",".join(str(x) for x in ds_map))
    return dong, ok, "; ".join(ghi), moi, la_lang


# ---------------------------------------------------------------- thuong (giong com.htbb.pokemon.ChuThuongNhiemVu)

def so(n):
    s = str(abs(int(n)))
    b = []
    for i, c in enumerate(s):
        if i > 0 and (len(s) - i) % 3 == 0:
            b.append(".")
        b.append(c)
    return ("-" if int(n) < 0 else "") + "".join(b)


def so_nguyen(s):
    kq = []
    for a in (s or "").split(","):
        try:
            kq.append(int(a.strip()))
        except ValueError:
            kq.append(32000)
    return kq if (s or "").strip() else []


def ten(t, i, mac):
    v = t.get(i)
    return v.strip() if v and v.strip() else "%s #%d" % (mac, i)


def dong_thuong(exp, xu, luong, lk, itemgift, potion, npotion, tk, ten_vp, ten_th):
    p = []
    if exp > 0:
        p.append(so(exp) + " EXP")
    if xu > 0:
        p.append(so(xu) + " xu")
    if luong > 0:
        p.append(so(luong) + " lượng")
    if lk > 0:
        p.append(so(lk) + " lượng khoá")
    g = so_nguyen(itemgift)
    if g and g[0] < 32000:
        cong = g[5] if len(g) > 5 else 0
        duoi = " +%d" % cong if cong > 0 else ""
        ds = []
        for i in g[:5]:
            n = ten(ten_vp, i, "vật phẩm")
            if n not in ds:
                ds.append(n)
        p.append(ds[0] + duoi if len(ds) == 1 else " / ".join(ds) + duoi + " (theo dòng Pokémon)")
    th, nt = so_nguyen(potion), so_nguyen(npotion)
    for i in range(min(len(th), len(nt))):
        if th[i] < 32000 and nt[i] > 0:
            p.append("%d %s" % (nt[i], ten(ten_th, th[i], "thuốc")))
    for phan in (tk or "").split(";"):
        k = phan.find(":")
        if k <= 0:
            continue
        loai, v = phan[:k].strip(), phan[k + 1:].strip()
        if loai == "danh_hieu" and v:
            p.append("danh hiệu " + v)
        elif loai in ("item", "thuoc"):
            x = v.find("x")
            try:
                i = int((v[:x] if x > 0 else v).strip())
                n = int(v[x + 1:].strip()) if x > 0 else 1
            except ValueError:
                continue
            if n > 0:
                p.append(ten(ten_vp, i, "vật phẩm") + " x%d" % n if loai == "item" else "%d %s" % (n, ten(ten_th, i, "thuốc")))
    return ", ".join(p) if p else "Không có"


# ---------------------------------------------------------------- main

def main():
    ra = []
    ten_map = {int(r[0]): r[1] for r in sql("SELECT map_id, ten FROM data_map_ten")}
    toi = toi_duoc()
    dong = sql("SELECT l.ma, l.kieu_muc_tieu, l.loai, IFNULL(n.map_id,''), IFNULL(n.toa_do,''), IFNULL(n.dieu_kien,''),"
               " IFNULL(n.muc_tieu_text,''), IFNULL(q.id,0), IFNULL(q.mainsub,-1), IFNULL(q.type,-1), IFNULL(q.npcres,32000)"
               " FROM data_quest_lau_dai l LEFT JOIN data_nhiem_vu_noi_dung n ON n.ma = l.ma"
               " LEFT JOIN data_quest_moi q ON q.id = l.id_so"
               " WHERE l.kieu_muc_tieu IN ('dung_toa_do','den_noi') ORDER BY l.id_so")
    ra.append("-- [khu-vuc] %d buoc dia diem (dung_toa_do / den_noi)" % len(dong))
    ra.append("DELETE FROM `data_nhiem_vu_khu_vuc` WHERE `ma` IN (%s);" % ",".join(q(d[0]) for d in dong))
    hang = []
    sua_chu = []
    tat = []
    for ma, kieu, loai, map_id, toa_do, dk, mtt, qid, ms, ty, npcres in dong:
        if not map_id.strip().lstrip("-").isdigit():
            tat.append("%s: khong co map_id" % ma)
            continue
        mp = int(map_id)
        t = re.match(r"\s*(\d+)\s*[;,]\s*(\d+)\s*$", toa_do)
        toa = (int(t.group(1)), int(t.group(2))) if t else None
        vung, ok, ghi, moi, la_lang = khu_vuc(ma, kieu, mp, toa, dk, ten_map, toi)
        if not ok:
            tat.append("%s (%s, ban do %d): %s" % (ma, kieu, mp, ghi))
        for stt, (m2, x1, y1, x2, y2, ten_noi) in enumerate(vung):
            hang.append("(%s,%d,%d,%d,%d,%d,%d,0,%d,%s,%s)" % (q(ma), stt, m2, x1, y1, x2, y2, 1 if ok else 0, q(ten_noi), q(ghi[:250])))
        # sua chu "(ô x,y)" khi doi o; buoc trong lang: "trên <Lang> (ô x,y)" -> "trong làng khởi đầu"
        if toa and kieu == "dung_toa_do":
            cu = "(ô %d,%d)" % toa
            if la_lang:
                tl = ten_map.get(mp, "")
                doi = [(" trên %s %s" % (tl, cu), " trong " + TEN_LANG if mp in LANG else " trên %s, cạnh %s" % (tl, TEN_LANG))]
            elif moi:
                doi = [(cu, "(ô %d,%d)" % moi)]
            else:
                doi = []
            if doi:
                sua_chu.append((ma, int(qid), doi, moi, toa))
    for i in range(0, len(hang), 50):
        ra.append("INSERT INTO `data_nhiem_vu_khu_vuc` (`ma`,`stt`,`map_id`,`x1`,`y1`,`x2`,`y2`,`noi_chuyen_sau`,`bat`,`ten_noi`,"
                  "`ghi_chu`) VALUES\n" + ",\n".join(hang[i:i + 50]) + ";")
    ra.append("-- [khu-vuc] %d dong vung, %d buoc tat (giu noi chuyen la xong):" % (len(hang), len(tat)))
    for t in tat:
        ra.append("--   " + t)
    ra.append("-- [khu-vuc-chu] %d buoc doi chu o / lang" % len(sua_chu))
    for ma, qid, doi, moi, toa in sua_chu:
        def rep(cot):
            e = "`%s`" % cot
            for a, b in doi:
                e = "REPLACE(%s, %s, %s)" % (e, q(a), q(b))
            return "`%s` = %s" % (cot, e)
        if moi:
            ra.append("UPDATE `data_nhiem_vu_noi_dung` SET `toa_do` = %s, %s WHERE `ma` = %s AND `toa_do` = %s;"
                      % (q("%d;%d" % moi), rep("muc_tieu_text"), q(ma), q("%d;%d" % toa)))
        else:
            ra.append("UPDATE `data_nhiem_vu_noi_dung` SET %s WHERE `ma` = %s;" % (rep("muc_tieu_text"), q(ma)))
        ra.append("UPDATE `data_quest_lau_dai` SET %s WHERE `ma` = %s;" % (rep("muc_tieu_ten"), q(ma)))
        if qid:
            ra.append("UPDATE `data_quest_moi` SET %s, %s WHERE `id` = %d;" % (rep("scontent"), rep("infogift"), qid))

    # ---- thuong
    ten_vp = {int(r[0]): b64(r[1]) for r in sql("SELECT id, TO_BASE64(name) FROM data_item")}
    ten_th = {int(r[0]): b64(r[1]) for r in sql("SELECT id, TO_BASE64(name) FROM data_potion")}
    # thuong_khac phat khi tra qua -64 (NhiemVuEngine.thuongKhacCu): chuoi chinh = THUONG_CU (chinh / huong_dan,
    # dong_co_tuong_thich <> 0); phu = mau engine (loai khac chinh / huong_dan)
    tk = {0: {}, 1: {}}
    for r in sql("SELECT l.id_so, l.loai, l.dong_co_tuong_thich, IFNULL(n.thuong_khac,'') FROM data_quest_lau_dai l"
                 " JOIN data_nhiem_vu_noi_dung n ON n.ma = l.ma"):
        chinh = r[1] in ("chinh", "huong_dan")
        if chinh and int(r[2]) != 0:
            tk[0][int(r[0])] = r[3]
        elif not chinh:
            tk[1][int(r[0])] = r[3]
    qs = sql("SELECT id, mainsub, exp, gold, luong, luongLock, itemgift, potiongift, npotion, IFNULL(infogift,'')"
             " FROM data_quest_moi ORDER BY id")
    lenh = []
    for id_, ms, exp, gold, luong, lk, ig, po, npo, info in qs:
        id_, ms = int(id_), int(ms)
        d = dong_thuong(int(exp), int(gold), int(luong or 0), int(lk or 0), ig, po, npo, tk.get(ms, {}).get(id_),
                        ten_vp, ten_th)
        cu = info.split("Phần thưởng:|", 1)[1] if "Phần thưởng:|" in info else None
        if cu == d:
            continue
        lenh.append("UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1),"
                    " 'Phần thưởng:|', %s) WHERE `id` = %d AND `mainsub` = %d AND `exp` = %s AND `gold` = %s AND `luong` = %s"
                    " AND `luongLock` = %s AND `itemgift` = %s AND `potiongift` = %s AND `npotion` = %s AND `infogift` LIKE"
                    " '%%Phần thưởng:|%%';" % (q(d), id_, ms, exp, gold, luong or 0, lk or 0, q(ig), q(po), q(npo)))
    ra.append("-- [thuong] %d / %d dong infogift viet lai tu dong thuong" % (len(lenh), len(qs)))
    ra.extend(lenh)
    print("\n".join(ra))
    print("-- %d vung, %d tat, %d sua chu, %d thuong" % (len(hang), len(tat), len(sua_chu), len(lenh)), file=sys.stderr)


if __name__ == "__main__":
    main()
