package game.pokemon;

import game.model.Char;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Tab "Tiềm năng" (WindowInfoScr / ShopNew) kiểu Pokémon - thay ảnh inv_1 kiếm hiệp (hàng chỉ số
 * có kiếm, cuộn giấy...). Vẽ trong hệ tọa độ nội dung của tab, giữ đúng bố cục cũ để phím / chạm
 * của bảng gốc không đổi: hàng "Điểm cộng" ở y = -20, 5 hàng chỉ số ở y = i * 19 (cao 18).
 */
public final class BangTiemNang {
    private static final String[] TEN = {"Sức mạnh", "Khéo léo", "Tinh thần", "Sức khỏe", "May mắn"};
    /** hình biểu tượng (KieuO.HINH_*) + hệ lấy màu cho từng chỉ số */
    private static final byte[] HINH = {KieuO.HINH_VA, KieuO.HINH_NHANH, KieuO.HINH_SAO, KieuO.HINH_TIM, KieuO.HINH_MAT_TROI};
    private static final byte[] HE = {6, 9, 10, 17, 3};

    private BangTiemNang() {
    }

    private static void oBieuTuong(Graphics g, int x, int y, int mau, int hinh) {
        GiaoDien.khoi(g, x, y, 16, 16, 5, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, 14, 14, 4, GiaoDien.pha(mau, GiaoDien.TRANG, 1, 4), GiaoDien.pha(mau, 0x000000, 1, 3));
        Image a = KieuO.anhHinh(hinh, 1);
        if (a != null) {
            g.drawImage(a, x + 8, y + 8, Graphics.HCENTER | Graphics.VCENTER);
        }
    }

    private static void vienSo(Graphics g, int xPhai, int y, String s, boolean sang) {
        Font f = Font.borderFont;
        int w = f.getWidth(s) + 10;
        if (w < 22) {
            w = 22;
        }
        GiaoDien.khoi(g, xPhai - w, y, w, 14, 6, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, xPhai - w + 1, y + 1, w - 2, 12, 5, sang ? GiaoDien.VANG_NHAT : 0x3A5F96, sang ? GiaoDien.VANG_TOI : 0x23497F);
        f.drawString(g, s, xPhai - w / 2, y, Font.CENTER);
    }

    /** w = bề rộng vùng nội dung; chon = hàng đang chọn (0..4), doiTab = đang chọn tab (không tô hàng). */
    public static void ve(Graphics g, Char c, int chon, boolean doiTab, int w) {
        if (c == null) {
            return;
        }
        int r = w + 4;
        if (r < 100) {
            r = 100;
        }
        int x0 = -2;
        int diem = c.basePointLeft;
        // ---- hàng điểm cộng
        GiaoDien.khoiDoc(g, x0, -20, r, 18, 4, diem > 0 ? 0x4E9A3C : 0x2A4F82, diem > 0 ? 0x2F6E25 : 0x1D3F70);
        oBieuTuong(g, x0 + 1, -19, GiaoDien.VANG, KieuO.HINH_CONG);
        Font.borderFont.drawString(g, "Điểm cộng", x0 + 21, -18, Font.LEFT);
        vienSo(g, x0 + r - 3, -18, String.valueOf(diem), diem > 0);
        // ---- 5 chỉ số
        int[] gt = {c.strength, c.agility, c.spirit, c.health, c.luck};
        for (int i = 0; i < 5; i++) {
            int y = i * 19;
            boolean dangChon = !doiTab && i == chon;
            if (dangChon) {
                GiaoDien.chon(g, x0, y, r, 18);
            } else {
                GiaoDien.khoi(g, x0, y, r, 18, 4, (i & 1) == 0 ? KieuO.O_CHAN : KieuO.O_LE);
            }
            oBieuTuong(g, x0 + 1, y + 1, KieuO.mauHe(HE[i]), HINH[i]);
            Font.arialFontW.drawString(g, TEN[i], x0 + 21, y + 3, Font.LEFT);
            int xPhai = x0 + r - 3;
            if (dangChon && diem > 0) {
                // nút "+" vàng: bấm chọn để cộng điểm
                GiaoDien.khoi(g, xPhai - 12, y + 2, 12, 14, 4, GiaoDien.VIEN);
                GiaoDien.khoiDoc(g, xPhai - 11, y + 3, 10, 12, 3, GiaoDien.VANG_NHAT, GiaoDien.VANG);
                g.setColor(GiaoDien.VIEN);
                g.fillRect(xPhai - 9, y + 8, 6, 2);
                g.fillRect(xPhai - 7, y + 6, 2, 6);
                xPhai -= 14;
            }
            vienSo(g, xPhai, y + 2, String.valueOf(gt[i]), false);
        }
    }
}
