package com.htbb.pokemon.hd;

/**
 * Bố cục nút HUD cảm ứng (thuần Java, không Android - kiểm thử JVM: client_jar/tools/KiemCoCham.java).
 *
 * 0.9.3 (ưu tiên điện thoại): mọi nút >= 44 dp (bán kính >= 22 dp) và cách nhau >= 8 dp SAU phép phóng của khung
 * phủ kín (logic -> màn thật = tiLe, dp -> px thật = dpi / 160): dpLogic = (dpi / 160) / tiLe px logic mỗi dp.
 * Vị trí gốc giữ như bản cũ (cụm chiến đấu góc phải dưới, hàng nút trên canh phải, bóng chat trái); nút nào nhỏ hơn
 * thì to ra, cung chiêu giãn theo, rồi một vòng "đẩy nhau" tách các nút phụ (đổi bô, mục tiêu, HP / PP, bóng chat)
 * khỏi chỗ chồng và kẹp trong vùng an toàn.
 */
public final class HudBoTri {
    public static final int N_DANH = 0, N_C0 = 1, N_C1 = 2, N_C3 = 3, N_C4 = 4, N_DOI_BO = 5, N_MUC_TIEU = 6,
            N_HP = 7, N_PP = 8, N_MENU = 9, N_TU_DONG = 10, N_CHAT = 11, N_THEM = 12, N_CHINH = 13,
            N_CHAT_BONG = 14, N_MINIMAP = 15, SO_NUT = 16;
    public static final int SO_NUT_TREN = 5;
    public static final String[] TEN = {"danh", "chieu0", "chieu1", "chieu3", "chieu4", "doi_bo", "muc_tieu", "hp", "pp",
        "menu", "tu_dong", "chat", "them", "chinh", "chat_bong", "minimap"};

    public static final float[] NX = new float[SO_NUT];
    public static final float[] NY = new float[SO_NUT];
    public static final float[] NR = new float[SO_NUT];

    /** px logic mỗi dp (đặt từ dpi / tiLe) */
    public static float dpLogic = 1f;
    /** vùng an toàn (px logic) */
    public static float L, T, R, B;
    public static float jx0, jy0, jr;

    private HudBoTri() {
    }

    public static float dp(float d) {
        return d * dpLogic;
    }

    /** bán kính tối thiểu (44 dp đường kính) */
    public static float rMin() {
        return dp(22);
    }

    /** khoảng cách tối thiểu giữa hai nút */
    public static float cach() {
        return dp(8);
    }

    public static float rTren() {
        return Math.max(12.5f, rMin());
    }

    public static float buocTren() {
        return Math.max(29f, 2 * rTren() + cach());
    }

    /** Nút i của hàng trên (0 = sát mép phải). 0.9.6: Menu (Poké Ball) rời hàng trên xuống GIỮA ĐÁY (kiểu GO). */
    public static float xNutTren(int i) {
        return R - rTren() - 1 - i * buocTren();
    }

    /** Bán kính nút Menu (Poké Ball) giữa đáy. */
    public static float rMenu() {
        return Math.max(rTren() * 1.35f, rMin() + dp(2));
    }

    public static float yNutTren() {
        return T + rTren() + 2.5f;
    }

    /** đỉnh thẻ bản đồ nhỏ tính từ T (dưới hàng nút trên + khoảng cách) */
    public static int yTheMiniMap() {
        return (int) Math.ceil(2 * rTren() + 2.5f + Math.max(3f, cach()));
    }

    static void dat(int n, float x, float y, float r) {
        NX[n] = x;
        NY[n] = y;
        NR[n] = r;
    }

    /**
     * Tính bố cục. kd = hệ số cỡ theo dpi cũ; gs / gx / gy = chỉnh cỡ / dời của người chơi cho 4 nhóm
     * (0 cần điều khiển, 1 cụm chiến đấu, 2 HP / PP, 3 bóng chat).
     */
    public static void boTri(float l, float t, float r, float b, float kd, float[] gs, float[] gx, float[] gy) {
        L = l;
        T = t;
        R = r;
        B = b;
        float rm = rMin(), k = cach();
        float s1 = gs[1] * kd;
        float rDanh = Math.max(31 * s1, rm + dp(6));
        float rChieu = Math.max(18.5f * s1, rm);
        // cung chiêu: góc gần nhau nhất 36 độ -> dây cung >= 2 rChieu + k; xa nút đánh >= rDanh + rChieu + k
        float rc = Math.max(68 * s1, Math.max((2 * rChieu + k) / (2f * (float) Math.sin(Math.toRadians(18))), rDanh + rChieu + k));
        float[] goc = {192, 153, 116, 80};
        float le = Math.max(44 * s1, Math.max(rDanh + 2, rChieu + rc * (float) Math.cos(Math.toRadians(80)) + 2));
        float leD = Math.max(44 * s1, Math.max(rDanh + 2, rChieu - rc * (float) Math.sin(Math.toRadians(192)) + 2));
        float cx = R - le + gx[1];
        float cy = B - leD + gy[1];
        dat(N_DANH, cx, cy, rDanh);
        int[] nut = {N_C0, N_C1, N_C3, N_C4};
        for (int i = 0; i < 4; i++) {
            double a = Math.toRadians(goc[i]);
            dat(nut[i], cx + (float) Math.cos(a) * rc, cy - (float) Math.sin(a) * rc, rChieu);
        }
        // đổi bô: hàng dưới, ngay trái chiêu 0 (chỗ cũ dưới-trái nút đánh quá chật khi nút >= 44 dp)
        float rDb = Math.max(10 * s1, rm);
        dat(N_DOI_BO, NX[N_C0] - rChieu - rDb - k, Math.min(NY[N_C0] + rChieu - rDb, B - rDb), rDb);
        float rMt = Math.max(12.5f * s1, rm);
        dat(N_MUC_TIEU, cx + 32 * s1, cy - Math.max(100 * s1, rc + rChieu + rMt + k), rMt);
        float s2 = gs[2] * kd;
        float rHp = Math.max(14 * s2, rm);
        float xHp = Math.min(cx - 112 * s1, NX[N_DOI_BO] - rDb - rHp - k) + gx[2];
        float yHp = cy + 20 * s1 + gy[2];
        dat(N_HP, xHp, yHp, rHp);
        dat(N_PP, xHp - Math.max(33 * s2, 2 * rHp + k), yHp, rHp);
        int[] tren = {N_TU_DONG, N_CHAT, N_THEM, N_CHINH};
        for (int i = 0; i < tren.length; i++) dat(tren[i], xNutTren(i), yNutTren(), rTren());
        float rMn = rMenu();
        dat(N_MENU, (L + R) / 2f, B - rMn - dp(4), rMn);
        float s3 = gs[3] * kd;
        float rCb = Math.max(12 * s3, rm);
        float s0 = gs[0] * kd;
        dat(N_CHAT_BONG, L + Math.max(13 * s3, rCb + 1) + gx[3], B - 140 * s0 + gy[3], rCb);
        jr = 42 * s0;
        jx0 = L + 58 * s0 + gx[0];
        jy0 = B - 58 * s0 + gy[0];
        dat(N_MINIMAP, -1000, -1000, 0);
        // nút phụ: tách khỏi chỗ chồng, kẹp trong vùng an toàn
        int[] di = {N_MUC_TIEU, N_HP, N_PP, N_CHAT_BONG};
        for (int lap = 0; lap < 40; lap++) {
            boolean doi = false;
            for (int q = 0; q < di.length; q++) {
                int m = di[q];
                kep(m);
                for (int n = 0; n < N_MINIMAP; n++) {
                    if (n == m || n == N_MENU) continue;   // Poké Ball đặt SAU vòng đẩy (khe giữa cần điều khiển và cụm)
                    float dx = NX[m] - NX[n], dy = NY[m] - NY[n];
                    float can = NR[m] + NR[n] + k;
                    float d2 = dx * dx + dy * dy;
                    if (d2 >= can * can) continue;
                    float d = (float) Math.sqrt(d2);
                    if (d < 0.01f) {
                        dx = -1;
                        dy = 0;
                        d = 1;
                    }
                    float day = can - d + 0.5f;
                    NX[m] += dx / d * day;
                    NY[m] += dy / d * day;
                    doi = true;
                }
                kep(m);
            }
            if (!doi) break;
        }
        // 0.9.6 Poké Ball (Menu) giữa đáy: trong khe giữa cần điều khiển và nút trái nhất của cụm đáy, gần giữa màn nhất;
        // khe hẹp thì thu về cỡ chạm tối thiểu
        float trai = jx0 + jr + k, phai = R;
        int[] cum = {N_DANH, N_C0, N_C1, N_C3, N_C4, N_DOI_BO, N_HP, N_PP};
        float yM = NY[N_MENU];
        for (int q = 0; q < cum.length; q++) {
            int n = cum[q];
            if (NY[n] + NR[n] > yM - NR[N_MENU] - k) {
                phai = Math.min(phai, NX[n] - NR[n] - k);
            }
        }
        if (phai - trai >= 2 * rm) {
            float rM = Math.max(rm, Math.min(NR[N_MENU], (phai - trai) / 2f));
            float xM = Math.max(trai + rM, Math.min(phai - rM, (L + R) / 2f));
            dat(N_MENU, xM, B - rM - dp(4), rM);
        } else {
            dat(N_MENU, xNutTren(4), yNutTren(), rTren());   // cụm đáy phóng quá to (người chơi chỉnh): về hàng trên
        }
    }

    static void kep(int n) {
        float r = NR[n];
        if (NX[n] < L + r) NX[n] = L + r;
        if (NX[n] > R - r) NX[n] = R - r;
        if (NY[n] < T + r) NY[n] = T + r;
        if (NY[n] > B - r) NY[n] = B - r;
    }

    /**
     * Kiểm cỡ chạm + khoảng cách + nằm trong vùng an toàn; "" = đạt. yTheMm = đỉnh thẻ bản đồ nhỏ (tuyệt đối),
     * hàng nút trên phải nằm trên nó.
     */
    public static String kiem() {
        StringBuffer b = new StringBuffer();
        float rm = rMin() - 0.01f, k = cach() - 0.6f;
        for (int n = 0; n < N_MINIMAP; n++) {
            if (NR[n] < rm) b.append("NHO ").append(TEN[n]).append(" r=").append(NR[n]).append(" < ").append(rMin()).append('\n');
            if (NX[n] - NR[n] < L - 0.5f || NX[n] + NR[n] > R + 0.5f || NY[n] - NR[n] < T - 0.5f || NY[n] + NR[n] > B + 0.5f) {
                b.append("NGOAI ").append(TEN[n]).append('\n');
            }
            for (int m = n + 1; m < N_MINIMAP; m++) {
                float dx = NX[m] - NX[n], dy = NY[m] - NY[n];
                float d = (float) Math.sqrt(dx * dx + dy * dy) - NR[n] - NR[m];
                if (d < k) b.append("SAT ").append(TEN[n]).append(" x ").append(TEN[m]).append(" cach ").append(d).append('\n');
            }
            // cần điều khiển không đè nút
            float dx = NX[n] - jx0, dy = NY[n] - jy0;
            if (n != N_CHAT_BONG && Math.sqrt(dx * dx + dy * dy) < jr + NR[n]) b.append("CAN ").append(TEN[n]).append('\n');
        }
        float dayTren = yNutTren() + rTren();
        if (T + yTheMiniMap() < dayTren + cach() - 0.6f) b.append("MINIMAP sat hang nut tren\n");
        return b.toString();
    }
}
