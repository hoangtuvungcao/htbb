package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import game.render.screen.Dialog;
import java.io.DataInputStream;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * CẬP NHẬT GAME (0.9.7, bản 907): máy chủ (HTBB com.htbb.capnhat.CapNhatClient) báo bản mới qua lệnh -115.
 * <ul>
 *   <li><b>NHẮC</b> (bản cũ hơn bản mới nhất nhưng còn được chơi): vào thế giới thì hiện thẻ "Đã có bản mới" (có gì mới,
 *       dung lượng) với "Cập nhật ngay" / "Để sau"; Để sau = chơi tiếp, nhắc lại một lần mỗi ngày (nhớ trong RMS).</li>
 *   <li><b>BẮT BUỘC</b> (bản dưới mức tối thiểu): máy chủ không cho vào; hộp "Cần cập nhật để tiếp tục chơi" CHẶN màn
 *       đăng nhập (không có nút đóng) với "Tải bản mới" / "Thoát game".</li>
 * </ul>
 * "Cập nhật ngay" / "Tải bản mới": nền tảng có bộ cập nhật ({@link CapNhatNen}: APK tải + kiểm SHA-256 + trình cài
 * Android; PC tải bộ cài / AppImage + kiểm SHA-256 + cài và mở lại) thì tải ngay trong game với thanh tiến độ; không có
 * (J2ME) hoặc thiếu SHA-256 thì mở link tải (platformRequest). Lỗi tải: "Thử lại" / "Mở trang tải".
 *
 * <p>Lệnh -115 server -> client: byte loại (1 nhắc, 2 bắt buộc), int bản mới, UTF phiên bản, UTF link tệp, UTF sha256,
 * UTF dung lượng, UTF có gì mới, UTF trang tải game, int bản tối thiểu, int bản client. Máy chủ chỉ gửi cho client
 * bản >= 907; client cũ nhận hộp 37 có chữ + link.
 */
public final class CapNhat extends Dialog {
    public static final int LENH = -115;
    public static final byte NHAC = 1, BAT_BUOC = 2;
    /** RMS nhớ lần nhắc "Để sau": "ngày:bản". */
    static final String RMS = "htbbcapnhat";

    static CapNhat me;
    /** gói NHẮC đã nhận, chờ vào thế giới mới hiện */
    static boolean choHien;
    static long lucNhan;

    public byte loai;
    public int banMoi, banToiThieu, banClient;
    public String phienBan = "", url = "", sha256 = "", dungLuong = "", nhatKy = "", trangTai = "";
    /** chữ thêm khi đã mở trình duyệt (nền tảng không tự cập nhật) */
    String daMoLink;
    boolean dangTaiTrongGame;

    private CapNhat() {
    }

    // ------------------------------------------------------------------ nền tảng

    static CapNhatNen nen;
    static boolean daTim;
    /** Lớp cập nhật của từng bản (không có = J2ME). */
    static final String[] LOP_NEN = {"htbb.pc.CapNhatPc", "com.htbb.pokemon.CapNhatApk"};

    public static CapNhatNen nen() {
        if (!daTim) {
            daTim = true;
            for (int i = 0; i < LOP_NEN.length && nen == null; i++) {
                try {
                    nen = (CapNhatNen) Class.forName(LOP_NEN[i]).newInstance();
                } catch (Throwable e) {
                    nen = null;
                }
            }
        }
        return nen;
    }

    /** Kiểm thử: đặt bộ cập nhật (null = như J2ME). */
    public static void datNen(CapNhatNen n) {
        nen = n;
        daTim = true;
    }

    // ------------------------------------------------------------------ mạng

    public static CapNhat hienTai() {
        return me;
    }

    /** Gói -115 (gọi ở đầu MessageHandler.onMessage). true = đã xử lý. */
    public static boolean nhan(Message msg) {
        if (msg == null || msg.command != LENH) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            CapNhat c = new CapNhat();
            c.loai = r.readByte();
            c.banMoi = r.readInt();
            c.phienBan = r.readUTF();
            c.url = r.readUTF();
            c.sha256 = r.readUTF();
            c.dungLuong = r.readUTF();
            c.nhatKy = r.readUTF();
            c.trangTai = r.readUTF();
            c.banToiThieu = r.readInt();
            c.banClient = r.readInt();
            nhanGoi(c);
        } catch (Throwable e) {
            // gói hỏng: bỏ qua
        }
        return true;
    }

    static void nhanGoi(CapNhat c) {
        if (me != null && me.dangTaiTrongGame && nen() != null && nen().trangThai() != CapNhatNen.CHUA
                && nen().trangThai() != CapNhatNen.LOI) {
            // đang tải bản này: giữ hộp đang có, chỉ nâng lên bắt buộc nếu cần
            if (c.loai == BAT_BUOC) {
                me.loai = BAT_BUOC;
            }
            return;
        }
        me = c;
        lucNhan = System.currentTimeMillis();
        if (c.loai == BAT_BUOC) {
            choHien = false;
            TuNoiLai.coThongBao();   // máy chủ đã báo lý do -> rớt mạng sau đó không tự nối lại
            hien();
        } else if (c.loai == NHAC) {
            choHien = !daNhacHomNay(docRms(), ngayHomNay(), c.banMoi);
        }
    }

    static void hien() {
        if (me == null) {
            return;
        }
        Canvas.menu.showMenu = false;
        me.left = null;
        me.center = null;
        me.right = null;
        Canvas.currentDialog = me;
    }

    /** Mỗi nhịp game (Canvas.run): hộp bắt buộc luôn nằm trên; thẻ nhắc hiện khi đã vào thế giới, không có hộp khác. */
    public static void tick() {
        CapNhat c = me;
        if (c == null) {
            return;
        }
        if (c.loai == BAT_BUOC) {
            if (Canvas.currentDialog != c && System.currentTimeMillis() - lucNhan > 600L) {
                hien();
            }
            return;
        }
        if (choHien && Canvas.currentDialog == null && !Canvas.menu.showMenu && ThongBao.trongGame()
                && System.currentTimeMillis() - lucNhan > 1500L) {
            choHien = false;
            ghiDaNhac(c.banMoi);
            hien();
        }
    }

    public static boolean dangMo() {
        return me != null && Canvas.currentDialog == me;
    }

    /** Hộp đang chờ hiện (NHẮC, chưa vào thế giới). */
    public static boolean dangCho() {
        return choHien;
    }

    // ------------------------------------------------------------------ nhắc một lần mỗi ngày

    /** Ngày theo giờ Việt Nam (UTC+7). */
    public static int ngayHomNay() {
        return (int) ((System.currentTimeMillis() + 7L * 3600000L) / 86400000L);
    }

    /** Đã nhắc bản này hôm nay chưa ("ngày:bản" trong RMS). */
    public static boolean daNhacHomNay(String luu, int ngay, int ban) {
        if (luu == null) {
            return false;
        }
        int i = luu.indexOf(':');
        if (i <= 0) {
            return false;
        }
        try {
            return Integer.parseInt(luu.substring(0, i).trim()) == ngay && Integer.parseInt(luu.substring(i + 1).trim()) == ban;
        } catch (Throwable e) {
            return false;
        }
    }

    static String docRms() {
        try {
            return game.render.screen.RMS.loadRMSString(RMS);
        } catch (Throwable e) {
            return null;
        }
    }

    static void ghiDaNhac(int ban) {
        try {
            game.render.screen.RMS.saveRMSString(RMS, ngayHomNay() + ":" + ban);
        } catch (Throwable e) {
            // không lưu được: mai nhắc lại sớm hơn, không sao
        }
    }

    // ------------------------------------------------------------------ chữ

    /** "0.9.7" (phiên bản) hoặc "bản 907". */
    public String tenMoi() {
        if (this.phienBan != null && this.phienBan.length() > 0) {
            return this.phienBan;
        }
        return this.banMoi > 0 ? "bản " + this.banMoi : "mới";
    }

    static String tenHienTai(int banClient) {
        String s = null;
        try {
            s = game.GameMidlet.instance == null ? null : game.GameMidlet.instance.getAppProperty("HTBB-Phien-Ban");
        } catch (Throwable e) {
            s = null;
        }
        if (s != null && s.trim().length() > 0) {
            return s.trim();
        }
        return banClient > 0 ? "bản " + banClient : "bản cũ";
    }

    public String tieuDe() {
        return this.loai == BAT_BUOC ? "Cần cập nhật để tiếp tục chơi" : "Đã có bản mới " + tenMoi();
    }

    /** 35,8 MB. */
    public static String mb(long b) {
        if (b <= 0) {
            return "0 MB";
        }
        long m10 = (b * 10 + 524288) / 1048576;
        return (m10 / 10) + "," + (m10 % 10) + " MB";
    }

    /** Phần trăm 0..100 (tổng chưa biết = -1). */
    public static int phanTram(long da, long tong) {
        if (tong <= 0) {
            return -1;
        }
        long p = da * 100 / tong;
        return (int) (p < 0 ? 0 : p > 100 ? 100 : p);
    }

    /** Dòng nội dung theo trạng thái ("\u0001" đầu dòng = dòng đậm). */
    String noiDung(int st) {
        StringBuffer b = new StringBuffer();
        CapNhatNen n = nen();
        if (st == CapNhatNen.DANG_TAI || st == CapNhatNen.DANG_KIEM) {
            b.append(st == CapNhatNen.DANG_KIEM ? "Đang kiểm tra tệp tải về…" : "Đang tải bản " + tenMoi() + "…");
            if (this.loai != BAT_BUOC) {
                b.append("\nGiữ game mở cho tới khi tải xong.");
            }
            return b.toString();
        }
        if (st == CapNhatNen.XONG) {
            String g = n == null ? null : n.ghiChu();
            return g == null || g.length() == 0 ? "Đã tải xong bản " + tenMoi() + "." : g;
        }
        if (st == CapNhatNen.LOI) {
            String l = n == null ? null : n.loi();
            b.append("Không tải được bản mới");
            b.append(l == null || l.length() == 0 ? "." : ": " + l);
            b.append("\nThử lại, hoặc tải ở trang tải game.");
            return b.toString();
        }
        if (this.daMoLink != null) {
            b.append(this.daMoLink).append('\n');
        }
        if (this.loai == BAT_BUOC) {
            b.append("Bản ").append(tenHienTai(this.banClient)).append(" đã cũ. Tải bản ").append(tenMoi()).append(" để vào game.");
        } else {
            b.append("Bạn đang dùng ").append(tenHienTai(this.banClient)).append(". Bấm Để sau để chơi tiếp.");
        }
        if (this.dungLuong != null && this.dungLuong.length() > 0) {
            b.append("\nDung lượng: ").append(this.dungLuong);
        }
        if (this.nhatKy != null && this.nhatKy.trim().length() > 0) {
            b.append("\n\u0001Có gì mới");
            String[] d = ChuDe.tach(this.nhatKy.trim(), '\n');
            for (int i = 0; i < d.length; i++) {
                String s = d[i].trim();
                while (s.startsWith("-") || s.startsWith("*") || s.startsWith("•")) {
                    s = s.substring(1).trim();
                }
                if (s.length() > 0) {
                    b.append("\n• ").append(s);
                }
            }
        }
        return b.toString();
    }

    /** Ngắt nội dung thành dòng vừa w (giữ dòng đậm "\u0001"), tối đa toiDa dòng. */
    static String[] ngat(String s, int w, int toiDa) {
        java.util.Vector v = new java.util.Vector();
        String[] doan = ChuDe.tach(s, '\n');
        for (int i = 0; i < doan.length && v.size() < toiDa; i++) {
            String d = doan[i];
            if (d.length() > 0 && d.charAt(0) == '\u0001') {
                v.addElement(d);
                continue;
            }
            String[] n = ChuDe.ngat(d, ChuDe.THUONG, w, toiDa - v.size());
            for (int k = 0; k < n.length; k++) {
                v.addElement(n[k]);
            }
        }
        String[] r = new String[v.size()];
        v.copyInto(r);
        return r;
    }

    // ------------------------------------------------------------------ việc

    final IAction bamCapNhat = new IAction() {
        public void perform() {
            CapNhat.this.capNhatNgay();
        }
    };
    final IAction bamDeSau = new IAction() {
        public void perform() {
            CapNhat.this.deSau();
        }
    };
    final IAction bamThoat = new IAction() {
        public void perform() {
            CapNhat.this.thoat();
        }
    };
    final IAction bamHuy = new IAction() {
        public void perform() {
            CapNhatNen n = nen();
            if (n != null) {
                n.huy();
            }
            CapNhat.this.dangTaiTrongGame = false;
        }
    };
    final IAction bamTrang = new IAction() {
        public void perform() {
            CapNhat.this.moLink(CapNhat.this.trangTai.length() > 0 ? CapNhat.this.trangTai : CapNhat.this.url);
        }
    };
    final IAction bamCaiLai = new IAction() {
        public void perform() {
            CapNhatNen n = nen();
            if (n == null || !n.caiLai()) {
                CapNhat.this.moLink(CapNhat.this.url.length() > 0 ? CapNhat.this.url : CapNhat.this.trangTai);
            }
        }
    };

    /** Link mở được khi bấm (tệp, không có thì trang tải). */
    public String linkMo() {
        return this.url != null && this.url.length() > 0 ? this.url : this.trangTai;
    }

    /** Cập nhật ngay / Tải bản mới: tự tải nếu nền tảng làm được, không thì mở link. */
    public void capNhatNgay() {
        CapNhatNen n = nen();
        if (n != null && this.url.length() > 0 && n.hoTro(this.url, this.sha256)) {
            this.dangTaiTrongGame = true;
            n.batDau(this.url, this.sha256, tenMoi());
            AmThanh.phat(AmThanh.XAC_NHAN);
            return;
        }
        moLink(linkMo());
    }

    void moLink(String link) {
        boolean phaiThoat = false;
        try {
            phaiThoat = link != null && link.length() > 0 && game.GameMidlet.instance.platformRequest(link);
        } catch (Throwable e) {
            phaiThoat = false;
        }
        if (phaiThoat) {
            // J2ME: máy chỉ tải sau khi thoát game
            game.GameMidlet.instance.notifyDestroyed();
            return;
        }
        if (this.loai == BAT_BUOC) {
            this.daMoLink = "Đã mở link tải. Cài bản mới rồi mở lại game.";
        } else {
            dong();
        }
    }

    public void deSau() {
        if (this.loai == BAT_BUOC) {
            return;   // không có đường chơi tiếp
        }
        ghiDaNhac(this.banMoi);
        dong();
    }

    void thoat() {
        try {
            game.GameMidlet.instance.notifyDestroyed();
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    void dong() {
        if (Canvas.currentDialog == this) {
            Canvas.currentDialog = null;
        }
        Canvas.clearKeyPressed();
        if (me == this && this.loai != BAT_BUOC) {
            me = null;
        }
    }

    // ------------------------------------------------------------------ vẽ

    final String[] nut2 = new String[2], nut1 = new String[1];
    final int[] kieu2 = new int[2], kieu1 = new int[1];
    final IAction[] bam2 = new IAction[2], bam1 = new IAction[1];
    static final String[] TEN_NUT2 = {"cap-nhat-phu", "cap-nhat-chinh"}, TEN_NUT1 = {"cap-nhat-huy"};
    String khoaNgat;
    int rongNgat, dongNgat;
    String[] dong;
    /** {x, y, w, h} hộp vừa vẽ (kiểm thử) */
    public static final int[] VUA_VE = new int[4];

    /** Nút theo trạng thái: điền nut / kieu / bam, trả mảng dùng. */
    int chonNut(int st) {
        boolean bb = this.loai == BAT_BUOC;
        if (st == CapNhatNen.DANG_TAI || st == CapNhatNen.DANG_KIEM) {
            this.nut1[0] = "Huỷ";
            this.kieu1[0] = Bang.TRUNG;
            this.bam1[0] = this.bamHuy;
            return 1;
        }
        if (st == CapNhatNen.XONG) {
            this.nut2[0] = bb ? "Thoát game" : "Đóng";
            this.bam2[0] = bb ? this.bamThoat : this.bamDeSau;
            this.nut2[1] = "Cài lại";
            this.bam2[1] = this.bamCaiLai;
        } else if (st == CapNhatNen.LOI) {
            this.nut2[0] = "Mở trang tải";
            this.bam2[0] = this.bamTrang;
            this.nut2[1] = "Thử lại";
            this.bam2[1] = this.bamCapNhat;
        } else {
            this.nut2[0] = bb ? "Thoát game" : "Để sau";
            this.bam2[0] = bb ? this.bamThoat : this.bamDeSau;
            this.nut2[1] = bb ? "Tải bản mới" : "Cập nhật ngay";
            this.bam2[1] = this.bamCapNhat;
        }
        this.kieu2[0] = Bang.TRUNG;
        this.kieu2[1] = Bang.CHINH;
        return 2;
    }

    int trangThai() {
        CapNhatNen n = nen();
        return n == null || !this.dangTaiTrongGame ? CapNhatNen.CHUA : n.trangThai();
    }

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        int st = trangThai();
        int soNut = chonNut(st);
        String[] nut = soNut == 1 ? this.nut1 : this.nut2;
        int[] kieu = soNut == 1 ? this.kieu1 : this.kieu2;
        IAction[] bam = soNut == 1 ? this.bam1 : this.bam2;
        boolean bb = this.loai == BAT_BUOC;
        boolean tienDo = st == CapNhatNen.DANG_TAI || st == CapNhatNen.DANG_KIEM;
        int ww = HopThongBao.rongToiDa();
        int pad = HopThongBao.pad();
        int lh = HopThongBao.caoDong();
        int nh = HopThongBao.caoNut();
        int hThanh = tienDo ? Math.max(8, lh) + lh + pad : 0;
        int dk = HopThongBao.dkHuy();
        int conCao = HopThongBao.caoToiDa() - dk / 2 - HopThongBao.caoDauA() - 3 * pad - nh - hThanh - 4;
        int toiDa = Math.max(2, conCao / lh);
        String nd = noiDung(st);
        int wt = ww - 2 * pad;
        if (this.dong == null || !nd.equals(this.khoaNgat) || this.rongNgat != wt || this.dongNgat != toiDa) {
            this.dong = ngat(nd, wt, toiDa);
            this.khoaNgat = nd;
            this.rongNgat = wt;
            this.dongNgat = toiDa;
        }
        if (st == CapNhatNen.LOI) {
            HopThongBao.datHuyHieu(BieuTuong.LOI, ChuDe.DO, false);
        } else if (bb) {
            HopThongBao.datHuyHieu(BieuTuong.CANH_BAO, ChuDe.CAM, false);
        } else {
            HopThongBao.datHuyHieu(BieuTuong.QUA, ChuDe.CHINH, false);
        }
        int hNoi = this.dong.length * lh + hThanh + nh + 2 * pad;
        // bắt buộc: không có nút X; nhắc: X = Để sau (đang tải: X ẩn - dùng Huỷ)
        IAction x = bb || tienDo ? null : this.bamDeSau;
        int[] kh = HopThongBao.khung(g, ww, hNoi, tieuDe(), x, false);
        VUA_VE[0] = HopThongBao.VUA_VE[0];
        VUA_VE[1] = HopThongBao.VUA_VE[1];
        VUA_VE[2] = HopThongBao.VUA_VE[2];
        VUA_VE[3] = HopThongBao.VUA_VE[3];
        int xx = kh[0], yy = kh[1], hh = kh[3];
        int xt = xx + pad;
        int yt = kh[4] + 2;
        for (int i = 0; i < this.dong.length; i++) {
            HopThongBao.veDong(g, this.dong[i], xt, yt + i * lh, false);
        }
        if (tienDo) {
            CapNhatNen n = nen();
            long da = n == null ? 0 : n.daTai(), tong = n == null ? 0 : n.tong();
            int p = phanTram(da, tong);
            int yb = yt + this.dong.length * lh + pad / 2;
            int hb = Math.max(8, lh * 2 / 3);
            Bang.thanh(g, xt, yb, wt, hb, st == CapNhatNen.DANG_KIEM ? 100 : (p < 0 ? 0 : p), 100, 1, null);
            String chu = st == CapNhatNen.DANG_KIEM ? "Kiểm tra SHA-256…"
                    : p < 0 ? mb(da) : p + "% · " + mb(da) + " / " + mb(tong);
            ChuDe.chu(g, chu, xt + wt / 2, yb + hb + 3, ChuDe.PHU, ChuDe.CHU_PHU, 2);
        }
        HopThongBao.hangNut(g, xx, yy + hh - pad - nh, ww, nut, kieu, bam, soNut == 1 ? TEN_NUT1 : TEN_NUT2);
    }

    public void keyPress(int keyCode) {
        int st = trangThai();
        if (keyCode == -5 || keyCode == 10 || keyCode == 13) {
            if (st == CapNhatNen.DANG_TAI || st == CapNhatNen.DANG_KIEM) {
                return;
            }
            if (st == CapNhatNen.XONG) {
                this.bamCaiLai.perform();
            } else {
                capNhatNgay();
            }
        } else if (keyCode == -7 || keyCode == -6 || keyCode == 27) {
            if (st == CapNhatNen.DANG_TAI || st == CapNhatNen.DANG_KIEM) {
                this.bamHuy.perform();
            } else {
                deSau();   // bắt buộc: không làm gì
            }
        }
    }

    public void update() {
    }
}
