package game.pokemon;

import game.model.GemTemp;
import game.render.Res;
import java.io.DataInputStream;

/**
 * LOẠI TIỀN TỪNG DÒNG CỬA HÀNG ĐẶC BIỆT (Res.shopTemplate, gói 25). Gói 25 chỉ có typeMoney 0 = lượng / 1 = xu, nhưng
 * máy chủ trừ LƯỢNG KHOÁ cho một số dòng (vé giờ vàng, thuốc, kẹo, đồ khoá - server real/TienShopDacBiet). Máy chủ gửi
 * client mới (BAN >= TienShopDacBiet.BAN_CLIENT, một hằng phía máy chủ) danh sách id nối sau bảng shop: u8 n, u8 id...;
 * gói phát lại cho cả máy chủ / máy chủ cũ thì mô tả dòng đó mở đầu "[Lượng khoá] " - nhận ra cả hai, bỏ nhãn khi hiện.
 */
public final class TienShop {
    /** nhãn máy chủ thêm đầu mô tả cho client cũ (server TienShopDacBiet.NHAN) */
    public static final String NHAN = "[Lượng khoá] ";

    private TienShop() {
    }

    /** Loại tiền (TienTe) của một dòng cửa hàng đặc biệt. */
    public static int loai(GemTemp t) {
        if (t == null) {
            return TienTe.LUONG;
        }
        if (t.luongKhoa) {
            return TienTe.LUONG_KHOA;
        }
        return t.typeMoney == 0 ? TienTe.LUONG : TienTe.XU;
    }

    /** Mô tả bắt đầu bằng nhãn -> đánh dấu lượng khoá, bỏ nhãn (mô tả chỉ còn nhãn + câu mặc định -> rỗng). */
    public static void nhanMoTa(GemTemp t) {
        if (t == null || t.decript == null || !t.decript.startsWith(NHAN)) {
            return;
        }
        t.luongKhoa = true;
        String d = t.decript.substring(NHAN.length());
        t.decript = "Giá tính bằng lượng khoá.".equals(d) ? "" : d;
    }

    /** Phần nối thêm sau bảng shop gói 25 (không có = máy chủ không gửi; giữ dấu theo nhãn). */
    public static void docNoiThem(DataInputStream r) {
        try {
            if (r == null || r.available() < 1) {
                return;
            }
            int n = r.readUnsignedByte();
            for (int i = 0; i < n && r.available() > 0; i++) {
                GemTemp t = Res.getShopByID((short) r.readUnsignedByte());
                if (t != null) {
                    t.luongKhoa = true;
                }
            }
        } catch (Throwable e) {
            // bỏ qua: gói cũ
        }
    }

    /** Số tiền loại l nhân vật chính đang có. */
    public static long dangCo(game.model.MainChar mc, int l) {
        if (mc == null) {
            return 0;
        }
        return l == TienTe.LUONG_KHOA ? Math.max(0, mc.luongKhoa) : l == TienTe.LUONG ? mc.luong : mc.charXu;
    }
}
