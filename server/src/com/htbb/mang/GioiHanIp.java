package com.htbb.mang;

/**
 * Cac gioi han THEO IP con lai trong luong dang nhap / chon nhan vat (truoc day viet cung
 * trong LoginHandler / SelectCharHandler). Moi ham deu bo qua IP proxy tin cay
 * ({@link ProxyTinCay}, sv.proxyTinCay) - sau WAF ca server chung vai IP.
 */
public final class GioiHanIp {

    public static final int DANG_NHAP_NGUONG_MAC_DINH = 10;
    public static final int DANG_NHAP_CHO_MAC_DINH_MS = 3000;

    /**
     * sv.dangNhapIpNguong: so lan dang nhap/giay (dinh cao, InfoClientConnect.maxLogin) tu MOT
     * IP thi bat dau cho. 0 = tat.
     */
    public static volatile int dangNhapNguong = DANG_NHAP_NGUONG_MAC_DINH;
    /** sv.dangNhapIpChoMs: so ms cho moi lan dang nhap khi vuot nguong. 0 = khong cho (chi ghi log). */
    public static volatile int dangNhapChoMs = DANG_NHAP_CHO_MAC_DINH_MS;

    private GioiHanIp() {
    }

    /** Kep va dat hai nguong dang nhap (khong doc duoc -> mac dinh; ngoai khoang -> kep). */
    public static void datDangNhap(final String nguong, final String choMs) {
        dangNhapNguong = kep(nguong, DANG_NHAP_NGUONG_MAC_DINH, 0, 100000);
        dangNhapChoMs = kep(choMs, DANG_NHAP_CHO_MAC_DINH_MS, 0, 10000);
    }

    /** IP nay co dem/ap gioi han theo IP khong (false = proxy tin cay). */
    public static boolean apDung(final String ip) {
        return !ProxyTinCay.tinCay(ip);
    }

    /** IP nay vuot nguong dang nhap (de ghi log nghi van + cho). Proxy tin cay: luon false. */
    public static boolean vuotNguongDangNhap(final String ip, final int maxLogin) {
        final int n = dangNhapNguong;
        return n > 0 && maxLogin >= n && apDung(ip);
    }

    /** So ms phai cho truoc khi xu ly dang nhap (0 = khong cho). */
    public static long choDangNhapMs(final String ip, final int maxLogin) {
        return vuotNguongDangNhap(ip, maxLogin) ? Math.max(0, dangNhapChoMs) : 0L;
    }

    static int kep(final String s, final int macDinh, final int min, final int max) {
        int v;
        try {
            v = Integer.parseInt(s == null ? "" : s.trim());
        } catch (final NumberFormatException e) {
            return macDinh;
        }
        if (v < min) {
            v = min;
        }
        return v > max ? max : v;
    }
}
