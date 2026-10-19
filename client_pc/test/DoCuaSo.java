import javax.microedition.lcdui.NenPc;

/**
 * ĐO FPS TRONG CỬA SỔ THẬT (cần màn hình): chạy bản PC như người chơi (htbb.pc.Chay), dựng nhân vật giả offline
 * (ChupTrongGame.giaLap + 24 người chơi khác), lần lượt mở các cảnh, mỗi cảnh đếm khung game (NenPc.soKhung) và khung
 * trình bày (ManHinh.soTrinhBay) trong 4 giây. In "canh game/s trinhbay/s"; rồi thoát.
 *
 *   java -cp client_pc/out/build/cls:client_pc/out/build/res:$O [-Dhtbb.volatile=false] DoCuaSo <thư mục dữ liệu> [WxH]
 */
public class DoCuaSo {
    public static void main(String[] a) throws Exception {
        String co = a.length > 1 ? a[1] : "1280x720";
        htbb.pc.Chay.main(new String[]{"--cua-so", co, "--khong-am", "--du-lieu", a[0], "--may-chu", "127.0.0.1:1"});
        htbb.pc.CaiDat.doKhung = true;   // bộ đo khung (NhipVe) bật
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ChupTrongGame.chay(() -> DoKhung.themNguoi(24));
        String[] canh = {"the_gioi", "menu", "nv_hanh_trang", "cua_hang_thuoc", "the_gioi"};
        for (String ten : canh) {
            ChupTrongGame.chay(() -> ChupTrongGame.moMan(ten));
            Thread.sleep(800);
            ChupTrongGame.chay(() -> {
                Object d = game.render.Canvas.currentDialog;
                if (d instanceof game.render.screen.MsgDlg || d instanceof game.pokemon.HopThongBao) game.render.Canvas.endDlg();
            });
            long k0 = NenPc.soKhung, t0 = htbb.pc.ManHinh.soTrinhBay, m0 = System.nanoTime();
            long gc0 = DoKhung.gc(), cap0 = capPhat();
            Thread.sleep(4000);
            double s = (System.nanoTime() - m0) / 1e9;
            long[] tb = game.pokemon.NhipVe.TB;
            System.out.printf("%-16s game %5.1f/s  trinh bay %5.1f/s | nhap %.2f logic %.2f the gioi %.2f giao dien %.2f trinh bay %.2f ms | khung p50 %.1f p99 %.1f ms | GC %.1f/s | cap phat %.0f KB/s%n",
                    ten, (NenPc.soKhung - k0) / s, (htbb.pc.ManHinh.soTrinhBay - t0) / s, tb[0] / 1000f, tb[1] / 1000f, tb[2] / 1000f, tb[3] / 1000f,
                    htbb.pc.ManHinh.tbTrinhBay / 1000f, game.pokemon.NhipVe.p50x10 / 10f, game.pokemon.NhipVe.p99x10 / 10f,
                    (DoKhung.gc() - gc0) / s, (capPhat() - cap0) / 1024.0 / s);
        }
        htbb.pc.Chay.thoat();
    }

    static long capPhat() {
        com.sun.management.ThreadMXBean t = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        long n = 0;
        for (long x : t.getThreadAllocatedBytes(t.getAllThreadIds())) if (x > 0) n += x;
        return n;
    }
}
