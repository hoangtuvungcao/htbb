"""Đọc / ghi phienban.json cho phat-hanh.sh (repo public kpah-server/nqsh-h2-tai, qua GitHub CLI).

    python -I phienban.py tai-ve <phienban.json>
        -> tải phienban.json hiện có trên repo (không có thì ghi {}); tạo README nếu repo còn trống
    python -I phienban.py ban-ke-tiep <phienban.json> <pc|android|jar>
        -> in số bản kế tiếp (bản hiện có + 1; android tối thiểu 1419 vì APK gốc TeaMobi là 1418)
    python -I phienban.py ghi <phienban.json> <pc|android|jar> <ban> <link> <file> [ghi chú]
        -> cập nhật mục pc/android/jar (sha256 + kích thước tính từ <file>), giữ nguyên mục còn lại
    python -I phienban.py day-len <phienban.json> <commit message>
        -> ghi phienban.json lên nhánh main của repo

Biến môi trường: GH_EXE (đường dẫn Windows tới gh.exe), REPO (mặc định kpah-server/nqsh-h2-tai).
"""
import base64
import hashlib
import json
import os
import subprocess
import sys
import time

TOI_THIEU = {"pc": 1, "android": 1419, "jar": 1}
GH_WIN = r"C:\Program Files\GitHub CLI\gh.exe"
GH = os.environ.get("GH_EXE") or (GH_WIN if os.path.exists(GH_WIN) else "gh")
REPO = os.environ.get("REPO") or "kpah-server/nqsh-h2-tai"
README = """# NQSH-H2 - bản phát hành

Chỉ chứa bản build để tải (không có mã nguồn). Game PC / Android tự đọc `phienban.json` để cập nhật.

- PC: https://github.com/%(r)s/releases/download/pc/NQSH-H2-PC.zip
- Android: https://github.com/%(r)s/releases/download/android/NQSH-H2.apk
- Jar (J2ME): https://github.com/%(r)s/releases/download/jar/NQSH-H2.jar
""" % {"r": REPO}


def gh_api(path, method="GET", fields=None):
    """Gọi GitHub API qua gh; trả (mã thoát, json hoặc None)."""
    cmd = [GH, "api", "-X", method, "repos/%s/%s" % (REPO, path)]
    for k, v in (fields or {}).items():
        cmd += ["-f", "%s=%s" % (k, v)]
    r = subprocess.run(cmd, capture_output=True)
    try:
        return r.returncode, json.loads(r.stdout.decode("utf-8") or "null")
    except ValueError:
        return r.returncode, None


def doc_tep(ten):
    """(nội dung bytes, sha) của file trên main, hoặc (None, None)."""
    ma, d = gh_api("contents/" + ten)
    if ma != 0 or not isinstance(d, dict) or "content" not in d:
        return None, None
    return base64.b64decode(d["content"]), d["sha"]


def ghi_tep(ten, noi_dung, thong_diep):
    _, sha = doc_tep(ten)
    f = {"message": thong_diep, "content": base64.b64encode(noi_dung).decode("ascii"), "branch": "main"}
    if sha:
        f["sha"] = sha
    ma, d = gh_api("contents/" + ten, "PUT", f)
    if ma != 0:
        raise SystemExit("Khong ghi duoc %s len %s: %s" % (ten, REPO, d))


def doc(p):
    try:
        with open(p, encoding="utf-8") as f:
            d = json.load(f)
        return d if isinstance(d, dict) else {}
    except Exception:
        return {}


lenh = sys.argv[1]
if lenh == "tai-ve":
    p = sys.argv[2]
    if doc_tep("README.md")[0] is None:
        ghi_tep("README.md", README.encode("utf-8"), "Khoi tao repo phat hanh")
    nd, _ = doc_tep("phienban.json")
    with open(p, "wb") as f:
        f.write(nd if nd else b"{}")
elif lenh == "day-len":
    p, thong_diep = sys.argv[2], sys.argv[3]
    with open(p, "rb") as f:
        ghi_tep("phienban.json", f.read(), thong_diep)
    print("da ghi phienban.json len " + REPO)
elif lenh in ("ban-ke-tiep", "ghi"):
    p, loai = sys.argv[2], sys.argv[3]
    assert loai in TOI_THIEU, loai
    d = doc(p)
    if lenh == "ban-ke-tiep":
        print(max(int((d.get(loai) or {}).get("ban", 0)) + 1, TOI_THIEU[loai]))
    else:
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
