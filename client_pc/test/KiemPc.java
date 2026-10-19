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

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        // ---- khung cố định: mọi cỡ cửa sổ -> logic 640x360, bộ đệm 640k x 360k, khung hiện nằm trong cửa sổ
        int[][] co = {{1280, 720}, {1280, 646}, {1493, 731}, {1920, 1080}, {2560, 1440}, {3840, 2160}, {2560, 1080},
            {1366, 768}, {1024, 768}, {800, 600}, {640, 360}, {640, 400}, {500, 300}, {1920, 1200}, {7680, 4320}, {3440, 1440}};
        for (int[] c : co) {
            int W = c[0], H = c[1];
            NenPc.tinhKichThuoc(W, H);
            String t = W + "x" + H;
            kiem(NenPc.rong == 640 && NenPc.cao == 360, t + " logic co dinh 640x360");
            kiem(NenPc.thatW == 640 * NenPc.heSo && NenPc.thatH == 360 * NenPc.heSo, t + " bo dem 640k x 360k");
            kiem(NenPc.hienX >= 0 && NenPc.hienY >= 0 && NenPc.hienX + NenPc.hienW <= W && NenPc.hienY + NenPc.hienH <= H, t + " khung hien trong cua so");
            // giữ tỉ lệ 16:9 (méo < 1 px)
            kiem(Math.abs(NenPc.hienW * 9 - NenPc.hienH * 16) <= 16 + 9, t + " giu ti le 16:9 " + NenPc.hienW + "x" + NenPc.hienH);
            // phủ ít nhất 80% chiều vừa nhất
            float vua = Math.min(W / 640f, H / 360f);
            kiem(NenPc.hienW >= 640 * vua * 0.8f - 1, t + " phu >= 80%");
            kiem(NenPc.heSo >= 1 && NenPc.heSo <= 8, t + " he so 1..8");
            // vùng nhìn thế giới cố định 320x180 px (20 x 11.25 ô)
            kiem(htbb.pc.Zoom.gsw(NenPc.rong) == 320 && htbb.pc.Zoom.gsh(NenPc.cao) == 180, t + " vung nhin the gioi 320x180");
            kiem(htbb.pc.Zoom.m() == 2 * NenPc.heSo, t + " phong the gioi = 2k");
            // chuột: góc khung hiện -> góc bộ đệm / logic; ngoài khung -> không trong khung
            kiem(NenPc.vaoBoDemX(NenPc.hienX) == 0 && NenPc.vaoBoDemY(NenPc.hienY) == 0, t + " goc tren trai");
            int bx = NenPc.vaoBoDemX(NenPc.hienX + NenPc.hienW - 1), by = NenPc.vaoBoDemY(NenPc.hienY + NenPc.hienH - 1);
            int sai = NenPc.hienW == NenPc.thatW ? 0 : 2;   // thu nhỏ: làm tròn <= 2 px logic
            kiem(NenPc.trongKhung(bx, by) && NenPc.logicX(bx) >= 639 - sai && NenPc.logicY(by) >= 359 - sai, t + " goc duoi phai -> 639,359 (" + NenPc.logicX(bx) + "," + NenPc.logicY(by) + ")");
            int cx = NenPc.vaoBoDemX(NenPc.hienX + NenPc.hienW / 2), cy = NenPc.vaoBoDemY(NenPc.hienY + NenPc.hienH / 2);
            kiem(Math.abs(NenPc.logicX(cx) - 320) <= 1 && Math.abs(NenPc.logicY(cy) - 180) <= 1, t + " giua -> 320,180");
            if (NenPc.hienX > 0) kiem(!NenPc.trongKhung(NenPc.vaoBoDemX(NenPc.hienX - 1), by), t + " vien trai ngoai khung");
            if (NenPc.hienY > 0) kiem(!NenPc.trongKhung(bx, NenPc.vaoBoDemY(NenPc.hienY - 1)), t + " vien tren ngoai khung");
        }
        // vài trường hợp cụ thể
        NenPc.tinhKichThuoc(1280, 720);
        kiem(NenPc.heSo == 2 && NenPc.hienW == 1280 && NenPc.hienX == 0, "1280x720 = x2 vua khit");
        NenPc.tinhKichThuoc(1920, 1080);
        kiem(NenPc.heSo == 3 && NenPc.hienW == 1920, "1920x1080 = x3 vua khit");
        NenPc.tinhKichThuoc(1493, 731);
        kiem(NenPc.heSo == 2 && NenPc.hienW == 1280 && NenPc.hienX == 106, "1493x731 = x2 + vien (cu: logic 746x365)");
        NenPc.tinhKichThuoc(1280, 646);
        kiem(NenPc.heSo == 2 && NenPc.hienH == 646 && NenPc.hienW < 1280, "1280x646 = ve x2 thu vua (cu: logic 640x323)");
        // phóng to / thu nhỏ nhiều lần: kết quả chỉ phụ thuộc cỡ cuối
        NenPc.tinhKichThuoc(3840, 2160);
        NenPc.tinhKichThuoc(1280, 646);
        int k1 = NenPc.heSo, x1 = NenPc.hienX, w1 = NenPc.hienW;
        NenPc.tinhKichThuoc(1280, 646);
        kiem(k1 == NenPc.heSo && x1 == NenPc.hienX && w1 == NenPc.hienW, "doi co lap lai on dinh");
        kiem(!NenPc.tinhKichThuoc(0, 0), "co 0 bo qua");

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
