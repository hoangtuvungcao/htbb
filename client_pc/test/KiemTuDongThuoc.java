import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM TỰ ĐỘNG UỐNG THUỐC (0.9.22, chủ game: "hết PP không tự sử dụng khi đang dùng auto" - ảnh: Auto đánh, PP 1/3769,
 * 678 bình PP ở ô nhanh, Auto không uống). Headless, máy chủ giả ghi gói 22 (dùng thuốc) và gói đánh.
 *  1) PP dưới ngưỡng -> uống bình PP (bình nhỏ nhất đủ bù; không đủ thì mạnh nhất đang có);
 *  2) ngưỡng PP = Tắt nhưng PP dưới chiêu tốn PP rẻ nhất -> vẫn uống; PP đủ -> không;
 *  3) HP dưới ngưỡng mà hết bình HP -> vẫn uống PP (nhánh HP không chặn PP); có bình HP -> HP trước, PP lần sau;
 *  4) cách nhau KHOANG_BINH; bình đang hồi (data_potion.delay) -> không dùng bình đó, dùng loại khác;
 *  5) hết bình PP -> không gửi gói, trạng thái "hết bình PP", báo một lần;
 *  6) máy chủ điều khiển (cờ 16): máy chủ chọn chiêu 5 PP mà PP client 1 -> client tự uống bình PP; hết bình -> đánh
 *     chiêu 0 PP thay vì đứng im; trạng thái hiện "hết bình PP".
 *
 *   APP_IMAGE=0 sh client_pc/build.sh
 *   javac -cp client_pc/out/build/cls -sourcepath client_pc/test:server/src -d $O client_pc/test/ChupTrongGame.java \
 *     client_pc/test/KiemTuDongThuoc.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTuDongThuoc <thư mục>
 * Mã thoát 1 = có lỗi.
 */
public class KiemTuDongThuoc {
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

    /** Loại thuốc trong các gói 22 đã gửi. */
    static List<Integer> thuoc() {
        List<Integer> r = new ArrayList<Integer>();
        synchronized (LENH) {
            for (int i = 0; i < LENH.size(); i++) {
                if (LENH.get(i) == 22 && GOI.get(i).length >= 1) r.add(GOI.get(i)[0] & 0xFF);
            }
        }
        return r;
    }

    static int demDanh() {
        int n = 0;
        synchronized (LENH) {
            for (int c : LENH) if (c == 6 || c == 9 || c == 106 || c == -113) n++;
        }
        return n;
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

    static final boolean[] KQ = new boolean[1];

    /** Gọi TuDong.binh(gs, mc, now, coHp) trên luồng game. */
    static boolean binh(final boolean coHp) throws Exception {
        KQ[0] = false;
        ChupTrongGame.chay(() -> {
            try {
                Method m = TD.getDeclaredMethod("binh", game.render.screen.GameScr.class, game.model.MainChar.class, long.class, boolean.class);
                m.setAccessible(true);
                KQ[0] = (Boolean) m.invoke(null, gs(), mc(), System.currentTimeMillis(), coHp);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        return KQ[0];
    }

    /** Cảnh sạch: không bình nào, HP/PP đầy, hết khoảng chờ, ngưỡng 50 / 30, thứ tự chiêu 1, 2 (5 PP). */
    static void canh() throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                game.model.MainChar m = mc();
                for (int i = 0; i < m.potions.length; i++) {
                    m.potions[i] = 0;
                    m.potionLastTimeUse[i] = 0;
                }
                for (int i = 0; i < game.model.MainChar.listPotion.length; i++) game.model.MainChar.listPotion[i].delay = 350;
                m.maxhp = 100;
                m.hp = 100;
                m.maxmp = 100;
                m.mp = 100;
                m.isDie = false;
                m.state = 0;
                dat("lucBinh", 0L);
                dat("nguongHp", 50);
                dat("nguongMp", 30);
                dat("hetBinhPP", false);
                dat("daBaoHetPP", false);
                dat("svCo", 1 | 2);
                byte[] tt = (byte[]) lay("thuTu");
                tt[0] = 1;
                tt[1] = 2;
                tt[2] = -1;
                tt[3] = -1;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        xoaGoi();
    }

    static void hetKhoang() throws Exception {
        dat("lucBinh", 0L);
    }

    static void chuanBi() throws Exception {
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.model.MainChar m = mc();
            m.clazz = 1;
            // như máy chủ thật: mảng theo bảng thuốc 163 dòng (giaLap đặt CHAR_POTION sau khi nhân vật đã tạo)
            if (m.potionLastTimeUse.length < m.potions.length) m.potionLastTimeUse = new long[m.potions.length];
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
                    st[0][k][l] = 100;
                }
            }
            game.model.skill.SkillManager.SKILL_RANGE = tam;
            game.model.skill.SkillManager.SKILL_MP = pp;
            game.model.skill.SkillManager.SKILL_COOLDOWN = hoi;
            game.model.skill.SkillManager.SKILL_DAM_PERCENT = st;
            game.model.skill.SkillManager.SKILL_AEO = new byte[5][0];
            if (game.model.MainChar.quickSlot == null) m.loadQuickSlot();
        });
    }

    // ------------------------------------------------------------------ phần kiểm

    static void kiemNguong() throws Exception {
        // 1) PP 20% < 30%, có Elixir tối đa (24) + Ether (4): thiếu 80 -> Ether 160 đủ
        canh();
        ChupTrongGame.chay(() -> {
            mc().mp = 20;
            mc().potions[24] = 678;
            mc().potions[4] = 3;
        });
        kiem(binh(true), "1) PP 20% < 30%: uong");
        kiem(thuoc().equals(java.util.Arrays.asList(4)), "1) binh nho nhat du bu (Ether 4): " + thuoc());
        // ảnh chủ game: PP 1/3769, chỉ có 24 -> 24
        canh();
        ChupTrongGame.chay(() -> {
            mc().maxmp = 3769;
            mc().mp = 1;
            mc().maxhp = 8752;
            mc().hp = 5731;
            mc().potions[24] = 678;
            mc().potions[94] = 197;
        });
        kiem(binh(true), "1) anh chu game PP 1/3769: uong");
        kiem(thuoc().equals(java.util.Arrays.asList(24)), "1) anh chu game: binh 24 (khong uong HP 65% > 50%): " + thuoc());
        // 4) ngay sau đó: khoảng chờ -> không uống lại
        xoaGoi();
        kiem(!binh(true) && thuoc().isEmpty(), "4) vua uong: cho KHOANG_BINH, khong uong lien: " + thuoc());
    }

    static void kiemHetPP() throws Exception {
        // 2) ngưỡng PP = Tắt, PP 3 < chiêu rẻ nhất 5 -> vẫn uống
        canh();
        dat("nguongMp", 0);
        ChupTrongGame.chay(() -> {
            mc().mp = 3;
            mc().potions[5] = 2;
        });
        kiem(binh(true) && thuoc().equals(java.util.Arrays.asList(5)), "2) nguong PP Tat, PP 3 < chieu re nhat 5: van uong: " + thuoc());
        canh();
        dat("nguongMp", 0);
        ChupTrongGame.chay(() -> {
            mc().mp = 6;
            mc().potions[5] = 2;
        });
        kiem(!binh(true) && thuoc().isEmpty(), "2) nguong PP Tat, PP 6 du chieu: khong uong: " + thuoc());
        // máy chủ tắt thuốc (cờ 2) -> không bao giờ
        canh();
        dat("svCo", 1);
        ChupTrongGame.chay(() -> {
            mc().mp = 1;
            mc().potions[24] = 5;
        });
        kiem(!binh(true) && thuoc().isEmpty(), "2) may chu tat thuoc: khong uong");
    }

    static void kiemHpKhongChan() throws Exception {
        // 3) HP 10% < 50% nhưng hết bình HP, PP 10% -> uống PP
        canh();
        ChupTrongGame.chay(() -> {
            mc().hp = 10;
            mc().mp = 10;
            mc().potions[24] = 5;
        });
        kiem(binh(true) && thuoc().equals(java.util.Arrays.asList(24)), "3) het binh HP: nhanh PP van uong: " + thuoc());
        // có bình HP: HP trước, PP ở lần sau
        canh();
        ChupTrongGame.chay(() -> {
            mc().hp = 10;
            mc().mp = 10;
            mc().potions[24] = 5;
            mc().potions[3] = 5;
        });
        kiem(binh(true) && thuoc().equals(java.util.Arrays.asList(3)), "3) co binh HP: HP truoc: " + thuoc());
        hetKhoang();
        kiem(binh(true) && thuoc().equals(java.util.Arrays.asList(3, 24)), "3) binh HP dang hoi (350 ms): khong chan PP: " + thuoc());
        // HP đã lên (giả máy chủ trả) -> PP
        ChupTrongGame.chay(() -> mc().hp = 90);
        ngu(400);
        hetKhoang();
        xoaGoi();
        kiem(binh(true) && thuoc().equals(java.util.Arrays.asList(24)), "3) HP du: PP: " + thuoc());
        // chế độ máy chủ điều khiển (coHp = false): HP thấp không uống HP, vẫn uống PP
        canh();
        ChupTrongGame.chay(() -> {
            mc().hp = 10;
            mc().mp = 10;
            mc().potions[24] = 5;
            mc().potions[3] = 5;
        });
        kiem(binh(false) && thuoc().equals(java.util.Arrays.asList(24)), "3) ke hoach may chu: chi PP (HP may chu quyet): " + thuoc());
    }

    static void kiemHoi() throws Exception {
        // 4) bình 24 đang hồi (delay 5 giây) -> dùng 23; chỉ có 24 -> không uống
        canh();
        ChupTrongGame.chay(() -> {
            game.model.MainChar.listPotion[24].delay = 5000;
            mc().mp = 1;
            mc().potions[24] = 5;
            mc().potionLastTimeUse[24] = System.currentTimeMillis();
        });
        kiem(!binh(true) && thuoc().isEmpty(), "4) binh 24 dang hoi: khong uong: " + thuoc());
        kiem(!(Boolean) lay("hetBinhPP"), "4) binh dang hoi khong phai het binh");
        ChupTrongGame.chay(() -> mc().potions[23] = 5);
        kiem(binh(true) && thuoc().equals(java.util.Arrays.asList(23)), "4) 24 dang hoi: dung 23: " + thuoc());
        // hai lần cách KHOANG_BINH thật
        canh();
        ChupTrongGame.chay(() -> {
            mc().mp = 1;
            mc().potions[6] = 9;
        });
        kiem(binh(true), "4) lan 1");
        ngu(600);
        kiem(!binh(true), "4) 0,6 giay sau: chua");
        ngu(1100);
        kiem(binh(true), "4) 1,7 giay sau: uong lai");
        kiem(thuoc().equals(java.util.Arrays.asList(6, 6)), "4) dung hai lan: " + thuoc());
    }

    static void kiemHetBinh() throws Exception {
        // 5) hết bình PP: không gói, trạng thái rõ, báo một lần
        canh();
        ChupTrongGame.chay(() -> {
            mc().mp = 1;
            mc().potions[94] = 197;
        });
        for (int i = 0; i < 5; i++) {
            hetKhoang();
            binh(true);
        }
        kiem(thuoc().isEmpty(), "5) het binh PP: khong gui goi thuoc: " + thuoc());
        kiem((Boolean) lay("hetBinhPP") && (Boolean) lay("daBaoHetPP"), "5) co het binh PP + da bao");
        dat("trangThai", "đang đánh");
        Method m = TD.getDeclaredMethod("trangThaiHien");
        m.setAccessible(true);
        String tt = (String) m.invoke(null);
        kiem("hết bình PP - đang đánh".equals(tt), "5) trang thai: " + tt);
        dat("trangThai", "hết bình PP - đánh - chiêu kém hiệu quả");
        tt = (String) m.invoke(null);
        kiem("hết bình PP - đánh - chiêu kém hiệu quả".equals(tt), "5) khong lap tien to (may chu da ghi): " + tt);
        // có bình lại -> hết cờ, lần sau hết lại thì báo lại
        ChupTrongGame.chay(() -> mc().potions[4] = 1);
        hetKhoang();
        kiem(binh(true) && !(Boolean) lay("hetBinhPP") && !(Boolean) lay("daBaoHetPP"), "5) co binh lai: uong, xoa co");
    }

    // ------------------------------------------------------------------ máy chủ điều khiển

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
        o.writeUTF("đánh - chiêu kém hiệu quả");
        final network.Message msg = goiNhan(-100, bo.toByteArray());
        ChupTrongGame.chay(() -> game.render.screen.TuDong.nhan(msg));
    }

    /** Chạy Tự động (kế hoạch máy chủ: luôn "đánh chiêu so") trong ms. */
    static void chayKeHoach(int so, long ms) throws Exception {
        long het = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < het) {
            keHoachDanh(so);
            ngu(250);
        }
    }

    static void kiemKeHoach() throws Exception {
        canh();
        ChupTrongGame.chay(() -> {
            try {
                game.render.Canvas.endDlg();
                game.render.Canvas.currentDialog = null;
                game.render.Canvas.menu.showMenu = false;
                game.render.screen.GameScr g = gs();
                game.model.MainChar m = mc();
                game.render.screen.GameScr.autoFight = false;
                for (int i = 0; i < m.timeLastUseSkills.length; i++) {
                    m.timeLastUseSkills[i] = 0;
                    m.coolDownSkill[i] = 0;
                }
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
                m.mp = 1;
                m.maxmp = 3769;
                m.potions[24] = 678;
                dat("coServer", true);
                dat("svCo", 1 | 2 | 16);
                dat("neoMap", g.map);
                dat("neoX", (int) m.x);
                dat("neoY", (int) m.y);
                dat("lucNhip", System.currentTimeMillis() + 600000L);
                dat("lucBoTro", System.currentTimeMillis() + 600000L);
                game.render.Canvas.clearKeyPressed();
                game.render.Canvas.clearKeyHold();
                game.render.screen.TuDong.dang = true;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        xoaGoi();
        chayKeHoach(2, 1200);
        List<Integer> t = thuoc();
        kiem(t.size() == 1 && t.get(0) == 24, "6) may chu chon chieu 5 PP, PP client 1: client tu uong binh PP 24 dung 1 lan: " + t);
        // hết bình: đánh chiêu 0 PP, không đứng im
        ChupTrongGame.chay(() -> {
            mc().potions[24] = 0;
            mc().mp = 1;
            mc().state = 0;
            for (int i = 0; i < mc().timeLastUseSkills.length; i++) mc().timeLastUseSkills[i] = 0;
        });
        xoaGoi();
        chayKeHoach(2, 1500);
        ChupTrongGame.chay(() -> game.render.screen.TuDong.dang = false);
        kiem(thuoc().isEmpty(), "6) het binh: khong goi thuoc: " + thuoc());
        kiem(demDanh() > 0, "6) het binh: van danh (chieu 0 PP) thay vi dung im: goi " + LENH + " tt=" + lay("trangThai") + " doing=" + mc().isDoing + " st=" + mc().state);
        kiem((Boolean) lay("hetBinhPP"), "6) het binh: co hetBinhPP");
        Method m = TD.getDeclaredMethod("trangThaiHien");
        m.setAccessible(true);
        String tt = (String) m.invoke(null);
        kiem(tt != null && tt.startsWith("hết bình PP"), "6) trang thai: " + tt);
        ChupTrongGame.chay(() -> gs().actors.removeElement(quai));
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "kiem_tu_dong_thuoc");
        ra.mkdirs();
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
        chuanBi();
        kiemNguong();
        kiemHetPP();
        kiemHpKhongChan();
        kiemHoi();
        kiemHetBinh();
        kiemKeHoach();
        System.out.println("KiemTuDongThuoc: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
