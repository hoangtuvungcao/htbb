package game.pokemon;

import javax.microedition.lcdui.Graphics;

/**
 * Móc phóng hình của NỀN (APK: Graphics.datBienDoi, PC: Graphics.vaoTheGioi) cho giao diện cần vẽ to (Pokémon lớn
 * giữa màn Nhân vật, icon đồ trong ô chạm lớn). J2ME không có phép phóng: bo == null -> nơi gọi tự vẽ cỡ thường.
 *
 * Quy ước: bat(g, z) -> lệnh vẽ tiếp theo dùng toạ độ p, hiện ở toạ độ màn z * p (gốc màn, bỏ translate); tat(g)
 * trả về bình thường (translate về 0, vùng cắt cả màn).
 */
public abstract class PhongVe {
    /** cài đặt của nền (null = không phóng được) */
    public static PhongVe bo;

    public abstract void bat(Graphics g, float z);

    public abstract void tat(Graphics g);

    public static boolean co() {
        return bo != null;
    }
}
