package game.pokemon;

import game.render.Canvas;
import game.render.screen.Font;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

/**
 * BẢNG CHUNG (0.9.3, ưu tiên điện thoại; 0.9.7 kiểu NRO / NSO): MỘT bộ thành phần cho mọi bảng trong game (Nhân vật,
 * Hành trang, Menu, Kỹ năng, Thú...) để tấm, tab, nút đóng, hàng danh sách, nút bấm giống nhau ở mọi nơi và đủ to.
 *
 * - TẤM canh giữa vùng an toàn (tai thỏ / bo góc - nền đặt anTrai..anDuoi theo px logic), rộng ~78 % (không phủ mép
 *   tới mép), cao theo nội dung (tối đa vùng an toàn) - {@link #oTam}. 0.9.7: khung GỖ viền vàng, góc tấm đồng + Poké
 *   Ball, thân giấy da (mảnh pixel {@link KhungNro}); BIỂN TIÊU ĐỀ đỏ viền vàng giữa mép trên, nút X đỏ góc phải
 *   trên; tab = TAB GỖ THẬT (tab chọn giấy da nối liền thân), chạm một lần.
 * - Nút = KHỐI nổi 3D: cam (chính), xanh lá (xác nhận), xanh dương (phụ), giấy da (trung tính), đỏ (nguy hiểm), đá
 *   (tắt), gỗ (HUD); nhấn = lõm + chữ lún 1 px; bóng cứng 2 px.
 * - Thẻ = giấy da LÕM; hàng = thanh giấy da NỔI, chọn = viền vàng; ô đồ = hộp gỗ lòng tối, chọn = khung vàng.
 * - Thanh trượt rãnh tối + núm đồng, công tắc pixel, chip lọc = nút nhỏ, chip chờ = bảng tối + Poké Ball quay.
 * - Mọi điều khiển đăng ký vùng chạm (VungCham) ngay lúc vẽ; cỡ tối thiểu = max(VungCham.MIN, cỡ thiết kế):
 *   APK ~ 44 dp, PC ~ 32 px thật. Khoảng cách giữa điều khiển >= cach() (~ 8 dp). API (tên hàm, tham số, vùng chạm)
 *   giữ nguyên 0.9.6 - chỉ đổi cách vẽ.
 * - Chữ: font thường của game (Be Vietnam Pro HD trên APK / PC) - không dùng font nhỏ; chữ trên nút màu / biển có viền.
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
    /** Như {@link #danhSach} nhưng mỗi hàng một chiều cao (cao[i]) - hàng nhiều dòng chữ không ép hàng khác cao theo. */
    public static void danhSachCao(Graphics g, Cuon c, int x, int y, int w, int h, int[] cao, Hang hang) {
        int k = cach();
        int n = cao.length;
        int noi = 0;
        for (int i = 0; i < n; i++) {
            noi += cao[i] + (i > 0 ? k : 0);
        }
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
            int yr = y - lech;
            for (int i = 0; i < n; i++) {
                if (yr >= y + h) {
                    break;
                }
                if (yr + cao[i] > y) {
                    hang.ve(g, i, x, yr, wr, cao[i]);
                    g.setClip(x, y, w, h);
                }
                yr += cao[i] + k;
            }
        } finally {
            cat = false;
        }
        g.setClip(0, 0, Canvas.w, Canvas.h);
        if (thanh) {
            c.veThanh(g, x + w - 5, y + 2, h - 4);
        }
    }

    public static int chip(Graphics g, int x, int y, String chu, boolean chon, Object viec, String ten) {
        int h = co(22);
        int w = Math.max(h, ChuDe.rong(chu, ChuDe.NHAN) + 20);
        boolean an = viec != null && dangAn(g, x, y, w, h);
        int m = chon ? KhungNroBang.NUT_CAM : KhungNroBang.NUT_GIAY;
        if (!KhungNro.ve(g, an ? m + 1 : m, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, chon ? ChuDe.CHINH : ChuDe.KE);
        }
        int dy = an ? 1 : 0;
        if (chon) {
            ChuDe.chuVienGiua(g, chu, x + w / 2, y + dy, h, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 2);
        } else {
            ChuDe.chuGiua(g, chu, x + w / 2, y + dy, h, ChuDe.NHAN, ChuDe.CHU, 2);
        }
        VungCham.them(g, x, y, w, h, viec, ten);
        return w;
    }

    /** Bề ngang chip lọc chu (như chip / hangChip). */
    public static int rongChip(String chu) {
        return Math.max(co(22), ChuDe.rong(chu, ChuDe.NHAN) + 20);
    }

    /** Tổng bề ngang hàng chip lọc nếu xếp một hàng. */
    public static int rongHangChip(String[] chu) {
        int t = 0;
        for (int i = 0; i < chu.length; i++) {
            t += rongChip(chu[i]) + (i > 0 ? cach() : 0);
        }
        return t;
    }

    /** Số hàng hangChip dùng ở bề ngang w. */
    public static int soHangChip(String[] chu, int w) {
        int k = cach(), xx = 0, n = 1;
        for (int i = 0; i < chu.length; i++) {
            int wc = rongChip(chu[i]);
            if (xx + wc > w && xx > 0) {
                xx = 0;
                n++;
            }
            xx += wc + k;
        }
        return n;
    }

    /** Nhãn chip chọn gọn "Lọc: Tất cả" (hẹp thì bỏ "Lọc:") - mũi tên xuống vẽ riêng (phông không có "▾"). */
    public static String nhanChipChon(String chu, int w) {
        String a = "Lọc: " + chu;
        return rongChipChon(a) <= w ? a : chu;
    }

    /** Bề ngang chip chọn có nhãn chu (chữ + mũi tên). */
    public static int rongChipChon(String chu) {
        return Math.max(co(22), ChuDe.rongVien(chu, ChuDe.NHAN, 0xFFFFFF) + 34);
    }

    /**
     * MỘT chip "Lọc: <đang chọn> ▼" thay hàng chip khi hàng chip nhiều dòng chiếm chỗ danh sách (APK mật độ cao: chip
     * >= 44 dp, màn 640x360 logic) - chạm mở danh sách bộ lọc (thẻ lệnh / menu phím J2ME). Trả bề ngang đã vẽ.
     */
    public static int chipChon(Graphics g, int x, int y, int wMax, final String[] chu, int chon, final Object[] viec, final String tieuDe,
            String ten) {
        final int fc = chon < 0 || chon >= chu.length ? 0 : chon;
        String nhan = nhanChipChon(chu[fc], wMax);
        int h = co(22), w = rongChipChon(nhan);
        Object mo = viec == null ? null : new game.model.IAction() {
            public void perform() {
                java.util.Vector v = new java.util.Vector();
                for (int i = 0; i < chu.length; i++) {
                    final Object vi = viec[i];
                    v.addElement(new game.model.Command(i == fc ? chu[i] + " (đang xem)" : chu[i], new game.model.IAction() {
                        public void perform() {
                            VungCham.chay(vi);
                        }
                    }));
                }
                if (MenuNguCanh.bat()) {
                    MenuNguCanh.mo(v, tieuDe);
                } else {
                    Canvas.menu.startAt(v, 3);
                }
            }
        };
        boolean an = mo != null && dangAn(g, x, y, w, h);
        if (!KhungNro.ve(g, an ? KhungNroBang.NUT_CAM + 1 : KhungNroBang.NUT_CAM, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, ChuDe.CHINH);
        }
        int dy = an ? 1 : 0, m = 4;
        ChuDe.chuVienGiua(g, nhan, x + (w - 14) / 2 + 2, y + dy, h, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 2);
        // mũi tên xuống (tam giác trắng viền nâu)
        int tx = x + w - 13, ty = y + h / 2 - 2 + dy;
        g.setColor(ChuDe.VIEN);
        g.fillTriangle(tx - m - 1, ty - 1, tx + m + 1, ty - 1, tx, ty + m + 1);
        g.setColor(0xFFFFFF);
        g.fillTriangle(tx - m + 1, ty, tx + m - 1, ty, tx, ty + m - 1);
        VungCham.them(g, x, y, w, h, mo, ten);
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
        mauTam = -1;   // 0.9.7: khung gỗ luôn cùng màu (màu hệ chỉ còn ở sân khấu Pokémon)
        boolean tam = dangTam;
        khungMoTam();
        if (tam) {
            VUA_TAM[0] = x;
            VUA_TAM[1] = y;
            VUA_TAM[2] = w;
            VUA_TAM[3] = h;
            demTam++;
        }
        KhungNro.bong(g, x, y, w, h);
        if (!KhungNro.ve(g, tam ? KhungNroBang.TAM : KhungNroBang.HOP, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 4, ChuDe.VIEN);
            GiaoDien.khoi(g, x + 2, y + 2, w - 4, h - 4, 3, ChuDe.NEN);
        }
        Font.nenSang = true;
        int hd = caoDau();
        int vai = tam ? ChuDe.TIEU_DE_TO : ChuDe.TIEU_DE;
        boolean gon = dauGon && tabs != null && tabs.length > 0;
        dauGon = false;
        if (gon) {
            // 0.9.20 đầu gọn (màn ngang thấp, cỡ chạm to): hàng tab + nút X cùng một hàng, bỏ dải tiêu đề -> nội dung cao
            // thêm một hàng chạm
            int ht = caoTab(), hr = Math.max(ht, hd);
            if (dong != null) {
                nutDong(g, x + w - hd - ChuDe.DEM + 2, y + 3 + (hr - hd) / 2, hd, dong);
            }
            int wt = Math.min(w - 2 * ChuDe.DEM - hd - cach() - 2, tabs.length * (ChuDe.dt() ? 150 : 110));
            int yt = y + 3 + hr - ht;
            ranhTab(g, x + ChuDe.DEM, yt + ht - 2, w - 2 * ChuDe.DEM);
            phanDoan(g, x + ChuDe.DEM + 2, yt, wt - 2, ht, tabs, tabChon, viecTab);
            int yNoi = y + 3 + hr + cach();
            return new int[]{x + ChuDe.DEM, yNoi, w - 2 * ChuDe.DEM, y + h - ChuDe.DEM - yNoi};
        }
        if (tieuDe != null) {
            String t = ChuDe.cat(tieuDe, vai, w - 2 * hd - 48);
            int wb = Math.min(w - 2 * hd - 16, ChuDe.rong(t, vai) + 44);
            int yb = y + 3;
            ChuDe.bien(g, x + (w - wb) / 2, yb, wb, hd);
            ChuDe.chuVienGiua(g, t, x + w / 2, yb, hd, vai, 0xFFFFFF, ChuDe.VIEN, 2);
        }
        if (dong != null) {
            nutDong(g, x + w - hd - ChuDe.DEM + 2, y + 3, hd, dong);
        }
        int yNoi = y + 3 + hd + Math.max(ChuDe.DEM - 2, cach());
        if (tabs != null && tabs.length > 0) {
            int ht = caoTab();
            int wt = Math.min(w - 2 * ChuDe.DEM, tabs.length * (ChuDe.dt() ? 150 : 110));
            ranhTab(g, x + ChuDe.DEM, yNoi + ht - 2, w - 2 * ChuDe.DEM);
            // 0.9.7: tab gỗ canh TRÁI (như sổ tab NRO), thẳng hàng với nội dung bên dưới
            phanDoan(g, x + ChuDe.DEM + 2, yNoi, wt - 2, ht, tabs, tabChon, viecTab);
            yNoi += ht + cach();
        }
        return new int[]{x + ChuDe.DEM, yNoi, w - 2 * ChuDe.DEM, y + h - ChuDe.DEM - yNoi};
    }

    /** Rãnh gỗ 2 px dưới hàng tab (tab chọn phủ lên -> nối liền thân tấm). */
    static void ranhTab(Graphics g, int x, int y, int w) {
        g.setColor(ChuDe.VIEN);
        g.fillRect(x, y, w, 1);
        g.setColor(0x8E4A1E);
        g.fillRect(x, y + 1, w, 1);
    }

    /** Ghi một khung vẽ tấm (không vẽ > 300 ms = tấm mới mở: đặt lại đồng hồ hiệu ứng mở). */
    public static void khungMoTam() {
        long now = System.currentTimeMillis();
        // 0.9.22 (KiemChamHud chạm ô Menu "không ăn" khi máy tải nặng): trước đây "tấm mới mở" = không vẽ > 300 ms - máy
        // chậm / GC dừng khung > 300 ms thì tấm ĐANG MỞ chạy lại hiệu ứng bung (nút Menu co về 70 %), cú chạm vào đúng ô
        // lúc đó rơi xuống nền = đóng Menu. Nay: tấm mới = khung trước KHÔNG vẽ tấm nào (đếm khung VungCham), không theo giờ.
        int k = VungCham.soKhung;
        if (k - khungVeTam > 1) {
            lucMoTam = now;
        }
        khungVeTam = k;
        lucVeTam = now;
    }

    /** Số khung (VungCham.soKhung) lần cuối vẽ tấm (khungMoTam). */
    private static int khungVeTam = -10;

    /**
     * Hiệu ứng mở tấm (0.9.6): gọi SAU khi vẽ nội dung - lớp trắng mờ dần trên tấm vừa mở (140 ms, "fade in" nhẹ).
     */
    public static void moDan(Graphics g) {
        int t = tuoiTam();
        if (t >= 140) {
            return;
        }
        Canvas.resetTrans(g);
        // 0.9.7: mở tấm = phủ tối (nâu) mờ dần, hợp khung gỗ (0.9.6: trắng)
        int a = 0x90 * (140 - t) / 140;
        KieuO.phu(g, VUA_TAM[0], VUA_TAM[1], VUA_TAM[2], VUA_TAM[3], a << 24 | 0x1B0E06);
    }

    /** Thời gian (ms, 0..140) từ lúc tấm hiện - hiệu ứng mở. */
    public static int tuoiTam() {
        if (dungHieuUng) {
            return 140;
        }
        return (int) Math.min(140, System.currentTimeMillis() - lucMoTam);
    }

    /** Kiểm thử: hiệu ứng mở tấm đứng ở trạng thái CUỐI (khung chụp cách nhau > 300 ms không mở lại hiệu ứng). */
    public static boolean dungHieuUng;

    /**
     * 0.9.20: khung tab kế tiếp dùng ĐẦU GỌN (tab cùng hàng nút X, không dải tiêu đề) - màn đặt ngay trước khung, tự xoá
     * sau khi vẽ. Dùng khi màn ngang thấp mà nội dung không vừa (ManNhanVat.canDauGon).
     */
    public static boolean dauGon;

    /** Chấm báo mới cho tab / chip kế tiếp (ChamMoi) - đặt ngay trước khung / hangChip, tự xoá sau khi vẽ. */
    public static boolean[] chamTab, chamChip;

    /** Chip phân đoạn mềm (0.9.6): rãnh viên xám xanh, đoạn chọn = viên trắng nổi (bóng mềm), chữ teal đậm. */
    /** Tiền tố tên vùng chạm của tab (kiểm thử phân biệt hàng tab lồng nhau) - đặt ngay trước phanDoan, tự trả về "tab-". */
    public static String tienToTab = "tab-";

    public static void phanDoan(Graphics g, int x, int y, int w, int h, String[] chu, int chon, Object[] viec) {
        String tienTo = tienToTab;
        tienToTab = "tab-";
        int n = chu.length;
        boolean[] cham = chamTab;
        chamTab = null;
        boolean anh = KhungNro.co();
        if (!anh) {
            GiaoDien.khoi(g, x, y, w, h, 3, ChuDe.NEN_PHU_TOI);
        }
        int wt = w / n;
        for (int i = 0; i < n; i++) {
            int xt = x + i * wt;
            int ww = i == n - 1 ? x + w - xt : wt;
            boolean c = i == chon;
            boolean an = !c && viec != null && dangAn(g, xt, y, ww, h);
            if (anh) {
                if (c) {
                    KhungNro.ve(g, KhungNroBang.TAB_CHON, xt, y, ww - 1, h + 1);
                } else {
                    KhungNro.ve(g, KhungNroBang.TAB, xt, y + (an ? 3 : 2), ww - 1, h - 2);
                }
            } else if (c) {
                GiaoDien.khoi(g, xt + 1, y + 1, ww - 2, h - 2, 3, ChuDe.NEN);
            }
            String ct = ChuDe.cat(chu[i], ChuDe.NHAN, ww - 14);
            if (c) {
                // 0.9.8: tab đang chọn cùng cỡ / độ đậm với tab khác (chữ NHAN viền), chữ tối trên nền giấy da sáng
                ChuDe.chuGiua(g, ct, xt + ww / 2, y + 1, h, ChuDe.NHAN, ChuDe.CHU, 2);
            } else {
                ChuDe.chuVienGiua(g, ct, xt + ww / 2, y + 1 + (an ? 1 : 0), h, ChuDe.NHAN, 0xF6E6C0, ChuDe.VIEN, 2);
            }
            if (cham != null && i < cham.length && cham[i]) {
                ChuDe.cham(g, xt + ww - 8, y + 7);   // 0.9.7: góc phải trên của tab - không đè chữ
            }
            // vùng chạm chừa khe cach() giữa hai tab (không bấm nhầm tab bên cạnh)
            int kh = cach();
            int l = i == 0 ? xt : xt + (kh + 1) / 2, r = i == n - 1 ? xt + ww : xt + ww - kh / 2;
            VungCham.them(g, l, y, r - l, h, viec == null ? null : viec[i], tenTab(tienTo, i));
        }
    }

    /** Nút đóng: đĩa tròn TRẮNG bóng mềm + X tối vẽ nét (vùng chạm cỡ s). */
    public static void nutDong(Graphics g, int x, int y, int s, Object viec) {
        nutDong(g, x, y, s, viec, false);
    }

    public static void nutDong(Graphics g, int x, int y, int s, Object viec, boolean trenDau) {
        int c = s / 2;
        boolean an = viec != null && dangAn(g, x, y, s, s);
        if (!KhungNro.hinhGiua(g, KhungNroBang.DONG, x + c, y + c + (an ? 1 : 0))) {
            int r = Math.min(c - 1, ChuDe.dt() ? 13 : 10);
            diaTrang(g, x + c, y + c, r, an);
            ChuDe.dauX(g, x + c, y + c, Math.max(3, r * 2 / 5), ChuDe.CHU);
        }
        VungCham.them(g, x, y, s, s, viec, "dong");
    }

    /** Đĩa tròn trắng nổi (bóng mềm) tâm (cx, cy) bán kính r - nền nút tròn kiểu GO. */
    public static void diaTrang(Graphics g, int cx, int cy, int r, boolean an) {
        // 0.9.7: nút vuông giấy da nổi (tên giữ cho màn cũ)
        int m = an ? KhungNroBang.NUT_GIAY_NHAN : KhungNroBang.NUT_GIAY;
        if (!an) {
            KhungNro.bong(g, cx - r, cy - r, 2 * r, 2 * r);
        }
        if (!KhungNro.ve(g, m, cx - r, cy - r, 2 * r, 2 * r)) {
            GiaoDien.khoi(g, cx - r, cy - r, 2 * r, 2 * r, 3, ChuDe.VIEN);
            GiaoDien.khoi(g, cx - r + 1, cy - r + 1, 2 * r - 2, 2 * r - 2, 2, an ? ChuDe.NEN_PHU : ChuDe.NEN);
        }
    }

    /**
     * NÚT ICON (0.9.7 kiểu NRO; tên giữ "nút tròn"): khối vuông gỗ (dac: khối màu theo mau) + icon pixel đủ màu, nhãn
     * chữ trắng viền dưới (null = không); vùng chạm phủ cả nút + nhãn. Nhấn = lõm 1 px.
     */
    public static void nutTron(Graphics g, int cx, int cy, int r, int hinh, int mau, boolean dac, String nhan, Object viec, String ten) {
        boolean an = viec != null && dangAn(g, cx - r, cy - r, 2 * r, 2 * r);
        int s = 2 * r, x0 = cx - r, y0 = cy - r;
        int m = dac ? manhMau(mau) : KhungNroBang.NUT_GO;
        if (!an) {
            KhungNro.bong(g, x0, y0, s, s);
        }
        int dy = an ? 1 : 0;
        if (!KhungNro.ve(g, an ? m + 1 : m, x0, y0, s, s)) {
            diaTrang(g, cx, cy, r, an);
        }
        int k = Math.max(1, (s - 6) / 16);
        if (!KhungNro.iconGiua(g, hinh, cx, cy + dy, k, -1)) {
            if (hinh == BieuTuong.POKEBALL) {
                GiaoDien.pokeBall(g, cx, cy, r * 3 / 4, 0);
            } else {
                BieuTuong.ve(g, hinh, cx - r / 2, cy - r / 2, r, dac ? 0xFFFFFF : mau, dac ? mau : ChuDe.NEN);
            }
        }
        int hNhan = 0;
        if (nhan != null) {
            hNhan = ChuDe.cao(ChuDe.NHAN) + 2;
            ChuDe.chuVien(g, nhan, cx, cy + r + 4, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 2);
        }
        if (viec != null) {
            int wv = Math.max(2 * r, nhan == null ? 0 : ChuDe.rong(nhan, ChuDe.NHAN) + 4);
            VungCham.them(g, cx - wv / 2, cy - r, wv, 2 * r + (hNhan > 0 ? hNhan + 3 : 0), viec, ten);
        }
    }

    /**
     * NÚT VUÔNG HUD (0.9.7): khối mảnh `manh` (KhungNroBang.NUT_*) tâm (cx, cy) nửa cạnh r, bóng cứng; sang = viền vàng
     * (đang bật / ngữ cảnh). Chỉ vẽ (vùng chạm do HUD tự quản).
     */
    public static void nutVuong(Graphics g, int cx, int cy, int r, int manh, boolean sang, boolean an) {
        int x = cx - r, y = cy - r, s = 2 * r;
        if (sang) {
            g.setColor(ChuDe.VIEN);
            g.fillRect(x - 3, y - 2, s + 6, s + 4);
            g.fillRect(x - 2, y - 3, s + 4, s + 6);
            g.setColor(ChuDe.VANG);
            g.fillRect(x - 2, y - 1, s + 4, s + 2);
            g.fillRect(x - 1, y - 2, s + 2, s + 4);
        } else if (!an) {
            KhungNro.bong(g, x, y, s, s);
        }
        if (!KhungNro.ve(g, an ? manh + 1 : manh, x, y, s, s)) {
            GiaoDien.khoi(g, x, y, s, s, 3, ChuDe.VIEN);
            GiaoDien.khoi(g, x + 1, y + 1, s - 2, s - 2, 2, 0x7A3E18);
        }
    }

    /** Mảnh nút khối gần màu mau nhất (đỏ / cam / xanh lá / xanh dương / gỗ). */
    public static int manhMau(int mau) {
        int r = mau >> 16 & 255, gg = mau >> 8 & 255, b = mau & 255;
        int mx = Math.max(r, Math.max(gg, b)), mn = Math.min(r, Math.min(gg, b));
        if (mx - mn < 40) {
            return KhungNroBang.NUT_GO;
        }
        if (r >= gg && r >= b) {
            return gg > r * 55 / 100 ? KhungNroBang.NUT_CAM : KhungNroBang.NUT_DO;
        }
        if (gg >= b) {
            return r > gg * 80 / 100 ? KhungNroBang.NUT_CAM : KhungNroBang.NUT_LA;
        }
        return KhungNroBang.NUT_XANH;
    }


    /** Tab đơn (giữ cho màn cũ): chip viên (chọn = trắng nổi chữ teal). */
    public static void tab(Graphics g, int x, int y, int w, int h, String chu, boolean chon, Object viec, String ten) {
        if (!KhungNro.ve(g, chon ? KhungNroBang.TAB_CHON : KhungNroBang.TAB, x, chon ? y : y + 2, w, chon ? h : h - 2)) {
            GiaoDien.khoi(g, x, y, w, h, 3, chon ? ChuDe.NEN : ChuDe.NEN_PHU_TOI);
        }
        String c = ChuDe.cat(chu, ChuDe.NHAN, w - 6);
        if (chon) {
            ChuDe.chuGiua(g, c, x + w / 2, y + 1, h, ChuDe.NHAN, ChuDe.CHU, 2);   // cùng cỡ / đậm với tab thường
        } else {
            ChuDe.chuVienGiua(g, c, x + w / 2, y + 1, h, ChuDe.NHAN, 0xF6E6C0, ChuDe.VIEN, 2);
        }
        VungCham.them(g, x, y, w, h, viec, ten);
    }

    /** Thẻ nội dung: trắng bo tròn, bóng mềm, đường kẻ rất nhạt (không viền tối). */
    public static void the(Graphics g, int x, int y, int w, int h) {
        // 0.9.7: thẻ giấy da LÕM (mảnh THE)
        if (!KhungNro.ve(g, KhungNroBang.THE, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, ChuDe.KE);
            GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, 2, ChuDe.NEN_DUOI);
        }
        Font.nenSang = true;
    }

    /**
     * kiểu nút (0.9.7 NRO): 0 phụ (xanh dương), 1 chính (cam), 2 xác nhận (xanh lá), 3 tắt (đá), 4 trung tính (giấy da),
     * 5 nguy hiểm (đỏ), 6 gỗ (HUD / thanh công cụ - thêm 0.9.7)
     */
    public static final int PHU = 0, CHINH = 1, XAC_NHAN = 2, TAT = 3, TRUNG = 4, NGUY = 5, GO = 6;

    /** Màu thân nút theo kiểu (đáy dải). */
    static int mauNut(int kieu) {
        switch (kieu) {
            case CHINH:
                return ChuDe.CHINH;
            case XAC_NHAN:
                return ChuDe.LA;
            case PHU:
                return ChuDe.XANH;
            case NGUY:
                return ChuDe.DO;
            case TAT:
                return 0x8A8A92;
            case GO:
                return 0x7A3E18;
            default:
                return ChuDe.NEN;
        }
    }

    /** Nút có thân màu đặc (chữ trắng)? */
    static boolean dac(int kieu) {
        return kieu != TRUNG;   // 0.9.7: mọi nút màu (chữ trắng viền) trừ giấy da trung tính
    }

    /** Nút KHỐI (0.9.7, dùng chung bảng + hộp thoại kiểu A): mảnh pixel theo kiểu; nhấn = lõm, chữ lún 1 px. */
    public static void vienNut(Graphics g, int x, int y, int w, int h, String chu, int kieu, boolean an) {
        if (kieu != TAT && !an) {
            KhungNro.bong(g, x, y, w, h);
        }
        if (!KhungNro.ve(g, KhungNro.manhNut(kieu, an), x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, ChuDe.VIEN);
            GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, 2, an ? GiaoDien.pha(mauNut(kieu), 0, 1, 5) : mauNut(kieu));
        }
        if (chu != null && chu.length() > 0) {
            int dy = an ? 1 : 0;
            String c = ChuDe.cat(chu, ChuDe.SO, w - 12);
            if (dac(kieu)) {
                ChuDe.chuVienGiua(g, c, x + w / 2, y + dy, h, ChuDe.SO, kieu == TAT ? 0xD8D8DE : 0xFFFFFF, ChuDe.VIEN, 2);
            } else {
                ChuDe.chuGiua(g, c, x + w / 2, y + dy, h, ChuDe.SO, ChuDe.CHU, 2);
            }
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
        boolean an = viec != null && dangAn(g, x, y, s, s);
        if (!KhungNro.hinhGiua(g, KhungNroBang.LUI, x + c, y + c + (an ? 1 : 0))) {
            int r = Math.min(c - 1, ChuDe.dt() ? 13 : 10);
            diaTrang(g, x + c, y + c, an ? r * 24 / 25 : r, an);
            int cx = x + c, cy = y + c, a = Math.max(3, r * 2 / 5);
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

    /** Hàng danh sách: thanh giấy da NỔI (0.9.7); đang chọn = viền vàng; nhấn = lõm; đăng ký chạm cả hàng. */
    public static void hang(Graphics g, int x, int y, int w, int h, int i, boolean chon, Object viec, String ten) {
        boolean an = viec != null && dangAn(g, x, y, w, h);
        int m = chon ? KhungNroBang.HANG_CHON : an ? KhungNroBang.HANG_NHAN : KhungNroBang.HANG;
        if (!KhungNro.ve(g, m, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, chon ? ChuDe.CHINH : ChuDe.KE);
            GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, 2, chon ? ChuDe.CHINH_NHAT : an ? ChuDe.NEN_PHU : ChuDe.NEN);
        }
        dangKy(g, x, y, w, h, viec, ten);
    }


    /** Trạng thái trống: Poké Ball + dòng chính (đậm) + dòng gợi ý (chữ tự xuống dòng). */
    public static void trong(Graphics g, int x, int y, int w, int h, String chinh, String phu) {
        int cy = y + h / 2 - 22;
        if (!KhungNro.iconGiua(g, BieuTuong.POKEBALL, x + w / 2, cy, 2, -1)) {
            GiaoDien.pokeBall(g, x + w / 2, cy, 12, 0);
        }
        int yy = cy + 20;
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

    /** Ô đồ (0.9.7): hộp gỗ lòng tối lõm; trống = tối hơn; chọn = khung vàng; đăng ký chạm. */
    public static void oDo(Graphics g, int x, int y, int s, boolean co, boolean chon, Object viec, String ten) {
        int m = chon ? KhungNroBang.O_CHON : co ? KhungNroBang.O : KhungNroBang.O_TRONG;
        if (!KhungNro.ve(g, m, x, y, s, s)) {
            GiaoDien.khoi(g, x, y, s, s, 3, chon ? ChuDe.CHINH : ChuDe.VIEN);
            GiaoDien.khoi(g, x + 2, y + 2, s - 4, s - 4, 2, 0x2E1E14);
        }
        dangKy(g, x, y, s, s, viec, ten);
    }

    // Kiem lag 10/2026: ten vung cham tab (tienTo + i) ghep chuoi moi khung moi tab - nho TEN_SO ket qua gan nhat.
    private static final int TEN_SO = 32;
    private static final String[] TEN_DAU = new String[TEN_SO], TEN_KQ = new String[TEN_SO];
    private static final int[] TEN_I = new int[TEN_SO];
    private static int tenKe;

    static String tenTab(String tienTo, int i) {
        if (tienTo == null) {
            return tienTo + i;
        }
        for (int k = 0; k < TEN_SO; k++) {
            if (TEN_I[k] == i && tienTo.equals(TEN_DAU[k])) {
                return TEN_KQ[k];
            }
        }
        String t = tienTo + i;
        int o = tenKe;
        tenKe = (o + 1) % TEN_SO;
        TEN_DAU[o] = tienTo;
        TEN_I[o] = i;
        TEN_KQ[o] = t;
        return t;
    }

    /** Ô KHOÁ cuối lưới hành trang (0.9.7): đá xám + ổ khoá pixel. */
    public static void oKhoa(Graphics g, int x, int y, int s, Object viec, String ten) {
        if (!KhungNro.ve(g, KhungNroBang.O_KHOA, x, y, s, s)) {
            GiaoDien.khoi(g, x, y, s, s, 3, 0x55555E);
        }
        KhungNro.icon(g, BieuTuong.KHOA, x, y, s, -1);
        dangKy(g, x, y, s, s, viec, ten);
    }


    /** Ô trang bị (tên giữ "tròn"; 0.9.7: ô VUÔNG hộp gỗ như NRO) tâm (cx, cy) nửa cạnh r. */
    public static void oTron(Graphics g, int cx, int cy, int r, boolean co, boolean chon, Object viec, String ten) {
        oDo(g, cx - r, cy - r, 2 * r, co, chon, viec, ten);
    }


    // ------------------------------------------------------------------ 0.9.6: điều khiển cài đặt (thanh trượt, công tắc, chip chờ)

    /**
     * Thanh trượt giá trị [min, max] bước buoc trong (x, y, w, h): rãnh mảnh, phần đầy teal, núm tròn trắng viền teal.
     * 0.9.6.1: nhấn trong thanh rồi KÉO thì núm + giá trị theo ngón / chuột NGAY từng khung (kể cả kéo ra ngoài thanh -
     * kẹp về min / max); thả (ở đâu cũng được, VungCham.themKeo) chạy viec MỘT lần - viec đọc {@link #giaTriTha()}.
     * Trả giá trị đang hiện. tat = xám, không nhận chạm.
     */
    public static int thanhTruot(Graphics g, int x, int y, int w, int h, int gt, int min, int max, int buoc, boolean tat, Object viec, String ten) {
        int rn = nutTruot(h);
        int x0 = x + rn, x1 = x + w - rn;
        int tx = g.getTranslateX(), ty = g.getTranslateY();
        boolean nhan = !tat && VungCham.trongVung(x + tx, y + ty, w, h, Canvas.pxLast, Canvas.pyLast);
        if (nhan) {
            // cú nhấn gần nhất bắt đầu trong thanh này: ghi hình học (toạ độ tuyệt đối) cho giaTriTha()
            truotX = x + tx;
            truotW = w;
            truotH = h;
            truotMin = min;
            truotMax = max;
            truotBuoc = buoc;
            if (Canvas.dangGiu) {
                gt = giaTriTruot(Canvas.px, truotX, w, h, min, max, buoc);
                keoTen = ten;
                keoGt = gt;
                keoLuc = System.currentTimeMillis();
            } else if (ten != null && ten.equals(keoTen) && System.currentTimeMillis() - keoLuc < 300) {
                // vừa thả: giữ giá trị vừa kéo tới khi viec (chạy ở nhịp game kế) lưu xong - không giật về giá trị cũ
                gt = keoGt;
            }
        }
        int cy = y + h / 2;
        int hr = ChuDe.dt() ? 9 : 8;
        int xn = max > min ? x0 + (int) ((long) (gt - min) * (x1 - x0) / (max - min)) : x0;
        // rãnh tối lõm + dải vàng đã kéo + núm đồng (pixel)
        if (!KhungNro.ve(g, KhungNroBang.RANH, x0 - 3, cy - hr / 2, x1 - x0 + 6, hr)) {
            GiaoDien.khoi(g, x0, cy - 2, x1 - x0, 4, 1, ChuDe.RANH);
        }
        if (xn > x0) {
            KhungNro.ve(g, tat ? KhungNroBang.DAY_PP : KhungNroBang.DAY_EXP, x0 - 1, cy - hr / 2 + 2, xn - x0 + 2, hr - 4);
        }
        if (!KhungNro.hinhGiua(g, KhungNroBang.NUM_TRUOT, xn, cy)) {
            GiaoDien.khoi(g, xn - rn, cy - rn, 2 * rn, 2 * rn, 2, ChuDe.VANG);
        }
        if (!tat) {
            VungCham.themKeo(g, x, y, w, h, viec, ten);
        }
        if (nhan) {
            nhanTen = ten;
            nhanGt = gt;
            nhanTamNut = xn + tx;
        }
        return gt;
    }

    /** Thanh trượt đang / vừa kéo (tên vùng), giá trị đang hiện, lúc ghi. */
    static String keoTen;
    static int keoGt;
    static long keoLuc;
    /** Hình học (toạ độ tuyệt đối) của thanh mà cú nhấn gần nhất bắt đầu trong đó. */
    private static int truotX, truotW = 1, truotH = 1, truotMin, truotMax, truotBuoc = 1;
    /** kiểm thử: thanh của cú nhấn gần nhất - tên, giá trị đang hiện, hoành độ tuyệt đối tâm núm (khung vừa vẽ) */
    public static String nhanTen;
    public static int nhanGt, nhanTamNut;

    /** Giá trị tại điểm thả (Canvas.px) của thanh vừa kéo - gọi trong viec của thanhTruot. */
    public static int giaTriTha() {
        return giaTriTruot(Canvas.px, truotX, truotW, truotH, truotMin, truotMax, truotBuoc);
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
        int m = bat ? KhungNroBang.CONG_TAC_BAT : KhungNroBang.CONG_TAC_TAT;
        if (KhungNro.co()) {
            int w = KhungNro.rong(m);
            KhungNro.hinh(g, m, x, y + (h - KhungNro.cao(m)) / 2);
            if (tat) {
                KieuO.phu(g, x, y + (h - KhungNro.cao(m)) / 2, w, KhungNro.cao(m), 0x80F2E0B4);
            }
            return w;
        }
        int w = h * 9 / 5;
        GiaoDien.khoi(g, x, y, w, h, 2, bat ? (tat ? ChuDe.CHU_NHAT : ChuDe.LA) : 0x2E1E14);
        int rn = h / 2 - 2;
        int cx = bat ? x + w - 2 - rn : x + 2 + rn;
        GiaoDien.khoi(g, cx - rn, y + 2, 2 * rn, 2 * rn, 1, ChuDe.VANG);
        return w;
    }

    /** Hàng công tắc: nhãn (+ dòng phụ) trái, công tắc phải; chạm cả hàng = đổi. */
    public static void hangCongTac(Graphics g, int x, int y, int w, int h, String nhan, String phu, boolean bat, boolean tat, Object viec, String ten) {
        int hc = Math.min(h - 6, ChuDe.dt() ? 20 : 15);
        int wc = KhungNro.co() ? KhungNro.rong(KhungNroBang.CONG_TAC_BAT) : hc * 9 / 5;
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
        int h = Math.max(ChuDe.cao(ChuDe.PHU) + 6, 20);
        int w = ChuDe.rong(chu, ChuDe.PHU) + h + 16;
        int x = cx - w / 2;
        if (!KhungNro.ve(g, KhungNroBang.TOOLTIP, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 2, 0x2B1A10);
        }
        int xoay = (int) (System.currentTimeMillis() / 120 % 4);
        if (!KhungNro.iconXoay(g, BieuTuong.POKEBALL, x + 4 + 8, y + h / 2, xoay)) {
            GiaoDien.dangTai(g, x + 4 + h / 2, y + h / 2, h / 2 - 4);
        }
        // chip nền tối: font ảnh J2ME giữ bản chữ sáng (Font.nenSang của thẻ sáng bên dưới tô chữ thành navy - không đọc được)
        boolean nenCu = Font.nenSang;
        Font.nenSang = false;
        try {
            ChuDe.chuGiua(g, chu, x + h + 6, y, h, ChuDe.PHU, 0xF2E0B4, 0);
        } finally {
            Font.nenSang = nenCu;
        }
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
            ChuDe.chuVienGiua(g, chu, x + w / 2, y, h, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
        }
    }
}
