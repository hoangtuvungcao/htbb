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
        BotChucNang.dk("qa_moba_chua_mo", true, KichBanQaToanGame::mobaChuaMo);
        BotChucNang.dk("qa_khoa_popup", false, KichBanQaToanGame::khoaPopup);
        BotChucNang.dk("qa_cap_nhat", false, KichBanQaToanGame::capNhat);
    }

    /** Dang nhap tai khoan thuong voi so ban client {@code ban}: {loai -115 (-1 = khong co), banMoi, banToiThieu, vao duoc (1/0)}. */
    static int[] dangNhapBan(int ban) throws Exception {
        try (BotChucNang.Bot b = new BotChucNang.Bot(BotChucNang.tkThuong())) {
            b.ketNoi();
            int m = b.moc();
            b.gui(1, b.user, "123456", "2.7.6", "nokia/1/1", "0", "0", "0", (byte) 4, (short) 800, (byte) 0, (byte) 0, "botcn-" + b.user, ban);
            b.cho(x -> x.coLenh(m, 13) || x.coLenh(m, -115) && x.coLenh(m, 2) || x.dut, 6000);
            b.bom(800);
            int[] kq = {-1, 0, 0, b.coLenh(m, 13) ? 1 : 0};
            for (int i = m; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd != -115 || e.d == null) continue;
                java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(e.d));
                kq[0] = r.readByte();
                kq[1] = r.readInt();
                for (int k = 0; k < 6; k++) r.readUTF();
                kq[2] = r.readInt();
            }
            return kq;
        }
    }

    /** Dat cau_hinh roi doi may chu nap lai (<= 70 giay) toi khi dangNhapBan(ban) cho loai mong doi. */
    static int[] doiCauHinh(int ban, int loai) throws Exception {
        int[] kq = null;
        for (int i = 0; i < 9; i++) {
            kq = dangNhapBan(ban);
            if (kq[0] == loai) return kq;
            Thread.sleep(9000);
        }
        return kq;
    }

    /**
     * Viec QA #6: hop cap nhat khi ban may chu &gt; ban client, dau-cuoi qua cau_hinh song (khong khoi dong lai):
     * sv.banMoiNhat = 999 -&gt; client 913 nhan -115 NHAC (1) va VAN vao duoc; sv.banToiThieu = 950 -&gt; -115 BAT_BUOC (2),
     * khong vao duoc; ban client 999 khong nhan gi. Tra lai cau hinh cu sau khi kiem.
     */
    static void capNhat() throws Exception {
        if (BotChucNang.DB.isEmpty()) { BotChucNang.ket("qa_cap_nhat", null, "can -Ddb"); return; }
        String cuMoi = BotChucNang.sql1("SELECT gia_tri FROM cau_hinh WHERE khoa = 'sv.banMoiNhat'");
        String cuToi = BotChucNang.sql1("SELECT gia_tri FROM cau_hinh WHERE khoa = 'sv.banToiThieu'");
        try {
            BotChucNang.sql("INSERT INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES ('sv.banMoiNhat', '999', 'qa', NOW()) ON DUPLICATE KEY UPDATE gia_tri = '999', sua_luc = NOW()");
            int[] a = doiCauHinh(913, 1);
            BotChucNang.ket("qa_cap_nhat.nhac", a[0] == 1 && a[1] == 999 && a[3] == 1, "loai " + a[0] + " ban moi " + a[1] + " vao " + a[3]);
            int[] c = dangNhapBan(999);
            BotChucNang.ket("qa_cap_nhat.ban_moi_khong_nhac", c[0] == -1 && c[3] == 1, "client 999: loai " + c[0] + " vao " + c[3]);
            BotChucNang.sql("INSERT INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES ('sv.banToiThieu', '950', 'qa', NOW()) ON DUPLICATE KEY UPDATE gia_tri = '950', sua_luc = NOW()");
            int[] b = doiCauHinh(913, 2);
            BotChucNang.ket("qa_cap_nhat.bat_buoc", b[0] == 2 && b[2] == 950 && b[3] == 0, "loai " + b[0] + " toi thieu " + b[2] + " vao " + b[3]);
        } finally {
            BotChucNang.sql(cuMoi == null || cuMoi.isEmpty() ? "DELETE FROM cau_hinh WHERE khoa = 'sv.banMoiNhat'"
                    : "UPDATE cau_hinh SET gia_tri = '" + cuMoi + "' WHERE khoa = 'sv.banMoiNhat'");
            BotChucNang.sql(cuToi == null || cuToi.isEmpty() ? "DELETE FROM cau_hinh WHERE khoa = 'sv.banToiThieu'"
                    : "UPDATE cau_hinh SET gia_tri = '" + cuToi + "' WHERE khoa = 'sv.banToiThieu'");
            int[] d = doiCauHinh(913, -1);
            BotChucNang.ket("qa_cap_nhat.tra_lai", d[0] == -1 && d[3] == 1, "sau khi tra cau hinh: loai " + d[0] + " vao " + d[3]);
        }
    }

    /** Goi -32 (popup) id {@code id} tu moc: {chu} hoac null. */
    static String popup(BotChucNang.Bot b, int tu, int id) {
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -32 || e.d == null) continue;
            try {
                java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(e.d));
                r.readShort();
                if (r.readByte() == id) return r.readUTF();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /** Dich cua cong {@code g} cua ban do {@code mapId} theo bang 105 cua bot: {map, x, y} hoac null (nhu BotXuyenSuot.canh). */
    static int[] dichCong(BotChucNang.Bot b, int mapId, int[] g) {
        if (b.viTriMap == null || b.maIdNhom == null) return null;
        for (int i = 0; i < b.maIdNhom.length; i++) {
            for (int k = 0; k < b.maIdNhom[i].length; k++) {
                if (b.maIdNhom[i][k] != mapId) continue;
                int n = b.idArr[i];
                if (n < 0 || n >= b.viTriMap.length || g[2] >= b.viTriMap[n].length) return null;
                short[] loc = b.viTriMap[n][g[2]];
                int kk = Math.min(k, loc.length / 3 - 1);
                if (loc[kk * 3] < 0) kk = 0;
                return loc[kk * 3] < 0 ? null : new int[]{loc[kk * 3], loc[kk * 3 + 1], loc[kk * 3 + 2]};
            }
        }
        return null;
    }

    /** Di bo toi o truoc cong sang {@code toM} roi buoc qua (goi 12); {moc truoc goi 12, moc truoc -500} hoac null. */
    static int[] quaCong(BotChucNang.Bot b, BotChucNang.BanDo bd, int toM) throws Exception {
        for (int[] g : bd.cong) {
            int[] d = dichCong(b, b.mapId, g);
            if (d == null || d[0] != toM) continue;
            int[] dung = bd.oTruocCong(g[0], g[1]);
            if (dung == null || !b.diDuong(bd, dung[0], dung[1])) continue;
            int m = b.moc();
            b.gui(12, (short) d[0], (short) d[1], (short) d[2]);
            b.cho(x -> x.coLenh(m, 12) || x.dut, 6000);
            int m500 = b.moc();
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(1500);
            b.px = b.tx * 16 + 8;
            b.py = b.ty * 16 + 8;
            return new int[]{m, m500};
        }
        return null;
    }

    /**
     * Viec QA #6 (chua kiem dau-cuoi): nhan vat THUONG (khong GM, khong duoc mien khoa) DI BO qua cong Pallet -&gt;
     * Cerulean (khoa: cua ai CA-01.2 + cap 3). Mong doi: o lai Pallet; hop 95 "Nhận cửa ải ngay?" toi SAU khi client
     * bao nap lai map (-500); "Đồng ý" nhan cua ai; buoc lai ngay trong 10 giay chi co the nhanh (khong hop moi); sau
     * 10 giay hop hien lai ("Xem tiến độ?").
     */
    static void khoaPopup() throws Exception {
        // tai khoan + nhan vat MOI (khong GM): roi san 105 ve Pewter, DI BO qua cong sang Pallet roi toi cong Cerulean
        // cap 3 (dieu kien nhan cua ai CA-01.2 - cap 1 chi co the nhanh "Cần đạt cấp 3 để nhận", khong hop): mot bot GM
        // KHAC dat cap cho nhan vat thu (chuan bi); nhan vat thu van la nguoi choi thuong, tu di bo, khong duoc mien khoa
        BotChucNang.Bot dung = KichBanHuongDan.botMoiTinh("kp");
        KichBanHuongDan.roiSan(dung);
        try (BotChucNang.Bot gm = BotChucNang.botMoi(0)) {
            gm.gm("slv " + dung.ten + " 5 0", 1500);
        }
        dung.bom(1500);
        dung.lamMoi();
        dung.px = dung.tx * 16 + 8;
        dung.py = dung.ty * 16 + 8;
        if (dung.lv < 3) { BotChucNang.ket("qa_khoa_popup.cap", false, "slv khong doi cap: lv " + dung.lv); dung.close(); return; }
        try (BotChucNang.Bot b = dung) {
            String da = "lv " + b.lv;
            if (b.mapId != 1) {
                BotChucNang.BanDo bd0 = BotChucNang.BanDo.nap(b.mapLoad);
                int tu = b.mapId;
                if (bd0 == null || quaCong(b, bd0, 1) == null || b.mapId != 1) {
                    BotChucNang.ket("qa_khoa_popup.vao", false, "khong di bo tu map " + tu + " sang Pallet: map " + b.mapId);
                    return;
                }
                b.px = b.tx * 16 + 8;
                b.py = b.ty * 16 + 8;
            }
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            b.bom(1000);
            int[] m = quaCong(b, bd, 4);
            if (m == null) { BotChucNang.ket("qa_khoa_popup.di_bo", false, "khong di bo toi cong sang Cerulean tu " + b.px / 16 + "," + b.py / 16); return; }
            BotChucNang.ket("qa_khoa_popup.o_lai", b.mapId == 1, "sau khi buoc qua cong: map " + b.mapId);
            // hop phai toi SAU khi client bao nap lai map xong (-500): GameScr.onMap dong moi hop dang mo
            String hop = popup(b, m[1], 95);
            boolean truoc500 = false;
            for (int i = m[0]; i < m[1]; i++) if (b.su.get(i).cmd == -32) truoc500 = true;
            BotChucNang.ket("qa_khoa_popup.hop_sau_500", hop != null && !truoc500, hop == null ? "khong co hop 95: " + b.chuTu(m[0]) : hop.replace('\n', ' '));
            BotChucNang.ket("qa_khoa_popup.hoi_nhan", hop != null && hop.contains("Nhận cửa ải ngay"), String.valueOf(hop));
            int md = b.moc();
            b.gui(-32, (short) b.actor, (byte) 95, "", (byte) 1); // Đồng ý
            b.bom(1500);
            String c = b.chuTu(md);
            BotChucNang.ket("qa_khoa_popup.dong_y", !c.isEmpty(), c);
            // buoc lai ngay (trong 10 giay): khong hop moi. Dong ho cho tinh tu lan bi chan dau (luc goi 12 dau); bot di bo lai
            // toi cong mat vai giay - qua 10 giay thi hop hien lai la DUNG (khong kiem duoc, BO QUA)
            long chan0 = b.su.get(m[0]).luc;
            int[] m2 = quaCong(b, bd, 4);
            String hop2 = m2 == null ? null : popup(b, m2[0], 95);
            long troi = m2 == null ? 0 : b.su.get(m2[0]).luc - chan0;
            BotChucNang.ket("qa_khoa_popup.cho_10s", m2 != null && troi >= 10000 ? null : Boolean.valueOf(m2 != null && hop2 == null && b.mapId == 1),
                    (m2 == null ? "khong buoc lai duoc" : "sau " + troi + " ms: hop2=" + hop2 + " | " + b.chuTu(m2[0])));
            Thread.sleep(10500);
            int[] m3 = quaCong(b, bd, 4);
            String hop3 = m3 == null ? null : popup(b, m3[0], 95);
            BotChucNang.ket("qa_khoa_popup.hien_lai", hop3 != null, "sau 10 giay: " + (hop3 == null ? (m3 == null ? "khong buoc lai duoc" : b.chuTu(m3[0])) : hop3.replace('\n', ' ')) + " (" + da + ")");
        }
    }

    /**
     * Bao cao QA #10: GM "mto" vao chien truong MOBA (map 40) khi chua co tran -&gt; truoc nem ArrayIndexOutOfBounds o
     * MapChienTruongMoba.playerJoin (bot ket o ban do "ma"); nay may chu tu choi, bao ro, bot o lai ban do cu va van
     * choi tiep duoc.
     */
    static void mobaChuaMo() throws Exception {
        try (BotChucNang.Bot b = BotChucNang.botMoi(0)) {
            KichBanDS.veLang(b);
            int mapTruoc = b.mapId;
            int m = b.moc();
            b.gui(27, "mto 40 20 20");
            b.bom(2500);
            String c = b.chuTu(m);
            BotChucNang.ket("qa_moba_chua_mo.bao", c.contains("Chiến trường chưa mở trận"), c);
            b.lamMoi();
            BotChucNang.ket("qa_moba_chua_mo.o_lai", b.mapId == mapTruoc, "map " + mapTruoc + " -> " + b.mapId);
            String tep = System.getProperty("log", "");
            String log = tep.isEmpty() || !new java.io.File(tep).isFile() ? "" : new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(tep)), java.nio.charset.StandardCharsets.UTF_8);
            BotChucNang.ket("qa_moba_chua_mo.khong_loi", !log.contains("MapChienTruongMoba.playerJoin"), "log server khong co loi playerJoin");
            boolean diDuoc = b.den(1, 30, 30);
            BotChucNang.ket("qa_moba_chua_mo.di_tiep", diDuoc, "mto 1 30 30 sau do: map " + b.mapId);
        }
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
     * Ten nhan vat (de xuat QA #7, sua 0.9.14 - LocTuCam): ten that pho bien PHAI tao duoc (truoc bi bad_word chan theo
     * CHUOI CON: "ngu" trong Nguyen, "phong", "tay", "ad" trong Shadow / Gyarados / Kadabra); ten tuc / bien the PHAI bi
     * chan voi cau bao noi ro phan bi cam ("Tên có từ không phù hợp: «…». Hãy chọn tên khác.").
     */
    static void tenPhoBien() throws Exception {
        String tk = BotChucNang.tkThuong();
        StringBuilder sb = new StringBuilder();
        boolean hetOk = true;
        for (String ten : new String[]{"Nguyenvu", "Phongvu", "Shadow", "Gyarados", "Kadabra", "Tayson", "Bichvan"}) {
            String[] kq = taoTen(tk, ten);
            boolean ok = kq[0] != null;
            hetOk &= ok;
            sb.append(ten).append(ok ? "=OK " : "=chan(" + kq[1] + ") ");
        }
        BotChucNang.ket("qa_ten_pho_bien.ten_that", hetOk, sb.toString());
        sb.setLength(0);
        boolean hetChan = true;
        for (String ten : new String[]{"ThangNgu", "Ditme", "D1tme99", "Vcl2k", "Admin01"}) {
            String[] kq = taoTen(tk, ten);
            boolean chan = kq[0] == null && kq[1].contains("Tên có từ không phù hợp: «");
            hetChan &= chan;
            sb.append(ten).append(chan ? "=chan(" + kq[1] + ") " : "=LOT ");
        }
        BotChucNang.ket("qa_ten_pho_bien.ten_tuc", hetChan, sb.toString());
        BotChucNang.sql("DELETE c FROM tob_char c JOIN account.team_user u ON u.id = c.userId WHERE u.username = '" + tk + "'");
    }

    /** Tao nhan vat {@code ten} bang phien moi (tai khoan da xoa het nhan vat): {ten neu tao duoc, chu server bao}. */
    static String[] taoTen(String tk, String ten) throws Exception {
        BotChucNang.sql("DELETE c FROM tob_char c JOIN account.team_user u ON u.id = c.userId WHERE u.username = '" + tk + "'");
        BotChucNang.sql("DELETE FROM tob_char WHERE charname = '" + ten.toLowerCase() + "'");
        try (BotChucNang.Bot b = new BotChucNang.Bot(tk)) {
            String l = b.dangNhap("123456");
            if (l != null) return new String[]{null, "dang nhap: " + l};
            int m = b.moc();
            b.gui(14, ten, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
            b.cho(x -> x.coLenh(m, 13) || x.doiChu(m, 0, "") != null, 4000);
            boolean ok = b.dsTen.stream().anyMatch(t -> t.equalsIgnoreCase(ten));
            return new String[]{ok ? ten : null, b.chuTu(m).trim()};
        } finally {
            Thread.sleep(1200);
        }
    }
}
