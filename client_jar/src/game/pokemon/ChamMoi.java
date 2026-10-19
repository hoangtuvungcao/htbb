package game.pokemon;

import game.model.Quest;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.util.Vector;

/**
 * DỊCH VỤ CHẤM ĐỎ (0.9.6 "dấu dẫn đường"): MỘT nơi quyết định mọi chấm báo mới, từ dữ liệu máy chủ đã nhận (nhiệm vụ,
 * thư, điểm kỹ năng / tiềm năng, chiêu mới). Mỗi chấm có NGUỒN cụ thể và một ĐƯỜNG từ HUD tới đúng thứ cần làm:
 *
 * <pre>
 *  NV_NHAN  nhiệm vụ mới nhận được   HUD Nhiệm vụ -> tab "Có thể nhận" -> hàng nhiệm vụ
 *  NV_TRA   nhiệm vụ đủ, về trả       HUD Nhiệm vụ -> tab "Đang làm"    -> hàng nhiệm vụ
 *  THU      thư chưa đọc              HUD Menu -> "Thư/Quà" -> hàng "Hộp thư (n mới)"
 *  DIEM_TN  điểm tiềm năng / Điểm huấn luyện chưa dùng  HUD Menu -> "Pokémon" -> tab Trang bị / Chỉ số -> nút "Huấn luyện"
 *  DIEM_KN  điểm kỹ năng chưa dùng    HUD Menu -> "Pokémon" -> tab Kỹ năng -> chip "Chiêu của Pokémon" -> hàng "Học: 1 điểm"
 *  CHIEU_MOI chiêu mới học được ở NPC  HUD Menu -> "Pokémon" -> tab Kỹ năng -> chip "Học chiêu mới" -> hàng chiêu (chưa xem)
 * </pre>
 *
 * Xoá: làm xong việc (đọc thư, cộng điểm, nhận / trả nhiệm vụ) làm nguồn về 0 - chấm mọi cấp tắt theo; chiêu mới tắt
 * khi đã MỞ danh sách "Học chiêu mới" ({@link #daXemChieuMoi}). Cấp cha chỉ sáng khi còn ít nhất một cấp con.
 * Kiểm thử: client_pc/test/KiemChamMoi (mỗi nguồn: chấm có trên đủ mọi cấp của đường).
 */
public final class ChamMoi {
    public static final int NV_NHAN = 0, NV_TRA = 1, THU = 2, DIEM_TN = 3, DIEM_KN = 4, CHIEU_MOI = 5, SO_NGUON = 6;
    public static final String[] TEN = {"nhiệm vụ mới", "nhiệm vụ đủ để trả", "thư chưa đọc", "điểm tiềm năng", "điểm kỹ năng",
        "chiêu mới"};

    /** số chiêu mới đã xem (danh sách "Học chiêu mới" đã mở) - theo dòng Pokémon */
    private static int daXemChieu = -1, daXemLop = -1;

    private ChamMoi() {
    }

    static game.model.MainChar mc() {
        return Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
    }

    /** Số mục của nguồn (0 = không chấm). */
    public static int dem(int nguon) {
        try {
            game.model.MainChar m = mc();
            switch (nguon) {
                case NV_NHAN: {
                    int n = 0;
                    Vector v = GameScr.allQuestCanReceive;
                    for (int i = 0; v != null && i < v.size(); i++) {
                        if (((Quest) v.elementAt(i)).stateQuest == 0) {
                            n++;
                        }
                    }
                    return n;
                }
                case NV_TRA: {
                    int n = 0;
                    Vector d = ManNhiemVu.dangLam();
                    for (int i = 0; i < d.size(); i++) {
                        if (ManNhiemVu.xong((Quest) d.elementAt(i))) {
                            n++;
                        }
                    }
                    return n;
                }
                case THU:
                    return Math.max(0, game.render.screen.MessageScr.nMSG);
                case DIEM_TN:
                    // 0.9.8: Điểm huấn luyện chưa phân bổ (máy chủ bật), không thì điểm tiềm năng cũ
                    return m == null ? 0 : HocChieu.batHL ? HocChieu.conLai() : Math.max(0, m.basePointLeft);
                case DIEM_KN:
                    return m == null ? 0 : Math.max(0, m.skillPointLeft);
                case CHIEU_MOI: {
                    if (m == null) {
                        return 0;
                    }
                    int n = ManNhanVat.demHocMoi(m.clazz);
                    return daXemLop == m.clazz && n <= daXemChieu ? 0 : n;
                }
                default:
                    return 0;
            }
        } catch (Throwable e) {
            return 0;
        }
    }

    public static boolean co(int nguon) {
        return dem(nguon) > 0;
    }

    /** Đã mở danh sách "Học chiêu mới": chấm chiêu mới tắt tới khi có thêm chiêu. */
    public static void daXemChieuMoi() {
        game.model.MainChar m = mc();
        if (m != null) {
            daXemLop = m.clazz;
            daXemChieu = ManNhanVat.demHocMoi(m.clazz);
        }
    }

    // ------------------------------------------------------------------ các cấp của đường

    /** Nút Nhiệm vụ trên HUD. */
    public static boolean hudNhiemVu() {
        return co(NV_NHAN) || co(NV_TRA);
    }

    /** Tab bảng Nhiệm vụ: 0 Đang làm (về trả), 1 Có thể nhận. */
    public static boolean tabNhiemVu(int tab) {
        return tab == 0 ? co(NV_TRA) : co(NV_NHAN);
    }

    /** Hàng nhiệm vụ q của tab. */
    public static boolean hangNhiemVu(int tab, Quest q) {
        return q != null && (tab == 0 ? ManNhiemVu.xong(q) : q.stateQuest == 0);
    }

    /** Ô "Pokémon" ở Menu. */
    public static boolean menuPokemon() {
        return co(DIEM_TN) || co(DIEM_KN) || co(CHIEU_MOI);
    }

    /** Viên "Thư/Quà" ở Menu. */
    public static boolean menuThu() {
        return co(THU);
    }

    /** Nút Menu (Poké Ball) của HUD. */
    public static boolean hudMenu() {
        return menuPokemon() || menuThu();
    }

    /** Tab bảng Pokémon (ManNhanVat.TAB_*). */
    public static boolean tabNhanVat(int tab) {
        switch (tab) {
            case ManNhanVat.TAB_TRANG_BI:
            case ManNhanVat.TAB_CHI_SO:
                return co(DIEM_TN);
            case ManNhanVat.TAB_KY_NANG:
                return co(DIEM_KN) || co(CHIEU_MOI);
            default:
                return false;
        }
    }

    /** Chip tab Kỹ năng: 0 "Chiêu của Pokémon" (điểm kỹ năng), 1 "Học chiêu mới". */
    public static boolean chipKyNang(int chip) {
        return chip == 0 ? co(DIEM_KN) : co(CHIEU_MOI);
    }

    /** Mảng chấm theo tab (cho Bang.chamTab). */
    public static boolean[] tabsNhanVat(int n) {
        boolean[] r = new boolean[n];
        for (int i = 0; i < n; i++) {
            r[i] = tabNhanVat(i);
        }
        return r;
    }
}
