package game.render.screen;

import game.GameMidlet;
import game.model.Char;
import game.model.Command;
import game.model.GemTemp;
import game.model.IAction;
import game.model.Layer;
import game.model.PageScr;
import game.model.ShopNew;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Menu chính của game (phím mềm trái). Hàng đầu (tên Pokémon) = "Tự động đánh" (chính thức, máy chủ điều khiển:
 * TuDong + Nen.moBangTuDong), "Hiệu ứng", "Tiện ích". Mod cũ của game gốc (Xa phu từ xa, Chế đồ nhanh, Dòng ẩn, Auto up,
 * Vận tiêu, Vận may, PK) đã bỏ hẳn.
 */
public class PauseMenu
extends Screen {
    public static PauseMenu me;
    private String[][] name = new String[][]{{"", "T\u1ef1 \u0111\u1ed9ng \u0111\u00e1nh", "Hi\u1ec7u \u1ee9ng", "Ti\u1ec7n \u00edch", "Ngo\u1ea1i h\u00ecnh"}, {"C\u00e0i \u0111\u1eb7t", "C\u1ea5u h\u00ecnh", "B\u1ea3n \u0111\u1ed3 v\u00f9ng", "B\u1eadt/t\u1eaft giao di\u1ec7n", "H\u01b0\u1edbng d\u1eabn", "\u00c2m thanh", "B\u1eadt/t\u1eaft l\u1eddi m\u1eddi", "B\u1eadt/t\u1eaft auto \u0111\u00e1nh", "Ch\u1ebf \u0111\u1ed9 ch\u1ecdn m\u1ee5c ti\u00eau", "Gi\u1ea5y ph\u00e9p"}, {"B\u1ea3n th\u00e2n", "H\u00e0nh trang", "K\u1ef9 n\u0103ng", "Ti\u1ec1m n\u0103ng", "Trang b\u1ecb", "Th\u00f4ng tin", "Trang b\u1ecb th\u00fa", "C\u00e2y th\u1ea7n", "C\u1ed5 v\u1eadt", "Kho & ti\u1ec7n \u00edch", "T\u00fai sinh th\u00e1i"}, {"N\u1ea1p xu"}, {"Nhi\u1ec7m v\u1ee5", "S\u1ed5 nhi\u1ec7m v\u1ee5", "Nhi\u1ec7m v\u1ee5 c\u1ee7a t\u00f4i", "S\u1ed5 tay hu\u1ea5n luy\u1ec7n", "S\u1ef1 ki\u1ec7n"}, {"PK"}, {"Kh\u00e1c", "Tin nh\u1eafn", "Nh\u00f3m", "B\u1ea1n b\u00e8", "Top nh\u00e0 v\u00f4 \u0111\u1ecbch", "Top \u0111\u1ea1i gia", "B\u1ea3ng Top", "K\u00eanh th\u1ebf gi\u1edbi", "Di\u1ec5n \u0111\u00e0n", "R\u1eddi \u0111\u1ed9i"}, {""}, {"C\u1eeda h\u00e0ng"}, {""}};
    int selected = 0;
    int index = -1;
    int Limit;
    int start = 0;
    int maxOption = 5;
    public int cmtoY;
    public int cmy;
    public int cmdy;
    public int cmvy;
    public int cmyLim;
    public int x;
    public int y;
    public int w;
    public int h;
    public int hText;
    public Command cmdTimban;
    boolean trans = false;
    int pa = 0;
    /** 0.9.11: đang kéo núm thanh cuộn */
    boolean keoNum;
    int cuonNumDau, pyNumDau;
    byte frame;
    /** 0.9.6.1: dòng hiện thứ i -> chỉ số gốc (hàng đầu: name[g]; menu con: mục name[index][g + 1]). */
    int[] hien = new int[0];

    /**
     * 0.9.6.1 MỘT LỐI VÀO MỖI VIỆC: game.pokemon.ManMenu đặt cờ này ngay trước khi mở menu chữ từ viên "Tiện ích & PK"
     * (HUD PC / Android) -> ẩn mục đã có lối vào riêng ở Menu kiểu GO / HUD. Mở bằng phím mềm (J2ME bấm phím): đủ mục.
     */
    public static boolean moTuMenu;
    /** lần mở này là từ Menu kiểu GO */
    boolean gonLan;

    boolean gon() {
        return this.gonLan;
    }

    /** true = ẩn (chỉ khi gon): hang = hàng gốc; muc = mục con gốc, -1 = chính hàng. */
    boolean an(int hang, int muc) {
        if (!gon()) {
            return false;
        }
        switch (hang) {
            case 0:   // Tự động đánh = viên AUTO + bánh răng HUD; Ngoại hình = tab Ngoại hình màn Pokémon
                return muc == 0 || muc == 3;
            case 1:   // Bản đồ vùng = Menu › Bản đồ vùng / chạm bản đồ nhỏ HUD; auto đánh = nút Tự động HUD;
                      // âm thanh = Cài đặt -> Âm thanh (bảng Cài đặt PC; HUD APK giữ)
                return muc == 1 && game.pokemon.ManMenu.coBanDoVung() || muc == 6 || muc == 4 && HudRong.bat();
            case 2:   // Hành trang (HUD), Kỹ năng / Tiềm năng / Trang bị / Thông tin / Trang bị thú (màn Pokémon),
                      // Túi sinh thái (= Menu -> Hệ sinh thái). 0.9.17: "Kho & tiện ích" (menu Khác -10000 của máy chủ)
                      // HIỆN - Menu › Sự kiện không còn mở menu này
                return muc >= 0 && muc <= 5 || muc == 9;
            case 3:   // Nạp xu SMS: máy chủ không gửi cú pháp -> chỉ báo "tạm khóa"
                return Canvas.gameScr == null || Canvas.gameScr.decriptNap == null || Canvas.gameScr.decriptNap.size() == 0;
            case 4:   // Nhiệm vụ = nút HUD; Nhiệm vụ của tôi / Sổ tay = Menu kiểu GO (0.9.16)
            case 8:   // Cửa hàng = Menu -> Cửa hàng
                return true;
            case 6:   // Tin nhắn (Thư/Quà), Nhóm + Bạn bè (Đội/Bạn bè), 3 bảng Top (Xếp hạng), Rời đội (hàng Đội);
                      // Diễn đàn khi máy chủ không gửi địa chỉ (bấm không làm gì)
                return muc >= 0 && muc <= 5 || muc == 8 || muc == 7 && (GameMidlet.url == null || GameMidlet.url.length() == 0);
            default:
                return false;
        }
    }

    /** Hàng gốc i ẩn: tự nó ẩn hoặc mọi mục con đều ẩn. */
    boolean anHang(int i) {
        if (an(i, -1)) {
            return true;
        }
        if (this.name[i].length <= 1) {
            return false;
        }
        for (int j = 0; j < this.name[i].length - 1; ++j) {
            if (!an(i, j)) {
                return false;
            }
        }
        return true;
    }

    /** Tính dòng hiện của cấp đang mở (gon: Limit / cmyLim theo số dòng hiện; không gon: như cũ). */
    void tinhHien() {
        // 0.9.17: máy chủ bật Điểm huấn luyện (production, gói -122 loại 1) -> mục "Huấn luyện" (bảng BangHuanLuyen), không
        // còn "Tiềm năng" (tab điểm tiềm năng tự do luôn 0 ở chế độ này)
        this.name[2][3] = game.pokemon.HocChieu.batHL ? "Hu\u1ea5n luy\u1ec7n" : "Ti\u1ec1m n\u0103ng";
        int n = this.index == -1 ? 9 : Math.max(0, this.name[this.index].length - 1);
        int[] t = new int[n];
        int k = 0;
        for (int i = 0; i < n; ++i) {
            if (this.index == -1 ? !anHang(i) : !an(this.index, i)) {
                t[k++] = i;
            }
        }
        this.hien = new int[k];
        System.arraycopy(t, 0, this.hien, 0, k);
        if (gon()) {
            this.Limit = k;
            this.cmyLim = Math.max(0, k * this.hText - this.h);
        }
    }

    /** Chỉ số gốc -> dòng hiện (không thấy: 0). */
    int viTri(int goc) {
        for (int i = 0; i < this.hien.length; ++i) {
            if (this.hien[i] == goc) {
                return i;
            }
        }
        return 0;
    }

    public static PauseMenu gI() {
        return me == null ? (me = new PauseMenu()) : me;
    }

    public void initName() {
        if (Canvas.gameScr.mainChar.idClan == -1) {
            this.name[7] = new String[]{"\u0110\u1ed9i", "Top \u0111\u1ed9i", "\u0110\u0103ng k\u00fd \u0111\u1ed9i"};
        } else if (Canvas.gameScr.mainChar.isMaster == 0) {
            this.name[7] = new String[]{"\u0110\u1ed9i", "Top \u0111\u1ed9i", "Nhi\u1ec7m v\u1ee5", "Th\u00e0nh vi\u00ean \u0111\u1ed9i", "Th\u00f4ng tin \u0111\u1ed9i", "Tin nh\u1eafn \u0111\u1ed9i", "K\u1ef9 n\u0103ng", "Chat to\u00e0n \u0111\u1ed9i", "Quy\u00ean g\u00f3p", "Gi\u1ea3i t\u00e1n \u0111\u1ed9i"};
            if (Char.dissolved) {
                this.name[7][9] = "Ph\u1ee5c h\u1ed3i \u0111\u1ed9i";
            }
        } else {
            this.name[7] = new String[]{"\u0110\u1ed9i", "Top \u0111\u1ed9i", "Nhi\u1ec7m v\u1ee5", "Th\u00e0nh vi\u00ean \u0111\u1ed9i", "Th\u00f4ng tin \u0111\u1ed9i", "Tin nh\u1eafn \u0111\u1ed9i", "K\u1ef9 n\u0103ng", "Chat to\u00e0n \u0111\u1ed9i", "Quy\u00ean g\u00f3p", "R\u1eddi \u0111\u1ed9i"};
        }
    }

    public void init() {
        this.w = 128;
        this.h = 83;
        this.hText = 18;
        if (Canvas.instance.hasPointerEvents()) {
            this.w = 170;
            this.h = 130;
            this.hText = 24;
        }
        // kiểu menu Start của Pokémon: bảng neo mép phải, rộng theo nhãn dài nhất (chữ canh trái)
        int dai = 0;
        for (int i = 0; i < this.name.length; ++i) {
            for (int j = 0; j < this.name[i].length; ++j) {
                if (this.name[i][j] != null) {
                    dai = Math.max(dai, Font.normalFont[0].getWidth(this.name[i][j]));
                }
            }
        }
        this.w = Math.min(Canvas.w - 16, Math.max(this.w, dai + 44));
        this.x = Canvas.w - this.w - 9;
        this.y = (Canvas.h - this.h) / 2;
        this.cmy = 0;
        this.cmyLim = this.name.length * this.hText - this.h;
    }


    /** Menu con mở gần nhất. Mở lại mà vẫn đứng ở dòng vừa bấm (Menu.startAt luôn về dòng đầu). */
    static Vector menuMod;

    static void moMenuGiuChon(Vector vector, int n, int n2) {
        menuMod = vector;
        // mọi popup của mod mở ở vị trí 3 (góc dưới phải) như menu xa phu của game
        Canvas.menu.startAt(vector, 3);
        if (n2 <= 0 || n2 >= vector.size()) {
            return;
        }
        Menu menu = Canvas.menu;
        menu.selected = n2;
        int n3 = n2 * Screen.ITEM_HEIGHT - Screen.ITEM_HEIGHT * 2;
        if (n3 > menu.cylim) {
            n3 = menu.cylim;
        }
        if (n3 < 0) {
            n3 = 0;
        }
        menu.ctoyBag = n3;
        menu.cyBag = n3;
    }

    /**
     * Menu mod dạng popup - cùng mục và hành động với hàng mod (index 0) của doSelect. Bản jar mở hàng
     * đó trong màn PauseMenu; Android / PC (menu thật là MenuWindows) mở popup này từ tab "Khác".
     */
    /**
     * Hàng mod dạng popup (Android / PC: menu thật là MenuWindows, mở từ tab "Khác") - cùng mục với hàng đầu
     * của doSelect.
     */
    public static void moMenuMod() {
        Vector vector = new Vector();
        vector.addElement(new Command("T\u1ef1 \u0111\u1ed9ng \u0111\u00e1nh", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                Nen.moBangTuDong();
            }
        }));
        vector.addElement(new Command("Hi\u1ec7u \u1ee9ng", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                HieuUng.moMenu(0);
            }
        }));
        vector.addElement(new Command("Ti\u1ec7n \u00edch", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                TienIch.moMenu(0);
            }
        }));
        vector.addElement(new Command("Ngo\u1ea1i h\u00ecnh", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                game.pokemon.NgoaiHinhMenu.mo(0);
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 3, 0);
    }

    private PauseMenu() {
        this.init();
        this.center = new Command("Ch\u1ecdn", new IAction(){

            public void perform() {
                PauseMenu.this.doSelect();
            }
        });
        this.right = new Command("\u0110\u00f3ng", new IAction(){

            public void perform() {
                if (PauseMenu.this.index != -1) {
                    PauseMenu.this.selected = PauseMenu.this.index;
                    PauseMenu.this.index = -1;
                    PauseMenu.this.Limit = 9;
                    PauseMenu.this.cmyLim = PauseMenu.this.Limit * PauseMenu.this.hText - PauseMenu.this.h;
                    PauseMenu.this.tinhHien();
                    PauseMenu.this.selected = PauseMenu.this.viTri(PauseMenu.this.selected);
                    PauseMenu.this.cmtoY = PauseMenu.this.selected * PauseMenu.this.hText - PauseMenu.this.h / 2;
                } else {
                    Canvas.gameScr.switchToMe();
                }
            }
        });
    }

    /**
     * 31/10/2026 (soi giao diện: Menu › Tiện ích & PK mở menu chữ cũ - hàng 24 px, không vùng chạm, phím mềm góc tí hon):
     * máy cảm ứng / PC vẽ thành TẤM chung (Bang.khungTam canh giữa, hàng >= 48 dp có vùng chạm, mũi tên mục con, nút lùi
     * về cấp trên, X đóng). Cùng mục / cùng việc (doSelect) như menu chữ; phím 2 / 8 / 5 / phím mềm vẫn chạy. J2ME bấm phím
     * giữ menu chữ.
     */
    static boolean cham() {
        return game.pokemon.MenuNguCanh.bat();
    }

    final game.pokemon.Cuon cuonCham = new game.pokemon.Cuon();
    /** vừa đi bằng phím (2 / 8): tô hàng đang chọn */
    boolean diPhim;

    String tieuDeCham() {
        if (this.index == -1) {
            return this.gonLan ? "Ti\u1ec7n \u00edch & PK" : "Menu";
        }
        return this.name[this.index][0];
    }

    void veCham(Graphics g) {
        Canvas.gameScr.paint(g);
        Canvas.resetTrans(g);
        game.pokemon.Bang.phuNen(g);
        final int n = Math.min(this.Limit, this.hien.length);
        int rh = game.pokemon.Bang.caoHang(), k = game.pokemon.Bang.cach();
        int wChu = 0;
        for (int i = 0; i < n; ++i) {
            wChu = Math.max(wChu, game.pokemon.ChuDe.rong(muc(i), game.pokemon.ChuDe.NHAN));
        }
        int hMuon = game.pokemon.Bang.caoDauTam(false) + n * (rh + k) + game.pokemon.ChuDe.DEM;
        int[] c = game.pokemon.Bang.khungTam(g, wChu + 120, hMuon, tieuDeCham(), null, 0, null, new IAction() {
            public void perform() {
                Canvas.gameScr.switchToMe();
            }
        });
        if (this.index != -1) {
            int hd = game.pokemon.Bang.caoDau();
            int[] t = game.pokemon.Bang.VUA_TAM;
            game.pokemon.Bang.nutLui(g, t[0] + game.pokemon.ChuDe.DEM, t[1] + 3, hd, this.right.action);
        }
        game.pokemon.Bang.danhSach(g, this.cuonCham, c[0], c[1], c[2], c[3], n, rh, new game.pokemon.Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final int dong = i;
                game.pokemon.Bang.hang(g, x, y, w, h, i, PauseMenu.this.diPhim && i == PauseMenu.this.selected, new IAction() {
                    public void perform() {
                        PauseMenu.this.chamDong(dong);
                    }
                }, "pm-" + i);
                String m = muc(i);
                boolean con = coCon(i);
                int wHinh = Nen.rongHinhMuc(m);
                Nen.veHinhMuc(g, x + 10, y + (h - 13) / 2, m);
                int xc = x + 12 + wHinh;
                game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(m, game.pokemon.ChuDe.NHAN, w - (xc - x) - (con ? 30 : 10)), xc, y, h,
                        game.pokemon.ChuDe.NHAN, game.pokemon.ChuDe.CHU, 0);
                if (con) {
                    game.pokemon.GiaoDien.muiTab(g, x + w - 12, y + h / 2, false);
                }
            }
        });
    }

    /** Chữ dòng hiện i. */
    String muc(int i) {
        int gi = i < this.hien.length ? this.hien[i] : i;
        String m = this.index == -1 ? this.name[gi][0] : this.name[this.index][gi + 1];
        return m == null ? "" : m;
    }

    /** Dòng hiện i mở cấp con (hàng gốc có mục con, trừ hàng tự làm việc ngay). */
    boolean coCon(int i) {
        if (this.index != -1) {
            return false;
        }
        int gi = i < this.hien.length ? this.hien[i] : i;
        return this.name[gi].length > 1;
    }

    void chamDong(int i) {
        this.selected = i;
        int cap = this.index;
        this.doSelect();
        if (this.index != cap) {
            this.cuonCham.den(0);
        }
        game.pokemon.AmThanh.phat(game.pokemon.AmThanh.XAC_NHAN);
    }

    public void paint(Graphics graphics) {
        if (cham()) {
            this.anNutLenh = true;
            veCham(graphics);
            return;
        }
        Canvas.gameScr.paint(graphics);
        Canvas.gameScr.paintServerInfo(graphics);
        Res.paintDlgDragonFull(graphics, this.x - 5, this.y - 10, this.w + 10, this.h + 20);
        graphics.setClip(this.x, this.y - 2, this.w + 2, this.h + 4);
        int n = this.y;
        graphics.translate(0, -this.cmy);
        for (int i = 0; i < this.Limit; ++i) {
            int gi = i < this.hien.length ? this.hien[i] : i;
            String muc = this.index == -1 ? this.name[gi][0] : this.name[this.index][gi + 1];
            if (i == this.selected) {
                Res.paintSelected(graphics, this.x, n, this.w, this.hText);
                Nen.conTroMenu(graphics, this.x + 5, n + this.hText / 2 - 4, 8);
            }
            // biểu tượng mục (bản jar; Android: không vẽ) đặt trước chữ, cả cụm canh giữa
            Font f = i == this.selected ? Font.normalFont[0] : Font.arialFontW;
            int wHinh = Nen.rongHinhMuc(muc);
            int xChu = this.x + 15 + wHinh;
            Nen.veHinhMuc(graphics, this.x + 15, n + (this.hText - 13) / 2, muc);
            f.drawString(graphics, muc, xChu, n + this.hText / 2 - Font.normalFont[0].getHeight() / 2, 0);
            n += this.hText;
        }
        graphics.translate(0, this.cmy);
        // tiêu đề nhóm (dải đỏ trên mép bảng) + thanh cuộn khi danh sách dài hơn khung
        Nen.veKhungMenu(graphics, this.x - 5, this.y - 10, this.w + 10, this.h + 20, this.index == -1 ? "Menu" : this.name[this.index][0],
                this.Limit, this.cmy / Math.max(1, this.hText), this.h / Math.max(1, this.hText));
        super.paint(graphics);
    }

    public void moveCamera() {
        if (this.cmy != this.cmtoY) {
            this.cmvy = this.cmtoY - this.cmy << 2;
            this.cmdy += this.cmvy;
            this.cmy += this.cmdy >> 4;
            this.cmdy &= 0xF;
        }
    }

    public void update() {
        if (cham()) {
            // thế giới sau tấm chạy tiếp nhưng không nhận chạm (chạm ngoài tấm không làm nhân vật đi)
            boolean c = Canvas.isPointerClick, d = Canvas.isPointerDown;
            Canvas.isPointerClick = false;
            Canvas.isPointerDown = false;
            try {
                Canvas.gameScr.update();
            } finally {
                Canvas.isPointerClick = c;
                Canvas.isPointerDown = d;
            }
            return;
        }
        Canvas.gameScr.update();
        this.moveCamera();
        if (Math.abs(this.cmtoY - this.cmy) < 15 && this.cmy < 0) {
            this.cmtoY = 0;
        }
        if (Math.abs(this.cmtoY - this.cmy) < 10 && this.cmy > this.cmyLim) {
            this.cmtoY = this.cmyLim;
        }
    }

    /** Thanh cuộn (như Nen.veKhungMenu -> KieuO.thanhCuon): {x, rộng, y rãnh, cao rãnh, cao núm}; null = không có. */
    int[] thanh() {
        int thay = this.h / Math.max(1, this.hText);
        int hb = this.h - 4;
        if (this.Limit <= thay || thay <= 0 || hb < 8 || this.cmyLim <= 0) {
            return null;
        }
        int dai = Math.max(6, hb * thay / this.Limit);
        return new int[]{this.x + this.w - 3, 3, this.y + 2, hb, Math.min(dai, hb)};
    }

    /**
     * 0.9.11: kéo NÚM thanh cuộn - núm theo con trỏ, danh sách theo tỉ lệ cùng chiều (trước: kéo trên thanh = vuốt nội
     * dung, ngược chiều, và nhấn còn chọn mục dưới thanh); chạm rãnh = nhảy tới chỗ chạm.
     */
    boolean keoThanh() {
        int[] t = thanh();
        if (this.keoNum) {
            if (t != null && (Canvas.dangGiu || Canvas.isPointerDown)) {
                this.cmtoY = this.cmy = game.pokemon.ThanhCuon.keo(this.cuonNumDau, this.pyNumDau, Canvas.py, t[3], t[4], this.cmyLim);
                return true;
            }
            this.keoNum = false;
            this.trans = false;
            Canvas.isPointerClick = false;   // cú thả của lượt kéo núm: không chọn mục
            return true;
        }
        if (t != null && (Canvas.dangGiu || Canvas.isPointerDown)
                && game.pokemon.ThanhCuon.trung(Canvas.pxLast, Canvas.pyLast, t[0], t[1], t[2], t[3])) {
            int c = Math.max(0, Math.min(this.cmyLim, this.cmy));
            if (!game.pokemon.ThanhCuon.trenNum(Canvas.pyLast, t[2], t[3], t[4], c, this.cmyLim)) {
                c = game.pokemon.ThanhCuon.toi(Canvas.pyLast, t[2], t[3], t[4], this.cmyLim);
            }
            this.keoNum = true;
            this.cuonNumDau = c;
            this.pyNumDau = Canvas.pyLast;
            this.cmtoY = this.cmy = game.pokemon.ThanhCuon.keo(c, this.pyNumDau, Canvas.py, t[3], t[4], this.cmyLim);
            return true;
        }
        return false;
    }

    public void updateKey() {
        if (cham()) {
            if (Canvas.isKeyPressed(8) && this.Limit > 0) {
                this.selected = (this.selected + 1) % this.Limit;
                this.diPhim = true;
            } else if (Canvas.isKeyPressed(2) && this.Limit > 0) {
                this.selected = (this.selected + this.Limit - 1) % this.Limit;
                this.diPhim = true;
            }
            if (Canvas.isPointerDown) {
                this.diPhim = false;
            }
            super.updateKey();
            return;
        }
        if (keoThanh()) {
            super.updateKey();
            return;
        }
        if (Canvas.isPointer(this.x, this.y, this.w, this.h)) {
            int n;
            int n2 = Canvas.pyLast - Canvas.py;
            if (Canvas.isPointerDown) {
                if (!this.trans) {
                    this.pa = this.cmy;
                    this.trans = true;
                    n = (this.cmtoY + Canvas.py - this.y) / this.hText;
                    if (n == this.selected) {
                        this.doSelect();
                    }
                    this.selected = n;
                    if (this.selected < 0) {
                        this.selected = 0;
                    }
                    if (this.selected >= this.Limit) {
                        this.selected = this.Limit - 1;
                    }
                    this.cmtoY = this.selected * this.hText - this.h / 2;
                    if (this.cmtoY < 0) {
                        this.cmtoY = 0;
                    }
                }
                this.cmtoY = this.pa + (Canvas.pyLast - Canvas.py);
                if (this.cmtoY < -10) {
                    this.cmtoY = -10;
                }
                if (this.cmtoY > this.cmyLim + 10) {
                    this.cmtoY = this.cmyLim + 10;
                }
            }
            if (Canvas.isPointerClick) {
                Canvas.isPointerClick = false;
                this.trans = false;
                if (Math.abs(n2) < 5 && (n = (this.cmtoY + Canvas.py - this.y) / this.hText) == this.selected) {
                    this.doSelect();
                }
            }
        }
        if (Canvas.isKeyPressed(8)) {
            ++this.selected;
            if (this.selected >= this.Limit) {
                this.selected = 0;
            }
            this.cmtoY = this.selected * this.hText - this.h / 2;
            if (this.cmtoY < 0) {
                this.cmtoY = 0;
            }
            if (this.cmtoY > this.cmyLim) {
                this.cmtoY = this.cmyLim;
            }
        }
        if (Canvas.isKeyPressed(2)) {
            --this.selected;
            if (this.selected < 0) {
                this.selected = this.Limit - 1;
            }
            this.cmtoY = this.selected * this.hText - this.h / 2;
            if (this.cmtoY < 0) {
                this.cmtoY = 0;
            }
            if (this.cmtoY > this.cmyLim) {
                this.cmtoY = this.cmyLim;
            }
        }
        super.updateKey();
    }

    /** Dòng hiện -> chỉ số gốc, chạy doSelect0 (xử lý theo chỉ số gốc như cũ), rồi về dòng hiện. */
    private void doSelect() {
        if (this.selected < 0 || this.selected >= this.hien.length) {
            return;
        }
        int cap = this.index;
        this.selected = this.hien[this.selected];
        this.doSelect0();
        if (this.index != cap) {
            this.tinhHien();
        } else {
            this.selected = this.viTri(this.selected);
        }
    }

    private void doSelect0() {
        if (this.index == -1) {
            if (this.name[this.selected].length == 1) {
                switch (this.selected) {
                    case 8: {
                        int n = 0;
                        for (int i = 0; i < Res.shopTemplate.size(); ++i) {
                            GemTemp gemTemp = (GemTemp)Res.shopTemplate.elementAt(i);
                            if (gemTemp.shopType + 1 <= n) continue;
                            n = gemTemp.shopType + 1;
                        }
                        byte[] object = new byte[n];
                        for (int i = 0; i < object.length; ++i) {
                            object[i] = 20;
                        }
                        if (game.pokemon.MenuNguCanh.bat()) {
                            game.pokemon.ManCuaHang.mo(game.pokemon.ManCuaHang.DAC_BIET, object);
                            return;
                        }
                        WindowInfoScr.gI().setInfo(0, true, object);
                        WindowInfoScr.gI().switchToMe();
                        return;
                    }
                    case 3: {
                        Canvas.gameScr.doNapXu();
                        this.right.action.perform();
                        break;
                    }
                    case 4: {
                        this.moSoNhiemVu();
                        return;
                    }
                    case 6: {
                        break;
                    }
                    case 5: {
                        Canvas.gameScr.switchToMe();
                        Canvas.gameScr.gameService.requestKiller();
                        return;
                    }
                }
            }
            this.index = this.selected;
            this.Limit = this.name[this.selected].length - 1;
            this.cmyLim = this.name[this.selected].length * this.hText - this.h;
            if (this.cmyLim < 0) {
                this.cmyLim = 0;
            }
            this.selected = 0;
            this.cmtoY = 0;
            this.cmy = 0;
        } else {
            // hàng đầu: Tự động đánh (chính thức) / Hiệu ứng / Tiện ích
            // 0.9.16 hàng Nhiệm vụ: Sổ nhiệm vụ / Nhiệm vụ của tôi / Sổ tay huấn luyện (mở ở mọi nơi, không phải về làng)
            if (this.index == 4) {
                if (this.selected == 0) {
                    this.moSoNhiemVu();
                } else if (this.selected == 1) {
                    GameScr.moNhiemVuCuaToi();
                } else if (this.selected == 2) {
                    GameScr.moSoTay();
                } else {
                    // 0.9.21: Sự kiện (mùa + hoạt động ngày / tuần) - máy bấm phím không có Menu cá nhân; Sổ tay không còn
                    // lặp các hoạt động này (GameScr.trongSoTayMenu)
                    GameScr.moSuKien();
                }
                return;
            }
            if (this.index == 0) {
                Canvas.gameScr.switchToMe();
                if (this.selected == 0) {
                    Nen.moBangTuDong();
                } else if (this.selected == 1) {
                    HieuUng.moMenu(0);
                } else if (this.selected == 3) {
                    game.pokemon.NgoaiHinhMenu.mo(0);   // ngoai hinh trang bi: an/hien phu kien, trang phuc
                } else {
                    TienIch.moMenu(0);
                }
                return;
            }
            switch (this.selected) {
                case 0: {
                    if (this.index == 1) {
                        Canvas.gameScr.showConfig();
                        Canvas.gameScr.switchToMe();
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(0);
                        break;
                    }
                    if (this.index == 6) {
                        Canvas.gameScr.switchToMe();
                        MessageScr.gI().switchToMe();
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.gameScr.switchToMe();
                    GameService.gI().requestTopStronger_Righer(5, 0);
                    break;
                }
                case 1: {
                    if (this.index == 1) {
                        Canvas.gameScr.switchToMe();
                        this.doBigMap();
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(3);
                        break;
                    }
                    if (this.index == 6) {
                        this.showMe(5);
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.gameScr.switchToMe();
                    Canvas.startWaitDlg();
                    if (Canvas.gameScr.mainChar.idClan == -1) {
                        GameService.gI().registerClan();
                        break;
                    }
                    GameService.gI().questClan(0);
                    Canvas.gameScr.showWindow(0, false, new byte[]{27});
                    Canvas.startWaitDlg();
                    break;
                }
                case 2: {
                    if (this.index == 1) {
                        Canvas.gameScr.switchToMe();
                        ++Canvas.gameScr.hideGUI;
                        if (Canvas.gameScr.hideGUI <= 2) break;
                        Canvas.gameScr.hideGUI = 0;
                        break;
                    }
                    if (this.index == 2) {
                        if (game.pokemon.HocChieu.batHL) {
                            Canvas.gameScr.switchToMe();
                            game.pokemon.HanhDong.moTiemNang();   // BangHuanLuyen
                            break;
                        }
                        this.showMe(2);
                        break;
                    }
                    if (this.index == 6) {
                        Canvas.gameScr.switchToMe();
                        if (ListScr.gI().freindList == null || ListScr.gI().freindList.size() == 0) {
                            Canvas.startOKDlg("Ch\u01b0a c\u00f3 b\u1ea1n");
                            break;
                        }
                        Vector<Char> vector = new Vector<Char>();
                        for (int i = 0; i < ListScr.gI().freindList.size(); ++i) {
                            Char char_ = (Char)ListScr.gI().freindList.elementAt(i);
                            vector.addElement(char_);
                        }
                        ListScr.gI().setInfo(vector, 0, "B\u1ea0N B\u00c8");
                        ListScr.gI().switchToMe();
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.startWaitDlg();
                    GameService.gI().requestClanList(Canvas.gameScr.mainChar.idClan, (byte)0);
                    break;
                }
                case 3: {
                    if (this.index == 1) {
                        Canvas.gameScr.switchToMe();
                        HelpScr.gI().switchToMe();
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(1);
                        break;
                    }
                    if (this.index == 6) {
                        GameService.gI().requestTopStronger_Righer(4, 0);
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.startWaitDlg();
                    GameService.gI().requestClanInfo(Canvas.gameScr.mainChar.idClan);
                    break;
                }
                case 4: {
                    if (this.index == 1) {
                        if (!Nen.coAmThanh()) break;
                        Nen.datAmThanh(!Nen.amThanhBat());
                        Canvas.gameScr.switchToMe();
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(4);
                        break;
                    }
                    if (this.index == 6) {
                        Canvas.gameScr.switchToMe();
                        GameService.gI().requestTopStronger_Righer(3, 0);
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.gameScr.switchToMe();
                    GameService.gI().msgClanAll(null, (byte)1, 0);
                    break;
                }
                case 5: {
                    if (this.index == 7) {
                        Canvas.gameScr.showWindow(0, true, new byte[]{29, 30});
                        break;
                    }
                    if (this.index == 2) {
                        // 0.9.30: Bản thân › Trang bị thú = bảng danh sách ô (Xem / Tháo / Thay), không còn lưới 10 ô không nhãn
                        Canvas.gameScr.switchToMe();
                        game.pokemon.TrangBiThu.mo();
                        break;
                    }
                    if (this.index == 6) {
                        GameService.gI().requestTopStronger_Righer(7, 0);
                        break;
                    }
                    if (this.index != 1) break;
                    Canvas.gameScr.onoffInvite = !Canvas.gameScr.onoffInvite;
                    GameService.gI().setConfig(!Canvas.gameScr.onoffInvite ? 4 : 5);
                    Canvas.gameScr.switchToMe();
                    Canvas.startOKDlg(!Canvas.gameScr.onoffInvite ? "\u0110\u00e3 b\u1eadt ch\u1ee9c n\u0103ng nh\u1eadn l\u1eddi m\u1eddi." : "\u0110\u00e3 t\u1eaft ch\u1ee9c n\u0103ng nh\u1eadn l\u1eddi m\u1eddi.");
                    break;
                }
                case 6: {
                    if (this.index == 7) {
                        Canvas.gameScr.switchToMe();
                        MessageScr.gI().addTab("", "\u0110\u1ed9i", 1);
                        MessageScr.gI().setFocusTab("\u0110\u1ed9i");
                        MessageScr.gI().switchToMe();
                        break;
                    }
                    if (this.index == 1) {
                        // "Bật/tắt auto đánh": Tự động đánh chính thức (máy chủ điều khiển)
                        Canvas.gameScr.switchToMe();
                        Nen.moBangTuDong();
                        break;
                    }
                    if (this.index == 6) {
                        MsgWorld.gI().switchToMe();
                        break;
                    }
                    if (this.index != 2) break;
                    Tree2014.gI().switchToMe(Canvas.gameScr);
                    break;
                }
                case 7: {
                    if (this.index == 7) {
                        Canvas.inputDlg.setInfo("S\u1ed1 l\u01b0\u1ee3ng", new IAction(){

                            public void perform() {
                                String string = Canvas.inputDlg.tfInput.getText();
                                if (!string.equals("")) {
                                    try {
                                        GameService.gI().transMoney(Integer.parseInt(string));
                                        Canvas.startOKDlg("\u0110\u00e3 chuy\u1ec3n ti\u1ec1n th\u00e0nh c\u00f4ng");
                                        Canvas.gameScr.switchToMe();
                                    }
                                    catch (Exception exception) {
                                        Canvas.startOKDlg("B\u1ea1n ph\u1ea3i nh\u1eadp s\u1ed1");
                                    }
                                }
                            }
                        }, 1, 100, false);
                        Canvas.inputDlg.show();
                        break;
                    }
                    if (this.index == 6) {
                        try {
                            if (GameMidlet.url == "") break;
                            GameMidlet.instance.platformRequest(GameMidlet.url);
                        }
                        catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        break;
                    }
                    if (this.index == 2) {
                        TuBinhScr.gI().switchToMe(Canvas.gameScr);
                        break;
                    }
                    if (this.index != 1) break;
                    Canvas.autoTab.switchToMe();
                    Canvas.autoTab.isOptionFocus = true;
                    Canvas.autoTab.currentIndex = GameScr.typeOptionFocus;
                    Canvas.autoTab.setInfo();
                    break;
                }
                case 9: {
                    // pokemon (hệ sinh thái): túi nguyên liệu dạng lưới (bản jar) / menu server (Android)
                    if (this.index == 2) {
                        Canvas.gameScr.switchToMe();
                        Nen.moTuiSinhThai();
                    }
                    break;
                }
                case 8: {
                    if (this.index == 1) {
                        // Cài đặt -> Giấy phép: ghi công tài nguyên (CC BY-NC 4.0 bắt buộc ghi công)
                        Canvas.gameScr.switchToMe();
                        Nen.moGiayPhep();
                        return;
                    }
                    if (this.index == 2) {
                        if (game.pokemon.MenuNguCanh.bat()) {
                            Canvas.gameScr.switchToMe();   // 0.9.17: thẻ máy chủ hiện trên thế giới, tiêu đề "Kho & tiện ích"
                        }
                        GameScr.moKhoTienIch();
                        return;
                    }
                    if (Canvas.gameScr.mainChar.idClan == -1) {
                        Canvas.startOKDlg("B\u1ea1n ch\u01b0a c\u00f3 \u0111\u1ed9i");
                        break;
                    }
                    if (Canvas.gameScr.mainChar.isMaster == 0) {
                        Canvas.startYesNoDlg(Char.dissolved ? "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n ph\u1ee5c h\u1ed3i l\u1ea1i \u0111\u1ed9i kh\u00f4ng ?" : "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n gi\u1ea3i t\u00e1n \u0111\u1ed9i kh\u00f4ng ?", new IAction(){

                            public void perform() {
                                Canvas.gameScr.switchToMe();
                                GameService.gI().dissolveCLan();
                                Canvas.startOKDlg(Char.dissolved ? "\u0110\u00e3 kh\u00f4i ph\u1ee5c l\u1ea1i \u0111\u1ed9i" : "\u0110\u00e3 gi\u1ea3i t\u00e1n \u0111\u1ed9i");
                                Char.dissolved = !Char.dissolved;
                                PauseMenu.this.initName();
                            }
                        });
                        break;
                    }
                    Canvas.startYesNoDlg("B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n r\u1eddi \u0111\u1ed9i kh\u00f4ng ?", new IAction(){

                        public void perform() {
                            GameService.gI().outClan();
                            Canvas.gameScr.mainChar.idClan = (short)-1;
                            Canvas.gameScr.switchToMe();
                            PauseMenu.this.initName();
                            Canvas.startOKDlg("R\u1eddi \u0111\u1ed9i th\u00e0nh c\u00f4ng");
                        }
                    });
                    break;
                }
            }
        }
    }

    /**
     * Tab bảng "Bản thân" (WindowInfoScr / ShopNew): 0 hành trang, 1 trang bị, 2 tiềm năng, 3 kỹ năng, 4 thông tin,
     * 5 nhóm, 6 nhiệm vụ, 31 trang bị thú. 0.9.17: máy chủ bật Điểm huấn luyện -> bỏ tab Tiềm năng (thay bằng mục
     * "Huấn luyện" = BangHuanLuyen).
     */
    static byte[] tabBanThan() {
        return game.pokemon.HocChieu.batHL ? new byte[]{0, 1, 3, 4, 5, 6, 31} : new byte[]{0, 1, 2, 3, 4, 5, 6, 31};
    }

    /** showMe(n): n = vị trí trong bảng tab đủ 8 (0..6 = mã tab, 7 = trang bị thú) -> vị trí trong bảng thật. */
    static int viTriTab(byte[] tab, int n) {
        int ma = n == 7 ? 31 : n;
        for (int i = 0; i < tab.length; ++i) {
            if (tab[i] == ma) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Menu › Nhiệm vụ › Sổ nhiệm vụ. 31/10/2026 (báo lỗi 0.9.24 J2ME giả lập có chạm ~500 px: "sổ nhiệm vụ trống" dù đang làm
     * cửa ải): máy có chạm mở CÙNG sổ với thẻ nhiệm vụ HUD (ManNhiemVu: tab Đang làm / Có thể nhận, chạm hàng = chi tiết,
     * nút Nhiệm vụ của tôi); máy chỉ bấm phím giữ tab Nhiệm vụ cũ (Quest.getInfoPaintScr không còn rỗng khi thiếu mẫu quái).
     */
    void moSoNhiemVu() {
        if (cham() && Canvas.gameScr != null && Canvas.gameScr.mainChar != null) {
            Canvas.gameScr.switchToMe();
            game.pokemon.ManNhiemVu.mo();
            return;
        }
        this.showMe(6);
    }

    public void showMe(int n) {
        byte[] byArray;
        WindowInfoScr.gI().switchToMe();
        WindowInfoScr windowInfoScr = WindowInfoScr.gI();
        boolean bl = WindowInfoScr.gI().isSell;
        if (WindowInfoScr.gI().isSell) {
            byArray = WindowInfoScr.gI().arrSell;
        } else {
            byArray = tabBanThan();
        }
        windowInfoScr.setInfo(bl ? n : viTriTab(byArray, n), bl, byArray);
        WindowInfoScr.charWearing = Canvas.gameScr.mainChar;
        WindowInfoScr.gI().setImageCharWear();
    }

    public void showShopNew(int n) {
        byte[] byArray;
        ShopNew.gI().switchToMe();
        ShopNew shopNew = ShopNew.gI();
        boolean bl = ShopNew.gI().isSell;
        if (ShopNew.gI().isSell) {
            byArray = ShopNew.gI().arrSell;
        } else {
            byArray = tabBanThan();
        }
        shopNew.setInfo(bl ? n : viTriTab(byArray, n), bl, byArray);
        ShopNew.charWearing = Canvas.gameScr.mainChar;
        ShopNew.gI().setImageCharWear();
    }

    /** Bản đồ vùng (chạm bản đồ nhỏ HUD / Menu › Bản đồ vùng / phím * / game.pokemon.HanhDong.moBanDoLon). */
    public static void moBanDoLon() {
        game.pokemon.ManBanDoVung.mo();
    }

    /** Cài đặt › Bản đồ vùng (APK: HdCau.moBanDoLon gọi qua phản chiếu - giữ tên). */
    private void doBigMap() {
        game.pokemon.ManBanDoVung.mo();
    }

    public void switchToMe() {
        this.gonLan = moTuMenu;
        moTuMenu = false;
        this.Limit = 9;
        this.selected = 0;
        this.index = -1;
        this.cmyLim = this.Limit * 18 - this.h;
        this.cmy = 0;
        this.cmtoY = 0;
        this.name[0][0] = Canvas.gameScr.mainChar.name;
        // gon: hàng này chỉ còn tùy chọn game cũ (lời mời, focus, giấy phép...) - không trùng tên viên "Cài đặt" của Menu
        this.name[1][0] = gon() ? "T\u00f9y ch\u1ecdn kh\u00e1c" : "C\u00e0i \u0111\u1eb7t";
        if (Nen.coAmThanh()) {
            this.name[1][5] = Nen.amThanhBat() ? "T\u1eaft \u00e2m thanh" : "B\u1eadt \u00e2m thanh";
        }
        super.switchToMe();
        this.init();
        this.tinhHien();
    }

}
