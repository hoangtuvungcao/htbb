#!/usr/bin/env python3
"""
Kiểm chồng lấn giao diện HD của APK (HdKiem ghi ra logcat thẻ HTBBKIEM khi bật debug.htbb.kiem=1).

    adb shell setprop debug.htbb.kiem 1
    ... mở các màn ...
    python3 -I client_apk/pokemon/tools/kiem_chong_lan.py [-s emulator-5554] [--tat-ca]

Mỗi bản ghi: màn đang hiện, cỡ màn logic W x H, vùng an toàn, danh sách hình chữ nhật
[loại, trái, trên, phải, dưới, "chữ"]: loại 0 = chữ game (đã cắt theo clip), 1 = nút theo ngữ cảnh
(HdNut), 2 = nút / thẻ HUD. Lỗi (mã thoát 1):
  - HUD / nút ngữ cảnh chồng nhau, ra ngoài màn hoặc lấn vùng tai thỏ;
  - nút ngữ cảnh đè lên chữ;
  - chữ đè chữ (giao > 35% chữ nhỏ hơn) trong cùng khung.
Thông tin (không lỗi): chữ thế giới (tên, "vào", số sát thương) nằm dưới nút HUD.
"""
import json
import os
import subprocess
import sys


def doc(adb_args):
    out = subprocess.run([os.environ.get("ADB", "adb")] + adb_args + ["logcat", "-d", "-s", "HTBBKIEM"], capture_output=True, text=True).stdout
    ban = []
    manh = []
    for dong in out.splitlines():
        i = dong.find("HTBBKIEM:")
        if i < 0:
            continue
        noi = dong[i + 9:].strip()
        so, _, du = noi.partition(" ")
        a, _, b = so.partition("/")
        if a == "1":
            manh = []
        manh.append(du)
        if a == b:
            try:
                ban.append(json.loads("".join(manh)))
            except ValueError:
                pass
            manh = []
    return ban


def giao(a, b):
    w = min(a[3], b[3]) - max(a[1], b[1])
    h = min(a[4], b[4]) - max(a[2], b[2])
    return w * h if w > 0 and h > 0 else 0


def tron(a):
    return isinstance(a[5], str) and a[5].startswith("o:")


def chong(a, b):
    """giao của 2 phần tử HUD; hai hình tròn ("o:...") thì xét theo khoảng cách tâm."""
    if tron(a) and tron(b):
        ra, rb = (a[3] - a[1]) / 2, (b[3] - b[1]) / 2
        dx = (a[1] + a[3]) / 2 - (b[1] + b[3]) / 2
        dy = (a[2] + a[4]) / 2 - (b[2] + b[4]) / 2
        return (dx * dx + dy * dy) ** 0.5 < ra + rb - 1.5
    return giao(a, b) > 2


def dt(a):
    return max(1, (a[3] - a[1]) * (a[4] - a[2]))


def kiem(bg):
    W, H = bg["W"], bg["H"]
    an = bg.get("an", [0, 0, 0, 0])
    o = bg["o"]
    loi, tin = [], []
    widget = [x for x in o if x[0] in (1, 2)]
    # chữ bị thẻ vẽ sau che (hộp thoại / bảng mở đè lên màn): bỏ khỏi kiểm chữ đè chữ
    chu = []
    for i, x in enumerate(o):
        if x[0] != 0:
            continue
        cx, cy = (x[1] + x[3]) / 2, (x[2] + x[4]) / 2
        bi_che = any(y[0] == 3 and y[1] <= cx <= y[3] and y[2] <= cy <= y[4] for y in o[i + 1:])
        if not bi_che:
            chu.append(x)
    for i, a in enumerate(widget):
        if a[1] < an[0] - 1 or a[2] < an[1] - 1 or a[3] > W - an[2] + 1 or a[4] > H - an[3] + 1:
            loi.append("ra ngoài vùng an toàn: %s %s" % (a[5], a[1:5]))
        for b in widget[i + 1:]:
            if chong(a, b):
                loi.append("chồng: %s x %s" % (a[5], b[5]))
    for a in [x for x in widget if x[0] == 1]:
        for c in chu:
            if giao(a, c) > 0.2 * dt(c):
                loi.append("nút '%s' đè chữ '%s'" % (a[5], c[5]))
    for i, a in enumerate(chu):
        for b in chu[i + 1:]:
            if a[5] == b[5] and a[1:5] == b[1:5]:
                continue   # vẽ 2 lần cùng chỗ (bóng chữ)
            g = giao(a, b)
            if g > 0.35 * min(dt(a), dt(b)):
                loi.append("chữ đè chữ: '%s' x '%s'" % (a[5], b[5]))
    for a in [x for x in widget if x[0] == 2]:
        for c in chu:
            if giao(a, c) > 0.3 * dt(c):
                tin.append("chữ thế giới dưới HUD: '%s' dưới %s" % (c[5], a[5]))
    return loi, tin


def main():
    adb = []
    tat_ca = "--tat-ca" in sys.argv
    if "-s" in sys.argv:
        adb = ["-s", sys.argv[sys.argv.index("-s") + 1]]
    ban = doc(adb)
    if not ban:
        print("không có bản ghi (adb shell setprop debug.htbb.kiem 1 ?)")
        return 2
    theo_man = {}
    for bg in ban:
        theo_man[(bg["man"], bg["W"], bg["H"])] = bg     # bản mới nhất mỗi màn / cỡ
    tong = 0
    for (man, W, H), bg in sorted(theo_man.items()):
        loi, tin = kiem(bg)
        tong += len(loi)
        print("%-40s %dx%d  %d phần tử  lỗi %d  ghi chú %d" % (man, W, H, len(bg["o"]), len(loi), len(tin)))
        for x in sorted(set(loi)):
            print("   LỖI  " + x)
        if tat_ca:
            for x in sorted(set(tin)):
                print("   tin  " + x)
    print("TỔNG LỖI:", tong)
    return 1 if tong else 0


sys.exit(main())
