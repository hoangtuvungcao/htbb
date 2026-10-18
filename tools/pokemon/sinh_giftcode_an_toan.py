#!/usr/bin/env python3
"""Sinh danh sach TRANG (whitelist) vat pham cho giftcode an toan - dung CHUNG cho web va server game.

Ra:
  ../HTBB-Web/shared/data/giftcode-an-toan.json     nguon chung (web: server/utils/giftcodeAnToan.ts doc truc tiep)
  src/com/htbb/pokemon/GiftcodeAnToanDuLieu.java    ban Java (server khong co thu vien JSON), kem SHA-256 cua JSON
Ca hai ben chay CUNG bo ca "thuNghiem" (HTBB-Web: npm test; HTBB: test/KiemItemMoCoi.java).

Quy tac (yeu cau 18/10/2026, luat "khong item mo coi" - tools/pokemon/kiem_item_mo_coi.py):
  - Giftcode chi phat vat pham KHOA, khong cao cap: tieu hao / nguyen lieu / tin vat su kien (dai VAT_PHAM),
    trang bi trong catalog (du_lieu/noi_dung_trang_bi.csv) voi cap mon <= cap nguoi nhan (web: <= cap_toi_thieu
    cua ma; server: <= cap nhan vat luc nhap). Khong do ban bang luong (thoi trang, thu cuoi, pet, skin, canh, thoi
    trang su kien...), khong vat pham "doi" cao cap (dong xu, ruong may man...).
  - Ngoc: moi ngoc co that (0..NGOC_TOI_DA) tru ngoc ban bang luong o cua hang ngoc (sell=1, typeMoney=1,
    MapNapTaiNguyen.loadGemTemplate) va sach ky nang pet cu (269, da nghi huu).
Chi thu vien chuan, tat dinh. Chay o HTBB/:  python3 -I tools/pokemon/sinh_giftcode_an_toan.py
"""
import csv
import hashlib
import json
import os
import re

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
WEB = os.path.join(os.path.dirname(GOC), "HTBB-Web")
RA_JSON = os.path.join(WEB, "shared", "data", "giftcode-an-toan.json")
RA_JAVA = os.path.join(GOC, "server", "src", "com", "htbb", "pokemon", "GiftcodeAnToanDuLieu.java")

# Tieu hao / nguyen lieu / tin vat (data_item type 28) phat duoc: Keo Poke, da tien hoa, nguyen lieu + tin vat + hop
# noi dung, tieu hao / do giu / bua dai han. KHONG: 720/721 (pet, ban luong), 729..739 (choi, dong xu, ruong),
# 751..753 (shop VIP), 807..831 (ban sao ngoc ban luong), 835/836 (vu khi thoi trang, the doi ten).
VAT_PHAM = [(714, 714), (960, 967), (1034, 1061), (1201, 1260)]
NGOC_TOI_DA = 268
NGOC_NGHI_HUU = [269]


def ngoc_ban_luong():
    """Ngoc sell=1 va typeMoney=1 trong khoi ban Viet cua MapNapTaiNguyen.loadGemTemplate."""
    s = open(os.path.join(GOC, "server", "src", "real", "MapNapTaiNguyen.java"), encoding="utf-8").read()
    i0 = s.index("static void loadGemTemplate()")
    i1 = s.index("Map.gemTemplate = new GemTemplate[]{", s.index("} else {", i0))
    i2 = s.index("};\n", i1)
    ra = []
    for m in re.finditer(r"new GemTemplate\(\s*(\d+),((?:[^()]|\([^()]*\))*)\)", s[i1:i2], re.S):
        so = [x.strip() for x in re.split(r",(?![^()]*\))", m.group(2))]
        if len(so) >= 9 and so[-2] in ("0", "1"):
            # (id, idImg, ten, mo ta, gia tri, he, gia, kieu, sell, typeMoney) | (id, idImg, ten, mo ta, typeItem, he, kieu, gia tri, sell, typeMoney, gia)
            sell, tien = (so[-2], so[-1]) if len(so) == 9 else (so[-3], so[-2])
            if sell == "1" and tien == "1":
                ra.append(int(m.group(1)))
    return sorted(ra)


def trang_bi():
    ra = set()
    for r in csv.DictReader(open(os.path.join(GOC, "du_lieu", "noi_dung_trang_bi.csv"), encoding="utf-8")):
        if (r.get("id_item") or "").isdigit():
            ra.add(int(r["id_item"]))
    return sorted(ra)


THU_NGHIEM = [
    # thuong, cap nguoi nhan, cap cua cac mon trang bi, ket qua mong doi (true = hop le)
    {"thuong": "ITEM:1214:2,LUONGKHOA:5", "cap": 1, "capItem": {}, "ok": True},
    {"thuong": "ITEM:960:1,GEM:8:3:-1", "cap": 25, "capItem": {}, "ok": True},
    {"thuong": "ITEM:1039:5", "cap": 1, "capItem": {}, "ok": True},
    {"thuong": "ITEM:735:1", "cap": 50, "capItem": {}, "ok": False},      # ruong may man (cao cap)
    {"thuong": "ITEM:836:1", "cap": 50, "capItem": {}, "ok": False},      # the doi ten (luong)
    {"thuong": "ITEM:736:1", "cap": 50, "capItem": {"736": 1}, "ok": False},  # ao thoi trang ban luong
    {"thuong": "ITEM:905:1", "cap": 50, "capItem": {"905": 1}, "ok": False},  # skin thu cuoi
    {"thuong": "ITEM:23:1", "cap": 60, "capItem": {"23": 54}, "ok": True},    # khan quang kim cuong, cap 54 <= 60
    {"thuong": "ITEM:23:1", "cap": 30, "capItem": {"23": 54}, "ok": False},   # tren bac nguoi nhan
    {"thuong": "ITEM:1008:1", "cap": 90, "capItem": {"1008": 90}, "ok": True},
    {"thuong": "ITEM:99999:1", "cap": 100, "capItem": {}, "ok": False},
    {"thuong": "GEM:106:1", "cap": 1, "capItem": {}, "ok": False},        # to lua cap 4: ban bang luong
    {"thuong": "GEM:269:1", "cap": 1, "capItem": {}, "ok": False},        # sach ky nang pet cu
    {"thuong": "GEM:300:1", "cap": 1, "capItem": {}, "ok": False},
    {"thuong": "GEM:0:2,GEM:73:5", "cap": 1, "capItem": {}, "ok": True},
]


def main():
    d = {
        "_ghiChu": "SINH RA boi HTBB/tools/pokemon/sinh_giftcode_an_toan.py - KHONG SUA TAY. Giftcode an toan: vat pham duoc phat.",
        "vatPham": [list(x) for x in VAT_PHAM],
        "trangBi": trang_bi(),
        "ngocToiDa": NGOC_TOI_DA,
        "ngocCam": sorted(set(ngoc_ban_luong() + NGOC_NGHI_HUU)),
        "thuNghiem": THU_NGHIEM,
    }
    raw = (json.dumps(d, ensure_ascii=False, indent=1) + "\n").encode("utf-8")
    os.makedirs(os.path.dirname(RA_JSON), exist_ok=True)
    open(RA_JSON, "wb").write(raw)
    sha = hashlib.sha256(raw).hexdigest()

    def mang(a):
        return "{" + ", ".join(str(x) for x in a) + "}"

    j = ["package com.htbb.pokemon;\n\n",
         "/**\n * SINH RA boi tools/pokemon/sinh_giftcode_an_toan.py - KHONG SUA TAY. Ban Java cua\n",
         " * HTBB-Web/shared/data/giftcode-an-toan.json (danh sach vat pham giftcode an toan duoc phat). Dung o\n",
         " * GiftcodeAnToanLogic.kiemVatPham; test/KiemItemMoCoi.java so SHA-256 voi file JSON.\n */\n",
         "public final class GiftcodeAnToanDuLieu {\n\n",
         "    public static final String SHA256_JSON = \"%s\";\n" % sha,
         "    /** Dai id [tu, den] tieu hao / nguyen lieu / tin vat phat duoc. */\n",
         "    public static final int[][] VAT_PHAM = {%s};\n" % ", ".join(mang(x) for x in VAT_PHAM),
         "    /** Trang bi catalog (noi_dung_trang_bi.csv): phat duoc khi cap mon <= cap nguoi nhan. */\n",
         "    public static final int[] TRANG_BI = %s;\n" % mang(d["trangBi"]),
         "    public static final int NGOC_TOI_DA = %d;\n" % NGOC_TOI_DA,
         "    /** Ngoc ban bang luong / da nghi huu: khong phat. */\n",
         "    public static final int[] NGOC_CAM = %s;\n\n" % mang(d["ngocCam"]),
         "    private GiftcodeAnToanDuLieu() {\n    }\n}\n"]
    open(RA_JAVA, "w", encoding="utf-8", newline="\n").write("".join(j))
    print("da ghi %s (%d trang bi, %d ngoc cam) + %s" % (RA_JSON, len(d["trangBi"]), len(d["ngocCam"]), RA_JAVA))


if __name__ == "__main__":
    main()
