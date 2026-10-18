package com.htbb.pokemon;

/**
 * Co {@code sv.he_sinh_thai} va SO LIEU GOC cua he sinh thai game (docs/thiet-ke-game.md).
 * Lop THUAN: test/KiemHeSinhThai va tools/pokemon/MoPhongKinhTe doc thang cac hang so o day,
 * nen doi so thi sua O DAY, chay lai mo phong + test.
 *
 * Tat co (mac dinh, thieu dong) = game y nhu truoc: moi diem moc "// he sinh thai:" tra ve ngay.
 *
 * Doc som tu server.ini ngay khi lop duoc nap : diem moc
 * Monster.update chay tu luong ban do co the truoc khi TeamServer doc xong cau hinh.
 *
 * Don vi tien: "phan nghin gio thu nhap" (‰H) o cap tham chieu cua bac vat lieu
 * (docs/thiet-ke-game.md N1: moi gia tinh theo gio choi, khong theo so co dinh).
 */
public final class HeSinhThai {

    /** Ten khoa trong server.ini. */
    public static final String KHOA = "sv.he_sinh_thai";

    private static volatile boolean bat = docTuTepCauHinh();

    private HeSinhThai() {
    }

    private static boolean docTuTepCauHinh() {
        try (java.io.InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Paths.get("server.ini"))) {
            final java.util.Properties p = new java.util.Properties();
            p.load(in);
            return docGiaTri(p.getProperty(KHOA));
        } catch (Exception e) {
            return false;
        }
    }

    /** He sinh thai dang bat khong. Ham DUY NHAT moi cho re nhanh duoc dung. */
    public static boolean bat() {
        return bat;
    }

    /** Chi TeamServer / test duoc goi. */
    public static void dat(final boolean giaTri) {
        bat = giaTri;
    }

    /** "1" (co the co khoang trang) = bat; moi gia tri khac, ke ca null = tat. */
    public static boolean docGiaTri(final String giaTri) {
        return giaTri != null && "1".equals(giaTri.trim());
    }

    // =============================================================== THE GIOI (muc 1)

    /** % lan hoi sinh quai duoc thay loai theo sinh thai (thoi tiet / ngay dem / mua). */
    public static final int TI_LE_THAY_PHAN_TRAM = 15;
    /** % lan hoi sinh tren tuyen dang bung phat thanh loai bung phat. */
    public static final int TI_LE_BUNG_PHAT_PHAN_TRAM = 40;
    /** Thoi tiet uu the: +% sat thuong chieu cung he; bat loi (mua-Lua, nang-Nuoc): -%. */
    public static final int THOI_TIET_SAT_THUONG_PHAN_TRAM = 10;
    /** He so trong so sinh cua loai hop thoi tiet / ban dem (x lan). */
    public static final int HE_SO_SINH_HOP_THOI_TIET = 3;
    /** Moi ngay bao nhieu khung bung phat (moi khung 1 gio) va gio bat dau. */
    public static final int[] GIO_BUNG_PHAT = {11, 17, 20, 22};
    /** Gio dem: [GIO_DEM_TU, 24) va [0, GIO_DEM_DEN). */
    public static final int GIO_DEM_TU = 19;
    public static final int GIO_DEM_DEN = 6;
    /** Khoang toi thieu giua hai thong bao quai hiem cung loai (ms). */
    public static final long THONG_BAO_HIEM_CACH_MS = 10L * 60L * 1000L;

    // =============================================================== KY NANG SONG (muc 2)

    /** The luc sinh hoat moi ngay; phan chua dung cong don toi da {@link #THE_LUC_TOI_DA}. */
    public static final int THE_LUC_NGAY = 100;
    public static final int THE_LUC_TOI_DA = 200;
    /** Chi phi the luc moi lan: cau ca, dao, nau/che, (thu hoach vuon mien phi). */
    public static final int TL_CAU_CA = 2;
    public static final int TL_DAO = 4;
    public static final int TL_NAU = 3;
    /** Cap ky nang toi da. */
    public static final int CAP_KY_NANG_TOI_DA = 50;
    /** Moi lan lam: EXP ky nang co ban. */
    public static final int EXP_MOI_LAN = 10;
    /** Phi nguyen lieu bat buoc (moi cau / xeng / cui lua), ‰H o cap nguoi choi. */
    public static final int PHI_MOI_CAU_PHAN_NGHIN = 2;
    public static final int PHI_XENG_PHAN_NGHIN = 4;
    public static final int PHI_BEP_PHAN_NGHIN = 5;

    // =============================================================== NHA TRE (muc 3)

    /** Phi gui nha tre moi ngay (‰H o cap nguoi choi). */
    public static final int PHI_NHA_TRE_NGAY_PHAN_NGHIN = 50;
    /** So buoc (quai ha) de nha tre de 1 trung; toi da trung/ngay. */
    public static final int BUOC_DE_TRUNG = 300;
    public static final int TRUNG_TOI_DA_NGAY = 2;
    /** Toi da trung trong tui. */
    public static final int TRUNG_TOI_DA_TUI = 6;
    /** Shiny trung: mau so (1/x) va pity (trung thu x chac chan shiny). */
    public static final int SHINY_TRUNG_MAU = 512;
    public static final int SHINY_TRUNG_PITY = 200;
    /** IV moi chi so 0..31 (3 chi so: HP, cong, thu). */
    public static final int IV_TOI_DA = 31;
    /** % dot bien IV khi di truyen. */
    public static final int IV_DOT_BIEN_PHAN_TRAM = 15;

    // =============================================================== CHO (muc 4)

    /** Thue ban (dot) va phi dang (khong hoan) - KHOP THIET_KE_LAU_DAI 2.4. */
    public static final int THUE_CHO_PHAN_TRAM = 6;
    public static final int PHI_DANG_PHAN_TRAM = 1;
    /** NPC mua lai = % gia tham chieu (san gia). */
    public static final int SAN_NPC_PHAN_TRAM = 40;
    /** Tran xu NPC mua lai moi ngay (‰H o cap nguoi choi). */
    public static final int TRAN_BAN_NPC_NGAY_PHAN_NGHIN = 100;
    /** Gia rao toi da = x lan gia tham chieu (chong rua tien). */
    public static final int GIA_TOI_DA_LAN = 5;
    /** Tin ban / don dat hang het han sau (ngay). */
    public static final int HAN_CHO_NGAY = 3;
    /** Toi da tin ban dang mo / don dang mo moi nguoi. */
    public static final int TOI_DA_TIN_BAN = 8;
    public static final int TOI_DA_DON = 4;

    // =============================================================== MINI GAME (muc 5)

    /** Gio do "Ai la Pokemon?" moi ngay; thoi gian tra loi (giay). */
    public static final int[] GIO_DO_VUI = {12, 21};
    public static final int PHUT_DO_VUI = 30;
    public static final int GIAY_TRA_LOI = 90;
    /** Thuong (luong khoa, khoa) cho nguoi tra loi dung dau tien. */
    public static final int THUONG_DO_VUI_LUONG_KHOA = 3;
    /** Dua Pokemon: gio xuat phat moi ngay, phut dang ky truoc, thuong top 1..3 (luong khoa). */
    public static final int GIO_DUA = 19;
    public static final int PHUT_DANG_KY_DUA = 10;
    public static final int[] THUONG_DUA_LUONG_KHOA = {5, 3, 2};
    public static final int DUA_TOI_DA_GIAY = 300;

    // =============================================================== SUU TAM (muc 7)

    /** Thuong moi nhiem vu nghien cuu (luong khoa). */
    public static final int THUONG_NGHIEN_CUU_LUONG_KHOA = 2;
    /** Thuong hoan thanh vung (luong khoa) va chi so vinh vien (phan nghin, cong don co tran). */
    public static final int THUONG_VUNG_LUONG_KHOA = 20;
    public static final int CHI_SO_VUNG_PHAN_NGHIN = 5;
    public static final int CHI_SO_VUNG_TRAN_PHAN_NGHIN = 30;
    /**
     * Id hieu ung danh hieu sinh thai hien tren dau = ID_EFF_DANH_HIEU_DAU + chi so trong HoSoSinhThai.DANH_HIEU
     * (Char.idEffDanhHieu). Da grep src/ + map/d_effect (17/10/2026): 9600..9615 trong (danh hieu cu 8767..8957,
     * hieu ung 8700..8999, skin 13000..13004). Client J2ME/Android ve theo dai nay.
     */
    public static final int ID_EFF_DANH_HIEU_DAU = 9600;
}
