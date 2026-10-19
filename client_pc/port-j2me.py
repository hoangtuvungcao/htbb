"""Sinh code game cho bản PC Java (pc-java) Huyền Thoại Bảo Bối từ code bản jar J2ME.

    python3 -I port-j2me.py <client_jar/src> <client_jar/shared/mod> <thư mục ra> <BAN>

Giống client_apk/port-j2me.py: bản jar (client_jar/src + client_jar/shared/mod) là NGUỒN DUY NHẤT của game; bản PC biên dịch lại
đúng code đó trên lớp tương thích MIDP cho Java SE (client_pc/src/javax/microedition/...) và chỉ vá vài chỗ
mà máy tính buộc phải khác:

- Class.getResourceAsStream ("".getClass() thuộc boot class loader, không thấy jar game) -> MIDlet.moTaiNguyen.
- TField: bấm chuột vào ô nhập = đặt con trỏ gõ (bàn phím thật, gõ tiếng Việt qua bộ gõ hệ điều hành), không mở
  bàn phím ảo VirTualKeyB của bản jar; ô đang gõ báo cho lớp nền (phím chữ vào ô nhập thay vì điều khiển).
- Font (font ảnh của game): vẽ + đo chữ qua htbb.pc.ChuHD = font thật (Noto Sans, đủ tiếng Việt) nét HD ở độ
  phân giải thật của cửa sổ; tắt bằng --chu-anh (giữ font ảnh gốc).
- PhienBan (build.sh của jar sinh): số bản.

Mỗi chỗ vá phải khớp đúng số lần mong đợi, lệch là DỪNG build (code jar đổi -> sửa lại mẫu ở đây).
"""
import os
import re
import sys

VA_CHUNG = [
    (r'(?:""\s*\.\s*|new\s+[\w.]+\s*\(\s*\)\s*\.\s*)?getClass\(\)\s*\.\s*getResourceAsStream\(',
     "javax.microedition.midlet.MIDlet.moTaiNguyen(", "tài nguyên jar -> classpath"),
    (r'\b[\w.]+\.class\s*\.\s*getResourceAsStream\(',
     "javax.microedition.midlet.MIDlet.moTaiNguyen(", "tài nguyên jar -> classpath"),
]

# (tệp, chuỗi gốc, chuỗi thay, số lần phải gặp)
VA_RIENG = [
    # bấm chuột vào ô nhập: chỉ đặt con trỏ gõ (không bàn phím ảo)
    ("game/render/screen/TField.java",
     "            if (game.render.Canvas.isPointer(this.x, this.y, this.width, this.height)) {\n",
     "            if (game.render.Canvas.isPointer(this.x, this.y, this.width, this.height)) {\n"
     "                // pc: bấm ô nhập = gõ bằng bàn phím thật (không mở bàn phím ảo)\n"
     "                this.isFocus = true;\n"
     "                game.render.Canvas.isPointerClick = false;\n"
     "                return;\n"
     "            }\n"
     "            if (false) {\n",
     1),
    # ô đang gõ được vẽ -> lớp nền biết phím chữ là chữ (không phải WASD / phím tắt)
    ("game/render/screen/TField.java",
     "        boolean isFocus = isFocused();\n",
     "        boolean isFocus = isFocused();\n"
     "        if (isFocus) {\n"
     "            htbb.pc.BanPhim.coONhap(this.inputType == 1);   // pc: đang gõ chữ\n"
     "        }\n",
     1),
    # màn đăng nhập: bấm chuột chọn ô nhập -> đồng bộ chỉ số focus (Tab / Enter đúng ô, Enter = Đăng nhập)
    ("game/render/screen/LoginScr.java",
     "        boolean is = false;\n",
     "        boolean is = false;\n"
     "        if (this.tfUser.isFocus && this.focus != 0 || this.tfPass.isFocus && this.focus != 1) {\n"
     "            this.focus = this.tfUser.isFocus ? 0 : 1;   // pc: ô chọn bằng chuột\n"
     "            this.center = this.cmdLogin;\n"
     "        }\n",
     1),
    # hiệu ứng HD (htbb.pc.HieuUngHD): camera thế giới, va chạm theo hệ, lên cấp / tiến hoá, gục, thời tiết
    ("game/pokemon/HieuUngDanh.java",
     "    public static void ve(Graphics g) {\n",
     "    public static void ve(Graphics g) {\n        htbb.pc.HieuUngHD.camera(g);   // pc\n",
     1),
    ("game/pokemon/HieuUngDanh.java",
     "    public static void noHe(int he, int x, int y, int manh) {\n",
     "    public static void noHe(int he, int x, int y, int manh) {\n"
     "        htbb.pc.HieuUngHD.vaCham(he, x, y, manh, KieuO.mauHe(he));   // pc\n",
     1),
    ("game/pokemon/HieuUngDanh.java",
     "            themNo(c, false);\n",
     "            themNo(c, false);\n            htbb.pc.HieuUngHD.bung(c.x, c.y, false);   // pc\n",
     1),
    ("game/pokemon/HieuUngDanh.java",
     "            themNo(c, true);\n",
     "            themNo(c, true);\n            htbb.pc.HieuUngHD.bung(c.x, c.y, true);   // pc\n",
     1),
    ("game/pokemon/HieuUngDanh.java",
     "            int t = m.monster_type;\n",
     "            int t = m.monster_type;\n            htbb.pc.HieuUngHD.tan(m.x, m.y);   // pc\n",
     1),
    ("game/pokemon/SinhThai.java",
     "        Canvas.resetTrans(g);\n        int a = 0x50 * doDam >> 8;\n",
     "        Canvas.resetTrans(g);\n        if (htbb.pc.HieuUngHD.suongMu(g, doDam)) {\n            return;   // pc: sương mù HD\n        }\n"
     "        int a = 0x50 * doDam >> 8;\n",
     1),
    ("game/pokemon/SinhThai.java",
     "        Canvas.resetTrans(g);\n        int t = Canvas.gameTick;\n        int nhip",
     "        Canvas.resetTrans(g);\n        if (htbb.pc.HieuUngHD.nangGat(g, doDam)) {\n            return;   // pc: nắng gắt HD\n        }\n"
     "        int t = Canvas.gameTick;\n        int nhip",
     1),
    # thu phóng thế giới riêng với giao diện (htbb.pc.Zoom): vùng nhìn camera, lớp thế giới, toạ độ chạm, nền lề
    ("game/render/screen/GameScr.java",
     "        gsw = Canvas.w;\n        gsh = Canvas.h;\n",
     "        gsw = htbb.pc.Zoom.gsw(Canvas.w);   // pc: vùng nhìn thế giới (phóng riêng)\n        gsh = htbb.pc.Zoom.gsh(Canvas.h);\n",
     1),
    ("game/render/screen/GameScr.java",
     "        g.translate((-cmx) + rung + game.pokemon.HieuUngDanh.rungX(), (-cmy) - rung + game.pokemon.HieuUngDanh.rungY());\n"
     "        g.setColor(0);\n        g.fillRect(cmx - 5, cmy - 5, Canvas.w + 10, Canvas.h + 10);\n",
     "        htbb.pc.Zoom.vaoTheGioi(g);   // pc: lớp thế giới phóng riêng\n"
     "        g.translate((-cmx) + rung + game.pokemon.HieuUngDanh.rungX(), (-cmy) - rung + game.pokemon.HieuUngDanh.rungY());\n"
     "        htbb.pc.Zoom.nen(g, cmx - 5, cmy - 5, gsw + 10, gsh + 10);\n",
     1),
    ("game/render/screen/GameScr.java",
     "        game.pokemon.SinhThai.veDem(g);\n",
     "        game.pokemon.SinhThai.veDem(g);\n        htbb.pc.Zoom.raTheGioi(g);   // pc: hết lớp thế giới -> HUD\n",
     1),
    ("game/render/screen/GameScr.java", "cmx + Canvas.px", "cmx + htbb.pc.Zoom.theGioiX(Canvas.px)", 3),
    ("game/render/screen/GameScr.java", "cmy + Canvas.py", "cmy + htbb.pc.Zoom.theGioiY(Canvas.py)", 3),
    # hạt / cánh hoa của mod (HieuUng, vẽ toạ độ màn) chỉ trong vùng bản đồ khi bản đồ nhỏ hơn màn
    ("game/render/screen/HieuUng.java",
     "        int w = Canvas.w;\n        int h = Canvas.h;\n        graphics.setClip(0, 0, w, h);\n",
     "        int w = Canvas.w;\n        int h = Canvas.h;\n        graphics.setClip(0, 0, w, h);\n"
     "        htbb.pc.Zoom.catVungTheGioi(graphics);   // pc\n",
     1),
    # thanh phím mềm J2ME -> nút trong ngữ cảnh (htbb.pc.ThanhLenh vẽ ở lớp phủ HD)
    ("game/model/Main.java",
     "        if (this.left != null) {\n",
     "        if (this.left != null && !htbb.pc.ThanhLenh.bat) {\n",
     1),
    ("game/model/Main.java",
     "        if (this.center != null) {\n",
     "        if (this.center != null && !htbb.pc.ThanhLenh.bat) {\n",
     1),
    ("game/model/Main.java",
     "        if (this.right != null) {\n",
     "        if (this.right != null && !htbb.pc.ThanhLenh.bat) {\n",
     1),
    # nút bàn phím ảo: máy tính có bàn phím thật
    ("game/model/Main.java",
     "        if (GameScr.msgWorld.size() <= 0 && ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey)) {",
     "        if (!htbb.pc.ThanhLenh.bat && GameScr.msgWorld.size() <= 0 && ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey)) {",
     1),
    ("game/model/Main.java",
     "            if (ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey) && Canvas.isPointer(",
     "            if (!htbb.pc.ThanhLenh.bat && ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey) && Canvas.isPointer(",
     1),
    ("game/render/screen/Screen.java",
     "        g.drawImage(Res.imgCmdBar, 0, Canvas.h + 3, 36);\n",
     "        if (!htbb.pc.ThanhLenh.bat) {\n            g.drawImage(Res.imgCmdBar, 0, Canvas.h + 3, 36);\n        }\n",
     1),
    ("game/render/Canvas.java",
     "    public static int collisionCmdBar() {\n",
     "    public static int collisionCmdBar() {\n        if (htbb.pc.ThanhLenh.bat) {\n            return -1;   // pc: nút trong ngữ cảnh\n        }\n",
     1),
    ("game/render/screen/GameScr.java",
     "    private void paintSoftIcon(Graphics g) {\n",
     "    private void paintSoftIcon(Graphics g) {\n        if (htbb.pc.ThanhLenh.bat) {\n            return;\n        }\n",
     1),
    # thẻ trạng thái HD góc trái trên (htbb.pc.TheTrangThai, cùng bố cục bản APK) + huy hiệu 12+ dưới thẻ
    ("game/render/screen/GameScr.java",
     "    private void paintMainCharInfo(Graphics g) {\n        if (this.hideGUI == 2) {\n            return;\n        }\n",
     "    private void paintMainCharInfo(Graphics g) {\n        if (this.hideGUI == 2) {\n            return;\n        }\n"
     "        if (htbb.pc.TheTrangThai.ve(g, this.mainChar)) {\n            return;   // pc: thẻ HD\n        }\n",
     1),
    ("game/render/screen/GameScr.java",
     "                dy = 40;\n",
     "                dy = htbb.pc.TheTrangThai.y12();   // pc: dưới thẻ trạng thái\n",
     1),
    # chế độ chat: chữ phím mềm "Đóng"/"Xóa" ở đáy -> nút của ThanhLenh trên ô chat
    ("game/render/screen/GameScr.java",
     "        Font.borderFont.drawString(g, \"Đóng\", 5, Canvas.h - Screen.deltaY, 0);\n        Font.borderFont.drawString(g, \"Xóa\", Canvas.w - 5, Canvas.h - Screen.deltaY, 1);\n",
     "        if (!htbb.pc.ThanhLenh.bat) {\n"
     "            Font.borderFont.drawString(g, \"Đóng\", 5, Canvas.h - Screen.deltaY, 0);\n"
     "            Font.borderFont.drawString(g, \"Xóa\", Canvas.w - 5, Canvas.h - Screen.deltaY, 1);\n        }\n",
     1),
    # HUD màn rộng (game.render.screen.HudRong / game.pokemon.HanhDong): toạ độ chạm -> thế giới theo thu phóng riêng,
    # nút cài đặt trên HUD = bảng Cài đặt của cửa sổ (LopPhu) thay Menu nhanh của jar
    ("game/pokemon/HanhDong.java", "return GameScr.cmx + px;", "return GameScr.cmx + htbb.pc.Zoom.theGioiX(px);   // pc", 1),
    ("game/pokemon/HanhDong.java", "return GameScr.cmy + py;", "return GameScr.cmy + htbb.pc.Zoom.theGioiY(py);   // pc", 1),
    ("game/render/screen/HudRong.java", "MenuNhanh.moBang();   // jar: Menu nhanh", "htbb.pc.LopPhu.moCaiDat();   // pc: bảng Cài đặt", 1),
    # font ảnh -> font thật HD
    ("game/render/screen/Font.java",
     "        st = ChuPokemon.D(st);\n        int x1;\n",
     "        st = ChuPokemon.D(st);\n"
     "        if (htbb.pc.ChuHD.bat && htbb.pc.ChuHD.ve(this, this.imgFont, this.charHeight, g, st, x, y, align)) {\n"
     "            return;   // pc: chữ HD\n"
     "        }\n"
     "        int x1;\n",
     1),
    ("game/render/screen/Font.java",
     "    public int getWidthNguyenVan(String st) {\n        if (st == null) {\n            return 0;\n        }\n",
     "    public int getWidthNguyenVan(String st) {\n        if (st == null) {\n            return 0;\n        }\n"
     "        if (htbb.pc.ChuHD.bat) {\n"
     "            int hd = htbb.pc.ChuHD.rong(this, this.imgFont, this.charHeight, st);\n"
     "            if (hd >= 0) {\n"
     "                return hd;   // pc: bề rộng theo chữ HD\n"
     "            }\n"
     "        }\n",
     1),
]


# Móc ĐẦU HÀM: (tệp, tên hàm, tham số (chuỗi chữ ký), lời gọi) -> chèn
#     if (htbb.pc.GiaoDienHD.bat && htbb.pc.GiaoDienHD.<ten>(<đối số>)) { return; }
# ngay sau "{" của hàm - cùng hệ thiết kế với bản APK (client_apk/.../hd/HdVe.java móc đúng các hàm này).
GD = "game/pokemon/GiaoDien.java"
VA_DAU = [
    (GD, "khoi", "Graphics g, int x, int y, int w, int h, int r, int mau"),
    (GD, "khoiDoc", "Graphics g, int x, int y, int w, int h, int r, int tren, int duoi"),
    (GD, "hop", "Graphics g, int x, int y, int w, int h"),
    (GD, "bang", "Graphics g, int x, int y, int w, int h"),
    (GD, "theMo", "Graphics g, int x, int y, int w, int h"),
    (GD, "bangMo", "Graphics g, int x, int y, int w, int h"),
    (GD, "dai", "Graphics g, int x, int y, int w, int h, String tieuDe"),
    (GD, "vienTab", "Graphics g, int x, int y, int w, int h, boolean doiTab"),
    (GD, "muiTab", "Graphics g, int x, int yTam, boolean trai"),
    (GD, "chon", "Graphics g, int x, int y, int w, int h"),
    (GD, "chonSang", "Graphics g, int x, int y, int w, int h"),
    (GD, "thanh", "Graphics g, int x, int y, int w, int h, long gt, long max, int loai"),
    (GD, "pokeBall", "Graphics g, int cx, int cy, int r, int goc"),
    (GD, "vien", "Graphics g, int x, int y, int w, int h, int mau, String chu"),
    ("game/pokemon/KieuO.java", "phuCat", "Graphics g, int x, int y, int w, int hDu, int h, int argb"),
]


def ap_dau(tuong_doi, s):
    for tep, ten, ts in VA_DAU:
        if tep != tuong_doi:
            continue
        mau = "    public static void %s(%s) {\n" % (ten, ts)
        if s.count(mau) != 1:
            sys.exit("port-j2me (pc): %s: không thấy đúng 1 hàm %s(%s) - code jar đã đổi, sửa VA_DAU" % (tep, ten, ts))
        doi = ", ".join(x.split()[-1] for x in ts.split(","))
        s = s.replace(mau, mau + "        if (htbb.pc.GiaoDienHD.bat && htbb.pc.GiaoDienHD.%s(%s)) {\n"
                      "            return;   // pc: HD\n        }\n" % (ten, doi))
    return s


def main():
    src, mod, out, ban = sys.argv[1:5]
    va_rieng = {}
    for f, a, b, n in VA_RIENG:
        va_rieng.setdefault(f, []).append((a, b, n))
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
                s = ap_dau(tuong_doi, s)
                for a, b, n in va_rieng.get(tuong_doi, []):
                    if s.count(a) != n:
                        sys.exit("port-j2me (pc): %s: mẫu vá gặp %d lần (cần %d) - code jar đã đổi, sửa VA_RIENG\n  mẫu: %r"
                                 % (tuong_doi, s.count(a), n, a[:120]))
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
        sys.exit("port-j2me (pc): không thấy tệp cần vá: %s" % ", ".join(sorted(thieu)))
    pb = os.path.join(out, "game/render/screen/PhienBan.java")
    da_sinh.add(os.path.normpath(pb))
    for thu_muc, _, tep in os.walk(out):          # xoá tệp cũ không còn trong bản jar
        for t in tep:
            d = os.path.normpath(os.path.join(thu_muc, t))
            if d not in da_sinh:
                os.remove(d)
    os.makedirs(os.path.dirname(pb), exist_ok=True)
    open(pb, "w", encoding="utf-8").write(
        "package game.render.screen;\n\n/** Sinh bởi client_pc/port-j2me.py - KHÔNG sửa tay. */\n"
        "final class PhienBan {\n    static final int BAN = %d;\n}\n"
        % int(ban))
    print("port-j2me (pc): %d file -> %s" % (dem + 1, out))


main()
