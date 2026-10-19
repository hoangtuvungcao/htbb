package game.pokemon;

import game.model.Char;
import game.render.Canvas;
import game.render.screen.Font;
import game.render.screen.GameScr;
import java.util.Calendar;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Móc client cho hệ sinh thái server (HTBB docs/thiet-ke-game.md). Mọi thứ chỉ làm
 * đẹp; server không gửi gì mới thì client chạy y như cũ.
 *  - Ngày / đêm: lớp phủ xanh đêm theo giờ máy (cùng múi giờ server VN; 19:00–05:59 là đêm, chạng
 *    vạng 18h và rạng sáng 6h phủ nhạt). Ảnh phủ là ô 32x32 alpha lát kín màn (vài KB). Tắt ở bản NHẸ.
 *  - Thời tiết: gói -39 có sẵn (0 mưa, 1 lá, 2 tuyết) + id mới 6 = sương mù (phủ xám + dải sương trôi),
 *    7 = nắng gắt (phủ vàng + tia nắng chéo) - AnimateEffect gọi veSuongMu / veNangGat.
 *  - Câu cá: gõ "cauca" hoặc chọn "Câu cá" (menu NPC -528) -> cần câu + phao bay ra mép nước, phao
 *    nhấp nhô, cá cắn (phao chìm, bọt nước, dấu "!") rồi kéo lên; thông báo "Câu được..." (lệnh 37)
 *    giữ lại tới lúc cá cắn mới hiện.
 *  - Mốc đua / điểm đào: đọc chữ thông báo server ("Mốc: (x,y) → ...", "Qua mốc k/n", "Máy dò rung:
 *    ... hướng X, khoảng N bước") -> cờ đánh số / vòng dò trên bản đồ + chấm trên bản đồ nhỏ.
 *  - Danh hiệu sinh thái trên đầu: id hiệu ứng danh hiệu 9600 + chỉ số HoSoSinhThai.DANH_HIEU của
 *    server -> huy hiệu vẽ bằng code (không cần ảnh / gói tải hiệu ứng).
 */
public final class SinhThai {
    private SinhThai() {
    }

    // ================================================================== ngày / đêm

    /** Tắt hẳn (bản NHẸ không có ảnh núi banner = máy yếu). */
    private static int tatDem = -1;

    private static boolean demBat() {
        if (tatDem < 0) {
            tatDem = Banner.laBanNhe() ? 1 : 0;
        }
        return tatDem == 0;
    }

    /** Giờ (0..23) theo đồng hồ máy; gốc thử có thể đặt gioThu >= 0. */
    public static int gioThu = -1;

    public static int gio() {
        if (gioThu >= 0) {
            return gioThu;
        }
        try {
            return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        } catch (Throwable e) {
            return 12;
        }
    }

    public static boolean laDem() {
        int h = gio();
        return h >= 19 || h < 6;
    }

    /** Alpha (0..255) lớp phủ đêm theo giờ. */
    static int alphaDem(int h) {
        if (h == 18 || h == 6) {
            return 0x38;            // chạng vạng / rạng sáng
        }
        if (h >= 19 || h < 6) {
            return h == 19 || h == 5 ? 0x60 : 0x80;
        }
        return 0;
    }

    /** Phủ đêm cả màn (gọi sau bản đồ + nhân vật + thời tiết, trước HUD). */
    public static void veDem(Graphics g) {
        if (!demBat() || game.model.Tilemap.isOfflineMap) {
            return;
        }
        int a = alphaDem(gio());
        if (a <= 0) {
            return;
        }
        Canvas.resetTrans(g);
        phuLat(g, 0, 0, Canvas.w, Canvas.h, a << 24 | 0x0B1440);
        // đom đóm nhỏ ban đêm cho đỡ tối
        if (a >= 0x60) {
            int t = Canvas.gameTick;
            for (int i = 0; i < 8; i++) {
                int ph = (t + i * 17) % 60;
                if (ph > 36) {
                    continue;
                }
                int x = (i * 61 + t / 3 + (ph < 18 ? ph : 36 - ph) / 3) % Canvas.w;
                int y = 30 + (i * 43 + t / 7) % (Canvas.h - 70);
                g.setColor(ph > 10 && ph < 26 ? 0xFFF59A : 0xC8D860);
                g.fillRect(x, y, 2, 2);
            }
        }
    }

    private static final java.util.Hashtable oPhu = new java.util.Hashtable();

    /** Lát ô alpha 32x32 kín vùng (ít bộ nhớ hơn một ảnh alpha cỡ màn). */
    static void phuLat(Graphics g, int x, int y, int w, int h, int argb) {
        Integer k = new Integer(argb);
        Image o = (Image) oPhu.get(k);
        if (o == null) {
            try {
                if (oPhu.size() > 12) {
                    oPhu.clear();
                }
                int[] p = new int[32 * 32];
                for (int i = 0; i < p.length; i++) {
                    p[i] = argb;
                }
                o = Image.createRGBImage(p, 32, 32, true);
                oPhu.put(k, o);
            } catch (Throwable e) {
                return;
            }
        }
        int cx = g.getClipX(), cy = g.getClipY(), cw = g.getClipWidth(), ch = g.getClipHeight();
        g.clipRect(x, y, w, h);
        for (int yy = y; yy < y + h; yy += 32) {
            for (int xx = x; xx < x + w; xx += 32) {
                g.drawImage(o, xx, yy, Graphics.TOP | Graphics.LEFT);
            }
        }
        g.setClip(cx, cy, cw, ch);
    }

    // ================================================================== thời tiết 6/7

    public static final byte SUONG_MU = 6;
    public static final byte NANG_GAT = 7;

    /** Sương mù: phủ xám + 4 dải sương mờ trôi ngang. doDam 0..256 (mờ dần khi tắt). */
    public static void veSuongMu(Graphics g, int doDam) {
        if (doDam <= 0) {
            return;
        }
        Canvas.resetTrans(g);
        int a = 0x50 * doDam >> 8;
        phuLat(g, 0, 0, Canvas.w, Canvas.h, a << 24 | 0xC8D0DA);
        int t = Canvas.gameTick;
        int a2 = 0x40 * doDam >> 8;
        for (int i = 0; i < 4; i++) {
            int y = Canvas.h * (i * 2 + 1) / 9 + (i * 13);
            int w = Canvas.w * 3 / 4;
            int x = (i * 97 + t * (i + 1) / 6) % (Canvas.w + w) - w;
            phuLat(g, x, y, w, 10 + i * 3, a2 << 24 | 0xE6ECF2);
            phuLat(g, x + 12, y + 3, w - 24, 4 + i, a2 << 24 | 0xF4F8FC);
        }
    }

    /** Nắng gắt: phủ vàng ấm + tia nắng chéo từ góc trên-phải, chớp lóa nhẹ. */
    public static void veNangGat(Graphics g, int doDam) {
        if (doDam <= 0) {
            return;
        }
        Canvas.resetTrans(g);
        int t = Canvas.gameTick;
        int nhip = 18 + ((t % 80) < 40 ? (t % 40) / 4 : (40 - t % 40) / 4);
        int a = (0x20 + nhip / 2) * doDam >> 8;
        phuLat(g, 0, 0, Canvas.w, Canvas.h, a << 24 | 0xFFD050);
        int a2 = (0x30 + nhip) * doDam >> 8;
        for (int i = 0; i < 4; i++) {
            int x0 = Canvas.w - 10 - i * Canvas.w / 5;
            for (int k = 0; k < Canvas.h; k += 4) {
                phuLat(g, x0 - k / 2, k, 6 + i * 2, 4, a2 << 24 | 0xFFF2B0);
            }
        }
        // quầng mặt trời góc trên-phải
        g.setColor(0xFFF0A0);
        g.fillArc(Canvas.w - 14, -14, 28, 28, 0, 360);
    }

    // ================================================================== hồ câu cá (luật: chỉ câu ở HỒ)

    /** Ô nước theo bộ tile (= HTBB BanDoSinhThai.tileNuoc, từ bang_tile_moi.csv: tên W* / sen, kiểu chặn). */
    private static final short[] NUOC_T = {8, 9, 10, 11, 12, 58, 60, 61, 62, 64, 65, 66, 67, 68, 69, 72, 74, 75, 76, 78, 79, 80, 81, 82, 83};
    private static final short[] NUOC_HANG = {3, 7, 9, 10, 11, 13, 14, 15, 16, 17, 18};
    private static final short[] NUOC_THANH = {4, 22, 24, 25, 26, 28, 29, 30, 31, 32, 33, 50, 52, 53, 54, 56, 57, 58, 59, 60, 61};
    /** Hồ nhỏ hơn số ô này coi là vũng nước, không câu. */
    private static final int HO_TOI_THIEU = 6;

    private static short[] daTinh;            // Tilemap.map đã phân tích
    private static boolean[] laHo;            // [y*w+x] ô nước thuộc hồ (vùng nước kín, không chạm mép bản đồ)
    private static Vector diemCau = new Vector();   // int[]{x, y} ô bờ hồ (đứng câu) - mỗi hồ một điểm
    /** Danh sách điểm câu server gửi (khi có dữ liệu hồ của server): ghi đè phát hiện tại client. */
    private static int mapDiemServer = -1;

    private static short[] nuocCuaBo() {
        int lv = game.model.Tilemap.lv;
        return lv > 200 ? NUOC_THANH : (lv < 110 ? NUOC_T : NUOC_HANG);
    }

    /** = HTBB BanDoSinhThai: hồ không tới gần mép bản đồ quá HO_CACH_MEP ô, tối đa 40% diện tích. */
    private static final int HO_CACH_MEP = 2;
    private static final int HO_TOI_DA_PHAN_TRAM = 40;
    private static final int DIEM_MOI_HO = 4;
    /** = HTBB HeSinhThaiGame.MAP_KHONG_DONG: bản đồ không có hệ sinh thái (không câu). */
    public static final int[] MAP_KHONG_DONG = {40, 42};

    public static boolean mapKhongDong(int map) {
        for (int i = 0; i < MAP_KHONG_DONG.length; i++) {
            if (MAP_KHONG_DONG[i] == map) {
                return true;
            }
        }
        return false;
    }

    /**
     * Nhãn hồ [y*w+x] (-1 = không phải hồ) - bản sao ĐÚNG HTBB BanDoSinhThai.nhanHo (vùng nước 4 hướng,
     * không tới mép &lt;= HO_CACH_MEP ô, số ô trong [HO_TOI_THIEU, 40% diện tích]); nhãn đánh theo thứ tự quét.
     */
    public static int[] nhanHo(boolean[] nuoc, int w, int h) {
        int n = w * h;
        int[] nhan = new int[n];
        for (int i = 0; i < n; i++) {
            nhan[i] = -1;
        }
        byte[] da = new byte[n];
        int[] hang = new int[n];
        int toiDa = n * HO_TOI_DA_PHAN_TRAM / 100;
        int soHo = 0;
        for (int bd = 0; bd < n; bd++) {
            if (!nuoc[bd] || da[bd] != 0) {
                continue;
            }
            int dau = 0, cuoi = 0;
            hang[cuoi++] = bd;
            da[bd] = 1;
            boolean chamMep = false;
            while (dau < cuoi) {
                int i = hang[dau++];
                int x = i % w, y = i / w;
                if (x <= HO_CACH_MEP || y <= HO_CACH_MEP || x >= w - 1 - HO_CACH_MEP || y >= h - 1 - HO_CACH_MEP) {
                    chamMep = true;
                }
                if (x > 0 && nuoc[i - 1] && da[i - 1] == 0) { da[i - 1] = 1; hang[cuoi++] = i - 1; }
                if (x < w - 1 && nuoc[i + 1] && da[i + 1] == 0) { da[i + 1] = 1; hang[cuoi++] = i + 1; }
                if (y > 0 && nuoc[i - w] && da[i - w] == 0) { da[i - w] = 1; hang[cuoi++] = i - w; }
                if (y < h - 1 && nuoc[i + w] && da[i + w] == 0) { da[i + w] = 1; hang[cuoi++] = i + w; }
            }
            boolean laHo0 = !chamMep && cuoi >= HO_TOI_THIEU && cuoi <= toiDa;
            if (laHo0) {
                for (int k = 0; k < cuoi; k++) {
                    nhan[hang[k]] = soHo;
                }
                soHo++;
            }
        }
        return nhan;
    }

    /**
     * Điểm câu - bản sao ĐÚNG HTBB BanDoSinhThai.diemCau: ô ĐI ĐƯỢC kề hồ (4 hướng, quét hàng rồi cột),
     * mỗi hồ tối đa DIEM_MOI_HO điểm chọn trải đều. Mỗi điểm {hoId, x, y}.
     */
    public static Vector diemCau(int[] nhan, boolean[] diDuoc, int w, int h, int toiDa) {
        Vector tatCa = new Vector();
        int soHo = 0;
        for (int i = 0; i < nhan.length; i++) {
            if (nhan[i] + 1 > soHo) {
                soHo = nhan[i] + 1;
            }
        }
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int i = y * w + x;
                if (diDuoc == null || !diDuoc[i] || nhan[i] >= 0) {
                    continue;
                }
                int[] ke = {i - 1, i + 1, i - w, i + w};
                for (int q = 0; q < 4; q++) {
                    if (nhan[ke[q]] >= 0) {
                        tatCa.addElement(new int[]{nhan[ke[q]], x, y});
                        break;
                    }
                }
            }
        }
        Vector ra = new Vector();
        for (int ho = 0; ho < soHo; ho++) {
            Vector cua = new Vector();
            for (int k = 0; k < tatCa.size(); k++) {
                int[] d = (int[]) tatCa.elementAt(k);
                if (d[0] == ho) {
                    cua.addElement(d);
                }
            }
            int nn = toiDa < cua.size() ? toiDa : cua.size();
            for (int k = 0; k < nn; k++) {
                ra.addElement(cua.elementAt(k * cua.size() / nn));
            }
        }
        return ra;
    }

    /** Lưới nước của bản đồ theo bộ tile (lv: id nạp - như HTBB DinhDangBanDo.boTile). */
    public static boolean[] luoiNuoc(short[] m, int lv) {
        boolean[] laNuoc = new boolean[256];
        short[] ds = lv > 200 ? NUOC_THANH : (lv < 110 ? NUOC_T : NUOC_HANG);
        for (int i = 0; i < ds.length; i++) {
            laNuoc[ds[i]] = true;
        }
        boolean[] nuoc = new boolean[m.length];
        for (int i = 0; i < m.length; i++) {
            int t = m[i];
            nuoc[i] = t >= 0 && t < 254 && laNuoc[t];
        }
        return nuoc;
    }

    /** Lưới đi được (= HTBB BanDoSinhThai.luoiDiDuoc): ô trống đi được; còn lại theo bit chặn (2) của kiểu tile. */
    public static boolean[] luoiDiDuoc(short[] m, int[] kieu) {
        boolean[] r = new boolean[m.length];
        for (int i = 0; i < m.length; i++) {
            int t = m[i];
            r[i] = t >= 254 || t < 0 || kieu == null || (kieu[i] & game.model.Tilemap.T_BLOCK) == 0;
        }
        return r;
    }

    private static void phanTichHo() {
        short[] m = game.model.Tilemap.map;
        if (m == daTinh) {
            return;
        }
        daTinh = m;
        laHo = null;
        if (mapDiemServer != (Canvas.gameScr != null ? Canvas.gameScr.map : -2)) {
            diemCau = new Vector();
            mapDiemServer = -1;
        }
        if (m == null || Canvas.gameScr != null && mapKhongDong(Canvas.gameScr.map)) {
            return;
        }
        try {
            int w = game.model.Tilemap.w;
            int h = game.model.Tilemap.h;
            boolean[] nuoc = luoiNuoc(m, game.model.Tilemap.lv);
            int[] nhan = nhanHo(nuoc, w, h);
            boolean[] ho = new boolean[nhan.length];
            boolean co = false;
            for (int i = 0; i < nhan.length; i++) {
                ho[i] = nhan[i] >= 0;
                co |= ho[i];
            }
            if (!co) {
                return;
            }
            laHo = ho;
            if (mapDiemServer < 0) {
                Vector v = diemCau(nhan, luoiDiDuoc(m, game.model.Tilemap.type), w, h, 1);   // bản đồ nhỏ: 1 phao / hồ
                Vector xy = new Vector();
                for (int i = 0; i < v.size(); i++) {
                    int[] d = (int[]) v.elementAt(i);
                    xy.addElement(new int[]{d[1], d[2]});
                }
                diemCau = xy;
            }
        } catch (Throwable e) {
            laHo = null;
        }
    }

    /** Kiểm thử: đặt lưới hồ [y*w+x] cho bản đồ đang nạp (bỏ qua phân tích tile). */
    public static void datHoThu(boolean[] ho) {
        daTinh = game.model.Tilemap.map;
        laHo = ho;
    }

    /** Server gửi danh sách điểm câu của bản đồ (khi có dữ liệu hồ phía server): dùng thay phát hiện tại client. */
    public static void datDiemCau(int map, int[] xs, int[] ys) {
        Vector v = new Vector();
        for (int i = 0; xs != null && ys != null && i < xs.length && i < ys.length; i++) {
            v.addElement(new int[]{xs[i], ys[i]});
        }
        diemCau = v;
        mapDiemServer = map;
    }

    /** Người chơi đứng cách ô hồ tối đa 2 ô (cùng bán kính server BanDoSinhThai.ganNuoc). */
    public static boolean ganHo() {
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return false;
        }
        phanTichHo();
        int x = gs.mainChar.x / 16, y = gs.mainChar.y / 16;
        if (mapDiemServer >= 0 && mapDiemServer == gs.map) {
            for (int i = 0; i < diemCau.size(); i++) {
                int[] d = (int[]) diemCau.elementAt(i);
                if (Math.abs(d[0] - x) <= 2 && Math.abs(d[1] - y) <= 2) {
                    return true;
                }
            }
            return false;
        }
        boolean[] ho = laHo;
        if (ho == null) {
            return false;
        }
        int w = game.model.Tilemap.w, h = game.model.Tilemap.h;
        for (int yy = y - 2; yy <= y + 2; yy++) {
            for (int xx = x - 2; xx <= x + 2; xx++) {
                if (xx >= 0 && yy >= 0 && xx < w && yy < h && ho[yy * w + xx]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Bản đồ nhỏ: MỘT phao câu nhỏ (3x4 px, nền trong suốt) mỗi hồ. Trước 0.9.6.1: ô vuông tối 6x6 + nét cần cho 4
     * điểm / hồ -> bản đồ nhỏ đầy khối đen (báo lỗi chủ game). KiemPc kiểm không còn khối tối.
     */
    public static void veDiemCauMini(Graphics g) {
        phanTichHo();
        for (int i = 0; i < diemCau.size(); i++) {
            int[] d = (int[]) diemCau.elementAt(i);
            int x = d[0], y = d[1];
            g.setColor(0xE3350D);
            g.fillRect(x - 1, y - 1, 3, 1);               // nửa trên phao đỏ
            g.setColor(0xFFFFFF);
            g.fillRect(x - 1, y, 3, 2);                   // nửa dưới trắng
            g.setColor(0xC08040);
            g.fillRect(x, y - 2, 1, 1);                   // cán phao
        }
    }

    // ================================================================== câu cá
    //
    // 0.9.6.1 (server CauCaPhien): thả câu ("cauca2") -> phao bay ra Ô NƯỚC HỒ gần nhất (ưu tiên hướng đang nhìn,
    // không bao giờ rơi trên đất) -> chờ server báo "Cá cắn câu!" -> bấm nút câu / Enter = "keocan" trong khung ngắn
    // -> "Câu được..." / "Không câu được gì" / "...cá đã thoát mất". Server quyết định thời gian + vị trí (chống gian).

    private static long cauLuc = -1;          // ms bắt đầu (-1 = không câu)
    private static long canLuc = -1;          // ms cá cắn (-1 = chưa)
    private static long keoLuc = -1;          // ms bắt đầu kéo về (có kết quả "Câu được")
    private static boolean daKeo;             // đã gửi "keocan" lần này
    private static int cauDir;
    private static int cauX, cauY;            // tọa độ người câu lúc bắt đầu (px)
    private static int phaoX, phaoY;          // chỗ phao (px, tâm ô nước)
    private static String choBao;             // thông báo kết quả chờ hiện

    static final int MS_QUANG = 600;          // ném phao
    static final int MS_KEO = 500;            // kéo về
    static final int MS_HET_CHO = 30000;      // không có tin cá cắn: thôi
    static final int MS_HET_CAN = 6000;       // cá cắn mà không có kết quả: thôi
    /** Bán kính (ô) tìm mặt nước quanh người câu - = server BanDoSinhThai.hoGan(..., 2). */
    static final int TAM_CAU = 2;

    /** Ô nước hồ gần (x, y) (ô) trong TAM_CAU ô, ưu tiên hướng dir; null = không có. Thuần (test gọi được). */
    public static int[] oNuocGan(boolean[] ho, int w, int h, int x, int y, int dir) {
        if (ho == null) {
            return null;
        }
        int dx = dxHuong(dir), dy = dyHuong(dir);
        int[] tot = null;
        int diemTot = Integer.MAX_VALUE;
        for (int yy = y - TAM_CAU; yy <= y + TAM_CAU; yy++) {
            for (int xx = x - TAM_CAU; xx <= x + TAM_CAU; xx++) {
                if (xx < 0 || yy < 0 || xx >= w || yy >= h || !ho[yy * w + xx]) {
                    continue;
                }
                int ex = xx - x, ey = yy - y;
                int d = (ex < 0 ? -ex : ex) + (ey < 0 ? -ey : ey);
                // cùng hướng nhìn: ưu tiên (điểm thấp); ngược hướng: phạt
                int cung = ex * dx + ey * dy;
                int diem = d * 4 - (cung > 0 ? 6 : 0) + (cung < 0 ? 6 : 0);
                if (diem < diemTot) {
                    diemTot = diem;
                    tot = new int[]{xx, yy};
                }
            }
        }
        return tot;
    }

    /** Hướng nhìn về (ex, ey) (ô lệch). */
    static int huongVe(int ex, int ey, int cu) {
        if (ex == 0 && ey == 0) {
            return cu;
        }
        if ((ex < 0 ? -ex : ex) >= (ey < 0 ? -ey : ey)) {
            return ex < 0 ? Char.LEFT : Char.RIGHT;
        }
        return ey < 0 ? Char.UP : Char.DOWN;
    }

    /** Ô nước để thả phao ngay lúc này (null = không đứng gần mặt nước). */
    static int[] oThaPhao() {
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return null;
        }
        phanTichHo();
        return oNuocGan(laHo, game.model.Tilemap.w, game.model.Tilemap.h, gs.mainChar.x / 16, gs.mainChar.y / 16, gs.mainChar.dir);
    }

    /** Có chỗ thả phao xuống nước (hồ) trong tầm. */
    public static boolean coCho() {
        return ganHo() && oThaPhao() != null;
    }

    public static final String BAO_XA_NUOC = "Hãy đứng gần mặt nước để câu cá";

    /** Thả câu: true = được (hoạt cảnh đã chạy, người gọi gửi "cauca2"); false = báo ngắn, không câu. */
    public static boolean thuCau() {
        if (!coCho() || !batDauCau()) {
            HanhDong.bao(BAO_XA_NUOC);
            return false;
        }
        return true;
    }

    /** Bắt đầu hoạt cảnh thả phao xuống ô nước gần nhất. false = không có mặt nước trong tầm (không ném xuống đất). */
    public static boolean batDauCau() {
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return false;
        }
        int[] o = oThaPhao();
        if (o == null) {
            return false;
        }
        int x = gs.mainChar.x / 16, y = gs.mainChar.y / 16;
        cauDir = huongVe(o[0] - x, o[1] - y, gs.mainChar.dir);
        gs.mainChar.dir = (short) cauDir;      // quay mặt ra mặt nước
        phaoX = o[0] * 16 + 8;
        phaoY = o[1] * 16 + 10;
        cauLuc = System.currentTimeMillis();
        canLuc = -1;
        keoLuc = -1;
        daKeo = false;
        cauX = gs.mainChar.x;
        cauY = gs.mainChar.y;
        choBao = null;
        return true;
    }

    public static boolean dangCau() {
        return cauLuc >= 0;
    }

    /** Cá đang cắn câu (chờ người chơi kéo). */
    public static boolean dangCan() {
        return cauLuc >= 0 && canLuc >= 0 && keoLuc < 0;
    }

    /** Đã gửi lệnh kéo lần này (nút câu tạm ẩn chữ "Kéo"). */
    public static boolean daKeo() {
        return daKeo;
    }

    /** Bấm nút câu / Enter khi đang câu: gửi "keocan" (một lần). true = đã gửi. */
    public static boolean keoCan() {
        if (cauLuc < 0 || daKeo || keoLuc >= 0) {
            return false;
        }
        daKeo = true;
        return true;
    }

    private static void ketThucCau() {
        cauLuc = -1;
        canLuc = -1;
        keoLuc = -1;
        daKeo = false;
        if (choBao != null) {
            String s = choBao;
            choBao = null;
            Canvas.startOKDlg(s);
        }
    }

    /** Thu cần ngay (không có kết quả). */
    private static void thuCan() {
        cauLuc = -1;
        canLuc = -1;
        keoLuc = -1;
        daKeo = false;
        choBao = null;
    }

    /**
     * Lệnh 37 (hộp thông báo): true = client giữ lại (hiện sau / thay bằng hoạt cảnh). Đồng thời đọc mốc đua / điểm đào.
     */
    public static boolean chanThongBao(String s) {
        if (s == null) {
            return false;
        }
        docMoc(s);
        long now = System.currentTimeMillis();
        if (s.startsWith("Cá cắn câu")) {
            if (cauLuc < 0 && !batDauCau()) {
                return false;
            }
            if (canLuc < 0) {
                canLuc = now > cauLuc + MS_QUANG ? now : cauLuc + MS_QUANG;
            }
            HanhDong.bao(s);           // dấu cá cắn: chữ nhanh + phao chìm, không hộp thoại chặn tay
            return true;
        }
        if (s.startsWith("Câu được")) {
            if (cauLuc < 0) {
                batDauCau();
            }
            choBao = s;
            if (cauLuc >= 0) {
                if (canLuc < 0) {
                    // server cũ (không báo cá cắn): diễn cá cắn ngắn rồi kéo
                    canLuc = now > cauLuc + MS_QUANG ? now : cauLuc + MS_QUANG;
                    keoLuc = canLuc + 900;
                } else {
                    keoLuc = now;
                }
                return true;
            }
            choBao = null;
            return false;
        }
        if (cauLuc >= 0 && (s.startsWith("Không câu được") || s.indexOf("thoát mất") >= 0 || s.startsWith("Chỉ có thể câu")
                || s.indexOf("thả câu tiếp") >= 0 || s.indexOf("mép nước") >= 0 || s.indexOf("thể lực") >= 0
                || s.indexOf("mồi câu") >= 0 || s.indexOf("tạm thời") >= 0)) {
            thuCan();            // câu không được / cá thoát: thu cần ngay, thông báo hiện như thường
        }
        return false;
    }

    /** Thông báo chữ chạy (-26): chỉ đọc mốc (kết quả đua, huỷ đua). */
    public static void docThongBaoChay(String s) {
        if (s != null) {
            docMoc(s);
        }
    }

    private static int dxHuong(int dir) {
        return dir == Char.LEFT ? -1 : (dir == Char.RIGHT ? 1 : 0);
    }

    private static int dyHuong(int dir) {
        return dir == Char.UP ? -1 : (dir == Char.DOWN ? 1 : 0);
    }

    private static void veCau(Graphics g) {
        if (cauLuc < 0) {
            return;
        }
        GameScr gs = Canvas.gameScr;
        long now = System.currentTimeMillis();
        long t = now - cauLuc;
        if (canLuc < 0 && t > MS_HET_CHO || canLuc >= 0 && keoLuc < 0 && now - canLuc > MS_HET_CAN) {
            thuCan();
            return;
        }
        // di chuyển xa chỗ câu: thu cần
        if (Math.abs(gs.mainChar.x - cauX) > 24 || Math.abs(gs.mainChar.y - cauY) > 24) {
            ketThucCau();
            return;
        }
        int dx = dxHuong(cauDir);
        int xNgon = gs.mainChar.x + (dx != 0 ? dx * 10 : (phaoX >= gs.mainChar.x ? 8 : -8));
        int yNgon = gs.mainChar.y - 26;
        int xPhao = phaoX;
        int yPhao = phaoY;
        int x = xPhao;
        int y = yPhao;
        int pha;                     // 0 ném, 1 nổi, 2 cắn, 3 kéo
        long tc = canLuc < 0 || now < canLuc ? -1 : now - canLuc;
        if (t < MS_QUANG) {
            pha = 0;
            int tt = (int) t;
            x = xNgon + (xPhao - xNgon) * tt / MS_QUANG;
            long cung = (long) tt * (MS_QUANG - tt);
            y = yNgon + (yPhao - yNgon) * tt / MS_QUANG - (int) (cung * 18 / ((long) MS_QUANG * MS_QUANG / 4));
        } else if (keoLuc >= 0 && now >= keoLuc) {
            long k = now - keoLuc;
            if (k >= MS_KEO) {
                ketThucCau();
                return;
            }
            pha = 3;
            x = xPhao + (int) ((xNgon - xPhao) * k / MS_KEO);
            y = yPhao + (int) ((yNgon - yPhao) * k / MS_KEO);
        } else if (tc < 0) {
            pha = 1;
            y += (now / 300) % 2;
        } else {
            pha = 2;
            y += tc < 400 ? ((tc / 100) % 2 == 0 ? 3 : 1) : 2;
            x += (now / 50) % 2;
        }
        // cần câu
        int xGoc = gs.mainChar.x + (dx != 0 ? dx * 2 : (xNgon > gs.mainChar.x ? 3 : -3));
        g.setColor(0x7A4A22);
        g.drawLine(xGoc, gs.mainChar.y - 14, xNgon, yNgon);
        g.setColor(0x9C6430);
        g.drawLine(xGoc + 1, gs.mainChar.y - 14, xNgon + 1, yNgon);
        // dây
        g.setColor(0xE8EEF4);
        g.drawLine(xNgon, yNgon, x, y);
        if (pha >= 1 && pha <= 2) {
            // gợn nước quanh phao
            int r = 3 + (int) ((now / 120) % 8);
            g.setColor(pha == 2 ? 0xFFFFFF : 0xBFE4FF);
            g.drawArc(x - r, y - r / 3 + 2, r * 2, r * 2 / 3 + 1, 0, 360);
            if (pha == 2) {
                // bọt nước bắn
                for (int i = 0; i < 5; i++) {
                    int bx = x - 6 + i * 3;
                    int by = y - 2 - (int) ((tc / 50 * (2 + i % 3)) % 9);
                    g.setColor(0xDFF3FF);
                    g.fillRect(bx, by, 2, 2);
                }
            }
        }
        // phao đỏ trắng
        g.setColor(0x1E2A44);
        g.fillArc(x - 3, y - 4, 7, 7, 0, 360);
        g.setColor(0xFFFFFF);
        g.fillArc(x - 2, y - 3, 5, 5, 0, 360);
        g.setColor(0xE3350D);
        g.fillArc(x - 2, y - 3, 5, 3, 0, 180);
        if (pha == 2 && !daKeo) {
            // dấu "!" trên đầu
            int yy = gs.mainChar.y - gs.mainChar.height - 22;
            GiaoDien.khoi(g, gs.mainChar.x - 5, yy, 11, 14, 4, GiaoDien.VIEN);
            GiaoDien.khoi(g, gs.mainChar.x - 4, yy + 1, 9, 12, 3, GiaoDien.VANG);
            g.setColor(GiaoDien.DO);
            g.fillRect(gs.mainChar.x - 1, yy + 3, 3, 5);
            g.fillRect(gs.mainChar.x - 1, yy + 9, 3, 2);
        }
    }

    // ================================================================== mốc đua / điểm đào

    /** Bản đồ đua Pokémon (HeSinhThaiGame.Dua.MAP_DUA, tuyến Goldenrod). */
    public static final int MAP_DUA = 18;
    private static int[] mocX, mocY;
    private static int mocKe;                 // mốc kế tiếp (0..)
    private static int daoMap = -1, daoX, daoY;
    private static long daoLuc;
    private static boolean daoUocLuong;

    private static int so(String s, int[] i) {
        int n = 0;
        boolean co = false;
        while (i[0] < s.length() && s.charAt(i[0]) >= '0' && s.charAt(i[0]) <= '9') {
            n = n * 10 + (s.charAt(i[0]) - '0');
            i[0]++;
            co = true;
        }
        return co ? n : -1;
    }

    /** Mọi cặp "(a,b)" trong chuỗi từ vị trí tu. */
    private static void docCap(String s, int tu) {
        Vector v = new Vector();
        int[] i = {tu};
        while (i[0] < s.length()) {
            int p = s.indexOf('(', i[0]);
            if (p < 0) {
                break;
            }
            i[0] = p + 1;
            int a = so(s, i);
            if (a < 0 || i[0] >= s.length() || s.charAt(i[0]) != ',') {
                continue;
            }
            i[0]++;
            int b = so(s, i);
            if (b < 0 || i[0] >= s.length() || s.charAt(i[0]) != ')') {
                continue;
            }
            v.addElement(new int[]{a, b});
        }
        if (v.size() > 0) {
            mocX = new int[v.size()];
            mocY = new int[v.size()];
            for (int k = 0; k < v.size(); k++) {
                int[] c = (int[]) v.elementAt(k);
                mocX[k] = c[0];
                mocY[k] = c[1];
            }
            mocKe = 0;
        }
    }

    private static void docMoc(String s) {
        try {
            int p = s.indexOf("Mốc:");
            if (p < 0) {
                p = s.indexOf("mốc:");          // server mới: "Xuất phát! Các mốc: ..."
            }
            if (p < 0) {
                p = s.indexOf("(toạ độ ô):");
            }
            if (p >= 0 && (s.indexOf("XUẤT PHÁT") >= 0 || s.indexOf("Xuất phát") >= 0 || s.indexOf("đăng ký đua") >= 0)) {
                docCap(s, p);
                return;
            }
            p = s.indexOf("Qua mốc ");
            if (p >= 0 && mocX != null) {
                int[] i = {p + 8};
                int k = so(s, i);
                if (k >= 0) {
                    mocKe = k;
                }
                return;
            }
            if (s.indexOf("Về đích") >= 0 || s.indexOf("Kết quả Đua") >= 0 || s.indexOf("hôm nay huỷ") >= 0) {
                mocX = null;
                mocY = null;
                return;
            }
            p = s.indexOf("Máy dò rung");
            if (p >= 0) {
                GameScr gs = Canvas.gameScr;
                int tx = gs.mainChar.x / 16;
                int ty = gs.mainChar.y / 16;
                int q = s.indexOf("khoảng ", p);
                int n = 0;
                if (q >= 0) {
                    int[] i = {q + 7};
                    n = so(s, i);
                }
                int ex = s.indexOf("Đông", p) >= 0 ? 1 : (s.indexOf("Tây", p) >= 0 ? -1 : 0);
                int ey = s.indexOf("Nam", p) >= 0 ? 1 : (s.indexOf("Bắc", p) >= 0 ? -1 : 0);
                if (n < 0) {
                    n = 0;
                }
                if (ex != 0 && ey != 0) {
                    tx += ex * (n / 2);
                    ty += ey * (n - n / 2);
                } else {
                    tx += ex * n;
                    ty += ey * n;
                }
                daoMap = gs.map;
                daoX = tx;
                daoY = ty;
                daoUocLuong = n > 0;
                daoLuc = System.currentTimeMillis();
                return;
            }
            if (s.indexOf("Đào được") >= 0 || s.indexOf("đào hết") >= 0) {
                daoMap = -1;
            }
        } catch (Throwable e) {
            // chữ lạ: bỏ qua
        }
    }

    private static void co(Graphics g, int x, int y, int so, boolean ke, boolean qua) {
        // cột cờ + lá cờ tam giác đánh số
        g.setColor(GiaoDien.VIEN);
        g.fillRect(x, y - 22, 2, 22);
        int mau = qua ? 0x8A93A3 : (ke ? GiaoDien.VANG : GiaoDien.DO);
        g.setColor(GiaoDien.VIEN);
        g.fillTriangle(x + 2, y - 23, x + 2, y - 11, x + 15, y - 17);
        g.setColor(mau);
        g.fillTriangle(x + 2, y - 21, x + 2, y - 13, x + 12, y - 17);
        Font.smallFont.drawString(g, String.valueOf(so), x + 5, y - 20, Font.LEFT);
        if (ke) {
            int r = 6 + (Canvas.gameTick % 16) / 2;
            g.setColor(GiaoDien.VANG);
            g.drawArc(x - r, y - r / 2, r * 2, r, 0, 360);
        }
    }

    private static void veMoc(Graphics g) {
        GameScr gs = Canvas.gameScr;
        if (mocX != null && gs.map == MAP_DUA) {
            for (int i = 0; i < mocX.length; i++) {
                co(g, mocX[i] * 16 + 8, mocY[i] * 16 + 14, i + 1, i == mocKe, i < mocKe);
            }
        }
        if (daoMap >= 0 && daoMap == gs.map) {
            if (System.currentTimeMillis() - daoLuc > 180000L) {
                daoMap = -1;          // gợi ý cũ (3 phút): bỏ
                return;
            }
            int x = daoX * 16 + 8;
            int y = daoY * 16 + 8;
            int r = 10 + (Canvas.gameTick % 20) / 2;
            g.setColor(0x7A4A22);
            g.drawArc(x - r, y - r / 2, r * 2, r, 0, 360);
            g.setColor(GiaoDien.VANG);
            g.drawArc(x - 8, y - 4, 16, 8, 0, 360);
            // dấu X (xẻng) hoặc "?" nếu chỉ ước lượng
            g.setColor(GiaoDien.VIEN);
            g.drawLine(x - 4, y - 2, x + 4, y + 2);
            g.drawLine(x - 4, y + 2, x + 4, y - 2);
            if (daoUocLuong) {
                Font.smallFont.drawString(g, "?", x + 7, y - 12, Font.LEFT);
            }
        }
    }

    /** Mũi tên mép màn chỉ về mốc kế / điểm đào nằm ngoài màn (vẽ hệ tọa độ màn). */
    private static void veMuiTen(Graphics g) {
        GameScr gs = Canvas.gameScr;
        int tx = -1, ty = -1;
        if (mocX != null && gs.map == MAP_DUA && mocKe < mocX.length) {
            tx = mocX[mocKe] * 16 + 8;
            ty = mocY[mocKe] * 16 + 8;
        } else if (daoMap >= 0 && daoMap == gs.map) {
            tx = daoX * 16 + 8;
            ty = daoY * 16 + 8;
        }
        if (tx < 0) {
            return;
        }
        int sx = tx - GameScr.cmx;
        int sy = ty - GameScr.cmy;
        if (sx >= 8 && sx < Canvas.w - 8 && sy >= 30 && sy < Canvas.h - 62) {   // dưới: thanh skill che
            return;
        }
        int cx = Canvas.hw;
        int cy = Canvas.hh;
        int dx = sx - cx;
        int dy = sy - cy;
        int ax = dx < 0 ? -dx : dx;
        int ay = dy < 0 ? -dy : dy;
        int mx = Canvas.hw - 14;
        int my = Canvas.hh - 62;
        int px, py;
        if (ax * my > ay * mx) {
            px = cx + (dx > 0 ? mx : -mx);
            py = cy + dy * mx / (ax == 0 ? 1 : ax);
        } else {
            py = cy + (dy > 0 ? my : -my);
            px = cx + dx * my / (ay == 0 ? 1 : ay);
        }
        Canvas.resetTrans(g);
        GiaoDien.khoi(g, px - 7, py - 7, 14, 14, 7, GiaoDien.VIEN);
        GiaoDien.khoi(g, px - 6, py - 6, 12, 12, 6, mocX != null && gs.map == MAP_DUA ? GiaoDien.VANG : 0xC08040);
        g.setColor(GiaoDien.VIEN);
        int ux = dx == 0 ? 0 : (dx > 0 ? 1 : -1);
        int uy = dy == 0 ? 0 : (dy > 0 ? 1 : -1);
        if (ax > ay * 2) {
            uy = 0;
        } else if (ay > ax * 2) {
            ux = 0;
        }
        g.fillTriangle(px + ux * 5, py + uy * 5, px - uy * 4 - ux * 2, py + ux * 4 - uy * 2, px + uy * 4 - ux * 2, py - ux * 4 - uy * 2);
    }

    /** Bản đồ nhỏ (gốc tọa độ = ô, 1 px / ô): chấm mốc đua / điểm đào. */
    public static void veMini(Graphics g) {
        GameScr gs = Canvas.gameScr;
        if (gs == null) {
            return;
        }
        try {
            veDiemCauMini(g);
        } catch (Throwable e) {
            // không làm hỏng bản đồ nhỏ
        }
        if (mocX != null && gs.map == MAP_DUA) {
            for (int i = 0; i < mocX.length; i++) {
                g.setColor(GiaoDien.VIEN);
                g.fillRect(mocX[i] - 2, mocY[i] - 2, 5, 5);
                g.setColor(i == mocKe ? GiaoDien.VANG : (i < mocKe ? 0x8A93A3 : GiaoDien.DO));
                g.fillRect(mocX[i] - 1, mocY[i] - 1, 3, 3);
            }
        }
        if (daoMap >= 0 && daoMap == gs.map && Canvas.gameTick % 10 < 7) {
            g.setColor(0x7A4A22);
            g.fillRect(daoX - 2, daoY - 2, 5, 5);
            g.setColor(GiaoDien.VANG);
            g.fillRect(daoX - 1, daoY - 1, 3, 3);
        }
    }

    /** Lớp thế giới (sau nhân vật, trước thời tiết): phao câu, cờ mốc, điểm đào. */
    public static void veTheGioi(Graphics g) {
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return;
        }
        try {
            Canvas.resetTrans(g);
            g.translate(-GameScr.cmx, -GameScr.cmy);
            veMoc(g);
            veCau(g);
            veMuiTen(g);
        } catch (Throwable e) {
            // không làm hỏng khung hình game
        }
        Canvas.resetTrans(g);
    }

    // ================================================================== danh hiệu sinh thái

    public static final int DANH_HIEU_TU = 9600;
    /** = HTBB HoSoSinhThai.DANH_HIEU (thứ tự cố định). */
    public static final String[] DANH_HIEU = {
        "Nhiếp ảnh tập sự", "Thợ săn khoảnh khắc", "Nhà sinh thái học", "Huyền thoại ống kính",
        "Ngư phủ bậc thầy", "Nhà khảo cổ", "Đầu bếp Poké", "Dược sư", "Nhà vườn xanh",
        "Nhà nhân giống", "Thợ săn shiny", "Vua tốc độ", "Thánh đố Pokémon", "Nhà vô địch giải tuần",
        "Nhà thám hiểm vùng", "Bạn của muôn loài",
    };
    private static final byte[] HINH_DH = {
        KieuO.HINH_SAO, KieuO.HINH_SAO, KieuO.HINH_LA, KieuO.HINH_SAO,
        KieuO.HINH_GIOT, KieuO.HINH_THEP, KieuO.HINH_LUA, KieuO.HINH_CONG, KieuO.HINH_LA,
        KieuO.HINH_TIM, KieuO.HINH_SAO, KieuO.HINH_NHANH, KieuO.HINH_SET, KieuO.HINH_KHIEN,
        KieuO.HINH_LOC, KieuO.HINH_TIM,
    };
    private static final byte[] HE_DH = {0, 0, 4, 10, 2, 8, 1, 7, 4, 17, 3, 9, 10, 6, 14, 17};

    public static boolean laDanhHieu(int id) {
        return id >= DANH_HIEU_TU && id < DANH_HIEU_TU + DANH_HIEU.length;
    }

    /** Danh hiệu sinh thái của nhân vật (chỉ số) hoặc -1. */
    public static int danhHieuCua(short[] ds) {
        if (ds == null) {
            return -1;
        }
        for (int i = 0; i < ds.length; i++) {
            if (laDanhHieu(ds[i])) {
                return ds[i] - DANH_HIEU_TU;
            }
        }
        return -1;
    }

    /** Huy hiệu danh hiệu, tâm ngang x, đáy yDay. */
    public static void veDanhHieu(Graphics g, int x, int yDay, int i) {
        if (i < 0 || i >= DANH_HIEU.length) {
            return;
        }
        Font f = Font.arialFontW;
        String s = DANH_HIEU[i];
        int w = f.getWidth(s) + 22;
        int h = 15;
        int x0 = x - w / 2;
        int y0 = yDay - h;
        int mau = KieuO.mauHe(HE_DH[i]);
        GiaoDien.khoi(g, x0, y0, w, h, 7, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x0 + 1, y0 + 1, w - 2, h - 2, 6, GiaoDien.pha(mau, 0x000000, 1, 3), GiaoDien.pha(mau, 0x000000, 3, 5));
        g.setColor(GiaoDien.pha(mau, 0xFFFFFF, 1, 2));
        g.fillRect(x0 + 6, y0 + 2, w - 12, 1);
        Image a = KieuO.anhHinh(HINH_DH[i], 1);
        if (a != null) {
            g.drawImage(a, x0 + 9, y0 + h / 2, Graphics.HCENTER | Graphics.VCENTER);
        }
        f.drawString(g, s, x0 + 17, y0 + 1, Font.LEFT);
    }
}
