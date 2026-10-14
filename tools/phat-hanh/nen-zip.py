"""Nén thư mục bản PC thành zip (dấu "/" chuẩn - Compress-Archive của PowerShell 5 ghi "\\").

    python -I nen-zip.py <thư mục> <file.zip>

Bỏ thư mục *_BurstDebugInformation_DoNotShip của Unity (không phát kèm).
"""
import os
import sys
import zipfile

src, dst = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as z:
    for goc, thu_muc, tep in os.walk(src):
        thu_muc[:] = [d for d in thu_muc if not d.endswith("_DoNotShip")]
        for f in tep:
            p = os.path.join(goc, f)
            z.write(p, os.path.relpath(p, src).replace(os.sep, "/"))
print("OK " + dst)
