package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * Lich su kien NAM theo ngay that (chu game duyet 19/10/2026): moi su kien tu bat / tat dung cua so cua no theo gio
 * Viet Nam (Asia/Ho_Chi_Minh), khong con theo tuan ISO co dinh (lich cu SuKienLauDai.LICH lech Tet / Trung Thu /
 * Noel moi nam). Le am lich (Tet Nguyen Dan, Gio To, Vu Lan, Trung Thu) tinh bang {@link AmLich}.
 *
 * MOI MOC DOI CHUYEN LUC {@link #GIO_CHUYEN}:00 - ngay truoc gio bao tri hang ngay (ngau nhien 04:30-05:30,
 * real/BaoTriDaily). Su kien chi doi hieu luc sau khi khoi dong lai (SuKien), nen moc 04:00 de LichSuKien hoan
 * viec khoi dong lai toi luc bao tri: KHONG them lan khoi dong lai nao ngoai bao tri hang ngay.
 *
 * Ngay cong dong (moi:congdong, 0.9.7): MOT ngay moi thang (NgayCongDong.ngay - Chu nhat cua tuan cong dong ISO hoac
 * Chu nhat thu ba), 04:00 -> 04:00; mo ta tung lan = dong noi bat cua thang (CuaSo.moTa, data_ngay_cong_dong).
 *
 * Tuan Pokemon (tuan he, shiny, dot kich, mua PvP, Pokedex, chien vung, bung phat) van theo tuan ISO
 * (HE_TUAN / DOT_KICH gan voi so tuan) - lay tu SuKienLauDai.LICH, thu Hai 04:00 -> thu Hai tuan sau 04:00.
 *
 * Lop THUAN: test/KiemLichNam.java. Ban keo noi CSDL + web: real/LichNamGame, bang tob_su_kien_lich.
 * Bang ra web: /api/su-kien (trang chu, cam nang), tin tu dong (web/server/plugins/tin-su-kien.ts).
 */
public final class LichNam {

    public static final TimeZone MUI_GIO = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
    /** Gio doi trang thai su kien (gio Viet Nam). */
    public static final int GIO_CHUYEN = 4;

    static final int DUONG = 0;
    static final int AM = 1;
    static final int THU_SAU_DEN = 2; // Black Friday: thu Sau thu 4 cua thang 11
    static final int TUAN_ISO = 3;
    /** Ngay cong dong: MOT ngay moi thang (NgayCongDong.ngay), lan = thang - 1. */
    static final int THANG = 4;

    /** Mot su kien trong lich nam. */
    public static final class Muc {
        public final String ma;
        /** Ten hien tren web / thong bao. */
        public final String ten;
        public final String nhom;
        final int kieu;
        /** Thang / ngay moc (am lich neu kieu AM). */
        final int thang;
        final int ngay;
        /** Bat dau = moc + lechDau ngay; dai soNgay ngay. */
        final int lechDau;
        final int soNgay;
        /** Ngay le that (hien tren web), vd "31/10", "15/8 âm lịch". */
        public final String ngayLe;
        /** Mo ta cho nguoi choi. */
        public final String moTa;
        /** Phan thuong noi bat. */
        public final String phanThuong;

        Muc(final String ma, final String ten, final String nhom, final int kieu, final int thang, final int ngay,
                final int lechDau, final int soNgay, final String ngayLe, final String moTa, final String phanThuong) {
            this.ma = ma;
            this.ten = ten;
            this.nhom = nhom;
            this.kieu = kieu;
            this.thang = thang;
            this.ngay = ngay;
            this.lechDau = lechDau;
            this.soNgay = soNgay;
            this.ngayLe = ngayLe;
            this.moTa = moTa;
            this.phanThuong = phanThuong;
        }

        public boolean laTuanPokemon() {
            return kieu == TUAN_ISO;
        }
    }

    private static Muc duong(final String ma, final String ten, final int thang, final int ngay, final int lechDau,
            final int soNgay, final String ngayLe, final String moTa, final String thuong) {
        return new Muc(ma, ten, "Lễ hội", DUONG, thang, ngay, lechDau, soNgay, ngayLe, moTa, thuong);
    }

    private static Muc am(final String ma, final String ten, final int thang, final int ngay, final int lechDau,
            final int soNgay, final String ngayLe, final String moTa, final String thuong) {
        return new Muc(ma, ten, "Lễ hội", AM, thang, ngay, lechDau, soNgay, ngayLe, moTa, thuong);
    }

    private static Muc tuan(final String ma, final String ten, final String moTa, final String thuong) {
        return new Muc(ma, ten, "Tuần Pokémon", TUAN_ISO, 0, 0, 0, 7, "theo tuần", moTa, thuong);
    }

    /** Thu tu = thu tu trong nam (hien tren web). */
    public static final Muc[] DANH_MUC = {
        duong("tetduonglich", "Tết Dương lịch", 1, 1, -2, 5, "1/1",
                "Đón năm mới cùng NPC Cây thông ở các làng: gom kẹo năm mới đổi quà may mắn.",
                "Quà năm mới ngẫu nhiên, kẹo năm mới"),
        am("tet2017", "Tết Nguyên Đán", 1, 1, -7, 17, "mùng 1 tháng Giêng âm lịch",
                "Không khí Tết khắp vùng: NPC lì xì, nấu bánh chưng, cây lì xì và bảng xếp hạng Tết.",
                "Lì xì lượng khoá, bánh chưng, quà top Tết"),
        am("tet123", "Ba ngày Tết", 1, 1, 0, 3, "mùng 1-2-3 Tết",
                "Thưởng riêng ba ngày mùng 1, mùng 2, mùng 3 Tết cho mọi huấn luyện viên đăng nhập.",
                "Quà đăng nhập mùng 1-2-3"),
        duong("moi:valentine", "Valentine", 2, 14, -3, 7, "14/2",
                "Luvdisc xuất hiện; cặp đôi cùng đội săn quà Valentine.",
                "Quà Valentine (vật phẩm khoá)"),
        duong("quocte83", "Quốc tế Phụ nữ 8/3", 3, 8, -3, 7, "8/3",
                "Tặng hoa, nhận quà 8/3 ở NPC sự kiện.",
                "Hoa hồng, quà 8/3"),
        am("gioto", "Giỗ Tổ Hùng Vương", 3, 10, -3, 7, "10/3 âm lịch",
                "Bánh giày, bắn pháo hoa, đổi nguyên liệu và bảng xếp hạng bắn pháo.",
                "Bánh giày, nguyên liệu, quà top bắn pháo"),
        duong("moi:3004", "Lễ 30/4 - 1/5", 4, 30, -2, 6, "30/4 - 1/5",
                "Mừng ngày thống nhất và Quốc tế Lao động: nhiệm vụ tuyến khắp các vùng.",
                "Thưởng nhiệm vụ tuyến"),
        duong("moi:thieunhi", "Quốc tế Thiếu nhi 1/6", 6, 1, -2, 7, "1/6",
                "Quà thiếu nhi cho mọi huấn luyện viên.",
                "Quà thiếu nhi (vật phẩm khoá, có hạn)"),
        duong("he2017", "Sự kiện Hè", 6, 15, 0, 30, "mùa hè",
                "Kem, dưa hấu, socola và NPC mùa hè; boss Người tuyết quay lại.",
                "Vật phẩm hè, quà đổi ở NPC mùa hè"),
        duong("moi:hamngucmua", "Hầm ngục bí ẩn mùa hè", 7, 15, 0, 14, "giữa tháng 7",
                "Hầm ngục bí ẩn phiên bản mùa hè.",
                "Thưởng tầng hầm ngục"),
        am("vulan", "Vu Lan", 7, 15, -4, 7, "15/7 âm lịch",
                "Nhánh hoa hồng vàng rơi khi thu hoạch, chế đồ, săn boss; NPC Đấu Trường đổi bó hoa.",
                "Bó hoa vàng / đỏ, nguyên liệu, Mặt nạ Snorlax"),
        duong("moi:quockhanh", "Quốc khánh 2/9", 9, 2, -3, 6, "2/9",
                "Nhiệm vụ tuyến toàn quốc mừng Quốc khánh.",
                "Thưởng nhiệm vụ tuyến"),
        am("trungthu", "Trung Thu", 8, 15, -10, 14, "15/8 âm lịch",
                "Bánh nướng, bánh dẻo, lồng đèn, NPC Clefairy và thả đèn.",
                "Bánh Trung Thu, lồng đèn, quà thả đèn"),
        duong("moi:2010", "Phụ nữ Việt Nam 20/10", 10, 20, -3, 6, "20/10",
                "Nhiệm vụ tặng quà mừng ngày Phụ nữ Việt Nam.",
                "Thưởng nhiệm vụ tặng quà"),
        duong("halloween", "Halloween", 10, 31, -13, 16, "31/10",
                "Bí ngô, mặt nạ, hộp quà Halloween và boss Darkrai.",
                "Hộp quà Halloween, mặt nạ, bí ngô"),
        duong("demhalloween", "Đêm Halloween", 10, 31, 0, 1, "31/10",
                "Thưởng riêng ngày 31/10.",
                "Quà đêm Halloween"),
        duong("sinhnhat2011", "Ngày Nhà giáo Việt Nam 20/11", 11, 20, -3, 7, "20/11",
                "Lồng đèn hoa sen, chổi băng / độc / điện ở NPC sự kiện.",
                "Lồng đèn hoa sen, chổi sự kiện"),
        new Muc("blackfriday", "Black Friday", "Lễ hội", THU_SAU_DEN, 11, 4, -1, 5, "thứ Sáu thứ tư của tháng 11",
                "Giảm giá cửa hàng dịp Black Friday.", "Giảm giá cửa hàng"),
        new Muc("blackfridaydaugia", "Black Friday: đấu giá", "Lễ hội", THU_SAU_DEN, 11, 4, -1, 5,
                "thứ Sáu thứ tư của tháng 11", "Phiên đấu giá chổi dịp Black Friday.", "Chổi đấu giá"),
        new Muc("blackfridayban", "Black Friday: mở bán", "Lễ hội", THU_SAU_DEN, 11, 4, -1, 5,
                "thứ Sáu thứ tư của tháng 11", "Mở bán các món giới hạn dịp Black Friday.", "Món giới hạn"),
        duong("noel", "Giáng sinh (Noel)", 12, 24, -6, 10, "24-25/12",
                "NPC Noel, quà Giáng sinh và Stantler kéo xe.",
                "Quà Giáng sinh"),
        duong("demnoel", "Đêm Noel", 12, 24, 0, 2, "24/12",
                "Thưởng riêng đêm 24/12 và ngày 25/12.",
                "Quà đêm Noel"),
        duong("moi:tongket", "Tổng kết năm", 12, 28, 0, 7, "cuối năm",
                "Tổng kết năm và chốt mùa giải PvP.",
                "Thưởng mùa PvP, danh hiệu"),
        tuan("moi:tuanhe", "Tuần hệ", "Chiêu của hệ trong tuần mạnh hơn 10%.", "+10% sát thương chiêu hệ của tuần"),
        new Muc(NgayCongDong.MA_SU_KIEN, "Ngày cộng đồng", "Ngày cộng đồng", THANG, 0, 0, 0, 1, "Chủ nhật hằng tháng",
                NgayCongDong.moTa(null), NgayCongDong.phanThuong(null)),
        tuan("moi:shiny", "Tuần Shiny", "Tỉ lệ shiny x10, bảo đảm sau 2.000 lần hạ.", "Shiny x10"),
        tuan("moi:dotkich", "Đột kích huyền thoại", "Boss thế giới theo lịch, thưởng theo % đóng góp.",
                "Thưởng theo đóng góp"),
        tuan("moi:muapvp", "Kết thúc mùa PvP", "Chốt bậc mùa PvP, phát thưởng khung / danh hiệu.", "Khung, danh hiệu mùa"),
        tuan("moi:pokedex", "Lễ hội Pokédex", "Điểm nghiên cứu Pokédex x2.", "Điểm Pokédex x2"),
        tuan("moi:chienvung", "Chiến vùng", "Thứ Bảy 20h: Kanto / Johto / Hoenn giành cờ.", "Thưởng vùng thắng"),
        tuan("moi:bungphat", "Bùng phát quần thể", "Mỗi ngày một loài x5 trên một tuyến.", "Loài bùng phát x5"),
    };

    private LichNam() {
    }

    /** Muc theo ma, null neu khong co trong lich. */
    public static Muc muc(final String ma) {
        for (final Muc m : DANH_MUC) {
            if (m.ma.equals(ma)) {
                return m;
            }
        }
        return null;
    }

    /** Moi ma co lich (khong trung). */
    public static Set<String> dsMa() {
        final Set<String> s = new LinkedHashSet<String>();
        for (final Muc m : DANH_MUC) {
            s.add(m.ma);
        }
        return s;
    }

    // ------------------------------------------------------------------ cua so

    /** Mot lan dien ra: ma, nam moc, lan (0.. trong nam, cho tuan Pokemon), [mo, dong) ms. */
    public static final class CuaSo {
        public final Muc muc;
        public final int nam;
        public final int lan;
        public final long mo;
        public final long dong;

        CuaSo(final Muc muc, final int nam, final int lan, final long mo, final long dong) {
            this.muc = muc;
            this.nam = nam;
            this.lan = lan;
            this.mo = mo;
            this.dong = dong;
        }

        public boolean chua(final long t) {
            return t >= mo && t < dong;
        }

        /** Mo ta RIENG lan nay (ngay cong dong: dong noi bat cua thang) - mac dinh mo ta chung cua muc. */
        public String moTa() {
            final String[] r = rieng();
            return r != null && r[0] != null ? r[0] : muc.moTa;
        }

        public String phanThuong() {
            final String[] r = rieng();
            return r != null && r.length > 1 && r[1] != null ? r[1] : muc.phanThuong;
        }

        private String[] rieng() {
            final MoTaLan f = moTaLan;
            try {
                return f == null ? null : f.moTa(muc, nam, lan);
            } catch (final RuntimeException e) {
                return null;
            }
        }
    }

    /** Mo ta rieng tung lan dien ra ({moTa, phanThuong} hoac null) - NgayCongDongGame dat sau khi nap CSDL. */
    public interface MoTaLan {
        String[] moTa(Muc m, int nam, int lan);
    }

    public static volatile MoTaLan moTaLan;

    /** Millis cua ngay duong lich {@code d/m/y} luc GIO_CHUYEN (gio Viet Nam), cong them {@code lech} ngay. */
    public static long moc(final int y, final int m, final int d, final int lech) {
        final Calendar c = Calendar.getInstance(MUI_GIO);
        c.clear();
        c.set(y, m - 1, d, GIO_CHUYEN, 0, 0);
        c.add(Calendar.DAY_OF_MONTH, lech);
        return c.getTimeInMillis();
    }

    /** Ngay duong {y,m,d} cua moc su kien (truoc lech) trong nam {@code nam}. */
    static int[] ngayMoc(final Muc m, final int nam) {
        switch (m.kieu) {
            case AM: {
                final int[] d = AmLich.amSangDuong(m.ngay, m.thang, nam, false);
                return new int[]{d[2], d[1], d[0]};
            }
            case THU_SAU_DEN: {
                final Calendar c = Calendar.getInstance(MUI_GIO);
                c.clear();
                c.set(nam, m.thang - 1, 1, 12, 0, 0);
                int thuSau = 0;
                while (true) {
                    if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && ++thuSau == m.ngay) {
                        return new int[]{nam, m.thang, c.get(Calendar.DAY_OF_MONTH)};
                    }
                    c.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
            default:
                return new int[]{nam, m.thang, m.ngay};
        }
    }

    /** Cac lan dien ra cua {@code m} co moc trong nam {@code nam}. */
    public static List<CuaSo> cuaSo(final Muc m, final int nam) {
        final List<CuaSo> ra = new ArrayList<CuaSo>();
        if (m.kieu == THANG) {
            for (int t = 1; t <= 12; t++) {
                final long[] c = NgayCongDong.cuaSo(nam, t);
                ra.add(new CuaSo(m, nam, t - 1, c[0], c[1]));
            }
            return ra;
        }
        if (m.kieu == TUAN_ISO) {
            int lan = 0;
            for (final Object[] r : SuKienLauDai.LICH) {
                for (final String ma : String.valueOf(r[2]).split(",")) {
                    if (ma.trim().equals(m.ma)) {
                        final long mo = thuHaiTuanIso(nam, ((Number) r[0]).intValue());
                        final int soNgay = ((Number) r[1]).intValue();
                        ra.add(new CuaSo(m, nam, lan++, mo, mo + soNgay * 86400000L));
                    }
                }
            }
            return ra;
        }
        final int[] d = ngayMoc(m, nam);
        final long mo = moc(d[0], d[1], d[2], m.lechDau);
        final long dong = moc(d[0], d[1], d[2], m.lechDau + m.soNgay);
        ra.add(new CuaSo(m, nam, 0, mo, dong));
        return ra;
    }

    /** Thu Hai GIO_CHUYEN:00 cua tuan ISO {@code tuan} nam {@code nam} (gio Viet Nam). */
    public static long thuHaiTuanIso(final int nam, final int tuan) {
        final Calendar c = Calendar.getInstance(MUI_GIO);
        c.clear();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(4);
        c.set(Calendar.YEAR, nam);
        c.set(Calendar.WEEK_OF_YEAR, tuan);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, GIO_CHUYEN);
        return c.getTimeInMillis();
    }

    /** Moi lan dien ra co moc trong cac nam [tuNam, denNam], sap theo gio mo. */
    public static List<CuaSo> tatCa(final int tuNam, final int denNam) {
        final List<CuaSo> ra = new ArrayList<CuaSo>();
        for (int n = tuNam; n <= denNam; n++) {
            for (final Muc m : DANH_MUC) {
                ra.addAll(cuaSo(m, n));
            }
        }
        java.util.Collections.sort(ra, (a, b) -> a.mo != b.mo ? Long.compare(a.mo, b.mo) : a.muc.ma.compareTo(b.muc.ma));
        return ra;
    }

    /** Su kien {@code ma} co dang trong cua so luc {@code t} theo lich mac dinh (khong tinh ghi de cua admin). */
    public static boolean dangDienRa(final String ma, final long t) {
        final Muc m = muc(ma);
        if (m == null) {
            return false;
        }
        final int nam = namCua(t);
        for (int n = nam - 1; n <= nam + 1; n++) {
            for (final CuaSo c : cuaSo(m, n)) {
                if (c.chua(t)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Nam (gio Viet Nam) cua thoi diem {@code t}. */
    public static int namCua(final long t) {
        final Calendar c = Calendar.getInstance(MUI_GIO);
        c.setTimeInMillis(t);
        return c.get(Calendar.YEAR);
    }
}
