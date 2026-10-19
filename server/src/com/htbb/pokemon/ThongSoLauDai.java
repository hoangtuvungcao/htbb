package com.htbb.pokemon;

/**
 * THONG SO THIET KE DAI HAN (docs/thiet-ke-game.md).
 *
 * FILE SINH RA boi tools/pokemon/sinh_sql_lau_dai.py - KHONG SUA TAY.
 * Hai loai so:
 *   - HANG (static final): so thiet ke CHUA co bang SQL (tran cap theo mua, ngan sach nhiem vu, muc tieu
 *     mo phong...). Sinh tu bang trong tools/pokemon/sinh_sql_lau_dai.py.
 *   - NAP TU SQL (static volatile, RONG cho toi khi nap): so co bang data_cap_do_lau_dai /
 *     data_nang_cap_lau_dai / data_kinh_te_lau_dai - bang la NGUON DUY NHAT. com.htbb.dulieu.DuLieuSql nap
 *     luc khoi dong (data.DatabaseDuLieuSql, thieu bang = server tu choi chay) hoac tu sql/htbb_day_du.sql
 *     (test, cong cu mo phong). KHONG gan truong nay ngoai DuLieuSql.
 * KHONG nham voi real.LevelDetail (bang EXP game goc).
 */
public final class ThongSoLauDai {

    private ThongSoLauDai() {
    }

    public static final int CAP_TOI_DA = 100;

    public static final int[] TRAN_CAP_NGAY = {
        0, 60, 150, 240, 365, 545,
    };

    public static final int[] TRAN_CAP_GIA_TRI = {
        60, 70, 80, 91, 95, 100,
    };

    public static final int HE_SO_BAT_KIP = 150;
    public static final int KHOANG_BAT_KIP = 5;
    public static final int TINH_THONG_PHAN_TRAM_CAP = 50;
    public static final int TINH_THONG_DIEM_MOI_MUA = 10;
    public static final int TINH_THONG_TOI_DA = 50;
    public static final int TINH_THONG_PHAN_NGHIN_MOI_DIEM = 2;
    public static final int SO_O_TRANG_BI = 11;
    public static final int SO_MON_MUA_MOI_BAC = 5;
    public static final int PITY_CHIA = 3;
    public static final int GIU_CAP_KHI_DOI_BAC = 2;
    public static final int DA_TU_PHAN_RA = 2;
    public static final int DICH_CHUYEN_LAN_NGAY = 3;
    public static final int DICH_CHUYEN_PHAN_TRAM_GIO = 3;
    public static final int TRAN_GIAO_DICH_TRUC_TIEP_GIO = 5;
    public static final int GIOI_HAN_NGAY = 3;
    public static final int GIOI_HAN_TUAN = 2;
    public static final int VE_CHUONG_BAN_DAU = 4;
    public static final int GIO_MOI_CHUONG_PHAN_NGHIN = 600;
    public static final int GIO_MOI_PHU_PHAN_NGHIN = 250;
    public static final int SU_KIEN_PHAN_XU = 25;
    public static final int CAP_TAN_THU = 16;
    public static final int HE_SO_TAN_THU_PHAN_TRAM = 300;
    public static final int NGOC_HE_CAP_TOI_DA = 10;
    public static final int NGOC_HE_GIA_GIO_PHAN_TRAM = 100;
    public static final int NGOC_HE_TANG_PHAN_TRAM = 145;
    public static final int TRANG_TRI_PHAN_NGHIN_NGAY = 20;
    public static final int TRANG_TRI_NGUONG_GIO = 20;

    public static final int[] LICH_DANG_NHAP = {
        3, 4, 5, 6, 8, 10, 15,
    };

    public static final int[] BAC_TB_NGAY_MO = {
        0, 0, 0, 0, 0, 0, 0, 60, 150, 365,
        545,
    };

    public static final long[] BAC_TB_GIA = {
        5385L, 8328L, 13527L, 20704L, 32340L, 50888L, 79101L, 122664L, 190464L, 294152L,
        416954L,
    };

    /** Ngan sach thuong theo loai nhiem vu: {gio EXP x1000, gio xu x1000, luong khoa, luong}. */
    public static final String[] NGAN_SACH_LOAI = {"huong_dan", "chinh", "chinh_moc", "phu", "tuyen", "ngay", "tuan", "pokedex", "doi", "su_pham", "hang_bi_an", "quay_lai", "thanh_tuu", "su_kien", "huyen_thoai", "an"};
    public static final int[][] NGAN_SACH = {{60, 30, 2, 0}, {300, 150, 3, 0}, {450, 250, 10, 0}, {400, 250, 3, 0}, {500, 300, 4, 0}, {100, 50, 4, 0}, {1000, 500, 20, 0}, {250, 100, 5, 0}, {500, 200, 10, 0}, {300, 100, 10, 0}, {200, 100, 5, 0}, {800, 300, 15, 0}, {0, 0, 20, 10}, {0, 0, 0, 0}, {0, 0, 5, 0}, {0, 0, 2, 0}};

    /** Luong theo bac xep hang tuan: {top <=, luong}. */
    public static final int[][] LUONG_XEP_HANG_TUAN = {{1, 50}, {10, 20}, {100, 8}, {1000, 3}};

    public static final int MT_NHE_XONG_CHINH_SOM_NHAT_THANG = 9;
    public static final int MT_NHE_XONG_CHINH_MUON_NHAT_THANG = 14;
    public static final int MT_CAY_XONG_CHINH_SOM_NHAT_THANG = 5;
    public static final int MT_NHE_CAP_TOI_DA_THANG_MIN = 15;
    public static final int MT_NHE_CAP_TOI_DA_THANG_MAX = 18;
    public static final int MT_CAY_CAP_TOI_DA_THANG_MIN = 9;
    public static final int MT_CAY_CAP_TOI_DA_THANG_MAX = 12;
    public static final int MT_TANG_TOC_NHANH_HON_TOI_DA_PHAN_TRAM = 40;
    public static final int MT_LUONG_DOT_TOI_THIEU_PHAN_TRAM = 95;
    public static final int MT_TANG_TIEN_THUC_THANG_TOI_DA_PHAN_TRAM = 5;
    public static final int MT_XU_THUC_TOI_DA_GIO = 20;
    public static final int MT_SINK_FAUCET_MIN_PHAN_TRAM = 80;
    public static final int MT_SINK_FAUCET_MAX_PHAN_TRAM = 105;
    public static final int MT_XU_GIU_GIO_THU_NHAP_TOI_DA = 60;
    public static final int MT_D1_TOI_THIEU = 40;
    public static final int MT_D7_TOI_THIEU = 20;
    public static final int MT_D30_TOI_THIEU = 10;
    public static final int MT_TIEN_HOA_DAU_GIO_TOI_DA_PHAN_TRAM = 120;
    public static final int MT_MUC_NANG_CAP_NHE = 12;
    public static final int MT_MUC_NANG_CAP_VUA = 14;
    public static final int MT_MUC_NANG_CAP_CAY = 15;

    // ---- NAP TU SQL (com.htbb.dulieu.DuLieuSql) - rong / 0 cho toi khi nap ----

    /** data_cap_do_lau_dai.exp_len_cap: EXP tu L len L+1 (L=1..99); [0] va [100] = 0. */
    public static volatile long[] EXP_LEN_CAP = new long[0];
    /** data_cap_do_lau_dai.mob_exp: EXP 1 quai cung cap (de xuat rcvxp). */
    public static volatile int[] MOB_EXP = new int[0];
    /** data_cap_do_lau_dai.quai_moi_gio. */
    public static volatile int[] QUAI_MOI_GIO = new int[0];
    /** data_cap_do_lau_dai.xu_moi_quai: xu ky vong / quai. */
    public static volatile int[] XU_MOI_QUAI = new int[0];
    /** data_cap_do_lau_dai.gio_tich_luy_phan_nghin: gio hieu dung tich luy de dat cap L (x1000). */
    public static volatile int[] GIO_TICH_LUY_PHAN_NGHIN = new int[0];
    /** data_nang_cap_lau_dai.cap_bac theo bac. */
    public static volatile int[] BAC_TB_CAP = new int[0];
    /** data_nang_cap_lau_dai.ten_bac theo bac. */
    public static volatile String[] BAC_TB_TEN = new String[0];
    /** data_nang_cap_lau_dai.ti_le: [k-1] = % len +k. */
    public static volatile int[] TI_LE_NC = new int[0];
    /** data_nang_cap_lau_dai.da. */
    public static volatile int[] DA_NC = new int[0];
    /** data_nang_cap_lau_dai.bua_luong_khoa. */
    public static volatile int[] BUA_BAO_HO_LUONG_KHOA = new int[0];
    /** data_nang_cap_lau_dai.chi_so_phan_tram (bang luu luy ke; day la phan them moi cap). */
    public static volatile int[] CHI_SO_NC_PHAN_TRAM = new int[0];
    /** data_nang_cap_lau_dai.xu: CHI_PHI_NC[bac][k] = xu cho 1 lan thu len +k (k=1..15), [bac][0] = 0. */
    public static volatile long[][] CHI_PHI_NC = new long[0][];
    /** data_kinh_te_lau_dai exp.bac_met: {so phut, % toc do} sau khi het gio 100%. Sau bac cuoi: 0. */
    public static volatile int[][] BAC_MET = new int[0][];

    /** data_nang_cap_lau_dai.mat_cap: truot tu +MAT_CAP_TU tro len thi tut 1 cap. */
    public static volatile int MAT_CAP_TU;
    /** data_kinh_te_lau_dai roi.xu.phan_tram. */
    public static volatile int TI_LE_ROI_XU;
    /** data_kinh_te_lau_dai roi.da_nang_cap.phan_nghin. */
    public static volatile int DA_ROI_PHAN_NGHIN;
    /** data_kinh_te_lau_dai exp.nang_luong_phut. */
    public static volatile int NANG_LUONG_PHUT_NGAY;
    /** data_kinh_te_lau_dai exp.nang_luong_tich_phut. */
    public static volatile int NANG_LUONG_PHUT_TICH_TOI_DA;
    /** data_kinh_te_lau_dai thue.cho. */
    public static volatile int THUE_CHO_PHAN_TRAM;
    /** data_kinh_te_lau_dai thue.dang_ban. */
    public static volatile int PHI_DANG_BAN_PHAN_TRAM;
    /** data_kinh_te_lau_dai tieu_hao.thuoc_phan_tram_thu_nhap. */
    public static volatile int TIEU_HAO_PHAN_TRAM;
    /** data_kinh_te_lau_dai tieu_hao.sua_do_phan_tram_thu_nhap. */
    public static volatile int SUA_DO_PHAN_TRAM;
    /** data_kinh_te_lau_dai giftcode.toi_da_thang. */
    public static volatile int GIFTCODE_TOI_DA_THANG;
    /** data_kinh_te_lau_dai ve_chuong.tuan. */
    public static volatile int VE_CHUONG_TUAN;
    /** data_kinh_te_lau_dai ve_chuong.toi_da. */
    public static volatile int VE_CHUONG_TOI_DA;

    /** EXP 1 gio danh quai o cap L (100%). */
    public static long expMoiGio(final int L) {
        return (long) QUAI_MOI_GIO[L] * MOB_EXP[L];
    }

    /** Xu 1 gio danh quai o cap L (100%). */
    public static long xuMoiGio(final int L) {
        return (long) QUAI_MOI_GIO[L] * XU_MOI_QUAI[L];
    }

    /** Tran cap tai ngay {@code ngay} ke tu khai truong. */
    public static int tranCap(final int ngay) {
        int t = TRAN_CAP_GIA_TRI[0];
        for (int i = 0; i < TRAN_CAP_NGAY.length; i++) {
            if (ngay >= TRAN_CAP_NGAY[i]) {
                t = TRAN_CAP_GIA_TRI[i];
            }
        }
        return t;
    }

    /** Ti le thanh cong (%) len +k sau {@code truot} lan truot lien tiep (pity). */
    public static int tiLeNangCap(final int k, final int truot) {
        final int goc = TI_LE_NC[k - 1];
        final int them = (goc + PITY_CHIA - 1) / PITY_CHIA;
        return Math.min(100, goc + truot * them);
    }

    /** He so toc do (%) cho phut thu {@code phut} trong ngay, sau khi da dung het {@code phut100} phut 100%. */
    public static int heSoMet(final int phutSau100) {
        int moc = 0;
        for (final int[] b : BAC_MET) {
            moc += b[0];
            if (phutSau100 < moc) {
                return b[1];
            }
        }
        return 0;
    }
}
