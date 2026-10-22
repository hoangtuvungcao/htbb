package game.pokemon;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * BỘ VẼ KHUNG PIXEL kiểu NRO / NSO (0.9.7) - một mã cho J2ME, APK, PC: mảnh 9-slice và hình cố định trong
 * /nro/khung.png (bảng {@link KhungNroBang}, sinh bởi tools/giao_dien_nro/sinh_khung_nro.py), icon pixel 16x16 trong
 * /nro/icon.png.
 *
 * - 1 px ảnh = 1 px logic giao diện; góc vẽ nguyên, cạnh + giữa LÁT LẶP bằng drawRegion (MIDP 2.0), giữa một màu thì
 *   fillRect. Bản PC / APK phóng khung logic lên màn thật bằng láng giềng gần nhất -> pixel nét.
 * - Icon phóng HỆ SỐ NGUYÊN (16, 32, 48... px), ảnh phóng nhớ đệm (createRGBImage) - không cấp phát mỗi khung.
 * - Thiếu ảnh (tệp hỏng / máy hết bộ nhớ): {@link #co()} = false, người gọi vẽ khối phẳng như cũ.
 */
public final class KhungNro {
    private KhungNro() {
    }

    private static Image khung, icon;
    private static boolean daNap, hong;

    /** Đã nạp tấm khung (vẽ được). */
    public static boolean co() {
        if (!daNap) {
            nap();
        }
        return khung != null;
    }

    private static synchronized void nap() {
        if (daNap) {
            return;
        }
        try {
            khung = Image.createImage(KhungNroBang.TAM_KHUNG);
        } catch (Throwable e) {
            khung = null;
        }
        try {
            icon = Image.createImage(KhungNroBang.TAM_ICON);
        } catch (Throwable e) {
            icon = null;
        }
        daNap = true;
    }

    public static int rong(int manh) {
        return KhungNroBang.MANH[manh * 9 + 2];
    }

    public static int cao(int manh) {
        return KhungNroBang.MANH[manh * 9 + 3];
    }

    /** Lề 9-slice: 0 trái, 1 trên, 2 phải, 3 dưới. */
    public static int le(int manh, int canh) {
        return KhungNroBang.MANH[manh * 9 + 4 + canh];
    }

    /** Màu thân (giữa) của mảnh; -1 = lát ảnh. */
    public static int mauGiua(int manh) {
        return KhungNroBang.GIUA[manh];
    }

    /** Vẽ vùng (sx, sy, w, h) của tấm tại (x, y). */
    private static void vung(Graphics g, int sx, int sy, int w, int h, int x, int y) {
        if (w > 0 && h > 0) {
            g.drawRegion(khung, sx, sy, w, h, 0, x, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    /** Lát vùng nguồn (sx, sy, sw, sh) phủ (x, y, w, h). */
    private static void lat(Graphics g, int sx, int sy, int sw, int sh, int x, int y, int w, int h) {
        if (sw <= 0 || sh <= 0 || w <= 0 || h <= 0) {
            return;
        }
        for (int yy = 0; yy < h; yy += sh) {
            int hh = Math.min(sh, h - yy);
            for (int xx = 0; xx < w; xx += sw) {
                vung(g, sx, sy, Math.min(sw, w - xx), hh, x + xx, y + yy);
            }
        }
    }

    /**
     * MẢNH 9-SLICE manh phủ (x, y, w, h). Nhỏ hơn hai lề: lề co lại (lấy nửa trái / phải của góc).
     * Trả false nếu chưa có ảnh (người gọi tự vẽ khối).
     */
    public static boolean ve(Graphics g, int manh, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return true;
        }
        if (!co()) {
            return false;
        }
        if (GhiChu.bat) {
            GhiChu.khung(g, manh, x, y, w, h);
        }
        short[] m = KhungNroBang.MANH;
        int i = manh * 9;
        int sx = m[i], sy = m[i + 1], sw = m[i + 2], sh = m[i + 3];
        int l = m[i + 4], t = m[i + 5], r = m[i + 6], b = m[i + 7];
        if (l + r > w) {
            l = w / 2;
            r = w - l;
        }
        if (t + b > h) {
            t = h / 2;
            b = h - t;
        }
        int cw = sw - m[i + 4] - m[i + 6], ch = sh - m[i + 5] - m[i + 7];   // cỡ phần giữa nguồn
        int gx = sx + m[i + 4], gy = sy + m[i + 5];                          // gốc phần giữa nguồn
        int rx = sx + sw - r, by = sy + sh - b;                               // gốc cạnh phải / dưới nguồn
        int mw = w - l - r, mh = h - t - b;
        // 4 góc
        vung(g, sx, sy, l, t, x, y);
        vung(g, rx, sy, r, t, x + w - r, y);
        vung(g, sx, by, l, b, x, y + h - b);
        vung(g, rx, by, r, b, x + w - r, y + h - b);
        // 4 cạnh
        lat(g, gx, sy, cw, t, x + l, y, mw, t);
        lat(g, gx, by, cw, b, x + l, y + h - b, mw, b);
        lat(g, sx, gy, l, ch, x, y + t, l, mh);
        lat(g, rx, gy, r, ch, x + w - r, y + t, r, mh);
        // giữa
        int giua = KhungNroBang.GIUA[manh];
        if (giua >= 0) {
            if (mw > 0 && mh > 0) {
                g.setColor(giua);
                g.fillRect(x + l, y + t, mw, mh);
            }
        } else {
            lat(g, gx, gy, cw, ch, x + l, y + t, mw, mh);
        }
        ChuUI c = ChuUI.bo;
        if (c != null) {
            c.nen(g, x, y, w, h, GiaoDien.doSang(giua >= 0 ? giua : 0x808080));
        }
        return true;
    }

    /** Hình cố định manh, góc trái trên (x, y). */
    public static boolean hinh(Graphics g, int manh, int x, int y) {
        if (!co()) {
            return false;
        }
        short[] m = KhungNroBang.MANH;
        int i = manh * 9;
        vung(g, m[i], m[i + 1], m[i + 2], m[i + 3], x, y);
        return true;
    }

    /** Hình cố định manh, tâm (cx, cy). */
    public static boolean hinhGiua(Graphics g, int manh, int cx, int cy) {
        return hinh(g, manh, cx - rong(manh) / 2, cy - cao(manh) / 2);
    }

    /** Một vùng con của hình cố định (cắt phải / dưới) - thanh / ô cỡ lẻ. */
    public static boolean hinhCat(Graphics g, int manh, int x, int y, int w, int h) {
        if (!co()) {
            return false;
        }
        short[] m = KhungNroBang.MANH;
        int i = manh * 9;
        vung(g, m[i], m[i + 1], Math.min(w, m[i + 2]), Math.min(h, m[i + 3]), x, y);
        return true;
    }

    // ------------------------------------------------------------------ icon pixel 16x16 (phóng hệ số nguyên)

    /** Có icon id (ô trong tấm, không trống). */
    public static boolean coIcon(int id) {
        if (!co() || icon == null || id < 0 || id >= KhungNroBang.SO_ICON) {
            return false;
        }
        return true;
    }

    /** Hệ số phóng icon cho ô cạnh s: lớn nhất mà 16k <= s (tối thiểu 1). */
    public static int heSoIcon(int s) {
        return Math.max(1, s / 16);
    }

    private static final int SO_DEM = 128;
    private static final int[] DEM_KHOA = new int[SO_DEM];
    private static final Image[] DEM_ANH = new Image[SO_DEM];
    private static int demVong;
    private static int[] dem16;

    /**
     * Icon id cạnh 16*k (k = heSoIcon(s)), canh giữa ô (x, y, s, s). bong >= 0: vẽ BÓNG MỜ một màu (ô trống),
     * -1 = đủ màu. Trả false khi không có icon (người gọi vẽ vector cũ).
     */
    public static boolean icon(Graphics g, int id, int x, int y, int s, int bong) {
        if (s < 8 || !coIcon(id)) {   // 0.9.7: icon PNG cả ở ô nhỏ (16 px canh giữa, tràn nhẹ)
            return false;
        }
        int k = heSoIcon(s);
        Image a = anhIcon(id, k, bong);
        if (a == null) {
            return false;
        }
        int c = 16 * k;
        g.drawImage(a, x + (s - c) / 2, y + (s - c) / 2, Graphics.TOP | Graphics.LEFT);
        return true;
    }

    /** Icon id phóng k, tâm (cx, cy). */
    public static boolean iconGiua(Graphics g, int id, int cx, int cy, int k, int bong) {
        if (!coIcon(id)) {
            return false;
        }
        Image a = anhIcon(id, Math.max(1, k), bong);
        if (a == null) {
            return false;
        }
        int c = 16 * Math.max(1, k);
        g.drawImage(a, cx - c / 2, cy - c / 2, Graphics.TOP | Graphics.LEFT);
        return true;
    }

    /** Biến đổi drawRegion quay 0 / 90 / 180 / 270 độ (MIDP Sprite.TRANS_*). */
    private static final int[] XOAY = {0, 5, 3, 6};

    /** Icon id cỡ 16 (không phóng) quay xoay*90 độ, tâm (cx, cy) - Poké Ball chờ quay. */
    public static boolean iconXoay(Graphics g, int id, int cx, int cy, int xoay) {
        if (!coIcon(id)) {
            return false;
        }
        int ix = (id % KhungNroBang.COT_ICON) * 16, iy = (id / KhungNroBang.COT_ICON) * 16;
        g.drawRegion(icon, ix, iy, 16, 16, XOAY[xoay & 3], cx - 8, cy - 8, Graphics.TOP | Graphics.LEFT);
        return true;
    }

    /** Hình cố định manh lật gương ngang (mũi tên trái từ mũi tên phải), góc trái trên (x, y). */
    public static boolean hinhLat(Graphics g, int manh, int x, int y) {
        if (!co()) {
            return false;
        }
        short[] m = KhungNroBang.MANH;
        int i = manh * 9;
        g.drawRegion(khung, m[i], m[i + 1], m[i + 2], m[i + 3], 2, x, y, Graphics.TOP | Graphics.LEFT);
        return true;
    }

    private static synchronized Image anhIcon(int id, int k, int bong) {
        if (k > 6) {
            k = 6;
        }
        int khoa = (id * 8 + k) * 31 + (bong < 0 ? 0 : bong + 1);
        int o = (khoa & 0x7FFFFFFF) % SO_DEM;
        for (int d = 0; d < 4; d++) {
            int j = (o + d) % SO_DEM;
            if (DEM_ANH[j] != null && DEM_KHOA[j] == khoa) {
                return DEM_ANH[j];
            }
        }
        try {
            if (dem16 == null) {
                dem16 = new int[256];
            }
            int ix = (id % KhungNroBang.COT_ICON) * 16, iy = (id / KhungNroBang.COT_ICON) * 16;
            icon.getRGB(dem16, 0, 16, ix, iy, 16, 16);
            int n = 16 * k;
            int[] ra = new int[n * n];
            for (int yy = 0; yy < n; yy++) {
                for (int xx = 0; xx < n; xx++) {
                    int p = dem16[(yy / k) * 16 + xx / k];
                    if (bong >= 0 && (p >>> 24) != 0) {
                        p = ((p >>> 24) * 3 / 4) << 24 | (bong & 0xFFFFFF);
                    }
                    ra[yy * n + xx] = p;
                }
            }
            Image a = Image.createRGBImage(ra, n, n, true);
            int j = o;
            for (int d = 0; d < 4; d++) {
                if (DEM_ANH[(o + d) % SO_DEM] == null) {
                    j = (o + d) % SO_DEM;
                    break;
                }
                if (d == 3) {
                    j = (o + (demVong++ & 3)) % SO_DEM;
                }
            }
            DEM_ANH[j] = a;
            DEM_KHOA[j] = khoa;
            return a;
        } catch (Throwable e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ ghép sẵn hay dùng

    /** Bóng đổ cứng 2 px (nâu đen) dưới-phải khối (x, y, w, h) - kiểu pixel, không bóng mềm. */
    public static void bong(Graphics g, int x, int y, int w, int h) {
        g.setColor(ChuDe.BONG);
        g.fillRect(x + 2, y + h, w - 2, 2);
        g.fillRect(x + w, y + 2, 2, h);
    }

    /** Nút khối theo kiểu Bang (CHINH / PHU...) và trạng thái. */
    public static int manhNut(int kieu, boolean an) {
        int m;
        switch (kieu) {
            case Bang.CHINH:
                m = KhungNroBang.NUT_CAM;
                break;
            case Bang.XAC_NHAN:
                m = KhungNroBang.NUT_LA;
                break;
            case Bang.PHU:
                m = KhungNroBang.NUT_XANH;
                break;
            case Bang.NGUY:
                m = KhungNroBang.NUT_DO;
                break;
            case Bang.TAT:
                return KhungNroBang.NUT_DA;
            case Bang.GO:
                m = KhungNroBang.NUT_GO;
                break;
            default:
                m = KhungNroBang.NUT_GIAY;
                break;
        }
        return an ? m + 1 : m;   // mảnh _NHAN ngay sau mảnh thường (bộ sinh xếp liền)
    }
}
