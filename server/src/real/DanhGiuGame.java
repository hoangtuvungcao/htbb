package real;

import com.htbb.pokemon.BatPokemon;
import com.htbb.pokemon.LauDai;

/**
 * "Danh giu" (False Swipe) cho Pokemon hoang da (chu game 05/11/2026, bao loi rongngoo cap 53: "no cap 16 doi danh
 * xuong 30% sao danh troi"). Don danh cua nguoi choi vao Pokemon hoang da ({@link LoaiQuaiGame#laHoangDa}) khi no con
 * TREN nguong bat (bat.nguong_hp_phan_tram) ma se ha no thi no con lai bat.danh_giu.hp_phan_tram % HP (trong o nem
 * bong); don ke tiep danh chet binh thuong. Logic thuan o {@link BatPokemon#satThuongGiu} / {@link BatPokemon#hpSauDon}.
 *
 * Cach goi o moi duong danh quai (MapDanhNhau / Region: don le + nhieu muc tieu, CharTrangBi dong hieu ung, phan don):
 * <pre>
 *   final int hpDau = mt.hp;                                  // dau don
 *   damage = DanhGiuGame.gioiHan(p, mt, damage, hpDau);       // TRUOC getXpReceive (EXP theo sat thuong that, khong
 *   ... than thu: dam = DanhGiuGame.gioiHan(p, mt, dam, hpDau) //   cong 2 lan phan "ha" - getXpReceive >= 100% tra ca xp)
 *   DanhGiuGame.chot(p, mt, hpDau);                           // sau moi phan cua don, truoc nhanh "con song / chet"
 * </pre>
 * EXP / roi do / da chi doi o cho: phan EXP theo sat thuong chia lai giua hai don, roi do khi ha (don sau) nhu cu.
 */
public final class DanhGiuGame {

    public static final String K_BAT = "bat.danh_giu.bat";
    public static final String K_HP = "bat.danh_giu.hp_phan_tram";

    private DanhGiuGame() {
    }

    static int giuPhanTram() {
        return LauDai.so(K_HP, BatPokemon.DANH_GIU_HP_PHAN_TRAM);
    }

    /** Ap dung cho don nay: co bat, thu phuc bat, nguoi choi danh Pokemon hoang da (khong phai boss). */
    static boolean apDung(final Char p, final LiveActor a) {
        if (p == null || !(a instanceof Monster)) {
            return false;
        }
        final Monster m = (Monster) a;
        if (!LoaiQuaiGame.laHoangDa(m) || m.isBoss || m.isDead) {
            return false;
        }
        return BatPokemonGame.bat() && LauDai.so(K_BAT, 1) != 0;
    }

    /** Sat thuong {@code dam} sap tru vao {@code a} (HP dau don {@code hpDau}), da cat neu can. */
    public static int gioiHan(final Char p, final LiveActor a, final int dam, final int hpDau) {
        try {
            if (dam <= 0 || !apDung(p, a)) {
                return dam;
            }
            return BatPokemon.satThuongGiu(hpDau, a.hp, a.maxhp, dam, BatPokemonGame.nguongHp(), giuPhanTram());
        } catch (final RuntimeException e) {
            return dam;
        }
    }

    /** Chot cuoi don: neu sau moi phan sat thuong {@code a} &lt;= 0 ma dau don tren nguong -> giu lai. */
    public static void chot(final Char p, final LiveActor a, final int hpDau) {
        try {
            if (a == null || a.hp > 0 || !apDung(p, a)) {
                return;
            }
            a.hp = (int) BatPokemon.hpSauDon(hpDau, a.hp, a.maxhp, BatPokemonGame.nguongHp(), giuPhanTram());
        } catch (final RuntimeException ignored) {
        }
    }
}
