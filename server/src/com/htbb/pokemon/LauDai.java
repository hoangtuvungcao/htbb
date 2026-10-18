package com.htbb.pokemon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Co {@code sv.lau_dai} (giai doan 2 - docs/pokemon/KE_HOACH_LAU_DAI_GD2.md). MOI thay doi
 * cua thiet ke dai han (THIET_KE_LAU_DAI.md) re nhanh qua {@link #bat()} - giong
 * {@link CheDoPokemon}. Tat co (mac dinh, thieu dong) = game y nhu truoc.
 *
 * So lieu goc nam o {@link ThongSoLauDai} (file sinh, khong sua tay). Lop nay chi:
 *   - giu co bat/tat (doc 1 lan luc khoi dong, TeamServer);
 *   - giu ngay khai truong ({@code sv.ngay_khai_truong}, yyyy-MM-dd) de tinh tran cap theo mua;
 *   - cho DOC DE so lieu bang bang {@code cau_hinh} (CauHinh.napLai goi
 *     {@link #apDungCauHinh} moi 10 giay). Ten khoa = cot {@code khoa} cua
 *     {@code data_kinh_te_lau_dai} (vd {@code roi.xu.phan_tram}, {@code thue.cho}). Thieu
 *     dong = gia tri mac dinh trong {@link ThongSoLauDai};
 *   - dem xu vao/ra trong ngay (Char.addXu/subXu) cho bang tob_kinh_te_ngay.
 */
public final class LauDai {

    /** Ten khoa trong server.ini. */
    public static final String KHOA = "sv.lau_dai";
    /** Ngay khai truong (yyyy-MM-dd) - ngay 0 cua tran cap theo mua. */
    public static final String KHOA_NGAY_KHAI_TRUONG = "sv.ngay_khai_truong";

    // ---- Ten khoa doc de tu cau_hinh (trung cot khoa cua data_kinh_te_lau_dai) ----
    public static final String K_ROI_XU = "roi.xu.phan_tram";
    public static final String K_ROI_LUONG = "roi.luong.phan_tram";
    public static final String K_ROI_LUONG_KHOA = "roi.luong_khoa.phan_tram";
    public static final String K_ROI_DA = "roi.da_nang_cap.phan_nghin";
    public static final String K_NANG_LUONG_PHUT = "exp.nang_luong_phut";
    public static final String K_NANG_LUONG_TICH = "exp.nang_luong_tich_phut";
    public static final String K_THUE_CHO = "thue.cho";
    public static final String K_PHI_DANG_BAN = "thue.dang_ban";
    public static final String K_THUE_CHUYEN_LUONG = "thue.chuyen_luong";
    public static final String K_SUA_DO = "tieu_hao.sua_do_phan_tram_thu_nhap";
    public static final String K_GIFTCODE_THANG = "giftcode.toi_da_thang";
    public static final String K_VE_CHUONG_TUAN = "ve_chuong.tuan";
    public static final String K_VE_CHUONG_TOI_DA = "ve_chuong.toi_da";
    /** Khoa them (khong co trong data_kinh_te_lau_dai): % thu nhap 1 gio cho dich chuyen / hoi sinh. */
    public static final String K_DICH_CHUYEN = "phi.dich_chuyen_phan_tram_gio";
    public static final String K_HOI_SINH = "phi.hoi_sinh_phan_tram_gio";
    public static final String K_BAN_NPC = "phi.ban_npc_phan_tram";
    public static final String K_TRAN_CHUYEN_XU = "thue.tran_chuyen_xu_gio";
    public static final String K_SHINY_MAU = "shiny.mau_so";
    public static final String K_SHINY_PITY = "shiny.pity";

    private static volatile boolean bat = false;
    /** Ngay khai truong, millis luc 00:00 gio may; 0 = chua dat (coi nhu hom nay la ngay 0). */
    private static volatile long khaiTruongMs = 0L;
    private static volatile Map<String, String> de = Collections.emptyMap();

    private static final AtomicLong XU_VAO = new AtomicLong();
    private static final AtomicLong XU_RA = new AtomicLong();

    private LauDai() {
    }

    /** Thiet ke dai han dang bat khong. Ham DUY NHAT moi cho re nhanh duoc dung. */
    public static boolean bat() {
        return bat;
    }

    /** Chi TeamServer (luc doc server.ini) va test duoc goi. */
    public static void dat(final boolean giaTri) {
        bat = giaTri;
    }

    /** "1" (co the co khoang trang) = bat; moi gia tri khac, ke ca null = tat. */
    public static boolean docGiaTri(final String giaTri) {
        return giaTri != null && "1".equals(giaTri.trim());
    }

    // ------------------------------------------------------------ ngay khai truong / mua

    /** Dat ngay khai truong tu chuoi yyyy-MM-dd; sai dinh dang / rong = bo dat (0). */
    public static void datNgayKhaiTruong(final String s) {
        khaiTruongMs = docNgay(s);
    }

    /** Millis 00:00 (gio may) cua ngay yyyy-MM-dd; 0 neu khong doc duoc. */
    public static long docNgay(final String s) {
        if (s == null || s.trim().isEmpty()) {
            return 0L;
        }
        try {
            final java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
            f.setLenient(false);
            return f.parse(s.trim()).getTime();
        } catch (final Exception e) {
            return 0L;
        }
    }

    public static long ngayKhaiTruongMs() {
        return khaiTruongMs;
    }

    /** So ngay tu khai truong toi {@code bayGioMs} (>= 0). Chua dat ngay = 0. */
    public static int ngayTuKhaiTruong(final long khaiTruong, final long bayGioMs) {
        if (khaiTruong <= 0L || bayGioMs <= khaiTruong) {
            return 0;
        }
        return (int) ((bayGioMs - khaiTruong) / 86400000L);
    }

    public static int ngayHienTai() {
        return ngayTuKhaiTruong(khaiTruongMs, System.currentTimeMillis());
    }

    /** Chi so mua (0 = S1) tai ngay {@code ngay}. */
    public static int mua(final int ngay) {
        int m = 0;
        for (int i = 0; i < ThongSoLauDai.TRAN_CAP_NGAY.length; i++) {
            if (ngay >= ThongSoLauDai.TRAN_CAP_NGAY[i]) {
                m = i;
            }
        }
        return m;
    }

    // ------------------------------------------------------------------ doc de cau_hinh

    /**
     * Goi tu CauHinh.napLai voi TOAN BO bang cau_hinh. Chi giu cac khoa co dau cham
     * KHONG bat dau bang "sv."/"db." (khong nham voi khoa server.ini).
     */
    public static void apDungCauHinh(final Properties p) {
        if (p == null) {
            return;
        }
        final Map<String, String> m = new HashMap<String, String>();
        for (final String k : p.stringPropertyNames()) {
            if (k.indexOf('.') > 0 && !k.startsWith("sv.") && !k.startsWith("db.")
                    && !k.startsWith("api.") && !k.startsWith("limit.")) {
                m.put(k, p.getProperty(k).trim());
            }
        }
        de = Collections.unmodifiableMap(m);
    }

    /** So nguyen doc de tu cau_hinh, sai/thieu = {@code macDinh}. */
    public static int so(final String khoa, final int macDinh) {
        final String v = de.get(khoa);
        if (v == null || v.isEmpty()) {
            return macDinh;
        }
        try {
            return Integer.parseInt(v);
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }

    // ------------------------------------------------------------------ xu vao/ra trong ngay

    public static void ghiXuVao(final long xu) {
        if (bat && xu > 0L) {
            XU_VAO.addAndGet(xu);
        }
    }

    public static void ghiXuRa(final long xu) {
        if (bat && xu > 0L) {
            XU_RA.addAndGet(xu);
        }
    }

    /** Lay roi dat lai bo dem {vao, ra} (job dem). */
    public static long[] layVaDatLaiXu() {
        return new long[]{XU_VAO.getAndSet(0L), XU_RA.getAndSet(0L)};
    }

    // ------------------------------------------------------------------ thu nhap theo cap

    /** Kep cap vao 1..CAP_TOI_DA de tra bang ThongSoLauDai. */
    public static int kepCap(final int cap) {
        return Math.max(1, Math.min(ThongSoLauDai.CAP_TOI_DA, cap));
    }

    /** Xu thu nhap 1 gio danh quai o cap {@code cap} (don vi chung "gio choi", N1). */
    public static long xuMoiGio(final int cap) {
        return ThongSoLauDai.xuMoiGio(kepCap(cap));
    }

    /** {@code phanTram}% cua thu nhap 1 gio o cap {@code cap}, toi thieu 1. */
    public static long phanTramGio(final int cap, final int phanTram) {
        return Math.max(1L, xuMoiGio(cap) * phanTram / 100L);
    }
}
