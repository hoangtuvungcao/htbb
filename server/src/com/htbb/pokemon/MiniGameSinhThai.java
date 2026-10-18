package com.htbb.pokemon;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Tro choi giai tri (docs/pokemon/HE_SINH_THAI.md muc 5). Lop THUAN.
 *
 *   - "Ai la Pokemon?": cau do theo bong (he, so chu, chu cai dau) tren kenh the gioi, tra loi bang chat.
 *   - Dua Pokemon: chuoi diem moc tren 1 ban do duong; di qua theo thu tu, xep hang theo thoi gian.
 *   - Giai dau tuan: cay loai truc tiep (bye cho so le), hat giong theo diem.
 */
public final class MiniGameSinhThai {

    private MiniGameSinhThai() {
    }

    // ------------------------------------------------------------------ do vui

    /** Bo dau, chu thuong, chi giu a-z0-9 ("Mr. Mime" -> "mrmime", "Đá" -> "da"). */
    public static String chuanHoa(final String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim().toLowerCase(java.util.Locale.ROOT).replace('đ', 'd');
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return t.replaceAll("[^a-z0-9]", "");
    }

    public static boolean dung(final String traLoi, final String dapAn) {
        final String a = chuanHoa(traLoi);
        return !a.isEmpty() && a.equals(chuanHoa(dapAn));
    }

    /** Goi y bong: he, so chu cai, chu dau. */
    public static String goiY(final String ten, final int he1, final int he2) {
        final String c = chuanHoa(ten);
        final StringBuilder b = new StringBuilder();
        b.append("Hệ ").append(HePokemon.ten(he1));
        if (he2 >= 0) {
            b.append('/').append(HePokemon.ten(he2));
        }
        b.append(", tên ").append(c.length()).append(" chữ cái, bắt đầu bằng '")
                .append(c.isEmpty() ? '?' : Character.toUpperCase(c.charAt(0))).append("'");
        return b.toString();
    }

    /** Chon cau hoi tat dinh theo (ngay, luot) tu danh sach ung vien. */
    public static int chonCauHoi(final int ngay, final int luot, final int soUngVien) {
        if (soUngVien <= 0) {
            return -1;
        }
        return (int) Long.remainderUnsigned(SinhThaiTheGioi.bam(ngay * 31L + luot * 7L + 11L), soUngVien);
    }

    // ------------------------------------------------------------------ dua

    /** Ban kinh (o) quanh moc duoc tinh la da qua. */
    public static final int BAN_KINH_MOC_O = 3;

    /** Diem moc ke tiep sau khi o vi tri (xO, yO): tang neu toi moc dang cho. */
    public static int capNhatMoc(final int[][] moc, final int mocDangCho, final int xO, final int yO) {
        if (mocDangCho < 0 || mocDangCho >= moc.length) {
            return mocDangCho;
        }
        final int[] m = moc[mocDangCho];
        if (Math.abs(m[0] - xO) <= BAN_KINH_MOC_O && Math.abs(m[1] - yO) <= BAN_KINH_MOC_O) {
            return mocDangCho + 1;
        }
        return mocDangCho;
    }

    /**
     * Chon {@code n} moc tren cac o di duoc cua ban do, trai -> phai (chia chieu rong thanh n khuc,
     * moi khuc lay o di duoc gan duong giua nhat theo hat giong). {@code diDuoc[y*w+x]}.
     */
    public static int[][] chonMocDua(final boolean[] diDuoc, final int w, final int h, final int n, final long hat) {
        final List<int[]> ra = new ArrayList<int[]>();
        if (diDuoc == null || w <= 0 || h <= 0 || n <= 0) {
            return new int[0][];
        }
        for (int k = 0; k < n; k++) {
            final int x0 = 1 + (w - 2) * k / n;
            final int x1 = Math.max(x0 + 1, 1 + (w - 2) * (k + 1) / n);
            final int lech = (int) Long.remainderUnsigned(SinhThaiTheGioi.bam(hat + k), Math.max(1, h / 3));
            int[] tot = null;
            int diemTot = Integer.MAX_VALUE;
            for (int x = x0; x < x1 && x < w; x++) {
                for (int y = 1; y < h - 1; y++) {
                    if (diDuoc[y * w + x]) {
                        final int d = Math.abs(y - (h / 3 + lech)) + Math.abs(x - (x0 + x1) / 2);
                        if (d < diemTot) {
                            diemTot = d;
                            tot = new int[]{x, y};
                        }
                    }
                }
            }
            if (tot != null) {
                ra.add(tot);
            }
        }
        return ra.toArray(new int[0][]);
    }

    // ------------------------------------------------------------------ giai dau

    /**
     * Cap dau vong 1 cua cay loai truc tiep: hat giong 1 gap cuoi, ... So nguoi khong phai luy thua
     * 2 thi hat giong cao duoc mien (bye = -1). {@code xepHang}: chi so nguoi choi da xep theo diem.
     */
    public static int[][] capDauVong1(final int[] xepHang) {
        final int n = xepHang == null ? 0 : xepHang.length;
        if (n < 2) {
            return new int[0][];
        }
        int kich = 1;
        while (kich < n) {
            kich <<= 1;
        }
        final int[][] cap = new int[kich / 2][];
        for (int i = 0; i < kich / 2; i++) {
            final int a = i;
            final int b = kich - 1 - i;
            cap[i] = new int[]{xepHang[a], b < n ? xepHang[b] : -1};
        }
        return cap;
    }

    /** Ghep nguoi thang cua vong truoc thanh cap vong sau (giu thu tu cay). */
    public static int[][] vongSau(final int[] nguoiThang) {
        final int n = nguoiThang == null ? 0 : nguoiThang.length;
        if (n < 2) {
            return new int[0][];
        }
        final int[][] cap = new int[(n + 1) / 2][];
        for (int i = 0; i < cap.length; i++) {
            cap[i] = new int[]{nguoiThang[2 * i], 2 * i + 1 < n ? nguoiThang[2 * i + 1] : -1};
        }
        return cap;
    }
}
