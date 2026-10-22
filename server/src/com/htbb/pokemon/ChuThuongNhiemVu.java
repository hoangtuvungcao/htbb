package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * CHU PHAN THUONG cua nhiem vu -64 (data_quest_moi.infogift, doan sau "Phần thưởng:|") SINH TU DONG THUONG THAT
 * (nhanh sua-tuyen) - luat thuan, khong I/O. Truoc day chu nay do bo sinh viet tay roi cat 200 ky tu (cot varchar(200))
 * va liet ke ca thu KHONG phat (huy hieu - nhan khi thang Gym, trang truyen / trang tri / diem bang - chi ghi nhat ky).
 *
 * <pre>
 *  Dung dung nhung gi InfoQuestTemplate.addGift + NhiemVuEngine.khiXongChuoiCu phat khi tra qua -64:
 *    exp, gold (xu), luong, luongLock (luong khoa), itemgift (vat pham theo dong: id lop 0..4 + cong), potiongift x npotion,
 *    va voi CHUOI CHINH (mainsub 0, ma co trong THUONG_CU) thuong_khac: item:&lt;id&gt;xN, thuoc:&lt;id&gt;xN, danh_hieu:&lt;ten&gt;.
 *  Thu tu: EXP, xu, luong, luong khoa, vat pham, thuoc, thuong khac. So co dau cham hang nghin.
 * </pre>
 * Bo sinh SQL (tools/pokemon/sinh_sua_tuyen.py) va server (real.ChuThuongNhiemVuGame, luc gui -64) cung luat; test
 * KiemSuaTuyen doi chieu CSDL voi lop nay.
 */
public final class ChuThuongNhiemVu {

    public static final String NHAN = "Phần thưởng:|";
    public static final String KHONG_CO = "Không có";

    /** Tra ten vat pham / thuoc theo id (null = khong biet). */
    public interface Ten {
        String ten(int id);
    }

    private ChuThuongNhiemVu() {
    }

    /**
     * Dong phan thuong.
     * @param itemgift  "id0,id1,id2,id3,id4,cong" ("32000" = khong co)
     * @param lop       lop nhan vat 0..4 (chi ten vat pham cua lop do), -1 = moi lop (ten khac nhau noi " / ")
     * @param thuongKhac thuong_khac (chi chuoi chinh tra qua -64), null / "" = khong
     */
    public static String dong(final long exp, final long xu, final long luong, final long luongKhoa, final String itemgift,
            final int lop, final String potiongift, final String npotion, final String thuongKhac, final Ten tenVatPham,
            final Ten tenThuoc) {
        final List<String> p = new ArrayList<String>();
        if (exp > 0) {
            p.add(HuongDanTanThu.so(exp) + " EXP");
        }
        if (xu > 0) {
            p.add(HuongDanTanThu.so(xu) + " xu");
        }
        if (luong > 0) {
            p.add(HuongDanTanThu.so(luong) + " lượng");
        }
        if (luongKhoa > 0) {
            p.add(HuongDanTanThu.so(luongKhoa) + " lượng khoá");
        }
        final String vp = vatPham(itemgift, lop, tenVatPham);
        if (vp != null) {
            p.add(vp);
        }
        final int[] th = soNguyen(potiongift);
        final int[] nt = soNguyen(npotion);
        for (int i = 0; i < th.length && i < nt.length; i++) {
            if (th[i] < 32000 && nt[i] > 0) {
                p.add(nt[i] + " " + ten(tenThuoc, th[i], "thuốc"));
            }
        }
        if (thuongKhac != null) {
            for (final String phan : thuongKhac.split(";")) {
                final int k = phan.indexOf(':');
                if (k <= 0) {
                    continue;
                }
                final String loai = phan.substring(0, k).trim();
                final String v = phan.substring(k + 1).trim();
                if ("danh_hieu".equals(loai) && !v.isEmpty()) {
                    p.add("danh hiệu " + v);
                } else if ("item".equals(loai) || "thuoc".equals(loai)) {
                    final int x = v.indexOf('x');
                    try {
                        final int id = Integer.parseInt((x > 0 ? v.substring(0, x) : v).trim());
                        final int n = x > 0 ? Integer.parseInt(v.substring(x + 1).trim()) : 1;
                        if (n > 0) {
                            p.add("item".equals(loai) ? ten(tenVatPham, id, "vật phẩm") + " x" + n : n + " " + ten(tenThuoc, id, "thuốc"));
                        }
                    } catch (final NumberFormatException ignored) {
                        // dong hong: engine cung bo qua
                    }
                }
                // truyen / trang_tri / diem_bang: engine chi ghi nhat ky, khong phat -> khong ghi vao chu thuong
            }
        }
        return p.isEmpty() ? KHONG_CO : String.join(", ", p);
    }

    /** Ten vat pham thuong (itemgift) - null khi khong co. */
    static String vatPham(final String itemgift, final int lop, final Ten ten) {
        final int[] g = soNguyen(itemgift);
        if (g.length == 0 || g[0] >= 32000) {
            return null;
        }
        final int cong = g.length > 5 ? g[5] : 0;
        final String duoi = cong > 0 ? " +" + cong : "";
        if (lop >= 0 && lop < 5 && lop < g.length) {
            return ten(ten, g[lop], "vật phẩm") + duoi;
        }
        final LinkedHashSet<String> ds = new LinkedHashSet<String>();
        for (int i = 0; i < 5 && i < g.length; i++) {
            ds.add(ten(ten, g[i], "vật phẩm"));
        }
        if (ds.size() == 1) {
            return ds.iterator().next() + duoi;
        }
        return String.join(" / ", ds) + duoi + " (theo dòng Pokémon)";
    }

    static String ten(final Ten t, final int id, final String macDinh) {
        final String s = t == null ? null : t.ten(id);
        return s == null || s.trim().isEmpty() ? macDinh + " #" + id : s.trim();
    }

    static int[] soNguyen(final String s) {
        if (s == null || s.trim().isEmpty()) {
            return new int[0];
        }
        final String[] a = s.split(",");
        final int[] kq = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            try {
                kq[i] = Integer.parseInt(a[i].trim());
            } catch (final NumberFormatException e) {
                kq[i] = 32000;
            }
        }
        return kq;
    }

    /** Thay doan phan thuong trong infogift (giu "Nhiệm vụ / Địa điểm / NPC"); khong co nhan thi noi them. */
    public static String thayPhanThuong(final String infogift, final String dong) {
        final String g = infogift == null ? "" : infogift;
        final int i = g.indexOf(NHAN);
        if (i < 0) {
            return g.isEmpty() ? NHAN + dong : g + "|" + NHAN + dong;
        }
        return g.substring(0, i + NHAN.length()) + dong;
    }

    /** Doan phan thuong hien co trong infogift ("" khi khong co nhan). */
    public static String phanThuong(final String infogift) {
        if (infogift == null) {
            return "";
        }
        final int i = infogift.indexOf(NHAN);
        return i < 0 ? "" : infogift.substring(i + NHAN.length());
    }

    /** Ten tu bang (test / cong cu). */
    public static Ten tu(final Map<Integer, String> m) {
        return new Ten() {
            @Override
            public String ten(final int id) {
                return m.get(Integer.valueOf(id));
            }
        };
    }
}
