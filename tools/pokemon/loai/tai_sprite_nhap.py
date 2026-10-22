#!/usr/bin/env python3
"""
Sprite BAN NHAP / DANG KHAC cua PMDCollab cho loai KHONG co sprite dung duoc (tai_sprite_loai.py bo qua).

    python3 -I tools/pokemon/loai/tai_sprite_nhap.py            # moi loai 1..1025 chua dung duoc
    python3 -I tools/pokemon/loai/tai_sprite_nhap.py --shiny    # ban sac mau tuong ung

Thu tu nguon (CHI hinh that cua PMDCollab o commit ghim, CC BY-NC 4.0, ghi cong theo credits.txt cua chinh nhanh do):
  1. mot DANG KHAC cua loai co sprite day du (sprite_complete >= 1 va co Walk; bo Mega / Gigantamax / Shiny / Female):
     vd Falinks "Trooper", Gimmighoul "Roaming", Miraidon "Low_Power", Ting-Lu "Temp";
  2. sprite BAN NHAP cua dang goc (sprite_complete = 0 nhung repo co tep: it nhat Idle hoac Walk).
Dong tac con thieu dung lai KHUNG cua chinh nguoi ve bang muc <CopyOf> chuan cua AnimData.xml (vd Walk = Idle,
Attack = Swing) - khong ve them, khong doi diem anh. Ra tools/pokemon/pmd_raw/<NNNN>[s]/ + NHAP.txt (nguon, dong tac
muon). Loai khong co gi (chi co anh nhap tren Discord, khong co tep trong repo) thi bo qua: van chi co chan dung Pokedex.
Du lieu tai ve KHONG tin cay: chi doc / sua XML bang ElementTree, khong bao gio chay.
"""
import json
import os
import sys
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import tai_sprite_loai as T  # noqa: E402

BO_DANG = ("Mega", "Gigantamax", "Shiny", "Female", "Altcolor")
#: dong tac game -> thu tu muon khi thieu (sau DONG_TAC uu tien cua tai_sprite_loai)
MUON = {
    "dung": ["Idle", "Walk", "Rotate"],
    "chay": ["Walk", "Hop", "Idle", "Rotate"],
    "danh_gan": ["Attack", "Strike", "QuickStrike", "Swing", "Double", "Charge", "Shoot", "Hop", "Idle"],
    "danh_xa": ["Shoot", "Charge", "Attack", "Swing", "Double", "Strike", "Idle"],
    "trung_don": ["Hurt", "Pain", "Idle"],
    "guc_nga": ["Faint", "Sleep", "Hurt", "Idle"],
}
#: ten anim chuan ma bo dong goi tim cho tung dong tac (dau danh sach DONG_TAC)
TEN_CHUAN = {"dung": "Idle", "chay": "Walk", "danh_gan": "Attack", "danh_xa": "Shoot", "trung_don": "Hurt", "guc_nga": "Faint"}


def co_tep(e):
    return bool(e) and bool(e.get("sprite_files"))


def chon_nguon(e):
    """(duong con trong sprite/<NNNN>, ten, la ban nhap) hoac None."""
    if T.dung_duoc(e):
        return None
    for k, v in sorted((e.get("subgroups") or {}).items()):
        if k == "0000" or v.get("name") in BO_DANG:
            continue
        if T.dung_duoc(v):
            return "/" + k, v.get("name"), False, v
    sf = e.get("sprite_files") or {}
    if "Walk" in sf or "Idle" in sf:
        return "", e.get("name"), True, e
    return None


def nhanh_shiny_cua(e, duong):
    """Nhanh shiny cua nguon (duong '' -> 0000/0001, '/000F' -> 000F/0001)."""
    g = e
    for k in [p for p in duong.split("/") if p] or ["0000"]:
        g = (g.get("subgroups") or {}).get(k) or {}
    return (g.get("subgroups") or {}).get("0001")


def main(argv):
    shiny = "--shiny" in argv
    with open(T.TRACKER, encoding="utf8") as f:
        tracker = json.load(f)
    so = 0
    ds = []
    for dex in range(1, 1026):
        k = "%04d" % dex
        e = tracker.get(k) or {}
        n = chon_nguon(e)
        if n is None:
            continue
        duong, ten, nhap, nut = n
        if shiny:
            nut = nhanh_shiny_cua(e, duong)
            if not co_tep(nut) or not ("Walk" in nut["sprite_files"] or "Idle" in nut["sprite_files"]):
                continue
            duong = (duong or "/0000") + "/0001"
        tm = os.path.join(T.RAW, k + ("s" if shiny else ""))
        os.makedirs(tm, exist_ok=True)
        base = "%s/sprite/%s%s" % (T.NGUON, k, duong)
        xml = os.path.join(tm, "AnimData.xml")
        if not T.tai(base + "/AnimData.xml", xml):
            print("BO %s: nhanh %s khong co AnimData.xml trong repo" % (k, duong or "/"))
            continue
        T.tai(base + "/credits.txt", os.path.join(tm, "credits.txt"))
        T.tai("%s/portrait/%s%s/Normal.png" % (T.NGUON, k, "/0000/0001" if shiny else ""), os.path.join(tm, "portrait.png"))
        anims = T.doc_anim(xml)
        co = {a for a in anims if os.path.isfile(os.path.join(tm, anims[a][3] + "-Anim.png"))
              or T.tai("%s/%s-Anim.png" % (base, anims[a][3]), os.path.join(tm, anims[a][3] + "-Anim.png"))}
        # chi giu anim co tep that; muon dong tac thieu bang CopyOf
        goc = ET.parse(xml)
        ds_anim = goc.getroot().find("Anims")
        muon = []
        for ten_game, uu_tien in T.DONG_TAC:
            if any(a in co for a in uu_tien):
                continue
            nguon = next((a for a in MUON[ten_game] if a in co), None)
            if nguon is None:
                continue
            moi = ET.SubElement(ds_anim, "Anim")
            ET.SubElement(moi, "Name").text = TEN_CHUAN[ten_game]
            ET.SubElement(moi, "CopyOf").text = nguon
            co.add(TEN_CHUAN[ten_game])
            muon.append("%s=%s" % (TEN_CHUAN[ten_game], nguon))
        # bo cac Anim tro toi tep khong co (ban nhap thieu tep)
        for a in list(ds_anim.findall("Anim")):
            if a.findtext("CopyOf") is None and a.findtext("Name") not in co:
                ds_anim.remove(a)
        goc.write(xml, encoding="utf-8", xml_declaration=True)
        anims = T.doc_anim(xml)
        for a in anims.values():
            for hau_to in ("Shadow", "Offsets"):
                T.tai("%s/%s-%s.png" % (base, a[3], hau_to), os.path.join(tm, "%s-%s.png" % (a[3], hau_to)))
        with open(os.path.join(tm, "NHAP.txt"), "w", encoding="utf-8") as f:
            f.write("nguon=sprite/%s%s\nten=%s\nban_nhap=%d\nmuon=%s\n" % (k, duong, ten, 1 if nhap else 0, ",".join(muon)))
        ds.append("%s(%s%s)" % (k, "nhap" if nhap else "dang " + ten, ", muon " + ",".join(muon) if muon else ""))
        so += 1
    print(("SHINY " if shiny else "") + "Ban nhap / dang khac: %d loai: %s" % (so, " ".join(ds)))
    ghi_danh_sach()
    return 0


def ghi_danh_sach():
    """du_lieu/loai/sprite_nhap.csv (commit): moi thu muc pmd_raw co NHAP.txt -> khoa, nguon, ten, ban_nhap, muon."""
    ra = os.path.join(T.GOC, "du_lieu", "loai", "sprite_nhap.csv")
    dong = []
    for k in sorted(os.listdir(T.RAW)):
        f = os.path.join(T.RAW, k, "NHAP.txt")
        if not os.path.isfile(f):
            continue
        kv = dict(x.split("=", 1) for x in open(f, encoding="utf-8").read().splitlines() if "=" in x)
        dong.append([k, kv.get("nguon", ""), kv.get("ten", ""), kv.get("ban_nhap", "0"), kv.get("muon", "")])
    with open(ra, "w", encoding="utf-8", newline="\n") as f:
        f.write("khoa,nguon,ten,ban_nhap,muon\n")
        for d in dong:
            f.write(",".join(x.replace(",", ";") for x in d) + "\n")
    print("Da ghi %s (%d dong)" % (os.path.relpath(ra, T.GOC), len(dong)))


if __name__ == "__main__":
    sys.exit(main(sys.argv))
