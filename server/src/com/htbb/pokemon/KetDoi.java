package com.htbb.pokemon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Le ket doi (thay he thong "dam cuoi" cua game goc): dieu kien de hai Pokemon nguoi choi KET DOI.
 *
 * Dieu kien theo Pokemon canon: hai LOAI HIEN TAI (Char.pokeDex, tinh LUC DANG KY Le ket doi) phai chung it nhat MOT
 * nhom trung (Egg Group). Nhom "Chưa khám phá" (Undiscovered: Pichu va Pokemon be) khong ket doi voi ai, ke ca cung
 * nhom. Khong co luat Ditto. Them cac dieu kien cu cua game goc (Party.checkConditionMarry trong htbb-core.jar):
 * nhom dung 2 nguoi (ca hai da vao chung nhom = dong y), mot duc mot cai, ca hai chua ket doi, ca hai tu cap 40.
 *
 * Du lieu nhom trung o CSDL (NGUON, sql/sua_ket_doi.sql): {@code data_nhom_trung_ten} (id, ten hien, ket_doi_duoc)
 * va {@code data_nhom_trung} (dex, ten_loai, nhom1, nhom2). test/KiemKetDoi nap bang tu chinh file SQL do.
 *
 * CHI xet luc dang ky: cap da ket doi tu truoc (ke ca cap cu cua he thong dam cuoi) GIU NGUYEN - tien hoa doi loai
 * khong lam mat ket doi, khong co cho nao xet lai. Lop THUAN (tru {@link #nap()}), kiem o test/KiemKetDoi.
 */
public final class KetDoi {

    public static final String BANG_NHOM = "data_nhom_trung_ten";
    public static final String BANG_LOAI = "data_nhom_trung";

    /** Cap toi thieu cua ca hai ben (giu nhu game goc). */
    public static final int CAP_TOI_THIEU = 40;

    /** Char.gender: 1 = duc, 2 = cai (game goc). */
    public static final int DUC = 1;
    public static final int CAI = 2;

    /** Mot nhom trung: ten hien + co ket doi duoc khong (0 = Chua kham pha). */
    public static final class Nhom {
        public final int id;
        public final String ten;
        public final boolean ketDoiDuoc;

        public Nhom(final int id, final String ten, final boolean ketDoiDuoc) {
            this.id = id;
            this.ten = ten;
            this.ketDoiDuoc = ketDoiDuoc;
        }
    }

    /** Mot loai: ten hien + 1..2 nhom trung. */
    public static final class Loai {
        public final int dex;
        public final String ten;
        public final int[] nhom;

        public Loai(final int dex, final String ten, final int[] nhom) {
            this.dex = dex;
            this.ten = ten;
            this.nhom = nhom;
        }
    }

    /** Mot ben xin ket doi (anh xa tu real.Char o real.KetDoiGame). */
    public static final class Ben {
        public final String ten;
        public final int dex;
        public final int gioiTinh;
        public final int cap;
        public final boolean daKetDoi;

        public Ben(final String ten, final int dex, final int gioiTinh, final int cap, final boolean daKetDoi) {
            this.ten = ten;
            this.dex = dex;
            this.gioiTinh = gioiTinh;
            this.cap = cap;
            this.daKetDoi = daKetDoi;
        }
    }

    /** Ket qua xet nhom trung: duoc / khong + cau giai thich cho nguoi choi. */
    public static final class KetQua {
        public final boolean duoc;
        public final List<Integer> nhomChung;
        public final String loiNhan;

        KetQua(final boolean duoc, final List<Integer> nhomChung, final String loiNhan) {
            this.duoc = duoc;
            this.nhomChung = nhomChung;
            this.loiNhan = loiNhan;
        }
    }

    private static volatile Map<Integer, Nhom> bangNhom = Collections.emptyMap();
    private static volatile Map<Integer, Loai> bangLoai = Collections.emptyMap();

    private KetDoi() {
    }

    /** Test / cong cu: dat bang tu ngoai (vd doc tu sql/sua_ket_doi.sql). */
    public static void datBang(final Map<Integer, Nhom> nhom, final Map<Integer, Loai> loai) {
        bangNhom = Collections.unmodifiableMap(new HashMap<Integer, Nhom>(nhom));
        bangLoai = Collections.unmodifiableMap(new HashMap<Integer, Loai>(loai));
    }

    public static boolean daNap() {
        return !bangNhom.isEmpty() && !bangLoai.isEmpty();
    }

    public static Map<Integer, Nhom> bangNhom() {
        return bangNhom;
    }

    public static Map<Integer, Loai> bangLoai() {
        return bangLoai;
    }

    public static Loai loai(final int dex) {
        return bangLoai.get(Integer.valueOf(dex));
    }

    /** Ten loai (CSDL; thieu dong thi lay LoaiPokemon.ten, cuoi cung "Pokémon #dex"). */
    public static String tenLoai(final int dex) {
        final Loai l = loai(dex);
        if (l != null && l.ten != null && !l.ten.isEmpty()) {
            return l.ten;
        }
        final String t = LoaiPokemon.ten(dex);
        return t.isEmpty() ? "Pokémon #" + dex : t;
    }

    public static String tenNhom(final int id) {
        final Nhom n = bangNhom.get(Integer.valueOf(id));
        return n == null ? "?" : n.ten;
    }

    /** "Charmander (Quái vật, Rồng)". */
    public static String moTaLoai(final int dex) {
        final StringBuilder sb = new StringBuilder(tenLoai(dex)).append(" (");
        final Loai l = loai(dex);
        if (l == null || l.nhom.length == 0) {
            sb.append("chưa rõ nhóm trứng");
        } else {
            for (int i = 0; i < l.nhom.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(tenNhom(l.nhom[i]));
            }
        }
        return sb.append(')').toString();
    }

    private static boolean ketDoiDuoc(final Loai l) {
        if (l == null || l.nhom.length == 0) {
            return false;
        }
        for (final int id : l.nhom) {
            final Nhom n = bangNhom.get(Integer.valueOf(id));
            if (n == null || !n.ketDoiDuoc) {
                return false;
            }
        }
        return true;
    }

    /**
     * Xet nhom trung cua hai loai. Cau tra ve luon neu ro ca hai loai va nhom, vd
     * "Charmander (Quái vật, Rồng) và Squirtle (Quái vật, Nước 1) cùng nhóm Quái vật - có thể kết đôi."
     */
    public static KetQua xet(final int dexA, final int dexB) {
        final List<Integer> chung = new ArrayList<Integer>();
        if (!daNap()) {
            return new KetQua(false, chung, "Chưa nạp được dữ liệu nhóm trứng, hãy thử lại sau ít phút.");
        }
        final Loai a = loai(dexA);
        final Loai b = loai(dexB);
        final String moTa = moTaLoai(dexA) + " và " + moTaLoai(dexB);
        if (a == null || b == null) {
            return new KetQua(false, chung, moTa + ": chưa có dữ liệu nhóm trứng - chưa thể kết đôi.");
        }
        for (final Loai x : new Loai[]{a, b}) {
            if (!ketDoiDuoc(x)) {
                final int sau = LoaiPokemon.tienHoaKeTiep(x.dex, Integer.MAX_VALUE);
                return new KetQua(false, chung, moTa + ": " + x.ten + " thuộc nhóm " + tenNhomKhongKetDoi(x)
                        + ", không thể kết đôi" + (sau != LoaiPokemon.KHONG
                        ? " - hãy tiến hoá thành " + tenLoai(sau) + " trước." : "."));
            }
        }
        for (final int na : a.nhom) {
            for (final int nb : b.nhom) {
                if (na == nb && !chung.contains(Integer.valueOf(na))) {
                    chung.add(Integer.valueOf(na));
                }
            }
        }
        if (chung.isEmpty()) {
            return new KetQua(false, chung, moTa + " không chung nhóm trứng nào - chưa thể kết đôi.");
        }
        final StringBuilder sb = new StringBuilder(moTa).append(" cùng nhóm ");
        for (int i = 0; i < chung.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(tenNhom(chung.get(i).intValue()));
        }
        return new KetQua(true, chung, sb.append(" - có thể kết đôi.").toString());
    }

    private static String tenNhomKhongKetDoi(final Loai l) {
        for (final int id : l.nhom) {
            final Nhom n = bangNhom.get(Integer.valueOf(id));
            if (n == null || !n.ketDoiDuoc) {
                return n == null ? "chưa rõ" : n.ten;
            }
        }
        return "?";
    }

    /**
     * Xet du dieu kien dang ky Le ket doi (CHUA tinh xu / luong: real.KetDoiGame xet sau). {@code nguoiDangKy} la
     * nguoi noi chuyen voi NPC, {@code nhom} la danh sach nhom hien tai (null = khong co nhom).
     * Tra ve null neu du dieu kien, nguoc lai la ly do tu choi (chu nguoi choi thay).
     */
    public static String lyDoTuChoi(final Ben nguoiDangKy, final List<Ben> nhom) {
        if (nguoiDangKy.daKetDoi) {
            return "Bạn đã có bạn kết đôi, không thể đăng ký Lễ kết đôi mới.";
        }
        if (nhom == null || nhom.size() != 2) {
            return "Hãy lập nhóm đúng 2 thành viên với Pokémon muốn kết đôi (cả hai vào chung nhóm là đồng ý).";
        }
        final Ben ban = nhom.get(0).ten.equals(nguoiDangKy.ten) ? nhom.get(1) : nhom.get(0);
        if (ban.daKetDoi) {
            return ban.ten + " đã có bạn kết đôi.";
        }
        if (nguoiDangKy.gioiTinh == ban.gioiTinh) {
            return "Lễ kết đôi cần một Pokémon đực và một Pokémon cái.";
        }
        if (nguoiDangKy.gioiTinh != DUC) {
            return "Pokémon đực trong nhóm đứng ra đăng ký Lễ kết đôi.";
        }
        if (nguoiDangKy.cap < CAP_TOI_THIEU || ban.cap < CAP_TOI_THIEU) {
            return "Cả hai cần từ cấp " + CAP_TOI_THIEU + " trở lên để kết đôi.";
        }
        final KetQua kq = xet(nguoiDangKy.dex, ban.dex);
        return kq.duoc ? null : kq.loiNhan;
    }

    // ------------------------------------------------------------------ nap CSDL

    /** Doc hai bang tu CSDL. false = thieu bang / rong / loi (da in loi), bang cu giu nguyen. */
    public static synchronized boolean nap() {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = data.Database.instance.getConnection();
            final Map<Integer, Nhom> nhom = new HashMap<Integer, Nhom>();
            p = c.prepareStatement("SELECT id, ten, ket_doi_duoc FROM " + BANG_NHOM);
            rs = p.executeQuery();
            while (rs.next()) {
                nhom.put(Integer.valueOf(rs.getInt(1)), new Nhom(rs.getInt(1), rs.getString(2).trim(), rs.getInt(3) != 0));
            }
            rs.close();
            p.close();
            final Map<Integer, Loai> loai = new HashMap<Integer, Loai>();
            p = c.prepareStatement("SELECT dex, ten_loai, nhom1, nhom2 FROM " + BANG_LOAI);
            rs = p.executeQuery();
            while (rs.next()) {
                final int n2 = rs.getInt(4);
                final int[] ds = rs.wasNull() || n2 <= 0 ? new int[]{rs.getInt(3)} : new int[]{rs.getInt(3), n2};
                loai.put(Integer.valueOf(rs.getInt(1)), new Loai(rs.getInt(1), rs.getString(2).trim(), ds));
            }
            if (nhom.isEmpty() || loai.isEmpty()) {
                System.out.println("[KET DOI] LOI: bang " + BANG_NHOM + " / " + BANG_LOAI
                        + " rong - chay sql/sua_ket_doi.sql");
                return false;
            }
            datBang(nhom, loai);
            System.out.println("[KET DOI] da nap " + nhom.size() + " nhom trung, " + loai.size() + " loai");
            return true;
        } catch (final Exception e) {
            System.out.println("[KET DOI] LOI: khong doc duoc nhom trung (chay sql/sua_ket_doi.sql): " + e);
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (final Exception ignored) {
            }
            try {
                if (p != null) {
                    p.close();
                }
            } catch (final Exception ignored) {
            }
            try {
                if (c != null) {
                    data.Database.connPool.free(c);
                }
            } catch (final Exception ignored) {
            }
        }
    }
}
