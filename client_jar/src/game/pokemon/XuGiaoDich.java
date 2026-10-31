package game.pokemon;

import game.model.ChatInfo;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.TField;

/**
 * Ô XU TRONG GIAO DỊCH 1-1 (0.9.27, kiểu NRO): mỗi bên gõ số xu muốn đưa, hai bên thấy số của nhau. Gói 66/2 kiểu 3:
 * gửi {@code long} = số xu TUYỆT ĐỐI mình đưa (0 = rút), máy chủ báo lại {@code short id + long xu} cho cả hai. Xu chỉ trừ
 * lúc hai bên xác nhận (máy chủ real/GiaoDichXu). Lượng / lượng khoá KHÔNG giao dịch được ở đây (chỉ NPC "Chuyển lượng").
 * Dùng chung cho ManGiaoDich (cảm ứng) và cửa sổ cũ WindowInfoScr tab 8 (máy phím: mục "Đưa xu").
 */
public final class XuGiaoDich {
    private XuGiaoDich() {
    }

    /** Xu mình đang đưa / đối phương đang đưa cho mình (theo gói máy chủ, không tự đoán). */
    public static long dua, nhan;

    public static void datLai() {
        dua = 0;
        nhan = 0;
    }

    /** GameScr.onItemTrade kiểu 3. */
    public static void nhanGoi(boolean laMinh, long xu, String tenDoi) {
        if (xu < 0) {
            xu = 0;
        }
        if (laMinh) {
            dua = xu;
        } else {
            long cu = nhan;
            nhan = xu;
            if (xu != cu) {
                // dòng chat: ai cũng thấy kể cả khi đang ở cửa sổ cũ / màn khác
                String ai = tenDoi != null && tenDoi.length() > 0 ? tenDoi : "Đối phương";
                String s = xu > 0 ? ai + " đưa " + TienTe.chu(TienTe.XU, xu) + " vào giao dịch" : ai + " đã rút xu khỏi giao dịch";
                try {
                    Canvas.gameScr.addChat(new ChatInfo("", s, 0));
                } catch (Throwable e) {
                    // bỏ qua
                }
            }
        }
    }

    /** Hộp nhập số xu đưa (số tuyệt đối; 0 = rút). */
    public static void hoi() {
        long co = 0;
        try {
            co = Canvas.gameScr.mainChar.charXu;
        } catch (Throwable e) {
            // bỏ qua
        }
        final long toiDa = co;
        Canvas.inputDlg.setInfo("Số xu đưa (có " + TienTe.so(co) + ", 0 = rút)", new IAction() {
            public void perform() {
                try {
                    long n = Long.parseLong(Canvas.inputDlg.tfInput.getText().trim());
                    if (n >= 0 && n <= toiDa) {
                        GameService.gI().sendXuTrade(n);
                        Canvas.endDlg();
                    }
                } catch (Throwable e) {
                    // không phải số
                }
            }
        }, TField.INPUT_TYPE_NUMERIC, 10, true);
        Canvas.inputDlg.soToiDa = (int) Math.min(Integer.MAX_VALUE, Math.max(0, toiDa));
        Canvas.inputDlg.show();
    }
}
