package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * Nhiem vu theo thiet ke dai han (THIET_KE_LAU_DAI.md muc 5, GD2 buoc 4). Lop THUAN.
 *
 *   - ve chuong: 2 ve / tuan (reset thu Hai 05:00), tich toi da 4, bat dau 4 ve;
 *   - loc nhiem vu phu theo cap truoc khi gui (danh sach ghi so luong bang 1 byte <= 127):
 *     chi gui nhiem vu co lv <= cap < lv + 10, toi da 20;
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

    /** Nhiem vu (cap toi thieu {@code lv}) co hop dai cap nguoi choi khong. */
    public static boolean hopCap(final int lv, final int cap) {
        final int d = cap - lv;
        return d >= 0 && d < DAI_CAP;
    }

    /**
     * Chon nhiem vu phu de gui: giu thu tu, chi lay hopCap, toi da {@code toiDa}.
     * @return chi so (trong mang lv) duoc chon
     */
    public static List<Integer> locPhu(final int[] lv, final int cap, final int toiDa) {
        final List<Integer> kq = new ArrayList<Integer>();
        for (int i = 0; i < lv.length && kq.size() < toiDa; i++) {
            if (hopCap(lv[i], cap)) {
                kq.add(Integer.valueOf(i));
            }
        }
        return kq;
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
