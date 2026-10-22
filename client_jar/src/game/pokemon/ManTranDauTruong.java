package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * TRẬN ĐẤU TRƯỜNG BẢO BỐI ({@link DauTruong}, gói -109 kiểu 2 / 3 / 4): hai bên (đối thủ trên, mình dưới) với chân dung
 * PNG, tên, thanh HP, trạng thái, 3 bóng đội; khung nhật ký các lượt; 4 nút chiêu (nhãn hệ màu hệ, PP còn / tối đa) + nút
 * đổi sang 2 Pokémon dự bị + Tự động + đồng hồ lượt. Hết giờ / Tự động: máy chủ chọn. Có kết quả: thẻ Thắng / Thua, điểm
 * trước → sau, lượng khoá, nút "Về Đấu trường". Màn hẹp (J2ME): bỏ chân dung to, nhật ký 2 dòng.
 */
public final class ManTranDauTruong extends ManBang {
    public static ManTranDauTruong me;

    public static ManTranDauTruong gI() {
        if (me == null) {
            me = new ManTranDauTruong();
        }
        return me;
    }

    public static void mo() {
        ManTranDauTruong m = gI();
        if (Canvas.currentScreen != m) {
            m.moBang();
        }
    }

    protected String tieuDe() {
        if (DauTruong.coKetQua) {
            return DauTruong.kqThang ? "Chiến thắng!" : "Thất bại";
        }
        return "Lượt " + Math.min(DauTruong.luotToiDa, DauTruong.luot + 1) + "/" + DauTruong.luotToiDa + " · " + DauTruong.tenDoiThu;
    }

    protected int rongMuon() {
        return ChuDe.dt() ? 720 : 620;
    }

    protected void lui() {
        dong();
    }

    public void dong() {
        if (DauTruong.dangDanh()) {
            HopXacNhan.hoiNguyHiem("Đầu hàng", "Rời trận bây giờ là thua trận này (vẫn tính một lượt).", "Đầu hàng", new IAction() {
                public void perform() {
                    DauTruong.dauHang();
                }
            });
            return;
        }
        veDauTruong();
    }

    static void veDauTruong() {
        DauTruong.tranId = 0;
        // đối thủ vừa đấu đã rời danh sách đề xuất của máy chủ: xin lại khi bấm Tìm
        DauTruong.DOI_THU = new java.util.Vector();
        DauTruong.coDoiThu = false;
        DauTruong.xinMo();
        if (ManDauTruong.me != null) {
            ManDauTruong.me.moBang();
        } else {
            Canvas.gameScr.switchToMe();
        }
    }

    static boolean hep() {
        return Canvas.w < 300 || Canvas.h < 260;
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        if (DauTruong.DOI[0][0] == null) {
            Bang.trong(g, x, y, w, h, "Đang vào trận...", null);
            return;
        }
        if (DauTruong.coKetQua) {
            veKetQua(g, x, y, w, h);
            return;
        }
        int k = Bang.cach();
        boolean nho = hep();
        int lh = ChuDe.cao(ChuDe.NHAN), lp = ChuDe.cao(ChuDe.PHU);
        // dưới cùng: 4 chiêu (2 x 2) + hàng đổi / tự động
        int hn = Math.max(Bang.co(34), lh + lp + 8);
        int hd = Bang.co(28);
        int hDuoi = 2 * hn + hd + 2 * k;
        int yDuoi = y + h - hDuoi;
        // trên: hai bên - xếp chồng khi đủ cao, cạnh nhau khi màn thấp (nhật ký luôn còn >= 3 dòng)
        int hMin = lh + lp + Bang.co(12) + 12;
        int logMin = 3 * (lp + 1) + 10;
        int avail = yDuoi - k - (y + 2);
        int yLog;
        if (avail - logMin - 2 * k >= 2 * hMin) {
            int hBen = Math.max(hMin, nho ? 0 : Math.min(Math.min(Bang.co(70), avail * 3 / 10), (avail - logMin - 2 * k) / 2));
            veBen(g, 1, x, y + 2, w, hBen, nho);
            veBen(g, 0, x, y + 2 + hBen + k, w, hBen, nho);
            yLog = y + 2 + 2 * (hBen + k);
        } else {
            int hBen = Math.max(hMin, Math.min(Bang.co(70), avail - logMin - k));
            int wb = (w - k) / 2;
            veBen(g, 0, x, y + 2, wb, hBen, nho);
            veBen(g, 1, x + wb + k, y + 2, w - wb - k, hBen, nho);
            yLog = y + 2 + hBen + k;
        }
        veNhatKy(g, x, yLog, w, yDuoi - k - yLog);
        veChieu(g, x, yDuoi, w, hn, hd);
    }

    /** Một bên: ben 1 = đối thủ (chân dung phải), 0 = mình (chân dung trái). */
    void veBen(Graphics g, int ben, int x, int y, int w, int h, boolean nho) {
        Bang.the(g, x, y, w, h);
        DauTruong.Con c = DauTruong.dangRa(ben);
        int lh = ChuDe.cao(ChuDe.NHAN), lp = ChuDe.cao(ChuDe.PHU);
        int s = nho ? 0 : h - 8;
        boolean phai = ben == 1;
        int xs = phai ? x + w - s - 6 : x + 6;
        if (s > 0) {
            int mh = KieuO.mauHe(c.he1);
            GiaoDien.khoi(g, xs, y + 4, s, s, s / 2, GiaoDien.pha(mh, 0xFFFFFF, 4, 5));
            PokeBody.paintChanDung(g, c.dex, c.shiny, xs + s / 2, y + 4 + s / 2, s);
        }
        int xt = phai ? x + 8 : x + 6 + s + 8;
        int wt = w - s - 22;
        int is = Math.max(10, Math.min(16, lh));
        int wBong = 3 * (is + 2);
        String ten = c.ten + (ben == 1 ? " (đối thủ)" : "") + " · cấp 50";
        ChuDe.chu(g, ChuDe.cat(ten, ChuDe.NHAN, wt - wBong - 6), xt, y + 5, ChuDe.NHAN, ChuDe.CHU, 0);
        // 3 bóng đội (PNG): sáng = còn, rỗng = đã gục
        for (int i = 0; i < 3; i++) {
            DauTruong.Con o = DauTruong.DOI[ben][i];
            boolean song = o != null && o.hp > 0;
            IconBat.ve(g, song ? IconBat.POKE : IconBat.SAO_RONG, xt + wt - wBong + i * (is + 2) + is / 2, y + 5 + lh / 2, is);
        }
        int yb = y + 7 + lh;
        int hb = Math.max(8, lp - 2);
        String tt = c.tt > 0 && c.tt < DauTruong.TEN_TT.length ? DauTruong.TEN_TT[c.tt] : "";
        int wtt = tt.length() > 0 ? ChuDe.rong(tt, ChuDe.PHU) + 12 : 0;
        Bang.thanh(g, xt, yb, wt - wtt - (wtt > 0 ? 6 : 0), hb, c.hp, c.hpMax, 0, null);
        if (wtt > 0) {
            GiaoDien.vien(g, xt + wt - wtt, yb - 1, wtt, hb + 2, ttMau(c.tt), null);
            ChuDe.chuVienGiua(g, tt, xt + wt - wtt / 2, yb - 1, hb + 2, ChuDe.PHU, 0xFFFFFF, ChuDe.VIEN, 2);
        }
        ChuDe.chu(g, "HP " + Math.max(0, c.hp) + "/" + c.hpMax, xt, yb + hb + 3, ChuDe.PHU, ChuDe.CHU_PHU, 0);
    }

    static int ttMau(int tt) {
        switch (tt) {
            case 1:
                return KieuO.mauHe(HePoke.LUA);
            case 2:
                return KieuO.mauHe(HePoke.DOC);
            case 3:
                return KieuO.mauHe(HePoke.DIEN);
            case 4:
                return ChuDe.XAM;
            default:
                return KieuO.mauHe(HePoke.BANG);
        }
    }

    void veNhatKy(Graphics g, int x, int y, int w, int h) {
        if (h < ChuDe.cao(ChuDe.PHU) + 8) {
            return;
        }
        Bang.the(g, x, y, w, h);
        int lp = ChuDe.cao(ChuDe.PHU);
        int n = Math.max(1, (h - 8) / (lp + 1));
        int dau = Math.max(0, DauTruong.NHAT_KY.size() - n);
        int yy = y + 4;
        for (int i = dau; i < DauTruong.NHAT_KY.size(); i++) {
            String s = (String) DauTruong.NHAT_KY.elementAt(i);
            boolean moi = i == DauTruong.NHAT_KY.size() - 1;
            ChuDe.chu(g, ChuDe.cat(s, ChuDe.PHU, w - 16), x + 8, yy, ChuDe.PHU, moi ? ChuDe.CHU : ChuDe.CHU_PHU, 0);
            yy += lp + 1;
        }
    }

    void veChieu(Graphics g, int x, int y, int w, int hn, int hd) {
        int k = Bang.cach();
        int wn = (w - k) / 2;
        boolean het = DauTruong.hetPp();
        boolean cho = DauTruong.choMayChu || DauTruong.tuDong;
        for (int o = 0; o < 4; o++) {
            final int oo = o;
            DauTruong.Chieu c = DauTruong.chieu(o);
            int xn = x + (o % 2) * (wn + k), yn = y + (o / 2) * (hn + k);
            boolean dung = c != null && (c.pp > 0 || het) && !cho;
            Bang.nut(g, xn, yn, wn, hn, null, dung ? Bang.TRUNG : Bang.TAT, new IAction() {
                public void perform() {
                    DauTruong.hanhDong(0, oo);
                }
            }, "chieu-" + o);
            if (c == null) {
                continue;
            }
            // nhãn hệ: khối màu hệ chữ trắng viền (cùng màu KieuO của Pokédex / Hộp PC)
            String he = KieuO.tenHe(c.he);
            int wh = Math.min(wn / 3, ChuDe.rong(he, ChuDe.PHU) + 12);
            int hh = ChuDe.cao(ChuDe.PHU) + 2;
            GiaoDien.vien(g, xn + 6, yn + 5, wh, hh, KieuO.mauHe(c.he), null);
            ChuDe.chuVienGiua(g, ChuDe.cat(he, ChuDe.PHU, wh - 6), xn + 6 + wh / 2, yn + 5, hh, ChuDe.PHU, 0xFFFFFF, ChuDe.VIEN, 2);
            String pp = "PP " + c.pp + "/" + c.ppMax;
            int wpp = ChuDe.rong(pp, ChuDe.PHU);
            ChuDe.chu(g, pp, xn + wn - 8, yn + 5, ChuDe.PHU, c.pp > 0 ? ChuDe.CHU_PHU : ChuDe.DO, 1);
            String ten = c.ten + (c.uyLuc > 0 ? " · " + c.uyLuc : " · " + DauTruong.TEN_LOAI[Math.max(0, Math.min(2, c.loai))]);
            int ytn = yn + 5 + hh + 1;
            if (ytn + ChuDe.cao(ChuDe.NHAN) <= yn + hn - 2) {
                ChuDe.chu(g, ChuDe.cat(ten, ChuDe.NHAN, wn - 14), xn + 8, ytn, ChuDe.NHAN, dung ? ChuDe.CHU : ChuDe.CHU_NHAT, 0);
            } else {
                ChuDe.chu(g, ChuDe.cat(c.ten, ChuDe.PHU, wn - wh - wpp - 26), xn + 12 + wh, yn + 5, ChuDe.PHU, ChuDe.CHU, 0);
            }
        }
        int yd = y + 2 * (hn + k);
        int wGio = Math.max(Bang.co(48), ChuDe.rong("20 giây", ChuDe.SO) + 16);
        int wTd = Math.max(Bang.co(76), ChuDe.rong("Tự động: bật", ChuDe.SO) + 20);
        int wDoi = (w - wGio - wTd - 3 * k) / 2;
        int nDoi = 0;
        for (int i = 0; i < 3; i++) {
            if (i == DauTruong.dangRa[0]) {
                continue;
            }
            final int ii = i;
            DauTruong.Con c = DauTruong.DOI[0][i];
            boolean dung = c != null && c.hp > 0 && !cho;
            String chu = c == null ? "" : (c.hp > 0 ? "Đổi: " + c.ten : c.ten + " (gục)");
            Bang.nut(g, x + nDoi * (wDoi + k), yd, wDoi, hd, ChuDe.cat(chu, ChuDe.SO, wDoi - 14), dung ? Bang.PHU : Bang.TAT, new IAction() {
                public void perform() {
                    DauTruong.hanhDong(1, ii);
                }
            }, "doi-" + nDoi);
            nDoi++;
        }
        int xTd = x + 2 * (wDoi + k);
        Bang.nut(g, xTd, yd, wTd, hd, DauTruong.tuDong ? "Tự động: bật" : "Tự động", DauTruong.tuDong ? Bang.CHINH : Bang.GO, new IAction() {
            public void perform() {
                DauTruong.datTuDong(!DauTruong.tuDong);
            }
        }, "tu-dong");
        int giay = DauTruong.giayCon();
        GiaoDien.vien(g, x + w - wGio, yd, wGio, hd, giay <= 5 ? ChuDe.DO : ChuDe.CHINH_TOI, null);
        ChuDe.chuVienGiua(g, DauTruong.choMayChu ? "..." : giay + " giây", x + w - wGio / 2, yd, hd, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
    }

    void veKetQua(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int lh = ChuDe.cao(ChuDe.TIEU_DE), ln = ChuDe.cao(ChuDe.NHAN), lp = ChuDe.cao(ChuDe.PHU);
        int hb = Bang.co(30);
        int hThe = Math.min(h - hb - k - 4, lh + 3 * ln + 2 * lp + 40);
        int yt = y + 4;
        Bang.the(g, x, yt, w, hThe);
        int cx = x + w / 2;
        int yy = yt + 10;
        GiaoDien.vien(g, cx - w / 4, yy, w / 2, lh + 6, DauTruong.kqThang ? ChuDe.LA : ChuDe.DO, null);
        ChuDe.chuVienGiua(g, DauTruong.kqThang ? "THẮNG" : "THUA", cx, yy, lh + 6, ChuDe.TIEU_DE, 0xFFFFFF, ChuDe.VIEN, 2);
        yy += lh + 12;
        int d = DauTruong.kqDiemSau - DauTruong.kqDiemTruoc;
        ChuDe.chu(g, "Điểm " + DauTruong.kqDiemTruoc + " thành " + DauTruong.kqDiemSau + " (" + (d >= 0 ? "+" : "") + d + ")", cx, yy, ChuDe.NHAN,
                d >= 0 ? ChuDe.LA_TOI : ChuDe.DO_TOI, 2);
        yy += ln + 4;
        ChuDe.chu(g, DauTruong.kqLuong > 0 ? "+" + DauTruong.kqLuong + " lượng khoá" : "Không có lượng khoá trận này", cx, yy, ChuDe.NHAN, ChuDe.CHU, 2);
        yy += ln + 4;
        ChuDe.chu(g, ChuDe.cat(DauTruong.kqChu, ChuDe.PHU, w - 20), cx, yy, ChuDe.PHU, ChuDe.CHU_PHU, 2);
        yy += lp + 2;
        ChuDe.chu(g, "Còn " + DauTruong.kqLuotCon + " lượt hôm nay", cx, yy, ChuDe.PHU, ChuDe.CHU_PHU, 2);
        int yl = yt + hThe + k;
        int hl = y + h - hb - k - yl;
        if (hl > lp + 8) {
            veNhatKy(g, x, yl, w, hl);
        }
        int wb = (w - k) / 2;
        Bang.nut(g, x, y + h - hb, wb, hb, "Về Đấu trường", Bang.CHINH, new IAction() {
            public void perform() {
                veDauTruong();
            }
        }, "kq-ve");
        Bang.nut(g, x + wb + k, y + h - hb, w - wb - k, hb, "Đóng", Bang.TRUNG, new IAction() {
            public void perform() {
                DauTruong.tranId = 0;
                Canvas.gameScr.switchToMe();
            }
        }, "kq-dong");
    }
}
