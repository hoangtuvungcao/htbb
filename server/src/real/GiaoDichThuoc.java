package real;

/**
 * Luat kiem tra GIAO DICH THUOC giua hai nguoi choi (goi 66, MapMuaBan.doTrade), tach ra
 * thanh ham thuan tren mang int de kiem thu duoc ma khong can Char/Map/DB
 * (xem test/KiemGiaoDichThuoc.java).
 *
 * Vi sao phai kiem lai luc HOAN TAT (case 5) chu khong tin vao luc BO VAO (case 2):
 *
 *  1. Trang thai giao dich (tPotions = thuoc minh bo vao, rPotions = thuoc doi phuong bo vao
 *     cho minh) truoc day KHONG duoc xoa o cac duong huy ngang (Map.checkTrade,
 *     doUsePotion.checkTrade, actorDie): chi tItems/rItems/userTrade bi xoa. Ket qua la
 *     rPotions con nguyen sau khi huy, va o lan giao dich SAU voi bat ky ai, luc hoan tat
 *     minh duoc cong dung bay nhieu thuoc tu khong khi - doi phuong lan nay khong bo gi,
 *     doi phuong lan truoc thi chua bi tru vi da huy. Do la goc cua chuyen nhan doi nhan sam,
 *     the van tieu, bo hoa... "qua giao dich".
 *
 *  2. Giua luc bo vao va luc hoan tat, so thuoc that trong tui co the da giam bang mot duong
 *     khong di qua checkTrade (menu NPC tieu thuoc chay tren luong phien, con goi 66 chay tren
 *     luong ban do). Ban cu tru "potions -= tPotions" roi chan ve 0 - ben nhan van nhan du.
 *
 *  3. Goi 66 khong nam trong danh sach lenh rieng (RealController.privateCmd) nen chay tren
 *     LUONG CUA BAN DO nguoi gui. Hai ben o hai ban do (hai luong) khac nhau thi hai lan
 *     "case 5" chay song song, ca hai deu thay doi phuong da khoa va cung cong thuoc. Phan
 *     nay chan bang Char.KHOA_GIAO_DICH + doi hai ben cung cho dung, khong nam trong lop nay.
 *
 * Quy uoc: mang A la nguoi goi, mang B la doi phuong; tA[i] phai bang rB[i] va nguoc lai.
 */
public final class GiaoDichThuoc {

    /** Mot o thuoc binh thuong toi da 999 (giong luc nhat, luc bo vao giao dich). */
    /**
     * RUT do khoi ban giao dich (goi 66/2 kieu 2) chi duoc khi CA HAI ben chua khoa. Truoc 25/10/2026 may chu chi kiem
     * ben dang rut: B khoa + xac nhan voi 10 binh A dua, A rut ra, bo lai 1 roi khoa + xac nhan -> giao dich xong, B mat
     * do ma chi nhan 1 binh (bot bat_thuong / bt_gd.doi_sau_khi_khoa). BO THEM do sau khi doi phuong khoa van duoc (chi
     * lam doi phuong nhan nhieu hon - luong binh thuong: mot ben khoa truoc, ben kia con dang bo do).
     */
    public static boolean rutDoDuoc(final boolean minhDaKhoa, final boolean doiPhuongDaKhoa, final boolean doiPhuongDaXacNhan) {
        return !minhDaKhoa && !doiPhuongDaKhoa && !doiPhuongDaXacNhan;
    }

    public static final int TOI_DA_MOT_O = 999;

    public static final int HOP_LE = 0;
    /** Mang null hoac lech chieu dai (vi du 160 so voi 162 - ban cu tao lai bang hai con so khac nhau). */
    public static final int LOI_MANG = 1;
    /** Co so am trong tPotions/rPotions. */
    public static final int LOI_SO_AM = 2;
    /** Ben chuyen khong con du thuoc nhu luc bo vao. */
    public static final int LOI_KHONG_DU = 3;
    /** Thuoc "doi phuong bo cho minh" khong khop "doi phuong tu ghi la da bo" - trang thai cu con sot. */
    public static final int LOI_LECH_HAI_BEN = 4;
    /** O 0 (xu) hoac thuoc co isTrade = 0 trong data_potion. */
    public static final int LOI_KHONG_GIAO_DICH_DUOC = 5;
    /** Ben nhan se vuot 999 o mot o thuoc co gioi han. */
    public static final int LOI_TRAN_O = 6;

    /** Luat phu thuoc du lieu game (bang data_potion, danh sach thuoc khong gioi han) - truyen tu ngoai vao. */
    public interface LuatThuoc {
        boolean giaoDichDuoc(int idThuoc);

        boolean khongGioiHan(int idThuoc);
    }

    private GiaoDichThuoc() {
    }

    /**
     * So o hanh trang THEM ma mot ben can sau giao dich thuoc (19/10/2026): loai nhan ve ma chua co (+1 o moi loai)
     * tru loai dua di het (-1). Char.countSlotInventory KHONG tinh rPotions nen phai kiem rieng luc chot: truoc day
     * dua N loai moi cho nguoi con 1 o thi moi lan dua deu qua, chot xong tui vuot suc chua.
     */
    public static int soOThemCan(final int[] pot, final int[] dua, final int[] nhan) {
        if (pot == null) {
            return 0;
        }
        int can = 0;
        for (int i = 1; i < pot.length; i++) {
            final int d = dua != null && i < dua.length ? Math.max(0, dua[i]) : 0;
            final int r = nhan != null && i < nhan.length ? Math.max(0, nhan[i]) : 0;
            final boolean truoc = pot[i] > 0;
            final boolean sau = pot[i] - d + r > 0;
            if (!truoc && sau) {
                can++;
            } else if (truoc && !sau) {
                can--;
            }
        }
        return can;
    }

    /**
     * Kiem tra mot giao dich sap hoan tat.
     *
     * @param potA thuoc dang co trong tui A; tA thuoc A bo vao; rA thuoc B bo cho A
     * @param potB thuoc dang co trong tui B; tB thuoc B bo vao; rB thuoc A bo cho B
     * @return HOP_LE hoac mot ma LOI_* (ma dau tien gap theo thu tu o)
     */
    public static int kiemTra(final int[] potA, final int[] tA, final int[] rA,
            final int[] potB, final int[] tB, final int[] rB, final LuatThuoc luat) {
        if (potA == null || tA == null || rA == null || potB == null || tB == null || rB == null || luat == null) {
            return LOI_MANG;
        }
        final int n = potA.length;
        if (tA.length != n || rA.length != n || potB.length != n || tB.length != n || rB.length != n) {
            return LOI_MANG;
        }
        for (int i = 0; i < n; i++) {
            if (tA[i] < 0 || rA[i] < 0 || tB[i] < 0 || rB[i] < 0) {
                return LOI_SO_AM;
            }
            if (rB[i] != tA[i] || rA[i] != tB[i]) {
                return LOI_LECH_HAI_BEN;
            }
            if (tA[i] == 0 && tB[i] == 0) {
                continue;
            }
            if (i == 0 || !luat.giaoDichDuoc(i)) {
                return LOI_KHONG_GIAO_DICH_DUOC;
            }
            if (tA[i] > potA[i] || tB[i] > potB[i]) {
                return LOI_KHONG_DU;
            }
            if (!luat.khongGioiHan(i)) {
                // Tinh bang long: potions la int, cong them co the tran neu du lieu da hong.
                final long sauA = (long) potA[i] - tA[i] + tB[i];
                final long sauB = (long) potB[i] - tB[i] + tA[i];
                if (sauA > TOI_DA_MOT_O || sauB > TOI_DA_MOT_O) {
                    return LOI_TRAN_O;
                }
            }
        }
        return HOP_LE;
    }

    public static String moTaLoi(final int ma) {
        switch (ma) {
            case HOP_LE:
                return "hop le";
            case LOI_MANG:
                return "mang trang thai hong";
            case LOI_SO_AM:
                return "so luong am";
            case LOI_KHONG_DU:
                return "ben chuyen khong con du thuoc";
            case LOI_LECH_HAI_BEN:
                return "trang thai hai ben khong khop";
            case LOI_KHONG_GIAO_DICH_DUOC:
                return "thuoc khong duoc phep giao dich";
            case LOI_TRAN_O:
                return "ben nhan vuot 999";
            default:
                return "loi " + ma;
        }
    }

    /** Tom tat cac o khac 0 de ghi log, vi du "tA[9]=999 rB[9]=999". Ngan, khong xuong dong. */
    public static String tomTat(final int[] tA, final int[] rA, final int[] tB, final int[] rB) {
        final StringBuilder sb = new StringBuilder();
        noi(sb, "tA", tA);
        noi(sb, "rA", rA);
        noi(sb, "tB", tB);
        noi(sb, "rB", rB);
        return sb.length() == 0 ? "(rong)" : sb.toString().trim();
    }

    private static void noi(final StringBuilder sb, final String ten, final int[] mang) {
        if (mang == null) {
            sb.append(ten).append("=null ");
            return;
        }
        for (int i = 0; i < mang.length; i++) {
            if (mang[i] != 0) {
                sb.append(ten).append('[').append(i).append("]=").append(mang[i]).append(' ');
            }
        }
    }
}
