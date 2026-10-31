#!/usr/bin/env python3
"""
P2 - kiem tai nguyen: khong con hinh cua may chu cu (game kiem hiep goc) va khong thieu tai nguyen.

  python3 -I tools/pokemon/kiem_tai_nguyen_cu.py            kiem (ma thoat 1 neu co loi), in tom tat
  python3 -I tools/pokemon/kiem_tai_nguyen_cu.py --ds FILE  ghi them danh sach day du moi id / tep da kiem
  python3 -I tools/pokemon/kiem_tai_nguyen_cu.py --sinh     dung lai du_lieu/tai_nguyen_cu.txt tu lich su git

1. HINH CU. du_lieu/tai_nguyen_cu.txt = dau van tay (16 ky tu hex dau cua SHA-1) cua MOI tep tai nguyen trong
   cac ban chup TRUOC khi chuyen sang Pokemon (cay git 9767a5ab~1, client_jar/res luc 1b85ef58, APK 9371ee19,
   Unity + docs/client/goc luc 3e954f2f), them dau van tay cua TUNG anh ben trong goi (FilePack .sh / .wp /
   .bytes, dinh dang "c" u16+PNG, PNG tach doi <k>_h + data). Moi tep hinh dang phat hanh (client_jar/res,
   res_nhe, shared/brand, Unity Resources + Icon, APK res, client_pc/res, server/map, web/public) trung mot
   dau van tay -> LOI, tru khi khop mot dong cua du_lieu/tai_nguyen_cu_giu.csv (giu co ly do: hieu ung chien
   dau chu game cho giu, khung giao dien chung, phong chu...). Tep chi chua du lieu (bang khung, luoi quai,
   _data) khong phai hinh nen khong tinh.
2. THIEU. Moi id hinh duoc tham chieu phai co tep:
   - dex Pokemon trong du_lieu/bang_{quai,thu,npc,npc_su_kien,pet_cu}.csv + bang client (NPC, Npc_Server,
     AnimalMove) -> sprite Unity Resources/poke/<k>_dl.bytes va jar res/pk/<k>.dl;
   - data_item.xstart (sql/htbb.sql phan A, sau sql/pokemon_icon_vat_pham.sql) -> server/map/icon_pokemon/<n>.png
     (14000+) hoac server/map/icon/<id>.png;
   - mau pet (Map.petTemplate idImg) -> server/map/pet/<idImg>.png; id d_effect gui trong goi 26
     (MessageCreator.id_eff_pet_tool / id_eff_tool_ngu_long) -> server/map/d_effect/<id> + <id>.png;
   - duong dan tai nguyen viet cung trong ma client jar ("/xxx.png", "/xxx.sh") -> client_jar/res.
"""
import csv
import fnmatch
import hashlib
import os
import re
import struct
import subprocess
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DS_CU = os.path.join(GOC, "du_lieu", "tai_nguyen_cu.txt")
DS_GIU = os.path.join(GOC, "du_lieu", "tai_nguyen_cu_giu.csv")

# Ban chup cua may chu cu: (commit, thu muc gioi han hoac None = ca cay)
BAN_CHUP_CU = [("9767a5ab~1", None), ("1b85ef58", "client_jar/res"), ("9371ee19", "client_apk"),
               ("3e954f2f", "client_pc_ios"), ("3e954f2f", "docs/client")]
DUOI_MA = re.compile(r"\.(java|md|sql|cs|meta|txt|json|xml|php|js|css|html|bat|ini|gradle|properties|py|asset|unity|"
                     r"prefab|mat|asmdef|cfg|csproj|sln|dll|pdb|jar|class|smali|yml|yaml|h|c|cpp|mjs|ts|vue|lock|"
                     r"gitignore|gitattributes|example|service|tmpl|sh~)$", re.I)

# Noi tai nguyen hinh duoc phat hanh / server gui di
THU_MUC_PHAT_HANH = ["client_jar/res", "client_jar/res_nhe", "client_jar/shared/brand", "client_jar/shared/banner",
                     "client_pc_ios/Assets/Resources", "client_pc_ios/Assets/Icon", "client_apk/pokemon/src/main/res",
                     "client_pc/res", "server/map", "web/public"]

CODE = bytes([78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104])


def p(*a):
    return os.path.join(GOC, *a)


def dau(b):
    return hashlib.sha1(b).hexdigest()[:16]


def dau_blob(b):
    return hashlib.sha1(b"blob %d\0" % len(b) + b).hexdigest()[:16]


# ------------------------------------------------------------------ doc goi

def xr(b):
    return bytes(c ^ CODE[i % len(CODE)] for i, c in enumerate(b))


def doc_goi(b):
    """FilePack (jar .sh / .wp / goi khong duoi, Unity .bytes): [(ten, byte)] hoac None."""
    try:
        i = 0
        n = b[i]
        i += 1
        ent = []
        for _ in range(n):
            ln = b[i]
            i += 1
            name = xr(b[i:i + ln]).decode("latin1")
            i += ln
            L = struct.unpack(">H", b[i:i + 2])[0]
            i += 2
            ent.append((name, L))
        if n == 0 or i + sum(L for _, L in ent) != len(b):
            return None
        if not all(re.match(r"^[\w./ -]+$", name) for name, _ in ent):
            return None
        data = xr(b[i:])
        out = []
        q = 0
        for name, L in ent:
            out.append((name, data[q:q + L]))
            q += L
        return out
    except Exception:
        return None


def anh_trong(b):
    """Cac khoi hinh ben trong mot tep: [(ten, byte)]; [] = khong phai tep hinh (du lieu khung...)."""
    if b[:8] == b"\x89PNG\r\n\x1a\n":
        return [("", b)]
    if len(b) > 8 and b[2:6] == b"\x89PNG":            # dinh dang "c": u16 + PNG + u16 + du lieu
        n = struct.unpack(">H", b[:2])[0]
        return [("c", b[2:2 + n])]
    g = doc_goi(b)
    if g:
        kq = [(t, x) for t, x in g if x[:4] == b"\x89PNG" or t.endswith(".png")]
        hs = sorted(t for t, _ in g if t.endswith("_h"))
        d = dict(g)
        if hs and "data" in d:
            kq += [(h, d[h] + d["data"]) for h in hs]   # PNG tach doi trong goi
        return kq
    for duoi in (b"IHDR",):                            # dau PNG tach doi dung mot minh (<k>_h, *_header)
        if b[:8] == b"\x89PNG\r\n\x1a\n" and duoi in b[:16]:
            return [("", b)]
    return []


# ------------------------------------------------------------------ danh sach cu

def git(*a, nhi_phan=False):
    r = subprocess.run(["git", "-C", GOC] + list(a), stdout=subprocess.PIPE, check=True)
    return r.stdout if nhi_phan else r.stdout.decode("utf8", "replace")


def sinh():
    blob = {}
    for commit, tm in BAN_CHUP_CU:
        lenh = ["ls-tree", "-r", commit] + ([tm] if tm else [])
        for dong in git(*lenh).splitlines():
            meta, ten = dong.split("\t", 1)
            _, kieu, sha = meta.split()
            if kieu == "blob" and not DUOI_MA.search(ten):
                blob[sha] = ten
    dauv = set()
    for sha in sorted(blob):
        b = git("cat-file", "blob", sha, nhi_phan=True)
        dauv.add(sha[:16])
        for _, x in anh_trong(b):
            dauv.add(dau(x))
    with open(DS_CU, "w", encoding="utf8", newline="\n") as f:
        f.write("# P2: dau van tay (16 hex dau SHA-1) cua tai nguyen may chu cu - sinh boi "
                "tools/pokemon/kiem_tai_nguyen_cu.py --sinh. Dong = blob git cua ca tep hoac SHA-1 mot anh trong goi.\n")
        for d in sorted(dauv):
            f.write(d + "\n")
    print("Da ghi %s: %d dau van tay tu %d tep cu" % (os.path.relpath(DS_CU, GOC), len(dauv), len(blob)))


# ------------------------------------------------------------------ kiem

def doc_giu():
    giu = []
    with open(DS_GIU, encoding="utf8") as f:
        for r in csv.DictReader(f):
            giu.append((r["mau"], r["nhom"], r["ly_do"]))
    return giu


def khop_giu(duong, giu):
    for mau, nhom, _ in giu:
        if fnmatch.fnmatch(duong, mau):
            return nhom
    return None


def kiem_hinh_cu(loi, ds):
    cu = set(l.strip() for l in open(DS_CU, encoding="utf8") if l.strip() and not l.startswith("#"))
    giu = doc_giu()
    dung_giu = {}
    so_tep = 0
    so_anh = 0
    for tm in THU_MUC_PHAT_HANH:
        for goc, _, tep in os.walk(p(tm)):
            for t in sorted(tep):
                d = os.path.join(goc, t)
                rel = os.path.relpath(d, GOC).replace(os.sep, "/")
                if t.endswith(".meta"):
                    continue
                b = open(d, "rb").read()
                khoi = anh_trong(b)
                if not khoi:
                    continue
                so_tep += 1
                so_anh += len(khoi)
                ca_tep = dau_blob(b) in cu
                cu_trong = [k for k, x in khoi if dau(x) in cu]
                if not ca_tep and not cu_trong:
                    ds.append("moi\t" + rel)
                    continue
                # ca tep cu: can dong giu cho ca tep; anh cu trong goi: dong giu "tep#anh" hoac ca tep
                nhom_tep = khop_giu(rel, giu)
                sai = []
                if nhom_tep:
                    dung_giu[nhom_tep] = dung_giu.get(nhom_tep, 0) + 1
                elif ca_tep and len(khoi) == 1:
                    sai = ["(ca tep)"]
                else:
                    for k in (cu_trong or [k for k, _ in khoi]):
                        nh = khop_giu(rel + "#" + k, giu)
                        if nh:
                            dung_giu[nh] = dung_giu.get(nh, 0) + 1
                        else:
                            sai.append(k or "(ca tep)")
                if sai:
                    loi.append("hinh cu %s %s" % (rel, ",".join(sai)[:120]))
                    ds.append("CU\t" + rel + "\t" + ",".join(sai))
                else:
                    ds.append("giu\t" + rel)
    return so_tep, so_anh, dung_giu


def khoa_sprite(dex, shiny):
    return "%04d%s" % (dex, "s" if shiny else "")


def kiem_thieu(loi, ds):
    dem = {}
    # a) dex
    dex = set()
    for ten in ("bang_quai.csv", "bang_thu.csv", "bang_npc.csv", "bang_npc_su_kien.csv", "bang_pet_cu.csv"):
        with open(p("du_lieu", ten), encoding="utf8") as f:
            for r in csv.DictReader(f):
                if (r.get("dex") or "").strip().isdigit() and int(r["dex"]) > 0:
                    dex.add((int(r["dex"]), int(r.get("shiny") or 0)))
    for ma in ("client_jar/src/game/model/NPC.java", "client_jar/src/game/model/Npc_Server.java",
               "client_jar/src/game/model/AnimalMove.java"):
        s = open(p(ma), encoding="utf8").read()
        m = re.search(r"POKE_DEX_THEO_TYPE = \{([^}]*)\}", s)
        if m:
            dex |= {(int(x), 0) for x in re.findall(r"\d+", m.group(1)) if int(x) > 0}
        for m in re.finditer(r"\{(\d+), (\d+)\}", s[s.find("POKE_DEX_"):] if "POKE_DEX_" in s else ""):
            dex.add((int(m.group(2)), 0))
    for d, sh in sorted(dex):
        k = khoa_sprite(d, sh)
        ok = os.path.isfile(p("client_pc_ios/Assets/Resources/poke", k + "_dl.bytes")) and os.path.isfile(p("client_jar/res/pk", k + ".dl"))
        ds.append("dex\t%s\t%s" % (k, "co" if ok else "THIEU"))
        if not ok:
            loi.append("thieu sprite %s (Unity poke/%s_dl.bytes + jar res/pk/%s.dl)" % (k, k, k))
    dem["dex"] = len(dex)
    # b) icon vat pham
    sql = open(p("sql", "htbb.sql"), encoding="utf8").read().split("\n-- ######## B. NANG CAP")[0]  # phan A
    i = sql.find("INSERT INTO `data_item`")
    khoi = sql[i:sql.find(";\n", i)]
    xstart = {}
    for m in re.finditer(r"\((\d+),'(?:[^'\\]|\\.)*',(?:-?\d+,){18}(-?\d+),", khoi):
        xstart[int(m.group(1))] = int(m.group(2))
    for m in re.finditer(r"UPDATE data_item SET xstart=(\d+) WHERE id (?:IN \(([\d,]+)\)|=(\d+))",
                         open(p("sql", "pokemon_icon_vat_pham.sql"), encoding="utf8").read()):
        for id_ in (m.group(2) or m.group(3)).split(","):
            xstart[int(id_)] = int(m.group(1))
    for id_, x in sorted(xstart.items()):
        f = ("server/map/icon_pokemon/%d.png" % (x - 14000)) if 14000 <= x < 15000 else ("server/map/icon/%d.png" % x)
        ok = os.path.isfile(p(f))
        ds.append("icon\t%d\t%d\t%s" % (id_, x, "co" if ok else "THIEU"))
        if not ok:
            loi.append("item %d xstart %d -> thieu %s" % (id_, x, f))
    dem["icon vat pham"] = len(xstart)
    # c) mau pet + d_effect goi 26
    mp = open(p("server/src/real/Map.java"), encoding="utf8").read()
    pet = sorted({int(m.group(1)) for m in re.finditer(r"new PetTemplate\(\(byte\) \d+, \"[^\"]*\", \d+, \(byte\) (\d+),", mp)})
    for i in pet:
        f = "server/map/pet/%d.png" % i
        if not os.path.isfile(p(f)):
            loi.append("mau pet idImg %d -> thieu %s" % (i, f))
        ds.append("pet\t%d" % i)
    mc = open(p("server/src/real/MessageCreator.java"), encoding="utf8").read()
    eff = set()
    for ten in ("id_eff_pet_tool", "id_eff_tool_ngu_long"):
        m = re.search(r"MessageCreator\.%s = new int\[\]\{([^}]*)\}" % ten, mc)
        if m:
            eff |= {int(x) for x in re.findall(r"\d+", m.group(1))}
    for i in sorted(eff):
        for f in ("server/map/d_effect/%d" % i, "server/map/d_effect/%d.png" % i):
            if not os.path.isfile(p(f)):
                loi.append("d_effect %d (goi 26) -> thieu %s" % (i, f))
        ds.append("d_effect\t%d" % i)
    dem["mau pet"] = len(pet)
    dem["d_effect goi 26"] = len(eff)
    # d) duong dan cung trong ma jar
    duong = set()
    for goc, _, tep in os.walk(p("client_jar/src")):
        for t in tep:
            if t.endswith(".java"):
                for m in re.finditer(r'"(/[^"\s]+\.(?:png|sh|wp|txt|type|bin|dl))"', open(os.path.join(goc, t), encoding="utf8").read()):
                    duong.add(m.group(1))
    # Duong dan da biet la khong can tep (co tu truoc P2, khong phai thieu that):
    #   /newEf/  chi ban Android / Unity co (J2ME bo qua); /t6.png cay le hoi tai tu server;
    #   /c/1_*.png CharPartInfo.loadtest (ham thu, khong noi nao goi); /type.sh hang FilePack.type khong dung;
    #   /m/bosshothanh.png khoi static BossAegislash (bat IOException; boss ve bang PokeBody truoc).
    bo_qua = ("/newEf/", "/t6.png", "/c/1_body.png", "/c/1_head.png", "/c/1_leg.png", "/type.sh", "/m/bosshothanh.png")
    for d in sorted(duong):
        if d.startswith(bo_qua):
            ds.append("jar\t%s\tbo qua (xem ghi chu)" % d)
            continue
        ok = os.path.isfile(p("client_jar/res" + d))
        ds.append("jar\t%s\t%s" % (d, "co" if ok else "THIEU"))
        if not ok:
            loi.append("ma jar doc %s nhung client_jar/res khong co" % d)
    dem["duong dan jar"] = len(duong)
    return dem


def main():
    if "--sinh" in sys.argv:
        sinh()
        return 0
    loi = []
    ds = []
    so_tep, so_anh, giu = kiem_hinh_cu(loi, ds)
    dem = kiem_thieu(loi, ds)
    if "--ds" in sys.argv:
        with open(sys.argv[sys.argv.index("--ds") + 1], "w", encoding="utf8") as f:
            f.write("\n".join(ds) + "\n")
    print("Hinh da kiem: %d tep (%d anh); giu co ly do: %s" % (so_tep, so_anh,
          ", ".join("%s %d" % kv for kv in sorted(giu.items())) or "0"))
    print("Tham chieu da kiem: " + ", ".join("%s %d" % kv for kv in dem.items()))
    for l in loi[:200]:
        print("LOI", l)
    print("LOI %d" % len(loi))
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
