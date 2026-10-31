package game.pokemon;

import game.render.Canvas;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Hộp thoại NPC (0.9.7 kiểu NRO): HỘP GỖ viền vàng ở ĐÁY màn, ô CHÂN DUNG NPC khung vàng bên trái (NPC có hình),
 * BIỂN TÊN đỏ trên mép hộp, chữ hiện dần (máy chữ), tam giác ▼ nhấp nháy khi hết dòng. Dùng cho lời thoại nhiệm vụ / nói chuyện NPC
 * (GameScr.addChat khi đang trò chuyện) thay bong bóng trên đầu; phím / chạm vẫn do GameScr xử lý.
 */
public final class HopThoaiDuoi {
    private static String ten;
    private static String[] dong;
    private static int tuTick;
    private static boolean bat;
    private static long hetLuc;
    /** giữ mở (đang trò chuyện có bước tiếp: mở đầu, nhận / trả nhiệm vụ) - không tự tắt theo giờ */
    private static boolean giu;
    /** bản đồ + vị trí người chơi lúc hiện lời: đổi map / đi xa thì tắt lời thông tin */
    private static int mapLuc = -1;
    private static int xLuc, yLuc;
    /** số lần đặt lời (GameScr biết chạm có làm sang lời mới không) */
    private static int soLan;
    /** hiện hết chữ ngay (chạm lần đầu khi máy chữ đang chạy) */
    private static boolean hienHet;

    private HopThoaiDuoi() {
    }

    /** số Pokédex NPC đang nói (chân dung trái hộp), -1 = không hình */
    private static int dex = -1;

    /** NPC đang chọn có hình Pokémon -> số dex, không thì -1. */
    static int dexDangNoi(String nguoiNoi) {
        try {
            game.model.Actor a = Canvas.gameScr == null ? null : Canvas.gameScr.focusedActor;
            if (nguoiNoi != null && nguoiNoi.length() > 0 && a instanceof game.model.NPC) {
                return ((game.model.NPC) a).getPokeDex();
            }
        } catch (Throwable e) {
            // bỏ qua
        }
        return -1;
    }

    /** Lời mới của người nói (ten có thể rỗng). */
    public static void dat(String nguoiNoi, String loi, int tickSong) {
        if (loi != null && loi.length() > 1 && (loi.charAt(0) == '0' || loi.charAt(0) == '1')) {
            // 31/10/2026: ký tự người nói của lời thoại nhiệm vụ ("1Luvdisc: ...") lọt ra hộp - bỏ khi theo sau là chữ hoa
            // (tên người nói); "1 viên", "10 con", "1/2" giữ nguyên
            char c = loi.charAt(1);
            if (c >= 'A' && c <= 'Z' || c == '\u0110') {
                loi = loi.substring(1);
            }
        }
        ten = nguoiNoi;
        dex = dexDangNoi(nguoiNoi);
        hetLuc = System.currentTimeMillis() + thoiGianDoc(loi == null ? 0 : loi.length(), tickSong);
        int w = rongLong();
        loiGoc = loi == null ? "" : loi;
        wNgat = w;
        dong = Font.arialFont.splitFontBStrInLine(loiGoc, w);
        tuTick = Canvas.gameTick;
        hienHet = false;
        trang = 0;
        bat = true;
        soLan++;
        mapLuc = game.model.Tilemap.lv;
        if (Canvas.gameScr != null && Canvas.gameScr.mainChar != null) {
            xLuc = Canvas.gameScr.mainChar.x;
            yLuc = Canvas.gameScr.mainChar.y;
        }
    }

    /**
     * Thời gian lời THÔNG TIN còn trên màn (ms): đủ đọc (~70 ms / ký tự + 2.5 giây), 3..10 giây; không
     * vượt thời gian bong bóng gốc (tickSong ~ 40 ms) nếu dài hơn 3 giây.
     */
    public static long thoiGianDoc(int soKyTu, int tickSong) {
        long ms = 2500L + soKyTu * 70L;
        if (ms > 10000L) {
            ms = 10000L;
        }
        long goc = tickSong * 40L;
        if (goc >= 3000L && goc < ms) {
            ms = goc;
        }
        return ms < 3000L ? 3000L : ms;
    }

    /** Đang trò chuyện có bước tiếp (GameScr đặt mỗi khung): hộp không tự tắt, hiện gợi ý "Chạm để tiếp". */
    public static void giu(boolean g) {
        if (giu && !g) {
            // hết trò chuyện: lời cuối thành lời thông tin, tự tắt sau ít giây
            hetLuc = Math.min(hetLuc, System.currentTimeMillis() + 3000L);
        }
        giu = g;
    }

    public static boolean dangGiu() {
        return giu;
    }

    public static int soLan() {
        return soLan;
    }

    /**
     * 31/10/2026 (soi giao diện hành trình, J2ME 240x320 / 320x240): lời dài hơn số dòng của hộp (3-4) bị bỏ mất phần
     * sau. Nay chia TRANG: chạm / bấm khi trang đã hiện hết = sang trang kế; xong trang cuối mới là "xong lời".
     */
    private static int trang;
    private static String loiGoc = "";
    private static int wNgat;

    private static int soTrang() {
        String[] d = HopThoaiDuoi.dong;
        int n = soDong();
        return d == null ? 1 : Math.max(1, (d.length + n - 1) / n);
    }

    /** Máy chữ đã hiện hết trang đang xem. */
    private static boolean trangXong() {
        String[] dong = HopThoaiDuoi.dong;
        if (hienHet || dong == null) {
            return true;
        }
        int n = soDong();
        int tong = 0;
        for (int i = trang * n; i < dong.length && i < (trang + 1) * n; i++) {
            tong += dong[i].length();
        }
        int tick = Canvas.gameTick - tuTick;
        if (tick < 0) {
            tick += 10001;
        }
        return tick * 2 >= tong;
    }

    /** Lời còn trang sau trang đang xem (phím 5 khi đang trò chuyện = sang trang, không bỏ qua lời). */
    public static boolean conTrang() {
        return dong != null && trang < soTrang() - 1;
    }

    /** Máy chữ đã hiện hết lời (cả trang cuối). */
    public static boolean xongChu() {
        return trangXong() && trang >= soTrang() - 1;
    }

    /** Chạm khi chưa xong: hiện hết trang này, hoặc sang trang kế. */
    public static void hienHetChu() {
        if (!trangXong()) {
            hienHet = true;
        } else if (trang < soTrang() - 1) {
            trang++;
            tuTick = Canvas.gameTick;
            hienHet = false;
            hetLuc = System.currentTimeMillis() + 6000L;
        }
    }

    public static void tat() {
        bat = false;
        dong = null;
        hienHet = false;
    }

    public static boolean dangBat() {
        if (bat && !giu) {
            if (System.currentTimeMillis() > hetLuc || mapLuc != game.model.Tilemap.lv || diXa()) {
                tat();
            }
        } else if (bat && mapLuc != game.model.Tilemap.lv) {
            tat();
        }
        return bat && dong != null;
    }

    /** Người chơi đã đi xa chỗ nghe lời (> 3 ô). */
    private static boolean diXa() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return false;
        }
        int dx = Canvas.gameScr.mainChar.x - xLuc;
        int dy = Canvas.gameScr.mainChar.y - yLuc;
        return dx * dx + dy * dy > 48 * 48;
    }

    /** cạnh ô chân dung (0 = không có) */
    private static int coChanDung() {
        return dex > 0 ? (Canvas.h >= 260 ? 46 : 36) : 0;
    }

    private static int rongLong() {
        int c = coChanDung();
        return Canvas.w - 24 - (c > 0 ? c + 8 : 0);
    }

    /** Số dòng hiện một lúc (3 trên máy cao, 2 trên máy thấp 240 px). */
    private static int soDong() {
        int toiDa = Canvas.h >= 260 ? 4 : 3;
        String[] dong = HopThoaiDuoi.dong;
        int n = dong == null ? 1 : dong.length;
        return n < 1 ? 1 : (n > toiDa ? toiDa : n);
    }

    public static void ve(Graphics g) {
        if (!dangBat()) {
            return;
        }
        if (dong != null && rongLong() != wNgat) {
            // 31/10/2026: đổi cỡ cửa sổ PC / xoay màn giữa lời: ngắt dòng lại theo bề ngang mới (trước: dòng tràn khỏi hộp)
            wNgat = rongLong();
            dong = Font.arialFont.splitFontBStrInLine(loiGoc, wNgat);
        }
        // 0.9.4: luồng game có thể tat() (dong = null) giữa lúc vẽ -> giữ bản cục bộ
        String[] dong = HopThoaiDuoi.dong;
        String ten = HopThoaiDuoi.ten;
        if (dong == null) {
            return;
        }
        if (Canvas.currentDialog != null) {
            // 31/10/2026 (soi giao diện hành trình): NPC nói rồi hỏi ngay ("Cháu mang thư ... nhé?"): hộp hỏi chặn nhịp game
            // nên chữ chạy dừng giữa chừng ("...cách đánh và cách d"). Có hộp đè lên thì hiện trọn lời (ngữ cảnh câu hỏi).
            hienHet = true;
        }
        Canvas.resetTrans(g);
        int n = soDong();
        int lh = 14;
        int h = n * lh + 14;
        {
            // dòng cuối trang dài tới chỗ "Chạm để tiếp" (góc phải dưới): thêm một hàng cho gợi ý, không đè chữ
            int t0 = Math.min(trang, soTrang() - 1) * n;
            int cuoi = Math.min(dong.length, t0 + n) - 1;
            String goiY = Canvas.instance != null && Canvas.instance.hasPointerEvents() ? "Chạm để tiếp" : "Bấm để tiếp";
            if (giu && cuoi >= t0 && Font.arialFont.getWidth(dong[cuoi]) + Font.arialFont.getWidth(goiY) + 40 > Canvas.w - 24) {
                h += lh;
            }
        }
        int y = Canvas.h - game.render.screen.Screen.deltaY - h - 2;
        int x = 3;
        int w = Canvas.w - 6;
        int cd = coChanDung();
        h = Math.max(h, cd + 12);
        y = Canvas.h - game.render.screen.Screen.deltaY - h - 2;
        GiaoDien.hop(g, x, y, w, h);
        int xChu = x + 10;
        if (cd > 0) {
            int px = x + 7, py = y + (h - cd) / 2;
            if (!KhungNro.ve(g, KhungNroBang.O_CHON, px, py, cd, cd)) {
                GiaoDien.khoi(g, px, py, cd, cd, 3, ChuDe.VANG);
            }
            int kx = g.getClipX(), ky = g.getClipY(), kw = g.getClipWidth(), kh = g.getClipHeight();
            g.clipRect(px + 4, py + 4, cd - 8, cd - 8);
            PokeBody.paintChanDung(g, dex, false, px + cd / 2, py + cd / 2 + 1, cd - 8);
            Canvas.resetTrans(g);
            g.setClip(kx, ky, kw, kh);
            xChu = px + cd + 8;
        }
        // biển tên người nói (đỏ viền vàng, trên mép trái hộp)
        if (ten != null && ten.length() > 0) {
            String t = GiaoDien.catChu(Font.borderFont, ten, w / 2);
            int wt = Math.max(ChuDe.rong(t, ChuDe.NHAN), Font.borderFont.getWidth(t)) + 24;
            ChuDe.bien(g, xChu - 4, y - 12, wt, 16);
            ChuDe.chuVienGiua(g, t, xChu - 4 + wt / 2, y - 12, 16, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 2);
        }
        // máy chữ: 2 ký tự / khung hình
        int tick = Canvas.gameTick - tuTick;
        if (tick < 0) {
            tick += 10001;
        }
        int conLai = hienHet ? 1 << 20 : tick * 2;
        boolean xong = true;
        int yy = y + 7;
        int t0 = Math.min(trang, soTrang() - 1) * n;
        for (int i = t0; i < dong.length && i < t0 + n; i++) {
            String s = dong[i];
            if (conLai < s.length()) {
                s = s.substring(0, conLai < 0 ? 0 : conLai);
                xong = false;
            }
            conLai -= dong[i].length();
            Font.arialFont.drawString(g, s, xChu, yy, Font.LEFT);
            yy += lh;
        }
        if (xong) {
            GiaoDien.muiXuong(g, x + w - 12, y + h - 9);
            if (giu) {
                // gợi ý rõ: chạm / bấm bất kỳ đâu (phím: chọn / 5) để nghe tiếp
                Font.arialFont.drawString(g, Canvas.instance != null && Canvas.instance.hasPointerEvents() ? "Chạm để tiếp" : "Bấm để tiếp", x + w - 18, y + h - 15, Font.RIGHT);
            }
        }
    }
}
