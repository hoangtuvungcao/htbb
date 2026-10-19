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

    // henLichNam (hen lan ke tiep theo lich 52 tuan ISO) DA BO 19/10/2026: lich su kien nay theo ngay that,
    // xem com.htbb.pokemon.LichNam + real.LichSuKien.dongBoLichNam.

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
