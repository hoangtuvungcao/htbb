#!/usr/bin/env python3
"""
Tai hieu ung chieu (particle) PMDCollab/RawAsset cho MOI chieu trong du_lieu/bang_chieu.csv
va dong goi cho client (viec K3 trong docs/kien-truc.md).

Dinh dang file RawAsset/Particle (doc tu RogueEssence/Content/DirSheet.cs, ham Import):
  Ten file = <Ten>.<Kieu>.png, <Kieu> quyet dinh cach cat khung:
    <n>      (vd Brine.45)     : n khung xep ngang, khung = (W/n) x H (co the khong vuong)
    <a>x<b>                    : luoi a cot x b hang, khung = (W/a) x (H/b)
    None                       : 1 hang khung VUONG canh H, so khung = W/H, khong huong
    Flip                       : nhu None; huong Up/UpRight/Right/DownRight thi lat ngang
    Dir1                       : nhu None; ve XOAY theo huong (dir*45 do, chieu kim dong ho
                                 tren man hinh), anh goc huong xuong (Down)
    Dir2                       : 2 hang, canh = H/2; hang 0 = huong thang (xoay (dir/2)*90 do),
                                 hang 1 = huong cheo
    Dir5                       : 5 hang (Down, DownLeft, Left, UpLeft, Up), canh = H/5;
                                 3 huong con lai = lat ngang hang (8 - dir)
    Dir8                       : 8 hang (Down, DownLeft, Left, UpLeft, Up, UpRight, Right,
                                 DownRight), canh = H/8
  Thoi luong khung KHONG nam trong file (PMDC dat trong du lieu chieu); ta dung so tick co dinh.

Dong goi (client Unity, Resources/poke/):
  hu_<ten>.bytes  PNG RGBA. Co huong (soHuong = 4): 4 hang theo thu tu client DOWN(0), UP(1),
                  LEFT(2), RIGHT(3) (giong atlas PokeBody), da xoay/lat san, moi hang soKhung khung.
                  Khong huong (soHuong = 1): khung i o cot i % soCot, hang i / soCot (dai qua
                  2048 px thi xuong dong).
  hu_bang.bytes   bang khung, van ban UTF-8, dong '#' la chu thich, moi dong:
                  lop,chieu,ten,rong,cao,soKhung,soHuong,soCot,kieuVe,tickMoiKhung
                  kieuVe: D = no tai muc tieu, B = bay tu nguoi danh toi muc tieu,
                          T = tren nguoi dung chieu (chieu bo tro).
  du_lieu/bang_hieu_ung.csv  bang doi chieu (commit).

Chay (o thu muc HTBB/):
    python3 -I tools/pokemon/tai_hieu_ung_pmd.py            # tai + dong goi
    python3 -I tools/pokemon/tai_hieu_ung_pmd.py --khong-tai # chi dong goi tu file da tai

GIAY PHEP: RawAsset KHONG co file LICENSE; phan lon trich tu game PMD goc (Chunsoft/Nintendo).
Chi dung phi thuong mai. Ghi cong duoc ghi vao Resources/poke/GHI_CONG.txt (khoi danh dau).

Ma thoat 1 neu co chieu trong bang_chieu.csv khong co particle (hoac tai/doc loi).
"""
import csv
import os
import struct
import sys
import urllib.error
import urllib.request
import zlib

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RAW = os.path.join(GOC, "tools", "pokemon", "pmd_raw_hieu_ung")
BANG_CHIEU = os.path.join(GOC, "du_lieu", "bang_chieu.csv")
BANG_HU = os.path.join(GOC, "du_lieu", "bang_hieu_ung.csv")
import importlib.util as _iu
_sd = _iu.spec_from_file_location("duong_client", os.path.join(os.path.dirname(os.path.abspath(__file__)), "duong_client.py"))
dc = _iu.module_from_spec(_sd)
_sd.loader.exec_module(dc)
# Client Unity: client_pc_ios/Assets (doi bang HTBB_CLIENT_ASSETS).
RA = dc.resources(dc.assets(), "poke")
GHI_CONG = os.path.join(RA, "GHI_CONG.txt")

NGUON = "https://raw.githubusercontent.com/PMDCollab/RawAsset/master/Particle"

# (lop, chieu) -> (file particle trong RawAsset/Particle, kieuVe)
# D = tai muc tieu, B = bay toi muc tieu, T = tren nguoi dung chieu
CHON = {
    # 0 Charmander
    (0, 0): ("Scratch.Dir1", "D"),            # Cao
    (0, 1): ("Ember.None", "B"),              # Than hong
    (0, 2): ("Fire_Fang_Fang.None", "D"),     # Nanh lua
    (0, 3): ("Fire_Spin_Front.None", "D"),    # Xoay lua
    (0, 4): ("Smoke_Black.None", "T"),        # Man khoi
    (0, 5): ("Swords_Dance.None", "T"),       # Mua kiem
    (0, 6): ("Flamethrower.None", "D"),       # Sung phun lua
    (0, 7): ("Shadow_Claw.None", "D"),        # Vuot rong
    (0, 8): ("Fire_Blast.None", "D"),         # Dai hoa
    # 1 Bulbasaur
    (1, 0): ("Hit_Neutral.None", "D"),        # Huc
    (1, 1): ("Vine_Whip.Dir1", "D"),          # Roi day leo
    (1, 2): ("Razor_Leaf.None", "B"),         # La dao
    (1, 3): ("Mud_Bomb_Ball.None", "B"),      # Bom bun
    (1, 4): ("Protect.None", "T"),            # Tu ve
    (1, 5): ("Stat_Green_Ring.None", "T"),    # Sinh truong
    (1, 6): ("Seed_Bomb_Blast.None", "D"),    # Bom hat
    (1, 7): ("Leaf_Storm_Hit.None", "D"),     # Bao la
    (1, 8): ("Charge_Beam_Hit.None", "D"),    # Tia mat troi
    # 2 Eevee
    (2, 0): ("Hit_Neutral.None", "D"),        # Huc
    (2, 1): ("Hit_Super_Effective.None", "D"),  # Tan cong nhanh
    (2, 2): ("Bite.Dir8", "D"),               # Can
    (2, 3): ("Swift_Star.None", "B"),         # Sao bang
    (2, 4): ("Wish.None", "T"),               # Mong uoc
    (2, 5): ("Sleep_Z.None", "T"),            # Nghi ngoi
    (2, 6): ("Healing_Wish_Charge.None", "T"),  # Phuoc lanh hoi sinh
    (2, 7): ("Grudge_Lights.None", "T"),      # Chia se noi dau
    (2, 8): ("Iron_Head.None", "D"),          # Duoi sat
    (2, 9): ("Last_Resort_Front.None", "D"),  # But pha cuoi cung
    (2, 10): ("Giga_Impact_Front.None", "D"),  # Tia pha huy
    # 3 Squirtle
    (3, 0): ("Hit_Neutral.None", "D"),        # Huc
    (3, 1): ("Hydro_Pump_RSE.Dir1", "B"),     # Sung nuoc
    (3, 2): ("Bubbles_Blue.None", "B"),       # Bong bong
    (3, 3): ("Brine_Splashes.None", "D"),     # Xoay nuoc
    (3, 4): ("Harden_RSE.None", "T"),         # Pha mai
    (3, 5): ("Withdraw.Flip", "T"),           # Rut vao mai
    (3, 6): ("Aqua_Tail_Splash.None", "D"),   # Duoi nuoc
    (3, 7): ("Water_Column_Ranger.None", "D"),  # Bom nuoc
    (3, 8): ("Hydro_Cannon.None", "B"),       # Dai phao nuoc
    # 4 Pichu
    (4, 0): ("Spark.None", "D"),              # Tia lua dien
    (4, 1): ("Shock_Wave.None", "D"),         # Song dien
    (4, 2): ("Light_Ball_Yellow.None", "B"),  # Cau dien
    (4, 3): ("Discharge_Hit.None", "D"),      # Tia chop
    (4, 4): ("Stat_Blue_Ring.None", "T"),     # Nhanh nhen
    (4, 5): ("Charge_Up.None", "T"),          # Nap dien
    (4, 6): ("Thunder_Fang_Fang.None", "D"),  # Luoi dien
    (4, 7): ("Discharge.None", "D"),          # Sam set
    (4, 8): ("Charge_Beam_Glow.None", "D"),   # Dam sam dien
}

# Huong client (Char.DOWN, UP, LEFT, RIGHT) -> Dir8 cua PMDC (Down=0 ... Left=2, Up=4, Right=6)
HUONG_CLIENT_DIR8 = (0, 4, 2, 6)


# ---------------------------------------------------------------- PNG (chi stdlib)

def doc_png(du_lieu):
    """-> (rong, cao, list hang, moi hang la bytearray RGBA)."""
    if du_lieu[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("khong phai PNG")
    p = 8
    idat = b""
    plte = None
    trns = None
    while p < len(du_lieu):
        n, loai = struct.unpack(">I4s", du_lieu[p:p + 8])
        khoi = du_lieu[p + 8:p + 8 + n]
        p += 12 + n
        if loai == b"IHDR":
            w, h, bd, ct, _, _, xen = struct.unpack(">IIBBBBB", khoi)
        elif loai == b"PLTE":
            plte = khoi
        elif loai == b"tRNS":
            trns = khoi
        elif loai == b"IDAT":
            idat += khoi
        elif loai == b"IEND":
            break
    if xen != 0:
        raise ValueError("PNG xen ke (interlace) chua ho tro")
    so_kenh = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[ct]
    if bd != 8 and not (ct in (0, 3) and bd in (1, 2, 4)):
        raise ValueError("bit depth %d / color type %d chua ho tro" % (bd, ct))
    bpp = max(1, so_kenh * bd // 8)
    dong_byte = (w * so_kenh * bd + 7) // 8
    tho = zlib.decompress(idat)
    hang = []
    truoc = bytearray(dong_byte)
    q = 0
    for _ in range(h):
        loc = tho[q]
        d = bytearray(tho[q + 1:q + 1 + dong_byte])
        q += 1 + dong_byte
        for i in range(dong_byte):
            a = d[i - bpp] if i >= bpp else 0
            b = truoc[i]
            c = truoc[i - bpp] if i >= bpp else 0
            if loc == 1:
                d[i] = (d[i] + a) & 255
            elif loc == 2:
                d[i] = (d[i] + b) & 255
            elif loc == 3:
                d[i] = (d[i] + ((a + b) >> 1)) & 255
            elif loc == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                d[i] = (d[i] + (a if pa <= pb and pa <= pc else (b if pb <= pc else c))) & 255
        truoc = d
        out = bytearray(w * 4)
        if bd < 8:
            moi = 8 // bd
            mat = (1 << bd) - 1
            gt = [(d[x // moi] >> (8 - bd * (x % moi + 1))) & mat for x in range(w)]
        else:
            gt = None
        for x in range(w):
            if ct == 6:
                out[x * 4:x * 4 + 4] = d[x * 4:x * 4 + 4]
            elif ct == 2:
                r, g_, b_ = d[x * 3:x * 3 + 3]
                anpha = 255
                if trns and len(trns) == 6 and struct.unpack(">HHH", trns) == (r, g_, b_):
                    anpha = 0
                out[x * 4:x * 4 + 4] = bytes((r, g_, b_, anpha))
            elif ct == 3:
                k = gt[x] if gt else d[x]
                anpha = trns[k] if trns and k < len(trns) else 255
                out[x * 4:x * 4 + 4] = plte[k * 3:k * 3 + 3] + bytes((anpha,))
            elif ct == 0:
                v = gt[x] * 255 // ((1 << bd) - 1) if gt else d[x]
                out[x * 4:x * 4 + 4] = bytes((v, v, v, 255))
            elif ct == 4:
                v, anpha = d[x * 2:x * 2 + 2]
                out[x * 4:x * 4 + 4] = bytes((v, v, v, anpha))
        hang.append(out)
    return w, h, hang


def ghi_png(w, h, hang):
    tho = b"".join(b"\x00" + bytes(r) for r in hang)

    def khoi(loai, dl):
        return struct.pack(">I", len(dl)) + loai + dl + struct.pack(">I", zlib.crc32(loai + dl) & 0xffffffff)
    return (b"\x89PNG\r\n\x1a\n" + khoi(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
            + khoi(b"IDAT", zlib.compress(tho, 9)) + khoi(b"IEND", b""))


# ---------------------------------------------------------------- cat khung

def cat(hang, x0, y0, rong, cao):
    return [bytearray(hang[y0 + y][x0 * 4:(x0 + rong) * 4]) for y in range(cao)]


def lat_ngang(k):
    out = []
    for r in k:
        n = len(r) // 4
        out.append(bytearray(b"".join(bytes(r[(n - 1 - x) * 4:(n - x) * 4]) for x in range(n))))
    return out


def xoay_90_cw(k):
    """Xoay khung vuong 90 do theo chieu kim dong ho (tren man hinh, y huong xuong)."""
    n = len(k)
    out = [bytearray(n * 4) for _ in range(n)]
    for y in range(n):
        for x in range(n):
            # diem (x, y) -> (n-1-y, x)
            out[x][(n - 1 - y) * 4:(n - y) * 4] = k[y][x * 4:x * 4 + 4]
    return out


def xoay(k, so_lan_90):
    for _ in range(so_lan_90 % 4):
        k = xoay_90_cw(k)
    return k


def tach_khung(ten_file, w, h, hang):
    """-> (rong, cao, soKhung, hangKhung) voi hangKhung = list (1 hoac 4) list khung.
    Theo DirSheet.Import cua RogueEssence."""
    kieu = ten_file.split(".")[-1]
    if kieu.isdigit():
        n = int(kieu)
        rong, cao = w // n, h
        return rong, cao, n, [[cat(hang, i * rong, 0, rong, cao) for i in range(n)]]
    if "x" in kieu and all(p.isdigit() for p in kieu.split("x")):
        a, b = (int(p) for p in kieu.split("x"))
        rong, cao = w // a, h // b
        return rong, cao, a * b, [[cat(hang, (i % a) * rong, (i // a) * cao, rong, cao) for i in range(a * b)]]
    chia = {"None": 1, "Dir1": 1, "Flip": 1, "Dir2": 2, "Dir5": 5, "Dir8": 8}[kieu]
    canh = h // chia
    n = w // canh
    if canh * chia != h or n * canh != w:
        raise ValueError("%s: kich thuoc %dx%d khong chia het khung vuong %d" % (ten_file, w, h, canh))

    def hang_pmd(r):
        return [cat(hang, i * canh, r * canh, canh, canh) for i in range(n)]
    if kieu == "None":
        return canh, canh, n, [hang_pmd(0)]
    ra = []
    for d8 in HUONG_CLIENT_DIR8:
        if kieu == "Dir1":
            ra.append([xoay(k, d8 // 2) for k in hang_pmd(0)])
        elif kieu == "Dir2":
            ra.append([xoay(k, d8 // 2) for k in hang_pmd(d8 % 2)])
        elif kieu == "Flip":
            ra.append([lat_ngang(k) if d8 >= 4 else k for k in hang_pmd(0)])
        elif kieu == "Dir5":
            ra.append([lat_ngang(k) if d8 > 4 else k for k in hang_pmd(8 - d8 if d8 > 4 else d8)])
        else:  # Dir8
            ra.append(hang_pmd(d8))
    return canh, canh, n, ra


RONG_TOI_DA = 2048  # may cu gioi han texture 2048 (xem muc 13 cua ke hoach)


def ghep(rong, cao, hang_khung):
    """-> (W, H, hang anh, soCot). Khong huong ma qua rong thi xuong dong (luoi soCot cot);
    co huong thi moi huong dung 1 hang."""
    n = len(hang_khung[0])
    so_cot = n
    if len(hang_khung) == 1 and rong * n > RONG_TOI_DA:
        so_cot = max(1, RONG_TOI_DA // rong)
        khung = hang_khung[0]
        hang_khung = [khung[i:i + so_cot] for i in range(0, n, so_cot)]
    elif rong * n > RONG_TOI_DA:
        raise ValueError("particle co huong rong %d > %d" % (rong * n, RONG_TOI_DA))
    trong = bytearray(rong * 4)
    out = []
    for khung in hang_khung:
        for y in range(cao):
            out.append(bytearray(b"".join(bytes(khung[i][y]) if i < len(khung) else bytes(trong)
                                          for i in range(so_cot))))
    return rong * so_cot, cao * len(hang_khung), out, so_cot


# ---------------------------------------------------------------- chinh

def tai(url, dich):
    if os.path.exists(dich):
        return True
    req = urllib.request.Request(url, headers={"User-Agent": "htbb-pokemon-tool"})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            data = r.read()
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return False
        raise
    with open(dich, "wb") as f:
        f.write(data)
    return True


def ten_goi(ten_file):
    return "hu_" + ten_file.split(".")[0].lower()


def tick_moi_khung(n):
    # gameTick 50 Hz. PMDC thuong 2-3 khung 60 Hz/khung; hieu ung dai thi chay nhanh hon.
    return 2 if n <= 30 else 1


DAU_GHI_CONG = "== HIEU UNG CHIEU (tai_hieu_ung_pmd.py) =="
CUOI_GHI_CONG = "== HET HIEU UNG CHIEU =="


def ghi_cong(cac_file):
    cu = open(GHI_CONG, encoding="utf8").read() if os.path.exists(GHI_CONG) else ""
    if DAU_GHI_CONG in cu:
        a = cu.index(DAU_GHI_CONG)
        b = cu.find(CUOI_GHI_CONG, a)
        b = len(cu) if b < 0 else b + len(CUOI_GHI_CONG)
        cu = cu[:a].rstrip("\n") + cu[b:]
    khoi = [DAU_GHI_CONG,
            "Hieu ung chieu (hu_*.bytes): PMDCollab RawAsset, thu muc Particle/ - "
            "https://github.com/PMDCollab/RawAsset",
            "RawAsset KHONG co file LICENSE: trich tu PMD goc (Chunsoft/Nintendo) / PMDCollab RawAsset, "
            "chi dung PHI THUONG MAI.",
            "Pokemon (c) Nintendo / Game Freak / The Pokemon Company.",
            "File goc: " + ", ".join(sorted(cac_file)),
            CUOI_GHI_CONG]
    with open(GHI_CONG, "w", encoding="utf8") as f:
        f.write(cu.rstrip("\n") + "\n" + "\n".join(khoi) + "\n")


def main():
    khong_tai = "--khong-tai" in sys.argv
    os.makedirs(RAW, exist_ok=True)
    os.makedirs(RA, exist_ok=True)
    with open(BANG_CHIEU, encoding="utf8") as f:
        chieu = [r for r in csv.DictReader(f)]
    loi = 0
    da_goi = {}  # ten_file -> (ten_goi, rong, cao, n, so_hang)
    dong_csv = []
    dong_bang = []
    for r in chieu:
        k = (int(r["class"]), int(r["so_hieu_skill"]))
        if k not in CHON:
            print("LOI lop %d chieu %d (%s): chua chon particle" % (k[0], k[1], r["ten_moi"]))
            loi += 1
            continue
        ten_file, kieu_ve = CHON[k]
        if ten_file not in da_goi:
            nguon = os.path.join(RAW, ten_file + ".png")
            if not khong_tai and not tai("%s/%s.png" % (NGUON, ten_file), nguon):
                print("LOI %s: khong co tren RawAsset" % ten_file)
                loi += 1
                continue
            if not os.path.exists(nguon):
                print("LOI %s: chua tai" % ten_file)
                loi += 1
                continue
            try:
                w, h, hang = doc_png(open(nguon, "rb").read())
                rong, cao, n, hang_khung = tach_khung(ten_file, w, h, hang)
                W, H, anh, so_cot = ghep(rong, cao, hang_khung)
            except (ValueError, KeyError, zlib.error) as e:
                print("LOI %s: %s" % (ten_file, e))
                loi += 1
                continue
            goi = ten_goi(ten_file)
            with open(os.path.join(RA, goi + ".bytes"), "wb") as f:
                f.write(ghi_png(W, H, anh))
            da_goi[ten_file] = (goi, rong, cao, n, len(hang_khung), so_cot)
        goi, rong, cao, n, so_hang, so_cot = da_goi[ten_file]
        t = tick_moi_khung(n)
        dong_csv.append([k[0], k[1], r["ten_moi"], ten_file + ".png", goi + ".bytes",
                         rong, cao, n, so_hang, so_cot, kieu_ve, t])
        dong_bang.append("%d,%d,%s,%d,%d,%d,%d,%d,%s,%d" % (k[0], k[1], goi, rong, cao, n, so_hang, so_cot, kieu_ve, t))

    with open(BANG_HU, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f)
        w.writerow(["class", "so_hieu_skill", "ten_moi", "particle", "file_client",
                    "rong_khung", "cao_khung", "so_khung", "so_hang", "so_cot", "kieu_ve", "tick_moi_khung"])
        w.writerows(dong_csv)
    with open(os.path.join(RA, "hu_bang.bytes"), "w", encoding="utf8", newline="\n") as f:
        f.write("# lop,chieu,ten,rong,cao,soKhung,soHuong,soCot,kieuVe,tickMoiKhung (sinh boi tai_hieu_ung_pmd.py)\n")
        f.write("\n".join(dong_bang) + "\n")
    ghi_cong(da_goi.keys())
    dc.chep_sang_ban_khac(RA, [v[0] + ".bytes" for v in da_goi.values()] + ["hu_bang.bytes", os.path.basename(GHI_CONG)], "poke")
    tong = sum(os.path.getsize(os.path.join(RA, v[0] + ".bytes")) for v in da_goi.values())
    print("Da ghi %s: %d chieu, %d particle, %d byte, %d loi"
          % (os.path.relpath(BANG_HU, GOC), len(dong_csv), len(da_goi), tong, loi))
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
