import com.htbb.pokemon.ThongSoLauDai;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MO PHONG KINH TE + TIEN TRINH 730 NGAY (docs/thiet-ke-game.md).
 *
 * TAT DINH: cung hat giong -> cung ket qua tung so (RNG SplitMix64 rieng, khong dung
 * Math.random / thoi gian). Doc so tu CSDL day du sql/htbb_day_du.sql (nguon duy nhat): ThongSoLauDai (nap tu
 * data_*_lau_dai), nhiem vu data_quest_lau_dai, lich su kien data_su_kien_nam.
 *
 * Mo hinh (rut gon co chu dich - muc tieu la SO SANH tham so, khong phai du doan tuyet doi):
 *   - 4 nhom nguoi choi: NHE 1-2 gio/ngay, VUA ~3 gio, CAY 5-7 gio, BOT 20 gio (treo may).
 *   - Moi ngay: huong dan / chuong chinh (ve chuong 2/tuan) / phu / ngay / tuan / Pokedex,
 *     phan con lai danh quai; EXP va xu qua "nang luong huan luyen" (100% -> 50% -> 20% -> 0).
 *   - Xu: vao = quai + nhiem vu + su kien + phan ra; ra = thuoc/sua do, dich chuyen, mua do
 *     theo bac, nang cap +1..+15 (pity, bua bao ho), hoc chieu, thue cho (bot ban hang).
 *   - Luong khoa / luong: nguon va ho hut rieng (khong nap tien - Q10).
 *   - Giu chan: nguy co bo choi/ngay theo nhom, nhan voi "dung lai khong tien bo", tuan su kien,
 *     doi; nguoi bo choi co the quay lai (nhiem vu quay lai + bat kip EXP).
 *   - 18/10/2026: KHONG tran cap theo mua (tranTheoMua = false, nhu server sv.tran_cap_theo_mua=0); nang luong
 *     90 phut 100% (+90 tich) -> 120 phut 20% -> 120 phut 5% -> 0; mo-dun TANG TOC (cuoi file): ve EXP, bua the
 *     luc, uy thac - ban khoa (choi) + ban cao cap (luong, dot), chung tran 60 diem/ngay, 210/tuan. Muc tieu moi:
 *     thoi gian toi cap 100 (nhe 15-18 thang, cay 9-12), mua toi tran nhanh hon <= 40%, suc manh cung cap, bot.
 *
 * Chay (o thu muc HTBB/):
 *   javac -encoding UTF-8 -cp build/src -d build/tools tools/pokemon/MoPhongKinhTe.java
 *   java -cp build/tools:build/src MoPhongKinhTe            (730 ngay, 100% dan so)
 *   java -cp build/tools:build/src MoPhongKinhTe 365 50     (365 ngay, 50% dan so)
 * Ra (chi khi chay tay): mo_phong_ra/mo_phong_kinh_te.csv, mo_phong_tom_tat.txt, mo_phong_*.png o thu muc hien tai.
 * test/KiemLauDai goi chayKiem(...) qua phan chieu (khong ghi tep).
 */
public final class MoPhongKinhTe {

    // ============================ NHOM NGUOI CHOI ============================
    static final int NHE = 0, VUA = 1, CAY = 2, BOT = 3, SO_NHOM = 4;
    static final String[] TEN_NHOM = {"nhe", "vua", "cay", "bot"};
    static final double[] GIO_NGAY = {1.5, 3.0, 6.0, 20.0};
    static final double[] XS_DANG_NHAP = {0.85, 0.90, 0.97, 1.0};
    static final double[] TI_TRONG = {0.50, 0.30, 0.15, 0.05};
    static final int[] PLUS_MUC_TIEU = {ThongSoLauDai.MT_MUC_NANG_CAP_NHE, ThongSoLauDai.MT_MUC_NANG_CAP_VUA,
        ThongSoLauDai.MT_MUC_NANG_CAP_CAY, 0};
    static final double[] BO_CHOI_GOC = {0.010, 0.006, 0.004, 0.0};
    static final int DAN_SO_KHAI_TRUONG = 2000;

    // ============================ DU LIEU NHIEM VU ============================
    static final class Chuong {
        int cap, ngayMo, nam;
        long exp, xu;
        int lk;
    }

    static final class NvDon {
        int cap;
        long exp, xu;
        int lk, lg;
        String kieu;
        int nguong;
    }

    final List<Chuong> chuong = new ArrayList<Chuong>();
    final List<NvDon> phu = new ArrayList<NvDon>();
    final List<NvDon> dex = new ArrayList<NvDon>();
    final List<NvDon> thanhTuu = new ArrayList<NvDon>();
    final Map<Integer, NvDon> ngayTheoBac = new HashMap<Integer, NvDon>();
    final Map<Integer, NvDon> tuanTheoBac = new HashMap<Integer, NvDon>();
    long huongDanExp, huongDanXu;
    int huongDanLk;
    int soChuongNam1;
    double[] nganSachTuan = new double[53];
    boolean[] tuanLe = new boolean[53];

    // ============================ NGUOI CHOI ============================
    static final class NguoiChoi {
        int nhom, ngayVao, cap = 1, tier, chuong, ve, phuIdx, dexIdx, tinhThong, mucTinhThong;
        int ngay60 = -1, ngay80 = -1, ngay91 = -1, ngay100 = -1;
        int ngayBoDau = -1, ngayCuoiTienBo, ngayBo = -1, ngayXongNam1 = -1, ngayXongNam2 = -1, ngayDangNhap;
        int tuanCuoi = -1, ngayChoi, lanDangNhapChuoi, daMuaMon, daNangCapCao;
        boolean conChoi = true, coDoi, daHuongDan, thanhTuuDat[];
        double exp, gioDau, tongGio, gioTienHoa = -1, tichNangLuong;
        double xu, lk, lg;
        int da;
        int[] plus = new int[ThongSoLauDai.SO_O_TRANG_BI];
        int[] truot = new int[ThongSoLauDai.SO_O_TRANG_BI];
        int chieuDaHoc, ngoc;
        boolean laKhaiTruong, batKip;
        double expNgay;
        NapMoPhong.Nguoi napNguoi; // nap tien (mo-dun cuoi file)
    }

    // ============================ RNG TAT DINH ============================
    long trangThaiRng;

    double ngauNhien() {
        trangThaiRng += 0x9E3779B97F4A7C15L;
        long z = trangThaiRng;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z = z ^ (z >>> 31);
        return (z >>> 11) * 0x1.0p-53;
    }

    // ============================ SO LIEU THEO NGAY ============================
    static final String[] COT = {"ngay", "nhom", "so_hoat_dong", "cap_tb", "cap_p10", "cap_p50", "cap_p90",
        "tran_cap", "xu_tb", "xu_trung_vi_gio_thu_nhap", "luong_khoa_tb", "luong_tb", "faucet_xu", "sink_xu",
        "nang_cap_tb", "bac_tb", "chuong_tb", "pct_xong_chinh_nam1", "pct_xong_chinh_nam2", "tinh_thong_tb",
        "exp_hieu_dung_ngay"};

    final List<String> dongCsv = new ArrayList<String>();
    // [ngay][nhom 0..4 (4 = tat ca nguoi that)]
    double[][] xuTb, capTb, xongNam1, xongNam2, nangCapTb, capKt;
    double[] tongFaucet, tongSink, tongLkVao, tongLkRa;
    final Map<String, double[]> faucetTheoNguon = new HashMap<String, double[]>();
    final Map<String, double[]> sinkTheoNguon = new HashMap<String, double[]>();
    final List<NguoiChoi> nguoi = new ArrayList<NguoiChoi>();
    int soNgay;
    /** Dan CHUAN: 300 nguoi/nhom tu khai truong, khong bo choi - de do tien trinh. */
    boolean chiChuan;
    /** Kich ban so sanh: tat nang luong (moi gio 100%) / nang cap gia co dinh 5.000 xu nhu game cu. */
    boolean khongMet, nangCapCoDinh;
    static final int SO_CHUAN_MOI_NHOM = 300;
    /** Dan chuan so sanh tang toc (chayHai): khong ai dung / moi nguoi mua toi tran. */
    MoPhongKinhTe ssKhong, ssToiDa;
    /** Suc manh theo dai cap 10 (cuoi moi thang, nguoi that): [dai][0 tong, 1 so nguoi]. */
    final double[][] smTheoCap = new double[ThongSoLauDai.CAP_TOI_DA / 10 + 1][2];
    /** Tran cap theo mua (thiet ke cu). Mac dinh TAT (quyet dinh 18/10/2026: khong tran, len cap cham + tang toc). */
    boolean tranTheoMua = false;
    /** Nang luong huan luyen (ban sao doc duoc, de chay kich ban): phut 100%/ngay, tich toi da, bac met. */
    int phutNgay = ThongSoLauDai.NANG_LUONG_PHUT_NGAY, phutTich = ThongSoLauDai.NANG_LUONG_PHUT_TICH_TOI_DA;
    int[][] bacMet = ThongSoLauDai.BAC_MET;

    int tran(final int ngay) {
        return tranTheoMua ? ThongSoLauDai.tranCap(ngay) : ThongSoLauDai.CAP_TOI_DA;
    }

    /** Phut hieu dung trong {@code phut} phut danh quai dau tien cua ngay co {@code phut100} phut 100%. */
    double hieuDungTrong(final double phut, final double phut100) {
        final double dung100 = Math.min(phut, phut100);
        double hd = dung100;
        double con = phut - dung100;
        for (final int[] bm : bacMet) {
            final double d = Math.min(con, bm[0]);
            hd += d * bm[1] / 100.0;
            con -= d;
        }
        return hd;
    }
    double heSoDanSo;
    int[] trungViCapNgayTruoc = {1};
    int phatHienBot;
    double tongLgVao, tongLgRa;
    double stockDaCho;
    double[] expHieuDungNhom = new double[SO_NHOM];
    int[] demExpHieuDungNhom = new int[SO_NHOM];

    // ============================ NAP DU LIEU ============================
    static List<Map<String, String>> docCsv(final File f) throws Exception {
        final List<Map<String, String>> ra = new ArrayList<Map<String, String>>();
        final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        try {
            String[] cot = null;
            String dong;
            while ((dong = r.readLine()) != null) {
                final List<String> o = tachCsv(dong);
                if (cot == null) {
                    cot = o.toArray(new String[0]);
                    continue;
                }
                final Map<String, String> m = new HashMap<String, String>();
                for (int i = 0; i < cot.length && i < o.size(); i++) {
                    m.put(cot[i], o.get(i));
                }
                ra.add(m);
            }
        } finally {
            r.close();
        }
        return ra;
    }

    static List<String> tachCsv(final String s) {
        final List<String> ra = new ArrayList<String>();
        final StringBuilder b = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (q) {
                if (c == '"') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '"') {
                        b.append('"');
                        i++;
                    } else {
                        q = false;
                    }
                } else {
                    b.append(c);
                }
            } else if (c == '"') {
                q = true;
            } else if (c == ',') {
                ra.add(b.toString());
                b.setLength(0);
            } else {
                b.append(c);
            }
        }
        ra.add(b.toString());
        return ra;
    }

    static int so(final String s) {
        return s == null || s.isEmpty() ? 0 : (int) Double.parseDouble(s);
    }

    static long soL(final String s) {
        return s == null || s.isEmpty() ? 0L : Long.parseLong(s);
    }

    /** Moi dong cua mot bang trong sql/htbb_day_du.sql (cot -> gia tri, NULL -> ""). */
    private static final Map<String, com.htbb.dulieu.DocDumpSql> DUMP = new HashMap<String, com.htbb.dulieu.DocDumpSql>();

    static synchronized List<Map<String, String>> docBang(final File goc, final String ten) throws Exception {
        final String p = new File(goc, "sql/htbb_day_du.sql").getPath();
        if (!DUMP.containsKey(p)) {
            DUMP.put(p, com.htbb.dulieu.DocDumpSql.tuTep(p));
        }
        final com.htbb.dulieu.DocDumpSql d = DUMP.get(p);
        final com.htbb.dulieu.BangSql b = d.doc(ten, d.cotCua(ten));
        final List<Map<String, String>> ra = new ArrayList<Map<String, String>>();
        for (int r = 0; r < b.soDong(); r++) {
            final Map<String, String> m = new HashMap<String, String>();
            for (final String c : b.cot()) {
                m.put(c, b.chu(r, c));
            }
            ra.add(m);
        }
        return ra;
    }

    void napNhiemVu(final List<Map<String, String>> bang) throws Exception {
        final Map<Integer, Chuong> theoSo = new HashMap<Integer, Chuong>();
        final Map<Integer, List<NvDon>> ngay = new HashMap<Integer, List<NvDon>>();
        final Map<Integer, List<NvDon>> tuan = new HashMap<Integer, List<NvDon>>();
        for (final Map<String, String> d : bang) {
            final String loai = d.get("loai");
            final NvDon n = new NvDon();
            n.cap = so(d.get("cap_toi_thieu"));
            n.exp = soL(d.get("exp"));
            n.xu = soL(d.get("xu"));
            n.lk = so(d.get("luong_khoa"));
            n.lg = so(d.get("luong"));
            n.kieu = d.get("kieu_muc_tieu");
            n.nguong = so(d.get("so_luong"));
            if ("huong_dan".equals(loai)) {
                huongDanExp += n.exp;
                huongDanXu += n.xu;
                huongDanLk += n.lk;
            } else if ("chinh".equals(loai)) {
                final int c = so(d.get("chuong"));
                Chuong ch = theoSo.get(c);
                if (ch == null) {
                    ch = new Chuong();
                    ch.nam = so(d.get("nam"));
                    theoSo.put(c, ch);
                }
                ch.cap = Math.max(ch.cap, n.cap);
                ch.ngayMo = Math.max(ch.ngayMo, so(d.get("ngay_mo")));
                ch.exp += n.exp;
                ch.xu += n.xu;
                ch.lk += n.lk;
            } else if ("phu".equals(loai) || "tuyen".equals(loai)) {
                phu.add(n);
            } else if ("pokedex".equals(loai) && !"pokedex_shiny".equals(n.kieu)) {
                dex.add(n);
            } else if ("ngay".equals(loai)) {
                put(ngay, n.cap, n);
            } else if ("tuan".equals(loai)) {
                put(tuan, n.cap, n);
            } else if ("thanh_tuu".equals(loai)) {
                thanhTuu.add(n);
            }
        }
        final Integer[] k = theoSo.keySet().toArray(new Integer[0]);
        Arrays.sort(k);
        for (final Integer c : k) {
            chuong.add(theoSo.get(c));
            if (theoSo.get(c).nam == 1) {
                soChuongNam1++;
            }
        }
        java.util.Collections.sort(phu, (a, b) -> a.cap - b.cap);
        java.util.Collections.sort(dex, (a, b) -> a.cap - b.cap);
        trungBinh(ngay, ngayTheoBac);
        trungBinh(tuan, tuanTheoBac);
    }

    static void put(final Map<Integer, List<NvDon>> m, final int k, final NvDon n) {
        List<NvDon> l = m.get(k);
        if (l == null) {
            l = new ArrayList<NvDon>();
            m.put(k, l);
        }
        l.add(n);
    }

    static void trungBinh(final Map<Integer, List<NvDon>> vao, final Map<Integer, NvDon> ra) {
        for (final Map.Entry<Integer, List<NvDon>> e : vao.entrySet()) {
            final NvDon t = new NvDon();
            t.cap = e.getKey();
            for (final NvDon n : e.getValue()) {
                t.exp += n.exp;
                t.xu += n.xu;
                t.lk += n.lk;
            }
            final int c = e.getValue().size();
            t.exp /= c;
            t.xu /= c;
            t.lk /= c;
            ra.put(e.getKey(), t);
        }
    }

    void napSuKien(final List<Map<String, String>> bang) throws Exception {
        for (final Map<String, String> d : bang) {
            final int t = so(d.get("tuan"));
            nganSachTuan[t] = Double.parseDouble(d.get("ngan_sach_gio"));
            final String loai = d.get("loai");
            tuanLe[t] = !"thuong".equals(loai);
        }
    }

    // ============================ TIEN ICH KINH TE ============================
    void vao(final String nguon, final int ngay, final double x) {
        tongFaucet[ngay] += x;
        double[] a = faucetTheoNguon.get(nguon);
        if (a == null) {
            a = new double[soNgay];
            faucetTheoNguon.put(nguon, a);
        }
        a[ngay] += x;
    }

    void ra(final String nguon, final int ngay, final double x) {
        tongSink[ngay] += x;
        double[] a = sinkTheoNguon.get(nguon);
        if (a == null) {
            a = new double[soNgay];
            sinkTheoNguon.put(nguon, a);
        }
        a[ngay] += x;
    }

    boolean tieu(final NguoiChoi p, final String nguon, final int ngay, final double x) {
        if (p.xu < x) {
            return false;
        }
        p.xu -= x;
        ra(nguon, ngay, x);
        return true;
    }

    static int capNhom(final Map<Integer, NvDon> m, final int cap) {
        int best = -1;
        for (final int k : m.keySet()) {
            if (k <= cap && k > best) {
                best = k;
            }
        }
        return best;
    }

    // ============================ CHAY ============================
    void chay(final File goc, final int soNgay, final double heSoDanSo, final long hat) throws Exception {
        this.soNgay = soNgay;
        this.heSoDanSo = heSoDanSo;
        this.trangThaiRng = hat;
        napNhiemVu(docBang(goc, "data_quest_lau_dai"));
        napSuKien(docBang(goc, "data_su_kien_nam"));
        xuTb = new double[soNgay][5];
        capTb = new double[soNgay][5];
        xongNam1 = new double[soNgay][5];
        xongNam2 = new double[soNgay][5];
        nangCapTb = new double[soNgay][5];
        capKt = new double[soNgay][5];
        tongFaucet = new double[soNgay];
        tongSink = new double[soNgay];
        tongLkVao = new double[soNgay];
        tongLkRa = new double[soNgay];
        if (chiChuan) {
            for (int g = 0; g < SO_NHOM; g++) {
                for (int i = 0; i < soChuanMoiNhom; i++) { // nap tien: mac dinh SO_CHUAN_MOI_NHOM
                    themNguoi(0, g, true);
                }
            }
        } else {
            final int khaiTruong = (int) Math.round(DAN_SO_KHAI_TRUONG * heSoDanSo);
            for (int i = 0; i < khaiTruong; i++) {
                themNguoi(0, chonNhom(), true);
            }
        }
        for (int ngay = 0; ngay < soNgay; ngay++) {
            if (ngay > 0 && !chiChuan) {
                final int tuan = 1 + (ngay / 7) % 52;
                double moi = (40 * Math.exp(-ngay / 150.0) + 12) * heSoDanSo * (tuanLe[tuan] ? 1.3 : 1.0);
                int n = (int) moi;
                if (ngauNhien() < moi - n) {
                    n++;
                }
                for (int i = 0; i < n; i++) {
                    themNguoi(ngay, chonNhom(), false);
                }
            }
            motNgay(ngay);
            thongKe(ngay);
        }
    }

    int chonNhom() {
        final double u = ngauNhien();
        double c = 0;
        for (int i = 0; i < SO_NHOM; i++) {
            c += TI_TRONG[i];
            if (u < c) {
                return i;
            }
        }
        return NHE;
    }

    void themNguoi(final int ngay, final int nhom, final boolean khaiTruong) {
        final NguoiChoi p = new NguoiChoi();
        p.nhom = nhom;
        p.ngayVao = ngay;
        p.laKhaiTruong = khaiTruong;
        p.ve = ThongSoLauDai.VE_CHUONG_BAN_DAU;
        p.ngayCuoiTienBo = ngay;
        p.thanhTuuDat = new boolean[thanhTuu.size()];
        p.coDoi = nhom != BOT && ngauNhien() < 0.6;
        if (napCheDo != NAP_TAT) {
            nap().themNguoi(p); // nap tien
        }
        nguoi.add(p);
    }

    void motNgay(final int ngay) {
        final int tran = tran(ngay);
        final int tuan = 1 + (ngay / 7) % 52;
        final int trungVi = trungViCapNgayTruoc[0];
        double xuBotBan = 0;
        final List<NguoiChoi> nguoiMua = new ArrayList<NguoiChoi>();
        for (final NguoiChoi p : nguoi) {
            if (!p.conChoi) {
                // Quay lai: 0.3%/ngay (nhiem vu quay lai + bat kip EXP).
                if (p.nhom != BOT && p.ngayBo >= 0 && ngay - p.ngayBo >= 14 && ngauNhien() < 0.003) {
                    p.conChoi = true;
                    p.ngayCuoiTienBo = ngay;
                    p.ngayBo = -1;
                    p.batKip = true;
                    final double qx = 0.30 * ThongSoLauDai.xuMoiGio(p.cap);
                    p.xu += qx;
                    vao("nhiem_vu", ngay, qx);
                } else {
                    continue;
                }
            }
            if (ngay < p.ngayVao) {
                continue;
            }
            // Bo choi.
            if (!chiChuan && p.nhom != BOT && ngay > p.ngayVao && biBoChoi(p, ngay, tuan)) {
                p.conChoi = false;
                p.ngayBo = ngay;
                if (p.ngayBoDau < 0) {
                    p.ngayBoDau = ngay;
                }
                continue;
            }
            if (!chiChuan && p.nhom == BOT && ngay - p.ngayVao > 3 && ngauNhien() < 0.01) {
                // Phat hien bot (log met moi: >10 gio/ngay lien tuc). Tai san bi xoa = ho hut.
                p.conChoi = false;
                p.ngayBo = ngay;
                ra("khoa_bot", ngay, p.xu);
                p.xu = 0;
                phatHienBot++;
                continue;
            }
            if (ngauNhien() > XS_DANG_NHAP[p.nhom]) {
                p.tichNangLuong = Math.min(phutTich - phutNgay, p.tichNangLuong + phutNgay);
                continue;
            }
            p.ngayDangNhap = ngay;
            p.ngayChoi++;
            choi(p, ngay, tran, tuan, trungVi);
            if (p.nhom == BOT) {
                xuBotBan += p.xu * 0.9;
                p.xu *= 0.1;
            } else if (p.xu < 3 * ThongSoLauDai.xuMoiGio(p.cap)) {
                nguoiMua.add(p);
            }
        }
        // Bot ban hang/xu qua cho (thue + phi dang ban bi dot). Nguoi mua: nguoi dang thieu tien.
        if (xuBotBan > 0) {
            final double thue = xuBotBan * (ThongSoLauDai.THUE_CHO_PHAN_TRAM + ThongSoLauDai.PHI_DANG_BAN_PHAN_TRAM) / 100.0;
            ra("thue_cho", ngay, thue);
            final double conLai = xuBotBan - thue;
            if (nguoiMua.isEmpty()) {
                ra("thue_cho", ngay, conLai);
            } else {
                // Nguoi mua tra tien that cho hang (da) -> tien chuyen sang bot da tinh o tren;
                // o day: phan hang ho (da) chia cho nguoi mua, tien cua ho -> bot (mo hinh hoa: xu
                // luan chuyen khong sinh them). Gioi han giao dich truc tiep: 5 gio thu nhap/ngay.
                final double moiNguoi = conLai / nguoiMua.size();
                for (final NguoiChoi p : nguoiMua) {
                    final double tran2 = ThongSoLauDai.TRAN_GIAO_DICH_TRUC_TIEP_GIO * ThongSoLauDai.xuMoiGio(p.cap);
                    final double nhan = Math.min(moiNguoi, tran2);
                    p.xu += nhan;
                    if (nhan < moiNguoi) {
                        ra("thue_cho", ngay, moiNguoi - nhan);
                    }
                }
            }
        }
        if (napCheDo != NAP_TAT) {
            nap().cuoiNgay(ngay); // nap tien
        }
    }

    boolean biBoChoi(final NguoiChoi p, final int ngay, final int tuan) {
        final int tuoi = ngay - p.ngayVao;
        double h;
        if (tuoi == 1) {
            h = p.gioTienHoa >= 0 && p.gioTienHoa <= 1.5 ? 0.38 : 0.55;
        } else if (tuoi <= 7) {
            h = 0.07;
        } else if (tuoi <= 30) {
            h = 0.022 + BO_CHOI_GOC[p.nhom];
        } else {
            h = BO_CHOI_GOC[p.nhom];
        }
        if (ngay - p.ngayCuoiTienBo > 10) {
            h *= 1.8;
        }
        if (tuanLe[tuan] && nganSachTuan[tuan] >= 1.0) {
            h *= 0.85;
        }
        if (p.coDoi && p.cap >= 20) {
            h *= 0.85;
        }
        if (p.nhom == CAY && tuoi > 30 && tran(ngay) == p.cap && p.tinhThong >= p.mucTinhThong) {
            h *= 1.3; // het viec o tran cap
        }
        return ngauNhien() < h;
    }

    void choi(final NguoiChoi p, final int ngay, final int tran, final int tuan, final int trungVi) {
        double gio = p.nhom == BOT ? GIO_NGAY[BOT] : GIO_NGAY[p.nhom] * (0.7 + 0.6 * ngauNhien());
        p.expNgay = 0;
        final int capDau = p.cap;
        // Dang nhap 7 ngay (luong khoa).
        if (p.nhom != BOT) {
            final int lk = ThongSoLauDai.LICH_DANG_NHAP[p.lanDangNhapChuoi % 7];
            p.lanDangNhapChuoi++;
            p.lk += lk;
            tongLkVao[ngay] += lk;
        }
        // Huong dan.
        if (!p.daHuongDan && p.nhom != BOT) {
            p.daHuongDan = true;
            p.tongGio += 0.5;
            congExp(p, huongDanExp, tran, ngay);
            congXu(p, "nhiem_vu", ngay, huongDanXu);
            p.lk += huongDanLk;
            tongLkVao[ngay] += huongDanLk;
            gio -= 0.5;
        }
        // Ve chuong theo tuan nhan vat.
        final int tuanNv = (ngay - p.ngayVao) / 7;
        if (p.nhom != BOT && tuanNv != p.tuanCuoi) {
            if (p.tuanCuoi >= 0) {
                p.ve = Math.min(ThongSoLauDai.VE_CHUONG_TOI_DA, p.ve + ThongSoLauDai.VE_CHUONG_TUAN);
                // Nhiem vu tuan.
                final int b = capNhom(tuanTheoBac, p.cap);
                if (b >= 0) {
                    final NvDon t = tuanTheoBac.get(b);
                    for (int i = 0; i < ThongSoLauDai.GIOI_HAN_TUAN; i++) {
                        congExp(p, t.exp, tran, ngay);
                        congXu(p, "nhiem_vu", ngay, t.xu);
                        p.lk += t.lk;
                        tongLkVao[ngay] += t.lk;
                    }
                }
                // Xep hang tuan -> luong (gan dung: theo nhom).
                final double lg = p.nhom == CAY ? 12 : (p.nhom == VUA ? 5 : 2);
                p.lg += lg;
                tongLgVao += lg;
                // Giftcode (2/thang, chi vat pham khoa ~ 10 luong khoa).
                if (tuanNv % 2 == 0) {
                    p.lk += 10;
                    tongLkVao[ngay] += 10;
                }
            }
            p.tuanCuoi = tuanNv;
        }
        // ---- Module GD2 bo sung 17/10/2026 (server: QuaHoatDong, HamNgucGame, BossLauDai,
        // ChiemGymLauDai). Them sau, KHONG sua cac module khac. ----
        // Qua hoat dong theo tai khoan (ca bot: van "hoat dong"): 15' 1 da, 30' 5% gio xu,
        // 60' 2 luong khoa, 120' 3 luong khoa + 3 nguyen lieu.
        {
            final double phutHd = gio * 60;
            if (phutHd >= 30) {
                congXu(p, "su_kien", ngay, ThongSoLauDai.xuMoiGio(p.cap) * 0.05);
            }
            final int lkHd = (phutHd >= 60 ? 2 : 0) + (phutHd >= 120 ? 3 : 0);
            p.lk += lkHd;
            tongLkVao[ngay] += lkHd;
        }
        if (p.nhom != BOT) {
            // Ham nguc bi an: 1 luot thuong/ngay, tang >= 5 -> 5 luong khoa (~50% nguoi choi >= cap 20).
            if (p.cap >= 20 && gio >= 1.0 && ngauNhien() < 0.5) {
                p.lk += 5;
                tongLkVao[ngay] += 5;
            }
            // Tuan: boss the gioi (50% tham gia, hop tham gia 3 luong khoa) + chiem thanh (20% dong
            // gop: 5 luong khoa; 3% bang thang: 20). Chia quy bang la chuyen tien, khong tinh.
            if (ngay > p.ngayVao && (ngay - p.ngayVao) % 7 == 0) {
                if (ngauNhien() < 0.5) {
                    p.lk += 3;
                    tongLkVao[ngay] += 3;
                }
                final double r = ngauNhien();
                final int lkCt = r < 0.03 ? 20 : (r < 0.20 ? 5 : 0);
                p.lk += lkCt;
                tongLkVao[ngay] += lkCt;
            }
        }
        // Chuong chinh.
        if (p.nhom != BOT) {
            int lam = 0;
            while (p.chuong < chuong.size() && p.ve > 0 && lam < 2 && gio >= 0.3) {
                final Chuong c = chuong.get(p.chuong);
                if (p.cap < c.cap || ngay < c.ngayMo) {
                    break;
                }
                p.ve--;
                p.chuong++;
                lam++;
                final double gc = ThongSoLauDai.GIO_MOI_CHUONG_PHAN_NGHIN / 1000.0 * (c.cap < ThongSoLauDai.CAP_TAN_THU ? 0.5 : 1.0);
                gio -= gc;
                p.tongGio += gc;
                congExp(p, c.exp, tran, ngay);
                congXu(p, "nhiem_vu", ngay, c.xu);
                p.lk += c.lk;
                tongLkVao[ngay] += c.lk;
                p.ngayCuoiTienBo = ngay;
                if (p.chuong == soChuongNam1 && p.ngayXongNam1 < 0) {
                    p.ngayXongNam1 = ngay;
                }
                if (p.chuong == chuong.size() && p.ngayXongNam2 < 0) {
                    p.ngayXongNam2 = ngay;
                }
            }
            // Phu: toi da 25% thoi gian.
            double gioPhu = gio * 0.25;
            while (p.phuIdx < phu.size() && phu.get(p.phuIdx).cap <= p.cap && gioPhu >= 0.25) {
                final NvDon n = phu.get(p.phuIdx++);
                gioPhu -= ThongSoLauDai.GIO_MOI_PHU_PHAN_NGHIN / 1000.0;
                gio -= ThongSoLauDai.GIO_MOI_PHU_PHAN_NGHIN / 1000.0;
                p.tongGio += ThongSoLauDai.GIO_MOI_PHU_PHAN_NGHIN / 1000.0;
                congExp(p, n.exp, tran, ngay);
                congXu(p, "nhiem_vu", ngay, n.xu);
                p.lk += n.lk;
                tongLkVao[ngay] += n.lk;
            }
            // Pokedex: 1/ngay (gap/ha trong luc danh quai).
            if (p.dexIdx < dex.size() && dex.get(p.dexIdx).cap <= p.cap) {
                final NvDon n = dex.get(p.dexIdx++);
                congExp(p, n.exp, tran, ngay);
                congXu(p, "nhiem_vu", ngay, n.xu);
                p.lk += n.lk;
                tongLkVao[ngay] += n.lk;
            }
            // Nhiem vu ngay (trong luc danh quai, khong ton them gio).
            final int b = capNhom(ngayTheoBac, p.cap);
            if (b >= 0) {
                final NvDon t = ngayTheoBac.get(b);
                for (int i = 0; i < ThongSoLauDai.GIOI_HAN_NGAY; i++) {
                    congExp(p, t.exp, tran, ngay);
                    congXu(p, "nhiem_vu", ngay, t.xu);
                    p.lk += t.lk;
                    tongLkVao[ngay] += t.lk;
                }
            }
            // Su kien tuan: ngan sach gio thu nhap/tuan, 70% nguoi tham gia, 25% bang xu.
            if (nganSachTuan[tuan] > 0 && ngauNhien() < 0.7) {
                final double gt = nganSachTuan[tuan] / 7.0 * ThongSoLauDai.xuMoiGio(p.cap);
                congXu(p, "su_kien", ngay, gt * ThongSoLauDai.SU_KIEN_PHAN_XU / 100.0);
                if (nganSachTuan[tuan] >= 1.0) {
                    p.ngayCuoiTienBo = Math.max(p.ngayCuoiTienBo, ngay - 3);
                }
            }
        }
        if (hstBat) {
            gio -= hst().motNgay(p, ngay, gio); // he sinh thai: cau ca/khao co/nau an truoc khi danh quai
        }
        // Danh quai: nang luong.
        // Tang toc (mo-dun TANG TOC cuoi file): {phut bua the luc, phut ve x2, phut ve x1,5} dung hom nay.
        final double[] tt = cheDoTangToc == TT_KHONG ? null : tangToc().truocDanhQuai(p, ngay, Math.max(0, gio));
        if (gio > 0) {
            double phut = gio * 60;
            final double phut100Goc = phutNgay + p.tichNangLuong;
            final double phut100 = phut100Goc + (tt == null ? 0 : tt[0]);
            final double dung100 = Math.min(phut, phut100);
            p.tichNangLuong = Math.min(phutTich - phutNgay, Math.max(0, phut100Goc - dung100));
            double hieuDung = hieuDungTrong(phut, phut100);
            if (khongMet) {
                hieuDung = phut;
            }
            // Ve nhan EXP chay tu phut dau danh quai (x2 truoc, roi x1,5): chi cong EXP, khong xu/da.
            double thuongVe = 0;
            if (tt != null && !khongMet) {
                final double a = Math.min(phut, tt[1]), b = Math.min(phut, tt[1] + tt[2]);
                thuongVe = (hieuDungTrong(phut, phut100) - hieuDungTrong(phut, phut100Goc))
                        + 1.0 * hieuDungTrong(a, phut100) + 0.5 * (hieuDungTrong(b, phut100) - hieuDungTrong(a, phut100));
                tangToc().ghiThuong(p, thuongVe);
                hieuDung = hieuDungTrong(phut, phut100Goc);
            }
            final double gioHd = hieuDung / 60.0;
            expHieuDungNhom[p.nhom] += gioHd;
            demExpHieuDungNhom[p.nhom]++;
            double heSo = 1.0;
            if (p.nhom != BOT && (p.batKip || p.ngayVao >= 30) && p.cap < trungVi - ThongSoLauDai.KHOANG_BAT_KIP) {
                heSo = ThongSoLauDai.HE_SO_BAT_KIP / 100.0;
            }
            // Chia lam 4 khuc de cap tang trong ngay thi muc thu nhap tang theo.
            for (int k = 0; k < 4; k++) {
                final double g = gioHd / 4;
                p.tongGio += gio / 4;
                congExp(p, (long) ((g + thuongVe / 240.0) * heSo * ThongSoLauDai.expMoiGio(p.cap)), tran, ngay);
                congXu(p, "quai", ngay, g * ThongSoLauDai.xuMoiGio(p.cap));
                p.da += (int) Math.round(g * ThongSoLauDai.QUAI_MOI_GIO[p.cap] * ThongSoLauDai.DA_ROI_PHAN_NGHIN / 1000.0);
            }
            // Thuoc + sua do: theo gio THAT (met van ton thuoc - chong bot).
            final double tieuHao = gio * ThongSoLauDai.xuMoiGio(p.cap)
                    * (ThongSoLauDai.TIEU_HAO_PHAN_TRAM + ThongSoLauDai.SUA_DO_PHAN_TRAM) / 100.0;
            ra("thuoc_sua_do", ngay, Math.min(p.xu, tieuHao));
            p.xu = Math.max(0, p.xu - tieuHao);
            final double dc = ThongSoLauDai.DICH_CHUYEN_LAN_NGAY * ThongSoLauDai.xuMoiGio(p.cap)
                    * ThongSoLauDai.DICH_CHUYEN_PHAN_TRAM_GIO / 100.0;
            tieu(p, "dich_chuyen", ngay, Math.min(p.xu, dc));
            if (hoiSinhBat) {
                hoiSinh().sauDanhQuai(p, ngay, gio, gioHd); // guc / hoi sinh (mo-dun cuoi file)
            }
        }
        if (tt != null) {
            // Uy thac luyen tap (offline): EXP = gio x 20% x EXP 1 gio o cap hien tai. Khong xu, khong vat pham.
            final double gioUt = tangToc().gioUyThac(p);
            if (gioUt > 0 && p.cap < ThongSoLauDai.CAP_TOI_DA) {
                congExp(p, (long) (gioUt * TangTocMoPhong.UY_THAC_PHAN_TRAM / 100.0 * ThongSoLauDai.expMoiGio(p.cap)), tran, ngay);
                tangToc().ghiThuong(p, gioUt * 60 * TangTocMoPhong.UY_THAC_PHAN_TRAM / 100.0);
            }
        }
        if (p.cap > capDau) {
            p.ngayCuoiTienBo = ngay;
        }
        if (p.nhom == BOT) {
            // Bot ban da cho nguoi choi: da -> tien da tinh trong xuBotBan (gia tri da ~ gia xu).
            p.da = 0;
            return;
        }
        // Hoc chieu: 3 gio thu nhap o cap 25/30/45/60/75/90.
        final int[] capChieu = {25, 30, 45, 60, 75, 90};
        while (p.chieuDaHoc < capChieu.length && p.cap >= capChieu[p.chieuDaHoc]) {
            final double gia = 3 * ThongSoLauDai.xuMoiGio(capChieu[p.chieuDaHoc]);
            if (!tieu(p, "hoc_chieu", ngay, gia)) {
                break;
            }
            p.chieuDaHoc++;
        }
        doiBac(p, ngay);
        nangCap(p, ngay);
        ngocVaTrangTri(p, ngay);
        // Luong khoa: thuoc (4/ngay choi) + do trang tri khi du (5% phan tren 200).
        final double lkThuoc = Math.min(p.lk, 4);
        p.lk -= lkThuoc;
        double lkRa = lkThuoc;
        if (p.lk > 200) {
            final double t = (p.lk - 200) * 0.05;
            p.lk -= t;
            lkRa += t;
        }
        tongLkRa[ngay] += lkRa;
        // Luong: do trang tri 70 luong (30%/tuan khi co >= 100), phi chuyen 5%.
        if (p.lg >= 100 && ngauNhien() < 0.3 / 7) {
            p.lg -= 70;
            tongLgRa += 70;
        }
        thanhTuu(p, ngay);
        if (napCheDo != NAP_TAT) {
            nap().moiNgay(p, ngay); // nap tien
        }
    }

    void congXu(final NguoiChoi p, final String nguon, final int ngay, final double x) {
        if (x <= 0) {
            return;
        }
        p.xu += x;
        vao(nguon, ngay, x);
    }

    void congExp(final NguoiChoi p, final long exp, final int tran, final int ngay) {
        if (exp <= 0) {
            return;
        }
        p.expNgay += exp;
        p.exp += exp;
        while (p.cap < tran && p.exp >= ThongSoLauDai.EXP_LEN_CAP[p.cap]) {
            p.exp -= ThongSoLauDai.EXP_LEN_CAP[p.cap];
            p.cap++;
            if (p.cap == 60) {
                p.ngay60 = ngay;
            } else if (p.cap == 80) {
                p.ngay80 = ngay;
            } else if (p.cap == 91) {
                p.ngay91 = ngay;
            } else if (p.cap == ThongSoLauDai.CAP_TOI_DA) {
                p.ngay100 = ngay;
            }
            if (p.cap == 16 && p.gioTienHoa < 0) {
                p.gioTienHoa = p.tongGio;
            }
        }
        if (p.cap >= tran) {
            // Tinh thong: EXP du -> diem (tran theo so mua da qua).
            final int muaDaQua = tranTheoMua ? soMuaDaQua(ngay) : ThongSoLauDai.TRAN_CAP_NGAY.length;
            p.mucTinhThong = Math.min(ThongSoLauDai.TINH_THONG_TOI_DA, muaDaQua * ThongSoLauDai.TINH_THONG_DIEM_MOI_MUA);
            final long moiDiem = ThongSoLauDai.EXP_LEN_CAP[Math.max(1, tran - 1)] * ThongSoLauDai.TINH_THONG_PHAN_TRAM_CAP / 100;
            while (p.exp >= moiDiem && p.tinhThong < p.mucTinhThong) {
                p.exp -= moiDiem;
                p.tinhThong++;
                p.ngayCuoiTienBo = ngay;
            }
            p.exp = Math.min(p.exp, moiDiem);
        }
    }

    static int soMuaDaQua(final int ngay) {
        int n = 0;
        for (final int d : ThongSoLauDai.TRAN_CAP_NGAY) {
            if (ngay >= d) {
                n++;
            }
        }
        return n;
    }

    void doiBac(final NguoiChoi p, final int ngay) {
        while (p.tier + 1 < ThongSoLauDai.BAC_TB_CAP.length
                && p.cap >= ThongSoLauDai.BAC_TB_CAP[p.tier + 1]
                && (!tranTheoMua || ngay >= ThongSoLauDai.BAC_TB_NGAY_MO[p.tier + 1])) {
            final int b = p.tier + 1;
            final double gia = ThongSoLauDai.BAC_TB_GIA[b];
            if (p.daMuaMon < ThongSoLauDai.SO_MON_MUA_MOI_BAC) {
                while (p.daMuaMon < ThongSoLauDai.SO_MON_MUA_MOI_BAC && tieu(p, "mua_do", ngay, gia)) {
                    p.daMuaMon++;
                }
                if (p.daMuaMon < ThongSoLauDai.SO_MON_MUA_MOI_BAC) {
                    return; // chua du tien, doi bac hom khac
                }
            }
            // Phan ra do bac cu: da + 10% gia.
            congXu(p, "phan_ra", ngay, 0.10 * ThongSoLauDai.BAC_TB_GIA[p.tier] * ThongSoLauDai.SO_O_TRANG_BI);
            p.da += ThongSoLauDai.DA_TU_PHAN_RA * ThongSoLauDai.SO_O_TRANG_BI;
            p.tier = b;
            p.daMuaMon = 0;
            for (int i = 0; i < p.plus.length; i++) {
                p.plus[i] = Math.max(0, p.plus[i] - ThongSoLauDai.GIU_CAP_KHI_DOI_BAC);
                p.truot[i] = 0;
            }
            p.ngayCuoiTienBo = ngay;
        }
    }

    /** Moc neo nang cap 0.9.8 (bat mac dinh nhu may chu: nang_cap.moc_neo = 1). */
    static final boolean MOC_NEO = !"0".equals(System.getProperty("moc_neo", "1"));

    void nangCap(final NguoiChoi p, final int ngay) {
        final int mucTieu = PLUS_MUC_TIEU[p.nhom];
        // Du tru: giu 2 gio thu nhap; mua da qua cho neu thieu (gia ~ 0.5 x xu de tu kiem 1 vien).
        final double duTru = 2 * ThongSoLauDai.xuMoiGio(p.cap);
        final double giaDa = 0.5 * ThongSoLauDai.xuMoiGio(p.cap)
                / (ThongSoLauDai.QUAI_MOI_GIO[p.cap] * ThongSoLauDai.DA_ROI_PHAN_NGHIN / 1000.0);
        for (int lan = 0; lan < 40; lan++) {
            int o = 0;
            for (int i = 1; i < p.plus.length; i++) {
                if (p.plus[i] < p.plus[o]) {
                    o = i;
                }
            }
            final int k = p.plus[o] + 1;
            if (k > mucTieu || k > 15) {
                return;
            }
            // 0.9.8 (bo-ngoc-bat): moc neo +7/+10/+13 (truot khong tut duoi moc) + he so xu buoc +11..+15
            // (nang_cap.he_so_xu_bac_cao = 1060 phan nghin) - nhu may chu (NangCapLauDai), tat bang -Dmoc_neo=0
            final double chiPhi = nangCapCoDinh ? 5000 : ThongSoLauDai.CHI_PHI_NC[p.tier][k]
                    * (MOC_NEO && k >= 11 ? com.htbb.pokemon.NangCapLauDai.HE_SO_XU_BAC_CAO_MAC_DINH / 1000.0 : 1.0);
            final int canDa = ThongSoLauDai.DA_NC[k - 1];
            if (p.da < canDa) {
                final double mua = (canDa - p.da) * giaDa;
                if (p.xu - mua - chiPhi < duTru) {
                    return;
                }
                // Mua da tren cho tu nguoi khac: tien chuyen tay (khong mat) tru thue.
                p.xu -= mua;
                ra("thue_cho", ngay, mua * (ThongSoLauDai.THUE_CHO_PHAN_TRAM + ThongSoLauDai.PHI_DANG_BAN_PHAN_TRAM) / 100.0);
                ra("mua_da_cho", ngay, mua * (100 - ThongSoLauDai.THUE_CHO_PHAN_TRAM - ThongSoLauDai.PHI_DANG_BAN_PHAN_TRAM) / 100.0);
                p.da = canDa;
            }
            if (p.xu - chiPhi < duTru) {
                return;
            }
            p.xu -= chiPhi;
            ra("nang_cap", ngay, chiPhi);
            p.da -= canDa;
            boolean bua = false;
            final int giaBua = ThongSoLauDai.BUA_BAO_HO_LUONG_KHOA[k - 1];
            if (k >= ThongSoLauDai.MAT_CAP_TU && giaBua > 0 && p.lk >= giaBua) {
                p.lk -= giaBua;
                tongLkRa[ngay] += giaBua;
                bua = true;
            }
            final int tl = ThongSoLauDai.tiLeNangCap(k, p.truot[o]);
            if (ngauNhien() * 100 < tl) {
                p.plus[o] = k;
                p.truot[o] = 0;
                if (k >= 7) {
                    p.ngayCuoiTienBo = ngay;
                    p.daNangCapCao = Math.max(p.daNangCapCao, k);
                }
            } else {
                p.truot[o]++;
                if (k >= ThongSoLauDai.MAT_CAP_TU && !bua) {
                    final int truoc = p.plus[o];
                    p.plus[o] = Math.max(0, truoc - 1);
                    if (MOC_NEO && !nangCapCoDinh) {
                        p.plus[o] = Math.max(p.plus[o], com.htbb.pokemon.NangCapLauDai.mocGiu(truoc,
                                com.htbb.pokemon.NangCapLauDai.MOC_NEO_MAC_DINH));
                    }
                }
            }
        }
    }

    /** Ho hut thu 2 (ngoc he) + do trang tri bang xu: chi phan xu vuot du tru. */
    void ngocVaTrangTri(final NguoiChoi p, final int ngay) {
        if (nangCapCoDinh) {
            return; // kich ban "game cu": khong co ho hut thu 2
        }
        final double gio = ThongSoLauDai.xuMoiGio(p.cap);
        final int toiDa = ThongSoLauDai.NGOC_HE_CAP_TOI_DA * ThongSoLauDai.SO_O_TRANG_BI;
        for (int lan = 0; lan < 11 && p.ngoc < toiDa && p.cap >= 30; lan++) {
            final int capNgoc = p.ngoc / ThongSoLauDai.SO_O_TRANG_BI;
            final double gia = gio * ThongSoLauDai.NGOC_HE_GIA_GIO_PHAN_TRAM / 100.0
                    * Math.pow(ThongSoLauDai.NGOC_HE_TANG_PHAN_TRAM / 100.0, capNgoc);
            if (p.xu - gia < 6 * gio) {
                break;
            }
            p.xu -= gia;
            ra("ngoc_he", ngay, gia);
            p.ngoc++;
        }
        final double vuot = p.xu - ThongSoLauDai.TRANG_TRI_NGUONG_GIO * gio;
        if (vuot > 0) {
            final double t = vuot * ThongSoLauDai.TRANG_TRI_PHAN_NGHIN_NGAY / 1000.0;
            p.xu -= t;
            ra("trang_tri", ngay, t);
        }
    }

    void thanhTuu(final NguoiChoi p, final int ngay) {
        for (int i = 0; i < thanhTuu.size(); i++) {
            if (p.thanhTuuDat[i]) {
                continue;
            }
            final NvDon t = thanhTuu.get(i);
            final String k = t.kieu == null ? "" : t.kieu.replace("thanh_tuu_", "");
            boolean dat;
            if ("cap".equals(k)) {
                dat = p.cap >= t.nguong;
            } else if ("chuong".equals(k)) {
                dat = p.chuong >= t.nguong;
            } else if ("nang_cap".equals(k)) {
                dat = p.daNangCapCao >= t.nguong;
            } else if ("ngay".equals(k)) {
                dat = p.ngayChoi >= t.nguong;
            } else if ("pokedex".equals(k)) {
                dat = p.dexIdx / 2 >= t.nguong;
            } else if ("phu".equals(k)) {
                dat = p.phuIdx >= t.nguong;
            } else {
                dat = false; // pvp/boss/doi/bo/su_kien: khong mo hinh
            }
            if (dat) {
                p.thanhTuuDat[i] = true;
                p.lk += t.lk;
                tongLkVao[ngay] += t.lk;
                p.lg += t.lg;
                tongLgVao += t.lg;
                p.ngayCuoiTienBo = ngay;
            }
        }
    }

    // ============================ THONG KE ============================
    void thongKe(final int ngay) {
        final int tran = tran(ngay);
        final int[][] demCap = new int[5][ThongSoLauDai.CAP_TOI_DA + 2];
        final double[] xu = new double[5], lk = new double[5], lg = new double[5], nc = new double[5],
                bac = new double[5], ch = new double[5], x1 = new double[5], x2 = new double[5], tt = new double[5],
                capS = new double[5], expS = new double[5];
        final int[] n = new int[5];
        final int[] nKt = new int[5];
        final double[] capKtS = new double[5];
        final List<Double> xuGio = new ArrayList<Double>();
        for (final NguoiChoi p : nguoi) {
            if (!p.conChoi || ngay < p.ngayVao) {
                continue;
            }
            for (final int g : new int[]{p.nhom, p.nhom == BOT ? -1 : 4}) {
                if (g < 0) {
                    continue;
                }
                n[g]++;
                demCap[g][p.cap]++;
                capS[g] += p.cap;
                xu[g] += p.xu;
                lk[g] += p.lk;
                lg[g] += p.lg;
                int s = 0;
                for (final int v : p.plus) {
                    s += v;
                }
                nc[g] += s / (double) p.plus.length;
                bac[g] += p.tier;
                ch[g] += p.chuong;
                x1[g] += p.ngayXongNam1 >= 0 ? 1 : 0;
                x2[g] += p.ngayXongNam2 >= 0 ? 1 : 0;
                tt[g] += p.tinhThong;
                expS[g] += p.ngayDangNhap == ngay ? p.expNgay : 0;
            }
            if (p.laKhaiTruong) {
                nKt[p.nhom]++;
                capKtS[p.nhom] += p.cap;
            }
            if (p.nhom != BOT) {
                xuGio.add(p.xu / Math.max(1, ThongSoLauDai.xuMoiGio(p.cap)));
                if (chiChuan && ngay % 30 == 29 && ngay >= 60) {
                    smTheoCap[p.cap / 10][0] += NapMoPhong.sucManhNguoi(p);
                    smTheoCap[p.cap / 10][1]++;
                }
            }
        }
        java.util.Collections.sort(xuGio);
        final double xuGioTv = xuGio.isEmpty() ? 0 : xuGio.get(xuGio.size() / 2);
        for (int g = 0; g < 5; g++) {
            final int c = Math.max(1, n[g]);
            xuTb[ngay][g] = xu[g] / c;
            capTb[ngay][g] = capS[g] / c;
            xongNam1[ngay][g] = 100.0 * x1[g] / c;
            xongNam2[ngay][g] = 100.0 * x2[g] / c;
            nangCapTb[ngay][g] = nc[g] / c;
            capKt[ngay][g] = capKtS[g] / Math.max(1, nKt[g]);
            final String ten = g == 4 ? "tat_ca" : TEN_NHOM[g];
            dongCsv.add(String.format(Locale.ROOT,
                    "%d,%s,%d,%.2f,%d,%d,%d,%d,%.0f,%.1f,%.1f,%.1f,%.0f,%.0f,%.2f,%.2f,%.2f,%.1f,%.1f,%.2f,%.0f",
                    ngay, ten, n[g], capS[g] / c, phanVi(demCap[g], n[g], 10), phanVi(demCap[g], n[g], 50),
                    phanVi(demCap[g], n[g], 90), tran, xu[g] / c, g == 4 ? xuGioTv : 0.0, lk[g] / c, lg[g] / c,
                    g == 4 ? tongFaucet[ngay] : 0.0, g == 4 ? tongSink[ngay] : 0.0, nc[g] / c, bac[g] / c,
                    ch[g] / c, 100.0 * x1[g] / c, 100.0 * x2[g] / c, tt[g] / c, expS[g] / c));
        }
        trungViCapNgayTruoc[0] = Math.max(1, phanVi(demCap[4], n[4], 50));
        capTrungViNgay.add(trungViCapNgayTruoc[0]);
        xuGioTrungVi = xuGioTv;
        xuGioTrungViNgay.add(xuGioTv);
    }

    double xuGioTrungVi;
    final List<Integer> capTrungViNgay = new ArrayList<Integer>();

    /** Xu TB/nguoi (nguoi that) chia thu nhap 1 gio o cap trung vi ngay do. */
    double xuThuc(final int ngay) {
        final int L = Math.max(1, capTrungViNgay.get(ngay));
        return xuTb[ngay][4] / ThongSoLauDai.xuMoiGio(L);
    }
    final List<Double> xuGioTrungViNgay = new ArrayList<Double>();

    static int phanVi(final int[] dem, final int n, final int pct) {
        if (n == 0) {
            return 0;
        }
        final int muc = (int) Math.ceil(n * pct / 100.0);
        int c = 0;
        for (int i = 0; i < dem.length; i++) {
            c += dem[i];
            if (c >= muc) {
                return i;
            }
        }
        return dem.length - 1;
    }

    // ============================ MUC TIEU ============================
    /** Tien trinh (chay tren dan CHUAN): {ten, "1"/"0", chi tiet}. */
    List<String[]> kiemTienTrinh() {
        final List<String[]> ra = new ArrayList<String[]>();
        // 1-2. Cot truyen nam 1: nhom NHE va CAY trong dan khai truong.
        final List<Integer> nhe = new ArrayList<Integer>(), cay = new ArrayList<Integer>();
        int nheTong = 0, cayTong = 0;
        for (final NguoiChoi p : nguoi) {
            if (!p.laKhaiTruong) {
                continue;
            }
            // Nguoi choi LIEN TUC (chua tung bo) - nguoi bo roi quay lai co khoang trong.
            final boolean conDen = p.ngayBoDau < 0 || (p.ngayXongNam1 >= 0 && p.ngayXongNam1 < p.ngayBoDau);
            if (p.nhom == NHE && conDen) {
                nheTong++;
                if (p.ngayXongNam1 >= 0) {
                    nhe.add(p.ngayXongNam1);
                }
            }
            if (p.nhom == CAY && conDen) {
                cayTong++;
                if (p.ngayXongNam1 >= 0) {
                    cay.add(p.ngayXongNam1);
                }
            }
        }
        java.util.Collections.sort(nhe);
        java.util.Collections.sort(cay);
        final int thangNhe = ThongSoLauDai.MT_NHE_XONG_CHINH_SOM_NHAT_THANG;
        final int thangCay = ThongSoLauDai.MT_CAY_XONG_CHINH_SOM_NHAT_THANG;
        final double nheSom = tiLeTruoc(nhe, nheTong, thangNhe * 30);
        final double caySom = tiLeTruoc(cay, cayTong, thangCay * 30);
        ra.add(new String[]{"Nhóm nhẹ: ≤5% xong cốt truyện năm 1 trước tháng " + thangNhe, nheSom <= 5 ? "1" : "0",
            String.format(Locale.ROOT, "%.1f%% (n=%d)", nheSom, nheTong)});
        final double nheTv = nhe.size() * 2 >= nheTong && !nhe.isEmpty() ? nhe.get(Math.max(0, (nheTong + 1) / 2 - 1)) / 30.0 : 99;
        if (soNgay >= 540) {
            ra.add(new String[]{"Nhóm nhẹ: trung vị xong cốt truyện năm 1 ≤ tháng "
                    + ThongSoLauDai.MT_NHE_XONG_CHINH_MUON_NHAT_THANG,
                nheTv <= ThongSoLauDai.MT_NHE_XONG_CHINH_MUON_NHAT_THANG ? "1" : "0",
                String.format(Locale.ROOT, "tháng %.1f", nheTv)});
        }
        ra.add(new String[]{"Nhóm cày: ≤5% xong cốt truyện năm 1 trước tháng " + thangCay, caySom <= 5 ? "1" : "0",
            String.format(Locale.ROOT, "%.1f%% (n=%d, sớm nhất ngày %s)", caySom, cayTong,
                    cay.isEmpty() ? "-" : String.valueOf(cay.get(0)))});
        tienHoaVaBot(ra);
        return ra;
    }

    /** Kinh te + giu chan (chay tren dan SO day du). */
    List<String[]> kiemKinhTe() {
        final List<String[]> ra = new ArrayList<String[]>();
        // 3. Tang tien THUC / nguoi hoat dong / thang (thang 4 -> cuoi). Gia ca (ho hut) deu tinh
        //    theo thu nhap 1 gio o cap -> chi so gia = xuMoiGio(cap trung vi). Danh nghia: chi in ra.
        final int thangCuoi = soNgay / 30;
        if (thangCuoi >= 5) {
            double tong = 0, tongDn = 0, maxThuc = 0;
            int dem = 0;
            for (int t = 4; t < thangCuoi; t++) {
                final int a = t * 30 - 1, b = Math.min(soNgay - 1, (t + 1) * 30 - 1);
                final double ta = xuThuc(a), tb = xuThuc(b);
                if (ta > 0) {
                    tong += (tb / ta - 1) * 100;
                    tongDn += (xuTb[b][4] / xuTb[a][4] - 1) * 100;
                    dem++;
                }
            }
            for (int d = 90; d < soNgay; d++) {
                maxThuc = Math.max(maxThuc, xuThuc(d));
            }
            final double tb = dem == 0 ? 0 : tong / dem;
            ra.add(new String[]{"Xu thực/người hoạt động tăng TB ≤ " + ThongSoLauDai.MT_TANG_TIEN_THUC_THANG_TOI_DA_PHAN_TRAM
                    + "%/tháng (tháng 4→cuối)", tb <= ThongSoLauDai.MT_TANG_TIEN_THUC_THANG_TOI_DA_PHAN_TRAM ? "1" : "0",
                String.format(Locale.ROOT, "TB %.2f%%/tháng thực (danh nghĩa %.2f%%)", tb, dem == 0 ? 0 : tongDn / dem)});
            ra.add(new String[]{"Xu TB/người ≤ " + ThongSoLauDai.MT_XU_THUC_TOI_DA_GIO + " giờ thu nhập ở cấp trung vị",
                maxThuc <= ThongSoLauDai.MT_XU_THUC_TOI_DA_GIO ? "1" : "0", String.format(Locale.ROOT, "cao nhất %.1f giờ", maxThuc)});
        }
        // 4. Sink / faucet (thang 3 -> cuoi).
        double f = 0, s = 0;
        for (int d = Math.min(90, soNgay / 2); d < soNgay; d++) {
            f += tongFaucet[d];
            s += tongSink[d];
        }
        final double tl = f == 0 ? 0 : s / f * 100;
        ra.add(new String[]{"Hố hút/nguồn xu " + ThongSoLauDai.MT_SINK_FAUCET_MIN_PHAN_TRAM + "–"
                + ThongSoLauDai.MT_SINK_FAUCET_MAX_PHAN_TRAM + "% (từ tháng 3)",
            tl >= ThongSoLauDai.MT_SINK_FAUCET_MIN_PHAN_TRAM && tl <= ThongSoLauDai.MT_SINK_FAUCET_MAX_PHAN_TRAM ? "1" : "0",
            String.format(Locale.ROOT, "%.1f%%", tl)});
        // 5. Xu dang giu (trung vi) / thu nhap 1 gio.
        double maxGio = 0;
        for (int d = Math.min(90, soNgay / 2); d < xuGioTrungViNgay.size(); d++) {
            maxGio = Math.max(maxGio, xuGioTrungViNgay.get(d));
        }
        ra.add(new String[]{"Trung vị xu đang giữ ≤ " + ThongSoLauDai.MT_XU_GIU_GIO_THU_NHAP_TOI_DA + " giờ thu nhập",
            maxGio <= ThongSoLauDai.MT_XU_GIU_GIO_THU_NHAP_TOI_DA ? "1" : "0",
            String.format(Locale.ROOT, "cao nhất %.1f giờ", maxGio)});
        // 6. Giu chan D1/D7/D30 (nguoi vao sau khai truong).
        final int[] d = {1, 7, 30};
        final int[] mt = {ThongSoLauDai.MT_D1_TOI_THIEU, ThongSoLauDai.MT_D7_TOI_THIEU, ThongSoLauDai.MT_D30_TOI_THIEU};
        for (int i = 0; i < d.length; i++) {
            int tong = 0, con = 0;
            for (final NguoiChoi p : nguoi) {
                if (p.nhom == BOT || p.ngayVao + d[i] >= soNgay) {
                    continue;
                }
                tong++;
                if (p.ngayBoDau < 0 || p.ngayBoDau > p.ngayVao + d[i]) {
                    con++;
                }
            }
            final double r = tong == 0 ? 0 : 100.0 * con / tong;
            ra.add(new String[]{"Giữ chân D" + d[i] + " ≥ " + mt[i] + "% (proxy)", r >= mt[i] ? "1" : "0",
                String.format(Locale.ROOT, "%.1f%%", r)});
        }
        return ra;
    }

    void tienHoaVaBot(final List<String[]> ra) {
        // 7. Tien hoa dau tien (cap 16) <= 1.2 gio choi (trung vi).
        final List<Double> th = new ArrayList<Double>();
        for (final NguoiChoi p : nguoi) {
            if (p.nhom != BOT && p.gioTienHoa >= 0) {
                th.add(p.gioTienHoa);
            }
        }
        java.util.Collections.sort(th);
        final double tv = th.isEmpty() ? 99 : th.get(th.size() / 2);
        ra.add(new String[]{"Tiến hoá đầu (cấp 16) ≤ " + ThongSoLauDai.MT_TIEN_HOA_DAU_GIO_TOI_DA_PHAN_TRAM / 100.0 + " giờ",
            tv * 100 <= ThongSoLauDai.MT_TIEN_HOA_DAU_GIO_TOI_DA_PHAN_TRAM ? "1" : "0",
            String.format(Locale.ROOT, "trung vị %.2f giờ", tv)});
        // 8. Bot khong hon 1.3 lan nhom cay ve gio hieu dung/ngay.
        final double hdCay = expHieuDungNhom[CAY] / Math.max(1, demExpHieuDungNhom[CAY]);
        final double hdBot = expHieuDungNhom[BOT] / Math.max(1, demExpHieuDungNhom[BOT]);
        ra.add(new String[]{"Bot 20 giờ ≤ 1,3 × nhóm cày (giờ hiệu dụng/ngày)", hdBot <= 1.3 * hdCay ? "1" : "0",
            String.format(Locale.ROOT, "bot %.2f, cày %.2f", hdBot, hdCay)});
    }

    // ============================ MUC TIEU TANG TOC (khong tran cap theo mua) ============================

    /**
     * Trung vi thang toi cap toi da (100) cua nhom {@code g} (dan khai truong); {@code chiKhongNap}: bo nguoi nap.
     * 99 neu chua toi mot nua.
     */
    static double thangCapToiDa(final MoPhongKinhTe m, final int g, final boolean chiKhongNap) {
        final List<Integer> d = new ArrayList<Integer>();
        int n = 0;
        for (final NguoiChoi p : m.nguoi) {
            if (p.nhom != g || !p.laKhaiTruong) {
                continue;
            }
            if (chiKhongNap && p.napNguoi != null && p.napNguoi.nhom != NapMoPhong.KHONG) {
                continue;
            }
            n++;
            if (p.ngay100 >= 0) {
                d.add(p.ngay100);
            }
        }
        if (n == 0 || d.size() * 2 < n) {
            return 99;
        }
        java.util.Collections.sort(d);
        return d.get((n + 1) / 2 - 1) / 30.0;
    }

    /**
     * Muc tieu tang toc (docs/thiet-ke-game.md / 10): {@code chuan} = dan chuan hon hop (ban khoa + nguoi nap
     * theo nhom nap), {@code khong} = cung dan, khong ai dung tang toc, {@code toiDa} = moi nguoi mua toi tran.
     */
    static List<String[]> kiemTangToc(final MoPhongKinhTe chuan, final MoPhongKinhTe khong, final MoPhongKinhTe toiDa) {
        final List<String[]> ra = new ArrayList<String[]>();
        final double nhe = thangCapToiDa(chuan, NHE, true), cay = thangCapToiDa(chuan, CAY, true),
                vua = thangCapToiDa(chuan, VUA, true);
        ra.add(new String[]{"Không nạp, nhẹ (1–2 giờ/ngày, dùng bản khoá kiếm được): trung vị tới cấp tối đa trong tháng "
                + ThongSoLauDai.MT_NHE_CAP_TOI_DA_THANG_MIN + "–" + ThongSoLauDai.MT_NHE_CAP_TOI_DA_THANG_MAX,
            nhe >= ThongSoLauDai.MT_NHE_CAP_TOI_DA_THANG_MIN && nhe <= ThongSoLauDai.MT_NHE_CAP_TOI_DA_THANG_MAX ? "1" : "0",
            String.format(Locale.ROOT, "tháng %.1f (vừa: tháng %.1f)", nhe, vua)});
        ra.add(new String[]{"Không nạp, cày (5–7 giờ/ngày): trung vị tới cấp tối đa trong tháng "
                + ThongSoLauDai.MT_CAY_CAP_TOI_DA_THANG_MIN + "–" + ThongSoLauDai.MT_CAY_CAP_TOI_DA_THANG_MAX,
            cay >= ThongSoLauDai.MT_CAY_CAP_TOI_DA_THANG_MIN && cay <= ThongSoLauDai.MT_CAY_CAP_TOI_DA_THANG_MAX ? "1" : "0",
            String.format(Locale.ROOT, "tháng %.1f", cay)});
        double maxNhanh = 0, maxSoKhoa = 0;
        final StringBuilder ct = new StringBuilder();
        for (final int g : new int[]{NHE, VUA, CAY}) {
            final double a = thangCapToiDa(khong, g, false), b = thangCapToiDa(toiDa, g, false), c = thangCapToiDa(chuan, g, true);
            final double up = (a / b - 1) * 100;
            maxNhanh = Math.max(maxNhanh, up);
            maxSoKhoa = Math.max(maxSoKhoa, (c / b - 1) * 100);
            ct.append(String.format(Locale.ROOT, "%s %.1f→%.1f tháng (+%.0f%%); ", TEN_NHOM[g], a, b, up));
        }
        ra.add(new String[]{"Mua tăng tốc tối đa nhanh hơn không dùng gì, cùng giờ chơi ≤ "
                + ThongSoLauDai.MT_TANG_TOC_NHANH_HON_TOI_DA_PHAN_TRAM + "%",
            maxNhanh <= ThongSoLauDai.MT_TANG_TOC_NHANH_HON_TOI_DA_PHAN_TRAM ? "1" : "0",
            String.format(Locale.ROOT, "%stối đa +%.1f%% (so với người không nạp dùng bản khoá: +%.1f%%)", ct, maxNhanh, maxSoKhoa)});
        // Suc manh cung cap (dai 10 cap, cap 1-99): tang toc chi cho EXP -> khong mua duoc suc manh.
        double gap = -100;
        for (int b = 0; b < 10; b++) {
            final double[] x = toiDa.smTheoCap[b], y = khong.smTheoCap[b];
            if (x[1] >= 30 && y[1] >= 30) {
                gap = Math.max(gap, (x[0] / x[1] / (y[0] / y[1]) - 1) * 100);
            }
        }
        ra.add(new String[]{"Sức mạnh cùng cấp (cấp 1–99): mua tăng tốc tối đa / không dùng ≤ +1%", gap <= 1.0 ? "1" : "0",
            String.format(Locale.ROOT, "chênh lớn nhất %+.2f%% (âm = người mua yếu hơn ở cùng cấp vì ít ngày tích đồ hơn)", gap)});
        // Bot: mua toi tran van khong vuot nguoi cay; diem tang toc khong vuot tran tuan.
        final TangTocMoPhong t = toiDa.tangToc;
        final double botNgay = (toiDa.expHieuDungNhom[BOT] + t.thuongNhom[BOT]) / Math.max(1, toiDa.demExpHieuDungNhom[BOT]);
        final double cayNgay = (toiDa.expHieuDungNhom[CAY] + t.thuongNhom[CAY]) / Math.max(1, toiDa.demExpHieuDungNhom[CAY]);
        ra.add(new String[]{"Bot mua tăng tốc tối đa ≤ 1,3 × nhóm cày (giờ EXP hiệu dụng/ngày, cả tăng tốc), ≤ "
                + com.htbb.pokemon.TangToc.DIEM_TUAN_TOI_DA + " điểm/tuần",
            botNgay <= 1.3 * cayNgay && t.maxDiemTuan <= com.htbb.pokemon.TangToc.DIEM_TUAN_TOI_DA ? "1" : "0",
            String.format(Locale.ROOT, "bot %.2f, cày %.2f giờ/ngày; điểm tuần cao nhất %.0f", botNgay, cayNgay, t.maxDiemTuan)});
        return ra;
    }

    static double tiLeTruoc(final List<Integer> ngay, final int tong, final int han) {
        int c = 0;
        for (final int x : ngay) {
            if (x < han) {
                c++;
            }
        }
        return tong == 0 ? 0 : 100.0 * c / tong;
    }

    /** Cho test (goi qua phan chieu): chay rut gon, tra ve {ten, dat, chi tiet}. */
    public static String[][] chayKiem(final String goc, final int soNgay, final int phanTramDanSo) throws Exception {
        final MoPhongKinhTe[] m = chayHai(new File(goc), soNgay, phanTramDanSo);
        return tongHop(m[0], m[1]).toArray(new String[0][]);
    }

    /**
     * So lieu dai han (ThongSoLauDai, lich su kien, danh muc cau hinh...) nap tu SQL (P1: bang la nguon duy nhat).
     * Chay rieng cong cu: doc {@code <goc>/sql/htbb_day_du.sql}; chay trong test: Kiem.main da nap, bo qua.
     */
    static void napDuLieuSql(final File goc) throws Exception {
        if (com.htbb.pokemon.ThongSoLauDai.EXP_LEN_CAP.length > 0) {
            return;
        }
        final com.htbb.dulieu.DuLieuSql.KetQua kq = com.htbb.dulieu.DuLieuSql.napTatCa(
                com.htbb.dulieu.DocDumpSql.tuTep(new File(goc, "sql/htbb_day_du.sql").getPath()));
        if (!kq.dat()) {
            throw new IllegalStateException("du lieu SQL hong: " + kq.loi);
        }
    }

    static MoPhongKinhTe[] chayHai(final File goc, final int soNgay, final int phanTramDanSo) throws Exception {
        napDuLieuSql(goc);
        final MoPhongKinhTe chuan = new MoPhongKinhTe();
        chuan.chiChuan = true;
        chuan.chay(goc, soNgay, 1.0, 20261017L);
        // Tang toc: cung dan chuan, cung hat giong - khong ai dung tang toc / MOI nguoi mua toi tran (so sanh cung gio).
        chuan.ssKhong = new MoPhongKinhTe();
        chuan.ssKhong.chiChuan = true;
        chuan.ssKhong.cheDoTangToc = TT_KHONG;
        chuan.ssKhong.chay(goc, soNgay, 1.0, 20261017L);
        chuan.ssToiDa = new MoPhongKinhTe();
        chuan.ssToiDa.chiChuan = true;
        chuan.ssToiDa.cheDoTangToc = TT_TOI_DA;
        chuan.ssToiDa.chay(goc, soNgay, 1.0, 20261017L);
        final MoPhongKinhTe danSo = new MoPhongKinhTe();
        danSo.chay(goc, soNgay, phanTramDanSo / 100.0, 20261017L);
        return new MoPhongKinhTe[]{chuan, danSo};
    }

    static List<String[]> tongHop(final MoPhongKinhTe chuan, final MoPhongKinhTe danSo) {
        final List<String[]> ra = chuan.kiemTienTrinh();
        ra.addAll(danSo.kiemKinhTe());
        if (chuan.ssKhong != null && chuan.ssToiDa != null && !chuan.tranTheoMua) {
            ra.addAll(kiemTangToc(chuan, chuan.ssKhong, chuan.ssToiDa));
        }
        return ra;
    }

    // ============================ GHI KET QUA ============================
    MoPhongKinhTe chuanDi;

    void ghi(final File goc) throws Exception {
        final File d = new File(goc, "mo_phong_ra");
        d.mkdirs();
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(d, "mo_phong_kinh_te.csv")),
                StandardCharsets.UTF_8))) {
            w.print(String.join(",", COT) + "\n");
            for (final String s : dongCsv) {
                w.print(s + "\n");
            }
        }
        final StringBuilder b = new StringBuilder();
        b.append("MO PHONG KINH TE - TOM TAT (sinh boi tools/pokemon/MoPhongKinhTe.java, KHONG SUA TAY)\n");
        b.append(String.format(Locale.ROOT, "So ngay %d, he so dan so %.2f, tong nguoi choi %d, bot bi khoa %d\n\n",
                soNgay, heSoDanSo, nguoi.size(), phatHienBot));
        b.append("MUC TIEU\n");
        int dat = 0;
        final List<String[]> kq = tongHop(chuanDi, this);
        for (final String[] r : kq) {
            b.append(r[1].equals("1") ? "  DAT   " : "  KHONG ").append(r[0]).append(": ").append(r[2]).append('\n');
            dat += r[1].equals("1") ? 1 : 0;
        }
        b.append(String.format("  => %d/%d\n\n", dat, kq.size()));
        b.append("THEO THANG (tat ca nguoi that, cuoi thang)\n");
        b.append(" thang  hoat_dong  cap_tb  p10  p50  p90  tran  xu_tb        xu_giu(gio)  nang_cap  xong_nam1%  xong_nam2%\n");
        for (int t = 1; t <= soNgay / 30; t++) {
            final int n = t * 30 - 1;
            final String[] o = dongCsv.get(n * 5 + 4).split(",");
            b.append(String.format(Locale.ROOT, " %5d  %9s  %6s  %3s  %3s  %3s  %4s  %-11s  %11s  %8s  %10s  %10s\n", t, o[2], o[3], o[4], o[5], o[6],
                    o[7], o[8], o[9], o[14], o[17], o[18]));
        }
        b.append("\nDAN CHUAN (300/nhom tu khai truong, khong bo choi), cuoi thang: cap_tb | nang_cap_tb | xong_nam1% | xong_nam2%\n");
        for (int g = 0; g < SO_NHOM; g++) {
            b.append("  ").append(TEN_NHOM[g]).append(":");
            for (final int t : new int[]{3, 6, 9, 12, 15, 18, 21, 24}) {
                final int n = Math.min(soNgay - 1, t * 30 - 1);
                b.append(String.format(Locale.ROOT, "  T%d %.1f|%.1f|%.0f%%|%.0f%%", t, chuanDi.capTb[n][g], chuanDi.nangCapTb[n][g],
                        chuanDi.xongNam1[n][g], chuanDi.xongNam2[n][g]));
            }
            b.append('\n');
        }
        b.append("\nNGUON / HO HUT XU (toan bo, % tong)\n");
        double tf = 0, ts = 0;
        for (int i = 0; i < soNgay; i++) {
            tf += tongFaucet[i];
            ts += tongSink[i];
        }
        for (final String k : sapXep(faucetTheoNguon)) {
            b.append(String.format(Locale.ROOT, "  vao %-14s %6.1f%%\n", k, 100 * tong(faucetTheoNguon.get(k)) / tf));
        }
        for (final String k : sapXep(sinkTheoNguon)) {
            b.append(String.format(Locale.ROOT, "  ra  %-14s %6.1f%%\n", k, 100 * tong(sinkTheoNguon.get(k)) / ts));
        }
        double lkv = 0, lkr = 0;
        for (int i = 0; i < soNgay; i++) {
            lkv += tongLkVao[i];
            lkr += tongLkRa[i];
        }
        b.append(String.format(Locale.ROOT, "  tong xu ra/vao = %.1f%%; luong khoa ra/vao = %.1f%%; luong ra/vao = %.1f%%\n",
                100 * ts / tf, 100 * lkr / lkv, 100 * tongLgRa / Math.max(1, tongLgVao)));
        if (hoiSinhBat) {
            b.append("\nGUC / HOI SINH (mo-dun HoiSinhMoPhong, dan so hon hop)\n").append(hoiSinh().tomTat());
        }
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(d, "mo_phong_tom_tat.txt")),
                StandardCharsets.UTF_8))) {
            w.print(b);
        }
        System.out.print(b);
        veBieuDo(new File(d, "mo_phong_xu.png"), "Xu trung bình / người đang chơi (toàn máy chủ)", xuTb, true, false);
        veBieuDo(new File(d, "mo_phong_cap.png"), "Cấp trung bình, dân chuẩn từ khai trương (nét đứt: trần cấp)",
                chuanDi.capTb, false, true);
        veBieuDo(new File(d, "mo_phong_cot_truyen.png"), "% đã xong cốt truyện năm 1 (dân chuẩn)", chuanDi.xongNam1, true, false);
        veBieuDo(new File(d, "mo_phong_nang_cap.png"), "Cấp nâng cấp trung bình +k (dân chuẩn)", chuanDi.nangCapTb, true, false);
    }

    static double tong(final double[] a) {
        double s = 0;
        for (final double x : a) {
            s += x;
        }
        return s;
    }

    static List<String> sapXep(final Map<String, double[]> m) {
        final List<String> k = new ArrayList<String>(m.keySet());
        java.util.Collections.sort(k, (a, b) -> Double.compare(tong(m.get(b)), tong(m.get(a))));
        return k;
    }

    // Mau: bang categorical mac dinh (skill dataviz), thu tu co dinh theo nhom.
    static final Color[] MAU = {new Color(0x2a78d6), new Color(0xeb6834), new Color(0x1baf7a), new Color(0xeda100),
        new Color(0x52514e)};
    static final String[] NHAN = {"nhẹ (1–2 giờ)", "vừa (~3 giờ)", "cày (5–7 giờ)", "bot (20 giờ)", "tất cả"};

    void veBieuDo(final File f, final String tieuDe, final double[][] du, final boolean boBot, final boolean laCap) throws Exception {
        final int W = 960, H = 520, L = 90, R = 190, T = 50, B = 50;
        final BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(0xfcfcfb));
        g.fillRect(0, 0, W, H);
        double max = 0;
        final int[] nhom = boBot ? new int[]{0, 1, 2, 4} : new int[]{0, 1, 2, 3, 4};
        for (int d = 0; d < soNgay; d++) {
            for (final int k : nhom) {
                max = Math.max(max, du[d][k]);
            }
        }
        if (laCap) {
            max = 100;
        }
        max = max <= 0 ? 1 : max * 1.05;
        final int pw = W - L - R, ph = H - T - B;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int i = 0; i <= 5; i++) {
            final int y = T + ph - ph * i / 5;
            g.setColor(new Color(0xe6e5e0));
            g.drawLine(L, y, L + pw, y);
            g.setColor(new Color(0x52514e));
            final double v = max * i / 5;
            g.drawString(v >= 1e6 ? String.format(Locale.ROOT, "%.1fM", v / 1e6) : (v >= 1e3 ? String.format(Locale.ROOT, "%.0fk", v / 1e3)
                    : String.format(Locale.ROOT, "%.0f", v)), 10, y + 4);
        }
        for (int m = 0; m <= soNgay / 30; m += 3) {
            final int x = L + pw * m * 30 / Math.max(1, soNgay - 1);
            g.setColor(new Color(0x52514e));
            g.drawString("T" + m, x - 6, T + ph + 18);
        }
        g.drawString("tháng", L + pw / 2 - 15, H - 10);
        if (laCap) {
            g.setColor(new Color(0xbdbcb5));
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{5, 4}, 0));
            int px = -1, py = -1;
            for (int d = 0; d < soNgay; d++) {
                final int x = L + pw * d / Math.max(1, soNgay - 1);
                final int y = T + ph - (int) (ph * tran(d) / max);
                if (px >= 0) {
                    g.drawLine(px, py, x, y);
                }
                px = x;
                py = y;
            }
        }
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int dongNhan = 0;
        for (final int k : nhom) {
            g.setColor(MAU[k == 4 ? 4 : k]);
            int px = -1, py = -1;
            for (int d = 0; d < soNgay; d++) {
                final int x = L + pw * d / Math.max(1, soNgay - 1);
                final int y = T + ph - (int) (ph * du[d][k] / max);
                if (px >= 0) {
                    g.drawLine(px, py, x, y);
                }
                px = x;
                py = y;
            }
            g.fillRect(W - R + 20, T + 10 + dongNhan * 22, 14, 4);
            g.setColor(new Color(0x0b0b0b));
            g.drawString(NHAN[k] + String.format(Locale.ROOT, ": %s", laCap ? String.format(Locale.ROOT, "%.0f", du[soNgay - 1][k])
                    : (du[soNgay - 1][k] >= 1e3 ? String.format(Locale.ROOT, "%.0fk", du[soNgay - 1][k] / 1e3)
                    : String.format(Locale.ROOT, "%.1f", du[soNgay - 1][k]))), W - R + 40, T + 16 + dongNhan * 22);
            dongNhan++;
        }
        g.setColor(new Color(0x0b0b0b));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.drawString(tieuDe, L, 30);
        g.dispose();
        javax.imageio.ImageIO.write(img, "png", f);
    }

    public static void main(final String[] a) throws Exception {
        final int soNgay = a.length > 0 ? Integer.parseInt(a[0]) : 730;
        final int pct = a.length > 1 ? Integer.parseInt(a[1]) : 100;
        final long t0 = System.nanoTime();
        final MoPhongKinhTe[] hai = chayHai(new File("."), soNgay, pct);
        final MoPhongKinhTe m = hai[1];
        m.chuanDi = hai[0];
        m.ghi(new File("."));
        // Kich ban so sanh (chi in, khong kiem).
        final StringBuilder b = new StringBuilder("\nKICH BAN SO SANH (dan so day du)\n");
        for (int k = 0; k < 2; k++) {
            final MoPhongKinhTe x = new MoPhongKinhTe();
            x.khongMet = k == 0;
            x.nangCapCoDinh = k == 1;
            x.chay(new File("."), soNgay, pct / 100.0, 20261017L);
            final MoPhongKinhTe c = new MoPhongKinhTe();
            c.chiChuan = true;
            c.khongMet = x.khongMet;
            c.nangCapCoDinh = x.nangCapCoDinh;
            c.chay(new File("."), soNgay, 1.0, 20261017L);
            b.append(k == 0 ? "  [A] Khong co nang luong huan luyen:\n" : "  [B] Nang cap gia co dinh 5.000 xu, khong ngoc he/trang tri (nhu game cu):\n");
            for (final String[] r : tongHop(c, x)) {
                if (r[0].startsWith("Bot") || r[0].startsWith("Xu") || r[0].startsWith("Hố") || r[0].startsWith("Nhóm cày")) {
                    b.append("      ").append(r[1].equals("1") ? "DAT   " : "KHONG ").append(r[0]).append(": ").append(r[2]).append('\n');
                }
            }
        }
        // nap tien: 6 muc tieu rieng (dung lai dan so hon hop vua chay).
        chayNapTien(new File("."), soNgay, pct, m);
        b.append(lastNapTomTat);
        b.append(HeSinhThaiMoPhong.tomTat(m)); // he sinh thai
        System.out.print(b);
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                new File("mo_phong_ra/mo_phong_tom_tat.txt"), true), StandardCharsets.UTF_8))) {
            w.print(b);
        }
        System.out.printf(Locale.ROOT, "(%.1f giay)%n", (System.nanoTime() - t0) / 1e9);
    }

    // ============================ NAP TIEN (docs/van-hanh.md) ============================
    // Mo-dun ROI: 3 nhom nguoi tra tien (khong nap 85%, nap nhe 12%, ca voi 3%), goi luong ty gia co
    // dinh, the thang, ve mua (free + cao cap), cua hang trang tri (luong bi dot), cho trang tri (xu
    // chuyen tay, thue 7% dot). RNG RIENG (khong doi chuoi so cua mo phong goc). Kiem: chayKiemNapTien.
    /** 0 = khong co he thong nap (kich ban so sanh), 1 = hon hop 85/12/3. */
    static final int NAP_TAT = 0, NAP_HON_HOP = 1;
    int napCheDo = NAP_HON_HOP;
    int soChuanMoiNhom = SO_CHUAN_MOI_NHOM;
    NapMoPhong nap;

    NapMoPhong nap() {
        if (nap == null) {
            nap = new NapMoPhong(this, trangThaiRng ^ 0x6E61705469656E31L);
        }
        return nap;
    }

    static final class NapMoPhong {
        static final int KHONG = 0, NHE_NAP = 1, CA_VOI = 2;
        static final String[] TEN = {"không nạp", "nạp nhẹ", "cá voi"};
        static final double[] TI_TRONG = {0.85, 0.12, 0.03};
        /** Goi luong = NapTienLogic.GOI_MAC_DINH (10 luong / 1.000 d). */
        static final int[] GOI = {200, 500, 1000, 2000, 5000};
        /** Gia mon trang tri (luong) - lay tu NapTienLogic.MON_MAC_DINH. */
        static final int[] GIA_MON;
        static {
            final List<Integer> g = new ArrayList<Integer>();
            for (final com.htbb.pokemon.NapTienLogic.Mon mon : com.htbb.pokemon.NapTienLogic.MON_MAC_DINH) {
                g.add(mon.giaLuong);
            }
            GIA_MON = new int[g.size()];
            for (int i = 0; i < GIA_MON.length; i++) {
                GIA_MON[i] = g.get(i);
            }
        }
        /** Xac suat muon mua 1 mon trang tri / ngay choi. */
        static final double[] MUON_MUA = {0, 0.05, 0.25};

        static final class Nguoi {
            int nhom, thangVnd = -1, tuanVnd = -1, muaVe = -1, diemVe, bacFree, bacCao, monGiaoDich;
            double lg, vndThang, vndTuan;
            int hetHanThe;
            boolean caoCap;
        }

        final MoPhongKinhTe m;
        long rng;
        double lgVao, lgRa, lgVaoTu90, lgRaTu90, vnd, thueCho, daTuVe;
        int soGiaoDich, soNguoiNap;
        final double[] vndThang = new double[40];
        final double[] soDuTb;
        final double[] giaGioNgay;
        double giaGio = 1.0;
        /** Gia nguoi nap chiu ban (gio thu nhap trung vi): thap hon thi giu mon lai. */
        static final double GIA_GIU_LAI_GIO = 1.0;
        final List<Double> giaGanDay = new ArrayList<Double>();
        final List<NguoiChoi> ban = new ArrayList<NguoiChoi>();
        final List<NguoiChoi> mua = new ArrayList<NguoiChoi>();
        final List<Double> nganSachMua = new ArrayList<Double>();
        /** [thang][nhom choi 0..2][nhom nap 0..2] = {tong suc manh, so nguoi}. */
        final double[][][][] sucManh = new double[30][3][3][2];

        NapMoPhong(final MoPhongKinhTe m, final long hat) {
            this.m = m;
            this.rng = hat;
            soDuTb = new double[m.soNgay];
            giaGioNgay = new double[m.soNgay];
        }

        double ngauNhien() {
            rng += 0x9E3779B97F4A7C15L;
            long z = rng;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            z = z ^ (z >>> 31);
            return (z >>> 11) * 0x1.0p-53;
        }

        void themNguoi(final NguoiChoi p) {
            final Nguoi n = new Nguoi();
            final double u = ngauNhien();
            n.nhom = p.nhom == BOT ? KHONG : (u < TI_TRONG[CA_VOI] ? CA_VOI : (u < TI_TRONG[CA_VOI] + TI_TRONG[NHE_NAP] ? NHE_NAP : KHONG));
            p.napNguoi = n;
        }

        /** Tra tien that (VND) neu con trong tran tuan/thang (NapTienLogic.kiemTran). */
        boolean tra(final Nguoi n, final int ngay, final int vndGia) {
            final int thang = ngay / 30, tuan = ngay / 7;
            if (n.thangVnd != thang) {
                n.thangVnd = thang;
                n.vndThang = 0;
            }
            if (n.tuanVnd != tuan) {
                n.tuanVnd = tuan;
                n.vndTuan = 0;
            }
            if (com.htbb.pokemon.NapTienLogic.kiemTran((long) n.vndTuan, (long) n.vndThang, vndGia, 0, 0, 0) != null) {
                return false;
            }
            n.vndThang += vndGia;
            n.vndTuan += vndGia;
            vnd += vndGia;
            vndThang[Math.min(vndThang.length - 1, thang)] += vndGia;
            return true;
        }

        void vaoLuong(final Nguoi n, final int ngay, final double l) {
            n.lg += l;
            lgVao += l;
            if (ngay >= 90) {
                lgVaoTu90 += l;
            }
        }

        void raLuong(final Nguoi n, final int ngay, final double l) {
            n.lg -= l;
            lgRa += l;
            if (ngay >= 90) {
                lgRaTu90 += l;
            }
        }

        /** Moi ngay choi (khong phai bot), sau nang cap / trang tri cua mo phong goc. */
        void moiNgay(final NguoiChoi p, final int ngay) {
            final Nguoi n = p.napNguoi;
            if (n == null) {
                return;
            }
            // Ve mua: moi nguoi deu co track free; cao cap = tra 99.000 d.
            final int s = com.htbb.pokemon.NapTienLogic.muaVe(ngay);
            if (n.muaVe != s) {
                n.muaVe = s;
                n.diemVe = 0;
                n.bacFree = 0;
                n.bacCao = 0;
                n.caoCap = (n.nhom == CA_VOI || (n.nhom == NHE_NAP && ngauNhien() < 0.5)) && tra(n, ngay, 99000);
            }
            // The thang 50.000 d / 30 ngay: ca voi luon gia han, nap nhe 25% moi thang.
            if (n.hetHanThe <= ngay && (n.nhom == CA_VOI || (n.nhom == NHE_NAP && ngay % 30 == 0 && ngauNhien() < 0.25))
                    && tra(n, ngay, 50000)) {
                n.hetHanThe = ngay + com.htbb.pokemon.NapTienLogic.THE_THANG_SO_NGAY;
            }
            final boolean coThe = n.hetHanThe > ngay;
            if (coThe) {
                vaoLuong(n, ngay, com.htbb.pokemon.NapTienLogic.THE_THANG_LUONG_NGAY);
            }
            // Diem ve: online 1/3 phut (tran 40) + 3 nhiem vu ngay x10 + luot ham nguc the thang 20; tran 120.
            final int online = (int) Math.min(com.htbb.pokemon.NapTienLogic.VE_MUA_DIEM_ONLINE_NGAY,
                    GIO_NGAY[p.nhom] * 60 / com.htbb.pokemon.NapTienLogic.VE_MUA_PHUT_MOI_DIEM);
            final int diem = Math.min(com.htbb.pokemon.NapTienLogic.VE_MUA_DIEM_NGAY_TOI_DA,
                    online + 30 + (coThe ? com.htbb.pokemon.NapTienLogic.THE_THANG_DIEM_VE_MUA_LUOT_THEM : 0));
            n.diemVe += diem;
            final int bac = com.htbb.pokemon.NapTienLogic.bacTuDiem(n.diemVe);
            while (n.bacFree < bac) {
                p.da += soDa(com.htbb.pokemon.NapTienLogic.thuongBac(false, ++n.bacFree));
            }
            while (n.caoCap && n.bacCao < bac) {
                final int d = soDa(com.htbb.pokemon.NapTienLogic.thuongBac(true, ++n.bacCao));
                p.da += d;
                daTuVe += d;
            }
            // Mua trang tri bang luong (luong bi dot). Thieu thi mua goi nho nhat du (ca voi: >= 1.000).
            if (n.nhom != KHONG) {
                double q = MUON_MUA[n.nhom] + (n.lg >= 600 ? 0.3 : 0);
                if (ngauNhien() < q) {
                    final int gia = GIA_MON[(int) (ngauNhien() * GIA_MON.length)];
                    if (n.lg < gia) {
                        int goi = GOI[GOI.length - 1];
                        for (final int g : GOI) {
                            if (g >= gia - n.lg && (n.nhom != CA_VOI || g >= 1000)) {
                                goi = g;
                                break;
                            }
                        }
                        if (tra(n, ngay, goi * 1000 / com.htbb.pokemon.NapTienLogic.LUONG_MOI_1000_VND)) {
                            vaoLuong(n, ngay, goi);
                        }
                    }
                    if (n.lg >= gia) {
                        raLuong(n, ngay, gia);
                        // Mon giao dich duoc: moi mon toi da 1 lan / nhan vat (gioi_han) -> ton kho co tran 3.
                        if (ngauNhien() < 0.5 && n.monGiaoDich < 3) {
                            n.monGiaoDich++;
                        }
                    }
                }
                // Rao ban mon giao dich duoc (kenh "ban vang" co thue) - chi khi gia >= gia giu lai (1 gio).
                if (n.monGiaoDich > 0 && giaGio >= GIA_GIU_LAI_GIO && ngauNhien() < 0.05) {
                    ban.add(p);
                }
            } else if (p.xu > 6 * ThongSoLauDai.xuMoiGio(p.cap) && ngauNhien() < 0.05) {
                // Nguoi khong nap co xu du (> 6 gio thu nhap, nguong ngoc he) muon mua trang tri bang xu.
                mua.add(p);
                nganSachMua.add(0.5 * (p.xu - 6 * ThongSoLauDai.xuMoiGio(p.cap)));
            }
        }

        static int soDa(final String t) {
            return t.startsWith("DA:") ? Integer.parseInt(t.substring(3)) : 0;
        }

        /** Cuoi ngay: khop cho, cap nhat gia (bien 50-200% trung vi), thong ke. */
        void cuoiNgay(final int ngay) {
            final double gioTv = ThongSoLauDai.xuMoiGio(Math.max(1, m.trungViCapNgayTruoc[0]));
            int khop = 0;
            for (int i = 0, j = 0; i < mua.size() && j < ban.size(); i++) {
                final NguoiChoi b = mua.get(i);
                final double gia = giaGio * gioTv;
                if (nganSachMua.get(i) < gia || b.xu < gia) {
                    continue;
                }
                final NguoiChoi s = ban.get(j++);
                b.xu -= gia;
                final double thue = gia * (com.htbb.pokemon.NapTienLogic.thueBanPhanTram()
                        + com.htbb.pokemon.NapTienLogic.phiDangPhanTram()) / 100.0;
                s.xu += gia - thue;
                m.ra("thue_cho_trang_tri", ngay, thue);
                thueCho += thue;
                s.napNguoi.monGiaoDich--;
                khop++;
                giaGanDay.add(giaGio);
                if (giaGanDay.size() > com.htbb.pokemon.NapTienLogic.SO_GIAO_DICH_THAM_CHIEU) {
                    giaGanDay.remove(0);
                }
            }
            soGiaoDich += khop;
            // Cung - cau -> gia (gio thu nhap): con nguoi mua cho -> tang, con hang ton -> giam. Gia rao
            // trong game chon tu 0,5..8 gio thu nhap (NapTienLogic.HE_SO_GIA_GIO_PHAN_TRAM) va phai trong
            // 50-200% trung vi giao dich gan day.
            // Gia "dinh" (nguoi ban doi gia rao tu tu): moi ngay toi da +-0,3% (~9%/thang).
            if (mua.size() > khop && ban.size() <= khop) {
                giaGio *= 1.003;
            } else if (ban.size() > khop) {
                giaGio *= 0.997;
            }
            giaGio = Math.max(com.htbb.pokemon.NapTienLogic.HE_SO_GIA_GIO_PHAN_TRAM[0] / 100.0,
                    Math.min(com.htbb.pokemon.NapTienLogic.HE_SO_GIA_GIO_PHAN_TRAM[
                            com.htbb.pokemon.NapTienLogic.HE_SO_GIA_GIO_PHAN_TRAM.length - 1] / 100.0, giaGio));
            if (giaGanDay.size() >= com.htbb.pokemon.NapTienLogic.SO_GIAO_DICH_TOI_THIEU) {
                final List<Double> s = new ArrayList<Double>(giaGanDay);
                java.util.Collections.sort(s);
                final double tv = s.get(s.size() / 2);
                giaGio = Math.max(tv * com.htbb.pokemon.NapTienLogic.BIEN_GIA_DUOI_PHAN_TRAM / 100.0,
                        Math.min(tv * com.htbb.pokemon.NapTienLogic.BIEN_GIA_TREN_PHAN_TRAM / 100.0, giaGio));
            }
            giaGioNgay[ngay] = giaGio;
            ban.clear();
            mua.clear();
            nganSachMua.clear();
            // So du luong nap TB / nguoi nap dang choi + suc manh cuoi thang.
            double tong = 0;
            int dem = 0;
            final boolean cuoiThang = ngay % 30 == 29;
            for (final NguoiChoi p : m.nguoi) {
                if (!p.conChoi || ngay < p.ngayVao || p.napNguoi == null || p.nhom == BOT) {
                    continue;
                }
                if (p.napNguoi.nhom != KHONG) {
                    tong += p.napNguoi.lg;
                    dem++;
                }
                if (cuoiThang && ngay / 30 < sucManh.length) {
                    final double[] o = sucManh[ngay / 30][p.nhom][p.napNguoi.nhom];
                    o[0] += sucManhNguoi(p);
                    o[1]++;
                }
            }
            soNguoiNap = dem;
            soDuTb[ngay] = dem == 0 ? 0 : tong / dem;
        }

        /** Chi so suc manh: % chi so tu +k trung binh 11 o x tinh thong x ngoc he (1%/cap/o). */
        static double sucManhNguoi(final NguoiChoi p) {
            double pc = 0;
            for (final int v : p.plus) {
                for (int k = 0; k < v; k++) {
                    pc += ThongSoLauDai.CHI_SO_NC_PHAN_TRAM[k];
                }
            }
            pc /= p.plus.length;
            return (1 + pc / 100.0) * (1 + 0.002 * p.tinhThong) * (1 + 0.01 * p.ngoc / ThongSoLauDai.SO_O_TRANG_BI);
        }

        /** Chenh suc manh {max moi nhom choi & thang tu 3: nguoi nap (nhe+ca voi), ca voi} so voi khong nap. */
        double[] chenh(final int soThang) {
            double maxNap = 0, maxCa = 0;
            for (int t = 2; t < Math.min(soThang, sucManh.length); t++) {
                for (int g = 0; g < 3; g++) {
                    final double[][] o = sucManh[t][g];
                    if (o[KHONG][1] < 10) {
                        continue;
                    }
                    final double goc = o[KHONG][0] / o[KHONG][1];
                    final double napN = o[NHE_NAP][1] + o[CA_VOI][1];
                    if (napN >= 10) {
                        maxNap = Math.max(maxNap, ((o[NHE_NAP][0] + o[CA_VOI][0]) / napN / goc - 1) * 100);
                    }
                    if (o[CA_VOI][1] >= 10) {
                        maxCa = Math.max(maxCa, (o[CA_VOI][0] / o[CA_VOI][1] / goc - 1) * 100);
                    }
                }
            }
            return new double[]{maxNap, maxCa};
        }
    }

    /** Ti le ra/vao xu tu thang 3 (cung cach kiemKinhTe). */
    double tiLeXuTuThang3() {
        double f = 0, s = 0;
        for (int d = Math.min(90, soNgay / 2); d < soNgay; d++) {
            f += tongFaucet[d];
            s += tongSink[d];
        }
        return f == 0 ? 0 : s / f * 100;
    }

    /** Tang xu thuc TB/thang tu thang 4 (cung cach kiemKinhTe). */
    double tangXuThucTb() {
        double tong = 0;
        int dem = 0;
        for (int t = 4; t < soNgay / 30; t++) {
            final int a = t * 30 - 1, b = Math.min(soNgay - 1, (t + 1) * 30 - 1);
            final double ta = xuThuc(a), tb = xuThuc(b);
            if (ta > 0) {
                tong += (tb / ta - 1) * 100;
                dem++;
            }
        }
        return dem == 0 ? 0 : tong / dem;
    }

    /**
     * Muc tieu nap tien (docs/van-hanh.md): {ten, "1"/"0", chi tiet}.
     * chuan = dan chuan hon hop (1.000/nhom), danSo = dan so hon hop, tat = dan so KHONG co nap.
     */
    static List<String[]> kiemNapTien(final MoPhongKinhTe chuan, final MoPhongKinhTe danSo, final MoPhongKinhTe tat) {
        final List<String[]> ra = new ArrayList<String[]>();
        final double[] c = chuan.nap().chenh(chuan.soNgay / 30);
        ra.add(new String[]{"Nạp: chênh sức mạnh người nạp / không nạp cùng giờ chơi ≤ 5% (cả cá voi)",
            c[0] <= 5 && c[1] <= 5 ? "1" : "0",
            String.format(Locale.ROOT, "người nạp tối đa %.2f%%, cá voi tối đa %.2f%%", c[0], c[1])});
        final double r1 = danSo.tiLeXuTuThang3(), r0 = tat.tiLeXuTuThang3();
        ra.add(new String[]{"Nạp: hố hút/nguồn xu vẫn 80–105% và lệch ≤ 2 điểm so với không có nạp",
            r1 >= ThongSoLauDai.MT_SINK_FAUCET_MIN_PHAN_TRAM && r1 <= ThongSoLauDai.MT_SINK_FAUCET_MAX_PHAN_TRAM
                && Math.abs(r1 - r0) <= 2 ? "1" : "0",
            String.format(Locale.ROOT, "%.1f%% (không nạp %.1f%%)", r1, r0)});
        final double g1 = danSo.tangXuThucTb(), g0 = tat.tangXuThucTb();
        ra.add(new String[]{"Nạp: xu thực tăng TB/tháng lệch ≤ 1 điểm % so với không có nạp",
            Math.abs(g1 - g0) <= 1 && g1 <= ThongSoLauDai.MT_TANG_TIEN_THUC_THANG_TOI_DA_PHAN_TRAM ? "1" : "0",
            String.format(Locale.ROOT, "%.2f%%/tháng (không nạp %.2f%%)", g1, g0)});
        final NapMoPhong n = danSo.nap();
        final double dot = n.lgVaoTu90 == 0 ? 0 : 100 * n.lgRaTu90 / n.lgVaoTu90;
        ra.add(new String[]{"Nạp: lượng nạp bị đốt ≥ " + ThongSoLauDai.MT_LUONG_DOT_TOI_THIEU_PHAN_TRAM + "% (từ tháng 3, gồm tăng tốc)",
            dot >= ThongSoLauDai.MT_LUONG_DOT_TOI_THIEU_PHAN_TRAM ? "1" : "0",
            String.format(Locale.ROOT, "%.1f%% (vào %.0f, đốt %.0f)", dot, n.lgVaoTu90, n.lgRaTu90)});
        double maxSoDu = 0;
        for (int d = 90; d < danSo.soNgay; d++) {
            maxSoDu = Math.max(maxSoDu, n.soDuTb[d]);
        }
        final int cuoi = danSo.soNgay - 1;
        final double dau6 = n.soDuTb[Math.max(90, cuoi - 180)];
        final double troi = dau6 <= 0 ? 0 : (n.soDuTb[cuoi] / dau6 - 1) * 100 / 6.0;
        ra.add(new String[]{"Nạp: số dư lượng TB/người nạp ≤ 1 gói (1.000), trôi ≤ 5%/tháng (6 tháng cuối)",
            maxSoDu <= 1000 && troi <= 5 ? "1" : "0",
            String.format(Locale.ROOT, "cao nhất %.0f lượng, trôi %.2f%%/tháng", maxSoDu, troi)});
        double maxDoi = 0, lo = Double.MAX_VALUE, hi = 0;
        for (int t = 3; t < danSo.soNgay / 30; t++) {
            final double a = n.giaGioNgay[t * 30 - 1], b = n.giaGioNgay[Math.min(cuoi, (t + 1) * 30 - 1)];
            if (a > 0) {
                maxDoi = Math.max(maxDoi, Math.abs(b / a - 1) * 100);
            }
            lo = Math.min(lo, b);
            hi = Math.max(hi, b);
        }
        final double moc = n.giaGioNgay[Math.min(cuoi, 89)];
        ra.add(new String[]{"Nạp: giá chợ trang trí ổn định (≤ 10%/tháng, trong ±50% giá tháng 3)",
            maxDoi <= 10 && lo >= 0.5 * moc && hi <= 1.5 * moc ? "1" : "0",
            String.format(Locale.ROOT, "đổi tối đa %.1f%%/tháng, %.2f–%.2f giờ (tháng 3: %.2f), %d giao dịch",
                    maxDoi, lo, hi, moc, n.soGiaoDich)});
        return ra;
    }

    /** Cho test (phan chieu): {ten, dat, chi tiet} cua 6 muc tieu nap tien. */
    public static String[][] chayKiemNapTien(final String goc, final int soNgay, final int phanTramDanSo) throws Exception {
        return chayNapTien(new File(goc), soNgay, phanTramDanSo, null).toArray(new String[0][]);
    }

    static List<String[]> chayNapTien(final File goc, final int soNgay, final int phanTramDanSo, final MoPhongKinhTe danSoCo)
            throws Exception {
        napDuLieuSql(goc);
        final MoPhongKinhTe chuan = new MoPhongKinhTe();
        chuan.chiChuan = true;
        chuan.soChuanMoiNhom = 1000;
        chuan.chay(goc, soNgay, 1.0, 20261017L);
        MoPhongKinhTe danSo = danSoCo;
        if (danSo == null) {
            danSo = new MoPhongKinhTe();
            danSo.chay(goc, soNgay, phanTramDanSo / 100.0, 20261017L);
        }
        final MoPhongKinhTe tat = new MoPhongKinhTe();
        tat.napCheDo = NAP_TAT;
        tat.chay(goc, soNgay, phanTramDanSo / 100.0, 20261017L);
        final List<String[]> kq = kiemNapTien(chuan, danSo, tat);
        final NapMoPhong n = danSo.nap();
        final StringBuilder b = new StringBuilder();
        b.append("\nNAP TIEN (docs/van-hanh.md) - dan so hon hop 85/12/3, so voi khong co nap\n");
        int dat = 0;
        for (final String[] r : kq) {
            b.append(r[1].equals("1") ? "  DAT   " : "  KHONG ").append(r[0]).append(": ").append(r[2]).append('\n');
            dat += r[1].equals("1") ? 1 : 0;
        }
        b.append(String.format("  => %d/%d\n", dat, kq.size()));
        double vndT = 0;
        int soThang = 0;
        for (int t = 1; t < Math.min(n.vndThang.length, soNgay / 30); t++) {
            vndT += n.vndThang[t];
            soThang++;
        }
        b.append(String.format(Locale.ROOT, "  Doanh thu TB %.0f d/thang (dan so %d%%), nguoi nap dang choi cuoi ky %d,"
                + " ARPPU %.0f d/thang; thue cho trang tri %.0f xu; da cao cap ve mua %.0f vien\n",
                soThang == 0 ? 0 : vndT / soThang, phanTramDanSo, n.soNguoiNap,
                n.soNguoiNap == 0 || soThang == 0 ? 0 : vndT / soThang / n.soNguoiNap, n.thueCho, n.daTuVe));
        lastNapTomTat = b.toString();
        return kq;
    }

    static String lastNapTomTat = "";

    // ============================ HE SINH THAI (docs/thiet-ke-game.md) ============================
    // Mo-dun ROI cua he sinh thai: ky nang song (cau ca la dai dien - gia tri ky vong lay thang tu
    // com.htbb.pokemon.KyNangSong), phi nguyen lieu bat buoc, NPC mua lai co tran, giao dich giua nguoi
    // choi (thue 7% dot), phi nha tre, luong khoa tu nghien cuu / do vui. RNG RIENG (khong doi chuoi so
    // cua mo phong goc). Thoi gian sinh hoat TRU vao thoi gian danh quai (khong cong them gio choi).
    static boolean HST_MAC_DINH = true;
    boolean hstBat = HST_MAC_DINH;
    HeSinhThaiMoPhong hst;

    HeSinhThaiMoPhong hst() {
        if (hst == null) {
            hst = new HeSinhThaiMoPhong(this, trangThaiRng ^ 0x4853542D53494D31L);
        }
        return hst;
    }

    static final class HeSinhThaiMoPhong {
        /** Ti le ngay choi co lam ky nang song: nhe / vua / cay / bot (bot macro cau ca toi tran). */
        static final double[] THAM_GIA = {0.6, 0.5, 0.35, 1.0};
        /** The luc muon dung moi ngay (toi da THE_LUC tich luy). */
        static final int[] THE_LUC_MUON = {60, 80, 100, 100};
        /** Mo khoa tu cap 10 (khong anh huong tien hoa dau ~1,1 gio). */
        static final int CAP_MO = 10;
        /** 1 the luc ~ 10 giay choi (1 lan cau = 2 the luc ~ 20 giay). */
        static final double GIAY_MOI_THE_LUC = 10.0;
        /** Phi nguyen lieu tron (cau 1‰/the luc, dao 1‰, bep 1,67‰) ~ 1,2‰ gio / the luc. */
        static final double PHI_PHAN_NGHIN_MOI_THE_LUC = 1.2;
        /** Gia dinh XAU NHAT cho lam phat: moi nguoi ban het hang du cho NPC toi tran ngay. */
        static final double PHAN_BAN_NPC = 1.0, PHAN_GIAO_DICH = 0.3, TI_LE_NHA_TRE = 0.3;

        final MoPhongKinhTe m;
        long rng;
        /** {the luc, exp cau ca, ngay cap nhat the luc}. */
        final Map<NguoiChoi, double[]> st = new java.util.IdentityHashMap<NguoiChoi, double[]>();
        double vaoNpc, raPhi, raThue, raNhaTre, hangHoa, lkVao, gioDung;
        long soLuot, soLenMoc;

        HeSinhThaiMoPhong(final MoPhongKinhTe m, final long hat) {
            this.m = m;
            this.rng = hat;
        }

        double ngauNhien() {
            rng += 0x9E3779B97F4A7C15L;
            long z = rng;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            z = z ^ (z >>> 31);
            return (z >>> 11) * 0x1.0p-53;
        }

        /** Mot ngay sinh hoat cua p; tra ve so GIO da dung (tru khoi thoi gian danh quai). */
        double motNgay(final NguoiChoi p, final int ngay, final double gio) {
            double[] s = st.get(p);
            if (s == null) {
                s = new double[]{com.htbb.pokemon.HeSinhThai.THE_LUC_TOI_DA, 0, ngay};
                st.put(p, s);
            }
            if (ngay > s[2]) {
                s[0] = com.htbb.pokemon.KyNangSong.theLucNgayMoi((int) s[0], ngay - (int) s[2]);
                s[2] = ngay;
            }
            if (p.cap < CAP_MO || gio <= 0.2 || ngauNhien() > THAM_GIA[p.nhom]) {
                return 0;
            }
            final double dung = Math.min(s[0], THE_LUC_MUON[p.nhom]);
            if (dung < com.htbb.pokemon.HeSinhThai.TL_CAU_CA) {
                return 0;
            }
            s[0] -= dung;
            soLuot++;
            final double h = ThongSoLauDai.xuMoiGio(p.cap);
            final double lan = dung / com.htbb.pokemon.HeSinhThai.TL_CAU_CA;
            final int capTruoc = com.htbb.pokemon.KyNangSong.capTuExp((long) s[1]);
            final double ev = com.htbb.pokemon.KyNangSong.kyVongCauCa(com.htbb.pokemon.KyNangSong.NUOC_NGOT, capTruoc, false);
            final double hang = lan * ev / 1000.0 * h;
            hangHoa += hang;
            s[1] += lan * com.htbb.pokemon.KyNangSong.expKhiLam((int) ev);
            final int capSau = com.htbb.pokemon.KyNangSong.capTuExp((long) s[1]);
            if (p.nhom != BOT && capSau / 5 > capTruoc / 5) {
                p.ngayCuoiTienBo = ngay; // moc ky nang = tien bo (giu chan)
                soLenMoc++;
            }
            // Phi nguyen lieu bat buoc (moi cau / xeng / cui) - ho hut.
            final double phi = Math.min(p.xu, dung * PHI_PHAN_NGHIN_MOI_THE_LUC / 1000.0 * h);
            if (phi > 0 && m.tieu(p, "hst_nguyen_lieu", ngay, phi)) {
                raPhi += phi;
            }
            // NPC mua lai: san 40%, tran 0,1 gio thu nhap / ngay.
            final double npc = Math.min((p.nhom == BOT ? 1.0 : PHAN_BAN_NPC) * hang
                    * com.htbb.pokemon.HeSinhThai.SAN_NPC_PHAN_TRAM / 100.0,
                    com.htbb.pokemon.HeSinhThai.TRAN_BAN_NPC_NGAY_PHAN_NGHIN / 1000.0 * h);
            m.congXu(p, "hst_ban_npc", ngay, npc);
            vaoNpc += npc;
            if (p.nhom != BOT) {
                // Giao dich giua nguoi choi: xu chuyen tay (bu tru), thue 6% + phi dang 1% bi dot.
                final double thue = Math.min(p.xu, PHAN_GIAO_DICH * hang
                        * (com.htbb.pokemon.HeSinhThai.THUE_CHO_PHAN_TRAM + com.htbb.pokemon.HeSinhThai.PHI_DANG_PHAN_TRAM) / 100.0);
                if (thue > 0 && m.tieu(p, "thue_cho", ngay, thue)) {
                    raThue += thue;
                }
                if (ngauNhien() < TI_LE_NHA_TRE) {
                    final double nt = com.htbb.pokemon.HeSinhThai.PHI_NHA_TRE_NGAY_PHAN_NGHIN / 1000.0 * h;
                    if (m.tieu(p, "hst_nha_tre", ngay, nt)) {
                        raNhaTre += nt;
                    }
                }
                // Luong khoa: nghien cuu Pokedex (2/nhiem vu) + do vui / dua (thuong nho).
                final double lk = p.ngayChoi < 200 ? 1.0 : 0.3;
                p.lk += lk;
                m.tongLkVao[ngay] += lk;
                lkVao += lk;
            }
            final double gioDungNgay = Math.min(gio * 0.3, dung * GIAY_MOI_THE_LUC / 3600.0);
            gioDung += gioDungNgay;
            return gioDungNgay;
        }

        /** Dong tom tat cho mo_phong_tom_tat.txt. */
        static String tomTat(final MoPhongKinhTe m) {
            if (m == null || m.hst == null) {
                return "";
            }
            final HeSinhThaiMoPhong x = m.hst;
            double tf = 0;
            for (final double v : m.tongFaucet) {
                tf += v;
            }
            final double raHst = x.raPhi + x.raThue + x.raNhaTre;
            final StringBuilder b = new StringBuilder("\nHE SINH THAI (docs/thiet-ke-game.md) - dan so day du\n");
            b.append(String.format(Locale.ROOT, "  luot sinh hoat %d, gio sinh hoat TB %.2f/luot, moc ky nang (5 cap) %d\n",
                    x.soLuot, x.soLuot == 0 ? 0 : x.gioDung / x.soLuot, x.soLenMoc));
            b.append(String.format(Locale.ROOT, "  gia tri hang lam ra %.3g xu; NPC mua lai (nguon) %.3g xu = %.2f%% tong nguon xu\n",
                    x.hangHoa, x.vaoNpc, tf == 0 ? 0 : 100 * x.vaoNpc / tf));
            b.append(String.format(Locale.ROOT, "  ho hut: nguyen lieu %.3g, thue giao dich %.3g, nha tre %.3g -> ra/vao cua he sinh thai = %.0f%%\n",
                    x.raPhi, x.raThue, x.raNhaTre, x.vaoNpc == 0 ? 0 : 100 * raHst / x.vaoNpc));
            b.append(String.format(Locale.ROOT, "  %s NPC mua lai <= 3%% tong nguon xu; %s he sinh thai hut >= nguon (khong lam phat)\n",
                    tf > 0 && x.vaoNpc / tf <= 0.03 ? "DAT  " : "KHONG", raHst >= x.vaoNpc ? "DAT  " : "KHONG"));
            b.append(String.format(Locale.ROOT, "  luong khoa tu nghien cuu / mini game: %.0f\n", x.lkVao));
            return b.toString();
        }
    }

    // ============================ TANG TOC (docs/thiet-ke-game.md, docs/van-hanh.md) ============================
    // Mo-dun ROI (them 18/10/2026, quyet dinh "khong tran cap theo mua, len cap cham + vat pham tang toc"): ve
    // nhan EXP x1,5 / x2, bua the luc, uy thac luyen tap (offline 20%). Ban KHOA kiem bang choi (nhiem vu tuan,
    // lich dang nhap, ve mua, Kecleon bang luong khoa) + ban CAO CAP mua bang luong (bi dot), CHUNG tran diem
    // ngay/tuan cua com.htbb.pokemon.TangToc. CHI cong EXP (khong xu / da / vat pham). RNG RIENG.
    /** 0 = khong co tang toc (kich ban so sanh), 1 = thuong (khoa + nguoi nap theo nhom nap), 2 = MOI nguoi mua toi tran. */
    static final int TT_KHONG = 0, TT_THUONG = 1, TT_TOI_DA = 2;
    int cheDoTangToc = TT_THUONG;
    TangTocMoPhong tangToc;

    TangTocMoPhong tangToc() {
        if (tangToc == null) {
            tangToc = new TangTocMoPhong(this, trangThaiRng ^ 0x54616E67546F6331L);
        }
        return tangToc;
    }

    static final class TangTocMoPhong {
        static final int VE_15 = com.htbb.pokemon.TangToc.VE_15, VE_2 = com.htbb.pokemon.TangToc.VE_2,
                BUA = com.htbb.pokemon.TangToc.BUA, UY_THAC = com.htbb.pokemon.TangToc.UY_THAC;
        static final int[] DIEM = com.htbb.pokemon.TangToc.DIEM;
        static final int[] GIA = com.htbb.pokemon.TangToc.GIA_LUONG;
        static final int UY_THAC_PHAN_TRAM = com.htbb.pokemon.TangToc.UY_THAC_PHAN_TRAM;
        /** Kecleon (luong khoa): ve x1,5 khoa 15, uy thac khoa 10, moi mon 2/tuan (sinh_vat_pham_lau_dai.py). */
        static final int LK_VE = 15, LK_UY_THAC = 10, LK_TUAN = 2;

        static final class Kho {
            int tuan = -1, diemTuan, muaLkTuan;
            final int[] khoa = new int[4];
            double gioUyThacHomNay;
            double thuongTong;
        }

        final MoPhongKinhTe m;
        long rng;
        /** Tong luong mua tang toc (bi dot) va so diem da dung theo loai nguoi (0 khong nap, 1 nap). */
        double luongTangToc;
        final double[] thuongNhom = new double[SO_NHOM];
        final double[] diemTuanNhom = new double[SO_NHOM];
        final int[] demTuanNhom = new int[SO_NHOM];
        double maxDiemTuan;
        final java.util.HashMap<NguoiChoi, Kho> kho = new java.util.HashMap<NguoiChoi, Kho>();

        TangTocMoPhong(final MoPhongKinhTe m, final long hat) {
            this.m = m;
            this.rng = hat;
        }

        double ngauNhien() {
            rng += 0x9E3779B97F4A7C15L;
            long z = rng;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            z = z ^ (z >>> 31);
            return (z >>> 11) * 0x1.0p-53;
        }

        Kho kho(final NguoiChoi p) {
            Kho k = kho.get(p);
            if (k == null) {
                k = new Kho();
                kho.put(p, k);
            }
            return k;
        }

        /** Nguoi nap mua cao cap (ca voi luon lap day tran; nap nhe 40% ngay, toi da 30 diem); che do TOI_DA: moi nguoi. */
        int diemMuaToiDa(final NguoiChoi p) {
            if (m.cheDoTangToc == TT_TOI_DA) {
                return Integer.MAX_VALUE;
            }
            if (p.nhom == BOT || m.napCheDo == NAP_TAT || p.napNguoi == null) {
                return 0;
            }
            if (p.napNguoi.nhom == NapMoPhong.CA_VOI) {
                return Integer.MAX_VALUE;
            }
            if (p.napNguoi.nhom == NapMoPhong.NHE_NAP && ngauNhien() < 0.4) {
                return 30;
            }
            return 0;
        }

        /**
         * Dau ngay choi: nhan ban khoa (nguon choi), quyet dinh dung gi hom nay theo gia tri cho nguoi nay.
         * @return {phut bua, phut ve x2, phut ve x1,5}
         */
        double[] truocDanhQuai(final NguoiChoi p, final int ngay, final double gioCon) {
            final Kho k = kho(p);
            k.gioUyThacHomNay = 0;
            final int tuan = ngay / 7;
            if (k.tuan != tuan) {
                if (k.tuan >= 0) {
                    diemTuanNhom[p.nhom] += k.diemTuan;
                    demTuanNhom[p.nhom]++;
                    maxDiemTuan = Math.max(maxDiemTuan, k.diemTuan);
                    // Nhiem vu tuan (2/2): 2 ve x1,5 khoa + 1 phieu uy thac khoa.
                    if (p.nhom != BOT) {
                        k.khoa[VE_15] += 2;
                        k.khoa[UY_THAC] += 1;
                    }
                }
                k.tuan = tuan;
                k.diemTuan = 0;
                k.muaLkTuan = 0;
            }
            if (p.nhom != BOT) {
                // Lich dang nhap 7 ngay: ngay 3 -> ve x1,5 khoa, ngay 7 -> bua the luc khoa.
                final int buoc = (p.lanDangNhapChuoi - 1) % 7;
                if (buoc == 2) {
                    k.khoa[VE_15]++;
                } else if (buoc == 6) {
                    k.khoa[BUA]++;
                }
                // Ve mua (56 ngay) track mien phi: bac 7/14/22 ve x1,5 khoa, bac 28 bua khoa (nguoi choi deu toi
                // bac 30 trong mua); track cao cap bac 26: 2 ve x2 khoa.
                final int nm = ngay % com.htbb.pokemon.NapTienLogic.VE_MUA_SO_NGAY;
                if (nm == 10 || nm == 20 || nm == 30) {
                    k.khoa[VE_15]++;
                } else if (nm == 40) {
                    k.khoa[BUA]++;
                    if (p.napNguoi != null && p.napNguoi.caoCap) {
                        k.khoa[VE_2] += 2;
                    }
                }
                // Kecleon: doi luong khoa du (> 150) lay ban khoa, 2 + 2 / tuan.
                while (k.muaLkTuan < 2 * LK_TUAN && p.lk > 150 + LK_VE) {
                    final boolean ve = k.muaLkTuan % 2 == 0;
                    final int gia = ve ? LK_VE : LK_UY_THAC;
                    p.lk -= gia;
                    m.tongLkRa[ngay] += gia;
                    k.khoa[ve ? VE_15 : UY_THAC]++;
                    k.muaLkTuan++;
                }
            }
            final double[] ra = new double[3];
            if (p.cap >= ThongSoLauDai.CAP_TOI_DA) {
                return ra;
            }
            int con = Math.min(com.htbb.pokemon.TangToc.DIEM_NGAY_TOI_DA, com.htbb.pokemon.TangToc.DIEM_TUAN_TOI_DA - k.diemTuan);
            int mua = diemMuaToiDa(p);
            final double phut = gioCon * 60;
            final double phut100 = m.phutNgay + p.tichNangLuong;
            int phutVe = 0;
            // 1. Ve EXP (chi khi con du phut danh quai de chay het ve, phut 100% truoc). Ban khoa truoc, roi cao cap.
            for (final int loai : new int[]{VE_2, VE_15}) {
                while (phutVe + com.htbb.pokemon.TangToc.VE_PHUT <= com.htbb.pokemon.TangToc.VE_PHUT_NGAY_TOI_DA
                        && phut >= phutVe + com.htbb.pokemon.TangToc.VE_PHUT && con >= DIEM[loai]) {
                    if (!lay(p, k, loai, ngay, mua)) {
                        break;
                    }
                    mua -= daMua ? DIEM[loai] : 0;
                    con -= DIEM[loai];
                    k.diemTuan += DIEM[loai];
                    ra[loai == VE_2 ? 1 : 2] += com.htbb.pokemon.TangToc.VE_PHUT;
                    phutVe += com.htbb.pokemon.TangToc.VE_PHUT;
                }
            }
            // 2. Bua the luc: chi khi danh quai qua phut 100% it nhat 40 phut (gia tri >= 30 diem).
            if (con >= DIEM[BUA] && phut - phut100 >= 40 && lay(p, k, BUA, ngay, mua)) {
                mua -= daMua ? DIEM[BUA] : 0;
                con -= DIEM[BUA];
                k.diemTuan += DIEM[BUA];
                ra[0] = com.htbb.pokemon.TangToc.BUA_PHUT;
            }
            // 3. Uy thac offline (luon dung duoc het gia tri): toi da 3 gio (+1 the thang).
            final boolean the = p.napNguoi != null && p.napNguoi.hetHanThe > ngay;
            final int gioMax = com.htbb.pokemon.TangToc.UY_THAC_GIO_NGAY + (the ? com.htbb.pokemon.TangToc.UY_THAC_GIO_THE_THANG : 0);
            for (int g = 0; g < gioMax && con >= DIEM[UY_THAC]; g++) {
                if (!lay(p, k, UY_THAC, ngay, mua)) {
                    break;
                }
                mua -= daMua ? DIEM[UY_THAC] : 0;
                con -= DIEM[UY_THAC];
                k.diemTuan += DIEM[UY_THAC];
                k.gioUyThacHomNay++;
            }
            return ra;
        }

        boolean daMua;

        /** Lay 1 mon: ban khoa neu co, khong thi mua cao cap (neu con trong muc mua). */
        boolean lay(final NguoiChoi p, final Kho k, final int loai, final int ngay, final int mua) {
            daMua = false;
            if (k.khoa[loai] > 0) {
                k.khoa[loai]--;
                return true;
            }
            if (mua < DIEM[loai]) {
                return false;
            }
            if (m.cheDoTangToc != TT_TOI_DA || m.napCheDo != NAP_TAT) {
                if (m.napCheDo == NAP_TAT || p.napNguoi == null) {
                    return false;
                }
                final NapMoPhong n = m.nap();
                final NapMoPhong.Nguoi nn = p.napNguoi;
                if (nn.lg < GIA[loai]) {
                    // goi nho nhat du (200 luong = 20.000 d), trong tran nap tuan/thang
                    if (!n.tra(nn, ngay, 20000)) {
                        return false;
                    }
                    n.vaoLuong(nn, ngay, 200);
                }
                n.raLuong(nn, ngay, GIA[loai]);
                luongTangToc += GIA[loai];
            }
            daMua = true;
            return true;
        }

        double gioUyThac(final NguoiChoi p) {
            return kho(p).gioUyThacHomNay;
        }

        void ghiThuong(final NguoiChoi p, final double phutHieuDung) {
            thuongNhom[p.nhom] += phutHieuDung / 60.0;
            kho(p).thuongTong += phutHieuDung / 60.0;
        }
    }

    // ============================ GUC / HOI SINH (com.htbb.pokemon.HoiSinh, docs/thiet-ke-game.md) ============================
    // Them 18/10/2026, KHONG sua cac mo-dun khac. GIA TRI KY VONG (khong rut RNG - khong doi chuoi so cua mo phong goc):
    // moi gio danh quai that guc TI_LE_GUC[nhom] lan; nguoi choi chon theo CHON_*: Thuoc hoi sinh (mua o Chansey, gia
    // HoiSinh.giaThuoc theo cap), tra xu tai cho (HoiSinh.phiXu, he so lap TB HE_SO_TB), luong khoa (HoiSinh.phiLuongKhoa), ve Trung
    // tam (mat EXP HoiSinh.expMat + Met moi: -metExp% EXP trong metPhut phut). Gia thuoc = HoiSinh.giaThuoc (theo cap). Tan thu: khong ton gi. Bot (tu dong) chi
    // dung thuoc (<= tu_dong.hoi_sinh_moi_gio / gio). Ho hut xu ghi o nguon "hoi_sinh".
    static boolean HOI_SINH_MAC_DINH = true;
    boolean hoiSinhBat = HOI_SINH_MAC_DINH;
    HoiSinhMoPhong hoiSinh;

    HoiSinhMoPhong hoiSinh() {
        if (hoiSinh == null) {
            hoiSinh = new HoiSinhMoPhong(this);
        }
        return hoiSinh;
    }

    static final class HoiSinhMoPhong {
        /** Lan guc / gio danh quai: nhe / vua / cay / bot. */
        static final double[] TI_LE_GUC = {0.25, 0.22, 0.2, 0.15};
        /** Lua chon (nguoi that, khong tan thu): thuoc / xu / luong khoa / ve Trung tam. */
        static final double CHON_THUOC = 0.45, CHON_XU = 0.15, CHON_LK = 0.05, CHON_TRUNG_TAM = 0.35;
        static final double HE_SO_TB = 1.2;
        final MoPhongKinhTe m;
        final com.htbb.pokemon.HoiSinh.CauHinh c = new com.htbb.pokemon.HoiSinh.CauHinh();
        double soGuc, xuDot, lkDot, expMat, soVe;

        HoiSinhMoPhong(final MoPhongKinhTe m) {
            this.m = m;
        }

        void sauDanhQuai(final NguoiChoi p, final int ngay, final double gio, final double gioHd) {
            if (gio <= 0) {
                return;
            }
            final double guc = TI_LE_GUC[p.nhom] * gio;
            soGuc += guc;
            if (com.htbb.pokemon.HoiSinh.tanThu(c, p.cap)) {
                return; // tan thu: mien phi
            }
            if (p.nhom == BOT) {
                final double x = Math.min(p.xu, guc * com.htbb.pokemon.HoiSinh.giaThuoc(c, p.cap));
                m.tieu(p, "hoi_sinh", ngay, x);
                xuDot += x;
                return;
            }
            double xu = guc * CHON_THUOC * com.htbb.pokemon.HoiSinh.giaThuoc(c, p.cap)
                    + guc * CHON_XU * com.htbb.pokemon.HoiSinh.phiXu(c, p.cap, 1) * HE_SO_TB;
            xu = Math.min(p.xu, xu);
            m.tieu(p, "hoi_sinh", ngay, xu);
            xuDot += xu;
            final double lk = Math.min(p.lk, guc * CHON_LK * com.htbb.pokemon.HoiSinh.phiLuongKhoa(c, 1) * HE_SO_TB);
            p.lk -= lk;
            m.tongLkRa[ngay] += lk;
            lkDot += lk;
            final double ve = guc * CHON_TRUNG_TAM;
            soVe += ve;
            final long len = ThongSoLauDai.EXP_LEN_CAP[Math.min(ThongSoLauDai.EXP_LEN_CAP.length - 1, p.cap)];
            final long moiGio = ThongSoLauDai.expMoiGio(p.cap);
            // p.exp = tien do trong cap (mo phong): expMat chan boi tien do -> khong bao gio tut cap
            final long motLan = com.htbb.pokemon.HoiSinh.expMat(c, p.cap, (long) p.exp, 0L, len, moiGio);
            final double met = moiGio * (gioHd / gio) * c.metPhut / 60.0 * c.metExp / 100.0;
            final double tru = Math.min(p.exp, ve * (motLan + met));
            p.exp -= tru;
            expMat += tru;
        }

        String tomTat() {
            return String.format(Locale.ROOT, "  guc %.0f lan, ve Trung tam %.0f, xu dot %.0f, luong khoa dot %.0f, EXP mat %.3g%n",
                    soGuc, soVe, xuDot, lkDot, expMat);
        }
    }
}
