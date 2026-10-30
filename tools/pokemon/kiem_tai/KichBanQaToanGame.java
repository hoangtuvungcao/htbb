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

    /** Dang nhap tai khoan thuong voi so ban client {@code ban} (platf J2ME): {loai -115 (-1 = khong co), banMoi, banToiThieu, vao duoc (1/0)}. */
    static int[] dangNhapBan(int ban) throws Exception {
        Object[] g = goi115(ban, "nokia/1/1");
        return (int[]) g[0];
    }

    /**
     * Dang nhap tai khoan thuong voi so ban + platf ({@code nokia/1/1} = jar, {@code Android/14}, {@code HTBB-PC/Windows 10},
     * {@code HTBB-PC/Linux} - nhu CapNhatClient.nenTang): {int[]{loai, banMoi, banToiThieu, vao}, String[]{phienBan, url,
     * sha256, trangTai}}.
     */
    static Object[] goi115(int ban, String platf) throws Exception {
        try (BotChucNang.Bot b = new BotChucNang.Bot(BotChucNang.tkThuong())) {
            b.ketNoi();
            int m = b.moc();
            b.gui(1, b.user, "123456", "2.7.6", platf, "0", "0", "0", (byte) 4, (short) 800, (byte) 0, (byte) 0, "botcn-" + b.user, ban);
            b.cho(x -> x.coLenh(m, 13) || x.coLenh(m, -115) && x.coLenh(m, 2) || x.dut, 6000);
            b.bom(800);
            int[] kq = {-1, 0, 0, b.coLenh(m, 13) ? 1 : 0};
            String[] chu = {"", "", "", ""};
            for (int i = m; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd != -115 || e.d == null) continue;
                java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(e.d));
                kq[0] = r.readByte();
                kq[1] = r.readInt();
                chu[0] = r.readUTF();
                chu[1] = r.readUTF();
                chu[2] = r.readUTF();
                r.readUTF();
                r.readUTF();
                chu[3] = r.readUTF();
                kq[2] = r.readInt();
            }
            return new Object[]{kq, chu};
        }
    }

    /** Dat cau_hinh roi doi may chu nap lai (<= 70 giay) toi khi dangNhapBan(ban) cho loai mong doi. */
    static int[] doiCauHinh(int ban, int loai) throws Exception {
        return doiCauHinh(ban, loai, -1);
    }

    /** Nhu tren, them dieu kien ban moi (-1 = bat ky) - danh muc tai game nap lai moi 60 giay, cau_hinh moi ~5 giay. */
    static int[] doiCauHinh(int ban, int loai, int banMoi) throws Exception {
        int[] kq = null;
        for (int i = 0; i < 10; i++) {
            kq = dangNhapBan(ban);
            if (kq[0] == loai && (banMoi < 0 || kq[1] == banMoi)) return kq;
            Thread.sleep(9000);
        }
        return kq;
    }

    static final String KHOA_LUU_TAI = "qa.taiGame.luu";
    static final String KHONG_CO = "<<qa:khong-co-dong>>";

    /** Tra danh muc tai game (web_cai_dat 'taiGame') tu ban luu {@link #KHOA_LUU_TAI} (an toan khi chay lai sau lan bi ngat). */
    static void traDanhMucTai() {
        String luu = BotChucNang.sql1("SELECT LEFT(gia_tri, 40) FROM web_cai_dat WHERE khoa = '" + KHOA_LUU_TAI + "'");
        if (luu == null) return;
        if (luu.startsWith(KHONG_CO)) {
            BotChucNang.sql("DELETE FROM web_cai_dat WHERE khoa = 'taiGame'");
        } else {
            BotChucNang.sql("INSERT INTO web_cai_dat (khoa, gia_tri, cap_nhat_luc, cap_nhat_boi) SELECT 'taiGame', gia_tri, NOW(), 'qa' FROM web_cai_dat"
                    + " WHERE khoa = '" + KHOA_LUU_TAI + "' ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), cap_nhat_luc = NOW()");
        }
        BotChucNang.sql("DELETE FROM web_cai_dat WHERE khoa = '" + KHOA_LUU_TAI + "'");
    }

    /**
     * Viec QA #6: hop cap nhat khi ban may chu &gt; ban client, dau-cuoi qua cau_hinh song (khong khoi dong lai).
     * <ol>
     *   <li>{@code tai.<nen tang>}: link tep MAY CHU QUANG CAO trong -115 (danh muc tai game THAT + sv.gocTaiCapNhat) tai
     *       duoc bang client KHONG phai trinh duyet (User-Agent bo cap nhat PC) - HTTP 200 + sha256 khop (WAF tra 403
     *       cho ten mien, 25/10/2026). Khong co mang (khong phan giai ten / khong co duong) -&gt; BO QUA.</li>
     *   <li>Danh muc tai game (web_cai_dat 'taiGame') TAM dat rong - du lieu production (ban sao staging) co so ban
     *       tung nen tang (vd jar 918) THANG sv.banMoiNhat (cong kiem 25/10: "loai 1 ban moi 918"). Roi sv.banMoiNhat=999
     *       -&gt; client 913 nhan -115 NHAC (1, ban moi 999) va VAN vao duoc; client 999 khong nhan gi; sv.banToiThieu=950
     *       -&gt; BAT_BUOC (2), khong vao duoc.</li>
     *   <li>Tra lai cau hinh + danh muc cu: client 913 nhan DUNG nhu truoc khi kiem (ban dau, khong gia dinh "khong co gi").</li>
     * </ol>
     */
    static void capNhat() throws Exception {
        if (BotChucNang.DB.isEmpty()) { BotChucNang.ket("qa_cap_nhat", null, "can -Ddb"); return; }
        traDanhMucTai();   // lan truoc bi ngat giua chung
        String[] khoa = {"sv.banMoiNhat", "sv.banToiThieu", "sv.nhacCapNhat"};
        String[] cu = new String[khoa.length];
        for (int i = 0; i < khoa.length; i++) cu[i] = BotChucNang.sql1("SELECT gia_tri FROM cau_hinh WHERE khoa = '" + khoa[i] + "'");
        int[] dau = dangNhapBan(913);
        try {
            datCauHinh("sv.nhacCapNhat", "1");
            datCauHinh("sv.banToiThieu", "0");
            Thread.sleep(7000);   // cau hinh trung tam hoi phien ban moi 5 giay
            taiTep();
            // danh muc tai game rong -> chi con sv.banMoiNhat quyet dinh (giu ban luu TRONG CSDL, khong qua chuoi Java)
            String coTai = BotChucNang.sql1("SELECT COUNT(*) FROM web_cai_dat WHERE khoa = 'taiGame'");
            if ("0".equals(coTai)) {
                BotChucNang.sql("INSERT INTO web_cai_dat (khoa, gia_tri, cap_nhat_boi) VALUES ('" + KHOA_LUU_TAI + "', '" + KHONG_CO + "', 'qa')");
            } else {
                BotChucNang.sql("INSERT INTO web_cai_dat (khoa, gia_tri, cap_nhat_boi) SELECT '" + KHOA_LUU_TAI + "', gia_tri, 'qa' FROM web_cai_dat WHERE khoa = 'taiGame'");
            }
            BotChucNang.sql("INSERT INTO web_cai_dat (khoa, gia_tri, cap_nhat_boi) VALUES ('taiGame', '{\"ban\":[]}', 'qa') ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), cap_nhat_luc = NOW()");
            datCauHinh("sv.banMoiNhat", "999");
            int[] a = doiCauHinh(913, 1, 999);
            BotChucNang.ket("qa_cap_nhat.nhac", a[0] == 1 && a[1] == 999 && a[3] == 1, "loai " + a[0] + " ban moi " + a[1] + " vao " + a[3]);
            int[] c = dangNhapBan(999);
            BotChucNang.ket("qa_cap_nhat.ban_moi_khong_nhac", c[0] == -1 && c[3] == 1, "client 999: loai " + c[0] + " vao " + c[3]);
            datCauHinh("sv.banToiThieu", "950");
            int[] b = doiCauHinh(913, 2);
            BotChucNang.ket("qa_cap_nhat.bat_buoc", b[0] == 2 && b[2] == 950 && b[3] == 0, "loai " + b[0] + " toi thieu " + b[2] + " vao " + b[3]);
        } finally {
            for (int i = 0; i < khoa.length; i++) {
                BotChucNang.sql(cu[i] == null ? "DELETE FROM cau_hinh WHERE khoa = '" + khoa[i] + "'"
                        : "UPDATE cau_hinh SET gia_tri = '" + cu[i].replace("'", "''") + "', sua_luc = NOW() WHERE khoa = '" + khoa[i] + "'");
            }
            BotChucNang.sql("UPDATE cau_hinh_phien_ban SET phien_ban = phien_ban + 1 WHERE id = 1");
            traDanhMucTai();
            int[] d = doiCauHinh(913, dau[0], dau[1] == 0 ? -1 : dau[1]);
            BotChucNang.ket("qa_cap_nhat.tra_lai", d[0] == dau[0] && d[3] == dau[3],
                    "sau khi tra cau hinh: loai " + d[0] + " ban moi " + d[1] + " vao " + d[3] + " (truoc khi kiem: loai " + dau[0] + " ban moi " + dau[1] + " vao " + dau[3] + ")");
        }
    }

    static void datCauHinh(String khoa, String gt) {
        BotChucNang.sql("INSERT INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES ('" + khoa + "', '" + gt + "', 'qa', NOW())"
                + " ON DUPLICATE KEY UPDATE gia_tri = '" + gt + "', sua_luc = NOW()");
        BotChucNang.sql("UPDATE cau_hinh_phien_ban SET phien_ban = phien_ban + 1 WHERE id = 1");
    }

    /** User-Agent cua bo cap nhat PC (client_jar shared/se/htbb/capnhat/TaiVe) - client KHONG phai trinh duyet. */
    static final String UA_CAP_NHAT = "HTBB-CapNhat/1";

    /**
     * qa_cap_nhat.tai.&lt;nen tang&gt;: client ban 907 (ban dau tien biet -115) moi nen tang; may chu bao ban moi (danh muc
     * that, sv.banMoiNhat=0 / nguong that) thi link tep trong -115 phai tai duoc nhu client that tai: khong cookie, khong
     * JS, User-Agent bo cap nhat; theo chuyen huong (toi da 5); HTTP 200; sha256 khop goi -115.
     */
    static void taiTep() throws Exception {
        String[][] nt = {{"jar", "nokia/1/1"}, {"android", "Android/14"}, {"windows", "HTBB-PC/Windows 10"}, {"linux", "HTBB-PC/Linux"}};
        for (String[] n : nt) {
            String ten = "qa_cap_nhat.tai." + n[0];
            Object[] g = goi115(907, n[1]);
            int[] kq = (int[]) g[0];
            String[] chu = (String[]) g[1];
            if (kq[0] < 0) { BotChucNang.ket(ten, null, "khong co -115 cho client 907 (" + n[1] + "): danh muc tai game chua co ban moi hon"); continue; }
            String url = chu[1], sha = chu[2];
            if (url.isEmpty()) { BotChucNang.ket(ten, null, "-115 khong co link tep (chi trang tai " + chu[3] + ")"); continue; }
            if (sha.isEmpty()) { BotChucNang.ket(ten, null, url + ": danh muc khong ghi sha256 -> client chi mo trinh duyet, khong tu tai"); continue; }
            // Doi chieu voi tep THAT tren may (thu muc web /tai/ = HTBB_TAI_GAME_DIR, VPS: /opt/htbb/downloads + SHA256SUMS.txt).
            // Cong kiem 25/10: CSDL staging chep tu ban sao luu prod CU van quang cao 0.9.18 trong khi thu muc chi con 0.9.20
            // -> 404. Danh muc CU (ban < ban hien hanh trong SHA256SUMS) = loi MOI TRUONG: bao ro + kiem tep ban hien hanh qua
            // cung duong. Danh muc quang cao ban hien hanh (hoac moi hon) ma tep thieu / sha khac = loi THAT (FAIL nhu cu).
            String tep = tenTep(url);
            Map<String, String> sums = sumsTai();
            if (sums != null && tep != null && !sums.containsKey(tep)) {
                String banQc = banTrongTen(tep), banHh = banMoiNhat(sums.keySet());
                if (banQc != null && banHh != null && soSanhBan(banQc, banHh) < 0) {
                    BotChucNang.ket(ten, null, "CANH BAO MOI TRUONG: danh muc tai game (web_cai_dat.taiGame) cua CSDL nay CU - quang cao "
                            + banQc + " (" + tep + ") nhung " + THU_MUC_TAI + " chi co ban hien hanh " + banHh
                            + " (CSDL staging lay tu ban sao luu prod cu? lay ban sao luu moi / cap nhat danh muc tren web admin)");
                    String tepHh = tep.replace(banQc, banHh);
                    if (sums.containsKey(tepHh)) {
                        String[] g2 = {""};
                        Boolean ok2 = kiemTai(url.substring(0, url.lastIndexOf('/') + 1) + tepHh, sums.get(tepHh), g2);
                        BotChucNang.ket(ten + ".hien_hanh", ok2, "tep ban hien hanh (SHA256SUMS) qua cung duong tai: " + g2[0]);
                    } else {
                        BotChucNang.ket(ten + ".hien_hanh", false, "khong co tep " + tepHh + " (ban hien hanh " + banHh + ") trong " + THU_MUC_TAI);
                    }
                    continue;
                }
            }
            String[] ghi = {""};
            Boolean ok = kiemTai(url, sha, ghi);
            if (sums != null && tep != null) {
                String s2 = sums.get(tep);
                if (s2 == null) {
                    ok = false;
                    ghi[0] += " | danh muc quang cao " + tep + " nhung tep KHONG co trong " + THU_MUC_TAI + "/SHA256SUMS (ban hien hanh "
                            + banMoiNhat(sums.keySet()) + ")";
                } else if (!s2.equalsIgnoreCase(sha)) {
                    ok = false;
                    ghi[0] += " | sha256 trong -115 (" + sha + ") khac SHA256SUMS (" + s2 + ")";
                }
            }
            BotChucNang.ket(ten, ok, ghi[0]);
        }
    }

    /** Thu muc tep tai game cua web (/tai/&lt;ten&gt;): -DthuMucTai, mac dinh /opt/htbb/downloads (VPS). Khong co = bo doi chieu. */
    static final String THU_MUC_TAI = System.getProperty("thuMucTai", "/opt/htbb/downloads");

    /** SHA256SUMS(.txt) trong {@link #THU_MUC_TAI}: ten tep -&gt; sha256; null neu khong doc duoc (vd may thu). */
    static Map<String, String> sumsTai() {
        for (String ten : new String[]{"SHA256SUMS.txt", "SHA256SUMS"}) {
            java.io.File f = new java.io.File(THU_MUC_TAI, ten);
            if (!f.isFile()) continue;
            try {
                Map<String, String> m = new LinkedHashMap<>();
                for (String d : java.nio.file.Files.readAllLines(f.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
                    String[] p = d.trim().split("\\s+", 2);
                    if (p.length == 2 && p[0].matches("[0-9a-fA-F]{64}")) m.put(p[1].replaceFirst("^\\*", "").trim(), p[0]);
                }
                return m.isEmpty() ? null : m;
            } catch (java.io.IOException e) {
                return null;
            }
        }
        return null;
    }

    /** Ten tep cuoi URL (bo ?query), null neu khong co. */
    static String tenTep(String url) {
        String u = url.replaceFirst("[?#].*$", "");
        int i = u.lastIndexOf('/');
        return i < 0 || i == u.length() - 1 ? null : u.substring(i + 1);
    }

    /** Ban dang a.b.c trong ten tep, null neu khong co. */
    static String banTrongTen(String ten) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+\\.\\d+\\.\\d+)").matcher(ten);
        return m.find() ? m.group(1) : null;
    }

    /** Ban lon nhat trong cac ten tep (SHA256SUMS), null neu khong co. */
    static String banMoiNhat(Collection<String> tep) {
        String max = null;
        for (String t : tep) {
            String b = banTrongTen(t);
            if (b != null && (max == null || soSanhBan(b, max) > 0)) max = b;
        }
        return max;
    }

    static int soSanhBan(String a, String b) {
        String[] x = a.split("\\."), y = b.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int u = i < x.length ? Integer.parseInt(x[i]) : 0, v = i < y.length ? Integer.parseInt(y[i]) : 0;
            if (u != v) return Integer.compare(u, v);
        }
        return 0;
    }

    /** Tai {@code url} nhu bo cap nhat: true = 200 + sha256 khop, null = BO QUA (khong co mang); ghi[0] = chi tiet. */
    static Boolean kiemTai(String url, String sha, String[] ghi) {
        String u = url;
        try {
            for (int lan = 0; lan <= 5; lan++) {
                java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(u).openConnection();
                c.setInstanceFollowRedirects(false);
                c.setConnectTimeout(10000);
                c.setReadTimeout(30000);
                c.setRequestProperty("User-Agent", UA_CAP_NHAT);
                c.setRequestProperty("Accept-Encoding", "identity");
                int ma = c.getResponseCode();
                if (ma >= 300 && ma < 400 && c.getHeaderField("Location") != null) {
                    u = new java.net.URL(new java.net.URL(u), c.getHeaderField("Location")).toString();
                    c.disconnect();
                    continue;
                }
                if (ma != 200) {
                    String loai = String.valueOf(c.getHeaderField("Content-Type"));
                    c.disconnect();
                    ghi[0] = u + " -> HTTP " + ma + " (" + loai + ")"
                            + (ma == 403 ? " - WAF chan client khong phai trinh duyet? dat sv.gocTaiCapNhat=http://<IP origin>" : "");
                    return false;
                }
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                long dai = 0;
                try (java.io.InputStream in = c.getInputStream()) {
                    byte[] buf = new byte[65536];
                    int n;
                    while ((n = in.read(buf)) > 0) { md.update(buf, 0, n); dai += n; }
                }
                StringBuilder hex = new StringBuilder();
                for (byte x : md.digest()) hex.append(String.format("%02x", x));
                boolean khop = hex.toString().equalsIgnoreCase(sha);
                ghi[0] = u + " -> HTTP 200, " + dai + " byte, sha256 " + (khop ? "khop" : hex + " != " + sha);
                return khop;
            }
            ghi[0] = url + " -> qua 5 lan chuyen huong";
            return false;
        } catch (java.net.UnknownHostException | java.net.NoRouteToHostException e) {
            ghi[0] = url + ": khong co mang (" + e + ")";
            return null;
        } catch (Exception e) {
            ghi[0] = u + " -> " + e;
            return false;
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
            // ban sao lang (301 / 1701...) dan toi ban sao Pallet cung nhom: so theo nhom ban do, khong theo id
            if (d == null || d[0] != toM && (KichBanDS2.nhomCua(b, d[0]) < 0 || KichBanDS2.nhomCua(b, d[0]) != KichBanDS2.nhomCua(b, toM))) continue;
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
            if (b.mapLoad != 1) {
                BotChucNang.BanDo bd0 = BotChucNang.BanDo.nap(b.mapLoad);
                int tu = b.mapId;
                if (bd0 == null || quaCong(b, bd0, 1) == null || b.mapLoad != 1) {
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
            BotChucNang.ket("qa_khoa_popup.o_lai", b.mapLoad == 1, "sau khi buoc qua cong: map " + b.mapId);
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
            BotChucNang.ket("qa_khoa_popup.cho_10s", m2 != null && troi >= 10000 ? null : Boolean.valueOf(m2 != null && hop2 == null && b.mapLoad == 1),
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
        n += b.soDo(tpl); // cong so luong chong type 28 (ChongVatPham), khong dem o
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
                w.println(b.user + " " + b.xu + " " + b.soThuoc(1) + " " + a + " " + b.lv + " " + b.tongMon()); // tong mon (cong chong type 28)
            }
            BotChucNang.ket("qa_khoi_dong_lai_ghi", true, b.user + " xu " + b.xu + " thuoc1 " + b.soThuoc(1) + " lv " + b.lv + " tui " + b.tongMon());
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
                    && o[2] == Long.parseLong(f[3]) && b.lv == Integer.parseInt(f[4]) && b.tongMon() == Integer.parseInt(f[5]);
            BotChucNang.ket("qa_khoi_dong_lai_kiem", ok, "mong doi xu " + f[1] + " thuoc1 " + f[2] + " o1 " + f[3] + " lv " + f[4] + " tui " + f[5]
                    + " | thuc te xu " + b.xu + " thuoc1 " + b.soThuoc(1) + " o1 " + (o == null ? "-" : String.valueOf(o[2])) + " lv " + b.lv
                    + " tui " + b.tongMon());
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
        // ten ghep tu am tiet an toan (phu am + nguyen am): khong the tao chuoi cam nhu "xxx", "dm", "ad"... (truoc: chu ngau
        // nhien b..y doi khi ra "xxx" -> bo loc ten tu choi dung luat, nhom qa_toan_game hong vo co)
        String[] AT = {"ba", "bo", "ki", "lu", "mi", "na", "po", "ru", "so", "tu", "vi", "ke"};
        String ten = "qa";
        for (long v = System.currentTimeMillis(), i = 0; i < 4; i++, v /= AT.length) ten += AT[(int) (v % AT.length)];
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
