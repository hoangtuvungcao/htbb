package com.htbb.pokemon;

/**
 * LUAT PK THE GIOI (0.9.7, chu game duyet sau ra soat loi choi) - phan THUAN, kiem duoc khong CSDL:
 * <ul>
 * <li><b>Khong ban suc manh:</b> bo mien mat EXP cua VIP 3 (VIP lay theo diem nap) - moi nguoi cung luat.</li>
 * <li><b>Khong tut cap:</b> EXP mat khi guc trong PK = 1% (dang ten do) / 0,1% (vo toi) EXP can cua cap HIEN TAI (bang
 * EXP dang dung, LevelDetail.expRequire sau ExpLauDai.apDungBang), kep de khong xuong duoi moc dau cap.</li>
 * <li><b>Ten do / nghiep (killer, short &lt;= 32000):</b> nguong "khong the PK them" cu 319000 khong bao gio toi
 * (killer co tran 32000) -&gt; nguong {@link #NGUONG_KHOA_PK} trong tam short. Nghiep phai theo GIO ONLINE
 * ({@link #PHAI_MOI_PHUT} / phut) thay cho -200 moi ngay dang nhap.</li>
 * <li><b>Chenh cap:</b> ngoai vung PvP khong danh duoc nguoi THAP hon qua {@link #CHENH_CAP} cap (tru khi nguoi do dang
 * ten do / da bat co PK).</li>
 * <li><b>Vung an toan:</b> thi tran (data_map_ten.loai = thi_tran) va lang khong PK, tru luc chien thanh / dua the.</li>
 * </ul>
 */
public final class LuatPk {

    private LuatPk() {
    }

    /** Chenh cap toi da khi tan cong nguoi thap cap hon ngoai vung PvP. */
    public static final int CHENH_CAP = 10;
    /** Nghiep tu nguong nay tro len: khong PK them duoc ngoai vung PvP (dang ten do nang). 100 / lan giet nguoi vo toi. */
    public static final int NGUONG_KHOA_PK = 3000;
    /** Nghiep phai moi phut online (khong phai theo ngay dang nhap). 1 lan giet vo toi (+100) phai sau ~20 phut. */
    public static final int PHAI_MOI_PHUT = 5;
    /** Tran nghiep (Char.killer la short). */
    public static final int TRAN_NGHIEP = 32000;

    /**
     * EXP mat khi guc trong PK (chua tru): 1% (ten do) hoac 0,1% EXP can cua cap hien tai, KHONG xuong duoi moc dau cap
     * {@code mocDauCap} (= expMain[lv - 1]).
     *
     * @param exp      EXP hien tai (tich luy)
     * @param canCap   EXP can de len cap ke (expRequire[lv])
     * @param mocDauCap EXP toi thieu cua cap hien tai
     */
    public static long expMat(final long exp, final long canCap, final long mocDauCap, final boolean tenDo) {
        if (exp <= 0L || canCap <= 0L) {
            return 0L;
        }
        final long mat = canCap / (tenDo ? 100L : 1000L);
        final long conLai = Math.max(0L, exp - Math.max(0L, mocDauCap));
        return Math.max(0L, Math.min(mat, conLai));
    }

    /** Nghiep sau {@code phut} phut online (khong am). */
    public static int phaiNghiep(final int killer, final int phut) {
        if (killer <= 0 || phut <= 0) {
            return Math.max(0, killer);
        }
        return (int) Math.max(0L, (long) killer - (long) phut * PHAI_MOI_PHUT);
    }

    /** Nghiep da du nguong khoa PK (ten do nang). */
    public static boolean khoaPk(final int killer) {
        return killer >= NGUONG_KHOA_PK;
    }

    /**
     * Ly do CHAN mot don PK the gioi, null = cho danh. Chi goi ngoai vung PvP (dau truong, loi dai, chien truong, chien
     * thanh... da cho phep o noi goi).
     *
     * @param vungAnToan ban do la thi tran / lang va khong co chien thanh
     * @param capDanh    cap nguoi danh
     * @param capBi      cap nguoi bi danh
     * @param biDangPk   nguoi bi danh dang ten do / bat co PK (tu chiu rui ro)
     * @param nghiepDanh nghiep nguoi danh
     */
    public static String chan(final boolean vungAnToan, final int capDanh, final int capBi, final boolean biDangPk,
            final int nghiepDanh) {
        if (vungAnToan) {
            return "Thị trấn là vùng an toàn, không thể tấn công người chơi khác.";
        }
        if (!biDangPk && capBi < capDanh - CHENH_CAP) {
            return "Không thể tấn công người chơi thấp hơn bạn quá " + CHENH_CAP + " cấp ngoài vùng PvP.";
        }
        if (!biDangPk && khoaPk(nghiepDanh)) {
            return "Nghiệp quá nặng, không thể PK thêm. Online để nghiệp giảm dần.";
        }
        return null;
    }
}
