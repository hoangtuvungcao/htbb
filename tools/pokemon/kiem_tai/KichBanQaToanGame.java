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
            int bo = 0;
            for (int[] d : new ArrayList<>(b.doDung)) {
                if (d[1] != 1 || bo >= 3) continue;
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
        final long a = 0x0123456789L, c2 = 0x0FEDCBA98L;
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
}
