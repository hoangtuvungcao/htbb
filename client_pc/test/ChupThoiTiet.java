import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM + ẢNH MẪU thời tiết / phủ màu mùa / ngày cộng đồng (0.9.7 đề xuất 8, game.pokemon.ThoiTiet / ThoiTietVe) ở 11 cỡ
 * chuẩn của ChupTrongGame (headless, đường vẽ bản PC):
 * <ul>
 * <li>đĩa thời tiết HUD: trong màn, cùng hàng nút trên, không chồng / đủ cách mọi vùng chạm HUD, bấm tâm trúng;</li>
 * <li>thẻ gợi ý: nằm trong màn, có dòng ảnh hưởng chiêu và dòng ngày cộng đồng;</li>
 * <li>dễ đọc: phủ mùa không đổi thẻ trạng thái HUD (vẽ sau phủ), độ sáng thế giới đổi &lt;= 16%;</li>
 * <li>rẻ cho J2ME: chất lượng Thấp &lt;= 40 lệnh vẽ / khung mọi thời tiết + mùa, Tắt = 0 hạt, thời gian vẽ trung bình nhỏ;</li>
 * <li>tắt màu mùa = ảnh như không phủ.</li>
 * </ul>
 * Ảnh mẫu mỗi thời tiết / mùa (pc1080 + dt20x9, cả bản J2ME Thấp) vào thư mục tham số.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java \
 *       client_pc/test/ChupThoiTiet.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java \
 *     && java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O ChupThoiTiet <thư mục ảnh>
 * Mã thoát 1 = có lỗi.
 */
public class ChupThoiTiet {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static final String[] TEN_TT = {"quang", "nang", "mua", "tuyet", "bao_cat", "suong_mu", "gio"};
    static final String[] TEN_MAU = {"", "xuan", "ha", "thu", "dong", "halloween", "tet", "noel", "trung_thu"};
    static final String[] GOI_Y = {"Quang đãng. Trời đẹp, không ảnh hưởng chiêu.",
        "Nắng gắt: chiêu hệ Lửa +10%, chiêu hệ Nước −10%. Pokémon hệ Lửa, Cỏ xuất hiện nhiều.",
        "Mưa: chiêu hệ Nước +10%, chiêu hệ Lửa −10%. Pokémon hệ Nước, Điện xuất hiện nhiều.",
        "Tuyết: chiêu hệ Băng +10%. Pokémon hệ Băng xuất hiện nhiều.",
        "Bão cát: chiêu hệ Đá +10%. Pokémon hệ Đá, Đất, Thép xuất hiện nhiều.",
        "Sương mù: chiêu hệ Ma +10%. Pokémon hệ Ma, Siêu linh xuất hiện nhiều.",
        "Gió mạnh: chiêu hệ Bay +10%. Pokémon hệ Bay, Rồng xuất hiện nhiều."};
    static final String[] TEN_MUA = {"", "Xuân hoa anh đào", "Hạ nắng vàng", "Thu lá phong", "Đông tuyết lạnh", "Hoàng hôn Halloween",
        "Sắc Tết", "Đêm Noel", "Đèn Trung Thu"};

    static void dat(final int tt, final int mau, final int cd, final int chatLuong, final boolean mauBat) throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                game.pokemon.ThoiTiet.co = false;   // ảnh mẫu: không bật thông báo "Trời chuyển..." mỗi cảnh
                game.pokemon.ThoiTiet.congDong = cd;
                game.pokemon.ThoiTiet.datTrangThai(tt, tt >= 0 ? game.pokemon.ThoiTiet.tenThoiTiet(tt) : "", tt >= 0 ? GOI_Y[tt] : "",
                        2 * 3600 + 600, 6, mau, TEN_MUA[mau], cd, cd > 0 ? "Gastly" : "", 1800, cd > 0,
                        cd > 0 ? "Dòng Gastly xuất hiện dày (40% lần hồi sinh) trên các tuyến của nó trong giờ vàng 14:00–17:00; hạ Gastly +20% EXP, shiny x5." : "");
                game.pokemon.ThoiTiet.chatLuong = chatLuong;
                game.pokemon.ThoiTiet.mauMuaBat = mauBat;
                game.pokemon.ThoiTiet.thu = true;
                game.pokemon.ThoiTiet.moThe = false;
                game.pokemon.DungDo.xoaHet();
            }
        });
    }

    /** Độ sáng trung bình (0..255) vùng [x0, y0, x1, y1) của ảnh. */
    static double sang(BufferedImage a, int x0, int y0, int x1, int y1) {
        long t = 0;
        int n = 0;
        for (int y = Math.max(0, y0); y < Math.min(a.getHeight(), y1); y += 2) {
            for (int x = Math.max(0, x0); x < Math.min(a.getWidth(), x1); x += 2) {
                int p = a.getRGB(x, y);
                t += ((p >> 16) & 255) * 299 + ((p >> 8) & 255) * 587 + (p & 255) * 114;
                n++;
            }
        }
        return n == 0 ? 0 : t / 1000.0 / n;
    }

    /** Khác biệt trung bình (0..255) giữa hai ảnh trong vùng. */
    static double khac(BufferedImage a, BufferedImage b, int x0, int y0, int x1, int y1) {
        long t = 0;
        int n = 0;
        for (int y = Math.max(0, y0); y < Math.min(a.getHeight(), y1); y += 2) {
            for (int x = Math.max(0, x0); x < Math.min(a.getWidth(), x1); x += 2) {
                int p = a.getRGB(x, y), q = b.getRGB(x, y);
                t += Math.abs(((p >> 16) & 255) - ((q >> 16) & 255)) + Math.abs(((p >> 8) & 255) - ((q >> 8) & 255)) + Math.abs((p & 255) - (q & 255));
                n += 3;
            }
        }
        return n == 0 ? 0 : t / (double) n;
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "anh_thoi_tiet");
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
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) {
            Thread.sleep(100);
        }
        Thread.sleep(2500);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.giaLap();
            }
        });
        Thread.sleep(800);
        // tắt hạt màn hình của mod (HieuUng "Hoa đào", cài đặt người chơi) để ảnh mẫu chỉ có lớp thời tiết / mùa
        try {
            java.lang.reflect.Field f = Class.forName("game.render.screen.HieuUng").getDeclaredField("manHinh");
            f.setAccessible(true);
            f.setInt(null, 0);
        } catch (Throwable e) {
            System.out.println("ChupThoiTiet: khong tat duoc HieuUng.manHinh: " + e);
        }
        String loc = System.getProperty("chup.co");
        for (int c = 0; c < ChupTrongGame.CO.length; c++) {
            final String co = ChupTrongGame.TEN_CO[c];
            if (loc != null && !("," + loc + ",").contains("," + co + ",")) {
                continue;
            }
            final int W = ChupTrongGame.CO[c][0], H = ChupTrongGame.CO[c][1], dpi = ChupTrongGame.CO[c][2];
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                    ChupTrongGame.moMan("the_gioi");
                }
            });
            float s = NenPc.hienW / (float) NenPc.rong;
            int min, cach;
            if (dpi > 0) {
                float dpL = (dpi / 160f) / s;
                min = (int) Math.ceil(44 * dpL);
                cach = (int) Math.floor(8 * dpL);
            } else {
                min = (int) Math.ceil(32 / s);
                cach = (int) Math.ceil(8 / s);
            }
            game.pokemon.VungCham.MIN = min;
            game.pokemon.VungCham.CACH = cach;
            boolean mau = "pc1080".equals(co) || "dt20x9".equals(co) || "pc4x3".equals(co);
            // gốc: không thời tiết, không phủ
            dat(-1, 0, 0, game.pokemon.ThoiTiet.DAY_DU, true);
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    game.pokemon.ThoiTiet.co = false;
                }
            });
            ChupTrongGame.ve(W, H);
            BufferedImage goc = ChupTrongGame.ve(W, H);
            int k = Math.max(1, NenPc.hienW / NenPc.rong);
            // thẻ trạng thái HUD (HudRong 170 x 50 px logic, trái trên) - trong ảnh ra
            int cx1 = (int) (NenPc.hienX + 160 * s), cy1 = (int) (NenPc.hienY + 44 * s);
            int wx0 = (int) (NenPc.hienX + 200 * s), wy0 = (int) (NenPc.hienY + 90 * s);
            int wx1 = (int) (NenPc.hienX + (NenPc.rong - 160) * s), wy1 = (int) (NenPc.hienY + (NenPc.cao - 90) * s);
            for (int tt = 0; tt < TEN_TT.length; tt++) {
                dat(tt, 0, 0, game.pokemon.ThoiTiet.DAY_DU, true);
                ChupTrongGame.ve(W, H);
                BufferedImage anh = ChupTrongGame.ve(W, H);
                if (mau) {
                    ImageIO.write(anh, "png", new File(ra, "thoi_tiet_" + TEN_TT[tt] + "_" + co + ".png"));
                }
                kiemHud(co + " " + TEN_TT[tt], min, cach);
                kiem(khac(goc, anh, (int) (NenPc.hienX + 6 * s), (int) (NenPc.hienY + 6 * s), cx1, cy1) < 1.5,
                        co + " " + TEN_TT[tt] + ": the trang thai HUD khong bi phu (" + khac(goc, anh, (int) (NenPc.hienX + 6 * s), (int) (NenPc.hienY + 6 * s), cx1, cy1) + ")");
            }
            for (int m = 1; m < TEN_MAU.length; m++) {
                // ảnh so sánh chụp NGAY trước (camera còn trôi sau khi vào map: ảnh gốc đầu cỡ không so được)
                dat(0, 0, 0, game.pokemon.ThoiTiet.DAY_DU, true);
                ChupTrongGame.ve(W, H);
                BufferedImage ref = ChupTrongGame.ve(W, H);
                double sangRef = sang(ref, wx0, wy0, wx1, wy1);
                dat(0, m, 0, game.pokemon.ThoiTiet.DAY_DU, true);
                ChupTrongGame.ve(W, H);
                BufferedImage anh = ChupTrongGame.ve(W, H);
                if (mau) {
                    ImageIO.write(anh, "png", new File(ra, "mua_" + TEN_MAU[m] + "_" + co + ".png"));
                }
                if (System.getProperty("chup.go") != null) {
                    System.out.println("GO mua " + m + " lenh " + game.pokemon.ThoiTietVe.demLenh + " cl " + game.pokemon.ThoiTiet.chatLuong + " mm " + game.pokemon.ThoiTiet.mauMua + " tick " + game.render.Canvas.gameTick + " w " + game.render.Canvas.w);
                }
                double sm = sang(anh, wx0, wy0, wx1, wy1);
                double lech = Math.abs(sm - sangRef) / Math.max(1, sangRef);
                kiem(lech <= 0.16, co + " mua " + TEN_MAU[m] + ": do sang the gioi doi " + Math.round(lech * 100) + "% <= 16%");
                kiem(khac(goc, anh, (int) (NenPc.hienX + 6 * s), (int) (NenPc.hienY + 6 * s), cx1, cy1) < 1.5, co + " mua " + TEN_MAU[m] + ": the HUD khong bi phu");
                // tắt màu mùa = như không phủ (thời tiết quang không hạt)
                dat(0, m, 0, game.pokemon.ThoiTiet.DAY_DU, false);
                ChupTrongGame.ve(W, H);
                BufferedImage tat = ChupTrongGame.ve(W, H);
                double kt = khac(ref, tat, wx0, wy0, wx1, wy1);
                // nhiễu nền (camera / hoạt hình còn trôi giữa hai lần chụp): chụp lại khung không phủ ngay sau
                dat(0, 0, 0, game.pokemon.ThoiTiet.DAY_DU, true);
                ChupTrongGame.ve(W, H);
                BufferedImage ref2 = ChupTrongGame.ve(W, H);
                double nhieu = khac(tat, ref2, wx0, wy0, wx1, wy1);
                kt = Math.min(kt, nhieu);
                kiem(kt < 4.0, co + " mua " + TEN_MAU[m] + " tat: nhu khong phu (" + kt + ")");
            }
            // ngày cộng đồng + thẻ gợi ý mở
            dat(2, 3, 2, game.pokemon.ThoiTiet.DAY_DU, true);
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    game.pokemon.ThoiTiet.moThe = true;
                }
            });
            ChupTrongGame.ve(W, H);
            BufferedImage the = ChupTrongGame.ve(W, H);
            if (mau) {
                ImageIO.write(the, "png", new File(ra, "the_goi_y_cong_dong_" + co + ".png"));
            }
            int[] v = game.pokemon.ThoiTiet.VUNG_THE;
            kiem(v[2] > 80 && v[3] > 30 && v[0] >= 0 && v[1] >= 0 && v[0] + v[2] <= NenPc.rong && v[1] + v[3] <= NenPc.cao,
                    co + ": the goi y trong man " + v[0] + "," + v[1] + " " + v[2] + "x" + v[3]);
            String[] d = game.pokemon.ThoiTiet.dongThe();
            String tatCa = String.join(" | ", d);
            kiem(tatCa.contains("Mưa · còn 2 giờ") && tatCa.contains("Nước +10%") && tatCa.contains("Sau đó: Gió mạnh")
                    && tatCa.contains("Ngày cộng đồng: giờ vàng") && tatCa.contains("Thu lá phong"), co + ": dong the goi y [" + tatCa + "]");
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    game.pokemon.ThoiTiet.moThe = false;
                }
            });
        }
        // J2ME: đường rẻ (Thấp) - đếm lệnh vẽ + thời gian / khung, Tắt = 0 hạt
        final BufferedImage bo = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        final java.awt.Graphics2D g2 = bo.createGraphics();
        final javax.microedition.lcdui.Graphics g = new javax.microedition.lcdui.Graphics(g2, 320, 240, 1);
        int maxThap = 0, maxDay = 0;
        long tg = 0;
        int lan = 0;
        for (int tt = 0; tt < TEN_TT.length; tt++) {
            for (int m = 0; m < TEN_MAU.length; m++) {
                for (int t = 0; t < 30; t++) {
                    long t0 = System.nanoTime();
                    game.pokemon.ThoiTietVe.ve(g, 320, 240, t * 7, tt, m, game.pokemon.ThoiTiet.THAP);
                    tg += System.nanoTime() - t0;
                    lan++;
                    maxThap = Math.max(maxThap, game.pokemon.ThoiTietVe.demLenh);
                    game.pokemon.ThoiTietVe.ve(g, 320, 240, t * 7, tt, m, game.pokemon.ThoiTiet.DAY_DU);
                    maxDay = Math.max(maxDay, game.pokemon.ThoiTietVe.demLenh);
                }
                game.pokemon.ThoiTietVe.ve(g, 320, 240, 0, tt, 0, game.pokemon.ThoiTiet.TAT);
                kiem(game.pokemon.ThoiTietVe.demLenh == 0, "Tat: " + TEN_TT[tt] + " khong ve hat (" + game.pokemon.ThoiTietVe.demLenh + ")");
            }
        }
        kiem(maxThap <= 40, "J2ME Thap: <= 40 lenh ve / khung (" + maxThap + ")");
        kiem(maxDay <= 400, "Day du (PC / APK, 320x240): <= 400 lenh ve / khung (" + maxDay + ")");
        kiem(tg / Math.max(1, lan) < 2_000_000L, "Thap: trung binh " + (tg / Math.max(1, lan) / 1000) + " us / khung < 2 ms (PC)");
        System.out.println("ChupThoiTiet: J2ME Thap max " + maxThap + " lenh, Day du max " + maxDay + " lenh, " + (tg / Math.max(1, lan) / 1000) + " us/khung");
        // ảnh mẫu bản J2ME (font ảnh, khối phẳng) chất lượng Thấp ở 640x360
        htbb.pc.GiaoDienHD.bat = false;
        htbb.pc.ChuHD.bat = false;
        game.pokemon.ChuUI.bo = null;
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                ChupTrongGame.moMan("the_gioi");
            }
        });
        for (int tt = 1; tt < TEN_TT.length; tt++) {
            dat(tt, tt == 3 ? 4 : tt == 5 ? 5 : 0, 0, game.pokemon.ThoiTiet.THAP, true);
            ChupTrongGame.ve(1280, 720);
            ImageIO.write(ChupTrongGame.ve(1280, 720), "png", new File(ra, "j2me_thap_" + TEN_TT[tt] + ".png"));
        }
        System.out.println("ChupThoiTiet: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }

    /** Đĩa thời tiết: có vùng chạm, trong màn, không chồng / đủ cách các vùng HUD khác, bấm tâm trúng. */
    static void kiemHud(String nhan, int min, int cach) {
        int n = game.pokemon.VungCham.dem();
        int iTt = -1;
        for (int i = 0; i < n; i++) {
            if ("hud-thoi-tiet".equals(game.pokemon.VungCham.ten(i))) {
                iTt = i;
            }
        }
        kiem(iTt >= 0, nhan + ": co vung cham hud-thoi-tiet");
        if (iTt < 0) {
            return;
        }
        int[] v = game.pokemon.VungCham.vungVe(iTt);
        kiem(v[0] >= 0 && v[1] >= 0 && v[0] + v[2] <= NenPc.rong + 1 && v[1] + v[3] <= NenPc.cao + 1, nhan + ": dia trong man");
        int cx = v[0] + v[2] / 2, cy = v[1] + v[3] / 2;
        kiem(game.pokemon.VungCham.tim(cx, cy) == iTt, nhan + ": bam tam trung dia thoi tiet");
        for (int q = 0; q < n; q++) {
            if (q == iTt || game.pokemon.VungCham.lopVung(q) != game.pokemon.VungCham.lopVung(iTt)) {
                continue;
            }
            String tq = game.pokemon.VungCham.ten(q);
            if (tq != null && tq.startsWith("nen")) {
                continue;
            }
            int[] w = game.pokemon.VungCham.vungVe(q);
            int hx = Math.max(w[0] - (v[0] + v[2]), v[0] - (w[0] + w[2]));
            int hy = Math.max(w[1] - (v[1] + v[3]), v[1] - (w[1] + w[3]));
            int ho = Math.max(hx, hy);
            if (tq != null && tq.startsWith("hud-") && !"hud-the".equals(tq) && !"hud-tu-dong".equals(tq)) {
                double dx = (v[0] + v[2] / 2.0) - (w[0] + w[2] / 2.0), dy = (v[1] + v[3] / 2.0) - (w[1] + w[3] / 2.0);
                ho = (int) Math.floor(Math.sqrt(dx * dx + dy * dy) - v[2] / 2.0 - w[2] / 2.0);
            }
            // >= khoảng hở giữa các nút hàng trên có sẵn (bước 30, r 12 -> 5 px logic) hoặc cỡ cách chuẩn nếu nhỏ hơn
            kiem(ho >= Math.min(cach, 5), nhan + ": dia thoi tiet cach [" + tq + "] " + ho);
        }
    }
}
