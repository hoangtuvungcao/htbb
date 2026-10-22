package game.pokemon;

import game.model.Char;
import game.model.GemTemp;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.Screen;
import game.render.screen.WindowInfoScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * CỬA HÀNG (0.9.3, bảng chung Bang): tab Mua | Bán, chip lọc theo loại, hàng = ảnh + tên + giá (biểu tượng tiền
 * TienTe); 0.9.7: chạm hàng = THẺ VẬT PHẨM kiểu NRO (TheVatPham.moMua: thông tin, chỉ số, mô tả, chọn số − / + /
 * Tối đa, tổng tiền có biểu tượng, nút "Mua"). Ba kiểu: THUỐC (Poké Mart, gói 33 NPC bán thuốc), ĐỒ (NPC bán trang bị) và
 * ĐẶC BIỆT (cửa hàng lượng, Res.shopTemplate). Mua gửi NGAY (server trả túi / tiền / thông báo). "Bảng cũ" mở cửa sổ
 * cũ (WindowInfoScr) cùng dữ liệu - chỉ còn để dự phòng.
 */
public final class ManCuaHang extends Screen {
    public static ManCuaHang me;
    /** MAY_CHU (0.9.7): danh sách chọn vật phẩm máy chủ gửi (-76: shop đặc biệt / sự kiện, túi đồ lấy ra / bỏ vào, cải
     * trang, bảo vật...) - trước là cửa sổ cũ ShopNew; động từ nút = ShopNew.text, tiêu đề = ShopNew.nameshop */
    public static final int THUOC = 0, DO = 1, DAC_BIET = 2, MAY_CHU = 3, NGUYEN_LIEU = 4;

    int kieu;
    byte[] chiSoCu;
    int tab;          // 0 mua, 1 bán
    int loc;          // chip lọc (0 = tất cả)
    Object chon;      // món đang xem
    int soLuong = 1;
    final Cuon ds = new Cuon();

    public static ManCuaHang gI() {
        if (me == null) {
            me = new ManCuaHang();
        }
        return me;
    }

    /** Mở cửa hàng; chiSoCu = tab cửa sổ cũ (dự phòng "Bảng cũ"). */
    public static void mo(int kieu, byte[] chiSoCu) {
        ManCuaHang m = gI();
        m.kieu = kieu;
        m.chiSoCu = chiSoCu;
        m.tab = 0;
        m.loc = 0;
        m.chon = null;
        m.soLuong = 1;
        m.ds.den(0);
        m.left = null;
        m.center = null;
        m.right = null;
        Canvas.menu.showMenu = false;
        Canvas.endDlg();
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    /** Danh sách -76 (GameScr.onShopNew) / mục "Cửa hàng" tiện ích: mở (hoặc làm mới tại chỗ nếu đang mở). */
    public static void moMayChu() {
        ManCuaHang m = gI();
        if (Canvas.currentScreen == m && m.kieu == MAY_CHU) {
            m.chon = null;
            m.daMoChon = null;
            m.dsDem = null;
            return;
        }
        mo(MAY_CHU, null);
    }

    /** Động từ nút của danh sách máy chủ (viết hoa chữ đầu; trống = "Chọn"). */
    static String dongTu() {
        String t = game.model.ShopNew.text;
        if (t == null || t.trim().length() == 0) {
            return "Chọn";
        }
        t = t.trim();
        return ChuPokemon.hoa(t.charAt(0)) + t.substring(1);
    }

    /** Danh sách máy chủ là CỬA HÀNG (động từ "Mua"). */
    static boolean laMua() {
        return "mua".equals(ChuPokemon.thuong(dongTu()));
    }

    /** Giá trị thuộc tính id của món (0 = không có); máy chủ gửi giá lượng = 125, số lượng gói = 126 (ItemShop). */
    static int thuocTinh(ItemInInventory it, int id) {
        for (int i = 0; it != null && i < it.allAttribute.size(); i++) {
            game.model.InfoAttributeItem a = (game.model.InfoAttributeItem) it.allAttribute.elementAt(i);
            if (a.getID() == id) {
                return a.getValueAtt();
            }
        }
        return 0;
    }

    static MainChar mc() {
        return Canvas.gameScr.mainChar;
    }

    void dong() {
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.gameScr.switchToMe();
    }

    void lui() {
        if (this.chon != null) {
            this.chon = null;
        } else {
            dong();
        }
    }

    void moCu() {
        if (this.chiSoCu == null) {
            return;
        }
        WindowInfoScr.gI().setInfo(this.chiSoCu.length > 1 ? 1 : 0, true, this.chiSoCu);
        WindowInfoScr.gI().switchToMe();
    }

    // ------------------------------------------------------------------ dữ liệu

    static String tenThuoc(int loai) {
        try {
            String t = MainChar.listPotion[loai].name;
            if (t != null && t.length() > 0) {
                return t;
            }
        } catch (Throwable e) {
            // bỏ qua
        }
        return "Thuốc " + loai;
    }

    static int giaThuoc(int loai) {
        try {
            return Res.potionTemplates[loai].price;
        } catch (Throwable e) {
            return 0;
        }
    }

    /** Loại (ItemTemplate.type) có trong cửa hàng ĐỒ, theo thứ tự gặp. */
    Vector loaiCo() {
        Vector v = new Vector();
        Vector shop = Canvas.gameScr.shop;
        for (int i = 0; shop != null && i < shop.size(); i++) {
            ItemInInventory it = (ItemInInventory) shop.elementAt(i);
            ItemTemplate t = ManNhanVat.mau(it);
            if (t != null) {
                Integer k = new Integer(t.type);
                if (!v.contains(k)) {
                    v.addElement(k);
                }
            }
        }
        return v;
    }

    private Vector dsDem;
    private int khoaDs = 1;

    /** Danh sách hàng của tab hiện tại (đã lọc) - nhớ giữa các khung, dựng lại khi dữ liệu / bộ lọc đổi. */
    Vector dsHang() {
        int k = TrangThaiNguoiChoi.phienBan * 31 + this.tab * 7 + this.kieu * 3 + this.loc;
        k = k * 31 + Bang.vanTay(Char.inventory);
        k = k * 31 + Bang.vanTay(Res.shopTemplate);
        k = k * 31 + Bang.vanTay(Canvas.gameScr == null ? null : Canvas.gameScr.shop);
        k = k * 31 + (this.kieu == MAY_CHU ? Bang.vanTay(game.model.ShopNew.gI().ItemShopNew) : 0);
        if (this.dsDem != null && k == this.khoaDs) {
            return this.dsDem;
        }
        this.dsDem = dsHang0();
        this.khoaDs = k;
        return this.dsDem;
    }

    private Vector dsHang0() {
        Vector v = new Vector();
        if (this.tab == 1) {
            for (int i = 0; i < Char.inventory.size(); i++) {
                v.addElement(Char.inventory.elementAt(i));
            }
            return v;
        }
        if (this.kieu == MAY_CHU) {
            Vector ds = game.model.ShopNew.gI().ItemShopNew;
            for (int i = 0; ds != null && i < ds.size(); i++) {
                v.addElement(ds.elementAt(i));
            }
            return v;
        }
        if (this.kieu == DAC_BIET) {
            for (int i = 0; i < Res.shopTemplate.size(); i++) {
                GemTemp g = (GemTemp) Res.shopTemplate.elementAt(i);
                if (g.isSell && (this.loc == 0 || (this.loc == 1) == (g.typeMoney == 0))) {
                    v.addElement(g);
                }
            }
            return v;
        }
        Vector shop = Canvas.gameScr.shop;
        if (this.kieu == NGUYEN_LIEU) {
            for (int i = 0; shop != null && i < shop.size(); i++) {
                if (shop.elementAt(i) instanceof GemTemp) {
                    v.addElement(shop.elementAt(i));
                }
            }
            return v;
        }
        Vector loai = this.kieu == DO ? loaiCo() : null;
        for (int i = 0; shop != null && i < shop.size(); i++) {
            ItemInInventory it = (ItemInInventory) shop.elementAt(i);
            if (this.kieu == DO && this.loc > 0) {
                ItemTemplate t = ManNhanVat.mau(it);
                if (t == null || this.loc - 1 >= loai.size() || ((Integer) loai.elementAt(this.loc - 1)).intValue() != t.type) {
                    continue;
                }
            }
            v.addElement(it);
        }
        return v;
    }

    String[] chips() {
        if (this.tab == 1 || this.kieu == MAY_CHU) {
            return null;
        }
        if (this.kieu == DAC_BIET) {
            return new String[]{"Tất cả", TienTe.ten(TienTe.LUONG), TienTe.ten(TienTe.XU)};
        }
        if (this.kieu != DO) {
            return null;
        }
        Vector loai = loaiCo();
        if (loai.size() <= 1) {
            return null;
        }
        String[] c = new String[loai.size() + 1];
        c[0] = "Tất cả";
        for (int i = 0; i < loai.size(); i++) {
            String t = BangTrangBi.tenLoai(((Integer) loai.elementAt(i)).intValue());
            c[i + 1] = t == null ? "Khác" : t;
        }
        return c;
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
        if (Canvas.currentScreen != this || Canvas.currentDialog != null) {
            return;
        }
        if (this.kieu != MAY_CHU && (Canvas.isKeyPressed(4) || Canvas.isKeyPressed(6))) {
            chonTab(1 - this.tab);
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
            return false;
        }
        if (keyCode == -7 || keyCode == 27) {
            lui();
            return true;
        }
        return false;
    }

    void chonTab(int t) {
        this.tab = t;
        this.loc = 0;
        this.chon = null;
        this.ds.den(0);
    }

    // ------------------------------------------------------------------ vẽ

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);   // 0.9.5: chỉ lớp thế giới (HUD sau bảng không vẽ)
        } catch (Throwable e) {
            // bỏ qua
        }
        Bang.phuNen(g);
        Object[] vt = {new IAction() {
            public void perform() {
                ManCuaHang.this.chonTab(0);
            }
        }, new IAction() {
            public void perform() {
                ManCuaHang.this.chonTab(1);
            }
        }};
        String ten = this.kieu == THUOC ? "Poké Mart" : (this.kieu == DAC_BIET ? "Cửa hàng đặc biệt"
                : this.kieu == NGUYEN_LIEU ? "Cửa hàng nguyên liệu" : "Cửa hàng");
        if (this.kieu == MAY_CHU) {
            this.tab = 0;
            String n = game.model.ShopNew.nameshop;
            ten = n == null || n.trim().length() == 0 ? "Vật phẩm" : n.trim();
        }
        int[] c = Bang.khung(g, ten, this.kieu == MAY_CHU ? null : new String[]{"Mua", "Bán"}, this.tab, vt, new IAction() {
            public void perform() {
                ManCuaHang.this.dong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        if (this.tab == 1) {
            veBan(g, x, y, w, h);   // 0.9.7: tab Bán = lưới hành trang NRO, chạm ô = thẻ vật phẩm (nút Bán)
            return;
        }
        // 0.9.7 (NRO): danh sách hàng trọn bề ngang; chạm một món = THẺ VẬT PHẨM (thông tin + chọn số + giá + "Mua")
        veDanhSach(g, x, y, w, h);
        moTheMua();
    }

    /** món đã mở thẻ (không mở lại khi thẻ nhường chỗ hộp khác) */
    private Object daMoChon;

    /** Mở thẻ MUA cho món đang chọn (một lần mỗi món). */
    void moTheMua() {
        final Object o = this.chon;
        if (o == null || o == this.daMoChon || TheVatPham.dangMo() || Canvas.currentDialog != null) {
            return;
        }
        this.daMoChon = o;
        IAction dong = new IAction() {
            public void perform() {
                ManCuaHang.this.chon = null;
                ManCuaHang.this.daMoChon = null;
            }
        };
        if (o instanceof GemTemp && this.kieu == NGUYEN_LIEU) {
            moTheNguyenLieu((GemTemp) o, dong);
            return;
        }
        if (o instanceof GemTemp) {
            final GemTemp t = Res.getShopByID(((GemTemp) o).id) != null ? Res.getShopByID(((GemTemp) o).id) : (GemTemp) o;
            final int loai = t.typeMoney == 0 ? TienTe.LUONG : TienTe.XU;
            TheVatPham.moMua(t, t.price, loai, 1, new TheVatPham.ViecMua() {
                public String khongMua(int so) {
                    if (mc().isFullInventory()) {
                        return "Hành trang đã đầy.";
                    }
                    long co = loai == TienTe.LUONG ? mc().luong : mc().charXu;
                    return t.price > co ? "Không đủ " + TienTe.ten(loai) + "." : null;
                }

                public void mua(int so) {
                    GameService.gI().buyItemShopSpecial(((GemTemp) o).id);
                }
            }, dong);
            return;
        }
        final ItemInInventory it = (ItemInInventory) o;
        if (this.kieu == MAY_CHU) {
            moTheMayChu(it, dong);
            return;
        }
        if (this.kieu == THUOC) {
            final int loai = it.potionType;
            final int max = toiDaThuoc(loai);
            TheVatPham.moMua(new Integer(loai), giaThuoc(loai), TienTe.XU, Math.max(1, max), new TheVatPham.ViecMua() {
                public String khongMua(int so) {
                    if (max < 1) {
                        return (long) giaThuoc(loai) > mc().charXu ? "Không đủ xu." : "Không mua thêm được (tối đa 999 bình).";
                    }
                    return (long) so * giaThuoc(loai) > mc().charXu ? "Không đủ xu." : null;
                }

                public void mua(int so) {
                    ItemInInventory m = new ItemInInventory();
                    m.potionType = (byte) loai;
                    m.number = (short) so;
                    Vector v = new Vector();
                    v.addElement(m);
                    GameService.gI().buyPotion(v);
                }
            }, dong);
            TheVatPham.gI().so = Math.max(1, Math.min(Math.max(1, max), this.soLuong));
            return;
        }
        final ItemTemplate t = ManNhanVat.mau(it);
        final boolean thue = t != null && t.ndayLoan != 0;
        final int loai = thue ? TienTe.LUONG : TienTe.XU;
        final long gia = t == null ? 0 : t.price;
        TheVatPham.moMua(it, gia, loai, 1, new TheVatPham.ViecMua() {
            public String khongMua(int so) {
                if (mc().FullInventory() >= TrangThaiNguoiChoi.sucChua(mc())) {
                    return "Hành trang đã đầy.";
                }
                if (!thue && gia > mc().charXu) {
                    return "Không đủ " + TienTe.ten(TienTe.XU) + ".";
                }
                return null;
            }

            public void mua(int so) {
                Vector v = new Vector();
                v.addElement(it);
                GameService.gI().buyItem(v);
            }
        }, dong);
    }

    /**
     * Thẻ cho món của danh sách máy chủ: thông tin + nút động từ ("Mua" / "Lấy ra" / "Bỏ vào" / "Chọn"...). Cửa hàng
     * ("Mua"): giá lượng (thuộc tính 125) trong thẻ + chọn số (mỗi chiếc một lệnh -76 như cửa sổ cũ).
     */
    void moTheMayChu(final ItemInInventory it, IAction dong) {
        final int idx = game.model.ShopNew.gI().ItemShopNew.indexOf(it);
        final short loaiDs = game.model.ShopNew.gI().typeshop;
        final boolean mua = laMua();
        final long gia = mua ? thuocTinh(it, 125) : 0;
        int toiDa = 1;
        if (mua) {
            int cho = Math.max(0, TrangThaiNguoiChoi.sucChua(mc()) - mc().FullInventory());
            int theoTien = gia > 0 ? (int) Math.min(10, mc().luong / gia) : 1;
            toiDa = Math.max(1, Math.min(10, Math.min(cho, theoTien)));
        }
        TheVatPham.moMua(it, gia, TienTe.LUONG, toiDa, mua ? null : dongTu(), new TheVatPham.ViecMua() {
            public String khongMua(int so) {
                if (idx < 0) {
                    return "Vật phẩm không còn trong danh sách.";
                }
                if (mua && mc().FullInventory() >= TrangThaiNguoiChoi.sucChua(mc())) {
                    return "Hành trang đã đầy.";
                }
                if (mua && gia * so > mc().luong) {
                    return "Không đủ " + TienTe.ten(TienTe.LUONG) + ".";
                }
                return null;
            }

            public void mua(int so) {
                for (int i = 0; i < so; i++) {
                    GameService.gI().buyItemShopnew((byte) idx, loaiDs);
                }
            }
        }, dong);
    }

    /** Số nguyên liệu id đang có (túi thường + khoá). */
    static int dangCoNguyenLieu(int id) {
        int n = 0;
        Vector[] ds = {MainChar.gemItem, MainChar.gemItemKhoa};
        for (int k = 0; k < ds.length; k++) {
            for (int i = 0; ds[k] != null && i < ds[k].size(); i++) {
                game.model.GemTemplate g = (game.model.GemTemplate) ds[k].elementAt(i);
                if (g.id == id) {
                    n += g.number;
                }
            }
        }
        return n;
    }

    /** Thẻ mua nguyên liệu (NPC bán ngọc / nguyên liệu, lệnh 74): chọn số (tối đa 300 chiếc mỗi loại) + giá. */
    void moTheNguyenLieu(final GemTemp g0, IAction dong) {
        GemTemp t0 = Res.getGemByID(g0.id);
        final GemTemp t = t0 != null ? t0 : g0;
        final int loai = t.typeMoney == 0 ? TienTe.XU : TienTe.LUONG;   // như cửa sổ cũ: 0 = xu
        final long co = loai == TienTe.XU ? mc().charXu : mc().luong;
        final int con = Math.max(0, 300 - dangCoNguyenLieu(t.id));
        int theoTien = t.price <= 0 ? con : (int) Math.min(con, co / t.price);
        final int toiDa = Math.max(1, Math.min(con, Math.min(300, theoTien)));
        game.model.GemTemplate mon = new game.model.GemTemplate();
        mon.id = t.id;
        TheVatPham.moMua(mon, t.price, loai, toiDa, new TheVatPham.ViecMua() {
            public String khongMua(int so) {
                if (con <= 0) {
                    return "Đã có tối đa 300 chiếc.";
                }
                long tien = loai == TienTe.XU ? mc().charXu : mc().luong;
                return (long) so * t.price > tien ? "Không đủ " + TienTe.ten(loai) + "." : null;
            }

            public void mua(int so) {
                GemTemp ge = new GemTemp();
                ge.id = t.id;
                ge.number = (short) so;
                Vector v = new Vector();
                v.addElement(ge);
                GameService.gI().buyGemItem(v);
            }
        }, dong);
    }

    /** Tab Bán (0.9.7): lưới hành trang (LuoiTui, ngữ cảnh BÁN) - chạm ô = thẻ vật phẩm với nút Bán / lý do không bán được. */
    private void veBan(Graphics g, int x, int y, int w, int h) {
        Object[] vl = new Object[LuoiTui.LOC.length];
        for (int i = 0; i < vl.length; i++) {
            final int li = i;
            vl[i] = new IAction() {
                public void perform() {
                    ManCuaHang.this.loc = li;
                    ManCuaHang.this.ds.den(0);
                }
            };
        }
        int lc = this.loc < LuoiTui.LOC.length ? this.loc : 0;
        LuoiTui.ve(g, this.ds, x, y, w, h, lc, LuoiTui.ds(lc), false, TheVatPham.monDangXem(), vl, new LuoiTui.Bam() {
            public void bam(Object mon) {
                if (mon != null) {
                    TheVatPham.mo(mon, TheVatPham.BAN, false, null);
                }
            }
        }, TheVatPham.BAN);
    }

    private void veDanhSach(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        // tiền đang có + bảng cũ
        int hd = Bang.co(26);
        int xx = x + 6;
        xx += TienTe.vePill(g, TienTe.XU, mc().charXu, xx, y + 4, hd, Font.blackFont) + k;
        TienTe.vePill(g, TienTe.LUONG, mc().luong, xx, y + 4, hd, Font.blackFont);
        // 0.9.5: bỏ nút "Bảng cũ" - màn mới đã có Mua / Bán (một lối vào mỗi việc)
        int yl = y + 4 + hd + k;
        String[] ch = chips();
        if (ch != null) {
            Object[] vl = new Object[ch.length];
            for (int i = 0; i < ch.length; i++) {
                final int li = i;
                vl[i] = new IAction() {
                    public void perform() {
                        ManCuaHang.this.loc = li;
                        ManCuaHang.this.chon = null;
                        ManCuaHang.this.ds.den(0);
                    }
                };
            }
            yl += Bang.hangChip(g, x + 6, yl, w - 12, ch, this.loc, vl) + k;
        }
        final Vector v = dsHang();
        int hl = y + h - 4 - yl;
        if (v.size() == 0) {
            Bang.trong(g, x + 4, yl, w - 8, hl, this.kieu == MAY_CHU ? "Không có vật phẩm" : this.tab == 0 ? "Cửa hàng chưa có hàng"
                    : "Hành trang không có đồ để bán", null);
            return;
        }
        // hàng gọn hơn (vẫn >= cỡ chạm tối thiểu) để thấy nhiều món hơn
        // 0.9.7: hàng đủ cao cho 2 dòng (tên + dòng phụ) đo theo font thật - dòng phụ không tràn khung hàng
        int rh = Math.max(Math.max(VungCham.MIN + 2, Bang.caoHang() * 5 / 6), 2 + Font.blackFont.getHeight() + 1 + Font.arialFont.getHeight() + 3);
        Bang.danhSach(g, this.ds, x + 4, yl, w - 8, hl, v.size(), rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final Object o = v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, o == ManCuaHang.this.chon, new IAction() {
                    public void perform() {
                        ManCuaHang.this.chon = o;
                        ManCuaHang.this.soLuong = 1;
                    }
                }, "hang-" + i);
                veHang(g, o, x, y, w, h);
            }
        });
    }

    /** Một hàng: ảnh + tên + giá canh phải + dòng phụ. */
    void veHang(Graphics g, Object o, int x, int y, int w, int h) {
        int s = ManNhanVat.oDau(g, x, y, h);
        String ten, phu;
        int loaiTien = TienTe.XU;
        long gia;
        if (o instanceof GemTemp && this.kieu == NGUYEN_LIEU) {
            GemTemp t = Res.getGemByID(((GemTemp) o).id);
            if (t == null) {
                t = (GemTemp) o;
            }
            IconVatPham.ve(g, (short) (t.idImage + 6500), x + 3 + s / 2, y + 3 + s / 2, s, IconVatPham.DP_NGOC);
            ten = t.name;
            phu = "Đang có " + dangCoNguyenLieu(t.id);
            gia = t.price;
            loaiTien = t.typeMoney == 0 ? TienTe.XU : TienTe.LUONG;
        } else if (o instanceof GemTemp) {
            GemTemp t = Res.getShopByID(((GemTemp) o).id);
            if (t == null) {
                t = (GemTemp) o;
            }
            // gói cửa hàng (data_shop.idImg) dùng dải ảnh thuốc 5500 (như cửa sổ cũ Res.paintPotion), không phải ngọc
            IconVatPham.ve(g, (short) (t.idImage + 5500), x + 3 + s / 2, y + 3 + s / 2, s, IconVatPham.DP_DO);
            ten = t.name;
            phu = t.decript;
            gia = t.price;
            loaiTien = t.typeMoney == 0 ? TienTe.LUONG : TienTe.XU;
        } else {
            ItemInInventory it = (ItemInInventory) o;
            if (this.kieu == THUOC && this.tab == 0) {
                ManNhanVat.veThuoc(g, it.potionType, x + 3 + s / 2, y + 3 + s / 2, s);
                ten = tenThuoc(it.potionType);
                gia = giaThuoc(it.potionType);
                phu = "Đang có " + mc().potions[it.potionType];
            } else {
                ItemTemplate t = ManNhanVat.mau(it);
                int hiem = KieuO.mauHiem(it.colorName);
                if (hiem >= 0) {
                    KieuO.khungHiem(g, x + 3, y + 3, s, hiem);
                }
                if (t != null) {
                    Bang.icon(g, t.idIcon, x + 3 + s / 2, y + 3 + s / 2, s >= 26 ? s / 22f : 1f);
                }
                ten = t == null ? "?" : t.name;
                String loai = t == null ? null : BangTrangBi.tenLoai(t.type);
                phu = "Cấp " + ManNhanVat.capCan(it, t) + (loai != null ? " · " + loai : "");
                gia = t == null ? 0 : (this.tab == 1 ? t.price / 5 + t.plus * 1000 : t.price);
                if (t != null && t.ndayLoan != 0 && this.tab == 0) {
                    loaiTien = TienTe.LUONG;
                }
                if (this.kieu == MAY_CHU) {
                    // máy chủ chỉ gửi giá lượng (thuộc tính 125) cho cửa hàng; danh sách khác: không giá
                    gia = laMua() ? thuocTinh(it, 125) : 0;
                    loaiTien = TienTe.LUONG;
                    int goi = thuocTinh(it, 126);
                    if (goi > 1) {
                        ten = ten + " x" + goi;
                    }
                }
            }
        }
        boolean coGia = !(this.kieu == MAY_CHU && gia <= 0);
        int wg = coGia ? TienTe.rongGia(loaiTien, gia, Font.blackFont, true) : 0;
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, ten, w - s - 22 - wg), x + s + 9, y + 2, 0);
        if (coGia) {
            TienTe.veGia(g, loaiTien, gia, x + w - 8 - wg, y + 2, Font.blackFont, true);
        }
        if (phu != null) {
            Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, phu, w - s - 14), x + s + 9, y + 3 + Font.blackFont.getHeight(), 0);
        }
    }

    /** Số tối đa mua được (thuốc): tiền, chỗ (999 bình), 120 bình mỗi lần. */
    int toiDaThuoc(int loai) {
        int gia = giaThuoc(loai);
        long tien = mc().charXu;
        int theoTien = gia <= 0 ? 120 : (int) Math.min(120, tien / gia);
        int cho = 999 - mc().potions[loai];
        return Math.max(0, Math.min(theoTien, Math.min(120, cho)));
    }




}
