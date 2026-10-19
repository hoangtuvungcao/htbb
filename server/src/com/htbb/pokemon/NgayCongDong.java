package com.htbb.pokemon;

import java.util.Calendar;

/**
 * NGAY CONG DONG hang thang (de xuat 8, chu game duyet 19/10/2026; kieu Pokemon GO Community Day). Lop THUAN -
 * test/KiemNgayCongDong. Ban noi game: NgayCongDongGame. Du lieu: data_ngay_cong_dong (sql/them_thoi_tiet_mua.sql).
 *
 * <h3>Lich (tu dong theo lich nam, khong admin bat tay)</h3>
 * MOT ngay moi thang: Chu nhat cua "tuan cong dong" ISO (SuKienLauDai.LICH ma moi:congdong - tuan 3, 12, 19, 25, 31, 40,
 * 50, de chuoi nhiem vu tuan cong dong luon co ngay cong dong trong tuan) neu Chu nhat do roi vao thang; cac thang con
 * lai: Chu nhat thu ba. Cua so lich 04:00 Chu nhat -> 04:00 thu Hai (LichNam.GIO_CHUYEN, bat / tat o lan bao tri
 * hang ngay, KHONG them lan khoi dong lai), web tu dang tin "Sap / Dang / Da ket thuc" (web/server/plugins/tin-su-kien.ts).
 *
 * <h3>Trong ngay</h3>
 * "3 gio vang" {@link #GIO_VANG_MAC_DINH}:00-17:00: tren cac ban do co mot loai cua DONG noi bat (ban do tu nhien cua
 * loai trong data_hst_bung_phat, hoac cot map_ids), {@code ti_le}% lan hoi sinh doi thanh loai cua dong tren ban do do
 * (tran {@link #TRAN_TI_LE_PHAN_TRAM}%); ha loai noi bat +{@code exp}% EXP (tran {@link #TRAN_EXP_PHAN_TRAM}%, cong vao
 * buff EXP chung - van chiu tran +100% cua CapDoLauDai); shiny x5 (SuKienLauDai.nhanShiny) chi trong gio vang; danh
 * hieu cua dong khi ha du {@code so_ha} con trong gio vang. Khong them xu, khong them luong: chi thoi gian + hinh +
 * danh hieu (ngan sach o 1%), nen khong lam phat.
 */
public final class NgayCongDong {

    private NgayCongDong() {
    }

    public static final String MA_SU_KIEN = "moi:congdong";
    /** Gio vang mac dinh (gio Viet Nam) va so gio. */
    public static final int GIO_VANG_MAC_DINH = 14;
    public static final int SO_GIO_MAC_DINH = 3;
    /** Tran cung (du CSDL ghi gi). */
    public static final int TRAN_SO_GIO = 4;
    public static final int TRAN_TI_LE_PHAN_TRAM = 50;
    public static final int TRAN_EXP_PHAN_TRAM = 25;
    public static final int NHAN_SHINY = 5;

    // ------------------------------------------------------------------ lich

    /** Ngay {y, m, d} cua ngay cong dong thang {@code thang} nam {@code nam}. */
    public static int[] ngay(final int nam, final int thang) {
        for (final Object[] r : SuKienLauDai.LICH) {
            if (!(String.valueOf(r[2]) + ",").contains(MA_SU_KIEN + ",")) {
                continue;
            }
            final Calendar c = Calendar.getInstance(LichNam.MUI_GIO);
            c.setTimeInMillis(LichNam.thuHaiTuanIso(nam, ((Number) r[0]).intValue()));
            c.add(Calendar.DAY_OF_MONTH, 6);
            if (c.get(Calendar.YEAR) == nam && c.get(Calendar.MONTH) + 1 == thang) {
                return new int[]{nam, thang, c.get(Calendar.DAY_OF_MONTH)};
            }
        }
        final Calendar c = Calendar.getInstance(LichNam.MUI_GIO);
        c.clear();
        c.set(nam, thang - 1, 1, 12, 0, 0);
        int cn = 0;
        while (true) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && ++cn == 3) {
                return new int[]{nam, thang, c.get(Calendar.DAY_OF_MONTH)};
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    /** {mo, dong} ms cua cua so lich (04:00 -> 04:00 hom sau). */
    public static long[] cuaSo(final int nam, final int thang) {
        final int[] d = ngay(nam, thang);
        return new long[]{LichNam.moc(d[0], d[1], d[2], 0), LichNam.moc(d[0], d[1], d[2], 1)};
    }

    /** Nam xoay (0 / 1): nam chan / le xoay dong noi bat (THIET_KE "nam 2 xoay vong"). */
    public static int namXoay(final int nam) {
        return Math.floorMod(nam - 2026, 2);
    }

    // ------------------------------------------------------------------ dong du lieu

    /** Mot dong data_ngay_cong_dong. */
    public static final class Dong {
        public final int thang;
        public final int namXoay;
        /** so dong (1..) dang chuoi - tham_so bo dem danh hieu "cong_dong" / "tap:cong_dong" (tap chi dem so). */
        public final String ma;
        public final String ten;
        /** id template quai cua dong (moi loai mot ban do tu nhien). */
        public final int[] idQuai;
        /** ban do them (rong = chi ban do tu nhien). */
        public final int[] mapThem;
        public final int gioBatDau;
        public final int soGio;
        public final int tiLe;
        public final int exp;
        public final int soHa;
        public final int idDanhHieu;
        public final String chieu;

        public Dong(final int thang, final int namXoay, final String ma, final String ten, final int[] idQuai, final int[] mapThem,
                final int gioBatDau, final int soGio, final int tiLe, final int exp, final int soHa, final int idDanhHieu,
                final String chieu) {
            this.thang = thang;
            this.namXoay = namXoay;
            this.ma = ma == null ? "" : ma;
            this.ten = ten == null ? "" : ten;
            this.idQuai = idQuai == null ? new int[0] : idQuai;
            this.mapThem = mapThem == null ? new int[0] : mapThem;
            this.gioBatDau = Math.max(0, Math.min(23 - Math.min(TRAN_SO_GIO, soGio) + 1, gioBatDau));
            this.soGio = Math.max(1, Math.min(TRAN_SO_GIO, soGio));
            this.tiLe = Math.max(0, Math.min(TRAN_TI_LE_PHAN_TRAM, tiLe));
            this.exp = Math.max(0, Math.min(TRAN_EXP_PHAN_TRAM, exp));
            this.soHa = Math.max(1, soHa);
            this.idDanhHieu = idDanhHieu;
            this.chieu = chieu == null ? "" : chieu;
        }

        public boolean coQuai(final int idTemplate) {
            for (final int q : idQuai) {
                if (q == idTemplate) {
                    return true;
                }
            }
            return false;
        }

        public boolean trongGioVang(final int gio) {
            return gio >= gioBatDau && gio < gioBatDau + soGio;
        }

        /** "14:00-17:00". */
        public String khungGio() {
            return hai(gioBatDau) + ":00–" + hai(gioBatDau + soGio) + ":00";
        }

        static String hai(final int g) {
            return g < 10 ? "0" + g : String.valueOf(g);
        }
    }

    /** Dong cua (thang, nam): khop thang + nam xoay, thieu nam xoay thi lay dong nam xoay 0, khong co -> null. */
    public static Dong chon(final Dong[] ds, final int nam, final int thang) {
        if (ds == null) {
            return null;
        }
        final int x = namXoay(nam);
        Dong du = null;
        for (final Dong d : ds) {
            if (d == null || d.thang != thang) {
                continue;
            }
            if (d.namXoay == x) {
                return d;
            }
            if (d.namXoay == 0) {
                du = d;
            }
        }
        return du;
    }

    // ------------------------------------------------------------------ trong gio vang

    /**
     * Loai cua dong tren ban do {@code mapId} (theo cap tu nhien {map, idQuai} cua data_hst_bung_phat), -1 = ban do
     * khong thuoc ngay cong dong. Ban do them (map_ids) dung loai dau dong.
     */
    public static int loaiTrenMap(final Dong d, final int mapId, final java.util.List<int[]> tuNhien) {
        if (d == null) {
            return -1;
        }
        if (tuNhien != null) {
            for (final int[] c : tuNhien) {
                if (c != null && c.length >= 2 && c[0] == mapId && d.coQuai(c[1])) {
                    return c[1];
                }
            }
        }
        for (final int m : d.mapThem) {
            if (m == mapId) {
                return d.idQuai.length > 0 ? d.idQuai[0] : -1;
            }
        }
        return -1;
    }

    /** Hoi sinh co doi thanh loai noi bat: {@code r100} = so ngau nhien 0..99. */
    public static boolean doiThanhNoiBat(final Dong d, final int idGoc, final int idNoiBat, final int r100) {
        return d != null && idNoiBat > 0 && idGoc != idNoiBat && r100 < d.tiLe;
    }

    /** % EXP them khi ha quai {@code idTemplate} (0 neu khong phai loai noi bat / ngoai gio vang). */
    public static int expThem(final Dong d, final boolean gioVang, final int idTemplate) {
        return d != null && gioVang && d.coQuai(idTemplate) ? d.exp : 0;
    }

    /** He so shiny trong ngay cong dong: chi gio vang. */
    public static boolean shinyTang(final boolean ngayCongDong, final boolean gioVang) {
        return ngayCongDong && gioVang;
    }

    /** Mo ta lan dien ra (tin web / LichNam): ten dong, gio vang, thuong. */
    public static String moTa(final Dong d) {
        if (d == null) {
            return "Một ngày mỗi tháng: một dòng Pokémon xuất hiện dày trên các tuyến của nó trong 3 giờ vàng, shiny x5.";
        }
        return "Dòng " + d.ten + " xuất hiện dày (" + d.tiLe + "% lần hồi sinh) trên các tuyến của nó trong giờ vàng "
                + d.khungGio() + "; hạ " + d.ten + " +" + d.exp + "% EXP, shiny x" + NHAN_SHINY + "."
                + (d.chieu.length() > 0 ? " Chiêu nổi bật: " + d.chieu + "." : "");
    }

    public static String phanThuong(final Dong d) {
        if (d == null) {
            return "Shiny x5, danh hiệu dòng nổi bật";
        }
        return "Danh hiệu khi hạ " + d.soHa + " " + d.ten + " trong giờ vàng; shiny x" + NHAN_SHINY + "; +" + d.exp + "% EXP";
    }
}
