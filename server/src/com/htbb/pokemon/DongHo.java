package com.htbb.pokemon;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;

/**
 * Dong ho chung cua cac he thong theo lich (lich dang nhap, reset ngay/tuan, nhiem vu ngay/tuan, lich su
 * kien 52 tuan, boss the gioi, tran cap mua, ve chuong, PvP mua, Gym tuan, ham nguc, he sinh thai, giftcode
 * thang...). Tren may that: {@link #bay()} == System.currentTimeMillis() (lech luon 0).
 *
 * MAY THU (server.ini sv.cho_phep_thu_nghiem=1 - CauHinhTrungTam.choPhepThuNghiem, khoa khoaProd: CSDL
 * khong bat duoc): GM go "tua <so><d|h|m>" (vd "tua 7d", "tua 3h", "tua 0" = ve gio that) de tua dong ho
 * ao; cong them sv.tua_nhanh_ngay (CongTac.tuaNhanhNgay). Do lech ghi vao tep dong_ho_thu.txt (thu muc chay)
 * de con sau khi server tu khoi dong lai (bao tri / su kien). Moi lan tua ghi nhat ky.
 * Dung cho BotXuyenSuot / bot persona (tools/pokemon/kiem_tai) - xem docs/pokemon/KIEM_THU_XUYEN_SUOT.md.
 *
 * Chi doi thoi gian "lich": khoang cho ngan (hoi chieu, chong spam, timeout ket noi) van dung gio that
 * o noi khac; trong cac tep da noi vao dong ho nay, moi moc deu lay tu day nen hieu so van nhat quan.
 * SQL NOW() trong CSDL khong bi tua.
 */
public final class DongHo {

    static final String TEP = "dong_ho_thu.txt";
    private static volatile long lechGm = -1L; // -1 = chua doc tep

    private DongHo() {
    }

    /** Dong ho ao chi bat tren may thu. */
    public static boolean choPhep() {
        try {
            return com.htbb.cauhinh.CauHinhTrungTam.choPhepThuNghiem();
        } catch (final Throwable t) {
            return false;
        }
    }

    /** Do lech (ms) so voi gio that: 0 tren may that. */
    public static long lech() {
        if (!choPhep()) {
            return 0L;
        }
        if (lechGm < 0L) {
            lechGm = docTep();
        }
        long ngay = 0L;
        try {
            ngay = com.htbb.cauhinh.CongTac.tuaNhanhNgay();
        } catch (final Throwable ignored) {
        }
        return ngay * 86400000L + lechGm;
    }

    public static long bay() {
        return System.currentTimeMillis() + lech();
    }

    public static Calendar lich() {
        final Calendar c = Calendar.getInstance();
        final long l = lech();
        if (l != 0L) {
            c.setTimeInMillis(System.currentTimeMillis() + l);
        }
        return c;
    }

    public static Date ngay() {
        return new Date(bay());
    }

    /**
     * Lenh GM "tua": tham so "7d" / "3h" / "45m" / "0". Tra ve cau tra loi cho GM. Chi tien (khong lui) -
     * lui dong ho lam lech cac moc da ghi (ve chuong, reset ngay). "0" dat lai lech GM ve 0.
     */
    public static synchronized String tua(final String thamSo, final String ai) {
        if (!choPhep()) {
            return "Chỉ dùng trên máy thử (sv.cho_phep_thu_nghiem=1).";
        }
        final String s = thamSo == null ? "" : thamSo.trim().toLowerCase();
        if (lechGm < 0L) {
            lechGm = docTep();
        }
        if (s.isEmpty()) {
            return "Đồng hồ thử: lệch " + lechGm / 1000L + " giây (" + new Date(bay()) + ").";
        }
        long them;
        try {
            if (s.equals("0")) {
                them = -lechGm;
            } else {
                final char d = s.charAt(s.length() - 1);
                final long n = Long.parseLong(Character.isDigit(d) ? s : s.substring(0, s.length() - 1));
                if (n < 0L || n > 3650L * 24L * 60L) {
                    return "Số không hợp lệ.";
                }
                them = d == 'd' ? n * 86400000L : d == 'h' ? n * 3600000L : d == 'm' ? n * 60000L : n * 86400000L;
            }
        } catch (final Exception e) {
            return "Cú pháp: tua <số><d|h|m>, ví dụ tua 7d, tua 3h, tua 0.";
        }
        lechGm += them;
        ghiTep(lechGm);
        final String tin = "tua dong ho thu " + s + " boi " + ai + " -> lech " + lechGm / 1000L + " giay, gio ao " + new Date(bay());
        try {
            com.htbb.log.Nhat.canhBao("DONG_HO", tin);
        } catch (final Throwable t) {
            System.out.println("[DONG_HO] " + tin);
        }
        return "Đã tua: giờ ảo " + new Date(bay()) + ".";
    }

    /**
     * He so tang toc EXP ha quai CHI DE THU (bot choi xuyen suot): JVM -Dhtbb.thu.nhanExp=N tren may thu
     * (sv.cho_phep_thu_nghiem=1). May that / khong dat: 1.
     */
    public static int nhanExpThu() {
        if (!choPhep()) {
            return 1;
        }
        return Math.max(1, Math.min(1000, Integer.getInteger("htbb.thu.nhanExp", 1)));
    }

    /** He so sat thuong vao BOSS (Gym / boss the gioi) CHI DE THU: -Dhtbb.thu.nhanBoss=N tren may thu. May that: 1. */
    public static int nhanBossThu() {
        if (!choPhep()) {
            return 1;
        }
        return Math.max(1, Math.min(1000, Integer.getInteger("htbb.thu.nhanBoss", 1)));
    }

    static long docTep() {
        try {
            final File f = new File(TEP);
            if (!f.isFile()) {
                return 0L;
            }
            return Math.max(0L, Long.parseLong(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).trim()));
        } catch (final Exception e) {
            return 0L;
        }
    }

    static void ghiTep(final long v) {
        try {
            Files.write(new File(TEP).toPath(), String.valueOf(v).getBytes(StandardCharsets.UTF_8));
        } catch (final Exception e) {
            System.out.println("[DONG_HO] khong ghi duoc " + TEP + ": " + e);
        }
    }
}
