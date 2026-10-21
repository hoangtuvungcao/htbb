package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import game.render.screen.Font;
import java.io.DataInputStream;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * 0.9.8 KHẢM NGỌC HỆ (Machamp thợ rèn, gói -120 kiểu 4 - real.BoNgocGame): tab "Trang bị" = món có lỗ ngọc (đang mặc +
 * hành trang) với lỗ tô màu hệ; chạm một món = thẻ chi tiết: từng lỗ (Nâng / Tháo / Mài kèm giá xu, hoặc lỗ trống: Khảm
 * ngọc mới cấp 1 theo hệ đang chọn / lấy viên trong túi), chip chọn hệ, lời giải thích lợi + hại. Tab "Túi ngọc" = ngọc
 * đã tháo (giữ cấp) + Phân rã ra Đá nâng cấp. Mọi giá / điều kiện do máy chủ tính, client chỉ hiện + gửi yêu cầu.
 */
public final class ManKhamNgoc extends ManBang {
    public static ManKhamNgoc me;

    /** Một món: id, idTemplate, idIcon, màu bậc hiếm, +k, đang mặc, lỗ {he -1..17, cap, mai}. */
    static final class Mon {
        int id, idTemplate, idIcon, mau, plus;
        boolean dangMac;
        String ten;
        int[] he, cap;
        boolean[] mai;
    }

    static Vector mon = new Vector();
    /** {he, cap, mai(0/1), so} */
    static Vector tui = new Vector();
    static int[] gia = new int[11];
    static int giaThao, giaMai;
    static int[] heKhac = new int[18];
    static boolean coDuLieu;
    static boolean cho;
    static long choLuc;
    int chon = -1;
    int heChon;
    int tuiChon = -1;

    public static ManKhamNgoc gI() {
        if (me == null) {
            me = new ManKhamNgoc();
        }
        return me;
    }

    static void doc(DataInputStream r) throws java.io.IOException {
        int n = r.readUnsignedByte();
        Vector v = new Vector();
        for (int i = 0; i < n; i++) {
            Mon m = new Mon();
            m.id = r.readShort();
            m.idTemplate = r.readShort();
            m.idIcon = r.readShort();
            m.ten = r.readUTF();
            m.mau = r.readByte();
            m.plus = r.readByte();
            m.dangMac = r.readByte() != 0;
            int lo = r.readUnsignedByte();
            m.he = new int[lo];
            m.cap = new int[lo];
            m.mai = new boolean[lo];
            for (int j = 0; j < lo; j++) {
                m.he[j] = r.readByte();
                m.cap[j] = r.readUnsignedByte();
                m.mai[j] = r.readByte() != 0;
            }
            v.addElement(m);
        }
        int nt = r.readUnsignedByte();
        Vector t = new Vector();
        for (int i = 0; i < nt; i++) {
            t.addElement(new int[]{r.readByte(), r.readUnsignedByte(), r.readUnsignedByte(), r.readShort()});
        }
        int ng = r.readUnsignedByte();
        int[] g = new int[11];
        for (int c = 1; c <= ng; c++) {
            int x = r.readInt();
            if (c < g.length) {
                g[c] = x;
            }
        }
        int th = r.readInt();
        int ma = r.readInt();
        int nk = r.readUnsignedByte();
        int[] hk = new int[18];
        for (int i = 0; i < nk; i++) {
            int x = r.readByte();
            if (i < hk.length) {
                hk[i] = x;
            }
        }
        datDuLieu(v, t, g, th, ma, hk);
        cho = false;
        if (Canvas.currentScreen != gI()) {
            gI().chon = -1;
            gI().moBang();
        }
    }

    static void datDuLieu(Vector v, Vector t, int[] g, int th, int ma, int[] hk) {
        // giữ món đang chọn theo id sau khi làm mới
        int idCu = -1;
        if (me != null && me.chon >= 0 && me.chon < mon.size()) {
            idCu = ((Mon) mon.elementAt(me.chon)).id;
        }
        mon = v;
        tui = t;
        gia = g;
        giaThao = th;
        giaMai = ma;
        heKhac = hk;
        coDuLieu = true;
        if (me != null) {
            me.chon = -1;
            for (int i = 0; idCu >= 0 && i < v.size(); i++) {
                if (((Mon) v.elementAt(i)).id == idCu) {
                    me.chon = i;
                }
            }
            if (me.tuiChon >= tui.size()) {
                me.tuiChon = -1;
            }
        }
    }

    protected String tieuDe() {
        return "Khảm ngọc hệ";
    }

    protected String[] tabs() {
        return new String[]{"Trang bị", "Túi ngọc (" + tongTui() + ")"};
    }

    static int tongTui() {
        int n = 0;
        for (int i = 0; i < tui.size(); i++) {
            n += ((int[]) tui.elementAt(i))[3];
        }
        return n;
    }

    protected int rongMuon() {
        return ChuDe.dt() ? 600 : 520;
    }

    protected void lui() {
        if (this.chon >= 0) {
            this.chon = -1;
        } else {
            dong();
        }
    }

    static boolean coTheGui() {
        if (cho && System.currentTimeMillis() - choLuc < 5000) {
            return false;
        }
        cho = true;
        choLuc = System.currentTimeMillis();
        return true;
    }

    static void gui(int kieu, int[] so, int[] co) {
        if (coTheGui()) {
            TrangBiThem.gui(kieu, so, co);
        }
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        if (!coDuLieu) {
            Bang.trong(g, x, y, w, h, "Đang tải...", null);
            return;
        }
        if (this.tab == 1) {
            veTui(g, x, y, w, h);
            return;
        }
        if (this.chon >= 0 && this.chon < mon.size()) {
            veChiTiet(g, x, y, w, h, (Mon) mon.elementAt(this.chon));
            return;
        }
        final Vector v = mon;
        if (v.size() == 0) {
            Bang.trong(g, x, y, w, h, "Chưa có trang bị có lỗ ngọc", "Trang bị Thường / Hiếm có 1 lỗ, Quý / Sử thi 2 lỗ, Huyền thoại 3 lỗ.");
            return;
        }
        int k = Bang.cach();
        int yy = y + 2;
        yy = Bang.doan(g, ChuDe.PHU, "Mỗi cấp ngọc: +1% chỉ số món (tối đa 10% mỗi món), +1,5‰ sát thương hệ đó; đổi lại −1‰ kháng hệ khắc. Chạm một món để khảm.",
                x + 4, yy, w - 8, false, ChuDe.CHU_PHU) + k;
        Bang.danhSach(g, this.ds, x, yy, w, y + h - 2 - yy, v.size(), Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, final int i, int x, int y, int w, int h) {
                Mon m = (Mon) v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, false, new IAction() {
                    public void perform() {
                        ManKhamNgoc.this.chon = i;
                        ManKhamNgoc.this.tuiChon = -1;
                    }
                }, "mon-" + i);
                veDauMon(g, m, x, y, w, h);
            }
        });
    }

    /** Ảnh + tên + đang mặc + lỗ (đĩa màu hệ) trên một hàng. */
    static void veDauMon(Graphics g, Mon m, int x, int y, int w, int h) {
        int s = h - 6;
        GiaoDien.khoi(g, x + 3, y + 3, s, s, ChuDe.BO_O, ChuDe.NEN_PHU);
        int hiem = KieuO.mauHiem(m.mau);
        if (hiem >= 0) {
            KieuO.khungHiem(g, x + 3, y + 3, s, hiem);
        }
        if (m.idIcon >= 0) {
            Bang.icon(g, (short) m.idIcon, x + 3 + s / 2, y + 3 + s / 2, s >= 26 ? s / 22f : 1f);
        }
        int r = Math.max(5, (h - 10) / 4);
        int wl = m.he.length * (2 * r + 4);
        String ten = m.ten + (m.plus > 0 ? " +" + m.plus : "");
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, ten, w - s - 20 - wl), x + s + 9, y + 2, 0);
        Font.arialFont.drawString(g, m.dangMac ? "Đang mặc" : "Trong hành trang", x + s + 9, y + 3 + Font.blackFont.getHeight(), 0);
        for (int j = 0; j < m.he.length; j++) {
            int cx = x + w - 8 - wl + j * (2 * r + 4) + r, cy = y + h / 2;
            if (m.he[j] < 0) {
                GiaoDien.khoi(g, cx - r, cy - r, 2 * r, 2 * r, r, ChuDe.NEN_PHU_TOI);
                GiaoDien.khoi(g, cx - r + 2, cy - r + 2, 2 * r - 4, 2 * r - 4, r - 2, ChuDe.NEN);
            } else {
                GiaoDien.khoiDoc(g, cx - r, cy - r, 2 * r, 2 * r, r, GiaoDien.pha(KieuO.mauHe(m.he[j]), 0xFFFFFF, 2, 5), KieuO.mauHe(m.he[j]));
                ChuDe.chuGiua(g, String.valueOf(m.cap[j]), cx, cy - r, 2 * r, ChuDe.NHAN_NHO, 0xFFFFFF, 2);
            }
        }
    }

    static int wc0(int w, int k, int cot) {
        return (w - 12 - (cot - 1) * k) / cot;
    }

    void veChiTiet(Graphics g, int x, int y, int w, int h, final Mon m) {
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int hd = Bang.co(26);
        Bang.nutLui(g, x + 4, y + 4, hd, new IAction() {
            public void perform() {
                ManKhamNgoc.this.chon = -1;
            }
        });
        int rh = Bang.caoHang();
        veDauMon(g, m, x + hd + 8, y + 2, w - hd - 12, rh);
        int yy = y + 4 + Math.max(hd, rh) + k;
        int hn = Bang.co(26);
        int hLo = Math.max(rh, ChuDe.cao(ChuDe.NHAN) + hn + 10);
        for (int j = 0; j < m.he.length; j++) {
            veLo(g, m, j, x + 6, yy, w - 12, hLo);
            yy += hLo + k;
        }
        boolean coTrong = false;
        for (int j = 0; j < m.he.length; j++) {
            coTrong |= m.he[j] < 0;
        }
        if (!coTrong) {
            Bang.doan(g, ChuDe.PHU, "Mọi lỗ đã có ngọc. Tháo một viên (giữ cấp, vào túi ngọc) để thay hệ khác.", x + 8, yy, w - 16, false, ChuDe.CHU_PHU);
            return;
        }
        // chọn hệ cho ngọc mới (lỗ trống)
        ChuDe.chu(g, "Hệ ngọc mới: " + KieuO.tenHe(this.heChon) + " (−kháng " + KieuO.tenHe(heKhac[this.heChon]) + ")", x + 8, yy, ChuDe.PHU, ChuDe.CHU, 0);
        yy += ChuDe.cao(ChuDe.PHU) + 3;
        int hc = Bang.co(22);
        int cot = Math.max(1, (w - 12 + k) / (Bang.co(60) + k));
        int hLuoi = ((KieuO.TEN_HE.length + cot - 1) / cot) * (hc + k);
        if (yy + hLuoi > y + h - 4) {
            // màn thấp (điện thoại ngang): một hàng chọn hệ ‹ Hệ › thay cho lưới 18 chip
            int hb = Math.max(hc, Bang.co(26));
            int wb = Math.max(hb, Bang.co(44));
            Bang.nut(g, x + 6, yy, wb, hb, "Trước", Bang.PHU, new IAction() {
                public void perform() {
                    ManKhamNgoc.this.heChon = (ManKhamNgoc.this.heChon + 17) % 18;
                    ManKhamNgoc.this.tuiChon = -1;
                }
            }, "he-truoc");
            int mh = KieuO.mauHe(this.heChon);
            GiaoDien.khoi(g, x + 6 + wb + k, yy, w - 12 - 2 * (wb + k), hb, hb / 2, mh);
            ChuDe.chuGiua(g, "Ngọc " + KieuO.tenHe(this.heChon), x + w / 2, yy, hb, ChuDe.NHAN, 0xFFFFFF, 2);
            Bang.nut(g, x + w - 6 - wb, yy, wb, hb, "Sau", Bang.PHU, new IAction() {
                public void perform() {
                    ManKhamNgoc.this.heChon = (ManKhamNgoc.this.heChon + 1) % 18;
                    ManKhamNgoc.this.tuiChon = -1;
                }
            }, "he-sau");
            hLuoi = hb + k;
            hc = 0;
        }
        for (int i = 0; hc > 0 && i < KieuO.TEN_HE.length; i++) {
            final int hi = i;
            int cx = x + 6 + (i % cot) * (wc0(w, k, cot) + k);
            int cy = yy + (i / cot) * (hc + k);
            int wc = wc0(w, k, cot);
            boolean ch = this.heChon == i && this.tuiChon < 0;
            int mh = KieuO.mauHe(i);
            GiaoDien.khoi(g, cx, cy, wc, hc, hc / 2, ch ? mh : GiaoDien.pha(mh, 0xFFFFFF, 1, 3));
            ChuDe.chuGiua(g, ChuDe.cat(KieuO.tenHe(i), ChuDe.NHAN_NHO, wc - 6), cx + wc / 2, cy, hc, ChuDe.NHAN_NHO, ch ? 0xFFFFFF : ChuDe.CHU, 2);
            Bang.dangKy(g, cx, cy, wc, hc, new IAction() {
                public void perform() {
                    ManKhamNgoc.this.heChon = hi;
                    ManKhamNgoc.this.tuiChon = -1;
                }
            }, "he-" + i);
        }
        hc = Bang.co(22);
        yy += hLuoi;
        // ngọc trong túi (lấy ra khảm miễn phí)
        if (tui.size() > 0 && yy + hc < y + h - 4) {
            ChuDe.chu(g, "Hoặc lấy từ túi ngọc:", x + 8, yy, ChuDe.PHU, ChuDe.CHU, 0);
            yy += ChuDe.cao(ChuDe.PHU) + 3;
            int xx = x + 6;
            for (int i = 0; i < tui.size(); i++) {
                final int ti = i;
                int[] t = (int[]) tui.elementAt(i);
                String chu = TrangBiThem.tenNgoc(t[0], t[1]) + (t[2] != 0 ? " (mài)" : "") + " ×" + t[3];
                int wcc = ChuDe.rong(chu, ChuDe.NHAN) + 20;
                if (xx + wcc > x + w - 6) {
                    break;
                }
                xx += Bang.chip(g, xx, yy, chu, this.tuiChon == i, new IAction() {
                    public void perform() {
                        ManKhamNgoc.this.tuiChon = ManKhamNgoc.this.tuiChon == ti ? -1 : ti;
                    }
                }, "tui-" + i) + k;
            }
        }
    }

    /** Một lỗ: đĩa màu hệ + tên + nút (Nâng / Tháo / Mài hoặc Khảm). */
    void veLo(Graphics g, final Mon m, final int j, int x, int y, int w, int h) {
        GiaoDien.khoi(g, x, y, w, h, ChuDe.BO_O, ChuDe.KE);
        GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, ChuDe.BO_O - 1, ChuDe.NEN_DUOI);
        int r = Math.min(h / 2 - 4, Bang.co(14));
        int cx = x + 6 + r, cy = y + h / 2;
        int hn = Bang.co(26);
        int yb = y + h - hn - 4;
        int xt = x + 2 * r + 14;
        if (m.he[j] < 0) {
            GiaoDien.khoi(g, cx - r, cy - r, 2 * r, 2 * r, r, ChuDe.NEN_PHU_TOI);
            GiaoDien.khoi(g, cx - r + 2, cy - r + 2, 2 * r - 4, 2 * r - 4, r - 2, ChuDe.NEN);
            final int[] t = this.tuiChon >= 0 && this.tuiChon < tui.size() ? (int[]) tui.elementAt(this.tuiChon) : null;
            ChuDe.chu(g, "Lỗ " + (j + 1) + ": trống", xt, y + 4, ChuDe.NHAN, ChuDe.CHU, 0);
            String chu = t != null ? "Khảm " + TrangBiThem.tenNgoc(t[0], t[1]) + " (miễn phí)"
                    : "Khảm Ngọc " + KieuO.tenHe(this.heChon) + " 1 · " + TienTe.so(gia[1]) + " xu";
            Bang.nut(g, xt, yb, x + w - 6 - xt, hn, chu, Bang.CHINH, new IAction() {
                public void perform() {
                    if (t != null) {
                        gui(TrangBiThem.C_DAT_NGOC, new int[]{m.id, j, t[0], t[1], t[2]}, new int[]{2, 1, 1, 1, 1});
                    } else {
                        gui(TrangBiThem.C_DAT_NGOC, new int[]{m.id, j, ManKhamNgoc.this.heChon, 0, 0}, new int[]{2, 1, 1, 1, 1});
                    }
                }
            }, "kham-" + j);
            return;
        }
        int mh = KieuO.mauHe(m.he[j]);
        GiaoDien.khoiDoc(g, cx - r, cy - r, 2 * r, 2 * r, r, GiaoDien.pha(mh, 0xFFFFFF, 2, 5), mh);
        ChuDe.chuGiua(g, String.valueOf(m.cap[j]), cx, cy - r, 2 * r, ChuDe.NHAN, 0xFFFFFF, 2);
        String ten = "Lỗ " + (j + 1) + ": " + TrangBiThem.tenNgoc(m.he[j], m.cap[j]) + (m.mai[j] ? " (đã mài)" : "")
                + " · −kháng " + KieuO.tenHe(heKhac[m.he[j]]);
        ChuDe.chu(g, ChuDe.cat(ten, ChuDe.NHAN, x + w - 8 - xt), xt, y + 4, ChuDe.NHAN, ChuDe.CHU, 0);
        int k = Bang.cach();
        int wb = (x + w - 6 - xt - 2 * k) / 3;
        boolean toiDa = m.cap[j] >= 10;
        Bang.nut(g, xt, yb, wb, hn, toiDa ? "Cấp tối đa" : "Nâng · " + TienTe.gon(gia[Math.min(10, m.cap[j] + 1)]), toiDa ? Bang.TAT : Bang.CHINH, new IAction() {
            public void perform() {
                gui(TrangBiThem.C_NANG_NGOC, new int[]{m.id, j}, new int[]{2, 1});
            }
        }, "nang-" + j);
        Bang.nut(g, xt + wb + k, yb, wb, hn, "Tháo · " + TienTe.gon(giaThao), Bang.PHU, new IAction() {
            public void perform() {
                gui(TrangBiThem.C_THAO_NGOC, new int[]{m.id, j}, new int[]{2, 1});
            }
        }, "thao-" + j);
        Bang.nut(g, xt + 2 * (wb + k), yb, wb, hn, m.mai[j] ? "Đã mài" : "Mài · " + TienTe.gon(giaMai), m.mai[j] ? Bang.TAT : Bang.PHU, new IAction() {
            public void perform() {
                gui(TrangBiThem.C_MAI_NGOC, new int[]{m.id, j}, new int[]{2, 1});
            }
        }, "mai-" + j);
    }

    void veTui(Graphics g, int x, int y, int w, int h) {
        final Vector v = tui;
        if (v.size() == 0) {
            Bang.trong(g, x, y, w, h, "Túi ngọc trống", "Ngọc tháo khỏi trang bị (giữ nguyên cấp) nằm ở đây, khảm lại vào món khác miễn phí.");
            return;
        }
        Bang.danhSach(g, this.ds, x, y + 2, w, h - 4, v.size(), Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, final int i, int x, int y, int w, int h) {
                final int[] t = (int[]) v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, false, null, null);
                int r = h / 2 - 5;
                int mh = KieuO.mauHe(t[0]);
                GiaoDien.khoiDoc(g, x + 6, y + 5, 2 * r, 2 * r, r, GiaoDien.pha(mh, 0xFFFFFF, 2, 5), mh);
                ChuDe.chuGiua(g, String.valueOf(t[1]), x + 6 + r, y + 5, 2 * r, ChuDe.NHAN, 0xFFFFFF, 2);
                int wn = Math.max(Bang.co(84), ChuDe.rong("Phân rã +10 đá", ChuDe.NHAN) + 20);
                int xt = x + 2 * r + 14;
                Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, TrangBiThem.tenNgoc(t[0], t[1]) + (t[2] != 0 ? " (đã mài)" : "") + " ×" + t[3],
                        w - (xt - x) - wn - 12), xt, y + 2, 0);
                Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, "−kháng " + KieuO.tenHe(heKhac[t[0]]) + " · khảm lại miễn phí ở tab Trang bị",
                        w - (xt - x) - wn - 12), xt, y + 3 + Font.blackFont.getHeight(), 0);
                int hn = Bang.co(26);
                Bang.nut(g, x + w - wn - 6, y + (h - hn) / 2, wn, hn, "Phân rã +" + t[1] + " đá", Bang.NGUY, new IAction() {
                    public void perform() {
                        gui(TrangBiThem.C_PHAN_RA, new int[]{t[0], t[1], t[2]}, new int[]{1, 1, 1});
                    }
                }, "phan-ra-" + i);
            }
        });
    }
}
