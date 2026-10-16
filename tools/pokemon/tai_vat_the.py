#!/usr/bin/env python3
"""
Tai hinh vat the PMDCollab/RawAsset (Object/, Item/) cho cac quai phan_loai=vat_the trong
docs/pokemon/bang_quai.csv va cac NPC su kien ve bang do vat (viec G7.B1/B2 trong
docs/pokemon/KE_HOACH_CHUYEN_DOI.md muc 15).

Dinh dang RawAsset (giong Particle, xem tai_hieu_ung_pmd.py): <Ten>.<Kieu>.png
  None / Flip : 1 hang khung VUONG canh H, so khung = W/H
  <n>         : n khung xep ngang, khung = (W/n) x H
Do vat dung o day phan lon chi 1 khung; co nhieu khung (Portal.6) thi giu nguyen dai ngang,
client cat khung theo so_khung (bang VatThePokemon.cs).

Ra (client Unity, Resources/poke/):
  vt_<id>.bytes      PNG RGBA cho quai template <id> (vd vt_83.bytes = cong thanh)
  vt_npc<idImg>.bytes PNG cho NPC su kien (lenh -50) ve bang do vat
  GHI_CONG_VAT_THE.txt ghi cong (file rieng: DongGoiSprite ghi lai GHI_CONG.txt tu dau)
  docs/pokemon/bang_vat_the.csv  bang doi chieu (commit). test/KiemHinhPokemon so bang nay
                                 voi bang C# trong ClientUnity/Assets/Scripts/Game/VatThePokemon.cs

Chay (o thu muc KPAH/):
    python3 -I tools/pokemon/tai_vat_the.py            # tai + dong goi
    python3 -I tools/pokemon/tai_vat_the.py --khong-tai # chi dong goi tu file da tai

GIAY PHEP: RawAsset KHONG co file LICENSE; trich tu PMD goc (Chunsoft/Nintendo). Chi dung phi
thuong mai (Q10). File tai ve nam o tools/pokemon/pmd_raw_vat_the/ (KHONG commit, .gitignore).

Ma thoat 1 neu thieu quai vat_the nao trong bang hoac tai/doc loi.
"""
import csv
import os
import sys
import urllib.error
import urllib.request

THU_MUC_TOOL = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, THU_MUC_TOOL)
from tai_hieu_ung_pmd import doc_png, ghi_png  # noqa: E402  (PNG chi dung stdlib)

GOC = os.path.dirname(os.path.dirname(THU_MUC_TOOL))
RAW = os.path.join(THU_MUC_TOOL, "pmd_raw_vat_the")
BANG_QUAI = os.path.join(GOC, "docs", "pokemon", "bang_quai.csv")
BANG_VT = os.path.join(GOC, "docs", "pokemon", "bang_vat_the.csv")
RA = os.path.join(GOC, "ClientUnity", "Assets", "Resources", "poke")
GHI_CONG = os.path.join(RA, "GHI_CONG_VAT_THE.txt")

NGUON = "https://raw.githubusercontent.com/PMDCollab/RawAsset/master/"

# Cong thanh: tuong khoi da 7 x 2 o (24 px). Hang tren toan khoi tron, hang duoi co khoi
# huy hieu hai ben va khoi khoa (Vault) o giua - nhin nhu cong dong.
_TRON, _HUY, _KHOA = "Object/Block_Blank.None", "Object/Block_Guild.None", "Object/Block_Vault.None"
CONG = [[_TRON] * 7, [_TRON, _TRON, _HUY, _KHOA, _HUY, _TRON, _TRON]]

# Ngoc rong n sao: ngoc cam (Orb_Tan) + n cham do kieu mat xuc xac (toa do trong anh 16 px).
CHAM = {
    1: [(7, 7)],
    2: [(5, 5), (9, 9)],
    3: [(5, 5), (7, 7), (9, 9)],
    4: [(5, 5), (9, 5), (5, 9), (9, 9)],
    5: [(5, 5), (9, 5), (7, 7), (5, 9), (9, 9)],
    6: [(5, 5), (9, 5), (5, 7), (9, 7), (5, 9), (9, 9)],
    7: [(5, 5), (9, 5), (5, 7), (7, 7), (9, 7), (5, 9), (9, 9)],
}

# khoa -> (loai, id, ten_cu, cach_ve, ti_le%, ghi_chu)
#   cach_ve: ("anh", "Thu_muc/Ten.Kieu")            1 file
#            ("anh_tim", "...")                      doi mau xanh la -> tim (ruong ma quai)
#            ("ngoc", n)                             Orb_Tan + n cham
#            ("cong",)                               ghep CONG
VAT_THE = {
    "vt_83": ("quai", 83, "cổng thành", ("cong",), 200, "Tường khối đá + khối khoá ở giữa"),
    "vt_85": ("quai", 85, "Đá", ("anh", "Item/Rock_Gray.None"), 200, "Mỏ đá"),
    "vt_86": ("quai", 86, "Bông", ("anh", "Item/Silk_White.None"), 200, "Bông = tơ trắng"),
    "vt_87": ("quai", 87, "Gỗ", ("anh", "Object/Logs_Stacked.Flip"), 100, "Đống gỗ"),
    "vt_88": ("quai", 88, "Da", ("anh", "Item/Belt_Brown.None"), 200, "Da = đai da nâu"),
    "vt_89": ("quai", 89, "Sắt", ("anh", "Item/Iron_Ball.None"), 200, "Sắt = bi sắt"),
    "vt_119": ("quai", 119, "cong thanh", ("cong",), 200, "Giống 83"),
    "vt_121": ("quai", 121, "Rương ma quái", ("anh_tim", "Object/Chest.None"), 100, "Rương PMD đổi màu tím"),
    "vt_122": ("quai", 122, "Ngọc 1 sao", ("ngoc", 1), 200, "Orb cam + 1 chấm"),
    "vt_123": ("quai", 123, "Ngọc 2 sao", ("ngoc", 2), 200, "Orb cam + 2 chấm"),
    "vt_124": ("quai", 124, "Ngọc 3 sao", ("ngoc", 3), 200, "Orb cam + 3 chấm"),
    "vt_125": ("quai", 125, "Ngọc 4 sao", ("ngoc", 4), 200, "Orb cam + 4 chấm"),
    "vt_126": ("quai", 126, "Ngọc 5 sao", ("ngoc", 5), 200, "Orb cam + 5 chấm"),
    "vt_127": ("quai", 127, "Ngọc 6 sao", ("ngoc", 6), 200, "Orb cam + 6 chấm"),
    "vt_128": ("quai", 128, "Ngọc 7 sao", ("ngoc", 7), 200, "Orb cam + 7 chấm"),
    "vt_131": ("quai", 131, "Thùng gỗ", ("anh", "Item/Box_Brown.None"), 200, "Thùng gỗ = hộp nâu"),
    # NPC su kien (lenh -50) la do vat, khong phai nhan vat -> do vat PMD (idImg, xem bang_npc_su_kien.csv)
    "vt_npc13": ("npc_su_kien", 13, "Cổng dịch chuyển", ("anh", "Object/Portal.6"), 100, "Cổng PMD 6 khung"),
    "vt_npc15": ("npc_su_kien", 15, "Khu N (bảng khu)", ("anh", "Object/Sign.None"), 150, "Biển gỗ"),
    "vt_npc25": ("npc_su_kien", 25, "Nồi bánh chưng", ("anh", "Object/Pot.Flip"), 250, "Hũ PMD"),
    "vt_npc26": ("npc_su_kien", 26, "Nồi bánh tét", ("anh", "Object/Pot.Flip"), 250, "Hũ PMD"),
}


def tai(ten, dich):
    if os.path.exists(dich):
        return True
    req = urllib.request.Request(NGUON + ten + ".png", headers={"User-Agent": "kpah-pokemon-tool"})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            data = r.read()
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return False
        raise
    os.makedirs(os.path.dirname(dich), exist_ok=True)
    with open(dich, "wb") as f:
        f.write(data)
    return True


def so_khung(ten, w, h):
    kieu = ten.rsplit(".", 1)[1]
    if kieu.isdigit():
        return int(kieu)
    if kieu in ("None", "Flip"):
        return max(1, w // h)
    raise ValueError("kieu %s chua ho tro" % kieu)


def nap(ten, khong_tai):
    p = os.path.join(RAW, ten + ".png")
    if not khong_tai and not tai(ten, p):
        raise ValueError("khong tai duoc %s" % ten)
    if not os.path.exists(p):
        raise ValueError("chua tai %s" % ten)
    return doc_png(open(p, "rb").read())


def doi_tim(hang):
    """Xanh la -> tim: (r, g, b) -> (g, r, g). Mau xam/den/trang giu nguyen."""
    for d in hang:
        for x in range(0, len(d), 4):
            r, g, b = d[x], d[x + 1], d[x + 2]
            if g > r + 20 and g > b + 20:
                d[x], d[x + 1], d[x + 2] = g, r, g
    return hang


def ve(cach, khong_tai, nguon_dung):
    """-> (rong, cao, hang, so_khung)."""
    if cach[0] in ("anh", "anh_tim"):
        w, h, hang = nap(cach[1], khong_tai)
        nguon_dung.add(cach[1])
        if cach[0] == "anh_tim":
            hang = doi_tim(hang)
        return w, h, hang, so_khung(cach[1], w, h)
    if cach[0] == "ngoc":
        ten = "Item/Orb_Tan.None"
        w, h, hang = nap(ten, khong_tai)
        nguon_dung.add(ten)
        for (x, y) in ((x + 1, y + 2) for (x, y) in CHAM[cach[1]]):  # tranh vet sang goc tren trai
            hang[y][x * 4:x * 4 + 4] = bytes((200, 24, 24, 255))
        return w, h, hang, 1
    if cach[0] == "cong":
        o = 24
        W, H = o * len(CONG[0]), o * len(CONG)
        ra = [bytearray(W * 4) for _ in range(H)]
        for r, dong in enumerate(CONG):
            for c, ten in enumerate(dong):
                w, h, hang = nap(ten, khong_tai)
                nguon_dung.add(ten)
                if (w, h) != (o, o):
                    raise ValueError("%s khong phai %dx%d" % (ten, o, o))
                for y in range(o):
                    ra[r * o + y][c * o * 4:(c + 1) * o * 4] = hang[y]
        return W, H, ra, 1
    raise ValueError("cach ve la %r" % (cach,))


def ghi_cong(nguon):
    with open(GHI_CONG, "w", encoding="utf8") as f:
        f.write("Hinh vat the (vt_*.bytes): PMDCollab RawAsset, thu muc Object/ va Item/ - "
                "https://github.com/PMDCollab/RawAsset\n"
                "RawAsset KHONG co file LICENSE: trich tu PMD goc (Chunsoft/Nintendo) / PMDCollab "
                "RawAsset, chi dung PHI THUONG MAI.\n"
                "Pokemon (c) Nintendo / Game Freak / The Pokemon Company.\n"
                "Sinh boi tools/pokemon/tai_vat_the.py. File goc: " + ", ".join(sorted(nguon)) + "\n")


def main():
    khong_tai = "--khong-tai" in sys.argv
    os.makedirs(RA, exist_ok=True)
    loi = 0
    with open(BANG_QUAI, encoding="utf8") as f:
        can = {int(r["id_quai"]): r["ten_cu"] for r in csv.DictReader(f) if r["phan_loai"] == "vat_the"}
    co = {v[1] for v in VAT_THE.values() if v[0] == "quai"}
    for id_quai in sorted(set(can) - co):
        loi += 1
        print("LOI: quai vat_the %d (%s) chua co trong VAT_THE" % (id_quai, can[id_quai]))
    dong = []
    nguon_dung = set()
    for khoa, (loai, id_, ten_cu, cach, ti_le, ghi_chu) in sorted(VAT_THE.items(), key=lambda kv: (kv[1][0] != "quai", kv[1][1])):
        try:
            w, h, hang, n = ve(cach, khong_tai, nguon_dung)
        except (ValueError, OSError, KeyError) as e:
            loi += 1
            print("LOI %s: %s" % (khoa, e))
            continue
        with open(os.path.join(RA, khoa + ".bytes"), "wb") as f:
            f.write(ghi_png(w, h, hang))
        nguon = {"anh": cach[-1], "anh_tim": cach[-1], "ngoc": "Item/Orb_Tan.None"}.get(cach[0], "Object/Block_*.None")
        dong.append([loai, id_, ten_cu, nguon, khoa + ".bytes", w // n, h, n, ti_le, ghi_chu])
        print("%-10s %-26s %3dx%-3d x%d %d%%" % (khoa, nguon, w // n, h, n, ti_le))
    with open(BANG_VT, "w", encoding="utf8", newline="") as f:
        wr = csv.writer(f)
        wr.writerow(["loai", "id", "ten_cu", "nguon_rawasset", "file_client", "rong_khung", "cao", "so_khung", "ti_le", "ghi_chu"])
        wr.writerows(dong)
    ghi_cong(nguon_dung)
    print("Da ghi %d anh, %s; loi: %d" % (len(dong), os.path.relpath(BANG_VT, GOC), loi))
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
