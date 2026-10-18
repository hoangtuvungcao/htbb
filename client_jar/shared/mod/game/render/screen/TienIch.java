package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.Tilemap;
import game.render.Canvas;
import java.util.Vector;
import network.Session_ME;

/**
 * Menu Tiện ích (chỉ thứ KHÔNG vượt luật máy chủ): mở cửa hàng chính thức, đăng xuất, bật/tắt popup thông báo.
 * Đã bỏ (vượt luật / tự động hoá ngoài máy chủ): tự đăng nhập lại, tự mua thuốc / vé / thẻ, tầm chiêu 300 phía
 * client, về làng không chờ 10 giây, mở Pidgeot (Bay) từ xa không cần tới NPC.
 */
final class TienIch {
    private static final String RMS_TEN = "htbbti";
    /** Tắt thông báo: nuốt hộp thoại OK của server khi đang trong game (màn đăng nhập vẫn hiện). */
    static boolean tatThongBao;
    private static boolean daNap;

    private TienIch() {
    }

    static void moMenu(int chon) {
        TienIch.nap();
        Vector vector = new Vector();
        vector.addElement(new Command("Cửa hàng", new IAction(){

            public void perform() {
                Canvas.menu.showMenu = false;
                PauseMenu.gI().showShopNew(0);
            }
        }));
        vector.addElement(new Command("Đăng xuất", new IAction(){

            public void perform() {
                TienIch.dangXuat();
            }
        }));
        vector.addElement(new Command(tatThongBao ? "Thông báo: tắt" : "Thông báo: bật", new IAction(){

            public void perform() {
                tatThongBao = !tatThongBao;
                TienIch.luu();
                TienIch.moMenu(2);
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    private static void dangXuat() {
        Canvas.menu.showMenu = false;
        TuDong.tat(true);
        TienIch.veManChonServer();
    }

    /** Y hệt nút OK của hộp "Bị ngắt kết nối" (GameScr.onDisconnect). */
    private static void veManChonServer() {
        Canvas.endDlg();
        Canvas.instance.init();
        Session_ME.gI().close();
        Tilemap.loadMap(0, null);
        ServerListScr.gI().switchToMe();
    }

    /** Gọi từ AutoGuard.autoEatDlg: true = nuốt hộp thoại này. */
    static boolean anThongBao(String string) {
        TienIch.nap();
        return tatThongBao && Canvas.gameScr != null && Canvas.currentScreen == Canvas.gameScr;
    }

    private static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        try {
            String string = RMS.loadRMSString(RMS_TEN);
            if (string != null && string.length() >= 1) {
                tatThongBao = string.charAt(0) == '1';
            }
        }
        catch (Throwable throwable) {
            // mặc định
        }
    }

    private static void luu() {
        try {
            RMS.saveRMSString(RMS_TEN, tatThongBao ? "1" : "0");
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}
