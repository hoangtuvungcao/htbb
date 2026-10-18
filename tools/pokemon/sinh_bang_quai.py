#!/usr/bin/env python3
"""
Sinh du_lieu/bang_quai.csv: MOI dong data_monster (138 dong) -> Pokemon thay the,
va sql/pokemon_du_lieu_quai.sql (UPDATE data_monster SET dex/shiny/he/he2 - chay SAU
sql/them_cot_pokemon.sql). Vat_the giu dex=0 -> client ve bang anh cu.

Nguon:
  - sql/gamedb7_clean.sql, bang data_monster (id, ten, cap, maxhp)
  - ANH_XA ben duoi (do nguoi quyet dinh - SUA O DAY, khong sua tay file CSV)
  - PokeAPI (he cua loai) + tracker.json cua PMDCollab/SpriteCollab (co sprite chua)

Chay (o thu muc HTBB/):
    python3 -I tools/pokemon/sinh_bang_quai.py

Script se BAO LOI va thoat ma 1 neu co dong data_monster chua duoc anh xa,
hoac loai duoc chon khong co sprite PMD -> khong the "quen" con quai nao.
"""
import csv
import json
import os
import re
import sys
import urllib.request

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SQL = os.path.join(GOC, "sql", "gamedb7_clean.sql")
RA = os.path.join(GOC, "du_lieu", "bang_quai.csv")
RA_SQL = os.path.join(GOC, "sql", "pokemon_du_lieu_quai.sql")
BO_NHO = os.path.join(GOC, "tools", "pokemon", ".cache")

TRACKER = "https://raw.githubusercontent.com/PMDCollab/SpriteCollab/master/tracker.json"
POKEAPI = "https://pokeapi.co/api/v2/pokemon/{}"

# Ten he PokeAPI -> so hieu HePokemon (src/com/htbb/pokemon/HePokemon.java)
HE = ["normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground",
      "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"]

# Phan loai: quai | boss | vat_the (khong phai Pokemon, ve bang Object PMD) | thu_cuoi
# id_quai: (phan_loai, dex, shiny, ghi_chu). dex = 0 khi la vat_the.
ANH_XA = {
    1: ("quai", 27, 0, "Nhím -> Sandshrew"),
    2: ("quai", 10, 0, "Sâu -> Caterpie"),
    3: ("quai", 194, 0, "Giọt nước -> Wooper"),
    4: ("quai", 255, 0, "Gà điên -> Torchic"),
    5: ("quai", 23, 0, "Rắn lục -> Ekans"),
    6: ("quai", 607, 0, "Ma trơi -> Litwick"),
    7: ("quai", 69, 0, "Nắp ấm -> Bellsprout"),
    8: ("quai", 48, 0, "Rệp quỷ -> Venonat"),
    9: ("quai", 19, 0, "Chuột cống -> Rattata"),
    10: ("quai", 43, 0, "Quỷ hoa -> Oddish"),
    11: ("quai", 13, 0, "Sâu róm -> Weedle"),
    12: ("quai", 37, 0, "Lửa ma -> Vulpix"),
    13: ("quai", 195, 0, "Giọt nước lớn -> Quagsire"),
    14: ("quai", 256, 0, "Gà con -> Combusken"),
    15: ("quai", 498, 0, "Heo mọi -> Tepig"),
    16: ("quai", 451, 0, "Bọ cạp -> Skorupi"),
    17: ("quai", 324, 0, "Rùa đỏ -> Torkoal"),
    18: ("quai", 168, 0, "Nhện tím -> Ariados"),
    19: ("quai", 46, 0, "Nấm ma -> Paras"),
    20: ("quai", 44, 0, "Quỷ sinh hoa -> Gloom"),
    21: ("quai", 543, 0, "Rết -> Venipede"),
    22: ("quai", 85, 0, "Gà khổng lồ -> Dodrio"),
    23: ("quai", 24, 0, "Rắn khổng lồ -> Arbok"),
    24: ("quai", 12, 0, "Bướm khổng lồ -> Butterfree"),
    25: ("quai", 92, 0, "Bóng ma -> Gastly"),
    26: ("quai", 72, 0, "Thủy nhãn -> Tentacool"),
    27: ("quai", 104, 0, "Skeleton -> Cubone"),
    28: ("quai", 58, 0, "Cọp khổng lồ -> Growlithe"),
    29: ("quai", 159, 0, "Cá sấu -> Croconaw"),
    30: ("quai", 453, 0, "Ếch quỷ -> Croagunk"),
    31: ("quai", 402, 0, "Dế khổng lồ -> Kricketune"),
    32: ("quai", 47, 0, "Nấm tinh -> Parasect"),
    33: ("quai", 128, 0, "Trâu núi -> Tauros"),
    34: ("quai", 559, 0, "Sơn tặc -> Scraggy"),
    35: ("quai", 342, 0, "Hải tặc -> Crawdaunt"),
    36: ("boss", 437, 0, "Long Trụ (sự kiện) -> Bronzong"),
    37: ("boss", 436, 0, "Long trụ phụ -> Bronzor"),
    38: ("boss", 130, 0, "Thuồng luồng -> Gyarados (BossGyarados)"),
    39: ("boss", 373, 0, "Thằn lằn -> Salamence (BossDotKich)"),
    40: ("quai", 544, 0, "Rết đỏ -> Whirlipede"),
    41: ("quai", 545, 0, "Rết tía -> Scolipede"),
    42: ("quai", 544, 1, "Rết xanh -> Whirlipede shiny"),
    43: ("boss", 185, 0, "Liên hoa trụ -> Sudowoodo"),
    44: ("quai", 257, 0, "Tử kê -> Blaziken"),
    45: ("quai", 83, 0, "Hồng kê -> Farfetch'd"),
    46: ("boss", 681, 0, "Tướng thủ thành -> Aegislash"),
    47: ("quai", 336, 0, "Rắn mang bành -> Seviper"),
    48: ("quai", 59, 0, "Sư tử -> Arcanine"),
    49: ("quai", 423, 0, "Ốc ma -> Gastrodon"),
    50: ("quai", 269, 0, "Bướm nâu -> Dustox"),
    51: ("quai", 709, 0, "Ma cây -> Trevenant"),
    52: ("quai", 93, 0, "Huyết ma -> Haunter"),
    53: ("quai", 340, 0, "Cá thòi lòi -> Whiscash"),
    54: ("quai", 99, 0, "Cua càng to -> Kingler"),
    55: ("quai", 355, 0, "Độc nhãn -> Duskull"),
    56: ("quai", 319, 0, "Cá ma -> Sharpedo"),
    57: ("quai", 356, 0, "Huyết nhãn -> Dusclops"),
    58: ("quai", 45, 0, "Quỷ hoa (lv78) -> Vileplume"),
    59: ("quai", 105, 0, "Skeleton lam -> Marowak"),
    60: ("quai", 112, 0, "Tê giác -> Rhydon"),
    61: ("quai", 510, 0, "Cọp tím -> Liepard"),
    62: ("quai", 404, 0, "Cọp xanh -> Luxio"),
    63: ("quai", 667, 0, "Cọp đỏ -> Litleo"),
    64: ("quai", 552, 0, "Cá sấu đỏ -> Krokorok"),
    65: ("quai", 739, 0, "Cua đinh -> Crabrawler"),
    66: ("quai", 160, 0, "Cá sấu xanh -> Feraligatr"),
    67: ("quai", 537, 0, "Cóc lam -> Seismitoad"),
    68: ("quai", 536, 0, "Cóc lửa -> Palpitoad"),
    69: ("quai", 454, 0, "Cóc xanh -> Toxicroak"),
    70: ("quai", 637, 0, "Dế lửa -> Volcarona"),
    71: ("quai", 401, 0, "Dế cam -> Kricketot"),
    72: ("quai", 214, 0, "Dế lam -> Heracross"),
    73: ("quai", 128, 1, "Trâu đỏ -> Tauros shiny"),
    74: ("quai", 76, 0, "Người đá -> Golem"),
    75: ("quai", 750, 0, "Trâu xanh -> Mudsdale"),
    76: ("quai", 219, 0, "Ốc sên -> Magcargo"),
    77: ("quai", 127, 0, "Bọ hung -> Pinsir"),
    78: ("quai", 560, 0, "Sơn tặc 3 -> Scrafty"),
    79: ("quai", 781, 0, "Hải tặc 1 -> Dhelmise"),
    80: ("quai", 286, 0, "Nấm độc -> Breloom"),
    81: ("quai", 123, 0, "Cánh cam -> Scyther"),
    82: ("boss", 145, 0, "Đại bàng -> Zapdos (huyền thoại)"),
    83: ("vat_the", 0, 0, "Cổng thành -> Object PMD (cổng/khối)"),
    84: ("quai", 562, 0, "Cương thi -> Yamask"),
    85: ("vat_the", 0, 0, "Đá (nguyên liệu khu mỏ) -> Object PMD"),
    86: ("vat_the", 0, 0, "Bông (nguyên liệu) -> Object PMD"),
    87: ("vat_the", 0, 0, "Gỗ (nguyên liệu) -> Object PMD"),
    88: ("vat_the", 0, 0, "Da (nguyên liệu) -> Object PMD"),
    89: ("vat_the", 0, 0, "Sắt (nguyên liệu) -> Object PMD"),
    90: ("boss", 460, 0, "Người tuyết -> Abomasnow (BossNguoiTuyet)"),
    91: ("boss", 497, 0, "Rắn chị -> Serperior (BossSerperior)"),
    92: ("boss", 496, 0, "Rắn em -> Servine (BossServine)"),
    93: ("boss", 94, 0, "Mắt quỷ -> Gengar"),
    94: ("boss", 477, 0, "Bạch cốt tướng quân -> Dusknoir (BossDusknoir)"),
    95: ("thu_cuoi", 77, 0, "Ngựa cá nhân trắng -> Ponyta"),
    96: ("thu_cuoi", 77, 0, "Ngựa cá nhân xanh -> Ponyta (đổi màu)"),
    97: ("thu_cuoi", 77, 0, "Ngựa cá nhân đỏ -> Ponyta (đổi màu)"),
    98: ("thu_cuoi", 77, 0, "Ngựa cá nhân xanh lá -> Ponyta (đổi màu)"),
    99: ("thu_cuoi", 77, 0, "Ngựa cá nhân vàng -> Ponyta (đổi màu)"),
    100: ("thu_cuoi", 77, 1, "Ngựa cá nhân tím -> Ponyta shiny"),
    101: ("thu_cuoi", 78, 0, "Ngựa bang trắng -> Rapidash"),
    102: ("thu_cuoi", 78, 0, "Ngựa bang xanh -> Rapidash (đổi màu)"),
    103: ("thu_cuoi", 78, 0, "Ngựa bang đỏ -> Rapidash (đổi màu)"),
    104: ("thu_cuoi", 78, 0, "Ngựa bang xanh lá -> Rapidash (đổi màu)"),
    105: ("thu_cuoi", 78, 0, "Ngựa bang vàng -> Rapidash (đổi màu)"),
    106: ("thu_cuoi", 78, 1, "Ngựa bang tím -> Rapidash shiny"),
    107: ("thu_cuoi", 750, 0, "Ngựa quốc gia trắng -> Mudsdale"),
    108: ("thu_cuoi", 750, 0, "Ngựa quốc gia xanh -> Mudsdale (đổi màu)"),
    109: ("thu_cuoi", 750, 0, "Ngựa quốc gia đỏ -> Mudsdale (đổi màu)"),
    110: ("thu_cuoi", 750, 0, "Ngựa quốc gia xanh lá -> Mudsdale (đổi màu)"),
    111: ("thu_cuoi", 750, 0, "Ngựa quốc gia vàng -> Mudsdale (đổi màu)"),
    112: ("thu_cuoi", 750, 1, "Ngựa quốc gia tím -> Mudsdale shiny"),
    113: ("boss", 428, 0, "Boss thỏ điên -> Lopunny"),
    114: ("quai", 353, 0, "Khô lâu -> Shuppet"),
    115: ("boss", 217, 0, "Boss Gấu xám -> Ursaring"),
    116: ("boss", 491, 0, "Dracula -> Darkrai (BossDracula)"),
    117: ("boss", 711, 0, "Bí ngô -> Gourgeist (BossDracula/Halloween)"),
    118: ("quai", 596, 0, "Nhện ma -> Galvantula"),
    119: ("vat_the", 0, 0, "cong thanh -> Object PMD"),
    120: ("boss", 384, 0, "Trụ rồng -> Rayquaza (BossTruRong)"),
    121: ("vat_the", 0, 0, "Rương ma quái -> Object PMD Chest"),
    122: ("vat_the", 0, 0, "Ngọc 1 sao -> Object/Item PMD"),
    123: ("vat_the", 0, 0, "Ngọc 2 sao -> Object/Item PMD"),
    124: ("vat_the", 0, 0, "Ngọc 3 sao -> Object/Item PMD"),
    125: ("vat_the", 0, 0, "Ngọc 4 sao -> Object/Item PMD"),
    126: ("vat_the", 0, 0, "Ngọc 5 sao -> Object/Item PMD"),
    127: ("vat_the", 0, 0, "Ngọc 6 sao -> Object/Item PMD"),
    128: ("vat_the", 0, 0, "Ngọc 7 sao -> Object/Item PMD"),
    129: ("boss", 382, 0, "Boss Thủy Tinh -> Kyogre (BossKyogreGroudon)"),
    130: ("boss", 383, 0, "Boss Sơn Tinh -> Groudon (BossKyogreGroudon)"),
    131: ("vat_the", 0, 0, "Thùng gỗ -> Object PMD"),
    132: ("quai", 105, 1, "Skeleton lam (bản 2) -> Marowak shiny"),
    133: ("quai", 93, 1, "Huyết ma (bản 2) -> Haunter shiny"),
    134: ("quai", 336, 1, "Rắn mang bành (bản 2) -> Seviper shiny"),
    135: ("quai", 901, 0, "Gấu xám lv90 -> Ursaluna"),
    136: ("quai", 510, 1, "Cọp tím lv82 -> Liepard shiny"),
    137: ("quai", 405, 0, "Cọp xanh lv84 -> Luxray"),
    138: ("boss", 244, 0, "Cọp đỏ lv86 -> Entei (huyền thoại)"),
}


def tai_json(url, ten_cache):
    os.makedirs(BO_NHO, exist_ok=True)
    p = os.path.join(BO_NHO, ten_cache)
    if os.path.exists(p):
        with open(p, encoding="utf8") as f:
            return json.load(f)
    req = urllib.request.Request(url, headers={"User-Agent": "htbb-pokemon-tool"})
    with urllib.request.urlopen(req, timeout=30) as r:
        d = json.loads(r.read().decode("utf8"))
    with open(p, "w", encoding="utf8") as f:
        json.dump(d, f)
    return d


def ten_hien_thi(ghi_chu, shiny):
    """'Rết xanh -> Whirlipede shiny' -> 'Whirlipede Shiny'; bo chu thich trong ngoac."""
    ten = ghi_chu.split("->", 1)[1]
    ten = re.sub(r"\(.*?\)", "", ten).replace(" shiny", "").strip()
    return (ten + " Shiny") if shiny else ten


def doc_quai():
    s = open(SQL, encoding="utf8").read()
    i = s.index("INSERT INTO `data_monster`")
    j = s.index(";\n", i)
    rows = re.findall(r"\((\d+), '((?:[^'\\]|\\.)*)', \d+, -?\d+, (-?\d+), (\d+),", s[i:j])
    return [(int(a), b, int(c), int(d)) for a, b, c, d in rows]


def main():
    tracker = tai_json(TRACKER, "tracker.json")
    loi = []
    dong = []
    for id_quai, ten, cap, hp in sorted(doc_quai()):
        if id_quai not in ANH_XA:
            loi.append("data_monster id %d (%s) CHUA anh xa trong ANH_XA" % (id_quai, ten))
            continue
        loai, dex, shiny, ghi = ANH_XA[id_quai]
        ten_pk = he1 = he2 = ""
        co_sprite = ""
        if dex:
            pk = tai_json(POKEAPI.format(dex), "pk_%d.json" % dex)
            ten_pk = pk["name"]
            hs = [t["type"]["name"] for t in sorted(pk["types"], key=lambda t: t["slot"])]
            he1 = HE.index(hs[0])
            he2 = HE.index(hs[1]) if len(hs) > 1 else -1
            t = tracker.get("%04d" % dex, {})
            co_sprite = t.get("sprite_complete", 0)
            if co_sprite < 1:
                loi.append("id %d: dex %d (%s) KHONG co sprite PMD" % (id_quai, dex, ten_pk))
        dong.append([id_quai, ten, cap, hp, loai, dex or "", ten_pk, shiny, he1, he2, co_sprite, ghi])
    thua = set(ANH_XA) - {d[0] for d in dong}
    for t in sorted(thua):
        loi.append("ANH_XA co id %d nhung data_monster khong co" % t)

    os.makedirs(os.path.dirname(RA), exist_ok=True)
    with open(RA, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f)
        w.writerow(["id_quai", "ten_cu", "cap", "maxhp", "phan_loai", "dex", "ten_pokemon",
                    "shiny", "he1", "he2", "pmd_sprite(2=du,1=co)", "ghi_chu"])
        w.writerows(dong)
        # Quai nam 2 (id 139+) do tools/pokemon/sinh_noi_dung.py them (data_monster nam trong
        # sql/pokemon_noi_dung.sql): noi lai nguyen van de chay lai script nay khong lam mat.
        them = os.path.join(GOC, "du_lieu", "noi_dung_quai_moi_bang.csv")
        if os.path.exists(them):
            with open(them, encoding="utf8", newline="") as g:
                w.writerows(list(csv.reader(g))[1:])
    with open(RA_SQL, "w", encoding="utf8", newline="\n") as f:
        f.write("-- SINH TU tools/pokemon/sinh_bang_quai.py - KHONG SUA TAY.\n")
        f.write("-- Chay SAU sql/them_cot_pokemon.sql. he = he 1 Pokemon (0..17, com.htbb.pokemon.HePokemon).\n")
        f.write("-- Chi dung cho server che_do_pokemon=1: cot he khong con la ngu hanh 0..4.\n")
        for d in dong:
            id_quai, dex, shiny, he1, he2 = d[0], d[5], d[7], d[8], d[9]
            if dex == "":
                f.write("UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=%d; -- %s\n" % (id_quai, d[11]))
            else:
                f.write("UPDATE data_monster SET name='%s', dex=%d, shiny=%d, he=%d, he2=%d WHERE id=%d; -- %s\n"
                        % (ten_hien_thi(d[11], shiny).replace("'", "''"), dex, shiny, he1, he2, id_quai, d[11]))
    print("Da ghi %s: %d dong" % (os.path.relpath(RA, GOC), len(dong)))
    print("Da ghi %s" % os.path.relpath(RA_SQL, GOC))
    for l in loi:
        print("LOI:", l)
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
