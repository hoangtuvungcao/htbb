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

    /**
     * "Vân tay" rẻ của danh sách (0.9.5): đổi khi phần tử / thứ tự đổi - danh sách đã lọc của bảng chỉ dựng lại khi
     * dữ liệu thật sự đổi (gói -105 / túi đồ), không dựng Vector mới mỗi khung vẽ. Không cấp phát.
     */
    public static int vanTay(java.util.Vector v) {
        if (v == null) {
            return 0;
        }
        int h = v.size();
        for (int i = 0; i < v.size(); i++) {
            h = h * 31 + System.identityHashCode(v.elementAt(i));
        }
        return h;
    }

    /** Vân tay mảng số (thuốc, vật phẩm nhiệm vụ). */
    public static int vanTay(int[] a) {
        if (a == null) {
            return 0;
        }
        int h = a.length;
        for (int i = 0; i < a.length; i++) {
            h = h * 31 + a[i];
        }
        return h;
    }

    public static int vanTay(short[] a) {
        if (a == null) {
            return 0;
        }
        int h = a.length;
        for (int i = 0; i < a.length; i++) {
            h = h * 31 + a[i];
        }
        return h;
    }

    /** Cỡ chạm tối thiểu (px logic) cho thiết kế cỡ d. */
    public static int co(int d) {
        return Math.max(VungCham.MIN, d);
    }

    /** Khoảng cách tối thiểu giữa hai điều khiển (px logic). */
    public static int cach() {
        return Math.max(VungCham.CACH, Math.max(4, VungCham.MIN / 5));
    }

    /** Cao đầu bảng (0.9.5: đầu mảnh - ChuDe). */
    public static int caoDau() {
        return ChuDe.caoDau();
    }

    /** Cao thanh tab (điều khiển phân đoạn). */
    public static int caoTab() {
        return co(22);
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
        int h = co(22);
        int w = Math.max(h, ChuDe.rong(chu, ChuDe.NHAN) + 18);
        if (chon) {
            GiaoDien.khoiDoc(g, x, y, w, h, h / 2, GiaoDien.pha(ChuDe.DO, 0xFFFFFF, 1, 6), ChuDe.DO);
        } else {
            GiaoDien.khoi(g, x, y, w, h, h / 2, ChuDe.KE);
            GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, h / 2 - 1, ChuDe.NEN);
        }
        ChuDe.chuGiua(g, chu, x + w / 2, y, h, ChuDe.NHAN, chon ? 0xFFFFFF : ChuDe.CHU_PHU, 2);
        VungCham.them(g, x, y, w, h, viec, ten);
        return w;
    }

    /** Hàng chip lọc (tự xuống dòng khi hết bề ngang): trả cao đã dùng. chon = chip đang chọn; viec[i] chạm chip i. */
    public static int hangChip(Graphics g, int x, int y, int w, String[] chu, int chon, Object[] viec) {
        int k = cach();
        int hc = co(22);
        int xx = x, yy = y;
        for (int i = 0; i < chu.length; i++) {
            int wc = Math.max(hc, ChuDe.rong(chu[i], ChuDe.NHAN) + 18);
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

    /** Phủ mờ nền game phía sau (0.9.5: ~45 %, thế giới vẫn thấy). */
    public static void phuNen(Graphics g) {
        ChuDe.phuMan(g);
    }

    /**
     * Khung bảng (0.9.5): thẻ SÁNG trong vùng an toàn, đầu mảnh đỏ (Poké Ball + tiêu đề chữ giao diện + X vẽ nét ở
     * góc phải), tab = điều khiển phân đoạn (segmented) ngay dưới đầu. Trả {x, y, w, h} vùng nội dung.
     */
    public static int[] khung(Graphics g, String tieuDe, String[] tabs, int tabChon, Object[] viecTab, Object dong) {
        Canvas.resetTrans(g);
        return khung(g, trai(), tren(), phai() - trai(), duoi() - tren(), tieuDe, tabs, tabChon, viecTab, dong);
    }

    /** Khung bảng ở (x, y, w, h) bất kỳ (bảng gọn: Menu, hộp thông báo). */
    public static int[] khung(Graphics g, int x, int y, int w, int h, String tieuDe, String[] tabs, int tabChon, Object[] viecTab, Object dong) {
        Canvas.resetTrans(g);
        ChuDe.the(g, x, y, w, h, ChuDe.BO);
        int hd = ChuDe.caoDau();
        ChuDe.dau(g, x + 1, y + 1, w - 2, hd, tieuDe, ChuDe.DO);
        Font.nenSang = true;
        if (dong != null) {
            nutDong(g, x + w - 1 - hd, y + 1, hd, dong, true);
        }
        int yNoi = y + 1 + hd + ChuDe.DEM;
        if (tabs != null && tabs.length > 0) {
            int ht = caoTab();
            phanDoan(g, x + ChuDe.DEM, yNoi, w - 2 * ChuDe.DEM, ht, tabs, tabChon, viecTab);
            yNoi += ht + cach();
        }
        return new int[]{x + ChuDe.DEM, yNoi, w - 2 * ChuDe.DEM, y + h - ChuDe.DEM - yNoi};
    }

    /** Điều khiển phân đoạn (tab): rãnh xám nhạt, đoạn chọn = thẻ trắng nổi, chữ đậm đỏ. */
    public static void phanDoan(Graphics g, int x, int y, int w, int h, String[] chu, int chon, Object[] viec) {
        int n = chu.length;
        GiaoDien.khoi(g, x, y, w, h, Math.min(ChuDe.BO_NUT, h / 2), ChuDe.NEN_PHU_TOI);
        int pad = 2;
        int wt = (w - 2 * pad) / n;
        for (int i = 0; i < n; i++) {
            int xt = x + pad + i * wt;
            int ww = i == n - 1 ? x + w - pad - xt : wt;
            if (i == chon) {
                GiaoDien.khoi(g, xt, y + pad + 1, ww, h - 2 * pad, ChuDe.BO_NUT - 2, ChuDe.BONG);
                GiaoDien.khoi(g, xt, y + pad, ww, h - 2 * pad, ChuDe.BO_NUT - 2, ChuDe.NEN);
            } else if (i > 0 && i - 1 != chon) {
                g.setColor(ChuDe.KE);
                g.fillRect(xt, y + h / 4, 1, h / 2);
            }
            ChuDe.chuGiua(g, ChuDe.cat(chu[i], ChuDe.NHAN, ww - 6), xt + ww / 2, y, h, ChuDe.NHAN, i == chon ? ChuDe.DO_TOI : ChuDe.CHU_PHU, 2);
            // đoạn liền nhau khi vẽ; vùng chạm chừa khe cach() giữa hai đoạn (không bấm nhầm đoạn bên cạnh)
            int kh = cach();
            int l = i == 0 ? xt : xt + (kh + 1) / 2, r = i == n - 1 ? xt + ww : xt + ww - kh / 2;
            VungCham.them(g, l, y, r - l, h, viec == null ? null : viec[i], "tab-" + i);
        }
    }

    /** Nút đóng (X vẽ nét 12-14 px logic, vùng chạm cỡ s). Mặc định trên đầu bảng đỏ (X trắng). */
    public static void nutDong(Graphics g, int x, int y, int s, Object viec) {
        nutDong(g, x, y, s, viec, false);
    }

    public static void nutDong(Graphics g, int x, int y, int s, Object viec, boolean trenDau) {
        int c = s / 2;
        int r = Math.min(c - 2, ChuDe.dt() ? 11 : 9);
        boolean an = viec != null && dangAn(g, x, y, s, s);
        ChuUI b = ChuUI.bo;
        if (trenDau) {
            KieuO.phu(g, x + c - r, y + c - r, 2 * r, 2 * r, an ? 0x60FFFFFF : 0x30FFFFFF);
        } else if (b == null || !b.hinh(g, AnhUI.NUT[an ? AnhUI.NUT_TRON_NHAN : AnhUI.NUT_TRON], x + c - r, y + c - r,
                2 * r << 16 | 2 * r, ChuDe.NEN_PHU_TOI, 0x9AA6BA)) {
            GiaoDien.khoi(g, x + c - r, y + c - r, 2 * r, 2 * r, r, ChuDe.NEN_PHU_TOI);
        }
        ChuDe.dauX(g, x + c, y + c, Math.max(3, r * 9 / 20), trenDau ? 0xFFFFFF : ChuDe.CHU);
        VungCham.them(g, x, y, s, s, viec, "dong");
    }

    /** Tab đơn (giữ cho màn cũ): đoạn của điều khiển phân đoạn. */
    public static void tab(Graphics g, int x, int y, int w, int h, String chu, boolean chon, Object viec, String ten) {
        if (chon) {
            GiaoDien.khoi(g, x, y + 1, w, h, ChuDe.BO_NUT, ChuDe.BONG);
            GiaoDien.khoi(g, x, y, w, h, ChuDe.BO_NUT, ChuDe.NEN);
        } else {
            GiaoDien.khoi(g, x, y, w, h, ChuDe.BO_NUT, ChuDe.NEN_PHU_TOI);
        }
        ChuDe.chuGiua(g, ChuDe.cat(chu, ChuDe.NHAN, w - 6), x + w / 2, y, h, ChuDe.NHAN, chon ? ChuDe.DO_TOI : ChuDe.CHU_PHU, 2);
        VungCham.them(g, x, y, w, h, viec, ten);
    }

    /** Thẻ nội dung (khối con sáng, viền kẻ nhạt). */
    public static void the(Graphics g, int x, int y, int w, int h) {
        GiaoDien.khoi(g, x, y, w, h, ChuDe.BO_O, ChuDe.KE);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, ChuDe.BO_O - 1, ChuDe.NEN, ChuDe.NEN_DUOI);
        Font.nenSang = true;
    }

    /** kiểu nút: 0 phụ (xanh), 1 chính (đỏ), 2 xác nhận (xanh lá), 3 tắt (xám), 4 trung tính (trắng viền) */
    public static final int PHU = 0, CHINH = 1, XAC_NHAN = 2, TAT = 3, TRUNG = 4;

    /** Màu thân nút theo kiểu. */
    static int mauNut(int kieu) {
        switch (kieu) {
            case CHINH:
                return ChuDe.DO;
            case XAC_NHAN:
                return 0xE8504A;   // 0.9.5: nút chính = đỏ san hô thương hiệu (không xanh lá neon)
            case TAT:
                return 0xB9C1CF;
            case TRUNG:
                return ChuDe.NEN;
            default:
                return ChuDe.XANH;
        }
    }

    /** Nút phẳng bo góc, chữ nhãn giữa. */
    public static void nut(Graphics g, int x, int y, int w, int h, String chu, int kieu, Object viec, String ten) {
        int mau = mauNut(kieu);
        int r = Math.min(h / 2, ChuDe.BO_NUT);
        boolean an = viec != null && kieu != TAT && dangAn(g, x, y, w, h);
        // PC / APK: da nút PNG 9-slice (AnhUI, thường / nhấn / tắt); J2ME: khối vẽ
        ChuUI b = ChuUI.bo;
        int da = kieu == TAT ? AnhUI.NUT_TAT : an ? AnhUI.NUT_NHAN : AnhUI.NUT_THUONG;
        if (b != null && w > 0 && h > 0 && w < 4096 && h < 65536
                && b.hinh(g, AnhUI.NUT[da], x, y, w << 16 | h, mau, kieu == TRUNG ? 0x9AA6BA : AnhUI.toi(mau))) {
            // đã vẽ
        } else if (kieu == TRUNG) {
            GiaoDien.khoi(g, x, y, w, h, r, ChuDe.KE);
            GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, r - 1, an ? ChuDe.NEN_PHU : ChuDe.NEN, an ? ChuDe.NEN_PHU_TOI : ChuDe.NEN_DUOI);
        } else {
            GiaoDien.khoi(g, x, y + 1, w, h, r, ChuDe.BONG);
            int tren = an ? mau : GiaoDien.pha(mau, 0xFFFFFF, 1, 6);
            int duoi = an ? GiaoDien.pha(mau, 0, 1, 6) : mau;
            GiaoDien.khoiDoc(g, x, y, w, h, r, tren, duoi);
        }
        if (chu != null && chu.length() > 0) {
            int mc = kieu == TRUNG ? ChuDe.CHU : 0xFFFFFF;
            ChuDe.chuGiua(g, ChuDe.cat(chu, ChuDe.NHAN, w - 8), x + w / 2, y + (an ? 1 : 0), h, ChuDe.NHAN, mc, 2);
        }
        if (kieu != TAT) {
            dangKy(g, x, y, w, h, viec, ten);
        }
    }

    /** Ngón tay đang giữ trong ô (x, y, w, h) (toạ độ sau translate). */
    static boolean dangAn(Graphics g, int x, int y, int w, int h) {
        return Canvas.isPointerDown && Canvas.px >= x + g.getTranslateX() && Canvas.px < x + g.getTranslateX() + w
                && Canvas.py >= y + g.getTranslateY() && Canvas.py < y + g.getTranslateY() + h;
    }

    /** Nút lùi (mũi tên trái vẽ nét) cỡ s. */
    public static void nutLui(Graphics g, int x, int y, int s, Object viec) {
        nut(g, x, y, s, s, "", TRUNG, viec, "lui");
        int cx = x + s / 2, cy = y + s / 2, a = Math.max(4, s / 5);
        ChuUI b = ChuUI.bo;
        if (b == null || !b.net(g, cx, cy, a, ChuDe.CHU, 1)) {
            g.setColor(ChuDe.CHU);
            g.fillTriangle(cx - a, cy, cx + a / 2 + 1, cy - a, cx + a / 2 + 1, cy + a);
        }
    }

    /** Cột cuộn (hai nút lên / xuống) bề ngang w ở (x, y, h). */
    public static void cotCuon(Graphics g, int x, int y, int w, int h, Object len, Object xuong) {
        int k = cach();
        int hc = (h - k) / 2, mx = x + w / 2;
        nut(g, x, y, w, hc, "", TRUNG, len, "cuon-len");
        nut(g, x, y + hc + k, w, hc, "", TRUNG, xuong, "cuon-xuong");
        g.setColor(ChuDe.CHU_PHU);
        g.fillTriangle(mx - 5, y + hc / 2 + 3, mx + 5, y + hc / 2 + 3, mx, y + hc / 2 - 3);
        int yd = y + hc + k + hc / 2;
        g.fillTriangle(mx - 5, yd - 3, mx + 5, yd - 3, mx, yd + 3);
    }

    /** Hàng danh sách (thẻ trắng viền kẻ; đang chọn vàng nhạt viền cam); đăng ký chạm cả hàng. */
    public static void hang(Graphics g, int x, int y, int w, int h, int i, boolean chon, Object viec, String ten) {
        GiaoDien.khoi(g, x, y, w, h, ChuDe.BO_O, chon ? ChuDe.CAM : ChuDe.KE);
        GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, ChuDe.BO_O - 1, chon ? ChuDe.VANG_NHAT : (i % 2 == 0 ? ChuDe.NEN : 0xF9FAFD));
        dangKy(g, x, y, w, h, viec, ten);
    }

    /** Trạng thái trống: Poké Ball + dòng chính (đậm) + dòng gợi ý (chữ tự xuống dòng). */
    public static void trong(Graphics g, int x, int y, int w, int h, String chinh, String phu) {
        int cy = y + h / 2 - 22;
        GiaoDien.pokeBall(g, x + w / 2, cy, 12, 0);
        int yy = cy + 18;
        yy = doan(g, ChuDe.NHAN, chinh, x + 6, yy, w - 12, true, ChuDe.CHU);
        if (phu != null) {
            doan(g, ChuDe.PHU, phu, x + 6, yy + 2, w - 12, true, ChuDe.CHU_PHU);
        }
    }

    /** Đoạn chữ giao diện tự xuống dòng; trả y sau đoạn. */
    public static int doan(Graphics g, int vai, String s, int x, int y, int w, boolean giua, int mau) {
        if (s == null) {
            return y;
        }
        String[] d = ChuDe.ngat(s, vai, w, 8);
        int lh = ChuDe.cao(vai);
        for (int i = 0; i < d.length; i++) {
            ChuDe.chu(g, d[i], giua ? x + w / 2 : x, y, vai, mau, giua ? 2 : 0);
            y += lh;
        }
        return y;
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

    /** Ô đồ (vuông bo góc, nền xám nhạt) cỡ s, đăng ký chạm. */
    public static void oDo(Graphics g, int x, int y, int s, boolean co, boolean chon, Object viec, String ten) {
        GiaoDien.khoi(g, x, y, s, s, ChuDe.BO_O, chon ? ChuDe.CAM : ChuDe.KE);
        GiaoDien.khoiDoc(g, x + 1, y + 1, s - 2, s - 2, ChuDe.BO_O - 1, co ? ChuDe.NEN : ChuDe.NEN_PHU, co ? ChuDe.NEN_PHU : ChuDe.NEN_PHU_TOI);
        dangKy(g, x, y, s, s, viec, ten);
    }

    /** Icon vật phẩm thật (IconVatPham: đĩa / máy chủ, thiếu thì biểu tượng loại) cỡ ~22*z quanh tâm (cx, cy). */
    public static void icon(Graphics g, short id, int cx, int cy, float z) {
        IconVatPham.ve(g, id, cx, cy, (int) (22 * z), IconVatPham.DP_DO);
    }

    /** Vẽ ảnh icon đã nạp, phóng z quanh tâm (cx, cy) nếu nền phóng được; không thì cỡ thường. */
    static void veAnhIcon(Graphics g, short id, int cx, int cy, float z) {
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
            ChuDe.chuGiua(g, chu, x + w / 2, y, h, ChuDe.SO, ChuDe.CHU, 2);
        }
    }
}
