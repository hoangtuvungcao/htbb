import java.util.List;

/**
 * Kich ban VUON BERRY (bat lai vuon 29/10/2026, khoi bat_lai_vuon.sql; server com.htbb.pokemon.VuonBerry) - nguoi choi
 * THUONG, tai khoan MOI TINH (khong GM); GM chi dung canh: slv nhan vat len cap 7 + "tua" dong ho ao 21 phut (CHI may thu
 * sv.cho_phep_thu_nghiem=1, "tua 0" tra lai cuoi kich ban).
 *
 *  vuon_berry  Sổ tay buoc P-01.2.3 "Trồng và thu hoạch 2 cây Berry Oran" (dat tien do dang lam khi nhan vat da thoat,
 *              nhu chon "Nhận nhiệm vụ phụ"); go "hst" → Vườn nhà và bạn bè → Ô đất của tôi (2 o) → gieo Berry Oran o 1 + o 2
 *              (tru xu hat); bam o dang lon = "còn N phút", khong thu; DANG NHAP LAI: cay con, van dang lon; tua 21 phut →
 *              "Thu hoạch tất cả ô đã chín (2)" → +3 + 3 Berry Oran, P-01.2.3 xong (tob_nhiem_vu_lau_dai trang_thai 1, 2/2);
 *              bam lai (goi menu cu) khong them berry; DANG NHAP LAI: o trong, tui 6 Berry Oran (tob_hst_nhan_vat), tien do
 *              giu nguyen; che "Thuốc xịt từ vườn" (3 Berry Oran -> 1 Thuốc xịt = 10 binh HP nho). Can -Ddb (+ -Ddbport), may thu.
 * Chay: BotChucNang host cong kt vuon_berry (hoi_quy.conf nhom vuon_berry).
 */
class KichBanVuon {

    static final int NPC = -528, M_GOC = 0, M_VUON = 19, M_O = 30, M_GIEO = 31;
    static final String BUOC = "P-01.2.3";

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        BotChucNang.dk("vuon_berry", false, KichBanVuon::vuon);
    }

    /** Mo he sinh thai bang chat "hst" (nhu nguoi choi) roi vao menu Vườn; tra ve so muc menu Vườn (-1 = khong mo duoc). */
    static int moVuon(BotChucNang.Bot b) throws Exception {
        int m = b.moc();
        b.chat("hst");
        if (b.doiMenu(m, NPC, M_GOC, 5000) < 0) return -1;
        int vt = b.timMuc("Vườn nhà và bạn bè");
        if (vt < 0) return -1;
        m = b.moc();
        b.menu(NPC, M_GOC, vt);
        return b.doiMenu(m, NPC, M_VUON, 5000);
    }

    /** Menu Vườn → muc 0 "Ô đất của tôi" → danh sach o (menu 30). */
    static List<String> moODat(BotChucNang.Bot b) throws Exception {
        if (moVuon(b) < 0) return null;
        int m = b.moc();
        b.menu(NPC, M_VUON, 0);
        return b.doiMenu(m, NPC, M_O, 5000) < 0 ? null : new java.util.ArrayList<>(b.menuChu);
    }

    static void cho(BotChucNang.Bot b) {
        b.bom(1700); // may chu chan bam lien < 1,5 giay (HeSinhThaiMenu.quaNhanh) - nhu nguoi bam tay
    }

    static String duLieu(int id) {
        return BotChucNang.sql1("SELECT du_lieu FROM tob_hst_nhan_vat WHERE id_char=" + id);
    }

    static String tienDo(int id) {
        return BotChucNang.sql1("SELECT CONCAT(tien_do, '/', trang_thai) FROM tob_nhiem_vu_lau_dai WHERE id_char=" + id + " AND ma='" + BUOC + "'");
    }

    static void vuon() throws Exception {
        String u, ten;
        int id;
        try (BotChucNang.Bot b = KichBanHuongDan.botMoiTinh("vu", 0)) {
            KichBanHuongDan.roiSan(b);
            u = b.user;
            ten = b.ten;
            id = b.charDb;
            try (BotChucNang.Bot gm = BotChucNang.botMoi(0)) {
                gm.gm("slv " + ten + " 7 0", 1500); // GM chi dung canh: cap 7 (cap mo P-01.2.3)
            }
            b.bom(1000);
        }
        String gmTk = BotChucNang.sql1("SELECT gia_tri FROM cau_hinh WHERE khoa='sv.adminAccounts'");
        ket("vuon.nhan_vat_thuong", gmTk != null && !("," + gmTk + ",").contains("," + u + ","), u + " khong trong sv.adminAccounts");
        // dang lam P-01.2.3 (nhu da chon "Nhận nhiệm vụ phụ" o So tay) - ghi khi nhan vat da thoat (TD nap luc vao game)
        Thread.sleep(3000);
        BotChucNang.sql("INSERT INTO tob_nhiem_vu_lau_dai (id_char, ma, tien_do, trang_thai, tuan) VALUES (" + id + ", '" + BUOC
                + "', 0, 0, 0) ON DUPLICATE KEY UPDATE tien_do = 0, trang_thai = 0");
        ket("vuon.dat_dang_lam", "0/0".equals(tienDo(id)), tienDo(id));
        BotChucNang.Bot gm = BotChucNang.botMoi(0);
        try {
            BotChucNang.Bot b = KichBanSuaTuyen.vaoLai(u);
            long xu0;
            try {
                ket("vuon.cap", b.lv >= 7, "cap " + b.lv);
                int n = moVuon(b);
                ket("vuon.mo_menu", n == 6 && b.menuChu.get(0).startsWith("Ô đất của tôi (0/2 ô có cây"), String.valueOf(b.menuChu));
                List<String> o = moODat(b);
                ket("vuon.o_dat_2_o", o != null && o.size() >= 2 && o.get(0).equals("Ô 1: trống - gieo hạt")
                        && o.get(1).equals("Ô 2: trống - gieo hạt"), String.valueOf(o));
                b.lamMoi();
                xu0 = b.xu;
                for (int i = 0; i < 2; i++) {
                    cho(b);
                    moODat(b);
                    int m = b.moc();
                    b.menu(NPC, M_O, i);
                    boolean coHat = b.doiMenu(m, NPC, M_GIEO, 5000) >= 0 && b.menuChu.get(0).startsWith("Berry Oran - hạt ");
                    ket("vuon.menu_hat." + (i + 1), coHat, String.valueOf(b.menuChu));
                    cho(b);
                    m = b.moc();
                    b.menu(NPC, M_GIEO, 0);
                    String r = b.doiChu(m, 5000, "Đã gieo Berry Oran ở ô " + (i + 1));
                    ket("vuon.gieo." + (i + 1), r != null, r == null ? b.chuTu(m) : r);
                }
                b.lamMoi();
                ket("vuon.tru_xu_hat", b.xu < xu0, "xu " + xu0 + " -> " + b.xu);
                cho(b);
                o = moODat(b);
                ket("vuon.dang_lon", o != null && o.get(0).startsWith("Ô 1: Berry Oran - còn ") && o.get(1).startsWith("Ô 2: Berry Oran - còn "),
                        String.valueOf(o));
                cho(b);
                int m = b.moc();
                b.menu(NPC, M_O, 0);
                String r = b.doiChu(m, 4000, "còn ");
                ket("vuon.bam_o_dang_lon_khong_thu", r != null && "0/0".equals(tienDo(id)), r + " | " + tienDo(id));
            } finally {
                b.close();
            }
            // ---- dang nhap lai giua chung: cay con, van lon theo dong ho may chu
            Thread.sleep(2500);
            String d = duLieu(id);
            ket("vuon.luu_khi_gieo", d != null && d.contains("|vu=30.1.") && d.contains(",30.1."), d == null ? "null" : d.replaceAll(".*\\|vu=", "vu="));
            b = KichBanSuaTuyen.vaoLai(u);
            try {
                List<String> o = moODat(b);
                ket("vuon.dang_nhap_lai_cay_con", o != null && o.get(0).startsWith("Ô 1: Berry Oran - còn ") && o.get(1).startsWith("Ô 2: Berry Oran - còn "),
                        String.valueOf(o));
                // ---- dong ho may chu tien 21 phut (GM tua - may thu)
                int mg = gm.moc();
                gm.gm("tua 21m", 1500);
                ket("vuon.tua_dong_ho", gm.chuTu(mg).length() > 0, gm.chuTu(mg));
                cho(b);
                o = moODat(b);
                ket("vuon.chin", o != null && o.get(0).equals("Thu hoạch tất cả ô đã chín (2)") && o.contains("Ô 1: Berry Oran đã chín - thu hoạch"),
                        String.valueOf(o));
                cho(b);
                int m = b.moc();
                b.menu(NPC, M_O, 0);
                String r = b.doiChu(m, 5000, "Thu hoạch ô 2: +3 Berry Oran");
                b.bom(1500);
                ket("vuon.thu_hoach", r != null && b.chuTu(m).contains("Thu hoạch ô 1: +3 Berry Oran"), b.chuTu(m));
                ket("vuon.so_tay_xong", "2/1".equals(tienDo(id)), "P-01.2.3 tien_do/trang_thai = " + tienDo(id));
                // bam lai dong cu (menu da gui truoc khi thu): khong them berry, tien do giu
                cho(b);
                b.menu(NPC, M_O, 1);
                b.bom(1500);
                cho(b);
                o = moODat(b);
                ket("vuon.sau_thu_o_trong", o != null && o.get(0).equals("Ô 1: trống - gieo hạt") && o.get(1).equals("Ô 2: trống - gieo hạt"),
                        String.valueOf(o));
            } finally {
                b.close();
            }
            // ---- dang nhap lai sau thu: tui 6 Berry Oran (bac 1 = khoa 301), o trong, tien do giu
            Thread.sleep(2500);
            d = duLieu(id);
            ket("vuon.luu_sau_thu", d != null && d.contains("tui=") && d.contains("301:6") && d.contains("|vu=0.0.0,0.0.0"),
                    d == null ? "null" : d.replaceAll("\\|(kn|tr|nt|gi|nc|al|vx|dem|bf|tt|mg|dh)=[^|]*", ""));
            b = KichBanSuaTuyen.vaoLai(u);
            try {
                List<String> o = moODat(b);
                ket("vuon.dang_nhap_lai_khong_nhan_doi", o != null && o.get(0).equals("Ô 1: trống - gieo hạt"), String.valueOf(o));
                ket("vuon.tien_do_giu", "2/1".equals(tienDo(id)), tienDo(id));
                String d2 = duLieu(id);
                ket("vuon.tui_khong_doi", d2 != null && d2.contains("301:6"), "tui " + (d2 == null ? "null" : d2.replaceAll(".*\\|tui=([^|]*).*", "$1")));
                // ---- dung berry: che "Thuốc xịt từ vườn" (3 Berry Oran -> 10 binh HP nho, re hon mua NPC)
                cho(b);
                int m = b.moc();
                b.chat("hst");
                int vt = b.doiMenu(m, NPC, M_GOC, 5000) < 0 ? -1 : b.timMuc("Nấu ăn, chế thuốc");
                m = b.moc();
                if (vt >= 0) b.menu(NPC, M_GOC, vt);
                boolean coCt = b.doiMenu(m, NPC, 1, 5000) >= 0 && b.menuChu.size() > 30 && b.menuChu.get(30).startsWith("Thuốc xịt từ vườn");
                ket("vuon.cong_thuc_thuoc_vuon", coCt, b.menuChu.size() > 30 ? b.menuChu.get(30) : String.valueOf(b.menuChu));
                cho(b);
                m = b.moc();
                b.menu(NPC, 1, 30);
                b.bom(2500);
                b.close();
                String d3 = null;
                for (int lan = 0; lan < 12; lan++) { // che thuoc ghi theo nhip luu he sinh thai (15 giay)
                    Thread.sleep(2500);
                    d3 = duLieu(id);
                    if (d3 != null && d3.contains("301:3")) break;
                }
                ket("vuon.che_thuoc_vuon", d3 != null && d3.contains("301:3") && d3.matches(".*\\|tui=[^|]*90[1-5]:1.*"),
                        "tui " + (d3 == null ? "null" : d3.replaceAll(".*\\|tui=([^|]*).*", "$1")));
            } finally {
                b.close();
            }
        } finally {
            try {
                gm.gm("tua 0", 1200); // tra dong ho may thu
            } finally {
                gm.close();
            }
        }
    }
}
