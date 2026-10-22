import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM THẺ MỤC TIÊU TÂN THỦ (0.9.8, game.pokemon.TheNhiemVu, nhánh huong-dan) ở 11 cỡ chuẩn của ChupTrongGame: danh
 * sách nhiệm vụ đưa vào bằng gói -64 THẬT (MessageHandler -> GameScr.onQuest, như máy chủ gửi); mỗi trạng thái (chưa
 * nhận / đang hạ quái / đang làm hành động / đủ để trả / NPC ở bản đồ khác / cấp cao / đang mở hộp thoại) vẽ một khung:
 * thẻ trong vùng an toàn dưới thẻ trạng thái, không chồng vùng chạm HUD, nút đủ cỡ chạm, chữ đúng việc cần làm, mũi tên
 * chỉ đúng hướng NPC ngoài màn, chạm thân thẻ mở sổ nhiệm vụ. Ảnh từng cỡ ghi ra thư mục tham số.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && O=$(mktemp -d)
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemTheNhiemVu.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTheNhiemVu <thư mục ảnh>
 * Mã thoát 1 = có phép kiểm hỏng.
 */
public class KiemTheNhiemVu {
    static int dat, hong, coMuiTen;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    // ------------------------------------------------------------------ gói -64 như máy chủ (MessageCreator.createMsgCharQuest)

    static byte[] nhom0(int id, String ten, int npc, String noi, int loai) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeByte(0);
        o.writeByte(1);
        o.writeShort(id);
        o.writeByte(0);
        o.writeUTF(ten);
        o.writeByte(npc);
        o.writeUTF(noi);
        o.writeByte(loai);
        o.writeByte(0);
        return b.toByteArray();
    }

    static byte[] rong(int nhom) throws Exception {
        return new byte[]{(byte) nhom, 0};
    }

    /** Đang làm: loai 0 (hạ quái {id, có, cần}) / 2 (vật phẩm {id, có, cần}). */
    static byte[] nhom2(int id, String ten, int loai, String ngan, int npcTra, int[] muc) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeByte(2);
        o.writeByte(1);
        o.writeShort(id);
        o.writeByte(0);
        o.writeUTF(ten);
        o.writeByte(loai);
        o.writeUTF(ngan);
        o.writeByte(npcTra);
        o.writeByte(0);
        o.writeUTF("Nhiệm vụ:|" + ngan + "|Phần thưởng:|1.735 EXP, 150 xu");
        o.writeByte(1);
        o.writeShort(muc[0]);
        o.writeShort(muc[1]);
        o.writeShort(muc[2]);
        return b.toByteArray();
    }

    static byte[] nhom1(int id, String ten, int npcTra, String tra) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeByte(1);
        o.writeByte(1);
        o.writeShort(id);
        o.writeByte(0);
        o.writeUTF(ten);
        o.writeByte(npcTra);
        o.writeUTF(tra);
        o.writeUTF("Nhiệm vụ hoàn thành");
        o.writeUTF("Phần thưởng:|2.370 EXP, 100 xu");
        return b.toByteArray();
    }

    /** Trạng thái danh sách nhiệm vụ: 0 chưa nhận, 1 hạ quái, 2 hành động, 3 đủ để trả, 4 NPC ở bản đồ khác. */
    static void datTrangThai(int tt) throws Exception {
        ChupTrongGame.goi(-64, rong(1));
        ChupTrongGame.goi(-64, rong(2));
        switch (tt) {
            case 0:
                ChupTrongGame.goi(-64, nhom0(0, "Chào trưởng làng", 5, "1Xatu: ...Cháu tới rồi.>1Xatu: Ta là Xatu, trưởng làng.", 3));
                break;
            case 1:
                ChupTrongGame.goi(-64, rong(0));
                ChupTrongGame.goi(-64, nhom2(3, "Bài học chiến đấu", 0, "Hạ 8 Caterpie ở Pallet", 21, new int[]{0, 3, 8}));
                break;
            case 2:
                ChupTrongGame.goi(-64, rong(0));
                ChupTrongGame.goi(-64, nhom2(2, "Bình thuốc khi máu thấp", 2, "Đánh quái ở Pallet, máu vơi thì dùng 1 bình thuốc", 21, new int[]{53, 0, 1}));
                break;
            case 3:
                ChupTrongGame.goi(-64, rong(0));
                ChupTrongGame.goi(-64, nhom1(4, "Chiêu thức đầu tiên", 0, "1Chansey: Chan-sey!"));
                break;
            default:
                ChupTrongGame.goi(-64, nhom0(9, "Về báo trưởng làng", 10, "1Lapras: Lên đò nào.>1Lapras: Nhận chứ?", 1));
                break;
        }
    }

    // ------------------------------------------------------------------ kiểm một khung

    static boolean giao(int[] a, int[] b) {
        return a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
    }

    static int vung(String ten) {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) if (ten.equals(game.pokemon.VungCham.ten(i))) return i;
        return -1;
    }

    static game.model.Actor npc(int loai) {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        for (int i = 0; i < gs.actors.size(); i++) {
            game.model.Actor a = (game.model.Actor) gs.actors.elementAt(i);
            try {
                if (a.isNPC() && a.getTypeNpc() == loai) return a;
            } catch (Throwable e) {
                // bỏ
            }
        }
        return null;
    }

    static void kiemKhung(String nhan, int tt, int min, int cach) {
        int[] the = game.pokemon.TheNhiemVu.vuaVe, nut = game.pokemon.TheNhiemVu.nutVuaVe;
        String[] chu = game.pokemon.TheNhiemVu.chuVuaVe;
        kiem(the != null && nut != null && chu != null, nhan + " the hien");
        if (the == null || chu == null) return;
        int W = game.render.Canvas.w, H = game.render.Canvas.h;
        kiem(the[0] >= game.pokemon.Bang.trai() && the[0] + the[2] <= W && the[1] + the[3] <= H - 60, nhan + " the trong vung an toan " + java.util.Arrays.toString(the) + " man " + W + "x" + H);
        kiem(the[1] > game.render.screen.GameScr.day12plus && the[1] >= 50, nhan + " the duoi the trang thai + nhan 12+ (y " + the[1] + ", nhan den " + game.render.screen.GameScr.day12plus + ")");
        // không chồng vùng chạm HUD (nút tròn / hộp)
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            String t = game.pokemon.VungCham.ten(i);
            if (t != null && t.startsWith("hud-")) kiem(!giao(the, game.pokemon.VungCham.vungVe(i)), nhan + " the khong chong " + t);
        }
        kiem(chu[0].length() > 0 && !chu[0].equals("..."), nhan + " co tieu de: " + chu[0]);
        String viec = chu[1];
        String[] mong = {"Nhận ở", "Caterpie", "Bình thuốc đã dùng 0/1", "Xong! Trả ở", "Nhận ở"};
        kiem(viec.contains(mong[tt]) || (tt == 1 && viec.contains("3/8")), nhan + " viec can lam '" + viec + "' chua '" + mong[tt] + "'");
        int iThe = vung("nv-the"), iNut = vung("nv-di-toi");
        kiem(iThe >= 0, nhan + " cham than the (mo so nhiem vu)");
        if (tt == 4) {
            kiem(iNut < 0 && chu[2].endsWith("ở bản đồ khác"), nhan + " NPC ban do khac: nut tat '" + chu[2] + "'");
        } else {
            kiem(iNut >= 0 && chu[2].startsWith("Đi tới"), nhan + " nut Di toi '" + chu[2] + "'");
            if (iNut >= 0) {
                int[] v = game.pokemon.VungCham.vungVe(iNut);
                kiem(v[2] >= min && v[3] >= min, nhan + " nut du co cham " + v[2] + "x" + v[3] + " >= " + min);
                int[] t = game.pokemon.VungCham.vungVe(iThe);
                int ho = v[1] - (t[1] + t[3]);
                kiem(ho >= cach, nhan + " khoang ho than / nut " + ho + " >= " + cach);
                kiem(game.pokemon.VungCham.tim(v[0] + v[2] / 2, v[1] + v[3] / 2) == iNut, nhan + " bam tam nut trung nut");
            }
        }
        if (iThe >= 0) {
            int[] v = game.pokemon.VungCham.vungVe(iThe);
            kiem(v[3] >= min, nhan + " than the du cao cham " + v[3] + " >= " + min);
        }
        // mũi tên: NPC cần gặp ngoài màn -> có, đúng hướng, trong màn, không đè thẻ
        int loai = tt == 0 ? 5 : tt == 3 ? 0 : tt == 4 ? 10 : 21;
        game.model.Actor a = npc(loai);
        int[] mt = game.pokemon.TheNhiemVu.muiTenVuaVe;
        if (a == null) {
            kiem(mt == null, nhan + " khong NPC -> khong mui ten");
        } else {
            int sx = a.x - game.render.screen.GameScr.cmx, sy = a.y - game.render.screen.GameScr.cmy;
            boolean ngoai = sx < 0 || sx >= W || sy < 0 || sy >= H;
            kiem(ngoai == (mt != null || game.pokemon.TheNhiemVu.muiTenBiThe), nhan + " mui ten khi NPC ngoai man (ngoai=" + ngoai + ")");
            if (mt != null) {
                kiem(mt[3] * (a.x - game.render.Canvas.gameScr.mainChar.x) + mt[4] * (a.y - game.render.Canvas.gameScr.mainChar.y) > 0, nhan + " mui ten dung huong");
                kiem(mt[0] - mt[2] >= 0 && mt[0] + mt[2] <= W && mt[1] - mt[2] >= 0 && mt[1] + mt[2] <= H, nhan + " mui ten trong man");
                int[] o = {mt[0] - mt[2], mt[1] - mt[2], 2 * mt[2], 2 * mt[2]};
                kiem(!giao(the, o), nhan + " mui ten khong de the");
                for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
                    String t = game.pokemon.VungCham.ten(i);
                    if (t != null && t.startsWith("hud-")) kiem(!giao(o, game.pokemon.VungCham.vungVe(i)), nhan + " mui ten khong de " + t);
                }
            }
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "kiem_the_nhiem_vu");
        ra.mkdirs();
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        Thread.sleep(800);
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.render.Canvas.endDlg();
            game.render.Canvas.gameScr.mainChar.level = 3;
            String[] ten = new String[60];
            for (int i = 0; i < ten.length; i++) ten[i] = "Vật phẩm " + i;
            ten[53] = "Bình thuốc đã dùng";
            game.model.ItemQuest.NAME_ITEM = ten;
        });
        String[] tenTt = {"chua_nhan", "ha_quai", "hanh_dong", "du_tra", "npc_xa"};
        for (int c = 0; c < ChupTrongGame.CO.length; c++) {
            final int W = ChupTrongGame.CO[c][0], H = ChupTrongGame.CO[c][1], dpi = ChupTrongGame.CO[c][2];
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            float s = NenPc.hienW / (float) NenPc.rong;
            int min, cach;
            if (dpi > 0) {
                float dpL = (dpi / 160f) / s;
                min = (int) Math.ceil(44 * dpL);
                cach = (int) Math.floor(8 * dpL);
                game.pokemon.Bang.anTrai = (int) Math.ceil(100 / s);
            } else {
                min = (int) Math.ceil(32 / s);
                cach = (int) Math.ceil(8 / s);
                game.pokemon.Bang.anTrai = 0;
            }
            game.pokemon.VungCham.MIN = min;
            game.pokemon.VungCham.CACH = cach;
            String co = ChupTrongGame.TEN_CO[c];
            ChupTrongGame.ve(W, H);   // khung khởi động: nhãn 12+ ghi đáy của cỡ mới
            for (int tt = 0; tt < tenTt.length; tt++) {
                final int t = tt;
                ChupTrongGame.chay(() -> {
                    try {
                        game.render.Canvas.endDlg();
                        game.render.Canvas.menu.showMenu = false;
                        game.render.Canvas.gameScr.switchToMe();
                        datTrangThai(t);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                BufferedImage img = ChupTrongGame.ve(W, H);
                ImageIO.write(img, "png", new File(ra, "the_nv_" + co + "_" + tenTt[tt] + ".png"));
                kiemKhung(co + "/" + tenTt[tt], tt, min, cach);
            }
            // nhân vật góc dưới trái bản đồ, Xatu (53,10) ngoài màn: mũi tên quanh nhân vật chỉ về Xatu
            ChupTrongGame.chay(() -> {
                try {
                    datTrangThai(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
                mc.x = 300;
                mc.y = 760;
                game.render.screen.GameScr.cmx = mc.x - game.render.Canvas.hw;
                game.render.screen.GameScr.cmy = mc.y - game.render.Canvas.hh;
            });
            ImageIO.write(ChupTrongGame.ve(W, H), "png", new File(ra, "the_nv_" + co + "_mui_ten.png"));
            kiemKhung(co + "/mui_ten", 0, min, cach);
            if (game.pokemon.TheNhiemVu.muiTenVuaVe != null) coMuiTen++;
            ChupTrongGame.chay(() -> {
                game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
                mc.x = 200;
                mc.y = 200;
            });
            // cấp cao: không hiện; hộp thoại mở: không hiện
            ChupTrongGame.chay(() -> {
                try {
                    datTrangThai(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                game.render.Canvas.gameScr.mainChar.level = (byte) (game.pokemon.TheNhiemVu.CAP_HIEN + 1);
            });
            ChupTrongGame.ve(W, H);
            kiem(game.pokemon.TheNhiemVu.vuaVe == null, co + " cap " + (game.pokemon.TheNhiemVu.CAP_HIEN + 1) + ": khong hien the");
            ChupTrongGame.chay(() -> {
                game.render.Canvas.gameScr.mainChar.level = 3;
                game.render.Canvas.startYesNoDlg("Hộp thoại thử", new game.model.IAction() {
                    public void perform() {
                        game.render.Canvas.endDlg();
                    }
                });
            });
            ChupTrongGame.ve(W, H);
            kiem(game.pokemon.TheNhiemVu.vuaVe == null, co + " hop thoai mo: khong hien the");
            ChupTrongGame.chay(game.render.Canvas::endDlg);
            // chạm thân thẻ -> sổ nhiệm vụ
            ChupTrongGame.ve(W, H);
            int i = vung("nv-the");
            if (i >= 0) {
                final int[] v = game.pokemon.VungCham.vungVe(i);
                ChupTrongGame.chay(() -> {
                    game.render.Canvas.pxLast = v[0] + v[2] / 2;
                    game.render.Canvas.pyLast = v[1] + v[3] / 2;
                    game.render.Canvas.px = v[0] + v[2] / 2;
                    game.render.Canvas.py = v[1] + v[3] / 2;
                    game.render.Canvas.isPointerClick = true;
                    game.pokemon.VungCham.xuLy();
                });
                kiem(game.render.Canvas.currentScreen == game.pokemon.ManNhiemVu.me && game.pokemon.ManNhiemVu.me != null, co + " cham than the mo so nhiem vu");
                ChupTrongGame.chay(() -> game.render.Canvas.gameScr.switchToMe());
            } else {
                kiem(false, co + " khong co vung nv-the de cham");
            }
        }
        kiem(coMuiTen >= 3, "mui ten chi huong ve that o >= 3/11 co (" + coMuiTen + ")");
        System.out.println("KiemTheNhiemVu: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
