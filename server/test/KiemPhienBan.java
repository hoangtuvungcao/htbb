/**
 * Kiem thu so sanh phien ban client (real.MessageCreator).
 *
 * Vi sao quan trong: may chu quyet dinh hanh vi theo phien ban client, va khi
 * chuyen tu client 2.3.2 sang 2.7.6 thi hai loai se chay lan nhau mot thoi
 * gian. Sai o day la nguoi choi bi doi xu theo phien ban cua NGUOI KHAC.
 */
public final class KiemPhienBan {

    public static void chay() {
        Kiem.nhom("real.MessageCreator - so sanh phien ban");

        // Client C# dang dung
        Kiem.sai("2.3.2 -> khong phai >=1.62? SAI, 2>1 nen la DUNG",
                !real.MessageCreator.isVersionAtLeast162("2.3.2"));
        Kiem.dung("2.3.2 >= 1.62", real.MessageCreator.isVersionAtLeast162("2.3.2"));
        // Client J2ME moi
        Kiem.dung("2.7.6 >= 1.62", real.MessageCreator.isVersionAtLeast162("2.7.6"));
        // Mac dinh cu cua server
        Kiem.sai("1.0.3 < 1.62", real.MessageCreator.isVersionAtLeast162("1.0.3"));
        // Bien
        Kiem.sai("1.61 < 1.62", real.MessageCreator.isVersionAtLeast162("1.61"));
        Kiem.dung("1.62 >= 1.62", real.MessageCreator.isVersionAtLeast162("1.62"));
        Kiem.dung("1.63 >= 1.62", real.MessageCreator.isVersionAtLeast162("1.63"));
        // Session.version mac dinh la "0" khi chua dang nhap
        Kiem.sai("\"0\" (mac dinh cua Session) -> false, khong nem ngoai le",
                real.MessageCreator.isVersionAtLeast162("0"));
        // Chuoi hong khong duoc lam sap server
        Kiem.sai("null -> false", real.MessageCreator.isVersionAtLeast162(null));
        Kiem.sai("chuoi rong -> false", real.MessageCreator.isVersionAtLeast162(""));
        Kiem.sai("\"abc\" -> false", real.MessageCreator.isVersionAtLeast162("abc"));
        Kiem.sai("\"1.x\" -> false", real.MessageCreator.isVersionAtLeast162("1.x"));

        // isVersion162(Char) voi null phai an toan, khong nem ngoai le
        Kiem.nhom("MessageCreator - nguong 2.7 (goi 26 doi byte -> short)");
        Kiem.sai("2.0.9 (Ngu De) < 2.7 -> ghi BYTE", real.MessageCreator.isVersionAtLeast27("2.0.9"));
        Kiem.sai("2.3.2 (client C#) < 2.7 -> ghi BYTE", real.MessageCreator.isVersionAtLeast27("2.3.2"));
        Kiem.dung("2.7.6 >= 2.7 -> ghi SHORT", real.MessageCreator.isVersionAtLeast27("2.7.6"));
        Kiem.dung("2.7.0 >= 2.7 -> ghi SHORT", real.MessageCreator.isVersionAtLeast27("2.7.0"));
        Kiem.sai("2.6.9 < 2.7 -> ghi BYTE", real.MessageCreator.isVersionAtLeast27("2.6.9"));
        Kiem.dung("3.0.0 >= 2.7 -> ghi SHORT", real.MessageCreator.isVersionAtLeast27("3.0.0"));
        Kiem.sai("null -> ghi BYTE (an toan, giu dinh dang cu)", real.MessageCreator.isVersionAtLeast27(null));
        Kiem.sai("\"0\" mac dinh -> ghi BYTE", real.MessageCreator.isVersionAtLeast27("0"));

        Kiem.sai("isVersion162(null) -> false", real.MessageCreator.isVersion162((real.Char) null));
    }

    private KiemPhienBan() {
    }
}
