package com.htbb.pokemon;

/**
 * VE LANG MIEN PHI (chu game duyet 24/10/2026) - luat THUAN tren {@link HoSoLauDai}, khong I/O. Lop noi game:
 * real.VeLangMienPhiGame.
 *
 * <pre>
 *  Luot:     moi ngay lich may chu {@link #K_MOI_NGAY} luot (mac dinh 1, khong cong don sang ngay sau) + luot THEM cat
 *            san: +{@link #K_THANG_GYM} moi lan thang mot tran Gym (lan dau hoac danh lai tuan), giu toi da
 *            {@link #K_TOI_DA} (mac dinh 3). Dung luot ngay truoc, het moi tru luot them.
 *  Luu:      HoSoLauDai (tob_char_lau_dai.du_lieu): l1 = ngay (yyyyMMdd) cua bo dem, l2 = luot ngay da dung hom do,
 *            l3 = luot them dang giu -> dang nhap lai / khoi dong lai may chu khong dat lai.
 *  Dung:     So tay huan luyen -> "Về làng miễn phí (còn N lượt)" -> cung diem den, cung duong di (Map.move2Map) va cung
 *            moi dieu cam + hoi chieu 60 giay CHUNG voi Ve dich chuyen "Về làng". Chi tru khi DA TOI lang.
 *  Qua huy hieu dau: l4 = 1 khi da trao 1 Ve dich chuyen cho huy hieu Gym dau tien (moi nhan vat 1 lan).
 * </pre>
 * Kiem thu: test/KiemVeLangMienPhi.
 */
public final class VeLangMienPhi {

    public static final String TEN = "Về làng miễn phí";

    /** Khoa cau_hinh (nhom tu_dong, chinh tren web, ap dung ngay). */
    public static final String K_MOI_NGAY = "ve_lang.moi_ngay";
    public static final String K_THANG_GYM = "ve_lang.thang_gym";
    public static final String K_TOI_DA = "ve_lang.toi_da";

    public static final int MOI_NGAY = 1;
    public static final int THANG_GYM = 1;
    public static final int TOI_DA = 3;
    /** Tran cung cho gia tri cau hinh (chong so lieu la). */
    public static final int TRAN_CUNG = 99;

    /** Loai luot vua tru (de hoan khi khong toi noi). */
    public static final int KHONG = 0;
    public static final int LUOT_NGAY = 1;
    public static final int LUOT_THEM = 2;

    private VeLangMienPhi() {
    }

    static int kep(final long v, final int macDinh) {
        if (v < 0L) {
            return macDinh;
        }
        return (int) Math.min(TRAN_CUNG, v);
    }

    /** Luot moi ngay (cau hinh, kep 0..99). */
    public static int moiNgay() {
        return kep(com.htbb.cauhinh.CauHinhTrungTam.so(K_MOI_NGAY, MOI_NGAY), MOI_NGAY);
    }

    /** Luot them moi lan thang Gym. */
    public static int thangGym() {
        return kep(com.htbb.cauhinh.CauHinhTrungTam.so(K_THANG_GYM, THANG_GYM), THANG_GYM);
    }

    /** So luot them giu toi da. */
    public static int toiDa() {
        return kep(com.htbb.cauhinh.CauHinhTrungTam.so(K_TOI_DA, TOI_DA), TOI_DA);
    }

    /** Luot ngay con lai hom {@code hom} (yyyyMMdd). */
    public static int conNgay(final HoSoLauDai h, final int hom, final int moiNgay) {
        synchronized (h) {
            final int daDung = h.vlNgay == hom ? h.vlDaDung : 0;
            return Math.max(0, moiNgay - daDung);
        }
    }

    /** Luot them dung duoc (kep theo tran hien tai: admin ha tran thi phan vuot khong dung duoc). */
    public static int conThem(final HoSoLauDai h, final int toiDa) {
        synchronized (h) {
            return Math.max(0, Math.min(h.vlThem, toiDa));
        }
    }

    public static int conLai(final HoSoLauDai h, final int hom, final int moiNgay, final int toiDa) {
        return conNgay(h, hom, moiNgay) + conThem(h, toiDa);
    }

    /** Tru 1 luot (ngay truoc, roi luot them). Tra loai luot da tru, {@link #KHONG} = het luot (khong doi gi). */
    public static int tru(final HoSoLauDai h, final int hom, final int moiNgay, final int toiDa) {
        synchronized (h) {
            if (h.vlNgay != hom) {
                h.vlNgay = hom;
                h.vlDaDung = 0;
                h.ban = true;
            }
            if (h.vlDaDung < moiNgay) {
                h.vlDaDung++;
                h.ban = true;
                return LUOT_NGAY;
            }
            if (Math.min(h.vlThem, toiDa) > 0) {
                h.vlThem = Math.min(h.vlThem, toiDa) - 1;
                h.ban = true;
                return LUOT_THEM;
            }
            return KHONG;
        }
    }

    /** Hoan luot vua {@link #tru} (khong toi noi). */
    public static void hoan(final HoSoLauDai h, final int hom, final int loai) {
        synchronized (h) {
            if (loai == LUOT_NGAY && h.vlNgay == hom && h.vlDaDung > 0) {
                h.vlDaDung--;
                h.ban = true;
            } else if (loai == LUOT_THEM) {
                h.vlThem++;
                h.ban = true;
            }
        }
    }

    /** Thang Gym: cong {@code n} luot them, khong vuot {@code toiDa}. Tra so luot thuc cong. */
    public static int congThangGym(final HoSoLauDai h, final int n, final int toiDa) {
        synchronized (h) {
            if (n <= 0 || h.vlThem >= toiDa) {
                return 0;
            }
            final int moi = Math.min(toiDa, h.vlThem + n);
            final int cong = moi - h.vlThem;
            h.vlThem = moi;
            h.ban = true;
            return cong;
        }
    }

    /** Nhan dong So tay. */
    public static String nhan(final int conLai) {
        return TEN + " (còn " + Math.max(0, conLai) + " lượt)";
    }

    /** Dong nhac trong thong bao thang Gym. */
    public static String nhacThangGym(final int conLai) {
        return TEN + " trong Sổ tay huấn luyện (còn " + Math.max(0, conLai) + " lượt).";
    }

    // ------------------------------------------------------------------ qua huy hieu dau tien

    /** Co huy hieu Gym (bat ky) ma chua nhan ve qua huy hieu dau tien. */
    public static boolean canQuaHuyHieu(final HoSoLauDai h) {
        synchronized (h) {
            return h.gymHuyHieu != 0 && h.vlQuaHuyHieu == 0;
        }
    }
}
