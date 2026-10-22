package real;

import com.htbb.pokemon.DoMoiLogic;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangCapLauDai;
import data.DatabaseBoNgoc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DO HO MOI - phan GAME (logic thuan com.htbb.pokemon.DoMoiLogic). Nap data_trang_bi_ho / _thuong / data_trang_bi_moi
 * (sql/them_do_trang_phuc.sql) cung luc BoNgocGame.nap (khoi dong, lenh admin 9, CHECKSUM doi).
 *
 * <p>Moc vao game:
 * <ul>
 *   <li>roi: Monster (quai thuong + boss) goi {@link #thayKhiRoi} ngay sau khi tao mon - THAY template, giu chi so /
 *       do hiem / so mon (khong nguon vao moi);</li>
 *   <li>che tao: CheTaoGame.taoMon goi {@link #thayCheTao} (vong co -&gt; bo he cua he da chon, phien da -&gt; bo vung);</li>
 *   <li>chi so: BoNgocGame.capNhat goi {@link #capNhat}; ST he / khang he qua ChiSoTrangBiGame (tran 20% / 60%), bo vung
 *       cong chung tran bo 5% / chi mang bo 3% trong BoNgocGame;</li>
 *   <li>the chi tiet: BoNgocGame.dongMoTa them dong bo ho ({@link #dongMoTa}).</li>
 * </ul>
 * Tat: khoa do_moi.bat = 0 (khong doi mon roi, khong thuong bo ho; mon ho da co van la do thuong cung chi so).
 */
public final class DoMoiGame {

    public static final String K_BAT = "do_moi.bat";

    private static volatile DoMoiLogic.DuLieu DL = DoMoiLogic.RONG;

    private static final ConcurrentHashMap<Integer, DoMoiLogic.KetQua> KQ = new ConcurrentHashMap<Integer, DoMoiLogic.KetQua>();
    private static final java.util.Map<Char, DoMoiLogic.KetQua> KQ_BOT =
            Collections.synchronizedMap(new java.util.WeakHashMap<Char, DoMoiLogic.KetQua>());

    private DoMoiGame() {
    }

    public static boolean bat() {
        return LauDai.so(K_BAT, 1) != 0;
    }

    public static DoMoiLogic.DuLieu duLieu() {
        return DL;
    }

    /** Cho test / cong cu: dat du lieu truc tiep. */
    public static void datDuLieu(final DoMoiLogic.DuLieu d) {
        DL = d == null ? DoMoiLogic.RONG : d;
    }

    /** Bang may chu tu tao neu thieu (RONG) - data.DatabaseBoNgoc.taoBang chay. */
    public static final String[] TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS data_trang_bi_ho (ma VARCHAR(24) NOT NULL, ten VARCHAR(64) NOT NULL DEFAULT '',"
            + " loai VARCHAR(8) NOT NULL DEFAULT 'he', he TINYINT NOT NULL DEFAULT -1, he_khac TINYINT NOT NULL DEFAULT -1,"
            + " vung VARCHAR(8) NOT NULL DEFAULT '', bac_tu TINYINT NOT NULL DEFAULT 0, bac_den TINYINT NOT NULL DEFAULT 10,"
            + " mo_ta VARCHAR(255) NOT NULL DEFAULT '', thu_tu INT NOT NULL DEFAULT 0, bat TINYINT NOT NULL DEFAULT 1,"
            + " PRIMARY KEY (ma)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_trang_bi_ho_thuong (ma_ho VARCHAR(24) NOT NULL, so_mon TINYINT NOT NULL,"
            + " chi_so VARCHAR(16) NOT NULL, phan_nghin INT NOT NULL DEFAULT 0, mo_ta VARCHAR(128) NOT NULL DEFAULT '',"
            + " PRIMARY KEY (ma_ho, so_mon, chi_so)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_trang_bi_moi (id_item INT NOT NULL, ma_ho VARCHAR(24) NOT NULL, type TINYINT NOT NULL,"
            + " bac TINYINT NOT NULL, id_goc INT NOT NULL, ten VARCHAR(64) NOT NULL DEFAULT '', icon INT NOT NULL DEFAULT 0,"
            + " nguon VARCHAR(64) NOT NULL DEFAULT '', PRIMARY KEY (id_item)) DEFAULT CHARSET=utf8",
    };

    /** BoNgocGame.nap goi (sau khi nap template vat pham). Bang loi / thieu: giu du lieu cu. */
    public static synchronized String nap() {
        final List<Object[]> ho = DatabaseBoNgoc.docBang("SELECT ma, ten, loai, he, he_khac, vung FROM data_trang_bi_ho WHERE bat = 1", 6);
        final List<Object[]> th = DatabaseBoNgoc.docBang("SELECT ma_ho, so_mon, chi_so, phan_nghin, mo_ta FROM data_trang_bi_ho_thuong", 5);
        final List<Object[]> mon = DatabaseBoNgoc.docBang("SELECT id_item, ma_ho, type, bac, id_goc, ten FROM data_trang_bi_moi", 6);
        if (ho == null || th == null || mon == null) {
            return "[DO MOI] giữ dữ liệu cũ (bảng lỗi)";
        }
        final DoMoiLogic.DuLieu d = new DoMoiLogic.DuLieu();
        for (final Object[] r : ho) {
            d.themHo(new DoMoiLogic.Ho(DatabaseBoNgoc.chu(r[0]), DatabaseBoNgoc.chu(r[1]),
                    "vung".equals(DatabaseBoNgoc.chu(r[2])) ? DoMoiLogic.LOAI_VUNG : DoMoiLogic.LOAI_HE, DatabaseBoNgoc.so(r[3]),
                    DatabaseBoNgoc.so(r[4]), DatabaseBoNgoc.chu(r[5])));
        }
        for (final Object[] r : th) {
            d.themThuong(DatabaseBoNgoc.chu(r[0]), new DoMoiLogic.Thuong(DatabaseBoNgoc.so(r[1]),
                    DoMoiLogic.maCs(DatabaseBoNgoc.chu(r[2])), DatabaseBoNgoc.so(r[3]), DatabaseBoNgoc.chu(r[4])));
        }
        int coMau = 0;
        for (final Object[] r : mon) {
            final int id = DatabaseBoNgoc.so(r[0]);
            if (mau(id) == null) {
                continue; // template chua nap (chua chay them_do_trang_phuc.sql phan data_item): bo
            }
            coMau++;
            d.themMon(id, DatabaseBoNgoc.chu(r[1]), DatabaseBoNgoc.so(r[2]), DatabaseBoNgoc.so(r[3]), DatabaseBoNgoc.so(r[4]),
                    DatabaseBoNgoc.chu(r[5]));
        }
        DL = d;
        return "[DO MOI] " + d.ho.size() + " bộ, " + coMau + "/" + mon.size() + " món họ";
    }

    static ItemTemplates mau(final int id) {
        try {
            return Map.itemTemplates.get(5).get(Integer.valueOf(id));
        } catch (final Exception e) {
            return null;
        }
    }

    /** Doi template cua mon {@code it} sang {@code idMoi}: giu chi so / do hiem; cap yeu cau khong thap hon mon goc. */
    static boolean doiMau(final Item it, final int idMoi) {
        final ItemTemplates t = mau(idMoi);
        if (it == null || t == null) {
            return false;
        }
        it.tempateID = idMoi;
        it.level = (short) Math.max(it.level, t.level & 0xFF);
        return true;
    }

    /** Monster: mon trang bi vua roi (quai thuong / boss) -&gt; co the doi sang mon ho theo he cua quai. */
    public static void thayKhiRoi(final Item it, final LiveActor quai, final boolean boss) {
        if (it == null || !bat() || DL.mon.isEmpty()) {
            return;
        }
        try {
            final ItemTemplates t = it.getTemplate();
            if (t == null || DL.mon(t.id) != null) {
                return;
            }
            final int bac = NangCapLauDai.bacTheoCap(t.level & 0xFF);
            final int he = quai != null ? quai.hePokemon1() : -1;
            final int id = DoMoiLogic.bienThe(DL, t.type, bac, he, boss, Map.r.nextInt(100));
            if (id > 0) {
                doiMau(it, id);
            }
        } catch (final Exception e) {
            // loi: giu mon goc
        }
    }

    /** CheTaoGame.taoMon: vong co -&gt; bo he cua he chon (luon, neu bac co bo); phien da -&gt; bo vung cua bac. */
    public static void thayCheTao(final Item it, final ItemTemplates t, final int he) {
        if (it == null || t == null || !bat() || DL.mon.isEmpty()) {
            return;
        }
        final int bac = NangCapLauDai.bacTheoCap(t.level & 0xFF);
        int id = -1;
        if (t.type == 9 && HePokemon.hopLe(he)) {
            id = DL.bien(t.type, bac, he);
        } else if (t.type == 12) {
            id = DL.bien(t.type, bac, -1);
        }
        if (id > 0) {
            doiMau(it, id);
        }
    }

    // ------------------------------------------------------------------ chi so bo ho

    /** BoNgocGame.capNhat: tinh lai bo ho tu do dang mac (chi do cap &lt;= cap nhan vat). */
    public static DoMoiLogic.KetQua capNhat(final Char p, final Vector<Item> ws) {
        final List<Integer> ids = new ArrayList<Integer>();
        if (ws != null && bat()) {
            final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
            for (final Item it : ws) {
                if (it != null && BoNgocGame.laTrangBi(it) && it.level <= capNv) {
                    ids.add(Integer.valueOf(it.tempateID));
                }
            }
        }
        final int[] a = new int[ids.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = ids.get(i).intValue();
        }
        final DoMoiLogic.KetQua k = DoMoiLogic.tinh(DL, a);
        if (p.charDBID > 0) {
            KQ.put(Integer.valueOf(p.charDBID), k);
        } else {
            KQ_BOT.put(p, k);
        }
        return k;
    }

    static DoMoiLogic.KetQua kq(final Char p) {
        if (p == null) {
            return null;
        }
        return p.charDBID > 0 ? KQ.get(Integer.valueOf(p.charDBID)) : KQ_BOT.get(p);
    }

    /** ST he {@code he} tu bo ho (phan nghin; kep chung 20% o ChiSoTrangBiGame.doHePhanTram). */
    public static int stHePhanNghin(final Char p, final int he) {
        final DoMoiLogic.KetQua k = kq(p);
        return k == null || !bat() || !HePokemon.hopLe(he) ? 0 : k.stHe[he];
    }

    /** Khang he {@code he} tu bo ho (phan nghin; vao tran khang he 60%). */
    public static int khangHePhanNghin(final Char p, final int he) {
        final DoMoiLogic.KetQua k = kq(p);
        return k == null || !bat() || !HePokemon.hopLe(he) ? 0 : k.khangHe[he];
    }

    public static void khiThoat(final Char p) {
        if (p != null && p.charDBID > 0) {
            KQ.remove(Integer.valueOf(p.charDBID));
        }
    }

    // ------------------------------------------------------------------ the chi tiet

    /** Dong bo ho cho the chi tiet mon {@code it}: {mau, chu} giong BoNgocGame.dongMoTa. */
    static List<Object[]> dongMoTa(final Char chu, final Item it) {
        final List<Object[]> ds = new ArrayList<Object[]>();
        if (it == null || !bat()) {
            return ds;
        }
        final DoMoiLogic.Mon m = DL.mon(it.tempateID);
        if (m == null) {
            return ds;
        }
        final DoMoiLogic.KetQua k = kq(chu);
        final int dem = k != null ? k.dem(m.ho.ma) : 0;
        ds.add(new Object[]{Integer.valueOf(4), m.ho.ten + ": đang mặc " + dem + "/" + m.ho.soMonDu() + " món họ"});
        for (final DoMoiLogic.Thuong t : m.ho.thuong) {
            final boolean kich = dem >= t.soMon;
            ds.add(new Object[]{Integer.valueOf(kich ? 1 : 0), DoMoiLogic.chuThuong(m.ho, t) + (kich ? " (đang kích)" : "")});
        }
        return ds;
    }
}
