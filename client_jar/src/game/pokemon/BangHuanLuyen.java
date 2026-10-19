package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import game.render.screen.Dialog;
import javax.microedition.lcdui.Graphics;

/**
 * 0.9.8: bảng ĐIỂM HUẤN LUYỆN (thay bảng cộng điểm tiềm năng tự do khi máy chủ bật - HocChieu.batHL). Mỗi dòng chỉ số
 * (HP, Tấn công, Phòng thủ, PP - máy chủ gửi): điểm / trần, hiệu quả "+12,0%" và mức tăng của điểm kế tiếp (giảm dần
 * sau ngưỡng), − / + chọn điểm định cộng; "Xác nhận" gửi -122 loại 1; "Tẩy điểm" (lần đầu miễn phí, sau đó giá xu, chờ)
 * hỏi lại rồi gửi -122 loại 2. Máy chủ kiểm trần / tổng, trả thông báo + gói mới.
 */
public final class BangHuanLuyen extends Dialog {
    static BangHuanLuyen me;
    int[] them = new int[0];
    final IAction dong, xacNhan, tayDiem;
    final IAction[] bamNut = new IAction[3];
    static final int[] KIEU_NUT = {Bang.TRUNG, Bang.TRUNG, Bang.CHINH};
    static final String[] TEN_NUT = {"hl-dong", "hl-tay", "hl-xac-nhan"};
    IAction[] tru = new IAction[0], cong = new IAction[0];
    /** vùng vẽ khung lần cuối {x, y, w, h} (kiểm thử) */
    public static final int[] VUA_VE = new int[4];

    private BangHuanLuyen() {
        this.dong = new IAction() {
            public void perform() {
                if (Canvas.currentDialog == BangHuanLuyen.this) {
                    Canvas.endDlg();
                }
            }
        };
        this.xacNhan = new IAction() {
            public void perform() {
                BangHuanLuyen.this.gui();
            }
        };
        this.tayDiem = new IAction() {
            public void perform() {
                BangHuanLuyen.this.hoiTay();
            }
        };
        this.bamNut[0] = this.dong;
        this.bamNut[1] = this.tayDiem;
        this.bamNut[2] = this.xacNhan;
        this.anNutLenh = true;
    }

    public static BangHuanLuyen gI() {
        if (me == null) {
            me = new BangHuanLuyen();
        }
        return me;
    }

    /** Mở bảng (nút "Huấn luyện" tab Chỉ số). */
    public static void mo() {
        BangHuanLuyen b = gI();
        b.dongBo();
        for (int i = 0; i < b.them.length; i++) {
            b.them[i] = 0;
        }
        HocChieu.xin();
        AmThanh.phat(AmThanh.MO);
        b.show();
    }

    /** Gói mới: số dòng đổi thì dựng lại nút; điểm định cộng vượt còn lại thì bỏ. */
    static void khiNhan() {
        if (me != null) {
            me.dongBo();
            for (int i = 0; i < me.them.length; i++) {
                me.them[i] = 0;
            }
        }
    }

    void dongBo() {
        int n = HocChieu.soDong;
        if (this.them.length == n) {
            return;
        }
        this.them = new int[n];
        this.tru = new IAction[n];
        this.cong = new IAction[n];
        for (int i = 0; i < n; i++) {
            final int k = i;
            this.tru[i] = new IAction() {
                public void perform() {
                    if (k < BangHuanLuyen.this.them.length && BangHuanLuyen.this.them[k] > 0) {
                        BangHuanLuyen.this.them[k]--;
                    }
                }
            };
            this.cong[i] = new IAction() {
                public void perform() {
                    if (k < BangHuanLuyen.this.them.length && BangHuanLuyen.this.coTheCong(k)) {
                        BangHuanLuyen.this.them[k]++;
                    }
                }
            };
        }
    }

    boolean coTheCong(int i) {
        return conLai() > 0 && i < HocChieu.soDong && HocChieu.diem[i] + this.them[i] < HocChieu.tran[i];
    }

    int tong() {
        int t = 0;
        for (int i = 0; i < this.them.length; i++) {
            t += this.them[i];
        }
        return t;
    }

    int conLai() {
        return HocChieu.conLai() - tong();
    }

    void gui() {
        int t = tong();
        if (t <= 0) {
            this.dong.perform();
            return;
        }
        HocChieu.phanBo(this.them);
        for (int i = 0; i < this.them.length; i++) {
            this.them[i] = 0;
        }
        this.dong.perform();
    }

    /** Chữ nút Tẩy điểm theo trạng thái máy chủ. */
    static String chuTay() {
        int cho = HocChieu.choCon();
        if (cho > 0) {
            int phut = (cho + 59) / 60;
            return "Chờ " + phut / 60 + "h" + (phut % 60 < 10 ? "0" : "") + phut % 60;
        }
        return HocChieu.mienPhi || HocChieu.giaTay <= 0 ? "Tẩy (miễn phí)" : "Tẩy điểm";
    }

    void hoiTay() {
        if (HocChieu.daDung <= 0) {
            DungDo.bao("Chưa phân bổ điểm nào để tẩy.");
            return;
        }
        int cho = HocChieu.choCon();
        if (cho > 0) {
            int phut = (cho + 59) / 60;
            DungDo.bao("Tẩy điểm lại sau " + phut / 60 + " giờ " + phut % 60 + " phút.");
            return;
        }
        IAction lam = new IAction() {
            public void perform() {
                HocChieu.tay();
            }
        };
        String nd = "Trả lại " + HocChieu.daDung + " Điểm huấn luyện để phân bổ lại.";
        if (HocChieu.mienPhi || HocChieu.giaTay <= 0) {
            HopXacNhan.hoi("Tẩy điểm", nd + " Lần đầu miễn phí.", "Tẩy điểm", lam, null);
        } else {
            HopXacNhan.hoiGia("Tẩy điểm", nd, "Tẩy điểm", TienTe.XU, HocChieu.giaTay, lam);
        }
    }

    /** Dấu ngăn: font ảnh J2ME không có "·". */
    static String ngan() {
        return ChuUI.bo != null ? " · " : " - ";
    }

    public void paint(Graphics g) {
        dongBo();
        int n = HocChieu.soDong;
        int nh = HopThongBao.caoNut();
        int k = Bang.cach();
        int pad = HopThongBao.pad();
        int lp = ChuDe.cao(ChuDe.THUONG) + 6;
        int vung = Bang.phai() - Bang.trai();
        int toiDa = Math.min(vung - 16, vung * 78 / 100);
        // chỗ cho các hàng: cao tối đa của thẻ trừ đầu, dòng "Còn n điểm", hàng nút
        int choHang = HopThongBao.caoToiDa() - (ChuDe.dt() ? 30 : 22) / 2 - HopThongBao.caoDauA() - lp - nh - 3 * pad - 4;
        int hMot = Math.max(VungCham.MIN, ChuDe.cao(ChuDe.NHAN) + 6);
        int hHai = Math.max(VungCham.MIN, ChuDe.cao(ChuDe.NHAN) + ChuDe.cao(ChuDe.PHU) + 6);
        int cot = 1;
        int hh = hHai;
        boolean moTa = true;
        if (n * (hh + k) > choHang) {
            moTa = false;
            hh = hMot;
            if (n * (hh + k) > choHang && n > 1) {
                cot = 2;   // màn thấp (điện thoại ngang): 2 cột
                int hang = (n + 1) / 2;
                if (hang * (hHai + k) <= choHang) {
                    moTa = true;
                    hh = hHai;
                }
            }
        }
        int soHang = (n + cot - 1) / cot;
        if (soHang * (hh + k) > choHang) {
            hh = Math.max(ChuDe.cao(ChuDe.NHAN) + 4, choHang / Math.max(1, soHang) - k);
        }
        int ww = Math.min(toiDa, cot == 2 ? (ChuDe.dt() ? 760 : 560) : (ChuDe.dt() ? 380 : 300));
        HopThongBao.datHuyHieu(BieuTuong.TIEN_ICH, ChuDe.CHINH, false);
        int[] kh = HopThongBao.khung(g, ww, lp + soHang * (hh + k) + nh + pad, "Điểm huấn luyện", this.dong, false);
        VUA_VE[0] = kh[0];
        VUA_VE[1] = kh[1];
        VUA_VE[2] = kh[2];
        VUA_VE[3] = kh[3];
        int x0 = kh[5], y = kh[4], w0 = kh[6];
        int con = conLai();
        String dau = "Còn " + con + " điểm" + ngan() + "tổng " + HocChieu.tong + " (" + HocChieu.diemMoiCap + " / cấp"
                + (HocChieu.huyHieu > 0 ? " + " + HocChieu.huyHieu + " huy hiệu" : "") + ")";
        ChuDe.chuGiua(g, ChuDe.cat(dau, ChuDe.THUONG, w0), x0 + w0 / 2, y, lp, ChuDe.THUONG, con > 0 ? ChuDe.DO : ChuDe.CHU_PHU, 2);
        y += lp;
        int w = cot == 2 ? (w0 - k) / 2 : w0;
        int s = Math.min(hh - 4, Math.max(VungCham.MIN, ChuDe.cao(ChuDe.NHAN) + 4));
        for (int i = 0; i < n; i++) {
            int x = x0 + (i % cot) * (w + k);
            int yr = y + (i / cot) * (hh + k);
            int d = HocChieu.diem[i] + this.them[i];
            Bang.hang(g, x, yr, w, hh, i, this.them[i] > 0, null, null);
            int ys = yr + (hh - s) / 2;
            int xc = x + w - 4 - s;
            Bang.nut(g, xc, ys, s, s, "+", coTheCong(i) ? Bang.CHINH : Bang.TAT, this.cong[i], "hl-cong-" + i);
            String gt = d + "/" + HocChieu.tran[i] + (this.them[i] > 0 ? " +" + this.them[i] : "");
            int wg = Math.max(ChuDe.rong("80/80 +99", ChuDe.SO), ChuDe.rong(gt, ChuDe.SO)) + 8;
            ChuDe.chuGiua(g, gt, xc - k - wg / 2, yr, hh, ChuDe.SO, this.them[i] > 0 ? ChuDe.LA_TOI : ChuDe.CHU, 2);
            int xt = xc - k - wg - k - s;
            Bang.nut(g, xt, ys, s, s, "-", this.them[i] > 0 ? Bang.TRUNG : Bang.TAT, this.tru[i], "hl-tru-" + i);
            int wChu = xt - x - 12;
            String hq = "+" + HocChieu.phanTram(HocChieu.phanNghin(i, d));
            String ke = d >= HocChieu.tran[i] ? "tối đa" : "điểm kế +" + HocChieu.phanTram(HocChieu.phanNghin(i, d + 1) - HocChieu.phanNghin(i, d));
            if (moTa) {
                ChuDe.chu(g, ChuDe.cat(HocChieu.ten[i] + " " + hq, ChuDe.NHAN, wChu), x + 8, yr + 3, ChuDe.NHAN, ChuDe.CHU, 0);
                ChuDe.chu(g, ChuDe.cat(ke + ngan() + HocChieu.moTa[i], ChuDe.PHU, wChu), x + 8, yr + 3 + ChuDe.cao(ChuDe.NHAN), ChuDe.PHU,
                        ChuDe.CHU_PHU, 0);
            } else {
                ChuDe.chuGiua(g, ChuDe.cat(HocChieu.ten[i] + " " + hq, ChuDe.NHAN, wChu), x + 8, yr, hh, ChuDe.NHAN, ChuDe.CHU, 0);
            }
        }
        String[] nut = {"Đóng", chuTay(), "Xác nhận"};
        HopThongBao.hangNut(g, kh[0], kh[1] + kh[3] - pad - nh, kh[2], nut, KIEU_NUT, this.bamNut, TEN_NUT);
        // đáy hàng cuối (kiểm thử: không chồng hàng nút)
        DAY_HANG = y + soHang * (hh + k) - k;
        DINH_NUT = kh[1] + kh[3] - pad - nh;
    }

    /** kiểm thử: đáy hàng chỉ số cuối / đỉnh hàng nút ở khung vừa vẽ */
    public static int DAY_HANG, DINH_NUT;

    public void keyPress(int keyCode) {
        if (keyCode == -5 || keyCode == 10 || keyCode == 13) {
            gui();
        } else if (keyCode == -7 || keyCode == -6 || keyCode == 27) {
            this.dong.perform();
        }
    }

    public void update() {
    }
}
