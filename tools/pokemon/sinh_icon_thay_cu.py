#!/usr/bin/env python3
"""
Thay TAI CHO (cung id, cung kich thuoc khung) moi anh icon kieu kiem hiep cu ma nguoi choi con co the
thay, bang anh Pokemon. Bo sung cho sinh_icon_pokemon.py (dai 14000+): tool do doi CHI SO anh, con
tool nay ghi de chinh FILE cu, de moi duong cu (id anh < 14000, goi -8 tap anh, client dong goi san)
cung ra hinh Pokemon - khong con mot net ve kiem hiep nao toi tay nguoi choi.

Ra (KHONG sua tay, chay lai tool):
  server/map/icon/<0..865>.png          icon vat pham goc: lay icon PMD cua chinh vat pham tung dung o do
                                        (xstart cu trong sql/gamedb7_clean.sql -> xstart moi trong
                                        sql/htbb.sql phan A); o khong vat pham nao dung -> Poke Ball ve tay
  server/map/images_potion|images_gem|images_animal|iconquest|fruit/
                                        o anh 1 byte (5500/6500/7500/8000/8200+K): icon PMD theo cung bang
                                        IconPokemonBang (o chua doi -> Poke Ball / qua mong)
  server/map/potionIcon.png, gemIcon.png, linhthu.png, itemIconGiap.png
                                        tap anh goi -8 (o 16x16 xep doc / 4 cot), dung lai icon o tren
  server/map/iconclan/<0..1343>.png     huy hieu bang 12x12: 24 hoa van Pokemon x 7 nen x 8 mau, KHONG trung
  server/map/iconclan.png               bang 10x12 o dau (goi -8 mac dinh)
  server/map/iconskillclan/<0..7>.png   icon ky nang bang 16x16 (khung vang nhu cu, hoa van he)
  server/map/tb/<0..15>.png             "Tu binh": 16 tranh treo 40x80 -> poster Pokemon (chan dung PMD)
  client_jar/res/0.png, client_pc_ios/Assets/Resources/res/02.bytes
                                        dau dong cua hang (12x12): Poke Ball nho
  client_jar/res/main.sh = client_pc_ios/Assets/Resources/res/main.bytes
                                        nguhanh.png (5 huy hieu he 16x16: Lua, Nuoc, Dat, Dien, Co) va
                                        wicon.png (25x25, Poke Ball) trong goi .sh (XOR NguyenVanMinh)
  du_lieu/bang_icon_thay_cu.csv         bang ke: tep -> nguon (pmd:<n> = map/icon_pokemon/<n>.png,
                                        ve:<ten> = tu ve, cd:<dex> = chan dung PMD); test KiemIconThayCu doc

Nguon hinh: tu ve (hoa van, Poke Ball, huy hieu) + PMDCollab (icon RawAsset da tai boi
sinh_icon_pokemon.py, chan dung SpriteCollab cho poster Tu binh; CC BY-NC 4.0, ghi cong GHI_CONG).

Chay (o goc repo, SAU sinh_icon_pokemon.py):
    python3 -I tools/pokemon/sinh_icon_thay_cu.py
"""
import os
import re
import struct
import sys
from collections import Counter

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import sinh_icon_pokemon as sip  # noqa: E402

GOC = sip.GOC
MAP = os.path.join(GOC, "server", "map")
ICON_PMD = os.path.join(MAP, "icon_pokemon")
N_ICON_ITEM = 866          # DataGame.nIconItem
SO_CLAN = 1344             # Map.MAX_CLAN
RA_CSV = os.path.join(GOC, "du_lieu", "bang_icon_thay_cu.csv")
GOI_MAIN = [os.path.join(GOC, "client_jar", "res", "main.sh"),
            os.path.join(GOC, "client_pc_ios", "Assets", "Resources", "res", "main.bytes")]
DAU_SHOP = [os.path.join(GOC, "client_jar", "res", "0.png"),
            os.path.join(GOC, "client_pc_ios", "Assets", "Resources", "res", "02.bytes")]

T = (0, 0, 0, 0)


def moi(w, h, m=T):
    return [[m] * w for _ in range(h)]


def dat(a, x, y, m):
    if 0 <= y < len(a) and 0 <= x < len(a[0]):
        a[y][x] = m


def dan(dich, nguon, ox, oy):
    for j, r in enumerate(nguon):
        for i, p in enumerate(r):
            if p[3]:
                dat(dich, ox + i, oy + j, p)


def doc(p):
    return sip.doc_png(open(p, "rb").read())


def ghi(p, a):
    """Ghi chi khi khac diem anh (giu byte cu neu giong)."""
    try:
        if doc(p) == [[tuple(q) for q in r] for r in a]:
            return False
    except Exception:
        pass
    os.makedirs(os.path.dirname(p), exist_ok=True)
    open(p, "wb").write(sip.ghi_png(a))
    return True


def thu_phong(a, w, h):
    """Doi kich thuoc kieu lang gieng gan nhat."""
    sh, sw = len(a), len(a[0])
    return [[a[y * sh // h][x * sw // w] for x in range(w)] for y in range(h)]


def vua_o(a, w, h):
    """Dat anh vao o w x h: to hon thi thu nho giu ti le, nho hon thi canh giua."""
    sh, sw = len(a), len(a[0])
    if sw > w or sh > h:
        k = min(w / sw, h / sh)
        a = thu_phong(a, max(1, int(sw * k)), max(1, int(sh * k)))
        sh, sw = len(a), len(a[0])
    o = moi(w, h)
    dan(o, a, (w - sw) // 2, (h - sh) // 2)
    return o


# ---------------------------------------------------------------- hinh ve tay

def tron(a, cx, cy, r, m, vien=None):
    for y in range(len(a)):
        for x in range(len(a[0])):
            d = (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2
            if d <= r * r:
                a[y][x] = m
                if vien and d > (r - 1) ** 2:
                    a[y][x] = vien


def poke_ball(n, do=(224, 48, 48, 255), tren=None):
    """Poke Ball n x n: nua tren mau, nua duoi trang, dai den + nut giua."""
    a = moi(n, n)
    c = n / 2.0
    r = n / 2.0 - 0.3
    den = (28, 28, 36, 255)
    trang = (244, 244, 248, 255)
    bong = (196, 196, 210, 255)
    tren = tren or do
    for y in range(n):
        for x in range(n):
            dx, dy = x + 0.5 - c, y + 0.5 - c
            d = (dx * dx + dy * dy) ** 0.5
            if d > r:
                continue
            if d > r - 1:
                a[y][x] = den
            elif abs(dy) <= max(0.6, n / 16.0):
                a[y][x] = den
            elif dy < 0:
                a[y][x] = tren if not (dx < -r / 3 and dy < -r / 3 and d < r - 1.5 and d > r - 3) else \
                    tuple(min(255, v + 70) for v in tren[:3]) + (255,)
            else:
                a[y][x] = bong if dx > r / 3 else trang
    nr = max(1.6, n / 6.0)
    for y in range(n):
        for x in range(n):
            d = ((x + 0.5 - c) ** 2 + (y + 0.5 - c) ** 2) ** 0.5
            if d <= nr:
                a[y][x] = den if d > nr - 1 else trang
    return a


def qua_mong():
    """Qua mong (berry) 16x16 cho o trai cay chua co icon."""
    a = moi(16, 16)
    tron(a, 8, 9.5, 5.5, (70, 110, 220, 255), (30, 40, 90, 255))
    for x, y in ((6, 7), (6, 8), (7, 7)):
        a[y][x] = (170, 200, 255, 255)
    for x, y in ((8, 3), (8, 2), (9, 2), (10, 2), (10, 3), (11, 3)):
        a[y][x] = (60, 150, 60, 255)
    return a


# Hoa van 8x8 ('#' = net chinh, '+' = net phu)
HOA_VAN = [
    ("ball", ["..####..", ".#....#.", "#......#", "###..###", "###..###", "#......#", ".#....#.", "..####.."]),
    ("lua", ["...#....", "...##...", "..###...", "..####..", ".##+###.", ".#+++##.", ".##+##..", "..####.."]),
    ("nuoc", ["...#....", "...#....", "..###...", "..###...", ".#####..", ".##+##..", ".#####..", "..###..."]),
    ("la", [".....###", "...#####", "..####+#", ".####+##", ".##+###.", ".#+###..", "#+##....", "#......."]),
    ("dien", ["....###.", "...###..", "..###...", ".######.", "....##..", "...##...", "..##....", ".#......"]),
    ("sao", ["...##...", "...##...", "########", ".######.", "..####..", ".######.", ".##..##.", "##....##"]),
    ("tim", [".##..##.", "########", "########", "########", ".######.", "..####..", "...##...", "........"]),
    ("trang", ["..####..", ".###....", "###.....", "###.....", "###.....", "###.....", ".###....", "..####.."]),
    ("bang", ["...#....", ".#.#.#..", "..###...", "#######.", "..###...", ".#.#.#..", "...#....", "........"]),
    ("da", ["........", "...##...", "..####..", ".##+###.", ".#+####.", "#######.", "########", "........"]),
    ("canh", ["#.......", "##......", "###.....", "####....", "#####...", "######..", ".#######", "...#####"]),
    ("chan", [".#...#..", "##...##.", "..#.#...", ".##.##..", "........", "..###...", ".#####..", ".#####.."]),
    ("trung", ["...##...", "..####..", ".#+####.", ".######.", "####+###", "###+####", ".######.", "..####.."]),
    ("khien", ["########", "#++##++#", "#++##++#", "########", "########", ".######.", "..####..", "...##..."]),
    ("mat", ["........", "..####..", ".#....#.", "#..##..#", "#..##..#", ".#....#.", "..####..", "........"]),
    ("ma", ["..####..", ".######.", "##.##.##", "########", "########", "########", "#.##.##.", "........"]),
    ("bo", ["#.....#.", ".#...#..", "..###...", ".#####..", "#######.", ".#####..", "#.###.#.", "..#.#..."]),
    ("nam_dam", ["........", ".####...", "######..", "######..", "#######.", ".######.", ".#####..", "..###..."]),
    ("doc", ["..#..#..", "........", ".####...", "######..", "######..", "######..", ".####...", "..##...."]),
    ("vuot", ["#...#...", "#...#...", "#...#...", "##..##..", ".#...#..", ".##..##.", "..#...#.", "...#...#"]),
    ("banh_rang", ["..#..#..", ".######.", "##....##", ".#.##.#.", ".#.##.#.", "##....##", ".######.", "..#..#.."]),
    ("song", ["........", ".##.....", "#..#..#.", "....##..", "........", ".##.....", "#..#..#.", "....##.."]),
    ("mat_troi", ["#..#..#.", ".#.#.#..", "..###...", "#######.", "..###...", ".#.#.#..", "#..#..#.", "........"]),
    ("vuong_mien", ["........", "#..#..#.", "#.###.#.", "#######.", "#######.", "#+#+#+#.", "#######.", "........"]),
]

# 8 bang mau: (nen, nen sang, vien toi)
MAU = [
    ((214, 52, 52), (240, 110, 96), (90, 16, 20)),     # do
    ((48, 112, 224), (110, 164, 250), (16, 34, 92)),   # xanh duong
    ((52, 160, 72), (118, 206, 110), (14, 62, 26)),    # xanh la
    ((236, 190, 40), (252, 226, 120), (110, 76, 8)),   # vang
    ((140, 72, 200), (186, 132, 236), (52, 20, 84)),   # tim
    ((232, 104, 168), (250, 160, 204), (104, 28, 68)), # hong
    ((40, 176, 190), (120, 220, 226), (8, 70, 80)),    # lam ngoc
    ((236, 128, 40), (252, 180, 104), (108, 48, 8)),   # cam
]
TRANG = (250, 250, 252, 255)
DEN = (24, 24, 32, 255)


def _nen12(kieu, mau):
    """Nen 12x12 theo kieu (0..6)."""
    nen, sang, toi = [m + (255,) for m in mau]
    a = moi(12, 12)
    for y in range(12):
        for x in range(12):
            cx, cy = x + 0.5 - 6, y + 0.5 - 6
            if kieu == 0:      # tron
                v = cx * cx + cy * cy <= 36
                b = v and cx * cx + cy * cy > 25
            elif kieu == 1:    # vuong bo goc
                v = not ((x in (0, 11)) and (y in (0, 11)))
                b = v and (x in (0, 11) or y in (0, 11))
            elif kieu == 2:    # khien
                v = y < 7 or abs(cx) < (12 - y) * 0.95
                b = v and (y == 0 or x in (0, 11) or abs(cx) > (12 - y) * 0.95 - 1.2 and y >= 7 or y == 11)
            elif kieu == 3:    # thoi
                v = abs(cx) + abs(cy) <= 6.5
                b = v and abs(cx) + abs(cy) > 5.2
            elif kieu == 4:    # bat giac
                v = abs(cx) + abs(cy) <= 8 and abs(cx) <= 6 and abs(cy) <= 6
                b = v and (abs(cx) + abs(cy) > 7 or abs(cx) > 5 or abs(cy) > 5)
            elif kieu == 5:    # co duoi nheo
                v = not (y >= 9 and abs(cx) < (y - 8) * 1.6)
                b = v and (y == 0 or x in (0, 11) or y >= 9 and abs(cx) < (y - 8) * 1.6 + 1.3)
            else:              # nen hai mau cheo (Poke Ball)
                v = cx * cx + cy * cy <= 36
                b = v and cx * cx + cy * cy > 25
            if v:
                a[y][x] = toi if b else (sang if (kieu == 6 and cy < 0) or (kieu != 6 and y < 3 and not b) else nen)
    return a


def huy_hieu_clan(i):
    hv = HOA_VAN[i % len(HOA_VAN)][1]
    v = i // len(HOA_VAN)
    kieu, mau = v % 7, MAU[(v // 7 + i % 3) % 8]
    a = _nen12(kieu, mau)
    net = DEN if mau[0][0] + mau[0][1] > 380 else TRANG
    phu = mau[2] + (255,) if net == TRANG else mau[1] + (255,)
    for j, r in enumerate(hv):
        for k, ch in enumerate(r):
            if ch == "#":
                a[2 + j][2 + k] = net
            elif ch == "+":
                a[2 + j][2 + k] = phu
    return a


# ky nang bang: (hoa van, mau)
SKILL_CLAN = [("nuoc", 1), ("khien", 6), ("mat", 4), ("la", 2), ("bang", 6), ("lua", 0), ("canh", 3), ("vuot", 7)]


def icon_skill_clan(k):
    ten, m = SKILL_CLAN[k]
    hv = dict(HOA_VAN)[ten]
    nen, sang, toi = [c + (255,) for c in MAU[m]]
    vang, vang_toi = (250, 210, 60, 255), (150, 100, 10, 255)
    a = moi(16, 16)
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                a[y][x] = vang_toi
            elif x in (1, 14) or y in (1, 14):
                a[y][x] = vang
            else:
                a[y][x] = sang if y < 6 else nen
    for j, r in enumerate(hv):
        for k2, ch in enumerate(r):
            if ch == "#":
                for dx in (-1, 0, 1):
                    for dy in (-1, 0, 1):
                        if a[4 + j + dy][4 + k2 + dx] in (nen, sang):
                            a[4 + j + dy][4 + k2 + dx] = toi
    for j, r in enumerate(hv):
        for k2, ch in enumerate(r):
            if ch in "#+":
                a[4 + j][4 + k2] = TRANG if ch == "#" else sang
    return a


# 5 huy hieu he 16x16 cho nguhanh.png, dung thu tu o anh cu: Lua, Nuoc, Dat, Dien, Co
HE_NGU_HANH = [("lua", (238, 129, 48)), ("nuoc", (99, 144, 240)), ("da", (226, 191, 101)),
               ("dien", (247, 208, 44)), ("la", (122, 199, 76))]


def huy_hieu_he(ten, mau):
    a = moi(16, 16)
    toi = tuple(c * 45 // 100 for c in mau) + (255,)
    tron(a, 8, 8, 7.6, mau + (255,), toi)
    hv = dict(HOA_VAN)[ten]
    for j, r in enumerate(hv):
        for k, ch in enumerate(r):
            if ch == "#":
                a[4 + j][4 + k] = TRANG
            elif ch == "+":
                a[4 + j][4 + k] = tuple(min(255, c + 60) for c in mau) + (255,)
    return a


ICON_SKIN = [860, 861, 862, 863, 864, 865]


def icon_skin(cd, ba_khung):
    """Chan dung 20x20 (vien toi 1px) -> 14x14 giua o 16x16 (vien trong suot 1px). Ba khung (865, nhu
    icon mat na 620): 18x54 = 3 khung 18x18, chan dung 16x16 sang dan (hoat anh nhap nhay)."""
    trong = [r[1:-1] for r in cd[1:-1]]
    if not ba_khung:
        o = moi(16, 16)
        dan(o, thu_phong(trong, 14, 14), 1, 1)
        return o
    o = moi(18, 54)
    for k, sang in enumerate((0, 25, 50)):
        anh = [[(tuple(min(255, c + (255 - c) * sang // 100) for c in p[:3]) + (p[3],)) for p in r]
               for r in thu_phong(trong, 16, 16)]
        dan(o, anh, 1, k * 18 + 1)
    return o


# ---------------------------------------------------------------- Tu binh: poster Pokemon 40x80

# TuBinhTemplate id 1..16 (anh tb/<id-1>): ten cu -> Pokemon cung y
TU_BINH = [("Mai", "0421", 2), ("Lan", "0315", 5), ("Cuc", "0192", 3), ("Truc", "0674", 2),
           ("Sao", "0039", 5), ("Quat", "0275", 7), ("Senh", "0402", 0), ("Dan", "0648", 2),
           ("Ngu", "0129", 1), ("Tieu", "0532", 7), ("Canh", "0241", 5), ("Doc", "0065", 4),
           ("Long", "0384", 2), ("Lan2", "0245", 6), ("Quy", "0009", 1), ("Phung", "0250", 0)]


def poster(dex, m, loi):
    dich = os.path.join(sip.TAI, "Portrait", dex + ".png")
    if not sip.tai(sip.NGUON_CHAN_DUNG % dex, dich, loi):
        return None
    cd = doc(dich)
    nen, sang, toi = [c + (255,) for c in MAU[m]]
    go, go_sang = (110, 70, 36, 255), (164, 112, 60, 255)
    giay = (244, 236, 214, 255)
    a = moi(40, 80)
    # truc treo tren / duoi
    for y0 in (0, 74):
        for y in range(y0, y0 + 6):
            for x in range(40):
                a[y][x] = go_sang if y == y0 + 1 else go
        for x in (0, 1, 38, 39):
            for y in range(y0, y0 + 6):
                a[y][x] = toi
    # than poster
    for y in range(6, 74):
        for x in range(2, 38):
            a[y][x] = giay
        a[y][2] = a[y][37] = nen
    for x in range(2, 38):
        a[6][x] = a[73][x] = nen
    # dai mau tren + chan dung 32x32 + Poke Ball duoi
    for y in range(7, 12):
        for x in range(3, 37):
            a[y][x] = sang if y < 9 else nen
    anh = thu_phong(cd, 32, 32)
    for y in range(32):
        for x in range(32):
            a[14 + y][4 + x] = anh[y][x]
    for x in range(3, 37):
        a[13][x] = a[46][x] = toi
    for y in range(13, 47):
        a[y][3] = a[y][36] = toi
    dan(a, poke_ball(14, nen), 13, 52)
    for x in range(6, 34):
        a[68][x] = sang
    return a


# ---------------------------------------------------------------- goi .sh (FilePack, XOR)

MA = bytes([78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104])


def _xor(b):
    return bytes(c ^ MA[i % len(MA)] for i, c in enumerate(b))


def doc_goi(p):
    d = open(p, "rb").read()
    i, n, muc = 1, d[0], []
    for _ in range(n):
        ln = d[i]
        ten = _xor(d[i + 1:i + 1 + ln]).decode("latin1")
        i += 1 + ln
        dai = struct.unpack(">H", d[i:i + 2])[0]
        i += 2
        muc.append([ten, dai])
    du = _xor(d[i:])
    q, ra = 0, []
    for ten, dai in muc:
        ra.append([ten, du[q:q + dai]])
        q += dai
    return ra


def ghi_goi(muc):
    dau, than = bytearray([len(muc)]), b""
    for ten, b in muc:
        tb = _xor(ten.encode("latin1"))
        dau.append(len(tb))
        dau += tb + struct.pack(">H", len(b))
        than += b
    return bytes(dau) + _xor(than)


# ---------------------------------------------------------------- SQL: xstart cu -> moi

def xstart_theo_id(duong):
    s = open(duong, encoding="utf8").read()
    s = s.split("\n-- ######## B. NANG CAP")[0]  # phan A (CSDL day du)
    ra = {}
    for m in re.finditer(r"INSERT INTO `data_item`[^;]*?VALUES(.*?);\n", s, re.S):
        for r in re.finditer(r"\((\d+),'(?:[^'\\]|\\.)*',((?:-?\d+,){18})(-?\d+)", m.group(1)):
            ra[int(r.group(1))] = int(r.group(3))
    return ra


def main():
    loi, dem = [], Counter()
    ke = []  # (tep tuong doi goc repo, nguon)

    def ghi_ke(p, a, nguon):
        ke.append((os.path.relpath(p, GOC).replace(os.sep, "/"), nguon))
        return ghi(p, a)
    n_pmd = 0
    while os.path.isfile(os.path.join(ICON_PMD, "%d.png" % n_pmd)):
        n_pmd += 1
    pmd = [doc(os.path.join(ICON_PMD, "%d.png" % n)) for n in range(n_pmd)]
    bong = poke_ball(16)

    # 1) map/icon/<id>.png
    cu = xstart_theo_id(os.path.join(GOC, "sql", "gamedb7_clean.sql"))
    moi_ = xstart_theo_id(os.path.join(GOC, "sql", "htbb.sql"))
    theo_o = {}
    for ident, x in cu.items():
        nx = moi_.get(ident)
        if nx is not None and sip.ID_DAU <= nx < sip.ID_DAU + n_pmd:
            theo_o.setdefault(x, Counter())[nx - sip.ID_DAU] += 1
    icon_o = []
    for k in range(N_ICON_ITEM):
        n = theo_o[k].most_common(1)[0][0] if k in theo_o else None
        a = pmd[n] if n is not None else bong
        if k in ICON_SKIN and n is not None:
            # 860..865 (skin linh thu): giu rang buoc kich thuoc cua test KiemIconSkinThu
            a = icon_skin(pmd[n], k == ICON_SKIN[-1])
            icon_o.append(a)
            dem["icon skin"] += 1
            dem["ghi"] += ghi_ke(os.path.join(MAP, "icon", "%d.png" % k), a, "ve:skin_pmd_%d" % n)
            continue
        dem["icon " + ("pmd" if n is not None else "ball")] += 1
        icon_o.append(a)
        dem["ghi"] += ghi_ke(os.path.join(MAP, "icon", "%d.png" % k), a,
                             "pmd:%d" % n if n is not None else "ve:poke_ball")

    # 2) dai 1 byte
    kq = sip.phan_tich(loi)
    dai_anh = {}
    for ten, (dau, so, tm, mau) in sip.DAI.items():
        ds = []
        for k in range(so):
            p = os.path.join(GOC, tm, mau % k)
            n = kq["dai"][ten].get(k)
            a = pmd[n] if n is not None and n < n_pmd else (qua_mong() if ten == "trai" else bong)
            dem["%s %s" % (ten, "pmd" if n is not None else "ve")] += 1
            ds.append(a)
            if os.path.isfile(p) or n is not None:
                dem["ghi"] += ghi_ke(p, a, "pmd:%d" % n if n is not None and n < n_pmd
                                     else ("ve:qua_mong" if ten == "trai" else "ve:poke_ball"))
        dai_anh[ten] = ds

    # 3) tap anh goi -8: o 16x16
    def tap(ten_file, cot, nguon):
        p = os.path.join(MAP, ten_file)
        w, h = len(doc(p)[0]), len(doc(p))
        a = moi(w, h)
        for c in range((w // 16) * (h // 16)):
            if c < len(nguon):
                dan(a, vua_o(nguon[c], 16, 16), (c % cot) * 16, (c // cot) * 16)
        dem["ghi"] += ghi_ke(p, a, "ve:tap_16")
    tap("potionIcon.png", 1, dai_anh["potion"])
    tap("gemIcon.png", 1, dai_anh["gem"])
    tap("linhthu.png", 1, dai_anh["thu"])
    tap("itemIconGiap.png", 4, icon_o)

    # 4) huy hieu bang + bang 10x12
    clan = [huy_hieu_clan(i) for i in range(SO_CLAN)]
    if len(set(str(c) for c in clan)) != SO_CLAN:
        loi.append("huy hieu bang bi trung")
    for i, c in enumerate(clan):
        dem["ghi"] += ghi_ke(os.path.join(MAP, "iconclan", "%d.png" % i), c, "ve:huy_hieu_bang")
    bang = moi(120, 144)
    for i in range(120):
        dan(bang, clan[i], (i % 10) * 12, (i // 10) * 12)
    dem["ghi"] += ghi_ke(os.path.join(MAP, "iconclan.png"), bang, "ve:huy_hieu_bang")

    # 5) ky nang bang
    for k in range(len(SKILL_CLAN)):
        dem["ghi"] += ghi_ke(os.path.join(MAP, "iconskillclan", "%d.png" % k), icon_skill_clan(k), "ve:ky_nang_bang")

    # 6) Tu binh
    for k, (_, dex, m) in enumerate(TU_BINH):
        a = poster(dex, m, loi)
        if a is not None:
            dem["ghi"] += ghi_ke(os.path.join(MAP, "tb", "%d.png" % k), a, "cd:" + dex)

    # 7) client: dau dong cua hang + goi main
    for p in DAU_SHOP:
        dem["ghi"] += ghi_ke(p, poke_ball(12), "ve:poke_ball")
    ngu_hanh = moi(80, 16)
    for k, (ten, mau) in enumerate(HE_NGU_HANH):
        dan(ngu_hanh, huy_hieu_he(ten, mau), k * 16, 0)
    thay = {"nguhanh.png": sip.ghi_png(ngu_hanh), "wicon.png": sip.ghi_png(poke_ball(25))}
    for p in GOI_MAIN:
        muc = doc_goi(p)
        for m in muc:
            if m[0] in thay:
                m[1] = thay[m[0]]
        b = ghi_goi(muc)
        if open(p, "rb").read() != b:
            open(p, "wb").write(b)
            dem["ghi"] += 1
        for ten in sorted(thay):
            ke.append((os.path.relpath(p, GOC).replace(os.sep, "/") + "#" + ten, "ve:" + ten[:-4]))

    with open(RA_CSV, "w", encoding="utf8", newline="\n") as f:
        f.write("tep,nguon\n")
        for t, n in ke:
            f.write("%s,%s\n" % (t, n))
    print("Da ghi %d tep (thay doi %d) -> bang ke %s" % (len(ke), dem.pop("ghi"), os.path.relpath(RA_CSV, GOC)))

    for k in sorted(dem):
        print("  %-14s %d" % (k, dem[k]))
    for l in loi:
        print("LOI:", l)
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
