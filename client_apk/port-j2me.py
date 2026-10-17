"""Sinh code game cho bản APK Huyền Thoại Bảo Bối (android/pokemon) từ code bản jar J2ME.

    python -I port-j2me.py <j2me/src> <shared/mod> <thư mục ra> <BAN> <XAC_THUC 0|1>

Bản jar (j2me/src + shared/mod) là NGUỒN DUY NHẤT của game Pokémon: APK biên dịch lại đúng code đó trên lớp
tương thích MIDP (android/pokemon/src/main/java/javax/microedition/...). Script này chỉ chép và vá vài chỗ
mà Android buộc phải khác:

- Class.getResourceAsStream (J2ME đọc tài nguyên trong jar; trên Android "".getClass() thuộc boot class
  loader nên không thấy APK) -> MIDlet.moTaiNguyen (đọc assets = j2me/res).
- TField: chạm ô nhập mở bàn phím hệ thống Android (TextBox -> hộp nhập, gõ tiếng Việt) thay bàn phím ảo
  VirTualKeyB của bản jar.
- PhienBan (build.sh của jar sinh): số bản + XAC_THUC (-88).

Mỗi chỗ vá phải khớp đúng số lần mong đợi, lệch là dừng build (code jar đổi -> sửa lại mẫu ở đây).
"""
import os
import re
import sys

VA_CHUNG = [
    # (mẫu regex, thay bằng, ghi chú)
    (r'(?:""\s*\.\s*|new\s+[\w.]+\s*\(\s*\)\s*\.\s*)?getClass\(\)\s*\.\s*getResourceAsStream\(',
     "javax.microedition.midlet.MIDlet.moTaiNguyen(", "tài nguyên jar -> assets"),
    (r'\b[\w.]+\.class\s*\.\s*getResourceAsStream\(',
     "javax.microedition.midlet.MIDlet.moTaiNguyen(", "tài nguyên jar -> assets"),
]

# (tệp, chuỗi gốc, chuỗi thay, số lần phải gặp)
VA_RIENG = [
    ("game/render/screen/TField.java",
     "            if (game.render.Canvas.isPointer(this.x, this.y, this.width, this.height)) {\n",
     "            if (game.render.Canvas.isPointer(this.x, this.y, this.width, this.height)) {\n"
     "                // android: chạm ô nhập = mở bàn phím hệ thống (TextBox -> hộp nhập Android)\n"
     "                this.isFocus = true;\n"
     "                game.render.Canvas.isPointerClick = false;\n"
     "                doChangeToTextBox();\n"
     "                return;\n"
     "            }\n"
     "            if (false) {\n",
     1),
]


def main():
    src, mod, out, ban, xac_thuc = sys.argv[1:6]
    xt = "true" if xac_thuc in ("1", "bat", "true") else "false"
    va_rieng = {f: (a, b, n) for f, a, b, n in VA_RIENG}
    dem = 0
    da_va = set()
    da_sinh = set()
    for goc in (src, mod):
        for thu_muc, _, tep in os.walk(goc):
            for t in tep:
                if not t.endswith(".java"):
                    continue
                duong = os.path.join(thu_muc, t)
                tuong_doi = os.path.relpath(duong, goc).replace(os.sep, "/")
                if tuong_doi.endswith("/PhienBan.java"):
                    continue
                s = open(duong, encoding="utf-8").read()
                for mau, thay, _ in VA_CHUNG:
                    s = re.sub(mau, thay, s)
                if tuong_doi in va_rieng:
                    a, b, n = va_rieng[tuong_doi]
                    if s.count(a) != n:
                        sys.exit("port-j2me: %s: mẫu vá gặp %d lần (cần %d) - code jar đã đổi, sửa VA_RIENG"
                                 % (tuong_doi, s.count(a), n))
                    s = s.replace(a, b)
                    da_va.add(tuong_doi)
                ra = os.path.join(out, tuong_doi)
                da_sinh.add(os.path.normpath(ra))
                os.makedirs(os.path.dirname(ra), exist_ok=True)
                cu = None
                if os.path.exists(ra):
                    cu = open(ra, encoding="utf-8").read()
                if cu != s:
                    open(ra, "w", encoding="utf-8").write(s)
                dem += 1
    thieu = set(va_rieng) - da_va
    if thieu:
        sys.exit("port-j2me: không thấy tệp cần vá: %s" % ", ".join(sorted(thieu)))
    pb = os.path.join(out, "game/render/screen/PhienBan.java")
    da_sinh.add(os.path.normpath(pb))
    for thu_muc, _, tep in os.walk(out):          # xoá tệp cũ không còn trong bản jar
        for t in tep:
            d = os.path.normpath(os.path.join(thu_muc, t))
            if d not in da_sinh:
                os.remove(d)
    os.makedirs(os.path.dirname(pb), exist_ok=True)
    open(pb, "w", encoding="utf-8").write(
        "package game.render.screen;\n\n/** Sinh bởi port-j2me.py (Gradle) - KHÔNG sửa tay. */\n"
        "final class PhienBan {\n    static final int BAN = %d;\n    static final boolean XAC_THUC = %s;\n}\n"
        % (int(ban), xt))
    print("port-j2me: %d file -> %s (XAC_THUC=%s)" % (dem + 1, out, xt))


main()
