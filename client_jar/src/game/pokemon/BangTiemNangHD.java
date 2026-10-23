package game.pokemon;

import game.model.IAction;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.Dialog;
import javax.microedition.lcdui.Graphics;

/**
 * 0.9.5.1: bảng CỘNG ĐIỂM TIỀM NĂNG (PC / APK) - nút "Tiềm năng" của tab Chỉ số mở bảng này (trước đây gọi bảng J2ME
 * WindowInfoScr nên không mở gì). 5 chỉ số: giá trị hiện tại, số điểm định cộng với − / +, "Còn n điểm"; Xác nhận gửi
 * lệnh 34 (GameService.addBasePoint) cho từng chỉ số - máy chủ trừ điểm, tính lại, gửi lại thông tin nhân vật
 * (TrangThaiNguoiChoi.TAT_CA làm mới bảng Chỉ số). J2ME vẫn dùng WindowInfoScr.
 * Tạm thời cho tới khi "Điểm huấn luyện" (cân bằng Q4) thay điểm tiềm năng tự do.
 */
public final class BangTiemNangHD extends Dialog {
    static BangTiemNangHD me;
    static final String[] TEN = {"Sức mạnh", "Khéo léo", "Tinh thần", "Sức khoẻ", "May mắn"};
    static final String[] MO_TA = {"tăng tấn công", "tăng né / chính xác", "tăng PP, sức chiêu", "tăng HP, phòng thủ", "tăng chí mạng"};

    final int[] them = new int[5];
    final IAction[] tru = new IAction[5], cong = new IAction[5];
    final IAction dong, xacNhan;
    final IAction[] bamNut = new IAction[2];
    static final String[] NUT = {"Đóng", "Xác nhận"};
    static final int[] KIEU_NUT = {Bang.TRUNG, Bang.CHINH};
    static final String[] TEN_NUT = {"tn-dong", "tn-xac-nhan"};

    private BangTiemNangHD() {
        for (int i = 0; i < 5; i++) {
            final int k = i;
            this.tru[i] = new IAction() {
                public void perform() {
                    if (BangTiemNangHD.this.them[k] > 0) {
                        BangTiemNangHD.this.them[k]--;
                    }
                }
            };
            this.cong[i] = new IAction() {
                public void perform() {
                    if (BangTiemNangHD.this.conLai() > 0) {
                        BangTiemNangHD.this.them[k]++;
                    }
                }
            };
        }
        this.dong = new IAction() {
            public void perform() {
                if (Canvas.currentDialog == BangTiemNangHD.this) {
                    Canvas.endDlg();
                }
            }
        };
        this.xacNhan = new IAction() {
            public void perform() {
                BangTiemNangHD.this.gui();
            }
        };
        this.bamNut[0] = this.dong;
        this.bamNut[1] = this.xacNhan;
        this.anNutLenh = true;
    }

    /** Mở bảng (nút "Tiềm năng"). */
    public static void mo() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        if (me == null) {
            me = new BangTiemNangHD();
        }
        for (int i = 0; i < 5; i++) {
            me.them[i] = 0;
        }
        AmThanh.phat(AmThanh.MO);
        me.show();
    }

    /** Số điểm tiềm năng chưa dùng (chấm đỏ trên nút). */
    public static int diemChuaDung() {
        MainChar m = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        return m == null ? 0 : Math.max(0, m.basePointLeft);
    }

    /** Điểm chưa dùng của nút tab Chỉ số: Điểm huấn luyện khi máy chủ bật (HocChieu), không thì điểm tiềm năng. */
    /** Nhãn nút góc trái thẻ nhân vật: "Huấn luyện" khi máy chủ bật Điểm huấn luyện, ngược lại "Tiềm năng". */
    public static String nhanNut() {
        return HocChieu.batHL ? "Huấn luyện" : "Tiềm năng";
    }

    /** Câu lên cấp (GameScr.onLevelUp): Điểm huấn luyện -> "+N Điểm huấn luyện", ngược lại tiềm năng như cũ. */
    public static String cauLenCap() {
        return HocChieu.batHL ? "Chúc mừng! Bạn vừa lên cấp: +" + HocChieu.diemMoiCap + " Điểm huấn luyện. Mở Menu, chọn Pokémon, bấm Huấn luyện để dùng."
                : "Chúc mừng! Bạn vừa lên cấp. Mở Menu, chọn Pokémon để cộng điểm tiềm năng và kỹ năng.";
    }

    public static int diemNut() {
        return HocChieu.batHL ? HocChieu.conLai() : diemChuaDung();
    }

    /** Chấm đỏ góc phải trên nút (x, y, w) khi còn điểm chưa dùng. */
    public static void chamDo(Graphics g, int x, int y, int w) {
        if (diemNut() <= 0) {
            return;
        }
        ChuDe.cham(g, x + w - 4, y + 4);   // 0.9.6: chấm chung (ChamMoi / KiemChamMoi)
    }

    int tong() {
        int t = 0;
        for (int i = 0; i < 5; i++) {
            t += this.them[i];
        }
        return t;
    }

    int conLai() {
        return diemChuaDung() - tong();
    }

    void gui() {
        int t = tong();
        if (t <= 0) {
            this.dong.perform();
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (this.them[i] > 0) {
                GameService.gI().addBasePoint(i, this.them[i]);
                this.them[i] = 0;
            }
        }
        this.dong.perform();
        DungDo.bao("Đã cộng " + t + " điểm tiềm năng.");
    }

    static int giaTri(MainChar m, int i) {
        switch (i) {
            case 0:
                return m.strength;
            case 1:
                return m.agility;
            case 2:
                return m.spirit;
            case 3:
                return m.health;
            default:
                return m.luck;
        }
    }

    public void paint(Graphics g) {
        MainChar m = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        if (m == null) {
            return;
        }
        int nh = HopThongBao.caoNut();
        int k = Bang.cach();
        int lp = ChuDe.cao(ChuDe.THUONG) + 6;
        int hh = Math.max(VungCham.MIN, ChuDe.cao(ChuDe.NHAN) + ChuDe.cao(ChuDe.PHU) + 6);
        // màn thấp (cửa sổ PC nhỏ): hàng một dòng, bỏ dòng mô tả
        boolean moTa = HopThongBao.caoDauA() + lp + 5 * (hh + k) + nh + 2 * HopThongBao.pad() <= HopThongBao.caoToiDa() - 16;
        if (!moTa) {
            hh = Math.max(VungCham.MIN, ChuDe.cao(ChuDe.NHAN) + 6);
        }
        int ww = Math.min(Bang.phai() - Bang.trai() - 16, ChuDe.dt() ? 360 : 280);
        HopThongBao.datHuyHieu(BieuTuong.TIEN_ICH, ChuDe.XANH, false);
        int[] kh = HopThongBao.khung(g, ww, lp + 5 * (hh + k) + nh + HopThongBao.pad(), "Điểm tiềm năng", this.dong, false);
        int x = kh[5], y = kh[4], w = kh[6];
        int con = conLai();
        ChuDe.chuGiua(g, "Còn " + con + " điểm" + (diemChuaDung() == 0 ? " · lên cấp để nhận thêm" : ""), x + w / 2, y, lp, ChuDe.THUONG,
                con > 0 ? ChuDe.DO : ChuDe.CHU_PHU, 2);
        y += lp;
        int s = hh - 4;
        for (int i = 0; i < 5; i++) {
            Bang.hang(g, x, y, w, hh, i, this.them[i] > 0, null, null);
            if (moTa) {
                ChuDe.chu(g, TEN[i], x + 8, y + 3, ChuDe.NHAN, ChuDe.CHU, 0);
                ChuDe.chu(g, MO_TA[i], x + 8, y + 3 + ChuDe.cao(ChuDe.NHAN), ChuDe.PHU, ChuDe.CHU_PHU, 0);
            } else {
                ChuDe.chuGiua(g, TEN[i], x + 8, y, hh, ChuDe.NHAN, ChuDe.CHU, 0);
            }
            // [-] giá trị (+n) [+]
            int xc = x + w - 4 - s;
            Bang.nut(g, xc, y + 2, s, s, "+", con > 0 ? Bang.CHINH : Bang.TAT, this.cong[i], "tn-cong-" + i);
            String gt = String.valueOf(giaTri(m, i)) + (this.them[i] > 0 ? " +" + this.them[i] : "");
            int wg = Math.max(ChuDe.rong("999 +99", ChuDe.SO), ChuDe.rong(gt, ChuDe.SO)) + 8;
            ChuDe.chuGiua(g, gt, xc - k - wg / 2, y, hh, ChuDe.SO, this.them[i] > 0 ? ChuDe.LA_TOI : ChuDe.CHU, 2);
            Bang.nut(g, xc - k - wg - k - s, y + 2, s, s, "−", this.them[i] > 0 ? Bang.TRUNG : Bang.TAT, this.tru[i], "tn-tru-" + i);
            y += hh + k;
        }
        HopThongBao.hangNut(g, kh[0], kh[1] + kh[3] - HopThongBao.pad() - nh, kh[2], NUT, KIEU_NUT, this.bamNut, TEN_NUT);
    }

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
