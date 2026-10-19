package game.pokemon;

import javax.microedition.lcdui.Graphics;

/**
 * Chữ giao diện bằng phông THẬT (0.9.5): bản PC (htbb.pc.ChuHD) và APK (com.htbb.pokemon.hd.HdChu) đặt {@link #bo};
 * máy J2ME để null -> ChuDe dùng font ảnh của game. Cỡ chữ tính bằng px logic (khung 640x360), có phần lẻ.
 */
public abstract class ChuUI {
    public static ChuUI bo;

    /** Cao hộp một dòng (px logic) cho cỡ co. */
    public abstract int cao(float co);

    /** Bề ngang chuỗi (px logic) - có bộ nhớ đệm. dam: 0 thường, 1 đậm vừa, 2 đậm. */
    public abstract int rong(String s, float co, int dam);

    /** Vẽ chuỗi: y = đỉnh hộp dòng; canh 0 trái / 1 phải / 2 giữa; mau RGB. */
    public abstract void ve(Graphics g, String s, int x, int y, float co, int dam, int mau, int canh);

    /**
     * Nét vector khử răng cưa (dấu X kieu 0, mũi tên trái kieu 1, dấu cộng kieu 2) tâm (cx, cy), nửa cạnh k.
     * false = để J2ME vẽ đường thẳng.
     */
    public boolean net(Graphics g, int cx, int cy, int k, int mau, int kieu) {
        return false;
    }

    /** Biểu tượng vector (BieuTuong): lệnh trong hộp 100x100 vẽ vào ô (x, y, s). false = để J2ME vẽ. */
    public boolean hinh(Graphics g, float[] lenh, int x, int y, int s, int mau, int nen) {
        return false;
    }

    /**
     * Icon chiêu PNG (AnhUI.oChieu ô o) cạnh s logic tại (x, y); tron = huy hiệu tròn (nút HUD); khoa = chưa học (xám mờ).
     * false = không có ảnh (J2ME / thiếu tấm) -> người gọi vẽ ô chiêu cũ.
     */
    public boolean chieu(Graphics g, int o, boolean tron, int x, int y, int s, boolean khoa) {
        return false;
    }

    /**
     * 0.9.7: khối nền (x, y, w, h) độ sáng sang (0..255) vừa vẽ bằng mảnh pixel (KhungNro) - bản PC / APK ghi lại để chữ
     * của game cũ tự chọn bản sáng / tối cho dễ đọc. J2ME: không làm gì.
     */
    public void nen(Graphics g, int x, int y, int w, int h, int sang) {
    }

    /** Đầu mỗi khung vẽ (Canvas.paint). */
    public void khungMoi() {
    }
}
