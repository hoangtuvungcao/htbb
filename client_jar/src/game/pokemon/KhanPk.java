package game.pokemon;

import game.render.Canvas;
import game.model.IAction;

/**
 * KHĂN PK (bình 14..18, máy chủ real.CoPkGame - 0.9.8 + sua-pk): đeo = bật PK, tháo = tắt PK. Mọi nơi dùng khăn (Hồ sơ /
 * Hành trang, thẻ vật phẩm, cửa sổ túi J2ME) hỏi xác nhận rồi gửi lệnh dùng thuốc; máy chủ quyết (tên đỏ, vừa đánh người:
 * chờ 10 giây - có báo lý do) và trả -65 cập nhật trạng thái. Không còn chặn 3 phút phía client.
 */
public final class KhanPk {
    private KhanPk() {
    }

    public static boolean laKhan(int loai) {
        return loai >= 14 && loai <= 18;
    }

    static boolean dangDeo() {
        return Canvas.gameScr != null && Canvas.gameScr.mainChar != null && Canvas.gameScr.mainChar.pk > 0;
    }

    /** Nhãn nút: "Đeo khăn (bật PK)" / "Tháo khăn (tắt PK)". */
    public static String nhan() {
        return dangDeo() ? "Tháo khăn (tắt PK)" : "Đeo khăn (bật PK)";
    }

    /** Hỏi xác nhận rồi đeo (khăn loai) / tháo (khăn đang đeo). */
    public static void hoi(final int loai) {
        final boolean thao = dangDeo();
        String noiDung = thao ? "Tháo khăn PK: tắt PK, người khác không đánh được bạn nữa. Vừa đánh người chơi thì chờ 10 giây."
                : "Đeo khăn PK: bật PK - bạn đánh được và BỊ đánh bởi người chơi khác (ngoài vùng an toàn). Hạ người không bật PK sẽ bị tên đỏ.";
        HopXacNhan.hoi(thao ? "Tháo khăn (tắt PK)" : "Đeo khăn (bật PK)", noiDung, thao ? "Tháo khăn" : "Đeo khăn", new IAction() {
            public void perform() {
                try {
                    Canvas.gameScr.doUsePotion(thao ? Canvas.gameScr.mainChar.pk : loai);
                } catch (Throwable e) {
                    // chưa có dữ liệu thuốc
                }
            }
        }, null);
    }
}
