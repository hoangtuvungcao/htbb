package com.htbb.mang;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Danh sach PROXY TIN CAY (sv.proxyTinCay): IP / dai CIDR (IPv4 va IPv6) ma server game
 * KHONG ap gioi han theo IP.
 *
 * VI SAO. Tren VPS moi nguoi choi vao game qua WAF / Nova agent, nen server chi thay vai IP
 * nguon (127.0.0.1 cua agent cung may, hoac dai IP cua WAF). Cac gioi han theo IP (sv.maxConnPerIp,
 * sv.connRateMax, ban sv.dosBanSec, cho 3 giay khi dang nhap, han muc dang ky theo IP...)
 * khi do gop CA SERVER vao mot "IP" va chan oan nguoi choi that. Gioi han theo tai khoan /
 * ket noi / thiet bi van giu nguyen (ChongSpam theo nhan vat, dem ngap goi tin theo ket noi,
 * han muc dang ky theo ma may, sv.reloginWaitSec theo nhan vat).
 *
 * Lop thuan (khong doc cau hinh, khong DNS): {@link #phanTich(String)} chi nhan IP dang so;
 * muc sai bi bo qua va ghi vao {@link #loi()}. Ban dang dung ({@link #hienTai()}) doi nong
 * qua {@link #dat(String)} (nguoi nghe cau hinh trung tam + doc server.ini luc khoi dong).
 */
public final class ProxyTinCay {

    /** Mac dinh: chi loopback (bot tai / proxy cung may). */
    public static final String MAC_DINH = "127.0.0.1,::1";

    private static volatile ProxyTinCay hienTai = phanTich(MAC_DINH);

    /** {dia chi da chuan hoa (4 hoac 16 byte), so bit mang}. */
    private final List<byte[]> mang;
    private final List<Integer> soBit;
    private final List<String> loi;
    private final String nguon;

    private ProxyTinCay(final List<byte[]> mang, final List<Integer> soBit, final List<String> loi, final String nguon) {
        this.mang = mang;
        this.soBit = soBit;
        this.loi = Collections.unmodifiableList(loi);
        this.nguon = nguon;
    }

    // ------------------------------------------------------------------ trang thai dung chung

    public static ProxyTinCay hienTai() {
        return hienTai;
    }

    /** Dat danh sach moi (null / rong = khong tin ai). Tra ve ban vua dat (de doc loi()). */
    public static ProxyTinCay dat(final String ds) {
        final ProxyTinCay p = phanTich(ds);
        hienTai = p;
        return p;
    }

    /** IP nay co phai proxy tin cay (theo danh sach dang dung) khong. */
    public static boolean tinCay(final String ip) {
        return hienTai.chua(ip);
    }

    // ------------------------------------------------------------------ thuan

    /** Phan tich "a.b.c.d, a.b.c.d/nn, ::1, fd00::/8". Muc sai -> bo qua + ghi loi(). */
    public static ProxyTinCay phanTich(final String ds) {
        final List<byte[]> m = new ArrayList<byte[]>();
        final List<Integer> b = new ArrayList<Integer>();
        final List<String> loi = new ArrayList<String>();
        if (ds != null) {
            for (final String tho : ds.split("[,;\\s]+")) {
                final String x = tho.trim();
                if (x.isEmpty()) {
                    continue;
                }
                final int gach = x.indexOf('/');
                final byte[] dc = diaChi(gach < 0 ? x : x.substring(0, gach));
                if (dc == null) {
                    loi.add(x);
                    continue;
                }
                int n = dc.length * 8;
                if (gach >= 0) {
                    try {
                        n = Integer.parseInt(x.substring(gach + 1).trim());
                    } catch (final NumberFormatException e) {
                        n = -1;
                    }
                    if (n < 0 || n > dc.length * 8) {
                        loi.add(x);
                        continue;
                    }
                }
                m.add(dc);
                b.add(Integer.valueOf(n));
            }
        }
        return new ProxyTinCay(m, b, loi, ds == null ? "" : ds.trim());
    }

    public boolean chua(final String ip) {
        if (ip == null || mang.isEmpty()) {
            return false;
        }
        final byte[] dc = diaChi(ip.trim());
        if (dc == null) {
            return false;
        }
        for (int i = 0; i < mang.size(); i++) {
            if (khop(mang.get(i), soBit.get(i).intValue(), dc)) {
                return true;
            }
        }
        return false;
    }

    /** Cac muc bi bo qua vi sai cu phap. */
    public List<String> loi() {
        return loi;
    }

    public int soMuc() {
        return mang.size();
    }

    @Override
    public String toString() {
        return nguon + " (" + mang.size() + " muc" + (loi.isEmpty() ? "" : ", bo qua sai: " + loi) + ")";
    }

    private static boolean khop(final byte[] mang, final int soBit, final byte[] dc) {
        if (mang.length != dc.length) {
            return false;
        }
        final int du = soBit % 8;
        final int nguyen = soBit / 8;
        for (int i = 0; i < nguyen; i++) {
            if (mang[i] != dc[i]) {
                return false;
            }
        }
        if (du == 0) {
            return true;
        }
        final int mat = (0xFF << (8 - du)) & 0xFF;
        return (mang[nguyen] & mat) == (dc[nguyen] & mat);
    }

    /**
     * IP dang so -> byte (4 cho IPv4, 16 cho IPv6; IPv4-mapped "::ffff:a.b.c.d" -> 4 byte).
     * Khong bao gio tra DNS: chuoi khong phai IP dang so -> null.
     */
    static byte[] diaChi(final String s) {
        if (s == null) {
            return null;
        }
        String x = s.trim();
        if (x.startsWith("[") && x.endsWith("]")) {
            x = x.substring(1, x.length() - 1);
        }
        final int vung = x.indexOf('%');
        if (vung >= 0) {
            x = x.substring(0, vung);
        }
        if (x.isEmpty()) {
            return null;
        }
        if (x.indexOf(':') < 0) {
            return ipv4(x);
        }
        // IPv6: chi chu hex, ':' va '.' (duoi IPv4) - InetAddress.getByName voi chuoi co ':'
        // la phan tich literal, khong tra DNS; loc truoc cho chac.
        for (int i = 0; i < x.length(); i++) {
            final char c = x.charAt(i);
            final boolean hop = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == ':' || c == '.';
            if (!hop) {
                return null;
            }
        }
        final int cham = x.lastIndexOf(':');
        if (x.indexOf('.') >= 0 && ipv4(x.substring(cham + 1)) == null) {
            return null;
        }
        try {
            return InetAddress.getByName(x).getAddress();
        } catch (final Exception e) {
            return null;
        }
    }

    private static byte[] ipv4(final String x) {
        final String[] p = x.split("\\.", -1);
        if (p.length != 4) {
            return null;
        }
        final byte[] r = new byte[4];
        for (int i = 0; i < 4; i++) {
            if (p[i].isEmpty() || p[i].length() > 3) {
                return null;
            }
            int v = 0;
            for (int j = 0; j < p[i].length(); j++) {
                final char c = p[i].charAt(j);
                if (c < '0' || c > '9') {
                    return null;
                }
                v = v * 10 + (c - '0');
            }
            if (v > 255) {
                return null;
            }
            r[i] = (byte) v;
        }
        return r;
    }
}
