package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import game.render.screen.Dialog;
import javax.microedition.lcdui.Graphics;

/**
 * HỘP THÔNG BÁO CHUNG 0.9.5 (một thành phần cho MsgDlg, HopXacNhan, thông báo máy chủ, "Xem" của biểu ngữ):
 * dải đầu (biểu tượng + tiêu đề) + nút X, thân có lề, TỰ CO theo nội dung trong giới hạn bề ngang / cao, CUỘN khi dài
 * (kéo / con lăn / phím lên xuống), hàng nút to dưới. Luôn trọn trong vùng an toàn, không bao giờ bị cắt. Hộp thông
 * tin (chỉ OK): chạm ra ngoài = đóng; Back / Esc = đóng; Enter = nút chính.
 *
 * Vẽ bằng {@link #ve} (tĩnh) để HopXacNhan / MsgDlg dùng cùng bố cục.
 */
public final class HopThongBao extends Dialog {
    static HopThongBao me;

    public static final int THONG_TIN = 0, CANH_BAO = 1, LOI = 2, HOI = 3;

    String tieuDe, noiDung;
    int kieu;
    String[] nut;
    int[] kieuNut;
    IAction[] viec;
    IAction dong;
    /** việc của nút / đóng dựng sẵn lúc mở (không tạo đối tượng mỗi khung vẽ) */
    IAction[] bam;
    IAction dongHop;
    boolean ngoaiDong;
    final Cuon cuon = new Cuon();
    /** dòng đã ngắt (nhớ theo nội dung + bề ngang) */
    String[] dong0;
    String khoaNgat;
    int rongNgat;

    public static HopThongBao gI() {
        if (me == null) {
            me = new HopThongBao();
        }
        return me;
    }

    public static boolean dangMo() {
        return me != null && Canvas.currentDialog == me;
    }

    /** Hộp thông tin một nút OK (đóng). */
    /** MsgDlg (lệnh trái / giữa / phải của game) -> hộp chung: nút cuối = lệnh chính; X / Back = lệnh phải (huỷ). */
    public static boolean tuMsgDlg(String[] dong, game.model.Command trai, game.model.Command giua, game.model.Command phai) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; dong != null && i < dong.length; i++) {
            if (dong[i] != null && dong[i].length() > 0) {
                b.append(b.length() > 0 ? " " : "").append(dong[i].trim());
            }
        }
        java.util.Vector ten = new java.util.Vector(), viec = new java.util.Vector();
        game.model.Command[] ds = {phai, giua, trai};
        for (int i = 0; i < ds.length; i++) {
            game.model.Command c = ds[i];
            if (c == null || c.caption == null || c.caption.length() == 0) {
                continue;
            }
            boolean trung = false;
            for (int k = 0; k < viec.size(); k++) {
                if (viec.elementAt(k) == c.action) {
                    trung = true;   // hai lệnh cùng một việc: một nút
                }
            }
            if (trung) {
                continue;
            }
            ten.addElement(c.caption);
            viec.addElement(c.action);
        }
        int n = ten.size();
        String[] nut = new String[n];
        int[] kn = new int[n];
        IAction[] vc = new IAction[n];
        for (int i = 0; i < n; i++) {
            nut[i] = (String) ten.elementAt(i);
            vc[i] = (IAction) viec.elementAt(i);
            kn[i] = i == n - 1 ? Bang.XAC_NHAN : Bang.TRUNG;
        }
        IAction huy = phai != null && phai.action != null ? phai.action : (n == 1 ? vc[0] : null);
        mo(null, b.toString(), n > 1 ? HOI : THONG_TIN, nut, kn, vc, huy);
        return true;
    }

    public static void bao(String tieuDe, String noiDung) {
        mo(tieuDe, noiDung, THONG_TIN, new String[]{"OK"}, new int[]{Bang.XAC_NHAN}, new IAction[]{null}, null);
    }

    /**
     * Mở hộp: nut[i] / kieuNut[i] (Bang.CHINH...) / viec[i] (null = chỉ đóng); dong = việc khi đóng bằng X / Back /
     * chạm ngoài (null = chỉ đóng). Nút CUỐI là nút chính (Enter).
     */
    public static void mo(String tieuDe, String noiDung, int kieu, String[] nut, int[] kieuNut, IAction[] viec, IAction dong) {
        HopThongBao h = gI();
        h.tieuDe = tieuDe == null || tieuDe.length() == 0 ? tieuDeTheoNoiDung(noiDung, kieu) : tieuDe;
        h.noiDung = noiDung == null ? "" : noiDung;
        h.kieu = kieu;
        h.nut = nut;
        h.kieuNut = kieuNut;
        h.viec = viec;
        h.dong = dong;
        h.left = null;
        h.center = null;
        h.right = null;
        h.cuon.den(0);
        int n = nut == null ? 0 : nut.length;
        h.bam = new IAction[n];
        for (int i = 0; i < n; i++) {
            final int k = i;
            h.bam[i] = new IAction() {
                public void perform() {
                    me.xong(me.viec[k]);
                }
            };
        }
        h.dongHop = new IAction() {
            public void perform() {
                me.xong(me.dong);
            }
        };
        // hộp thông tin một nút: chạm ra ngoài = đóng; hộp nhiều lựa chọn: phải chọn nút / X
        h.ngoaiDong = n <= 1;
        h.show();
    }

    /** Tiêu đề theo nội dung khi máy chủ không gửi tiêu đề (tiến hoá / lên cấp / hoàn thành / quà...). */
    static String tieuDeTheoNoiDung(String noiDung, int kieu) {
        String t = noiDung == null ? "" : noiDung.toLowerCase();
        if (kieu == THONG_TIN) {
            if (t.indexOf("tiến hoá thành") >= 0 || t.indexOf("tiến hóa thành") >= 0) {
                return "Tiến hoá!";
            }
            if (t.indexOf("lên cấp") >= 0) {
                return "Lên cấp!";
            }
            if (t.indexOf("hoàn thành nhiệm vụ") >= 0) {
                return "Hoàn thành nhiệm vụ";
            }
            if (t.indexOf("giftcode") >= 0 && t.indexOf("thành công") >= 0) {
                return "Đổi quà thành công";
            }
            if (t.indexOf("bảo trì") >= 0) {
                return "Bảo trì máy chủ";
            }
        }
        return tieuDeMacDinh(kieu);
    }

    static String tieuDeMacDinh(int kieu) {
        switch (kieu) {
            case CANH_BAO:
                return "Lưu ý";
            case LOI:
                return "Không thực hiện được";
            case HOI:
                return "Xác nhận";
            default:
                return "Thông báo";
        }
    }

    static int mauKieu(int kieu) {
        switch (kieu) {
            case CANH_BAO:
                return ChuDe.CAM;
            case LOI:
                return ChuDe.DO;
            case HOI:
                return ChuDe.CHINH;
            default:
                return ChuDe.CHINH;
        }
    }

    void xong(IAction a) {
        if (Canvas.currentDialog == this) {
            Canvas.currentDialog = null;
        }
        Canvas.clearKeyPressed();
        if (a != null) {
            a.perform();
        }
    }

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        int ww = rongHop(this.tieuDe, this.noiDung, this.nut, 0);
        if (this.dong0 == null || !this.noiDung.equals(this.khoaNgat) || this.rongNgat != ww) {
            this.dong0 = ngatDanhSach(this.noiDung, ww - 2 * pad());
            this.khoaNgat = this.noiDung;
            this.rongNgat = ww;
        }
        chonHuyHieu(this.tieuDe, this.noiDung, this.kieu);
        ve(g, ww, this.tieuDe, mauKieu(this.kieu), this.dong0, null, this.nut, this.kieuNut, this.bam, this.dongHop, this.ngoaiDong, this.cuon, TEN_NUT);
    }

    /** Lề trong hộp (px logic): ~18 px thật ở PC 1080p, ~16 dp ở điện thoại. */
    public static int pad() {
        return ChuDe.dt() ? 10 : 7;
    }

    /** Bề ngang tối đa: PC 40% màn, điện thoại ngang ~50%, máy tính bảng (gần vuông) ~62%; trọn vùng an toàn. */
    public static int rongToiDa() {
        int vung = Bang.phai() - Bang.trai();
        int tl = !ChuDe.dt() ? 40 : Canvas.w * 10 > Canvas.h * 16 ? 50 : 62;
        return Math.min(vung - 16, Math.max(rongToiThieu0(), Canvas.w * tl / 100));
    }

    static int rongToiThieu0() {
        return ChuDe.dt() ? 200 : 140;
    }

    public static int rongToiThieu() {
        return Math.min(rongToiThieu0(), Bang.phai() - Bang.trai() - 16);
    }

    /** Cao tối đa: 70% vùng an toàn (dài hơn thì cuộn). */
    public static int caoToiDa() {
        return (Bang.duoi() - Bang.tren()) * 7 / 10;
    }

    /** Cao một dòng thân (giãn dòng ~1,4). */
    static int caoDong() {
        return Math.max(ChuDe.cao(ChuDe.THUONG) + 1, (int) (ChuDe.co(ChuDe.THUONG) * 1.45f + 0.5f));
    }

    /** Bề rộng một nút theo chữ (PC: nút co theo chữ, canh phải). */
    static int rongNut(String chu) {
        return Math.max(ChuDe.dt() ? 90 : 48, ChuDe.rong(chu, ChuDe.NHAN) + (ChuDe.dt() ? 32 : 20));
    }

    /** Bề ngang hộp CO THEO NỘI DUNG: tiêu đề + X, một dòng chữ không ngắt, hàng nút; kẹp [tối thiểu, tối đa]. */
    static int rongHop(String tieuDe, String noiDung, String[] nut, int wPhu) {
        int p = pad();
        int hd = ChuDe.caoDau();
        int w = ChuDe.rong(tieuDe == null ? "" : tieuDe, ChuDe.TIEU_DE) + hd * 2 + 16;
        int dongMot = 0;
        if (noiDung != null) {
            int i = 0;
            // đoạn dài nhất giữa các xuống dòng
            while (i <= noiDung.length()) {
                int j = noiDung.indexOf('\n', i);
                if (j < 0) {
                    j = noiDung.length();
                }
                dongMot = Math.max(dongMot, ChuDe.rong(noiDung.substring(i, j), ChuDe.THUONG));
                i = j + 1;
            }
        }
        w = Math.max(w, dongMot + 2 * p + 2 + (bieuTuongTinh >= 0 ? rongIcon() : 0));
        w = Math.max(w, wPhu + 2 * p);
        if (nut != null && nut.length > 0) {
            int k = Bang.cach(), wn = 0;
            for (int i = 0; i < nut.length; i++) {
                wn += rongNut(nut[i]) + (i > 0 ? k : 0);
            }
            w = Math.max(w, wn + 2 * p);
        }
        return Math.max(rongToiThieu(), Math.min(rongToiDa(), w));
    }

    /** Bề ngang thân chữ (mặc định, hộp rộng nhất). */
    static int rongThan() {
        return rongHop() - 2 * pad();
    }

    /** Bề ngang hộp mặc định (tối đa). */
    static int rongHop() {
        return rongToiDa();
    }

    /** Hiệu ứng mở (0.9.5): mốc mở hộp đang vẽ - lần vẽ cách lần trước > 300 ms = hộp mới. */
    private static long lucMo, lucVe;

    /**
     * Vẽ hộp chung (cả HopXacNhan): dòng chữ d (đã ngắt), dòng phụ tuỳ chọn (giá...) do người gọi vẽ trong vùng
     * trả về. Trả {x, y, w, h, yPhu} - yPhu = đáy phần chữ (để vẽ thêm, vd giá).
     */
    static final String[] TEN_NUT = {"hop-nut-0", "hop-nut-1", "hop-nut-2", "hop-nut-3"};

    static int[] ve(Graphics g, String tieuDe, int mau, String[] d, int[] hPhu, String[] nut, int[] kieuNut, IAction[] bam,
            IAction dong, boolean ngoaiDong, Cuon cuon) {
        return ve(g, rongHop(), tieuDe, mau, d, hPhu, nut, kieuNut, bam, dong, ngoaiDong, cuon, TEN_NUT);
    }

    /** màu nhấn thương hiệu (dải trên, nút chính), chữ tiêu đề */
    static final int MAU_NHAN = ChuDe.CHINH_TOI, MAU_TIEU_DE = ChuDe.CHU;
    /** biểu tượng loại cho lần vẽ kế (BieuTuong id, -1 = không) - người gọi đặt trước ve() */
    static int bieuTuong = -1;
    /** biểu tượng tính vào bề ngang (rongHop) */
    static int bieuTuongTinh = -1;

    /** Huy hiệu theo loại / nội dung: tin xanh dương, lưu ý cam, lỗi đỏ, thưởng / thành công vàng, tiến hoá tím. */
    static void chonHuyHieu(String tieuDe, String noiDung, int kieu) {
        String t = ((tieuDe == null ? "" : tieuDe) + " " + (noiDung == null ? "" : noiDung)).toLowerCase();
        if (kieu == LOI) {
            datHuyHieu(BieuTuong.LOI, ChuDe.DO, false);
        } else if (kieu == CANH_BAO || t.indexOf("mất kết nối") >= 0 || t.indexOf("bảo trì") >= 0) {
            datHuyHieu(BieuTuong.CANH_BAO, ChuDe.CAM, false);
        } else if (t.indexOf("tiến hoá") >= 0 || t.indexOf("tiến hóa") >= 0) {
            datHuyHieu(BieuTuong.SU_KIEN, 0x8E5BD8, false);
        } else if (t.indexOf("chúc mừng") >= 0 || t.indexOf("hoàn thành") >= 0 || t.indexOf("thành công") >= 0 || t.indexOf("phần thưởng") >= 0
                || t.indexOf("nhận được") >= 0) {
            datHuyHieu(BieuTuong.QUA, 0xF2B705, false);
        } else if (t.indexOf("sự kiện") >= 0) {
            datHuyHieu(BieuTuong.SU_KIEN, 0xF2B705, false);
        } else {
            datHuyHieu(BieuTuong.TIN, ChuDe.XANH, false);
        }
    }

    static int bieuTuongKieu(int kieu) {
        switch (kieu) {
            case CANH_BAO:
                return BieuTuong.CANH_BAO;
            case LOI:
                return BieuTuong.LOI;
            case HOI:
                return -1;
            default:
                return BieuTuong.TIN;
        }
    }

    /** hộp vừa vẽ {x, y, w, h} (kiểm thử: canh giữa / giới hạn cỡ) */
    public static final int[] VUA_VE = new int[4];

    /** Huy hiệu (kiểu A): biểu tượng BieuTuong + màu đĩa; nền nhạt (true) = biểu tượng màu trên đĩa sáng (hộp mua / xác nhận). */
    static int huyHieu = BieuTuong.TIN, mauHuy = ChuDe.XANH;
    static boolean huyNhat;

    /** Đặt huy hiệu cho lần vẽ kế. */
    public static void datHuyHieu(int bieu, int mau, boolean nhat) {
        huyHieu = bieu;
        mauHuy = mau;
        huyNhat = nhat;
    }

    /** Đường kính huy hiệu (px logic). */
    static int dkHuy() {
        return ChuDe.dt() ? 30 : 22;
    }

    /** Bo góc thẻ. */
    static int boThe() {
        return ChuDe.dt() ? 14 : 10;
    }

    /** Cao phần đầu (nửa huy hiệu + tiêu đề) trong thẻ. */
    static int caoDauA() {
        // đủ cao cho vùng chạm nút X (góc phải) + khe chạm trước hàng đầu của thân
        return Math.max(dkHuy() / 2 + 4 + ChuDe.cao(ChuDe.TIEU_DE) + 4, 2 + Math.max(VungCham.MIN, ChuDe.dt() ? 24 : 16) + Bang.cach());
    }

    /**
     * KHUNG HỘP CHUNG kiểu A (0.9.7 kiểu NRO: hộp gỗ, ô huy hiệu vuông, tiêu đề vàng viền; trước đây giống Pokémon GO; mọi hộp: thông báo, xác nhận, nhập chữ / số, chờ): nền mờ 45% (mở dần +
     * thẻ trượt lên), thẻ TRẮNG bo góc lớn + bóng mềm, KHÔNG dải đầu; huy hiệu tròn to đè mép trên (màu theo loại, biểu
     * tượng PNG), tiêu đề đậm canh giữa, X nhỏ góc phải (chỉ khi đóng được). hNoi = cao phần thân (gồm hàng nút); kẹp 70%.
     * Trả {x, y, w, h, yNoi, xNoi, wNoi}.
     */
    public static int[] khung(Graphics g, int ww, int hNoi, String tieuDe, IAction dong, boolean ngoaiDong) {
        long now = System.currentTimeMillis();
        if (now - lucVe > 300) {
            lucMo = now;
        }
        lucVe = now;
        int t = (int) Math.min(140, now - lucMo);
        Canvas.resetTrans(g);
        KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, ((ChuDe.PHU_NEN >>> 24) * (40 + t * 60 / 140) / 100) << 24 | (ChuDe.PHU_NEN & 0xFFFFFF));
        if (ngoaiDong && dong != null) {
            VungCham.them(g, 0, 0, Canvas.w, Canvas.h, dong, "nen-ngoai");
        }
        int pad = pad();
        int dk = dkHuy();
        int hDau = caoDauA();
        int hh = Math.min(caoToiDa() - dk / 2, hDau + hNoi + pad);
        int x = Bang.trai() + ((Bang.phai() - Bang.trai()) - ww) / 2;
        // canh giữa CẢ khối (huy hiệu nhô lên + thẻ)
        int y0 = Bang.tren() + ((Bang.duoi() - Bang.tren()) - hh - dk / 2) / 2 + dk / 2;
        int y = y0 + (140 - t) * 6 / 140;
        VUA_VE[0] = x;
        VUA_VE[1] = y0 - dk / 2;
        VUA_VE[2] = ww;
        VUA_VE[3] = hh + dk / 2;
        int bo = boThe();
        // 0.9.7 (NRO): hộp gỗ viền vàng giấy da + bóng cứng; huy hiệu = ô hộp gỗ khung vàng đè mép trên, icon pixel
        KhungNro.bong(g, x, y, ww, hh);
        if (!KhungNro.ve(g, KhungNroBang.HOP, x, y, ww, hh)) {
            GiaoDien.khoi(g, x, y, ww, hh, 4, ChuDe.VIEN);
            GiaoDien.khoi(g, x + 2, y + 2, ww - 4, hh - 4, 3, ChuDe.NEN);
        }
        game.render.screen.Font.nenSang = true;
        int cx = x + ww / 2, cy = y;
        long tt = now - lucMo;
        if ((huyHieu == BieuTuong.QUA || huyHieu == BieuTuong.SU_KIEN) && tt < 520) {
            cy -= (int) (Math.sin(Math.PI * tt / 520.0) * 5);
        }
        int mau = mauHuy;
        int sh = Math.max(dk, 24);
        if (!KhungNro.ve(g, KhungNroBang.O_CHON, cx - sh / 2, cy - sh / 2, sh, sh)) {
            GiaoDien.khoi(g, cx - sh / 2, cy - sh / 2, sh, sh, 3, mau);
        }
        int ic = sh - 8;
        BieuTuong.ve(g, huyHieu, cx - ic / 2, cy - ic / 2, ic, mau, 0x2E1E14);
        huyHieu = BieuTuong.TIN;
        mauHuy = ChuDe.CHINH;
        huyNhat = false;
        // tiêu đề
        int yTieu = y + Math.max(dk, 24) / 2 + 3;
        ChuDe.chu(g, ChuDe.cat(tieuDe == null ? "" : tieuDe, ChuDe.TIEU_DE, ww - 2 * pad - 24), cx, yTieu, ChuDe.TIEU_DE, 0x8E1A0E, 2);
        if (dong != null) {
            int sx = Math.max(VungCham.MIN, ChuDe.dt() ? 24 : 16);   // vùng chạm đủ cỡ, X vẽ nhỏ
            Bang.nutDong(g, x + ww - sx - 2, y + 2, sx, dong, false);
        }
        return new int[]{x, y, ww, hh, y + hDau, x + pad, ww - 2 * pad};
    }

    /** Cao nút viên (~44 dp điện thoại, ~38-44 px PC). */
    public static int caoNut() {
        return Math.max(VungCham.MIN, ChuDe.dt() ? Bang.caoHang() : 17);
    }

    /** Nút VIÊN (kiểu A, 0.9.6 = Bang.vienNut): chính = teal, nguy hiểm = đỏ, phụ = trắng chữ tối; nhấn co 0,96. */
    public static void nutVien(Graphics g, int x, int y, int w, int h, String chu, boolean chinh, Object viec, String ten) {
        nutVien(g, x, y, w, h, chu, chinh ? Bang.CHINH : Bang.TRUNG, viec, ten);
    }

    public static void nutVien(Graphics g, int x, int y, int w, int h, String chu, int kieu, Object viec, String ten) {
        boolean an = viec != null && Bang.dangAn(g, x, y, w, h);
        Bang.vienNut(g, x, y, w, h, chu, kieu, an);
        Bang.dangKy(g, x, y, w, h, viec, ten);
    }

    /** Hàng nút đáy hộp: PC co theo chữ, cặp nút CANH GIỮA (chính bên phải); điện thoại chia đều. */
    public static void hangNut(Graphics g, int x, int yn, int ww, String[] nut, int[] kieuNut, IAction[] bam, String[] tenNut) {
        int n = nut.length, k = Bang.cach(), pad = pad(), nh = caoNut();
        int[] wb = new int[n];
        int tong = 0;
        for (int i = 0; i < n; i++) {
            wb[i] = ChuDe.dt() ? (ww - 2 * pad - (n - 1) * k) / n : Math.min(rongNut(nut[i]), (ww - 2 * pad - (n - 1) * k) / n);
            tong += wb[i] + (i > 0 ? k : 0);
        }
        int xb = x + (ww - tong) / 2;
        for (int i = 0; i < n; i++) {
            int kh = kieuHop(kieuNut, i, n);
            nutVien(g, xb, yn, wb[i], nh, nut[i], kh, bam[i], tenNut != null && i < tenNut.length ? tenNut[i] : "hop-nut-" + i);
            xb += wb[i] + k;
        }
    }

    static int[] ve(Graphics g, int ww, String tieuDe, int mau, String[] d, int[] hPhu, String[] nut, int[] kieuNut, IAction[] bam,
            IAction dong, boolean ngoaiDong, Cuon cuon, String[] tenNut) {
        int hd = ChuDe.caoDau();
        int lh = caoDong();
        int nh = caoNut();
        int pad = pad();
        int phu = hPhu == null ? 0 : hPhu[0];
        int hChu = d.length * lh + phu;
        int coNut = nut == null || nut.length == 0 ? 0 : nh + pad;
        int[] kh = khung(g, ww, hChu + coNut + pad, tieuDe, dong, ngoaiDong);
        int x = kh[0], y = kh[1], hh = kh[3];
        int hThan = Math.min(hChu, hh - (caoDauA() + 2 * pad + coNut));
        int yt = kh[4] + 2;
        int xt = x + pad, wt = ww - 2 * pad;
        // biểu tượng loại thông báo đầu thân (thông tin / lưu ý / lỗi / thành công)
        int bt = -1;
        bieuTuong = -1;
        if (bt >= 0) {
            int ic = Math.min(18, lh + 4);
            BieuTuong.ve(g, bt, xt, yt, ic, mau, 0xFFFDF8);
            xt += ic + 8;
            wt -= ic + 8;
        }
        if (hThan < hChu && cuon != null) {
            // nội dung dài: cuộn trong khung thân
            cuon.khung(g, xt, yt, wt, hThan, hChu);
            cuon.capNhat();
            int lech = cuon.lech();
            g.setClip(xt, yt, wt, hThan);
            int dau = Math.max(0, lech / lh);
            for (int i = dau; i < d.length; i++) {
                int yy = yt + i * lh - lech;
                if (yy > yt + hThan) {
                    break;
                }
                veDong(g, d[i], xt, yy, false);
            }
            g.setClip(0, 0, Canvas.w, Canvas.h);
            cuon.veThanh(g, x + ww - 7, yt, hThan);
        } else {
            // một dòng: canh giữa; nhiều dòng: canh trái cho dễ đọc
            boolean giua = d.length <= 1;
            for (int i = 0; i < d.length; i++) {
                veDong(g, d[i], giua ? xt + wt / 2 : xt, yt + i * lh, giua);
            }
        }
        int yPhu = yt + Math.min(hThan, d.length * lh);
        if (coNut > 0) {
            hangNut(g, x, y + hh - pad - nh, ww, nut, kieuNut, bam, tenNut);
        }
        return new int[]{x, y, ww, hh, yPhu};
    }

    /** Kiểu nút trong hộp: nút chính = đỏ thương hiệu (không xanh lá neon), phụ = trắng viền xám. */
    static int kieuHop(int[] kieuNut, int i, int n) {
        int k = kieuNut == null ? (i == n - 1 ? Bang.CHINH : Bang.TRUNG) : kieuNut[i];
        return k == Bang.XAC_NHAN || k == Bang.PHU ? Bang.CHINH : k == Bang.NGUY ? Bang.NGUY : k == Bang.CHINH ? Bang.CHINH : Bang.TRUNG;
    }

    static void veDong(Graphics g, String d, int x, int y, boolean giua) {
        if (d.length() > 0 && d.charAt(0) == '\u0001') {
            ChuDe.chu(g, d.substring(1), x, y, ChuDe.NHAN, MAU_TIEU_DE, giua ? 2 : 0);
        } else {
            ChuDe.chu(g, d, x, y, ChuDe.THUONG, ChuDe.CHU, giua ? 2 : 0);
        }
    }

    static int rongIcon() {
        return Math.min(18, caoDong() + 4) + 8;
    }

    /**
     * Ngắt nội dung: đoạn ngăn bằng xuống dòng = DANH SÁCH (mỗi mục "Tiêu đề: mô tả" -> dòng tiêu đề đậm ("\u0001" đầu
     * dòng) + mô tả, dòng trống giữa các mục); một đoạn = ngắt thường.
     */
    static String[] ngatDanhSach(String s, int w) {
        if (s.indexOf('\n') < 0) {
            return ChuDe.ngat(s, ChuDe.THUONG, w, 200);
        }
        java.util.Vector v = new java.util.Vector();
        int i = 0;
        while (i <= s.length()) {
            int j = s.indexOf('\n', i);
            if (j < 0) {
                j = s.length();
            }
            String d = s.substring(i, j).trim();
            i = j + 1;
            if (d.length() == 0) {
                continue;
            }
            if (v.size() > 0) {
                v.addElement("");
            }
            int c = d.indexOf(':');
            String moTa = d;
            if (c > 0 && c < 48) {
                v.addElement("\u0001" + d.substring(0, c).trim());
                moTa = d.substring(c + 1).trim();
            }
            if (moTa.length() > 0) {
                String[] n = ChuDe.ngat(moTa, ChuDe.THUONG, w, 40);
                for (int k = 0; k < n.length; k++) {
                    v.addElement(n[k]);
                }
            }
        }
        String[] r = new String[v.size()];
        v.copyInto(r);
        return r;
    }

    public void keyPress(int keyCode) {
        int n = this.nut == null ? 0 : this.nut.length;
        if (keyCode == -5 || keyCode == 10 || keyCode == 13) {
            xong(n > 0 ? this.viec[n - 1] : this.dong);
        } else if (keyCode == -7 || keyCode == -6 || keyCode == 27) {
            xong(this.dong);
        } else if (keyCode == -1 || keyCode == 2) {
            this.cuon.them(-20);
        } else if (keyCode == -2 || keyCode == 8) {
            this.cuon.them(20);
        }
    }

    public void update() {
    }
}
