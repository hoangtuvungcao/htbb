package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.MainChar;
import game.render.Canvas;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Hiệu ứng chỉ ở phía client (không gửi gì lên server, người khác không thấy):
 * - di chuyển: hạt sáng tròn (lõi trắng, viền màu) tỏa ra sau lưng nhân vật mình khi chạy, bay lên,
 *   nhỏ dần rồi tắt. Vẽ từ GameScr.paint TRƯỚC khi vẽ nhân vật (veDuoi) nên hạt nằm sau người.
 * - màn hình: hạt rơi trên màn game (hoa đào / tuyết / lá vàng / mưa / đom đóm), vẽ từ AutoGuard.paint.
 * Chỉ dùng số nguyên (máy J2ME), màu chia bậc cố định để bản PC không sinh quá nhiều ảnh màu trong
 * MyGraphics.fillRect. Cài đặt lưu RMS "htbbhu".
 */
public class HieuUng {
    static final String[] DI_CHUYEN = new String[]{"Tắt", "Ngũ sắc", "Vàng kim", "Băng lam", "Tím huyễn", "Lửa"};
    static final String[] MAN_HINH = new String[]{"Tắt", "Hoa đào", "Tuyết", "Lá vàng", "Mưa", "Đom đóm"};
    static final String[] MAT_DO = new String[]{"", "Ít", "Vừa", "Nhiều"};
    static int diChuyen = 1;
    static int manHinh = 1;
    static int matDo = 2;
    private static final String RMS_TEN = "htbbhu";
    private static boolean daNap;

    /** 0..N_DI-1: hạt di chuyển (tọa độ map), N_DI..N-1: hạt màn hình (tọa độ màn hình). Đơn vị 1/16 px. */
    private static final int N_DI = 60;
    private static final int N = 140;
    private static final int[] hx = new int[N];
    private static final int[] hy = new int[N];
    private static final int[] vx = new int[N];
    private static final int[] vy = new int[N];
    private static final int[] con = new int[N];
    private static final int[] dai = new int[N];
    private static final int[] mau = new int[N];
    private static final boolean[] song = new boolean[N];
    private static final Random rd = new Random();
    /** lắc ngang của hạt rơi (px), thay cho Math.sin */
    private static final int[] LAC = new int[]{0, 1, 2, 2, 1, 0, -1, -2, -2, -1};
    /** bảng màu viền hạt sáng theo kiểu di chuyển (chỉ số = diChuyen) */
    private static final int[][] BANG_MAU = new int[][]{
        new int[]{0xFFFFFF},
        new int[]{0xFF4060, 0xFF9A30, 0xFFE040, 0x50E870, 0x40C0FF, 0x7068FF, 0xE060FF},
        new int[]{0xFFD040, 0xFFB020, 0xFFE68A, 0xFFC860},
        new int[]{0x40D8FF, 0x80F0FF, 0x3098FF, 0xA8F8FF},
        new int[]{0xD070FF, 0xFF80E0, 0x9858FF, 0xF0A8FF},
        new int[]{0xFF9020, 0xFFC030, 0xFF5818, 0xFFE070}};
    /** nửa bề ngang từng hàng của hình tròn bán kính r (0..4) */
    private static final int[][] HANG = new int[][]{
        new int[]{0},
        new int[]{0, 1, 0},
        new int[]{1, 2, 2, 2, 1},
        new int[]{1, 2, 3, 3, 3, 2, 1},
        new int[]{1, 3, 3, 4, 4, 4, 3, 3, 1}};
    private static final int[] HOA = new int[]{0xFFB7C5, 0xFF8FAB, 0xFFD6E0};
    private static final int[] LA = new int[]{0xE0A030, 0xC87020, 0xA05018, 0xD8B040};
    private static long luc;
    private static long lucDi;
    private static int cuX = -9999;
    private static int cuY;
    private static int demDi;
    private static int demMan;

    private static int ngauNhien(int n) {
        return n <= 0 ? 0 : (rd.nextInt() & 0x7FFFFFFF) % n;
    }

    /** a -> b theo t/n, chia 6 bậc */
    private static int pha(int a, int b, int t, int n) {
        if (n <= 0) {
            return a;
        }
        int bac = t * 6 / n;
        if (bac > 6) {
            bac = 6;
        }
        if (bac < 0) {
            bac = 0;
        }
        int r = (a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * bac / 6;
        int g = (a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * bac / 6;
        int x = (a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * bac / 6;
        return r << 16 | g << 8 | x;
    }

    static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        try {
            String s = RMS.loadRMSString(RMS_TEN);
            if (s == null || s.length() < 5) {
                return;
            }
            int a = s.charAt(0) - '0';
            int b = s.charAt(2) - '0';
            int c = s.charAt(4) - '0';
            if (a >= 0 && a < DI_CHUYEN.length) {
                diChuyen = a;
            }
            if (b >= 0 && b < MAN_HINH.length) {
                manHinh = b;
            }
            if (c >= 1 && c < MAT_DO.length) {
                matDo = c;
            }
        }
        catch (Throwable throwable) {
            // RMS hỏng thì dùng mặc định
        }
    }

    private static void luu() {
        try {
            RMS.saveRMSString(RMS_TEN, new StringBuilder().append(diChuyen).append(",").append(manHinh).append(",").append(matDo).toString());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void xoa(int tu, int den) {
        for (int i = tu; i < den; ++i) {
            song[i] = false;
        }
    }

    static void moMenu(int chon) {
        HieuUng.nap();
        Vector vector = new Vector();
        vector.addElement(new Command(new StringBuilder().append("Di chuyển: ").append(DI_CHUYEN[diChuyen]).toString(), new IAction(){

            public void perform() {
                diChuyen = (diChuyen + 1) % DI_CHUYEN.length;
                HieuUng.xoa(0, N_DI);
                HieuUng.luu();
                HieuUng.moMenu(0);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Màn hình: ").append(MAN_HINH[manHinh]).toString(), new IAction(){

            public void perform() {
                manHinh = (manHinh + 1) % MAN_HINH.length;
                HieuUng.xoa(N_DI, N);
                HieuUng.luu();
                HieuUng.moMenu(1);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Mật độ: ").append(MAT_DO[matDo]).toString(), new IAction(){

            public void perform() {
                matDo = matDo >= MAT_DO.length - 1 ? 1 : matDo + 1;
                HieuUng.luu();
                HieuUng.moMenu(2);
            }
        }));
        vector.addElement(new Command("Tắt hết hiệu ứng", new IAction(){

            public void perform() {
                diChuyen = 0;
                manHinh = 0;
                HieuUng.xoa(0, N);
                HieuUng.luu();
                Canvas.menu.showMenu = false;
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    private static int choTrong(int tu, int den) {
        for (int i = tu; i < den; ++i) {
            if (!song[i]) {
                return i;
            }
        }
        return -1;
    }

    // ---------------- di chuyển: hạt sáng tỏa ra sau lưng
    /** (hx, hy) chân nhân vật; (ndx, ndy) hướng chạy, độ dài ~16 */
    private static void sinhDi(int x, int y, int ndx, int ndy) {
        int i = HieuUng.choTrong(0, N_DI);
        if (i < 0) {
            return;
        }
        song[i] = true;
        hx[i] = (x + ngauNhien(9) - 4) * 16;
        hy[i] = (y - 3 - ngauNhien(24)) * 16;
        int manh = 6 + ngauNhien(14);
        // bắn ngược hướng chạy, tỏa ngang, hơi bay lên
        vx[i] = -ndx * manh / 16 + ngauNhien(13) - 6;
        vy[i] = -ndy * manh / 16 + ngauNhien(9) - 4 - (diChuyen == 5 ? 10 : 3);
        dai[i] = 450 + ngauNhien(350);
        int[] bang = BANG_MAU[diChuyen];
        mau[i] = bang[ngauNhien(bang.length)];
        con[i] = dai[i];
    }

    /** Hạt sáng: viền màu, giữa sáng hơn, lõi trắng lúc mới sinh; nhỏ dần theo tuổi. */
    private static void veDi(Graphics graphics, int i, int x, int y) {
        int tuoi = dai[i] - con[i];
        int r = 1 + con[i] * 3 / dai[i];
        if (r > 3) {
            r = 3;
        }
        int vien = mau[i];
        if (diChuyen == 5) {
            // lửa: vàng sáng -> cam -> đỏ sẫm
            vien = tuoi * 2 < dai[i] ? HieuUng.pha(0xFFE070, mau[i], tuoi * 2, dai[i]) : HieuUng.pha(mau[i], 0xA01800, tuoi * 2 - dai[i], dai[i]);
        }
        HieuUng.veTron(graphics, x, y, r, vien);
        if (r >= 2) {
            HieuUng.veTron(graphics, x, y, r - 1, HieuUng.pha(vien, 0xFFFFFF, 3, 6));
        }
        if (tuoi * 3 < dai[i] * 2) {
            graphics.setColor(0xFFFFFF);
            graphics.fillRect(x, y, 1, 1);
        }
        // một số hạt lấp lánh: chữ thập trắng nhấp nháy
        if (i % 3 == 0 && tuoi * 2 < dai[i] && (tuoi / 70) % 2 == 0) {
            graphics.setColor(HieuUng.pha(0xFFFFFF, vien, 2, 6));
            graphics.fillRect(x - r - 2, y, 2 * r + 5, 1);
            graphics.fillRect(x, y - r - 2, 1, 2 * r + 5);
        }
    }

    private static void veTron(Graphics graphics, int cx, int cy, int r, int mauVe) {
        int[] hang = HANG[r];
        graphics.setColor(mauVe);
        for (int k = 0; k < hang.length; ++k) {
            graphics.fillRect(cx - hang[k], cy - r + k, 2 * hang[k] + 1, 1);
        }
    }

    /**
     * Gọi từ GameScr.paint sau lớp hiệu ứng nền, TRƯỚC khi vẽ nhân vật / quái; graphics đang dịch
     * theo camera nên vẽ thẳng tọa độ map.
     */
    public static void veDuoi(Graphics graphics) {
        HieuUng.nap();
        long t = System.currentTimeMillis();
        int dt = lucDi == 0L ? 16 : (int)(t - lucDi);
        lucDi = t;
        if (dt < 0 || dt > 100) {
            dt = 100;
        }
        if (diChuyen == 0) {
            HieuUng.xoa(0, N_DI);
            cuX = -9999;
            return;
        }
        MainChar mainChar = Canvas.gameScr.mainChar;
        if (mainChar != null) {
            int x = mainChar.x;
            int y = mainChar.y;
            int dx = x - cuX;
            int dy = y - cuY;
            boolean di = cuX != -9999 && (dx != 0 || dy != 0) && dx < 80 && dx > -80 && dy < 80 && dy > -80;
            cuX = x;
            cuY = y;
            if (di) {
                int dai2 = Math.max(dx < 0 ? -dx : dx, dy < 0 ? -dy : dy);
                int ndx = dx * 16 / dai2;
                int ndy = dy * 16 / dai2;
                demDi += dt * matDo;
                while (demDi >= 30) {
                    demDi -= 30;
                    HieuUng.sinhDi(x, y, ndx, ndy);
                }
            } else {
                demDi = 0;
            }
        }
        for (int i = 0; i < N_DI; ++i) {
            if (!song[i]) {
                continue;
            }
            con[i] -= dt;
            if (con[i] <= 0) {
                song[i] = false;
                continue;
            }
            hx[i] += vx[i] * dt / 16;
            hy[i] += vy[i] * dt / 16;
            // chậm dần
            vx[i] = vx[i] * (512 - dt) / 512;
            vy[i] = vy[i] * (512 - dt) / 512 - (diChuyen == 5 ? dt / 16 : 0);
            HieuUng.veDi(graphics, i, hx[i] / 16, hy[i] / 16);
        }
    }

    // ---------------- màn hình
    private static void sinhMan(int w, int h) {
        int i = HieuUng.choTrong(N_DI, N);
        if (i < 0) {
            return;
        }
        song[i] = true;
        if (manHinh == 5) {
            hx[i] = ngauNhien(w) * 16;
            hy[i] = (h / 6 + ngauNhien(h * 5 / 6)) * 16;
            vx[i] = ngauNhien(9) - 4;
            vy[i] = ngauNhien(9) - 4;
            dai[i] = 3000 + ngauNhien(3000);
            mau[i] = ngauNhien(2) == 0 ? 0xD8FF60 : 0xFFF080;
        } else {
            hx[i] = ngauNhien(w + w / 3) * 16;
            hy[i] = -ngauNhien(20) * 16;
            dai[i] = 30000;
            if (manHinh == 1) {
                vx[i] = -4 - ngauNhien(5);
                vy[i] = 10 + ngauNhien(10);
                mau[i] = HOA[ngauNhien(HOA.length)];
            } else if (manHinh == 2) {
                vx[i] = -2 - ngauNhien(3);
                vy[i] = 8 + ngauNhien(12);
                mau[i] = ngauNhien(3) == 0 ? 0xD8ECFF : 0xFFFFFF;
            } else if (manHinh == 3) {
                vx[i] = -6 - ngauNhien(6);
                vy[i] = 12 + ngauNhien(10);
                mau[i] = LA[ngauNhien(LA.length)];
            } else {
                hy[i] = -ngauNhien(h) * 16;
                vx[i] = -26;
                vy[i] = 150 + ngauNhien(60);
                mau[i] = ngauNhien(2) == 0 ? 0x9CC8FF : 0xC8E0FF;
            }
        }
        con[i] = dai[i];
    }

    private static void veMan(Graphics graphics, int i, int x, int y, long t) {
        if (manHinh == 5) {
            if ((t / 220 + i) % 5 == 0) {
                return;
            }
            graphics.setColor(mau[i]);
            graphics.fillRect(x, y, 3, 3);
            if ((t / 220 + i) % 5 == 2) {
                graphics.fillRect(x - 2, y + 1, 7, 1);
                graphics.fillRect(x + 1, y - 2, 1, 7);
            }
            return;
        }
        int lac = LAC[(int)((t / 160 + i) % LAC.length)];
        graphics.setColor(mau[i]);
        if (manHinh == 1) {
            graphics.fillRect(x + lac, y, 5, 3);
            graphics.fillRect(x + lac + 1, y + 3, 3, 1);
            graphics.fillRect(x + lac + 1, y - 1, 2, 1);
        } else if (manHinh == 2) {
            int k = 2 + i % 3;
            graphics.fillRect(x + lac, y, k, k);
        } else if (manHinh == 3) {
            graphics.fillRect(x + lac * 2, y, 5, 3);
            graphics.fillRect(x + lac * 2 + 2, y - 1, 3, 1);
            graphics.fillRect(x + lac * 2 - 1, y + 3, 2, 1);
        } else {
            graphics.drawLine(x, y, x - 3, y + 12);
            graphics.drawLine(x + 1, y, x - 2, y + 12);
        }
    }

    /** Hạt rơi trên màn game. Gọi mỗi khung hình từ AutoGuard.paint (đã trả gốc tọa độ về 0,0). */
    static void ve(Graphics graphics) {
        if (Canvas.currentScreen != Canvas.gameScr) {
            return;
        }
        HieuUng.nap();
        // 0.9.8: đang có hạt thời tiết / mùa của máy chủ (gói -118) thì tự tắt hạt màn hình của mod (chủ game chốt):
        // không hai lớp hạt chồng nhau, nhẹ cho máy J2ME. Cài đặt người chơi giữ nguyên, hết thời tiết thì hiện lại.
        if (manHinh == 0 || Canvas.menu.showMenu || Canvas.currentDialog != null || game.pokemon.ThoiTiet.dangVeHat()) {
            luc = 0L;
            return;
        }
        long t = System.currentTimeMillis();
        int dt = luc == 0L ? 16 : (int)(t - luc);
        luc = t;
        if (dt < 0 || dt > 100) {
            dt = 100;
        }
        int w = Canvas.w;
        int h = Canvas.h;
        graphics.setClip(0, 0, w, h);
        int toiDa = (manHinh == 4 ? 26 : manHinh == 5 ? 10 : 16) * matDo;
        int dangCo = 0;
        for (int i = N_DI; i < N; ++i) {
            if (song[i]) {
                ++dangCo;
            }
        }
        demMan += dt;
        int moi = manHinh == 4 ? 24 : 160;
        while (demMan >= moi) {
            demMan -= moi;
            if (dangCo < toiDa) {
                HieuUng.sinhMan(w, h);
                ++dangCo;
            }
        }
        for (int i = N_DI; i < N; ++i) {
            if (!song[i]) {
                continue;
            }
            con[i] -= dt;
            if (manHinh == 5 && ngauNhien(40) == 0) {
                vx[i] = ngauNhien(9) - 4;
                vy[i] = ngauNhien(9) - 4;
            }
            hx[i] += vx[i] * dt / 16;
            hy[i] += vy[i] * dt / 16;
            int x = hx[i] / 16;
            int y = hy[i] / 16;
            if (con[i] <= 0 || y > h + 10 || x < -20 || manHinh == 5 && (x > w + 10 || y < -10)) {
                song[i] = false;
                continue;
            }
            if (x > w + 10 || y < -30) {
                continue;
            }
            HieuUng.veMan(graphics, i, x, y, t);
        }
    }
}
