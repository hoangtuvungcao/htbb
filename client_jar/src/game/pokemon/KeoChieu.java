package game.pokemon;

import game.model.Char;
import game.model.MainChar;
import game.model.QuickSlot;
import game.render.Canvas;
import game.render.screen.GameScr;
import game.render.screen.RMS;
import javax.microedition.lcdui.Graphics;

/**
 * KÉO THẢ CHIÊU VÀO Ô NHANH (0.9.22, mọi bản có con trỏ: PC chuột, APK / J2ME cảm ứng) - màn Nhân vật › Kỹ năng.
 *
 * Chủ dự án: "kéo thả chiêu ... ảo ảo, chưa được như các trò chơi khác". Mô hình như game di động hiện nay:
 * <ul>
 *   <li><b>Bắt đầu</b>: nhấn lên Ô NHANH có chiêu / thuốc hoặc lên HÌNH CHIÊU (tay nắm) ở danh sách rồi kéo quá NGƯỠNG
 *       {@link #slop()} (~10 px logic theo cỡ chạm); hoặc NHẤN GIỮ {@link #GIU_MS} ms trên cả hàng chiêu. Dưới ngưỡng =
 *       chạm thường (mở bảng gán / chi tiết như cũ). Kéo dọc trên hàng (không phải tay nắm) = cuộn danh sách, không kéo chiêu.</li>
 *   <li><b>Bóng chiêu</b> theo con trỏ 1:1 MỖI KHUNG VẼ từ điểm con trỏ mới nhất (Canvas.px / py ghi theo sự kiện, không
 *       đợi nhịp logic), GIỮ chỗ nắm (điểm nắm trên hình không nhảy về tâm), to hơn 15 % + bóng đổ.</li>
 *   <li><b>Đích</b>: ô nhanh GẦN NHẤT trong bán kính hút {@link #banKinhHut} (cả khe giữa hai ô) sáng lên.</li>
 *   <li><b>Thả</b> trên ô = cập nhật NGAY (ô nhanh chỉ lưu ở máy - RMS, không chờ máy chủ): ô → ô = ĐỔI CHỖ; chiêu → ô =
 *       gán, chiêu đã nằm ở ô khác cùng trang thì đổi chỗ (không trùng hai ô). Thả ngoài / chiêu chưa học / huỷ = bóng TRƯỢT
 *       VỀ chỗ cũ ({@link #VE_MS} ms), không đổi gì.</li>
 *   <li><b>Huỷ</b> ({@link #huy}): hệ thống huỷ chạm (APK ACTION_CANCEL), rời app, hộp thoại / menu mở, rời tab (không vẽ
 *       khung nào {@link #CU_MS} ms) - không bao giờ kẹt trạng thái kéo. Một con trỏ (ngón thứ hai không vào game).</li>
 * </ul>
 * Phím J2ME giữ nguyên (ChonChieuNhanh: gán / bỏ trống / dời trái phải).
 *
 * Cách dùng (ManNhanVat.veKyNang, mỗi khung): {@link #khung} đầu tab -> {@link #nguonO} / {@link #dichO} /
 * {@link #nguonChieu} lúc vẽ -> {@link #ve} cuối tab. Vùng đăng ký của khung TRƯỚC dùng cho cú nhấn (như VungCham).
 * Toạ độ: màn jar tuyệt đối (đã cộng g.translate).
 */
public final class KeoChieu {
    private KeoChieu() {
    }

    public static final int NGHI = 0, CHO = 1, KEO = 2;
    public static final int O = 1, CHIEU = 2;
    /** nhấn giữ trên hàng chiêu để nhấc */
    public static final int GIU_MS = 280;
    /** bóng trượt về khi huỷ / thả sai */
    public static final int VE_MS = 160;
    /** ô vừa nhận chiêu sáng thêm */
    public static final int SANG_MS = 300;
    /** không vẽ tab lâu chừng này = đã rời tab: bỏ trạng thái */
    public static final int CU_MS = 400;
    /** bóng to hơn hình gốc (phần trăm) */
    public static final int TO = 115;

    static int trangThai;
    static int loai, giaTri, lop;
    /** hình nguồn (tuyệt đối) */
    static int nx, ny, ns;
    static boolean canGiu;
    static int soNhanDau = -1;
    static long lucNhan;
    static int xBam, yBam;
    /** chỗ nắm trên hình gốc (điểm nhấn - góc hình) */
    static int lechX, lechY;
    static int dich = -1;
    static long khungCuoi;
    /** bóng trượt về: từ (veX0, veY0) tới (veX1, veY1) cỡ veS0 -> veS1 */
    static long veLuc = -1;
    static int veX0, veY0, veX1, veY1, veS0, veS1, veKieu, veGiaTri;
    /** ô nhanh ẩn nội dung trong lúc bóng trượt (ô nguồn khi trượt về / ô đích khi rơi vào); -1 = không */
    static int veAnO = -1;
    /** nội dung đang kéo (chụp lúc nhấc): 1 chiêu (giaTriNd = chiêu), 2 thuốc (giaTriNd = loại thuốc) */
    static int kieuNd, giaTriNd;
    static int oVuaTha = -1;
    static long lucVuaTha;

    // vùng đăng ký: bộ khung này (dang) và khung trước (chot)
    static final int TOI_DA = 64;
    static int[][] dang = new int[TOI_DA][], chot = new int[TOI_DA][];
    static int nDang, nChot;
    static final int K_O = 1, K_DICH = 2, K_CHIEU = 3;

    /** Kiểm thử: số lần thả thành công / trượt về. */
    public static int soTha, soVe;

    public static int trangThai() {
        return trangThai;
    }

    public static boolean dangKeo() {
        return trangThai == KEO;
    }

    /** Ngưỡng bắt đầu kéo (px logic): ~ 1/5 cỡ chạm tối thiểu, ít nhất 8 (PC 32 px thật -> 8; Android 44 dp -> ~9-10). */
    public static int slop() {
        return Math.max(8, VungCham.MIN / 5);
    }

    /** Bán kính hút quanh ô đích (px logic, tính từ MÉP ô): thả lệch ra khe / ra ngoài một chút vẫn vào ô gần nhất. */
    public static int banKinhHut(int s) {
        return Math.max(s / 2, Math.max(slop() * 2, VungCham.MIN / 2));
    }

    /** Cuon: đang nắm tay nắm / đang kéo chiêu -> danh sách không cuộn. */
    static boolean chanCuon() {
        return trangThai == KEO || (trangThai == CHO && !canGiu);
    }

    // ------------------------------------------------------------------ đăng ký lúc vẽ

    static void them(int[] v) {
        if (nDang < TOI_DA) {
            dang[nDang++] = v;
        }
    }

    /** Ô nhanh o (hình x, y cỡ s theo g) có nội dung: nguồn kéo + đích thả. */
    public static void nguonO(Graphics g, int o, int x, int y, int s) {
        int tx = g == null ? 0 : g.getTranslateX(), ty = g == null ? 0 : g.getTranslateY();
        them(new int[]{K_O, o, x + tx, y + ty, s});
    }

    /** Ô nhanh o: đích thả (mọi ô, kể cả trống). */
    public static void dichO(Graphics g, int o, int x, int y, int s) {
        int tx = g == null ? 0 : g.getTranslateX(), ty = g == null ? 0 : g.getTranslateY();
        them(new int[]{K_DICH, o, x + tx, y + ty, s});
    }

    /** Chiêu sk đã học ở danh sách: hình (ix, iy, is) = tay nắm; hàng (rx, ry, rw, rh) = nhấn giữ. */
    public static void nguonChieu(Graphics g, int sk, int ix, int iy, int is, int rx, int ry, int rw, int rh) {
        int tx = g == null ? 0 : g.getTranslateX(), ty = g == null ? 0 : g.getTranslateY();
        if (g != null) {
            // hàng cắt bởi khung danh sách (cuộn): chỉ phần thấy được nhận nhấn; hình bị che quá nửa thì không là tay nắm
            int cx = g.getClipX(), cy = g.getClipY(), cw = g.getClipWidth(), ch = g.getClipHeight();
            int y0 = Math.max(ry, cy), y1 = Math.min(ry + rh, cy + ch);
            int x0 = Math.max(rx, cx), x1 = Math.min(rx + rw, cx + cw);
            if (y1 <= y0 || x1 <= x0) {
                return;
            }
            rx = x0;
            ry = y0;
            rw = x1 - x0;
            rh = y1 - y0;
            int my = iy + is / 2;
            if (my < cy || my >= cy + ch) {
                is = -1;   // không tay nắm (chỉ nhấn giữ hàng)
            }
        }
        them(new int[]{K_CHIEU, sk, ix + tx, iy + ty, is, rx + tx, ry + ty, rw, rh});
    }

    // ------------------------------------------------------------------ mỗi khung

    /**
     * Đầu tab Kỹ năng mỗi khung: chốt vùng khung trước, xử lý con trỏ (nhấn / ngưỡng / giữ / thả). cuon = danh sách chiêu
     * (cú nhấn hàng đã thành cuộn thì không nhấc).
     */
    public static void khung(int lopNay, Cuon cuon) {
        long now = System.currentTimeMillis();
        int[][] t = chot;
        chot = dang;
        nChot = nDang;
        dang = t;
        nDang = 0;
        if (trangThai != NGHI && now - khungCuoi > CU_MS) {
            trangThai = NGHI;   // rời tab / màn rồi quay lại: không kẹt
            dich = -1;
        }
        khungCuoi = now;
        lop = lopNay;
        if (Canvas.currentDialog != null || (Canvas.menu != null && Canvas.menu.showMenu)) {
            if (trangThai == KEO) {
                huy();
            } else {
                trangThai = NGHI;
            }
            return;
        }
        capNhat(now, cuon);
    }

    static void capNhat(long now, Cuon cuon) {
        if (trangThai == NGHI) {
            if (Canvas.dangGiu && Canvas.soNhan != soNhanDau) {
                soNhanDau = Canvas.soNhan;
                nhan(Canvas.xNhan, Canvas.yNhan, now);
            }
            if (trangThai == NGHI) {
                return;
            }
        }
        int px = Canvas.px, py = Canvas.py;
        if (trangThai == CHO) {
            if (!Canvas.dangGiu || Canvas.soNhan != soNhanDau) {
                trangThai = NGHI;   // thả dưới ngưỡng = chạm thường (VungCham xử lý)
                return;
            }
            int dx = px - xBam, dy = py - yBam, s = slop();
            boolean qua = dx * dx + dy * dy > s * s;
            if (canGiu) {
                if (qua || (cuon != null && cuon.keo && cuon.daKeo)) {
                    trangThai = NGHI;   // vuốt trên hàng = cuộn / chạm
                } else if (now - lucNhan >= GIU_MS) {
                    batKeo(cuon);
                }
            } else if (qua) {
                batKeo(cuon);
            }
            return;
        }
        if (trangThai == KEO) {
            if (!Canvas.dangGiu || Canvas.soNhan != soNhanDau) {
                tha(px, py, now);
            } else {
                dich = timDich(px, py);
                Canvas.pxLast = -9999;   // cú thả của lượt kéo không phải cú chạm vào ô / hàng
                Canvas.pyLast = -9999;
            }
        }
    }

    /** Cú nhấn mới tại (x, y): trúng ô có nội dung / hình chiêu / hàng chiêu -> chờ ngưỡng. */
    static void nhan(int x, int y, long now) {
        int[] tot = null;
        boolean giu = false;
        for (int i = nChot - 1; i >= 0 && tot == null; i--) {
            int[] v = chot[i];
            if ((v[0] == K_O || v[0] == K_CHIEU) && v[4] > 0 && trongHop(x, y, v[2], v[3], v[4], v[4], Math.max(2, (VungCham.MIN - v[4]) / 2))) {
                tot = v;
            }
        }
        for (int i = nChot - 1; i >= 0 && tot == null; i--) {
            int[] v = chot[i];
            if (v[0] == K_CHIEU && trongHop(x, y, v[5], v[6], v[7], v[8], 0)) {
                tot = v;
                giu = true;
            }
        }
        if (tot == null) {
            return;
        }
        loai = tot[0] == K_O ? O : CHIEU;
        giaTri = tot[1];
        nx = tot[2];
        ny = tot[3];
        ns = tot[4] > 0 ? tot[4] : Math.max(8, tot[8] - 6);
        canGiu = giu;
        xBam = x;
        yBam = y;
        // chỗ nắm: kẹp trong hình (nhấn ngoài hình - vùng nới / cả hàng - thì nắm mép gần nhất)
        lechX = Math.max(0, Math.min(ns - 1, x - nx));
        lechY = Math.max(0, Math.min(ns - 1, y - ny));
        lucNhan = now;
        dich = -1;
        trangThai = CHO;
    }

    static boolean trongHop(int x, int y, int hx, int hy, int w, int h, int noi) {
        return x >= hx - noi && y >= hy - noi && x < hx + w + noi && y < hy + h + noi;
    }

    static void batKeo(Cuon cuon) {
        kieuNd = 1;
        giaTriNd = giaTri;
        if (loai == O) {
            QuickSlot qs = null;
            try {
                qs = MainChar.quickSlot[GameScr.indexTabSlot][giaTri];
            } catch (Throwable e) {
                qs = null;
            }
            if (qs == null || qs.quickslotType < 0) {
                trangThai = NGHI;   // ô vừa trống (đổi trang giữa lúc nhấn): không nhấc
                return;
            }
            kieuNd = qs.quickslotType == QuickSlot.TYPE_POTION ? 2 : 1;
            giaTriNd = kieuNd == 2 ? qs.potionType : qs.skillType;
        }
        trangThai = KEO;
        veLuc = -1;
        if (cuon != null) {
            cuon.keo = false;
            cuon.v = 0;
        }
        Canvas.pxLast = -9999;
        Canvas.pyLast = -9999;
        dich = timDich(Canvas.px, Canvas.py);
        AmThanh.phat(AmThanh.CLICK);
    }

    /** Ô đích gần nhất cho con trỏ (x, y): khoảng cách tới MÉP ô <= bán kính hút, chọn ô có tâm gần nhất. -1 = không. */
    public static int timDich(int x, int y) {
        int tot = -1;
        long dTot = Long.MAX_VALUE;
        for (int i = 0; i < nChot; i++) {
            int[] v = chot[i];
            if (v[0] != K_DICH) {
                continue;
            }
            int s = v[4];
            int ex = Math.max(0, Math.max(v[2] - x, x - (v[2] + s - 1)));
            int ey = Math.max(0, Math.max(v[3] - y, y - (v[3] + s - 1)));
            int r = banKinhHut(s);
            if (ex * ex + ey * ey > r * r) {
                continue;
            }
            long cx = 2L * x - (2L * v[2] + s), cy = 2L * y - (2L * v[3] + s);
            long d = cx * cx + cy * cy;
            if (d < dTot) {
                dTot = d;
                tot = v[1];
            }
        }
        return tot;
    }

    static int[] vungDich(int o) {
        for (int i = 0; i < nChot; i++) {
            if (chot[i][0] == K_DICH && chot[i][1] == o) {
                return chot[i];
            }
        }
        return null;
    }

    /** Thả tại (x, y). */
    static void tha(int x, int y, long now) {
        int d = timDich(x, y);
        int[] g = bong(x, y);
        trangThai = NGHI;
        dich = -1;
        Canvas.pxLast = -9999;
        Canvas.pyLast = -9999;
        String loi = d < 0 ? null : apDung(d);
        if (d >= 0 && loi == null) {
            soTha++;
            oVuaTha = d;
            lucVuaTha = now;
            // bóng "rơi" vào ô: trượt ngắn từ chỗ thả vào đúng ô
            int[] v = vungDich(d);
            if (v != null) {
                batDauVe(g, v[2], v[3], v[4], now, d);
            }
            AmThanh.phat(AmThanh.XAC_NHAN);
            return;
        }
        if (loi != null) {
            DungDo.bao(loi);
        }
        soVe++;
        batDauVe(g, nx, ny, ns, now, loai == O ? giaTri : -1);
        AmThanh.phat(AmThanh.HUY);
    }

    static void batDauVe(int[] g, int x1, int y1, int s1, long now, int anO) {
        veX0 = g[0];
        veY0 = g[1];
        veS0 = g[2];
        veX1 = x1;
        veY1 = y1;
        veS1 = s1;
        veKieu = kieuNd;
        veGiaTri = giaTriNd;
        veAnO = anO;
        veLuc = now;
    }

    /**
     * Huỷ kéo đang có (APK ACTION_CANCEL / rời app / hộp thoại): bóng trượt về, không đổi ô. An toàn gọi bất cứ lúc nào.
     */
    public static void huy() {
        if (trangThai == KEO) {
            soVe++;
            batDauVe(bong(Canvas.px, Canvas.py), nx, ny, ns, System.currentTimeMillis(), loai == O ? giaTri : -1);
        }
        trangThai = NGHI;
        dich = -1;
        soNhanDau = Canvas.soNhan;   // cú nhấn đang giữ (nếu có) không nhấc lại
    }

    /** Áp dụng thả vào ô d (cập nhật ngay + lưu RMS). null = được; khác = lý do không được. */
    static String apDung(int d) {
        QuickSlot[] bo;
        try {
            bo = MainChar.quickSlot[GameScr.indexTabSlot];
        } catch (Throwable e) {
            return "Chưa có ô nhanh.";
        }
        if (bo == null || d < 0 || d >= bo.length) {
            return "Chưa có ô nhanh.";
        }
        if (bo[d] == null) {
            bo[d] = new QuickSlot();
        }
        if (loai == O) {
            if (giaTri != d) {
                OChieuNhanh.doiCho(bo, giaTri, d);
            }
        } else {
            int sk = giaTri;
            int cap = 0;
            try {
                cap = Char.skillLevelLearnt[sk];
            } catch (Throwable e) {
                cap = 0;
            }
            if (cap <= 0) {
                return "Học chiêu này trước rồi mới gán vào ô nhanh.";
            }
            int c = -1;
            for (int i = 0; i < bo.length; i++) {
                if (bo[i] != null && bo[i].quickslotType == QuickSlot.TYPE_SKILL && bo[i].skillType == sk) {
                    c = i;
                }
            }
            if (c >= 0) {
                OChieuNhanh.doiCho(bo, c, d);   // chiêu đã ở ô khác: dời sang ô đích, chiêu của ô đích về chỗ cũ
            } else {
                bo[d].setIsSkill(sk, lop, OChieuNhanh.laBuff(lop, sk));
            }
        }
        try {
            RMS.saveQuickSlot();
        } catch (Throwable e) {
            // không lưu được: vẫn đổi trong phiên
        }
        return null;
    }

    // ------------------------------------------------------------------ vẽ

    /** Bóng tại con trỏ (x, y): {x, y, cỡ} - to TO %, điểm nắm trên hình vẫn nằm dưới con trỏ. */
    public static int[] bong(int x, int y) {
        int s = ns * TO / 100;
        return new int[]{x - lechX * TO / 100, y - lechY * TO / 100, s};
    }

    /** Nguồn (O, ô) / (CHIEU, chiêu) đang được kéo: chỗ cũ vẽ mờ. */
    public static boolean nguonDangKeo(int l, int v) {
        return trangThai == KEO && loai == l && giaTri == v;
    }

    /** Ô nhanh o không vẽ nội dung khung này (đang kéo đi, hoặc bóng đang trượt về / rơi vào ô đó). */
    public static boolean anO(int o) {
        if (nguonDangKeo(O, o)) {
            return true;
        }
        return veLuc >= 0 && veAnO == o && System.currentTimeMillis() - veLuc < VE_MS;
    }

    /** Ô o đang sáng: đích dưới con trỏ, hoặc vừa nhận chiêu. */
    public static boolean sang(int o) {
        if (trangThai == KEO && dich == o) {
            return true;
        }
        return oVuaTha == o && System.currentTimeMillis() - lucVuaTha < SANG_MS;
    }

    /** Ô đích hiện tại (-1 = không) - kiểm thử. */
    public static int dich() {
        return trangThai == KEO ? dich : -1;
    }

    /** Cuối tab: bóng chiêu theo con trỏ (điểm mới nhất lúc vẽ) / bóng trượt về. */
    public static void ve(Graphics g) {
        long now = System.currentTimeMillis();
        BONG_VE = null;
        int x, y, s, k, v;
        if (trangThai == KEO) {
            dich = timDich(Canvas.px, Canvas.py);
            int[] b = bong(Canvas.px, Canvas.py);
            x = b[0];
            y = b[1];
            s = b[2];
            k = kieuNd;
            v = giaTriNd;
            BONG_VE = new int[]{x, y, s, Canvas.px, Canvas.py};
        } else if (veLuc >= 0 && now - veLuc < VE_MS) {
            // trượt về / rơi vào ô: chậm dần (1 - (1 - t)^2)
            int t = (int) ((now - veLuc) * 1024 / VE_MS);
            int e = 1024 - ((1024 - t) * (1024 - t) >> 10);
            x = veX0 + (veX1 - veX0) * e / 1024;
            y = veY0 + (veY1 - veY0) * e / 1024;
            s = veS0 + (veS1 - veS0) * e / 1024;
            k = veKieu;
            v = veGiaTri;
        } else {
            veLuc = -1;
            return;
        }
        Canvas.resetTrans(g);
        if (trangThai == KEO && dich >= 0) {
            // ô đích: viền vàng dày quanh ô (ngoài bóng - không bị bóng che hết)
            int[] d = vungDich(dich);
            if (d != null) {
                g.setColor(ChuDe.VANG);
                int e = d[4] * (TO - 100) / 200;   // bóng phóng to TO % che mép ô: viền nằm ngoài phần phóng
                for (int i = e + 1; i <= e + 3; i++) {
                    g.drawRect(d[2] - i, d[3] - i, d[4] + 2 * i - 1, d[4] + 2 * i - 1);
                }
            }
        }
        KieuO.phu(g, x + 3, y + 4, s, s, 0x70000000);
        if (k == 2) {
            Bang.oDo(g, x, y, s, true, true, null, null);
            ManNhanVat.veThuoc(g, v, x + s / 2, y + s / 2, s - 4);
        } else {
            KieuO.oChieuSk(g, x, y, s, s, lop, v, false);
        }
    }

    /** Kiểm thử: bóng vẽ ở khung cuối {x, y, cỡ, con trỏ x, con trỏ y}; null = khung cuối không vẽ bóng theo con trỏ. */
    public static int[] BONG_VE;

    /** Kiểm thử: vùng ô đích o đã chốt {x, y, cỡ}; null = không có. */
    public static int[] dichCua(int o) {
        int[] v = vungDich(o);
        return v == null ? null : new int[]{v[2], v[3], v[4]};
    }

    /** Kiểm thử: vùng chiêu sk ở danh sách đã chốt {hình x, y, cỡ, hàng x, y, w, h}; null = không có. */
    public static int[] chieuCua(int sk) {
        for (int i = 0; i < nChot; i++) {
            int[] v = chot[i];
            if (v[0] == K_CHIEU && v[1] == sk) {
                return new int[]{v[2], v[3], v[4], v[5], v[6], v[7], v[8]};
            }
        }
        return null;
    }

    /** Kiểm thử: chỗ nắm {lechX, lechY}. */
    public static int[] cho() {
        return new int[]{lechX, lechY};
    }

    /** Kiểm thử: đặt lại. */
    public static void datLai() {
        trangThai = NGHI;
        dich = -1;
        veLuc = -1;
        veAnO = -1;
        oVuaTha = -1;
        nDang = 0;
        nChot = 0;
        soTha = 0;
        soVe = 0;
        soNhanDau = Canvas.soNhan;
    }
}
