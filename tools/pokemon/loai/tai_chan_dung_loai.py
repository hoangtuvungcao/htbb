#!/usr/bin/env python3
"""
Chan dung Pokedex TINH (40x40, PMDCollab portrait "Normal") cho moi loai CHUA co goi sprite trong server/map/loai -
gom 64 loai PMDCollab chua ve sprite (chi co chan dung) va cac loai Gen 3-9 chua mo dot. Trang web /pokedex dung anh
nay; trong game loai chua mo / chua co sprite van ve bong (khong xuat hien ngoai troi cho toi khi co sprite that).

    python3 -I tools/pokemon/loai/tai_chan_dung_loai.py            # 1..1025, bo loai da co goi .goi
    python3 -I tools/pokemon/loai/tai_chan_dung_loai.py --kiem     # chi kiem (khong mang): moi loai 1..1025 co chan dung

Nguon: PMDCollab SpriteCollab o COMMIT GHIM (cung commit voi tai_sprite_loai.py), giay phep CC BY-NC 4.0:
    portrait/<NNNN>/Normal.png, portrait/<NNNN>/credits.txt
Ra: web/public/pokemon/loai/<dex>.png (commit; chan dung goc PMDCollab la 40x40, khong doi diem anh) va
    tools/pokemon/pmd_raw/chan_dung/<NNNN>/credits.txt (khong commit; ghi_cong_loai.py doc de ghi cong).
Du lieu tai ve KHONG tin cay: chi doc byte (kiem chu ky PNG + IHDR 40x40), khong bao gio chay.
"""
import json
import os
import struct
import sys
import urllib.error
import urllib.request

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
COMMIT = "4702f10d888d19e804c5986a91170dfc92ae01b8"
NGUON = "https://raw.githubusercontent.com/PMDCollab/SpriteCollab/" + COMMIT
TRACKER = os.path.join(GOC, "tools", "pokemon", "nguon_loai", "pmdcollab-" + COMMIT, "tracker.json")
GOI = os.path.join(GOC, "server", "map", "loai")
RA_WEB = os.path.join(GOC, "web", "public", "pokemon", "loai")
RAW = os.path.join(GOC, "tools", "pokemon", "pmd_raw", "chan_dung")
CU = os.path.join(GOC, "web", "public", "pokemon")


def tai(url):
    req = urllib.request.Request(url, headers={"User-Agent": "htbb-pokemon-tool"})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            return r.read()
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return None
        raise


def png_40(b):
    """True neu b la PNG 40x40 (doc IHDR, khong giai ma)."""
    if not b or len(b) < 24 or b[:8] != b"\x89PNG\r\n\x1a\n" or b[12:16] != b"IHDR":
        return False
    w, h = struct.unpack(">II", b[16:24])
    return w == 40 and h == 40


def co_goi(dex):
    return os.path.isfile(os.path.join(GOI, "%04d.goi" % dex))


def co_anh(dex):
    for p in (os.path.join(RA_WEB, "%d.png" % dex), os.path.join(CU, "%d.png" % dex)):
        if os.path.isfile(p):
            return True
    return False


def kiem():
    thieu = [d for d in range(1, 1026) if not co_goi(d) and not co_anh(d)]
    print("Chan dung Pokedex: %d / 1025 loai co (goi sprite hoac anh tinh)%s"
          % (1025 - len(thieu), (", THIEU: " + " ".join(map(str, thieu))) if thieu else ""))
    return 1 if thieu else 0


def main(argv):
    if "--kiem" in argv:
        return kiem()
    with open(TRACKER, encoding="utf8") as f:
        tracker = json.load(f)
    os.makedirs(RA_WEB, exist_ok=True)
    so = bo = loi = 0
    for dex in range(1, 1026):
        if co_goi(dex) or os.path.isfile(os.path.join(CU, "%d.png" % dex)):
            bo += 1   # da co goi sprite / chan dung web cu (public/pokemon/<dex>.png): khong tai lai
            continue
        k = "%04d" % dex
        e = tracker.get(k) or {}
        if e.get("portrait_complete", 0) < 1:
            print("LOI %s: tracker khong co chan dung" % k)
            loi += 1
            continue
        dich = os.path.join(RA_WEB, "%d.png" % dex)
        tm = os.path.join(RAW, k)
        os.makedirs(tm, exist_ok=True)
        if not os.path.isfile(os.path.join(tm, "credits.txt")):
            cr = tai("%s/portrait/%s/credits.txt" % (NGUON, k))
            if cr is not None:
                with open(os.path.join(tm, "credits.txt"), "wb") as f:
                    f.write(cr)
        if os.path.isfile(dich):
            with open(dich, "rb") as f:
                if png_40(f.read()):
                    so += 1
                    continue
        b = tai("%s/portrait/%s/Normal.png" % (NGUON, k))
        if not png_40(b):
            print("LOI %s: Normal.png khong phai PNG 40x40" % k)
            loi += 1
            continue
        with open(dich + ".tam", "wb") as f:
            f.write(b)
        os.replace(dich + ".tam", dich)
        so += 1
    print("Chan dung tinh: %d loai -> %s (%d loai da co goi sprite / chan dung web, bo qua), loi %d"
          % (so, os.path.relpath(RA_WEB, GOC), bo, loi))
    return 1 if loi else kiem()


if __name__ == "__main__":
    sys.exit(main(sys.argv))
