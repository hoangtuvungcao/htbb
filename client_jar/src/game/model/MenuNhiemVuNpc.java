package game.model;

/**
 * Menu nhiệm vụ khi nói chuyện với NPC (GameScr.getAllQuestNpc) - thuần Java, không phụ thuộc lớp game (bộ kiểm máy chủ
 * KiemMenuNhiemVuNpc biên dịch riêng lớp này và chạy thật).
 *
 * Báo lỗi người chơi 05/11/2026 (Trưởng làng Xatu: lưới 7 tên nhiệm vụ + "Thêm", "không biết cái nào là nhận nhiệm vụ,
 * trả nhiệm vụ, nhét nhiều quá"). Nay mỗi dòng ghi rõ trạng thái và xếp:
 *   1. "* Trả: tên"            - xong, chờ trả ở NPC này
 *   2. "! Nhận: tên"           - bước cốt truyện chính nhận được
 *   3. "! Nhận: tên"           - nhiệm vụ phụ; nhiều hơn PHU_TOI_DA thì gom một dòng "Nhiệm vụ phụ (N)" (mở danh sách con)
 *   4. "… Đang làm: tên (x/y)" - đang làm, báo lại ở NPC này; từ 2 dòng trở lên gom "… Đang làm (N)"
 * Ký hiệu chỉ dùng ký tự font ảnh J2ME có (! * : ( ) /) - '…' do Font.taoKyTu vẽ (bản cũ thay '.').
 */
public final class MenuNhiemVuNpc {
    public static final int TRA = 0, CHINH = 1, PHU = 2, DANG = 3;
    /** Mã dòng gom trong kết quả xep: nhóm nhiệm vụ phụ / nhóm đang làm. */
    public static final int NHOM_PHU = -1, NHOM_DANG = -2;
    /** Nhiệm vụ phụ hiện thẳng ở trang đầu tối đa chừng này dòng; nhiều hơn thì gom. */
    public static final int PHU_TOI_DA = 3;
    /** Dòng đang làm hiện thẳng tối đa chừng này; nhiều hơn thì gom. */
    public static final int DANG_TOI_DA = 1;

    public static final String NHAN = "! Nhận: ";
    public static final String TRA_TIEN_TO = "* Trả: ";
    public static final String DANG_TIEN_TO = "… Đang làm: ";
    public static final String TEN_NHOM_PHU = "Nhiệm vụ phụ";
    public static final String TEN_NHOM_DANG = "… Đang làm";

    private MenuNhiemVuNpc() {
    }

    /** Bỏ tiền tố máy chủ đã gắn cho client cũ ("! Nhận: ") - không bao giờ lặp hai lần. */
    public static String boTienTo(String ten) {
        if (ten == null) {
            return "";
        }
        if (ten.startsWith(NHAN)) {
            return ten.substring(NHAN.length());
        }
        return ten;
    }

    /** Chữ của một dòng. {@code da} / {@code can} chỉ dùng cho DANG (can &lt;= 0 = không đếm được). */
    public static String nhan(int loai, String ten, int da, int can) {
        String t = boTienTo(ten);
        switch (loai) {
            case TRA:
                return TRA_TIEN_TO + t;
            case DANG:
                return DANG_TIEN_TO + t + (can > 0 ? " (" + Math.min(da, can) + "/" + can + ")" : "");
            default:
                return NHAN + t;
        }
    }

    /** Chữ dòng gom ("Nhiệm vụ phụ (5)" / "… Đang làm (2)"). */
    public static String tenNhom(int nhom, int n) {
        return (nhom == NHOM_DANG ? TEN_NHOM_DANG : TEN_NHOM_PHU) + " (" + n + ")";
    }

    static int dem(int[] loai, int k) {
        int n = 0;
        for (int i = 0; i < loai.length; i++) {
            if (loai[i] == k) {
                n++;
            }
        }
        return n;
    }

    /**
     * Thứ tự trang đầu: chỉ số dòng gốc (theo đúng thứ tự gốc trong mỗi loại), hoặc NHOM_PHU / NHOM_DANG cho dòng gom.
     * Trả -> Chính -> Phụ (hoặc dòng gom) -> Đang làm (hoặc dòng gom).
     */
    public static int[] xep(int[] loai) {
        int nPhu = dem(loai, PHU), nDang = dem(loai, DANG);
        boolean gomPhu = nPhu > PHU_TOI_DA, gomDang = nDang > DANG_TOI_DA;
        int n = dem(loai, TRA) + dem(loai, CHINH) + (gomPhu ? 1 : nPhu) + (gomDang ? 1 : nDang);
        int[] r = new int[n];
        int k = 0;
        int[] thuTu = {TRA, CHINH, PHU, DANG};
        for (int t = 0; t < thuTu.length; t++) {
            int lo = thuTu[t];
            if (lo == PHU && gomPhu) {
                r[k++] = NHOM_PHU;
                continue;
            }
            if (lo == DANG && gomDang) {
                r[k++] = NHOM_DANG;
                continue;
            }
            for (int i = 0; i < loai.length; i++) {
                if (loai[i] == lo) {
                    r[k++] = i;
                }
            }
        }
        return r;
    }

    /** Các dòng trong một nhóm gom (theo thứ tự gốc). */
    public static int[] trongNhom(int[] loai, int nhom) {
        int lo = nhom == NHOM_DANG ? DANG : PHU;
        int[] r = new int[dem(loai, lo)];
        int k = 0;
        for (int i = 0; i < loai.length; i++) {
            if (loai[i] == lo) {
                r[k++] = i;
            }
        }
        return r;
    }
}
