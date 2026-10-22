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
            LUNG_X = 8, LUNG_Y = 9, EO_Y = 10, THAN_TRAI = 11, THAN_PHAI = 12, DAY_Y = 13, MU_SAU = 14, SO = 15;
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
    /**
     * Loài có ĐẦU NẰM DƯỚI một vật trên lưng / đỉnh (dòng Bulbasaur: củ, nụ, hoa): đỉnh hình là vật đó, không phải sọ. Mũ
     * phải ngồi trên ĐẦU thật: dò từ hàng mắt lên tới nét viền tối ngăn mặt với củ / hoa; quay lưng: mũ sau thân (củ che).
     */
    public static final int[] DAU_DUOI_VAT = {1, 2, 3};

    /**
     * Neo mặt đặt tay cho loài DAU_DUOI_VAT (nhìn xuống 0 / ngang 2; nhìn phải lật dấu x): {dex, hướng, cộng vào đỉnh PMD,
     * cộng vào x đầu PMD, bề ngang mặt}. Đỉnh PMD của các loài này là đỉnh củ / hoa, mặt thật nằm thấp hơn.
     */
    static final int[][] DAT_TAY = {
        {1, 0, 7, 1, 14}, {1, 2, -1, 2, 8},
        {2, 0, 8, 1, 12}, {2, 2, 5, -9, 8},
        {3, 0, 11, 0, 14}, {3, 2, 3, 3, 9},
    };

    /**
     * GHI ĐÈ TAY (loai-bo-sung, client_pc/test/KiemNeoLoai): {dex, động tác, hướng} mà dò điểm ảnh + DAT_TAY KHÔNG tìm
     * được đầu thật -> không vẽ MŨ ở khung đó (thà không vẽ còn hơn lệch). Bulbasaur / Ivysaur nhìn ngang lúc đánh xa
     * (củ chĩa lên trước, mặt khuất dưới củ) và Ivysaur gục ngã nhìn trái (DAT_TAY lệch x của tư thế đứng đẩy mũ ra ngoài
     * thân). Đồ cổ / lưng / cầm tay vẫn vẽ.
     */
    public static final int[][] AN_MU = {
        {1, PokeDuLieu.DANH_XA, 2}, {1, PokeDuLieu.DANH_XA, 3},
        {2, PokeDuLieu.DANH_XA, 2}, {2, PokeDuLieu.GUC_NGA, 2},
    };

    public static boolean anMu(int dex, int dongTac, int dir) {
        for (int i = 0; i < AN_MU.length; i++) {
            if (AN_MU[i][0] == dex && AN_MU[i][1] == dongTac && AN_MU[i][2] == dir) {
                return true;
            }
        }
        return false;
    }

    static int pmdDex;

    static int[] datTay(int dex, int dir) {
        for (int i = 0; i < DAT_TAY.length; i++) {
            if (DAT_TAY[i][0] == dex && DAT_TAY[i][1] == dir) {
                return DAT_TAY[i];
            }
        }
        return null;
    }

    /** Như tinh(..., duoiVat, out) với số Pokédex (bảng neo đặt tay). */
    public static void tinhLoai(int dex, int[] px, int w, int h, int cx, int cy, int[] pmd, int dir, int[] out) {
        pmdDex = dex;
        try {
            tinh(px, w, h, cx, cy, pmd, dir, dauDuoiVat(dex), out);
        } finally {
            pmdDex = 0;
        }
    }

    public static boolean dauDuoiVat(int dex) {
        for (int i = 0; i < DAU_DUOI_VAT.length; i++) {
            if (DAU_DUOI_VAT[i] == dex) {
                return true;
            }
        }
        return false;
    }

    public static void tinh(int[] px, int w, int h, int cx, int cy, int[] pmd, int dir, int[] out) {
        tinh(px, w, h, cx, cy, pmd, dir, false, out);
    }

    public static void tinh(int[] px, int w, int h, int cx, int cy, int[] pmd, int dir, boolean duoiVat, int[] out) {
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
        int matTren = KHONG, tong = 0, dem = 0, tongX = 0;
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
                    tongX += cum[tot * 6 + 1];
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
        int muSau = 0;
        if (duoiVat) {
            int[] t = dir == 1 ? null : datTay(pmdDex, dir == 3 ? 2 : dir);
            if (t != null) {
                // neo ĐẶT TAY (đã soát từng loài, theo neo PMD của chính khung nên chạy theo hoạt ảnh): đỉnh mặt, giữa mặt
                int dX = dir == 3 ? -t[3] : t[3];
                dinhY = cy + pmd[2] + t[2];
                dauX = cx + pmd[0] + dX;
                L = dauX - t[4] / 2;
                R = L + t[4] - 1;
                if (matTren != KHONG && matTren < dinhY - 1) {
                    matTren = KHONG;   // "mắt" phía trên mặt = điểm tối trong lá / củ, không phải mắt
                    matY = KHONG;
                } else if (matTren != KHONG && dinhY > matTren - 2) {
                    dinhY = matTren - 2;   // không bao giờ xuống tới mắt (khung gục / đánh: mặt lệch)
                }
            } else if (dir == 1) {
                muSau = 1;   // quay lưng: đầu khuất sau củ / hoa -> mũ vẽ sau thân, chỉ ló phần thò ra
                dinhY = Math.max(dinhY, hy - 2);
            }
        }
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
        if (coY <= dinhY + 2) {
            // đỉnh đầu đặt tay thấp hơn cổ ước lượng: cổ ngay dưới mắt / dưới đầu
            coY = Math.min(Math.max(tu, matY != KHONG ? matY + 2 : dinhY + 5), Math.max(dinhY + 3, cy + pmd[6]));
            if (coY <= dinhY + 2) {
                coY = dinhY + 3;
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
        out[MU_SAU] = muSau;
    }

    static int sac(int c) {
        int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
        int mx = Math.max(r, Math.max(g, b)), mn = Math.min(r, Math.min(g, b));
        if (mx == mn) {
            return -1;
        }
        int d = mx - mn, hh;
        if (mx == r) {
            hh = 60 * (g - b) / d;
        } else if (mx == g) {
            hh = 120 + 60 * (b - r) / d;
        } else {
            hh = 240 + 60 * (r - g) / d;
        }
        return hh < 0 ? hh + 360 : hh;
    }

    static boolean cungSac(int c, int hue) {
        int s = sac(c);
        if (s < 0 || bao(c) < 24 || sangDo(c) < 55) {
            return false;
        }
        int d = Math.abs(s - hue);
        return Math.min(d, 360 - d) <= 26;
    }

    /**
     * Vùng da mặt quanh (hx, hy): {đỉnh, trái, phải} (bề ngang ở 2 hàng dưới đỉnh), null nếu không tìm thấy hạt giống da.
     * Chỉ lấy phần vùng nằm từ hàng điểm đầu trở lên (thân cùng màu da ở dưới không tính).
     */
    static int[] vungDa(int[] px, int w, int h, int hx, int hy) {
        int seed = -1;
        for (int r = 0; r <= 3 && seed < 0; r++) {
            for (int dy = -r; dy <= r && seed < 0; dy++) {
                for (int dx = -r; dx <= r && seed < 0; dx++) {
                    int x = hx + dx, y = hy + dy;
                    if (dac(px, w, h, x, y) && sac(px[y * w + x]) >= 0 && bao(px[y * w + x]) >= 24 && sangDo(px[y * w + x]) >= 70) {
                        seed = y * w + x;
                    }
                }
            }
        }
        if (seed < 0) {
            return null;
        }
        int hue = sac(px[seed]);
        boolean[] da = new boolean[w * h];
        int[] hang = new int[w * h];
        int dau = 0, cuoi = 0;
        hang[cuoi++] = seed;
        da[seed] = true;
        int dinh = seed / w;
        while (dau < cuoi) {
            int j = hang[dau++];
            int x = j % w, y = j / w;
            if (y < dinh) {
                dinh = y;
            }
            int[] k = {j - 1, j + 1, j - w, j + w};
            for (int t = 0; t < 4; t++) {
                int q = k[t];
                if (q < 0 || q >= w * h || da[q] || (t < 2 && q / w != y)) {
                    continue;
                }
                if (q / w > hy + 2) {
                    continue;   // chỉ phần trên (mặt), không loang xuống thân
                }
                if (dac(px, w, h, q % w, q / w) && cungSac(px[q], hue)) {
                    da[q] = true;
                    hang[cuoi++] = q;
                }
            }
        }
        // đỉnh: hàng cao nhất có >= 2 điểm da (bỏ điểm lẻ); viền ngoài ngay trên đó là hàng đỉnh đầu
        int top = -1;
        for (int y = dinh; y <= hy && top < 0; y++) {
            int n = 0;
            for (int x = 0; x < w; x++) {
                if (da[y * w + x]) {
                    n++;
                }
            }
            if (n >= 2) {
                top = y;
            }
        }
        if (top < 0) {
            return null;
        }
        int yy = Math.min(hy, top + 2), L = w, R = -1;
        for (int x = 0; x < w; x++) {
            if (da[yy * w + x]) {
                L = Math.min(L, x);
                R = Math.max(R, x);
            }
        }
        if (R - L < 3) {
            return null;
        }
        return new int[]{Math.max(0, top - 1), L, R};
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
