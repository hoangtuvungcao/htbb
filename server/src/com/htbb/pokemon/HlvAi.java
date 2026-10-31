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
 *             ten cho client hien tien to tuy chon (cau hinh hlv_ai.tien_to_ten, mac dinh rong) + ten ({@link #hienThi}). Ten that chi co [0-9a-zA-Z] (CreateCharHandler)
 *             nen tien to (neu dat, co '_' ':' ' ') khong trung ten nguoi choi nao; ten client gui nguoc lai (tin rieng, ket ban,
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


    /** Khoa cau_hinh nhom hlv_ai (chinh tren web, ap dung ngay). */
    public static final String K_HE_SO = "hlv_ai.he_so_thu_nhap";
    public static final String K_TRAN_XU = "hlv_ai.tran_xu";
    public static final String K_TRAN_VAT_PHAM = "hlv_ai.tran_vat_pham";
    public static final String K_GIA_BAN = "hlv_ai.gia_ban_toi_thieu";
    public static final String K_CHUYEN_NGUOI = "hlv_ai.chuyen_moi_nguoi_ngay";
    public static final String K_CHUYEN_TONG = "hlv_ai.chuyen_tong_ngay";
    public static final String K_DOI_BOT = "hlv_ai.doi_bot_toi_da";
    /** So doi (bang) toi da do bot lam chu - bot lap doi khong mat 100 trieu xu / huy hieu. */
    public static final int DOI_BOT = 3;

    public static final int HE_SO = 60;
    public static final long TRAN_XU = 300000L;
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

    // ------------------------------------------------------------------ ten hien thi (tien to cau hinh hlv_ai.tien_to_ten)
    /** Khoa cau hinh tien to ten (chinh tren /admin/hlv-ai, ap dung ngay). */
    public static final String K_TIEN_TO = "hlv_ai.tien_to_ten";
    /** Mac dinh: "" -> ten hien thi nhu nguoi choi binh thuong, khong tien to. */
    public static final String TIEN_TO_MAC_DINH = "";
    public static final int TIEN_TO_TOI_DA = 12;
    /**
     * Mau tien to (danh muc + web + may chu dung chung): 0-12 ky tu, khong ':' / ky tu dieu khien (chu chat kieu cu tach ten
     * o ": "). De trong = ten nhu nguoi choi that, khong can tien to bot/ai.
     */
    public static final String MAU_TIEN_TO = "^[^:\\u0000-\\u001F]{0,12}$";
    public static final String LOI_TIEN_TO = "Tiền tố tối đa 12 ký tự và không dùng dấu \":\"";

    private static volatile String tienTo = TIEN_TO_MAC_DINH;
    /** Moi tien to da dung tu luc bat may (ten client con giu tu truoc khi doi). */
    private static final java.util.Set<String> DA_DUNG = new java.util.concurrent.CopyOnWriteArraySet<String>(
            Arrays.asList("ai ", "bot_ai: "));
    /** Ten (khong tien to) co phai bot dang biet khong - real.HlvAiGame dat; null = coi moi ten la bot (kiem thu). */
    private static volatile java.util.function.Predicate<String> laTenBot = null;

    public static boolean tienToHopLe(final String cauHinh) {
        return cauHinh != null && cauHinh.matches(MAU_TIEN_TO);
    }

    /** Tien to that dung: gia tri cau hinh hop le giu NGUYEN (co the dinh lien ten), sai luat = mac dinh "". */
    public static String chuan(final String cauHinh) {
        return tienToHopLe(cauHinh) ? cauHinh : TIEN_TO_MAC_DINH;
    }

    /** Dat tien to moi (nhip 15 giay cua real.HlvAiGame). Tien to cu van duoc boTienTo nhan ra. */
    public static void datTienTo(final String cauHinh) {
        final String t = chuan(cauHinh);
        if (!t.isEmpty()) {
            DA_DUNG.add(t);
        }
        tienTo = t;
    }

    public static void datLaTenBot(final java.util.function.Predicate<String> p) {
        laTenBot = p;
    }

    public static String tienTo() {
        return tienTo;
    }

    /** Ten client thay cho mot nhan vat: bot -> tien to + ten (neu co tien to), nguoi -> nguyen ten. */
    public static String hienThi(final String ten, final boolean laBot) {
        if (ten == null || !laBot) {
            return ten;
        }
        final String t = tienTo;
        if (t == null || t.isEmpty()) {
            return ten;
        }
        return ten.startsWith(t) ? ten : t + ten;
    }

    /**
     * Bo tien to (hien tai hoac da dung) khoi ten client gui nguoc lai (tin rieng, ket ban, tim ten). Tien to co the dinh
     * lien ten ("ai" + "minh" = "aiminh" trung ten nguoi choi "aiminh") nen CHI cat khi phan con lai la ten mot bot.
     */
    public static String boTienTo(final String ten) {
        if (ten == null) {
            return null;
        }
        final String t = ten.trim();
        final java.util.function.Predicate<String> la = laTenBot;
        for (final String tt : DA_DUNG) {
            final String g = tt.trim();
            for (final String x : new String[]{tt, g}) {
                if (!x.isEmpty() && t.length() > x.length() && t.regionMatches(true, 0, x, 0, x.length())) {
                    final String con = t.substring(x.length()).trim();
                    if (la == null || la.test(con)) {
                        return con;
                    }
                }
            }
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
