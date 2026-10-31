package game.pokemon;

import game.model.Command;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.ItemInInventory;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * BÀN THỢ RÈN chung (31/10/2026, soi giao diện: "Nghiền Bột / Thêm dòng / Luyện đồ tự động" là ô vuông xanh trơn, danh
 * sách trắng, không nhãn, phím mềm góc tí hon, không vùng chạm). Vẽ lại màn cũ game.render.screen.ScreenDapDo (Cộng
 * thuộc tính, Luyện đồ tự động, Khóa đồ thú, Khóa trang bị, Thêm dòng) và ScreenItemTim (Nghiền bột) theo kiểu ManLuyen:
 * tấm gỗ + nút X; trái (màn rộng) / trên (màn dọc): hàng Ô CÓ NHÃN (chạm ô đã đặt = lấy ra, chạm ô trống = mở đúng danh
 * sách), dòng gợi ý / kết quả, nút Đóng + nút chính (xám kèm lý do khi chưa đủ); phải / dưới: chip "Trang bị | Đá" + danh
 * sách hàng có icon / tên / số còn, chạm = thẻ vật phẩm có nút "Đặt vào". Luật đặt / gói gửi giữ nguyên màn cũ (màn cũ
 * là Nguon). J2ME bấm phím: 2 / 8 chọn hàng, 4 / 6 đổi danh sách, 5 = Đặt vào, phím mềm trái = nút chính, phải = Đóng.
 */
public final class BanRen {
    /** Màn cũ cung cấp dữ liệu + luật. */
    public interface Nguon {
        String tieuDe();

        int soO();

        String nhanO(int i);

        /** ItemInInventory / GemTemplate / null (ô trống) */
        Object monO(int i);

        /** true = màn tự vẽ ô i (ô kết quả) */
        boolean veO(Graphics g, int i, int x, int y, int s);

        void layRa(int i);

        /** danh sách mở khi chạm ô trống i (0 / 1; -1 = không) */
        int tabO(int i);

        Vector ds(int tab);

        /** tên chip danh sách (null = không có danh sách đó) */
        String tenTab(int tab);

        /** null = đặt được */
        String khongDat(Object o);

        void dat(Object o);

        String goiY();

        /** null = bấm nút chính được */
        String khongLam();

        Command chinh();

        void dong();
    }

    static int tab;
    static int chon;
    static final Cuon ds = new Cuon();
    static int rhCuoi, hlCuoi;

    private BanRen() {
    }

    /** Màn rèn vừa mở: về danh sách đầu, hàng đầu. */
    public static void moi() {
        tab = 0;
        chon = 0;
        ds.den(0);
    }

    /** Máy có con trỏ: nút trong bảng, không vẽ chip phím mềm. */
    public static boolean cham() {
        return MenuNguCanh.bat();
    }

    /** Menu chọn (mốc luyện...): thẻ lệnh kiểu chung ở máy cảm ứng, menu chữ ở J2ME. */
    public static void moMenu(Vector lenh, String tieuDe) {
        if (cham()) {
            MenuNguCanh.mo(lenh, tieuDe);
        } else {
            Canvas.menu.startAt(lenh, 3);
        }
    }

    /** Báo lỗi ngắn (lý do không đặt / không làm). */
    public static void bao(String s) {
        if (cham()) {
            HopThongBao.mo(null, s, HopThongBao.LOI, new String[]{"OK"}, new int[]{Bang.XAC_NHAN}, new IAction[]{null}, null);
        } else {
            Canvas.startOKDlg(s);
        }
    }

    static int soTab(Nguon n) {
        return n.tenTab(1) == null ? 1 : 2;
    }

    static Vector dsHien(Nguon n) {
        Vector v = n.ds(tab);
        return v == null ? new Vector() : v;
    }

    // ------------------------------------------------------------------ vẽ

    public static void ve(Graphics g, final Nguon n) {
        Bang.phuNen(g);
        int[] c = Bang.khung(g, n.tieuDe(), null, 0, null, new IAction() {
            public void perform() {
                n.dong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int k = Bang.cach();
        if (tab >= soTab(n)) {
            tab = 0;
        }
        boolean ngang = w * 4 >= h * 5 && w >= 280;
        if (ngang) {
            int lw = Math.min(w * 46 / 100, Math.max(200, w / 2));
            lw = Math.max(lw, Math.min(n.soO() * VungCham.MIN + (n.soO() - 1) * Math.max(k, 12) + 16, w * 6 / 10));
            veBan(g, n, x, y, lw, h);
            veDanhSach(g, n, x + lw + k, y, w - lw - k, h);
        } else {
            int hb = Math.min(caoBan(n, w), h * 11 / 20);
            veBan(g, n, x, y, w, hb);
            veDanhSach(g, n, x, y + hb + k, w, h - hb - k);
        }
    }

    static int caoNhan() {
        return ChuDe.cao(ChuDe.PHU) + 2;
    }

    static int caoNut() {
        return cham() ? Bang.caoHang() : 0;
    }

    static int canhO(Nguon n, int w, int hCo) {
        int so = n.soO(), kk = Math.max(Bang.cach(), 12);
        int s = (w - 16 - (so - 1) * kk) / so;
        s = Math.min(s, Math.max(VungCham.MIN, Bang.coO() * 5 / 4));
        if (hCo > 0) {
            s = Math.min(s, hCo);
        }
        return Math.max(Math.max(VungCham.MIN, 22), s);
    }

    /** Cao bàn khi xếp dọc (bàn trên, danh sách dưới). */
    static int caoBan(Nguon n, int w) {
        int k = Bang.cach();
        int s = canhO(n, w, 0);
        int hg = 2 * ChuDe.cao(ChuDe.PHU);
        int hn = caoNut();
        return 6 + s + caoNhan() + k + hg + k + (hn > 0 ? hn + 6 : 0);
    }

    static void veBan(Graphics g, final Nguon n, int x, int y, int w, int h) {
        int k = Bang.cach();
        ManNhanVat.nenSanKhau(g, x, y, w, h);
        int hn = caoNut();
        int hl = caoNhan();
        int hg = ChuDe.cao(ChuDe.PHU);
        int so = n.soO(), kk = Math.max(k, 12);
        int hCo = h - 6 - hl - k - 2 * hg - k - (hn > 0 ? hn + 6 : 0);
        int s = canhO(n, w, hCo);
        // khe giữa các ô: chia phần ngang còn dư (nhãn dưới ô có chỗ, màn hẹp không cắt "Trang..")
        if (so > 1) {
            kk = Math.max(Math.min(kk, (w - 8 - so * s) / (so - 1)), Math.min(s * 3 / 4, (w - 16 - so * s) / (so - 1)));
        }
        int wHang = so * s + (so - 1) * kk;
        int xh = x + (w - wHang) / 2;
        // phần trên căn giữa theo chiều dọc trong chỗ trống (màn rộng: bàn cao hơn hàng ô)
        int than = s + hl + k + 2 * hg;
        int yy = y + 6 + Math.max(0, (h - 6 - (hn > 0 ? hn + 6 : 0) - than) / 3);
        for (int i = 0; i < so; i++) {
            final int oi = i;
            int xx = xh + i * (s + kk);
            Object m = n.monO(i);
            if (n.veO(g, i, xx, yy, s)) {
                // màn tự vẽ (ô kết quả)
            } else if (m != null) {
                LuoiTui.veO(g, m, xx, yy, s, false, new IAction() {
                    public void perform() {
                        n.layRa(oi);
                        AmThanh.phat(AmThanh.XAC_NHAN);
                    }
                }, "ren-o-" + i);
            } else {
                Bang.oDo(g, xx, yy, s, false, false, n.tabO(i) < 0 ? null : new IAction() {
                    public void perform() {
                        int t = n.tabO(oi);
                        if (t >= 0 && t < soTab(n)) {
                            tab = t;
                            chon = 0;
                            ds.den(0);
                        }
                    }
                }, "ren-o-" + i);
                int ic = Math.max(16, s / 2);
                BieuTuong.veBong(g, BieuTuong.DA, xx + (s - ic) / 2, yy + (s - ic) / 2, ic, 0x8A6A4A, ChuDe.NEN_PHU);
            }
            String nh = ChuDe.cat(n.nhanO(i), ChuDe.PHU, s + kk - 2);
            ChuDe.chu(g, nh, xx + s / 2, yy + s + 2, ChuDe.PHU, ChuDe.CHU, 2);
        }
        yy += s + hl + k;
        // nút chính: chưa đủ thì xám; lý do ghi trên nút nếu vừa, không thì dòng đỏ trên gợi ý
        final Command cm = n.chinh();
        String l = n.khongLam();
        boolean duoc = l == null && cm != null && cm.action != null && cm.caption != null && cm.caption.length() > 0;
        int wn = (w - 16 - k) / 2, wb = w - 16 - wn - k;
        String ten = cm != null && cm.caption != null && cm.caption.length() > 0 ? cm.caption : "Xin chờ";
        String chu, lyDo = null;
        if (duoc) {
            chu = cm.caption;
        } else if (l != null && hn > 0 && ChuDe.rong(l, ChuDe.SO) <= wb - 12) {
            chu = l;
        } else {
            chu = ten;
            lyDo = l;
        }
        String gy = n.goiY();
        int hConLai = y + h - (hn > 0 ? hn + 6 : 0) - yy - 2;
        int soDong = Math.max(1, Math.min(3, hConLai / Math.max(1, hg)));
        if (lyDo != null) {
            ChuDe.chu(g, ChuDe.cat(lyDo, ChuDe.PHU, w - 16), x + w / 2, yy, ChuDe.PHU, ChuDe.DO, 2);
            yy += hg;
            soDong--;
        }
        if (gy != null && gy.length() > 0 && soDong > 0) {
            String[] d = ChuDe.ngat(gy, ChuDe.PHU, w - 16, soDong);
            for (int i = 0; i < d.length && i < soDong; i++) {
                ChuDe.chu(g, d[i], x + w / 2, yy + i * hg, ChuDe.PHU, ChuDe.CHU_PHU, 2);
            }
        }
        if (hn <= 0) {
            return;
        }
        int yn = y + h - 6 - hn;
        Bang.nut(g, x + 8, yn, wn, hn, "Đóng", Bang.TRUNG, new IAction() {
            public void perform() {
                n.dong();
            }
        }, "ren-dong");
        Bang.nut(g, x + 8 + wn + k, yn, w - 16 - wn - k, hn, chu, duoc ? Bang.XAC_NHAN : Bang.TAT, duoc ? new IAction() {
            public void perform() {
                cm.action.perform();
            }
        } : null, "ren-lam");
    }

    static void veDanhSach(Graphics g, final Nguon n, int x, int y, int w, int h) {
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int yl;
        if (soTab(n) > 1) {
            Object[] vc = {new IAction() {
                public void perform() {
                    tab = 0;
                    chon = 0;
                    ds.den(0);
                }
            }, new IAction() {
                public void perform() {
                    tab = 1;
                    chon = 0;
                    ds.den(0);
                }
            }};
            yl = y + 4 + Bang.hangChip(g, x + 6, y + 4, w - 12, new String[]{n.tenTab(0), n.tenTab(1)}, tab, vc) + k;
        } else {
            ChuDe.chu(g, ChuDe.cat(n.tenTab(0), ChuDe.NHAN, w - 16), x + 8, y + 4, ChuDe.NHAN, ChuDe.CHU, 0);
            yl = y + 4 + ChuDe.cao(ChuDe.NHAN) + 6;
        }
        int hl = y + h - 4 - yl;
        final Vector v = dsHien(n);
        if (chon >= v.size()) {
            chon = Math.max(0, v.size() - 1);
        }
        if (v.size() == 0) {
            Bang.trong(g, x + 4, yl, w - 8, hl, tab == 0 ? "Không có " + n.tenTab(0).toLowerCase() : "Chưa có " + n.tenTab(1).toLowerCase(),
                    tab == 0 ? null : "Mua ở cửa hàng nguyên liệu");
            return;
        }
        final int rh = ManXemTrangBi.caoHangMon();
        rhCuoi = rh;
        hlCuoi = hl;
        final boolean phim = !cham();
        Bang.danhSach(g, ds, x + 4, yl, w - 8, hl, v.size(), rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final Object o = v.elementAt(i);
                boolean dat = daDat(n, o);
                Bang.hang(g, x, y, w, h, i, phim && i == chon, new IAction() {
                    public void perform() {
                        moThe(n, o);
                    }
                }, "ren-ds-" + i);
                if (o instanceof ItemInInventory) {
                    ManXemTrangBi.veHangMon(g, (ItemInInventory) o, x, y, w, h, dat ? "Đang đặt trên bàn" : null);
                } else if (o instanceof GemTemplate) {
                    GemTemplate gt = (GemTemplate) o;
                    int s = ManNhanVat.oDau(g, x, y, h);
                    GemTemp t = Res.getGemByID(gt.id);
                    if (t != null) {
                        IconVatPham.ve(g, (short) (t.idImage + 6500), x + 3 + s / 2, y + 3 + s / 2, s, IconVatPham.DP_NGOC);
                    }
                    game.render.screen.Font f = game.render.screen.Font.blackFont, f2 = game.render.screen.Font.arialFont;
                    f.drawString(g, GiaoDien.catChu(f, t == null ? "?" : t.name, w - s - 14), x + s + 9, y + 2, 0);
                    String phu = (dat ? "Đang đặt - " : "") + "Còn " + gt.number + (gt.lock ? " - khóa" : "");
                    f2.drawString(g, GiaoDien.catChu(f2, phu, w - s - 14), x + s + 9, y + 3 + f.getHeight(), 0);
                }
            }
        });
    }

    static boolean daDat(Nguon n, Object o) {
        for (int i = 0; i < n.soO(); i++) {
            Object m = n.monO(i);
            if (m == null) {
                continue;
            }
            if (m == o) {
                return true;
            }
            if (m instanceof GemTemplate && o instanceof GemTemplate) {
                GemTemplate a = (GemTemplate) m, b = (GemTemplate) o;
                if (a.id == b.id && a.lock == b.lock) {
                    return true;
                }
            } else if (m instanceof ItemInInventory && o instanceof ItemInInventory
                    && ((ItemInInventory) m).itemID == ((ItemInInventory) o).itemID) {
                return true;
            }
        }
        return false;
    }

    static void moThe(final Nguon n, final Object o) {
        if (!cham()) {
            datNgay(n, o);
            return;
        }
        if (TheVatPham.dangMo() || Canvas.currentDialog != null) {
            return;
        }
        TheVatPham.moMua(o, 0, TienTe.XU, 1, "Đặt vào", new TheVatPham.ViecMua() {
            public String khongMua(int so) {
                return n.khongDat(o);
            }

            public void mua(int so) {
                n.dat(o);
                AmThanh.phat(AmThanh.XAC_NHAN);
            }
        }, null);
    }

    static void datNgay(Nguon n, Object o) {
        String l = n.khongDat(o);
        if (l != null) {
            bao(l);
            return;
        }
        n.dat(o);
        AmThanh.phat(AmThanh.XAC_NHAN);
    }

    // ------------------------------------------------------------------ phím (J2ME / bàn phím PC)

    /** Màn cũ gọi trong updateKey: 2 / 8 hàng, 4 / 6 danh sách, 5 đặt vào. Trả true = đã dùng phím. */
    public static boolean phim(Nguon n) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;
        }
        Vector v = dsHien(n);
        boolean dung = false;
        if (Canvas.keyPressed[2]) {
            Canvas.keyPressed[2] = false;
            chon = chon <= 0 ? Math.max(0, v.size() - 1) : chon - 1;
            dung = true;
        } else if (Canvas.keyPressed[8]) {
            Canvas.keyPressed[8] = false;
            chon = chon >= v.size() - 1 ? 0 : chon + 1;
            dung = true;
        } else if (Canvas.keyPressed[4] || Canvas.keyPressed[6]) {
            Canvas.keyPressed[4] = false;
            Canvas.keyPressed[6] = false;
            if (soTab(n) > 1) {
                tab = 1 - tab;
                chon = 0;
                ds.den(0);
            }
            return true;
        } else if (Canvas.keyPressed[5]) {
            Canvas.keyPressed[5] = false;
            if (chon >= 0 && chon < v.size()) {
                datNgay(n, v.elementAt(chon));
            }
            return true;
        }
        if (dung && rhCuoi > 0) {
            int k = Bang.cach();
            int yr = chon * (rhCuoi + k);
            if (yr < ds.lech()) {
                ds.den(yr);
            } else if (yr + rhCuoi > ds.lech() + hlCuoi) {
                ds.den(yr + rhCuoi - hlCuoi);
            }
        }
        return dung;
    }

    /** Phím mềm giữa "Đặt vào" (J2ME): đặt hàng đang chọn. */
    public static void datChon(Nguon n) {
        Vector v = dsHien(n);
        if (chon >= 0 && chon < v.size()) {
            datNgay(n, v.elementAt(chon));
        }
    }

    /** Số lượng góc ô (như ô hành trang). */
    public static void soLuong(Graphics g, int x, int y, int s, int so) {
        LuoiTui.soLuong(g, x, y, s, so);
    }

    /** Hàng đang chọn bằng phím (kiểm thử). */
    public static int dangChon() {
        return chon;
    }

    public static int tabDangMo() {
        return tab;
    }
}
