import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM BẢNG CHAT (0.9.8, game.pokemon.ManChat + KenhChat, gói -124) - headless, máy chủ giả (gói qua MessageHandler thật):
 *  - 11 cỡ (điện thoại 16:9 -> 21:9, máy tính bảng, PC) + J2ME 320x240 / 240x320: mỗi tab (Bản đồ, Thế giới, Bang hội,
 *    Đội, Bạn bè danh sách + hội thoại, Hệ thống, Tuỳ chọn) - tấm canh giữa / <= 80 % / trong vùng an toàn, ô nhập + nút
 *    Gửi trong tấm, dưới danh sách, không chồng nhau, mọi vùng chạm đủ cỡ / không chồng (ChupTrongGame.kiemCham), chấm đỏ
 *    tab có tin chưa đọc;
 *  - thao tác (pc720, dt20x9): chạm tab, chạm dòng -> menu tên (Nhắn riêng / Mời vào đội / Kết bạn / Xem thông tin / Chặn),
 *    Nhắn riêng -> hội thoại, gõ + Gửi -> gói -124 đúng kênh / người nhận, kênh Bản đồ -> gói 27, Thế giới đang chờ ->
 *    nút Gửi tắt + đếm ngược, Mời vào đội -> tra id (-124 op 6) -> gói 49, chặn -> gói -124 op 2 + tin người bị chặn ẩn;
 *  - HUD: tin riêng chưa đọc -> chấm đỏ nút chat, chạm nút chat -> bảng mở lại tab cuối, dải chat HUD theo kênh bật /
 *    tắt, gói 27 của người khác vào tab Bản đồ, máy chủ cũ (không gói cấu hình) vẫn gửi bằng gói cũ;
 *  - J2ME bấm phím: phải / trái đổi tab, xuống chọn dòng, 5 -> menu tên, phím số gõ, 5 gửi, phím mềm phải lùi / đóng.
 *
 *   APP_IMAGE=0 BAN=908 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java \
 *     client_pc/test/KiemKenhChat.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemKenhChat [thư mục ảnh]
 * Mã thoát 1 = có lỗi.
 */
public class KiemKenhChat {
    static int dat, hong;
    static String co = "";
    static File ra;
    static final List<Integer> LENH = new ArrayList<Integer>();
    static final List<byte[]> GOI = new ArrayList<byte[]>();

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

    static void chay(Runnable r) throws Exception {
        ChupTrongGame.chay(r);
    }

    static void ve() throws Exception {
        ChupTrongGame.chay(ChupTrongGame::veThang);
    }

    static void goiCanvas(String ten, int x, int y) {
        try {
            java.lang.reflect.Method m = game.render.Canvas.class.getDeclaredMethod(ten, int.class, int.class);
            m.setAccessible(true);
            m.invoke(game.render.Canvas.instance, x, y);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static void cham(final int x, final int y) throws Exception {
        ve();
        chay(() -> goiCanvas("pointerPressed", x, y));
        ngu(90);
        chay(() -> goiCanvas("pointerReleased", x, y));
        ngu(250);
        ve();
    }

    static int[] vung(String ten) {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) return game.pokemon.VungCham.vungVe(i);
        }
        return null;
    }

    static boolean chamVung(String ten) throws Exception {
        ve();
        int[] v = vung(ten);
        if (v == null) return false;
        cham(v[0] + v[2] / 2, v[1] + v[3] / 2);
        return true;
    }

    static void phim(final int k) throws Exception {
        chay(() -> game.render.Canvas.instance.keyPressed(k));
        ngu(120);
        chay(() -> game.render.Canvas.instance.keyReleased(k));
        ngu(120);
    }

    static void xoaGoi() {
        synchronized (LENH) {
            LENH.clear();
            GOI.clear();
        }
    }

    /** gói cuối có lệnh lenh (null = không). */
    static byte[] goiCuoi(int lenh) {
        synchronized (LENH) {
            for (int i = LENH.size() - 1; i >= 0; i--) {
                if (LENH.get(i) == lenh) return GOI.get(i);
            }
        }
        return null;
    }

    static java.io.DataInputStream doc(byte[] b) {
        return new java.io.DataInputStream(new java.io.ByteArrayInputStream(b));
    }

    // ------------------------------------------------------------------ máy chủ giả: gói -124

    interface Viet {
        void viet(DataOutputStream o) throws Exception;
    }

    static void mayChu(Viet v) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        v.viet(o);
        final byte[] du = b.toByteArray();
        chay(() -> ChupTrongGame.goi(-124, du));
    }

    static void cauHinh() throws Exception {
        mayChu(o -> {
            o.writeByte(0);
            o.writeByte(1);
            String[] ten = {"Bản đồ", "Thế giới", "Đội", "Nhóm", "Bạn bè", "Hệ thống"};
            o.writeByte(6);
            for (int i = 0; i < 6; i++) {
                o.writeByte(i);
                o.writeUTF(ten[i]);
                o.writeByte(i == 5 ? 0 : 1);
                o.writeShort(i == 1 ? 10 : i == 0 ? 5 : 0);
                o.writeInt(i == 1 ? 100 : 0);
                o.writeShort(i == 1 ? 30 : 0);
            }
            o.writeShort(80);
        });
    }

    static void tin(int kenh, int coTin, String ten, String doiPhuong, String noiDung, int id) throws Exception {
        mayChu(o -> {
            o.writeByte(1);
            o.writeByte(kenh);
            o.writeByte(coTin);
            o.writeLong(System.currentTimeMillis());
            o.writeShort(id);
            o.writeUTF(ten);
            o.writeUTF(doiPhuong);
            o.writeUTF(noiDung);
            o.writeUTF(kenh == 2 ? "(PB)" : "");
        });
    }

    static void banBe() throws Exception {
        mayChu(o -> {
            o.writeByte(2);
            o.writeShort(3);
            o.writeUTF("Pikachu7");
            o.writeByte(1);
            o.writeShort(31);
            o.writeUTF("Eevee2");
            o.writeByte(0);
            o.writeShort(0);
            o.writeUTF("Bulbasaur1");
            o.writeByte(1);
            o.writeShort(12);
        });
    }

    static void ketQua(int kenh, int ma, int giay, String lyDo) throws Exception {
        mayChu(o -> {
            o.writeByte(4);
            o.writeByte(kenh);
            o.writeByte(ma);
            o.writeShort(giay);
            o.writeUTF(lyDo);
        });
    }

    static void napDuLieu() throws Exception {
        chay(game.pokemon.KenhChat::xoaHet);
        cauHinh();
        banBe();
        tin(0, 0, "Squirtle3", "", "Có ai đi săn Pidgey ở Rừng Viridian không?", 21);
        tin(0, 1, "Charmander99", "", "Mình đi cùng nhé!", 1);
        String[] tg = {"Bán Great Ball giá rẻ, ai cần nhắn mình", "Boss Mewtwo xuất hiện ở Hang Cerulean!",
            "Bang hội Lửa Thiêng tuyển thành viên cấp 20 trở lên, hoạt động buổi tối, có hỗ trợ người mới rất nhiệt tình",
            "chào cả nhà", "Ai biết chỗ câu cá Magikarp shiny không?"};
        for (int i = 0; i < tg.length; i++) tin(1, i == 2 ? 8 : 0, i % 2 == 0 ? "Gengar" + i : "Onix" + i, "", tg[i], 30 + i);
        tin(2, 0, "Bulbasaur1", "", "Tối nay 21h đánh chiếm Gym nhé cả bang", 12);
        tin(3, 0, "Pikachu7", "", "Gom quái lại rồi mình dùng chiêu diện rộng", 7);
        tin(4, 0, "Pikachu7", "Pikachu7", "Cậu ơi chiều nay đi phó bản không?", 7);
        tin(4, 2, "Eevee2", "Eevee2", "Hôm qua cậu offline mình để lại lời nhắn nè", -1);
        chay(() -> game.pokemon.KenhChat.heThong("Bạn nhận được 120 xu."));
    }

    // ------------------------------------------------------------------ bố cục một cỡ

    static boolean trong(int[] a, int[] k) {
        return a[0] >= k[0] && a[1] >= k[1] && a[0] + a[2] <= k[0] + k[2] + 1 && a[1] + a[3] <= k[1] + k[3] + 1;
    }

    static boolean giao(int[] a, int[] b) {
        return a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
    }

    static int soAnh;

    static void moTab(final int t, final String rieng, final int xem) throws Exception {
        chay(() -> {
            game.render.Canvas.endDlg();
            game.pokemon.ManChat.moTab(t);
            game.pokemon.KenhChat.dangNhan = rieng;
            ChupTrongGame.dat(game.pokemon.ManChat.gI(), "xem", Integer.valueOf(xem));
        });
        ngu(120);
    }

    static void boCuc(String nhan, int t, String rieng, int xem, int min, int cach, int W, int H) throws Exception {
        moTab(t, rieng, xem);
        ve();
        final int dem0 = game.pokemon.Bang.demTam;
        ve();
        String n = co + " " + nhan;
        ChupTrongGame.kiemTam(n, dem0 - 1);
        boolean coNhap = xem == 0 && (t != 4 || rieng != null);
        int[] tam = game.pokemon.Bang.VUA_TAM.clone();
        if (coNhap) {
            int[] o = game.pokemon.ManChat.VUA_NHAP.clone(), g = game.pokemon.ManChat.VUA_GUI.clone(), ds = game.pokemon.ManChat.VUA_DS.clone();
            kiem(trong(o, tam), n + ": ô nhập trong tấm " + java.util.Arrays.toString(o) + " / " + java.util.Arrays.toString(tam));
            kiem(trong(g, tam), n + ": nút Gửi trong tấm");
            kiem(!giao(o, g), n + ": ô nhập không chồng nút Gửi");
            kiem(o[1] >= ds[1] + ds[3] - 1 && g[1] >= ds[1] + ds[3] - 1, n + ": ô nhập + Gửi dưới danh sách tin");
            kiem(o[2] >= 60, n + ": ô nhập đủ rộng (" + o[2] + ")");
            kiem(o[1] + o[3] <= game.pokemon.Bang.duoi(), n + ": ô nhập trong vùng an toàn");
        }
        for (int i = 0; i < 6; i++) kiem(vung("tab-" + i) != null, n + ": có tab " + i);
        if (xem == 0) {
            kiem(game.pokemon.ManChat.soDongVe > 0, n + ": vẽ dòng tin / bạn bè (" + game.pokemon.ManChat.soDongVe + ")");
        }
        ChupTrongGame.dat = 0;
        ChupTrongGame.hong = 0;
        ChupTrongGame.kiemCham(n, min, cach);
        dat += ChupTrongGame.dat;
        hong += ChupTrongGame.hong;
        if (ra != null && soAnh < 120 && (co.startsWith("dt16") || co.equals("pc720") || co.startsWith("j2me") || co.equals("dt21x9"))) {
            javax.imageio.ImageIO.write(ChupTrongGame.veKiem(W, H, co + " " + nhan), "png", new File(ra, String.format("%03d_%s_%s.png", soAnh++, co, nhan)));
        }
    }

    /** Chấm đỏ vẽ trong vùng tên (sau một khung). */
    static boolean coCham(String tenVung) throws Exception {
        ve();
        final int truoc = game.pokemon.ChuDe.demCham;
        ve();
        final int sau = game.pokemon.ChuDe.demCham;
        int[] r = vung(tenVung);
        if (r == null) return false;
        for (int c = truoc; c < sau && c - truoc < 64; c++) {
            int x = game.pokemon.ChuDe.CHAM_X[c & 63], y = game.pokemon.ChuDe.CHAM_Y[c & 63];
            if (x >= r[0] - 8 && x <= r[0] + r[2] + 8 && y >= r[1] - 8 && y <= r[1] + r[3] + 8) return true;
        }
        return false;
    }

    static void datCo(int W, int H, int dpi, boolean j2me, int[] minCach) throws Exception {
        if (j2me) {
            NenPc.rong = W;
            NenPc.cao = H;
            NenPc.heSo = 1;
            NenPc.thatW = W;
            NenPc.thatH = H;
            NenPc.hienX = 0;
            NenPc.hienY = 0;
            NenPc.hienW = W;
            NenPc.hienH = H;
        } else {
            NenPc.tinhKichThuoc(W, H);
        }
        chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
        float s = NenPc.hienW / (float) NenPc.rong;
        if (dpi > 0) {
            float dpL = (dpi / 160f) / s;
            minCach[0] = (int) Math.ceil(44 * dpL);
            minCach[1] = (int) Math.floor(8 * dpL);
            game.pokemon.Bang.anTrai = (int) Math.ceil(100 / s);
        } else {
            minCach[0] = j2me ? 0 : (int) Math.ceil(32 / s);
            minCach[1] = j2me ? 0 : (int) Math.ceil(8 / s);
            game.pokemon.Bang.anTrai = 0;
        }
        game.pokemon.VungCham.MIN = minCach[0];
        game.pokemon.VungCham.CACH = minCach[1];
    }

    // ------------------------------------------------------------------ thao tác

    static void thaoTac() throws Exception {
        napDuLieu();
        // chạm tab Thế giới
        moTab(0, null, 0);
        kiem(chamVung("tab-1"), "chạm tab Thế giới: có vùng");
        kiem(game.pokemon.ManChat.gI().kenhGui() == 1, "chạm tab Thế giới: tab đổi (" + game.pokemon.ManChat.gI().kenhGui() + ")");
        kiem(game.pokemon.KenhChat.chuaDoc[1] == 0, "mở tab Thế giới: hết chưa đọc");
        // chạm dòng người khác -> menu tên
        int[] d = null;
        for (int i = 0; i < 6 && d == null; i++) d = vung("chat-dong-" + i);
        kiem(d != null, "tab Thế giới: dòng người khác có vùng chạm");
        if (d != null) cham(d[0] + d[2] / 2, d[1] + d[3] / 2);
        Object dlg = game.render.Canvas.currentDialog;
        kiem(dlg instanceof game.pokemon.MenuNguCanh, "chạm dòng: mở menu tên (" + dlg + ")");
        List<String> chu = new ArrayList<String>();
        if (dlg instanceof game.pokemon.MenuNguCanh) {
            java.util.Vector v = (java.util.Vector) lay(dlg, "lenh");
            for (int i = 0; v != null && i < v.size(); i++) chu.add(((game.model.Command) v.elementAt(i)).caption);
        }
        for (String c : new String[]{"Nhắn riêng", "Mời vào nhóm", "Xem thông tin", "Chặn"}) {
            kiem(chu.contains(c), "menu tên có '" + c + "' " + chu);
        }
        // Nhắn riêng
        final game.model.Command nhan = lenh(dlg, "Nhắn riêng");
        if (nhan != null) chay(() -> nhan.action.perform());
        ngu(150);
        String ai = game.pokemon.KenhChat.dangNhan;
        kiem(ai != null && game.pokemon.ManChat.gI().kenhGui() == 4, "Nhắn riêng: mở hội thoại riêng với " + ai);
        // gõ + Gửi
        xoaGoi();
        chay(() -> game.pokemon.ManChat.gI().o.setText("hẹn 8 giờ nhé"));
        kiem(chamVung("chat-gui"), "nút Gửi có vùng chạm");
        byte[] g = goiCuoi(-124);
        kiem(g != null, "Gửi tin riêng: gói -124");
        if (g != null) {
            java.io.DataInputStream r = doc(g);
            int op = r.readByte(), kenh = r.readByte();
            String dich = r.readUTF(), nd = r.readUTF();
            kiem(op == 1 && kenh == 4 && dich.equals(ai) && nd.equals("hẹn 8 giờ nhé"), "gói tin riêng: op 1 kênh 4 tới " + dich + ": " + nd);
        }
        kiem(game.pokemon.ManChat.gI().o.getText().length() == 0, "gửi xong: ô nhập trống");
        // kênh Bản đồ: Enter (-5) -> gói 27
        moTab(0, null, 0);
        xoaGoi();
        chay(() -> game.pokemon.ManChat.gI().o.setText("xin chào bản đồ"));
        phim(-5);
        byte[] g27 = goiCuoi(27);
        kiem(g27 != null && doc(g27).readUTF().equals("xin chào bản đồ"), "kênh Bản đồ + Enter: gói 27 như ô chat cũ");
        java.util.Vector bd = game.pokemon.KenhChat.dong(0);
        kiem(bd.size() > 0 && ((game.pokemon.KenhChat.Dong) bd.lastElement()).cuaMinh(), "kênh Bản đồ: tin của mình vào tab");
        // Thế giới đang chờ: nút Gửi tắt + đếm ngược
        ketQua(1, 2, 25, "Kênh thế giới: chờ 25 giây nữa.");
        moTab(1, null, 0);
        ve();
        kiem(vung("chat-gui") == null, "Thế giới đang chờ: nút Gửi tắt (không vùng chạm)");
        kiem(game.pokemon.KenhChat.giayCho() >= 24, "Thế giới: đếm ngược " + game.pokemon.KenhChat.giayCho() + " giây");
        chay(() -> game.pokemon.KenhChat.choTheGioiDen = 0);
        // Mời vào đội: tra id -> gói 49
        xoaGoi();
        chay(() -> game.pokemon.KenhChat.viecTheoTen(game.pokemon.KenhChat.VIEC_MOI_DOI, "Gengar0", (short) 30));
        byte[] g6 = goiCuoi(-124);
        kiem(g6 != null && g6[0] == 6, "Mời vào đội: hỏi id theo tên (op 6)");
        mayChu(o -> {
            o.writeByte(6);
            o.writeByte(0);
            o.writeUTF("Gengar0");
            o.writeShort(30);
            o.writeByte(1);
        });
        byte[] g49 = goiCuoi(49);
        kiem(g49 != null && g49[0] == 0 && ((g49[1] << 8) | (g49[2] & 0xFF)) == 30, "id trả về: gói 49 mời vào đội id 30");
        // chặn
        xoaGoi();
        chay(() -> game.pokemon.KenhChat.datChan("Onix1", true));
        byte[] gc = goiCuoi(-124);
        kiem(gc != null && gc[0] == 2 && gc[1] == 1, "chặn: gói -124 op 2");
        int truoc = game.pokemon.KenhChat.dong(1).size();
        tin(1, 0, "Onix1", "", "tin của người bị chặn", 31);
        kiem(game.pokemon.KenhChat.dong(1).size() == truoc, "người bị chặn: tin không hiện");
        chay(() -> game.pokemon.KenhChat.datChan("Onix1", false));
        // tuỳ chọn HUD: tắt Thế giới -> tin Thế giới không lên dải HUD
        moTab(1, null, 1);
        kiem(chamVung("chat-hud-1"), "tuỳ chọn: hàng 'Thế giới' trên HUD có vùng chạm");
        kiem(!game.pokemon.KenhChat.hienHud[1], "tuỳ chọn: tắt Thế giới trên HUD");
        java.util.Vector hud = hud();
        int nHud = hud.size();
        tin(1, 0, "Mew9", "", "không lên HUD", 40);
        kiem(hud().size() == nHud, "Thế giới tắt trên HUD: dải HUD không thêm dòng");
        chay(() -> game.pokemon.KenhChat.hienHud[1] = true);
        tin(1, 0, "Mew9", "", "lên HUD", 40);
        java.util.Vector h2 = hud();
        Object cuoi = h2.size() > 0 ? h2.lastElement() : null;
        kiem(cuoi instanceof game.model.ChatInfo && ((game.model.ChatInfo) cuoi).name.startsWith("[TG]")
                && ((game.model.ChatInfo) cuoi).content.equals("lên HUD"), "Thế giới bật: dải HUD có '[TG] Mew9: lên HUD' (giữ chữ hoa)");
        // đóng bảng: tin riêng mới -> chấm đỏ nút chat HUD; chạm nút = mở lại tab cuối
        chay(() -> {
            game.render.Canvas.endDlg();
            game.pokemon.ManChat.gI().dong();
        });
        ngu(120);
        chay(() -> {
            for (int i = 0; i < 6; i++) game.pokemon.KenhChat.daDoc(i);
            for (int i = 0; i < game.pokemon.KenhChat.dsBan.size(); i++) ((game.pokemon.KenhChat.Ban) game.pokemon.KenhChat.dsBan.elementAt(i)).chuaDoc = 0;
        });
        kiem(!game.pokemon.ChamMoi.hudChat(), "đọc hết: không chấm nút chat");
        tin(4, 0, "Bulbasaur1", "Bulbasaur1", "Cậu có rảnh không?", 12);
        kiem(game.pokemon.ChamMoi.hudChat(), "tin riêng chưa đọc: ChamMoi.hudChat");
        if (game.render.screen.HudRong.bat()) {
            kiem(coCham("hud-chat"), "tin riêng chưa đọc: chấm đỏ ở nút chat HUD");
            kiem(chamVung("hud-chat"), "nút chat HUD có vùng chạm");
            kiem(game.render.Canvas.currentScreen == game.pokemon.ManChat.me, "chạm nút chat HUD: mở bảng chat");
            kiem(game.pokemon.ManChat.gI().kenhGui() == 1, "mở lại tab dùng lần cuối (Thế giới)");
            kiem(coCham("tab-4"), "tab Bạn bè có chấm đỏ (tin riêng chưa đọc)");
            kiem(chamVung("tab-4"), "chạm tab Bạn bè");
            ve();
            kiem(vung("chat-ban-0") != null, "tab Bạn bè: hàng người nhắn");
            kiem(chamVung("chat-ban-0"), "chạm hàng người nhắn chưa đọc (đứng đầu)");
            kiem("Bulbasaur1".equals(game.pokemon.KenhChat.dangNhan), "hội thoại người có tin chưa đọc (" + game.pokemon.KenhChat.dangNhan + ")");
            kiem(!game.pokemon.ChamMoi.hudChat(), "đã đọc: hết chấm nút chat");
        }
        // gói 27 của người khác (bản đồ) vào tab Bản đồ
        chay(() -> game.pokemon.KenhChat.banDo((short) 77, "Psyduck5", "đi đâu vậy"));
        java.util.Vector b0 = game.pokemon.KenhChat.dong(0);
        kiem(((game.pokemon.KenhChat.Dong) b0.lastElement()).ten.equals("Psyduck5"), "tin bản đồ người khác vào tab Bản đồ");
        // máy chủ cũ: không gói cấu hình -> bang hội gửi -18, tin riêng -5
        chay(() -> game.pokemon.KenhChat.coMayChu = false);
        xoaGoi();
        chay(() -> game.pokemon.KenhChat.gui(2, "", "chào bang"));
        kiem(goiCuoi(-18) != null, "máy chủ cũ: Đội gửi gói -18");
        chay(() -> game.pokemon.KenhChat.gui(4, "Eevee2", "chào bạn"));
        kiem(goiCuoi(-5) != null, "máy chủ cũ: tin riêng gửi gói -5");
        final String[] loiTg = {null};
        chay(() -> loiTg[0] = game.pokemon.KenhChat.gui(1, "", "alo"));
        kiem(loiTg[0] != null && loiTg[0].length() > 0, "máy chủ cũ: Thế giới báo chưa hỗ trợ (" + loiTg[0] + ")");
        chay(() -> game.pokemon.KenhChat.riengCu("Nhóm", "Pikachu7: gom quái"));
        kiem(((game.pokemon.KenhChat.Dong) game.pokemon.KenhChat.dong(3).lastElement()).noiDung.equals("gom quái"),
                "máy chủ cũ: -5 từ 'Nhóm' vào tab Nhóm");
        chay(() -> game.pokemon.KenhChat.coMayChu = true);
    }

    static Object lay(Object o, String ten) {
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            try {
                java.lang.reflect.Field f = c.getDeclaredField(ten);
                f.setAccessible(true);
                return f.get(o);
            } catch (NoSuchFieldException e) {
                // lớp cha
            } catch (Throwable e) {
                return null;
            }
        }
        return null;
    }

    static java.util.Vector hud() {
        try {
            java.lang.reflect.Field f = game.render.screen.GameScr.class.getDeclaredField("chatHistory");
            f.setAccessible(true);
            return (java.util.Vector) f.get(game.render.Canvas.gameScr);
        } catch (Throwable e) {
            return new java.util.Vector();
        }
    }

    static game.model.Command lenh(Object dlg, String chu) {
        if (!(dlg instanceof game.pokemon.MenuNguCanh)) return null;
        java.util.Vector v = (java.util.Vector) lay(dlg, "lenh");
        for (int i = 0; v != null && i < v.size(); i++) {
            game.model.Command c = (game.model.Command) v.elementAt(i);
            if (chu.equals(c.caption)) return c;
        }
        return null;
    }

    /** J2ME bấm phím (không chạm). */
    static void phimJ2me() throws Exception {
        napDuLieu();
        moTab(0, null, 0);
        chay(() -> game.pokemon.ManChat.gI().o.setText(""));
        phim(-4);   // phải
        kiem(game.pokemon.ManChat.gI().kenhGui() == 1, "J2ME phải: sang tab Thế giới (" + game.pokemon.ManChat.gI().kenhGui() + ")");
        phim(-3);   // trái
        kiem(game.pokemon.ManChat.gI().kenhGui() == 0, "J2ME trái: về tab Bản đồ");
        phim(-4);
        phim(-2);   // xuống: chọn dòng
        int chon = ((Integer) lay(game.pokemon.ManChat.gI(), "chon")).intValue();
        kiem(chon >= 0, "J2ME xuống: chọn dòng " + chon);
        phim(-5);   // 5: menu tên
        kiem(game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh, "J2ME 5 trên dòng: menu tên");
        chay(game.render.Canvas::endDlg);
        // phím số = gõ
        chay(() -> game.render.screen.TField.isQwerty = true);
        phim('h');
        phim('i');
        kiem("hi".equals(game.pokemon.ManChat.gI().o.getText()), "J2ME gõ phím: ô nhập '" + game.pokemon.ManChat.gI().o.getText() + "'");
        xoaGoi();
        phim(-5);
        byte[] g = goiCuoi(-124);
        kiem(g != null && g[0] == 1 && g[1] == 1, "J2ME 5 có chữ: gửi kênh Thế giới");
        // phím mềm phải: đóng
        phim(-7);
        kiem(game.render.Canvas.currentScreen == game.render.Canvas.gameScr, "J2ME phím mềm phải: đóng bảng");
        // phím # menu -> "Chat" mở bảng
        chay(() -> game.pokemon.ManChat.mo());
        ngu(100);
        kiem(game.render.Canvas.currentScreen == game.pokemon.ManChat.me, "ManChat.mo: mở bảng (lối vào menu phím #)");
        phim(-6);
        kiem(game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh, "J2ME phím mềm trái: menu Tuỳ chọn");
        chay(game.render.Canvas::endDlg);
        phim(-7);
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "kiem_kenh_chat");
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
        chay(() -> {
            try {
                game.render.screen.TField.isQwerty = true;
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
        chay(ChupTrongGame::giaLap);
        ngu(500);
        game.pokemon.ChuUI boHd = game.pokemon.ChuUI.bo;
        int[][] CO = ChupTrongGame.CO;
        int[] mc = new int[2];
        for (int c = 0; c <= CO.length + 1; c++) {
            int W, H, dpi;
            if (c < CO.length) {
                W = CO[c][0];
                H = CO[c][1];
                dpi = CO[c][2];
                co = ChupTrongGame.TEN_CO[c];
                htbb.pc.ChuHD.bat = true;
                htbb.pc.GiaoDienHD.bat = true;
                game.pokemon.ChuUI.bo = boHd;
            } else {
                W = c == CO.length ? 320 : 240;
                H = c == CO.length ? 240 : 320;
                dpi = 0;
                co = c == CO.length ? "j2me320x240" : "j2me240x320";
                htbb.pc.ChuHD.bat = false;
                htbb.pc.GiaoDienHD.bat = false;
                game.pokemon.ChuUI.bo = null;
            }
            datCo(W, H, dpi, co.startsWith("j2me"), mc);
            napDuLieu();
            // chưa đọc: Bang hội có tin mới khi đang ở tab khác -> chấm ở tab 2
            moTab(0, null, 0);
            tin(2, 0, "Bulbasaur1", "", "Ai online không?", 12);
            ve();
            kiem(coCham("tab-2"), "tab Bang hội có chấm đỏ khi có tin chưa đọc");
            kiem(!coCham("tab-0"), "tab đang xem (Bản đồ) không chấm");
            String[] ten = {"ban_do", "the_gioi", "bang_hoi", "doi", "ban_be", "rieng", "he_thong", "tuy_chon"};
            int[] tab = {0, 1, 2, 3, 4, 4, 5, 1};
            String[] rieng = {null, null, null, null, null, "Pikachu7", null, null};
            int[] xem = {0, 0, 0, 0, 0, 0, 0, 1};
            for (int i = 0; i < ten.length; i++) boCuc(ten[i], tab[i], rieng[i], xem[i], mc[0], mc[1], W, H);
            if ("pc720".equals(co) || "dt20x9".equals(co)) {
                thaoTac();
            }
            if ("j2me320x240".equals(co)) {
                phimJ2me();
            }
            chay(() -> {
                game.render.Canvas.endDlg();
                game.render.Canvas.gameScr.switchToMe();
            });
        }
        kiem(ChupTrongGame.hong == 0, "chu trong khung + icon PNG (ChupTrongGame.veKiem): " + ChupTrongGame.hong + " hong");
        System.out.println("KiemKenhChat: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
