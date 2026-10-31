package com.htbb.pokemon;

/**
 * CO PK (khan PK - binh 14..18, Char.pk) - phan THUAN, kiem duoc khong CSDL. Sua loi 22/10/2026 "PK tu bat, bat roi
 * khong tat duoc":
 * <ul>
 * <li>Truoc: dung khan lan hai trong 150 giay sau khi deo bi coi la "hack khan" -&gt; ten do (isKiller) + 300 nghiep, khan
 * KHONG thao; moi lan dung sau do bi bo qua im lang vi dang co nghiep (killer &gt; 0) -&gt; PK ket toi khi het nghiep
 * (300 / 5 moi phut online = 1 gio online, ban cu -200 / ngay = 2 ngay). Hanh trang 0.9.7 cham hang thuoc la dung ngay,
 * nen cham nham hai lan la dinh.</li>
 * <li>Nay: khan chi BAT khi nguoi choi tu dung; THAO luon duoc, chi cho {@link #CHO_SAU_DANH_MS} sau don danh nguoi choi
 * cuoi (chong danh xong thao khan chay), bao so giay con lai. Ten do (nghiep) tach rieng: khong deo khan duoc khi
 * dang ten do (nhu client cu "Không thể đeo khăn khi đang phạm tội"), nhung thao thi luon duoc.</li>
 * </ul>
 */
public final class CoPk {

    private CoPk() {
    }

    /** Cho sau don danh nguoi choi cuoi moi thao khan duoc. */
    public static final long CHO_SAU_DANH_MS = 10_000L;

    /** Khan PK: binh 14 (do) .. 18 (tim). */
    public static boolean laKhan(final int loai) {
        return loai >= 14 && loai <= 18;
    }

    /** So giay (lam tron len) con phai cho moi thao khan duoc; 0 = thao ngay. */
    public static int giayCho(final long lucDanhNguoi, final long bay) {
        if (lucDanhNguoi <= 0L) {
            return 0;
        }
        final long con = lucDanhNguoi + CHO_SAU_DANH_MS - bay;
        if (con <= 0L) {
            return 0;
        }
        if (con > CHO_SAU_DANH_MS) { // dong ho lui: khong bat cho qua muc
            return (int) (CHO_SAU_DANH_MS / 1000L);
        }
        return (int) ((con + 999L) / 1000L);
    }

    /** Phut online (lam tron len) de het nghiep. */
    public static int phutHetNghiep(final int nghiep) {
        if (nghiep <= 0) {
            return 0;
        }
        return (nghiep + LuatPk.PHAI_MOI_PHUT - 1) / LuatPk.PHAI_MOI_PHUT;
    }

    public static String baoDeo(final String tenKhan) {
        return "Đã đeo " + tenKhan + ", bật PK. Dùng lại khăn để tháo (tắt PK).";
    }

    public static String baoThao() {
        return "Đã tháo khăn, tắt PK.";
    }

    public static String baoCho(final int giay) {
        return "Vừa tấn công người chơi khác: còn " + giay + " giây nữa mới tháo khăn (tắt PK) được.";
    }

    public static String baoTenDo(final int nghiep) {
        return "Đang mang tên đỏ (nghiệp " + Math.max(0, nghiep) + "): không thể đeo khăn PK. Nghiệp giảm "
                + LuatPk.PHAI_MOI_PHUT + " mỗi phút online, còn khoảng " + phutHetNghiep(nghiep) + " phút.";
    }

    public static String baoKhuVuc() {
        return "Không dùng khăn PK ở khu vực này.";
    }

    /**
     * Nghiep KHONG phai khi vua danh trung nguoi choi trong chung nay (ms): dang giao tranh thi nghiep dung, khong tu het
     * ten do / tat PK giua tran. Truoc 31/10/2026: don dau tien cho +1 nghiep, nhip phut sau phai ve 0 -> isKiller = false
     * -> moi don sau bi MapDanhNhau chan (cong kiem pvp.ha_doi_thu: 700 don ngam chi ~45 don trung roi B hoi day mau).
     */
    public static final long DUNG_PHAI_KHI_GIAO_TRANH_MS = 60_000L;

    /** Dang giao tranh PK (danh trung nguoi choi trong {@link #DUNG_PHAI_KHI_GIAO_TRANH_MS}): nghiep chua phai. */
    public static boolean dangGiaoTranh(final long lucDanhNguoi, final long bay) {
        return lucDanhNguoi > 0L && bay >= lucDanhNguoi && bay - lucDanhNguoi < DUNG_PHAI_KHI_GIAO_TRANH_MS;
    }

    /** Nghiep sau khi online them tu lucTruoc toi bay (theo dong ho that, khong theo nhip vong lap). [nghiep, luc moi]. */
    public static long[] phaiTheoGio(final int nghiep, final long lucTruoc, final long bay) {
        if (nghiep <= 0) {
            return new long[]{0L, bay};
        }
        if (lucTruoc <= 0L || bay < lucTruoc) {
            return new long[]{nghiep, bay};
        }
        final long phut = (bay - lucTruoc) / 60_000L;
        if (phut <= 0L) {
            return new long[]{nghiep, lucTruoc};
        }
        final int moi = LuatPk.phaiNghiep(nghiep, (int) Math.min(Integer.MAX_VALUE, phut));
        return new long[]{moi, lucTruoc + phut * 60_000L};
    }
}
