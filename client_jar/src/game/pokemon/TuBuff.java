package game.pokemon;

/**
 * 0.9.20 (chủ game: "kỹ năng phụ trợ gắn vào các ô auto cho nó hoạt động"): Tự động đánh tự dùng chiêu bổ trợ đặt ở ô
 * Tự động (TuDong: cả 5 ô của trang chiêu tự động dùng; Auto gốc: phím 1 / 3 / 5) lên CHÍNH MÌNH khi hiệu ứng của
 * chiêu đó không còn. Logic thuần (không lcdui, CLDC 1.1) - GameScr.tuDungBoTro gọi cho từng ô; kiểm thử:
 * client_pc/test/KiemTuBuff.
 *
 * Luật (khớp máy chủ ChieuHoTro.kiem - máy chủ vẫn là bên quyết định PP / hồi chiêu / hiệu lực):
 * <ul>
 * <li>không bao giờ: hồi sinh (dòng 2 chiêu 6 - chỉ dùng cho người đã gục) và chiêu bổ trợ không dùng cho bản thân
 * được (SKILL_CAN_BUFF_TO_USER khác 0: -1 máy chủ từ chối "không thể dùng", 1 = chỉ cho người khác);</li>
 * <li>chưa học -> không;</li>
 * <li>hiệu ứng của chiêu còn trên người -> không (máy chủ từ chối "Hiệu ứng vẫn còn", không cộng dồn / làm mới, nên
 * dùng sớm lúc "sắp hết" chỉ phí gói và hiện thông báo - đợi hết hẳn; máy chủ gửi xoá biểu tượng đúng lúc hết);</li>
 * <li>đang hồi chiêu -> không (hồi chiêu bổ trợ của máy chủ luôn dài hơn thời lượng + biên, gói 35);</li>
 * <li>thiếu PP -> không.</li>
 * </ul>
 * Mỗi chiêu bổ trợ theo dõi bằng hiệu ứng RIÊNG của nó (Char.buff, mỗi hiệu ứng một hạn) - nhiều chiêu bổ trợ ở nhiều
 * ô Tự động không chặn nhau.
 */
public final class TuBuff {
    private TuBuff() {
    }

    public static final int DUNG = 0;
    public static final int KHONG_DUNG_DUOC = 1;
    public static final int CHUA_HOC = 2;
    public static final int CON_HIEU_LUC = 3;
    public static final int DANG_HOI = 4;
    public static final int THIEU_PP = 5;

    /** Phím các ô của vòng Auto gốc (GameScr.autoFight / BangAuto). */
    public static final int[] PHIM = {1, 3, 5};
    /** Cả 5 ô của một trang chiêu (Tự động đánh chính thức TuDong đánh bằng cả trang chiêu đã chọn). */
    public static final int[] PHIM_TRANG = {1, 3, 5, 7, 9};

    /** Gợi ý ở bảng Tự động đánh. */
    public static final String GOI_Y = "Chiêu bổ trợ ở trang chiêu tự dùng khi hết hiệu lực";
    /** Gợi ý ngắn (hàng tiêu đề "Thứ tự chiêu" của bảng mới). */
    public static final String GOI_Y_NGAN = "Bổ trợ: tự dùng khi hết hiệu lực";

    /** Chiêu hồi sinh (dòng Eevee, ô 6). */
    public static boolean laHoiSinh(int lop, int chieu) {
        return lop == 2 && chieu == 6;
    }

    /**
     * @param choMinh   SkillManager.SKILL_CAN_BUFF_TO_USER[lop][chieu - 4] (0 = dùng cho bản thân được)
     * @param capHoc    Char.skillLevelLearnt[chieu]
     * @param conMs     thời gian hiệu ứng của chiêu còn trên người (ms), 0 = không có
     * @param daQuaMs   từ lần dùng chiêu này gần nhất (ms)
     * @param hoiMs     hồi chiêu (ms)
     */
    public static int xet(int lop, int chieu, int choMinh, int capHoc, long conMs, long daQuaMs, long hoiMs, int pp, int ppCan) {
        if (laHoiSinh(lop, chieu) || choMinh != 0) {
            return KHONG_DUNG_DUOC;
        }
        if (capHoc <= 0) {
            return CHUA_HOC;
        }
        if (conMs > 0) {
            return CON_HIEU_LUC;
        }
        if (daQuaMs <= hoiMs) {
            return DANG_HOI;
        }
        if (ppCan > 0 && pp < ppCan) {
            return THIEU_PP;
        }
        return DUNG;
    }
}
