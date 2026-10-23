package com.htbb.pokemon;

/**
 * Icon (data_item.xstart = id anh) cua trang bi theo O (type). Ham THUAN, khong doc CSDL.
 *
 * Mon goc moi o (11 bac so cap..toi thuong, ten "Bang do bac · Cap 24"...) dung CHUNG mot icon. Truoc 24/10/2026 la icon
 * PMD 14000+type (map/icon_pokemon) - 9 o nhin sai do vat (Bang do = tui nau, Than lua = khoi gach, Nam cham = vien bi
 * sat, Vong co = hat ngoc nho, Long vu = vay ca tron, Gang dam = mong vuot, Huy hieu = bia tron, Khan lua = cuc da, Dai
 * = soi day xoan). Nay tro sang icon tu ve cua bo do ho moi CUNG o (map/icon_do_moi, 15000+n - IconDoMoi), ban mau
 * trung tinh: o ho Pokemon (khan, dai, bang do, vong co, long vu, ruy bang) lay mau "Meowth" (he Thuong, xam); o vung
 * (do cam tay, huy hieu, gang, phien da) lay mau thanh pho hop do vat. 5 o giu icon PMD vi da dung hinh (khan quang
 * do, hat giong la, binh nuoc than, phien da xam, no hong).
 *
 * SQL: phan B sql/htbb.sql khoi doi_icon_trang_bi.sql. Kiem: server/test/KiemIconTrangBi.java (moi trang bi chi dung icon
 * thuoc tap duyet cua o minh). Client test ChupTrongGame dung cung bang nay cho mon mau.
 */
public final class IconTrangBi {

    private IconTrangBi() {
    }

    /** Cac o trang bi (data_item.type) va danh tu dau ten cua mon trong o (mon goc + do ho / vung). */
    public static final int[] O = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 19};
    public static final String[] DANH_TU = {"Khăn quàng", "Đai", "Băng đô", "Than lửa", "Hạt giống thần", "Khăn lụa", "Nước thần",
        "Nam châm", "Huy hiệu", "Vòng cổ", "Lông vũ", "Găng", "Phiến đá", "Ruy băng"};
    /** Icon cu (PMD) cua mon goc moi o. */
    public static final int[] GOC_CU = {14000, 14001, 14002, 14003, 14004, 14005, 14006, 14007, 14008, 14009, 14010, 14011, 14012, 14013};
    /** Icon mon goc moi o (sau 24/10/2026). */
    public static final int[] GOC = {14000, 15005, 15010, 15543, 14004, 15577, 14006, 15563, 15540, 15015, 15020, 15541, 14012, 14013};

    /** Chi so cua o {@code type} trong O, -1 neu khong phai o trang bi. */
    public static int viTri(final int type) {
        for (int i = 0; i < O.length; i++) {
            if (O[i] == type) {
                return i;
            }
        }
        return -1;
    }

    /** Icon mon goc cua o, -1 neu khong phai o trang bi. */
    public static int goc(final int type) {
        final int i = viTri(type);
        return i < 0 ? -1 : GOC[i];
    }

    /** Ten thuoc o (bat dau bang danh tu cua o + dau cach). */
    public static boolean tenCuaO(final int type, final String ten) {
        final int i = viTri(type);
        return i >= 0 && ten != null && ten.startsWith(DANH_TU[i] + " ");
    }
}
