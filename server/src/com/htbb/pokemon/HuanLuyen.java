package com.htbb.pokemon;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DIEM HUAN LUYEN (de xuat 1, spec thiet_ke_can_bang 4.2) thay DIEM TIEM NANG TU DO (5 diem / cap cong thang vao
 * Suc manh / Kheo leo / Tinh than / Suc khoe / May man - cung loai lech HP / tan cong toi x4,3).
 *
 * <ul>
 *   <li>Chi so goc (5 chi so cu) tu tang theo cap: +1 moi chi so nhu cu cong them vector tang truong cua dong
 *       (data_tang_truong_dong, tong 5 = so diem tu do cu) - khong ai con dong duoc x4 vao mot chi so.</li>
 *   <li>Diem huan luyen: {@code diem_moi_cap} (2) moi cap + 1 / huy hieu Gym (tran 16). Phan bo vao vai DONG chi so
 *       (data_huan_luyen: HP, Tan cong, Phong thu, PP), moi dong tran {@code tran} diem, giam dan: {@code nguong}
 *       diem dau +pn1 phan nghin / diem, sau do +pn2. Mac dinh 40 x 3 + 40 x 2 = +200 phan nghin (+20%) o 80 diem.</li>
 *   <li>Tay diem: lan dau mien phi, sau do 1 gio thu nhap (xu - ho hut) va cho {@code hoi_tay_gio} gio.</li>
 * </ul>
 * Lop THUAN (khong Char / CSDL / cau hinh): real.HocChieuGame noi vao game, test/KiemHocChieu kiem.
 */
public final class HuanLuyen {

    /** Ma dong chi so (data_huan_luyen.ma) ma game biet ap dung. */
    public static final String HP = "hp", TAN_CONG = "tan_cong", PHONG_THU = "phong_thu", PP = "pp";

    /** Chi so cu (Char.strength / agitity / spirit / health / luck). */
    public static final int SUC_MANH = 0, KHEO_LEO = 1, TINH_THAN = 2, SUC_KHOE = 3, MAY_MAN = 4;

    /** Mot dong data_huan_luyen. */
    public static final class Dong {
        public final int id, tran, nguong, pn1, pn2, thuTu;
        public final String ma, ten, moTa;

        public Dong(final int id, final String ma, final String ten, final String moTa, final int tran,
                final int nguong, final int pn1, final int pn2, final int thuTu) {
            this.id = id;
            this.ma = ma;
            this.ten = ten;
            this.moTa = moTa == null ? "" : moTa;
            this.tran = Math.max(0, tran);
            this.nguong = Math.max(0, Math.min(this.tran, nguong));
            this.pn1 = Math.max(0, pn1);
            this.pn2 = Math.max(0, pn2);
            this.thuTu = thuTu;
        }
    }

    /** Cac dong, sap theo thu_tu; vi tri trong mang = chi so trong mang phan bo. */
    private static volatile Dong[] DONG = new Dong[0];
    /** dong Pokemon (class) -> 5 diem cong them moi cap (SM, KL, TT, SK, MM). */
    private static volatile Map<Integer, int[]> TANG = Collections.emptyMap();

    private HuanLuyen() {
    }

    // ================================================================== du lieu

    public static synchronized void datDong(final List<Dong> ds) {
        final Dong[] a = ds == null ? new Dong[0] : ds.toArray(new Dong[0]);
        Arrays.sort(a, (x, y) -> x.thuTu != y.thuTu ? Integer.compare(x.thuTu, y.thuTu) : Integer.compare(x.id, y.id));
        DONG = a;
    }

    public static synchronized void datTangTruong(final Map<Integer, int[]> m) {
        TANG = m == null ? Collections.<Integer, int[]>emptyMap() : new HashMap<Integer, int[]>(m);
    }

    public static boolean coDuLieu() {
        return DONG.length > 0 && !TANG.isEmpty();
    }

    public static int soDong() {
        return DONG.length;
    }

    public static Dong dong(final int i) {
        final Dong[] a = DONG;
        return i >= 0 && i < a.length ? a[i] : null;
    }

    /** Vi tri dong co ma, -1 neu khong co. */
    public static int viTri(final String ma) {
        final Dong[] a = DONG;
        for (int i = 0; i < a.length; i++) {
            if (a[i].ma.equals(ma)) {
                return i;
            }
        }
        return -1;
    }

    /** Vi tri theo id dong (goi tin client gui id), -1 neu khong co. */
    public static int viTriId(final int id) {
        final Dong[] a = DONG;
        for (int i = 0; i < a.length; i++) {
            if (a[i].id == id) {
                return i;
            }
        }
        return -1;
    }

    public static int[] tangTruong(final int dongPokemon) {
        final int[] t = TANG.get(dongPokemon);
        return t == null ? null : t.clone();
    }

    // ================================================================== cong thuc

    /** Phan nghin cong them cua dong khi co {@code diem} diem (giam dan, chan tran). */
    public static int phanNghin(final Dong d, final int diem) {
        if (d == null || diem <= 0) {
            return 0;
        }
        final int n = Math.min(diem, d.tran);
        return Math.min(n, d.nguong) * d.pn1 + Math.max(0, n - d.nguong) * d.pn2;
    }

    /** Phan nghin toi da cua dong (o tran). */
    public static int phanNghinToiDa(final Dong d) {
        return d == null ? 0 : phanNghin(d, d.tran);
    }

    /** Tong diem huan luyen o cap: diemMoiCap x (cap - 1) + min(huy hieu, tranThuong). */
    public static int tongDiem(final int cap, final int huyHieu, final int diemMoiCap, final int tranThuong) {
        return Math.max(0, diemMoiCap) * Math.max(0, cap - 1) + Math.max(0, Math.min(huyHieu, tranThuong));
    }

    public static int daDung(final int[] phanBo) {
        int t = 0;
        if (phanBo != null) {
            for (final int x : phanBo) {
                t += Math.max(0, x);
            }
        }
        return t;
    }

    /** Mang phan bo dung do dai so dong (thieu = 0, am = 0, vuot tran = tran). */
    public static int[] chuanPhanBo(final int[] pb) {
        final int[] r = new int[DONG.length];
        for (int i = 0; i < r.length; i++) {
            final int v = pb != null && i < pb.length ? pb[i] : 0;
            r[i] = Math.max(0, Math.min(DONG[i].tran, v));
        }
        return r;
    }

    /**
     * Phan bo vuot tong (vd tong giam vi admin ha cap): bo bot tu dong cuoi len dau cho toi khi vua. Tra mang moi.
     */
    public static int[] catTheoTong(final int[] pb, final int tong) {
        final int[] r = chuanPhanBo(pb);
        int du = daDung(r) - Math.max(0, tong);
        for (int i = r.length - 1; i >= 0 && du > 0; i--) {
            final int bot = Math.min(du, r[i]);
            r[i] -= bot;
            du -= bot;
        }
        return r;
    }

    /**
     * Kiem mot lan phan bo them: null = hop le, nguoc lai la ly do (tieng Viet, hien cho nguoi choi).
     *
     * @param hienTai phan bo dang co
     * @param them    so diem them moi dong (cung thu tu)
     * @param tong    tong diem dang co
     */
    public static String kiemPhanBo(final int[] hienTai, final int[] them, final int tong) {
        if (them == null || them.length != DONG.length) {
            return "Dữ liệu phân bổ không hợp lệ.";
        }
        int t = 0;
        for (int i = 0; i < them.length; i++) {
            if (them[i] < 0) {
                return "Không thể bớt điểm khi phân bổ (dùng Tẩy điểm).";
            }
            final int cu = hienTai != null && i < hienTai.length ? hienTai[i] : 0;
            if (cu + them[i] > DONG[i].tran) {
                return DONG[i].ten + " tối đa " + DONG[i].tran + " điểm.";
            }
            t += them[i];
        }
        if (t <= 0) {
            return "Chưa chọn điểm nào.";
        }
        if (daDung(hienTai) + t > tong) {
            return "Không đủ Điểm huấn luyện (còn " + Math.max(0, tong - daDung(hienTai)) + ").";
        }
        return null;
    }

    /** Gia tay diem (xu): lan dau (lanTay = 0) mien phi, sau do xuMoiGio x phanNghinGio / 1000. */
    public static long giaTay(final int lanTay, final long xuMoiGio, final long phanNghinGio) {
        if (lanTay <= 0) {
            return 0L;
        }
        return Math.max(0L, xuMoiGio) * Math.max(0L, phanNghinGio) / 1000L;
    }

    /** Mili giay con phai cho truoc lan tay ke tiep (lan dau khong cho). */
    public static long choTay(final int lanTay, final long tayLuc, final long bayGio, final long hoiGio) {
        if (lanTay <= 0 || tayLuc <= 0L) {
            return 0L;
        }
        return Math.max(0L, tayLuc + Math.max(0L, hoiGio) * 3600000L - bayGio);
    }

    // ================================================================== chi so goc theo cap

    /** Chi so goc 5 chi so cu o cap: goc + (cap - 1) x (1 + tang). null neu dong chua co du lieu. */
    public static int[] chiSoGoc(final int dongPokemon, final int cap, final int[] goc) {
        final int[] t = TANG.get(dongPokemon);
        if (t == null || goc == null || goc.length < 5) {
            return null;
        }
        final int l = Math.max(0, cap - 1);
        final int[] r = new int[5];
        for (int i = 0; i < 5; i++) {
            r[i] = goc[i] + l * (1 + (i < t.length ? Math.max(0, t[i]) : 0));
        }
        return r;
    }

    // ================================================================== chuyen doi diem tiem nang cu

    /**
     * Chi so cu (theo dong Pokemon) -> ma dong huan luyen. Chi so tan cong cua dong: Charmander / Bulbasaur / Squirtle
     * = Suc manh, Eevee = Tinh than, Pichu = Kheo leo; Kheo leo con lai = Phong thu; Suc khoe = HP; Tinh than con lai
     * = PP. Chi so khong dong vai tro gi (Suc manh cua Eevee / Pichu, May man) = null (tra ve thanh diem chua dung).
     */
    public static String maTheoChiSo(final int dongPokemon, final int chiSo) {
        switch (chiSo) {
            case SUC_MANH:
                return dongPokemon == 2 || dongPokemon == 4 ? null : TAN_CONG;
            case KHEO_LEO:
                return dongPokemon == 4 ? TAN_CONG : PHONG_THU;
            case TINH_THAN:
                return dongPokemon == 2 ? TAN_CONG : PP;
            case SUC_KHOE:
                return HP;
            default:
                return null;
        }
    }

    /**
     * Quy doi diem tiem nang da cong sang phan bo Diem huan luyen (spec 7: cung ti le phan bo, co ve tong moi, tran moi
     * dong). Phan con lai (diem tu do chua cong, May man, chi so khong vai tro, phan vuot tran, phan le) de CHUA DUNG -
     * nguoi choi tu phan bo (va co 1 lan tay mien phi). Nguoi choi KHONG mat diem nao: tong diem moi luon la tongDiem.
     *
     * @param chiSoCu   5 chi so dang luu (strength, agitity, spirit, health, luck)
     * @param goc       5 chi so goc cua dong (Database.basicPoint)
     * @param diemConCu diem tiem nang chua cong (Char.basepoint)
     * @param tong      tong Diem huan luyen moi
     */
    public static int[] quyDoi(final int dongPokemon, final int cap, final int[] chiSoCu, final int[] goc,
            final int diemConCu, final int tong) {
        final int[] pb = new int[DONG.length];
        if (chiSoCu == null || goc == null || tong <= 0) {
            return pb;
        }
        final long[] w = new long[DONG.length];
        long tongCu = Math.max(0, diemConCu);
        for (int s = 0; s < 5 && s < chiSoCu.length && s < goc.length; s++) {
            final int f = Math.max(0, chiSoCu[s] - goc[s] - Math.max(0, cap - 1));
            tongCu += f;
            final String ma = maTheoChiSo(dongPokemon, s);
            final int vt = ma == null ? -1 : viTri(ma);
            if (vt >= 0) {
                w[vt] += f;
            }
        }
        if (tongCu <= 0) {
            return pb;
        }
        int conLai = tong;
        for (int i = 0; i < pb.length; i++) {
            final int v = (int) Math.min(DONG[i].tran, (long) tong * w[i] / tongCu);
            pb[i] = Math.max(0, Math.min(conLai, v));
            conLai -= pb[i];
        }
        return pb;
    }

    /** "12,5%" tu phan nghin. */
    public static String phanTram(final int phanNghin) {
        final int a = Math.abs(phanNghin);
        return (phanNghin < 0 ? "-" : "") + a / 10 + "," + a % 10 + "%";
    }

    /** "id:diem,id:diem" (CSDL, theo id dong - doi thu_tu khong lech) -> mang theo vi tri (hong / id la = 0). */
    public static int[] docChuoi(final String s) {
        final int[] r = new int[DONG.length];
        if (s == null || s.trim().isEmpty()) {
            return r;
        }
        for (final String p : s.split(",")) {
            final int hai = p.indexOf(':');
            if (hai <= 0) {
                continue;
            }
            try {
                final int vt = viTriId(Integer.parseInt(p.substring(0, hai).trim()));
                if (vt >= 0) {
                    r[vt] = Math.max(0, Integer.parseInt(p.substring(hai + 1).trim()));
                }
            } catch (final NumberFormatException ignored) {
                // phan hong: bo qua
            }
        }
        return r;
    }

    /** Mang theo vi tri -> "id:diem,..." (chi dong co diem). */
    public static String viet(final int[] pb) {
        final StringBuilder b = new StringBuilder();
        final Dong[] a = DONG;
        for (int i = 0; pb != null && i < pb.length && i < a.length; i++) {
            if (pb[i] <= 0) {
                continue;
            }
            if (b.length() > 0) {
                b.append(',');
            }
            b.append(a[i].id).append(':').append(pb[i]);
        }
        return b.toString();
    }
}
