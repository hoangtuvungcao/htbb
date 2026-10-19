package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * DANH HIỆU (0.9.7) - tấm 78 % (ManBang), mở từ Hồ sơ (ManNhanVat, viên "Danh hiệu" cạnh huy hiệu hệ): tab Đã có |
 * Chưa có | Ẩn. Hàng = viên danh hiệu (màu độ hiếm, token ChuDe.mauDanhHieu) + tên + độ hiếm · dòng chỉ số; danh
 * hiệu chưa có có thanh tiến độ "a/b"; danh hiệu có hạn đếm ngược; danh hiệu ẩn hiện "???" + gợi ý tới khi mở. Chạm
 * hàng = thẻ chi tiết (mô tả điều kiện, tiến độ, chỉ số, nút to Đeo / Tháo). Chỉ MỘT danh hiệu đang đeo được tính chỉ
 * số. Mở bảng = đã xem danh hiệu mới (chấm đỏ ChamMoi.DANH_HIEU tắt).
 *
 * Dữ liệu: DanhHieuKho (gói -110 / -111). Chỉ dùng thành phần Bang / ChuDe (giao diện khác thay token là đổi theo).
 */
public final class ManDanhHieu extends ManBang {
    public static ManDanhHieu me;
    /** dòng đang xem chi tiết (null = danh sách) */
    DanhHieuKho.Dong xem;

    public static ManDanhHieu gI() {
        if (me == null) {
            me = new ManDanhHieu();
        }
        return me;
    }

    /** Mở bảng (đã có bảng thì hiện ngay, đồng thời xin bảng mới). */
    public static void mo() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        ManDanhHieu m = gI();
        boolean dangMo = Canvas.currentScreen == m;
        if (!dangMo) {
            m.xem = null;
            m.moBang();
            m.chonTab(DanhHieuKho.soMoi > 0 || DanhHieuKho.dem(0) > 0 ? 0 : 1);
        }
        if (DanhHieuKho.bang == null && !DanhHieuKho.choBang) {
            DanhHieuKho.xin();
        }
        DanhHieuKho.daXem();
    }

    /** Mở ở tab t (0 Đã có, 1 Chưa có, 2 Ẩn). */
    public static void moTab(int t) {
        mo();
        gI().chonTab(t);
    }

    /** Lối vào từ Hồ sơ: mở + xin bảng mới. */
    public static void moTuHoSo() {
        DanhHieuKho.xin();
        mo();
    }

    protected String tieuDe() {
        return "Danh hiệu";
    }

    protected String[] tabs() {
        return new String[]{"Đã có (" + DanhHieuKho.dem(0) + ")", "Chưa có (" + DanhHieuKho.dem(1) + ")",
            "Bí ẩn (" + DanhHieuKho.dem(2) + ")"};
    }

    protected int rongMuon() {
        return ChuDe.dt() ? 500 : 460;
    }

    protected void chonTab(int t) {
        super.chonTab(t);
        this.xem = null;
    }

    protected void lui() {
        if (this.xem != null) {
            this.xem = null;
            return;
        }
        dong();
    }

    /** Cao hàng: tên + dòng phụ + thanh. */
    static int caoHang() {
        return Math.max(Math.max(Bang.caoHang(), Bang.co(24) + 8), hVien() + ChuDe.cao(ChuDe.PHU) + 22);
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        Vector bang = DanhHieuKho.bang;
        if (bang == null) {
            Bang.trong(g, x, y, w, h, DanhHieuKho.choBang ? "Đang tải danh hiệu..." : "Chưa có dữ liệu danh hiệu", null);
            return;
        }
        if (this.xem != null) {
            veChiTiet(g, x, y, w, h, this.xem);
            return;
        }
        int yy = y + 2;
        // thẻ "đang đeo"
        int hd = Bang.co(26) + 8;
        DanhHieuKho.Dong deo = DanhHieuKho.dongDeo();
        Bang.the(g, x, yy, w, hd);
        int xx = x + 8;
        ChuDe.chuGiua(g, "Đang đeo", xx, yy, hd, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        xx += ChuDe.rong("Đang đeo", ChuDe.PHU) + 8;
        if (deo == null) {
            ChuDe.chuGiua(g, "Chưa đeo danh hiệu nào", xx, yy, hd, ChuDe.NHAN, ChuDe.CHU_NHAT, 0);
        } else {
            int wTh = ChuDe.rong("Tháo", ChuDe.NHAN) + 24;
            int wv = vien(g, xx, yy + (hd - hVien()) / 2, deo.ten, deo.doHiem, deo.hinh, w - (xx - x) - wTh - 16);
            if (deo.chiSo != null && deo.chiSo.length() > 0) {
                ChuDe.chuGiua(g, ChuDe.cat(deo.chiSo, ChuDe.PHU, w - (xx - x) - wv - wTh - 24), xx + wv + 6, yy, hd, ChuDe.PHU,
                        Bang.TANG, 0);
            }
            Bang.nut(g, x + w - 6 - wTh, yy + 4, wTh, hd - 8, "Tháo", Bang.PHU, new IAction() {
                public void perform() {
                    DanhHieuKho.thao();
                    AmThanh.phat(AmThanh.XAC_NHAN);
                }
            }, "danh-hieu-thao");
        }
        yy += hd + Bang.cach();
        final Vector v = DanhHieuKho.loc(this.tab);
        int hl = y + h - yy;
        if (v.size() == 0) {
            String[] chinh = {"Chưa có danh hiệu nào", "Đã mở hết danh hiệu", "Đã tìm ra mọi danh hiệu ẩn"};
            String[] phu = {"Lên cấp, câu cá, hạ trùm, thắng PvP, làm nhiệm vụ... để mở danh hiệu.", null,
                "Danh hiệu ẩn hiện tên khi bạn mở được."};
            Bang.trong(g, x, yy, w, hl, chinh[this.tab], phu[this.tab]);
            return;
        }
        Bang.danhSach(g, this.ds, x, yy, w, hl, v.size(), caoHang(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final DanhHieuKho.Dong d = (DanhHieuKho.Dong) v.elementAt(i);
                veHang(g, d, i, x, y, w, h);
            }
        });
    }

    void veHang(Graphics g, final DanhHieuKho.Dong d, int i, int x, int y, int w, int h) {
        Bang.hang(g, x, y, w, h, i, d.deo(), new IAction() {
            public void perform() {
                ManDanhHieu.this.xem = d;
                AmThanh.phat(AmThanh.XAC_NHAN);
            }
        }, "danh-hieu-" + d.id);
        int xx = x + 8, yy = y + 5;
        // phải: nút Đeo / chip Đang đeo / phần trăm
        int wPhai = 0;
        if (d.co()) {
            String chu = d.deo() ? "Đang đeo" : "Đeo";
            int wn = ChuDe.rong(chu, ChuDe.NHAN) + 24;
            int hn = Math.min(h - 6, Bang.co(24));
            if (d.deo()) {
                GiaoDien.khoi(g, x + w - 8 - wn, y + (h - hn) / 2, wn, hn, hn / 2, ChuDe.CHINH_NHAT);
                ChuDe.chuGiua(g, chu, x + w - 8 - wn / 2, y + (h - hn) / 2, hn, ChuDe.NHAN, ChuDe.CHINH_TOI, 2);
            } else {
                Bang.nut(g, x + w - 8 - wn, y + (h - hn) / 2, wn, hn, chu, Bang.CHINH, new IAction() {
                    public void perform() {
                        DanhHieuKho.deo(d.id);
                        AmThanh.phat(AmThanh.XAC_NHAN);
                    }
                }, "danh-hieu-deo-" + d.id);
            }
            wPhai = wn + 12;
        }
        int wNoi = w - 16 - wPhai;
        // dòng 1: viên tên (màu độ hiếm)
        vien(g, xx, yy, d.ten, d.doHiem, d.an ? null : d.hinh, wNoi);
        if (d.moi) {
            ChuDe.cham(g, x + w - 6, y + 6);
        }
        yy += hVien() + 3;
        // dòng 2: độ hiếm · chỉ số · hạn  |  hoặc mô tả điều kiện / gợi ý
        StringBuffer b = new StringBuffer();
        if (d.an) {
            b.append(d.goiY != null && d.goiY.length() > 0 ? d.goiY : "Danh hiệu ẩn - tự tìm cách mở.");
        } else {
            b.append(DanhHieuKho.tenHiem(d.doHiem));
            if (d.chiSo != null && d.chiSo.length() > 0) {
                b.append(" · ").append(d.chiSo);
            }
            int cl = d.conLai();
            if (d.co() && cl >= 0) {
                b.append(" · còn ").append(chuConLai(cl));
            } else if (!d.co() && d.phutConLai > 0) {
                b.append(" · có hạn ").append(chuConLai(d.phutConLai));
            }
        }
        ChuDe.chu(g, ChuDe.cat(b.toString(), ChuDe.PHU, wNoi), xx, yy, ChuDe.PHU, d.an ? ChuDe.CHU_NHAT : ChuDe.CHU_PHU, 0);
        // thanh tiến độ (chưa có, không ẩn)
        if (!d.co() && !d.an && d.can > 1) {
            String so = d.tienDo + "/" + d.can;
            int ws = ChuDe.rong(so, ChuDe.SO);
            int yb = yy + ChuDe.cao(ChuDe.PHU) + 4;
            int wb = Math.max(40, w - 16 - ws - 8);
            GiaoDien.thanh(g, xx, yb, wb, 5, d.tienDo, d.can, 2);
            ChuDe.chuGiua(g, so, xx + wb + 6, yb - 5, 15, ChuDe.SO, ChuDe.CHU_PHU, 0);
        }
    }

    /** Thẻ chi tiết: viên to, mô tả, tiến độ, chỉ số, nguồn, nút Đeo / Tháo. */
    void veChiTiet(Graphics g, int x, int y, int w, int h, final DanhHieuKho.Dong d0) {
        DanhHieuKho.Dong d = d0;
        // bảng mới về (đeo / tháo): lấy dòng mới cùng id
        Vector v = DanhHieuKho.bang;
        for (int i = 0; v != null && i < v.size(); i++) {
            DanhHieuKho.Dong e = (DanhHieuKho.Dong) v.elementAt(i);
            if (e.id == d0.id) {
                d = e;
                this.xem = e;
            }
        }
        final DanhHieuKho.Dong dd = d;
        Bang.the(g, x, y + 2, w, h - 4);
        int xx = x + 12, yy = y + 10, ww = w - 24;
        Bang.nutLui(g, xx - 4, yy - 4, Bang.co(26), new IAction() {
            public void perform() {
                ManDanhHieu.this.xem = null;
            }
        });
        yy += Bang.co(26) + 2;
        vien(g, xx, yy, d.ten, d.doHiem, d.an ? null : d.hinh, ww);
        yy += hVien() + 6;
        String hiem = DanhHieuKho.tenHiem(d.doHiem);
        ChuDe.chu(g, d.an ? "Danh hiệu ẩn" : hiem, xx, yy, ChuDe.NHAN,
                ChuDe.mauDanhHieu(d.doHiem), 0);
        yy += ChuDe.cao(ChuDe.NHAN) + 4;
        yy = Bang.doan(g, ChuDe.THUONG, d.an ? "???" : d.moTa, xx, yy, ww, false, ChuDe.CHU) + 4;
        if (d.an || (d.goiY != null && d.goiY.length() > 0 && !d.co())) {
            yy = Bang.doan(g, ChuDe.PHU, "Gợi ý: " + (d.goiY == null || d.goiY.length() == 0 ? "tự khám phá." : d.goiY), xx, yy, ww,
                    false, ChuDe.CHU_PHU) + 4;
        }
        if (!d.an && d.chiSo != null && d.chiSo.length() > 0) {
            ChuDe.chu(g, "Chỉ số khi đeo: " + d.chiSo, xx, yy, ChuDe.PHU, Bang.TANG, 0);
            yy += ChuDe.cao(ChuDe.PHU) + 2;
            ChuDe.chu(g, ChuDe.cat("Chỉ danh hiệu đang đeo được tính, không cộng dồn.", ChuDe.PHU, ww), xx, yy, ChuDe.PHU,
                    ChuDe.CHU_NHAT, 0);
            yy += ChuDe.cao(ChuDe.PHU) + 6;
        }
        int cl = d.conLai();
        if (d.co()) {
            ChuDe.chu(g, cl < 0 ? "Vĩnh viễn" : "Còn " + chuConLai(cl), xx, yy, ChuDe.PHU, cl >= 0 && cl < 1440 ? ChuDe.DO : ChuDe.CHU_PHU, 0);
            yy += ChuDe.cao(ChuDe.PHU) + 6;
        } else if (!d.an) {
            if (d.can > 1) {
                Bang.thanh(g, xx, yy, ww, Bang.co(14), d.tienDo, d.can, 2, d.tienDo + " / " + d.can);
                yy += Bang.co(14) + 6;
            }
            if (d.phutConLai > 0) {
                ChuDe.chu(g, "Có hạn " + chuConLai(d.phutConLai) + " (giữ điều kiện để gia hạn).", xx, yy, ChuDe.PHU, ChuDe.CHU_PHU, 0);
                yy += ChuDe.cao(ChuDe.PHU) + 6;
            }
        }
        int nutH = Bang.co(30);
        int yn = y + h - 10 - nutH;
        if (d.co()) {
            Bang.nut(g, xx, yn, ww, nutH, d.deo() ? "Tháo danh hiệu" : "Đeo danh hiệu", d.deo() ? Bang.PHU : Bang.XAC_NHAN,
                    new IAction() {
                        public void perform() {
                            if (dd.deo()) {
                                DanhHieuKho.thao();
                            } else {
                                DanhHieuKho.deo(dd.id);
                            }
                            AmThanh.phat(AmThanh.XAC_NHAN);
                        }
                    }, "danh-hieu-nut");
        } else {
            Bang.nut(g, xx, yn, ww, nutH, "Chưa mở khoá", Bang.TAT, null, "danh-hieu-nut");
        }
    }

    // ------------------------------------------------------------------ viên danh hiệu (dùng chung: bảng, Hồ sơ)

    static int hVien() {
        return ChuDe.cao(ChuDe.NHAN) + 4;
    }

    /**
     * Viên danh hiệu: nền nhạt cùng tông độ hiếm, viền + chữ màu độ hiếm, biểu tượng hình (KieuO) bên trái. Trả bề
     * ngang. hinh null = không biểu tượng (danh hiệu ẩn).
     */
    public static int vien(Graphics g, int x, int y, String ten, int doHiem, String hinh, int wToiDa) {
        int h = hVien();
        int mau = ChuDe.mauDanhHieu(doHiem);
        Image a = hinh == null ? null : KieuO.anhHinh(DanhHieuKho.hinh(hinh), 1);
        int icon = a == null ? 0 : a.getWidth() + 3;
        String s = ChuDe.cat(ten == null ? "" : ten, ChuDe.NHAN, Math.max(20, wToiDa - icon - 16));
        int w = ChuDe.rong(s, ChuDe.NHAN) + 14 + icon;
        GiaoDien.khoi(g, x, y, w, h, h / 2, mau);
        GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, h / 2 - 1, ChuDe.nenDanhHieu(doHiem));
        int xc = x + 7;
        if (a != null) {
            g.drawImage(a, xc - 1 + a.getWidth() / 2, y + h / 2, Graphics.HCENTER | Graphics.VCENTER);
            xc += icon;
        }
        ChuDe.chuGiua(g, s, xc, y, h, ChuDe.NHAN, GiaoDien.pha(mau, ChuDe.CHU, 1, 2), 0);
        return w;
    }

    /** "3 ngày 4 giờ" / "5 giờ" / "12 phút". */
    static String chuConLai(int phut) {
        int ngay = phut / 1440, gio = (phut % 1440) / 60, p = phut % 60;
        if (ngay > 0) {
            return ngay + " ngày" + (gio > 0 ? " " + gio + " giờ" : "");
        }
        if (gio > 0) {
            return gio + " giờ" + (p > 0 ? " " + p + " phút" : "");
        }
        return Math.max(1, p) + " phút";
    }
}
