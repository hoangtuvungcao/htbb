import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM CHIÊU BỔ TRỢ Ở Ô TỰ ĐỘNG (0.9.20, chủ game: "kỹ năng phụ trợ gắn vào các ô auto cho nó hoạt động"), headless,
 * máy chủ giả ghi gói và (tuỳ chọn) trả lời gói 51 như máy chủ thật (CharCapNhat.setBuff2Char: 51 kiểu 1 + thời lượng).
 * Nhân vật dòng Eevee (lớp 2: bổ trợ 4 = hiệu ứng 25, 7 = 23 dùng cho bản thân; 5 = 30 không dùng được (-1); 6 = 27
 * hồi sinh). Trang 0: phím 1 = chiêu 4, 3 = chiêu đánh 1, 5 = chiêu 7, 7 = hồi sinh 6, 9 = chiêu 5.
 *  1) hiệu ứng không có -> dùng (gói 51 cho CHÍNH MÌNH, mỗi chiêu một lần), không bao giờ hồi sinh / chiêu -1;
 *  2) hiệu ứng còn -> không dùng lại (kể cả khi hồi chiêu đã xong);
 *  3) hết hiệu ứng -> dùng lại; vừa dùng (hồi chiêu) -> không;
 *  4) thiếu PP -> không;
 *  5) mỗi chiêu theo hạn riêng (25 hết, 23 còn -> chỉ 25);
 *  6) vòng Auto gốc (GameScr.autoFight): có hiệu ứng bổ trợ / ô hồi sinh ở trang 2 -> chiêu đánh VẪN đánh, không gói 51,
 *     không hộp "chỉ hồi sinh người hết HP";
 *  7) Tự động đánh chính thức (TuDong, tự chọn quái): dùng bổ trợ đúng một lần mỗi chiêu (máy chủ giả trả lời) và vẫn đánh.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh
 *   javac -cp client_pc/out/build/cls -sourcepath client_pc/test:server/src -d $O client_pc/test/ChupTrongGame.java \
 *     client_pc/test/KiemTuBuff.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTuBuff <thư mục>
 * Mã thoát 1 = có lỗi.
 */
public class KiemTuBuff {
    static int dat, hong;
    static final List<Integer> LENH = new ArrayList<Integer>();
    static final List<byte[]> GOI = new ArrayList<byte[]>();
    /** máy chủ giả trả lời gói 51 (thời lượng giây) */
    static volatile boolean traLoi;
    static volatile int thoiLuong = 30;
    /** mọi hiệu ứng client đã xin cả phiên (kiểm hồi sinh không bao giờ) */
    static final List<Integer> MOI_HIEU_UNG = new ArrayList<Integer>();

    static final int LOP = 2;
    static final int HU_4 = 25, HU_5 = 30, HU_6 = 27, HU_7 = 23;

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

    static void xoaGoi() {
        synchronized (LENH) {
            LENH.clear();
            GOI.clear();
        }
    }

    static int demLenh(int lenh) {
        int n = 0;
        synchronized (LENH) {
            for (int c : LENH) if (c == lenh) n++;
        }
        return n;
    }

    static int demDanh() {
        return demLenh(6) + demLenh(9) + demLenh(106) + demLenh(-113);
    }

    /** Hiệu ứng trong các gói 51 kiểu 1 (byte 1, short id, byte chiêu, byte hiệu ứng, short). */
    static List<Integer> hieuUng() {
        List<Integer> r = new ArrayList<Integer>();
        synchronized (LENH) {
            for (int i = 0; i < LENH.size(); i++) {
                byte[] d = GOI.get(i);
                if (LENH.get(i) == 51 && d.length >= 5 && d[0] == 1) r.add((int) d[4]);
            }
        }
        return r;
    }

    /** Mọi gói 51 đều nhắm CHÍNH MÌNH. */
    static boolean choMinh() {
        short id = gs().mainChar.ID;
        synchronized (LENH) {
            for (int i = 0; i < LENH.size(); i++) {
                byte[] d = GOI.get(i);
                if (LENH.get(i) == 51 && d.length >= 3 && d[0] == 1 && (short) (((d[1] & 0xFF) << 8) | (d[2] & 0xFF)) != id) return false;
            }
        }
        return true;
    }

    static int dem(List<Integer> ds, int hu) {
        int n = 0;
        for (int x : ds) if (x == hu) n++;
        return n;
    }

    // ------------------------------------------------------------------ gói máy chủ

    static network.Message goiNhan(int lenh, byte[] data) throws Exception {
        Constructor<network.Message> c = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        c.setAccessible(true);
        return c.newInstance((byte) lenh, data);
    }

    /** Máy chủ: 51 kiểu 1 (đặt hiệu ứng) / kiểu 0 (xoá) cho chính mình. */
    static void mayChuBuff(int kieu, int hu, int giay) {
        try {
            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream o = new java.io.DataOutputStream(bo);
            o.writeByte(kieu);
            o.writeShort(gs().mainChar.ID);
            o.writeByte(hu);
            o.writeShort(giay);
            if (kieu == 1) o.writeByte(1);
            gs().onCharUseBuff(goiNhan(51, bo.toByteArray()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ cảnh

    static game.model.Monster quai;

    static void datTinh(Class<?> c, String ten, Object gt) throws Exception {
        Field f = c.getDeclaredField(ten);
        f.setAccessible(true);
        f.set(null, gt);
    }

    static int[] kq() {
        return gs().tuBuffKq;
    }

    static void tuDung() throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                Method m = game.render.screen.GameScr.class.getDeclaredMethod("tuDungBoTro", int.class, int.class, int[].class);
                m.setAccessible(true);
                m.invoke(gs(), 0, -1, game.pokemon.TuBuff.PHIM_TRANG);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        ngu(150);   // trả lời máy chủ giả (luồng game)
    }

    static void hetHoi() throws Exception {
        ChupTrongGame.chay(() -> {
            game.model.MainChar mc = gs().mainChar;
            for (int i = 0; i < mc.timeLastUseSkills.length; i++) mc.timeLastUseSkills[i] = 0;
        });
    }

    static void chuanBi() throws Exception {
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.render.screen.GameScr g = gs();
            game.model.MainChar mc = g.mainChar;
            mc.clazz = LOP;
            game.model.Char.skillLevelLearnt = new byte[]{1, 2, 1, 1, 1, 1, 1, 1, 0, 0, 0};
            short[][] tam = new short[1][12];
            short[][][] pp = new short[1][12][11];
            int[][][] hoi = new int[1][12][11];
            short[][][] st = new short[1][12][11];
            for (int k = 0; k < 12; k++) {
                tam[0][k] = 80;
                for (int l = 0; l < 11; l++) {
                    pp[0][k][l] = 5;
                    hoi[0][k][l] = 500;
                    st[0][k][l] = 100;
                }
            }
            game.model.skill.SkillManager.SKILL_RANGE = tam;
            game.model.skill.SkillManager.SKILL_MP = pp;
            game.model.skill.SkillManager.SKILL_COOLDOWN = hoi;
            game.model.skill.SkillManager.SKILL_DAM_PERCENT = st;
            game.model.skill.SkillManager.SKILL_AEO = new byte[5][0];
            if (game.model.MainChar.quickSlot == null) mc.loadQuickSlot();
            int[] chieu = {4, 1, 7, 6, 5};
            for (int i = 0; i < 5; i++) game.model.MainChar.quickSlot[0][i].setIsSkill(chieu[i], LOP, chieu[i] >= 4);
            // trang 2 (vòng Auto gốc: doFire(1/3/5, 1)): hồi sinh, chiêu đánh, bổ trợ
            int[] chieu2 = {6, 1, 4, 0, 0};
            for (int i = 0; i < 5; i++) game.model.MainChar.quickSlot[1][i].setIsSkill(chieu2[i], LOP, chieu2[i] >= 4);
            game.render.screen.GameScr.indexTabSlot = 0;
            quai = new game.model.Monster();
            quai.ID = 3001;
            quai.catagory = 1;
            quai.setType((short) 0);
            quai.level = 3;
        });
    }

    /** Cảnh sạch: không tự động, không hiệu ứng, đủ PP, hồi chiêu xong, không quái. */
    static void canh(final boolean coQuai) throws Exception {
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.render.Canvas.currentDialog = null;
            game.render.Canvas.menu.showMenu = false;
            game.pokemon.HopThoaiDuoi.tat();
            game.render.screen.GameScr g = gs();
            game.model.MainChar mc = g.mainChar;
            game.render.screen.GameScr.autoFight = false;
            game.render.screen.GameScr.saveAutoFight = false;
            game.render.screen.TuDong.dang = false;
            game.model.Tilemap.lv = 0;
            game.pokemon.NgamTay.mayChu = false;
            game.pokemon.NgamTay.pvp = false;
            game.pokemon.NgamTay.datThuCong(false);
            game.pokemon.NgamTay.huy();
            mc.state = 0;
            mc.isDie = false;
            mc.hp = Math.max(mc.hp, 100);
            mc.maxhp = Math.max(mc.maxhp, mc.hp);
            mc.maxmp = 100;
            mc.mp = 100;
            mc.posTransRoad = null;
            mc.resetBuff();
            g.indexBuff = -1;
            game.render.screen.GameScr.timeBuff = -1;
            for (int i = 0; i < mc.timeLastUseSkills.length; i++) {
                mc.timeLastUseSkills[i] = 0;
                mc.coolDownSkill[i] = 0;
            }
            for (int i = 0; i < g.tuBuffKq.length; i++) g.tuBuffKq[i] = -1;
            g.actors.removeElement(quai);
            if (coQuai) {
                quai.maxhp = 500000;
                quai.hp = 500000;
                quai.setRealHP(500000);
                quai.state = 0;
                quai.x = (short) (mc.x + 24);
                quai.y = mc.y;
                quai.xFirst = quai.x;
                quai.yFirst = quai.y;
                g.actors.addElement(quai);
            }
            g.focusedActor = null;
            game.pokemon.HanhDong.boChon();
            game.render.Canvas.clearKeyPressed();
            game.render.Canvas.clearKeyHold();
        });
    }

    // ------------------------------------------------------------------ các phần kiểm

    static void kiemTrucTiep() throws Exception {
        canh(false);
        traLoi = true;
        thoiLuong = 2;
        // 1) không hiệu ứng -> dùng hai chiêu bổ trợ cho mình, một lần mỗi chiêu
        xoaGoi();
        tuDung();
        List<Integer> h = hieuUng();
        kiem(dem(h, HU_4) == 1 && dem(h, HU_7) == 1 && h.size() == 2, "1) het hieu ung -> dung 4 (25) va 7 (23) mot lan: " + h);
        kiem(choMinh(), "1) goi 51 nham chinh minh");
        kiem(dem(h, HU_6) == 0 && kq()[6] == game.pokemon.TuBuff.KHONG_DUNG_DUOC, "1) khong hoi sinh (kq " + kq()[6] + ")");
        kiem(dem(h, HU_5) == 0 && kq()[5] == game.pokemon.TuBuff.KHONG_DUNG_DUOC, "1) khong chieu -1 (kq " + kq()[5] + ")");
        kiem(gs().mainChar.conLaiBuffMs(HU_4, System.currentTimeMillis()) > 0, "1) may chu tra loi -> hieu ung 25 co tren nguoi");
        // 2) còn hiệu lực (hồi chiêu đã xong) -> không dùng lại
        hetHoi();
        xoaGoi();
        tuDung();
        h = hieuUng();
        kiem(h.isEmpty(), "2) con hieu luc -> khong dung lai: " + h);
        kiem(kq()[4] == game.pokemon.TuBuff.CON_HIEU_LUC && kq()[7] == game.pokemon.TuBuff.CON_HIEU_LUC,
                "2) kq CON_HIEU_LUC (" + kq()[4] + "," + kq()[7] + ")");
        // 3) hết hiệu lực -> dùng lại; ngay sau đó (hồi chiêu, chưa có hiệu ứng) -> không
        ngu(2300);
        xoaGoi();
        traLoi = false;
        tuDung();
        h = hieuUng();
        kiem(dem(h, HU_4) == 1 && dem(h, HU_7) == 1, "3) het hieu luc -> dung lai: " + h);
        xoaGoi();
        tuDung();
        h = hieuUng();
        kiem(h.isEmpty() && kq()[4] == game.pokemon.TuBuff.DANG_HOI, "3) vua dung (hoi chieu) -> khong gui lai: " + h + " kq " + kq()[4]);
        // 4) thiếu PP -> không
        hetHoi();
        ChupTrongGame.chay(() -> gs().mainChar.mp = 3);
        xoaGoi();
        tuDung();
        h = hieuUng();
        kiem(h.isEmpty() && kq()[4] == game.pokemon.TuBuff.THIEU_PP && kq()[7] == game.pokemon.TuBuff.THIEU_PP,
                "4) thieu PP -> khong dung: " + h + " kq " + kq()[4] + "," + kq()[7]);
        // 5) mỗi chiêu theo hạn riêng: 25 hết (1 giây), 23 còn (30 giây)
        canh(false);
        ChupTrongGame.chay(() -> {
            mayChuBuff(1, HU_4, 1);
            mayChuBuff(1, HU_7, 30);
        });
        ngu(1300);
        hetHoi();
        xoaGoi();
        tuDung();
        h = hieuUng();
        kiem(dem(h, HU_4) == 1 && dem(h, HU_7) == 0, "5) han rieng: chi 25 (het) duoc dung lai, 23 (con) khong: " + h);
        // máy chủ xoá hiệu ứng (51 kiểu 0) -> dùng lại
        ChupTrongGame.chay(() -> mayChuBuff(0, HU_7, 0));
        hetHoi();
        xoaGoi();
        tuDung();
        h = hieuUng();
        kiem(dem(h, HU_7) == 1, "5) may chu xoa hieu ung 23 -> dung lai: " + h);
    }

    /** 6) vòng Auto gốc: đòn đánh vẫn chạy khi có hiệu ứng bổ trợ + ô hồi sinh trang 2. */
    static void kiemAutoGoc() throws Exception {
        canh(true);
        traLoi = true;
        thoiLuong = 30;
        ChupTrongGame.chay(() -> {
            mayChuBuff(1, HU_4, 30);
            mayChuBuff(1, HU_7, 30);
            gs().indexBuff = 4;   // như bản cũ: biểu tượng + timeBuff (ms) -> isBeginAutoBuff chặn mọi đòn
            game.model.Tilemap.lv = 1;
            game.render.Canvas.autoTab.isAutoFire = true;
            game.render.screen.BangAuto.tgDelay = 0;
            gs().focusedActor = quai;
            game.render.screen.GameScr.autoFight = true;
        });
        xoaGoi();
        ngu(3000);
        ChupTrongGame.chay(() -> {
            game.render.screen.GameScr.autoFight = false;
            game.model.Tilemap.lv = 0;
        });
        List<Integer> h = hieuUng();
        kiem(demDanh() > 0, "6) Auto goc: co hieu ung bo tro + o hoi sinh -> van danh (goi danh " + demDanh() + ", goi " + LENH + ")");
        kiem(h.isEmpty(), "6) Auto goc: hieu ung con -> khong gui 51: " + h);
        kiem(game.render.Canvas.currentDialog == null, "6) Auto goc: khong hop thoai hoi sinh");
        // hết hiệu ứng -> Auto gốc dùng lại (trang đang dùng + trang 2), vẫn đánh
        canh(true);
        ChupTrongGame.chay(() -> {
            game.model.Tilemap.lv = 1;
            game.render.Canvas.autoTab.isAutoFire = true;
            game.render.screen.BangAuto.tgDelay = 0;
            gs().focusedActor = quai;
            game.render.screen.GameScr.autoFight = true;
        });
        xoaGoi();
        ngu(3000);
        ChupTrongGame.chay(() -> {
            game.render.screen.GameScr.autoFight = false;
            game.model.Tilemap.lv = 0;
        });
        h = hieuUng();
        kiem(dem(h, HU_4) == 1 && dem(h, HU_7) == 1 && dem(h, HU_6) == 0, "6) Auto goc: het hieu ung -> dung moi chieu dung 1 lan: " + h);
        kiem(choMinh(), "6) Auto goc: 51 nham chinh minh");
        kiem(demDanh() > 0, "6) Auto goc: van danh sau khi dung bo tro (goi " + LENH + ")");
    }

    /** 7) Tự động đánh chính thức (TuDong, client tự chọn quái). */
    static void kiemTuDong() throws Exception {
        canh(true);
        traLoi = true;
        thoiLuong = 30;
        ChupTrongGame.chay(() -> {
            try {
                Class<?> td = game.render.screen.TuDong.class;
                datTinh(td, "coServer", true);
                datTinh(td, "svCo", 1 | 2);   // tính năng + thuốc, client tự chọn (không kế hoạch máy chủ)
                datTinh(td, "neoMap", gs().map);
                datTinh(td, "neoX", (int) gs().mainChar.x);
                datTinh(td, "neoY", (int) gs().mainChar.y);
                datTinh(td, "lucNhip", System.currentTimeMillis() + 600000L);
                datTinh(td, "trangChieu", -1);
                game.render.screen.TuDong.dang = true;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        xoaGoi();
        ngu(3500);
        ChupTrongGame.chay(() -> game.render.screen.TuDong.dang = false);
        List<Integer> h = hieuUng();
        kiem(dem(h, HU_4) == 1 && dem(h, HU_7) == 1, "7) TuDong: dung moi chieu bo tro dung 1 lan (con hieu luc thi khong lai): " + h);
        kiem(dem(h, HU_6) == 0 && dem(h, HU_5) == 0, "7) TuDong: khong hoi sinh / chieu -1: " + h);
        kiem(choMinh(), "7) TuDong: 51 nham chinh minh");
        kiem(demDanh() > 0, "7) TuDong: van danh quai (goi " + LENH + ")");
        kiem(gs().focusedActor == null || gs().focusedActor.catagory == 1, "7) TuDong: muc tieu la quai");
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "kiem_tu_buff");
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
                final byte[] d = m.getData();
                synchronized (LENH) {
                    LENH.add((int) m.command);
                    GOI.add(d);
                }
                if (m.command == 51 && d.length >= 5 && d[0] == 1) {
                    final int hu = d[4];
                    synchronized (MOI_HIEU_UNG) {
                        MOI_HIEU_UNG.add(hu);
                    }
                    if (traLoi && hu != HU_6) {
                        final int giay = thoiLuong;
                        NenPc.luong().them(new Runnable() {
                            public void run() {
                                mayChuBuff(1, hu, giay);
                            }
                        });
                    }
                }
            }

            public void close() {
            }
        });
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ngu(500);
        chuanBi();
        // logic thuần
        kiem(game.pokemon.TuBuff.xet(2, 6, 0, 5, 0, 99999, 0, 100, 1) == game.pokemon.TuBuff.KHONG_DUNG_DUOC, "TuBuff: hoi sinh khong bao gio");
        kiem(game.pokemon.TuBuff.xet(2, 4, 0, 1, 0, 99999, 0, 100, 5) == game.pokemon.TuBuff.DUNG, "TuBuff: du dieu kien -> dung");
        kiem(game.pokemon.TuBuff.xet(2, 4, 0, 1, 1, 99999, 0, 100, 5) == game.pokemon.TuBuff.CON_HIEU_LUC, "TuBuff: con hieu luc");
        kiem(game.pokemon.TuBuff.xet(2, 4, 0, 0, 0, 99999, 0, 100, 5) == game.pokemon.TuBuff.CHUA_HOC, "TuBuff: chua hoc");
        kiem(game.pokemon.TuBuff.xet(2, 4, 0, 1, 0, 100, 500, 100, 5) == game.pokemon.TuBuff.DANG_HOI, "TuBuff: dang hoi");
        kiem(game.pokemon.TuBuff.xet(2, 4, 0, 1, 0, 99999, 0, 4, 5) == game.pokemon.TuBuff.THIEU_PP, "TuBuff: thieu PP");
        kiem(game.pokemon.TuBuff.xet(0, 4, -1, 1, 0, 99999, 0, 100, 5) == game.pokemon.TuBuff.KHONG_DUNG_DUOC, "TuBuff: chieu -1 khong");
        kiemTrucTiep();
        kiemAutoGoc();
        kiemTuDong();
        synchronized (MOI_HIEU_UNG) {
            kiem(!MOI_HIEU_UNG.contains(HU_6), "ca phien: khong bao gio tu dung hoi sinh (" + MOI_HIEU_UNG + ")");
        }
        canh(false);
        System.out.println("KiemTuBuff: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
