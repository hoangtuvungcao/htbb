package game.model;

/**
 * Tìm đường + kiểm đoạn thẳng đi được cho nhân vật chính (chạm đất, đuổi quái, tự động đánh).
 * Thuần mảng int / short (MIDP2 / CLDC1.1), không cấp phát mỗi lần gọi (bộ đệm dùng lại).
 *
 * Thay thuật toán cũ GameScr.updateFindRoad (bao 0.9.2 "đánh / tự động đánh đi giật"):
 *  - cũ: mỗi bước mở rộng QUÉT CẢ BẢN ĐỒ (w*h ô, tới 500 bước) -> một lần tìm đường trên map 130x130 tốn
 *    hàng triệu phép so sánh, khựng cả khung hình; đuổi quái tìm lại mỗi lần bấm đánh -> giật liên tục.
 *  - cũ: tường là type == 2 đúng bằng, còn máy chủ (Map.canMove) và va chạm phím của client là (type & 2):
 *    đường đi xuyên ô có thêm bit khác -> máy chủ trả vị trí cũ -> kéo lùi.
 *  - cũ: toạ độ ô ép kiểu byte -> map rộng hơn 127 ô (map 2, 10, 11, 40, 114) đi lạc / không đi.
 * Mới: BFS 4 hướng trong khung bao (điểm đầu + đích + lề 12 ô, tối đa 100x100), tường = (type & 2), ô cổng
 * chuyển map (type >= 2000000000) chỉ đi được khi là đích.
 *
 * Mã hoá điểm đường (posTransRoad): ma(x, y) = ((x << 8) | y) + 1 ép short, 0 = trống / đã đi; thứ tự như cũ:
 * phần tử 0 là ĐÍCH, phần tử cuối là ô đầu tiên phải đi (MainChar.updateWayPointer đọc từ cuối về).
 */
public final class DuongDi {
    public static final int CONG = 2000000000;
    static final int LE = 12;
    static final int TOI_DA = 100;

    /** Lần tìm gần nhất bỏ cuộc vì quá xa (khung > TOI_DA), khác với "không có đường". */
    public static boolean quaXa;
    /** Số lần tìm đường / tổng số ô đã duyệt (đo). */
    public static int soLanTim, soODuyet;

    private static int[] truoc = new int[0];
    private static int[] hang = new int[0];

    private DuongDi() {
    }

    public static short ma(int x, int y) {
        return (short) ((((x & 255) << 8) | (y & 255)) + 1);
    }

    public static int maX(short e) {
        return (((e & 0xFFFF) - 1) >> 8) & 255;
    }

    public static int maY(short e) {
        return ((e & 0xFFFF) - 1) & 255;
    }

    public static boolean co(short e) {
        return e != 0;
    }

    /** Ô (x, y) là tường (luật va chạm của máy chủ Map.canMove: type & 2), ngoài bản đồ = tường. */
    public static boolean tuong(int[] type, int w, int h, int x, int y) {
        if (type == null || x < 0 || y < 0 || x >= w || y >= h) {
            return true;
        }
        int t = type[y * w + x];
        if (t >= CONG) {
            return false;
        }
        return (t & 2) == 2;
    }

    /**
     * Điểm pixel quái đi được (cùng luật va chạm người chơi / máy chủ com.htbb.pokemon.DiChuyenQuai): trong bản đồ,
     * không phải ô trống (tile 255 = vực), không phải ô cổng chuyển map, (type & 2) == 0. Chưa có bản đồ = được.
     */
    public static boolean diQuai(int[] type, short[] map, int w, int h, int px, int py) {
        if (type == null || w <= 0 || h <= 0) {
            return true;
        }
        if (px < 0 || py < 0) {
            return false;
        }
        int x = px >> 4, y = py >> 4;
        if (x >= w || y >= h) {
            return false;
        }
        int i = y * w + x;
        if (i >= type.length) {
            return false;
        }
        if (map != null && i < map.length && (map[i] == 255 || map[i] < 0)) {
            return false;
        }
        int t = type[i];
        return t < CONG && (t & 2) == 0;
    }

    /** Kết quả buocQuai (pixel). */
    public static int buocX, buocY;

    /**
     * Một bước tối đa sp px của quái từ (x, y) về (tx, ty): ưu tiên trục ngang như cũ, bị chặn thì đi trục dọc
     * (vòng qua góc tường). true = có bước (buocX / buocY), false = kẹt cả hai trục (nơi gọi tự xử lý: dịch thẳng tới
     * vị trí máy chủ / về điểm sinh, không bao giờ bước vào ô chặn).
     */
    public static boolean buocQuai(int[] type, short[] map, int w, int h, int x, int y, int tx, int ty, int sp) {
        if (sp < 1) {
            sp = 1;
        }
        for (int k = 0; k < 2; k++) {
            int nx = x, ny = y;
            if (k == 0 && x != tx) {
                nx = x + Math.max(-sp, Math.min(sp, tx - x));
            } else if (k == 1 && y != ty) {
                ny = y + Math.max(-sp, Math.min(sp, ty - y));
            } else {
                continue;
            }
            if (diQuai(type, map, w, h, nx, ny)) {
                buocX = nx;
                buocY = ny;
                return true;
            }
        }
        return false;
    }

    static boolean cong(int[] type, int w, int x, int y) {
        return type[y * w + x] >= CONG;
    }

    /** Đoạn thẳng pixel (x0,y0) -> (x1,y1) không cắt ô tường / ô cổng (lấy mẫu mỗi 4 px). */
    public static boolean thongThoang(int[] type, int w, int h, int x0, int y0, int x1, int y1) {
        int dx = x1 - x0, dy = y1 - y0;
        int n = Math.max(Math.abs(dx), Math.abs(dy)) / 4 + 1;
        for (int i = 0; i <= n; i++) {
            int x = x0 + dx * i / n, y = y0 + dy * i / n;
            int tx = x >> 4, ty = y >> 4;
            if (tuong(type, w, h, tx, ty)) {
                return false;
            }
            if (i > 0 && cong(type, w, tx, ty)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Đường ngắn nhất (4 hướng) từ ô (sx, sy) tới ô (dx, dy); null nếu không có / quá xa (quaXa = true).
     * Đích là tường thì nhắm ô đi được gần đích nhất trong bán kính 2 ô.
     */
    public static short[] tim(int[] type, int w, int h, int sx, int sy, int dx, int dy) {
        quaXa = false;
        if (type == null || w <= 0 || h <= 0 || sx < 0 || sy < 0 || sx >= w || sy >= h) {
            return null;
        }
        if (dx < 0) {
            dx = 0;
        } else if (dx >= w) {
            dx = w - 1;
        }
        if (dy < 0) {
            dy = 0;
        } else if (dy >= h) {
            dy = h - 1;
        }
        if (tuong(type, w, h, dx, dy)) {
            int best = -1;
            for (int r = 1; r <= 2 && best < 0; r++) {
                for (int yy = dy - r; yy <= dy + r && best < 0; yy++) {
                    for (int xx = dx - r; xx <= dx + r; xx++) {
                        if (!tuong(type, w, h, xx, yy)) {
                            best = yy * w + xx;
                            break;
                        }
                    }
                }
            }
            if (best < 0) {
                return null;
            }
            dx = best % w;
            dy = best / w;
        }
        if (sx == dx && sy == dy) {
            return null;
        }
        int x0 = Math.max(0, Math.min(sx, dx) - LE), y0 = Math.max(0, Math.min(sy, dy) - LE);
        int x1 = Math.min(w - 1, Math.max(sx, dx) + LE), y1 = Math.min(h - 1, Math.max(sy, dy) + LE);
        int kw = x1 - x0 + 1, kh = y1 - y0 + 1;
        if (kw > TOI_DA || kh > TOI_DA) {
            quaXa = true;
            return null;
        }
        int n = kw * kh;
        if (truoc.length < n) {
            truoc = new int[n];
            hang = new int[n];
        }
        for (int i = 0; i < n; i++) {
            truoc[i] = -1;
        }
        soLanTim++;
        int s = (sy - y0) * kw + (sx - x0), d = (dy - y0) * kw + (dx - x0);
        truoc[s] = s;
        int dau = 0, cuoi = 0;
        hang[cuoi++] = s;
        while (dau < cuoi && truoc[d] < 0) {
            int c = hang[dau++];
            int cx = c % kw, cy = c / kw;
            for (int k = 0; k < 4; k++) {
                int nx = cx + (k == 0 ? 1 : (k == 1 ? -1 : 0));
                int ny = cy + (k == 2 ? 1 : (k == 3 ? -1 : 0));
                if (nx < 0 || ny < 0 || nx >= kw || ny >= kh) {
                    continue;
                }
                int ni = ny * kw + nx;
                if (truoc[ni] >= 0) {
                    continue;
                }
                int ax = nx + x0, ay = ny + y0;
                if (tuong(type, w, h, ax, ay) || (ni != d && cong(type, w, ax, ay))) {
                    continue;
                }
                truoc[ni] = c;
                hang[cuoi++] = ni;
            }
        }
        soODuyet += dau;
        if (truoc[d] < 0) {
            return null;
        }
        int len = 0;
        for (int c = d; c != s; c = truoc[c]) {
            len++;
        }
        short[] kq = new short[len];
        int i = 0;
        for (int c = d; c != s; c = truoc[c]) {
            kq[i++] = ma(c % kw + x0, c / kw + y0);
        }
        return kq;
    }
}
