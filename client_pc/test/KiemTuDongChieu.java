import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM TỰ ĐỘNG DÙNG CHIÊU ĐÃ GÁN (0.9.24, chủ game: "jar bật auto không dùng các chiêu đã gán à?"). Trước: máy chủ lập
 * kế hoạch (tu_dong.ke_hoach=1) chọn trong MỌI chiêu đã học (real.TuDongGame.tinhHinh), client làm đúng chiêu máy chủ
 * chọn -> người chơi gán chiêu 1, 3 vào ô nhưng Tự động đánh chiêu 2 (mạnh hơn, không gán). Headless, máy chủ giả ghi gói.
 *  1) J2ME phím: Kỹ năng -> "Cho vào phím tắt" -> menu "Phím số 1" / "Phím số 3" (phím mềm Chọn) gán chiêu 1, 3 vào trang
 *     1; gói -103 loại 1 gửi máy chủ, máy chủ giải trang (TuDongKeHoach.chieuTrongTrang) = [1, 3];
 *  2) danh sách chiêu Tự động (TuDong.chieuGan / dsChieu) = [1, 3]; "Thứ tự chiêu" tự đặt -> theo thứ tự đó;
 *  3) lệnh -100 loại 4 (xin kế hoạch) kèm danh sách: 4, 1, cờ, n, chiêu...; máy chủ đọc (real.TuDongGame.docChieuGan) và
 *     bộ não (TuDongKeHoach) chỉ chọn 1 / 3, không bao giờ 2; chiêu gán đang hồi -> đánh thường 0;
 *  4) máy chủ CŨ (không biết chiêu gán) bảo đánh chiêu 2: client đánh chiêu đã gán (gói 9 / 106 mang chiêu 1 / 3),
 *     không đánh chiêu 2; máy chủ bảo chiêu 3 (đã gán) -> đánh 3.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh
 *   javac -cp client_pc/out/build/cls -sourcepath client_pc/test:server/src -d $O client_pc/test/ChupTrongGame.java \
 *     client_pc/test/KiemTuDongChieu.java
 *   java -Djava.awt.headless=true -Dhtbb.khongCham=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O \
 *     KiemTuDongChieu <thư mục>
 * (-Dhtbb.khongCham=true = máy J2ME chỉ có phím, cỡ 240x320.) Mã thoát 1 = có lỗi.
 */
public class KiemTuDongChieu {
    static int dat, hong;
    static final List<Integer> LENH = new ArrayList<Integer>();
    static final List<byte[]> GOI = new ArrayList<byte[]>();
    static final Class<?> TD = game.render.screen.TuDong.class;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static void ngu(long ms) throws Exception {
        Thread.sleep(ms);
    }

    static game.render.screen.GameScr gs() {
        return game.render.Canvas.gameScr;
    }

    static game.model.MainChar mc() {
        return gs().mainChar;
    }

    static void xoaGoi() {
        synchronized (LENH) {
            LENH.clear();
            GOI.clear();
        }
    }

    static List<byte[]> goi(int lenh) {
        List<byte[]> r = new ArrayList<byte[]>();
        synchronized (LENH) {
            for (int i = 0; i < LENH.size(); i++) if (LENH.get(i) == lenh) r.add(GOI.get(i));
        }
        return r;
    }

    /** Chiêu trong các gói đánh quái (9 / 106: short id, byte chiêu). */
    static List<Integer> chieuDanh() {
        List<Integer> r = new ArrayList<Integer>();
        synchronized (LENH) {
            for (int i = 0; i < LENH.size(); i++) {
                int c = LENH.get(i);
                if ((c == 9 || c == 106) && GOI.get(i).length >= 3) r.add((int) GOI.get(i)[2]);
            }
        }
        return r;
    }

    static void dat(String ten, Object gt) throws Exception {
        Field f = TD.getDeclaredField(ten);
        f.setAccessible(true);
        f.set(null, gt);
    }

    static Object lay(String ten) throws Exception {
        Field f = TD.getDeclaredField(ten);
        f.setAccessible(true);
        return f.get(null);
    }

    static Object goiTD(String ten, Class<?>[] kieu, Object... gt) throws Exception {
        Method m = TD.getDeclaredMethod(ten, kieu);
        m.setAccessible(true);
        return m.invoke(null, gt);
    }

    static final Object[] KQ = new Object[1];

    static String ds(byte[] b) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; b != null && i < b.length; i++) s.append(i > 0 ? "," : "").append(b[i]);
        return s.append(']').toString();
    }

    static byte[] chieuGan() throws Exception {
        KQ[0] = null;
        ChupTrongGame.chay(() -> {
            try {
                KQ[0] = goiTD("chieuGan", new Class<?>[]{game.model.MainChar.class}, mc());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        return (byte[]) KQ[0];
    }

    static void chuanBi() throws Exception {
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.model.MainChar m = mc();
            m.clazz = 1;   // dòng 1: chiêu 4, 5 là bổ trợ
            game.model.Char.skillLevelLearnt = new byte[]{1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
            short[][] tam = new short[1][12];
            short[][][] pp = new short[1][12][11];
            int[][][] hoi = new int[1][12][11];
            short[][][] st = new short[1][12][11];
            for (int k = 0; k < 12; k++) {
                tam[0][k] = 80;
                for (int l = 0; l < 11; l++) {
                    pp[0][k][l] = (short) (k == 0 ? 0 : 5);
                    hoi[0][k][l] = 300;
                    st[0][k][l] = (short) (k == 2 ? 300 : 100);
                }
            }
            game.model.skill.SkillManager.SKILL_RANGE = tam;
            game.model.skill.SkillManager.SKILL_MP = pp;
            game.model.skill.SkillManager.SKILL_COOLDOWN = hoi;
            game.model.skill.SkillManager.SKILL_DAM_PERCENT = st;
            game.model.skill.SkillManager.SKILL_AEO = new byte[5][0];
            if (game.model.MainChar.quickSlot == null) m.loadQuickSlot();
            for (int t = 0; t < game.model.MainChar.quickSlot.length; t++) {
                for (int i = 0; i < 5; i++) game.model.MainChar.quickSlot[t][i].setIsNothing();
            }
            game.render.screen.GameScr.indexTabSlot = 0;
            game.pokemon.TrangChieu.coMayChu = true;   // máy chủ giữ trang chiêu (client >= 908)
            try {
                byte[] tt = (byte[]) lay("thuTu");
                for (int i = 0; i < tt.length; i++) tt[i] = -1;
                dat("trangChieu", -1);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    /** J2ME: Kỹ năng -> "Cho vào phím tắt" -> menu "Phím số N" bằng phím mềm Chọn. */
    static void ganBangPhim(final int chieu, final int oMenu) throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                game.render.screen.WindowInfoScr w = game.render.screen.WindowInfoScr.gI();
                Method m = game.render.screen.WindowInfoScr.class.getDeclaredMethod("doGiveSkillToQuickSlot", int.class, boolean.class);
                m.setAccessible(true);
                m.invoke(w, chieu, false);
                game.render.screen.Menu mn = game.render.Canvas.menu;
                mn.selected = oMenu;
                mn.left.action.perform();   // phím mềm trái "Chọn"
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    static void kiemGanPhim() throws Exception {
        xoaGoi();
        ganBangPhim(1, 0);   // "Phím số 1"
        ganBangPhim(3, 1);   // "Phím số 3"
        game.model.QuickSlot[] t = game.model.MainChar.quickSlot[0];
        kiem(t[0].quickslotType == game.model.QuickSlot.TYPE_SKILL && t[0].skillType == 1, "1) phim so 1 = chieu 1: " + t[0].quickslotType + "/" + t[0].skillType);
        kiem(t[1].quickslotType == game.model.QuickSlot.TYPE_SKILL && t[1].skillType == 3, "1) phim so 3 = chieu 3: " + t[1].quickslotType + "/" + t[1].skillType);
        List<byte[]> g = goi(game.pokemon.TrangChieu.LENH);
        kiem(!g.isEmpty(), "1) gan xong gui -103 loai 1 len may chu");
        if (!g.isEmpty()) {
            java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(g.get(g.size() - 1)));
            int loai = r.readUnsignedByte();
            int trang = r.readUnsignedByte();
            long t1 = r.readLong();
            int[] mayChu = com.htbb.pokemon.TuDongKeHoach.chieuTrongTrang(t1);
            kiem(loai == 1 && trang == 0 && java.util.Arrays.equals(mayChu, new int[]{1, 3}),
                    "1) may chu giai trang 1 = [1, 3]: loai " + loai + " trang " + trang + " " + java.util.Arrays.toString(mayChu));
        }
        byte[] gan = chieuGan();
        kiem(java.util.Arrays.equals(gan, new byte[]{1, 3}), "2) chieu Tu dong = chieu da gan [1,3]: " + ds(gan));
        KQ[0] = null;
        ChupTrongGame.chay(() -> {
            try {
                KQ[0] = goiTD("dsChieu", new Class<?>[]{game.model.MainChar.class}, mc());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        kiem(java.util.Arrays.equals((byte[]) KQ[0], new byte[]{1, 3}), "2) dsChieu = [1,3]: " + ds((byte[]) KQ[0]));
        byte[] tt = (byte[]) lay("thuTu");
        tt[0] = 3;
        tt[1] = 1;
        gan = chieuGan();
        kiem(java.util.Arrays.equals(gan, new byte[]{3, 1}), "2) Thu tu chieu tu dat 3, 1: " + ds(gan));
        tt[0] = -1;
        tt[1] = -1;
    }

    static game.model.Monster quai;

    static network.Message goiNhan(int lenh, byte[] data) throws Exception {
        Constructor<network.Message> c = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        c.setAccessible(true);
        return c.newInstance((byte) lenh, data);
    }

    static void keHoachDanh(final int so) throws Exception {
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream o = new java.io.DataOutputStream(bo);
        o.writeByte(1);
        o.writeByte(1);   // HD_DANH
        o.writeShort(quai.ID);
        o.writeShort(so);
        o.writeShort(quai.x);
        o.writeShort(quai.y);
        o.writeUTF("đang đánh");
        final network.Message msg = goiNhan(-100, bo.toByteArray());
        ChupTrongGame.chay(() -> game.render.screen.TuDong.nhan(msg));
    }

    static void hetHoi() throws Exception {
        ChupTrongGame.chay(() -> {
            game.model.MainChar m = mc();
            for (int i = 0; i < m.timeLastUseSkills.length; i++) {
                m.timeLastUseSkills[i] = 0;
                m.coolDownSkill[i] = 0;
            }
            m.state = 0;
            m.isDoing = false;
            try {
                dat("lucDanh", 0L);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    static void chayKeHoach(int so, long ms) throws Exception {
        long het = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < het) {
            keHoachDanh(so);
            ngu(250);
        }
    }

    static void kiemKeHoach() throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                game.render.Canvas.endDlg();
                game.render.Canvas.currentDialog = null;
                game.render.Canvas.menu.showMenu = false;
                game.render.screen.GameScr g = gs();
                game.model.MainChar m = mc();
                game.render.screen.GameScr.autoFight = false;
                quai = new game.model.Monster();
                quai.ID = 3101;
                quai.catagory = 1;
                quai.setType((short) 0);
                quai.level = 3;
                quai.maxhp = 500000;
                quai.hp = 500000;
                quai.setRealHP(500000);
                quai.state = 0;
                quai.x = (short) (m.x + 24);
                quai.y = m.y;
                quai.xFirst = quai.x;
                quai.yFirst = quai.y;
                g.actors.addElement(quai);
                m.mp = 3000;
                m.maxmp = 3000;
                m.hp = m.maxhp = 3000;
                dat("coServer", true);
                dat("svCo", 1 | 2 | 16);
                dat("neoMap", g.map);
                dat("neoX", (int) m.x);
                dat("neoY", (int) m.y);
                dat("lucNhip", System.currentTimeMillis() + 600000L);
                dat("lucBoTro", System.currentTimeMillis() + 600000L);
                dat("lucBinh", System.currentTimeMillis() + 600000L);
                game.render.Canvas.clearKeyPressed();
                game.render.Canvas.clearKeyHold();
                game.render.screen.TuDong.dang = true;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        hetHoi();
        xoaGoi();
        // 3) xin kế hoạch kèm danh sách chiêu đã gán
        chayKeHoach(3, 1500);
        List<byte[]> xin = new ArrayList<byte[]>();
        for (byte[] b : goi(-100)) if (b.length > 0 && b[0] == 4) xin.add(b);
        kiem(!xin.isEmpty(), "3) Tu dong (may chu dieu khien) xin ke hoach -100 loai 4");
        if (!xin.isEmpty()) {
            byte[] b = xin.get(xin.size() - 1);
            kiem(b.length == 6 && b[1] == 1 && b[2] == 0 && b[3] == 2 && b[4] == 1 && b[5] == 3,
                    "3) goi xin ke hoach kem chieu gan [4,1,0,2,1,3]: " + ds(b));
            // máy chủ đọc gói + bộ não chọn trong chiêu đã gán
            com.htbb.pokemon.TuDong.Phien ph = new com.htbb.pokemon.TuDong.Phien();
            java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(b));
            r.readByte();
            Method doc = Class.forName("real.TuDongGame").getDeclaredMethod("docChieuGan", com.htbb.pokemon.TuDong.Phien.class, java.io.DataInputStream.class);
            doc.setAccessible(true);
            doc.invoke(null, ph, r);
            kiem(java.util.Arrays.equals(ph.chieuGan, new int[]{1, 3}) && !ph.thuTuNghiem, "3) may chu doc chieu gan [1,3]: " + java.util.Arrays.toString(ph.chieuGan));
            com.htbb.pokemon.TuDongKeHoach.TinhHinh t = new com.htbb.pokemon.TuDongKeHoach.TinhHinh();
            t.x = t.neoX = 400;
            t.y = t.neoY = 400;
            t.cap = 10;
            t.hp = t.maxhp = 1000;
            t.mp = t.maxmp = 100;
            t.thuoc = new int[200];
            for (int s = 0; s < 6; s++) {
                if (s == 4 || s == 5) continue;   // bổ trợ dòng 1 (máy chủ bỏ như real.TuDongGame.laBuff)
                t.chieu.add(new com.htbb.pokemon.TuDongKeHoach.Chieu(s, -1, s == 2 ? 300 : 100, s == 0 ? 0 : 5, 0L));
            }
            t.quai.add(new com.htbb.pokemon.TuDongKeHoach.Quai(1, 420, 400, 10, 100, 100));
            t.chieuGan = ph.chieuGan;
            t.thuTuNghiem = ph.thuTuNghiem;
            int so = com.htbb.pokemon.TuDongKeHoach.lap(new com.htbb.pokemon.TuDongKeHoach.CauHinh(), t).so;
            kiem(so == 1 || so == 3, "3) bo nao chon chieu da gan (khong chieu 2 manh hon chua gan): " + so);
            t.chieu.get(1).conHoi = 500L;
            t.chieu.get(3).conHoi = 500L;
            so = com.htbb.pokemon.TuDongKeHoach.lap(new com.htbb.pokemon.TuDongKeHoach.CauHinh(), t).so;
            kiem(so == 0, "3) chieu gan dang hoi: danh thuong 0 (khong chieu 2): " + so);
        }
        List<Integer> c3 = chieuDanh();
        kiem(!c3.isEmpty() && c3.contains(3) && !c3.contains(2), "4) may chu bao chieu 3 (da gan): danh 3: " + c3);
        // 4) máy chủ cũ bảo chiêu 2 (không gán)
        hetHoi();
        xoaGoi();
        long het = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < het) {
            keHoachDanh(2);
            ngu(250);
            hetHoi();
        }
        ChupTrongGame.chay(() -> game.render.screen.TuDong.dang = false);
        List<Integer> c = chieuDanh();
        kiem(!c.isEmpty(), "4) may chu cu bao chieu 2: van danh (khong dung im): " + c + " tt=" + lay("trangThai"));
        kiem(!c.contains(2), "4) may chu cu bao chieu 2 (chua gan): KHONG danh chieu 2: " + c);
        boolean chiGan = true;
        for (int s : c) chiGan &= s == 1 || s == 3 || s == 0;
        kiem(chiGan, "4) chi chieu da gan (1, 3) / danh thuong: " + c);
        ChupTrongGame.chay(() -> gs().actors.removeElement(quai));
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "kiem_tu_dong_chieu");
        ra.mkdirs();
        boolean j2me = Boolean.getBoolean("htbb.khongCham");
        javax.microedition.lcdui.NghiemMidp.bat = true;
        System.setProperty("htbb.duLieu", new java.io.File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) ngu(100);
        ngu(2500);
        game.networklogic.GameService.gI().setSession(new network.ISession() {
            public boolean isConnected() {
                return true;
            }

            public void setHandler(network.IMessageHandler h) {
            }

            public void connect(String s, String s2) {
            }

            public void sendMessage(network.Message m) {
                synchronized (LENH) {
                    LENH.add((int) m.command);
                    GOI.add(m.getData());
                }
            }

            public void close() {
            }
        });
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ngu(500);
        if (j2me) {
            // máy J2ME chỉ có phím 240x320 (vẽ như J2ME)
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            game.pokemon.ChuUI.bo = null;
            NenPc.rong = NenPc.thatW = NenPc.hienW = 240;
            NenPc.cao = NenPc.thatH = NenPc.hienH = 320;
            NenPc.heSo = 1;
            NenPc.hienX = NenPc.hienY = 0;
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            ngu(200);
            kiem(!game.render.Canvas.instance.hasPointerEvents(), "J2ME: may chi co phim");
        }
        chuanBi();
        kiemGanPhim();
        kiemKeHoach();
        System.out.println("KiemTuDongChieu" + (j2me ? " (J2ME 240x320)" : "") + ": " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
