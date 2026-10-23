package htbb.pc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import javax.microedition.lcdui.NenPc;

/**
 * Lớp phủ HD của bản PC, vẽ sau game ở toạ độ THẬT (khử răng cưa): nút cài đặt, bảng Cài đặt kiểu Pokémon (thẻ
 * Hiển thị / Âm thanh / Đổi phím / Phím tắt (mọi phím, F1) / Giới thiệu; biểu tượng vẽ bằng nét vì phông đi kèm
 * không có ký hiệu ✕ ⚙ ▶ ♪...), hộp nhập
 * chữ (TextBox), thông báo nhanh, FPS. Giao diện kiểu "immediate mode": mỗi khung vẽ lại + ghi vùng bấm;
 * chuột (luồng AWT) đọc bản chụp vùng bấm, hành động chạy trên luồng sự kiện NenPc.
 */
public final class LopPhu {
    private LopPhu() {
    }

    // 0.9.3: phép phóng cho giao diện mới (Pokémon to ở màn Nhân vật, icon đồ trong ô lớn) - game.pokemon.PhongVe
    static {
        game.pokemon.PhongVe.bo = new game.pokemon.PhongVe() {
            public void bat(javax.microedition.lcdui.Graphics g, float z) {
                int ax = g.getClipX() + g.getTranslateX(), ay = g.getClipY() + g.getTranslateY();
                int aw = g.getClipWidth(), ah = g.getClipHeight();
                g.vaoTheGioi(z, (int) Math.ceil(game.render.Canvas.w / z) + 2, (int) Math.ceil(game.render.Canvas.h / z) + 2);
                g.setClip((int) (ax / z), (int) (ay / z), (int) Math.ceil(aw / z) + 1, (int) Math.ceil(ah / z) + 1);
            }

            public void tat(javax.microedition.lcdui.Graphics g) {
                g.boTheGioi();
            }
        };
    }

    /** Nạp lớp sớm (đặt PhongVe + chữ giao diện trước khung đầu). */
    public static void nap() {
        game.pokemon.ChuUI.bo = ChuHD.bat ? ChuGiaoDien.I : null;
    }

    /** Vùng bấm của khung vừa vẽ. */
    static final class Vung {
        final float x, y, w, h;
        final Runnable bam;
        final Keo keo;

        Vung(float x, float y, float w, float h, Runnable bam, Keo keo) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.bam = bam;
            this.keo = keo;
        }

        boolean trong(int px, int py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    interface Keo {
        void keo(float tiLe);
    }

    private static ArrayList<Vung> dangVe = new ArrayList<Vung>();
    private static volatile ArrayList<Vung> vung = new ArrayList<Vung>();
    private static volatile Vung dangKeo;

    static volatile boolean caiDatMo;
    private static int tab;
    /** đang chờ phím mới cho hành động này (-1 = không) */
    private static int doiPhim = -1;
    private static volatile String thongBao;
    private static volatile long hetThongBao;
    private static long fpsMoc;
    private static long fpsKhung;
    private static int fps;
    private static int fpsHt;
    private static long htMoc;
    private static long htKhung;
    private static final long BAT_DAU = System.currentTimeMillis();

    /** F1 (BanPhim gọi, luồng sự kiện): mở Cài đặt ở trang "Phím tắt". Nút / ô chiêu không ghi phím nữa. */
    static void moGoiY() {
        caiDatMo = true;
        tab = TAB_PHIM_TAT;
        doiPhim = -1;
    }

    public static void bao(String s) {
        thongBao = s;
        hetThongBao = System.currentTimeMillis() + 3500;
    }

    public static void moCaiDat() {
        caiDatMo = !caiDatMo;
        doiPhim = -1;
        if (!caiDatMo) CaiDat.luu();
    }

    private static void them(float x, float y, float w, float h, Runnable r) {
        dangVe.add(new Vung(x, y, w, h, r, null));
    }

    private static boolean tro(float x, float y, float w, float h) {
        ManHinh m = ManHinh.me;
        if (m == null) return false;
        int px = m.chuotX, py = m.chuotY;
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    private static Font f(boolean dam, float co) {
        return PhongChu.co(dam, co);
    }

    // ================================================================ vẽ
    public static void ve(Graphics2D g, int W, int H) {
        game.pokemon.ChuUI.bo = ChuHD.bat ? ChuGiaoDien.I : null;
        // 0.9.5: Hiệu ứng cao = vẽ 60 khung/giây (nội suy vị trí giữa hai nhịp logic 25/giây); thấp = 25
        game.pokemon.NhipVe.fps = CaiDat.hieuUngCao ? 60 : 25;
        game.pokemon.NhipVe.doBat = CaiDat.doKhung && CaiDat.choDo();
        dangVe = new ArrayList<Vung>();
        Kinh.min(g);
        float u = Math.max(0.75f, Math.min(W / 1280f, H / 720f));
        try {
            if (!caiDatMo && !HopNhap.mo) {
                ThanhLenh.ve(g, W, H, u, dangVe);
                // màn chơi có HUD màn rộng: nút cài đặt nằm trong hàng nút phải trên (HudRong) -> không vẽ đè khung mục tiêu
                boolean hudCo = game.render.Canvas.gameScr != null && game.render.Canvas.currentScreen == game.render.Canvas.gameScr
                        && game.render.screen.HudRong.bat();
                // bảng giao diện mới (Nhân vật / Menu lưới) có nút đóng riêng góc phải trên: không vẽ nút cài đặt đè đầu bảng
                Object mh = game.render.Canvas.currentScreen;
                boolean bangMoi = mh != null && mh.getClass().getName().startsWith("game.pokemon.Man");
                if (!hudCo && !bangMoi) veNutCaiDat(g, W, H, u);
            }
            if (HopNhap.mo) HopNhap.ve(g, W, H, u, dangVe);
            if (caiDatMo) veCaiDat(g, W, H, u);
            veThongBao(g, W, H, u);
            if (CaiDat.hienLopDo()) veFps(g, W, H, u);
        } catch (Throwable e) {
            NhatKy.loi("lop phu", e);
        }
        vung = dangVe;
    }

    private static final java.lang.management.ThreadMXBean TMX = java.lang.management.ManagementFactory.getThreadMXBean();
    private static long gcMoc, capMoc;
    private static int gcGiay;
    private static long capGiay;
    private static String dongDo = "", dongDo2 = "";

    /** Byte đã cấp phát của cả tiến trình (tổng các luồng sống) - -1 nếu JVM không hỗ trợ. */
    static long daCapPhat() {
        try {
            if (TMX instanceof com.sun.management.ThreadMXBean) {
                com.sun.management.ThreadMXBean t = (com.sun.management.ThreadMXBean) TMX;
                long[] id = t.getAllThreadIds();
                long[] b = t.getThreadAllocatedBytes(id);
                long s = 0;
                for (long x : b) if (x > 0) s += x;
                return s;
            }
        } catch (Throwable e) {
            // JVM khác
        }
        return -1;
    }

    static long soGc() {
        long n = 0;
        for (java.lang.management.GarbageCollectorMXBean b : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()) {
            n += Math.max(0, b.getCollectionCount());
        }
        return n;
    }

    private static void veFps(Graphics2D g, int W, int H, float u) {
        long now = System.currentTimeMillis();
        if (now - fpsMoc >= 1000) {
            fps = (int) ((NenPc.soKhung - fpsKhung) * 1000 / Math.max(1, now - fpsMoc));
            long dt = Math.max(1, now - fpsMoc);
            fpsMoc = now;
            fpsKhung = NenPc.soKhung;
            fpsHt = (int) ((ManHinh.soTrinhBay - htKhung) * 1000 / Math.max(1, now - htMoc));
            htMoc = now;
            htKhung = ManHinh.soTrinhBay;
            if (CaiDat.doKhung) {
                long gc = soGc(), cap = daCapPhat();
                gcGiay = (int) (gc - gcMoc);
                capGiay = cap < 0 || capMoc <= 0 ? -1 : (cap - capMoc) * 1000 / dt;
                gcMoc = gc;
                capMoc = cap;
                long[] tb = game.pokemon.NhipVe.TB, mx = game.pokemon.NhipVe.MAX;
                dongDo = String.format("nhập %.2f · logic %.2f · thế giới %.2f · giao diện %.2f · trình bày %.2f ms (tb)",
                        tb[0] / 1000f, tb[1] / 1000f, tb[2] / 1000f, tb[3] / 1000f, ManHinh.tbTrinhBay / 1000f);
                dongDo2 = String.format("khung p50 %.1f · p99 %.1f ms · max vẽ %.1f ms · GC %d/s · cấp phát %s/s",
                        game.pokemon.NhipVe.p50x10 / 10f, game.pokemon.NhipVe.p99x10 / 10f,
                        Math.max(mx[2], 0) / 1000f + Math.max(mx[3], 0) / 1000f, gcGiay,
                        capGiay < 0 ? "?" : capGiay > 1048576 ? String.format("%.1f MB", capGiay / 1048576f) : (capGiay / 1024) + " KB");
            }
        }
        g.setFont(f(true, 12 * u));
        String s = "game " + fps + " · hiển thị " + fpsHt + " FPS  " + W + "×" + H + "  logic " + NenPc.rong + "×" + NenPc.cao + " ×" + NenPc.heSo;
        // 0.9.5: trên nút chat (góc trái dưới), không che điều khiển; chỉ hiện khi bật trong Cài đặt
        float y0 = H - 78 * u;
        if (CaiDat.doKhung) {
            Kinh.chu(g, dongDo2, 8 * u, y0, Kinh.VANG);
            Kinh.chu(g, dongDo, 8 * u, y0 - 16 * u, Kinh.VANG);
            Kinh.chu(g, s, 8 * u, y0 - 32 * u, Kinh.VANG);
        } else {
            Kinh.chu(g, s, 8 * u, y0, Kinh.VANG);
        }
    }

    private static void veThongBao(Graphics2D g, int W, int H, float u) {
        String s = thongBao;
        if (s == null) return;
        long con = hetThongBao - System.currentTimeMillis();
        if (con <= 0) {
            thongBao = null;
            return;
        }
        int a = (int) Math.min(255, con / 2);
        g.setFont(f(true, 15 * u));
        FontMetrics fm = g.getFontMetrics();
        float w = fm.stringWidth(s) + 36 * u;
        float h = 34 * u;
        float x = (W - w) / 2;
        float y = 64 * u;
        Kinh.bang(g, x, y, w, h, h, a);
        Kinh.chuGiua(g, s, W / 2f, y + h / 2, Kinh.mo(Kinh.CHU, a), false);
    }

    /** Nút bánh răng nhỏ (mở Cài đặt) - giữa mép trên, cạnh nút bàn phím ảo của game. */
    private static void veNutCaiDat(Graphics2D g, int W, int H, float u) {
        float r = 13 * u;
        float cx = W / 2f + NenPc.heSo * 22 + r;
        float cy = NenPc.lechY + 4 * u + r;
        boolean sang = tro(cx - r, cy - r, 2 * r, 2 * r);
        int a = sang ? 255 : 150;
        // 0.9.6: đĩa trắng bóng mềm, bánh răng xám đá
        g.setColor(new Color(0, 0, 0, a * 40 / 255));
        g.fill(new Ellipse2D.Float(cx - r, cy - r + 1.5f * u, 2 * r, 2 * r));
        g.setColor(new Color(255, 255, 255, a));
        g.fill(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
        // bánh răng
        g.setColor(Kinh.mo(Kinh.CHU_MO, a));
        float rr = r * 0.52f;
        g.setStroke(new BasicStroke(2.6f * u, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double t = Math.PI * 2 * i / 8;
            g.draw(new java.awt.geom.Line2D.Float(cx + (float) Math.cos(t) * rr * 0.9f, cy + (float) Math.sin(t) * rr * 0.9f,
                    cx + (float) Math.cos(t) * rr * 1.35f, cy + (float) Math.sin(t) * rr * 1.35f));
        }
        g.setStroke(new BasicStroke(2.2f * u));
        g.draw(new Ellipse2D.Float(cx - rr, cy - rr, 2 * rr, 2 * rr));
        if (sang) {
            g.setFont(f(true, 12 * u));
            FontMetrics fm = g.getFontMetrics();
            String s = "Cài đặt";
            float w = fm.stringWidth(s) + 16 * u;
            Kinh.bang(g, cx + r + 6 * u, cy - 12 * u, w, 24 * u, 12 * u, 255);
            Kinh.chuGiua(g, s, cx + r + 6 * u + w / 2, cy, Kinh.CHU, false);
        }
        them(cx - r, cy - r, 2 * r, 2 * r, new Runnable() {
            public void run() {
                moCaiDat();
            }
        });
    }

    // ================================================================ bảng cài đặt
    private static final String[] TAB = {"Hiển thị", "Âm thanh", "Đổi phím", "Phím tắt", "Tài khoản", "Giới thiệu"};
    private static final int[] TAB_BT = {Kinh.BT_MAN_HINH, Kinh.BT_LOA, Kinh.BT_BAN_PHIM, Kinh.BT_SET, Kinh.BT_TAI_KHOAN, Kinh.BT_THONG_TIN};
    private static final int TAB_TAI_KHOAN = 4;
    private static final int TAB_PHIM_TAT = 3;
    private static final int[][] DO_PHAN_GIAI = {{1280, 720}, {1600, 900}, {1920, 1080}, {2560, 1440}, {2560, 1080}, {1024, 768}, {1440, 1080}};
    private static final String[] TEN_DPG = {"1280×720", "1600×900", "1920×1080", "2560×1440", "2560×1080", "1024×768", "1440×1080"};
    // 0.9.6 (kiểu GO): thẻ trắng viền kẻ nhạt trên tấm sáng
    private static final Color NEN_THE = Color.WHITE;
    private static final Color VIEN_THE = new Color(game.pokemon.ChuDe.KE);
    private static final Color KE = new Color(game.pokemon.ChuDe.NEN_PHU_TOI);
    private static java.awt.image.BufferedImage bieuTuongGame;
    private static boolean daNapBieuTuong;

    private static void veCaiDat(Graphics2D g, int W, int H, float u) {
        // nền tối mờ + chặn bấm xuống game
        g.setColor(new Color(0x12, 0x08, 0x04, 122));
        g.fillRect(0, 0, W, H);
        them(0, 0, W, H, null);
        float pw = Math.min(W - 24 * u, 860 * u);
        float ph = Math.min(H - 24 * u, 580 * u);
        float px = Math.round((W - pw) / 2);
        float py = Math.round((H - ph) / 2);
        float r = 22 * u;
        Kinh.bang(g, px, py, pw, ph, r, 255);
        // ---- đầu tấm (0.9.6 kiểu GO): KHÔNG dải màu - tiêu đề tròn đậm canh giữa, phụ đề nhạt, nút tròn trắng X trái
        float hh = 70 * u;
        g.setFont(PhongChu.tieuDe().deriveFont(Font.BOLD, 27 * u));
        FontMetrics ftd = g.getFontMetrics();
        // 0.9.7: biển tiêu đề đỏ viền vàng, chữ trắng
        float wb = ftd.stringWidth("Cài đặt") + 70 * u;
        Kinh.ve9(g, game.pokemon.KhungNroBang.BIEN, px + (pw - wb) / 2, py + 12 * u, wb, 38 * u, Kinh.k());
        Kinh.chuGiua(g, "Cài đặt", px + pw / 2, py + 31 * u, Color.WHITE, true);
        g.setFont(f(false, 12.5f * u));
        FontMetrics fpd = g.getFontMetrics();
        String pd = "Huyền Thoại Bảo Bối · bản PC";
        Kinh.chu(g, pd, px + (pw - fpd.stringWidth(pd)) / 2, py + 64 * u, Kinh.CHU_MO);
        float br = 18 * u;
        float bcx = px + 24 * u + br, bcy = py + hh / 2 - 1 * u;
        boolean sx = tro(bcx - br, bcy - br, 2 * br, 2 * br);
        // 0.9.7: nút X đỏ pixel (mảnh DONG)
        int kd = Kinh.k();
        if (!Kinh.ve9(g, game.pokemon.KhungNroBang.DONG, bcx - 8 * kd, bcy - 8 * kd + (sx ? kd : 0), 16 * kd, 16 * kd, kd)) {
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Float(bcx - br, bcy - br, 2 * br, 2 * br));
            Kinh.bieuTuong(g, Kinh.BT_DONG, bcx, bcy, 2 * br * 0.55f, Kinh.CHU);
        }
        them(bcx - br, bcy - br, 2 * br, 2 * br, new Runnable() {
            public void run() {
                moCaiDat();
            }
        });
        // ---- chip phân đoạn: rãnh xám xanh viên, thẻ đang chọn = viên trắng nổi chữ teal
        float trX = px + 22 * u, trY = py + hh + 10 * u, trW = pw - 44 * u, trH = 46 * u;
        // 0.9.7: hàng TAB GỖ (tab chọn giấy da nối liền thân tấm) trên rãnh gỗ
        int kt = Kinh.k();
        g.setColor(new Color(game.pokemon.ChuDe.VIEN));
        g.fillRect(Math.round(trX), Math.round(trY + trH - 2 * kt), Math.round(trW), kt);
        g.setColor(new Color(0x8E4A1E));
        g.fillRect(Math.round(trX), Math.round(trY + trH - kt), Math.round(trW), kt);
        float o = 4 * u;
        float tw = (trW - 2 * o) / TAB.length;
        Font ft = f(true, 13.5f * u);
        for (int i = 0; i < TAB.length; i++) {
            float x = trX + o + i * tw, y = trY + o, w = tw, h = trH - 2 * o;
            boolean dang = tab == i;
            boolean sang = !dang && tro(x, y, w, h);
            if (dang) {
                Kinh.ve9(g, game.pokemon.KhungNroBang.TAB_CHON, x, trY, w - kt, trH + kt, kt);
            } else {
                Kinh.ve9(g, game.pokemon.KhungNroBang.TAB, x, trY + 2 * kt + (sang ? kt : 0), w - kt, trH - 2 * kt, kt);
            }
            Color mau = dang ? Kinh.CHU : new Color(0xF6E6C0);
            g.setFont(ft);
            FontMetrics fm = g.getFontMetrics();
            float s = 18 * u;
            float cw = s + 8 * u + fm.stringWidth(TAB[i]);
            float x0 = x + (w - cw) / 2;
            Kinh.bieuTuong(g, TAB_BT[i], x0 + s / 2, y + h / 2, s, mau);
            float by = y + (h - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent();
            g.setColor(mau);
            g.drawString(TAB[i], x0 + s + 8 * u, by);
            final int ii = i;
            them(x, y, w, h, new Runnable() {
                public void run() {
                    tab = ii;
                    doiPhim = -1;
                }
            });
        }
        // ---- nội dung (cắt trong vùng nội dung)
        float cx = px + 24 * u, cy = trY + trH + 18 * u, cw = pw - 48 * u, ch = py + ph - 20 * u - cy;
        java.awt.Shape cu2 = g.getClip();
        g.clip(new Rectangle2D.Float(cx - 4 * u, cy - 4 * u, cw + 8 * u, ch + 8 * u));
        switch (tab) {
            case 0: veHienThi(g, cx, cy, cw, ch, u); break;
            case 1: veAmThanh(g, cx, cy, cw, ch, u); break;
            case 2: vePhim(g, cx, cy, cw, ch, u); break;
            case TAB_PHIM_TAT: vePhimTat(g, cx, cy, cw, ch, u); break;
            case TAB_TAI_KHOAN: veTaiKhoan(g, cx, cy, cw, ch, u); break;
            default: veGioiThieu(g, cx, cy, cw, ch, u); break;
        }
        g.setClip(cu2);
    }

    // ---------------------------------------------------------------- khối dựng giao diện
    /** Tiêu đề nhóm (vàng, đậm) - trả y đỉnh thẻ. */
    private static float tieuDeNhom(Graphics2D g, String s, float x, float y, float u) {
        g.setFont(f(true, 12.5f * u));
        Kinh.chu(g, s.toUpperCase(), x + 4 * u, y + 13 * u, Kinh.CHINH_TOI);
        return y + 22 * u;
    }

    /** Thẻ nhóm: nền trắng mờ bo góc + viền mảnh. */
    private static void the(Graphics2D g, float x, float y, float w, float h, float u) {
        // 0.9.7: thẻ nhóm = giấy da lõm (mảnh THE)
        if (Kinh.ve9(g, game.pokemon.KhungNroBang.THE, x, y, w, h, Kinh.k())) return;
        RoundRectangle2D.Float rr = new RoundRectangle2D.Float(x, y, w, h, 16 * u, 16 * u);
        g.setColor(NEN_THE);
        g.fill(rr);
    }

    private static void ke(Graphics2D g, float x, float y, float w, float u) {
        g.setColor(KE);
        g.fill(new Rectangle2D.Float(x + 16 * u, y, w - 32 * u, Math.max(1, u * 0.8f)));
    }

    /** Nhãn hàng (đậm) + mô tả (nhạt, dưới) căn giữa dọc trong hàng [y, y + h). */
    private static void nhanHang(Graphics2D g, String nhan, String moTa, float x, float y, float h, float u) {
        g.setFont(f(true, 14 * u));
        if (moTa == null) {
            FontMetrics fm = g.getFontMetrics();
            Kinh.chu(g, nhan, x + 16 * u, y + (h - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent(), Kinh.CHU);
            return;
        }
        Kinh.chu(g, nhan, x + 16 * u, y + h / 2 - 3 * u, Kinh.CHU);
        g.setFont(f(false, 11.5f * u));
        Kinh.chu(g, moTa, x + 16 * u, y + h / 2 + 14 * u, Kinh.CHU_MO);
    }

    /** Hàng công tắc: nhãn trái, công tắc phải. Cả hàng bấm được. */
    private static void hangCongTac(Graphics2D g, String nhan, String moTa, float x, float y, float w, float h, float u,
                                    boolean bat, Runnable doi) {
        boolean sang = tro(x, y, w, h);
        if (sang) {
            g.setColor(new Color(0, 0, 0, 10));
            g.fill(new RoundRectangle2D.Float(x + 4 * u, y + 2 * u, w - 8 * u, h - 4 * u, 12 * u, 12 * u));
        }
        nhanHang(g, nhan, moTa, x, y, h, u);
        float th = 24 * u;
        Kinh.congTac(g, x + w - 16 * u - th * 1.9f, y + (h - th) / 2, th, bat, sang);
        them(x, y, w, h, doi);
    }

    /** Nhóm nút chọn một (phân đoạn) căn phải trong hàng; trả bề rộng đã dùng. */
    private static void hangChon(Graphics2D g, String nhan, String moTa, float x, float y, float w, float h, float u,
                                 String[] ten, int dang, final Runnable[] bam) {
        nhanHang(g, nhan, moTa, x, y, h, u);
        Font fb = f(true, 12.5f * u);
        g.setFont(fb);
        FontMetrics fm = g.getFontMetrics();
        float bh = 30 * u;
        float tong = 0;
        float[] rong = new float[ten.length];
        for (int i = 0; i < ten.length; i++) {
            rong[i] = Math.max(84 * u, fm.stringWidth(ten[i]) + 28 * u);
            tong += rong[i];
        }
        float bx = x + w - 16 * u - tong - 4 * u * (ten.length - 1);
        float by = y + (h - bh) / 2;
        for (int i = 0; i < ten.length; i++) {
            Kinh.nut(g, bx, by, rong[i], bh, ten[i], fb, false, tro(bx, by, rong[i], bh), dang == i);
            them(bx, by, rong[i], bh, bam[i]);
            bx += rong[i] + 4 * u;
        }
    }

    private static void hangTruot(Graphics2D g, String nhan, float x, float y, float w, float h, float u, int gt, Keo k) {
        nhanHang(g, nhan, null, x, y, h, u);
        float tw = Math.min(380 * u, w * 0.5f);
        float tx = x + w - 16 * u - 56 * u - tw;
        float th = 8 * u;
        float ty = y + (h - th) / 2;
        boolean sang = tro(tx - 10 * u, y, tw + 20 * u, h);
        Kinh.thanhTruot(g, tx, ty, tw, th, gt, sang);
        g.setFont(f(true, 13.5f * u));
        FontMetrics fm = g.getFontMetrics();
        String s = gt + "%";
        Kinh.chu(g, s, x + w - 16 * u - fm.stringWidth(s), y + (h - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent(), Kinh.CHU);
        dangVe.add(new Vung(tx - 10 * u, y, tw + 20 * u, h, null, new KeoTheo(tx, tw, k)));
    }

    // ---------------------------------------------------------------- thẻ Hiển thị
    private static void veHienThi(Graphics2D g, float x, float y, float w, float hMax, float u) {
        ManHinh m = ManHinh.me;
        final boolean tm = m != null && m.laToanMan();
        // nhóm Cửa sổ
        y = tieuDeNhom(g, "Cửa sổ", x, y, u);
        float hKich = 92 * u, hHang = 52 * u;
        the(g, x, y, w, hKich + hHang, u);
        nhanHang(g, "Kích thước cửa sổ", tm ? "Đang toàn màn hình - chọn một cỡ để về chế độ cửa sổ" : "Hình luôn sắc nét, tự canh giữa",
                x, y + 4 * u, 46 * u, u);
        Font fb = f(true, 12.5f * u);
        g.setFont(fb);
        FontMetrics fm = g.getFontMetrics();
        float bx = x + 16 * u, by = y + 52 * u, bh = 30 * u;
        for (int i = 0; i < DO_PHAN_GIAI.length; i++) {
            float bw = fm.stringWidth(TEN_DPG[i]) + 26 * u;
            if (bx + bw > x + w - 16 * u) {
                bx = x + 16 * u;
                by += bh + 6 * u;
            }
            final int dw = DO_PHAN_GIAI[i][0], dh = DO_PHAN_GIAI[i][1];
            boolean dang = m != null && !tm && m.getWidth() == dw && m.getHeight() == dh;
            Kinh.nut(g, bx, by, bw, bh, TEN_DPG[i], fb, false, tro(bx, by, bw, bh), dang);
            them(bx, by, bw, bh, new Runnable() {
                public void run() {
                    ManHinh mm = ManHinh.me;
                    if (mm != null) mm.datCo(dw, dh);
                }
            });
            bx += bw + 6 * u;
        }
        float yy = y + hKich;
        ke(g, x, yy, w, u);
        hangCongTac(g, "Toàn màn hình", "Phủ kín màn hình, không viền", x, yy, w, hHang, u, tm, new Runnable() {
            public void run() {
                ManHinh mm = ManHinh.me;
                if (mm != null) mm.datToanMan(!mm.laToanMan());
            }
        });
        y = yy + hHang + 14 * u;
        // nhóm Hình ảnh
        y = tieuDeNhom(g, "Hình ảnh", x, y, u);
        final boolean choDo = CaiDat.choDo();   // bản phát hành: không có công tắc FPS / đo khung
        the(g, x, y, w, (choDo ? 5 : 3) * hHang, u);
        hangCongTac(g, "Chữ HD", "Phông Noto Sans mịn (tắt = phông điểm ảnh gốc)", x, y, w, hHang, u, ChuHD.bat, new Runnable() {
            public void run() {
                CaiDat.chuHD = !CaiDat.chuHD;
                ChuHD.bat = CaiDat.chuHD;
                NenPc.baoDoiCo();
            }
        });
        y += hHang;
        ke(g, x, y, w, u);
        hangCongTac(g, "Khung HD", "Bảng, nút, thanh máu vẽ mịn ở độ phân giải thật", x, y, w, hHang, u, GiaoDienHD.bat, new Runnable() {
            public void run() {
                CaiDat.khungHD = !CaiDat.khungHD;
                GiaoDienHD.bat = CaiDat.khungHD;
            }
        });
        y += hHang;
        ke(g, x, y, w, u);
        hangChon(g, "Hiệu ứng", "Cao: hiệu ứng HD, 60 khung/giây · Thấp: cho máy yếu", x, y, w, hHang, u,
                new String[]{"Cao", "Thấp"}, CaiDat.hieuUngCao ? 0 : 1, new Runnable[]{
                    new Runnable() {
                        public void run() {
                            CaiDat.hieuUngCao = true;
                        }
                    }, new Runnable() {
                        public void run() {
                            CaiDat.hieuUngCao = false;
                        }
                    }});
        if (!choDo) return;
        y += hHang;
        ke(g, x, y, w, u);
        hangCongTac(g, "Hiện FPS", "Số khung hình/giây ở góc dưới", x, y, w, hHang, u, CaiDat.hienFps, new Runnable() {
            public void run() {
                CaiDat.hienFps = !CaiDat.hienFps;
            }
        });
        y += hHang;
        ke(g, x, y, w, u);
        hangCongTac(g, "Đo khung (nhà phát triển)", "ms nhập / logic / vẽ thế giới / giao diện / trình bày, p99, GC, cấp phát", x, y, w, hHang, u,
                CaiDat.doKhung, new Runnable() {
                    public void run() {
                        CaiDat.doKhung = !CaiDat.doKhung;
                    }
                });
    }

    /** 0.9.8 Ngắm thủ công (PvE) - trong trận giữa người chơi luôn ngắm tay (game.pokemon.NgamTay). Thẻ Đổi phím. */
    private static void veChienDau(Graphics2D g, float x, float y, float w, float hHang, float u) {
        the(g, x, y, w, hHang, u);
        hangCongTac(g, "Ngắm thủ công", "Giữ phím / nút chiêu, rê chuột để ngắm cả khi đánh Pokémon hoang dã (PvP luôn ngắm tay)",
                x, y, w, hHang, u, game.pokemon.NgamTay.thuCong(), new Runnable() {
                    public void run() {
                        game.pokemon.NgamTay.datThuCong(!game.pokemon.NgamTay.thuCong());
                    }
                });
    }

    /** Kéo thanh trượt: tỉ lệ theo x thật. */
    static final class KeoTheo implements Keo {
        final float x0, w;
        final Keo k;

        KeoTheo(float x0, float w, Keo k) {
            this.x0 = x0;
            this.w = w;
            this.k = k;
        }

        public void keo(float xThat) {
            k.keo(Math.max(0, Math.min(1, (xThat - x0) / w)));
        }
    }

    // ---------------------------------------------------------------- thẻ Âm thanh
    private static void veAmThanh(Graphics2D g, float x, float y, float w, float hMax, float u) {
        float hHang = 54 * u;
        y = tieuDeNhom(g, "Âm lượng", x, y, u);
        the(g, x, y, w, 3 * hHang, u);
        hangTruot(g, "Nhạc nền", x, y, w, hHang, u, CaiDat.amNhac, new Keo() {
            public void keo(float t) {
                CaiDat.amNhac = Math.round(t * 100);
                CaiDat.apAmThanh();
            }
        });
        y += hHang;
        ke(g, x, y, w, u);
        hangTruot(g, "Hiệu ứng", x, y, w, hHang, u, CaiDat.amHieuUng, new Keo() {
            public void keo(float t) {
                CaiDat.amHieuUng = Math.round(t * 100);
                CaiDat.apAmThanh();
            }
        });
        y += hHang;
        ke(g, x, y, w, u);
        hangCongTac(g, "Tắt tiếng", "Tắt cả nhạc nền và tiếng hiệu ứng", x, y, w, hHang, u, CaiDat.tatTieng, new Runnable() {
            public void run() {
                CaiDat.tatTieng = !CaiDat.tatTieng;
                CaiDat.apAmThanh();
            }
        });
        y += hHang + 14 * u;
        y = tieuDeNhom(g, "Thiết bị", x, y, u);
        the(g, x, y, w, hHang, u);
        int loi = javax.microedition.media.Manager.soLoiThietBi;
        boolean tot = loi <= 0;
        nhanHang(g, tot ? "Sẵn sàng" : "Không mở được thiết bị âm thanh",
                tot ? "WAV + MIDI · bật/tắt nhạc riêng trong Menu nhanh của game" : "Đã thử " + loi + " lần - game vẫn chạy, không có tiếng",
                x + 20 * u, y, hHang, u);
        float d = 12 * u;
        g.setColor(tot ? new Color(0x48D058) : new Color(0xF04830));
        g.fill(new Ellipse2D.Float(x + 18 * u, y + (hHang - d) / 2, d, d));
    }

    // ---------------------------------------------------------------- thẻ Đổi phím
    private static void vePhim(Graphics2D g, float x, float y, float w, float hMax, float u) {
        float y0 = y;
        tieuDeNhom(g, "Phím điều khiển", x, y, u);
        // nút khôi phục: góc phải dòng tiêu đề
        Font fb = f(true, 12 * u);
        g.setFont(fb);
        String md = "Khôi phục mặc định";
        float mw = g.getFontMetrics().stringWidth(md) + 26 * u, mh = 24 * u;
        float mx = x + w - mw, my = y - 4 * u;
        Kinh.nut(g, mx, my, mw, mh, md, fb, false, tro(mx, my, mw, mh), false);
        them(mx, my, mw, mh, new Runnable() {
            public void run() {
                System.arraycopy(BanPhim.MAC_DINH, 0, BanPhim.PHIM, 0, BanPhim.PHIM.length);
                doiPhim = -1;
                CaiDat.luu();
            }
        });
        g.setFont(f(false, 11.5f * u));
        String hd = "Bấm vào một phím rồi nhấn phím mới";
        FontMetrics fmh = g.getFontMetrics();
        Kinh.chu(g, hd, mx - 14 * u - fmh.stringWidth(hd), y + 13 * u, Kinh.CHU_MO);
        y += 22 * u;
        int n = BanPhim.MA.length;
        int cot = 2;
        int moiCot = (n + cot - 1) / cot;
        float pad = 8 * u;
        float hCd = 52 * u;   // 0.9.8: hàng "Ngắm thủ công" dưới bảng phím (cùng nhóm điều khiển chiêu)
        float hang = Math.min(34 * u, (y0 + hMax - hCd - 10 * u - y - 2 * pad) / moiCot);
        veChienDau(g, x, y + moiCot * hang + 2 * pad + 10 * u, w, hCd, u);
        the(g, x, y, w, moiCot * hang + 2 * pad, u);
        float gap = 20 * u;
        float cw = (w - gap) / cot;
        g.setColor(KE);
        g.fill(new Rectangle2D.Float(x + cw + gap / 2, y + pad + 4 * u, Math.max(1, u * 0.8f), moiCot * hang - 8 * u));
        Font fc = f(false, 13 * u);
        Font fp = f(true, 12 * u);
        for (int i = 0; i < n; i++) {
            float cx = x + (i / moiCot) * (cw + gap);
            float cy = y + pad + (i % moiCot) * hang;
            g.setFont(fc);
            FontMetrics fm = g.getFontMetrics();
            Kinh.chu(g, BanPhim.TEN[i], cx + 16 * u, cy + (hang - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent(), Kinh.CHU);
            float bw = 118 * u, bh = Math.min(26 * u, hang - 6 * u);
            float bx = cx + cw - 12 * u - bw, by = cy + (hang - bh) / 2;
            final int ii = i;
            boolean cho = doiPhim == i;
            String nhan = cho ? "Nhấn phím…" : BanPhim.tenPhim(BanPhim.PHIM[i]);
            Kinh.nut(g, bx, by, bw, bh, nhan, fp, cho, tro(bx, by, bw, bh), false);
            them(bx, by, bw, bh, new Runnable() {
                public void run() {
                    doiPhim = doiPhim == ii ? -1 : ii;
                }
            });
        }
    }

    // ---------------------------------------------------------------- thẻ Phím tắt
    /** Phím đang gán cho hành động (đổi được trong thẻ Đổi phím). */
    private static String ten(int hd) {
        return BanPhim.tenPhim(BanPhim.PHIM[hd]);
    }

    /** Hai cột Phím tắt. Mỗi mục {phím, việc, mô tả phụ}; {null, tiêu đề} = tiêu đề nhóm. Phím: "@0" chuột trái,
     *  "@1" chuột phải, "@2" con lăn; còn lại các phím cách nhau "|" (hoặc), "+" (tổ hợp). */
    private static ArrayList<String[]> danhSachPhimTat(boolean trai) {
        ArrayList<String[]> ds = new ArrayList<String[]>();
        boolean wasd = BanPhim.PHIM[BanPhim.LEN] == KeyEvent.VK_W && BanPhim.PHIM[BanPhim.TRAI] == KeyEvent.VK_A
                && BanPhim.PHIM[BanPhim.XUONG] == KeyEvent.VK_S && BanPhim.PHIM[BanPhim.PHAI] == KeyEvent.VK_D;
        String di = wasd ? "W A S D" : ten(BanPhim.LEN) + " " + ten(BanPhim.TRAI) + " " + ten(BanPhim.XUONG) + " " + ten(BanPhim.PHAI);
        boolean so = true;
        for (int i = 0; i < 5; i++) so &= BanPhim.PHIM[BanPhim.CHIEU1 + i] == KeyEvent.VK_1 + i;
        String chieu = so ? "1 – 5" : ten(BanPhim.CHIEU1) + " … " + ten(BanPhim.CHIEU1 + 4);
        int kc = BanPhim.PHIM[BanPhim.CHAT];
        String chat = kc == 0 || kc == KeyEvent.VK_ENTER || kc == KeyEvent.VK_T || kc == KeyEvent.VK_SLASH ? "T" : ten(BanPhim.CHAT);
        if (trai) {
            ds.add(new String[]{null, "Di chuyển & tương tác"});
            ds.add(new String[]{di + "|Mũi tên", "Đi", null});
            ds.add(new String[]{"Enter", "Hành động theo ngữ cảnh", "Nói chuyện, đánh, nhặt đồ, tiếp lời thoại"});
            ds.add(new String[]{"@0", "Làm mọi thứ", "Đi, chọn, nói chuyện, đánh, bấm nút"});
            ds.add(new String[]{"@1", "Menu ngữ cảnh (tuỳ chọn)", null});
            ds.add(new String[]{"@2", "Cuộn danh sách", null});
            ds.add(new String[]{null, "Chiến đấu"});
            ds.add(new String[]{ten(BanPhim.DANH), "Đánh thường", null});
            ds.add(new String[]{chieu, "Dùng chiêu 1 – 5", "PvP: giữ phím, rê chuột ngắm, thả để bắn"});
            ds.add(new String[]{ten(BanPhim.BINH_HP) + "|" + ten(BanPhim.BINH_MP), "Bình HP / Bình MP", null});
            ds.add(new String[]{ten(BanPhim.MUC_TIEU), "Đổi mục tiêu", null});
            ds.add(new String[]{"Shift+" + ten(BanPhim.MUC_TIEU), "Mục tiêu trước", null});
            ds.add(new String[]{ten(BanPhim.DOI_BO), "Đổi bộ chiêu", null});
            ds.add(new String[]{ten(BanPhim.NEM_BONG), "Ném Poké Ball (Pokémon hoang dã đã yếu)", null});
        } else {
            ds.add(new String[]{null, "Giao diện"});
            ds.add(new String[]{chat + "|/", "Mở chat", "Enter gửi tin khi đang gõ"});
            ds.add(new String[]{ten(BanPhim.MENU), "Menu · quay lại · đóng", null});
            ds.add(new String[]{ten(BanPhim.MENU_NHANH), "Menu nhanh", null});
            ds.add(new String[]{ten(BanPhim.TO_DOI), "Tổ đội", null});
            ds.add(new String[]{"F3|F2", "Phím mềm trái / phải", null});
            ds.add(new String[]{"Ctrl+V", "Dán chữ vào ô nhập", null});
            ds.add(new String[]{null, "Hệ thống"});
            ds.add(new String[]{ten(BanPhim.CAI_DAT), "Cài đặt", null});
            ds.add(new String[]{"F1", "Trang Phím tắt này", null});
            ds.add(new String[]{ten(BanPhim.TOAN_MAN), "Toàn màn hình", null});
            ds.add(new String[]{"Alt+Enter", "Toàn màn hình", null});
            ds.add(new String[]{ten(BanPhim.CHUP), "Chụp màn hình", null});
        }
        return ds;
    }

    /** Trang "Phím tắt": MỘT nơi liệt kê mọi phím (phím đổi được theo cài đặt hiện tại + phím cố định). */
    private static void vePhimTat(Graphics2D g, float x, float y, float w, float h, float u) {
        float gap = 16 * u;
        float cw = (w - gap) / 2;
        for (int c = 0; c < 2; c++) {
            ArrayList<String[]> ds = danhSachPhimTat(c == 0);
            float cx = x + c * (cw + gap);
            // đo chiều cao cần, co hàng nếu thiếu chỗ
            float tieu = 0, hang = 0, phu = 0;
            for (int i = 0; i < ds.size(); i++) {
                String[] d = ds.get(i);
                if (d[0] == null) tieu++;
                else if (d[2] == null) hang++;
                else phu++;
            }
            float can = 2 * 10 * u + tieu * 30 * u + hang * 28 * u + phu * 42 * u;
            float k = Math.min(1f, h / can);
            the(g, cx, y, cw, Math.min(h, can * k), u);
            float yy = y + 10 * u * k;
            for (int i = 0; i < ds.size(); i++) {
                String[] d = ds.get(i);
                if (d[0] == null) {
                    g.setFont(f(true, 12.5f * u));
                    Kinh.chu(g, d[1].toUpperCase(), cx + 16 * u, yy + 20 * u * k, Kinh.CHINH_TOI);
                    yy += 30 * u * k;
                    continue;
                }
                float hh = (d[2] == null ? 28 : 42) * u * k;
                float kh = Math.min(22 * u, 22 * u * k);
                float ky = yy + (28 * u * k - kh) / 2;
                float het = vePhimGop(g, d[0], cx + 16 * u, ky, kh, u);
                float tx = Math.max(cx + 16 * u + 136 * u, het + 10 * u);
                g.setFont(f(false, 13 * u));
                FontMetrics fm = g.getFontMetrics();
                float by = ky + (kh - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent();
                Kinh.chu(g, d[1], tx, by, Kinh.CHU);
                if (d[2] != null) {
                    g.setFont(f(false, 11 * u));
                    Kinh.chu(g, catChu(g, d[2], cx + cw - 12 * u - tx), tx, by + 16 * u * k, Kinh.CHU_MO);
                }
                yy += hh;
            }
        }
    }

    private static String catChu(Graphics2D g, String s, float w) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= w) return s;
        while (s.length() > 1 && fm.stringWidth(s + "…") > w) s = s.substring(0, s.length() - 1);
        return s.trim() + "…";
    }

    /** Vẽ nhóm phím: "a|b" = a hoặc b (chữ "/" giữa), "Shift+Tab" = hai phím nối "+", "@n" = chuột. */
    private static float vePhimGop(Graphics2D g, String s, float x, float y, float h, float u) {
        Font fp = f(true, h * 0.52f);
        if (s.startsWith("@")) {
            int nut = s.charAt(1) - '0';
            Kinh.chuot(g, x + h * 0.4f, y + h / 2, h * 1.1f, nut);
            g.setFont(f(true, 12 * u));
            FontMetrics fm = g.getFontMetrics();
            String t = nut == 0 ? "Chuột trái" : nut == 1 ? "Chuột phải" : "Lăn chuột";
            Kinh.chu(g, t, x + h + 6 * u, y + (h - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent(), Kinh.CHU_MO);
            return x + h + 6 * u + fm.stringWidth(t);
        }
        String[] hoac = s.split("\\|");
        float xx = x;
        for (int i = 0; i < hoac.length; i++) {
            if (i > 0) {
                g.setFont(f(false, 12 * u));
                FontMetrics fm = g.getFontMetrics();
                Kinh.chu(g, "/", xx + 3 * u, y + (h - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent(), Kinh.CHU_MO);
                xx += fm.stringWidth("/") + 6 * u;
            }
            String[] to = hoac[i].length() > 1 ? hoac[i].split("\\+") : new String[]{hoac[i]};
            for (int j = 0; j < to.length; j++) {
                if (j > 0) {
                    g.setFont(f(true, 11 * u));
                    FontMetrics fm = g.getFontMetrics();
                    Kinh.chu(g, "+", xx + 2 * u, y + (h - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent(), Kinh.CHU_MO);
                    xx += fm.stringWidth("+") + 4 * u;
                }
                xx += Kinh.phim(g, xx, y, h, to[j], fp) + 2 * u;
            }
        }
        return xx;
    }

    // ---------------------------------------------------------------- thẻ Giới thiệu
    private static java.awt.image.BufferedImage bieuTuong() {
        if (!daNapBieuTuong) {
            daNapBieuTuong = true;
            try {
                java.io.InputStream in = LopPhu.class.getResourceAsStream("/htbb-pc/icon_128.png");
                if (in != null) {
                    try {
                        bieuTuongGame = javax.imageio.ImageIO.read(in);
                    } finally {
                        in.close();
                    }
                }
            } catch (Throwable e) {
                bieuTuongGame = null;
            }
        }
        return bieuTuongGame;
    }

    private static void veGioiThieu(Graphics2D g, float x, float y, float w, float hMax, float u) {
        // thẻ đầu: biểu tượng game + tên + phiên bản
        float hDau = 104 * u;
        the(g, x, y, w, hDau, u);
        float s = 76 * u;
        java.awt.image.BufferedImage bt = bieuTuong();
        if (bt != null) {
            g.drawImage(bt, Math.round(x + 18 * u), Math.round(y + (hDau - s) / 2), Math.round(s), Math.round(s), null);
        } else {
            Kinh.pokeBall(g, x + 18 * u + s / 2, y + hDau / 2, s * 0.45f);
        }
        float tx = x + 18 * u + s + 20 * u;
        g.setFont(PhongChu.tieuDe().deriveFont(Font.BOLD, 26 * u));
        Kinh.chu(g, "Huyền Thoại Bảo Bối", tx, y + 42 * u, Kinh.CHU);
        String pb = javax.microedition.midlet.MIDlet.thuocTinh("HTBB-Phien-Ban");
        String ban = javax.microedition.midlet.MIDlet.thuocTinh("HTBB-Ban");
        boolean thu = "1".equals(javax.microedition.midlet.MIDlet.thuocTinh("HTBB-Ban-Thu"));
        g.setFont(f(true, 13 * u));
        Kinh.chu(g, (pb == null ? "Bản phát triển" : "Phiên bản " + pb) + (ban == null ? "" : " (bản " + ban + ")") + (thu ? " · bản thử" : ""),
                tx, y + 66 * u, Kinh.CHINH_TOI);
        g.setFont(f(false, 12 * u));
        Kinh.chu(g, "Game fan Pokémon phi thương mại · bản PC", tx, y + 86 * u, Kinh.CHU_MO);
        y += hDau + 14 * u;
        // thông tin
        String mc = javax.microedition.midlet.MIDlet.thuocTinh("HTBB-May-Chu");
        String[][] tt = {
            {"Máy chủ", mc == null ? "Theo danh sách máy chủ" : mc},
            {"Hệ thống", "Java " + System.getProperty("java.version") + " · " + System.getProperty("os.name")},
            {"Phông chữ", "Noto Sans, Baloo 2 (SIL OFL 1.1)"},
            {"Hình Pokémon", "PMDCollab (CC BY-NC 4.0)"},
            {"Bản quyền", "Pokémon © Nintendo / Game Freak / Creatures"},
        };
        float hh = 30 * u;
        y = tieuDeNhom(g, "Thông tin", x, y, u);
        the(g, x, y, w, tt.length * hh + 12 * u, u);
        for (int i = 0; i < tt.length; i++) {
            float yy = y + 6 * u + i * hh;
            g.setFont(f(false, 12.5f * u));
            FontMetrics fm = g.getFontMetrics();
            float by = yy + (hh - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent();
            Kinh.chu(g, tt[i][0], x + 16 * u, by, Kinh.CHU_MO);
            g.setFont(f(true, 12.5f * u));
            Kinh.chu(g, tt[i][1], x + 150 * u, by, Kinh.CHU);
        }
        y += tt.length * hh + 12 * u + 16 * u;
        // nút: Giấy phép & ghi công (chính) + Mở thư mục dữ liệu (nhỏ)
        Font fb = f(true, 13 * u);
        g.setFont(fb);
        float bh = 34 * u;
        String gp = "Giấy phép & ghi công";
        float gw = g.getFontMetrics().stringWidth(gp) + 36 * u;
        Kinh.nut(g, x, y, gw, bh, gp, fb, true, tro(x, y, gw, bh), false);
        them(x, y, gw, bh, new Runnable() {
            public void run() {
                caiDatMo = false;
                CaiDat.luu();
                try {
                    game.render.screen.GhiCongScr.mo();
                } catch (Throwable e) {
                    NhatKy.loi("giay phep", e);
                }
            }
        });
        String tm = "Mở thư mục dữ liệu";
        float tw = g.getFontMetrics().stringWidth(tm) + 36 * u;
        float tmx = x + gw + 10 * u;
        Kinh.nut(g, tmx, y, tw, bh, tm, fb, false, tro(tmx, y, tw, bh), false);
        them(tmx, y, tw, bh, new Runnable() {
            public void run() {
                moThuMuc();
            }
        });
    }

    /**
     * Tài khoản: tên nhân vật / cấp. 0.9.9 (chủ game): giftcode KHÔNG ở Cài đặt - một lối vào trong game là Menu -> Thư/Quà
     * -> "Nhập giftcode" (cộng NPC quà tặng).
     */
    private static void veTaiKhoan(Graphics2D g, float x, float y, float w, float hMax, float u) {
        game.model.MainChar mc = game.render.Canvas.gameScr == null ? null : game.render.Canvas.gameScr.mainChar;
        float hh = 58 * u;
        y = tieuDeNhom(g, "Tài khoản", x, y, u);
        the(g, x, y, w, hh, u);
        nhanHang(g, mc == null || mc.name == null ? "Chưa vào game" : mc.name,
                mc == null ? "Đăng nhập rồi chọn Pokémon để xem tài khoản" : "Pokémon cấp " + mc.level, x, y, hh, u);
    }

    private static void moThuMuc() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (!java.awt.Desktop.isDesktopSupported()
                            || !java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                        bao("Máy này không hỗ trợ mở thư mục");
                        return;
                    }
                    java.awt.Desktop.getDesktop().open(CaiDat.thuMucDuLieu());
                } catch (Throwable e) {
                    bao("Không mở được thư mục dữ liệu");
                }
            }
        }, "mo-thu-muc").start();
    }

    // ================================================================ nhập
    /** Phím (luồng sự kiện). true = lớp phủ đã dùng phím. */
    static boolean phim(int vk, int mod) {
        if (HopNhap.mo) {
            HopNhap.phim(vk, mod);
            return true;
        }
        if (!caiDatMo) return false;
        if (doiPhim >= 0) {
            if (vk != KeyEvent.VK_ESCAPE) {
                for (int i = 0; i < BanPhim.PHIM.length; i++) {
                    if (BanPhim.PHIM[i] == vk) BanPhim.PHIM[i] = 0; // bỏ trùng
                }
                BanPhim.PHIM[doiPhim] = vk;
                CaiDat.luu();
            }
            doiPhim = -1;
            return true;
        }
        if (vk == KeyEvent.VK_F1) {   // F1: trang Phím tắt <-> đóng
            if (tab == TAB_PHIM_TAT) moCaiDat();
            else tab = TAB_PHIM_TAT;
            return true;
        }
        if (vk == KeyEvent.VK_ESCAPE || vk == BanPhim.PHIM[BanPhim.CAI_DAT]) {
            moCaiDat();
            return true;
        }
        if (vk == KeyEvent.VK_LEFT || vk == KeyEvent.VK_RIGHT) {
            tab = (tab + (vk == KeyEvent.VK_LEFT ? TAB.length - 1 : 1)) % TAB.length;
        }
        return true;
    }

    static boolean goChu(char c) {
        if (HopNhap.mo) {
            HopNhap.goChu(c);
            return true;
        }
        return caiDatMo;
    }

    /** Chuột (luồng AWT): kieu 0 nhấn, 1 kéo, 2 thả. true = lớp phủ dùng. */
    static boolean chuot(int kieu, final int x, int y) {
        if (kieu == 0) {
            ArrayList<Vung> ds = vung;
            for (int i = ds.size() - 1; i >= 0; i--) {
                final Vung v = ds.get(i);
                if (!v.trong(x, y)) continue;
                if (v.keo != null) {
                    dangKeo = v;
                    NenPc.luong().them(new Runnable() {
                        public void run() {
                            v.keo.keo(x);
                        }
                    });
                } else if (v.bam != null) {
                    NenPc.luong().them(v.bam);
                }
                return true;
            }
            return false;
        }
        final Vung k = dangKeo;
        if (k != null) {
            NenPc.luong().them(new Runnable() {
                public void run() {
                    k.keo.keo(x);
                }
            });
            if (kieu == 2) {
                dangKeo = null;
                CaiDat.luu();
            }
        }
        return true;
    }

    static boolean chuotPhai(int x, int y) {
        if (caiDatMo) {
            moCaiDat();
            return true;
        }
        return HopNhap.mo;
    }

    static boolean cuon(int n, int x, int y) {
        return caiDatMo || HopNhap.mo;
    }

    /** Chuột đang trỏ vào nút của lớp phủ (đổi con trỏ bàn tay). */
    static boolean diQua(int x, int y) {
        ArrayList<Vung> ds = vung;
        for (int i = ds.size() - 1; i >= 0; i--) {
            Vung v = ds.get(i);
            if (v.trong(x, y)) return v.bam != null || v.keo != null;
        }
        return false;
    }
}
