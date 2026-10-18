package com.htbb.pokemon;

/**
 * Cho sinh thai (docs/thiet-ke-game.md). Lop THUAN: chi tinh tien, khong cham CSDL.
 *
 *   - Rao ban theo LOAI (ca / berry / khoang / hoa thach / mon an / thuoc / trung / trang tri).
 *   - Phi dang 1% (khong hoan) + thue 6% khi ban (dot) - khop thue cho cua THIET_KE_LAU_DAI 2.4.
 *   - Gia chi duoc chon trong bang muc (x1 / x1,5 / x2 / x3 gia tham chieu) - khong nhap so: khong
 *     can o nhap chu cua client, va chan rua tien qua gia ao (tran x5).
 *   - NPC mua lai = san 40% gia tham chieu, tran 0,1 gio thu nhap / ngay / nguoi -> nguon xu nho,
 *     co tran, khong bao gio cao hon ho hut nguyen lieu (moi cau, xeng, bep) cua cung hoat dong.
 *   - Don dat hang: nguoi mua ky quy truoc (gia x so + phi dang 1%), nguoi ban giao hang qua menu,
 *     nhan tien tru thue. Huy don: hoan ky quy phan chua giao (phi dang khong hoan).
 */
public final class ChoSinhThai {

    private ChoSinhThai() {
    }

    /** He so gia rao (phan tram gia tham chieu) cho menu chon gia. */
    public static final int[] MUC_GIA_PHAN_TRAM = {100, 150, 200, 300};
    /** So luong rao / dat mot lan cho menu. */
    public static final int[] MUC_SO_LUONG = {1, 5, 10, 50};

    public static long phiDang(final long tongGia) {
        return Math.max(1L, tongGia * HeSinhThai.PHI_DANG_PHAN_TRAM / 100L);
    }

    public static long thue(final long tongGia) {
        return tongGia * HeSinhThai.THUE_CHO_PHAN_TRAM / 100L;
    }

    /** Nguoi ban nhan khi ban duoc {@code tongGia}. */
    public static long nguoiBanNhan(final long tongGia) {
        return Math.max(0L, tongGia - thue(tongGia));
    }

    /** Gia 1 cai theo muc. */
    public static long giaTheoMuc(final int ma, final int bac, final int muc) {
        final int m = Math.max(0, Math.min(MUC_GIA_PHAN_TRAM.length - 1, muc));
        return Math.max(1L, VatLieuSinhThai.giaXu(ma, bac) * MUC_GIA_PHAN_TRAM[m] / 100L);
    }

    /** Gia hop le: >= 1 va <= GIA_TOI_DA_LAN x tham chieu. */
    public static boolean giaHopLe(final int ma, final int bac, final long giaMoi) {
        return giaMoi >= 1 && giaMoi <= VatLieuSinhThai.giaXu(ma, bac) * HeSinhThai.GIA_TOI_DA_LAN;
    }

    /** Tran xu NPC mua lai / ngay o cap nguoi choi. */
    public static long tranBanNpcNgay(final int capNguoiChoi) {
        final int c = Math.max(1, Math.min(ThongSoLauDai.CAP_TOI_DA, capNguoiChoi));
        return ThongSoLauDai.xuMoiGio(c) * HeSinhThai.TRAN_BAN_NPC_NGAY_PHAN_NGHIN / 1000L;
    }

    /**
     * So cai NPC chiu mua: khong vuot tran ngay con lai. Tra {so cai, tong xu}.
     * {@code daBanHomNay} = xu da ban NPC hom nay.
     */
    public static long[] npcMua(final int ma, final int bac, final int soMuonBan, final long daBanHomNay, final int cap) {
        final long gia = VatLieuSinhThai.giaNpc(ma, bac);
        if (gia <= 0 || soMuonBan <= 0) {
            return new long[]{0, 0};
        }
        final long con = Math.max(0L, tranBanNpcNgay(cap) - daBanHomNay);
        final long so = Math.min(soMuonBan, con / gia);
        return new long[]{so, so * gia};
    }

    /** Ky quy cua don dat hang: gia x so + phi dang. */
    public static long kyQuy(final long giaMoi, final int so) {
        final long t = giaMoi * Math.max(0, so);
        return t + phiDang(t);
    }

    /** Hoan lai khi huy don: phan chua giao (phi dang khong hoan). */
    public static long hoanHuyDon(final long giaMoi, final int soCan, final int soDaGiao) {
        return giaMoi * Math.max(0, soCan - soDaGiao);
    }

    /** Ma vat thuoc loai (loc theo danh muc rao ban). */
    public static boolean thuocLoai(final int ma, final int loai) {
        return VatLieuSinhThai.loai(ma) == loai;
    }
}
