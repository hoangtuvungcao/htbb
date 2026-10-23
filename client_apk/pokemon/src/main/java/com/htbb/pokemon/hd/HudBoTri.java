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
            N_CHAT_BONG = 14, N_BONG = 15, N_CAI_DAT = 16, N_TD_CAI = 17, N_MINIMAP = 18, SO_NUT = 19;
    /** 0.9.9: hàng trên (phải -> trái) Tự động, bánh răng cài đặt Tự động, Cài đặt, Chat, Hành trang, Nhiệm vụ (+ chỗ Menu dự phòng). */
    public static final int SO_NUT_TREN = 7;
    public static final String[] TEN = {"danh", "chieu0", "chieu1", "chieu3", "chieu4", "doi_bo", "muc_tieu", "hp", "pp",
        "menu", "tu_dong", "chat", "them", "chinh", "chat_bong", "bong", "cai_dat", "tu_dong_cai_dat", "minimap"};

    /**
     * 0.9.8 nút NÉM BÓNG (chỉ hiện khi mục tiêu hoang dã đã yếu - chỗ luôn dành sẵn): hình bóng, nhãn "Ultra Ball ×3"
     * ngay dưới. Ô nhãn (px logic) = W_NHAN x H_NHAN canh giữa dưới nút, cách KHE_NHAN.
     */
    public static float W_NHAN = 80f, H_NHAN = 11f, KHE_NHAN = 2f;

    /** Ô nhãn bóng {trái, trên, phải, dưới}. */
    public static float[] oNhanBong() {
        float x = NX[N_BONG], y = NY[N_BONG] + NR[N_BONG] + KHE_NHAN;
        return new float[]{x - W_NHAN / 2, y, x + W_NHAN / 2, y + H_NHAN};
    }

    /** Khoảng cách từ tâm (x, y) tới hình chữ nhật o. */
    static float denO(float x, float y, float[] o) {
        float dx = Math.max(Math.max(o[0] - x, 0), x - o[2]), dy = Math.max(Math.max(o[1] - y, 0), y - o[3]);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /** Nút bóng ở (x, y) bán kính r + nhãn: trong vùng an toàn, cách mọi nút khác (trừ minimap / bóng) >= k, ngoài cần. */
    static boolean choBongTot(float x, float y, float r, float k) {
        float ox = NX[N_BONG], oy = NY[N_BONG], or = NR[N_BONG];
        dat(N_BONG, x, y, r);
        float[] o = oNhanBong();
        boolean tot = x - r >= L && x + r <= R && y - r >= T && o[0] >= L && o[2] <= R && o[3] <= B;
        for (int n = 0; tot && n < N_MINIMAP; n++) {
            if (n == N_BONG) continue;
            float dx = NX[n] - x, dy = NY[n] - y;
            if (Math.sqrt(dx * dx + dy * dy) < NR[n] + r + k || denO(NX[n], NY[n], o) < NR[n] + k) tot = false;
        }
        if (tot) {
            float dx = jx0 - x, dy = jy0 - y;
            if (Math.sqrt(dx * dx + dy * dy) < jr + r + k || denO(jx0, jy0, o) < jr + k) tot = false;
        }
        if (!tot) dat(N_BONG, ox, oy, or);
        return tot;
    }

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
        // 0.9.7 (NRO): ô chiêu VUÔNG khe ĐỀU trên cung: góc 180, 180 - a, 90 + a, 90 (tan a = 1/2) -> mọi cặp ô kề cách
        // nhau theo trục 0,447 rc; dây cung ngắn nhất (góc 26,57 độ) >= 2 rChieu + k; xa nút đánh >= rDanh + rChieu + k
        float aKhe = (float) Math.toDegrees(Math.atan(0.5));
        float rc = Math.max(76 * s1, Math.max((2 * rChieu + k) / (2f * (float) Math.sin(Math.toRadians(aKhe / 2))), rDanh + rChieu + k));
        float[] goc = {180, 180 - aKhe, 90 + aKhe, 90};
        float le = Math.max(44 * s1, Math.max(rDanh + 2, rChieu + 2));
        float leD = Math.max(44 * s1, Math.max(rDanh + 2, rChieu + 2));
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
        // mục tiêu: NGOÀI cung, hướng 135 độ (giữa ô chiêu 2 và 3 - ô chiêu 4 nay ở đỉnh cung ngay trên nút đánh)
        float rMtc = rc + rChieu + rMt + k + dp(4);
        dat(N_MUC_TIEU, cx - rMtc * 0.7071f, cy - rMtc * 0.7071f, rMt);
        float s2 = gs[2] * kd;
        float rHp = Math.max(14 * s2, rm);
        float xHp = Math.min(cx - 112 * s1, NX[N_DOI_BO] - rDb - rHp - k) + gx[2];
        float yHp = cy + 20 * s1 + gy[2];
        dat(N_HP, xHp, yHp, rHp);
        dat(N_PP, xHp - Math.max(33 * s2, 2 * rHp + k), yHp, rHp);
        int[] tren = {N_TU_DONG, N_TD_CAI, N_CAI_DAT, N_CHAT, N_THEM, N_CHINH};
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
        dat(N_BONG, -1000, -1000, 0);   // đặt SAU vòng đẩy
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
        // 0.9.8 nút ném bóng: ngoài cung chiêu phía trái (quanh hướng 160 độ, giữa chiêu 0 / chiêu 1 và mục tiêu), chỗ gần
        // nhất đủ rộng cho nút + nhãn dưới; không chỗ nào (cụm phóng quá to): trái hàng nút trên
        float rB = Math.max(16 * s1, rm);
        boolean coCho = false;
        int[] gocB = {160, 150, 170, 140, 180, 130, 190, 120, 200};
        for (float xa = rc + rChieu + rB + k; !coCho && xa < rc + rChieu + 10 * rB; xa += rB / 2) {
            for (int g = 0; !coCho && g < gocB.length; g++) {
                double a = Math.toRadians(gocB[g]);
                coCho = choBongTot(cx + (float) Math.cos(a) * xa, cy - (float) Math.sin(a) * xa, rB, k);
            }
        }
        if (!coCho) {
            dat(N_BONG, xNutTren(5), yNutTren(), rTren());
        }
        // 0.9.6 Poké Ball (Menu) giữa đáy: trong khe giữa cần điều khiển và nút trái nhất của cụm đáy, gần giữa màn nhất;
        // khe hẹp thì thu về cỡ chạm tối thiểu
        float trai = jx0 + jr + k, phai = R;
        int[] cum = {N_DANH, N_C0, N_C1, N_C3, N_C4, N_DOI_BO, N_HP, N_PP, N_BONG};
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
            dat(N_MENU, xNutTren(SO_NUT_TREN - 1), yNutTren(), rTren());   // cụm đáy phóng quá to (người chơi chỉnh): về hàng trên
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
        float[] o = oNhanBong();
        if (o[0] < L - 0.5f || o[2] > R + 0.5f || o[1] < T - 0.5f || o[3] > B + 0.5f) b.append("NGOAI nhan_bong\n");
        for (int n = 0; n < N_MINIMAP; n++) {
            if (n == N_BONG) continue;
            if (denO(NX[n], NY[n], o) < NR[n] + k) b.append("SAT nhan_bong x ").append(TEN[n]).append('\n');
        }
        if (denO(jx0, jy0, o) < jr) b.append("CAN nhan_bong\n");
        float dayTren = yNutTren() + rTren();
        if (T + yTheMiniMap() < dayTren + cach() - 0.6f) b.append("MINIMAP sat hang nut tren\n");
        return b.toString();
    }
}
