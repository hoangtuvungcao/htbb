package com.htbb.pokemon;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loi moi ket ban dang cho (goi 101). Truoc 25/10/2026 MapXaHoi.doAddFriend nhan "dong y" (101, ok=1) ma KHONG can loi
 * moi: ai gui 101 + id nhan vat + 1 la ep nguoi kia thanh ban (bo qua tuy chon "khong nhan loi moi"), gui lap lai thi
 * danh sach ban phinh ra trung lap (bot bat_thuong / bt_ket_ban).
 *
 * Gio: buoc moi (ok=0) ghi {nguoi moi, luc} cho nguoi nhan; buoc dong y chi hop le khi co loi moi con han tu dung nguoi
 * do (dung xong thi xoa), va khong them khi hai nguoi da la ban.
 */
public final class LoiMoiKetBan {

    /** Loi moi het han sau 2 phut. */
    public static final long HAN_MS = 120000L;

    /** charDBID nguoi nhan -> {charDBID nguoi moi, luc moi}. */
    private static final ConcurrentHashMap<Integer, long[]> DANG_CHO = new ConcurrentHashMap<Integer, long[]>();

    private LoiMoiKetBan() {
    }

    public static void ghi(final int nguoiNhan, final int nguoiMoi, final long luc) {
        DANG_CHO.put(Integer.valueOf(nguoiNhan), new long[]{nguoiMoi, luc});
    }

    /** Nguoi nhan dong y loi moi cua nguoiMoi: true (va xoa loi moi) neu co loi moi con han tu dung nguoi do. */
    public static boolean dungLoiMoi(final int nguoiNhan, final int nguoiMoi, final long luc) {
        final Integer k = Integer.valueOf(nguoiNhan);
        final long[] v = DANG_CHO.get(k);
        if (v == null || v[0] != nguoiMoi || luc - v[1] > HAN_MS || luc < v[1]) {
            return false;
        }
        return DANG_CHO.remove(k, v);
    }

    public static boolean daLaBan(final List<Integer> ds, final int id) {
        if (ds == null) {
            return false;
        }
        synchronized (ds) {
            for (final Integer i : ds) {
                if (i != null && i.intValue() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Chi test. */
    public static void xoaHet() {
        DANG_CHO.clear();
    }
}
