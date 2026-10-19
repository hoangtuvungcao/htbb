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
 * TienTe), chạm hàng = thẻ chi tiết bên phải (màn hẹp: thay danh sách) với bộ đếm số lượng − / + / Tối đa, tổng tiền
 * và nút to "Mua" (xác nhận HopXacNhan). Ba kiểu: THUỐC (Poké Mart, gói 33 NPC bán thuốc), ĐỒ (NPC bán trang bị) và
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
        int k = Bang.cach();
        boolean hai = w >= 400;
        int wl = hai ? w * 54 / 100 : w;
        if (!hai && this.chon != null) {
            veChiTiet(g, x, y, w, h);
            return;
        }
        if (hai && this.chon == null) {
            // 0.9.5: hai cột -> chọn sẵn món đầu (khung chi tiết + bộ chọn số lượng hiện ngay, không để trống)
            Vector v = dsHang();
            if (v.size() > 0) {
                this.chon = v.elementAt(0);
                this.soLuong = 1;
            }
        }
        veDanhSach(g, x, y, wl, h);
        if (hai) {
            int xr = x + wl + k, wr = w - wl - k;
            if (this.chon != null) {
                veChiTiet(g, xr, y, wr, h);
            } else {
                Bang.the(g, xr, y, wr, h);
                Bang.trong(g, xr, y, wr, h, this.tab == 0 ? "Chọn một món để xem" : "Chọn món muốn bán",
                        this.tab == 0 ? "Chạm hàng bên trái: xem chi tiết, chọn số lượng rồi bấm Mua." : "Giá bán lại bằng 1/5 giá mua.");
            }
        }
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
        int rh = Math.max(VungCham.MIN + 2, Bang.caoHang() * 5 / 6);
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

    private void veChiTiet(Graphics g, int x, int y, int w, int h) {
        final Object o = this.chon;
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int hd = Bang.co(26);
        Bang.nutLui(g, x + 4, y + 4, hd, new IAction() {
            public void perform() {
                ManCuaHang.this.chon = null;
            }
        });
        int rh = Bang.caoHang();
        // hàng đầu: như hàng danh sách (ảnh + tên + giá)
        veHang(g, o, x + hd + 8, y + 2, w - hd - 12, rh);
        int yy = y + 4 + Math.max(hd, rh) + k;
        int nh = Bang.caoHang();
        int yn = y + h - 6 - nh;
        if (o instanceof GemTemp) {
            final GemTemp t = Res.getShopByID(((GemTemp) o).id) != null ? Res.getShopByID(((GemTemp) o).id) : (GemTemp) o;
            Bang.doan(g, Font.blackFont, t.decript, x + 10, yy, w - 20, false);
            final int loai = t.typeMoney == 0 ? TienTe.LUONG : TienTe.XU;
            Bang.nut(g, x + 6, yn, w - 12, nh, "Mua", Bang.XAC_NHAN, new IAction() {
                public void perform() {
                    if (mc().isFullInventory()) {
                        DungDo.loi("Hành trang đã đầy.");
                        return;
                    }
                    HopXacNhan.hoiGia("Mua vật phẩm", "Mua " + t.name + "?", "Mua", loai, t.price, new IAction() {
                        public void perform() {
                            GameService.gI().buyItemShopSpecial(((GemTemp) o).id);
                        }
                    });
                }
            }, "mua");
            return;
        }
        final ItemInInventory it = (ItemInInventory) o;
        if (this.kieu == THUOC && this.tab == 0) {
            // bộ đếm số lượng − / + / Tối đa + tổng tiền
            final int loai = it.potionType;
            final int max = toiDaThuoc(loai);
            if (this.soLuong > max) {
                this.soLuong = Math.max(max, 1);
            }
            int sb = nh;
            int wo = w - 20 - 3 * (sb + k);
            Bang.nut(g, x + 10, yy, sb, sb, "", Bang.PHU, new IAction() {
                public void perform() {
                    ManCuaHang.this.soLuong = Math.max(1, ManCuaHang.this.soLuong - 1);
                }
            }, "sl-tru");
            GiaoDien.khoi(g, x + 10 + sb + k, yy, wo, sb, 6, GiaoDien.VIEN);
            GiaoDien.khoi(g, x + 11 + sb + k, yy + 1, wo - 2, sb - 2, 5, 0xFFFBE6);
            Font.blackFont.drawString(g, String.valueOf(this.soLuong), x + 10 + sb + k + wo / 2, yy + (sb - Font.blackFont.getHeight()) / 2, Font.CENTER);
            Bang.nut(g, x + 10 + sb + k + wo + k, yy, sb, sb, "", Bang.PHU, new IAction() {
                public void perform() {
                    ManCuaHang.this.soLuong = Math.min(Math.max(1, max), ManCuaHang.this.soLuong + 1);
                }
            }, "sl-cong");
            Bang.nut(g, x + 10 + 2 * (sb + k) + wo + k - sb + sb, yy, sb, sb, "Max", Bang.TRUNG, new IAction() {
                public void perform() {
                    ManCuaHang.this.soLuong = Math.max(1, max);
                }
            }, "sl-max");
            g.setColor(0xFFFFFF);
            int cy = yy + sb / 2, a = sb / 4;
            g.fillRect(x + 10 + sb / 2 - a, cy - 1, 2 * a, 3);
            int cxp = x + 10 + sb + k + wo + k + sb / 2;
            g.fillRect(cxp - a, cy - 1, 2 * a, 3);
            g.fillRect(cxp - 1, cy - a, 3, 2 * a);
            yy += sb + k;
            final long tong = (long) this.soLuong * giaThuoc(loai);
            Font.blackFont.drawString(g, "Tổng:", x + 10, yy + 2, 0);
            TienTe.veGia(g, TienTe.XU, tong, x + 14 + Font.blackFont.getWidth("Tổng:"), yy + 2, Font.blackFont, false);
            final int sl = this.soLuong;
            boolean du = max >= 1 && tong <= mc().charXu;
            Bang.nut(g, x + 6, yn, w - 12, nh, du ? "Mua " + sl : (max < 1 ? "Không mua thêm được" : "Không đủ xu"), du ? Bang.XAC_NHAN : Bang.TAT,
                    new IAction() {
                public void perform() {
                    HopXacNhan.hoiGia("Mua thuốc", "Mua " + sl + " " + tenThuoc(loai) + "?", "Mua", TienTe.XU, tong, new IAction() {
                        public void perform() {
                            ItemInInventory mua = new ItemInInventory();
                            mua.potionType = (byte) loai;
                            mua.number = (short) sl;
                            Vector v = new Vector();
                            v.addElement(mua);
                            GameService.gI().buyPotion(v);
                        }
                    });
                }
            }, "mua");
            return;
        }
        final ItemTemplate t = ManNhanVat.mau(it);
        // so sánh với món đang mặc cùng ô
        int oSoSanh = -1;
        for (int i = 0; t != null && i < ManNhanVat.LOAI_O.length; i++) {
            if (ManNhanVat.hopLoai(i, t.type)) {
                oSoSanh = i;
                break;
            }
        }
        ItemInInventory cu = this.tab == 0 && oSoSanh >= 0 ? ManNhanVat.dangMac(oSoSanh) : null;
        yy = veSoSanh(g, it, cu, x + 10, yy, w - 20, yn - k);
        if (this.tab == 1) {
            final long gia = t == null ? 0 : t.price / 5 + t.plus * 1000;
            Bang.nut(g, x + 6, yn, w - 12, nh, "Bán", Bang.CHINH, new IAction() {
                public void perform() {
                    HopXacNhan.hoiGia("Bán vật phẩm", "Bán " + (t == null ? "món này" : t.name) + "? Không lấy lại được.", "Bán", TienTe.XU, gia, new IAction() {
                        public void perform() {
                            GameService.gI().sellItem(it.itemID);
                            ManCuaHang.this.chon = null;
                        }
                    });
                    HopXacNhan.gI().nguyHiem = true;
                }
            }, "ban");
            return;
        }
        final boolean thue = t != null && t.ndayLoan != 0;
        final int loai = thue ? TienTe.LUONG : TienTe.XU;
        final long gia = t == null ? 0 : t.price;
        Bang.nut(g, x + 6, yn, w - 12, nh, thue ? "Thuê" : "Mua", Bang.XAC_NHAN, new IAction() {
            public void perform() {
                if (mc().FullInventory() >= 42 * mc().limItemBag) {
                    DungDo.loi("Hành trang đã đầy.");
                    return;
                }
                if (!thue && gia > mc().charXu) {
                    DungDo.loi("Không đủ " + TienTe.ten(TienTe.XU) + ".");
                    return;
                }
                HopXacNhan.hoiGia(thue ? "Thuê vật phẩm" : "Mua vật phẩm", (thue ? "Thuê " : "Mua ") + (t == null ? "món này" : t.name) + "?",
                        thue ? "Thuê" : "Mua", loai, gia, new IAction() {
                    public void perform() {
                        Vector v = new Vector();
                        v.addElement(it);
                        GameService.gI().buyItem(v);
                    }
                });
            }
        }, "mua");
    }

    /** Bảng chỉ số món (so với món đang mặc: +xanh / -đỏ). Trả y sau bảng. */
    static int veSoSanh(Graphics g, ItemInInventory it, ItemInInventory cu, int x, int y, int w, int day) {
        Vector ids = new Vector();
        ManNhanVat.themId(ids, it);
        if (cu != null) {
            ManNhanVat.themId(ids, cu);
        }
        int lh = Font.arialFont.getHeight() + 3;
        if (ids.size() == 0) {
            Font.arialFont.drawString(g, it.isEnoughData ? "Không có chỉ số cộng thêm." : "Đang tải thông tin...", x, y, 0);
            return y + lh;
        }
        if (cu != null) {
            Font.arialFont.drawString(g, "So với món đang mặc:", x, y, 0);
            y += lh;
        }
        for (int i = 0; i < ids.size() && y + lh <= day; i++) {
            int id = ((Integer) ids.elementAt(i)).intValue();
            int moi = ManNhanVat.giaTri(it, id), cuGt = cu == null ? 0 : ManNhanVat.giaTri(cu, id);
            Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, ManNhanVat.tenThuocTinh(it, cu, id), w / 2), x, y, 0);
            int xv = x + w;
            if (cu != null && moi != cuGt) {
                int d = moi - cuGt;
                String sd = (d > 0 ? "+" : "") + d;
                int wd = Font.arialFont.getWidth(sd) + 8;
                GiaoDien.khoi(g, xv - wd, y - 1, wd, lh - 1, 4, d > 0 ? Bang.TANG : Bang.GIAM);
                Font.borderFont.drawString(g, sd, xv - wd / 2, y, Font.CENTER);
                xv -= wd + 6;
            }
            Font.blackFont.drawString(g, String.valueOf(moi), xv, y, 1);
            y += lh;
        }
        return y;
    }
}
