package com.htbb.pokemon;

/**
 * Hoi HP ngoai tran (31/10/2026, quyet dinh chu game): khong bi quai / nguoi danh trong {@link #NGOAI_TRAN_MS} thi moi nhip
 * 5 giay hoi toi 1% HP toi da (tran thiet ke: "hồi HP / PP mỗi nhịp 5 giây ≤ 1% tối đa", docs/thiet-ke-game.md muc 7).
 * Truoc day HP chi tu hoi nho dong "hoi HP" cua do (haveRegentHP) - goc 0: nguoi choi it tien phai uong thuoc / ve Chansey
 * sau moi tran (cong kiem bot cb2: thuoc ~100-180% thu nhap thiet ke). Hoi cua do tinh vao cung tran 1%.
 */
public final class HoiMauNgoaiTran {

    /** Khong mat HP trong chung nay (ms) thi coi la ngoai tran. */
    public static final long NGOAI_TRAN_MS = 10000L;
    /** Phan nghin HP toi da moi nhip 5 giay (tong voi hoi cua do). */
    public static final int PHAN_NGHIN = 10;

    private HoiMauNgoaiTran() {
    }

    /**
     * HP hoi them o nhip nay. {@code daHoi} = HP do da hoi o nhip nay (haveRegentHP). 0 khi guc / day / con trong tran.
     */
    public static int luong(final int hp, final int maxhp, final long bay, final long lucMatMauCuoi, final int daHoi) {
        if (hp <= 0 || maxhp <= 0 || hp >= maxhp || bay - lucMatMauCuoi < NGOAI_TRAN_MS) {
            return 0;
        }
        final int muc = Math.max(1, (int) ((long) maxhp * PHAN_NGHIN / 1000L));
        return Math.max(0, Math.min(muc - Math.max(0, daHoi), maxhp - hp));
    }
}
