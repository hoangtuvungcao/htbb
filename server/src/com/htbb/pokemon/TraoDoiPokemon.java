package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TRAO DOI POKEMON giua hai nguoi choi + CHUYEN DOI con trung loai (docs/thiet-ke-game.md 7.8). Lop THUAN
 * (test/KiemTraoDoiPokemon): moi thoi gian / CSDL truyen vao tu ngoai.
 *
 * <h3>Doi nguyen tu (khong nhan ban khi rot mang / sap may)</h3>
 * {@link #thucHien}: khoa HAI hop theo thu tu charDBID (khong deadlock khi hai phien cheo nhau), kiem lai moi con con
 * nguyen nhu luc chon, lam tren BAN SAO, goi {@link Luu#luu} ghi CA HAI chuoi + nhat ky trong MOT giao dich CSDL; chi
 * khi ghi xong moi thay hop that bang ban sao. Ghi loi / nem loi / sap truoc khi commit: hai hop that khong doi (CSDL
 * van giu ban cu) - khong ai mat, khong ai duoc them. Sap sau commit: CSDL da co ban moi, nap lai la dung.
 */
public final class TraoDoiPokemon {

    /** Toi da moi ben moi lan. */
    public static final int TOI_DA_MOI_BEN = 3;

    private TraoDoiPokemon() {
    }

    /** Ghi hai hop (va nhat ky) trong mot giao dich; false / nem loi = khong commit. */
    public interface Luu {
        boolean luu(int idA, String hopA, int idB, String hopB, List<HopPc.Con> aDua, List<HopPc.Con> bDua) throws Exception;
    }

    /** Luat (tu cau hinh trao_doi.*). */
    public static final class Luat {
        /** So Pokemon nhan toi da / ngay / nhan vat. */
        public final int tranNgay;
        /** Con vua nhan qua trao doi: bao lau (giay) moi trao doi tiep duoc. */
        public final long khoaGiay;
        /** Nhip giua hai lan trao doi cua mot nhan vat (giay). */
        public final long cachGiay;

        public Luat(final int tranNgay, final long khoaGiay, final long cachGiay) {
            this.tranNgay = Math.max(0, tranNgay);
            this.khoaGiay = Math.max(0L, khoaGiay);
            this.cachGiay = Math.max(0L, cachGiay);
        }

        public static Luat macDinh() {
            return new Luat(10, 24L * 3600L, 60L);
        }
    }

    /** Ket qua. */
    public static final class KetQua {
        public final boolean ok;
        public final String loi;
        /** Con A nhan (id trong hop A, da tien hoa neu co) / B nhan. */
        public final List<HopPc.Con> aNhan;
        public final List<HopPc.Con> bNhan;
        /** {dex truoc, dex sau} cac con tien hoa khi trao doi. */
        public final List<int[]> tienHoa;

        KetQua(final boolean ok, final String loi, final List<HopPc.Con> aNhan, final List<HopPc.Con> bNhan, final List<int[]> tienHoa) {
            this.ok = ok;
            this.loi = loi == null ? "" : loi;
            this.aNhan = aNhan;
            this.bNhan = bNhan;
            this.tienHoa = tienHoa;
        }

        static KetQua hong(final String loi) {
            return new KetQua(false, loi, new ArrayList<HopPc.Con>(), new ArrayList<HopPc.Con>(), new ArrayList<int[]>());
        }
    }

    // ------------------------------------------------------------------ kiem mot con / mot ben

    /** Ly do con {@code c} khong dua vao trao doi duoc, null neu duoc. */
    public static String lyDoKhongDua(final HopPc h, final HopPc.Con c, final long bayGiay, final Luat luat) {
        if (c == null) {
            return "Pokémon đó không còn trong Hộp PC.";
        }
        if (h.dangTheo == c.id) {
            return "Pokémon này đang đi theo bạn: chọn Thôi đi theo trước khi trao đổi.";
        }
        if (c.doiLuc > 0 && bayGiay - c.doiLuc < luat.khoaGiay) {
            final long con = (luat.khoaGiay - (bayGiay - c.doiLuc) + 3599L) / 3600L;
            return "Pokémon vừa nhận qua trao đổi, " + Math.max(1L, con) + " giờ nữa mới trao đổi tiếp được.";
        }
        return null;
    }

    /** Kiem mot ben: so con, trung id, tung con, nhip, tran ngay (so con se NHAN), cho trong hop sau khi doi. */
    public static String kiemBen(final HopPc h, final List<Integer> dua, final int nhan, final long bayGiay, final int homNay,
            final Luat luat) {
        if (dua == null || dua.isEmpty()) {
            return "Mỗi bên cần đưa ít nhất 1 Pokémon.";
        }
        if (dua.size() > TOI_DA_MOI_BEN) {
            return "Mỗi lần tối đa " + TOI_DA_MOI_BEN + " Pokémon mỗi bên.";
        }
        final Set<Integer> thay = new HashSet<Integer>(dua);
        if (thay.size() != dua.size()) {
            return "Một Pokémon được chọn hai lần.";
        }
        synchronized (h) {
            final int daNhan = h.ngay == homNay ? h.doiNgay : 0; // khong sua hop khi chi kiem
            if (luat.cachGiay > 0 && h.doiCuoi > 0 && bayGiay - h.doiCuoi < luat.cachGiay) {
                return "Vừa trao đổi xong, chờ " + (luat.cachGiay - (bayGiay - h.doiCuoi)) + " giây nữa.";
            }
            if (daNhan + nhan > luat.tranNgay) {
                return "Hôm nay chỉ nhận tối đa " + luat.tranNgay + " Pokémon qua trao đổi (đã nhận " + daNhan + ").";
            }
            for (final Integer id : dua) {
                final String m = lyDoKhongDua(h, h.lay(id.intValue()), bayGiay, luat);
                if (m != null) {
                    return m;
                }
            }
            if (h.soCon() - dua.size() + nhan > h.sucChua()) {
                return "Hộp PC không đủ chỗ (" + h.soCon() + "/" + h.sucChua() + ").";
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ doi

    /**
     * Doi nguyen tu. {@code mauA} / {@code mauB}: ban sao cac con luc hai ben KHOA (con nao doi cap / loai / ca the
     * tu luc do thi huy). {@code tienHoaTraoDoi}: dex -&gt; dex sau khi trao doi (Haunter -&gt; Gengar).
     */
    public static KetQua thucHien(final int idA, final HopPc hA, final List<HopPc.Con> mauA, final int idB, final HopPc hB,
            final List<HopPc.Con> mauB, final long bayGiay, final int homNay, final Luat luat,
            final Map<Integer, Integer> tienHoaTraoDoi, final Luu luu) {
        if (hA == null || hB == null || hA == hB || idA == idB) {
            return KetQua.hong("Không thể tự trao đổi với chính mình.");
        }
        final HopPc dau = idA < idB ? hA : hB;
        final HopPc sau = idA < idB ? hB : hA;
        synchronized (dau) {
            synchronized (sau) {
                final List<Integer> dA = ids(mauA);
                final List<Integer> dB = ids(mauB);
                String m = kiemBen(hA, dA, dB.size(), bayGiay, homNay, luat);
                if (m == null) {
                    m = kiemBen(hB, dB, dA.size(), bayGiay, homNay, luat);
                }
                if (m == null) {
                    m = kiemGiong(hA, mauA);
                }
                if (m == null) {
                    m = kiemGiong(hB, mauB);
                }
                if (m != null) {
                    return KetQua.hong(m);
                }
                // lam tren ban sao
                final HopPc sA = hA.banSao();
                final HopPc sB = hB.banSao();
                sA.doiNgay(homNay);
                sB.doiNgay(homNay);
                final List<HopPc.Con> aDua = new ArrayList<HopPc.Con>();
                final List<HopPc.Con> bDua = new ArrayList<HopPc.Con>();
                for (final Integer id : dA) {
                    aDua.add(sA.tha(id.intValue()));
                }
                for (final Integer id : dB) {
                    bDua.add(sB.tha(id.intValue()));
                }
                final List<int[]> tienHoa = new ArrayList<int[]>();
                final List<HopPc.Con> aNhan = new ArrayList<HopPc.Con>();
                final List<HopPc.Con> bNhan = new ArrayList<HopPc.Con>();
                for (final HopPc.Con c : bDua) {
                    aNhan.add(nhan(sA, c, bayGiay, tienHoaTraoDoi, tienHoa));
                }
                for (final HopPc.Con c : aDua) {
                    bNhan.add(nhan(sB, c, bayGiay, tienHoaTraoDoi, tienHoa));
                }
                if (aNhan.contains(null) || bNhan.contains(null)) {
                    return KetQua.hong("Hộp PC không đủ chỗ.");
                }
                sA.doiNgay += aNhan.size();
                sB.doiNgay += bNhan.size();
                sA.doiCuoi = bayGiay;
                sB.doiCuoi = bayGiay;
                sA.tongDoi++;
                sB.tongDoi++;
                boolean daGhi;
                try {
                    daGhi = luu != null && luu.luu(idA, sA.maHoa(), idB, sB.maHoa(), aDua, bDua);
                } catch (final Exception e) {
                    daGhi = false;
                }
                if (!daGhi) {
                    return KetQua.hong("Máy chủ chưa ghi được trao đổi, không ai mất Pokémon. Thử lại sau.");
                }
                hA.thayBang(sA);
                hB.thayBang(sB);
                hA.ban = false;
                hB.ban = false;
                return new KetQua(true, "", aNhan, bNhan, tienHoa);
            }
        }
    }

    static List<Integer> ids(final List<HopPc.Con> ds) {
        final List<Integer> v = new ArrayList<Integer>();
        if (ds != null) {
            for (final HopPc.Con c : ds) {
                v.add(Integer.valueOf(c.id));
            }
        }
        return v;
    }

    static String kiemGiong(final HopPc h, final List<HopPc.Con> mau) {
        for (final HopPc.Con m : mau) {
            final HopPc.Con c = h.lay(m.id);
            if (c == null || !c.giong(m)) {
                return "Một Pokémon trong trao đổi đã thay đổi, trao đổi bị huỷ.";
            }
        }
        return null;
    }

    static HopPc.Con nhan(final HopPc h, final HopPc.Con c, final long bayGiay, final Map<Integer, Integer> tienHoa,
            final List<int[]> ghi) {
        if (c == null) {
            return null;
        }
        final HopPc.Con moi = h.nhan(c);
        if (moi == null) {
            return null;
        }
        moi.doiLuc = bayGiay;
        final Integer sau = tienHoa != null ? tienHoa.get(Integer.valueOf(moi.dex)) : null;
        if (sau != null && sau.intValue() > 0) {
            ghi.add(new int[]{moi.dex, sau.intValue()});
            moi.dex = sau.intValue();
        }
        return moi;
    }

    // ------------------------------------------------------------------ phi (ho hut xu)

    /** He so phi theo bac hiem (Thuong / It gap / Hiem / Rat hiem). */
    public static final int[] HE_SO_PHI_HIEM = {1, 2, 4, 8};

    /**
     * Phi (xu) cho MOT con nguoi nhan phai tra: phanNghinGio x gio thu nhap o cap nguoi nhan x bac hiem x (sac mau x4)
     * x (3 sao x2), kep o tranPhanNghinGio.
     */
    public static long phi(final long xuMoiGio, final int phanNghinGio, final int tranPhanNghinGio, final int doHiem,
            final boolean shiny, final int sao) {
        long pn = (long) Math.max(0, phanNghinGio) * HE_SO_PHI_HIEM[Math.max(0, Math.min(3, doHiem))];
        if (shiny) {
            pn *= 4;
        }
        if (sao >= 3) {
            pn *= 2;
        }
        pn = Math.min(pn, Math.max(0, tranPhanNghinGio));
        return Math.max(0L, Math.max(0L, xuMoiGio) * pn / 1000L);
    }

    // ------------------------------------------------------------------ chuyen doi con trung loai

    /** Phan thuong chuyen doi mot con (dong data_bat_chuyen_doi). */
    public static final class ThuongChuyen {
        public final int keo;
        public final int xuPhanNghinGio;
        public final int luongKhoa;

        public ThuongChuyen(final int keo, final int xuPhanNghinGio, final int luongKhoa) {
            this.keo = Math.max(0, keo);
            this.xuPhanNghinGio = Math.max(0, xuPhanNghinGio);
            this.luongKhoa = Math.max(0, luongKhoa);
        }
    }

    /** Bang mac dinh theo bac hiem 0..3 (khi CSDL chua co data_bat_chuyen_doi). */
    public static ThuongChuyen[] chuyenMacDinh() {
        return new ThuongChuyen[]{new ThuongChuyen(2, 10, 0), new ThuongChuyen(3, 30, 0), new ThuongChuyen(5, 100, 0),
            new ThuongChuyen(8, 0, 1)};
    }

    /** Thuong da tinh {keo, xu, luong khoa}. */
    public static final class DaChuyen {
        public final int keo;
        public final long xu;
        public final int luongKhoa;

        DaChuyen(final int keo, final long xu, final int luongKhoa) {
            this.keo = keo;
            this.xu = xu;
            this.luongKhoa = luongKhoa;
        }
    }

    /**
     * Thuong khi chuyen doi con {@code c} cua hop {@code h} (chu = {@code chuId}). Xu / luong khoa CHI khi con do chinh
     * chu thu phuc (con nhan qua trao doi chi ra Keo - chan nuoi clone bat ho roi chuyen cho nick chinh doi xu) va con
     * trong tran ngay; sac mau x2 keo. Ghi bo dem ngay vao {@code h} (goi trong khoa h).
     */
    public static DaChuyen tinhChuyen(final HopPc h, final HopPc.Con c, final int chuId, final ThuongChuyen t, final long xuMoiGio,
            final int tranXuPhanNghinGio, final int tranLuongNgay, final int homNay) {
        h.doiNgay(homNay);
        final int keo = t.keo * (c.shiny ? 2 : 1);
        if (c.chuGoc != chuId || c.doiLuc > 0) {
            return new DaChuyen(keo, 0L, 0);
        }
        final long tranXu = Math.max(0L, xuMoiGio) * Math.max(0, tranXuPhanNghinGio) / 1000L;
        final long xu = Math.max(0L, Math.min(Math.max(0L, xuMoiGio) * t.xuPhanNghinGio / 1000L, tranXu - h.chuyenXuNgay));
        final int luong = Math.max(0, Math.min(t.luongKhoa, tranLuongNgay - h.chuyenLuongNgay));
        h.chuyenXuNgay += xu;
        h.chuyenLuongNgay += luong;
        h.tongChuyen++;
        h.ban = true;
        return new DaChuyen(keo, xu, luong);
    }

    /**
     * Khi bat duoc mot con THUONG (khong sac mau) ma hop da co >= {@code giu} con thuong cung loai: chon con se chuyen
     * doi = con co tong IV thap nhat trong {cac con thuong cung loai do chinh chu bat, khong dang theo} va con moi.
     * Tra id con cu can chuyen, 0 = chuyen chinh con moi, -1 = khong chuyen (giu ca).
     */
    public static int chonChuyenKhiBat(final HopPc h, final int dex, final boolean shiny, final int ivMoi, final int giu,
            final int chuId) {
        if (shiny || giu <= 0) {
            return -1;
        }
        synchronized (h) {
            if (h.soCungLoai(dex, true) < giu) {
                return -1;
            }
            int idThap = 0;
            int thap = CaThePokemon.tong(ivMoi);
            for (final HopPc.Con c : h.danhSach()) {
                if (c.dex == dex && !c.shiny && c.id != h.dangTheo && c.chuGoc == chuId && c.doiLuc == 0
                        && CaThePokemon.tong(c.iv) < thap) {
                    thap = CaThePokemon.tong(c.iv);
                    idThap = c.id;
                }
            }
            return idThap;
        }
    }
}
