import java.awt.image.BufferedImage;
import java.io.File;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM HỒI QUY 0.9.6.1 (headless, không máy chủ):
 *
 * A. THANH TRƯỢT KÉO (game.pokemon.Bang.thanhTruot - bảng Tự động đánh: thuốc HP / PP, vùng đánh). Lỗi 0.9.6: kéo thì
 *    núm đứng yên, thả mới nhảy (bảng xoá Canvas.isPointerDown mỗi nhịp). Mỗi kiểu nhập (cham = chạm APK / J2ME, chuot
 *    = chuột PC qua NenPc.cham): nhấn -> nhiều lần kéo (có nhịp game chen giữa) -> mỗi lần kéo, khung vẽ kế tiếp có
 *    giá trị = giá trị tại ngón và tâm núm dịch theo; chưa lưu khi đang kéo; kéo quá cuối thanh / lệch xuống = kẹp max
 *    và thả NGOÀI thanh vẫn lưu; thả xong núm không giật về giá trị cũ; kéo về đầu = 0 (Tắt).
 *
 * B. ĐỔI CỠ CỬA SỔ (htbb.pc.ManHinh + NenPc). Lỗi 0.9.6: cửa sổ to ra (phóng to / cỡ lớn hơn trong Cài đặt) -> đen.
 *    Chuỗi cỡ bắt đầu nhỏ rồi TO HƠN cỡ đầu, phóng to 1280x646, 21:9, 4:3, toàn màn 1920x1080, thu lại: mỗi cỡ, vẽ đúng
 *    đường của cửa sổ thật (luồng sự kiện NenPc -> bộ đệm game -> hợp thành -> bề mặt) vào ảnh cỡ cửa sổ, lấy mẫu
 *    điểm ảnh: không đen; bộ đệm game đúng cỡ mới; khung logic phủ kín; game nhận cỡ logic mới (sizeChanged).
 *    Luật tạo lại bề mặt trình bày (ManHinh.canTaoLai): đổi cỡ to / nhỏ / vừa lỗi = tạo lại, cùng cỡ = giữ.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java \
 *       client_pc/test/KiemTruotDoiCo.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTruotDoiCo [thư mục ảnh]
 * Mã thoát 1 = có lỗi.
 */
public class KiemTruotDoiCo {
    static int dat, hong;
    static String KIEU = "cham";
    static File ra;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG [" + KIEU + "]: " + ten);
        }
    }

    static void ngu(long ms) throws Exception {
        Thread.sleep(ms);
    }

    static void goi(final String ten, final int x, final int y) throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    java.lang.reflect.Method m = game.render.Canvas.class.getDeclaredMethod(ten, int.class, int.class);
                    m.setAccessible(true);
                    m.invoke(game.render.Canvas.instance, x, y);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** kieu 0 nhấn, 1 kéo, 2 thả - toạ độ logic. Chuột: đi qua NenPc.cham (toạ độ bộ đệm thật) như ManHinh. */
    static void con(int kieu, int x, int y) throws Exception {
        if ("chuot".equals(KIEU)) {
            NenPc.cham(kieu, x * NenPc.heSo + NenPc.heSo / 2, y * NenPc.heSo + NenPc.heSo / 2);
        } else {
            goi(kieu == 0 ? "pointerPressed" : kieu == 1 ? "pointerDragged" : "pointerReleased", x, y);
        }
    }

    static int[] vung(String ten) {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) return game.pokemon.VungCham.vungVe(i);
        }
        return null;
    }

    static int docTinh(Class<?> c, String ten) {
        try {
            java.lang.reflect.Field f = c.getDeclaredField(ten);
            f.setAccessible(true);
            return ((Number) f.get(null)).intValue();
        } catch (Throwable e) {
            e.printStackTrace();
            return -999;
        }
    }

    static int soAnh;

    /** số mức vùng đánh máy chủ cho (BangTuDong.soVung - thanh "Vùng đánh" có max = soVung - 1) */
    static int soVung() {
        try {
            java.lang.reflect.Method m = Class.forName("game.render.screen.BangTuDong").getDeclaredMethod("soVung");
            m.setAccessible(true);
            return ((Integer) m.invoke(null)).intValue();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static int[] vungO() {
        try {
            java.lang.reflect.Field f = game.render.screen.TuDong.class.getDeclaredField("VUNG_O");
            f.setAccessible(true);
            return (int[]) f.get(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static void veKhung(String nhan) throws Exception {
        BufferedImage a = ChupTrongGame.ve(1280, 720);
        if (ra != null && soAnh < 30) {
            javax.imageio.ImageIO.write(a, "png", new File(ra, String.format("truot_%02d_%s_%s.png", soAnh++, KIEU, nhan)));
        }
    }

    /** Một lượt kéo thanh tên `ten` (giá trị lưu ở TuDong.`truong`, max, bước). */
    static void keoThanh(String ten, String truong, int max, int buoc) throws Exception {
        Class<?> td = game.render.screen.TuDong.class;
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.moMan("tu_dong");
            }
        });
        ChupTrongGame.datTinh(td, "nguongHp", Integer.valueOf(0));
        ChupTrongGame.datTinh(td, "nguongMp", Integer.valueOf(0));
        veKhung(ten + "_mo");
        int[] v = vung(ten);
        kiem(v != null, ten + ": có vùng chạm thanh trượt");
        if (v == null) return;
        int x = v[0], y = v[1], w = v[2], h = v[3], cy = y + h / 2;
        int luu0 = docTinh(td, truong);
        // nhấn ở đầu trái thanh
        con(0, x + 2, cy);
        ngu(120);   // vài nhịp game: bảng xoá Canvas.isPointerDown (lỗi cũ: núm đứng yên từ đây)
        veKhung(ten + "_nhan");
        int truocGt = game.pokemon.Bang.nhanGt, truocNut = game.pokemon.Bang.nhanTamNut;
        kiem(ten.equals(game.pokemon.Bang.nhanTen), ten + ": thanh nhận cú nhấn (" + game.pokemon.Bang.nhanTen + ")");
        int khac = 0;
        for (int i = 1; i <= 6; i++) {
            int px = x + w * i / 7;
            con(1, px, cy + (i % 2 == 0 ? 3 : -2));   // kéo lệch lên xuống chút như ngón tay thật
            ngu(90);
            veKhung(ten + "_keo" + i);
            int gt = game.pokemon.Bang.nhanGt, nut = game.pokemon.Bang.nhanTamNut;
            int mong = game.pokemon.Bang.giaTriTruot(px, x, w, h, 0, max, buoc);
            kiem(gt == mong, ten + ": kéo lần " + i + " giá trị hiện = giá trị tại ngón (" + gt + " / " + mong + ")");
            kiem(gt >= truocGt && nut >= truocNut, ten + ": kéo lần " + i + " núm đi theo sang phải (" + truocNut + " -> " + nut + ")");
            if (gt != truocGt) khac++;
            truocGt = gt;
            truocNut = nut;
            kiem(docTinh(td, truong) == luu0, ten + ": đang kéo chưa lưu (lưu khi thả)");
        }
        kiem(khac >= Math.min(3, max - 1), ten + ": giá trị đổi liên tục khi kéo (" + khac + " lần đổi / 6 lần kéo)");
        // kéo quá cuối thanh + lệch xuống ngoài hàng: kẹp max, thả ngoài thanh vẫn lưu
        con(1, x + w + 40, y + h + 12);
        ngu(90);
        veKhung(ten + "_qua_cuoi");
        kiem(game.pokemon.Bang.nhanGt == max, ten + ": kéo quá cuối = max (" + game.pokemon.Bang.nhanGt + ")");
        con(2, x + w + 40, y + h + 12);
        ngu(250);
        int luu = docTinh(td, truong);
        kiem(luu == (truong.equals("vungO") ? vungO()[max] : max), ten + ": thả ngoài thanh vẫn lưu giá trị kéo tới (" + luu + ")");
        veKhung(ten + "_tha");
        kiem(game.pokemon.Bang.nhanGt == max, ten + ": thả xong núm không giật về giá trị cũ (" + game.pokemon.Bang.nhanGt + ")");
        // nhấn giữa, kéo về quá đầu trái -> 0
        con(0, x + w / 2, cy);
        ngu(90);
        con(1, x + w / 4, cy);
        ngu(90);
        veKhung(ten + "_ve");
        kiem(game.pokemon.Bang.nhanGt == game.pokemon.Bang.giaTriTruot(x + w / 4, x, w, h, 0, max, buoc), ten + ": kéo ngược lại theo ngón");
        con(1, x - 30, cy);
        ngu(90);
        con(2, x - 30, cy);
        ngu(250);
        luu = docTinh(td, truong);
        kiem(luu == (truong.equals("vungO") ? vungO()[0] : 0), ten + ": kéo về quá đầu = 0 (" + luu + ")");
        // chạm một cái (không kéo) giữa thanh: lưu giá trị tại chỗ chạm
        int xc = x + w * 3 / 4;
        con(0, xc, cy);
        ngu(90);
        con(2, xc, cy);
        ngu(250);
        int mong = game.pokemon.Bang.giaTriTruot(xc, x, w, h, 0, max, buoc);
        luu = docTinh(td, truong);
        kiem(luu == (truong.equals("vungO") ? vungO()[mong] : mong), ten + ": chạm một cái lưu giá trị tại chỗ chạm (" + luu + ")");
        ChupTrongGame.datTinh(Class.forName("game.render.screen.BangTuDong"), "mo", Boolean.FALSE);
    }

    // ------------------------------------------------------------------ B. đổi cỡ cửa sổ

    static void doiCo(final htbb.pc.ManHinh m, final int w, final int h) throws Exception {
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                m.setSize(w, h);
                try {
                    java.lang.reflect.Method d = htbb.pc.ManHinh.class.getDeclaredMethod("doiCo");
                    d.setAccessible(true);
                    d.invoke(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        // chờ luồng sự kiện vẽ ít nhất 2 khung mới ở cỡ mới (game tự yêu cầu vẽ mỗi nhịp)
        long k0 = NenPc.soKhung;
        for (int i = 0; i < 100 && NenPc.soKhung < k0 + 3; i++) {
            NenPc.luong().yeuCauVe();
            ngu(20);
        }
    }

    static void kiemCo(htbb.pc.ManHinh m, int w, int h, String nhan) throws Exception {
        doiCo(m, w, h);
        String t = nhan + " " + w + "x" + h;
        kiem(NenPc.cuaSoW == w && NenPc.cuaSoH == h, t + ": NenPc nhận cỡ cửa sổ");
        kiem(NenPc.rong <= 640 && NenPc.cao <= 360 && (NenPc.rong == 640 || NenPc.cao == 360), t + ": logic <= 640x360 (" + NenPc.rong + "x" + NenPc.cao + ")");
        kiem(NenPc.hienX == 0 && NenPc.hienY == 0 && NenPc.hienW >= w && NenPc.hienH >= h, t + ": khung phủ kín cửa sổ (" + NenPc.hienW + "x" + NenPc.hienH + ")");
        kiem(NenPc.thatW == NenPc.rong * NenPc.heSo && NenPc.thatH == NenPc.cao * NenPc.heSo, t + ": bộ đệm = logic x k");
        float s = Math.max(w / 640f, h / 360f);
        kiem(NenPc.heSo == Math.max(1, (int) Math.ceil(s - 1e-3f)), t + ": hệ số k = ceil(s) (" + NenPc.heSo + ")");
        kiem(game.render.Canvas.w == NenPc.rong && game.render.Canvas.h == NenPc.cao, t + ": game nhận cỡ logic mới (" + game.render.Canvas.w + "x" + game.render.Canvas.h + ")");
        java.awt.Image sau = m.demSau();
        kiem(sau != null && sau.getWidth(null) == NenPc.thatW && sau.getHeight(null) == NenPc.thatH,
                t + ": khung game vẽ ở cỡ MỚI (" + (sau == null ? "null" : sau.getWidth(null) + "x" + sau.getHeight(null)) + ")");
        int[] kt = m.coKhungTruoc();
        kiem(kt != null && kt[0] == NenPc.thatW && kt[1] == NenPc.thatH,
                t + ": khung đưa lên trình bày ở cỡ MỚI (" + (kt == null ? "null" : kt[0] + "x" + kt[1]) + ")");
        BufferedImage a = m.trinhBayVaoAnh();
        kiem(a.getWidth() == w && a.getHeight() == h, t + ": bề mặt trình bày đúng cỡ cửa sổ");
        int n = 0, den = 0;
        java.util.HashSet<Integer> mau = new java.util.HashSet<Integer>();
        int[][] vungMau = {{0, 0, w, h}, {w * 2 / 3, h * 2 / 3, w - w * 2 / 3, h - h * 2 / 3}};   // cả cửa sổ + góc dưới phải (phần to thêm)
        for (int[] r : vungMau) {
            for (int y = r[1] + 1; y < r[1] + r[3]; y += 9) {
                for (int x = r[0] + 1; x < r[0] + r[2]; x += 9) {
                    int c = a.getRGB(x, y) & 0xFFFFFF;
                    n++;
                    if (c == 0) den++;
                    mau.add(c);
                }
            }
        }
        kiem(n > 0 && den * 100 / n < 3, t + ": không đen (" + (n == 0 ? 0 : den * 100 / n) + "% điểm đen)");
        // nền chờ (chưa có khung game) chỉ là dải chuyển màu ~50 màu; khung game thật có hàng trăm màu
        kiem(mau.size() > 150, t + ": có hình game (" + mau.size() + " màu)");
        if (ra != null) javax.imageio.ImageIO.write(a, "png", new File(ra, "co_" + w + "x" + h + ".png"));
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "kiem_truot_doi_co");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;
                    MIDlet.batDau((MIDlet) Class.forName("game.GameMidlet").newInstance());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) ngu(100);
        ngu(2500);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.giaLap();
            }
        });
        ngu(800);

        // ---- A. thanh trượt
        String[] kieu = {"cham", "chuot"};
        for (String k : kieu) {
            KIEU = k;
            keoThanh("td-thuoc-hp", "nguongHp", 90, 10);
            keoThanh("td-thuoc-pp", "nguongMp", 90, 10);
            keoThanh("td-vung", "vungO", soVung() - 1, 1);
        }

        // ---- B. đổi cỡ cửa sổ (đường vẽ cửa sổ thật, bề mặt = ảnh)
        KIEU = "cua_so";
        kiem(htbb.pc.ManHinh.canTaoLai(true, 960, 540, 1280, 720, false), "canTaoLai: to ra -> tạo lại bề mặt");
        kiem(htbb.pc.ManHinh.canTaoLai(true, 1280, 720, 960, 540, false), "canTaoLai: nhỏ lại -> tạo lại bề mặt");
        kiem(htbb.pc.ManHinh.canTaoLai(true, 1280, 720, 1280, 646, false), "canTaoLai: phóng to (cao khác) -> tạo lại");
        kiem(!htbb.pc.ManHinh.canTaoLai(true, 1280, 720, 1280, 720, false), "canTaoLai: cùng cỡ -> giữ");
        kiem(htbb.pc.ManHinh.canTaoLai(true, 1280, 720, 1280, 720, true), "canTaoLai: vừa lỗi -> tạo lại");
        kiem(htbb.pc.ManHinh.canTaoLai(false, -1, -1, 1280, 720, false), "canTaoLai: chưa có -> tạo");
        final htbb.pc.ManHinh[] mh = new htbb.pc.ManHinh[1];
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                mh[0] = htbb.pc.ManHinh.khongCuaSo();
            }
        });
        int[][] co = {{960, 540}, {800, 450}, {1280, 720}, {1600, 900}, {1280, 646}, {1920, 1080}, {2560, 1080}, {1366, 705},
            {1024, 768}, {2560, 1440}, {960, 540}, {1920, 1080}, {700, 400}};
        String[] nhan = {"dau", "nho hon", "to hon dau", "Cai dat 1600x900", "phong to", "toan man", "21:9", "phong to 1366",
            "4:3", "2K", "thu lai", "to lai", "nho nhat"};
        for (int i = 0; i < co.length; i++) {
            kiemCo(mh[0], co[i][0], co[i][1], nhan[i]);
        }
        System.out.println("KiemTruotDoiCo: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
