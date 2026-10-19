package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import game.render.screen.Font;
import java.io.DataInputStream;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * 0.9.7 CHẾ TẠO (Machamp thợ rèn, gói -120 kiểu 2 / 3 - real.CheTaoGame): đầu bảng = tỉ lệ bậc thành phẩm (Hiếm 60% /
 * Quý 30% / Sử thi 9% / Huyền thoại 1%) + bộ đếm pity ("Chưa ra Sử thi 7/20 - lần 20 chắc Sử thi"); danh sách công thức
 * (icon, tên, bậc · cấp, nguyên liệu có / cần, xu) với nút "Chế tạo" (xám + lý do khi thiếu). Phiến đá hệ: chọn hệ + kiểu
 * (tăng ST hệ / giảm ST nhận) ở tấm dưới trước khi chế. Máy chủ kiểm lại mọi điều kiện.
 */
public final class ManCheTao extends ManBang {
    public static ManCheTao me;

    /** Một công thức: {idTemplate, idIcon, bac, cap, da, soNl, nlCo, xu, loi, phienDa} + chữ {ten, nlPhu, thieu}. */
    static final class CongThuc {
        int idTemplate, idIcon, bac, cap, da, soNl, nlCo, xu, loi;
        boolean phienDa;
        String ten, nlPhu, thieu;
    }

    static Vector dsCt = new Vector();
    static String[] tenBac = new String[0];
    static int[] mauBac = new int[0];
    static int[] phanNghin = new int[0];
    static int pity, nguong, daCo;
    static boolean coDuLieu;
    /** Đang chờ máy chủ trả lời lần chế (chặn bấm đúp). */
    static boolean cho;
    static long choLuc;
    /** Kết quả vừa chế (tên, bậc, màu) - hiện ở đầu bảng vài giây. */
    static String kqTen;
    static String kqBac;
    static int kqMau;
    static long kqLuc;
    int chon = -1;
    int he;
    int kieuDa;

    public static ManCheTao gI() {
        if (me == null) {
            me = new ManCheTao();
        }
        return me;
    }

    /** Gói -120 kiểu 2: danh sách. Mở màn nếu chưa mở. */
    static void doc(DataInputStream r) throws java.io.IOException {
        int nb = r.readUnsignedByte();
        String[] tb = new String[nb];
        int[] mb = new int[nb];
        int[] pn = new int[nb];
        for (int i = 0; i < nb; i++) {
            r.readByte();
            tb[i] = r.readUTF();
            mb[i] = r.readByte();
            pn[i] = r.readShort();
            r.readByte();
        }
        int pi = r.readUnsignedByte();
        int ng = r.readUnsignedByte();
        int da = r.readInt();
        int n = r.readUnsignedByte();
        Vector v = new Vector();
        for (int i = 0; i < n; i++) {
            CongThuc c = new CongThuc();
            c.idTemplate = r.readShort();
            c.idIcon = r.readShort();
            c.ten = r.readUTF();
            c.bac = r.readUnsignedByte();
            c.cap = r.readUnsignedByte();
            c.da = r.readUnsignedByte();
            c.nlPhu = r.readUTF();
            c.soNl = r.readUnsignedByte();
            c.nlCo = r.readShort();
            c.xu = r.readInt();
            c.loi = r.readUnsignedByte();
            c.phienDa = r.readByte() != 0;
            c.thieu = r.readUTF();
            v.addElement(c);
        }
        datDuLieu(tb, mb, pn, pi, ng, da, v);
        cho = false;
        if (Canvas.currentScreen != gI()) {
            gI().chon = -1;
            gI().moBang();
        }
    }

    /** Đặt dữ liệu (gói hoặc kiểm thử). */
    static void datDuLieu(String[] tb, int[] mb, int[] pn, int pi, int ng, int da, Vector v) {
        tenBac = tb;
        mauBac = mb;
        phanNghin = pn;
        pity = pi;
        nguong = ng;
        daCo = da;
        dsCt = v;
        coDuLieu = true;
    }

    /** Gói -120 kiểu 3: kết quả. */
    static void ketQua(DataInputStream r) throws java.io.IOException {
        r.readByte();
        r.readShort();
        r.readShort();
        kqTen = r.readUTF();
        r.readByte();
        kqBac = r.readUTF();
        kqMau = r.readByte();
        kqLuc = System.currentTimeMillis();
        cho = false;
        AmThanh.phat(AmThanh.XAC_NHAN);
    }

    /** Mở từ client (xin danh sách) - menu Machamp mở qua máy chủ. */
    public static void mo() {
        TrangBiThem.gui(TrangBiThem.C_XIN_CHE_TAO, null, null);
    }

    protected String tieuDe() {
        return "Chế tạo";
    }

    protected int rongMuon() {
        return ChuDe.dt() ? 560 : 500;
    }

    protected void lui() {
        if (this.chon >= 0) {
            this.chon = -1;
        } else {
            dong();
        }
    }

    static String phanTram(int pn) {
        return pn % 10 == 0 ? (pn / 10) + "%" : (pn / 10) + "," + (pn % 10) + "%";
    }

    /** "Hiếm 60% · Quý 30% · Sử thi 9% · Huyền thoại 1%" */
    static String chuTiLe() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tenBac.length; i++) {
            if (i > 0) {
                sb.append(" · ");
            }
            sb.append(tenBac[i]).append(' ').append(phanTram(phanNghin[i]));
        }
        return sb.toString();
    }

    static String chuPity() {
        if (nguong <= 0) {
            return "Đá nâng cấp đang có: " + daCo;
        }
        return "Chưa ra Sử thi: " + pity + "/" + nguong + " (lần " + nguong + " chắc Sử thi) · Đá nâng cấp: " + daCo;
    }

    static String chuNguyenLieu(CongThuc c) {
        StringBuffer sb = new StringBuffer();
        sb.append("Đá ").append(Math.min(daCo, 999)).append('/').append(c.da);
        if (c.soNl > 0) {
            sb.append(" · ").append(c.nlPhu).append(' ').append(c.nlCo).append('/').append(c.soNl);
        }
        if (c.loi > 0) {
            sb.append(" · Lõi chế tạo ").append(c.loi);
        }
        return sb.toString();
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        if (!coDuLieu) {
            Bang.trong(g, x, y, w, h, "Đang tải công thức...", null);
            return;
        }
        int yy = y + 2;
        // đầu bảng: tỉ lệ + pity (hoặc kết quả vừa chế)
        int hd = ChuDe.cao(ChuDe.PHU) * 2 + 10;
        GiaoDien.khoi(g, x, yy, w, hd, ChuDe.BO_O, ChuDe.CHINH_NHAT);
        if (kqTen != null && System.currentTimeMillis() - kqLuc < 6000) {
            int mh = KieuO.mauHiem(kqMau);
            ChuDe.chu(g, ChuDe.cat("Vừa chế: " + kqTen, ChuDe.NHAN, w - 16), x + 8, yy + 4, ChuDe.NHAN, mh >= 0 ? GiaoDien.pha(mh, 0, 2, 5) : ChuDe.CHU, 0);
            ChuDe.chu(g, ChuDe.cat("Bậc " + kqBac + " · " + chuPity(), ChuDe.PHU, w - 16), x + 8, yy + 5 + ChuDe.cao(ChuDe.NHAN), ChuDe.PHU, ChuDe.CHU_PHU, 0);
        } else {
            ChuDe.chu(g, ChuDe.cat(chuTiLe(), ChuDe.PHU, w - 16), x + 8, yy + 4, ChuDe.PHU, ChuDe.CHINH_TOI, 0);
            ChuDe.chu(g, ChuDe.cat(chuPity(), ChuDe.PHU, w - 16), x + 8, yy + 5 + ChuDe.cao(ChuDe.PHU), ChuDe.PHU, ChuDe.CHU_PHU, 0);
        }
        yy += hd + k;
        final Vector v = dsCt;
        if (v.size() == 0) {
            Bang.trong(g, x, yy, w, y + h - yy, "Chưa có công thức", "Máy chủ chưa nạp bảng chế tạo.");
            return;
        }
        // tấm dưới cho Phiến đá hệ đang chọn: chọn hệ + kiểu + nút chế
        CongThuc chonCt = this.chon >= 0 && this.chon < v.size() ? (CongThuc) v.elementAt(this.chon) : null;
        int hTam = 0;
        if (chonCt != null && chonCt.phienDa) {
            hTam = veTamPhienDa(g, x, y + h - 2, w, chonCt, true);
        }
        final int rh = Bang.caoHang() + ChuDe.cao(ChuDe.PHU) + 2;
        Bang.danhSach(g, this.ds, x, yy, w, y + h - 2 - yy - (hTam > 0 ? hTam + k : 0), v.size(), rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                veHang(g, (CongThuc) v.elementAt(i), i, x, y, w, h);
            }
        });
        if (hTam > 0) {
            veTamPhienDa(g, x, y + h - 2 - hTam, w, chonCt, false);
        }
    }

    void veHang(Graphics g, final CongThuc c, final int i, int x, int y, int w, int h) {
        Bang.hang(g, x, y, w, h, i, i == this.chon, new IAction() {
            public void perform() {
                ManCheTao.this.chon = ManCheTao.this.chon == i ? -1 : i;
            }
        }, "ct-" + i);
        int s = Bang.caoHang() - 6;
        GiaoDien.khoi(g, x + 3, y + 3, s, s, ChuDe.BO_O, ChuDe.NEN_PHU);
        if (c.idIcon >= 0) {
            Bang.icon(g, (short) c.idIcon, x + 3 + s / 2, y + 3 + s / 2, s >= 26 ? s / 22f : 1f);
        }
        boolean du = c.thieu == null || c.thieu.length() == 0;
        int wn = Math.max(Bang.co(72), ChuDe.rong("Chế tạo", ChuDe.NHAN) + 24);
        int hn = Bang.co(26);
        int xt = x + s + 9;
        int wt = w - s - 16 - wn;
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, c.ten, wt), xt, y + 2, 0);
        Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, "Bậc " + c.bac + " · Lv." + c.cap + " · " + TienTe.so(c.xu) + " xu", wt),
                xt, y + 3 + Font.blackFont.getHeight(), 0);
        ChuDe.chu(g, ChuDe.cat(du ? chuNguyenLieu(c) : c.thieu, ChuDe.PHU, w - 16), xt, y + 4 + Font.blackFont.getHeight() + Font.arialFont.getHeight(),
                ChuDe.PHU, du ? ChuDe.CHU_PHU : ChuDe.DO_TOI, 0);
        if (!c.phienDa) {
            Bang.nut(g, x + w - wn - 6, y + (h - hn) / 2, wn, hn, cho ? "..." : "Chế tạo", du && !cho ? Bang.CHINH : Bang.TAT, new IAction() {
                public void perform() {
                    cheTao(c, -1, 0);
                }
            }, "che-" + i);
        } else {
            Bang.nut(g, x + w - wn - 6, y + (h - hn) / 2, wn, hn, "Chọn hệ", Bang.PHU, new IAction() {
                public void perform() {
                    ManCheTao.this.chon = i;
                }
            }, "chon-he-" + i);
        }
    }

    /** Tấm chọn hệ cho Phiến đá: trả cao (doCao = chỉ đo, không vẽ). */
    int veTamPhienDa(Graphics g, int x, int yDay, int w, final CongThuc c, boolean doCao) {
        int k = Bang.cach();
        int hc = Bang.co(22);
        int cot = Math.max(1, (w - 12 + k) / (Bang.co(64) + k));
        int hang = (KieuO.TEN_HE.length + cot - 1) / cot;
        int hn = Bang.co(28);
        int hTam = 6 + ChuDe.cao(ChuDe.PHU) + 4 + hang * (hc + k) + hc + k + hn + 6;
        if (doCao) {
            return hTam;
        }
        int y = yDay;
        Bang.the(g, x, y, w, hTam);
        int yy = y + 6;
        ChuDe.chu(g, ChuDe.cat(c.ten + ": chọn 1 trong 18 hệ (5%)", ChuDe.PHU, w - 16), x + 8, yy, ChuDe.PHU, ChuDe.CHU, 0);
        yy += ChuDe.cao(ChuDe.PHU) + 4;
        int wc = (w - 12 - (cot - 1) * k) / cot;
        for (int i = 0; i < KieuO.TEN_HE.length; i++) {
            final int hi = i;
            int cx = x + 6 + (i % cot) * (wc + k);
            int cy = yy + (i / cot) * (hc + k);
            boolean ch = this.he == i;
            int mh = KieuO.mauHe(i);
            GiaoDien.khoi(g, cx, cy, wc, hc, hc / 2, ch ? mh : GiaoDien.pha(mh, 0xFFFFFF, 1, 3));
            ChuDe.chuGiua(g, ChuDe.cat(KieuO.tenHe(i), ChuDe.NHAN_NHO, wc - 6), cx + wc / 2, cy, hc, ChuDe.NHAN_NHO, ch ? 0xFFFFFF : ChuDe.CHU, 2);
            Bang.dangKy(g, cx, cy, wc, hc, new IAction() {
                public void perform() {
                    ManCheTao.this.he = hi;
                }
            }, "he-" + i);
        }
        yy += hang * (hc + k);
        String[] kieu = {"+5% sát thương hệ " + KieuO.tenHe(this.he), "−5% sát thương nhận từ hệ " + KieuO.tenHe(this.he)};
        Object[] vk = new Object[2];
        for (int i = 0; i < 2; i++) {
            final int ki = i;
            vk[i] = new IAction() {
                public void perform() {
                    ManCheTao.this.kieuDa = ki;
                }
            };
        }
        Bang.hangChip(g, x + 6, yy, w - 12, kieu, this.kieuDa, vk);
        yy += hc + k;
        boolean du = c.thieu == null || c.thieu.length() == 0;
        Bang.nut(g, x + 6, yy, w - 12, hn, cho ? "..." : (du ? "Chế tạo " + c.ten : c.thieu), du && !cho ? Bang.CHINH : Bang.TAT, new IAction() {
            public void perform() {
                cheTao(c, ManCheTao.this.he, ManCheTao.this.kieuDa);
            }
        }, "che-phien-da");
        return hTam;
    }

    static void cheTao(CongThuc c, int he, int kieu) {
        if (cho && System.currentTimeMillis() - choLuc < 5000) {
            return;
        }
        cho = true;
        choLuc = System.currentTimeMillis();
        TrangBiThem.gui(TrangBiThem.C_CHE_TAO, new int[]{c.idTemplate, he, kieu}, new int[]{2, 1, 1});
    }
}
