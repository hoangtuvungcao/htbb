package game.pokemon;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * MÀN TẢI DUY NHẤT (0.9.5): từ đăng nhập tới vào thế giới là MỘT trạng thái + MỘT thành phần vẽ - không còn hộp chờ
 * "Đang kết nối" / "Xin chờ.." chồng lên màn chuyển bản đồ (hai vòng quay). Các bước:
 * khởi động (tải dữ liệu game) -> kết nối máy chủ -> đăng nhập -> tải Pokémon -> tải bản đồ -> vào thế giới.
 * Đổi bản đồ / dịch chuyển / tự kết nối lại dùng cùng thành phần (kèm tên bản đồ / lần thử).
 *
 * Nơi đặt bước: LoginScr / DangKyScr (kết nối), GameScr.onConnectOK (đăng nhập), CharSelectScr chọn Pokémon (tải
 * Pokémon), GameScr.onMainCharInfo (tải bản đồ), ChangScr (đổi bản đồ), TuNoiLai (kết nối lại), GameScr.switchToMe
 * (xong). Vẽ: {@link #HOP} (hộp chặn khi chưa có màn tải riêng), ChangScr.paint, Canvas (khởi động), CharSelectScr
 * (đang tự kết nối lại: không lộ màn chọn Pokémon).
 */
public final class ManTai {
    public static final int KHOI_DONG = 0, KET_NOI = 1, DANG_NHAP = 2, NHAN_VAT = 3, BAN_DO = 4;
    static final String[] TEN = {"Đang tải dữ liệu game", "Đang kết nối máy chủ", "Đang đăng nhập", "Đang tải Pokémon của bạn",
        "Đang tải bản đồ"};

    /** bước hiện tại */
    public static int buoc = KHOI_DONG;
    /** true = đang đi đường đăng nhập -> thế giới (hiện dãy bước); false = đổi bản đồ trong game */
    public static boolean dangVao;
    /** dòng phụ (lần thử kết nối lại...); null = không */
    public static String phu;
    /** tên bản đồ đích (đổi bản đồ) */
    public static String tenMap;
    private static long tu = System.currentTimeMillis(), tuBuoc = tu;
    /** tiến trình đã hiện (phần nghìn) - chỉ tăng */
    private static int daHien;

    /** Hộp chặn (Canvas.currentDialog) cho các bước chưa có màn riêng; nút "Huỷ" = phím mềm phải như hộp chờ cũ. */
    public static final Hop HOP = new Hop();

    private ManTai() {
    }

    public static final class Hop extends game.render.screen.Dialog {
        Hop() {
            this.right = new game.model.Command("Huỷ", new game.model.IAction() {
                public void perform() {
                    if (Canvas.currentDialog == HOP) {
                        Canvas.endDlg();
                    }
                    dangVao = false;
                }
            });
        }

        public void paint(Graphics g) {
            // màn chuyển bản đồ đang vẽ đúng thành phần này -> không vẽ lần hai
            if (!(Canvas.currentScreen instanceof game.render.screen.ChangScr)) {
                ve(g);
            }
            super.paint(g);
        }
    }

    /** Bắt đầu (hoặc tiếp) đường vào game ở bước b; hiện hộp chặn. */
    public static void batDau(int b, String dongPhu) {
        if (!dangVao || b <= KET_NOI && buoc > KET_NOI && !TuNoiLai.dangNoi) {
            tu = System.currentTimeMillis();
            daHien = 0;
        }
        dangVao = true;
        tenMap = null;
        if (dongPhu != null || !TuNoiLai.dangNoi) {
            phu = dongPhu;
        }
        datBuoc(b);
        HOP.show();
    }

    /** Sang bước b (chỉ khi đang vào game). */
    public static void toi(int b) {
        if (dangVao && b > buoc) {
            datBuoc(b);
        }
    }

    private static void datBuoc(int b) {
        buoc = b;
        tuBuoc = System.currentTimeMillis();
    }

    /** Đổi bản đồ trong game (ChangScr): cùng thành phần, kèm tên bản đồ, không dãy bước. */
    public static void doiMap(int idMap) {
        dangVao = false;
        phu = null;
        tenMap = idMap >= 0 ? VungMap.ten(idMap) : null;
        tu = System.currentTimeMillis();
        daHien = 0;
        datBuoc(BAN_DO);
    }

    /** Đã vào thế giới (GameScr.switchToMe): hết đường vào; gỡ hộp chặn nếu còn. */
    public static void xong() {
        dangVao = false;
        phu = null;
        if (Canvas.currentDialog == HOP) {
            Canvas.endDlg();
        }
    }

    public static boolean dangMo() {
        return Canvas.currentDialog == HOP;
    }

    /** Tiến trình 0..1000: đường vào chia đều 4 bước (mỗi bước chạy chậm dần về cuối đoạn); đổi bản đồ: một đoạn. */
    static int tienTrinh() {
        long now = System.currentTimeMillis();
        int p;
        if (dangVao && buoc >= KET_NOI) {
            long t = now - tuBuoc;
            int doan = 1000 / 4;
            p = (buoc - KET_NOI) * doan + (int) (doan * 9L / 10 * t / (t + 900L));
        } else {
            long t = now - tu;
            p = (int) (950L * t / (t + 1200L));
        }
        if (p < daHien) {
            p = daHien;
        }
        daHien = p > 990 ? 990 : p;
        return daHien;
    }

    /** Vẽ màn tải ở bước hiện tại. */
    public static void ve(Graphics g) {
        ve(g, buoc);
    }

    public static void ve(Graphics g, int b) {
        Canvas.resetTrans(g);
        int w = Canvas.w, h = Canvas.h;
        g.setClip(0, 0, w, h);
        boolean coBanner = false;
        try {
            coBanner = Banner.ve(g, -1, b == KHOI_DONG);
        } catch (Throwable e) {
            // thiếu ảnh banner
        }
        Canvas.resetTrans(g);
        if (!coBanner) {
            GiaoDien.nenTroi(g, w, h);
        }
        try {
            if (coBanner) {
                ThuongHieu.veLogo(g, w >> 1, Math.max(8, (h >> 1) - 90), w);
            } else {
                ThuongHieu.veTieuDe(g, w >> 1, (h >> 1) - 60, w);
            }
        } catch (Throwable e) {
            // font chưa nạp
        }
        boolean day = dangVao && b >= KET_NOI;
        String dong2 = phu != null ? phu : tenMap;
        int lh = 13, lp = 11;
        try {
            lh = ChuDe.cao(ChuDe.NHAN);
            lp = ChuDe.cao(ChuDe.PHU);
        } catch (Throwable e) {
            // phông chưa nạp (khởi động)
        }
        int cw = Math.min(w - 32, 300);
        int ch = 12 + lh + (dong2 != null ? lp : 0) + 10 + (day ? 12 : 0) + 8;
        int cx = (w - cw) / 2;
        int cy = Math.min(h - 10 - ch, h * 2 / 3 - ch / 2);
        int[] kd = Banner.vungKhoiDau();
        if (kd != null && cy + ch > kd[1] - 4) {
            cy = Math.max(h / 3, kd[1] - 4 - ch);   // không che đội hình Pokémon khởi đầu
        }
        ChuDe.the(g, cx, cy, cw, ch, ChuDe.BO);
        int r = 11;
        int bx = cx + 12 + r, by = cy + 10 + r;
        GiaoDien.dangTai(g, bx, by, r);
        int tx = bx + r + 10, tw = cx + cw - 12 - tx;
        StringBuffer t = new StringBuffer(b >= 0 && b < TEN.length ? TEN[b] : TEN[0]);
        int cham = (int) ((System.currentTimeMillis() / 400) % 4);
        for (int i = 0; i < cham; i++) {
            t.append('.');
        }
        int y = cy + 9;
        try {
            ChuDe.chu(g, ChuDe.cat(t.toString(), ChuDe.NHAN, tw), tx, y, ChuDe.NHAN, ChuDe.CHU, 0);
            if (dong2 != null) {
                ChuDe.chu(g, ChuDe.cat(dong2, ChuDe.PHU, tw), tx, y + lh, ChuDe.PHU, ChuDe.CHU_PHU, 0);
            }
        } catch (Throwable e) {
            // phông chưa nạp (khởi động)
        }
        y += lh + (dong2 != null ? lp : 0);
        y += 4;
        // thanh tiến trình
        int hh = 6;
        int hh0 = hh;
        hh = 8;   // 0.9.7: rãnh tối pixel + dải vàng (GiaoDien.thanh)
        int dai = tw * (b == KHOI_DONG ? (int) (950L * (System.currentTimeMillis() - tu) / (System.currentTimeMillis() - tu + 1500L)) : tienTrinh()) / 1000;
        if (dai < hh) {
            dai = hh;
        }
        GiaoDien.thanh(g, tx, y, tw, hh, dai, tw, 2);
        y += hh + 6;
        if (day) {
            // dãy bước: xong = xanh lá, đang làm = xanh dương, chưa = xám
            int n = BAN_DO - KET_NOI + 1;
            int kc = tw / n;
            for (int i = 0; i < n; i++) {
                int bi = KET_NOI + i;
                int mau = bi < b ? ChuDe.LA : bi == b ? ChuDe.CHINH : ChuDe.KE;
                int xc = tx + kc * i + kc / 2;
                GiaoDien.khoi(g, xc - 3, y, 7, 7, 3, mau);
                if (i > 0) {
                    g.setColor(bi <= b ? ChuDe.LA : ChuDe.KE);
                    g.fillRect(xc - kc + 5, y + 3, kc - 10, 1);
                }
            }
        }
    }
}
