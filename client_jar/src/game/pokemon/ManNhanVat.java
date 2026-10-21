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
        m.tab = t;
        m.o = -1;
        m.xem = null;
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
        }
    }

    void xemMon(ItemInInventory it, boolean dangMac) {
        this.xem = it;
        this.xemDangMac = dangMac;
        if (it != null && !it.isEnoughData && it.allAttribute.size() == 0) {
            it.isEnoughData = true;
            try {
                GameService.gI().requestItemInfo(it.itemID, mc().ID);
            } catch (Throwable e) {
                // chưa nối máy chủ
            }
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
        if (Canvas.isKeyPressed(4)) {
            chonTab((this.tab + TABS.length - 1) % TABS.length);
        } else if (Canvas.isKeyPressed(6)) {
            chonTab((this.tab + 1) % TABS.length);
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
        int[] c = Bang.khung(g, m.name == null ? "Hồ sơ" : m.name, TABS, this.tab, vt, new IAction() {
            public void perform() {
                ManNhanVat.this.dong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
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
        if (ch.pokeThuDex > 0 && PokeBody.coSprite(ch.pokeThuDex, ch.pokeThuShiny)) {
            int caoThu = PokeBody.caoTrenChan(ch.pokeThuDex, ch.pokeThuShiny, 100);
            int tong = caoThu * 55 / 100 + Math.max(24, PokeBody.caoTrenChan(ch.pokeDex, ch.pokeShiny, 100));
            if (tong > 34) {
                z = Math.max(z / 2, z * 34 / tong);
            }
            vePoke(g, ch.pokeThuDex, ch.pokeThuShiny, cx, yChan, z, huong);
            yChan -= (int) (caoThu * 55 / 100 * z);
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
        if (PhongVe.co()) {
            int tx = g.getTranslateX(), ty = g.getTranslateY();
            PhongVe.bo.bat(g, z);
            PokeBody.paint(g, dex, shiny, (int) ((cx + tx) / z), (int) ((yChan + ty) / z), huong, PokeDuLieu.DUNG, Canvas.gameTick, 100);
            PhongVe.bo.tat(g);
            g.translate(tx, ty);
        } else {
            PokeBody.paint(g, dex, shiny, cx, yChan, huong, PokeDuLieu.DUNG, Canvas.gameTick, (int) (100 * z));
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
        int mau = mauHePoke(mc());
        int r = Math.min(w, h) * 9 / 20;
        int cx = x + w / 2, cy = y + h / 2 + h / 20;
        GiaoDien.khoiDoc(g, cx - r, cy - r, 2 * r, 2 * r, r, GiaoDien.pha(mau, 0xFFFFFF, 3, 5), GiaoDien.pha(mau, 0xFFFFFF, 6, 8));
        int r2 = r * 7 / 10;
        GiaoDien.khoiDoc(g, cx - r2, cy - r2, 2 * r2, 2 * r2, r2, GiaoDien.pha(mau, 0xFFFFFF, 5, 8), GiaoDien.pha(mau, 0xFFFFFF, 4, 7));
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
        int mau = mauHePoke(mc());
        int rw = Math.min((int) (28 * z), banRongBe), rh = Math.max(6, rw * 3 / 10);
        g.setColor(GiaoDien.pha(mau, 0xFFFFFF, 2, 4));
        g.fillArc(cx - rw, yChan - rh / 2 + 1, 2 * rw, rh, 0, 360);
        g.setColor(0xFFFFFF);
        g.fillArc(cx - rw + 2, yChan - rh / 2, 2 * rw - 4, rh - 3, 0, 360);
        g.setColor(GiaoDien.pha(mau, 0xFFFFFF, 4, 6));
        int bw = (int) (13 * z), bh = Math.max(3, (int) (3.5f * z));
        g.fillArc(cx - bw, yChan - bh / 2, 2 * bw, bh, 0, 360);
    }

    /**
     * Trang bị kiểu chi tiết Pokémon GO: "Sức mạnh" lớn trên, Pokémon to trên bệ tròn màu hệ, 12 ô trang bị TRÒN quanh
     * (4 trái, 4 phải xếp cung, 4 dưới); bên phải: thẻ chỉ số / hành trang lọc / chi tiết món.
     */
    private void veTrangBi(Graphics g, int x, int y, int w, int h) {
        MainChar m = mc();
        int s = canhO(), k = Bang.cach();
        int rs = (s + 1) / 2;
        int lw = rongKhoiNv(w);
        // Sức mạnh (hoặc tên ô đang chọn) trên cùng
        int hSm = ChuDe.cao(ChuDe.PHU) + ChuDe.cao(ChuDe.SO_TO) - 2;
        int cxS = x + lw / 2;
        if (this.o >= 0 && this.o < TEN_O.length) {
            String ten = TEN_O[this.o];
            int wt = ChuDe.rong(ten, ChuDe.NHAN) + 18, ht = ChuDe.cao(ChuDe.NHAN) + 6;
            GiaoDien.khoi(g, cxS - wt / 2, y + (hSm - ht) / 2, wt, ht, ht / 2, ChuDe.CHINH);
            ChuDe.chuGiua(g, ten, cxS, y + (hSm - ht) / 2, ht, ChuDe.NHAN, 0xFFFFFF, 2);
        } else {
            ChuDe.chu(g, "SỨC MẠNH", cxS, y, ChuDe.PHU, ChuDe.CHU_PHU, 2);
            ChuDe.chu(g, String.valueOf(sucManh(m)), cxS, y + ChuDe.cao(ChuDe.PHU) - 3, ChuDe.SO_TO, ChuDe.CHU, 2);
        }
        int yTren = y + hSm + 2;
        int d = 2 * rs, b = d + k + 1;   // đường kính ô tròn, bước (khe >= cach)
        boolean gon = 4 * b - k + k + d > y + h - yTren;   // không đủ cao cho 4 hàng + hàng dưới: 2 cột x 3 hàng mỗi bên
        int yDuoi = gon ? y + h : y + h - d;
        // sân khấu: đĩa màu hệ giữa vùng ô
        int coCot = gon ? 2 * b : b;
        nenSanKhau(g, x + coCot - 4, yTren, lw - 2 * coCot + 8, yDuoi - k - yTren);
        if (gon) {
            int caoCot = 3 * b - k - 1;
            int y0 = yTren + Math.max(0, (yDuoi - yTren - caoCot) / 2);
            for (int i = 0; i < 6; i++) {
                int c = i / 3, r = i % 3;
                veO(g, i, x + rs + 1 + c * b, y0 + r * b + rs, rs);
                veO(g, 6 + i, x + lw - rs - 1 - c * b, y0 + r * b + rs, rs);
            }
        } else {
            int caoCot = 4 * b - k - 1;
            int y0 = yTren + Math.max(0, (yDuoi - k - yTren - caoCot) / 2);
            for (int i = 0; i < 4; i++) {
                int lech = i == 1 || i == 2 ? rs / 3 : 0;   // cung nhẹ ôm lấy Pokémon
                veO(g, i, x + rs + 2 + lech, y0 + i * b + rs, rs);
                veO(g, 4 + i, x + lw - rs - 2 - lech, y0 + i * b + rs, rs);
            }
            int wDuoi = 4 * d + 3 * (k + 5);
            int xd = x + (lw - wDuoi) / 2;
            for (int i = 0; i < 4; i++) {
                veO(g, 8 + i, xd + i * (d + k + 5) + rs, yDuoi + rs, rs);
            }
        }
        int s0 = s;
        s = coCot;
        // Pokémon to ở giữa
        int giuaL = x + s + 4, giuaR = x + lw - s - 4;
        int cao = yDuoi - k - yTren;
        // đứng giữa đĩa màu hệ (cùng phép tính nenSanKhau), chân hơi dưới tâm
        int wD = lw - 2 * s + 8, rD = Math.min(wD, cao) * 9 / 20, cyD = yTren + cao / 2 + cao / 20;
        float z = Math.max(1.5f, Math.min(4.2f, Math.min((giuaR - giuaL) / 40f, rD * 2 / 38f)));
        int cx = (giuaL + giuaR) / 2;
        int yChan = Math.min(yDuoi - k - (int) (4 * z), cyD + rD * 6 / 10);
        banRongBe = (giuaR - giuaL) / 2 - 2;
        veNguoiChoi(g, m, cx, yChan, z);
        banRongBe = 1000;
        if (nhanO >= 0 && nhanO < TEN_O.length) {
            String ten = TEN_O[nhanO];
            int wt = ChuDe.rong(ten, ChuDe.NHAN_NHO) + 12, ht = ChuDe.cao(ChuDe.NHAN_NHO) + 4;
            int xl = Math.max(x + 2, Math.min(x + lw - wt - 2, nhanX - wt / 2));
            GiaoDien.khoi(g, xl, nhanY - ht - 2, wt, ht, ht / 2, ChuDe.CHU);
            ChuDe.chuGiua(g, ten, xl + wt / 2, nhanY - ht - 2, ht, ChuDe.NHAN_NHO, 0xFFFFFF, 2);
            nhanO = -1;
        }
        // bên phải: chỉ số / hành trang lọc / chi tiết
        int xr = x + lw + k, wr = w - lw - k;
        if (this.xem != null) {
            veChiTiet(g, xr, y, wr, h);
        } else if (this.o >= 0) {
            veHanhTrang(g, xr, y, wr, h);
        } else {
            veChiSo(g, xr, y, wr, h, true);
        }
    }

    /** Ô trang bị TRÒN tâm (cx, cy) bán kính r. */
    private void veO(Graphics g, final int i, int cx, int cy, int r) {
        int s = 2 * r, x = cx - r, y = cy - r;
        ItemInInventory it = i == O_THU ? null : dangMac(i);
        boolean co = it != null || (i == O_THU && dangCuoi(mc()));
        Bang.oTron(g, cx, cy, r, co, this.o == i, new IAction() {
            public void perform() {
                ManNhanVat.this.chonO(i);
            }
        }, "o-" + TEN_O[i]);
        float z = s >= 30 ? s / 22f : 1f;
        if (!co) {
            // ô trống: bóng mờ kiểu ô (mũ, vòng cổ, găng...) thay dấu "+"
            int ic = s * 11 / 20;
            BieuTuong.ve(g, HINH_O[i], x + (s - ic) / 2, y + (s - ic) / 2, ic, 0xB3BFC9, ChuDe.NEN_PHU);
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
                // độ hiếm: vành màu quanh đĩa
                g.setColor(hiem);
                g.drawArc(x, y, s - 1, s - 1, 0, 360);
                g.drawArc(x + 1, y + 1, s - 3, s - 3, 0, 360);
            }
            if (t != null) {
                Bang.icon(g, t.idIcon, x + s / 2, y + s / 2, z);
            }
            if (it.plusTemplate > 0) {
                KieuO.capCuong(g, x + r / 3, y, it.plusTemplate);
            }
        } else if (co) {
            MainChar m = mc();
            veHinhThu(g, m.pokeThuDex, m.pokeThuShiny, (short) (m.idPaintIconAnimal + 7500), x + s / 2, y + s / 2, s - 4);
        }
    }

    // ------------------------------------------------------------------ thú cưỡi / pet đi theo

    /**
     * Đang cưỡi thú: -93 POKE_CUOI (thú Pokémon) hoặc thú cưỡi cũ (gói 3: useHorse / idhorse), kèm idAnimal của gói
     * mặc đồ 15. idAnimal một mình không đủ: xuống thú máy chủ gửi lại gói 3 + -93 nhưng không gửi gói 15.
     */
    static boolean dangCuoi(Char m) {
        return m.idAnimal > -1 && (m.pokeThuDex > 0 || m.idhorse > -1 || m.useHorse > -1);
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
        k = Math.max(t.lastIndexOf(" cấp "), Math.max(t.lastIndexOf(" Cấp "), t.lastIndexOf(" lv ")));
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

    /** Hàng thú: tên đậm, "#id" nhỏ, viên "Cấp N" canh phải, dòng phụ. */
    static void hangThu(Graphics g, int x, int y, int w, int h, int s, String ten, String cap, String id, String phu, boolean dangDung) {
        int xt = x + s + 9;
        int hv = Math.max(14, Font.borderFont.getHeight() + 2);
        int wv = 0;
        if (cap != null && cap.length() > 0) {
            String c = "Cấp " + cap;
            wv = Font.borderFont.getWidth(c) + 14;
            GiaoDien.vien(g, x + w - 8 - wv, y + 3, wv, hv, dangDung ? ChuDe.LA : ChuDe.CHINH, c);
            wv += 6;
        }
        String so = id != null && id.length() > 0 ? "#" + id : null;
        int wso = so == null ? 0 : ChuDe.rong(so, ChuDe.NHAN_NHO) + 6;
        String t = GiaoDien.catChu(Font.blackFont, ten, Math.max(20, w - s - 16 - wv - wso));
        Font.blackFont.drawString(g, t, xt, y + 2, 0);
        if (so != null) {
            ChuDe.chu(g, so, xt + Font.blackFont.getWidth(t) + 5, y + 4, ChuDe.NHAN_NHO, ChuDe.CHU_NHAT, 0);
        }
        if (phu != null) {
            Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, phu, w - s - 14), xt, y + 3 + Font.blackFont.getHeight(), 0);
        }
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
        GiaoDien.khoi(g, xx + ww - wlv, ytd, wlv, hlv, hlv / 2, ChuDe.CHINH_NHAT);
        ChuDe.chuGiua(g, lv, xx + ww - wlv / 2, ytd, hlv, ChuDe.SO, ChuDe.CHINH_TOI, 2);
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
            String chu = coDh ? m.danhHieuTen : "Danh hiệu";
            int wv = ManDanhHieu.vien(g, xb + 2, yy, chu + " ›", coDh ? m.danhHieuHiem : 0, null, xx + ww - xb - 4);
            VungCham.them(g, xb, yy - 4, Math.max(wv + 4, VungCham.MIN), Math.max(ManDanhHieu.hVien() + 8, VungCham.MIN), new IAction() {
                public void perform() {
                    ManDanhHieu.moTuHoSo();
                }
            }, "danh-hieu");
            if (ChamMoi.co(ChamMoi.DANH_HIEU)) {
                ChuDe.cham(g, xb + 2 + wv - 2, yy + 1);
            }
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

    static final String[] LOC = {"Tất cả", "Thuốc", "Trang bị", "Nguyên liệu", "Nhiệm vụ"};
    static final int LOC_TAT_CA = 0, LOC_THUOC = 1, LOC_DO = 2, LOC_NGUYEN_LIEU = 3, LOC_NHIEM_VU = 4;
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
        if (hiem >= 0) {
            KieuO.khungHiem(g, x + 3, y + 3, s, hiem);
        }
        if (t != null) {
            Bang.icon(g, t.idIcon, x + 3 + s / 2, y + 3 + s / 2, s >= 26 ? s / 22f : 1f);
        }
        String ten = t == null ? "?" : t.name;
        if (it.plusTemplate > 0) {
            ten = ten + " +" + it.plusTemplate;
        }
        String loai = t == null ? null : BangTrangBi.tenLoai(t.type);
        chuHang(g, x, y, w, h, s, ten, "Cấp " + capCan(it, t), (laMac ? "Đang mặc" + (loai != null ? " · " : "") : "") + (loai == null ? "" : loai));
    }

    /**
     * Danh sách thú: hàng đầu = thú đang cưỡi (máy chủ không gửi con đang cưỡi trong gói túi thú - place 1; tên lấy từ
     * infoAnimal của gói 15) -> chạm = xuống; sau đó thú cưỡi + pet đi theo trong túi (gói 16 loại 2).
     */
    private void veDanhSachThu(Graphics g, int x, int y, int w, int h) {
        final MainChar m = mc();
        final int nCuoi = dangCuoi(m) ? 1 : 0;
        int n = Char.animal.size() + nCuoi;
        if (n == 0) {
            Bang.trong(g, x, y, w, h, "Chưa có thú cưỡi", GOI_Y[O_THU]);
            return;
        }
        Bang.danhSach(g, this.ds, x, y, w, h, n, Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                int s;
                if (i < nCuoi) {
                    final String[] t = tachTen(m.infoAnimal);
                    final String ten = t[0].length() > 0 ? t[0] : "Thú cưỡi";
                    Bang.hang(g, x, y, w, h, i, true, new IAction() {
                        public void perform() {
                            xuongThu(ten);
                        }
                    }, "thu-dang-cuoi");
                    s = oDau(g, x, y, h);
                    veHinhThu(g, m.pokeThuDex, m.pokeThuShiny, (short) (m.idPaintIconAnimal + 7500), x + 3 + s / 2, y + 3 + s / 2, s);
                    hangThu(g, x, y, w, h, s, ten, t[1], null, "Đang cưỡi · chạm để xuống", true);
                    return;
                }
                final AnimalInfo a = (AnimalInfo) Char.animal.elementAt(i - nCuoi);
                final String[] t = tachTen(a.name);
                final String ten = t[0].length() > 0 ? t[0] : (a.typeAnimal == 0 ? "Thú cưỡi" : "Pokémon đi theo");
                Bang.hang(g, x, y, w, h, i, false, new IAction() {
                    public void perform() {
                        chonThu(a, ten);
                    }
                }, "thu-" + (i - nCuoi));
                s = oDau(g, x, y, h);
                veHinhThu(g, dexThu(a), shinyThu(a), (short) (a.idPaint + 7500), x + 3 + s / 2, y + 3 + s / 2, s);
                String phu;
                if (a.typeAnimal == 0) {
                    phu = nCuoi > 0 ? "Thú cưỡi · chạm để đổi" : "Thú cưỡi · chạm để cưỡi";
                } else {
                    phu = "Đi theo" + (t[3].length() > 0 ? " · hạn " + t[3] : "") + " · chạm để gọi";
                }
                hangThu(g, x, y, w, h, s, ten, a.level > 0 ? String.valueOf(a.level) : t[1], t[2], phu, false);
            }
        });
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
        if (hiem >= 0) {
            KieuO.khungHiem(g, xi, y + 4, s, hiem);
        }
        if (t != null) {
            Bang.icon(g, t.idIcon, xi + s / 2, y + 4 + s / 2, s / 22f);
        }
        int xt = xi + s + 6, wt = x + w - 6 - xt;
        String ten = (t == null ? "?" : t.name) + (it.plusTemplate > 0 ? " +" + it.plusTemplate : "");
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, ten, wt), xt, y + 5, 0);
        String loai = t == null ? null : BangTrangBi.tenLoai(t.type);
        Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, (loai == null ? "" : loai + " · ") + "Yêu cầu cấp " + capCan(it, t), wt), xt, y + 7 + Font.blackFont.getHeight(), 0);
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

    private void veKyNang(Graphics g, int x, int y, int w, int h) {
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
            boolean coChieu = qs != null && qs.quickslotType == game.model.QuickSlot.TYPE_SKILL;
            boolean coThuoc = qs != null && qs.quickslotType == game.model.QuickSlot.TYPE_POTION;
            Bang.oDo(g, xi, y + 6, sO, coChieu || coThuoc, false, new IAction() {
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
                    KieuO.oChieuSk(g, xr + 3, y + 3, s, s, lop, i, cap <= 0);
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
                return "học ở NPC Blaziken: " + TienTe.chu(TienTe.XU, sk.price);
            }
        }
        return "học ở NPC Blaziken";
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
        int lw = Math.min(w * 45 / 100, 260);
        nenSanKhau(g, x, y, lw, h);
        // người chơi (cưỡi thú nếu đang cưỡi - veNguoiChoi) + Pokémon đi theo (-94 pokePetDex) bên cạnh
        boolean coPet = m.pokePetDex > 0 && PokeBody.coSprite(m.pokePetDex, m.pokePetShiny);
        int rD = Math.min(lw, h) * 9 / 20, cyD = y + h / 2 + h / 20;
        float z = Math.max(1.4f, Math.min(4f, Math.min(lw / (coPet ? 90f : 50f), rD * 2 / 40f)));
        int yChan = Math.min(y + h - (int) (4 * z), cyD + rD * 6 / 10);
        int xNv = coPet ? x + lw * 40 / 100 : x + lw / 2;
        banRongBe = coPet ? lw * 30 / 100 : 1000;
        veNguoiChoi(g, m, xNv, yChan, z);
        banRongBe = 1000;
        if (coPet) {
            vePoke(g, m.pokePetDex, m.pokePetShiny, x + lw * 80 / 100, yChan - (int) (2 * z), z * 0.8f, NgoaiHinhVe.huongThe(Canvas.gameTick));
        }
        String nhan = dangCuoi(m) ? (coPet ? "Đang cưỡi · có pet đi theo" : "Đang cưỡi") : (coPet ? "Pet đang đi theo" : "Chưa cưỡi thú");
        ChuDe.chu(g, nhan, x + lw / 2, y + 6, ChuDe.NHAN_NHO, ChuDe.CHU_PHU, 2);
        int xr = x + lw + k, wr = w - lw - k;
        Bang.the(g, xr, y, wr, h);
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
        sanKhauNguoi(g, x, y, lw, h);
        int xr = x + lw + k, wr = w - lw - k;
        Bang.the(g, xr, y, wr, h);
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
}
