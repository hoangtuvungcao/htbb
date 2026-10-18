package htbb.pc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javax.microedition.lcdui.NenPc;

/**
 * Lớp phủ HD của bản PC, vẽ sau game ở toạ độ THẬT (khử răng cưa): dải gợi ý phím tắt trong màn chơi, nút
 * cài đặt, bảng Cài đặt (độ phân giải / toàn màn / cỡ giao diện / âm lượng / đổi phím / giới thiệu), hộp nhập
 * chữ (TextBox), thông báo nhanh, FPS. Giao diện kiểu "immediate mode": mỗi khung vẽ lại + ghi vùng bấm;
 * chuột (luồng AWT) đọc bản chụp vùng bấm, hành động chạy trên luồng sự kiện NenPc.
 */
public final class LopPhu {
    private LopPhu() {
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

    private static volatile long moGoiYLuc = System.currentTimeMillis();

    /** F1: mở dải phím tắt (đếm lại thời gian tự thu gọn). */
    static void moGoiY() {
        moGoiYLuc = System.currentTimeMillis();
        CaiDat.luu();
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
        return PhongChu.thuong(dam).deriveFont(co);
    }

    // ================================================================ vẽ
    public static void ve(Graphics2D g, int W, int H) {
        dangVe = new ArrayList<Vung>();
        Kinh.min(g);
        float u = Math.max(0.75f, Math.min(W / 1280f, H / 720f));
        try {
            if (BanPhim.trongTheGioi() && !caiDatMo && !HopNhap.mo) {
                if (CaiDat.goiYPhim) veGoiY(g, W, H, u);
            }
            if (!caiDatMo && !HopNhap.mo) {
                ThanhLenh.ve(g, W, H, u, dangVe);
                veNutCaiDat(g, W, H, u);
            }
            if (HopNhap.mo) HopNhap.ve(g, W, H, u, dangVe);
            if (caiDatMo) veCaiDat(g, W, H, u);
            veThongBao(g, W, H, u);
            if (CaiDat.hienFps) veFps(g, W, H, u);
        } catch (Throwable e) {
            NhatKy.loi("lop phu", e);
        }
        vung = dangVe;
    }

    private static void veFps(Graphics2D g, int W, int H, float u) {
        long now = System.currentTimeMillis();
        if (now - fpsMoc >= 1000) {
            fps = (int) ((NenPc.soKhung - fpsKhung) * 1000 / Math.max(1, now - fpsMoc));
            fpsMoc = now;
            fpsKhung = NenPc.soKhung;
            fpsHt = (int) ((ManHinh.soTrinhBay - htKhung) * 1000 / Math.max(1, now - htMoc));
            htMoc = now;
            htKhung = ManHinh.soTrinhBay;
        }
        g.setFont(f(true, 12 * u));
        String s = "game " + fps + " · hiển thị " + fpsHt + " FPS  " + W + "×" + H + "  logic " + NenPc.rong + "×" + NenPc.cao + " ×" + NenPc.heSo;
        Kinh.chu(g, s, 8 * u, H - 8 * u, Kinh.VANG);
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
        Kinh.chuGiua(g, s, W / 2f, y + h / 2, Kinh.mo(Kinh.CHU, a), true);
    }

    /** Nút bánh răng nhỏ (mở Cài đặt) - giữa mép trên, cạnh nút bàn phím ảo của game. */
    private static void veNutCaiDat(Graphics2D g, int W, int H, float u) {
        float r = 13 * u;
        float cx = W / 2f + NenPc.heSo * 22 + r;
        float cy = NenPc.lechY + 4 * u + r;
        boolean sang = tro(cx - r, cy - r, 2 * r, 2 * r);
        int a = sang ? 255 : 150;
        g.setColor(new Color(10, 20, 44, a * 200 / 255));
        g.fill(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
        g.setColor(new Color(255, 255, 255, a * 140 / 255));
        g.setStroke(new BasicStroke(1.2f * u));
        g.draw(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
        // bánh răng
        g.setColor(new Color(255, 255, 255, a));
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
            String s = "Cài đặt (" + BanPhim.tenPhim(BanPhim.PHIM[BanPhim.CAI_DAT]) + ") · phím tắt (F1)";
            float w = fm.stringWidth(s) + 16 * u;
            Kinh.bang(g, cx + r + 6 * u, cy - 12 * u, w, 24 * u, 12 * u, 255);
            Kinh.chuGiua(g, s, cx + r + 6 * u + w / 2, cy, Kinh.CHU, true);
        }
        them(cx - r, cy - r, 2 * r, 2 * r, new Runnable() {
            public void run() {
                moCaiDat();
            }
        });
    }

    /** Dải gợi ý phím tắt (màn chơi): 30 s đầu hiện đủ, sau đó thu gọn còn một dòng nhỏ; di chuột vào để mở. */
    private static void veGoiY(Graphics2D g, int W, int H, float u) {
        String[][] ds = {
            {ten(BanPhim.LEN) + ten(BanPhim.TRAI) + ten(BanPhim.XUONG) + ten(BanPhim.PHAI), "Đi"},
            {ten(BanPhim.DANH), "Đánh"},
            {ten(BanPhim.CHIEU1) + "–" + ten(BanPhim.CHIEU1 + 4), "Chiêu"},
            {ten(BanPhim.BINH_HP) + "/" + ten(BanPhim.BINH_MP), "Bình HP/MP"},
            {ten(BanPhim.MUC_TIEU), "Mục tiêu"},
            {ten(BanPhim.CHAT), "Chat"},
            {ten(BanPhim.MENU), "Menu"},
            {ten(BanPhim.MENU_NHANH), "Menu nhanh"},
            {"Lăn / + -", "Thu phóng"},
            {"F1", "Ẩn gợi ý"},
        };
        float h = 18 * u;
        Font fp = f(true, 10.5f * u);
        Font fc = f(false, 11.5f * u);
        g.setFont(fc);
        FontMetrics fm = g.getFontMetrics();
        // bề rộng
        float tong = 10 * u;
        float[] wp = new float[ds.length];
        for (int i = 0; i < ds.length; i++) {
            g.setFont(fp);
            wp[i] = Math.max(h, g.getFontMetrics().stringWidth(ds[i][0]) + h * 0.6f);
            tong += wp[i] + 5 * u + fm.stringWidth(ds[i][1]) + 12 * u;
        }
        float x0 = NenPc.lechX + 8 * u;
        // dưới nút Poké Ball (Menu nhanh) ở mép trái giữa màn
        float y0 = NenPc.lechY + NenPc.cao * NenPc.heSo * 0.5f + 34 * u;
        // tự thu gọn sau 60 giây (F1 mở lại / tắt hẳn); thu gọn = một phím nhỏ "F1" mờ
        boolean gon = !CaiDat.goiYMo || System.currentTimeMillis() - moGoiYLuc > 60000;
        if (gon) {
            if (!CaiDat.goiYMo) return;
            float wPhim = Kinh.phim(g, x0, y0, h * 0.9f, "F1", fp);
            g.setFont(f(false, 10.5f * u));
            Kinh.chu(g, "phím tắt", x0 + wPhim + 4 * u, y0 + h * 0.66f, Kinh.mo(Kinh.CHU, 170));
            return;
        }
        // cột dọc mép trái dưới Poké Ball (không đè HP góc trái trên, ô chiêu giữa dưới)
        float hang = h * 1.22f;
        float rongCot = 0;
        for (int i = 0; i < ds.length; i++) {
            rongCot = Math.max(rongCot, wp[i] + 6 * u + fm.stringWidth(ds[i][1]));
        }
        float yTop = y0 + 4 * u;
        Kinh.bang(g, x0 - 4 * u, yTop - 5 * u, rongCot + 16 * u, hang * ds.length + 8 * u, 12 * u, 150);
        for (int i = 0; i < ds.length; i++) {
            float y = yTop + i * hang;
            Kinh.phim(g, x0 + 2 * u, y, h, ds[i][0], fp);
            g.setFont(fc);
            Kinh.chu(g, ds[i][1], x0 + 2 * u + wp[i] + 6 * u, y + h * 0.74f, Kinh.CHU);
        }
    }

    private static String ten(int hd) {
        String s = BanPhim.tenPhim(BanPhim.PHIM[hd]);
        return s;
    }

    // ================================================================ bảng cài đặt
    private static final String[] TAB = {"Hiển thị", "Âm thanh", "Phím", "Giới thiệu"};
    private static final int[][] DO_PHAN_GIAI = {{1280, 720}, {1600, 900}, {1920, 1080}, {2560, 1440}, {2560, 1080}, {1024, 768}, {1440, 1080}};
    private static final String[] TEN_DPG = {"1280×720", "1600×900", "1920×1080", "2560×1440", "2560×1080 (21:9)", "1024×768 (4:3)", "1440×1080 (4:3)"};

    private static void veCaiDat(Graphics2D g, int W, int H, float u) {
        // nền tối mờ
        g.setColor(new Color(4, 8, 20, 150));
        g.fillRect(0, 0, W, H);
        them(0, 0, W, H, null); // chặn bấm xuống game
        float pw = Math.min(W - 40 * u, 760 * u);
        float ph = Math.min(H - 40 * u, 520 * u);
        float px = (W - pw) / 2;
        float py = (H - ph) / 2;
        Kinh.bang(g, px, py, pw, ph, 22 * u, 255);
        // tiêu đề
        g.setFont(PhongChu.tieuDe().deriveFont(Font.BOLD, 26 * u));
        Kinh.chu(g, "Cài đặt", px + 26 * u, py + 42 * u, Kinh.VANG);
        g.setFont(f(false, 12 * u));
        Kinh.chu(g, "Huyền Thoại Bảo Bối · bản PC", px + 136 * u, py + 40 * u, Kinh.CHU_MO);
        // nút đóng
        float bx = px + pw - 46 * u, by = py + 16 * u, bs = 30 * u;
        boolean sx = tro(bx, by, bs, bs);
        Kinh.nut(g, bx, by, bs, bs, "✕", f(true, 14 * u), false, sx, false);
        them(bx, by, bs, bs, new Runnable() {
            public void run() {
                moCaiDat();
            }
        });
        // tab
        float tx = px + 24 * u, ty = py + 60 * u, th = 32 * u;
        for (int i = 0; i < TAB.length; i++) {
            g.setFont(f(true, 13 * u));
            float tw = g.getFontMetrics().stringWidth(TAB[i]) + 34 * u;
            final int ii = i;
            Kinh.nut(g, tx, ty, tw, th, TAB[i], f(true, 13 * u), false, tro(tx, ty, tw, th), tab == i);
            them(tx, ty, tw, th, new Runnable() {
                public void run() {
                    tab = ii;
                    doiPhim = -1;
                }
            });
            tx += tw + 8 * u;
        }
        g.setColor(new Color(255, 255, 255, 40));
        g.fillRect((int) (px + 24 * u), (int) (ty + th + 10 * u), (int) (pw - 48 * u), Math.max(1, (int) u));
        float cx = px + 30 * u, cy = ty + th + 26 * u, cw = pw - 60 * u;
        switch (tab) {
            case 0: veHienThi(g, cx, cy, cw, u); break;
            case 1: veAmThanh(g, cx, cy, cw, u); break;
            case 2: vePhim(g, cx, cy, cw, py + ph - cy - 20 * u, u); break;
            default: veGioiThieu(g, cx, cy, cw, u); break;
        }
        g.setFont(f(false, 11 * u));
        Kinh.chu(g, "Esc / " + BanPhim.tenPhim(BanPhim.PHIM[BanPhim.CAI_DAT]) + ": đóng · cài đặt tự lưu", px + 26 * u, py + ph - 14 * u, Kinh.CHU_MO);
        // giấy phép / ghi công (yêu cầu CC BY-NC 4.0 + OFL): mục nhỏ góc phải dưới
        String gp = "Giấy phép";
        float gw = g.getFontMetrics().stringWidth(gp) + 16 * u;
        float gx = px + pw - 26 * u - gw, gy = py + ph - 30 * u;
        boolean gs = tro(gx, gy, gw, 22 * u);
        Kinh.nut(g, gx, gy, gw, 22 * u, gp, f(false, 11 * u), false, gs, false);
        them(gx, gy, gw, 22 * u, new Runnable() {
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
    }

    private static float nhan(Graphics2D g, String s, float x, float y, float u) {
        g.setFont(f(true, 13.5f * u));
        Kinh.chu(g, s, x, y + 19 * u, Kinh.CHU);
        return x + 170 * u;
    }

    private static float congTac(Graphics2D g, String s, String moTa, float x, float y, float u, boolean bat, Runnable doi) {
        float xx = nhan(g, s, x, y, u);
        float h = 22 * u;
        boolean sang = tro(xx, y + 4 * u, h * 1.9f, h);
        Kinh.congTac(g, xx, y + 4 * u, h, bat, sang);
        if (moTa != null) {
            g.setFont(f(false, 11.5f * u));
            Kinh.chu(g, moTa, xx + h * 1.9f + 12 * u, y + 20 * u, Kinh.CHU_MO);
        }
        them(xx, y + 4 * u, h * 1.9f, h, doi);
        return y + 36 * u;
    }

    private static void veHienThi(Graphics2D g, float x, float y, float w, float u) {
        float xx = nhan(g, "Độ phân giải", x, y, u);
        float bx = xx, by = y;
        ManHinh m = ManHinh.me;
        for (int i = 0; i < DO_PHAN_GIAI.length; i++) {
            g.setFont(f(true, 12 * u));
            float bw = g.getFontMetrics().stringWidth(TEN_DPG[i]) + 22 * u;
            if (bx + bw > x + w) {
                bx = xx;
                by += 34 * u;
            }
            final int dw = DO_PHAN_GIAI[i][0], dh = DO_PHAN_GIAI[i][1];
            boolean dang = m != null && !m.laToanMan() && m.getWidth() == dw && m.getHeight() == dh;
            Kinh.nut(g, bx, by + 2 * u, bw, 28 * u, TEN_DPG[i], f(true, 12 * u), false, tro(bx, by, bw, 28 * u), dang);
            them(bx, by, bw, 28 * u, new Runnable() {
                public void run() {
                    ManHinh mm = ManHinh.me;
                    if (mm != null) mm.datCo(dw, dh);
                }
            });
            bx += bw + 8 * u;
        }
        y = by + 42 * u;
        final boolean tm = m != null && m.laToanMan();
        y = congTac(g, "Toàn màn hình", "F11 hoặc Alt+Enter", x, y, u, tm, new Runnable() {
            public void run() {
                ManHinh mm = ManHinh.me;
                if (mm != null) mm.datToanMan(!tm);
            }
        });
        xx = nhan(g, "Cỡ giao diện", x, y, u);
        String[] ten = {"Lớn", "Vừa", "Nhỏ"};
        final int[] gt = {240, 320, 400};
        bx = xx;
        for (int i = 0; i < 3; i++) {
            float bw = 70 * u;
            final int v = gt[i];
            Kinh.nut(g, bx, y + 2 * u, bw, 28 * u, ten[i], f(true, 12 * u), false, tro(bx, y, bw, 28 * u), CaiDat.coGiaoDien == v);
            them(bx, y, bw, 28 * u, new Runnable() {
                public void run() {
                    CaiDat.coGiaoDien = v;
                    NenPc.canhNgan = v;
                    ManHinh mm = ManHinh.me;
                    if (mm != null) mm.doiCo();
                }
            });
            bx += bw + 8 * u;
        }
        g.setFont(f(false, 11.5f * u));
        Kinh.chu(g, "logic " + NenPc.rong + "×" + NenPc.cao + " · phóng ×" + NenPc.heSo, bx + 6 * u, y + 20 * u, Kinh.CHU_MO);
        y += 40 * u;
        y = congTac(g, "Chữ HD", "font thật Noto Sans (tắt = font điểm ảnh gốc)", x, y, u, ChuHD.bat, new Runnable() {
            public void run() {
                CaiDat.chuHD = !CaiDat.chuHD;
                ChuHD.bat = CaiDat.chuHD;
                ManHinh mm = ManHinh.me;
                if (mm != null) NenPc.baoDoiCo();
            }
        });
        y = congTac(g, "Khung HD", "khung, bảng, thanh máu vẽ mịn", x, y, u, GiaoDienHD.bat, new Runnable() {
            public void run() {
                CaiDat.khungHD = !CaiDat.khungHD;
                GiaoDienHD.bat = CaiDat.khungHD;
            }
        });
        xx = nhan(g, "Hiệu ứng", x, y, u);
        String[] tenHu = {"Cao (60 FPS)", "Thấp"};
        bx = xx;
        for (int i = 0; i < 2; i++) {
            final boolean cao = i == 0;
            float bw = 120 * u;
            Kinh.nut(g, bx, y + 2 * u, bw, 28 * u, tenHu[i], f(true, 12 * u), false, tro(bx, y, bw, 28 * u), CaiDat.hieuUngCao == cao);
            them(bx, y, bw, 28 * u, new Runnable() {
                public void run() {
                    CaiDat.hieuUngCao = cao;
                }
            });
            bx += bw + 8 * u;
        }
        y += 40 * u;
        y = congTac(g, "Gợi ý phím tắt", "dải phím bên trái màn chơi", x, y, u, CaiDat.goiYPhim, new Runnable() {
            public void run() {
                CaiDat.goiYPhim = !CaiDat.goiYPhim;
            }
        });
        congTac(g, "Hiện FPS", null, x, y, u, CaiDat.hienFps, new Runnable() {
            public void run() {
                CaiDat.hienFps = !CaiDat.hienFps;
            }
        });
    }

    private static float thanhTruot(Graphics2D g, String s, float x, float y, float w, float u, int gt, final Keo k) {
        float xx = nhan(g, s, x, y, u);
        float tw = Math.min(320 * u, x + w - xx - 60 * u);
        float h = 8 * u;
        float ty = y + 12 * u;
        Kinh.thanhTruot(g, xx, ty, tw, h, gt, tro(xx - 8 * u, y, tw + 16 * u, 30 * u));
        g.setFont(f(true, 13 * u));
        Kinh.chu(g, gt + "%", xx + tw + 16 * u, y + 19 * u, Kinh.CHU);
        dangVe.add(new Vung(xx - 8 * u, y, tw + 16 * u, 30 * u, null, new KeoTheo(xx, tw, k)));
        return y + 40 * u;
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

    private static void veAmThanh(Graphics2D g, float x, float y, float w, float u) {
        y = thanhTruot(g, "Nhạc nền", x, y, w, u, CaiDat.amNhac, new Keo() {
            public void keo(float t) {
                CaiDat.amNhac = Math.round(t * 100);
                CaiDat.apAmThanh();
            }
        });
        y = thanhTruot(g, "Hiệu ứng", x, y, w, u, CaiDat.amHieuUng, new Keo() {
            public void keo(float t) {
                CaiDat.amHieuUng = Math.round(t * 100);
                CaiDat.apAmThanh();
            }
        });
        y = congTac(g, "Tắt tiếng", null, x, y, u, CaiDat.tatTieng, new Runnable() {
            public void run() {
                CaiDat.tatTieng = !CaiDat.tatTieng;
                CaiDat.apAmThanh();
            }
        });
        g.setFont(f(false, 12 * u));
        int loi = javax.microedition.media.Manager.soLoiThietBi;
        String tt = loi > 0 ? "Thiết bị âm thanh: không mở được (" + loi + " lần) - game chạy không tiếng"
                : "Thiết bị âm thanh: sẵn sàng (WAV + MIDI)";
        Kinh.chu(g, tt, x, y + 18 * u, loi > 0 ? new Color(0xFFB0A0) : Kinh.CHU_MO);
        Kinh.chu(g, "Bật/tắt nhạc và tiếng trong game: Menu nhanh (" + ten(BanPhim.MENU_NHANH) + ") của game.", x, y + 42 * u, Kinh.CHU_MO);
    }

    private static void vePhim(Graphics2D g, float x, float y, float w, float h, float u) {
        int n = BanPhim.MA.length;
        int cot = 2;
        int moiCot = (n + cot - 1) / cot;
        float hang = Math.min(30 * u, (h - 40 * u) / moiCot);
        float cw = (w - 20 * u) / cot;
        for (int i = 0; i < n; i++) {
            float cx = x + (i / moiCot) * (cw + 20 * u);
            float cy = y + (i % moiCot) * hang;
            g.setFont(f(false, 12.5f * u));
            Kinh.chu(g, BanPhim.TEN[i], cx, cy + hang * 0.62f, Kinh.CHU);
            float bw = 110 * u;
            float bx = cx + cw - bw;
            final int ii = i;
            boolean cho = doiPhim == i;
            String nhan = cho ? "Nhấn phím…" : BanPhim.tenPhim(BanPhim.PHIM[i]);
            Kinh.nut(g, bx, cy + 2 * u, bw, hang - 5 * u, nhan, f(true, 12 * u), cho, tro(bx, cy, bw, hang), false);
            them(bx, cy, bw, hang, new Runnable() {
                public void run() {
                    doiPhim = doiPhim == ii ? -1 : ii;
                }
            });
        }
        float by = y + moiCot * hang + 8 * u;
        Kinh.nut(g, x, by, 150 * u, 28 * u, "Phím mặc định", f(true, 12 * u), false, tro(x, by, 150 * u, 28 * u), false);
        them(x, by, 150 * u, 28 * u, new Runnable() {
            public void run() {
                System.arraycopy(BanPhim.MAC_DINH, 0, BanPhim.PHIM, 0, BanPhim.PHIM.length);
                doiPhim = -1;
                CaiDat.luu();
            }
        });
        g.setFont(f(false, 11 * u));
        Kinh.chu(g, "Mũi tên luôn đi/chọn · F1 gợi ý · F3/F2 phím mềm trái/phải · chuột phải = quay lại · lăn = cuộn / thu phóng",
                x + 162 * u, by + 19 * u, Kinh.CHU_MO);
    }

    private static void veGioiThieu(Graphics2D g, float x, float y, float w, float u) {
        g.setFont(PhongChu.tieuDe().deriveFont(Font.BOLD, 22 * u));
        Kinh.chu(g, "Huyền Thoại Bảo Bối", x, y + 22 * u, Kinh.CHU);
        g.setFont(f(false, 12.5f * u));
        String pb = javax.microedition.midlet.MIDlet.thuocTinh("HTBB-Phien-Ban");
        String ban = javax.microedition.midlet.MIDlet.thuocTinh("HTBB-Ban");
        String mc = javax.microedition.midlet.MIDlet.thuocTinh("HTBB-May-Chu");
        boolean thu = "1".equals(javax.microedition.midlet.MIDlet.thuocTinh("HTBB-Ban-Thu"));
        String[] dong = {
            "Phiên bản: " + (pb == null ? "?" : pb) + (ban == null ? "" : " (bản " + ban + ")") + (thu ? " · bản THỬ" : ""),
            "Máy chủ: " + (mc == null ? "danh sách máy chủ" : mc),
            "Java: " + System.getProperty("java.version") + " · " + System.getProperty("os.name"),
            "Dữ liệu: " + CaiDat.thuMucDuLieu().getAbsolutePath(),
            "Phông chữ: Noto Sans, Baloo 2 (SIL OFL 1.1)",
            "Hình Pokémon: PMDCollab (CC BY-NC 4.0) - xem Giấy phép (dưới bảng)",
        };
        for (int i = 0; i < dong.length; i++) {
            Kinh.chu(g, dong[i], x, y + 52 * u + i * 22 * u, Kinh.CHU_MO);
        }
        float by = y + 52 * u + dong.length * 22 * u + 10 * u;
        Kinh.nut(g, x, by, 190 * u, 32 * u, "Mở thư mục dữ liệu", f(true, 13 * u), false, tro(x, by, 190 * u, 32 * u), false);
        them(x, by, 190 * u, 32 * u, new Runnable() {
            public void run() {
                try {
                    java.awt.Desktop.getDesktop().open(CaiDat.thuMucDuLieu());
                } catch (Throwable e) {
                    bao("Không mở được thư mục");
                }
            }
        });
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

    static void unused(GradientPaint p) {
    }
}
