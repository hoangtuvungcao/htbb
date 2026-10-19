package real;

/** So ban client (PhienBan.BAN gui luc dang nhap, -1 = khong ro / Unity) cua nhan vat - cho lop ngoai goi real. */
public final class BanClientNhanVat {

    private BanClientNhanVat() {
    }

    public static int ban(final Char p) {
        return p != null && p.session != null ? p.session.banClient : -1;
    }
}
