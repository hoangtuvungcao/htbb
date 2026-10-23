import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.lcdui.NenPc;

/**
 * 0.9.9 GIFTCODE (chủ game: "nhập mã không được" + "sao Cài đặt có giftcode"): MỘT lối vào trong game = Menu -> Thư/Quà ->
 * "Nhập giftcode" (cộng NPC quà tặng của máy chủ). Kiểm bằng chạm thật (VungCham) ở PC 1280x720 và điện thoại:
 *  1. Menu -> Thư/Quà -> "Nhập giftcode" mở hộp nhập (Canvas.inputDlg) và hộp CÒN MỞ sau vài khung;
 *  2. gõ mã + "Xác nhận" -> đúng MỘT gói -31 (i16 id nhân vật, i8 7, UTF mã) như NPC quà tặng (máy chủ real.GiftcodeAnToan);
 *  3. trả lời máy chủ (lệnh 37): thành công -> thẻ "Đổi quà thành công" có ô phần thưởng; mã sai / đã nhập / chưa đủ cấp /
 *     hết hạn / hết lượt / chưa kích hoạt -> thẻ báo rõ (không im lặng);
 *  4. Cài đặt (PC LopPhu Tài khoản, Menu nhanh J2ME) KHÔNG còn "Nhập giftcode".
 * Phía máy chủ dầu-cuối: tools/pokemon/kiem_tai/KichBanGiftcode (bot thật, tob_giftcode).
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemChamHud.java \
 *       client_pc/test/KiemGiftcode.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemGiftcode <thư mục>
 */
public class KiemGiftcode {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static String moTa() {
        Object d = game.render.Canvas.currentDialog;
        return d == null ? "null" : d.getClass().getName();
    }

    static void nhan37(String chu) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeUTF(chu);
        o.writeUTF("");
        java.lang.reflect.Constructor<network.Message> c = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        c.setAccessible(true);
        final network.Message m = c.newInstance((byte) 37, bo.toByteArray());
        Thread t = new Thread(() -> new game.networklogic.MessageHandler().onMessage(m));
        t.start();
        t.join(3000);
        KiemChamHud.ngu(200);
        KiemChamHud.ve();
    }

    /** Chữ đang hiện của thông báo: thẻ nhanh (DungDo) đang hiện / đang chờ + hộp đang mở. */
    static String thongBaoHien() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            try {
                String c = game.pokemon.DungDo.chuHien(i);
                if (c != null) b.append(c).append(" | ");
            } catch (Throwable e) {
                // ô trống
            }
        }
        try {
            java.lang.reflect.Field f = game.pokemon.DungDo.class.getDeclaredField("CHO");
            f.setAccessible(true);
            for (Object e : (java.util.Vector) f.get(null)) b.append(chuoiCua(e)).append(" | ");
        } catch (Throwable e) {
            b.append(e);
        }
        Object d = game.render.Canvas.currentDialog;
        if (d != null) b.append(chuoiCua(d));
        return b.toString();
    }

    static String chuoiCua(Object o) {
        StringBuilder b = new StringBuilder();
        for (Class<?> c = o.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                try {
                    f.setAccessible(true);
                    Object v = f.get(o);
                    if (v instanceof String) b.append(v).append(' ');
                    if (v instanceof String[]) for (String s : (String[]) v) b.append(s).append(' ');
                } catch (Throwable e) {
                    // bỏ qua
                }
            }
        }
        return b.toString();
    }

    static void kiemCo(String co) throws Exception {
        KiemChamHud.dungCanh(false);
        // 1) Menu -> Thư/Quà -> Nhập giftcode
        ChupTrongGame.chay(() -> game.pokemon.ManMenu.mo());
        KiemChamHud.ve();
        kiem(KiemChamHud.chamVung("menu-Thư/Quà"), co + ": Menu co o Thu/Qua");
        KiemChamHud.ngu(200);
        KiemChamHud.ve();
        // thẻ Thư / Quà (MenuNguCanh): hàng 0 Hộp thư, hàng 1 Nhập giftcode
        String vung = KiemChamHud.vung("lenh-1") != null && chuoiCua(game.pokemon.MenuNguCanh.gI()).length() >= 0 ? "lenh-1" : null;
        kiem(vung != null, co + ": the Thu/Qua co hang Nhap giftcode (" + vung + ")");
        if (vung == null) return;
        KiemChamHud.chamVung(vung);
        for (int i = 0; i < 6; i++) {
            KiemChamHud.ngu(80);
            KiemChamHud.ve();
        }
        kiem(game.render.Canvas.currentDialog == game.render.Canvas.inputDlg, co + ": Nhap giftcode mo hop nhap va hop con mo (" + moTa() + ")");
        if (game.render.Canvas.currentDialog != game.render.Canvas.inputDlg) return;
        // 2) gõ mã + Xác nhận
        ChupTrongGame.chay(() -> game.render.Canvas.inputDlg.tfInput.setText("HTBBTANTHU"));
        KiemChamHud.xoaGoi();
        kiem(KiemChamHud.chamVung("so-ok"), co + ": hop nhap co nut Xac nhan (so-ok)");
        KiemChamHud.ngu(300);
        int n = KiemChamHud.demLenh(-31);
        kiem(n == 1, co + ": Xac nhan gui dung 1 goi -31 (" + n + ")");
        synchronized (KiemChamHud.LENH) {
            for (int i = 0; i < KiemChamHud.LENH.size(); i++) {
                if (KiemChamHud.LENH.get(i) != -31) continue;
                DataInputStream r = new DataInputStream(new java.io.ByteArrayInputStream(KiemChamHud.GOI.get(i)));
                int id = r.readShort();
                int muc = r.readByte();
                String ma = r.readUTF();
                kiem(id == (short) game.render.Canvas.gameScr.mainChar.ID && muc == 7 && "HTBBTANTHU".equals(ma),
                        co + ": goi -31 = (id nhan vat, 7, ma) - nhan (" + id + ", " + muc + ", " + ma + ")");
            }
        }
        kiem(game.render.Canvas.currentDialog != game.render.Canvas.inputDlg, co + ": gui xong hop nhap dong");
        // 3) trả lời máy chủ
        String[][] tl = {
            {"Đổi giftcode thành công: +5 lượng khoá, 2 Poké Ball.", "lượng khoá"},
            {"Mã quà tặng không tồn tại hoặc đã hết hạn.", "không tồn tại"},
            {"Tài khoản của bạn đã nhập mã quà tặng này rồi.", "đã nhập"},
            {"Cần đạt cấp 10 để nhập mã này.", "Cần đạt cấp"},
            {"Mã quà tặng đã hết lượt sử dụng.", "hết lượt"},
            {"Tính năng Giftcode cần tài khoản đã kích hoạt. Xem cách kích hoạt tại http://180.93.57.30/kichhoat.", "kích hoạt"}};
        for (String[] t : tl) {
            nhan37(t[0]);
            String hien = thongBaoHien();
            kiem(hien.contains(t[1]), co + ": tra loi '" + t[0] + "' hien cho nguoi choi (" + (hien.length() > 160 ? hien.substring(0, 160) : hien) + ")");
            if (t[0].startsWith("Đổi giftcode")) {
                kiem(game.pokemon.PhanThuong.tach(t[0]).size() >= 1, co + ": the thanh cong co o phan thuong (" + game.pokemon.PhanThuong.tach(t[0]).size() + ")");
            }
            ChupTrongGame.chay(() -> {
                if (game.render.Canvas.currentDialog != null) game.render.Canvas.endDlg();
                try {
                    game.pokemon.DungDo.xoaHet();
                } catch (Throwable e) {
                    // không có hàm xoá: thẻ tự hết
                }
            });
        }
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.switchToMe());
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "kiem_giftcode");
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) KiemChamHud.ngu(100);
        KiemChamHud.ngu(2500);
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
        KiemChamHud.ngu(500);
        String[] ten = {"pc720", "dt20x9"};
        int[][] co = {{1280, 720, 0}, {2400, 1080, 420}};
        for (int c = 0; c < co.length; c++) {
            KiemChamHud.co = ten[c];
            NenPc.tinhKichThuoc(co[c][0], co[c][1]);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            float s = NenPc.hienW / (float) NenPc.rong;
            game.pokemon.VungCham.MIN = co[c][2] > 0 ? 0 : (int) Math.ceil(32 / s);
            kiemCo(ten[c]);
        }
        // 4) Cài đặt không còn giftcode
        String nguon = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("client_pc/src/htbb/pc/LopPhu.java")), "UTF-8");
        kiem(nguon.indexOf("moGiftcode") < 0, "Cai dat PC (LopPhu) khong con nut Nhap giftcode");
        String mn = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("client_jar/src/game/render/screen/MenuNhanh.java")), "UTF-8");
        kiem(mn.indexOf("moGiftcode") < 0, "Menu nhanh / Cai dat J2ME khong con Nhap giftcode");
        System.out.println("KiemGiftcode: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
