import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * HÀNH TRÌNH NGƯỜI CHƠI THẬT + SOI GIAO DIỆN (31/10/2026, chủ dự án: "cho bot vừa chơi vừa check giao diện luôn xem có lỗi
 * hay chỗ nào thao tác khó"). Client PC thật (cùng code jar / APK), headless, nối MÁY CHỦ THẬT (stack thử riêng, CSDL nạp
 * sql/htbb.sql): đăng ký trong game -> tạo Pokémon -> chọn -> vào làng -> nhiệm vụ / NPC / Sổ tay / bản đồ vùng / cửa hàng /
 * hành trang / kỹ năng / tự động / chat / đội... Thao tác bằng CHẠM vùng chạm thật (VungCham, như ngón tay) và đếm số chạm
 * mỗi việc hay làm. Mỗi bước chụp ở 7 cỡ (APK 1080x1920 d3 / 720x1600 d2, PC 720p / 1080p, J2ME 320x240 / 240x320 / 640x360)
 * và tự soi:
 *  - chữ tràn khung / đè chữ / đè biểu tượng, ký tự thiếu nét (ChupTrongGame.kiemChu), khung / biểu tượng ra ngoài màn;
 *  - vùng chạm lớp trên: >= 44 dp (APK) / 32 px (PC), cách nhau, trong màn, bấm tâm trúng (ChupTrongGame.kiemCham);
 *  - chữ bị cắt "..": NHÃN NÚT / TAB (chuỗi ngắn bị cắt) ở cỡ điện thoại / PC;
 *  - tương phản chữ / nền (màu chữ ghi lúc vẽ so với màu nền chiếm nhiều nhất trong hộp chữ) < 3:1;
 *  - tiếng Việt thiếu dấu ("khong", "duoc", "nhiem vu"...), chuỗi giữ chỗ (null, TODO, %s, {0});
 *  - màn trống (không chữ nào ở bước không phải thế giới).
 * Kết quả: ảnh <bước>_<cỡ>.png, bao_cao.tsv (mỗi phát hiện một dòng: bước, cỡ, loại, chi tiết), so_cham.tsv (số chạm mỗi
 * việc), dòng "KET QUA". Mã thoát 1 = có lỗi loại HONG (tràn / chạm / màn trống); NHAC (cắt nhãn, tương phản) chỉ báo.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -sourcepath client_pc/test:server/src -d $O \
 *       client_pc/test/KiemHanhTrinhUI.java
 *   java -Djava.awt.headless=true -Dhtbb.mayChu=$PWD/server [-Dhanh.host=...] [-Dhanh.gm=<tk GM>] \
 *       -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemHanhTrinhUI <cổng game> <thư mục ảnh>
 * (hoặc sh tools/pokemon/kiem_tai/kiem_hanh_trinh_ui.sh <host> <cổng> <thư mục ra> [tk GM]). -Dhanh.gm = phần 2: tài
 * khoản GM có sẵn (mk 123456) dịch chuyển tới nơi người chơi tới sau nhiều giờ (Pewter, Rừng Viridian, Đấu trường...).
 * -Dhanh.co=apk720x1600d2,j2me240x320 chụp một phần cỡ; -Dhanh.buoc=... chỉ chụp các bước có tên chứa chuỗi này.
 */
public class KiemHanhTrinhUI {
    static File RA;
    static PrintWriter BAO;
    static int soHong, soNhac, soBuoc;
    static final Map<String, Integer> SO_CHAM = new LinkedHashMap<String, Integer>();
    static final Map<String, String> CHAM_CHI_TIET = new LinkedHashMap<String, String>();
    static String viecDangDem;
    static int chamDangDem;
    static StringBuilder chamDuong = new StringBuilder();

    /** {tên, kiểu (0 apk, 1 pc, 2 j2me), W, H, dpi, tai thỏ} */
    static final Object[][] CO = {
        {"apk1080x1920d3", 0, 1920, 1080, 480, 0},
        {"apk720x1600d2", 0, 1600, 720, 320, 0},
        {"pc720", 1, 1280, 720, 0, 0},
        {"pc1080", 1, 1920, 1080, 0, 0},
        {"j2me320x240", 2, 320, 240, 0, 0},
        {"j2me240x320", 2, 240, 320, 0, 0},
        {"j2me640x360", 2, 640, 360, 0, 0}};

    static void bao(String buoc, String co, String loai, String chiTiet) {
        boolean hong = loai.startsWith("HONG");
        if (hong) soHong++;
        else soNhac++;
        String d = buoc + "\t" + co + "\t" + loai + "\t" + chiTiet.replace('\t', ' ').replace('\n', '|');
        BAO.println(d);
        BAO.flush();
        if (hong) System.out.println("HONG: " + d);
    }

    static void tren(Runnable r) throws Exception {
        KiemTaiThat.tren(r);
    }

    static void ngu(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // bỏ
        }
    }

    interface DieuKien {
        boolean dung();
    }

    static boolean cho(DieuKien d, int ms) {
        long het = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < het) {
            final boolean[] ok = {false};
            try {
                final DieuKien dd = d;
                tren(new Runnable() {
                    public void run() {
                        ok[0] = dd.dung();
                    }
                });
            } catch (Exception e) {
                // bỏ
            }
            if (ok[0]) return true;
            ngu(100);
        }
        return false;
    }

    // ------------------------------------------------------------------ cỡ màn

    static Object boChu;
    static int coDangDat = -1;

    static void datCo(int i) throws Exception {
        final Object[] c = CO[i];
        int kieu = (Integer) c[1], W = (Integer) c[2], H = (Integer) c[3], dpi = (Integer) c[4], tai = (Integer) c[5];
        game.pokemon.Bang.anPhai = 0;
        game.pokemon.Bang.anTrai = 0;
        if (kieu == 2) {
            ChupTrongGame.datCoJ2me(W, H);
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            if (game.pokemon.ChuUI.bo != null) boChu = game.pokemon.ChuUI.bo;
            game.pokemon.ChuUI.bo = null;
            game.pokemon.VungCham.MIN = 0;
            game.pokemon.VungCham.CACH = 0;
        } else {
            htbb.pc.GiaoDienHD.bat = true;
            htbb.pc.ChuHD.bat = true;
            if (game.pokemon.ChuUI.bo == null && boChu != null) game.pokemon.ChuUI.bo = (game.pokemon.ChuUI) boChu;
            if (kieu == 0) {
                ChupTrongGame.datCoApk(W, H, dpi, tai);
            } else {
                NenPc.tinhKichThuoc(W, H);
                float s = NenPc.hienW / (float) NenPc.rong;
                game.pokemon.VungCham.MIN = (int) Math.ceil(32 / s);
                game.pokemon.VungCham.CACH = (int) Math.ceil(8 / s);
            }
        }
        coDangDat = i;
        tren(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
            }
        });
    }

    /** cỡ thao tác (chạm): điện thoại 1080p d3 */
    static void coGoc() throws Exception {
        datCo(0);
    }

    // ------------------------------------------------------------------ chụp + soi

    static final String LOC_CO = System.getProperty("hanh.co"), LOC_BUOC = System.getProperty("hanh.buoc");
    static final List<String> BUOC = new ArrayList<String>();

    /** Chụp bước ở mọi cỡ + soi. theGioi: bước màn chơi (không đòi có chữ). */
    static void chup(String buoc, boolean theGioi) throws Exception {
        soBuoc++;
        String ten = String.format("%02d_%s", soBuoc, buoc);
        BUOC.add(ten + "\t" + KiemTaiThat.trangThai());
        System.out.println("BUOC " + ten + " " + KiemTaiThat.trangThai());
        if (LOC_BUOC != null && !buoc.contains(LOC_BUOC)) return;
        ngu(400);
        dangChup = true;
        try {
            chupMoiCo(ten, theGioi);
        } finally {
            dangChup = false;
        }
    }

    static volatile boolean dangChup;

    static void chupMoiCo(String ten, boolean theGioi) throws Exception {
        for (int i = 0; i < CO.length; i++) {
            final String co = (String) CO[i][0];
            if (LOC_CO != null && !("," + LOC_CO + ",").contains("," + co + ",")) continue;
            datCo(i);
            ngu(150);
            int truoc = ChupTrongGame.hong;
            int W = (Integer) CO[i][2], H = (Integer) CO[i][3];
            ChupTrongGame.giaMayChuAnh();
            game.pokemon.VeTay.xoa();
            game.pokemon.GhiChu.xoa();
            game.pokemon.GhiChu.bat = true;
            game.render.screen.Font.thieuNet = 0;
            BufferedImage anh = ChupTrongGame.ve(W, H);
            game.pokemon.GhiChu.bat = false;
            ChupTrongGame.kiemChu(ten + " " + co);
            ChupTrongGame.kiemTrongMan(ten + " " + co);
            int kieu = (Integer) CO[i][1];
            // HUD màn chơi ở cỡ APK: APK thật dùng HUD riêng (client_apk Hud.java, kiểm bằng client_jar/tools/KiemCoCham) -
            // HUD PC (HudRong) vẽ ở đây chỉ để thấy bố cục, không kiểm cỡ chạm (như ChupTrongGame)
            boolean hudTren = false;
            for (String v : vungCo()) if (v.startsWith("*hud-")) hudTren = true;
            if (kieu == 1 || kieu == 0 && !hudTren) ChupTrongGame.kiemCham(ten + " " + co, game.pokemon.VungCham.MIN, game.pokemon.VungCham.CACH);
            // HỎNG của ChupTrongGame trong lần soi này -> báo cáo (chúng in "HONG: ..." sẵn)
            for (int k = truoc; k < ChupTrongGame.hong; k++) soHong++;
            if (ChupTrongGame.hong > truoc) {
                bao(ten, co, "HONG_BO_CUC", (ChupTrongGame.hong - truoc) + " loi bo cuc / cham (xem dong HONG ngay tren)");
            }
            soiChu(ten, co, anh, kieu, theGioi);
            ImageIO.write(anh, "png", new File(RA, ten + "_" + co + ".png"));
        }
        coGoc();
    }

    static final String[] KHONG_DAU = {"khong", "duoc", "nguoi", "nhiem vu", "chua", "thuoc", "nhung", "nhieu", "tien", "Khong",
        "Nhiem vu", "Thanh cong", "that bai", "Ban da", "ban da", "Xin chao", "cap do", "trang bi", "Trang bi", "hanh trang", "cua hang",
        "Cua hang", "nhan duoc", "Nhan", "Chua", "Dong y", "Huy bo", "Tro ve", "Quay lai"};
    static final String[] GIU_CHO = {"null", "TODO", "%s", "{0}", "undefined", "NaN"};   // "???" = Pokémon chưa gặp (Pokédex, chủ ý)

    static boolean coTu(String s, String tu) {
        int i = s.indexOf(tu);
        while (i >= 0) {
            boolean dau = i == 0 || !Character.isLetter(s.charAt(i - 1));
            int j = i + tu.length();
            boolean cuoi = j >= s.length() || !Character.isLetter(s.charAt(j));
            if (dau && cuoi) return true;
            i = s.indexOf(tu, i + 1);
        }
        return false;
    }

    static double doSang(int rgb) {
        double[] c = new double[3];
        for (int k = 0; k < 3; k++) {
            double v = ((rgb >> (16 - 8 * k)) & 0xFF) / 255.0;
            c[k] = v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
    }

    static double tuongPhan(int a, int b) {
        double x = doSang(a), y = doSang(b);
        return (Math.max(x, y) + 0.05) / (Math.min(x, y) + 0.05);
    }

    /**
     * Soi chuỗi đã vẽ: nhãn ngắn bị cắt, thiếu dấu, giữ chỗ, màn trống, tương phản (ảnh thật: màu nền = màu chiếm nhiều nhất
     * trong hộp chữ; màu chữ = màu xa nền nhất trong các màu chiếm >= 4% hộp).
     */
    static void soiChu(String ten, String co, BufferedImage anh, int kieu, boolean theGioi) {
        int n = game.pokemon.GhiChu.soChu;
        if (!theGioi && n == 0) bao(ten, co, "HONG_MAN_TRONG", "khong ve chu nao");
        float sx = anh.getWidth() / (float) Math.max(1, game.render.Canvas.w);
        float sy = anh.getHeight() / (float) Math.max(1, game.render.Canvas.h);
        java.util.HashSet<String> daBao = new java.util.HashSet<String>();
        for (int i = 0; i < n; i++) {
            String s = game.pokemon.GhiChu.CS[i];
            if (s == null || !daBao.add(s)) continue;
            String goc = s.trim();
            if ((goc.endsWith("..") || goc.endsWith("…")) && goc.length() <= 16) {
                bao(ten, co, "NHAC_CAT_NHAN", "\"" + s + "\" (" + game.pokemon.GhiChu.CW[i] + "px)");
            }
            for (String t : KHONG_DAU) {
                if (coTu(s, t)) {
                    bao(ten, co, "HONG_KHONG_DAU", "\"" + s + "\" (" + t + ")");
                    break;
                }
            }
            for (String t : GIU_CHO) {
                if (s.contains(t)) bao(ten, co, "HONG_GIU_CHO", "\"" + s + "\"");
            }
            // tương phản
            int x0 = Math.round(game.pokemon.GhiChu.CX[i] * sx), y0 = Math.round(game.pokemon.GhiChu.CY[i] * sy);
            int w = Math.round(game.pokemon.GhiChu.CW[i] * sx), h = Math.round(game.pokemon.GhiChu.CH[i] * sy);
            if (w < 4 || h < 4 || x0 < 0 || y0 < 0 || x0 + w > anh.getWidth() || y0 + h > anh.getHeight()) continue;
            Map<Integer, Integer> dem = new java.util.HashMap<Integer, Integer>();
            int tong = 0;
            for (int y = y0; y < y0 + h; y++) {
                for (int x = x0; x < x0 + w; x++) {
                    int c = anh.getRGB(x, y) & 0xF0F0F0;   // gộp màu gần nhau
                    Integer v = dem.get(c);
                    dem.put(c, v == null ? 1 : v + 1);
                    tong++;
                }
            }
            int nen = 0, nenN = -1;
            for (Map.Entry<Integer, Integer> e : dem.entrySet()) {
                if (e.getValue() > nenN) {
                    nenN = e.getValue();
                    nen = e.getKey();
                }
            }
            double tot = 1;
            for (Map.Entry<Integer, Integer> e : dem.entrySet()) {
                if (e.getValue() < Math.max(3, tong / 60) || e.getKey() == nen) continue;
                tot = Math.max(tot, tuongPhan(e.getKey(), nen));
            }
            if (nenN * 10 < tong * 9 && tot < 2.5 && !theGioi) {
                bao(ten, co, "NHAC_TUONG_PHAN", String.format("\"%s\" %.1f:1 nen #%06X", s, tot, nen));
            }
        }
    }

    // ------------------------------------------------------------------ thao tác (đếm chạm)

    static void batDauViec(String viec) {
        viecDangDem = viec;
        chamDangDem = 0;
        chamDuong.setLength(0);
    }

    static void xongViec() {
        if (viecDangDem != null) {
            SO_CHAM.put(viecDangDem, chamDangDem);
            CHAM_CHI_TIET.put(viecDangDem, chamDuong.toString());
            System.out.println("SO CHAM " + viecDangDem + " = " + chamDangDem + " (" + chamDuong + ")");
        }
        viecDangDem = null;
    }

    static List<String> vungCo() {
        List<String> l = new ArrayList<String>();
        int top = game.pokemon.VungCham.lopCaoNhat();
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            String t = game.pokemon.VungCham.ten(i);
            l.add((game.pokemon.VungCham.lopVung(i) == top ? "*" : "") + t);
        }
        return l;
    }

    /** Vẽ một khung ở cỡ gốc (đăng ký vùng chạm) rồi in tên vùng (gỡ lỗi kịch bản). */
    static void inVung(String nhan) throws Exception {
        ChupTrongGame.ve(1920, 1080);
        System.out.println("VUNG " + nhan + ": " + vungCo());
    }

    /** Chạm vùng có tên bắt đầu bằng t: ngón tay thật ở tâm vùng (vẽ một khung trước để vùng đúng). Không thấy = HỎNG kịch bản. */
    static boolean cham(final String t) throws Exception {
        ChupTrongGame.ve(1920, 1080);
        final int[] tam = {-1, -1};
        tren(new Runnable() {
            public void run() {
                int top = game.pokemon.VungCham.lopCaoNhat();
                for (int pass = 0; pass < 2 && tam[0] < 0; pass++) {
                    for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
                        String n = game.pokemon.VungCham.ten(i);
                        if (n != null && n.startsWith(t) && (pass == 1 || game.pokemon.VungCham.lopVung(i) == top)) {
                            int[] v = game.pokemon.VungCham.vungVe(i);
                            tam[0] = v[0] + v[2] / 2;
                            tam[1] = v[1] + v[3] / 2;
                            break;
                        }
                    }
                }
            }
        });
        if (tam[0] < 0) {
            System.out.println("KHONG THAY VUNG '" + t + "' trong " + vungCo() + " | " + KiemTaiThat.trangThai());
            bao("kich_ban", "-", "HONG_KICH_BAN", "khong thay vung cham '" + t + "' (" + KiemTaiThat.trangThai() + ")");
            return false;
        }
        chamThat(tam[0], tam[1], t);
        return true;
    }

    /** Chạm điểm (x, y) logic ở cỡ gốc (vùng không tên / thế giới). */
    static void chamDiem(final int x, final int y, String ghi) throws Exception {
        chamThat(x, y, ghi);
    }

    /** Chuỗi có đang được vẽ trên màn (một khung ở cỡ gốc). */
    static boolean dangVeChu(final String chu) throws Exception {
        game.pokemon.GhiChu.xoa();
        game.pokemon.GhiChu.bat = true;
        ChupTrongGame.ve(1920, 1080);
        game.pokemon.GhiChu.bat = false;
        for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
            String s = game.pokemon.GhiChu.CS[i];
            if (s != null && s.contains(chu)) {
                return true;
            }
        }
        return false;
    }

    static void chamTheoChu(final String chu) throws Exception {
        // chạm vùng chạm nào chứa chuỗi chữ đã vẽ (nút / hàng menu không tên)
        game.pokemon.GhiChu.xoa();
        game.pokemon.GhiChu.bat = true;
        ChupTrongGame.ve(1920, 1080);
        game.pokemon.GhiChu.bat = false;
        int[] v = null;
        for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
            String s = game.pokemon.GhiChu.CS[i];
            if (s != null && s.contains(chu)) {
                v = new int[]{game.pokemon.GhiChu.CX[i] + game.pokemon.GhiChu.CW[i] / 2, game.pokemon.GhiChu.CY[i] + game.pokemon.GhiChu.CH[i] / 2};
            }
        }
        if (v == null) {
            bao("kich_ban", "-", "HONG_KICH_BAN", "khong thay chu '" + chu + "' (" + KiemTaiThat.trangThai() + ")");
            return;
        }
        chamDiem(v[0], v[1], "\"" + chu + "\"");
    }

    static void gui(final int lenh, final Object... f) throws Exception {
        ChupThat.gui(lenh, f);
    }

    // ------------------------------------------------------------------ kịch bản

    public static void main(String[] a) throws Exception {
        final int cong = Integer.parseInt(a[0]);
        RA = new File(a[1]);
        RA.mkdirs();
        BAO = new PrintWriter(new FileWriter(new File(RA, "bao_cao.tsv")));
        BAO.println("buoc\tco\tloai\tchi_tiet");
        File du = new File(RA, "du_lieu");
        System.setProperty("java.awt.headless", "true");
        System.setProperty("htbb.duLieu", du.getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        final String host = System.getProperty("hanh.host", "127.0.0.1");
        MIDlet.GHI_DE.put("HTBB-May-Chu", host + ":" + cong);
        htbb.pc.ChuHD.ghi = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
        coGoc();
        tren(new Runnable() {
            public void run() {
                try {
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        coGoc();
        // máy thật vẽ ~30 khung / giây: thông báo hết giờ, hiệu ứng chạy, vùng chạm cập nhật. Headless không có cửa sổ
        // nên tự bơm khung (không bơm lúc đang chụp nhiều cỡ).
        Thread bom = new Thread(new Runnable() {
            public void run() {
                final java.util.concurrent.atomic.AtomicBoolean cho = new java.util.concurrent.atomic.AtomicBoolean();
                while (true) {
                    ngu(40);
                    if (dangChup || game.render.Canvas.instance == null || !cho.compareAndSet(false, true)) continue;
                    NenPc.luong().them(new Runnable() {
                        public void run() {
                            try {
                                if (!dangChup) ChupTrongGame.veThang();
                            } finally {
                                cho.set(false);
                            }
                        }
                    });
                }
            }
        });
        bom.setDaemon(true);
        bom.start();
        for (int i = 0; i < 300 && !(game.render.Canvas.currentScreen instanceof game.render.screen.LoginScr); i++) {
            if (i == 30) tren(new Runnable() {
                public void run() {
                    game.render.Canvas.loginScr.switchToMe();
                }
            });
            ngu(100);
        }
        tren(new Runnable() {
            public void run() {
                game.render.screen.ServerListScr.address = new String[]{host};
                game.render.screen.ServerListScr.port = new short[]{(short) cong};
                game.render.screen.ServerListScr.index = 0;
            }
        });
        try {
            if (System.getProperty("hanh.gm") != null) hanhTrinhGm(System.getProperty("hanh.gm"));
            else hanhTrinh(a.length > 2 ? a[2] : null);
        } catch (Throwable e) {
            e.printStackTrace();
            bao("kich_ban", "-", "HONG_KICH_BAN", "ngoai le: " + e);
        }
        ket();
    }

    static final String TK = System.getProperty("hanh.tk", "ui" + (System.currentTimeMillis() / 1000 % 1000000));

    static void hanhTrinh(String tkGm) throws Exception {
        chup("dang_nhap", false);
        inVung("dang_nhap");
        batDauViec("dang_ky_tai_khoan");
        cham("dang-ky");
        chup("dang_ky", false);
        final game.render.screen.DangKyScr dk = game.render.screen.DangKyScr.gI();
        tren(new Runnable() {
            public void run() {
                try {
                    java.lang.reflect.Field f = game.render.screen.DangKyScr.class.getDeclaredField("o");
                    f.setAccessible(true);
                    game.render.screen.TField[] o = (game.render.screen.TField[]) f.get(dk);
                    o[0].setText(TK);
                    o[1].setText("123456");
                    o[2].setText("123456");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        chamDangDem += 3;   // ba ô nhập (gõ bàn phím)
        chamDuong.append(" > [go 3 o]");
        chup("dang_ky_da_dien", false);
        cham("dang-ky-1");
        cho(new DieuKien() {
            public boolean dung() {
                return game.render.Canvas.currentDialog != null && game.render.Canvas.currentDialog == game.render.Canvas.inputDlg;
            }
        }, 8000);
        chup("dang_ky_cau_do", false);
        ngu(2500);   // máy chủ chặn trả lời quá nhanh (người thật đọc câu hỏi)
        tren(new Runnable() {
            public void run() {
                String q = game.render.Canvas.inputDlg.tfInput != null ? cauHoi() : "";
                game.render.Canvas.inputDlg.tfInput.setText(String.valueOf(giai(q)));
            }
        });
        chamDangDem++;
        chamDuong.append(" > [go dap an]");
        inVung("cau_do");
        chamChinh();
        cho(new DieuKien() {
            public boolean dung() {
                return game.render.Canvas.currentScreen instanceof game.render.screen.LoginScr && game.render.Canvas.currentDialog != null;
            }
        }, 10000);
        chup("dang_ky_xong", false);
        inVung("dang_ky_xong");
        xongViec();
        // ---- đăng nhập ngay từ hộp "Đăng ký thành công" -> tạo Pokémon
        batDauViec("tao_pokemon");
        chamChinh();
        cho(new DieuKien() {
            public boolean dung() {
                Object m = game.render.Canvas.currentScreen;
                return (m instanceof game.render.screen.CreateCharScr || m instanceof game.render.screen.CharSelectScr)
                        && game.render.Canvas.currentDialog == null;
            }
        }, 30000);
        if (game.render.Canvas.currentScreen instanceof game.render.screen.CharSelectScr) {
            chup("chon_pokemon_trong", false);
            cham("the-0");
        }
        chup("tao_pokemon", false);
        inVung("tao_pokemon");
        cham("khoi-dau-1");
        tren(new Runnable() {
            public void run() {
                game.render.screen.CreateCharScr.gI().tfChat.setText(TK.length() > 10 ? TK.substring(0, 10) : TK);
            }
        });
        chamDangDem++;
        chamDuong.append(" > [go ten]");
        chup("tao_pokemon_da_chon", false);
        cham("tao-1");
        cho(new DieuKien() {
            public boolean dung() {
                Object m = game.render.Canvas.currentScreen;
                return m instanceof game.render.screen.CharSelectScr || m == game.render.Canvas.gameScr && !game.pokemon.ManTai.dangVao;
            }
        }, 30000);
        if (game.render.Canvas.currentScreen instanceof game.render.screen.CharSelectScr) {
            chup("chon_pokemon", false);
            cham("chon-1");
        }
        cho(new DieuKien() {
            public boolean dung() {
                return game.render.Canvas.currentScreen == game.render.Canvas.gameScr && !game.pokemon.ManTai.dangVao
                        && game.render.Canvas.gameScr.mainChar != null;
            }
        }, 60000);
        xongViec();
        ngu(3000);
        chup("vao_the_gioi", true);
        inVung("the_gioi");
        for (int i = 0; i < 6 && game.render.Canvas.currentDialog != null; i++) {
            chup("hop_dau_" + i, false);
            inVung("hop_dau_" + i);
            chamChinh();
            ngu(1500);
        }
        ngu(2000);
        chup("san_nha", true);
        inVung("san_nha");
        hanhTrinh2(tkGm);
    }

    // ------------------------------------------------------------------ chạm thật (sự kiện con trỏ của Canvas)

    static void goiCanvas(String ten, int x, int y) {
        try {
            java.lang.reflect.Method m = game.render.Canvas.class.getDeclaredMethod(ten, int.class, int.class);
            m.setAccessible(true);
            m.invoke(game.render.Canvas.instance, x, y);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /** Một cú chạm ngón tay thật (nhấn / nhả) ở toạ độ logic (cỡ gốc). */
    static void chamThat(final int x, final int y, String ghi) throws Exception {
        ChupTrongGame.ve(1920, 1080);
        tren(new Runnable() {
            public void run() {
                goiCanvas("pointerPressed", x, y);
            }
        });
        ngu(90);
        tren(new Runnable() {
            public void run() {
                goiCanvas("pointerReleased", x, y);
            }
        });
        chamDangDem++;
        chamDuong.append(chamDuong.length() == 0 ? "" : " > ").append(ghi);
        ngu(350);
    }

    static void phim(final int ma, String ghi) throws Exception {
        tren(new Runnable() {
            public void run() {
                try {
                    java.lang.reflect.Method m = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("keyPressed", int.class);
                    m.setAccessible(true);
                    m.invoke(game.render.Canvas.instance, ma);
                    java.lang.reflect.Method r = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("keyReleased", int.class);
                    r.setAccessible(true);
                    r.invoke(game.render.Canvas.instance, ma);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        chamDangDem++;
        chamDuong.append(chamDuong.length() == 0 ? "" : " > ").append("[" + ghi + "]");
        ngu(300);
    }

    static String loiNpc() {
        try {
            java.lang.reflect.Field f = game.pokemon.HopThoaiDuoi.class.getDeclaredField("dong");
            f.setAccessible(true);
            return java.util.Arrays.toString((String[]) f.get(null));
        } catch (Throwable e) {
            return "?";
        }
    }

    static String moTaHop() {
        Object d = game.render.Canvas.currentDialog;
        if (d == null && game.pokemon.HopThoaiDuoi.dangBat()) return "noi:" + loiNpc();
        if (d == null) return null;
        if (d instanceof game.pokemon.MenuNguCanh) return "menu:" + game.pokemon.MenuNguCanh.tieuDe() + java.util.Arrays.toString(game.pokemon.MenuNguCanh.cacLenh());
        return d.getClass().getSimpleName() + KiemTaiThat.noiDung(d);
    }

    static final String[] UU_TIEN = {"Làm", "Nhận nhiệm vụ", "Nhận", "Trả nhiệm vụ", "Trả", "Đồng ý", "Tiếp", "OK"};

    /**
     * Xử lý mọi hộp đang hiện như người chơi: lời NPC (chạm để tiếp), menu thẻ (chọn mục ưu tiên), hộp thông báo / hỏi
     * (nút chính). Mỗi hộp KHÁC nhau chụp một lần. Trả số hộp đã qua.
     */
    static java.util.HashSet<String> daChupHop = new java.util.HashSet<String>();

    static int xuLyHop(String nhan, int toiDa) throws Exception {
        int n = 0;
        for (int lan = 0; lan < toiDa; lan++) {
            final String[] mt = {null};
            tren(new Runnable() {
                public void run() {
                    mt[0] = moTaHop();
                }
            });
            if (mt[0] == null) {
                ngu(700);
                tren(new Runnable() {
                    public void run() {
                        mt[0] = moTaHop();
                    }
                });
                if (mt[0] == null) break;
            }
            String khoa = mt[0].length() > 80 ? mt[0].substring(0, 80) : mt[0];
            System.out.println("HOP " + nhan + ": " + mt[0]);
            if (daChupHop.add(khoa)) chup(nhan + "_" + (mt[0].startsWith("noi:") ? "loi_npc" : mt[0].startsWith("menu:") ? "menu" : "hop"), false);
            n++;
            if (mt[0].startsWith("noi:")) {
                tren(new Runnable() {
                    public void run() {
                        game.pokemon.HopThoaiDuoi.hienHetChu();
                    }
                });
                chamThat(game.render.Canvas.hw, game.render.Canvas.h - 16, "loi-npc");
            } else if (mt[0].startsWith("menu:")) {
                String[] l = game.pokemon.MenuNguCanh.cacLenh();
                String chon = l.length > 0 ? l[0] : null;
                for (int u = UU_TIEN.length - 1; u >= 0; u--) {
                    for (String x : l) if (x != null && x.startsWith(UU_TIEN[u])) chon = x;
                }
                if (chon == null) break;
                chamTheoChu(chon);
            } else {
                chamChinh();
            }
            ngu(600);
        }
        return n;
    }

    static void dong() throws Exception {
        // đóng tấm / hộp đang mở như người chơi: nút X ("dong") nếu có, không thì phím mềm phải
        ChupTrongGame.ve(1920, 1080);
        java.lang.reflect.Field bd = game.render.screen.HudRong.class.getDeclaredField("banDoTo");
        bd.setAccessible(true);
        java.lang.reflect.Field cd = htbb.pc.LopPhu.class.getDeclaredField("caiDatMo");
        cd.setAccessible(true);
        if (cd.getBoolean(null)) {
            // bảng Cài đặt cửa sổ PC (lớp phủ AWT, nút X riêng)
            cd.setBoolean(null, false);
            chamDangDem++;
            chamDuong.append(" > [X cai dat PC]");
            ngu(300);
            return;
        }
        if (bd.getBoolean(null)) {
            // bản đồ to của HUD: nút X tự vẽ (không có vùng chạm tên) - chạm X = mã 41
            bd.setBoolean(null, false);
            chamDangDem++;
            chamDuong.append(" > [X ban do]");
            ngu(300);
            return;
        }
        for (String t : vungCo()) {
            if (t.equals("*dong")) {
                cham("dong");
                return;
            }
        }
        phim(-7, "phai");
    }

    static game.render.screen.GameScr gs() {
        return game.render.Canvas.gameScr;
    }

    /** Chờ nhân vật đứng yên (đi tới nơi) tối đa ms. */
    static void choDungYen(int ms) {
        long het = System.currentTimeMillis() + ms;
        int x0 = -1, y0 = -1, yen = 0;
        while (System.currentTimeMillis() < het) {
            ngu(250);
            game.model.Char c = gs().mainChar;
            if (c == null) continue;
            if (c.x == x0 && c.y == y0) {
                if (++yen >= 6) return;
            } else {
                yen = 0;
            }
            x0 = c.x;
            y0 = c.y;
        }
    }

    static game.model.NPC timNpc(String ten) {
        java.util.Vector v = gs().actors;
        for (int i = 0; v != null && i < v.size(); i++) {
            Object o = v.elementAt(i);
            if (o instanceof game.model.NPC) {
                game.model.NPC n = (game.model.NPC) o;
                String t = n.name != null ? n.name : (n.type >= 0 && n.type < game.model.NPC.NPCNAME.length ? game.model.NPC.NPCNAME[n.type] : "");
                if (t.indexOf(ten) >= 0) return n;
            }
        }
        return null;
    }

    /**
     * Tới NPC như người chơi: chạm đất cạnh NPC (đi bộ tìm đường - một chạm) rồi chạm NPC. false = không có NPC ở bản
     * đồ này.
     */
    static boolean toiNpc(final String ten) throws Exception {
        final game.model.NPC[] n = {null};
        tren(new Runnable() {
            public void run() {
                n[0] = timNpc(ten);
            }
        });
        if (n[0] == null) {
            System.out.println("KHONG THAY NPC " + ten);
            return false;
        }
        final int tx = n[0].x / 16, ty = n[0].y / 16 + 1;
        tren(new Runnable() {
            public void run() {
                game.render.screen.HudRong.diToi(gs(), tx, ty);
            }
        });
        chamDangDem++;
        chamDuong.append(chamDuong.length() == 0 ? "" : " > ").append("[cham dat canh " + ten + "]");
        choDungYen(25000);
        ngu(400);
        System.out.println("NPC " + ten + " @" + n[0].x + "," + n[0].y + " cam " + game.render.screen.GameScr.cmx + "," + game.render.screen.GameScr.cmy
                + " nv " + gs().mainChar.x + "," + gs().mainChar.y + " man " + game.render.Canvas.w + "x" + game.render.Canvas.h);
        chamThat(n[0].x - game.render.screen.GameScr.cmx, n[0].y - game.render.screen.GameScr.cmy - 12, "npc " + ten);
        System.out.println("  -> nham " + (gs().focusedActor == null ? "-" : gs().focusedActor.getClass().getSimpleName()) + " hop " + moTaHop());
        if (moTaHop() == null) {
            // chạm vào NPC bị thẻ thông báo / thẻ nhiệm vụ che, hoặc chỉ mới nhắm: nút hành động lớn (nói chuyện khi nhắm NPC)
            ngu(1500);
            if (moTaHop() == null) {
                tren(new Runnable() {
                    public void run() {
                        gs().focusedActor = n[0];
                    }
                });
                if (coVung("hud-danh")) cham("hud-danh");
                cho(new DieuKien() {
                    public boolean dung() {
                        return game.render.Canvas.currentDialog != null || game.pokemon.HopThoaiDuoi.dangBat();
                    }
                }, 4000);
                System.out.println("  -> nut hanh dong: hop " + moTaHop());
            }
        }
        cho(new DieuKien() {
            public boolean dung() {
                return game.render.Canvas.currentDialog != null || game.pokemon.HopThoaiDuoi.dangBat();
            }
        }, 4000);
        return true;
    }

    /** Chọn mục menu thẻ có chữ bắt đầu bằng một trong các tiền tố (thứ tự ưu tiên). */
    static boolean chonMenu(String... tienTo) throws Exception {
        if (!(game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh)) {
            System.out.println("KHONG CO MENU khi chon " + java.util.Arrays.toString(tienTo) + " | " + moTaHop());
            return false;
        }
        String[] l = game.pokemon.MenuNguCanh.cacLenh();
        for (String t : tienTo) {
            for (String x : l) {
                if (x != null && x.toLowerCase().indexOf(t.toLowerCase()) >= 0) {
                    // 31/10/2026: thẻ nhiều trang (8 lệnh / trang): lật "Thêm" như ngón tay thật tới trang có lệnh
                    for (int lat = 0; lat < 4 && !dangVeChu(x); lat++) {
                        chamTheoChu("Thêm");
                        ngu(300);
                    }
                    chamTheoChu(x);
                    ngu(500);
                    return true;
                }
            }
        }
        System.out.println("MENU KHONG CO " + java.util.Arrays.toString(tienTo) + " trong " + java.util.Arrays.toString(l));
        return false;
    }

    static void hanhTrinh2(String tkGm) throws Exception {
        game.pokemon.Bang.dungHieuUng = true;
        batDauViec("loi_mo_dau");
        xuLyHop("mo_dau", 20);
        xongViec();
        chup("lang_dau", true);
        inVung("lang_dau");
        // ---- hướng dẫn tân thủ: theo thẻ nhiệm vụ "Đi tới" như người chơi (tối đa 10 bước)
        for (int k = 0; k < 10; k++) {
            xuLyHop("hd" + k, 12);
            if (!coVung("nv-di-toi")) {
                System.out.println("HD " + k + ": khong co nut di toi; vung " + vungCo());
                break;
            }
            batDauViec("hd_buoc_" + k);
            chup("hd" + k + "_the_nv", true);
            cham("nv-di-toi");
            choDungYen(30000);
            xuLyHop("hd" + k + "_toi", 12);
            xongViec();
        }
        chup("sau_huong_dan", true);
        inVung("sau_huong_dan");
        // ---- nút HUD trên: từng tấm
        for (int i = 0; i < 4; i++) {
            if (!coVung("hud-tren-" + i)) continue;
            batDauViec("hud_tren_" + i);
            cham("hud-tren-" + i);
            ngu(800);
            xongViec();
            chup("hud_tren_" + i, false);
            inVung("hud_tren_" + i);
            dong();
            xuLyHop("sau_hud_" + i, 3);
        }
        String[][] hud = {{"hud-the", "the_nhan_vat"}, {"hud-ban-do-nho", "ban_do_vung"}, {"hud-cai-dat", "cai_dat"},
            {"hud-tu-dong-cai-dat", "tu_dong"}, {"hud-chat", "chat"}, {"hud-thoi-tiet", "thoi_tiet"}};
        for (String[] h : hud) {
            if (!coVung(h[0])) {
                System.out.println("KHONG CO " + h[0]);
                continue;
            }
            batDauViec("mo_" + h[1]);
            cham(h[0]);
            ngu(900);
            xongViec();
            chup(h[1], false);
            inVung(h[1]);
            dong();
            xuLyHop("sau_" + h[1], 3);
        }
        // ---- NPC dịch vụ trong làng
        String[][] npc = {{"Chansey", "thuốc", "mua", "cửa hàng"}, {"Kecleon", "mua", "cửa hàng"}, {"Machamp", "sửa", "nâng", "rèn"},
            {"Kangaskhan", "kho", "gửi"}, {"Xatu", "nhiệm vụ"}};
        for (String[] n : npc) {
            batDauViec("npc_" + n[0]);
            if (!toiNpc(n[0])) {
                xongViec();
                continue;
            }
            chup("npc_" + n[0], false);
            inVung("npc_" + n[0]);
            String[] chon = new String[n.length - 1];
            System.arraycopy(n, 1, chon, 0, chon.length);
            if (chonMenu(chon)) {
                ngu(1200);
                chup("npc_" + n[0] + "_mo", false);
                inVung("npc_" + n[0] + "_mo");
            }
            xongViec();
            for (int d = 0; d < 3 && dangMo(); d++) dong();
            xuLyHop("sau_npc_" + n[0], 4);
        }
        // ---- việc hay làm: mua thuốc ở Chansey (tới NPC -> menu -> cửa hàng -> chạm thuốc -> Mua)
        batDauViec("mua_thuoc");
        if (toiNpc("Chansey")) {
            if (chonMenu("thuốc", "mua", "cửa hàng")) {
                ngu(1200);
                if (coVung("hang-")) {
                    cham("hang-");
                    ngu(500);
                    chup("mua_thuoc_the", false);
                    inVung("mua_thuoc_the");
                    if (coVung("tvp-mua")) {
                        cham("tvp-mua");
                        ngu(800);
                        xuLyHop("mua_thuoc_xac_nhan", 3);
                    }
                }
            }
        }
        xongViec();
        for (int d = 0; d < 3 && dangMo(); d++) dong();
        // ---- Menu lưới: mở từng ô như người chơi (một chạm Menu + một chạm ô)
        java.util.List<String> o = new ArrayList<String>();
        cham("hud-tren-3");
        ngu(600);
        for (String v : vungCo()) {
            String t = v.replace("*", "");
            if (t.startsWith("menu-") && !o.contains(t)) o.add(t);
        }
        System.out.println("MENU O: " + o);
        dong();
        for (String t : o) {
            batDauViec("menu_" + t.substring(5));
            cham("hud-tren-3");
            ngu(500);
            if (!cham(t)) {
                xongViec();
                continue;
            }
            ngu(1200);
            xongViec();
            chup("menu_" + t.substring(5).replace(' ', '_').replace('/', '_'), false);
            for (int d = 0; d < 4 && dangMo(); d++) dong();
            xuLyHop("sau_menu", 3);
        }
        // ---- hồ sơ: các tab + hành trang + chạm đồ
        cham("hud-the");
        ngu(700);
        for (int t = 0; t < 5; t++) {
            if (!coVung("tab-" + t)) continue;
            cham("tab-" + t);
            ngu(600);
            chup("ho_so_tab_" + t, false);
        }
        dong();
        batDauViec("mo_hanh_trang_cham_do");
        cham("hud-tren-0");
        ngu(700);
        if (coVung("o-tui-") || coVung("tui-")) {
            cham(coVung("o-tui-") ? "o-tui-" : "tui-");
            ngu(600);
            chup("hanh_trang_the_do", false);
            inVung("hanh_trang_the_do");
        }
        xongViec();
        dong();
        dong();
        // ---- tự động đánh: nhấn giữ AUTO = cài đặt (người chơi thường chỉ thấy nút)
        if (coVung("hud-tu-dong")) {
            batDauViec("bat_tu_dong");
            cham("hud-tu-dong");
            ngu(800);
            xongViec();
            chup("tu_dong_bat", true);
            cham("hud-tu-dong");
        }
        // ---- cổng ra khỏi làng (Arcanine gác cổng): cuối cùng vì có thể đổi bản đồ
        batDauViec("npc_Arcanine");
        if (toiNpc("Arcanine")) {
            chup("npc_Arcanine", false);
            inVung("npc_Arcanine");
            chonMenu("đi", "qua", "cổng");
            ngu(1500);
            chup("npc_Arcanine_mo", false);
        }
        xongViec();
        xuLyHop("sau_Arcanine", 4);
    }

    /** Phần 2: tài khoản GM có sẵn (dịch chuyển nhanh tới các nơi người chơi tới sau nhiều giờ: Gym, Nhà trẻ, cổng...). */
    static void chat(String s) throws Exception {
        ChupThat.chat(s);
        ngu(2500);
    }

    static void hanhTrinhGm(final String tk) throws Exception {
        game.pokemon.Bang.dungHieuUng = true;
        tren(new Runnable() {
            public void run() {
                game.render.Canvas.loginScr.tfUser.setText(tk);
                game.render.Canvas.loginScr.tfPass.setText("123456");
            }
        });
        cham("dang-nhap-0");
        cho(new DieuKien() {
            public boolean dung() {
                Object m = game.render.Canvas.currentScreen;
                return m instanceof game.render.screen.CreateCharScr || m instanceof game.render.screen.CharSelectScr
                        || m == gs() && !game.pokemon.ManTai.dangVao;
            }
        }, 30000);
        if (game.render.Canvas.currentScreen instanceof game.render.screen.CreateCharScr) {
            cham("khoi-dau-0");
            tren(new Runnable() {
                public void run() {
                    game.render.screen.CreateCharScr.gI().tfChat.setText(tk.length() > 10 ? tk.substring(0, 10) : tk);
                }
            });
            cham("tao-1");
        } else if (game.render.Canvas.currentScreen instanceof game.render.screen.CharSelectScr) {
            cham("chon-1");
        }
        cho(new DieuKien() {
            public boolean dung() {
                return game.render.Canvas.currentScreen == gs() && !game.pokemon.ManTai.dangVao && gs().mainChar != null;
            }
        }, 60000);
        ngu(3000);
        xuLyHop("gm_vao", 20);
        String ten = gs().mainChar.name;
        chat("slv " + ten + " 40 0");
        chat("smn " + ten + " 3000000 200 500");
        xuLyHop("gm_len_cap", 6);
        // {lệnh mto, bước, NPC cần chạm (null = chỉ chụp)}
        String[][] noi = {{"mto 0 26 22", "gm_pewter", "Machamp"}, {"mto 0 26 22", "gm_pewter_kho", "Kangaskhan"},
            {"mto 1 30 30", "gm_pallet", null}, {"mto 11 30 30", "gm_rung_viridian", null}, {"mto 202 30 30", "gm_dau_truong", null},
            {"mto 201 30 30", "gm_cao_nguyen", null}};
        for (String[] n : noi) {
            chat(n[0]);
            cho(new DieuKien() {
                public boolean dung() {
                    return game.render.Canvas.currentScreen == gs() && !game.pokemon.ManTai.dangVao;
                }
            }, 20000);
            ngu(1500);
            xuLyHop(n[1] + "_vao", 4);
            chup(n[1], true);
            if (n[2] != null) {
                batDauViec("gm_npc_" + n[2]);
                if (toiNpc(n[2])) {
                    chup(n[1] + "_npc", false);
                    inVung(n[1] + "_npc");
                    String[] l = game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh ? game.pokemon.MenuNguCanh.cacLenh() : new String[0];
                    for (int i = 0; i < l.length && i < 6; i++) {
                        if (l[i] == null || l[i].startsWith("Nói")) continue;
                        if (!(game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh)) toiNpc(n[2]);
                        chonMenu(l[i]);
                        ngu(1500);
                        chup(n[1] + "_npc_" + i, false);
                        for (int d = 0; d < 4 && dangMo(); d++) dong();
                    }
                }
                xongViec();
            }
        }
        // Sổ tay huấn luyện (menu máy chủ -527): mọi mục cấp 1
        tren(new Runnable() {
            public void run() {
                game.render.screen.GameScr.moSoTay();
            }
        });
        ngu(1500);
        String[] l = game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh ? game.pokemon.MenuNguCanh.cacLenh() : new String[0];
        chup("gm_so_tay", false);
        System.out.println("SO TAY: " + java.util.Arrays.toString(l));
        for (int i = 0; i < l.length; i++) {
            if (l[i] == null) continue;
            if (!(game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh)) {
                tren(new Runnable() {
                    public void run() {
                        game.render.screen.GameScr.moSoTay();
                    }
                });
                ngu(1200);
            }
            if (!chonMenu(l[i])) continue;
            ngu(1500);
            chup("gm_so_tay_" + i, false);
            for (int d = 0; d < 4 && dangMo(); d++) dong();
            xuLyHop("gm_so_tay_sau", 2);
        }
    }

    /** Còn tấm / hộp đang mở (kể cả bảng phủ trên màn chơi như Tự động đánh: có nút X "dong" ở lớp trên). */
    static boolean dangMo() throws Exception {
        if (game.render.Canvas.currentDialog != null || game.render.Canvas.currentScreen != gs()) return true;
        java.lang.reflect.Field cd = htbb.pc.LopPhu.class.getDeclaredField("caiDatMo");
        cd.setAccessible(true);
        java.lang.reflect.Field bd = game.render.screen.HudRong.class.getDeclaredField("banDoTo");
        bd.setAccessible(true);
        if (cd.getBoolean(null) || bd.getBoolean(null)) return true;
        ChupTrongGame.ve(1920, 1080);
        return vungCo().contains("*dong");
    }

    static boolean coVung(String t) throws Exception {
        ChupTrongGame.ve(1920, 1080);
        for (String v : vungCo()) if (v.replace("*", "").startsWith(t)) return true;
        return false;
    }

    static String cauHoi() {
        try {
            java.lang.reflect.Field f = game.render.screen.DangKyScr.class.getDeclaredField("cauHoi");
            f.setAccessible(true);
            Object o = f.get(game.render.screen.DangKyScr.gI());
            if (o != null) return (String) o;
        } catch (Throwable e) {
            // bỏ
        }
        try {
            java.lang.reflect.Field f = game.render.Canvas.inputDlg.getClass().getDeclaredField("info");
            f.setAccessible(true);
            return String.valueOf(f.get(game.render.Canvas.inputDlg));
        } catch (Throwable e) {
            return "";
        }
    }

    static final String[] CHU_SO = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín", "mười"};

    /** "Bảy cộng 3 bằng mấy?" -> 10 */
    static int giai(String q) {
        String s = q.toLowerCase().replace("bằng mấy?", "").trim();
        boolean cong = s.contains(" cộng ");
        String[] p = s.split(cong ? " cộng " : " trừ ");
        int a = so(p[0].trim()), b = so(p[1].trim());
        return cong ? a + b : a - b;
    }

    static int so(String t) {
        for (int i = 0; i < CHU_SO.length; i++) if (CHU_SO[i].equals(t)) return i;
        return Integer.parseInt(t);
    }

    /** Nút chính của hộp thoại đang mở (OK / Đồng ý): chạm vùng chạm đầu tiên lớp trên có tên chứa "ok"/"dong-y"/"1". */
    static void chamChinh() throws Exception {
        ChupTrongGame.ve(1920, 1080);
        List<String> v = vungCo();
        String chon = null;
        for (String t : v) {
            if (!t.startsWith("*")) continue;
            String n = t.substring(1);
            if (n.equals("null") || n.startsWith("nen")) continue;
            chon = n;   // cuối cùng = nút phải (chính) thường gặp
        }
        System.out.println("CHAM CHINH " + chon + " trong " + v);
        if (chon != null) cham(chon);
    }

    static void ket() throws Exception {
        PrintWriter p = new PrintWriter(new FileWriter(new File(RA, "so_cham.tsv")));
        p.println("viec\tso_cham\tduong");
        for (Map.Entry<String, Integer> e : SO_CHAM.entrySet()) {
            p.println(e.getKey() + "\t" + e.getValue() + "\t" + CHAM_CHI_TIET.get(e.getKey()));
        }
        p.close();
        PrintWriter b = new PrintWriter(new FileWriter(new File(RA, "buoc.tsv")));
        for (String s : BUOC) b.println(s);
        b.close();
        BAO.close();
        System.out.println("KET QUA: " + soBuoc + " buoc, " + soHong + " HONG, " + soNhac + " NHAC -> " + RA);
        System.exit(soHong == 0 ? 0 : 1);
    }
}
