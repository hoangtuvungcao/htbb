package real;

import com.htbb.pokemon.LangKhoiDauLuat;

/**
 * LANG KHOI DAU theo vung (nhanh sua-tuyen) - lop noi game cua com.htbb.pokemon.LangKhoiDauLuat.
 *
 * Ma goc viet cung bang lang {{70,1701},{0,301},{80,1901}} chi so theo myCountry o ~15 noi (roi san 105, ve lang, hoi
 * sinh, dang nhap, xa phu, Pidgeot "Về làng"...) -> chon "Kanto" ra Blackthorn, "Johto" ra Pewter. Moi noi do nay goi
 * {@link #bang(Char)} / {@link #viTri(int)} / {@link #oCong(Char, int)}: nhan vat tao tu ban sua (co dong trong
 * tob_lang_khoi_dau, ghi luc tao o DatabaseNhanVat.addChar) ve dung lang cua vung; nhan vat cu (khong co dong) giu
 * nguyen lang dang song - khong ai bi doi cho.
 */
public final class LangKhoiDau {

    static final String TAO_BANG = "CREATE TABLE IF NOT EXISTS tob_lang_khoi_dau (id_char INT NOT NULL, vung TINYINT NOT NULL,"
            + " lang INT NOT NULL, tao_luc DATETIME NOT NULL, PRIMARY KEY (id_char)) DEFAULT CHARSET=utf8";

    private LangKhoiDau() {
    }

    /** TeamServer luc khoi dong (moi che do). */
    public static void taoBang() {
        if (data.DatabaseLauDai.ghiCong(TAO_BANG) >= 0) {
            System.out.println("[LANG] bang tob_lang_khoi_dau san sang (lang khoi dau theo vung cho nhan vat moi).");
        }
    }

    /** DatabaseNhanVat.addChar: nhan vat moi -> lang dung vung. */
    public static void ghiNhanVatMoi(final int idChar, final int vung) {
        if (idChar <= 0) {
            return;
        }
        data.DatabaseLauDai.ghiCong("INSERT IGNORE INTO tob_lang_khoi_dau (id_char, vung, lang, tao_luc) VALUES (?, ?, ?, NOW())",
                Integer.valueOf(idChar), Integer.valueOf(vung), Integer.valueOf(LangKhoiDauLuat.langCua(vung, true)));
    }

    /** Nap co lang dung vung cua nhan vat (dang nhap); goi lai duoc. */
    public static boolean nap(final Char p) {
        if (p == null) {
            return false;
        }
        boolean co = false;
        if (p.charDBID > 0) {
            co = data.DatabaseLauDai.coLangKhoiDau(p.charDBID);
        }
        p.langDungVung = Boolean.valueOf(co);
        return co;
    }

    /** Nhan vat dung bang lang dung vung (nap luoi neu chua). */
    public static boolean dungVung(final Char p) {
        if (p == null) {
            return false;
        }
        final Boolean b = p.langDungVung;
        return b != null ? b.booleanValue() : nap(p);
    }

    /** Bang lang (chi so = vung) cua nhan vat - thay cho {{70,1701},{0,301},{80,1901}}. */
    public static int[][] bang(final Char p) {
        return LangKhoiDauLuat.bang(dungVung(p));
    }

    /** Bang diem xuat hien (chi so = vung) khop {@link #bang(Char)}. */
    public static int[][] bangViTri(final Char p) {
        return LangKhoiDauLuat.bangViTri(dungVung(p));
    }

    /** Diem xuat hien trong lang theo ban do dich (khong theo vung): null = khong phai lang. */
    public static int[] viTri(final int mapId) {
        return LangKhoiDauLuat.viTri(mapId);
    }

    /** Chi so cong (Map.getLocaltion / createMessageLocation) cua vung {@code vung} voi nhan vat nay. */
    public static int oCong(final Char p, final int vung) {
        if (vung < 0) {
            return vung;
        }
        return LangKhoiDauLuat.oCong(vung, dungVung(p));
    }
}
