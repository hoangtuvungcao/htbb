package data;

/**
 * TEP SINH TU DONG - KHONG SUA TAY.
 * Nguon: HTBB-Web/shared/data/quy-tac-dang-ky.json (quy tac dang ky CHUNG cho web + game).
 * Sinh lai: cd HTBB-Web && node scripts/sinh-quy-tac-dang-ky-java.mjs
 * Logic dung du lieu nay: data.QuyTacDangKy (ban dich 1-1 cua HTBB-Web/shared/dang-ky/kiem-tra.ts).
 */
public final class QuyTacDangKyDuLieu {

    /** SHA-256 cua tep JSON nguon luc sinh. Kiem thu hai ben so gia tri nay de bat lech. */
    public static final String SHA256 = "1b272f21b8824f31cf86221719a2ff2e213e4c0152734982e5339e053d5beeb2";
    public static final int PHIEN_BAN = 1;

    public static final int TEN_MIN = 4;
    public static final int TEN_MAX = 20;
    public static final int MAT_KHAU_MIN = 4;
    public static final int MAT_KHAU_MAX = 20;
    public static final String[] MAT_KHAU_CAM = {"000000"};
    public static final int EMAIL_TOI_DA = 100;
    public static final boolean EMAIL_BAT_BUOC_WEB = true;
    public static final boolean EMAIL_BAT_BUOC_GAME = false;

    public static final String[] TEN_QUAN_TRI_BANG = {"root", "sysop", "system", "server", "staff", "support"};
    public static final String[] TEN_QUAN_TRI_BAT_DAU = {"admin", "adm", "gm", "dm", "mod", "smod", "bqt", "quantri", "hotro", "sysop", "staff", "server", "system"};
    public static final String[] TEN_QUAN_TRI_CHUA = {"admin", "administrator", "moderator", "quantri", "quanly", "hotro", "gamemaster", "bqt", "teamobi", "kpah"};
    public static final String[] TU_CAM = {"ditme", "ditmia", "ditcon", "ditbo", "ditnhau", "dimemay", "dmm", "dcm", "dkm", "dcmm", "dmvl", "vcl", "vkl", "vcc", "cailon", "conlon", "lonme", "loncon", "concac", "caccon", "occho", "occac", "dumamay", "dumemay", "dumama", "duma", "chetme", "chetcha", "nungloz", "loz", "buoito", "thangngu", "conchongu", "sex", "porn", "fuck", "shit", "bitch", "pussy", "dick", "cave", "phophong", "hiepdam", "hentai", "xxx"};
    public static final String THAY_THE_SO_TU = "013457";
    public static final String THAY_THE_SO_THANH = "oieast";

    public static final int HAN_MUC_MOI_GIO = 3;
    public static final int HAN_MUC_MOI_NGAY = 10;

    public static final String[] THONG_BAO_MA = {"tenSai", "tenQuanTri", "tenTuCam", "matKhauSai", "matKhauKhongKhop", "emailThieu", "emailSai", "trungTen", "quaGio", "quaNgay", "loiHeThong"};
    public static final String[] THONG_BAO_CHU = {"T\u00ean t\u00e0i kho\u1ea3n ph\u1ea3i t\u1eeb {min}-{max} k\u00fd t\u1ef1, ch\u1ec9 g\u1ed3m ch\u1eef th\u01b0\u1eddng v\u00e0 s\u1ed1.", "T\u00ean t\u00e0i kho\u1ea3n kh\u00f4ng \u0111\u01b0\u1ee3c gi\u1ed1ng t\u00ean qu\u1ea3n tr\u1ecb vi\u00ean (admin, gm, mod...). Vui l\u00f2ng ch\u1ecdn t\u00ean kh\u00e1c.", "T\u00ean t\u00e0i kho\u1ea3n ch\u1ee9a t\u1eeb ng\u1eef kh\u00f4ng ph\u00f9 h\u1ee3p. Vui l\u00f2ng ch\u1ecdn t\u00ean kh\u00e1c.", "M\u1eadt kh\u1ea9u ph\u1ea3i t\u1eeb {min}-{max} k\u00fd t\u1ef1, ch\u1ec9 g\u1ed3m ch\u1eef c\u00e1i kh\u00f4ng d\u1ea5u v\u00e0 s\u1ed1, v\u00e0 kh\u00f4ng \u0111\u01b0\u1ee3c l\u00e0 000000.", "M\u1eadt kh\u1ea9u nh\u1eadp l\u1ea1i kh\u00f4ng kh\u1edbp.", "Vui l\u00f2ng nh\u1eadp email (d\u00f9ng \u0111\u1ec3 l\u1ea5y l\u1ea1i m\u1eadt kh\u1ea9u).", "Email kh\u00f4ng h\u1ee3p l\u1ec7.", "T\u00ean t\u00e0i kho\u1ea3n \u0111\u00e3 t\u1ed3n t\u1ea1i, vui l\u00f2ng ch\u1ecdn t\u00ean kh\u00e1c.", "B\u1ea1n \u0111\u00e3 t\u1ea1o qu\u00e1 nhi\u1ec1u t\u00e0i kho\u1ea3n t\u1eeb \u0111\u1ecba ch\u1ec9 n\u00e0y. Vui l\u00f2ng th\u1eed l\u1ea1i sau m\u1ed9t gi\u1edd.", "B\u1ea1n \u0111\u00e3 t\u1ea1o qu\u00e1 nhi\u1ec1u t\u00e0i kho\u1ea3n h\u00f4m nay t\u1eeb \u0111\u1ecba ch\u1ec9 n\u00e0y. Vui l\u00f2ng th\u1eed l\u1ea1i v\u00e0o ng\u00e0y mai.", "H\u1ec7 th\u1ed1ng t\u1ea1m th\u1eddi ch\u01b0a t\u1ea1o \u0111\u01b0\u1ee3c t\u00e0i kho\u1ea3n. Vui l\u00f2ng th\u1eed l\u1ea1i sau."};

    /** Bo ca chung {u, p, c, e, maLoiWeb (phay), maLoiGame (phay)} - chay ca o npm test lan test/Kiem.java. */
    public static final String[][] THU_NGHIEM = {
        {"pikachu99", "abc123", "abc123", "a@b.vn", "", ""},
        {"pikachu99", "abc123", "abc123", "", "emailThieu", ""},
        {"pikachu99", "abc123", "abc123", "khong-hop-le", "emailSai", "emailSai"},
        {"abc", "abc123", "abc123", "a@b.vn", "tenSai", "tenSai"},
        {"abcdefghijklmnopqrstu", "abc123", "abc123", "", "tenSai,emailThieu", "tenSai"},
        {"Pikachu", "abc123", "abc123", "", "tenSai,emailThieu", "tenSai"},
        {"pika_chu", "abc123", "abc123", "", "tenSai,emailThieu", "tenSai"},
        {"admin", "abc123", "abc123", "", "tenQuanTri,emailThieu", "tenQuanTri"},
        {"gm2026", "abc123", "abc123", "", "tenQuanTri,emailThieu", "tenQuanTri"},
        {"dmtuan", "abc123", "abc123", "", "tenQuanTri,emailThieu", "tenQuanTri"},
        {"modvip", "abc123", "abc123", "", "tenQuanTri,emailThieu", "tenQuanTri"},
        {"tuanadmin1", "abc123", "abc123", "", "tenQuanTri,emailThieu", "tenQuanTri"},
        {"root", "abc123", "abc123", "", "tenQuanTri,emailThieu", "tenQuanTri"},
        {"toiditme", "abc123", "abc123", "", "tenTuCam,emailThieu", "tenTuCam"},
        {"d1tme123", "abc123", "abc123", "", "tenTuCam,emailThieu", "tenTuCam"},
        {"vcl2k", "abc123", "abc123", "", "tenTuCam,emailThieu", "tenTuCam"},
        {"nguyenvan", "abc123", "abc123", "", "emailThieu", ""},
        {"longphung", "abc123", "abc123", "", "emailThieu", ""},
        {"pikachu99", "abc", "abc", "", "matKhauSai,emailThieu", "matKhauSai"},
        {"pikachu99", "000000", "000000", "", "matKhauSai,emailThieu", "matKhauSai"},
        {"pikachu99", "m\u1eadt123", "m\u1eadt123", "", "matKhauSai,emailThieu", "matKhauSai"},
        {"pikachu99", "abc 123", "abc 123", "", "matKhauSai,emailThieu", "matKhauSai"},
        {"pikachu99", "abcdefghijklmnopqrstu", "abcdefghijklmnopqrstu", "", "matKhauSai,emailThieu", "matKhauSai"},
        {"pikachu99", "abc123", "abc124", "", "matKhauKhongKhop,emailThieu", "matKhauKhongKhop"},
        {"pikachu99", "ABC123", "ABC123", "x.y+z@mail.example.com", "", ""},
        {"pikachu99", "abc123", "abc123", "a..b@x.vn", "emailSai", "emailSai"},
        {"pikachu99", "abc123", "abc123", "a@localhost", "emailSai", "emailSai"},
        {"pikachu99", "abc123", "abc123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.vn", "emailSai", "emailSai"},
        {"", "", "", "", "tenSai,matKhauSai,emailThieu", "tenSai,matKhauSai"}
    };

    private QuyTacDangKyDuLieu() {
    }
}
