package com.htbb.pokemon;

/**
 * GIOI HAN CHI SO chong lam phat (docs/thiet-ke-game.md). Lop THUAN: khong dung
 * Char/Map/CSDL, de kiem o test/KiemChiSo.java va dung chung voi tools/pokemon/MoPhongChiSo.java.
 *
 * Chi co tac dung khi bat co sv.lau_dai (noi goi kiem LauDai.bat()). Moi gioi han la TRAN CUNG +
 * GIAM DAN (diminishing returns): duoi nguong "goi" giu nguyen, tren goi tang cham dan va KHONG
 * BAO GIO cham tran. Vi vay cong them chi so luon co ich mot chut nhung khong the cong don vo han.
 *
 * Don vi: PHAN NGHIN (1000 = 100%) tru khi ten hang so ghi khac.
 */
public final class GioiHanChiSo {

    private GioiHanChiSo() {
    }

    // ================================================================== tran + goi giam dan

    /** Ti le chi mang (moi nguon: diem chi mang /300, bao kich, do giu, buff). */
    public static final int CHI_MANG_GOI = 250;
    public static final int CHI_MANG_TRAN = 400;
    public static final int CHI_MANG_PVP_GOI = 200;
    public static final int CHI_MANG_PVP_TRAN = 300;

    /**
     * Sat thuong chi mang CONG THEM tren nen x1,5 (SatThuongLauDai.CHI_MANG_PHAN_TRAM). Tran +50% =>
     * don chi mang toi da x2,0, tuc "+100% so voi don thuong". PvP: KHONG cong them (x1,5 phang).
     */
    public static final int ST_CHI_MANG_GOI = 300;
    public static final int ST_CHI_MANG_TRAN = 500;

    /** Ne tranh (ti le ne moi don). */
    public static final int NE_GOI = 200;
    public static final int NE_TRAN = 300;
    public static final int NE_PVP_GOI = 150;
    public static final int NE_PVP_TRAN = 200;

    /** Hut HP theo sat thuong gay ra (tong moi nguon). PvE 10%, PvP 5%. */
    public static final int HUT_GOI = 60;
    public static final int HUT_TRAN = 100;
    public static final int HUT_PVP_GOI = 30;
    public static final int HUT_PVP_TRAN = 50;

    /**
     * Hut HP len noi dung THU THACH (boss, Gym, boss the gioi, quai tinh anh) chi con 10% (tuc &lt;= 1%), va moi don
     * hoi khong qua 0,2% HP toi da (~0,45%/giay): chong "dung yen hut mau" vo han (MoPhongChiSo muc 3).
     */
    public static final int HUT_THU_THACH_PHAN_TRAM = 10;
    public static final int HUT_MOI_DON_TRAN = 2;

    /** Hoi HP theo sat thuong NHAN vao (mat na...). */
    public static final int HOI_KHI_BI_DANH_TRAN = 30;

    /** Giam sat thuong nhan vao (tong moi nguon thu dong: giap, thu cuoi, skin, bao vat...). */
    public static final int GIAM_GOI = 350;
    public static final int GIAM_TRAN = 500;
    public static final int GIAM_PVP_GOI = 300;
    public static final int GIAM_PVP_TRAN = 400;

    /** Khang theo MOT he (khang he do + khang tat ca). */
    public static final int KHANG_HE_GOI = 400;
    public static final int KHANG_HE_TRAN = 600;

    /**
     * SAN SAT THUONG: sau MOI lop giam o phia bi danh (khang he, giam ST, chan, ne theo he, X Phong thu...)
     * mot don van con it nhat 40% luong truoc khi giam (tong giam moi lop &lt;= 60% = tran khang mot he). Chong
     * bat tu khi cac lop nhan voi nhau.
     */
    public static final int SAN_SAT_THUONG = 400;

    /** Phan don (phan sat thuong nhan vao tra lai ke danh). */
    public static final int PHAN_DON_TRAN = 150;
    public static final int PHAN_DON_PVP_TRAN = 100;

    /** Toc danh cong them (+50% = hoi chieu con 1/1,5). Than khi luu "% giam hoi chieu". */
    public static final int TOC_DANH_TRAN = 500;
    /** Toc chay cong them (+30%). Toc co ban nhan vat = 5 o/nhip. */
    public static final int TOC_CHAY_TRAN = 300;
    public static final int TOC_CHAY_CO_BAN = 5;

    /** Sat thuong cong them len boss / len quai thuong. */
    public static final int ST_BOSS_TRAN = 200;
    public static final int ST_QUAI_TRAN = 200;

    /** Sat thuong theo % HP toi da muc tieu moi don (sat thuong chuan, bao vat, skin): thuong 1%, boss 0,2%. */
    public static final int ST_THEO_HP_TRAN = 10;
    public static final int ST_THEO_HP_BOSS_TRAN = 2;

    /** Xuyen giap / bo qua thu (tong moi nguon). */
    public static final int XUYEN_GOI = 200;
    public static final int XUYEN_TRAN = 300;

    /** Buff tu trang bi + pet + thoi trang (%, KHONG tinh ve EXP / su kien - CapDoLauDai da kep tong +100%). */
    public static final int EXP_TRANG_BI_TRAN_PHAN_TRAM = 30;
    public static final int ROI_DO_TRAN_PHAN_TRAM = 20;
    public static final int ROI_XU_TRAN_PHAN_TRAM = 10;

    /** Hoi HP/PP moi nhip hoi (5 giay): <= 1% luong toi da. */
    public static final int HOI_NHIP_TRAN = 10;

    /** Gay trang thai tu trang bi (cong vao ti le cua chieu): PvE 15%, PvP 7,5%. Khang trang thai <= 50%. */
    public static final int GAY_TT_GOI = 100;
    public static final int GAY_TT_TRAN = 150;
    public static final int KHANG_TT_GOI = 300;
    public static final int KHANG_TT_TRAN = 500;

    /**
     * Tong % cong them (nhan) vao tan cong / phong thu / HP tu MOI nguon ngoai do (pet, thu cuoi, thoi
     * trang, bo +15, VIP, chuc vu, skin, bao vat %...). Khong tinh: giai doan tien hoa, tinh thong, he
     * sinh thai, vat pham tieu hao co han (da co tran rieng trong thiet ke).
     */
    public static final int PHAN_TRAM_CONG_TRAN = 500;

    /** Thuong bo (+15 du bo cu, du bo thu cuoi): <= 5% moi chi so (THIET_KE 3.4: +5% HP, +5% tan cong). */
    public static final int THUONG_BO_TRAN_PHAN_TRAM = 5;

    // ================================================================== ham giam dan

    /**
     * Giam dan hyperbol: x <= goi -> x; tren goi: goi + du * (tran - goi) / (du + tran - goi).
     * Don dieu tang, lien tuc, doc = 1 tai goi va LUON &lt; tran (x huu han). x &lt; 0 -> 0.
     */
    public static int giamDan(final long x, final int goi, final int tran) {
        if (x <= 0L) {
            return 0;
        }
        if (x <= goi) {
            return (int) x;
        }
        final long du = x - goi;
        final long phong = Math.max(1, tran - goi);
        return (int) (goi + du * phong / (du + phong));
    }

    /** Chi mang (phan nghin) sau giam dan. */
    public static int chiMang(final long tho, final boolean pvp) {
        return pvp ? giamDan(tho, CHI_MANG_PVP_GOI, CHI_MANG_PVP_TRAN) : giamDan(tho, CHI_MANG_GOI, CHI_MANG_TRAN);
    }

    /**
     * Diem chi mang cu (thang 300: 3 diem = 1%) + bao kich (phan nghin) -> phan nghin THO (chua giam dan).
     */
    public static long chiMangTho(final int diemChiMang, final int baoKichPhanNghin) {
        return Math.max(0, diemChiMang) * 1000L / 300L + Math.max(0, baoKichPhanNghin);
    }

    /**
     * Ti le GIU mot don chi mang PvE khi danh nguoi (phan nghin): don da quay chi mang PvE voi
     * chiMang(tho,false) thi giu lai voi ti le nay -> ti le that = chiMang(tho,true).
     */
    public static int giuChiMangPvp(final long tho) {
        final int pve = chiMang(tho, false);
        if (pve <= 0) {
            return 0;
        }
        return (int) Math.min(1000L, chiMang(tho, true) * 1000L / pve);
    }

    /** Sat thuong chi mang cong them (phan nghin tren nen x1,5) sau giam dan. */
    public static int stChiMangThem(final long tho) {
        return giamDan(tho, ST_CHI_MANG_GOI, ST_CHI_MANG_TRAN);
    }

    /** He so don chi mang PvE (phan tram): 150 + them/10. Tran 200. */
    public static int heSoChiMangPhanTram(final long thoThem) {
        return SatThuongLauDai.CHI_MANG_PHAN_TRAM + stChiMangThem(thoThem) / 10;
    }

    public static int neTranh(final long tho, final boolean pvp) {
        return pvp ? giamDan(tho, NE_PVP_GOI, NE_PVP_TRAN) : giamDan(tho, NE_GOI, NE_TRAN);
    }

    public static int hut(final long tho, final boolean pvp) {
        return pvp ? giamDan(tho, HUT_PVP_GOI, HUT_PVP_TRAN) : giamDan(tho, HUT_GOI, HUT_TRAN);
    }

    public static int giamSatThuong(final long tho, final boolean pvp) {
        return pvp ? giamDan(tho, GIAM_PVP_GOI, GIAM_PVP_TRAN) : giamDan(tho, GIAM_GOI, GIAM_TRAN);
    }

    public static int khangHe(final long tho) {
        return giamDan(tho, KHANG_HE_GOI, KHANG_HE_TRAN);
    }

    public static int xuyenGiap(final long tho) {
        return giamDan(tho, XUYEN_GOI, XUYEN_TRAN);
    }

    public static int gayTrangThai(final long tho, final boolean pvp) {
        final int v = giamDan(tho, GAY_TT_GOI, GAY_TT_TRAN);
        return pvp ? v / 2 : v;
    }

    public static int khangTrangThai(final long tho) {
        return giamDan(tho, KHANG_TT_GOI, KHANG_TT_TRAN);
    }

    /** Tran cung (khong giam dan) - cho cac dong vot it nguon. */
    public static int tran(final long tho, final int tran) {
        return (int) Math.max(0L, Math.min(tran, tho));
    }

    public static int phanDon(final long tho, final boolean pvp) {
        return tran(tho, pvp ? PHAN_DON_PVP_TRAN : PHAN_DON_TRAN);
    }

    /** Toc danh: tu "% giam hoi chieu" tho (than khi, 0..100) -> % giam hoi chieu sau tran (toc danh <= +50%). */
    public static int giamHoiChieuPhanTram(final int phanTramTho) {
        // giam hoi chieu g% <=> toc danh +g/(100-g). Tran toc danh +50% <=> g <= 33%.
        final int gToiDa = 100 * TOC_DANH_TRAN / (1000 + TOC_DANH_TRAN); // 33
        return Math.max(0, Math.min(gToiDa, phanTramTho));
    }

    /** Toc chay cong them (o/nhip) toi da so voi toc co ban. */
    public static int tocChayThemToiDa(final int tocCoBan) {
        return Math.max(0, tocCoBan) * TOC_CHAY_TRAN / 1000;
    }

    /** Sat thuong theo % HP muc tieu (phan nghin tho) -> luong cong them, kep tran va &lt;= don goc. */
    public static long stTheoHp(final long hpToiDaMucTieu, final int phanNghinTho, final boolean boss, final long donGoc) {
        if (hpToiDaMucTieu <= 0L || phanNghinTho <= 0) {
            return 0L;
        }
        final int pn = Math.min(phanNghinTho, boss ? ST_THEO_HP_BOSS_TRAN : ST_THEO_HP_TRAN);
        return Math.min(Math.max(0L, donGoc), hpToiDaMucTieu * pn / 1000L);
    }

    /** % EXP / roi do / roi xu tu trang bi, pet, thoi trang. */
    public static int expTrangBi(final int phanTramTho) {
        return tran(phanTramTho, EXP_TRANG_BI_TRAN_PHAN_TRAM);
    }

    public static int roiDo(final int phanTramTho) {
        return tran(phanTramTho, ROI_DO_TRAN_PHAN_TRAM);
    }

    public static int roiXu(final int phanTramTho) {
        return tran(phanTramTho, ROI_XU_TRAN_PHAN_TRAM);
    }

    /** Luong hoi moi nhip, kep 1% luong toi da (toi thieu 1 neu tho &gt; 0). */
    public static int hoiNhip(final int tho, final int toiDa) {
        if (tho <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(tho, (int) ((long) Math.max(0, toiDa) * HOI_NHIP_TRAN / 1000L)));
    }

    /** Thuong bo % (legacy +8/+12) -> kep tran thiet ke. */
    public static int thuongBo(final int phanTramCu) {
        return tran(phanTramCu, THUONG_BO_TRAN_PHAN_TRAM);
    }

    /** Gia tri sau khi kep tong % cong them: {@code moi <= goc * (1 + PHAN_TRAM_CONG_TRAN)}. */
    public static int kepPhanTramCong(final long moi, final long goc) {
        if (goc <= 0L) {
            return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, moi));
        }
        final long tran = goc * (1000L + PHAN_TRAM_CONG_TRAN) / 1000L;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, Math.min(moi, tran)));
    }

    // ================================================================== san sat thuong (chong bat tu)

    /**
     * Mot lop giam {@code phanNghin} tren don con {@code conLai} (phan nghin cua luong ban dau). Tra lai
     * phan nghin giam DUOC PHEP de luong con lai khong xuong duoi SAN_SAT_THUONG.
     */
    public static int giamDuocPhep(final int conLai, final int phanNghin) {
        if (phanNghin <= 0 || conLai <= 0) {
            return 0;
        }
        if (conLai <= SAN_SAT_THUONG) {
            return 0;
        }
        // (conLai * (1000 - g) / 1000) >= SAN  <=>  g <= 1000 - SAN*1000/conLai
        final int toiDa = 1000 - (int) ((long) SAN_SAT_THUONG * 1000L / conLai);
        return Math.max(0, Math.min(phanNghin, toiDa));
    }

    /** Luong HP hut duoc tu mot don (da kep: thu thach x10%, &lt;= 0,2% HP toi da moi don). */
    public static long hutMotDon(final long damThat, final int phanNghinHut, final boolean thuThach, final long hpToiDa) {
        if (damThat <= 0L || phanNghinHut <= 0) {
            return 0L;
        }
        long h = damThat * phanNghinHut / 1000L;
        if (thuThach) {
            h = h * HUT_THU_THACH_PHAN_TRAM / 100L;
        }
        return Math.min(h, Math.max(1L, Math.max(0L, hpToiDa) * HUT_MOI_DON_TRAN / 1000L));
    }

    // ================================================================== PvP

    /** PvP: thoi luong trang thai x0,5 (TrangThaiPokemon), sat thuong x0,3 + tran 8% HP (SatThuongLauDai). */
    public static final int PVP_HUT_TREN_SAT_THUONG_THAT = SatThuongLauDai.PVP_PHAN_TRAM;

    /**
     * Hut HP trong PvP tinh tren sat thuong THAT (da x0,30), khong tren sat thuong PvE truoc khi doi.
     * Ban cu hut tren so truoc doi -> hut gap ~3,3 lan con so tren mo ta.
     */
    public static long satThuongThatPvp(final long damTruocDoi) {
        return Math.max(0L, damTruocDoi) * PVP_HUT_TREN_SAT_THUONG_THAT / 100L;
    }
}
