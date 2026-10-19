package game.pokemon;

import game.render.Canvas;
import game.render.screen.Font;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

/**
 * BẢNG CHUNG (0.9.3, ưu tiên điện thoại): MỘT bộ thành phần cho mọi bảng trong game (Nhân vật, Hành trang, Menu,
 * Kỹ năng, Thú...) để đầu bảng, tab, nút đóng, hàng danh sách, nút bấm giống nhau ở mọi nơi và đủ to cho ngón tay.
 *
 * - Bảng phủ cả màn (trong vùng an toàn: tai thỏ / bo góc - nền đặt anTrai..anDuoi theo px logic).
 * - Đầu bảng: dải đỏ tiêu đề + nút ĐÓNG (X) to, LUÔN ở góc phải trên. Tab ngay dưới đầu bảng, chạm một lần.
 * - Mọi điều khiển đăng ký vùng chạm (VungCham) ngay lúc vẽ; cỡ tối thiểu = max(VungCham.MIN, cỡ thiết kế):
 *   APK ~ 44 dp, PC ~ 32 px thật. Khoảng cách giữa điều khiển >= cach() (~ 8 dp).
 * - Chữ: font thường của game (Be Vietnam Pro HD trên APK / PC) - không dùng font nhỏ.
 */
public final class Bang {
    /** vùng an toàn (px logic, tính từ mép màn) - nền đặt mỗi khung (APK Hud, PC 0) */
    public static int anTrai, anTren, anPhai, anDuoi;

    public static final int NEN_THE = 0xF7F9FD;
    public static final int NEN_THE_DUOI = 0xE8EDF6;
    public static final int CHU_PHU = 0x5E6A80;
    public static final int TANG = 0x2E9E4A;
    public static final int GIAM = 0xD8342C;

    private Bang() {
    }

    /** Cỡ chạm tối thiểu (px logic) cho thiết kế cỡ d. */
    public static int co(int d) {
        return Math.max(VungCham.MIN, d);
    }

    /** Khoảng cách tối thiểu giữa hai điều khiển (px logic). */
    public static int cach() {
        return Math.max(VungCham.CACH, Math.max(4, VungCham.MIN / 5));
    }

    /** Cao đầu bảng. */
    public static int caoDau() {
        return co(28);
    }

    /** Cao thanh tab. */
    public static int caoTab() {
        return co(26);
    }

    /** Cao hàng danh sách: >= 48 dp (MIN = 44 dp -> x 12/11), PC >= 30 px logic. */
    public static int caoHang() {
        return Math.max(32, VungCham.MIN * 12 / 11 + 1);
    }

    /** Cạnh ô lưới menu: >= 64 dp. */
    public static int coO() {
        return Math.max(56, VungCham.MIN * 16 / 11 + 1);
    }

    // ------------------------------------------------------------------ vùng cắt đăng ký chạm (danh sách cuộn)
    private static boolean cat;
    private static int cx0, cy0, cx1, cy1;

    /** Đăng ký chạm trong danh sách cuộn: chỉ phần THẤY được của hàng (che quá nửa / thấy < cỡ chạm tối thiểu thì không nhận chạm). */
    public static void dangKy(Graphics g, int x, int y, int w, int h, Object viec, String ten) {
        if (viec == null) {
            return;
        }
        if (!cat) {
            VungCham.them(g, x, y, w, h, viec, ten);
            return;
        }
        int ax = x + g.getTranslateX(), ay = y + g.getTranslateY();
        int l = Math.max(ax, cx0), t = Math.max(ay, cy0), r = Math.min(ax + w, cx1), b = Math.min(ay + h, cy1);
        // hàng bị che quá nửa, hoặc phần thấy nhỏ hơn cỡ chạm tối thiểu: không nhận chạm (cuộn thêm để bấm)
        if (r - l < Math.max(w / 2, Math.min(w, VungCham.MIN)) || b - t < Math.max(h / 2, Math.min(h, VungCham.MIN))) {
            return;
        }
        VungCham.themTuyetDoi(l, t, r - l, b - t, viec, ten);
    }

    /** Hàng của danh sách cuộn. */
    public interface Hang {
        void ve(Graphics g, int i, int x, int y, int w, int h);
    }

    /**
     * Danh sách cuộn quán tính (Cuon): n hàng cao rh trong khung (x, y, w, h); thanh cuộn mảnh bên phải khi dài hơn
     * khung. Trả bề ngang dành cho hàng.
     */
    public static void danhSach(Graphics g, Cuon c, int x, int y, int w, int h, int n, int rh, Hang hang) {
        int k = cach();
        int noi = n <= 0 ? 0 : n * (rh + k) - k;
        c.khung(g, x, y, w, h, noi);
        c.capNhat();
        boolean thanh = noi > h;
        int wr = thanh ? w - 10 : w;
        int lech = c.lech();
        int tx = g.getTranslateX(), ty = g.getTranslateY();
        g.setClip(x, y, w, h);
        cat = true;
        cx0 = x + tx;
        cy0 = y + ty;
        cx1 = x + tx + w;
        cy1 = y + ty + h;
        try {
            int dau = Math.max(0, lech / (rh + k));
            for (int i = dau; i < n; i++) {
                int yr = y + i * (rh + k) - lech;
                if (yr >= y + h) {
                    break;
                }
                hang.ve(g, i, x, yr, wr, rh);
                g.setClip(x, y, w, h);
            }
        } finally {
            cat = false;
        }
        g.setClip(0, 0, Canvas.w, Canvas.h);
        if (thanh) {
            c.veThanh(g, x + w - 5, y + 2, h - 4);
        }
    }

    /** Chip lọc (viên nhỏ chọn / không chọn) - trả bề ngang. */
    public static int chip(Graphics g, int x, int y, String chu, boolean chon, Object viec, String ten) {
        int h = co(24);
        int w = Math.max(h, Font.blackFont.getWidth(chu) + 18);
        GiaoDien.khoi(g, x, y, w, h, h / 2, chon ? GiaoDien.VANG_TOI : 0x9AA6BC);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, h / 2 - 1, chon ? GiaoDien.VANG_NHAT : 0xFFFFFF, chon ? GiaoDien.VANG : 0xE6EAF4);
        Font.blackFont.drawString(g, chu, x + w / 2, y + (h - Font.blackFont.getHeight()) / 2, Font.CENTER);
        VungCham.them(g, x, y, w, h, viec, ten);
        return w;
    }

    /** Hàng chip lọc (tự xuống dòng khi hết bề ngang): trả cao đã dùng. chon = chip đang chọn; viec[i] chạm chip i. */
    public static int hangChip(Graphics g, int x, int y, int w, String[] chu, int chon, Object[] viec) {
        int k = cach();
        int hc = co(24);
        int xx = x, yy = y;
        for (int i = 0; i < chu.length; i++) {
            int wc = Math.max(hc, Font.blackFont.getWidth(chu[i]) + 18);
            if (xx + wc > x + w && xx > x) {
                xx = x;
                yy += hc + k;
            }
            xx += chip(g, xx, yy, chu[i], i == chon, viec == null ? null : viec[i], "chip-" + i) + k;
        }
        return yy + hc - y;
    }

    public static int trai() {
        return anTrai + 4;
    }

    public static int tren() {
        return anTren + 3;
    }

    public static int phai() {
        return Canvas.w - anPhai - 4;
    }

    public static int duoi() {
        return Canvas.h - anDuoi - 3;
    }

    /** Phủ mờ nền game phía sau. */
    public static void phuNen(Graphics g) {
        Canvas.resetTrans(g);
        KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, 0xB0060B1A);
    }

    /**
     * Khung bảng phủ cả vùng an toàn: đầu bảng (tiêu đề + nút đóng) + thanh tab (tabs null = không có tab).
     * Trả {x, y, w, h} vùng nội dung bên dưới.
     */
    public static int[] khung(Graphics g, String tieuDe, String[] tabs, int tabChon, Object[] viecTab, Object dong) {
        Canvas.resetTrans(g);
        int x = trai(), y = tren(), r = phai(), b = duoi();
        int w = r - x, h = b - y;
        // thân bảng kính xanh đậm (vẽ tay: không qua GiaoDien.bangMo - APK phóng bảng đó như màn trước game)
        GiaoDien.khoi(g, x + 1, y + 2, w, h, 10, 0x0A0F2C);
        GiaoDien.khoi(g, x, y, w, h, 10, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 9, 0x2A4C8C, 0x13224E);
        g.setColor(0x6E8FD8);
        g.drawRoundRect(x + 2, y + 2, w - 5, h - 5, 14, 14);
        Font.nenSang = false;
        int hd = caoDau();
        int sDong = hd;   // nút đóng vuông bằng cao đầu bảng
        GiaoDien.dai(g, x + 4, y + 4, w - 8 - sDong - cach(), hd - 6, tieuDe);
        nutDong(g, r - 3 - sDong, y + 2, sDong, dong);
        int yNoi = y + 2 + hd + cach();
        if (tabs != null && tabs.length > 0) {
            int ht = caoTab();
            int n = tabs.length;
            int gap = cach();
            int wt = (w - 8 - gap * (n - 1)) / n;
            for (int i = 0; i < n; i++) {
                int xt = x + 4 + i * (wt + gap);
                tab(g, xt, yNoi, wt, ht, tabs[i], i == tabChon, viecTab == null ? null : viecTab[i], "tab-" + i);
            }
            yNoi += ht + cach();
        }
        return new int[]{x + 4, yNoi, w - 8, b - 4 - yNoi};
    }

    /** Nút đóng tròn đỏ dấu X (góc phải trên mọi bảng). */
    public static void nutDong(Graphics g, int x, int y, int s, Object viec) {
        int c = s / 2;
        int r = Math.max(9, s / 2 - 2);
        g.setColor(GiaoDien.VIEN);
        g.fillArc(x + c - r - 1, y + c - r - 1, 2 * r + 2, 2 * r + 2, 0, 360);
        g.setColor(GiaoDien.DO);
        g.fillArc(x + c - r, y + c - r, 2 * r, 2 * r, 0, 360);
        g.setColor(GiaoDien.DO_SANG);
        g.fillArc(x + c - r + 2, y + c - r + 1, 2 * r - 4, r, 0, 180);
        int k = Math.max(3, r / 2);
        g.setColor(GiaoDien.TRANG);
        for (int d = -1; d <= 1; d++) {
            g.drawLine(x + c - k + d, y + c - k, x + c + k + d, y + c + k);
            g.drawLine(x + c + k + d, y + c - k, x + c - k + d, y + c + k);
        }
        VungCham.them(g, x, y, s, s, viec, "dong");
    }

    /** Tab (viên): đang chọn = vàng chữ tối, còn lại = kính xanh chữ trắng. */
    public static void tab(Graphics g, int x, int y, int w, int h, String chu, boolean chon, Object viec, String ten) {
        GiaoDien.khoi(g, x, y, w, h, 6, GiaoDien.VIEN);
        if (chon) {
            GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 5, GiaoDien.VANG_NHAT, GiaoDien.VANG);
        } else {
            GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 5, 0x34508E, 0x22396E);
        }
        Font f = chon ? Font.blackFont : Font.borderFont;
        String s = GiaoDien.catChu(f, chu, w - 6);
        f.drawString(g, s, x + w / 2, y + (h - f.getHeight()) / 2 + 1, Font.CENTER);
        VungCham.them(g, x, y, w, h, viec, ten);
    }

    /** Thẻ trắng (khối nội dung). */
    public static void the(Graphics g, int x, int y, int w, int h) {
        GiaoDien.khoi(g, x, y, w, h, 7, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 6, NEN_THE, NEN_THE_DUOI);
    }

    /** kiểu nút: 0 phụ (xanh), 1 chính (đỏ), 2 xác nhận (xanh lá), 3 tắt (xám) */
    public static final int PHU = 0, CHINH = 1, XAC_NHAN = 2, TAT = 3, TRUNG = 4;
    /** chữ nút trung tính (nền xám sáng, chữ tối) */

    /** Nút viên to, chữ giữa. */
    public static void nut(Graphics g, int x, int y, int w, int h, String chu, int kieu, Object viec, String ten) {
        int tren, duoi;
        switch (kieu) {
            case CHINH:
                tren = GiaoDien.DO_SANG;
                duoi = GiaoDien.DO_TOI;
                break;
            case XAC_NHAN:
                tren = 0x6CD86A;
                duoi = 0x2E8E3E;
                break;
            case TAT:
                tren = 0xB4BDCA;
                duoi = 0x7A8496;
                break;
            case TRUNG:
                tren = 0xFFFFFF;
                duoi = 0xD9DFEA;
                break;
            default:
                tren = GiaoDien.XANH_SANG;
                duoi = GiaoDien.XANH;
                break;
        }
        int r = Math.min(h / 2, 10);
        GiaoDien.khoi(g, x, y + 1, w, h, r, 0x0A0F2C);
        GiaoDien.khoi(g, x, y, w, h, r, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, r - 1, tren, duoi);
        g.setColor(0xFFFFFF);
        g.fillRect(x + r, y + 2, w - 2 * r, 1);
        Font f = kieu == TRUNG ? Font.blackFont : Font.borderFont;
        String s = GiaoDien.catChu(f, chu, w - 8);
        f.drawString(g, s, x + w / 2, y + (h - f.getHeight()) / 2 + 1, Font.CENTER);
        if (kieu != TAT) {
            dangKy(g, x, y, w, h, viec, ten);
        }
    }

    /** Nút lùi (mũi tên trái vẽ tay) cỡ s. */
    public static void nutLui(Graphics g, int x, int y, int s, Object viec) {
        nut(g, x, y, s, s, "", PHU, viec, "lui");
        int cx = x + s / 2, cy = y + s / 2, a = Math.max(4, s / 5);
        g.setColor(GiaoDien.TRANG);
        g.fillTriangle(cx - a, cy, cx + a / 2 + 1, cy - a, cx + a / 2 + 1, cy + a);
    }

    /** Cột cuộn (hai nút lên / xuống) bề ngang w ở (x, y, h). */
    public static void cotCuon(Graphics g, int x, int y, int w, int h, Object len, Object xuong) {
        int k = cach();
        int hc = (h - k) / 2, mx = x + w / 2;
        nut(g, x, y, w, hc, "", PHU, len, "cuon-len");
        nut(g, x, y + hc + k, w, hc, "", PHU, xuong, "cuon-xuong");
        g.setColor(GiaoDien.TRANG);
        g.fillTriangle(mx - 5, y + hc / 2 + 3, mx + 5, y + hc / 2 + 3, mx, y + hc / 2 - 3);
        int yd = y + hc + k + hc / 2;
        g.fillTriangle(mx - 5, yd - 3, mx + 5, yd - 3, mx, yd + 3);
    }

    /** Hàng danh sách (nền chẵn / lẻ, đang chọn viền vàng); đăng ký chạm cả hàng. */
    public static void hang(Graphics g, int x, int y, int w, int h, int i, boolean chon, Object viec, String ten) {
        GiaoDien.khoi(g, x, y, w, h, 6, chon ? GiaoDien.VANG : 0xC9D2E2);
        GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, 5, chon ? 0xFFF6C8 : (i % 2 == 0 ? GiaoDien.HANG_CHAN : GiaoDien.HANG_LE));
        dangKy(g, x, y, w, h, viec, ten);
    }

    /** Trạng thái trống: biểu tượng Poké Ball mờ + dòng chính + dòng gợi ý (chữ tự xuống dòng). */
    public static void trong(Graphics g, int x, int y, int w, int h, String chinh, String phu) {
        int cy = y + h / 2 - 22;
        GiaoDien.pokeBall(g, x + w / 2, cy, 12, 0);
        int yy = cy + 18;
        yy = doan(g, Font.blackFont, chinh, x + 6, yy, w - 12, true);
        if (phu != null) {
            doan(g, Font.arialFont, phu, x + 6, yy + 2, w - 12, true);
        }
    }

    /** Đoạn chữ tự xuống dòng theo bề ngang w; trả y sau đoạn. */
    public static int doan(Graphics g, Font f, String s, int x, int y, int w, boolean giua) {
        if (s == null) {
            return y;
        }
        int lh = f.getHeight() + 1;
        String con = s;
        int dem = 0;
        while (con.length() > 0 && dem < 8) {
            int n = con.length();
            while (n > 1 && f.getWidth(con.substring(0, n)) > w) {
                int sp = con.lastIndexOf(' ', n - 1);
                n = sp > 0 ? sp : n - 1;
            }
            String dong = con.substring(0, n).trim();
            f.drawString(g, dong, giua ? x + w / 2 : x, y, giua ? Font.CENTER : 0);
            y += lh;
            con = con.substring(n).trim();
            dem++;
        }
        return y;
    }

    /** Ô đồ (vuông bo góc) cỡ s, đăng ký chạm. */
    public static void oDo(Graphics g, int x, int y, int s, boolean co, boolean chon, Object viec, String ten) {
        GiaoDien.khoi(g, x, y + 1, s, s, 6, 0x0A0F2C);
        GiaoDien.khoi(g, x, y, s, s, 6, chon ? GiaoDien.VANG : GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 2, y + 2, s - 4, s - 4, 4, co ? 0x3B5AA8 : 0x26386E, co ? 0x22397A : 0x1A2658);
        if (!co) {
            g.setColor(0x4C64A8);
            int c = s / 2;
            g.fillRect(x + c - 4, y + c, 9, 1);
            g.fillRect(x + c, y + c - 4, 1, 9);
        }
        dangKy(g, x, y, s, s, viec, ten);
    }

    /** Icon vật phẩm phóng z quanh tâm (cx, cy) nếu nền phóng được; không thì cỡ thường. */
    public static void icon(Graphics g, short id, int cx, int cy, float z) {
        if (z > 1.05f && PhongVe.co()) {
            int tx = g.getTranslateX(), ty = g.getTranslateY();
            int kx = g.getClipX(), ky = g.getClipY(), kw = g.getClipWidth(), kh = g.getClipHeight();
            PhongVe.bo.bat(g, z);
            GameData.paintIcon(g, id, (int) ((cx + tx) / z), (int) ((cy + ty) / z));
            PhongVe.bo.tat(g);
            g.translate(tx, ty);
            g.setClip(kx, ky, kw, kh);   // giữ vùng cắt của nơi gọi (hàng danh sách cuộn)
            return;
        }
        GameData.paintIcon(g, id, cx, cy);
    }

    /** Thanh giá trị (HP / PP / EXP) có chữ "a/b" giữa. */
    public static void thanh(Graphics g, int x, int y, int w, int h, long gt, long max, int loai, String chu) {
        GiaoDien.thanh(g, x, y, w, h, gt, max, loai);
        if (chu != null) {
            Font.borderFont.drawString(g, chu, x + w / 2, y + (h - Font.borderFont.getHeight()) / 2 + 1, Font.CENTER);
        }
    }
}
