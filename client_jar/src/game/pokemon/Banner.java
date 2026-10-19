package game.pokemon;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Nền key art cho các màn trước khi vào game (tải, chọn máy chủ, đăng nhập, chọn / tạo nhân vật) - cùng một
 * cảnh hoàng hôn với bản PC (BannerParallax.cs), website và ảnh quảng bá (client_jar/tools/tao_banner.py):
 * trời hoàng hôn, mặt trời trên hồ, núi xa sương tím, rừng ngược sáng, thảm cỏ có lối mòn, bóng Rayquaza
 * mờ trên trời, mây trôi, đom đóm, 5 Pokémon khởi đầu đứng thở theo đội hình chữ V (Pokémon đang chọn: cột
 * sáng + vầng hào quang màu hệ).
 *
 * Hai chế độ:
 * - HD (APK: assets /bn/hd/ từ client_jar/shared/banner/res): cảnh tĩnh 880x400 ghép sẵn (nen.png), mây trôi, khung
 *   tiền cảnh, Pokémon phóng x2 - đúng khung hình key art.
 * - jar: dải lớp rộng 320 px (nui, rung, dat, khung, may, rong) neo theo chân trời, trời + mặt trời vẽ
 *   bằng code -> tự dựng lại cho màn dọc / ngang (176x220, 240x320, 320x240...). Bản NHẸ chỉ có dat.png.
 */
public final class Banner {
    private static boolean daNap;
    private static Image nui, rung, dat, khung, may, rong, quang, cot;
    private static Image hdNen, hdMay, hdKhung;
    private static Image logoHd, logoLon, logoNho;
    /** jar: độ cao (so với chân trời) đỉnh dải nui / rung / dat và chân 5 khởi đầu. */
    private static int dyNui = -36, dyRung = -26, dyDat = 3, dyKhoi = 57;
    /** HD: rộng, cao, chân trời, chân khởi đầu, tâm x, hệ số, y mây trong nen.png. */
    private static int hdW = 880, hdH = 400, hdChan = 234, hdKhoi = 344, hdCx = 440, hdK = 2, hdMayY = 40;
    private static Image rongTo;
    private static int rongToY = -1;

    /** Mốc dải màu trời (phần nghìn chiều cao trời) - khớp TROI trong tao_banner.py. */
    private static final int[] MOC = {0, 220, 420, 600, 740, 870, 1000};
    private static final int[] MAU = {0x12163D, 0x272462, 0x553384, 0xA4457F, 0xDE666B, 0xF99B5A, 0xFFD480};

    /** Đội hình chữ V (theo class 0..4): lệch ngang, lùi sau (px ở 1x), hướng (Char.DOWN/UP/LEFT/RIGHT). */
    private static final int[] DX = {-64, -124, 0, 64, 122};
    private static final int[] LUI = {9, 20, 0, 9, 20};
    private static final int[] HUONG = {3, 0, 0, 2, 0};
    /** thứ tự vẽ: sau trước, gần sau */
    private static final int[] THU_TU = {1, 4, 0, 3, 2};

    private Banner() {
    }

    private static Image anh(String ten) {
        try {
            return Image.createImage("/bn/" + ten + ".png");
        } catch (Throwable e) {
            return null;
        }
    }

    private static int[] docSo(String ten) {
        try {
            java.io.InputStream in = "".getClass().getResourceAsStream("/bn/" + ten);
            if (in == null) {
                return null;
            }
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = in.read()) >= 0) {
                sb.append((char) c);
            }
            in.close();
            int[] kq = new int[12];
            int n = 0;
            int so = 0;
            boolean am = false, co = false;
            for (int i = 0; i <= sb.length() && n < kq.length; i++) {
                char ch = i < sb.length() ? sb.charAt(i) : ' ';
                if (ch == '-') {
                    am = true;
                } else if (ch >= '0' && ch <= '9') {
                    so = so * 10 + (ch - '0');
                    co = true;
                } else {
                    if (co) {
                        kq[n++] = am ? -so : so;
                    }
                    so = 0;
                    am = false;
                    co = false;
                }
            }
            int[] r = new int[n];
            System.arraycopy(kq, 0, r, 0, n);
            return r;
        } catch (Throwable e) {
            return null;
        }
    }

    private static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        {
            hdNen = anh("hd/nen");
            if (hdNen != null) {
                hdMay = anh("hd/may");
                hdKhung = anh("hd/khung");
                logoHd = anh("hd/logo");
                int[] s = docSo("hd/bo_cuc.txt");
                if (s != null && s.length >= 7) {
                    hdW = s[0];
                    hdH = s[1];
                    hdChan = s[2];
                    hdKhoi = s[3];
                    hdCx = s[4];
                    hdK = s[5];
                    hdMayY = s[6];
                }
            }
        }
        logoLon = anh("logo");
        logoNho = anh("logo_nho");
        dat = anh("dat");
        if (hdNen == null) {
            nui = anh("nui");
            rung = anh("rung");
            khung = anh("khung");
            may = anh("may");
            rong = anh("rong");
            int[] s = docSo("bo_cuc.txt");
            if (s != null && s.length >= 4) {
                dyNui = s[0];
                dyRung = s[1];
                dyDat = s[2];
                dyKhoi = s[3];
            }
        }
        try {
            // quầng mặt trời: đĩa sáng alpha giảm dần
            int d = 96;
            int[] p = new int[d * d];
            for (int y = 0; y < d; y++) {
                for (int x = 0; x < d; x++) {
                    int dx = 2 * x + 1 - d;
                    int dy = 2 * y + 1 - d;
                    int r2 = (dx * dx + dy * dy) / 4;
                    int R = d / 2;
                    if (r2 < R * R) {
                        int t = 255 - r2 * 255 / (R * R);
                        p[y * d + x] = (t * t / 255 * 170 / 255) << 24 | 0xFFEFC4;
                    }
                }
            }
            quang = Image.createRGBImage(p, d, d, true);
            // cột sáng chiếu xuống Pokémon đang chọn: hình thang, alpha đậm dần xuống dưới
            int cw = 44;
            int ch = 96;
            int[] q = new int[cw * ch];
            for (int y = 0; y < ch; y++) {
                int nua = 6 + (cw / 2 - 6) * y / ch;
                int a = 20 + 70 * y / ch;
                for (int x = 0; x < cw; x++) {
                    int k = x - cw / 2;
                    if (k >= -nua && k < nua) {
                        int mep = nua - (k < 0 ? -k : k);
                        int aa = mep < 4 ? a * mep / 4 : a;
                        q[y * cw + x] = aa << 24 | 0xFFF6D0;
                    }
                }
            }
            cot = Image.createRGBImage(q, cw, ch, true);
        } catch (Throwable e) {
            quang = null;
            cot = null;
        }
    }

    /** Bản NHẸ (GOI=nhe): chỉ có lớp cỏ, không có ảnh núi -> tắt các lớp phủ tốn sức (đêm...). */
    public static boolean laBanNhe() {
        nap();
        return nui == null && hdNen == null;
    }

    /** Có ảnh banner trong jar không (không có thì nơi gọi vẽ nền cũ). */
    public static boolean co() {
        nap();
        return dat != null || hdNen != null;
    }

    /**
     * Ảnh logo lớn nhất vừa khung rongToiDa x caoToiDa (bản pixel art: HD 240 px, 160 px, 120 px); null nếu
     * jar không có logo.
     */
    public static Image logo(int rongToiDa, int caoToiDa) {
        nap();
        Image[] ds = {logoHd, logoLon, logoNho};
        Image nhoNhat = null;
        for (int i = 0; i < ds.length; i++) {
            Image a = ds[i];
            if (a == null) {
                continue;
            }
            nhoNhat = a;
            if (a.getWidth() <= rongToiDa && a.getHeight() <= caoToiDa) {
                return a;
            }
        }
        return nhoNhat != null && nhoNhat.getWidth() <= rongToiDa ? nhoNhat : null;
    }

    private static int mauTroi(int phanNghin) {
        if (phanNghin <= 0) {
            return MAU[0];
        }
        for (int i = 0; i < MOC.length - 1; i++) {
            if (phanNghin <= MOC[i + 1]) {
                return GiaoDien.pha(MAU[i], MAU[i + 1], phanNghin - MOC[i], MOC[i + 1] - MOC[i]);
            }
        }
        return MAU[MAU.length - 1];
    }

    /** Vẽ dải ảnh rộng cố định canh giữa màn; màn rộng hơn thì lặp sang hai bên. */
    private static void dai(Graphics g, Image a, int y, int w, int dich) {
        if (a == null) {
            return;
        }
        int aw = a.getWidth();
        int x = (w - aw) / 2 - dich;
        while (x > 0) {
            x -= aw;
        }
        for (; x < w; x += aw) {
            g.drawImage(a, x, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    /** Tung độ chân của Pokémon trên thảm cỏ (để màn khác canh chỗ). */
    public static int yCo() {
        return Canvas.h - game.render.screen.Screen.deltaY - 14;
    }

    /**
     * Vẽ toàn màn. chon = Pokémon khởi đầu đang chọn (0..4 theo LoaiPokemonClient.khoiDau), -1 = không;
     * veKhoiDau = có vẽ 5 Pokémon đứng trên cỏ hay không.
     */
    public static boolean ve(Graphics g, int chon, boolean veKhoiDau) {
        nap();
        if (dat == null && hdNen == null) {
            return false;
        }
        int w = Canvas.w;
        int h = Canvas.h;
        g.setClip(0, 0, w, h);
        if (hdNen != null) {
            veHd(g, chon, veKhoiDau, w, h);
            return true;
        }
        int tick = Canvas.gameTick;
        int yKhoi = yCo();
        int chan = yKhoi - dyKhoi;
        boolean du = nui != null;
        // ---- trời (dải 2 px) + sao
        for (int y = 0; y < chan + 4; y += 2) {
            g.setColor(mauTroi(y * 1000 / (chan > 0 ? chan : 1)));
            g.fillRect(0, y, w, 2);
        }
        for (int i = 0; i < 22; i++) {
            int x = (i * 97 + 13) % w;
            int y = (i * 53 + 7) % (chan * 45 / 100 + 1);
            if (((tick >> 3) + i) % 7 != 0) {
                g.setColor(i % 3 == 0 ? 0xC8D6FF : 0xFFF8E0);
                g.fillRect(x, y, 1, 1);
            }
        }
        // ---- mặt trời (trên mặt hồ, sau núi)
        int xMt = w * 56 / 100;
        int yMt = chan - 3;
        if (quang != null) {
            g.drawImage(quang, xMt, yMt, Graphics.HCENTER | Graphics.VCENTER);
        }
        g.setColor(0xFFECBA);
        g.fillArc(xMt - 8, yMt - 8, 16, 16, 0, 360);
        g.setColor(0xFFF8DE);
        g.fillArc(xMt - 6, yMt - 6, 12, 12, 0, 360);
        // ---- bóng Rayquaza mờ (tô theo màu trời tại chỗ) + mây trôi
        if (rong != null && w >= 280 && chan > 110) {
            // chỉ màn ngang đủ rộng: bên trái logo, trên núi
            veRong(g, w * 20 / 100, chan * 44 / 100);
        }
        if (may != null) {
            dai(g, may, Math.max(2, chan * 16 / 100), w, (tick / 7) % may.getWidth());
        }
        // ---- núi xa, rừng + hồ, cỏ (neo theo chân trời)
        if (du) {
            dai(g, nui, chan + dyNui, w, 0);
        }
        dai(g, rung, chan + dyRung, w, 0);
        dai(g, dat, chan + dyDat, w, 0);
        int dayDat = chan + dyDat + dat.getHeight();
        if (dayDat < h) {
            g.setColor(0x1A3528);
            g.fillRect(0, dayDat, w, h - dayDat);
        }
        // ---- đom đóm / lấp lánh
        veDomDom(g, w, chan - 6, yKhoi + 10 - (chan - 6), 1, tick);
        if (veKhoiDau) {
            veKhoiDau(g, chon, w / 2, yKhoi, 100);
        }
        // ---- khung tiền cảnh: hai góc dưới neo mép màn
        if (khung != null && w >= 220) {
            // chỉ phần mép ngoài (màn hẹp không che Pokémon hai bên đội hình)
            int kw = khung.getWidth();
            int kh = khung.getHeight();
            int y = h - game.render.screen.Screen.deltaY - kh + 6;
            int nua = Math.min(kw / 2, (w - 240) / 2 + 56);
            g.drawRegion(khung, 0, 0, nua, kh, 0, 0, y, Graphics.TOP | Graphics.LEFT);
            g.drawRegion(khung, kw - nua, 0, nua, kh, 0, w - nua, y, Graphics.TOP | Graphics.LEFT);
            g.setClip(0, 0, w, h);
        }
        return true;
    }

    /** Bóng Rayquaza: ảnh mặt nạ một màu tô lại theo màu trời ở độ cao y (một lần cho mỗi y). */
    private static void veRong(Graphics g, int x, int y) {
        if (rongTo == null || rongToY != y) {
            try {
                int rw = rong.getWidth(), rh = rong.getHeight();
                int[] p = new int[rw * rh];
                rong.getRGB(p, 0, rw, 0, 0, rw, rh);
                int chan = yCo() - dyKhoi;
                int mau = GiaoDien.pha(mauTroi((y + rh / 2) * 1000 / (chan > 0 ? chan : 1)), 0x1A1238, 9, 20);
                for (int i = 0; i < p.length; i++) {
                    if ((p[i] >>> 24) > 127) {
                        p[i] = 0xFF000000 | mau;
                    } else {
                        p[i] = 0;
                    }
                }
                rongTo = Image.createRGBImage(p, rw, rh, true);
                rongToY = y;
            } catch (Throwable e) {
                rong = null;
                return;
            }
        }
        g.drawImage(rongTo, x, y + (Canvas.gameTick / 12) % 2, Graphics.HCENTER | Graphics.VCENTER);
    }

    /** Chế độ HD (APK): cảnh 880x400 canh giữa, chân khởi đầu ở yCo(). */
    private static void veHd(Graphics g, int chon, boolean veKhoiDau, int w, int h) {
        int tick = Canvas.gameTick;
        int yKhoi = yCo();
        int ox = (w - hdW) / 2;
        int oy = yKhoi - hdKhoi;
        if (oy + hdH < h) {
            oy = h - hdH;
        }
        if (oy > 0) {
            g.setColor(MAU[0]);
            g.fillRect(0, 0, w, oy);
        }
        g.drawImage(hdNen, ox, oy, Graphics.TOP | Graphics.LEFT);
        if (ox > 0) {
            // màn rộng hơn cảnh (hiếm): kéo dài mép
            g.drawRegion(hdNen, 0, 0, 1, hdH, 0, 0, oy, Graphics.TOP | Graphics.LEFT);
            g.setColor(0x1A3528);
            g.fillRect(0, oy + hdChan + 20, ox, hdH);
            g.fillRect(ox + hdW, oy + hdChan + 20, ox + 1, hdH);
        }
        if (hdMay != null) {
            int mw = hdMay.getWidth();
            int x = ox - (tick / 4) % mw;
            for (; x < w; x += mw) {
                g.drawImage(hdMay, x, oy + hdMayY, Graphics.TOP | Graphics.LEFT);
            }
        }
        int cx = ox + hdCx;
        int yChan = oy + hdKhoi;
        veDomDom(g, w, oy + hdChan - 8, yChan + 20 - (oy + hdChan - 8), 2, tick);
        if (veKhoiDau) {
            veKhoiDau(g, chon, cx, yChan, hdK * 100);
        }
        if (hdKhung != null) {
            g.drawImage(hdKhung, ox, oy, Graphics.TOP | Graphics.LEFT);
        }
    }

    private static void veDomDom(Graphics g, int w, int yTren, int cao, int s, int tick) {
        for (int i = 0; i < 16; i++) {
            int pha = (tick + i * 13) % 48;
            if (pha >= 30) {
                continue;
            }
            int x = (i * 53 + tick * (2 + i % 3) / 8) % (w + 20) - 10;
            int y = yTren + (i * 37 * s) % (cao > 1 ? cao : 1) + (pha < 15 ? pha : 30 - pha) / 5;
            boolean to = pha > 8 && pha < 22;
            g.setColor(to ? 0xFFF7B0 : 0xFFD070);
            int c = to ? 2 * s : s;
            g.fillRect(x, y, c, c);
            if (to && (i & 1) == 0) {
                g.setColor(0xFFF0A0);
                g.fillRect(x - s, y, s, s);
                g.fillRect(x + 2 * s, y + s, s, s);
            }
        }
    }

    /** {cx, yChan, tiLe} của đội hình 5 Pokémon khởi đầu ở cỡ màn hiện tại (cùng phép tính với ve / veHd). */
    private static int[] doiHinh() {
        nap();
        int w = Canvas.w, h = Canvas.h;
        if (hdNen != null) {
            int ox = (w - hdW) / 2;
            int oy = yCo() - hdKhoi;
            if (oy + hdH < h) {
                oy = h - hdH;
            }
            return new int[]{ox + hdCx, oy + hdKhoi, hdK * 100};
        }
        return new int[]{w / 2, yCo(), 100};
    }

    /** Chỗ đứng (chân) Pokémon khởi đầu i: {x, y, tiLe}. */
    public static int[] viTriKhoiDau(int i) {
        int[] d = doiHinh();
        if (i < 0 || i >= DX.length) {
            return new int[]{d[0], d[1], d[2]};
        }
        return new int[]{d[0] + DX[i] * d[2] / 200, d[1] - LUI[i] * d[2] / 200, d[2]};
    }

    /** Vùng đội hình khởi đầu {x0, y0, x1, y1} (để bảng không che); null = không có banner. */
    public static int[] vungKhoiDau() {
        if (!co()) {
            return null;
        }
        int[] d = doiHinh();
        int t = d[2];
        int minX = 0, maxX = 0, lui = 0;
        for (int i = 0; i < DX.length; i++) {
            minX = Math.min(minX, DX[i]);
            maxX = Math.max(maxX, DX[i]);
            lui = Math.max(lui, LUI[i]);
        }
        int pad = 10 * t / 100;
        return new int[]{d[0] + minX * t / 200 - pad, d[1] - lui * t / 200 - 34 * t / 100, d[0] + maxX * t / 200 + pad, d[1] + 4 * t / 100};
    }

    /** 5 Pokémon khởi đầu theo đội hình chữ V quanh (cx, yChan); tiLe 100 = 1x, 200 = 2x. */
    private static void veKhoiDau(Graphics g, int chon, int cx, int yChan, int tiLe) {
        int n = ThuongHieu.SO_KHOI_DAU;
        int tick = Canvas.gameTick;
        int s = tiLe / 100;
        for (int j = 0; j < n && j < THU_TU.length; j++) {
            int i = THU_TU[j];
            int dex = LoaiPokemonClient.khoiDau(i);
            if (dex <= 0) {
                continue;
            }
            int x = cx + DX[i] * tiLe / 200;
            int y = yChan - LUI[i] * tiLe / 200;
            if (i == chon) {
                int mau = KieuO.mauHe(HuyHieuPoke.he(dex));
                if (cot != null) {
                    g.drawImage(cot, x, y + 2, Graphics.HCENTER | Graphics.BOTTOM);
                }
                int r = (13 + (tick % 20) / 4) * s;
                g.setColor(GiaoDien.pha(mau, 0x000000, 1, 3));
                g.fillArc(x - 15 * s, y - 4 * s, 30 * s, 9 * s, 0, 360);
                g.setColor(mau);
                g.fillArc(x - 13 * s, y - 3 * s, 26 * s, 7 * s, 0, 360);
                g.setColor(GiaoDien.pha(mau, 0xFFFFFF, 1, 2));
                g.drawArc(x - r - 2, y - 4 * s - r / 6, (r + 2) * 2, 9 * s + r / 3, 0, 360);
            } else {
                g.setColor(0x16301F);
                g.fillArc(x - 10 * s, y - 3 * s, 20 * s, 6 * s, 0, 360);
            }
            // thở: nhún theo nhịp, Pokémon đang chọn nhảy nhẹ
            int dy = 0;
            if (i == chon) {
                int t = (tick + i * 7) % 24;
                dy = t < 12 ? -(t < 6 ? t : 12 - t) * s / 2 : 0;
            }
            PokeBody.paint(g, dex, false, x, y + dy, HUONG[i], PokeDuLieu.DUNG, tick + i * 9, tiLe);
        }
    }
}
