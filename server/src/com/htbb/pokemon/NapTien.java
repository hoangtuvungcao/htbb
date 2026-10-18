package com.htbb.pokemon;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Co {@code sv.nap_tien} - he thong nap tien KHONG lam phat, KHONG tra tien de manh hon
 * (docs/pokemon/NAP_TIEN.md). MOI cho re nhanh cua nap tien doc qua {@link #bat()}.
 *
 * FAIL CLOSED: nap tien chi BAT khi {@code sv.nap_tien=1} VA {@code sv.lau_dai=1}. Ly do:
 * khi tat thiet ke dai han thi game cu van ban do co chi so bang luong (shop dac biet), van
 * roi luong tu quai, van doi luong ra xu o su kien - mo nap luc do la ban suc manh. Thieu
 * mot trong hai co -> {@link #bat()} = false, game y nhu truoc (TeamServer in canh bao).
 *
 * Tat co (mac dinh, thieu dong) = game y het truoc: khong menu "Cua hang", khong luong nao
 * duoc cong, khong luong doi ra gi khac.
 */
public final class NapTien {

    /** Ten khoa trong server.ini. */
    public static final String KHOA = "sv.nap_tien";

    private static volatile boolean coBat = false;

    /** Luong (cao cap) da tieu tu luc khoi dong - moi luong tieu la ho hut (dot). */
    private static final AtomicLong LUONG_TIEU = new AtomicLong();
    /** Luong da cong tu don nap / the thang tu luc khoi dong. */
    private static final AtomicLong LUONG_VAO = new AtomicLong();

    private NapTien() {
    }

    /** Nap tien dang bat khong. Ham DUY NHAT moi cho re nhanh duoc dung. */
    public static boolean bat() {
        return coBat && LauDai.bat();
    }

    /** Co sv.nap_tien da dat (chua xet sv.lau_dai) - de TeamServer bao canh bao. */
    public static boolean coDat() {
        return coBat;
    }

    /** Chi TeamServer (luc doc server.ini) va test duoc goi. */
    public static void dat(final boolean giaTri) {
        coBat = giaTri;
    }

    /** "1" (co the co khoang trang) = bat; moi gia tri khac, ke ca null = tat. */
    public static boolean docGiaTri(final String giaTri) {
        return giaTri != null && "1".equals(giaTri.trim());
    }

    /** Dong in luc khoi dong (TeamServer). */
    public static String moTaTrangThai() {
        if (!coBat) {
            return "NAP TIEN -> TAT";
        }
        if (!LauDai.bat()) {
            return "NAP TIEN -> TAT (CANH BAO: sv.nap_tien=1 nhung sv.lau_dai=0 - nap chi chay khi bat ca"
                    + " thiet ke dai han, xem docs/pokemon/NAP_TIEN.md muc 1)";
        }
        return "NAP TIEN -> BAT (sql/pokemon_nap_tien.sql phai da chay; web KPAH_NAP_BAT=true)";
    }

    // ------------------------------------------------------------------ dem (thong ke)

    /** Char.subLuong goi khi bat: luong bi dot. */
    public static void ghiTieu(final long l) {
        if (l > 0L) {
            LUONG_TIEU.addAndGet(l);
        }
    }

    public static void ghiVao(final long l) {
        if (l > 0L) {
            LUONG_VAO.addAndGet(l);
        }
    }

    public static long luongDaTieu() {
        return LUONG_TIEU.get();
    }

    public static long luongDaVao() {
        return LUONG_VAO.get();
    }
}
