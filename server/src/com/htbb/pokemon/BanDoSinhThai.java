package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * Doc bo cuc ban do (cMap, DinhDangBanDo) de biet MEP NUOC (cau ca) va DIEM DAO (khao co). Lop THUAN.
 *
 * O nuoc = tile co ten bat dau "W" (nuoc, nuoc sau, mep nuoc) hoac "sen", kieu 2 (chan), trong bo tile moi
 * (docs/pokemon/bang_tile_moi.csv, cot ten). test/KiemHeSinhThai doi chieu bang duoi voi CSV.
 * O di duoc = bit 2 cua tep .type bang 0 (DINH_DANG_MAP.md muc 1), o trong (254/255) = di duoc.
 */
public final class BanDoSinhThai {

    private BanDoSinhThai() {
    }

    /**
     * Chi so tile NUOC THAT (ten "W..." / "sen" VA kieu 2 = chan) theo bo tile ("t", "t_hang", "t_thanh").
     * Tile mep bo co phan dat di duoc (kieu 0) KHONG tinh - dung tren do la dung tren bo.
     */
    public static int[] tileNuoc(final String boTile) {
        if ("t".equals(boTile)) {
            return new int[]{8, 9, 10, 11, 12, 58, 60, 61, 62, 64, 65, 66, 67, 68, 69, 72, 74, 75, 76, 78, 79, 80, 81, 82, 83};
        }
        if ("t_hang".equals(boTile)) {
            return new int[]{3, 7, 9, 10, 11, 13, 14, 15, 16, 17, 18};
        }
        if ("t_thanh".equals(boTile)) {
            return new int[]{4, 22, 24, 25, 26, 28, 29, 30, 31, 32, 33, 50, 52, 53, 54, 56, 57, 58, 59, 60, 61};
        }
        return new int[0];
    }

    /** Luoi nuoc [y*w+x] cua ban do. */
    public static boolean[] luoiNuoc(final DinhDangBanDo bd, final String boTile) {
        final boolean[] laNuoc = new boolean[256];
        for (final int t : tileNuoc(boTile)) {
            if (t >= 0 && t < 256) {
                laNuoc[t] = true;
            }
        }
        final boolean[] r = new boolean[bd.w * bd.h];
        for (int i = 0; i < r.length; i++) {
            final int t = bd.tile[i];
            r[i] = t >= 0 && t < 254 && laNuoc[t];
        }
        return r;
    }

    /** Luoi di duoc [y*w+x] tu tep .type cua bo tile (null = coi moi o di duoc tru o nuoc). */
    public static boolean[] luoiDiDuoc(final DinhDangBanDo bd, final byte[] kieuTile, final boolean[] nuoc) {
        final boolean[] r = new boolean[bd.w * bd.h];
        for (int i = 0; i < r.length; i++) {
            final int t = bd.tile[i];
            if (t >= 254 || t < 0) {
                r[i] = true;
            } else if (kieuTile != null && t < kieuTile.length) {
                r[i] = (kieuTile[t] & 2) == 0;
            } else {
                r[i] = nuoc == null || !nuoc[i];
            }
        }
        return r;
    }

    // ------------------------------------------------------------------ ho (chi cau ca o ho)

    /** Ho: vung nuoc lien thong (4 huong) KHONG toi gan mep ban do (HO_CACH_MEP), so o trong [HO_TOI_THIEU, HO_TOI_DA_PHAN_TRAM% dien tich]. */
    public static final int HO_TOI_THIEU = 6;
    /** Ban do co vien 1-2 o quanh mep: nuoc toi sat vien (<= 2 o) = bien / song chay ra ngoai, khong phai ho. */
    public static final int HO_CACH_MEP = 2;
    public static final int HO_TOI_DA_PHAN_TRAM = 40;
    /** Ho tu {@link #HO_LON} o tro len la "ho lon" (co ca hiem); nho hon la ao (chi ca thuong). */
    public static final int HO_LON = 60;

    /**
     * Gan nhan ho cho tung o: -1 = khong phai ho (dat, bien/song cham mep, vung nuoc qua nho/qua lon),
     * k >= 0 = ho thu k (danh so theo thu tu quet hang -> cot, tat dinh).
     */
    public static int[] nhanHo(final boolean[] nuoc, final int w, final int h) {
        final int[] nhan = new int[w * h];
        java.util.Arrays.fill(nhan, -1);
        if (nuoc == null) {
            return nhan;
        }
        final int[] tam = new int[w * h];
        java.util.Arrays.fill(tam, -2);
        int soHo = 0;
        final int[] hang = new int[w * h];
        final int toiDa = w * h * HO_TOI_DA_PHAN_TRAM / 100;
        for (int bd = 0; bd < w * h; bd++) {
            if (!nuoc[bd] || tam[bd] != -2) {
                continue;
            }
            int dau = 0, cuoi = 0;
            hang[cuoi++] = bd;
            tam[bd] = -3;
            boolean chamMep = false;
            while (dau < cuoi) {
                final int i = hang[dau++];
                final int x = i % w, y = i / w;
                if (x <= HO_CACH_MEP || y <= HO_CACH_MEP || x >= w - 1 - HO_CACH_MEP || y >= h - 1 - HO_CACH_MEP) {
                    chamMep = true;
                }
                final int[] ke = {x > 0 ? i - 1 : -1, x < w - 1 ? i + 1 : -1, y > 0 ? i - w : -1, y < h - 1 ? i + w : -1};
                for (final int j : ke) {
                    if (j >= 0 && nuoc[j] && tam[j] == -2) {
                        tam[j] = -3;
                        hang[cuoi++] = j;
                    }
                }
            }
            final boolean laHo = !chamMep && cuoi >= HO_TOI_THIEU && cuoi <= toiDa;
            for (int k = 0; k < cuoi; k++) {
                tam[hang[k]] = laHo ? soHo : -1;
                nhan[hang[k]] = laHo ? soHo : -1;
            }
            if (laHo) {
                soHo++;
            }
        }
        return nhan;
    }

    /** So o cua tung ho. */
    public static int[] kichThuocHo(final int[] nhan) {
        int n = 0;
        for (final int v : nhan) {
            n = Math.max(n, v + 1);
        }
        final int[] r = new int[n];
        for (final int v : nhan) {
            if (v >= 0) {
                r[v]++;
            }
        }
        return r;
    }

    /** Ho gan nhat trong ban kinh r o quanh (x, y); -1 = khong co ho. */
    public static int hoGan(final int[] nhan, final int w, final int h, final int x, final int y, final int r) {
        if (nhan == null) {
            return -1;
        }
        for (int yy = Math.max(0, y - r); yy <= Math.min(h - 1, y + r); yy++) {
            for (int xx = Math.max(0, x - r); xx <= Math.min(w - 1, x + r); xx++) {
                if (nhan[yy * w + xx] >= 0) {
                    return nhan[yy * w + xx];
                }
            }
        }
        return -1;
    }

    /**
     * Diem cau cua moi ho: o DI DUOC sat ho (ke 4 huong), toi da {@code toiDa} diem / ho, chon trai deu
     * theo thu tu quet. Moi diem {hoId, x, y}.
     */
    public static List<int[]> diemCau(final int[] nhan, final boolean[] diDuoc, final int w, final int h, final int toiDa) {
        final List<int[]> tatCa = new ArrayList<int[]>();
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                final int i = y * w + x;
                if (diDuoc == null || !diDuoc[i] || nhan[i] >= 0) {
                    continue;
                }
                final int[] ke = {i - 1, i + 1, i - w, i + w};
                for (final int j : ke) {
                    if (nhan[j] >= 0) {
                        tatCa.add(new int[]{nhan[j], x, y});
                        break;
                    }
                }
            }
        }
        final List<int[]> ra = new ArrayList<int[]>();
        final int[] kt = kichThuocHo(nhan);
        for (int ho = 0; ho < kt.length; ho++) {
            final List<int[]> cua = new ArrayList<int[]>();
            for (final int[] d : tatCa) {
                if (d[0] == ho) {
                    cua.add(d);
                }
            }
            final int n = Math.min(toiDa, cua.size());
            for (int k = 0; k < n; k++) {
                ra.add(cua.get(k * cua.size() / n));
            }
        }
        return ra;
    }

    /** Co o nuoc trong ban kinh {@code r} o quanh (x, y) khong. */
    public static boolean ganNuoc(final boolean[] nuoc, final int w, final int h, final int x, final int y, final int r) {
        if (nuoc == null) {
            return false;
        }
        for (int yy = Math.max(0, y - r); yy <= Math.min(h - 1, y + r); yy++) {
            for (int xx = Math.max(0, x - r); xx <= Math.min(w - 1, x + r); xx++) {
                if (nuoc[yy * w + xx]) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Dem o nuoc (de biet ban do co ho / bien that hay khong). */
    public static int demNuoc(final boolean[] nuoc) {
        int n = 0;
        if (nuoc != null) {
            for (final boolean b : nuoc) {
                n += b ? 1 : 0;
            }
        }
        return n;
    }

    /**
     * {@code n} diem dao trong ngay tren ban do: o di duoc, cach mep >= 2, tat dinh theo (ngay, map).
     * Moi diem {x, y} (don vi o 16 px).
     */
    public static int[][] diemDao(final boolean[] diDuoc, final int w, final int h, final int ngay, final int mapId, final int n) {
        final List<int[]> ra = new ArrayList<int[]>();
        if (diDuoc == null || w < 6 || h < 6) {
            return new int[0][];
        }
        long hat = SinhThaiTheGioi.bam(ngay * 1000003L + mapId);
        int thu = 0;
        while (ra.size() < n && thu < n * 200) {
            thu++;
            hat = SinhThaiTheGioi.bam(hat);
            final int x = 2 + (int) Long.remainderUnsigned(hat, w - 4);
            final int y = 2 + (int) Long.remainderUnsigned(hat >>> 20, h - 4);
            if (!diDuoc[y * w + x]) {
                continue;
            }
            boolean xa = true;
            for (final int[] d : ra) {
                if (Math.abs(d[0] - x) + Math.abs(d[1] - y) < 6) {
                    xa = false;
                    break;
                }
            }
            if (xa) {
                ra.add(new int[]{x, y});
            }
        }
        return ra.toArray(new int[0][]);
    }

    /** Huong (8 huong, tieng Viet) va khoang cach o tu (x,y) toi diem gan nhat. */
    public static String goiYDiemGanNhat(final int[][] diem, final boolean[] daDao, final int x, final int y) {
        int best = -1;
        int d = Integer.MAX_VALUE;
        for (int i = 0; i < diem.length; i++) {
            if (daDao != null && i < daDao.length && daDao[i]) {
                continue;
            }
            final int dd = Math.abs(diem[i][0] - x) + Math.abs(diem[i][1] - y);
            if (dd < d) {
                d = dd;
                best = i;
            }
        }
        if (best < 0) {
            return null;
        }
        final int dx = diem[best][0] - x;
        final int dy = diem[best][1] - y;
        final String ns = dy < -1 ? "Bắc" : (dy > 1 ? "Nam" : "");
        final String dt = dx < -1 ? "Tây" : (dx > 1 ? "Đông" : "");
        final String huong = (ns + (ns.isEmpty() || dt.isEmpty() ? "" : " ") + dt).trim();
        return (huong.isEmpty() ? "ngay dưới chân" : "hướng " + huong) + ", khoảng " + d + " bước";
    }

    /** Chi so diem dao trong ban kinh {@code r} o, -1 = khong co. */
    public static int diemTaiCho(final int[][] diem, final int x, final int y, final int r) {
        for (int i = 0; i < diem.length; i++) {
            if (Math.abs(diem[i][0] - x) <= r && Math.abs(diem[i][1] - y) <= r) {
                return i;
            }
        }
        return -1;
    }
}
