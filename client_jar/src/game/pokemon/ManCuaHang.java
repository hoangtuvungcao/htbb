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
    public static final int THUOC = 0, DO = 1, DAC_BIET = 2;

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
        if (this.tab == 1) {
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
        if (Canvas.isKeyPressed(4) || Canvas.isKeyPressed(6)) {
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
        String ten = this.kieu == THUOC ? "Poké Mart" : (this.kieu == DAC_BIET ? "Cửa hàng đặc biệt" : "Cửa hàng");
        int[] c = Bang.khung(g, ten, new String[]{"Mua", "Bán"}, this.tab, vt, new IAction() {
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
            Bang.trong(g, x + 4, yl, w - 8, hl, this.tab == 0 ? "Cửa hàng chưa có hàng" : "Hành trang không có đồ để bán", null);
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
        if (o instanceof GemTemp) {
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
            }
        }
        int wg = TienTe.rongGia(loaiTien, gia, Font.blackFont, true);
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, ten, w - s - 22 - wg), x + s + 9, y + 2, 0);
        TienTe.veGia(g, loaiTien, gia, x + w - 8 - wg, y + 2, Font.blackFont, true);
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
