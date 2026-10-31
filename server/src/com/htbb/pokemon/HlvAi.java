package com.htbb.pokemon;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * HUAN LUYEN VIEN AI (bot lap server, chu game duyet 31/10/2026) - luat THUAN, khong I/O. Lop noi game: real.HlvAiGame;
 * dich vu bot: tools/pokemon/hlv_ai/ (tien trinh rieng, noi giao thuc that nhu client).
 *
 * <pre>
 *  Nhan dien: cot account.team_user.la_bot = 1 (KHONG gui xuong client). Ten nhan vat luu nguyen; moi cho may chu GUI
 *             ten cho client hien {@link #TIEN_TO} + ten ({@link #hienThi}). Ten that chi co [0-9a-zA-Z] (CreateCharHandler)
 *             nen tien to (co '_' ':' ' ') khong trung ten nguoi choi nao; ten client gui nguoc lai (tin rieng, ket ban,
 *             tim ten) duoc {@link #boTienTo} truoc khi tra cuu.
 *  Kinh te:   bot choi tu do nhu nguoi, may chu ghim lam phat:
 *             - thu nhap xu SINH RA (quai, nhiem vu, thuong... = Char.XU_DEM) nhan {@link #K_HE_SO} % ({@link #thuNhap});
 *             - so du xu moi bot toi da {@link #K_TRAN_XU}: phan vuot bi DOT ({@link #phanDot});
 *             - tui moi bot toi da {@link #K_TRAN_VAT_PHAM} o: o thua bi huy (mon re nhat truoc);
 *             - bot rao ban (ky gui, cho Pokemon, cho sinh thai, cho trang tri) >= {@link #K_GIA_BAN} % gia trung vi
 *               ({@link #giaSan}): khong ban pha gia;
 *             - gia tri bot -> nguoi that moi ngay: toi da {@link #K_CHUYEN_NGUOI} moi nguoi that va {@link #K_CHUYEN_TONG}
 *               ca may chu ({@link #conDuocChuyen}); moi lan chuyen bot <-> nguoi ghi tob_hlv_ai_chuyen.
 *  Tien that: bot khong nap, khong mua goi, khong chuyen / nhan luong, khong nhap giftcode (real.HlvAiGame.chanTienThat).
 * </pre>
 * Kiem thu: test/KiemHlvAi.
 */
public final class HlvAi {

    public static final String TIEN_TO = "bot_ai: ";

    /** Khoa cau_hinh nhom hlv_ai (chinh tren web, ap dung ngay). */
    public static final String K_HE_SO = "hlv_ai.he_so_thu_nhap";
    public static final String K_TRAN_XU = "hlv_ai.tran_xu";
    public static final String K_TRAN_VAT_PHAM = "hlv_ai.tran_vat_pham";
    public static final String K_GIA_BAN = "hlv_ai.gia_ban_toi_thieu";
    public static final String K_CHUYEN_NGUOI = "hlv_ai.chuyen_moi_nguoi_ngay";
    public static final String K_CHUYEN_TONG = "hlv_ai.chuyen_tong_ngay";

    public static final int HE_SO = 60;
    public static final long TRAN_XU = 2000000L;
    public static final int TRAN_VAT_PHAM = 60;
    public static final int GIA_BAN = 100;
    public static final long CHUYEN_NGUOI = 50000L;
    public static final long CHUYEN_TONG = 500000L;

    /** Lan chuyen kieu doi thuoc / doi Pokemon (khong co xu): uoc gia tri moi lan de van tinh vao han muc. */
    public static final long GIA_TRI_KHONG_XU = 5000L;
    /** Can it nhat bao nhieu lan ban de tin gia trung vi; it hon thi dung gia tham chieu (NPC / khung gia). */
    public static final int MAU_TOI_THIEU = 3;

    /** Kieu chuyen (giong ChongClone.KIEU_*) khong mang xu -> dung {@link #GIA_TRI_KHONG_XU}. */
    private static final Set<String> KIEU_KHONG_XU = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "giao_dich", "trao_doi_pokemon")));

    private HlvAi() {
    }

    // ------------------------------------------------------------------ ten hien thi

    /** Ten client thay cho mot nhan vat: bot -> "bot_ai: ten", nguoi -> nguyen ten. */
    public static String hienThi(final String ten, final boolean laBot) {
        if (ten == null || !laBot || ten.startsWith(TIEN_TO)) {
            return ten;
        }
        return TIEN_TO + ten;
    }

    /** Bo tien to (ten client gui nguoc lai khi tra loi tin rieng / ket ban / tim ten). */
    public static String boTienTo(final String ten) {
        if (ten == null) {
            return null;
        }
        String t = ten.trim();
        // client co the cat mat dau cach cuoi tien to hoac doi hoa thuong
        if (t.regionMatches(true, 0, TIEN_TO, 0, TIEN_TO.length())) {
            return t.substring(TIEN_TO.length()).trim();
        }
        final String gon = TIEN_TO.trim();
        if (t.regionMatches(true, 0, gon, 0, gon.length())) {
            return t.substring(gon.length()).trim();
        }
        return t;
    }

    // ------------------------------------------------------------------ kinh te

    /** He so phan tram kep 0..100 (bot khong bao gio duoc nhieu hon nguoi that). */
    public static int kepHeSo(final long heSo) {
        return (int) Math.max(0L, Math.min(100L, heSo));
    }

    /** Xu bot that su nhan tu mot nguon SINH RA {@code xu} voi he so {@code heSo} %. */
    public static long thuNhap(final long xu, final long heSo) {
        if (xu <= 0L) {
            return xu;
        }
        return xu / 100L * kepHeSo(heSo) + xu % 100L * kepHeSo(heSo) / 100L;
    }

    /** Phan xu vuot tran bi dot (0 neu chua vuot). {@code tran} < 0 = khong tran. */
    public static long phanDot(final long soDu, final long tran) {
        if (tran < 0L || soDu <= tran) {
            return 0L;
        }
        return soDu - tran;
    }

    /** Trung vi (mang khong rong); chan = trung binh hai so giua (lam tron xuong). -1 neu rong. */
    public static long trungVi(final long[] gia, final int n) {
        if (gia == null || n <= 0) {
            return -1L;
        }
        final long[] a = Arrays.copyOf(gia, n);
        Arrays.sort(a);
        if ((n & 1) == 1) {
            return a[n / 2];
        }
        return a[n / 2 - 1] / 2L + a[n / 2] / 2L + (a[n / 2 - 1] % 2L + a[n / 2] % 2L) / 2L;
    }

    /**
     * Gia san cho bot rao ban (moi don vi): {@code phanTram} % cua gia trung vi khi du mau ({@link #MAU_TOI_THIEU}),
     * khong thi cua gia tham chieu. Khong bao gio duoi gia tham chieu (chong ban pha gia).
     */
    public static long giaSan(final long trungVi, final int soMau, final long thamChieu, final long phanTram) {
        final long pt = Math.max(0L, Math.min(1000L, phanTram));
        final long goc = soMau >= MAU_TOI_THIEU && trungVi > 0L ? trungVi : Math.max(0L, thamChieu);
        final long san = goc / 100L * pt + goc % 100L * pt / 100L;
        return Math.max(san, Math.max(0L, thamChieu));
    }

    /** Gia tri tinh vao han muc cho mot lan chuyen kieu {@code kieu} mang {@code xu}. */
    public static long giaTri(final String kieu, final long xu) {
        // doi thuoc / doi Pokemon: tham so xu o day la PHI (khong phai gia tri trao) -> uoc gia tri co dinh
        if (kieu != null && KIEU_KHONG_XU.contains(kieu)) {
            return GIA_TRI_KHONG_XU;
        }
        return Math.max(0L, xu);
    }

    /**
     * Con duoc chuyen them {@code them} (gia tri bot -> nguoi that) khong: nguoi nhan da nhan {@code daNguoi} hom nay,
     * ca may chu da {@code daTong}. Tran < 0 = khong gioi han; tran 0 = cam.
     */
    public static boolean conDuocChuyen(final long daNguoi, final long daTong, final long them, final long tranNguoi,
            final long tranTong) {
        if (them <= 0L) {
            return true;
        }
        if (tranNguoi >= 0L && daNguoi + them > tranNguoi) {
            return false;
        }
        return tranTong < 0L || daTong + them <= tranTong;
    }

    /**
     * So o tui phai huy de con {@code tran} o (0 neu chua vuot). Chi tinh o KHONG dang mac (do mac rang buoc khi mac).
     */
    public static int soOHuy(final int soO, final int tran) {
        if (tran < 0 || soO <= tran) {
            return 0;
        }
        return soO - tran;
    }

    /** Ngay (yyyyMMdd) cua mot moc thoi gian theo lich may chu - khoa bo dem han muc ngay. */
    public static int ngay(final java.util.Calendar c) {
        return c.get(java.util.Calendar.YEAR) * 10000 + (c.get(java.util.Calendar.MONTH) + 1) * 100
                + c.get(java.util.Calendar.DAY_OF_MONTH);
    }
}
