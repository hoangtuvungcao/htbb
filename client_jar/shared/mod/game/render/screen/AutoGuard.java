package game.render.screen;

import game.render.Canvas;

import game.networklogic.GameService;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Cổng hook của code mod dùng chung (jar / APK / PC). Sau khi bỏ mod NQSH (Auto up, PK, Vận may,
 * Lên thành, Giao hàng tự động, Chế đồ nhanh, Dòng ẩn, Xa phu từ xa...) chỉ còn:
 *  - Tự động đánh CHÍNH THỨC (TuDong): gói -100 của máy chủ, vòng mỗi khung hình, thanh trạng thái;
 *  - Menu nhanh / nút nổi (Nen), hiệu ứng (HieuUng), Tiện ích (đăng xuất, tắt popup);
 *  - đăng nhập kèm mã máy + số bản, máy chủ gắn lúc build (TaiDanhSach);
 *  - hai sửa lỗi hiển thị của game gốc (NPC mất khi vào lại cùng map, xe giao hàng biến mất khi đổi màu).
 */
public final class AutoGuard {
    /** Luôn false: không còn auto nào ẩn giao diện / nuốt thông báo của game. */
    public static boolean on;

    private AutoGuard() {
    }

    /** Canvas.startOKDlg: true = nuốt hộp thoại (Tiện ích "Thông báo: tắt"). */
    public static boolean autoEatDlg(String string) {
        try {
            return TienIch.anThongBao(string);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    // ---------------------------------------------------------------- đăng nhập

    /** Chèn ở ĐẦU GameService.login: gửi kèm mã máy. */
    public static boolean loginGuard(String string, String string2, String string3) {
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

    /** Chèn ở ĐẦU MessageHandler.onMessage: -100 Tự động đánh. true = đã xử lý. */
    public static boolean goiGuard(network.Message message) {
        try {
            return TuDong.nhan(message);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    // ---------------------------------------------------------------- sửa lỗi hiển thị của game gốc

    /** Chèn ở ĐẦU GameScr.onActorMove: không chặn, chỉ bỏ actor quái đang chết trùng id (xe giao hàng đổi màu). */
    public static boolean viTriGuard(byte by, short s, short s2, short s3, short s4) {
        try {
            AutoGuard.boQuaiDangChet(by, s2);
        }
        catch (Throwable throwable) {
            // không bao giờ cản lệnh di chuyển của game
        }
        return false;
    }

    /**
     * Lệnh 4 cho quái (cat 1) mà actor cùng id đang chạy hiệu ứng chết (lệnh 90, die = true): bỏ actor cũ để
     * onActorMove tạo lại actor mới (đúng loại + mã chủ hàng). Đổi màu hàng là vậy: server gửi 90 rồi 4 cùng id với
     * loại xe mới; giữ actor cũ thì nó chỉ đổi vị trí rồi bị xoá ở khung hình sau -> xe biến mất tới khi nó đi lại.
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
     * Chèn ở ĐẦU GameScr.onMap. onMap luôn xóa hết actor (cả NPC tĩnh của map) nhưng chỉ nạp lại map - và NPC -
     * khi id map KHÁC map đang đứng. Server gửi lại cùng id (đăng nhập lại đúng map cũ, đổi khu) thì NPC mất hẳn.
     * Đặt map = -1 để luôn nạp lại; nạp xong client gửi changeMapOK(-500) và server gửi lại quái gần.
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

    // ---------------------------------------------------------------- danh sách server

    /** Chèn ở ĐẦU ServerListScr.switchToMe: lần đầu mở màn chọn server thì đặt danh sách = máy chủ gắn lúc build. */
    public static boolean dsGuard() {
        try {
            TaiDanhSach.taiNgam();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    /** Chèn ở ĐẦU ServerListScr.doUpdateServer: nút "Cập nhật" (không tải gì). */
    public static boolean capNhatGuard() {
        try {
            TaiDanhSach.capNhat();
            return true;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    // ---------------------------------------------------------------- phím, vòng, vẽ

    /** Chèn ở đầu xử lý phím của Canvas: Menu nhanh / bảng Tự động đang mở dùng phím. true = đã dùng. */
    public static boolean phimPk(int n) {
        try {
            return Nen.menuNhanhPhim(n);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /** Mỗi khung hình của GameScr.update (trước khi game xử lý chạm). */
    public static void tick() {
        try {
            if (!Nen.menuNhanhTick()) {
                AutoGuard.nutTick();
            }
            TuDong.tick();
        }
        catch (Throwable throwable) {
            // tự động lỗi không được làm hỏng khung hình của game
        }
    }

    public static void paint(Graphics graphics) {
        try {
            HieuUng.ve(graphics);
        }
        catch (Throwable throwable) {
            // hiệu ứng lỗi thì bỏ qua
        }
        try {
            if (!Nen.menuNhanhVe(graphics)) {
                AutoGuard.veNut(graphics);
            }
        }
        catch (Throwable throwable) {
            // nút lỗi thì bỏ qua
        }
        try {
            TuDong.ve(graphics, Nen.yTrangThai(2));
        }
        catch (Throwable throwable) {
            // thanh trạng thái lỗi thì bỏ qua
        }
    }

    // ---------------------------------------------------------------- nút nổi "Tự động" (bản không có Menu nhanh)

    static final int NUT = Nen.CO_NUT;

    static int nutX() {
        return Canvas.w - NUT - 6;
    }

    static int nutY() {
        return Canvas.h / 3;
    }

    static boolean trongNut(int x, int y) {
        return Nen.coNutUp() && Canvas.currentScreen == Canvas.gameScr
                && x >= nutX() && x < nutX() + NUT && y >= nutY() && y < nutY() + NUT;
    }

    /** Chạm nút: mở bảng Tự động đánh; nuốt cú chạm (Pokémon không chạy tới chỗ đó). */
    private static void nutTick() {
        if (!Nen.coNutUp() || Canvas.currentScreen != Canvas.gameScr || Canvas.menu.showMenu || Canvas.currentDialog != null) {
            return;
        }
        if (Canvas.isPointerDown && trongNut(Canvas.px, Canvas.py)) {
            Canvas.isPointerDown = false;
        }
        if (Canvas.isPointerClick && trongNut(Canvas.px, Canvas.py) && trongNut(Canvas.pxLast, Canvas.pyLast)) {
            Canvas.isPointerClick = false;
            Nen.moBangTuDong();
        }
    }

    private static void veNut(Graphics graphics) {
        if (!Nen.coNutUp() || Canvas.currentScreen != Canvas.gameScr) {
            return;
        }
        int r = NUT / 2 - 3;
        int cx = nutX() + NUT / 2;
        int cy = nutY() + NUT / 2 - 1;
        boolean bat = TuDong.dang;
        graphics.setClip(0, 0, Canvas.w, Canvas.h);
        graphics.setColor(0x141414);
        graphics.fillArc(cx - r - 1, cy - r + 1, 2 * r + 2, 2 * r + 2, 0, 360);
        graphics.setColor(bat ? 0xFFD870 : 0xB4B4B4);
        graphics.fillArc(cx - r, cy - r, 2 * r, 2 * r, 0, 360);
        graphics.setColor(bat ? 0x2E9E48 : 0x4A4A4A);
        graphics.fillArc(cx - r + 3, cy - r + 3, 2 * r - 6, 2 * r - 6, 0, 360);
        int h = Font.normalFont[0].getHeight();
        Font.normalFont[0].drawString(graphics, "Tự động", cx, cy - h + 1, 2);
        Font.normalFont[0].drawString(graphics, bat ? "Bật" : "Tắt", cx, cy, 2);
    }
}
