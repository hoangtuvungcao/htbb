import htbb.pc.NhapChu;
import java.awt.Component;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.text.AttributedString;
import javax.microedition.lcdui.NenPc;

/**
 * Kiểm logic bản PC không cần màn hình (khung cố định / phóng / viền / đổi toạ độ chuột, nhập chữ từ bộ gõ, lọc
 * dán, hàng tên trên đầu, hộp thoại đáy, vùng bản đồ):
 *   sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d /tmp/kpc client_pc/test/KiemPc.java \
 *     && java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:/tmp/kpc KiemPc
 */
public class KiemPc {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    /**
     * 0.9.6.1 câu cá: phao luôn rơi xuống Ô NƯỚC hồ (kể cả đang quay lưng lại hồ), xa nước = không câu; bản đồ nhỏ vẽ
     * phao nhỏ nền trong suốt (không còn ô vuông tối 6x6 mỗi điểm câu - lỗi chủ game báo).
     */
    static void kiemCauCa() {
        int w = 12, h = 10;
        boolean[] ho = new boolean[w * h];
        for (int y = 3; y < 7; y++) {
            for (int x = 6; x < 10; x++) {
                ho[y * w + x] = true;
            }
        }
        int[][] dir = {{game.model.Char.LEFT}, {game.model.Char.RIGHT}, {game.model.Char.UP}, {game.model.Char.DOWN}};
        for (int[] d : dir) {
            int[] o = game.pokemon.SinhThai.oNuocGan(ho, w, h, 4, 4, d[0]);
            kiem(o != null && ho[o[1] * w + o[0]], "cau ca: dung cach ho 2 o, huong " + d[0] + " -> phao xuong o nuoc");
            int[] o2 = game.pokemon.SinhThai.oNuocGan(ho, w, h, 5, 4, d[0]);
            kiem(o2 != null && ho[o2[1] * w + o2[0]] && Math.abs(o2[0] - 5) + Math.abs(o2[1] - 4) <= 2,
                    "cau ca: sat bo, huong " + d[0] + " -> o nuoc gan");
        }
        int[] uu = game.pokemon.SinhThai.oNuocGan(ho, w, h, 5, 4, game.model.Char.RIGHT);
        kiem(uu != null && uu[0] == 6 && uu[1] == 4, "cau ca: nhin ra ho -> o nuoc ngay truoc mat (6,4)");
        kiem(game.pokemon.SinhThai.oNuocGan(ho, w, h, 1, 4, game.model.Char.RIGHT) == null, "cau ca: cach 5 o -> khong cau (khong nem xuong dat)");
        kiem(game.pokemon.SinhThai.oNuocGan(ho, w, h, 3, 4, game.model.Char.RIGHT) == null, "cau ca: cach 3 o -> khong cau (= server 2 o)");
        kiem(game.pokemon.SinhThai.oNuocGan(null, w, h, 5, 4, game.model.Char.RIGHT) == null, "cau ca: map khong ho -> khong cau");
        // bản đồ nhỏ: 4 hồ x 1 phao, nền xanh cỏ -> không điểm tối, mỗi phao <= 10 điểm ảnh (3x3 + cán)
        javax.microedition.lcdui.Image im = javax.microedition.lcdui.Image.createImage(60, 40);
        javax.microedition.lcdui.Graphics g = im.getGraphics();
        g.setColor(0x5A9A40);
        g.fillRect(0, 0, 60, 40);
        game.pokemon.SinhThai.datDiemCau(-99, new int[]{8, 20, 35, 50}, new int[]{8, 30, 12, 25});
        game.pokemon.SinhThai.veDiemCauMini(g);
        int[] px = new int[60 * 40];
        im.getRGB(px, 0, 60, 0, 0, 60, 40);
        int toi = 0, doi = 0;
        for (int p : px) {
            int r = (p >> 16) & 255, gg = (p >> 8) & 255, b = p & 255;
            if ((p & 0xFFFFFF) != 0x5A9A40) doi++;
            if (r * 3 + gg * 6 + b < 600) toi++;
        }
        kiem(toi == 0, "ban do nho: diem cau khong ve khoi toi (" + toi + " diem toi)");
        kiem(doi > 0 && doi <= 4 * 10, "ban do nho: moi diem cau la phao nho <= 10 px (" + doi + " px / 4 diem)");
        game.pokemon.SinhThai.datDiemCau(-98, new int[0], new int[0]);
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        // ---- khung PHỦ KÍN (0.9.3): mọi cỡ cửa sổ -> logic <= 640x360 mỗi chiều, phủ kín cửa sổ, không viền
        int[][] co = {{1280, 720}, {1280, 646}, {1493, 731}, {1920, 1080}, {2560, 1440}, {3840, 2160}, {2560, 1080},
            {1366, 768}, {1024, 768}, {800, 600}, {640, 360}, {640, 400}, {500, 300}, {1920, 1200}, {7680, 4320}, {3440, 1440}, {2400, 1080}};
        for (int[] c : co) {
            int W = c[0], H = c[1];
            NenPc.tinhKichThuoc(W, H);
            String t = W + "x" + H;
            kiem(NenPc.rong <= 640 && NenPc.cao <= 360 && (NenPc.rong == 640 || NenPc.cao == 360), t + " logic <= 640x360, mot chieu day (" + NenPc.rong + "x" + NenPc.cao + ")");
            kiem(NenPc.thatW == NenPc.rong * NenPc.heSo && NenPc.thatH == NenPc.cao * NenPc.heSo, t + " bo dem logic x k");
            kiem(NenPc.hienX == 0 && NenPc.hienY == 0 && NenPc.hienW >= W && NenPc.hienH >= H, t + " phu kin cua so (khong vien) " + NenPc.hienW + "x" + NenPc.hienH);
            float s0 = Math.max(W / 640f, H / 360f);
            kiem(NenPc.hienW - W < s0 + 1 && NenPc.hienH - H < s0 + 1, t + " tran mep < 1 px logic");
            // không méo: tỉ lệ hiện = tỉ lệ logic
            kiem(Math.abs(NenPc.hienW / (float) NenPc.rong - NenPc.hienH / (float) NenPc.cao) < 0.02f, t + " khong meo");
            kiem(NenPc.heSo >= 1 && NenPc.heSo <= 8, t + " he so 1..8");
            // vùng nhìn thế giới <= 320x180 px (không lợi thế khi phóng to / màn dài)
            // 0.9.7: camera "cao hơn" (Zoom.NHIN_THEM 1,35): vùng nhìn ~432x243, sai khác <= 11% do làm tròn m nguyên
            int vw = htbb.pc.Zoom.gsw(NenPc.rong), vh = htbb.pc.Zoom.gsh(NenPc.cao);
            kiem(vw <= 432 * 1.13f && vh <= 243 * 1.13f, t + " vung nhin the gioi <= 432x243 (+11%): " + vw + "x" + vh);
            kiem(vw >= 320 * 1.25f || vh >= 180 * 1.25f, t + " thay them >= ~25% (" + vw + "x" + vh + ")");
            float mm = htbb.pc.Zoom.mThuc(), muc = 2f * NenPc.heSo / htbb.pc.Zoom.NHIN_THEM;
            kiem(Math.abs(mm - muc) <= muc * 0.11f + 1e-4f, t + " phong the gioi ~ 2k / 1,35 (" + mm + ")");
            // chuột: góc trên trái -> 0,0; góc dưới phải -> hàng logic cuối; giữa -> giữa
            kiem(NenPc.vaoBoDemX(0) == 0 && NenPc.vaoBoDemY(0) == 0, t + " goc tren trai");
            int bx = NenPc.vaoBoDemX(W - 1), by = NenPc.vaoBoDemY(H - 1);
            kiem(NenPc.trongKhung(bx, by) && NenPc.logicX(bx) >= NenPc.rong - 2 && NenPc.logicY(by) >= NenPc.cao - 2, t + " goc duoi phai -> hang cuoi (" + NenPc.logicX(bx) + "," + NenPc.logicY(by) + ")");
            int cx = NenPc.vaoBoDemX(W / 2), cy = NenPc.vaoBoDemY(H / 2);
            kiem(Math.abs(NenPc.logicX(cx) - NenPc.rong / 2) <= 1 && Math.abs(NenPc.logicY(cy) - NenPc.cao / 2) <= 1, t + " giua -> giua");
        }
        // vài trường hợp cụ thể
        NenPc.tinhKichThuoc(1280, 720);
        kiem(NenPc.heSo == 2 && NenPc.hienW == 1280 && NenPc.rong == 640 && NenPc.cao == 360, "1280x720 = x2 vua khit");
        NenPc.tinhKichThuoc(1920, 1080);
        kiem(NenPc.heSo == 3 && NenPc.hienW == 1920, "1920x1080 = x3 vua khit");
        NenPc.tinhKichThuoc(2560, 1080);
        kiem(NenPc.rong == 640 && NenPc.cao == 270, "21:9 2560x1080 -> logic 640x270 (cat cao) " + NenPc.rong + "x" + NenPc.cao);
        NenPc.tinhKichThuoc(1024, 768);
        kiem(NenPc.rong == 480 && NenPc.cao == 360, "4:3 1024x768 -> logic 480x360 (cat ngang) " + NenPc.rong + "x" + NenPc.cao);
        // phóng to / thu nhỏ nhiều lần: kết quả chỉ phụ thuộc cỡ cuối
        NenPc.tinhKichThuoc(3840, 2160);
        NenPc.tinhKichThuoc(1280, 646);
        int k1 = NenPc.heSo, x1 = NenPc.rong, w1 = NenPc.hienW;
        NenPc.tinhKichThuoc(1280, 646);
        kiem(k1 == NenPc.heSo && x1 == NenPc.rong && w1 == NenPc.hienW, "doi co lap lai on dinh");
        kiem(!NenPc.tinhKichThuoc(0, 0), "co 0 bo qua");
        NenPc.tinhKichThuoc(1280, 720);

        // ---- bộ gõ: chữ đã chốt từ InputMethodEvent (ibus-bamboo / fcitx-unikey...)
        Component nguon = new java.awt.Canvas();
        kiem(NhapChu.chuDaChot(ime(nguon, "Việt", 4)).equals("Việt"), "IME chot ca chuoi");
        kiem(NhapChu.chuDaChot(ime(nguon, "ệab", 1)).equals("ệ"), "IME chot 1, phan con lai dang soan");
        kiem(NhapChu.chuDaChot(ime(nguon, "viet", 0)).equals(""), "IME dang soan chua chot");
        kiem(NhapChu.chuDaChot(new InputMethodEvent(nguon, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, null, 0, null, null)).equals(""), "IME text null");
        // KEY_TYPED: chữ thường / AltGr là chữ; Ctrl+V / Alt+x là phím tắt
        kiem(NhapChu.laChuGo(go(nguon, 'a', 0)), "keyTyped a");
        kiem(NhapChu.laChuGo(go(nguon, 'ư', 0)), "keyTyped ư (bo go gui KEY_TYPED)");
        kiem(!NhapChu.laChuGo(go(nguon, (char) 22, KeyEvent.CTRL_DOWN_MASK)), "Ctrl+V khong phai chu");
        kiem(!NhapChu.laChuGo(go(nguon, 'x', KeyEvent.ALT_DOWN_MASK)), "Alt+x khong phai chu");
        kiem(NhapChu.laChuGo(go(nguon, '@', KeyEvent.ALT_GRAPH_DOWN_MASK)), "AltGr+@ la chu");
        // dán: xuống dòng thành dấu cách, bỏ ký tự điều khiển, cắt độ dài
        kiem(NhapChu.locDan("ab\ncd\r\te\u0007f", 100).equals("ab cd  ef"), "loc dan " + NhapChu.locDan("ab\ncd\r\te\u0007f", 100));
        kiem(NhapChu.locDan("abcdef", 3).equals("abc"), "dan cat do dai");
        kiem(NhapChu.locDan(null, 3).equals(""), "dan null");

        // ---- hàng tên trên đầu: sát đỉnh hình thật (cũ cố định y-40)
        kiem(game.model.Char.tinhDyTen(31) == 0, "nguoi ~31px giu vi tri cu");
        kiem(game.model.Char.tinhDyTen(18) == 13, "Bulbasaur ~18px: ten ha 13px (dau = 27)");
        kiem(game.model.Char.tinhDyTen(0) == 26, "hinh 0px: ten toi thieu 14px tren chan");
        kiem(game.model.Char.tinhDyTen(60) == -29, "Pokemon cao 60px: ten len cao hon");
        // ---- hộp thoại đáy: lời thông tin tự tắt 3..10 giây
        kiem(game.pokemon.HopThoaiDuoi.thoiGianDoc(0, 0) == 3000L, "loi rong 3s");
        kiem(game.pokemon.HopThoaiDuoi.thoiGianDoc(32, 500) == 2500L + 32 * 70L, "loi chao tam biet ~4.7s (cu 20s)");
        kiem(game.pokemon.HopThoaiDuoi.thoiGianDoc(400, 2000) == 10000L, "loi dai toi da 10s");
        // ---- vùng địa lý của map
        kiem("Kanto".equals(game.pokemon.VungMap.ten(0)), "map 0 Pewter = Kanto");
        kiem("Johto".equals(game.pokemon.VungMap.ten(70)), "map 70 Blackthorn = Johto");
        kiem("Hoenn".equals(game.pokemon.VungMap.ten(43)), "map 43 Littleroot = Hoenn");
        kiem(game.pokemon.VungMap.ten(105) == null, "map 105 san khoi dau: khong nhan");
        kiem(game.pokemon.VungMap.ten(202) == null, "dau truong: khong nhan");
        // ---- 0.9.6.1: lớp chữ FPS / độ phân giải ("hiển thị 60 FPS 1190×720 logic ...") TẮT ở bản phát hành
        boolean fps0 = htbb.pc.CaiDat.hienFps, dk0 = htbb.pc.CaiDat.doKhung, dev0 = htbb.pc.CaiDat.dev;
        Object thu0 = javax.microedition.midlet.MIDlet.GHI_DE.get("HTBB-Ban-Thu");
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-Ban-Thu", "0");
        htbb.pc.CaiDat.dev = false;
        htbb.pc.CaiDat.hienFps = false;
        htbb.pc.CaiDat.doKhung = false;
        kiem(!htbb.pc.CaiDat.hienLopDo(), "ban phat hanh mac dinh: khong ve lop FPS");
        htbb.pc.CaiDat.hienFps = true;   // hien_fps=1 con trong cai_dat.properties tu ban cu
        htbb.pc.CaiDat.doKhung = true;
        kiem(!htbb.pc.CaiDat.hienLopDo() && !htbb.pc.CaiDat.choDo(), "ban phat hanh: hien_fps / do_khung cu bi bo qua, khong co cong tac");
        htbb.pc.CaiDat.dev = true;       // --fps / -Dhtbb.dev=1
        kiem(htbb.pc.CaiDat.hienLopDo(), "ban phat hanh + co dev (--fps): co lop FPS");
        htbb.pc.CaiDat.dev = false;
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-Ban-Thu", "1");
        kiem(htbb.pc.CaiDat.hienLopDo(), "ban thu + bat Hien FPS: co lop FPS");
        htbb.pc.CaiDat.hienFps = false;
        htbb.pc.CaiDat.doKhung = false;
        kiem(!htbb.pc.CaiDat.hienLopDo(), "ban thu mac dinh: khong ve lop FPS");
        if (thu0 == null) javax.microedition.midlet.MIDlet.GHI_DE.remove("HTBB-Ban-Thu");
        else javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-Ban-Thu", thu0);
        htbb.pc.CaiDat.hienFps = fps0;
        htbb.pc.CaiDat.doKhung = dk0;
        htbb.pc.CaiDat.dev = dev0;
        kiemCauCa();
        System.out.println("KiemPc: " + dat + " dat, " + hong + " hong");
        if (hong > 0) System.exit(1);
    }

    static InputMethodEvent ime(Component c, String s, int chot) {
        return new InputMethodEvent(c, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, new AttributedString(s).getIterator(), chot, null, null);
    }

    static KeyEvent go(Component c, char ch, int mod) {
        return new KeyEvent(c, KeyEvent.KEY_TYPED, 0L, mod, KeyEvent.VK_UNDEFINED, ch);
    }
}
