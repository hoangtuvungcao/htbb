package game.pokemon;

import game.model.AnimalInfo;
import game.model.Char;
import game.model.IAction;
import game.model.InfoAttributeItem;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameScr;
import game.render.screen.Screen;
import game.render.screen.WindowInfoScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * MÀN NHÂN VẬT (0.9.3, ưu tiên điện thoại) - mở bằng một chạm vào thẻ hồ sơ (HanhDong.moHoSo) hoặc ô "Nhân vật" /
 * "Hành trang" / "Kỹ năng" của Menu (ManMenu). Thay chuỗi Menu -> Bản thân -> Trang bị -> chọn ô -> menu con cũ.
 *
 * - Tab trên cùng, chạm MỘT lần: Trang bị | Chỉ số | Kỹ năng | Thú | Ngoại hình. Nút đóng to góc phải trên; phím
 *   Back (Android) / Esc (PC) = lùi một bước (chi tiết -> danh sách -> đóng).
 * - Trang bị: Pokémon của người chơi vẽ TO giữa màn, động, kèm phụ kiện / ngoại hình đang mặc (Char.paintPokemonTinh
 *   + NgoaiHinhVe); 4 ô trái + 4 ô phải + hàng 4 ô dưới (Phiến đá hệ, Lông vũ, Ruy băng, Thú cưỡi). Bên phải: tên,
 *   cấp, huy hiệu hệ, chỉ số chính.
 * - 0.9.7 (kiểu NRO, yêu cầu chủ dự án): bên phải = LƯỚI Ô VUÔNG hành trang (LuoiTui: chip lọc, đếm ô, ô khoá cuối lưới
 *   -> mở rộng, hàng ví Xu / Lượng / Lượng khoá); chạm ô = thẻ thông tin TheVatPham (Dùng / Mặc / Tháo / Vứt); ô trang
 *   bị VUÔNG hộp gỗ quanh nhân vật.
 * - Chạm một ô (trống hay có đồ): bên phải thành HÀNH TRANG LỌC theo ô đó (đúng loại + đúng dòng + đủ cấp); không
 *   có món nào -> "Chưa có trang bị phù hợp cho ô này" + gợi ý nơi kiếm. Chạm món -> thẻ chi tiết so sánh chỉ số với
 *   món đang mặc (xanh tăng / đỏ giảm) + nút to "Trang bị" / "Gỡ". Máy chủ xác nhận (gói mặc đồ) là hình + chỉ số
 *   đổi ngay khung đó (màn đọc thẳng mainChar mỗi khung).
 */
public final class ManNhanVat extends Screen {
    public static ManNhanVat me;

    public static final int TAB_TRANG_BI = 0, TAB_CHI_SO = 1, TAB_KY_NANG = 2, TAB_THU = 3, TAB_NGOAI_HINH = 4;
    static final String[] TABS = {"Trang bị", "Chỉ số", "Kỹ năng", "Thú", "Ngoại hình"};

    int tab;
    /** ô đang xem (-1 = chưa chọn ô: bên phải là chỉ số) ; O_TAT_CA = toàn bộ hành trang */
    int o = -1;
    static final int O_TAT_CA = 99;
    /** món đang xem chi tiết (null = danh sách) */
    ItemInInventory xem;
    boolean xemDangMac;
    /** cuộn danh sách quán tính (hành trang / kỹ năng / thú) */
    final Cuon ds = new Cuon();
    /** tab Kỹ năng: chiêu đang xem chi tiết (-1 = danh sách), học mới (danh sách NPC dạy) */
    int chieu = -1;
    boolean hocMoi;

    // ---- ô trang bị: tên, các loại (ItemTemplate.type) nhận, huy hiệu: 1 = ô trên (viTriVe 1), 2 = ô dưới
    static final String[] TEN_O = {"Đồ cầm tay", "Khăn quàng", "Băng đô", "Đai", "Vòng cổ", "Huy hiệu", "Huy hiệu 2", "Găng đấm",
        "Lông vũ", "Phiến đá hệ", "Ruy băng", "Thú cưỡi"};
    static final int[][] LOAI_O = {{3, 4, 5, 6, 7}, {0}, {2}, {1}, {9}, {8}, {8}, {11}, {10}, {12}, {19}, {}};
    static final int O_THU = 11;
    static final String NUT_KHAC = "Bỏ · Khoá · Tách";
    /** bóng mờ ô trống theo loại ô (BieuTuong) */
    static final int[] HINH_O = {BieuTuong.DA, BieuTuong.KHAN, BieuTuong.MU, BieuTuong.DAI, BieuTuong.VONG_CO, BieuTuong.HUY_HIEU,
        BieuTuong.HUY_HIEU, BieuTuong.GANG, BieuTuong.LONG_VU, BieuTuong.DA, BieuTuong.QUA, BieuTuong.POKEBALL};
    /** ô đang nhấn giữ (nhãn) */
    static int nhanO = -1, nhanX, nhanY;
    static final String[] GOI_Y = {
        "Đồ cầm tay theo dòng Pokémon: mua ở Poké Mart hoặc nhận từ nhiệm vụ chính.",
        "Khăn quàng: rơi từ Pokémon hoang dã, mua ở Poké Mart.",
        "Băng đô: phần thưởng nhiệm vụ, rơi từ trùm.",
        "Đai: rơi từ Pokémon hoang dã, mua ở Poké Mart.",
        "Vòng cổ: phần thưởng nhiệm vụ, đổi ở cửa hàng đặc biệt.",
        "Huy hiệu: thắng thủ lĩnh Gym, phần thưởng sự kiện.",
        "Huy hiệu: thắng thủ lĩnh Gym, phần thưởng sự kiện.",
        "Găng đấm: rơi từ Pokémon hoang dã, mua ở Poké Mart.",
        "Lông vũ tốc độ: rơi từ Pokémon bay, phần thưởng nhiệm vụ.",
        "Phiến đá hệ: khảo cổ, rơi từ trùm.",
        "Ruy băng: sự kiện, cửa hàng đặc biệt.",
        "Thú cưỡi: nhận từ nhiệm vụ, trứng Pokémon, cửa hàng đặc biệt."
    };

    public static ManNhanVat gI() {
        if (me == null) {
            me = new ManNhanVat();
        }
        return me;
    }

    /** Mở màn ở tab t (TAB_*). */
    public static void mo(int t) {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        ManNhanVat m = gI();
        if (t == TAB_NGOAI_HINH) {
            ManTuDo.xinLuc = 0;
            ManTuDo.xin();
        }
        ManTuDo.boThu();
        m.tab = t;
        m.o = -1;
        m.loc = 0;
        m.xem = null;
        m.daMoXem = null;
        m.ds.den(0);
        m.chieu = -1;
        m.hocMoi = false;
        m.left = null;
        m.center = null;
        m.right = null;
        Canvas.menu.showMenu = false;
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    /** NPC dạy chiêu ("Học kỹ năng"): tab Kỹ năng, danh sách học mới (thay cửa sổ cũ WindowInfoScr tab 7). */
    public static void moHocChieu() {
        mo(TAB_KY_NANG);
        gI().hocMoi = true;
    }

    /** Mở thẳng hành trang (toàn bộ) ở tab Trang bị. */
    public static void moHanhTrang() {
        mo(TAB_TRANG_BI);
        gI().chonO(O_TAT_CA);
    }

    static MainChar mc() {
        return Canvas.gameScr.mainChar;
    }

    /** đã mở danh sách "Học chiêu mới" (rời đi = đã xem chiêu mới, ChamMoi) */
    boolean daMoHocMoi;

    public void dong() {
        if (this.daMoHocMoi) {
            ChamMoi.daXemChieuMoi();
            this.daMoHocMoi = false;
        }
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.gameScr.switchToMe();
    }

    /** Lùi một bước: chi tiết -> danh sách -> chỉ số -> đóng. */
    public void lui() {
        if (this.tab == TAB_KY_NANG && this.chieu >= 0) {
            this.chieu = -1;
        } else if (this.xem != null) {
            this.xem = null;
        } else if (this.o >= 0) {
            this.o = -1;
        } else {
            dong();
        }
    }

    void chonTab(int t) {
        if (t == TAB_NGOAI_HINH && this.tab != t) {
            ManTuDo.xinLuc = 0;
            ManTuDo.xin();   // 0.9.8: tủ đồ mới nhất mỗi lần vào tab
        }
        if (t != TAB_NGOAI_HINH) {
            ManTuDo.boThu();
        }
        this.tab = t;
        this.o = -1;
        this.xem = null;
        this.chieu = -1;
        this.ds.den(0);
    }

    void chonO(int i) {
        this.tab = TAB_TRANG_BI;   // hành trang / ô trang bị chỉ hiện ở tab Trang bị
        this.o = i;
        this.xem = null;
        this.ds.den(0);
        if (i == O_THU) {
            AnimalInfo.getImg();
            doiLoaiThu(QuanLyThu.LOAI_CUOI);   // 0.9.16: ô "Thú cưỡi" mở tab con Thú cưỡi
        }
    }

    void xemMon(ItemInInventory it, boolean dangMac) {
        this.xem = it;
        this.xemDangMac = dangMac;
        moXem();
    }

    /** món đã mở thẻ (không mở lại khi thẻ nhường chỗ cho hộp xác nhận) */
    private Object daMoXem;
    private final IAction dongXem = new IAction() {
        public void perform() {
            ManNhanVat.this.xem = null;
            ManNhanVat.this.daMoXem = null;
        }
    };

    /** Mở thẻ thông tin cho xem (đặt bằng xemMon hoặc kiểm thử) - một lần mỗi món. */
    void moXem() {
        if (this.xem != null && this.xem != this.daMoXem && !TheVatPham.dangMo()) {
            this.daMoXem = this.xem;
            TheVatPham.mo(this.xem, TheVatPham.TUI, this.xemDangMac, this.dongXem);
        }
    }

    // ------------------------------------------------------------------ dữ liệu

    static ItemTemplate mau(ItemInInventory it) {
        try {
            return Res.getItem(it.charClazz, it.idTemplate);
        } catch (Throwable e) {
            return null;
        }
    }

    /** Món đang mặc ở ô i (null = trống). */
    static ItemInInventory dangMac(int i) {
        MainChar m = mc();
        if (m.wearingItems == null || i < 0 || i >= LOAI_O.length) {
            return null;
        }
        for (int k = 0; k < m.wearingItems.size(); k++) {
            ItemInInventory it = (ItemInInventory) m.wearingItems.elementAt(k);
            ItemTemplate t = mau(it);
            if (t == null || !hopLoai(i, t.type)) {
                continue;
            }
            if (t.type == 8 && (i == 5) != (it.viTriVe == 1)) {
                continue;
            }
            return it;
        }
        return null;
    }

    static boolean hopLoai(int i, int type) {
        if (i == O_TAT_CA) {
            return true;
        }
        int[] l = LOAI_O[i];
        for (int k = 0; k < l.length; k++) {
            if (l[k] == type) {
                return true;
            }
        }
        return false;
    }

    static int capCan(ItemInInventory it, ItemTemplate t) {
        if (it.level > 0) {
            return it.level;
        }
        return t == null ? 0 : t.level;
    }

    /** Món mặc được ở ô i: đúng loại, đúng dòng (đồ cầm tay), đúng giới tính, đủ cấp. */
    static boolean hop(int i, ItemInInventory it, boolean xetCap) {
        ItemTemplate t = mau(it);
        if (t == null || !hopLoai(i, t.type)) {
            return false;
        }
        MainChar m = mc();
        if (i != O_TAT_CA) {
            if (t.type >= 3 && t.type <= 7 && t.type != 3 + m.clazz) {
                return false;
            }
            if (t.gender != 0 && m.gender != 0 && t.gender != m.gender) {
                return false;
            }
        }
        return !xetCap || capCan(it, t) <= m.level;
    }

    /** Hành trang lọc theo ô; dem[0] = số món đúng loại nhưng chưa đủ cấp. */
    static Vector loc(int i, int[] dem) {
        Vector v = new Vector();
        int thieuCap = 0;
        for (int k = 0; k < Char.inventory.size(); k++) {
            ItemInInventory it = (ItemInInventory) Char.inventory.elementAt(k);
            if (hop(i, it, true)) {
                v.addElement(it);
            } else if (hop(i, it, false)) {
                thieuCap++;
            }
        }
        if (dem != null) {
            dem[0] = thieuCap;
        }
        return v;
    }

    // ------------------------------------------------------------------ vòng game

    public void update() {
        // thế giới vẫn chạy phía sau, nhưng cú chạm trong bảng không lọt xuống màn chơi (đi bộ / chọn mục tiêu)
        boolean c = Canvas.isPointerClick, d = Canvas.isPointerDown, r = Canvas.isPointerRelease, j = Canvas.isPointerJustRelease;
        Canvas.isPointerClick = false;
        Canvas.isPointerDown = false;
        Canvas.isPointerRelease = false;
        Canvas.isPointerJustRelease = false;
        try {
            Canvas.gameScr.update();
        } catch (Throwable e) {
            // bỏ qua
        }
        Canvas.isPointerClick = c;
        Canvas.isPointerDown = d;
        Canvas.isPointerRelease = r;
        Canvas.isPointerJustRelease = j;
        if (Canvas.currentScreen != this) {
            return;
        }
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return;
        }
        // phím: trái / phải đổi tab, lên / xuống cuộn danh sách
        // 0.9.16: ở danh sách thú trái / phải đổi tab con Thú cưỡi <-> Đi theo trước; tới mép mới đổi tab màn
        if (Canvas.isKeyPressed(4)) {
            if (dangXemDsThu() && this.loaiThu == QuanLyThu.LOAI_DI_THEO) {
                doiLoaiThu(QuanLyThu.LOAI_CUOI);
            } else {
                chonTab((this.tab + TABS.length - 1) % TABS.length);
            }
        } else if (Canvas.isKeyPressed(6)) {
            if (dangXemDsThu() && this.loaiThu == QuanLyThu.LOAI_CUOI) {
                doiLoaiThu(QuanLyThu.LOAI_DI_THEO);
            } else {
                chonTab((this.tab + 1) % TABS.length);
            }
        }
        if (Canvas.isKeyPressed(2)) {
            this.ds.them(-(Bang.caoHang() + Bang.cach()));
        } else if (Canvas.isKeyPressed(8)) {
            this.ds.them(Bang.caoHang() + Bang.cach());
        }
    }


    public void updateKey() {
    }

    public boolean keyPress(int keyCode) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;   // hộp thoại / menu trên màn này tự nhận phím
        }
        if (keyCode == -7 || keyCode == 27) {
            lui();
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ vẽ

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);   // 0.9.5: chỉ lớp thế giới (HUD sau bảng không vẽ)
        } catch (Throwable e) {
            // bỏ qua
        }
        Canvas.resetTrans(g);
        Bang.phuNen(g);
        Object[] vt = new Object[TABS.length];
        for (int i = 0; i < vt.length; i++) {
            final int t = i;
            vt[i] = new IAction() {
                public void perform() {
                    ManNhanVat.this.chonTab(t);
                }
            };
        }
        MainChar m = mc();
        Bang.mauTam = mauHePoke(m);   // 0.9.6: tấm nhuốm màu hệ Pokémon (kiểu GO)
        Bang.chamTab = ChamMoi.tabsNhanVat(TABS.length);
        Bang.dauGon = canDauGon();
        int[] c = Bang.khung(g, m.name == null ? "Hồ sơ" : m.name, TABS, this.tab, vt, new IAction() {
            public void perform() {
                ManNhanVat.this.dong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        minGon = 0;
        switch (this.tab) {
            case TAB_TRANG_BI:
                veTrangBi(g, x, y, w, h);
                break;
            case TAB_CHI_SO:
                veChiSoDay(g, x, y, w, h);
                break;
            case TAB_KY_NANG:
                veKyNang(g, x, y, w, h);
                break;
            case TAB_THU:
                veThu(g, x, y, w, h);
                break;
            default:
                veNgoaiHinh(g, x, y, w, h);
                break;
        }
        Bang.moDan(g);
        Canvas.resetTrans(g);
    }

    /** Cạnh ô trang bị. */
    static int canhO() {
        return Bang.co(30);
    }

    /** Bề rộng khối nhân vật (ô trái + hình + ô phải). */
    static int rongKhoiNv(int w) {
        int s = canhO(), k = Bang.cach();
        int toiThieu = 4 * s + 5 * k;   // hàng dưới 4 ô
        int muon = 2 * s + 4 * k + 150;
        int r = Math.min(muon, w * 52 / 100);
        return Math.max(Math.min(toiThieu + 2 * s + 2 * k, w), r);
    }

    /**
     * Vẽ Pokémon của người chơi to (phóng z) với chân ở (cx, yChan). Đang cưỡi thú (lệnh -93 pokeThuDex): thú dưới
     * chân, người chơi nâng lên 55% chiều cao thú như trên bản đồ (Char.paint); z thu lại cho cả hai vừa khung.
     */
    static void veNguoiChoi(Graphics g, Char ch, int cx, int yChan, float z) {
        int huong = NgoaiHinhVe.huongThe(Canvas.gameTick);
        veSanKhau(g, cx, yChan, z);
        boolean cuoi = ch.pokeThuDex > 0 && PokeBody.coSprite(ch.pokeThuDex, ch.pokeThuShiny);
        if (cuoi) {
            int nang = NgoaiHinhVe.nangNguoi(ch.pokeThuDex, ch.pokeThuShiny);
            int tong = nang + Math.max(24, PokeBody.caoTrenChan(ch.pokeDex, ch.pokeShiny, 100));
            if (tong > 34) {
                z = Math.max(z / 2, z * 34 / tong);
            }
        }
        if (PhongVe.co() && cuoi && ch.pokeDex > 0) {
            // 0.9.16: đang cưỡi - thú + người + hào quang vẽ MỘT lượt như bản đồ (Char.paintPokemonTinh voiThu): hào quang
            // dưới chân THÚ ở mặt đất, sau thú (trước: hào quang dưới chân người trên lưng thú, bản đồ dưới chân thú)
            int tx = g.getTranslateX(), ty = g.getTranslateY();
            PhongVe.bo.bat(g, z);
            ch.paintPokemonTinh(g, (int) ((cx + tx) / z), (int) ((yChan + ty) / z), huong, true);
            PhongVe.bo.tat(g);
            g.translate(tx, ty);
            return;
        }
        if (cuoi) {
            vePoke(g, ch.pokeThuDex, ch.pokeThuShiny, cx, yChan, z, huong, TrangPhucVe.tpThu(ch.ngoaiHinh));
            yChan -= (int) (NgoaiHinhVe.nangNguoi(ch.pokeThuDex, ch.pokeThuShiny) * z);
        }
        if (PhongVe.co()) {
            int tx = g.getTranslateX(), ty = g.getTranslateY();
            PhongVe.bo.bat(g, z);
            if (!ch.paintPokemonTinh(g, (int) ((cx + tx) / z), (int) ((yChan + ty) / z), huong)) {
                ch.paintAvatar(g, (short) ((cx + tx) / z), (short) ((yChan + ty) / z));
            }
            PhongVe.bo.tat(g);
            g.translate(tx, ty);
            return;
        }
        if (ch.pokeDex <= 0 || !PokeBody.paint(g, ch.pokeDex, ch.pokeShiny, cx, yChan, huong, PokeDuLieu.DUNG, Canvas.gameTick, (int) (100 * z))) {
            ch.paintAvatar(g, (short) cx, (short) yChan);
        }
    }

    /** Một Pokémon (thú cưỡi / pet đi theo) đứng yên phóng z, chân ở (cx, yChan). */
    static void vePoke(Graphics g, int dex, boolean shiny, int cx, int yChan, float z, int huong) {
        vePoke(g, dex, shiny, cx, yChan, z, huong, 0);
    }

    /** Như trên, tp = trang phục / skin thú cưỡi đổi màu (PhuKien.doiMauTrangPhuc; 0 = không). */
    static void vePoke(Graphics g, int dex, boolean shiny, int cx, int yChan, float z, int huong, int tp) {
        if (PhongVe.co()) {
            int tx = g.getTranslateX(), ty = g.getTranslateY();
            PhongVe.bo.bat(g, z);
            PokeBody.paint(g, dex, shiny, (int) ((cx + tx) / z), (int) ((yChan + ty) / z), huong, PokeDuLieu.DUNG, Canvas.gameTick, 100, 0, tp);
            PhongVe.bo.tat(g);
            g.translate(tx, ty);
        } else {
            PokeBody.paint(g, dex, shiny, cx, yChan, huong, PokeDuLieu.DUNG, Canvas.gameTick, (int) (100 * z), 0, tp);
        }
    }

    /** Màu hệ chính của Pokémon người chơi (sân khấu, tấm). */
    static int mauHePoke(Char m) {
        int he = m == null ? -1 : HePoke.he1(m.pokeDex);
        return he < 0 ? ChuDe.CHINH : KieuO.mauHe(he);
    }

    /** "Sức mạnh" hiển thị (chỉ để xem, kiểu CP): từ chỉ số thật của Pokémon. */
    static int sucManh(MainChar m) {
        long v = (long) m.getAttack() * 3 + m.defend + m.defend_magic + m.maxhp / 8 + m.maxmp / 16 + m.accurate + m.dodge + m.level * 12L;
        return (int) Math.max(10, Math.min(999999, v));
    }

    /**
     * Sân khấu kiểu GO (mọi tab): KHÔNG khung - đĩa tròn lớn dải màu hệ nhạt phía sau Pokémon trên nền tấm nhuốm màu.
     */
    static void nenSanKhau(Graphics g, int x, int y, int w, int h) {
        // 0.9.7 (NRO): sân khấu = khung lõm giấy da + dải màu hệ dưới chân (pixel, không đĩa tròn mềm)
        int mau = mauHePoke(mc());
        if (!KhungNro.ve(g, KhungNroBang.THE, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, ChuDe.NEN_DUOI);
        }
        int hd = 4;
        g.setColor(GiaoDien.pha(mau, 0xE9D2A0, 3, 5));
        g.fillRect(x + 3, y + h - 3 - hd, w - 6, hd);
        g.setColor(GiaoDien.pha(mau, 0x1B0E06, 1, 3));
        g.fillRect(x + 3, y + h - 3 - hd, w - 6, 1);
    }

    /** Đĩa màu hệ + Pokémon người chơi đứng giữa (tab Chỉ số / Ngoại hình). */
    static void sanKhauNguoi(Graphics g, int x, int y, int w, int h) {
        nenSanKhau(g, x, y, w, h);
        int r = Math.min(w, h) * 9 / 20, cy = y + h / 2 + h / 20;
        float z = Math.max(1.5f, Math.min(4.2f, Math.min(w / 46f, r * 2 / 38f)));
        banRongBe = r;
        veNguoiChoi(g, mc(), x + w / 2, Math.min(y + h - (int) (4 * z), cy + r * 6 / 10), z);
        banRongBe = 1000;
    }

    /** Bệ dưới chân Pokémon (kiểu GO): elip trắng mềm + vành màu hệ nhạt + bóng dưới chân. */
    /** nửa bề ngang tối đa của bệ (khoảng giữa hai cột ô) - người gọi đặt */
    static int banRongBe = 1000;

    static void veSanKhau(Graphics g, int cx, int yChan, float z) {
        // 0.9.7: bệ pixel (elip nâu đậm + vành sáng) dưới chân
        int rw = Math.min((int) (24 * z), banRongBe), rh = Math.max(4, rw / 4);
        g.setColor(0x6A4422);
        g.fillArc(cx - rw, yChan - rh / 2, 2 * rw, rh, 0, 360);
        g.setColor(0x8E5E2E);
        g.fillArc(cx - rw + 2, yChan - rh / 2, 2 * rw - 4, rh - 2, 0, 360);
    }

    /**
     * Tab Trang bị (0.9.18 gọn kiểu NRO, xem {@link #boCuc}): hàng đầu "SỨC MẠNH" + chip Tiềm năng / Huấn luyện, 12 ô
     * vuông sát hai bên sân khấu (cùng cạnh ô hành trang), viên Danh hiệu dưới sân khấu; hành trang gọn bên phải (dưới
     * khi màn dọc hẹp). Ô Thú cưỡi: khối thú riêng (veKhoiThu).
     */
    private void veTrangBi(Graphics g, int x, int y, int w, int h) {
        MainChar m = mc();
        int s, k = Bang.cach();
        int lw = rongKhoiNv(w);
        if (this.o == O_THU) {
            if (gonThu(h)) {
                // 0.9.16 điện thoại (chủ dự án: "menu pet / thú cưỡi trên Android hơi to, chỉ thấy được có 2"): cột hai ô
                // Thú cưỡi / Đi theo hẹp + danh sách rộng 2 cột, đầu một hàng (lùi + tab con + Ô thú); không sân khấu
                // sân khấu (người cưỡi thú + pet / thú đang chọn) + hai ô vẫn giữ, hẹp lại 36 %
                gonVuaVe = true;
                int wc = w * 36 / 100;
                veKhoiThu(g, m, x, y, wc, h);
                Bang.the(g, x + wc + k, y, w - wc - k, h);
                veDanhSachThu(g, x + wc + k + 4, y + 4, w - wc - k - 8, h - 8, new IAction() {
                    public void perform() {
                        ManNhanVat.this.lui();
                    }
                }, true);
                moXem();
                return;
            }
            gonVuaVe = false;
            // 0.9.13: Trang bị › Thú cưỡi KHÔNG vẽ lưới đồ nhân vật (chủ dự án: "thú cưỡi sao có hành trang của nhân vật")
            veKhoiThu(g, m, x, y, lw, h);
            veHanhTrang(g, x + lw + k, y, w - lw - k, h);
            moXem();
            return;
        }
        // 0.9.18 (chủ dự án: "giao diện này bị lộn xộn với xấu, button to"): bố cục GỌN kiểu NRO - đầu một hàng (Sức mạnh +
        // chip Tiềm năng / Huấn luyện), 12 ô sát quanh sân khấu (cùng cạnh ô hành trang), viên Danh hiệu dưới sân khấu;
        // phải: hành trang gọn (chip lọc một hàng, lưới chiếm gần hết chiều cao, ví gọn)
        VUNG_TB[0] = x;
        VUNG_TB[1] = y;
        VUNG_TB[2] = w;
        VUNG_TB[3] = h;
        // 0.9.18: tên danh hiệu dài (không vừa một dòng ở bề ngang khối) -> viên 2-3 dòng: hàng Danh hiệu cao hơn, bố cục
        // tính lại với hàng đó (ô nhỏ đi / sân khấu thấp đi, không chồng)
        // 0.9.20: không bố cục nào vừa với cỡ chạm thường (điện thoại mật độ cao, "cỡ hiển thị lớn") -> cỡ chạm gọn
        // (36 dp thay 44 dp, hạ dần, không dưới mức tối thiểu 24 dp) chỉ trong tab này; vẫn không vừa -> boCucEp (không xếp dọc)
        int min0 = VungCham.MIN;
        minGon = 0;
        int[] bc = chonBoCuc(m, w, h);
        for (int f = 9; f >= 6 && bc[0] == KHONG_VUA && ChuDe.dt(); f--) {
            // 36 dp, 32 dp, 28 dp, 24 dp (mức tối thiểu) - mức lớn nhất còn vừa
            minGon = Math.max(24, (min0 * f + 10) / 11);
            VungCham.MIN = minGon;
            bc = chonBoCuc(m, w, h);
        }
        try {
            veTrangBiGon(g, m, x, y, w, h, bc);
        } finally {
            VungCham.MIN = min0;
        }
    }

    /** {kiểu, s, cột, hàng, sân khấu, cao hàng Danh hiệu}: boCuc theo tên danh hiệu đang đeo (viên 1-3 dòng). */
    int[] chonBoCuc(MainChar m, int w, int h) {
        int k = Bang.cach();
        String chuDh = chuDanhHieu(m);
        int soDong = 1, hP = caoHangDh();
        int[] bc = boCuc(w, h, hP);
        for (int lan = 0; lan < 3; lan++) {
            int lwThu = 2 * (bc[2] * bc[1] + (bc[2] - 1) * k) + 2 * k + bc[4];
            int n = ManDanhHieu.soDongVien(chuDh, lwThu - 4);
            if (n <= soDong) {
                break;
            }
            soDong = n;
            hP = caoHangDh(n);
            bc = boCuc(w, h, hP);
        }
        if (bc[0] == KHONG_VUA && !ChuDe.dt()) {
            bc = boCucEp(w, h, hP);
        }
        return new int[]{bc[0], bc[1], bc[2], bc[3], bc[4], hP};
    }

    private void veTrangBiGon(Graphics g, MainChar m, int x, int y, int w, int h, int[] bc) {
        int k = Bang.cach();
        if (bc[0] == KHONG_VUA) {
            int[] e = boCucEp(w, h, bc[5]);
            bc = new int[]{e[0], e[1], e[2], e[3], e[4], bc[5]};
        }
        int hP = bc[5];
        int s;
        int lw;
        int kieu = bc[0];
        s = bc[1];
        int cs = bc[2], rh = bc[3], st = bc[4];
        lw = 2 * (cs * s + (cs - 1) * k) + 2 * k + st;
        int hDau = caoDauNv();
        int hCot = rh * s + (rh - 1) * k;
        int xe = kieu == XEP_DOC ? x + (w - lw) / 2 : x;
        int ye = y + 4;
        int he = kieu == XEP_DOC ? hDau + k + hCot + k + hP : h - 8;
        BO_CUC[0] = kieu;
        BO_CUC[1] = s;
        BO_CUC[2] = lw;
        BO_CUC[3] = st;
        BO_CUC[4] = hCot;
        veDauNv(g, m, xe, ye, lw, hDau);
        int yP = ye + he - hP;
        int yS = ye + hDau + k + Math.max(0, (yP - k - (ye + hDau + k) - hCot) / 2);
        int wCot = cs * s + (cs - 1) * k;
        int xSan = xe + wCot + k;
        BO_CUC[5] = xSan;
        BO_CUC[6] = yS;
        // sân khấu giữa hai cụm ô, cao bằng cụm ô
        nenSanKhau(g, xSan, yS, st, hCot);
        for (int i = 0; i < 6; i++) {
            int c = i / rh, r = i % rh;
            int yo = yS + r * (s + k);
            veOVuong(g, i, xe + c * (s + k), yo, s);
            veOVuong(g, 6 + i, xe + lw - s - c * (s + k), yo, s);
        }
        float z = Math.max(1f, Math.min(3.6f, Math.min((st - 8) / 30f, (hCot - 8) / 38f)));
        int yChan = yS + hCot - Math.max(6, (int) (4 * z));
        veTheLoai(g, m, xSan, yS, st, yChan - (int) (36 * z) - yS);
        banRongBe = st / 2 - 4;
        veNguoiChoi(g, m, xSan + st / 2, yChan, z);
        banRongBe = 1000;
        veDanhHieuNv(g, m, xe, yP, lw, hP);
        if (nhanO >= 0 && nhanO < TEN_O.length) {
            String ten = TEN_O[nhanO];
            int wt = ChuDe.rong(ten, ChuDe.NHAN_NHO) + 12, ht = ChuDe.cao(ChuDe.NHAN_NHO) + 4;
            int xl = Math.max(xe + 2, Math.min(xe + lw - wt - 2, nhanX - wt / 2));
            if (!KhungNro.ve(g, KhungNroBang.TOOLTIP, xl, nhanY - ht - 2, wt, ht)) {
                GiaoDien.khoi(g, xl, nhanY - ht - 2, wt, ht, 2, 0x2B1A10);
            }
            ChuDe.chuGiua(g, ten, xl + wt / 2, nhanY - ht - 2, ht, ChuDe.NHAN_NHO, 0xF6E6C0, 2);
            nhanO = -1;
        }
        // hành trang: bên phải (hoặc dưới khi màn dọc hẹp - J2ME 240x320)
        if (kieu == XEP_DOC) {
            int yt = ye + he + k;
            veTui(g, x, yt, w, y + h - yt, s);
        } else {
            veTui(g, x + lw + k, y, w - lw - k, h, s);
        }
        moXem();
    }

    // ------------------------------------------------------------------ 0.9.18: bố cục gọn tab Trang bị

    /** kiểu bố cục: một cột 6 ô mỗi bên (màn cao: PC / máy tính bảng), hai cột 3 ô (điện thoại), xếp dọc (màn dọc hẹp) */
    public static final int MOT_COT = 0, HAI_COT = 1, XEP_DOC = 2, KHONG_VUA = 3;
    /** Kiểm thử: bố cục vừa vẽ {kiểu, cạnh ô, bề ngang khối nhân vật, bề ngang sân khấu, cao cụm ô, x sân khấu, y cụm ô}. */
    public static final int[] BO_CUC = new int[7];
    /** Kiểm thử: vùng nội dung tab Trang bị vừa vẽ {x, y, w, h}. */
    public static final int[] VUNG_TB = new int[4];

    /**
     * Đầu sân khấu (khi còn chỗ trên hình): tên loài + "Cấp N · Hệ" - sân khấu cao (PC) không còn khoảng trống lớn phía
     * trên Pokémon. phong = chiều cao còn trống từ đỉnh sân khấu tới đỉnh hình.
     */
    static void veTheLoai(Graphics g, MainChar m, int x, int y, int w, int phong) {
        int h1 = ChuDe.cao(ChuDe.NHAN), h2 = ChuDe.cao(ChuDe.NHAN_NHO);
        if (phong < 6 + h1 + h2) {
            return;
        }
        String loai = LoaiPokemonClient.ten(m.pokeDex);
        int he = HePoke.he1(m.pokeDex);
        String phu = "Cấp " + m.level + (he >= 0 ? " · " + KieuO.tenHe(he) : "");
        int yy = y + 6;
        if (loai != null && loai.length() > 0 && ChuDe.rong(loai, ChuDe.NHAN) <= w - 8) {
            ChuDe.chu(g, loai, x + w / 2, yy, ChuDe.NHAN, ChuDe.CHU, 2);
            yy += h1;
        }
        if (ChuDe.rong(phu, ChuDe.NHAN_NHO) <= w - 8) {
            ChuDe.chu(g, phu, x + w / 2, yy, ChuDe.NHAN_NHO, he >= 0 ? GiaoDien.pha(KieuO.mauHe(he), ChuDe.CHU, 1, 1) : ChuDe.CHU_PHU, 2);
        }
    }

    /** Cao hàng đầu (Sức mạnh + chip Tiềm năng): >= cỡ chạm. */
    static int caoDauNv() {
        return Math.max(VungCham.MIN, ChuDe.cao(ChuDe.NHAN) + 8);
    }

    /** Cao hàng viên Danh hiệu (vùng chạm >= cỡ chạm). */
    static int caoHangDh() {
        return Math.max(VungCham.MIN, ManDanhHieu.hVien() + 2);
    }

    /** Cao hàng viên Danh hiệu n dòng. */
    static int caoHangDh(int n) {
        return Math.max(VungCham.MIN, ManDanhHieu.hVien(n) + 2);
    }

    /** Chữ viên Danh hiệu thẻ nhân vật (tên đang đeo hoặc "Danh hiệu") + đuôi gợi ý bấm. */
    static String chuDanhHieu(MainChar m) {
        return (m.danhHieuTen != null ? m.danhHieuTen : "Danh hiệu") + (ChuUI.bo != null ? " ›" : " >");   // phông ảnh J2ME không có "›"
    }

    /** Cạnh ô gốc (cùng ô hành trang); máy J2ME màn nhỏ (không cảm ứng) 24. */
    static int coOGoc() {
        if (VungCham.MIN < 20 && (Canvas.w <= 320 || Canvas.h <= 240)) {
            return 24;
        }
        return LuoiTui.coO();
    }

    /**
     * Chọn bố cục cho vùng w x h: {kiểu, cạnh ô s, số cột mỗi bên, số hàng mỗi bên, bề ngang sân khấu}. Ưu tiên một cột 6
     * ô mỗi bên (khối hẹp, sân khấu cao), rồi hai cột 3 ô; hành trang bên phải giữ >= 4 (một cột) / 3 (hai cột) cột ô và
     * lưới thấy >= 3 hàng (tính cả ví xuống dòng); xếp dọc (3 cột x 2 hàng mỗi bên, hành trang dưới) chỉ khi lưới dưới vẫn
     * thấy >= 3 hàng. Không bố cục nào vừa: kiểu KHONG_VUA (màn tự thu: đầu tấm gọn, cỡ chạm gọn, cuối cùng boCucEp).
     */
    static int[] boCuc(int w, int h) {
        return boCuc(w, h, caoHangDh());
    }

    /** boCuc với hàng Danh hiệu cao hP. */
    static int[] boCuc(int w, int h, int hP) {
        int k = Bang.cach(), min = VungCham.MIN;
        int goc = Math.max(min, coOGoc());
        int tran = Math.max(goc, Math.min(34, goc * 5 / 4));
        int giua = h - 8 - caoDauNv() - hP - 2 * k;
        int hLuoi = h - LuoiTui.caoKhungGon(0);
        // một cột 6 ô mỗi bên
        int sA = Math.min(tran, (giua - 5 * k) / 6);
        sA = Math.min(sA, (hLuoi - 3 * k) / 4);   // hành trang thấy >= 4 hàng
        for (int c = 6; c >= 4; c--) {
            for (int s = sA; s >= goc; s--) {
                int st = w - k - (2 * s + 2 * k) - rongTui(c, s, k);
                if (st >= 96 && hangTui(h, c, s, k) >= 4) {
                    return new int[]{MOT_COT, s, 1, 6, Math.min(st, 150)};
                }
            }
        }
        // hai cột 3 ô mỗi bên
        int hangTui = ChuDe.dt() ? 3 : 4;   // hành trang thấy >= 3 hàng (cảm ứng) / 4 (PC, J2ME)
        int sB = Math.min(tran, Math.min((giua - 2 * k) / 3, (hLuoi - (hangTui - 1) * k) / hangTui));
        int sMin = Math.max(min, Math.min(goc, 24));
        for (int c = 5; c >= 3; c--) {
            for (int s = sB; s >= sMin; s--) {
                int st = w - k - (4 * s + 4 * k) - rongTui(c, s, k);
                if (st >= (goc <= 24 ? 44 : 60) && hangTui(h, c, s, k) >= hangTui) {
                    return new int[]{HAI_COT, s, 2, 3, Math.min(st, 120)};
                }
            }
        }
        // xếp dọc: 3 cột x 2 hàng mỗi bên, sân khấu giữa, hành trang dưới (màn dọc hẹp - J2ME 240x320)
        int s = goc;
        int st = Math.min(120, w - (6 * s + 6 * k));
        if (st < 40) {
            s = Math.max(min, (w - 40 - 6 * k) / 6);
            st = Math.min(120, w - (6 * s + 6 * k));
        }
        int hTren = 4 + caoDauNv() + k + 2 * s + k + k + hP + k;
        if ((h - hTren - LuoiTui.caoKhungGon(w, s) + k) / (s + k) >= 3) {
            return new int[]{XEP_DOC, s, 3, 2, st};
        }
        return new int[]{KHONG_VUA, s, 2, 3, st};
    }

    /** Số hàng lưới hành trang thấy được ở bảng gọn c cột ô cạnh s, cao h (ví xuống dòng tính cả). */
    static int hangTui(int h, int c, int s, int k) {
        return (h - LuoiTui.caoKhungGon(rongTui(c, s, k), s) + k) / (s + k);
    }

    /**
     * 0.9.20 đường cuối (màn quá thấp kể cả đầu tấm gọn + cỡ chạm gọn): hai cột 3 ô, hành trang bên phải, cạnh ô thu cho
     * cụm ô VÀ lưới hành trang vẫn đủ 3 hàng - không bao giờ xếp dọc làm lưới mất trên màn ngang.
     */
    static int[] boCucEp(int w, int h, int hP) {
        int k = Bang.cach();
        int giua = h - 8 - caoDauNv() - hP - 2 * k;
        int s = Math.max(12, Math.min(34, (giua - 2 * k) / 3));
        for (; s > 12; s--) {
            if (hangTui(h, 3, s, k) >= 3) {
                break;
            }
        }
        int c = 3;
        while (c < 5 && w - k - (4 * s + 4 * k) - rongTui(c + 1, s, k) >= 60) {
            c++;
        }
        while (s > 12 && w - k - (4 * s + 4 * k) - rongTui(c, s, k) < 44) {
            s--;
        }
        int st = w - k - (4 * s + 4 * k) - rongTui(c, s, k);
        return new int[]{HAI_COT, s, 2, 3, Math.max(20, Math.min(st, 120))};
    }

    /**
     * 0.9.20 (chủ dự án: APK 0.9.19 tab Trang bị "mất lưới hành trang" trên điện thoại 1080p mật độ 3 - 44 dp = 44 px logic
     * trên màn 640x360): màn ngang cảm ứng mà tấm thường không đủ cao cho bố cục gọn -> tấm Nhân vật dùng ĐẦU GỌN (tab cùng
     * hàng nút X, bỏ dải tên). Cùng một quyết định cho mọi tab (đổi tab không nhảy khung).
     */
    static boolean canDauGon() {
        if (!ChuDe.dt() || Canvas.w <= Canvas.h) {
            return false;
        }
        int[] o = Bang.oTam(0, 0);
        return boCuc(o[2] - 2 * ChuDe.DEM, o[3] - Bang.caoDauTam(true), caoHangDh())[0] == KHONG_VUA;
    }

    /** Cỡ chạm gọn đang dùng ở tab Trang bị (0 = cỡ chạm thường) - kiểm thử đọc. */
    public static int minGon;

    /** Bề ngang bảng hành trang gọn có c cột ô cạnh s (đệm 5 + thanh cuộn 10). */
    static int rongTui(int c, int s, int k) {
        return 10 + 10 + c * (s + k) - k;
    }

    /**
     * Hàng đầu: trái "SỨC MẠNH 1644" một dòng (hẹp: hai dòng / chỉ số) hoặc tên ô đang chọn; phải chip nhỏ Tiềm năng /
     * Huấn luyện (BangTiemNangHD.nhanNut - theo chế độ máy chủ) có chấm đỏ khi còn điểm. Vùng chạm chip >= cỡ chạm.
     */
    void veDauNv(Graphics g, MainChar m, int x, int y, int w, int h) {
        String nhanTn = BangTiemNangHD.nhanNut();
        boolean coDiem = BangTiemNangHD.diemNut() > 0;
        int hv = ChuDe.cao(ChuDe.NHAN) + 6;
        int wv = ChuDe.rong(nhanTn, ChuDe.NHAN) + 16;
        int wc = Math.max(wv, VungCham.MIN), hc = Math.max(hv, VungCham.MIN);
        int xv = x + w - wc + (wc - wv) / 2 - 2, yv = y + (h - hv) / 2;
        boolean an = Bang.dangAn(g, x + w - wc - 2, y + (h - hc) / 2, wc, hc);
        int manh = coDiem ? KhungNroBang.NUT_CAM : KhungNroBang.NUT_GIAY;
        if (!KhungNro.ve(g, an ? manh + 1 : manh, xv, yv, wv, hv)) {
            GiaoDien.khoi(g, xv, yv, wv, hv, 3, coDiem ? ChuDe.CHINH : ChuDe.KE);
        }
        int dy = an ? 1 : 0;
        if (coDiem) {
            ChuDe.chuVienGiua(g, nhanTn, xv + wv / 2, yv + dy, hv, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 2);
        } else {
            ChuDe.chuGiua(g, nhanTn, xv + wv / 2, yv + dy, hv, ChuDe.NHAN, ChuDe.CHU, 2);
        }
        VungCham.them(g, x + w - wc - 2, y + (h - hc) / 2, wc, hc, new IAction() {
            public void perform() {
                HanhDong.moTiemNang();
            }
        }, "tiem-nang");
        BangTiemNangHD.chamDo(g, xv, yv, wv);
        int wTrai = w - wc - 2 - 10;
        if (this.o >= 0 && this.o < TEN_O.length) {
            String ten = ChuDe.cat(TEN_O[this.o], ChuDe.NHAN, wTrai - 14);
            int wt = ChuDe.rong(ten, ChuDe.NHAN) + 14, ht = ChuDe.cao(ChuDe.NHAN) + 6;
            ChuDe.bien(g, x, y + (h - ht) / 2, wt, ht);
            ChuDe.chuVienGiua(g, ten, x + wt / 2, y + (h - ht) / 2, ht, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 2);
            return;
        }
        String nhan = "SỨC MẠNH", so = String.valueOf(sucManh(m));
        // số to (phông hiển thị) khi vừa chiều cao hàng - thứ bậc rõ: nhãn nhỏ nhạt, số đậm to
        int vs = ChuDe.cao(ChuDe.TIEU_DE) <= h ? ChuDe.TIEU_DE : ChuDe.SO;
        int wn = ChuDe.rong(nhan, ChuDe.NHAN_NHO), ws = ChuDe.rong(so, vs);
        int hn = ChuDe.cao(ChuDe.NHAN_NHO), hs = ChuDe.cao(vs);
        if (wn + 5 + ws <= wTrai) {
            int yd = y + (h - hs) / 2;
            ChuDe.chu(g, nhan, x + 2, yd + hs - hn - (hs - hn) / 4, ChuDe.NHAN_NHO, ChuDe.CHU_PHU, 0);
            ChuDe.chu(g, so, x + 2 + wn + 5, yd, vs, ChuDe.CHU, 0);
        } else if (hn + hs - 2 <= h && Math.max(wn, ws) <= wTrai) {
            int yd = y + (h - hn - hs + 2) / 2;
            ChuDe.chu(g, nhan, x + 2, yd, ChuDe.NHAN_NHO, ChuDe.CHU_PHU, 0);
            ChuDe.chu(g, so, x + 2, yd + hn - 2, vs, ChuDe.CHU, 0);
        } else {
            // hẹp (J2ME 320x240): "SM 1644" như NRO
            int w2 = ChuDe.rong("SM", ChuDe.NHAN_NHO);
            int yd = y + (h - hs) / 2;
            ChuDe.chu(g, "SM", x + 2, yd + hs - hn - (hs - hn) / 4, ChuDe.NHAN_NHO, ChuDe.CHU_PHU, 0);
            ChuDe.chu(g, so, x + 2 + w2 + 4, yd, vs, ChuDe.CHU, 0);
        }
    }

    /** Viên Danh hiệu (lối vào bảng Danh hiệu) canh giữa hàng (x, y, w, h) dưới sân khấu - đủ chữ (cả bề ngang khối). */
    void veDanhHieuNv(Graphics g, MainChar m, int x, int y, int w, int h) {
        boolean coDh = m.danhHieuTen != null;
        String chu = chuDanhHieu(m);
        int wMax = w - 4;
        int hDh = ManDanhHieu.hVien(ManDanhHieu.soDongVien(chu, wMax));   // 0.9.18: tên dài 2-3 dòng, không cắt
        int yDh = y + (h - hDh) / 2;
        int wv = ManDanhHieu.vienDu(g, -10000, yDh, chu, coDh ? m.danhHieuHiem : 0, wMax);   // đo bề ngang
        int xv = x + w / 2 - wv / 2;
        ManDanhHieu.vienDu(g, xv, yDh, chu, coDh ? m.danhHieuHiem : 0, wMax);
        int wcDh = Math.min(w, Math.max(wv + 4, VungCham.MIN));
        VungCham.them(g, x + w / 2 - wcDh / 2, y, wcDh, h, new IAction() {
            public void perform() {
                ManDanhHieu.moTuHoSo();
            }
        }, "danh-hieu");
        if (ChamMoi.co(ChamMoi.DANH_HIEU)) {
            ChuDe.cham(g, xv + wv - 2, yDh + 1);
        }
    }

    /**
     * Khối trái Trang bị › Thú cưỡi (0.9.13): tên ô trên cùng, sân khấu xem thú (thú đang chọn / đang cưỡi / pet đi theo)
     * và hàng HAI Ô thú: "Thú cưỡi" (đang cưỡi) + "Đi theo" (pet đang đi theo) - chạm ô = xem con đó. Không ô đồ nhân
     * vật, không "Tiềm năng", không Danh hiệu.
     */
    private void veKhoiThu(Graphics g, MainChar m, int x, int y, int lw, int h) {
        int k = Bang.cach();
        int hSm = ChuDe.cao(ChuDe.PHU) + ChuDe.cao(ChuDe.SO_TO) - 2;
        String tieuDe = TEN_O[O_THU];
        int wt = ChuDe.rong(tieuDe, ChuDe.NHAN) + 18, ht = ChuDe.cao(ChuDe.NHAN) + 6;
        int cxS = x + lw / 2;
        ChuDe.bien(g, cxS - wt / 2 - 6, y + (hSm - ht) / 2, wt + 12, ht);
        ChuDe.chuVienGiua(g, tieuDe, cxS, y + (hSm - ht) / 2, ht, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 2);
        int yTren = y + hSm + 2;
        // hàng ô dưới: [ô] tên + nhãn  [ô] tên + nhãn
        int d = canhO(), rs = (d + 1) / 2;
        int hHang = Math.max(d, ChuDe.cao(ChuDe.NHAN_NHO) * 2 + 2);
        // 0.9.16 điện thoại (khối hẹp): hai ô xếp DỌC, mỗi ô cả bề ngang (tên Rapidash / Hoothoot không cắt)
        boolean doc = gonVuaVe;
        int kd = k + 2;   // khe hai ô dọc (vùng chạm ô tròn rộng hơn hình một chút)
        int yHang = y + h - (doc ? 2 * hHang + kd : hHang);
        final AnimalInfo cu = thuDangCuoi(), dt = petDiTheo();
        boolean coCuoi = dangCuoi(m);
        boolean coPet = dt != null || (!QuanLyThu.coDung && m.pokePetDex > 0);
        int wNua = doc ? lw : lw / 2;
        for (int i = 0; i < 2; i++) {
            final boolean laCuoi = i == 0;
            int x0 = doc ? x : x + i * wNua;
            int cxO = x0 + rs + 2, cyO = yHang + (doc ? i * (hHang + kd) : 0) + hHang / 2;
            boolean co = laCuoi ? coCuoi : coPet;
            final AnimalInfo a = laCuoi ? cu : dt;
            veOThu(g, m, laCuoi, cxO, cyO, rs, d);
            String ten;
            if (a != null) {
                String[] t = tachTen(a.name);
                ten = t[0].length() > 0 ? t[0] : (laCuoi ? "Thú cưỡi" : "Pokémon");
            } else if (laCuoi && coCuoi) {
                String[] t = tachTen(QuanLyThu.thuCu != null ? QuanLyThu.thuCu : m.infoAnimal);
                ten = t[0].length() > 0 ? t[0] : "Thú cưỡi";
            } else if (!laCuoi && coPet && m.pokePetTen != null) {
                ten = m.pokePetTen;
            } else {
                ten = laCuoi ? "Chưa cưỡi" : (coPet ? "Pokémon" : "Chưa có");
            }
            int xt = cxO + rs + 4, wChu = Math.max(10, x0 + wNua - 2 - xt);
            int yt = cyO - hHang / 2 + (hHang - ChuDe.cao(ChuDe.NHAN_NHO) * 2 - 1) / 2;
            ChuDe.chu(g, ChuDe.cat(laCuoi ? "Thú cưỡi" : "Đi theo", ChuDe.NHAN_NHO, wChu), xt, yt, ChuDe.NHAN_NHO, ChuDe.CHU_PHU, 0);
            ChuDe.chu(g, ChuDe.cat(ten, ChuDe.NHAN_NHO, wChu), xt, yt + ChuDe.cao(ChuDe.NHAN_NHO) + 1, ChuDe.NHAN_NHO,
                    co ? ChuDe.CHU : ChuDe.CHU_PHU, 0);
        }
        // sân khấu xem thú
        int cao = yHang - k - yTren;
        nenSanKhau(g, x, yTren, lw, cao);
        int rD = Math.min(lw, cao) * 9 / 20, cyD = yTren + cao / 2 + cao / 20;
        float z = Math.max(1.5f, Math.min(4.2f, Math.min(lw / 46f, rD * 2 / 38f)));
        int yChan = Math.min(yTren + cao - (int) (4 * z), cyD + rD * 6 / 10);
        if (doc) {
            // sân khấu thấp (điện thoại): hình thu nhỏ vừa phần dưới hai dòng chữ, chân sát đáy
            int hChu = ChuDe.cao(ChuDe.NHAN) + ChuDe.cao(ChuDe.NHAN_NHO) + 8;
            z = Math.max(0.8f, Math.min(z, (cao - hChu - 6) / 36f));
            yChan = yTren + cao - 4;
        }
        banRongBe = lw / 2 - 6;
        veXemThu(g, m, x, yTren, lw, x + lw / 2, yChan, z);
        banRongBe = 1000;
    }

    /** Một ô Thú cưỡi / Đi theo (tròn, hình con đang dùng): chạm = tab con đó + chọn con đang dùng. */
    private void veOThu(Graphics g, MainChar m, final boolean laCuoi, int cxO, int cyO, int rs, int d) {
        final AnimalInfo cu = thuDangCuoi(), dt = petDiTheo();
        boolean coCuoi = dangCuoi(m);
        boolean coPet = dt != null || (!QuanLyThu.coDung && m.pokePetDex > 0);
        boolean co = laCuoi ? coCuoi : coPet;
        final AnimalInfo a = laCuoi ? cu : dt;
        boolean chon = a != null && this.thuChon == a;
        Bang.oTron(g, cxO, cyO, rs, co, chon, new IAction() {
            public void perform() {
                // 0.9.16: ô "Thú cưỡi" / "Đi theo" mở đúng tab con danh sách bên phải
                ManNhanVat.this.doiLoaiThu(laCuoi ? QuanLyThu.LOAI_CUOI : QuanLyThu.LOAI_DI_THEO);
                ManNhanVat.this.thuChon = a == null || ManNhanVat.this.thuChon == a ? null : a;
            }
        }, laCuoi ? "o-thu-cuoi" : "o-di-theo");
        int ic = d - 4;
        if (a != null) {
            veHinhThu(g, dexThu(a), shinyThu(a), (short) (a.idPaint + 7500), cxO, cyO, ic);
        } else if (laCuoi && coCuoi) {
            veHinhThu(g, m.pokeThuDex, m.pokeThuShiny, (short) (m.idPaintIconAnimal + 7500), cxO, cyO, ic);
        } else if (!laCuoi && coPet && coHinhThat(m.pokePetDex, m.pokePetShiny)) {
            PokeBody.paintChanDung(g, m.pokePetDex, m.pokePetShiny, cxO, cyO, ic - 2);
        } else {
            int b = d * 11 / 20;
            BieuTuong.veBong(g, BieuTuong.POKEBALL, cxO - b / 2, cyO - b / 2, Math.max(b, 16), 0x8A6A4A, ChuDe.NEN_PHU);
        }
    }

    /**
     * 0.9.16: danh sách thú gọn (điện thoại) khi bố cục thường (đầu + tab con + Ô thú + thao tác) thấy < 5 hàng trong bảng
     * cao h. Máy tính bảng / PC giữ bố cục cũ.
     */
    static boolean gonThu(int h) {
        int k = Bang.cach();
        int chrome = 3 * (Bang.co(26) + k) + 8;
        return (h - chrome + k) / (Bang.caoHang() + k) < 5;
    }

    /** Kiểm thử: khung vừa vẽ dùng danh sách thú gọn. */
    public static boolean gonVuaVe;

    static int giuaL0(int x, int coCot) {
        return x + coCot - 4;
    }

    static int giuaR0(int x, int lw, int coCot) {
        return x + lw - coCot + 4;
    }

    /** Ô trang bị TRÒN tâm (cx, cy) bán kính r. */
    private void veO(Graphics g, final int i, int cx, int cy, int r) {
        veOVuong(g, i, cx - r, cy - r, 2 * r);
    }

    /** Ô trang bị vuông góc trái trên (x, y) cạnh s (0.9.18: cùng cạnh ô hành trang). */
    private void veOVuong(Graphics g, final int i, int x, int y, int s) {
        int r = s / 2;
        ItemInInventory it = i == O_THU ? null : dangMac(i);
        boolean co = it != null || (i == O_THU && dangCuoi(mc()));
        Bang.oDo(g, x, y, s, co, this.o == i, new IAction() {
            public void perform() {
                ManNhanVat.this.chonO(i);
            }
        }, "o-" + TEN_O[i]);
        int icO = s - 10;
        float z = icO >= 22 ? icO / 22f : 1f;   // cùng cỡ icon ô hành trang (LuoiTui.veO)
        if (!co) {
            // ô trống: bóng mờ kiểu ô (mũ, vòng cổ, găng...) thay dấu "+"
            int ic = s * 11 / 20;
            BieuTuong.veBong(g, HINH_O[i], x + (s - ic) / 2, y + (s - ic) / 2, Math.max(ic, 16), 0x8A6A4A, ChuDe.NEN_PHU);
        }
        if (Canvas.isPointerDown && Canvas.px >= x && Canvas.px < x + s && Canvas.py >= y && Canvas.py < y + s) {
            nhanO = i;   // nhấn giữ / di chuột: nhãn tên ô
            nhanX = x + s / 2;
            nhanY = y;
        }
        if (it != null) {
            ItemTemplate t = mau(it);
            int hiem = KieuO.mauHiem(it.colorName);
            if (hiem >= 0) {
                // độ hiếm: viền gỗ đổi màu độ hiếm (ô vuông NRO)
                g.setColor(hiem);
                g.drawRect(x + 1, y + 1, s - 3, s - 3);
                g.drawRect(x + 2, y + 2, s - 5, s - 5);
            }
            if (t != null) {
                Bang.icon(g, t.idIcon, x + s / 2, y + s / 2, z);
            }
            if (it.plusTemplate > 0) {
                KieuO.capCuong(g, x + r / 3, y, it.plusTemplate);
            }
        } else if (co) {
            MainChar m = mc();
            AnimalInfo a = thuDangCuoi();
            if (a != null) {
                veHinhThu(g, dexThu(a), shinyThu(a), (short) (a.idPaint + 7500), x + s / 2, y + s / 2, s - 4);
            } else {
                veHinhThu(g, m.pokeThuDex, m.pokeThuShiny, (short) (m.idPaintIconAnimal + 7500), x + s / 2, y + s / 2, s - 4);
            }
        }
    }

    // ------------------------------------------------------------------ thú cưỡi / pet đi theo

    /**
     * Đang cưỡi thú: -93 POKE_CUOI (thú Pokémon) hoặc thú cưỡi cũ (gói 3: useHorse / idhorse), kèm idAnimal của gói
     * mặc đồ 15. idAnimal một mình không đủ: xuống thú máy chủ gửi lại gói 3 + -93 nhưng không gửi gói 15.
     */
    static boolean dangCuoi(Char m) {
        if (QuanLyThu.coDung && Canvas.gameScr != null && m == Canvas.gameScr.mainChar) {
            // 0.9.13: máy chủ nói thẳng (khối đang dùng gói 16) - không đoán từ gói 15 / 3 / -93
            return QuanLyThu.cuoi != null || QuanLyThu.thuCu != null;
        }
        return m.idAnimal > -1 && (m.pokeThuDex > 0 || m.idhorse > -1 || m.useHorse > -1);
    }

    /** Người chơi đang cưỡi thú (kiểm thử / màn khác). */
    public static boolean dangCuoiThu() {
        return Canvas.gameScr != null && Canvas.gameScr.mainChar != null && dangCuoi(mc());
    }

    /** Thú đang cưỡi theo máy chủ (khối đang dùng 0.9.13), null = không / máy chủ cũ. */
    static AnimalInfo thuDangCuoi() {
        return QuanLyThu.coDung ? QuanLyThu.cuoi : null;
    }

    /** Pet đang đi theo theo máy chủ (khối đang dùng 0.9.13), null = không / máy chủ cũ. */
    static AnimalInfo petDiTheo() {
        return QuanLyThu.coDung ? QuanLyThu.diTheo : null;
    }

    /** Người chơi trên lưng thú {@code a} (dù -93 chưa tới: pokeThuDex = 0). */
    static void veNguoiCuoi(Graphics g, Char m, AnimalInfo a, int cx, int yChan, float z) {
        short dexCu = m.pokeThuDex;
        boolean shCu = m.pokeThuShiny;
        if (a != null && m.pokeThuDex <= 0) {
            m.pokeThuDex = (short) dexThu(a);
            m.pokeThuShiny = shinyThu(a);
        }
        try {
            veNguoiChoi(g, m, cx, yChan, z);
        } finally {
            m.pokeThuDex = dexCu;
            m.pokeThuShiny = shCu;
        }
    }

    /** Thú cưỡi: idImg (data.Animal) -> số Pokédex - bản sao com.htbb.pokemon.ThuPokemon.DEX của máy chủ. */
    static final short[] DEX_THU = {78, 59, 128, 262, 18, 146, 428, 234, 144, 831, 245, 221, 897, 149, 250, 357, 227, 330, 473, 244, 896};
    /** Pet đi theo: tên (chữ thường) -> số Pokédex - theo com.htbb.pokemon.PetPokemon của máy chủ. */
    static final String[] TEN_PET = {"rapidash", "ponyta", "hoothoot", "noctowl", "dratini", "dragonair", "dragonite", "cleffa",
        "clefairy", "clefable", "zubat", "golbat", "crobat", "rufflet", "braviary", "pachirisu", "delibird", "voltorb", "shinx",
        "litleo", "growlithe", "deerling", "bidoof"};
    static final short[] DEX_PET = {78, 77, 163, 164, 147, 148, 149, 173, 35, 36, 41, 42, 169, 627, 628, 417, 225, 100, 403, 667, 58,
        585, 399};

    static int dexThu(AnimalInfo a) {
        if (a.typeAnimal == 0) {
            return a.idImg >= 0 && a.idImg < DEX_THU.length ? DEX_THU[a.idImg] : 0;
        }
        String t = a.name == null ? "" : a.name.toLowerCase();
        for (int i = 0; i < TEN_PET.length; i++) {
            if (t.indexOf(TEN_PET[i]) >= 0) {
                return DEX_PET[i];
            }
        }
        return 0;
    }

    static boolean shinyThu(AnimalInfo a) {
        return a.typeAnimal == 0 ? a.idImg == 6 : a.name != null && a.name.toLowerCase().indexOf("shiny") >= 0;
    }

    /** Có sprite thật (không phải bóng thay thế PokeBody.KHOA_BONG). */
    static boolean coHinhThat(int dex, boolean shiny) {
        PokeBody p = dex > 0 ? PokeBody.lay(dex, shiny) : null;
        return p != null && !PokeBody.KHOA_BONG.equals(p.khoa);
    }

    /** Hình thú trong ô cạnh s: huy hiệu Pokémon nếu có sprite, không thì icon máy chủ (7500 + idPaint). */
    static void veHinhThu(Graphics g, int dex, boolean shiny, short icon, int cx, int cy, int s) {
        if (coHinhThat(dex, shiny) && PokeBody.paintChanDung(g, dex, shiny, cx, cy, s - 2)) {
            return;
        }
        IconVatPham.ve(g, icon, cx, cy, s, BieuTuong.POKEBALL);
    }

    static boolean laSo(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Tên thú máy chủ gửi ("Hoothoot cấp 1\nID: 2\nhh: 20/10\n-12:00", "Rapidash lv 3\n...") -> {tên, cấp, id, hạn};
     * phần không có = "".
     */
    static String[] tachTen(String s) {
        String[] r = {"", "", "", ""};
        if (s == null) {
            return r;
        }
        int n = s.length();
        for (int i = 0, d = 0; i <= n; d++) {
            int j = s.indexOf('\n', i);
            if (j < 0) {
                j = n;
            }
            String t = s.substring(i, j).trim();
            if (d == 0) {
                r[0] = t;
            } else if (t.startsWith("ID:")) {
                r[2] = t.substring(3).trim();
            } else if (t.startsWith("hh:")) {
                r[3] = t.substring(3).trim();
            } else if (t.startsWith("-") && r[3].length() > 0) {
                r[3] = r[3] + " " + t.substring(1).trim();
            }
            i = j + 1;
        }
        String t = r[0];
        int k = t.indexOf("ID:");
        if (k > 0) {
            r[2] = t.substring(k + 3).trim();
            t = t.substring(0, k).trim();
        }
        k = Math.max(cuoi(t, " cấp "), Math.max(cuoi(t, " Cấp "), cuoi(t, " lv ")));
        if (k > 0) {
            String so = t.substring(t.indexOf(' ', k + 1) + 1).trim();
            if (laSo(so)) {
                r[1] = so;
                t = t.substring(0, k).trim();
            }
        }
        r[0] = t;
        return r;
    }

    /**
     * Hàng thú: tên đậm, "#id" nhỏ, viên "Cấp N" canh phải, dòng phụ. 0.9.13: TÊN ƯU TIÊN - chật thì bỏ "#id" trước,
     * rồi đưa cấp xuống đầu dòng phụ ("Cấp 5 · Đang cưỡi") thay vì cắt tên thành "Rapida.."; dòng phụ chật thì bỏ dần
     * đoạn cuối (" · chạm để chọn", " · hạn ...") trước khi cắt chữ.
     */
    static void hangThu(Graphics g, int x, int y, int w, int h, int s, String ten, String cap, String id, String phu, boolean dangDung) {
        int xt = x + s + 9;
        int hv = Math.max(14, Font.borderFont.getHeight() + 2);
        int wTen = Math.max(20, w - s - 16);
        int rTen = Font.blackFont.getWidth(ten);
        String c = cap != null && cap.length() > 0 ? "Cấp " + cap : null;
        int wv = c == null ? 0 : Font.borderFont.getWidth(c) + 14;
        String so = id != null && id.length() > 0 ? "#" + id : null;
        int wso = so == null ? 0 : ChuDe.rong(so, ChuDe.NHAN_NHO) + 6;
        boolean vien = c != null;
        if (so != null && rTen + wso + (vien ? wv + 6 : 0) > wTen) {
            so = null;
            wso = 0;
        }
        if (vien && rTen + wv + 6 > wTen) {
            vien = false;   // cấp xuống dòng phụ
        }
        if (vien) {
            GiaoDien.vien(g, x + w - 8 - wv, y + 3, wv, hv, dangDung ? ChuDe.LA : ChuDe.CHINH, c);
        }
        String t = GiaoDien.catChu(Font.blackFont, ten, Math.max(20, wTen - (vien ? wv + 6 : 0) - wso));
        Font.blackFont.drawString(g, t, xt, y + 2, 0);
        if (so != null) {
            ChuDe.chu(g, so, xt + Font.blackFont.getWidth(t) + 5, y + 4, ChuDe.NHAN_NHO, ChuDe.CHU_NHAT, 0);
        }
        if (!vien && c != null) {
            // cấp sau nhãn loại / trạng thái ("Đang đi theo · Cấp 3 · ..."): chật thì bỏ đoạn cuối, nhãn vẫn còn
            if (phu == null) {
                phu = c;
            } else {
                int k = phu.indexOf(" · ");
                if (k >= 0 && phu.startsWith("Đã khoá · ")) {
                    k = phu.indexOf(" · ", k + 3);
                }
                phu = k < 0 ? phu + " · " + c : phu.substring(0, k) + " · " + c + phu.substring(k);
            }
        }
        if (phu != null) {
            int wp = w - s - 14;
            while (Font.arialFont.getWidth(phu) > wp && doanCuoi(phu) > 0) {
                phu = phu.substring(0, doanCuoi(phu));
            }
            Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, phu, wp), xt, y + 3 + Font.blackFont.getHeight(), 0);
        }
    }

    /** Vị trí " · " cuối (CLDC 1.1 không có String.lastIndexOf(String)); -1 = không có. */
    static int doanCuoi(String s) {
        int r = -1;
        for (int i = s.indexOf(" · "); i >= 0; i = s.indexOf(" · ", i + 1)) {
            r = i;
        }
        return r;
    }

    /** Chạm thú trong danh sách: cưỡi / gọi pet; đang cưỡi thú khác thì hỏi đổi (xuống rồi cưỡi con mới). */
    static void chonThu(final AnimalInfo a, final String ten) {
        if (a.typeAnimal == 0 && dangCuoi(mc())) {
            HopXacNhan.hoi("Đổi thú cưỡi", "Xuống thú đang cưỡi và cưỡi " + ten + "?", "Đổi thú", new IAction() {
                public void perform() {
                    GameService.gI().unRideHorse((byte) 0);
                    GameService.gI().doRideAnimal(a.id, a.typeAnimal);
                    DungDo.bao("Đang đổi sang " + ten + "...");
                }
            }, null);
            return;
        }
        GameService.gI().doRideAnimal(a.id, a.typeAnimal);
        DungDo.bao((a.typeAnimal == 0 ? "Đang cưỡi " : "Đang gọi ") + ten + "...");
    }

    static void xuongThu(String ten) {
        HopXacNhan.hoi("Xuống thú", "Xuống " + ten + "?", "Xuống thú", new IAction() {
            public void perform() {
                GameService.gI().unRideHorse((byte) 0);
                DungDo.bao("Đang xuống thú...");
            }
        }, null);
    }

    /**
     * Khối chỉ số kiểu GO: loài to + viên hệ + Lv, thanh HP / PP / EXP mảnh có nhãn, hàng chỉ số sạch (nhãn xám, số
     * đậm, kẻ mờ), nút viên. gon = kèm nút Hành trang / Tiềm năng.
     */
    private void veChiSo(Graphics g, int x, int y, int w, int h, boolean gon) {
        MainChar m = mc();
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int xx = x + 10, yy = y + 6, ww = w - 20;
        // loài + Lv
        String loai = LoaiPokemonClient.ten(m.pokeDex);
        if (loai == null || loai.length() == 0) {
            loai = m.name;
        }
        String lv = "Lv." + m.level;
        int wlv = ChuDe.rong(lv, ChuDe.SO) + 14, hlv = ChuDe.cao(ChuDe.SO) + 2;
        ChuDe.chu(g, ChuDe.cat(loai, ChuDe.TIEU_DE, ww - wlv - 8), xx, yy, ChuDe.TIEU_DE, ChuDe.CHU, 0);
        int ytd = yy + (ChuDe.cao(ChuDe.TIEU_DE) - hlv) / 2;
        GiaoDien.khoiPixel(g, xx + ww - wlv, ytd, wlv, hlv, ChuDe.CHINH);
        ChuDe.chuVienGiua(g, lv, xx + ww - wlv / 2, ytd, hlv, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
        yy += ChuDe.cao(ChuDe.TIEU_DE) + 1;
        int he1 = HePoke.he1(m.pokeDex), he2 = HePoke.he2(m.pokeDex);
        int xb = xx;
        if (he1 >= 0) {
            xb += KieuO.vienHe(g, xb, yy, he1) + 4;
        }
        if (he2 >= 0 && he2 != he1) {
            xb += KieuO.vienHe(g, xb, yy, he2) + 4;
        }
        // 0.9.7 lối vào DUY NHẤT của bảng Danh hiệu: viên danh hiệu đang đeo (hoặc "Danh hiệu") cạnh huy hiệu hệ
        {
            boolean coDh = m.danhHieuTen != null;
            String chu = (coDh ? m.danhHieuTen : "Danh hiệu") + " ›";
            // 0.9.18: tên dài không vừa cạnh huy hiệu hệ (cần thêm dòng / vẫn bị cắt) -> xuống hàng riêng cả bề ngang; vẫn
            // không vừa -> 2-3 dòng (không cắt)
            int wToi = xx + ww - xb - 4, nDong = ManDanhHieu.soDongVien(chu, wToi);
            if (nDong > 1 && xb > xx && (ManDanhHieu.soDongVien(chu, ww - 4) < nDong || !ManDanhHieu.duTen(chu, wToi))) {
                yy += 17;
                xb = xx;
                wToi = ww - 4;
                nDong = ManDanhHieu.soDongVien(chu, wToi);
            }
            int hv = ManDanhHieu.hVien(nDong);
            int wv = ManDanhHieu.vienDu(g, xb + 2, yy, chu, coDh ? m.danhHieuHiem : 0, wToi);
            VungCham.them(g, xb, yy - 4, Math.max(wv + 4, VungCham.MIN), Math.max(hv + 8, VungCham.MIN), new IAction() {
                public void perform() {
                    ManDanhHieu.moTuHoSo();
                }
            }, "danh-hieu");
            if (ChamMoi.co(ChamMoi.DANH_HIEU)) {
                ChuDe.cham(g, xb + 2 + wv - 2, yy + 1);
            }
            yy += hv - ManDanhHieu.hVien();
        }
        yy += 17;
        // thanh mảnh có nhãn
        int hb = ChuDe.dt() ? 6 : 5;
        int lh = ChuDe.cao(ChuDe.PHU);
        if (ww >= 260) {
            int wt = (ww - 12) / 2;
            thanhNhan(g, xx, yy, wt, hb, "HP", m.hp + " / " + m.maxhp, m.hp, m.maxhp, 0);
            thanhNhan(g, xx + ww - wt, yy, wt, hb, "PP", m.mp + " / " + m.maxmp, m.mp, m.maxmp, 1);
        } else {
            thanhNhan(g, xx, yy, ww, hb, "HP", m.hp + " / " + m.maxhp, m.hp, m.maxhp, 0);
            yy += lh + hb + 4;
            thanhNhan(g, xx, yy, ww, hb, "PP", m.mp + " / " + m.maxmp, m.mp, m.maxmp, 1);
        }
        yy += lh + hb + 4;
        // máy chủ chỉ gửi % kinh nghiệm (gói 3: lvpercent phần nghìn), không gửi số EXP
        thanhNhan(g, xx, yy, ww, hb, "EXP", phanNghin(m.lvpercent), m.lvpercent, 1000, 2);
        yy += lh + hb + 6;
        String[][] cs = {
            {"Tấn công", String.valueOf(m.getAttack())},
            {"Phòng thủ", String.valueOf(m.defend)},
            {"P.thủ đặc biệt", String.valueOf(m.defend_magic)},
            {"Tốc độ chạy", String.valueOf(m.speed)},
            {"Chính xác", String.valueOf(m.accurate)},
            {"Né tránh", String.valueOf(m.dodge)},
            {"Chí mạng", phanNghin(chiMang(m.critical, m.baokich))}
        };
        int nutH = gon ? Bang.co(26) : 0;
        int day = y + h - 6 - (gon ? nutH + k : 0);
        int lhs = ChuDe.cao(ChuDe.THUONG) + 3;
        int cot = ww >= 520 ? 3 : (ww >= 200 ? 2 : 1);
        int ke = cot == 3 ? 16 : 12;
        int wc = (ww - (cot - 1) * ke) / cot;
        int n = 0;
        for (int i = 0; i < cs.length; i++) {
            int cc = n % cot, hh = n / cot;
            int yc = yy + hh * lhs;
            if (yc + lhs > day) {
                break;
            }
            hangChiSo(g, xx + cc * (wc + ke), yc, wc, cs[i][0], cs[i][1]);
            n++;
        }
        // chỉ số máy chủ gửi thêm (kháng hệ, hút máu...) nếu còn chỗ - bỏ dòng thừa của game gốc (chiến trường, giáp)
        String[] sv = WindowInfoScr.infoMainCharServer;
        if (sv != null) {
            for (int i = 0; i < sv.length; i++) {
                int cc = n % cot, hh = n / cot;
                int yc = yy + hh * lhs;
                if (yc + lhs > day) {
                    break;
                }
                String s = sv[i];
                if (chiSoRac(s)) {
                    continue;
                }
                s = s.trim();
                int hai = s.indexOf(':');
                hangChiSo(g, xx + cc * (wc + ke), yc, wc, hai > 0 ? s.substring(0, hai) : s, hai > 0 ? s.substring(hai + 1).trim() : "");
                n++;
            }
        }
        if (gon) {
            int wn = (ww - k) / 2;
            Bang.nut(g, xx, y + h - 6 - nutH, wn, nutH, "Hành trang", Bang.PHU, new IAction() {
                public void perform() {
                    ManNhanVat.this.chonO(O_TAT_CA);
                }
            }, "hanh-trang");
            Bang.nut(g, xx + wn + k, y + h - 6 - nutH, wn, nutH, HocChieu.batHL ? "Huấn luyện" : "Tiềm năng",
                    BangTiemNangHD.diemNut() > 0 ? Bang.CHINH : Bang.PHU, new IAction() {
                public void perform() {
                    HanhDong.moTiemNang();
                }
            }, "tiem-nang");
            BangTiemNangHD.chamDo(g, xx + wn + k, y + h - 6 - nutH, wn);
        }
    }

    /** Thanh mảnh kiểu GO: nhãn trái + giá trị phải (chữ phụ) trên, thanh bo tròn cao hb dưới. */
    static void thanhNhan(Graphics g, int x, int y, int w, int hb, String nhan, String gt, long a, long b, int loai) {
        int lh = ChuDe.cao(ChuDe.PHU);
        ChuDe.chu(g, nhan, x, y, ChuDe.SO, ChuDe.CHU_PHU, 0);
        ChuDe.chu(g, gt, x + w, y, ChuDe.PHU, ChuDe.CHU, 1);
        GiaoDien.thanh(g, x, y + lh + 1, w, hb, a, b, loai);
    }

    /** Phần nghìn -> "12,5%". */
    static String phanNghin(int pm) {
        if (pm < 0) {
            pm = 0;
        }
        return pm / 10 + (pm % 10 != 0 ? "," + pm % 10 : "") + "%";
    }

    /**
     * Tỉ lệ chí mạng (phần nghìn) như máy chủ (ChiSoTrangBiGame / GioiHanChiSo): điểm chí mạng thang 300 + bạo kích
     * phần nghìn, giảm dần trên 25% về trần 40%.
     */
    static int chiMang(int diem, int baoKich) {
        long x = Math.max(0, diem) * 1000L / 300L + Math.max(0, baoKich);
        if (x <= 250) {
            return (int) x;
        }
        long du = x - 250, phong = 150;
        return (int) (250 + du * phong / (du + phong));
    }

    /** Dòng chỉ số máy chủ vô nghĩa với Pokémon (khung chiến trường của game gốc: "Sát thương trụ", "Lớp giáp"...). */
    static boolean chiSoRac(String s) {
        if (s == null) {
            return true;
        }
        String t = s.trim();
        int hai = t.indexOf(':');
        if (hai <= 0 || t.substring(hai + 1).trim().length() == 0) {
            return true;
        }
        String l = t.toLowerCase();
        return l.indexOf("chiến trường") >= 0 || l.indexOf("trụ") >= 0 || l.indexOf("giáp") >= 0;
    }

    /** Hàng chỉ số kiểu GO: nhãn xám trái, số đậm phải, đường kẻ mờ dưới. */
    static void hangChiSo(Graphics g, int x, int y, int w, String ten, String gt) {
        int wg = ChuDe.rong(gt, ChuDe.SO);
        ChuDe.chu(g, ChuDe.cat(ten, ChuDe.THUONG, w - wg - 6), x, y, ChuDe.THUONG, ChuDe.CHU_PHU, 0);
        ChuDe.chu(g, gt, x + w, y, ChuDe.SO, ChuDe.CHU, 1);
        g.setColor(ChuDe.NEN_PHU);
        g.fillRect(x, y + ChuDe.cao(ChuDe.THUONG) + 1, w, 1);
    }

    /** 0.9.7: bộ lọc của lưới hành trang (LuoiTui): Tất cả / Trang bị / Thuốc / Nguyên liệu / Khác */
    static final String[] LOC = LuoiTui.LOC;
    static final int LOC_TAT_CA = LuoiTui.LOC_TAT_CA, LOC_THUOC = LuoiTui.LOC_THUOC, LOC_DO = LuoiTui.LOC_DO,
            LOC_NGUYEN_LIEU = LuoiTui.LOC_NGUYEN_LIEU, LOC_NHIEM_VU = LuoiTui.LOC_KHAC;
    /** bộ lọc hành trang đầy đủ */
    int loc;

    /**
     * Mục hành trang đang xem: Integer = thuốc (loại), ItemInInventory = đồ, GemTemplate = nguyên liệu, int[]{id} = vật
     * phẩm nhiệm vụ. Ô trang bị: món đang mặc + món hợp ô; dem[0] = số món đúng loại nhưng thiếu cấp.
     */
    /** danh sách đã lọc lần trước + khoá (dựng lại khi túi / bộ lọc / gói -105 đổi) */
    private Vector dsDem;
    private int khoaDs = 1;
    private int demDem;

    Vector dsHanhTrang(ItemInInventory mac, int[] dem) {
        MainChar m = mc();
        int k = TrangThaiNguoiChoi.phienBan * 31 + this.o * 7 + this.loc;
        k = k * 31 + Bang.vanTay(Char.inventory);
        k = k * 31 + Bang.vanTay(MainChar.gemItem);
        k = k * 31 + Bang.vanTay(MainChar.gemItemKhoa);
        k = k * 31 + Bang.vanTay(m.potions);
        k = k * 31 + Bang.vanTay(MainChar.itemQuest);
        k = k * 31 + System.identityHashCode(mac) + Bang.vanTay(m.wearingItems);
        if (this.dsDem != null && k == this.khoaDs) {
            if (dem != null && dem.length > 0) {
                dem[0] = this.demDem;
            }
            return this.dsDem;
        }
        Vector ds = dsHanhTrang0(mac, dem, m);
        this.dsDem = ds;
        this.khoaDs = k;
        this.demDem = dem != null && dem.length > 0 ? dem[0] : 0;
        return ds;
    }

    private Vector dsHanhTrang0(ItemInInventory mac, int[] dem, MainChar m) {
        Vector ds = new Vector();
        if (this.o != O_TAT_CA) {
            if (mac != null) {
                ds.addElement(mac);
            }
            Vector l = loc(this.o, dem);
            for (int i = 0; i < l.size(); i++) {
                ds.addElement(l.elementAt(i));
            }
            return ds;
        }
        boolean tat = this.loc == LOC_TAT_CA;
        if (tat || this.loc == LOC_THUOC) {
            for (int i = 1; m.potions != null && i < m.potions.length; i++) {
                if (m.potions[i] > 0) {
                    ds.addElement(new Integer(i));
                }
            }
        }
        if (tat || this.loc == LOC_DO) {
            for (int i = 0; i < Char.inventory.size(); i++) {
                ds.addElement(Char.inventory.elementAt(i));
            }
        }
        if (tat || this.loc == LOC_NGUYEN_LIEU) {
            Vector[] g = {MainChar.gemItem, MainChar.gemItemKhoa};
            for (int k = 0; k < g.length; k++) {
                for (int i = 0; g[k] != null && i < g[k].size(); i++) {
                    ds.addElement(g[k].elementAt(i));
                }
            }
        }
        if ((tat || this.loc == LOC_NHIEM_VU) && MainChar.itemQuest != null) {
            for (int i = 0; i < MainChar.itemQuest.length; i++) {
                if (MainChar.itemQuest[i] > 0) {
                    ds.addElement(new int[]{i});
                }
            }
        }
        return ds;
    }

    /** Lưới hành trang (LuoiTui): tất cả + chip lọc, hoặc lọc theo ô trang bị đang chọn (món đang mặc đứng đầu). */
    private void veTui(Graphics g, int x, int y, int w, int h, int s) {
        final boolean tat = this.o < 0 || this.o == O_TAT_CA;
        final ItemInInventory mac = tat ? null : dangMac(this.o);
        int[] dem = new int[1];
        Vector ds = tat ? LuoiTui.ds(this.loc) : dsHanhTrang(mac, dem);
        Object[] vl = null;
        if (tat) {
            vl = new Object[LOC.length];
            for (int i = 0; i < vl.length; i++) {
                final int li = i;
                vl[i] = new IAction() {
                    public void perform() {
                        ManNhanVat.this.loc = li;
                        ManNhanVat.this.ds.den(0);
                    }
                };
            }
        }
        LuoiTui.veGon(g, this.ds, x, y, w, h, s, tat ? this.loc : LOC_DO, ds, tat && this.loc == LOC_TAT_CA, this.xem, vl, new LuoiTui.Bam() {
            public void bam(Object mon) {
                if (mon instanceof ItemInInventory) {
                    ManNhanVat.this.xemMon((ItemInInventory) mon, mon == mac);
                } else if (mon != null) {
                    TheVatPham.mo(mon, TheVatPham.TUI, false, null);
                }
            }
        }, TheVatPham.TUI);
        if (!tat) {
            // đầu lưới khi lọc theo ô: nút lùi + tên ô (thay chip lọc), cùng hàng / mép với chip lọc
            int hd = LuoiTui.gonCaoDau, xd = LuoiTui.GON_CHIP[0], yd = LuoiTui.GON_CHIP[1], wd = LuoiTui.GON_CHIP[2];
            Bang.nutLui(g, xd, yd, hd, new IAction() {
                public void perform() {
                    ManNhanVat.this.o = -1;
                    ManNhanVat.this.ds.den(0);
                }
            });
            String nhanO = "Phù hợp: " + TEN_O[this.o];
            if (ChuDe.rong(nhanO, ChuDe.NHAN) > wd - hd - 8) {
                nhanO = TEN_O[this.o];   // hẹp (J2ME 320x240): chỉ tên ô, không cắt
            }
            ChuDe.chuGiua(g, ChuDe.cat(nhanO, ChuDe.NHAN, wd - hd - 8), xd + hd + 6, yd, hd, ChuDe.NHAN, ChuDe.CHU, 0);
            if (ds.size() == 0) {
                String phu = GOI_Y[this.o];
                if (dem[0] > 0) {
                    phu = dem[0] + " món cần cấp cao hơn. " + phu;
                }
                LuoiTui.dongTrong(g, LuoiTui.GON_LUOI[0], LuoiTui.yLuoi + Math.min(LuoiTui.hLuoi / 4, 40), LuoiTui.GON_LUOI[2],
                        "Chưa có trang bị phù hợp cho ô này. " + phu);
            }
        }
    }

    private void veHanhTrang(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        String tieuDe = this.o == O_TAT_CA ? "Hành trang" : "Hành trang · " + TEN_O[this.o];
        int hd = Bang.co(26);
        Bang.nutLui(g, x + 4, y + 4, hd, new IAction() {
            public void perform() {
                ManNhanVat.this.lui();
            }
        });
        int wDu = 0;
        if (this.o == O_TAT_CA) {
            // thao tác màn mới chưa có (bỏ ra đất, khoá, tách, thăng cấp...) = cửa sổ cũ; nhãn nói rõ chứa gì
            wDu = Math.min(ChuDe.rong(NUT_KHAC, ChuDe.NHAN) + 20, w / 3);
            Bang.nut(g, x + w - 4 - wDu, y + 4, wDu, hd, NUT_KHAC, Bang.TRUNG, new IAction() {
                public void perform() {
                    HanhDong.moBang(0);
                }
            }, "hanh-trang-cu");
        }
        if (this.o == O_THU) {
            // 0.9.16: tab con dưới đã nói loại - bảng hẹp thì tiêu đề gọn (không cắt "Hành trang · Thú c..")
            String[] gon = {tieuDe, "Hành trang thú", "Thú"};
            for (int i = 0; i < gon.length && Font.blackFont.getWidth(tieuDe) > w - hd - 20 - wDu; i++) {
                tieuDe = gon[i];
            }
        }
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, tieuDe, w - hd - 20 - wDu), x + hd + 10, y + 4 + (hd - Font.blackFont.getHeight()) / 2, 0);
        int yl = y + 4 + hd + k;
        if (this.o == O_THU) {
            veDanhSachThu(g, x + 4, yl, w - 8, y + h - 4 - yl);
            return;
        }
        if (this.o == O_TAT_CA) {
            Object[] vl = new Object[LOC.length];
            for (int i = 0; i < vl.length; i++) {
                final int li = i;
                vl[i] = new IAction() {
                    public void perform() {
                        ManNhanVat.this.loc = li;
                        ManNhanVat.this.ds.den(0);
                    }
                };
            }
            yl += Bang.hangChip(g, x + 6, yl, w - 12, LOC, this.loc, vl) + k;
        }
        int hl = y + h - 4 - yl;
        final ItemInInventory mac = this.o == O_TAT_CA ? null : dangMac(this.o);
        int[] dem = new int[1];
        final Vector ds = dsHanhTrang(mac, dem);
        if (ds.size() == 0) {
            String phu = this.o == O_TAT_CA ? "Nhặt đồ rơi từ Pokémon hoang dã hoặc mua ở Poké Mart." : GOI_Y[this.o];
            if (dem[0] > 0) {
                phu = dem[0] + " món cần cấp cao hơn. " + phu;
            }
            Bang.trong(g, x + 4, yl, w - 8, hl, this.o == O_TAT_CA ? (this.loc == 0 ? "Hành trang trống" : "Không có " + LOC[this.loc].toLowerCase())
                    : "Chưa có trang bị phù hợp cho ô này", phu);
            return;
        }
        Bang.danhSach(g, this.ds, x + 4, yl, w - 8, hl, ds.size(), Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                Object o = ds.elementAt(i);
                if (o instanceof Integer) {
                    veHangThuoc(g, x, y, w, h, i, ((Integer) o).intValue());
                } else if (o instanceof ItemInInventory) {
                    final ItemInInventory it = (ItemInInventory) o;
                    final boolean laMac = it == mac;
                    veHangMon(g, x, y, w, h, i, it, laMac, new IAction() {
                        public void perform() {
                            ManNhanVat.this.xemMon(it, laMac);
                        }
                    });
                } else if (o instanceof game.model.GemTemplate) {
                    veHangNguyenLieu(g, x, y, w, h, i, (game.model.GemTemplate) o, MainChar.gemItemKhoa != null && MainChar.gemItemKhoa.contains(o));
                } else if (o instanceof int[]) {
                    veHangNhiemVu(g, x, y, w, h, i, ((int[]) o)[0]);
                }
            }
        });
    }

    /** Ô ảnh thu nhỏ ở đầu hàng (cạnh h - 6). */
    static int oDau(Graphics g, int x, int y, int h) {
        int s = h - 6;
        GiaoDien.khoi(g, x + 3, y + 3, s, s, ChuDe.BO_O, ChuDe.NEN_PHU);
        return s;
    }

    /** Hàng chung: tên (đậm) + số chính canh phải + dòng phụ. */
    static void chuHang(Graphics g, int x, int y, int w, int h, int s, String ten, String so, String phu) {
        int xt = x + s + 9;
        int wso = so == null ? 0 : Font.blackFont.getWidth(so) + 10;
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, ten, w - s - 16 - wso), xt, y + 2, 0);
        if (so != null) {
            Font.blackFont.drawString(g, so, x + w - 8, y + 2, 1);
        }
        if (phu != null) {
            Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, phu, w - s - 14), xt, y + 3 + Font.blackFont.getHeight(), 0);
        }
    }

    /** Như chuHang nhưng tên dài ngắt sang đầu dòng 2 (trước dòng phụ) thay vì cắt - hàng trang bị trong túi. */
    static void chuHangNgat(Graphics g, int x, int y, int w, int h, int s, String ten, String so, String phu) {
        int xt = x + s + 9;
        int wso = so == null ? 0 : Font.blackFont.getWidth(so) + 10;
        GiaoDien.tenHaiDong(g, Font.blackFont, Font.arialFont, ten, phu, xt, y + 2, w - s - 16 - wso, y + 3 + Font.blackFont.getHeight(),
                w - s - 14);
        if (so != null) {
            Font.blackFont.drawString(g, so, x + w - 8, y + 2, 1);
        }
    }

    static void veHangNguyenLieu(Graphics g, int x, int y, int w, int h, int i, final game.model.GemTemplate gem, final boolean khoa) {
        final game.model.GemTemp t = Res.getGemByID(gem.id);
        final String ten = t == null ? "Nguyên liệu" : t.name;
        Bang.hang(g, x, y, w, h, i, false, new IAction() {
            public void perform() {
                if (t != null && t.type == 4) {
                    HopXacNhan.hoi("Dùng vật phẩm", ten + (t.decript.length() > 0 ? ": " + t.decript : ""), "Dùng", new IAction() {
                        public void perform() {
                            GameService.gI().douseGemItem(gem.id, (byte) (khoa ? 1 : 0));
                        }
                    }, null);
                } else {
                    DungDo.bao(ten + (t != null && t.decript.length() > 0 ? ": " + t.decript : ""));
                }
            }
        }, "nguyen-lieu-" + i);
        int s = oDau(g, x, y, h);
        if (t != null) {
            IconVatPham.ve(g, (short) (t.idImage + 6500), x + 3 + s / 2, y + 3 + s / 2, s, IconVatPham.DP_NGOC);
        }
        chuHang(g, x, y, w, h, s, ten, "x" + gem.number, khoa ? "Nguyên liệu · khoá" : "Nguyên liệu");
    }

    static void veHangNhiemVu(Graphics g, int x, int y, int w, int h, int i, final int id) {
        final String ten = id < game.model.ItemQuest.NAME_ITEM.length ? game.model.ItemQuest.NAME_ITEM[id] : "Vật phẩm nhiệm vụ";
        Bang.hang(g, x, y, w, h, i, false, new IAction() {
            public void perform() {
                DungDo.bao(ten + ": vật phẩm nhiệm vụ, giao cho NPC nhiệm vụ.");
            }
        }, "nhiem-vu-" + i);
        int s = oDau(g, x, y, h);
        if (id < game.model.ItemQuest.ICON_IMG.length) {
            IconVatPham.ve(g, (short) (game.model.ItemQuest.ICON_IMG[id] + 8000), x + 3 + s / 2, y + 3 + s / 2, s, IconVatPham.DP_NV);
        }
        chuHang(g, x, y, w, h, s, ten, "x" + MainChar.itemQuest[id], "Vật phẩm nhiệm vụ");
    }


    /** Hàng thuốc: chạm = dùng ngay (như nút HP / PP của HUD). */
    static void veHangThuoc(Graphics g, int x, int y, int w, int h, int i, final int loai) {
        Bang.hang(g, x, y, w, h, i, false, new IAction() {
            public void perform() {
                if (KhanPk.laKhan(loai)) {
                    KhanPk.hoi(loai);   // 0.9.8: khăn PK hỏi xác nhận "Đeo khăn (bật PK)" / "Tháo khăn (tắt PK)"
                    return;
                }
                try {
                    Canvas.gameScr.doUsePotion(loai);
                } catch (Throwable e) {
                    // chưa có dữ liệu thuốc
                }
            }
        }, "thuoc-" + loai);
        int s = oDau(g, x, y, h);
        boolean pp = loai >= 4 && loai <= 6 || loai == 23 || loai == 24 || loai == 95 || loai == 96;
        veThuoc(g, loai, x + 3 + s / 2, y + 3 + s / 2, s);
        String ten = null;
        try {
            if (MainChar.listPotion != null && loai < MainChar.listPotion.length && MainChar.listPotion[loai] != null) {
                ten = MainChar.listPotion[loai].name;
            }
        } catch (Throwable e) {
            ten = null;
        }
        if (ten == null || ten.length() == 0) {
            ten = pp ? "Thuốc hồi PP" : "Thuốc hồi HP";
        }
        chuHang(g, x, y, w, h, s, ten, "x" + mc().potions[loai], "Chạm để dùng");
    }

    /** HUD (HudRong): icon thật bình thuốc loai trong nút tròn cạnh s. */
    public static void veThuocHud(Graphics g, int loai, int cx, int cy, int s) {
        veThuoc(g, loai, cx, cy, s);
    }

    /** Icon thật của thuốc loai (ảnh 5500 + chỉ số ảnh của bảng thuốc); thiếu thì bình thuốc vẽ. */
    static void veThuoc(Graphics g, int loai, int cx, int cy, int s) {
        int idx = -1;
        try {
            if (MainChar.listPotion != null && loai < MainChar.listPotion.length && MainChar.listPotion[loai] != null) {
                idx = MainChar.listPotion[loai].index;
            }
        } catch (Throwable e) {
            idx = -1;
        }
        if (idx >= 0) {
            IconVatPham.ve(g, (short) (idx + 5500), cx, cy, s, IconVatPham.DP_THUOC);
        } else {
            boolean pp = loai >= 4 && loai <= 6 || loai == 23 || loai == 24 || loai == 95 || loai == 96;
            int k = s * 3 / 5;
            BieuTuong.ve(g, BieuTuong.THUOC, cx - k / 2, cy - k / 2, k, pp ? ChuDe.PP : ChuDe.DO, ChuDe.NEN);
        }
    }

    static void veHangMon(Graphics g, int x, int y, int w, int h, int i, ItemInInventory it, boolean laMac, Object viec) {
        Bang.hang(g, x, y, w, h, i, laMac, viec, "mon-" + i);
        ItemTemplate t = mau(it);
        int s = oDau(g, x, y, h);
        int hiem = KieuO.mauHiem(it.colorName);
        if (hiem >= 0 && !IconVatPham.veKhungHiem(g, it.colorName, x + 3 + s / 2, y + 3 + s / 2, s)) {
            KieuO.khungHiem(g, x + 3, y + 3, s, hiem);
        }
        if (t != null) {
            Bang.icon(g, t.idIcon, x + 3 + s / 2, y + 3 + s / 2, s >= 26 ? s / 22f : 1f);
        }
        String ten = t == null ? "?" : ChuDe.boCap(t.name);   // cột bên đã ghi "Cấp N": tên bỏ " · Cấp N"
        if (it.plusTemplate > 0) {
            ten = ten + " +" + it.plusTemplate;
        }
        String loai = t == null ? null : BangTrangBi.tenLoai(t.type);
        chuHangNgat(g, x, y, w, h, s, ten, "Cấp " + capCan(it, t), (laMac ? "Đang mặc" + (loai != null ? " · " : "") : "") + (loai == null ? "" : loai));
    }

    /**
     * Danh sách thú: hàng đầu = thú đang cưỡi (máy chủ không gửi con đang cưỡi trong gói túi thú - place 1; tên lấy từ
     * infoAnimal của gói 15) -> chạm = xuống; sau đó thú cưỡi + pet đi theo trong túi (gói 16 loại 2).
     * 0.9.13 (máy chủ gửi khối đang dùng): con ĐANG CƯỠI + pet ĐANG ĐI THEO đứng đầu (thật, chọn được: Xuống thú / Khoá).
     * 0.9.16 (chủ dự án: "chia rõ ra thú cưỡi và đi theo"): hai TAB CON trên đầu "Thú cưỡi (n)" / "Đi theo (m)", mỗi tab
     * chỉ một loại (typeAnimal 0 / khác 0), con đang dùng của loại đứng đầu tab; "Ô thú" vẫn là tổng chung; Thả trùng
     * chỉ trong tab đang xem; phím trái / phải đổi tab con (mép ngoài: đổi tab màn như cũ).
     */
    private void veDanhSachThu(Graphics g, int x, int y, int w, int h) {
        veDanhSachThu(g, x, y, w, h, null, false);
    }

    /** gon (0.9.16 điện thoại): một hàng đầu [lùi][tab con][Ô thú · Trùng · + Ô], lưới 2 cột khi đủ rộng. */
    private void veDanhSachThu(Graphics g, int x, int y, int w, int h, Object lui, boolean gon) {
        final MainChar m = mc();
        final boolean moi = QuanLyThu.coDung;
        if (this.thuChon != null && !QuanLyThu.conTrongDs(this.thuChon)) {
            this.thuChon = null;
        }
        if (this.thuChon != null && QuanLyThu.loai(this.thuChon) != this.loaiThu) {
            this.loaiThu = QuanLyThu.loai(this.thuChon);   // con đang chọn (ô Đi theo / sân khấu) ở tab của nó
        }
        final int loaiXem = this.loaiThu;
        // hàng "đang cưỡi" kiểu cũ (chỉ ở tab Thú cưỡi): máy chủ cũ (đoán), hoặc thú cưỡi cũ bằng thuốc (không chiếm ô)
        final int nCu = moi ? (QuanLyThu.thuCu != null ? 1 : 0) : (dangCuoi(m) ? 1 : 0);
        final Vector cuoi = QuanLyThu.dsLoai(QuanLyThu.LOAI_CUOI), diTheo = QuanLyThu.dsLoai(QuanLyThu.LOAI_DI_THEO);
        final Vector dsLoai = loaiXem == QuanLyThu.LOAI_CUOI ? cuoi : diTheo;
        final int nCuoi = loaiXem == QuanLyThu.LOAI_CUOI ? nCu : 0;
        int n = dsLoai.size() + nCuoi;
        int k = Bang.cach();
        // tab con: Thú cưỡi (n) / Đi theo (m)
        boolean daOThu = false, daThaoTac = false;
        if (gon) {
            int hc = Bang.caoTab();
            int xx = x;
            if (lui != null) {
                Bang.nutLui(g, xx, y, hc, lui);
                xx += hc + k;
            }
            String[] chu = {"Thú cưỡi (" + (cuoi.size() + nCu) + ")", "Đi theo (" + diTheo.size() + ")"};
            int wl = Math.max(ChuDe.rong(chu[0], ChuDe.NHAN), ChuDe.rong(chu[1], ChuDe.NHAN)) + 30;
            int conLai = x + w - xx;
            int wTab = Math.min(2 * wl, conLai);
            // hàng đầu còn chỗ: Ô thú (hoặc thao tác con đang chọn) cùng hàng; không thì hàng hai. Con đang chọn: thanh
            // thao tác THAY hàng Ô thú (không thêm hàng dưới) - danh sách giữ đủ chỗ
            int wO = ChuDe.rong("Ô thú: " + QuanLyThu.soThu + "/" + QuanLyThu.sucChua, ChuDe.NHAN) + 8
                    + 2 * Math.max(VungCham.MIN, ChuDe.rong("Trùng 88", ChuDe.SO) + 12) + 2 * k;
            int canO = this.thuChon != null ? 3 * Math.max(VungCham.MIN, 76) + 2 * k : wO;
            boolean motHang = (QuanLyThu.co || this.thuChon != null) && conLai - wTab - k >= canO;
            if (!motHang) {
                wTab = conLai;
            }
            if (!ChuDe.cat(chu[0], ChuDe.NHAN, wTab / 2 - 14).equals(chu[0]) || !ChuDe.cat(chu[1], ChuDe.NHAN, wTab / 2 - 14).equals(chu[1])) {
                chu[0] = "Cưỡi (" + (cuoi.size() + nCu) + ")";
            }
            Object[] viec = new Object[2];
            for (int i = 0; i < 2; i++) {
                final int li = i;
                viec[i] = new IAction() {
                    public void perform() {
                        ManNhanVat.this.doiLoaiThu(li);
                    }
                };
            }
            Bang.ranhTab(g, xx, y + hc - 2, wTab);
            Bang.tienToTab = "thu-loai-";
            Bang.phanDoan(g, xx, y, wTab, hc, chu, loaiXem, viec);
            int xO = motHang ? xx + wTab + k : x, yO = motHang ? y : y + hc + k;
            if (!motHang && (QuanLyThu.co || this.thuChon != null)) {
                y += hc + k;
                h -= hc + k;
            }
            if (this.thuChon != null) {
                veThaoTacThu(g, xO, yO, x + w - xO, hc, this.thuChon, nCuoi);
                daThaoTac = true;
            } else if (QuanLyThu.co) {
                veThanhOThu(g, xO, yO, x + w - xO, hc, loaiXem);
            }
            daOThu = true;
            y += hc + k;
            h -= hc + k;
        } else {
            int hc = Bang.caoTab();
            String[] chu = {"Thú cưỡi (" + (cuoi.size() + nCu) + ")", "Đi theo (" + diTheo.size() + ")"};
            int wt = (w - 2) / 2 - 14;   // như Bang.phanDoan (ChuDe.cat(chu, NHAN, ww - 14))
            if (!ChuDe.cat(chu[0], ChuDe.NHAN, wt).equals(chu[0]) || !ChuDe.cat(chu[1], ChuDe.NHAN, wt).equals(chu[1])) {
                chu[0] = "Cưỡi (" + (cuoi.size() + nCu) + ")";   // chật (J2ME): chữ gọn, không cắt "Thú cư.."
            }
            Object[] viec = new Object[2];
            for (int i = 0; i < 2; i++) {
                final int li = i;
                viec[i] = new IAction() {
                    public void perform() {
                        ManNhanVat.this.doiLoaiThu(li);
                    }
                };
            }
            Bang.ranhTab(g, x, y + hc - 2, w);
            Bang.tienToTab = "thu-loai-";
            Bang.phanDoan(g, x + 1, y, w - 2, hc, chu, loaiXem, viec);
            y += hc + k;
            h -= hc + k;
        }
        // thanh trên (máy chủ có quản lý thú): "Ô thú: n/max" (tổng chung) + Thả trùng (tab này) + Thêm ô
        if (QuanLyThu.co && !daOThu) {
            int hb = Bang.co(24);
            veThanhOThu(g, x, y, w, hb, loaiXem);
            y += hb + k;
            h -= hb + k;
        }
        // thanh dưới: thao tác thú đang chọn (cưỡi / gọi / xuống, khoá, thả)
        if (this.thuChon != null && !daThaoTac) {
            int hb = Bang.co(26);
            veThaoTacThu(g, x, y + h - hb, w, hb, this.thuChon, nCuoi);
            h -= hb + k;
        }
        if (n == 0) {
            if (loaiXem == QuanLyThu.LOAI_CUOI) {
                Bang.trong(g, x, y, w, h, "Chưa có thú cưỡi", "Mua Sáo gọi ở Cửa hàng đặc biệt");
            } else {
                Bang.trong(g, x, y, w, h, "Chưa có Pokémon đi theo", "Bắt hoặc ấp trứng");
            }
            return;
        }
        // lưới 2-3 cột (gọn, đủ rộng): mỗi ô >= 128 px logic (tên Rapidash / Dragonite không cắt; chật thì cấp xuống dòng phụ)
        final int cot = gon ? Math.max(1, Math.min(3, (w - 10 + k) / (128 + k))) : 1;
        final int nThu = n;
        int nHang = (n + cot - 1) / cot;
        VUA_DS[0] = x;
        VUA_DS[1] = y;
        VUA_DS[2] = w;
        VUA_DS[3] = h;
        // gọn: hàng thấp = cỡ chạm tối thiểu (tên + một dòng phụ), vẫn >= VungCham.MIN
        int rh = gon ? Math.max(30, VungCham.MIN + 1) : Bang.caoHang();
        VUA_DS[4] = rh;
        VUA_DS[5] = n;
        VUA_DS[6] = cot;
        Bang.danhSach(g, this.ds, x, y, w, h, nHang, rh, new Bang.Hang() {
            public void ve(Graphics g, int hang, int x, int y, int w, int h) {
                int kc = Bang.cach();
                int wo = (w - (cot - 1) * kc) / cot;
                for (int c = 0; c < cot; c++) {
                    int i = hang * cot + c;
                    if (i < nThu) {
                        o(g, i, x + c * (wo + kc), y, wo, h);
                    }
                }
            }

            void o(Graphics g, int i, int x, int y, int w, int h) {
                int s;
                if (i < nCuoi) {
                    final String[] t = tachTen(moi ? QuanLyThu.thuCu : m.infoAnimal);
                    final String ten = t[0].length() > 0 ? t[0] : "Thú cưỡi";
                    Bang.hang(g, x, y, w, h, i, true, new IAction() {
                        public void perform() {
                            xuongThu(ten);
                        }
                    }, "thu-dang-cuoi");
                    s = oDau(g, x, y, h);
                    veHinhThu(g, m.pokeThuDex, m.pokeThuShiny, (short) (m.idPaintIconAnimal + 7500), x + 3 + s / 2, y + 3 + s / 2, s);
                    hangThu(g, x, y, w, h, s, ten, t[1], null, moi ? "Đang cưỡi · không chiếm ô · chạm để xuống"
                            : "Đang cưỡi · chạm để xuống", true);
                    return;
                }
                final AnimalInfo a = (AnimalInfo) dsLoai.elementAt(i - nCuoi);
                final boolean dung = QuanLyThu.dangDung(a);
                final String[] t = tachTen(a.name);
                final String ten = t[0].length() > 0 ? t[0] : (a.typeAnimal == 0 ? "Thú cưỡi" : "Pokémon đi theo");
                final boolean quanLy = QuanLyThu.coThongTin(a);
                int vt = dung ? QuanLyThu.dung.indexOf(a) : Char.animal.indexOf(a);
                Bang.hang(g, x, y, w, h, i, a == ManNhanVat.this.thuChon, new IAction() {
                    public void perform() {
                        if (quanLy) {
                            // chạm = chọn (thanh thao tác dưới: cưỡi / gọi, khoá, thả); chạm lại = bỏ chọn
                            ManNhanVat.this.thuChon = ManNhanVat.this.thuChon == a ? null : a;
                        } else {
                            chonThu(a, ten);
                        }
                    }
                }, dung ? "thu-dung-" + vt : "thu-" + vt);
                s = oDau(g, x, y, h);
                veHinhThu(g, dexThu(a), shinyThu(a), (short) (a.idPaint + 7500), x + 3 + s / 2, y + 3 + s / 2, s);
                String phu;
                String dau = quanLy && QuanLyThu.daKhoa(a) ? "Đã khoá · " : "";
                String nhan = nhanThu(a);
                if (quanLy) {
                    phu = dau + nhan + (t[3].length() > 0 ? " · hạn " + t[3] : "") + " · chạm để chọn";
                } else if (a.typeAnimal == 0) {
                    phu = nCu > 0 ? "Thú cưỡi · chạm để đổi" : "Thú cưỡi · chạm để cưỡi";
                } else {
                    phu = "Đi theo" + (t[3].length() > 0 ? " · hạn " + t[3] : "") + " · chạm để gọi";
                }
                hangThu(g, x, y, w, h, s, ten, a.level > 0 ? String.valueOf(a.level) : t[1], t[2], phu,
                        dung || a == ManNhanVat.this.thuChon);
            }
        });
    }

    /** Kiểm thử: vùng danh sách thú vừa vẽ {x, y, w, h, cao hàng, số hàng}. */
    public static final int[] VUA_DS = new int[7];

    /** Tab con danh sách thú đang xem (QuanLyThu.LOAI_CUOI / LOAI_DI_THEO) - giữ lần dùng cuối cho tab Thú. */
    int loaiThu;

    /** Đổi tab con danh sách thú: về đầu danh sách, bỏ chọn con loại kia. */
    void doiLoaiThu(int l) {
        if (l != this.loaiThu) {
            this.loaiThu = l;
            this.ds.den(0);
            if (this.thuChon != null && QuanLyThu.loai(this.thuChon) != l) {
                this.thuChon = null;
            }
        }
    }

    /** Màn đang hiện danh sách thú (tab Thú, hoặc Trang bị › Thú cưỡi ở danh sách). */
    boolean dangXemDsThu() {
        return this.tab == TAB_THU || (this.tab == TAB_TRANG_BI && this.o == O_THU && this.xem == null);
    }

    /** Nhãn loại / trạng thái một thú: "Đang cưỡi", "Đang đi theo", "Thú cưỡi" hay "Đi theo". */
    public static String nhanThu(AnimalInfo a) {
        if (a != null && a == thuDangCuoi()) {
            return "Đang cưỡi";
        }
        if (a != null && a == petDiTheo()) {
            return "Đang đi theo";
        }
        if (QuanLyThu.dangDung(a)) {
            return a.typeAnimal == 0 ? "Thú cưỡi · đang dùng" : "Đi theo · đang dùng";
        }
        return a != null && a.typeAnimal == 0 ? "Thú cưỡi" : "Đi theo";
    }

    /**
     * Sân khấu Trang bị khi chọn ô Thú cưỡi: thú đang chọn ở danh sách bên phải (hình to + tên + cấp + xu thả), không thì
     * thú đang cưỡi (người chơi trên lưng thú + tên, cấp), không thì người chơi + gợi ý "chạm một thú bên phải để cưỡi".
     * Trước đây ô này chỉ vẽ người chơi: không cưỡi thì không có gì về thú (hộp trống), sprite người chơi đang tải thì
     * trống hẳn. 0.9.13: thú đang cưỡi / pet đang đi theo theo khối đang dùng của máy chủ (không đoán).
     */
    private void veXemThu(Graphics g, MainChar m, int x, int yTren, int w, int cx, int yChan, float z) {
        AnimalInfo a = this.thuChon != null && QuanLyThu.conTrongDs(this.thuChon) ? this.thuChon : null;
        AnimalInfo cu = thuDangCuoi(), dt = petDiTheo();
        if (a == null && cu != null) {
            a = cu;   // mặc định: con đang cưỡi
        }
        String dong1, dong2;
        if (a != null) {
            String[] t = tachTen(a.name);
            int dex = dexThu(a);
            boolean sh = shinyThu(a);
            String ten = t[0].length() > 0 ? t[0] : (a.typeAnimal == 0 ? "Thú cưỡi" : "Pokémon đi theo");
            if (a == cu) {
                veNguoiCuoi(g, m, a, cx, yChan, z);
                dong1 = "Đang cưỡi " + ten;
            } else {
                if (coHinhThat(dex, sh)) {
                    vePoke(g, dex, sh, cx, yChan, z, NgoaiHinhVe.huongThe(Canvas.gameTick));
                } else {
                    int sz = Math.max(24, (int) (30 * z));
                    veHinhThu(g, dex, sh, (short) (a.idPaint + 7500), cx, yChan - sz / 2, sz);
                }
                dong1 = ten;
            }
            String cap = a.level > 0 ? String.valueOf(a.level) : t[1];
            int xu = QuanLyThu.xuTha(a);
            dong2 = (cap.length() > 0 ? "Cấp " + cap + " · " : "") + nhanThu(a)
                    + (QuanLyThu.daKhoa(a) ? " · Đã khoá" : (QuanLyThu.dangDung(a) ? "" : (xu > 0 ? " · Thả: +" + TienTe.so(xu) + " xu"
                    : (QuanLyThu.hetTranNgay(a) ? " · " + QuanLyThu.HET_TRAN : ""))));
            if (a == cu && dt != null) {
                String[] tp = tachTen(dt.name);
                dong2 = dong2 + " · " + (tp[0].length() > 0 ? tp[0] : "Pokémon") + " đi theo";
            }
        } else if (dangCuoi(m)) {
            veNguoiChoi(g, m, cx, yChan, z);
            String[] t = tachTen(QuanLyThu.coDung && QuanLyThu.thuCu != null ? QuanLyThu.thuCu : m.infoAnimal);
            dong1 = "Đang cưỡi " + (t[0].length() > 0 ? t[0] : "thú");
            dong2 = t[1].length() > 0 ? "Cấp " + t[1] + " · chạm hàng đầu bên phải để xuống" : "Chạm hàng đầu bên phải để xuống";
        } else {
            veNguoiChoi(g, m, cx, yChan, z);
            dong1 = "Chưa cưỡi thú";
            boolean coThuCuoi = false;
            for (int i = 0; i < Char.animal.size(); i++) {
                coThuCuoi |= ((AnimalInfo) Char.animal.elementAt(i)).typeAnimal == 0;
            }
            String goiY = coThuCuoi ? "Chạm một thú cưỡi bên phải để cưỡi" : (Char.animal.size() > 0 && !QuanLyThu.coDung
                    ? "Chạm một thú bên phải để cưỡi" : "Thú cưỡi có từ nhiệm vụ, trứng, cửa hàng");
            if (this.loaiThu == QuanLyThu.LOAI_DI_THEO && dt == null) {
                goiY = "Chạm một Pokémon bên phải để gọi đi theo";
            }
            if (dt != null) {
                String[] tp = tachTen(dt.name);
                dong2 = (tp[0].length() > 0 ? tp[0] : "Pokémon") + " đang đi theo · " + goiY;
            } else {
                dong2 = goiY;
            }
        }
        int y = yTren + 6;
        ChuDe.chu(g, ChuDe.cat(dong1, ChuDe.NHAN, w - 8), cx, y, ChuDe.NHAN, ChuDe.CHU, 2);
        ChuDe.chu(g, ChuDe.cat(dong2, ChuDe.NHAN_NHO, w - 8), cx, y + ChuDe.cao(ChuDe.NHAN) + 1, ChuDe.NHAN_NHO, ChuDe.CHU_PHU, 2);
    }

    /** Thú đang chọn trong danh sách (máy chủ có quản lý thú) - thanh thao tác + xem trước ở sân khấu. */
    AnimalInfo thuChon;

    /** Thanh "Ô thú: n/max" (tổng chung) + nút Thả bản trùng + Thêm ô (chung cho tab Thú và Trang bị › Thú cưỡi). */
    static void veThanhOThu(Graphics g, int x, int y, int w, int h) {
        veThanhOThu(g, x, y, w, h, -1);
    }

    /** loaiThu: tab con đang xem - Thả trùng chỉ thả bản trùng loại đó (-1 = cả hai). */
    static void veThanhOThu(Graphics g, int x, int y, int w, int h, final int loaiThu) {
        int k = Bang.cach();
        String nhan = "Ô thú: " + QuanLyThu.soThu + "/" + QuanLyThu.sucChua;
        boolean toiDa = QuanLyThu.sucChua >= QuanLyThu.toiDa;
        String chuThem = toiDa ? "Tối đa" : (QuanLyThu.soVe > 0 ? "Dùng vé (" + QuanLyThu.soVe + ")" : "Thêm ô");
        int nTrung = QuanLyThu.banTrung(loaiThu).size();
        String chuTrung = "Thả trùng" + (nTrung > 0 ? " (" + nTrung + ")" : "");
        int wThem = Math.max(VungCham.MIN, ChuDe.rong(chuThem, ChuDe.SO) + 16);
        int wTrung = Math.max(VungCham.MIN, ChuDe.rong(chuTrung, ChuDe.SO) + 16);
        int wNhan = ChuDe.rong(nhan, ChuDe.NHAN) + 4;
        if (wNhan + wThem + wTrung + 2 * k > w) {
            // chật (J2ME): chữ nút gọn
            chuThem = toiDa ? "Tối đa" : "+ Ô";
            chuTrung = "Trùng" + (nTrung > 0 ? " " + nTrung : "");
            wThem = Math.max(VungCham.MIN, ChuDe.rong(chuThem, ChuDe.SO) + 12);
            wTrung = Math.max(VungCham.MIN, ChuDe.rong(chuTrung, ChuDe.SO) + 12);
        }
        int xThem = x + w - wThem;
        Bang.nut(g, xThem, y, wThem, h, chuThem, toiDa ? Bang.TAT : Bang.CHINH, new IAction() {
            public void perform() {
                QuanLyThu.hoiThemO();
            }
        }, "thu-them-o");
        int xTrung = xThem - k - wTrung;
        Bang.nut(g, xTrung, y, wTrung, h, chuTrung, nTrung > 0 ? Bang.NGUY : Bang.TAT, new IAction() {
            public void perform() {
                QuanLyThu.hoiThaTrung(loaiThu);
            }
        }, "thu-tha-trung");
        ChuDe.chu(g, ChuDe.cat(nhan, ChuDe.NHAN, Math.max(20, xTrung - x - k)), x + 2, y + (h - ChuDe.cao(ChuDe.NHAN)) / 2,
                ChuDe.NHAN, QuanLyThu.day() ? ChuDe.DO : ChuDe.CHU, 0);
    }

    /** Thanh thao tác thú đang chọn: Cưỡi / Gọi, Khoá / Mở khoá, Thả (+xu). */
    void veThaoTacThu(Graphics g, int x, int y, int w, int h, final AnimalInfo a, int nCuoi) {
        int k = Bang.cach();
        final String[] t = tachTen(a.name);
        final String ten = t[0].length() > 0 ? t[0] : (a.typeAnimal == 0 ? "Thú cưỡi" : "Pokémon đi theo");
        final boolean khoa = QuanLyThu.daKhoa(a);
        boolean tha = QuanLyThu.thaDuoc(a);
        int xu = QuanLyThu.xuTha(a);
        final boolean laCuoi = a == thuDangCuoi(), laDiTheo = a == petDiTheo();
        String c1 = laCuoi ? "Xuống thú" : laDiTheo ? "Đang đi theo"
                : a.typeAnimal == 0 ? (dangCuoi(mc()) ? "Đổi thú" : "Cưỡi") : "Gọi";
        String c2 = khoa ? "Mở khoá" : "Khoá";
        String c3 = tha ? (xu > 0 ? "Thả +" + TienTe.so(xu) + " xu" : (QuanLyThu.hetTranNgay(a) ? "Thả (hết xu hôm nay)" : "Thả"))
                : "Không thả được";
        // con ĐANG DÙNG (đang cưỡi / đang đi theo): không có nút Thả (máy chủ không cho thả) - hai nút Xuống / Khoá rộng
        final boolean dung = QuanLyThu.dangDung(a);
        int w1 = dung ? (w - k) / 2 : (w - 2 * k) / 3;
        if (laCuoi && ChuDe.rong(c1, ChuDe.SO) + 12 > w1) {
            c1 = "Xuống";
        } else if (laDiTheo && ChuDe.rong(c1, ChuDe.SO) + 12 > w1) {
            c1 = "Đi theo";
        }
        Bang.nut(g, x, y, w1, h, c1, laDiTheo ? Bang.TAT : Bang.XAC_NHAN, new IAction() {
            public void perform() {
                if (laCuoi) {
                    xuongThu(ten);
                } else if (laDiTheo) {
                    DungDo.bao(ten + " đang đi theo bạn. Gọi pet khác để đổi.");
                } else {
                    chonThu(a, ten);
                }
            }
        }, "thu-dung");
        Bang.nut(g, x + w1 + k, y, dung ? w - w1 - k : w1, h, c2, Bang.GO, new IAction() {
            public void perform() {
                QuanLyThu.doiKhoa(a, !khoa);
            }
        }, "thu-khoa");
        if (dung) {
            return;
        }
        Bang.nut(g, x + 2 * (w1 + k), y, w - 2 * (w1 + k), h, c3, tha ? Bang.NGUY : Bang.TAT, new IAction() {
            public void perform() {
                QuanLyThu.hoiTha(a, ten);
            }
        }, "thu-tha");
    }

    /** Thẻ chi tiết món: so sánh với món đang mặc ở cùng ô + nút Trang bị / Gỡ. */
    private void veChiTiet(Graphics g, int x, int y, int w, int h) {
        final ItemInInventory it = this.xem;
        ItemTemplate t = mau(it);
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int hd = Bang.co(26);
        Bang.nutLui(g, x + 4, y + 4, hd, new IAction() {
            public void perform() {
                ManNhanVat.this.lui();
            }
        });
        int s = Math.max(30, hd + 6);
        int xi = x + hd + 10;
        KieuO.oDo(g, xi, y + 4, s, true, false);
        int hiem = KieuO.mauHiem(it.colorName);
        if (hiem >= 0 && !IconVatPham.veKhungHiem(g, it.colorName, xi + s / 2, y + 4 + s / 2, s)) {
            KieuO.khungHiem(g, xi, y + 4, s, hiem);
        }
        if (t != null) {
            Bang.icon(g, t.idIcon, xi + s / 2, y + 4 + s / 2, s / 22f);
        }
        int xt = xi + s + 6, wt = x + w - 6 - xt;
        // dòng phụ ghi "Yêu cầu cấp N": tên bỏ " · Cấp N"; tên dài ngắt sang dòng 2
        String ten = (t == null ? "?" : ChuDe.boCap(t.name)) + (it.plusTemplate > 0 ? " +" + it.plusTemplate : "");
        String loai = t == null ? null : BangTrangBi.tenLoai(t.type);
        GiaoDien.tenHaiDong(g, Font.blackFont, Font.arialFont, ten, (loai == null ? "" : loai + " · ") + "Yêu cầu cấp " + capCan(it, t),
                xt, y + 5, wt, y + 7 + Font.blackFont.getHeight(), wt);
        int yy = y + 4 + s + k;
        // so sánh: món đang mặc ở ô tương ứng
        ItemInInventory cu = null;
        if (!this.xemDangMac && t != null) {
            int oSoSanh = this.o;
            if (oSoSanh == O_TAT_CA) {
                for (int i = 0; i < LOAI_O.length; i++) {
                    if (hopLoai(i, t.type)) {
                        oSoSanh = i;
                        break;
                    }
                }
            }
            cu = oSoSanh >= 0 && oSoSanh < LOAI_O.length ? dangMac(oSoSanh) : null;
        }
        int nutH = Bang.co(30);
        int day = y + h - 6 - nutH - k;
        int lh = Font.arialFont.getHeight() + 3;
        Vector ids = new Vector();
        themId(ids, it);
        if (cu != null) {
            themId(ids, cu);
        }
        if (ids.size() == 0) {
            Font.arialFont.drawString(g, it.isEnoughData ? "Không có chỉ số cộng thêm." : "Đang tải thông tin...", x + 10, yy, 0);
        }
        if (cu != null && ids.size() > 0) {
            Font.arialFont.drawString(g, "So với món đang mặc:", x + 10, yy, 0);
            yy += lh;
        }
        for (int i = 0; i < ids.size() && yy + lh <= day; i++) {
            int id = ((Integer) ids.elementAt(i)).intValue();
            int moi = giaTri(it, id), cuGt = cu == null ? 0 : giaTri(cu, id);
            String tenCs = tenThuocTinh(it, cu, id);
            Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, tenCs, w / 2), x + 10, yy, 0);
            String gt = String.valueOf(moi);
            int xv = x + w - 10;
            if (cu != null && moi != cuGt) {
                int d = moi - cuGt;
                String sd = (d > 0 ? "+" : "") + d;
                int wd = Font.arialFont.getWidth(sd) + 8;
                GiaoDien.khoi(g, xv - wd, yy - 1, wd, lh - 1, 4, d > 0 ? Bang.TANG : Bang.GIAM);
                Font.borderFont.drawString(g, sd, xv - wd / 2, yy, Font.CENTER);
                xv -= wd + 6;
            }
            Font.blackFont.drawString(g, gt, xv, yy, 1);
            yy += lh;
        }
        // 0.9.8: bộ cùng bậc đang mặc x/11 + lỗ ngọc hệ (máy chủ -120 kiểu 0)
        yy = TrangBiThem.veDong(g, it.itemID, x + 10, yy, w - 20, day);
        // nút to
        int yn = y + h - 6 - nutH;
        int wn = w - 12;
        boolean coGo = this.xemDangMac;
        boolean duCap = t == null || capCan(it, t) <= mc().level;
        if (coGo) {
            Bang.nut(g, x + 6, yn, wn, nutH, "Gỡ trang bị", Bang.PHU, new IAction() {
                public void perform() {
                    DungDo.thao(it);
                    ManNhanVat.this.xem = null;
                }
            }, "go");
        } else if (cu != null) {
            int w2 = (wn - k) / 2;
            Bang.nut(g, x + 6, yn, w2, nutH, "Gỡ món cũ", Bang.PHU, new IAction() {
                public void perform() {
                    DungDo.thao(ManNhanVat.dangMac(ManNhanVat.this.oCuaMon(it)));
                }
            }, "go-cu");
            Bang.nut(g, x + 6 + w2 + k, yn, wn - w2 - k, nutH, duCap ? "Trang bị" : "Chưa đủ cấp", duCap ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
                public void perform() {
                    ManNhanVat.this.trangBi(it);
                }
            }, "trang-bi");
        } else {
            Bang.nut(g, x + 6, yn, wn, nutH, duCap ? "Trang bị" : "Chưa đủ cấp", duCap ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
                public void perform() {
                    ManNhanVat.this.trangBi(it);
                }
            }, "trang-bi");
        }
    }

    int oCuaMon(ItemInInventory it) {
        if (this.o >= 0 && this.o < LOAI_O.length) {
            return this.o;
        }
        ItemTemplate t = mau(it);
        for (int i = 0; t != null && i < LOAI_O.length; i++) {
            if (hopLoai(i, t.type)) {
                return i;
            }
        }
        return -1;
    }

    void trangBi(ItemInInventory it) {
        ItemTemplate t = mau(it);
        if (t != null && t.type >= 3 && t.type <= 7 && t.type != 3 + mc().clazz) {
            DungDo.loi("Đồ cầm tay này dành cho dòng Pokémon khác.");
            return;
        }
        DungDo.dungItem(it);
        this.xem = null;
    }

    static void themId(Vector ids, ItemInInventory it) {
        for (int i = 0; i < it.allAttribute.size(); i++) {
            Integer id = new Integer(((InfoAttributeItem) it.allAttribute.elementAt(i)).getID());
            if (!ids.contains(id)) {
                ids.addElement(id);
            }
        }
    }

    static int giaTri(ItemInInventory it, int id) {
        for (int i = 0; i < it.allAttribute.size(); i++) {
            InfoAttributeItem a = (InfoAttributeItem) it.allAttribute.elementAt(i);
            if (a.getID() == id) {
                return a.getValueAtt();
            }
        }
        return 0;
    }

    static String tenThuocTinh(ItemInInventory a, ItemInInventory b, int id) {
        ItemInInventory[] ds = {a, b};
        for (int k = 0; k < 2; k++) {
            if (ds[k] == null) {
                continue;
            }
            for (int i = 0; i < ds[k].allAttribute.size(); i++) {
                InfoAttributeItem x = (InfoAttributeItem) ds[k].allAttribute.elementAt(i);
                if (x.getID() == id) {
                    try {
                        String s = x.getName(mc().clazz);
                        return s.endsWith(": ") ? s.substring(0, s.length() - 2) : s;
                    } catch (Throwable e) {
                        return "Chỉ số " + id;
                    }
                }
            }
        }
        return "Chỉ số " + id;
    }

    // ------------------------------------------------------------------ các tab khác

    private void veChiSoDay(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int lw = Math.min(w * 40 / 100, 220);
        sanKhauNguoi(g, x, y, lw, h);
        veChiSo(g, x + lw + k, y, w - lw - k, h, true);
    }

    /** Chiêu NPC dạy cho dòng của người chơi (gói 13: GameScr.infoNewSkill[clazz]). */
    static Vector chieuHocMoi() {
        try {
            return (Vector) GameScr.infoNewSkill.elementAt(mc().clazz);
        } catch (Throwable e) {
            return new Vector();
        }
    }

    /** 0.9.8: mở trang chiêu 3 - xu (máy chủ trừ) hoặc Thẻ mở trang chiêu (cửa hàng lượng, Tiện ích). */
    static void moMenuTrang3() {
        java.util.Vector v = new java.util.Vector();
        String gia = TrangChieu.giaXu > 0 ? TienTe.gon(TrangChieu.giaXu) + " xu" : "xu";
        v.addElement(new game.model.Command("Mở bằng " + gia, new IAction() {
            public void perform() {
                TrangChieu.muaXu();
            }
        }));
        v.addElement(new game.model.Command("Thẻ mở trang chiêu (cửa hàng lượng)", new IAction() {
            public void perform() {
                DungDo.bao("Thẻ mở trang chiêu 3: Cửa hàng lượng > Tiện ích. Mở xong đổi trang bằng nút 1/3 cạnh chiêu.", DungDo.TIN);
            }
        }));
        game.render.Canvas.menu.startAt(v, 3);
    }

    /**
     * 0.9.22: tab Kỹ năng + KÉO THẢ chiêu vào ô nhanh (KeoChieu): kéo ô -> ô = đổi chỗ, kéo hình chiêu (hoặc nhấn giữ hàng)
     * -> ô = gán; chạm như cũ.
     */
    private void veKyNang(Graphics g, int x, int y, int w, int h) {
        MainChar m0 = mc();
        KeoChieu.khung(m0 == null ? 0 : m0.clazz, this.ds);
        veKyNangNoi(g, x, y, w, h);
        KeoChieu.ve(g);
    }

    private void veKyNangNoi(Graphics g, int x, int y, int w, int h) {
        final MainChar m = mc();
        int k = Bang.cach();
        final int lop = m.clazz;
        HocChieu.xinNeuChua();   // 0.9.8: bảng chiêu theo loài (máy chủ mới)
        final boolean theoLoai = HocChieu.dung(lop);
        // hàng ô nhanh (gán chiêu: một chạm) + điểm kỹ năng còn lại
        if (MainChar.quickSlot == null) {
            m.loadQuickSlot();   // chưa vào trận lần nào: dựng bộ ô (tự điền chiêu đã học) để hàng này khớp thanh ô nhanh
        }
        int sO = Bang.co(30);
        Bang.the(g, x, y, w, sO + 12);
        Font.blackFont.drawString(g, "Ô nhanh:", x + 8, y + 6 + (sO - Font.blackFont.getHeight()) / 2, 0);
        int xo = x + 12 + Font.blackFont.getWidth("Ô nhanh:");
        int xHet = xo;
        for (int i = 0; i < OChieuNhanh.SO_O; i++) {
            final int oo = i;
            int xi = xo + i * (sO + k);
            if (xi + sO > x + w - 4) {
                break;
            }
            game.model.QuickSlot qs = null;
            try {
                qs = MainChar.quickSlot == null ? null : MainChar.quickSlot[GameScr.indexTabSlot][i];
            } catch (Throwable e) {
                qs = null;
            }
            boolean an = KeoChieu.anO(i);   // 0.9.22: nội dung đang được kéo đi / bóng đang trượt về ô này
            boolean coChieu = !an && qs != null && qs.quickslotType == game.model.QuickSlot.TYPE_SKILL;
            boolean coThuoc = !an && qs != null && qs.quickslotType == game.model.QuickSlot.TYPE_POTION;
            KeoChieu.dichO(g, i, xi, y + 6, sO);
            if (qs != null && qs.quickslotType >= 0) {
                KeoChieu.nguonO(g, i, xi, y + 6, sO);   // cả lúc đang ẩn (bóng trượt về): nhấn ngay sau vẫn nhấc được
            }
            Bang.oDo(g, xi, y + 6, sO, coChieu || coThuoc, KeoChieu.sang(i), new IAction() {
                public void perform() {
                    ChonChieuNhanh.moO(oo);
                }
            }, "o-nhanh-" + i);
            if (coChieu) {
                // ô đã gán: huy hiệu hệ + hình chiêu (như thanh ô nhanh trong trận)
                KieuO.oChieuSk(g, xi + 2, y + 8, sO - 4, sO - 4, lop, qs.skillType, false);
            } else if (coThuoc) {
                veThuoc(g, qs.potionType, xi + sO / 2, y + 6 + sO / 2, sO - 4);
            } else {
                ChuDe.chuGiua(g, "+", xi + sO / 2, y + 6, sO, ChuDe.TIEU_DE, ChuDe.CHU_NHAT, 2);
            }
            // số phím ở góc dưới phải
            ChuDe.chu(g, String.valueOf(i + 1), xi + sO - 3, y + 6 + sO - ChuDe.cao(ChuDe.NHAN_NHO) - 1, ChuDe.NHAN_NHO,
                    coChieu ? 0xFFFFFF : ChuDe.CHU_PHU, 1);
            xHet = xi + sO;
        }
        // 0.9.8: chọn trang ô nhanh để gán (mỗi trang gán riêng) + mở trang 3
        {
            String tr = "Trang " + TrangChieu.nhan();
            int wt = ChuDe.rong(tr, ChuDe.NHAN) + 20;
            if (xHet + k + wt < x + w - 8) {
                Bang.nut(g, xHet + k, y + 6, wt, sO, tr, Bang.TRUNG, new IAction() {
                    public void perform() {
                        TrangChieu.tiep();
                        game.render.screen.RMS.saveQuickSlot();
                    }
                }, "o-nhanh-trang");
                xHet += k + wt;
            }
            if (TrangChieu.so < TrangChieu.TOI_DA) {
                String mo = "+ Trang 3";
                int wm = ChuDe.rong(mo, ChuDe.NHAN) + 20;
                if (xHet + k + wm < x + w - 8) {
                    Bang.nut(g, xHet + k, y + 6, wm, sO, mo, Bang.PHU, new IAction() {
                        public void perform() {
                            ManNhanVat.moMenuTrang3();
                        }
                    }, "o-nhanh-mo-trang");
                    xHet += k + wm;
                }
            }
        }
        String diem = m.skillPointLeft > 0 ? m.skillPointLeft + " điểm kỹ năng" : "Hết điểm kỹ năng";
        int wd = ChuDe.rong(diem, ChuDe.NHAN) + 18, hd0 = ChuDe.cao(ChuDe.NHAN) + 8;
        if (xHet + 12 + wd < x + w - 8) {
            int yd = y + 6 + (sO - hd0) / 2;
            if (m.skillPointLeft > 0) {
                GiaoDien.vien(g, x + w - 10 - wd, yd, wd, hd0, ChuDe.LA, null);
                ChuDe.chuGiua(g, diem, x + w - 10 - wd / 2, yd, hd0, ChuDe.NHAN, 0xFFFFFF, 2);
            } else {
                ChuDe.chuGiua(g, diem, x + w - 10, yd, hd0, ChuDe.NHAN, ChuDe.CHU_PHU, 1);
            }
        }
        int yl = y + sO + 12 + k;
        // chip: chiêu của Pokémon | học mới (NPC dạy)
        Object[] vc = {new IAction() {
            public void perform() {
                ManNhanVat.this.hocMoi = false;
                ManNhanVat.this.chieu = -1;
                ManNhanVat.this.ds.den(0);
            }
        }, new IAction() {
            public void perform() {
                ManNhanVat.this.hocMoi = true;
                ManNhanVat.this.chieu = -1;
                ManNhanVat.this.ds.den(0);
            }
        }};
        // 0.9.6 dấu dẫn đường: chip có chấm theo ChamMoi; danh sách chiêu mới đã MỞ rồi rời đi = đã xem
        if (!this.hocMoi && this.daMoHocMoi) {
            ChamMoi.daXemChieuMoi();
            this.daMoHocMoi = false;
        }
        final boolean chamMoi = ChamMoi.co(ChamMoi.CHIEU_MOI);
        Bang.chamChip = new boolean[]{ChamMoi.chipKyNang(0), chamMoi};
        yl += Bang.hangChip(g, x, yl, w, new String[]{"Chiêu của Pokémon", "Học chiêu mới (" + demHocMoi(lop) + ")"}, this.hocMoi ? 1 : 0, vc) + k;
        int hl = y + h - yl;
        if (this.hocMoi) {
            this.daMoHocMoi = true;
            final Vector moi = chieuHocMoi();
            if (moi.size() == 0) {
                Bang.trong(g, x, yl, w, hl, "Chưa có chiêu mới để học", "Gặp NPC dạy chiêu ở thành phố khi đủ cấp.");
                return;
            }
            Bang.danhSach(g, this.ds, x, yl, w, hl, moi.size(), Bang.caoHang(), new Bang.Hang() {
                public void ve(Graphics g, final int i, int x, int y, int w, int h) {
                    final game.model.skill.InfoSkillLearn sk = (game.model.skill.InfoSkillLearn) moi.elementAt(i);
                    final boolean daHoc = theoLoai ? capChieu(sk.idSkill) >= 1 : capChieu(sk.idSkill) >= 0;
                    final String tenSk = theoLoai ? HocChieu.tenChieu(lop, sk.idSkill) : sk.name;
                    // 0.9.8: loài / cấp chưa học được -> mờ + lý do (máy chủ cũng từ chối)
                    final String lyDo = theoLoai && !daHoc ? HocChieu.lyDoNpc(sk.idSkill, m.level) : null;
                    Bang.hang(g, x, y, w, h, i, false, new IAction() {
                        public void perform() {
                            if (daHoc) {
                                DungDo.bao(tenSk + ": đã học - cộng điểm ở tab Chiêu của Pokémon.");
                                return;
                            }
                            if (lyDo != null) {
                                DungDo.bao(tenSk + ": " + lyDo + ".");
                                return;
                            }
                            HopXacNhan.hoiGia("Học kỹ năng", "Học " + tenSk + "?" + (sk.decript.length() > 0 ? " " + sk.decript : ""), "Học",
                                    TienTe.XU, sk.price, new IAction() {
                                public void perform() {
                                    GameService.gI().learnSkill(lop, i);
                                }
                            });
                        }
                    }, "hoc-" + i);
                    int s = h - 6;
                    int he = HocChieu.heChieu(lop, sk.idSkill);
                    KieuO.oChieuSk(g, x + 3, y + 3, s, s, lop, sk.idSkill, daHoc || lyDo != null);
                    if (chamMoi && !daHoc && lyDo == null) {
                        ChuDe.cham(g, x + 3 + s - 2, y + 5);
                    }
                    int wg;
                    if (daHoc || lyDo != null) {
                        String nv = daHoc ? "Đã học" : (theoLoai && HocChieu.capHocLoai(sk.idSkill) > 0 ? "Cần cấp " + HocChieu.capHocLoai(sk.idSkill) : "Chưa học được");
                        int hv = Math.max(14, Font.borderFont.getHeight() + 2);
                        wg = Font.borderFont.getWidth(nv) + 14;
                        GiaoDien.vien(g, x + w - 8 - wg, y + 3, wg, hv, ChuDe.XAM, nv);
                    } else {
                        wg = TienTe.rongGia(TienTe.XU, sk.price, Font.blackFont, true);
                        TienTe.veGia(g, TienTe.XU, sk.price, x + w - 8 - wg, y + 2, Font.blackFont, true);
                    }
                    Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, tenSk, w - s - 24 - wg), x + s + 9, y + 2, 0);
                    int yp = y + 3 + Font.blackFont.getHeight();
                    int wh = KieuO.vienHe(g, x + s + 9, yp, he) + 5;
                    String phuNpc = daHoc ? "Đã học - cộng điểm ở tab Chiêu" : lyDo != null ? lyDo
                            : (theoLoai ? "Học ngay ở NPC Blaziken" : (sk.decript.length() > 0 ? sk.decript : "Chạm để học"));
                    Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, phuNpc, w - s - 14 - wh), x + s + 9 + wh, yp, 0);
                }
            });
            return;
        }
        final int so = lop >= 0 && lop < PokeChieu.TEN.length ? PokeChieu.TEN[lop].length : 0;
        if (this.chieu >= 0 && this.chieu < so) {
            veChiTietChieu(g, x, yl, w, hl, lop, this.chieu);
            return;
        }
        final int cot = w >= 420 ? 2 : 1;
        final int wc0 = w;
        // hàng có chấm (điểm kỹ năng): chiêu học được bằng 1 điểm; không có thì chiêu đã học chưa tối đa (cộng điểm)
        boolean coHoc0 = false;
        for (int i = 0; i < so && m.skillPointLeft > 0 && !theoLoai; i++) {
            coHoc0 |= capChieu(i) == 0 && m.level >= capCanChieu(lop, i, 0);   // luật cũ: học = 1 điểm
        }
        final boolean coHoc = coHoc0;
        Bang.danhSach(g, this.ds, x, yl, w, hl, (so + cot - 1) / cot, Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, int hang, int x, int y, int w, int h) {
                int kk = Bang.cach();
                int wc = (w - (cot - 1) * kk) / cot;
                for (int cc = 0; cc < cot; cc++) {
                    final int i = hang * cot + cc;
                    if (i >= so) {
                        break;
                    }
                    int xr = x + cc * (wc + kk);
                    Bang.hang(g, xr, y, wc, h, i, false, new IAction() {
                        public void perform() {
                            ManNhanVat.this.chieu = i;
                        }
                    }, "chieu-" + i);
                    int cap = capChieu(i);
                    int s = h - 6;
                    int he = HocChieu.heChieu(lop, i);
                    if (cap > 0) {
                        KeoChieu.nguonChieu(g, i, xr + 3, y + 3, s, xr, y, wc, h);   // 0.9.22: kéo hình / giữ hàng -> ô nhanh
                    }
                    KieuO.oChieuSk(g, xr + 3, y + 3, s, s, lop, i, cap <= 0 || KeoChieu.nguonDangKeo(KeoChieu.CHIEU, i));
                    // trạng thái canh phải: Cấp n | Học: 1 điểm (xanh) | Cần cấp n | Học ở NPC (giá)
                    String tt;
                    int mau;
                    if (theoLoai) {
                        // 0.9.8 học theo loài: Cấp n | Tự học cấp N | NPC · cấp N | Tiến hoá: cấp N
                        tt = HocChieu.trangThai(i, cap, m.level);
                        mau = cap > 0 ? ChuDe.CHINH : HocChieu.laNpc(lop, i) && HocChieu.capHocLoai(i) > 0 ? ChuDe.CAM : ChuDe.XAM;
                    } else if (cap > 0) {
                        tt = cap >= PokeChieu.CAP_TOI_DA ? "Tối đa" : "Cấp " + cap;
                        mau = ChuDe.CHINH;
                    } else if (cap == 0) {
                        int can = capCanChieu(lop, i, 0);
                        boolean du = m.level >= can;
                        tt = du ? "Học: 1 điểm" : "Cần cấp " + can;
                        mau = du && m.skillPointLeft > 0 ? ChuDe.LA : ChuDe.XAM;
                    } else {
                        tt = "Học ở NPC";
                        mau = ChuDe.CAM;
                    }
                    int hv = Math.max(14, Font.borderFont.getHeight() + 2);
                    int wv = Font.borderFont.getWidth(tt) + 14;
                    GiaoDien.vien(g, xr + wc - 8 - wv, y + 3, wv, hv, mau, tt);
                    if (m.skillPointLeft > 0 && (coHoc ? cap == 0 && m.level >= capCanChieu(lop, i, 0) : cap > 0 && cap < PokeChieu.CAP_TOI_DA)) {
                        ChuDe.cham(g, xr + wc - 8, y + 4);
                    }
                    int xt = xr + s + 9;
                    Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, HocChieu.tenChieu(lop, i), wc - s - 22 - wv), xt, y + 2, 0);
                    int yp = y + 3 + Font.blackFont.getHeight();
                    int wh = KieuO.vienHe(g, xt, yp, he) + 5;
                    String phu = theoLoai ? (cap > 0 ? PokeChieu.moTa(lop, i) : HocChieu.moTaTrangThai(i, cap, m.level))
                            : cap < 0 ? giaHocMoi(lop, i) : PokeChieu.moTa(lop, i);
                    Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, phu, wc - s - 20 - wh - wv), xt + wh, yp, 0);
                }
            }
        });
    }

    /** Chi tiết một chiêu: mô tả, cấp, hồi chiêu; nút to Cộng điểm / Gán ô nhanh. */
    private void veChiTietChieu(Graphics g, int x, int y, int w, int h, int lop, final int i) {
        final MainChar m = mc();
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int hd = Bang.co(26);
        Bang.nutLui(g, x + 4, y + 4, hd, new IAction() {
            public void perform() {
                ManNhanVat.this.chieu = -1;
            }
        });
        int s = Math.max(30, hd + 6);
        KieuO.oChieuSk(g, x + hd + 10, y + 4, s, s, lop, i, capChieu(i) <= 0);
        int xt = x + hd + s + 18;
        int cap = capChieu(i);
        final boolean theoLoai = HocChieu.dung(lop);
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, HocChieu.tenChieu(lop, i), x + w - 8 - xt), xt, y + 5, 0);
        int xh = xt + KieuO.vienHe(g, xt, y + 9 + Font.blackFont.getHeight(), HocChieu.heChieu(lop, i)) + 6;
        Font.arialFont.drawString(g, cap <= 0 ? "Chưa học" : "Cấp " + cap + "/" + PokeChieu.CAP_TOI_DA, xh, y + 9 + Font.blackFont.getHeight(), 0);
        int yy = y + 4 + s + k;
        yy = Bang.doan(g, Font.blackFont, PokeChieu.moTa(lop, i), x + 10, yy, w - 20, false);
        int nh = Bang.caoHang();
        int yn = y + h - 6 - nh;
        int wb = (w - 12 - k) / 2;
        int capCan = capCanChieu(m.clazz, i, Math.max(0, cap));
        // yêu cầu bước kế tiếp (máy chủ Map.doAddSkillPoint: chiêu -1 = phải học ở NPC trước; 0..9 = cộng 1 điểm khi đủ cấp)
        String yc = theoLoai && cap <= 0 ? HocChieu.moTaTrangThai(i, cap, m.level)
                : cap < 0 ? "Chưa học · " + giaHocMoi(lop, i)
                : (cap >= PokeChieu.CAP_TOI_DA ? "Đã đạt cấp tối đa" : (cap == 0 ? "Học" : "Lên cấp " + (cap + 1)) + ": 1 điểm kỹ năng · cần Pokémon cấp " + capCan);
        int lh = Font.arialFont.getHeight() + 2;
        if (yy + 2 + lh <= yn) {
            Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, yc, w - 20), x + 10, yy + 2, 0);
            yy += lh;
        }
        try {
            long hoi = game.model.skill.SkillManager.getSkillCooldown((byte) i, m.clazz, Math.max(1, cap));
            if (yy + 2 + lh <= yn) {
                Font.arialFont.drawString(g, "Hồi chiêu: " + hoi / 1000 + "," + (hoi % 1000) / 100 + " giây", x + 10, yy + 2, 0);
            }
        } catch (Throwable e) {
            // thiếu dữ liệu chiêu
        }
        boolean cong = (theoLoai ? cap >= 1 : cap >= 0) && m.skillPointLeft > 0 && m.level >= capCan && cap < PokeChieu.CAP_TOI_DA;
        Bang.nut(g, x + 6, yn, wb, nh, "Gán ô nhanh", cap > 0 ? Bang.PHU : Bang.TAT, new IAction() {
            public void perform() {
                if (capChieu(i) > 0) {
                    ChonChieuNhanh.moO(0);
                } else {
                    DungDo.bao("Học chiêu này trước rồi mới gán vào ô nhanh.");
                }
            }
        }, "gan-o");
        if (cap < 0) {
            Bang.nut(g, x + 6 + wb + k, yn, w - 12 - wb - k, nh, "Học ở NPC", Bang.CHINH, new IAction() {
                public void perform() {
                    ManNhanVat.this.hocMoi = true;
                    ManNhanVat.this.chieu = -1;
                    ManNhanVat.this.ds.den(0);
                }
            }, "cong-diem");
            return;
        }
        if (theoLoai && cap == 0) {
            // 0.9.8: chiêu tự học khi đủ cấp của loài (không tốn điểm) - không có nút học
            final String tb = HocChieu.moTaTrangThai(i, cap, m.level);
            int ch = HocChieu.capHocLoai(i);
            Bang.nut(g, x + 6 + wb + k, yn, w - 12 - wb - k, nh, ch > 0 ? "Tự học cấp " + ch : "Chưa học được", Bang.TAT, new IAction() {
                public void perform() {
                    DungDo.bao(tb + ".");
                }
            }, "cong-diem");
            return;
        }
        String chuCong = cong ? (cap == 0 ? "Học chiêu (1 điểm)" : "Cộng điểm (+1)")
                : (cap >= PokeChieu.CAP_TOI_DA ? "Đã tối đa" : (m.level < capCan ? "Cần cấp " + capCan : "Hết điểm"));
        Bang.nut(g, x + 6 + wb + k, yn, w - 12 - wb - k, nh, chuCong, cong ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
            public void perform() {
                GameService.gI().addSkillPoint(i);
            }
        }, "cong-diem");
    }

    /** Số chiêu NPC dạy mà Pokémon chưa học (nhãn chip "Học chiêu mới (n)"). */
    static int demHocMoi(int lop) {
        if (HocChieu.dung(lop)) {
            MainChar m = mc();
            return HocChieu.demNpcNgay(m == null ? 0 : m.level);   // 0.9.8: chỉ chiêu loài học được NGAY
        }
        Vector moi = chieuHocMoi();
        int n = 0;
        for (int k = 0; k < moi.size(); k++) {
            if (capChieu(((game.model.skill.InfoSkillLearn) moi.elementAt(k)).idSkill) < 0) {
                n++;
            }
        }
        return n;
    }

    /** Cấp Pokémon cần để cộng điểm khi chiêu đang ở cấp cap (bảng máy chủ gửi lúc vào game). */
    static int capCanChieu(int lop, int i, int cap) {
        try {
            return game.model.skill.SkillManager.getLvAddSkill(lop, i, Math.max(0, cap));
        } catch (Throwable e) {
            return 0;
        }
    }

    /** Chiêu phải mua ở NPC (gói 13: GameScr.infoNewSkill): "Học ở NPC: 60.000 xu" / không có trong danh sách. */
    static String giaHocMoi(int lop, int i) {
        Vector moi = chieuHocMoi();
        for (int k = 0; k < moi.size(); k++) {
            game.model.skill.InfoSkillLearn sk = (game.model.skill.InfoSkillLearn) moi.elementAt(k);
            if (sk.idSkill == i) {
                return "Blaziken dạy, " + TienTe.chu(TienTe.XU, sk.price);   // nút bên phải đã ghi "Học ở NPC"
            }
        }
        return "Blaziken dạy";
    }


    static int capChieu(int i) {
        try {
            return Char.skillLevelLearnt != null && i < Char.skillLevelLearnt.length ? Char.skillLevelLearnt[i] : -1;
        } catch (Throwable e) {
            return -1;
        }
    }

    private void veThu(Graphics g, int x, int y, int w, int h) {
        MainChar m = mc();
        int k = Bang.cach();
        // 0.9.16 điện thoại: sân khấu hẹp 36 %, danh sách gọn nhiều cột (thấy >= 5 thú)
        boolean gon = gonThu(h);
        gonVuaVe = gon;
        int lw = gon ? w * 36 / 100 : Math.min(w * 45 / 100, 260);
        nenSanKhau(g, x, y, lw, h);
        // người chơi (cưỡi thú nếu đang cưỡi - veNguoiChoi) + Pokémon đi theo (-94 pokePetDex) bên cạnh
        boolean coPet = m.pokePetDex > 0 && PokeBody.coSprite(m.pokePetDex, m.pokePetShiny);
        int rD = Math.min(lw, h) * 9 / 20, cyD = y + h / 2 + h / 20;
        float z = Math.max(1.4f, Math.min(4f, Math.min(lw / (coPet ? 90f : 50f), rD * 2 / 40f)));
        int yChan = Math.min(y + h - (int) (4 * z), cyD + rD * 6 / 10);
        int xNv = coPet ? x + lw * 40 / 100 : x + lw / 2;
        if (this.thuChon != null && QuanLyThu.conTrongDs(this.thuChon)) {
            // thú đang chọn ở danh sách: sân khấu xem thú (hình + tên + cấp + xu thả), như Trang bị › Thú cưỡi
            banRongBe = lw * 45 / 100;
            veXemThu(g, m, x, y, lw, x + lw / 2, yChan, z);
            banRongBe = 1000;
            coPet = false;
        } else {
            banRongBe = coPet ? lw * 30 / 100 : 1000;
            veNguoiChoi(g, m, xNv, yChan, z);
            banRongBe = 1000;
        }
        if (coPet) {
            vePoke(g, m.pokePetDex, m.pokePetShiny, x + lw * 80 / 100, yChan - (int) (2 * z), z * 0.8f, NgoaiHinhVe.huongThe(Canvas.gameTick));
        }
        if (this.thuChon == null || !QuanLyThu.conTrongDs(this.thuChon)) {
            boolean coDiTheo = coPet || petDiTheo() != null;
            String nhan = dangCuoi(m) ? (coDiTheo ? "Đang cưỡi · có pet đi theo" : "Đang cưỡi") : (coDiTheo ? "Pet đang đi theo" : "Chưa cưỡi thú");
            ChuDe.chu(g, nhan, x + lw / 2, y + 6, ChuDe.NHAN_NHO, ChuDe.CHU_PHU, 2);
        }
        int xr = x + lw + k, wr = w - lw - k;
        Bang.the(g, xr, y, wr, h);
        if (gon) {
            veDanhSachThu(g, xr + 4, y + 4, wr - 8, h - 8, null, true);
            return;
        }
        Font.blackFont.drawString(g, "Thú cưỡi / đi theo", xr + 8, y + 6, 0);
        int yl = y + 10 + Font.blackFont.getHeight();
        veDanhSachThu(g, xr + 4, yl, wr - 8, y + h - 4 - yl);
    }

    /** Biểu tượng hàng Ngoại hình: Trang phục + NgoaiHinhMenu.TEN_O (mũ, khăn, vòng cổ, đồ cầm tay, ruy băng, đá, kính). */
    static final int[] HINH_NGOAI_HINH = {BieuTuong.AO, BieuTuong.MU, BieuTuong.KHAN, BieuTuong.VONG_CO, BieuTuong.HOP_DO, BieuTuong.QUA,
        BieuTuong.DA, BieuTuong.NHAN};

    private void veNgoaiHinh(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int lw = Math.min(w * 42 / 100, 240);
        // 0.9.8: sân khấu mặc THỬ món đang chọn ở tủ đồ (bản sao ngoại hình, không gửi máy chủ)
        MainChar m = mc();
        ManTuDo.batThu(m);
        try {
            sanKhauNguoi(g, x, y, lw, h);
        } finally {
            ManTuDo.tatThu(m);
        }
        ManTuDo.veNhanThu(g, x, y, lw);
        int xr = x + lw + k, wr = w - lw - k;
        Bang.the(g, xr, y, wr, h);
        ManTuDo.ve(g, xr, y, wr, h, this);
    }

    /** Hàng Hiện / ẩn từng phụ kiện + trang phục (gửi -103) - tab con "Hiện / ẩn" của tủ đồ. */
    void veAnHien(Graphics g, int xr, int y, int wr, int h) {
        NgoaiHinhMenu.nap();
        final int an = NgoaiHinhMenu.anHienTai();
        int n = NgoaiHinhMenu.TEN_O.length + 1;
        // 8 hàng không vừa màn điện thoại: danh sách cuộn (trước chỉ vẽ tới hết chỗ -> mất Đồ cầm tay, Ruy băng, Đá, Kính)
        Bang.danhSach(g, this.ds, xr + 4, y + 4, wr - 8, h - 8, n, Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int rh) {
                final int bit = i == 0 ? NgoaiHinhCua.AN_TRANG_PHUC : 1 << (i - 1);
                String ten = i == 0 ? "Trang phục" : NgoaiHinhMenu.TEN_O[i - 1];
                boolean hien = (an & bit) == 0;
                Bang.hang(g, x, y, w, rh, i, false, new IAction() {
                    public void perform() {
                        NgoaiHinhMenu.gui(an ^ bit);
                    }
                }, "ngoai-hinh-" + i);
                int s = oDau(g, x, y, rh);
                int ic = s * 3 / 4;
                BieuTuong.ve(g, HINH_NGOAI_HINH[i], x + 3 + (s - ic) / 2, y + 3 + (s - ic) / 2, ic, hien ? ChuDe.CHU_PHU : ChuDe.CHU_NHAT, ChuDe.NEN_PHU);
                Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, ten, w - 80 - s), x + s + 9, y + (rh - Font.blackFont.getHeight()) / 2, 0);
                int wv = 54;
                GiaoDien.vien(g, x + w - 6 - wv, y + (rh - 16) / 2, wv, 16, hien ? 0x48C058 : 0x8A94A6, hien ? "Hiện" : "Ẩn");
            }
        });
    }

    /** String.lastIndexOf(String) - CLDC 1.1 chỉ có lastIndexOf(char) (0.9.7.3). */
    static int cuoi(String t, String x) {
        int k = -1, i = t.indexOf(x);
        while (i >= 0) {
            k = i;
            i = t.indexOf(x, i + 1);
        }
        return k;
    }
}
