import java.awt.image.BufferedImage;
import java.io.File;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM THẺ CHI TIẾT VẬT PHẨM (0.9.6.1, game.pokemon.HopChiTiet) - headless, không máy chủ. Lỗi 0.9.6 (APK, cửa hàng đá):
 * tên "Đá cầu vồng tinh khiết lv 6" bị ngắt thành tiêu đề + một dòng thân giả, nút "Đóng" / "Mua" (phím mềm đáy màn)
 * đè lên mô tả, "Mua" lòi khỏi mép phải thẻ, giá nằm dưới nút.
 *
 * Với cửa hàng (ShopNew: mua đá / bán) và hành trang / cửa hàng NPC (WindowInfoScr), mô tả ngắn / rất dài (phải cuộn),
 * có / không khảm ngọc, ở 11 cỡ (điện thoại 16:9 -> 21:9, máy tính bảng, PC 16:9 / 21:9 / 16:10 / 4:3) và cỡ J2ME
 * 320x240 / 240x320 (phông ảnh): thẻ trong vùng an toàn, canh giữa, rộng <= 80 % (hoặc tối thiểu 200 px khi màn hẹp);
 * mọi dòng chữ + nút nằm TRONG thẻ; không nút nào giao với chữ; nút ở dưới mọi chữ; tên vào tiêu đề đủ cả cụm (không
 * thành dòng thân); có hàng giá (biểu tượng tiền); phím mềm đáy màn không vẽ khi thẻ mở; bấm "Đóng" đóng thẻ.
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemHopChiTiet.java \
 *       server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemHopChiTiet [thư mục ảnh]
 * Mã thoát 1 = có lỗi.
 */
public class KiemHopChiTiet {
    static int dat, hong;
    static File ra;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static boolean giao(int[] a, int[] b) {
        return a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
    }

    static boolean trong(int[] a, int[] k) {
        return a[0] >= k[0] && a[1] >= k[1] && a[0] + a[2] <= k[0] + k[2] && a[1] + a[3] <= k[1] + k[3];
    }

    static final String TEN = "Đá cầu vồng tinh khiết lv 6";
    static final String MO_TA = "Dùng nâng cấp trang bị hoàn mỹ, tăng mạnh chỉ số khi khảm vào ô trống.";
    static final String MO_TA_DAI;
    static {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < 14; i++) b.append("Dòng mô tả số ").append(i + 1).append(" rất dài để thẻ phải cuộn phần thân. ");
        MO_TA_DAI = b.toString();
    }

    /** Chuỗi chi tiết đúng như ShopNew.setListSellGemItem (setTemp tên + mô tả + giá). */
    static String chuoi(String moTa, long gia, int loai) {
        return game.model.ItemInInventory.setTemp(TEN, "0") + game.model.ItemInInventory.setTemp(moTa, "0")
                + game.model.ItemInInventory.setTemp("Giá: " + game.pokemon.TienTe.chu(loai, gia), "0");
    }

    static int soAnh;

    static void datTruong(Object o, String ten, Object gt) {
        try {
            java.lang.reflect.Field f = o.getClass().getDeclaredField(ten);
            f.setAccessible(true);
            f.set(o, gt);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /** Cỡ màn như ChupTrongGame (dpi > 0 = điện thoại: cỡ chạm 44 dp, tai thỏ); j2me = màn logic đúng W x H, x1. */
    static void datCo(int W, int H, int dpi, boolean j2me) throws Exception {
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
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
            }
        });
        float s = NenPc.hienW / (float) NenPc.rong;
        if (dpi > 0) {
            float dpL = (dpi / 160f) / s;
            game.pokemon.VungCham.MIN = (int) Math.ceil(44 * dpL);
            game.pokemon.VungCham.CACH = (int) Math.floor(8 * dpL);
            game.pokemon.Bang.anTrai = (int) Math.ceil(100 / s);
        } else {
            game.pokemon.VungCham.MIN = j2me ? 0 : (int) Math.ceil(32 / s);
            game.pokemon.VungCham.CACH = j2me ? 0 : (int) Math.ceil(8 / s);
            game.pokemon.Bang.anTrai = 0;
        }
    }

    static void mot(final String man, final String nhan, final String text, final boolean kham, int W, int H, String co) throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    if (man.equals("shop")) {
                        game.render.screen.PauseMenu.gI().showShopNew(0);
                        game.model.ShopNew s = game.model.ShopNew.gI();
                        s.closeText();
                        s.left = new game.model.Command("Mua", new game.model.IAction() {
                            public void perform() {
                            }
                        });
                        datTruong(s, "arrayKhamNgoc", kham ? new byte[]{0, -1, 2} : null);
                        s.setShowText(text, 20, 20);
                    } else {
                        if (man.equals("da")) {
                            // cửa hàng đá (PauseMenu: WindowInfoScr tab 20) - đúng chỗ lỗi ảnh chụp
                            game.render.screen.WindowInfoScr.gI().setInfo(0, true, new byte[]{20});
                            game.render.screen.WindowInfoScr.gI().switchToMe();
                        } else {
                            game.render.screen.PauseMenu.gI().showMe(0);
                        }
                        game.render.screen.WindowInfoScr s = game.render.screen.WindowInfoScr.gI();
                        s.closeText();
                        s.left = new game.model.Command(man.equals("da") ? "Mua" : "Dùng", new game.model.IAction() {
                            public void perform() {
                            }
                        });
                        datTruong(s, "arrayKhamNgoc", kham ? new byte[]{1, -1} : null);
                        s.setShowText(text, 20, 20);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        int d0 = game.pokemon.HopChiTiet.demVe;
        BufferedImage a = ChupTrongGame.ve(W, H);
        String t = co + " " + man + " " + nhan;
        kiem(game.pokemon.HopChiTiet.demVe > d0, t + ": vẽ thẻ chi tiết chung (HopChiTiet)");
        int[] k = game.pokemon.HopChiTiet.VUA.clone();
        int sw = game.pokemon.Bang.phai() - game.pokemon.Bang.trai();
        kiem(k[0] >= game.pokemon.Bang.trai() && k[1] >= game.pokemon.Bang.tren() && k[0] + k[2] <= game.pokemon.Bang.phai()
                && k[1] + k[3] <= game.pokemon.Bang.duoi(), t + ": thẻ trong vùng an toàn " + java.util.Arrays.toString(k));
        int cx = k[0] + k[2] / 2 - (game.pokemon.Bang.trai() + game.pokemon.Bang.phai()) / 2;
        int cy = k[1] + k[3] / 2 - (game.pokemon.Bang.tren() + game.pokemon.Bang.duoi()) / 2;
        kiem(Math.abs(cx) <= 2 && Math.abs(cy) <= 2, t + ": thẻ canh giữa (" + cx + "," + cy + ")");
        kiem(k[2] <= Math.max(sw * 80 / 100, Math.min(sw, 200)), t + ": rộng <= 80 % vùng an toàn (" + k[2] + "/" + sw + ")");
        int nc = game.pokemon.HopChiTiet.soChu, nn = game.pokemon.HopChiTiet.soNut;
        kiem(nn >= 2, t + ": có nút Đóng + nút hành động trong thẻ (" + nn + ")");
        int chuDuoi = 0;
        for (int i = 0; i < nc; i++) {
            int[] c = game.pokemon.HopChiTiet.CHU[i];
            kiem(trong(c, k), t + ": dòng chữ " + i + " trong thẻ " + java.util.Arrays.toString(c));
            chuDuoi = Math.max(chuDuoi, c[1] + c[3]);
        }
        for (int j = 0; j < nn; j++) {
            int[] b = game.pokemon.HopChiTiet.NUT[j];
            kiem(trong(b, k), t + ": nút " + j + " trong thẻ " + java.util.Arrays.toString(b) + " / " + java.util.Arrays.toString(k));
            kiem(b[1] >= chuDuoi, t + ": nút " + j + " ở dưới mọi chữ");
            for (int i = 0; i < nc; i++) {
                kiem(!giao(b, game.pokemon.HopChiTiet.CHU[i]), t + ": nút " + j + " không đè chữ " + i);
            }
            for (int q = j + 1; q < nn; q++) kiem(!giao(b, game.pokemon.HopChiTiet.NUT[q]), t + ": hai nút không chồng");
        }
        // tên đủ cả cụm trong tiêu đề: "khiết lv 6" không thành dòng thân riêng
        String[] ds = game.pokemon.HopChiTiet.doanKiem();
        kiem(ds.length > 0 && ds[0].equals(game.pokemon.ChuPokemon.D(TEN).trim()), t + ": tên là MỘT đoạn tiêu đề (" + (ds.length > 0 ? ds[0] : "") + ")");
        boolean coGia = false;
        for (int i = 1; i < ds.length; i++) coGia |= game.pokemon.HopChiTiet.laGia(ds[i]);
        kiem(coGia, t + ": có hàng giá (biểu tượng tiền)");
        kiem(game.render.Canvas.currentScreen != null && ((game.model.Main) game.render.Canvas.currentScreen).anNutLenh,
                t + ": phím mềm đáy màn tắt khi thẻ mở");
        if (ra != null && soAnh < 60 && (co.startsWith("dt16") || co.equals("pc720") || co.startsWith("j2me"))) {
            javax.imageio.ImageIO.write(a, "png", new File(ra, String.format("%02d_%s_%s_%s.png", soAnh++, co, man, nhan)));
        }
        // đóng bằng nút Đóng (vùng chạm ct-nut-0 = phím mềm phải "Đóng")
        final boolean[] dong = {false};
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                Object s = game.render.Canvas.currentScreen;
                if (s instanceof game.model.ShopNew) {
                    ((game.model.ShopNew) s).right.action.perform();
                    dong[0] = !((game.model.ShopNew) s).isShowText && !((game.model.ShopNew) s).anNutLenh;
                } else if (s instanceof game.render.screen.WindowInfoScr) {
                    game.render.screen.WindowInfoScr w = (game.render.screen.WindowInfoScr) s;
                    w.closeText();
                    dong[0] = !w.isShowText && !w.anNutLenh;
                }
            }
        });
        kiem(dong[0], t + ": đóng thẻ trả lại phím mềm");
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "kiem_hop_chi_tiet");
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.giaLap();
            }
        });
        Thread.sleep(800);
        // mở hành trang một lần trước (lần đầu trên thế giới giả nạp dữ liệu túi - không phải phần đang kiểm)
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    // thế giới giả: mảng số thuốc ngắn hơn danh sách thuốc -> nới cho đủ (dữ liệu máy chủ thật luôn đủ)
                    game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
                    int n = game.model.MainChar.listPotion.length;
                    if (mc.potions == null || mc.potions.length < n) {
                        int[] p = new int[n];
                        if (mc.potions != null) System.arraycopy(mc.potions, 0, p, 0, mc.potions.length);
                        mc.potions = p;
                    }
                    game.render.screen.PauseMenu.gI().showMe(0);
                } catch (Throwable e) {
                    // bỏ
                }
            }
        });
        Thread.sleep(300);
        final String ngan = chuoi(MO_TA, 50, game.pokemon.TienTe.LUONG);
        final String dai = chuoi(MO_TA_DAI, 1250000, game.pokemon.TienTe.XU);
        game.pokemon.ChuUI boHd = game.pokemon.ChuUI.bo;
        int[][] CO = ChupTrongGame.CO;
        String[] TEN_CO = ChupTrongGame.TEN_CO;
        for (int c = 0; c <= CO.length + 1; c++) {
            final int W, H, dpi;
            String co;
            if (c < CO.length) {
                W = CO[c][0];
                H = CO[c][1];
                dpi = CO[c][2];
                co = TEN_CO[c];
                htbb.pc.ChuHD.bat = true;
                htbb.pc.GiaoDienHD.bat = true;
                game.pokemon.ChuUI.bo = boHd;
            } else {
                // máy J2ME: màn nhỏ, phông ảnh
                W = c == CO.length ? 320 : 240;
                H = c == CO.length ? 240 : 320;
                dpi = 0;
                co = c == CO.length ? "j2me320x240" : "j2me240x320";
                htbb.pc.ChuHD.bat = false;
                htbb.pc.GiaoDienHD.bat = false;
                game.pokemon.ChuUI.bo = null;
            }
            datCo(W, H, dpi, co.startsWith("j2me"));
            mot("shop", "ngan", ngan, false, W, H, co);
            mot("shop", "dai_kham", dai, true, W, H, co);
            mot("tui", "ngan", ngan, false, W, H, co);
            mot("tui", "dai", dai, false, W, H, co);
            mot("da", "ngan", ngan, false, W, H, co);
            mot("da", "dai_kham", dai, true, W, H, co);
        }
        System.out.println("KiemHopChiTiet: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
