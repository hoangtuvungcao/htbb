import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM HỒI QUY "TREO MÀN KHI NHẬN NHIỆM VỤ" (0.9.4) - headless, không cần máy chủ: client PC thật (Canvas / GameScr /
 * vòng game / vẽ), gói tin nhiệm vụ đi qua MessageHandler như từ server, gói client gửi đi ghi lại bằng phiên giả.
 *
 * Mỗi kiểu nhập (cham = chạm APK, chuot = chuột PC, phim = phím; chạy thêm một lần với -Dhtbb.khongCham=true cho
 * máy J2ME chỉ có phím: menu chữ + hộp thoại cũ):
 *   1. nhiệm vụ nói chuyện 2 lời (như "Thức giấc ở Pallet"): mở từ menu NPC, nghe hết -> client gửi -64 trả, hết
 *      hội thoại, chạm / phím sau đó tới được màn chơi (nhân vật đi được);
 *   2. nhiệm vụ đánh quái 1 lời: hỏi nhận -> Đồng ý -> gửi -64 nhận;
 *   3. nhiệm vụ 3 lời: Không -> không gửi gì, đóng;
 *   4. trả nhiệm vụ (lời trả 2 dòng) -> gửi -64 trả;
 *   5. Back / Esc giữa hội thoại -> đóng;
 *   6. thông báo server (lệnh 37) -> đóng bằng Esc / chạm ngoài;
 *   7. hộp xác nhận: chạm ngoài = Huỷ;
 *   8. giám sát: hội thoại mồ côi, hộp thoại vẽ lỗi, nhịp lỗi liên tục -> tự gỡ trong ~2 giây;
 *   9. gói tin lỗi không giết luồng / không treo.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/KiemNhiemVu.java \
 *     && java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemNhiemVu
 *     (+ lần nữa với -Dhtbb.khongCham=true)
 * Mã thoát 1 = có lỗi.
 */
public class KiemNhiemVu {
    static int dat, hong;
    static String KIEU = "cham";
    static final List<byte[]> GUI = new ArrayList<byte[]>();
    static final List<Integer> GUI_LENH = new ArrayList<Integer>();

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG [" + KIEU + "]: " + ten);
        }
    }

    static void chay(final Runnable r) throws Exception {
        final Object k = new Object();
        final boolean[] xong = {false};
        NenPc.luong().them(new Runnable() {
            public void run() {
                try {
                    r.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    synchronized (k) {
                        xong[0] = true;
                        k.notifyAll();
                    }
                }
            }
        });
        synchronized (k) {
            long het = System.currentTimeMillis() + 5000;
            while (!xong[0] && System.currentTimeMillis() < het) k.wait(100);
        }
    }

    static final java.awt.image.BufferedImage DEM = new java.awt.image.BufferedImage(1280, 720, java.awt.image.BufferedImage.TYPE_INT_RGB);

    /** Vẽ một khung như màn hình thật (VungCham đăng ký vùng chạm lúc vẽ). */
    static void ve() throws Exception {
        chay(new Runnable() {
            public void run() {
                java.awt.Graphics2D g2 = DEM.createGraphics();
                g2.scale(NenPc.heSo, NenPc.heSo);
                g2.clipRect(0, 0, NenPc.rong, NenPc.cao);
                Graphics g = new Graphics(g2, NenPc.rong, NenPc.cao, NenPc.heSo);
                try {
                    Method m = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("paint", Graphics.class);
                    m.setAccessible(true);
                    m.invoke(game.render.Canvas.instance, g);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                g2.dispose();
            }
        });
    }

    static void ngu(long ms) throws Exception {
        Thread.sleep(ms);
    }

    static void goi(String ten, int x, int y) {
        try {
            Method m = game.render.Canvas.class.getDeclaredMethod(ten, int.class, int.class);
            m.setAccessible(true);
            m.invoke(game.render.Canvas.instance, x, y);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static void cham(final int x, final int y) throws Exception {
        if (KIEU.equals("chuot")) {
            int rx = NenPc.lechX + x * NenPc.heSo + 1, ry = NenPc.lechY + y * NenPc.heSo + 1;
            NenPc.cham(0, rx, ry);
            ngu(90);
            NenPc.cham(2, rx, ry);
            ngu(180);
            return;
        }
        chay(new Runnable() {
            public void run() {
                goi("pointerPressed", x, y);
            }
        });
        ngu(90);
        chay(new Runnable() {
            public void run() {
                goi("pointerReleased", x, y);
            }
        });
        ngu(180);
    }

    static void phim(int ma) throws Exception {
        NenPc.phim(0, ma);
        ngu(90);
        NenPc.phim(2, ma);
        ngu(180);
    }

    /** Chạm vào vùng VungCham tên ten (true = có). */
    static boolean chamVung(String ten) throws Exception {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) {
                int[] v = game.pokemon.VungCham.vungVe(i);
                cham(v[0] + v[2] / 2, v[1] + v[3] / 2);
                return true;
            }
        }
        return false;
    }

    /** "Tiếp lời" theo kiểu nhập: chạm giữa màn / phím chọn. */
    static void tiep() throws Exception {
        if (KIEU.equals("phim")) phim(-5);
        else cham(game.render.Canvas.w / 2, game.render.Canvas.h / 3);
    }

    /** Đồng ý trên hộp xác nhận hiện có (HopXacNhan hoặc MsgDlg Có / Không). */
    static boolean dongY(boolean co) throws Exception {
        Object d = game.render.Canvas.currentDialog;
        if (d instanceof game.pokemon.HopXacNhan) {
            if (KIEU.equals("phim")) phim(co ? -5 : -7);
            else chamVung(co ? "xn-dong-y" : "xn-huy");
            return true;
        }
        if (d instanceof game.render.screen.MsgDlg) {
            phim(co ? -6 : -7);   // phím mềm trái "Có" / phải "Không"
            return true;
        }
        if (d instanceof game.pokemon.HopThongBao) {
            // 0.9.5: hộp thông báo chung (MsgDlg trên máy cảm ứng / chuột): nút cuối = Có, nút đầu = Không
            if (KIEU.equals("phim")) phim(co ? -5 : -7);
            else chamVung(co ? "hop-nut-1" : "hop-nut-0");
            return true;
        }
        return false;
    }

    /** Có hộp hỏi đang mở (HopXacNhan / MsgDlg / hộp thông báo chung). */
    static boolean coHopHoi() {
        Object d = game.render.Canvas.currentDialog;
        return d instanceof game.pokemon.HopXacNhan || d instanceof game.render.screen.MsgDlg || d instanceof game.pokemon.HopThongBao;
    }

    /** Thông báo server 37 trong game: máy chạm / chuột = biểu ngữ không chặn (0.9.5); J2ME phím = hộp MsgDlg. */
    static boolean coThongBao37() {
        if (game.pokemon.MenuNguCanh.bat()) {
            return game.render.Canvas.currentDialog == null && game.pokemon.DungDo.dangHien();
        }
        // J2ME chi phim: tin ngan trong game la the nhanh khong chan (game.pokemon.ThongBao, 0.9.6.1), tin nghiem trong /
        // dai van la hop MsgDlg
        return game.render.Canvas.currentDialog instanceof game.render.screen.MsgDlg
                || game.render.Canvas.currentDialog == null && game.pokemon.DungDo.dangHien();
    }

    static boolean coLenhGui(int lenh, int byteDau) {
        for (int i = 0; i < GUI.size(); i++) {
            byte[] b = GUI.get(i);
            if (GUI_LENH.get(i) == lenh && (byteDau < -999 || b.length > 0 && b[0] == byteDau)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ gói tin giả từ server

    static network.Message tao(int lenh, byte[] d) throws Exception {
        Constructor<network.Message> c = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        c.setAccessible(true);
        return c.newInstance((byte) lenh, d);
    }

    static void nhan(int lenh, byte[] d) throws Exception {
        final network.Message m = tao(lenh, d);
        // luồng mạng thật (không phải luồng game / vẽ)
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    network.Session_ME.gI().messageHandler.onMessage(m);
                } catch (Throwable e) {
                    game.pokemon.GiamSat.loi("kiem goi " + m.command, e);
                }
            }
        });
        t.start();
        t.join(3000);
    }

    /** -64 kiểu 0: nhiệm vụ nhận được. */
    static byte[] goiNhan(int id, int mainSub, String ten, int npc, String noiDung, int loai) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeByte(0);
        o.writeByte(1);
        o.writeShort(id);
        o.writeByte(mainSub);
        o.writeUTF(ten);
        o.writeByte(npc);
        o.writeUTF(noiDung);
        o.writeByte(loai);
        o.writeByte(0);
        return b.toByteArray();
    }

    /** -64 kiểu 1: nhiệm vụ đã xong chờ trả. */
    static byte[] goiTra(int id, int mainSub, String ten, int npc, String loiTra) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeByte(1);
        o.writeByte(1);
        o.writeShort(id);
        o.writeByte(mainSub);
        o.writeUTF(ten);
        o.writeByte(npc);
        o.writeUTF(loiTra);
        o.writeUTF("Đã xong");
        o.writeUTF("100 EXP");
        return b.toByteArray();
    }

    static byte[] goi37(String s) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeUTF(s);
        o.writeUTF("");
        return b.toByteArray();
    }

    // ------------------------------------------------------------------ dựng màn

    static game.model.NPC npc;

    static void giaLap() {
        game.render.Canvas.isLoad = false;
        game.render.Canvas.endDlg();
        try {
            game.model.Tilemap.loadMap(0, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        gs.mainChar.name = "kiemnv";
        gs.mainChar.level = 3;
        gs.mainChar.hp = gs.mainChar.maxhp = 500;
        gs.mainChar.mp = gs.mainChar.maxmp = 100;
        gs.mainChar.x = 200;
        gs.mainChar.y = 200;
        npc = new game.model.NPC();
        npc.type = 5;
        npc.name = "Trưởng làng";
        npc.catagory = 2;
        npc.x = 232;
        npc.y = 184;
        gs.focusedActor = npc;
        game.render.screen.GameScr.allQuestCanReceive.removeAllElements();
        game.render.screen.GameScr.mainQuest = null;
        game.render.screen.GameScr.subQuest = null;
        game.render.screen.GameScr.currQuest = null;
        gs.switchToMe();
    }

    /** Mở menu NPC như chạm / Enter vào NPC (menu chữ -> menu nút to ở máy có con trỏ) và chọn mục đầu. */
    static void moNpcChonDau() throws Exception {
        chay(new Runnable() {
            public void run() {
                game.render.Canvas.gameScr.focusedActor = npc;
                Vector v = game.render.screen.GameScr.getAllQuestNpc(npc.type);
                game.render.Canvas.menu.startAt(v, 3);
            }
        });
        ngu(300);
        ve();
        ngu(150);
        if (game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh && !KIEU.equals("phim")) {
            kiem(chamVung("lenh-0"), "menu NPC co nut lenh-0");
        } else {
            phim(-5);
        }
        ngu(300);
    }

    /** Thế giới nhận chạm / phím: nhân vật đi được. */
    static boolean diDuoc(String nhan) throws Exception {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        final int x0 = gs.mainChar.x, y0 = gs.mainChar.y;
        boolean di = false;
        int[][] h = {{90, 0}, {-90, 0}, {0, 60}, {0, -60}};
        int[] ph = {-4, -3, -2, -1};
        for (int k = 0; k < h.length && !di; k++) {
            if (KIEU.equals("phim")) {
                NenPc.phim(0, ph[k]);
                ngu(350);
                // bản đồ giả lập không có ô đi được chắc chắn: màn chơi nhận phím = nhân vật chuyển sang chạy (state 1)
                boolean chay = gs.mainChar.state == 1;
                ngu(350);
                if (chay) di = true;
                NenPc.phim(2, ph[k]);
                ngu(200);
            } else {
                float z = game.render.screen.GameScr.gsw > 0 ? game.render.Canvas.w / (float) game.render.screen.GameScr.gsw : 1f;
                int sx = (int) ((gs.mainChar.x - game.render.screen.GameScr.cmx) * z), sy = (int) ((gs.mainChar.y - game.render.screen.GameScr.cmy) * z);
                cham(Math.max(2, Math.min(game.render.Canvas.w - 3, sx + h[k][0])), Math.max(40, Math.min(game.render.Canvas.h - 60, sy + h[k][1])));
                ngu(900);
            }
            di = di || gs.mainChar.x != x0 || gs.mainChar.y != y0 || gs.mainChar.posTransRoad != null;
        }
        kiem(di, nhan + ": nhan vat di duoc sau do (" + x0 + "," + y0 + " -> " + gs.mainChar.x + "," + gs.mainChar.y + ") " + trangThai());
        // trả về chỗ cũ cho kịch bản sau
        chay(new Runnable() {
            public void run() {
                game.render.screen.GameScr g = game.render.Canvas.gameScr;
                g.mainChar.posTransRoad = null;
                g.mainChar.state = (byte) 0;
                g.mainChar.x = (short) 200;
                g.mainChar.y = (short) 200;
                g.focusedActor = npc;
            }
        });
        ngu(200);
        return di;
    }

    static String trangThai() {
        game.model.Quest q = game.render.screen.GameScr.currQuest;
        return "[hop=" + ten(game.render.Canvas.currentDialog) + " menu=" + game.render.Canvas.menu.showMenu
                + " quest=" + (q == null ? "-" : q.name + "/" + q.isShow) + " htd=" + game.pokemon.HopThoaiDuoi.dangBat() + "]";
    }

    static String ten(Object o) {
        return o == null ? "-" : o.getClass().getSimpleName();
    }

    static void sachGui() {
        synchronized (GUI) {
            GUI.clear();
            GUI_LENH.clear();
        }
    }

    // ------------------------------------------------------------------ kịch bản

    static void kichBan() throws Exception {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        diDuoc("0 (truoc nhiem vu)");
        // 1. nhiệm vụ nói chuyện 2 lời
        sachGui();
        nhan(-64, goiNhan(0, 0, "Thức giấc ở Pallet", 5, "1Xatu: ...Cháu tỉnh rồi. Đây là Pallet.>1Xatu: Ta là Xatu, trưởng làng.", 3));
        kiem(game.render.screen.GameScr.allQuestCanReceive.size() == 1, "1: nhan goi -64 (co the nhan = " + game.render.screen.GameScr.allQuestCanReceive.size() + ")");
        moNpcChonDau();
        game.model.Quest q = game.render.screen.GameScr.currQuest;
        kiem(q != null && q.isShow, "1: mo hoi thoai nhiem vu " + trangThai());
        for (int i = 0; i < 8 && q != null && q.isShow; i++) {
            tiep();
            ngu(250);
        }
        kiem(q != null && !q.isShow, "1: nghe het hoi thoai (khong ket) " + trangThai());
        kiem(coLenhGui(-64, 1), "1: gui -64 tra nhiem vu noi chuyen");
        kiem(game.render.Canvas.currentDialog == null, "1: khong con hop thoai " + trangThai());
        diDuoc("1");

        // 2. đánh quái 1 lời: hỏi nhận
        sachGui();
        nhan(-64, goiNhan(2, 0, "Bài học tấn công", 5, "1Xatu: Hãy hạ 3 Rattata ngoài làng.", 0));
        moNpcChonDau();
        q = game.render.screen.GameScr.currQuest;
        for (int i = 0; i < 6 && !coHopHoi(); i++) {
            tiep();
            ngu(250);
        }
        ve();
        ngu(100);
        kiem(dongY(true), "2: co hop hoi nhan nhiem vu 1 loi " + trangThai());
        ngu(400);
        kiem(coLenhGui(-64, 0), "2: gui -64 nhan nhiem vu");
        kiem(q != null && !q.isShow && game.render.Canvas.currentDialog == null, "2: dong sau khi nhan " + trangThai());
        diDuoc("2");

        // 3. 3 lời, từ chối
        sachGui();
        nhan(-64, goiNhan(3, 0, "Ngắm sấm từ xa", 5, "1Xatu: Trời sắp mưa.>0Vâng.>1Xatu: Con có muốn đi ngắm sấm không?", 0));
        moNpcChonDau();
        q = game.render.screen.GameScr.currQuest;
        for (int i = 0; i < 6 && !coHopHoi(); i++) {
            tiep();
            ngu(250);
        }
        ve();
        ngu(100);
        kiem(dongY(false), "3: co hop hoi nhan (3 loi) " + trangThai());
        ngu(400);
        kiem(!coLenhGui(-64, 0), "3: tu choi khong gui nhan");
        kiem(q != null && !q.isShow && game.render.Canvas.currentDialog == null, "3: dong sau khi tu choi " + trangThai());
        diDuoc("3");

        // 4. trả nhiệm vụ
        sachGui();
        nhan(-64, goiTra(2, 0, "Bài học tấn công", 5, "1Xatu: Giỏi lắm!>0Cảm ơn ông."));
        moNpcChonDau();
        q = game.render.screen.GameScr.currQuest;
        for (int i = 0; i < 6 && q != null && q.isShow; i++) {
            tiep();
            ngu(250);
        }
        kiem(coLenhGui(-64, 1), "4: gui -64 tra nhiem vu " + trangThai());
        kiem(q != null && !q.isShow, "4: dong sau khi tra " + trangThai());
        diDuoc("4");

        // 5. Back / Esc giữa hội thoại
        nhan(-64, goiNhan(5, 1, "Lá thư không tên", 5, "1Xatu: Có một lá thư.>1Xatu: Không ghi tên ai.>0Lạ thật.>1Xatu: Cháu giúp ta nhé?", 2));
        moNpcChonDau();
        q = game.render.screen.GameScr.currQuest;
        kiem(q != null && q.isShow, "5: mo hoi thoai " + trangThai());
        phim(-7);
        ngu(300);
        kiem(q != null && !q.isShow, "5: Back / Esc dong hoi thoai nhiem vu " + trangThai());
        diDuoc("5");

        // 6. thông báo server (37): Esc / chạm ngoài đóng
        nhan(37, goi37("Đã nhận nhiệm vụ Bài học tấn công"));
        ngu(200);
        kiem(coThongBao37(), "6: hien thong bao 37 " + trangThai());
        phim(-7);
        ngu(300);
        kiem(game.render.Canvas.currentDialog == null, "6: Back / Esc dong thong bao " + trangThai());
        nhan(37, goi37("Tài khoản chưa kích hoạt: vẫn chơi bình thường."));
        ngu(200);
        ve();
        if (game.pokemon.MenuNguCanh.bat()) {
            // 0.9.5: biểu ngữ không chặn - chạm vào biểu ngữ = tắt (chạm ra ngoài đi xuống màn chơi như thường)
            ve();
            kiem(chamVung("thong-bao-0") || chamVung("thong-bao-1"), "6: cham bieu ngu " + trangThai());
        } else {
            cham(4, 40);   // chạm góc trên trái - ngoài hộp
        }
        ngu(300);
        kiem(game.render.Canvas.currentDialog == null, "6: cham ngoai dong thong bao " + trangThai());

        // 7. hộp xác nhận: chạm ngoài = Huỷ (máy có con trỏ)
        if (game.pokemon.MenuNguCanh.bat()) {
            final boolean[] khong = {false};
            chay(new Runnable() {
                public void run() {
                    game.render.Canvas.startYesNoDlg("Bán vật phẩm?", null, new game.model.IAction() {
                        public void perform() {
                            khong[0] = true;
                        }
                    });
                }
            });
            ve();
            ngu(150);
            cham(4, 40);
            ngu(300);
            kiem(game.render.Canvas.currentDialog == null && khong[0], "7: cham ngoai hop xac nhan = Huy " + trangThai());
        }

        // 8. giám sát
        chay(new Runnable() {
            public void run() {
                game.model.Quest m = new game.model.Quest(99);
                m.name = "mo coi";
                m.isShow = true;
                game.render.screen.GameScr.currQuest = m;
                game.pokemon.HopThoaiDuoi.tat();
            }
        });
        int go0 = game.pokemon.GiamSat.soLanGo;
        ngu(2800);
        kiem(!game.render.screen.GameScr.currQuest.isShow && game.pokemon.GiamSat.soLanGo > go0, "8: giam sat dong hoi thoai nhiem vu mo coi " + game.pokemon.GiamSat.nhatKyCuoi);
        go0 = game.pokemon.GiamSat.soLanGo;
        chay(new Runnable() {
            public void run() {
                game.render.Canvas.currentDialog = new game.render.screen.Dialog() {
                    public void paint(Graphics g) {
                        throw new IllegalStateException("hop thoai hong (kiem)");
                    }
                };
            }
        });
        ngu(2800);
        kiem(game.render.Canvas.currentDialog == null && game.pokemon.GiamSat.soLanGo > go0, "8: giam sat go hop thoai ve loi " + game.pokemon.GiamSat.nhatKyCuoi);
        go0 = game.pokemon.GiamSat.soLanGo;
        chay(new Runnable() {
            public void run() {
                game.render.Canvas.currentDialog = new game.render.screen.Dialog() {
                    public void update() {
                        throw new Error("update hong (kiem)");
                    }
                };
            }
        });
        int tick0 = game.render.Canvas.gameTick;
        ngu(2800);
        kiem(game.render.Canvas.currentDialog == null && game.pokemon.GiamSat.soLanGo > go0, "8: nhip loi lien tuc -> go hop thoai " + game.pokemon.GiamSat.nhatKyCuoi);
        kiem(game.render.Canvas.gameTick != tick0, "8: vong game van chay sau Error");
        diDuoc("8");

        // 9. gói lỗi (cụt) không treo, gói sau vẫn xử lý
        nhan(-64, new byte[]{0, 3, 0});
        nhan(37, new byte[]{0});
        nhan(-30, new byte[]{0, 1});
        nhan(37, goi37("Sau goi loi"));
        ngu(200);
        kiem(coThongBao37(), "9: goi sau goi loi van xu ly " + trangThai());
        phim(-7);
        ngu(300);
        diDuoc("9");
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : System.getProperty("java.io.tmpdir") + "/kiem_nhiem_vu");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        chay(new Runnable() {
            public void run() {
                try {
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) ngu(100);
        ngu(2500);
        // phiên giả: ghi lại gói client gửi, luôn "đang nối"
        game.networklogic.GameService.gI().setSession(new network.ISession() {
            public boolean isConnected() {
                return true;
            }

            public void setHandler(network.IMessageHandler h) {
            }

            public void connect(String s, String s2) {
            }

            public void sendMessage(network.Message m) {
                synchronized (GUI) {
                    GUI_LENH.add((int) m.command);
                    GUI.add(m.getData());
                }
            }

            public void close() {
            }
        });
        // vẽ liên tục như màn hình thật
        Thread v = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        ve();
                        Thread.sleep(40);
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        });
        v.setDaemon(true);
        v.start();
        String[] kieu = Boolean.getBoolean("htbb.khongCham") ? new String[]{"phim"} : new String[]{"cham", "chuot", "phim"};
        if (System.getProperty("kieu") != null) kieu = System.getProperty("kieu").split(",");
        for (int i = 0; i < kieu.length; i++) {
            KIEU = kieu[i];
            chay(new Runnable() {
                public void run() {
                    giaLap();
                }
            });
            ngu(800);
            int h0 = hong;
            kichBan();
            System.out.println("kieu " + KIEU + (Boolean.getBoolean("htbb.khongCham") ? " (J2ME chi phim)" : "") + ": " + (hong == h0 ? "DAT" : "HONG " + (hong - h0)));
        }
        System.out.println("KiemNhiemVu: " + dat + " dat / " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
