package game.pokemon;

import game.render.Canvas;
import game.render.screen.Font;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

/**
 * BẢNG CHUNG (0.9.3, ưu tiên điện thoại; 0.9.6 kiểu Pokémon GO): MỘT bộ thành phần cho mọi bảng trong game (Nhân vật,
 * Hành trang, Menu, Kỹ năng, Thú...) để tấm, tab, nút đóng, hàng danh sách, nút bấm giống nhau ở mọi nơi và đủ to.
 *
 * - TẤM canh giữa vùng an toàn (tai thỏ / bo góc - nền đặt anTrai..anDuoi theo px logic), rộng ~78 % (không phủ mép
 *   tới mép), cao theo nội dung (tối đa vùng an toàn) - {@link #oTam}. Nền dải teal rất nhạt (hoặc màu ngữ cảnh
 *   {@link #mauTam}), KHÔNG dải đầu màu: tiêu đề tròn đậm lớn canh giữa, nút tròn trắng X góc trái trên (thẻ gọn: góc
 *   phải trên). Tab = chip phân đoạn mềm ngay dưới tiêu đề, chạm một lần.
 * - Nút = viên dải teal (chính) / trắng (phụ) / đỏ (nguy hiểm) / xám (tắt); nhấn co 0,96.
 * - Thẻ / hàng = trắng bo tròn, bóng mềm, không viền tối; hàng chọn = viền teal + nền teal nhạt.
 * - Thanh trượt, công tắc, chip lọc, chip chờ (vòng quay nhỏ) cho bảng cài đặt.
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

    /** Chip lọc (viên): chọn = teal đặc chữ trắng, thường = trắng viền kẻ chữ phụ - trả bề ngang. */
    public static int chip(Graphics g, int x, int y, String chu, boolean chon, Object viec, String ten) {
        int h = co(22);
        int w = Math.max(h, ChuDe.rong(chu, ChuDe.NHAN) + 20);
        if (chon) {
            GiaoDien.khoi(g, x, y + 1, w, h, h / 2, ChuDe.BONG);
            GiaoDien.khoiDoc(g, x, y, w, h, h / 2, ChuDe.CHINH_SANG, ChuDe.CHINH);
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
        boolean[] cham = chamChip;
        chamChip = null;
        for (int i = 0; i < chu.length; i++) {
            int wc = Math.max(hc, ChuDe.rong(chu[i], ChuDe.NHAN) + 20);
            if (xx + wc > x + w && xx > x) {
                xx = x;
                yy += hc + k;
            }
            int dw = chip(g, xx, yy, chu[i], i == chon, viec == null ? null : viec[i], "chip-" + i);
            if (cham != null && i < cham.length && cham[i]) {
                ChuDe.cham(g, xx + dw - 4, yy + 4);
            }
            xx += dw + k;
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
     * Màu ngữ cảnh của tấm kế tiếp (Pokémon: màu hệ) - đặt NGAY trước {@link #khung}, khung tự trả về -1 (teal mặc định).
     */
    public static int mauTam = -1;

    /** Lúc mở tấm (mờ dần nền 140 ms khi tấm mới hiện). */
    private static long lucMoTam, lucVeTam;

    /** Khung tấm vừa vẽ {x, y, w, h} (kiểm thử: canh giữa / trong giới hạn cỡ theo tỉ lệ màn). */
    public static final int[] VUA_TAM = new int[4];
    /** số tấm đã vẽ (kiểm thử đọc VUA_TAM khi đổi) */
    public static int demTam;

    /** Bề ngang tối đa của tấm: ~78 % vùng an toàn (màn ngang rộng), hẹp hơn thì gần trọn. */
    public static int rongTamToiDa() {
        int sw = phai() - trai();
        int w = sw * 78 / 100;
        int toiThieu = Math.min(sw, ChuDe.dt() ? 420 : 380);
        return Math.max(toiThieu, w);
    }

    /** Cao tối đa của tấm (vùng an toàn trừ lề nhỏ). */
    public static int caoTamToiDa() {
        return duoi() - tren();
    }

    /**
     * Ô tấm canh giữa vùng an toàn: wMuon / hMuon = cỡ nội dung muốn (<= 0 = tối đa), kẹp trong [toi thieu, toi da].
     * Trả {x, y, w, h}.
     */
    public static int[] oTam(int wMuon, int hMuon) {
        int wMax = rongTamToiDa(), hMax = caoTamToiDa();
        int w = wMuon <= 0 ? wMax : Math.max(Math.min(wMax, ChuDe.dt() ? 300 : 260), Math.min(wMax, wMuon));
        int h = hMuon <= 0 ? hMax : Math.max(Math.min(hMax, 140), Math.min(hMax, hMuon));
        int x = trai() + (phai() - trai() - w) / 2;
        int y = tren() + (duoi() - tren() - h) / 2;
        return new int[]{x, y, w, h};
    }

    /** Cao phần đầu tấm (tiêu đề + tab nếu có) - để màn tính cao tấm theo nội dung. */
    public static int caoDauTam(boolean coTab) {
        return 3 + caoDau() + Math.max(ChuDe.DEM - 2, cach()) + (coTab ? caoTab() + cach() : 0) + ChuDe.DEM;
    }

    /**
     * TẤM (0.9.6 kiểu Pokémon GO) cỡ tối đa canh giữa: nền dải nhạt, tiêu đề tròn đậm lớn canh giữa, nút tròn X trái
     * trên, tab chip phân đoạn dưới tiêu đề. Trả {x, y, w, h} vùng nội dung.
     */
    public static int[] khung(Graphics g, String tieuDe, String[] tabs, int tabChon, Object[] viecTab, Object dong) {
        return khungTam(g, 0, 0, tieuDe, tabs, tabChon, viecTab, dong);
    }

    /** Tấm cỡ theo nội dung (wMuon x hMuon cả tấm; <= 0 = tối đa), canh giữa vùng an toàn. */
    public static int[] khungTam(Graphics g, int wMuon, int hMuon, String tieuDe, String[] tabs, int tabChon, Object[] viecTab, Object dong) {
        Canvas.resetTrans(g);
        int[] o = oTam(wMuon, hMuon);
        dangTam = true;
        try {
            return khung(g, o[0], o[1], o[2], o[3], tieuDe, tabs, tabChon, viecTab, dong);
        } finally {
            dangTam = false;
        }
    }

    private static boolean dangTam;

    /** Tấm (khungTam) hoặc thẻ gọn ở (x, y, w, h) bất kỳ (thẻ gọn: nền trắng, X góc phải). */
    public static int[] khung(Graphics g, int x, int y, int w, int h, String tieuDe, String[] tabs, int tabChon, Object[] viecTab, Object dong) {
        Canvas.resetTrans(g);
        int mau = mauTam;
        mauTam = -1;
        boolean tam = dangTam;
        khungMoTam();
        if (tam) {
            VUA_TAM[0] = x;
            VUA_TAM[1] = y;
            VUA_TAM[2] = w;
            VUA_TAM[3] = h;
            demTam++;
            int tren = mau < 0 ? ChuDe.TAM_TREN : GiaoDien.pha(mau, 0xFFFFFF, 6, 7);
            int duoi = mau < 0 ? ChuDe.TAM_DUOI : GiaoDien.pha(mau, 0xFFFFFF, 15, 16);
            GiaoDien.khoi(g, x, y + 2, w, h, ChuDe.BO, ChuDe.BONG);
            GiaoDien.khoiDoc(g, x, y, w, h, ChuDe.BO, tren, duoi);
        } else {
            GiaoDien.khoi(g, x, y + 2, w, h, ChuDe.BO, ChuDe.BONG);
            GiaoDien.khoi(g, x, y, w, h, ChuDe.BO, ChuDe.NEN);
        }
        Font.nenSang = true;
        int hd = caoDau();
        int vai = tam ? ChuDe.TIEU_DE_TO : ChuDe.TIEU_DE;
        if (tieuDe != null) {
            ChuDe.chuGiua(g, ChuDe.cat(tieuDe, vai, w - 2 * hd - 24), x + w / 2, y + 3, hd, vai, ChuDe.CHU, 2);
        }
        if (dong != null) {
            nutDong(g, tam ? x + ChuDe.DEM - 2 : x + w - hd - ChuDe.DEM + 2, y + 3, hd, dong);
        }
        int yNoi = y + 3 + hd + Math.max(ChuDe.DEM - 2, cach());
        if (tabs != null && tabs.length > 0) {
            int ht = caoTab();
            int wt = Math.min(w - 2 * ChuDe.DEM, tabs.length * (ChuDe.dt() ? 150 : 110));
            phanDoan(g, x + (w - wt) / 2, yNoi, wt, ht, tabs, tabChon, viecTab);
            yNoi += ht + cach();
        }
        return new int[]{x + ChuDe.DEM, yNoi, w - 2 * ChuDe.DEM, y + h - ChuDe.DEM - yNoi};
    }

    /** Ghi một khung vẽ tấm (không vẽ > 300 ms = tấm mới mở: đặt lại đồng hồ hiệu ứng mở). */
    public static void khungMoTam() {
        long now = System.currentTimeMillis();
        if (now - lucVeTam > 300) {
            lucMoTam = now;
        }
        lucVeTam = now;
    }

    /**
     * Hiệu ứng mở tấm (0.9.6): gọi SAU khi vẽ nội dung - lớp trắng mờ dần trên tấm vừa mở (140 ms, "fade in" nhẹ).
     */
    public static void moDan(Graphics g) {
        int t = tuoiTam();
        if (t >= 140) {
            return;
        }
        Canvas.resetTrans(g);
        int a = 0xB0 * (140 - t) / 140;
        KieuO.phu(g, VUA_TAM[0], VUA_TAM[1], VUA_TAM[2], VUA_TAM[3], a << 24 | 0xFFFFFF);
    }

    /** Thời gian (ms, 0..140) từ lúc tấm hiện - hiệu ứng mở. */
    public static int tuoiTam() {
        return (int) Math.min(140, System.currentTimeMillis() - lucMoTam);
    }

    /** Chấm báo mới cho tab / chip kế tiếp (ChamMoi) - đặt ngay trước khung / hangChip, tự xoá sau khi vẽ. */
    public static boolean[] chamTab, chamChip;

    /** Chip phân đoạn mềm (0.9.6): rãnh viên xám xanh, đoạn chọn = viên trắng nổi (bóng mềm), chữ teal đậm. */
    public static void phanDoan(Graphics g, int x, int y, int w, int h, String[] chu, int chon, Object[] viec) {
        int n = chu.length;
        boolean[] cham = chamTab;
        chamTab = null;
        GiaoDien.khoi(g, x, y, w, h, h / 2, ChuDe.NEN_PHU_TOI);
        int pad = 2;
        int wt = (w - 2 * pad) / n;
        for (int i = 0; i < n; i++) {
            int xt = x + pad + i * wt;
            int ww = i == n - 1 ? x + w - pad - xt : wt;
            if (i == chon) {
                GiaoDien.khoi(g, xt, y + pad + 1, ww, h - 2 * pad, (h - 2 * pad) / 2, ChuDe.BONG);
                GiaoDien.khoi(g, xt, y + pad, ww, h - 2 * pad, (h - 2 * pad) / 2, ChuDe.NEN);
            }
            String ct = ChuDe.cat(chu[i], ChuDe.NHAN, ww - 14);
            ChuDe.chuGiua(g, ct, xt + ww / 2, y, h, i == chon ? ChuDe.SO : ChuDe.NHAN,
                    i == chon ? ChuDe.CHINH_TOI : ChuDe.CHU_PHU, 2);
            if (cham != null && i < cham.length && cham[i]) {
                ChuDe.cham(g, Math.min(xt + ww - 5, xt + ww / 2 + ChuDe.rong(ct, ChuDe.SO) / 2 + 5), y + h / 2 - 4);
            }
            // đoạn liền nhau khi vẽ; vùng chạm chừa khe cach() giữa hai đoạn (không bấm nhầm đoạn bên cạnh)
            int kh = cach();
            int l = i == 0 ? xt : xt + (kh + 1) / 2, r = i == n - 1 ? xt + ww : xt + ww - kh / 2;
            VungCham.them(g, l, y, r - l, h, viec == null ? null : viec[i], "tab-" + i);
        }
    }

    /** Nút đóng: đĩa tròn TRẮNG bóng mềm + X tối vẽ nét (vùng chạm cỡ s). */
    public static void nutDong(Graphics g, int x, int y, int s, Object viec) {
        nutDong(g, x, y, s, viec, false);
    }

    public static void nutDong(Graphics g, int x, int y, int s, Object viec, boolean trenDau) {
        int c = s / 2;
        int r = Math.min(c - 1, ChuDe.dt() ? 13 : 10);
        boolean an = viec != null && dangAn(g, x, y, s, s);
        if (an) {
            r = r * 24 / 25;
        }
        diaTrang(g, x + c, y + c, r, an);
        ChuDe.dauX(g, x + c, y + c, Math.max(3, r * 2 / 5), ChuDe.CHU);
        VungCham.them(g, x, y, s, s, viec, "dong");
    }

    /** Đĩa tròn trắng nổi (bóng mềm) tâm (cx, cy) bán kính r - nền nút tròn kiểu GO. */
    public static void diaTrang(Graphics g, int cx, int cy, int r, boolean an) {
        GiaoDien.khoi(g, cx - r, cy - r + 1, 2 * r, 2 * r, r, ChuDe.BONG);
        GiaoDien.khoiDoc(g, cx - r, cy - r, 2 * r, 2 * r, r, an ? ChuDe.NEN_PHU : ChuDe.NEN, an ? ChuDe.NEN_PHU_TOI : ChuDe.NEN_DUOI);
    }

    /**
     * NÚT TRÒN kiểu GO: đĩa trắng (hoặc đĩa màu khi dac) + icon BieuTuong màu, nhãn dưới (null = không); vùng chạm
     * phủ cả đĩa + nhãn. Nhấn co 0,96.
     */
    public static void nutTron(Graphics g, int cx, int cy, int r, int hinh, int mau, boolean dac, String nhan, Object viec, String ten) {
        boolean an = viec != null && dangAn(g, cx - r, cy - r, 2 * r, 2 * r);
        int rr = an ? r * 24 / 25 : r;
        if (dac) {
            GiaoDien.khoi(g, cx - rr - 2, cy - rr - 1, 2 * rr + 4, 2 * rr + 4, rr + 2, ChuDe.BONG);
            GiaoDien.khoi(g, cx - rr - 2, cy - rr - 2, 2 * rr + 4, 2 * rr + 4, rr + 2, ChuDe.NEN);
            GiaoDien.khoiDoc(g, cx - rr, cy - rr, 2 * rr, 2 * rr, rr, GiaoDien.pha(mau, 0xFFFFFF, 1, 4), mau);
        } else {
            diaTrang(g, cx, cy, rr, an);
        }
        int ic = rr;
        if (hinh == BieuTuong.POKEBALL) {
            GiaoDien.pokeBall(g, cx, cy, rr * 3 / 4, 0);
        } else {
            BieuTuong.ve(g, hinh, cx - ic / 2, cy - ic / 2, ic, dac ? 0xFFFFFF : mau, dac ? mau : ChuDe.NEN);
        }
        int hNhan = 0;
        if (nhan != null) {
            hNhan = ChuDe.cao(ChuDe.NHAN) + 2;
            ChuDe.chu(g, nhan, cx, cy + r + 3, ChuDe.NHAN, ChuDe.CHU, 2);
        }
        if (viec != null) {
            int wv = Math.max(2 * r, nhan == null ? 0 : ChuDe.rong(nhan, ChuDe.NHAN) + 4);
            VungCham.them(g, cx - wv / 2, cy - r, wv, 2 * r + (hNhan > 0 ? hNhan + 3 : 0), viec, ten);
        }
    }

    /** Tab đơn (giữ cho màn cũ): chip viên (chọn = trắng nổi chữ teal). */
    public static void tab(Graphics g, int x, int y, int w, int h, String chu, boolean chon, Object viec, String ten) {
        if (chon) {
            GiaoDien.khoi(g, x, y + 1, w, h, h / 2, ChuDe.BONG);
            GiaoDien.khoi(g, x, y, w, h, h / 2, ChuDe.NEN);
        } else {
            GiaoDien.khoi(g, x, y, w, h, h / 2, ChuDe.NEN_PHU_TOI);
        }
        ChuDe.chuGiua(g, ChuDe.cat(chu, ChuDe.NHAN, w - 6), x + w / 2, y, h, chon ? ChuDe.SO : ChuDe.NHAN, chon ? ChuDe.CHINH_TOI : ChuDe.CHU_PHU, 2);
        VungCham.them(g, x, y, w, h, viec, ten);
    }

    /** Thẻ nội dung: trắng bo tròn, bóng mềm, đường kẻ rất nhạt (không viền tối). */
    public static void the(Graphics g, int x, int y, int w, int h) {
        GiaoDien.khoi(g, x, y + 1, w, h, ChuDe.BO_THE, ChuDe.BONG);
        GiaoDien.khoi(g, x, y, w, h, ChuDe.BO_THE, ChuDe.KE);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, ChuDe.BO_THE - 1, ChuDe.NEN, ChuDe.NEN_DUOI);
        Font.nenSang = true;
    }

    /** kiểu nút: 0 phụ (trắng chữ teal), 1 chính (teal), 2 xác nhận (teal), 3 tắt (xám), 4 trung tính (trắng), 5 nguy hiểm (đỏ) */
    public static final int PHU = 0, CHINH = 1, XAC_NHAN = 2, TAT = 3, TRUNG = 4, NGUY = 5;

    /** Màu thân nút theo kiểu (đáy dải). */
    static int mauNut(int kieu) {
        switch (kieu) {
            case CHINH:
            case XAC_NHAN:
                return ChuDe.CHINH;
            case NGUY:
                return ChuDe.DO;
            case TAT:
                return 0xC3CCD4;
            default:
                return ChuDe.NEN;
        }
    }

    /** Nút có thân màu đặc (chữ trắng)? */
    static boolean dac(int kieu) {
        return kieu == CHINH || kieu == XAC_NHAN || kieu == NGUY || kieu == TAT;
    }

    /** Viên nút (0.9.6, dùng chung bảng + hộp thoại kiểu A): bo tròn hẳn, dải dọc, bóng mềm; nhấn co 0,96 quanh tâm. */
    public static void vienNut(Graphics g, int x, int y, int w, int h, String chu, int kieu, boolean an) {
        if (an) {
            int dx = Math.max(1, w / 50), dy = Math.max(1, h / 50);
            x += dx;
            y += dy;
            w -= 2 * dx;
            h -= 2 * dy;
        }
        int r = h / 2;
        int mau = mauNut(kieu);
        if (kieu != TAT) {
            GiaoDien.khoi(g, x, y + 1, w, h, r, ChuDe.BONG);
        }
        if (dac(kieu)) {
            int tren = kieu == CHINH || kieu == XAC_NHAN ? ChuDe.CHINH_SANG : kieu == NGUY ? 0xF7735F : 0xCDD5DC;
            if (an) {
                tren = mau;
                mau = GiaoDien.pha(mau, 0, 1, 7);
            }
            GiaoDien.khoiDoc(g, x, y, w, h, r, tren, mau);
        } else {
            GiaoDien.khoi(g, x, y, w, h, r, kieu == PHU ? GiaoDien.pha(ChuDe.CHINH, 0xFFFFFF, 3, 5) : ChuDe.KE);
            GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, r - 1, an ? ChuDe.NEN_PHU : ChuDe.NEN, an ? ChuDe.NEN_PHU_TOI : ChuDe.NEN_DUOI);
        }
        if (chu != null && chu.length() > 0) {
            int mc = dac(kieu) ? 0xFFFFFF : kieu == PHU ? ChuDe.CHINH_TOI : ChuDe.CHU;
            ChuDe.chuGiua(g, ChuDe.cat(chu, ChuDe.NHAN, w - 12), x + w / 2, y, h, dac(kieu) ? ChuDe.SO : ChuDe.NHAN, mc, 2);
        }
    }

    /** Nút viên, chữ nhãn giữa; đăng ký chạm (trừ nút tắt). */
    public static void nut(Graphics g, int x, int y, int w, int h, String chu, int kieu, Object viec, String ten) {
        boolean an = viec != null && kieu != TAT && dangAn(g, x, y, w, h);
        vienNut(g, x, y, w, h, chu, kieu, an);
        if (kieu != TAT) {
            dangKy(g, x, y, w, h, viec, ten);
        }
    }

    /** Ngón tay đang giữ trong ô (x, y, w, h) (toạ độ sau translate). */
    /** Ngón tay đang giữ trong đĩa tâm (cx, cy) bán kính r. */
    public static boolean dangAnTron(Graphics g, int cx, int cy, int r) {
        return dangAn(g, cx - r, cy - r, 2 * r, 2 * r);
    }

    public static boolean dangAn(Graphics g, int x, int y, int w, int h) {
        return Canvas.isPointerDown && Canvas.px >= x + g.getTranslateX() && Canvas.px < x + g.getTranslateX() + w
                && Canvas.py >= y + g.getTranslateY() && Canvas.py < y + g.getTranslateY() + h;
    }

    /** Nút lùi: đĩa tròn trắng + mũi tên trái vẽ nét, vùng chạm cỡ s. */
    public static void nutLui(Graphics g, int x, int y, int s, Object viec) {
        int c = s / 2;
        int r = Math.min(c - 1, ChuDe.dt() ? 13 : 10);
        boolean an = viec != null && dangAn(g, x, y, s, s);
        diaTrang(g, x + c, y + c, an ? r * 24 / 25 : r, an);
        int cx = x + c, cy = y + c, a = Math.max(3, r * 2 / 5);
        ChuUI b = ChuUI.bo;
        if (b == null || !b.net(g, cx, cy, a, ChuDe.CHU, 1)) {
            g.setColor(ChuDe.CHU);
            g.fillTriangle(cx - a, cy, cx + a / 2 + 1, cy - a, cx + a / 2 + 1, cy + a);
        }
        dangKy(g, x, y, s, s, viec, "lui");
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

    /** Hàng danh sách: thẻ trắng bo tròn (bóng mềm); đang chọn = viền teal + nền teal nhạt; đăng ký chạm cả hàng. */
    public static void hang(Graphics g, int x, int y, int w, int h, int i, boolean chon, Object viec, String ten) {
        boolean an = viec != null && dangAn(g, x, y, w, h);
        GiaoDien.khoi(g, x, y + 1, w, h, ChuDe.BO_O, ChuDe.BONG);
        GiaoDien.khoi(g, x, y, w, h, ChuDe.BO_O, chon ? ChuDe.CHINH : ChuDe.KE);
        GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, ChuDe.BO_O - 1, chon ? ChuDe.CHINH_NHAT : an ? ChuDe.NEN_PHU : ChuDe.NEN);
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

    /** Ô đồ (vuông bo tròn): có đồ = trắng nổi, trống = xám xanh nhạt; chọn = viền teal; đăng ký chạm. */
    public static void oDo(Graphics g, int x, int y, int s, boolean co, boolean chon, Object viec, String ten) {
        if (co || chon) {
            GiaoDien.khoi(g, x, y + 1, s, s, ChuDe.BO_O, ChuDe.BONG);
        }
        GiaoDien.khoi(g, x, y, s, s, ChuDe.BO_O, chon ? ChuDe.CHINH : co ? ChuDe.KE : ChuDe.NEN_PHU_TOI);
        GiaoDien.khoiDoc(g, x + 1, y + 1, s - 2, s - 2, ChuDe.BO_O - 1, co ? ChuDe.NEN : ChuDe.NEN_PHU, co ? ChuDe.NEN_DUOI : ChuDe.NEN_PHU);
        dangKy(g, x, y, s, s, viec, ten);
    }

    /** Ô đồ TRÒN (bảng Trang bị kiểu GO): đĩa trắng nổi / đĩa xám nhạt khi trống, vòng teal khi chọn. */
    public static void oTron(Graphics g, int cx, int cy, int r, boolean co, boolean chon, Object viec, String ten) {
        if (chon) {
            GiaoDien.khoi(g, cx - r - 2, cy - r - 2, 2 * r + 4, 2 * r + 4, r + 2, ChuDe.CHINH);
        }
        if (co) {
            diaTrang(g, cx, cy, r, false);
        } else {
            GiaoDien.khoi(g, cx - r, cy - r, 2 * r, 2 * r, r, ChuDe.NEN_PHU_TOI);
            GiaoDien.khoi(g, cx - r + 1, cy - r + 1, 2 * r - 2, 2 * r - 2, r - 1, ChuDe.NEN_PHU);
        }
        dangKy(g, cx - r, cy - r, 2 * r, 2 * r, viec, ten);
    }

    // ------------------------------------------------------------------ 0.9.6: điều khiển cài đặt (thanh trượt, công tắc, chip chờ)

    /**
     * Thanh trượt giá trị [min, max] bước buoc trong (x, y, w, h): rãnh mảnh, phần đầy teal, núm tròn trắng viền teal.
     * Giữ ngón / chuột kéo hiện giá trị mới ngay (trả giá trị đang hiện); cú chạm thả chạy viec (đọc
     * {@link #giaTriTruot}). tat = xám, không nhận chạm.
     */
    public static int thanhTruot(Graphics g, int x, int y, int w, int h, int gt, int min, int max, int buoc, boolean tat, Object viec, String ten) {
        int rn = nutTruot(h);
        int x0 = x + rn, x1 = x + w - rn;
        if (!tat && Canvas.isPointerDown && dangAn(g, x, y, w, h)) {
            gt = giaTriTruot(Canvas.px - g.getTranslateX(), x, w, h, min, max, buoc);
        } else if (!tat && System.currentTimeMillis() - keoLuc < 160) {
            int kx = keoX - g.getTranslateX(), ky = keoY - g.getTranslateY();
            if (kx >= x && kx < x + w && ky >= y && ky < y + h) {
                gt = giaTriTruot(kx, x, w, h, min, max, buoc);
            }
        }
        int cy = y + h / 2;
        int hr = Math.max(3, h / 7);
        GiaoDien.khoi(g, x0, cy - hr / 2, x1 - x0, hr, hr / 2, ChuDe.NEN_PHU_TOI);
        int xn = max > min ? x0 + (int) ((long) (gt - min) * (x1 - x0) / (max - min)) : x0;
        if (xn > x0) {
            GiaoDien.khoi(g, x0, cy - hr / 2, xn - x0 + hr / 2, hr, hr / 2, tat ? ChuDe.CHU_NHAT : ChuDe.CHINH);
        }
        GiaoDien.khoi(g, xn - rn, cy - rn + 1, 2 * rn, 2 * rn, rn, ChuDe.BONG);
        GiaoDien.khoi(g, xn - rn, cy - rn, 2 * rn, 2 * rn, rn, tat ? ChuDe.KE : ChuDe.CHINH);
        GiaoDien.khoi(g, xn - rn + 2, cy - rn + 2, 2 * rn - 4, 2 * rn - 4, rn - 2, ChuDe.NEN);
        if (!tat) {
            dangKy(g, x, y, w, h, viec, ten);
        }
        return gt;
    }

    /** Điểm giữ ngón gần nhất (màn tự xoá Canvas.isPointerDown mỗi nhịp để thế giới phía sau không nhận): thanh trượt đọc. */
    public static int keoX, keoY;
    public static long keoLuc;

    /** Ghi điểm đang giữ (gọi trước khi màn xoá Canvas.isPointerDown). */
    public static void ghiKeo() {
        if (Canvas.isPointerDown) {
            keoX = Canvas.px;
            keoY = Canvas.py;
            keoLuc = System.currentTimeMillis();
        }
    }

    static int nutTruot(int h) {
        return Math.max(5, Math.min(h / 2 - 1, ChuDe.dt() ? 9 : 7));
    }

    /** Giá trị thanh trượt (x, w, h như lúc vẽ, toạ độ cùng gốc) tại hoành độ px, làm tròn theo bước. */
    public static int giaTriTruot(int px, int x, int w, int h, int min, int max, int buoc) {
        int rn = nutTruot(h);
        int x0 = x + rn, x1 = x + w - rn;
        if (x1 <= x0 || max <= min) {
            return min;
        }
        int v = min + (int) ((long) (Math.max(x0, Math.min(x1, px)) - x0) * (max - min) / (x1 - x0));
        v = min + ((v - min + buoc / 2) / buoc) * buoc;
        return Math.max(min, Math.min(max, v));
    }

    /** Công tắc (rãnh viên, núm tròn trắng): bật = teal, núm phải. Cao h, rộng 1,8h. Trả bề ngang. */
    public static int congTac(Graphics g, int x, int y, int h, boolean bat, boolean tat) {
        int w = h * 9 / 5;
        GiaoDien.khoi(g, x, y, w, h, h / 2, bat ? (tat ? ChuDe.CHU_NHAT : ChuDe.CHINH) : ChuDe.NEN_PHU_TOI);
        int rn = h / 2 - 2;
        int cx = bat ? x + w - 2 - rn : x + 2 + rn;
        GiaoDien.khoi(g, cx - rn, y + 3, 2 * rn, 2 * rn, rn, ChuDe.BONG);
        GiaoDien.khoi(g, cx - rn, y + 2, 2 * rn, 2 * rn, rn, ChuDe.NEN);
        return w;
    }

    /** Hàng công tắc: nhãn (+ dòng phụ) trái, công tắc phải; chạm cả hàng = đổi. */
    public static void hangCongTac(Graphics g, int x, int y, int w, int h, String nhan, String phu, boolean bat, boolean tat, Object viec, String ten) {
        int hc = Math.min(h - 6, ChuDe.dt() ? 20 : 15);
        int wc = hc * 9 / 5;
        int lh = ChuDe.cao(ChuDe.NHAN), lp = phu == null ? 0 : ChuDe.cao(ChuDe.PHU);
        int yt = y + (h - lh - lp) / 2;
        ChuDe.chu(g, ChuDe.cat(nhan, ChuDe.NHAN, w - wc - 18), x + 8, yt, ChuDe.NHAN, tat ? ChuDe.CHU_NHAT : ChuDe.CHU, 0);
        if (phu != null) {
            ChuDe.chu(g, ChuDe.cat(phu, ChuDe.PHU, w - wc - 18), x + 8, yt + lh, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        }
        congTac(g, x + w - wc - 8, y + (h - hc) / 2, hc, bat, tat);
        if (!tat) {
            dangKy(g, x, y, w, h, viec, ten);
        }
    }

    /** Chip chờ nhỏ (thay dòng "Đang hỏi máy chủ..."): viên trắng, vòng quay teal + chữ phụ, tâm ngang cx. Trả bề ngang. */
    public static int chipCho(Graphics g, int cx, int y, String chu) {
        int h = ChuDe.cao(ChuDe.PHU) + 6;
        int w = ChuDe.rong(chu, ChuDe.PHU) + h + 12;
        int x = cx - w / 2;
        GiaoDien.khoi(g, x, y + 1, w, h, h / 2, ChuDe.BONG);
        GiaoDien.khoi(g, x, y, w, h, h / 2, ChuDe.NEN);
        int r = h / 2 - 3, vx = x + 5 + r, vy = y + h / 2;
        g.setColor(ChuDe.NEN_PHU_TOI);
        g.drawArc(vx - r, vy - r, 2 * r, 2 * r, 0, 360);
        g.setColor(ChuDe.CHINH);
        int goc = (int) ((System.currentTimeMillis() / 3) % 360);
        g.drawArc(vx - r, vy - r, 2 * r, 2 * r, -goc, 100);
        g.drawArc(vx - r + 1, vy - r + 1, 2 * r - 2, 2 * r - 2, -goc, 100);
        ChuDe.chuGiua(g, chu, x + h + 4, y, h, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        return w;
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
