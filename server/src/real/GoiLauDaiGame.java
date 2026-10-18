package real;

import com.htbb.pokemon.GoiLauDai;
import com.htbb.pokemon.TrangThaiPokemon;
import io.Message;

/**
 * Tao io.Message cho 5 goi -95..-99 (noi dung byte o com.htbb.pokemon.GoiLauDai, dac ta o
 * THIET_KE_LAU_DAI.md muc 4.6). NOI DUY NHAT trong src/ tao Message(-95..-99) (test kiem).
 * Chi goi khi LauDai.bat().
 */
public final class GoiLauDaiGame {

    private GoiLauDaiGame() {
    }

    /** -95 kem sau lenh 9 (nguoi -> quai). Dong thoi thu gay trang thai theo he chieu. */
    static Message don(final Char p, final LiveActor dich, final int dam) {
        try {
            VatPhamDungLauDai.sauDonDanh(p, dam);
            int co = p.lauDaiCo;
            int tt = 0;
            final int st = TrangThaiGame.thuGay(p, dich, p.lauDaiHe);
            if (st > 0) {
                co |= com.htbb.pokemon.SatThuongLauDai.CO_TRANG_THAI;
                tt = st;
            }
            final Message m = new Message(GoiLauDai.POKE_DON);
            GoiLauDai.ghiDon(m.dos, p.id, GoiLauDai.LOAI_NGUOI, dich.id, loai(dich), dam, co, p.lauDaiHe, tt);
            if (tt > 0) {
                m.kemTheo = trangThai(dich, TrangThaiGame.lay(dich));
            }
            return m;
        } catch (final Exception e) {
            return null;
        }
    }

    static int loai(final LiveActor a) {
        return a instanceof Char ? GoiLauDai.LOAI_NGUOI : GoiLauDai.LOAI_QUAI;
    }

    /** -97 trang thai hien tai cua actor (msConLai = 0 nghia la go). */
    static Message trangThai(final LiveActor a, final TrangThaiPokemon t) {
        try {
            final long bay = System.currentTimeMillis();
            final Message m = new Message(GoiLauDai.POKE_TRANG_THAI);
            GoiLauDai.ghiTrangThai(m.dos, a.id, loai(a), t == null ? 0 : t.loai, t == null ? 0 : t.chong,
                    t == null ? 0 : t.msConLai(bay));
            return m;
        } catch (final Exception e) {
            return null;
        }
    }

    /** -96 bao truoc chieu lon cua boss. */
    static Message canhBao(final Monster boss, final int idChieu, final int he, final int hinh, final int x, final int y,
            final int r, final int rong, final int huong, final int ms) {
        try {
            final Message m = new Message(GoiLauDai.POKE_CANH_BAO);
            GoiLauDai.ghiCanhBao(m.dos, boss.id, idChieu, he, hinh, x, y, r, rong, huong, ms);
            return m;
        } catch (final Exception e) {
            return null;
        }
    }

    /** -98 su kien boss. */
    static Message boss(final Monster boss, final int pha, final int suKien, final int giayDenNo) {
        try {
            final Message m = new Message(GoiLauDai.POKE_BOSS);
            GoiLauDai.ghiBoss(m.dos, boss.id, pha, suKien, Math.max(0, boss.hp), giayDenNo);
            return m;
        } catch (final Exception e) {
            return null;
        }
    }

    /** -99 bang dong gop. */
    static Message dongGop(final int[] id, final int[] phanNghin, final int n, final int cuaToi) {
        try {
            final Message m = new Message(GoiLauDai.POKE_DONG_GOP);
            GoiLauDai.ghiDongGop(m.dos, id, phanNghin, n, cuaToi);
            return m;
        } catch (final Exception e) {
            return null;
        }
    }
}
