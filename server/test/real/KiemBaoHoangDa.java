package real;

import com.htbb.pokemon.BaoHoangDa;
import com.htbb.pokemon.ChonLoaiHoangDa;
import com.htbb.pokemon.LoaiDuLieu;
import com.htbb.pokemon.TuDongKeHoach;
import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Bao loi 12/11/2026 (ruby?, "Ad check map Mahogany, báo ra Pokémon để bắt mà tìm chả thấy con nào"): tin Pokemon hoang da
 * ghi khu + huong, T5 toan may chu chi toi nguoi choi that cung phe o noi khac, con T4+ / sac mau danh cho nguoi choi
 * (bot HLV AI: 0 sat thuong, khong gianh giao chien, nem bong bi tu choi, Tu dong khong tu chon), song 10 phut.
 * Goi tu Kiem.java qua {@link #BAO}.
 */
public final class KiemBaoHoangDa {

    private KiemBaoHoangDa() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void dung(final String m, final boolean v) {
        BAO.bang(m, Boolean.TRUE, Boolean.valueOf(v));
    }

    static void bang(final String m, final Object a, final Object b) {
        BAO.bang(m, a, b);
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    public static void chay() {
        try {
            thuan();
            quai();
            thongBao();
            tuDong();
            nguon();
        } catch (final Throwable e) {
            e.printStackTrace();
            dung("KiemBaoHoangDa chay het khong loi: " + e, false);
        }
    }

    // ------------------------------------------------------------------ logic thuan

    static void thuan() {
        bang("noi: ban do chia khu ghi khu + huong", "Mahogany khu 3, phía đông bắc", BaoHoangDa.noi("Mahogany", 3, "phía đông bắc"));
        bang("noi: ban do khong chia khu", "Mahogany", BaoHoangDa.noi("Mahogany", 0, ""));
        bang("noi: khong biet ten", "bãi hoang dã khu 1", BaoHoangDa.noi("", 1, null));
        bang("huong: goc tay bac", "phía tây bắc", BaoHoangDa.huong(16, 16, 90, 60));
        bang("huong: goc dong nam", "phía đông nam", BaoHoangDa.huong(89 * 16, 59 * 16, 90, 60));
        bang("huong: giua", "giữa bản đồ", BaoHoangDa.huong(45 * 16, 30 * 16, 90, 60));
        bang("huong: dong bac", "phía đông bắc", BaoHoangDa.huong(80 * 16, 5 * 16, 90, 60));
        bang("huong: ngoai ban do kep vao bien", "phía đông", BaoHoangDa.huong(500 * 16, 30 * 16, 90, 60));
        bang("huong: khong ro kich thuoc", "", BaoHoangDa.huong(10, 10, 0, 0));
        dung("danh cho nguoi: T4", BaoHoangDa.danhChoNguoi(147, LoaiDuLieu.T4, false, BaoHoangDa.GIU_BAC));
        dung("danh cho nguoi: T5", BaoHoangDa.danhChoNguoi(147, LoaiDuLieu.T5, false, BaoHoangDa.GIU_BAC));
        dung("danh cho nguoi: sac mau T1", BaoHoangDa.danhChoNguoi(19, LoaiDuLieu.T1, true, BaoHoangDa.GIU_BAC));
        dung("T3 thuong: bot van bat", !BaoHoangDa.danhChoNguoi(65, LoaiDuLieu.T3, false, BaoHoangDa.GIU_BAC));
        dung("quai thuong (dex 0): khong", !BaoHoangDa.danhChoNguoi(0, LoaiDuLieu.T5, true, BaoHoangDa.GIU_BAC));
        bang("song: con thuong 4 phut", 4, BaoHoangDa.phutBienMat(false, 4, BaoHoangDa.PHUT_BIEN_MAT_HIEM));
        bang("song: con danh cho nguoi 10 phut", 10, BaoHoangDa.phutBienMat(true, 4, BaoHoangDa.PHUT_BIEN_MAT_HIEM));
        bang("tin hoang da", "Một Dratini hoang dã (Rất hiếm) vừa xuất hiện ở Mahogany khu 2, phía tây - bắt được bằng Poké Ball!",
                BaoHoangDa.tinHoangDa("Dratini", "Rất hiếm", "Mahogany khu 2, phía tây"));
        bang("tin sac mau", "Một Dratini sắc màu vừa xuất hiện ở Mahogany khu 2, phía tây!",
                BaoHoangDa.tinShiny("Dratini", "Mahogany khu 2, phía tây"));
    }

    // ------------------------------------------------------------------ quai / nguoi

    static Map banDo(final int id, final int nRegion) {
        final Map m = new Map();
        m.initKhoQuaiVaNguoi();
        m.mapId = id;
        m.nRegion = (byte) nRegion;
        m.w = 90;
        m.h = 60;
        return m;
    }

    static Monster quai(final Map map, final int region, final int dex, final int bac, final boolean shiny) {
        final Monster m = new Monster((byte) 1);
        m.id = (short) 501;
        m.map = map;
        m.region = region;
        m.inCountry = 0;
        m.dexRieng = dex;
        m.bacRieng = bac;
        m.shinyRieng = shiny;
        m.default_x = 80 * 16;
        m.default_y = 5 * 16;
        m.x = 10;
        m.y = 10;
        m.maxhp = 1000;
        m.hp = 1000;
        return m;
    }

    static final class Nguoi {
        final Char p;
        final List<Message> goi = new ArrayList<Message>();

        Nguoi(final int ban, final boolean bot, final Map map, final int region, final int phe) {
            if (Map.gemTemplate == null) {
                Map.gemTemplate = new GemTemplate[0];
            }
            p = new Char(null);
            final Session s = new Session(null, null, true) {
                @Override
                public void sendMessage(final Message m) {
                    goi.add(m);
                }
            };
            s.banClient = ban;
            p.setSession(s);
            p.charClass = 1;
            p.isBot = -1;
            p.laHlvAi = bot;
            p.map = map;
            p.region = region;
            p.inCountry = (byte) phe;
            p.charDBID = 900000 + (int) (Math.random() * 90000);
        }

        /** Chu cac goi thong bao (lenh 38). */
        List<String> tin() {
            final List<String> ra = new ArrayList<String>();
            for (final Message m : goi) {
                if (m.command != 38) {
                    continue;
                }
                try {
                    final byte[] b = m.toByteArray();
                    ra.add(new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3)).readUTF());
                } catch (final Exception e) {
                    ra.add("?");
                }
            }
            return ra;
        }
    }

    static void quai() {
        final Map mah = banDo(28, 5);
        final Monster m = quai(mah, 2, 147, LoaiDuLieu.T5, false);
        dung("noi(quai): khu 3 (region 2) + huong theo diem sinh (khong theo cho vua chet)",
                LoaiQuaiGame.noi(m).endsWith(" khu 3, phía đông bắc"));
        final Map pallet = banDo(1, 0);
        dung("noi(quai): ban do khong chia khu khong ghi khu", !LoaiQuaiGame.noi(quai(pallet, 0, 19, 1, false)).contains(" khu "));

        final Nguoi bot = new Nguoi(930, true, mah, 2, 0);
        final Nguoi nguoi = new Nguoi(930, false, mah, 2, 0);
        dung("T5: danh cho nguoi", LoaiQuaiGame.danhChoNguoi(m));
        dung("T5: bot khong dung", LoaiQuaiGame.botKhongDung(bot.p, m));
        dung("T5: nguoi choi van danh", !LoaiQuaiGame.botKhongDung(nguoi.p, m));
        final Monster t3 = quai(mah, 2, 65, LoaiDuLieu.T3, false);
        dung("T3: bot van danh / bat", !LoaiQuaiGame.botKhongDung(bot.p, t3));
        final Monster shiny = quai(mah, 2, 19, LoaiDuLieu.T1, true);
        dung("sac mau T1: bot khong dung", LoaiQuaiGame.botKhongDung(bot.p, shiny));
        final Monster thuong = quai(mah, 2, 0, 0, false);
        dung("quai thuong: bot danh", !LoaiQuaiGame.botKhongDung(bot.p, thuong));

        // sat thuong bot = 0, chot tra lai HP; nguoi choi thi danh giu nhu cu
        bang("bot danh T5: 0 sat thuong", 0, DanhGiuGame.gioiHan(bot.p, m, 5000, m.hp));
        m.hp = 400; // % HP toi da khong qua gioiHan
        DanhGiuGame.chot(bot.p, m, 1000);
        bang("bot: chot tra lai HP dau don", 1000, m.hp);
        final int dn = DanhGiuGame.gioiHan(nguoi.p, m, 5000, m.hp);
        dung("nguoi choi danh T5: van gay sat thuong (danh giu giu lai > 0 HP): " + dn,
                BatPokemonGame.bat() ? dn > 0 && dn < 1000 : dn == 5000);
        bang("bot danh T3: sat thuong nho nguyen ven", 10, DanhGiuGame.gioiHan(bot.p, t3, 10, t3.hp));
        bang("bot khong gay trang thai len con T5", 0, TrangThaiGame.thuGay(bot.p, m, 9));
        bang("bot khong gay trang thai (trang bi) len con T5", 0, TrangThaiGame.gayTuTrangBi(bot.p, m, 1));

        // song lau hon: 5 phut khong ai danh - con T5 con, con T3 bien mat
        final long truoc = System.currentTimeMillis() - 5L * 60000L;
        LoaiQuaiGame.HOANG_DA.put(m, Long.valueOf(truoc));
        LoaiQuaiGame.HOANG_DA.put(t3, Long.valueOf(truoc));
        m.hp = m.maxhp;
        t3.hp = t3.maxhp;
        t3.isDead = false;
        m.target = null;
        t3.target = null;
        LoaiQuaiGame.nhip(m);
        dung("T5 sau 5 phut khong ai danh: van con (10 phut)", !m.isDead && LoaiQuaiGame.HOANG_DA.containsKey(m));
        try {
            LoaiQuaiGame.nhip(t3);
        } catch (final Throwable e) {
            // actorDie can ban do day du: chi can biet da roi HOANG_DA
        }
        dung("T3 sau 5 phut: bien mat nhu cu (4 phut)", !LoaiQuaiGame.HOANG_DA.containsKey(t3));
        LoaiQuaiGame.HOANG_DA.remove(m);
        LoaiQuaiGame.HOANG_DA.remove(t3);
    }

    // ------------------------------------------------------------------ thong bao

    static void thongBao() throws Exception {
        final LoaiDuLieu cu = LoaiDuLieu.hien();
        final List<LoaiDuLieu.Loai> ds = new ArrayList<LoaiDuLieu.Loai>();
        ds.add(new LoaiDuLieu.Loai(147, "Dratini", 1, 0, -1, new int[]{50, 50, 50, 50, 50, 50}, 0, 147, 0, 1, LoaiDuLieu.T5, 45,
                true, false, true, 1, false, true, true));
        final LoaiDuLieu d = new LoaiDuLieu(ds, Collections.<LoaiDuLieu.TienHoa>emptyList(),
                Collections.<LoaiDuLieu.Vung>emptyList(), new HashMap<Integer, String>());
        final java.lang.reflect.Constructor<ChonLoaiHoangDa.KetQua> kqc =
                ChonLoaiHoangDa.KetQua.class.getDeclaredConstructor(int.class, int.class);
        kqc.setAccessible(true);
        final ChonLoaiHoangDa.KetQua t5 = kqc.newInstance(147, LoaiDuLieu.T5);

        final Map mah = banDo(28, 0); // khong chia khu: getAllPlayer = allPlayers(phe) (Region that can luong ban do)
        final Map khac = banDo(14, 0);
        final Monster m = quai(mah, 0, 147, LoaiDuLieu.T5, false);
        final Nguoi tai = new Nguoi(930, false, mah, 0, 0);       // dang o dung cho
        final Nguoi botTai = new Nguoi(930, true, mah, 0, 0);     // bot o dung cho
        final Nguoi xa = new Nguoi(930, false, khac, 0, 0);       // nguoi choi ban do khac, cung phe
        final Nguoi pheKhac = new Nguoi(930, false, khac, 0, 1);  // phe khac: khong bao gio thay con nay
        final Nguoi cuXa = new Nguoi(907, false, khac, 0, 0);     // client cu: khong thay loai rieng
        final Nguoi botXa = new Nguoi(930, true, khac, 0, 0);
        mah.allPlayers.get(0).add(tai.p);
        mah.allPlayers.get(0).add(botTai.p);
        final List<Char> them = new ArrayList<Char>();
        for (final Nguoi n : new Nguoi[]{tai, botTai, xa, pheKhac, cuXa, botXa}) {
            them.add(n.p);
        }
        CharManager.instance.vChars.addAll(them);
        try {
            LoaiQuaiGame.LAN_BAO_MAP.clear();
            LoaiQuaiGame.lanBaoToan = 0L;
            LoaiQuaiGame.baoNgay(m, d, t5);
            bang("nguoi tai cho: 1 tin", 1, tai.tin().size());
            dung("tin ghi ten loai + huong + Poke Ball: " + tai.tin(), !tai.tin().isEmpty()
                    && tai.tin().get(0).contains("Dratini") && tai.tin().get(0).contains("phía đông bắc")
                    && tai.tin().get(0).contains("Poké Ball"));
            bang("bot tai cho: khong nhan", 0, botTai.tin().size());
            bang("T5: nguoi choi cung phe o ban do khac nhan 1 tin", 1, xa.tin().size());
            bang("T5: phe khac khong nhan", 0, pheKhac.tin().size());
            bang("T5: client cu khong nhan", 0, cuXa.tin().size());
            bang("T5: bot o xa khong nhan", 0, botXa.tin().size());

            // cung khu trong 1 phut: khong bao lai; khu khac cua cung ban do (truoc day bi chan theo ca ban do)
            LoaiQuaiGame.baoNgay(m, d, t5);
            bang("cung khu trong 1 phut: khong bao lai", 1, tai.tin().size());
            final Monster khu2 = quai(mah, 1, 147, LoaiDuLieu.T5, false);
            dung("khoa gioi han theo khu: khu khac khong trung", !LoaiQuaiGame.khoaKhu(m).equals(LoaiQuaiGame.khoaKhu(khu2)));

            // T5 toan may chu: dung khu khac / cung phe nhan, dung khu do khong nhan lan 2
            final Map chiaKhu = banDo(28, 5);
            final Monster mk = quai(chiaKhu, 2, 147, LoaiDuLieu.T5, false);
            final Nguoi khu3 = new Nguoi(930, false, chiaKhu, 2, 0);
            final Nguoi khu1 = new Nguoi(930, false, chiaKhu, 0, 0);
            dung("toan may chu: nguoi dung khu do khong nhan (da co tin trong khu)", !LoaiQuaiGame.nhanTinToan(khu3.p, mk));
            dung("toan may chu: nguoi cung ban do khac khu nhan", LoaiQuaiGame.nhanTinToan(khu1.p, mk));
            final int n = LoaiQuaiGame.guiToanMayChu(mk, "x", System.currentTimeMillis());
            bang("toan may chu: toi da 1 tin / phut ca may chu", -1, n);
        } finally {
            CharManager.instance.vChars.removeAll(them);
            LoaiDuLieu.dat(cu);
            LoaiQuaiGame.LAN_BAO_MAP.clear();
            LoaiQuaiGame.lanBaoToan = 0L;
        }
    }

    // ------------------------------------------------------------------ Tu dong danh

    static void tuDong() {
        final TuDongKeHoach.TinhHinh t = new TuDongKeHoach.TinhHinh();
        t.x = 400;
        t.y = 400;
        t.neoX = 400;
        t.neoY = 400;
        t.banKinh = 240;
        t.cap = 10;
        t.hp = 1000;
        t.maxhp = 1000;
        t.mp = 100;
        t.maxmp = 100;
        t.thuoc = new int[200];
        t.chieu.add(new TuDongKeHoach.Chieu(0, 0, 100, 0, 0L));
        final TuDongKeHoach.Quai hiem = new TuDongKeHoach.Quai(1, 420, 400, 10, 100, 100);
        hiem.deBat = true;
        final TuDongKeHoach.Quai thuong = new TuDongKeHoach.Quai(2, 520, 400, 10, 100, 100);
        t.quai.add(hiem);
        t.quai.add(thuong);
        TuDongKeHoach.KeHoach k = TuDongKeHoach.lap(new TuDongKeHoach.CauHinh(), t);
        bang("Tu dong: bo con danh cho nguoi (gan hon), danh con thuong", 2, k.id);
        t.quai.remove(thuong);
        k = TuDongKeHoach.lap(new TuDongKeHoach.CauHinh(), t);
        bang("Tu dong: chi con con danh cho nguoi -> cho", TuDongKeHoach.HD_CHO, k.hd);
        hiem.danhMinh = true;
        k = TuDongKeHoach.lap(new TuDongKeHoach.CauHinh(), t);
        bang("Tu dong (nguoi choi): no dang danh minh -> danh tra", 1, k.id);
        hiem.cuaNguoiKhac = true; // bot HLV AI: TuDongGame dat them cuaNguoiKhac
        k = TuDongKeHoach.lap(new TuDongKeHoach.CauHinh(), t);
        dung("Tu dong (bot): khong danh ca khi no danh minh", k.hd != TuDongKeHoach.HD_DANH || k.id != 1);
    }

    // ------------------------------------------------------------------ noi day (doc nguon)

    static void nguon() {
        final String bat = doc("src/real/BatPokemonGame.java");
        final int chan = bat.indexOf("LoaiQuaiGame.botKhongDung(p, m)");
        dung("nem bong: bot bi tu choi TRUOC khi tru bong", chan > 0 && chan < bat.indexOf("h.truBong(b.ma)"));
        dung("giao chien: bot khong gianh quyen nem", bat.contains("if (LoaiQuaiGame.botKhongDung(p, a)) {"));
        final String td = doc("src/real/TuDongGame.java");
        dung("TuDongGame dat deBat + cuaNguoiKhac cho bot", td.contains("q.deBat = LoaiQuaiGame.danhChoNguoi(mt);")
                && td.contains("if (q.deBat && p.laHlvAi) {"));
        final String l = doc("src/real/LoaiQuaiGame.java");
        dung("T5 khong con qua ThongBaoGop toi moi nguoi", !l.contains("ThongBaoGop.dua(\"sinh_thai\""));
        final String hst = doc("src/com/htbb/pokemon/HeSinhThaiGame.java");
        dung("tin quai hiem he sinh thai cung ghi khu", hst.contains("real.LoaiQuaiGame.noi(m)"));
        final String nc = doc("../tools/pokemon/hlv_ai/HlvAiNguoiChoi.java");
        dung("bot HLV AI: chi san con khong danh cho nguoi", nc.contains("KichBanCap140TinhNang.batPokemon(this, true)")
                && nc.contains("KichBanCap140TinhNang.coConChoBot(b)"));
        final String bc = doc("../tools/pokemon/kiem_tai/BotChucNang.java");
        dung("bot doc co shiny cua -125", bc.contains("hoangDa.put(id, new int[]{dex, bac, shiny})"));
    }
}
