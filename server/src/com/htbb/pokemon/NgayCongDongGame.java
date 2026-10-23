package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import real.Char;
import real.Monster;

/**
 * Noi NGAY CONG DONG ({@link NgayCongDong}) vao game. Du lieu data_ngay_cong_dong (sql/htbb.sql phan 11), nap
 * luc khoi dong (SuKien.nap - truoc khi ghi lich nam, de tin web co ten dong cua thang) va moi 10 phut. Diem moc:
 *
 *   HeSinhThaiGame.khiHoiSinh   -> {@link #loaiNoiBat} (doi loai trong gio vang, tren ban do cua dong)
 *   HeSinhThaiGame.khiHaQuai    -> {@link #khiHaQuai} (bo dem danh hieu "cong_dong")
 *   ExpLauDai.traoHaQuai        -> {@link #expThem} (cong vao buff EXP chung, chiu tran +100%)
 *   SuKienLauDaiGame.nhanShiny  -> {@link #gioVang} (shiny x5 chi trong gio vang)
 *   ThoiTietGame (goi -118)     -> trang thai HUD
 *
 * Su kien moi:congdong bat / tat theo lich nam (tob_su_kien.bat, ap dung luc khoi dong lai / bao tri). Moi loi bat va
 * in log, khong nem ra diem moc.
 */
public final class NgayCongDongGame {

    private NgayCongDongGame() {
    }

    static volatile NgayCongDong.Dong[] ds = new NgayCongDong.Dong[0];
    static volatile long napLuc;
    static final AtomicLong DEM_DOI = new AtomicLong();

    static {
        // mo ta tung lan dien ra cho lich nam / tin web: dong noi bat cua thang
        LichNam.moTaLan = (m, nam, lan) -> {
            if (m == null || !NgayCongDong.MA_SU_KIEN.equals(m.ma)) {
                return null;
            }
            final NgayCongDong.Dong d = NgayCongDong.chon(ds, nam, lan + 1);
            return d == null ? null : new String[]{NgayCongDong.moTa(d), NgayCongDong.phanThuong(d)};
        };
    }

    /** Nap (lai) bang. An toan khi thieu bang / thieu CSDL (giu danh sach cu). */
    public static void nap() {
        try {
            final List<NgayCongDong.Dong> r = new ArrayList<NgayCongDong.Dong>();
            DuLieuHeSinhThai.docBang("SELECT thang, nam_xoay, dong, ten, id_quai, map_ids, gio_bat_dau, so_gio, ti_le,"
                    + " exp_phan_tram, so_ha, id_danh_hieu, chieu FROM data_ngay_cong_dong WHERE bat = 1 ORDER BY thang, nam_xoay",
                    (rs) -> r.add(new NgayCongDong.Dong(rs.getInt(1), rs.getInt(2), String.valueOf(rs.getInt(3)), rs.getString(4),
                            soCsv(rs.getString(5)), soCsv(rs.getString(6)), rs.getInt(7), rs.getInt(8), rs.getInt(9),
                            rs.getInt(10), rs.getInt(11), rs.getInt(12), rs.getString(13))));
            if (!r.isEmpty()) {
                ds = r.toArray(new NgayCongDong.Dong[0]);
            }
            napLuc = DongHo.bay();
        } catch (final Throwable e) {
            System.out.println("[CONG DONG] khong nap duoc data_ngay_cong_dong: " + e);
        }
    }

    static void napNeuCu() {
        if (DongHo.bay() - napLuc > 10L * 60L * 1000L) {
            nap();
        }
    }

    /** "1, 2,3" -> {1,2,3} (bo phan tu hong). */
    public static int[] soCsv(final String s) {
        if (s == null || s.trim().isEmpty()) {
            return new int[0];
        }
        final List<Integer> r = new ArrayList<Integer>();
        for (final String x : s.split(",")) {
            try {
                r.add(Integer.parseInt(x.trim()));
            } catch (final NumberFormatException ignored) {
            }
        }
        final int[] a = new int[r.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = r.get(i);
        }
        return a;
    }

    // ------------------------------------------------------------------ trang thai

    /** Hom nay la ngay cong dong (co su kien bat theo lich). */
    public static boolean homNay() {
        return real.SuKien.bat(NgayCongDong.MA_SU_KIEN);
    }

    /** Dong noi bat cua thang hien tai (null neu chua co du lieu). */
    public static NgayCongDong.Dong dongThangNay() {
        napNeuCu();
        final Calendar c = DongHo.lich();
        return NgayCongDong.chon(ds, c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
    }

    static int gio() {
        return DongHo.lich().get(Calendar.HOUR_OF_DAY);
    }

    /** Dang gio vang (ngay cong dong + trong khung gio cua dong; thieu du lieu: 14:00-17:00). */
    public static boolean gioVang() {
        if (!homNay()) {
            return false;
        }
        final NgayCongDong.Dong d = dongThangNay();
        final int g = gio();
        return d != null ? d.trongGioVang(g)
                : g >= NgayCongDong.GIO_VANG_MAC_DINH && g < NgayCongDong.GIO_VANG_MAC_DINH + NgayCongDong.SO_GIO_MAC_DINH;
    }

    // ------------------------------------------------------------------ diem moc

    /** Loai noi bat cho lan hoi sinh tren {@code mapId} (-1 = khong doi). Goi tu HeSinhThaiGame.khiHoiSinh. */
    public static int loaiNoiBat(final int mapId, final int idGoc) {
        try {
            if (!gioVang()) {
                return -1;
            }
            final NgayCongDong.Dong d = dongThangNay();
            final int loai = NgayCongDong.loaiTrenMap(d, mapId, HeSinhThaiGame.capBungPhat);
            if (loai > 0 && NgayCongDong.doiThanhNoiBat(d, idGoc, loai, HeSinhThaiGame.R.nextInt(100))) {
                DEM_DOI.incrementAndGet();
                return loai;
            }
        } catch (final Throwable e) {
            System.out.println("[CONG DONG] loaiNoiBat: " + e);
        }
        return -1;
    }

    /** Ban do co loai noi bat hom nay. */
    public static boolean mapCoNoiBat(final int mapId) {
        try {
            return homNay() && NgayCongDong.loaiTrenMap(dongThangNay(), mapId, HeSinhThaiGame.capBungPhat) > 0;
        } catch (final Throwable e) {
            return false;
        }
    }

    /** % EXP them khi ha {@code mt} (ExpLauDai, cong vao buff chung - van chiu tran +100% cua CapDoLauDai). */
    public static int expThem(final real.LiveActor mt) {
        try {
            if (!(mt instanceof Monster) || !homNay()) {
                return 0;
            }
            return NgayCongDong.expThem(dongThangNay(), gioVang(), ((Monster) mt).idTemplate);
        } catch (final Throwable e) {
            return 0;
        }
    }

    /** Ha quai: dem danh hieu cua dong (DanhHieuGame dieu kien "cong_dong" / "tap:cong_dong", tham_so = so dong). */
    public static void khiHaQuai(final Char p, final Monster m) {
        try {
            if (p == null || m == null || !gioVang()) {
                return;
            }
            final NgayCongDong.Dong d = dongThangNay();
            if (d != null && d.coQuai(m.idTemplate)) {
                real.DanhHieuGame.suKien(p, "cong_dong", d.ma, 1);
                if (real.Map.r.nextInt(20) == 0) {
                    real.BatPokemonGame.thuongBong(p, "great", 1, "Ngày cộng đồng"); // 0.9.8: tran bong tang / ngay
                }
            }
        } catch (final Throwable e) {
            System.out.println("[CONG DONG] khiHaQuai: " + e);
        }
    }

    /** Ten dong thang nay ("" neu khong co). */
    public static String tenDong() {
        final NgayCongDong.Dong d = dongThangNay();
        return d == null ? "" : d.ten;
    }

    /** Danh sach dong (admin / test). */
    public static List<NgayCongDong.Dong> dsDong() {
        return Collections.unmodifiableList(java.util.Arrays.asList(ds));
    }
}
