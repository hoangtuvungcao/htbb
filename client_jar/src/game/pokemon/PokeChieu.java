package game.pokemon;

/**
 * Chiêu Pokémon của 5 dòng người chơi (index = class cũ, so hiệu skill như server) - khớp
 * HTBB du_lieu/bang_chieu.csv (ten_moi, so_he). Dùng cho thanh skill nhanh, bảng kỹ năng:
 * tên chiêu Pokémon (SkillManager.SKILL_NAME của client vẫn là tên kiếm hiệp cũ), hệ (màu ô) và
 * hình biểu tượng (KieuO.HINH_*). Lớp chỉ chứa dữ liệu, không dùng lcdui.
 */
public final class PokeChieu {
    /** Cấp tối đa của một chiêu (client gốc: skillLevelLearnt == 9 là MAX). */
    public static final int CAP_TOI_DA = 9;

    public static final String[][] TEN = {
        {"Cào", "Than hồng", "Nanh lửa", "Xoáy lửa", "Màn khói", "Vũ điệu kiếm", "Súng phun lửa", "Vuốt rồng", "Đại hỏa"},
        {"Húc", "Roi dây leo", "Lá dao", "Bom bùn", "Tự vệ", "Sinh trưởng", "Bom hạt", "Bão lá", "Tia mặt trời"},
        {"Húc", "Tấn công nhanh", "Cắn", "Sao băng", "Mong ước", "Nghỉ ngơi", "Phước lành hồi sinh", "Chia sẻ nỗi đau", "Đuôi sắt", "Bứt phá cuối cùng", "Tia phá hủy"},
        {"Húc", "Súng nước", "Bong bóng", "Xoáy nước", "Phá mai", "Rút vào mai", "Đuôi nước", "Bơm nước", "Đại pháo nước"},
        {"Tia lửa điện", "Sóng điện", "Cầu điện", "Tia chớp", "Nhanh nhẹn", "Nạp điện", "Lưới điện", "Sấm sét", "Đâm sầm điện"}
    };

    /** Số hệ (0 Thường, 1 Lửa, 2 Nước, 3 Điện, 4 Cỏ, 7 Độc, 10 Siêu linh, 13 Ma, 14 Rồng, 15 Bóng tối, 16 Thép). */
    public static final byte[][] HE = {
        {0, 1, 1, 1, 0, 0, 1, 14, 1},
        {0, 4, 4, 7, 0, 0, 4, 4, 4},
        {0, 0, 15, 0, 0, 10, 0, 13, 16, 0, 0},
        {0, 2, 2, 2, 0, 2, 2, 2, 2},
        {3, 3, 3, 3, 10, 3, 3, 3, 3}
    };

    /** Hình biểu tượng chiêu (KieuO.HINH_*). */
    public static final byte[][] HINH = {
        {KieuO.HINH_VUOT, KieuO.HINH_LUA, KieuO.HINH_NANH, KieuO.HINH_LOC, KieuO.HINH_MAY, KieuO.HINH_TANG, KieuO.HINH_TIA, KieuO.HINH_VUOT, KieuO.HINH_NO},
        {KieuO.HINH_VA, KieuO.HINH_ROI, KieuO.HINH_LA, KieuO.HINH_BOM, KieuO.HINH_KHIEN, KieuO.HINH_TANG, KieuO.HINH_BOM, KieuO.HINH_LOC, KieuO.HINH_MAT_TROI},
        {KieuO.HINH_VA, KieuO.HINH_NHANH, KieuO.HINH_NANH, KieuO.HINH_SAO, KieuO.HINH_TIM, KieuO.HINH_NGU, KieuO.HINH_CONG, KieuO.HINH_MA, KieuO.HINH_THEP, KieuO.HINH_NO, KieuO.HINH_TIA},
        {KieuO.HINH_VA, KieuO.HINH_GIOT, KieuO.HINH_BONG, KieuO.HINH_LOC, KieuO.HINH_TANG, KieuO.HINH_KHIEN, KieuO.HINH_SONG, KieuO.HINH_TIA, KieuO.HINH_NO},
        {KieuO.HINH_SET, KieuO.HINH_SONG, KieuO.HINH_BOM, KieuO.HINH_TIA, KieuO.HINH_NHANH, KieuO.HINH_VONG, KieuO.HINH_LUOI, KieuO.HINH_MAY, KieuO.HINH_VA}
    };

    /** Câu mô tả ngắn từng chiêu (hiện trong bảng kỹ năng). */
    public static final String[][] MO_TA = {
        {"Cào bằng móng vuốt sắc.", "Phun tàn lửa nhỏ vào đối thủ.", "Cắn bằng nanh rực lửa.", "Cuốn đối thủ trong xoáy lửa.", "Bị động: màn khói che mắt, có cơ hội đánh xuyên giáp.", "Xoay lưỡi kiếm quanh mình, phản một phần sát thương.", "Phun luồng lửa dữ dội.", "Cào bằng vuốt rồng sắc bén.", "Ngọn lửa hình chữ đại thiêu rụi mọi thứ."},
        {"Lao vào húc đối thủ.", "Quất đối thủ bằng dây leo.", "Phóng lá sắc như dao.", "Ném bùn độc vào đối thủ.", "Thủ thế tăng phòng thủ.", "Bị động: hấp thụ ánh nắng, luôn tăng tấn công.", "Bắn mưa hạt giống nổ tung.", "Cuốn đối thủ trong bão lá.", "Gom ánh nắng rồi bắn tia mặt trời."},
        {"Lao vào húc đối thủ.", "Lao tới cực nhanh, ra đòn trước.", "Cắn bằng răng sắc.", "Bắn sao không bao giờ trượt.", "Ước nguyện hồi HP và PP.", "Bị động: nghỉ ngơi, luôn tăng PP tối đa.", "Hồi sinh đồng đội đã gục.", "Chia sẻ thiệt hại sang PP.", "Quất đuôi cứng như thép.", "Tung đòn quyết định cuối cùng.", "Bắn tia năng lượng hủy diệt."},
        {"Lao vào húc đối thủ.", "Phun tia nước mạnh.", "Thổi bong bóng vào đối thủ.", "Nhốt đối thủ trong xoáy nước.", "Bổ trợ: phá mai, đòn đánh có cơ hội làm choáng.", "Bị động: rút vào mai, luôn tăng phòng thủ.", "Quất đuôi tạo sóng nước.", "Bơm nước áp lực cực mạnh.", "Pháo nước công suất tối đa."},
        {"Phóng tia lửa điện nhỏ.", "Bắn sóng điện không trượt.", "Ném cầu điện vào đối thủ.", "Phóng tia chớp cực mạnh.", "Đòn đánh kèm hiệu ứng độc.", "Bị động: nạp điện, độc của Nhanh nhẹn mạnh hơn.", "Giăng lưới điện bắt đối thủ.", "Gọi sấm sét đánh xuống.", "Phủ điện toàn thân lao vào đối thủ."}
    };

    /** Kiểu chiêu (bảng kỹ năng, Tự động đánh). */
    public static final int CHU_DONG = 0, BO_TRO = 1, BI_DONG = 2;

    /**
     * Ô chiêu bị động theo dòng (SKILL_CAN_BUFF_TO_USER == -1, máy chủ ChieuBiDong.O): đã học là luôn có hiệu lực,
     * không bấm / không gán ô nhanh / không cài Tự động đánh được. Báo lỗi 05/11/2026: "skill con rùa chủ động hay bị
     * động mà dùng không được" (Rút vào mai) - bảng kỹ năng trước không ghi.
     */
    public static final int[] O_BI_DONG = {4, 5, 5, 5, 5};

    /** Hiệu ứng chiêu bị động, "#" = phần trăm theo cấp chiêu (khớp máy chủ ChieuBiDong.HIEU_UNG). */
    public static final String[] HIEU_UNG_BI_DONG = {"#% cơ hội đánh xuyên giáp", "+#% tấn công", "+#% PP tối đa", "+#% phòng thủ", "độc Nhanh nhẹn +#%"};

    /** Kiểu của chiêu: BI_DONG / BO_TRO (laBuff) / CHU_DONG. */
    public static int kieu(int lop, int chieu, boolean laBuff) {
        if (lop >= 0 && lop < O_BI_DONG.length && O_BI_DONG[lop] == chieu) {
            return BI_DONG;
        }
        return laBuff ? BO_TRO : CHU_DONG;
    }

    /** Nhãn kiểu chiêu. */
    public static String nhanKieu(int k) {
        return k == BI_DONG ? "Bị động" : (k == BO_TRO ? "Bổ trợ" : "Chủ động");
    }

    /** Dòng giải thích kiểu chiêu (dưới cấp). */
    public static String giaiThichKieu(int k) {
        if (k == BI_DONG) {
            return "Luôn có hiệu lực, không cần bấm";
        }
        if (k == BO_TRO) {
            return "Bấm để dùng; Tự động đánh tự dùng nếu ở trang chiêu";
        }
        return "Bấm để đánh; cài được vào Tự động đánh";
    }

    /** "+65% phòng thủ"; "" nếu không phải dòng có chiêu bị động. */
    public static String hieuUngBiDong(int lop, int phanTram) {
        if (lop < 0 || lop >= HIEU_UNG_BI_DONG.length) {
            return "";
        }
        String s = HIEU_UNG_BI_DONG[lop];
        int i = s.indexOf('#');
        return i < 0 ? s : new StringBuffer(s.substring(0, i)).append(phanTram).append(s.substring(i + 1)).toString();
    }

    /** Câu báo khi chạm chiêu bị động ở Tự động đánh / ô nhanh. */
    public static String cauBiDong(int lop, int chieu) {
        String t = ten(lop, chieu);
        return new StringBuffer(t == null ? "Chiêu này" : t).append(" là chiêu bị động: đã học là luôn có hiệu lực, không cần bấm và không cài vào Tự động đánh.").toString();
    }

    private PokeChieu() {
    }

    private static boolean co(int lop, int chieu) {
        return lop >= 0 && lop < TEN.length && chieu >= 0 && chieu < TEN[lop].length;
    }

    /** Tên chiêu Pokémon; null nếu ngoài bảng (người gọi dùng tên cũ). */
    public static String ten(int lop, int chieu) {
        return co(lop, chieu) ? TEN[lop][chieu] : null;
    }

    public static int he(int lop, int chieu) {
        return co(lop, chieu) ? HE[lop][chieu] : 0;
    }

    public static int hinh(int lop, int chieu) {
        return co(lop, chieu) ? HINH[lop][chieu] : KieuO.HINH_VA;
    }

    public static String moTa(int lop, int chieu) {
        return co(lop, chieu) ? MO_TA[lop][chieu] : "";
    }
}
