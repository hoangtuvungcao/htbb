package game.pokemon;

import game.model.Command;
import game.model.IAction;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * MÀN CŨ KIỂU CHUNG (31/10/2026, soi giao diện đợt 3: vòng quay, vé số, hộp may mắn, Cây thần, Cổ vật, đội, hướng dẫn,
 * kênh thế giới, top đội, thông báo, chọn mục tiêu... còn vẽ hộp J2ME cũ - chữ nhỏ, phím mềm, không vùng chạm trong màn).
 * Máy có con trỏ (MenuNguCanh.bat): màn cũ vẽ qua đây - tấm gỗ chung (Bang.khung) + X, nội dung (đoạn chữ / danh sách hàng
 * chạm được / lưới ô), hàng nút khối đủ cỡ chạm theo lệnh trái / giữa / phải của màn. Dữ liệu và việc của màn giữ nguyên;
 * J2ME bấm phím vẽ như cũ.
 */
public final class ManCu {
    private ManCu() {
    }

    static final Cuon CUON = new Cuon(), CUON_PHU = new Cuon();
    static Object chu;
    static String chuTieuDe;

    /** Máy cảm ứng / chuột: màn cũ vẽ kiểu chung. */
    public static boolean bat() {
        return MenuNguCanh.bat();
    }

    /** Một hàng danh sách. */
    public interface Dong {
        String chinh(int i);

        /** null = một dòng */
        String phu(int i);

        void cham(int i);
    }

    /**
     * Phủ nền + tấm đầy đủ (tiêu đề, X = dong) + hàng nút đáy (lenh: null bỏ qua; nút cuối = nút chính). Trả {x, y, w, h}
     * vùng nội dung còn lại. Đổi màn / tiêu đề = cuộn về đầu.
     */
    public static int[] tam(Graphics g, Object man, String tieuDe, final IAction dong, Command[] lenh) {
        if (man != chu || tieuDe != null && !tieuDe.equals(chuTieuDe)) {
            chu = man;
            chuTieuDe = tieuDe;
            CUON.den(0);
            CUON_PHU.den(0);
        }
        Bang.phuNen(g);
        int[] c = Bang.khung(g, tieuDe, null, 0, null, dong);
        int k = Bang.cach();
        int n = 0;
        for (int i = 0; lenh != null && i < lenh.length; i++) {
            if (coLenh(lenh[i])) {
                n++;
            }
        }
        if (n > 0) {
            int hb = Bang.caoHang();
            int yb = c[1] + c[3] - hb;
            int wn = Math.min((c[2] - (n - 1) * k) / n, Math.max(Bang.coO() * 3, 160));
            int x0 = c[0] + c[2] - (n * wn + (n - 1) * k);
            int j = 0;
            for (int i = 0; i < lenh.length; i++) {
                final Command cm = lenh[i];
                if (!coLenh(cm)) {
                    continue;
                }
                Bang.nut(g, x0 + j * (wn + k), yb, wn, hb, cm.caption, j == n - 1 ? Bang.XAC_NHAN : Bang.TRUNG, cm.action == null ? null : new IAction() {
                    public void perform() {
                        cm.action.perform();
                    }
                }, "cu-nut-" + j);
                j++;
            }
            c[3] -= hb + k;
        }
        return c;
    }

    static boolean coLenh(Command c) {
        return c != null && c.caption != null && c.caption.length() > 0 && c.action != null;
    }

    /** Lệnh đóng của màn cũ (lệnh phải) thành việc cho nút X. */
    public static IAction viec(final Command c) {
        if (c == null || c.action == null) {
            return null;
        }
        return new IAction() {
            public void perform() {
                c.action.perform();
            }
        };
    }

    /** Đoạn chữ cuộn (kieu[i] = 1: dòng tiêu đề đậm), ngắt theo bề ngang khung. */
    public static void vanBan(Graphics g, int[] c, String[] doan, int[] kieu) {
        vanBan(g, c, doan, kieu, CUON);
    }

    /** Đoạn chữ ở khung phụ (chi tiết cạnh danh sách) - cuộn riêng. */
    public static void vanBanPhu(Graphics g, int[] c, String[] doan, int[] kieu) {
        Bang.the(g, c[0], c[1], c[2], c[3]);
        vanBan(g, new int[]{c[0] + 2, c[1] + 4, c[2] - 4, c[3] - 8}, doan, kieu, CUON_PHU);
    }

    /** Chia vùng nội dung: màn rộng = trái (danh sách) / phải (chi tiết), màn hẹp = trên / dưới. */
    public static int[][] chia(int[] c, int phanTram) {
        int k = Bang.cach();
        if (c[2] >= 360 && c[2] * 3 >= c[3] * 4) {
            int wl = c[2] * phanTram / 100;
            return new int[][]{{c[0], c[1], wl, c[3]}, {c[0] + wl + k, c[1], c[2] - wl - k, c[3]}};
        }
        int hl = c[3] * phanTram / 100;
        return new int[][]{{c[0], c[1], c[2], hl}, {c[0], c[1] + hl + k, c[2], c[3] - hl - k}};
    }

    static void vanBan(Graphics g, int[] c, String[] doan, int[] kieu, Cuon cuon) {
        if (doan == null) {
            return;
        }
        int w = c[2] - 16;
        final java.util.Vector dong = new java.util.Vector();
        final java.util.Vector dam = new java.util.Vector();
        for (int i = 0; i < doan.length; i++) {
            boolean d = kieu != null && i < kieu.length && kieu[i] == 1;
            String[] l = ChuDe.ngat(doan[i], d ? ChuDe.NHAN : ChuDe.THUONG, w, 40);
            for (int j = 0; j < l.length; j++) {
                dong.addElement(l[j]);
                dam.addElement(d ? Boolean.TRUE : Boolean.FALSE);
            }
        }
        int lh = Math.max(ChuDe.cao(ChuDe.NHAN), ChuDe.cao(ChuDe.THUONG)) + 2;
        Bang.danhSach(g, cuon, c[0] + 2, c[1], c[2] - 4, c[3], dong.size(), lh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                boolean d = ((Boolean) dam.elementAt(i)).booleanValue();
                ChuDe.chu(g, (String) dong.elementAt(i), x + 6, y + 1, d ? ChuDe.NHAN : ChuDe.THUONG, d ? ChuDe.CHU : ChuDe.CHU_PHU, 0);
            }
        });
    }

    /** Danh sách hàng chạm được (cao >= cỡ chạm), chon = hàng đang chọn (phím); trong = chữ khi rỗng. */
    public static void danhSach(Graphics g, int[] c, int n, final int chon, final Dong d, String trong) {
        if (n <= 0) {
            Bang.trong(g, c[0], c[1], c[2], c[3], trong == null ? "Chưa có gì ở đây" : trong, null);
            return;
        }
        final int rh = ManXemTrangBi.caoHangMon();
        Bang.danhSach(g, CUON, c[0], c[1], c[2], c[3], n, rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final int ii = i;
                Bang.hang(g, x, y, w, h, i, i == chon && !Canvas.isPointerDown, new IAction() {
                    public void perform() {
                        d.cham(ii);
                    }
                }, "cu-hang-" + i);
                String a = d.chinh(i), b = d.phu(i);
                game.render.screen.Font f = game.render.screen.Font.blackFont, f2 = game.render.screen.Font.arialFont;
                if (b == null) {
                    ChuDe.chuGiua(g, ChuDe.cat(a, ChuDe.NHAN, w - 16), x + 8, y, h, ChuDe.NHAN, ChuDe.CHU, 0);
                } else {
                    GiaoDien.tenHaiDong(g, f, f2, a, b, x + 8, y + 2, w - 16, y + 3 + f.getHeight(), w - 16);
                }
            }
        });
    }

    /** Ô lưới (ô vuông cỡ chạm, chữ giữa; ve != null = màn tự vẽ trong ô). */
    public interface O {
        String chu(int i);

        void cham(int i);

        /** vẽ thêm trong ô (icon) - không cần thì để trống */
        void ve(Graphics g, int i, int x, int y, int s);
    }

    /** Lưới n ô vuông cạnh >= cỡ chạm, canh giữa, cuộn dọc khi dài; chon = ô đang chọn. */
    public static void luoi(Graphics g, int[] c, int n, final int chon, final O o, int sMuon) {
        int k = Bang.cach();
        int s = Math.max(Math.max(VungCham.MIN, 24), sMuon);
        final int cot = Math.max(1, (c[2] + k) / (s + k));
        final int sz = s;
        int hang = (n + cot - 1) / cot;
        final int x0 = c[0] + (c[2] - (cot * s + (cot - 1) * k)) / 2;
        final int n0 = n;
        final int kk = k;
        Bang.danhSach(g, CUON, c[0], c[1], c[2], c[3], hang, s, new Bang.Hang() {
            public void ve(Graphics g, int h, int x, int y, int w, int hh) {
                for (int j = 0; j < cot; j++) {
                    int i = h * cot + j;
                    if (i >= n0) {
                        break;
                    }
                    final int ii = i;
                    int xx = x0 + j * (sz + kk);
                    Bang.oDo(g, xx, y, sz, true, i == chon, new IAction() {
                        public void perform() {
                            o.cham(ii);
                        }
                    }, "cu-o-" + i);
                    o.ve(g, i, xx, y, sz);
                    String t = o.chu(i);
                    if (t != null) {
                        ChuDe.chuVienGiua(g, ChuDe.catVien(t, ChuDe.SO, 0xFFFFFF, sz - 4), xx + sz / 2, y, sz, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
                    }
                }
            }
        });
    }

    /** Màn cũ chạy logic phím của nó nhưng không xử lý chạm thô (vùng chạm kiểu chung đã xử lý). */
    public static void phimKhongCham(Runnable r) {
        boolean c = Canvas.isPointerClick, dn = Canvas.isPointerDown, rl = Canvas.isPointerRelease;
        Canvas.isPointerClick = false;
        Canvas.isPointerDown = false;
        Canvas.isPointerRelease = false;
        try {
            r.run();
        } finally {
            Canvas.isPointerClick = c;
            Canvas.isPointerDown = dn;
            Canvas.isPointerRelease = rl;
        }
    }

    /** Kiểm thử: giữ menu chữ cũ (giả lập máy J2ME bấm phím trên máy có con trỏ - ChupTrongGame menu_chu_*). */
    public static boolean giuMenuChu;

    /** Menu chữ cũ vừa mở trên màn cũ -> thẻ lệnh chung. */
    public static void tiepQuanMenu(String tieuDe) {
        if (!bat() || giuMenuChu || Canvas.menu == null || !Canvas.menu.showMenu || Canvas.currentDialog != null) {
            return;
        }
        java.util.Vector v = Canvas.menu.menuItems;
        if (v == null || v.size() == 0) {
            return;
        }
        Canvas.menu.showMenu = false;
        MenuNguCanh.mo(v, tieuDe);
    }
}
