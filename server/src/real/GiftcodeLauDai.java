package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.ThongSoLauDai;

/**
 * Giftcode khi bat co sv.lau_dai (GD2 5.4, THIET_KE 2.1): chi vat pham (KHONG xu / luong /
 * luong khoa), toi da {@code giftcode.toi_da_thang} (mac dinh 2) ma moi thang moi nhan vat.
 * Kiem o CHINH noi xu ly (DatabaseGiftCode.newDoGetGiftCode, CODING_CONVENTION muc 11).
 */
public final class GiftcodeLauDai {

    private GiftcodeLauDai() {
    }

    static int thang(final long ms) {
        return NangLuong.ngay(ms) / 100;
    }

    /** null = duoc nhap; nguoc lai ly do tu choi. */
    public static String choPhep(final Char p) {
        if (!LauDai.bat()) {
            return null;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return "Tạm thời không nhận được mã, thử lại sau.";
        }
        final int toiDa = LauDai.so(LauDai.K_GIFTCODE_THANG, ThongSoLauDai.GIFTCODE_TOI_DA_THANG);
        synchronized (h) {
            final int t = thang(com.htbb.pokemon.DongHo.bay());
            final int dem = h.gcThang == t ? h.gcDem : 0;
            return dem >= toiDa ? "Mỗi tháng chỉ nhập được " + toiDa + " mã quà tặng." : null;
        }
    }

    /** Ghi nhan da dung 1 ma trong thang (goi SAU khi tru luot ma, TRUOC khi phat). */
    public static void ghiNhan(final Char p) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return;
        }
        synchronized (h) {
            final int t = thang(com.htbb.pokemon.DongHo.bay());
            if (h.gcThang != t) {
                h.gcThang = t;
                h.gcDem = 0;
            }
            h.gcDem++;
            h.ban = true;
        }
    }
}
