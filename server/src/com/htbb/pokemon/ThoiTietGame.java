package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;

/**
 * Noi LICH THOI TIET THEO KHUNG GIO + PHU MAU MUA ({@link ThoiTietLich}) vao he sinh thai (de xuat 8). Du lieu:
 * data_thoi_tiet (anh huong, kep tran), data_thoi_tiet_map (ban do co dinh thoi tiet / tat phu mau),
 * data_thoi_tiet_su_kien (su kien dang bat them trong so + mau) - sql/htbb.sql phan 11; thieu bang = mac dinh
 * trong ThoiTietLich. Nap lan dau khi he sinh thai khoi dong, nap lai moi 10 phut.
 *
 * HeSinhThaiGame.thoiTietVung / thoiTietMap goi vao day (moi noi dung thoi tiet: sinh quai, nghien cuu, sat thuong,
 * menu, goi client). Goi -118 ({@link GoiThoiTiet}) gui cho client ban &gt;= 908 moi khi trang thai ban do cua ho doi
 * (nhip 15 giay cua he sinh thai); client cu van nhan -39.
 */
public final class ThoiTietGame {

    private ThoiTietGame() {
    }

    static volatile Map<Integer, Integer> coDinhMap = Collections.emptyMap();
    static volatile Set<Integer> tatMauMap = Collections.emptySet();
    static volatile ThoiTietLich.SuKienThoiTiet[] suKien = ThoiTietLich.SU_KIEN_MAC_DINH;
    static volatile long napLuc;

    /** Nap (lai) 3 bang. Thieu bang / loi: giu mac dinh. */
    public static void nap() {
        napLuc = DongHo.bay();
        try {
            final List<ThoiTietLich.AnhHuong> ah = new ArrayList<ThoiTietLich.AnhHuong>();
            DuLieuHeSinhThai.docBang("SELECT id, he_tang, he_giam, he_sinh, phan_tram, goi_y FROM data_thoi_tiet",
                    (rs) -> ah.add(new ThoiTietLich.AnhHuong(rs.getInt(1), NgayCongDongGame.soCsv(rs.getString(2)),
                            NgayCongDongGame.soCsv(rs.getString(3)), NgayCongDongGame.soCsv(rs.getString(4)), rs.getInt(5),
                            rs.getString(6))));
            if (!ah.isEmpty()) {
                ThoiTietLich.datBang(ah.toArray(new ThoiTietLich.AnhHuong[0]));
            }
            final Map<Integer, Integer> cd = new HashMap<Integer, Integer>();
            final Set<Integer> tm = new HashSet<Integer>();
            DuLieuHeSinhThai.docBang("SELECT map_id, thoi_tiet_co_dinh, tat_mau_mua FROM data_thoi_tiet_map", (rs) -> {
                final int t = rs.getInt(2);
                if (t >= 0 && t < SinhThaiTheGioi.SO_THOI_TIET) {
                    cd.put(rs.getInt(1), t);
                }
                if (rs.getInt(3) == 1) {
                    tm.add(rs.getInt(1));
                }
            });
            coDinhMap = cd;
            tatMauMap = tm;
            final List<ThoiTietLich.SuKienThoiTiet> sk = new ArrayList<ThoiTietLich.SuKienThoiTiet>();
            DuLieuHeSinhThai.docBang("SELECT ma_su_kien, vung, thoi_tiet, trong_so, mau_mua, uu_tien FROM data_thoi_tiet_su_kien"
                    + " WHERE bat = 1", (rs) -> sk.add(new ThoiTietLich.SuKienThoiTiet(rs.getString(1), rs.getInt(2), rs.getInt(3),
                            rs.getInt(4), rs.getInt(5), rs.getInt(6))));
            if (!sk.isEmpty()) {
                suKien = sk.toArray(new ThoiTietLich.SuKienThoiTiet[0]);
            }
        } catch (final Throwable e) {
            System.out.println("[THOI TIET] khong nap duoc bang thoi tiet: " + e);
        }
    }

    static void napNeuCu() {
        if (DongHo.bay() - napLuc > 10L * 60L * 1000L) {
            nap();
        }
    }

    /** Ma su kien trong bang thien huong dang bat (SuKien.bat). */
    static Set<String> dangBat() {
        final Set<String> s = new HashSet<String>();
        for (final ThoiTietLich.SuKienThoiTiet x : suKien) {
            if (x != null && real.SuKien.bat(x.maSuKien)) {
                s.add(x.maSuKien);
            }
        }
        return s;
    }

    /** Thoi tiet cua vung bay gio (lich khung gio + su kien). */
    public static int thoiTietVung(final int vung) {
        napNeuCu();
        final Calendar c = DongHo.lich();
        return ThoiTietLich.thoiTiet(HeSinhThaiGame.homNay(), ThoiTietLich.khung(c.get(Calendar.HOUR_OF_DAY)), vung,
                ThoiTietLich.congSuKien(suKien, dangBat(), vung));
    }

    /** Thoi tiet co dinh cua ban do (-1 = theo vung). */
    public static int coDinh(final int mapId) {
        final Integer t = coDinhMap.get(Integer.valueOf(mapId));
        return t == null ? -1 : t.intValue();
    }

    /** Mau phu mua cua ban do bay gio. */
    public static int mauMap(final int mapId) {
        if (tatMauMap.contains(Integer.valueOf(mapId))) {
            return ThoiTietLich.MAU_KHONG;
        }
        return ThoiTietLich.mauMua(HeSinhThaiGame.vung(mapId), HeSinhThaiGame.thang(), ThoiTietLich.mauSuKien(suKien, dangBat()));
    }

    // ------------------------------------------------------------------ goi -118

    public static boolean moi(final Char c) {
        return real.BanClientNhanVat.ban(c) >= GoiThoiTiet.BAN_CLIENT;
    }

    /** Trang thai goi -118 cho nguoi o ban do {@code mapId}. */
    public static GoiThoiTiet.TrangThai trangThai(final int mapId) {
        final GoiThoiTiet.TrangThai t = new GoiThoiTiet.TrangThai();
        final int vung = HeSinhThaiGame.vung(mapId);
        final boolean coTroi = SinhThaiTheGioi.trongSoThoiTiet(vung, HeSinhThaiGame.thang())[SinhThaiTheGioi.QUANG] < 100
                || coDinh(mapId) >= 0;
        final Calendar c = DongHo.lich();
        final int gio = c.get(Calendar.HOUR_OF_DAY);
        final int khung = ThoiTietLich.khung(gio);
        if (coTroi) {
            t.thoiTiet = HeSinhThaiGame.thoiTietMap(mapId);
            t.ten = SinhThaiTheGioi.tenThoiTiet(t.thoiTiet);
            t.goiY = ThoiTietLich.goiY(t.thoiTiet);
            final int giayDaQua = (gio - ThoiTietLich.gioBatDau(khung)) * 3600 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
            t.giayConLai = ThoiTietLich.GIO_KHUNG * 3600 - giayDaQua;
            t.tiepTheo = coDinh(mapId) >= 0 ? coDinh(mapId)
                    : ThoiTietLich.duBao(HeSinhThaiGame.homNay(), khung, vung, ThoiTietLich.congSuKien(suKien, dangBat(), vung), 1)[0];
        }
        t.mauMua = mauMap(mapId);
        t.tenMauMua = ThoiTietLich.TEN_MAU[Math.max(0, Math.min(ThoiTietLich.SO_MAU - 1, t.mauMua))];
        if (NgayCongDongGame.homNay()) {
            final NgayCongDong.Dong d = NgayCongDongGame.dongThangNay();
            final boolean vang = NgayCongDongGame.gioVang();
            t.congDong = vang ? 2 : 1;
            t.tenDong = d == null ? "" : d.ten;
            final int bd = d == null ? NgayCongDong.GIO_VANG_MAC_DINH : d.gioBatDau;
            final int sg = d == null ? NgayCongDong.SO_GIO_MAC_DINH : d.soGio;
            final int giayNay = gio * 3600 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
            t.giayCongDong = vang ? (bd + sg) * 3600 - giayNay : Math.max(0, bd * 3600 - giayNay);
            t.coTrenMap = NgayCongDongGame.mapCoNoiBat(mapId);
            t.goiYCongDong = d == null ? NgayCongDong.moTa(null) : NgayCongDong.moTa(d);
        }
        return t;
    }

    /** Khoa da gui moi nguoi (charDBID -> khoa trang thai + ma ban do). */
    static final Map<Integer, String> DA_GUI = new ConcurrentHashMap<Integer, String>();

    /** Gui -118 cho {@code c} neu trang thai ban do cua ho doi (hoac ep). */
    public static void gui(final Char c, final boolean ep) {
        if (!moi(c)) {
            return;
        }
        try {
            final GoiThoiTiet.TrangThai t = trangThai(c.mapID);
            final String khoa = c.mapID + "#" + t.khoa();
            final String cu = DA_GUI.get(c.charDBID);
            if (!ep && khoa.equals(cu)) {
                return;
            }
            DA_GUI.put(c.charDBID, khoa);
            final io.Message m = new io.Message(GoiThoiTiet.LENH);
            GoiThoiTiet.ghi(m.dos, t);
            c.sendMessage(m);
        } catch (final Throwable e) {
            System.out.println("[THOI TIET] gui -118: " + e);
        }
    }
}
