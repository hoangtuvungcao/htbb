package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ho so he sinh thai cua MOT nhan vat (luu 1 dong tob_hst_nhan_vat.du_lieu). Lop THUAN.
 * Chuoi hoa: cac muc "khoa=gia tri" noi bang '|', gia tri chi dung so va ',', ':', '.', '~'
 * (khong co chu nguoi choi nhap) nen khong can thoat ky tu.
 *
 * Moi phuong thuc doi trang thai deu synchronized tren doi tuong (luong ban do + luong menu +
 * luong dinh ky cung cham).
 */
public final class HoSoSinhThai {

    public static final String PHIEN_BAN = "v1";

    public int ngay;                 // yyyyMMdd lan dat lai the luc / bo dem ngay
    public int theLuc = HeSinhThai.THE_LUC_TOI_DA;
    public final long[] expKyNang = new long[KyNangSong.SO_KY_NANG];
    /** khoa (ma*10+bac) -> so luong. */
    public final TreeMap<Integer, Integer> tui = new TreeMap<Integer, Integer>();
    public final List<NhaTre.Trung> trung = new ArrayList<NhaTre.Trung>();
    /** Nha tre: dex 2 pet dang gui (0 = trong), buoc tich luy, trung da de hom nay, ngay da tra phi, dem shiny. */
    public int nhaTre1, nhaTre2, buocNhaTre, deHomNay, ngayPhiNhaTre, demKhongShiny;
    /** Dong giong: dex goc -> IV tot nhat da no (3 so). */
    public final Map<Integer, int[]> giong = new HashMap<Integer, int[]>();
    /** Nghien cuu: dex -> mat na nhiem vu da lam. */
    public final TreeMap<Integer, Integer> nghienCuu = new TreeMap<Integer, Integer>();
    public final List<String> album = new ArrayList<String>();
    /** Bit vung sinh thai da hoan thanh. */
    public int vungXong;
    /** Bo dem trong ngay. */
    public long banNpcHomNay;
    public int meThuocHomNay, banhPokeHomNay, tuoiNhanHomNay, tuoiChoHomNay;
    /** Buff: so lan con lai. */
    public int buffMoiThom, buffSup;
    /** Do trang tri dat trong vuon: ma -> so. */
    public final TreeMap<Integer, Integer> trangTri = new TreeMap<Integer, Integer>();
    /** Luot tra loi do vui dung / lan thang dua / moc album da nhan (bit). */
    public int doVuiDung, duaThang, mocAlbum;
    /** Diem than thien (tuoi vuon ban be). */
    public int thanThien;
    /** Danh hieu chu (khong chi so) da mo, noi bang '~'. */
    public final List<String> danhHieu = new ArrayList<String>();
    /**
     * Vuon berry (VuonBerry, bat lai 29/10/2026): o i dang trong berry ma {@code vuonMa[i]} (0 = trong), bac
     * {@code vuonBac[i]}, gieo luc {@code vuonGieo[i]} (ms, DongHo.bay() - dong ho MAY CHU). Cung dong voi tui nen gieo
     * (tru xu) / thu hoach (cong berry vao tui) ghi chung mot lan: khong the nhan doi khi dang nhap lai / rot mang.
     */
    public final int[] vuonMa = new int[VuonBerry.O_TOI_DA];
    public final int[] vuonBac = new int[VuonBerry.O_TOI_DA];
    public final long[] vuonGieo = new long[VuonBerry.O_TOI_DA];
    /** So o dat da mua them (0..VuonBerry.O_MUA_TOI_DA) / so lan thu hoach hom nay (tran VuonBerry.THU_TOI_DA_NGAY). */
    public int vuonDaMua, thuVuonHomNay;

    // ------------------------------------------------------------------ ngay moi

    /** Dat lai bo dem khi sang ngay moi (the luc tich luy theo KyNangSong.theLucNgayMoi). */
    public synchronized boolean sangNgay(final int homNay) {
        if (homNay == ngay) {
            return false;
        }
        final int cach = ngay <= 0 ? 2 : NangLuong.cachNgay(ngay, homNay);
        theLuc = KyNangSong.theLucNgayMoi(theLuc, cach);
        ngay = homNay;
        banNpcHomNay = 0;
        meThuocHomNay = 0;
        banhPokeHomNay = 0;
        tuoiNhanHomNay = 0;
        tuoiChoHomNay = 0;
        deHomNay = 0;
        thuVuonHomNay = 0;
        return true;
    }

    public synchronized boolean truTheLuc(final int n) {
        if (theLuc < n) {
            return false;
        }
        theLuc -= n;
        return true;
    }

    public synchronized void congTheLuc(final int n) {
        theLuc = Math.min(HeSinhThai.THE_LUC_TOI_DA + 60, theLuc + Math.max(0, n));
    }

    // ------------------------------------------------------------------ ky nang

    public synchronized int capKyNang(final int kn) {
        return KyNangSong.capTuExp(expKyNang[kn]);
    }

    /** Cong EXP, tra ve cap moi neu vua len cap (nguoc lai 0). */
    public synchronized int congExp(final int kn, final int exp) {
        final int truoc = KyNangSong.capTuExp(expKyNang[kn]);
        int e = exp;
        if (buffSup > 0) {
            buffSup--;
            e = e * 3 / 2;
        }
        expKyNang[kn] += Math.max(0, e);
        final int sau = KyNangSong.capTuExp(expKyNang[kn]);
        return sau > truoc ? sau : 0;
    }

    // ------------------------------------------------------------------ tui

    public synchronized int so(final int khoa) {
        final Integer v = tui.get(khoa);
        return v == null ? 0 : v.intValue();
    }

    public synchronized void them(final int khoa, final int n) {
        if (n <= 0) {
            return;
        }
        tui.put(khoa, so(khoa) + n);
    }

    public synchronized boolean bot(final int khoa, final int n) {
        final int c = so(khoa);
        if (n <= 0 || c < n) {
            return false;
        }
        if (c == n) {
            tui.remove(khoa);
        } else {
            tui.put(khoa, c - n);
        }
        return true;
    }

    /** Tong so cua ma (moi bac). */
    public synchronized int tongMa(final int ma) {
        int s = 0;
        for (final Map.Entry<Integer, Integer> e : tui.entrySet()) {
            if (VatLieuSinhThai.maCuaKhoa(e.getKey()) == ma) {
                s += e.getValue();
            }
        }
        return s;
    }

    /**
     * Lay {@code n} cai cua ma, uu tien bac THAP truoc. Tra ve bac thap nhat da lay (0 = khong du,
     * khong lay gi).
     */
    public synchronized int layTheoMa(final int ma, final int n) {
        if (tongMa(ma) < n) {
            return 0;
        }
        int con = n;
        int bacThap = 0;
        for (int bac = 1; bac <= 5 && con > 0; bac++) {
            final int k = VatLieuSinhThai.khoa(ma, bac);
            final int c = so(k);
            if (c > 0) {
                final int lay = Math.min(c, con);
                bot(k, lay);
                con -= lay;
                if (bacThap == 0) {
                    bacThap = bac;
                }
            }
        }
        return bacThap;
    }

    // ------------------------------------------------------------------ nghien cuu / album

    /** Danh dau nhiem vu nghien cuu; tra ve true neu MOI lam lan dau. */
    public synchronized boolean ghiNghienCuu(final int dex, final int bit) {
        if (dex <= 0) {
            return false;
        }
        final Integer c = nghienCuu.get(dex);
        final int cu = c == null ? 0 : c.intValue();
        if ((cu & bit) != 0) {
            return false;
        }
        nghienCuu.put(dex, cu | bit);
        return true;
    }

    public synchronized int nghienCuu(final int dex) {
        final Integer c = nghienCuu.get(dex);
        return c == null ? 0 : c.intValue();
    }

    /** Them anh album; tra so anh sau khi them (khong them neu da day). */
    public synchronized int themAnh(final String a) {
        if (album.size() < SuuTamSinhThai.ALBUM_TOI_DA) {
            album.add(a);
        }
        return album.size();
    }

    // ------------------------------------------------------------------ chuoi hoa

    public synchronized String chuoi() {
        final Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("ng", String.valueOf(ngay));
        m.put("tl", String.valueOf(theLuc));
        final StringBuilder kn = new StringBuilder();
        for (int i = 0; i < expKyNang.length; i++) {
            kn.append(i == 0 ? "" : ",").append(expKyNang[i]);
        }
        m.put("kn", kn.toString());
        m.put("tui", banDo(tui));
        final StringBuilder tr = new StringBuilder();
        for (final NhaTre.Trung t : trung) {
            tr.append(tr.length() == 0 ? "" : ",").append(t.chuoi());
        }
        m.put("tr", tr.toString());
        m.put("nt", nhaTre1 + "," + nhaTre2 + "," + buocNhaTre + "," + deHomNay + "," + ngayPhiNhaTre + "," + demKhongShiny);
        final StringBuilder gi = new StringBuilder();
        for (final Map.Entry<Integer, int[]> e : new TreeMap<Integer, int[]>(giong).entrySet()) {
            gi.append(gi.length() == 0 ? "" : ",").append(e.getKey()).append(':').append(e.getValue()[0]).append('.')
                    .append(e.getValue()[1]).append('.').append(e.getValue()[2]);
        }
        m.put("gi", gi.toString());
        m.put("nc", banDo(nghienCuu));
        m.put("al", String.join("~", album));
        m.put("vx", String.valueOf(vungXong));
        m.put("dem", banNpcHomNay + "," + meThuocHomNay + "," + banhPokeHomNay + "," + tuoiNhanHomNay + "," + tuoiChoHomNay);
        m.put("bf", buffMoiThom + "," + buffSup);
        m.put("tt", banDo(trangTri));
        m.put("mg", doVuiDung + "," + duaThang + "," + mocAlbum + "," + thanThien);
        final StringBuilder dh = new StringBuilder();
        for (final String s : danhHieu) {
            dh.append(dh.length() == 0 ? "" : "~").append(maDanhHieu(s));
        }
        m.put("dh", dh.toString());
        final StringBuilder vu = new StringBuilder();
        for (int i = 0; i < vuonMa.length; i++) {
            vu.append(i == 0 ? "" : ",").append(vuonMa[i]).append('.').append(vuonBac[i]).append('.').append(vuonGieo[i]);
        }
        m.put("vu", vu.toString());
        m.put("vd", vuonDaMua + "," + thuVuonHomNay);
        final StringBuilder b = new StringBuilder(PHIEN_BAN);
        for (final Map.Entry<String, String> e : m.entrySet()) {
            b.append('|').append(e.getKey()).append('=').append(e.getValue());
        }
        return b.toString();
    }

    /** Danh hieu luu bang chi so trong DANH_HIEU (khong luu chu co dau). */
    static String maDanhHieu(final String ten) {
        for (int i = 0; i < DANH_HIEU.length; i++) {
            if (DANH_HIEU[i].equals(ten)) {
                return String.valueOf(i);
            }
        }
        return "";
    }

    /** Moi danh hieu chu cua he sinh thai (chi so luu trong ho so). Them cuoi, khong doi thu tu. */
    public static final String[] DANH_HIEU = {
        "Nhiếp ảnh tập sự", "Thợ săn khoảnh khắc", "Nhà sinh thái học", "Huyền thoại ống kính",
        "Ngư phủ bậc thầy", "Nhà khảo cổ", "Đầu bếp Poké", "Dược sư", "Nhà vườn xanh",
        "Nhà nhân giống", "Thợ săn shiny", "Vua tốc độ", "Thánh đố Pokémon", "Nhà vô địch giải tuần",
        "Nhà thám hiểm vùng", "Bạn của muôn loài",
    };

    public synchronized boolean coDanhHieu(final String ten) {
        return danhHieu.contains(ten);
    }

    public synchronized boolean themDanhHieu(final String ten) {
        if (maDanhHieu(ten).isEmpty() || danhHieu.contains(ten)) {
            return false;
        }
        danhHieu.add(ten);
        return true;
    }

    static String banDo(final Map<Integer, Integer> m) {
        final StringBuilder b = new StringBuilder();
        for (final Map.Entry<Integer, Integer> e : m.entrySet()) {
            b.append(b.length() == 0 ? "" : ",").append(e.getKey()).append(':').append(e.getValue());
        }
        return b.toString();
    }

    static void docBanDo(final String s, final Map<Integer, Integer> m) {
        if (s == null || s.isEmpty()) {
            return;
        }
        for (final String p : s.split(",")) {
            final int i = p.indexOf(':');
            if (i > 0) {
                try {
                    final int k = Integer.parseInt(p.substring(0, i));
                    final int v = Integer.parseInt(p.substring(i + 1));
                    if (v > 0) {
                        m.put(k, v);
                    }
                } catch (final NumberFormatException ignored) {
                }
            }
        }
    }

    static long[] soLong(final String s, final int n) {
        final long[] r = new long[n];
        if (s == null || s.isEmpty()) {
            return r;
        }
        final String[] a = s.split(",");
        for (int i = 0; i < n && i < a.length; i++) {
            try {
                r[i] = Long.parseLong(a[i].trim());
            } catch (final NumberFormatException ignored) {
            }
        }
        return r;
    }

    /** Doc chuoi; chuoi hong / rong -> ho so moi (khong nem loi). */
    public static HoSoSinhThai doc(final String s) {
        final HoSoSinhThai h = new HoSoSinhThai();
        if (s == null || !s.startsWith(PHIEN_BAN)) {
            return h;
        }
        final Map<String, String> m = new HashMap<String, String>();
        for (final String p : s.split("\\|")) {
            final int i = p.indexOf('=');
            if (i > 0) {
                m.put(p.substring(0, i), p.substring(i + 1));
            }
        }
        h.ngay = (int) soLong(m.get("ng"), 1)[0];
        h.theLuc = m.containsKey("tl") ? (int) soLong(m.get("tl"), 1)[0] : HeSinhThai.THE_LUC_TOI_DA;
        final long[] kn = soLong(m.get("kn"), KyNangSong.SO_KY_NANG);
        System.arraycopy(kn, 0, h.expKyNang, 0, kn.length);
        docBanDo(m.get("tui"), h.tui);
        final String tr = m.get("tr");
        if (tr != null && !tr.isEmpty()) {
            for (final String t : tr.split(",")) {
                final NhaTre.Trung x = NhaTre.Trung.doc(t);
                if (x != null && h.trung.size() < HeSinhThai.TRUNG_TOI_DA_TUI) {
                    h.trung.add(x);
                }
            }
        }
        final long[] nt = soLong(m.get("nt"), 6);
        h.nhaTre1 = (int) nt[0];
        h.nhaTre2 = (int) nt[1];
        h.buocNhaTre = (int) nt[2];
        h.deHomNay = (int) nt[3];
        h.ngayPhiNhaTre = (int) nt[4];
        h.demKhongShiny = (int) nt[5];
        final String gi = m.get("gi");
        if (gi != null && !gi.isEmpty()) {
            for (final String g : gi.split(",")) {
                final int i = g.indexOf(':');
                if (i > 0) {
                    final String[] iv = g.substring(i + 1).split("\\.");
                    if (iv.length == 3) {
                        try {
                            h.giong.put(Integer.parseInt(g.substring(0, i)), new int[]{NhaTre.kep(Integer.parseInt(iv[0])),
                                NhaTre.kep(Integer.parseInt(iv[1])), NhaTre.kep(Integer.parseInt(iv[2]))});
                        } catch (final NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
        docBanDo(m.get("nc"), h.nghienCuu);
        final String al = m.get("al");
        if (al != null && !al.isEmpty()) {
            for (final String a : al.split("~")) {
                if (!a.isEmpty() && h.album.size() < SuuTamSinhThai.ALBUM_TOI_DA) {
                    h.album.add(a);
                }
            }
        }
        h.vungXong = (int) soLong(m.get("vx"), 1)[0];
        final long[] dem = soLong(m.get("dem"), 5);
        h.banNpcHomNay = dem[0];
        h.meThuocHomNay = (int) dem[1];
        h.banhPokeHomNay = (int) dem[2];
        h.tuoiNhanHomNay = (int) dem[3];
        h.tuoiChoHomNay = (int) dem[4];
        final long[] bf = soLong(m.get("bf"), 2);
        h.buffMoiThom = (int) bf[0];
        h.buffSup = (int) bf[1];
        docBanDo(m.get("tt"), h.trangTri);
        final long[] mg = soLong(m.get("mg"), 4);
        h.doVuiDung = (int) mg[0];
        h.duaThang = (int) mg[1];
        h.mocAlbum = (int) mg[2];
        h.thanThien = (int) mg[3];
        final String dh = m.get("dh");
        if (dh != null && !dh.isEmpty()) {
            for (final String d : dh.split("~")) {
                try {
                    final int i = Integer.parseInt(d);
                    if (i >= 0 && i < DANH_HIEU.length && !h.danhHieu.contains(DANH_HIEU[i])) {
                        h.danhHieu.add(DANH_HIEU[i]);
                    }
                } catch (final NumberFormatException ignored) {
                }
            }
        }
        final String vu = m.get("vu");
        if (vu != null && !vu.isEmpty()) {
            final String[] o = vu.split(",");
            for (int i = 0; i < o.length && i < h.vuonMa.length; i++) {
                final String[] x = o[i].split("\\.");
                if (x.length == 3) {
                    try {
                        final int ma = Integer.parseInt(x[0]);
                        final int bac = Integer.parseInt(x[1]);
                        final long gieo = Long.parseLong(x[2]);
                        if (VuonBerry.chiSo(ma) >= 0) {
                            h.vuonMa[i] = ma;
                            h.vuonBac[i] = Math.max(1, Math.min(5, bac));
                            h.vuonGieo[i] = gieo;
                        }
                    } catch (final NumberFormatException ignored) {
                    }
                }
            }
        }
        final long[] vd = soLong(m.get("vd"), 2);
        h.vuonDaMua = (int) Math.max(0, Math.min(VuonBerry.O_MUA_TOI_DA, vd[0]));
        h.thuVuonHomNay = (int) Math.max(0, vd[1]);
        return h;
    }
}
