package game.pokemon;

import javax.microedition.lcdui.Graphics;

/**
 * BỘ BIỂU TƯỢNG PHẲNG 0.9.5 (một kiểu cho Menu, HUD, ô trang bị, thuốc...): mỗi biểu tượng là danh sách lệnh vẽ trong
 * hộp 100 x 100 (hình tròn, chữ nhật bo góc, đa giác, nét), vẽ ĐÚNG CỠ (không phóng ảnh) nên sắc nét ở mọi tỉ lệ:
 * bản PC / APK vẽ vector khử răng cưa (ChuUI.hinh), máy J2ME vẽ bằng fillArc / fillRoundRect / fillTriangle.
 *
 * Màu lệnh: CHINH (màu biểu tượng), TOI (đậm hơn), NHAT (nhạt hơn), NEN (màu nền dưới biểu tượng - đục lỗ), TRANG.
 */
public final class BieuTuong {
    private BieuTuong() {
    }

    // lệnh
    public static final int TRON = 0, CHU_NHAT = 1, NET = 2, DA_GIAC = 3, VONG = 4, MAU = 5, CUNG = 6, DUONG = 7;
    // màu
    public static final int M_CHINH = 0, M_TRANG = 1, M_TOI = 2, M_NHAT = 3, M_NEN = 4;

    public static final int POKEBALL = 0, TUI = 1, NHIEM_VU = 2, BAN_DO = 3, DEX = 4, DOI = 5, SU_KIEN = 6, SINH_THAI = 7,
            CUA_HANG = 8, THU = 9, XEP_HANG = 10, CAI_DAT = 11, TIEN_ICH = 12, CHAT = 13, THUOC = 14, MU = 15, VONG_CO = 16,
            NHAN = 17, GIAY = 18, DAI = 19, GANG = 20, AO = 21, DA = 22, LONG_VU = 23, HUY_HIEU = 24, KHAN = 25, CHIEU = 26,
            QUA = 27, NGUYEN_LIEU = 28, PP = 29, TU_DONG = 30, DOI_BO = 31,
            // biểu tượng HỆ (nút chiêu: không có ảnh từng chiêu -> huy hiệu hệ)
            H_THUONG = 32, H_LUA = 33, H_NUOC = 34, H_DIEN = 35, H_CO = 36, H_DOC = 37, H_SIEU_LINH = 38, H_MA = 39, H_RONG = 40,
            H_BONG_TOI = 41, H_THEP = 42, H_DAT = 43, H_BANG = 44,
            // thông báo / điều khiển (0.9.5): máy ảnh (album), xu, thông tin, cảnh báo, lỗi, đóng (X), lùi, hộp đồ
            CAMERA = 45, XU = 46, TIN = 47, CANH_BAO = 48, LOI = 49, DONG = 50, LUI = 51, HOP_DO = 52,
            // 0.9.6.1: cần câu (nút câu cạnh hồ). 0.9.7 (icon pixel NRO, chỉ có ảnh /nro/icon.png - không có lệnh vector):
            // thú cưng (bàn chân), ổ khoá, mở rộng (+), lượng (ngọc), vứt, trận đấu, bạn bè, thoát
            CAN_CAU = 53, THU_CUNG = 54, KHOA = 55, MO_RONG = 56, LUONG = 57, VUT = 58, TRAN = 59, BAN_BE = 60, THOAT = 61,
            // cài đặt bản PC: màn hình, loa, bàn phím, chuột (trái / phải / lăn), phím, tài khoản
            MAN_HINH = 62, LOA = 63, BAN_PHIM = 64, CHUOT_TRAI = 65, CHUOT_PHAI = 66, CHUOT_LAN = 67, PHIM = 68, TAI_KHOAN = 69;

    /** số id biểu tượng (kể cả id chỉ có icon pixel) */
    public static final int SO = 70;

    static final float[][] HINH = new float[54][];

    /** Biểu tượng hệ theo số hệ (PokeChieu.he / KieuO.mauHe). */
    public static int heIcon(int he) {
        switch (he) {
            case 1:
                return H_LUA;
            case 2:
                return H_NUOC;
            case 3:
                return H_DIEN;
            case 4:
                return H_CO;
            case 5:
                return H_BANG;
            case 7:
                return H_DOC;
            case 8:
                return H_DAT;
            case 10:
                return H_SIEU_LINH;
            case 13:
                return H_MA;
            case 14:
                return H_RONG;
            case 15:
                return H_BONG_TOI;
            case 16:
                return H_THEP;
            default:
                return H_THUONG;
        }
    }

    static float[] sao(float cx, float cy, float r1, float r2, int n, float lech) {
        float[] p = new float[2 + n * 4];
        p[0] = DA_GIAC;
        p[1] = n * 2;
        for (int i = 0; i < n * 2; i++) {
            double a = Math.PI * i / n + lech;
            float r = i % 2 == 0 ? r1 : r2;
            p[2 + i * 2] = cx + (float) (Math.sin(a) * r);
            p[3 + i * 2] = cy - (float) (Math.cos(a) * r);
        }
        return p;
    }

    static float[] noi(float[][] ds) {
        int n = 0;
        for (int i = 0; i < ds.length; i++) {
            n += ds[i].length;
        }
        float[] r = new float[n];
        int k = 0;
        for (int i = 0; i < ds.length; i++) {
            System.arraycopy(ds[i], 0, r, k, ds[i].length);
            k += ds[i].length;
        }
        return r;
    }

    static {
        float c = M_CHINH, t = M_TRANG, d = M_TOI, l = M_NHAT, n = M_NEN;
        HINH[TUI] = new float[]{MAU, c, CHU_NHAT, 16, 32, 68, 56, 14, MAU, d, CHU_NHAT, 16, 32, 68, 22, 12, MAU, c, CUNG, 50, 32, 15, 0, 180, 8,
            MAU, n, CHU_NHAT, 41, 47, 18, 13, 4};
        HINH[NHIEM_VU] = new float[]{MAU, c, CHU_NHAT, 22, 12, 56, 76, 10, MAU, d, CHU_NHAT, 16, 10, 68, 14, 7, CHU_NHAT, 16, 76, 68, 14, 7,
            MAU, n, CHU_NHAT, 44, 30, 12, 30, 6, TRON, 50, 70, 7};
        HINH[BAN_DO] = new float[]{MAU, c, DA_GIAC, 8, 12, 24, 37, 14, 63, 24, 88, 14, 88, 76, 63, 86, 37, 76, 12, 86,
            MAU, d, NET, 37, 14, 37, 76, 5, NET, 63, 24, 63, 86, 5, MAU, n, TRON, 50, 44, 11, MAU, c, TRON, 50, 44, 5};
        HINH[DEX] = new float[]{MAU, c, CHU_NHAT, 22, 8, 56, 84, 12, MAU, n, TRON, 50, 36, 16, MAU, l, TRON, 50, 36, 10,
            MAU, n, CHU_NHAT, 33, 62, 34, 7, 4, CHU_NHAT, 33, 74, 22, 7, 4};
        HINH[DOI] = new float[]{MAU, c, TRON, 33, 34, 13, TRON, 67, 34, 13, CHU_NHAT, 10, 54, 46, 32, 16, CHU_NHAT, 44, 54, 46, 32, 16};
        HINH[SU_KIEN] = noi(new float[][]{{MAU, c}, sao(50, 52, 44, 19, 5, 0)});
        HINH[SINH_THAI] = new float[]{MAU, c, DA_GIAC, 9, 14, 86, 22, 40, 48, 18, 78, 14, 88, 14, 86, 44, 72, 72, 46, 86, 24, 80,
            MAU, n, NET, 22, 78, 64, 34, 5};
        HINH[CUA_HANG] = new float[]{MAU, c, CHU_NHAT, 16, 42, 68, 46, 7, MAU, d, CHU_NHAT, 10, 14, 80, 28, 8, MAU, n,
            CHU_NHAT, 26, 14, 12, 28, 0, CHU_NHAT, 50, 14, 12, 28, 0, MAU, n, CHU_NHAT, 41, 58, 18, 30, 4};
        HINH[THU] = new float[]{MAU, c, CHU_NHAT, 10, 22, 80, 58, 10, MAU, n, DUONG, 3, 6, 18, 32, 50, 56, 82, 32};
        HINH[XEP_HANG] = new float[]{MAU, c, CHU_NHAT, 28, 10, 44, 42, 20, CHU_NHAT, 28, 10, 44, 20, 4, CHU_NHAT, 44, 48, 12, 22, 3,
            CHU_NHAT, 30, 74, 40, 14, 5, CUNG, 28, 28, 13, 90, 180, 7, CUNG, 72, 28, 13, 270, 180, 7, MAU, n, CHU_NHAT, 38, 20, 7, 18, 3};
        HINH[CAI_DAT] = noi(new float[][]{{MAU, c}, sao(50, 50, 44, 34, 8, (float) (Math.PI / 16)), {TRON, 50, 50, 31, MAU, n, TRON, 50, 50, 13}});
        HINH[TIEN_ICH] = new float[]{MAU, c, CHU_NHAT, 14, 14, 32, 32, 9, CHU_NHAT, 54, 14, 32, 32, 9, CHU_NHAT, 14, 54, 32, 32, 9,
            MAU, d, CHU_NHAT, 54, 54, 32, 32, 9};
        HINH[CHAT] = new float[]{MAU, c, CHU_NHAT, 8, 14, 84, 58, 22, DA_GIAC, 3, 26, 64, 20, 90, 48, 68, MAU, n, TRON, 30, 43, 6, TRON, 50, 43, 6,
            TRON, 70, 43, 6};
        HINH[THUOC] = new float[]{MAU, d, CHU_NHAT, 38, 6, 24, 14, 4, MAU, c, CHU_NHAT, 40, 18, 20, 18, 3, CHU_NHAT, 22, 30, 56, 62, 20,
            MAU, n, CHU_NHAT, 33, 42, 8, 26, 4, MAU, l, CHU_NHAT, 22, 62, 56, 30, 18};
        HINH[PP] = HINH[THUOC];
        HINH[MU] = new float[]{MAU, c, CUNG, 50, 62, 32, 0, 180, 0,
            MAU, d, CHU_NHAT, 8, 58, 84, 12, 6, MAU, c, CHU_NHAT, 40, 22, 20, 8, 4};
        HINH[VONG_CO] = new float[]{MAU, c, CUNG, 50, 34, 30, 180, 180, 7, MAU, d, TRON, 50, 72, 14, MAU, l, TRON, 46, 68, 5};
        HINH[NHAN] = new float[]{MAU, c, VONG, 50, 60, 26, 9, MAU, d, DA_GIAC, 4, 36, 30, 50, 10, 64, 30, 50, 42, MAU, l, TRON, 50, 26, 5};
        HINH[GIAY] = new float[]{MAU, c, DA_GIAC, 7, 22, 16, 46, 16, 48, 52, 86, 62, 88, 82, 14, 82, 14, 50, MAU, d, CHU_NHAT, 12, 78, 78, 10, 4};
        HINH[DAI] = new float[]{MAU, c, CHU_NHAT, 6, 38, 88, 24, 8, MAU, d, CHU_NHAT, 38, 30, 24, 40, 6, MAU, n, CHU_NHAT, 45, 38, 10, 24, 3};
        HINH[GANG] = new float[]{MAU, c, CHU_NHAT, 22, 34, 56, 48, 16, CHU_NHAT, 22, 18, 12, 30, 6, CHU_NHAT, 36, 12, 12, 30, 6,
            CHU_NHAT, 50, 12, 12, 30, 6, CHU_NHAT, 64, 18, 12, 30, 6, MAU, d, CHU_NHAT, 26, 74, 48, 16, 5};
        HINH[AO] = new float[]{MAU, c, DA_GIAC, 8, 32, 12, 68, 12, 92, 32, 80, 48, 72, 42, 72, 88, 28, 88, 28, 42, MAU, d, DA_GIAC, 3, 38, 12, 62, 12, 50, 26};
        HINH[DA] = new float[]{MAU, c, DA_GIAC, 6, 50, 8, 84, 34, 74, 82, 26, 82, 16, 34, 50, 8, MAU, l, DA_GIAC, 3, 50, 8, 84, 34, 50, 40,
            MAU, d, DA_GIAC, 3, 50, 40, 74, 82, 26, 82};
        HINH[LONG_VU] = new float[]{MAU, c, DA_GIAC, 6, 80, 10, 88, 22, 52, 70, 34, 74, 36, 56, 70, 14, MAU, d, NET, 80, 16, 22, 88, 5};
        HINH[HUY_HIEU] = noi(new float[][]{{MAU, c}, sao(50, 48, 42, 30, 8, 0), {MAU, l}, sao(50, 48, 24, 12, 4, 0)});
        HINH[KHAN] = new float[]{MAU, c, CHU_NHAT, 12, 20, 76, 24, 12, DA_GIAC, 4, 56, 36, 76, 36, 72, 88, 52, 84, MAU, d, CHU_NHAT, 42, 22, 18, 22, 6};
        HINH[CHIEU] = new float[]{MAU, c, DA_GIAC, 6, 58, 6, 18, 54, 46, 54, 38, 94, 82, 40, 54, 40};
        HINH[QUA] = new float[]{MAU, c, CHU_NHAT, 14, 40, 72, 48, 8, MAU, d, CHU_NHAT, 10, 28, 80, 18, 6, MAU, n, CHU_NHAT, 44, 28, 12, 60, 0,
            MAU, c, VONG, 38, 20, 10, 6, VONG, 62, 20, 10, 6};
        HINH[NGUYEN_LIEU] = HINH[DA];
        HINH[TU_DONG] = HINH[CHIEU];
        // hệ (vẽ trắng trên thân màu hệ)
        HINH[H_THUONG] = new float[]{MAU, c, VONG, 50, 50, 30, 12, TRON, 50, 50, 10};
        HINH[H_LUA] = new float[]{MAU, c, DA_GIAC, 9, 50, 6, 70, 34, 78, 56, 70, 80, 50, 92, 30, 80, 22, 56, 34, 40, 44, 22,
            MAU, n, DA_GIAC, 6, 50, 46, 60, 60, 62, 74, 50, 84, 38, 74, 40, 60};
        HINH[H_NUOC] = new float[]{MAU, c, DA_GIAC, 3, 50, 6, 78, 56, 22, 56, TRON, 50, 62, 28, MAU, n, CHU_NHAT, 34, 58, 10, 20, 5};
        HINH[H_DIEN] = HINH[CHIEU];
        HINH[H_CO] = HINH[SINH_THAI];
        HINH[H_DOC] = new float[]{MAU, c, TRON, 40, 58, 26, TRON, 68, 34, 16, TRON, 70, 76, 12};
        HINH[H_SIEU_LINH] = new float[]{MAU, c, VONG, 50, 50, 34, 10, TRON, 50, 50, 14, MAU, n, TRON, 50, 50, 6};
        HINH[H_MA] = new float[]{MAU, c, CHU_NHAT, 22, 14, 56, 60, 28, DA_GIAC, 6, 22, 60, 78, 60, 78, 90, 64, 78, 50, 90, 36, 78,
            DA_GIAC, 3, 36, 78, 22, 90, 22, 60, MAU, n, TRON, 40, 42, 7, TRON, 60, 42, 7};
        HINH[H_RONG] = new float[]{MAU, c, DA_GIAC, 7, 20, 20, 60, 10, 84, 40, 66, 52, 82, 86, 46, 64, 26, 70, MAU, n, TRON, 52, 30, 6};
        HINH[H_BONG_TOI] = new float[]{MAU, c, TRON, 48, 50, 36, MAU, n, TRON, 64, 40, 30};
        HINH[H_THEP] = noi(new float[][]{{MAU, c}, sao(50, 50, 42, 34, 6, 0), {MAU, n, TRON, 50, 50, 14}});
        HINH[H_DAT] = new float[]{MAU, c, DA_GIAC, 3, 10, 84, 40, 30, 70, 84, DA_GIAC, 3, 46, 84, 70, 46, 92, 84};
        HINH[H_BANG] = noi(new float[][]{{MAU, c, NET, 50, 10, 50, 90, 10, NET, 16, 30, 84, 70, 10, NET, 84, 30, 16, 70, 10}});
        HINH[CAMERA] = new float[]{MAU, c, CHU_NHAT, 8, 30, 84, 56, 14, CHU_NHAT, 32, 16, 36, 22, 8, MAU, n, TRON, 50, 58, 21,
            MAU, c, TRON, 50, 58, 13, MAU, n, TRON, 78, 42, 5};
        HINH[XU] = noi(new float[][]{{MAU, c, TRON, 50, 50, 42, MAU, d, VONG, 50, 50, 31, 6, MAU, n}, sao(50, 50, 18, 8, 5, 0)});
        HINH[TIN] = new float[]{MAU, c, TRON, 50, 50, 44, MAU, n, TRON, 50, 28, 8, CHU_NHAT, 43, 42, 14, 36, 6};
        HINH[CANH_BAO] = new float[]{MAU, c, DA_GIAC, 3, 50, 6, 96, 90, 4, 90, MAU, n, CHU_NHAT, 44, 34, 12, 32, 6, TRON, 50, 77, 7};
        HINH[LOI] = new float[]{MAU, c, TRON, 50, 50, 44, MAU, n, NET, 32, 32, 68, 68, 13, NET, 68, 32, 32, 68, 13};
        HINH[DONG] = new float[]{MAU, c, NET, 20, 20, 80, 80, 18, NET, 80, 20, 20, 80, 18};
        HINH[LUI] = new float[]{MAU, c, DUONG, 3, 18, 66, 14, 30, 50, 66, 86};
        HINH[HOP_DO] = new float[]{MAU, c, CHU_NHAT, 14, 32, 72, 56, 8, MAU, d, CHU_NHAT, 8, 20, 84, 22, 7, MAU, n, CHU_NHAT, 44, 20, 12, 68, 0};
        // cần câu chéo + guồng + dây + phao (nửa trên màu chính, nửa dưới trắng)
        HINH[CAN_CAU] = new float[]{MAU, d, NET, 14, 88, 66, 14, 10, MAU, c, NET, 16, 86, 64, 16, 6, MAU, d, TRON, 30, 68, 11,
            MAU, l, TRON, 30, 68, 6, MAU, d, NET, 66, 14, 80, 56, 3, MAU, d, TRON, 80, 70, 13, MAU, t, TRON, 80, 70, 10,
            MAU, c, CUNG, 80, 70, 10, 0, 180, 10};
        HINH[DOI_BO] = new float[]{MAU, c, CUNG, 50, 50, 30, 30, 260, 9, DA_GIAC, 3, 78, 14, 86, 44, 56, 36};
    }

    /** Màu lệnh -> RGB. */
    static int mau(int m, int chinh, int nen) {
        switch (m) {
            case M_TRANG:
                return 0xFFFFFF;
            case M_TOI:
                return GiaoDien.pha(chinh, 0x000000, 1, 4);
            case M_NHAT:
                return GiaoDien.pha(chinh, 0xFFFFFF, 2, 5);
            case M_NEN:
                return nen;
            default:
                return chinh;
        }
    }

    /**
     * Vẽ biểu tượng id cỡ s (hộp vuông góc trái trên (x, y)), màu chính mau, màu nền dưới nen (cho phần "đục lỗ").
     */
    public static void ve(Graphics g, int id, int x, int y, int s, int mau, int nen) {
        // 0.9.7: icon PIXEL đủ màu (KhungNro, phóng hệ số nguyên) - mọi bản; thiếu ảnh mới vẽ vector cũ
        if (KhungNro.icon(g, id, x, y, s, -1)) {
            return;
        }
        if (id == POKEBALL) {
            if (ChuUI.bo != null && ChuUI.bo.hinh(g, AnhUI.BONG, x, y, s, mau, nen)) {
                return;
            }
            GiaoDien.pokeBall(g, x + s / 2, y + s / 2, Math.max(3, s * 9 / 20), 0);
            return;
        }
        float[] h = id >= 0 && id < HINH.length ? HINH[id] : null;
        if (h == null) {
            return;
        }
        GhiChu.bieu(g, "bieu-tuong-" + id, x, y, s, s);   // 0.9.7 kiểm: biểu tượng trong màn / không bị chữ đè
        ChuUI b = ChuUI.bo;
        if (b != null && b.hinh(g, h, x, y, s, mau, nen)) {
            return;
        }
        veJ2me(g, h, x, y, s, mau, nen);
    }

    /** Bóng mờ một màu của biểu tượng id (ô trống: mũ, găng... mờ) - 0.9.7 icon pixel tô một màu. */
    public static void veBong(Graphics g, int id, int x, int y, int s, int mau, int nen) {
        if (KhungNro.icon(g, id, x, y, s, mau)) {
            return;
        }
        ve(g, id, x, y, s, mau, nen);
    }

    /** Vẽ bằng lệnh J2ME (không khử răng cưa). */
    static void veJ2me(Graphics g, float[] h, int x, int y, int s, int chinh, int nen) {
        VeTay.ghi("BieuTuong vector");
        int i = 0;
        while (i < h.length) {
            int lenh = (int) h[i++];
            switch (lenh) {
                case MAU:
                    g.setColor(mau((int) h[i++], chinh, nen));
                    break;
                case TRON: {
                    int cx = x + (int) (h[i] * s / 100), cy = y + (int) (h[i + 1] * s / 100), r = Math.max(1, (int) (h[i + 2] * s / 100));
                    g.fillArc(cx - r, cy - r, 2 * r, 2 * r, 0, 360);
                    i += 3;
                    break;
                }
                case CHU_NHAT: {
                    int rx = x + (int) (h[i] * s / 100), ry = y + (int) (h[i + 1] * s / 100);
                    int rw = Math.max(1, (int) (h[i + 2] * s / 100)), rh = Math.max(1, (int) (h[i + 3] * s / 100)), rr = (int) (h[i + 4] * s / 50);
                    g.fillRoundRect(rx, ry, rw, rh, rr, rr);
                    i += 5;
                    break;
                }
                case NET:
                case VONG:
                case CUNG: {
                    int day = 1;
                    if (lenh == NET) {
                        day = Math.max(1, (int) (h[i + 4] * s / 100));
                        int x1 = x + (int) (h[i] * s / 100), y1 = y + (int) (h[i + 1] * s / 100), x2 = x + (int) (h[i + 2] * s / 100), y2 = y + (int) (h[i + 3] * s / 100);
                        for (int k = -day / 2; k <= day / 2; k++) {
                            g.drawLine(x1 + k, y1, x2 + k, y2);
                        }
                        i += 5;
                    } else if (lenh == VONG) {
                        int cx = x + (int) (h[i] * s / 100), cy = y + (int) (h[i + 1] * s / 100), r = (int) (h[i + 2] * s / 100);
                        day = Math.max(1, (int) (h[i + 3] * s / 100));
                        for (int k = 0; k < day; k++) {
                            g.drawArc(cx - r + k, cy - r + k, 2 * (r - k), 2 * (r - k), 0, 360);
                        }
                        i += 4;
                    } else {
                        int cx = x + (int) (h[i] * s / 100), cy = y + (int) (h[i + 1] * s / 100), r = (int) (h[i + 2] * s / 100);
                        int a0 = (int) h[i + 3], a1 = (int) h[i + 4];
                        day = (int) (h[i + 5] * s / 100);
                        if (day <= 0) {
                            g.fillArc(cx - r, cy - r, 2 * r, 2 * r, a0, a1);
                        } else {
                            for (int k = 0; k < day; k++) {
                                g.drawArc(cx - r + k, cy - r + k, 2 * (r - k), 2 * (r - k), a0, a1);
                            }
                        }
                        i += 6;
                    }
                    break;
                }
                case DA_GIAC: {
                    int n = (int) h[i++];
                    int x0 = x + (int) (h[i] * s / 100), y0 = y + (int) (h[i + 1] * s / 100);
                    for (int k = 1; k + 1 < n; k++) {
                        g.fillTriangle(x0, y0, x + (int) (h[i + 2 * k] * s / 100), y + (int) (h[i + 2 * k + 1] * s / 100),
                                x + (int) (h[i + 2 * k + 2] * s / 100), y + (int) (h[i + 2 * k + 3] * s / 100));
                    }
                    i += 2 * n;
                    break;
                }
                case DUONG: {
                    int n = (int) h[i++];
                    int day = Math.max(1, (int) (h[i++] * s / 100));
                    for (int k = 0; k + 1 < n; k++) {
                        int x1 = x + (int) (h[i + 2 * k] * s / 100), y1 = y + (int) (h[i + 2 * k + 1] * s / 100);
                        int x2 = x + (int) (h[i + 2 * k + 2] * s / 100), y2 = y + (int) (h[i + 2 * k + 3] * s / 100);
                        for (int q = -day / 2; q <= day / 2; q++) {
                            g.drawLine(x1, y1 + q, x2, y2 + q);
                        }
                    }
                    i += 2 * n;
                    break;
                }
                default:
                    return;
            }
        }
    }
}
