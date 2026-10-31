package com.htbb.pokemon;

/**
 * Nang luong huan luyen (docs/thiet-ke-game.md). Lop THUAN.
 *
 *   180 phut dau (+ tich luy tu hom truoc, toi da them 180) -> 100%
 *   180 phut tiep -> 50%, 240 phut tiep -> 20%, sau do 0% (EXP, xu, da tu quai).
 *
 * "Thoi gian danh quai" = tong cac khoang giua hai lan ha quai lien tiep, moi khoang
 * toi da {@link #KHOANG_TOI_DA_MS} (dung yen / di cho / AFK khong bi tinh).
 */
public final class NangLuong {

    /** Khoang lon nhat giua hai lan ha quai van coi la "dang danh lien tuc". */
    public static final long KHOANG_TOI_DA_MS = 60000L;

    private NangLuong() {
    }

    /** yyyyMMdd cua thoi diem (gio may). Doi ngay luc 00:00 nhu CharCapNhat. */
    public static int ngay(final long ms) {
        final java.util.Calendar c = com.htbb.pokemon.DongHo.lich();
        c.setTimeInMillis(ms);
        return c.get(java.util.Calendar.YEAR) * 10000 + (c.get(java.util.Calendar.MONTH) + 1) * 100
                + c.get(java.util.Calendar.DAY_OF_MONTH);
    }

    /** So ngay giua hai yyyyMMdd (b - a), >= 0. */
    public static int cachNgay(final int a, final int b) {
        if (a <= 0 || b <= 0) {
            return 0;
        }
        final java.util.Calendar ca = com.htbb.pokemon.DongHo.lich();
        ca.clear();
        ca.set(a / 10000, (a / 100) % 100 - 1, a % 100, 12, 0, 0);
        final java.util.Calendar cb = com.htbb.pokemon.DongHo.lich();
        cb.clear();
        cb.set(b / 10000, (b / 100) % 100 - 1, b % 100, 12, 0, 0);
        return (int) Math.max(0L, Math.round((cb.getTimeInMillis() - ca.getTimeInMillis()) / 86400000.0));
    }

    /**
     * Phut 100% cua ngay moi: 180 + phan 100% chua dung (toi da 180). Nghi tu 2 ngay tro
     * len thi tich du 180 (tong 6 gio) - khong ep dang nhap moi ngay.
     */
    public static int phut100NgayMoi(final int phut100Cu, final long msDungCu, final int soNgayCach,
            final int phutNgay, final int phutTichToiDa) {
        final int tichToiDa = Math.max(0, phutTichToiDa - phutNgay);
        if (soNgayCach <= 0) {
            return phut100Cu;
        }
        final int du;
        if (soNgayCach >= 2) {
            du = tichToiDa;
        } else {
            du = (int) Math.max(0L, phut100Cu - msDungCu / 60000L);
        }
        return phutNgay + Math.min(tichToiDa, du);
    }

    /** He so (%) khi da dung {@code msDung} ms trong ngay co {@code phut100} phut 100%. */
    public static int heSo(final long msDung, final int phut100) {
        final long phut = msDung / 60000L;
        if (phut < phut100) {
            return 100;
        }
        return ThongSoLauDai.heSoMet((int) Math.min(Integer.MAX_VALUE, phut - phut100));
    }

    // ================================================================== thuong thanh vien (31/10/2026)

    /**
     * Chu game 31/10/2026: phut nang luong 100% CONG THEM moi ngay khi dang co Thanh vien HTBB / The thang / Ve mua cao
     * cap (cong don, mac dinh 30 + 30 + 30, toi da 90). Tinh MOT lan luc khoi tao nang luong cua ngay (lan ha quai /
     * xem So tay dau tien trong ngay) theo goi dang con han luc do; KHONG tich sang hom sau, CHI EXP (nhu Bua the luc:
     * xu / da / do roi van theo phut goc - nap tien khong ban xu). Khoa danh muc nhom kinh_te, ap dung ngay.
     */
    public static final String K_THUONG_THANH_VIEN = "exp.nang_luong_thanh_vien_phut";
    public static final String K_THUONG_THE_THANG = "exp.nang_luong_the_thang_phut";
    public static final String K_THUONG_VE_MUA = "exp.nang_luong_ve_mua_phut";
    public static final String[] K_THUONG = {K_THUONG_THANH_VIEN, K_THUONG_THE_THANG, K_THUONG_VE_MUA};
    public static final int THUONG_MAC_DINH = 30;
    /** Tran moi khoa (phut) - khop gioi_han cua danh muc. */
    public static final int THUONG_TOI_DA = 180;
    public static final int TV = 0, THE = 1, VE = 2;
    /** Ten nguon cho So tay (cung thu tu TV / THE / VE). */
    public static final String[] TEN_THUONG = {"Thành viên HTBB", "Thẻ tháng", "Vé mùa cao cấp"};

    /** Phut moi nguon tu cau hinh (kep 0..180). {@code co[i]} = goi i dang con han. */
    public static int[] thuongTheoCo(final boolean[] co) {
        final int[] r = new int[3];
        for (int i = 0; i < 3; i++) {
            if (co != null && i < co.length && co[i]) {
                long v = THUONG_MAC_DINH;
                try {
                    v = com.htbb.cauhinh.CauHinhTrungTam.so(K_THUONG[i], THUONG_MAC_DINH);
                } catch (final Throwable t) {
                    // danh muc chua nap: mac dinh
                }
                r[i] = (int) Math.max(0L, Math.min(THUONG_TOI_DA, v));
            }
        }
        return r;
    }

    /** Tong phut thuong thanh vien da cong vao nlPhut100 cua ngay nlNgay. */
    public static int thuongTong(final HoSoLauDai h) {
        return Math.max(0, h.nlThuongTv) + Math.max(0, h.nlThuongThe) + Math.max(0, h.nlThuongVe);
    }

    /** Phut 100% "goc" cua ngay (khong tinh Bua the luc, khong tinh thuong thanh vien) - dung cho tich luy va xu. */
    public static int phut100Goc(final HoSoLauDai h) {
        return TangToc.phut100KhongBua(h) - thuongTong(h);
    }

    /**
     * Dong "Phut 100% hom nay" cho So tay, kem chi tiet: goc (+ tich luy), Bua the luc, tung goi thanh vien. Neu chua
     * co goi nao: goi y so phut moi goi (theo cau hinh hien tai).
     */
    public static String moTaPhut100(final HoSoLauDai h) {
        synchronized (h) {
            final StringBuilder b = new StringBuilder();
            b.append("Phút 100% hôm nay: ").append(h.nlPhut100).append(" phút (cơ bản + tích luỹ ").append(phut100Goc(h));
            final int bua = TangToc.phut100KhongBua(h) == h.nlPhut100 ? 0 : TangToc.BUA_PHUT;
            if (bua > 0) {
                b.append(", Bùa thể lực +").append(bua);
            }
            final int[] t = {h.nlThuongTv, h.nlThuongThe, h.nlThuongVe};
            for (int i = 0; i < 3; i++) {
                if (t[i] > 0) {
                    b.append(", ").append(TEN_THUONG[i]).append(" +").append(t[i]);
                }
            }
            b.append(')');
            if (thuongTong(h) > 0) {
                b.append("\nPhút thưởng gói chỉ tính EXP, không cộng dồn sang hôm sau.");
            } else {
                final int[] goiY = thuongTheoCo(new boolean[]{true, true, true});
                if (goiY[TV] + goiY[THE] + goiY[VE] > 0) {
                    b.append("\nMỗi ngày cộng thêm phút 100% (chỉ EXP): ").append(TEN_THUONG[TV]).append(" +").append(goiY[TV])
                            .append(", ").append(TEN_THUONG[THE]).append(" +").append(goiY[THE]).append(", ")
                            .append(TEN_THUONG[VE]).append(" +").append(goiY[VE]).append(" (cộng dồn, tính lúc bắt đầu ngày).");
                }
            }
            return b.toString();
        }
    }

    /** Ngay {@code bayGio} da khoi tao nang luong chua (false = lan dau trong ngay se tinh thuong thanh vien). */
    public static boolean daKhoiTao(final HoSoLauDai h, final long bayGio) {
        synchronized (h) {
            return h.nlNgay == ngay(bayGio);
        }
    }

    /**
     * Doi ngay neu can (khong ghi nhan lan ha quai). {@code thuong} = phut cong them hom nay {TV, THE, VE} (null = 0).
     * @return true neu vua khoi tao ngay moi.
     */
    public static boolean doiNgay(final HoSoLauDai h, final long bayGio, final int phutNgay, final int phutTichToiDa,
            final int[] thuong) {
        synchronized (h) {
            final int hn = ngay(bayGio);
            if (h.nlNgay == hn) {
                return false;
            }
            final int cach = h.nlNgay <= 0 ? 2 : cachNgay(h.nlNgay, hn);
            // Bua the luc (TangToc) va thuong thanh vien chi cho hom do: tru ra truoc khi tinh phan tich sang hom sau.
            final int goc = h.nlNgay <= 0 ? phutNgay : phut100NgayMoi(phut100Goc(h), h.nlMsDung, cach, phutNgay, phutTichToiDa);
            h.nlThuongTv = thuong == null || thuong.length < 1 ? 0 : Math.max(0, thuong[TV]);
            h.nlThuongThe = thuong == null || thuong.length < 2 ? 0 : Math.max(0, thuong[THE]);
            h.nlThuongVe = thuong == null || thuong.length < 3 ? 0 : Math.max(0, thuong[VE]);
            h.nlPhut100 = goc + thuongTong(h);
            h.nlNgay = hn;
            h.nlMsDung = 0L;
            h.nlLanCuoi = 0L;
            TangToc.sauDoiNgay(h, hn);
            h.ban = true;
            return true;
        }
    }

    /**
     * Ghi nhan mot lan ha quai luc {@code bayGio} va tra ve he so (%) ap cho lan do.
     * Doi ngay tu dong (theo gio may), khong co thuong thanh vien.
     */
    public static int ghiNhan(final HoSoLauDai h, final long bayGio, final int phutNgay, final int phutTichToiDa) {
        return ghiNhan(h, bayGio, phutNgay, phutTichToiDa, null);
    }

    /** Nhu tren; {@code thuong} = phut thuong thanh vien {TV, THE, VE} dung NEU lan nay khoi tao ngay moi. */
    public static int ghiNhan(final HoSoLauDai h, final long bayGio, final int phutNgay, final int phutTichToiDa,
            final int[] thuong) {
        synchronized (h) {
            doiNgay(h, bayGio, phutNgay, phutTichToiDa, thuong);
            if (h.nlLanCuoi > 0L && bayGio > h.nlLanCuoi) {
                h.nlMsDung += Math.min(KHOANG_TOI_DA_MS, bayGio - h.nlLanCuoi);
            }
            h.nlLanCuoi = bayGio;
            h.ban = true;
            return heSo(h.nlMsDung, h.nlPhut100);
        }
    }

    /**
     * He so cho XU / da / vat pham roi (khong ghi nhan): KHONG tinh Bua the luc (TangToc) va thuong thanh vien - vat
     * pham tang toc / goi nap chi cho EXP, khong lam tang tien / do roi.
     */
    public static int heSoXuHienTai(final HoSoLauDai h, final long bayGio) {
        synchronized (h) {
            if (h.nlNgay != ngay(bayGio)) {
                return 100;
            }
            return heSo(h.nlMsDung, phut100Goc(h));
        }
    }

    /** He so hien tai (khong ghi nhan) - de hien cho nguoi choi. */
    public static int heSoHienTai(final HoSoLauDai h, final long bayGio) {
        synchronized (h) {
            if (h.nlNgay != ngay(bayGio)) {
                return 100;
            }
            return heSo(h.nlMsDung, h.nlPhut100);
        }
    }
}
