#!/usr/bin/env python3
"""
Sinh du lieu ~1.025 loai Pokemon cho HTBB tu MOT nguon (he_thong_day_du.md muc 3.1-3.4, 4.2).

Dau vao (chi doc, du lieu tai ve la KHONG tin cay -> chay bang python3 -I, chi parse CSV/JSON/van ban):
  tools/pokemon/nguon_loai/pokeapi-<commit>/*.csv     PokeAPI (BSD-3-Clause), ghim commit
  tools/pokemon/nguon_loai/showdown-<commit>/pokedex.ts  Pokemon Showdown (MIT), doi chieu cheo
  tools/pokemon/nguon_loai/pmdcollab-<commit>/tracker.json  PMDCollab (trang thai sprite / chan dung)
  tools/pokemon/loai/loai_duyet.json                  CHO DUY NHAT sua tay (bac, vung, chieu, vat pham)
  du_lieu/kiem_ke_sprite_loai.csv                     kiem ke sprite (tools/pokemon/loai/kiem_ke_sprite_loai.py)

Ra (commit; KHONG sua tay - sua dau vao roi chay lai):
  sql/nang_cap.sql, khoi "-- >>> them_loai_day_du.sql"   NGUON SU THAT trien khai (idempotent, MariaDB 10.3+, utf8)
  du_lieu/loai/*.csv              ban xem (loai, tien hoa, chieu, chieu loai, vung)
  du_lieu/loai/doi_chieu.txt      bao cao doi chieu PokeAPI <-> Showdown
  tools/pokemon/nguon_loai/nguon.txt  URL + commit + SHA-256 tung tep nguon

Chay o goc repo:
  python3 -I tools/pokemon/loai/sinh_loai.py           # sinh
  python3 -I tools/pokemon/loai/sinh_loai.py --tai     # tai nguon con thieu (mang) roi sinh
Bo sinh CHI chay mot lan khi doi du lieu; quy trinh deploy chi dung tep SQL (luat chu game).
Ma thoat 1 neu du lieu khong hop le (chuoi tien hoa, he, chieu thieu, lech Showdown ngoai danh sach cho phep).
"""
import csv
import hashlib
import json
import os
import re
import sys
import urllib.request
from collections import defaultdict

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
DUYET = os.path.join(GOC, "tools", "pokemon", "loai", "loai_duyet.json")
NGUON_DIR = os.path.join(GOC, "tools", "pokemon", "nguon_loai")
RA_SQL = os.path.join(GOC, "sql", "nang_cap.sql")
MOC_SQL = "-- >>> them_loai_day_du.sql\n"  # mot tep SQL (sql/README.md): khoi nay trong nang_cap.sql phan 11
RA_CSV = os.path.join(GOC, "du_lieu", "loai")
KIEM_KE = os.path.join(GOC, "du_lieu", "kiem_ke_sprite_loai.csv")

TEP_POKEAPI = ["pokemon_species", "pokemon_species_names", "pokemon", "pokemon_types", "pokemon_stats",
               "pokemon_evolution", "evolution_triggers", "items", "pokemon_egg_groups", "egg_groups",
               "pokemon_habitats", "pokemon_moves", "moves", "types"]

SO_LOAI = 1025

# PokeAPI type id -> HePokemon (0 Thuong,1 Lua,2 Nuoc,3 Dien,4 Co,5 Bang,6 Giac dau,7 Doc,8 Dat,9 Bay,10 Sieu linh,
# 11 Bo,12 Da,13 Ma,14 Rong,15 Bong toi,16 Thep,17 Tien)
HE_TU_POKEAPI = {1: 0, 10: 1, 11: 2, 13: 3, 12: 4, 15: 5, 2: 6, 4: 7, 5: 8, 3: 9, 14: 10, 7: 11, 6: 12, 8: 13,
                 16: 14, 17: 15, 9: 16, 18: 17}
TEN_HE = ["Thường", "Lửa", "Nước", "Điện", "Cỏ", "Băng", "Giác đấu", "Độc", "Đất", "Bay", "Siêu linh", "Bọ", "Đá",
          "Ma", "Rồng", "Bóng tối", "Thép", "Tiên"]
HE_EN = ["Normal", "Fire", "Water", "Electric", "Grass", "Ice", "Fighting", "Poison", "Ground", "Flying", "Psychic",
         "Bug", "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"]

# Bang khac he chuan (tan cong -> {phong thu: he so}), chi ghi khac 1.
_SE = {0: [], 1: [4, 5, 11, 16], 2: [1, 8, 12], 3: [2, 9], 4: [2, 8, 12], 5: [4, 8, 9, 14], 6: [0, 5, 12, 15, 16],
       7: [4, 17], 8: [1, 3, 7, 12, 16], 9: [4, 6, 11], 10: [6, 7], 11: [4, 10, 15], 12: [1, 5, 9, 11], 13: [10, 13],
       14: [14], 15: [10, 13], 16: [5, 12, 17], 17: [6, 14, 15]}
_NVE = {0: [12, 16], 1: [1, 2, 12, 14], 2: [2, 4, 14], 3: [3, 4, 14], 4: [1, 4, 7, 9, 11, 14, 16], 5: [1, 2, 5, 16],
        6: [7, 9, 10, 11, 17], 7: [7, 8, 12, 13], 8: [4, 11], 9: [3, 12, 16], 10: [10, 16], 11: [1, 6, 7, 9, 13, 16, 17],
        12: [6, 8, 16], 13: [15], 14: [16], 15: [6, 15, 17], 16: [1, 2, 3, 16], 17: [1, 7, 16]}
_IMM = {0: [13], 3: [8], 6: [13], 7: [16], 8: [9], 10: [15], 13: [0], 14: [17]}


def he_so(tc, pt):
    if pt in _IMM.get(tc, []):
        return 0.0
    if pt in _SE[tc]:
        return 2.0
    if pt in _NVE[tc]:
        return 0.5
    return 1.0


def he_so_loai(tc, he1, he2):
    v = he_so(tc, he1)
    if he2 >= 0:
        v *= he_so(tc, he2)
    return v


# ------------------------------------------------------------------ nguon

def thu_muc(ten, commit):
    return os.path.join(NGUON_DIR, "%s-%s" % (ten, commit))


def tai(url, dich):
    req = urllib.request.Request(url, headers={"User-Agent": "htbb-sinh-loai"})
    with urllib.request.urlopen(req, timeout=120) as r:
        data = r.read()
    with open(dich, "wb") as f:
        f.write(data)


def dam_bao_nguon(cfg, cho_tai):
    pa = cfg["nguon"]["pokeapi"]["commit"]
    sd = cfg["nguon"]["showdown"]["commit"]
    pm = cfg["nguon"]["pmdcollab"]["commit"]
    can = []
    d = thu_muc("pokeapi", pa)
    can.append((os.path.join(d, "LICENSE.md"), "https://raw.githubusercontent.com/PokeAPI/pokeapi/%s/LICENSE.md" % pa))
    for t in TEP_POKEAPI:
        can.append((os.path.join(d, t + ".csv"),
                    "https://raw.githubusercontent.com/PokeAPI/pokeapi/%s/data/v2/csv/%s.csv" % (pa, t)))
    d = thu_muc("showdown", sd)
    for t in ("LICENSE", "data/pokedex.ts"):
        can.append((os.path.join(d, os.path.basename(t)),
                    "https://raw.githubusercontent.com/smogon/pokemon-showdown/%s/%s" % (sd, t)))
    d = thu_muc("pmdcollab", pm)
    can.append((os.path.join(d, "tracker.json"),
                "https://raw.githubusercontent.com/PMDCollab/SpriteCollab/%s/tracker.json" % pm))
    thieu = [c for c in can if not os.path.isfile(c[0])]
    if thieu:
        if not cho_tai:
            print("Thieu %d tep nguon (vd %s). Chay lai voi --tai." % (len(thieu), os.path.relpath(thieu[0][0], GOC)))
            sys.exit(2)
        for dich, url in thieu:
            os.makedirs(os.path.dirname(dich), exist_ok=True)
            print("tai", url)
            tai(url, dich)
    dong = ["# Nguon du lieu loai (sinh boi tools/pokemon/loai/sinh_loai.py). Tep CSV/TS/JSON khong commit (.gitignore);",
            "# tai lai dung commit bang --tai, SHA-256 phai khop dong duoi.", ""]
    for dich, url in can:
        h = hashlib.sha256(open(dich, "rb").read()).hexdigest()
        dong.append("%s  %s  %s" % (h, os.path.relpath(dich, NGUON_DIR), url))
    nguon_txt = os.path.join(NGUON_DIR, "nguon.txt")
    cu = open(nguon_txt, encoding="utf8").read() if os.path.isfile(nguon_txt) else None
    moi = "\n".join(dong) + "\n"
    if cu is not None:
        # tep nguon da ghim: SHA-256 phai khop (tai lai ra noi dung khac = commit sai / bi sua)
        ma_cu = {l.split()[1]: l.split()[0] for l in cu.splitlines() if l and not l.startswith("#")}
        for l in dong[3:]:
            h, p = l.split()[0], l.split()[1]
            if p in ma_cu and ma_cu[p] != h:
                print("LOI: SHA-256 cua %s khac nguon.txt (du lieu nguon bi doi)" % p)
                sys.exit(1)
    with open(nguon_txt, "w", encoding="utf8") as f:
        f.write(moi)
    return thu_muc("pokeapi", pa), thu_muc("showdown", sd), thu_muc("pmdcollab", pm)


def doc_csv(d, ten):
    with open(os.path.join(d, ten + ".csv"), encoding="utf8", newline="") as f:
        return list(csv.DictReader(f))


def so(v, mac=None):
    v = (v or "").strip()
    return int(v) if v else mac


# ------------------------------------------------------------------ Showdown (doi chieu)

def doc_showdown(tep):
    """num -> {name, types, stats, prevo, evoLevel, evoType, evoItem, eggGroups} (chi dang goc, khong forme)."""
    s = open(tep, encoding="utf8").read()
    kq = {}
    for m in re.finditer(r"\n\t([a-z0-9]+): \{\n(.*?)\n\t\},", s, re.S):
        body = m.group(2)
        num = re.search(r"\tnum: (-?\d+),", body)
        if not num or "\t\tforme:" in body or "baseSpecies:" in body:
            continue
        n = int(num.group(1))
        if n < 1 or n > SO_LOAI or n in kq:
            continue
        types = re.search(r"types: \[([^\]]*)\]", body)
        bs = re.search(r"baseStats: \{([^}]*)\}", body)
        st = dict((k, int(v)) for k, v in re.findall(r"(\w+): (\d+)", bs.group(1))) if bs else {}
        prevo = re.search(r'prevo: "([^"]+)"', body)
        egg = re.search(r"eggGroups: \[([^\]]*)\]", body)
        kq[n] = {
            "name": re.search(r'name: "([^"]+)"', body).group(1),
            "types": re.findall(r'"([^"]+)"', types.group(1)) if types else [],
            "stats": [st.get(k, 0) for k in ("hp", "atk", "def", "spa", "spd", "spe")],
            "prevo": prevo.group(1) if prevo else "",
            "egg": re.findall(r'"([^"]+)"', egg.group(1)) if egg else [],
        }
    return kq


# ------------------------------------------------------------------ sinh

def sql_chuoi(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int,)):
        return str(v)
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"


def main():
    cfg = json.load(open(DUYET, encoding="utf8"))
    d_pa, d_sd, d_pm = dam_bao_nguon(cfg, "--tai" in sys.argv)
    loi = []

    species = {int(r["id"]): r for r in doc_csv(d_pa, "pokemon_species") if int(r["id"]) <= SO_LOAI}
    if len(species) != SO_LOAI:
        loi.append("PokeAPI co %d loai, can %d" % (len(species), SO_LOAI))
    ten_en = {}
    for r in doc_csv(d_pa, "pokemon_species_names"):
        if r["local_language_id"] == "9" and int(r["pokemon_species_id"]) <= SO_LOAI:
            ten_en[int(r["pokemon_species_id"])] = r["name"]
    poke_mac_dinh = {}
    for r in doc_csv(d_pa, "pokemon"):
        if r["is_default"] == "1" and int(r["species_id"]) <= SO_LOAI:
            poke_mac_dinh[int(r["species_id"])] = r
    he = defaultdict(dict)
    for r in doc_csv(d_pa, "pokemon_types"):
        he[int(r["pokemon_id"])][int(r["slot"])] = HE_TU_POKEAPI[int(r["type_id"])]
    stat = defaultdict(dict)
    for r in doc_csv(d_pa, "pokemon_stats"):
        stat[int(r["pokemon_id"])][int(r["stat_id"])] = int(r["base_stat"])
    trung = defaultdict(list)
    for r in doc_csv(d_pa, "pokemon_egg_groups"):
        trung[int(r["species_id"])].append(int(r["egg_group_id"]))
    ten_trung = {int(r["id"]): r["identifier"] for r in doc_csv(d_pa, "egg_groups")}
    moi_truong_pa = {int(r["id"]): r["identifier"] for r in doc_csv(d_pa, "pokemon_habitats")}
    item_id = {int(r["id"]): r["identifier"] for r in doc_csv(d_pa, "items")}
    trigger = {int(r["id"]): r["identifier"] for r in doc_csv(d_pa, "evolution_triggers")}
    moves = {int(r["id"]): r for r in doc_csv(d_pa, "moves")}
    move_id_tu_ten = {r["identifier"]: int(r["id"]) for r in moves.values()}

    # ---- loai co ban
    L = {}
    for dex in range(1, SO_LOAI + 1):
        s = species[dex]
        p = poke_mac_dinh[dex]
        pid = int(p["id"])
        t = he[pid]
        st = stat[pid]
        L[dex] = {
            "dex": dex, "ma": s["identifier"], "ten": ten_en.get(dex, s["identifier"].title()).replace("\u2019", "'"),
            "the_he": int(s["generation_id"]), "he1": t.get(1, 0), "he2": t.get(2, -1),
            "cs": [st[1], st[2], st[3], st[4], st[5], st[6]],
            "tu": so(s["evolves_from_species_id"], 0), "ho": int(s["evolution_chain_id"]),
            "baby": s["is_baby"] == "1", "legend": s["is_legendary"] == "1", "myth": s["is_mythical"] == "1",
            "moi_truong_pa": moi_truong_pa.get(so(s["habitat_id"], 0), ""),
            "gioi_tinh": int(s["gender_rate"]), "cao": int(p["height"]), "nang": int(p["weight"]),
            "trung": sorted(trung.get(dex, [15]))[:2],
        }
        L[dex]["bst"] = sum(L[dex]["cs"])
    con = defaultdict(list)
    for dex, l in L.items():
        if l["tu"]:
            con[l["tu"]].append(dex)
    # giai doan (bo qua baby khi dem), goc ho
    for dex, l in L.items():
        g = 1
        cur = l["tu"]
        goc = dex
        while cur:
            if not L[cur]["baby"]:
                g += 1
            goc = cur
            cur = L[cur]["tu"]
        l["giai_doan"] = 0 if l["baby"] else g
        l["dex_goc"] = goc
    ho_thanh_vien = defaultdict(list)
    for dex, l in L.items():
        ho_thanh_vien[l["dex_goc"]].append(dex)
    for dex, l in L.items():
        l["co_tien_hoa"] = bool(con.get(dex))
        # "loai don" = ho chi co mot loai KHONG tinh baby (Snorlax, Lapras... 2.3)
        l["don"] = sum(1 for x in ho_thanh_vien[l["dex_goc"]] if not L[x]["baby"]) == 1
        l["cuoi"] = not l["co_tien_hoa"]

    # ---- nhom + bac
    b = cfg["bac"]
    ub, nl = set(b["ub"]), set(b["nghich_ly"])
    gh_goc, gh_cuoi = set(b["gia_huyen_thoai_goc"]), set(b["gia_huyen_thoai_cuoi"])
    hoa_thach, khoi_dau, ho_t4 = set(b["hoa_thach_goc"]), set(b["khoi_dau_goc"]), set(b["ho_t4"])
    for dex, l in L.items():
        l["nhom"] = 3 if dex in ub else 4 if dex in nl else 2 if l["myth"] else 1 if l["legend"] else 0
        g = l["dex_goc"]
        bst = l["bst"]
        if l["nhom"]:
            bac = 6
        elif dex in gh_cuoi:
            bac = 5
        elif g in gh_goc or g in hoa_thach or g in khoi_dau or g in ho_t4:
            bac = 4
        elif l["don"]:
            bac = 4 if bst >= 535 else 3 if bst >= 450 else 2 if bst >= 350 else 1
        elif l["giai_doan"] >= 3:
            bac = 3
        elif l["giai_doan"] == 2:
            bac = 2
        elif l["giai_doan"] == 0:
            bac = 2
        else:
            bac = 3 if bst >= 450 else 2 if bst >= 350 else 1
        bac = int(b["ghi_de"].get(str(dex), bac))
        l["bac"] = bac
        l["goc_bat"] = int(b["goc_bat_phan_nghin"][str(bac)])
        l["bat_duoc"] = 0 if bac == 6 else 1
        l["chi_su_kien"] = 1 if bac >= 5 else 0

    # ---- dot mo + sprite
    dm = cfg["dot_mo"]
    sprite = {}
    if os.path.isfile(KIEM_KE):
        with open(KIEM_KE, encoding="utf8", newline="") as f:
            for r in csv.DictReader(f):
                sprite[int(r["dex"])] = r
    else:
        loi.append("thieu %s (chay tools/pokemon/loai/kiem_ke_sprite_loai.py)" % os.path.relpath(KIEM_KE, GOC))
    for dex, l in L.items():
        l["dot_mo"] = int(dm["the_he"][str(l["the_he"])])
        r = sprite.get(dex, {})
        pmd = int(r.get("pmd_sprite") or 0)
        walk = int(r.get("pmd_walk") or 0)
        trong_jar = int(r.get("trong_jar") or 0)
        goi = int(r.get("goi_server") or 0)
        l["sprite"] = {
            "trang_thai": 2 if (pmd >= 2 and walk) else 1 if (walk or trong_jar or goi) else 0,
            "chan_dung": int(r.get("pmd_chan_dung") or 0),
            "trong_jar": trong_jar, "goi_server": goi, "kich_thuoc": int(r.get("kich_thuoc_goi") or 0),
            "ghi_cong": (r.get("ghi_cong") or "")[:250],
        }
        co_hinh = trong_jar or goi
        l["bat"] = 1 if (l["dot_mo"] in dm["dang_mo"] and co_hinh) else 0
        if l["dot_mo"] in dm["dang_mo"] and not co_hinh:
            loi.append("loai %d (%s) thuoc dot dang mo nhung khong co hinh (jar / goi may chu)" % (dex, l["ten"]))

    # ---- moi truong
    v = cfg["vung"]
    for dex, l in L.items():
        mt = l["moi_truong_pa"]
        ma, vung = v["moi_truong"].get(mt, ["", []]) if mt else ["", []]
        if not vung:
            vung = v["theo_he"][str(l["he1"])]
            if not ma:
                ma = "theo_he"
        if v["he_bang"] in (l["he1"], l["he2"]) and v["vung_tuyet"] not in vung:
            vung = [v["vung_tuyet"]] + list(vung)
        l["moi_truong"] = ma
        l["vung"] = list(vung)

    # ---- tien hoa
    vp = cfg["vat_pham_tien_hoa"]
    keo = cfg["keo_tien_hoa"]
    cap_khac = cfg["cap_khong_theo_cap"]
    ev_rows = defaultdict(list)
    for r in doc_csv(d_pa, "pokemon_evolution"):
        e = int(r["evolved_species_id"])
        if e <= SO_LOAI:
            ev_rows[e].append(r)
    TH = []
    for dex, l in L.items():
        if not l["tu"]:
            continue
        rows = ev_rows.get(dex, [])
        if not rows:
            loi.append("tien hoa %d <- %d khong co dong pokemon_evolution" % (dex, l["tu"]))
            continue

        # bo dong cua dang vung (Alola / Galar / Hisui / Paldea: form >= 10000) neu con dong dang goc
        goc_rows = [r for r in rows if so(r.get("evolved_pokemon_form_id"), 0) < 10000
                    and so(r.get("required_pokemon_form_id"), 0) < 10000 and not (r.get("region_id") or "").strip()]
        if goc_rows:
            rows = goc_rows

        def diem(r):
            t = trigger.get(so(r["evolution_trigger_id"], 0), "")
            return (1 if r.get("is_default") == "1" else 0,
                    {"use-item": 5, "level-up": 4, "trade": 3}.get(t, 1),
                    -so(r["id"], 0))
        r = max(rows, key=diem)
        t = trigger.get(so(r["evolution_trigger_id"], 0), "unclassified")
        lv = so(r["minimum_level"])
        it = item_id.get(so(r["trigger_item_id"], 0), "")
        held = item_id.get(so(r["held_item_id"], 0), "")
        tod = (r.get("time_of_day") or "").strip()
        happy = so(r["minimum_happiness"]) or so(r["minimum_affection"])
        ngay_dem = 1 if tod == "day" else 2 if tod in ("night", "dusk") else 0
        giai = l["giai_doan"] if l["giai_doan"] > 0 else 2
        cap_mac = int(cap_khac[str(min(3, max(2, giai)))])
        k_goc = keo["giai_1"] if giai <= 2 else keo["giai_2"]
        if l["bac"] >= 4:
            k_goc *= keo["nhan_bac_tu_4"]
        ten_vp, id_vp, ma_vp = "", 0, ""
        mo = []
        if t == "level-up" and lv and not happy and not held and not r.get("known_move_id") and not r.get("known_move_type_id") \
                and not r.get("location_id") and not r.get("relative_physical_stats") and not r.get("party_species_id") \
                and not r.get("party_type_id") and r.get("needs_overworld_rain") != "1" and r.get("turn_upside_down") != "1" \
                and not r.get("gender_id"):
            kieu, cap, co_che = "cap", lv, "cap"
            mo.append("Cấp %d" % lv)
        elif t == "use-item" and it:
            ma_vp = it
            ten_vp, id_vp = vp.get(it, ["Vật phẩm tiến hoá (%s)" % it, 0])
            kieu, cap = "da", cap_mac
            co_che = "vat_pham" if id_vp else "keo"
            mo.append("Dùng %s" % ten_vp)
        elif t == "trade":
            kieu, cap = "trao_doi", cap_mac
            co_che = "trao_doi"
            if held:
                ma_vp = held
                ten_vp, id_vp = vp.get(held, ["Vật phẩm tiến hoá (%s)" % held, 0])
                mo.append("Trao đổi khi giữ %s" % ten_vp)
            else:
                mo.append("Trao đổi")
            mo.append("hoặc dùng Dây Liên Kết / Kẹo sau khi được trao đổi")
        elif happy:
            kieu, cap, co_che = "than_thiet", lv or cap_mac, "than_thiet"
            mo.append("Thân thiết %d" % happy)
        elif held:
            ma_vp = held
            ten_vp, id_vp = vp.get(held, ["Vật phẩm tiến hoá (%s)" % held, 0])
            kieu, cap = "vat_pham", lv or cap_mac
            co_che = "vat_pham" if id_vp else "keo"
            mo.append("Lên cấp khi giữ %s" % ten_vp)
        else:
            kieu, cap, co_che = "khac", lv or cap_mac, "keo"
            mota = {"known_move_id": "biết chiêu %s", "known_move_type_id": "biết chiêu hệ", "location_id": "ở địa điểm đặc biệt",
                    "relative_physical_stats": "so sánh Tấn công / Phòng thủ", "party_species_id": "có loài khác trong đội",
                    "gender_id": "theo giới tính", "needs_overworld_rain": "trời mưa"}
            for kk, txt in mota.items():
                val = (r.get(kk) or "").strip()
                if kk == "relative_physical_stats" and val:
                    mo.append({"1": "Tấn công lớn hơn Phòng thủ", "-1": "Tấn công nhỏ hơn Phòng thủ",
                               "0": "Tấn công bằng Phòng thủ"}.get(val, txt))
                    continue
                if val and val != "0":
                    if kk == "known_move_id":
                        txt = txt % moves.get(int(val), {}).get("identifier", val)
                    mo.append(txt)
            if not mo:
                mo.append("điều kiện đặc biệt (%s)" % t)
            mo = ["Cách gốc: " + ", ".join(mo)]
        if ngay_dem:
            mo.append("ban ngày" if ngay_dem == 1 else "ban đêm")
        if co_che == "keo" and kieu != "khac":
            mo.append("HTBB: chưa có vật phẩm, dùng Kẹo")
        if co_che == "keo":
            k_goc *= keo["khong_co_vat_pham"] if kieu != "khac" else 1
        TH.append({"tu": l["tu"], "sang": dex, "kieu": kieu, "co_che": co_che, "cap": min(100, max(1, cap)),
                   "ma_vp": ma_vp, "ten_vp": ten_vp, "id_vp": id_vp, "ngay_dem": ngay_dem, "keo": k_goc,
                   "than_thiet": happy or 0, "dieu_kien": "; ".join(mo)[:160], "goc_kieu": t})

    # ---- kiem chuoi tien hoa
    for dex, l in L.items():
        seen = set()
        cur = dex
        while cur:
            if cur in seen:
                loi.append("chuoi tien hoa co vong o %d" % dex)
                break
            seen.add(cur)
            cur = L[cur]["tu"]
    th_dich = set((x["tu"], x["sang"]) for x in TH)
    for dex, l in L.items():
        if l["tu"] and (l["tu"], dex) not in th_dich:
            loi.append("thieu dong tien hoa %d -> %d" % (l["tu"], dex))

    # ---- chieu
    cc = cfg["chieu"]
    VAI = cc["vai_tro"]
    CHIEU = {}
    ten_da_dung = {}
    for hs, ds in cc["chung"].items():
        h = int(hs)
        for vai, (ten, goc_ids, hu) in ds.items():
            ma = "%s.%s" % (HE_EN[h].lower(), vai)
            if ten in ten_da_dung:
                loi.append("trung ten chieu '%s' (%s, %s)" % (ten, ma, ten_da_dung[ten]))
            ten_da_dung[ten] = ma
            for g in goc_ids:
                if g not in move_id_tu_ten:
                    loi.append("chieu %s: id PokeAPI '%s' khong co" % (ma, g))
            vt = VAI[vai]
            kieu = vt["kieu"] or ("vat_ly" if vai == "thuong" and h in (0, 6, 9, 11, 12, 13, 15, 16, 8) else "dac_biet")
            CHIEU[ma] = {"ma": ma, "ten": ten, "he": h, "kieu": kieu, "vai_tro": vai, "pt": vt["pt"], "so_don": vt["so_don"],
                         "hoi_ms": vt["hoi_ms"], "pp": vt["pp"],
                         "trang_thai": cc["trang_thai_he"][h] if vai in ("khong_che", "toi_thuong", "tan_cong") else "",
                         "ti_le": 1000 if vai == "khong_che" else 100 if vai in ("tan_cong", "toi_thuong") else 0,
                         "hinh": vt["hinh"], "tam": vt["tam"], "rong": vt["rong"], "hu": hu,
                         "mau": cc["mau_he"][h], "doi_mau": 0, "dac_trung_ho": None, "goc": goc_ids}
    hu_ban_dia = set()
    with open(os.path.join(GOC, "du_lieu", "bang_hieu_ung.csv"), encoding="utf8", newline="") as f:
        for r in csv.DictReader(f):
            hu_ban_dia.add(r["file_client"].replace(".bytes", ""))
    # hieu ung goc co mau rieng (lua, nuoc...) dung cho he khac thi doi bang mau luc dong goi
    mau_goc_hu = {"hu_ember": 1, "hu_fire_fang_fang": 1, "hu_fire_spin_front": 1, "hu_flamethrower": 1, "hu_fire_blast": 1,
                  "hu_hydro_pump_rse": 2, "hu_bubbles_blue": 2, "hu_brine_splashes": 2, "hu_aqua_tail_splash": 2,
                  "hu_water_column_ranger": 2, "hu_hydro_cannon": 2, "hu_withdraw": 2, "hu_spark": 3, "hu_shock_wave": 3,
                  "hu_light_ball_yellow": 3, "hu_discharge_hit": 3, "hu_discharge": 3, "hu_charge_up": 3,
                  "hu_thunder_fang_fang": 3, "hu_charge_beam_glow": 3, "hu_vine_whip": 4, "hu_razor_leaf": 4,
                  "hu_seed_bomb_blast": 4, "hu_leaf_storm_hit": 4, "hu_charge_beam_hit": 4, "hu_stat_green_ring": 4,
                  "hu_mud_bomb_ball": 7}
    dt = {k: x for k, x in cc["dac_trung"].items() if not k.startswith("_")}
    for goc_s, (ma, ten, h, kieu, hu, goc_id) in dt.items():
        if not ma.startswith("dt."):
            continue
        if ten in ten_da_dung:
            loi.append("trung ten chieu dac trung '%s'" % ten)
        ten_da_dung[ten] = ma
        vt = VAI["dac_trung"]
        CHIEU[ma] = {"ma": ma, "ten": ten, "he": h, "kieu": kieu, "vai_tro": "dac_trung", "pt": vt["pt"], "so_don": 1,
                     "hoi_ms": vt["hoi_ms"], "pp": vt["pp"], "trang_thai": cc["trang_thai_he"][h], "ti_le": 150,
                     "hinh": vt["hinh"], "tam": vt["tam"], "rong": vt["rong"], "hu": hu, "mau": cc["mau_he"][h],
                     "doi_mau": 0, "dac_trung_ho": int(goc_s), "goc": [goc_id]}
    for c in CHIEU.values():
        if c["hu"] not in hu_ban_dia:
            loi.append("chieu %s: hieu ung %s khong co trong du_lieu/bang_hieu_ung.csv" % (c["ma"], c["hu"]))
        g = mau_goc_hu.get(c["hu"])
        c["doi_mau"] = 1 if (g is None and c["he"] != 0) or (g is not None and g != c["he"]) else 0

    # learnset PokeAPI: dex -> tap id chieu (moi phien ban, moi cach hoc) cua pokemon mac dinh
    pid_dex = {int(p["id"]): d for d, p in poke_mac_dinh.items()}
    hoc = defaultdict(set)
    with open(os.path.join(d_pa, "pokemon_moves.csv"), encoding="utf8", newline="") as f:
        for r in csv.DictReader(f):
            dd = pid_dex.get(int(r["pokemon_id"]))
            if dd:
                hoc[dd].add(int(r["move_id"]))
    he_hoc = defaultdict(set)  # dex -> he cua chieu gay sat thuong loai hoc duoc
    for dd, ids in hoc.items():
        for mid in ids:
            m = moves.get(mid)
            if m and so(m["power"], 0) and so(m["type_id"], 0) in HE_TU_POKEAPI and m["damage_class_id"] in ("2", "3"):
                he_hoc[dd].add(HE_TU_POKEAPI[int(m["type_id"])])

    def hoc_duoc(dex, ma):
        return any(move_id_tu_ten.get(g) in hoc[dex] for g in CHIEU[ma]["goc"])

    def chieu_he(h, vai):
        ma = "%s.%s" % (HE_EN[h].lower(), vai)
        return ma if ma in CHIEU else None

    VAI_CONG_VL = ["tan_cong", "nhieu_don", "thuong"]
    VAI_CONG_DB = ["xa", "vung", "thuong"]

    def chon_cong(dex, h, uu, tru=()):
        ds = [chieu_he(h, v) for v in uu]
        ds = [x for x in ds if x and x not in tru]
        for x in ds:
            if hoc_duoc(dex, x):
                return x
        return ds[0] if ds else None

    # chieu dac trung theo ho: thanh vien nhan = dang cuoi co he cua chieu (khong co thi moi thanh vien co he do)
    dac_trung_cua = {}
    for goc_s, val in dt.items():
        if not val[0].startswith("dt."):
            continue
        g = int(goc_s)
        if g not in ho_thanh_vien:
            loi.append("chieu dac trung %s: ho %d khong co" % (val[0], g))
            continue
        if max(L[x]["bac"] for x in ho_thanh_vien[g]) < 4 and g not in khoi_dau:
            loi.append("chieu dac trung %s cho ho %d bac %d (chi T4-T6 / khoi dau)" % (val[0], g, L[g]["bac"]))
        tv = ho_thanh_vien[g]
        cuoi = [x for x in tv if L[x]["cuoi"] and val[2] in (L[x]["he1"], L[x]["he2"])]
        if not cuoi:
            cuoi = [x for x in tv if val[2] in (L[x]["he1"], L[x]["he2"])]
        for x in cuoi:
            dac_trung_cua[x] = val[0]

    LC = []
    cap_hoc = cc["cap_hoc"]
    for dex, l in L.items():
        h1, h2 = l["he1"], l["he2"]
        cs = l["cs"]
        vl = cs[1] >= cs[3]
        uu = VAI_CONG_VL if vl else VAI_CONG_DB
        s1 = chon_cong(dex, h1, uu)
        if l["cuoi"] and l["bst"] >= 500:
            tt = chieu_he(h1, "toi_thuong")
            if tt and hoc_duoc(dex, tt):
                s1 = tt
        if h2 >= 0:
            s2 = chon_cong(dex, h2, uu, (s1,))
        else:
            s2 = chon_cong(dex, h1, uu[1:] + uu[:1], (s1,))
        if dex in dac_trung_cua:
            s2 = dac_trung_cua[dex]
        # phu he: he X khac he loai, X khac (x2) mot he W ma W khac loai
        yeu = [tc for tc in range(18) if he_so_loai(tc, h1, h2) > 1.0]
        tot = None
        for x in range(18):
            if x in (h1, h2):
                continue
            n = sum(1 for w in yeu if he_so(x, w) > 1.0)
            if n == 0:
                continue
            ma = chon_cong(dex, x, ["tan_cong", "xa"] if vl else ["xa", "tan_cong"], (s1, s2))
            if not ma:
                continue
            diem = (10 if x in he_hoc[dex] else 0) + (5 if hoc_duoc(dex, ma) else 0) + 3 * n
            if tot is None or diem > tot[0]:
                tot = (diem, ma)
        s3 = tot[1] if tot else chon_cong(dex, 0, ["tan_cong", "xa"], (s1, s2))
        # ho tro theo chi so nen cao nhat: HP/Thu/Thu DB -> khien; TC DB / TC -> buff; Toc do -> khong che
        hi = max(range(6), key=lambda i: (cs[i], -i))
        vai_ht = "khien" if hi in (0, 2, 4) else "khong_che" if hi == 5 else "buff"
        s4 = None
        for hh in ([h1, h2] if h2 >= 0 else [h1]):
            ma = chieu_he(hh, vai_ht)
            if ma and hoc_duoc(dex, ma):
                s4 = ma
                break
        if not s4:
            for hh in ([h1, h2] if h2 >= 0 else [h1]) + [0]:
                ma = chieu_he(hh, vai_ht)
                if ma:
                    s4 = ma
                    break
        vai_o = ["stab", "stab", "phu_he", "ho_tro"]
        for o, ma in enumerate([s1, s2, s3, s4]):
            if not ma:
                loi.append("loai %d thieu chieu o %d" % (dex, o))
                continue
            vai = "dac_trung" if CHIEU[ma]["vai_tro"] == "dac_trung" else vai_o[o]
            LC.append({"dex": dex, "o": o, "ma": ma, "vai": vai, "cap": cap_hoc[o]})
        if len(set([s1, s2, s3, s4])) < 4:
            loi.append("loai %d trung chieu: %s" % (dex, [s1, s2, s3, s4]))

    # ---- vung (chi loai da mo)
    cap_cfg = v["cap"]
    he_so_cap = cap_cfg["he_so"]
    VG = []

    def cap_tien_hoa_vao(dex):
        for x in TH:
            if x["sang"] == dex:
                return x["cap"] if x["kieu"] == "cap" else int(cap_cfg["tien_hoa_mac_dinh"][str(min(3, max(2, L[dex]["giai_doan"])))])
        return 0

    def cap_tien_hoa_ra(dex):
        cs_ = [x["cap"] if x["kieu"] == "cap" else int(cap_cfg["tien_hoa_mac_dinh"][str(min(3, L[dex]["giai_doan"] + 1))])
               for x in TH if x["tu"] == dex]
        return min(cs_) if cs_ else 0

    for dex, l in L.items():
        if not l["bat"] or l["bac"] == 6 or l["giai_doan"] == 0:
            continue
        if l["bac"] == 5 and str(dex) not in v["gap_t5"]:
            continue  # dang cuoi gia huyen thoai: hoang da chi o su kien (2.3)
        if l["giai_doan"] <= 1:
            cmin = int(cap_cfg["goc_theo_bac"][str(min(5, l["bac"]))])
        else:
            cmin = max(10, int(round(he_so_cap * cap_tien_hoa_vao(dex))))
        ra = cap_tien_hoa_ra(dex)
        cmax = min(cap_cfg["toi_da"], int(round(he_so_cap * ra)) + cap_cfg["them_tren"]) if ra else cap_cfg["toi_da"]
        if cmax < cmin:
            cmax = min(cap_cfg["toi_da"], cmin + 20)
        nd = 2 if dex in v["ngay_dem"]["dem"] else 1 if dex in v["ngay_dem"]["ngay"] else 0
        tt = -1
        bac_gap = l["bac"]
        g5 = v["gap_t5"].get(str(dex))
        if g5:
            bac_gap, nd, tt = 5, int(g5["ngay_dem"]), int(g5["thoi_tiet"])
            cmin = max(cmin, int(cap_cfg["goc_theo_bac"]["5"]))
        nha = v["nha"].get(str(l["the_he"]))
        if not nha:
            continue
        khu = [(nha, v["trong_so_nha"])]
        if nha in v["khu_phu"]:
            khu.append((v["khu_phu"][nha], v["trong_so_khu_phu"]))
        for kv, ts_kv in khu:
            for i, vg in enumerate(l["vung"]):
                ts = ts_kv * [100, 40, 20, 10][min(i, 3)] // 100
                if ts <= 0:
                    continue
                VG.append({"dex": dex, "khu": kv, "vung": vg, "cmin": cmin, "cmax": cmax, "ts": ts, "nd": nd, "tt": tt,
                           "bac_gap": bac_gap})

    # ---- doi chieu Showdown
    sd = doc_showdown(os.path.join(d_sd, "pokedex.ts"))
    dc = ["# Doi chieu PokeAPI %s <-> Showdown %s (sinh boi sinh_loai.py)" % (
        cfg["nguon"]["pokeapi"]["commit"][:10], cfg["nguon"]["showdown"]["commit"][:10]), ""]
    lech = 0
    cho_phep = cfg.get("doi_chieu_cho_phep", {})
    sd_ten = {}
    for n, e in sd.items():
        sd_ten[re.sub(r"[^a-z0-9]", "", e["name"].lower())] = n
    trung_sd = {"monster": 1, "water 1": 2, "bug": 3, "flying": 4, "field": 5, "fairy": 6, "grass": 7, "human-like": 8,
                "water 3": 9, "mineral": 10, "amorphous": 11, "water 2": 12, "ditto": 13, "dragon": 14, "undiscovered": 15}
    for dex, l in L.items():
        e = sd.get(dex)
        if not e:
            dc.append("%d %s: KHONG CO trong Showdown" % (dex, l["ten"]))
            lech += 1
            continue
        he_sd = [HE_EN.index(t) for t in e["types"] if t in HE_EN]
        he_pa = [l["he1"]] + ([l["he2"]] if l["he2"] >= 0 else [])
        if he_sd != he_pa:
            dc.append("%d %s: he PokeAPI %s / Showdown %s" % (dex, l["ten"], he_pa, he_sd))
            lech += 1
        if str(dex) in cho_phep:
            dc.append("%d %s: bo qua doi chieu (%s)" % (dex, l["ten"], cho_phep[str(dex)]))
            continue
        if e["stats"] != l["cs"]:
            dc.append("%d %s: chi so PokeAPI %s / Showdown %s" % (dex, l["ten"], l["cs"], e["stats"]))
            lech += 1
        pv = sd_ten.get(re.sub(r"[^a-z0-9]", "", e["prevo"].split("-")[0].lower()), 0) if e["prevo"] else 0
        if e["prevo"] and not pv:
            pv = sd_ten.get(re.sub(r"[^a-z0-9]", "", e["prevo"].lower()), 0)
        if pv != l["tu"]:
            dc.append("%d %s: tien hoa tu PokeAPI %d / Showdown %d (%s)" % (dex, l["ten"], l["tu"], pv, e["prevo"]))
            lech += 1
        tg = sorted(trung_sd.get(x.lower(), 0) for x in e["egg"])
        if tg and tg != sorted(l["trung"]):
            dc.append("%d %s: nhom trung PokeAPI %s / Showdown %s" % (dex, l["ten"], sorted(l["trung"]), tg))
            lech += 1
        if e["name"].replace("\\u2019", "'").replace("\u2019", "'") != l["ten"]:
            dc.append("%d: ten PokeAPI '%s' / Showdown '%s' (giu ten PokeAPI)" % (dex, l["ten"], e["name"]))
    dc.append("")
    dc.append("Tong: %d loai, %d lech du lieu (he / chi so / tien hoa / nhom trung)." % (len(L), lech))
    if lech > 0:
        loi.append("lech Showdown %d muc (xem du_lieu/loai/doi_chieu.txt)" % lech)

    # ---- kiem cuoi
    for dex, l in L.items():
        if not (0 <= l["he1"] < 18) or not (-1 <= l["he2"] < 18) or l["he1"] == l["he2"]:
            loi.append("loai %d he khong hop le" % dex)
        if l["bst"] != sum(l["cs"]):
            loi.append("loai %d BST lech" % dex)
    for x in TH:
        if x["sang"] not in L or x["tu"] not in L:
            loi.append("tien hoa tro toi loai khong co: %s" % x)
    n_lc = defaultdict(int)
    for x in LC:
        n_lc[x["dex"]] += 1
        if x["ma"] not in CHIEU:
            loi.append("chieu loai %d tro toi chieu khong co %s" % (x["dex"], x["ma"]))
    for dex in L:
        if n_lc[dex] != 4:
            loi.append("loai %d co %d chieu (can 4)" % (dex, n_lc[dex]))

    os.makedirs(RA_CSV, exist_ok=True)
    with open(os.path.join(RA_CSV, "doi_chieu.txt"), "w", encoding="utf8") as f:
        f.write("\n".join(dc) + "\n")
    ghi_csv(L, TH, CHIEU, LC, VG, cfg, ten_trung)
    ghi_sql(L, TH, CHIEU, LC, VG, cfg, ten_trung)

    so_mo = sum(1 for l in L.values() if l["bat"])
    print("Loai: %d (mo %d) · tien hoa: %d · chieu: %d (dac trung %d) · chieu loai: %d · vung: %d dong" % (
        len(L), so_mo, len(TH), len(CHIEU), sum(1 for c in CHIEU.values() if c["vai_tro"] == "dac_trung"), len(LC), len(VG)))
    print("Bac:", dict(sorted(defaultdict(int, ((k, sum(1 for l in L.values() if l["bac"] == k)) for k in range(1, 7))).items())))
    print("Doi chieu Showdown: %d lech" % lech)
    if loi:
        for x in loi[:60]:
            print("LOI", x)
        print("%d loi" % len(loi))
        return 1
    print("Da ghi %s" % os.path.relpath(RA_SQL, GOC))
    return 0


def ghi_csv(L, TH, CHIEU, LC, VG, cfg, ten_trung):
    def w(ten, hang, dong):
        with open(os.path.join(RA_CSV, ten), "w", encoding="utf8", newline="") as f:
            c = csv.writer(f, lineterminator="\n")
            c.writerow(hang)
            c.writerows(dong)
    w("bang_loai.csv", ["dex", "ten", "the_he", "he1", "he2", "hp", "tan_cong", "phong_thu", "tan_cong_db", "phong_thu_db",
                        "toc_do", "tong", "nhom", "giai_doan", "dex_goc", "tien_hoa_tu", "bac", "goc_bat", "nhom_trung",
                        "moi_truong", "dot_mo", "bat", "sprite"],
      [[l["dex"], l["ten"], l["the_he"], TEN_HE[l["he1"]], TEN_HE[l["he2"]] if l["he2"] >= 0 else "", *l["cs"], l["bst"],
        l["nhom"], l["giai_doan"], l["dex_goc"], l["tu"], l["bac"], l["goc_bat"],
        "/".join(ten_trung[x] for x in l["trung"]), l["moi_truong"], l["dot_mo"], l["bat"], l["sprite"]["trang_thai"]]
       for l in L.values()])
    w("bang_tien_hoa.csv", ["tu", "sang", "kieu", "co_che", "cap", "vat_pham", "id_vat_pham", "ngay_dem", "keo", "dieu_kien"],
      [[x["tu"], x["sang"], x["kieu"], x["co_che"], x["cap"], x["ten_vp"], x["id_vp"], x["ngay_dem"], x["keo"], x["dieu_kien"]]
       for x in TH])
    w("bang_chieu.csv", ["ma", "ten", "he", "kieu", "vai_tro", "pt", "so_don", "hoi_ms", "pp", "trang_thai", "hinh", "hieu_ung",
                         "mau", "doi_mau", "dac_trung_ho"],
      [[c["ma"], c["ten"], TEN_HE[c["he"]], c["kieu"], c["vai_tro"], c["pt"], c["so_don"], c["hoi_ms"], c["pp"],
        c["trang_thai"], c["hinh"], c["hu"], c["mau"], c["doi_mau"], c["dac_trung_ho"] or ""] for c in CHIEU.values()])
    w("bang_loai_chieu.csv", ["dex", "ten", "o", "chieu", "ten_chieu", "vai", "cap_hoc"],
      [[x["dex"], L[x["dex"]]["ten"], x["o"], x["ma"], CHIEU[x["ma"]]["ten"], x["vai"], x["cap"]] for x in LC])
    w("bang_loai_vung.csv", ["dex", "ten", "khu_vuc", "vung", "cap_min", "cap_max", "trong_so", "ngay_dem", "thoi_tiet", "bac_gap"],
      [[x["dex"], L[x["dex"]]["ten"], x["khu"], x["vung"], x["cmin"], x["cmax"], x["ts"], x["nd"], x["tt"], x["bac_gap"]]
       for x in VG])


def khoi_insert(bang, cot, dong, cap_nhat=None, lo=200, ignore=False):
    """INSERT ... ON DUPLICATE KEY UPDATE theo lo 200 dong."""
    ra = []
    for i in range(0, len(dong), lo):
        phan = dong[i:i + lo]
        s = "INSERT %sINTO `%s` (%s) VALUES\n" % ("IGNORE " if ignore else "", bang, ", ".join("`%s`" % c for c in cot))
        s += ",\n".join("(" + ",".join(sql_chuoi(v) for v in d) + ")" for d in phan)
        if cap_nhat:
            s += "\nON DUPLICATE KEY UPDATE " + ", ".join(cap_nhat)
        ra.append(s + ";")
    return "\n".join(ra)


def ghi_sql(L, TH, CHIEU, LC, VG, cfg, ten_trung):
    n = cfg["nguon"]
    out = []
    out.append("""-- =====================================================================================================================
-- HTBB - DU LIEU DAY DU 1.025 LOAI POKEMON (he_thong_day_du.md 3.1-3.4, 4.2; thiet_ke_can_bang.md)
-- TEP SINH (dung sua tay): python3 -I tools/pokemon/loai/sinh_loai.py tu
--   PokeAPI %s (BSD-3-Clause, docs/giay-phep.md), doi chieu Pokemon Showdown %s (MIT),
--   PMDCollab SpriteCollab %s (CC BY-NC 4.0: chi trang thai sprite + ghi cong),
--   tools/pokemon/loai/loai_duyet.json (cho duy nhat sua tay: bac, vung, chieu, vat pham).
-- Day la NGUON SU THAT khi trien khai (khong chay bo sinh khi deploy). Idempotent: chay lai bao nhieu lan cung duoc,
-- MariaDB 10.3+, utf8:   khoi nay nam trong sql/nang_cap.sql (vps/nang_cap_csdl.sh), dump: sql/htbb_day_du.sql
--   data_loai            1.025 loai: ten (giu ten chinh thuc), he, 6 chi so nen, nhom, ho, giai doan, bac T1-T6, goc bat,
--                        nhom trung, moi truong, dot mo; bat = 1 loai DA MO (Gen 1-2, dot A)
--   data_loai_tien_hoa   moi buoc tien hoa that + dieu kien, anh xa co che HTBB (cap / vat_pham / trao_doi / than_thiet / keo)
--   data_chieu           danh muc chieu dung chung (18 he x vai tro) + chieu dac trung ho T4-T6 / khoi dau
--   data_loai_chieu      4 chieu ban dong hanh moi loai: 2 cung he (STAB), 1 phu he, 1 ho tro
--   data_loai_vung       noi song cua loai DA MO: khu vuc, vung sinh thai, dai cap quai, trong so, ngay/dem, thoi tiet
--   data_loai_sprite     trang thai sprite / chan dung / goi may chu (server/map/loai) + ghi cong
--   data_nhom_trung_ten, data_nhom_trung   cung luoc do nhanh ket-doi (khong ghi de dong da co)
--   data_bat_loai, data_bat_chi_so_loai    cung luoc do nhanh bat-trade (them dong loai da mo, giu dong da chinh tay)
-- Khong ghi de cot nguoi van hanh da chinh tren web: data_loai.bat / goc_bat (chi ghi lan dau), mo_ta.
-- =====================================================================================================================
""" % (n["pokeapi"]["commit"], n["showdown"]["commit"], n["pmdcollab"]["commit"]))

    out.append("""CREATE TABLE IF NOT EXISTS `data_loai` (
  `dex` SMALLINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL,
  `ma` VARCHAR(32) NOT NULL,
  `the_he` TINYINT NOT NULL,
  `he1` TINYINT NOT NULL,
  `he2` TINYINT NOT NULL DEFAULT -1,
  `hp` SMALLINT NOT NULL,
  `tan_cong` SMALLINT NOT NULL,
  `phong_thu` SMALLINT NOT NULL,
  `tan_cong_db` SMALLINT NOT NULL,
  `phong_thu_db` SMALLINT NOT NULL,
  `toc_do` SMALLINT NOT NULL,
  `tong_chi_so` SMALLINT NOT NULL,
  `nhom` TINYINT NOT NULL DEFAULT 0 COMMENT '0 thuong, 1 huyen thoai, 2 huyen ao, 3 Ultra Beast, 4 Nghich ly',
  `ho` SMALLINT NOT NULL DEFAULT 0 COMMENT 'evolution_chain_id PokeAPI',
  `dex_goc` SMALLINT NOT NULL DEFAULT 0 COMMENT 'loai goc cua ho (Keo dung chung ca ho)',
  `tien_hoa_tu` SMALLINT NOT NULL DEFAULT 0,
  `giai_doan` TINYINT NOT NULL DEFAULT 1 COMMENT '0 baby, 1..3',
  `bac` TINYINT NOT NULL COMMENT '1..6 = T1 Pho bien .. T6 Huyen thoai',
  `goc_bat` SMALLINT NOT NULL COMMENT 'ti le bat goc phan nghin (T6: chi sau dot kich)',
  `bat_duoc` TINYINT NOT NULL DEFAULT 1,
  `chi_su_kien` TINYINT NOT NULL DEFAULT 0,
  `nhom_trung1` TINYINT NOT NULL DEFAULT 15,
  `nhom_trung2` TINYINT DEFAULT NULL,
  `moi_truong` VARCHAR(16) NOT NULL DEFAULT '',
  `ti_le_cai` TINYINT NOT NULL DEFAULT -1 COMMENT 'gender_rate PokeAPI: -1 vo tinh, 0..8 phan tam cai',
  `chieu_cao` SMALLINT NOT NULL DEFAULT 0 COMMENT 'dm',
  `can_nang` SMALLINT NOT NULL DEFAULT 0 COMMENT 'hg',
  `dot_mo` TINYINT NOT NULL DEFAULT 4 COMMENT '1 A Gen1-2, 2 B Gen3, 3 C Gen4, 4 D Gen5-9',
  `bat` TINYINT NOT NULL DEFAULT 0 COMMENT '1 = loai da mo (ngoai troi, bat duoc)',
  `mo_ta` VARCHAR(255) NOT NULL DEFAULT '',
  `sua_luc` DATETIME DEFAULT NULL,
  PRIMARY KEY (`dex`),
  KEY `k_bat` (`bat`, `the_he`),
  KEY `k_dex_goc` (`dex_goc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
""")
    cot = ["dex", "ten", "ma", "the_he", "he1", "he2", "hp", "tan_cong", "phong_thu", "tan_cong_db", "phong_thu_db", "toc_do",
           "tong_chi_so", "nhom", "ho", "dex_goc", "tien_hoa_tu", "giai_doan", "bac", "goc_bat", "bat_duoc", "chi_su_kien",
           "nhom_trung1", "nhom_trung2", "moi_truong", "ti_le_cai", "chieu_cao", "can_nang", "dot_mo", "bat"]
    dong = []
    for l in L.values():
        dong.append([l["dex"], l["ten"], l["ma"], l["the_he"], l["he1"], l["he2"], *l["cs"], l["bst"], l["nhom"], l["ho"],
                     l["dex_goc"], l["tu"], l["giai_doan"], l["bac"], l["goc_bat"], l["bat_duoc"], l["chi_su_kien"],
                     l["trung"][0], l["trung"][1] if len(l["trung"]) > 1 else None, l["moi_truong"], l["gioi_tinh"],
                     min(32767, l["cao"]), min(32767, l["nang"]), l["dot_mo"], l["bat"]])
    giu = {"bat", "goc_bat", "dex"}
    cn = ["`%s` = VALUES(`%s`)" % (c, c) for c in cot if c not in giu]
    out.append(khoi_insert("data_loai", cot, dong, cn))
    out.append("-- Dot dang mo: bat loai da mo co hinh (chi bat, khong tat loai nguoi van hanh da bat).")
    mo = [l["dex"] for l in L.values() if l["bat"]]
    out.append("UPDATE `data_loai` SET `bat` = 1 WHERE `bat` = 0 AND `sua_luc` IS NULL AND `dex` IN (%s);\n" % ",".join(map(str, mo)))

    out.append("""CREATE TABLE IF NOT EXISTS `data_loai_tien_hoa` (
  `tu_dex` SMALLINT NOT NULL,
  `sang_dex` SMALLINT NOT NULL,
  `kieu` VARCHAR(12) NOT NULL COMMENT 'cap / da / trao_doi / than_thiet / vat_pham / khac (cach goc)',
  `co_che` VARCHAR(12) NOT NULL COMMENT 'co che HTBB: cap (tu dong) / vat_pham / trao_doi (Day Lien Ket hoac sau trao doi) / than_thiet / keo',
  `cap` TINYINT NOT NULL,
  `vat_pham` VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'ma vat pham PokeAPI',
  `ten_vat_pham` VARCHAR(48) NOT NULL DEFAULT '',
  `id_vat_pham` INT NOT NULL DEFAULT 0 COMMENT 'data_item HTBB, 0 = chua co (dung Keo)',
  `ngay_dem` TINYINT NOT NULL DEFAULT 0,
  `than_thiet` SMALLINT NOT NULL DEFAULT 0,
  `keo` SMALLINT NOT NULL DEFAULT 0,
  `dieu_kien` VARCHAR(160) NOT NULL DEFAULT '',
  `goc_kieu` VARCHAR(32) NOT NULL DEFAULT '',
  PRIMARY KEY (`tu_dex`, `sang_dex`),
  KEY `k_sang` (`sang_dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
""")
    cot = ["tu_dex", "sang_dex", "kieu", "co_che", "cap", "vat_pham", "ten_vat_pham", "id_vat_pham", "ngay_dem", "than_thiet",
           "keo", "dieu_kien", "goc_kieu"]
    dong = [[x["tu"], x["sang"], x["kieu"], x["co_che"], x["cap"], x["ma_vp"], x["ten_vp"], x["id_vp"], x["ngay_dem"],
             x["than_thiet"], x["keo"], x["dieu_kien"], x["goc_kieu"]] for x in TH]
    out.append(khoi_insert("data_loai_tien_hoa", cot, dong, ["`%s` = VALUES(`%s`)" % (c, c) for c in cot[2:]]))
    out.append("")

    out.append("""CREATE TABLE IF NOT EXISTS `data_chieu` (
  `ma` VARCHAR(32) NOT NULL,
  `ten` VARCHAR(48) NOT NULL,
  `he` TINYINT NOT NULL,
  `kieu` VARCHAR(10) NOT NULL COMMENT 'vat_ly / dac_biet / ho_tro',
  `vai_tro` VARCHAR(12) NOT NULL,
  `pt_sat_thuong` SMALLINT NOT NULL DEFAULT 0 COMMENT 'ti le trong vai tro (ban dong hanh bi kep 12%% DPS chu)',
  `so_don` TINYINT NOT NULL DEFAULT 1,
  `hoi_chieu_ms` INT NOT NULL DEFAULT 0,
  `pp` SMALLINT NOT NULL DEFAULT 0,
  `trang_thai` VARCHAR(16) NOT NULL DEFAULT '',
  `ti_le_tt_phan_nghin` SMALLINT NOT NULL DEFAULT 0,
  `hinh_dang` VARCHAR(8) NOT NULL DEFAULT 'tron' COMMENT 'tron / thang / non / vung / than (pvp-keo dung)',
  `tam_xa` SMALLINT NOT NULL DEFAULT 0,
  `rong` SMALLINT NOT NULL DEFAULT 0,
  `hieu_ung` VARCHAR(48) NOT NULL COMMENT 'hieu ung co san du_lieu/bang_hieu_ung.csv (file_client khong .bytes)',
  `mau` CHAR(6) NOT NULL DEFAULT 'FFFFFF' COMMENT 'mau he (doi bang mau luc dong goi neu doi_mau = 1)',
  `doi_mau` TINYINT NOT NULL DEFAULT 0,
  `dac_trung_ho` SMALLINT DEFAULT NULL COMMENT 'dex goc cua ho neu la chieu dac trung',
  `goc` VARCHAR(255) NOT NULL DEFAULT '' COMMENT 'id chieu PokeAPI tuong ung (doi chieu learnset)',
  `bat` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
""".replace("%%", "%"))
    cot = ["ma", "ten", "he", "kieu", "vai_tro", "pt_sat_thuong", "so_don", "hoi_chieu_ms", "pp", "trang_thai",
           "ti_le_tt_phan_nghin", "hinh_dang", "tam_xa", "rong", "hieu_ung", "mau", "doi_mau", "dac_trung_ho", "goc"]
    dong = [[c["ma"], c["ten"], c["he"], c["kieu"], c["vai_tro"], c["pt"], c["so_don"], c["hoi_ms"], c["pp"], c["trang_thai"],
             c["ti_le"], c["hinh"], c["tam"], c["rong"], c["hu"], c["mau"], c["doi_mau"], c["dac_trung_ho"], ",".join(c["goc"])]
            for c in CHIEU.values()]
    out.append(khoi_insert("data_chieu", cot, dong, ["`%s` = VALUES(`%s`)" % (c, c) for c in cot[1:]]))
    out.append("")

    out.append("""CREATE TABLE IF NOT EXISTS `data_loai_chieu` (
  `dex` SMALLINT NOT NULL,
  `o` TINYINT NOT NULL COMMENT '0..3',
  `ma_chieu` VARCHAR(32) NOT NULL,
  `vai` VARCHAR(10) NOT NULL COMMENT 'stab / phu_he / ho_tro / dac_trung',
  `cap_hoc` TINYINT NOT NULL DEFAULT 1,
  `nguon` VARCHAR(8) NOT NULL DEFAULT 'cap',
  PRIMARY KEY (`dex`, `o`),
  KEY `k_chieu` (`ma_chieu`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
""")
    cot = ["dex", "o", "ma_chieu", "vai", "cap_hoc"]
    dong = [[x["dex"], x["o"], x["ma"], x["vai"], x["cap"]] for x in LC]
    out.append(khoi_insert("data_loai_chieu", cot, dong, ["`%s` = VALUES(`%s`)" % (c, c) for c in cot[2:]]))
    out.append("")

    out.append("""CREATE TABLE IF NOT EXISTS `data_loai_vung` (
  `dex` SMALLINT NOT NULL,
  `khu_vuc` VARCHAR(8) NOT NULL COMMENT 'Kanto / Johto / Hoenn (du_lieu/vung_map.csv)',
  `vung` TINYINT NOT NULL COMMENT 'vung sinh thai SinhThaiTheGioi.V_* (data_hst_vung)',
  `cap_min` TINYINT NOT NULL,
  `cap_max` TINYINT NOT NULL,
  `trong_so` SMALLINT NOT NULL,
  `ngay_dem` TINYINT NOT NULL DEFAULT 0 COMMENT '0 moi luc, 1 ngay, 2 dem',
  `thoi_tiet` TINYINT NOT NULL DEFAULT -1 COMMENT '-1 moi thoi tiet (SinhThaiTheGioi.TEN_THOI_TIET)',
  `bac_gap` TINYINT NOT NULL COMMENT 'bac dung o luot gap khi hoi sinh (T5: chi dung gio HOAC dung thoi tiet)',
  PRIMARY KEY (`dex`, `khu_vuc`, `vung`),
  KEY `k_vung` (`khu_vuc`, `vung`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- Bang chi chua loai DA MO: xoa dong cua loai chua mo (chay lai sau khi doi dot mo).
""")
    out.append("DELETE FROM `data_loai_vung` WHERE `dex` NOT IN (%s);" % ",".join(map(str, sorted(set(x["dex"] for x in VG)))))
    cot = ["dex", "khu_vuc", "vung", "cap_min", "cap_max", "trong_so", "ngay_dem", "thoi_tiet", "bac_gap"]
    dong = [[x["dex"], x["khu"], x["vung"], x["cmin"], x["cmax"], x["ts"], x["nd"], x["tt"], x["bac_gap"]] for x in VG]
    out.append(khoi_insert("data_loai_vung", cot, dong, ["`%s` = VALUES(`%s`)" % (c, c) for c in cot[3:]]))
    out.append("")

    out.append("""-- Khu vuc dia ly cua ban do (ban sao du_lieu/vung_map.csv): may chu ghep voi data_hst_vung de chon loai.
CREATE TABLE IF NOT EXISTS `data_loai_khu_vuc_map` (
  `map_id` SMALLINT NOT NULL,
  `khu_vuc` VARCHAR(8) NOT NULL,
  PRIMARY KEY (`map_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
""")
    km = []
    with open(os.path.join(GOC, "du_lieu", "vung_map.csv"), encoding="utf8") as f:
        for dong_ in f:
            dong_ = dong_.strip()
            if not dong_ or dong_.startswith("#") or dong_.startswith("id;"):
                continue
            a, b_ = dong_.split(";")
            km.append([int(a), b_])
    out.append(khoi_insert("data_loai_khu_vuc_map", ["map_id", "khu_vuc"], km, ["`khu_vuc` = VALUES(`khu_vuc`)"]))
    out.append("")

    out.append("""CREATE TABLE IF NOT EXISTS `data_loai_sprite` (
  `dex` SMALLINT NOT NULL,
  `trang_thai` TINYINT NOT NULL DEFAULT 0 COMMENT '0 thieu (chi Pokedex), 1 dung duoc, 2 du dong tac',
  `chan_dung` TINYINT NOT NULL DEFAULT 0,
  `trong_jar` TINYINT NOT NULL DEFAULT 0,
  `goi_may_chu` TINYINT NOT NULL DEFAULT 0 COMMENT 'server/map/loai/<NNNN>.goi',
  `kich_thuoc` INT NOT NULL DEFAULT 0,
  `ghi_cong` VARCHAR(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
""")
    cot = ["dex", "trang_thai", "chan_dung", "trong_jar", "goi_may_chu", "kich_thuoc", "ghi_cong"]
    dong = [[l["dex"], l["sprite"]["trang_thai"], l["sprite"]["chan_dung"], l["sprite"]["trong_jar"], l["sprite"]["goi_server"],
             l["sprite"]["kich_thuoc"], l["sprite"]["ghi_cong"]] for l in L.values()]
    out.append(khoi_insert("data_loai_sprite", cot, dong, ["`%s` = VALUES(`%s`)" % (c, c) for c in cot[1:]]))
    out.append("")

    out.append("""-- Nhom trung: CUNG luoc do nhanh ket-doi (sql/sua_ket_doi.sql). id = egg_group PokeAPI. Khong ghi de dong da co.
CREATE TABLE IF NOT EXISTS `data_nhom_trung_ten` (
  `id` TINYINT UNSIGNED NOT NULL,
  `ma` VARCHAR(20) NOT NULL,
  `ten` VARCHAR(50) NOT NULL,
  `ket_doi_duoc` TINYINT NOT NULL DEFAULT 1,
  `sua_luc` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `data_nhom_trung` (
  `dex` SMALLINT UNSIGNED NOT NULL,
  `ten_loai` VARCHAR(30) NOT NULL,
  `nhom1` TINYINT UNSIGNED NOT NULL,
  `nhom2` TINYINT UNSIGNED DEFAULT NULL,
  `sua_luc` DATETIME DEFAULT NULL,
  PRIMARY KEY (`dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
""")
    TEN_NT = {1: ("monster", "Quái vật"), 2: ("water1", "Nước 1"), 3: ("bug", "Côn trùng"), 4: ("flying", "Bay"),
              5: ("field", "Đồng cỏ"), 6: ("fairy", "Tiên"), 7: ("grass", "Thực vật"), 8: ("humanlike", "Dạng người"),
              9: ("water3", "Nước 3"), 10: ("mineral", "Khoáng vật"), 11: ("amorphous", "Vô định hình"),
              12: ("water2", "Nước 2"), 13: ("ditto", "Ditto"), 14: ("dragon", "Rồng"), 15: ("undiscovered", "Chưa khám phá")}
    out.append(khoi_insert("data_nhom_trung_ten", ["id", "ma", "ten", "ket_doi_duoc"],
                           [[k, v[0], v[1], 0 if k == 15 else 1] for k, v in TEN_NT.items()], ignore=True))
    out.append(khoi_insert("data_nhom_trung", ["dex", "ten_loai", "nhom1", "nhom2"],
                           [[l["dex"], l["ten"][:30], l["trung"][0], l["trung"][1] if len(l["trung"]) > 1 else None]
                            for l in L.values()], ignore=True))
    out.append("")

    out.append("""-- Thu phuc: CUNG luoc do nhanh bat-trade (data_bat_loai, data_bat_chi_so_loai). Chi them dong cho loai DA MO;
-- dong da co giu nguyen (ti_le_phan_nghin chi dat khi dang 0 = theo bac cu). do_hiem: T1 -> 0, T2 -> 1, T3 -> 2, T4-T5 -> 3.
CREATE TABLE IF NOT EXISTS `data_bat_loai` (
  `dex` SMALLINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `do_hiem` TINYINT NOT NULL DEFAULT 0,
  `ti_le_phan_nghin` SMALLINT NOT NULL DEFAULT 0,
  `tien_hoa_thanh` SMALLINT NOT NULL DEFAULT 0,
  `cap_tien_hoa` TINYINT NOT NULL DEFAULT 0,
  `keo_tien_hoa` SMALLINT NOT NULL DEFAULT 0,
  `bat_duoc` TINYINT NOT NULL DEFAULT 1,
  `chi_su_kien` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`dex`)
) DEFAULT CHARSET=utf8;
ALTER TABLE `data_bat_loai` ADD COLUMN IF NOT EXISTS `tien_hoa_trao_doi` TINYINT NOT NULL DEFAULT 0;
CREATE TABLE IF NOT EXISTS `data_bat_chi_so_loai` (
  `dex` SMALLINT NOT NULL,
  `hp` SMALLINT NOT NULL DEFAULT 60,
  `tan_cong` SMALLINT NOT NULL DEFAULT 60,
  `phong_thu` SMALLINT NOT NULL DEFAULT 60,
  `tan_cong_db` SMALLINT NOT NULL DEFAULT 60,
  `phong_thu_db` SMALLINT NOT NULL DEFAULT 60,
  `toc_do` SMALLINT NOT NULL DEFAULT 60,
  PRIMARY KEY (`dex`)
) DEFAULT CHARSET=utf8;
""")
    th_dau = {}
    for x in TH:
        if x["tu"] not in th_dau:  # tien hoa dau tien (nhanh chinh); Hop PC nhieu nhanh doc data_loai_tien_hoa
            th_dau[x["tu"]] = x
    dong = []
    for l in L.values():
        if not l["bat"]:
            continue
        x = th_dau.get(l["dex"])
        dong.append([l["dex"], l["ten"], {1: 0, 2: 1, 3: 2}.get(l["bac"], 3), l["goc_bat"], x["sang"] if x else 0,
                     x["cap"] if x else 0, x["keo"] if x else 0, l["bat_duoc"], l["chi_su_kien"],
                     1 if x and x["co_che"] == "trao_doi" else 0])
    out.append(khoi_insert("data_bat_loai", ["dex", "ten", "do_hiem", "ti_le_phan_nghin", "tien_hoa_thanh", "cap_tien_hoa",
                                             "keo_tien_hoa", "bat_duoc", "chi_su_kien", "tien_hoa_trao_doi"], dong,
                           ["`ti_le_phan_nghin` = IF(`ti_le_phan_nghin` = 0, VALUES(`ti_le_phan_nghin`), `ti_le_phan_nghin`)"]))
    out.append(khoi_insert("data_bat_chi_so_loai", ["dex", "hp", "tan_cong", "phong_thu", "tan_cong_db", "phong_thu_db", "toc_do"],
                           [[l["dex"], *l["cs"]] for l in L.values()], ignore=True))
    out.append("")
    out.append("-- Het khoi them_loai_day_du.sql. Kiem du lieu: server/test/KiemLoaiDayDu (DayDu.phanNangCap).")
    khoi = "\n".join(out) + "\n"
    with open(RA_SQL, encoding="utf8") as f:
        cu = f.read()
    a = cu.find("\n" + MOC_SQL)
    if a < 0:
        sys.exit("sql/nang_cap.sql thieu dong moc " + MOC_SQL.strip())
    a += 1
    b = cu.find("\n-- >>> ", a + 1)
    duoi = "" if b < 0 else cu[b + 1:]
    with open(RA_SQL, "w", encoding="utf8", newline="\n") as f:
        f.write(cu[:a] + MOC_SQL + khoi + duoi)


if __name__ == "__main__":
    sys.exit(main())
