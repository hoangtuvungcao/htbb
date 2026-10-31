package game.pokemon;

import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.Dialog;
import game.render.screen.Font;
import game.render.screen.GameScr;
import java.io.DataInputStream;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * Hộp thoại "Pokémon đã gục" (HTBB com.htbb.pokemon.HoiSinh / real.HoiSinhGame, lệnh -101). MÁY CHỦ quyết định
 * mọi thứ: danh sách lựa chọn, phí, lý do khoá, đếm ngược; client chỉ vẽ và gửi lựa chọn (server kiểm lại hết).
 * Server cũ không trả lời -101 thì client giữ hộp thoại "Về làng / Hồi sinh" của game gốc.
 *
 * Lệnh -101 server -> client: byte 0 MỞ {UTF tiêu đề, UTF mô tả, byte n, n x {byte mã, UTF tên, UTF chi tiết,
 * byte bấm được, UTF lý do khoá, short giây chờ}, short giây chờ đồng đội}; byte 1 ĐÓNG.
 * Client -> server: byte 0 XIN, byte 1 CHỌN + byte mã. Dùng chung jar / APK / PC Java (port từ client_jar/src).
 */
public final class HopHoiSinh extends Dialog {
    public static final int LENH = -101;

    /** Máy chủ đã trả lời -101 ít nhất một lần. */
    public static boolean coMayChu;
    private static HopHoiSinh hop;
    private static long lucXin;

    private String tieuDe = "";
    private String[] moTa = new String[0];
    private String moTaGoc = "";
    /** Khung vừa vẽ kiểu tấm cảm ứng (chạm đi qua VungCham, không dò toạ độ kiểu cũ). */
    private boolean veMoi;
    private Command trai, phai;
    private int n;
    private byte[] ma = new byte[0];
    private String[] ten = new String[0];
    private String[] chiTiet = new String[0];
    private boolean[] duoc = new boolean[0];
    private String[] khoa = new String[0];
    private int[] cho = new int[0];
    private int choDongDoi;
    private long lucNhan;
    private int chon;
    private int dau;
    private long lucGui;

    private HopHoiSinh() {
        this.left = new Command("Chọn", new IAction() {
            public void perform() {
                HopHoiSinh.this.bam(HopHoiSinh.this.chon);
            }
        });
        this.right = new Command("Đóng", new IAction() {
            public void perform() {
                Canvas.currentDialog = null;
            }
        });
    }

    // ---------------------------------------------------------------- mạng

    private static void gui(int loai, int maChon) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(loai);
            if (loai == 1) {
                m.writer().writeByte(maChon);
            }
            GameService.gI().session.sendMessage(m);
            m.cleanup();
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    /** Pokémon của mình vừa gục: xin hộp thoại (server mới trả MỞ, server cũ im lặng). */
    public static void khiGuc() {
        long bay = System.currentTimeMillis();
        if (bay - lucXin < 1500L) {
            return;
        }
        lucXin = bay;
        gui(0, 0);
    }

    /** Bấm phím bắn khi đang gục: true = server mới, đã xin mở lại hộp thoại (bỏ hộp thoại cũ). */
    public static boolean moLai() {
        if (!coMayChu) {
            return false;
        }
        if (hop != null && hop.n > 0) {
            Canvas.currentDialog = hop;
        }
        lucXin = 0L;
        khiGuc();
        return true;
    }

    /** Gói -101 (gọi ở đầu MessageHandler). true = đã xử lý. */
    public static boolean nhan(Message msg) {
        if (msg == null || msg.command != LENH) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            int loai = r.readByte();
            coMayChu = true;
            if (loai != 0) {
                dong();
                return true;
            }
            if (hop == null) {
                hop = new HopHoiSinh();
            }
            HopHoiSinh h = hop;
            h.tieuDe = r.readUTF();
            String mt = r.readUTF();
            h.moTaGoc = mt;
            int k = r.readUnsignedByte();
            h.ma = new byte[k];
            h.ten = new String[k];
            h.chiTiet = new String[k];
            h.duoc = new boolean[k];
            h.khoa = new String[k];
            h.cho = new int[k];
            for (int i = 0; i < k; i++) {
                h.ma[i] = r.readByte();
                h.ten[i] = r.readUTF();
                h.chiTiet[i] = r.readUTF();
                h.duoc[i] = r.readByte() != 0;
                h.khoa[i] = r.readUTF();
                h.cho[i] = r.readShort();
            }
            h.choDongDoi = r.readShort();
            h.n = k;
            h.lucNhan = System.currentTimeMillis();
            h.lucGui = 0L;
            h.moTa = Font.arialFont.splitFontBStrInLine(mt, rong() - 20);
            if (h.chon >= k || !h.bamDuoc(h.chon)) {
                h.chon = h.dauTienDuoc();
            }
            Canvas.menu.showMenu = false; // hộp thoại cũ / menu cũ (nếu có) nhường chỗ
            Canvas.currentDialog = h;
        } catch (Throwable e) {
            // gói hỏng
        }
        return true;
    }

    /** Đóng hộp thoại (đã hồi sinh / về Trung tâm). */
    public static void dong() {
        if (hop != null) {
            hop.n = 0;
            if (Canvas.currentDialog == hop) {
                Canvas.currentDialog = null;
            }
        }
    }

    // ---------------------------------------------------------------- trạng thái dòng

    private int conCho(int i) {
        int g = this.cho[i] - (int) ((System.currentTimeMillis() - this.lucNhan) / 1000L);
        return g < 0 ? 0 : g;
    }

    private boolean bamDuoc(int i) {
        return i >= 0 && i < this.n && this.duoc[i] && conCho(i) <= 0;
    }

    private int dauTienDuoc() {
        for (int i = 0; i < this.n; i++) {
            if (this.duoc[i]) {
                return i;
            }
        }
        return 0;
    }

    private void bam(int i) {
        if (i < 0 || i >= this.n) {
            return;
        }
        if (!this.duoc[i]) {
            Canvas.gameScr.addChat(new game.model.ChatInfo("", this.khoa[i], 0));
            return;
        }
        int g = conCho(i);
        if (g > 0) {
            Canvas.gameScr.addChat(new game.model.ChatInfo("", new StringBuffer("Chờ ").append(g).append(" giây nữa.").toString(), 0));
            return;
        }
        long bay = System.currentTimeMillis();
        if (bay - this.lucGui < 1500L) {
            return; // chống bấm đúp
        }
        this.lucGui = bay;
        gui(1, this.ma[i]);
    }

    // ---------------------------------------------------------------- vẽ

    private static final int H_DONG = 28;

    private static int rong() {
        int w = Canvas.w - 12;
        return w > 300 ? 300 : w;
    }

    private int hDau() {
        return 22 + this.moTa.length * 13 + 4;
    }

    private int soDongThay() {
        int con = Canvas.h - 40 - 16 - hDau() - 10;
        int k = con / H_DONG;
        if (k < 1) {
            k = 1;
        }
        return k > this.n ? this.n : k;
    }

    /**
     * 31/10/2026 (soi giao diện toàn bộ): hộp gục / hồi sinh - người chơi nào gục cũng thấy - còn vẽ kiểu J2ME cũ (dải đỏ,
     * hàng chữ nhỏ 28 px, dòng khoá bị cắt "...", phím mềm "Chọn"/"Đóng" tí hon) và không đăng ký vùng chạm. Máy cảm ứng:
     * tấm chung của giao diện (Bang.khungTam): mô tả, mỗi lựa chọn một hàng cao bằng cỡ chạm (tên + chi tiết, khoá = xám +
     * lý do đủ chữ), chạm hàng = chọn. Máy bấm phím giữ lối cũ (phím 2 8 / 5).
     */
    private void veCham(Graphics g) {
        int k = Bang.cach();
        int nhan = ChuDe.cao(ChuDe.NHAN), phu = ChuDe.cao(ChuDe.PHU);
        int hMax = Bang.duoi() - Bang.tren() - 8;
        int dau = Bang.caoDauTam(false) + ChuDe.DEM;
        // một cột; không đủ cao (màn ngang thấp: 6 lựa chọn) thì 2 cột - mọi lựa chọn (cả "Về Trung tâm") luôn thấy
        // thứ tự thử: 1 cột (chi tiết 2 dòng), 2 cột (2 dòng), 1 cột (1 dòng), 2 cột (1 dòng) - J2ME 320x240 thấp
        // 31/10/2026: màn dọc hẹp (J2ME 240x320): 2 cột chỉ còn ~100 px / ô -> chữ "Dùng Thuốc hồi.." -> thử 1 cột chi tiết
        // 1 dòng (mô tả 2 dòng) TRƯỚC 2 cột
        boolean hep = Canvas.w < 300;
        for (int thu = 0; thu < 4; thu++) {
            int lan = hep && thu == 1 ? 2 : hep && thu == 2 ? 1 : thu;
            int cot = lan == 0 || lan == 2 ? 1 : 2, md = lan < 2 ? 2 : 1;
            int wT = Math.min(Bang.phai() - Bang.trai() - 16, cot == 1 ? (ChuDe.dt() ? 520 : 420) : (ChuDe.dt() ? 760 : 620));
            wT = Math.min(wT, Bang.oTam(wT, 0)[2]);   // tấm bị kẹp <= 80 % vùng an toàn: ngắt dòng theo bề ngang thật
            int wNoi = wT - 2 * ChuDe.DEM - 8;
            int wO = (wNoi - (cot - 1) * k) / cot;
            String[] mt = ChuDe.ngat(this.moTaGoc, ChuDe.PHU, wNoi, hep && cot == 1 && md == 1 ? 1 : 3);
            int hMoTa = mt.length * phu + (mt.length > 0 ? k : 0);
            String[][] d2 = new String[this.n][];
            int hh = VungCham.MIN;
            for (int i = 0; i < this.n; i++) {
                d2[i] = ChuDe.ngat(dong2(i), ChuDe.PHU, wO - 16, md);
                for (int l = 0; l < d2[i].length; l++) {
                    d2[i][l] = ChuDe.cat(d2[i][l], ChuDe.PHU, wO - 16);   // dòng cuối của ngat có thể còn dài
                }
                hh = Math.max(hh, nhan + d2[i].length * phu + 8);
            }
            int hang = (this.n + cot - 1) / cot;
            int tong = hMoTa + hang * (hh + k);
            if (thu < 3 && dau + tong > hMax) {
                continue;
            }
            int[] c = Bang.khungTam(g, wT, Math.min(hMax, dau + tong), this.tieuDe, null, 0, null, new IAction() {
                public void perform() {
                    Canvas.currentDialog = null;
                }
            });
            int x = c[0] + 4, y = c[1], w = c[2] - 8;
            wO = (w - (cot - 1) * k) / cot;
            for (int i = 0; i < mt.length; i++) {
                ChuDe.chu(g, mt[i], x + w / 2, y + i * phu, ChuDe.PHU, ChuDe.CHU_PHU, 2);
            }
            y += hMoTa;
            for (int i = 0; i < this.n; i++) {
                final int li = i;
                int xo = x + (i / hang) * (wO + k), yo = y + (i % hang) * (hh + k);
                boolean bd = bamDuoc(i);
                Bang.hang(g, xo, yo, wO, hh, i, i == this.chon, new IAction() {
                    public void perform() {
                        HopHoiSinh.this.chon = li;
                        HopHoiSinh.this.bam(li);
                    }
                }, "hs-" + i);
                ChuDe.chu(g, ChuDe.cat(this.ten[i], ChuDe.NHAN, wO - 16), xo + 8, yo + 4, ChuDe.NHAN, bd ? ChuDe.CHU : ChuDe.XAM, 0);
                for (int l = 0; l < d2[i].length; l++) {
                    ChuDe.chu(g, ChuDe.cat(d2[i][l], ChuDe.PHU, wO - 16), xo + 8, yo + 4 + nhan + l * phu, ChuDe.PHU,
                            this.duoc[i] ? ChuDe.CHU_PHU : ChuDe.DO_TOI, 0);
                }
            }
            return;
        }
    }

    /** Dòng chi tiết của lựa chọn i (khoá / chờ / đồng đội). */
    private String dong2(int i) {
        String d;
        if (!this.duoc[i]) {
            d = new StringBuffer("Khoá: ").append(this.khoa[i]).toString();
        } else if (!bamDuoc(i)) {
            d = new StringBuffer(this.chiTiet[i]).append(" · chờ ").append(conCho(i)).append("s").toString();
        } else {
            d = this.chiTiet[i];
        }
        if (this.ma[i] == 6 && this.choDongDoi > 0) {
            int con = this.choDongDoi - (int) ((System.currentTimeMillis() - this.lucNhan) / 1000L);
            if (con > 0) {
                d = new StringBuffer("Đang chờ đồng đội: ").append(con).append("s").toString();
            }
        }
        return d;
    }

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        if (this.n <= 0) {
            super.paint(g);
            return;
        }
        this.veMoi = MenuNguCanh.bat();
        if (this.veMoi) {
            // máy cảm ứng: không vẽ dải phím mềm "Chọn" / "Đóng" tí hon (chạm hàng = chọn, X = đóng)
            if (this.left != null) {
                this.trai = this.left;
                this.phai = this.right;
                this.left = null;
                this.right = null;
            }
            veCham(g);
            return;
        }
        if (this.left == null && this.trai != null) {
            this.left = this.trai;
            this.right = this.phai;
        }
        int w = rong();
        int x = (Canvas.w - w) / 2;
        int thay = soDongThay();
        int hop = hDau() + thay * H_DONG + 10;
        int y = (Canvas.h - 40 - hop) / 2;
        if (y < 4) {
            y = 4;
        }
        GiaoDien.hop(g, x, y, w, hop);
        // dải tiêu đề đỏ Poké Ball
        GiaoDien.khoiDoc(g, x + 4, y + 4, w - 8, 18, 3, GiaoDien.DO_SANG, GiaoDien.DO);
        GiaoDien.pokeBall(g, x + 14, y + 13, 6, 0);
        Font.arialFontW.drawString(g, this.tieuDe, x + w / 2 + 6, y + 6, 2);
        int yy = y + 24;
        for (int i = 0; i < this.moTa.length; i++) {
            Font.arialFont.drawString(g, this.moTa[i], x + w / 2, yy + i * 13, 2);
        }
        yy = y + hDau();
        if (this.chon < this.dau) {
            this.dau = this.chon;
        }
        if (this.chon >= this.dau + thay) {
            this.dau = this.chon - thay + 1;
        }
        for (int k = 0; k < thay; k++) {
            int i = this.dau + k;
            if (i >= this.n) {
                break;
            }
            int ry = yy + k * H_DONG;
            boolean bd = bamDuoc(i);
            if (i == this.chon) {
                GiaoDien.chonSang(g, x + 6, ry, w - 12, H_DONG - 2);
                GiaoDien.conTro(g, x + 9, ry + 4, 8);
            } else if (!this.duoc[i]) {
                GiaoDien.khoi(g, x + 6, ry, w - 12, H_DONG - 2, 3, GiaoDien.XANH_NHAT);
            }
            String dong1 = this.ten[i];
            String dong2;
            if (!this.duoc[i]) {
                dong2 = new StringBuffer("Khoá: ").append(this.khoa[i]).toString();
            } else if (!bd) {
                dong2 = new StringBuffer(this.chiTiet[i]).append(" · chờ ").append(conCho(i)).append("s").toString();
            } else {
                dong2 = this.chiTiet[i];
            }
            if (this.ma[i] == 6 && this.choDongDoi > 0) {
                int con = this.choDongDoi - (int) ((System.currentTimeMillis() - this.lucNhan) / 1000L);
                if (con > 0) {
                    dong2 = new StringBuffer("Đang chờ đồng đội: ").append(con).append("s").toString();
                }
            }
            Font f1 = this.duoc[i] ? Font.blackFont : Font.arialFont;
            f1.drawString(g, GiaoDien.catChu(f1, dong1, w - 34), x + 20, ry + 1, 0);
            Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, dong2, w - 34), x + 20, ry + 14, 0);
        }
        if (this.dau > 0) {
            GiaoDien.muiTen(g, x + w - 14, yy - 6, 5, GiaoDien.VIEN);
        }
        if (this.dau + thay < this.n) {
            GiaoDien.muiXuong(g, x + w - 12, yy + thay * H_DONG);
        }
        super.paint(g);
    }

    public void update() {
        if (this.n <= 0) {
            Canvas.currentDialog = null;
            return;
        }
        GameScr gs = Canvas.gameScr;
        if (gs != null && gs.mainChar != null && gs.mainChar.hp > 0 && gs.mainChar.state != 3
                && System.currentTimeMillis() - this.lucNhan > 3000L) {
            dong(); // đã hồi sinh bằng đường khác
            return;
        }
        if (Canvas.keyPressed[2] || Canvas.keyPressed[4]) {
            Canvas.keyPressed[2] = false;
            Canvas.keyPressed[4] = false;
            this.chon = this.chon > 0 ? this.chon - 1 : this.n - 1;
        } else if (Canvas.keyPressed[8] || Canvas.keyPressed[6]) {
            Canvas.keyPressed[8] = false;
            Canvas.keyPressed[6] = false;
            this.chon = this.chon < this.n - 1 ? this.chon + 1 : 0;
        } else if (Canvas.keyPressed[5]) {
            Canvas.keyPressed[5] = false;
            bam(this.chon);
            return;
        }
        if (Canvas.isPointerClick && !this.veMoi) {
            int w = rong();
            int x = (Canvas.w - w) / 2;
            int thay = soDongThay();
            int hop = hDau() + thay * H_DONG + 10;
            int y = (Canvas.h - 40 - hop) / 2;
            if (y < 4) {
                y = 4;
            }
            int yy = y + hDau();
            for (int k = 0; k < thay && this.dau + k < this.n; k++) {
                if (Canvas.isPointer(x + 6, yy + k * H_DONG, w - 12, H_DONG - 2)) {
                    Canvas.isPointerClick = false;
                    int i = this.dau + k;
                    if (i == this.chon) {
                        bam(i);
                    } else {
                        this.chon = i;
                    }
                    return;
                }
            }
        }
        super.update();
    }
}
