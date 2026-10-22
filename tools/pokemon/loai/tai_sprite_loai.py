#!/usr/bin/env python3
"""
Tai sprite PMDCollab SpriteCollab o COMMIT GHIM cho mot dai so Pokedex (mac dinh 1..251 = Gen 1-2).

    python3 -I tools/pokemon/loai/tai_sprite_loai.py            # 1..251
    python3 -I tools/pokemon/loai/tai_sprite_loai.py 252 386    # dai khac (Gen 3)
    python3 -I tools/pokemon/loai/tai_sprite_loai.py --shiny 1 251   # ban SHINY (sac mau) cua PMDCollab

Shiny: PMDCollab de ban sac mau o nhanh con 0000/0001 cua moi loai (tracker: subgroups 0000 -> subgroups 0001,
ten "Shiny"; tep: sprite/<NNNN>/0000/0001/, portrait/<NNNN>/0000/0001/Normal.png). Ra tools/pokemon/pmd_raw/<NNNN>s/.
Chi tai khi nhanh shiny DUNG DUOC (cung luat voi ban thuong: sprite_complete >= 1 va co Walk). Khong co thi loai do
"chua co shiny": KHONG tu doi mau / suy bang mau (khong co nguon bang mau shiny chinh thuc co giay phep).

Ra: tools/pokemon/pmd_raw/<NNNN>/ (AnimData.xml, <Anim>-{Anim,Shadow,Offsets}.png, credits.txt, portrait.png).
Thu muc nay KHONG commit (.gitignore). Neu thu muc cua repo chinh (bien HTBB_PMD_RAW_CU, mac dinh
<goc repo chinh>/tools/pokemon/pmd_raw) da co loai do thi chep sang thay vi tai lai.

Chi tai cac loai tracker.json (cung commit ghim) danh dau co sprite (co Walk). Loai khong co sprite dung duoc
thi bo qua: van co trong Pokedex (chan dung) nhung khong xuat hien ngoai troi.

Uu tien dong tac PHAI giong DONG_TAC trong tools/pokemon/tai_sprite_pmd.py va tools/pokemon/DongGoiSprite.java.

GIAY PHEP: CC BY-NC 4.0 - khong thuong mai, PHAI ghi cong (credits.txt tung loai -> GHI_CONG.txt).
Du lieu tai ve la du lieu KHONG tin cay: chi doc bang python3 -I, khong bao gio chay.
"""
import json
import os
import shutil
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
COMMIT = "4702f10d888d19e804c5986a91170dfc92ae01b8"
NGUON = "https://raw.githubusercontent.com/PMDCollab/SpriteCollab/" + COMMIT
TRACKER = os.path.join(GOC, "tools", "pokemon", "nguon_loai", "pmdcollab-" + COMMIT, "tracker.json")
RAW = os.path.join(GOC, "tools", "pokemon", "pmd_raw")

DONG_TAC = [
    ("dung", ["Idle", "Walk"]),
    ("chay", ["Walk"]),
    ("danh_gan", ["Attack", "Strike", "QuickStrike", "Swing"]),
    ("danh_xa", ["Shoot", "Charge", "Attack"]),
    ("trung_don", ["Hurt", "Pain"]),
    ("guc_nga", ["Faint", "Sleep", "Hurt"]),
]


def raw_cu():
    r = os.environ.get("HTBB_PMD_RAW_CU")
    if r:
        return r
    # worktree: <repo chinh>/.claude/worktrees/<ten>/ -> <repo chinh>/tools/pokemon/pmd_raw
    p = os.path.abspath(os.path.join(GOC, "..", "..", "..", "tools", "pokemon", "pmd_raw"))
    return p if os.path.isdir(p) else ""


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
    with open(dich + ".tam", "wb") as f:
        f.write(data)
    os.replace(dich + ".tam", dich)
    return True


def doc_anim(xml_path):
    """ten anim -> (rong, cao, so_khung, ten_file_goc); CopyOf giai ve anim goc."""
    goc = ET.parse(xml_path).getroot()
    tho = {}
    for a in goc.iter("Anim"):
        ten = a.findtext("Name")
        copy = a.findtext("CopyOf")
        if copy:
            tho[ten] = copy
        else:
            tho[ten] = (int(a.findtext("FrameWidth")), int(a.findtext("FrameHeight")),
                        len(a.findall("Durations/Duration")))
    kq = {}
    for ten, v in tho.items():
        goc_ten = ten
        for _ in range(10):
            if not isinstance(v, str):
                break
            goc_ten = v
            v = tho.get(v)
        if v and not isinstance(v, str):
            kq[ten] = v + (goc_ten,)
    return kq


def dung_duoc(e):
    return bool(e) and e.get("sprite_complete", 0) > 0 and "Walk" in (e.get("sprite_files") or {})


def nhanh_shiny(e):
    """Nhanh shiny (0000/0001) cua mot loai trong tracker.json, None neu khong co."""
    return ((((e or {}).get("subgroups") or {}).get("0000") or {}).get("subgroups") or {}).get("0001")


def main(argv):
    shiny = "--shiny" in argv
    argv = [a for a in argv if a != "--shiny"]
    tu = int(argv[1]) if len(argv) > 1 else 1
    den = int(argv[2]) if len(argv) > 2 else 251
    with open(TRACKER, encoding="utf8") as f:
        tracker = json.load(f)
    cu = raw_cu()
    os.makedirs(RAW, exist_ok=True)
    so_tai = so_chep = so_bo = loi = 0
    bo = []
    for dex in range(tu, den + 1):
        k = "%04d" % dex
        e = tracker.get(k)
        if shiny:
            e = nhanh_shiny(e)
        if not dung_duoc(e):
            bo.append(k)
            so_bo += 1
            continue
        tm = os.path.join(RAW, k + ("s" if shiny else ""))
        if not shiny and not os.path.isfile(os.path.join(tm, "AnimData.xml")) and cu and os.path.isfile(os.path.join(cu, k, "AnimData.xml")):
            shutil.copytree(os.path.join(cu, k), tm, dirs_exist_ok=True)
            so_chep += 1
        os.makedirs(tm, exist_ok=True)
        base = "%s/sprite/%s%s" % (NGUON, k, "/0000/0001" if shiny else "")
        xml = os.path.join(tm, "AnimData.xml")
        if not tai(base + "/AnimData.xml", xml):
            print("LOI %s: khong co AnimData.xml" % k)
            loi += 1
            continue
        tai(base + "/credits.txt", os.path.join(tm, "credits.txt"))
        tai("%s/portrait/%s%s/Normal.png" % (NGUON, k, "/0000/0001" if shiny else ""), os.path.join(tm, "portrait.png"))
        anims = doc_anim(xml)
        thieu = []
        for ten_game, uu_tien in DONG_TAC:
            chon = next((a for a in uu_tien if a in anims), None)
            if chon is None:
                thieu.append(ten_game)
                continue
            ten_file = anims[chon][3]
            for hau_to in ("Anim", "Shadow", "Offsets"):
                tai("%s/%s-%s.png" % (base, ten_file, hau_to), os.path.join(tm, "%s-%s.png" % (ten_file, hau_to)))
        if thieu:
            loi += 1
            print("LOI %s: thieu dong tac %s" % (k, ",".join(thieu)))
        so_tai += 1
    print(("SHINY " if shiny else "") + "Dai %d..%d: %d loai co sprite (%d chep tu repo chinh), %d khong co sprite dung duoc%s, %d loi"
          % (tu, den, so_tai, so_chep, so_bo, (" (" + " ".join(bo) + ")") if bo else "", loi))
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
