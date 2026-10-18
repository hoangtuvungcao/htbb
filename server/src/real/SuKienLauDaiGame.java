package real;

import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.SuKienLauDai;
import data.DatabaseLauDai;
import java.util.Map;

/**
 * Lich su kien 52 tuan (GD2 buoc 5.2) va hieu ung cua cac ma "moi:*" (5.1). Logic lich o
 * com.htbb.pokemon.SuKienLauDai (thuan, co test).
 *
 * Hen lich: moi ma chi hen LAN KE TIEP vao tob_su_kien.mo_luc/dong_luc va CHI khi ca hai cot
 * dang trong (khong de len lich admin da dat tay tren HTBB-Web /admin/sukien). Bat/tat that do
 * DatabaseSuKien.chayLichDenHan + LichSuKien lam (can khoi dong lai, co san chot an toan).
 */
public final class SuKienLauDaiGame {

    private SuKienLauDaiGame() {
    }

    /** Job dem (LauDaiJob) va luc khoi dong: hen lan ke tiep cua moi ma trong lich. */
    public static int henLichNam() {
        if (!LauDai.bat()) {
            return 0;
        }
        int n = 0;
        for (final Map.Entry<String, long[]> e : SuKienLauDai.lanKeTiep(com.htbb.pokemon.DongHo.bay()).entrySet()) {
            if (SuKien.timMuc(e.getKey()) == null) {
                continue;
            }
            if (DatabaseLauDai.henSuKien(e.getKey(), e.getValue()[0], e.getValue()[1])) {
                n++;
            }
        }
        if (n > 0) {
            System.out.println("[LAU DAI] da hen lich " + n + " su kien (lich 52 tuan).");
        }
        return n;
    }

    /** He so nhan shiny hien tai theo su kien dang bat. */
    public static int nhanShiny() {
        return SuKienLauDai.nhanShiny(SuKien.bat("moi:shiny"), SuKien.bat("moi:congdong"));
    }

    /** He dang duoc +10% cua "tuan he" (-1 neu khong co). */
    public static int heTuan() {
        if (!SuKien.bat("moi:tuanhe")) {
            return -1;
        }
        return SuKienLauDai.heTuan(SuKienLauDai.tuanIso(com.htbb.pokemon.DongHo.bay()));
    }
}
