package game.pokemon;

import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.RMS;
import java.util.Vector;
import network.Message;

/**
 * Menu "Ngoại hình" (hàng mod của PauseMenu / tab Khác): ẩn/hiện từng phụ kiện trên người mình và trang phục
 * (gửi -103 lên server, mọi người thấy theo), và "Phụ kiện người khác" (chỉ phía client, RMS "htbbnh") để máy
 * yếu tắt bớt hình. Chỉ đổi HÌNH, không đổi chỉ số.
 */
public final class NgoaiHinhMenu {
    static final String[] TEN_O = {"Mũ / nơ", "Khăn quàng", "Vòng cổ", "Đồ cầm tay", "Ruy băng / áo choàng",
        "Phiến đá hệ", "Kính"};
    private static final String RMS_TEN = "htbbnh";
    private static boolean daNap;

    private NgoaiHinhMenu() {
    }

    public static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        try {
            String s = RMS.loadRMSString(RMS_TEN);
            if (s != null && s.length() > 0) {
                NgoaiHinhVe.hien = s.charAt(0) != '0';
            }
        } catch (Throwable e) {
            // mac dinh: hien
        }
    }

    static int anHienTai() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null || Canvas.gameScr.mainChar.ngoaiHinh == null) {
            return 0;
        }
        return Canvas.gameScr.mainChar.ngoaiHinh.an;
    }

    /** Gui mat na an len server (-103 loai 0). Server gui lai -102. */
    static void gui(int an) {
        try {
            Message m = new Message(-103);
            m.writer().writeByte(0);
            m.writer().writeByte(an);
            GameService.gI().session.sendMessage(m);
            m.cleanup();
            if (Canvas.gameScr.mainChar.ngoaiHinh != null) {
                Canvas.gameScr.mainChar.ngoaiHinh.an = an;   // menu mo lai hien ngay
            }
        } catch (Throwable e) {
            // mat ket noi
        }
    }

    public static void mo(final int chon) {
        nap();
        final int an = anHienTai();
        Vector v = new Vector();
        v.addElement(new Command("Trang phục: " + ((an & NgoaiHinhCua.AN_TRANG_PHUC) != 0 ? "Ẩn" : "Hiện"), new IAction() {
            public void perform() {
                gui(an ^ NgoaiHinhCua.AN_TRANG_PHUC);
                mo(0);
            }
        }));
        for (int i = 0; i < TEN_O.length; i++) {
            final int o = i;
            v.addElement(new Command(TEN_O[i] + ": " + ((an & (1 << i)) != 0 ? "Ẩn" : "Hiện"), new IAction() {
                public void perform() {
                    gui(an ^ (1 << o));
                    mo(o + 1);
                }
            }));
        }
        v.addElement(new Command("Phụ kiện người khác: " + (NgoaiHinhVe.hien ? "Hiện" : "Tắt"), new IAction() {
            public void perform() {
                NgoaiHinhVe.hien = !NgoaiHinhVe.hien;
                try {
                    RMS.saveRMSString(RMS_TEN, NgoaiHinhVe.hien ? "1" : "0");
                } catch (Throwable e) {
                    // bo qua
                }
                mo(TEN_O.length + 1);
            }
        }));
        Canvas.menu.startAt(v, 3);
        if (chon > 0 && chon < v.size()) {
            Canvas.menu.selected = chon;
        }
    }
}
