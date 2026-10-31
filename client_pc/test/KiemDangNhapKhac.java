import java.io.File;
import javax.microedition.lcdui.NenPc;

/**
 * 31/10/2026 (nhật ký máy chủ: hai client cùng tài khoản đá nhau 3 giây một lần - client bị đá tự nối lại rồi đá ngược):
 * client đang chơi nhận thông báo máy chủ (lệnh 37) "Tài khoản của bạn vừa đăng nhập ở thiết bị khác." rồi rớt kết nối thật
 * (MessageHandler.onDisconnected) -> KHÔNG tự nối lại (TuNoiLai không hẹn lần thử), hộp mất kết nối ghi đúng lý do + nút
 * "Kết nối lại" thủ công; bấm "Kết nối lại" bỏ cờ. Đối chứng: rớt mạng thường (không thông báo) vẫn tự nối lại.
 *
 *   javac -cp client_pc/out/build/cls -sourcepath client_pc/test:server/src -d $O client_pc/test/KiemDangNhapKhac.java
 *   java -Djava.awt.headless=true -Dhtbb.mayChu=$PWD/server -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemDangNhapKhac <ra>
 */
public class KiemDangNhapKhac {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static network.Message goi(int lenh, byte[] du) throws Exception {
        java.lang.reflect.Constructor<network.Message> c = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        c.setAccessible(true);
        return c.newInstance((byte) lenh, du);
    }

    static byte[] thongBao(String s) throws Exception {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream o = new java.io.DataOutputStream(b);
        o.writeUTF(s);
        o.writeUTF("");
        return b.toByteArray();
    }

    static String chuHop() {
        Object d = game.render.Canvas.currentDialog;
        if (!(d instanceof game.render.screen.MsgDlg)) {
            return "";
        }
        try {
            java.lang.reflect.Field f = game.render.screen.MsgDlg.class.getDeclaredField("info");
            f.setAccessible(true);
            String[] a = (String[]) f.get(d);
            StringBuilder b = new StringBuilder();
            for (String x : a) b.append(x).append(' ');
            return b.toString();
        } catch (Throwable e) {
            return "";
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "kiem_dang_nhap_khac");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                game.render.screen.TField.isQwerty = true;
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        Thread.sleep(800);
        final network.Message tb = goi(37, thongBao(game.pokemon.TuNoiLai.DANG_NHAP_KHAC));
        // 1) bị đá vì đăng nhập nơi khác: không tự nối lại, hộp ghi lý do
        ChupTrongGame.chay(() -> {
            game.render.Canvas.gameScr.switchToMe();
            game.render.Canvas.endDlg();
            game.pokemon.TuNoiLai.nho("kt001", "123456");
            game.pokemon.TuNoiLai.vaoGame();
            game.render.screen.GameScr.autoFight = false;
            game.networklogic.MessageHandler.gI().onMessage(tb);
            kiem(game.pokemon.TuNoiLai.biDangNhapKhac, "thong bao 'dang nhap o thiet bi khac' -> danh dau bi da");
            game.networklogic.MessageHandler.gI().onDisconnected();
            kiem(!game.pokemon.TuNoiLai.dangNoi, "bi da vi dang nhap noi khac -> KHONG tu ket noi lai");
            String h = chuHop();
            kiem(h.contains("thiết bị khác"), "hop mat ket noi ghi ly do dang nhap noi khac: '" + h + "'");
            Object d = game.render.Canvas.currentDialog;
            kiem(d instanceof game.render.screen.MsgDlg && ((game.render.screen.MsgDlg) d).center != null
                    && "Kết nối lại".equals(((game.render.screen.MsgDlg) d).center.caption), "hop co nut 'Ket noi lai' thu cong");
            game.pokemon.TuNoiLai.thuCong();
            kiem(!game.pokemon.TuNoiLai.biDangNhapKhac, "bam Ket noi lai -> bo co bi da");
            game.pokemon.TuNoiLai.vaoGame();
            game.render.Canvas.endDlg();
        });
        Thread.sleep(6000);   // qua cửa sổ 5 s "vừa có thông báo" của TuNoiLai
        // 2) đối chứng: rớt mạng thường vẫn tự nối lại
        ChupTrongGame.chay(() -> {
            game.render.Canvas.gameScr.switchToMe();
            game.pokemon.TuNoiLai.nho("kt001", "123456");
            game.pokemon.TuNoiLai.vaoGame();
            game.networklogic.MessageHandler.gI().onDisconnected();
            kiem(game.pokemon.TuNoiLai.dangNoi, "rot mang thuong (khong thong bao) -> van tu ket noi lai");
            game.pokemon.TuNoiLai.tat = true;
        });
        System.out.println("KiemDangNhapKhac: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
