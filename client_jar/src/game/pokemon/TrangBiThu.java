package game.pokemon;

import game.model.AnimalInfo;
import game.model.Char;
import game.model.Command;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.render.Canvas;
import game.render.screen.Screen;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * BẢNG "TRANG BỊ THÚ" (0.9.30, chủ dự án: "APK và JAR không có mấy cái đó" - không tìm được chỗ XEM thú đang mặc gì,
 * cách THÁO / THAY đồ thú). Một danh sách dùng chung cho APK / PC (chạm) và J2ME (phím, 240x320):
 * <ul>
 *   <li>hàng "Thú cưỡi" + "Đi theo" (con đang dùng) - chọn = đổi thú (màn Pokémon › tab Thú) / xuống thú;</li>
 *   <li>một hàng cho MỖI ô đồ thú (Yên cương, Giáp cưỡi, Mũ cưỡi, Vòng chân, Bàn đạp, Skin) với món đang mặc;</li>
 *   <li>chọn hàng có đồ = menu Xem / Tháo / Thay; ô trống = Trang bị. Tháo khi hành trang đầy = báo ngay, không gửi.</li>
 *   <li>Thay / Trang bị = hành trang LỌC đúng loại ô; chọn món = gửi lệnh 29 (máy chủ Map.exchangeItemAnimal tự trả món
 *   cũ về hành trang). Tháo = lệnh 29 với id món đang mặc (Map.thaoTrangBiThu).</li>
 * </ul>
 * Lối vào: Menu › Pokémon › tab Thú (nút "Trang bị thú") và Trang bị › ô Thú cưỡi; thẻ vật phẩm đồ thú trong hành trang
 * (nút "Đồ thú"); J2ME Bản thân › Trang bị thú; cửa sổ cũ tab Trang bị thú (chọn ô).
 */
public final class TrangBiThu extends Screen {
    static TrangBiThu me;

    /** ItemTemplate.type của từng ô đồ thú (máy chủ Map.isWearingAnimal 14..18 + skin SkinThuCuoi.TYPE_ITEM 30). */
    public static final int[] LOAI = {18, 14, 16, 15, 17, 30};
    public static final String[] TEN = {"Yên cương", "Giáp cưỡi", "Mũ cưỡi", "Vòng chân", "Bàn đạp", "Skin thú cưỡi"};
    static final int[] HINH = {BieuTuong.DAI, BieuTuong.AO, BieuTuong.MU, BieuTuong.VONG_CO, BieuTuong.GIAY, BieuTuong.THU_CUNG};
    /** hàng 0 = thú cưỡi, 1 = đi theo, 2.. = ô đồ */
    public static final int HANG_CUOI = 0, HANG_DI_THEO = 1, HANG_O = 2;

    /** hàng đang chọn bằng phím (danh sách ô hoặc danh sách món thay) */
    public int chon;
    /** ô đang chọn món thay (-1 = danh sách ô) */
    public int oThay = -1;
    final Cuon ds = new Cuon();
    Screen truoc;
    /** vùng danh sách lần vẽ cuối (cuộn theo phím) */
    int yDs, hDs, rhDs;
    /** kiểm thử: số hàng vừa vẽ, tiêu đề vừa vẽ */
    public static int soHangVe;
    public static String tieuDeVe = "";

    public static TrangBiThu gI() {
        if (me == null) {
            me = new TrangBiThu();
        }
        return me;
    }

    /** Mở bảng (danh sách ô). Đóng = quay về màn đang mở (màn Pokémon / màn chơi). */
    public static void mo() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        TrangBiThu m = gI();
        Screen s = Canvas.currentScreen;
        if (s != m) {
            // cửa sổ cũ / menu chữ: đóng về màn chơi; màn Pokémon (ManNhanVat) thì quay lại đó
            m.truoc = s instanceof ManNhanVat ? s : null;
        }
        m.oThay = -1;
        m.chon = HANG_O;
        m.ds.den(0);
        Canvas.menu.showMenu = false;
        Canvas.endDlg();
        if (TheVatPham.dangMo()) {
            TheVatPham.gI().dong();
        }
        m.datLenh();
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    /** Mở bảng, chọn sẵn ô nhận loại {@code type} và mở menu của ô đó (cửa sổ cũ tab Trang bị thú). */
    public static void moTaiLoai(int type) {
        mo();
        int o = oCuaLoai(type);
        if (o >= 0) {
            gI().chon = HANG_O + o;
            gI().bam(HANG_O + o);
        }
    }

    /** Đang mở bảng (kiểm thử). */
    public static boolean dangMo() {
        return me != null && Canvas.currentScreen == me;
    }

    void datLenh() {
        this.anNutLenh = MenuNguCanh.bat();   // máy chạm: nút trong bảng; phím (Enter / Esc / phím mềm) vẫn chạy
        this.left = null;
        this.center = new Command("Chọn", new IAction() {
            public void perform() {
                TrangBiThu.this.bam(TrangBiThu.this.chon);
            }
        });
        this.right = new Command(this.oThay >= 0 ? "Lùi" : "Đóng", new IAction() {
            public void perform() {
                TrangBiThu.this.lui();
            }
        });
    }

    void dong() {
        AmThanh.phat(AmThanh.XAC_NHAN);
        Screen t = this.truoc;
        this.truoc = null;
        if (t != null) {
            t.switchToMe();
        } else {
            Canvas.gameScr.switchToMe();
        }
    }

    /** Lùi: danh sách món thay -> danh sách ô -> đóng. */
    public void lui() {
        if (this.oThay >= 0) {
            this.chon = HANG_O + this.oThay;
            this.oThay = -1;
            this.ds.den(0);
            datLenh();
            return;
        }
        dong();
    }

    // ------------------------------------------------------------------ dữ liệu

    static MainChar mc() {
        return Canvas.gameScr.mainChar;
    }

    /** Ô nhận loại type (-1 = không phải đồ thú). */
    public static int oCuaLoai(int type) {
        for (int i = 0; i < LOAI.length; i++) {
            if (LOAI[i] == type) {
                return i;
            }
        }
        return -1;
    }

    /** Vật phẩm là đồ thú cưỡi (mặc ở bảng này). */
    public static boolean laDoThu(ItemInInventory it) {
        ItemTemplate t = it == null ? null : ManNhanVat.mau(it);
        return t != null && oCuaLoai(t.type) >= 0;
    }

    /** Đang cưỡi thú (đồ thú chỉ mặc / hiện khi đang cưỡi - máy chủ gửi gói 15 len -1 khi không cưỡi). */
    public static boolean dangCuoi() {
        return Canvas.gameScr != null && Canvas.gameScr.mainChar != null && ManNhanVat.dangCuoi(mc());
    }

    /** Món đang mặc ở ô o (null = trống / không cưỡi). */
    public static ItemInInventory monO(int o) {
        MainChar m = mc();
        if (o < 0 || o >= LOAI.length || m.wearingAnimal == null || !dangCuoi()) {
            return null;
        }
        for (int k = 0; k < m.wearingAnimal.size(); k++) {
            Object x = m.wearingAnimal.elementAt(k);
            if (!(x instanceof ItemInInventory)) {
                continue;
            }
            ItemTemplate t = ManNhanVat.mau((ItemInInventory) x);
            if (t != null && t.type == LOAI[o]) {
                return (ItemInInventory) x;
            }
        }
        return null;
    }

    /** Hành trang lọc theo ô o (đúng loại; món đủ cấp trước, thiếu cấp sau). */
    public static Vector monThay(int o) {
        Vector du = new Vector(), thieu = new Vector();
        if (o < 0 || o >= LOAI.length) {
            return du;
        }
        int cap = mc().level;
        for (int k = 0; k < Char.inventory.size(); k++) {
            Object x = Char.inventory.elementAt(k);
            if (!(x instanceof ItemInInventory)) {
                continue;
            }
            ItemInInventory it = (ItemInInventory) x;
            ItemTemplate t = ManNhanVat.mau(it);
            if (t == null || t.type != LOAI[o]) {
                continue;
            }
            (ManNhanVat.capCan(it, t) <= cap ? du : thieu).addElement(it);
        }
        for (int k = 0; k < thieu.size(); k++) {
            du.addElement(thieu.elementAt(k));
        }
        return du;
    }

    static String tenMon(ItemInInventory it) {
        ItemTemplate t = ManNhanVat.mau(it);
        return (t == null ? "?" : ChuDe.boCap(t.name)) + (it.plusTemplate > 0 ? " +" + it.plusTemplate : "");
    }

    /** Dấu ngăn: font ảnh J2ME không có "·". */
    static String ngan() {
        return ChuUI.bo != null ? " · " : " - ";
    }

    /** Tên thú cưỡi đang cưỡi ("" = không cưỡi). */
    static String tenCuoi() {
        if (!dangCuoi()) {
            return "";
        }
        AnimalInfo a = ManNhanVat.thuDangCuoi();
        String[] t = ManNhanVat.tachTen(a != null ? a.name : (QuanLyThu.coDung && QuanLyThu.thuCu != null ? QuanLyThu.thuCu : mc().infoAnimal));
        return t[0].length() > 0 ? t[0] : "Thú cưỡi";
    }

    /** Tên Pokémon đi theo ("" = không có). */
    static String tenDiTheo() {
        AnimalInfo a = ManNhanVat.petDiTheo();
        if (a != null) {
            String[] t = ManNhanVat.tachTen(a.name);
            return t[0].length() > 0 ? t[0] : "Pokémon";
        }
        MainChar m = mc();
        if (!QuanLyThu.coDung && m.pokePetDex > 0) {
            return m.pokePetTen != null ? m.pokePetTen : "Pokémon";
        }
        return "";
    }

    /** Hành trang đã đầy (tháo ra không có chỗ). */
    static boolean tuiDay() {
        MainChar m = mc();
        return m.FullInventory() >= TrangThaiNguoiChoi.sucChua(m);
    }

    // ------------------------------------------------------------------ thao tác

    int soHang() {
        return this.oThay >= 0 ? monThay(this.oThay).size() : HANG_O + LOAI.length;
    }

    /** Chọn hàng i (chạm hoặc phím giữa). */
    public void bam(int i) {
        this.chon = i;
        if (this.oThay >= 0) {
            Vector v = monThay(this.oThay);
            if (i >= 0 && i < v.size()) {
                mac((ItemInInventory) v.elementAt(i));
            }
            return;
        }
        if (i == HANG_CUOI || i == HANG_DI_THEO) {
            menuThu(i == HANG_CUOI);
        } else if (i >= HANG_O && i < HANG_O + LOAI.length) {
            menuO(i - HANG_O);
        }
    }

    static void moLenh(Vector v, String tieuDe) {
        if (MenuNguCanh.bat()) {
            MenuNguCanh.mo(v, tieuDe);
        } else {
            Canvas.menu.startAt(v, 3);
        }
    }

    void menuThu(final boolean cuoi) {
        Vector v = new Vector();
        final String ten = cuoi ? tenCuoi() : tenDiTheo();
        v.addElement(new Command(cuoi ? (ten.length() > 0 ? "Đổi thú cưỡi" : "Chọn thú cưỡi") : (ten.length() > 0 ? "Đổi Pokémon đi theo"
                : "Gọi Pokémon đi theo"), new IAction() {
            public void perform() {
                Canvas.endDlg();
                TrangBiThu.this.moDsThu(cuoi);
            }
        }));
        if (cuoi && ten.length() > 0) {
            v.addElement(new Command("Xuống thú", new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    ManNhanVat.xuongThu(ten);
                }
            }));
        }
        moLenh(v, cuoi ? "Thú cưỡi" : "Đi theo");
    }

    /** Danh sách thú (màn Pokémon › tab Thú, tab con Thú cưỡi / Đi theo). */
    void moDsThu(boolean cuoi) {
        this.truoc = null;
        ManNhanVat.mo(ManNhanVat.TAB_THU);
        ManNhanVat.gI().doiLoaiThu(cuoi ? QuanLyThu.LOAI_CUOI : QuanLyThu.LOAI_DI_THEO);
    }

    void menuO(final int o) {
        final ItemInInventory it = monO(o);
        Vector v = new Vector();
        if (!dangCuoi()) {
            v.addElement(new Command("Chọn thú cưỡi", new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    TrangBiThu.this.moDsThu(true);
                }
            }));
            DungDo.bao("Đồ thú chỉ mặc / tháo khi đang cưỡi thú. Hãy cưỡi thú trước.", DungDo.TIN);
            moLenh(v, TEN[o]);
            return;
        }
        if (it != null) {
            v.addElement(new Command("Xem", new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    TrangBiThu.xem(it);
                }
            }));
            v.addElement(new Command("Tháo", new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    TrangBiThu.thao(it);
                }
            }));
            v.addElement(new Command("Thay", new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    TrangBiThu.this.moThay(o);
                }
            }));
        } else {
            v.addElement(new Command("Trang bị", new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    TrangBiThu.this.moThay(o);
                }
            }));
        }
        moLenh(v, TEN[o] + (it != null ? ": " + tenMon(it) : ""));
    }

    /** Hành trang lọc theo ô o (Thay / Trang bị). */
    public void moThay(int o) {
        this.oThay = o;
        this.chon = 0;
        this.ds.den(0);
        datLenh();
    }

    static void xem(ItemInInventory it) {
        if (!it.isEnoughData && it.allAttribute.size() == 0) {
            it.isEnoughData = true;
            try {
                game.networklogic.GameService.gI().requestItemInfo(it.itemID, mc().ID);
            } catch (Throwable e) {
                // chưa nối máy chủ
            }
        }
        TheVatPham.mo(it, TheVatPham.TUI, true, null);   // thẻ đồ đang mặc: nút Tháo
    }

    /** Tháo món đang mặc về hành trang (hành trang đầy: báo, không gửi). */
    public static void thao(ItemInInventory it) {
        if (it == null) {
            return;
        }
        if (tuiDay()) {
            DungDo.loi("Hành trang đầy, không tháo được " + tenMon(it) + ". Hãy bỏ bớt đồ.");
            return;
        }
        DungDo.thao(it);
    }

    /** Mặc món it (Thay: máy chủ trả món cũ cùng ô về hành trang). */
    void mac(ItemInInventory it) {
        ItemTemplate t = ManNhanVat.mau(it);
        int can = ManNhanVat.capCan(it, t);
        if (can > mc().level) {
            DungDo.loi("Cần cấp " + can + " để dùng " + tenMon(it) + ".");
            return;
        }
        if (!dangCuoi()) {
            DungDo.loi("Phải cưỡi thú mới trang bị được " + tenMon(it) + ".");
            return;
        }
        DungDo.dungItem(it);
        lui();
    }

    // ------------------------------------------------------------------ vòng game

    public void update() {
        boolean c = Canvas.isPointerClick, d = Canvas.isPointerDown;
        Canvas.isPointerClick = false;
        Canvas.isPointerDown = false;
        try {
            Canvas.gameScr.update();
        } catch (Throwable e) {
            // bỏ qua
        }
        Canvas.isPointerClick = c;
        Canvas.isPointerDown = d;
        if (Canvas.currentScreen != this || Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return;
        }
        int n = soHang();
        if (n <= 0) {
            this.chon = 0;
            return;
        }
        if (Canvas.isKeyPressed(2)) {
            this.chon = (this.chon + n - 1) % n;
            canChon();
        } else if (Canvas.isKeyPressed(8)) {
            this.chon = (this.chon + 1) % n;
            canChon();
        }
        if (this.chon >= n) {
            this.chon = n - 1;
        }
    }

    /** Cuộn để hàng đang chọn luôn thấy (phím). */
    void canChon() {
        if (this.rhDs <= 0 || this.hDs <= 0) {
            return;
        }
        int k = Bang.cach();
        int top = this.chon * (this.rhDs + k);
        int lech = this.ds.lech();
        if (top < lech) {
            this.ds.den(top);
        } else if (top + this.rhDs > lech + this.hDs) {
            this.ds.den(top + this.rhDs - this.hDs);
        }
    }

    public boolean keyPress(int keyCode) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;
        }
        if (keyCode == -7 || keyCode == 27 || keyCode == -8) {
            lui();
            return true;
        }
        return false;
    }

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);
        } catch (Throwable e) {
            // bỏ qua
        }
        Canvas.resetTrans(g);
        Bang.phuNen(g);
        String td = this.oThay >= 0 ? "Chọn " + TEN[this.oThay] : "Trang bị thú";
        tieuDeVe = td;
        int[] c = Bang.khung(g, td, null, 0, null, new IAction() {
            public void perform() {
                TrangBiThu.this.lui();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        Bang.the(g, x, y, w, h);
        // J2ME: phím mềm Chọn / Đóng vẽ đè đáy màn - danh sách dừng trên hàng phím (hàng cuối không bị che)
        int day = this.anNutLenh ? 0 : Math.max(0, y + h - (Canvas.h - 24));
        if (this.oThay >= 0) {
            veThay(g, x + 4, y + 4, w - 8, h - 8 - day);
        } else {
            veO(g, x + 4, y + 4, w - 8, h - 8 - day);
        }
        Canvas.resetTrans(g);
        if (!this.anNutLenh) {
            super.paint(g);   // J2ME: phím mềm Chọn / Đóng
        }
    }

    /** Dòng gợi ý trên danh sách (1-2 dòng); trả chiều cao đã dùng. */
    static int goiY(Graphics g, int x, int y, int w, String s, int mau) {
        String[] d = ChuDe.ngat(s, ChuDe.PHU, w - 4, 2);
        int lp = ChuDe.cao(ChuDe.PHU);
        for (int i = 0; i < d.length; i++) {
            ChuDe.chu(g, d[i], x + 2, y + i * lp, ChuDe.PHU, mau, 0);
        }
        return d.length * lp + 2;
    }

    void veO(Graphics g, int x, int y, int w, int h) {
        boolean cuoi = dangCuoi();
        String gy = cuoi ? (MenuNguCanh.bat() ? "Chạm một ô: Xem, Tháo, Thay đồ của thú đang cưỡi." : "Chọn một ô: Xem, Tháo, Thay.")
                : "Chưa cưỡi thú: đồ thú chỉ hiện và mặc được khi đang cưỡi.";
        int dy = goiY(g, x, y, w, gy, cuoi ? ChuDe.CHU_PHU : ChuDe.DO);
        int yl = y + dy;
        final int rh = ManXemTrangBi.caoHangMon();
        this.yDs = yl;
        this.hDs = y + h - yl;
        this.rhDs = rh;
        final int n = HANG_O + LOAI.length;
        soHangVe = n;
        final boolean cham = MenuNguCanh.bat();
        Bang.danhSach(g, this.ds, x, yl, w, y + h - yl, n, rh, new Bang.Hang() {
            public void ve(Graphics g, final int i, int x, int y, int w, int h) {
                boolean chonPhim = !cham && i == TrangBiThu.this.chon;
                Bang.hang(g, x, y, w, h, i, chonPhim, new IAction() {
                    public void perform() {
                        TrangBiThu.this.bam(i);
                    }
                }, "tbt-" + i);
                if (i < HANG_O) {
                    TrangBiThu.this.veHangThu(g, x, y, w, h, i == HANG_CUOI);
                } else {
                    TrangBiThu.this.veHangO(g, x, y, w, h, i - HANG_O);
                }
            }
        });
    }

    void veHangThu(Graphics g, int x, int y, int w, int h, boolean cuoi) {
        int s = ManNhanVat.oDau(g, x, y, h);
        MainChar m = mc();
        String ten = cuoi ? tenCuoi() : tenDiTheo();
        AnimalInfo a = cuoi ? ManNhanVat.thuDangCuoi() : ManNhanVat.petDiTheo();
        int cx = x + 3 + s / 2, cy = y + 3 + s / 2;
        if (a != null) {
            ManNhanVat.veHinhThu(g, ManNhanVat.dexThu(a), ManNhanVat.shinyThu(a), (short) (a.idPaint + 7500), cx, cy, s);
        } else if (cuoi && ten.length() > 0) {
            ManNhanVat.veHinhThu(g, m.pokeThuDex, m.pokeThuShiny, (short) (m.idPaintIconAnimal + 7500), cx, cy, s);
        } else {
            int b = s * 3 / 5;
            BieuTuong.veBong(g, cuoi ? BieuTuong.POKEBALL : BieuTuong.THU_CUNG, cx - b / 2, cy - b / 2, Math.max(b, 12), 0x8A6A4A, ChuDe.NEN_PHU);
        }
        String nhan = cuoi ? "Thú cưỡi" : "Đi theo";
        String phu = ten.length() > 0 ? ten : (cuoi ? "Chưa cưỡi" + ngan() + "chọn để cưỡi" : "Chưa có" + ngan() + "chọn để gọi");
        ManNhanVat.chuHang(g, x, y, w, h, s, nhan, null, phu);
    }

    void veHangO(Graphics g, int x, int y, int w, int h, int o) {
        ItemInInventory it = monO(o);
        if (it != null) {
            ManXemTrangBi.veHangMon(g, it, x, y, w, h, TEN[o] + ngan() + "Cấp " + ManNhanVat.capCan(it, ManNhanVat.mau(it)));
            return;
        }
        int s = ManNhanVat.oDau(g, x, y, h);
        int b = s * 3 / 5;
        BieuTuong.veBong(g, HINH[o], x + 3 + (s - b) / 2, y + 3 + (s - b) / 2, Math.max(b, 12), 0x8A6A4A, ChuDe.NEN_PHU);
        int coSan = dangCuoi() ? monThay(o).size() : 0;
        String phu = !dangCuoi() ? "Trống" : coSan > 0 ? "Trống" + ngan() + coSan + " món trong hành trang" : "Trống";
        ManNhanVat.chuHang(g, x, y, w, h, s, TEN[o], null, phu);
    }

    void veThay(Graphics g, int x, int y, int w, int h) {
        final int o = this.oThay;
        int k = Bang.cach();
        int hd = Bang.co(26);
        Bang.nutLui(g, x, y, hd, new IAction() {
            public void perform() {
                TrangBiThu.this.lui();
            }
        });
        ItemInInventory cu = monO(o);
        String dau = cu != null ? "Đang mặc: " + tenMon(cu) : TEN[o] + ": trống";
        ChuDe.chuGiua(g, ChuDe.cat(dau, ChuDe.NHAN, w - hd - 10), x + hd + 6, y, hd, ChuDe.NHAN, ChuDe.CHU, 0);
        int yl = y + hd + k;
        yl += goiY(g, x, yl, w, cu != null ? "Chọn món để thay - món cũ về hành trang." : "Chọn món để mặc cho thú.", ChuDe.CHU_PHU);
        final Vector v = monThay(o);
        final int rh = ManXemTrangBi.caoHangMon();
        this.yDs = yl;
        this.hDs = y + h - yl;
        this.rhDs = rh;
        soHangVe = v.size();
        if (v.size() == 0) {
            Bang.trong(g, x, yl, w, y + h - yl, "Không có " + TEN[o].toLowerCase() + " trong hành trang",
                    "Đồ thú cưỡi: cửa hàng đặc biệt, sự kiện, quà nhiệm vụ.");
            return;
        }
        final int cap = mc().level;
        final boolean cham = MenuNguCanh.bat();
        Bang.danhSach(g, this.ds, x, yl, w, y + h - yl, v.size(), rh, new Bang.Hang() {
            public void ve(Graphics g, final int i, int x, int y, int w, int h) {
                ItemInInventory it = (ItemInInventory) v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, !cham && i == TrangBiThu.this.chon, new IAction() {
                    public void perform() {
                        TrangBiThu.this.bam(i);
                    }
                }, "tbt-mon-" + i);
                int can = ManNhanVat.capCan(it, ManNhanVat.mau(it));
                ManXemTrangBi.veHangMon(g, it, x, y, w, h, can > cap ? "Cần cấp " + can : "Cấp " + can + ngan() + "chọn để mặc");
            }
        });
    }
}
