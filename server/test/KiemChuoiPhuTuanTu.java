import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuMau;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;
import real.NewCharQuest;
import real.NhiemVuEngine;
import real.NhiemVuLauDaiGame;

/**
 * Bao loi 05/11/2026: Truong lang Xatu (NPC) moi CUNG LUC buoc 1..4 cua mot chuoi nhiem vu phu ("Giọng hát khàn" ...
 * "Buổi diễn"). Danh sach NPC (-64 loai 0, NhiemVuLauDaiGame.locDanhSach / Char.getAllQuestCanReceive) chi loc cap + "da
 * dung"; cot {@code truoc} cua chuoi P-xx khong duoc kiem. Kiem:
 * <ul>
 * <li>chua lam gi: chi moi buoc 1; buoc 1 da tra (v[1] = 2): chi moi buoc 2; buoc 1 xong chua tra (v[1] = 1): khong moi buoc 2;</li>
 * <li>Char.OnQuest nhan buoc sau (goi cu / client sua): may chu tu choi, noi ro buoc truoc, khe phu van trong; nhan dung buoc
 * ke tiep: duoc;</li>
 * <li>nhiem vu khong co mau engine / khong co truoc: nhu cu (khong rang buoc);</li>
 * <li>du lieu that sql/htbb.sql: chuoi "Ca sĩ mất giọng" P-04.2.x co truoc tuan tu (moi buoc tro buoc truoc).</li>
 * </ul>
 */
public final class KiemChuoiPhuTuanTu {

    private KiemChuoiPhuTuanTu() {
    }

    static final int ID = 987_654_331;
    static final short ID0 = 31_901;
    static final String[] MA = {"P-KIEMTT.1", "P-KIEMTT.2", "P-KIEMTT.3", "P-KIEMTT.4"};
    static final String[] TEN = {"Giọng hát khàn", "Mật ong ngọt", "Trà chữa họng", "Buổi diễn"};

    static final class NguoiChoi extends Char {
        final List<io.Message> goi = new ArrayList<io.Message>();

        NguoiChoi(final int dbid) {
            super(null);
            charDBID = dbid;
            charname = "kiemchuoitt";
            isBot = -1;
            lvDetail = new real.LevelDetail();
            lvDetail.lv = 20;
            mainQuest = new NewCharQuest(true);
            mainQuest.state_quest = NewCharQuest.DONE;
            subQuest = new NewCharQuest(true);
            subQuest.id_quest = -1;
            clanQuestNew = new NewCharQuest(true);
            clanQuestNew.id_quest = -1;
        }

        @Override
        public void sendMessage(final io.Message m) {
            goi.add(m);
        }

        String chu() {
            final StringBuilder s = new StringBuilder();
            for (final io.Message m : goi) {
                s.append(new String(m.toByteArray(), StandardCharsets.UTF_8)).append('\n');
            }
            return s.toString();
        }
    }

    public static void chay() {
        Kiem.nhom("Chuoi nhiem vu phu: NPC chi moi buoc KE TIEP, may chu tu choi nhan buoc sau (bao loi 05/11/2026)");
        final boolean batCu = LauDai.bat();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        final java.util.Map<Short, data.InfoQuestTemplate> cu = new java.util.HashMap<Short, data.InfoQuestTemplate>(real.Map.allSubQuest);
        final ConcurrentHashMap<String, long[]> td = new ConcurrentHashMap<String, long[]>();
        try {
            LauDai.dat(true);
            real.Map.allSubQuest.clear();
            final List<NhiemVuMau> ds = new ArrayList<NhiemVuMau>();
            for (int i = 0; i < MA.length; i++) {
                real.Map.allSubQuest.put(Short.valueOf((short) (ID0 + i)), mauNpc(i));
                ds.add(mauEngine(i));
            }
            NhiemVuEngine.datKiem(ds, ID, td);
            danhSach(td);
            nhan(td);
            khongRangBuoc(td);
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            NhiemVuEngine.xoaKiem(ID);
            real.Map.allSubQuest.clear();
            real.Map.allSubQuest.putAll(cu);
            LauDai.dat(batCu);
            real.Map.gemTemplate = gemCu;
        }
        duLieuThat();
    }

    static data.InfoQuestTemplate mauNpc(final int i) {
        final data.InfoQuestTemplate t = new data.InfoQuestTemplate();
        t.id = (short) (ID0 + i);
        t.ma = MA[i];
        t.main_sub = 1;
        t.type = 0;
        t.lv = (short) (19 + i);
        t.idNpcRcv = 5;
        t.idNpcRes = 5;
        t.name = TEN[i];
        t.monsterKill = new Vector<Short>(Collections.singletonList(Short.valueOf((short) 16)));
        t.totalMonsKilled = new Vector<Short>(Collections.singletonList(Short.valueOf((short) 5)));
        t.itemget = new Vector<Short>();
        t.totalitemget = new Vector<Short>();
        return t;
    }

    static NhiemVuMau mauEngine(final int i) {
        final NhiemVuMau m = new NhiemVuMau();
        m.ma = MA[i];
        m.loai = "phu";
        m.ten = TEN[i];
        m.kieu = "ha_quai";
        m.mucTieuId = "16";
        m.soLuong = 5;
        m.cap = 19 + i;
        m.truoc = i == 0 ? "" : MA[i - 1];
        return m;
    }

    static List<Integer> moi(final Char p) {
        final List<Integer> r = new ArrayList<Integer>();
        for (final NewCharQuest q : NhiemVuLauDaiGame.locDanhSach(p)) {
            if (q.main_sub == 1) {
                r.add(Integer.valueOf(q.id_quest));
            }
        }
        return r;
    }

    static void danhSach(final ConcurrentHashMap<String, long[]> td) {
        td.clear();
        final NguoiChoi p = new NguoiChoi(ID);
        Kiem.bang("chua lam: NPC chi moi buoc 1 (truoc moi ca 4)", "[" + ID0 + "]", moi(p).toString());
        Kiem.bang("getAllQuestCanReceive cung vay", 1, p.getAllQuestCanReceive().size());
        td.put(MA[0], new long[]{1L, 1L, 0L, 0L}); // xong, chua tra
        Kiem.bang("buoc 1 xong chua tra: chua moi buoc 2", "[]", moi(p).toString());
        td.put(MA[0], new long[]{1L, 2L, 0L, 0L}); // da tra
        Kiem.bang("buoc 1 da tra: chi moi buoc 2", "[" + (ID0 + 1) + "]", moi(p).toString());
        td.put(MA[1], new long[]{5L, 2L, 0L, 0L});
        td.put(MA[2], new long[]{5L, 2L, 0L, 0L});
        Kiem.bang("buoc 1..3 da tra: chi moi buoc 4", "[" + (ID0 + 3) + "]", moi(p).toString());
        Kiem.dung("phuDuBuocTruoc(buoc 4) khi buoc 3 xong", NhiemVuEngine.phuDuBuocTruoc(p, MA[3]));
        td.remove(MA[2]);
        Kiem.sai("phuDuBuocTruoc(buoc 4) khi buoc 3 chua lam", NhiemVuEngine.phuDuBuocTruoc(p, MA[3]));
        Kiem.bang("ten buoc truoc thieu", TEN[2], NhiemVuEngine.tenBuocTruocThieu(p, MA[3]));
    }

    static void nhan(final ConcurrentHashMap<String, long[]> td) {
        td.clear();
        final NguoiChoi p = new NguoiChoi(ID);
        p.OnQuest((byte) 0, (short) (ID0 + 2), (byte) 1); // buoc 3 khi chua lam buoc 1
        Kiem.bang("nhan buoc 3 khi chua lam buoc 1: tu choi, khe phu trong", Integer.valueOf(-1), Integer.valueOf(p.subQuest.id_quest));
        Kiem.dung("... noi ro buoc truoc (\"" + TEN[1] + "\")", p.chu().contains("bước trước \"" + TEN[1] + "\""));
        td.put(MA[0], new long[]{1L, 2L, 0L, 0L});
        final NguoiChoi q = new NguoiChoi(ID);
        q.OnQuest((byte) 0, (short) (ID0 + 3), (byte) 1);
        Kiem.bang("buoc 1 da tra, nhan buoc 4: tu choi", Integer.valueOf(-1), Integer.valueOf(q.subQuest.id_quest));
        q.OnQuest((byte) 0, (short) (ID0 + 1), (byte) 1);
        Kiem.bang("buoc 1 da tra, nhan buoc 2: duoc", Integer.valueOf(ID0 + 1), Integer.valueOf(q.subQuest.id_quest));
    }

    static void khongRangBuoc(final ConcurrentHashMap<String, long[]> td) {
        td.clear();
        // nhan vat khong co tien do (charDBID 0: bot thu / kiem) - nhu cu
        final NguoiChoi o = new NguoiChoi(0);
        Kiem.bang("charDBID 0: khong rang buoc (nhu cu, ca 4 buoc)", 4, moi(o).size());
        // tat sv.lau_dai: khong rang buoc
        LauDai.dat(false);
        Kiem.dung("tat sv.lau_dai: phuDuBuocTruoc luon dung", NhiemVuEngine.phuDuBuocTruoc(new NguoiChoi(ID), MA[3]));
        LauDai.dat(true);
        // nhiem vu NPC khong co mau engine (ma la) -> khong rang buoc
        Kiem.dung("ma khong co mau engine: khong rang buoc", NhiemVuEngine.phuDuBuocTruoc(new NguoiChoi(ID), "P-KHONG-CO.9"));
    }

    /** Du lieu that: chuoi "Ca sĩ mất giọng" (P-04.2.x) - moi buoc tro buoc truoc (truoc la cot tu P-04.2.1). */
    static void duLieuThat() {
        final String s = DayDu.toanVan();
        for (int i = 2; i <= 4; i++) {
            final java.util.regex.Matcher mk = java.util.regex.Pattern.compile("\\('P-04\\.2\\." + i + "',\\d+,'phu',").matcher(s);
            final int k = mk.find() ? mk.start() : -1;
            Kiem.dung("data_quest_lau_dai co P-04.2." + i, k >= 0);
            if (k >= 0) {
                final String dong = s.substring(k, Math.min(s.length(), k + 400));
                Kiem.dung("P-04.2." + i + ": truoc = P-04.2." + (i - 1), dong.contains(",'P-04.2." + (i - 1) + "',"));
            }
        }
    }
}
