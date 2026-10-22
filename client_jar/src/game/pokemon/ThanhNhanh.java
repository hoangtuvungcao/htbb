package game.pokemon;

import game.model.Char;
import game.model.MainChar;
import game.model.QuickSlot;

/**
 * Tự điền thanh chiêu (0.9.5): nhân vật mới không còn 5 ô "+" trống khó hiểu. Nút đánh chính (phím 5) = chiêu cơ bản
 * (chiêu 0); 4 ô chiêu (phím 1 / 3 / 7 / 9) = các chiêu đã học theo thứ tự. Chỉ điền ô TRỐNG, chỉ khi số chiêu đã học
 * tăng (hoặc lần đầu nạp) - người chơi tự xoá / đổi ô thì giữ nguyên lựa chọn đó. Thuốc HP / PP có nút riêng trên HUD.
 */
public final class ThanhNhanh {
    private ThanhNhanh() {
    }

    /** ô (chỉ số trong bộ) của phím 1, 3, 7, 9 và phím 5 (GameScr.SLOTINDEXOFKEY) */
    static final int[] O_CHIEU = {0, 1, 3, 4};
    static final int O_DANH = 2;
    private static int daDem = -1;
    private static String daTen;

    /** Số chiêu đã học (cấp > 0), không kể chiêu cơ bản 0. */
    static int soDaHoc() {
        byte[] lv = Char.skillLevelLearnt;
        if (lv == null) {
            return 0;
        }
        int n = 0;
        for (int i = 1; i < lv.length; i++) {
            if (lv[i] > 0) {
                n++;
            }
        }
        return n;
    }

    /** Gọi mỗi khung HUD (rẻ): điền khi số chiêu đã học đổi / đổi nhân vật. */
    public static void kiem(MainChar mc) {
        if (MainChar.quickSlot == null || mc == null) {
            return;
        }
        if (mc.name != null && !mc.name.equals(daTen)) {
            HocChieu.xoa();   // đổi Pokémon: bảng chiêu theo loài / Điểm huấn luyện của Pokémon trước không còn đúng
        }
        HocChieu.xinNeuChua();
        int n = soDaHoc();
        if (n == daDem && mc.name != null && mc.name.equals(daTen)) {
            return;
        }
        daDem = n;
        daTen = mc.name;
        // 0.9.8: chiêu bị gỡ (loài chưa học được - máy chủ chuyển đổi học chiêu theo loài) -> chiêu gần nhất còn học
        for (int b = 0; b < MainChar.quickSlot.length; b++) {
            thayChieuMat(MainChar.quickSlot[b], Canvas_lop());
        }
        // 0.9.5: thuốc có nút HP / PP riêng -> bỏ thuốc khỏi ô CHIÊU (bản cũ đặt thuốc ở phím 7 / 9 = hai nút thuốc
        // thừa cạnh chiêu); cả hai bộ ô
        for (int b = 0; b < MainChar.quickSlot.length; b++) {
            QuickSlot[] bo = MainChar.quickSlot[b];
            for (int i = 0; bo != null && i < bo.length; i++) {
                if (bo[i] != null && bo[i].quickslotType == QuickSlot.TYPE_POTION) {
                    bo[i].setIsNothing();
                }
            }
        }
        dien(MainChar.quickSlot[0]);
    }

    /**
     * Ô giữ chiêu có cấp &lt;= 0 (đã bị gỡ) -> chiêu đã học gần nhất chưa có trên bộ ô: ưu tiên cùng hệ, rồi số hiệu gần
     * nhất; không còn chiêu nào thì để trống (ô "+"). Chiêu cơ bản 0 luôn giữ. Trả số ô đã đổi.
     */
    public static int thayChieuMat(QuickSlot[] bo, int lop) {
        byte[] lv = Char.skillLevelLearnt;
        if (bo == null || lv == null) {
            return 0;
        }
        int doi = 0;
        for (int i = 0; i < bo.length; i++) {
            QuickSlot q = bo[i];
            if (q == null || q.quickslotType != QuickSlot.TYPE_SKILL) {
                continue;
            }
            int sk = q.skillType;
            if (sk == 0 || (sk > 0 && sk < lv.length && lv[sk] > 0)) {
                continue;
            }
            int heCu = HocChieu.heChieu(lop, sk);
            int tot = -1, diemTot = Integer.MAX_VALUE;
            for (int c = 1; c < lv.length; c++) {
                if (lv[c] <= 0 || coChieu(bo, c)) {
                    continue;
                }
                int d = Math.abs(c - sk) + (HocChieu.heChieu(lop, c) == heCu ? 0 : 100);
                if (d < diemTot) {
                    diemTot = d;
                    tot = c;
                }
            }
            if (tot > 0) {
                q.setIsSkill(tot, lop, false);
            } else {
                q.setIsNothing();
            }
            doi++;
        }
        return doi;
    }

    /** Điền ô trống của một bộ ô. */
    public static void dien(QuickSlot[] bo) {
        if (bo == null || bo.length < 5) {
            return;
        }
        int lop = Canvas_lop();
        if (bo[O_DANH].quickslotType != QuickSlot.TYPE_SKILL && bo[O_DANH].quickslotType != QuickSlot.TYPE_POTION) {
            bo[O_DANH].setIsSkill(0, lop, false);
        }
        byte[] lv = Char.skillLevelLearnt;
        if (lv == null) {
            return;
        }
        int k = 0;
        for (int sk = 1; sk < lv.length && k < O_CHIEU.length; sk++) {
            if (lv[sk] <= 0 || coChieu(bo, sk)) {
                continue;
            }
            while (k < O_CHIEU.length && bo[O_CHIEU[k]].quickslotType >= 0) {
                k++;
            }
            if (k < O_CHIEU.length) {
                bo[O_CHIEU[k]].setIsSkill(sk, lop, false);
                k++;
            }
        }
    }

    /** Có cần bộ ô thứ 2 (học hơn 4 chiêu, hoặc bộ 2 đã có gì): nút đổi bộ chỉ hiện khi đó. */
    public static boolean coBo2() {
        // 0.9.8: luôn có ≥ 2 trang (gán riêng từng trang ở Hồ sơ > Kỹ năng) - viên đổi trang luôn hiện (trước: chỉ khi
        // học > 4 chiêu -> mất viên 1/2 ở đầu / giữa game vì learnset theo loài)
        if (MainChar.quickSlot != null && TrangChieu.so >= 2) {
            return true;
        }
        if (soDaHoc() > 4) {
            return true;
        }
        if (MainChar.quickSlot == null || MainChar.quickSlot.length < 2) {
            return false;
        }
        if (game.render.screen.GameScr.indexTabSlot != 0) {
            return true;
        }
        QuickSlot[] b = MainChar.quickSlot[1];
        for (int i = 0; b != null && i < b.length; i++) {
            if (b[i] != null && b[i].quickslotType >= 0) {
                return true;
            }
        }
        return false;
    }

    /** Có chiêu đã học chưa nằm trên bộ ô (ô trống mới đáng hiện "+"). */
    public static boolean coGiDeGan(QuickSlot[] bo) {
        byte[] lv = Char.skillLevelLearnt;
        if (bo == null || lv == null) {
            return false;
        }
        for (int sk = 0; sk < lv.length; sk++) {
            if ((sk == 0 || lv[sk] > 0) && !coChieu(bo, sk)) {
                return true;
            }
        }
        return false;
    }

    /** Ô o của bộ đang dùng có nên hiện (có việc, hoặc trống nhưng còn chiêu để gán). */
    public static boolean hienO(QuickSlot[] bo, int o) {
        if (bo == null || o < 0 || o >= bo.length) {
            return true;
        }
        return bo[o].quickslotType >= 0 || coGiDeGan(bo);
    }

    static boolean coChieu(QuickSlot[] bo, int sk) {
        for (int i = 0; i < bo.length; i++) {
            if (bo[i].quickslotType == QuickSlot.TYPE_SKILL && bo[i].skillType == sk) {
                return true;
            }
        }
        return false;
    }

    static int Canvas_lop() {
        try {
            return game.render.Canvas.gameScr.mainChar.clazz;
        } catch (Throwable e) {
            return 0;
        }
    }
}
