package com.htbb.pokemon;

/**
 * Do ben trang bi (31/10/2026, quyet dinh chu game sau cong kiem bot cb2): guc mat 2% do ben, dang nhap lai KHONG con hoi
 * day (Item.napLaiTemplateGiuDoBen) - truoc ban nay vu khi hong = khong danh duoc va sua = 10% gia mon bat ke da mat bao
 * nhieu: nguoi choi het xu sua khong con cach kiem xu (bot cb2: 8/9 nhan vat "Không đủ tiền" o Machamp, Gym 0 sat thuong).
 * Nay: (a) vu khi hong van danh, {@link #PT_SAT_THUONG_VU_KHI_HONG}% sat thuong, kem canh bao; (b) gia sua moi mon =
 * 10% gia x phan do ben da mat (lam tron len, mon con nguyen = 0).
 */
public final class DoBenLogic {

    public static final int PT_SAT_THUONG_VU_KHI_HONG = 30;
    public static final String CAU_VU_KHI_HONG = "Đồ cầm tay đã hỏng: chỉ gây 30% sát thương. Hãy nhờ Machamp thợ rèn sửa lại.";

    private DoBenLogic() {
    }

    /**
     * Gia sua mot mon: gia/10 x (do ben toi da - do ben con) / do ben toi da; do ben theo mDurable (x10, toi da = durable mau
     * x 10). Mau khong co do ben (0) = 0; mDurable chua tung luu (< 0) = coi nhu day.
     */
    public static int giaSua(final int giaMon, final int doBenMau, final int mDurable) {
        if (giaMon <= 0 || doBenMau <= 0 || mDurable < 0) {
            return 0;
        }
        final long toiDa = (long) doBenMau * 10L;
        final long con = Math.min(toiDa, mDurable);
        final long mat = toiDa - con;
        if (mat <= 0) {
            return 0;
        }
        final long goc = giaMon / 10L;
        return (int) Math.max(1L, (goc * mat + toiDa - 1) / toiDa);
    }
}
