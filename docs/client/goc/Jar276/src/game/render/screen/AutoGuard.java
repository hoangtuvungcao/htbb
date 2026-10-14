package game.render.screen;

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
        return false;
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

    public static void paint(Graphics graphics) {
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
