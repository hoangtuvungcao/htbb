package game.pokemon;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * TRANG PHỤC / HÀO QUANG / SKIN THÚ CƯỠI / THẺ BẠN ĐỒNG HÀNH (0.9.8, docs/thiet-ke-game.md "Trang phục"): vẽ lớp phủ
 * pixel từ atlas res/pk/trang_phuc.png + chỉ mục trang_phuc.bin (sinh bởi tools/do_trang_phuc/sinh.py, tự vẽ, CC0).
 *
 * Đặt mảnh như phụ kiện trang bị (NgoaiHinhVe.veO): điểm neo PMD từng khung (PokeBody.neo), lớp sau / trước theo hướng,
 * lật gương khi nhìn phải, loài to vẽ gấp đôi (NgoaiHinhVe.heSo), mảnh quá to so với thân thì thu về cỡ 1 rồi bỏ. Loài cấm
 * ô (bảng ngoại lệ phukien.bin: sâu, cá, cục tròn...) thì bỏ mảnh ô đó. Jar J2ME không có điểm neo: chỉ vẽ vòng hào quang.
 * Thiếu tệp atlas: không vẽ gì (an toàn).
 */
public final class TrangPhucVe {
    private TrangPhucVe() {
    }

    /** Một mảnh (một hướng). */
    public static final class Manh {
        public int id, o, huong, lop, neo, dx, dy, ax, ay, x, y, w, h;
        int[] px;
        final Image[] anh = new Image[8];   // (lat, k-1) k = 1..4
        /** hộp điểm đặc trong ảnh mảnh (tính khi đọc điểm ảnh) */
        public int bx0, by0, bx1, by1;
    }

    /** Hộp điểm đặc {x0, y0, x1, y1} của mảnh (trong ảnh chưa phóng, chưa lật). */
    public static int[] hop(Manh m) {
        pixel(m);
        return new int[]{m.bx0, m.by0, m.bx1, m.by1};
    }

    private static boolean daNap, co;
    private static Image atlas;
    /** MANH[id * 3 + huong]. */
    private static final Manh[] MANH = new Manh[256 * 3];
    /** trang phục so -> id mảnh. */
    private static final int[][] TP = new int[256][];
    /** hào quang so -> 4 khung {x, y, w, h} + màu. */
    private static final int[][][] HQ = new int[256][][];
    private static final int[] HQ_MAU = new int[256];
    private static final Image[][] HQ_ANH = new Image[256][];
    /** skin thú cưỡi so -> {xoay, bão hoà, sáng, phủ màu, phủ %}. */
    private static final int[][] SKIN = new int[256][];
    /** thẻ bạn đồng hành so -> {id mảnh, màu tên}. */
    private static final int[][] THE = new int[256][];

    /** Nạp một lần; false = không có tệp (jar cũ). */
    public static boolean nap() {
        if (daNap) {
            return co;
        }
        daNap = true;
        try {
            byte[] b = PokeBody.docTaiNguyen(PokeBody.THU_MUC + "trang_phuc.bin");
            byte[] a = PokeBody.docTaiNguyen(PokeBody.THU_MUC + "trang_phuc.png");
            if (b == null || a == null) {
                return co = false;
            }
            atlas = Image.createImage(a, 0, a.length);
            int AW = atlas.getWidth(), AH = atlas.getHeight();
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(b));
            if (r.readUnsignedByte() != 'T' || r.readUnsignedByte() != 1) {
                return co = false;
            }
            int n = r.readUnsignedShort();
            for (int i = 0; i < n; i++) {
                Manh m = new Manh();
                m.id = r.readUnsignedByte();
                m.o = r.readUnsignedByte();
                m.huong = r.readUnsignedByte();
                m.lop = r.readUnsignedByte();
                m.neo = r.readUnsignedByte();
                m.dx = r.readByte();
                m.dy = r.readByte();
                m.ax = r.readByte();
                m.ay = r.readByte();
                m.x = r.readUnsignedShort();
                m.y = r.readUnsignedShort();
                m.w = r.readUnsignedByte();
                m.h = r.readUnsignedByte();
                if (m.huong < 3 && m.x + m.w <= AW && m.y + m.h <= AH) {
                    MANH[m.id * 3 + m.huong] = m;
                }
            }
            n = r.readUnsignedByte();
            for (int i = 0; i < n; i++) {
                int so = r.readUnsignedByte();
                int k = r.readUnsignedByte();
                int[] ds = new int[k];
                for (int j = 0; j < k; j++) {
                    ds[j] = r.readUnsignedByte();
                }
                TP[so] = ds;
            }
            n = r.readUnsignedByte();
            for (int i = 0; i < n; i++) {
                int so = r.readUnsignedByte();
                int k = r.readUnsignedByte();
                int[][] kh = new int[k][4];
                for (int j = 0; j < k; j++) {
                    kh[j][0] = r.readUnsignedShort();
                    kh[j][1] = r.readUnsignedShort();
                    kh[j][2] = r.readUnsignedByte();
                    kh[j][3] = r.readUnsignedByte();
                }
                HQ[so] = kh;
                HQ_MAU[so] = rgb(r);
                rgb(r);
            }
            n = r.readUnsignedByte();
            for (int i = 0; i < n; i++) {
                int so = r.readUnsignedByte();
                SKIN[so] = new int[]{r.readShort(), r.readUnsignedByte(), r.readUnsignedByte(), rgb(r), r.readUnsignedByte()};
            }
            n = r.readUnsignedByte();
            for (int i = 0; i < n; i++) {
                int so = r.readUnsignedByte();
                THE[so] = new int[]{r.readUnsignedByte(), rgb(r)};
            }
            co = true;
        } catch (Throwable e) {
            co = false;
        }
        return co;
    }

    private static int rgb(DataInputStream r) throws java.io.IOException {
        return (r.readUnsignedByte() << 16) | (r.readUnsignedByte() << 8) | r.readUnsignedByte();
    }

    // ------------------------------------------------------------------ tra cứu (test + giao diện)

    public static Manh manh(int id, int huong) {
        return nap() && id >= 0 && id < 256 && huong >= 0 && huong < 3 ? MANH[id * 3 + huong] : null;
    }

    /** Các mảnh của trang phục so (null nếu không có). */
    public static int[] manhCua(int so) {
        return nap() && so > 0 && so < 256 ? TP[so] : null;
    }

    public static boolean coHaoQuang(int so) {
        return nap() && so > 0 && so < 256 && HQ[so] != null;
    }

    public static int[] skin(int so) {
        return nap() && so > 0 && so < 256 ? SKIN[so] : null;
    }

    public static int[] the(int so) {
        return nap() && so > 0 && so < 256 ? THE[so] : null;
    }

    public static int rongAtlas() {
        return nap() ? atlas.getWidth() : 0;
    }

    public static int caoAtlas() {
        return nap() ? atlas.getHeight() : 0;
    }

    /** Khung hào quang {x, y, w, h} (test). */
    public static int[][] khungHaoQuang(int so) {
        return nap() && so > 0 && so < 256 ? HQ[so] : null;
    }

    /** Số trang phục có trong atlas (danh sách so). */
    public static int soTrangPhuc() {
        int d = 0;
        for (int i = 1; nap() && i < 256; i++) {
            if (TP[i] != null) {
                d++;
            }
        }
        return d;
    }

    /** Màu nhãn tên bạn đồng hành của thẻ so (-1 = mặc định). */
    public static int mauTenBan(int so) {
        int[] t = the(so);
        return t == null ? -1 : t[1];
    }

    /** Mã trang phục PokeBody cho thú cưỡi: 100 + so khi skin có đổi màu (0 = không đổi). */
    public static int tpThu(NgoaiHinhCua n) {
        if (n == null || n.skinThu <= 0) {
            return 0;
        }
        int[] s = skin(n.skinThu);
        if (s == null || (s[0] == 0 && s[1] == 100 && s[2] == 100 && s[4] == 0)) {
            return 0;
        }
        return 100 + n.skinThu;
    }

    // ------------------------------------------------------------------ ảnh

    static int[] pixel(Manh m) {
        if (m.px == null) {
            int[] p = new int[m.w * m.h];
            atlas.getRGB(p, 0, m.w, m.x, m.y, m.w, m.h);
            int x0 = m.w, y0 = m.h, x1 = -1, y1 = -1;
            for (int y = 0; y < m.h; y++) {
                for (int x = 0; x < m.w; x++) {
                    if ((p[y * m.w + x] >>> 24) >= 128) {
                        x0 = Math.min(x0, x);
                        y0 = Math.min(y0, y);
                        x1 = Math.max(x1, x);
                        y1 = Math.max(y1, y);
                    }
                }
            }
            m.bx0 = x1 < 0 ? 0 : x0;
            m.by0 = y1 < 0 ? 0 : y0;
            m.bx1 = x1 < 0 ? m.w - 1 : x1;
            m.by1 = y1 < 0 ? m.h - 1 : y1;
            m.px = p;
        }
        return m.px;
    }

    /** Ảnh mảnh (lật gương, phóng k nguyên 1..4), nhớ sẵn. */
    public static Image anh(Manh m, boolean lat, int k) {
        if (k < 1) {
            k = 1;
        }
        if (k > 4) {
            k = 4;
        }
        int i = (lat ? 4 : 0) + k - 1;
        if (m.anh[i] != null) {
            return m.anh[i];
        }
        int[] p = pixel(m);
        int W = m.w * k, H = m.h * k;
        int[] q = new int[W * H];
        for (int y = 0; y < H; y++) {
            int sy = y / k;
            for (int x = 0; x < W; x++) {
                int sx = x / k;
                q[y * W + x] = p[sy * m.w + (lat ? m.w - 1 - sx : sx)];
            }
        }
        try {
            m.anh[i] = Image.createRGBImage(q, W, H, true);
        } catch (Throwable e) {
            return null;
        }
        return m.anh[i];
    }

    // ------------------------------------------------------------------ vẽ

    /** Có trang phục đang hiện (không bị ẩn) không. */
    public static boolean coTrangPhuc(NgoaiHinhCua n) {
        return n != null && n.tpSo > 0 && (n.an & NgoaiHinhCua.AN_TRANG_PHUC) == 0 && manhCua(n.tpSo) != null;
    }

    /** Giới hạn bề ngang mảnh theo ô (phần trăm bề ngang thân). */
    static int toiDaPhanTram(int o) {
        switch (o) {
            case NgoaiHinhCua.LUNG:
                return 100;
            case NgoaiHinhCua.MU:
                return 75;
            case NgoaiHinhCua.TAY:
                return 70;
            default:
                return 50;
        }
    }

    private static final int[] THU_TU = {NgoaiHinhCua.LUNG, NgoaiHinhCua.KHAN, NgoaiHinhCua.TAY, NgoaiHinhCua.MU};
    private static final int[] NEO = new int[8];
    /** Hộp các mảnh vừa vẽ (test): {x, y, w, h, o, lop, idManh} * n. */
    public static int[] hopVe = new int[64 * 7];
    public static int soHopVe;
    public static boolean ghiHop;

    /** Vẽ các mảnh trang phục thuộc lớp {@code lop}. false = không vẽ được (thiếu neo / không có). */
    public static boolean veLop(Graphics g, NgoaiHinhCua n, int dex, boolean shiny, int x, int y, int dir, int dongTac,
            int tick, int lop) {
        if (!coTrangPhuc(n) || !PokeBody.neo(dex, shiny, dir, dongTac, tick, 100, NEO)
                || !PokeBody.neoTrangPhuc(dex, shiny, dir, dongTac, tick, 100, NTP)) {
            return false;
        }
        int[] ds = manhCua(n.tpSo);
        // thứ tự trong một lớp: lưng (áo choàng / cánh / ba lô) -> cổ (khăn nằm trên áo choàng) -> đồ cầm -> mũ cuối cùng
        for (int pass = 0; pass < THU_TU.length; pass++) {
            for (int i = 0; i < ds.length; i++) {
                veManh(g, ds[i], THU_TU[pass], dex, x, y, dir, lop);
            }
        }
        return true;
    }

    /** Neo trang phục từ điểm ảnh khung (NeoTrangPhuc) của lần vẽ đang chạy. */
    private static final int[] NTP = new int[NeoTrangPhuc.SO];

    /** Mảnh đeo ở TRÁN (kính bơi trên trán): đáy mảnh ngay trên hàng mắt. */
    static boolean laManhTran(int id) {
        return id == 19;
    }

    /** Hệ số phóng nguyên lớn nhất để bề ngang điểm đặc (w) không vượt {@code toiDa}; tối thiểu 1, tối đa 4. */
    static int heSoVua(int w, int toiDa) {
        int k = 1;
        while (k < 4 && w * (k + 1) <= toiDa) {
            k++;
        }
        return k;
    }

    /**
     * Vẽ mảnh id nếu thuộc ô oCan và lớp lop. Đặt theo ĐIỂM ẢNH khung (TP): hộp điểm đặc của mảnh canh giữa đúng cột giữa
     * sọ / cổ / lưng (đối xứng nên lật gương không lệch); mũ: hàng đáy chạm đỉnh sọ (chồng đúng 1 hàng, không hở, không
     * lún xuống mắt), rộng tối đa bề ngang đầu + 2; kính trán: đáy ngay trên hàng mắt; khăn: đỉnh ở cổ; áo choàng / cánh /
     * ba lô: đỉnh ở vai, sau thân khi nhìn xuống / ngang, trước thân (ở lưng, không bao giờ ở chân) khi quay lưng.
     */
    static void veManh(Graphics g, int id, int oCan, int dex, int x, int y, int dir, int lop) {
        int huong = dir == 0 ? 0 : dir == 1 ? 1 : 2;
        boolean lat = dir == 3;
        Manh m = manh(id, huong);
        if (m == null || m.o != oCan || NgoaiHinhVe.biCam(dex, m.o)) {
            return;
        }
        int l = m.lop;
        if (m.o == NgoaiHinhCua.TAY && (NEO[7] & 2) != 0) {
            l = PhuKien.SAU;
        }
        if (l != lop) {
            return;
        }
        pixel(m);
        int bw = m.bx1 - m.bx0 + 1, bh = m.by1 - m.by0 + 1;
        int rongDau = NTP[NeoTrangPhuc.DAU_PHAI] - NTP[NeoTrangPhuc.DAU_TRAI] + 1;
        int rongThan = NTP[NeoTrangPhuc.THAN_PHAI] - NTP[NeoTrangPhuc.THAN_TRAI] + 1;
        int kk, cx, dayY = 0, dinhY = 0;
        boolean theoDay;
        switch (m.o) {
            case NgoaiHinhCua.MU: {
                // hoa cài / nơ: phụ kiện nhỏ, tối đa nửa đầu; mũ: 60-100% bề ngang sọ, không quá đầu + 2
                kk = m.dx != 0 ? heSoVua(bw, Math.max(bw, rongDau / 2)) : heSoVua(bw, rongDau);
                if (bw * kk > rongDau + 2 && m.dx == 0) {
                    return;   // mũ to hơn đầu: không vẽ (thà không có còn hơn đè lệch)
                }
                cx = NTP[NeoTrangPhuc.DAU_X];
                theoDay = true;
                if (laManhTran(id) && NTP[NeoTrangPhuc.MAT_TREN] != NeoTrangPhuc.KHONG) {
                    dayY = NTP[NeoTrangPhuc.MAT_TREN] - 1;
                    if (dayY < NTP[NeoTrangPhuc.DINH_Y] + bh * kk - 1 - 2 * kk) {
                        dayY = NTP[NeoTrangPhuc.DINH_Y] + bh * kk - 1 - 2 * kk;   // kính không nhô quá đỉnh sọ
                    }
                    if (dayY > NTP[NeoTrangPhuc.MAT_TREN] - 1) {
                        dayY = NTP[NeoTrangPhuc.MAT_TREN] - 1;
                    }
                } else {
                    dayY = NTP[NeoTrangPhuc.DINH_Y];   // đáy mũ chồng đúng hàng đỉnh sọ
                }
                break;
            }
            case NgoaiHinhCua.KHAN:
            case NgoaiHinhCua.VONG: {
                kk = heSoVua(bw, Math.max(rongThan * 7 / 10, rongDau * 6 / 10));
                cx = NTP[NeoTrangPhuc.CO_X];
                theoDay = false;
                dinhY = NTP[NeoTrangPhuc.CO_Y] - 1;
                break;
            }
            case NgoaiHinhCua.LUNG: {
                kk = heSoVua(bw, Math.max(rongThan, rongDau) + 4);
                cx = dir == 1 ? NTP[NeoTrangPhuc.CO_X] : NTP[NeoTrangPhuc.LUNG_X];
                theoDay = false;
                dinhY = NTP[NeoTrangPhuc.LUNG_Y] - 1;
                // không dài quá mặt đất (điểm chân): áo choàng / cánh không thò xuống dưới chân; quay lưng: không quá eo nhiều
                int day = dir == 1 ? NTP[NeoTrangPhuc.EO_Y] + 2 : NTP[NeoTrangPhuc.DAY_Y] - 1;   // đáy thân thật (khung đánh thân lệch)
                while (kk > 1 && dinhY + bh * kk - 1 > day) {
                    kk--;
                }
                if (dinhY + bh * kk - 1 > NTP[NeoTrangPhuc.DAY_Y] - 1) {
                    return;   // nằm gục / thân quá thấp: đồ lưng thò xuống đất -> không vẽ
                }
                break;
            }
            default: {   // TAY: điểm tay PMD
                kk = heSoVua(bw, Math.max(6, rongDau * 7 / 10));
                int X = x + NEO[3] - ((bw * kk) - 1) / 2;
                int Y = y + NEO[4] - ((bh * kk) - 1) / 2;
                ve(g, m, lat, kk, X - m.bx0 * kk, Y - m.by0 * kk, lop);
                return;
            }
        }
        int dx = m.o == NgoaiHinhCua.MU ? m.dx * kk : 0;   // chỉ hoa cài / nơ cố ý lệch; đồ lưng canh đúng vai
        if (lat) {
            dx = -dx;
        }
        int bxLat0 = lat ? m.w - 1 - m.bx1 : m.bx0;
        int X = x + cx + dx - (bw * kk - 1) / 2;          // hộp đặc canh giữa cột cx
        int Y = theoDay ? y + dayY - (bh * kk - 1) : y + dinhY;
        ve(g, m, lat, kk, X - bxLat0 * kk, Y - m.by0 * kk, lop);
    }

    static void ve(Graphics g, Manh m, boolean lat, int kk, int X, int Y, int lop) {
        Image img = anh(m, lat, kk);
        if (img == null) {
            return;
        }
        g.drawImage(img, X, Y, Graphics.TOP | Graphics.LEFT);
        if (ghiHop && soHopVe < 64) {
            int b = soHopVe * 7;
            int bx0 = lat ? m.w - 1 - m.bx1 : m.bx0;
            hopVe[b] = X + bx0 * kk;                         // HỘP ĐIỂM ĐẶC (không phải hộp ảnh)
            hopVe[b + 1] = Y + m.by0 * kk;
            hopVe[b + 2] = (m.bx1 - m.bx0 + 1) * kk;
            hopVe[b + 3] = (m.by1 - m.by0 + 1) * kk;
            hopVe[b + 4] = m.o;
            hopVe[b + 5] = lop;
            hopVe[b + 6] = m.id;
            soHopVe++;
        }
    }

    /** Điểm cổ (giống NgoaiHinhVe.veO): null = loài quá thấp, không có cổ. */
    static int[] diemCo(int[] neo, int k) {
        int px = neo[0] + (neo[5] - neo[0]) / 2;
        int py = (neo[1] + k + neo[2] / 2) / 2;
        if (py < neo[2] + 4 * k) {
            py = neo[2] + 4 * k;
        }
        if (py > neo[6] + 2 * k) {
            py = neo[6] + 2 * k;
        }
        if (py > -3 * k) {
            return null;
        }
        return new int[]{px, py};
    }

    /** Neo của khung vừa dùng ở veLop (test). */
    public static int[] neoVuaDung() {
        return NEO;
    }

    /**
     * Vòng hào quang dưới chân (thay vòng sáng theo bậc): khung động 4 nhịp, phóng nguyên theo bề ngang thân.
     * true = đã vẽ.
     */
    public static boolean veHaoQuang(Graphics g, NgoaiHinhCua n, int dex, boolean shiny, int x, int yChan, int tick) {
        if (n == null || n.haoQuang <= 0 || !coHaoQuang(n.haoQuang)) {
            return false;
        }
        int rw = NgoaiHinhVe.banKinh(dex, shiny);
        int k = Math.max(1, Math.min(4, (2 * rw * 5 / 4 + 14) / 15));   // vòng rộng hơn chân một chút
        int f = (tick / 5) & 3;
        Image img = anhHaoQuang(n.haoQuang, f, k);
        if (img == null) {
            return false;
        }
        g.drawImage(img, x - img.getWidth() / 2, yChan - img.getHeight() + k, Graphics.TOP | Graphics.LEFT);
        return true;
    }

    static Image anhHaoQuang(int so, int f, int k) {
        int[][] kh = HQ[so];
        if (kh == null || f >= kh.length) {
            return null;
        }
        if (HQ_ANH[so] == null) {
            HQ_ANH[so] = new Image[kh.length * 4];
        }
        int i = f * 4 + k - 1;
        if (HQ_ANH[so][i] != null) {
            return HQ_ANH[so][i];
        }
        int w = kh[f][2], h = kh[f][3];
        int[] p = new int[w * h];
        atlas.getRGB(p, 0, w, kh[f][0], kh[f][1], w, h);
        int W = w * k, H = h * k;
        int[] q = new int[W * H];
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                q[y * W + x] = p[(y / k) * w + x / k];
            }
        }
        try {
            HQ_ANH[so][i] = Image.createRGBImage(q, W, H, true);
        } catch (Throwable e) {
            return null;
        }
        return HQ_ANH[so][i];
    }

    /** Mảnh thẻ bạn đồng hành trên Pokémon đi theo (tiLe % như lúc vẽ pet). */
    public static void veThe(Graphics g, int so, int dex, boolean shiny, int x, int y, int dir, int dongTac, int tick, int tiLe) {
        int[] t = the(so);
        if (t == null || !PokeBody.neo(dex, shiny, dir, dongTac, tick, tiLe, NEO)) {
            return;
        }
        int huong = dir == 0 ? 0 : dir == 1 ? 1 : 2;
        boolean lat = dir == 3;
        Manh m = manh(t[0], huong);
        if (m == null || NgoaiHinhVe.biCam(dex, m.o)) {
            return;
        }
        if (m.lop != PhuKien.TRUOC && m.o != NgoaiHinhCua.MU) {
            return;   // khăn / chuông nhỏ khi quay lưng: khuất sau thân
        }
        int px, py;
        if (m.neo == PhuKien.N_DINH) {
            px = NEO[0];
            py = NEO[2];
        } else {
            int[] c = diemCo(NEO, 1);
            if (c == null) {
                return;
            }
            px = c[0];
            py = c[1];
        }
        int ax = lat ? m.w - 1 - m.ax : m.ax;
        Image img = anh(m, lat, 1);
        if (img != null) {
            g.drawImage(img, x + px + (lat ? -m.dx : m.dx) - ax, y + py + m.dy - m.ay, Graphics.TOP | Graphics.LEFT);
        }
    }
}
