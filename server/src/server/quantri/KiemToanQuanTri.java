package server.quantri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NHAT KY KIEM TOAN cua bang dieu khien may chu: MOI thao tac thay doi trang thai (kick, cam,
 * phat qua, bao tri, sua cau hinh...) ghi MOT dong:
 *   - tep AdminPanel_kiem_toan.txt o thu muc lam viec (canh AdminChat_lenh47.txt), them cuoi,
 *     ghi tren luong rieng (khong bao gio cham EDT / luong game);
 *   - com.htbb.log.Nhat.kiemToan("ADMIN", ...) -> tep kiem_toan-yyyyMMdd.log cua nhat ky chung;
 *   - bo dem 500 dong gan nhat trong RAM cho trang "Nhat ky".
 * Noi dung duoc lam sach (xuong dong / ky tu dieu khien) de khong gia mao them dong.
 */
public final class KiemToanQuanTri {

    public static final String TEP = "AdminPanel_kiem_toan.txt";
    static final int GIU_TRONG_RAM = 500;

    private static final ArrayDeque<String> GAN_DAY = new ArrayDeque<String>();
    private static final List<Runnable> NGHE = new CopyOnWriteArrayList<Runnable>();
    private static final ExecutorService GHI = Executors.newSingleThreadExecutor(r -> {
        final Thread t = new Thread(r, "PANEL QUAN TRI - kiem toan");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });
    private static volatile File tep = new File(TEP);
    /** Kiem thu: khong ghi tep. */
    static volatile boolean tatGhiTep = false;

    private KiemToanQuanTri() {
    }

    /** Doi noi ghi (kiem thu). */
    static void datTep(final File f) {
        tep = f;
    }

    public static String sach(final String s) {
        if (s == null) {
            return "";
        }
        final StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            b.append(c < 0x20 || c == 0x7f ? ' ' : c);
        }
        return b.toString().trim();
    }

    /**
     * @param hanhDong ma ngan, vd KICK, CAM, BAO_TRI
     * @param doiTuong nhan vat / tai khoan / khoa cau hinh (co the rong)
     * @param chiTiet  ly do, gia tri, ket qua
     */
    public static String ghi(final String hanhDong, final String doiTuong, final String chiTiet) {
        final String nguoi = sach(System.getProperty("user.name", "?"));
        final String dong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " [" + sach(hanhDong) + "] "
                + (doiTuong == null || doiTuong.isEmpty() ? "" : "doi_tuong=" + sach(doiTuong) + " ")
                + "boi=panel:" + nguoi + " | " + sach(chiTiet);
        synchronized (GAN_DAY) {
            GAN_DAY.addLast(dong);
            while (GAN_DAY.size() > GIU_TRONG_RAM) {
                GAN_DAY.removeFirst();
            }
        }
        try {
            com.htbb.log.Nhat.kiemToan("ADMIN", "[PANEL] " + sach(hanhDong) + " " + sach(doiTuong) + " " + sach(chiTiet));
        } catch (final Throwable ignored) {
        }
        if (!tatGhiTep) {
            final File f = tep;
            GHI.execute(() -> {
                try (Writer w = new OutputStreamWriter(new FileOutputStream(f, true), StandardCharsets.UTF_8)) {
                    w.write(dong);
                    w.write('\n');
                } catch (final Throwable ignored) {
                }
            });
        }
        for (final Runnable r : NGHE) {
            try {
                r.run();
            } catch (final Throwable ignored) {
            }
        }
        return dong;
    }

    public static List<String> ganDay() {
        synchronized (GAN_DAY) {
            return new ArrayList<String>(GAN_DAY);
        }
    }

    public static void nghe(final Runnable r) {
        NGHE.add(r);
    }

    public static File tep() {
        return tep;
    }
}
