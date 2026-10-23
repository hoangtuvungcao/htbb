package game.pokemon;

import game.model.Command;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * 0.9.6.1: THẺ CHI TIẾT VẬT PHẨM dùng chung (jar / APK / PC) cho hộp chi tiết cũ của Cửa hàng (ShopNew) và Hành trang /
 * cửa hàng NPC (WindowInfoScr) - thay hộp 125 px kiểu J2ME (tên bị ngắt thành dòng "thân" giả, nút mềm đáy màn đè lên
 * mô tả, nút "Mua" lòi khỏi thẻ, giá nằm dưới nút).
 *
 * - Chữ vào là chuỗi cũ "\n<màu><dòng>..." (ItemInInventory.setTemp đã ngắt sẵn 95 px theo phông ảnh): các dòng nối
 *   tiếp của CÙNG một đoạn (setTemp ghi lại bằng {@link #ghiNoi}) được nối lại rồi ngắt lại theo phông thật (ChuDe) và
 *   bề ngang thẻ thật. Đoạn đầu = tên (tiêu đề, ngắt cả cụm tối đa 3 dòng, cạnh biểu tượng vật phẩm); đoạn "Giá: ..."
 *   = hàng giá có biểu tượng tiền tệ.
 * - Thẻ canh giữa vùng an toàn, rộng <= 80 %, cao theo nội dung; quá cao thì phần thân cuộn (kéo / phím lên xuống),
 *   tiêu đề và hàng nút đứng yên. Nút (phím mềm của màn: trái / giữa / phải) nằm ở HÀNG CHÂN trong thẻ, dưới mọi chữ.
 */
public final class HopChiTiet {
    private HopChiTiet() {
    }

    private static final java.util.Hashtable NOI = new java.util.Hashtable();

    /** lớp Command thường (không vẽ gì) - không dùng class literal: máy J2ME (class 45) không có */
    private static final Class LENH_THUONG = new Command("", null).getClass();

    /** o là lệnh ô có vẽ biểu tượng (lớp con của Command ghi đè paint). */
    public static boolean coHinh(Object o) {
        return o instanceof Command && o.getClass() != LENH_THUONG;
    }

    /** setTemp: dòng (đã có tiền tố màu) là phần NỐI TIẾP của dòng trước trong cùng một đoạn. */
    public static void ghiNoi(String dong) {
        if (dong == null) {
            return;
        }
        if (NOI.size() > 600) {
            NOI.clear();
        }
        NOI.put(dong.replace((char) 194, (char) 226), Boolean.TRUE);
    }

    // ------------------------------------------------------------------ tách đoạn (có bộ đệm theo chuỗi vào)

    private static String daTach;
    private static String[] doan = new String[0];
    private static int[] mauDoan = new int[0];

    private static void tach(String text) {
        if (text == null) {
            text = "";
        }
        if (text.equals(daTach)) {
            return;
        }
        java.util.Vector ds = new java.util.Vector(), mau = new java.util.Vector();
        int i = 0;
        while (i <= text.length()) {
            int j = text.indexOf('\n', i);
            if (j < 0) {
                j = text.length();
            }
            String dong = text.substring(i, j);
            i = j + 1;
            if (dong.length() == 0) {
                continue;
            }
            int m = 0;
            String s = dong;
            if (dong.charAt(0) >= '0' && dong.charAt(0) <= '9') {
                m = dong.charAt(0) - '0';
                s = dong.substring(1);
            }
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            int n = ds.size();
            if (n > 0 && NOI.containsKey(dong) && ((Integer) mau.elementAt(n - 1)).intValue() == m) {
                ds.setElementAt(ds.elementAt(n - 1) + " " + s, n - 1);
            } else {
                ds.addElement(s);
                mau.addElement(new Integer(m));
            }
        }
        doan = new String[ds.size()];
        mauDoan = new int[ds.size()];
        for (int k = 0; k < doan.length; k++) {
            doan[k] = (String) ds.elementAt(k);
            mauDoan[k] = ((Integer) mau.elementAt(k)).intValue();
        }
        daTach = text;
    }

    /** Đoạn "Giá: 50 lượng" -> {nhãn "Giá:", loại tiền, số} hoặc null. */
    static Object[] gia(String s) {
        int c = s.indexOf(": ");
        if (c < 0 || !s.startsWith("Giá")) {
            return null;
        }
        String con = s.substring(c + 2).trim();
        int loaiTot = -1, dai = 0;
        for (int l = 0; l < TienTe.SO_LOAI; l++) {
            String t = TienTe.ten(l);
            if (t.length() > dai && con.endsWith(" " + t)) {
                loaiTot = l;
                dai = t.length();
            }
        }
        if (loaiTot < 0) {
            return null;
        }
        String so = con.substring(0, con.length() - dai).trim();
        if (so.length() == 0) {
            return null;
        }
        for (int k = 0; k < so.length(); k++) {
            char ch = so.charAt(k);
            if ((ch < '0' || ch > '9') && ch != '.' && ch != ',' && ch != '-') {
                return null;
            }
        }
        return new Object[]{s.substring(0, c + 1), new Integer(loaiTot), so};
    }

    /**
     * Màu dòng mô tả theo mã màu chữ đầu dòng của máy chủ (mã phông cũ 1..5, KHÔNG phải độ hiếm - tên món dùng
     * KieuO.mauHiem theo colorName): 1 xanh lá, 2 xanh dương, 3 tím, 4 vàng, 5 đỏ, pha tối cho dễ đọc trên thẻ sáng.
     */
    static int mau(int m) {
        return m <= 0 || m >= 6 ? ChuDe.CHU : GiaoDien.pha(MAU_DONG[m], 0, 2, 5);
    }

    private static final int[] MAU_DONG = {0, 0x48D058, 0x3FA0F0, 0xC060F0, 0xFFCB05, 0xF04830};

    // ------------------------------------------------------------------ kiểm thử: hình học khung vừa vẽ

    /** kiểm thử: các đoạn (đoạn 0 = tên) của thẻ vừa vẽ */
    public static String[] doanKiem() {
        String[] r = new String[doan.length];
        System.arraycopy(doan, 0, r, 0, r.length);
        return r;
    }

    /** kiểm thử: đoạn là hàng giá (vẽ biểu tượng tiền) */
    public static boolean laGia(String s) {
        return gia(s) != null;
    }

    /** {x, y, w, h} thẻ vừa vẽ */
    public static final int[] VUA = new int[4];
    /** hình chữ nhật mọi dòng chữ / hàng giá vừa vẽ (trong vùng thấy) và mọi nút: {x, y, w, h} */
    public static final int[][] CHU = new int[80][4], NUT = new int[4][4];
    public static int soChu, soNut, demVe;

    /** 0.9.11: thanh cuộn thân vẽ lần cuối (toạ độ màn): cột thX, rãnh thY..thY+thH, núm thN, giới hạn cuộn thLim */
    static int thX, thY, thH, thN, thLim;
    /** lượt kéo đang nắm núm thanh cuộn (không phải kéo chữ) */
    public static boolean keoNum;
    static int numDau, pyNumDau;

    /**
     * 0.9.11: gọi khi bắt đầu một lượt kéo trong thẻ (cuon = độ cuộn hiện tại). Nhấn (Canvas.pxLast / pyLast) lên thanh
     * cuộn thân = kéo NÚM (cùng chiều con trỏ, theo tỉ lệ), lên rãnh = nhảy tới chỗ nhấn. Trả keoNum.
     */
    public static boolean batDauKeo(int cuon) {
        keoNum = thH > 0 && thLim > 0 && ThanhCuon.trung(Canvas.pxLast, Canvas.pyLast, thX, 3, thY, thH);
        if (keoNum) {
            numDau = Math.max(0, Math.min(thLim, cuon));
            if (!ThanhCuon.trenNum(Canvas.pyLast, thY, thH, thN, numDau, thLim)) {
                numDau = ThanhCuon.toi(Canvas.pyLast, thY, thH, thN, thLim);
            }
            pyNumDau = Canvas.pyLast;
        }
        return keoNum;
    }

    /** Độ cuộn khi đang kéo núm (con trỏ ở Canvas.py). */
    public static int cuonKeoNum() {
        return ThanhCuon.keo(numDau, pyNumDau, Canvas.py, thH, thN, thLim);
    }

    private static void ghiChu(int x, int y, int w, int h) {
        if (soChu < CHU.length) {
            CHU[soChu][0] = x;
            CHU[soChu][1] = y;
            CHU[soChu][2] = w;
            CHU[soChu][3] = h;
            soChu++;
        }
    }

    // ------------------------------------------------------------------ vẽ

    /**
     * Vẽ thẻ (toạ độ màn, tự resetTrans). kham = ô khảm ngọc (null = không), icon = lệnh có paint(g, cx, cy) vẽ biểu
     * tượng (null = không), trai / giua / phai = phím mềm của màn (null / chữ rỗng = không có nút), cuon = độ cuộn thân.
     * Trả {x, y, w, h, giới hạn cuộn}.
     */
    public static int[] ve(Graphics g, String text, byte[] kham, Command icon, Command trai, Command giua, Command phai, int cuon) {
        Canvas.resetTrans(g);
        tach(text);
        soChu = 0;
        soNut = 0;
        demVe++;
        boolean dt = ChuDe.dt();
        int sw = Bang.phai() - Bang.trai();
        int w = Math.min(sw * 80 / 100, dt ? 340 : 300);
        w = Math.max(w, Math.min(sw, 200));
        int pad = ChuDe.DEM + 2;
        int s = icon != null ? (dt ? 34 : 28) : 0;
        String ten = doan.length > 0 ? doan[0] : "";
        int wTen = w - 2 * pad - (s > 0 ? s + 8 : 0);
        String[] dongTen = ChuDe.ngat(ten, ChuDe.TIEU_DE, wTen, 3);
        int lhTen = ChuDe.cao(ChuDe.TIEU_DE) + 1;
        int hTen = Math.max(s, dongTen.length * lhTen);
        int hDau = pad + hTen + 6;
        // thân: mỗi đoạn ngắt theo bề ngang thật
        int wThan = w - 2 * pad;
        int lh = ChuDe.cao(ChuDe.THUONG) + 2;
        int hGia = Math.max(lh, 18);
        int hKham = kham != null && kham.length > 0 ? 22 : 0;
        java.util.Vector hang = new java.util.Vector();   // String[] dòng hoặc Object[] giá
        int hThan = hKham;
        for (int d = 1; d < doan.length; d++) {
            Object[] gi = gia(doan[d]);
            if (gi != null) {
                hang.addElement(gi);
                hThan += hGia;
            } else {
                String[] l = ChuDe.ngat(doan[d], ChuDe.THUONG, wThan, 40);
                hang.addElement(new Object[]{l, new Integer(mau(mauDoan[d]))});
                hThan += l.length * lh;
            }
        }
        // nút chân
        Command[] cmd = new Command[3];
        int n = 0;
        Command[] thu = {phai, giua, trai};   // trái -> phải: Đóng (phụ), giữa, hành động chính
        for (int k = 0; k < thu.length; k++) {
            Command c = thu[k];
            if (c != null && c.caption != null && c.caption.length() > 0) {
                boolean trung = false;
                for (int q = 0; q < n; q++) {
                    trung |= cmd[q] == c;
                }
                if (!trung) {
                    cmd[n++] = c;
                }
            }
        }
        int hNut = dt ? 32 : 26;
        int hChan = n > 0 ? hNut + pad + 6 : pad;
        int hMax = Bang.caoTamToiDa();
        int hThanHien = hThan;
        int h = hDau + 1 + 6 + hThan + hChan;
        if (h > hMax) {
            hThanHien = Math.max(lh, hThan - (h - hMax));
            h = hDau + 1 + 6 + hThanHien + hChan;
        }
        int lim = Math.max(0, hThan - hThanHien);
        if (cuon > lim) {
            cuon = lim;
        }
        if (cuon < 0) {
            cuon = 0;
        }
        int x = Bang.trai() + (sw - w) / 2;
        int y = Bang.tren() + (Bang.duoi() - Bang.tren() - h) / 2;
        VUA[0] = x;
        VUA[1] = y;
        VUA[2] = w;
        VUA[3] = h;
        ChuDe.phuMan(g);
        Bang.the(g, x, y, w, h);
        // tên món + vạch đầu thẻ theo bậc hiếm chung (DoHiemSinh, cùng màu web / máy chủ); Thường: chữ thường, vạch màu chính
        int mauTen = doan.length > 0 && mauDoan[0] > 0 ? KieuO.mauHiem(mauDoan[0]) : -1;
        int mauChuTen = mauTen >= 0 ? mauTen : ChuDe.CHU;
        if (mauTen < 0) {
            mauTen = ChuDe.CHINH;
        }
        GiaoDien.khoi(g, x + pad, y + 3, w - 2 * pad, 3, 1, mauTen);
        // đầu: biểu tượng + tên
        int yTen = y + pad;
        int xTen = x + pad;
        if (s > 0) {
            int cx = x + pad + s / 2, cy = yTen + hTen / 2;
            GiaoDien.khoi(g, cx - s / 2, cy - s / 2, s, s, ChuDe.BO_O, ChuDe.NEN_PHU);
            try {
                icon.paint(g, cx, cy);
            } catch (Throwable e) {
                // biểu tượng lỗi: bỏ
            }
            Canvas.resetTrans(g);
            xTen += s + 8;
        }
        int yDong = yTen + (hTen - dongTen.length * lhTen) / 2;
        for (int k = 0; k < dongTen.length; k++) {
            ChuDe.chu(g, dongTen[k], xTen, yDong, ChuDe.TIEU_DE, mauChuTen, 0);
            ghiChu(xTen, yDong, ChuDe.rong(dongTen[k], ChuDe.TIEU_DE), lhTen);
            yDong += lhTen;
        }
        int yKe = y + hDau;
        g.setColor(ChuDe.KE);
        g.fillRect(x + pad, yKe, w - 2 * pad, 1);
        // thân (cuộn trong vùng của nó)
        int yThan = yKe + 1 + 6;
        g.setClip(x + 1, yThan, w - 2, hThanHien);
        int yy = yThan - cuon;
        if (hKham > 0) {
            for (int j = 0; j < kham.length; j++) {
                int cx = x + pad + 9 + j * 22, cy = yy + 10;
                GiaoDien.khoi(g, cx - 9, cy - 9, 18, 18, 9, ChuDe.NEN_PHU_TOI);
                if (kham[j] != -1) {
                    game.render.Res.paintGem(g, kham[j], cx, cy);
                }
            }
            yy += hKham;
        }
        for (int k = 0; k < hang.size(); k++) {
            Object[] o = (Object[]) hang.elementAt(k);
            if (o[0] instanceof String) {
                // hàng giá: "Giá:" + biểu tượng tiền + số
                String nhan = (String) o[0];
                int loai = ((Integer) o[1]).intValue();
                String so = (String) o[2];
                int co = dt ? 16 : 14;
                int xx = x + pad;
                int ty = yy + (hGia - ChuDe.cao(ChuDe.NHAN)) / 2;
                ChuDe.chu(g, nhan, xx, ty, ChuDe.NHAN, ChuDe.CHU_PHU, 0);
                xx += ChuDe.rong(nhan, ChuDe.NHAN) + 5;
                TienTe.ve(g, loai, xx, yy + (hGia - co) / 2, co);
                xx += TienTe.rong(co) + 3;
                ChuDe.chu(g, so, xx, yy + (hGia - ChuDe.cao(ChuDe.SO)) / 2, ChuDe.SO, ChuDe.CHU, 0);
                int wGia = xx + ChuDe.rong(so, ChuDe.SO) - (x + pad);
                if (yy + hGia > yThan && yy < yThan + hThanHien) {
                    ghiChu(x + pad, Math.max(yy, yThan), wGia, Math.min(yy + hGia, yThan + hThanHien) - Math.max(yy, yThan));
                }
                yy += hGia;
            } else {
                String[] l = (String[]) o[0];
                int m = ((Integer) o[1]).intValue();
                for (int q = 0; q < l.length; q++) {
                    if (yy + lh > yThan && yy < yThan + hThanHien) {
                        ChuDe.chu(g, l[q], x + pad, yy + 1, ChuDe.THUONG, m, 0);
                        ghiChu(x + pad, Math.max(yy, yThan), ChuDe.rong(l[q], ChuDe.THUONG),
                                Math.min(yy + lh, yThan + hThanHien) - Math.max(yy, yThan));
                    }
                    yy += lh;
                }
            }
        }
        Canvas.resetTrans(g);
        thH = 0;
        if (lim > 0) {
            // thanh cuộn mảnh bên phải thân
            int th = Math.max(10, hThanHien * hThanHien / Math.max(1, hThan));
            int ty = yThan + (hThanHien - th) * cuon / Math.max(1, lim);
            GiaoDien.khoi(g, x + w - 5, ty, 3, th, 1, ChuDe.CHU_NHAT);
            thX = x + w - 5;
            thY = yThan;
            thH = hThanHien;
            thN = Math.min(th, hThanHien);
            thLim = lim;
        }
        // hàng chân: nút trong thẻ, dưới mọi chữ
        if (n > 0) {
            int k = 8;
            int yNut = y + h - pad - hNut;
            g.setColor(ChuDe.KE);
            g.fillRect(x + pad, yNut - 6, w - 2 * pad, 1);
            int bw = Math.min((w - 2 * pad - (n - 1) * k) / n, dt ? 150 : 130);
            int tong = n * bw + (n - 1) * k;
            int bx = x + (w - tong) / 2;
            for (int q = 0; q < n; q++) {
                int kieu = q == n - 1 && n > 1 ? Bang.CHINH : Bang.TRUNG;
                if (n == 1) {
                    kieu = Bang.TRUNG;
                }
                Bang.nut(g, bx, yNut, bw, hNut, cmd[q].caption, kieu, cmd[q], "ct-nut-" + q);
                if (soNut < NUT.length) {
                    NUT[soNut][0] = bx;
                    NUT[soNut][1] = yNut;
                    NUT[soNut][2] = bw;
                    NUT[soNut][3] = hNut;
                    soNut++;
                }
                bx += bw + k;
            }
        }
        return new int[]{x, y, w, h, lim};
    }
}
