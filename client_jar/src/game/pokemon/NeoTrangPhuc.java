package game.pokemon;

/**
 * ĐIỂM NEO TRANG PHỤC tính từ CHÍNH ĐIỂM ẢNH của khung đang vẽ (0.9.8): mỗi loài x hướng x động tác x khung có đỉnh sọ
 * (giữa hai tai, bỏ tai / râu / mào hẹp), bề ngang đầu, hàng mắt (điểm tối nhỏ nằm trong thân, không phải viền), cổ (chỗ
 * thân thắt lại dưới đầu, không có thì cằm), lưng (vai, lệch về phía sau theo hướng) và eo. Điểm đầu / tâm PMD
 * (PokeBody.neo) chỉ là HẠT GIỐNG để biết vùng đầu. Lớp THUẦN (client_pc/test/KiemTrangPhuc kiểm; tools/do_trang_phuc
 * ghi bảng du_lieu/neo_trang_phuc.csv từ cùng hàm).
 *
 * Kết quả (so với điểm chân, px gốc): {@link #DINH_Y} đỉnh sọ (hàng điểm đặc trên cùng của sọ), {@link #DAU_X} giữa sọ,
 * {@link #DAU_TRAI} / {@link #DAU_PHAI} mép đầu, {@link #MAT_Y} hàng mắt (KHONG nếu không thấy - quay lưng),
 * {@link #MAT_TREN} hàng mắt trên cùng, {@link #CO_X} / {@link #CO_Y} cổ, {@link #LUNG_X} / {@link #LUNG_Y} vai sau,
 * {@link #EO_Y} eo (giữa cổ và chân), {@link #THAN_TRAI} / {@link #THAN_PHAI} mép thân ở hàng cổ.
 */
public final class NeoTrangPhuc {
    public static final int DINH_Y = 0, DAU_X = 1, DAU_TRAI = 2, DAU_PHAI = 3, MAT_Y = 4, MAT_TREN = 5, CO_X = 6, CO_Y = 7,
            LUNG_X = 8, LUNG_Y = 9, EO_Y = 10, THAN_TRAI = 11, THAN_PHAI = 12, DAY_Y = 13, SO = 14;
    public static final int KHONG = -9999;

    private NeoTrangPhuc() {
    }

    /** Gỡ lỗi / test: điểm mắt của lần tính gần nhất (bật ghiMat). */
    public static boolean ghiMat;
    public static final int[] MAT_DIEM = new int[256];
    public static int soMat;

    static boolean dac(int[] px, int w, int h, int x, int y) {
        return x >= 0 && y >= 0 && x < w && y < h && (px[y * w + x] >>> 24) >= 128;
    }

    static int sangDo(int c) {
        return (((c >> 16) & 0xFF) * 77 + ((c >> 8) & 0xFF) * 150 + (c & 0xFF) * 29) >> 8;
    }

    /**
     * Tính neo cho một khung: px (w x h, ARGB) với điểm chân ở (cx, cy) trong khung; pmd = neo PMD của khung (dauX, dauY,
     * dinhY, tayX, tayY, tamX, tamY, co) so với chân; dir 0 xuống, 1 lên, 2 trái, 3 phải. out[SO].
     */
    public static void tinh(int[] px, int w, int h, int cx, int cy, int[] pmd, int dir, int[] out) {
        soMat = 0;
        // hạt giống: điểm đầu PMD (đưa vào trong thân nếu rơi ra ngoài)
        int hx = cx + pmd[0], hy = cy + pmd[1];
        if (!dac(px, w, h, hx, hy)) {
            int[] g = ganNhat(px, w, h, hx, hy);
            hx = g[0];
            hy = g[1];
        }
        // bề ngang đầu ở hàng điểm đầu
        int l = hx, r = hx;
        while (dac(px, w, h, l - 1, hy)) {
            l--;
        }
        while (dac(px, w, h, r + 1, hy)) {
            r++;
        }
        // đỉnh sọ: mỗi cột trong nửa giữa đầu, đi lên từ hàng đầu khi còn đặc; lấy TRUNG VỊ (tai / mào hẹp không kéo lên)
        int rong = r - l + 1;
        // toàn bề ngang mặt ở hàng điểm đầu (tai / sừng / mào là cột hẹp -> trung vị bỏ qua), bỏ 1 cột viền mỗi bên
        int c0 = l + (rong > 4 ? 1 : 0), c1 = r - (rong > 4 ? 1 : 0);
        if (c1 < c0) {
            c0 = c1 = hx;
        }
        int[] dinh = new int[c1 - c0 + 1];
        for (int x = c0; x <= c1; x++) {
            int y = hy;
            while (dac(px, w, h, x, y - 1)) {
                y--;
            }
            dinh[x - c0] = y;
        }
        sap(dinh);
        int dinhY = dinh[dinh.length / 2];
        // mép đầu: rộng nhất trong 1/2 trên của đầu (từ đỉnh sọ tới hàng đầu), chỉ phần nối liền với cột giữa
        int sx = (l + r) / 2;
        int L = sx, R = sx;
        // chỉ vài hàng ngay dưới đỉnh sọ (thân / tay / cánh ở thấp hơn không tính là đầu)
        int hetSo = Math.max(dinhY + 1, Math.min(hy, dinhY + 4));
        for (int y = dinhY + 1; y <= hetSo; y++) {
            if (!dac(px, w, h, sx, y)) {
                continue;
            }
            int a = sx, b = sx;
            while (dac(px, w, h, a - 1, y)) {
                a--;
            }
            while (dac(px, w, h, b + 1, y)) {
                b++;
            }
            if (b - a > R - L) {
                L = a;
                R = b;
            }
        }
        if (R - L < 2) {
            L = l;
            R = r;
        }
        // không quá mặt ở hàng điểm đầu (đuôi lửa / cánh / tay sát đầu không tính)
        L = Math.max(L, l - 1);
        R = Math.min(R, r + 1);
        // giữa sọ: trung điểm các cột đặc ở hàng đỉnh sọ + 1 quanh cột giữa
        int yt = Math.min(dinhY + 1, h - 1);
        int a = sx, b = sx;
        while (dac(px, w, h, a - 1, yt) && a - 1 >= L) {
            a--;
        }
        while (dac(px, w, h, b + 1, yt) && b + 1 <= R) {
            b++;
        }
        int dauX = (a + b) / 2;
        // mắt: điểm tối nằm TRONG thân (4 lân cận đặc, không phải viền ngoài), trong đầu, phía trên giữa đầu
        int matTren = KHONG, tong = 0, dem = 0;
        int matDuoi = Math.max(hy, dinhY + (R - L) * 3 / 4);
        if (dir != 1) {
            // mắt = cụm NHỎ gọn điểm rất tối (gần đen, ít màu) nằm trong thân, sát điểm sáng (lòng trắng / ánh mắt);
            // nét kẻ trong (viền tai, miệng) là đường dài / mảnh -> loại theo cỡ cụm
            int x0 = Math.max(0, L - 1), x1 = Math.min(w - 1, R + 1);
            int y0 = Math.max(0, dinhY + 1), y1 = Math.min(h - 1, matDuoi);
            int ww = x1 - x0 + 1, hh = y1 - y0 + 1;
            if (ww > 0 && hh > 0) {
                boolean[] toi = new boolean[ww * hh];
                for (int y = y0; y <= y1; y++) {
                    for (int x = x0; x <= x1; x++) {
                        int c = dac(px, w, h, x, y) ? px[y * w + x] : 0;
                        toi[(y - y0) * ww + x - x0] = c != 0 && sangDo(c) <= 48 && bao(c) <= 60
                                && dac(px, w, h, x - 1, y) && dac(px, w, h, x + 1, y) && dac(px, w, h, x, y - 1) && dac(px, w, h, x, y + 1);
                    }
                }
                int[] hang = new int[ww * hh];
                int[] cum = new int[16 * 6];   // {n, tongX, tongY, minX, maxX, minY, maxY, sang} rút gọn: 6 trường
                int soCum = 0;
                int[] diemTong = new int[256], soDiem = new int[1];
                for (int i = 0; i < ww * hh && soCum < 16; i++) {
                    if (!toi[i]) {
                        continue;
                    }
                    // loang 8 hướng
                    int n = 0, sxx = 0, syy = 0, mnx = 9999, mxx = -1, mny = 9999, mxy = -1;
                    boolean sangGan = false;
                    int dau = 0, cuoi = 0;
                    hang[cuoi++] = i;
                    toi[i] = false;
                    while (dau < cuoi) {
                        int j = hang[dau++];
                        int x = x0 + j % ww, y = y0 + j / ww;
                        n++;
                        sxx += x;
                        syy += y;
                        mnx = Math.min(mnx, x);
                        mxx = Math.max(mxx, x);
                        mny = Math.min(mny, y);
                        mxy = Math.max(mxy, y);
                        if (!sangGan && coSangGan(px, w, h, x, y)) {
                            sangGan = true;
                        }
                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dx = -1; dx <= 1; dx++) {
                                int X = x + dx - x0, Y = y + dy - y0;
                                if (X >= 0 && Y >= 0 && X < ww && Y < hh && toi[Y * ww + X]) {
                                    toi[Y * ww + X] = false;
                                    hang[cuoi++] = Y * ww + X;
                                }
                            }
                        }
                    }
                    int bw = mxx - mnx + 1, bh = mxy - mny + 1;
                    if (!sangGan || n > 14 || bw > 4 || bh > 5 || (n >= 3 && (bw == 1 || bh == 1) && Math.max(bw, bh) >= 4)) {
                        continue;   // không có ánh sáng / quá to / là đường kẻ
                    }
                    cum[soCum * 6] = n;
                    cum[soCum * 6 + 1] = sxx / n;
                    cum[soCum * 6 + 2] = syy / n;
                    cum[soCum * 6 + 3] = mny;
                    cum[soCum * 6 + 4] = mnx;
                    cum[soCum * 6 + 5] = mxx;
                    soCum++;
                }
                // tối đa 2 mắt: cụm gần điểm đầu nhất (mắt luôn ở nửa trên đầu, quanh điểm đầu PMD)
                for (int lan = 0; lan < 2 && soCum > 0; lan++) {
                    int tot = -1;
                    long dTot = Long.MAX_VALUE;
                    for (int c = 0; c < soCum; c++) {
                        if (cum[c * 6] == 0) {
                            continue;
                        }
                        long dx = cum[c * 6 + 1] - hx, dy = cum[c * 6 + 2] - hy;
                        long dd = dx * dx + dy * dy * 2;
                        if (dd < dTot) {
                            dTot = dd;
                            tot = c;
                        }
                    }
                    if (tot < 0) {
                        break;
                    }
                    if (lan == 1 && matTren != KHONG && Math.abs(cum[tot * 6 + 2] - tong / Math.max(1, dem)) > 3) {
                        break;   // mắt thứ hai phải cùng hàng
                    }
                    if (matTren == KHONG || cum[tot * 6 + 3] < matTren) {
                        matTren = cum[tot * 6 + 3];
                    }
                    tong += cum[tot * 6 + 2];
                    dem++;
                    if (ghiMat && soMat < MAT_DIEM.length / 2) {
                        MAT_DIEM[soMat * 2] = cum[tot * 6 + 1] - cx;
                        MAT_DIEM[soMat * 2 + 1] = cum[tot * 6 + 2] - cy;
                        soMat++;
                    }
                    cum[tot * 6] = 0;
                }
            }
        }
        int matY = dem > 0 ? tong / dem : KHONG;
        // cổ: hàng hẹp nhất của thân (liền cột giữa) giữa mắt / đầu và tâm PMD; không thắt -> cằm ước lượng
        int tamY = cy + pmd[6];
        int tu = Math.max(hy, matY != KHONG ? matY + 2 : hy) + 1, den = Math.max(tu, tamY);   // cổ dưới mắt và dưới điểm đầu
        int coY = KHONG, hepNhat = 9999, coL = L, coR = R;
        int rongDau = R - L + 1;
        for (int y = tu; y <= den && y < h; y++) {
            int x0 = timDac(px, w, h, dauX, y);
            if (x0 < 0) {
                continue;
            }
            int p = x0, q = x0;
            while (dac(px, w, h, p - 1, y)) {
                p--;
            }
            while (dac(px, w, h, q + 1, y)) {
                q++;
            }
            int wr = q - p + 1;
            if (wr < hepNhat - 1) {
                hepNhat = wr;
                coY = y;
                coL = p;
                coR = q;
            }
        }
        boolean thatLai = coY != KHONG && hepNhat < rongDau * 85 / 100 && coY < den;
        if (!thatLai) {
            int dauCao = Math.max(3, (matY != KHONG ? matY : hy) - dinhY);
            coY = Math.min(den, (matY != KHONG ? matY : hy) + dauCao);
            int x0 = timDac(px, w, h, dauX, coY);
            coL = L;
            coR = R;
            if (x0 >= 0) {
                coL = x0;
                coR = x0;
                while (dac(px, w, h, coL - 1, coY)) {
                    coL--;
                }
                while (dac(px, w, h, coR + 1, coY)) {
                    coR++;
                }
            }
        }
        int coX = (coL + coR) / 2;
        // lưng / vai: hàng cổ, lệch về phía sau (nhìn trái: lưng bên phải; nhìn phải: bên trái)
        int lungX = coX;
        if (dir == 2) {
            lungX = coX + (coR - coL) / 4;
        } else if (dir == 3) {
            lungX = coX - (coR - coL) / 4;
        }
        int lungY = coY;
        // eo: giữa cổ và đáy thân THẬT (hàng đặc thấp nhất) - khung đánh / ngã có thân lệch khỏi điểm chân
        int day = coY;
        for (int y = h - 1; y > coY; y--) {
            boolean co = false;
            for (int x = 0; x < w && !co; x++) {
                co = dac(px, w, h, x, y);
            }
            if (co) {
                day = y;
                break;
            }
        }
        int eoY = Math.max(coY + 1, (coY + day) / 2);
        out[DINH_Y] = dinhY - cy;
        out[DAU_X] = dauX - cx;
        out[DAU_TRAI] = L - cx;
        out[DAU_PHAI] = R - cx;
        out[MAT_Y] = matY == KHONG ? KHONG : matY - cy;
        out[MAT_TREN] = matTren == KHONG ? KHONG : matTren - cy;
        out[CO_X] = coX - cx;
        out[CO_Y] = coY - cy;
        out[LUNG_X] = lungX - cx;
        out[LUNG_Y] = lungY - cy;
        out[EO_Y] = eoY - cy;
        out[THAN_TRAI] = coL - cx;
        out[THAN_PHAI] = coR - cx;
        out[DAY_Y] = day - cy;
    }

    static boolean coSangGan(int[] px, int w, int h, int x, int y) {
        int c = sangDo(px[y * w + x]);
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                int X = x + dx, Y = y + dy;
                if (dac(px, w, h, X, Y) && sangDo(px[Y * w + X]) > Math.max(c + 150, 190)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Độ bão hoà thô (max - min kênh): mắt PMD gần xám đen, bóng da tối vẫn đậm màu. */
    static int bao(int c) {
        int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
        return Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b));
    }

    static int timDac(int[] px, int w, int h, int x, int y) {
        for (int d = 0; d < w; d++) {
            if (dac(px, w, h, x - d, y)) {
                return x - d;
            }
            if (dac(px, w, h, x + d, y)) {
                return x + d;
            }
        }
        return -1;
    }

    static int[] ganNhat(int[] px, int w, int h, int x, int y) {
        for (int r = 1; r < Math.max(w, h); r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if ((Math.abs(dx) == r || Math.abs(dy) == r) && dac(px, w, h, x + dx, y + dy)) {
                        return new int[]{x + dx, y + dy};
                    }
                }
            }
        }
        return new int[]{x, y};
    }

    static void sap(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int v = a[i], j = i - 1;
            while (j >= 0 && a[j] > v) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = v;
        }
    }
}
