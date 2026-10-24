import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * Chụp màn TRONG GAME không cần cửa sổ / máy chủ (headless, đường vẽ bản PC) + KIỂM CỠ CHẠM giao diện mới:
 * nhân vật giả trên bản đồ offline, mở lần lượt màn chơi (HUD), Menu lưới (ManMenu), màn Nhân vật (5 tab), ô trang
 * bị -> hành trang lọc (có đồ / trống), thẻ chi tiết so sánh. Mỗi màn ở cỡ điện thoại 16:9, 19.5:9, 20:9, 21:9, 16:10, 4:3
 * (giả lập cỡ chạm Android 44 dp) và cỡ PC (16:9, 21:9, 16:10, 4:3; cỡ chạm 32 px). Với MỌI vùng chạm đăng ký (VungCham) ở lớp
 * trên cùng: cỡ >= tối thiểu, hai vùng không chồng + cách nhau >= 8 dp (PC 8 px), nằm trong màn, bấm giả tâm trúng đúng.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java \
 *       server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java \
 *     && java -Djava.awt.headless=true [-Dhtbb.mayChu=server] -cp client_pc/out/build/cls:client_pc/out/build/res:$O ChupTrongGame <thư mục ảnh>
 * (0.9.5: máy chủ ảnh giả - icon vật phẩm nạp từ server/map như lệnh -51 thật; -Dchup.co / -Dchup.man chạy một phần)
 * Mã thoát 1 = có lỗi.
 */
public class ChupTrongGame {
    static int dat, hong;
    /** Tên mẫu trang bị có hậu tố " · Cấp N" (đặt khi dựng mẫu đồ): không màn nào được cắt chúng. */
    static String[] TEN_DO_CAP = new String[0];
    /** Mọi tên mẫu đồ (+ bản bỏ " · Cấp N"): ở màn danh sách / thẻ đồ (MAN_TEN_DU) không tên nào được cắt. */
    static String[] TEN_DO = new String[0];
    static final String[] MAN_TEN_DU = {"nv_hanh_trang", "nv_chi_tiet", "nv_trang_bi", "kho_do", "cua_hang_do", "cua_hang_ban",
        "xem_trang_bi", "xem_do_thu", "hop_vat_pham", "hop_cho", "hop_mac_do"};

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    /** {rộng, cao, dpi giả lập (0 = PC)} */
    static final int[][] CO = {{1920, 1080, 420}, {2340, 1080, 420}, {2400, 1080, 420}, {2520, 1080, 420},
        {2560, 1600, 320}, {2048, 1536, 320}, {1280, 720, 0}, {1920, 1080, 0}, {2560, 1080, 0}, {1920, 1200, 0}, {1024, 768, 0}};
    static final String[] TEN_CO = {"dt16x9", "dt19.5x9", "dt20x9", "dt21x9", "tablet16x10", "tablet4x3", "pc720", "pc1080", "pc21x9", "pc16x10", "pc4x3"};

    /** htbb.pc.ChuHD.thayKyHieu (gói riêng): ký hiệu phông PC không có -> chữ thay, đúng như lúc vẽ. */
    static String thayKyHieuHD(String s) {
        try {
            java.lang.reflect.Method m = htbb.pc.ChuHD.class.getDeclaredMethod("thayKyHieu", String.class);
            m.setAccessible(true);
            return (String) m.invoke(null, s);
        } catch (Throwable e) {
            return s;
        }
    }

    static void chay(final Runnable r) throws Exception {
        final Object k = new Object();
        final boolean[] xong = {false};
        NenPc.luong().them(new Runnable() {
            public void run() {
                try {
                    r.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    synchronized (k) {
                        xong[0] = true;
                        k.notifyAll();
                    }
                }
            }
        });
        synchronized (k) {
            long het = System.currentTimeMillis() + 5000;
            while (!xong[0] && System.currentTimeMillis() < het) k.wait(100);
        }
    }

    static BufferedImage ve(int W, int H) throws Exception {
        final BufferedImage dem = new BufferedImage(NenPc.thatW, NenPc.thatH, BufferedImage.TYPE_INT_RGB);
        chay(new Runnable() {
            public void run() {
                Graphics2D g2 = dem.createGraphics();
                g2.setColor(java.awt.Color.MAGENTA);
                g2.fillRect(0, 0, dem.getWidth(), dem.getHeight());
                Graphics2D gg = (Graphics2D) g2.create();
                int k = NenPc.heSo;
                gg.scale(k, k);
                gg.clipRect(0, 0, NenPc.rong, NenPc.cao);
                Graphics g = new Graphics(gg, NenPc.rong, NenPc.cao, k);
                try {
                    java.lang.reflect.Method m = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("paint", Graphics.class);
                    m.setAccessible(true);
                    m.invoke(game.render.Canvas.instance, g);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                gg.dispose();
                try {
                    htbb.pc.LopPhu.ve(g2, dem.getWidth(), dem.getHeight());
                } catch (Throwable e) {
                    // bỏ
                }
                g2.dispose();
            }
        });
        BufferedImage ra = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = ra.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(dem, NenPc.hienX, NenPc.hienY, NenPc.hienW, NenPc.hienH, null);
        g.dispose();
        return ra;
    }

    private static BufferedImage demDo;

    /** Vẽ một khung vào bộ đệm dùng lại (gọi TRÊN luồng sự kiện NenPc) - DoKhung đo thời gian / cấp phát. */
    static void veThang() {
        if (demDo == null || demDo.getWidth() != NenPc.thatW || demDo.getHeight() != NenPc.thatH) {
            demDo = new BufferedImage(NenPc.thatW, NenPc.thatH, BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g2 = demDo.createGraphics();
        Graphics2D gg = (Graphics2D) g2.create();
        int k = NenPc.heSo;
        gg.scale(k, k);
        gg.clipRect(0, 0, NenPc.rong, NenPc.cao);
        Graphics g = new Graphics(gg, NenPc.rong, NenPc.cao, k);
        try {
            if (veM == null) {
                veM = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("paint", Graphics.class);
                veM.setAccessible(true);
            }
            veM.invoke(game.render.Canvas.instance, g);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        gg.dispose();
        try {
            htbb.pc.LopPhu.ve(g2, demDo.getWidth(), demDo.getHeight());
        } catch (Throwable e) {
            // bỏ
        }
        g2.dispose();
    }

    private static java.lang.reflect.Method veM;

    /** Kiểm mọi vùng chạm lớp trên cùng: cỡ, khoảng cách, trong màn, bấm tâm trúng. */
    static void kiemCham(String nhan, int min, int cach) {
        int n = game.pokemon.VungCham.dem();
        int top = game.pokemon.VungCham.lopCaoNhat();
        int so = 0;
        for (int i = 0; i < n; i++) {
            if (game.pokemon.VungCham.lopVung(i) != top) continue;
            String ten = game.pokemon.VungCham.ten(i);
            if (ten != null && ten.startsWith("nen")) continue;   // vùng nền (chạm ra ngoài = đóng)
            so++;
            int[] v = game.pokemon.VungCham.vungVe(i);
            String t = nhan + " [" + ten + "]";
            kiem(v[2] >= min && v[3] >= min, t + " co " + v[2] + "x" + v[3] + " >= " + min + " @" + v[0] + "," + v[1] + " (MIN luc ve " + game.pokemon.VungCham.MIN + ", logic " + NenPc.rong + "x" + NenPc.cao + ")");
            kiem(v[0] >= 0 && v[1] >= 0 && v[0] + v[2] <= NenPc.rong + 1 && v[1] + v[3] <= NenPc.cao + 1, t + " trong man");
            int cx = v[0] + v[2] / 2, cy = v[1] + v[3] / 2;
            int j = game.pokemon.VungCham.tim(cx, cy);
            kiem(j == i, t + " bam tam trung (" + (j < 0 ? "-" : game.pokemon.VungCham.ten(j)) + ")");
            for (int q = i + 1; q < n; q++) {
                if (game.pokemon.VungCham.lopVung(q) != top) continue;
                String tq = game.pokemon.VungCham.ten(q);
                if (tq != null && tq.startsWith("nen")) continue;
                int[] w = game.pokemon.VungCham.vungVe(q);
                // vùng lồng nhau (nút trong hàng) bỏ qua; còn lại: khoảng hở >= cach
                boolean trong = w[0] >= v[0] && w[1] >= v[1] && w[0] + w[2] <= v[0] + v[2] && w[1] + w[3] <= v[1] + v[3]
                        || v[0] >= w[0] && v[1] >= w[1] && v[0] + v[2] <= w[0] + w[2] && v[1] + v[3] <= w[1] + w[3];
                if (trong) continue;
                int hx = Math.max(w[0] - (v[0] + v[2]), v[0] - (w[0] + w[2]));
                int hy = Math.max(w[1] - (v[1] + v[3]), v[1] - (w[1] + w[3]));
                int ho = Math.max(hx, hy);
                if (tron(ten) && tron(tq)) {
                    // hai nút tròn (HUD): khoảng hở thật giữa hai đường tròn nội tiếp
                    double dx = (v[0] + v[2] / 2.0) - (w[0] + w[2] / 2.0), dy = (v[1] + v[3] / 2.0) - (w[1] + w[3] / 2.0);
                    ho = (int) Math.floor(Math.sqrt(dx * dx + dy * dy) - v[2] / 2.0 - w[2] / 2.0);
                }
                kiem(ho >= cach, t + " x [" + tq + "] cach " + ho + " >= " + cach);
            }
        }
        kiem(so > 0, nhan + " co vung cham");
    }

    static boolean tron(String t) {
        return t != null && t.startsWith("hud-") && !t.equals("hud-the") && !t.equals("hud-tu-dong");
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        // hiệu ứng mở tấm ở trạng thái cuối: hai khung chụp cách nhau > 300 ms (giả lập máy chủ, ghi ảnh) mở lại hiệu ứng
        game.pokemon.Bang.dungHieuUng = true;
        File ra = new File(a.length > 0 ? a[0] : "anh_trong_game");
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
        chay(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        chay(new Runnable() {
            public void run() {
                giaLap();
            }
        });
        Thread.sleep(800);
        htbb.pc.ChuHD.ghi = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
        if (System.getProperty("chup.j2me") != null) {
            // 0.9.6: vẽ như máy J2ME (font ảnh, khối phẳng - không chữ thật / khung HD), cùng bảng màu ChuDe
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            game.pokemon.ChuUI.bo = null;
        }
        String[] man = {"the_gioi", "menu", "nv_trang_bi", "nv_o_co_do", "nv_o_trong", "nv_chi_tiet", "nv_hanh_trang", "nv_chi_so",
            "nv_ky_nang", "nv_thu", "nv_ngoai_hinh", "nv_cuoi_trang_bi", "nv_cuoi_ds_thu", "nv_cuoi_thu",
            // quản lý thú: ô thú, chọn thú (thanh Cưỡi / Khoá / Thả), sân khấu xem thú ở Trang bị › Thú cưỡi
            "nv_ds_thu", "nv_ds_thu_quan_ly", "nv_thu_quan_ly", "nv_cuoi_ds_thu_quan_ly", "the_gioi_cuoi", "so_luong",
            // 0.9.13: khối đang dùng (máy chủ >= 913) - ĐANG CƯỠI + pet ĐANG ĐI THEO dù gói 15 / -93 chưa tới (lỗi chủ dự án)
            "nv_thu_dang_dung", "nv_thu_dang_dung_chon", "nv_thu_di_theo_dang_dung", "nv_thu_dang_dung_tab",
            // 0.9.16: tách tab con Thú cưỡi / Đi theo - hai lối vào (ô Trang bị › Thú cưỡi / ô Đi theo, tab Thú), Thả trùng
            // theo tab (hộp hỏi nói loại), tab trống
            "nv_thu_dang_dung_tb_cuoi", "nv_thu_dang_dung_tb_di_theo", "nv_thu_dang_dung_tab_di_theo", "nv_thu_dang_dung_tab_nho",
            "nv_thu_dang_dung_tha_trung_di_theo", "nv_thu_dang_dung_tha_trung_cuoi", "nv_thu_trong_tab0", "nv_thu_trong_tab1",
            "menu_ngu_canh", "tui_loc_thuoc", "tui_loc_nguyen_lieu", "xac_nhan", "cua_hang_thuoc", "cua_hang_thuoc_chi_tiet",
            "cua_hang_do", "cua_hang_do_chi_tiet", "cua_hang_ban", "nhiem_vu", "nhiem_vu_chi_tiet", "giao_dich", "pokedex",
            "pokedex_chi_tiet", "xep_hang", "doi", "ky_nang_hoc", "ky_nang_chi_tiet", "thong_bao_kich_hoat", "hop_thong_bao",
            "hop_thong_bao_dai", "suc_khoe", "chon_chieu", "goi_y_chieu", "tu_dong", "hop_ban", "hop_mac_do", "hop_len_cap", "hop_tien_hoa",
            "hop_phan_thuong", "hop_nhan_nhiem_vu", "hop_bao_tri", "hop_loi", "hop_mat_ket_noi", "hop_hoi_sinh", "hop_giftcode",
            "hop_giftcode_ket_qua", "hop_moi_giao_dich", "hop_moi_doi", "hop_dang_xuat", "hop_cho", "hop_nhap_chu", "hop_vat_pham",
            // 0.9.6
            "tu_dong_dang", "thu_qua", "cai_dat_tai_khoan", "npc_chansey", "npc_kecleon", "npc_kangaskhan",
            // 0.9.6.1: hệ sinh thái / câu cá (menu máy chủ NPC ảo -528 + thông báo 37 qua MessageHandler thật)
            "hst_menu", "hst_menu_trang2", "hst_cong_thuc", "hst_suu_tam", "hst_vung", "hst_cho", "hst_cho_loai", "hst_tui", "hst_tui_day",
            "hst_tui_hanh_dong", "hst_ky_nang", "hst_tu_man", "hop_hst_ky_nang", "hop_hst_nghien_cuu", "hop_hst_the_gioi",
            "hop_cau_ket_qua", "cau_can",
            // 0.9.7
            "danh_hieu", "danh_hieu_chua_co", "danh_hieu_an", "danh_hieu_chi_tiet", "hop_phan_thuong_xem", "the_phan_thuong",
            // 0.9.7: tin nhiệm vụ đội (trước: cửa sổ chữ ViewInfoScr)
            "hop_tin_bang",
            // 0.9.8 tủ đồ trang trí (tab Ngoại hình)
            "nv_tu_do", "nv_tu_do_thu", "nv_tu_do_hao_quang", "nv_tu_do_an",

            // 0.9.7: cửa sổ cũ (WindowInfoScr / ShopNew) -> màn NRO ở máy cảm ứng
            "xem_trang_bi", "xem_trang_bi_the", "xem_do_thu", "shop_su_kien", "shop_su_kien_the", "tui_do_lay_ra_the",
            "cua_hang_nguyen_lieu", "cua_hang_nguyen_lieu_the", "kho_do", "kho_do_the", "luyen_do", "luyen_do_dat", "kham",
            "hop_thanh",
            // 0.9.14: thẻ vật phẩm dạng thuốc theo bảng máy chủ (-104 kiểu 9) - Sáo gọi Tauros / nguyên liệu sự kiện
            "tui_sao_goi", "tui_nguyen_lieu_su_kien"};
        // chạy nhanh một phần: -Dchup.co=dt20x9,pc1080 -Dchup.man=menu,the_gioi
        String locCo = System.getProperty("chup.co"), locMan = System.getProperty("chup.man");
        for (int c = 0; c < CO.length; c++) {
            if (locCo != null && !("," + locCo + ",").contains("," + TEN_CO[c] + ",")) continue;
            final int W = CO[c][0], H = CO[c][1], dpi = CO[c][2];
            NenPc.tinhKichThuoc(W, H);
            chay(new Runnable() {
                public void run() {
                    game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                }
            });
            float s = NenPc.hienW / (float) NenPc.rong;
            int min, cach;
            if (dpi > 0) {
                // giả lập Android: 44 dp / 8 dp theo px logic của khung phủ kín
                float dpL = (dpi / 160f) / s;
                min = (int) Math.ceil(44 * dpL);
                cach = (int) Math.floor(8 * dpL);
                game.pokemon.Bang.anTrai = (int) Math.ceil(100 / s);   // tai thỏ 100 px
            } else {
                min = (int) Math.ceil(32 / s);
                cach = (int) Math.ceil(8 / s);
                game.pokemon.Bang.anTrai = 0;
            }
            game.pokemon.VungCham.MIN = min;
            game.pokemon.VungCham.CACH = cach;
            for (int m = 0; m < man.length; m++) {
                final String ten = man[m];
                if (locMan != null && !("," + locMan + ",").contains("," + ten + ",")) continue;
                chay(new Runnable() {
                    public void run() {
                        moMan(ten);
                    }
                });
                Thread.sleep(250);
                chay(new Runnable() {
                    public void run() {
                        // hộp thoại mạng (không có máy chủ khi kiểm) đè lên màn đang kiểm: bỏ
                        Object d = game.render.Canvas.currentDialog;
                        if (!ten.startsWith("hop_") && !ten.equals("the_phan_thuong") && !ten.equals("xac_nhan") && !ten.equals("so_luong")
                                && (d instanceof game.render.screen.MsgDlg || d instanceof game.pokemon.HopThongBao)) {
                            game.render.Canvas.endDlg();
                        }
                    }
                });
                ve(W, H);
                // chuỗi chạm thật sau khi mở (thẻ vật phẩm, đặt vào bàn luyện...): vẽ lại sau mỗi chạm
                String[] cham = chamSau(ten);
                for (int ci = 0; cham != null && ci < cham.length; ci++) {
                    final String vc = cham[ci];
                    chay(new Runnable() {
                        public void run() {
                            chamVung(vc);
                        }
                    });
                    ve(W, H);
                }
                final int demTam0 = game.pokemon.Bang.demTam;
                chay(new Runnable() {
                    public void run() {
                        giaMayChuAnh();
                    }
                });
                Thread.sleep(150);
                game.pokemon.VeTay.xoa();
                game.pokemon.GhiChu.xoa();
                game.pokemon.GhiChu.bat = true;
                game.render.screen.Font.thieuNet = 0;
                BufferedImage anh = ve(W, H);
                game.pokemon.GhiChu.bat = false;
                kiemChu(ten + " " + TEN_CO[c]);
                kiemTrongMan(ten + " " + TEN_CO[c]);
                if ("nhiem_vu".equals(ten) || "nhiem_vu_chi_tiet".equals(ten)) {
                    kiemMucNhiemVu(ten + " " + TEN_CO[c]);
                }
                kiemXemThu(ten, ten + " " + TEN_CO[c]);
                if (ten.startsWith("the_gioi") || "suc_khoe".equals(ten) || "goi_y_chieu".equals(ten)) {
                    kiemPhuKin(anh, ten + " " + TEN_CO[c]);
                    kiemNhanTheGioi(ten + " " + TEN_CO[c]);
                    if (game.render.screen.HudRong.bat()) kiemKheChieu(ten + " " + TEN_CO[c]);
                }
                ImageIO.write(anh, "png", new File(ra, ten + "_" + TEN_CO[c] + ".png"));
                // 0.9.7 quy tắc chủ dự án: mọi icon là ẢNH PNG - không icon nào vẽ bằng hình (vector / cung / nét)
                kiem(game.pokemon.VeTay.dem == 0, ten + " " + TEN_CO[c] + " icon ve bang code (" + game.pokemon.VeTay.dem + " lan, "
                        + game.pokemon.VeTay.cuoi + ")");
                if (ten.startsWith("hop_") || ten.equals("xac_nhan") || ten.equals("so_luong")) {
                    kiemHop(ten + " " + TEN_CO[c]);
                }
                // 0.9.6: tấm (sheet) canh giữa vùng an toàn, trong giới hạn cỡ theo tỉ lệ màn; menu ngữ cảnh trong vùng
                // an toàn và không chồng phần HUD
                kiemTam(ten + " " + TEN_CO[c], demTam0);
                if ("menu_ngu_canh".equals(ten)) {
                    kiemMenuNguCanh(ten + " " + TEN_CO[c]);
                }
                kiemTuiNro(ten, ten + " " + TEN_CO[c]);
                if (ten.startsWith("hst_") && !"hst_ky_nang".equals(ten)) {
                    kiemMenuMayChu(ten + " " + TEN_CO[c]);
                }
                // màn chơi: HUD PC (HudRong) chỉ ở cỡ PC; HUD APK (Hud.java) kiểm riêng bằng client_jar/tools/KiemCoCham
                boolean theGioi = ten.startsWith("the_gioi") || "thong_bao_kich_hoat".equals(ten) || "suc_khoe".equals(ten)
                        || "goi_y_chieu".equals(ten) || ten.startsWith("tu_dong") || "hop_vat_pham".equals(ten) || ten.startsWith("npc_")
                        || "cai_dat_tai_khoan".equals(ten) || "hst_ky_nang".equals(ten) || "cau_can".equals(ten)
                        // 0.9.6.1: hộp "OK" báo tin trong game (lên cấp, phần thưởng...) nay là thẻ nhanh trên màn chơi
                        || ten.startsWith("hop_") && game.render.Canvas.currentDialog == null
                        && game.render.Canvas.currentScreen == game.render.Canvas.gameScr;
                if (!theGioi || dpi == 0) {
                    kiemCham(ten + " " + TEN_CO[c], min, cach);
                }
            }
        }
        // soát chữ đã vẽ: mọi chuỗi ra màn (ChuHD / ChuGiaoDien) -> chuoi_da_ve.txt (tools/pokemon/kiem_chuoi_ui.py đọc)
        java.util.TreeSet<String> ds = new java.util.TreeSet<String>(htbb.pc.ChuHD.ghi);
        java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(new File(ra, "chuoi_da_ve.txt")), "UTF-8"));
        for (String x : ds) pw.println(x.replace('\n', ' '));
        pw.close();
        System.out.println("ChupTrongGame: " + ds.size() + " chuoi da ve -> chuoi_da_ve.txt");
        System.out.println("ChupTrongGame: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }

    /** Nhân vật + hành trang + đồ đang mặc giả (không máy chủ). */
    static void giaLap() {
        // 0.9.8: ảnh / kiểm KHÔNG phụ thuộc giờ máy (lớp phủ đêm 19h-6h đổi màu thế giới - KiemNgamTay đếm màu vạch ngắm
        // trượt sau 19h): giữa trưa cố định
        game.pokemon.SinhThai.gioThu = 12;
        // 0.9.7: trạng thái hành trang như gói -105 của máy chủ mới (sức chứa 21 ô -> ô khoá hiện ngay trong lưới, trần 126)
        game.pokemon.TrangThaiNguoiChoi.oDaMo = 21;
        game.pokemon.TrangThaiNguoiChoi.oToiDaVe = 126;
        game.pokemon.TrangThaiNguoiChoi.giaXuMoRong = 30000;
        game.pokemon.TrangThaiNguoiChoi.soVe = 0;
        game.render.Canvas.isLoad = false;
        game.render.Canvas.endDlg();
        try {
            game.model.Tilemap.loadMap(0, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        game.model.MainChar mc = gs.mainChar;
        mc.name = "Charmander99";
        mc.level = 24;
        mc.clazz = 0;
        mc.pokeDex = 5;
        mc.hp = 820;
        mc.maxhp = 1200;
        mc.mp = 240;
        mc.maxmp = 400;
        mc.lvpercent = 455;
        mc.defend = 310;
        mc.defend_magic = 180;
        mc.accurate = 92;
        mc.dodge = 41;
        mc.critical = 15;
        mc.baokich = 75;
        mc.attack = 186;   // 0.9.5: trước đây không đặt -> "Tấn công 0"
        mc.speed = 5;
        mc.potions[1] = 12;
        mc.potions[4] = 5;
        mc.x = 200;
        mc.y = 200;
        // như máy chủ thật gửi (Char.getInfoServer: dòng chiến trường của game gốc) + chỉ số có nghĩa -> màn lọc dòng rác
        game.render.screen.WindowInfoScr.infoMainCharServer = game.render.Util.split(
                "Thông tin chiến trường:@Sát thương trụ: Lớp giáp: 20@Giáp quái: 10@Kháng Lửa: 12%@Hút máu: 3%@", "@");
        // mẫu đồ
        // 23/10/2026: tên trang bị trùng nhau mang hậu tố cấp (doi_ten_trang_bi_cap.sql) - 3 mẫu dài nhất thật
        // ("Lông vũ tốc độ huyền thoại · Cấp 80" = 35 ký tự): kiemChu báo hỏng nếu tên bị cắt ("…") ở bất kỳ màn nào.
        String[] tenDo = {"Than lửa hồng", "Khăn quàng đỏ", "Băng đô gió", "Đai sức mạnh", "Vòng cổ ngọc", "Huy hiệu Đá",
            "Găng đấm thép", "Lông vũ tốc độ huyền thoại · Cấp 12", "Phiến đá Lửa", "Ruy băng vàng", "Khăn quàng xanh",
            "Đai bạc · Cấp 40", "Hạt giống thần chuyên gia · Cấp 17"};
        java.util.Vector capDo = new java.util.Vector();
        for (int i = 0; i < tenDo.length; i++) {
            if (tenDo[i].indexOf(" · Cấp ") > 0) capDo.addElement(tenDo[i]);
        }
        TEN_DO_CAP = new String[capDo.size()];
        capDo.copyInto(TEN_DO_CAP);
        java.util.Vector tatCa = new java.util.Vector();
        for (int i = 0; i < tenDo.length; i++) {
            tatCa.addElement(tenDo[i]);
            String gon = game.pokemon.ChuDe.boCap(tenDo[i]);
            if (!gon.equals(tenDo[i])) tatCa.addElement(gon);
        }
        TEN_DO = new String[tatCa.size()];
        tatCa.copyInto(TEN_DO);
        int[] loai = {3, 0, 2, 1, 9, 8, 11, 10, 12, 19, 0, 1, 4};
        game.model.ItemTemplate[] mau = new game.model.ItemTemplate[tenDo.length];
        for (int i = 0; i < mau.length; i++) {
            game.model.ItemTemplate t = new game.model.ItemTemplate();
            t.id = (short) i;
            t.name = tenDo[i];
            t.type = (short) loai[i];
            t.level = (short) (i == 11 ? 40 : 5 + i);
            t.idIcon = (short) com.htbb.pokemon.IconTrangBi.goc(loai[i]);   // icon thật của ô (như data_item.xstart sau doi_icon_trang_bi.sql)
            mau[i] = t;
        }
        game.render.Res.itemTemplates.removeAllElements();
        game.render.Res.itemTemplates.addElement(mau);
        String[] tenCs = {"Tấn công", "Phòng thủ", "Né tránh", "Chính xác", "Chí mạng", "HP"};
        for (int i = 0; i < tenCs.length; i++) {
            game.model.ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.put(String.valueOf(i), new game.model.NameAttributeItem((short) i, tenCs[i], (byte) 0, (byte) 0));
        }
        mc.wearingItems = new java.util.Vector();
        game.model.Char.inventory.removeAllElements();
        int[] mac = {0, 1, 4, 5};
        for (int i = 0; i < mau.length; i++) {
            game.model.ItemInInventory it = new game.model.ItemInInventory();
            it.itemID = (short) (500 + i);
            it.idTemplate = (short) i;
            it.charClazz = 0;
            it.level = mau[i].level;
            it.colorName = (byte) (i % 4);
            it.isEnoughData = true;
            it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 0, (short) (20 + i * 3)));
            it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 1, (short) (12 + (i % 5) * 4)));
            if (i % 2 == 0) it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 5, (short) (40 + i)));
            boolean dangMac = false;
            for (int k = 0; k < mac.length; k++) if (mac[k] == i) dangMac = true;
            if (dangMac) {
                it.viTriVe = 1;
                mc.wearingItems.addElement(it);
            } else {
                game.model.Char.inventory.addElement(it);
            }
        }
        // nguyên liệu, vật phẩm nhiệm vụ
        game.model.GemTemp gt = new game.model.GemTemp();
        gt.id = 3;
        gt.name = "Đá tiến hoá Lửa";
        gt.decript = "Nguyên liệu tiến hoá dòng Lửa.";
        gt.type = 1;
        gt.price = 1200;
        gt.isSell = true;
        gt.typeMoney = 0;
        game.render.Res.gemTemplate.removeAllElements();
        game.render.Res.gemTemplate.addElement(gt);
        game.render.Res.shopTemplate.removeAllElements();
        for (int i = 0; i < 6; i++) {
            game.model.GemTemp sp = new game.model.GemTemp();
            sp.id = (short) (10 + i);
            sp.name = "Rương quà " + (i + 1);
            sp.decript = "Mở ra nhận vật phẩm ngẫu nhiên.";
            sp.price = 50 * (i + 1);
            sp.typeMoney = (byte) (i % 2);
            sp.isSell = true;
            game.render.Res.shopTemplate.addElement(sp);
        }
        game.model.GemTemplate gm = new game.model.GemTemplate(3);
        gm.number = 7;
        game.model.MainChar.gemItem.removeAllElements();
        game.model.MainChar.gemItem.addElement(gm);
        game.model.ItemQuest.NAME_ITEM = new String[]{"Lông Pidgey", "Vỏ Caterpie"};
        game.model.ItemQuest.ICON_IMG = new byte[]{1, 2};
        game.model.MainChar.itemQuest = new short[]{4, 9};
        mc.charXu = 1250000;
        mc.luong = 320;
        mc.skillPointLeft = 2;
        // 0.9.5: nhân vật đã học 3 chiêu (thanh chiêu tự điền: đánh = chiêu 0, ô 1-3 = chiêu đã học)
        // -1 = phải học ở NPC (chiêu 6-8), 0 = cộng 1 điểm khi đủ cấp
        game.model.Char.skillLevelLearnt = new byte[]{1, 2, 1, 1, 0, 0, -1, -1, -1};
        game.model.MainChar.quickSlot = null;
        // bảng cấp cộng điểm chiêu (gói vào game) - dòng Charmander của máy chủ CharManager.LEVEL_ADD_SKILL[0]
        short[][] capCong = {{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10}, {6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 15}, {11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 20},
            {17, 20, 22, 24, 26, 30, 33, 36, 39, 42, 42}, {3, 6, 9, 12, 18, 24, 26, 28, 30, 32, 32}, {26, 29, 32, 34, 36, 38, 40, 42, 44, 46, 46},
            {25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 43}, {30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 48}, {45, 45, 47, 49, 51, 53, 55, 57, 59, 61, 61}};
        game.model.skill.SkillManager.LEVEL_ADD_SKILL = new byte[15][11];
        for (int i = 0; i < capCong.length; i++) for (int k = 0; k < 11; k++) game.model.skill.SkillManager.LEVEL_ADD_SKILL[i][k] = (byte) capCong[i][k];
        // thú cưỡi + pet trong túi thú (gói 16 loại 2, tên đúng như máy chủ gửi)
        game.model.Char.animal.removeAllElements();
        String[][] thu = {{"0", "2", "3", "Tauros lv 3", "0"}, {"1", "13", "1", "Dragonite", "0"}, {"2", "4", "0", "Hoothoot cấp 1\nID: 2", "1"},
            {"3", "6", "0", "Trứng Dratini\nID: 7\nhh: 21/10/2026\n-18:00", "1"}};
        for (int i = 0; i < thu.length; i++) {
            game.model.AnimalInfo an = new game.model.AnimalInfo();
            an.id = (short) Integer.parseInt(thu[i][0]);
            an.idImg = (byte) Integer.parseInt(thu[i][1]);
            an.idPaint = an.idImg;
            an.level = (byte) Integer.parseInt(thu[i][2]);
            an.name = thu[i][3];
            an.typeAnimal = (byte) Integer.parseInt(thu[i][4]);
            game.model.Char.animal.addElement(an);
        }
        // bảng thuốc (tên + chỉ số ảnh 5500+K như máy chủ gửi)
        try {
            String[] tenThuoc = {"", "Thuốc hồi HP", "Siêu thuốc HP", "Thuốc hồi máu tối đa", "Ether", "Ether lớn", "Elixir tối đa"};
            int[] anh = {0, 0, 1, 2, 3, 4, 5};
            game.model.Potion[] lp = new game.model.Potion[120];
            for (int i = 0; i < lp.length; i++) {
                lp[i] = new game.model.Potion();
                lp[i].index = (short) (i < anh.length ? anh[i] : 0);
                lp[i].name = i < tenThuoc.length ? tenThuoc[i] : "Thuốc " + i;
                lp[i].delay = 3000;
            }
            game.model.MainChar.listPotion = lp;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // giá thuốc
        try {
            for (int i = 0; i < game.render.Res.potionTemplates.length; i++) {
                if (game.render.Res.potionTemplates[i] == null) {
                    game.render.Res.potionTemplates[i] = new game.model.PotionTemplate((short) (20 + i * 15), (short) 100);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // nhiệm vụ
        game.model.Quest q = new game.model.Quest(1);
        q.name = "Dẹp loạn Rattata";
        q.decript = "Rattata phá vườn của bà Hoa. Hạ 10 con rồi về báo lại.";
        q.infoGift = "500 xu, 120 điểm kinh nghiệm, 2 Thuốc hồi HP";
        q.stateQuest = 2;
        q.type = 0;
        q.idMonsKill = new short[]{0};
        q.allMonsKilled = new short[]{6};
        q.totalMonskill = new short[]{10};
        q.idNpcReceive = 5;     // Trưởng làng (NPC.NPCNAME)
        q.idNpcResponse = 5;
        // 0.9.15: đuôi -64 (máy chủ >= 915): mục tiêu + nơi trả hiện ngay trên hàng
        q.muc = "Hạ 10 Rattata ở Pallet";
        q.tra = "Xatu (Pewter)";
        game.render.screen.GameScr.coMucTieuNv = true;
        game.render.screen.GameScr.mainQuest = q;
        game.model.Quest q2 = new game.model.Quest(2);
        q2.name = "Thư gửi giáo sư";
        q2.decript = "Mang thư tới phòng thí nghiệm.";
        q2.stateQuest = 0;
        q2.idNpcReceive = 21;   // Blaziken
        game.render.screen.GameScr.allQuestCanReceive.removeAllElements();
        game.render.screen.GameScr.allQuestCanReceive.addElement(q2);
        // mẫu quái (Pokédex)
        String[] tq = {"Rattata", "Pidgey", "Caterpie", "Oddish", "Pikachu", "Geodude", "Zubat", "Magikarp"};
        int[] dq = {19, 16, 10, 43, 25, 74, 41, 129};
        game.render.Res.monsterTemplates = new game.model.MonsterTemplate[tq.length];
        for (int i = 0; i < tq.length; i++) {
            game.model.MonsterTemplate m = new game.model.MonsterTemplate();
            m.name = tq[i];
            m.maxhp = 50 + i * 20;
            game.render.Res.monsterTemplates[i] = m;
            game.model.MonsterTemplate.pokeDex[i] = (short) dq[i];
        }
        // cửa hàng thuốc
        gs.shop.removeAllElements();
        // giao dịch
        game.model.ItemInInventory ra = (game.model.ItemInInventory) game.model.Char.inventory.elementAt(0);
        mc.tItems.removeAllElements();
        mc.tItems.addElement(ra);
        mc.rItems.removeAllElements();
        mc.rItems.addElement(game.model.Char.inventory.elementAt(1));
        mc.nameUserTrade = "Misty";
        // đội / xếp hạng / bạn
        game.model.Char.party.removeAllElements();
        game.model.Char.party.addElement(new game.model.PartyInfo(mc.ID, mc.name, 24, 0));
        game.model.Char.party.addElement(new game.model.PartyInfo(7, "Misty", 22, 3));
        mc.IDMasterParty = mc.ID;
        java.util.Vector top = new java.util.Vector();
        String[] tt = {"Red", "Blue", "Charmander99", "Gold", "Silver", "Crystal", "Brock"};
        for (int i = 0; i < tt.length; i++) {
            game.model.Char c = new game.model.Char();
            c.name = tt[i];
            c.level = (byte) (60 - i * 4);
            c.clazz = (byte) (i % 5);
            c.charXu = 90000000L / (i + 1);
            top.addElement(c);
        }
        game.render.screen.ListScr.gI().freindList = top;
        java.util.Vector hoc = new java.util.Vector();
        for (int i = 0; i < 3; i++) {
            game.model.skill.InfoSkillLearn sk = new game.model.skill.InfoSkillLearn();
            sk.idSkill = (byte) (6 + i);
            sk.name = game.pokemon.PokeChieu.ten(0, 6 + i);
            sk.decript = game.pokemon.PokeChieu.moTa(0, 6 + i);
            sk.price = 15000 * (i + 1);
            hoc.addElement(sk);
        }
        game.render.screen.GameScr.infoNewSkill.removeAllElements();
        game.render.screen.GameScr.infoNewSkill.addElement(hoc);
        TOP = top;
        giaLapDanhHieu();
        gs.switchToMe();
    }

    /** 0.9.7: bảng danh hiệu giả như máy chủ gửi (-110 loại 0) + nhãn danh hiệu của nhân vật chính. */
    static void giaLapDanhHieu() {
        try {
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream o = new java.io.DataOutputStream(b);
            o.writeByte(0);
            o.writeByte(0);
            o.writeShort(9713);
            String[] hiem = {"Thường", "Hiếm", "Sử thi", "Huyền thoại"};
            o.writeByte(hiem.length);
            for (String h : hiem) o.writeUTF(h);
            Object[][] d = {
                // id, tên, mô tả, gợi ý, độ hiếm, hình, trạng thái, ẩn, tiến độ, cần, phút còn, chỉ số, nguồn, mới
                {9713, "Ngư phủ Magikarp", "Câu cá 300 lần ở hồ.", "", 1, "song", 2, 0, 300, 300, -1, "+0,7% Né tránh", "thanh_tuu", 0},
                {9720, "Master Ball mùa này", "Đạt bậc Master Ball ở thang PvP mùa hiện tại. Giữ bậc để gia hạn.", "", 3, "khien", 1, 0, 3, 3, 3 * 1440 + 200, "+1% Chí mạng", "xep_hang", 1},
                {9707, "Nhà Pokédex học", "Nghiên cứu 50% số loài trong Pokédex.", "", 2, "sao", 1, 0, 50, 50, -1, "+0,9% Chí mạng", "pokedex", 0},
                {9700, "Huấn luyện viên tập sự", "Đạt cấp 10.", "", 0, "sao", 1, 0, 10, 10, -1, "+0,5% HP tối đa", "thanh_tuu", 0},
                {9702, "Huấn luyện viên tinh anh", "Đạt cấp 50.", "", 1, "nhanh", 0, 0, 24, 50, -1, "+0,7% Chí mạng", "thanh_tuu", 0},
                {9710, "Thợ săn trùm", "Góp sức hạ 25 trùm thế giới.", "", 1, "nanh", 0, 0, 7, 25, -1, "+0,7% Chí mạng", "boss", 0},
                {9734, "Bí ngô đêm hội", "Có mặt trong lễ Halloween. Hết hạn sau 30 ngày.", "", 1, "ma", 0, 0, 0, 1, 43200, "+0,7% Né tránh", "su_kien", 0},
                {9704, "Đỉnh cao huấn luyện", "Đạt cấp tối đa của năm đầu (91).", "", 3, "mat_troi", 0, 0, 24, 91, -1, "+1% HP tối đa", "thanh_tuu", 0},
                {9714, "???", "???", "Mặt hồ chỉ trả lời người đủ kiên nhẫn...", 2, "", 0, 1, 0, 1000, -1, "", "an", 0},
                {9738, "???", "???", "Ngã nhiều lần cũng là một kiểu bản lĩnh.", 0, "", 0, 1, 0, 100, -1, "", "an", 0},
                {9740, "???", "???", "Đã đặt chân tới mọi miền chưa?", 3, "", 0, 1, 0, 120, -1, "", "an", 0},
            };
            o.writeShort(d.length);
            for (Object[] r : d) {
                o.writeShort((Integer) r[0]);
                o.writeUTF((String) r[1]);
                o.writeUTF((String) r[2]);
                o.writeUTF((String) r[3]);
                o.writeByte((Integer) r[4]);
                o.writeUTF((String) r[5]);
                o.writeByte((Integer) r[6]);
                o.writeByte((Integer) r[7]);
                o.writeInt((Integer) r[8]);
                o.writeInt((Integer) r[9]);
                o.writeInt((Integer) r[10]);
                o.writeUTF((String) r[11]);
                o.writeUTF((String) r[12]);
                o.writeByte((Integer) r[13]);
            }
            game.pokemon.DanhHieuKho.doc(new java.io.DataInputStream(new java.io.ByteArrayInputStream(b.toByteArray())));
            game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
            java.io.ByteArrayOutputStream b2 = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream o2 = new java.io.DataOutputStream(b2);
            o2.writeByte(1);
            o2.writeShort(mc.ID);
            o2.writeShort(9713);
            o2.writeUTF("Ngư phủ Magikarp");
            o2.writeByte(1);
            game.pokemon.DanhHieuKho.doc(new java.io.DataInputStream(new java.io.ByteArrayInputStream(b2.toByteArray())));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static java.util.Vector TOP;

    /**
     * Giả MÁY CHỦ ẢNH (lệnh -51): ô icon đang chờ -> nạp đúng tệp server sẽ gửi (server/map/icon_pokemon theo
     * com.htbb.pokemon.IconPokemon.doiAnh, id 14000+n, hoặc map/icon/id.png). Gọi trên luồng sự kiện trước mỗi ảnh.
     */
    static void giaMayChuAnh() {
        try {
            java.lang.reflect.Field f = game.render.screen.GameData.class.getDeclaredField("BANG");
            f.setAccessible(true);
            game.model.ImageIcon[] b = (game.model.ImageIcon[]) f.get(null);
            java.io.File goc = new java.io.File(System.getProperty("htbb.mayChu", "../server"));
            for (int id = 0; id < b.length; id++) {
                game.model.ImageIcon ic = b[id];
                if (ic == null || !ic.isLoad) continue;
                java.io.File t = null;
                if (id >= 14000 && id < 15000) {
                    t = new java.io.File(goc, "map/icon_pokemon/" + (id - 14000) + ".png");
                } else if (id >= 15000 && id < 16000) {
                    t = new java.io.File(goc, "map/icon_do_moi/" + (id - 15000) + ".png");   // IconDoMoi (đồ họ / vùng)
                } else {
                    int n = -1;
                    try {
                        n = com.htbb.pokemon.IconPokemon.doiAnh(id);
                    } catch (Throwable e) {
                        n = -1;
                    }
                    t = n >= 0 ? new java.io.File(goc, "map/icon_pokemon/" + n + ".png") : new java.io.File(goc, "map/icon/" + id + ".png");
                }
                if (t.isFile()) {
                    byte[] d = java.nio.file.Files.readAllBytes(t.toPath());
                    ic.img = javax.microedition.lcdui.Image.createImage(d, 0, d.length);
                    ic.isLoad = false;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static void shopThuoc() {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        gs.shop.removeAllElements();
        byte[] loai = {1, 2, 3, 4, 5, 6};
        for (int i = 0; i < loai.length; i++) {
            game.model.ItemInInventory it = new game.model.ItemInInventory();
            it.catagory = 4;
            it.potionType = loai[i];
            it.isEnoughData = true;
            gs.shop.addElement(it);
        }
    }

    static void shopDo() {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        gs.shop.removeAllElements();
        for (int i = 0; i < 13; i++) {
            game.model.ItemInInventory it = new game.model.ItemInInventory();
            it.idTemplate = (short) i;
            it.charClazz = 0;
            it.level = (short) (5 + i);
            it.isEnoughData = true;
            it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 0, (short) (30 + i * 2)));
            it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 1, (short) (8 + i)));
            gs.shop.addElement(it);
        }
    }

    /** Gói -116 kiểu 0 giả (sau byte kiểu): 4 ô + vài món mỗi ô (màn tủ đồ). */
    static byte[] goiTuDo() throws java.io.IOException {
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream o = new java.io.DataOutputStream(bo);
        String[][] oo = {{"trang_phuc", "Trang phục", "15"}, {"hao_quang", "Hào quang", "10"}, {"skin_thu", "Skin thú cưỡi", "5"},
            {"the_dong_hanh", "Thẻ bạn đồng hành", "10"}};
        o.writeByte(oo.length);
        for (int i = 0; i < oo.length; i++) {
            o.writeUTF(oo[i][0]);
            o.writeUTF(oo[i][1]);
            o.writeShort(Integer.parseInt(oo[i][2]));
            o.writeShort(Integer.parseInt(oo[i][2]));
            o.writeByte(2);
            o.writeUTF("hp");
            o.writeUTF("HP");
            o.writeUTF("tan_cong");
            o.writeUTF("Tấn công");
            o.writeByte(0);
            o.writeUTF(i == 0 ? "tp_tan_thu" : "");
            o.writeInt(i == 0 ? 3600 : 0);
        }
        o.writeShort(35);
        o.writeShort(50);
        o.writeShort(10);
        Object[][] mon = {
            {"tp_tan_thu", 0, 1, "Áo huấn luyện viên tân thủ", 0, 15628, 3, "Miễn phí ở cấp 5", "", 0, "Mũ lưỡi trai đỏ và khăn trắng."},
            {"tp_halloween", 0, 11, "Trang phục Halloween", 3, 15638, 0, "Sự kiện Halloween", "Hạ quái 120/500", 0, "Mũ phù thuỷ, áo choàng tím và đũa phép."},
            {"tp_tham_tu", 0, 6, "Trang phục Thám tử", 2, 15633, 1, "Ngày cộng đồng", "Đủ 200/200", 1, "Mũ thám tử và kính lúp."},
            {"tp_hiep_si", 0, 23, "Trang phục Hiệp sĩ", 3, 15650, 0, "Cửa hàng: 250 lượng", "", 3, "Mũ giáp lông đỏ và áo choàng."},
            {"tp_tho_mo", 0, 29, "Trang phục Thợ mỏ", 1, 15656, 0, "Chế tạo ở Machamp", "10 Bụi sao, 4 Mảnh sao, 2 giờ xu", 2, "Mũ thợ mỏ có đèn."},
            {"tp_nha_vo_dich", 0, 18, "Trang phục Nhà vô địch", 4, 15645, 2, "Thành tựu: Top 10 Nhà vô địch", "", 0, "Vương miện và áo choàng đỏ."},
            {"hq_tap_su", 1, 1, "Hào quang tập sự", 0, 15660, 3, "Miễn phí ở cấp 10", "", 0, "Vòng sáng đầu tiên."},
            {"hq_lua", 1, 2, "Hào quang Lửa", 2, 15661, 0, "Chế tạo ở Machamp", "5 Mảnh sao, 2 giờ xu", 2, "Vòng lửa."},
            {"hq_cau_vong", 1, 11, "Hào quang Cầu vồng", 4, 15670, 0, "Thành tựu: Người hoàn thiện Pokédex", "Pokédex 61/90", 0, "Cầu vồng."},
        };
        o.writeShort(mon.length);
        for (int i = 0; i < mon.length; i++) {
            Object[] m = mon[i];
            o.writeUTF((String) m[0]);
            o.writeByte((Integer) m[1]);
            o.writeByte((Integer) m[2]);
            o.writeUTF((String) m[3]);
            o.writeByte((Integer) m[4]);
            o.writeShort((Integer) m[5]);
            o.writeByte((Integer) m[6]);
            o.writeUTF((String) m[7]);
            o.writeUTF((String) m[8]);
            o.writeByte((Integer) m[9]);
            o.writeUTF((String) m[10]);
        }
        return bo.toByteArray();
    }

    static void moMan(String ten) {
        game.render.Canvas.isPointerDown = false;
        game.pokemon.DungDo.xoaHet();   // thông báo của màn trước không lọt sang màn này
        try {
            datTinh(Class.forName("game.render.screen.BangTuDong"), "mo", Boolean.FALSE);
        } catch (Throwable e) {
            // không có bảng tự động
        }
        game.render.Canvas.endDlg();
        game.render.Canvas.menu.showMenu = false;
        game.render.screen.TuDong.dang = false;
        datTinh(game.render.screen.TuDong.class, "coServer", Boolean.FALSE);
        datTinh(htbb.pc.LopPhu.class, "caiDatMo", Boolean.FALSE);
        // thú bỏ tạm ở màn tab trống: trả lại (đúng thứ tự gốc: thú cưỡi trước, pet sau)
        if (THU_BO.size() > 0) {
            java.util.Vector a = game.model.Char.animal;
            java.util.Vector goc = new java.util.Vector();
            for (int l = 0; l < 2; l++) {
                for (int i = 0; i < a.size(); i++) if (game.pokemon.QuanLyThu.loai((game.model.AnimalInfo) a.elementAt(i)) == l) goc.addElement(a.elementAt(i));
                for (int i = THU_BO.size() - 1; i >= 0; i--) if (game.pokemon.QuanLyThu.loai((game.model.AnimalInfo) THU_BO.elementAt(i)) == l) goc.addElement(THU_BO.elementAt(i));
            }
            a.removeAllElements();
            for (int i = 0; i < goc.size(); i++) a.addElement(goc.elementAt(i));
            THU_BO.removeAllElements();
        }
        datCuoi(ten.indexOf("cuoi") >= 0 && !ten.startsWith("nv_thu_dang_dung") && !ten.startsWith("nv_thu_trong"));
        datQuanLyThu(ten.endsWith("_quan_ly") || ten.indexOf("_dang_dung") >= 0,
                ten.startsWith("nv_thu_dang_dung") ? 2 : "nv_thu_di_theo_dang_dung".equals(ten) ? 1 : 0);
        game.pokemon.ManNhanVat nv = game.pokemon.ManNhanVat.gI();
        dat(nv, "thuChon", null);
        dat(nv, "loaiThu", Integer.valueOf(0));   // 0.9.16: tab con mặc định mỗi màn (lối vào thật đặt lại bằng chạm)
        if (ten.startsWith("nv_thu_trong_tab")) {
            // tab trống: túi thú chỉ còn loại kia (máy chủ cũ, không cưỡi)
            int bo = ten.endsWith("0") ? 0 : 1;
            java.util.Vector a = game.model.Char.animal;
            for (int i = a.size() - 1; i >= 0; i--) {
                game.model.AnimalInfo an = (game.model.AnimalInfo) a.elementAt(i);
                if (game.pokemon.QuanLyThu.loai(an) == bo) {
                    THU_BO.addElement(an);
                    a.removeElementAt(i);
                }
            }
            dat(nv, "loaiThu", Integer.valueOf(bo));
        }
        if ("the_gioi".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
        } else if ("menu".equals(ten)) {
            game.pokemon.ManMenu.mo();
        } else if ("nv_trang_bi".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
        } else if ("nv_o_co_do".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 1);   // khăn quàng (đang mặc + 1 trong túi)
        } else if ("nv_o_trong".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 7);   // găng: không... (có 1) -> dùng Ruy băng? chọn ô Huy hiệu 2 (trống)
            dat(nv, "o", 6);
        } else if ("tui_sao_goi".equals(ten) || "tui_nguyen_lieu_su_kien".equals(ten)) {
            // gói -104 kiểu 9 thật qua MessageHandler -> DungDo -> MoTaVatPham
            try {
                java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                java.io.DataOutputStream o = new java.io.DataOutputStream(bo);
                o.writeByte(game.pokemon.MoTaVatPham.KIEU);
                o.writeUTF("64\tVật phẩm gọi thú cưỡi\tDùng để nhận thú cưỡi Tauros vĩnh viễn (vào Ô thú, cấp 1).\t1\t\n"
                        + "103\tNguyên liệu sự kiện\tLá vàng: nguyên liệu sự kiện.\t0\tĐổi ở NPC sự kiện khi sự kiện mở (không dùng trực tiếp từ túi).");
                java.lang.reflect.Constructor<network.Message> k0 = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
                k0.setAccessible(true);
                game.pokemon.DungDo.nhan(k0.newInstance(Byte.valueOf((byte) -104), bo.toByteArray()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            game.model.MainChar mc0 = game.render.Canvas.gameScr.mainChar;
            if (mc0.potions.length < 162) {
                int[] moi = new int[162];
                System.arraycopy(mc0.potions, 0, moi, 0, mc0.potions.length);
                mc0.potions = moi;
            }
            mc0.potions[64] = 1;
            mc0.potions[103] = 3;
            game.pokemon.TheVatPham.mo(new Integer("tui_sao_goi".equals(ten) ? 64 : 103), game.pokemon.TheVatPham.TUI, false, null);
        } else if ("nv_chi_tiet".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 1);
            Object mon = game.model.Char.inventory.elementAt(6);   // Khăn quàng xanh (idTemplate 10)
            for (int i = 0; i < game.model.Char.inventory.size(); i++) {
                game.model.ItemInInventory it = (game.model.ItemInInventory) game.model.Char.inventory.elementAt(i);
                if (it.idTemplate == 10) mon = it;
            }
            dat(nv, "xem", mon);
        } else if ("nv_hanh_trang".equals(ten)) {
            game.pokemon.ManNhanVat.moHanhTrang();
        } else if ("nv_chi_so".equals(ten)) {
            game.pokemon.ManNhanVat.mo(1);
        } else if ("nv_ky_nang".equals(ten)) {
            game.pokemon.ManNhanVat.mo(2);
        } else if ("nv_thu".equals(ten)) {
            game.pokemon.ManNhanVat.mo(3);
        } else if ("nv_cuoi_trang_bi".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
        } else if ("nv_cuoi_ds_thu".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 11);
        } else if ("nv_ds_thu".equals(ten) || "nv_cuoi_ds_thu_quan_ly".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 11);
        } else if ("nv_ds_thu_quan_ly".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 11);
            dat(nv, "thuChon", game.model.Char.animal.elementAt(1));   // Dragonite
        } else if ("nv_thu_quan_ly".equals(ten)) {
            game.pokemon.ManNhanVat.mo(3);
            dat(nv, "thuChon", game.model.Char.animal.elementAt(2));   // Hoothoot (có 2 bản trùng)
        } else if ("nv_thu_dang_dung".equals(ten) || "nv_thu_di_theo_dang_dung".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 11);
        } else if ("nv_thu_dang_dung_chon".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 11);
            dat(nv, "thuChon", game.pokemon.QuanLyThu.cuoi);
        } else if ("nv_thu_dang_dung_tab".equals(ten) || "nv_thu_dang_dung_tab_di_theo".equals(ten) || "nv_thu_dang_dung_tab_nho".equals(ten)
                || ten.startsWith("nv_thu_dang_dung_tha_trung") || ten.startsWith("nv_thu_trong_tab")) {
            game.pokemon.ManNhanVat.mo(3);
            if ("nv_thu_dang_dung_tab_nho".equals(ten)) {
                dat(nv, "loaiThu", Integer.valueOf(1));   // tab Thú mở lại tab con dùng lần cuối (Đi theo)
            }
        } else if (ten.startsWith("nv_thu_dang_dung_tb_")) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "loaiThu", Integer.valueOf(1));   // lần trước xem Đi theo: chạm ô Thú cưỡi vẫn mở tab Thú cưỡi
        } else if ("nv_cuoi_thu".equals(ten)) {
            game.pokemon.ManNhanVat.mo(3);
        } else if ("the_gioi_cuoi".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            // đặt người chơi (cưỡi Rapidash) + pet cạnh NPC đầu tiên trong khung nhìn
            game.render.screen.GameScr gs = game.render.Canvas.gameScr;
            for (int i = 0; i < gs.actors.size(); i++) {
                game.model.Actor ac = (game.model.Actor) gs.actors.elementAt(i);
                if (ac.isNPC()) {
                    gs.mainChar.x = (short) (ac.x + 90);
                    gs.mainChar.y = (short) (ac.y + 10);
                    break;
                }
            }
            if (gs.mainChar.petCanh != null) {
                gs.mainChar.petCanh.x = (short) (gs.mainChar.x - 34);
                gs.mainChar.petCanh.y = gs.mainChar.y;
            }
            if (!gs.actors.contains(gs.mainChar)) gs.actors.addElement(gs.mainChar);
            game.render.Util.quickSort(gs.actors);   // khung vẽ đi theo thứ tự y (GameScr.update sắp mỗi nhịp)
        } else if ("nv_ngoai_hinh".equals(ten)) {
            game.pokemon.ManNhanVat.mo(4);
        } else if (ten.startsWith("nv_tu_do")) {
            game.pokemon.ManNhanVat.mo(4);
            try {
                game.pokemon.ManTuDo.napThu(goiTuDo());
            } catch (Throwable e) {
                e.printStackTrace();
            }
            game.pokemon.ManTuDo.xinLuc = Long.MAX_VALUE / 2;   // không gửi xin (không máy chủ)
            game.pokemon.ManTuDo.tab = ten.endsWith("_an") ? game.pokemon.ManTuDo.TAB_AN : ten.endsWith("hao_quang") ? 1 : 0;
            game.pokemon.ManTuDo.boThu();
            if (ten.endsWith("_thu")) {
                game.pokemon.ManTuDo.thu((game.pokemon.ManTuDo.Mon) game.pokemon.ManTuDo.MON.elementAt(1));
            }
        } else if ("menu_ngu_canh".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            java.util.Vector v = new java.util.Vector();
            String[] c = {"Nói chuyện", "Mua bán", "Nhiệm vụ", "Giao dịch", "Mời vào đội", "Kết bạn", "Thông tin"};
            for (int i = 0; i < c.length; i++) {
                v.addElement(new game.model.Command(c[i], new game.model.IAction() {
                    public void perform() {
                    }
                }));
            }
            game.pokemon.MenuNguCanh.mo(v, false);
        } else if ("tui_loc_thuoc".equals(ten)) {
            game.pokemon.ManNhanVat.moHanhTrang();
            dat(nv, "loc", Integer.valueOf(game.pokemon.LuoiTui.LOC_THUOC));
        } else if ("tui_loc_nguyen_lieu".equals(ten)) {
            game.pokemon.ManNhanVat.moHanhTrang();
            dat(nv, "loc", Integer.valueOf(3));
        } else if ("xac_nhan".equals(ten)) {
            game.pokemon.ManNhanVat.moHanhTrang();
            game.pokemon.HopXacNhan.hoiGia("Mua thuốc", "Mua 10 Thuốc hồi HP?", "Mua", game.pokemon.TienTe.XU, 250, null);
        } else if ("cua_hang_thuoc".equals(ten) || "cua_hang_thuoc_chi_tiet".equals(ten)) {
            shopThuoc();
            game.pokemon.ManCuaHang.mo(game.pokemon.ManCuaHang.THUOC, new byte[]{0, 19});
            if (ten.endsWith("chi_tiet")) {
                dat(game.pokemon.ManCuaHang.gI(), "chon", game.render.Canvas.gameScr.shop.elementAt(0));
                dat(game.pokemon.ManCuaHang.gI(), "soLuong", Integer.valueOf(12));
            }
        } else if ("cua_hang_do".equals(ten) || "cua_hang_do_chi_tiet".equals(ten)) {
            shopDo();
            game.pokemon.ManCuaHang.mo(game.pokemon.ManCuaHang.DO, new byte[]{0, 9});
            if (ten.endsWith("chi_tiet")) {
                dat(game.pokemon.ManCuaHang.gI(), "chon", game.render.Canvas.gameScr.shop.elementAt(1));
            }
        } else if ("cua_hang_ban".equals(ten)) {
            shopDo();
            game.pokemon.ManCuaHang.mo(game.pokemon.ManCuaHang.DO, new byte[]{0, 9});
            dat(game.pokemon.ManCuaHang.gI(), "tab", Integer.valueOf(1));
            dat(game.pokemon.ManCuaHang.gI(), "chon", game.model.Char.inventory.elementAt(2));
        } else if ("nhiem_vu".equals(ten)) {
            game.pokemon.ManNhiemVu.mo();
        } else if ("nhiem_vu_chi_tiet".equals(ten)) {
            game.pokemon.ManNhiemVu.mo();
            dat(game.pokemon.ManNhiemVu.gI(), "tab", Integer.valueOf(0));
            dat(game.pokemon.ManNhiemVu.gI(), "chon", game.render.screen.GameScr.mainQuest);
        } else if ("giao_dich".equals(ten)) {
            game.pokemon.ManGiaoDich.mo();
        } else if ("pokedex".equals(ten)) {
            game.pokemon.ManPokedex.mo();
        } else if ("pokedex_chi_tiet".equals(ten)) {
            game.pokemon.ManPokedex.mo();
            dat(game.pokemon.ManPokedex.gI(), "chon", Integer.valueOf(4));
        } else if ("xep_hang".equals(ten)) {
            game.pokemon.ManXepHang.mo();
            game.pokemon.ManXepHang.nhan(4, TOP);
        } else if ("doi".equals(ten)) {
            game.pokemon.ManDoi.mo(0);
        } else if (ten.startsWith("danh_hieu")) {
            game.pokemon.ManDanhHieu.moTab(ten.endsWith("chua_co") ? 1 : ten.endsWith("_an") ? 2 : 0);
            if (ten.endsWith("chi_tiet")) {
                dat(game.pokemon.ManDanhHieu.gI(), "xem", game.pokemon.DanhHieuKho.loc(0).elementAt(1));
            }
        } else if ("ky_nang_hoc".equals(ten)) {
            game.pokemon.ManNhanVat.mo(2);
            dat(nv, "hocMoi", Boolean.TRUE);
        } else if ("ky_nang_chi_tiet".equals(ten)) {
            game.pokemon.ManNhanVat.mo(2);
            dat(nv, "chieu", Integer.valueOf(1));
        } else if ("thong_bao_kich_hoat".equals(ten) || "suc_khoe".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            if (ten.startsWith("thong")) {
                game.pokemon.DungDo.bieuNgu("Tài khoản chưa kích hoạt: vẫn chơi bình thường, chỉ tạm khoá giao dịch, chợ, tặng quà. "
                        + "Kích hoạt tại https://huyenthoaibaoboi.fun/kichhoat.");
                game.pokemon.DungDo.bao("Đã dùng Thuốc hồi HP (+200 HP).");
            } else {
                game.render.Canvas.gameScr.onMSGServer("Chơi quá 180 phút mỗi ngày sẽ hại sức khoẻ.");
            }
        } else if ("chon_chieu".equals(ten)) {
            // bộ chọn chiêu cho ô nhanh (chạm ô trong tab Kỹ năng)
            game.pokemon.ManNhanVat.mo(2);
            game.pokemon.ChonChieuNhanh.moO(1);
        } else if ("goi_y_chieu".equals(ten)) {
            // giữ nút đòn chính 1 s -> gợi ý tên / hệ / PP / hồi chiêu
            game.render.Canvas.gameScr.switchToMe();
            try {
                Class<?> h = Class.forName("game.render.screen.HudRong");
                java.lang.reflect.Method mx = h.getDeclaredMethod("xDanh"), my = h.getDeclaredMethod("yDanh");
                mx.setAccessible(true);
                my.setAccessible(true);
                game.render.Canvas.px = ((Integer) mx.invoke(null)).intValue();
                game.render.Canvas.py = ((Integer) my.invoke(null)).intValue();
                game.render.Canvas.isPointerDown = true;
                datTinh(h, "giuO", Integer.valueOf(4));
                datTinh(h, "giuLuc", Long.valueOf(System.currentTimeMillis() - 1000));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if ("tu_dong".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            try {
                java.lang.reflect.Field f = game.render.screen.TuDong.class.getDeclaredField("thuTu");
                f.setAccessible(true);
                byte[] t = (byte[]) f.get(null);
                t[0] = 1;
                t[1] = 2;
                t[2] = 0;
                java.lang.reflect.Method m = Class.forName("game.render.screen.BangTuDong").getDeclaredMethod("mo");
                m.setAccessible(true);
                m.invoke(null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if ("tu_dong_dang".equals(ten)) {
            // 0.9.6: Tự động đang chạy (máy chủ báo còn 4:37 hôm nay) - thẻ trạng thái "Đang đánh · 4:37"
            moMan("tu_dong");
            datTinh(game.render.screen.TuDong.class, "coServer", Boolean.TRUE);
            datTinh(game.render.screen.TuDong.class, "svConLai", Integer.valueOf(4 * 3600 + 37 * 60));
            datTinh(game.render.screen.TuDong.class, "svToiDa", Integer.valueOf(6 * 3600));
            datTinh(game.render.screen.TuDong.class, "svLuc", Long.valueOf(System.currentTimeMillis()));
            datTinh(game.render.screen.TuDong.class, "trangThai", "Đang đánh Zigzagoon");
            game.render.screen.TuDong.dang = true;
        } else if ("thu_qua".equals(ten)) {
            game.render.screen.MessageScr.nMSG = 2;
            game.pokemon.ManMenu.mo();
            try {
                java.lang.reflect.Method m = game.pokemon.ManMenu.class.getDeclaredMethod("lam", int.class);
                m.setAccessible(true);
                m.invoke(null, 6);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if ("cai_dat_tai_khoan".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            if (game.pokemon.VungCham.MIN >= 24) {
                // máy cảm ứng (APK / J2ME): Cài đặt = Menu nhanh (có "Nhập giftcode")
                game.render.screen.CauGiaoDien.moCaiDat();
            } else {
                htbb.pc.LopPhu.moCaiDat();
                datTinh(htbb.pc.LopPhu.class, "tab", Integer.valueOf(4));
            }
        } else if (ten.startsWith("npc_")) {
            moNpc(ten.substring(4));
        } else if ("hop_thong_bao".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            game.render.Canvas.startOKDlg("Bạn chưa đủ cấp để vào khu vực này. Hãy luyện thêm rồi quay lại nhé!");
        } else if ("hop_thong_bao_dai".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            // thông báo dài của máy chủ (biểu ngữ -> "Xem"): danh sách mục, mỗi mục tiêu đề đậm + mô tả
            game.pokemon.HopThongBao.bao("Sự kiện Trung thu", "Đăng nhập mỗi ngày: nhận 1 Bánh trung thu và 2.000 xu.\n"
                    + "Săn đèn lồng: hạ Pokémon hoang dã ở Rừng Viridian để nhặt Đèn lồng, đổi quà ở NPC Trưởng làng.\n"
                    + "Đua top cấp: 10 người cấp cao nhất nhận Huy hiệu Trăng rằm và 50 lượng khoá.\n"
                    + "Hộp quà Trăng: mở ra ngẫu nhiên Thuốc hồi HP lớn, Ether lớn hoặc Cỏ hồi sinh.\n"
                    + "Thời gian: từ 15/10 đến 25/10, máy chủ tự kết thúc lúc 04:00 sáng ngày 26/10.\n"
                    + "Lưu ý: phần thưởng gửi vào hộp thư, nhận trong 7 ngày.\n"
                    + "Đổi quà: mang 10 Đèn lồng tới NPC Trưởng làng để đổi 1 Hộp quà Trăng.\n"
                    + "Bảng xếp hạng: xem tại menu Xếp hạng, cập nhật mỗi 10 phút.");
        } else if (ten.startsWith("hst_") || ten.startsWith("cau_") || ten.startsWith("hop_hst_") || ten.startsWith("hop_cau_")) {
            moSinhThai(ten);
        } else if ("the_phan_thuong".equals(ten)) {
            hopMau(ten);
        } else if ("xem_trang_bi".equals(ten) || "xem_trang_bi_the".equals(ten)) {
            game.render.Canvas.gameScr.onViewCharInfo(nguoiKhac());
        } else if ("xem_do_thu".equals(ten)) {
            game.render.Canvas.gameScr.onViewAnimalInfo(nguoiKhac());
        } else if ("shop_su_kien".equals(ten) || "shop_su_kien_the".equals(ten)) {
            giaLapShopNew("Mua", "Shop Trung Thu", true);
            game.pokemon.ManCuaHang.moMayChu();
        } else if ("tui_do_lay_ra_the".equals(ten)) {
            giaLapShopNew("Lấy ra", "Túi Đồ", false);
            game.pokemon.ManCuaHang.moMayChu();
        } else if ("cua_hang_nguyen_lieu".equals(ten) || "cua_hang_nguyen_lieu_the".equals(ten)) {
            giaLapNguyenLieu();
            game.render.Canvas.gameScr.onNPCSellGemItem(null);
        } else if ("kho_do".equals(ten) || "kho_do_the".equals(ten)) {
            java.util.Vector kho = new java.util.Vector();
            for (int i = 0; i < 2 && i < game.model.Char.inventory.size(); i++) kho.addElement(game.model.Char.inventory.elementAt(i));
            game.render.Canvas.gameScr.onNPCKeepItem(kho);
        } else if ("luyen_do".equals(ten) || "luyen_do_dat".equals(ten)) {
            giaLapNguyenLieu();
            game.render.Canvas.gameScr.onImbue((short) 5000, (byte) 0);
        } else if ("kham".equals(ten)) {
            giaLapNguyenLieu();
            game.render.Canvas.gameScr.onKham((short) 3000, (byte) 0);
        } else if ("hop_thanh".equals(ten)) {
            giaLapNguyenLieu();
            game.render.Canvas.gameScr.onEpNgoc((short) 2000);
        } else if ("hop_tin_bang".equals(ten)) {
            game.pokemon.HopThongBao.tinNhiemVuBang("Nhiệm vụ đội: hạ 30 Geodude ở Đường hầm đá. Phần thưởng: 2000 xu, 500 điểm cống hiến.");
        } else if (ten.startsWith("hop_") && hopMau(ten)) {
            // đã mở trong hopMau
        } else if ("so_luong".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            game.render.Canvas.inputDlg.setInfo("Số lượng mua: Thuốc hồi HP", new game.model.IAction() {
                public void perform() {
                    game.render.Canvas.endDlg();
                }
            }, game.render.screen.TField.INPUT_TYPE_NUMERIC, 3, true);
            game.render.Canvas.inputDlg.show();
        }
    }

    /**
     * 0.9.6 NPC ngoài trời (bản đồ thị trấn giả lập): chạm NPC = thẻ GO neo cạnh NPC, lệnh chức năng thật của NPC đó
     * (moChucNangNpc -> GameService.requestNPCInfo / menu NPC). Không máy chủ: chỉ kiểm thẻ + vị trí (không chồng HUD).
     */
    static void moNpc(String ten) {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        gs.switchToMe();
        int loai = "chansey".equals(ten) ? 0 : "kecleon".equals(ten) ? 1 : 9;
        String tenNpc = "chansey".equals(ten) ? "Chansey" : "kecleon".equals(ten) ? "Kecleon" : "Kangaskhan";
        String[] lenh = "chansey".equals(ten) ? new String[]{"Hồi phục Pokémon", "Mua thuốc", "Nói chuyện"}
                : "kecleon".equals(ten) ? new String[]{"Mua bán", "Nói chuyện"} : new String[]{"Giữ đồ", "Ngân hàng xu, lượng", "Nói chuyện"};
        game.model.Actor npc = null;
        for (int i = 0; i < gs.actors.size(); i++) {
            game.model.Actor a = (game.model.Actor) gs.actors.elementAt(i);
            if (a.isNPC()) {
                npc = a;
                break;
            }
        }
        if (npc != null) {
            gs.focusedActor = npc;
            gs.mainChar.x = (short) (npc.x - 40);
            gs.mainChar.y = (short) (npc.y + 8);
        }
        java.util.Vector v = new java.util.Vector();
        final int lo = loai;
        for (int i = 0; i < lenh.length; i++) {
            v.addElement(new game.model.Command(lenh[i], new game.model.IAction() {
                public void perform() {
                    game.render.Canvas.gameScr.moChucNangNpc(lo);
                }
            }));
        }
        game.pokemon.MenuNguCanh.mo(v, true);
        dat(game.pokemon.MenuNguCanh.gI(), "tieuDe", tenNpc);
    }

        /** Đang cưỡi Rapidash (gói 15 idAnimal + -93 dex 78) + Hoothoot đi theo (-94) / hay không cưỡi gì. */
    static void datCuoi(boolean cuoi) {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        game.model.MainChar mc = gs.mainChar;
        mc.idAnimal = (byte) (cuoi ? 0 : -1);
        mc.idPaintIconAnimal = 0;
        mc.infoAnimal = cuoi ? "Rapidash lv 5\nTốc độ +2" : "";
        mc.pokeThuDex = (short) (cuoi ? 78 : 0);
        mc.pokePetDex = (short) (cuoi ? 163 : 0);
        game.pokemon.PetDuyNhat.cat(gs.actors, mc);
        if (cuoi) {
            game.model.AnimalMove p = new game.model.AnimalMove();
            p.myOwner = mc;
            p.idSameMyOwnerID = mc.ID;
            p.x = (short) (mc.x - 24);
            p.y = mc.y;
            p.catagory = (byte) 12;
            p.totalTime = 600;   // phút còn hạn (gói 15) - 0 = hết hạn, pet mờ dần
            p.timeStart = System.currentTimeMillis();
            game.pokemon.PetDuyNhat.dat(gs.actors, mc, p);
        }
    }

    static final game.model.IAction KHONG = new game.model.IAction() {
        public void perform() {
            game.render.Canvas.endDlg();
        }
    };

    /** Mọi loại hộp thoại (0.9.5): mở đúng đường gọi của game. true = đã mở. */
    static boolean hopMau(String ten) {
        game.render.Canvas.gameScr.switchToMe();
        if ("hop_ban".equals(ten)) {
            game.pokemon.HopXacNhan.hoiGia("Bán vật phẩm", "Bán 3 Khăn quàng xanh?", "Bán", game.pokemon.TienTe.XU, 900, null);
        } else if ("hop_mac_do".equals(ten)) {
            game.render.Canvas.startYesNoDlg("Mặc Khăn quàng xanh? Trang bị sẽ gắn với Pokémon này.", KHONG, KHONG);
        } else if ("hop_len_cap".equals(ten)) {
            game.render.Canvas.startOKDlg("Chúc mừng! Charmander99 đã lên cấp 25 và nhận 1 điểm kỹ năng.");
        } else if ("hop_tien_hoa".equals(ten)) {
            game.render.Canvas.startOKDlg("Charmander đang tiến hoá... Chúc mừng! Charmander đã tiến hoá thành Charmeleon.");
        } else if ("hop_phan_thuong".equals(ten)) {
            game.render.Canvas.startOKDlg("Hoàn thành nhiệm vụ Dẹp loạn Rattata: +500 xu, +120 điểm kinh nghiệm, +2 Thuốc hồi HP.");
        } else if ("hop_phan_thuong_xem".equals(ten) || "the_phan_thuong".equals(ten)) {
            // thẻ thông báo phần thưởng -> "Xem" -> hộp phần thưởng có hàng ô vật phẩm
            game.pokemon.HopThongBao.bao("Phần thưởng", "Hoàn thành nhiệm vụ Dẹp loạn Rattata: +500 xu, +120 điểm kinh nghiệm, +2 Thuốc hồi HP, +1 Đá tiến hoá Lửa.");
        } else if ("hop_nhan_nhiem_vu".equals(ten)) {
            game.render.Canvas.startYesNoDlg("Nhận nhiệm vụ Dẹp loạn Rattata? Hạ 10 Rattata ở Tuyến đường 1 rồi báo lại Trưởng làng.", KHONG, KHONG);
        } else if ("hop_bao_tri".equals(ten)) {
            game.render.Canvas.startOKDlg("Máy chủ sẽ bảo trì lúc 04:00 (còn 10 phút). Vui lòng thoát game để không mất dữ liệu.");
        } else if ("hop_loi".equals(ten)) {
            game.pokemon.HopThongBao.mo("Không thực hiện được", "Túi đã đầy, hãy bỏ bớt vật phẩm rồi thử lại.", game.pokemon.HopThongBao.LOI,
                    new String[]{"Đóng"}, new int[]{game.pokemon.Bang.TRUNG}, new game.model.IAction[]{KHONG}, KHONG);
        } else if ("hop_mat_ket_noi".equals(ten)) {
            game.pokemon.HopThongBao.mo("Mất kết nối", "Đã thử kết nối lại 3 lần nhưng chưa được. Kiểm tra mạng rồi bấm Kết nối lại.",
                    game.pokemon.HopThongBao.CANH_BAO, new String[]{"Thoát", "Kết nối lại"}, null, new game.model.IAction[]{KHONG, KHONG}, KHONG);
        } else if ("hop_hoi_sinh".equals(ten)) {
            game.render.Canvas.startYesNoDlg("Bạn muốn hồi sinh tại chỗ không? Chi phí 1 Cỏ hồi sinh.", KHONG, KHONG);
        } else if ("hop_giftcode".equals(ten)) {
            game.render.Canvas.inputDlg.setInfo("Nhập giftcode", KHONG, game.render.screen.TField.INPUT_TYPE_ANY, 20, false);
            game.render.Canvas.inputDlg.show();
        } else if ("hop_giftcode_ket_qua".equals(ten)) {
            game.render.Canvas.startOKDlg("Đổi giftcode thành công: +5.000 xu, +3 Thuốc hồi HP lớn.");
        } else if ("hop_moi_giao_dich".equals(ten)) {
            game.render.Canvas.startYesNoDlg("Pikachu88 đề nghị giao dịch với bạn.", KHONG, KHONG);
        } else if ("hop_moi_doi".equals(ten)) {
            game.render.Canvas.startYesNoDlg("Bulba77 mời bạn vào đội. Đồng ý?", KHONG, KHONG);
        } else if ("hop_dang_xuat".equals(ten)) {
            game.pokemon.HopXacNhan.hoi("Đăng xuất", "Thoát về màn đăng nhập?", "Đăng xuất", KHONG, KHONG);
        } else if ("hop_cho".equals(ten)) {
            game.render.Canvas.startWaitDlg("Đang gửi yêu cầu...", true);
        } else if ("hop_nhap_chu".equals(ten)) {
            game.render.Canvas.inputDlg.setInfo("Đặt tên cho thú cưỡi", KHONG, game.render.screen.TField.INPUT_TYPE_ANY, 15, false);
            game.render.Canvas.inputDlg.show();
        } else if ("hop_vat_pham".equals(ten)) {
            game.pokemon.DungDo.bao("Đã dùng Thuốc hồi HP: +200 HP.");
            game.pokemon.DungDo.loi("Không dùng được: HP đã đầy.");
            game.pokemon.DungDo.bao("Nhận được 2 Ether lớn.");
        } else {
            return false;
        }
        return true;
    }

    static final String[] HOP = {"hop_ban", "hop_mac_do", "hop_len_cap", "hop_tien_hoa", "hop_phan_thuong", "hop_nhan_nhiem_vu", "hop_bao_tri",
        "hop_loi", "hop_mat_ket_noi", "hop_hoi_sinh", "hop_giftcode", "hop_giftcode_ket_qua", "hop_moi_giao_dich", "hop_moi_doi", "hop_dang_xuat",
        "hop_cho", "hop_nhap_chu", "hop_vat_pham"};

    /** Hộp vừa vẽ canh giữa vùng an toàn (±2 px) và trong giới hạn cỡ. */
    static void kiemHop(String nhan) {
        Object d = game.render.Canvas.currentDialog;
        if (d == null) {
            return;   // thông báo nhanh (không phải hộp)
        }
        int[] r = game.pokemon.HopThongBao.VUA_VE;
        int cx = r[0] + r[2] / 2, cy = r[1] + r[3] / 2;
        int tx = (game.pokemon.Bang.trai() + game.pokemon.Bang.phai()) / 2, ty = (game.pokemon.Bang.tren() + game.pokemon.Bang.duoi()) / 2;
        kiem(Math.abs(cx - tx) <= 2 && Math.abs(cy - ty) <= 2, nhan + " hop canh giua: tam " + cx + "," + cy + " / " + tx + "," + ty);
        kiem(r[2] <= Math.max(game.pokemon.HopThongBao.rongToiDa(), 340) + 1, nhan + " hop rong " + r[2] + " <= toi da " + game.pokemon.HopThongBao.rongToiDa());
        kiem(r[3] <= game.pokemon.HopThongBao.caoToiDa() + 1, nhan + " hop cao " + r[3] + " <= 70% " + game.pokemon.HopThongBao.caoToiDa());
        kiem(r[0] >= game.pokemon.Bang.trai() && r[0] + r[2] <= game.pokemon.Bang.phai() && r[1] >= game.pokemon.Bang.tren()
                && r[1] + r[3] <= game.pokemon.Bang.duoi(), nhan + " hop trong vung an toan");
    }

    /** Tấm vừa vẽ (nếu màn có tấm): canh giữa ±2, rộng <= 80 % vùng an toàn (hoặc cỡ tối thiểu), trong vùng an toàn. */
    static void kiemTam(String nhan, int demTruoc) {
        if (game.pokemon.Bang.demTam == demTruoc) {
            return;   // màn không có tấm (thế giới, hộp thoại...)
        }
        int[] r = game.pokemon.Bang.VUA_TAM;
        int L = game.pokemon.Bang.trai(), T = game.pokemon.Bang.tren(), R = game.pokemon.Bang.phai(), B = game.pokemon.Bang.duoi();
        int sw = R - L;
        kiem(Math.abs((r[0] - L) - (R - r[0] - r[2])) <= 2, nhan + " tam canh giua ngang: " + (r[0] - L) + " / " + (R - r[0] - r[2]));
        kiem(Math.abs((r[1] - T) - (B - r[1] - r[3])) <= 2, nhan + " tam canh giua doc: " + (r[1] - T) + " / " + (B - r[1] - r[3]));
        kiem(r[2] <= Math.max(sw * 80 / 100, game.pokemon.Bang.rongTamToiDa()) + 1 && r[2] <= sw,
                nhan + " tam rong " + r[2] + " <= 80% vung an toan " + sw);
        kiem(r[3] <= B - T + 1 && r[3] >= 100, nhan + " tam cao " + r[3] + " trong [100, " + (B - T) + "]");
        kiem(r[0] >= L && r[1] >= T && r[0] + r[2] <= R && r[1] + r[3] <= B, nhan + " tam trong vung an toan");
    }

    static java.awt.Font phongKiem;

    /**
     * 0.9.7 KIỂM CHỮ (mọi màn, mọi cỡ): (1) không ký tự thiếu nét (font ảnh J2ME: Font.thieuNet; font thật: canDisplay);
     * (2) mỗi dòng chữ nằm TRỌN trong khung pixel nhỏ nhất (vẽ trước nó) chứa điểm đầu dòng; (3) hai dòng chữ trong
     * khung không chồng nhau. Chữ trên thế giới (tên trên đầu, không khung) bỏ qua.
     */
    /**
     * 0.9.8: chụp một màn CÙNG kiểm của ChupTrongGame (chữ trọn trong khung / không đè / đủ nét - kiemChu, icon chỉ PNG -
     * VeTay = 0) cho các bộ kiểm màn riêng (KiemBoNgocBat, KiemPhongRieng, KiemKenhChat...). Lỗi cộng vào {@link #hong}.
     */
    static BufferedImage veKiem(int W, int H, String nhan) throws Exception {
        game.pokemon.VeTay.xoa();
        game.pokemon.GhiChu.xoa();
        game.pokemon.GhiChu.bat = true;
        game.render.screen.Font.thieuNet = 0;
        BufferedImage anh = ve(W, H);
        game.pokemon.GhiChu.bat = false;
        kiemChu(nhan);
        kiem(game.pokemon.VeTay.dem == 0, nhan + " icon ve bang code (" + game.pokemon.VeTay.dem + " lan, " + game.pokemon.VeTay.cuoi + ")");
        return anh;
    }

    static void kiemChu(String nhan) {
        kiem(game.render.screen.Font.thieuNet == 0, nhan + " ky tu thieu net (" + game.render.screen.Font.thieuNet + " lan, '"
                + game.render.screen.Font.thieuCuoi + "')");
        if (phongKiem == null) {
            try {
                phongKiem = htbb.pc.PhongChu.co(false, 12f);
            } catch (Throwable e) {
                phongKiem = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12);
            }
        }
        int n = game.pokemon.GhiChu.soChu, nk = game.pokemon.GhiChu.soKhung;
        int[] cx = game.pokemon.GhiChu.CX, cy = game.pokemon.GhiChu.CY, cw = game.pokemon.GhiChu.CW, ch = game.pokemon.GhiChu.CH;
        int[] ct = game.pokemon.GhiChu.CT;
        int[] kx = game.pokemon.GhiChu.KX, ky = game.pokemon.GhiChu.KY, kw = game.pokemon.GhiChu.KW, kh = game.pokemon.GhiChu.KH;
        int[] kt = game.pokemon.GhiChu.KT;
        int[] khungCua = new int[n];
        for (int i = 0; i < n; i++) {
            String s = game.pokemon.GhiChu.CS[i];
            String goc = s.endsWith("…") ? s.substring(0, s.length() - 1) : s.endsWith("..") ? s.substring(0, s.length() - 2) : null;
            boolean du = false;
            for (int j = 0; j < MAN_TEN_DU.length; j++) du |= nhan.startsWith(MAN_TEN_DU[j] + " ");
            if (goc == null && du) {
                // tên gọn "<Tên>.. C12" (ChuDe.cat giữ cấp): ở màn danh sách / thẻ đồ vẫn là bị cắt
                int c = Math.max(s.lastIndexOf(".. C"), s.lastIndexOf("… C"));
                if (c > 0) goc = s.substring(0, c);
            }
            if (goc != null && goc.trim().length() >= 4) {   // "Đ.." (Đang mặc / Đổi...) không phải tên đồ
                String[] ds = du ? TEN_DO : TEN_DO_CAP;
                for (int j = 0; j < ds.length; j++) {
                    if (ds[j].startsWith(goc.trim())) kiem(false, nhan + " ten trang bi bi cat: '" + s + "' (" + ds[j] + ")");
                }
            }
            if (htbb.pc.ChuHD.bat) {
                String sv = thayKyHieuHD(s).replace('\u2026', '.');   // ChuHD.thayKyHieu: → − ≥ ... vẽ bằng chữ thay (như lúc chạy)
                int u = phongKiem.canDisplayUpTo(sv);
                kiem(u < 0, nhan + " phong khong co ky tu '" + (u < 0 ? "" : sv.substring(u, u + 1)) + "' trong \"" + s + "\"");
            }
            // khung TRÊN CÙNG (vẽ gần nhất trước chữ) chứa điểm đầu dòng; bỏ rãnh / dải thanh / thanh cuộn (chữ số đè
            // thanh HP / PP là chủ ý)
            int px = cx[i] + 1, py = cy[i] + ch[i] / 2;
            int tot = -1;
            for (int k = 0; k < nk; k++) {
                if (kt[k] > ct[i]) continue;
                int m = game.pokemon.GhiChu.KM[k];
                if (m == game.pokemon.KhungNroBang.RANH || m == game.pokemon.KhungNroBang.CUON || m == game.pokemon.KhungNroBang.CUON_NUM
                        || m >= game.pokemon.KhungNroBang.DAY_HP && m <= game.pokemon.KhungNroBang.DAY_LA) continue;
                if (px < kx[k] || py < ky[k] || px >= kx[k] + kw[k] || py >= ky[k] + kh[k]) continue;
                if (tot < 0 || kt[k] > kt[tot]) {
                    tot = k;
                }
            }
            khungCua[i] = tot;
            if (tot < 0) continue;
            int dung = 1;
            boolean trong = cx[i] >= kx[tot] - dung && cy[i] >= ky[tot] - dung && cx[i] + cw[i] <= kx[tot] + kw[tot] + dung
                    && cy[i] + ch[i] <= ky[tot] + kh[tot] + 2;
            kiem(trong, nhan + " chu tran khung: \"" + s + "\" " + cx[i] + "," + cy[i] + " " + cw[i] + "x" + ch[i] + " ngoai khung "
                    + kx[tot] + "," + ky[tot] + " " + kw[tot] + "x" + kh[tot]);
        }
        for (int i = 0; i < n; i++) {
            if (khungCua[i] < 0) continue;
            for (int j = i + 1; j < n; j++) {
                if (khungCua[j] != khungCua[i]) continue;
                int ox = Math.min(cx[i] + cw[i], cx[j] + cw[j]) - Math.max(cx[i], cx[j]);
                int oy = Math.min(cy[i] + ch[i], cy[j] + ch[j]) - Math.max(cy[i], cy[j]);
                // hộp dòng có khoảng đệm trên / dưới: chồng > 1/3 chiều cao dòng thấp hơn mới là đè chữ
                if (ox > 2 && oy > Math.min(ch[i], ch[j]) / 3) {
                    kiem(false, nhan + " chu de chu: \"" + game.pokemon.GhiChu.CS[i] + "\" x \"" + game.pokemon.GhiChu.CS[j] + "\"");
                }
            }
        }
        // 0.9.7: chữ không đè BIỂU TƯỢNG (tiền tệ, icon pixel) - phần lõi dòng chữ (bỏ đệm trên / dưới) giao hộp ảnh
        int nb = game.pokemon.GhiChu.soBieu;
        int[] bx = game.pokemon.GhiChu.BX, by = game.pokemon.GhiChu.BY, bw = game.pokemon.GhiChu.BW, bh = game.pokemon.GhiChu.BH;
        int[] bt = game.pokemon.GhiChu.BT;
        for (int b = 0; b < nb; b++) {
            // khung trên cùng chứa tâm biểu tượng (vẽ trước nó) - chữ ở khung khác (vd hàng dưới thẻ phủ) không tính
            int pxb = bx[b] + bw[b] / 2, pyb = by[b] + bh[b] / 2, kb = -1;
            for (int k = 0; k < nk; k++) {
                if (kt[k] > bt[b]) continue;
                int m = game.pokemon.GhiChu.KM[k];
                if (m == game.pokemon.KhungNroBang.RANH || m == game.pokemon.KhungNroBang.CUON || m == game.pokemon.KhungNroBang.CUON_NUM
                        || m >= game.pokemon.KhungNroBang.DAY_HP && m <= game.pokemon.KhungNroBang.DAY_LA) continue;
                if (pxb < kx[k] || pyb < ky[k] || pxb >= kx[k] + kw[k] || pyb >= ky[k] + kh[k]) continue;
                if (kb < 0 || kt[k] > kt[kb]) kb = k;
            }
            for (int i = 0; i < n; i++) {
                if (khungCua[i] != kb) continue;
                int t0 = cy[i] + ch[i] / 5, t1 = cy[i] + ch[i] * 4 / 5;
                int ox = Math.min(cx[i] + cw[i], bx[b] + bw[b]) - Math.max(cx[i], bx[b]);
                int oy = Math.min(t1, by[b] + bh[b]) - Math.max(t0, by[b]);
                if (ox > 1 && oy > 1) {
                    kiem(false, nhan + " chu de bieu tuong: \"" + game.pokemon.GhiChu.CS[i] + "\" x " + game.pokemon.GhiChu.BS[b] + " @"
                            + bx[b] + "," + by[b] + " " + bw[b] + "x" + bh[b]);
                }
            }
        }
    }

    /** 0.9.7: phần HUD / giao diện (khung pixel + biểu tượng đã ghi) nằm TRỌN trong màn (logic). */
    /** 0.9.15: sổ nhiệm vụ vẽ mục tiêu (quái + bản đồ) và NPC trả ngay trên hàng / thẻ, không cắt. */
    static void kiemMucNhiemVu(String nhan) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
            if (game.pokemon.GhiChu.CS[i] != null) b.append(game.pokemon.GhiChu.CS[i]).append('\n');
        }
        String t = b.toString();
        kiem(t.contains("Rattata ở Pallet") || t.contains("Rattata") && t.contains("Pallet"), nhan + " so nhiem vu ve muc tieu (Rattata o Pallet)");
        kiem(t.contains("Xatu (Pewter)") || t.contains("Xatu") && t.contains("(Pewter)"), nhan + " so nhiem vu ve NPC tra (Xatu (Pewter))");
    }

    static void kiemTrongMan(String nhan) {
        int W = NenPc.rong, H = NenPc.cao;
        for (int b = 0; b < game.pokemon.GhiChu.soBieu; b++) {
            int x = game.pokemon.GhiChu.BX[b], y = game.pokemon.GhiChu.BY[b], w = game.pokemon.GhiChu.BW[b], h = game.pokemon.GhiChu.BH[b];
            kiem(x >= -1 && y >= -1 && x + w <= W + 1 && y + h <= H + 1, nhan + " bieu tuong ngoai man: " + game.pokemon.GhiChu.BS[b] + " @" + x + "," + y + " " + w + "x" + h);
        }
        for (int k = 0; k < game.pokemon.GhiChu.soKhung; k++) {
            // phần thấy được (hàng danh sách cuộn bị cắt theo khung danh sách không tính)
            int x = game.pokemon.GhiChu.KVX[k], y = game.pokemon.GhiChu.KVY[k], w = game.pokemon.GhiChu.KVW[k], h = game.pokemon.GhiChu.KVH[k];
            if (w <= 0 || h <= 0) continue;
            if (w >= W - 2 && h >= H - 2) continue;   // nền phủ màn
            kiem(x >= -1 && y >= -1 && x + w <= W + 1 && y + h <= H + 1, nhan + " khung ngoai man: manh " + game.pokemon.GhiChu.KM[k] + " @" + x + "," + y + " " + w + "x" + h);
        }
    }

    /** 0.9.7: thế giới PHỦ KÍN màn (không lề màu nền thử magenta / đen thuần ở mép phải / dưới). */
    static void kiemPhuKin(BufferedImage anh, String nhan) {
        int W = anh.getWidth(), H = anh.getHeight(), n = 0, ho = 0;
        for (int y = 1; y < H; y += 7) {
            for (int x = 1; x < W; x += 7) {
                int c = anh.getRGB(x, y) & 0xFFFFFF;
                n++;
                if (c == 0xFF00FF) ho++;
            }
        }
        kiem(ho == 0, nhan + " the gioi phu kin man (" + ho + "/" + n + " diem ho)");
        // mép phải / dưới: cột / hàng cuối không đen thuần cả dải (lề đen của cover hỏng)
        int denP = 0, denD = 0;
        for (int y = 0; y < H; y += 5) if ((anh.getRGB(W - 2, y) & 0xFFFFFF) == 0) denP++;
        for (int x = 0; x < W; x += 5) if ((anh.getRGB(x, H - 2) & 0xFFFFFF) == 0) denD++;
        kiem(denP * 5 < H / 2 && denD * 5 < W / 2, nhan + " khong lề den mep phai / duoi (" + denP + ", " + denD + ")");
    }

    /** 0.9.7: nhãn thế giới (tên người / NPC / quái, biển "vào") đã đặt không chồng nhau và trọn trong màn. */
    static void kiemNhanTheGioi(String nhan) {
        int n = game.pokemon.NhanTheGioi.soDaVe;
        int[] x = game.pokemon.NhanTheGioi.DX, y = game.pokemon.NhanTheGioi.DY, w = game.pokemon.NhanTheGioi.DW, h = game.pokemon.NhanTheGioi.DH_;
        for (int i = 0; i < n; i++) {
            kiem(x[i] >= 0 && y[i] >= 0 && x[i] + w[i] <= NenPc.rong && y[i] + h[i] <= NenPc.cao, nhan + " nhan the gioi ngoai man: \"" + game.pokemon.NhanTheGioi.DS[i] + "\"");
            for (int j = i + 1; j < n; j++) {
                boolean giao = x[i] < x[j] + w[j] && x[j] < x[i] + w[i] && y[i] < y[j] + h[j] && y[j] < y[i] + h[i];
                kiem(!giao, nhan + " nhan the gioi chong: \"" + game.pokemon.NhanTheGioi.DS[i] + "\" x \"" + game.pokemon.NhanTheGioi.DS[j] + "\"");
            }
        }
    }

    /** 0.9.7: cụm chiêu HUD PC: khe giữa các ô vuông kề nhau trên cung BẰNG NHAU (sai <= 1 px). */
    static void kiemKheChieu(String nhan) {
        int h = 15;   // nửa cạnh ô vuông chiêu (4/5 r + viền)
        int[] khe = new int[3];
        for (int i = 0; i < 3; i++) {
            int dx = Math.abs(game.render.screen.HudRong.xChieuKiem(i + 1) - game.render.screen.HudRong.xChieuKiem(i));
            int dy = Math.abs(game.render.screen.HudRong.yChieuKiem(i + 1) - game.render.screen.HudRong.yChieuKiem(i));
            khe[i] = Math.max(dx, dy) - 2 * h;
        }
        int mn = Math.min(khe[0], Math.min(khe[1], khe[2])), mx = Math.max(khe[0], Math.max(khe[1], khe[2]));
        kiem(mx - mn <= 1 && mn >= 4, nhan + " khe o chieu deu (" + khe[0] + ", " + khe[1] + ", " + khe[2] + ")");
    }

    /** Chuỗi vùng chạm (tiền tố) sau khi mở màn ten. */
    static String[] chamSau(String ten) {
        if ("the_phan_thuong".equals(ten)) return new String[]{"phan-thuong-"};
        if ("xem_trang_bi_the".equals(ten)) return new String[]{"xem-hang-0"};
        if ("shop_su_kien_the".equals(ten) || "tui_do_lay_ra_the".equals(ten) || "cua_hang_nguyen_lieu_the".equals(ten)) {
            return new String[]{"hang-0"};
        }
        if ("kho_do_the".equals(ten)) return new String[]{"kho-kho-0"};
        // 0.9.16: lối vào thật bằng chạm (ô Trang bị › Thú cưỡi, ô Đi theo, tab con, nút Thả trùng)
        if ("nv_thu_dang_dung_tb_cuoi".equals(ten)) return new String[]{"o-Thú cưỡi"};
        if ("nv_thu_dang_dung_tb_di_theo".equals(ten)) return new String[]{"o-Thú cưỡi", "o-di-theo"};
        if ("nv_thu_dang_dung_tab_di_theo".equals(ten)) return new String[]{"thu-loai-1"};
        if ("nv_thu_dang_dung_tha_trung_di_theo".equals(ten)) return new String[]{"thu-loai-1", "thu-tha-trung"};
        if ("nv_thu_dang_dung_tha_trung_cuoi".equals(ten)) return new String[]{"thu-loai-1", "thu-loai-0", "thu-tha-trung"};
        if ("luyen_do_dat".equals(ten)) {
            return new String[]{"luyen-tui-0", "tvp-mua", "luyen-nl-0", "luyen-ngoc-0", "tvp-mua", "luyen-ngoc-0", "tvp-mua"};
        }
        if ("hop_thanh".equals(ten)) {
            return new String[]{"luyen-ngoc-0", "tvp-mua", "luyen-ngoc-0", "tvp-mua", "luyen-ngoc-0", "tvp-mua"};
        }
        return null;
    }

    /** Nguyên liệu luyện / hợp giả lập: luyện kim dược (id 8) + ngọc (id 1, nhóm ép 1). */
    static void giaLapNguyenLieu() {
        String[] ten = {"Ngọc bảo hộ", "Luyện kim dược"};
        int[] id = {1, 8};
        for (int i = 0; i < id.length; i++) {
            if (game.render.Res.getGemByID((short) id[i]) == null) {
                game.model.GemTemp t = new game.model.GemTemp();
                t.id = (short) id[i];
                t.name = ten[i];
                t.decript = i == 0 ? "Ngọc khảm / luyện, hợp 3 viên thành viên cấp cao hơn." : "Đặt trước khi luyện đồ.";
                t.type = 0;
                t.typeEp = (byte) (i == 0 ? 1 : 0);
                t.price = 300;
                t.isSell = true;
                game.render.Res.gemTemplate.addElement(t);
            }
        }
        game.model.MainChar.gemItem.removeAllElements();
        game.model.GemTemplate lkd = new game.model.GemTemplate(8);
        lkd.number = 1;
        lkd.rID = 801;
        game.model.GemTemplate ngoc = new game.model.GemTemplate(1);
        ngoc.number = 5;
        ngoc.rID = 101;
        game.model.GemTemplate da = new game.model.GemTemplate(3);
        da.number = 7;
        game.model.MainChar.gemItem.addElement(lkd);
        game.model.MainChar.gemItem.addElement(ngoc);
        game.model.MainChar.gemItem.addElement(da);
    }

    /** Người chơi khác giả lập (xem trang bị): mặc như nhân vật chính, thú mang 3 món. */
    static game.model.Char nguoiKhac() {
        game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
        game.model.Char c = new game.model.Char();
        c.ID = 7;
        c.name = "Misty";
        c.level = 22;
        c.pokeDex = mc.pokeDex;
        c.pokeShiny = mc.pokeShiny;
        c.ngoaiHinh = mc.ngoaiHinh;
        c.wearingItems = mc.wearingItems;
        c.wearingAnimal = new java.util.Vector();
        for (int i = 0; i < 3 && i < game.model.Char.inventory.size(); i++) c.wearingAnimal.addElement(game.model.Char.inventory.elementAt(i));
        return c;
    }

    /** Danh sách -76 giả lập (shop: giá lượng = thuộc tính 125). */
    static void giaLapShopNew(String dongTu, String tenDs, boolean coGia) {
        shopDo();
        java.util.Vector v = new java.util.Vector();
        for (int i = 0; i < game.render.Canvas.gameScr.shop.size(); i++) {
            game.model.ItemInInventory g0 = (game.model.ItemInInventory) game.render.Canvas.gameScr.shop.elementAt(i);
            game.model.ItemInInventory it = new game.model.ItemInInventory();
            it.itemID = (short) (900 + i);
            it.idTemplate = g0.idTemplate;
            it.charClazz = g0.charClazz;
            it.colorName = (byte) (i % 4);
            it.isEnoughData = true;
            it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 0, (short) (30 + i)));
            if (coGia) it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 125, (short) (80 + 20 * i)));
            v.addElement(it);
        }
        game.model.ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.put("125", new game.model.NameAttributeItem((short) 125, "Giá lượng", (byte) 0, (byte) 0));
        game.model.ShopNew.text = dongTu;
        game.model.ShopNew.nameshop = tenDs;
        game.model.ShopNew.gI().typeshop = 8;
        game.model.ShopNew.gI().ItemShopNew = v;
    }

    /** Chạm (qua VungCham.xuLy như Canvas) tâm vùng đầu tiên có tiền tố t. */
    static void chamVung(String t) {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            String n = game.pokemon.VungCham.ten(i);
            if (n != null && n.startsWith(t)) {
                int[] v = game.pokemon.VungCham.vungVe(i);
                int cx = v[0] + v[2] / 2, cy = v[1] + v[3] / 2;
                game.render.Canvas.px = cx;
                game.render.Canvas.py = cy;
                game.render.Canvas.pxLast = cx;
                game.render.Canvas.pyLast = cy;
                game.render.Canvas.isPointerClick = true;
                game.pokemon.VungCham.xuLy();
                game.render.Canvas.isPointerClick = false;
                return;
            }
        }
    }

    /** Tên vùng chạm đã đăng ký có tiền tố t (lớp nào cũng được). */
    static boolean coVung(String t) {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            String n = game.pokemon.VungCham.ten(i);
            if (n != null && n.startsWith(t)) return true;
        }
        return false;
    }

    /**
     * 0.9.7 hành trang kiểu NRO: tab Trang bị / tab Bán = LƯỚI ô (vùng tui-0..), hàng ví có vùng chạm xu + lượng, "Tất cả"
     * có ô khoá cuối lưới; thẻ vật phẩm (nv_chi_tiet) trong vùng an toàn, có nút Mặc / Vứt / Đóng; tab Bán có nút Bán.
     */
    static void kiemTuiNro(String ten, String nhan) {
        if ("nv_trang_bi".equals(ten) || "nv_hanh_trang".equals(ten) || "cua_hang_ban".equals(ten) || ten.startsWith("tui_loc")) {
            kiem(coVung("tui-0"), nhan + " luoi hanh trang co o (tui-0)");
            kiem(coVung("tien-xu") && coVung("tien-luong"), nhan + " hang vi co xu + luong (cham duoc)");
            if ("nv_trang_bi".equals(ten) || "nv_hanh_trang".equals(ten)) {
                kiem(game.pokemon.LuoiTui.soKhoaVe > 0, nhan + " luoi Tat ca co o khoa cuoi (mo rong) - " + game.pokemon.LuoiTui.soKhoaVe);
            }
        }
        if ("cua_hang_thuoc_chi_tiet".equals(ten) || "cua_hang_do_chi_tiet".equals(ten)) {
            kiem(game.pokemon.TheVatPham.dangMo() && coVung("tvp-mua") && coVung("dong"), nhan + " cua hang: cham mon = the vat pham co nut Mua + X");
            if ("cua_hang_thuoc_chi_tiet".equals(ten)) {
                kiem(coVung("sl-cong") && coVung("sl-max") && game.pokemon.TheVatPham.soMua() == 12, nhan + " the Mua thuoc co chon so (12)");
            }
        }
        if ("hop_phan_thuong_xem".equals(ten) || "nhiem_vu_chi_tiet".equals(ten)) {
            kiem(coVung("phan-thuong-"), nhan + " phan thuong = hang o vat pham (cham duoc)");
        }
        if ("the_phan_thuong".equals(ten)) {
            kiem(game.pokemon.TheVatPham.dangMo() && coVung("dong") && !coVung("tvp-"), nhan + " cham o phan thuong = the vat pham chi xem (chi X)");
        }
        if ("tui_sao_goi".equals(ten)) {
            kiem(game.pokemon.TheVatPham.dangMo() && coChu("Vật phẩm gọi thú cưỡi"), nhan + " sao goi: loai 'Vat pham goi thu cuoi'");
            kiem(coChu("nhận thú cưỡi Tauros vĩnh viễn"), nhan + " sao goi: mo ta that");
            kiem(!coChu("Bình thuốc") && !coChu("Hồi HP"), nhan + " sao goi: khong con 'Binh thuoc / Hoi HP'");
            kiem(coVung("tvp-dung") && coChu("Sử dụng"), nhan + " sao goi: nut Su dung");
        }
        if ("tui_nguyen_lieu_su_kien".equals(ten)) {
            kiem(game.pokemon.TheVatPham.dangMo() && coChu("Nguyên liệu sự kiện") && !coChu("Hồi HP"), nhan + " nguyen lieu: loai dung, khong chu HP");
            kiem(coChu("Không dùng được") && coChu("Đổi ở NPC sự kiện"), nhan + " nguyen lieu: nut xam + ly do");
        }
        kiemCuaSoCu(ten, nhan);
        if ("nv_chi_tiet".equals(ten)) {
            int[] r = game.pokemon.TheVatPham.VUA_VE;
            int L = game.pokemon.Bang.trai(), T = game.pokemon.Bang.tren(), R = game.pokemon.Bang.phai(), B = game.pokemon.Bang.duoi();
            kiem(game.pokemon.TheVatPham.dangMo(), nhan + " the vat pham mo");
            kiem(r[2] > 0 && r[0] >= L && r[1] >= T && r[0] + r[2] <= R && r[1] + r[3] <= B, nhan + " the vat pham trong vung an toan");
            kiem(coVung("tvp-mac") && (coVung("tvp-vut") || coVung("tvp-huy")) && coVung("dong"), nhan + " the vat pham co nut Mac / Vut|Huy / X");
        }
    }

    /** 0.9.7: đường cũ (WindowInfoScr / ShopNew) nay là màn NRO có vùng chạm ở máy cảm ứng. */
    static void kiemCuaSoCu(String ten, String nhan) {
        Object man = game.render.Canvas.currentScreen;
        boolean cu = man == game.render.screen.WindowInfoScr.gI() || man == game.model.ShopNew.gI();
        boolean the = game.pokemon.TheVatPham.dangMo();
        game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
        if ("xem_trang_bi".equals(ten)) {
            kiem(!cu && game.pokemon.ManXemTrangBi.dangXem() && coVung("xem-o-") && coVung("xem-hang-0"), nhan + " xem trang bi nguoi khac = man NRO (o + danh sach)");
        } else if ("xem_trang_bi_the".equals(ten)) {
            kiem(the && coVung("dong") && !coVung("tvp-"), nhan + " xem trang bi: cham mon = the chi xem");
        } else if ("xem_do_thu".equals(ten)) {
            kiem(!cu && game.pokemon.ManXemTrangBi.dangXem() && coVung("xem-thu-0"), nhan + " xem do thu = man NRO (luoi o)");
        } else if ("shop_su_kien".equals(ten)) {
            kiem(!cu && man == game.pokemon.ManCuaHang.gI() && coVung("hang-0"), nhan + " danh sach -76 (shop su kien) = cua hang NRO");
        } else if ("shop_su_kien_the".equals(ten)) {
            kiem(the && coVung("tvp-mua") && coVung("sl-cong") && coVung("dong"), nhan + " shop su kien: the Mua co chon so + gia luong");
        } else if ("tui_do_lay_ra_the".equals(ten)) {
            kiem(the && coVung("tvp-mua") && !coVung("sl-cong"), nhan + " tui do: the co nut Lay ra (khong gia)");
        } else if ("cua_hang_nguyen_lieu".equals(ten)) {
            kiem(!cu && man == game.pokemon.ManCuaHang.gI() && coVung("hang-0"), nhan + " NPC ban nguyen lieu = cua hang NRO");
        } else if ("cua_hang_nguyen_lieu_the".equals(ten)) {
            kiem(the && coVung("tvp-mua") && coVung("sl-cong"), nhan + " nguyen lieu: the Mua co chon so");
        } else if ("kho_do".equals(ten)) {
            kiem(!cu && coVung("kho-tui-0") && coVung("kho-kho-0"), nhan + " kho do = hai cot NRO");
        } else if ("kho_do_the".equals(ten)) {
            kiem(the && coVung("tvp-mua"), nhan + " kho do: the co nut Lay ra");
        } else if ("luyen_do".equals(ten) || "kham".equals(ten)) {
            kiem(!cu && game.pokemon.ManLuyen.dangMo() && coVung("luyen-do") && coVung("luyen-nl-7") && coVung("luyen-tui-0"), nhan + " ban luyen / kham NRO (o trang bi + 8 o + danh sach)");
        } else if ("luyen_do_dat".equals(ten)) {
            kiem(mc.itemImbue != null && game.model.MainChar.gemitemImbue.size() == 2 && coVung("luyen-lam"), nhan + " luyen: dat trang bi + luyen kim duoc + ngoc qua the (" + game.model.MainChar.gemitemImbue.size() + ")");
        } else if ("hop_thanh".equals(ten)) {
            kiem(!cu && game.pokemon.ManLuyen.dangMo() && game.model.MainChar.gemitemImbue.size() == 3 && coVung("luyen-nl-4") && !coVung("luyen-nl-5") && !coVung("luyen-do"), nhan + " hop thanh: 5 o, dat 3 vien");
        }
    }

    /** Menu ngữ cảnh: thẻ trong vùng an toàn, không chồng phần HUD (thẻ trạng thái, nút, bản đồ nhỏ, cụm chiêu, chat). */
    static void kiemMenuNguCanh(String nhan) {
        int[] r = game.pokemon.MenuNguCanh.VUA_VE;
        int L = game.pokemon.Bang.trai(), T = game.pokemon.Bang.tren(), R = game.pokemon.Bang.phai(), B = game.pokemon.Bang.duoi();
        kiem(r[2] > 0 && r[0] >= L && r[1] >= T && r[0] + r[2] <= R && r[1] + r[3] <= B, nhan + " menu ngu canh trong vung an toan");
        if (!game.render.screen.HudRong.bat() || game.render.Canvas.gameScr == null) {
            return;
        }
        Object[][] v = game.render.screen.HudRong.cacVung(game.render.Canvas.gameScr);
        for (int i = 0; i < v.length; i++) {
            String t = (String) v[i][0];
            if ("ten_chieu".equals(t) || "muc_tieu".equals(t)) continue;
            int[] a = (int[]) v[i][1];
            boolean giao = r[0] < a[0] + a[2] && a[0] < r[0] + r[2] && r[1] < a[1] + a[3] && a[1] < r[1] + r[3];
            kiem(!giao, nhan + " menu ngu canh khong chong HUD " + t);
        }
    }

    // ------------------------------------------------------------------ 0.9.6.1 hệ sinh thái / câu cá (gói giả)

    /** Gói máy chủ giả qua MessageHandler.onMessage thật (cùng đường đọc như mạng). */
    static void goi(int lenh, byte[] duLieu) {
        try {
            java.lang.reflect.Constructor<network.Message> k = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
            k.setAccessible(true);
            game.networklogic.MessageHandler.gI().onMessage(k.newInstance((byte) lenh, duLieu));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /** -30 menu máy chủ (HeSinhThaiMenu.menu -> MessageCreator.createMsgMenu). */
    static void menuMayChu(int npc, int idMenu, String[] ds) {
        try {
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream o = new java.io.DataOutputStream(b);
            o.writeShort(npc);
            o.writeByte(idMenu);
            o.writeByte(ds.length);
            for (int i = 0; i < ds.length; i++) o.writeUTF(ds[i]);
            goi(-30, b.toByteArray());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        game.pokemon.MenuNguCanh.tiepQuan();   // GameScr.update làm mỗi nhịp
    }

    /** 37 thông báo máy chủ (HeSinhThaiGame.bao -> createServerAlertMessage(s, "")). */
    static void thongBaoMayChu(String s) {
        try {
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream o = new java.io.DataOutputStream(b);
            o.writeUTF(s);
            o.writeUTF("");
            goi(37, b.toByteArray());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    static final String[] HST_GOC = {"Thời tiết: Mưa rào (ngày)", "Khảo cổ: đào tại đây", "Nấu ăn, chế thuốc, chế tác", "Túi sinh thái",
        "Nhà trẻ và trứng (2)", "Chợ sinh thái", "Vườn nhà và bạn bè", "Sưu tầm và nghiên cứu", "Giải trí: đố vui, đua, giải đấu",
        "Kỹ năng và thể lực (96)"};
    static final String HST_KY_NANG = "Kỹ năng sống\n- Câu cá: cấp 12 (340/1200)\n- Trồng trọt: cấp 3 (40/300)\n- Khảo cổ: cấp 7 (95/700)\n"
            + "- Nấu ăn: cấp 5 (10/500)\n- Chế thuốc: cấp 1 (0/100)\nThể lực sinh hoạt: 96 (+100/ngày, tích tối đa 200)\n"
            + "Câu cá 2, đào 4, nấu/chế 3 thể lực\nHiệu ứng: mồi thơm 2 lần, súp berry 0 lần\nĐã bán NPC hôm nay: 1250/20000 xu";
    static final String HST_NGHIEN_CUU = "Nghiên cứu: 14 loài đã có ghi chép, 3 loài hoàn tất.\nGần đây:\n"
            + "- Magikarp: (x) Gặp, (x) Câu được, ( ) Chụp ảnh lúc mưa, ( ) Chụp ảnh ban đêm\n"
            + "- Goldeen: (x) Gặp, ( ) Câu được, ( ) Chụp ảnh hiếm\n- Poliwag: (x) Gặp, (x) Câu được\n"
            + "- Tentacool: (x) Gặp, ( ) Câu được, ( ) Chụp ảnh ban đêm\n- Horsea: ( ) Gặp, ( ) Câu được\n"
            + "- Shellder: (x) Gặp, (x) Câu được, (x) Chụp ảnh hiếm\n- Staryu: (x) Gặp, ( ) Câu được\n"
            + "- Krabby: (x) Gặp, (x) Câu được\n- Chinchou: (x) Gặp, ( ) Câu được, ( ) Chụp ảnh ban đêm\n"
            + "- Remoraid: (x) Gặp, ( ) Câu được";
    static final String HST_THE_GIOI = "Hồ - Hồ Phẫn Nộ\nThời tiết: Mưa rào, ban ngày, mùa Thu\n"
            + "Pokémon hệ Nước/Điện xuất hiện nhiều hơn, chiêu Nước +50%, chiêu Lửa -50%\nBùng phát mỗi ngày lúc 12h, 20h\n"
            + "Hôm nay:\n- Đồng cỏ: Nắng\n- Rừng: Sương mù\n- Hồ: Mưa rào\n- Biển: Gió mạnh\n- Núi: Nắng gắt\n- Tuyết: Bão tuyết\n- Sa mạc: Bão cát";

    static void moSinhThai(String ten) {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        gs.switchToMe();
        gs.focusedActor = null;   // menu -528 mở từ Menu / nút: không neo NPC
        int N = game.render.screen.TuiSinhThaiScr.NPC_ID;
        if ("hst_menu".equals(ten) || "hst_menu_trang2".equals(ten)) {
            menuMayChu(N, 0, HST_GOC);
            if (ten.endsWith("2")) dat(game.pokemon.MenuNguCanh.gI(), "trang", Integer.valueOf(1));
        } else if ("hst_tu_man".equals(ten)) {
            // mở từ Menu lưới (ManMenu -> Hệ sinh thái): gói tới khi màn Menu còn đang hiện
            game.pokemon.ManMenu.mo();
            menuMayChu(N, 0, HST_GOC);
        } else if ("hst_cong_thuc".equals(ten)) {
            String[] t = {"Bánh Poké (Nấu ăn 1) - làm được", "Mồi thơm (x3) (Nấu ăn 5) - làm được", "Súp berry (Nấu ăn 10) - chưa đủ cấp",
                "Cà ri cá (Nấu ăn 20) - chưa đủ cấp", "Thuốc xịt (Chế thuốc 1)", "Nước tinh khiết (Chế thuốc 1)",
                "Phục hồi Hoá thạch Vỏ ốc (Khảo cổ 10) - chưa đủ cấp", "Lẩu hải sản (Cà ri cá) (Nấu ăn 25) - chưa đủ cấp",
                "Mồi thơm biển (Nấu ăn 15) - chưa đủ cấp", "Tượng rồng Dratini (Khảo cổ 45) - chưa đủ cấp"};
            menuMayChu(N, 1, t);
        } else if ("hst_suu_tam".equals(ten)) {
            menuMayChu(N, 22, new String[]{"Album ảnh (7)", "Nghiên cứu Pokédex (14 loài)", "Nhận thưởng vùng sinh thái", "Danh hiệu (1)"});
        } else if ("hst_vung".equals(ten)) {
            menuMayChu(N, 26, new String[]{"Đồng cỏ: 2/12", "Rừng: 0/15", "Hồ: 3/9 (đã nhận)", "Biển: 1/14", "Núi: 0/10", "Tuyết: 0/8",
                "Sa mạc: 0/9", "Tàu: 0/6"});
        } else if ("hst_cho".equals(ten)) {
            menuMayChu(N, 10, new String[]{"Mua: Cá", "Mua: Berry", "Mua: Khoáng", "Mua: Hoá thạch", "Mua: Món ăn", "Mua: Thuốc", "Mua: Trứng",
                "Mua: Trang trí", "Tin bán của tôi (huỷ)", "Đơn đặt hàng đang mở (giao hàng)", "Tạo đơn đặt hàng", "Đơn của tôi (huỷ)",
                "Hộp thư: nhận tiền/hàng"});
        } else if ("hst_cho_loai".equals(ten)) {
            menuMayChu(N, 11, new String[]{"Vảy rồng Dratini (bậc 3) x2 - 12.500 xu - Pikachu88", "Vảy Magikarp (bậc 1) x40 - 900 xu - Bulba77",
                "Mai Lapras (bậc 4) x1 - 48.000 xu - NguPhuBacThay2024", "Gai Qwilfish (bậc 2) x5 - 1.500 xu - Ash"});
        } else if ("hst_tui".equals(ten)) {
            menuMayChu(N, 2, new String[]{"Vảy Magikarp (bậc 2) x37", "Vảy Goldeen (bậc 2) x5", "Rác trôi (bậc 1) x3", "Vỏ sò (bậc 2) x12",
                "Ngọc trai nhỏ (bậc 2) x1", "Berry Oran (bậc 1) x8", "Mồi thơm (bậc 2) x3", "Vảy rồng Dratini (bậc 3) x1",
                "Xúc tu Tentacool (bậc 2) x4", "Càng Krabby (bậc 2) x2", "Rong biển (bậc 1) x9", "Đèn Chinchou (bậc 3) x1"});
        } else if ("hst_tui_day".equals(ten)) {
            // túi đầy (máy chủ gửi tối đa 40 dòng): tấm cao tối đa, lưới cuộn bên trong
            String[] t = new String[40];
            String[] loai = {"Vảy Magikarp", "Vảy Goldeen", "Vỏ sò", "Berry Oran", "Mồi thơm", "Càng Krabby", "Rong biển", "Đèn Chinchou"};
            for (int i = 0; i < t.length; i++) t[i] = loai[i % loai.length] + " (bậc " + (1 + i % 4) + ") x" + (i * 7 + 1);
            menuMayChu(N, 2, t);
            dat(game.render.screen.TuiSinhThaiScr.gI(), "chon", Integer.valueOf(0));
        } else if ("hst_tui_hanh_dong".equals(ten)) {
            menuMayChu(N, 3, new String[]{"Dùng", "Bán cho NPC (12 xu/cái)", "Rao bán ở chợ", "Gửi tặng bạn bè"});
        } else if ("hst_ky_nang".equals(ten)) {
            thongBaoMayChu(HST_KY_NANG);
        } else if ("hop_hst_ky_nang".equals(ten) || "hop_hst_nghien_cuu".equals(ten) || "hop_hst_the_gioi".equals(ten)) {
            // biểu ngữ -> "Xem": thông báo đủ
            String s = ten.endsWith("ky_nang") ? HST_KY_NANG : ten.endsWith("nghien_cuu") ? HST_NGHIEN_CUU : HST_THE_GIOI;
            game.pokemon.HopThongBao.bao("Thông báo", s);
        } else if ("hop_cau_ket_qua".equals(ten)) {
            // kết quả câu cá sau hoạt cảnh kéo cần (SinhThai.ketThucCau -> Canvas.startOKDlg)
            game.render.Canvas.startOKDlg("Câu được: 1 Vảy rồng Dratini (bậc 3). Gặp Magikarp! Thể lực còn 96.");
        } else if ("cau_can".equals(ten)) {
            game.pokemon.HanhDong.bao("Cá cắn câu! Bấm nút câu để kéo cần ngay trước khi cá thoát mất.");
        }
    }

    /**
     * 0.9.6.1 menu máy chủ (hệ sinh thái -528...): đã mở (thẻ lệnh / màn túi), thẻ trong vùng an toàn, rộng <= 80 %
     * vùng an toàn, không chồng HUD (kiemMenuNguCanh).
     */
    static void kiemMenuMayChu(String nhan) {
        Object d = game.render.Canvas.currentDialog;
        if (d instanceof game.pokemon.MenuNguCanh) {
            int[] r = game.pokemon.MenuNguCanh.VUA_VE;
            int sw = game.pokemon.Bang.phai() - game.pokemon.Bang.trai();
            kiem(r[2] <= sw * 80 / 100 + 1, nhan + " the lenh rong " + r[2] + " <= 80% vung an toan " + sw);
            kiemMenuNguCanh(nhan);
        } else {
            kiem(game.render.Canvas.currentScreen instanceof game.render.screen.TuiSinhThaiScr || game.render.Canvas.menu.showMenu,
                    nhan + " menu may chu da mo (" + d + ", " + game.render.Canvas.currentScreen + ")");
            kiem(!game.render.Canvas.menu.showMenu, nhan + " khong con menu chu J2ME (Canvas.menu) tren may cham");
        }
    }

    static void datTinh(Class<?> c, String ten, Object gt) {
        try {
            java.lang.reflect.Field f = c.getDeclaredField(ten);
            f.setAccessible(true);
            f.set(null, gt);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

        /**
     * Quản lý thú: bật = 2 bản Hoothoot trùng + phần nối thêm gói 16 loại 2 (ô 6/20, Tauros khoá, xu thả) như máy chủ
     * real.QuanLyThuGame gửi; tắt = danh sách gốc 4 thú, máy chủ cũ (không nối thêm).
     */
    static void datQuanLyThu(boolean bat) {
        datQuanLyThu(bat, 0);
    }

    /**
     * dangDung (khối đang dùng 0.9.13 nối sau n dòng): 0 = không gửi (máy chủ 912), 1 = chỉ Hoothoot đang đi theo,
     * 2 = đang cưỡi Rapidash cấp 5 + Hoothoot đang đi theo (nhân vật chủ dự án: cưỡi thú + có pet theo).
     */
    static void datQuanLyThu(boolean bat, int dangDung) {
        java.util.Vector a = game.model.Char.animal;
        while (a.size() > 4) a.removeElementAt(a.size() - 1);
        game.pokemon.QuanLyThu.co = false;
        game.pokemon.QuanLyThu.coDung = false;
        game.pokemon.QuanLyThu.cuoi = null;
        game.pokemon.QuanLyThu.diTheo = null;
        game.pokemon.QuanLyThu.thuCu = null;
        game.pokemon.QuanLyThu.dung = new java.util.Vector();
        if (!bat) return;
        for (int i = 0; i < 2; i++) {
            game.model.AnimalInfo an = new game.model.AnimalInfo();
            an.id = (short) (10 + i);
            an.idImg = 4;
            an.idPaint = 4;
            an.name = "Hoothoot cấp 1\nID: " + (20 + i);
            an.typeAnimal = 1;
            a.addElement(an);
        }
        try {
            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream o = new java.io.DataOutputStream(bo);
            o.writeByte(1);
            o.writeShort(a.size() + (dangDung == 2 ? 2 : dangDung == 1 ? 1 : 0));
            o.writeShort(20);
            o.writeShort(60);
            o.writeShort(0);
            o.writeShort(271);
            o.writeInt(10);
            o.writeInt(2000);
            o.writeByte(a.size());
            int[][] dong = {{3, 100002, 3, 0}, {0, 100013, 1, 26}, {0, 163, 1001, 22}, {0, 147, 1001, 0}, {0, 163, 1001, 22}, {0, 163, 1001, 22}};
            for (int i = 0; i < a.size(); i++) {
                o.writeByte(dong[i][0]);
                o.writeInt(dong[i][1]);
                o.writeShort(dong[i][2]);
                o.writeInt(dong[i][3]);
                o.writeUTF(dong[i][0] != 0 ? "Đã khoá" : "");
            }
            if (dangDung > 0) {
                // như real.QuanLyThuGame.ghiDangDung
                o.writeByte(1);
                o.writeByte(dangDung == 2 ? 2 : 1);
                if (dangDung == 2) {
                    o.writeByte(0);
                    o.writeShort(30);
                    o.writeByte(0);
                    o.writeByte(0);
                    o.writeByte(5);
                    o.writeUTF("Rapidash");
                    o.writeUTF("Tốc độ +2");
                    o.writeByte(1);
                    o.writeByte(2);
                    o.writeInt(100000);
                    o.writeShort(5);
                    o.writeInt(0);
                    o.writeUTF("Đang cưỡi");
                }
                o.writeByte(1);
                o.writeShort(31);
                o.writeByte(4 + 5);
                o.writeByte(4);
                o.writeUTF("Hoothoot cấp 3\nID: 9");
                o.writeByte(0);
                o.writeUTF("");
                o.writeInt(600);
                o.writeByte(1);
                o.writeByte(2);
                o.writeInt(163);
                o.writeShort(1003);
                o.writeInt(0);
                o.writeUTF("Đang đi theo");
                o.writeByte(0);
            }
            game.pokemon.QuanLyThu.doc(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bo.toByteArray())), a);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Sân khấu Trang bị › Thú cưỡi không trống (luôn có tên thú / gợi ý), tab Thú có "Ô thú: n/max" khi có quản lý. */
    static final java.util.Vector THU_BO = new java.util.Vector();

    static int loaiThuDangXem() {
        try {
            java.lang.reflect.Field f = game.pokemon.ManNhanVat.class.getDeclaredField("loaiThu");
            f.setAccessible(true);
            return f.getInt(game.pokemon.ManNhanVat.gI());
        } catch (Throwable e) {
            return -1;
        }
    }

    /** Tên vùng chạm hàng một thú (ManNhanVat.veDanhSachThu): "thu-dung-i" (khối đang dùng) / "thu-i" (túi). */
    static String tenHang(game.model.AnimalInfo a) {
        return game.pokemon.QuanLyThu.dangDung(a) ? "thu-dung-" + game.pokemon.QuanLyThu.dung.indexOf(a)
                : "thu-" + game.model.Char.animal.indexOf(a);
    }

    /**
     * 0.9.16 tab con danh sách thú: "Thú cưỡi (n)" / "Đi theo (m)" đúng số, chỉ hàng của loại đang xem, con đang dùng
     * của loại đứng đầu, lối vào đúng tab (ô Thú cưỡi -> Thú cưỡi, ô Đi theo -> Đi theo, tab Thú = lần cuối), tab trống
     * có chữ gợi ý, Thả trùng chỉ trong tab (hộp hỏi nói loại).
     */
    static void kiemTachThu(String ten, String nhan) {
        boolean ds = ten.startsWith("nv_thu") || ten.startsWith("nv_ds_thu") || ten.startsWith("nv_cuoi_ds_thu") || "nv_cuoi_thu".equals(ten);
        if (!ds || game.render.Canvas.currentDialog != null && !ten.startsWith("nv_thu_dang_dung_tha_trung")) return;
        int l = loaiThuDangXem();
        int muon = -1;
        if (ten.endsWith("_tb_cuoi") || ten.endsWith("tha_trung_cuoi") || ten.endsWith("_tab0")) muon = 0;
        if (ten.endsWith("_tb_di_theo") || ten.endsWith("_tab_di_theo") || ten.endsWith("_tab_nho") || ten.endsWith("tha_trung_di_theo")
                || ten.endsWith("_tab1")) muon = 1;
        if (muon >= 0) kiem(l == muon, nhan + " tab con sai: " + l + " (muon " + muon + ")");
        if (ten.startsWith("nv_thu_dang_dung_tha_trung")) {
            // hộp hỏi / thông báo Thả trùng nói đúng loại; loại kia không bị đụng
            if (l == 1) {
                kiem(coChu("Pokémon đi theo trùng") || coChuDoan("Pokémon đi theo"), nhan + " hop Tha trung khong noi 'Pokemon di theo'");
                kiem(game.pokemon.QuanLyThu.banTrung(1).size() == 2, nhan + " ban trung Di theo != 2");
            } else {
                kiem(game.pokemon.QuanLyThu.banTrung(0).size() == 0, nhan + " ban trung Thu cuoi != 0");
            }
            return;
        }
        kiem(coVungDung("thu-loai-0") && coVungDung("thu-loai-1"), nhan + " thieu tab con Thu cuoi / Di theo");
        java.util.Vector cuoi = game.pokemon.QuanLyThu.dsLoai(0), theo = game.pokemon.QuanLyThu.dsLoai(1);
        game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
        int cu = game.pokemon.QuanLyThu.coDung ? (game.pokemon.QuanLyThu.thuCu != null ? 1 : 0) : (game.pokemon.ManNhanVat.dangCuoiThu() ? 1 : 0);
        int nC = cuoi.size() + cu, nT = theo.size();
        kiem(coChu("Thú cưỡi (" + nC + ")") || coChu("Cưỡi (" + nC + ")"), nhan + " nhan tab 'Thu cuoi (" + nC + ")'");
        kiem(coChu("Đi theo (" + nT + ")"), nhan + " nhan tab 'Di theo (" + nT + ")'");
        java.util.Vector dsXem = l == 0 ? cuoi : theo, khac = l == 0 ? theo : cuoi;
        for (int i = 0; i < khac.size(); i++) {
            String h = tenHang((game.model.AnimalInfo) khac.elementAt(i));
            kiem(!coVungDung(h), nhan + " hang loai khac lot vao tab: " + h);
        }
        for (int i = 0; i < dsXem.size(); i++) {
            game.model.AnimalInfo a = (game.model.AnimalInfo) dsXem.elementAt(i);
            kiem(game.pokemon.QuanLyThu.loai(a) == l, nhan + " dsLoai sai loai");
        }
        game.model.AnimalInfo dangDung = game.pokemon.QuanLyThu.coDung ? (l == 0 ? game.pokemon.QuanLyThu.cuoi : game.pokemon.QuanLyThu.diTheo) : null;
        if (dangDung != null) {
            kiem(dsXem.elementAt(0) == dangDung, nhan + " con dang dung khong dung dau tab");
            kiem(coVungDung(tenHang(dangDung)), nhan + " thieu hang con dang dung");
        } else if (dsXem.size() > 0 && (l == 1 || cu == 0)) {
            kiem(coVungDung(tenHang((game.model.AnimalInfo) dsXem.elementAt(0))), nhan + " thieu hang dau tab");
        }
        if (dsXem.size() + (l == 0 ? cu : 0) == 0) {
            kiem(l == 0 ? coChu("Chưa có thú cưỡi") && coChuDoan("Sáo gọi") : coChu("Chưa có Pokémon đi theo") && coChuDoan("ấp trứng"),
                    nhan + " tab trong thieu chu goi y");
        }
        if (game.pokemon.QuanLyThu.co) {
            int nTrung = game.pokemon.QuanLyThu.banTrung(l).size();
            kiem(nTrung > 0 ? coChu("Thả trùng (" + nTrung + ")") || coChu("Trùng " + nTrung) : !coChu("Thả trùng (") && !coChu("Trùng "),
                    nhan + " so Tha trung theo tab sai (" + nTrung + ")");
        }
    }

    /** Có chữ chứa đoạn (đoạn văn ngắt dòng: tìm từng dòng). */
    static boolean coChuDoan(String can) {
        return coChu(can);
    }

    static void kiemXemThu(String ten, String nhan) {
        kiemTachThu(ten, nhan);
        kiemKhoiThu(ten, nhan);
        String can = null;
        if ("nv_ds_thu".equals(ten)) can = "Chưa cưỡi";
        else if ("nv_ds_thu_quan_ly".equals(ten) || "nv_thu_quan_ly".equals(ten)) can = ten.startsWith("nv_thu") ? "Hoothoot" : "Dragonite";
        else if (ten.startsWith("nv_cuoi_ds_thu")) can = "Đang cưỡi";
        if (can != null) {
            boolean co = false;
            for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
                String c = game.pokemon.GhiChu.CS[i];
                co |= c != null && c.indexOf(can) >= 0;
            }
            kiem(co, nhan + " san khau Thu cuoi trong (thieu '" + can + "')");
        }
        if (ten.endsWith("_quan_ly")) {
            boolean o = false, nut = false;
            for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
                String c = game.pokemon.GhiChu.CS[i];
                o |= c != null && c.startsWith("Ô thú");
            }
            for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
                String t = game.pokemon.VungCham.ten(i);
                nut |= "thu-tha-trung".equals(t);
            }
            kiem(o, nhan + " thieu 'O thu: n/max'");
            // 0.9.16: Thả trùng theo tab con - tab không có bản trùng: nút xám (không vùng chạm)
            kiem(nut == (game.pokemon.QuanLyThu.banTrung(loaiThuDangXem()).size() > 0), nhan + " nut Tha trung sai theo tab");
        }
    }

    static boolean coChu(String can) {
        for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
            String c = game.pokemon.GhiChu.CS[i];
            if (c != null && c.indexOf(can) >= 0) return true;
        }
        return false;
    }

    static boolean coVungDung(String ten) {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) return true;
        }
        return false;
    }

    /**
     * 0.9.13: Trang bị › Thú cưỡi KHÔNG có lưới đồ nhân vật (ô đồ, Tiềm năng, Danh hiệu) mà có ô "Thú cưỡi" + "Đi theo";
     * Trang bị thường vẫn đủ 12 ô + Tiềm năng. Khối đang dùng: thú đang cưỡi / pet đang đi theo hiện ở ô, sân khấu và
     * danh sách (nhãn Đang cưỡi / Đang đi theo / Thú cưỡi), số hàng = Ô thú, thao tác đúng con (Xuống thú, không thả).
     */
    static void kiemKhoiThu(String ten, String nhan) {
        boolean thu = ten.startsWith("nv_cuoi_ds_thu") || ten.startsWith("nv_ds_thu")
                || (ten.startsWith("nv_thu_") && ten.indexOf("dang_dung") >= 0 && !ten.endsWith("_tab") && !ten.endsWith("_tab_di_theo")
                && !ten.endsWith("_tab_nho") && ten.indexOf("tha_trung") < 0) || "nv_thu_di_theo_dang_dung".equals(ten);
        boolean thuong = "nv_trang_bi".equals(ten) || "nv_cuoi_trang_bi".equals(ten);
        if (!thu && !thuong) return;
        String[] tenO = {"Đồ cầm tay", "Khăn quàng", "Băng đô", "Đai", "Vòng cổ", "Huy hiệu", "Huy hiệu 2", "Găng đấm", "Lông vũ",
            "Phiến đá hệ", "Ruy băng", "Thú cưỡi"};
        int soO = 0;
        for (int i = 0; i < tenO.length; i++) {
            if (coVungDung("o-" + tenO[i])) soO++;
        }
        if (thu) {
            kiem(soO == 0, nhan + " Thu cuoi: con " + soO + " o do nhan vat");
            kiem(!coVungDung("tiem-nang"), nhan + " Thu cuoi: con nut Tiem nang");
            kiem(!coVungDung("danh-hieu"), nhan + " Thu cuoi: con vien Danh hieu");
            kiem(coVungDung("o-thu-cuoi") && coVungDung("o-di-theo"), nhan + " Thu cuoi: thieu o Thu cuoi / Di theo");
        } else {
            kiem(soO == tenO.length, nhan + " Trang bi: thieu o do (" + soO + "/" + tenO.length + ")");
            kiem(coVungDung("tiem-nang"), nhan + " Trang bi: thieu nut Tiem nang");
            kiem(!coVungDung("o-thu-cuoi") && !coVungDung("o-di-theo"), nhan + " Trang bi: lan o Thu cuoi / Di theo");
        }
        if (thu) {
            // 0.9.13: tên thú trong danh sách không bị cắt ("Rapida..", "Hoo..", "Dragon..") - cấp / #id nhường chỗ
            String[] tenThu = {"Rapidash", "Hoothoot", "Dragonite", "Tauros", "Trứng Dratini"};
            for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
                String c = game.pokemon.GhiChu.CS[i];
                if (c == null || !c.endsWith("..")) continue;
                String goc = c.substring(0, c.length() - 2);
                for (int j = 0; j < tenThu.length; j++) {
                    kiem(goc.length() == 0 || !tenThu[j].startsWith(goc), nhan + " ten thu bi cat: '" + c + "'");
                }
            }
            if (ten.startsWith("nv_thu_dang_dung")) {
                boolean co = false;
                for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) co |= "Rapidash".equals(game.pokemon.GhiChu.CS[i]);
                kiem(co, nhan + " hang Rapidash khong hien du ten");
            }
        }
        if (ten.indexOf("dang_dung") >= 0) {
            int soThu = game.pokemon.QuanLyThu.soThu;
            int hang = game.model.Char.animal.size() + game.pokemon.QuanLyThu.dung.size();
            kiem(game.pokemon.QuanLyThu.coDung, nhan + " khong doc duoc khoi dang dung");
            kiem(hang == soThu, nhan + " so hang " + hang + " != O thu " + soThu);
            kiem(coChu("Ô thú: " + soThu + "/"), nhan + " thieu 'O thu: " + soThu + "/..'");
            if (ten.startsWith("nv_thu_dang_dung_tha_trung")) return;   // hộp hỏi đè danh sách
            // 0.9.16: mỗi tab con chỉ một loại - con đang dùng của loại đứng đầu tab (Rapidash / Hoothoot)
            boolean tabTheo = loaiThuDangXem() == 1;
            game.model.AnimalInfo dauTab = tabTheo ? game.pokemon.QuanLyThu.diTheo : game.pokemon.QuanLyThu.cuoi;
            if (dauTab != null) {
                kiem(coVungDung(tenHang(dauTab)), nhan + " thieu hang dang dung dau danh sach");
                kiem(coChu(tabTheo ? "Đang đi theo" : "Đang cưỡi"), nhan + " thieu nhan 'Dang di theo' / 'Dang cuoi'");
            }
            kiem(coChu("Đi theo (" + game.pokemon.QuanLyThu.dsLoai(1).size() + ")"), nhan + " tab Di theo thieu so");
            // nhãn từng hàng (danh sách có thể cuộn ở cỡ nhỏ: kiểm theo dữ liệu)
            for (int i = 0; i < game.model.Char.animal.size(); i++) {
                game.model.AnimalInfo an = (game.model.AnimalInfo) game.model.Char.animal.elementAt(i);
                String nt = game.pokemon.ManNhanVat.nhanThu(an);
                kiem(nt.equals(an.typeAnimal == 0 ? "Thú cưỡi" : "Đi theo"), nhan + " nhan hang tui " + i + " = " + nt);
            }
            if (game.pokemon.QuanLyThu.cuoi != null) {
                kiem("Đang cưỡi".equals(game.pokemon.ManNhanVat.nhanThu(game.pokemon.QuanLyThu.cuoi)), nhan + " nhan con dang cuoi");
            }
            kiem("Đang đi theo".equals(game.pokemon.ManNhanVat.nhanThu(game.pokemon.QuanLyThu.diTheo)), nhan + " nhan pet di theo");
            if (ten.startsWith("nv_thu_dang_dung") && ten.endsWith("_tb_di_theo")) {
                // chạm ô Đi theo: sân khấu xem pet đang đi theo, tab Đi theo
                kiem(coChu("Hoothoot"), nhan + " o Di theo: san khau thieu Hoothoot");
                kiem(game.pokemon.ManNhanVat.dangCuoiThu(), nhan + " dangCuoi sai (khoi dang dung noi dang cuoi)");
            } else if (ten.startsWith("nv_thu_dang_dung")) {
                kiem(coChu("Đang cưỡi Rapidash"), nhan + " san khau thieu 'Dang cuoi Rapidash'");
                kiem(coChu("Đang cưỡi"), nhan + " danh sach thieu nhan 'Dang cuoi'");
                kiem(coChu("Cấp 5"), nhan + " thieu cap thu dang cuoi");
                kiem(game.pokemon.ManNhanVat.dangCuoiThu(), nhan + " dangCuoi sai (khoi dang dung noi dang cuoi)");
            } else {
                kiem(coChu("Chưa cưỡi"), nhan + " pet di theo: thieu 'Chua cuoi'");
                kiem(coChu("đang đi theo"), nhan + " pet di theo: san khau thieu 'dang di theo'");
                kiem(!game.pokemon.ManNhanVat.dangCuoiThu(), nhan + " pet di theo: dangCuoi sai");
            }
            if (ten.endsWith("_chon")) {
                kiem(coChu("Xuống"), nhan + " chon con dang cuoi: thieu nut 'Xuong thu'");
                kiem(!coVungDung("thu-tha") && coVungDung("thu-khoa"), nhan + " chon con dang cuoi: khong co nut Tha, co nut Khoa");
            }
        }
    }

    static void dat(Object o, String ten, Object gt) {
        try {
            java.lang.reflect.Field f = o.getClass().getDeclaredField(ten);
            f.setAccessible(true);
            f.set(o, gt);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
