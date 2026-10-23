import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM MỤC TIÊU SỔ NHIỆM VỤ (0.9.15, nhánh sua-tien-do-nv): máy chủ >= này gửi ĐUÔI gói -64 (client >= 915) - mỗi nhiệm
 * vụ một câu mục tiêu, nơi trả, gợi ý khoá, cờ (cửa ải / bản đồ khoá). Ở 11 cỡ của ChupTrongGame (+ 240x320; chạy thêm
 * -Dchup.j2me=1 = vẽ như J2ME) đưa gói -64 THẬT (MessageHandler -> GameScr.onQuest) rồi kiểm chữ ĐÃ VẼ (GhiChu):
 * <ul>
 *   <li>hàng "Đang làm": tên · loại + x/y, dòng 2 "Hạ 20 Weedle ở Lavender · Trả: Machamp (Pewter)" đủ từng chữ (xuống
 *       dòng, không cắt "…"), dòng khoá đỏ, hàng xong "Xong! Về trả Kecleon (Pewter)", cửa ải ở ô đội ghi "Cửa ải";</li>
 *   <li>"Có thể nhận": mục tiêu + "Nhận ở";</li>
 *   <li>thẻ chi tiết: "Mục tiêu: ...", "Trả: ...", gợi ý khoá; cửa ải không có nút Huỷ, nút phải "Mở Nhiệm vụ của tôi"
 *       gửi -30 (NPC ảo -527, menu 0, mục 8);</li>
 *   <li>NPC Xatu (loại 5): menu có "Nhiệm vụ của tôi"; máy chủ cũ (gói không đuôi): đọc đúng, không có lối mới.</li>
 * </ul>
 *   APP_IMAGE=0 sh client_pc/build.sh && O=$(mktemp -d)
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemMucTieuNhiemVu.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemMucTieuNhiemVu <thư mục ảnh>
 * Mã thoát 1 = có phép kiểm hỏng.
 */
public class KiemMucTieuNhiemVu {
    static int dat, hong;
    static final List<Integer> GUI_LENH = new ArrayList<Integer>();
    static final List<byte[]> GUI = new ArrayList<byte[]>();

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    // ------------------------------------------------------------------ gói -64 (MessageCreator.createMsgCharQuest + đuôi)

    static void duoi(DataOutputStream o, String[][] d) throws Exception {
        o.writeByte(d.length);
        for (int i = 0; i < d.length; i++) {
            o.writeUTF(d[i][0]);
            o.writeUTF(d[i][1]);
            o.writeUTF(d[i][2]);
            o.writeByte(Integer.parseInt(d[i][3]));
        }
    }

    /** Đang làm: 3 ô (chính thu thập ĐỦ 5/5, phụ hạ Weedle 3/20 + khoá, cửa ải ở ô đội 7/20). */
    static byte[] dangLam(boolean coDuoi) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeByte(2);
        o.writeByte(3);
        // chính: thu thập 5/5 (xong, chờ trả)
        o.writeShort(14);
        o.writeByte(0);
        o.writeUTF("Gom hàng rơi");
        o.writeByte(2);
        o.writeUTF("Thu 5 Gói hàng (rơi từ Litwick) ở Cerulean");
        o.writeByte(1);
        o.writeByte(0);
        o.writeUTF("Phần thưởng:|13.341 EXP, 1.294 xu");
        o.writeByte(1);
        o.writeShort(1);
        o.writeShort(5);
        o.writeShort(5);
        // phụ: hạ Weedle 3/20
        o.writeShort(44);
        o.writeByte(1);
        o.writeUTF("Chứng tỏ sức mạnh");
        o.writeByte(0);
        o.writeUTF("Hạ 20 Weedle ở Pewter");
        o.writeByte(2);
        o.writeByte(0);
        o.writeUTF("Phần thưởng:|41.984 EXP, 1.249 xu");
        o.writeByte(1);
        o.writeShort(2);
        o.writeShort(3);
        o.writeShort(20);
        // cửa ải (ô đội): hạ Torchic 7/20
        o.writeShort(30000);
        o.writeByte(2);
        o.writeUTF("Cửa ải: 20 Torchic ở Pallet");
        o.writeByte(0);
        o.writeUTF("Hạ 20 Torchic ở Pallet");
        o.writeByte(-1);
        o.writeByte(0);
        o.writeUTF("Phần thưởng:|3.161 EXP, 384 xu");
        o.writeByte(1);
        o.writeShort(3);
        o.writeShort(7);
        o.writeShort(20);
        if (coDuoi) {
            duoi(o, new String[][]{
                {"Hạ Litwick ở Cerulean, nhặt 5 Gói hàng", "Kecleon (Pewter)", "", "0"},
                {"Hạ 20 Weedle ở Lavender", "Machamp (Pewter)", "Lavender đang khoá: cần cửa ải «Vượt hồ Vermilion» (Sổ tay)", "2"},
                {"Hạ 20 Torchic ở Pallet", "", "", "1"}});
        }
        return b.toByteArray();
    }

    static byte[] coTheNhan(boolean coDuoi) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeByte(0);
        o.writeByte(1);
        o.writeShort(15);
        o.writeByte(0);
        o.writeUTF("Thùng gỗ lạ");
        o.writeByte(1);
        o.writeUTF("1Kecleon: Còn một thùng gỗ nặng nữa...");
        o.writeByte(3);
        o.writeByte(0);
        if (coDuoi) {
            duoi(o, new String[][]{{"Nói chuyện với Kecleon ở Pewter", "Kecleon (Pewter)", "", "0"}});
        }
        return b.toByteArray();
    }

    static byte[] rong(int nhom, boolean coDuoi) {
        return coDuoi ? new byte[]{(byte) nhom, 0, 0} : new byte[]{(byte) nhom, 0};
    }

    static void datDanhSach(boolean coDuoi) throws Exception {
        ChupTrongGame.goi(-64, rong(1, coDuoi));
        ChupTrongGame.goi(-64, coTheNhan(coDuoi));
        ChupTrongGame.goi(-64, dangLam(coDuoi));
    }

    // ------------------------------------------------------------------ chữ đã vẽ

    static List<String> chuDaVe() {
        List<String> l = new ArrayList<String>();
        for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
            if (game.pokemon.GhiChu.CS[i] != null) l.add(game.pokemon.GhiChu.CS[i].replace('-', '·').replace('"', ' '));
        }
        return l;
    }

    static Set<String> tu(List<String> l) {
        Set<String> s = new HashSet<String>();
        for (String x : l) for (String t : x.replace('«', ' ').replace('»', ' ').split("[\\s·]+")) if (t.length() > 0) s.add(t);
        return s;
    }

    /** Mọi chữ (từ) của câu đều được vẽ trọn (không bị "…" cắt mất). */
    static void duTu(String nhan, List<String> ve, String cau) {
        Set<String> s = tu(ve);
        StringBuilder thieu = new StringBuilder();
        for (String t : cau.replace('«', ' ').replace('»', ' ').split("[\\s·]+")) {
            if (t.length() > 0 && !s.contains(t)) thieu.append(" '").append(t).append("'");
        }
        kiem(thieu.length() == 0, nhan + " ve du chu \"" + cau + "\" (thieu:" + thieu + ")");
    }

    static boolean coChua(List<String> ve, String s) {
        for (String x : ve) if (x.contains(s)) return true;
        return false;
    }

    static BufferedImage veGhi(int W, int H) throws Exception {
        game.pokemon.GhiChu.xoa();
        game.pokemon.GhiChu.bat = true;
        game.render.screen.Font.thieuNet = 0;
        BufferedImage a = ChupTrongGame.ve(W, H);
        game.pokemon.GhiChu.bat = false;
        return a;
    }

    static int vung(String ten) {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) if (ten.equals(game.pokemon.VungCham.ten(i))) return i;
        return -1;
    }

    static void cham(String ten) throws Exception {
        int i = vung(ten);
        if (i < 0) return;
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

    static boolean daGuiSoTay() {
        synchronized (GUI) {
            for (int i = 0; i < GUI.size(); i++) {
                byte[] b = GUI.get(i);
                if (GUI_LENH.get(i) == -30 && b.length >= 4 && (short) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF)) == -527 && b[2] == 0 && b[3] == 8) return true;
            }
        }
        return false;
    }

    static void xoaGui() {
        synchronized (GUI) {
            GUI.clear();
            GUI_LENH.clear();
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "kiem_muc_tieu_nhiem_vu");
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
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
                synchronized (GUI) {
                    GUI_LENH.add((int) m.command);
                    GUI.add(m.getData());
                }
            }

            public void close() {
            }
        });
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.render.Canvas.endDlg();
            String[] ten = new String[20];
            for (int i = 0; i < ten.length; i++) ten[i] = "Vật phẩm " + i;
            ten[1] = "Gói hàng";
            game.model.ItemQuest.NAME_ITEM = ten;
            try {
                game.render.Res.monsterTemplates[2].name = "Weedle";
                game.render.Res.monsterTemplates[3].name = "Torchic";
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        // ---- máy chủ cũ: gói không đuôi đọc đúng, không lối mới
        game.render.screen.GameScr.coMucTieuNv = false;
        ChupTrongGame.chay(() -> {
            try {
                datDanhSach(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        game.model.Quest sq = game.render.screen.GameScr.subQuest;
        kiem(sq != null && "Chứng tỏ sức mạnh".equals(sq.name) && sq.totalMonskill != null && sq.totalMonskill[0] == 20 && !sq.coMuc(),
                "may chu cu: doc goi khong duoi (phu = " + (sq == null ? null : sq.name) + ")");
        kiem(game.render.screen.GameScr.clanQuest != null && game.render.screen.GameScr.allQuestCanReceive.size() == 1, "may chu cu: du o doi + co the nhan");
        kiem(!game.render.screen.GameScr.coMucTieuNv, "may chu cu: khong bat loi 'Nhiem vu cua toi'");

        // ---- máy chủ mới
        ChupTrongGame.chay(() -> {
            try {
                datDanhSach(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        sq = game.render.screen.GameScr.subQuest;
        kiem(game.render.screen.GameScr.coMucTieuNv, "may chu moi: bat co muc tieu");
        kiem(sq != null && "Hạ 20 Weedle ở Lavender".equals(sq.muc) && "Machamp (Pewter)".equals(sq.tra) && sq.khoa.startsWith("Lavender") && sq.co == 2,
                "may chu moi: doc duoi phu (" + (sq == null ? null : sq.muc + " | " + sq.tra + " | " + sq.khoa + " | " + sq.co) + ")");
        game.model.Quest cq = game.render.screen.GameScr.clanQuest;
        kiem(cq != null && cq.laCuaAi(), "may chu moi: cua ai o o doi co co");
        kiem(((game.model.Quest) game.render.screen.GameScr.allQuestCanReceive.elementAt(0)).muc.startsWith("Nói chuyện"), "may chu moi: duoi co the nhan");

        int[][] co = new int[ChupTrongGame.CO.length + 1][];
        String[] tenCo = new String[co.length];
        for (int i = 0; i < ChupTrongGame.CO.length; i++) {
            co[i] = ChupTrongGame.CO[i];
            tenCo[i] = ChupTrongGame.TEN_CO[i];
        }
        co[co.length - 1] = new int[]{240, 320, 0};
        tenCo[co.length - 1] = "j2me240x320";
        String hau = j2me ? "_j2me" : "";
        for (int c = 0; c < co.length; c++) {
            final int W = co[c][0], H = co[c][1], dpi = co[c][2];
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
            String n = tenCo[c] + hau;
            // tab Đang làm
            ChupTrongGame.chay(() -> {
                try {
                    game.render.Canvas.endDlg();
                    game.render.Canvas.menu.showMenu = false;
                    game.render.Canvas.gameScr.switchToMe();
                    datDanhSach(true);
                    game.pokemon.ManNhiemVu.mo();
                    ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "tab", Integer.valueOf(0));
                    ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "chon", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            ChupTrongGame.ve(W, H);
            BufferedImage img = veGhi(W, H);
            ImageIO.write(img, "png", new File(ra, "muc_tieu_" + n + "_dang_lam.png"));
            List<String> ve = chuDaVe();
            int[] dauHang = new int[3];
            for (int i = 0; i < 3; i++) dauHang[i] = vung("nv-" + i);
            // cuộn xuống cuối (máy thấp: hàng cửa ải nằm dưới) - chữ cả hai lần vẽ
            ChupTrongGame.chay(() -> {
                try {
                    java.lang.reflect.Field f = game.pokemon.ManNhiemVu.class.getDeclaredField("ds");
                    f.setAccessible(true);
                    ((game.pokemon.Cuon) f.get(game.pokemon.ManNhiemVu.gI())).den(100000);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            ChupTrongGame.ve(W, H);
            BufferedImage img2 = veGhi(W, H);
            ImageIO.write(img2, "png", new File(ra, "muc_tieu_" + n + "_dang_lam_cuoi.png"));
            ve.addAll(chuDaVe());
            for (int i = 0; i < 3; i++) if (dauHang[i] < 0) dauHang[i] = vung("nv-" + i);
            boolean hai = game.render.Canvas.w >= 400 + 40;   // hai cột: thẻ chi tiết nhiệm vụ đầu cũng vẽ
            duTu(n + " dang lam", ve, "Hạ 20 Weedle ở Lavender · Trả: Machamp (Pewter)");
            duTu(n + " dang lam", ve, "Xong! Về trả Kecleon (Pewter)");
            duTu(n + " dang lam", ve, "Lavender đang khoá: cần cửa ải «Vượt hồ Vermilion» (Sổ tay)");
            duTu(n + " dang lam", ve, "Hạ 20 Torchic ở Pallet · Nhận thưởng ở Sổ tay");
            kiem(coChua(ve, "Cửa ải"), n + " o doi ghi 'Cua ai'");
            kiem(coChua(ve, "3/20") && coChua(ve, "7/20") && coChua(ve, "Xong"), n + " tien do x/y + Xong");
            kiem(vung("nv-so-tay") >= 0, n + " nut 'Nhiem vu cua toi' trong so");
            int iSo = vung("nv-so-tay");
            if (iSo >= 0) {
                int[] v = game.pokemon.VungCham.vungVe(iSo);
                kiem(v[3] >= min && v[1] + v[3] <= game.render.Canvas.h, n + " nut so tay du co cham, trong man " + java.util.Arrays.toString(v));
            }
            for (int i = 0; i < 3; i++) {
                kiem(dauHang[i] >= 0, n + " hang " + i + " cham duoc");
            }
            ChupTrongGame.kiemChu(n + " dang lam");
            ChupTrongGame.kiemTrongMan(n + " dang lam");
            // chạm nút sổ tay -> -30 (-527, 0, 8)
            xoaGui();
            cham("nv-so-tay");
            kiem(daGuiSoTay(), n + " cham 'Nhiem vu cua toi' gui -30 (-527,0,8)");
            // thẻ chi tiết phụ
            ChupTrongGame.chay(() -> {
                game.render.Canvas.endDlg();
                game.pokemon.ManNhiemVu.mo();
                ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "tab", Integer.valueOf(0));
                ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "chon", game.render.screen.GameScr.subQuest);
            });
            img = veGhi(W, H);
            ImageIO.write(img, "png", new File(ra, "muc_tieu_" + n + "_chi_tiet.png"));
            ve = chuDaVe();
            duTu(n + " chi tiet", ve, "Mục tiêu: Hạ 20 Weedle ở Lavender");
            duTu(n + " chi tiet", ve, "Trả: Machamp (Pewter)");
            kiem(coChua(ve, "đang khoá"), n + " chi tiet goi y khoa");
            kiem(vung("huy-nv") >= 0, n + " chi tiet phu co Huy");
            ChupTrongGame.kiemChu(n + " chi tiet");
            // thẻ chi tiết cửa ải: không Huỷ, nút mở sổ tay
            ChupTrongGame.chay(() -> ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "chon", game.render.screen.GameScr.clanQuest));
            img = veGhi(W, H);
            ImageIO.write(img, "png", new File(ra, "muc_tieu_" + n + "_cua_ai.png"));
            ve = chuDaVe();
            kiem(vung("huy-nv") < 0, n + " cua ai khong co Huy");
            kiem(coChua(ve, "Mở Nhiệm vụ của tôi"), n + " cua ai nut 'Mo Nhiem vu cua toi'");
            xoaGui();
            cham("di-toi");
            kiem(daGuiSoTay(), n + " cua ai cham nut gui -30 so tay");
            // tab Có thể nhận
            ChupTrongGame.chay(() -> {
                game.render.Canvas.endDlg();
                game.pokemon.ManNhiemVu.mo();
                ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "tab", Integer.valueOf(1));
                ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "chon", null);
            });
            ChupTrongGame.ve(W, H);
            img = veGhi(W, H);
            ImageIO.write(img, "png", new File(ra, "muc_tieu_" + n + "_co_the_nhan.png"));
            ve = chuDaVe();
            duTu(n + " co the nhan", ve, "Nói chuyện với Kecleon ở Pewter · Nhận ở");
            ChupTrongGame.kiemChu(n + " co the nhan");
            ChupTrongGame.kiemTrongMan(n + " co the nhan");
            if (hai) {
                kiem(true, n + " hai cot");
            }
        }

        // ---- NPC Xatu (loại 5): menu có "Nhiệm vụ của tôi" -> -30
        xoaGui();
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.render.Canvas.gameScr.switchToMe();
            game.render.Canvas.menu.showMenu = false;
            game.render.Canvas.gameScr.moChucNangNpc(5);
        });
        boolean coMuc = false;
        try {
            java.lang.reflect.Field f = game.render.Canvas.menu.getClass().getDeclaredField("menuItems");
            f.setAccessible(true);
            java.util.Vector v = (java.util.Vector) f.get(game.render.Canvas.menu);
            for (int i = 0; v != null && i < v.size(); i++) {
                game.model.Command cm = (game.model.Command) v.elementAt(i);
                if ("Nhiệm vụ của tôi".equals(cm.caption)) {
                    coMuc = true;
                    final game.model.Command chon = cm;
                    ChupTrongGame.chay(() -> chon.action.perform());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        kiem(game.render.Canvas.menu.showMenu || coMuc, "Xatu: mo menu");
        kiem(coMuc, "Xatu: menu co 'Nhiem vu cua toi'");
        kiem(daGuiSoTay(), "Xatu: chon gui -30 so tay");

        dat += ChupTrongGame.dat;   // kiemChu / kiemTrongMan đếm ở ChupTrongGame
        hong += ChupTrongGame.hong;
        System.out.println("KiemMucTieuNhiemVu" + hau + ": " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
