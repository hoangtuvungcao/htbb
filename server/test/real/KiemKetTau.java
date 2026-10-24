package real;

import com.htbb.pokemon.KhoaBanDo;
import io.Message;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bao loi 24/10/2026 (J2ME, cap 32, kazuhas4): len tau Ben tau 106 -> thuyen 107 khi chua xong cua ai «Đường sang Johto»
 * (New Bark 10 cap 27 + CA-05.3). Thuyen cap ben: MapVongLap goi move2Map(10) MOI nhip vong lap ban do (dieu kien
 * timeOutBoard khong chan khi = 0) -> KhoaBanDoGame.chanDiBo chan -> nap lai thuyen tai cho -> nap map lien tuc, moi menu /
 * hop bi dong (GameScr.onMap), ket mai. Kiem: thuyen + ben kia khoa -> ve ben MOT lan (giu ve), khong lap; len tau bi tu choi
 * truoc khi di; move2Map bi chan lap lai chi nap lai 1 lan / 3 giay; cuu luc nap (o thuyen / o ban do khoa) + bao sau -500;
 * Ve dich chuyen dung duoc tren thuyen; quet ma moi duong xuong / len thuyen.
 */
public final class KiemKetTau {

    private KiemKetTau() {
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

    static final class NguoiChoi extends Char {
        final List<Message> goi = new ArrayList<Message>();
        final List<String> chu = new ArrayList<String>();

        NguoiChoi(final int id, final int cap, final Map m) {
            super(null);
            this.id = (short) (id % 30000);
            charDBID = id;
            charname = "kettau" + id;
            lvDetail = new LevelDetail();
            lvDetail.lv = cap;
            inCountry = 0;
            myCountry = 0;
            region = 0;
            hp = 100;
            maxhp = 100;
            dat(m, 20, 12);
        }

        void dat(final Map m, final int ox, final int oy) {
            map = m;
            mapID = m.mapId;
            x = ox * 16 + 8;
            y = oy * 16 + 8;
        }

        @Override
        public void sendMessage(final Message m) {
            goi.add(m);
            if (m.command == -26 || m.command == -32) {
                try {
                    final DataInputStream in = new DataInputStream(new ByteArrayInputStream(m.toByteArray()));
                    in.readByte();
                    in.readShort();
                    if (m.command == -32) {
                        in.readShort();
                        in.readByte();
                    }
                    chu.add(m.command + ":" + in.readUTF());
                } catch (final Exception e) {
                    chu.add(m.command + ":?");
                }
            }
        }

        int dem(final int lenh) {
            int n = 0;
            for (final Message m : goi) {
                if (m.command == lenh) {
                    n++;
                }
            }
            return n;
        }

        boolean coChu(final String s) {
            for (final String c : chu) {
                if (c.contains(s)) {
                    return true;
                }
            }
            return false;
        }

        void xoa() {
            goi.clear();
            chu.clear();
        }
    }

    static Map banDo(final int id) {
        final Map m = new Map();
        m.initKhoQuaiVaNguoi();
        m.mapId = id;
        m.mapIDLoadMap = id == 2441 ? 107 : id;
        RealController.mapList.put(Integer.valueOf(id), m);
        return m;
    }

    /** Tien do nhiem vu rong (CSDL doc duoc, chua xong gi) hoac co ma da xong. */
    @SuppressWarnings("unchecked")
    static void tienDo(final Char p, final String xong) throws Exception {
        final java.lang.reflect.Field f = NhiemVuEngine.class.getDeclaredField("TD");
        f.setAccessible(true);
        final ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>> td = (ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>>) f.get(null);
        final ConcurrentHashMap<String, long[]> t = new ConcurrentHashMap<String, long[]>();
        if (xong != null) {
            t.put(xong, new long[]{1L, 2L, 0L, 0L});
        }
        td.put(Integer.valueOf(p.charDBID), t);
    }

    @SuppressWarnings("unchecked")
    static void boTienDo(final Char p) throws Exception {
        final java.lang.reflect.Field f = NhiemVuEngine.class.getDeclaredField("TD");
        f.setAccessible(true);
        ((ConcurrentHashMap<Integer, ?>) f.get(null)).remove(Integer.valueOf(p.charDBID));
    }

    static final int[] IDS = {0, 1, 70, 80, 301, 1701, 1901, 10, 106, 107, 501, 2421, 2441, 9};

    public static void chay() {
        final java.util.Map<Integer, Map> cuMap = new HashMap<Integer, Map>();
        for (final int id : IDS) {
            final Map m = RealController.mapList.get(Integer.valueOf(id));
            if (m != null) {
                cuMap.put(Integer.valueOf(id), m);
            }
        }
        final java.util.Map<Integer, KhoaBanDo.Luat> cuLuat = new HashMap<Integer, KhoaBanDo.Luat>(KhoaBanDo.bang());
        final java.util.Map<Integer, KhoaBanDo.Luat> luat = new HashMap<Integer, KhoaBanDo.Luat>();
        luat.put(10, new KhoaBanDo.Luat(10, 27, "CA-05.3", "Cửa ải: Đường sang Johto", true, ""));
        luat.put(9, new KhoaBanDo.Luat(9, 18, "CA-04.2", "Cửa ải: Vượt bờ biển Fuchsia", true, ""));
        luat.put(106, new KhoaBanDo.Luat(106, 0, "", "", false, "Khu an toàn"));
        luat.put(107, new KhoaBanDo.Luat(107, 0, "", "", false, "Khu an toàn"));
        KhoaBanDo.datBang(luat);
        try {
            for (final int id : IDS) {
                banDo(id);
            }
            thuan();
            xuongThuyen();
            lenTau();
            move2MapKhongLap();
            cuuKhiNap();
            veTrenThuyen();
            quetMa();
        } catch (final Throwable e) {
            e.printStackTrace();
            dung("KiemKetTau chay het: " + e, false);
        } finally {
            for (final int id : IDS) {
                RealController.mapList.remove(Integer.valueOf(id));
            }
            RealController.mapList.putAll(cuMap);
            KhoaBanDo.datBang(cuLuat);
        }
    }

    static void thuan() {
        dung("107 la tram trung chuyen", KhoaBanDoGame.laTramTrungChuyen(107));
        dung("2441 (thuyen ben dong) la tram trung chuyen", KhoaBanDoGame.laTramTrungChuyen(2441));
        dung("106 / 10 khong phai", !KhoaBanDoGame.laTramTrungChuyen(106) && !KhoaBanDoGame.laTramTrungChuyen(10));
        bang("thuyen 107 cap New Bark 10", 10, KhoaBanDoGame.denCuaThuyen(107));
        bang("thuyen 2441 cap 501", 501, KhoaBanDoGame.denCuaThuyen(2441));
        bang("ben cua 107 = 106", 106, KhoaBanDoGame.benCuaThuyen(107));
        bang("ben cua 2443 = 2423", 2423, KhoaBanDoGame.benCuaThuyen(2443));
        bang("thuyen cua ben 106 = 107", 107, KhoaBanDoGame.thuyenCuaBen(106));
        bang("thuyen cua ben 2422 = 2442", 2442, KhoaBanDoGame.thuyenCuaBen(2422));
    }

    /** Thuyen cap ben, New Bark khoa: ve ben 106 mot lan, giu ve, hop cua ai cho -500; goi lai (nhip sau) khong lap. */
    static void xuongThuyen() throws Exception {
        final Map thuyen = RealController.mapList.get(107);
        final NguoiChoi p = new NguoiChoi(991001, 32, thuyen);
        KhoaBanDoGame.datMien(p.charDBID, 1);
        KhoaBanDoGame.xoaHoi(p.charDBID);
        tienDo(p, null);
        p.potions[20] = 2;
        try {
            dung("cap 32 chua CA-05.3: bi chan vao New Bark", KhoaBanDoGame.lyDoChan(p, 10) != null);
            try {
                KhoaBanDoGame.xuongThuyen(thuyen, p, 10);
            } catch (final Throwable ignored) {
                // nap map can phien: chi kiem vi tri
            }
            bang("thuyen cap ben, New Bark khoa: ve Ben tau 106 (khong o lai thuyen)", 106, p.mapID);
            bang("giu ve tau (chua toi noi)", 2, p.potions[20]);
            dung("bao ro: tau dua ve ben", p.coChu("tàu đã đưa bạn về"));
            bang("dung 1 lan nap map (goi 12)", 1, p.dem(12));
            // vong lap ban do: nguoi choi da roi thuyen -> thuyen khong con ai; ben 106 khong co nhip xuong thuyen
            final int truoc = p.dem(12);
            dung("vong lap thuyen khong con nhin thay nguoi nay", p.mapID != 107);
            bang("khong nap lai them", truoc, p.dem(12));

            // ben kia mo: xuong thuyen binh thuong, tru 1 ve
            final NguoiChoi q = new NguoiChoi(991002, 32, thuyen);
            KhoaBanDoGame.datMien(q.charDBID, 1);
            tienDo(q, "CA-05.3");
            q.potions[20] = 1;
            try {
                KhoaBanDoGame.xuongThuyen(thuyen, q, 10);
            } catch (final Throwable ignored) {
            }
            bang("da xong cua ai: toi New Bark", 10, q.mapID);
            bang("da xong cua ai: tru 1 ve", 0, q.potions[20]);
            boTienDo(q);
        } finally {
            boTienDo(p);
            KhoaBanDoGame.xoaHoi(p.charDBID);
        }
    }

    /** Ben tau: chua mo New Bark -> "Lên tàu" bi tu choi truoc khi di (gotoIsland khong bat, hop cua ai). */
    static void lenTau() throws Exception {
        final Map ben = RealController.mapList.get(106);
        final NguoiChoi p = new NguoiChoi(991003, 32, ben);
        KhoaBanDoGame.datMien(p.charDBID, 1);
        KhoaBanDoGame.xoaHoi(p.charDBID);
        tienDo(p, null);
        p.potions[20] = 1;
        try {
            dung("len thuyen 107 = vao New Bark: bi chan", KhoaBanDoGame.lyDoChan(p, 107) != null);
            ben.doUp2Board(p);
            dung("len tau bi tu choi: khong gotoIsland", !p.gotoIsland);
            dung("bao dieu kien New Bark", p.coChu("New Bark") || p.coChu("Đường sang Johto"));
            bang("van giu ve", 1, p.potions[20]);
            bang("van o ben", 106, p.mapID);
            // da xong: len duoc
            final NguoiChoi q = new NguoiChoi(991004, 32, ben);
            KhoaBanDoGame.datMien(q.charDBID, 1);
            tienDo(q, "CA-05.3");
            q.potions[20] = 1;
            ben.doUp2Board(q);
            dung("da xong cua ai: len tau duoc", q.gotoIsland);
            boTienDo(q);
            // nhan vat cu duoc mien tru cap 40: len duoc
            final NguoiChoi cu = new NguoiChoi(991005, 40, ben);
            KhoaBanDoGame.datMien(cu.charDBID, 40);
            tienDo(cu, null);
            cu.potions[20] = 1;
            ben.doUp2Board(cu);
            dung("mien tru cap 40: len tau duoc", cu.gotoIsland);
            boTienDo(cu);
            // nhan diem / di bo vao thuyen cung bi chan
            dung("nhan diem thuyen: Khoá", KhoaBanDoGame.nhanDiem(p, 107, "Thuyền").startsWith("Khoá: "));
        } finally {
            boTienDo(p);
            KhoaBanDoGame.xoaHoi(p.charDBID);
        }
    }

    /** move2Map bi chan goi lap (duong may chu): chi nap lai MOT lan / 3 giay. */
    static void move2MapKhongLap() throws Exception {
        final Map thuyen = RealController.mapList.get(107);
        final NguoiChoi p = new NguoiChoi(991006, 32, thuyen);
        KhoaBanDoGame.datMien(p.charDBID, 1);
        KhoaBanDoGame.xoaHoi(p.charDBID);
        tienDo(p, null);
        try {
            for (int i = 0; i < 10; i++) {
                try {
                    thuyen.move2Map(p, 62, 122, 10, 0);
                } catch (final Throwable ignored) {
                }
            }
            bang("10 lan move2Map bi chan: van o thuyen", 107, p.mapID);
            bang("10 lan move2Map bi chan: chi nap lai 1 lan", 1, p.dem(12));
            bang("chanMove2Map lan sau trong 3 giay = 2 (bo qua)", 2, KhoaBanDoGame.chanMove2Map(p, 10));
            KhoaBanDoGame.xoaHoi(p.charDBID);
            bang("het chung: nap lai lai duoc (1)", 1, KhoaBanDoGame.chanMove2Map(p, 10));
            bang("ban do mo: 0", 0, KhoaBanDoGame.chanMove2Map(p, 106));
        } finally {
            boTienDo(p);
            KhoaBanDoGame.xoaHoi(p.charDBID);
        }
    }

    /** Luc nap: o thuyen ma New Bark khoa / o ban do khoa -> ve lang; mo / CSDL loi -> giu nguyen; bao sau -500. */
    static void cuuKhiNap() throws Exception {
        final Map thuyen = RealController.mapList.get(107);
        final NguoiChoi p = new NguoiChoi(991007, 32, thuyen);
        KhoaBanDoGame.datMien(p.charDBID, 1);
        KhoaBanDoGame.xoaHoi(p.charDBID);
        tienDo(p, null);
        try {
            final int[] lang = KhoaBanDoGame.lang(p);
            dung("cuu: o thuyen 107, New Bark khoa -> dua di", KhoaBanDoGame.cuuKhiNap(p));
            bang("cuu: ve lang khoi dau cua vung", lang[0], p.mapID);
            dung("cuu: khong con o thuyen", p.mapID != 107);
            p.map = RealController.mapList.get(Integer.valueOf(p.mapID));
            p.xoa();
            KhoaBanDoGame.sauKhiVaoMap(p);
            dung("sau -500: bao 'Bạn đã được đưa về ... vì chưa mở'", p.coChu("Bạn đã được đưa về") && p.coChu("vì chưa mở"));
            p.xoa();
            KhoaBanDoGame.sauKhiVaoMap(p);
            dung("bao mot lan", !p.coChu("Bạn đã được đưa về"));

            // o ban do khoa (New Bark) luc nap
            final NguoiChoi k = new NguoiChoi(991008, 32, RealController.mapList.get(10));
            KhoaBanDoGame.datMien(k.charDBID, 1);
            tienDo(k, null);
            dung("cuu: o New Bark khoa -> dua di", KhoaBanDoGame.cuuKhiNap(k));
            bang("cuu: ve lang", lang[0], k.mapID);
            boTienDo(k);
            KhoaBanDoGame.xoaHoi(k.charDBID);

            // da xong cua ai: o thuyen giu nguyen (tau se cap ben)
            final NguoiChoi x = new NguoiChoi(991009, 32, thuyen);
            KhoaBanDoGame.datMien(x.charDBID, 1);
            tienDo(x, "CA-05.3");
            dung("da mo New Bark: khong dua di", !KhoaBanDoGame.cuuKhiNap(x));
            bang("da mo: van o thuyen", 107, x.mapID);
            boTienDo(x);

            // mien tru nhan vat cu
            final NguoiChoi m = new NguoiChoi(991010, 32, RealController.mapList.get(10));
            KhoaBanDoGame.datMien(m.charDBID, 30);
            tienDo(m, null);
            dung("mien tru cap 30: o New Bark khong bi dua di", !KhoaBanDoGame.cuuKhiNap(m));
            boTienDo(m);

            // admin
            final NguoiChoi gm = new NguoiChoi(991011, 1, thuyen);
            gm.isAdmin = true;
            dung("admin: khong dua di", !KhoaBanDoGame.cuuKhiNap(gm));
        } finally {
            boTienDo(p);
            KhoaBanDoGame.xoaHoi(p.charDBID);
        }
    }

    /** Ve dich chuyen tren thuyen: khong bi cam (loi thoat tu nhien). */
    static void veTrenThuyen() {
        final NguoiChoi p = new NguoiChoi(991012, 32, RealController.mapList.get(107));
        final String ly = VeDichChuyenGame.lyDoChan(p);
        dung("Ve dich chuyen dung duoc tren thuyen 107 (ly do: " + ly + ")", ly == null);
        dung("thuyen khong tinh la phong rieng / pho ban", !VeDichChuyenGame.phongRieng(p));
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static int dem(final String s, final String mau) {
        int n = 0;
        for (int i = s.indexOf(mau); i >= 0; i = s.indexOf(mau, i + 1)) {
            n++;
        }
        return n;
    }

    static void quetMa() {
        final String vl = doc("src/real/MapVongLap.java");
        bang("MapVongLap: 3 nhip thuyen cap ben qua KhoaBanDoGame.xuongThuyen", 3, dem(vl, "KhoaBanDoGame.xuongThuyen(map, p, id)"));
        dung("MapVongLap: khong con move2Map thang toi ben kia", !vl.contains("map.move2Map(p, 62 + Map.random(5), 122 + Map.random(5), id"));
        final String dun = doc("src/real/Dun.java");
        dung("Dun: thuyen cap ben qua xuongThuyen", dun.contains("KhoaBanDoGame.xuongThuyen(this, p, id)"));
        final String cv = doc("src/real/CharVongLap.java");
        dung("CharVongLap: len thuyen kiem ben kia", cv.contains("KhoaBanDoGame.chan(self, id2)"));
        final String mp = doc("src/real/Map.java");
        dung("Map.doUp2Board: kiem ben kia truoc khi len", mp.contains("KhoaBanDoGame.chan(player, KhoaBanDoGame.thuyenCuaBen(this.mapId))"));
        dung("Map.move2Map: chong nap lai lien tuc", mp.contains("KhoaBanDoGame.chanMove2Map(player, mapID)"));
        final String nap = doc("src/real/CharNapDuLieu.java");
        dung("CharNapDuLieu: khiNap (cuu) sau LauDaiNhanVat.khiNap", nap.indexOf("LauDaiNhanVat.khiNap(self)") > 0
                && nap.indexOf("LauDaiNhanVat.khiNap(self)") < nap.indexOf("KhoaBanDoGame.khiNap(self)"));
    }
}
