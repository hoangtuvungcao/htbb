package game.pokemon;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * VẼ THỜI TIẾT + PHỦ MÀU MÙA (đề xuất 8, 0.9.7) trên thế giới - chỉ lớp phủ / hạt, KHÔNG đổi kiểu bảng (giao diện NRO
 * vẽ bảng riêng). Gọi từ GameScr.paint sau thế giới + hiệu ứng, TRƯỚC phủ đêm và HUD (chữ HUD không bị phủ).
 *
 * <p>Rẻ cho J2ME: không cấp phát mỗi khung, hạt tất định theo Canvas.gameTick (bảng hạt giống dựng một lần), mỗi hạt là
 * một fillRect / drawLine. Chất lượng ({@link ThoiTiet#chatLuong}): TẮT = không hạt, THẤP = {@link #HAT_THAP} hạt và
 * không phủ toàn màn của thời tiết, ĐẦY ĐỦ = {@link #HAT_DAY_DU} hạt + phủ. Phủ màu mùa alpha thấp (tối đa
 * {@link #ALPHA_MUA_TOI_DA} ~ 17%), tắt được riêng ({@link ThoiTiet#mauMuaBat}); trong nhà / hang máy chủ gửi mùa 0.
 */
public final class ThoiTietVe {
    private ThoiTietVe() {
    }

    /** Mã thời tiết = HTBB SinhThaiTheGioi. */
    public static final int QUANG = 0, NANG = 1, MUA = 2, TUYET = 3, BAO_CAT = 4, SUONG_MU = 5, GIO = 6;
    /** Mã phủ mùa = HTBB ThoiTietLich.MAU_*. */
    public static final int MAU_KHONG = 0, MAU_XUAN = 1, MAU_HA = 2, MAU_THU = 3, MAU_DONG = 4, MAU_HALLOWEEN = 5,
            MAU_TET = 6, MAU_NOEL = 7, MAU_TRUNG_THU = 8;
    public static final int HAT_THAP = 24, HAT_DAY_DU = 72, HAT_TOI_DA = 160;
    /** Alpha phủ mùa tối đa (0..255): không làm khó đọc bản đồ / tên. */
    public static final int ALPHA_MUA_TOI_DA = 0x2C;

    /** {rgb, alpha} phủ mùa theo mã. */
    static final int[][] PHU_MUA = {
        {0, 0},
        {0xFFB7D5, 0x1C},   // xuân: hồng hoa anh đào
        {0xFFE08A, 0x12},   // hạ: nắng vàng
        {0xFF9A3C, 0x20},   // thu: cam lá phong
        {0xBFD9FF, 0x20},   // đông: xanh lạnh
        {0x5A2A7A, 0x2C},   // Halloween: hoàng hôn tím
        {0xFFC860, 0x18},   // Tết: đỏ vàng ấm
        {0xCFE6FF, 0x1C},   // Noel: tuyết lạnh
        {0xFFB45A, 0x1C},   // Trung Thu: ánh đèn lồng
    };
    /** Màu hạt mùa (cánh hoa / lá / tàn lửa / pháo giấy / lấp lánh / đèn), 0 = không hạt. */
    static final int[][] HAT_MUA = {
        {}, {0xFFC4DC, 0xFF9EC4}, {}, {0xF07A28, 0xD8502A, 0xE8B030}, {0xFFFFFF}, {0xFF9A3C, 0xB070F8},
        {0xE8412C, 0xFFD24A}, {0xFFFFFF, 0xD8F0FF}, {0xFFB45A, 0xFF7A30},
    };

    private static int[] hx, hy, hk;

    /** Hạt giống tất định (một lần, ~1 KB). */
    static void dungHat() {
        if (hx != null) {
            return;
        }
        hx = new int[HAT_TOI_DA];
        hy = new int[HAT_TOI_DA];
        hk = new int[HAT_TOI_DA];
        int s = 0x2545F491;
        for (int i = 0; i < HAT_TOI_DA; i++) {
            s = s * 1103515245 + 12345;
            hx[i] = (s >>> 8) & 0xFFFF;
            s = s * 1103515245 + 12345;
            hy[i] = (s >>> 8) & 0xFFFF;
            s = s * 1103515245 + 12345;
            hk[i] = (s >>> 8) & 0xFF;
        }
    }

    /** Số hạt theo chất lượng. */
    public static int soHat(int chatLuong) {
        return soHat(chatLuong, 320, 240);
    }

    /**
     * Số hạt theo chất lượng và cỡ màn logic: Đầy đủ giữ mật độ của màn 320x240 (72 hạt), tối đa {@link #HAT_TOI_DA};
     * Thấp cố định {@link #HAT_THAP} (máy J2ME màn nhỏ).
     */
    public static int soHat(int chatLuong, int w, int h) {
        if (chatLuong >= ThoiTiet.DAY_DU) {
            int n = (int) ((long) HAT_DAY_DU * Math.max(1, w) * Math.max(1, h) / (320L * 240L));
            return Math.max(HAT_DAY_DU, Math.min(HAT_TOI_DA, n));
        }
        return chatLuong == ThoiTiet.THAP ? HAT_THAP : 0;
    }

    /** Đếm lệnh vẽ của khung vừa rồi (kiểm thử: đường rẻ cho J2ME). */
    public static int demLenh;

    /** Vẽ phủ mùa + thời tiết cả màn (thế giới). */
    public static void veTheGioi(Graphics g) {
        demLenh = 0;
        if (!ThoiTiet.co || game.model.Tilemap.isOfflineMap && !ThoiTiet.thu) {
            return;
        }
        try {
            ve(g, Canvas.w, Canvas.h, Canvas.gameTick, ThoiTiet.thoiTiet, ThoiTiet.mauMuaBat ? ThoiTiet.mauMua : MAU_KHONG,
                    ThoiTiet.chatLuong);
        } catch (Throwable e) {
            // thiếu bộ nhớ ảnh phủ: bỏ khung này
        }
    }

    /** Vẽ cho (w, h, tick) - tách để kiểm thử / ảnh mẫu. */
    public static void ve(Graphics g, int w, int h, int t, int thoiTiet, int mau, int chatLuong) {
        demLenh = 0;
        Canvas.resetTrans(g);
        dungHat();
        int n = soHat(chatLuong, w, h);
        boolean phu = chatLuong >= ThoiTiet.DAY_DU;
        // 1. phủ mùa (dưới hạt thời tiết)
        if (mau > MAU_KHONG && mau < PHU_MUA.length) {
            int a = Math.min(ALPHA_MUA_TOI_DA, PHU_MUA[mau][1]);
            SinhThai.phuLat(g, 0, 0, w, h, a << 24 | PHU_MUA[mau][0]);
            demLenh++;
            if (mau == MAU_HALLOWEEN && phu) {
                // quầng cam chân trời (dải dưới) - hoàng hôn
                SinhThai.phuLat(g, 0, h - h / 4, w, h / 4, 0x14 << 24 | 0xFF8A30);
                demLenh++;
            }
            int[] mh = HAT_MUA[mau];
            if (mh.length > 0 && n > 0 && thoiTiet != MUA && thoiTiet != BAO_CAT) {
                veHatMua(g, w, h, t, mau, mh, phu ? n / 2 : n / 3);
            }
        }
        // 2. thời tiết
        switch (thoiTiet) {
            case MUA:
                veMua(g, w, h, t, n, phu);
                break;
            case TUYET:
                veTuyet(g, w, h, t, n, phu);
                break;
            case SUONG_MU:
                if (phu) {
                    SinhThai.veSuongMu(g, 256);
                    demLenh += 9;
                } else if (n > 0) {
                    SinhThai.phuLat(g, 0, 0, w, h, 0x30 << 24 | 0xC8D0DA);
                    demLenh++;
                }
                break;
            case NANG:
                if (phu) {
                    SinhThai.veNangGat(g, 256);
                    demLenh += 4 + 4 * (h / 4 + 1);
                } else if (n > 0) {
                    g.setColor(0xFFF0A0);
                    g.fillArc(w - 14, -14, 28, 28, 0, 360);
                    demLenh++;
                }
                break;
            case BAO_CAT:
                veBaoCat(g, w, h, t, n, phu);
                break;
            case GIO:
                veGio(g, w, h, t, n);
                break;
            default:
                break;
        }
    }

    static int mod(int a, int m) {
        int r = a % m;
        return r < 0 ? r + m : r;
    }

    static void veMua(Graphics g, int w, int h, int t, int n, boolean phu) {
        if (n <= 0) {
            return;
        }
        if (phu) {
            SinhThai.phuLat(g, 0, 0, w, h, 0x18 << 24 | 0x30486A);   // trời âm u nhẹ
            demLenh++;
        }
        for (int i = 0; i < n; i++) {
            int sp = 9 + (hk[i] & 7);
            int x = mod(hx[i] - t * sp / 4, w + 40) - 20;
            int y = mod(hy[i] + t * sp, h + 20) - 10;
            g.setColor((hk[i] & 1) == 0 ? 0xB8D4F4 : 0x8FB4E0);
            g.drawLine(x, y, x - 2, y + 7 + (hk[i] & 3));
            demLenh++;
        }
        // vệt nước bắn ở đáy (đầy đủ)
        if (phu) {
            g.setColor(0xD0E4FA);
            for (int i = 0; i < n / 6; i++) {
                int ph = mod(t + hk[i], 12);
                if (ph < 4) {
                    int x = mod(hx[i] * 3, w), y = h - 8 - (hy[i] % Math.max(1, h / 2));
                    g.fillRect(x - ph, y, 1 + ph * 2, 1);
                    demLenh++;
                }
            }
        }
    }

    static void veTuyet(Graphics g, int w, int h, int t, int n, boolean phu) {
        if (n <= 0) {
            return;
        }
        if (phu) {
            SinhThai.phuLat(g, 0, 0, w, h, 0x18 << 24 | 0xE8F2FF);
            demLenh++;
        }
        for (int i = 0; i < n; i++) {
            int sp = 1 + (hk[i] & 3);
            int lac = ((t / 6 + hk[i]) & 15);
            lac = lac < 8 ? lac : 15 - lac;
            int x = mod(hx[i] + lac - 4 + t / 5, w);
            int y = mod(hy[i] + t * sp / 2, h + 8) - 4;
            int s = phu ? 2 + (hk[i] & 1) : (hk[i] & 4) != 0 ? 2 : 1;
            g.setColor((hk[i] & 8) != 0 ? 0xFFFFFF : 0xE4EEFA);
            g.fillRect(x, y, s, s);
            demLenh++;
        }
    }

    static void veBaoCat(Graphics g, int w, int h, int t, int n, boolean phu) {
        if (n <= 0) {
            return;
        }
        SinhThai.phuLat(g, 0, 0, w, h, (phu ? 0x38 : 0x24) << 24 | 0xC8A060);
        demLenh++;
        for (int i = 0; i < n; i++) {
            int sp = 10 + (hk[i] & 7);
            int x = mod(hx[i] + t * sp, w + 40) - 20;
            int y = mod(hy[i] + (t / 3) * ((hk[i] & 1) == 0 ? 1 : -1), h);
            g.setColor((hk[i] & 2) != 0 ? 0xE8C888 : 0xC09858);
            g.drawLine(x, y, x + 8 + (hk[i] & 7), y);
            demLenh++;
        }
    }

    static void veGio(Graphics g, int w, int h, int t, int n) {
        if (n <= 0) {
            return;
        }
        int nv = n / 2;
        for (int i = 0; i < nv; i++) {
            int sp = 12 + (hk[i] & 7);
            int x = mod(hx[i] + t * sp, w + 60) - 30;
            int y = mod(hy[i], h);
            int dai = 12 + (hk[i] & 15);
            int lan = ((t / 4 + hk[i]) & 7);
            int cong = lan < 4 ? lan : 7 - lan;   // vệt gió hơi uốn
            g.setColor((hk[i] & 1) == 0 ? 0xF0F6FC : 0xD8E6F2);
            g.drawLine(x, y, x + dai / 2, y - cong / 2);
            g.drawLine(x + dai / 2, y - cong / 2, x + dai, y);
            demLenh += 2;
        }
        // lá bị cuốn
        for (int i = nv; i < n && i < nv + n / 4; i++) {
            int x = mod(hx[i] + t * (9 + (hk[i] & 3)), w + 20) - 10;
            int lac = ((t / 3 + hk[i]) & 15);
            int y = mod(hy[i] + (lac < 8 ? lac : 15 - lac) * 2 + t / 2, h);
            g.setColor((hk[i] & 2) != 0 ? 0x6CBF4A : 0xC8A040);
            g.fillRect(x, y, 3, 2);
            demLenh++;
        }
    }

    static void veHatMua(Graphics g, int w, int h, int t, int mau, int[] mh, int n) {
        for (int i = 0; i < n; i++) {
            int k = hk[i];
            int x, y;
            if (mau == MAU_HALLOWEEN || mau == MAU_TRUNG_THU) {
                // tàn lửa / đèn bay LÊN chậm, chập chờn
                x = mod(hx[i] + ((t / 8 + k) & 7) - 3, w);
                y = h - mod(hy[i] + t / 2, h + 10);
            } else if (mau == MAU_TET || mau == MAU_NOEL) {
                x = mod(hx[i] + t / 6, w);
                y = mod(hy[i] + t * (1 + (k & 1)) / 3, h);
                if (mau == MAU_NOEL && ((t + k) & 31) > 4) {
                    continue;   // lấp lánh: chỉ chớp
                }
            } else {
                // cánh hoa / lá rơi chéo, lắc
                int lac = ((t / 5 + k) & 15);
                x = mod(hx[i] + t / 2 + (lac < 8 ? lac : 15 - lac), w);
                y = mod(hy[i] + t * (1 + (k & 1)) / 2, h + 6) - 3;
            }
            g.setColor(mh[k % mh.length]);
            if (mau == MAU_TRUNG_THU) {
                g.fillRect(x, y, 3, 4);
            } else {
                g.fillRect(x, y, (k & 2) != 0 ? 3 : 2, (k & 4) != 0 ? 3 : 2);
            }
            demLenh++;
        }
    }

    // ------------------------------------------------------------------ biểu tượng thời tiết (pixel, không ảnh)

    /** Biểu tượng thời tiết tâm (cx, cy), cỡ s (~16..28 px), vẽ bằng khối (J2ME rẻ). */
    public static void veIcon(Graphics g, int tt, int cx, int cy, int s) {
        int r = Math.max(3, s / 4);
        switch (tt) {
            case NANG:
                g.setColor(0xF5A623);
                for (int i = 0; i < 8; i++) {
                    int dx = COS8[i] * (r + 3) / 8, dy = SIN8[i] * (r + 3) / 8;
                    g.fillRect(cx + dx - 1, cy + dy - 1, 2, 2);
                }
                g.setColor(0xFFC93C);
                g.fillArc(cx - r, cy - r, 2 * r, 2 * r, 0, 360);
                break;
            case MUA:
                may(g, cx, cy - r / 2, r, 0xB4BDCA, 0xE6EBF2);
                g.setColor(0x3B8FE8);
                for (int i = -1; i <= 1; i++) {
                    g.drawLine(cx + i * r, cy + r / 2 + 1, cx + i * r - 1, cy + r / 2 + 4);
                }
                break;
            case TUYET:
                may(g, cx, cy - r / 2, r, 0xB4BDCA, 0xF4F8FC);
                g.setColor(0x5AA8E8);
                for (int i = -1; i <= 1; i++) {
                    int x = cx + i * r, y = cy + r / 2 + 2 + (i == 0 ? 2 : 0);
                    g.fillRect(x - 1, y, 3, 1);
                    g.fillRect(x, y - 1, 1, 3);
                }
                break;
            case BAO_CAT:
                g.setColor(0xC08A40);
                for (int i = -1; i <= 1; i++) {
                    int w = r * 2 - (i == 0 ? 0 : r / 2);
                    g.fillRect(cx - r + (i == 1 ? r / 2 : 0), cy + i * (r / 2 + 1), w, 2);
                }
                g.setColor(0xE8C888);
                g.fillRect(cx + r - 2, cy - r / 2 - 2, 2, 2);
                break;
            case SUONG_MU:
                g.setColor(0x8A94A6);
                for (int i = -1; i <= 1; i++) {
                    g.fillRect(cx - r - (i & 1), cy + i * (r / 2 + 1), 2 * r + (i == 0 ? 2 : 0), 2);
                }
                break;
            case GIO:
                g.setColor(0x4AA8C8);
                g.fillRect(cx - r, cy - r / 2, 2 * r - 2, 2);
                g.fillRect(cx + r - 3, cy - r / 2 - 2, 2, 2);
                g.fillRect(cx - r + 2, cy + r / 3, 2 * r - 1, 2);
                g.fillRect(cx + r, cy + r / 3 + 2, 2, 2);
                g.fillRect(cx - r, cy + r - 1, r + 2, 2);
                break;
            default:
                // quang đãng: mặt trời nhỏ + mây trắng
                g.setColor(0xFFC93C);
                g.fillArc(cx - r + 2, cy - r - 1, 2 * r - 2, 2 * r - 2, 0, 360);
                may(g, cx - 1, cy + 1, r - 1, 0xC8D0DA, 0xFFFFFF);
                break;
        }
    }

    static final int[] COS8 = {8, 6, 0, -6, -8, -6, 0, 6};
    static final int[] SIN8 = {0, 6, 8, 6, 0, -6, -8, -6};

    /** Mây: ba vòng + thân. */
    static void may(Graphics g, int cx, int cy, int r, int vien, int than) {
        g.setColor(vien);
        g.fillArc(cx - r - 1, cy - r / 2 - 1, r + 2, r + 2, 0, 360);
        g.fillArc(cx - r / 2 - 1, cy - r - 1, r + 4, r + 4, 0, 360);
        g.fillArc(cx, cy - r / 2 - 1, r + 2, r + 2, 0, 360);
        g.setColor(than);
        g.fillArc(cx - r, cy - r / 2, r, r, 0, 360);
        g.fillArc(cx - r / 2, cy - r, r + 2, r + 2, 0, 360);
        g.fillArc(cx + 1, cy - r / 2, r, r, 0, 360);
        g.fillRect(cx - r + 1, cy, 2 * r - 1, r / 2 + 1);
    }

    /** Biểu tượng mùa nhỏ (chấm màu) cho thẻ. */
    public static int mauChamMua(int mau) {
        return mau > MAU_KHONG && mau < PHU_MUA.length ? PHU_MUA[mau][0] : 0xB4BDCA;
    }
}
