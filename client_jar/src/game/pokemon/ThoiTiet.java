package game.pokemon;

import game.render.Canvas;
import game.render.screen.RMS;
import java.io.DataInputStream;
import javax.microedition.lcdui.Graphics;

/**
 * THỜI TIẾT / MÙA / NGÀY CỘNG ĐỒNG phía client (đề xuất 8, 0.9.8, bản 908). Máy chủ (HTBB ThoiTietGame) gửi gói -118
 * mỗi khi thời tiết bản đồ / khung giờ / phủ mùa / ngày cộng đồng đổi; client cũ không nhận gói này (vẫn -39).
 *
 * <ul>
 * <li>HUD: đĩa tròn biểu tượng thời tiết cạnh hàng nút trên (màn rộng; cùng cỡ / khoảng cách nút HUD), chấm cam khi đang
 * ngày cộng đồng. Chạm (hoặc Menu nhanh › Xem thời tiết trên máy phím) = thẻ gợi ý: tên, còn bao lâu, ảnh hưởng chiêu (máy chủ viết sẵn),
 * khung sau, mùa, ngày cộng đồng.</li>
 * <li>Đổi thời tiết / mở giờ vàng: thông báo nhanh (ThongBao) một lần.</li>
 * <li>Cài đặt (Menu nhanh): Hiệu ứng thời tiết Tắt / Thấp / Đầy đủ (mặc định: bản nhẹ J2ME Thấp, còn lại Đầy đủ),
 * Màu theo mùa Bật / Tắt (mặc định tắt ở bản nhẹ). Lưu RecordStore "htbbTT" / "htbbMM".</li>
 * </ul>
 * Vẽ: {@link ThoiTietVe}.
 */
public final class ThoiTiet {
    private ThoiTiet() {
    }

    public static final int LENH = -118;
    public static final int TAT = 0, THAP = 1, DAY_DU = 2;
    public static final String[] TEN_CHAT_LUONG = {"Tắt", "Thấp", "Đầy đủ"};

    /** Đã nhận gói -118 (máy chủ mới). */
    public static boolean co;
    /** Kiểm thử: vẽ cả trên bản đồ offline. */
    public static boolean thu;
    public static int thoiTiet = -1;
    public static String ten = "";
    public static String goiY = "";
    /** System.currentTimeMillis() lúc đổi khung. */
    public static long hetLuc;
    public static int tiepTheo = -1;
    public static int mauMua;
    public static String tenMauMua = "";
    public static int congDong;
    public static String tenDong = "";
    public static long congDongMoc;
    public static boolean coTrenMap;
    public static String goiYCongDong = "";

    public static int chatLuong = -1;
    public static boolean mauMuaBat = true;
    private static boolean daNap;

    /** Thẻ gợi ý đang mở. */
    public static boolean moThe;

    static final String[] TEN_TT = {"Quang đãng", "Nắng gắt", "Mưa", "Tuyết", "Bão cát", "Sương mù", "Gió mạnh"};

    public static String tenThoiTiet(int tt) {
        return tt >= 0 && tt < TEN_TT.length ? TEN_TT[tt] : "";
    }

    /**
     * Đang vẽ hạt thời tiết / mùa (đã có gói -118, chất lượng khác Tắt, trời có hạt hoặc mùa có phủ): HieuUng (mod
     * "Hoa đào"...) tự tắt hạt màn hình trong lúc này.
     */
    public static boolean dangVeHat() {
        if (!co || chatLuong <= TAT) {
            return false;
        }
        return thoiTiet > 0 || mauMuaBat && mauMua > 0;
    }

    // ------------------------------------------------------------------ cài đặt

    public static void napCaiDat() {
        if (daNap) {
            return;
        }
        daNap = true;
        boolean nhe = false;
        try {
            nhe = Banner.laBanNhe();
        } catch (Throwable e) {
            // bỏ
        }
        int c = -1, m = -1;
        try {
            c = RMS.loadRMSInt("htbbTT");
            m = RMS.loadRMSInt("htbbMM");
        } catch (Throwable e) {
            // RecordStore hỏng: mặc định
        }
        chatLuong = c >= TAT && c <= DAY_DU ? c : (nhe ? THAP : DAY_DU);
        mauMuaBat = m == 0 || m == 1 ? m == 1 : !nhe;
    }

    /** Menu nhanh: Tắt -> Thấp -> Đầy đủ -> Tắt. */
    public static void doiChatLuong() {
        napCaiDat();
        chatLuong = (chatLuong + 1) % 3;
        try {
            RMS.saveRMSInt("htbbTT", chatLuong);
        } catch (Throwable e) {
            // chỉ giữ trong phiên
        }
    }

    public static void doiMauMua() {
        napCaiDat();
        mauMuaBat = !mauMuaBat;
        try {
            RMS.saveRMSInt("htbbMM", mauMuaBat ? 1 : 0);
        } catch (Throwable e) {
            // chỉ giữ trong phiên
        }
    }

    // ------------------------------------------------------------------ gói -118

    /** MessageHandler: gói -118 (true = đã xử lý). */
    public static boolean nhan(network.Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        xuLy(msg.reader());
        return true;
    }

    public static void xuLy(DataInputStream r) {
        try {
            docGoi(r);
        } catch (Throwable e) {
            // gói hỏng / thiếu trường: giữ trạng thái cũ
        }
    }

    static void docGoi(DataInputStream r) throws java.io.IOException {
        int pb = r.readUnsignedByte();
        if (pb < 1) {
            return;
        }
        int tt = r.readByte();
        String tn = r.readUTF();
        String gy = r.readUTF();
        int giay = r.readInt();
        int sau = r.readByte();
        int mm = r.readByte();
        String tmm = r.readUTF();
        int cd = r.readUnsignedByte();
        String td = r.readUTF();
        int giayCd = r.readInt();
        boolean tren = r.readUnsignedByte() == 1;
        String gyCd = r.readUTF();
        datTrangThai(tt, tn, gy, giay, sau, mm, tmm, cd, td, giayCd, tren, gyCd);
    }

    /** Đặt trạng thái (gói / kiểm thử) + thông báo khi đổi. */
    public static void datTrangThai(int tt, String tn, String gy, int giay, int sau, int mm, String tmm, int cd, String td,
            int giayCd, boolean tren, String gyCd) {
        napCaiDat();
        boolean daCo = co;
        int ttCu = thoiTiet, cdCu = congDong;
        long bay = System.currentTimeMillis();
        co = true;
        thoiTiet = tt;
        ten = tn == null ? "" : tn;
        goiY = gy == null ? "" : gy;
        hetLuc = bay + Math.max(0, giay) * 1000L;
        tiepTheo = sau;
        mauMua = mm;
        tenMauMua = tmm == null ? "" : tmm;
        congDong = cd;
        tenDong = td == null ? "" : td;
        congDongMoc = bay + Math.max(0, giayCd) * 1000L;
        coTrenMap = tren;
        goiYCongDong = gyCd == null ? "" : gyCd;
        if (daCo && tt != ttCu && tt > 0 && goiY.length() > 0) {
            baoNhanh("Trời chuyển " + ten.toLowerCase() + ". " + goiY, ThongBao.TIN);
        }
        if (cd == 2 && cdCu != 2) {
            baoNhanh("Giờ vàng Ngày cộng đồng: dòng " + tenDong + " xuất hiện dày" + (tren ? " ngay tại đây!" : " trên các tuyến của nó!"),
                    ThongBao.SU_KIEN);
        }
    }

    static void baoNhanh(String s, int loai) {
        try {
            ThongBao.hien(s, loai);
        } catch (Throwable e) {
            // chưa có màn chơi
        }
    }

    // ------------------------------------------------------------------ HUD

    /** Đĩa HUD màn rộng: ngay trái hàng nút phải trên, cùng cỡ (HudRong.xTruocHangTren / yHangTren / rHangTren). */
    public static int xHud() {
        return game.render.screen.HudRong.xTruocHangTren();
    }

    /** Màn hẹp (máy phím J2ME): đĩa nhỏ giữa trên. */
    static int xHep() {
        return Canvas.hw;
    }

    public static boolean manRong() {
        return game.render.screen.HudRong.bat();
    }

    /** Vẽ đĩa thời tiết (+ thẻ gợi ý nếu mở) - GameScr.paint sau HUD. */
    public static void veHud(Graphics g) {
        if (!co || thoiTiet < 0 && congDong == 0) {
            moThe = false;
            return;
        }
        Canvas.resetTrans(g);
        boolean rong = manRong();
        int cx = rong ? xHud() : xHep(), cy = rong ? game.render.screen.HudRong.yHangTren() : 9;
        int r = rong ? game.render.screen.HudRong.rHangTren() : 8;
        GiaoDien.khoi(g, cx - r, cy - r + 1, 2 * r + 1, 2 * r + 1, r, ChuDe.BONG);
        // 0.9.8 giao diện NRO: đĩa gỗ viền đậm (không còn đĩa trắng kiểu GO)
        GiaoDien.khoi(g, cx - r, cy - r, 2 * r + 1, 2 * r + 1, r, ChuDe.VIEN);
        GiaoDien.khoi(g, cx - r + 1, cy - r + 1, 2 * r - 1, 2 * r - 1, r - 1, ChuDe.NEN);
        ThoiTietVe.veIcon(g, thoiTiet < 0 ? ThoiTietVe.QUANG : thoiTiet, cx, cy, 2 * r - 2);
        if (congDong > 0) {
            // chấm ngày cộng đồng: cam khi giờ vàng, vàng khi chờ
            g.setColor(congDong == 2 ? 0xF07A1E : 0xF5B921);
            g.fillArc(cx + r - 6, cy - r - 1, 8, 8, 0, 360);
            g.setColor(ChuDe.VIEN);
            g.drawArc(cx + r - 6, cy - r - 1, 8, 8, 0, 360);
        }
        boolean cham = rong || Canvas.instance != null && Canvas.instance.hasPointerEvents();
        if (cham && Canvas.currentDialog == null && Canvas.currentScreen == Canvas.gameScr) {
            VungCham.themTuyetDoi(cx - r, cy - r, 2 * r + 1, 2 * r + 1, BAM, "hud-thoi-tiet");
        }
        if (moThe) {
            veThe(g, rong ? cx : Canvas.hw, rong ? cy + r + 6 : cy + r + 4);
        }
    }

    static final Runnable BAM = new Runnable() {
        public void run() {
            moThe = !moThe;
            try {
                AmThanh.phat(AmThanh.CLICK);
            } catch (Throwable e) {
                // không âm thanh
            }
        }
    };

    /** Menu nhanh "Xem thời tiết" (cả máy phím): mở thẻ gợi ý. */
    public static void moTheGoiY() {
        moThe = co;
    }

    /** "2 giờ 10 phút" / "8 phút". */
    public static String conLai(long ms) {
        long ph = Math.max(0, ms) / 60000L;
        if (ph >= 60) {
            return (ph / 60) + " giờ " + (ph % 60) + " phút";
        }
        return Math.max(1, ph) + " phút";
    }

    /** Các dòng của thẻ gợi ý (kiểm thử đọc). */
    public static String[] dongThe() {
        long bay = System.currentTimeMillis();
        java.util.Vector v = new java.util.Vector();
        if (thoiTiet >= 0) {
            v.addElement(ten + " · còn " + conLai(hetLuc - bay));
            if (goiY.length() > 0) {
                v.addElement(goiY);
            }
            if (tiepTheo >= 0 && tiepTheo != thoiTiet) {
                v.addElement("Sau đó: " + tenThoiTiet(tiepTheo));
            }
        } else {
            v.addElement("Trong nhà: không có thời tiết.");
        }
        if (mauMua > 0 && tenMauMua.length() > 0) {
            v.addElement("Mùa: " + tenMauMua + (mauMuaBat ? "" : " (đã tắt màu)"));
        }
        if (congDong > 0) {
            v.addElement(congDong == 2 ? "Ngày cộng đồng: giờ vàng, còn " + conLai(congDongMoc - bay)
                    : "Ngày cộng đồng: giờ vàng sau " + conLai(congDongMoc - bay));
            if (goiYCongDong.length() > 0) {
                v.addElement(goiYCongDong);
            }
        }
        String[] r = new String[v.size()];
        v.copyInto(r);
        return r;
    }

    /** Vùng thẻ vừa vẽ {x, y, w, h} (kiểm thử: nằm trong màn). */
    public static final int[] VUNG_THE = new int[4];

    static void veThe(Graphics g, int cxNeo, int yTren) {
        String[] d = dongThe();
        int w = Math.min(Canvas.w - 16, ChuDe.dt() ? 260 : 230);
        int pad = ChuDe.DEM;
        int lh = ChuDe.cao(ChuDe.PHU) + 2;
        java.util.Vector hang = new java.util.Vector();
        for (int i = 0; i < d.length; i++) {
            String[] l = ChuDe.ngat(d[i], i == 0 ? ChuDe.NHAN : ChuDe.PHU, w - 2 * pad, 4);
            for (int k = 0; k < l.length; k++) {
                hang.addElement(new Object[]{l[k], new Integer(i)});
            }
        }
        int h = pad * 2 + hang.size() * lh + 2;
        int x = Math.max(8, Math.min(Canvas.w - 8 - w, cxNeo - w / 2));
        int y = Math.min(yTren, Canvas.h - 8 - h);
        VUNG_THE[0] = x;
        VUNG_THE[1] = y;
        VUNG_THE[2] = w;
        VUNG_THE[3] = h;
        ChuDe.the(g, x, y, w, h, ChuDe.BO_THE);
        int yy = y + pad;
        for (int i = 0; i < hang.size(); i++) {
            Object[] o = (Object[]) hang.elementAt(i);
            int dong = ((Integer) o[1]).intValue();
            boolean cd = congDong > 0 && ((String) o[0]).startsWith("Ngày cộng đồng");
            int mau = dong == 0 ? ChuDe.CHU : cd ? ChuDe.CAM : ChuDe.CHU_PHU;
            ChuDe.chu(g, (String) o[0], x + pad, yy, dong == 0 ? ChuDe.NHAN : ChuDe.PHU, mau, 0);
            yy += lh;
        }
        // chạm thẻ / ra ngoài = đóng (vùng nền phủ lớp dưới đĩa)
        VungCham.themTuyetDoi(x, y, w, h, BAM, "nen-the-thoi-tiet");
    }
}
