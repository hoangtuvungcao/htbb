"""Đọc / ghi phienban.json cho phat-hanh.sh.

    python -I phienban.py ban-ke-tiep <phienban.json> <pc|android|jar>
        -> in số bản kế tiếp (bản hiện có + 1; android tối thiểu 1419 vì APK gốc TeaMobi là 1418)
    python -I phienban.py ghi <phienban.json> <pc|android|jar> <ban> <link> <file> [ghi chú]
        -> cập nhật mục pc/android (sha256 + kích thước tính từ <file>), giữ nguyên mục còn lại
"""
import hashlib
import json
import os
import sys
import time

TOI_THIEU = {"pc": 1, "android": 1419, "jar": 1}


def doc(p):
    try:
        with open(p, encoding="utf-8") as f:
            d = json.load(f)
        return d if isinstance(d, dict) else {}
    except Exception:
        return {}


lenh, p, loai = sys.argv[1:4]
assert loai in TOI_THIEU, loai
d = doc(p)

if lenh == "ban-ke-tiep":
    print(max(int((d.get(loai) or {}).get("ban", 0)) + 1, TOI_THIEU[loai]))
elif lenh == "ghi":
    ban, link, file = int(sys.argv[4]), sys.argv[5], sys.argv[6]
    ghi_chu = sys.argv[7] if len(sys.argv) > 7 else ""
    h = hashlib.sha256()
    with open(file, "rb") as f:
        for khoi in iter(lambda: f.read(1 << 20), b""):
            h.update(khoi)
    d[loai] = {
        "ban": ban,
        "link": link,
        "sha256": h.hexdigest(),
        "kichThuoc": os.path.getsize(file),
        "ghiChu": ghi_chu,
        "ngay": time.strftime("%Y-%m-%d %H:%M"),
    }
    with open(p, "w", encoding="utf-8") as f:
        json.dump(d, f, ensure_ascii=False, indent=2)
    print(json.dumps(d[loai]))  # ASCII: console Windows (cp1252) khong in duoc tieng Viet
else:
    raise SystemExit("lenh khong hop le: " + lenh)
