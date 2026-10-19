package com.htbb.mang;

/**
 * CAMERA CO DINH (quyet dinh chu game - chong loi the man to): moi client thay cung mot vung the gioi
 * (giao dien logic 640x360, the gioi phong 2 -> 320x180 px the gioi). Client gui be rong man (Canvas.w) trong
 * goi dang nhap (LoginHandler: session.w); server dung no tinh tam them / bo quai (Char.setSizeScreen:
 * rangeAddMonster = w/2 + 50). Client sua doi gui w lon (vd 32000) se nhan quai / nhan vat ca ban do ->
 * KEP o server ve be rong logic co dinh 640 (client chuan gui dung 640 nen khong doi gi).
 */
public final class VungNhinCoDinh {
    /** be rong giao dien logic co dinh cua moi client (PC / Android / J2ME moi) */
    public static final short RONG_TOI_DA = 640;

    private VungNhinCoDinh() {
    }

    /** Kep be rong man client gui len: am / 0 -> 320 (mac dinh cu), lon hon 640 -> 640. */
    public static short kepRong(final int w) {
        if (w <= 0) {
            return 320;
        }
        return (short) Math.min(w, RONG_TOI_DA);
    }
}
