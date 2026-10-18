package com.htbb.pokemon;

/**
 * THONG SO THIET KE DAI HAN (docs/pokemon/THIET_KE_LAU_DAI.md).
 *
 * FILE SINH RA boi tools/pokemon/sinh_sql_lau_dai.py - KHONG SUA TAY.
 * CHUA NOI vao game (giai doan 2: docs/pokemon/KE_HOACH_LAU_DAI_GD2.md). Hien chi
 * tools/pokemon/MoPhongKinhTe.java va test/KiemLauDai.java doc.
 * KHONG nham voi real.LevelDetail (bang EXP DANG chay).
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

    /** EXP_LEN_CAP[L] = EXP tu L len L+1 (L=1..99); [0] va [100] = 0. */
    public static final long[] EXP_LEN_CAP = {
        0L, 215L, 306L, 443L, 619L, 893L, 1260L, 1810L, 2560L, 3650L,
        5210L, 6510L, 8100L, 10100L, 12600L, 15800L, 19700L, 27500L, 38500L, 53700L,
        75200L, 92300L, 113000L, 139000L, 170000L, 209000L, 255000L, 314000L, 385000L, 471000L,
        578000L, 670000L, 775000L, 899000L, 1040000L, 1210000L, 1400000L, 1620000L, 1880000L, 2180000L,
        2530000L, 2770000L, 3030000L, 3320000L, 3650000L, 4000000L, 4370000L, 4800000L, 5260000L, 5750000L,
        6310000L, 6920000L, 7560000L, 8300000L, 9100000L, 9950000L, 10900000L, 12000000L, 13100000L, 14300000L,
        15700000L, 17400000L, 19100000L, 21000000L, 23200000L, 25600000L, 28100000L, 31000000L, 34200000L, 37600000L,
        41400000L, 46000000L, 50900000L, 56500000L, 62800000L, 69400000L, 77100000L, 85600000L, 95100000L, 105000000L,
        117000000L, 127000000L, 138000000L, 150000000L, 164000000L, 178000000L, 193000000L, 209000000L, 227000000L, 244000000L,
        264000000L, 281000000L, 297000000L, 315000000L, 335000000L, 354000000L, 376000000L, 399000000L, 424000000L, 448000000L,
        0L,
    };

    /** EXP 1 quai cung cap (de xuat rcvxp). */
    public static final int[] MOB_EXP = {
        0, 20, 27, 37, 49, 67, 90, 122, 164, 222,
        300, 335, 374, 417, 466, 520, 580, 647, 722, 806,
        900, 975, 1056, 1144, 1239, 1342, 1453, 1574, 1705, 1847,
        2000, 2149, 2309, 2481, 2665, 2864, 3077, 3306, 3552, 3816,
        4100, 4255, 4416, 4582, 4755, 4935, 5121, 5315, 5516, 5724,
        5940, 6192, 6455, 6729, 7014, 7312, 7622, 7945, 8282, 8634,
        9000, 9407, 9831, 10276, 10740, 11225, 11732, 12262, 12816, 13395,
        14000, 14775, 15594, 16457, 17368, 18330, 19345, 20417, 21547, 22741,
        24000, 24909, 25853, 26833, 27850, 28905, 30000, 30954, 31937, 32953,
        34000, 34967, 35960, 36983, 38034, 39115, 40227, 41371, 42547, 43756,
        45000,
    };

    public static final int[] QUAI_MOI_GIO = {
        0, 359, 357, 356, 355, 354, 352, 351, 350, 348,
        347, 346, 344, 343, 342, 340, 339, 338, 337, 335,
        334, 333, 331, 330, 329, 328, 326, 325, 324, 322,
        321, 320, 318, 317, 316, 314, 313, 312, 311, 309,
        308, 307, 305, 304, 303, 302, 300, 299, 298, 296,
        295, 294, 292, 291, 290, 288, 287, 286, 285, 283,
        282, 281, 279, 278, 277, 276, 274, 273, 272, 270,
        269, 268, 266, 265, 264, 262, 261, 260, 259, 257,
        256, 255, 253, 252, 251, 250, 248, 247, 246, 244,
        243, 242, 240, 239, 238, 236, 235, 234, 233, 231,
        230,
    };

    /** Xu ky vong / quai (roi 30%). */
    public static final int[] XU_MOI_QUAI = {
        0, 10, 11, 12, 12, 13, 13, 14, 15, 16,
        16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
        27, 28, 29, 31, 32, 34, 36, 37, 39, 41,
        43, 45, 48, 50, 53, 55, 58, 61, 64, 67,
        70, 74, 78, 81, 86, 90, 94, 99, 104, 109,
        115, 120, 126, 133, 139, 146, 154, 161, 169, 178,
        187, 196, 206, 216, 227, 238, 250, 263, 276, 290,
        304, 319, 335, 352, 370, 388, 408, 428, 450, 472,
        496, 520, 546, 574, 602, 633, 664, 697, 732, 769,
        807, 848, 890, 935, 981, 1030, 1082, 1136, 1193, 1252,
        1315,
    };

    /** Gio hieu dung tich luy de dat cap L (x1000). */
    public static final int[] GIO_TICH_LUY_PHAN_NGHIN = {
        0, 0, 30, 62, 95, 131, 169, 208, 251, 295,
        342, 392, 449, 512, 582, 662, 751, 851, 976, 1135,
        1333, 1583, 1868, 2191, 2558, 2975, 3449, 3989, 4601, 5298,
        6090, 6990, 7965, 9020, 10164, 11403, 12744, 14198, 15772, 17476,
        19323, 21323, 23444, 25693, 28079, 30609, 33292, 36138, 39156, 42357,
        45751, 49351, 53153, 57166, 61404, 65878, 70603, 75591, 80858, 86419,
        92291, 98491, 105057, 112010, 119374, 127172, 135430, 144176, 153438, 163246,
        173633, 184633, 196251, 208522, 221482, 235169, 249626, 264895, 281022, 298055,
        316044, 335044, 355061, 376149, 398365, 421770, 446428, 472405, 499772, 528604,
        558978, 590978, 624161, 658570, 694251, 731250, 769617, 809401, 850656, 893436,
        937796,
    };

    public static final int TI_LE_ROI_XU = 30;
    public static final int NANG_LUONG_PHUT_NGAY = 180;
    public static final int NANG_LUONG_PHUT_TICH_TOI_DA = 360;
    public static final int HE_SO_BAT_KIP = 150;
    public static final int KHOANG_BAT_KIP = 5;
    public static final int TINH_THONG_PHAN_TRAM_CAP = 50;
    public static final int TINH_THONG_DIEM_MOI_MUA = 10;
    public static final int TINH_THONG_TOI_DA = 50;
    public static final int TINH_THONG_PHAN_NGHIN_MOI_DIEM = 2;
    public static final int SO_O_TRANG_BI = 11;
    public static final int SO_MON_MUA_MOI_BAC = 5;
    public static final int PITY_CHIA = 3;
    public static final int MAT_CAP_TU = 7;
    public static final int GIU_CAP_KHI_DOI_BAC = 2;
    public static final int DA_ROI_PHAN_NGHIN = 15;
    public static final int DA_TU_PHAN_RA = 2;
    public static final int TIEU_HAO_PHAN_TRAM = 12;
    public static final int SUA_DO_PHAN_TRAM = 8;
    public static final int DICH_CHUYEN_LAN_NGAY = 3;
    public static final int DICH_CHUYEN_PHAN_TRAM_GIO = 3;
    public static final int THUE_CHO_PHAN_TRAM = 6;
    public static final int PHI_DANG_BAN_PHAN_TRAM = 1;
    public static final int TRAN_GIAO_DICH_TRUC_TIEP_GIO = 5;
    public static final int GIOI_HAN_NGAY = 3;
    public static final int GIOI_HAN_TUAN = 2;
    public static final int VE_CHUONG_TUAN = 2;
    public static final int VE_CHUONG_TOI_DA = 4;
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
    public static final int GIFTCODE_TOI_DA_THANG = 2;

    /** Bac met: {so phut, % toc do} sau khi het gio 100%. Sau bac cuoi: 0. */
    public static final int[][] BAC_MET = {{180, 50}, {240, 20}};

    public static final int[] LICH_DANG_NHAP = {
        3, 4, 5, 6, 8, 10, 15,
    };

    public static final int[] BAC_TB_CAP = {
        1, 10, 20, 30, 40, 50, 60, 70, 80, 90,
        98,
    };

    public static final int[] BAC_TB_NGAY_MO = {
        0, 0, 0, 0, 0, 0, 0, 60, 150, 365,
        545,
    };

    public static final long[] BAC_TB_GIA = {
        5385L, 8328L, 13527L, 20704L, 32340L, 50888L, 79101L, 122664L, 190464L, 294152L,
        416954L,
    };

    public static final String[] BAC_TB_TEN = {"sơ cấp", "đồng", "bạc", "vàng", "bạch kim", "kim cương", "chuyên gia", "tinh anh", "huyền thoại", "thần thoại", "tối thượng"};

    public static final int[] TI_LE_NC = {
        100, 100, 95, 90, 85, 75, 65, 55, 45, 38, 30, 24, 18, 13, 10,
    };

    public static final int[] DA_NC = {
        1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4,
    };

    public static final int[] BUA_BAO_HO_LUONG_KHOA = {
        0, 0, 0, 0, 0, 0, 10, 10, 10, 10, 25, 25, 25, 25, 25,
    };

    public static final int[] CHI_SO_NC_PHAN_TRAM = {
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5,
    };

    /** CHI_PHI_NC[bac][k] = xu cho 1 lan thu len +k (k=1..15). */
    public static final long[][] CHI_PHI_NC = {
        {0L, 215L, 280L, 364L, 473L, 615L, 800L, 1040L, 1352L, 1757L, 2284L, 2969L, 3860L, 5018L, 6524L, 8481L},
        {0L, 333L, 433L, 563L, 732L, 951L, 1237L, 1608L, 2090L, 2717L, 3533L, 4592L, 5970L, 7761L, 10089L, 13116L},
        {0L, 541L, 703L, 914L, 1189L, 1545L, 2009L, 2612L, 3395L, 4414L, 5738L, 7459L, 9697L, 12606L, 16388L, 21304L},
        {0L, 828L, 1077L, 1400L, 1819L, 2365L, 3075L, 3997L, 5197L, 6756L, 8782L, 11417L, 14842L, 19295L, 25083L, 32608L},
        {0L, 1294L, 1682L, 2186L, 2842L, 3695L, 4803L, 6244L, 8117L, 10552L, 13718L, 17833L, 23183L, 30138L, 39180L, 50934L},
        {0L, 2036L, 2646L, 3440L, 4472L, 5814L, 7558L, 9825L, 12773L, 16604L, 21586L, 28061L, 36480L, 47424L, 61651L, 80146L},
        {0L, 3164L, 4113L, 5347L, 6951L, 9037L, 11748L, 15272L, 19854L, 25810L, 33553L, 43619L, 56705L, 73716L, 95831L, 124580L},
        {0L, 4907L, 6379L, 8292L, 10780L, 14014L, 18218L, 23683L, 30788L, 40024L, 52032L, 67641L, 87933L, 114313L, 148607L, 193190L},
        {0L, 7619L, 9904L, 12875L, 16738L, 21759L, 28287L, 36773L, 47805L, 62147L, 80791L, 105028L, 136537L, 177498L, 230747L, 299971L},
        {0L, 11766L, 15296L, 19885L, 25850L, 33605L, 43687L, 56793L, 73830L, 95980L, 124773L, 162205L, 210867L, 274127L, 356365L, 463275L},
        {0L, 16678L, 21682L, 28186L, 36642L, 47634L, 61925L, 80502L, 104653L, 136049L, 176864L, 229923L, 298899L, 388569L, 505140L, 656682L},
    };

    /** Ngan sach thuong theo loai nhiem vu: {gio EXP x1000, gio xu x1000, luong khoa, luong}. */
    public static final String[] NGAN_SACH_LOAI = {"huong_dan", "chinh", "chinh_moc", "phu", "tuyen", "ngay", "tuan", "pokedex", "doi", "su_pham", "hang_bi_an", "quay_lai", "thanh_tuu", "su_kien", "huyen_thoai", "an"};
    public static final int[][] NGAN_SACH = {{60, 30, 2, 0}, {300, 150, 3, 0}, {450, 250, 10, 0}, {400, 250, 3, 0}, {500, 300, 4, 0}, {100, 50, 4, 0}, {1000, 500, 20, 0}, {250, 100, 5, 0}, {500, 200, 10, 0}, {300, 100, 10, 0}, {200, 100, 5, 0}, {800, 300, 15, 0}, {0, 0, 20, 10}, {0, 0, 0, 0}, {0, 0, 5, 0}, {0, 0, 2, 0}};

    /** Luong theo bac xep hang tuan: {top <=, luong}. */
    public static final int[][] LUONG_XEP_HANG_TUAN = {{1, 50}, {10, 20}, {100, 8}, {1000, 3}};

    public static final int MT_NHE_XONG_CHINH_SOM_NHAT_THANG = 12;
    public static final int MT_NHE_XONG_CHINH_MUON_NHAT_THANG = 18;
    public static final int MT_CAY_XONG_CHINH_SOM_NHAT_THANG = 6;
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
