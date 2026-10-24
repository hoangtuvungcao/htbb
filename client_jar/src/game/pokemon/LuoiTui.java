package game.pokemon;

import game.model.Char;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * LƯỚI HÀNH TRANG kiểu NRO (0.9.7, yêu cầu chủ dự án): Ô VUÔNG hộp gỗ thay danh sách, đặt cạnh nhân vật (màn Nhân vật /
 * tab Trang bị) và ở tab Bán của cửa hàng. Chip lọc Tất cả / Trang bị / Thuốc / Nguyên liệu / Khác, đếm "38/50" (số ô
 * đã dùng / sức chứa - gói -105 TrangThaiNguoiChoi.oDaMo), ô trống, ô KHOÁ cuối lưới (tới trần mở rộng) - chạm ô khoá =
 * hỏi mở rộng (dùng vé / mua bằng xu, lệnh -107). Chạm ô đồ = thẻ thông tin {@link TheVatPham}; PC: nháy đúp = dùng /
 * mặc nhanh, chuột phải = thao tác nhanh ({@link #chuotPhai}).
 *
 * Mục: Integer = bình thuốc (loại), ItemInInventory = đồ, GemTemplate = nguyên liệu (khoá nếu thuộc
 * MainChar.gemItemKhoa) hoặc vật phẩm đặc biệt (thuộc MainChar.shopItem), int[]{id} = vật phẩm nhiệm vụ.
 */
public final class LuoiTui {
    // Kiem lag 10/2026: ten vung cham moi o duoc ghep chuoi MOI khung (vai chuc o) - nho lai theo so o.
    private static final String[] TEN_TUI = new String[160], TEN_KHOA = new String[160];

    static String tenO(String dau, String[] bang, int j) {
        if (j < 0 || j >= bang.length) {
            return dau + j;
        }
        String t = bang[j];
        if (t == null) {
            t = dau + j;
            bang[j] = t;
        }
        return t;
    }

    private LuoiTui() {
    }

    public static final String[] LOC = {"Tất cả", "Trang bị", "Thuốc", "Nguyên liệu", "Khác"};
    public static final int LOC_TAT_CA = 0, LOC_DO = 1, LOC_THUOC = 2, LOC_NGUYEN_LIEU = 3, LOC_KHAC = 4;

    /** Bấm một mục (null = ô trống). */
    public interface Bam {
        void bam(Object mon);
    }

    // ------------------------------------------------------------------ dữ liệu

    static MainChar mc() {
        return Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
    }

    public static boolean laKhoa(Object o) {
        return o instanceof GemTemplate && MainChar.gemItemKhoa != null && MainChar.gemItemKhoa.contains(o);
    }

    public static boolean laDacBiet(Object o) {
        return o instanceof GemTemplate && MainChar.shopItem != null && MainChar.shopItem.contains(o);
    }

    /**
     * Số ô đang dùng = số mục của lưới "Tất cả" trừ vật phẩm nhiệm vụ (mảng riêng của máy chủ, không chiếm ô); mỗi chồng
     * (bình cùng loại, nguyên liệu) = 1 ô - đếm khớp đúng số ô có đồ người chơi thấy.
     */
    public static int soDung() {
        Vector v = ds(LOC_TAT_CA);
        int n = 0;
        for (int i = 0; i < v.size(); i++) {
            if (!(v.elementAt(i) instanceof int[])) {
                n++;
            }
        }
        return n;
    }

    private static Vector dem;
    private static int khoa = 1;

    /** Danh sách mục theo bộ lọc (nhớ giữa các khung, dựng lại khi túi / gói -105 đổi). */
    public static Vector ds(int loc) {
        MainChar m = mc();
        int k = TrangThaiNguoiChoi.phienBan * 31 + loc;
        k = k * 31 + Bang.vanTay(Char.inventory);
        k = k * 31 + Bang.vanTay(MainChar.gemItem);
        k = k * 31 + Bang.vanTay(MainChar.gemItemKhoa);
        k = k * 31 + Bang.vanTay(MainChar.shopItem);
        k = k * 31 + (m == null ? 0 : Bang.vanTay(m.potions));
        k = k * 31 + Bang.vanTay(MainChar.itemQuest);
        if (dem != null && k == khoa) {
            return dem;
        }
        Vector v = new Vector();
        boolean tat = loc == LOC_TAT_CA;
        if ((tat || loc == LOC_DO) && Char.inventory != null) {
            for (int i = 0; i < Char.inventory.size(); i++) {
                v.addElement(Char.inventory.elementAt(i));
            }
        }
        if ((tat || loc == LOC_THUOC) && m != null && m.potions != null) {
            for (int i = 1; i < m.potions.length; i++) {
                if (m.potions[i] > 0) {
                    v.addElement(new Integer(i));
                }
            }
        }
        if (tat || loc == LOC_NGUYEN_LIEU) {
            Vector[] g = {MainChar.gemItemKhoa, MainChar.gemItem};
            for (int q = 0; q < g.length; q++) {
                for (int i = 0; g[q] != null && i < g[q].size(); i++) {
                    v.addElement(g[q].elementAt(i));
                }
            }
        }
        if (tat || loc == LOC_KHAC) {
            for (int i = 0; MainChar.shopItem != null && i < MainChar.shopItem.size(); i++) {
                v.addElement(MainChar.shopItem.elementAt(i));
            }
            // vật phẩm nhiệm vụ: mảng riêng của máy chủ (không chiếm ô hành trang) -> chỉ ở tab Khác, không ở "Tất cả"
            if (MainChar.itemQuest != null && loc == LOC_KHAC) {
                for (int i = 0; i < MainChar.itemQuest.length; i++) {
                    if (MainChar.itemQuest[i] > 0) {
                        v.addElement(new int[]{i});
                    }
                }
            }
        }
        dem = v;
        khoa = k;
        return v;
    }

    /** Cạnh ô lưới (px logic, >= cỡ chạm). */
    public static int coO() {
        return Math.max(Bang.co(26), ChuDe.dt() ? 32 : 28);
    }

    // ------------------------------------------------------------------ PC: nháy đúp / chuột phải

    /** ô vừa vẽ (toạ độ tuyệt đối) - chuột phải / nháy đúp */
    private static final int SO_O = 160;
    private static final int[] OX = new int[SO_O], OY = new int[SO_O], OS = new int[SO_O];
    private static final Object[] OM = new Object[SO_O];
    private static int soO;
    private static long luoiLuc;
    /** ngữ cảnh lưới vừa vẽ (TheVatPham.TUI / BAN) */
    private static int nguCanhVe;
    private static Object choMon;
    private static long choLuc;
    private static Bam choBam;

    /** Chuột phải (toạ độ logic): trên một ô đồ của lưới vừa vẽ -> thao tác nhanh. true = đã xử lý. */
    public static boolean chuotPhai(int x, int y) {
        if (System.currentTimeMillis() - luoiLuc > 400) {
            return false;
        }
        for (int i = 0; i < soO; i++) {
            if (OM[i] != null && x >= OX[i] && x < OX[i] + OS[i] && y >= OY[i] && y < OY[i] + OS[i]) {
                TheVatPham.nhanh(OM[i], nguCanhVe);
                return true;
            }
        }
        return false;
    }

    /** Bấm ô: máy cảm ứng mở thẻ ngay; PC chờ ~0,3 s để phân biệt nháy đúp (= thao tác nhanh). */
    static void bamO(Object mon, Bam b) {
        if (ChuDe.dt() || mon == null) {
            b.bam(mon);
            return;
        }
        long now = System.currentTimeMillis();
        if (choMon == mon && now - choLuc < 350) {
            choMon = null;
            TheVatPham.nhanh(mon, nguCanhVe);
            return;
        }
        choMon = mon;
        choLuc = now;
        choBam = b;
    }

    /** Gọi mỗi khung vẽ: hết thời gian chờ nháy đúp -> mở thẻ. */
    static void capNhatCho() {
        if (choMon != null && System.currentTimeMillis() - choLuc >= 300) {
            Object m = choMon;
            choMon = null;
            if (choBam != null) {
                choBam.bam(m);
            }
        }
    }

    // ------------------------------------------------------------------ vẽ

    /**
     * Bảng hành trang trong (x, y, w, h): đầu = chip lọc + đếm ô; giữa = lưới cuộn; đáy = hàng ví. mon = danh sách đã
     * lọc (null = ds(loc)); coKhoa = hiện ô trống + ô khoá tới trần (chỉ "Tất cả"); chon = mục đang chọn.
     * Trả {chiều cao đầu} (để màn đặt tiêu đề phụ).
     */
    public static void ve(Graphics g, Cuon c, int x, int y, int w, int h, int loc, final Vector mon, boolean coKhoa, Object chon,
            final Object[] viecLoc, final Bam bam, int nguCanh) {
        capNhatCho();
        int k = Bang.cach();
        if (!KhungNro.ve(g, KhungNroBang.THE, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, ChuDe.KE);
        }
        Font_nenSang();
        MainChar m = mc();
        int yy = y + 5;
        // đầu: TAB GỖ lọc (Tất cả / Trang bị / Thuốc / Nguyên liệu / Khác); đáy: hàng ví + đếm ô "38/50"
        int suc = TrangThaiNguoiChoi.sucChua(m);
        int hc = Bang.caoTab();
        if (viecLoc != null && (w - 10) / LOC.length >= VungCham.MIN + k + 4) {
            Bang.ranhTab(g, x + 4, yy + hc - 2, w - 8);
            Bang.tienToTab = "loc-";
            Bang.phanDoan(g, x + 5, yy, w - 10, hc, LOC, loc, viecLoc);
            yy += hc + k;
        } else if (viecLoc != null) {
            // bảng hẹp (điện thoại 16:9 ...): chip nút nhỏ xuống dòng, mỗi chip >= cỡ chạm
            yy += Bang.hangChip(g, x + 6, yy, w - 12, LOC, loc, viecLoc) + k;
        } else {
            yy += hc + k;
        }
        String dem = soDung() + "/" + suc;
        int wDem = ChuDe.rong(dem, ChuDe.SO) + 16;
        int wVi = w - 10 - wDem - k;
        ViTien.boKhoa = false;
        int soDVi = ViTien.soDong(wVi);
        if (soDVi > 1 && y + h - 5 - (ViTien.cao() * soDVi + (soDVi - 1) * k) - k - yy < 2 * (coO() + k)) {
            // ví xuống dòng mà lưới còn < 2 hàng ô (điện thoại 16:9): bỏ lượng khoá khỏi hàng ví (cửa hàng vẫn hiện)
            ViTien.boKhoa = true;
            soDVi = ViTien.soDong(wVi);
        }
        int hVi = ViTien.cao() * soDVi + (soDVi - 1) * k;   // 0.9.10: ví hẹp (J2ME 4:3) xuống dòng thay vì chữ đè biểu tượng
        int yVi = y + h - 5 - hVi;
        ViTien.hang(g, x + 5, yVi, wVi, hVi);
        ViTien.boKhoa = false;
        GiaoDien.khoiPixel(g, x + w - 5 - wDem, yVi, wDem, hVi, soDung() >= suc ? ChuDe.DO : 0x5C2E12);
        ChuDe.chuVienGiua(g, dem, x + w - 5 - wDem / 2, yVi, hVi, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
        // lưới
        final int s = coO();
        int gw = w - 12;
        final int cot = Math.max(1, (gw + k) / (s + k));
        final int n = mon.size();
        int tran = TrangThaiNguoiChoi.tran(m);
        int hl0 = yVi - k - yy;
        int hangDay = Math.max(1, (hl0 + k) / (s + k));   // số hàng lấp đầy bảng (lưới đủ ô như NRO)
        int dung = coKhoa ? soDung() : n;
        final int soTrong = coKhoa ? Math.max(0, suc - dung) : 0;
        final int soKhoa = coKhoa ? Math.max(0, tran - Math.max(suc, dung)) : 0;
        soKhoaVe = soKhoa;
        int tong = n + soTrong + soKhoa;
        tong = Math.max(((tong + cot - 1) / cot) * cot, hangDay * cot);   // lấp hàng cuối + đủ hàng phủ bảng
        final int tongO = tong;
        final Object chonO = chon;
        int xl = x + 6 + (gw - (cot * (s + k) - k)) / 2;
        int hl = yVi - k - yy;
        nguCanhVe = nguCanh;
        luoiLuc = System.currentTimeMillis();
        soO = 0;
        final int x0 = xl;
        Bang.danhSach(g, c, xl - 1, yy, cot * (s + k) - k + 12, hl, (tongO + cot - 1) / cot, s, new Bang.Hang() {
            public void ve(Graphics g, int i, int xr, int yr, int wr, int hr) {
                for (int q = 0; q < cot; q++) {
                    int j = i * cot + q;
                    if (j >= tongO) {
                        break;
                    }
                    int xo = x0 + q * (s + Bang.cach());
                    if (j < n) {
                        final Object o = mon.elementAt(j);
                        veO(g, o, xo, yr, s, o == chonO, new IAction() {
                            public void perform() {
                                bamO(o, bam);
                            }
                        }, tenO("tui-", TEN_TUI, j));
                        if (soO < SO_O) {
                            OX[soO] = xo + g.getTranslateX();
                            OY[soO] = yr + g.getTranslateY();
                            OS[soO] = s;
                            OM[soO] = o;
                            soO++;
                        }
                    } else if (j < n + soTrong || j >= n + soTrong + soKhoa) {
                        Bang.oDo(g, xo, yr, s, false, false, null, null);
                    } else {
                        Bang.oKhoa(g, xo, yr, s, new IAction() {
                            public void perform() {
                                hoiMoRong();
                            }
                        }, tenO("tui-khoa-", TEN_KHOA, j));
                    }
                }
            }
        });
        if (n == 0 && viecLoc != null) {
            dongTrong(g, x + 14, yy + Math.min(hl / 3, 2 * (s + k)), w - 28, loc);
        }
        yLuoi = yy;
        hLuoi = hl;
    }

    // ------------------------------------------------------------------ 0.9.18: kiểu GỌN (màn Nhân vật › Trang bị)

    /** Nhãn ngắn của bộ lọc (chip hẹp). */
    public static final String[] LOC_NGAN = {"Tất cả", "Đồ", "Thuốc", "N.liệu", "Khác"};
    /** Icon PNG từng bộ lọc (chip chỉ icon khi bảng rất hẹp). */
    static final int[] ICON_LOC = {BieuTuong.TUI, BieuTuong.AO, BieuTuong.THUOC, BieuTuong.NGUYEN_LIEU, BieuTuong.HOP_DO};

    /** Hình học lần vẽ gọn gần nhất (kiểm thử): {x, y, w, h} hàng chip lọc / lưới / hàng ví, số cột, số hàng thấy đủ, mức nhãn chip. */
    public static final int[] GON_CHIP = new int[4], GON_LUOI = new int[4], GON_VI = new int[4];
    public static int gonCot, gonHang, gonMucNhan;
    /** Cao đầu bảng gọn (hàng chip / hàng lùi) lần vẽ gần nhất - màn đặt nút lùi đúng cỡ. */
    public static int gonCaoDau;

    /** Cao hàng chip lọc gọn (>= cỡ chạm). */
    public static int caoChipGon() {
        return Bang.co(Math.max(18, ChuDe.cao(ChuDe.NHAN) + 6));
    }

    /** Cao tối thiểu phần "khung" của bảng gọn (đệm + chip + ví + khe) - màn tính số hàng lưới thấy được. */
    public static int caoKhungGon(int w) {
        int k = Bang.cach();
        return 4 + caoChipGon() + k + k + ViTien.cao() + 4;
    }

    /** Bề ngang hàng chip ở mức nhãn muc (0 đủ chữ, 1 chữ ngắn, 2 icon + chữ mục chọn, 3 chỉ icon). */
    static int rongChip(int muc, int i, int chon, int hc) {
        int ic = coIconChip(hc);
        int toiThieu = Math.max(VungCham.MIN, hc);
        if (muc == 0) {
            return Math.max(toiThieu, ChuDe.rong(LOC[i], ChuDe.NHAN) + 14);
        }
        if (muc == 1) {
            return Math.max(toiThieu, ChuDe.rong(LOC_NGAN[i], ChuDe.NHAN) + 12);
        }
        if (muc == 2 && i == chon) {
            return Math.max(toiThieu, ic + 4 + ChuDe.rong(LOC_NGAN[i], ChuDe.NHAN) + 12);
        }
        return Math.max(toiThieu, ic + 8);
    }

    static int coIconChip(int hc) {
        return 16 * Math.max(1, (hc - 4) / 24);
    }

    /** Trang đầu của hàng chip khi phải lật trang (bảng quá hẹp cho 5 chip đủ cỡ chạm - điện thoại 16:9 dpi cao). */
    static int trangChip;

    /** Chữ nút lật trang chip: phông ảnh J2ME không có "‹ ›". */
    static String muiTen(boolean phai) {
        return ChuUI.bo != null ? (phai ? "›" : "‹") : (phai ? ">" : "<");
    }

    static int rongMuiTen(int hc) {
        return Math.max(Math.max(VungCham.MIN, hc), ChuDe.rong(muiTen(true), ChuDe.NHAN) + 14);
    }

    /**
     * Hàng chip lọc MỘT hàng trong (x, y, w, hc): chọn mức nhãn dài nhất còn vừa (đủ chữ -> chữ ngắn -> icon + chữ mục
     * chọn -> chỉ icon), phần dư chia đều để hàng phủ kín bề ngang (mép thẳng với lưới). Vùng chạm "loc-i".
     */
    static void hangChipGon(Graphics g, int x, int y, int w, int hc, int chon, final Object[] viec) {
        int k = Bang.cach();
        int n = LOC.length;
        int muc = 0;
        int tong = 0;
        for (; muc < 3; muc++) {
            tong = (n - 1) * k;
            for (int i = 0; i < n; i++) {
                tong += rongChip(muc, i, chon, hc);
            }
            if (tong <= w) {
                break;
            }
        }
        int dau = 0, cuoi = n;
        if (muc >= 3) {
            // không vừa cả icon: LẬT TRANG - chip chữ ngắn đủ cỡ chạm + nút "<" / ">" (không xuống dòng, không chồng)
            muc = 1;
            int wm = rongMuiTen(hc);
            if (chon >= 0 && chon < trangChip) {
                trangChip = chon;
            }
            trangChip = Math.max(0, Math.min(n - 1, trangChip));
            while (true) {
                dau = trangChip;
                int con = w - (dau > 0 ? wm + k : 0);
                cuoi = dau;
                while (cuoi < n) {
                    int wc = rongChip(1, cuoi, chon, hc);
                    int canPhai = cuoi + 1 < n ? wm + k : 0;
                    if (wc + (cuoi > dau ? k : 0) + canPhai > con && cuoi > dau) {
                        break;
                    }
                    con -= wc + (cuoi > dau ? k : 0);
                    cuoi++;
                }
                if (chon < cuoi || chon < 0 || trangChip >= n - 1) {
                    break;
                }
                trangChip++;
            }
            tong = (dau > 0 ? wm + k : 0) + (cuoi < n ? wm + k : 0) - k;
            for (int i = dau; i < cuoi; i++) {
                tong += rongChip(1, i, chon, hc) + k;
            }
        }
        gonMucNhan = dau > 0 || cuoi < n ? 4 : muc;
        int soMuc = (cuoi - dau) + (dau > 0 ? 1 : 0) + (cuoi < n ? 1 : 0);
        int du = Math.max(0, w - tong);
        int xx = x;
        int ic = coIconChip(hc);
        int thu = 0;
        if (dau > 0) {
            final int lui = dau;
            int wc = rongMuiTen(hc) + du / soMuc + (thu < du % soMuc ? 1 : 0);
            veChip(g, xx, y, wc, hc, muiTen(false), -1, false, new game.model.IAction() {
                public void perform() {
                    trangChip = Math.max(0, lui - 1);
                }
            }, "loc-truoc");
            xx += wc + k;
            thu++;
        }
        for (int i = dau; i < cuoi; i++) {
            int wc = rongChip(muc, i, chon, hc) + du / soMuc + (thu < du % soMuc ? 1 : 0);
            if (i == cuoi - 1 && cuoi == n) {
                wc = Math.max(wc, x + w - xx);
            }
            boolean c = i == chon;
            String chu = muc == 0 ? LOC[i] : muc == 1 || (muc == 2 && c) ? LOC_NGAN[i] : null;
            veChip(g, xx, y, wc, hc, chu, muc >= 2 ? ICON_LOC[i] : -1, c, viec == null ? null : viec[i], Bang.tenTab("loc-", i));
            xx += wc + k;
            thu++;
        }
        if (cuoi < n) {
            final int toi = cuoi;
            veChip(g, xx, y, Math.max(rongMuiTen(hc), x + w - xx), hc, muiTen(true), -1, false, new game.model.IAction() {
                public void perform() {
                    trangChip = toi;
                }
            }, "loc-sau");
        }
    }

    /** Một chip lọc (x, y, w, hc): chọn = cam chữ trắng viền, thường = giấy da chữ tối; icon (>= 0) PNG trước chữ. */
    static void veChip(Graphics g, int x, int y, int w, int hc, String chu, int icon, boolean c, Object v, String ten) {
        boolean an = v != null && Bang.dangAn(g, x, y, w, hc);
        int m = c ? KhungNroBang.NUT_CAM : KhungNroBang.NUT_GIAY;
        if (!KhungNro.ve(g, an ? m + 1 : m, x, y, w, hc)) {
            GiaoDien.khoi(g, x, y, w, hc, 3, c ? ChuDe.CHINH : ChuDe.KE);
        }
        int dy = an ? 1 : 0;
        int ic = coIconChip(hc);
        int wNoi = (icon >= 0 ? ic : 0) + (chu != null ? ChuDe.rong(chu, ChuDe.NHAN) + (icon >= 0 ? 4 : 0) : 0);
        int xn = x + (w - wNoi) / 2;
        if (icon >= 0) {
            BieuTuong.ve(g, icon, xn, y + (hc - ic) / 2 + dy, ic, ChuDe.CHU, ChuDe.NEN);
            xn += ic + 4;
        }
        if (chu != null) {
            if (c) {
                ChuDe.chuVienGiua(g, chu, xn, y + dy, hc, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 0);
            } else {
                ChuDe.chuGiua(g, chu, xn, y + dy, hc, ChuDe.NHAN, ChuDe.CHU, 0);
            }
        }
        VungCham.them(g, x, y, w, hc, v, ten);
    }

    /**
     * Bảng hành trang GỌN (0.9.18, chủ dự án: "lộn xộn, nút to"): đầu = MỘT hàng chip lọc nhỏ (hoặc đầu riêng của màn khi
     * viecLoc null - màn tự vẽ trong {@link #gonCaoDau}), giữa = lưới ô cạnh s chiếm gần hết chiều cao + thanh cuộn mảnh,
     * đáy = hàng ví gọn (xu, lượng, lượng khoá) + đếm ô. Chip, lưới, ví cùng mép trái / phải.
     */
    public static void veGon(Graphics g, Cuon c, int x, int y, int w, int h, int s, int loc, final Vector mon, boolean coKhoa,
            Object chon, final Object[] viecLoc, final Bam bam, int nguCanh) {
        capNhatCho();
        int k = Bang.cach();
        if (!KhungNro.ve(g, KhungNroBang.THE, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, ChuDe.KE);
        }
        Font_nenSang();
        MainChar m = mc();
        // lưới: cột theo bề ngang (chừa 10 px thanh cuộn), canh giữa; chip + ví cùng nhịp mép với lưới
        int dem0 = 5;
        int trong = w - 2 * dem0;
        final int cot = Math.max(1, (trong - 10 + k) / (s + k));
        int wLuoi = cot * (s + k) - k;
        int xl = x + dem0 + Math.max(0, (trong - 10 - wLuoi) / 2);
        int wNhip = wLuoi + 10;   // mép phải = mép phải thanh cuộn
        int hc = caoChipGon();
        int yy = y + 4;
        gonCaoDau = hc;
        if (viecLoc != null) {
            hangChipGon(g, xl, yy, wNhip, hc, loc, viecLoc);
        }
        GON_CHIP[0] = xl;
        GON_CHIP[1] = yy;
        GON_CHIP[2] = wNhip;
        GON_CHIP[3] = hc;
        yy += hc + k;
        int suc = TrangThaiNguoiChoi.sucChua(m);
        String dem = soDung() + "/" + suc;
        int wDem = ChuDe.rong(dem, ChuDe.SO) + 12;
        int wVi = wNhip - wDem - k;
        // mỗi mục ví là vùng chạm >= cỡ chạm: hàng hẹp (điện thoại 16:9 + lượng khoá) bỏ ô đếm (lưới đã cho thấy ô trống /
        // ô khoá) để ví đủ chỗ
        int soMucVi = ViTien.coKhoa(m) ? 3 : 2;
        boolean coDem = wVi - 8 >= soMucVi * (VungCham.MIN + k);
        if (!coDem) {
            wDem = 0;
            wVi = wNhip;
        }
        int soDVi = ViTien.soDong(wVi);
        if (soDVi > 1 && coDem) {
            // ví xuống dòng (J2ME 320x240): bỏ ô đếm trước khi lấy chiều cao của lưới
            coDem = false;
            wDem = 0;
            wVi = wNhip;
            soDVi = ViTien.soDong(wVi);
        }
        int hVi = ViTien.cao() * soDVi + (soDVi - 1) * k;
        int yVi = y + h - 4 - hVi;
        ViTien.hang(g, xl, yVi, wVi, hVi);
        if (coDem) {
            GiaoDien.khoiPixel(g, xl + wNhip - wDem, yVi, wDem, hVi, soDung() >= suc ? ChuDe.DO : 0x5C2E12);
            ChuDe.chuVienGiua(g, dem, xl + wNhip - wDem / 2, yVi, hVi, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
        }
        GON_VI[0] = xl;
        GON_VI[1] = yVi;
        GON_VI[2] = wNhip;
        GON_VI[3] = hVi;
        final int n = mon.size();
        int tran = TrangThaiNguoiChoi.tran(m);
        int hl = yVi - k - yy;
        int hangDay = Math.max(1, (hl + k) / (s + k));
        gonCot = cot;
        gonHang = hangDay;
        GON_LUOI[0] = xl;
        GON_LUOI[1] = yy;
        GON_LUOI[2] = wNhip;
        GON_LUOI[3] = hl;
        int dung = coKhoa ? soDung() : n;
        final int soTrong = coKhoa ? Math.max(0, suc - dung) : 0;
        final int soKhoa = coKhoa ? Math.max(0, tran - Math.max(suc, dung)) : 0;
        soKhoaVe = soKhoa;
        int tong = n + soTrong + soKhoa;
        tong = Math.max(((tong + cot - 1) / cot) * cot, hangDay * cot);
        final int tongO = tong;
        final Object chonO = chon;
        final int x0 = xl, s0 = s;
        nguCanhVe = nguCanh;
        luoiLuc = System.currentTimeMillis();
        soO = 0;
        Bang.danhSach(g, c, xl, yy, wNhip, hl, (tongO + cot - 1) / cot, s, new Bang.Hang() {
            public void ve(Graphics g, int i, int xr, int yr, int wr, int hr) {
                for (int q = 0; q < cot; q++) {
                    int j = i * cot + q;
                    if (j >= tongO) {
                        break;
                    }
                    int xo = x0 + q * (s0 + Bang.cach());
                    if (j < n) {
                        final Object o = mon.elementAt(j);
                        veO(g, o, xo, yr, s0, o == chonO, new IAction() {
                            public void perform() {
                                bamO(o, bam);
                            }
                        }, tenO("tui-", TEN_TUI, j));
                        if (soO < SO_O) {
                            OX[soO] = xo + g.getTranslateX();
                            OY[soO] = yr + g.getTranslateY();
                            OS[soO] = s0;
                            OM[soO] = o;
                            soO++;
                        }
                    } else if (j < n + soTrong || j >= n + soTrong + soKhoa) {
                        Bang.oDo(g, xo, yr, s0, false, false, null, null);
                    } else {
                        Bang.oKhoa(g, xo, yr, s0, new IAction() {
                            public void perform() {
                                hoiMoRong();
                            }
                        }, tenO("tui-khoa-", TEN_KHOA, j));
                    }
                }
            }
        });
        if (n == 0 && viecLoc != null) {
            dongTrong(g, xl, yy + Math.min(hl / 3, 2 * (s + k)), wNhip, loc);
        }
        yLuoi = yy;
        hLuoi = hl;
    }

    /** Dòng trống thân thiện (bộ lọc không có món) giữa lưới: bảng tối chỉ vàng + chữ. */
    static void dongTrong(Graphics g, int x, int y, int w, int loc) {
        String c = loc == LOC_TAT_CA ? "Hành trang trống - nhặt đồ từ Pokémon hoang dã hoặc mua ở Poké Mart."
                : loc == LOC_THUOC ? "Chưa có bình thuốc - mua ở Poké Mart (Chansey)."
                : loc == LOC_DO ? "Chưa có trang bị - đánh Pokémon hoang dã hoặc mua ở cửa hàng."
                : loc == LOC_NGUYEN_LIEU ? "Chưa có nguyên liệu - rơi từ Pokémon hoang dã, khai thác."
                : "Không có vật phẩm khác.";
        dongTrong(g, x, y, w, c);
    }

    /** Số ô khoá của lưới vừa vẽ (kiểm thử: lưới "Tất cả" có ô khoá cuối tới trần mở rộng). */
    public static int soKhoaVe;

    /** Đỉnh / cao vùng lưới vừa vẽ (màn đặt dòng trống riêng). */
    public static int yLuoi, hLuoi;

    /** Dòng trống với chữ c. */
    public static void dongTrong(Graphics g, int x, int y, int w, String c) {
        String[] d = ChuDe.ngat(c, ChuDe.NHAN, w - 24, 3);
        int lh = ChuDe.cao(ChuDe.NHAN);
        int h = d.length * lh + 10;
        if (!KhungNro.ve(g, KhungNroBang.TOOLTIP, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 2, 0x2B1A10);
        }
        for (int i = 0; i < d.length; i++) {
            ChuDe.chu(g, d[i], x + w / 2, y + 5 + i * lh, ChuDe.NHAN, 0xF6E6C0, 2);
        }
    }

    private static void Font_nenSang() {
        game.render.screen.Font.nenSang = true;
    }

    /** true = ô bình không ghi số trong túi (ô phần thưởng tự ghi số nhận). */
    static boolean boSo;

    /** Một ô đồ: hộp gỗ, viền độ hiếm, icon thật, số lượng, +N, khoá. */
    public static void veO(Graphics g, Object o, int x, int y, int s, boolean chon, Object viec, String ten) {
        Bang.oDo(g, x, y, s, true, chon, viec, ten);
        int cx = x + s / 2, cy = y + s / 2, ic = s - 10;
        if (o instanceof ItemInInventory) {
            ItemInInventory it = (ItemInInventory) o;
            ItemTemplate t = ManNhanVat.mau(it);
            int hiem = KieuO.mauHiem(it.colorName);
            if (hiem >= 0 && !chon) {
                g.setColor(hiem);
                g.drawRect(x + 1, y + 1, s - 3, s - 3);
                g.drawRect(x + 2, y + 2, s - 5, s - 5);
            }
            if (t != null) {
                Bang.icon(g, t.idIcon, cx, cy, ic >= 22 ? ic / 22f : 1f);
            }
            if (it.plusTemplate > 0) {
                KieuO.capCuong(g, x + 3, y + 3, it.plusTemplate);
            }
            if (it.lock == 1) {
                oKhoaNho(g, x + s - 9, y + 3);
            }
        } else if (o instanceof Integer) {
            int loai = ((Integer) o).intValue();
            ManNhanVat.veThuoc(g, loai, cx, cy, ic);
            MainChar m = mc();
            if (!boSo) {
                soLuong(g, x, y, s, m == null || m.potions == null ? 0 : m.potions[loai]);
            }
        } else if (o instanceof GemTemplate) {
            GemTemplate gt = (GemTemplate) o;
            if (laDacBiet(o)) {
                GemTemp t = Res.getShopByID(gt.id);
                if (t != null) {
                    IconVatPham.ve(g, (short) (t.idImage + 5500), cx, cy, ic, IconVatPham.DP_DO);
                }
            } else {
                GemTemp t = Res.getGemByID(gt.id);
                if (t != null) {
                    IconVatPham.ve(g, (short) (t.idImage + 6500), cx, cy, ic, IconVatPham.DP_NGOC);
                }
                if (laKhoa(o)) {
                    oKhoaNho(g, x + s - 9, y + 3);
                }
            }
            soLuong(g, x, y, s, gt.number);
        } else if (o instanceof GemTemp) {
            // món cửa hàng đặc biệt (gói cửa hàng: dải ảnh 5500)
            IconVatPham.ve(g, (short) (((GemTemp) o).idImage + 5500), cx, cy, ic, IconVatPham.DP_DO);
        } else if (o instanceof int[]) {
            int id = ((int[]) o)[0];
            if (id < game.model.ItemQuest.ICON_IMG.length) {
                IconVatPham.ve(g, (short) (game.model.ItemQuest.ICON_IMG[id] + 8000), cx, cy, ic, IconVatPham.DP_NV);
            }
            soLuong(g, x, y, s, MainChar.itemQuest[id]);
            GiaoDien.khoiPixel(g, x + 3, y + 3, 7, 7, ChuDe.VANG);   // dấu vật phẩm nhiệm vụ
        }
    }

    /** Số lượng góc dưới phải (chữ trắng viền). */
    static void soLuong(Graphics g, int x, int y, int s, int n) {
        if (n <= 1) {
            return;
        }
        String c = KieuO.soGon(n);
        ChuDe.chuVien(g, c, x + s - 4, y + s - 4 - ChuDe.cao(ChuDe.NHAN_NHO), ChuDe.NHAN_NHO, 0xFFFFFF, ChuDe.VIEN, 1);
    }

    /** Ổ khoá pixel nhỏ 6x7 (vật phẩm khoá / không giao dịch). */
    static void oKhoaNho(Graphics g, int x, int y) {
        g.setColor(ChuDe.VIEN);
        g.fillRect(x, y + 2, 7, 6);
        g.fillRect(x + 1, y, 5, 3);
        g.setColor(0xC8C8D0);
        g.fillRect(x + 2, y + 1, 3, 2);
        g.setColor(ChuDe.VANG);
        g.fillRect(x + 1, y + 3, 5, 4);
        g.setColor(ChuDe.VIEN);
        g.fillRect(x + 3, y + 4, 1, 2);
    }

    // ------------------------------------------------------------------ mở rộng

    /** Chạm ô khoá: dùng vé nếu có, không thì hỏi mua bằng xu (giá leo thang theo gói -105). */
    public static void hoiMoRong() {
        MainChar m = mc();
        int suc = TrangThaiNguoiChoi.sucChua(m);
        int tran = TrangThaiNguoiChoi.tran(m);
        int moi = Math.max(1, Math.min(TrangThaiNguoiChoi.oMoiVe, tran - suc));
        if (TrangThaiNguoiChoi.oDaMo < 0) {
            DungDo.bao("Máy chủ chưa hỗ trợ mở rộng hành trang bằng vé. Hỏi Kangaskhan ở thị trấn.");
            return;
        }
        if (suc >= tran) {
            DungDo.loi("Hành trang đã mở rộng tối đa (" + tran + " ô).");
            return;
        }
        if (TrangThaiNguoiChoi.soVe > 0) {
            HopXacNhan.hoi("Mở rộng hành trang", "Dùng 1 vé mở rộng hành trang (còn " + TrangThaiNguoiChoi.soVe + " vé) để mở thêm " + moi
                    + " ô (" + suc + " -> " + (suc + moi) + ")?", "Dùng vé", new IAction() {
                public void perform() {
                    TrangThaiNguoiChoi.guiMoRong(1);
                }
            }, null);
            return;
        }
        final long gia = TrangThaiNguoiChoi.giaXuMoRong;
        if (gia < 0) {
            DungDo.loi("Chưa mở rộng thêm được. Vé mở rộng hành trang có ở cửa hàng đặc biệt.");
            return;
        }
        HopXacNhan.hoiGia("Mở rộng hành trang", "Mở thêm " + moi + " ô (" + suc + " -> " + (suc + moi)
                + "). Giá tăng dần mỗi lần mở; vé mở rộng có ở cửa hàng đặc biệt.", "Mở rộng", TienTe.XU, gia, new IAction() {
            public void perform() {
                MainChar mm = mc();
                if (mm != null && mm.charXu < gia) {
                    DungDo.loi("Không đủ xu (cần " + TienTe.so(gia) + ").");
                    return;
                }
                TrangThaiNguoiChoi.guiMoRong(2);
            }
        });
    }
}
