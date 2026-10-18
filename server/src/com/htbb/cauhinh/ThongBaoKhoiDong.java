package com.htbb.cauhinh;

import java.sql.Connection;

/**
 * Bao nguoi choi + thoat co trat tu cho yeu cau khoi dong lai an toan (CauHinhTrungTam#demNguoc).
 *
 * Tin chay tren dinh man hinh (tin 38, nhu real.BaoTriDaily.baoTruocBaoTri). Gui IT tin: client xep hang tin
 * nay va moi tin mat 15-20 giay moi troi het (xem chu thich BaoTriDaily) - gui dau dem nguoc va moc 60 giay.
 *
 * Thoat: bat co {@code 5h_systems command='restart'} -> luong BaoTriDaily (doc moi 5 giay) luu clan, nhan vat,
 * cho, su kien, log roi System.exit(0); systemd (Restart=always) / chay_lap.sh bat lai. Khong co luong do
 * hoac khong co dong 5h_systems -> System.exit(0) truc tiep (shutdown hook cua TeamServer luu du lieu).
 * Chot cuoi: 90 giay sau van con song -> System.exit(0).
 */
final class ThongBaoKhoiDong {

    private ThongBaoKhoiDong() {
    }

    static void gui(final int giay, final String lyDo) {
        final String khiNao = giay >= 60 && giay % 60 == 0 ? (giay / 60) + " phút" : giay + " giây";
        guiTatCa("Máy chủ sẽ khởi động lại sau " + khiNao + (lyDo == null || lyDo.trim().isEmpty() ? "" : " (" + lyDo.trim() + ")")
                + ". Dữ liệu được lưu tự động, vui lòng đứng yên ở nơi an toàn.");
    }

    static void guiHuy() {
        guiTatCa("Đã huỷ khởi động lại máy chủ. Chúc bạn chơi vui!");
    }

    static void guiTatCa(final String noiDung) {
        try {
            final io.Message m = real.MessageCreator.createthongbao(noiDung);
            int so = 0;
            for (int i = 0; i < real.CharManager.instance.vChars.size(); ++i) {
                final real.Char c = real.CharManager.instance.vChars.elementAt(i);
                c.sendMessage(m);
                if (c.isBot == -1) {
                    so++;
                }
            }
            m.cleanup();
            System.out.println("[CAU HINH] da bao " + so + " nguoi choi: " + noiDung);
        } catch (final Throwable e) {
            System.out.println("[CAU HINH] khong gui duoc thong bao: " + e);
        }
    }

    static void thoat() {
        boolean coCo = false;
        if (real.BaoTriDaily.Running) {
            Connection c = null;
            try {
                c = CauHinhTrungTam.layKetNoi();
                coCo = CauHinhTrungTam.capNhat(c, "UPDATE 5h_systems SET status = 0 WHERE command = 'restart'") > 0;
            } catch (final Exception e) {
                System.out.println("[CAU HINH] khong bat duoc co 5h_systems: " + e);
            } finally {
                CauHinhTrungTam.traKetNoi(c);
            }
        }
        if (!coCo) {
            System.out.println("[CAU HINH] khong co luong bao tri / dong 5h_systems -> System.exit(0) (shutdown hook luu du lieu)");
            System.exit(0);
            return;
        }
        final Thread chot = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(90000L);
                } catch (final InterruptedException e) {
                    return;
                }
                System.out.println("[CAU HINH] 90 giay chua thoat -> System.exit(0)");
                System.exit(0);
            }
        }, "CauHinhTrungTam-chot-thoat");
        chot.setDaemon(true);
        chot.start();
    }
}
