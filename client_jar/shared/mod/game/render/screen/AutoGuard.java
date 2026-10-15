package game.render.screen;

import game.render.Canvas;

import game.networklogic.GameService;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public final class AutoGuard {
    public static boolean on;
    public static int pendingSell;
    public static boolean eatXinCho;
    /** Bảng màu nền thanh trạng thái auto. */
    static final int[] MAU_NEN = {0x69436c, 0x45957f, 0xd11674, 0xb24394, 0xf69a31, 0x8c9b09, 0xbcdeeb, 0x8f48ec};

    public static boolean autoHideUi() {
        return on;
    }

    public static boolean autoEatDlg(String string) {
        try {
            if (AutoVanTieu.thongBao(string) || AutoVanMay.thongBao(string)) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // không được chặn hộp thoại của game
        }
        try {
            if (TienIch.anThongBao(string)) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (eatXinCho) {
            eatXinCho = false;
            if (string != null && string.startsWith("Xin ch")) {
                return true;
            }
        }
        if (pendingSell > 0 && string != null && string.startsWith("\u0110\u00e3 b\u00e1n")) {
            --pendingSell;
            return true;
        }
        if (!on) {
            return false;
        }
        return PauseMenu.autoEatDlg(string);
    }

    public static void startAtDapDo(Menu menu, Vector vector, int n) {
        try {
            PauseMenu.addLuyenCommands(vector);
        }
        catch (Throwable throwable) {
            // never block the original menu
        }
        menu.startAt(vector, n);
    }

    public static void themDong(GameService gameService, short s, int n, int n2, int n3, int n4, int n5) {
        try {
            if (PauseMenu.showThemDongMenu(s, n, n2, n3, n4, n5)) {
                eatXinCho = true;
                return;
            }
        }
        catch (Throwable throwable) {
            // fall back to the game's own single roll
        }
        gameService.doRequestThemThuocTinh(s, n, n2, n3, n4, n5);
    }

    private static boolean dapThat;

    /**
     * Chèn ở ĐẦU ScreenDapDo$7.perform() (nút "Đập"). Màn Cộng thuộc tính (type 0) / Khóa đồ thú (type 2) đã đặt đủ đồ
     * + đá: chỉ mở menu rồi trả true để bỏ toàn bộ code gốc (gửi lệnh, animation, đổi nút
     * "Tiếp tục") - chưa có thao tác nào cho tới khi chọn một mục trong menu.
     * Thiếu đồ/đá thì trả false để code gốc tự báo lỗi như cũ.
     */
    public static boolean dapGuard() {
        if (dapThat) {
            return false;
        }
        try {
            ScreenDapDo screenDapDo = ScreenDapDo.gI();
            if (screenDapDo.type != 0 && screenDapDo.type != 2 || ScreenDapDo.vnguyenLieu.size() <= 0 || ScreenDapDo.itemSelect == null || ScreenDapDo.selectGem == null) {
                return false;
            }
            if (screenDapDo.type == 2) {
                return PauseMenu.showKhoaThuMenu(ScreenDapDo.itemSelect.itemID, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0);
            }
            return PauseMenu.showCongTTMenu(ScreenDapDo.itemSelect.itemID, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0);
        }
        catch (Throwable throwable) {
            // never block the original button
            return false;
        }
    }

    /** Chèn ở ĐẦU GameScr.onMenuOption: true = menu NPC đã được auto dùng, không hiện ra. */
    public static boolean menuGuard(int n, byte by, String[] stringArray) {
        try {
            if (AutoVanMay.anMenu(n)) {
                return true;
            }
            return PauseMenu.petMenuGuard(n, by, stringArray);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /** Tài khoản của lần đăng nhập gần nhất - để auto nuôi pet tự đăng nhập lại (chỉ giữ trong bộ nhớ). */
    public static String tkDangNhap;
    public static String mkDangNhap;

    /** Chèn ở ĐẦU GameService.login: ghi nhớ tài khoản; chặn đăng nhập tới khi server chứng minh là server thật (KiemServer). */
    public static boolean loginGuard(String string, String string2, String string3) {
        if (string != null && string.length() > 0 && string2 != null && string2.length() > 0) {
            tkDangNhap = string;
            mkDangNhap = string2;
        }
        try {
            if (KiemServer.chanDangNhap(string, string2, string3)) {
                return true;
            }
        }
        catch (Throwable throwable) {
            return true;
        }
        // Tự gửi gói đăng nhập, y hệt GameService.login nhưng thêm MÃ MÁY ở trường cuối
        // (server đọc ở LoginHandler -> CamTheoMay). Gửi được thì bỏ qua bản gốc.
        return AutoGuard.guiDangNhap(string, string2, string3);
    }

    /** Mã máy: chuỗi ngẫu nhiên sinh một lần, lưu RMS "kpahmay" (chỉ chữ/số, server lọc như vậy). */
    static String maMay() {
        String string = null;
        try {
            string = RMS.loadRMSString("kpahmay");
        }
        catch (Throwable throwable) {
            // chưa có
        }
        if (string != null && string.length() >= 16) {
            return string;
        }
        java.util.Random random = new java.util.Random(System.currentTimeMillis() ^ (long)Runtime.getRuntime().freeMemory() << 21 ^ (long)System.identityHashCode(new Object()));
        StringBuffer stringBuffer = new StringBuffer("j2me-");
        String string2 = "0123456789abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 24; ++i) {
            stringBuffer.append(string2.charAt((random.nextInt() >>> 1) % string2.length()));
        }
        string = stringBuffer.toString();
        try {
            RMS.saveRMSString("kpahmay", string);
        }
        catch (Throwable throwable) {
            // không lưu được thì lần sau sinh mã khác - vẫn đăng nhập bình thường
        }
        return string;
    }

    static boolean guiDangNhap(String string, String string2, String string3) {
        try {
            network.Message message = new network.Message((byte)1);
            message.writer().writeUTF(string);
            message.writer().writeUTF(string2);
            message.writer().writeUTF(string3);
            message.writer().writeUTF(System.getProperty("microedition.platform"));
            message.writer().writeUTF(game.GameMidlet.client_Provider);
            message.writer().writeUTF(game.GameMidlet.provider);
            message.writer().writeUTF(game.GameMidlet.agent);
            message.writer().writeByte(0);
            message.writer().writeShort(game.render.Canvas.w);
            message.writer().writeByte(game.render.Canvas.isLoadTileServer ? -1 : 0);
            message.writer().writeByte(GameService.gI().getInDexServer(ServerListScr.index));
            message.writer().writeUTF(AutoGuard.maMay());
            // số bản jar (build.sh sinh PhienBan.java): server (real.BanMoiNhat) báo khi có bản mới hơn
            message.writer().writeInt(PhienBan.BAN);
            GameService.gI().session.sendMessage(message);
            message.cleanup();
            return true;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /** Chèn ở ĐẦU MessageHandler.onMessage: gói -88 (server ký xác thực) xử lý ở đây. */
    public static boolean goiGuard(network.Message message) {
        try {
            if (AutoVanMay.anGoi(message.command)) {
                return true;
            }
            return KiemServer.nhan(message);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /** Chèn ở ĐẦU GameScr.onActorMove: chỉ ghi nhận vị trí server gửi cho chính mình, không chặn. */
    public static boolean viTriGuard(byte by, short s, short s2, short s3, short s4) {
        try {
            PauseMenu.viTriServer(by, s2, s3, s4);
        }
        catch (Throwable throwable) {
            // không bao giờ cản lệnh di chuyển của game
        }
        try {
            AutoGuard.boQuaiDangChet(by, s2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    /**
     * Chèn ở ĐẦU GameService.requestMonsterInfo: true = không gửi lệnh 7. Lúc auto vận tiêu nháy map
     * (nạp lại map mỗi vài ô) client xin lại thông tin MỌI con quái -> vượt chống flood của server
     * (300 gói / 5s), server bỏ cả lệnh nháy -> xe tiêu bị bỏ lại. Chỉ xin cho xe tiêu (idChar >= 0).
     */
    /** Chèn ở đầu xử lý phím của Canvas: phím tắt PK (AutoPk.phim). true = đã dùng phím này. */
    public static boolean phimPk(int n) {
        try {
            return AutoPk.phim(n);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public static boolean chanXinQuai(short s) {
        try {
            if (!AutoVanTieu.dang || game.render.Canvas.gameScr == null) {
                return false;
            }
            Vector vector = game.render.Canvas.gameScr.actors;
            for (int i = vector.size() - 1; i >= 0; --i) {
                game.model.Actor actor = (game.model.Actor)vector.elementAt(i);
                if (actor == null || actor.catagory != 1 || actor.ID != s) continue;
                return !(actor instanceof game.model.Monster) || ((game.model.Monster)actor).idChar < 0;
            }
        }
        catch (Throwable throwable) {
            // không bao giờ cản lệnh của game
        }
        return false;
    }

    /**
     * Lệnh 4 cho quái (cat 1) mà actor cùng id đang chạy hiệu ứng chết (lệnh 90, die = true): bỏ
     * actor cũ để onActorMove tạo lại actor mới (đúng loại + mã chủ tiêu). Đổi tiêu là vậy: server
     * gửi 90 rồi 4 cùng id với loại xe mới; giữ actor cũ thì nó chỉ đổi vị trí rồi bị xoá ở khung
     * hình sau -> xe tiêu biến mất tới khi nó đi lại.
     */
    private static void boQuaiDangChet(byte by, short s) {
        if (by != 1 || game.render.Canvas.gameScr == null) {
            return;
        }
        Vector vector = game.render.Canvas.gameScr.actors;
        for (int i = vector.size() - 1; i >= 0; --i) {
            game.model.Actor actor = (game.model.Actor)vector.elementAt(i);
            if (actor == null || actor.catagory != 1 || actor.ID != s || !actor.die) continue;
            vector.removeElementAt(i);
        }
    }

    /**
     * Chèn ở ĐẦU GameScr.onMap. onMap luôn xóa hết actor (cả NPC tĩnh của map) nhưng chỉ nạp lại
     * map - và NPC - khi id map KHÁC map đang đứng. Server gửi lại cùng id (đăng nhập lại đúng map
     * cũ, đổi khu, hoa tiêu trong cùng map) thì NPC mất hẳn. Đặt map = -1 để luôn nạp lại; nạp xong
     * client gửi changeMapOK(-500) và server gửi lại quái gần.
     */
    public static boolean mapGuard(short s) {
        try {
            GameScr gameScr = game.render.Canvas.gameScr;
            if (gameScr != null && gameScr.map == s) {
                gameScr.map = (short)-1;
            }
        }
        catch (Throwable throwable) {
            // không bao giờ cản lệnh vào map
        }
        return false;
    }

    /** Chèn ở ĐẦU ServerListScr.switchToMe: lần đầu mở màn chọn server thì tải danh sách từ GitHub. */
    public static boolean dsGuard() {
        try {
            TaiDanhSach.taiNgam();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    /** Chèn ở ĐẦU ServerListScr.doUpdateServer: nút "Cập nhật" tải danh sách từ GitHub. */
    public static boolean capNhatGuard() {
        try {
            TaiDanhSach.capNhat();
            return true;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /** Chèn ở ĐẦU GameScr.onAdd2Party: auto up tự vào nhóm / bỏ qua lời mời người lạ. */
    public static boolean nhomGuard(network.Message message) {
        try {
            return AutoUp.moiNhom(message);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /** Chèn ở ĐẦU GameScr.showWindow: true = không mở cửa sổ (auto nuôi pet chế thẳng). */
    public static boolean cuaSoGuard(int n, boolean bl, byte[] byArray) {
        try {
            return PauseMenu.anCuaSoCheDo(n, bl, byArray);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /** Mục "Đập 1 lần": chạy đúng nút "Đập" gốc (gửi lệnh + animation) một lần. */
    public static void dapMotLan() {
        dapThat = true;
        try {
            ScreenDapDo.gI().cmdEp.action.perform();
        }
        finally {
            dapThat = false;
        }
    }

    // ---------------------------------------------------------------- nút UP nổi trên màn game
    // Bật/tắt Auto up không cần mở menu. Chỉ có ở bản cảm ứng (Android / PC): Nen.coNutUp().

    /** cỡ nút theo nền tảng: Nen.CO_NUT (jar trên giả lập nhỏ hơn) */
    static final int NUT_W = Nen.CO_NUT;
    static final int NUT_H = Nen.CO_NUT;
    static final int CACH = NUT_H / 6;

    static int nutX() {
        return Canvas.w - NUT_W - 6;
    }

    static int nutY() {
        return Canvas.h / 3;
    }

    /** Nút TIÊU (bật/tắt vòng vận tiêu) nằm ngay dưới nút UP. */
    static int nutY2() {
        return nutY() + NUT_H + CACH;
    }

    /** Có nút và điểm (x, y) nằm trong nút. */
    static boolean trongNutUp(int x, int y) {
        return Nen.coNutUp() && Canvas.currentScreen == Canvas.gameScr
                && x >= nutX() && x < nutX() + NUT_W && y >= nutY() && y < nutY() + NUT_H;
    }

    /** Nút PK (bật/tắt Đập người) dưới nút TIÊU. */
    static int nutY3() {
        return nutY2() + NUT_H + CACH;
    }

    static boolean trongNutPk(int x, int y) {
        return Nen.coNutUp() && Canvas.currentScreen == Canvas.gameScr
                && x >= nutX() && x < nutX() + NUT_W && y >= nutY3() && y < nutY3() + NUT_H;
    }

    /** Cột nút thứ hai (MÃ PK / NHÁY / ĐS / DỪNG) bên trái cột UP / TIÊU / PK. */
    static int nutX2() {
        return nutX() - NUT_W - CACH;
    }

    static int nutYPhim(int n) {
        return nutY() + n * (NUT_H + CACH);
    }

    /** Nút phím PK thứ n (0..3) có điểm (x, y) - chỉ khi đang hiện. */
    static boolean trongNutPhim(int n, int x, int y) {
        return Nen.coNutUp() && AutoPk.hienNut && Canvas.currentScreen == Canvas.gameScr
                && x >= nutX2() && x < nutX2() + NUT_W && y >= nutYPhim(n) && y < nutYPhim(n) + NUT_H;
    }

    static boolean trongNutTieu(int x, int y) {
        return Nen.coNutUp() && Canvas.currentScreen == Canvas.gameScr
                && x >= nutX() && x < nutX() + NUT_W && y >= nutY2() && y < nutY2() + NUT_H;
    }

    /**
     * Đầu PauseMenu.autoCheDoTick (trước khi game xử lý chạm): chạm nút thì bật/tắt Auto up và nuốt cú
     * chạm - nhân vật không chạy tới chỗ đó, AutoUp.nguoiBam không coi là người chơi chạm để dừng.
     */
    static void nutUpTick() {
        if (!Nen.coNutUp() || Canvas.currentScreen != Canvas.gameScr || Canvas.menu.showMenu || Canvas.currentDialog != null) {
            return;
        }
        if (Canvas.isPointerDown && (trongNutUp(Canvas.px, Canvas.py) || trongNutTieu(Canvas.px, Canvas.py) || trongNutPk(Canvas.px, Canvas.py))) {
            Canvas.isPointerDown = false;
        }
        if (Canvas.isPointerClick && trongNutUp(Canvas.px, Canvas.py) && trongNutUp(Canvas.pxLast, Canvas.pyLast)) {
            Canvas.isPointerClick = false;
            AutoUp.batTat();
        }
        if (Canvas.isPointerClick && trongNutTieu(Canvas.px, Canvas.py) && trongNutTieu(Canvas.pxLast, Canvas.pyLast)) {
            Canvas.isPointerClick = false;
            AutoVanTieu.batTat();
        }
        if (Canvas.isPointerClick && trongNutPk(Canvas.px, Canvas.py) && trongNutPk(Canvas.pxLast, Canvas.pyLast)) {
            Canvas.isPointerClick = false;
            AutoPk.batTat();
        }
        for (int i = 0; i < 5; ++i) {
            if (Canvas.isPointerDown && trongNutPhim(i, Canvas.px, Canvas.py)) {
                Canvas.isPointerDown = false;
            }
            if (!Canvas.isPointerClick || !trongNutPhim(i, Canvas.px, Canvas.py) || !trongNutPhim(i, Canvas.pxLast, Canvas.pyLast)) continue;
            Canvas.isPointerClick = false;
            if (i == 0) {
                AutoPk.nutMaPk();
            } else if (i == 1) {
                AutoPk.nutNhay();
            } else if (i == 2) {
                AutoPk.nutDs();
            } else if (i == 3) {
                AutoPk.nutDung();
            } else {
                AutoPk.nutDoiPet();
            }
        }
    }

    /** Hình tròn tâm (cx, cy) bán kính r tô dải màu dọc từ tren xuống duoi (0xRRGGBB) - vẽ từng hàng nên chạy cả 3 bản. */
    private static void veTron(Graphics graphics, int cx, int cy, int r, int tren, int duoi) {
        for (int dy = -r; dy <= r; ++dy) {
            int w = 0;
            while ((w + 1) * (w + 1) <= r * r + r - dy * dy) {
                ++w;
            }
            graphics.setColor(0xFF000000 | AutoGuard.tronMau(tren, duoi, dy + r, 2 * r));
            graphics.fillRect(cx - w, cy + dy, 2 * w + 1, 1);
        }
    }

    private static int tronMau(int a, int b, int t, int n) {
        if (a == b || n <= 0) {
            return a;
        }
        int r = (a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t / n;
        int g = (a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t / n;
        int c = (a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t / n;
        return r << 16 | g << 8 | c;
    }

    /** Nút tròn: viền vàng nhấp nháy + nền xanh khi đang bật, viền bạc + nền xám khi tắt; chữ UP và BẬT/TẮT. */
    static void veNutUp(Graphics graphics) {
        if (!Nen.coNutUp() || Canvas.currentScreen != Canvas.gameScr) {
            return;
        }
        AutoGuard.veNut(graphics, nutY(), "Up", AutoUp.dang);
        AutoGuard.veNut(graphics, nutY2(), "Tiêu", AutoVanTieu.dang);
        AutoGuard.veNut(graphics, nutY3(), "PK", AutoPk.danh());
        if (AutoPk.hienNut) {
            AutoGuard.veNut(graphics, nutX2(), nutYPhim(0), "Mã PK", AutoPk.maPk ? "Bật" : "Tắt", AutoPk.maPk);
            AutoGuard.veNut(graphics, nutX2(), nutYPhim(1), "Nháy", "Map", false);
            AutoGuard.veNut(graphics, nutX2(), nutYPhim(2), "Bật", "ĐS", false);
            AutoGuard.veNut(graphics, nutX2(), nutYPhim(3), "Dừng", "Hết", false);
            AutoGuard.veNut(graphics, nutX2(), nutYPhim(4), "Đổi", "Pet", false);
        }
    }

    private static void veNut(Graphics graphics, int y, String ten, boolean bat) {
        AutoGuard.veNut(graphics, nutX(), y, ten, bat ? "Bật" : "Tắt", bat);
    }

    private static void veNut(Graphics graphics, int x, int y, String ten, String dong2, boolean bat) {
        int r = NUT_W / 2 - 3;
        int cx = x + NUT_W / 2;
        int cy = y + NUT_H / 2 - 1;
        boolean sang = System.currentTimeMillis() / 500L % 2L == 0L;
        graphics.setClip(0, 0, Canvas.w, Canvas.h);
        AutoGuard.veTron(graphics, cx, cy + 2, r + 1, 0x000000, 0x000000);
        int vien = bat ? (sang ? 0xFFD870 : 0xC8963A) : 0xB4B4B4;
        AutoGuard.veTron(graphics, cx, cy, r, vien, bat ? 0x8A5A10 : 0x5A5A5A);
        AutoGuard.veTron(graphics, cx, cy, r - 2, 0x141414, 0x141414);
        if (bat) {
            AutoGuard.veTron(graphics, cx, cy, r - 3, 0x5ED872, 0x0F5A22);
        } else {
            AutoGuard.veTron(graphics, cx, cy, r - 3, 0x6A6A6A, 0x242424);
        }
        int h = Font.normalFont[0].getHeight();
        Font.normalFont[0].drawString(graphics, ten, cx, cy - h + 1, 2);
        Font.normalFont[0].drawString(graphics, dong2, cx, cy, 2);
    }

    public static void paint(Graphics graphics) {
        try {
            HieuUng.ve(graphics);
        }
        catch (Throwable throwable) {
            // hiệu ứng lỗi thì bỏ qua
        }
        try {
            AutoGuard.veNutUp(graphics);
        }
        catch (Throwable throwable) {
            // nút UP lỗi thì bỏ qua
        }
        try {
            AutoUp.ve(graphics, on ? 4 + AutoUp.caoThanh() : 2);
            AutoVanTieu.ve(graphics, on ? 4 + AutoUp.caoThanh() : 2);
            AutoVanMay.ve(graphics, on ? 4 + AutoUp.caoThanh() : 2);
            LenThanh.ve(graphics, on ? 4 + AutoUp.caoThanh() : 2);
        }
        catch (Throwable throwable) {
            // thanh trạng thái auto up lỗi thì bỏ qua
        }
        try {
            TienIch.batLuong();
        }
        catch (Throwable throwable) {
            // thanh trạng thái auto up lỗi thì bỏ qua
        }
        if (!on) {
            return;
        }
        try {
            PauseMenu.autoPaintStatus(graphics);
        }
        catch (Throwable throwable) {
            // an overlay problem must never break the game's frame
        }
    }
}
