package game.pokemon;

import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import java.io.DataInputStream;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * THẺ PHÒNG RIÊNG (0.9.7, client BAN &gt;= 908): Phó bản Hầm ngục (tầng, tiến độ, đội còn sống, đếm ngược) và trận Cúp
 * PvP tuần (vòng, hai bên, đếm ngược). Máy chủ gửi lệnh -112 (com.htbb.pokemon.PhongRieng.viet): byte loại (0 = ẩn),
 * UTF tiêu đề, UTF dòng 1, UTF dòng 2, short giây còn lại (-1 = không đếm), byte phần trăm (-1 = không thanh).
 *
 * Vẽ bằng thành phần chung (Bang.the + ChuDe, giữa trên màn, dưới vùng an toàn) - không ảnh riêng. Chạm thẻ = gửi lại
 * -112 byte 0: máy chủ hỏi xác nhận "Rời phó bản" / "Bỏ cuộc" bằng menu (không rời ngay khi chạm nhầm).
 */
public final class PhongRieng {
    public static final int LENH = -112;
    public static final int AN = 0, PHO_BAN = 1, CUP = 2;

    static int loai;
    static String tieuDe = "", dong1 = "", dong2 = "";
    static int giay = -1, phanTram = -1;
    static long luc;
    /** Bản đồ lúc nhận thẻ: đổi bản đồ (rời phòng mà không nhận gói ẩn, vd mất mạng) thì tự ẩn. */
    static int mapLuc = Integer.MIN_VALUE;
    static final int KHONG_RO = Integer.MIN_VALUE;

    static int mapHienTai() {
        try {
            return Canvas.gameScr != null ? Canvas.gameScr.map : KHONG_RO;
        } catch (Throwable e) {
            return KHONG_RO;
        }
    }

    private PhongRieng() {
    }

    /** Gói -112 (gọi ở đầu MessageHandler). true = đã xử lý. */
    public static boolean nhan(Message msg) {
        if (msg == null || msg.command != LENH) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            loai = r.readByte();
            tieuDe = r.readUTF();
            dong1 = r.readUTF();
            dong2 = r.readUTF();
            giay = r.readShort();
            phanTram = r.readByte();
            luc = System.currentTimeMillis();
            mapLuc = mapHienTai();
        } catch (Throwable e) {
            loai = AN; // gói hỏng: ẩn thẻ, không làm vỡ màn chơi
        }
        return true;
    }

    public static boolean dangHien() {
        return loai != AN;
    }

    /** Giây còn lại (tự đếm ở client từ lúc nhận gói), -1 nếu không đếm. */
    public static int conLai() {
        if (giay < 0) {
            return -1;
        }
        int g = giay - (int) ((System.currentTimeMillis() - luc) / 1000L);
        return g < 0 ? 0 : g;
    }

    static String mmss(int g) {
        int p = g / 60, s = g % 60;
        return (p < 10 ? "0" : "") + p + ":" + (s < 10 ? "0" : "") + s;
    }

    private static final IAction CHAM = new IAction() {
        public void perform() {
            try {
                Message m = new Message(LENH);
                m.writer().writeByte(0);
                GameService.gI().session.sendMessage(m);
                m.cleanup();
            } catch (Throwable e) {
                // mất kết nối
            }
        }
    };

    /** Bề ngang thẻ: ~42 % màn rộng, tối thiểu 150, tối đa 260 px logic, không quá vùng an toàn. */
    static int rong() {
        int an = Bang.phai() - Bang.trai();
        return Math.min(an, Math.min(260, Math.max(150, Canvas.w * 42 / 100)));
    }

    /** Đỉnh thẻ: dưới vùng an toàn và dưới mọi nút HUD trên cùng chồng theo chiều ngang (đã đăng ký chạm trước). */
    static int dinh(int x, int w) {
        int y = Bang.tren() + 2;
        try {
            int n = VungCham.dem();
            for (int i = 0; i < n; i++) {
                String t = VungCham.ten(i);
                if (t == null || !t.startsWith("hud-")) {
                    continue;
                }
                int[] v = VungCham.vungVe(i);
                if (v == null || v[1] > Canvas.h / 3 || v[0] >= x + w || v[0] + v[2] <= x) {
                    continue;
                }
                y = Math.max(y, v[1] + v[3] + 3);
            }
        } catch (Throwable e) {
            // không có vùng chạm: giữ mép trên
        }
        return y;
    }

    /** Vẽ thẻ (GameScr.paint, sau HUD). */
    public static void ve(Graphics g) {
        if (loai == AN) {
            return;
        }
        if (mapLuc != KHONG_RO && mapHienTai() != mapLuc) {
            loai = AN;
            return;
        }
        if (g == null) {
            return;
        }
        int w = rong();
        int lt = ChuDe.cao(ChuDe.NHAN), l1 = ChuDe.cao(ChuDe.THUONG), l2 = ChuDe.cao(ChuDe.PHU);
        int dem = conLai();
        int pad = 5;
        int hThanh = phanTram >= 0 ? 5 : 0;
        int h = pad + lt + l1 + (dong2.length() > 0 ? l2 : 0) + (hThanh > 0 ? hThanh + 3 : 0) + pad;
        h = Math.max(h, VungCham.MIN);
        int x = (Canvas.w - w) / 2;
        int y = dinh(x, w);
        Bang.the(g, x, y, w, h);
        int mau = loai == CUP ? ChuDe.CAM : ChuDe.CHINH;
        g.setColor(mau);
        g.fillRect(x + 4, y + pad, 3, h - 2 * pad); // gạch màu nhận diện (teal phó bản / cam cúp)
        int tx = x + 12;
        int tw = w - 12 - pad;
        String dongDem = dem >= 0 ? mmss(dem) : null;
        int wDem = dongDem == null ? 0 : ChuDe.rong(dongDem, ChuDe.SO) + 6;
        int cy = y + pad;
        ChuDe.chu(g, ChuDe.cat(tieuDe, ChuDe.NHAN, tw - wDem), tx, cy, ChuDe.NHAN, ChuDe.CHU, 0);
        if (dongDem != null) {
            ChuDe.chu(g, dongDem, x + w - pad, cy, ChuDe.SO, dem <= 30 ? ChuDe.DO : ChuDe.CHU_PHU, 1);
        }
        cy += lt;
        ChuDe.chu(g, ChuDe.cat(dong1, ChuDe.THUONG, tw), tx, cy, ChuDe.THUONG, ChuDe.CHU, 0);
        cy += l1;
        if (dong2.length() > 0) {
            ChuDe.chu(g, ChuDe.cat(dong2, ChuDe.PHU, tw), tx, cy, ChuDe.PHU, ChuDe.CHU_PHU, 0);
            cy += l2;
        }
        if (hThanh > 0) {
            GiaoDien.thanh(g, tx, cy + 2, tw, hThanh, phanTram, 100, 2);
        }
        VungCham.them(g, x, y, w, h, CHAM, "phong_rieng");
    }

    /** Chỉ cho kiểm thử: đặt trạng thái như vừa nhận gói. */
    public static void datThu(int l, String t, String d1, String d2, int gi, int pt) {
        loai = l;
        tieuDe = t == null ? "" : t;
        dong1 = d1 == null ? "" : d1;
        dong2 = d2 == null ? "" : d2;
        giay = gi;
        phanTram = pt;
        luc = System.currentTimeMillis();
        mapLuc = mapHienTai();
    }
}
