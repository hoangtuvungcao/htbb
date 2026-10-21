package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * CUP PVP TUAN (0.9.7, de xuat 7 da duyet) - phan THUAN (khong CSDL, khong mang). Che do RIENG, khong dung Elo
 * mua (ThangPvp) va khong qua luat PK the gioi: tran cup dien ra trong phong rieng (PhongRieng), HP khong bao gio
 * ve 0 (khong guc, khong mat EXP, khong tinh diem do sat / Elo).
 *
 * <ul>
 *   <li><b>Tuan cup</b> = tuan ISO cua may chu, thu Hai 04:00 gio Viet Nam -&gt; thu Hai tuan sau 04:00 (cung moc
 *       {@link LichNam#thuHaiTuanIso} ma website dung cho "tuan nay"). {@link #tuan} = chi so tuan lien tuc (khong
 *       nhay o tuan 53).</li>
 *   <li><b>Xoay vong</b>: lich 16 o (data_cup_lich) lap lai: Cup Nhi (cap &lt;= 30), Cup he (mot he xoay Lua / Nuoc /
 *       Co / Dien), Cup Doi 2v2, Cup Khong trang bi. Loai cup o data_cup_loai.</li>
 *   <li><b>Lich trong tuan</b>: dang ky tu dau tuan toi Chu nhat {@link #GIO_VONG_DAU}:00 - 5 phut; Chu nhat
 *       {@link #GIO_VONG_DAU}:00 vong 1, moi {@link #PHUT_MOI_VONG} phut mot vong Thuy Si ({@link #SO_VONG} vong),
 *       tran toi da {@link #GIAY_TRAN} giay; xong vong cuoi thi chot hang va phat thuong.</li>
 *   <li><b>Ghep Thuy Si</b> ({@link #ghep}): cung diem truoc, khong gap lai, CHONG CLONE: hai ben cung thiet bi / IP /
 *       tai khoan khong bao gio gap nhau; le thi mien dau (3 diem, moi doi toi da 1 lan).</li>
 *   <li><b>Chuan hoa</b> ({@link #satThuong}): moi don = phan nghin HP toi da cua muc tieu, tinh tu sat thuong PvP tho
 *       chia HP toi da cua nguoi danh (ai cung "danh bang thang do cua minh"), kep [min, max] cua loai cup - cap do
 *       khong con quyet dinh; Cup Khong trang bi kep hep 30..50 phan nghin nen trang bi gan nhu khong tinh.</li>
 *   <li><b>Thuong</b> ({@link #thuong}): danh hieu co han + luong khoa nho (vo dich 20, tran 20 / nguoi / tuan, trong
 *       tran 25 luong khoa / tuan cua Gym / boss / xep hang), KHONG xu. Ghi tob_cup_thuong truoc khi phat (khong
 *       phat 2 lan).</li>
 * </ul>
 */
public final class CupLogic {

    private CupLogic() {
    }

    // ================================================================== hang so (mac dinh cau hinh cup.*)

    public static final int GIO_DOI_TUAN = LichNam.GIO_CHUYEN; // 04:00 gio Viet Nam
    public static final int GIO_VONG_DAU = 20;
    public static final int PHUT_MOI_VONG = 10;
    public static final int SO_VONG = 5;
    public static final int GIAY_TRAN = 180;
    /** Dong dang ky truoc vong 1 bao nhieu phut. */
    public static final int PHUT_DONG_DANG_KY = 5;
    public static final int SO_DOI_TOI_THIEU = 4;
    public static final int CAP_TOI_THIEU = 20;
    /** Diem Thuy Si. */
    public static final int DIEM_THANG = 3, DIEM_HOA = 1, DIEM_THUA = 0, DIEM_MIEN = 3;
    /** Thuong luong khoa theo hang. Tran mot nguoi / tuan = LK_VO_DICH. */
    public static final int LK_VO_DICH = 20, LK_HANG_2_3 = 10, LK_HANG_4_8 = 5, LK_THAM_GIA = 2;
    /** Tham gia du dieu kien thuong: da dau &gt;= 3 tran va thang &gt;= 1 (chong treo may). */
    public static final int TRAN_THAM_GIA_TOI_THIEU = 3;
    /** Danh hieu (id hieu ung, chung kho Char.allDanhHieu / data_danh_hieu): 9850.. cup. */
    public static final int DH_VO_DICH_NHI = 9850, DH_VO_DICH_HE = 9851, DH_VO_DICH_DOI = 9852, DH_VO_DICH_THUAN = 9853,
            DH_A_QUAN = 9854, DH_DAU_SI_CUP = 9855;
    public static final int PHUT_DH_VO_DICH = 28 * 1440, PHUT_DH_A_QUAN = 14 * 1440, PHUT_DH_DAU_SI = 7 * 1440;

    /** Ma lich nam (tob_su_kien_lich): moi tuan mot dong, ten = ten cup tuan do. */
    public static final String MA_LICH = "moi:cuptuan";
    public static final String NHOM_LICH = "Cúp PvP";

    // ================================================================== tuan

    private static final long NGAY_MS = 86400000L;
    private static final long LECH_VN_MS = 7L * 3600000L; // Asia/Ho_Chi_Minh: UTC+7, khong gio mua he

    /** Chi so tuan lien tuc: tuan 0 bat dau thu Hai 05/01/1970 04:00 gio Viet Nam. */
    public static int tuan(final long ms) {
        final long ngay = Math.floorDiv(ms + LECH_VN_MS - GIO_DOI_TUAN * 3600000L, NGAY_MS); // 0 = thu Nam 01/01/1970
        return (int) Math.floorDiv(ngay - 4L, 7L);
    }

    /** Moc mo (thu Hai 04:00 gio Viet Nam) cua tuan {@code t}. */
    public static long moTuan(final int t) {
        return ((long) t * 7L + 4L) * NGAY_MS + GIO_DOI_TUAN * 3600000L - LECH_VN_MS;
    }

    /** {nam ISO, tuan ISO} cua tuan cup chua {@code ms} (cung cach LichNam.thuHaiTuanIso dem). */
    public static int[] namTuanIso(final long ms) {
        final Calendar c = Calendar.getInstance(LichNam.MUI_GIO);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(4);
        c.setTimeInMillis(moTuan(tuan(ms)) + 12L * 3600000L);
        final int w = c.get(Calendar.WEEK_OF_YEAR);
        int y = c.get(Calendar.YEAR);
        final int m = c.get(Calendar.MONTH);
        if (w >= 52 && m == Calendar.JANUARY) {
            y--;
        } else if (w == 1 && m == Calendar.DECEMBER) {
            y++;
        }
        return new int[]{y, w};
    }

    // ================================================================== loai cup + xoay vong

    /** Mot loai cup (data_cup_loai). capToiDa 0 = khong gioi han; he -1 = moi he; soNguoi 1 (don) / 2 (doi). */
    public static final class Loai {
        public final String ma, ten, moTa;
        public final int capToiDa, he, soNguoi;
        public final boolean khongTrangBi;
        public final int phanNghinMin, phanNghinMax;

        public Loai(final String ma, final String ten, final String moTa, final int capToiDa, final int he,
                final int soNguoi, final boolean khongTrangBi, final int phanNghinMin, final int phanNghinMax) {
            this.ma = ma;
            this.ten = ten;
            this.moTa = moTa == null ? "" : moTa;
            this.capToiDa = Math.max(0, capToiDa);
            this.he = he;
            this.soNguoi = soNguoi == 2 ? 2 : 1;
            this.khongTrangBi = khongTrangBi;
            this.phanNghinMin = Math.max(1, Math.min(phanNghinMin, phanNghinMax));
            this.phanNghinMax = Math.max(this.phanNghinMin, phanNghinMax);
        }

        /** Danh hieu vo dich cua loai cup nay. */
        public int danhHieuVoDich() {
            if (soNguoi == 2) {
                return DH_VO_DICH_DOI;
            }
            if (khongTrangBi) {
                return DH_VO_DICH_THUAN;
            }
            return he >= 0 ? DH_VO_DICH_HE : DH_VO_DICH_NHI;
        }

        /** Luat mot dong cho thong bao / web. */
        public String luat() {
            final StringBuilder b = new StringBuilder();
            b.append(soNguoi == 2 ? "Đấu đôi 2 đấu 2 (đăng ký theo tổ đội 2 người)" : "Đấu đơn 1 đấu 1");
            if (capToiDa > 0) {
                b.append(", cấp ").append(CAP_TOI_THIEU).append("–").append(capToiDa);
            } else {
                b.append(", từ cấp ").append(CAP_TOI_THIEU);
            }
            if (he >= 0) {
                b.append(", chỉ Pokémon mang hệ ").append(tenHe(he));
            }
            b.append(khongTrangBi ? ", trang bị gần như không tính" : ", chỉ số chuẩn hoá theo cấp");
            return b.toString();
        }
    }

    public static List<Loai> loaiMacDinh() {
        final List<Loai> ds = new ArrayList<Loai>();
        ds.add(new Loai("nhi", "Cúp Nhí", "Cúp cho Pokémon cấp 20–30: chỉ số chuẩn hoá, ai cũng có cơ hội.", 30, -1, 1, false, 10, 80));
        ds.add(new Loai("he_lua", "Cúp hệ Lửa", "Chỉ Pokémon mang hệ Lửa (Charmander, Flareon...).", 0, HePokemon.LUA, 1, false, 10, 80));
        ds.add(new Loai("he_nuoc", "Cúp hệ Nước", "Chỉ Pokémon mang hệ Nước (Squirtle, Vaporeon...).", 0, HePokemon.NUOC, 1, false, 10, 80));
        ds.add(new Loai("he_co", "Cúp hệ Cỏ", "Chỉ Pokémon mang hệ Cỏ (Bulbasaur, Leafeon...).", 0, HePokemon.CO, 1, false, 10, 80));
        ds.add(new Loai("he_dien", "Cúp hệ Điện", "Chỉ Pokémon mang hệ Điện (Pichu, Jolteon...).", 0, HePokemon.DIEN, 1, false, 10, 80));
        ds.add(new Loai("doi", "Cúp Đôi", "Đấu 2 đấu 2: đăng ký cùng một bạn trong tổ đội.", 0, -1, 2, false, 10, 80));
        ds.add(new Loai("thuan", "Cúp Không trang bị", "Chỉ kỹ năng và hệ quyết định: trang bị gần như không tính.", 0, -1, 1, true, 30, 50));
        return ds;
    }

    /** Lich xoay 16 tuan mac dinh (data_cup_lich). */
    public static List<String> lichMacDinh() {
        final List<String> l = new ArrayList<String>();
        final String[] he = {"he_lua", "he_nuoc", "he_co", "he_dien"};
        for (int i = 0; i < 4; i++) {
            l.add("nhi");
            l.add(he[i]);
            l.add("doi");
            l.add("thuan");
        }
        return l;
    }

    /** Du lieu dang dung (CSDL nap de vao; thieu thi mac dinh). */
    private static volatile List<Loai> LOAI = loaiMacDinh();
    private static volatile List<String> LICH = lichMacDinh();

    public static synchronized void datDuLieu(final List<Loai> loai, final List<String> lich) {
        if (loai != null && !loai.isEmpty()) {
            LOAI = Collections.unmodifiableList(new ArrayList<Loai>(loai));
        }
        if (lich != null && !lich.isEmpty()) {
            LICH = Collections.unmodifiableList(new ArrayList<String>(lich));
        }
    }

    public static List<Loai> dsLoai() {
        return LOAI;
    }

    public static List<String> dsLich() {
        return LICH;
    }

    public static Loai loai(final String ma) {
        for (final Loai l : LOAI) {
            if (l.ma.equals(ma)) {
                return l;
            }
        }
        return null;
    }

    /** Loai cup cua tuan {@code t} theo lich xoay (o lich tro toi ma khong co thi lay loai dau). */
    public static Loai loaiTuan(final int t) {
        final List<String> l = LICH;
        final Loai x = l.isEmpty() ? null : loai(l.get(Math.floorMod(t, l.size())));
        return x != null ? x : LOAI.get(0);
    }

    // ================================================================== lich trong tuan

    public static final int PHA_DANG_KY = 0, PHA_CHO_VONG = 1, PHA_VONG = 2, PHA_XONG = 3;

    /** Moc bat dau vong {@code v} (0..SO_VONG-1) cua tuan {@code t}: Chu nhat GIO_VONG_DAU:00 + v x PHUT_MOI_VONG. */
    public static long batDauVong(final int t, final int v, final int gioVongDau, final int phutMoiVong) {
        return moTuan(t) + 6L * NGAY_MS + (gioVongDau - GIO_DOI_TUAN) * 3600000L + (long) v * phutMoiVong * 60000L;
    }

    public static long dongDangKy(final int t, final int gioVongDau) {
        return batDauVong(t, 0, gioVongDau, PHUT_MOI_VONG) - PHUT_DONG_DANG_KY * 60000L;
    }

    /** {pha, vong} luc {@code ms}: vong = chi so vong dang dien ra (PHA_VONG) / sap toi (PHA_CHO_VONG). */
    public static int[] pha(final long ms, final int gioVongDau, final int phutMoiVong, final int soVong) {
        final int t = tuan(ms);
        if (ms < dongDangKy(t, gioVongDau)) {
            return new int[]{PHA_DANG_KY, 0};
        }
        final long v0 = batDauVong(t, 0, gioVongDau, phutMoiVong);
        if (ms < v0) {
            return new int[]{PHA_CHO_VONG, 0};
        }
        final int v = (int) ((ms - v0) / (phutMoiVong * 60000L));
        if (v < soVong) {
            return new int[]{PHA_VONG, v};
        }
        return new int[]{PHA_XONG, soVong};
    }

    // ================================================================== dang ky

    /**
     * Kiem mot thanh vien dang ky cup {@code l}. tuoiGio / gioChoi = -1 khi khong biet (khong chan). Nguong tuoi tai
     * khoan / gio choi lay tu cau hinh chong clone (chong_clone.*).
     */
    public static String kiemThanhVien(final Loai l, final String ten, final int cap, final int he1, final int he2,
            final long tuoiTaiKhoanGio, final long gioChoi, final int tuoiToiThieuGio, final int gioChoiToiThieu) {
        final String ai = ten == null || ten.isEmpty() ? "Bạn" : ten;
        if (cap < CAP_TOI_THIEU) {
            return ai + " cần đạt cấp " + CAP_TOI_THIEU + " mới dự cúp được.";
        }
        if (l.capToiDa > 0 && cap > l.capToiDa) {
            return l.ten + " chỉ dành cho Pokémon cấp " + CAP_TOI_THIEU + "–" + l.capToiDa + " (" + ai + " cấp " + cap + ").";
        }
        if (l.he >= 0 && he1 != l.he && he2 != l.he) {
            return l.ten + " chỉ dành cho Pokémon mang hệ " + tenHe(l.he) + ".";
        }
        if (tuoiToiThieuGio > 0 && tuoiTaiKhoanGio >= 0 && tuoiTaiKhoanGio < tuoiToiThieuGio) {
            return "Tài khoản của " + ai + " cần tạo đủ " + tuoiToiThieuGio + " giờ mới dự cúp được.";
        }
        if (gioChoiToiThieu > 0 && gioChoi >= 0 && gioChoi < gioChoiToiThieu) {
            return ai + " cần chơi đủ " + gioChoiToiThieu + " giờ mới dự cúp được.";
        }
        return null;
    }

    /** Khoa chong clone cua mot nhan vat: thiet bi / IP / tai khoan (rong thi bo). */
    public static Set<String> khoaClone(final String may, final String ip, final int userId) {
        final Set<String> s = new HashSet<String>();
        if (may != null && !may.trim().isEmpty()) {
            s.add("may:" + may.trim());
        }
        if (ip != null && !ip.trim().isEmpty()) {
            s.add("ip:" + ip.trim());
        }
        if (userId > 0) {
            s.add("tk:" + userId);
        }
        return s;
    }

    /** Hai tap khoa co phan tu chung (cung thiet bi / IP / tai khoan). */
    public static boolean trungClone(final Set<String> a, final Set<String> b) {
        for (final String k : a) {
            if (b.contains(k)) {
                return true;
            }
        }
        return false;
    }

    // ================================================================== Thuy Si

    /** Mot doi (1 nguoi o cup don, 2 o Cup Doi) trong bang Thuy Si. */
    public static final class Doi {
        public final int id;
        public final int thuTuDangKy;
        public final Set<String> khoa;
        public int diem, thang, hoa, thua;
        public boolean daMien;
        public final Set<Integer> daGap = new HashSet<Integer>();

        public Doi(final int id, final int thuTuDangKy, final Set<String> khoa) {
            this.id = id;
            this.thuTuDangKy = thuTuDangKy;
            this.khoa = khoa == null ? new HashSet<String>() : khoa;
        }

        public int soTran() {
            return thang + hoa + thua;
        }
    }

    /** Ket qua ghep mot vong: cap {idA, idB}, doi mien dau (-1 khong co), doi khong ghep duoc (chong clone). */
    public static final class Ghep {
        public final List<int[]> cap = new ArrayList<int[]>();
        public int mien = -1;
        public final List<Integer> khongGhep = new ArrayList<Integer>();
    }

    static boolean hopLe(final Doi a, final Doi b, final boolean choGapLai) {
        if (a.id == b.id || trungClone(a.khoa, b.khoa)) {
            return false;
        }
        return choGapLai || !a.daGap.contains(Integer.valueOf(b.id));
    }

    /**
     * Ghep mot vong Thuy Si. Thu tu: diem giam dan, roi xao tron on dinh theo {@code hatGiong} trong cung diem. Le:
     * doi thap nhat chua tung mien thi mien. Tim ghep day du bang tim kiem quay lui (gioi han buoc); khong duoc thi
     * cho gap lai; van khong duoc thi tham lam - doi con lai (chi vi chong clone) vao {@link Ghep#khongGhep}.
     */
    public static Ghep ghep(final List<Doi> ds, final long hatGiong) {
        final List<Doi> thuTu = new ArrayList<Doi>(ds);
        Collections.shuffle(thuTu, new Random(hatGiong));
        Collections.sort(thuTu, (a, b) -> Integer.compare(b.diem, a.diem));
        final Ghep g = new Ghep();
        if (thuTu.size() % 2 == 1) {
            for (int i = thuTu.size() - 1; i >= 0; i--) {
                if (!thuTu.get(i).daMien) {
                    g.mien = thuTu.remove(i).id;
                    break;
                }
            }
            if (g.mien < 0) {
                g.mien = thuTu.remove(thuTu.size() - 1).id; // ai cung da mien: doi cuoi mien lan 2
            }
        }
        for (int lan = 0; lan < 2; lan++) {
            final List<int[]> kq = new ArrayList<int[]>();
            final int[] buoc = {0};
            if (quayLui(thuTu, new boolean[thuTu.size()], kq, lan == 1, buoc)) {
                g.cap.addAll(kq);
                return g;
            }
        }
        // tham lam: ghep duoc bao nhieu thi ghep, con lai khong co doi thu vong nay
        final boolean[] da = new boolean[thuTu.size()];
        for (int i = 0; i < thuTu.size(); i++) {
            if (da[i]) {
                continue;
            }
            for (int j = i + 1; j < thuTu.size(); j++) {
                if (!da[j] && hopLe(thuTu.get(i), thuTu.get(j), true)) {
                    da[i] = true;
                    da[j] = true;
                    g.cap.add(new int[]{thuTu.get(i).id, thuTu.get(j).id});
                    break;
                }
            }
            if (!da[i]) {
                da[i] = true;
                g.khongGhep.add(Integer.valueOf(thuTu.get(i).id));
            }
        }
        return g;
    }

    static final int BUOC_TOI_DA = 200000;

    static boolean quayLui(final List<Doi> ds, final boolean[] da, final List<int[]> kq, final boolean choGapLai, final int[] buoc) {
        int i = 0;
        while (i < ds.size() && da[i]) {
            i++;
        }
        if (i == ds.size()) {
            return true;
        }
        if (++buoc[0] > BUOC_TOI_DA) {
            return false;
        }
        da[i] = true;
        for (int j = i + 1; j < ds.size(); j++) {
            if (!da[j] && hopLe(ds.get(i), ds.get(j), choGapLai)) {
                da[j] = true;
                kq.add(new int[]{ds.get(i).id, ds.get(j).id});
                if (quayLui(ds, da, kq, choGapLai, buoc)) {
                    return true;
                }
                kq.remove(kq.size() - 1);
                da[j] = false;
            }
        }
        da[i] = false;
        return false;
    }

    /** Ket qua tran: 1 = doi A thang, 2 = doi B thang, 3 = hoa. */
    public static final int A_THANG = 1, B_THANG = 2, HOA = 3;

    /** Ghi ket qua mot tran vao hai doi (diem Thuy Si + da gap). */
    public static void ghiTran(final Doi a, final Doi b, final int kq) {
        a.daGap.add(Integer.valueOf(b.id));
        b.daGap.add(Integer.valueOf(a.id));
        if (kq == A_THANG) {
            a.diem += DIEM_THANG;
            a.thang++;
            b.thua++;
        } else if (kq == B_THANG) {
            b.diem += DIEM_THANG;
            b.thang++;
            a.thua++;
        } else {
            a.diem += DIEM_HOA;
            b.diem += DIEM_HOA;
            a.hoa++;
            b.hoa++;
        }
    }

    public static void ghiMien(final Doi d) {
        d.diem += DIEM_MIEN;
        d.daMien = true;
    }

    /** Het gio: ben con nhieu % HP (tong phan nghin cua doi) hon thang; bang nhau = hoa. */
    public static int ketQuaHetGio(final int phanNghinA, final int phanNghinB) {
        if (phanNghinA > phanNghinB) {
            return A_THANG;
        }
        return phanNghinB > phanNghinA ? B_THANG : HOA;
    }

    /** Buchholz: tong diem cac doi da gap. */
    public static int buchholz(final Doi d, final Map<Integer, Doi> theoId) {
        int s = 0;
        for (final Integer id : d.daGap) {
            final Doi x = theoId.get(id);
            if (x != null) {
                s += x.diem;
            }
        }
        return s;
    }

    /** Bang xep hang: diem, Buchholz, so tran thang, dang ky som hon. */
    public static List<Doi> xepHang(final List<Doi> ds) {
        final Map<Integer, Doi> theoId = new LinkedHashMap<Integer, Doi>();
        for (final Doi d : ds) {
            theoId.put(Integer.valueOf(d.id), d);
        }
        final List<Doi> kq = new ArrayList<Doi>(ds);
        Collections.sort(kq, (a, b) -> {
            if (a.diem != b.diem) {
                return Integer.compare(b.diem, a.diem);
            }
            final int ba = buchholz(a, theoId);
            final int bb = buchholz(b, theoId);
            if (ba != bb) {
                return Integer.compare(bb, ba);
            }
            if (a.thang != b.thang) {
                return Integer.compare(b.thang, a.thang);
            }
            return Integer.compare(a.thuTuDangKy, b.thuTuDangKy);
        });
        return kq;
    }

    // ================================================================== thuong

    /** Thuong mot nguoi: luong khoa, id danh hieu (-1 khong co), thoi han danh hieu (phut). */
    public static final class Thuong {
        public final int luongKhoa, danhHieu, phut;

        Thuong(final int luongKhoa, final int danhHieu, final int phut) {
            this.luongKhoa = luongKhoa;
            this.danhHieu = danhHieu;
            this.phut = phut;
        }
    }

    /**
     * Thuong theo hang (1..). Hang 4-8 va "tham gia" can da dau &gt;= {@link #TRAN_THAM_GIA_TOI_THIEU} tran va thang
     * &gt;= 1 (treo may / thua het khong nhan). Vo dich / a quan van can da dau it nhat 1 tran.
     */
    public static Thuong thuong(final Loai l, final int hang, final int soTran, final int soThang) {
        if (hang <= 0 || soTran <= 0) {
            return new Thuong(0, -1, 0);
        }
        if (hang == 1) {
            return new Thuong(LK_VO_DICH, l.danhHieuVoDich(), PHUT_DH_VO_DICH);
        }
        if (hang <= 3) {
            return new Thuong(LK_HANG_2_3, DH_A_QUAN, PHUT_DH_A_QUAN);
        }
        final boolean du = soTran >= TRAN_THAM_GIA_TOI_THIEU && soThang >= 1;
        if (!du) {
            return new Thuong(0, -1, 0);
        }
        if (hang <= 8) {
            return new Thuong(LK_HANG_4_8, DH_DAU_SI_CUP, PHUT_DH_DAU_SI);
        }
        return new Thuong(LK_THAM_GIA, DH_DAU_SI_CUP, PHUT_DH_DAU_SI);
    }

    // ================================================================== chuan hoa sat thuong

    /**
     * Sat thuong mot don trong tran cup. {@code thoPvp} = sat thuong PvP tho cua nguoi danh (truoc tran 8% HP muc
     * tieu); ti le = thoPvp / HP toi da nguoi danh (phan nghin), kep [min, max] cua loai cup, nhan HP toi da muc tieu.
     */
    public static int satThuong(final long thoPvp, final int maxHpDanh, final int maxHpNhan, final int phanNghinMin,
            final int phanNghinMax) {
        final long pn = Math.max(phanNghinMin, Math.min(phanNghinMax, Math.max(0L, thoPvp) * 1000L / Math.max(1, maxHpDanh)));
        return (int) Math.max(1L, (long) Math.max(1, maxHpNhan) * pn / 1000L);
    }

    /** Phan nghin HP con lai (0..1000). */
    public static int phanNghinHp(final int hp, final int maxHp) {
        return (int) Math.max(0L, Math.min(1000L, (long) Math.max(0, hp) * 1000L / Math.max(1, maxHp)));
    }

    // ================================================================== chu cho web / thong bao

    public static String tenHe(final int he) {
        return he >= 0 && he < HePokemon.TEN.length ? HePokemon.TEN[he] : "?";
    }

    public static String phanThuongChu() {
        return "Vô địch: danh hiệu 28 ngày + " + LK_VO_DICH + " lượng khoá; hạng 2–3: danh hiệu Á quân 14 ngày + "
                + LK_HANG_2_3 + " lượng khoá; hạng 4–8: " + LK_HANG_4_8 + " lượng khoá; tham gia đủ "
                + TRAN_THAM_GIA_TOI_THIEU + " trận và thắng ít nhất 1: " + LK_THAM_GIA + " lượng khoá. Không có xu.";
    }

    public static String lichChu(final int gioVongDau, final int soVong, final int phutMoiVong) {
        return "Đăng ký từ thứ Hai 04:00 tới Chủ nhật " + (gioVongDau - 1) + ":" + (60 - PHUT_DONG_DANG_KY) + "; Chủ nhật "
                + gioVongDau + ":00 bắt đầu " + soVong + " vòng Thuỵ Sĩ, mỗi " + phutMoiVong + " phút một vòng, trận tối đa "
                + (GIAY_TRAN / 60) + " phút. Hệ thống tự ghép đối thủ cùng điểm.";
    }
}
