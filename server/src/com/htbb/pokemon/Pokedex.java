package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Pokedex cua MOT nhan vat (viec G7.D2/D4): loai da gap / da ha (theo dex cua template quai)
 * va cac moc thuong da nhan. Ban trong bo nho - real.PokedexPokemon giu cache theo
 * charDBID, data.DatabasePokedex nap/luu (bang tob_pokedex, tob_pokedex_thuong).
 *
 * "Da gap" = co dong (danh hoac ha). "Da ha" = so_ha > 0.
 * Ghi theo lo: moi lan danh/ha chi doi bo nho + danh dau "ban" (dirty); saveChar xa ra CSDL.
 *
 * Lop THUAN + dong bo (danh quai chay tren luong map, luu chay tren luong luu).
 */
public final class Pokedex {

    /** Moc so loai DA HA de nhan thuong (D3). Doi thi giu tang dan. */
    public static final int[] MOC = {10, 25, 50, 100};

    /** dex -> {so_ha, lan_dau (giay epoch)}. */
    private final Map<Integer, long[]> bang = new LinkedHashMap<>();
    private final Set<Integer> ban = new HashSet<>();
    private final Set<Integer> mocDaNhan = new TreeSet<>();

    /** Nap mot dong tu CSDL (khong danh dau ban). */
    public synchronized void napDong(final int dex, final int soHa, final long lanDau) {
        if (dex <= 0) {
            return;
        }
        bang.put(Integer.valueOf(dex), new long[]{Math.max(0, soHa), lanDau});
    }

    public synchronized void napMoc(final int moc) {
        mocDaNhan.add(Integer.valueOf(moc));
    }

    /** Gap (danh trung) loai {@code dex}. Tra ve true neu la lan dau gap. */
    public synchronized boolean ghiGap(final int dex, final long bayGioGiay) {
        if (dex <= 0) {
            return false;
        }
        final Integer k = Integer.valueOf(dex);
        if (bang.containsKey(k)) {
            return false;
        }
        bang.put(k, new long[]{0, bayGioGiay});
        ban.add(k);
        return true;
    }

    /** Ha mot con loai {@code dex}. Tra ve true neu la lan dau ha loai nay. */
    public synchronized boolean ghiHa(final int dex, final long bayGioGiay) {
        if (dex <= 0) {
            return false;
        }
        final Integer k = Integer.valueOf(dex);
        long[] d = bang.get(k);
        if (d == null) {
            d = new long[]{0, bayGioGiay};
            bang.put(k, d);
        }
        final boolean dau = d[0] == 0;
        if (d[0] < Integer.MAX_VALUE) {
            d[0]++;
        }
        ban.add(k);
        return dau;
    }

    public synchronized int soHa(final int dex) {
        final long[] d = bang.get(Integer.valueOf(dex));
        return d == null ? 0 : (int) d[0];
    }

    public synchronized boolean daGap(final int dex) {
        return bang.containsKey(Integer.valueOf(dex));
    }

    public synchronized int soLoaiDaGap() {
        return bang.size();
    }

    public synchronized int soLoaiDaHa() {
        int n = 0;
        for (final long[] d : bang.values()) {
            if (d[0] > 0) {
                n++;
            }
        }
        return n;
    }

    public synchronized boolean daNhanMoc(final int moc) {
        return mocDaNhan.contains(Integer.valueOf(moc));
    }

    public synchronized void danhDauMoc(final int moc) {
        mocDaNhan.add(Integer.valueOf(moc));
    }

    /**
     * Lay va XOA danh sach dong ban: moi phan tu {dex, so_ha, lan_dau}. Noi goi ghi CSDL;
     * ghi loi thi goi {@link #traLaiBan} de lan sau ghi lai.
     */
    public synchronized List<long[]> layDongBan() {
        final List<long[]> ds = new ArrayList<>();
        for (final Integer k : ban) {
            final long[] d = bang.get(k);
            if (d != null) {
                ds.add(new long[]{k.longValue(), d[0], d[1]});
            }
        }
        ban.clear();
        return ds;
    }

    public synchronized void traLaiBan(final Collection<long[]> ds) {
        for (final long[] d : ds) {
            ban.add(Integer.valueOf((int) d[0]));
        }
    }

    public synchronized boolean coBan() {
        return !ban.isEmpty();
    }

    /** Top {@code n} loai ha nhieu nhat: {dex, so_ha}, so_ha giam dan roi dex tang dan. */
    public synchronized List<int[]> top(final int n) {
        final List<int[]> ds = new ArrayList<>();
        for (final Map.Entry<Integer, long[]> e : bang.entrySet()) {
            if (e.getValue()[0] > 0) {
                ds.add(new int[]{e.getKey().intValue(), (int) e.getValue()[0]});
            }
        }
        Collections.sort(ds, (a, b) -> a[1] != b[1] ? Integer.compare(b[1], a[1]) : Integer.compare(a[0], b[0]));
        return ds.size() > n ? new ArrayList<>(ds.subList(0, Math.max(0, n))) : ds;
    }

    // ------------------------------------------------------------------ ham thuan tinh

    /** So loai khac nhau (dex > 0) trong danh sach dex cua cac template quai. */
    public static int tongLoai(final int[] dexTemplate) {
        final Set<Integer> s = new HashSet<>();
        if (dexTemplate != null) {
            for (final int d : dexTemplate) {
                if (d > 0) {
                    s.add(Integer.valueOf(d));
                }
            }
        }
        return s.size();
    }

    /** Moc da du dieu kien (soLoaiDaHa >= moc) va chua nhan. */
    public static boolean nhanDuoc(final int moc, final int soLoaiDaHa, final boolean daNhan) {
        return laMoc(moc) && !daNhan && soLoaiDaHa >= moc;
    }

    public static boolean laMoc(final int moc) {
        for (final int m : MOC) {
            if (m == moc) {
                return true;
            }
        }
        return false;
    }

    /**
     * Chuoi thuong (cu phap DatabaseGiftCode.applyItemString) cua moc. PT:id:sl la thuoc
     * (data_potion: 3 HP to, 6 MP to, 10/11/12 tien duoc exp), ITEM:id:sl la vat pham
     * (960-967 = da tien hoa Eevee, chon theo {@code rDa}). "" neu khong phai moc.
     */
    public static String phanThuong(final int moc, final int rDa) {
        final int da = DaTienHoa.ID_ITEM[Math.abs(rDa % DaTienHoa.ID_ITEM.length)];
        switch (moc) {
            case 10:
                return "PT:3:20,PT:6:20";
            case 25:
                return "PT:3:50,PT:6:50,PT:10:2";
            case 50:
                return "PT:3:100,PT:6:100,PT:11:1,ITEM:" + da + ":1";
            case 100:
                return "PT:3:200,PT:6:200,PT:12:1,ITEM:" + da + ":1";
            default:
                return "";
        }
    }

    /** Thuong cua moc co vat pham can mot o hanh trang khong. */
    public static boolean canOTrong(final int moc) {
        return phanThuong(moc, 0).contains("ITEM:");
    }
}
