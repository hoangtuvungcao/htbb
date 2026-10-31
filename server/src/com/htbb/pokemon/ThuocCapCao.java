package com.htbb.pokemon;

/**
 * THUOC CAP CAO BANG XU (Y ta Chansey / Kecleon, 31/10/2026) - luat thuan, phan game: real.ThuocCapCaoGame.
 *
 * <p>Chu game + nguoi choi: cua so thuoc (NpcDichVu.THUOC_BAN) chi ban toi "Thuốc hồi máu cực mạnh" 1.000 HP / "Ether tối
 * đa" 2.000 PP. Sau can bang dong, cap 50+ co 11-22 nghin HP nen thuoc xu vo dung va nguoi choi phai dot luong khoa vao goi
 * 200 binh o Cua hang dac biet (data_shop 74..77). Lop nay ban LAI chinh cac binh do (cung id data_potion, cung luong hoi
 * o real.doUsePotion) bang xu, mo theo cap:
 *
 * <pre>
 *  bac  id  hoi            cap   gia goc (xu / binh)
 *   0   22  3.000 HP        30    8
 *   1   93  7.000 HP        50   18
 *   2   94  15.000 HP       70   60
 *   3   24  3.500 PP        30    9
 *   4   95  7.000 PP        50   18
 * </pre>
 *
 * Gia 1 binh = max(gia goc, 0,05% thu nhap 1 gio o cap nguoi mua) - docs/thiet-ke-game.md 9.3: moi mon 0,05-5% thu
 * nhap 1 gio, tong chi thuoc &lt;= 12% (tieu_hao.thuoc_phan_tram_thu_nhap). Gia goc dat de o cap mo (cap 30 / 50 / 70) danh
 * quai dung cap (~3-6% HP toi da moi don, vai don moi con, hoi 1% / 5 giay ngoai tran; PP = nua so diem HP) ton ~6-10%
 * thu nhap cho HP + PP, nang nhat so do (4 don x 6%) o cap 50 / 70 ~11,8% (server/test/KiemThuocCapCao tinh lai bang bang
 * data_cap_do_lau_dai). San 0,05% giu binh van la ho hut xu nho o
 * cap cao (cap 100: binh 15.000 HP ~ 152 xu).
 *
 * <p>data_potion.price cua cac id nay GIU 0: goi 24 (cua so thuoc, gia co dinh, client chi biet gia 20 dong dau) tu
 * choi chung (OfflineMap.doBuyItem: price &lt;= 0 bo qua) - chi mua duoc qua menu may chu nen cap mo + NPC dang dung canh
 * luon duoc kiem. Goi luong khoa 200 binh giu nguyen (tien loi, khong bat buoc).
 */
public final class ThuocCapCao {

    /** id data_potion theo bac. */
    public static final int[] ID = {22, 93, 94, 24, 95};
    /** Luong hoi (khop real.doUsePotion case 22 / 93 / 94 / 24 / 95). */
    public static final int[] HOI = {3000, 7000, 15000, 3500, 7000};
    /** true = PP (Ether / Elixir), false = HP. */
    public static final boolean[] LA_PP = {false, false, false, true, true};
    /** Cap mo ban. */
    public static final int[] CAP = {30, 50, 70, 30, 50};
    /** Gia goc (xu / binh) o cap mo. */
    public static final long[] GIA_GOC = {8L, 18L, 60L, 9L, 18L};
    /** Ten mac dinh (khi bang data_potion chua nap). */
    public static final String[] TEN = {"Thuốc hồi máu tối đa", "Thuốc hồi máu 7k", "Thuốc hồi máu 15k", "Elixir tối đa", "Ether 7k"};
    /** San gia: phan van (1/10.000) thu nhap 1 gio - 5 = 0,05%. */
    public static final int SAN_PHAN_VAN = 5;
    /** Tran mot mon theo thiet ke: 5% thu nhap 1 gio (500 phan van). */
    public static final int TRAN_PHAN_VAN = 500;
    /** Cac goi mua o menu. */
    public static final int[] SO_MUA = {10, 50, 100, 200};
    /** Mot o thuoc toi da (real.GiaoDichThuoc.TOI_DA_MOT_O). */
    public static final int TOI_DA_O = 999;
    /** Cap mo thap nhat (dong menu Chansey / Kecleon). */
    public static final int CAP_MO_DAU = 30;

    private ThuocCapCao() {
    }

    public static int soBac() {
        return ID.length;
    }

    /** Bac cua id data_potion, -1 neu khong phai thuoc cap cao. */
    public static int bacCuaId(final int id) {
        for (int i = 0; i < ID.length; i++) {
            if (ID[i] == id) {
                return i;
            }
        }
        return -1;
    }

    public static boolean hopLe(final int bac) {
        return bac >= 0 && bac < ID.length;
    }

    /** Nhan vat cap {@code cap} mua duoc bac {@code bac}. */
    public static boolean mo(final int bac, final int cap) {
        return hopLe(bac) && cap >= CAP[bac];
    }

    /** Co it nhat mot bac mo o cap {@code cap}. */
    public static boolean coBacMo(final int cap) {
        for (int i = 0; i < ID.length; i++) {
            if (mo(i, cap)) {
                return true;
            }
        }
        return false;
    }

    /** Gia 1 binh theo thu nhap 1 gio {@code xuMoiGio} (thuan, test): max(gia goc, ceil(0,05% thu nhap)). */
    public static long gia(final int bac, final long xuMoiGio) {
        if (!hopLe(bac)) {
            return Long.MAX_VALUE;
        }
        final long san = (Math.max(0L, xuMoiGio) * SAN_PHAN_VAN + 9999L) / 10000L;
        return Math.max(GIA_GOC[bac], san);
    }

    /** Gia 1 binh o cap {@code cap} (bang data_cap_do_lau_dai chua nap -&gt; gia goc). */
    public static long giaTheoCap(final int bac, final int cap) {
        long thuNhap;
        try {
            thuNhap = LauDai.xuMoiGio(cap);
        } catch (final RuntimeException e) {
            thuNhap = 0L;
        }
        return gia(bac, thuNhap);
    }

    /** So binh mua that: {@code muon} kep theo cho con trong o (toi da {@link #TOI_DA_O}). */
    public static int soMuaDuoc(final int dangCo, final int muon) {
        return Math.max(0, Math.min(muon, TOI_DA_O - Math.max(0, dangCo)));
    }

    /** "+7.000 HP" / "+3.500 PP". */
    public static String hoiChu(final int bac) {
        return hopLe(bac) ? "+" + HoiSinh.so(HOI[bac]) + (LA_PP[bac] ? " PP" : " HP") : "";
    }
}
