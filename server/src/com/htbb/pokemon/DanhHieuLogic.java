package com.htbb.pokemon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DANH HIEU (0.9.7) - phan THUAN (khong CSDL, khong mang): mau danh hieu doc tu data_danh_hieu, tien do mo khoa,
 * an / hien "???", han dung, chi so nho co tran (chi MOT danh hieu dang deo duoc tinh), dong goi tin -110.
 *
 * <p>Danh hieu dung CHUNG kho cu cua nhan vat (Char.allDanhHieu = {id hieu ung, het han} trong tob_danh_hieu, dang
 * deo = Char.idEffDanhHieu gui trong listEffect): id cua mau chinh la id hieu ung. Dai id: 8767..8999 danh hieu cu
 * (giftcode / su kien), 9600..9615 danh hieu sinh thai, 9700.. danh hieu thanh tuu moi, 10000.. danh hieu nhiem vu.
 *
 * <p>Dieu kien (cot dieu_kien):
 * <ul>
 * <li>{@code cap}, {@code pokedex_phan_tram}, {@code pokedex_loai}, {@code xong_nhiem_vu} (tham_so = ma),
 * {@code gym_huy_hieu} (so huy hieu Gym), {@code pvp_bac} (bac Elo mua 0..3), {@code top_pvp} (hang mua &lt;= so_luong), {@code su_kien} (tham_so = ma su kien
 * dang bat), {@code sinh_thai} (tham_so = ten danh hieu sinh thai), {@code nhiem_vu} (tham_so = ten danh hieu thuong
 * nhiem vu), {@code trao} (chi trao tay: giftcode / su kien / admin) - doc truc tiep trang thai game (SONG).</li>
 * <li>{@code tap:<kieu>} - dem so gia tri tham so KHAC NHAU cua su kien kieu (vd tap:den_ban_do = so ban do da den).</li>
 * <li>con lai: bo dem su kien NhiemVuEngine.suKien / datMuc cung ten (giet, boss_the_gioi, cau_ca, guc, thang_pvp,
 * thanh_tuu_nang_cap...); tham_so rong = moi tham so.</li>
 * </ul>
 */
public final class DanhHieuLogic {

    private DanhHieuLogic() {
    }

    // ================================================================== hang so

    /** Lenh S->C (bang / nhan / so moi) va C->S (xin / deo / thao / da xem). */
    public static final int LENH = -110;
    public static final int LENH_KHACH = -111;
    public static final byte G_DANH_SACH = 0, G_NHAN = 1, G_SO_MOI = 2;
    public static final byte K_XIN = 0, K_DEO = 1, K_THAO = 2, K_DA_XEM = 3;
    /**
     * Ban client dau tien biet lenh -110 / -111. Ban 906 da phat hanh KHONG co bo xu ly -> 907 (BAN tang moi ban phat
     * hanh, nhanh cap-nhat).
     */
    public static final int BAN_CLIENT = 907;

    /** Ma dong chi so (cot chi_so). Ngan sach o danh hieu (thiet_ke_can_bang 4.5): +1% Ne / Khang trang thai / Chi mang. */
    public static final String NE_TRANH = "ne_tranh", CHI_MANG = "chi_mang", KHANG_TRANG_THAI = "khang_trang_thai", HP = "hp";
    public static final String[] MA_CHI_SO = {NE_TRANH, CHI_MANG, KHANG_TRANG_THAI, HP};
    static final String[] TEN_CHI_SO = {"Né tránh", "Chí mạng", "Kháng trạng thái", "HP tối đa"};

    /** Cau hinh (cau_hinh): bat, tran o danh hieu (phan nghin), tran tong trang tri (phan nghin). */
    public static final String K_BAT = "danh_hieu.bat";
    public static final String K_TRAN = "danh_hieu.tran_phan_nghin";
    public static final String K_TRAN_TRANG_TRI = "trang_tri.tran_tong_phan_nghin";
    public static final int TRAN_MAC_DINH = 10;
    public static final int TRAN_TRANG_TRI_MAC_DINH = 50;
    /** Tran CUNG cua o danh hieu du cau hinh dat gi (3%). */
    public static final int TRAN_CUNG = 30;

    /** an: 0 hien, 1 an ("???" toi khi mo), 2 chi hien khi dang co (danh hieu cu / trao tay). */
    public static final int HIEN = 0, AN = 1, CHI_KHI_CO = 2;

    /** Trang thai mot dong o bang client. */
    public static final byte CHUA_CO = 0, DA_CO = 1, DANG_DEO = 2;

    static final Set<String> SONG = new HashSet<String>(java.util.Arrays.asList("cap", "pokedex_phan_tram", "pokedex_loai",
            "xong_nhiem_vu", "pvp_bac", "top_pvp", "su_kien", "sinh_thai", "nhiem_vu", "gym_huy_hieu", "trao"));

    /** Khoa tap rieng: id da tung mo (giu "hien" sau khi het han), id moi chua xem (cham do). */
    public static final String TAP_DA_MO = "#da_mo", TAP_MOI = "#moi";

    // ================================================================== mau

    /** Mot dong data_danh_hieu. */
    public static final class Mau {
        public final int id;
        public final String ma, ten, moTa, goiY;
        public final int doHiem;
        public final String hinh;
        public final String chiSo;
        public final int phanNghin;
        public final String dieuKien, thamSo;
        public final long soLuong;
        public final int an;
        public final int thoiHanPhut;
        public final String nguon;
        public final int thuTu;

        public Mau(final int id, final String ma, final String ten, final String moTa, final String goiY, final int doHiem,
                final String hinh, final String chiSo, final int phanNghin, final String dieuKien, final String thamSo,
                final long soLuong, final int an, final int thoiHanPhut, final String nguon, final int thuTu) {
            this.id = id;
            this.ma = ma == null ? "" : ma;
            this.ten = ten == null ? "" : ten;
            this.moTa = moTa == null ? "" : moTa;
            this.goiY = goiY == null ? "" : goiY;
            this.doHiem = Math.max(0, Math.min(9, doHiem));
            this.hinh = hinh == null ? "" : hinh;
            this.chiSo = chiSo == null ? "" : chiSo.trim();
            this.phanNghin = Math.max(0, phanNghin);
            this.dieuKien = dieuKien == null ? "trao" : dieuKien.trim();
            this.thamSo = thamSo == null ? "" : thamSo.trim();
            this.soLuong = Math.max(1L, soLuong);
            this.an = an;
            this.thoiHanPhut = Math.max(0, thoiHanPhut);
            this.nguon = nguon == null ? "" : nguon;
            this.thuTu = thuTu;
        }

        public boolean laSong() {
            return SONG.contains(dieuKien);
        }

        public boolean laTap() {
            return dieuKien.startsWith("tap:");
        }

        /** Kieu su kien ma bo dem cua mau nay nghe (rong = mau song). */
        public String kieuSuKien() {
            if (laTap()) {
                return dieuKien.substring(4);
            }
            return laSong() ? "" : dieuKien;
        }

        /** Khoa bo dem (tob_danh_hieu_dem.khoa). */
        public String khoaDem() {
            if (laTap()) {
                return dieuKien;
            }
            return thamSo.isEmpty() ? dieuKien : dieuKien + ":" + thamSo;
        }

        public boolean coHan() {
            return thoiHanPhut > 0;
        }
    }

    /** Ngu canh doc trang thai song cua nhan vat (game hoac gia lap trong kiem thu). */
    public interface NguCanh {
        int cap();

        int pokedexPhanTram();

        int pokedexLoai();

        boolean xongNhiemVu(String ma);

        /** Bac Elo mua hien tai 0..3 (-1 chua danh). */
        int pvpBac();

        /** Hang mua hien tai (1 = dau), 0 = ngoai bang. */
        int hangPvp();

        boolean suKienBat(String ma);

        boolean sinhThai(String ten);

        boolean nhiemVu(String ten);

        /** So huy hieu Gym da co (HoSoLauDai.gymHuyHieu). */
        int gymHuyHieu();

        /** Gia tri bo dem (dem / muc). */
        long dem(String khoa);

        /** So phan tu tap. */
        int tap(String khoa);
    }

    // ================================================================== tien do

    /** Tien do hien tai (so cung don vi so_luong). */
    public static long tienDo(final Mau m, final NguCanh c) {
        if (m == null || c == null) {
            return 0L;
        }
        if (m.laTap()) {
            return c.tap(m.khoaDem());
        }
        switch (m.dieuKien) {
            case "cap":
                return c.cap();
            case "pokedex_phan_tram":
                return c.pokedexPhanTram();
            case "pokedex_loai":
                return c.pokedexLoai();
            case "xong_nhiem_vu":
                return c.xongNhiemVu(m.thamSo) ? m.soLuong : 0L;
            case "pvp_bac":
                return Math.max(0, c.pvpBac());
            case "top_pvp": {
                final int h = c.hangPvp();
                return h > 0 && h <= m.soLuong ? m.soLuong : 0L;
            }
            case "su_kien":
                return c.suKienBat(m.thamSo) ? m.soLuong : 0L;
            case "sinh_thai":
                return c.sinhThai(m.thamSo) ? m.soLuong : 0L;
            case "nhiem_vu":
                return c.nhiemVu(m.thamSo) ? m.soLuong : 0L;
            case "gym_huy_hieu":
                return c.gymHuyHieu();
            case "trao":
                return 0L;
            default:
                return c.dem(m.khoaDem());
        }
    }

    /** Du dieu kien mo. "trao" khong bao gio tu mo. */
    public static boolean du(final Mau m, final NguCanh c) {
        return m != null && !"trao".equals(m.dieuKien) && tienDo(m, c) >= m.soLuong;
    }

    /** Hien ten that? an = 1 chi hien khi dang co hoac da tung mo. */
    public static boolean hienTen(final Mau m, final boolean dangCo, final boolean daTungMo) {
        return m.an != AN || dangCo || daTungMo;
    }

    /** Co dua vao bang client khong (an = 2 chi khi dang co). */
    public static boolean coTrongBang(final Mau m, final boolean dangCo) {
        return m.an != CHI_KHI_CO || dangCo;
    }

    // ================================================================== han

    /** Het han: timeExpire &gt; 0 va da qua (cung luat data.DanhHieu.isExpire). */
    public static boolean hetHan(final long timeExpire, final long bay) {
        return timeExpire > 0L && bay - timeExpire >= 0L;
    }

    /** Phut con lai (-1 = vinh vien, 0 = het). */
    public static int phutConLai(final long timeExpire, final long bay) {
        if (timeExpire <= 0L) {
            return -1;
        }
        final long ms = timeExpire - bay;
        return ms <= 0L ? 0 : (int) Math.min(Integer.MAX_VALUE, (ms + 59999L) / 60000L);
    }

    /** Danh hieu co han dang con: trao lai (gia han) khi con duoi nua thoi han. */
    public static boolean canGiaHan(final long timeExpire, final int thoiHanPhut, final long bay) {
        if (thoiHanPhut <= 0 || timeExpire <= 0L) {
            return false;
        }
        return timeExpire - bay < thoiHanPhut * 60000L / 2L;
    }

    /** "3 ngày 4 giờ", "5 giờ 12 phút", "12 phút"; "" vinh vien. */
    public static String chuConLai(final int phut) {
        if (phut < 0) {
            return "";
        }
        final int ngay = phut / 1440, gio = (phut % 1440) / 60, p = phut % 60;
        if (ngay > 0) {
            return ngay + " ngày" + (gio > 0 ? " " + gio + " giờ" : "");
        }
        if (gio > 0) {
            return gio + " giờ" + (p > 0 ? " " + p + " phút" : "");
        }
        return Math.max(1, p) + " phút";
    }

    // ================================================================== chi so (mot danh hieu dang deo, co tran)

    /** Tran o (phan nghin) sau kep cung [0, TRAN_CUNG]. */
    public static int tranO(final int cauHinh) {
        return Math.max(0, Math.min(TRAN_CUNG, cauHinh));
    }

    /**
     * Hieu luc phan nghin cua dong dang deo: min(gia tri, tran o, tran tong trang tri - trang tri khac), &gt;= 0.
     * Chi danh hieu DANG DEO (Char.idEffDanhHieu) goi ham nay - so huu nhieu khong cong don.
     */
    public static int hieuLuc(final int phanNghin, final int tranO, final int tranTrangTri, final int trangTriKhac) {
        final long con = (long) Math.max(0, tranTrangTri) - Math.max(0, trangTriKhac);
        final long v = Math.min(Math.min((long) Math.max(0, phanNghin), (long) tranO(tranO)), con);
        return (int) Math.max(0L, v);
    }

    /** Phan nghin cua dong {@code ma} tu mau dang deo (null = khong deo / het han / sai dong). */
    public static int phanNghin(final Mau dangDeo, final String ma, final int tranO, final int tranTrangTri, final int trangTriKhac) {
        if (dangDeo == null || ma == null || !ma.equals(dangDeo.chiSo)) {
            return 0;
        }
        return hieuLuc(dangDeo.phanNghin, tranO, tranTrangTri, trangTriKhac);
    }

    public static boolean laMaChiSo(final String ma) {
        for (final String s : MA_CHI_SO) {
            if (s.equals(ma)) {
                return true;
            }
        }
        return false;
    }

    /** "+1% Né tránh", "+0,5% Chí mạng"; "" khi khong co dong. */
    public static String moTaChiSo(final String ma, final int phanNghin) {
        if (ma == null || ma.isEmpty() || phanNghin <= 0) {
            return "";
        }
        String ten = ma;
        for (int i = 0; i < MA_CHI_SO.length; i++) {
            if (MA_CHI_SO[i].equals(ma)) {
                ten = TEN_CHI_SO[i];
            }
        }
        return "+" + phanTram(phanNghin) + " " + ten;
    }

    /** 10 -> "1%", 5 -> "0,5%", 7 -> "0,7%". */
    public static String phanTram(final int phanNghin) {
        final int nguyen = phanNghin / 10, le = phanNghin % 10;
        return le == 0 ? nguyen + "%" : nguyen + "," + le + "%";
    }

    // ================================================================== bang client

    /** Mot dong bang gui client (da tinh an / hien). */
    public static final class Dong {
        public int id;
        public String ten, moTa, goiY, chiSo, nguon;
        public int doHiem;
        public String hinh;
        public byte trangThai;
        public boolean an;
        public long tienDo, can;
        public int phutConLai;
        public boolean moi;
    }

    /** Dung dong bang cho mau m. */
    public static Dong dong(final Mau m, final NguCanh c, final boolean dangCo, final boolean dangDeo, final boolean daTungMo,
            final long timeExpire, final boolean moi, final long bay, final int tranO) {
        final Dong d = new Dong();
        final boolean hien = hienTen(m, dangCo, daTungMo);
        d.id = m.id;
        d.an = !hien;
        d.ten = hien ? m.ten : "???";
        d.moTa = hien ? m.moTa : "???";
        d.goiY = m.goiY;
        d.doHiem = m.doHiem;
        d.hinh = hien ? m.hinh : "";
        d.chiSo = hien ? moTaChiSo(m.chiSo, Math.min(m.phanNghin, tranO(tranO))) : "";
        d.nguon = m.nguon;
        d.trangThai = dangDeo ? DANG_DEO : dangCo ? DA_CO : CHUA_CO;
        d.can = m.soLuong;
        final long td = dangCo ? m.soLuong : tienDo(m, c);
        d.tienDo = hien ? Math.max(0L, Math.min(m.soLuong, td)) : 0L;
        d.phutConLai = dangCo ? phutConLai(timeExpire, bay) : (m.coHan() ? m.thoiHanPhut : -1);
        d.moi = moi && dangCo;
        return d;
    }

    /** Sap: dang deo, moi, da co (do hiem cao truoc), chua co (gan xong truoc), roi thu_tu. */
    public static void sap(final List<Dong> ds, final java.util.Map<Integer, Integer> thuTu) {
        Collections.sort(ds, (a, b) -> {
            if (a.trangThai != b.trangThai) {
                return b.trangThai - a.trangThai;
            }
            if (a.moi != b.moi) {
                return a.moi ? -1 : 1;
            }
            if (a.trangThai != CHUA_CO && a.doHiem != b.doHiem) {
                return b.doHiem - a.doHiem;
            }
            final Integer ta = thuTu.get(Integer.valueOf(a.id)), tb = thuTu.get(Integer.valueOf(b.id));
            final int x = ta == null ? 0 : ta.intValue(), y = tb == null ? 0 : tb.intValue();
            return x != y ? x - y : a.id - b.id;
        });
    }

    /**
     * Goi -110 G_DANH_SACH: byte mo (1 = client mo bang), short dang deo, byte so do hiem + UTF ten moi do hiem,
     * short n, moi dong: short id, UTF ten, UTF mo ta, UTF goi y, byte do hiem, UTF hinh, byte trang thai, byte an,
     * int tien do, int can, int phut con lai, UTF chi so, UTF nguon, byte moi.
     */
    public static void ghiDanhSach(final DataOutputStream o, final boolean mo, final int dangDeo, final String[] tenDoHiem,
            final List<Dong> ds) throws IOException {
        o.writeByte(G_DANH_SACH);
        o.writeByte(mo ? 1 : 0);
        o.writeShort(dangDeo);
        final int nh = tenDoHiem == null ? 0 : Math.min(16, tenDoHiem.length);
        o.writeByte(nh);
        for (int i = 0; i < nh; i++) {
            o.writeUTF(tenDoHiem[i] == null ? "" : tenDoHiem[i]);
        }
        final int n = Math.min(Short.MAX_VALUE, ds.size());
        o.writeShort(n);
        for (int i = 0; i < n; i++) {
            final Dong d = ds.get(i);
            o.writeShort(d.id);
            o.writeUTF(cat(d.ten, 60));
            o.writeUTF(cat(d.moTa, 200));
            o.writeUTF(cat(d.goiY, 200));
            o.writeByte(d.doHiem);
            o.writeUTF(cat(d.hinh, 16));
            o.writeByte(d.trangThai);
            o.writeByte(d.an ? 1 : 0);
            o.writeInt((int) Math.min(Integer.MAX_VALUE, d.tienDo));
            o.writeInt((int) Math.min(Integer.MAX_VALUE, d.can));
            o.writeInt(d.phutConLai);
            o.writeUTF(cat(d.chiSo, 60));
            o.writeUTF(cat(d.nguon, 40));
            o.writeByte(d.moi ? 1 : 0);
        }
    }

    /** Goi -110 G_NHAN: short id nhan vat, short id danh hieu (-1 = khong), UTF ten, byte do hiem. */
    public static void ghiNhan(final DataOutputStream o, final int idChar, final int idDh, final String ten, final int doHiem)
            throws IOException {
        o.writeByte(G_NHAN);
        o.writeShort(idChar);
        o.writeShort(idDh);
        o.writeUTF(cat(ten == null ? "" : ten, 60));
        o.writeByte(doHiem);
    }

    /** Goi -110 G_SO_MOI: short so danh hieu moi chua xem. */
    public static void ghiSoMoi(final DataOutputStream o, final int so) throws IOException {
        o.writeByte(G_SO_MOI);
        o.writeShort(Math.max(0, Math.min(Short.MAX_VALUE, so)));
    }

    static String cat(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() <= n ? s : s.substring(0, n);
    }

    // ================================================================== tap (chuoi "a,b,c")

    public static Set<String> docTap(final String s) {
        final Set<String> t = new java.util.LinkedHashSet<String>();
        if (s != null) {
            for (final String x : s.split(",")) {
                final String y = x.trim();
                if (!y.isEmpty()) {
                    t.add(y);
                }
            }
        }
        return t;
    }

    public static String ghiTap(final Set<String> t) {
        final StringBuilder b = new StringBuilder();
        for (final String s : t) {
            if (b.length() > 0) {
                b.append(',');
            }
            b.append(s);
        }
        return b.toString();
    }

    /** Cac mau nghe su kien {@code kieu} (bo dem / tap). */
    public static List<Mau> theoKieu(final List<Mau> tatCa, final String kieu) {
        final List<Mau> r = new ArrayList<Mau>();
        for (final Mau m : tatCa) {
            if (kieu.equals(m.kieuSuKien())) {
                r.add(m);
            }
        }
        return r;
    }
}
