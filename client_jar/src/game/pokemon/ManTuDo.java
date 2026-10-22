package game.pokemon;

import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import java.io.DataInputStream;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * TỦ ĐỒ TRANG TRÍ (0.9.8) - phần phải của tab "Ngoại hình" (ManNhanVat): 4 ô Trang phục / Hào quang / Skin thú /
 * Thẻ bạn đồng hành + "Hiện / ẩn" (ẩn từng phụ kiện, như trước). Mỗi ô một ngân sách chỉ số cố định (chọn dòng ở đầu ô,
 * đổi miễn phí theo hồi của máy chủ); món nào đang mặc cũng cho đúng ngân sách đó - trả lượng chỉ mua HÌNH.
 *
 * THỬ ĐỒ: chạm một hàng -> Pokémon trên sân khấu bên trái mặc thử món đó ngay (bản sao NgoaiHinhCua, không gửi gì lên máy
 * chủ, nhãn "Đang thử"); nút Mặc / Nhận / Chế tạo / Mua mới gửi gói -116. Chỉ dùng Bang / ChuDe / GiaoDien (đổi kiểu
 * theo chủ đề chung).
 *
 * Gói -116 TRANG_PHUC (máy chủ chỉ gửi cho client BAN &gt;= 908): C-&gt;S u8 kiểu 0 xin / 1 mặc (u8 ô, UTF mã) / 2 chọn
 * dòng (u8 ô, u8 dòng) / 3 nhận / 4 chế tạo / 5 mua (UTF mã). S-&gt;C kiểu 0 tủ đồ, kiểu 1 kết quả (u8 ok, UTF câu).
 */
public final class ManTuDo {
    public static final int LENH = -116;
    public static final int XIN = 0, MAC = 1, DONG = 2, NHAN = 3, CHE = 4, MUA = 5;
    public static final String[] TAB = {"Trang phục", "Hào quang", "Skin", "Thẻ", "Ẩn/hiện"};
    public static final int TAB_AN = 4;

    private ManTuDo() {
    }

    /** Một ô trang trí (ngân sách + dòng chỉ số). */
    public static final class O {
        public String ma = "", ten = "", dangMac = "";
        public int pnHieuLuc, pnGoc, dongChon, giayCon;
        public String[] dongMa = new String[0], dongTen = new String[0];
        long nhanLuc;

        public int giayConLai() {
            int da = (int) ((System.currentTimeMillis() - nhanLuc) / 1000);
            return Math.max(0, giayCon - da);
        }
    }

    /** Một món. */
    public static final class Mon {
        public String ma = "", ten = "", nguon = "", tienDo = "", moTa = "";
        public int o, so, doHiem, icon, trangThai, hanhDong;
    }

    public static final int CHUA_CO = 0, NHAN_DUOC = 1, DA_CO = 2, DANG_MAC = 3;
    public static final int HD_KHONG = 0, HD_NHAN = 1, HD_CHE = 2, HD_MUA = 3;

    public static O[] o = new O[0];
    public static final Vector MON = new Vector();
    public static int tong, tran = 50, pnDanhHieu;
    public static boolean coDuLieu;
    public static long xinLuc;
    public static int tab;
    static final Cuon CUON = new Cuon();

    /** Thử đồ: ô + số đang thử (-1 = không thử). */
    public static int thuO = -1, thuSo;
    public static String thuTen = "";

    // ------------------------------------------------------------------ mạng

    public static boolean nhan(Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            int kieu = r.readUnsignedByte();
            if (kieu == 0) {
                docTuDo(r);
            } else if (kieu == 1) {
                boolean ok = r.readUnsignedByte() != 0;
                String s = r.readUTF();
                if (s.length() > 0) {
                    if (ok) {
                        DungDo.bao(s);
                    } else {
                        DungDo.loi(s);
                    }
                }
                if (ok) {
                    boThu();
                }
            }
        } catch (Throwable e) {
            // gói hỏng: bỏ qua
        }
        return true;
    }

    static void docTuDo(DataInputStream r) throws java.io.IOException {
        int n = r.readUnsignedByte();
        O[] ds = new O[n];
        long bay = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            O x = new O();
            x.ma = r.readUTF();
            x.ten = r.readUTF();
            x.pnHieuLuc = r.readUnsignedShort();
            x.pnGoc = r.readUnsignedShort();
            int nd = r.readUnsignedByte();
            x.dongMa = new String[nd];
            x.dongTen = new String[nd];
            for (int j = 0; j < nd; j++) {
                x.dongMa[j] = r.readUTF();
                x.dongTen[j] = r.readUTF();
            }
            x.dongChon = r.readUnsignedByte();
            x.dangMac = r.readUTF();
            x.giayCon = r.readInt();
            x.nhanLuc = bay;
            ds[i] = x;
        }
        tong = r.readUnsignedShort();
        tran = r.readUnsignedShort();
        pnDanhHieu = r.readUnsignedShort();
        int m = r.readUnsignedShort();
        Vector v = new Vector();
        for (int i = 0; i < m; i++) {
            Mon x = new Mon();
            x.ma = r.readUTF();
            x.o = r.readUnsignedByte();
            x.so = r.readUnsignedByte();
            x.ten = r.readUTF();
            x.doHiem = r.readUnsignedByte();
            x.icon = r.readUnsignedShort();
            x.trangThai = r.readUnsignedByte();
            x.nguon = r.readUTF();
            x.tienDo = r.readUTF();
            x.hanhDong = r.readUnsignedByte();
            x.moTa = r.readUTF();
            v.addElement(x);
        }
        o = ds;
        synchronized (MON) {
            MON.removeAllElements();
            for (int i = 0; i < v.size(); i++) {
                MON.addElement(v.elementAt(i));
            }
        }
        coDuLieu = true;
    }

    static void gui(int kieu, int a, int b, String ma) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(kieu);
            if (kieu == MAC) {
                m.writer().writeByte(a);
                m.writer().writeUTF(ma == null ? "" : ma);
            } else if (kieu == DONG) {
                m.writer().writeByte(a);
                m.writer().writeByte(b);
            } else if (kieu >= NHAN) {
                m.writer().writeUTF(ma == null ? "" : ma);
            }
            GameService.gI().session.sendMessage(m);
            m.cleanup();
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    /** Mở tab Ngoại hình: xin tủ đồ (tối đa 1 lần / 3 giây). */
    public static void xin() {
        long t = System.currentTimeMillis();
        if (t - xinLuc < 3000) {
            return;
        }
        xinLuc = t;
        gui(XIN, 0, 0, null);
    }

    // ------------------------------------------------------------------ thử đồ

    public static void thu(Mon m) {
        if (thuO == m.o && thuSo == m.so) {
            boThu();
            return;
        }
        thuO = m.o;
        thuSo = m.so;
        thuTen = m.ten;
    }

    public static void boThu() {
        thuO = -1;
        thuSo = 0;
        thuTen = "";
    }

    /** Ngoại hình dùng để vẽ sân khấu: bản thật, hoặc bản sao mặc món đang thử. */
    public static NgoaiHinhCua ngoaiHinhThu(NgoaiHinhCua that) {
        if (thuO < 0) {
            return that;
        }
        NgoaiHinhCua n = that == null ? new NgoaiHinhCua() : that.sao();
        switch (thuO) {
            case 0:
                n.tpSo = thuSo;
                n.an &= ~NgoaiHinhCua.AN_TRANG_PHUC;
                break;
            case 1:
                n.haoQuang = thuSo;
                break;
            case 2:
                n.skinThu = thuSo;
                break;
            default:
                n.theBan = thuSo;
                break;
        }
        return n;
    }

    private static NgoaiHinhCua cat;

    /** Bọc lúc vẽ sân khấu: mặc thử lên nhân vật chính, xong trả lại. */
    public static void batThu(game.model.Char c) {
        cat = c.ngoaiHinh;
        c.ngoaiHinh = ngoaiHinhThu(cat);
    }

    public static void tatThu(game.model.Char c) {
        c.ngoaiHinh = cat;
        cat = null;
    }

    /** Nhãn "Đang thử: ..." + nút "Bỏ thử" trên đầu sân khấu. */
    public static void veNhanThu(Graphics g, int x, int y, int w) {
        if (thuO < 0) {
            return;
        }
        int h = Bang.co(24);
        String chu = "Đang thử: " + thuTen;
        int wb = Math.max(h * 2, ChuDe.rong("Bỏ thử", ChuDe.NHAN) + 20);
        String c = ChuDe.cat(chu, ChuDe.NHAN, w - wb - 24);
        int wc = ChuDe.rong(c, ChuDe.NHAN) + 16;
        GiaoDien.khoi(g, x + 4, y + 4, wc, h, h / 2, ChuDe.VANG_NHAT);
        ChuDe.chuGiua(g, c, x + 4 + wc / 2, y + 4, h, ChuDe.NHAN, ChuDe.VANG_TOI, 2);
        Bang.nut(g, x + w - wb - 4, y + 4, wb, h, "Bỏ thử", Bang.PHU, new IAction() {
            public void perform() {
                boThu();
            }
        }, "bo-thu");
    }

    // ------------------------------------------------------------------ vẽ

    static String phanTram(int pn) {
        int a = pn / 10, b = pn % 10;
        return b == 0 ? a + "%" : a + "," + b + "%";
    }

    static String tenDong(O x) {
        return x.dongChon >= 0 && x.dongChon < x.dongTen.length ? x.dongTen[x.dongChon] : "";
    }

    static Mon[] monCuaO(int oi) {
        Vector v = new Vector();
        synchronized (MON) {
            for (int i = 0; i < MON.size(); i++) {
                Mon m = (Mon) MON.elementAt(i);
                if (m.o == oi) {
                    v.addElement(m);
                }
            }
        }
        Mon[] a = new Mon[v.size()];
        v.copyInto(a);
        return a;
    }

    /** Phần phải tab Ngoại hình. an = mặt nạ ẩn hiện tại (hàng Hiện / ẩn). */
    public static void ve(Graphics g, int x, int y, int w, int h, final ManNhanVat man) {
        int k = Bang.cach();
        Object[] viec = new Object[TAB.length];
        for (int i = 0; i < TAB.length; i++) {
            final int t = i;
            viec[i] = new IAction() {
                public void perform() {
                    tab = t;
                    CUON.den(0);
                    boThu();
                }
            };
        }
        // chip tự xuống dòng (đoạn phân chia cắt chữ "Trang..." trên điện thoại)
        int yy = y + 4 + Bang.hangChip(g, x + 6, y + 4, w - 12, TAB, tab, viec) + k;
        if (tab == TAB_AN) {
            man.veAnHien(g, x, yy, w, y + h - yy);
            return;
        }
        if (!coDuLieu) {
            xin();
            Bang.trong(g, x, yy, w, y + h - yy, "Đang mở tủ đồ…", "Máy chủ chưa có tủ đồ thì cập nhật bản mới.");
            return;
        }
        yy = veDauO(g, x + 6, yy, w - 12);
        final Mon[] ds = monCuaO(tab);
        if (ds.length == 0) {
            Bang.trong(g, x, yy, w, y + h - yy, "Chưa có món nào", "Sự kiện, thành tựu và chế tạo sẽ mở thêm.");
            return;
        }
        int rh = Math.max(Math.max(Bang.caoHang() + 8, VungCham.MIN + 10), ChuDe.cao(ChuDe.NHAN) + 2 * ChuDe.cao(ChuDe.PHU) + 8);
        Bang.danhSach(g, CUON, x + 4, yy, w - 8, y + h - 4 - yy, ds.length, rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int rh) {
                veHang(g, ds[i], i, x, y, w, rh);
            }
        });
    }

    /** Đầu ô: ngân sách + dòng chỉ số (đổi) + tổng trang trí. Trả y sau đầu. */
    static int veDauO(Graphics g, int x, int y, int w) {
        if (tab < 0 || tab >= o.length) {
            return y;
        }
        final O x0 = o[tab];
        final int oi = tab;
        int h = Bang.co(26);
        String dong = "+" + phanTram(x0.pnHieuLuc) + " " + tenDong(x0);
        int con = x0.giayConLai();
        String nut = con > 0 ? "Đổi sau " + thoiGian(con) : "Đổi dòng";
        int wn = Math.min(w / 2, ChuDe.rong(nut, ChuDe.NHAN) + 22);
        GiaoDien.khoi(g, x, y, w - wn - 6, h, h / 2, ChuDe.CHINH_NHAT);
        ChuDe.chuGiua(g, ChuDe.cat(x0.ten + ": " + dong, ChuDe.NHAN, w - wn - 20), x + 8, y, h, ChuDe.NHAN, ChuDe.CHINH_TOI, 0);
        Bang.nut(g, x + w - wn, y, wn, h, nut, con > 0 || x0.dongTen.length < 2 ? Bang.TAT : Bang.PHU, new IAction() {
            public void perform() {
                gui(DONG, oi, (x0.dongChon + 1) % Math.max(1, x0.dongTen.length), null);
            }
        }, "doi-dong");
        y += h + 2;
        String phu = "Trang trí: " + phanTram(tong) + " / " + phanTram(tran) + " (danh hiệu " + phanTram(pnDanhHieu) + ")";
        if (x0.pnHieuLuc < x0.pnGoc) {
            phu += " - chạm trần tổng";
        }
        ChuDe.chu(g, ChuDe.cat(phu, ChuDe.PHU, w), x + 2, y, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        return y + ChuDe.cao(ChuDe.PHU) + 4;
    }

    static String thoiGian(int giay) {
        if (giay >= 3600) {
            return (giay / 3600) + " giờ";
        }
        return Math.max(1, giay / 60) + " phút";
    }

    static String chuNut(Mon m) {
        if (m.trangThai == DANG_MAC) {
            return "Tháo";
        }
        if (m.trangThai == DA_CO) {
            return "Mặc";
        }
        switch (m.hanhDong) {
            case HD_NHAN:
                return "Nhận";
            case HD_CHE:
                return "Chế tạo";
            case HD_MUA: {
                int gia = giaLuong(m);
                return gia > 0 ? "Mua " + gia + " lượng" : "Mua";
            }
            default:
                return null;
        }
    }

    /** Giá lượng đọc từ chữ nguồn / tiến độ ("... 200 lượng"); 0 = không rõ. */
    static int giaLuong(Mon m) {
        String s = m.nguon + " " + m.tienDo;
        int i = s.indexOf("lượng");
        if (i <= 0) {
            return 0;
        }
        int j = i - 1;
        while (j >= 0 && s.charAt(j) == ' ') {
            j--;
        }
        int het = j + 1;
        while (j >= 0 && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.')) {
            j--;
        }
        String so = s.substring(j + 1, het).replace('.', ' ');
        StringBuffer b = new StringBuffer();
        for (int t = 0; t < so.length(); t++) {
            if (Character.isDigit(so.charAt(t))) {
                b.append(so.charAt(t));
            }
        }
        try {
            return b.length() == 0 ? 0 : Integer.parseInt(b.toString());
        } catch (Throwable e) {
            return 0;
        }
    }

    static int mauTrangThai(Mon m) {
        switch (m.trangThai) {
            case DANG_MAC:
                return ChuDe.CHINH;
            case DA_CO:
                return ChuDe.LA;
            case NHAN_DUOC:
                return ChuDe.CAM;
            default:
                return ChuDe.XAM;
        }
    }

    static String chuTrangThai(Mon m) {
        switch (m.trangThai) {
            case DANG_MAC:
                return "Đang mặc";
            case DA_CO:
                return "Đã có";
            case NHAN_DUOC:
                return "Nhận được";
            default:
                return "Chưa có";
        }
    }

    static void veHang(Graphics g, final Mon m, int i, int x, int y, int w, int rh) {
        boolean dangThu = thuO == m.o && thuSo == m.so;
        Bang.hang(g, x, y, w, rh, i, dangThu, null, null);
        final String nut = chuNut(m);
        int hn = Math.max(VungCham.MIN, Math.min(rh - 8, Bang.co(26)));
        int wn = nut == null ? 0 : Math.min(w * 2 / 5, ChuDe.rong(nut, ChuDe.NHAN) + 22);
        // chạm phần trái (không gồm nút) = thử
        Bang.dangKy(g, x, y, nut == null ? w : w - wn - 6 - Bang.cach() - 2, rh, new IAction() {
            public void perform() {
                thu(m);
            }
        }, "thu-" + m.ma);
        int s = rh - 8;
        GiaoDien.khoi(g, x + 4, y + 4, s, s, ChuDe.BO_O, ChuDe.NEN_PHU);
        int cx = x + 4 + s / 2, cy = y + 4 + s / 2;
        IconVatPham.ve(g, (short) m.icon, cx, cy, s, BieuTuong.AO);
        IconVatPham.veKhungHiemTheoBac(g, m.doHiem, cx, cy, s);
        int xt = x + s + 10, wt = w - s - 16 - wn;
        int lh = ChuDe.cao(ChuDe.NHAN), lp = ChuDe.cao(ChuDe.PHU);
        ChuDe.chu(g, ChuDe.cat(m.ten, ChuDe.NHAN, wt), xt, y + 3, ChuDe.NHAN, ChuDe.CHU, 0);
        String trai = chuTrangThai(m);
        int wb = ChuDe.rong(trai, ChuDe.PHU) + 10;
        GiaoDien.khoi(g, xt, y + 4 + lh, wb, lp, lp / 2, GiaoDien.pha(mauTrangThai(m), 0xFFFFFF, 3, 4));
        ChuDe.chuGiua(g, trai, xt + wb / 2, y + 4 + lh, lp, ChuDe.PHU, GiaoDien.pha(mauTrangThai(m), 0, 1, 3), 2);
        String phu = m.tienDo.length() > 0 ? m.tienDo : m.nguon;
        ChuDe.chu(g, ChuDe.cat(phu, ChuDe.PHU, wt - wb - 6), xt + wb + 6, y + 4 + lh, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        if (rh >= lh + 2 * lp + 8) {
            ChuDe.chu(g, ChuDe.cat(m.moTa, ChuDe.PHU, wt), xt, y + 5 + lh + lp, ChuDe.PHU, ChuDe.CHU_NHAT, 0);
        }
        if (nut != null) {
            int kieu = m.trangThai == DANG_MAC ? Bang.PHU : m.hanhDong == HD_MUA && m.trangThai < DA_CO ? Bang.CHINH : Bang.XAC_NHAN;
            Bang.nut(g, x + w - wn - 6, y + (rh - hn) / 2, wn, hn, nut, kieu, new IAction() {
                public void perform() {
                    bam(m);
                }
            }, "tu-do-" + m.ma);
        }
    }

    static void bam(final Mon m) {
        if (m.trangThai == DANG_MAC) {
            gui(MAC, m.o, 0, "");
            return;
        }
        if (m.trangThai == DA_CO) {
            gui(MAC, m.o, 0, m.ma);
            return;
        }
        switch (m.hanhDong) {
            case HD_NHAN:
                gui(NHAN, 0, 0, m.ma);
                break;
            case HD_CHE:
                HopXacNhan.hoi("Chế tạo", "Chế " + m.ten + "?\n" + m.tienDo, "Chế tạo", new IAction() {
                    public void perform() {
                        gui(CHE, 0, 0, m.ma);
                    }
                }, null);
                break;
            case HD_MUA: {
                int gia = giaLuong(m);
                IAction co = new IAction() {
                    public void perform() {
                        gui(MUA, 0, 0, m.ma);
                    }
                };
                String nd = "Mua " + m.ten + "? Chỉ đổi hình - chỉ số theo ô, như món miễn phí.";
                if (gia > 0) {
                    HopXacNhan.hoiGia("Mua trang trí", nd, "Mua", TienTe.LUONG, gia, co);
                } else {
                    HopXacNhan.hoi("Mua trang trí", nd, "Mua", co, null);
                }
                break;
            }
            default:
                break;
        }
    }

    /** Vào game mới / đổi nhân vật: xoá dữ liệu cũ. */
    public static void xoa() {
        coDuLieu = false;
        o = new O[0];
        synchronized (MON) {
            MON.removeAllElements();
        }
        boThu();
        xinLuc = 0;
    }

    /** Để test: đặt dữ liệu từ gói giả (byte của S->C kiểu 0, sau byte kiểu). */
    public static void napThu(byte[] b) throws java.io.IOException {
        docTuDo(new DataInputStream(new java.io.ByteArrayInputStream(b)));
    }
}
