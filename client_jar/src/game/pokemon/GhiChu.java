package game.pokemon;

import javax.microedition.lcdui.Graphics;

/**
 * GHI CHỮ + KHUNG ĐÃ VẼ (0.9.7, kiểm thử bố cục chữ): mỗi lần vẽ chữ (ChuDe.chu / chuVien, Font.drawString của màn cũ)
 * ghi hộp chữ (toạ độ tuyệt đối) và mỗi khung pixel (KhungNro.ve) ghi hình chữ nhật + thứ tự vẽ. Kiểm thử
 * (ChupTrongGame.kiemChu) đòi: chữ nằm trọn trong khung nhỏ nhất chứa điểm đầu dòng, hai dòng chữ không chồng nhau,
 * không ký tự thiếu nét. Tắt mặc định (bat = false) - không tốn gì khi chơi.
 */
public final class GhiChu {
    private GhiChu() {
    }

    public static boolean bat;
    public static final int TOI_DA = 2048;
    public static int soChu, soKhung;
    /** phần THẤY ĐƯỢC của khung (cắt theo vùng cắt lúc vẽ; w <= 0 = bị cắt hết) - kiểm khung trong màn */
    public static final int[] KVX = new int[2048], KVY = new int[2048], KVW = new int[2048], KVH = new int[2048];
    /** 0.9.7: hộp BIỂU TƯỢNG đã vẽ (TienTe, BieuTuong): kiểm chữ không đè biểu tượng */
    public static int soBieu;
    public static final int[] BX = new int[TOI_DA], BY = new int[TOI_DA], BW = new int[TOI_DA], BH = new int[TOI_DA], BT = new int[TOI_DA];
    public static final String[] BS = new String[TOI_DA];
    public static final int[] CX = new int[TOI_DA], CY = new int[TOI_DA], CW = new int[TOI_DA], CH = new int[TOI_DA], CT = new int[TOI_DA];
    public static final String[] CS = new String[TOI_DA];
    public static final int[] KX = new int[TOI_DA], KY = new int[TOI_DA], KW = new int[TOI_DA], KH = new int[TOI_DA], KT = new int[TOI_DA],
            KM = new int[TOI_DA];
    /** thứ tự vẽ chung (chữ / khung) */
    static int thuTu;
    /** đang trong ChuDe (không ghi lại lần nữa ở Font.drawString) */
    public static int trongChuDe;

    public static void xoa() {
        soChu = 0;
        soKhung = 0;
        soBieu = 0;
        thuTu = 0;
    }

    /** Hộp chữ s (x, y = góc trái trên tuỳ canh 0 trái / 1 phải / 2 giữa) rộng w cao h. */
    public static void chu(Graphics g, String s, int x, int y, int w, int h, int canh) {
        if (!bat || s == null || s.length() == 0 || soChu >= TOI_DA) {
            return;
        }
        int ax = x + g.getTranslateX() - (canh == 2 ? w / 2 : canh == 1 ? w : 0);
        int ay = y + g.getTranslateY();
        // bỏ chữ ngoài vùng cắt hiện tại (danh sách cuộn)
        int kx = g.getClipX() + g.getTranslateX(), ky = g.getClipY() + g.getTranslateY();
        int kw = g.getClipWidth(), kh = g.getClipHeight();
        if (ax + w <= kx || ay + h <= ky || ax >= kx + kw || ay >= ky + kh) {
            return;
        }
        int i = soChu++;
        CX[i] = ax;
        CY[i] = ay;
        CW[i] = w;
        CH[i] = h;
        CS[i] = s;
        CT[i] = thuTu++;
    }

    /** Hộp biểu tượng (toạ độ đang dịch của g). */
    public static void bieu(Graphics g, String ten, int x, int y, int w, int h) {
        if (!bat || soBieu >= TOI_DA || w <= 0 || h <= 0) {
            return;
        }
        int ax = x + g.getTranslateX(), ay = y + g.getTranslateY();
        int kx = g.getClipX() + g.getTranslateX(), ky = g.getClipY() + g.getTranslateY();
        if (ax + w <= kx || ay + h <= ky || ax >= kx + g.getClipWidth() || ay >= ky + g.getClipHeight()) {
            return;
        }
        int i = soBieu++;
        BX[i] = ax;
        BY[i] = ay;
        BW[i] = w;
        BH[i] = h;
        BS[i] = ten;
        BT[i] = thuTu++;
    }

    public static void khung(Graphics g, int manh, int x, int y, int w, int h) {
        if (!bat || soKhung >= TOI_DA) {
            return;
        }
        int i = soKhung++;
        KX[i] = x + g.getTranslateX();
        KY[i] = y + g.getTranslateY();
        KW[i] = w;
        KH[i] = h;
        KM[i] = manh;
        KT[i] = thuTu++;
        int cx = g.getClipX() + g.getTranslateX(), cy = g.getClipY() + g.getTranslateY();
        int x0 = Math.max(KX[i], cx), y0 = Math.max(KY[i], cy);
        int x1 = Math.min(KX[i] + w, cx + g.getClipWidth()), y1 = Math.min(KY[i] + h, cy + g.getClipHeight());
        KVX[i] = x0;
        KVY[i] = y0;
        KVW[i] = x1 - x0;
        KVH[i] = y1 - y0;
    }
}
