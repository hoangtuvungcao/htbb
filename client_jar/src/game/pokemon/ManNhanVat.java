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

    public void dong() {
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
            Canvas.gameScr.paint(g);
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

    /** Vẽ Pokémon của người chơi to (phóng z) với chân ở (cx, yChan). */
    static void veNguoiChoi(Graphics g, Char ch, int cx, int yChan, float z) {
        int huong = NgoaiHinhVe.huongThe(Canvas.gameTick);
        g.setColor(0x9FC4FF);
        int rw = (int) (26 * z), rh = (int) (7 * z);
        g.fillArc(cx - rw, yChan - rh / 2, 2 * rw, rh, 0, 360);
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

    private void veTrangBi(Graphics g, int x, int y, int w, int h) {
        MainChar m = mc();
        int s = canhO(), k = Bang.cach();
        int lw = rongKhoiNv(w);
        // nền khối nhân vật
        GiaoDien.khoiDoc(g, x, y, lw, h, 8, 0x2C4A86, 0x16285A);
        int nHang = 4;
        int caoCot = nHang * s + (nHang - 1) * k;
        int yDuoi = y + h - s - k;
        int y0 = y + k + Math.max(0, (yDuoi - k - (y + k) - caoCot) / 2);
        int xT = x + k, xP = x + lw - k - s;
        for (int i = 0; i < 4; i++) {
            veO(g, i, xT, y0 + i * (s + k), s);
            veO(g, 4 + i, xP, y0 + i * (s + k), s);
        }
        int wDuoi = 4 * s + 3 * k;
        int xd = x + (lw - wDuoi) / 2;
        for (int i = 0; i < 4; i++) {
            veO(g, 8 + i, xd + i * (s + k), yDuoi, s);
        }
        // Pokémon to ở giữa
        int giuaL = xT + s + k, giuaR = xP - k;
        int cao = yDuoi - k - (y + k);
        float z = Math.max(1.5f, Math.min(3.2f, Math.min((giuaR - giuaL) / 44f, cao / 40f)));
        int cx = (giuaL + giuaR) / 2;
        int yChan = yDuoi - k - (int) (6 * z);
        veNguoiChoi(g, m, cx, yChan, z);
        // tên ô đang chọn trên đầu Pokémon
        if (this.o >= 0 && this.o < TEN_O.length) {
            String ten = TEN_O[this.o];
            int wt = Font.borderFont.getWidth(ten) + 14;
            GiaoDien.vien(g, cx - wt / 2, y + k, wt, Font.borderFont.getHeight() + 4, GiaoDien.DO, ten);
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

    private void veO(Graphics g, final int i, int x, int y, int s) {
        ItemInInventory it = i == O_THU ? null : dangMac(i);
        boolean co = it != null || (i == O_THU && mc().idAnimal > -1);
        Bang.oDo(g, x, y, s, co, this.o == i, new IAction() {
            public void perform() {
                ManNhanVat.this.chonO(i);
            }
        }, "o-" + TEN_O[i]);
        float z = s >= 30 ? s / 22f : 1f;
        if (it != null) {
            ItemTemplate t = mau(it);
            int hiem = KieuO.mauHiem(it.colorName);
            if (hiem >= 0) {
                KieuO.khungHiem(g, x + 2, y + 2, s - 4, hiem);
            }
            if (t != null) {
                Bang.icon(g, t.idIcon, x + s / 2, y + s / 2, z);
            }
            if (it.plusTemplate > 0) {
                KieuO.capCuong(g, x + 2, y + 2, it.plusTemplate);
            }
        } else if (i == O_THU && mc().idAnimal > -1) {
            Bang.icon(g, (short) (mc().idPaintIconAnimal + 7500), x + s / 2, y + s / 2, z);
        }
    }

    /** Khối chỉ số (tên, cấp, hệ, HP/PP/EXP, chỉ số chính). gon = kèm nút Hành trang / Tiềm năng. */
    private void veChiSo(Graphics g, int x, int y, int w, int h, boolean gon) {
        MainChar m = mc();
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int xx = x + 8, yy = y + 6, ww = w - 16;
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, m.name + "  Lv." + m.level, ww), xx, yy, 0);
        yy += Font.blackFont.getHeight() + 3;
        int he1 = HePoke.he1(m.pokeDex), he2 = HePoke.he2(m.pokeDex);
        int xb = xx;
        if (he1 >= 0) {
            xb += KieuO.vienHe(g, xb, yy, he1) + 4;
        }
        if (he2 >= 0 && he2 != he1) {
            xb += KieuO.vienHe(g, xb, yy, he2) + 4;
        }
        String loai = LoaiPokemonClient.ten(m.pokeDex);
        if (loai != null && xb + Font.arialFont.getWidth(loai) < xx + ww) {
            Font.arialFont.drawString(g, loai, xb, yy + 1, 0);
        }
        yy += 17;
        int hb = Math.max(11, Font.borderFont.getHeight() + 2);
        Bang.thanh(g, xx, yy, ww, hb, m.hp, m.maxhp, 0, "HP " + m.hp + "/" + m.maxhp);
        yy += hb + 3;
        Bang.thanh(g, xx, yy, ww, hb, m.mp, m.maxmp, 1, "PP " + m.mp + "/" + m.maxmp);
        yy += hb + 3;
        Bang.thanh(g, xx, yy, ww, Math.max(6, hb - 4), m.lvpercent, 1000, 2, null);
        yy += Math.max(6, hb - 4) + 5;
        String[][] cs = {
            {"Tấn công", String.valueOf(m.getAttack())},
            {"Phòng thủ", String.valueOf(m.defend)},
            {"P.thủ đặc biệt", String.valueOf(m.defend_magic)},
            {"Chính xác", String.valueOf(m.accurate)},
            {"Né tránh", String.valueOf(m.dodge)},
            {"Chí mạng", String.valueOf(m.critical)},
            {"Bạo kích", m.baokich / 10 + "." + m.baokich % 10 + "%"}
        };
        int nutH = gon ? Bang.co(26) : 0;
        int day = y + h - 6 - (gon ? nutH + k : 0);
        int lh = Font.arialFont.getHeight() + 2;
        int cot = ww >= 200 ? 2 : 1;
        int wc = (ww - (cot - 1) * 8) / cot;
        int n = 0;
        for (int i = 0; i < cs.length; i++) {
            int cc = n % cot, hh = n / cot;
            int yc = yy + hh * lh;
            if (yc + lh > day) {
                break;
            }
            hangChiSo(g, xx + cc * (wc + 8), yc, wc, cs[i][0], cs[i][1]);
            n++;
        }
        // chỉ số máy chủ gửi thêm (kháng hệ, hút máu, tốc độ...) nếu còn chỗ
        String[] sv = WindowInfoScr.infoMainCharServer;
        if (sv != null) {
            for (int i = 0; i < sv.length; i++) {
                int cc = n % cot, hh = n / cot;
                int yc = yy + hh * lh;
                if (yc + lh > day) {
                    break;
                }
                String s = sv[i];
                int hai = s.indexOf(':');
                hangChiSo(g, xx + cc * (wc + 8), yc, wc, hai > 0 ? s.substring(0, hai) : s, hai > 0 ? s.substring(hai + 1).trim() : "");
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
            Bang.nut(g, xx + wn + k, y + h - 6 - nutH, wn, nutH, "Tiềm năng", Bang.PHU, new IAction() {
                public void perform() {
                    HanhDong.moBang(2);
                }
            }, "tiem-nang");
        }
    }

    static void hangChiSo(Graphics g, int x, int y, int w, String ten, String gt) {
        Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, ten, w - Font.blackFont.getWidth(gt) - 6), x, y, 0);
        Font.blackFont.drawString(g, gt, x + w, y, 1);
    }

    static final String[] LOC = {"Tất cả", "Thuốc", "Trang bị", "Nguyên liệu", "Nhiệm vụ"};
    static final int LOC_TAT_CA = 0, LOC_THUOC = 1, LOC_DO = 2, LOC_NGUYEN_LIEU = 3, LOC_NHIEM_VU = 4;
    /** bộ lọc hành trang đầy đủ */
    int loc;

    /**
     * Mục hành trang đang xem: Integer = thuốc (loại), ItemInInventory = đồ, GemTemplate = nguyên liệu, int[]{id} = vật
     * phẩm nhiệm vụ. Ô trang bị: món đang mặc + món hợp ô; dem[0] = số món đúng loại nhưng thiếu cấp.
     */
    Vector dsHanhTrang(ItemInInventory mac, int[] dem) {
        Vector ds = new Vector();
        MainChar m = mc();
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
            // hành trang đầy đủ (bán / bỏ / tách đồ...) = bảng cũ
            wDu = Math.min(Font.borderFont.getWidth("Đầy đủ") + 24, w / 3);
            Bang.nut(g, x + w - 4 - wDu, y + 4, wDu, hd, "Đầy đủ", Bang.PHU, new IAction() {
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
        KieuO.oDo(g, x + 3, y + 3, s, true, false);
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
            Res.paintGem(g, t.idImage, x + 3 + s / 2, y + 3 + s / 2);
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
            game.render.screen.GameData.paintIcon(g, (short) (game.model.ItemQuest.ICON_IMG[id] + 8000), x + 3 + s / 2, y + 3 + s / 2);
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
        int s = h - 6;
        boolean pp = loai >= 4 && loai <= 6 || loai == 23 || loai == 24 || loai == 95 || loai == 96;
        KieuO.oChieu(g, x + 3, y + 3, s, s, pp ? 2 : 1, KieuO.HINH_GIOT, false);
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

    static void veHangMon(Graphics g, int x, int y, int w, int h, int i, ItemInInventory it, boolean laMac, Object viec) {
        Bang.hang(g, x, y, w, h, i, laMac, viec, "mon-" + i);
        ItemTemplate t = mau(it);
        int s = h - 6;
        KieuO.oDo(g, x + 3, y + 3, s, true, false);
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

    private void veDanhSachThu(Graphics g, int x, int y, int w, int h) {
        if (Char.animal.size() == 0) {
            Bang.trong(g, x, y, w, h, "Chưa có thú cưỡi", GOI_Y[O_THU]);
            return;
        }
        Bang.danhSach(g, this.ds, x, y, w, h, Char.animal.size(), Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final AnimalInfo a = (AnimalInfo) Char.animal.elementAt(i);
                Bang.hang(g, x, y, w, h, i, false, new IAction() {
                    public void perform() {
                        MainChar m = mc();
                        if ((m.useHorse > -1 || m.idhorse > -1) && a.typeAnimal == 0) {
                            DungDo.bao("Đang cưỡi thú khác.");
                        } else {
                            GameService.gI().doRideAnimal(a.id, a.typeAnimal);
                        }
                    }
                }, "thu-" + i);
                int s = oDau(g, x, y, h);
                a.paint(g, x + 3 + s / 2, y + 3 + s / 2);
                chuHang(g, x, y, w, h, s, a.name, "Cấp " + a.level, "Chạm để cưỡi");
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
        // nút to
        int yn = y + h - 6 - nutH;
        int wn = w - 12;
        boolean coGo = this.xemDangMac;
        boolean duCap = t == null || capCan(it, t) <= mc().level;
        if (coGo) {
            Bang.nut(g, x + 6, yn, wn, nutH, "Gỡ trang bị", Bang.CHINH, new IAction() {
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
        GiaoDien.khoiDoc(g, x, y, lw, h, 8, 0x2C4A86, 0x16285A);
        float z = Math.max(1.5f, Math.min(3.4f, Math.min(lw / 48f, h / 40f)));
        veNguoiChoi(g, mc(), x + lw / 2, y + h - (int) (12 * z), z);
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
        // hàng ô nhanh (gán chiêu: một chạm) + điểm kỹ năng còn lại
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
            Bang.oDo(g, xi, y + 6, sO, true, false, new IAction() {
                public void perform() {
                    ChonChieuNhanh.moO(oo);
                }
            }, "o-nhanh-" + i);
            Font.borderFont.drawString(g, String.valueOf(i + 1), xi + sO / 2, y + 6 + (sO - Font.borderFont.getHeight()) / 2, Font.CENTER);
            xHet = xi + sO;
        }
        String diem = "Điểm kỹ năng: " + m.skillPointLeft;
        if (xHet + 16 + Font.blackFont.getWidth(diem) < x + w - 8) {
            Font.blackFont.drawString(g, diem, x + w - 10, y + 6 + (sO - Font.blackFont.getHeight()) / 2, 1);
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
        yl += Bang.hangChip(g, x, yl, w, new String[]{"Chiêu của Pokémon", "Học mới"}, this.hocMoi ? 1 : 0, vc) + k;
        int hl = y + h - yl;
        if (this.hocMoi) {
            final Vector moi = chieuHocMoi();
            if (moi.size() == 0) {
                Bang.trong(g, x, yl, w, hl, "Chưa có chiêu mới để học", "Gặp NPC dạy chiêu ở thành phố khi đủ cấp.");
                return;
            }
            Bang.danhSach(g, this.ds, x, yl, w, hl, moi.size(), Bang.caoHang(), new Bang.Hang() {
                public void ve(Graphics g, final int i, int x, int y, int w, int h) {
                    final game.model.skill.InfoSkillLearn sk = (game.model.skill.InfoSkillLearn) moi.elementAt(i);
                    Bang.hang(g, x, y, w, h, i, false, new IAction() {
                        public void perform() {
                            HopXacNhan.hoiGia("Học kỹ năng", "Học " + sk.name + "?" + (sk.decript.length() > 0 ? " " + sk.decript : ""), "Học",
                                    TienTe.XU, sk.price, new IAction() {
                                public void perform() {
                                    GameService.gI().learnSkill(lop, i);
                                }
                            });
                        }
                    }, "hoc-" + i);
                    int s = h - 6;
                    KieuO.oChieu(g, x + 3, y + 3, s, s, PokeChieu.he(lop, sk.idSkill), PokeChieu.hinh(lop, sk.idSkill), false);
                    int wg = TienTe.rongGia(TienTe.XU, sk.price, Font.blackFont, true);
                    Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, sk.name, w - s - 24 - wg), x + s + 9, y + 2, 0);
                    TienTe.veGia(g, TienTe.XU, sk.price, x + w - 8 - wg, y + 2, Font.blackFont, true);
                    Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, sk.decript, w - s - 14), x + s + 9, y + 3 + Font.blackFont.getHeight(), 0);
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
                    KieuO.oChieu(g, xr + 3, y + 3, s, s, PokeChieu.he(lop, i), PokeChieu.hinh(lop, i), cap <= 0);
                    chuHang(g, xr, y, wc, h, s, PokeChieu.ten(lop, i), cap <= 0 ? "Chưa học" : (cap >= PokeChieu.CAP_TOI_DA ? "Tối đa" : "Cấp " + cap),
                            PokeChieu.moTa(lop, i));
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
        KieuO.oChieu(g, x + hd + 10, y + 4, s, s, PokeChieu.he(lop, i), PokeChieu.hinh(lop, i), capChieu(i) <= 0);
        int xt = x + hd + s + 18;
        int cap = capChieu(i);
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, PokeChieu.ten(lop, i), x + w - 8 - xt), xt, y + 5, 0);
        int xh = xt + KieuO.vienHe(g, xt, y + 9 + Font.blackFont.getHeight(), PokeChieu.he(lop, i)) + 6;
        Font.arialFont.drawString(g, cap <= 0 ? "Chưa học" : "Cấp " + cap + "/" + PokeChieu.CAP_TOI_DA, xh, y + 9 + Font.blackFont.getHeight(), 0);
        int yy = y + 4 + s + k;
        yy = Bang.doan(g, Font.blackFont, PokeChieu.moTa(lop, i), x + 10, yy, w - 20, false);
        try {
            long hoi = game.model.skill.SkillManager.getSkillCooldown((byte) i, m.clazz, Math.max(1, cap));
            Font.arialFont.drawString(g, "Hồi chiêu: " + hoi / 1000 + "," + (hoi % 1000) / 100 + " giây", x + 10, yy + 2, 0);
        } catch (Throwable e) {
            // thiếu dữ liệu chiêu
        }
        int nh = Bang.caoHang();
        int yn = y + h - 6 - nh;
        int wb = (w - 12 - k) / 2;
        int capCan = 0;
        try {
            capCan = game.model.skill.SkillManager.getLvAddSkill(m.clazz, i, Math.max(0, cap));
        } catch (Throwable e) {
            capCan = 0;
        }
        boolean cong = m.skillPointLeft > 0 && m.level >= capCan && cap < PokeChieu.CAP_TOI_DA;
        Bang.nut(g, x + 6, yn, wb, nh, "Gán ô nhanh", Bang.PHU, new IAction() {
            public void perform() {
                ChonChieuNhanh.moO(0);
            }
        }, "gan-o");
        String chuCong = cong ? "Cộng điểm (+1)" : (cap >= PokeChieu.CAP_TOI_DA ? "Đã tối đa" : (m.skillPointLeft <= 0 ? "Hết điểm" : "Cần cấp " + capCan));
        Bang.nut(g, x + 6 + wb + k, yn, w - 12 - wb - k, nh, chuCong, cong ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
            public void perform() {
                GameService.gI().addSkillPoint(i);
            }
        }, "cong-diem");
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
        GiaoDien.khoiDoc(g, x, y, lw, h, 8, 0x2C4A86, 0x16285A);
        // người chơi + thú cưỡi / Pokémon đi theo cạnh nhau
        float z = Math.max(1.4f, Math.min(3.2f, Math.min(lw / (m.pokeThuDex > 0 ? 100f : 56f), h / 42f)));
        int yChan = y + h - (int) (12 * z);
        if (m.pokeThuDex > 0 && PokeBody.coSprite(m.pokeThuDex, m.pokeThuShiny)) {
            veNguoiChoi(g, m, x + lw * 32 / 100, yChan, z);
            if (PhongVe.co()) {
                int tx = g.getTranslateX(), ty = g.getTranslateY();
                PhongVe.bo.bat(g, z);
                PokeBody.paint(g, m.pokeThuDex, m.pokeThuShiny, (int) ((x + lw * 70 / 100 + tx) / z), (int) ((yChan + ty) / z),
                        NgoaiHinhVe.huongThe(Canvas.gameTick + 40), PokeDuLieu.DUNG, Canvas.gameTick, 100);
                PhongVe.bo.tat(g);
                g.translate(tx, ty);
            } else {
                PokeBody.paint(g, m.pokeThuDex, m.pokeThuShiny, x + lw * 70 / 100, yChan, 0, PokeDuLieu.DUNG, Canvas.gameTick, (int) (100 * z));
            }
        } else {
            veNguoiChoi(g, m, x + lw / 2, yChan, z);
        }
        int xr = x + lw + k, wr = w - lw - k;
        Bang.the(g, xr, y, wr, h);
        Font.blackFont.drawString(g, "Thú cưỡi / đi theo", xr + 8, y + 6, 0);
        int yl = y + 10 + Font.blackFont.getHeight();
        veDanhSachThu(g, xr + 4, yl, wr - 8, y + h - 4 - yl);
    }

    private void veNgoaiHinh(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int lw = Math.min(w * 42 / 100, 240);
        GiaoDien.khoiDoc(g, x, y, lw, h, 8, 0x2C4A86, 0x16285A);
        float z = Math.max(1.5f, Math.min(3.4f, Math.min(lw / 48f, h / 40f)));
        veNguoiChoi(g, mc(), x + lw / 2, y + h - (int) (12 * z), z);
        int xr = x + lw + k, wr = w - lw - k;
        Bang.the(g, xr, y, wr, h);
        NgoaiHinhMenu.nap();
        final int an = NgoaiHinhMenu.anHienTai();
        int rh = Bang.caoHang();
        int n = NgoaiHinhMenu.TEN_O.length + 1;
        int yy = y + 6;
        for (int i = 0; i < n && yy + rh <= y + h - 4; i++) {
            final int bit = i == 0 ? NgoaiHinhCua.AN_TRANG_PHUC : 1 << (i - 1);
            String ten = i == 0 ? "Trang phục" : NgoaiHinhMenu.TEN_O[i - 1];
            boolean hien = (an & bit) == 0;
            Bang.hang(g, xr + 4, yy, wr - 8, rh, i, false, new IAction() {
                public void perform() {
                    NgoaiHinhMenu.gui(an ^ bit);
                }
            }, "ngoai-hinh-" + i);
            Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, ten, wr - 80), xr + 12, yy + (rh - Font.blackFont.getHeight()) / 2, 0);
            int wv = 54;
            GiaoDien.vien(g, xr + wr - 10 - wv, yy + (rh - 16) / 2, wv, 16, hien ? 0x48C058 : 0x8A94A6, hien ? "Hiện" : "Ẩn");
            yy += rh + k;
        }
    }
}
