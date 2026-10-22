import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM NGẮM TAY 0.9.8 (game.pokemon.NgamTay) - client thật headless, phiên giả ghi gói gửi đi, ở 11 cỡ chuẩn
 * (ChupTrongGame.CO), dùng lại màn chơi giả của KiemChamHud:
 *  1) máy chủ báo PvP (-113 loại 1): vào chế độ ngắm, tắt tự động phía client; loại 2: bảng chiêu đọc đúng;
 *  2) CẢM ỨNG: nhấn nút chiêu, kéo sang trái -> đang ngắm, hướng ~(-1000, 0), vạch ngắm VẼ RA (đếm điểm ảnh màu vạch
 *     quanh Pokémon, so khung không ngắm); thả -> đúng MỘT gói -113 (chiêu đúng ô, hướng đúng), KHÔNG gói 6 / 9;
 *  3) HUỶ: kéo ra rồi kéo về nút -> vùng huỷ (vạch đỏ), thả -> không gói nào;
 *  4) CHẠM NHANH (không kéo) -> bắn theo hướng đang quay;
 *  5) PC: giữ phím chiêu + con trỏ phía trên Pokémon -> thả phím = bắn lên; chuột phải = huỷ;
 *  6) PHÍM J2ME (không chuột): bấm phím chiêu = bắn theo hướng quay;
 *  7) KHÔNG KHOÁ MỤC TIÊU: đang PvP, nhắm người chơi khác, doUseSkillToFocusActor không gửi gói 6;
 *  8) PvE không đổi: hết PvP, chạm chiêu khi nhắm quái = gói 9 như cũ; bật "Ngắm thủ công" = gói -113.
 *
 *   sh client_pc/build.sh && O=$(mktemp -d)
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemChamHud.java \
 *     client_pc/test/KiemNgamTay.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemNgamTay <thư mục>
 * Mã thoát 1 = có lỗi.
 */
public class KiemNgamTay {
    static int dat, hong;
    static String co = "";

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG [" + co + "]: " + ten);
        }
    }

    static void ngu(long ms) throws Exception {
        Thread.sleep(ms);
    }

    static network.Message goiVao(byte[] d) throws Exception {
        Constructor<network.Message> k = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        k.setAccessible(true);
        return k.newInstance((byte) -113, d);
    }

    /** Gói -113 đầu tiên đã gửi: {chiêu, hx, hy}; null = không có. */
    static int[] goiBan() {
        synchronized (KiemChamHud.LENH) {
            for (int i = 0; i < KiemChamHud.LENH.size(); i++) {
                if (KiemChamHud.LENH.get(i) == -113) {
                    byte[] d = KiemChamHud.GOI.get(i);
                    if (d.length >= 10 && d[0] == 0) {
                        return new int[]{d[1], (short) (((d[6] & 0xFF) << 8) | (d[7] & 0xFF)), (short) (((d[8] & 0xFF) << 8) | (d[9] & 0xFF))};
                    }
                }
            }
        }
        return null;
    }

    static int demBan() {
        int n = 0;
        synchronized (KiemChamHud.LENH) {
            for (int i = 0; i < KiemChamHud.LENH.size(); i++) {
                byte[] d = KiemChamHud.GOI.get(i);
                if (KiemChamHud.LENH.get(i) == -113 && d.length >= 1 && d[0] == 0) n++;
            }
        }
        return n;
    }

    /** Tâm nút chiêu của phím {@code phim} trên màn đang chạy (quét lưới, GameScr.oChieuTai). */
    static int[] nut(final int phim) throws Exception {
        final int[][] ra = {null};
        ChupTrongGame.chay(() -> {
            game.render.screen.GameScr gs = game.render.Canvas.gameScr;
            for (int y = game.render.Canvas.h - 1; y >= 0 && ra[0] == null; y -= 2) {
                for (int x = game.render.Canvas.w - 1; x >= 0; x -= 2) {
                    int[] o = gs.oChieuTai(x, y);
                    if (o != null && o[0] == phim) {
                        ra[0] = o;
                        break;
                    }
                }
            }
        });
        return ra[0];
    }

    static void datLaiHoiChieu() throws Exception {
        ChupTrongGame.chay(() -> {
            game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
            for (int i = 0; i < mc.timeLastUseSkills.length; i++) {
                mc.timeLastUseSkills[i] = 0;
                mc.coolDownSkill[i] = 0;
            }
            mc.mp = mc.maxmp > 0 ? mc.maxmp : 500;
            mc.state = 0;
            mc.dir = 3;   // quay phải
        });
    }

    static void pvp(boolean b) throws Exception {
        final byte[] d = {1, (byte) (b ? 1 : 0), 0, 0};
        ChupTrongGame.chay(() -> {
            try {
                game.pokemon.NgamTay.nhan(goiVao(d));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    /** Đếm điểm ảnh gần màu {@code rgb} (khoảng cách màu <= 60) trong hình chữ nhật. */
    static int demMau(BufferedImage im, int rgb, int x0, int y0, int x1, int y1) {
        int r0 = (rgb >> 16) & 255, g0 = (rgb >> 8) & 255, b0 = rgb & 255, n = 0;
        for (int y = Math.max(0, y0); y < Math.min(im.getHeight(), y1); y++) {
            for (int x = Math.max(0, x0); x < Math.min(im.getWidth(), x1); x++) {
                int p = im.getRGB(x, y);
                int dr = ((p >> 16) & 255) - r0, dg = ((p >> 8) & 255) - g0, db = (p & 255) - b0;
                if (dr * dr + dg * dg + db * db <= 3600) n++;
            }
        }
        return n;
    }

    static void kiemCo(int W, int H, java.io.File ra) throws Exception {
        game.pokemon.NgamTay.datLai();
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.HUD_RIENG = false);
        KiemChamHud.dungCanh(false);
        datLaiHoiChieu();
        // (1) máy chủ báo PvP
        game.render.screen.GameScr.autoFight = true;
        pvp(true);
        kiem(game.pokemon.NgamTay.mayChu && game.pokemon.NgamTay.pvp && game.pokemon.NgamTay.dangNgam(), "-113 loai 1: vao che do ngam");
        kiem(!game.render.screen.GameScr.autoFight, "vao PvP: tat tu danh phia client");
        game.render.screen.GameScr.autoFight = true;   // bật lại tự đánh cũ giữa trận
        ngu(200);
        kiem(!game.render.screen.GameScr.autoFight, "dang PvP: bat lai tu danh -> bi tat ngay");
        final int[] n1 = nut(1);
        kiem(n1 != null, "tim thay nut chieu phim 1");
        if (n1 == null) return;
        KiemChamHud.xoaGoi();
        BufferedImage truoc = ChupTrongGame.ve(W, H);
        // (2) nhấn - kéo trái - thả
        final int kx = n1[1] - Math.max(40, n1[3] * 3), ky = n1[2];
        KiemChamHud.ve();
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerPressed", n1[1], n1[2]));
        ngu(120);
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerDragged", (n1[1] + kx) / 2, ky));
        ngu(60);
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerDragged", kx, ky));
        ngu(120);
        kiem(game.pokemon.NgamTay.dangGiu(), "nhan + keo: dang ngam");
        kiem(game.pokemon.NgamTay.phimDangGiu() == 1, "dang ngam dung phim 1");
        BufferedImage ngam = ChupTrongGame.veKiem(W, H, "ngam " + co);
        // vạch ngắm quanh Pokémon (giữa lớp thế giới): đếm màu CHINH_SANG tăng rõ so với khung trước
        int[] tam = viTriManHinh(W, H);
        int vung = Math.max(60, W / 6);
        int c0 = demMau(truoc, game.pokemon.ChuDe.CHINH_SANG, tam[0] - vung, tam[1] - vung, tam[0] + vung, tam[1] + vung);
        int c1 = demMau(ngam, game.pokemon.ChuDe.CHINH_SANG, tam[0] - vung, tam[1] - vung, tam[0] + vung, tam[1] + vung);
        kiem(c1 > c0 + 20, "vach ngam duoc ve (diem anh mau vach " + c0 + " -> " + c1 + ")");
        javax.imageio.ImageIO.write(ngam, "png", new java.io.File(ra, "ngam_" + co + ".png"));
        kiem(demBan() == 0, "dang keo: chua ban");
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerReleased", kx, ky));
        ngu(300);
        int[] b = goiBan();
        kiem(b != null && demBan() == 1, "tha: dung MOT goi -113 (" + demBan() + ")");
        kiem(b != null && b[0] == KiemChamHud.o(0, 1).getSkillType(), "goi -113: dung chieu cua o phim 1");
        kiem(b != null && b[1] < -900 && Math.abs(b[2]) < 200, "goi -113: huong keo sang trai (" + (b == null ? "-" : b[1] + "," + b[2]) + ")");
        kiem(KiemChamHud.demLenh(6) == 0 && KiemChamHud.demLenh(9) == 0, "khong goi khoa muc tieu 6 / 9");
        kiem(!game.pokemon.NgamTay.dangGiu(), "tha: het ngam");
        // (3) huỷ: kéo ra rồi về nút
        datLaiHoiChieu();
        KiemChamHud.xoaGoi();
        KiemChamHud.ve();
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerPressed", n1[1], n1[2]));
        ngu(120);
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerDragged", kx, ky));
        ngu(100);
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerDragged", n1[1] + 1, n1[2] - 1));
        ngu(120);
        kiem(game.pokemon.NgamTay.vungHuy(), "keo ve nut: vung huy");
        BufferedImage huy = ChupTrongGame.ve(W, H);
        int d1 = demMau(huy, game.pokemon.ChuDe.DO, tam[0] - vung, tam[1] - vung, tam[0] + vung, tam[1] + vung);
        int d0 = demMau(truoc, game.pokemon.ChuDe.DO, tam[0] - vung, tam[1] - vung, tam[0] + vung, tam[1] + vung);
        kiem(d1 > d0 + 10, "vung huy: vach do (" + d0 + " -> " + d1 + ")");
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerReleased", n1[1] + 1, n1[2] - 1));
        ngu(300);
        kiem(demBan() == 0 && game.pokemon.NgamTay.KQ_THA == 0, "huy: khong ban");
        // (4) chạm nhanh = hướng đang quay (phải)
        datLaiHoiChieu();
        KiemChamHud.xoaGoi();
        KiemChamHud.cham(n1[1], n1[2]);
        int[] b2 = goiBan();
        kiem(b2 != null && b2[1] > 900 && Math.abs(b2[2]) < 100, "cham nhanh: ban theo huong quay (phai)");
        // (5) PC: giữ phím 1 + con trỏ phía trên Pokémon
        datLaiHoiChieu();
        KiemChamHud.xoaGoi();
        final int[] tr = manHinhCua(0, -80);
        ChupTrongGame.chay(() -> {
            game.pokemon.NgamTay.chuotDi(tr[0], tr[1]);
            game.render.Canvas.instance.keyPressed(49);
        });
        ngu(200);
        kiem(game.pokemon.NgamTay.dangGiu() && demBan() == 0, "PC giu phim chieu: dang ngam, chua ban");
        ChupTrongGame.chay(() -> game.render.Canvas.instance.keyReleased(49));
        ngu(300);
        int[] b3 = goiBan();
        kiem(b3 != null && b3[2] < -900 && Math.abs(b3[1]) < 200, "PC tha phim: ban theo con tro (len) " + (b3 == null ? "-" : b3[1] + "," + b3[2]));
        datLaiHoiChieu();
        KiemChamHud.xoaGoi();
        ChupTrongGame.chay(() -> game.render.Canvas.instance.keyPressed(49));
        ngu(200);
        ChupTrongGame.chay(() -> game.pokemon.HanhDong.chuotPhai(5, 5));
        ChupTrongGame.chay(() -> game.render.Canvas.instance.keyReleased(49));
        ngu(300);
        kiem(demBan() == 0, "PC chuot phai: huy");
        // (6) phím J2ME (không chuột): bấm = bắn hướng quay
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.chuot = false);
        datLaiHoiChieu();
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.mainChar.dir = 2);
        KiemChamHud.xoaGoi();
        ChupTrongGame.chay(() -> {
            game.render.Canvas.instance.keyPressed(51);
            game.render.Canvas.instance.keyReleased(51);
        });
        ngu(300);
        int[] b4 = goiBan();
        kiem(b4 != null && b4[0] == KiemChamHud.o(0, 3).getSkillType() && b4[1] < -900, "phim J2ME 3: ban chieu o 3 theo huong quay (trai)");
        // (7) không khoá mục tiêu người chơi
        datLaiHoiChieu();
        KiemChamHud.xoaGoi();
        ChupTrongGame.chay(() -> {
            game.render.screen.GameScr gs = game.render.Canvas.gameScr;
            game.model.Char k = new game.model.Char();
            k.ID = 4321;
            k.catagory = 0;
            k.x = (short) (gs.mainChar.x + 20);
            k.y = gs.mainChar.y;
            k.hp = 100;
            k.realHP = 100;
            gs.focusedActor = k;
            gs.doUseSkillToFocusActor((byte) 1);
            gs.focusedActor = null;
        });
        ngu(150);
        kiem(KiemChamHud.demLenh(6) == 0, "PvP: doUseSkillToFocusActor khong gui goi 6");
        // (8) PvE không đổi / Ngắm thủ công
        pvp(false);
        kiem(!game.pokemon.NgamTay.dangNgam(), "het PvP: het ngam");
        KiemChamHud.dungCanh(true);
        datLaiHoiChieu();
        KiemChamHud.nham(KiemChamHud.quai);
        KiemChamHud.xoaGoi();
        KiemChamHud.cham(n1[1], n1[2]);
        kiem(KiemChamHud.demLenh(9) + KiemChamHud.demLenh(106) >= 1 && demBan() == 0, "PvE: cham chieu = goi 9 nhu cu");
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.datThuCong(true));
        datLaiHoiChieu();
        KiemChamHud.xoaGoi();
        KiemChamHud.cham(n1[1], n1[2]);
        kiem(demBan() == 1 && KiemChamHud.demLenh(9) == 0, "Ngam thu cong (PvE): cham chieu = goi -113");
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.datThuCong(false));
    }

    /** Lớp (ẩn danh) có tham chiếu game.pokemon.NgamTay trong hằng số (công tắc Ngắm thủ công). */
    static boolean thamChieuNgamTay(Class<?> c) {
        try (java.io.InputStream in = c.getClassLoader().getResourceAsStream(c.getName().replace('.', '/') + ".class")) {
            if (in == null) return false;
            byte[] b = in.readAllBytes();
            return new String(b, java.nio.charset.StandardCharsets.ISO_8859_1).contains("game/pokemon/NgamTay");
        } catch (Exception e) {
            return false;
        }
    }

    /** Toạ độ màn hình (logic) của điểm thế giới (Pokémon + dx, dy) - đảo HanhDong.wx / wy (đã vá zoom PC). */
    static int[] manHinhCua(final int dx, final int dy) throws Exception {
        final int[] ra = {0, 0};
        ChupTrongGame.chay(() -> {
            game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
            int tx = mc.x + dx, ty = mc.y - 12 + dy;
            int bx = 0, by = 0, ex = Integer.MAX_VALUE, ey = Integer.MAX_VALUE;
            for (int x = 0; x < game.render.Canvas.w; x++) {
                int e = Math.abs(game.pokemon.HanhDong.wx(x) - tx);
                if (e < ex) {
                    ex = e;
                    bx = x;
                }
            }
            for (int y = 0; y < game.render.Canvas.h; y++) {
                int e = Math.abs(game.pokemon.HanhDong.wy(y) - ty);
                if (e < ey) {
                    ey = e;
                    by = y;
                }
            }
            ra[0] = bx;
            ra[1] = by;
        });
        return ra;
    }

    /** Vị trí Pokémon trên ẢNH chụp (điểm ảnh thật). */
    static int[] viTriManHinh(int W, int H) throws Exception {
        int[] l = manHinhCua(0, 0);
        float s = NenPc.hienW / (float) NenPc.rong;
        return new int[]{NenPc.hienX + Math.round(l[0] * s), NenPc.hienY + Math.round(l[1] * s)};
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "kiem_ngam_tay");
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
                synchronized (KiemChamHud.LENH) {
                    KiemChamHud.LENH.add((int) m.command);
                    KiemChamHud.GOI.add(m.getData());
                }
            }

            public void close() {
            }
        });
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ngu(500);
        // bảng chiêu -113 loại 2 đọc đúng (trước mọi cỡ)
        game.pokemon.NgamTay.datLai();
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream d = new java.io.DataOutputStream(bo);
        d.writeByte(2);
        d.writeByte(1);
        d.writeByte(1);
        d.writeByte(2);   // kiểu nón
        d.writeShort(77);
        d.writeShort(30);
        d.writeShort(0);
        d.writeByte(20);  // góc 60
        d.writeByte(4);
        final byte[] bang = bo.toByteArray();
        ChupTrongGame.chay(() -> {
            try {
                game.pokemon.NgamTay.nhan(goiVao(bang));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        int lop = game.render.Canvas.gameScr.mainChar.clazz;
        int[] ts = game.pokemon.NgamTay.thongSo(lop, 1);
        co = "bang";
        kiem(game.pokemon.NgamTay.mayChu && ts[0] == 2 && ts[1] == 77 && ts[2] == 30 && ts[4] == 60, "-113 loai 2: bang chieu (non 77 px, 60 do)");
        kiem(game.pokemon.NgamTay.gocDo(1000, 0) == 0 && game.pokemon.NgamTay.gocDo(0, 1000) == 90
                && game.pokemon.NgamTay.gocDo(-1000, 0) == 180 && game.pokemon.NgamTay.gocDo(0, -1000) == 270
                && Math.abs(game.pokemon.NgamTay.gocDo(1000, 1000) - 45) <= 1 && Math.abs(game.pokemon.NgamTay.gocDo(-500, 866) - 120) <= 1,
                "gocDo (thay Math.atan2 cho CLDC 1.1)");
        int[] cn = game.pokemon.NgamTay.chuan(-30, 40);
        kiem(cn != null && cn[0] == -600 && cn[1] == 800, "chuan hoa vector x1000");
        kiem(game.pokemon.NgamTay.chuan(1, 1) == null, "keo qua ngan: chua co huong");
        String locCo = System.getProperty("chup.co");
        for (int c = 0; c < ChupTrongGame.CO.length; c++) {
            if (locCo != null && !("," + locCo + ",").contains("," + ChupTrongGame.TEN_CO[c] + ",")) continue;
            final int W = ChupTrongGame.CO[c][0], H = ChupTrongGame.CO[c][1];
            co = ChupTrongGame.TEN_CO[c];
            KiemChamHud.co = co;
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            game.pokemon.VungCham.MIN = 0;
            try {
                kiemCo(W, H, ra);
            } catch (Throwable e) {
                e.printStackTrace();
                kiem(false, "nem loi: " + e);
            }
        }
        // Cài đặt PC (LopPhu, thẻ Đổi phím): công tắc "Ngắm thủ công" vẽ ra, nằm trong màn, bấm đổi được
        for (String c : new String[]{"pc720", "pc4x3", "pc21x9", "dt20x9"}) {
            int i = java.util.Arrays.asList(ChupTrongGame.TEN_CO).indexOf(c);
            if (locCo != null && !("," + locCo + ",").contains("," + c + ",")) continue;
            final int W = ChupTrongGame.CO[i][0], H = ChupTrongGame.CO[i][1];
            co = "cai_dat_" + c;
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            ChupTrongGame.chay(() -> {
                try {
                    htbb.pc.LopPhu.moCaiDat();
                    KiemChamHud.datTinh(htbb.pc.LopPhu.class, "tab", Integer.valueOf(2));   // thẻ Đổi phím
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            BufferedImage im = ChupTrongGame.ve(W, H); // man Cai dat (doi phim) da kiem chu / icon o ChupTrongGame; HUD sau tam bi che mot phan
            javax.imageio.ImageIO.write(im, "png", new java.io.File(ra, "cai_dat_doi_phim_" + c + ".png"));
            final float[] o = {-1, -1, -1, -1};
            final int[] soVung = {0};
            ChupTrongGame.chay(() -> {
                try {
                    java.lang.reflect.Field f = htbb.pc.LopPhu.class.getDeclaredField("dangVe");
                    f.setAccessible(true);
                    java.util.List<?> ds = new java.util.ArrayList<Object>((java.util.List<?>) f.get(null));
                    soVung[0] = ds.size();
                    for (Object v : ds) {
                        java.lang.reflect.Field fb = v.getClass().getDeclaredField("bam");
                        fb.setAccessible(true);
                        Runnable r = (Runnable) fb.get(v);
                        if (r == null) continue;
                        String ten = r.getClass().getName();
                        if (!ten.startsWith("htbb.pc.LopPhu$")) continue;
                        boolean truoc = game.pokemon.NgamTay.thuCong();
                        // chỉ thử công tắc của nhóm Chiến đấu: lớp ẩn danh có gọi NgamTay (đọc mã byte tên tham chiếu)
                        if (!thamChieuNgamTay(r.getClass())) continue;
                        r.run();
                        if (game.pokemon.NgamTay.thuCong() != truoc) {
                            for (String fn : new String[]{"x", "y", "w", "h"}) {
                                java.lang.reflect.Field q = v.getClass().getDeclaredField(fn);
                                q.setAccessible(true);
                                o["xywh".indexOf(fn)] = q.getFloat(v);
                            }
                            r.run();   // trả lại
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            kiem(o[2] > 0, "cai dat PC: co cong tac Ngam thu cong (" + soVung[0] + " vung)");
            // vùng bấm của LopPhu tính theo bộ đệm thật (NenPc.thatW x thatH), không phải ảnh đã co
            kiem(o[0] >= 0 && o[1] >= 0 && o[0] + o[2] <= NenPc.thatW + 1 && o[1] + o[3] <= NenPc.thatH + 1,
                    "cai dat PC: cong tac trong man (" + o[0] + "," + o[1] + " " + o[2] + "x" + o[3] + " / " + NenPc.thatW + "x" + NenPc.thatH + ")");
            ChupTrongGame.chay(() -> {
                try {
                    KiemChamHud.datTinh(htbb.pc.LopPhu.class, "caiDatMo", Boolean.FALSE);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
        kiemCamera();
        // vạch / nón / vòng ngắm là HÌNH CHỈ HƯỚNG (không phải icon) - vẽ bằng lệnh là chủ ý, VeTay không đếm (miễn trừ ghi ở
        // NgamTay.java); mọi icon trên màn ngắm vẫn phải là PNG (veKiem: VeTay = 0)
        kiem(ChupTrongGame.hong == 0, "chu trong khung + icon PNG (ChupTrongGame.veKiem): " + ChupTrongGame.hong + " hong");
        System.out.println("KiemNgamTay: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }

    /**
     * 0.9.8 (giao-dien-nro c6c276a6): camera "cao hơn" NHIN_THEM 1,15 (vùng nhìn 368 x 207 px thế giới ở APK). Ngắm tay
     * bằng chuột / chạm điểm phải đổi điểm màn -> thế giới CÙNG công thức với chạm chọn quái / đi tới (HanhDong.wx / wy, bản
     * PC = Zoom.theGioiX/Y, bản APK = HdZoom.wx -> KhungCoDinh.theGioi): hướng ngắm tới đúng điểm thế giới dưới con trỏ.
     */
    static void kiemCamera() {
        co = "camera";
        try {
            // gốc repo: -Dhtbb.goc, không thì suy từ client_pc/out/build/cls của lớp game
            java.io.File cls = new java.io.File(game.pokemon.NgamTay.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String goc = System.getProperty("htbb.goc", cls.getParentFile().getParentFile().getParentFile().getParent());
            String nt = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(goc, "client_jar/src/game/pokemon/NgamTay.java")), "UTF-8");
            kiem(nt.contains("chuan(HanhDong.wx(x) - mc.x, HanhDong.wy(y) - (mc.y - 12))"), "NgamTay.huongChuot doi diem qua HanhDong.wx / wy");
            String apk = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(goc, "client_apk/port-j2me.py")), "UTF-8");
            kiem(apk.contains("\"game/pokemon/HanhDong.java\", r\"\\n    public static int wx\\(int px\\)\", \" if (true) { return %sHdZoom.wx(GameScr.cmx, px); }\"")
                    && apk.contains("\"cmx + Canvas.px\", \"%sHdZoom.wx(cmx, Canvas.px)\""), "APK: HanhDong.wx va cham GameScr cung HdZoom.wx (KhungCoDinh.theGioi)");
            String kc = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(goc,
                    "client_apk/pokemon/src/main/java/javax/microedition/lcdui/KhungCoDinh.java")), "UTF-8");
            kiem(kc.contains("NHIN_THEM = " + htbb.pc.Zoom.NHIN_THEM + "f"), "APK KhungCoDinh.NHIN_THEM == PC Zoom.NHIN_THEM (" + htbb.pc.Zoom.NHIN_THEM + ")");
            // APK: vùng nhìn 640 x 360 logic -> 368 x 207 thế giới; chạm px -> floor(px / (2 / 1,15))
            float zA = 2f / 1.15f;
            kiem((int) Math.ceil(640 / zA - 1e-3f) == 368 && (int) Math.ceil(360 / zA - 1e-3f) == 207, "APK vung nhin 368 x 207");
            // chạy trên luồng game (camera không trôi giữa hai lần đọc cmx)
            final int[][] ra = new int[1][];
            ChupTrongGame.chay(() -> {
                try {
                    // PC: HanhDong.wx == cmx + Zoom.theGioiX; điểm thế giới dưới con trỏ -> hướng ngắm tới đúng điểm đó
                    game.render.screen.GameScr gs = game.render.Canvas.gameScr;
                    game.model.MainChar mc = gs.mainChar;
                    int cx0 = game.render.screen.GameScr.cmx, cy0 = game.render.screen.GameScr.cmy;
                    java.lang.reflect.Method hc = game.pokemon.NgamTay.class.getDeclaredMethod("huongChuot", int.class, int.class);
                    hc.setAccessible(true);
                    float z = htbb.pc.Zoom.z();
                    final int[] kq = new int[3]; // lech, sai, thu
                    for (int tx = -160; tx <= 160; tx += 23) {
                        for (int ty = -90; ty <= 90; ty += 17) {
                            if (tx * tx + ty * ty < 40 * 40) {
                                continue;
                            }
                            int wx = mc.x + tx, wy = mc.y - 12 + ty;
                            int px = (int) Math.ceil((wx - cx0) * z), py = (int) Math.ceil((wy - cy0) * z);
                            if (game.pokemon.HanhDong.wx(px) != cx0 + htbb.pc.Zoom.theGioiX(px)) {
                                kq[1]++;
                            }
                            // điểm thế giới đọc lại từ điểm màn (một điểm ảnh màn phủ ~1 / z px thế giới: sai số <= 1 px)
                            int rx = game.pokemon.HanhDong.wx(px), ry = game.pokemon.HanhDong.wy(py);
                            int[] h = (int[]) hc.invoke(null, px, py);
                            int[] mong = game.pokemon.NgamTay.chuan(rx - mc.x, ry - (mc.y - 12));
                                                        kq[2]++;
                            if (h == null || mong == null || Math.abs(rx - wx) > 1 || Math.abs(ry - wy) > 1
                                    || h[0] != mong[0] || h[1] != mong[1]) {
                                kq[0]++;
                                continue;
                            }
                            // góc giữa hướng ngắm và hướng tới điểm thật: <= sai số 1 px thế giới ở khoảng cách đó
                            double gNgam = Math.atan2(ry - (mc.y - 12), rx - mc.x), gThat = Math.atan2(ty, tx);
                            double d = Math.abs(Math.IEEEremainder(gNgam - gThat, 2 * Math.PI));
                            if (d > Math.atan(1.5 / Math.sqrt(tx * tx + ty * ty)) + 1e-6) {
                                kq[0]++;
                            }
                        }
                    }
                    ra[0] = kq;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            final int lech = ra[0] == null ? -1 : ra[0][0], sai = ra[0] == null ? -1 : ra[0][1], thu = ra[0] == null ? 0 : ra[0][2];
            final float z = htbb.pc.Zoom.z();
            kiem(sai == 0, "PC: HanhDong.wx(px) == cmx + Zoom.theGioiX(px) (z = " + z + ")");
            kiem(thu > 50 && lech == 0, "PC: ngam chuot toi diem the gioi duoi con tro (sai so <= 1 px the gioi) (" + lech + "/" + thu + ")");
        } catch (Throwable e) {
            e.printStackTrace();
            kiem(false, "kiemCamera: " + e);
        }
    }

}
