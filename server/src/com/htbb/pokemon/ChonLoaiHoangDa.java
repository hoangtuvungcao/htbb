package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Chon loai hien thi cho mot quai thuong VUA HOI SINH (he_thong_day_du.md 2.4, 3.7). Mau quai (cap, HP, TTK) giu
 * nguyen; chi doi loai (dex) gan cho con quai do. Khong them quai moi nao: so quai tren ban do khong doi (khong lam
 * phong spawn).
 *
 * Mot luot (moi lan hoi sinh):
 *   u < 1/gapT5                -> loai T5 cua vung (chi dong DUNG khung gio HOAC dung thoi tiet)
 *   u < + heSo/gapT4           -> loai T4 cua vung (dung ngay / dem neu loai co)
 *   u < + heSo/gapT3           -> loai T3 cua vung
 *   con lai, doiThuong%        -> loai T1-T2 cua vung theo trong so (quan the vung)
 *   con lai                    -> giu loai mac dinh cua mau (0)
 * heSo = gio it nguoi (0-5 gio) x1,5, kep o heSoToiDa. Trong bac, trong so loai hop thoi tiet x1,5.
 * Bac duoc chon ma khong co ung vien thi tra 0 (KHONG roi xuong bac thap hon -> khong tang ti le gap).
 * Loai chua mo, khong bat duoc (T6) hoac bac 6 KHONG BAO GIO duoc chon (T6 chi o dot kich).
 * Loai KHONG CO SPRITE dung duoc (data_loai_sprite.trang_thai = 0: 64 loai PMDCollab chua ve, chi co chan dung
 * Pokedex) cung khong bao gio duoc chon, ke ca khi dot cua no da mo - cho toi khi co hinh that.
 * Shiny rieng (sau khi da co loai rieng): {@link #shiny} - 1/mau (mac dinh 4.096, khoa shiny.mau_so) x he so su kien
 * (tuan shiny x10, ngay cong dong x5 CHI trong gio vang - SuKienLauDai.nhanShiny); chi loai co goi shiny.
 *
 * Lop THUAN (Random truyen vao), test: test/KiemLoaiDayDu.
 */
public final class ChonLoaiHoangDa {

    /** Tham so (khoa cau hinh loai.*). */
    public static final class ThamSo {
        public int doiThuongPhanTram = 30;
        public int gapT3 = 100;
        public int gapT4 = 600;
        public int gapT5 = 4000;
        public int heSoToiDaPhanTram = 300;
    }

    /** Ket qua: dex (0 = giu loai mau) va bac luot gap. */
    public static final class KetQua {
        public final int dex, bac;

        KetQua(final int dex, final int bac) {
            this.dex = dex;
            this.bac = bac;
        }
    }

    public static final KetQua GIU = new KetQua(0, 0);

    private ChonLoaiHoangDa() {
    }

    /** Gio it nguoi (NRO "gio vang" doi thanh tang gap T3-T4, 2.1). */
    public static boolean gioItNguoi(final int gio) {
        return gio >= 0 && gio < 5;
    }

    /** He so gap T3-T4 (phan tram). */
    public static int heSoPhanTram(final int gio, final ThamSo ts) {
        int h = 100;
        if (gioItNguoi(gio)) {
            h = h * 150 / 100;
        }
        return Math.min(h, Math.max(100, ts.heSoToiDaPhanTram));
    }

    /** Dong vung co hieu luc luc nay khong. */
    public static boolean conHieuLuc(final LoaiDuLieu.Vung v, final boolean dem, final int thoiTiet) {
        final boolean dungGio = v.ngayDem == 0 || (v.ngayDem == 2) == dem;
        final boolean dungThoiTiet = v.thoiTiet < 0 || v.thoiTiet == thoiTiet;
        if (v.bacGap >= LoaiDuLieu.T5) {
            // T5: dung khung gio HOAC dung thoi tiet (2.4)
            final boolean coGio = v.ngayDem != 0, coTt = v.thoiTiet >= 0;
            if (!coGio && !coTt) {
                return true;
            }
            return (coGio && (v.ngayDem == 2) == dem) || (coTt && v.thoiTiet == thoiTiet);
        }
        return dungGio && dungThoiTiet;
    }

    /** Ung vien cua mot bac (bacMin..bacMax) cho (khu vuc, vung, cap quai). */
    public static List<LoaiDuLieu.Vung> ungVien(final LoaiDuLieu d, final String khuVuc, final int vung, final int cap,
            final boolean dem, final int thoiTiet, final int bacMin, final int bacMax) {
        final List<LoaiDuLieu.Vung> kq = new ArrayList<LoaiDuLieu.Vung>();
        for (final LoaiDuLieu.Vung v : d.vung(khuVuc, vung)) {
            if (v.bacGap < bacMin || v.bacGap > bacMax || v.bacGap >= LoaiDuLieu.T6) {
                continue;
            }
            if (cap < v.capMin || cap > v.capMax || v.trongSo <= 0) {
                continue;
            }
            final LoaiDuLieu.Loai l = d.lay(v.dex);
            if (l == null || !l.daMo || !l.batDuoc || l.bac >= LoaiDuLieu.T6 || l.sprite <= 0) {
                continue;
            }
            if (!conHieuLuc(v, dem, thoiTiet)) {
                continue;
            }
            kq.add(v);
        }
        return kq;
    }

    static int trongSo(final LoaiDuLieu d, final LoaiDuLieu.Vung v, final int thoiTiet) {
        final LoaiDuLieu.Loai l = d.lay(v.dex);
        int w = v.trongSo;
        if (l != null && thoiTiet >= 0 && (SinhThaiTheGioi.heHopThoiTiet(l.he1, thoiTiet)
                || (l.he2 >= 0 && SinhThaiTheGioi.heHopThoiTiet(l.he2, thoiTiet)))) {
            w = w * 3 / 2;
        }
        return w;
    }

    static int chonTrongSo(final LoaiDuLieu d, final List<LoaiDuLieu.Vung> ds, final int thoiTiet, final Random r) {
        long tong = 0;
        for (final LoaiDuLieu.Vung v : ds) {
            tong += trongSo(d, v, thoiTiet);
        }
        if (tong <= 0) {
            return 0;
        }
        long x = (long) (r.nextDouble() * tong);
        for (final LoaiDuLieu.Vung v : ds) {
            x -= trongSo(d, v, thoiTiet);
            if (x < 0) {
                return v.dex;
            }
        }
        return ds.get(ds.size() - 1).dex;
    }

    /**
     * Con quai vua co loai rieng {@code dex} co la shiny khong: {@code roll} = so ngau nhien 0..mau-1, {@code nhan} = he so
     * su kien (1 thuong, 10 tuan shiny, 5 gio vang ngay cong dong). Loai khong co ban sac mau (chua co goi shiny that,
     * KHONG doi mau gia) thi khong bao gio shiny. Khong pity (pity la cua dem ha quai tung nguoi, RoiLauDai).
     */
    public static boolean shiny(final LoaiDuLieu d, final int dex, final int roll, final int mau, final int nhan) {
        if (d == null || dex <= 0 || !d.coShiny(dex)) {
            return false;
        }
        return roll >= 0 && roll < Math.max(1, Math.min(nhan, Math.max(1, mau)));
    }

    /**
     * Mot luot chon. {@code dexMau}: loai mac dinh cua mau quai (khong bao gio tra lai chinh no - tra GIU).
     */
    public static KetQua chon(final LoaiDuLieu d, final String khuVuc, final int vung, final int cap, final int gio,
            final boolean dem, final int thoiTiet, final int dexMau, final ThamSo ts, final Random r) {
        if (d == null || d.rong() || vung < 0) {
            return GIU;
        }
        final double heSo = heSoPhanTram(gio, ts) / 100.0;
        final double p5 = 1.0 / Math.max(1, ts.gapT5);
        final double p4 = heSo / Math.max(1, ts.gapT4);
        final double p3 = heSo / Math.max(1, ts.gapT3);
        final double u = r.nextDouble();
        int bac;
        if (u < p5) {
            bac = LoaiDuLieu.T5;
        } else if (u < p5 + p4) {
            bac = LoaiDuLieu.T4;
        } else if (u < p5 + p4 + p3) {
            bac = LoaiDuLieu.T3;
        } else if (r.nextInt(100) < ts.doiThuongPhanTram) {
            bac = LoaiDuLieu.T2;
        } else {
            return GIU;
        }
        final List<LoaiDuLieu.Vung> ds = bac == LoaiDuLieu.T2
                ? ungVien(d, khuVuc, vung, cap, dem, thoiTiet, LoaiDuLieu.T1, LoaiDuLieu.T2)
                : ungVien(d, khuVuc, vung, cap, dem, thoiTiet, bac, bac);
        if (ds.isEmpty()) {
            return GIU;
        }
        final int dex = chonTrongSo(d, ds, thoiTiet, r);
        if (dex <= 0 || dex == dexMau) {
            return GIU;
        }
        return new KetQua(dex, d.bac(dex));
    }
}
