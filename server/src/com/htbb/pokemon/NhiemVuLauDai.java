package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * Nhiem vu theo thiet ke dai han (docs/thiet-ke-game.md, GD2 buoc 4). Lop THUAN.
 *
 *   - ve chuong: 2 ve / tuan (reset thu Hai 05:00), tich toi da 4, bat dau 4 ve;
 *   - loc nhiem vu phu theo cap truoc khi gui (danh sach ghi so luong bang 1 byte <= 127): nhiem vu co lv <= cap + 10
 *     (KHONG chan duoi tu 30/10/2026 - truoc day lv <= cap < lv + 10: len cap nhanh qua 10 cap la mat han nhiem vu phu),
 *     xep gan cap nguoi choi nhat truoc, cung do lech thi thap truoc, roi theo thu tu nhiem vu (thi tran); toi da 20;
 *   - bang tuan: 2 nhiem vu / tuan theo bac cap.
 */
public final class NhiemVuLauDai {

    /** So nhiem vu phu toi da gui xuong client cung luc. */
    public static final int PHU_TOI_DA = 20;
    /** Gioi han cung cua goi (writeByte(size)). */
    public static final int GOI_TOI_DA = 127;
    public static final int DAI_CAP = 10;

    /** Moc tuan: thu Hai 05:00 (gio may). */
    public static final int GIO_DOI_TUAN = 5;

    private NhiemVuLauDai() {
    }

    /** Chi so tuan (tang dan) - doi luc thu Hai 05:00 gio may. */
    public static int tuan(final long ms) {
        // lui 5 gio roi tinh ngay dia phuong; 1970-01-05 (ngay thu 4) la thu Hai
        final long t = ms - GIO_DOI_TUAN * 3600000L;
        final long ngay = Math.floorDiv(t + java.util.TimeZone.getDefault().getOffset(t), 86400000L);
        return (int) Math.floorDiv(ngay - 4L, 7L);
    }

    /**
     * Cap nhat ve chuong khi sang tuan moi: +2 moi tuan da qua, tran 4.
     * @return {ve moi, tuan moi}
     */
    public static int[] capNhatVe(final int ve, final int tuanCuoi, final int tuanNay) {
        if (tuanCuoi <= 0) {
            return new int[]{Math.max(ve, 0), tuanNay};
        }
        if (tuanNay <= tuanCuoi) {
            return new int[]{ve, tuanCuoi};
        }
        final long them = (long) (tuanNay - tuanCuoi) * ThongSoLauDai.VE_CHUONG_TUAN;
        final int moi = (int) Math.min(ThongSoLauDai.VE_CHUONG_TOI_DA, Math.max(0, ve) + them);
        return new int[]{moi, tuanNay};
    }

    /**
     * Nhiem vu phu (cap {@code lv}) co moi nguoi choi cap {@code cap} khong: lv <= cap + DAI_CAP, khong chan duoi (chu game
     * 30/10/2026: nguoi len cap nhanh khong mat vinh vien nhiem vu phu cap thap; thuong giu nguyen o cap cua nhiem vu).
     */
    public static boolean hopCap(final int lv, final int cap) {
        return lv <= cap + DAI_CAP;
    }

    /** Thu tu hien: gan cap nguoi choi nhat truoc; cung do lech -> nhiem vu thap hon truoc; roi thu tu goc (thi tran). */
    public static List<Integer> xepGanCap(final int[] lv, final int cap, final List<Integer> chiSo) {
        final List<Integer> kq = new ArrayList<Integer>(chiSo);
        java.util.Collections.sort(kq, new java.util.Comparator<Integer>() {
            @Override
            public int compare(final Integer a, final Integer b) {
                final int da = Math.abs(lv[a.intValue()] - cap), db = Math.abs(lv[b.intValue()] - cap);
                if (da != db) {
                    return da < db ? -1 : 1;
                }
                if (lv[a.intValue()] != lv[b.intValue()]) {
                    return lv[a.intValue()] < lv[b.intValue()] ? -1 : 1;
                }
                return a.compareTo(b);
            }
        });
        return kq;
    }

    /**
     * Chon nhiem vu phu de gui (goi -64, client cu khong co trang): moi muc hopCap, xep gan cap nhat truoc, toi da
     * {@code toiDa} (xong / bo bot muc gan thi muc xa hon hien len). Day du + trang: So tay -> Nhan nhiem vu phu.
     * @return chi so (trong mang lv) duoc chon
     */
    public static List<Integer> locPhu(final int[] lv, final int cap, final int toiDa) {
        final List<Integer> hop = new ArrayList<Integer>();
        for (int i = 0; i < lv.length; i++) {
            if (hopCap(lv[i], cap)) {
                hop.add(Integer.valueOf(i));
            }
        }
        final List<Integer> kq = xepGanCap(lv, cap, hop);
        return kq.size() > toiDa ? new ArrayList<Integer>(kq.subList(0, toiDa)) : kq;
    }

    /**
     * Nhiem vu chinh co nhan duoc khong.
     * @return null neu duoc, nguoc lai ly do (de bao cho nguoi choi)
     */
    public static String kiemChinh(final int lvYeuCau, final int cap, final int ngayMo, final int ngayHienTai,
            final boolean canVe, final int ve) {
        if (cap < lvYeuCau) {
            return "Cần đạt cấp " + lvYeuCau + " để nhận nhiệm vụ này.";
        }
        if (ngayMo > ngayHienTai) {
            return "Chương này mở sau " + (ngayMo - ngayHienTai) + " ngày nữa.";
        }
        if (canVe && ve <= 0) {
            return "Bạn đã hết vé chương tuần này (2 vé/tuần, làm mới thứ Hai 05:00).";
        }
        return null;
    }

    /** Bac nhiem vu tuan/ngay theo cap (1..4): 1-29 -> 1, 30-54 -> 2, 55-79 -> 3, 80+ -> 4. */
    public static int bacCap(final int cap) {
        if (cap < 30) {
            return 1;
        }
        if (cap < 55) {
            return 2;
        }
        if (cap < 80) {
            return 3;
        }
        return 4;
    }

    /**
     * Chi so nhiem vu chinh dau tien (theo thu tu chuoi) phu hop cap {@code cap} cho
     * nhan vat CU khi chuyen sang bang data_quest_moi: bo qua cac chuong cap thap hon cap
     * hien tai, vao dau chuong co lv lon nhat <= cap. {@code batDauChuong[i]} = true neu
     * nhiem vu i la buoc 1 cua chuong (hoac buoc dau huong dan).
     */
    public static int chiSoChinhChoCap(final int[] lv, final boolean[] batDauChuong, final int cap) {
        int chon = 0;
        for (int i = 0; i < lv.length; i++) {
            if (batDauChuong[i] && lv[i] <= cap) {
                chon = i;
            }
        }
        return chon;
    }
}
