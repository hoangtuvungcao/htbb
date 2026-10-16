"""
G7.C - phan dung chung cua sinh_sql_chu.py va quet_tu_cam.py (KHONG chay truc tiep).

MOT NGUON: tu dien doi chu va danh sach tu cam nam trong
src/com/kpah/pokemon/ChuPokemon.java (DOI, TEN_THU_CUOI, CHO_PHEP, TU_CAM) va ten dia danh
trong src/com/kpah/pokemon/TenMapPokemon.java (BANG). File nay doc thang hai file Java do
(regex tren ma nguon) va ghep bang dung thu tu ChuPokemon.bangDayDu().

Nap bang importlib (python3 -I khong dua thu muc script vao sys.path):
    spec = importlib.util.spec_from_file_location("chu_pokemon", <duong dan>)
"""
import os
import re

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
F_CHU = os.path.join(GOC, "src", "com", "kpah", "pokemon", "ChuPokemon.java")
F_MAP = os.path.join(GOC, "src", "com", "kpah", "pokemon", "TenMapPokemon.java")
F_DUMP = os.path.join(GOC, "sql", "gamedb7_clean.sql")

PHAN_BIET = "="

# Chuoi Java "..." (co \" \\ \n ben trong)
_LIT = re.compile(r'"((?:[^"\\\n]|\\.)*)"')


def bo_thoat_java(s):
    """Bo ky tu thoat cua mot chuoi Java (chi cac kieu dung trong cac bang)."""
    ra = []
    i = 0
    while i < len(s):
        c = s[i]
        if c == "\\" and i + 1 < len(s):
            n = s[i + 1]
            ra.append({"n": "\n", "t": "\t", "r": "\r", '"': '"', "'": "'", "\\": "\\"}.get(n, n))
            i += 2
            continue
        ra.append(c)
        i += 1
    return "".join(ra)


def _khoi(ma, ten):
    """Phan than { ... }; cua mang `ten` trong ma Java."""
    m = re.search(r"\b%s\s*=\s*\{" % re.escape(ten), ma)
    if not m:
        raise SystemExit("LOI: khong tim thay mang %s" % ten)
    i = m.end()
    sau = 1
    j = i
    trong_chuoi = False
    while sau > 0:
        c = ma[j]
        if trong_chuoi:
            if c == "\\":
                j += 2
                continue
            if c == '"':
                trong_chuoi = False
        elif c == '"':
            trong_chuoi = True
        elif c == "/" and ma[j + 1] == "/":
            j = ma.index("\n", j)
            continue
        elif c == "{":
            sau += 1
        elif c == "}":
            sau -= 1
        j += 1
    return ma[i:j - 1]


def mang_1(ma, ten):
    return [bo_thoat_java(x) for x in _LIT.findall(_khoi(ma, ten))]


def mang_2(ma, ten):
    than = _khoi(ma, ten)
    ra = []
    for dong in re.findall(r"\{([^{}]*)\}", than):
        ra.append([bo_thoat_java(x) for x in _LIT.findall(dong)])
    return ra


def doc_bang():
    """Tra ve dict: TU_CAM, CHO_PHEP, DOI, TEN_THU_CUOI, DIA_DANH."""
    chu = open(F_CHU, encoding="utf8").read()
    mp = open(F_MAP, encoding="utf8").read()
    return {
        "TU_CAM": mang_1(chu, "TU_CAM"),
        "CHO_PHEP": mang_1(chu, "CHO_PHEP"),
        "DOI": mang_2(chu, "DOI"),
        "TEN_THU_CUOI": mang_2(chu, "TEN_THU_CUOI"),
        "DIA_DANH": mang_2(mp, "BANG"),
    }


def bang_day_du(b):
    """Giong het ChuPokemon.bangDayDu() (thu tu ghep)."""
    d = [[p, p] for p in b["CHO_PHEP"]]
    d += [[x[0], x[1]] for x in b["DOI"]]
    for k, v in b["TEN_THU_CUOI"]:
        d.append(["linh thú " + k, "thú cưỡi " + v])
        d.append(["trứng " + k, "trứng " + v])
    for k, v in b["DIA_DANH"]:
        d.append([PHAN_BIET + k, v])
    return d


def _khoa(s):
    return s[1:] if s.startswith(PHAN_BIET) else s


class TuDien:
    """Ban Python cua ChuPokemon.Bang: regex mot luot, khoa dai truoc."""

    def __init__(self, dong):
        sx = sorted(dong, key=lambda d: -len(_khoa(d[0])))  # sorted on dinh nhu Collections.sort
        self.pb = {}
        self.kpb = {}
        alt = []
        for k0, v in sx:
            k = _khoa(k0)
            if k0.startswith(PHAN_BIET):
                if k in self.pb:
                    continue
                self.pb[k] = v
                alt.append("(?-i:%s)" % re.escape(k))
            else:
                t = k.lower()
                if t in self.kpb:
                    continue
                self.kpb[t] = v
                alt.append(re.escape(k))
        self.mau = re.compile(r"(?<!\w)(?:%s)(?!\w)" % "|".join(alt), re.IGNORECASE)

    @staticmethod
    def _theo_kieu(goc, moi):
        if not moi:
            return moi
        chu = [c for c in goc if c.isalpha()]
        if chu and all(c.isupper() for c in chu) and len(goc) > 1:
            return moi.upper()
        tu = goc.split(" ")
        if len(tu) >= 2 and all(t and t[0].isupper() for t in tu):
            return "".join(c.upper() if i == 0 or moi[i - 1] == " " else c for i, c in enumerate(moi))
        if goc[0].isupper() and moi[0].islower():
            return moi[0].upper() + moi[1:]
        return moi

    def doi(self, s):
        if not s:
            return s

        def thay(m):
            g = m.group(0)
            if g in self.pb:
                return self.pb[g]
            v = self.kpb.get(g.lower())
            return g if v is None else self._theo_kieu(g, v)

        return self.mau.sub(thay, s)

    def tim(self, s):
        """Cho bang tu cam: danh sach doan khop co gia tri khac rong (CHO_PHEP = rong)."""
        ra = []
        if not s:
            return ra
        for m in self.mau.finditer(s):
            g = m.group(0)
            if g in self.pb:
                ra.append(g)
                continue
            v = self.kpb.get(g.lower())
            if v:
                ra.append(g)
        return ra


def tu_dien_doi(b=None):
    b = b or doc_bang()
    return TuDien(bang_day_du(b))


def tu_dien_cam(b=None):
    b = b or doc_bang()
    return TuDien([[p, ""] for p in b["CHO_PHEP"]] + [[t, t] for t in b["TU_CAM"]])


# ==================== DOC/GHI SQL ====================

def bo_thoat_sql(s):
    """Chuoi trong dump MySQL (\\n, \\', '' ...) -> chu that."""
    ra = []
    i = 0
    while i < len(s):
        c = s[i]
        if c == "\\" and i + 1 < len(s):
            n = s[i + 1]
            ra.append({"n": "\n", "r": "\r", "t": "\t", "0": "\0", "Z": "\x1a"}.get(n, n))
            i += 2
            continue
        if c == "'" and s[i + 1:i + 2] == "'":
            ra.append("'")
            i += 2
            continue
        ra.append(c)
        i += 1
    return "".join(ra)


def thoat_sql(s):
    """Chu that -> chuoi SQL an toan (kieu mysqldump, dung duoc ca khi NO_BACKSLASH_ESCAPES tat)."""
    return (s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")
            .replace("\r", "\\r").replace("\0", "\\0").replace("\x1a", "\\Z"))


def _doc_gia_tri(s, i):
    """Doc mot gia tri tai s[i] (chuoi '...' hoac so/NULL). Tra (gia_tri, la_chuoi, vi_tri_sau)."""
    while s[i] in " \n\r\t":
        i += 1
    if s[i] == "'":
        j = i + 1
        while True:
            c = s[j]
            if c == "\\":
                j += 2
                continue
            if c == "'":
                if s[j + 1:j + 2] == "'":
                    j += 2
                    continue
                break
            j += 1
        return bo_thoat_sql(s[i + 1:j]), True, j + 1
    j = i
    while s[j] not in ",)":
        j += 1
    return s[i:j].strip(), False, j


def doc_insert(s, bang):
    """Moi dong INSERT INTO `bang` trong chuoi s -> list dict {cot: gia_tri} (chu da bo thoat)."""
    ra = []
    for m in re.finditer(r"INSERT INTO `%s` \(([^)]*)\) VALUES\s*" % re.escape(bang), s):
        cot = [c.strip(" `") for c in m.group(1).split(",")]
        i = m.end()
        while True:
            while s[i] in " \n\r\t":
                i += 1
            assert s[i] == "(", s[i:i + 60]
            i += 1
            gt = []
            while True:
                v, _, i = _doc_gia_tri(s, i)
                gt.append(v)
                while s[i] in " \n\r\t":
                    i += 1
                if s[i] == ",":
                    i += 1
                    continue
                i += 1  # ')'
                break
            ra.append(dict(zip(cot, gt)))
            while s[i] in " \n\r\t":
                i += 1
            if s[i] == ",":
                i += 1
                continue
            break
    return ra


_UPDATE = re.compile(r"UPDATE\s+`?(\w+)`?\s+SET\s+(.*?)\s+WHERE\s+`?id`?\s*(=\s*(-?\d+)|IN\s*\(([^)]*)\))\s*;", re.S | re.I)


def doc_update(s):
    """Cac cau UPDATE <bang> SET cot=..., ... WHERE id=N | id IN (...); -> list (bang, [id], {cot: gt})."""
    ra = []
    s = re.sub(r"--[^\n]*", "", s)
    for m in _UPDATE.finditer(s):
        bang, gan = m.group(1), m.group(2)
        ids = [int(m.group(4))] if m.group(4) else [int(x) for x in m.group(5).split(",") if x.strip()]
        cot = {}
        i = 0
        gan = gan + ")"
        while i < len(gan) - 1:
            mm = re.compile(r"\s*`?(\w+)`?\s*=\s*").match(gan, i)
            if not mm:
                break
            v, _, i = _doc_gia_tri(gan, mm.end())
            cot[mm.group(1)] = v
            while i < len(gan) and gan[i] in " ,\n\t":
                i += 1
        ra.append((bang, ids, cot))
    return ra


# Thu tu chay SQL tren server moi (muc 14 cua ke hoach). pokemon_doi_chu.sql chay CUOI.
SQL_POKEMON = [
    "pokemon_du_lieu_quai.sql",
    "pokemon_doi_ten_vat_pham.sql",
    "pokemon_doi_ten_thu.sql",
    "pokemon_vat_pham_eevee.sql",
    "pokemon_icon_vat_pham.sql",
]
SQL_CHU = "pokemon_doi_chu.sql"

# Bang/cot chu nguoi choi thay (phai co cot id).
COT_CHU = {
    "data_potion": ["name"],
    "data_quest": ["name", "content", "scontent", "rescontent", "infogift", "infofinish"],
    "data_shop": ["name", "decript"],
    "data_item": ["name"],
    "data_monster": ["name"],
    "data_attribute": ["name"],
    "tob_fruit": ["name", "decript"],
}


def nap_csdl(cac_file_sql):
    """Trang thai CSDL sau dump + cac file SQL (theo thu tu). {bang: {id: dict}}."""
    dump = open(F_DUMP, encoding="utf8").read()
    db = {}
    for bang in COT_CHU:
        db[bang] = {}
        for r in doc_insert(dump, bang):
            db[bang][int(r["id"])] = r
    da_sua = set()  # (bang, id, cot) bi file SQL khac sua
    for f in cac_file_sql:
        p = os.path.join(GOC, "sql", f)
        if not os.path.exists(p):
            continue
        s = open(p, encoding="utf8").read()
        for bang in COT_CHU:
            for r in doc_insert(s, bang):
                db[bang][int(r["id"])] = r
                for c in COT_CHU[bang]:
                    da_sua.add((bang, int(r["id"]), c))
        for bang, ids, cot in doc_update(s):
            if bang not in db:
                continue
            for i in ids:
                if i in db[bang]:
                    db[bang][i].update(cot)
                for c in cot:
                    da_sua.add((bang, i, c))
    return db, da_sua
