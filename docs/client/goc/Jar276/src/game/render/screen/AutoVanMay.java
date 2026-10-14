package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Auto thử vận may hộp vàng 20 lượng (NPC -10 Tổng quản, KPAH real/MapDoMenu.java case -10).
 * Mỗi lượt gửi liền (server xử lý tuần tự): menu 0 mục 2 "Thử vận may" -> menu 2 mục 1 "Hộp vàng"
 * (typeBox = 1) -> menu 3 mục 0 "5 hộp 20l" (trừ 20 lượng, mở bảng hộp) -> lệnh -62 kiểu 2 mở hết hộp.
 * Menu NPC -10 và bảng hộp (MiniGame) hiện ra giữa chừng thì tự đóng; câu báo thưởng đọc qua
 * AutoGuard.autoEatDlg. Dừng khi đủ số lượt, server báo "Không đủ tiền." hoặc bấm phím bất kỳ.
 * Gửi theo đợt: tối đa DOT lượt nối đuôi (4 gói/lượt), mỗi đợt cách nhau >= 1s -> ~20 gói/giây,
 * dưới xa trần chống spam của server (io.BoDemGoiTin: 300 gói / 5s).
 */
final class AutoVanMay {
    private static final int NPC = -10;
    private static final int[] SO_LUOT = new int[]{10, 50, 100, 0};
    private static final int DOT = 5;

    static boolean dang;
    private static int soLuot;
    private static long luc;
    private static long lucDot;
    private static long lucBat;
    private static int daMo;
    private static int trung;
    private static String thuongCuoi = "";
    private static final Vector thongBao = new Vector();
    private static int choKetQua;
    private static boolean hetLuong;

    private AutoVanMay() {
    }

    static void moMenu(int chon) {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(dang ? "Tắt vận may" : "Bật vận may (hộp vàng 20l)", new IAction(){

            public void perform() {
                if (dang) {
                    AutoVanMay.tat();
                } else {
                    AutoVanMay.bat();
                }
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Số lượt: ").append(SO_LUOT[soLuot] == 0 ? "tới khi hết lượng" : String.valueOf(SO_LUOT[soLuot])).toString(), new IAction(){

            public void perform() {
                soLuot = (soLuot + 1) % SO_LUOT.length;
                AutoVanMay.moMenu(1);
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    private static void bat() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        AutoUp.tat();
        AutoVanTieu.tat();
        dang = true;
        daMo = 0;
        trung = 0;
        thuongCuoi = "";
        thongBao.removeAllElements();
        choKetQua = 0;
        hetLuong = false;
        lucDot = 0L;
        lucBat = System.currentTimeMillis();
        Canvas.menu.showMenu = false;
    }

    static void tat() {
        if (!dang) {
            return;
        }
        dang = false;
        Canvas.menu.showMenu = false;
    }

    private static void dung(String string) {
        AutoVanMay.tat();
        if (Canvas.currentScreen != Canvas.gameScr && Canvas.gameScr != null) {
            Canvas.gameScr.switchToMe();
        }
        Canvas.startOKDlg(string);
    }

    /** Gọi từ AutoGuard.autoEatDlg: nuốt mọi hộp thoại server trong lúc chạy, giữ lại câu cuối. */
    static boolean thongBao(String string) {
        if (!dang || string == null) {
            return false;
        }
        thongBao.addElement(string);
        return true;
    }

    /** Gọi từ AutoGuard.goiGuard: bảng hộp (lệnh -62) không mở ra - mở hết hộp ngay bằng lệnh. */
    static boolean anGoi(int n) {
        return dang && n == -62;
    }

    /** Gọi từ AutoGuard.menuGuard: menu của NPC -10 hiện ra giữa chừng thì không hiện. */
    static boolean anMenu(int n) {
        return dang && n == NPC;
    }

    static void tick() {
        if (!dang) {
            return;
        }
        GameScr gameScr = Canvas.gameScr;
        if (gameScr == null || gameScr.mainChar == null) {
            return;
        }
        long l = System.currentTimeMillis();
        // bấm phím / chạm màn hình bất kỳ thì dừng (bỏ qua 0.5s đầu: chính cú bấm bật auto)
        if (l - lucBat > 500L && AutoVanMay.coBam()) {
            AutoVanMay.dung(new StringBuilder().append("Đã dừng vận may. Đã mở ").append(daMo).append(" lượt, trúng ").append(trung).append(" lần.").toString());
            return;
        }
        // đọc hết kết quả đã về
        while (thongBao.size() > 0) {
            String string = (String)thongBao.elementAt(0);
            thongBao.removeElementAt(0);
            if (string.startsWith("Không đủ")) {
                hetLuong = true;
                choKetQua = Math.max(0, choKetQua - 1);
                continue;
            }
            String string2 = "Chúc mừng bạn đã nhận được ";
            if (string.startsWith(string2)) {
                ++trung;
                thuongCuoi = string.substring(string2.length()).trim();
            } else if (!string.startsWith("Chúc bạn may mắn")) {
                continue;
            }
            ++daMo;
            choKetQua = Math.max(0, choKetQua - 1);
            luc = l;
        }
        AutoVanMay.dongBang(gameScr);
        if (choKetQua > 0) {
            // còn lượt chưa có kết quả: chờ, quá 5s không thấy gì thì bỏ qua số còn thiếu
            if (l - luc > 5000L) {
                choKetQua = 0;
            }
            return;
        }
        if (hetLuong) {
            AutoVanMay.dung(new StringBuilder().append("Vận may: hết lượng. Đã mở ").append(daMo).append(" lượt, trúng ").append(trung).append(" lần.").toString());
            return;
        }
        int n = SO_LUOT[soLuot];
        if (n > 0 && daMo >= n) {
            AutoVanMay.dung(new StringBuilder().append("Vận may: xong ").append(daMo).append(" lượt, trúng ").append(trung).append(" lần.").toString());
            return;
        }
        if (l - lucDot < 1000L) {
            return;
        }
        int n2 = n > 0 ? Math.min(DOT, n - daMo) : DOT;
        GameService gameService = GameService.gI();
        for (int i = 0; i < n2; ++i) {
            gameService.doMenuOption(NPC, (byte)0, 2);
            gameService.doMenuOption(NPC, (byte)2, 1);
            gameService.doMenuOption(NPC, (byte)3, 0);
            Nen.moRuongVanMay(gameService);
        }
        choKetQua = n2;
        lucDot = l;
        luc = l;
    }

    private static boolean coBam() {
        for (int i = 0; i < Canvas.keyPressed.length; ++i) {
            if (!Canvas.keyPressed[i]) continue;
            return true;
        }
        return Canvas.isPointerDown || Canvas.isPointerClick;
    }

    private static void dongBang(GameScr gameScr) {
        Canvas.menu.showMenu = false;
        if (Nen.laManRuong(Canvas.currentScreen)) {
            gameScr.switchToMe();
        }
    }

    static void ve(Graphics graphics, int n) {
        if (!dang || Font.normalFont == null || Font.normalFont[0] == null) {
            return;
        }
        String string = new StringBuilder().append("Vận may: lượt ").append(daMo).append(" | trúng ").append(trung).append(thuongCuoi.length() > 0 ? new StringBuilder().append(" | ").append(thuongCuoi).toString() : "").toString();
        Font font = Font.normalFont[0];
        while (string.length() > 6 && font.getWidth(string) > Canvas.w - 12) {
            string = string.substring(0, string.length() - 1);
        }
        int n2 = Math.min(Canvas.w, font.getWidth(string) + 12);
        int n3 = font.getHeight() + 6;
        int n4 = (Canvas.w - n2) / 2;
        graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
        graphics.setClip(0, 0, Canvas.w, Canvas.h);
        graphics.setColor(0xFF2E2A0B);
        graphics.fillRect(n4, n, n2, n3);
        graphics.setColor(0xFFE0D040);
        graphics.drawRect(n4, n, n2 - 1, n3 - 1);
        font.drawString(graphics, string, Canvas.w / 2, n + 3, 2);
    }
}
