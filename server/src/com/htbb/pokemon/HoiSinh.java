package com.htbb.pokemon;

import com.htbb.cauhinh.CauHinhTrungTam;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GUC / HOI SINH (18/10/2026, docs/thiet-ke-game.md). Truoc day hoi sinh gan nhu mien phi: duoi cap 50 tu hoi
 * sinh tai cho sau vai giay o moi bai luyen, "Auto ve Map Up" dua ve lang roi tu bay lai cho cu, phi xu tai cho = 5%
 * gio thu nhap - treo may danh mai khong dung. Nay khi Pokemon guc, nguoi choi CHON:
 * <ol>
 *   <li>Hoi sinh tai cho: dung Thuoc hoi sinh (30% HP) / Thuoc hoi sinh toi da (100% HP), hoac tra xu theo cap
 *       (~0,4 gio thu nhap, tang theo so lan guc trong 30 phut x1 / x1,5 / x2 ...), hoac luong khoa (co tran).
 *       Co thoi gian cho sau khi guc + hoi chieu giua hai lan; cam o san PvP / Gym / pha boss / Me cung (cau hinh).</li>
 *   <li>Ve Trung tam Pokemon gan nhat cua VUNG hien tai (Hoenn -> thi tran Hoenn gan nhat; Kanto/Johto -> lang nha
 *       cua phe): mien phi, phat nhe - mat mot phan EXP cua cap hien tai (KHONG BAO GIO tut cap), mat do ben trang bi,
 *       "Met moi" (-X% EXP va tan cong trong N phut). Khong rot do.</li>
 *   <li>Cho dong doi hoi sinh: dong doi o gan co Thuoc hoi sinh duoc moi cuu trong 60 giay.</li>
 * </ol>
 * Tan thu (duoi cap {@link CauHinh#tanThuCap}): khong mat EXP / do ben / met moi, hoi sinh tai cho mien phi (co hoi
 * chieu). Tu dong danh: guc -> dung tu dong; chi tu dung vat pham hoi sinh khi nguoi choi bat "Tu hoi sinh" (tran
 * tu_dong.hoi_sinh_moi_gio), con lai ve Trung tam va DUNG (khong treo may vo han).
 *
 * Lop THUAN (khong phu thuoc real.*): test/KiemHoiSinh.java goi truc tiep, tools/pokemon/MoPhongKinhTe dung cung ham
 * tinh phi; game goi qua real.HoiSinhGame. Moi so doc tu cau hinh trung tam (nhom "hoi_sinh", ap dung ngay).
 */
public final class HoiSinh {

    private HoiSinh() {
    }

    // ================================================================== khoa cau hinh (tools/luu_tru_sinh_du_lieu/cauhinh/danh_muc.json)

    public static final String K_BAT = "hoi_sinh.bat";
    public static final String K_XU_PHAN_TRAM_GIO = "hoi_sinh.xu_phan_tram_gio";
    public static final String K_XU_TOI_THIEU = "hoi_sinh.xu_toi_thieu";
    public static final String K_HE_SO_LAP = "hoi_sinh.he_so_lap";
    public static final String K_CUA_SO_PHUT = "hoi_sinh.cua_so_phut";
    public static final String K_LUONG_KHOA = "hoi_sinh.luong_khoa";
    public static final String K_LUONG_KHOA_TOI_DA = "hoi_sinh.luong_khoa_toi_da";
    public static final String K_CHO_SAU_GUC = "hoi_sinh.cho_sau_guc_giay";
    public static final String K_HOI_CHIEU = "hoi_sinh.hoi_chieu_giay";
    public static final String K_HP_THUOC = "hoi_sinh.hp_thuoc";
    public static final String K_HP_TRA_PHI = "hoi_sinh.hp_tra_phi";
    public static final String K_EXP_MAT = "hoi_sinh.exp_mat_phan_nghin";
    public static final String K_EXP_MAT_TOI_DA_PHUT = "hoi_sinh.exp_mat_toi_da_phut";
    public static final String K_DO_BEN = "hoi_sinh.do_ben_mat_phan_tram";
    public static final String K_MET_PHUT = "hoi_sinh.met_moi_phut";
    public static final String K_MET_EXP = "hoi_sinh.met_moi_exp";
    public static final String K_MET_TAN_CONG = "hoi_sinh.met_moi_tan_cong";
    public static final String K_TAN_THU_CAP = "hoi_sinh.tan_thu_cap";
    public static final String K_TAN_THU_HOI_CHIEU = "hoi_sinh.tan_thu_hoi_chieu_giay";
    public static final String K_DONG_DOI_BAT = "hoi_sinh.dong_doi_bat";
    public static final String K_DONG_DOI_GIAY = "hoi_sinh.dong_doi_giay";
    public static final String K_CAM_PVP = "hoi_sinh.cam_pvp";
    public static final String K_CAM_GYM = "hoi_sinh.cam_gym";
    public static final String K_CAM_BOSS = "hoi_sinh.cam_boss";
    public static final String K_CAM_ME_CUNG = "hoi_sinh.cam_me_cung";
    public static final String K_MAP_PVP = "hoi_sinh.map_pvp";
    public static final String K_MAP_CAM_THEM = "hoi_sinh.map_cam_them";
    public static final String K_AUTO_VE_MAP_UP = "hoi_sinh.auto_ve_map_up";
    public static final String K_GIA_THUOC = "hoi_sinh.gia_thuoc_phan_tram_gio";
    public static final String K_GIA_THUOC_TOI_THIEU = "hoi_sinh.gia_thuoc_toi_thieu";
    public static final String K_THUOC_MOI_GIO = "hoi_sinh.thuoc_moi_gio";

    // ================================================================== mac dinh (khop danh_muc.json)

    public static final int XU_PHAN_TRAM_GIO = 40;
    public static final int XU_TOI_THIEU = 200;
    public static final int[] HE_SO_LAP = {100, 150, 200, 300};
    public static final int CUA_SO_PHUT = 30;
    public static final int LUONG_KHOA = 2;
    public static final int LUONG_KHOA_TOI_DA = 10;
    public static final int CHO_SAU_GUC = 5;
    public static final int HOI_CHIEU = 60;
    public static final int HP_THUOC = 30;
    public static final int HP_TRA_PHI = 50;
    public static final int EXP_MAT_PHAN_NGHIN = 20;
    public static final int EXP_MAT_TOI_DA_PHUT = 15;
    public static final int DO_BEN_PHAN_TRAM = 2;   // 31/10/2026 chu game: 5 -> 2 (sau khi dang nhap lai khong con hoi day do ben)
    public static final int MET_PHUT = 5;
    public static final int MET_EXP = 20;
    public static final int MET_TAN_CONG = 10;
    public static final int TAN_THU_CAP = 15;
    public static final int TAN_THU_HOI_CHIEU = 30;
    public static final int DONG_DOI_GIAY = 60;
    public static final int[] MAP_PVP = {202, 203, 204, 205, 227, 228};
    /** Gia Thuoc hoi sinh o Chansey: % gio thu nhap o cap NGUOI MUA (cung bac voi phi xu tai cho), toi thieu = gia cu. */
    public static final int GIA_THUOC_PHAN_TRAM_GIO = 30;
    public static final int GIA_THUOC_TOI_THIEU = 407;
    /** Toi da lan hoi sinh tai cho bang vat pham CUA MINH / 60 phut (tay + tu dong + tu tui). 0 = khong gioi han. */
    public static final int THUOC_MOI_GIO = 6;

    /** Vat pham: Thuoc hoi sinh (potions[119], 30% HP) va Thuoc hoi sinh toi da (data_item 1207, 100% HP). */
    public static final int THUOC_HOI_SINH = 119;
    public static final int ITEM_HOI_SINH_TOI_DA = 1207;

    public static final class CauHinh {
        public boolean bat = true;
        public int xuPhanTramGio = XU_PHAN_TRAM_GIO;
        public int xuToiThieu = XU_TOI_THIEU;
        public int[] heSoLap = HE_SO_LAP.clone();
        public int cuaSoPhut = CUA_SO_PHUT;
        public int luongKhoa = LUONG_KHOA;
        public int luongKhoaToiDa = LUONG_KHOA_TOI_DA;
        public int choSauGuc = CHO_SAU_GUC;
        public int hoiChieu = HOI_CHIEU;
        public int hpThuoc = HP_THUOC;
        public int hpTraPhi = HP_TRA_PHI;
        public int expMatPhanNghin = EXP_MAT_PHAN_NGHIN;
        public int expMatToiDaPhut = EXP_MAT_TOI_DA_PHUT;
        public int doBen = DO_BEN_PHAN_TRAM;
        public int metPhut = MET_PHUT;
        public int metExp = MET_EXP;
        public int metTanCong = MET_TAN_CONG;
        public int tanThuCap = TAN_THU_CAP;
        public int tanThuHoiChieu = TAN_THU_HOI_CHIEU;
        public boolean dongDoiBat = true;
        public int dongDoiGiay = DONG_DOI_GIAY;
        public boolean camPvp = true;
        public boolean camGym = true;
        public boolean camBoss = true;
        public boolean camMeCung = true;
        public int[] mapPvp = MAP_PVP.clone();
        public int[] mapCamThem = new int[0];
        public boolean autoVeMapUp = false;
        public int giaThuocPhanTramGio = GIA_THUOC_PHAN_TRAM_GIO;
        public int giaThuocToiThieu = GIA_THUOC_TOI_THIEU;
        public int thuocMoiGio = THUOC_MOI_GIO;
    }

    static long kep(final long v, final long lo, final long hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    static int[] dsSo(final String s, final int[] macDinh) {
        if (s == null) {
            return macDinh.clone();
        }
        final List<Integer> ra = new ArrayList<Integer>();
        for (final String x : s.split(",")) {
            final String t = x.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                ra.add(Integer.valueOf(Integer.parseInt(t)));
            } catch (final NumberFormatException e) {
                return macDinh.clone();
            }
        }
        final int[] a = new int[ra.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = ra.get(i).intValue();
        }
        return a;
    }

    /** Doc cau hinh trung tam (ap dung ngay, khong cache - CauHinhTrungTam doc tu anh chup trong bo nho). */
    public static CauHinh cauHinh() {
        final CauHinh c = new CauHinh();
        try {
            c.bat = !"0".equals(CauHinhTrungTam.chuoi(K_BAT).trim());
            c.xuPhanTramGio = (int) kep(CauHinhTrungTam.so(K_XU_PHAN_TRAM_GIO, XU_PHAN_TRAM_GIO), 0, 1000);
            c.xuToiThieu = (int) kep(CauHinhTrungTam.so(K_XU_TOI_THIEU, XU_TOI_THIEU), 0, 100000000);
            final int[] hs = dsSo(CauHinhTrungTam.chuoi(K_HE_SO_LAP), HE_SO_LAP);
            c.heSoLap = hs.length == 0 ? HE_SO_LAP.clone() : hs;
            c.cuaSoPhut = (int) kep(CauHinhTrungTam.so(K_CUA_SO_PHUT, CUA_SO_PHUT), 1, 1440);
            c.luongKhoa = (int) kep(CauHinhTrungTam.so(K_LUONG_KHOA, LUONG_KHOA), 0, 1000);
            c.luongKhoaToiDa = (int) kep(CauHinhTrungTam.so(K_LUONG_KHOA_TOI_DA, LUONG_KHOA_TOI_DA), 0, 1000);
            c.choSauGuc = (int) kep(CauHinhTrungTam.so(K_CHO_SAU_GUC, CHO_SAU_GUC), 0, 600);
            c.hoiChieu = (int) kep(CauHinhTrungTam.so(K_HOI_CHIEU, HOI_CHIEU), 0, 3600);
            c.hpThuoc = (int) kep(CauHinhTrungTam.so(K_HP_THUOC, HP_THUOC), 1, 100);
            c.hpTraPhi = (int) kep(CauHinhTrungTam.so(K_HP_TRA_PHI, HP_TRA_PHI), 1, 100);
            c.expMatPhanNghin = (int) kep(CauHinhTrungTam.so(K_EXP_MAT, EXP_MAT_PHAN_NGHIN), 0, 200);
            c.expMatToiDaPhut = (int) kep(CauHinhTrungTam.so(K_EXP_MAT_TOI_DA_PHUT, EXP_MAT_TOI_DA_PHUT), 0, 600);
            c.doBen = (int) kep(CauHinhTrungTam.so(K_DO_BEN, DO_BEN_PHAN_TRAM), 0, 100);
            c.metPhut = (int) kep(CauHinhTrungTam.so(K_MET_PHUT, MET_PHUT), 0, 120);
            c.metExp = (int) kep(CauHinhTrungTam.so(K_MET_EXP, MET_EXP), 0, 90);
            c.metTanCong = (int) kep(CauHinhTrungTam.so(K_MET_TAN_CONG, MET_TAN_CONG), 0, 90);
            c.tanThuCap = (int) kep(CauHinhTrungTam.so(K_TAN_THU_CAP, TAN_THU_CAP), 0, 100);
            c.tanThuHoiChieu = (int) kep(CauHinhTrungTam.so(K_TAN_THU_HOI_CHIEU, TAN_THU_HOI_CHIEU), 0, 3600);
            c.dongDoiBat = !"0".equals(CauHinhTrungTam.chuoi(K_DONG_DOI_BAT).trim());
            c.dongDoiGiay = (int) kep(CauHinhTrungTam.so(K_DONG_DOI_GIAY, DONG_DOI_GIAY), 10, 600);
            c.camPvp = !"0".equals(CauHinhTrungTam.chuoi(K_CAM_PVP).trim());
            c.camGym = !"0".equals(CauHinhTrungTam.chuoi(K_CAM_GYM).trim());
            c.camBoss = !"0".equals(CauHinhTrungTam.chuoi(K_CAM_BOSS).trim());
            c.camMeCung = !"0".equals(CauHinhTrungTam.chuoi(K_CAM_ME_CUNG).trim());
            c.mapPvp = dsSo(CauHinhTrungTam.chuoi(K_MAP_PVP), MAP_PVP);
            c.mapCamThem = dsSo(CauHinhTrungTam.chuoi(K_MAP_CAM_THEM), new int[0]);
            c.autoVeMapUp = CauHinhTrungTam.co(K_AUTO_VE_MAP_UP);
            c.giaThuocPhanTramGio = (int) kep(CauHinhTrungTam.so(K_GIA_THUOC, GIA_THUOC_PHAN_TRAM_GIO), 0, 1000);
            c.giaThuocToiThieu = (int) kep(CauHinhTrungTam.so(K_GIA_THUOC_TOI_THIEU, GIA_THUOC_TOI_THIEU), 0, 100000000);
            c.thuocMoiGio = (int) kep(CauHinhTrungTam.so(K_THUOC_MOI_GIO, THUOC_MOI_GIO), 0, 100);
        } catch (final Exception e) {
            // giu mac dinh
        }
        return c;
    }

    static boolean co(final int[] a, final int v) {
        for (final int x : a) {
            if (x == v) {
                return true;
            }
        }
        return false;
    }

    // ================================================================== phi

    /** He so (%) cho lan guc thu {@code lan} (1 = lan dau) trong cua so: x1, x1,5, x2 ... chan o phan tu cuoi. */
    public static int heSoLap(final CauHinh c, final int lan) {
        final int[] h = c.heSoLap.length == 0 ? HE_SO_LAP : c.heSoLap;
        return Math.max(0, h[Math.max(0, Math.min(h.length - 1, lan - 1))]);
    }

    /** Tan thu (khong mat EXP, hoi sinh tai cho mien phi). */
    public static boolean tanThu(final CauHinh c, final int cap) {
        return cap < c.tanThuCap;
    }

    /** Phi xu hoi sinh tai cho o {@code cap}, lan guc thu {@code lan}: max(toi thieu, % gio thu nhap) x he so lap. */
    public static long phiXu(final CauHinh c, final int cap, final int lan) {
        final long goc = Math.max(c.xuToiThieu, LauDai.xuMoiGio(cap) * c.xuPhanTramGio / 100L);
        return goc * heSoLap(c, lan) / 100L;
    }

    /** Phi luong khoa: goc x he so lap (lam tron len), chan {@link CauHinh#luongKhoaToiDa}. 0 = tat lua chon. */
    public static int phiLuongKhoa(final CauHinh c, final int lan) {
        if (c.luongKhoa <= 0) {
            return 0;
        }
        final long p = ((long) c.luongKhoa * heSoLap(c, lan) + 99L) / 100L;
        return (int) Math.max(1L, Math.min(Math.max(1, c.luongKhoaToiDa), p));
    }

    /** Gia 1 Thuoc hoi sinh o cua hang (xu) cho nguoi mua cap {@code cap}: max(toi thieu, % gio thu nhap). */
    public static long giaThuoc(final CauHinh c, final int cap) {
        return Math.max(c.giaThuocToiThieu, LauDai.xuMoiGio(cap) * c.giaThuocPhanTramGio / 100L);
    }

    /** Con duoc dung vat pham hoi sinh tai cho khong (tran {@link CauHinh#thuocMoiGio} / 60 phut). */
    public static boolean conLuotThuoc(final CauHinh c, final int daDungTrongGio) {
        return c.thuocMoiGio <= 0 || daDungTrongGio < c.thuocMoiGio;
    }

    // ================================================================== phat khi ve Trung tam

    /**
     * EXP mat khi ve Trung tam: phan nghin cua EXP len cap hien tai, chan {@code expMatToiDaPhut} phut thu nhap o cap
     * do (expMoiGio, 0 = khong chan) va chan boi TIEN DO TRONG CAP (exp - expDauCap): KHONG BAO GIO tut cap.
     *
     * @param exp      tong EXP hien tai
     * @param expDauCap tong EXP toi thieu cua cap hien tai (LevelDetail.expMain[cap-1])
     * @param expLenCap EXP can de len cap tiep (LevelDetail.expRequire[cap])
     * @param expMoiGio EXP 1 gio danh quai o cap nay (ThongSoLauDai.expMoiGio), <= 0 = khong chan theo phut
     */
    public static long expMat(final CauHinh c, final int cap, final long exp, final long expDauCap, final long expLenCap,
            final long expMoiGio) {
        if (tanThu(c, cap) || c.expMatPhanNghin <= 0 || expLenCap <= 0L) {
            return 0L;
        }
        long mat = expLenCap / 1000L * c.expMatPhanNghin + expLenCap % 1000L * c.expMatPhanNghin / 1000L;
        if (expMoiGio > 0L && c.expMatToiDaPhut > 0) {
            mat = Math.min(mat, expMoiGio * c.expMatToiDaPhut / 60L);
        }
        final long tienDo = Math.max(0L, exp - expDauCap);
        return Math.max(0L, Math.min(mat, tienDo));
    }

    /**
     * Do ben con lai sau khi ve Trung tam: tru {@code phanTram}% cua do ben toi da (lam tron len, it nhat 1 khi > 0),
     * khong xuong duoi 0, khong lam vo do (0 van la 0 - da hong thi sua o Xuong che tao).
     */
    public static int doBenSau(final int hienTai, final int toiDa, final int phanTram) {
        if (hienTai <= 0 || toiDa <= 0 || phanTram <= 0) {
            return Math.max(0, hienTai);
        }
        final int tru = Math.max(1, (int) (((long) toiDa * phanTram + 99L) / 100L));
        return Math.max(0, hienTai - tru);
    }

    /** He so % sau met moi (100 - giam), dung cho EXP / tan cong. */
    public static long apGiam(final long goc, final int giamPhanTram) {
        if (giamPhanTram <= 0 || goc <= 0L) {
            return goc;
        }
        return goc * (100L - Math.min(90, giamPhanTram)) / 100L;
    }

    // ================================================================== lich su guc (theo nhan vat)

    /** Lich su guc + hoi sinh cua mot nhan vat (dong bo tren chinh doi tuong). */
    public static final class LichSu {
        private final long[] guc = new long[16];
        private int n;
        public long lanTaiChoCuoi;
        public long gucLuc;
        private final long[] thuoc = new long[100];
        private int nThuoc;

        /** Ghi mot lan hoi sinh tai cho bang vat pham cua minh. */
        public synchronized void ghiThuoc(final long bay) {
            int giu = 0;
            for (int i = 0; i < this.nThuoc; i++) {
                if (bay - this.thuoc[i] < 3600000L) {
                    this.thuoc[giu++] = this.thuoc[i];
                }
            }
            this.nThuoc = giu;
            if (this.nThuoc == this.thuoc.length) {
                System.arraycopy(this.thuoc, 1, this.thuoc, 0, this.nThuoc - 1);
                this.nThuoc--;
            }
            this.thuoc[this.nThuoc++] = bay;
        }

        /** So lan hoi sinh bang vat pham trong 60 phut gan nhat. */
        public synchronized int thuocTrongGio(final long bay) {
            int d = 0;
            for (int i = 0; i < this.nThuoc; i++) {
                if (bay - this.thuoc[i] < 3600000L) {
                    d++;
                }
            }
            return d;
        }

        /** Ghi mot lan guc, tra ve so lan guc trong cua so (ke ca lan nay). */
        public synchronized int ghiGuc(final long bay, final long cuaSoMs) {
            this.gucLuc = bay;
            int giu = 0;
            for (int i = 0; i < this.n; i++) {
                if (bay - this.guc[i] < cuaSoMs) {
                    this.guc[giu++] = this.guc[i];
                }
            }
            this.n = giu;
            if (this.n == this.guc.length) {
                System.arraycopy(this.guc, 1, this.guc, 0, this.n - 1);
                this.n--;
            }
            this.guc[this.n++] = bay;
            return this.n;
        }

        /** So lan guc trong cua so tinh toi {@code bay} (khong ghi). */
        public synchronized int lanTrongCuaSo(final long bay, final long cuaSoMs) {
            int d = 0;
            for (int i = 0; i < this.n; i++) {
                if (bay - this.guc[i] < cuaSoMs) {
                    d++;
                }
            }
            return d;
        }

        /** Con lan guc nao trong cua so khong (de don bo nho). */
        public synchronized boolean conMoi(final long bay, final long cuaSoMs) {
            return lanTrongCuaSo(bay, cuaSoMs) > 0 || bay - this.lanTaiChoCuoi < 3600000L;
        }
    }

    // ================================================================== han che + lua chon

    /** Tinh huong guc (game dien vao), de lap danh sach lua chon. */
    public static final class TinhHuong {
        public int cap;
        public int lan = 1;
        public long gucLuc;
        public long lanTaiChoCuoi;
        public int soThuoc;
        public int soThuocToiDa;
        public int hpThuocToiDa = 100;
        public long xu;
        public long luongKhoa;
        /** Ly do cam hoi sinh tai cho (null = duoc). */
        public String camTaiCho;
        /** Co dong doi o gan con song mang vat pham hoi sinh. */
        public boolean coDongDoi;
        /** Da dung luot cho dong doi o lan guc nay. */
        public boolean daChoDongDoi;
        /** Dang cho dong doi toi luc (ms), 0 = khong. */
        public long choDongDoiDen;
        public String tenVung = "";
        /** So lan da hoi sinh bang vat pham cua minh trong 60 phut. */
        public int thuocTrongGio;
        /** Con luot vat pham (game dien = conLuotThuoc). */
        public boolean conLuotThuoc = true;
    }

    /** Ly do cam hoi sinh tai cho, null = duoc. */
    public static String lyDoCam(final CauHinh c, final int mapId, final boolean pvp, final boolean gym, final boolean boss,
            final boolean meCung) {
        if (c.camPvp && (pvp || co(c.mapPvp, mapId))) {
            return "Không hồi sinh tại chỗ ở sàn đấu / PvP.";
        }
        if (c.camGym && gym) {
            return "Không hồi sinh tại chỗ khi đang thách đấu Gym.";
        }
        if (c.camBoss && boss) {
            return "Không hồi sinh tại chỗ giữa trận boss.";
        }
        if (c.camMeCung && meCung) {
            return "Không hồi sinh tại chỗ trong Mê cung.";
        }
        if (co(c.mapCamThem, mapId)) {
            return "Bản đồ này không cho hồi sinh tại chỗ.";
        }
        return null;
    }

    public static final int LC_TAN_THU = 0, LC_THUOC = 1, LC_THUOC_TOI_DA = 2, LC_XU = 3, LC_LUONG_KHOA = 4,
            LC_TRUNG_TAM = 5, LC_DONG_DOI = 6;

    /** Mot dong trong hop thoai guc. */
    public static final class LuaChon {
        public final int ma;
        public final String ten;
        public final String chiTiet;
        /** null = bam duoc (sau khi het {@link #choGiay}); khac null = khoa, ly do hien cho nguoi choi. */
        public final String khoa;
        /** > 0: con cho ngan nay giay moi bam duoc (dem nguoc). */
        public final int choGiay;
        public final long phi;

        LuaChon(final int ma, final String ten, final String chiTiet, final String khoa, final int choGiay, final long phi) {
            this.ma = ma;
            this.ten = ten;
            this.chiTiet = chiTiet;
            this.khoa = khoa;
            this.choGiay = Math.max(0, choGiay);
            this.phi = phi;
        }

        public boolean bamDuoc() {
            return this.khoa == null && this.choGiay <= 0;
        }
    }

    /** Giay con cho truoc khi duoc hoi sinh tai cho (cho sau guc + hoi chieu giua hai lan tai cho). */
    public static int choTaiCho(final CauHinh c, final TinhHuong t, final long bay) {
        final boolean tt = tanThu(c, t.cap);
        final long sauGuc = t.gucLuc + c.choSauGuc * 1000L - bay;
        final long hc = t.lanTaiChoCuoi <= 0L ? 0L : t.lanTaiChoCuoi + (tt ? c.tanThuHoiChieu : c.hoiChieu) * 1000L - bay;
        final long ms = Math.max(sauGuc, hc);
        return ms <= 0L ? 0 : (int) ((ms + 999L) / 1000L);
    }

    /** Danh sach lua chon hien trong hop thoai (thu tu hien). */
    public static List<LuaChon> dsLuaChon(final CauHinh c, final TinhHuong t, final long bay) {
        final List<LuaChon> ds = new ArrayList<LuaChon>();
        final boolean tt = tanThu(c, t.cap);
        final int cho = choTaiCho(c, t, bay);
        final String cam = t.camTaiCho;
        final int heSo = heSoLap(c, t.lan);
        if (tt) {
            ds.add(new LuaChon(LC_TAN_THU, "Hồi sinh tại chỗ (tân thủ)", "Miễn phí dưới cấp " + c.tanThuCap + " · "
                    + c.hpTraPhi + "% HP", cam, cho, 0L));
        }
        final String hetLuot = conLuotThuoc(c, t.thuocTrongGio) ? null
                : "Đã dùng " + c.thuocMoiGio + " vật phẩm hồi sinh trong 60 phút - trả phí hoặc về Trung tâm.";
        ds.add(new LuaChon(LC_THUOC, "Dùng Thuốc hồi sinh", "Còn " + t.soThuoc + " · " + c.hpThuoc + "% HP",
                cam != null ? cam : (t.soThuoc <= 0 ? "Không có Thuốc hồi sinh (Sổ tay - Cửa hàng, " + so(giaThuoc(c, t.cap)) + " xu/viên)." : hetLuot), cho, 0L));
        ds.add(new LuaChon(LC_THUOC_TOI_DA, "Dùng Thuốc hồi sinh tối đa", "Còn " + t.soThuocToiDa + " · "
                + t.hpThuocToiDa + "% HP", cam != null ? cam : (t.soThuocToiDa <= 0 ? "Không có Thuốc hồi sinh tối đa." : hetLuot),
                cho, 0L));
        if (!tt) {
            final long px = phiXu(c, t.cap, t.lan);
            ds.add(new LuaChon(LC_XU, "Trả xu hồi sinh tại chỗ", so(px) + " xu" + (heSo != 100 ? " (×" + heSoChu(heSo) + ")" : "")
                    + " · " + c.hpTraPhi + "% HP",
                    cam != null ? cam : (t.xu < px ? "Thiếu " + so(px - t.xu) + " xu." : null), cho, px));
            final int pl = phiLuongKhoa(c, t.lan);
            if (pl > 0) {
                ds.add(new LuaChon(LC_LUONG_KHOA, "Trả lượng khoá hồi sinh tại chỗ", pl + " lượng khoá · " + c.hpTraPhi + "% HP",
                        cam != null ? cam : (t.luongKhoa < pl ? "Thiếu " + (pl - t.luongKhoa) + " lượng khoá." : null), cho, pl));
            }
        }
        ds.add(new LuaChon(LC_TRUNG_TAM, "Về Trung tâm Pokémon" + (t.tenVung.isEmpty() ? "" : " (" + t.tenVung + ")"),
                tt ? "Miễn phí · tân thủ không bị phạt" : "Miễn phí · −" + phanTramChu(c.expMatPhanNghin) + " EXP cấp, −"
                        + c.doBen + "% độ bền, Mệt mỏi " + c.metPhut + " phút", null, 0, 0L));
        if (c.dongDoiBat) {
            final int conCho = t.choDongDoiDen > bay ? (int) ((t.choDongDoiDen - bay + 999L) / 1000L) : 0;
            final String k;
            if (conCho > 0) {
                k = "Đang chờ đồng đội hồi sinh...";
            } else if (t.daChoDongDoi) {
                k = "Đã chờ đồng đội ở lần gục này.";
            } else if (!t.coDongDoi) {
                k = "Không có đồng đội ở gần mang Thuốc hồi sinh.";
            } else {
                k = null;
            }
            ds.add(new LuaChon(LC_DONG_DOI, "Chờ đồng đội hồi sinh", "Đồng đội có " + c.dongDoiGiay + " giây để cứu bạn",
                    k, conCho, 0L));
        }
        return ds;
    }

    /** Dong mo ta duoi tieu de hop thoai. */
    public static String moTa(final CauHinh c, final TinhHuong t) {
        if (tanThu(c, t.cap)) {
            return "Tân thủ: hồi sinh tại chỗ miễn phí, về Trung tâm không bị phạt.";
        }
        final int hs = heSoLap(c, t.lan);
        if (t.lan <= 1) {
            return "Gục lần đầu trong " + c.cuaSoPhut + " phút.";
        }
        return "Gục lần " + t.lan + " trong " + c.cuaSoPhut + " phút: phí tại chỗ ×" + heSoChu(hs) + ".";
    }

    /** 150 -> "1,5"; 200 -> "2". */
    public static String heSoChu(final int phanTram) {
        final int nguyen = phanTram / 100, le = phanTram % 100;
        if (le == 0) {
            return String.valueOf(nguyen);
        }
        return nguyen + "," + (le % 10 == 0 ? String.valueOf(le / 10) : (le < 10 ? "0" + le : String.valueOf(le)));
    }

    /** 20 phan nghin -> "2%"; 5 -> "0,5%". */
    public static String phanTramChu(final int phanNghin) {
        return (phanNghin % 10 == 0 ? String.valueOf(phanNghin / 10) : (phanNghin / 10) + "," + (phanNghin % 10)) + "%";
    }

    /** 1234567 -> "1.234.567". */
    public static String so(final long v) {
        final String s = String.valueOf(Math.abs(v));
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return (v < 0 ? "-" : "") + b;
    }

    /**
     * Tu dong danh guc: vat pham nen tu dung (LC_THUOC / LC_THUOC_TOI_DA, uu tien thuoc re) hoac LC_TRUNG_TAM (dung tu
     * dong + ve Trung tam). Chua tinh tran tu_dong.hoi_sinh_moi_gio - game hoi TuDongGame.choHoiSinhTuDong SAU khi
     * ham nay chon vat pham (de khong ton luot khi khong co vat pham).
     */
    public static int chonTuDong(final boolean xinTuHoiSinh, final TinhHuong t) {
        return chonTuDong(xinTuHoiSinh, t, true);
    }

    /**
     * Nhu tren, nhung {@code choTraPhi} = false thi KHONG BAO GIO tu dung Thuoc hoi sinh toi da (vat pham 1207 mua
     * bang luong khoa): chi Thuoc hoi sinh thuong (potion 119, mua bang xu). Game (real.HoiSinhGame) truyen
     * tu_dong.hoi_sinh_tra_phi=1 VA nguoi choi dong y rieng trong bang Tu dong ("Tu hoi sinh: ca vat pham luong").
     * Khong bao gio tra xu / luong (LC_XU / LC_LUONG) thay nguoi choi.
     */
    public static int chonTuDong(final boolean xinTuHoiSinh, final TinhHuong t, final boolean choTraPhi) {
        if (!xinTuHoiSinh || t.camTaiCho != null || !t.conLuotThuoc) {
            return LC_TRUNG_TAM;
        }
        if (t.soThuoc > 0) {
            return LC_THUOC;
        }
        if (choTraPhi && t.soThuocToiDa > 0) {
            return LC_THUOC_TOI_DA;
        }
        return LC_TRUNG_TAM;
    }

    // ================================================================== vung + Trung tam Pokemon

    public static final int VUNG_CU = 0, VUNG_HOENN = 2;

    /** Vung cua ban do (id nap): Hoenn hay the gioi cu (Kanto/Johto - lang nha cua phe). */
    public static int vung(final int idNap) {
        return BanDoHoenn.laHoenn(idNap) ? VUNG_HOENN : VUNG_CU;
    }

    /** Kiem o di duoc (toa do o) - game: Map.canMove, test: tep cMap. */
    public interface DiDuoc {
        boolean o(int map, int x, int y);
    }

    /** NPC loai 0 = Chansey (ban HP, du_lieu/bang_npc.csv) = Trung tam Pokemon cua thi tran. */
    public static final int NPC_TRUNG_TAM = 0;

    /** Ban do Hoenn co Trung tam Pokemon (thi tran co NPC loai 0). */
    public static boolean coTrungTam(final BanDoHoenn.BanDo b) {
        if (b == null || !"thi_tran".equals(b.loai)) {
            return false;
        }
        for (final int[] n : b.npc) {
            if (n[2] == NPC_TRUNG_TAM) {
                return true;
            }
        }
        return false;
    }

    /** Thi tran Hoenn co Trung tam gan nhat theo so lan qua cong (BFS) tu {@code tuMap}; khong phai Hoenn = -1. */
    public static int trungTamHoennGanNhat(final int tuMap) {
        if (!BanDoHoenn.laHoenn(tuMap)) {
            return -1;
        }
        final Set<Integer> da = new HashSet<Integer>();
        final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
        q.add(Integer.valueOf(tuMap));
        da.add(Integer.valueOf(tuMap));
        while (!q.isEmpty()) {
            final int id = q.poll().intValue();
            final BanDoHoenn.BanDo b = BanDoHoenn.lay(id);
            if (coTrungTam(b)) {
                return id;
            }
            if (b == null) {
                continue;
            }
            for (final BanDoHoenn.Cong c : b.cong) {
                // cong ve Kanto (map 9) khong tinh: hoi sinh o Hoenn phai o lai Hoenn
                if (BanDoHoenn.laHoenn(c.den) && da.add(Integer.valueOf(c.den))) {
                    q.add(Integer.valueOf(c.den));
                }
            }
        }
        return BanDoHoenn.cuaNgo().id;
    }

    /**
     * Diem hoi sinh {map, x o, y o} o vung Hoenn: truoc NPC Trung tam (Chansey) cua thi tran gan nhat, tim o di duoc
     * quanh do (ban kinh 4); khong co thi o vao cong dau tien. null = khong phai Hoenn.
     */
    public static int[] diemTrungTamHoenn(final int tuMap, final DiDuoc dd) {
        final int id = trungTamHoennGanNhat(tuMap);
        if (id < 0) {
            return null;
        }
        final BanDoHoenn.BanDo b = BanDoHoenn.lay(id);
        for (final int[] n : b.npcTuyetDoi()) {
            if (n[2] != NPC_TRUNG_TAM) {
                continue;
            }
            for (int r = 0; r <= 4; r++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dx = -r; dx <= r; dx++) {
                        if (Math.max(Math.abs(dx), Math.abs(dy)) != r) {
                            continue;
                        }
                        // uu tien dung NGAY DUOI NPC (2 o) - nhu buoc ra khoi Trung tam
                        final int x = n[0] + dx, y = n[1] + 2 + dy;
                        if (x > 0 && y > 0 && x < b.w - 1 && y < b.h - 1 && (dd == null || dd.o(id, x, y))) {
                            return new int[]{id, x, y};
                        }
                    }
                }
            }
        }
        final int[] v = b.diemVao(0);
        return new int[]{id, v[0], v[1]};
    }
}
