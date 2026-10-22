package com.htbb.pokemon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TU DO TRANG TRI (0.9.8): trang phuc (lop phu tren than Pokemon), hao quang, skin thu cuoi, the ban dong hanh. Moi mon
 * co chi so NHO theo O (khong theo mon - thiet ke can bang 4.5, quyet dinh chu game 22/10/2026):
 * <pre>
 *   trang phuc 1,5% | danh hieu 1% (DanhHieuLogic) | hao quang 1% | skin thu cuoi 0,5% | the ban dong hanh 1%  = 5%
 * </pre>
 * Mon mien phi, su kien, thanh tuu, che tao hay mua bang luong o cung mot o cho DUNG ngan sach o do (luong chi mua HINH);
 * nguoi choi chon dong chi so cua o (doi mien phi 1 lan / ngay). Tong trang tri (gom danh hieu dang deo) &lt;= khoa
 * trang_tri.tran_tong_phan_nghin (50): danh hieu giu uu tien, cac o con lai bi cat theo thu tu o.
 *
 * <p>Lop THUAN (test/KiemDoTrangPhuc). Ban keo CSDL / goi -116: real.TrangPhucGame.
 */
public final class TrangPhucLogic {

    public static final int LENH = -116;

    public static final String[] O = {"trang_phuc", "hao_quang", "skin_thu", "the_dong_hanh"};
    public static final String[] TEN_O = {"Trang phục", "Hào quang", "Skin thú cưỡi", "Thẻ bạn đồng hành"};
    public static final int TRANG_PHUC = 0;
    public static final int HAO_QUANG = 1;
    public static final int SKIN_THU = 2;
    public static final int THE_DONG_HANH = 3;
    /** Tran CUNG moi o (phan nghin) - sua data_trang_phuc_o.phan_nghin vuot muc nay cung bi kep. */
    public static final int[] TRAN_O = {15, 10, 5, 10};
    /** Ngan sach danh hieu (DanhHieuLogic.TRAN_MAC_DINH) va tran tong trang tri (trang_tri.tran_tong_phan_nghin). */
    public static final int NGAN_SACH_DANH_HIEU = 10;
    public static final int TRAN_TONG = 50;

    /** Dong chi so mac dinh moi o (data_trang_phuc_o.dong ghi de). */
    public static final String[][] DONG_MAC_DINH = {
        {"hp", "tan_cong", "phong_thu", "thu_dac_biet"},
        {"hp", "tan_cong"},
        {"hp", "phong_thu"},
        {"hp", "tan_cong", "phong_thu"},
    };
    public static final String[] MA_CHI_SO = {"hp", "tan_cong", "phong_thu", "thu_dac_biet"};
    public static final String[] TEN_CHI_SO = {"HP", "Tấn công", "Phòng thủ", "Phòng thủ đặc biệt"};

    /** Doi dong chi so: 1 lan / 24 gio moi o. */
    public static final long KHOANG_DOI_DONG_MS = 24L * 3600L * 1000L;

    /** Nguyen lieu che tao (data_item 1034..1037). */
    public static final int[] ID_NGUYEN_LIEU = {1034, 1035, 1036, 1037};
    public static final String[] TEN_NGUYEN_LIEU = {"Bụi sao", "Mảnh sao", "Mảnh thiên thạch", "Vảy rồng"};

    private TrangPhucLogic() {
    }

    public static int oIdx(final String o) {
        for (int i = 0; i < O.length; i++) {
            if (O[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    public static String tenChiSo(final String ma) {
        for (int i = 0; i < MA_CHI_SO.length; i++) {
            if (MA_CHI_SO[i].equals(ma)) {
                return TEN_CHI_SO[i];
            }
        }
        return ma == null ? "" : ma;
    }

    /** Ngan sach goc cua o {@code i} sau kep tran cung. */
    public static int nganSach(final int i, final int tuCsdl) {
        if (i < 0 || i >= TRAN_O.length) {
            return 0;
        }
        return Math.max(0, Math.min(TRAN_O[i], tuCsdl));
    }

    /**
     * Phan nghin hieu luc tung o: o co mon dang mac (dangMac[i]) duoc nganSach[i] (da kep), cat dan theo thu tu o sao cho
     * danh hieu (uu tien) + tong o &lt;= tranTong.
     */
    public static int[] hieuLuc(final int[] nganSach, final boolean[] dangMac, final int danhHieuPhanNghin, final int tranTong) {
        final int[] r = new int[O.length];
        long con = Math.max(0, Math.min(TRAN_TONG, tranTong)) - Math.max(0, danhHieuPhanNghin);
        for (int i = 0; i < O.length; i++) {
            if (dangMac == null || i >= dangMac.length || !dangMac[i]) {
                continue;
            }
            final int ns = nganSach(i, nganSach != null && i < nganSach.length ? nganSach[i] : TRAN_O[i]);
            final int v = (int) Math.max(0L, Math.min(ns, con));
            r[i] = v;
            con -= v;
        }
        return r;
    }

    /** Tong phan nghin dong {@code chiSo} tu cac o dang chon dong do. */
    public static int phanNghin(final String chiSo, final String[] dongChon, final int[] hieuLuc) {
        int t = 0;
        if (chiSo == null || dongChon == null || hieuLuc == null) {
            return 0;
        }
        for (int i = 0; i < O.length && i < dongChon.length && i < hieuLuc.length; i++) {
            if (chiSo.equals(dongChon[i])) {
                t += hieuLuc[i];
            }
        }
        return t;
    }

    // ================================================================== nguon

    public static final String MIEN_PHI = "mien_phi";
    public static final String SU_KIEN = "su_kien";
    public static final String THANH_TUU = "thanh_tuu";
    public static final String CUA_HANG = "cua_hang";
    public static final String CHE_TAO = "che_tao";
    public static final String NHIEM_VU = "nhiem_vu";

    /** Dieu kien da doc tu (nguon, tham_so). */
    public static final class DieuKien {
        public String loai = "";
        public int cap;
        public final List<String> lich = new ArrayList<String>();
        public int quai;
        public String danhHieu = "";
        /** {id item, so luong}. */
        public final List<int[]> nguyenLieu = new ArrayList<int[]>();
        public int gio;
    }

    /** "cap:5" | "lich:a,b;quai:300" | "danh_hieu:gym_8" | "nl:1034x10,1035x4;gio:2". Phan hong bi bo qua. */
    public static DieuKien doc(final String nguon, final String thamSo) {
        final DieuKien d = new DieuKien();
        d.loai = nguon == null ? "" : nguon.trim();
        if (thamSo == null || thamSo.trim().isEmpty()) {
            return d;
        }
        for (final String phan : thamSo.split(";")) {
            final int i = phan.indexOf(':');
            if (i <= 0) {
                continue;
            }
            final String k = phan.substring(0, i).trim();
            final String v = phan.substring(i + 1).trim();
            try {
                if ("cap".equals(k)) {
                    d.cap = Integer.parseInt(v);
                } else if ("quai".equals(k)) {
                    d.quai = Integer.parseInt(v);
                } else if ("gio".equals(k)) {
                    d.gio = Integer.parseInt(v);
                } else if ("danh_hieu".equals(k)) {
                    d.danhHieu = v;
                } else if ("lich".equals(k)) {
                    // ma LichNam co the chua ':' (moi:valentine) - tach theo ',' tren phan sau "lich:"
                    for (final String m : v.split(",")) {
                        if (!m.trim().isEmpty()) {
                            d.lich.add(m.trim());
                        }
                    }
                } else if ("nl".equals(k)) {
                    for (final String m : v.split(",")) {
                        final int x = m.indexOf('x');
                        if (x > 0) {
                            final int id = Integer.parseInt(m.substring(0, x).trim());
                            final int n = Integer.parseInt(m.substring(x + 1).trim());
                            if (id > 0 && n > 0) {
                                d.nguyenLieu.add(new int[]{id, n});
                            }
                        }
                    }
                }
            } catch (final NumberFormatException e) {
                // bo phan hong
            }
        }
        return d;
    }

    public static String tenNguyenLieu(final int id) {
        for (int i = 0; i < ID_NGUYEN_LIEU.length; i++) {
            if (ID_NGUYEN_LIEU[i] == id) {
                return TEN_NGUYEN_LIEU[i];
            }
        }
        return "vật phẩm #" + id;
    }

    /** "10 Bụi sao, 4 Mảnh sao + 2 giờ xu". */
    public static String chuCheTao(final DieuKien d) {
        final StringBuilder b = new StringBuilder();
        for (final int[] x : d.nguyenLieu) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(x[1]).append(' ').append(tenNguyenLieu(x[0]));
        }
        if (d.gio > 0) {
            b.append(b.length() > 0 ? " + " : "").append(d.gio).append(" giờ xu");
        }
        return b.toString();
    }

    /** Xu che tao = gio x xu moi gio (o cap nguoi choi). */
    public static long xuCheTao(final DieuKien d, final long xuMoiGio) {
        return Math.max(0L, (long) d.gio * Math.max(0L, xuMoiGio));
    }

    /** Khoa bo dem quai su kien. */
    public static String khoaSuKien(final String maMon, final int nam) {
        return "sk:" + maMon + ":" + nam;
    }

    // ================================================================== goi -116

    public static final int S_TU_DO = 0;
    public static final int S_KET_QUA = 1;
    public static final int C_XIN = 0;
    public static final int C_MAC = 1;
    public static final int C_DONG = 2;
    public static final int C_NHAN = 3;
    public static final int C_CHE = 4;
    public static final int C_MUA = 5;

    public static final int TT_CHUA_CO = 0;
    public static final int TT_NHAN_DUOC = 1;
    public static final int TT_DA_CO = 2;
    public static final int TT_DANG_MAC = 3;

    public static final int HD_KHONG = 0;
    public static final int HD_NHAN = 1;
    public static final int HD_CHE = 2;
    public static final int HD_MUA = 3;

    /** Mot o trong goi tu do. */
    public static final class OTuDo {
        public String ma = "";
        public String ten = "";
        public int hieuLuc;
        public int goc;
        public String[] dong = new String[0];
        public int dongChon;
        public String dangMac = "";
        public int giayConDoi;
    }

    /** Mot mon trong goi tu do. */
    public static final class MonTuDo {
        public String ma = "";
        public int oIdx;
        public int so;
        public String ten = "";
        public int doHiem;
        public int icon;
        public int trangThai;
        public String nguon = "";
        public String tienDo = "";
        public int hanhDong;
        public String moTa = "";
    }

    /** Ghi than goi -116 kieu 0 (sau byte kieu). */
    public static void ghiTuDo(final DataOutputStream o, final List<OTuDo> oDs, final int tong, final int tran, final int danhHieu,
            final List<MonTuDo> mon) throws IOException {
        o.writeByte(oDs.size());
        for (final OTuDo x : oDs) {
            o.writeUTF(x.ma);
            o.writeUTF(x.ten);
            o.writeShort(x.hieuLuc);
            o.writeShort(x.goc);
            o.writeByte(x.dong.length);
            for (final String d : x.dong) {
                o.writeUTF(d);
                o.writeUTF(tenChiSo(d));
            }
            o.writeByte(x.dongChon);
            o.writeUTF(x.dangMac == null ? "" : x.dangMac);
            o.writeInt(x.giayConDoi);
        }
        o.writeShort(tong);
        o.writeShort(tran);
        o.writeShort(danhHieu);
        o.writeShort(mon.size());
        for (final MonTuDo m : mon) {
            o.writeUTF(m.ma);
            o.writeByte(m.oIdx);
            o.writeByte(m.so);
            o.writeUTF(m.ten);
            o.writeByte(m.doHiem);
            o.writeShort(m.icon);
            o.writeByte(m.trangThai);
            o.writeUTF(m.nguon);
            o.writeUTF(m.tienDo);
            o.writeByte(m.hanhDong);
            o.writeUTF(m.moTa);
        }
    }
}
