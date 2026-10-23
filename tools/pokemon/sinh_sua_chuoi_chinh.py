#!/usr/bin/env python3
"""Sinh khoi "SUA CHUOI CHINH" cua sql/htbb.sql (khoi them_huong_dan_tan_thu.sql) tu CSDL dang chay + server/cMap + server/map.

Kiem toan chuoi chinh (data_quest_moi mainsub=0) sau huong dan tan thu va in ra lenh SQL idempotent (REPLACE / SET theo
id) sua cac loi du lieu nguoi choi thay:

  1. "Dia diem" cua buoc ha quai / nhat do (type 0 / 2) tro ban do KHONG co quai muc tieu -> ban do quai that su moc
     (luoi map/map<N>, id nho nhat). Vd C03.5 "Ha 6 Rattata o Viridian": Rattata chi moc o Vermilion.
  2. Buoc noi chuyen "Noi chuyen voi X o Y" ma X khong dung o Y (NPC tinh chi o lang + vai noi) -> "o lang" (NPC co o
     ca ba lang Pewter / Blackthorn / Cianwood) hoac ban do co NPC.
  3. Buoc noi chuyen ma loi dau la mot NPC lang KHAC nguoi giao (vd C21.5 Chansey noi nhung Xatu giao) -> giao = NPC noi.

Chay o goc repo:  MYSQL="mysql -h127.0.0.1 -P3399 -uroot" python3 -I tools/pokemon/sinh_sua_chuoi_chinh.py > /tmp/khoi.sql
roi dan khoi vao giua hai dong danh dau SUA CHUOI CHINH cua sql/htbb.sql (khoi them_huong_dan_tan_thu.sql). Chi doc, khong ghi CSDL.
Bo qua cac id da sua tay trong tep SQL (BO_QUA).
"""
import os
import re
import shlex
import subprocess
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
LANG = (0, 70, 80)
NHAN_LANG = "Làng khởi đầu"
NHAN_LANG_NGAN = "làng"
# id da sua tay trong sql/htbb.sql (khoi them_huong_dan_tan_thu.sql) (huong dan + chuong 1-2)
BO_QUA = set(range(901, 911)) | {1001, 1005, 1010}
TEN_NPC = {0: "Chansey", 1: "Kecleon", 2: "Machamp", 3: "Bronzong", 5: "Xatu", 6: "Persian", 7: "Pidgeot", 8: "Snorlax",
           9: "Kangaskhan", 10: "Lapras", 21: "Blaziken", 22: "Meowth", 23: "Torkoal", 25: "Heracross", 16: "Tauros"}
# dia danh truyen -> ban do (tools/pokemon/nv_chung.DIA_DANH_MAP)
DIA_DANH = {"Pewter": 0, "Pallet": 1, "Viridian": 2, "Vermilion": 3, "Cerulean": 4, "Lavender": 5, "Celadon": 6,
            "Saffron": 7, "Fuchsia": 8, "Cinnabar": 9, "New Bark": 10, "Rừng Viridian": 11, "Khu mua bán": 12,
            "Núi Mặt Trăng": 13, "Vùng Safari": 14, "Cherrygrove": 15, "Khu mỏ": 17, "Goldenrod": 18, "Ecruteak": 19,
            "Violet": 20, "Đồng cỏ phía tây": 21, "Tàu S.S. Anne": 22, "Azalea": 23, "Olivine": 24, "Rừng Cổ": 26,
            "Mahogany": 28, "Rừng Ilex": 29, "Núi Silver": 42, "Blackthorn": 70, "Cianwood": 80, "Bến tàu": 106,
            "Hang Salamence": 112, "Hang Tối": 113, "Hồ Phẫn Nộ": 114, "Đường Victory": 118, "Cao nguyên Indigo": 201,
            "Tổ Zapdos": 110}


def sql(q):
    lenh = shlex.split(os.environ.get("MYSQL", "mysql")) + ["--default-character-set=utf8", "-N", "-B", "htbb_db", "-e", q]
    out = subprocess.run(lenh, check=True, capture_output=True).stdout.decode("utf-8")
    return [l.split("\t") for l in out.splitlines() if l]


def npc_cmap(id_):
    """Khoi NPC cuoi tep cMap (com.htbb.pokemon.DinhDangBanDo.doc)."""
    try:
        b = open(os.path.join(GOC, "server", "cMap", str(id_)), "rb").read()
    except OSError:
        return []
    p = 2 + b[0] * b[1]
    while b[p] != 255:
        p += 4 + b[p + 2] * b[p + 3]
    p += 1
    while b[p] != 255:
        p += 3
    p += 1
    while b[p] not in (255, 254):
        p += 3
    p += 2
    n = b[p]
    p += 1 + n * 6
    if p >= len(b):
        return []
    nn = b[p]
    p += 1
    return [b[p + 3 * i + 2] for i in range(nn)]


def quai_ban_do():
    kq = {}
    d = os.path.join(GOC, "server", "map")
    for f in os.listdir(d):
        if not re.fullmatch(r"map\d+", f):
            continue
        n = int(f[3:])
        b = open(os.path.join(d, f), "rb").read()
        for x in b[2:2 + b[0] * b[1]]:
            if x in (0, 255):
                continue
            if n == 17 and x > 81:
                x += 3
            kq.setdefault(x, set()).add(n)
    return kq


def q(s):
    return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'"


def main():
    npc = {}
    for f in os.listdir(os.path.join(GOC, "server", "cMap")):
        if f.isdigit():
            for t in npc_cmap(int(f)):
                npc.setdefault(t, set()).add(int(f))
    quai = quai_ban_do()
    ten_map = {}
    hoenn = set()
    for r in sql("SELECT map_id, ten, vung FROM data_map_ten"):
        ten_map[int(r[0])] = r[1]
        if r[2] == "Hoenn":
            hoenn.add(int(r[0]))

    def gan_nhat(o):
        """Ban do de toi nhat: ngoai Hoenn (cap 90) truoc, roi id nho."""
        return min(o, key=lambda m: (m in hoenn, m))

    dong = sql("SELECT q.id, q.type, q.npcrcv, q.npcres, q.idMonskill, q.content, q.scontent, q.infogift, IFNULL(m.ma,'')"
               " FROM data_quest_moi q LEFT JOIN data_quest_lau_dai m ON m.id_so = q.id WHERE q.mainsub = 0 ORDER BY q.id")
    ra = []
    for id_, ty, rcv, res, mons, content, sc, ig, ma in dong:
        id_, ty, rcv, res = int(id_), int(ty), int(rcv), int(res)
        if id_ in BO_QUA:
            continue
        m = re.search(r"Địa điểm:\|([^|]*)\|", ig)
        cu = m.group(1) if m else None
        moi = None
        ly_do = ""
        if ty in (0, 2):
            ms = [int(x) for x in mons.split(",") if x.strip().isdigit() and int(x) < 32000]
            if ms and quai.get(ms[0]):
                o = sorted(quai[ms[0]])
                if DIA_DANH.get(cu) not in o:
                    moi = ten_map.get(gan_nhat(o), cu)
                    ly_do = "quai %d chi moc o %s" % (ms[0], o)
        elif (ty == 3 and sc.startswith("Nói chuyện với")) or ty == 1:
            ai = rcv if ty == 3 else res  # mang do: noi den la NPC nhan
            o = npc.get(ai, set())
            if o and DIA_DANH.get(cu) not in o:
                moi = NHAN_LANG if any(l in o for l in LANG) else ten_map.get(gan_nhat(o), cu)
                ly_do = "NPC %d chi o %s" % (ai, sorted(o))
        cap_nhat = []
        ig_expr = "`infogift`"
        if moi and cu and moi != cu:
            ig_expr = "REPLACE(%s, %s, %s)" % (ig_expr, q("Địa điểm:|%s|" % cu), q("Địa điểm:|%s|" % moi))
            ngan = NHAN_LANG_NGAN if moi == NHAN_LANG else moi
            if ty in (1, 3):
                ig_expr = "REPLACE(%s, %s, %s)" % (ig_expr, q(" ở %s|" % cu), q(" ở %s|" % ngan))
            if sc.endswith(" ở " + cu):
                cap_nhat.append("`scontent` = IF(`scontent` LIKE %s, CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - %d), %s), `scontent`)"
                                % (q("% ở " + cu), len(" ở " + cu), q(" ở " + ngan)))
        if ty == 3:
            mo = re.match(r"1([^:>]+):", content)
            noi = mo.group(1).strip() if mo else ""
            ma_noi = [k for k, v in TEN_NPC.items() if v == noi]
            if ma_noi and ma_noi[0] != rcv and rcv in TEN_NPC and any(l in npc.get(ma_noi[0], ()) for l in LANG):
                k = ma_noi[0]
                cap_nhat.append("`npcrcv` = %d" % k)
                ig_expr = "REPLACE(%s, %s, %s)" % (ig_expr, q("NPC:|%s|" % TEN_NPC[rcv]), q("NPC:|%s|" % noi))
                ly_do += ("; " if ly_do else "") + "loi dau cua %s, giao boi %s" % (noi, TEN_NPC[rcv])
                ra.append("UPDATE `data_quest_lau_dai` SET `npc_giao` = %d, `ten_npc` = %s WHERE `id_so` = %d;" % (k, q(noi), id_))
        if ig_expr != "`infogift`":
            cap_nhat.append("`infogift` = LEFT(%s, 1000)" % ig_expr)  # cot varchar(1000) (sua-tuyen; cu 200 cat chu)
        if cap_nhat:
            ra.append("-- %d %s: %s" % (id_, ma, ly_do))
            ra.append("UPDATE `data_quest_moi` SET %s WHERE `id` = %d AND `mainsub` = 0;" % (", ".join(cap_nhat), id_))
    print("\n".join(ra))
    print("-- %d lenh" % sum(1 for l in ra if l.startswith("UPDATE")), file=sys.stderr)


if __name__ == "__main__":
    main()
