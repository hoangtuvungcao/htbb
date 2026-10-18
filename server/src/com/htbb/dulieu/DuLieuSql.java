package com.htbb.dulieu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.BanDoSinhThai;
import com.htbb.pokemon.GiftcodeAnToanDuLieu;
import com.htbb.pokemon.SuKienLauDai;
import com.htbb.pokemon.ThongSoLauDai;

/**
 * DU LIEU THIET KE DOC TU SQL - nguon DUY NHAT (giai doan P1 cua ke hoach bo ma cu, docs/kien-truc.md muc 4.4).
 *
 * Truoc day may chu doc ban CHEP cung trong Java (ThongSoLauDai, SuKienLauDai.LICH, DanhMucSinh, diem cau ca tinh
 * lai tu ban do, danh sach trang bi giftcode) trong khi CSDL co dung bang do. Nay may chu nap bang SQL luc khoi
 * dong ({@code data.DatabaseDuLieuSql}, ngay sau kiemTraCSDL), test va cong cu nap tu tep {@code sql/htbb_day_du.sql}
 * ({@link DocDumpSql}). Lop nay THUAN: chi doi {@link BangSql} -> cau truc Java va kiem hinh dang bang (so dong,
 * khoa lien tuc, gia tri dong nhat giua cac bac...). Sai hinh dang = loi ro rang, KHONG lang le dung so 0.
 *
 * Bang (danh sach trang - ten bang / cot ghep SQL chi lay o day):
 * <pre>
 *   data_cap_do_lau_dai      -> ThongSoLauDai.EXP_LEN_CAP / MOB_EXP / QUAI_MOI_GIO / XU_MOI_QUAI / GIO_TICH_LUY_PHAN_NGHIN
 *   data_nang_cap_lau_dai    -> ThongSoLauDai.TI_LE_NC / DA_NC / BUA_BAO_HO_LUONG_KHOA / CHI_SO_NC_PHAN_TRAM / CHI_PHI_NC
 *                               / BAC_TB_TEN / BAC_TB_CAP / MAT_CAP_TU
 *   data_kinh_te_lau_dai     -> ThongSoLauDai.TI_LE_ROI_XU, THUE_CHO_PHAN_TRAM, BAC_MET... (12 so, xem KHOA_KINH_TE)
 *   data_su_kien_nam         -> SuKienLauDai.LICH
 *   data_hst_ho_cau          -> BanDoSinhThai diem cau ca theo ban do
 *   data_noi_dung_trang_bi   -> GiftcodeAnToanLogic: trang bi phat duoc qua giftcode
 *   cau_hinh_danh_muc + cau_hinh_nhom -> com.htbb.cauhinh.CauHinhTrungTam (danh muc khoa cau hinh)
 * </pre>
 */
public final class DuLieuSql {

    private DuLieuSql() {
    }

    public static final String CAP_DO = "data_cap_do_lau_dai";
    public static final String NANG_CAP = "data_nang_cap_lau_dai";
    public static final String KINH_TE = "data_kinh_te_lau_dai";
    public static final String SU_KIEN_NAM = "data_su_kien_nam";
    public static final String HO_CAU = "data_hst_ho_cau";
    public static final String TRANG_BI = "data_noi_dung_trang_bi";

    public static final String[] COT_CAP_DO = {"cap", "exp_len_cap", "gio_tich_luy_phan_nghin", "mob_exp", "quai_moi_gio",
        "xu_moi_quai", "exp_cu"};
    public static final String[] COT_NANG_CAP = {"bac", "ten_bac", "cap_bac", "cong", "ti_le", "pity_moi_lan", "xu", "da",
        "bua_luong_khoa", "mat_cap", "chi_so_phan_tram"};
    public static final String[] COT_KINH_TE = {"khoa", "gia_tri"};
    public static final String[] COT_SU_KIEN_NAM = {"tuan", "so_ngay", "ma_su_kien"};
    public static final String[] COT_HO_CAU = {"map_id", "ho_id", "so_o", "x", "y"};
    public static final String[] COT_TRANG_BI = {"id_item"};

    /** So bac nang cap (+1..+15) moi bac trang bi. */
    public static final int SO_CONG = 15;

    // =================================================================== cap do

    /** Bang cap do: moi mang co chi so 0..capToiDa, [0] = 0 (giong ban Java cu). */
    public static final class CapDo {
        public final long[] expLenCap;
        public final int[] gioTichLuyPhanNghin;
        public final int[] mobExp;
        public final int[] quaiMoiGio;
        public final int[] xuMoiQuai;
        /** exp_cu: bang EXP game goc (real.LevelDetail.expRequire), chi de doi chieu. */
        public final long[] expCu;

        CapDo(final int n) {
            expLenCap = new long[n + 1];
            gioTichLuyPhanNghin = new int[n + 1];
            mobExp = new int[n + 1];
            quaiMoiGio = new int[n + 1];
            xuMoiQuai = new int[n + 1];
            expCu = new long[n + 1];
        }
    }

    public static CapDo docCapDo(final BangSql b, final int capToiDa) {
        if (b.soDong() != capToiDa) {
            throw new IllegalStateException(b.ten + ": can dung " + capToiDa + " dong (cap 1.." + capToiDa + "), co " + b.soDong());
        }
        final CapDo d = new CapDo(capToiDa);
        final boolean[] co = new boolean[capToiDa + 1];
        for (int r = 0; r < b.soDong(); r++) {
            final int cap = b.so(r, "cap");
            if (cap < 1 || cap > capToiDa || co[cap]) {
                throw new IllegalStateException(b.ten + ": cap " + cap + " sai hoac trung (dong " + (r + 1) + ")");
            }
            co[cap] = true;
            d.expLenCap[cap] = b.soLong(r, "exp_len_cap");
            d.gioTichLuyPhanNghin[cap] = b.so(r, "gio_tich_luy_phan_nghin");
            d.mobExp[cap] = b.so(r, "mob_exp");
            d.quaiMoiGio[cap] = b.so(r, "quai_moi_gio");
            d.xuMoiQuai[cap] = b.so(r, "xu_moi_quai");
            d.expCu[cap] = b.soLong(r, "exp_cu");
        }
        for (int L = 1; L < capToiDa; L++) {
            if (d.expLenCap[L] <= 0 || d.mobExp[L] <= 0 || d.quaiMoiGio[L] <= 0) {
                throw new IllegalStateException(b.ten + ": cap " + L + " co exp_len_cap / mob_exp / quai_moi_gio <= 0");
            }
        }
        return d;
    }

    // =================================================================== nang cap

    /** Bang nang cap trang bi: {@code bac} 0..n-1, {@code cong} 1..15. Gia tri theo cong giong nhau o moi bac. */
    public static final class NangCap {
        public final String[] tenBac;
        public final int[] capBac;
        /** [k-1] cho lan len +k. */
        public final int[] tiLe = new int[SO_CONG];
        public final int[] pity = new int[SO_CONG];
        public final int[] da = new int[SO_CONG];
        public final int[] buaLuongKhoa = new int[SO_CONG];
        /** % chi so THEM o +k (bang luu luy ke; day la hieu). */
        public final int[] chiSoPhanTram = new int[SO_CONG];
        /** [bac][k], k = 0..15, [bac][0] = 0. */
        public final long[][] chiPhi;
        /** Truot tu +MAT_CAP_TU tro len thi tut cap. */
        public final int matCapTu;

        NangCap(final String[] tenBac, final int[] capBac, final long[][] chiPhi, final int matCapTu) {
            this.tenBac = tenBac;
            this.capBac = capBac;
            this.chiPhi = chiPhi;
            this.matCapTu = matCapTu;
        }
    }

    public static NangCap docNangCap(final BangSql b) {
        final TreeMap<Integer, Integer> dongTheoKhoa = new TreeMap<Integer, Integer>();
        int soBac = 0;
        for (int r = 0; r < b.soDong(); r++) {
            final int bac = b.so(r, "bac");
            final int cong = b.so(r, "cong");
            if (bac < 0 || bac > 100 || cong < 1 || cong > SO_CONG) {
                throw new IllegalStateException(b.ten + ": bac/cong sai (" + bac + ", " + cong + ") dong " + (r + 1));
            }
            if (dongTheoKhoa.put(bac * 100 + cong, r) != null) {
                throw new IllegalStateException(b.ten + ": trung (bac " + bac + ", cong " + cong + ")");
            }
            soBac = Math.max(soBac, bac + 1);
        }
        if (soBac == 0 || dongTheoKhoa.size() != soBac * SO_CONG) {
            throw new IllegalStateException(b.ten + ": can du " + SO_CONG + " cong cho moi bac 0.." + (soBac - 1)
                    + ", co " + dongTheoKhoa.size() + " dong");
        }
        final String[] ten = new String[soBac];
        final int[] cap = new int[soBac];
        final long[][] chiPhi = new long[soBac][SO_CONG + 1];
        int matCapTu = -1;
        final int[][] mau = new int[5][SO_CONG];
        for (int bac = 0; bac < soBac; bac++) {
            int luyKeTruoc = 0;
            for (int k = 1; k <= SO_CONG; k++) {
                final int r = dongTheoKhoa.get(bac * 100 + k).intValue();
                final String t = b.chu(r, "ten_bac");
                final int c = b.so(r, "cap_bac");
                if (k == 1) {
                    ten[bac] = t;
                    cap[bac] = c;
                } else if (!t.equals(ten[bac]) || c != cap[bac]) {
                    throw new IllegalStateException(b.ten + ": bac " + bac + " co ten_bac/cap_bac khac nhau giua cac cong");
                }
                chiPhi[bac][k] = b.soLong(r, "xu");
                final int luyKe = b.so(r, "chi_so_phan_tram");
                final int[] gt = {b.so(r, "ti_le"), b.so(r, "pity_moi_lan"), b.so(r, "da"), b.so(r, "bua_luong_khoa"),
                    luyKe - luyKeTruoc};
                luyKeTruoc = luyKe;
                for (int i = 0; i < gt.length; i++) {
                    if (bac == 0) {
                        mau[i][k - 1] = gt[i];
                    } else if (mau[i][k - 1] != gt[i]) {
                        throw new IllegalStateException(b.ten + ": cot " + new String[]{"ti_le", "pity_moi_lan", "da",
                            "bua_luong_khoa", "chi_so_phan_tram"}[i] + " cong " + k + " khac nhau giua bac 0 va bac " + bac);
                    }
                }
                final boolean mat = b.so(r, "mat_cap") != 0;
                if (bac == 0 && mat && matCapTu < 0) {
                    matCapTu = k;
                }
                if (mat != (matCapTu > 0 && k >= matCapTu)) {
                    throw new IllegalStateException(b.ten + ": cot mat_cap phai la 0 truoc +MAT_CAP_TU va 1 tu do (bac " + bac + ", cong " + k + ")");
                }
            }
        }
        final NangCap n = new NangCap(ten, cap, chiPhi, matCapTu < 0 ? SO_CONG + 1 : matCapTu);
        System.arraycopy(mau[0], 0, n.tiLe, 0, SO_CONG);
        System.arraycopy(mau[1], 0, n.pity, 0, SO_CONG);
        System.arraycopy(mau[2], 0, n.da, 0, SO_CONG);
        System.arraycopy(mau[3], 0, n.buaLuongKhoa, 0, SO_CONG);
        System.arraycopy(mau[4], 0, n.chiSoPhanTram, 0, SO_CONG);
        return n;
    }

    // =================================================================== kinh te

    /** khoa -> gia tri (chuoi tho). */
    public static Map<String, String> docKinhTe(final BangSql b) {
        final Map<String, String> m = new LinkedHashMap<String, String>();
        for (int r = 0; r < b.soDong(); r++) {
            if (m.put(b.chu(r, "khoa").trim(), b.chu(r, "gia_tri").trim()) != null) {
                throw new IllegalStateException(b.ten + ": trung khoa " + b.chu(r, "khoa"));
            }
        }
        return m;
    }

    /** So nguyen cua khoa {@code k}; thieu / sai = loi. */
    public static int soKinhTe(final Map<String, String> m, final String k) {
        final String v = m.get(k);
        if (v == null) {
            throw new IllegalStateException(KINH_TE + ": thieu khoa " + k);
        }
        try {
            return Integer.parseInt(v);
        } catch (final NumberFormatException e) {
            throw new IllegalStateException(KINH_TE + ": khoa " + k + " khong phai so nguyen '" + v + "'");
        }
    }

    /** "120:20,120:5" -> {{120,20},{120,5}}. */
    public static int[][] docBacMet(final String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalStateException(KINH_TE + ": exp.bac_met rong");
        }
        final String[] p = s.split(",");
        final int[][] ra = new int[p.length][2];
        for (int i = 0; i < p.length; i++) {
            final String[] ab = p[i].trim().split(":");
            if (ab.length != 2) {
                throw new IllegalStateException(KINH_TE + ": exp.bac_met sai dang '" + s + "' (phut:%,phut:%)");
            }
            ra[i][0] = Integer.parseInt(ab[0].trim());
            ra[i][1] = Integer.parseInt(ab[1].trim());
        }
        return ra;
    }

    // =================================================================== su kien nam

    /** Lich {tuan, so ngay, ma} cua cac tuan CO ma su kien (tuan thuong bo qua), theo tuan tang dan. */
    public static Object[][] docLichSuKien(final BangSql b) {
        final TreeMap<Integer, Object[]> m = new TreeMap<Integer, Object[]>();
        final TreeSet<Integer> tuan = new TreeSet<Integer>();
        for (int r = 0; r < b.soDong(); r++) {
            final int t = b.so(r, "tuan");
            if (t < 1 || t > 53 || !tuan.add(t)) {
                throw new IllegalStateException(b.ten + ": tuan " + t + " sai hoac trung");
            }
            final String ma = b.chu(r, "ma_su_kien").trim();
            if (ma.isEmpty()) {
                continue;
            }
            final int ngay = b.so(r, "so_ngay");
            if (ngay < 1 || ngay > 60) {
                throw new IllegalStateException(b.ten + ": tuan " + t + " so_ngay " + ngay + " sai");
            }
            m.put(t, new Object[]{Integer.valueOf(t), Integer.valueOf(ngay), ma});
        }
        if (tuan.size() < 52) {
            throw new IllegalStateException(b.ten + ": can 52 tuan, co " + tuan.size());
        }
        return m.values().toArray(new Object[0][]);
    }

    // =================================================================== diem cau ca

    /**
     * map_id -> danh sach {hoId, x, y, so o cua ho}; trong moi ban do: theo ho tang dan, trong moi ho theo thu tu
     * quet (y roi x) - dung thu tu BanDoSinhThai.diemCau cu tinh (diem gan nhat bang nhau thi diem dau thang).
     */
    public static Map<Integer, List<int[]>> docHoCau(final BangSql b) {
        final TreeMap<Integer, List<int[]>> m = new TreeMap<Integer, List<int[]>>();
        for (int r = 0; r < b.soDong(); r++) {
            m.computeIfAbsent(b.so(r, "map_id"), (k) -> new ArrayList<int[]>())
                    .add(new int[]{b.so(r, "ho_id"), b.so(r, "x"), b.so(r, "y"), b.so(r, "so_o")});
        }
        final Map<Integer, List<int[]>> ra = new HashMap<Integer, List<int[]>>();
        for (final Map.Entry<Integer, List<int[]>> e : m.entrySet()) {
            final List<int[]> ds = e.getValue();
            Collections.sort(ds, (p, q) -> p[0] != q[0] ? Integer.compare(p[0], q[0])
                    : (p[2] != q[2] ? Integer.compare(p[2], q[2]) : Integer.compare(p[1], q[1])));
            ra.put(e.getKey(), Collections.unmodifiableList(ds));
        }
        return Collections.unmodifiableMap(ra);
    }

    // =================================================================== trang bi giftcode

    /** id_item (> 0) khac nhau, tang dan. */
    public static int[] docTrangBiGiftcode(final BangSql b) {
        final TreeSet<Integer> s = new TreeSet<Integer>();
        for (int r = 0; r < b.soDong(); r++) {
            final long id = b.soHoac(r, "id_item", 0L);
            if (id > 0 && id <= Integer.MAX_VALUE) {
                s.add((int) id);
            }
        }
        final int[] a = new int[s.size()];
        int i = 0;
        for (final Integer x : s) {
            a[i++] = x.intValue();
        }
        return a;
    }

    // =================================================================== NAP + AP DUNG

    /** Ket qua mot lan nap: {@link #loi} (bang QUAN TRONG hong -> may chu dung), {@link #canhBao}, {@link #ok}. */
    public static final class KetQua {
        public final List<String> loi = new ArrayList<String>();
        public final List<String> canhBao = new ArrayList<String>();
        public final List<String> ok = new ArrayList<String>();

        public boolean dat() {
            return loi.isEmpty();
        }
    }

    interface ApDung {
        String chay(BangSql b);
    }

    /**
     * Doc mot bang, kiem, ap dung. Thieu bang / thieu cot / RONG / sai hinh dang: bang quan trong -> {@link KetQua#loi},
     * con lai -> {@link KetQua#canhBao}. Truong Java chi bi gan khi bang doc + kiem xong toan bo (khong nua voi).
     */
    static void muc(final KetQua kq, final NguonDuLieu n, final String bang, final String[] cot, final boolean quanTrong,
            final ApDung f) {
        String ly;
        try {
            final BangSql b = n.doc(bang, cot);
            if (b.soDong() == 0) {
                ly = "bang RONG";
            } else {
                final String them = f.chay(b);
                kq.ok.add(bang + ": " + b.soDong() + " dong" + (them == null ? "" : " (" + them + ")"));
                return;
            }
        } catch (final Exception e) {
            ly = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        (quanTrong ? kq.loi : kq.canhBao).add(bang + ": " + ly + " [" + n.moTa() + "]");
    }

    /**
     * Nap MOI bang so lieu (khong gom danh muc cau hinh - CauHinhTrungTam tu nap trong khoiTao). Goi lai duoc (nap lai
     * nong): moi bang thay nguyen khoi, loi thi giu ban dang chay.
     */
    public static KetQua napSoLieu(final NguonDuLieu n) {
        final KetQua kq = new KetQua();
        muc(kq, n, CAP_DO, COT_CAP_DO, true, (b) -> {
            final CapDo d = docCapDo(b, ThongSoLauDai.CAP_TOI_DA);
            ThongSoLauDai.GIO_TICH_LUY_PHAN_NGHIN = d.gioTichLuyPhanNghin;
            ThongSoLauDai.MOB_EXP = d.mobExp;
            ThongSoLauDai.QUAI_MOI_GIO = d.quaiMoiGio;
            ThongSoLauDai.XU_MOI_QUAI = d.xuMoiQuai;
            ThongSoLauDai.EXP_LEN_CAP = d.expLenCap;
            return "cap 1.." + ThongSoLauDai.CAP_TOI_DA;
        });
        muc(kq, n, NANG_CAP, COT_NANG_CAP, true, (b) -> {
            final NangCap c = docNangCap(b);
            if (c.tenBac.length != ThongSoLauDai.BAC_TB_GIA.length || c.tenBac.length != ThongSoLauDai.BAC_TB_NGAY_MO.length) {
                throw new IllegalStateException(c.tenBac.length + " bac, ThongSoLauDai co " + ThongSoLauDai.BAC_TB_GIA.length
                        + " bac gia/ngay mo (chay lai tools/pokemon/sinh_sql_lau_dai.py)");
            }
            for (int k = 1; k <= SO_CONG; k++) {
                if (c.pity[k - 1] != (c.tiLe[k - 1] + ThongSoLauDai.PITY_CHIA - 1) / ThongSoLauDai.PITY_CHIA) {
                    throw new IllegalStateException("pity_moi_lan +" + k + " khac ceil(ti_le / PITY_CHIA)");
                }
            }
            ThongSoLauDai.BAC_TB_TEN = c.tenBac;
            ThongSoLauDai.BAC_TB_CAP = c.capBac;
            ThongSoLauDai.DA_NC = c.da;
            ThongSoLauDai.BUA_BAO_HO_LUONG_KHOA = c.buaLuongKhoa;
            ThongSoLauDai.CHI_SO_NC_PHAN_TRAM = c.chiSoPhanTram;
            ThongSoLauDai.CHI_PHI_NC = c.chiPhi;
            ThongSoLauDai.MAT_CAP_TU = c.matCapTu;
            ThongSoLauDai.TI_LE_NC = c.tiLe;
            return c.tenBac.length + " bac x " + SO_CONG + " cap";
        });
        muc(kq, n, KINH_TE, COT_KINH_TE, true, (b) -> {
            final Map<String, String> m = docKinhTe(b);
            // Doc HET truoc roi moi gan (thieu mot khoa = khong gan gi).
            final int[] v = new int[KHOA_KINH_TE.length];
            for (int i = 0; i < v.length; i++) {
                v[i] = soKinhTe(m, KHOA_KINH_TE[i]);
            }
            final int[][] met = docBacMet(m.get("exp.bac_met"));
            ThongSoLauDai.TI_LE_ROI_XU = v[0];
            ThongSoLauDai.DA_ROI_PHAN_NGHIN = v[1];
            ThongSoLauDai.NANG_LUONG_PHUT_NGAY = v[2];
            ThongSoLauDai.NANG_LUONG_PHUT_TICH_TOI_DA = v[3];
            ThongSoLauDai.THUE_CHO_PHAN_TRAM = v[4];
            ThongSoLauDai.PHI_DANG_BAN_PHAN_TRAM = v[5];
            ThongSoLauDai.TIEU_HAO_PHAN_TRAM = v[6];
            ThongSoLauDai.SUA_DO_PHAN_TRAM = v[7];
            ThongSoLauDai.GIFTCODE_TOI_DA_THANG = v[8];
            ThongSoLauDai.VE_CHUONG_TUAN = v[9];
            ThongSoLauDai.VE_CHUONG_TOI_DA = v[10];
            ThongSoLauDai.BAC_MET = met;
            return (KHOA_KINH_TE.length + 1) + " so";
        });
        muc(kq, n, SU_KIEN_NAM, COT_SU_KIEN_NAM, true, (b) -> {
            SuKienLauDai.LICH = docLichSuKien(b);
            return SuKienLauDai.LICH.length + " tuan co su kien";
        });
        muc(kq, n, HO_CAU, COT_HO_CAU, false, (b) -> {
            final Map<Integer, List<int[]>> m = docHoCau(b);
            BanDoSinhThai.datDiemCau(m);
            return m.size() + " ban do";
        });
        muc(kq, n, TRANG_BI, COT_TRANG_BI, false, (b) -> {
            final int[] a = docTrangBiGiftcode(b);
            if (a.length == 0) {
                throw new IllegalStateException("khong co id_item > 0");
            }
            GiftcodeAnToanDuLieu.TRANG_BI = a;
            return a.length + " trang bi giftcode";
        });
        return kq;
    }

    /** Thu tu = thu tu gan trong napSoLieu (KINH_TE). exp.bac_met doc rieng. */
    static final String[] KHOA_KINH_TE = {"roi.xu.phan_tram", "roi.da_nang_cap.phan_nghin", "exp.nang_luong_phut",
        "exp.nang_luong_tich_phut", "thue.cho", "thue.dang_ban", "tieu_hao.thuoc_phan_tram_thu_nhap",
        "tieu_hao.sua_do_phan_tram_thu_nhap", "giftcode.toi_da_thang", "ve_chuong.tuan", "ve_chuong.toi_da"};

    /** Danh muc cau hinh + moi bang so lieu (test, cong cu: mot nguon duy nhat la tep SQL). */
    public static KetQua napTatCa(final NguonDuLieu n) {
        final boolean dm = CauHinhTrungTam.napDanhMuc(n);
        final KetQua kq = napSoLieu(n);
        if (!dm) {
            kq.loi.add(0, "cau_hinh_danh_muc/cau_hinh_nhom: " + CauHinhTrungTam.loiDanhMuc());
        } else {
            kq.ok.add(0, "cau_hinh_danh_muc + cau_hinh_nhom: " + CauHinhTrungTam.danhMuc().length + " khoa");
        }
        return kq;
    }

    /** Duong dan mac dinh cua dump day du khi chay o server/ (test) hoac goc repo (cong cu). */
    public static String duongDumpDayDu() {
        for (final String p : new String[]{"../sql/htbb_day_du.sql", "sql/htbb_day_du.sql"}) {
            if (new java.io.File(p).isFile()) {
                return p;
            }
        }
        return "../sql/htbb_day_du.sql";
    }

    /**
     * Test / cong cu mo phong: nap moi bang tu sql/htbb_day_du.sql. Hong = nem loi (khong chay tiep voi so 0).
     */
    public static KetQua napTuDumpDayDu() {
        final KetQua kq;
        try {
            kq = napTatCa(DocDumpSql.tuTep(duongDumpDayDu()));
        } catch (final java.io.IOException e) {
            throw new IllegalStateException("khong doc duoc " + duongDumpDayDu() + ": " + e);
        }
        if (!kq.dat()) {
            throw new IllegalStateException("du lieu SQL hong: " + kq.loi);
        }
        return kq;
    }
}
