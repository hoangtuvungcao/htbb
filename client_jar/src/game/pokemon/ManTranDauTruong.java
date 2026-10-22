package game.pokemon;

import game.model.Char;
import game.model.IAction;
import game.render.Canvas;
import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * TRẬN ĐẤU TRƯỜNG BẢO BỐI ({@link DauTruong}, gói -109 kiểu 2 / 3 / 4) - CẢNH ĐẤU riêng toàn màn (không phải tấm trên thế
 * giới; đóng cảnh là về đúng bản đồ / chỗ đứng cũ vì nhân vật không rời bản đồ).
 * <ul>
 * <li><b>Sân:</b> sân đấu kiểu Pokémon (biểu tượng Poké Ball trên sân, hai bục nổi có bóng: mình trái dưới, đối thủ phải
 *     trên) theo bậc Đấu trường của người tấn công + biến thể mùa (máy chủ gửi số sân {@link DauTruong#san}, tên hiện
 *     trên băng lượt); ảnh res/ui/dau_truong_san_&lt;n&gt;.png sinh bởi tools/pokemon/sinh_anh_dau_truong.py, phóng
 *     nguyên. J2ME (không có res/ui): sân khối phẳng cùng màu.</li>
 * <li><b>Pokémon:</b> sprite động PMD (kể cả sắc màu) đi lại TỰ DO trên sân (chỉ là hình, máy chủ quyết kết quả), luôn
 *     quay mặt về nhau, không đè bảng HP / nút / thanh dưới; chỗ xuất phát chỉ dùng khi ra sân.</li>
 * <li><b>Diễn sự kiện máy chủ</b> (DauTruongTran.SK_*): vật lý lao tới mục tiêu rồi ở lại chỗ mới; đặc biệt bắn hiệu ứng
 *     (res/pk/hu_*.png) tới mục tiêu; trúng = nổ, giật lùi + rung, số sát thương bay, "Siêu hiệu quả!" / "Không hiệu quả
 *     lắm" / "Chí mạng!", rung màn khi đòn to (PC / APK); trượt = né sang bên + "Trượt!"; trạng thái có hạt (tia điện,
 *     lửa, Zzz); ngã gục = rơi + mờ dần; ra sân = tia đỏ thu con cũ về bóng, bên kia loé sáng dịch chuyển về chỗ xuất
 *     phát, ném bóng vào chỗ xuất phát rồi bung ra.</li>
 * <li><b>Mặc định máy tự đánh</b> (máy chủ chọn chiêu theo AI, giới hạn PP / hồi chiêu / không lặp trạng thái): không bảng
 *     chiêu, không hộp chữ - sự kiện hiện thành chữ bay; băng nhỏ lượt + tên sân; MỘT nút tốc độ ×1 -&gt; ×2 -&gt; ×3 (mở theo
 *     cấp, máy chủ gửi cấp cần; bấm tốc độ khoá thì hiện khoá + cấp).</li>
 * <li><b>Tự chọn chiêu</b> (bật trong cài đặt Đấu trường): bảng 4 chiêu (icon PNG chieu_loai, hệ, tên, PP, hồi chiêu / khoá),
 *     2 nút đổi (chân dung + HP), "Máy đánh", đồng hồ 20 giây; mọi nút tắt khi đang diễn.</li>
 * </ul>
 */
public final class ManTranDauTruong extends ManBang {
    public static ManTranDauTruong me;

    public static ManTranDauTruong gI() {
        if (me == null) {
            me = new ManTranDauTruong();
        }
        return me;
    }

    public static void mo() {
        ManTranDauTruong m = gI();
        if (Canvas.currentScreen != m) {
            m.moBang();
        }
    }

    // ================================================================== đồng hồ cảnh (kiểm thử đặt giả)

    /** > 0 = giờ giả (ms) cho kiểm thử / quay clip. */
    public static long gioGia;

    static long bay() {
        return gioGia > 0 ? gioGia : System.currentTimeMillis();
    }

    static boolean nhe() {
        return ChuUI.bo == null;
    }

    // ================================================================== trạng thái hiển thị

    /** HP / trạng thái / con đang ra ĐANG HIỆN (theo sự kiện đã diễn); hết hàng thì khớp lại dữ liệu máy chủ. */
    static final int[][] HP = new int[2][3], TT = new int[2][3];
    static final int[] RA = new int[2];
    static final boolean[] AN = new boolean[2];
    /** Vị trí chân (x, y), đích đi tới, mốc đổi đích của mỗi bên (toạ độ màn). */
    static final int[] X = new int[2], Y = new int[2], DX = new int[2], DY = new int[2];
    static final long[] DOI_DICH = new long[2];
    static boolean coViTri;
    static long hat = 1, nhipCuoi;
    static DauTruong.SuKien dang;
    static long dangTu;
    static int[] tuDash;

    /** Bắt đầu trận (gói 2): HP đầy, hai bên chưa ra sân, xếp hàng diễn "ra sân" hai bên. */
    public static void batDauCanh() {
        for (int b = 0; b < 2; b++) {
            for (int i = 0; i < 3; i++) {
                DauTruong.Con c = DauTruong.DOI[b][i];
                HP[b][i] = c == null ? 0 : c.hp;
                TT[b][i] = c == null ? 0 : c.tt;
            }
            RA[b] = DauTruong.dangRa[b];
            AN[b] = true;
        }
        coViTri = false;
        dang = null;
        hat = DauTruong.tranId * 2654435761L + 7;
        java.util.Vector v = new java.util.Vector();
        for (int b = 1; b >= 0; b--) {
            DauTruong.SuKien e = new DauTruong.SuKien();
            e.kieu = DauTruong.SK_RA_SAN;
            e.ben = b;
            e.so = DauTruong.dangRa[b];
            e.co = DauTruong.CO_MO_DAU;
            DauTruong.Con c = DauTruong.DOI[b][DauTruong.dangRa[b]];
            e.chu = b == 1 ? DauTruong.tenDoiThu + " đưa " + (c == null ? "" : c.ten) + " ra!" : "Tiến lên, " + (c == null ? "" : c.ten) + "!";
            v.addElement(e);
        }
        for (int i = 0; i < v.size(); i++) {
            DauTruong.HANG.insertElementAt(v.elementAt(i), i);
        }
    }

    /** Kiểm thử: diễn xong ngay mọi sự kiện đang chờ. */
    public static void xongHoatCanh() {
        if (dang != null) {
            apDung(dang);
            dang = null;
        }
        while (DauTruong.HANG.size() > 0) {
            DauTruong.SuKien e = (DauTruong.SuKien) DauTruong.HANG.elementAt(0);
            DauTruong.HANG.removeElementAt(0);
            apDung(e);
        }
        khop();
    }

    public static boolean dangDien() {
        return dang != null || DauTruong.HANG.size() > 0;
    }

    static int rnd(int n) {
        hat = hat * 6364136223846793005L + 1442695040888963407L;
        return n <= 1 ? 0 : (int) (((hat >>> 33) & 0x7FFFFFFF) % n);
    }

    static void veXuatPhat(int b) {
        X[b] = xuatPhatX(b);
        Y[b] = xuatPhatY(b);
        DX[b] = X[b];
        DY[b] = Y[b];
        DOI_DICH[b] = bay() + 700;
    }

    /** Kết quả cuối của một sự kiện lên trạng thái hiển thị. */
    static void apDung(DauTruong.SuKien e) {
        int b = e.ben < 0 ? 0 : e.ben;
        // vị trí cuối của bên đang di chuyển trong sự kiện (lao tới / lùi / né / giật lùi)
        if (diBen >= 0 && coViTri) {
            if (hopLe(diBen, DEN[0], DEN[1]) || e.kieu == DauTruong.SK_DUNG) {
                X[diBen] = DEN[0];
                Y[diBen] = DEN[1];
            }
            diBen = -1;
        }
        switch (e.kieu) {
            case DauTruong.SK_RA_SAN:
                RA[b] = Math.max(0, Math.min(2, e.so));
                if (coViTri) {
                    veXuatPhat(b);
                    if ((e.co & DauTruong.CO_MO_DAU) == 0 && !AN[1 - b]) {
                        veXuatPhat(1 - b);
                    }
                }
                AN[b] = false;
                break;
            case DauTruong.SK_TRUNG:
            case DauTruong.SK_MAT_MAU:
                HP[b][RA[b]] = Math.max(0, HP[b][RA[b]] - e.so);
                break;
            case DauTruong.SK_HOI:
                HP[b][RA[b]] += e.so;
                break;
            case DauTruong.SK_TRANG_THAI:
                if (e.so >= 1 && e.so <= 5) {
                    TT[b][RA[b]] = e.so;
                }
                break;
            case DauTruong.SK_GUC:
                HP[b][RA[b]] = 0;
                AN[b] = true;
                break;
            default:
                break;
        }
        DOI_DICH[0] = DOI_DICH[1] = bay();
    }

    /** Bên đang di chuyển theo sự kiện hiện tại (-1 = không), điểm đi / đến (toạ độ chân). */
    static int diBen = -1;
    static final int[] TU = new int[2], DEN = new int[2];

    /** Lúc bắt đầu một sự kiện: AI "đấu sĩ" chỉ để nhìn - vật lý áp sát tầm đánh, đặc biệt lùi giữ khoảng cách, trượt thì
     *  né ngang, trúng thì giật lùi; kết quả do máy chủ quyết. */
    static void batDauDi(DauTruong.SuKien e) {
        diBen = -1;
        if (!coViTri || e.ben < 0 || e.ben > 1) {
            return;
        }
        int b = e.ben, o = 1 - b;
        int dxv = X[o] - X[b], dyv = Y[o] - Y[b];
        int d = Math.max(1, (int) Math.sqrt(dxv * dxv + dyv * dyv));
        int s = coSprite(b);
        int x = X[b], y = Y[b];
        if (e.kieu == DauTruong.SK_DUNG && !AN[o]) {
            if (e.loai == 0) {
                int tam = (coSprite(o) + s) * 45 / 100;   // tầm cận chiến
                x = X[o] - dxv * tam / d;
                y = Y[o] - dyv * tam / d;
            } else if (e.loai == 1) {
                int lui = (R - L) / 9;                    // lùi giữ khoảng cách rồi bắn
                x = X[b] - dxv * lui / d;
                y = Y[b] - dyv * lui / d;
            } else {
                return;
            }
        } else if (e.kieu == DauTruong.SK_TRUOT) {
            int ne = s * 3 / 4;                           // né vuông góc hướng đòn
            x = X[b] - dyv * ne / d;
            y = Y[b] + dxv * ne / d / 2;
            if (!hopLe(b, x, y)) {
                x = X[b] + dyv * ne / d;
                y = Y[b] - dxv * ne / d / 2;
            }
        } else if ((e.kieu == DauTruong.SK_TRUNG || e.kieu == DauTruong.SK_MAT_MAU) && e.so > 0) {
            int lui = s / 3;                              // giật lùi xa người đánh
            x = X[b] - dxv * lui / d;
            y = Y[b] - dyv * lui / d;
        } else {
            return;
        }
        x = Math.max(L + s / 2 + 2, Math.min(R - s / 2 - 2, x));
        y = Math.max(zTop + s + 2, Math.min(zBot, y));
        if (e.kieu != DauTruong.SK_DUNG && !hopLe(b, x, y)) {
            x = X[b];
            y = Y[b];
        }
        diBen = b;
        TU[0] = X[b];
        TU[1] = Y[b];
        DEN[0] = x;
        DEN[1] = y;
    }


    /** Hết hàng: khớp dữ liệu máy chủ (HP / trạng thái / con đang ra). */
    static void khop() {
        for (int b = 0; b < 2; b++) {
            for (int i = 0; i < 3; i++) {
                DauTruong.Con c = DauTruong.DOI[b][i];
                if (c != null) {
                    HP[b][i] = c.hp;
                    TT[b][i] = c.tt;
                }
            }
            RA[b] = DauTruong.dangRa[b];
            DauTruong.Con c = DauTruong.DOI[b][RA[b]];
            AN[b] = c == null || c.hp <= 0;
        }
    }

    static int thoiLuong(DauTruong.SuKien e) {
        switch (e.kieu) {
            case DauTruong.SK_RA_SAN:
                return (e.co & DauTruong.CO_MO_DAU) != 0 ? 1100 : 1900;
            case DauTruong.SK_DUNG:
                return 850;
            case DauTruong.SK_TRUNG:
                return 1000;
            case DauTruong.SK_GUC:
                return 1100;
            case DauTruong.SK_TRANG_THAI:
            case DauTruong.SK_BI_CHAN:
                return 1000;
            case DauTruong.SK_CHU:
                return 700;
            default:
                return 900;
        }
    }

    static int nhanh() {
        return 100 * Math.max(1, DauTruong.toc) + Math.max(0, DauTruong.HANG.size() - 6) * 15;
    }

    /** Nhịp diễn: hết sự kiện hiện tại -> áp dụng, lấy sự kiện kế. Hàng dài (máy đánh) thì diễn nhanh hơn; "Bỏ qua" = xong ngay. */
    static void nhipDien(long t) {
        if (DauTruong.boQua && dangDien()) {
            xongHoatCanh();
            return;
        }
        if (dang != null && (t - dangTu) * nhanh() / 100 >= thoiLuong(dang)) {
            apDung(dang);
            dang = null;
        }
        if (dang == null && DauTruong.HANG.size() > 0) {
            dang = (DauTruong.SuKien) DauTruong.HANG.elementAt(0);
            DauTruong.HANG.removeElementAt(0);
            dangTu = t;
            batDauDi(dang);
        }
        if (dang == null && DauTruong.HANG.size() == 0) {
            khop();
        }
    }


    static float tienDo(long t) {
        if (dang == null) {
            return 1f;
        }
        float p = (t - dangTu) * nhanh() / 100f / thoiLuong(dang);
        return p < 0 ? 0 : p > 1 ? 1 : p;
    }

    // ================================================================== bố cục

    static int L, T, R, B, yUi, fTop, fBot, cuW, cuH;
    /** Mép trên vùng đánh nhau (dưới hai bảng HP ở dải nền sau): sprite không bao giờ vào bảng HP / thanh dưới. */
    static int zTop, zBot, hBang;
    static int[] tamThu = new int[4], tamCong = new int[4];

    static void boCuc(int hUi) {
        L = Bang.trai();
        T = Bang.tren();
        R = Bang.phai();
        B = Bang.duoi();
        yUi = B - hUi;
        fTop = T;
        fBot = yUi - Bang.cach();
        if (cuW != Canvas.w || cuH != Canvas.h) {
            cuW = Canvas.w;
            cuH = Canvas.h;
            coViTri = false;
        }
    }

    /** Ảnh sân 800 x 340 (điểm logic; khung sân rộng 600 ở giữa) (toạ độ bục khớp sinh_anh_dau_truong.py BUC_THU / BUC_CONG). */
    static final int NEN_W = 800, NEN_H = 340, BUC_THU_X = 490, BUC_THU_Y = 168, BUC_CONG_X = 310, BUC_CONG_Y = 292;
    /** Màu khối phẳng J2ME theo sân: {trời / nền sau, mép sân, sân}. */
    static final int[][] MAU_SAN = {{0x8CC6F0, 0x5C8A3A, 0x78B850}, {0xD8B888, 0x8A6A44, 0xC8A070}, {0x9AD0F0, 0x4A7EB8, 0x5A9AD8},
        {0x2A3040, 0xC8A020, 0x7A8494}, {0xC8E4F4, 0x8AB0C8, 0xDCEEF8}, {0x3A1A14, 0xC84818, 0x5A3428}, {0x6A5AA0, 0x5A4A6A, 0x8A7A8A},
        {0x1E1630, 0x4A3A6A, 0x3A2E54}};
    static int nenF, nenX0, nenY0;

    static void tinhNen() {
        // phóng nguyên vừa phủ bề ngang + cao sân; cắt bớt nền sau phía trên nếu màn thấp
        // phóng NGUYÊN vừa phủ bề ngang (cả khung sân luôn thấy hết); sân cao hơn ảnh thì phần trên tô màu nền sau
        int f = Math.max(1, Canvas.w / (NEN_W * 3 / 4));
        nenF = f;
        nenX0 = (Canvas.w - NEN_W * f) / 2;
        nenY0 = fBot - NEN_H * f + f * 8;
    }

    /** Bục đỏ đối thủ (ảnh: tâm BUC_THU, rx 30 / ry 8 + bóng) trên màn {x, y, w, h}. */
    static int[] oBucThu() {
        int f = nenF;
        return new int[]{nenX0 + (BUC_THU_X - 32) * f, nenY0 + (BUC_THU_Y - 10) * f, 70 * f, 26 * f};
    }

    static boolean giao(int[] a, int[] b) {
        return a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
    }

    /**
     * Bố cục màn thấp (bảng mình lên góc phải trên, vd 2400x1080 tự chọn chiêu): bục đối thủ trong ảnh sân bị bảng che ->
     * dời cả ảnh sân sang TRÁI (vẫn phủ kín mép phải) rồi nếu còn chồng thì XUỐNG dưới bảng.
     */
    static void tranhBucThu() {
        if (PokeBody.napAnhChung(anhSan()) == null || tamCong == null) {
            return;
        }
        int[] o = oBucThu();
        if (!giao(o, tamCong)) {
            return;
        }
        int conTrai = nenX0 + NEN_W * nenF - Canvas.w;   // dời trái tối đa chừng này (ảnh vẫn phủ tới mép phải)
        int can = o[0] + o[2] - (tamCong[0] - 6);
        nenX0 -= Math.max(0, Math.min(can, conTrai));
        o = oBucThu();
        if (giao(o, tamCong)) {
            nenY0 += tamCong[1] + tamCong[3] + 6 - o[1];
        }
        if (nenX0 != bucX0 || nenY0 != bucY0) {
            bucX0 = nenX0;
            bucY0 = nenY0;
            coViTri = false;   // vị trí xuất phát theo bục mới (chỉ khi đổi - không phá hoạt hình mỗi khung)
        }
    }

    static int bucX0 = Integer.MIN_VALUE, bucY0 = Integer.MIN_VALUE;

    /** Kiểm thử: bục đối thủ bị bảng HP che? */
    public static boolean bucThuBiChe() {
        return PokeBody.napAnhChung(anhSan()) != null && tamCong != null && (giao(oBucThu(), tamCong) || giao(oBucThu(), tamThu));
    }

    static int xuatPhatX(int b) {
        int x = nenX0 + (b == 1 ? BUC_THU_X : BUC_CONG_X) * nenF;
        if (PokeBody.napAnhChung(anhSan()) == null) {
            x = L + (R - L) * (b == 1 ? 71 : 29) / 100;
        }
        return Math.max(L + coSprite(b) / 2 + 4, Math.min(R - coSprite(b) / 2 - 4, x));
    }

    static int xuatPhatY(int b) {
        int y = nenY0 + (b == 1 ? BUC_THU_Y : BUC_CONG_Y) * nenF;
        if (PokeBody.napAnhChung(anhSan()) == null) {
            y = fTop + (fBot - fTop) * (b == 1 ? 50 : 84) / 100;
        }
        int s = coSprite(b);
        // tránh bảng HP: đối thủ dưới bảng đối thủ, mình trên đáy sân
        return Math.max(zTop + s + 2, Math.min(zBot, y));
    }

    /** % phóng sprite theo cao sân + độ sâu (gần đáy = to hơn). */
    static int tiLe(int b) {
        // phóng NGUYÊN (nét pixel): hình cao ~1/7 màn ở mọi cỡ; loài to (Gyarados) không lấp sân, loài nhỏ không lọt thỏm
        // nhóm cỡ loài (nhỏ / vừa / to) trong khoảng hẹp 0,9..1,15, rồi chọn hệ số nguyên gần mục tiêu nhất
        // loài nhỏ / vừa coi như cao 28 (dáng rộng như Gengar không to hơn Pikachu); loài to (Gyarados) giữ to hơn chút
        int h = Math.max(28, Math.min(90, caoGoc(b)));
        int muc = Math.max(24, Canvas.h / 7) * (h < 32 ? 90 : h > 56 ? 115 : 100) / 100;
        int f = Math.max(1, muc / h);
        if (Math.abs(h * (f + 1) - muc) < Math.abs(h * f - muc)) {
            f++;
        }
        return f * 100;
    }

    /** Chiều cao hình thật (px ảnh) của con đang ra bên b; chưa có sprite = 40. */
    static int caoGoc(int b) {
        DauTruong.Con c = DauTruong.DOI[b][RA[b]];
        int h = c == null ? 0 : PokeBody.caoHinh(c.dex, c.shiny);
        return h <= 0 ? 40 : h;
    }

    /** Cỡ hình hiện (px màn) - dùng tránh bảng HP / giữ khoảng cách. */
    static int coSprite(int b) {
        return Math.max(24, Math.max(32, caoGoc(b)) * tiLe(b) / 100);
    }

    static boolean giao(int x, int y, int w, int h, int[] r) {
        return x < r[0] + r[2] && x + w > r[0] && y < r[1] + r[3] && y + h > r[1];
    }

    /** Điểm chân (x, y) hợp lệ cho bên b: sprite không đè bảng HP / thanh dưới, cách bên kia đủ xa. */
    static boolean hopLe(int b, int x, int y) {
        int s = coSprite(b);
        int sx = x - s / 2, sy = y - s;
        if (sx < L + 4 || sx + s > R - 4 || sy < zTop || y > zBot) {
            return false;
        }
        if (giao(sx, sy, s, s, tamThu) || giao(sx, sy, s, s, tamCong) || giao(sx, sy, s, s, tamNut) || giao(sx, sy, s, s, tamBang)
                || giao(sx, sy, s, s, tamDong)) {
            return false;
        }
        if (AN[1 - b]) {
            return true;
        }
        int o = 1 - b;
        int dx = x - X[o], dy = y - Y[o];
        int min = (coSprite(0) + coSprite(1)) * 6 / 10;
        return dx * dx + dy * dy >= min * min;
    }

    static void chonDich(int b, long t) {
        DOI_DICH[b] = t;
    }

    /**
     * Đi lại giữa các đòn (chỉ hình): hai bên vờn quanh nhau trên một vòng elip giữa sân (đối diện nhau), lúc áp sát lúc lùi
     * ra (khoảng cách dao động chậm), chạy tới chỗ mới chứ không giật cục; đứng yên khi đang diễn một sự kiện.
     */
    static void diLai(long t) {
        if (!coViTri) {
            coViTri = true;
            for (int b = 0; b < 2; b++) {
                veXuatPhat(b);
            }
            nhipCuoi = t;
            DOI_DICH[0] = DOI_DICH[1] = t;
        }
        long dt = Math.max(0, Math.min(100, t - nhipCuoi));
        nhipCuoi = t;
        boolean dungYen = dang != null && dang.kieu != DauTruong.SK_CHU;
        if (dungYen) {
            return;
        }
        int cx = (L + R) / 2, cy = (zTop + zBot) / 2 + coSprite(0) / 2;
        int rx = (R - L) * 32 / 100, ry = Math.max(4, (zBot - zTop - coSprite(0)) * 40 / 100);
        double goc = t / 2600.0;
        double xa = 0.70 + 0.30 * Math.sin(t / 1900.0);
        for (int b = 0; b < 2; b++) {
            if (AN[b]) {
                continue;
            }
            // vừa ra sân: đứng ở chỗ xuất phát một nhịp rồi mới vờn
            if (t - DOI_DICH[b] < 700) {
                continue;
            }
            double a = goc + (b == 1 ? -0.25 : 0.75) * Math.PI;
            int tx = cx + (int) (Math.cos(a) * rx * xa), ty = cy + (int) (Math.sin(a) * ry * xa);
            int v = (int) Math.max(1, (R - L) * dt / 5000);
            int dx = tx - X[b], dy = ty - Y[b];
            int d = (int) Math.sqrt(dx * dx + dy * dy);
            if (d > 1) {
                int nx = X[b] + (d <= v ? dx : dx * v / d), ny = Y[b] + (d <= v ? dy : dy * v / d);
                if (hopLe(b, nx, ny)) {
                    X[b] = nx;
                    Y[b] = ny;
                } else if (hopLe(b, nx, Y[b])) {
                    X[b] = nx;
                } else if (hopLe(b, X[b], ny)) {
                    Y[b] = ny;
                }
            }
        }
    }


    protected String tieuDe() {
        if (DauTruong.coKetQua) {
            return DauTruong.kqThang ? "Chiến thắng!" : "Thất bại";
        }
        return "Lượt " + Math.min(DauTruong.luotToiDa, DauTruong.luot + 1) + "/" + DauTruong.luotToiDa + " · " + DauTruong.tenSan;
    }

    protected int rongMuon() {
        return ChuDe.dt() ? 560 : 480;
    }

    protected void lui() {
        dong();
    }

    public void dong() {
        if (DauTruong.dangDanh() && !DauTruong.xemLai) {
            HopXacNhan.hoiNguyHiem("Đầu hàng", "Thoát trận sẽ tính là THUA. Bạn chắc chắn?", "Đầu hàng", new IAction() {
                public void perform() {
                    DauTruong.dauHang();
                }
            });
            return;
        }
        veDauTruong();
    }

    static void veDauTruong() {
        DauTruong.tranId = 0;
        DauTruong.DOI_THU = new java.util.Vector();
        DauTruong.coDoiThu = false;
        DauTruong.HANG.removeAllElements();
        dang = null;
        DauTruong.xinMo();
        if (ManDauTruong.me != null) {
            ManDauTruong.me.moBang();
        } else {
            Canvas.gameScr.switchToMe();
        }
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        // không dùng: cảnh đấu tự vẽ toàn màn ở paint()
    }

    // ================================================================== vẽ

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        long t = bay();
        if (DauTruong.DOI[0][0] == null) {
            g.setColor(0x2E5A22);
            g.fillRect(0, 0, Canvas.w, Canvas.h);
            return;
        }
        nhipDien(t);
        int k = Bang.cach();
        int lh = ChuDe.cao(ChuDe.NHAN), lp = ChuDe.cao(ChuDe.PHU);
        int hn = Math.max(Math.max(Bang.co(40), VungCham.MIN), lh + lp + 12);
        boolean rong = Bang.phai() - Bang.trai() >= Bang.co(560);
        int hLenh = Math.max(2 * hn + k + 10, (Bang.duoi() - Bang.tren()) * 22 / 100);
        boCuc(DauTruong.tuDong ? 0 : rong ? hLenh : 3 * hn + 2 * k + 10);
        tinhNen();
        int sDong = Math.max(VungCham.MIN, Bang.co(30));
        // bảng HP gọn 2 hàng (tên + trạng thái + Lv / thanh HP [+ số]) kiểu game chính; dải 3 bóng đội gắn dưới bảng
        int wb = Math.max(Bang.co(150), Math.min((R - L) * 26 / 100, Bang.co(230)));
        int is = Math.max(12, Math.min(16, lh));
        hBang = lh + Math.max(8, lp - 2) + 14;
        int hTab = is + 6;
        tamThu = new int[]{L + 6, T + 6, wb, hBang + hTab - 2};
        tamCong = new int[]{R - wb - 6, fBot - hBang - hTab - 4, wb, hBang + hTab - 2};
        tamDong = new int[]{R - sDong - 6, T, sDong + 8, sDong + 6};
        int wLuot = ChuDe.rong("Chọn chiêu · Lượt 50/50 · Đỉnh rồng Blackthorn", ChuDe.PHU) + 24;
        int cho = R - L - 2 * (wb + 12) - 2 * (sDong + 12);
        int yBang = wLuot <= cho ? T + 6 : T + 6 + tamThu[3] + 6;
        tamBang = new int[]{(L + R) / 2 - wLuot / 2, yBang, wLuot, lp + 8};
        // vùng đánh nhau: dưới bảng đối thủ / băng lượt, trên bảng mình, cách mép
        zTop = Math.max(tamThu[1] + tamThu[3], tamBang[1] + tamBang[3]) + 8;
        zBot = tamCong[1] - 6;
        if (zBot - zTop < coSprite(0) * 2) {
            // màn thấp (bảng lệnh tự chọn chiếm chỗ): bảng mình lên góc phải trên, băng lượt xuống dưới hai bảng
            int phai = R - sDong - 14 - (DauTruong.tuDong ? sDong + 12 : 0);
            tamCong = new int[]{phai - wb, T + 6, wb, tamThu[3]};
            if (tamCong[0] < tamThu[0] + tamThu[2] + 6) {
                tamCong[0] = tamThu[0] + tamThu[2] + 6;
                tamCong[2] = Math.max(Bang.co(120), phai - tamCong[0]);
            }
            tamBang = new int[]{(L + R) / 2 - wLuot / 2, T + 6 + tamThu[3] + 6, wLuot, lp + 8};
            zTop = tamBang[1] + tamBang[3] + 6;
            zBot = fBot - 6;
            tranhBucThu();   // bảng mình ở góc phải trên: dời sân sang trái / xuống để bục đỏ đối thủ không bị che
        }
        diLai(t);
        // rung màn khi đòn to (PC / APK)
        int rx = 0, ry = 0;
        if (!nhe() && dang != null && dang.kieu == DauTruong.SK_TRUNG && dang.so * 4 >= Math.max(1, hpMax(dang.ben)) && tienDo(t) < 0.35f) {
            int a = Math.max(2, Bang.co(5));
            rx = (int) ((t / 30) % 3 - 1) * a;
            ry = (int) ((t / 40) % 3 - 1) * a / 2;
        }
        g.setColor(MAU_SAN[Math.max(0, Math.min(MAU_SAN.length - 1, DauTruong.san))][2]);
        g.fillRect(0, 0, Canvas.w, Canvas.h);
        veNen(g);                                         // nền đứng yên (rung không lộ mép màn)
        g.translate(rx, ry);
        int dau = Y[0] < Y[1] ? 0 : 1;
        veBen(g, dau, t);
        veBen(g, 1 - dau, t);
        veHieuUng(g, t);
        g.translate(-rx, -ry);
        Canvas.resetTrans(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        veBang(g, 1, tamThu, t);
        veBang(g, 0, tamCong, t);
        ketQuaHien = DauTruong.coKetQua && !dangDien();
        if (!ketQuaHien) {
            Bang.nutDong(g, R - sDong - 2, T + 2, sDong, new IAction() {
                public void perform() {
                    ManTranDauTruong.this.dong();
                }
            });
        }
        veBangLuot(g, yBang);
        if (DauTruong.tuDong) {
            veNutToc(g, t);
        } else {
            tamNut = new int[4];
            veTuChon(g, L, yUi, R - L, B - yUi, hn, rong);
        }
        if (ketQuaHien) {
            veKetQua(g);
        }
        Bang.moDan(g);
        Canvas.resetTrans(g);
    }

    /** Thẻ kết quả đang hiện: cảnh phía sau không nhận chạm. */
    static boolean ketQuaHien;

    static int hpMax(int b) {
        DauTruong.Con c = DauTruong.DOI[b][RA[b]];
        return c == null ? 1 : c.hpMax;
    }

    static String anhSan() {
        return "/ui/dau_truong_san_" + Math.max(0, Math.min(MAU_SAN.length - 1, DauTruong.san)) + ".png";
    }

    static Image nenPhong;
    static String nenPhongKhoa = "";

    void veNen(Graphics g) {
        int s = Math.max(0, Math.min(MAU_SAN.length - 1, DauTruong.san));
        Image goc = PokeBody.napAnhChung(anhSan());
        g.setColor(MAU_SAN[s][2]);
        g.fillRect(0, 0, Canvas.w, Canvas.h);
        if (goc == null) {
            // J2ME (không có res/ui): nền sau, mép sân, sân, hai bục - khối phẳng
            int yK = fTop + (fBot - fTop) / 5;
            g.setColor(MAU_SAN[s][0]);
            g.fillRect(0, 0, Canvas.w, yK);
            g.setColor(MAU_SAN[s][1]);
            g.fillRect(0, yK, Canvas.w, 4);
            for (int b = 0; b < 2; b++) {
                int cx = xuatPhatX(b), cy = xuatPhatY(b), rw = coSprite(b);
                g.setColor(MAU_SAN[s][1]);
                g.fillArc(cx - rw, cy - rw / 4, 2 * rw, rw / 2, 0, 360);
            }
            return;
        }
        String khoa = anhSan() + "@" + nenF;
        if (nenPhong == null || !khoa.equals(nenPhongKhoa)) {
            nenPhong = null;
            nenPhong = phong(goc, nenF);
            nenPhongKhoa = khoa;
        }
        if (nenPhong != null) {
            if (nenY0 > 0) {
                g.setColor(MAU_SAN[s][0]);
                g.fillRect(0, 0, Canvas.w, nenY0);
            }
            g.drawImage(nenPhong, nenX0, nenY0, Graphics.TOP | Graphics.LEFT);
        }
    }

    /** Phóng nguyên ảnh f lần (điểm gần nhất). */
    static Image phong(Image goc, int f) {
        if (f <= 1) {
            return goc;
        }
        try {
            int w = goc.getWidth(), h = goc.getHeight();
            int[] src = new int[w * h];
            goc.getRGB(src, 0, w, 0, 0, w, h);
            int W = w * f, H = h * f;
            int[] dst = new int[W * H];
            for (int y = 0; y < H; y++) {
                int hs = (y / f) * w, hd = y * W;
                for (int x = 0; x < W; x++) {
                    dst[hd + x] = src[hs + x / f];
                }
            }
            return Image.createRGBImage(dst, W, H, true);
        } catch (Throwable e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ sprite

    static int huong(int b) {
        return X[1 - b] >= X[b] ? Char.RIGHT : Char.LEFT;
    }

    void veBen(Graphics g, int b, long t) {
        DauTruong.SuKien e = dang;
        float p = tienDo(t);
        int x = X[b], y = Y[b];
        int s = coSprite(b);
        int z = tiLe(b);
        boolean diChuyen = !AN[b] && (dang == null || dang.kieu == DauTruong.SK_CHU) && t - DOI_DICH[b] >= 700;
        int dongTac = diChuyen ? PokeDuLieu.CHAY : PokeDuLieu.DUNG;
        int tick = (int) (t / 40);
        boolean hien = !AN[b];
        int catDuoi = 0;
        int ra = RA[b];
        // thở / nhấp nhô
        y -= (int) (Math.abs(((t / 9 + b * 37) % 80) - 40) * Math.max(1, s / 28) / 40);
        if (e != null && diBen == b) {
            // nội suy theo pha: vật lý = chạy tới (0..0.5) -> vồ quá đà (0.5..0.65) -> đứng; đặc biệt = lùi (0..0.35);
            // né / giật lùi = nhảy cung
            float q;
            if (e.kieu == DauTruong.SK_DUNG && e.loai == 0) {
                q = p < 0.5f ? p / 0.5f : p < 0.65f ? 1f + (float) Math.sin(Math.PI * (p - 0.5f) / 0.15f) * 0.18f : 1f;
            } else if (e.kieu == DauTruong.SK_DUNG) {
                q = Math.min(1f, p / 0.35f);
            } else {
                q = Math.min(1f, p / 0.5f);
            }
            x = TU[0] + (int) ((DEN[0] - TU[0]) * q);
            y = TU[1] + (int) ((DEN[1] - TU[1]) * q) - (e.kieu == DauTruong.SK_TRUOT ? (int) (Math.sin(Math.PI * Math.min(1f, p / 0.5f)) * s / 3) : 0);
        }
        if (e != null) {
            long da = (long) (p * thoiLuong(e));
            if (e.kieu == DauTruong.SK_DUNG && e.ben == b) {
                tick = (int) (da / 40);
                dongTac = e.loai == 0 ? (p < 0.5f ? PokeDuLieu.CHAY : PokeDuLieu.DANH_GAN) : PokeDuLieu.DANH_XA;
            } else if ((e.kieu == DauTruong.SK_TRUNG || e.kieu == DauTruong.SK_MAT_MAU) && e.ben == b && e.so > 0) {
                dongTac = PokeDuLieu.TRUNG_DON;
                tick = (int) (da / 40);
                x += p < 0.35f ? ((int) (t / 25) % 2 == 0 ? 3 : -3) : 0;
            } else if (e.kieu == DauTruong.SK_TRUOT && e.ben == b) {
                dongTac = PokeDuLieu.CHAY;
            } else if (e.kieu == DauTruong.SK_BI_CHAN && e.ben == b) {
                x += (int) (t / 60) % 2 == 0 ? 2 : -2;
            } else if (e.kieu == DauTruong.SK_GUC && e.ben == b) {
                dongTac = PokeDuLieu.GUC_NGA;
                tick = (int) (da / 40);
                y += (int) (p * s / 5);
                catDuoi = (int) (p * s);
            } else if (e.kieu == DauTruong.SK_RA_SAN) {
                boolean moDau = (e.co & DauTruong.CO_MO_DAU) != 0;
                if (e.ben == b) {
                    if (!moDau && p < 0.3f) {
                        hien = true;                      // con cũ (kể cả con vừa gục) thu vào bóng
                        z = (int) (z * (1 - p / 0.3f));
                    } else if (p < 0.9f) {
                        hien = false;
                    } else {
                        hien = true;
                        ra = Math.max(0, Math.min(2, e.so));
                        x = xuatPhatX(b);
                        y = xuatPhatY(b);
                        z = Math.max(10, (int) (z * (p - 0.9f) / 0.1f));
                    }
                } else if (!moDau && !AN[b] && p >= 0.3f && p < 0.55f) {
                    if (p < 0.42f) {                      // loé rồi biến mất ở chỗ cũ
                        z = (int) (z * (0.42f - p) / 0.12f);
                    } else {                              // hiện lại ở chỗ xuất phát
                        x = xuatPhatX(b);
                        y = xuatPhatY(b);
                        z = (int) (z * (p - 0.42f) / 0.13f);
                    }
                } else if (!moDau && !AN[b] && p >= 0.55f) {
                    x = xuatPhatX(b);
                    y = xuatPhatY(b);
                }
            }
        }
        if (!hien || z <= 5) {
            return;
        }
        DauTruong.Con c = DauTruong.DOI[b][ra];
        if (c == null) {
            return;
        }
        // bóng đổ dưới chân (khối tối dẹt)
        if (catDuoi == 0) {
            g.setColor(0x202818);
            int bw = s * 2 / 3;
            g.fillArc(x - bw / 2, y - bw / 8, bw, bw / 4, 0, 360);
        }
        if (catDuoi > 0) {
            g.setClip(0, 0, Canvas.w, Math.max(0, y - catDuoi / 2));
        }
        if (!PokeBody.paint(g, c.dex, c.shiny, x, y, huong(b, x), dongTac, tick, z)) {
            PokeBody.paintChanDung(g, c.dex, c.shiny, x, y - s / 2, Math.max(8, s * z / Math.max(1, tiLe(b))));
        }
        g.setClip(0, 0, Canvas.w, Canvas.h);
        int tt = TT[b][ra];
        if (tt == 4 && catDuoi == 0) {
            veZzz(g, x + s / 4, y - s, t);
        } else if (tt == 3 && catDuoi == 0 && (t / 700) % 3 == 0) {
            veHu(g, "hu_spark", x + s / 3, y - s * 3 / 4, (int) ((t / 40) % 1000), true, 25);
        }
    }

    /** Hướng quay mặt về phía đối thủ (tính từ x đang vẽ). */
    static int huong(int b, int x) {
        return X[1 - b] >= x ? Char.RIGHT : Char.LEFT;
    }


    // ------------------------------------------------------------------ hiệu ứng

    static final Hashtable HU_PHONG = new Hashtable();

    static Image anhPhong(Image img, String khoa, int f) {
        if (f <= 1) {
            return img;
        }
        Object o = HU_PHONG.get(khoa + "@" + f);
        if (o != null) {
            return (Image) o;
        }
        if (HU_PHONG.size() > 32) {
            HU_PHONG.clear();
        }
        Image p = phong(img, f);
        if (p != null) {
            HU_PHONG.put(khoa + "@" + f, p);
        }
        return p;
    }

    /** Vẽ hiệu ứng tên ten (tâm x, y), tick từ đầu; lap = vòng lại; phong = % so với cỡ sprite. */
    static void veHu(Graphics g, String ten, int x, int y, int tick, boolean lap, int phong) {
        HieuUngBang h = PokeHieuUng.layTheoTen(ten);
        if (h == null) {
            h = PokeHieuUng.layTheoTen("hu_hit_neutral");
            if (h == null) {
                return;
            }
        }
        Image goc = PokeHieuUng.anh(h);
        if (goc == null) {
            return;
        }
        int f = nhe() ? 1 : Math.max(1, Math.min(6, tiLe(0) * phong / 100 / 100));
        Image img = anhPhong(goc, h.ten, f);
        if (img == null) {
            img = goc;
            f = 1;
        }
        int[] n = h.nguon(h.khungTai(tick, lap), 0);
        int w = h.rong * f, hh = h.cao * f;
        if ((n[0] + h.rong) * f > img.getWidth() || (n[1] + h.cao) * f > img.getHeight()) {
            return;
        }
        g.drawRegion(img, n[0] * f, n[1] * f, w, hh, 0, x - w / 2, y - hh / 2, Graphics.TOP | Graphics.LEFT);
    }

    /** Zzz nhỏ bay lên (chữ pixel trắng viền, 3 hạt so le). */
    static void veZzz(Graphics g, int x, int y, long t) {
        int lp = ChuDe.cao(ChuDe.PHU);
        for (int i = 0; i < 3; i++) {
            int pha = (int) ((t / 25 + i * 18) % 54);
            ChuDe.chuVien(g, "z", x + i * lp / 2 + pha / 6, y - pha * lp / 40, ChuDe.PHU, 0xFFFFFF, ChuDe.VIEN, 0);
        }
    }

    static String huTrangThai(int so) {
        switch (so) {
            case 1:
                return "hu_ember";
            case 2:
                return "hu_smoke_black";
            case 3:
                return "hu_spark";
            case 4:
                return "hu_sleep_z";
            case 5:
                return "hu_stat_blue_ring";
            case 6:
                return "hu_swift_star";
            default:
                return "hu_hit_neutral";
        }
    }

    static final String[] TEN_TT = {"", "Bỏng!", "Trúng độc!", "Tê liệt!", "Ngủ!", "Đóng băng!", "Lú lẫn!", "Choáng!"};

    /** Hiệu ứng + chữ bay của sự kiện đang diễn. */
    void veHieuUng(Graphics g, long t) {
        DauTruong.SuKien e = dang;
        if (e == null || !coViTri) {
            return;
        }
        float p = tienDo(t);
        int tick = (int) (p * thoiLuong(e) / 40);
        int b = e.ben < 0 ? 0 : e.ben;
        int sb = coSprite(b);
        int cx = X[b], cy = Y[b] - sb / 2;
        switch (e.kieu) {
            case DauTruong.SK_DUNG: {
                int o = 1 - b;
                if (e.loai == 1 && p > 0.35f) {
                    float q = (p - 0.35f) / 0.65f;
                    int x = X[b] + (int) ((X[o] - X[b]) * q), y = cy + (int) ((Y[o] - coSprite(o) / 2 - cy) * q);
                    veHu(g, e.hieuUng, x, y, tick, true, 80);
                } else if (e.loai == 1) {
                    veHu(g, "hu_charge_up", cx, cy, tick, false, 70);
                } else if (e.loai == 2) {
                    veHu(g, e.hieuUng, cx, cy, tick, false, 100);
                }
                break;
            }
            case DauTruong.SK_TRUNG: {
                if ((e.co & DauTruong.CO_MIEN) != 0 || e.so <= 0) {
                    chuBay(g, "Không có tác dụng", cx, cy - sb / 2, p, 0xE8E8E8);
                    break;
                }
                if (p < 0.7f) {
                    veHu(g, e.hieuUng, cx, cy, tick, false, 100);
                    if (p < 0.10f && !nhe()) {
                        g.setColor(0xFFFFFF);
                        int r = sb / 6;
                        g.fillArc(cx - r, cy - r, 2 * r, 2 * r, 0, 360);
                    }
                }
                soBay(g, "-" + e.so, cx, cy - sb / 3, p, (e.co & DauTruong.CO_CHI_MANG) != 0 ? 0xFFD040 : 0xFFFFFF);
                String hq = (e.co & DauTruong.CO_SIEU) != 0 ? "Siêu hiệu quả!" : (e.co & DauTruong.CO_YEU) != 0 ? "Không hiệu quả lắm" : null;
                if (hq != null) {
                    chuBay(g, hq, cx, cy - sb * 3 / 4, p, (e.co & DauTruong.CO_SIEU) != 0 ? 0xFF8A3C : 0xC8D2DC);
                }
                if ((e.co & DauTruong.CO_CHI_MANG) != 0) {
                    chuBay(g, "Chí mạng!", cx, cy - sb, p, 0xFFD040);
                }
                break;
            }
            case DauTruong.SK_TRUOT:
                chuBay(g, "Trượt!", cx, cy - sb / 2, p, 0xFFFFFF);
                break;
            case DauTruong.SK_TRANG_THAI:
            case DauTruong.SK_BI_CHAN:
                if (e.so == 4) {
                    veZzz(g, cx, cy - sb / 3, bay());
                } else {
                    veHu(g, huTrangThai(e.so), cx, cy, tick, true, 60);
                }
                if (e.kieu == DauTruong.SK_TRANG_THAI && e.so > 0 && e.so < TEN_TT.length) {
                    chuBay(g, TEN_TT[e.so], cx, cy - sb / 2, p, 0xFFE070);
                }
                break;
            case DauTruong.SK_BAC:
                veHu(g, e.so >= 0 ? "hu_stat_green_ring" : "hu_stat_blue_ring", cx, Y[b] - sb / 4, tick, false, 100);
                chuBay(g, e.so > 0 ? "Tăng chỉ số!" : e.so < 0 ? "Giảm chỉ số!" : "Không đổi", cx, cy - sb / 2, p, e.so >= 0 ? 0x9CF08C : 0x9CC8F8);
                break;
            case DauTruong.SK_HOI:
                veHu(g, "hu_stat_green_ring", cx, Y[b] - sb / 4, tick, false, 100);
                soBay(g, "+" + e.so, cx, cy - sb / 3, p, 0x7CF07C);
                break;
            case DauTruong.SK_MAT_MAU:
                veHu(g, e.co == 1 ? "hu_ember" : e.co == 2 ? "hu_smoke_black" : "hu_hit_neutral", cx, cy, tick, false, 80);
                soBay(g, "-" + e.so, cx, cy - sb / 3, p, 0xFFFFFF);
                break;
            case DauTruong.SK_GUC:
                if (p < 0.3f) {
                    veHu(g, "hu_smoke_black", cx, Y[b] - sb / 4, tick, false, 100);
                }
                break;
            case DauTruong.SK_RA_SAN:
                veRaSan(g, e, p, tick);
                break;
            default:
                break;
        }
    }

    void veRaSan(Graphics g, DauTruong.SuKien e, float p, int tick) {
        int b = e.ben, o = 1 - b;
        boolean moDau = (e.co & DauTruong.CO_MO_DAU) != 0;
        int s = coSprite(b);
        // 1. tia đỏ thu con cũ (kể cả con vừa gục) về bóng
        if (!moDau && p < 0.3f) {
            int xb = b == 1 ? R - 4 : L + 4, yb = b == 1 ? fTop + (fBot - fTop) / 3 : fBot - 4;
            int x = X[b], y = Y[b] - s / 2;
            g.setColor(0xF04030);
            for (int i = 0; i <= 14; i++) {
                int px = xb + (x - xb) * i / 14, py = yb + (y - yb) * i / 14;
                g.fillRect(px - 2, py - 2, 5, 5);
            }
            g.setColor(0xFFB0A0);
            for (int i = 0; i <= 14; i += 2) {
                g.fillRect(xb + (x - xb) * i / 14 - 1, yb + (y - yb) * i / 14 - 1, 3, 3);
            }
            veHu(g, "hu_charge_beam_glow", x, y, tick, true, 80);
        }
        // 2. bên kia dịch chuyển về chỗ xuất phát: loé ở chỗ cũ, bùng sáng ở chỗ xuất phát
        if (!moDau && !AN[o] && p >= 0.28f && p < 0.6f) {
            int so = coSprite(o);
            if (p < 0.44f) {
                veHu(g, "hu_wish", X[o], Y[o] - so / 2, (int) ((p - 0.28f) * 60), false, 100);
            } else {
                veHu(g, "hu_swift_star", xuatPhatX(o), xuatPhatY(o) - so / 2, (int) ((p - 0.44f) * 60), false, 100);
            }
        }
        // 3. ném bóng vào chỗ xuất phát (vòng cung)
        float tu = moDau ? 0.15f : 0.55f;
        if (p >= tu && p < 0.92f) {
            float q = (p - tu) / (0.92f - tu);
            int x0 = b == 1 ? R + s / 2 : L - s / 2, y0 = b == 1 ? fTop + (fBot - fTop) / 3 : fBot;
            int x1 = xuatPhatX(b), y1 = xuatPhatY(b) - s / 3;
            int x = x0 + (int) ((x1 - x0) * q);
            int y = y0 + (int) ((y1 - y0) * q) - (int) (Math.sin(Math.PI * q) * (fBot - fTop) / 4);
            IconBat.ve(g, IconBat.POKE, x, y, Math.max(14, s / 3));
        }
        // 4. bung sáng
        if (p >= 0.86f) {
            veHu(g, "hu_wish", xuatPhatX(b), xuatPhatY(b) - s / 2, (int) ((p - 0.86f) * 120), false, 100);
        }
    }

    /** Số sát thương / hồi bay lên. */
    static void soBay(Graphics g, String s, int x, int y, float p, int mau) {
        int dy = (int) (p * Bang.co(30));
        ChuDe.chuVien(g, s, x, Math.max(zTop, Math.min(zBot - ChuDe.cao(ChuDe.SO_TO), y - dy)), ChuDe.SO_TO, mau, ChuDe.VIEN, 2);
    }

    static void chuBay(Graphics g, String s, int x, int y, float p, int mau) {
        if (p > 0.92f) {
            return;
        }
        int dy = (int) (Math.min(1f, p * 3) * Bang.co(10));
        int w = ChuDe.rong(s, ChuDe.NHAN);
        int xx = Math.max(L + w / 2 + 2, Math.min(R - w / 2 - 2, x));
        ChuDe.chuVien(g, s, xx, Math.max(zTop, Math.min(zBot - ChuDe.cao(ChuDe.NHAN), y - dy)), ChuDe.NHAN, mau, ChuDe.VIEN, 2);
    }

    // ------------------------------------------------------------------ bảng HP

    void veBang(Graphics g, int b, int[] r, long t) {
        int lh = ChuDe.cao(ChuDe.NHAN), lp = ChuDe.cao(ChuDe.PHU);
        int is = Math.max(12, Math.min(16, lh));
        int x = r[0], y = r[1], w = r[2], h = hBang;
        int ra = RA[b];
        DauTruong.SuKien e = dang;
        float p = tienDo(t);
        if (e != null && e.ben == b && e.kieu == DauTruong.SK_RA_SAN && p >= 0.9f) {
            ra = Math.max(0, Math.min(2, e.so));
        }
        // dải 3 bóng đội gắn dưới bảng (đối thủ: trái, mình: phải)
        int wTab = 3 * (is + 3) + 10, hTab = is + 6;
        int xTab = b == 1 ? x + 8 : x + w - wTab - 8;
        Bang.the(g, xTab, y + h - 2, wTab, hTab);
        for (int i = 0; i < 3; i++) {
            boolean song = DauTruong.DOI[b][i] != null && HP[b][i] > 0;
            IconBat.ve(g, song ? IconBat.POKE : IconBat.SAO_RONG, xTab + 6 + i * (is + 3) + is / 2, y + h - 2 + hTab / 2, is);
        }
        Bang.the(g, x, y, w, h);
        DauTruong.Con c = DauTruong.DOI[b][ra];
        if (c == null) {
            return;
        }
        int xt = x + 8, wt = w - 16;
        int tt = TT[b][ra];
        String ttc = tt > 0 && tt < DauTruong.TEN_TT.length ? DauTruong.TEN_TT[tt] : "";
        int wtt = ttc.length() > 0 ? ChuDe.rong(ttc, ChuDe.PHU) + 10 : 0;
        String lv = "Lv50";
        int wlv = ChuDe.rong(lv, ChuDe.PHU);
        ChuDe.chu(g, ChuDe.cat(c.ten, ChuDe.NHAN, wt - wlv - wtt - 12), xt, y + 4, ChuDe.NHAN, ChuDe.CHU, 0);
        ChuDe.chu(g, lv, xt + wt, y + 4 + (lh - lp) / 2, ChuDe.PHU, ChuDe.CHU_PHU, 1);
        if (wtt > 0) {
            GiaoDien.vien(g, xt + wt - wlv - wtt - 6, y + 4 + (lh - lp - 2) / 2, wtt, lp + 2, ttMau(tt), null);
            ChuDe.chuVienGiua(g, ttc, xt + wt - wlv - 6 - wtt / 2, y + 4 + (lh - lp - 2) / 2, lp + 2, ChuDe.PHU, 0xFFFFFF, ChuDe.VIEN, 2);
        }
        int hb = Math.max(8, lp - 2);
        int yb = y + h - hb - 6;
        int hp = HP[b][ra];
        if (e != null && e.ben == b && ra == RA[b] && (e.kieu == DauTruong.SK_TRUNG || e.kieu == DauTruong.SK_MAT_MAU)) {
            hp = Math.max(0, hp - (int) (e.so * Math.min(1f, p / 0.6f)));
        } else if (e != null && e.ben == b && e.kieu == DauTruong.SK_HOI) {
            hp = Math.min(c.hpMax, hp + (int) (e.so * Math.min(1f, p / 0.6f)));
        }
        int wso = 0;
        if (b == 0) {
            String so = Math.max(0, hp) + "/" + c.hpMax;
            wso = ChuDe.rong(so, ChuDe.PHU) + 6;
            ChuDe.chuGiua(g, so, xt + wt, yb - 2, hb + 4, ChuDe.PHU, ChuDe.CHU, 1);
        }
        GiaoDien.thanhHP(g, xt, yb, wt - wso, hb, hp, c.hpMax);
    }

    static int ttMau(int tt) {
        switch (tt) {
            case 1:
                return KieuO.mauHe(HePoke.LUA);
            case 2:
                return KieuO.mauHe(HePoke.DOC);
            case 3:
                return KieuO.mauHe(HePoke.DIEN);
            case 4:
                return ChuDe.XAM;
            default:
                return KieuO.mauHe(HePoke.BANG);
        }
    }

    // ------------------------------------------------------------------ băng lượt + nút tốc độ (máy luôn tự đánh)

    static int[] tamNut = new int[4], tamBang = new int[4], tamDong = new int[4];
    static long khoaLuc;
    static int khoaCap, khoaToc;

    /** Băng nhỏ trên sân: lượt + tên sân. */
    static void veBangLuot(Graphics g, int y) {
        String s = (DauTruong.xemLai ? "Xem lại · " : !DauTruong.tuDong && !dangDien() && !DauTruong.xong ? "Chọn chiêu · " : "") + "Lượt " + Math.min(DauTruong.luotToiDa, Math.max(1, DauTruong.luot)) + "/"
                + DauTruong.luotToiDa + " · " + DauTruong.tenSan;
        int lp = ChuDe.cao(ChuDe.PHU);
        int w = Math.min(R - L - 20, ChuDe.rong(s, ChuDe.PHU) + 24), h = lp + 8;
        int x = (L + R - w) / 2;
        Bang.the(g, x, y, w, h);
        ChuDe.chuGiua(g, ChuDe.cat(s, ChuDe.PHU, w - 14), x + w / 2, y, h, ChuDe.PHU, ChuDe.CHU, 2);
    }

    /** Một nút tốc độ ×1 -> ×2 -> ×3 (×2 / ×3 mở theo cấp); bấm vào tốc độ đang khoá: hiện khoá + cấp cần. */
    void veNutToc(Graphics g, long t) {
        int sD = Math.max(VungCham.MIN, Bang.co(30));
        int rr = (sD + 1) / 2;
        int cx = R - sD - 10 - rr, cy = T + 4 + rr;
        int x = cx - rr, y = cy - rr, w = 2 * rr, s = 2 * rr;
        tamNut = new int[]{x, y, w, s};
        boolean an = !ketQuaHien && Bang.dangAnTron(g, cx, cy, rr);
        Bang.diaTrang(g, cx, cy + (an ? 1 : 0), rr - 1, an);
        ChuDe.chuVienGiua(g, "×" + DauTruong.toc, cx, y + (an ? 1 : 0), s, ChuDe.SO, ChuDe.CHINH_TOI, 0xFFFFFF, 2);
        if (!ketQuaHien) {
            VungCham.them(g, x, y, w, s, new IAction() {
                public void perform() {
                    int ke = DauTruong.tocKe();
                    if (ke > 0) {
                        DauTruong.datToc(ke);
                    } else {
                        khoaLuc = bay();
                        khoaCap = -ke;
                        khoaToc = DauTruong.toc >= 3 ? 1 : DauTruong.toc + 1;
                    }
                }
            }, "toc-do");
        }
        if (khoaLuc > 0 && t - khoaLuc < 1800) {
            String chu = "×" + khoaToc + " mở ở cấp " + khoaCap;
            int lp = ChuDe.cao(ChuDe.PHU);
            int is = Math.max(14, lp);
            int wc = ChuDe.rong(chu, ChuDe.PHU) + is + 22, hc = Math.max(is + 6, lp + 8);
            int xc = x - wc - 6, yc = y + (s - hc) / 2;
            Bang.the(g, xc, yc, wc, hc);
            IconBat.ve(g, IconBat.KHOA, xc + 6 + is / 2, yc + hc / 2, is);
            ChuDe.chuGiua(g, chu, xc + is + 12, yc, hc, ChuDe.PHU, ChuDe.CHU, 0);
        }
    }

    // ------------------------------------------------------------------ chế độ TỰ CHỌN CHIÊU (bật trong cài đặt Đấu trường)

    /** Bảng tự chọn: 4 nút chiêu (icon PNG, nhãn hệ, tên + uy lực, PP; hồi chiêu / khoá / hết PP = xám + lý do), 2 nút đổi
     *  (chân dung + HP), "Máy đánh", đồng hồ lượt. Mọi nút tắt khi đang diễn (một hành động mỗi lượt). */
    void veTuChon(Graphics g, int x, int y, int w, int h, int hn, boolean rong) {
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int x0 = x + 6, y0 = y + 6, w0 = w - 12, h0 = h - 12;
        boolean cho = DauTruong.choMayChu || dangDien() || DauTruong.xong || ketQuaHien;
        int wCot = rong ? Math.max(2 * hn + k, w0 * 24 / 100) : w0;
        int wm = rong ? w0 - wCot - 2 * k : w0;
        int hm = rong ? (h0 - k) / 2 : hn;
        int wn = (wm - k) / 2;
        boolean het = DauTruong.hetPp();
        for (int o = 0; o < 4; o++) {
            int xn = x0 + (o % 2) * (wn + k), yn = y0 + (o / 2) * (hm + k);
            veNutChieu(g, o, xn, yn, wn, hm, cho, het);
        }
        // cột phải: 2 nút dự bị (chân dung + HP) / đồng hồ tròn + nút Máy đánh
        int xc = rong ? x0 + wm + 2 * k : x0, yc = rong ? y0 : y0 + 2 * (hm + k);
        int hc = rong ? hm : hn;
        int wDoi = rong ? (wCot - k) / 2 : (w0 - 2 * hn - 3 * k) / 2;
        int nDoi = 0;
        for (int i = 0; i < 3; i++) {
            if (i == DauTruong.dangRa[0]) {
                continue;
            }
            final int ii = i;
            DauTruong.Con c = DauTruong.DOI[0][i];
            boolean song = c != null && c.hp > 0;
            int xb = xc + nDoi * (wDoi + k);
            Bang.nut(g, xb, yc, wDoi, hc, null, song && !cho ? Bang.PHU : Bang.TAT, new IAction() {
                public void perform() {
                    DauTruong.hanhDong(1, ii);
                }
            }, "doi-" + nDoi);
            if (c != null) {
                int sp = Math.min(wDoi - 12, hc - 18);
                PokeBody.paintChanDung(g, c.dex, c.shiny, xb + wDoi / 2, yc + 4 + sp / 2, sp);
                Bang.thanh(g, xb + 6, yc + hc - 11, wDoi - 12, 6, c.hp, c.hpMax, 0, null);
            }
            nDoi++;
        }
        int xg = rong ? xc : xc + 2 * (wDoi + k), yg = rong ? yc + hc + k : yc;
        // đồng hồ: vòng đếm ngược tròn (cao bằng hàng chiêu) + nút Máy đánh cùng chiều cao
        int wo = rong ? wDoi : hc;
        int dg = Math.min(hc, wo);
        int cxg = xg + wo / 2, cyg = yg + hc / 2;
        veVongGio(g, cxg, cyg, dg / 2, cho ? -1 : DauTruong.giayCon());
        int xa = xg + wo + k, wa = rong ? wCot - wo - k : hn;
        Bang.nut(g, xa, yg, wa, hc, "Máy", ketQuaHien ? Bang.TAT : Bang.GO, new IAction() {
            public void perform() {
                DauTruong.datTuDong(true);
            }
        }, "tu-dong");
    }

    /** Vòng đếm ngược lượt: đĩa tối + cung còn lại (đỏ khi <= 5 giây) + số giây giữa; giay < 0 = đang diễn ("..."). */
    static void veVongGio(Graphics g, int cx, int cy, int r, int giay) {
        int tong = Math.max(1, DauTruong.giayLuot);
        GiaoDien.khoi(g, cx - r, cy - r, 2 * r, 2 * r, r, ChuDe.VIEN);
        GiaoDien.khoi(g, cx - r + 2, cy - r + 2, 2 * r - 4, 2 * r - 4, r - 2, 0x3A2A1E);
        int deg = giay < 0 ? 360 : Math.max(0, Math.min(360, giay * 360 / tong));
        g.setColor(giay >= 0 && giay <= 5 ? ChuDe.DO : ChuDe.CHINH);
        int day = Math.max(3, r / 4);
        for (int i = 0; i < day; i++) {
            g.drawArc(cx - r + 3 + i, cy - r + 3 + i, 2 * r - 6 - 2 * i, 2 * r - 6 - 2 * i, 90, deg);
        }
        GiaoDien.khoiPixel(g, cx - r / 2, cy - r / 3, r, 2 * r / 3, 0x3A2A1E);
        ChuDe.chuVienGiua(g, giay < 0 ? "..." : String.valueOf(giay), cx, cy - r / 3, 2 * r / 3, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
    }

    /** Nút chiêu kiểu game chính: nền MÀU HỆ viền tối, icon PNG trái, tên, PP phải; hồi chiêu = huy hiệu số; không dùng
     *  được = nhạt màu + khoá. */
    void veNutChieu(Graphics g, int o, int x, int y, int w, int h, boolean cho, boolean het) {
        final int oo = o;
        DauTruong.Chieu c = DauTruong.chieu(o);
        if (c == null) {
            return;
        }
        boolean duoc = c.lyDo == 0 && c.pp > 0 || het;
        boolean bam = duoc && !cho;
        boolean an = bam && Bang.dangAn(g, x, y, w, h);
        int mau = KieuO.mauHe(c.he);
        if (!duoc) {
            mau = GiaoDien.pha(mau, 0x9A9AA2, 3, 4);
        } else if (cho) {
            mau = GiaoDien.pha(mau, 0x9A9AA2, 1, 3);
        }
        if (an) {
            mau = GiaoDien.pha(mau, 0, 1, 5);
        }
        GiaoDien.khoiPixel(g, x, y + (an ? 1 : 0), w, h, mau);
        int lp = ChuDe.cao(ChuDe.PHU), ln = ChuDe.cao(ChuDe.NHAN);
        int si = Math.min(h - 10, Bang.co(40));
        int xi = x + 6, yi = y + (h - si) / 2;
        boolean coIcon = veIconChieu(g, c.he, c.vai, xi, yi, si, duoc);
        if (!duoc) {
            IconBat.ve(g, IconBat.KHOA, xi + si - si / 4, yi + si - si / 4, Math.max(12, si / 2));
        }
        if (c.hoi > 0) {
            int d = Math.max(14, lp + 2);
            GiaoDien.khoiPixel(g, xi + si - d + 4, yi - 2, d, d, ChuDe.DO_TOI);
            ChuDe.chuVienGiua(g, String.valueOf(c.hoi), xi + si + 4 - d / 2, yi - 2, d, ChuDe.PHU, 0xFFFFFF, ChuDe.VIEN, 2);
        }
        int xt = x + 10 + (coIcon ? si + 6 : 0);
        String pp = c.pp + "/" + c.ppMax;
        int wpp = ChuDe.rong(pp, ChuDe.PHU);
        int wt = x + w - 10 - wpp - 8 - xt;
        String ten = c.ten + (c.uyLuc > 0 && wt > ChuDe.rong(c.ten + " · 120", ChuDe.NHAN) ? " · " + c.uyLuc : "");
        ChuDe.chuVienGiua(g, ChuDe.cat(ten, ChuDe.NHAN, wt), xt, y, h, ChuDe.NHAN, duoc ? 0xFFFFFF : 0xE4E4E8, ChuDe.VIEN, 0);
        ChuDe.chuVienGiua(g, pp, x + w - 10, y, h, ChuDe.PHU, c.pp > 0 ? 0xFFFFFF : 0xFFB0A8, ChuDe.VIEN, 1);
        if (bam) {
            Bang.dangKy(g, x, y, w, h, new IAction() {
                public void perform() {
                    DauTruong.hanhDong(0, oo);
                }
            }, "chieu-" + o);
        }
    }

    /** Nút đổi: chân dung + tên + thanh HP (hẹp: chỉ thanh HP dưới chân dung). */
    static void veDuBi(Graphics g, DauTruong.Con c, int xb, int yd, int w, int hn) {
        if (c == null) {
            return;
        }
        boolean song = c.hp > 0;
        int lp = ChuDe.cao(ChuDe.PHU);
        int sp = hn - 10;
        PokeBody.paintChanDung(g, c.dex, c.shiny, xb + 5 + sp / 2, yd + hn / 2, sp);
        int xt = xb + 10 + sp, wt = xb + w - 7 - xt;
        if (wt >= Bang.co(64)) {
            ChuDe.chuVien(g, ChuDe.cat(song ? c.ten : c.ten + " (gục)", ChuDe.PHU, wt), xt, yd + 4, ChuDe.PHU, 0xFFFFFF, ChuDe.VIEN, 0);
            Bang.thanh(g, xt, yd + 7 + lp, wt, Math.max(6, Math.min(12, hn - lp - 14)), c.hp, c.hpMax, 0, null);
        } else {
            Bang.thanh(g, xb + 4, yd + hn - 10, w - 8, 6, c.hp, c.hpMax, 0, null);
        }
    }

    /** Icon PNG chiêu loài (res/ui/chieu_loai_32|64.png, ô = hệ x 10 + vai trò; không dùng được = ô hệ Thường). false = J2ME. */
    static boolean veIconChieu(Graphics g, int he, int vai, int x, int y, int s, boolean duoc) {
        int co = s >= 56 ? 64 : 32;
        String ten = "/ui/chieu_loai_" + co + ".png";
        Image img = PokeBody.napAnhChung(ten);
        if (img == null || he < 0 || he > 17 || vai < 0 || vai > 9) {
            return false;
        }
        int f = Math.max(1, s / co);
        Image ve = anhPhong(img, ten, f);
        if (ve == null) {
            ve = img;
            f = 1;
        }
        int c = co * f;
        g.drawRegion(ve, vai * c, (duoc ? he : 0) * c, c, c, 0, x + (s - c) / 2, y + (s - c) / 2, Graphics.TOP | Graphics.LEFT);
        IconBat.demVe++;
        return true;
    }

    /** Kiểm thử: bố cục lần vẽ cuối - mọi bảng / băng / nút nằm trọn vùng an toàn, vùng đánh nhau còn chỗ, hai sprite trọn
     *  trong vùng đánh nhau. null = đạt, khác = lý do. */
    public static String loiBoCuc() {
        int[][] ds = {tamThu, tamCong, tamBang, tamNut};
        String[] ten = {"bang doi thu", "bang minh", "bang luot", "nut toc do"};
        for (int i = 0; i < ds.length; i++) {
            int[] r = ds[i];
            if (r[2] <= 0) {
                continue;
            }
            if (r[0] < L - 1 || r[1] < T - 1 || r[0] + r[2] > R + 1 || r[1] + r[3] > B + 1) {
                return ten[i] + " ra ngoai vung an toan " + r[0] + "," + r[1] + " " + r[2] + "x" + r[3] + " / " + L + "," + T + "-" + R + "," + B;
            }
        }
        if (zBot - zTop < coSprite(0) + coSprite(1) / 2) {
            return "vung danh nhau qua hep " + zTop + ".." + zBot;
        }
        if (bucThuBiChe()) {
            return "buc do doi thu bi bang HP che " + java.util.Arrays.toString(oBucThu());
        }
        for (int b = 0; b < 2; b++) {
            int sb = coSprite(b);
            if (!AN[b] && (X[b] - sb / 2 < L || X[b] + sb / 2 > R || Y[b] - sb < zTop - 2 || Y[b] > zBot + 2)) {
                return "sprite ben " + b + " ra ngoai vung danh nhau";
            }
        }
        return null;
    }

    /** Kiểm thử / chụp: sự kiện đang diễn + tiến độ. */
    public static DauTruong.SuKien suKienDang() {
        return dang;
    }

    public static float tienDoDang() {
        return tienDo(bay());
    }

    // ------------------------------------------------------------------ kết quả (tấm NRO trên cảnh đấu)

    void veKetQua(Graphics g) {
        Bang.phuNen(g);
        int lh = ChuDe.cao(ChuDe.TIEU_DE), ln = ChuDe.cao(ChuDe.NHAN), lp = ChuDe.cao(ChuDe.PHU);
        int hb = Math.max(VungCham.MIN, Bang.co(30));
        int k = Bang.cach();
        int hn = lh + 3 * ln + 2 * lp + 48 + hb + k;
        int wT = Bang.oTam(rongMuon(), 0)[2];
        int[] c = Bang.khungTam(g, wT, Bang.caoDauTam(false) + hn, tieuDe(), null, 0, null, new IAction() {
            public void perform() {
                ManTranDauTruong.veDauTruong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int cx = x + w / 2;
        int yy = y + 6;
        GiaoDien.vien(g, cx - w / 4, yy, w / 2, lh + 6, DauTruong.kqThang ? ChuDe.LA : ChuDe.DO, null);
        ChuDe.chuVienGiua(g, DauTruong.kqThang ? "THẮNG" : "THUA", cx, yy, lh + 6, ChuDe.TIEU_DE, 0xFFFFFF, ChuDe.VIEN, 2);
        yy += lh + 12;
        Bang.the(g, x, yy, w, 3 * ln + 2 * lp + 24);
        yy += 6;
        int d = DauTruong.kqDiemSau - DauTruong.kqDiemTruoc;
        ChuDe.chu(g, "Điểm " + DauTruong.kqDiemTruoc + " thành " + DauTruong.kqDiemSau + " (" + (d >= 0 ? "+" : "") + d + ")", cx, yy, ChuDe.NHAN,
                d >= 0 ? ChuDe.LA_TOI : ChuDe.DO_TOI, 2);
        yy += ln + 4;
        ChuDe.chu(g, DauTruong.kqLuong > 0 ? "+" + DauTruong.kqLuong + " lượng khoá" : "Không có lượng khoá trận này", cx, yy, ChuDe.NHAN, ChuDe.CHU, 2);
        yy += ln + 4;
        ChuDe.chu(g, ChuDe.cat(DauTruong.kqChu, ChuDe.PHU, w - 20), cx, yy, ChuDe.PHU, ChuDe.CHU_PHU, 2);
        yy += lp + 2;
        ChuDe.chu(g, "Còn " + DauTruong.kqLuotCon + " lượt hôm nay", cx, yy, ChuDe.PHU, ChuDe.CHU_PHU, 2);
        int wb = (w - k) / 2;
        Bang.nut(g, x, y + h - hb, wb, hb, "Về Đấu trường", Bang.CHINH, new IAction() {
            public void perform() {
                veDauTruong();
            }
        }, "kq-ve");
        Bang.nut(g, x + wb + k, y + h - hb, w - wb - k, hb, "Về bản đồ", Bang.TRUNG, new IAction() {
            public void perform() {
                DauTruong.tranId = 0;
                Canvas.gameScr.switchToMe();
            }
        }, "kq-dong");
    }
}
