import com.htbb.pokemon.GioiHanChiSo;
import com.htbb.pokemon.NganSachTrangBi;
import com.htbb.pokemon.SatThuongLauDai;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MO PHONG CHI SO / TTK (docs/thiet-ke-game.md). TAT DINH (khong ngau nhien: ky vong).
 *
 * Mo hinh = mo hinh noi dung (tools/pokemon/sinh_sql_lau_dai.py quai_de_xuat + sinh_noi_dung.py Gym/boss):
 *   tan cong nguoi  = (30 + 12L) x (1 + 0,6 x he so nang cap(+p)) x he so dong
 *   thu nguoi       = 0,25 x tan cong (+6) x he so dong;  HP nguoi = 70(25+L) x (1 + 0,6 x nang cap) x he so dong
 *   quai thuong     = quai_de_xuat(L): TTK muc tieu 4/5/6 giay o +6, 15/12 don ha nguoi
 *   DPS nguoi       = max(5% atk, atk - thu) x 1,1 x 2,2 (xoay chieu) x (1 + ti le CM x (he so CM - 1))
 *   PvP             = x0,30, tran 8% HP moi don (SatThuongLauDai)
 * Ba kich ban chi so phu: THUONG (CM 5%), TOI DA TRUOC TRAN (cong don moi nguon: CM 80%, ST CM +60%, hut 35%,
 * giam ST 85%, ne 60%, % cong +150%), TOI DA SAU TRAN (cung so lieu qua GioiHanChiSo).
 *
 * Chay o HTBB/: javac -encoding UTF-8 -cp &lt;lop src&gt; -d &lt;ra&gt; tools/pokemon/MoPhongChiSo.java
 *              java -cp &lt;ra&gt;:&lt;lop src&gt; MoPhongChiSo        -> du_lieu/mo_phong_chi_so.txt
 * test/KiemChiSo goi chayKiem(".") qua phan chieu.
 */
public final class MoPhongChiSo {

    static final String RA = "du_lieu/mo_phong_chi_so.txt";
    /** {ten, HP, tan cong, thu} tuong doi (THIET_KE 4.4). */
    static final String[] DONG = {"Charmander", "Bulbasaur", "Eevee", "Squirtle", "Pichu"};
    static final int[][] HE_DONG = {{100, 105, 90}, {115, 90, 115}, {90, 100, 95}, {120, 85, 125}, {85, 110, 85}};
    static final int[] CAP = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    static final int[] NANG_CAP = {0, 6, 10, 15};
    static final double XOAY = 2.2;
    static final int TTK_GYM = 105;
    static final int TTK_BOSS_PHUT = 10;
    static final int NGUOI_BOSS = 20;

    /** Kich ban chi so phu. */
    static final class KichBan {
        final String ten;
        final double cm, heSoCm, hut, giam, ne, phanTramCong, hutPvp, giamPvp, cmPvp, nePvp;

        KichBan(final String ten, final double cm, final double heSoCm, final double hut, final double giam, final double ne,
                final double pt, final double hutPvp, final double giamPvp, final double cmPvp, final double nePvp) {
            this.ten = ten;
            this.cm = cm;
            this.heSoCm = heSoCm;
            this.hut = hut;
            this.giam = giam;
            this.ne = ne;
            this.phanTramCong = pt;
            this.hutPvp = hutPvp;
            this.giamPvp = giamPvp;
            this.cmPvp = cmPvp;
            this.nePvp = nePvp;
        }
    }

    static KichBan thuong() {
        return new KichBan("thuong", 0.05, 1.5, 0, 0, 0, 0, 0, 0, 0.05, 0);
    }

    static KichBan toiDaTruocTran() {
        return new KichBan("toi_da_truoc_tran", 0.80, 1.5 + 0.60, 0.35, 0.85, 0.60, 1.50, 0.35 / 0.30, 0.85, 0.80, 0.60);
    }

    static KichBan toiDaSauTran() {
        final double cm = GioiHanChiSo.chiMang(800, false) / 1000.0;
        final double hs = GioiHanChiSo.heSoChiMangPhanTram(600) / 100.0;
        final double hut = GioiHanChiSo.hut(350, false) / 1000.0;
        // giam ST thu dong tran 50% + khang he 60% nhan voi nhau, nhung san sat thuong: con >= 25%
        final double giam = Math.min(1.0 - GioiHanChiSo.SAN_SAT_THUONG / 1000.0,
                1.0 - (1.0 - GioiHanChiSo.giamSatThuong(850, false) / 1000.0) * (1.0 - GioiHanChiSo.khangHe(850) / 1000.0));
        final double ne = GioiHanChiSo.neTranh(600, false) / 1000.0;
        final double pt = GioiHanChiSo.PHAN_TRAM_CONG_TRAN / 1000.0;
        final double hutPvp = GioiHanChiSo.hut(350, true) / 1000.0;
        final double giamPvp = Math.min(1.0 - GioiHanChiSo.SAN_SAT_THUONG / 1000.0,
                1.0 - (1.0 - GioiHanChiSo.giamSatThuong(850, true) / 1000.0) * (1.0 - GioiHanChiSo.khangHe(850) / 1000.0));
        final double cmPvp = GioiHanChiSo.chiMang(800, true) / 1000.0;
        final double nePvp = GioiHanChiSo.neTranh(600, true) / 1000.0;
        return new KichBan("toi_da_sau_tran", cm, hs, hut, giam, ne, pt, hutPvp, giamPvp, cmPvp, nePvp);
    }

    // ------------------------------------------------------------------ mo hinh

    static double nc(final int p) {
        return NganSachTrangBi.heSoNangCap(p) / 100.0;
    }

    static double atkTran(final int L) {
        return 30 + 12 * L;
    }

    static double atkNguoi(final int L, final int p, final int d, final KichBan k) {
        return atkTran(L) * (1 + 0.6 * nc(p)) * HE_DONG[d][1] / 100.0 * (1 + k.phanTramCong * 0.2);
        // % cong them ngoai do: chi mot phan (20%) cua kich ban % tac dong len tan cong trung binh (pet, thu cuoi)
    }

    static double thuNguoi(final int L, final int d) {
        return 0.25 * atkTran(L) * (1 + 0.6 * nc(6)) * HE_DONG[d][2] / 100.0;
    }

    static double hpNguoi(final int L, final int p, final int d) {
        return 70.0 * (25 + L) * (1 + 0.6 * nc(p)) * HE_DONG[d][0] / 100.0;
    }

    /** quai_de_xuat(L): {hp, tan cong, thu} (sinh_sql_lau_dai.py). */
    static double[] quai(final int L) {
        final double atk = atkTran(L) * (1 + 0.6 * nc(6));
        final double thu = Math.round(0.35 * atk);
        final double ttk = L <= 30 ? 4.0 : (L <= 60 ? 5.0 : 6.0);
        final double dps = (atk - thu) * 1.1 * XOAY;
        final double hp = Math.round(ttk * dps / 10.0) * 10;
        final double hpN = 70.0 * (25 + L) * 1.6;
        final double don = L <= 30 ? 15 : 12;
        final double atkQ = Math.round(hpN / don / 0.76 + 0.25 * atk);
        return new double[]{hp, atkQ, thu, ttk};
    }

    static double dps(final double atk, final double thu, final double cm, final double hsCm) {
        return SatThuongLauDai.coBan((long) atk, (long) thu) * 1.1 * XOAY * (1 + cm * (hsCm - 1));
    }

    /** DPS quai danh nguoi (15/12 don toi HP chuan): 0,25 don/giay x (atkQ - thu nguoi) x 0,76. */
    static double dpsQuai(final double atkQ, final double thuN, final double nhan) {
        return 0.25 * Math.max(0.05 * atkQ * nhan, atkQ * nhan - thuN) * 0.76;
    }

    // ------------------------------------------------------------------ chay

    static final class Kq {
        final List<String[]> mucTieu = new ArrayList<String[]>();
        final StringBuilder bao = new StringBuilder();

        void muc(final String ten, final boolean dat, final String chiTiet) {
            mucTieu.add(new String[]{ten, dat ? "1" : "0", chiTiet});
        }

        void p(final String f, final Object... a) {
            bao.append(String.format(Locale.ROOT, f, a)).append('\n');
        }
    }

    public static String[][] chayKiem(final String goc) {
        final Kq k = chay(goc);
        return k.mucTieu.toArray(new String[0][]);
    }

    static Kq chay(final String goc) {
        final Kq k = new Kq();
        final KichBan th = thuong();
        final KichBan tr = toiDaTruocTran();
        final KichBan sa = toiDaSauTran();
        k.p("MO PHONG CHI SO / TTK (sinh boi tools/pokemon/MoPhongChiSo.java, KHONG SUA TAY)");
        k.p("Kich ban toi da sau tran: CM %.1f%% x%.2f, hut %.1f%%, giam ST tong %.1f%%, ne %.1f%%, %% cong +%.0f%%",
                sa.cm * 100, sa.heSoCm, sa.hut * 100, sa.giam * 100, sa.ne * 100, sa.phanTramCong * 100);
        k.p("Kich ban toi da TRUOC tran: CM %.0f%% x%.2f, hut %.0f%%, giam ST %.0f%%, ne %.0f%%, %% cong +%.0f%%",
                tr.cm * 100, tr.heSoCm, tr.hut * 100, tr.giam * 100, tr.ne * 100, tr.phanTramCong * 100);
        k.p("");
        // ---------- 1. TTK nguoi -> quai thuong / tinh anh / Gym / boss the gioi
        double lechThuongMax = 0;
        double lechChuanMax = 0;
        double tiMin = 99;
        double tiMax = 0;
        double ttkSauTranMin = 99;
        double ttkTruocTranMin = 99;
        boolean gymDat = true;
        boolean bossDat = true;
        boolean tinhAnhDat = true;
        k.p("1. TTK (giay) NGUOI HA QUAI THUONG theo cap / dong / nang cap (kich ban thuong) - muc tieu 4 / 5 / 6 giay");
        k.p("   cap  muc  %s", hang(DONG, "%-24s"));
        for (final int L : CAP) {
            final double[] q = quai(L);
            final StringBuilder sb = new StringBuilder();
            for (int d = 0; d < DONG.length; d++) {
                final StringBuilder o = new StringBuilder();
                for (final int p : NANG_CAP) {
                    final double t = q[0] / dps(atkNguoi(L, p, d, th), q[2], th.cm, th.heSoCm);
                    lechThuongMax = Math.max(lechThuongMax, Math.abs(t / q[3] - 1));
                    if (d == 2) {
                        lechChuanMax = Math.max(lechChuanMax, Math.abs(t / q[3] - 1));
                    }
                    tiMin = Math.min(tiMin, t / q[3]);
                    tiMax = Math.max(tiMax, t / q[3]);
                    o.append(String.format(Locale.ROOT, "%5.1f", t));
                    final double tS = q[0] / dps(atkNguoi(L, p, d, sa), q[2], sa.cm, sa.heSoCm);
                    final double tT = q[0] / dps(atkNguoi(L, p, d, tr), q[2], tr.cm, tr.heSoCm);
                    ttkSauTranMin = Math.min(ttkSauTranMin, tS / q[3]);
                    ttkTruocTranMin = Math.min(ttkTruocTranMin, tT / q[3]);
                }
                sb.append(String.format(Locale.ROOT, "%-24s", o));
            }
            k.p("   %3d  %3.0f  %s", L, q[3], sb);
        }
        k.p("   (moi o: +0 / +6 / +10 / +15)");
        k.muc("TTK quai thuong dong chuan (Eevee) trong +-25% muc tieu o MOI muc +0..+15",
                lechChuanMax <= 0.25, String.format(Locale.ROOT, "lech lon nhat %.1f%%", lechChuanMax * 100));
        k.muc("TTK quai thuong MOI dong trong 0,6..1,5 x muc tieu o moi muc +0..+15 (dong do don / dong sat thuong)",
                tiMin >= 0.6 && tiMax <= 1.5, String.format(Locale.ROOT, "%.2f..%.2f", tiMin, tiMax));
        k.muc("Chi so phu toi da SAU tran: TTK quai thuong >= 40% muc tieu (it nhat ~4 don)",
                ttkSauTranMin >= 0.4, String.format(Locale.ROOT, "thap nhat %.0f%% muc tieu (truoc tran %.0f%%)",
                        ttkSauTranMin * 100, ttkTruocTranMin * 100));
        k.p("   Toi da sau tran: TTK thap nhat = %.0f%% muc tieu; truoc tran = %.0f%%.", ttkSauTranMin * 100, ttkTruocTranMin * 100);
        k.p("");
        k.p("2. TINH ANH / GYM / BOSS THE GIOI (dong chuan Eevee, +6, kich ban thuong; ngoac: +0 / +15)");
        for (final int L : new int[]{12, 20, 30, 40, 50, 60, 70, 80, 90}) {
            final double[] q = quai(L);
            final double ta = q[0] * 4 / dps(atkNguoi(L, 6, 2, th), q[2], th.cm, th.heSoCm);
            final double gymHp = Math.round(TTK_GYM * (atkTran(L) * (1 + 0.6 * nc(6)) - q[2]) * 1.1 * XOAY / 100.0) * 100;
            final double gym6 = gymHp / dps(atkNguoi(L, 6, 2, th), q[2] * 1.2, th.cm, th.heSoCm);
            final double gym0 = gymHp / dps(atkNguoi(L, 0, 2, th), q[2] * 1.2, th.cm, th.heSoCm);
            final double gym15 = gymHp / dps(atkNguoi(L, 15, 2, th), q[2] * 1.2, th.cm, th.heSoCm);
            final double bossHp = Math.round(TTK_BOSS_PHUT * 60 * NGUOI_BOSS * (atkTran(L) * (1 + 0.6 * nc(6)) - q[2] * 1.5) * 1.1 * XOAY / 1000.0) * 1000;
            final double boss = bossHp / (NGUOI_BOSS * dps(atkNguoi(L, 6, 2, th), q[2] * 1.5, th.cm, th.heSoCm)) / 60.0;
            final double bossSa = bossHp / (NGUOI_BOSS * dps(atkNguoi(L, 15, 2, sa), q[2] * 1.5, sa.cm, sa.heSoCm)) / 60.0;
            gymDat &= gym6 >= 90 && gym6 <= 120;
            bossDat &= boss >= 8 && boss <= 12 && bossSa >= 4;
            tinhAnhDat &= ta >= 12 && ta <= 30;
            k.p("   cap %3d: tinh anh %5.1f s | Gym %5.1f s (%5.1f / %5.1f) | boss TG 20 nguoi %4.1f phut (toi da sau tran +15: %4.1f)",
                    L, ta, gym6, gym0, gym15, boss, bossSa);
        }
        k.muc("Gym 90-120 giay danh mot minh, dung cap, +6", gymDat, "xem bang 2");
        k.muc("Boss the gioi 8-12 phut voi 20 nguoi (+6); toi da sau tran >= 4 phut", bossDat, "xem bang 2");
        k.muc("Tinh anh 12-30 giay (+6)", tinhAnhDat, "xem bang 2");
        k.p("");
        // ---------- 3. Bat tu: hoi phuc < sat thuong nhan
        k.p("3. CHONG BAT TU: hoi phuc (hoi 1%%/5 giay + hut HP x DPS) so voi DPS nhan (toi da sau tran, +15, sau ne/giam)");
        boolean batTuSau = false;
        boolean batTuTruoc = false;
        double tiLeMax = 0;
        String batTuChiTiet = "";
        for (final int L : new int[]{10, 30, 50, 70, 90}) {
            final double[] q = quai(L);
            for (int d = 0; d < DONG.length; d++) {
                final double hp = hpNguoi(L, 15, d) * (1 + sa.phanTramCong * 0.2);
                final double myDps = dps(atkNguoi(L, 15, d, sa), q[2], sa.cm, sa.heSoCm);
                final double[][] nguon = {{1.3, 1, 1}, {1.5, 1, 1}, {2.0, 1, 1}, {1.0, 3, 0}}; // tinh anh, Gym, boss TG, 3 quai
                for (final double[] n : nguon) {
                    final double hoi = hoiSauTran(hp, myDps, sa.hut, n[2] > 0);
                    final double vao = dpsQuai(q[1], thuNguoi(L, d), n[0]) * n[1] * (1 - sa.giam) * (1 - sa.ne);
                    if (hoi / vao > tiLeMax) {
                        tiLeMax = hoi / vao;
                        batTuChiTiet = String.format(Locale.ROOT, "cap %d %s nguon x%.1f x%.0f", L, DONG[d], n[0], n[1]);
                    }
                    batTuSau |= hoi >= vao;
                    final double hpT = hpNguoi(L, 15, d) * (1 + tr.phanTramCong * 0.2);
                    final double hoiT = hpT * 0.10 / 5.0 + tr.hut * dps(atkNguoi(L, 15, d, tr), q[2], tr.cm, tr.heSoCm);
                    final double vaoT = dpsQuai(q[1], thuNguoi(L, d), n[0]) * n[1] * (1 - tr.giam) * (1 - tr.ne);
                    batTuTruoc |= hoiT >= vaoT;
                }
            }
            final double hp = hpNguoi(L, 15, 3);
            final double vao = dpsQuai(q[1], thuNguoi(L, 3), 1.5) * (1 - sa.giam) * (1 - sa.ne);
            final double hoi = hoiSauTran(hp, dps(atkNguoi(L, 15, 3, sa), q[2], sa.cm, sa.heSoCm), sa.hut, true);
            k.p("   cap %3d Squirtle +15 vs Gym: hoi %6.1f HP/s < nhan %6.1f HP/s (%.0f%%)", L, hoi, vao, hoi / vao * 100);
        }
        k.muc("Khong bat tu (toi da sau tran): hoi phuc < sat thuong nhan tu tinh anh / Gym / boss / 3 quai", !batTuSau,
                String.format(Locale.ROOT, "ti le hoi/nhan cao nhat %.0f%% o %s (truoc tran: %s)", tiLeMax * 100, batTuChiTiet,
                        batTuTruoc ? "CO bat tu" : "khong"));
        k.p("   Truoc tran (giam 85%%, ne 60%%, hut 35%%): %s.", batTuTruoc ? "CO truong hop hoi >= nhan (bat tu)" : "khong bat tu");
        k.p("");
        // ---------- 4. PvP
        k.p("4. PvP cung cap, cung do, cung dong (giay): thuong (khong chi mang) | khac x2 + chi mang toi da sau tran | truoc tran");
        boolean pvpDat = true;
        double pvpChuanMin = 99;
        double pvpChuanMax = 0;
        boolean pvpNhanh = true;
        for (final int L : CAP) {
            final StringBuilder sb = new StringBuilder();
            for (int d = 0; d < DONG.length; d++) {
                final double hp = hpNguoi(L, 6, d);
                final double a = atkNguoi(L, 6, d, th);
                final double tN = pvpTtk(hp, a, thuNguoi(L, d), 0, 1.5, 1.0, 0, 0, 0);
                final double tS = pvpTtk(hp * (1 + sa.phanTramCong * 0.2), atkNguoi(L, 6, d, sa), thuNguoi(L, d), sa.cmPvp, 1.5, 2.0,
                        sa.giamPvp, sa.hutPvp, sa.nePvp);
                final double tT = pvpTtk(hp * (1 + tr.phanTramCong * 0.2), atkNguoi(L, 6, d, tr), thuNguoi(L, d), tr.cmPvp, 2.0, 2.0,
                        tr.giamPvp, tr.hutPvp, tr.nePvp);
                if (d == 2 && L >= 40) {
                    pvpChuanMin = Math.min(pvpChuanMin, tN);
                    pvpChuanMax = Math.max(pvpChuanMax, tN);
                }
                pvpNhanh &= tS >= 6;
                sb.append(String.format(Locale.ROOT, "%5.1f|%5.1f|%5.0f  ", tN, tS, tT));
            }
            k.p("   cap %3d: %s", L, sb);
        }
        k.p("   (cot: %s)", String.join(" / ", DONG));
        pvpDat = pvpChuanMin >= 10 && pvpChuanMax <= 15;
        k.muc("PvP cung cap cung do, dong chuan, cap >= 40: 10-15 giay (thiet ke 10-14)", pvpDat,
                String.format(Locale.ROOT, "%.1f..%.1f giay", pvpChuanMin, pvpChuanMax));
        k.muc("PvP khac he x2 + chi mang toi da sau tran >= 6 giay", pvpNhanh, "xem bang 4");
        k.p("");
        // ---------- 5. Bac do
        k.p("5. DO BAC CU +15 (+ ngoc 10%%) / DO BAC MOI +0 (chi so chinh, cung cap moc bac moi): truoc kep | sau kep");
        double truocMax = 0;
        double sauMax = 0;
        for (final int t : NganSachTrangBi.cacO()) {
            final StringBuilder sb = new StringBuilder();
            for (int b = 1; b <= 10; b++) {
                final double a = NganSachTrangBi.tiLeBacCuChuaKep(t, b);
                final double s = NganSachTrangBi.tiLeBacCuSauKep(t, b);
                truocMax = Math.max(truocMax, a);
                sauMax = Math.max(sauMax, s);
                sb.append(String.format(Locale.ROOT, "%4.2f|%4.2f ", a, s));
            }
            k.p("   o %2d: %s", t, sb);
        }
        k.muc("Do bac cu nang toi da khong hon do bac hien tai +0 qua 30% (sau kep)", sauMax <= 1.30 + 1e-9,
                String.format(Locale.ROOT, "lon nhat %.2f (truoc kep %.2f)", sauMax, truocMax));
        final double[] moiCu = moiTrenCu();
        k.p("   (Chi so chinh MOT mon, don vi engine: bac moi +0 / bac cu +15 lon nhat %.2f - bac so cap -> dong chenh 13 cap.)", moiCu[0]);
        final double tk = bacMoiTrenCuThietKe(goc, k);
        k.muc("Bac moi +0 <= 1,10 x bac cu +15 cung cap, TONG chi so (goc + 11 o, THIET_KE 3.6, noi_dung_trang_bi.csv)",
                tk > 0 && tk <= 1.10 + 1e-9, String.format(Locale.ROOT, "lon nhat %.3f", tk));
        k.p("");
        // ---------- 6. Du lieu data_item
        final List<int[]> items = SinhSqlChiSo.docDataItem(SinhSqlChiSo.doc(new File(goc, SinhSqlChiSo.DUMP).getPath()));
        int truoc = 0;
        int sau = 0;
        int soMau = 0;
        final StringBuilder vd = new StringBuilder();
        for (final int[] r : items) {
            if (!NganSachTrangBi.coNganSach(r[1])) {
                continue;
            }
            soMau++;
            final int[] atb = new int[10];
            System.arraycopy(r, 3, atb, 0, 10);
            final List<String> loi = NganSachTrangBi.kiemMau(r[1], r[2], atb);
            if (!loi.isEmpty()) {
                truoc++;
                if (vd.length() < 900) {
                    vd.append("     id ").append(r[0]).append(" (o ").append(r[1]).append(", cap ").append(r[2]).append("): ")
                            .append(loi).append('\n');
                }
                // ap dung dung quy tac SQL (LEAST) roi kiem lai
                for (int i = 0; i < NganSachTrangBi.SO_CHI_SO; i++) {
                    atb[i] = Math.min(atb[i], NganSachTrangBi.tranMon(r[1], i, r[2], 0));
                }
                if (!NganSachTrangBi.kiemMau(r[1], r[2], atb).isEmpty()) {
                    sau++;
                }
            }
        }
        k.p("6. DATA_ITEM (sql/htbb_day_du.sql): %d dong trang bi nhan vat; vuot ngan sach TRUOC migration %d, SAU %d", soMau, truoc, sau);
        k.bao.append(vd);
        k.muc("Moi dong data_item trang bi nhan vat trong ngan sach sau du_lieu/sql_nguon/pokemon_chi_so.sql", soMau > 100 && sau == 0,
                soMau + " dong, truoc " + truoc + " vuot, sau " + sau);
        k.p("");
        k.p("MUC TIEU");
        int dat = 0;
        for (final String[] m : k.mucTieu) {
            k.p("  %s %s: %s", "1".equals(m[1]) ? "DAT  " : "KHONG", m[0], m[2]);
            dat += "1".equals(m[1]) ? 1 : 0;
        }
        k.p("  => %d/%d", dat, k.mucTieu.size());
        return k;
    }

    /** Hoi phuc /giay sau tran: hoi nhip (1%/5 giay) + hut (thu thach x10%, moi don <= 0,2% HP, 2,2 don/giay). */
    static double hoiSauTran(final double hp, final double myDps, final double hut, final boolean thuThach) {
        final double h = myDps * hut * (thuThach ? GioiHanChiSo.HUT_THU_THACH_PHAN_TRAM / 100.0 : 1.0);
        return hp * GioiHanChiSo.HOI_NHIP_TRAN / 1000.0 / 5.0 + Math.min(h, hp * GioiHanChiSo.HUT_MOI_DON_TRAN / 1000.0 * XOAY);
    }

    /** TTK PvP: don = coBan x 1,1 x 0,30 x he, tran 8% HP; 2,2 don/giay; tru giam, ne, hoi (hut cua doi thu dong dang). */
    static double pvpTtk(final double hp, final double atk, final double thu, final double cm, final double hsCm, final double he,
            final double giam, final double hut, final double ne) {
        final double don = Math.min(SatThuongLauDai.coBan((long) atk, (long) thu) * 1.1 * 0.30 * he,
                hp * SatThuongLauDai.PVP_TRAN_PHAN_TRAM_HP / 100.0);
        final double dps = don * XOAY * (1 + cm * (hsCm - 1)) * (1 - giam) * (1 - ne);
        final double rong = dps * (1 - Math.min(0.99, hut));
        return hp / Math.max(1e-9, rong);
    }

    /** {lon nhat bac moi +0 / bac cu +15} tren chi so chinh moi o. */
    static double[] moiTrenCu() {
        double m = 0;
        for (final int t : NganSachTrangBi.cacO()) {
            final int idx = NganSachTrangBi.chiSoChinh(t);
            for (int b = 1; b <= 10; b++) {
                final double cu = NganSachTrangBi.giaTriMoc(t, idx, b - 1) * NganSachTrangBi.heSoNangCap(15) / 100.0;
                if (cu > 0) {
                    m = Math.max(m, NganSachTrangBi.giaTriMoc(t, idx, b) / cu);
                }
            }
        }
        return new double[]{m};
    }

    /** THIET_KE 3.6 tren danh muc (don vi thiet ke): (goc + do bac b) / (goc + 1,55 x do bac b-1) lon nhat. */
    static double bacMoiTrenCuThietKe(final String goc, final Kq k) {
        final String csv = SinhSqlChiSo.doc(new File(goc, "du_lieu/noi_dung_trang_bi.csv").getPath());
        if (csv.isEmpty()) {
            return 0;
        }
        final String[] dong = csv.split("\n");
        final java.util.List<String> cot = java.util.Arrays.asList(dong[0].split(","));
        final int cBac = cot.indexOf("bac");
        final int cO = cot.indexOf("o");
        final int cDong = cot.indexOf("dong");
        final int cCap = cot.indexOf("cap_yeu_cau");
        final int cSo = cot.indexOf("so_o");
        final int[] cs = {cot.indexOf("tan_cong"), cot.indexOf("phong_thu"), cot.indexOf("hp")};
        final double[][] tong = new double[11][3];
        final int[] cap = new int[11];
        for (int i = 1; i < dong.length; i++) {
            final String[] f = dong[i].split(",", -1);
            if (f.length <= cs[2] || ("vu_khi".equals(f[cO]) && !"Eevee".equals(f[cDong]))) {
                continue;
            }
            final int b = Integer.parseInt(f[cBac]);
            cap[b] = Integer.parseInt(f[cCap]);
            for (int j = 0; j < 3; j++) {
                tong[b][j] += Double.parseDouble(f[cs[j]]) * Integer.parseInt(f[cSo]);
            }
        }
        double m = 0;
        final StringBuilder sb = new StringBuilder();
        for (int b = 1; b <= 10; b++) {
            final double tc = 30 + 12 * cap[b];
            final double[] g = {tc, 0.25 * tc, 70.0 * (25 + cap[b])};
            for (int j = 0; j < 3; j++) {
                final double r = (g[j] + tong[b][j]) / (g[j] + NganSachTrangBi.heSoNangCap(15) / 100.0 * tong[b - 1][j]);
                m = Math.max(m, r);
                sb.append(String.format(Locale.ROOT, "%.3f%s", r, j < 2 ? "/" : "  "));
            }
        }
        k.p("   THIET_KE 3.6 (tong tc/thu/HP, bac 1..10): %s", sb);
        return m;
    }

    static String hang(final String[] a, final String f) {
        final StringBuilder b = new StringBuilder();
        for (final String s : a) {
            b.append(String.format(Locale.ROOT, f, s));
        }
        return b.toString();
    }

    public static void main(final String[] args) throws Exception {
        final Kq k = chay(".");
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(RA), StandardCharsets.UTF_8))) {
            w.print(k.bao);
        }
        System.out.print(k.bao);
    }
}
