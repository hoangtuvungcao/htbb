import com.htbb.pokemon.ChiSoDong;
import com.htbb.pokemon.DoBenLogic;
import com.htbb.pokemon.HoiMauNgoaiTran;
import java.util.TreeMap;
import real.Char;

/**
 * Can bang dong + do ben + hoi HP ngoai tran (quyet dinh chu game 31/10/2026, sau cong kiem bot cb2: o cap 30 tich HP x
 * tan cong x thu cac dong lech 11 lan, Charmander / Eevee khong ha noi quai cung cap; vu khi hong + sua 10% gia = nguoi het
 * xu ket vinh vien).
 * <ul>
 *   <li>ChiSoDong: moi dong cap 10..100 dung ti le bang thiet ke (HP / tan cong / thu, +-3% do lam tron), tich HP x TC x
 *       thu cua moi dong lech tich thiet ke cua dong do <= 5%; nhan vat that (Char.calculateAttrib, khong do) cung vay;</li>
 *   <li>vu khi tiem trung tinh: moi dong cung tan cong goc o cung bac;</li>
 *   <li>HoiMauNgoaiTran: chi hoi sau 10 giay khong mat HP, toi da 1% / nhip, tinh ca hoi cua do;</li>
 *   <li>DoBenLogic: gia sua theo phan do ben mat; vu khi hong 30% sat thuong.</li>
 * </ul>
 */
public final class KiemCanBangDong {

    private KiemCanBangDong() {
    }

    static final String[] TEN = {"Charmander", "Bulbasaur", "Eevee", "Squirtle", "Pichu"};

    public static void chay() {
        Kiem.nhom("Can bang dong theo bang thiet ke + hoi HP ngoai tran + do ben (chu game 31/10/2026)");
        try {
            tiLe();
            nhanVat();
            vuKhi();
            hoiMau();
            doBen();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static void tiLe() {
        final StringBuilder sai = new StringBuilder();
        for (int cap = 10; cap <= 100; cap++) {
            final double hp0 = ChiSoDong.hp(0, cap), tc0 = ChiSoDong.tanCong(0, cap), thu0 = ChiSoDong.thu(0, cap);
            for (int d = 0; d < 5; d++) {
                final int[] t = ChiSoDong.TI_LE[d];
                final double rHp = ChiSoDong.hp(d, cap) / hp0 * 100.0, rTc = ChiSoDong.tanCong(d, cap) / tc0 * 105.0,
                        rThu = ChiSoDong.thu(d, cap) / thu0 * 90.0;
                if (Math.abs(rHp / (t[0] / 100.0 * 100.0) - 1) > 0.03 || Math.abs(rTc / t[1] - 1) > 0.03 || Math.abs(rThu / t[2] - 1) > 0.03) {
                    sai.append(" ").append(TEN[d]).append("@").append(cap);
                }
                // tich chi so cua dong so voi tich thiet ke cua chinh dong do (cung he quy chieu dong 0)
                final double tich = ChiSoDong.hp(d, cap) * (double) ChiSoDong.tanCong(d, cap) * ChiSoDong.thu(d, cap);
                final double tich0 = hp0 * tc0 * thu0;
                final double tichTk = t[0] * (double) t[1] * t[2] / (100.0 * 105.0 * 90.0);
                if (Math.abs(tich / tich0 / tichTk - 1) > 0.05) {
                    sai.append(" tich:").append(TEN[d]).append("@").append(cap);
                }
            }
        }
        Kiem.dung("ChiSoDong cap 10..100: ti le HP / TC / thu dung bang thiet ke (+-3%), tich trong +-5%" + sai, sai.length() == 0);
        Kiem.dung("ChiSoDong tang theo cap (HP, TC, thu cap 50 > cap 10)", ChiSoDong.hp(3, 50) > ChiSoDong.hp(3, 10)
                && ChiSoDong.tanCong(0, 50) > ChiSoDong.tanCong(0, 10) && ChiSoDong.thu(4, 50) > ChiSoDong.thu(4, 10));
        Kiem.dung("cap 30 khong lech qua so cu (HP 4.000-7.000, TC goc 180-260, thu goc 75-120)",
                ChiSoDong.hp(0, 30) > 4000 && ChiSoDong.hp(0, 30) < 7000 && ChiSoDong.tanCong(0, 30) > 180 && ChiSoDong.tanCong(0, 30) < 260
                && ChiSoDong.thu(0, 30) > 75 && ChiSoDong.thu(0, 30) < 120);
    }

    /** Nhan vat that (khong do, khong tien hoa / huan luyen): calculateAttrib cho 5 dong dung ti le. */
    static void nhanVat() {
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        try {
            final StringBuilder sai = new StringBuilder();
            for (final int cap : new int[]{10, 30, 50, 72, 85, 100}) {
                final int[][] cs = new int[5][];
                for (int d = 0; d < 5; d++) {
                    final Char p = new Char(null);
                    p.charClass = (byte) d;
                    p.lvDetail.lv = (short) cap;
                    p.pokeDex = 0;
                    p.calculateAttrib();
                    cs[d] = new int[]{p.maxhp, p.attack, p.defend_physic, p.defend_magic};
                }
                for (int d = 1; d < 5; d++) {
                    final int[] t = ChiSoDong.TI_LE[d], t0 = ChiSoDong.TI_LE[0];
                    for (int k = 0; k < 3; k++) {
                        final double kq = cs[d][k] / (double) cs[0][k], tk = t[k] / (double) t0[k];
                        if (Math.abs(kq / tk - 1) > 0.05) {
                            sai.append(" ").append(TEN[d]).append("@").append(cap).append("[").append(k).append("]=")
                                    .append(String.format("%.2f/%.2f", kq, tk));
                        }
                    }
                }
                if (cap == 30) {
                    Kiem.dung("cap 30 khong do: phong thu vat = phong thu phep moi dong (bo Squirtle x4/3, Pichu x11/10)",
                            cs[3][2] == cs[3][3] && cs[4][2] == cs[4][3]);
                    Kiem.dung("cap 30 khong do: Squirtle thu < 1,6 x Charmander (truoc 5 lan)", cs[3][2] < cs[0][2] * 1.6);
                }
            }
            Kiem.dung("Char.calculateAttrib (khong do) cap 10..100: HP / TC / thu cac dong dung ti le thiet ke +-5%" + sai, sai.length() == 0);
        } finally {
            real.Map.gemTemplate = gemCu;
        }
    }

    static void vuKhi() {
        final TreeMap<Integer, int[]> b = new TreeMap<Integer, int[]>();
        b.put(Integer.valueOf(16), new int[]{90, 110, 130, 150, 80});
        b.put(Integer.valueOf(36), new int[]{180, 190, 200, 230, 150});
        ChiSoDong.datVuKhi(b);
        try {
            boolean bang = true;
            for (final int cap : new int[]{16, 20, 36, 40}) {
                final int[] a = b.floorEntry(Integer.valueOf(cap)).getValue();
                int dau = -1;
                for (int d = 0; d < 5; d++) {
                    final int v = a[d] + ChiSoDong.lechVuKhi(3 + d, cap, d);
                    bang &= dau < 0 || v == dau;
                    dau = v;
                }
            }
            Kiem.dung("vu khi tiem cung bac: moi dong cung tan cong goc sau khi chinh (16: 112, 36: 190)", bang
                    && 90 + ChiSoDong.lechVuKhi(3, 16, 0) == 112);
            Kiem.bang("khong phai vu khi (giap type 0): khong chinh", Integer.valueOf(0), Integer.valueOf(ChiSoDong.lechVuKhi(0, 16, 0)));
            Kiem.bang("vu khi duoi bac thap nhat: khong chinh", Integer.valueOf(0), Integer.valueOf(ChiSoDong.lechVuKhi(3, 5, 0)));
        } finally {
            ChiSoDong.datVuKhi(null);
        }
    }

    static void hoiMau() {
        final long t = 1000000L;
        Kiem.bang("vua mat HP 5 giay truoc: khong hoi", Integer.valueOf(0), Integer.valueOf(HoiMauNgoaiTran.luong(5000, 10000, t, t - 5000L, 0)));
        Kiem.bang("9,9 giay: chua hoi", Integer.valueOf(0), Integer.valueOf(HoiMauNgoaiTran.luong(5000, 10000, t, t - 9900L, 0)));
        Kiem.bang("10 giay khong mat HP: hoi 1% (100 / 10.000)", Integer.valueOf(100), Integer.valueOf(HoiMauNgoaiTran.luong(5000, 10000, t, t - 10000L, 0)));
        Kiem.bang("do da hoi 40: chi them 60 (tran 1% tong)", Integer.valueOf(60), Integer.valueOf(HoiMauNgoaiTran.luong(5000, 10000, t, t - 20000L, 40)));
        Kiem.bang("do da hoi >= 1%: khong them", Integer.valueOf(0), Integer.valueOf(HoiMauNgoaiTran.luong(5000, 10000, t, t - 20000L, 150)));
        Kiem.bang("gan day: chi hoi toi day", Integer.valueOf(30), Integer.valueOf(HoiMauNgoaiTran.luong(9970, 10000, t, 0L, 0)));
        Kiem.bang("dang guc: khong hoi", Integer.valueOf(0), Integer.valueOf(HoiMauNgoaiTran.luong(0, 10000, t, 0L, 0)));
        Kiem.bang("HP thap (50): van hoi it nhat 1", Integer.valueOf(1), Integer.valueOf(HoiMauNgoaiTran.luong(10, 50, t, 0L, 0)));
        final String vl = doc("src/real/CharVongLap.java");
        Kiem.dung("CharVongLap: ghi luc mat HP moi nhip va hoi ngoai tran o nhip 5 giay", vl.contains("self.lucMatMauCuoi = System.currentTimeMillis();")
                && vl.contains("HoiMauNgoaiTran.luong(self.hp, self.maxhp, System.currentTimeMillis(), self.lucMatMauCuoi, daHoiDo)"));
    }

    static void doBen() {
        // gia 50.000, do ben mau 100 (mDurable toi da 1.000)
        Kiem.bang("mon con nguyen: sua 0 xu", Integer.valueOf(0), Integer.valueOf(DoBenLogic.giaSua(50000, 100, 1000)));
        Kiem.bang("mat 2% (mot lan guc): 2% x 5.000 = 100", Integer.valueOf(100), Integer.valueOf(DoBenLogic.giaSua(50000, 100, 980)));
        Kiem.bang("mat 10%: 500", Integer.valueOf(500), Integer.valueOf(DoBenLogic.giaSua(50000, 100, 900)));
        Kiem.bang("hong han (0): 5.000 = gia cu", Integer.valueOf(5000), Integer.valueOf(DoBenLogic.giaSua(50000, 100, 0)));
        Kiem.bang("mat 0,1%: 5", Integer.valueOf(5), Integer.valueOf(DoBenLogic.giaSua(50000, 100, 999)));
        Kiem.bang("mat rat it tren mon re: it nhat 1 xu", Integer.valueOf(1), Integer.valueOf(DoBenLogic.giaSua(500, 100, 999)));
        Kiem.bang("mau khong co do ben: 0", Integer.valueOf(0), Integer.valueOf(DoBenLogic.giaSua(50000, 0, 0)));
        Kiem.bang("chua tung luu do ben (-1): 0", Integer.valueOf(0), Integer.valueOf(DoBenLogic.giaSua(50000, 100, -1)));
        Kiem.bang("vu khi hong: 30% sat thuong", Integer.valueOf(30), Integer.valueOf(DoBenLogic.PT_SAT_THUONG_VU_KHI_HONG));
        final String ch = doc("src/real/Char.java");
        Kiem.dung("Char.attackDam: vu khi hong x30%", ch.contains("this.vuKhiDangHong() ? Math.max(1, d * com.htbb.pokemon.DoBenLogic.PT_SAT_THUONG_VU_KHI_HONG / 100) : d"));
        Kiem.dung("checkDurableWeapone: vu khi hong van cho danh (return true) + canh bao", ch.contains("DoBenLogic.CAU_VU_KHI_HONG"));
        Kiem.dung("getPriceRepair: gia theo do ben mat (DoBenLogic.giaSua)", ch.contains("DoBenLogic.giaSua(cwi.getTemplate().price, cwi.getTemplate().durable, cwi.mDurable)"));
        // that: Char co vu khi hong -> vuKhiDangHong
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        try {
            final Char p = new Char(null);
            Kiem.dung("khong mac gi: vu khi khong hong", !p.vuKhiDangHong());
        } finally {
            real.Map.gemTemplate = gemCu;
        }
    }

    static String doc(final String f) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(f)), java.nio.charset.StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
