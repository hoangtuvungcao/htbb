import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM ĐIỂM NHIỆM VỤ (báo lỗi người chơi 29/10/2026: "Làm sao để tới cái ô 53 19 thế"): bước địa điểm của chuỗi chính /
 * phụ chỉ ghi toạ độ ô. Máy chủ mới gửi ĐUÔI THỨ HAI gói -64 (điểm đích: bản đồ nạp, ô tâm, nửa rộng / cao, cả bản đồ,
 * tên nơi - real.MessageCreator.createMsgCharQuest). Đưa gói -64 THẬT (GameScr.onQuest) rồi kiểm:
 * <ul>
 *   <li>đọc đuôi thứ hai vào Quest.dich*; gói chỉ có đuôi thứ nhất (máy chủ cũ) / không đuôi: dichMap = -1, đọc như cũ;</li>
 *   <li>cùng bản đồ: dấu sao vẽ trên ô đích khi trong màn, ngoài màn thì mũi tên quanh nhân vật; bản đồ khác: không vẽ;</li>
 *   <li>thẻ chi tiết sổ nhiệm vụ: nút "Đi tới dấu sao" (chạm = tìm đường, nhân vật có đường đi), không còn "Mở
 *       Nhiệm vụ của tôi"; khác bản đồ: nút tắt "Tới &lt;nơi&gt;"; dòng "Còn N ô tới dấu sao";</li>
 *   <li>máy phím: phím 5 ở sổ nhiệm vụ = đi tới.</li>
 * </ul>
 * Cỡ: 1280x720, 640x360, 240x320 (-Dchup.j2me=1 = vẽ như J2ME). Ảnh: &lt;thư mục&gt;/diem_*.png.
 *   APP_IMAGE=0 sh client_pc/build.sh && O=$(mktemp -d)
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemDiemNhiemVu.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemDiemNhiemVu &lt;thư mục ảnh&gt;
 * Mã thoát 1 = có phép kiểm hỏng.
 */
public class KiemDiemNhiemVu {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    /** -64 nhóm 2: chính = bước địa điểm (type 3, NPC trả -1); đuôi 1 (mục tiêu) / đuôi 2 (điểm đích) tuỳ chọn. */
    static byte[] goi(int duoi, int map, int x, int y) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeByte(2);
        o.writeByte(1);
        o.writeShort(14);
        o.writeByte(0);
        o.writeUTF("Nơi ngôi sao rơi");
        o.writeByte(3);
        o.writeUTF("Ra đồng cỏ Pallet");
        o.writeByte(-1);
        o.writeByte(0);
        o.writeUTF("Phần thưởng:|4.038 EXP, 1.009 xu");
        if (duoi >= 1) {
            o.writeByte(1);
            o.writeUTF("Ra đồng cỏ Pallet (cổng phía Đông làng), đứng ở chỗ cỏ cháy góc trên bên phải");
            o.writeUTF("");
            o.writeUTF("");
            o.writeByte(0);
        }
        if (duoi >= 2) {
            o.writeByte(1);
            o.writeShort(map);
            o.writeShort(x);
            o.writeShort(y);
            o.writeByte(3);
            o.writeByte(3);
            o.writeByte(0);
            o.writeUTF("Pallet (góc trên bên phải)");
        }
        return b.toByteArray();
    }

    static void dat(final int duoi, final int map, final int x, final int y) throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                ChupTrongGame.goi(-64, new byte[]{1, 0, 0});
                ChupTrongGame.goi(-64, new byte[]{0, 0, 0});
                ChupTrongGame.goi(-64, goi(duoi, map, x, y));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static List<String> chu() {
        List<String> l = new java.util.ArrayList<String>();
        for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
            if (game.pokemon.GhiChu.CS[i] != null) {
                l.add(game.pokemon.GhiChu.CS[i]);
            }
        }
        return l;
    }

    static boolean coChua(List<String> ve, String s) {
        for (String x : ve) {
            if (x.contains(s)) {
                return true;
            }
        }
        return false;
    }

    static BufferedImage veGhi(int W, int H) throws Exception {
        game.pokemon.GhiChu.xoa();
        game.pokemon.GhiChu.bat = true;
        BufferedImage a = ChupTrongGame.ve(W, H);
        game.pokemon.GhiChu.bat = false;
        return a;
    }

    static int vung(String ten) {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) {
                return i;
            }
        }
        return -1;
    }

    static void cham(String ten) throws Exception {
        int i = vung(ten);
        if (i < 0) {
            return;
        }
        final int[] v = game.pokemon.VungCham.vungVe(i);
        ChupTrongGame.chay(() -> {
            game.render.Canvas.pxLast = v[0] + v[2] / 2;
            game.render.Canvas.pyLast = v[1] + v[3] / 2;
            game.render.Canvas.px = v[0] + v[2] / 2;
            game.render.Canvas.py = v[1] + v[3] / 2;
            game.render.Canvas.isPointerClick = true;
            game.pokemon.VungCham.xuLy();
        });
    }

    /** Ô đi được gần nhân vật (cách dx..dx+8 ô về phải), -1 = không có. */
    static int[] oGan(int dx) {
        game.model.MainChar c = game.render.Canvas.gameScr.mainChar;
        int ox = c.x >> 4, oy = c.y >> 4;
        for (int d = dx; d < dx + 30; d++) {
            for (int e = -3; e <= 3; e++) {
                int tx = ox + d, ty = oy + e;
                if (tx >= 2 && ty >= 2 && tx < game.model.Tilemap.w - 2 && ty < game.model.Tilemap.h - 2
                        && !game.model.Tilemap.tileTypeAtPixel(tx * 16 + 8, ty * 16 + 8, 2)) {
                    return new int[]{tx, ty};
                }
            }
        }
        return null;
    }

    /** O di duoc xa nhan vat nhat tren ban do (ngoai man), null = map nho hon man. */
    static int[] xa() {
        game.model.MainChar c = game.render.Canvas.gameScr.mainChar;
        int[] tot = null;
        int dt = -1;
        for (int ty = 2; ty < game.model.Tilemap.h - 2; ty++) {
            for (int tx = 2; tx < game.model.Tilemap.w - 2; tx++) {
                int sx = tx * 16 + 8 - game.render.screen.GameScr.cmx, sy = ty * 16 + 8 - game.render.screen.GameScr.cmy;
                if (sx >= 0 && sx < game.render.Canvas.w && sy >= 0 && sy < game.render.Canvas.h) continue;
                if (game.model.Tilemap.tileTypeAtPixel(tx * 16 + 8, ty * 16 + 8, 2)) continue;
                int d = Math.abs(tx * 16 - c.x) + Math.abs(ty * 16 - c.y);
                if (d > dt) {
                    dt = d;
                    tot = new int[]{tx, ty};
                }
            }
        }
        return tot;
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "kiem_diem_nhiem_vu");
        ra.mkdirs();
        boolean j2me = System.getProperty("chup.j2me") != null;
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                game.render.screen.TField.isQwerty = true;
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) {
            Thread.sleep(100);
        }
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        Thread.sleep(800);
        if (j2me) {
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            game.pokemon.ChuUI.bo = null;
        }
        game.networklogic.GameService.gI().setSession(new network.ISession() {
            public boolean isConnected() {
                return true;
            }

            public void setHandler(network.IMessageHandler h) {
            }

            public void connect(String s, String s2) {
            }

            public void sendMessage(network.Message m) {
            }

            public void close() {
            }
        });
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.render.Canvas.endDlg();
        });
        final int ban = game.render.screen.GameScr.mapNap >= 0 ? game.render.screen.GameScr.mapNap : 1;
        game.render.screen.GameScr.mapNap = ban;

        // ---- máy chủ cũ: không đuôi / chỉ đuôi mục tiêu
        dat(0, ban, 10, 10);
        game.model.Quest q = game.render.screen.GameScr.mainQuest;
        kiem(q != null && q.dichMap == -1 && !q.coMuc(), "khong duoi: doc dung, khong diem dich");
        dat(1, ban, 10, 10);
        q = game.render.screen.GameScr.mainQuest;
        kiem(q != null && q.dichMap == -1 && q.coMuc(), "chi duoi muc tieu (may chu 0.9.15..0.9.22): khong diem dich");
        kiem(game.pokemon.DiemNhiemVu.nhiemVu() == null, "khong diem dich: DiemNhiemVu khong co nhiem vu");

        int[][] co = {{1280, 720}, {640, 360}, {240, 320}};
        String hau = j2me ? "_j2me" : "";
        for (int c = 0; c < co.length; c++) {
            final int W = co[c][0], H = co[c][1];
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            String n = W + "x" + H + hau;
            ChupTrongGame.chay(() -> {
                game.render.Canvas.endDlg();
                game.render.Canvas.menu.showMenu = false;
                game.render.Canvas.gameScr.switchToMe();
            });
            // ---- cùng bản đồ, điểm gần (trong màn)
            int[] g = oGan(2);
            kiem(g != null, n + " co o di duoc gan nhan vat");
            if (g == null) {
                continue;
            }
            dat(2, ban, g[0], g[1]);
            q = game.render.screen.GameScr.mainQuest;
            kiem(q != null && q.dichMap == ban && q.dichX == g[0] && q.dichY == g[1] && q.dichRx == 3 && "Pallet (góc trên bên phải)".equals(q.dichNoi),
                    n + " doc duoi diem dich (" + (q == null ? null : q.dichMap + " " + q.dichX + "," + q.dichY + " " + q.dichNoi) + ")");
            ImageIO.write(veGhi(W, H), "png", new File(ra, "diem_" + n + "_gan.png"));
            kiem(game.pokemon.DiemNhiemVu.saoVuaVe != null, n + " dau sao ve tren o dich trong man");
            // ---- điểm xa (ngoài màn): mũi tên mép màn
            int[] xa = xa();
            kiem(xa != null, n + " co o ngoai man");
            if (xa != null) {
                dat(2, ban, xa[0], xa[1]);
                ImageIO.write(veGhi(W, H), "png", new File(ra, "diem_" + n + "_xa.png"));
                kiem(game.pokemon.DiemNhiemVu.muiTenVuaVe != null, n + " diem ngoai man: mui ten mep man");
                int[] m = game.pokemon.DiemNhiemVu.muiTenVuaVe;
                game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
                int csx = mc.x - game.render.screen.GameScr.cmx, csy = mc.y - game.render.screen.GameScr.cmy;
                int dsx = xa[0] * 16 + 8 - game.render.screen.GameScr.cmx, dsy = xa[1] * 16 + 8 - game.render.screen.GameScr.cmy;
                System.out.println(n + " nhan vat man " + csx + "," + csy + " dich man " + dsx + "," + dsy + " mui ten " + (m == null ? "-" : m[0] + "," + m[1]));
                // mui ten nam cung phia dich so voi nhan vat
                kiem(m != null && (long) (m[0] - csx) * (dsx - csx) + (long) (m[1] - csy + 14) * (dsy - csy) > 0, n + " mui ten cung phia diem dich");
                kiem(m != null && m[0] >= 0 && m[0] < game.render.Canvas.w && m[1] >= 0 && m[1] < game.render.Canvas.h, n + " mui ten trong man");
            }
            // ---- sổ nhiệm vụ, thẻ chi tiết
            ChupTrongGame.chay(() -> {
                game.pokemon.ManNhiemVu.mo();
                ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "tab", Integer.valueOf(0));
                ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "chon", game.render.screen.GameScr.mainQuest);
            });
            ImageIO.write(veGhi(W, H), "png", new File(ra, "diem_" + n + "_so.png"));
            List<String> ve = chu();
            kiem(coChua(ve, "Đi tới dấu sao"), n + " so: nut 'Di toi diem danh dau'");
            kiem(!coChua(ve, "Mở Nhiệm vụ của tôi"), n + " so: khong con 'Mo Nhiem vu cua toi'");
            kiem(coChua(ve, "tới dấu sao") || coChua(ve, "Đã tới điểm"), n + " so: dong con N o");
            kiem(vung("di-toi") >= 0, n + " so: nut cham duoc");
            ChupTrongGame.kiemChu(n + " so");
            game.render.Canvas.gameScr.mainChar.posTransRoad = null;
            cham("di-toi");
            kiem(game.render.Canvas.currentScreen == game.render.Canvas.gameScr, n + " cham di toi: ve the gioi");
            kiem(game.render.Canvas.gameScr.mainChar.posTransRoad != null, n + " cham di toi: nhan vat co duong di");
            // ---- máy phím: phím 5 ở sổ nhiệm vụ
            game.render.Canvas.gameScr.mainChar.posTransRoad = null;
            ChupTrongGame.chay(() -> {
                game.pokemon.ManNhiemVu.mo();
                game.render.Canvas.keyPressed[5] = true;
                game.pokemon.ManNhiemVu.gI().update();
                game.render.Canvas.keyPressed[5] = false;
            });
            kiem(game.render.Canvas.gameScr.mainChar.posTransRoad != null, n + " phim 5: nhan vat co duong di");
            // ---- bản đồ khác: nút tắt "Tới <nơi>", không vẽ dấu
            dat(2, ban + 1, 5, 5);
            ChupTrongGame.chay(() -> {
                game.render.Canvas.endDlg();
                game.render.Canvas.gameScr.switchToMe();
            });
            veGhi(W, H);
            kiem(game.pokemon.DiemNhiemVu.saoVuaVe == null && game.pokemon.DiemNhiemVu.muiTenVuaVe == null, n + " ban do khac: khong ve dau");
            ChupTrongGame.chay(() -> {
                game.pokemon.ManNhiemVu.mo();
                ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "chon", game.render.screen.GameScr.mainQuest);
            });
            ImageIO.write(veGhi(W, H), "png", new File(ra, "diem_" + n + "_ban_do_khac.png"));
            ve = chu();
            kiem(coChua(ve, "Tới Pallet"), n + " ban do khac: nut 'Toi Pallet'");
        }
        System.out.println("=== KiemDiemNhiemVu: DAT " + dat + ", HONG " + hong + " ===");
        System.exit(hong == 0 ? 0 : 1);
    }
}
