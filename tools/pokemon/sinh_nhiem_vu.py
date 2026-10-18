#!/usr/bin/env python3
"""
SINH NHIEM VU (docs/pokemon/NHIEM_VU_THIET_KE.md). Goi tu tools/pokemon/sinh_sql_lau_dai.py
(ham sinh(s)), nen chay MOT lenh la ra het:

  python3 -I tools/pokemon/sinh_nhiem_vu.py      (= chay sinh_sql_lau_dai.py roi ghi them file rieng)

Doc noi dung tu nv_chung.py, nv_cot_truyen.py, nv_phu.py, nv_lap_lai.py (chi la bang chu) va
so lieu tu sinh_sql_lau_dai.py (ngan sach, EXP/xu moi gio) + docs/pokemon/noi_dung_*.csv
(trang bi, alpha, Gym, vat pham moi, su kien) + thiet_ke_map.csv / diem_co_dinh.csv.

Ra (KHONG sua tay):
  docs/pokemon/nhiem_vu_moi.csv          (qua sinh_sql_lau_dai: 30 cot cu + cot moi)
  sql/pokemon_lau_dai.sql                (qua sinh_sql_lau_dai: data_quest_lau_dai + data_quest_moi)
  sql/pokemon_nhiem_vu.sql               data_nhiem_vu_noi_dung (hoi thoai, goi y, nhanh, an...),
                                         data_nhiem_vu_kieu, data_vat_pham_nhiem_vu
  docs/pokemon/nhiem_vu_nhip_do.csv      nhip do theo cap (so lua chon, gio noi dung)
  docs/pokemon/web/nhiem_vu_tom_tat.json tom tat cho website (khong id, khong ngan sach, khong dap an)
  docs/pokemon/NHIEM_VU_THIET_KE.md      cac bang giua <!-- SINH:... --> (phan con lai viet tay)

NGAN SACH: tong thuong moi nhom bang tong cu (721 dong truoc day) - them nhiem vu thi thuong
moi dong nho lai; nhiem vu moi (su kien, huyen thoai, an) thuong bang danh hieu / trang tri /
truyen / mo khoa, luong khoa cua chung tru vao quy thanh tuu. Kiem: test/KiemNhiemVu.java.

Chi thu vien chuan. Tat dinh.
"""
import csv
import io
import json
import os
import re
import sys

DIR = os.path.dirname(os.path.abspath(__file__))
GOC = os.path.dirname(os.path.dirname(DIR))
sys.path.insert(0, DIR)

import nv_chung as NC  # noqa: E402
import nv_cot_truyen as NT  # noqa: E402
import nv_phu as NP  # noqa: E402
import nv_hoenn as NH  # noqa: E402
import nv_lap_lai as NL  # noqa: E402

D = os.path.join(GOC, "docs", "pokemon")
RA_SQL = os.path.join(GOC, "sql", "pokemon_nhiem_vu.sql")
RA_NHIP = os.path.join(D, "nhiem_vu_nhip_do.csv")
RA_WEB = os.path.join(D, "web", "nhiem_vu_tom_tat.json")
RA_DOC = os.path.join(D, "NHIEM_VU_THIET_KE.md")

# Kieu muc tieu dem theo LOAI (so dex trong muc_tieu_id): mot loai co the co nhieu template quai.
KIEU_THEO_LOAI = ("pokedex_gap", "pokedex_ha", "pokedex_shiny", "quan_sat")

# Cot them vao nhiem_vu_moi.csv (sau 30 cot cu - giu tuong thich).
COT_THEM = ["nhom", "nhan_vat", "map_id", "toa_do", "dieu_kien", "du_phong", "mo_ta", "muc_tieu_text",
            "hoi_thoai_bat_dau", "hoi_thoai_dang_lam", "hoi_thoai_xong", "goi_y", "dong_thuong",
            "thuong_khac", "nhanh_lua_chon", "an", "dieu_kien_mo", "dap_an", "phut"]

LD = None   # module sinh_sql_lau_dai (gan trong sinh())


# =====================================================================================
# DU LIEU DOC TU FILE
# =====================================================================================

def doc_csv(ten, delim=","):
    with open(os.path.join(D, ten), encoding="utf-8") as f:
        dong = [l for l in f if not l.startswith("#")]
    return list(csv.DictReader(dong, delimiter=delim))


class Kho:
    def __init__(self):
        self.map_ids = set()
        self.map_loai = {}
        for r in doc_csv("thiet_ke_map.csv", ";"):
            self.map_ids.add(int(r["id"]))
            self.map_loai[int(r["id"])] = r["loai"]
        # id template quai -> so dex (docs/pokemon/bang_quai.csv = data_monster.dex)
        self.dex = {int(r["id_quai"]): int(r["dex"]) for r in doc_csv("bang_quai.csv") if (r.get("dex") or "").isdigit()}
        # Ho cau ca (docs/pokemon/diem_cau_ca.csv, he sinh thai): CHI cau o ho/ao.
        self.ho = {}
        for r in doc_csv("diem_cau_ca.csv"):
            self.ho.setdefault(int(r["map_id"]), []).append((int(r["x"]), int(r["y"]), r["loai_ho"]))
        self.diem = {}
        # diem_co_dinh.csv KHONG co dong tieu de (chi chu thich "# Cot: map;x0;y0;x1;y1;noi;ghi_chu")
        with open(os.path.join(D, "diem_co_dinh.csv"), encoding="utf-8") as f:
            dong_dcd = [l for l in f if l.strip() and not l.startswith("#")]
        for r in csv.DictReader(dong_dcd, fieldnames=["map", "x0", "y0", "x1", "y1", "noi", "ghi_chu"], delimiter=";"):
            try:
                m, x0, y0, x1, y1 = (int(r[k]) for k in ("map", "x0", "y0", "x1", "y1"))
            except (ValueError, KeyError, TypeError):
                continue
            self.diem.setdefault(m, []).append(((x0 + x1) // 2, (y0 + y1) // 2, r.get("ghi_chu") or ""))
        self.alpha = {int(r["id_goc"]): (r["ten"], int(r["cap"])) for r in doc_csv("noi_dung_quai_tinh_anh.csv")}
        self.gym = {r["huy_hieu"]: r for r in doc_csv("noi_dung_gym.csv")}
        self.item_moi = {r["name"]: int(r["id"]) for r in doc_csv("noi_dung_item_moi.csv")}
        self.trang_bi = []
        for r in doc_csv("noi_dung_trang_bi.csv"):
            st = sum(int(r[k] or 0) for k in ("tan_cong", "phong_thu", "hp"))
            if st > 0:
                self.trang_bi.append((int(r["bac"]), int(r["cap_yeu_cau"]), r["o"], int(r["id_item"]), r["ten"]))
        self.trang_bi.sort()
        self.sk_vp = {}
        for r in doc_csv("noi_dung_su_kien_vat_pham.csv"):
            self.sk_vp.setdefault(int(r["tuan"]), []).append(r)
        self.su_kien = doc_csv("su_kien_nam.csv")
        self.trang_tri = doc_csv("noi_dung_trang_tri.csv")

    def luoi_di_duoc(self, map_id):
        """(w, h, [bool]) o di duoc - giong BanDoSinhThai.luoiDiDuoc (cMap_pokemon/<id> + <bo tile>.type)."""
        if not hasattr(self, "_luoi"):
            self._luoi = {}
        if map_id in self._luoi:
            return self._luoi[map_id]
        f = os.path.join(GOC, "server", "cMap_pokemon", str(map_id))
        if not os.path.isfile(f):
            self._luoi[map_id] = None
            return None
        b = open(f, "rb").read()
        w, h = b[0], b[1]
        tile = b[2:2 + w * h]
        bo = "t" if map_id < 110 else ("t_thanh" if map_id > 200 else "t_hang")
        ft = os.path.join(GOC, "server", "cMap_pokemon", bo + ".type")
        kieu = open(ft, "rb").read() if os.path.isfile(ft) else None
        di = [t >= 254 or (kieu is not None and t < len(kieu) and (kieu[t] & 2) == 0) for t in tile]
        self._luoi[map_id] = (w, h, di)
        return self._luoi[map_id]

    def diem_dung(self, map_id, chi_so):
        """O dung cho muc tieu "dung tai diem": diem co dinh (diem_co_dinh.csv) neu di duoc, khong thi o di
        duoc gan tam ban do nhat. Tra ve (x, y) hoac None."""
        l = self.luoi_di_duoc(map_id)
        if l is None:
            return None
        w, h, di = l
        ds = self.diem.get(map_id) or []
        for j in range(len(ds)):
            x, y, gc = ds[(chi_so + j) % len(ds)]
            if 0 <= x < w and 0 <= y < h and di[y * w + x]:
                return x, y
        cx, cy = w // 2, h // 2
        tot = sorted((abs(x - cx) + abs(y - cy), y, x) for y in range(h) for x in range(w) if di[y * w + x])
        return (tot[0][2], tot[0][1]) if tot else None

    def do_theo_cap(self, L, k):
        bac = max(b for b, c, o, i, t in self.trang_bi if c <= max(1, L))
        cung = [x for x in self.trang_bi if x[0] == bac]
        x = cung[k % len(cung)]
        return x[3], x[4]

    def vat_lieu_bac(self, L):
        if L < 30:
            return "Bụi sao"
        if L < 60:
            return "Mảnh sao"
        return "Mảnh thiên thạch"


# =====================================================================================
# TIEN ICH CHU
# =====================================================================================

TEN_NPC = {k: v[0] for k, v in NC.NPC.items()}


def ten_npc(t):
    return NC.NPC[int(t)][0] if t not in ("", None) and int(t) in NC.NPC else "Xatu"


def tham_so(s):
    d = {}
    for p in (s or "").split(";"):
        if "=" in p:
            k, v = p.split("=", 1)
            d[k.strip()] = v.strip()
    return d


def thay(s, gt):
    def f(m):
        k = m.group(1)
        if k in gt and isinstance(gt[k], int) and not isinstance(gt[k], bool) and gt[k] >= 1000:
            return so_dep(gt[k])   # so lon trong chu: 1.000
        return str(gt[k]) if k in gt and gt[k] not in (None,) else m.group(0)
    return re.sub(r"\{(\w+)\}", f, s)


NGUOI_NOI = re.compile(r"^([^:|]{2,24}): (.*)$")


def hoi_thoai(chu, nguoi_giao, gt):
    """'a|Ten: b|Bạn: c' -> 'Nguoi giao: a|Ten: b|Bạn: c' (moi cau co nguoi noi)."""
    ra = []
    for c in thay(chu, gt).split("|"):
        c = c.strip()
        if not c:
            continue
        m = NGUOI_NOI.match(c)
        if m and not m.group(1).startswith("'") and "..." not in m.group(1):   # "...Ta thay: x" la loi NPC giao
            ra.append(c)
        else:
            ra.append("%s: %s" % (nguoi_giao, c))
    return "|".join(ra)


def hoa_dau(t):
    return t[:1].upper() + t[1:]


def loi_mo(mo, cau, nguoi):
    """'Ten: <cau mo dac trung> <cau>': cau mo ket bang dau phay -> doi thanh '!' (khong viet hoa sau phay);
    '...' dinh lien chu sau."""
    if mo.endswith(","):
        mo = mo[:-1] + "!"
    return "%s: %s%s%s" % (nguoi, mo, "" if mo == "..." else " ", cau)


def so_dep(n):
    return "{:,}".format(int(n)).replace(",", ".")


# =====================================================================================
# BO SINH
# =====================================================================================

class Sinh:
    def __init__(self, s, kho):
        self.s = s          # SinhNV cua sinh_sql_lau_dai (s.quai, s.them, s.dong, s.id_moi)
        self.k = kho
        self.dem_do = 0

    # ---- quai -----------------------------------------------------------------------
    def quai(self, L, ts, k):
        q = ts.get("q", "+0")
        if q.startswith("#"):
            i = int(q[1:])
            ten = self.s.ten_quai_tat_ca.get(i, ("?", L))[0]
            return i, ten, self.s.ten_quai_tat_ca.get(i, ("?", L))[1]
        off = int(q)
        return self.s.chon_quai(L + off, k, len_tren=1 + max(0, off))

    def alpha(self, L, k):
        ung = sorted(((abs(c - L), c, i) for i, (t, c) in self.k.alpha.items()
                      if i in self.s.quai), key=lambda x: (x[0], x[2]))
        _, c, i = ung[k % min(3, len(ung))]
        return i, self.s.quai[i][0], c

    # ---- so luong -------------------------------------------------------------------
    def so_luong(self, kieu, L, ts, loai):
        if "so" in ts:
            return int(ts["so"])
        phut = {"chinh": 8, "phu": 7, "ngay": 5, "tuan": 60}.get(loai, 6)
        if kieu in ("giet", "pokedex_ha"):
            return LD.so_quai(L, phut)
        if kieu == "giet_lech_cap":
            return LD.so_quai(L, phut + 3)
        if kieu == "giet_tinh_anh":
            return max(3, LD.so_quai(L, phut) // 4)
        if kieu == "thu_thap":
            return max(3, LD.so_quai(L, phut) // 5)
        if kieu == "thu_thach_gio":
            p = int(ts.get("phut", 5))
            return max(5, int(LD.KPH[max(1, min(L, 100))] * p / 60.0 * 0.6))
        return 1

    # ---- 1 buoc ---------------------------------------------------------------------
    def buoc(self, loai, ma, L, buoc_td, giao, gt0, k, **them):
        """buoc_td = (kieu, tham so, ten, mo, xong). Tra ve dict dong (chua them vao s.dong)."""
        kieu, tss, ten, mo, xong = buoc_td
        gt = dict(gt0)
        ts = tham_so(thay(tss, gt))
        if "tu" in ts:
            giao = int(ts["tu"])
        npc_nhan = int(ts["npc"]) if "npc" in ts else None
        if kieu == "noi_chuyen" and npc_nhan is not None:
            giao, npc_nhan = npc_nhan, None
        nguoi = ten_npc(giao)
        noi = ts.get("noi") or ts.get("map") or gt.get("noi", "")
        gt["noi"] = noi
        mid, mten, mcap = "", "", ""
        if kieu in ("giet", "giet_tinh_anh", "thu_thap", "quan_sat", "thu_thach_gio", "pokedex_ha", "pokedex_gap",
                    "pokedex_shiny", "boss_the_gioi") and ("q" in ts or kieu not in ("boss_the_gioi",)):
            mid, mten, mcap = self.quai(L, ts, k)
        if kieu == "ha_alpha":
            mid, mten, mcap = self.alpha(L + int(ts.get("q", "0").lstrip("#") or 0) if not ts.get("q", "").startswith("#") else L, k)
        if kieu == "ha_gym":
            if "q" in ts:
                mid, mten, mcap = self.quai(L, ts, k)
            else:
                g = self.k.gym[them["huy_hieu"]]
                mid = int(g["template_quai"])
                mten = self.s.ten_quai_tat_ca[mid][0]
                gt["he"] = g["he"]
        so = self.so_luong(kieu, L, ts, loai)
        vat = ts.get("vat", "")
        npc_t = NC.NPC.get(int(giao), NC.NPC[5]) if str(giao).lstrip("-").isdigit() else NC.NPC[5]
        gt.update(goi=npc_t[2], Goi=hoa_dau(npc_t[2]), xung=npc_t[3], Xung=hoa_dau(npc_t[3]))
        ten_chu = mten
        if kieu == "pokedex_shiny" and "q" in ts:
            # chi dien TEN loai vao chu (muc_tieu_id van rong = engine nhan shiny bat ky, giu nguyen luat)
            ten_chu = self.quai(L, ts, k)[1]
        gt.update(quai=ten_chu or ("boss thế giới" if kieu == "boss_the_gioi" else ""), so=so, vat=vat, npc=nguoi, npc_nhan=ten_npc(npc_nhan) if npc_nhan is not None else nguoi,
                  phut=ts.get("phut", ""), bac=ts.get("bac", ""), so2=ts.get("so2", ""), dk=ts.get("dk", ""),
                  khi_dk=NC.KHI_DK.get(ts.get("dk", ""), "khi " + ts.get("dk", "")),
                  he=gt.get("he", ""), cap=L, da="{da}")
        doi_ho = ""
        if kieu == "cau_ca" and "map" not in ts:
            # chi cau o ho: noi khong co ho -> ho gan nhat theo cap (NP.NOI)
            if NC.DIA_DANH_MAP.get(noi) in self.k.ho:
                ts["map"] = noi
            else:
                ung = [(c, n) for (n, v, c, g, sm) in NP.NOI if NC.DIA_DANH_MAP.get(n) in self.k.ho]
                truoc_cap = [x for x in ung if x[0] <= L]
                ts["map"] = (truoc_cap[-1] if truoc_cap else ung[0])[1]
                doi_ho = ts["map"]
                gt["noi"] = noi = ts["map"]
        map_id, toa = "", ""
        if "map" in ts:
            map_id = NC.DIA_DANH_MAP[ts["map"]]
            gt["map"] = ts["map"]
            if "diem" in ts or kieu == "dung_toa_do":
                xy = self.k.diem_dung(map_id, int(ts.get("diem", "0")))
                if xy:
                    x, y = xy
                    toa = "%d;%d" % (x, y)   # dang chuan "x;y" (NhiemVuMau.docToaDo doc ca "x,y")
                    gt.update(x=x, y=y, diem="điểm đánh dấu")
        else:
            gt["map"] = noi
        if kieu == "cau_ca":
            if map_id not in self.k.ho:
                raise ValueError("%s: cau ca o ban do khong co ho (%s)" % (ma, map_id))
            if not toa:
                x, y, lh = self.k.ho[map_id][0]
                toa = "%d;%d" % (x, y)
        K = NC.KIEU[kieu]
        mau_muc, mau_goi_y, mau_dl = K[4], K[5], [K[6]] + NC.DANG_LAM_THEM.get(kieu, [])
        if kieu == "dung_toa_do" and not toa:
            # khong co diem danh dau: engine chap nhan moi vi tri tren ban do -> khong in "o ?,?"
            mau_muc, mau_goi_y = "Đến {map} và đứng yên vài giây", "Tới {map} rồi đứng yên vài giây là được."
            mau_dl = ["Chưa đúng chỗ. Tới {map} rồi đứng yên một lát nhé."]
        if kieu == "ha_gym" and not gt.get("he"):
            mau_goi_y = "Trưởng Gym rất mạnh: mang chiêu khắc hệ và đồ +6 trở lên."
        ten_ra = thay(ten, gt)[:45]
        muc_tieu_text = thay(mau_muc, gt)
        goi_y = thay(mau_goi_y, gt)
        dang_lam = loi_mo(npc_t[1], thay(mau_dl[k % len(mau_dl)], gt), nguoi)
        bd = hoi_thoai(mo, nguoi, gt)
        if doi_ho:
            bd += "|%s: Chỗ này không có hồ. Hồ gần nhất có cá là ở %s, ra đó mà câu nhé." % (nguoi, doi_ho)
        xg = hoi_thoai(xong, nguoi, gt)
        mo_ta = them.get("mo_ta") or re.sub(r"^[^:]+: ", "", bd.split("|")[0])[:160]
        dk = ""
        if kieu == "thu_thach_gio":
            dk = "gioi_han_phut=" + ts.get("phut", "5")
        if kieu == "to_doi":
            dk = "to_doi_toi_thieu=" + ts.get("so2", "2")
        if kieu == "giup_tan_thu":
            dk = "thanh_vien_duoi_cap=20"
        if kieu == "dung_toa_do":
            dk = "ban_kinh_o=3"
        # khop he sinh thai (docs/pokemon/HE_SINH_THAI.md)
        if kieu == "quan_sat":
            dk = "dieu_kien_quan_sat=" + NC.DK_QUAN_SAT[ts.get("dk", "")]
        if kieu in ("cau_ca", "lam_vuon", "nau_an"):
            if vat in NC.VAT_SINH_THAI:
                dk = "vat_sinh_thai=" + NC.VAT_SINH_THAI[vat]
            elif kieu == "cau_ca" and vat in NC.CAU_VAT_NHIEM_VU:
                dk = "vat_nhiem_vu_khi_cau=%s" % NC.VAT_NV_ID[vat]
            else:
                raise ValueError("%s: %s '%s' khong khop vat lieu he sinh thai" % (ma, kieu, vat))
        if kieu == "dao_dat":
            dk = "khao_co=1" if (map_id != "" and self.k.map_loai.get(map_id) in NC.LOAI_MAP_KHAO_CO) else "diem_dao_nhiem_vu=1"
        if kieu == "ap_trung":
            dk = "trung=bat_ky"
        nhanh = ""
        if kieu == "lua_chon":
            a, b = ts["chon"].split("/")
            da, db = (ts.get("dh", "/").split("/") + [""])[:2]
            nhanh = "A:%s=>danh_hieu:%s|B:%s=>danh_hieu:%s" % (a, da, b, db)
        # du phong engine cu (data_quest type 0..4)
        dp = K[2]
        du_phong = ""
        if dp != "":
            if dp == 0:
                if kieu in ("giet", "giet_tinh_anh"):
                    du_phong = "0:%s:%s" % (mid, so)
                else:  # alpha / gym / boss -> quai manh gan cap
                    fi, fn, fc = self.s.chon_quai(L + 3, k, len_tren=3)
                    du_phong = "0:%s:%s" % (fi, max(3, LD.so_quai(L, 4) // 4))
            elif dp == 4:
                du_phong = "4:%s" % (so if kieu != "to_doi" and kieu != "doi_dong_gop" else LD.so_quai(L, 8))
            elif dp == 2:
                fi, fn, fc = self.s.chon_quai(L, k)
                du_phong = "2:%s:%s" % (fi, max(3, LD.so_quai(L, 7) // 5))
            elif dp == 1:
                du_phong = "1:%s" % (npc_nhan if npc_nhan is not None else giao)
            elif dp == 3:
                du_phong = "3:%s" % giao
        if kieu == "thu_thap":
            mt_id, mt_ten = NC.VAT_NV_ID.get(vat, ""), vat
        else:
            mt_id = mid if mid != "" else (npc_nhan if npc_nhan is not None else "")
            if kieu in KIEU_THEO_LOAI and mid != "":
                # engine (PokedexPokemon/RoiLauDai -> NhiemVuEngine.suKien) gui SO DEX cua loai, khong phai id template
                mt_id = self.k.dex[int(mid)]
            mt_ten = mten or vat or (ten_npc(npc_nhan) if npc_nhan is not None else "")
        d = dict(ma=ma, loai=loai, ten=ten_ra, npc_giao=giao, ten_npc=nguoi, dia_danh=them.get("dia_danh") or noi,
                 cap_toi_thieu=L, kieu_muc_tieu=kieu, muc_tieu_id=mt_id, muc_tieu_ten=mt_ten[:200], so_luong=so,
                 quai_roi=mid if kieu == "thu_thap" else "",
                 map_id=map_id, toa_do=toa, dieu_kien=dk, du_phong=du_phong, mo_ta=mo_ta,
                 muc_tieu_text=muc_tieu_text, hoi_thoai_bat_dau=bd, hoi_thoai_dang_lam=dang_lam,
                 hoi_thoai_xong=xg, goi_y=goi_y, nhanh_lua_chon=nhanh, an=0, dap_an=ts.get("dap", ""),
                 phut=them.get("phut", 6), nhan_vat=them.get("nhan_vat", ""), nhom=them.get("nhom", loai),
                 vat_nv=vat)
        if kieu == "cau_ca":
            d["dia_danh"] = ts["map"]   # cau ca chi o ho: dia danh = noi co ho
        for kk in ("nam", "vung", "cung", "chuong", "buoc", "ngay_mo", "truoc", "lap_lai", "gioi_han",
                   "huy_hieu", "ghi_chu", "dieu_kien_mo", "id_so"):
            if kk in them:
                d[kk] = them[kk]
        return d


# =====================================================================================
# CHIA NGAN SACH
# =====================================================================================

def chia_nguyen(tong, trong_so):
    """Chia so nguyen tong theo trong so (phan du cho dong trong so lon, roi dong dau)."""
    t = sum(trong_so)
    if t <= 0:
        return [0] * len(trong_so)
    ra = [int(tong * w / t) for w in trong_so]
    con = tong - sum(ra)
    thu_tu = sorted(range(len(trong_so)), key=lambda i: (-trong_so[i], i))
    i = 0
    while con > 0 and thu_tu:
        ra[thu_tu[i % len(thu_tu)]] += 1
        con -= 1
        i += 1
    return ra


def he_tan_thu(loai, L):
    return LD.HE_SO_TAN_THU if (L < LD.CAP_TAN_THU and loai in ("huong_dan", "chinh", "phu", "tuyen")) else 1.0


def gan_gio(dong, gio_exp, gio_xu):
    """gio_* = gio thu nhap (da gom he tan thu) -> exp, xu tuyet doi theo cap dong."""
    L = max(1, min(int(dong["cap_toi_thieu"]), LD.CAP_TOI_DA))
    dong["exp"] = int(gio_exp * LD.EXP_GIO[L])
    dong["xu"] = int(gio_xu * LD.XU_GIO[L])


def tong_cu():
    """Tong ngan sach (gio) cua bang 721 dong cu theo tung nhom - de can bang lai."""
    NS = LD.NGAN_SACH
    CT = LD.CT
    t = {}
    # phu + tuyen
    e = x = 0.0
    lk = 0
    for (noi, vung, L0, npc, cd) in CT.THI_TRAN:
        for bi in range(1, 6):
            L = L0 + (bi - 1) * 2
            h = he_tan_thu("phu", L)
            e += NS["phu"][0] * h
            x += NS["phu"][1] * h
            lk += NS["phu"][2]
    for (noi, L0) in CT.TUYEN:
        for bi in range(1, 4):
            L = L0 + bi - 1
            h = he_tan_thu("tuyen", L)
            e += NS["tuyen"][0] * h
            x += NS["tuyen"][1] * h
            lk += NS["tuyen"][2]
    t["phu"] = (e, x, lk)
    for loai, n in (("doi", 8), ("su_pham", 5), ("hang_bi_an", 5), ("quay_lai", 3)):
        t[loai] = (NS[loai][0] * n, NS[loai][1] * n, NS[loai][2] * n)
    t["thanh_tuu"] = (0, 0, NS["thanh_tuu"][2] * len(CT.THANH_TUU),
                      NS["thanh_tuu"][3] * sum(1 for a in CT.THANH_TUU if a[2] >= 20))
    return t


# =====================================================================================
# CAC NHOM
# =====================================================================================

def sinh(s):
    """Thay sinh_sql_lau_dai.sinh_nhiem_vu: them moi dong vao s.dong."""
    global LD
    LD = sys.modules.get("sinh_sql_lau_dai") or sys.modules["__main__"]
    kho = Kho()
    s.ten_quai_tat_ca = getattr(s, "ten_quai_tat_ca", None) or {i: v for i, v in s.quai.items()}
    G = Sinh(s, kho)
    TC = tong_cu()
    NS = LD.NGAN_SACH
    CT = LD.CT

    def them(d):
        d.setdefault("nam", 1)
        d.setdefault("lap_lai", "khong")
        d.setdefault("dong_co_tuong_thich", 0)
        for c in LD.COT_NV + COT_THEM:
            d.setdefault(c, "")
        s.dong.append(d)
        return d

    # ---------------- 1. Huong dan (10 dong, giu ngan sach cu) ----------------
    hd = [
        ("noi_chuyen", "", "Thức giấc ở Pallet", "...Cháu tỉnh rồi. Đây là Pallet, làng của những Pokémon hiền lành.|Ta là Xatu, trưởng làng. Ta sẽ chỉ cháu những điều đầu tiên.", "Đi chào Kecleon ở tiệm tạp hoá trước nhé."),
        ("noi_chuyen", "npc=1", "Chào Kecleon", "Hí hí, khách mới! Tiệm tôi có Thuốc hồi máu và Ether. Hết máu thì ghé nhé.", "Giờ tập đánh một chút. Ra đồng đi bạn nhỏ."),
        ("giet", "q=+0", "Bài học tấn công", "Chạm vào quái để đánh. Hạ {so} {quai} ngoài đồng.", "Giỏi lắm! Lên cấp là mạnh hơn đấy."),
        ("noi_chuyen", "npc=0", "Bình thuốc đầu tiên", "Chan-sey! Máu thấp thì dùng thuốc trong túi nhé. Chị cho em vài bình.", "Nhớ hồi máu trước khi đánh quái mạnh."),
        ("giet", "q=+1", "Bài học chiêu thức", "Dùng chiêu thức (phím tắt) để đánh mạnh hơn. Hạ {so} {quai}.", "Chiêu thức hồi chiêu, xen kẽ với đòn thường."),
        ("noi_chuyen", "npc=2", "Gặp Machamp thợ chế tạo", "Khà khà! Ta lo đồ đạc. Nâng cấp, chế tạo, khảm ngọc: tìm ta.", "Mặc thử món đồ ta tặng đi."),
        ("nang_cap", "", "Mặc trang bị", "Mở túi, chọn món đồ rồi chọn Mặc.", "Ra dáng rồi đấy!"),
        ("giet", "q=+2", "Thử sức ngoài đồng", "Đi xa hơn một chút. Hạ {so} {quai}.", "Cháu tiến bộ nhanh lắm."),
        ("pokedex_gap", "q=+0", "Sổ tay Pokédex", "Mỗi loài cháu gặp sẽ được ghi vào Pokédex. Thử gặp {quai} đi.", "Sổ có trang đầu tiên rồi."),
        ("noi_chuyen", "npc=5", "Lời hứa với Xatu", "...Cháu đã sẵn sàng. Ngôi sao rơi đêm qua đang chờ cháu tìm hiểu.", "Đi đi. Ta sẽ dõi theo."),
    ]
    truoc = ""
    for i, b in enumerate(hd, 1):
        L = 1 + (i - 1) // 2
        ex, xu, lk, lg = s.thuong("huong_dan", L)
        d = G.buoc("huong_dan", "HD.%02d" % i, L, b, 5, {"noi": "Pallet"}, i, vung="Kanto", cung="HD", chuong=0,
                   buoc=i, ngay_mo=0, truoc=truoc, ghi_chu="Hướng dẫn: 10 bước ~30 phút, xong ở cấp ~8",
                   id_so=900 + i, dia_danh="Pallet", phut=3)
        d.update(exp=ex, xu=xu, luong_khoa=lk, luong=lg, so_luong=d["so_luong"] if b[0] != "giet" else 10,
                 vat_pham=kho.do_theo_cap(1, i)[0] if b[0] == "nang_cap" else "", dong_co_tuong_thich=1)
        them(d)
        truoc = d["ma"]

    # ---------------- 2. Cot truyen nam 1 ----------------
    stt = 0
    id_c = 1000
    gym_ma = {}
    for (mc, tc, vung, chuongs) in CT.CUNG_NAM1:
        for ci, (tch, noi, npc, L) in enumerate(chuongs, 1):
            stt += 1
            ch = NT.NAM1[stt]
            hh = LD.HUY_HIEU.get((mc, ci), "")
            bs = ch["buoc"]
            n = len(bs)
            w = [0.8] + [1.0] * (n - 1)
            for bi, b in enumerate(bs):
                if b[0] == "ha_gym" or bi == n - 1:
                    w[bi] = 1.6
            h = he_tan_thu("chinh", L)
            gexp = (NS["chinh"][0] * 3 + (NS["chinh_moc"][0] if hh else NS["chinh"][0])) * h
            gxu = (NS["chinh"][1] * 3 + (NS["chinh_moc"][1] if hh else NS["chinh"][1])) * h
            lk_tong = NS["chinh"][2] * 3 + (NS["chinh_moc"][2] if hh else NS["chinh"][2])
            idx_gym = next((i for i, b in enumerate(bs) if b[0] == "ha_gym"), None) if hh else None
            if idx_gym is not None:
                lks = chia_nguyen(lk_tong - NS["chinh_moc"][2], [1 if i != idx_gym else 0 for i in range(n)])
                lks[idx_gym] += NS["chinh_moc"][2]
            else:
                lks = chia_nguyen(lk_tong, w)
            for bi, b in enumerate(bs, 1):
                id_c += 1
                cuoi = bi == n
                la_gym = idx_gym is not None and bi - 1 == idx_gym
                ma = "C%02d.%d" % (stt, bi)
                d = G.buoc("chinh", ma, L, b, npc, {"noi": noi}, stt * 7 + bi, nam=1, vung=vung, cung=mc + " " + tc,
                           chuong=stt, buoc=bi, ngay_mo=0, truoc=truoc, id_so=id_c, nhom="chinh",
                           huy_hieu=hh if la_gym else "", dia_danh=noi, phut=int(round(LD.GIO_MOI_CHUONG * 60 / n)),
                           ghi_chu=("Cần 1 vé chương (2/tuần, tích tối đa 4)" if bi == 1 else
                                    ("Cliffhanger: " + ch["treo"] if cuoi else "")),
                           mo_ta=(ch["mo"] if bi == 1 else None))
                gan_gio(d, gexp * w[bi - 1] / sum(w), gxu * w[bi - 1] / sum(w))
                d["luong_khoa"] = lks[bi - 1]
                d["luong"] = 0
                tk = []
                if cuoi:
                    vid, vten = kho.do_theo_cap(L, stt)
                    d["vat_pham"] = vid
                    tk.append("truyen:Chương %d - %s" % (stt, tch))
                if la_gym:
                    tk.append("item:%dx3" % kho.item_moi["Huy chương Gym"])
                    gym_ma[hh] = ma
                if b[0] == "lua_chon":
                    pass
                d["thuong_khac"] = ";".join(tk)
                d["dong_co_tuong_thich"] = 1 if d["du_phong"] else 0
                if not d["du_phong"]:
                    d["du_phong"] = "3:%s" % d["npc_giao"]
                    d["dong_co_tuong_thich"] = 1
                them(d)
                truoc = ma
    G.gym_ma = gym_ma

    # ---------------- 3. Nam 2 ----------------
    id2 = 1500
    for ai, (mc, tc, vung, tens, L) in enumerate(CT.CUNG_NAM2):
        for ci, tch in enumerate(tens, 1):
            stt += 1
            ngay_mo = 365 if L <= 95 else 545
            ex, xu, lk, lg = s.thuong("chinh", L, he=4.0)
            lk_tong = lk * 4
            if stt in NT.NAM2 or stt in NH.NAM2_THEM:
                ch = NT.NAM2[stt] if stt in NT.NAM2 else NH.NAM2_THEM[stt]
                # ban do Hoenn (BanDoHoenn): moi buoc co map/diem + quai muc tieu moc tren ban do do
                cap_quai = {i: c for i, (t, c) in s.ten_quai_tat_ca.items()}
                bs = [NH.chuan_buoc(stt, bi, b, L, cap_quai) for bi, b in enumerate(ch["buoc"], 1)]
                noi_h = NH.CHUONG[stt][0]
                n = len(bs)
                w = [0.8] + [1.0] * (n - 2) + [1.6]
                lks = chia_nguyen(lk_tong, w)
                for bi, b in enumerate(bs, 1):
                    id2 += 1
                    ma = "C%02d.%d" % (stt, bi)
                    d = G.buoc("chinh", ma, L, b, ch["npc"], {"noi": tch}, stt * 7 + bi, nam=2, vung=vung,
                               cung=mc + " " + tc, chuong=stt, buoc=bi, ngay_mo=ngay_mo, truoc=truoc, id_so=id2,
                               nhom="chinh", dia_danh=tch, phut=int(round(LD.GIO_MOI_CHUONG * 60 / n)),
                               ghi_chu=("Cần 1 vé chương; năm 2: bản đồ Hoenn %s (map %d)" % (noi_h, NH.DIA_DANH[noi_h])
                                        if bi == 1 else ("Cliffhanger: " + ch["treo"] if bi == n else "")),
                               mo_ta=(ch["mo"] if bi == 1 else None))
                    d["exp"] = int(ex * w[bi - 1] / sum(w))
                    d["xu"] = int(xu * w[bi - 1] / sum(w))
                    d["luong_khoa"] = lks[bi - 1]
                    d["luong"] = 0
                    if bi == n:
                        d["vat_pham"] = kho.do_theo_cap(L, stt)[0]
                        d["thuong_khac"] = "truyen:Chương %d - %s" % (stt, tch)
                    them(d)
                    truoc = ma
            else:
                id2 += 1
                ma = "C%02d" % stt
                d = dict(ma=ma, id_so=id2, loai="chinh", nam=2, vung=vung, cung=mc + " " + tc, chuong=stt, buoc=0,
                         ten=tch, npc_giao="", ten_npc="", dia_danh=tch, cap_toi_thieu=L, ngay_mo=ngay_mo,
                         truoc=truoc, kieu_muc_tieu="dan_y", lap_lai="khong", exp=ex, xu=xu, luong_khoa=lk_tong,
                         luong=0, dong_co_tuong_thich=0, nhom="dan_y",
                         ghi_chu="Dàn ý năm 2: cần map/quái Hoenn + cấp >91 (GĐ3)",
                         mo_ta="Chương sắp mở: %s (phần %s)." % (tch, tc),
                         muc_tieu_text="Chương sắp mở (năm 2)",
                         goi_y="Mở khi bản đồ Hoenn của chương này sẵn sàng; cần cấp %d và xong chương trước." % L,
                         hoi_thoai_bat_dau="Xatu: ...Ta chưa nhìn rõ đoạn đường này. Hãy đạt cấp %d và đi tiếp câu chuyện." % L,
                         hoi_thoai_dang_lam="Xatu: ...Chưa tới lúc.", hoi_thoai_xong="Xatu: ...Câu chuyện vẫn còn tiếp.",
                         phut=36)
                them(d)
                truoc = ma

    # ---------------- 4. Phu: chuoi NPC ----------------
    phu = []
    mau_i = 0
    id_p = 2000
    for pi, (noi, vung, L0, npc, so_mau) in enumerate(NP.NOI, 1):
        chuoi = [(0, NP.DAC_TRUNG[noi][0], NP.DAC_TRUNG[noi][2], npc, NP.DAC_TRUNG[noi][3])]
        co_ho = NC.DIA_DANH_MAP.get(noi) in kho.ho
        for j in range(so_mau):
            # chuoi mau co cau ca chi gan cho noi co ho (cau ca chi o ho)
            while not co_ho and any(b[0] == "cau_ca" for b in NP.MAU[mau_i % len(NP.MAU)][4]):
                mau_i += 1
            m = NP.MAU[mau_i % len(NP.MAU)]
            nv = m[1][(mau_i // len(NP.MAU)) % 2][0]
            mau_i += 1
            chuoi.append((j + 1, nv, m[2], m[3], m[4]))
        for ci, nv, tchuoi, giao, bs in chuoi:
            truoc_p = ""
            Lc = L0 + (ci * 2 if ci else 0)
            for bi, b in enumerate(bs, 1):
                id_p += 1
                L = min(100, Lc + (bi - 1))
                ma = "P-%02d.%d.%d" % (pi, ci, bi)
                d = G.buoc("phu", ma, L, b, giao, {"noi": noi, "nv": nv}, pi * 5 + ci + bi, vung=vung,
                           cung="%s: %s" % (noi, tchuoi), buoc=bi, ngay_mo=365 if L > 91 else 0, truoc=truoc_p,
                           id_so=id_p, nhom="chuoi_dac_trung" if ci == 0 else "chuoi_npc", nhan_vat=nv,
                           dia_danh=noi, phut=12,
                           ghi_chu=("Chuỗi %d bước; bước cuối cho danh hiệu" % len(bs)))
                d["cuoi"] = bi == len(bs)
                d["dac_trung"] = ci == 0
                tk = []
                if bi == len(bs):
                    tk.append("danh_hieu:%s %s" % ("Người bạn của" if ci else "Người hùng", noi if ci == 0 else nv))
                    if ci == 0:
                        d["vat_pham"] = kho.do_theo_cap(L, pi)[0]
                    else:
                        tk.append("item:%dx1" % kho.item_moi[kho.vat_lieu_bac(L)])
                d["thuong_khac"] = ";".join(tk)
                if d["nhanh_lua_chon"]:
                    pass
                d["dong_co_tuong_thich"] = 1
                if not d["du_phong"]:
                    d["du_phong"] = "3:%s" % d["npc_giao"]
                phu.append(them(d))
                truoc_p = ma
    # ngan sach phu: tong gio = tong cu
    e0, x0, lk0 = TC["phu"]
    tw = [2.0 if d["cuoi"] else 1.0 for d in phu]
    he = [tw[i] * he_tan_thu("phu", int(d["cap_toi_thieu"])) for i, d in enumerate(phu)]
    ke = e0 / sum(he)
    kx = x0 / sum(he)
    lks = chia_nguyen(lk0, tw)
    for i, d in enumerate(phu):
        gan_gio(d, ke * he[i], kx * he[i])
        d["luong_khoa"] = lks[i]
        d["luong"] = 0
        d.pop("cuoi")
        d.pop("dac_trung")

    # ---------------- 5. Ngay / truy na ----------------
    id_n = 3000
    noi_theo_cap = sorted(((n[2], n[0]) for n in NP.NOI if n[0] in NC.DIA_DANH_MAP))
    for bac, Lb in enumerate(NL.BAC_NGAY, 1):
        Lhet = (NL.BAC_NGAY[bac] - 1) if bac < len(NL.BAC_NGAY) else 100
        for i, t in enumerate(NL.NGAY, 1):
            cmin, kieu, tss, ten, mo, xong = t
            if cmin > Lhet:
                continue
            L = max(Lb, cmin)
            id_n += 1
            noi = [n for c, n in noi_theo_cap if c <= L][-1]
            truy_na = kieu in ("ha_alpha", "giet_tinh_anh")
            giao = 4 if truy_na else 21
            d = G.buoc("ngay", "N-%d.%02d" % (bac, i), L, (kieu, tss, ten, mo, xong), giao, {"noi": noi}, i + bac,
                       vung="Mọi vùng", cung="Bảng truy nã" if truy_na else "Bảng nhiệm vụ ngày", lap_lai="ngay",
                       gioi_han=LD.GIOI_HAN_NGAY, id_so=id_n, nhom="truy_na" if truy_na else "bang_ngay",
                       dia_danh=noi, phut=6,
                       ghi_chu="Bậc %d-%d; mỗi ngày giao ngẫu nhiên 5 mẫu theo bậc, thưởng tối đa %d" % (
                           Lb, Lhet, LD.GIOI_HAN_NGAY))
            ex, xu, lk, lg = s.thuong("ngay", L)
            d.update(exp=ex, xu=xu, luong_khoa=lk, luong=0, dong_co_tuong_thich=0,
                     thuong_khac="diem_bang:1")
            them(d)

    # ---------------- 6. Tuan ----------------
    id_t = 3500
    for bac, Lt in NL.BAC_TUAN:
        for i, (ten, loi) in enumerate(NL.TUAN_TEN, 1):
            id_t += 1
            d = G.buoc("tuan", "T-%d.%02d" % (bac, i), Lt, ("giet_lech_cap", "so=%d" % LD.so_quai(Lt, 60), ten, loi, "Tuần này nhóc làm tốt lắm. Lửa lên nào!"),
                       21, {"noi": "Trung tâm Pokémon"}, i, vung="Mọi vùng", cung="Bảng nhiệm vụ tuần",
                       lap_lai="tuan", gioi_han=LD.GIOI_HAN_TUAN, id_so=id_t, nhom="tuan",
                       dia_danh="Trung tâm Pokémon", phut=60, ghi_chu="Reset thứ Hai 05:00; tính vào 2 nhiệm vụ tuần")
            d["so_luong"] = LD.so_quai(Lt, 60)
            d["muc_tieu_text"] = "Hạ %s Pokémon hoang dã chênh không quá 5 cấp" % so_dep(d["so_luong"])
            d["hoi_thoai_bat_dau"] = d["hoi_thoai_bat_dau"].replace("{so}", so_dep(d["so_luong"]))
            ex, xu, lk, lg = s.thuong("tuan", Lt)
            d.update(exp=ex, xu=xu, luong_khoa=lk, luong=0, du_phong="4:%d" % d["so_luong"], dong_co_tuong_thich=0)
            them(d)
    for ci, (ten_c, vung, noi, L, bs) in enumerate(NL.CHUOI_VUNG, 1):
        truoc_t = ""
        for bi, b in enumerate(bs, 1):
            id_t += 1
            ma = "TV-%02d.%d" % (ci, bi)
            d = G.buoc("tuan", ma, L, b, 5, {"noi": noi}, ci + bi, vung=vung, cung="Chuỗi tuần: " + ten_c,
                       buoc=bi, lap_lai="tuan", gioi_han=LD.GIOI_HAN_TUAN, truoc=truoc_t, id_so=id_t,
                       nhom="chuoi_vung", dia_danh=noi, phut=20,
                       ghi_chu="Cả chuỗi 3 bước = 1 trong 2 nhiệm vụ tuần (thưởng chia 3)")
            ex, xu, lk, lg = s.thuong("tuan", L)
            d.update(exp=ex // 3, xu=xu // 3, luong_khoa=chia_nguyen(lk, [1, 1, 1])[bi - 1], luong=0,
                     dong_co_tuong_thich=0)
            if bi == 3:
                d["thuong_khac"] = "danh_hieu:Người gác %s" % noi
            them(d)
            truoc_t = ma
    for i, (ten, L) in enumerate(NL.TO_DOI, 1):
        id_t += 1
        so2 = 2 if L < 40 else 3
        d = G.buoc("tuan", "TD-%02d" % i, L, ("to_doi", "so=%d;so2=%d" % (LD.so_quai(L, 40) * 2, so2), ten,
                                               "Lửa lên nào! Tuần này cần sức cả nhóm: lập nhóm từ {so2} Pokémon, cùng hạ {so} quái.",
                                               "Cả nhóm làm tốt lắm!"),
                   21, {"noi": "Trụ sở đội"}, i, vung="Mọi vùng", cung="Nhiệm vụ nhóm", lap_lai="tuan", gioi_han=1,
                   id_so=id_t, nhom="to_doi", dia_danh="Trụ sở đội", phut=40,
                   ghi_chu="Cả tổ cùng tiến độ; tính vào 2 nhiệm vụ tuần")
        ex, xu, lk, lg = s.thuong("tuan", L)
        d.update(exp=ex, xu=xu, luong_khoa=lk, luong=0, dong_co_tuong_thich=0)
        them(d)

    # ---------------- 7. Pokedex ----------------
    loai_dex = sorted(set((t, c) for i, (t, c) in s.quai.items()), key=lambda x: (x[1], x[0]))
    da = set()
    dex = []
    k = 0
    id_d = 4000
    for ten, cap in loai_dex:
        if ten in da or "Shiny" in ten:
            continue
        da.add(ten)
        k += 1
        qid = [i for i, (t, c) in s.quai.items() if t == ten][0]
        for bac, (kieu, sl, nhan, hs) in enumerate((("pokedex_gap", 1, "Gặp %s lần đầu", 0.5),
                                                    ("pokedex_ha", 50, "Nghiên cứu %s", 1.0),
                                                    ("pokedex_shiny", 1, "Săn %s shiny", 2.0))):
            if bac == 2 and k % 3 != 0:
                continue
            L = max(1, cap - 5)
            id_d += 1
            d = G.buoc("pokedex", "D-%03d.%d" % (k, bac + 1), L,
                       (kieu, "q=#%d;so=%d" % (qid, sl), nhan % ten,
                        ["Oáp... trang về {quai} trong sổ ông còn trống. Cháu gặp nó thử xem.",
                         "Muốn hiểu {quai} thì phải đấu với nó nhiều lần. Hạ {so} con nhé.",
                         "Nghe nói có {quai} màu sắc khác thường. Ông muốn thấy một lần trong đời."][bac],
                        ["Ghi xong! Sổ dày thêm một trang.", "Giờ ông biết {quai} ăn gì, ngủ đâu. Hay lắm.",
                         "Oáp... đẹp quá. Cảm ơn cháu."][bac]),
                       8, {"noi": "Phòng nghiên cứu"}, k, vung="Mọi vùng", cung="Nghiên cứu Pokédex", id_so=id_d,
                       nhom="pokedex", dia_danh="Phòng nghiên cứu", phut=[1, 10, 30][bac],
                       ghi_chu="Đọc tob_pokedex (so_ha); shiny: tob_pokedex_shiny")
            d["w"] = hs
            d["lk_cu"] = 5 * (bac + 1)
            if bac == 2:
                d["thuong_khac"] = "item:%dx1" % kho.item_moi["Tem nghiên cứu"]
            dex.append(them(d))
    e_dex = sum(NS["pokedex"][0] * d["w"] for d in dex)
    x_dex = sum(NS["pokedex"][1] * d["w"] for d in dex)
    lk_dex = sum(d["lk_cu"] for d in dex)
    # quan sat sinh thai (them, cung quy)
    for j, (ten, cap) in enumerate([x for x in loai_dex if "Shiny" not in x[0]][:40]):
        qid = [i for i, (t, c) in s.quai.items() if t == ten][0]
        dk = NL.QUAN_SAT_DK[j % len(NL.QUAN_SAT_DK)]
        id_d += 1
        d = G.buoc("pokedex", "DQ-%03d" % (j + 1), max(1, cap - 3),
                   ("quan_sat", "q=#%d;dk=%s" % (qid, dk), "%s %s" % (ten, NC.KHI_DK.get(dk, "khi " + dk)),
                    "Oáp... {quai} {khi_dk} sống khác hẳn. Quan sát giúp ông một lần.",
                    "Ông ghi lại rồi. {quai} {khi_dk} thú vị thật."),
                   8, {"noi": "Phòng nghiên cứu"}, j, vung="Mọi vùng", cung="Nghiên cứu sinh thái", id_so=id_d,
                   nhom="sinh_thai", dia_danh="Phòng nghiên cứu", phut=15,
                   ghi_chu="Cần thời tiết/giờ (SinhThaiTheGioi)")
        d["w"] = 0.5
        d["lk_cu"] = 0
        dex.append(them(d))
    tw = sum(d["w"] for d in dex)
    lks = [min(x, 15) for x in chia_nguyen(lk_dex, [d["w"] for d in dex])]
    for i, d in enumerate(dex):
        Ld = max(1, int(d["cap_toi_thieu"]))
        d["exp"] = int(e_dex * d["w"] / tw * LD.EXP_GIO[Ld])
        d["xu"] = int(x_dex * d["w"] / tw * LD.XU_GIO[Ld])
        d["luong_khoa"] = lks[i]
        d["luong"] = 0
        d.pop("w")
        d.pop("lk_cu")

    # ---------------- 8. Doi, su pham, ham nguc, quay lai ----------------
    def nhom_scale(loai, ds):
        e, x, lk = TC[loai][0], TC[loai][1], TC[loai][2]
        lks = chia_nguyen(int(lk), [1] * len(ds))
        for i, d in enumerate(ds):
            gan_gio(d, e / len(ds), x / len(ds))
            d["luong_khoa"] = lks[i]
            d["luong"] = 0

    ds = []
    for i, (ten, kieu, L) in enumerate(NL.DOI, 1):
        so = LD.so_quai(L, 40) * 3
        d = G.buoc("doi", "DOI-%02d" % i, L, (kieu, "so=%d" % (so if kieu == "doi_dong_gop" else 1), ten,
                                               "Hự! Đội của chiến hữu đang cần sức. Tuần này hãy góp {so} điểm!" if kieu == "doi_dong_gop"
                                               else "Hự! Trận chiếm Gym tuần này, cả đội phải có mặt!",
                                               "Hự! Đội tự hào về chiến hữu."),
                   25, {"noi": "Lãnh địa đội"}, i, vung="Mọi vùng", cung="Nhiệm vụ Đội", lap_lai="tuan", gioi_han=1,
                   id_so=5000 + i, nhom="doi", dia_danh="Lãnh địa đội", phut=40,
                   ghi_chu="mainsub=2 (Đội); cả Đội cùng tiến độ")
        d["dong_co_tuong_thich"] = 1
        if not d["du_phong"]:
            d["du_phong"] = "4:%d" % LD.so_quai(L, 40)
        ds.append(them(d))
    nhom_scale("doi", ds)
    ds = []
    for i, ten in enumerate(NL.SU_PHAM, 1):
        L = 25 + i * 5
        d = G.buoc("su_pham", "SP-%02d" % i, L, ("kem_cap", "", ten,
                                                  "...Có những Pokémon mới cần người dẫn đường. Cháu nhận một học trò nhé.",
                                                  "...Học trò của cháu tiến bộ lắm. Ta tự hào."),
                   5, {"noi": "Pallet"}, i, vung="Mọi vùng", cung="Sư phạm", lap_lai="tuan", gioi_han=1,
                   id_so=5100 + i, nhom="su_pham", dia_danh="Pallet", phut=60,
                   ghi_chu="Thầy (cấp ≥ 30) và trò (thấp hơn ≥ 20 cấp) cùng nhận; trò +20% EXP khi chung đội")
        d["muc_tieu_text"] = "Kèm học trò đạt mốc: %s" % ten
        ds.append(them(d))
    nhom_scale("su_pham", ds)
    ds = []
    for i, ten in enumerate(NL.HAM_NGUC, 1):
        L = 20 + (i - 1) * 12
        d = G.buoc("hang_bi_an", "HM-%02d" % i, L, ("tang_hang", "so=%d" % (5 * i), ten,
                                                     "Oáp... Hầm ngục bí ẩn đổi lối mỗi ngày. Cháu xuống thử {so} tầng xem.",
                                                     "Cháu lên rồi! Kể ông nghe dưới đó có gì."),
                   8, {"noi": "Hang động"}, i, vung="Mọi vùng", cung="Hầm ngục bí ẩn", lap_lai="ngay", gioi_han=1,
                   id_so=5200 + i, nhom="ham_nguc", dia_danh="Hang động", phut=20,
                   ghi_chu="HamNgucGame: tầng ngẫu nhiên, hạt giống theo ngày")
        ds.append(them(d))
    nhom_scale("hang_bi_an", ds)
    ds = []
    for i, (ten, kieu, mo, xong) in enumerate(NL.QUAY_LAI, 1):
        ts = "so=%d" % ({"nang_cap_cong": 5, "dang_nhap": 3}.get(kieu, 100) if kieu != "noi_chuyen" else 1)
        d = G.buoc("quay_lai", "QL-%02d" % i, 30, (kieu, ts, ten, mo, xong), [5, 21, 21, 8, 2, 9][i - 1],
                   {"noi": "Pallet"}, i, vung="Mọi vùng", cung="Quay lại", truoc="QL-%02d" % (i - 1) if i > 1 else "",
                   id_so=5300 + i, nhom="quay_lai", dia_danh="Pallet", phut=20,
                   dieu_kien_mo="vắng mặt ≥ 14 ngày", ghi_chu="Chỉ hiện khi vắng ≥ 14 ngày; EXP theo cấp người nhận")
        ds.append(them(d))
    nhom_scale("quay_lai", ds)

    # ---------------- 9. Su kien 52 tuan x 3 ----------------
    id_s = 5700
    for r in kho.su_kien:
        t = int(r["tuan"])
        tpl = NL.SU_KIEN[r["loai"]]
        vps = kho.sk_vp.get(t, [])
        vp = vps[0] if vps else {"vat_pham": "Kẹo", "anh_xa": "", "so_luong_toi_da_tuan": "10"}
        truoc_s = ""
        for bi, b in enumerate(tpl, 1):
            id_s += 1
            ma = "SK-%02d.%d" % (t, bi)
            thuong_sk = r["phan_thuong"] if r["phan_thuong"].strip() not in ("", "-", "—", "–") else "quà sự kiện"
            # font J2ME khong co "&": doi thanh "và" trong chu nguoi choi thay
            gt = {"ten": r["ten_nam1"].split(" (")[0].replace(" & ", " và "), "vat": vp["vat_pham"], "thuong": thuong_sk,
                  "mo_rieng": NL.SU_KIEN_MO[t - 1], "noi": "Pallet"}
            kieu, tss, ten, mo, xong = b
            ten_ngan = NL.SU_KIEN_TEN_NGAN[t - 1]
            m_ = re.match(r"\{ten\}: (.+)$", ten)
            if m_:
                ten = m_.group(1)[0].upper() + m_.group(1)[1:] + ": " + ten_ngan
            tss2 = thay(tss, gt)
            if kieu == "su_kien_vat_pham":
                tss2 = (tss2 + ";" if tss2 else "") + "vat=" + vp["vat_pham"]
            if kieu == "boss_the_gioi":
                tss2 = "q=#%d;so=1" % {9: 82, 20: 138, 28: 116, 36: 120, 48: 130}.get(t, 82)
            d = G.buoc("su_kien", ma, 5, (kieu, tss2, thay(ten, gt), thay(mo, gt), thay(xong, gt)),
                       int(tham_so(tss).get("npc", 21)) if kieu == "noi_chuyen" else 21, gt, t + bi,
                       vung="Mọi vùng", cung="Sự kiện tuần %d: %s" % (t, gt["ten"]), buoc=bi, truoc=truoc_s,
                       lap_lai="su_kien", id_so=id_s, nhom=r["loai"], dia_danh="Mọi làng", phut=[3, 45, 5][bi - 1],
                       ghi_chu="Mở theo lịch sự kiện tuần %d (tob_su_kien); thưởng trong ngân sách sự kiện tuần" % t)
            if kieu == "noi_chuyen" and bi == 1:
                d["npc_giao"] = int(tham_so(tss).get("npc", 21))
            d.update(exp=0, xu=0, luong_khoa=0, luong=0, dong_co_tuong_thich=0)
            tk = []
            if bi == len(tpl):
                if vp.get("anh_xa"):
                    tk.append("%sx%d" % (vp["anh_xa"], max(1, int(vp.get("so_luong_toi_da_tuan") or 10) // 4)))
                tk.append("trang_tri:%s" % r["phan_thuong"].split(",")[0].strip())
            d["thuong_khac"] = ";".join(tk)
            them(d)
            truoc_s = ma

    # ---------------- 10. Huyen thoai ----------------
    # 18/10/2026 (khong tran cap theo mua): bo khoa "cach buoc truoc 7 ngay" (engine chua tung chay) -> moi buoc can
    # cap cao hon buoc truoc HT_CAP_MOI_BUOC cap (khoa theo cap + chuoi `truoc` + vat pham nhiem vu cua buoc).
    HT_CAP_MOI_BUOC = 2
    id_h = 5500
    lk_ht = 0
    for (mh, ten_ht, L, npc, token, boss, noi, dh, bs) in NL.HUYEN_THOAI:
        truoc_h = ""
        for bi, b in enumerate(bs, 1):
            id_h += 1
            ma = "HT-%s.%d" % (mh, bi)
            d = G.buoc("huyen_thoai", ma, L, b, npc, {"noi": noi}, bi, vung="Mọi vùng",
                       cung="Huyền thoại: " + ten_ht, buoc=bi, truoc=truoc_h, id_so=id_h, nhom="huyen_thoai",
                       nhan_vat=ten_ht, dia_danh=noi, phut=20, ngay_mo=0 if L <= 91 else 365,
                       ghi_chu="Chuỗi nhiều bước: mỗi bước cần cấp cao hơn bước trước %d cấp" % HT_CAP_MOI_BUOC)
            d["cap_toi_thieu"] = min(100, L + HT_CAP_MOI_BUOC * (bi - 1))
            d.update(exp=0, xu=0, luong=0, dong_co_tuong_thich=0, luong_khoa=0)
            if bi == len(bs):
                d["luong_khoa"] = 5
                lk_ht += 5
                d["thuong_khac"] = "item:%dx1;danh_hieu:%s;truyen:Sử thi %s" % (kho.item_moi[token], dh, ten_ht)
            else:
                d["thuong_khac"] = "truyen:Sử thi %s - trang %d" % (ten_ht, bi)
            them(d)
            truoc_h = ma

    # ---------------- 11. An ----------------
    lk_an = 0
    for i, (ten, dkm, kieu, tss, L, mo, xong, dh) in enumerate(NL.AN, 1):
        d = G.buoc("an", "AN-%02d" % i, L, (kieu, tss, ten, mo, xong), 5 if kieu != "noi_chuyen" else 5,
                   {"noi": "Pallet"}, i, vung="Mọi vùng", cung="Nhiệm vụ ẩn", id_so=5400 + i, nhom="an",
                   dia_danh="?", phut=10, dieu_kien_mo=dkm, ghi_chu="Không hiện trong danh sách tới khi đủ điều kiện")
        d.update(exp=0, xu=0, luong=0, luong_khoa=2, an=1, dong_co_tuong_thich=0,
                 thuong_khac="danh_hieu:%s" % dh)
        lk_an += 2
        them(d)

    # ---------------- 12. Thanh tuu ----------------
    lk_tt, lg_tt = TC["thanh_tuu"][2] - lk_ht - lk_an, TC["thanh_tuu"][3]
    tt = []
    for nhom, kieu, bac in NL.THANH_TUU:
        for j, (ng, ten) in enumerate(bac):
            tt.append((nhom, kieu, ng, ten, j, len(bac)))
    w = [1 + j for (_, _, _, _, j, n) in tt]
    lks = chia_nguyen(lk_tt, w)
    lks = [min(x, NS["thanh_tuu"][2]) for x in lks]
    dinh = [i for i, x in enumerate(tt) if x[4] == x[5] - 1]
    lgs = [0] * len(tt)
    for i, v in zip(dinh, chia_nguyen(lg_tt, [1] * len(dinh))):
        lgs[i] = min(v, NS["thanh_tuu"][3])
    for i, (nhom, kieu, ng, ten, j, n) in enumerate(tt):
        d = dict(ma="A-%03d" % (i + 1), id_so=6000 + i + 1, loai="thanh_tuu", vung="Mọi vùng", cung="Thành tựu: " + nhom,
                 ten=ten, npc_giao="", dia_danh="", cap_toi_thieu=1, kieu_muc_tieu="thanh_tuu_" + kieu, so_luong=ng,
                 lap_lai="khong", exp=0, xu=0, luong_khoa=lks[i], luong=lgs[i], dong_co_tuong_thich=0, nhom="thanh_tuu",
                 ghi_chu="Danh hiệu (DanhHieu, tob_danh_hieu); không cộng chỉ số. Đếm: " + NC.THANH_TUU_CO_CHE[kieu],
                 mo_ta="%s: đạt mốc %s." % (nhom, so_dep(ng)), muc_tieu_text="%s đạt %s" % (nhom, so_dep(ng)),
                 goi_y="Tự ghi nhận khi đạt mốc.", hoi_thoai_bat_dau="", hoi_thoai_dang_lam="",
                 hoi_thoai_xong="Xatu: ...Sổ thành tựu đã ghi thêm: %s." % ten,
                 thuong_khac="danh_hieu:%s" % ten + (";trang_tri:Khung %s" % nhom if j == n - 1 else ""),
                 truoc=("A-%03d" % i) if j > 0 else "", phut=0)
        them(d)

    # ---------------- dong thuong + kiem ----------------
    ten_item = {}
    for b, c, o, iid, t in kho.trang_bi:
        ten_item[iid] = t
    for t, iid in kho.item_moi.items():
        ten_item[iid] = t
    for d in s.dong:
        if d.get("vat_nv") is not None:
            d.pop("vat_nv", None)
        p = []
        if d.get("exp"):
            p.append("%s EXP" % so_dep(d["exp"]))
        if d.get("xu"):
            p.append("%s xu" % so_dep(d["xu"]))
        if d.get("luong_khoa"):
            p.append("%s lượng khoá" % d["luong_khoa"])
        if d.get("luong"):
            p.append("%s lượng" % d["luong"])
        if d.get("vat_pham") not in ("", None):
            p.append(ten_item.get(int(d["vat_pham"]), "trang bị"))
        if d.get("huy_hieu"):
            p.append(d["huy_hieu"])
        for x in (d.get("thuong_khac") or "").split(";"):
            if not x:
                continue
            k_, v = x.split(":", 1)
            if k_ == "item":
                iid, sl = v.split("x")
                p.append("%s x%s" % (ten_item.get(int(iid), "vật phẩm"), sl))
            elif k_ == "thuoc":
                iid, sl = v.split("x") if "x" in v else (v, "1")
                p.append("vật phẩm sự kiện x%s" % sl)
            elif k_ == "danh_hieu":
                p.append("danh hiệu %s" % v)   # font J2ME khong co dau nhay kep
            elif k_ == "trang_tri":
                if v.strip().lower().startswith("danh hiệu"):
                    p.append(v[:1].lower() + v[1:])
                elif v.strip() not in ("", "-", "—", "–"):
                    p.append("trang trí: %s" % v)
            elif k_ == "truyen":
                p.append("trang truyện: %s" % v)
            elif k_ == "diem_bang":
                p.append("%s điểm bảng ngày" % v)
        if d.get("nhanh_lua_chon"):
            p.append("danh hiệu theo lựa chọn")
        if p:
            d["dong_thuong"] = "Thưởng: " + ", ".join(p)
        elif d.get("loai") == "su_kien":
            d["dong_thuong"] = "Thưởng: nhận ở bước cuối của sự kiện"
        else:
            d["dong_thuong"] = "Thưởng: lời cảm ơn"
    s.nv_kho = kho


# =====================================================================================
# DONG data_quest_moi (engine cu) - thay sinh_sql_lau_dai.dong_data_quest
# =====================================================================================

def sql_chuoi(x):
    return "'" + str(x).replace("\\", "\\\\").replace("'", "\\'") + "'"


def noi_dung_cu(hoi_thoai_bd):
    """'Ten: cau|Bạn: cau' -> '1Ten: cau>0cau' (data_quest.content <= 1000)."""
    ra = []
    for c in hoi_thoai_bd.split("|"):
        if c.startswith("Bạn: "):
            ra.append("0" + c[5:])
        else:
            ra.append("1" + c)
    s = ">".join(ra)
    while len(s) > 990 and ">" in s:
        s = s[:s.rindex(">")]
    return s[:990]


def vat_nv_hop_le(x, mac_dinh):
    """ItemQuest dung duoc: 10..50 (data_vat_pham_nhiem_vu, NoiDungLauDai.moRongVatPhamNhiemVu) hoac cac ten cu
    dung duoc trong jar (nv_chung.VAT_NV_CU)."""
    try:
        i = int(x)
    except (TypeError, ValueError):
        return mac_dinh
    if i in NC.VAT_NV_ID.values() or i in NC.VAT_NV_CU:
        return str(i)
    return mac_dinh


_VK_THEO_LOP = None
LOP_DONG = ["Charmander", "Bulbasaur", "Eevee", "Squirtle", "Pichu"]   # charClass 0..4 (GymLauDai)


def item_gift(vat):
    """data_quest.itemgift = "id lop0,..,id lop4,cong" (InfoQuestTemplate.addGift doc idItemgift[charClass] va
    idItemgift[5]). Mot id -> cung id cho moi lop; vu khi (type 3..7, theo dong) -> vu khi cung bac cua tung
    dong (docs/pokemon/noi_dung_trang_bi.csv). Truoc day ghi 1 so: lop > 0 loi AIOOBE, lop 0 mat cong."""
    global _VK_THEO_LOP
    if not vat or not str(vat).strip().isdigit():
        return "32000"
    i = int(vat)
    if _VK_THEO_LOP is None:
        _VK_THEO_LOP = {}
        bac_dong = {}
        for r in doc_csv("noi_dung_trang_bi.csv"):
            if r.get("o") == "vu_khi" and (r.get("id_item") or "").isdigit():
                bac_dong[(r["bac"], r["dong"])] = int(r["id_item"])
        for (bac, dong), iid in bac_dong.items():
            _VK_THEO_LOP[iid] = [bac_dong.get((bac, l), iid) for l in LOP_DONG]
    ds = _VK_THEO_LOP.get(i, [i] * 5)
    return ",".join(str(x) for x in ds) + ",0"


def dong_data_quest(d, mainsub):
    dp = (d.get("du_phong") or "3:%s" % (d.get("npc_giao") or 5)).split(":")
    typ = int(dp[0])
    npc = int(d["npc_giao"] or 5)
    iget = mon = nget = nmon = "32000"
    npcres = npc
    if typ == 0:
        mon, nmon = dp[1], dp[2]
    elif typ == 4:
        nmon = dp[1]
    elif typ == 2:
        # vat pham nhiem vu THAT cua thiet ke (muc_tieu_id = ItemQuest 10..50 / cu dung duoc), roi khi lam
        # nhiem vu (Monster -> checkDropItemQuest), nop luc tra (Char.OnQuest). Khac -> Long duoi Hong Ke (6).
        iget, mon, nget = vat_nv_hop_le(d.get("muc_tieu_id"), "6"), dp[1], dp[2]
    elif typ == 1:
        # giao_hang: hang trong tui nhiem vu = muc_tieu_id; dua_thu: La thu (4)
        iget = vat_nv_hop_le(d.get("muc_tieu_id"), "4") if d.get("kieu_muc_tieu") == "giao_hang" else "4"
        npcres = int(dp[1])
    elif typ == 3:
        npcres = 32000
    nv = (d.get("muc_tieu_text") or d["ten"])[:150]
    thuong = (d.get("dong_thuong") or "")[:120]
    info = "Nhiệm vụ:|%s|Địa điểm:|%s|NPC:|%s|Phần thưởng:|%s" % (nv, d["dia_danh"] or "-", d["ten_npc"] or "-",
                                                                   thuong.replace("Thưởng: ", ""))
    dd = d["dia_danh"] or ""
    sc = nv if (not dd or dd == "?" or dd in nv) else nv + " ở " + dd   # khong lap "o Pallet o Pallet"
    content = noi_dung_cu(d.get("hoi_thoai_bat_dau") or ("%s: %s" % (d["ten_npc"], nv)))
    xg = (d.get("hoi_thoai_xong") or "").split("|")
    res = "1" + xg[0][:480] if typ != 3 and xg[0] else "32000"
    fin = ("Nhiệm vụ hoàn thành|%s" % (xg[-1] if xg and xg[-1] else "Quay về nhận thưởng"))[:200]
    vat = item_gift(d.get("vat_pham"))
    gt = [d["id_so"], sql_chuoi(d["ten"][:45]), npc, npcres, max(1, int(d["cap_toi_thieu"])), mainsub,
          sql_chuoi(content), sql_chuoi(sc[:490]), sql_chuoi(res), sql_chuoi(iget),
          sql_chuoi(mon), typ, sql_chuoi(nget), sql_chuoi(nmon), d["xu"] or 0, d["exp"] or 0, "'32000'", "'32000'",
          sql_chuoi(vat), 5 if typ == 4 else 0, sql_chuoi(info[:200]), sql_chuoi(fin), 0, d["luong"] or 0,
          d["luong_khoa"] or 0]
    return "(" + ",".join(str(x) for x in gt) + ")"


# =====================================================================================
# FILE PHU
# =====================================================================================

COT_SQL_ND = ["ma", "loai", "nhom", "nhan_vat", "map_id", "toa_do", "dieu_kien", "du_phong", "mo_ta", "muc_tieu_text",
              "hoi_thoai_bat_dau", "hoi_thoai_dang_lam", "hoi_thoai_xong", "goi_y", "dong_thuong", "thuong_khac",
              "nhanh_lua_chon", "an", "dieu_kien_mo", "dap_an", "phut"]


def ghi_sql(dong):
    o = ["-- SINH RA boi tools/pokemon/sinh_nhiem_vu.py - KHONG SUA TAY.\n",
         "-- Noi dung nhiem vu (hoi thoai, goi y, nhanh lua chon, an, tham so muc tieu moi). Bang staging:\n",
         "-- engine GD2 doc theo ma (= data_quest_lau_dai.ma). Xem docs/pokemon/NHIEM_VU_THIET_KE.md.\n",
         "SET NAMES utf8;\n\n", "DROP TABLE IF EXISTS `data_nhiem_vu_noi_dung`;\n",
         "CREATE TABLE `data_nhiem_vu_noi_dung` (\n"]
    for c in COT_SQL_ND:
        typ = {"ma": "varchar(32)", "loai": "varchar(32)", "nhom": "varchar(32)", "map_id": "varchar(8)",
               "toa_do": "varchar(16)", "an": "tinyint", "phut": "int"}.get(c, "text")
        o.append("  `%s` %s NOT NULL%s,\n" % (c, typ, " DEFAULT 0" if typ in ("tinyint", "int") else ""))
    o.append("  PRIMARY KEY (`ma`)\n) DEFAULT CHARSET=utf8;\n")
    rows = []
    for d in dong:
        rows.append("(" + ",".join(str(int(d[c] or 0)) if c in ("an", "phut") else sql_chuoi(d.get(c, ""))
                                   for c in COT_SQL_ND) + ")")
    for i in range(0, len(rows), 100):
        o.append("INSERT INTO `data_nhiem_vu_noi_dung` VALUES\n" + ",\n".join(rows[i:i + 100]) + ";\n")
    o.append("\nDROP TABLE IF EXISTS `data_nhiem_vu_kieu`;\nCREATE TABLE `data_nhiem_vu_kieu` (\n  `ma` varchar(32) NOT NULL,\n"
             "  `ten` varchar(64) NOT NULL,\n  `co_che` varchar(255) NOT NULL,\n  `du_phong` varchar(4) NOT NULL,\n"
             "  `trang_thai` varchar(16) NOT NULL,\n  `so_dong` int NOT NULL,\n  PRIMARY KEY (`ma`)\n) DEFAULT CHARSET=utf8;\n")
    dem = {}
    for d in dong:
        dem[d["kieu_muc_tieu"]] = dem.get(d["kieu_muc_tieu"], 0) + 1
    o.append("INSERT INTO `data_nhiem_vu_kieu` VALUES\n" + ",\n".join(
        "(%s,%s,%s,%s,%s,%d)" % (sql_chuoi(k), sql_chuoi(v[0]), sql_chuoi(v[1]), sql_chuoi(v[2]), sql_chuoi(v[3]),
                                  dem.get(k, 0)) for k, v in sorted(NC.KIEU.items())) + ";\n")
    o.append("\nDROP TABLE IF EXISTS `data_vat_pham_nhiem_vu`;\nCREATE TABLE `data_vat_pham_nhiem_vu` (\n"
             "  `id` int NOT NULL,\n  `ten` varchar(64) NOT NULL,\n  PRIMARY KEY (`id`)\n) DEFAULT CHARSET=utf8;\n")
    o.append("INSERT INTO `data_vat_pham_nhiem_vu` VALUES\n" + ",\n".join(
        "(%d,%s)" % (i, sql_chuoi(t)) for t, i in sorted(NC.VAT_NV_ID.items(), key=lambda x: x[1])) + ";\n")
    open(RA_SQL, "w", encoding="utf-8", newline="\n").write("".join(o))


def lua_chon_tai_cap(dong):
    """So lua chon co y nghia o moi cap (cua so loc engine lv <= cap < lv+10)."""
    ra = []
    for L in range(1, 101):
        mot_lan = [d for d in dong if d["loai"] in ("huong_dan", "chinh", "phu", "pokedex", "huyen_thoai", "su_pham",
                                                    "quay_lai")
                   and d["lap_lai"] == "khong" and d["loai"] != "quay_lai"
                   and int(d["cap_toi_thieu"]) <= L < int(d["cap_toi_thieu"]) + 10
                   and (d["loai"] != "chinh" or int(d.get("ngay_mo") or 0) == 0 or L > 91)]
        phu = [d for d in mot_lan if d["loai"] == "phu"]
        chinh = [d for d in mot_lan if d["loai"] in ("chinh", "huong_dan")]
        dex = [d for d in mot_lan if d["loai"] == "pokedex"]
        ht = [d for d in mot_lan if d["loai"] == "huyen_thoai"]
        ngay = 5 if any(d["loai"] == "ngay" and int(d["cap_toi_thieu"]) <= L for d in dong) else 0
        tuan = len([d for d in dong if d["loai"] == "tuan" and int(d["cap_toi_thieu"]) <= L < int(d["cap_toi_thieu"]) + 30
                    and str(d["ma"]).startswith(("T-", "TV-", "TD-")) and (str(d["ma"]).endswith(".1") or not str(d["ma"]).startswith("TV-"))])
        sk = 3 if L >= 5 else 0
        ham = len([d for d in dong if d["loai"] == "hang_bi_an" and int(d["cap_toi_thieu"]) <= L])
        # chuoi phu: chi buoc dau moi chuoi la "nhan duoc ngay" (sau do mo dan)
        chuoi = len(set(d["cung"] for d in phu))
        so_goi = len([d for d in dong if d["loai"] == "phu" and int(d["cap_toi_thieu"]) <= L < int(d["cap_toi_thieu"]) + 10]) \
            + len([d for d in dong if d["loai"] == "doi"])
        y_nghia = min(1, len(chinh)) + chuoi + ngay + min(tuan, 2 + 4) + len(set(d["cung"] for d in dex) and dex[:5]) \
            + len(set(d["cung"] for d in ht)) + sk + min(ham, 1)
        ra.append({"cap": L, "chinh": len(chinh), "chuoi_phu": chuoi, "nhiem_vu_phu": len(phu), "ngay_giao": ngay,
                   "tuan": tuan, "pokedex": len(dex), "huyen_thoai": len(set(d["cung"] for d in ht)), "su_kien": sk,
                   "ham_nguc": min(ham, 1), "lua_chon_y_nghia": y_nghia, "goi_danh_sach_phu": so_goi})
    return ra


def nhip_do(dong):
    """Gio noi dung nhiem vu (1 lan) so voi gio cay theo dai cap."""
    dai = [(1, 15), (16, 30), (31, 45), (46, 60), (61, 75), (76, 90), (91, 100)]
    ra = []
    for a, b in dai:
        gio_cap = LD.GIO_TL[min(b + 1, 100)] - LD.GIO_TL[a] if b < 100 else LD.GIO_TL[100] - LD.GIO_TL[a]
        mot_lan = sum(int(d["phut"] or 0) for d in dong if a <= int(d["cap_toi_thieu"]) <= b
                      and d["lap_lai"] == "khong" and d["loai"] not in ("thanh_tuu", "an")) / 60.0
        so = len([d for d in dong if a <= int(d["cap_toi_thieu"]) <= b and d["loai"] != "thanh_tuu"])
        ra.append({"dai_cap": "%d-%d" % (a, b), "gio_len_cap_mo_phong": round(gio_cap, 1),
                   "gio_noi_dung_mot_lan": round(mot_lan, 1),
                   "phan_tram_nhiem_vu": int(round(100 * min(1.0, mot_lan / gio_cap))) if gio_cap > 0 else 100,
                   "so_nhiem_vu": so})
    return ra


def ghi_web(dong):
    chuong = []
    cur = None
    for d in dong:
        if d["loai"] != "chinh":
            continue
        if cur is None or cur["chuong"] != int(d["chuong"]):
            cur = {"chuong": int(d["chuong"]), "nam": int(d["nam"]), "vung": d["vung"], "cung": d["cung"].split(" ", 1)[1],
                   "dia_danh": d["dia_danh"], "cap": int(d["cap_toi_thieu"]), "tom_tat": d["mo_ta"], "buoc": [],
                   "huy_hieu": ""}
            chuong.append(cur)
        if d["kieu_muc_tieu"] != "dan_y":
            cur["buoc"].append(d["ten"])
        if d["huy_hieu"]:
            cur["huy_hieu"] = d["huy_hieu"]
    phu = {}
    for d in dong:
        if d["loai"] == "phu":
            k = d["cung"]
            p = phu.setdefault(k, {"noi": d["dia_danh"], "chuoi": k.split(": ", 1)[1], "nhan_vat": d["nhan_vat"],
                                   "cap_tu": int(d["cap_toi_thieu"]), "cap_den": 0, "so_buoc": 0})
            p["cap_den"] = max(p["cap_den"], int(d["cap_toi_thieu"]))
            p["so_buoc"] += 1
    dem = {}
    for d in dong:
        dem[d["loai"]] = dem.get(d["loai"], 0) + 1
    j = {
        "tong": len(dong), "theo_loai": dem,
        "cot_truyen": chuong,
        "chuoi_phu": list(phu.values()),
        "bang_ngay": sorted(set(d["ten"] for d in dong if d["loai"] == "ngay")),
        "tuan": sorted(set(d["cung"].replace("Chuỗi tuần: ", "") for d in dong if d["loai"] == "tuan" and d["nhom"] == "chuoi_vung")),
        "su_kien": [{"tuan": int(d["ma"][3:5]), "ten": d["cung"].split(": ", 1)[1], "buoc": d["ten"]}
                    for d in dong if d["loai"] == "su_kien"],
        "huyen_thoai": sorted(set(d["nhan_vat"] for d in dong if d["loai"] == "huyen_thoai")),
        "thanh_tuu": [{"nhom": d["cung"].replace("Thành tựu: ", ""), "ten": d["ten"], "moc": d["so_luong"]}
                      for d in dong if d["loai"] == "thanh_tuu"],
        "an": {"so_luong": dem.get("an", 0), "goi_y": "Có những nhiệm vụ chỉ hiện ra khi bạn làm điều đặc biệt..."},
    }
    os.makedirs(os.path.dirname(RA_WEB), exist_ok=True)
    open(RA_WEB, "w", encoding="utf-8", newline="\n").write(json.dumps(j, ensure_ascii=False, indent=1) + "\n")


def bang_md(cot, dong):
    s = "| " + " | ".join(cot) + " |\n|" + "---|" * len(cot) + "\n"
    for r in dong:
        s += "| " + " | ".join(str(r[c]) for c in cot) + " |\n"
    return s


def ghi_doc(dong, nhip, lc):
    if not os.path.exists(RA_DOC):
        return
    doc = open(RA_DOC, encoding="utf-8").read()
    dem = {}
    for d in dong:
        k = (d["loai"], d["nhom"])
        dem[k] = dem.get(k, 0) + 1
    t1 = bang_md(["loai", "nhom", "so_dong"], [{"loai": a, "nhom": b, "so_dong": n} for (a, b), n in sorted(dem.items())])
    t1 += "\n**Tổng: %d dòng.**\n" % len(dong)
    t2 = bang_md(list(nhip[0].keys()), nhip)
    mn = min(lc, key=lambda r: r["lua_chon_y_nghia"])
    mx = max(lc, key=lambda r: r["goi_danh_sach_phu"])
    t3 = bang_md(["cap", "chinh", "chuoi_phu", "nhiem_vu_phu", "ngay_giao", "tuan", "pokedex", "huyen_thoai", "su_kien",
                  "lua_chon_y_nghia", "goi_danh_sach_phu"], [r for r in lc if r["cap"] in (1, 5, 10, 15, 20, 30, 40, 50, 60, 70, 80, 85, 90, 95, 100)])
    t3 += "\nÍt lựa chọn nhất: cấp %d (%d). Gói danh sách phụ lớn nhất (trước khi engine cắt 20): cấp %d (%d ≤ 127).\n" % (
        mn["cap"], mn["lua_chon_y_nghia"], mx["cap"], mx["goi_danh_sach_phu"])
    dk = {}
    for d in dong:
        dk[d["kieu_muc_tieu"]] = dk.get(d["kieu_muc_tieu"], 0) + 1
    t4 = "| kiểu | tên | cơ chế phát hiện (server) | dự phòng engine cũ | trạng thái | số dòng |\n|---|---|---|---|---|---|\n"
    for k, v in sorted(NC.KIEU.items()):
        t4 += "| `%s` | %s | %s | %s | %s | %d |\n" % (k, v[0], v[1], v[2] if v[2] != "" else "—", v[3], dk.get(k, 0))
    tt = sum(n for k, n in dk.items() if k.startswith("thanh_tuu_"))
    t4 += "| `thanh_tuu_*` | Thành tựu | bảng tiến độ + bộ đếm (xem cột ghi_chu) | — | gd2 | %d |\n" % tt
    for ten, bang in (("DEM", t1), ("NHIP", t2), ("LUA_CHON", t3), ("KIEU", t4)):
        doc = re.sub(r"(<!-- SINH:%s -->\n).*?(<!-- /SINH:%s -->)" % (ten, ten), lambda m: m.group(1) + bang + m.group(2),
                     doc, flags=re.S)
    open(RA_DOC, "w", encoding="utf-8", newline="\n").write(doc)


def kiem_tu_cam(dong):
    import chu_pokemon as cp
    cam = cp.tu_dien_cam(cp.doc_bang())
    loi = []
    for d in dong:
        for c in ("ten", "mo_ta", "muc_tieu_text", "muc_tieu_ten", "hoi_thoai_bat_dau", "hoi_thoai_dang_lam",
                  "hoi_thoai_xong", "goi_y", "dong_thuong", "thuong_khac", "nhanh_lua_chon", "cung", "dieu_kien_mo"):
            t = cam.tim(str(d.get(c) or ""))
            if t:
                loi.append("%s.%s: %s" % (d["ma"], c, t))
    return loi


def sau_khi_ghi(dong):
    """Goi tu sinh_sql_lau_dai.main sau khi ghi CSV/SQL chinh."""
    ghi_sql(dong)
    lc = lua_chon_tai_cap(dong)
    nhip = nhip_do(dong)
    with open(RA_NHIP, "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(lc[0].keys()), lineterminator="\n")
        w.writeheader()
        w.writerows(lc)
    ghi_web(dong)
    ghi_doc(dong, nhip, lc)
    loi = kiem_tu_cam(dong)
    if loi:
        print("TU CAM:", len(loi), loi[:10])
    dem = {}
    for d in dong:
        dem[d["loai"]] = dem.get(d["loai"], 0) + 1
    print("nhiem vu moi:", len(dong), dem)
    print("lua chon it nhat:", min(r["lua_chon_y_nghia"] for r in lc),
          "goi phu lon nhat:", max(r["goi_danh_sach_phu"] for r in lc))
    for r in nhip:
        print("  nhip", r)


if __name__ == "__main__":
    import sinh_sql_lau_dai
    sinh_sql_lau_dai.main()
