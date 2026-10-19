"""Sinh code game cho bản APK Huyền Thoại Bảo Bối (client_apk/pokemon) từ code bản jar J2ME.

    python -I port-j2me.py <client_jar/src> <client_jar/shared/mod> <thư mục ra> <BAN>

Bản jar (client_jar/src + client_jar/shared/mod) là NGUỒN DUY NHẤT của game Pokémon: APK biên dịch lại đúng code đó trên lớp
tương thích MIDP (client_apk/pokemon/src/main/java/javax/microedition/...). Script này chỉ chép và vá vài chỗ
mà Android buộc phải khác:

- Class.getResourceAsStream (J2ME đọc tài nguyên trong jar; trên Android "".getClass() thuộc boot class
  loader nên không thấy APK) -> MIDlet.moTaiNguyen (đọc assets = client_jar/res).
- TField: chạm ô nhập mở bàn phím hệ thống Android (TextBox -> hộp nhập, gõ tiếng Việt) thay bàn phím ảo
  VirTualKeyB của bản jar.
- PhienBan (build.sh của jar sinh): số bản.

Mỗi chỗ vá phải khớp đúng số lần mong đợi, lệch là dừng build (code jar đổi -> sửa lại mẫu ở đây).

Lớp giao diện HD của APK (VA_HD, client_apk/pokemon/src/main/java/com/htbb/pokemon/hd): chèn móc vào đầu hàm
(DAU) hoặc thay chuỗi trong thân một hàm (TRONG) để chữ / khung / HUD vẽ bằng Android. Vá HD là vá MỀM:
code jar đổi làm mẫu không khớp thì chỉ cảnh báo và bỏ qua (APK vẫn chạy, chỗ đó giữ kiểu jar).
Bản jar không bị ảnh hưởng (chỉ code chép sang build/generated của APK được vá).
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


HD = "com.htbb.pokemon.hd."
HUD = HD + "Hud.bat()"

# ("DAU", tệp, regex chữ ký hàm, code chèn ngay sau "{")
# ("TRONG", tệp, regex chữ ký hàm, chuỗi cũ, chuỗi mới)  - thay MỌI lần gặp trong thân hàm đó
def _gd(ten, thamso, goi):
    return ("DAU", "game/pokemon/GiaoDien.java", r"\n    public static void %s\(%s\)" % (ten, thamso),
            " if (%sHdVe.%s(%s)) { return; }" % (HD, ten, goi))

VA_HD = [
    # chữ: font ảnh -> font vector Be Vietnam Pro
    ("DAU", "game/render/screen/Font.java", r"\n    public void drawString\(Graphics g, String st, int x, int y, int align\)",
     " if (%sHdChu.bat) { %sHdChu.ve(this, this.imgFont, this.charHeight, g, st, x, y, align); return; }" % (HD, HD)),
    ("DAU", "game/render/screen/Font.java", r"\n    public int getWidthNguyenVan\(String st\)",
     " if (%sHdChu.bat) { return %sHdChu.rong(this, this.imgFont, this.charHeight, st); }" % (HD, HD)),
    # khung / bảng / thanh HD
    _gd("khoi", r"Graphics g, int x, int y, int w, int h, int r, int mau", "g, x, y, w, h, r, mau"),
    _gd("khoiDoc", r"Graphics g, int x, int y, int w, int h, int r, int tren, int duoi", "g, x, y, w, h, r, tren, duoi"),
    _gd("hop", r"Graphics g, int x, int y, int w, int h", "g, x, y, w, h"),
    _gd("bang", r"Graphics g, int x, int y, int w, int h", "g, x, y, w, h"),
    _gd("theMo", r"Graphics g, int x, int y, int w, int h", "g, x, y, w, h"),
    _gd("bangToi", r"Graphics g, int x, int y, int w, int h", "g, x, y, w, h"),
    _gd("bangMo", r"Graphics g, int x, int y, int w, int h", "g, x, y, w, h"),
    _gd("dai", r"Graphics g, int x, int y, int w, int h, String tieuDe", "g, x, y, w, h, tieuDe"),
    _gd("vienTab", r"Graphics g, int x, int y, int w, int h, boolean doiTab", "g, x, y, w, h, doiTab"),
    _gd("muiTab", r"Graphics g, int x, int yTam, boolean trai", "g, x, yTam, trai"),
    _gd("chon", r"Graphics g, int x, int y, int w, int h", "g, x, y, w, h"),
    _gd("chonSang", r"Graphics g, int x, int y, int w, int h", "g, x, y, w, h"),
    _gd("thanh", r"Graphics g, int x, int y, int w, int h, long gt, long max, int loai", "g, x, y, w, h, gt, max, loai"),
    _gd("pokeBall", r"Graphics g, int cx, int cy, int r, int goc", "g, cx, cy, r, goc"),
    _gd("vien", r"Graphics g, int x, int y, int w, int h, int mau, String chu", "g, x, y, w, h, mau, chu"),
    ("DAU", "game/pokemon/KieuO.java", r"\n    public static void phuCat\(Graphics g, int x, int y, int w, int hDu, int h, int argb\)",
     " if (%sHdVe.phuCat(g, x, y, w, hDu, h, argb)) { return; }" % HD),
    # HUD cảm ứng: tắt các phần HUD jar mà Hud.java vẽ lại (cùng dữ liệu, cùng thao tác)
    ("DAU", "game/render/screen/GameScr.java", r"\n    private void paintMainCharInfo\(Graphics g\)", " if (%s) { return; }" % HUD),
    ("DAU", "game/render/screen/GameScr.java", r"\n    private void paintQuickSlot\(Graphics g\)", " if (%s) { return; }" % HUD),
    ("DAU", "game/render/screen/GameScr.java", r"\n    private void paintSoftIcon\(Graphics g\)", " if (%s) { return; }" % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paint\(Graphics g\)",
     "g.drawImage(Res.imgSoft[", "if (!%s) g.drawImage(Res.imgSoft[" % HUD),
    ("DAU", "game/render/screen/GameScr.java", r"\n    public void setPosMiniMap\(\)",
     " if (%s && Tilemap.imgMap != null) { int[] o = %sHud.oMiniMap(Tilemap.imgMap.getWidth(), Tilemap.imgMap.getHeight());"
     " wMiniMap = o[2]; hMiniMap = o[3]; this.posMiniMap = new Position(o[0], o[1]); return; }" % (HUD, HD)),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "g.drawRect(i, i, wMiniMap - (i * 2), hMiniMap - (i * 2));",
     "if (!%s) { g.drawRect(i, i, wMiniMap - (i * 2), hMiniMap - (i * 2)); } else if (i == 0) { %sHud.nenMiniMap(g, wMiniMap, hMiniMap); }" % (HUD, HD)),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "if (Res.imgPanel != null && this.posMiniMap.y + hMiniMap < Canvas.h - Screen.deltaY - 4) {",
     "if (!%s && Res.imgPanel != null && this.posMiniMap.y + hMiniMap < Canvas.h - Screen.deltaY - 4) {" % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "(Canvas.h - Screen.deltaY) - 7, 1);", "(%s ? this.posMiniMap.y + hMiniMap + 2 : (Canvas.h - Screen.deltaY) - 7), 1);" % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "((Canvas.h - hMiniMap) - Screen.deltaY) - 20", "(%s ? this.posMiniMap.y + hMiniMap + 12 : ((Canvas.h - hMiniMap) - Screen.deltaY) - 20)" % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "((Canvas.h - hMiniMap) - Screen.deltaY) - 28", "(%s ? this.posMiniMap.y + hMiniMap + 4 : ((Canvas.h - hMiniMap) - Screen.deltaY) - 28)" % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "(this.posMiniMap.y - 85)", "(%s ? this.posMiniMap.y + hMiniMap + 46 : this.posMiniMap.y - 85)" % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "this.posMiniMap.y - 31, 1);", "(%s ? this.posMiniMap.y + hMiniMap + 30 : this.posMiniMap.y - 31), 1);" % HUD),
    # đầu thẻ bản đồ nhỏ của HUD hiện tên map / vùng / toạ độ -> tắt chữ cùng loại của jar (khỏi chồng)
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "game.pokemon.GiaoDien.vien(g, xTen, yTen, wTen, 15,", "if (!%s) game.pokemon.GiaoDien.vien(g, xTen, yTen, wTen, 15," % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "Font.smallFont.drawString(g, new StringBuffer(String.valueOf(this.mainChar.x / 16))",
     "if (!%s) Font.smallFont.drawString(g, new StringBuffer(String.valueOf(this.mainChar.x / 16))" % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "Font.normalFontColor.drawString(g, vungMap,", "if (!%s) Font.normalFontColor.drawString(g, vungMap," % HUD),
    # bản đồ nhỏ: thẻ HD của HUD vẽ cả bản đồ (phóng vừa khung) -> jar không vẽ ảnh / điểm trong ô nữa
    ("TRONG", "game/render/screen/GameScr.java", r"\n    public void paintMiniMap\(Graphics g\)",
     "g.setClip(3, 3, wMiniMap - 5, hMiniMap - 5);", "g.setClip(3, 3, %s ? 0 : wMiniMap - 5, %s ? 0 : hMiniMap - 5);" % (HUD, HUD)),
    # 0.9.5: ô Cài đặt của Menu = bảng chỉnh của HUD APK (âm thanh, hiệu ứng, rung, bố cục) - một lối vào
    ("DAU", "game/render/screen/CauGiaoDien.java", r"\n    public static void moCaiDat\(\)", " if (%s) { %sHud.moCaiDat(); return; }" % (HUD, HD)),
    # HUD màn rộng chung (HudRong, bản PC): APK có HUD HD riêng (Hud.java) -> tắt, khỏi vẽ / nhận chạm chồng
    ("DAU", "game/render/screen/HudRong.java", r"\n    public static boolean bat\(\)", " if (%s) { return false; }" % HUD),
    # toạ độ chạm logic -> thế giới (HanhDong: chọn quái / NPC khi chạm): camera cố định phóng 2 (HdZoom)
    ("DAU", "game/pokemon/HanhDong.java", r"\n    public static int wx\(int px\)", " if (true) { return %sHdZoom.wx(GameScr.cmx, px); }" % HD),
    ("DAU", "game/pokemon/HanhDong.java", r"\n    public static int wy\(int py\)", " if (true) { return %sHdZoom.wy(GameScr.cmy, py); }" % HD),
    # thanh ô nhanh của jar ẩn khi có HUD: chạm vùng đó không mở bảng gán chiêu (HUD tự mở qua nút chiêu)
    ("TEP", "game/render/screen/GameScr.java", "game.pokemon.ChonChieuNhanh.thanhJar(",
     "!%s && game.pokemon.ChonChieuNhanh.thanhJar(" % HUD),
    # ô chat: chữ Đóng / Xoá của jar -> nút viên của HUD (Hud.veNutChat)
    ("TEP", "game/render/screen/GameScr.java", 'Font.borderFont.drawString(g, "Đóng", 5, Canvas.h - Screen.deltaY, 0);',
     'if (!%s) Font.borderFont.drawString(g, "Đóng", 5, Canvas.h - Screen.deltaY, 0);' % HUD),
    ("TEP", "game/render/screen/GameScr.java", 'Font.borderFont.drawString(g, "Xóa", Canvas.w - 5, Canvas.h - Screen.deltaY, 1);',
     'if (!%s) Font.borderFont.drawString(g, "Xóa", Canvas.w - 5, Canvas.h - Screen.deltaY, 1);' % HUD),
    # nhãn 12+: trong game có HUD thì vẽ trong bảng Chỉnh nút (không đè thẻ trạng thái)
    ("DAU", "game/render/screen/GameScr.java", r"\n    public static void paint12plus\(Graphics g\)", " if (%s && !HdCau.hien12()) { return; }" % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    private void paintFocusActorInfo\(Graphics g\)",
     "Canvas.resetTrans(g);", "Canvas.resetTrans(g); if (%s) { g.translate(%sHud.dxMucTieu(rongKhungHp()), %sHud.yTren()); }" % (HUD, HD, HD)),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    private void paintChat\(Graphics g\)",
     "if (this.hideGUI == 0) {", "if (this.hideGUI == 0 && !%s) {" % HUD),
    ("DAU", "game/render/screen/GameScr.java", r"\n    private void paintMsgServer\(Graphics g\)",
     " if (%s) { HdCau.veTinMayChu(g, (String) this.listMSGServer.elementAt(0), this.xMsgServer); return; }" % HUD),
    ("DAU", "game/render/screen/GameScr.java", r"\n        public void paint\(Graphics g\)",
     " if (%s) { HdCau.veTicker(g, this.info, this.x, this.y); return; }" % HUD),
    ("TRONG", "game/render/screen/GameScr.java", r"\n    private void updatePoint\(\)",
     "if (Canvas.isPointerClick) {\n                int wSlot = buocO() * 5;",
     "if (Canvas.isPointerClick && !%s) {\n                int wSlot = buocO() * 5;" % HUD),
    # hiệu ứng HD (HdHieuUng): hạt phát sáng, số sát thương lò xo, đêm mượt, nổ lên cấp / tiến hoá
    ("DAU", "game/pokemon/HieuUngDanh.java", r"\n    private static void veHat\(Graphics g\)",
     " if (%sHdHieuUng.veHat(g, hK, hX, hY, hVX, hVY, hTuoi, hMax, hMau, hMau2, hCo, soHat)) { return; }" % HD),
    ("DAU", "game/pokemon/HieuUngDanh.java", r"\n    public static void lenCap\(Char c\)", " %sHdHieuUng.lenCap(c);" % HD),
    ("DAU", "game/pokemon/HieuUngDanh.java", r"\n    public static void tienHoa\(Char c\)", " %sHdHieuUng.tienHoa(c);" % HD),
    ("TRONG", "game/pokemon/SoBay.java", r"\n    public static void ve\(Graphics g\)",
     "veSo(g, chu[i], kieu[i], c, x, y, xu[i]);",
     "if (!%sHdHieuUng.veSo(g, chu[i], MAU[kieu[i]], co[i], x, y, xu[i], t, tuoiMax[i])) { veSo(g, chu[i], kieu[i], c, x, y, xu[i]); }" % HD),
    ("DAU", "game/pokemon/SinhThai.java", r"\n    public static void veDem\(Graphics g\)",
     " if (%s) { %sHdHieuUng.veDem(g, (!demBat() || game.model.Tilemap.isOfflineMap) ? 0 : alphaDem(gio())); return; }" % (HUD, HD)),
    # banner HD trên máy tính bảng (màn cao hơn cảnh 400 px): kéo dài dải trời thay vì nền navy phẳng
    ("TRONG", "game/pokemon/Banner.java", r"\n    private static void veHd\(Graphics g, int chon, boolean veKhoiDau, int w, int h\)",
     "g.fillRect(0, 0, w, oy);", "g.fillRect(0, 0, w, oy); %sHdVe.troiBanner(g, hdNen, ox, oy, w);" % HD),
    # âm thanh HQ (assets/am_hd: OGG 44,1 kHz, SoundPool + nhạc crossfade) thay lớp MMAPI của jar
    ("DAU", "game/model/SoundMm.java", r"\n    void napTruoc\(\)", " if (%sAmThanhHd.napTruoc()) { return; }" % HD),
    ("DAU", "game/model/SoundMm.java", r"\n    void choi\(int id, String ten, int uuTien, int amLuong\)",
     " if (%sAmThanhHd.choi(id, ten, uuTien, amLuong)) { return; }" % HD),
    ("DAU", "game/model/SoundMm.java", r"\n    void choiNhac\(String ten, int amLuong\)", " if (%sAmThanhHd.choiNhac(ten, amLuong)) { return; }" % HD),
    ("DAU", "game/model/SoundMm.java", r"\n    void dungNhac\(\)", " if (%sAmThanhHd.dungNhac()) { return; }" % HD),
    ("DAU", "game/model/SoundMm.java", r"\n    void amLuong\(int a\)", " if (%sAmThanhHd.amLuong(a)) { return; }" % HD),
    # phím mềm / nút bàn phím ảo của màn chơi: HUD có nút riêng
    ("TRONG", "game/model/Main.java", r"\n    public void paint\(Graphics g\)",
     "Canvas.resetTrans(g);", "Canvas.resetTrans(g); if (%sHud.anPhimMem()) { return; }" % HD),
    ("TRONG", "game/model/Main.java", r"\n    public void updateKey\(\)",
     "switch (Canvas.collisionCmdBar()) {", "switch (%sHud.anPhimMem() ? -1 : Canvas.collisionCmdBar()) {" % HD),
    ("TRONG", "game/model/Main.java", r"\n    public void updateKey\(\)",
     "if (ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey) && Canvas.isPointer(",
     "if (!(%sHud.BAT && Canvas.currentScreen == Canvas.gameScr) && ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey) && Canvas.isPointer(" % HD),
    # nút bàn phím ảo của jar: APK dùng bàn phím hệ thống -> không vẽ ở màn chơi
    ("TEP", "game/model/Main.java", "if (GameScr.msgWorld.size() <= 0 && ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey)) {",
     "if (!(%sHud.BAT && Canvas.currentScreen == Canvas.gameScr) && GameScr.msgWorld.size() <= 0 && ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey)) {" % HD),
    ("TRONG", "game/render/screen/Screen.java", r"\n    public void paint\(Graphics g\)",
     "g.drawImage(Res.imgCmdBar, 0, Canvas.h + 3, 36);",
     "if (!%sHud.anPhimMem()) { g.tamTatPhong(true); g.drawImage(Res.imgCmdBar, 0, Canvas.h + 3, 36); g.tamTatPhong(false); }" % HD),
    # chữ phím mềm (GiaoDien.nutMem của jar) -> không vẽ: lệnh hiện thành nút theo ngữ cảnh (HdNut)
    ("DAU", "game/pokemon/GiaoDien.java", r"\n    public static void nutMem\(Graphics g, String \w+, int \w+, boolean \w+\)",
     " if (%s) { return; }" % HUD),
    ("DAU", "game/pokemon/GiaoDien.java", r"\n    public static void nutMem\(Graphics g, String \w+, int \w+, boolean \w+, int \w+\)",
     " if (%s) { return; }" % HUD),
    ("DAU", "game/render/Canvas.java", r"\n    public static int collisionCmdBar\(\)", " if (%s) { return %sHdVe.vungPhimMem(); }" % (HUD, HD)),
    _gd("thanhPhim", r"Graphics g, int w, int h", "g, w, h"),
    # cửa sổ cỡ cố định -> thẻ lớn giữa màn (HdPhong)
    ("TRONG", "game/render/Canvas.java", r"\n    private void ve0\(Graphics g\)",
     "currentScreen.paint(g);", "currentScreen.paint(g);\n                %sHdPhong.ket(g);" % HD),
    ("TRONG", "game/render/screen/WindowInfoScr.java", r"\n    public void paint\(Graphics g\)",
     "Canvas.gameScr.paint(g);", "Canvas.gameScr.paint(g); %sHdPhong.batDau(g, this, this.x - 10, this.y - 10, 145, 144, 2.2f);" % HD),
    ("TRONG", "game/render/Canvas.java", r"\n    private void ve0\(Graphics g\)",
     "dlg.paint(g);", "dlg.paint(g);\n                    %sHdPhong.ket(g);" % HD),   # 0.9.4: Canvas vẽ hộp qua biến dlg (try riêng)
    ("TRONG", "game/render/screen/MsgDlg.java", r"\n    public void paint\(Graphics g\)",
     "game.pokemon.GiaoDien.hop(g, Canvas.hw - (this.wHop / 2), 12, this.wHop, this.hHop);",
     "%sHdPhong.batDau(g, this, Canvas.hw - (this.wHop / 2), 12, this.wHop, this.hHop, 1.7f); game.pokemon.GiaoDien.hop(g, Canvas.hw - (this.wHop / 2), 12, this.wHop, this.hHop);" % HD),
    ("TRONG", "game/render/screen/PauseMenu.java", r"\n    public void paint\(Graphics graphics\)",
     "Canvas.gameScr.paintServerInfo(graphics);",
     "Canvas.gameScr.paintServerInfo(graphics); %sHdPhong.batDau(graphics, this, this.x - 5, this.y - 10, this.w + 10, this.h + 20, 1.8f);" % HD),
    # Poké Ball Menu nhanh mép trái -> nút trên HUD; thanh trạng thái auto dưới thẻ HP của HUD
    ("DAU", "game/render/screen/MenuNhanh.java", r"\n    private static boolean anBong\(\)", " if (%s) { return true; }" % HUD),
    ("DAU", "game/render/screen/Nen.java", r"\n    static int yTrangThai\(int macDinh\)", " if (%s) { return %sHud.yTrangThai(macDinh); }" % (HUD, HD)),
]


# CAMERA CỐ ĐỊNH (HdZoom, KhungCoDinh): vá BẮT BUỘC - lệch là dừng build (máy nào cũng thấy đúng 320x180 px thế giới;
# vá mềm bị bỏ qua sẽ cho camera sai -> không được phép). (tệp, chuỗi gốc, chuỗi thay, số lần phải gặp)
VA_CAMERA = [
    ("game/render/screen/GameScr.java",
     "        gsw = Canvas.w;\n        gsh = Canvas.h;\n",
     "        gsw = %sHdZoom.gsw(Canvas.w);   // apk: camera cố định 320x180 px thế giới\n"
     "        gsh = %sHdZoom.gsh(Canvas.h);\n" % (HD, HD),
     1),
    ("game/render/screen/GameScr.java",
     "g.translate((-cmx) + rung + game.pokemon.HieuUngDanh.rungX(), (-cmy) - rung + game.pokemon.HieuUngDanh.rungY());\n"
     "        g.setColor(0);\n        g.fillRect(cmx - 5, cmy - 5, Canvas.w + 10, Canvas.h + 10);\n",
     "g.translate((-cmx) + rung + game.pokemon.HieuUngDanh.rungX(), (-cmy) - rung + game.pokemon.HieuUngDanh.rungY());\n"
     "        %sHdZoom.batDau(g, this);   // apk: lớp thế giới phóng 2\n"
     "        %sHdZoom.nen(g, cmx - 5, cmy - 5, gsw + 10, gsh + 10);\n" % (HD, HD),
     1),
    ("game/render/screen/GameScr.java",
     "            paintFocusActorInfo(g);\n            paintMainCharInfo(g);\n",
     "            %sHdZoom.ket(g);   // apk: hết lớp thế giới -> HUD\n"
     "            paintFocusActorInfo(g);\n            paintMainCharInfo(g);\n" % HD,
     1),
    ("game/render/screen/GameScr.java", "cmx + Canvas.px", "%sHdZoom.wx(cmx, Canvas.px)" % HD, 3),
    ("game/render/screen/GameScr.java", "cmy + Canvas.py", "%sHdZoom.wy(cmy, Canvas.py)" % HD, 3),
]


def va_camera(tuong_doi, s):
    for tep, a, b, n in VA_CAMERA:
        if tep != tuong_doi:
            continue
        if s.count(a) != n:
            sys.exit("port-j2me: %s: mẫu camera cố định gặp %d lần (cần %d) - code jar đã đổi, sửa VA_CAMERA: %r"
                     % (tuong_doi, s.count(a), n, a[:70]))
        s = s.replace(a, b)
    return s


def _than_ham(s, mau):
    """(vị trí '{' mở thân, vị trí '}' đóng) của hàm khớp regex mau đầu tiên; None nếu không thấy."""
    m = re.search(mau, s)
    if not m:
        return None
    i = s.find("{", m.end())
    if i < 0:
        return None
    sau = 0
    j = i
    n = len(s)
    while j < n:
        c = s[j]
        if c == '"' or c == "'":
            q = c
            j += 1
            while j < n and s[j] != q:
                j += 2 if s[j] == "\\" else 1
        elif s.startswith("//", j):
            j = s.find("\n", j)
            if j < 0:
                return None
        elif s.startswith("/*", j):
            j = s.find("*/", j) + 1
        elif c == "{":
            sau += 1
        elif c == "}":
            sau -= 1
            if sau == 0:
                return i, j
        j += 1
    return None


def va_hd(tuong_doi, s, bao):
    for va in VA_HD:
        if va[1] != tuong_doi:
            continue
        than = None if va[0] == "TEP" else _than_ham(s, va[2])
        if than is None and va[0] != "TEP":
            bao.append("%s: không thấy hàm %s" % (tuong_doi, va[2]))
            continue
        if va[0] == "TEP":
            if va[2] not in s:
                bao.append("%s: không thấy '%s'" % (tuong_doi, va[2][:60]))
                continue
            s = s.replace(va[2], va[3])
            continue
        i, j = than
        if va[0] == "DAU":
            s = s[:i + 1] + va[3] + s[i + 1:]
        else:
            body = s[i:j]
            if va[3] not in body:
                bao.append("%s: không thấy '%s'" % (tuong_doi, va[3][:60]))
                continue
            s = s[:i] + body.replace(va[3], va[4]) + s[j:]
    return s


def main():
    src, mod, out, ban = sys.argv[1:5]
    va_rieng = {f: (a, b, n) for f, a, b, n in VA_RIENG}
    dem = 0
    da_va = set()
    da_sinh = set()
    bao_hd = []
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
                s = va_camera(tuong_doi, s)
                s = va_hd(tuong_doi, s, bao_hd)
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
        "final class PhienBan {\n    static final int BAN = %d;\n}\n"
        % int(ban))
    for b in bao_hd:
        print("port-j2me: CANH BAO vá HD bỏ qua - " + b)
    print("port-j2me: %d file -> %s (vá HD %d/%d)" % (dem + 1, out,
          len(VA_HD) - len(bao_hd), len(VA_HD)))


main()
