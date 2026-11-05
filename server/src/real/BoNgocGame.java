package real;

import com.htbb.pokemon.BoTrangBi;
import com.htbb.pokemon.CheTaoLogic;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangCapLauDai;
import com.htbb.pokemon.NgocHe;
import data.DatabaseBoNgoc;
import io.Message;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BO TRANG BI, NGOC HE, MOC NEO NANG CAP - phan GAME (thiet ke can bang 4.4, de xuat 6 cua ra soat loi choi). Logic
 * thuan o com.htbb.pokemon.BoTrangBi / NgocHe / NangCapLauDai; che tao o real.CheTaoGame; CSDL o data.DatabaseBoNgoc.
 *
 * <h3>Du lieu (nap luc khoi dong, nap lai khong can khoi dong lai)</h3>
 * data_noi_dung_bo, data_noi_dung_ngoc_he, data_noi_dung_che_tao, data_ngoc_he_lo, data_ngoc_he_khac, data_che_tao_bac,
 * data_nang_cap_moc (+ data_bat_bong / data_bat_loai cua BatPokemonGame). Nap lai: lenh admin "9" (AdminMenu) hoac tu
 * dong khi CHECKSUM TABLE cac bang doi (luong theo doi moi {@link #CHU_KY_MS}). Bang thieu / loi doc thi giu du lieu cu
 * (lan dau: mac dinh trong code - spec 4.4).
 *
 * <h3>Diem noi</h3>
 * <ul>
 * <li>ChiSoTrangBiGame.capNhat (moi lan mac / thao) -&gt; {@link #capNhat}: dem mon cung bac, thuong bo, ST he / tru khang
 *     cua ngoc.</li>
 * <li>ChiSoTrangBiGame.atb (chi so chinh mon dang mac) -&gt; {@link #phanTramNgocMon}; doHePhanTram / khangHe -&gt; ngoc;
 *     chiMangTho -&gt; chi mang bo; CharChiSo HP / tan cong -&gt; ChiSoTrangBiGame.thuongBoTong.</li>
 * <li>MapCheTao.doRequestItemInfo -&gt; {@link #guiMoTa} (dong bo / lo ngoc tren the chi tiet, TRUOC goi 21).</li>
 * <li>MapEpNgoc (dat mon vao o nang cap, sau moi lan thu) -&gt; {@link #guiNangCap} (ti le + pity + moc neo tren nut).</li>
 * </ul>
 *
 * <h3>Goi -120 TRANG_BI (client &gt;= {@link #BAN_CLIENT})</h3>
 * <pre>
 * S-&gt;C byte kieu:
 *   0 DONG_THEM  short idItem, byte n, n x (byte mau, UTF chu)          - dong them cho the chi tiet (gui TRUOC goi 21)
 *   1 NANG_CAP   short idItem, byte k (+ke), byte tiLe, byte pity, byte mocGiu, int xu, byte da
 *   2 CHE_TAO    (CheTaoGame) danh sach cong thuc
 *   3 CHE_TAO_KQ (CheTaoGame) ket qua
 *   4 KHAM       byte n mon, n x (short idItem, short idTemplate, short idIcon, UTF ten, byte mau, byte plus, byte dangMac, byte soLo,
 *                soLo x (byte he -1..17, byte cap, byte mai)); byte nTui, nTui x (byte he, byte cap, byte mai, short so);
 *                byte 10 x int gia(cap 1..10); int giaThao; int giaMai; byte heKhac[18]
 *   5 KET_QUA    byte loai (0 ok / 1 loi), UTF chu
 * C-&gt;S byte kieu:
 *   0 xin danh sach che tao; 1 che tao (short idTemplate, byte he, byte kieu) - CheTaoGame
 *   2 xin man khảm; 3 dat ngoc (short idItem, byte lo, byte he, byte cap: 0 = khảm moi cap 1, &gt;0 = lay tu tui,
 *   byte mai); 4 nang ngoc (short idItem, byte lo); 5 thao (short idItem, byte lo); 6 mai (short idItem, byte lo);
 *   7 phan ra ngoc trong tui (byte he, byte cap, byte mai) -&gt; Da nang cap = cap ngoc (bang phan_ra_da)
 * </pre>
 */
public final class BoNgocGame {

    public static final int LENH = -120;
    /** Ban client dau tien biet goi -120 / -121 (0.9.8; 0.9.8 da phat hanh khong co). */
    public static final int BAN_CLIENT = 908;

    public static final byte S_DONG_THEM = 0, S_NANG_CAP = 1, S_CHE_TAO = 2, S_CHE_TAO_KQ = 3, S_KHAM = 4, S_KET_QUA = 5;
    public static final byte C_XIN_CHE_TAO = 0, C_CHE_TAO = 1, C_XIN_KHAM = 2, C_DAT_NGOC = 3, C_NANG_NGOC = 4,
            C_THAO_NGOC = 5, C_MAI_NGOC = 6, C_PHAN_RA = 7;

    // ------------------------------------------------------------------ cau hinh (cau_hinh, ap dung ngay)

    public static final String K_BO_BAT = "bo.bat";
    public static final String K_BO_TRAN = "bo.tran_phan_tram";
    public static final String K_BO_TRAN_CHI_MANG = "bo.tran_chi_mang_phan_nghin";
    public static final String K_NGOC_BAT = "ngoc_he.bat";
    public static final String K_NGOC_TRAN_MON = "ngoc_he.tran_phan_tram_mon";
    public static final String K_NGOC_ST = "ngoc_he.st_he_phan_van_moi_cap";
    public static final String K_NGOC_KHANG = "ngoc_he.khang_tru_phan_nghin_moi_cap";
    public static final String K_NGOC_THAO = "ngoc_he.gia_thao_phan_nghin_gio";
    public static final String K_NGOC_MAI = "ngoc_he.gia_mai_phan_nghin_gio";
    public static final String K_MOC_NEO = "nang_cap.moc_neo";
    public static final String K_XU_BAC_CAO = "nang_cap.he_so_xu_bac_cao";

    /** Ten NPC cho thong bao. */
    static final String NPC = "Machamp thợ rèn";
    /** Id mau "Da nang cap" (Map.gemTemplate 8, data_noi_dung_nguyen_lieu ngoc:8). */
    public static final int ID_DA = 8;

    private BoNgocGame() {
    }

    static boolean bat() {
        return LauDai.bat() && ChiSoTrangBiGame.bat();
    }

    static boolean boBat() {
        return bat() && LauDai.so(K_BO_BAT, 1) != 0;
    }

    static boolean ngocBat() {
        return bat() && LauDai.so(K_NGOC_BAT, 1) != 0;
    }

    static boolean mocNeoBat() {
        return LauDai.so(K_MOC_NEO, 1) != 0;
    }

    // ================================================================== du lieu (snapshot bat bien)

    /** Mot ban chup du lieu; doi ca khoi khi nap lai. */
    static final class DuLieu {
        BoTrangBi bo = BoTrangBi.RONG;
        /** colorName (0..15) -&gt; so lo ngoc; -1 = khong khảm duoc (do trang tri). */
        final int[] loTheoMau = new int[16];
        final String[] tenBacMau = new String[16];
        /** he ngoc -&gt; he bi tru khang. */
        final int[] heKhac = new int[HePokemon.SO_HE];
        /** cap 1..10 -&gt; gio thu nhap / % chi so mon / da khi phan ra. */
        final double[] gio = new double[NgocHe.CAP_TOI_DA + 1];
        final int[] pct = new int[NgocHe.CAP_TOI_DA + 1];
        final int[] phanRaDa = new int[NgocHe.CAP_TOI_DA + 1];
        List<CheTaoLogic.CongThuc> congThuc = Collections.emptyList();
        List<CheTaoLogic.BacHiem> bacHiem = CheTaoLogic.bacMacDinh();
        int[] mocNeo = NangCapLauDai.MOC_NEO_MAC_DINH.clone();

        DuLieu() {
            java.util.Arrays.fill(this.loTheoMau, 1);
            this.loTheoMau[5] = -1; // vang = do trang tri
            final String[] ten = {"Thường", "Sử thi", "Quý", "Hiếm", "Sử thi", "Trang trí", "Huyền thoại", "Hiếm"};
            for (int i = 0; i < this.tenBacMau.length; i++) {
                this.tenBacMau[i] = i < ten.length ? ten[i] : "";
            }
            this.loTheoMau[1] = 2; // Hoan my = Su thi
            this.loTheoMau[2] = 2; // Do = Quy
            this.loTheoMau[4] = 2;
            this.loTheoMau[6] = 3;
            // Ngoc Lua -&gt; -khang Nuoc ... (mac dinh: he khac chinh, data_ngoc_he_khac)
            final int[] khac = {6, 2, 3, 8, 1, 1, 10, 8, 2, 3, 15, 1, 2, 15, 5, 6, 1, 16};
            System.arraycopy(khac, 0, this.heKhac, 0, khac.length);
            for (int c = 1; c <= NgocHe.CAP_TOI_DA; c++) {
                this.gio[c] = Math.pow(1.45, c - 1);
                this.pct[c] = c;
                this.phanRaDa[c] = c;
            }
        }
    }

    static volatile DuLieu DL = new DuLieu();
    static volatile String CHU_KY = "";
    public static final long CHU_KY_MS = 60_000L;
    private static Thread theoDoi;

    /** Dat du lieu (kiem thu). */
    static void datDuLieu(final DuLieu d) {
        DL = d;
    }

    /** Nap tu CSDL (giu du lieu cu cho bang loi doc). TeamServer goi sau khi nap template vat pham. */
    public static synchronized String nap() {
        final DuLieu cu = DL;
        final DuLieu d = new DuLieu();
        final StringBuilder bao = new StringBuilder();
        // bo trang bi
        final List<Object[]> bo = DatabaseBoNgoc.docBang("SELECT bac, ten_bo, vung, so_mon, chi_so, phan_tram, mo_ta FROM data_noi_dung_bo", 7);
        if (bo != null) {
            final List<BoTrangBi.Dong> v = new ArrayList<BoTrangBi.Dong>();
            for (final Object[] r : bo) {
                v.add(new BoTrangBi.Dong(DatabaseBoNgoc.so(r[0]), DatabaseBoNgoc.chu(r[1]), DatabaseBoNgoc.chu(r[2]),
                        DatabaseBoNgoc.so(r[3]), BoTrangBi.maChiSo(DatabaseBoNgoc.chu(r[4])), DatabaseBoNgoc.so(r[5]),
                        DatabaseBoNgoc.chu(r[6])));
            }
            d.bo = new BoTrangBi(v);
        } else {
            d.bo = cu.bo;
        }
        bao.append(d.bo.soDong()).append(" dòng bộ, ");
        // so lo theo mau
        final List<Object[]> lo = DatabaseBoNgoc.docBang("SELECT color_name, ten_bac, so_lo FROM data_ngoc_he_lo", 3);
        if (lo != null && !lo.isEmpty()) {
            for (final Object[] r : lo) {
                final int m = DatabaseBoNgoc.so(r[0]);
                if (m >= 0 && m < d.loTheoMau.length) {
                    d.loTheoMau[m] = Math.max(-1, Math.min(NgocHe.LO_TOI_DA, DatabaseBoNgoc.so(r[2])));
                    d.tenBacMau[m] = DatabaseBoNgoc.chu(r[1]);
                }
            }
        } else if (lo == null) {
            System.arraycopy(cu.loTheoMau, 0, d.loTheoMau, 0, d.loTheoMau.length);
        }
        final List<Object[]> khac = DatabaseBoNgoc.docBang("SELECT he, he_khac FROM data_ngoc_he_khac", 2);
        if (khac != null) {
            for (final Object[] r : khac) {
                final int he = DatabaseBoNgoc.so(r[0]);
                final int k = DatabaseBoNgoc.so(r[1]);
                if (HePokemon.hopLe(he) && HePokemon.hopLe(k)) {
                    d.heKhac[he] = k;
                }
            }
        }
        // ngoc he: gio thu nhap / % mon / phan ra (lay he 0 lam mau cho moi he - bang 18 he giong nhau theo cap)
        final List<Object[]> ng = DatabaseBoNgoc.docBang("SELECT cap, MAX(chi_so_phan_tram), MAX(gio_thu_nhap), MAX(phan_ra_da)"
                + " FROM data_noi_dung_ngoc_he GROUP BY cap", 4);
        if (ng != null && !ng.isEmpty()) {
            for (final Object[] r : ng) {
                final int c = DatabaseBoNgoc.so(r[0]);
                if (c >= 1 && c <= NgocHe.CAP_TOI_DA) {
                    d.pct[c] = Math.max(0, Math.min(NgocHe.TRAN_PHAN_TRAM_MON, DatabaseBoNgoc.so(r[1])));
                    d.gio[c] = Math.max(0.01, DatabaseBoNgoc.soThuc(r[2]));
                    d.phanRaDa[c] = Math.max(0, DatabaseBoNgoc.so(r[3]));
                }
            }
            bao.append(ng.size()).append(" cấp ngọc, ");
        }
        // che tao
        final List<CheTaoLogic.CongThuc> ct = CheTaoGame.napCongThuc();
        d.congThuc = ct != null ? ct : cu.congThuc;
        bao.append(d.congThuc.size()).append(" công thức, ");
        final List<Object[]> bac = DatabaseBoNgoc.docBang("SELECT ma, ten, color_name, phan_nghin, he_so_phan_tram, so_lo"
                + " FROM data_che_tao_bac ORDER BY ma", 6);
        if (bac != null && !bac.isEmpty()) {
            final List<CheTaoLogic.BacHiem> v = new ArrayList<CheTaoLogic.BacHiem>();
            for (final Object[] r : bac) {
                v.add(new CheTaoLogic.BacHiem(DatabaseBoNgoc.so(r[0]), DatabaseBoNgoc.chu(r[1]), DatabaseBoNgoc.so(r[2]),
                        DatabaseBoNgoc.so(r[3]), DatabaseBoNgoc.so(r[4]), DatabaseBoNgoc.so(r[5])));
            }
            d.bacHiem = Collections.unmodifiableList(v);
        } else if (bac == null) {
            d.bacHiem = cu.bacHiem;
        }
        final List<Object[]> moc = DatabaseBoNgoc.docBang("SELECT moc FROM data_nang_cap_moc ORDER BY moc", 1);
        if (moc != null && !moc.isEmpty()) {
            final int[] m = new int[moc.size()];
            for (int i = 0; i < m.length; i++) {
                m[i] = Math.max(1, Math.min(15, DatabaseBoNgoc.so(moc.get(i)[0])));
            }
            d.mocNeo = m;
        } else if (moc == null) {
            d.mocNeo = cu.mocNeo;
        }
        DL = d;
        final String bat = BatPokemonGame.nap();
        final String doMoi = DoMoiGame.nap() + "; " + TrangPhucGame.nap(); // [do-trang-phuc]
        final String kq = "[BO NGOC] " + bao + "mốc neo " + java.util.Arrays.toString(d.mocNeo) + "; " + bat + "; " + doMoi;
        System.out.println(kq);
        return kq;
    }

    /** Lenh admin / web: nap lai ngay. */
    public static String napLaiNgay(final String lyDo) {
        final String kq = nap();
        CHU_KY = DatabaseBoNgoc.chuKy();
        return "Đã nạp lại bộ / ngọc / chế tạo / thu phục (" + lyDo + "): " + kq;
    }

    /** TeamServer: tao bang thieu, nap, bat luong theo doi chu ky bang (nap lai khi doi). */
    public static synchronized void khoiDong() {
        DatabaseBoNgoc.taoBang();
        nap();
        CHU_KY = DatabaseBoNgoc.chuKy();
        if (theoDoi != null) {
            return;
        }
        theoDoi = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(CHU_KY_MS);
                        final String ky = DatabaseBoNgoc.chuKy();
                        if (!ky.isEmpty() && !ky.equals(CHU_KY)) {
                            CHU_KY = ky;
                            nap();
                        }
                    } catch (final InterruptedException e) {
                        return;
                    } catch (final Throwable t) {
                        System.out.println("[BO NGOC] loi theo doi: " + t);
                    }
                }
            }
        }, "BoNgoc-napLai");
        theoDoi.setDaemon(true);
        theoDoi.start();
    }

    // ================================================================== mon trang bi

    /** Mon tinh bo / khảm duoc: 11 o trang bi (type 0..12, 19), khong thoi trang, khong do trang tri (mau 5). */
    public static boolean laTrangBi(final Item it) {
        if (it == null) {
            return false;
        }
        final ItemTemplates t = it.getTemplate();
        if (t == null) {
            return false;
        }
        if (!(t.type >= 0 && t.type <= 12 || t.type == 19)) {
            return false;
        }
        if (t.atb != null && t.atb.length > 9 && t.atb[9] != 0) {
            return false;
        }
        return it.colorName != Item.COLOR_YELLOW;
    }

    /** Bac trang bi (0..10) theo cap mau. */
    static int bac(final Item it) {
        final ItemTemplates t = it.getTemplate();
        return NangCapLauDai.bacTheoCap(t != null ? t.level & 0xFF : it.level);
    }

    static NgocHe.Kham kham(final Item it) {
        return it == null ? new NgocHe.Kham() : NgocHe.giaiMa(it.ngocHe);
    }

    /** So lo ngoc dung duoc cua mon. */
    public static int soLo(final Item it) {
        if (!laTrangBi(it)) {
            return 0;
        }
        final DuLieu d = DL;
        final int m = it.colorName >= 0 && it.colorName < d.loTheoMau.length ? d.loTheoMau[it.colorName] : 1;
        if (m < 0) {
            return 0;
        }
        return NgocHe.soLo(m, kham(it).loThem);
    }

    static String tenBacHiem(final Item it) {
        final DuLieu d = DL;
        final String s = it.colorName >= 0 && it.colorName < d.tenBacMau.length ? d.tenBacMau[it.colorName] : "";
        return s == null ? "" : s;
    }

    /** % chi so chinh cua mon tu ngoc (0 khi tat); ChiSoTrangBiGame.atb nhan vao chi so da kep. */
    public static int phanTramNgocMon(final Item it) {
        if (it == null || it.ngocHe == null || it.ngocHe.isEmpty() || !ngocBat()) {
            return 0;
        }
        final int tran = Math.max(0, Math.min(NgocHe.TRAN_PHAN_TRAM_MON, LauDai.so(K_NGOC_TRAN_MON, NgocHe.TRAN_PHAN_TRAM_MON)));
        return NgocHe.phanTramMon(kham(it), soLo(it), DL.pct, tran);
    }

    // ================================================================== tinh theo nhan vat (moi lan mac / thao)

    /** Ket qua tinh cua mot nhan vat. */
    static final class TinhToan {
        final int[] bo = new int[BoTrangBi.SO_CHI_SO];
        final int[] stHePhanVan = new int[HePokemon.SO_HE];
        final int[] khangTruPhanVan = new int[HePokemon.SO_HE];
        final int[] demBac = new int[BoTrangBi.SO_BAC];
    }

    private static final ConcurrentHashMap<Integer, TinhToan> TT = new ConcurrentHashMap<Integer, TinhToan>();
    private static final java.util.Map<Char, TinhToan> TT_BOT = Collections.synchronizedMap(new java.util.WeakHashMap<Char, TinhToan>());

    static TinhToan tt(final Char p) {
        if (p == null) {
            return null;
        }
        return p.charDBID > 0 ? TT.get(Integer.valueOf(p.charDBID)) : TT_BOT.get(p);
    }

    /** ChiSoTrangBiGame.capNhat: tinh lai tu do dang mac (chi do co cap &lt;= cap nhan vat). */
    public static void capNhat(final Char p, final Vector<Item> ws) {
        if (p == null) {
            return;
        }
        final TinhToan t = new TinhToan();
        try {
            final List<Integer> bacMon = new ArrayList<Integer>();
            final DuLieu d = DL;
            final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
            final int stMoiCap = Math.max(0, Math.min(100, LauDai.so(K_NGOC_ST, NgocHe.ST_HE_PHAN_VAN_MOI_CAP)));
            final int khangMoiCap = Math.max(0, Math.min(10, LauDai.so(K_NGOC_KHANG, NgocHe.KHANG_TRU_MOI_CAP)));
            if (ws != null) {
                for (final Item it : ws) {
                    if (!laTrangBi(it) || it.level > capNv) {
                        continue;
                    }
                    bacMon.add(Integer.valueOf(bac(it)));
                    if (ngocBat() && it.ngocHe != null && !it.ngocHe.isEmpty()) {
                        final NgocHe.Kham k = kham(it);
                        final int n = soLo(it);
                        NgocHe.congStHe(k, n, stMoiCap, t.stHePhanVan);
                        NgocHe.congKhangTru(k, n, d.heKhac, khangMoiCap, t.khangTruPhanVan);
                    }
                }
            }
            final int[] b = new int[bacMon.size()];
            for (int i = 0; i < b.length; i++) {
                b[i] = bacMon.get(i).intValue();
            }
            final int[] dem = BoTrangBi.dem(b);
            System.arraycopy(dem, 0, t.demBac, 0, dem.length);
            if (boBat()) {
                final int tr = Math.max(0, Math.min(5, LauDai.so(K_BO_TRAN, 5))) * 10;
                final int trCm = Math.max(0, Math.min(30, LauDai.so(K_BO_TRAN_CHI_MANG, 30)));
                final int[] th = d.bo.thuong(dem, new int[]{tr, tr, trCm});
                // [do-moi] bo huy chuong vung (HP / tan cong / chi mang) cong CHUNG tran bo voi bo bac
                final com.htbb.pokemon.DoMoiLogic.KetQua ho = DoMoiGame.capNhat(p, ws);
                th[BoTrangBi.HP] = Math.min(tr, th[BoTrangBi.HP] + ho.hp);
                th[BoTrangBi.TAN_CONG] = Math.min(tr, th[BoTrangBi.TAN_CONG] + ho.tanCong);
                th[BoTrangBi.CHI_MANG] = Math.min(trCm, th[BoTrangBi.CHI_MANG] + ho.chiMang);
                System.arraycopy(th, 0, t.bo, 0, th.length);
            } else {
                DoMoiGame.capNhat(p, ws); // [do-moi] bo he (ST he / khang he) van tinh khi tat bo bac
            }
        } catch (final Exception e) {
            // du lieu mot mon hong: bo qua, khong lam hong tinh chi so
        }
        if (p.charDBID > 0) {
            TT.put(Integer.valueOf(p.charDBID), t);
        } else {
            TT_BOT.put(p, t);
        }
    }

    /** Thuong bo (phan nghin) dang co cua chi so {@code cs} (BoTrangBi.HP / TAN_CONG / CHI_MANG). */
    public static int boPhanNghin(final Char p, final int cs) {
        final TinhToan t = tt(p);
        return t == null || !boBat() || cs < 0 || cs >= t.bo.length ? 0 : t.bo[cs];
    }

    /** ST he {@code he} tu ngoc (phan nghin, chua kep: kep chung 20% o ChiSoTrangBiGame.doHePhanTram). */
    public static int stHePhanNghin(final Char p, final int he) {
        final TinhToan t = tt(p);
        return t == null || !ngocBat() || !HePokemon.hopLe(he) ? 0 : t.stHePhanVan[he] / 10;
    }

    /** Khang he {@code he} bi tru boi ngoc (phan nghin). */
    public static int khangTruPhanNghin(final Char p, final int he) {
        final TinhToan t = tt(p);
        return t == null || !ngocBat() || !HePokemon.hopLe(he) ? 0 : t.khangTruPhanVan[he] / 10;
    }

    /** Thoat game: bo cache chi so (tui ngoc giu lai cho lan luu cuoi - giong LauDaiNhanVat). */
    public static void khiThoat(final Char p) {
        if (p != null && p.charDBID > 0) {
            TT.remove(Integer.valueOf(p.charDBID));
        }
    }

    // ================================================================== tui ngoc (ho so)

    private static final ConcurrentHashMap<Integer, NgocHe.Tui> TUI = new ConcurrentHashMap<Integer, NgocHe.Tui>();

    /** Tui ngoc / pity che tao cua nhan vat; null neu bot / loi CSDL. */
    static NgocHe.Tui tui(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return null;
        }
        final Integer k = Integer.valueOf(p.charDBID);
        NgocHe.Tui t = TUI.get(k);
        if (t != null) {
            return t;
        }
        final String s = DatabaseBoNgoc.napHoSo(DatabaseBoNgoc.BANG_TUI_NGOC, p.charDBID);
        if (s == null) {
            return null;
        }
        t = NgocHe.Tui.giaiMa(s);
        final NgocHe.Tui cu = TUI.putIfAbsent(k, t);
        return cu != null ? cu : t;
    }

    /** Kiem thu: dat tui ngoc / pity che tao cho nhan vat (khong doc CSDL). */
    static void datTuiKiem(final int charDBID, final NgocHe.Tui t) {
        if (t == null) {
            TUI.remove(Integer.valueOf(charDBID));
        } else {
            TUI.put(Integer.valueOf(charDBID), t);
        }
    }

    /** Database.saveChar / saveCharAuto. */
    public static void luu(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        final NgocHe.Tui t = TUI.get(Integer.valueOf(p.charDBID));
        if (t != null && t.ban) {
            t.ban = false;
            if (!DatabaseBoNgoc.luuHoSo(DatabaseBoNgoc.BANG_TUI_NGOC, p.charDBID, t.maHoa())) {
                t.ban = true;
            }
        }
    }

    // ================================================================== goi tin

    static boolean coClient(final Char p) {
        return p != null && p.getSession() != null && p.getSession().banClient >= BAN_CLIENT;
    }

    static Message moi(final byte kieu) throws java.io.IOException {
        final Message m = new Message(LENH);
        m.dos.writeByte(kieu);
        return m;
    }

    static void ketQua(final Char p, final boolean ok, final String chu) {
        if (ok) {
            com.htbb.thongbao.ThongBao.ketQua(p, chu, "");
        } else {
            com.htbb.thongbao.ThongBao.loi(p, chu, "");
        }
        if (!coClient(p)) {
            return;
        }
        try {
            final Message m = moi(S_KET_QUA);
            m.dos.writeByte(ok ? 0 : 1);
            m.dos.writeUTF(chu);
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ dong them cho the chi tiet

    /** Dong mo ta bo + lo ngoc cua mon {@code it} ({@code chu} = nguoi dang giu / mac). {mau, chu}. */
    static List<Object[]> dongMoTa(final Char chu, final Item it) {
        final List<Object[]> ds = new ArrayList<Object[]>();
        if (!laTrangBi(it) || !bat()) {
            return ds;
        }
        final DuLieu d = DL;
        // ---- bo bac
        if (boBat()) {
            final int b = bac(it);
            final List<BoTrangBi.Dong> bo = d.bo.cuaBac(b);
            if (!bo.isEmpty()) {
                final TinhToan t = tt(chu);
                final int dem = t != null ? t.demBac[b] : 0;
                ds.add(new Object[]{Integer.valueOf(4), bo.get(0).ten + " (" + bo.get(0).vung + "): đang mặc " + dem + "/"
                        + d.bo.soMonDu(b) + " món cùng bậc"});
                for (final BoTrangBi.Dong x : bo) {
                    final boolean kich = dem >= x.soMon;
                    ds.add(new Object[]{Integer.valueOf(kich ? 1 : 0), x.soMon + " món: " + BoTrangBi.chuThuong(x)
                            + (kich ? " (đang kích)" : "")});
                }
            }
        }
        ds.addAll(DoMoiGame.dongMoTa(chu, it)); // [do-moi] bo ho
        // ---- lo ngoc
        if (ngocBat()) {
            final int n = soLo(it);
            if (n > 0) {
                final NgocHe.Kham k = kham(it);
                int co = 0;
                final StringBuilder lo = new StringBuilder();
                for (int i = 0; i < n; i++) {
                    final NgocHe.O o = k.o[i];
                    if (i > 0) {
                        lo.append(" · ");
                    }
                    if (o.trong()) {
                        lo.append("trống");
                    } else {
                        co++;
                        lo.append(tenNgoc(o.he, o.cap)).append(o.mai ? " (đã mài)" : "");
                    }
                }
                ds.add(new Object[]{Integer.valueOf(2), "Lỗ ngọc " + co + "/" + n + ": " + lo});
                if (co > 0) {
                    final int pct = phanTramNgocMon(it);
                    final int[] st = new int[HePokemon.SO_HE];
                    final int[] tru = new int[HePokemon.SO_HE];
                    NgocHe.congStHe(k, n, Math.max(0, LauDai.so(K_NGOC_ST, NgocHe.ST_HE_PHAN_VAN_MOI_CAP)), st);
                    NgocHe.congKhangTru(k, n, d.heKhac, Math.max(0, LauDai.so(K_NGOC_KHANG, NgocHe.KHANG_TRU_MOI_CAP)), tru);
                    final StringBuilder loi = new StringBuilder("Ngọc: +").append(pct).append("% chỉ số món");
                    for (int h = 0; h < HePokemon.SO_HE; h++) {
                        if (st[h] > 0) {
                            loi.append(" · +").append(phanVan(st[h])).append("‰ sát thương hệ ").append(HePokemon.TEN[h]);
                        }
                    }
                    ds.add(new Object[]{Integer.valueOf(2), loi.toString()});
                    final StringBuilder hai = new StringBuilder();
                    for (int h = 0; h < HePokemon.SO_HE; h++) {
                        if (tru[h] > 0) {
                            hai.append(hai.length() == 0 ? "Đổi lại: " : " · ").append("−").append(phanVan(tru[h]))
                                    .append("‰ kháng hệ ").append(HePokemon.TEN[h]);
                        }
                    }
                    if (hai.length() > 0) {
                        ds.add(new Object[]{Integer.valueOf(5), hai.toString()});
                    }
                } else {
                    ds.add(new Object[]{Integer.valueOf(0), "Khảm ngọc hệ tại " + NPC + "."});
                }
            }
        }
        return ds;
    }

    /** 45 phan van -&gt; "4,5". */
    static String phanVan(final int pv) {
        final int nguyen = pv / 10;
        final int le = Math.abs(pv % 10);
        return le == 0 ? String.valueOf(nguyen) : nguyen + "," + le;
    }

    static String tenNgoc(final int he, final int cap) {
        return "Ngọc " + HePokemon.ten(he) + " cấp " + cap;
    }

    /** MapCheTao.doRequestItemInfo: gui dong them TRUOC goi 21 cua mon {@code it} (chu = nguoi giu mon). */
    public static void guiMoTa(final Char xem, final Char chu, final Item it) {
        if (!coClient(xem) || it == null) {
            return;
        }
        try {
            final List<Object[]> ds = dongMoTa(chu != null ? chu : xem, it);
            final Message m = moi(S_DONG_THEM);
            m.dos.writeShort(it.id);
            final int n = Math.min(20, ds.size());
            m.dos.writeByte(n);
            for (int i = 0; i < n; i++) {
                m.dos.writeByte(((Integer) ds.get(i)[0]).intValue());
                m.dos.writeUTF((String) ds.get(i)[1]);
            }
            xem.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ nang cap: ti le + pity + moc neo

    /** Moc neo dang ap dung (rong khi tat nang_cap.moc_neo). */
    public static int[] mocNeo() {
        return mocNeoBat() ? DL.mocNeo : new int[0];
    }

    /** He so xu buoc +11..+15 (phan nghin; tat moc neo -&gt; 1000). */
    public static int heSoXuBacCao() {
        return mocNeoBat() ? Math.max(1000, Math.min(2000, LauDai.so(K_XU_BAC_CAO, NangCapLauDai.HE_SO_XU_BAC_CAO_MAC_DINH))) : 1000;
    }

    /** Gui ti le + pity + moc neo cua lan nang ke tiep cho nut "Dap" (ScreenDapDo). */
    public static void guiNangCap(final Char p, final Item it) {
        if (!coClient(p) || it == null || !LauDai.bat()) {
            return;
        }
        try {
            final Message m = moi(S_NANG_CAP);
            m.dos.writeShort(it.id);
            final int k = Math.min(15, it.plus + 1);
            final com.htbb.pokemon.HoSoLauDai h = LauDaiNhanVat.lay(p);
            final int truot = h != null ? h.truot(it.id) : 0;
            final int capDo = it.getTemplate() != null ? it.getTemplate().level & 0xFF : it.level;
            m.dos.writeByte(it.plus >= 15 ? 0 : k);
            m.dos.writeByte(it.plus >= 15 ? 0 : NangCapLauDai.tiLe(k, truot));
            m.dos.writeByte(it.plus >= 15 ? 0 : NangCapLauDai.pityCong(k, truot));
            m.dos.writeByte(NangCapLauDai.mocGiu(it.plus, mocNeo()));
            m.dos.writeInt((int) Math.min(Integer.MAX_VALUE, it.plus >= 15 ? 0 : NangCapLauDai.chiPhi(capDo, k, heSoXuBacCao())));
            m.dos.writeByte(it.plus >= 15 ? 0 : NangCapLauDai.da(k));
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== khảm ngoc (C-&gt;S -120 kieu 2..7)

    /** MapXuLyLenh case -120. */
    public static void xuLy(final Char p, final Message msg) {
        try {
            final byte kieu = msg.dis.readByte();
            switch (kieu) {
                case C_XIN_CHE_TAO:
                case C_CHE_TAO:
                    CheTaoGame.xuLy(p, kieu, msg);
                    return;
                case C_XIN_KHAM:
                    if (oMachamp(p)) {
                        guiKham(p);
                    }
                    return;
                case C_DAT_NGOC: {
                    final short id = msg.dis.readShort();
                    final int lo = msg.dis.readByte();
                    final int he = msg.dis.readByte();
                    final int cap = msg.dis.readByte();
                    int mai = 0;
                    try {
                        mai = msg.dis.readByte();
                    } catch (final Exception ignored) {
                    }
                    datNgoc(p, id, lo, he, cap, mai != 0);
                    return;
                }
                case C_NANG_NGOC:
                    nangNgoc(p, msg.dis.readShort(), msg.dis.readByte());
                    return;
                case C_THAO_NGOC:
                    thaoNgoc(p, msg.dis.readShort(), msg.dis.readByte());
                    return;
                case C_MAI_NGOC:
                    maiNgoc(p, msg.dis.readShort(), msg.dis.readByte());
                    return;
                case C_PHAN_RA: {
                    final int he = msg.dis.readByte();
                    final int cap = msg.dis.readByte();
                    int mai = 0;
                    try {
                        mai = msg.dis.readByte();
                    } catch (final Exception ignored) {
                    }
                    phanRa(p, he, cap, mai != 0);
                    return;
                }
                default:
                    return;
            }
        } catch (final Exception e) {
            // goi hong: bo qua
        }
    }

    /** Dang canh Machamp (hoac trong nha tho ren); khong thi bao. */
    static boolean oMachamp(final Char p) {
        if (!bat()) {
            ketQua(p, false, "Chức năng chưa mở.");
            return false;
        }
        if (!NpcTinhBanDo.oNoiDichVu(p, NpcTinhBanDo.NPC_THO_REN)) {
            ketQua(p, false, "Hãy đến gần " + NPC + ".");
            return false;
        }
        return true;
    }

    /** Mon cua nhan vat theo id (tui hoac dang mac). {mon, dangMac(Boolean)}. */
    static Object[] timMon(final Char p, final short id) {
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && it.id == id) {
                    return new Object[]{it, Boolean.FALSE};
                }
            }
        }
        final Item[] w = p.getArrayCharWearing();
        if (w != null) {
            for (final Item it : w) {
                if (it != null && it.id == id) {
                    return new Object[]{it, Boolean.TRUE};
                }
            }
        }
        return null;
    }

    static long xuMoiGio(final Char p) {
        return LauDai.xuMoiGio(p.lvDetail != null ? p.lvDetail.lv : 1);
    }

    static long giaCap(final Char p, final int cap) {
        return NgocHe.giaCap(cap, DL.gio, xuMoiGio(p));
    }

    static long giaThao(final Char p) {
        return NgocHe.giaGio(Math.max(0, LauDai.so(K_NGOC_THAO, NgocHe.GIA_THAO_PHAN_NGHIN_GIO)), xuMoiGio(p));
    }

    static long giaMai(final Char p) {
        return NgocHe.giaGio(Math.max(0, LauDai.so(K_NGOC_MAI, NgocHe.GIA_MAI_PHAN_NGHIN_GIO)), xuMoiGio(p));
    }

    /** Sau khi doi ngoc mot mon: luu, tinh lai chi so neu dang mac, gui lai the + man khảm. */
    static void sauKhiDoi(final Char p, final Item it, final boolean dangMac, final String bao, final String log) {
        if (dangMac) {
            try {
                p.calculateAttrib();
                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            } catch (final Exception ignored) {
            }
        }
        try {
            guiMoTa(p, p, it);
            p.sendMessage(MessageCreator.createMsgItemInfo(it));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception ignored) {
        }
        ketQua(p, true, bao);
        guiKham(p);
        try {
            data.Database.instance.saveOrtherLog("tob_log_other_kham", p.charname, log + " | " + it.getTemplate().name + " " + it.ngocHe, "ngoche");
        } catch (final Exception ignored) {
        }
    }

    static Item monKham(final Char p, final short id, final boolean[] dangMac) {
        final Object[] r = timMon(p, id);
        if (r == null) {
            ketQua(p, false, "Không tìm thấy trang bị.");
            return null;
        }
        final Item it = (Item) r[0];
        dangMac[0] = ((Boolean) r[1]).booleanValue();
        if (!laTrangBi(it) || soLo(it) <= 0) {
            ketQua(p, false, "Món này không có lỗ ngọc.");
            return null;
        }
        if (it.isSelling || Map.isItemSelled(p, it.id, it)) {
            ketQua(p, false, "Không thể khảm khi món đang được rao bán.");
            return null;
        }
        return it;
    }

    /** Dat ngoc vao lo trong: cap 0 = khảm moi ngoc cap 1 (tra gia cap 1), cap &gt; 0 = lay mot vien tu tui (mien phi). */
    static void datNgoc(final Char p, final short id, final int lo, final int he, final int cap, final boolean mai) {
        if (!oMachamp(p) || !ngocBat()) {
            return;
        }
        if (!HePokemon.hopLe(he) || cap < 0 || cap > NgocHe.CAP_TOI_DA) {
            ketQua(p, false, "Ngọc không hợp lệ.");
            return;
        }
        final boolean[] dangMac = {false};
        synchronized (p) {
            final Item it = monKham(p, id, dangMac);
            if (it == null) {
                return;
            }
            final NgocHe.Kham k = kham(it);
            if (lo < 0 || lo >= soLo(it) || !k.o[lo].trong()) {
                ketQua(p, false, "Lỗ này không trống.");
                return;
            }
            if (cap == 0) {
                final long gia = giaCap(p, 1);
                if (!p.truNeuDu(() -> p.xu >= gia, () -> p.subXu(gia, true, "kham ngoc he moi"))) {
                    ketQua(p, false, "Khảm ngọc mới cần " + gia + " xu.");
                    return;
                }
                k.o[lo] = new NgocHe.O(he, 1, false);
            } else {
                final NgocHe.Tui t = tui(p);
                if (t == null || !t.lay(he, cap, mai)) {
                    ketQua(p, false, "Túi ngọc không có " + tenNgoc(he, cap) + ".");
                    return;
                }
                k.o[lo] = new NgocHe.O(he, cap, mai);
            }
            it.ngocHe = NgocHe.maHoa(k);
            NhiemVuEngine.suKien(p, "kham_ngoc", "", 1);
            sauKhiDoi(p, it, dangMac[0], "Đã khảm " + tenNgoc(he, k.o[lo].cap) + ".", "dat lo " + lo + " he " + he + " cap " + cap);
        }
    }

    /** Nang ngoc trong lo len cap + 1 (tra gia cap moi). */
    static void nangNgoc(final Char p, final short id, final int lo) {
        if (!oMachamp(p) || !ngocBat()) {
            return;
        }
        final boolean[] dangMac = {false};
        synchronized (p) {
            final Item it = monKham(p, id, dangMac);
            if (it == null) {
                return;
            }
            final NgocHe.Kham k = kham(it);
            if (lo < 0 || lo >= soLo(it) || k.o[lo].trong()) {
                ketQua(p, false, "Lỗ này chưa có ngọc.");
                return;
            }
            final NgocHe.O o = k.o[lo];
            if (o.cap >= NgocHe.CAP_TOI_DA) {
                ketQua(p, false, "Ngọc đã đạt cấp " + NgocHe.CAP_TOI_DA + ".");
                return;
            }
            final long gia = giaCap(p, o.cap + 1);
            if (!p.truNeuDu(() -> p.xu >= gia, () -> p.subXu(gia, true, "nang ngoc he " + (o.cap + 1)))) {
                ketQua(p, false, "Nâng lên cấp " + (o.cap + 1) + " cần " + gia + " xu.");
                return;
            }
            o.cap++;
            it.ngocHe = NgocHe.maHoa(k);
            sauKhiDoi(p, it, dangMac[0], "Ngọc " + HePokemon.ten(o.he) + " lên cấp " + o.cap + ".", "nang lo " + lo + " -> " + o.cap);
        }
    }

    /** Thao ngoc (0,5 gio thu nhap): ngoc giu cap, vao tui ngoc. */
    static void thaoNgoc(final Char p, final short id, final int lo) {
        if (!oMachamp(p) || !ngocBat()) {
            return;
        }
        final boolean[] dangMac = {false};
        synchronized (p) {
            final Item it = monKham(p, id, dangMac);
            if (it == null) {
                return;
            }
            final NgocHe.Tui t = tui(p);
            if (t == null) {
                ketQua(p, false, "Túi ngọc tạm thời không đọc được, thử lại sau.");
                return;
            }
            final NgocHe.Kham k = kham(it);
            if (lo < 0 || lo >= NgocHe.LO_TOI_DA || k.o[lo].trong()) {
                ketQua(p, false, "Lỗ này chưa có ngọc.");
                return;
            }
            final long gia = giaThao(p);
            if (!p.truNeuDu(() -> p.xu >= gia, () -> p.subXu(gia, true, "thao ngoc he"))) {
                ketQua(p, false, "Tháo ngọc cần " + gia + " xu.");
                return;
            }
            final NgocHe.O o = k.o[lo];
            t.them(o.he, o.cap, o.mai, 1);
            k.o[lo] = new NgocHe.O();
            it.ngocHe = NgocHe.maHoa(k);
            sauKhiDoi(p, it, dangMac[0], "Đã tháo " + tenNgoc(o.he, o.cap) + " vào túi ngọc.", "thao lo " + lo);
        }
    }

    /** Mai ngoc (0,3 gio thu nhap): giam phan tru khang 50%, mot lan. */
    static void maiNgoc(final Char p, final short id, final int lo) {
        if (!oMachamp(p) || !ngocBat()) {
            return;
        }
        final boolean[] dangMac = {false};
        synchronized (p) {
            final Item it = monKham(p, id, dangMac);
            if (it == null) {
                return;
            }
            final NgocHe.Kham k = kham(it);
            if (lo < 0 || lo >= soLo(it) || k.o[lo].trong()) {
                ketQua(p, false, "Lỗ này chưa có ngọc.");
                return;
            }
            if (k.o[lo].mai) {
                ketQua(p, false, "Ngọc này đã được mài.");
                return;
            }
            final long gia = giaMai(p);
            if (!p.truNeuDu(() -> p.xu >= gia, () -> p.subXu(gia, true, "mai ngoc he"))) {
                ketQua(p, false, "Mài ngọc cần " + gia + " xu.");
                return;
            }
            k.o[lo].mai = true;
            it.ngocHe = NgocHe.maHoa(k);
            sauKhiDoi(p, it, dangMac[0], "Đã mài ngọc: phần trừ kháng giảm một nửa.", "mai lo " + lo);
        }
    }

    /** Phan ra mot vien trong tui -&gt; Da nang cap (bang phan_ra_da theo cap). */
    static void phanRa(final Char p, final int he, final int cap, final boolean mai) {
        if (!oMachamp(p) || !ngocBat()) {
            return;
        }
        synchronized (p) {
            final NgocHe.Tui t = tui(p);
            if (t == null || cap < 1 || cap > NgocHe.CAP_TOI_DA || !t.lay(he, cap, mai)) {
                ketQua(p, false, "Túi ngọc không có viên này.");
                return;
            }
            final int da = Math.max(1, DL.phanRaDa[cap]);
            try {
                if (p.listGemitem != null && p.listGemitem.length > ID_DA) {
                    themDa(p, da);
                }
            } catch (final Exception e) {
                t.them(he, cap, mai, 1); // tra lai neu khong them duoc da
                ketQua(p, false, "Không phân rã được, thử lại sau.");
                return;
            }
            ketQua(p, true, "Phân rã " + tenNgoc(he, cap) + ": +" + da + " Đá nâng cấp.");
            try {
                p.sendMessage(MessageCreator.createCharGemItem(p));
            } catch (final Exception ignored) {
            }
            guiKham(p);
        }
    }

    /** Them {@code n} Da nang cap (ngoc 8, loai thuong) vao tui nguyen lieu. */
    static void themDa(final Char p, final int n) {
        if (p.listGemitem[ID_DA] == 0 && p.listGemitemLock[ID_DA] == 0) {
            final data.GemItem ge = new data.GemItem(ID_DA);
            ge.ownerId = p.charDBID;
            ge.realId = p.getIDItem();
            p.gemItem.add(ge);
        }
        p.listGemitem[ID_DA] += n;
        p.allGemGet[ID_DA] += n;
    }

    /** Man khảm: mon trong tui + dang mac co lo, tui ngoc, bang gia. */
    static void guiKham(final Char p) {
        if (!coClient(p)) {
            return;
        }
        try {
            final List<Object[]> mon = new ArrayList<Object[]>();
            final Item[] w = p.getArrayCharWearing();
            if (w != null) {
                for (final Item it : w) {
                    if (it != null && soLo(it) > 0) {
                        mon.add(new Object[]{it, Boolean.TRUE});
                    }
                }
            }
            synchronized (p.iItems) {
                for (final Item it : p.iItems) {
                    if (it != null && soLo(it) > 0 && !it.isSelling) {
                        mon.add(new Object[]{it, Boolean.FALSE});
                    }
                }
            }
            final Message m = moi(S_KHAM);
            final DataOutputStream o = m.dos;
            final int n = Math.min(60, mon.size());
            o.writeByte(n);
            for (int i = 0; i < n; i++) {
                final Item it = (Item) mon.get(i)[0];
                final NgocHe.Kham k = kham(it);
                final int lo = soLo(it);
                o.writeShort(it.id);
                o.writeShort(it.getTemplate().id);
                o.writeShort(it.getTemplate().idIcon);
                o.writeUTF(it.getTemplate().name + ChiSoTrangBiGame.tenHePhienDa(it));
                o.writeByte(it.colorName);
                o.writeByte(it.plus);
                o.writeByte(((Boolean) mon.get(i)[1]).booleanValue() ? 1 : 0);
                o.writeByte(lo);
                for (int j = 0; j < lo; j++) {
                    final NgocHe.O x = k.o[j];
                    o.writeByte(x.trong() ? -1 : x.he);
                    o.writeByte(x.trong() ? 0 : x.cap);
                    o.writeByte(x.mai ? 1 : 0);
                }
            }
            final NgocHe.Tui t = tui(p);
            final int[][] tuiDs = t == null ? new int[0][] : t.danhSach();
            final int nt = Math.min(120, tuiDs.length);
            o.writeByte(nt);
            for (int i = 0; i < nt; i++) {
                o.writeByte(tuiDs[i][0]);
                o.writeByte(tuiDs[i][1]);
                o.writeByte(tuiDs[i][2]);
                o.writeShort(Math.min(Short.MAX_VALUE, tuiDs[i][3]));
            }
            o.writeByte(NgocHe.CAP_TOI_DA);
            for (int c = 1; c <= NgocHe.CAP_TOI_DA; c++) {
                o.writeInt((int) Math.min(Integer.MAX_VALUE, giaCap(p, c)));
            }
            o.writeInt((int) Math.min(Integer.MAX_VALUE, giaThao(p)));
            o.writeInt((int) Math.min(Integer.MAX_VALUE, giaMai(p)));
            final int[] hk = DL.heKhac;
            o.writeByte(hk.length);
            for (final int x : hk) {
                o.writeByte(x);
            }
            p.sendMessage(m);
        } catch (final Exception e) {
            System.out.println("BoNgocGame.guiKham: " + e);
        }
    }

    /** Menu Machamp "Chế tạo & khảm ngọc": client moi mo man; client cu: bao cap nhat. */
    public static void moTuNpc(final Char p, final int muc) {
        if (!coClient(p)) {
            ketQua(p, false, "Cần cập nhật game bản 0.9.8 để dùng Chế tạo và Khảm ngọc hệ.");
            return;
        }
        if (!oMachamp(p)) {
            return;
        }
        if (muc == 0) {
            CheTaoGame.guiDanhSach(p);
        } else {
            guiKham(p);
        }
    }

    /** Ten muc trong menu Machamp tho ren (RequestNPCInfoHandler case 2) va menu con. */
    public static final String TEN_MUC = "Chế tạo & khảm ngọc";
    public static final String[] MENU_CON = {"Chế tạo trang bị", "Khảm ngọc hệ"};
    /** Npc ao menu con (grep moi worktree 20/10/2026: -520..-529, -540..-549, -561 dang dung; -580..-589 trong). */
    public static final int NPC_ID = -580;

    /** Vi tri muc "Che tao & kham ngoc" trong menu Machamp (dat boi {@link #themMucThoRen}); -1 = chua co. */
    public static volatile int viTriThoRen = -1;

    /** Noi muc vao CUOI menu Machamp tho ren (chi khi bat); ghi lai vi tri cho MapDoMenu. */
    public static String[] themMucThoRen(final String[] menu) {
        if (!bat() || menu == null) {
            return menu;
        }
        final String[] m = java.util.Arrays.copyOf(menu, menu.length + 1);
        m[menu.length] = TEN_MUC;
        viTriThoRen = menu.length;
        return m;
    }

    public static void moMenu(final Char p) {
        p.sendMessage(MessageCreator.createMsgMenu(MENU_CON, NPC_ID, 0));
    }

    public static void doMenu(final Char p, final int idMenu, final int idOption) {
        if (idMenu == 0 && idOption >= 0 && idOption < MENU_CON.length) {
            moTuNpc(p, idOption);
        }
    }
}
