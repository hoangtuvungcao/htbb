package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.NhiemVuMau;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Bao loi 24/10/2026 (J2ME, Pewter): "danh ha Truong Gym xong va hoan thanh nhiem vu, quai van xuat hien lai - 2 Truong Gym
 * cung luc". Goc: boss Gym la MonsterSumon nam trong bang quai cua khu nhu quai thuong -> Monster.actorDie dat bornTime =
 * now + 6 giay, Monster.update hoi sinh day mau (BossLauDai.khiChet da go trang thai Gym nen con moi nay la boss MO COI: trong
 * lang khong ai danh duoc, ton tai toi khi khoi dong lai); het 10 phut thi chi danh dau guc, khong goi 90 nen client van ve.
 * Kiem: mot tran / nhan vat (bam dup, nhieu cua, nhieu luong cung luc), thang -> go ngay cho moi nguoi + thuong / buoc nhiem
 * vu mot lan, khong hoi sinh, het gio / roi ban do / thoat game -> go + danh lai duoc, de cua boss go cung boss, So tay /
 * Nhiem vu cua toi / nhan thuong cua ai o ban do bat ky.
 */
public final class KiemGymMotTran {

    private KiemGymMotTran() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void dung(final String m, final boolean v) {
        BAO.bang(m, Boolean.TRUE, Boolean.valueOf(v));
    }

    static void sai(final String m, final boolean v) {
        BAO.bang(m, Boolean.FALSE, Boolean.valueOf(v));
    }

    static final class NguoiChoi extends Char {
        final List<Message> goi = java.util.Collections.synchronizedList(new ArrayList<Message>());

        NguoiChoi(final int id, final int cap, final Map m) {
            super(null);
            this.id = (short) (id % 30000);
            charDBID = id;
            charname = "gymmot" + id;
            lvDetail = new LevelDetail();
            lvDetail.lv = cap;
            mainQuest = new NewCharQuest(true);
            subQuest = new NewCharQuest(true);
            clanQuestNew = new NewCharQuest(true);
            inCountry = 0;
            myCountry = 0;
            region = 0;
            map = m;
            mapID = m.mapId;
            x = 400;
            y = 300;
        }

        @Override
        public void sendMessage(final Message m) {
            goi.add(m);
        }

        /** Co goi 90 (actorDie) cho quai {@code id} tu vi tri {@code tu}. */
        boolean coChet(final int id, final int tu) throws Exception {
            for (int i = tu; i < goi.size(); i++) {
                final Message m = goi.get(i);
                if (m.command == 90) {
                    final DataInputStream in = new DataInputStream(new ByteArrayInputStream(m.toByteArray()));
                    in.readByte();
                    in.readShort();
                    if (in.readShort() == id) {
                        return true;
                    }
                }
            }
            return false;
        }

        /** Moi chu (UTF dau tien sau tieu de) cua cac goi tu {@code tu} - de tim thong bao. */
        String chu(final int tu) {
            final StringBuilder sb = new StringBuilder();
            for (int i = tu; i < goi.size(); i++) {
                final byte[] b = goi.get(i).toByteArray();
                sb.append(new String(b, StandardCharsets.UTF_8)).append('\n');
            }
            return sb.toString();
        }
    }

    static Map banDo(final int loadMap, final int id) {
        final Map m = new Map();
        m.initKhoQuaiVaNguoi();
        m.mapId = id;
        m.mapIDLoadMap = loadMap;
        return m;
    }

    /** Boss Gym con song cua {@code p} trong bang quai cua {@code m}. */
    static List<Monster> bossCua(final Map m, final Char p) {
        final List<Monster> kq = new ArrayList<Monster>();
        for (final Monster q : m.monsters.get(0).values()) {
            if (BossLauDai.laGymCua(q, p) && q.isBoss) {
                kq.add(q);
            }
        }
        return kq;
    }

    /** Hang go cua ban do (nRegion 0) - lam dung viec MapVongLap / Region lam trong luong ban do. */
    static void donHang(final Map m) {
        @SuppressWarnings("unchecked")
        final java.util.Vector<Monster> h = (java.util.Vector<Monster>) m.tempRemoveMonster.get(0);
        while (h.size() > 0) {
            final Monster mt = h.remove(0);
            if (BossLauDai.goKhoiBang(m, m.monsters.get(0), mt)) {
                continue;
            }
            if (mt.map != null && mt.map.mapId == m.mapId && mt.hp > 0) {
                m.monsters.get(0).remove(mt.id);
            }
        }
    }

    static Object[] dongGym(final int stt, final String ma, final int cap, final int tpl, final String ten) {
        // {stt, ma, huyHieu, cap, template, tenBoss, hp, tc, thu, noPhut, thuongDau, thuongTuan}
        return new Object[]{Integer.valueOf(stt), ma, "Huy hiệu Đá", Integer.valueOf(cap), Integer.valueOf(tpl), ten,
            Integer.valueOf(5000), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(5), "", ""};
    }

    static NhiemVuMau buocGym(final String ma, final String stt) {
        final NhiemVuMau m = new NhiemVuMau();
        m.ma = ma;
        m.loai = "cua_ai";
        m.kieu = "ha_gym";
        m.mucTieuId = stt;
        m.ten = "Cửa ải: hạ Gym Pewter";
        m.mucTieuText = "Hạ Trưởng Gym Pewter: Golem.";
        m.soLuong = 1;
        m.cap = 1;
        m.truoc = "";
        return m;
    }

    static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
    }

    public static void chay() throws Exception {
        final boolean lauDaiCu = com.htbb.pokemon.LauDai.bat();
        final List<Object[]> gymCu = GymLauDai.GYM;
        final RealController rcCu = RealController.intance;
        final MonsterTemplate tplCu = Map.monsterTemplates.get(Integer.valueOf(-31777));
        final MonsterTemplate tplDeCu = Map.monsterTemplates.get(Integer.valueOf(2));
        final LinkedHashMap<String, NhiemVuMau> mau = new LinkedHashMap<String, NhiemVuMau>();
        mau.put("CA-03.2", buocGym("CA-03.2", "1"));
        final LinkedHashMap<String, NhiemVuMau> mauCu = GymKiem.datMau(mau);
        final java.util.HashMap<String, List<NhiemVuMau>> kieuCu = NhiemVuEngine.THEO_KIEU;
        final java.util.HashMap<String, List<NhiemVuMau>> kieu = new java.util.HashMap<String, List<NhiemVuMau>>();
        kieu.put("ha_gym", new ArrayList<NhiemVuMau>(mau.values()));
        NhiemVuEngine.THEO_KIEU = kieu;
        final List<NguoiChoi> moi = new ArrayList<NguoiChoi>();
        try {
            com.htbb.pokemon.LauDai.dat(true);
            final HashMap<String, String> bat = new HashMap<String, String>();
            bat.put("gym.bat", "1");
            com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, bat, false);
            // id quai: RealController that khong khoi tao trong test -> kho id rieng
            if (RealController.intance == null) {
                final Class<?> uc = Class.forName("sun.misc.Unsafe"); // khong goi constructor (khoi dong ca may chu)
                final java.lang.reflect.Field uf = uc.getDeclaredField("theUnsafe");
                uf.setAccessible(true);
                final Object u = uf.get(null);
                RealController.intance = (RealController) uc.getMethod("allocateInstance", Class.class).invoke(u, RealController.class);
            }
            if (RealController.intance.idGen == null) {
                final IDGen g = new IDGen(100);
                g.setMaxCatalory(2, new int[]{0, 1}, new int[]{200, 200});
                RealController.intance.idGen = g;
            }
            final MonsterTemplate tpl = new MonsterTemplate();
            tpl.id = -31777;
            tpl.name = "Golem";
            tpl.maxhp = 5000;
            tpl.level = 12;
            Map.monsterTemplates.put(Integer.valueOf(tpl.id), tpl);
            if (tplDeCu == null) { // mau de boss (boss.de.mau mac dinh 2)
                final MonsterTemplate tde = new MonsterTemplate();
                tde.id = 2;
                tde.name = "Geodude";
                tde.maxhp = 300;
                Map.monsterTemplates.put(Integer.valueOf(2), tde);
            }
            final List<Object[]> g = new ArrayList<Object[]>();
            g.add(dongGym(1, "C03.6", 12, tpl.id, "Trưởng Gym Pewter: Golem"));
            GymLauDai.GYM = g;

            final Map pewter = banDo(0, 301);
            final Map pallet = banDo(1, 1);
            final NguoiChoi p = new NguoiChoi(771001, 30, pewter);
            final NguoiChoi khac = new NguoiChoi(771002, 30, pewter);
            moi.add(p);
            moi.add(khac);
            pewter.allPlayers.get(0).add(p);
            pewter.allPlayers.get(0).add(khac);
            for (final NguoiChoi c : moi) {
                CharManager.instance.vChars.add(c);
            }
            final HoSoLauDai h = new HoSoLauDai();
            GymKiem.datHoSo(p, h);
            GymKiem.datHoSo(khac, new HoSoLauDai());
            final ConcurrentHashMap<String, long[]> t = new ConcurrentHashMap<String, long[]>();
            t.put("CA-03.2", new long[]{0L, 0L, 0L, 0L});
            GymKiem.datTienDo(p, t);

            // ---- 1. mot tran / nhan vat
            final String lan1 = GymLauDai.thachDauStt(p, 1);
            dung("thach dau lan 1: boss xuat hien canh minh", lan1.contains("xuất hiện ngay cạnh bạn"));
            BAO.bang("thach dau lan 1: dung 1 boss Gym cua minh", Integer.valueOf(1), Integer.valueOf(bossCua(pewter, p).size()));
            final Monster boss = bossCua(pewter, p).get(0);
            dung("boss Gym: khong hoi sinh (khongHoiSinh)", boss.khongHoiSinh);
            final String lan2 = GymLauDai.thachDauStt(p, 1);
            dung("bam lai (So tay / bam dup): chi ve boss dang co, khong trieu hoi", lan2.startsWith("Bạn đang thách đấu Trưởng Gym Pewter: Golem")
                    && lan2.contains("ngay gần bạn") && lan2.contains("phút"));
            KhoaBanDoGame.hoiGym(p, 1, "Thách đấu Trưởng Gym Pewter ngay?");
            KhoaBanDoGame.khiTraLoi(p, true);
            MenuDoiThuong.chon(p, 0, null); // muc tat dau Quầy đổi thưởng (dang lam buoc ha Gym) -> hop 95
            KhoaBanDoGame.khiTraLoi(p, true);
            GymLauDai.chon(p, 0); // So tay -> Thách đấu Gym -> dong 0
            BAO.bang("hop 95 + muc tat Quay + So tay sau khi da co tran: van 1 boss", Integer.valueOf(1), Integer.valueOf(bossCua(pewter, p).size()));
            BAO.bang("gymDangDanh = stt Gym dang mo", Integer.valueOf(1), Integer.valueOf(BossLauDai.gymDangDanh(p)));

            // nhieu luong cung luc (bam dup tren client cham / hai goi -30 xu ly song song)
            final NguoiChoi p2 = new NguoiChoi(771003, 30, pallet);
            moi.add(p2);
            CharManager.instance.vChars.add(p2);
            GymKiem.datHoSo(p2, new HoSoLauDai());
            final CountDownLatch xuat = new CountDownLatch(1);
            final List<Thread> ds = new ArrayList<Thread>();
            for (int i = 0; i < 8; i++) {
                final Thread th = new Thread(() -> {
                    try {
                        xuat.await();
                        GymLauDai.thachDauStt(p2, 1);
                    } catch (final Exception ignored) {
                    }
                });
                th.start();
                ds.add(th);
            }
            xuat.countDown();
            for (final Thread th : ds) {
                th.join(5000);
            }
            BAO.bang("8 luong thach dau cung luc: dung 1 boss", Integer.valueOf(1), Integer.valueOf(bossCua(pallet, p2).size()));

            // ---- 2. nguoi khac khong danh duoc boss Gym cua minh (trong lang), van thay (khong an)
            sai("boss Gym cua nguoi khac: khong phai Gym cua minh", BossLauDai.laGymCua(boss, khac));

            // ---- 3. thang: go ngay cho moi nguoi, buoc nhiem vu + huy hieu mot lan, khong hoi sinh
            final int tuKhac = khac.goi.size();
            final int tuChu = p.goi.size();
            boss.hp = 0;
            BossLauDai.khiChet(boss); // Monster.onDropItem -> BossLauDai.khiChet
            dung("thang: goi 90 boss toi nguoi khac trong khu", khac.coChet(boss.id, tuKhac));
            dung("thang: goi 90 boss toi chu", p.coChet(boss.id, tuChu));
            dung("thang: huy hieu Gym 1", GymLauDai.coHuyHieu(h, 1));
            BAO.bang("thang: buoc ha_gym CA-03.2 xong (cho nhan thuong)", Long.valueOf(1L), Long.valueOf(t.get("CA-03.2")[1]));
            dung("thang: thong bao nhan thuong o moi noi + ve lang nhanh",
                    p.chu(tuChu).contains("Nhiệm vụ của tôi → Đang làm") && p.chu(tuChu).contains("Dây thoát hiểm") && p.chu(tuChu).contains("Vé dịch chuyển"));
            // ve lang mien phi: +1 luot them; huy hieu dau tien: +1 Ve dich chuyen (1 lan)
            dung("thang: nhac Về làng miễn phí trong Sổ tay (còn 2 lượt, +1 nhờ thắng Gym)",
                    p.chu(tuChu).contains("Về làng miễn phí trong Sổ tay huấn luyện (còn 2 lượt, +1 nhờ thắng Gym)."));
            BAO.bang("thang: +1 luot Ve lang mien phi (luu ho so)", Integer.valueOf(1), Integer.valueOf(h.vlThem));
            BAO.bang("thang huy hieu dau: +1 Ve dich chuyen", Integer.valueOf(1), Integer.valueOf(p.potions[com.htbb.pokemon.VeDichChuyen.ID]));
            BAO.bang("thang huy hieu dau: co qua l4", Integer.valueOf(1), Integer.valueOf(h.vlQuaHuyHieu));
            dung("thang huy hieu dau: bao Nhận thêm 1 Vé dịch chuyển (quà huy hiệu đầu tiên)",
                    p.chu(tuChu).contains("Nhận thêm 1 Vé dịch chuyển (quà huy hiệu đầu tiên)"));
            final int bitTuan = h.gymTuanBit;
            BossLauDai.khiChet(boss); // goi lai (vd hai don cung guc) - khong thuong lan hai
            BossLauDai.donGym(new HashMap<Integer, Char>());
            BAO.bang("thang: khiChet lan hai / don 5 giay khong thuong them (bit tuan)", Integer.valueOf(bitTuan), Integer.valueOf(h.gymTuanBit));
            BAO.bang("thang: khiChet lan hai khong cong them luot / ve", "1/1", h.vlThem + "/" + p.potions[com.htbb.pokemon.VeDichChuyen.ID]);
            BAO.bang("thang: khong con tran Gym", Integer.valueOf(0), Integer.valueOf(BossLauDai.gymDangDanh(p)));
            boss.bornTime = System.currentTimeMillis() - 1000L;
            try {
                boss.update();
            } catch (final Exception ignored) {
            }
            dung("thang: 6 giay sau KHONG hoi sinh (truoc: day mau lai, boss mo coi)", boss.isDead && boss.hp <= 0);
            dung("thang: con trong bang quai cho hang go", pewter.monsters.get(0).containsValue(boss));
            donHang(pewter);
            sai("thang: hang go cua khu go boss khoi bang quai", pewter.monsters.get(0).containsValue(boss));
            BAO.bang("thang: khong con boss Gym nao cua minh tren ban do", Integer.valueOf(0), Integer.valueOf(bossCua(pewter, p).size()));
            // doi chung: quai thuong guc van hoi sinh nhu cu
            final Monster thuong = new Monster(pewter, tpl, 100, 100, 0);
            thuong.isDead = true;
            thuong.hp = 0;
            thuong.bornTime = System.currentTimeMillis() - 1000L;
            try {
                thuong.update();
            } catch (final Exception ignored) {
            }
            sai("doi chung: quai thuong guc qua timeReBorn van hoi sinh", thuong.isDead);

            // ---- 4. danh lai sau khi thang (1 lan / tuan) duoc ngay
            final String lai = GymLauDai.thachDauStt(p, 1);
            dung("sau khi thang: thach dau lai duoc (danh lai tuan)", lai.contains("xuất hiện ngay cạnh bạn"));
            final Monster boss2 = bossCua(pewter, p).get(0);

            // ---- 5. roi ban do: huy, go boss, bao, thach dau lai duoc
            final int tu5 = khac.goi.size();
            p.map = pallet;
            p.mapID = pallet.mapId;
            BossLauDai.huyGymCua(p, true); // goi 12 -500 o ban do moi
            dung("roi ban do: boss go (goi 90 toi nguoi o lai)", khac.coChet(boss2.id, tu5) && boss2.isDead && boss2.khongHoiSinh);
            BAO.bang("roi ban do: het tran", Integer.valueOf(0), Integer.valueOf(BossLauDai.gymDangDanh(p)));
            dung("roi ban do: bao chu thach dau lai duoc", p.chu(0).contains("rời khỏi bản đồ thách đấu"));
            BAO.bang("roi ban do: khong tinh thang", Integer.valueOf(bitTuan), Integer.valueOf(h.gymTuanBit));
            donHang(pewter);

            // don 5 giay (LauDaiJob) cung bat duoc roi ban do (khong qua -500)
            p.map = pewter;
            p.mapID = pewter.mapId;
            GymLauDai.thachDauStt(p, 1);
            final Monster boss3 = bossCua(pewter, p).get(0);
            p.map = pallet;
            final HashMap<Integer, Char> online = new HashMap<Integer, Char>();
            online.put(Integer.valueOf(p.charDBID), p);
            online.put(Integer.valueOf(p2.charDBID), p2);
            BossLauDai.donGym(online);
            dung("don 5 giay: chu o ban do khac -> go boss", boss3.isDead && BossLauDai.gymDangDanh(p) == 0);
            p.map = pewter;

            // ---- 6. thoat game / mat ket noi
            GymLauDai.thachDauStt(p, 1);
            final Monster boss4 = bossCua(pewter, p).get(0);
            BossLauDai.huyGymCua(p, false); // CharManager.kickPlayer
            dung("thoat game: go boss ngay", boss4.isDead && boss4.khongHoiSinh && BossLauDai.gymDangDanh(p) == 0);
            GymLauDai.thachDauStt(p, 1);
            final Monster boss5 = bossCua(pewter, p).get(0);
            online.remove(Integer.valueOf(p.charDBID));
            BossLauDai.donGym(online); // mat ket noi khong qua kickPlayer: don 5 giay thay chu khong online
            dung("mat ket noi (khong online): don 5 giay go boss", boss5.isDead && BossLauDai.gymDangDanh(p) == 0);
            online.put(Integer.valueOf(p.charDBID), p);

            // ---- 7. het gio (10 phut): thua, go boss + de, danh lai duoc
            final boolean[] m1 = {false};
            final Monster boss6 = BossLauDai.moGym(p, 1, tpl, 12, 5000, 10, 10, 1L, m1);
            dung("moGym 1 ms: tao moi", m1[0] && boss6 != null);
            BossLauDai.goiDe(boss6, 1);
            final List<Monster> de = new ArrayList<Monster>();
            for (final Monster q : pewter.monsters.get(0).values()) {
                if (q != boss6 && !q.isDead && q.khongHoiSinh && BossLauDai.laGymCua(q, p)) {
                    de.add(q);
                }
            }
            dung("de cua boss Gym: chu danh duoc trong lang, khong hoi sinh", !de.isEmpty());
            Thread.sleep(5L);
            final int tu7 = khac.goi.size();
            BossLauDai.donGym(online);
            dung("het gio: boss go (goi 90)", boss6.isDead && khac.coChet(boss6.id, tu7));
            boolean deGo = true;
            for (final Monster q : de) {
                deGo &= q.isDead && khac.coChet(q.id, tu7);
            }
            dung("het gio: de go cung boss", deGo);
            dung("het gio: bao 'Hết 10 phút' + thach dau lai duoc", p.chu(0).contains("Hết 10 phút"));
            BAO.bang("het gio: khong tinh thang", Integer.valueOf(bitTuan), Integer.valueOf(h.gymTuanBit));
            sai("het gio: khong con tran (thach dau lai duoc ngay)", GymLauDai.thachDauStt(p, 1).startsWith("Bạn đang thách đấu"));
            BossLauDai.huyGymCua(p, false);
            BossLauDai.huyGymCua(p2, false);
            donHang(pewter);
            donHang(pallet);
            BAO.bang("don xong: bang quai Pewter khong con quai trieu hoi", Integer.valueOf(0),
                    Integer.valueOf((int) pewter.monsters.get(0).values().stream().filter(q -> q.khongHoiSinh).count()));

            // ---- 8. ban do dac biet: khong trieu hoi
            final Map ld = new MapTown(); // Cao nguyen 201: duoc
            dung("Cao nguyen (MapTown) trieu hoi duoc", GymLauDai.banDoDuocTrieu(ld));
            sai("ban do lop con khac (pho ban / dau truong / bang...): khong", GymLauDai.banDoDuocTrieu(new Map() {
            }));

            // ---- 9. So tay / Nhiem vu cua toi / nhan thuong o ban do bat ky (khong o canh NPC, khong o lang)
            p.map = pallet;
            p.mapID = pallet.mapId;
            p.x = 3000;
            p.y = 3000;
            int tu9 = p.goi.size();
            SoTayLauDai.doMenu(p, 0, 8); // client 0.9.15 nut «Mở Nhiệm vụ của tôi» = -527/0/8
            dung("Pallet, xa moi NPC: -527/0/8 mo menu Nhiệm vụ của tôi", coMenu(p, tu9));
            tu9 = p.goi.size();
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            final DataOutputStream d = new DataOutputStream(bo);
            d.writeShort(SoTayLauDai.NPC_ID);
            d.writeByte(0);
            d.writeByte(8);
            pallet.doMenu(p, new Message(-30, bo.toByteArray()));
            dung("Pallet: goi -30 that (Map.doMenu) -527/0/8 -> menu, khong kiem vi tri", coMenu(p, tu9));
            tu9 = p.goi.size();
            SoTayLauDai.doMenu(p, 0, 7);
            dung("Pallet: So tay -> Thách đấu Gym mo menu", coMenu(p, tu9));
            BAO.bang("buoc CA-03.2 dang cho nhan thuong", Long.valueOf(1L), Long.valueOf(t.get("CA-03.2")[1]));
            MucTieuNhiemVuGame.khiGoiCuaAi(p, 2, MucTieuNhiemVuGame.idCuaAi("CA-03.2")); // the cua ai: nut Huỷ (client cu)
            BAO.bang("the cua ai da xong + nut Huỷ o Pallet = nhan thuong tai cho", Long.valueOf(2L), Long.valueOf(t.get("CA-03.2")[1]));
            t.get("CA-03.2")[1] = 1;
            MucTieuNhiemVuGame.khiGoiCuaAi(p, 1, MucTieuNhiemVuGame.idCuaAi("CA-03.2")); // nut Trả
            BAO.bang("the cua ai da xong + nut Trả o Pallet = nhan thuong", Long.valueOf(2L), Long.valueOf(t.get("CA-03.2")[1]));
            final String mt = com.htbb.pokemon.DuongMenu.NHAN_THUONG_MOI_NOI;
            dung("chi tiet cua ai: nhan thuong o moi noi, khong bat ve Quay", mt.startsWith("Nhiệm vụ của tôi → Đang làm")
                    && mt.contains("ở đâu cũng được"));

            // ---- 10. ma nguon: hang go cua khu xu ly quai trieu hoi; moc thoat / vao ban do; khong luu boss
            final String rg = doc("src/real/Region.java");
            final String vl = doc("src/real/MapVongLap.java");
            BAO.bang("Region: 3 hang go (nuoc 0/1/2) go quai trieu hoi ke ca da guc", Integer.valueOf(3),
                    Integer.valueOf(rg.split("BossLauDai.goKhoiBang\\(", -1).length - 1));
            BAO.bang("MapVongLap: 3 hang go", Integer.valueOf(3), Integer.valueOf(vl.split("BossLauDai.goKhoiBang\\(", -1).length - 1));
            dung("CharManager.kickPlayer: huy tran Gym", doc("src/real/CharManager.java").contains("BossLauDai.huyGymCua(p, false);"));
            dung("goi 12 -500: huy tran Gym o ban do cu", doc("src/real/MapXuLyLenh.java").contains("BossLauDai.huyGymCua(player, true);"));
            final String bl = doc("src/real/BossLauDai.java");
            sai("boss Gym khong luu CSDL (khoi dong lai = khong boss mo coi)", bl.contains("DatabaseLauDai.luuBoss") || bl.contains("INSERT"));
            final String gl = doc("src/real/GymLauDai.java");
            BAO.bang("GymLauDai: mot cho mo tran (BossLauDai.moGym)", Integer.valueOf(1), Integer.valueOf(gl.split("BossLauDai.moGym\\(", -1).length - 1));
        } finally {
            for (final NguoiChoi c : moi) {
                try {
                    BossLauDai.huyGymCua(c, false);
                    CharManager.instance.vChars.remove(c);
                    GymKiem.datHoSo(c, null);
                    GymKiem.datTienDo(c, null);
                    KhoaBanDoGame.xoaHoi(c.charDBID);
                    MenuDoiThuong.khiThoat(c);
                } catch (final Exception ignored) {
                }
            }
            GymLauDai.GYM = gymCu;
            GymKiem.datMau(mauCu);
            NhiemVuEngine.THEO_KIEU = kieuCu;
            if (tplCu == null) {
                Map.monsterTemplates.remove(Integer.valueOf(-31777));
            }
            RealController.intance = rcCu;
            if (tplDeCu == null) {
                Map.monsterTemplates.remove(Integer.valueOf(2));
            }
            com.htbb.pokemon.LauDai.dat(lauDaiCu);
            com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, null, false);
        }
    }

    /** Co goi -30 (menu) tu vi tri {@code tu}. */
    static boolean coMenu(final NguoiChoi p, final int tu) {
        for (int i = tu; i < p.goi.size(); i++) {
            if (p.goi.get(i).command == -30) {
                return true;
            }
        }
        return false;
    }
}
