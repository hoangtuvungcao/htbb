package real;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import server.TeamServer;

/**
 * CONG BAT/TAT CUA MOI SU KIEN.
 *
 * ==== VAN DE CU ====
 * Gan het su kien duoc mo bang cach SO NGAY HE THONG voi mot moc GHI CUNG trong
 * Char.java, vi du:
 *
 *     public static boolean isSuKienHaloween2016() {
 *         return getDayTime(0L).compareTo("2020-11-01 00:00:00") &lt; 0;
 *     }
 *
 * Moi moc nhu vay deu nam trong 2015..2024, tuc HOM NAY MOI SU KIEN DEU DA TAT VINH
 * VIEN va khong the mo lai neu khong sua ma nguon roi build lai jar. Chi nam su kien
 * co co rieng trong server.ini (sv.isSuKienTet2017, sv.isSuKienGioTo2016,
 * sv.isSuKienNoel2023, sv.isSuKienTetduonglich2024, sv.isSuKienTrungThul2016).
 *
 * ==== BAY GIO ====
 * KHONG CON MOC NGAY NAO trong ma nguon. Moi su kien la mot MA + mot cong bat/tat,
 * luu trong bang CSDL `tob_su_kien` (xem data/DatabaseSuKien), bat luc nao cung duoc.
 * Trang quan tri HTBB-Web /admin/sukien la cho bat/tat.
 *
 * Cac ham Char.isSuKien* / Char.isFinishAllSuKien* van con nguyen ten va chu ky - hon
 * 400 cho trong ma nguon lan lop loi cu (phuc hoi tu htbb-core.jar o P3) goi chung - chi doi than
 * thanh mot dong goi sang day.
 *
 * ==== MAC DINH GIU NGUYEN HANH VI CU ====
 * Khi CHUA co ban ghi nao trong CSDL (bang chua tao, CSDL loi, hoac may chu chay lan
 * dau sau khi cap nhat), macDinh() tra ve DUNG gia tri ma luat cu dang cho ra vao
 * ngay 30/08/2026: tat ca TAT, rieng Trung Thu theo co sv.isSuKienTrungThul2016 trong
 * server.ini. Nen cap nhat ban nay len may chu dang chay khong lam doi mot thu gi cho
 * den khi co nguoi vao trang quan tri bat mot su kien len.
 *
 * ==== VI SAO PHAI KHOI DONG LAI SAU KHI DOI ====
 * Rat nhieu tac dung cua su kien duoc TINH MOT LAN luc khoi dong chu khong hoi lai moi
 * lan dung: bang shop (Map.loadShop dat SELL/NOT_SELL theo isSuKien*), Map.isSale,
 * danh sach npc, bang vat pham... Doi cong giua chung thi duoc nua nay hong nua kia,
 * kho lan hon la khong doi gi. Nen lop nay CO Y chi doc CSDL mot lan luc khoi dong.
 */
public final class SuKien {

    // ============================ MA SU KIEN ============================
    // Ma la khoa chinh trong bang tob_su_kien - DOI LA MAT SETTING cu.

    public static final String TAT_CA = "tatca";

    public static final String TET_2017 = "tet2017";
    public static final String TET_DUONG_LICH = "tetduonglich";
    public static final String TET_2016 = "tet2016";
    public static final String TET_123 = "tet123";

    public static final String NOEL = "noel";
    public static final String NOEL_2023 = "noel2023";
    public static final String DEM_NOEL = "demnoel";
    public static final String BLACK_FRIDAY = "blackfriday";
    public static final String BLACK_FRIDAY_DAU_GIA = "blackfridaydaugia";
    public static final String BLACK_FRIDAY_BAN = "blackfridayban";

    public static final String TRUNG_THU = "trungthu";
    public static final String DEM_TRUNG_THU = "demtrungthu";
    public static final String TRUNG_THU_2015 = "trungthu2015";
    public static final String LONG_DEN_HOA_SEN = "longdenhoasen";
    public static final String HALLOWEEN = "halloween";
    public static final String DEM_HALLOWEEN = "demhalloween";
    public static final String HALLOWEEN_2015 = "halloween2015";
    public static final String HE_2017 = "he2017";
    public static final String HE_2015 = "he2015";
    public static final String VU_LAN = "vulan";
    public static final String GIO_TO = "gioto";

    public static final String MINI = "mini";
    public static final String MINI_CHUC_NU = "minichucnu";
    public static final String MINI_NUI_CHAU_BAU = "mininuichaubau";

    public static final String QUOC_TE_8_3 = "quocte83";
    public static final String SINH_NHAT_2011 = "sinhnhat2011";
    public static final String SINH_NHAT_NGUDE = "snngude";
    public static final String SINH_NHAT_NGUDE_QUA = "snngudequa";
    public static final String DUA_TOP_HOA_LU = "duatophoalu";

    public static final String WORLD_CUP = "worldcup";
    public static final String WORLD_CUP_NHAN_QUA = "worldcupnhanqua";
    public static final String WORLD_CUP_2017 = "worldcup2017";

    public static final String AN_XU_LUONG_TOP = "anxuluongtop";

    // ============================ DANH MUC ============================

    /** Mot dong trong danh muc su kien, dung ca cho trang quan tri. */
    public static final class Muc {

        public final String ma;
        public final String ten;
        public final String nhom;
        /** Mo ta ngan: bat cai nay len thi nguoi choi thay gi. */
        public final String moTa;

        Muc(final String ma, final String ten, final String nhom, final String moTa) {
            this.ma = ma;
            this.ten = ten;
            this.nhom = nhom;
            this.moTa = moTa;
        }
    }

    /**
     * Thu tu o day la thu tu hien tren trang quan tri.
     *
     * Don repo 21/09/2026 (yeu cau nguoi dung "xoa su kien trung lap + xoa het Trung Thu,
     * chi giu ban da sua"): bo 6 muc KHONG con Char.isSuKienXxx() nao trong ma nguon doc
     * toi (rieng biet voi chuyen "trung ten hien thi" - ten co "2015/2016/2017" da KIEM
     * CHUNG tung cai, phan lon van song, khong xoa nham):
     *   - TAT_CA ("Cong tong"): comment cu noi "hon 70 cho hoi isFinishAllSuKien() truoc"
     *     nhung ra soat toan bo ma nguon KHONG thay mot loi goi nao - co tong khong con
     *     chan gi ca, chi con la dong text mo ta cu.
     *   - TET_2016, TRUNG_THU_2015: ban "cu" that su chet, khong con Char.isSuKienXxx()
     *     nao goi toi (khac HALLOWEEN_2015/HE_2015/WORLD_CUP_2017 - ba cai nay TEN cung co
     *     nam nhung VAN dang song, co giu nguyen).
     *   - SINH_NHAT_NGUDE_QUA: Char.canReceiveGiftSNNgude() khong con noi nao goi - tinh
     *     nang phat qua sinh nhat co ve bi bo do, khong chi la "ban cu".
     *   - DEM_TRUNG_THU, LONG_DEN_HOA_SEN: THEO YEU CAU RIENG cua nguoi dung xoa toan bo
     *     Trung Thu tru ban vua sua (TRUNG_THU) - CA HAI VAN DANG SONG that su (tang banh
     *     nuong dang nhap/len cap, ban long den hoa sen qua NPC), khong phai code chet.
     *     Xoa co y, khong phai do nham voi ban cu.
     * Xoa Muc khoi DANH_MUC KHONG xoa dong cu con lai trong bang tob_su_kien (ghiAnhChup()
     * chi INSERT/UPDATE cac ma con trong DANH_MUC, khong bao gio DELETE) - phai chay
     * du_lieu/sql_nguon/xoa_su_kien_trung_lap.sql de don dong cu, khong thi trang admin van con hien
     * (du bam nut cung khong con tac dung, vi SuKien.dat() da loai ma la khong con trong
     * danh muc).
     */
    private static final Muc[] DANH_MUC = {
        new Muc(TET_2017, "Tết Nguyên Đán", "Tết",
                "NPC lì xì, nấu bánh chưng, cây lì xì, top Tết."),
        new Muc(TET_DUONG_LICH, "Tết dương lịch", "Tết",
                "NPC cây thông (npc -35): đổi 10 kẹo + 30 lượng lấy quà ngẫu nhiên."),
        new Muc(TET_123, "Ba ngày Tết", "Tết",
                "Thưởng riêng ba ngày mùng 1-2-3."),

        new Muc(NOEL, "Noel", "Cuối năm",
                "NPC Noel, quà Giáng sinh, Stantler. Luôn tắt trên server giải Liên minh và server local."),
        new Muc(NOEL_2023, "Noel 2023", "Cuối năm",
                "Bản Noel dựng lại năm 2023."),
        new Muc(DEM_NOEL, "Đêm Noel", "Cuối năm",
                "Thưởng riêng đêm 24/12."),
        new Muc(BLACK_FRIDAY, "Black Friday", "Cuối năm",
                "Giảm giá shop dịp Black Friday."),
        new Muc(BLACK_FRIDAY_DAU_GIA, "Black Friday: đấu giá chổi", "Cuối năm",
                "Mở phiên đấu giá chổi."),
        new Muc(BLACK_FRIDAY_BAN, "Black Friday: mở bán", "Cuối năm",
                "Mở bán các món giới hạn của đợt Black Friday."),

        new Muc(TRUNG_THU, "Trung Thu", "Mùa",
                "Bánh nướng / bánh dẻo, lồng đèn, NPC Clefairy, thả đèn."),
        new Muc(HALLOWEEN, "Halloween", "Mùa",
                "Bí ngô, mặt nạ, hộp quà Halloween, boss Darkrai."),
        new Muc(DEM_HALLOWEEN, "Đêm Halloween", "Mùa",
                "Thưởng riêng đêm 31/10."),
        new Muc(HALLOWEEN_2015, "Halloween 2015", "Mùa",
                "Bản Halloween cũ."),
        new Muc(HE_2017, "Sự kiện Hè", "Mùa",
                "Kem, dưa hấu, socola, NPC mùa hè."),
        new Muc(HE_2015, "Hè 2015", "Mùa",
                "Bản sự kiện hè cũ."),
        new Muc(VU_LAN, "Vu Lan", "Mùa",
                "Rơi nhánh hoa hồng vàng (thu hoạch cây thần, chế đồ, thăng bậc, săn boss,"
                + " hạ quái lệch cấp); NPC Đấu Trường đổi bó hoa vàng/đỏ và nguyên liệu;"
                + " tinh luyện bảo vật huyền thoại +10% với đá nham thạch cấp 2; Mặt nạ Snorlax."),
        new Muc(GIO_TO, "Giỗ Tổ Hùng Vương", "Mùa",
                "Bánh giày, pháo hoa, đổi nguyên liệu, bảng xếp hạng bắn pháo."),

        new Muc(MINI, "Sự kiện mini", "Mini",
                "Đổi đồ Absol, Oricorio, quà đặc biệt ở NPC đấu trường."),
        new Muc(MINI_CHUC_NU, "Mini Illumise", "Mini",
                "Nhiệm vụ đi tìm Illumise."),
        new Muc(MINI_NUI_CHAU_BAU, "Mini Núi Châu Báu", "Mini",
                "Bản mini của núi châu báu."),

        new Muc(QUOC_TE_8_3, "Quốc tế Phụ nữ 8/3", "Kỷ niệm",
                "Quà 8/3, hoa hồng."),
        new Muc(SINH_NHAT_2011, "Ngày Nhà giáo Việt Nam 20/11", "Kỷ niệm",
                "Lồng đèn hoa sen, chổi băng / chổi độc / chổi điện ở NPC sự kiện."),


        // ---- GD2 buoc 5.1: ma moi cua lich 52 tuan (du_lieu/su_kien_nam.csv). Chi co hieu
        // ung khi sv.lau_dai=1 (SuKienLauDaiGame); tat co thi bat cung khong lam gi. ----
        new Muc("moi:congdong", "Ngày cộng đồng", "Pokémon",
                "3 giờ: dòng khởi đầu xuất hiện dày, shiny x5 (cần sv.lau_dai=1)."),
        new Muc("moi:shiny", "Tuần Shiny", "Pokémon",
                "Tỉ lệ shiny x10, pity 2.000 lần hạ (cần sv.lau_dai=1)."),
        new Muc("moi:dotkich", "Đột kích huyền thoại", "Pokémon",
                "Boss thế giới theo lịch, thưởng theo % đóng góp (cần sv.lau_dai=1)."),
        new Muc("moi:tuanhe", "Tuần hệ", "Pokémon",
                "Chiêu hệ của tuần +10% sát thương (cần sv.lau_dai=1)."),
        new Muc("moi:pokedex", "Lễ hội Pokédex", "Pokémon",
                "Điểm nghiên cứu Pokédex x2 (cần sv.lau_dai=1)."),
        new Muc("moi:bungphat", "Bùng phát quần thể", "Pokémon",
                "Một loài x5 trên một tuyến mỗi ngày (cần sv.lau_dai=1)."),
        new Muc("moi:chienvung", "Chiến vùng", "Pokémon",
                "Thứ Bảy 20h, Kanto/Johto/Hoenn giành cờ (dùng lại chiến trường)."),
        new Muc("moi:muapvp", "Kết thúc mùa PvP", "Pokémon",
                "Chốt bậc mùa PvP, phát thưởng khung/danh hiệu (cần sv.lau_dai=1)."),
        new Muc("moi:valentine", "Valentine", "Lễ Việt Nam",
                "Valentine: Luvdisc, cặp đôi cùng đội."),
        new Muc("moi:3004", "30/4 - 1/5", "Lễ Việt Nam",
                "Thống nhất đất nước, nhiệm vụ tuyến."),
        new Muc("moi:thieunhi", "Quốc tế Thiếu nhi 1/6", "Lễ Việt Nam",
                "Quà thiếu nhi (vật phẩm khoá, hết hạn)."),
        new Muc("moi:quockhanh", "Quốc khánh 2/9", "Lễ Việt Nam",
                "Nhiệm vụ tuyến toàn quốc."),
        new Muc("moi:2010", "Phụ nữ Việt Nam 20/10", "Lễ Việt Nam",
                "Nhiệm vụ tặng quà."),
        new Muc("moi:hamngucmua", "Hầm ngục bí ẩn mùa hè", "Pokémon",
                "Hầm ngục bí ẩn mùa hè (cần sv.lau_dai=1)."),
        new Muc("moi:tongket", "Tổng kết năm", "Pokémon",
                "Tổng kết năm + mùa giải PvP 4."),

        new Muc(AN_XU_LUONG_TOP, "Bảng top: ẩn xu/lượng", "Bảng xếp hạng",
                "Bật thì mọi bảng top (đại gia, nhà vô địch, đội, top theo phe, top nhóm đua)"
                + " không hiện số xu/lượng của từng người/đội nữa - thứ hạng vẫn giữ nguyên."),
    };

    // ============================ TRANG THAI ============================

    /** Ma -> bat/tat. Ma khong co trong day thi dung macDinh(). */
    private static final ConcurrentHashMap<String, Boolean> TRANG_THAI =
            new ConcurrentHashMap<String, Boolean>();

    /** Da doc CSDL thanh cong chua - trang quan tri can biet de canh bao. */
    private static volatile boolean daNap = false;

    private SuKien() {
    }

    public static Muc[] danhMuc() {
        return DANH_MUC.clone();
    }

    public static Muc timMuc(final String ma) {
        for (final Muc m : DANH_MUC) {
            if (m.ma.equals(ma)) {
                return m;
            }
        }
        return null;
    }

    public static boolean daNap() {
        return daNap;
    }

    /** Su kien nay dang bat hay khong. Day la CAU HOI DUY NHAT ma phan con lai cua game hoi. */
    public static boolean bat(final String ma) {
        final Boolean v = TRANG_THAI.get(ma);
        return (v == null) ? macDinh(ma) : v.booleanValue();
    }

    /** Xu that hay 0, tuy AN_XU_LUONG_TOP dang bat hay khong - dung khi ghi goi tin bang top. */
    public static long xuChoTop(final long xuThat) {
        return bat(AN_XU_LUONG_TOP) ? 0L : xuThat;
    }

    /** Tuong tu xuChoTop() nhung cho luong. */
    public static int luongChoTop(final int luongThat) {
        return bat(AN_XU_LUONG_TOP) ? 0 : luongThat;
    }

    /**
     * Gia tri dung khi CSDL chua noi gi.
     *
     * CO Y tra ve dung nhung gi luat ngay thang cu dang cho ra hom bo moc ngay
     * (30/08/2026): tat ca TAT, tru cac muc con co trong server.ini. Nho vay cap nhat
     * ban nay len may chu dang chay khong lam doi mot thu gi.
     */
    private static boolean macDinh(final String ma) {
        if (TET_2017.equals(ma)) {
            return TeamServer.isSuKienTet2017;
        }
        if (TET_DUONG_LICH.equals(ma)) {
            return TeamServer.isSuKienTetduonglich2024;
        }
        if (NOEL_2023.equals(ma)) {
            return TeamServer.isSuKienNoel2023;
        }
        if (GIO_TO.equals(ma)) {
            return TeamServer.isSuKienGioTo2016;
        }
        if (TRUNG_THU.equals(ma) || DEM_TRUNG_THU.equals(ma)) {
            return TeamServer.isSuKienTrungThul2016;
        }
        // World Cup (binh chon Messi/Ronaldo), Sinh nhat Ngude, Dua top Hoa Lu: su kien rieng cua game kiem hiep cu,
        // BO 19/10/2026 (chu game: moi su kien phai la su kien Pokemon theo lich nam) - khong con trong DANH_MUC,
        // SuKien.dat() bo qua, mac dinh TAT (khong con mo theo cua so sv.worldCup* trong server.ini).
        return false;
    }

    /**
     * Dat lai toan bo bang trang thai. Goi tu DatabaseSuKien luc khoi dong.
     *
     * Ma khong co trong danh muc bi bo qua: mot dong sot lai trong CSDL sau khi doi ten
     * ma khong duoc phep am tham lam doi hanh vi cua su kien khac.
     */
    public static void dat(final java.util.Map<String, Boolean> moi) {
        TRANG_THAI.clear();
        if (moi == null) {
            return;
        }
        for (final java.util.Map.Entry<String, Boolean> e : moi.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || timMuc(e.getKey()) == null) {
                continue;
            }
            TRANG_THAI.put(e.getKey(), e.getValue());
        }
        daNap = true;
    }

    /**
     * Doc CSDL roi ghi lai anh chup de trang quan tri thay duoc trang thai that.
     *
     * CHAY LICH HEN TRUOC KHI DOC. May chu tat vai tieng (hoac vai ngay) thi trong luc do
     * co the da co moc mo/dong toi han. Doc cot `bat` truoc roi moi chay lich thi lan bat
     * may nay van theo trang thai CU, va phai doi them mot vong khoi dong lai nua - tuc la
     * mot su kien hen mo luc 0h se thuc su mo luc 0h06 kem mot lan da nguoi choi khong can
     * thiet. Chay lich truoc thi no vao game ngay tu lan bat may nay.
     */
    public static void nap() {
        data.DatabaseSuKien.chayLichDenHan();
        // Lich nam (19/10/2026): dat bat theo ngay that TRUOC khi doc - khoi dong luc nao cung ap dung dung lich.
        try {
            LichSuKien.dongBoLichNam();
        } catch (final Throwable e) {
            System.out.println("[SUKIEN] khong dong bo duoc lich nam luc khoi dong: " + e);
        }
        data.DatabaseSuKien.nap();
        data.DatabaseSuKien.ghiAnhChup();
    }

    /** Mot dong tom tat de in ra luc khoi dong. */
    public static String moTa() {
        final List<String> dangBat = new ArrayList<String>();
        for (final Muc m : DANH_MUC) {
            if (bat(m.ma)) {
                dangBat.add(m.ten);
            }
        }
        final String nguon = daNap ? "CSDL" : "MAC DINH (chua doc duoc bang tob_su_kien)";
        if (dangBat.isEmpty()) {
            return "khong co su kien nao dang bat  [nguon: " + nguon + "]";
        }
        return dangBat.size() + "/" + DANH_MUC.length + " dang bat: "
                + noiChuoi(dangBat) + "  [nguon: " + nguon + "]";
    }

    /** String.join khong co tren muc ngon ngu 8 cua ban build cu; viet tay cho chac. */
    private static String noiChuoi(final List<String> ds) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ds.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ds.get(i));
        }
        return sb.toString();
    }
}
