package com.htbb.pokemon;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * Doi luong (docs/thiet-ke-game.md 9.1 / 9.5) - LUAT THUAN, khong CSDL, khong Char (test: KiemDoiLuong).
 *
 * Ba duong, CHI di tu LUONG THUONG (luong mo, Char.luong). Luong khoa KHONG BAO GIO la dau vao cua duong nao, va khong
 * duong nao ra luong (luong khoa / xu khong doi nguoc ra luong qua he thong):
 * <ol>
 *   <li>Luong -> luong khoa: 1 luong = {@code doi_luong.ti_le} (mac dinh 1) luong khoa. Mot chieu, khong tran ngay
 *       (chu game 31/10/2026). ti_le = 0 -> tat.</li>
 *   <li>Luong -> xu (he thong, IN XU): MOT ti gia chung toan may chu moi ngay:
 *       1 luong = round({@code doi_luong_xu.phan_tram_gio}% x thu nhap 1 gio o CAP TRUNG VI cua nhan vat that (khong bot)
 *       hoat dong 7 ngay), tinh luc 00:00 (gio Viet Nam) va dong bang ca ngay (bang tob_ti_gia_luong_xu). Tran
 *       {@code doi_luong_xu.tran_ngay} (5.000) luong / tai khoan / ngay. phan_tram_gio = 0 hoac tran = 0 -> tat.</li>
 *   <li>San doi luong <-> xu giua nguoi choi: lenh ban (ky quy luong thuong) / lenh mua (ky quy xu + phi), khop theo gia
 *       tot nhat roi som nhat, gia khop = gia lenh dang cho (maker). Khong sinh xu: xu nguoi mua -> nguoi ban; phi
 *       {@code san_luong.phi_phan_tram}% (mac dinh 5) tinh tren phan xu nguoi mua tra, BI DOT. Tran
 *       {@code san_luong.tran_ngay} (50.000) luong dat lenh / tai khoan / ngay. Gia nam trong
 *       [{@link #SAN_GIA_DUOI_PHAN_TRAM}%, {@link #SAN_GIA_TREN_PHAN_TRAM}%] ti gia he thong hom nay (chan rua xu qua
 *       clone bang gia ao). Lenh het han sau {@link #SAN_HAN_GIO} gio.</li>
 * </ol>
 */
public final class DoiLuong {

    public static final String K_TI_LE_KHOA = "doi_luong.ti_le";
    public static final String K_XU_PHAN_TRAM_GIO = "doi_luong_xu.phan_tram_gio";
    public static final String K_XU_TRAN_NGAY = "doi_luong_xu.tran_ngay";
    public static final String K_SAN_PHI = "san_luong.phi_phan_tram";
    public static final String K_SAN_TRAN_NGAY = "san_luong.tran_ngay";

    public static final int TI_LE_KHOA = 1;
    public static final int XU_PHAN_TRAM_GIO = 1;
    public static final int XU_TRAN_NGAY = 5000;
    public static final int SAN_PHI = 5;
    public static final int SAN_TRAN_NGAY = 50000;

    /** Loai dem tran ngay trong tob_doi_tien_ngay. */
    public static final String TRAN_XU = "doi_luong_xu";
    public static final String TRAN_SAN = "san_luong";

    /** Ly do ghi nhat ky / thong ke chi tieu (tob_log_use_luong). */
    public static final String LY_DO_KHOA = "doi_luong_khoa";
    public static final String LY_DO_XU = "doi_luong_xu";
    public static final String LY_DO_SAN_PHI = "san_luong_phi";

    public static final int SAN_GIA_DUOI_PHAN_TRAM = 50;
    public static final int SAN_GIA_TREN_PHAN_TRAM = 500;
    public static final int SAN_HAN_GIO = 24;
    /** Lenh dang mo toi da moi nhan vat. */
    public static final int SAN_LENH_MO_TOI_DA = 10;
    /** Mot lenh toi da bay nhieu luong (tran int + gioi han hop ly cho mot dong). */
    public static final int SAN_LENH_TOI_DA_LUONG = 1000000;

    public static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");

    private DoiLuong() {
    }

    // ------------------------------------------------------------------ ngay

    /** Ngay (gio Viet Nam) cua moc {@code ms}: khoa ngay cho tran va ti gia (web dung cung mui gio). */
    public static LocalDate ngay(final long ms) {
        return Instant.ofEpochMilli(ms).atZone(MUI_GIO).toLocalDate();
    }

    // ------------------------------------------------------------------ 1. luong -> luong khoa

    /** So luong khoa nhan duoc khi doi {@code soLuong} luong; -1 neu khong hop le / tat / tran so. */
    public static long luongKhoaNhan(final long soLuong, final long tiLe) {
        if (soLuong <= 0L || tiLe <= 0L) {
            return -1L;
        }
        final long ra = soLuong * tiLe;
        return ra / tiLe != soLuong || ra > Integer.MAX_VALUE ? -1L : ra;
    }

    // ------------------------------------------------------------------ 2. luong -> xu (he thong)

    /** Cap trung vi (cung quy uoc ChiSoKinhTe: phan tu s[n/2] sau khi sap xep); 0 nguoi -> 1. */
    public static int capTrungVi(final int[] cap) {
        if (cap == null || cap.length == 0) {
            return 1;
        }
        final int[] s = cap.clone();
        Arrays.sort(s);
        return Math.max(1, s[s.length / 2]);
    }

    /** Ti gia (xu / 1 luong) tu thu nhap 1 gio tham chieu: round(phanTram% x xuMoiGio), toi thieu 1; phanTram <= 0 -> 0 (tat). */
    public static long tiGia(final long xuMoiGio, final long phanTram) {
        if (phanTram <= 0L || xuMoiGio <= 0L) {
            return 0L;
        }
        return Math.max(1L, (xuMoiGio * phanTram + 50L) / 100L);
    }

    /** Xu nhan duoc khi doi {@code soLuong} luong o ti gia {@code tiGia}; -1 neu khong hop le. */
    public static long xuNhan(final long soLuong, final long tiGia) {
        if (soLuong <= 0L || tiGia <= 0L || soLuong > Long.MAX_VALUE / tiGia) {
            return -1L;
        }
        return soLuong * tiGia;
    }

    /** Con doi duoc bao nhieu hom nay (khong am). */
    public static long conLai(final long tran, final long daDung) {
        return Math.max(0L, tran - Math.max(0L, daDung));
    }

    // ------------------------------------------------------------------ 3. san

    /** Phi (xu, BI DOT) cho 1 luong khop o gia {@code gia}: ceil(gia x phi%). Tinh theo tung luong nen ky quy luon du. */
    public static long phiMoiLuong(final long gia, final long phiPhanTram) {
        if (gia <= 0L || phiPhanTram <= 0L) {
            return 0L;
        }
        return (gia * phiPhanTram + 99L) / 100L;
    }

    /** Xu nguoi mua phai ky quy cho lenh mua {@code soLuong} luong o gia toi da {@code gia} (gom ca phi). -1 neu tran. */
    public static long kyQuyMua(final long soLuong, final long gia, final long phiPhanTram) {
        final long motDon = gia + phiMoiLuong(gia, phiPhanTram);
        if (soLuong <= 0L || gia <= 0L || soLuong > Long.MAX_VALUE / motDon) {
            return -1L;
        }
        return soLuong * motDon;
    }

    /** Khung gia hop le {min, max} (xu / luong) quanh ti gia he thong hom nay. tiGia <= 0 -> null (san dong). */
    public static long[] khungGia(final long tiGia) {
        if (tiGia <= 0L) {
            return null;
        }
        final long duoi = Math.max(1L, tiGia * SAN_GIA_DUOI_PHAN_TRAM / 100L);
        final long tren = Math.max(duoi, tiGia * SAN_GIA_TREN_PHAN_TRAM / 100L);
        return new long[]{duoi, tren};
    }

    /** Lenh moi khop duoc voi lenh dang cho? Ban (gia ban) khop mua co gia mua >= gia ban. */
    public static boolean khopDuoc(final boolean moiLaBan, final long giaMoi, final long giaCho) {
        return moiLaBan ? giaCho >= giaMoi : giaCho <= giaMoi;
    }

    /** Ket qua khop {@code soLuong} luong o gia {@code gia}: {xu nguoi ban nhan, phi dot, xu tru khoi ky quy nguoi mua}. */
    public static long[] tienKhop(final long soLuong, final long gia, final long phiPhanTram) {
        final long xuBan = soLuong * gia;
        final long phi = soLuong * phiMoiLuong(gia, phiPhanTram);
        return new long[]{xuBan, phi, xuBan + phi};
    }

    /** Gia trung binh (lam tron) cua cac lan khop: tong xu / tong luong; khong co -> 0. */
    public static long giaTrungBinh(final long tongXu, final long tongLuong) {
        return tongLuong <= 0L ? 0L : (tongXu + tongLuong / 2L) / tongLuong;
    }

    /** Ly do khong cho dat lenh (null = hop le). */
    public static String loiLenh(final long soLuong, final long gia, final long tiGia, final long conTran, final int soLenhMo) {
        final long[] k = khungGia(tiGia);
        if (k == null) {
            return "Sàn chưa mở (chưa có tỉ giá hôm nay).";
        }
        if (soLuong <= 0L || soLuong > SAN_LENH_TOI_DA_LUONG) {
            return "Số lượng phải từ 1 đến " + SAN_LENH_TOI_DA_LUONG + ".";
        }
        if (gia < k[0] || gia > k[1]) {
            return "Giá phải từ " + k[0] + " đến " + k[1] + " xu / 1 lượng (50% – 500% tỉ giá hệ thống hôm nay).";
        }
        if (soLenhMo >= SAN_LENH_MO_TOI_DA) {
            return "Bạn đang có " + soLenhMo + " lệnh mở (tối đa " + SAN_LENH_MO_TOI_DA + "). Huỷ bớt hoặc chờ khớp.";
        }
        if (soLuong > conTran) {
            return "Hôm nay tài khoản chỉ còn đặt được " + conTran + " lượng trên sàn.";
        }
        return null;
    }
}
