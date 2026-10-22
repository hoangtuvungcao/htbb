package game.pokemon;

import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Dialog;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * THẺ THÔNG TIN VẬT PHẨM kiểu NRO (0.9.7, một thành phần cho PC / APK / J2ME có chạm): hộp gỗ giữa vùng an toàn - ô icon
 * lớn viền độ hiếm, TÊN màu độ hiếm, loại, cấp cần (đỏ nếu chưa đủ), dấu Đang mặc / Khoá, chỉ số (so với món đang
 * mặc: xanh tăng / đỏ giảm, dòng hiếm màu tím), mô tả, số lượng, giá bán; HÀNG NÚT ĐÁY theo ngữ cảnh:
 * - hành trang ({@link #TUI}): Dùng (vật dùng được) / Mặc (đồ mặc được) / Tháo (đang mặc), Vứt (xác nhận + chọn số với
 *   chồng; khoá / nhiệm vụ: nút xám kèm lý do), Đóng;
 * - cửa hàng tab Bán ({@link #BAN}): Bán (chọn số với chồng, tổng tiền kèm biểu tượng, hỏi lại với đồ hiếm; không bán
 *   được: lý do), Đóng.
 * Bố cục: nội dung trên, hàng nút DƯỚI nội dung (không chồng chữ), hộp cao theo nội dung, trong vùng an toàn.
 * Chọn số lượng = chế độ thứ hai của cùng hộp (−, thanh trượt, +, Tất cả, tổng tiền, nút xác nhận).
 */
public final class TheVatPham extends Dialog {
    public static final int TUI = 0, BAN = 1;

    static TheVatPham me;
    Object mon;
    int nguCanh;
    boolean dangMac;
    /** chế độ chọn số: 0 = không, 1 = vứt, 2 = bán */
    int chonSo;
    int so = 1;
    /** đóng thì gọi (màn bỏ chọn) */
    IAction khiDong;

    /** Khung vừa vẽ {x, y, w, h} (kiểm thử). */
    public static final int[] VUA_VE = new int[4];

    public static TheVatPham gI() {
        if (me == null) {
            me = new TheVatPham();
        }
        return me;
    }

    public static boolean dangMo() {
        return me != null && Canvas.currentDialog == me;
    }

    public static Object monDangXem() {
        return dangMo() ? me.mon : null;
    }

    /** Mở thẻ cho mục mon (đồ / bình / nguyên liệu / đặc biệt / nhiệm vụ). */
    public static void mo(Object mon, int nguCanh, boolean dangMac, IAction khiDong) {
        TheVatPham t = gI();
        t.mon = mon;
        t.nguCanh = nguCanh;
        t.dangMac = dangMac;
        t.chonSo = 0;
        t.so = 1;
        t.khiDong = khiDong;
        t.left = null;
        t.center = null;
        t.right = null;
        if (mon instanceof ItemInInventory) {
            ItemInInventory it = (ItemInInventory) mon;
            if (!it.isEnoughData && it.allAttribute.size() == 0) {
                it.isEnoughData = true;
                try {
                    GameService.gI().requestItemInfo(it.itemID, Canvas.gameScr.mainChar.ID);
                } catch (Throwable e) {
                    // chưa nối máy chủ
                }
            }
        }
        t.show();
        AmThanh.phat(AmThanh.MO);
    }

    void dong() {
        if (Canvas.currentDialog == this) {
            Canvas.currentDialog = null;
        }
        IAction a = this.khiDong;
        this.khiDong = null;
        if (a != null) {
            a.perform();
        }
    }

    static MainChar mc() {
        return Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
    }

    // ------------------------------------------------------------------ dữ liệu từng loại

    String ten() {
        Object o = this.mon;
        if (o instanceof ItemInInventory) {
            ItemInInventory it = (ItemInInventory) o;
            ItemTemplate t = ManNhanVat.mau(it);
            return (t == null ? "Vật phẩm" : t.name) + (it.plusTemplate > 0 ? " +" + it.plusTemplate : "");
        }
        if (o instanceof Integer) {
            return ManCuaHang.tenThuoc(((Integer) o).intValue());
        }
        if (o instanceof GemTemplate) {
            GemTemp t = gem(o);
            return t == null ? "Nguyên liệu" : t.name;
        }
        if (o instanceof int[]) {
            int id = ((int[]) o)[0];
            return id < game.model.ItemQuest.NAME_ITEM.length ? game.model.ItemQuest.NAME_ITEM[id] : "Vật phẩm nhiệm vụ";
        }
        return "";
    }

    static GemTemp gem(Object o) {
        GemTemplate g = (GemTemplate) o;
        return LuoiTui.laDacBiet(o) ? Res.getShopByID(g.id) : Res.getGemByID(g.id);
    }

    String loai() {
        Object o = this.mon;
        if (o instanceof ItemInInventory) {
            ItemTemplate t = ManNhanVat.mau((ItemInInventory) o);
            String l = t == null ? null : BangTrangBi.tenLoai(t.type);
            return l == null ? "Vật phẩm" : l;
        }
        if (o instanceof Integer) {
            return "Bình thuốc";
        }
        if (o instanceof GemTemplate) {
            return LuoiTui.laDacBiet(o) ? "Vật phẩm đặc biệt" : "Nguyên liệu";
        }
        return "Vật phẩm nhiệm vụ";
    }

    int soCo() {
        Object o = this.mon;
        if (o instanceof Integer) {
            MainChar m = mc();
            return m == null || m.potions == null ? 0 : m.potions[((Integer) o).intValue()];
        }
        if (o instanceof GemTemplate) {
            return ((GemTemplate) o).number;
        }
        if (o instanceof int[]) {
            return MainChar.itemQuest[((int[]) o)[0]];
        }
        if (o instanceof ItemInInventory) {
            return Math.max(1, ((ItemInInventory) o).number);
        }
        return 1;
    }

    /** Có dòng "Số lượng" (chồng / bình / nguyên liệu; đồ lẻ thì không). */
    boolean coSoLuong() {
        return !(this.mon instanceof ItemInInventory) || soCo() > 1;
    }

    String moTa() {
        Object o = this.mon;
        if (o instanceof GemTemplate) {
            GemTemp t = gem(o);
            return t == null ? null : t.decript;
        }
        if (o instanceof int[]) {
            return "Giao cho NPC nhiệm vụ khi đủ số lượng.";
        }
        if (o instanceof Integer) {
            int l = ((Integer) o).intValue();
            boolean pp = l >= 4 && l <= 6 || l == 23 || l == 24 || l == 95 || l == 96;
            return pp ? "Hồi PP cho Pokémon. Gán vào nút thuốc PP ở HUD để dùng nhanh." : "Hồi HP cho Pokémon. Gán vào nút thuốc HP ở HUD để dùng nhanh.";
        }
        return null;
    }

    /** Giá bán lại 1 chiếc (xu); -1 = không bán được. */
    long giaBan() {
        Object o = this.mon;
        long gia = -1;
        if (o instanceof ItemInInventory) {
            ItemTemplate t = ManNhanVat.mau((ItemInInventory) o);
            gia = t == null ? -1 : t.price / 5 + t.plus * 1000;
        } else if (o instanceof GemTemplate && !LuoiTui.laDacBiet(o)) {
            GemTemp t = gem(o);
            gia = t == null ? -1 : t.price / 5;
        }
        // 0.9.8: đồ khoá bán NPC ở giá thấp (máy chủ com.htbb.pokemon.BanDoKhoa: 15% giá NPC thường, quyết định cuối ở máy chủ)
        return gia > 0 && laKhoa() ? gia * PHAN_TRAM_KHOA / 100 : gia;
    }

    /** % giá NPC thường khi bán đồ khoá (khớp mặc định máy chủ chong_clone.ban_npc_khoa_phan_tram). */
    static final int PHAN_TRAM_KHOA = 15;

    /** Món đang xem là đồ / nguyên liệu khoá. */
    boolean laKhoa() {
        Object o = this.mon;
        return LuoiTui.laKhoa(o) || (o instanceof ItemInInventory && ((ItemInInventory) o).lock == 1);
    }

    /** Lý do không bán được (null = bán được). */
    String khongBan() {
        Object o = this.mon;
        if (o instanceof Integer) {
            return "Thuốc không bán cho NPC (giữ kinh tế: không tạo thêm xu).";
        }
        if (o instanceof int[]) {
            return "Vật phẩm nhiệm vụ không bán được.";
        }
        if (LuoiTui.laDacBiet(o)) {
            return "Vật phẩm đặc biệt không bán được.";
        }
        if (o instanceof GemTemplate) {
            GemTemp t = gem(o);
            if (t != null && !t.isSell) {
                return "Vật phẩm này không bán được.";
            }
            if (((GemTemplate) o).id == TrangThaiNguoiChoi.idVe) {
                return "Vé mở rộng không bán được - dùng để mở thêm ô hành trang.";
            }
        }
        if (this.dangMac) {
            return "Tháo ra trước khi bán.";
        }
        return giaBan() < 0 ? "Vật phẩm này không bán được." : null;
    }

    /** Lý do không vứt được (null = vứt được). */
    String khongVut() {
        Object o = this.mon;
        if (o instanceof int[]) {
            return "Vật phẩm nhiệm vụ không bỏ được.";
        }
        if (this.dangMac) {
            return laKhoa() ? "Tháo ra trước khi huỷ." : "Tháo ra trước khi bỏ.";
        }
        return null;
    }

    /** Dùng được (không phải đồ mặc). */
    boolean dungDuoc() {
        Object o = this.mon;
        if (o instanceof Integer) {
            return true;
        }
        if (o instanceof GemTemplate) {
            if (LuoiTui.laDacBiet(o)) {
                return true;
            }
            GemTemp t = gem(o);
            return t != null && t.type == GemTemp.TYPE_TU_BINH;
        }
        if (o instanceof ItemInInventory) {
            return !laDoMac((ItemInInventory) o);
        }
        return false;
    }

    static boolean laDoMac(ItemInInventory it) {
        ItemTemplate t = ManNhanVat.mau(it);
        for (int i = 0; t != null && i < ManNhanVat.LOAI_O.length; i++) {
            if (ManNhanVat.hopLoai(i, t.type)) {
                return true;
            }
        }
        return false;
    }

    /** Mặc được: đồ mặc, đúng dòng / giới tính, đủ cấp. Lý do (null = được). */
    static String khongMac(ItemInInventory it) {
        ItemTemplate t = ManNhanVat.mau(it);
        MainChar m = mc();
        if (t == null || m == null) {
            return "Đang tải thông tin...";
        }
        if (t.type >= 3 && t.type <= 7 && t.type != 3 + m.clazz) {
            return "Đồ cầm tay này dành cho dòng Pokémon khác.";
        }
        if (t.gender != 0 && m.gender != 0 && t.gender != m.gender) {
            return "Không hợp giới tính.";
        }
        if (ManNhanVat.capCan(it, t) > m.level) {
            return "Cần cấp " + ManNhanVat.capCan(it, t) + ".";
        }
        return null;
    }

    // ------------------------------------------------------------------ thao tác

    void dung() {
        Object o = this.mon;
        dong();
        if (o instanceof Integer) {
            try {
                Canvas.gameScr.doUsePotion(((Integer) o).intValue());
            } catch (Throwable e) {
                // chưa có bảng thuốc
            }
        } else if (o instanceof GemTemplate) {
            GemTemplate g = (GemTemplate) o;
            if (LuoiTui.laDacBiet(o)) {
                GameService.gI().doUseSpecialItem(g.rID);
            } else {
                GameService.gI().douseGemItem(g.id, (byte) (LuoiTui.laKhoa(o) ? 1 : 0));
            }
        } else if (o instanceof ItemInInventory) {
            DungDo.dungItem((ItemInInventory) o);
        }
    }

    void mac() {
        ItemInInventory it = (ItemInInventory) this.mon;
        String loi = khongMac(it);
        if (loi != null) {
            DungDo.loi(loi);
            return;
        }
        dong();
        DungDo.dungItem(it);
    }

    void thao() {
        ItemInInventory it = (ItemInInventory) this.mon;
        dong();
        DungDo.thao(it);
    }

    /** Vứt so chiếc (đã xác nhận). */
    void vut(int so) {
        Object o = this.mon;
        dong();
        if (o instanceof Integer) {
            GameService.gI().dellPotion(((Integer) o).intValue(), so >= soCoTinh(o) ? 0 : so);
        } else if (o instanceof GemTemplate) {
            GemTemplate g = (GemTemplate) o;
            boolean dacBiet = LuoiTui.laDacBiet(o);
            GameService.gI().dellGemItem(g.id, dacBiet ? 1 : 0, (byte) (LuoiTui.laKhoa(o) ? 1 : 0), so >= g.number ? 0 : so);
        } else if (o instanceof ItemInInventory) {
            GameService.gI().giveItemToGround(((ItemInInventory) o).itemID);
        }
        AmThanh.phat(AmThanh.XAC_NHAN);
    }

    static int soCoTinh(Object o) {
        MainChar m = mc();
        if (o instanceof Integer && m != null && m.potions != null) {
            return m.potions[((Integer) o).intValue()];
        }
        return 1;
    }

    /** Bán so chiếc (đã xác nhận). */
    void ban(int so) {
        Object o = this.mon;
        dong();
        if (o instanceof GemTemplate) {
            GemTemplate g = (GemTemplate) o;
            GameService.gI().sellGemItem(g.rID, (byte) (LuoiTui.laKhoa(o) ? 1 : 0), so >= g.number ? 0 : so);
        } else if (o instanceof ItemInInventory) {
            GameService.gI().sellItem(((ItemInInventory) o).itemID);
        }
    }

    /** Bấm "Vứt": chồng -> chọn số; chiếc lẻ -> hỏi lại. */
    void bamVut() {
        if (soCo() > 1 && !(this.mon instanceof ItemInInventory)) {
            this.chonSo = 1;
            this.so = soCo();
            return;
        }
        hoiVut(1);
    }

    void hoiVut(final int so) {
        final TheVatPham t = this;
        final boolean khoa = laKhoa();
        // 0.9.8: đồ khoá không ra đất (không ai nhặt) - "Huỷ" xoá hẳn, hỏi lại
        String noi = khoa ? "Huỷ " + (so > 1 ? so + " " : "") + ten() + "? Đồ khoá không bỏ ra đất được, sẽ mất vĩnh viễn."
                : (so > 1 ? "Bỏ " + so + " " : "Bỏ ") + ten() + " ra đất? Vật phẩm sẽ mất.";
        Object o = this.mon;
        final Object giu = o;
        HopXacNhan.hoiNguyHiem(khoa ? "Huỷ vật phẩm khoá" : "Bỏ vật phẩm", noi, khoa ? "Huỷ" : "Bỏ", new IAction() {
            public void perform() {
                t.mon = giu;
                t.vut(so);
            }
        });
    }

    void bamBan() {
        if (soCo() > 1 && this.mon instanceof GemTemplate) {
            this.chonSo = 2;
            this.so = soCo();
            return;
        }
        hoiBan(1);
    }

    void hoiBan(final int so) {
        final TheVatPham t = this;
        final Object giu = this.mon;
        long tong = Math.max(0, giaBan()) * so;
        boolean hiem = giu instanceof ItemInInventory && (((ItemInInventory) giu).colorName >= 2 || ((ItemInInventory) giu).plusTemplate > 0);
        HopXacNhan.hoiGia("Bán vật phẩm", "Bán " + (so > 1 ? so + " " : "") + ten() + "?" + (laKhoa() ? " Đồ khoá: bán cho NPC (giá thấp)." : "")
                + (hiem ? " Đồ hiếm - không lấy lại được." : " Không lấy lại được."),
                "Bán", TienTe.XU, tong, new IAction() {
            public void perform() {
                t.mon = giu;
                t.ban(so);
            }
        });
        if (hiem) {
            HopXacNhan.gI().nguyHiem = true;
        }
    }

    /** Thao tác nhanh (PC nháy đúp / chuột phải): mặc / tháo / dùng; ở cửa hàng: bán. Khác: mở thẻ. */
    public static void nhanh(Object mon, int nguCanh) {
        TheVatPham t = gI();
        t.mon = mon;
        t.nguCanh = nguCanh;
        t.dangMac = false;
        t.khiDong = null;
        if (nguCanh == BAN) {
            String l = t.khongBan();
            if (l != null) {
                DungDo.loi(l);
                return;
            }
            t.hoiBan(1);
            return;
        }
        if (mon instanceof ItemInInventory && laDoMac((ItemInInventory) mon)) {
            t.mac();
        } else if (t.dungDuoc()) {
            t.dung();
        } else {
            mo(mon, nguCanh, false, null);
        }
    }

    // ------------------------------------------------------------------ vẽ

    static final IAction[] KHONG = new IAction[0];

    public void paint(Graphics g) {
        if (this.mon == null) {
            dong();
            return;
        }
        Canvas.resetTrans(g);
        ChuDe.phuMan(g);
        VungCham.them(g, 0, 0, Canvas.w, Canvas.h, new IAction() {
            public void perform() {
                TheVatPham.this.dong();
            }
        }, "nen-ngoai");
        if (this.chonSo > 0) {
            veChonSo(g);
            return;
        }
        int k = Bang.cach();
        int pad = 10;
        int wMax = Math.min(Bang.phai() - Bang.trai(), ChuDe.dt() ? 420 : 340);
        int w = Math.max(Math.min(wMax, ChuDe.dt() ? 300 : 250), Math.min(wMax, ChuDe.rong(ten(), ChuDe.TIEU_DE) + 120));
        int wNoi = w - 2 * pad;
        int s = ChuDe.dt() ? 44 : 38;
        // chỉ số + mô tả
        Vector dong = new Vector();
        Vector mau = new Vector();
        chiSo(dong, mau, wNoi);
        String mt = moTa();
        String[] dmt = mt == null || mt.length() == 0 ? new String[0] : ChuDe.ngat(mt, ChuDe.PHU, wNoi, 4);
        int lh = ChuDe.cao(ChuDe.THUONG) + 2, lp = ChuDe.cao(ChuDe.PHU);
        int hHang = Bang.co(ChuDe.dt() ? 30 : 26);
        long gb = giaBan();
        boolean coGia = gb >= 0 && khongBan() == null;
        int hNoi = s + 6 + dong.size() * lh + (dmt.length > 0 ? dmt.length * lp + 6 : 0) + (coSoLuong() ? lh : 0) + (coGia ? lh : 0);
        int h = 8 + hNoi + k + hHang + 10 + ChuDe.cao(ChuDe.PHU) + 4;   // + dòng lý do (nút xám) trên hàng nút
        int H = Bang.duoi() - Bang.tren();
        if (h > H) {
            h = H;
        }
        int x = Bang.trai() + (Bang.phai() - Bang.trai() - w) / 2, y = Bang.tren() + (H - h) / 2;
        VUA_VE[0] = x;
        VUA_VE[1] = y;
        VUA_VE[2] = w;
        VUA_VE[3] = h;
        KhungNro.bong(g, x, y, w, h);
        if (!KhungNro.ve(g, KhungNroBang.HOP, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 4, ChuDe.VIEN);
            GiaoDien.khoi(g, x + 2, y + 2, w - 4, h - 4, 3, ChuDe.NEN);
        }
        game.render.screen.Font.nenSang = true;
        // chặn chạm trong hộp (không đóng)
        VungCham.them(g, x, y, w, h, IAction_KHONG, "the-vat-pham");
        // đầu: ô icon + tên màu độ hiếm + loại / cấp
        int xi = x + pad, yi = y + 8;
        int hiem = this.mon instanceof ItemInInventory ? KieuO.mauHiem(((ItemInInventory) this.mon).colorName) : -1;
        LuoiTui.veO(g, this.mon, xi, yi, s, hiem >= 3, null, null);
        int xt = xi + s + 8, wt = x + w - pad - xt - Bang.co(22);
        ChuDe.chuVien(g, ChuDe.cat(ten(), ChuDe.TIEU_DE, wt), xt, yi, ChuDe.TIEU_DE, hiem >= 0 ? hiem : 0xFFFFFF, ChuDe.VIEN, 0);
        int yl = yi + ChuDe.cao(ChuDe.TIEU_DE) + 1;
        String phu = loai();
        int capCan = -1;
        if (this.mon instanceof ItemInInventory) {
            ItemTemplate t = ManNhanVat.mau((ItemInInventory) this.mon);
            capCan = ManNhanVat.capCan((ItemInInventory) this.mon, t);
        }
        ChuDe.chu(g, phu, xt, yl, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        if (capCan > 0) {
            MainChar m = mc();
            boolean du = m == null || capCan <= m.level;
            ChuDe.chu(g, "Cấp " + capCan, xt + ChuDe.rong(phu, ChuDe.PHU) + 8, yl, ChuDe.PHU, du ? ChuDe.LA_TOI : ChuDe.DO, 0);
        }
        // dấu trạng thái
        int xd = xt, yd = yl + lp + 2;
        if (this.dangMac) {
            xd += dau(g, xd, yd, "Đang mặc", ChuDe.LA) + 4;
        }
        if (LuoiTui.laKhoa(this.mon) || (this.mon instanceof ItemInInventory && ((ItemInInventory) this.mon).lock == 1)) {
            xd += dau(g, xd, yd, "Khoá", 0x55555E) + 4;
        }
        if (this.mon instanceof GemTemplate && ((GemTemplate) this.mon).id == TrangThaiNguoiChoi.idVe) {
            dau(g, xd, yd, "+" + TrangThaiNguoiChoi.oMoiVe + " ô", ChuDe.CHINH);
        }
        // nút X
        Bang.nutDong(g, x + w - Bang.co(22) - 4, y + 4, Bang.co(22), new IAction() {
            public void perform() {
                TheVatPham.this.dong();
            }
        });
        // chỉ số
        int yy = yi + s + 6;
        int day = y + h - 10 - hHang - k;
        if (dong.size() > 0) {
            g.setColor(ChuDe.KE);
            g.fillRect(x + pad, yy - 3, wNoi, 1);
        }
        for (int i = 0; i < dong.size() && yy + lh <= day; i++) {
            String[] d = (String[]) dong.elementAt(i);
            int m = ((Integer) mau.elementAt(i)).intValue();
            ChuDe.chu(g, ChuDe.cat(d[0], ChuDe.THUONG, wNoi * 6 / 10), x + pad, yy, ChuDe.THUONG, m == 0 ? ChuDe.CHU : m, 0);
            int xv = x + w - pad;
            if (d.length > 2 && d[2] != null) {
                int wd = ChuDe.rong(d[2], ChuDe.SO) + 8;
                GiaoDien.khoiPixel(g, xv - wd, yy, wd, lh - 2, d[2].startsWith("+") ? Bang.TANG : Bang.GIAM);
                ChuDe.chuVienGiua(g, d[2], xv - wd / 2, yy, lh - 2, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
                xv -= wd + 6;
            }
            ChuDe.chu(g, d[1], xv, yy, ChuDe.SO, ChuDe.CHU, 1);
            yy += lh;
        }
        for (int i = 0; i < dmt.length && yy + lp <= day; i++) {
            ChuDe.chu(g, dmt[i], x + pad, yy + (i == 0 ? 3 : 0), ChuDe.PHU, ChuDe.CHU_PHU, 0);
            yy += lp;
        }
        if (dmt.length > 0) {
            yy += 6;
        }
        // số lượng + giá bán
        if (coSoLuong() && yy + lh <= day) {
            ChuDe.chu(g, "Số lượng: " + soCo(), x + pad, yy, ChuDe.THUONG, ChuDe.CHU, 0);
            yy += lh;
        }
        if (coGia && yy + lh <= day) {
            ChuDe.chu(g, "Giá bán:", x + pad, yy, ChuDe.THUONG, ChuDe.CHU, 0);
            int xg = x + pad + ChuDe.rong("Giá bán:", ChuDe.THUONG) + 6;
            int co = Math.min(12, lh - 2);
            TienTe.ve(g, TienTe.XU, xg, yy + (lh - co) / 2, co);
            ChuDe.chu(g, TienTe.so(gb), xg + co + 3, yy, ChuDe.SO, ChuDe.CHU, 0);
        }
        // hàng nút đáy
        veNut(g, x + pad, y + h - 8 - hHang, wNoi, hHang);
    }

    static final IAction IAction_KHONG = new IAction() {
        public void perform() {
        }
    };

    /** Dấu nhỏ (khối pixel màu + chữ trắng viền); trả bề ngang. */
    static int dau(Graphics g, int x, int y, String chu, int mau) {
        int w = ChuDe.rong(chu, ChuDe.NHAN_NHO) + 8, h = ChuDe.cao(ChuDe.NHAN_NHO) + 2;
        GiaoDien.khoiPixel(g, x, y, w, h, mau);
        ChuDe.chuVienGiua(g, chu, x + w / 2, y, h, ChuDe.NHAN_NHO, 0xFFFFFF, ChuDe.VIEN, 2);
        return w;
    }

    /** Dòng chỉ số {tên, giá trị, chênh lệch|null} + màu (0 thường, tím = dòng hiếm). */
    void chiSo(Vector dong, Vector mau, int w) {
        if (!(this.mon instanceof ItemInInventory)) {
            return;
        }
        ItemInInventory it = (ItemInInventory) this.mon;
        if (it.allAttribute.size() == 0) {
            dong.addElement(new String[]{it.isEnoughData ? "Không có chỉ số cộng thêm." : "Đang tải thông tin...", ""});
            mau.addElement(new Integer(ChuDe.CHU_PHU));
            return;
        }
        ItemInInventory cu = null;
        if (!this.dangMac) {
            ItemTemplate t = ManNhanVat.mau(it);
            for (int i = 0; t != null && i < ManNhanVat.LOAI_O.length && cu == null; i++) {
                if (ManNhanVat.hopLoai(i, t.type)) {
                    cu = ManNhanVat.dangMac(i);
                }
            }
        }
        Vector ids = new Vector();
        ManNhanVat.themId(ids, it);
        for (int i = 0; i < ids.size(); i++) {
            int id = ((Integer) ids.elementAt(i)).intValue();
            int moi = ManNhanVat.giaTri(it, id);
            String d = null;
            if (cu != null && cu != it) {
                int c = ManNhanVat.giaTri(cu, id);
                if (c != moi) {
                    d = (moi > c ? "+" : "") + (moi - c);
                }
            }
            dong.addElement(new String[]{ManNhanVat.tenThuocTinh(it, cu, id), String.valueOf(moi), d});
            // dòng thứ 3 trở đi của đồ hiếm (tím / vàng / đỏ) = dòng hiếm
            mau.addElement(new Integer(i >= 2 && it.colorName >= 3 ? 0x8A3AD0 : 0));
        }
    }

    /** Hàng nút đáy theo ngữ cảnh. */
    void veNut(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        String[] chu = new String[3];
        int[] kieu = new int[3];
        IAction[] viec = new IAction[3];
        String[] ten = new String[3];
        int n = 0;
        final TheVatPham t = this;
        if (this.nguCanh == BAN) {
            String l = khongBan();
            chu[n] = l == null ? (laKhoa() ? "Bán (giá thấp)" : "Bán") : "Không bán được";
            kieu[n] = l == null ? Bang.CHINH : Bang.TAT;
            viec[n] = l == null ? new IAction() {
                public void perform() {
                    t.bamBan();
                }
            } : null;
            ten[n++] = "tvp-ban";
            if (l != null) {
                lyDo = l;
            } else if (laKhoa()) {
                goiY = "Bán cho NPC (giá thấp)";
            }
        } else {
            if (this.dangMac) {
                chu[n] = "Tháo";
                kieu[n] = Bang.PHU;
                viec[n] = new IAction() {
                    public void perform() {
                        t.thao();
                    }
                };
                ten[n++] = "tvp-thao";
            } else if (this.mon instanceof ItemInInventory && laDoMac((ItemInInventory) this.mon)) {
                String l = khongMac((ItemInInventory) this.mon);
                chu[n] = l == null ? "Mặc" : "Chưa mặc được";
                kieu[n] = l == null ? Bang.XAC_NHAN : Bang.TAT;
                viec[n] = l == null ? new IAction() {
                    public void perform() {
                        t.mac();
                    }
                } : null;
                ten[n++] = "tvp-mac";
                if (l != null) {
                    lyDo = l;
                }
            } else if (dungDuoc()) {
                chu[n] = "Dùng";
                kieu[n] = Bang.XAC_NHAN;
                viec[n] = new IAction() {
                    public void perform() {
                        t.dung();
                    }
                };
                ten[n++] = "tvp-dung";
            }
            String lv = khongVut();
            boolean huy = laKhoa();
            chu[n] = huy ? "Huỷ" : "Vứt";
            kieu[n] = lv == null ? Bang.NGUY : Bang.TAT;
            viec[n] = lv == null ? new IAction() {
                public void perform() {
                    t.bamVut();
                }
            } : null;
            ten[n++] = huy ? "tvp-huy" : "tvp-vut";
            if (huy && lv == null && (this.mon instanceof ItemInInventory || this.mon instanceof GemTemplate)) {
                goiY = "Khoá: không giao dịch. Bán cho NPC (giá thấp) ở cửa hàng";
            }
            if (lv != null && n == 1) {
                lyDo = lv;
            }
        }
        chu[n] = "Đóng";
        kieu[n] = Bang.TRUNG;
        viec[n] = new IAction() {
            public void perform() {
                t.dong();
            }
        };
        ten[n++] = "tvp-dong";
        int wn = (w - (n - 1) * k) / n;
        for (int i = 0; i < n; i++) {
            Bang.nut(g, x + i * (wn + k), y, i == n - 1 ? x + w - (x + i * (wn + k)) : wn, h, chu[i], kieu[i], viec[i], ten[i]);
        }
        // lý do (nút xám) ngay trên hàng nút
        if (lyDo != null) {
            ChuDe.chu(g, ChuDe.cat(lyDo, ChuDe.PHU, w), x, y - ChuDe.cao(ChuDe.PHU) - 2, ChuDe.PHU, ChuDe.DO, 0);
            lyDo = null;
            goiY = null;
        } else if (goiY != null) {
            ChuDe.chu(g, ChuDe.cat(goiY, ChuDe.PHU, w), x, y - ChuDe.cao(ChuDe.PHU) - 2, ChuDe.PHU, ChuDe.CHU_PHU, 0);
            goiY = null;
        }
    }

    private String lyDo;
    /** gợi ý (không phải lỗi) ngay trên hàng nút: đồ khoá bán NPC giá thấp. */
    private String goiY;

    /** Chế độ chọn số lượng (vứt / bán chồng). */
    void veChonSo(Graphics g) {
        final TheVatPham t = this;
        int k = Bang.cach();
        int max = Math.max(1, soCo());
        if (this.so > max) {
            this.so = max;
        }
        if (this.so < 1) {
            this.so = 1;
        }
        int pad = 10;
        int w = Math.min(Bang.phai() - Bang.trai(), ChuDe.dt() ? 380 : 300);
        int hn = Bang.co(ChuDe.dt() ? 30 : 26);
        int lh = ChuDe.cao(ChuDe.THUONG) + 4;
        int h = 8 + ChuDe.cao(ChuDe.TIEU_DE) + 8 + lh + hn + k + hn + k + (this.chonSo == 2 ? lh : 0) + hn + 12;
        int x = Bang.trai() + (Bang.phai() - Bang.trai() - w) / 2, y = Bang.tren() + (Bang.duoi() - Bang.tren() - h) / 2;
        VUA_VE[0] = x;
        VUA_VE[1] = y;
        VUA_VE[2] = w;
        VUA_VE[3] = h;
        KhungNro.bong(g, x, y, w, h);
        if (!KhungNro.ve(g, KhungNroBang.HOP, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 4, ChuDe.NEN);
        }
        VungCham.them(g, x, y, w, h, IAction_KHONG, "the-vat-pham");
        String td = this.chonSo == 1 ? (laKhoa() ? "Huỷ bao nhiêu?" : "Bỏ bao nhiêu?") : "Bán bao nhiêu?";
        int hb = ChuDe.cao(ChuDe.TIEU_DE) + 6;
        int wb = Math.min(w - 20, ChuDe.rong(td, ChuDe.TIEU_DE) + 40);
        ChuDe.bien(g, x + (w - wb) / 2, y + 6, wb, hb);
        ChuDe.chuVienGiua(g, td, x + w / 2, y + 6, hb, ChuDe.TIEU_DE, 0xFFFFFF, ChuDe.VIEN, 2);
        int yy = y + 6 + hb + 6;
        ChuDe.chu(g, ChuDe.cat(ten() + " (có " + max + ")", ChuDe.THUONG, w - 2 * pad), x + w / 2, yy, ChuDe.THUONG, ChuDe.CHU, 2);
        yy += lh;
        // − [số] + Tất cả
        int sb = hn, wTat = ChuDe.rong("Tất cả", ChuDe.SO) + 20;
        int wo = w - 2 * pad - 2 * sb - wTat - 3 * k;
        Bang.nut(g, x + pad, yy, sb, sb, "-", Bang.PHU, new IAction() {
            public void perform() {
                t.so = Math.max(1, t.so - 1);
            }
        }, "sl-tru");
        int xo = x + pad + sb + k;
        if (!KhungNro.ve(g, KhungNroBang.THE, xo, yy, wo, sb)) {
            GiaoDien.khoi(g, xo, yy, wo, sb, 3, ChuDe.NEN_DUOI);
        }
        ChuDe.chuGiua(g, String.valueOf(this.so), xo + wo / 2, yy, sb, ChuDe.TIEU_DE, ChuDe.CHU, 2);
        Bang.nut(g, xo + wo + k, yy, sb, sb, "+", Bang.PHU, new IAction() {
            public void perform() {
                t.so = Math.min(Math.max(1, t.soCo()), t.so + 1);
            }
        }, "sl-cong");
        final int fmax = max;
        Bang.nut(g, xo + wo + sb + 2 * k, yy, x + w - pad - (xo + wo + sb + 2 * k), sb, "Tất cả", Bang.TRUNG, new IAction() {
            public void perform() {
                t.so = fmax;
            }
        }, "sl-max");
        yy += sb + k;
        // giá trị kéo áp ngay mỗi khung (thanhTruot trả giá trị đang hiện)
        this.so = Bang.thanhTruot(g, x + pad, yy, w - 2 * pad, hn, this.so, 1, max, 1, max <= 1, new IAction() {
            public void perform() {
                t.so = Bang.giaTriTha();
            }
        }, "sl-truot");
        yy += hn + k;
        if (this.chonSo == 2) {
            long tong = Math.max(0, giaBan()) * this.so;
            String c = "Tổng:";
            int co = Math.min(12, lh - 2);
            int wc = ChuDe.rong(c, ChuDe.THUONG) + 6 + co + 3 + ChuDe.rong(TienTe.so(tong), ChuDe.SO);
            int xc = x + (w - wc) / 2;
            ChuDe.chu(g, c, xc, yy, ChuDe.THUONG, ChuDe.CHU, 0);
            xc += ChuDe.rong(c, ChuDe.THUONG) + 6;
            TienTe.ve(g, TienTe.XU, xc, yy + (lh - co) / 2, co);
            ChuDe.chu(g, TienTe.so(tong), xc + co + 3, yy, ChuDe.SO, ChuDe.CHU, 0);
            yy += lh;
        }
        final int sl = this.so;
        int w2 = (w - 2 * pad - k) / 2;
        Bang.nut(g, x + pad, y + h - 8 - hn, w2, hn, this.chonSo == 1 && laKhoa() ? "Thôi" : "Huỷ", Bang.TRUNG, new IAction() {
            public void perform() {
                t.chonSo = 0;
            }
        }, "sl-huy");
        final boolean laBan = this.chonSo == 2;
        Bang.nut(g, x + pad + w2 + k, y + h - 8 - hn, w - 2 * pad - w2 - k, hn, laBan ? "Bán " + sl : (laKhoa() ? "Huỷ " : "Bỏ ") + sl, laBan ? Bang.CHINH : Bang.NGUY,
                new IAction() {
            public void perform() {
                t.chonSo = 0;
                if (laBan) {
                    t.hoiBan(sl);
                } else {
                    t.hoiVut(sl);
                }
            }
        }, "sl-ok");
    }

    public void keyPress(int keyCode) {
        if (keyCode == -7 || keyCode == -6 || keyCode == 27) {
            if (this.chonSo > 0) {
                this.chonSo = 0;
            } else {
                dong();
            }
        } else if (this.chonSo > 0 && (keyCode == -3 || keyCode == 52)) {
            this.so = Math.max(1, this.so - 1);
        } else if (this.chonSo > 0 && (keyCode == -4 || keyCode == 54)) {
            this.so = Math.min(Math.max(1, soCo()), this.so + 1);
        }
    }

    public void update() {
    }
}
