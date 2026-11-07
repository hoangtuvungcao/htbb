import com.htbb.pokemon.KhoaBanDo;
import io.Message;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import real.Char;
import real.KhoaBanDoGame;
import real.LevelDetail;
import real.MonsterTemplate;
import real.MonsterVantieu;
import real.RealController;
import real.XeHangChuyenMap;

/**
 * Bao loi 07/11/2026 (ruby, cap 72, ~09:24): van tieu di tau (Bến tàu 106 -&gt; Thuyền 107 -&gt; New Bark 10 -&gt; ... -&gt; Đường
 * Victory 118), toi Dịch trạm (o 51..59 x 16..24) bao "Không thể trả hàng do khác map" - xe o lai ben tau. Kiem tren ban
 * do / nhan vat / xe that (real.Map, real.Char, real.MonsterVantieu) qua cac duong doi ban do that:
 * <ul>
 *   <li>tai hien: Map.move2Map (duong tau cu) bo xe lai; TraHang tai Dịch trạm khong con bao "khác map" tran;</li>
 *   <li>tau: len tau (XeHangChuyenMap.move2MapCungXe, CharVongLap), xuong tau (KhoaBanDoGame.xuongThuyen) ca ban goc 106/107/10
 *       va ban sao khu 2421/2441/501; xe song qua nhip doi tren thuyen;</li>
 *   <li>di bo qua cong that (MapXuLyLenh.doChangeMap): tu ban sao khu Bến đông 2421 ve Pallet 0 (truoc day bo xe);</li>
 *   <li>ban do khong cho xe (Gym, nha): xe doi, bao noi xe;</li>
 *   <li>dich chuyen tat (xa phu / Pidgeot bay, Arcanine, Lapras, Pidgeot NPC, ve...): chan TRUOC khi di;</li>
 *   <li>xe da lac: toi Dịch trạm / Kecleon xe duoc keo ve (lac do loi) hoac bao xe dang o dau (chu bo xe).</li>
 * </ul>
 */
public final class KiemXeHangLacMap {

    private KiemXeHangLacMap() {
    }

    static final class NguoiChoi extends Char {
        final List<String> bao = new ArrayList<String>();

        NguoiChoi(final int id) {
            super(null);
            charDBID = id;
            charname = "kiemxe" + id;
            lvDetail = new LevelDetail();
            lvDetail.lv = 72;
            myCountry = 0;
            inCountry = 0;
        }

        @Override
        public void sendMessage(final Message m) {
            if (m == null || m.command != 37 && m.command != -26) {
                return;
            }
            try {
                final DataInputStream in = new DataInputStream(new ByteArrayInputStream(m.toByteArray()));
                in.readByte();
                in.readShort();
                bao.add(in.readUTF());
            } catch (final Exception e) {
                bao.add("?");
            }
        }

        String cuoi() {
            return bao.isEmpty() ? "" : bao.get(bao.size() - 1);
        }

        boolean coBao(final String s) {
            for (final String b : bao) {
                if (b.contains(s)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static void main(final String[] args) {
        try {
            real.Map.loadGemTemplate(); // chay rieng: Char can bang ngoc (Kiem.main da nap)
        } catch (final Throwable ignored) {
        }
        chay();
        System.exit(Kiem.tongKet());
    }

    static final HashMap<Integer, real.Map> CU = new HashMap<Integer, real.Map>();

    public static void chay() {
        Kiem.nhom("Xe hang van tieu theo nguoi choi qua moi duong doi ban do (bao loi ruby 07/11: tau -> Dịch trạm 'khác map')");
        final Map<Integer, KhoaBanDo.Luat> luatCu = new HashMap<Integer, KhoaBanDo.Luat>(KhoaBanDo.bang());
        KhoaBanDo.datBang(new HashMap<Integer, KhoaBanDo.Luat>()); // khong khoa ban do: chi kiem xe
        try {
            taiHien();
            tau();
            diBo();
            khongChoXe();
            chanDichChuyen();
            xeLac();
            docMa();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemXeHangLacMap chay het: " + e, false);
        } finally {
            for (final int id : CU.keySet()) {
                final real.Map m = CU.get(id);
                if (m == null) {
                    RealController.mapList.remove(Integer.valueOf(id));
                } else {
                    RealController.mapList.put(Integer.valueOf(id), m);
                }
            }
            KhoaBanDo.datBang(luatCu);
        }
    }

    // ------------------------------------------------------------------ dung canh

    static real.Map banDo(final int id, final int goc) {
        if (!CU.containsKey(id)) {
            CU.put(id, RealController.mapList.get(Integer.valueOf(id))); // tra lai sau khi kiem
        }
        final real.Map m = new real.Map();
        m.mapId = id;
        m.mapIDLoadMap = goc;
        for (int i = 0; i < 3; i++) {
            m.allPlayers.add(new Vector<Char>());
        }
        RealController.mapList.put(Integer.valueOf(id), m);
        return m;
    }

    static int dem = 0;

    /** Nhan vat dung o (tx,ty) ban do m, co xe hang canh (2 o). */
    static NguoiChoi coXe(final real.Map m, final int tx, final int ty) {
        final NguoiChoi p = new NguoiChoi(997000 + (++dem));
        vao(p, m, tx, ty);
        final MonsterTemplate t = new MonsterTemplate();
        t.id = 95;
        final MonsterVantieu xe = new MonsterVantieu(m, t, p.x + 24, p.y, p.inCountry);
        xe.id = (short) (31000 + dem);
        xe.charDbid = p.charDBID;
        xe.namemaster = p.charname;
        xe.typeTieu = 0;
        xe.setTimeFinish(System.currentTimeMillis() + 1200000L);
        p.monster = xe;
        p.typeTieu = 0;
        return p;
    }

    static void vao(final NguoiChoi p, final real.Map m, final int tx, final int ty) {
        if (p.map != null) {
            p.map.playerExit(p);
        }
        m.playerJoin(p);
        p.x = tx * 16 + 8;
        p.y = ty * 16 + 8;
    }

    static boolean xeCanh(final NguoiChoi p) {
        return p.monster.map == p.map && Math.abs(p.monster.x - p.x) <= 32 && Math.abs(p.monster.y - p.y) <= 32
                && p.monster.region == p.region && p.monster.inCountry == p.inCountry;
    }

    /** Dịch trạm: dung o (55,20) Đường Victory, goi TraHang.excecute (MapDoMenu -51 / RealController -51). */
    static void traHang(final NguoiChoi p) throws Exception {
        final java.lang.reflect.Method m = Class.forName("real.TraHang").getMethod("excecute", Char.class);
        m.invoke(null, p);
    }

    // ------------------------------------------------------------------ tai hien

    static void taiHien() throws Exception {
        final real.Map ben = banDo(106, 106);
        final real.Map thuyen = banDo(107, 107);
        final real.Map victory = banDo(118, 118);
        final NguoiChoi p = coXe(ben, 30, 20);
        Kiem.dung("xe di cung (canh nguoi choi o Bến tàu)", XeHangChuyenMap.diCung(p));
        // duong tau CU: Map.move2Map tran - xe o lai ben
        ben.move2Map(p, 20, 11, 107, 0);
        Kiem.bang("tai hien: move2Map tran (duong tau cu) - nguoi choi len thuyen", 107, p.mapID);
        Kiem.dung("tai hien: xe o lai Bến tàu", p.monster.map == ben);
        // di tiep (move2Map tran) toi Dịch trạm: TraHang
        thuyen.move2Map(p, 55, 20, 118, 0);
        p.monster.ganChu = false; // nhu chu bo xe (khong phai loi): bao xe o dau, khong keo
        traHang(p);
        Kiem.dung("Dịch trạm: khong con bao 'khác map' tran (" + p.cuoi() + ")", !p.coBao("do khác map"));
        Kiem.dung("Dịch trạm: bao xe dang o Bến tàu + toa do", p.cuoi().startsWith("Xe hàng đang ở ") && p.cuoi().contains("(" + p.monster.x / 16 + "," + p.monster.y / 16 + ")"));
        Kiem.dung("chua giao (xe chua toi)", !p.monster.isFinish);
        Kiem.dung("victory dung (bien dung)", victory != null);
    }

    // ------------------------------------------------------------------ tau

    static void tau() throws Exception {
        // ban goc: 106 -> 107 -> 10
        final real.Map ben = banDo(106, 106);
        final real.Map thuyen = banDo(107, 107);
        final real.Map newBark = banDo(10, 10);
        final NguoiChoi p = coXe(ben, 30, 20);
        XeHangChuyenMap.move2MapCungXe(ben, p, 20, 11, 107, 0); // CharVongLap: len tau
        Kiem.bang("len tau: nguoi choi o Thuyền 107", 107, p.mapID);
        Kiem.dung("len tau: xe len cung, dat canh nguoi choi", xeCanh(p) && p.monster.map == thuyen);
        // nhip doi tren thuyen: xe van chay (khong het gio / khong bi go)
        p.monster.target = p;
        p.monster.update();
        Kiem.dung("doi tren thuyen: xe con chay, van o thuyen", !p.monster.isFinish() && p.monster.map == thuyen);
        KhoaBanDoGame.xuongThuyen(thuyen, p, 10); // MapVongLap / Dun: tau cap ben
        Kiem.bang("xuong tau: nguoi choi o New Bark 10", 10, p.mapID);
        Kiem.dung("xuong tau: xe xuong cung, canh nguoi choi", xeCanh(p) && p.monster.map == newBark);

        // ban sao khu: Bến đông 2421 -> Thuyền bến đông 2441 -> New Bark 501
        final real.Map benDong = banDo(2421, 106);
        final real.Map thuyenDong = banDo(2441, 107);
        final real.Map nb501 = banDo(501, 10);
        final NguoiChoi q = coXe(benDong, 30, 20);
        XeHangChuyenMap.move2MapCungXe(benDong, q, 20, 11, KhoaBanDoGame.thuyenCuaBen(2421), 0);
        Kiem.dung("ban sao: len thuyen 2441 cung xe", q.mapID == 2441 && xeCanh(q) && q.monster.map == thuyenDong);
        KhoaBanDoGame.xuongThuyen(thuyenDong, q, KhoaBanDoGame.denCuaThuyen(2441));
        Kiem.dung("ban sao: xuong New Bark 501 cung xe", q.mapID == 501 && xeCanh(q) && q.monster.map == nb501);

        // xe bi bo xa o ben: khong len tau, bao xe doi o ben
        final NguoiChoi r = coXe(ben, 30, 20);
        r.monster.x = r.x + 400;
        XeHangChuyenMap.move2MapCungXe(ben, r, 20, 11, 107, 0);
        Kiem.dung("xe o xa (> " + XeHangChuyenMap.TAM + " px): khong len tau", r.monster.map == ben);
        Kiem.dung("xe o xa: bao xe dang doi o ben (" + r.cuoi() + ")", r.cuoi().startsWith("Xe hàng ở xa nên không đi cùng bạn"));
    }

    // ------------------------------------------------------------------ di bo qua cong (MapXuLyLenh.doChangeMap that)

    static Message goiDoiMap(final int den, final int tx, final int ty) throws Exception {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final DataOutputStream o = new DataOutputStream(b);
        o.writeShort(den);
        o.writeShort(tx);
        o.writeShort(ty);
        return new Message(4, b.toByteArray());
    }

    static void diBo() throws Exception {
        final real.Map benDong = banDo(2421, 106);
        final NguoiChoi p = coXe(benDong, 30, 20);
        // qua cong ve lang: may chu tu chon lang khoi dau (LangKhoiDau.bang) - dang ky moi ban sao lang
        banDo(0, 0); // Pallet: cong client xin
        for (final int id : real.LangKhoiDau.bang(p)[p.myCountry]) {
            banDo(id, id == 1701 ? 70 : id >= 301 && id <= 304 ? 0 : id);
        }
        final java.lang.reflect.Method dm = Class.forName("real.MapXuLyLenh").getDeclaredMethod("doChangeMap", real.Map.class, Char.class, Message.class);
        dm.setAccessible(true);
        dm.invoke(null, benDong, p, goiDoiMap(0, 40, 30));
        Kiem.dung("di bo Bến đông 2421 -> lang (Pallet / ban sao) qua cong (" + p.mapID + ")", p.map != benDong && real.Map.isMapLang(p.map));
        Kiem.dung("di bo tu ban sao khu: xe sang cung (truoc day o lai 2421)", p.monster.map == p.map && xeCanh(p));

        final real.Map ben = banDo(106, 106);
        final NguoiChoi q = coXe(ben, 30, 20);
        dm.invoke(null, ben, q, goiDoiMap(0, 40, 30));
        Kiem.dung("di bo ban goc 106 -> lang: xe sang cung (nhu cu)", real.Map.isMapLang(q.map) && q.monster.map == q.map && xeCanh(q));

        final NguoiChoi r = coXe(ben, 30, 20);
        r.monster.x += 400;
        dm.invoke(null, ben, r, goiDoiMap(0, 40, 30));
        Kiem.dung("di bo bo xe o xa: xe o lai, bao noi xe", real.Map.isMapLang(r.map) && r.monster.map == ben && r.coBao("Xe hàng ở xa nên không đi cùng bạn"));
    }

    // ------------------------------------------------------------------ ban do khong cho xe

    static void khongChoXe() throws Exception {
        final real.Map lang = banDo(9, 9);
        final real.Map gym = new real.Map() {
            @Override
            public boolean isMapTrain() {
                return false; // nhu MapTown (Gym 201)
            }
        };
        gym.mapId = 201;
        gym.mapIDLoadMap = 201;
        for (int i = 0; i < 3; i++) {
            gym.allPlayers.add(new Vector<Char>());
        }
        if (!CU.containsKey(201)) {
            CU.put(201, RealController.mapList.get(Integer.valueOf(201)));
        }
        RealController.mapList.put(Integer.valueOf(201), gym);
        final NguoiChoi p = coXe(lang, 30, 20);
        final boolean di = XeHangChuyenMap.diCung(p);
        final real.Map cu = p.map;
        vao(p, gym, 39, 90);
        p.doChangeMapMonster(di, cu);
        XeHangChuyenMap.sauDoiMap(p, cu, di);
        Kiem.dung("vao Gym: xe khong vao, doi o ban do cu", p.monster.map == lang);
        Kiem.dung("vao Gym: bao xe doi o dau (" + p.cuoi() + ")", p.cuoi().startsWith("Xe hàng không vào được"));

        final real.Map nha = banDo(9201, 9201);
        nha.isMapOffline = true;
        final NguoiChoi q = coXe(lang, 30, 20);
        final boolean di2 = XeHangChuyenMap.diCung(q);
        final real.Map cu2 = q.map;
        vao(q, nha, 5, 5);
        q.doChangeMapMonster(di2, cu2);
        XeHangChuyenMap.sauDoiMap(q, cu2, di2);
        Kiem.dung("vao nha: xe doi ngoai cua", q.monster.map == lang);
        Kiem.dung("choXeVao: lang / thuyen / ban sao khu", XeHangChuyenMap.choXeVao(lang) && XeHangChuyenMap.choXeVao(banDo(2441, 107)));
        Kiem.dung("choXeVao: khong nha / Gym / null", !XeHangChuyenMap.choXeVao(nha) && !XeHangChuyenMap.choXeVao(gym) && !XeHangChuyenMap.choXeVao(null));
    }

    // ------------------------------------------------------------------ dich chuyen tat

    static void chanDichChuyen() throws Exception {
        final real.Map lang = banDo(9, 9);
        final NguoiChoi p = coXe(lang, 30, 20);
        // xa phu / Pidgeot (Bay) (goi 85 -> Map.doMove2Map): truoc day khong xet xe
        final java.lang.reflect.Method mm = real.Map.class.getDeclaredMethod("doMove2Map", Char.class, int.class, int.class);
        mm.setAccessible(true);
        mm.invoke(lang, p, 0, 10);
        Kiem.dung("Pidgeot (Bay) xa phu: chan, o lai", p.mapID == 9 && p.monster.map == lang);
        // createServerAlertMessage doi chu qua ChuPokemon ("vận tiêu" -> "giao hàng")
        Kiem.bang("Pidgeot (Bay): cau bao truoc khi di", com.htbb.pokemon.ChuPokemon.doi(XeHangChuyenMap.cauChan("Pidgeot (Bay)")), p.cuoi());
        Kiem.bang("cau chan dung mau",
                "Đang giao hàng, không thể dùng vé dịch chuyển - hãy đi bộ / đi tàu cùng xe hàng.", XeHangChuyenMap.cauChan("vé dịch chuyển"));
        Kiem.dung("chan: co xe -> true", XeHangChuyenMap.chan(p, "x"));
        p.monster.isFinish = true;
        Kiem.dung("chan: xe da xong -> khong chan", !XeHangChuyenMap.chan(p, "x"));
        final NguoiChoi khong = new NguoiChoi(997999);
        Kiem.dung("chan: khong co xe -> khong chan", !XeHangChuyenMap.chan(khong, "x"));
    }

    // ------------------------------------------------------------------ xe da lac: cho giao

    static void xeLac() throws Exception {
        final real.Map ben = banDo(106, 106);
        final real.Map victory = banDo(118, 118);
        // lac do loi (chu roi ban do luc dang canh xe): keo ve, giao duoc
        final NguoiChoi p = coXe(ben, 30, 20);
        XeHangChuyenMap.capNhatGanChu(p);
        Kiem.dung("ganChu: canh xe", p.monster.ganChu);
        vao(p, victory, 55, 20);
        XeHangChuyenMap.capNhatGanChu(p); // khac ban do: giu nguyen
        Kiem.dung("ganChu: khac ban do giu nguyen", p.monster.ganChu);
        try {
            traHang(p);
        } catch (final java.lang.reflect.InvocationTargetException e) {
            // giao: doResponseTieu can CSDL / phien - chi kiem xe
        }
        Kiem.dung("Dịch trạm: xe lac do loi keo ve canh nguoi choi", p.monster.map == victory && xeCanh(p));
        Kiem.dung("Dịch trạm: bao xe da duoc dua ve", p.coBao("Xe hàng bị lạc ở ") && p.coBao("đã được đưa về đây"));
        Kiem.dung("Dịch trạm: giao (isFinish)", p.monster.isFinish);

        // chu bo xe di xa cung ban do (ganChu false) roi doi ban do: bao noi xe, khong keo
        final NguoiChoi q = coXe(ben, 30, 20);
        q.monster.x += 400;
        XeHangChuyenMap.capNhatGanChu(q);
        Kiem.dung("ganChu: bo xe xa -> false", !q.monster.ganChu);
        vao(q, victory, 55, 20);
        traHang(q);
        Kiem.dung("Dịch trạm: xe bi bo - khong keo", q.monster.map == ben && !q.monster.isFinish);
        Kiem.dung("Dịch trạm: bao ten ban do + o (" + q.cuoi() + ")", q.cuoi().startsWith("Xe hàng đang ở ") && q.cuoi().contains("(" + q.monster.x / 16 + "," + q.monster.y / 16 + ")"));

        // ngoai o giao: khong keo
        final NguoiChoi r = coXe(ben, 30, 20);
        vao(r, victory, 10, 10);
        traHang(r);
        Kiem.dung("ngoai o Dịch trạm: khong keo xe", r.monster.map == ben);

        // Kecleon: cung luat (TraHang.xeTaiCho)
        final java.lang.reflect.Method tc = Class.forName("real.TraHang").getDeclaredMethod("xeTaiCho", Char.class, String.class);
        tc.setAccessible(true);
        final real.Map pewter = banDo(9, 9);
        final NguoiChoi k = coXe(ben, 30, 20);
        vao(k, pewter, 55, 40);
        final Object ly = tc.invoke(null, k, "xa");
        Kiem.dung("xeTaiCho (Kecleon dung chung): lac do loi -> keo ve, giao duoc", ly == null && k.monster.map == pewter && xeCanh(k));
        final NguoiChoi k2 = coXe(pewter, 55, 40);
        k2.monster.x += 200;
        Kiem.bang("xeTaiCho: cung ban do nhung xa -> cau 'xa'", "xa", tc.invoke(null, k2, "xa"));
    }

    // ------------------------------------------------------------------ doc ma: moi duong noi dung luat

    static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/real/" + f)), StandardCharsets.UTF_8);
    }

    static void docMa() throws Exception {
        final String cv = doc("CharVongLap.java");
        Kiem.dung("CharVongLap len tau: move2MapCungXe", cv.contains("XeHangChuyenMap.move2MapCungXe(self.map, self, 19 + Map.random(3) + 1"));
        Kiem.dung("CharVongLap: cap nhat ganChu", cv.contains("XeHangChuyenMap.capNhatGanChu(self)"));
        final String kb = doc("KhoaBanDoGame.java");
        Kiem.dung("xuongThuyen + ve ben: move2MapCungXe", kb.contains("XeHangChuyenMap.move2MapCungXe(map, p, 62 + Map.random(5)")
                && kb.contains("XeHangChuyenMap.move2MapCungXe(map, p, x, y, ben, p.inCountry)"));
        final String xl = doc("MapXuLyLenh.java");
        Kiem.dung("di bo: diCung, bo dieu kien ban goc < 301", xl.contains("onMapMonster = XeHangChuyenMap.diCung(player)")
                && !xl.contains("toMap.mapId == toMap.mapIDLoadMap && toMap.mapId < 301"));
        Kiem.dung("di bo: bao xe khong sang duoc", xl.contains("XeHangChuyenMap.sauDoiMap(player, oldMap, onMapMonster)"));
        final String ch = doc("Char.java");
        Kiem.dung("doChangeMapMonster: choXeVao + keoXe", ch.contains("onMapMonster && XeHangChuyenMap.choXeVao(this.map)"));
        final String md = doc("MapDoMenu.java");
        Kiem.dung("MapDoMenu -51: dung TraHang (khong con ban sao 'khác map')", !md.contains("do khác map") && md.contains("TraHang.excecute(player)"));
        Kiem.dung("TraHang: khong con 'khác map'", !doc("TraHang.java").contains("do khác map"));
        Kiem.dung("Kecleon: TraHang.xeTaiCho", doc("HoTongNhiemVuGame.java").contains("TraHang.xeTaiCho(p,"));
        final String nv = doc("NpcDichVu.java");
        Kiem.dung("Lapras / Pidgeot / Arcanine: chan truoc khi di", nv.contains("XeHangChuyenMap.chan(p, \"đò Lapras\")")
                && nv.contains("XeHangChuyenMap.chan(p, \"Pidgeot bay\")") && nv.contains("XeHangChuyenMap.chan(p, \"lối đi Arcanine\")"));
        final String up = doc("doUsePotion.java");
        Kiem.dung("Ve Bay Pidgeot / dich chuyen ket doi: chan (moi noi xe)", up.contains("XeHangChuyenMap.chan(player, \"Vé Bay Pidgeot\")")
                && up.contains("XeHangChuyenMap.chan(player, \"dịch chuyển tới bạn kết đôi\")"));
        Kiem.dung("xa phu / Pidgeot (Bay): chan", doc("Map.java").contains("XeHangChuyenMap.chan(player, \"Pidgeot (Bay)\")"));
    }
}
