import java.util.*;

/**
 * Kich ban QA toan game (vong ra soat 23/10/2026): cac loi tim thay khi choi dau-cuoi bang bot, moi loi mot kich ban
 * hoi quy. Chay cung BotChucNang (can -Ddb, -Ddbport tro CSDL thu; tai khoan GM kt001..kt040):
 *
 *   java -cp build/kiem_tai -Ddb=htbb_db -Ddbport=3399 BotChucNang 127.0.0.1 19239 kt qa_giftcode_tui_day
 *
 * <ul>
 *   <li>{@code qa_giftcode_tui_day}: giftcode co ITEM khi hanh trang day - truoc day ma bi tru luot + ghi log nhung
 *       vat pham roi mat (Char.createItemSoLuong bo qua khi day), cau bao van noi "+n". Nay: tu choi TRUOC khi danh
 *       dau, ma con nguyen luot, nhap lai sau khi don tui thi nhan du.</li>
 * </ul>
 */
public class KichBanQaToanGame {

    static void dangKy() {
        BotChucNang.dk("qa_giftcode_tui_day", true, KichBanQaToanGame::giftcodeTuiDay);
        BotChucNang.dk("qa_o_nhanh_dang_nhap_lai", false, KichBanQaToanGame::oNhanhDangNhapLai);
        BotChucNang.dk("qa_quan_ly_thu", true, KichBanQaToanGame::quanLyThu);
        BotChucNang.dk("qa_dang_ky_trong_game", false, KichBanQaToanGame::dangKyTrongGame);
        BotChucNang.dk("qa_ve_o_thu", true, KichBanQaToanGame::veOThu);
        BotChucNang.dk("qa_ten_pho_bien", false, KichBanQaToanGame::tenPhoBien);
        BotChucNang.dk("qa_khoi_dong_lai_ghi", true, KichBanQaToanGame::khoiDongLaiGhi);
        BotChucNang.dk("qa_khoi_dong_lai_kiem", false, KichBanQaToanGame::khoiDongLaiKiem);
    }

    /** So mon template {@code tpl} trong tui. */
    static int dem(BotChucNang.Bot b, int tpl) {
        int n = 0;
        for (int[] d : b.doDung) if (d[1] == tpl) n++;
        return n;
    }

    /** GM ait mon {@code tpl} cho toi khi server bao tui day (toi da {@code toiDa} lan). */
    static void lamDayTui(BotChucNang.Bot b, int tpl, int toiDa) throws Exception {
        for (int i = 0; i < toiDa; i++) {
            int truoc = b.doDung.size();
            b.gm("ait " + b.ten + " " + tpl + " 0 0 0", 120);
            if (b.doDung.size() == truoc) b.bom(300);
            if (i % 10 == 9) b.lamMoi();
        }
        b.lamMoi();
    }

    static void giftcodeTuiDay() throws Exception {
        if (BotChucNang.DB.isEmpty()) {
            BotChucNang.ket("qa_giftcode_tui_day", null, "can -Ddb");
            return;
        }
        final String ma = "qagctuiday";
        final int tpl = 714;
        BotChucNang.sql("DELETE FROM tob_giftcode WHERE ma = '" + ma + "'");
        BotChucNang.sql("DELETE FROM tob_giftcode_log WHERE ma = '" + ma + "'");
        BotChucNang.sql("INSERT INTO tob_giftcode (ma, phan_thuong, han, tong_luot, da_dung, cap_toi_thieu, bat, ghi_chu) VALUES ('"
                + ma + "', 'ITEM:" + tpl + ":2', NULL, 10, 0, 1, 1, 'qa tui day')");
        try (BotChucNang.Bot b = BotChucNang.botMoi(0)) {
            KichBanDS.veLang(b);
            String uid = BotChucNang.sql1("SELECT userId FROM tob_char WHERE id = " + b.charDb);
            BotChucNang.sql("DELETE FROM tob_giftcode_log WHERE user_id = " + uid);
            // tui day: them do toi khi khong them duoc nua (sucChuaTui = 42 o mac dinh)
            lamDayTui(b, 1, 60);
            int truoc = dem(b, tpl), tong = b.doDung.size();
            String t = KichBanGiftcode.nhap(b, ma, "thanh cong", "hanh trang", "o trong", "khong", "het");
            b.lamMoi();
            int sau = dem(b, tpl);
            String dd = BotChucNang.sql1("SELECT da_dung FROM tob_giftcode WHERE ma = '" + ma + "'");
            String log = BotChucNang.sql1("SELECT COUNT(*) FROM tob_giftcode_log WHERE ma = '" + ma + "' AND user_id = " + uid);
            boolean khongMat = (sau - truoc == 2) || ("0".equals(dd) && "0".equals(log) && sau == truoc);
            BotChucNang.ket("qa_giftcode_tui_day.khong_mat_qua", khongMat,
                    "tui " + tong + " mon; " + t + " | mon " + tpl + ": " + truoc + " -> " + sau + ", da_dung " + dd + ", log " + log);
            boolean baoRo = sau - truoc == 2 || t.contains("ô trống") || t.contains("Hành trang");
            BotChucNang.ket("qa_giftcode_tui_day.bao_ro", baoRo, t);
            // don tui (bo 3 mon dau ra dat) roi nhap lai: nhan du 2
            int bo = 0, canBo = Math.max(3, b.doDung.size() - 36); // tui GM ait co the vuot suc chua: bo toi khi con cho
            for (int[] d : new ArrayList<>(b.doDung)) {
                if (d[1] != 1 || bo >= canBo) continue;
                b.gui(61, (short) d[0], (byte) 1); // vut / huy do (Map.doGiveItemToGround)
                b.bom(250);
                bo++;
            }
            b.lamMoi();
            int truoc2 = dem(b, tpl);
            String t2 = KichBanGiftcode.nhap(b, ma, "thanh cong", "da nhap", "o trong", "khong");
            b.lamMoi();
            int sau2 = dem(b, tpl);
            if ("0".equals(dd)) {
                BotChucNang.ket("qa_giftcode_tui_day.nhap_lai", sau2 - truoc2 == 2 && t2.contains("thành công"),
                        "vut " + bo + " mon, tui " + b.doDung.size() + "; " + t2 + " | " + truoc2 + " -> " + sau2);
            }
        }
    }

    // ------------------------------------------------------------------ luu khi dang nhap lai / khoi dong lai

    /** Goi -103 loai 1 cuoi cung tu server: {so trang, trang dang dung, a, b, c} hoac null. */
    static long[] oNhanh(BotChucNang.Bot b, int tu) {
        long[] kq = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -103 || e.d == null || e.d.length < 27 || e.d[0] != 1) continue;
            try {
                java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(e.d));
                r.readByte();
                kq = new long[]{r.readUnsignedByte(), r.readUnsignedByte(), r.readLong(), r.readLong(), r.readLong()};
            } catch (java.io.IOException ignored) {
            }
        }
        return kq;
    }

    static BotChucNang.Bot vaoLai(String u) throws Exception {
        BotChucNang.Bot b = new BotChucNang.Bot(u);
        String l = b.dangNhap("123456");
        if (l != null && l.contains("khong tra loi")) {
            b.close();
            Thread.sleep(2000);
            b = new BotChucNang.Bot(u);
            l = b.dangNhap("123456");
        }
        if (l == null) l = b.vaoGame(0);
        if (l != null) throw new IllegalStateException(u + ": " + l);
        b.bom(1500);
        return b;
    }

    /**
     * O nhanh (trang chieu, -103 loai 1) gan xong roi ROT KET NOI ngay (khong thoat): vao lai phai thay dung cac o vua
     * gan - may chu giu o HoSoLauDai (co "ban"), phai luu khi thoat.
     */
    static void oNhanhDangNhapLai() throws Exception {
        String u;
        final long a = 0x0123456000L, c2 = 0x0FEDCBA98L; // o hop le (may chu bo o la tu 0.9.14)
        try (BotChucNang.Bot b = BotChucNang.botMoi(0)) {
            u = b.user;
            KichBanDS.veLang(b);
            b.gui(-103, (byte) 1, (byte) 1, a, c2, 0L);
            b.bom(800);
            b.s.close(); // rot dot ngot
        }
        Thread.sleep(3000);
        try (BotChucNang.Bot b2 = vaoLai(u)) {
            long[] o = oNhanh(b2, 0);
            BotChucNang.ket("qa_o_nhanh_dang_nhap_lai", o != null && o[2] == a && o[3] == c2 && o[1] == 1,
                    o == null ? "khong nhan -103 loai 1 (client >= 908)" : "trang " + o[1] + "/" + o[0] + " o1=" + Long.toHexString(o[2])
                            + " o2=" + Long.toHexString(o[3]) + " (mong doi trang 1, " + Long.toHexString(a) + ", " + Long.toHexString(c2) + ")");
        }
    }

    static final String TEP_KDL = System.getProperty("tepKdl", "/tmp/qa_khoi_dong_lai.txt");

    /**
     * Truoc khi khoi dong lai server: mot nhan vat mua thuoc o NPC (xu), gan o nhanh, khoa / doi trang thai... roi THOAT
     * BINH THUONG ngay; ghi trang thai mong doi vao {@link #TEP_KDL}. Sau khi khoi dong lai: {@code qa_khoi_dong_lai_kiem}.
     */
    static void khoiDongLaiGhi() throws Exception {
        try (BotChucNang.Bot b = KichBanDS3.botNha(10)) {
            b.gui(23, (byte) 1);
            b.bom(800);
            b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 7);
            b.bom(1200);
            final long a = 0x0A0B0C0DL;
            b.gui(-103, (byte) 1, (byte) 0, a, 0L, 0L);
            b.bom(600);
            b.lamMoi();
            try (java.io.PrintWriter w = new java.io.PrintWriter(TEP_KDL, "UTF-8")) {
                w.println(b.user + " " + b.xu + " " + b.soThuoc(1) + " " + a + " " + b.lv + " " + b.doDung.size());
            }
            BotChucNang.ket("qa_khoi_dong_lai_ghi", true, b.user + " xu " + b.xu + " thuoc1 " + b.soThuoc(1) + " lv " + b.lv + " tui " + b.doDung.size());
        }
    }

    static void khoiDongLaiKiem() throws Exception {
        String[] f;
        try {
            f = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(TEP_KDL)), "UTF-8").trim().split(" ");
        } catch (Exception e) {
            BotChucNang.ket("qa_khoi_dong_lai_kiem", null, "chua chay qa_khoi_dong_lai_ghi");
            return;
        }
        try (BotChucNang.Bot b = vaoLai(f[0])) {
            b.lamMoi();
            long[] o = oNhanh(b, 0);
            boolean ok = b.xu == Long.parseLong(f[1]) && b.soThuoc(1) == Integer.parseInt(f[2]) && o != null
                    && o[2] == Long.parseLong(f[3]) && b.lv == Integer.parseInt(f[4]) && b.doDung.size() == Integer.parseInt(f[5]);
            BotChucNang.ket("qa_khoi_dong_lai_kiem", ok, "mong doi xu " + f[1] + " thuoc1 " + f[2] + " o1 " + f[3] + " lv " + f[4] + " tui " + f[5]
                    + " | thuc te xu " + b.xu + " thuoc1 " + b.soThuoc(1) + " o1 " + (o == null ? "-" : String.valueOf(o[2])) + " lv " + b.lv
                    + " tui " + b.doDung.size());
        }
    }

    // ------------------------------------------------------------------ quan ly thu (-119)

    /** {sucChua, soVe, xuConNgay} tu phan noi them goi 16 loai 2 cuoi cung; null = khong co. */
    static int[] oThu(BotChucNang.Bot b) {
        int[] kq = null;
        for (int i = 0; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != 16 || e.d == null || e.d.length < 2 || e.d[0] != 2) continue;
            try {
                java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(e.d));
                r.readByte();
                r.readInt();
                int na = r.readByte();
                for (int k = 0; k < na; k++) { r.readShort(); r.readByte(); r.readByte(); r.readByte(); r.readUTF(); r.readUTF(); r.readByte(); }
                int np = r.readByte();
                for (int k = 0; k < np; k++) { r.readShort(); r.readByte(); r.readByte(); r.readUTF(); r.readByte(); r.readUTF(); r.readInt(); r.readByte(); }
                r.readInt();
                if (r.available() < 20) continue;
                r.readByte();
                r.readShort();
                int suc = r.readShort();
                r.readShort();
                int ve = r.readShort();
                r.readShort();
                r.readInt();
                kq = new int[]{suc, ve, r.readInt()};
            } catch (java.io.IOException ignored) {
            }
        }
        return kq;
    }

    /** Ve mo o thu: mua bang luong (-119 4) roi dung (-119 3): o +1, luong tru dung mot lan; bam dung hai lan khi chi co 1 ve; dang nhap lai giu o. */
    static void veOThu() throws Exception {
        String u;
        int sucSau;
        try (BotChucNang.Bot b = KichBanDS3.botLang(10)) {
            u = b.user;
            b.gm("smn " + b.ten + " 0 100 0", 800);
            b.gui(-119, (byte) 0);
            b.bom(800);
            b.lamMoi();
            int[] o0 = oThu(b);
            int l0 = b.luong;
            int m = b.moc();
            b.gui(-119, (byte) 4);
            b.bom(1200);
            b.lamMoi();
            int[] o1 = oThu(b);
            BotChucNang.ket("qa_ve_o_thu.mua", o0 != null && o1 != null && o1[1] == o0[1] + 1 && b.luong < l0,
                    "ve " + (o0 == null ? "?" : o0[1]) + " -> " + (o1 == null ? "?" : o1[1]) + ", luong " + l0 + " -> " + b.luong + " " + b.chuTu(m));
            m = b.moc();
            b.gui(-119, (byte) 3);
            b.gui(-119, (byte) 3); // bam dup, chi co 1 ve
            b.bom(1500);
            b.lamMoi();
            int[] o2 = oThu(b);
            sucSau = o2 == null ? -1 : o2[0];
            BotChucNang.ket("qa_ve_o_thu.dung", o1 != null && o2 != null && o2[0] == o1[0] + 1 && o2[1] == o1[1] - 1,
                    "o " + (o1 == null ? "?" : o1[0]) + " -> " + sucSau + ", ve -> " + (o2 == null ? "?" : o2[1]) + " " + b.chuTu(m));
        }
        Thread.sleep(2500);
        try (BotChucNang.Bot b = vaoLai(u)) {
            b.gui(-119, (byte) 0);
            b.bom(1000);
            int[] o = oThu(b);
            BotChucNang.ket("qa_ve_o_thu.dang_nhap_lai", o != null && o[0] == sucSau, "o sau dang nhap lai " + (o == null ? "?" : o[0]) + " (mong doi " + sucSau + ")");
        }
    }

    /** Co (bit0 khoa, bit1 khong tha duoc) tung dong thu cuoi trong phan noi them goi 16 loai 2 cuoi cung; null = khong co. */
    static int[] coThu(BotChucNang.Bot b) {
        int[] kq = null;
        for (int i = 0; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != 16 || e.d == null || e.d.length < 2 || e.d[0] != 2) continue;
            try {
                java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(e.d));
                r.readByte();
                r.readInt();
                int na = r.readByte();
                for (int k = 0; k < na; k++) { r.readShort(); r.readByte(); r.readByte(); r.readByte(); r.readUTF(); r.readUTF(); r.readByte(); }
                int np = r.readByte();
                for (int k = 0; k < np; k++) { r.readShort(); r.readByte(); r.readByte(); r.readUTF(); r.readByte(); r.readUTF(); r.readInt(); r.readByte(); }
                r.readInt();
                if (r.available() < 20) continue;
                r.readByte();
                for (int k = 0; k < 5; k++) r.readShort();
                r.readInt();
                r.readInt();
                int n = r.readByte();
                int[] co = new int[n];
                for (int k = 0; k < n; k++) { co[k] = r.readByte(); r.readInt(); r.readShort(); r.readInt(); r.readUTF(); }
                kq = co;
            } catch (java.io.IOException ignored) {
            }
        }
        return kq;
    }

    /**
     * Quan ly thu: 3 thu cuoi, khoa con dau, gui THA ca 3 HAI LAN lien (bam dup): con khoa con, 2 con kia di mot lan
     * (log THATHU dung 2 dong, xu lan 2 khong doi); dang nhap lai: khoa van giu, thu da tha khong quay lai.
     */
    static void quanLyThu() throws Exception {
        String u;
        int idKhoa;
        try (BotChucNang.Bot b = KichBanDS3.botLang(30)) {
            u = b.user;
            for (int i = 0; i < 3; i++) b.gm("ngua 0", 900);
            b.gui(-119, (byte) 0);
            b.bom(1000);
            if (b.thu.size() < 3) { BotChucNang.ket("qa_quan_ly_thu", false, "GM ngua khong tao du 3 thu: " + b.thu.size()); return; }
            List<int[]> ds = new ArrayList<>(b.thu);
            idKhoa = ds.get(0)[0];
            b.gui(-119, (byte) 2, (byte) 0, (short) idKhoa, (byte) 1);
            b.bom(800);
            b.lamMoi();
            long xu0 = b.xu;
            Object[] f = new Object[2 + 2 * ds.size()];
            f[0] = (byte) 1;
            f[1] = (byte) ds.size();
            for (int i = 0; i < ds.size(); i++) { f[2 + 2 * i] = (byte) 0; f[3 + 2 * i] = (short) ds.get(i)[0]; }
            int m = b.moc();
            b.gui(-119, f);
            b.gui(-119, f); // bam dup
            b.bom(1500);
            b.lamMoi();
            long xu1 = b.xu;
            boolean conKhoa = b.thu.stream().anyMatch(t -> t[0] == idKhoa);
            int conLai = b.thu.size();
            int nDaTha = 0;
            for (int i = m; i < b.su.size(); i++) if (b.su.get(i).chu != null && b.su.get(i).chu.contains("Đã thả")) nDaTha++;
            BotChucNang.ket("qa_quan_ly_thu.tha_khoa_dup", conKhoa && conLai == 1 && nDaTha == 1,
                    "thu " + ds.size() + " -> " + conLai + ", con khoa=" + conKhoa + ", cau 'Đã thả' x" + nDaTha + ", xu " + xu0 + " -> " + xu1 + " " + b.chuTu(m));
            b.gui(-119, f);
            b.bom(1000);
            b.lamMoi();
            BotChucNang.ket("qa_quan_ly_thu.tha_lai_khong_xu", b.xu == xu1 && b.thu.size() == 1, "lan 3: xu " + xu1 + " -> " + b.xu + ", thu " + b.thu.size());
        }
        Thread.sleep(2500);
        try (BotChucNang.Bot b = vaoLai(u)) {
            b.gui(-119, (byte) 0);
            b.bom(1000);
            int[] co = coThu(b);
            boolean ok = b.thu.size() == 1 && co != null && co.length >= 1 && (co[0] & 1) != 0;
            BotChucNang.ket("qa_quan_ly_thu.dang_nhap_lai", ok, "thu " + b.thu.size() + (b.thu.isEmpty() ? "" : " id " + b.thu.get(0)[0]) + ", co " + (co == null ? "null" : Arrays.toString(co)));
        }
    }

    // ------------------------------------------------------------------ dang ky trong game (lenh 39 "#dk2")

    static final String[] CHU = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín", "mười"};

    static int so(String t) {
        t = t.trim().toLowerCase();
        for (int i = 0; i < CHU.length; i++) if (CHU[i].equals(t)) return i;
        return Integer.parseInt(t);
    }

    /** "Ba cộng 4 bằng mấy?" -> 7. */
    static int giai(String q) {
        String s = q.replace("bằng mấy?", "").trim().toLowerCase();
        boolean cong = s.contains(" cộng ");
        String[] p = s.split(cong ? " cộng " : " trừ ");
        return cong ? so(p[0]) + so(p[1]) : so(p[0]) - so(p[1]);
    }

    /** Goi 39 "#dk2" cuoi cung: {loai, chu...}. */
    static String[] dk2(BotChucNang.Bot b, int tu) {
        String[] kq = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != 39 || e.d == null) continue;
            try {
                java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(e.d));
                if (!"#dk2".equals(r.readUTF())) continue;
                int loai = r.readByte();
                if (loai == 0) kq = new String[]{"0", r.readUTF()};
                else if (loai == 1) kq = new String[]{"1", r.readUTF(), r.readUTF(), String.valueOf(r.readBoolean())};
                else {
                    int n = r.readByte();
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < n; k++) sb.append(r.readUTF()).append('=').append(r.readUTF()).append("; ");
                    kq = new String[]{"2", sb.toString(), r.readUTF()};
                }
            } catch (java.io.IOException ignored) {
            }
        }
        return kq;
    }

    /** Dang ky trong game bang dung giao thuc client (cau do), roi dang nhap tai khoan moi, tao nhan vat, vao game. */
    static void dangKyTrongGame() throws Exception {
        String ten = "qx";
        for (long v = System.currentTimeMillis(), i = 0; i < 6; i++, v /= 26) ten += (char) ('b' + (int) (v % 24)); // chi chu (tranh bad_word "ad", "dm"...)
        String may = "qa-may-" + ten;
        try (BotChucNang.Bot b = new BotChucNang.Bot(ten)) {
            b.ketNoi();
            int m = b.moc();
            b.gui(39, "#dk2", (byte) 0, may);
            b.cho(x -> dk2(x, m) != null, 5000);
            String[] q = dk2(b, m);
            if (q == null || !"0".equals(q[0])) { BotChucNang.ket("qa_dang_ky_trong_game", false, "khong nhan cau do: " + b.chuTu(m)); return; }
            Thread.sleep(2300); // tra loi sau >= 2 giay
            int m2 = b.moc();
            b.gui(39, "#dk2", (byte) 1, ten, "123456", "123456", "", String.valueOf(giai(q[1])), may);
            b.cho(x -> dk2(x, m2) != null, 6000);
            String[] k = dk2(b, m2);
            BotChucNang.ket("qa_dang_ky_trong_game.dang_ky", k != null && "1".equals(k[0]),
                    "cau do '" + q[1] + "' -> " + (k == null ? "khong tra loi " + b.chuTu(m2) : String.join(" | ", k)));
            if (k == null || !"1".equals(k[0])) return;
        }
        BotChucNang.sql("UPDATE account.team_user SET regdate = DATE_SUB(NOW(), INTERVAL 30 DAY) WHERE username='" + ten + "'");
        try (BotChucNang.Bot b = new BotChucNang.Bot(ten)) {
            String l = b.dangNhap("123456");
            if (l == null) l = b.vaoGame(0);
            BotChucNang.ket("qa_dang_ky_trong_game.vao_game", l == null && b.mapId >= 0, l == null ? "map " + b.mapId + " lv " + b.lv + " xu " + b.xu : l);
        }
    }

    /**
     * Ten nhan vat pho bien bi bad_word chan theo CHUOI CON ("ngu" trong Nguyen, "phong", "ad" trong Shadow / Gyarados):
     * ghi lai ket qua (de xuat cho chu game, KHONG phai FAIL cua kich ban).
     */
    static void tenPhoBien() throws Exception {
        String tk = BotChucNang.tkThuong();
        BotChucNang.sql("DELETE c FROM tob_char c JOIN account.team_user u ON u.id = c.userId WHERE u.username = '" + tk + "'");
        try (BotChucNang.Bot b = new BotChucNang.Bot(tk)) {
            String l = b.dangNhap("123456");
            if (l != null) { BotChucNang.ket("qa_ten_pho_bien", false, l); return; }
            StringBuilder sb = new StringBuilder();
            for (String ten : new String[]{"Nguyenvu", "Phongvu", "Shadow", "Gyarados", "Kadabra", "Tayson", "Bichvan"}) {
                int m = b.moc();
                b.gui(14, ten, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
                b.cho(x -> x.coLenh(m, 13) || x.doiChu(m, 0, "") != null, 4000);
                boolean ok = b.dsTen.contains(ten);
                sb.append(ten).append(ok ? "=OK " : "=chan(" + b.chuTu(m).trim() + ") ");
                if (ok) break;
                Thread.sleep(1200);
            }
            BotChucNang.ket("qa_ten_pho_bien", null, sb.toString());
        }
    }
}
