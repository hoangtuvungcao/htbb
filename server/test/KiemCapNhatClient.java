import com.htbb.capnhat.CapNhatClient;
import com.htbb.capnhat.CapNhatClient.Ban;
import com.htbb.capnhat.CapNhatClient.QuyetDinh;
import com.htbb.capnhat.JsonNho;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Kiem CAP NHAT CLIENT (0.9.7, com.htbb.capnhat): bo doc JSON nho, nan danh muc tai game (web_cai_dat 'taiGame'),
 * nen tang theo platf, hai nguong (ban moi nhat / ban toi thieu) cho moi truong hop, link tuyet doi, BO CUC goi -115
 * (khop client_jar game.pokemon.CapNhat), chu hop 37 cho client cu LUON co link, va cho noi vao LoginHandler.
 */
public final class KiemCapNhatClient {

    private KiemCapNhatClient() {
    }

    public static void chay() {
        kiemJson();
        kiemDanhMuc();
        kiemNenTang();
        kiemNguong();
        kiemLink();
        kiemGoi();
        kiemClientCu();
        kiemNoiVao();
    }

    static final String DANH_MUC = "{\"ban\":["
            + "{\"ma\":\"jar\",\"ban\":907,\"phienBan\":\"0.9.7\",\"url\":\"/tai/HTBB-v907.jar\",\"kieu\":\"tep\",\"sha256\":\"" + rep('a', 64)
            + "\",\"dungLuong\":\"4,1 MB\",\"nhatKy\":\"Cập nhật trong game\\nSửa lỗi\",\"trangThai\":\"san-sang\",\"hien\":true},"
            + "{\"ma\":\"android\",\"ban\":908,\"phienBan\":\"0.9.7.1\",\"url\":\"https://cdn.vd/HTBB-908.apk\",\"kieu\":\"tep\",\"sha256\":\"" + rep('B', 64)
            + "\",\"dungLuong\":\"85,3 MB\",\"nhatKy\":\"\",\"trangThai\":\"san-sang\",\"hien\":true},"
            + "{\"ma\":\"windows\",\"phienBan\":\"0.9.6\",\"url\":\"/tai/HTBB-PC-Setup-0.9.6.exe\",\"kieu\":\"tep\",\"sha256\":\"xyz\",\"trangThai\":\"san-sang\",\"hien\":true},"
            + "{\"ma\":\"linux\",\"ban\":\"907\",\"phienBan\":\"0.9.7\",\"url\":\"/tai/HTBB.AppImage\",\"kieu\":\"tep\",\"trangThai\":\"sap-ra-mat\",\"hien\":true},"
            + "{\"ma\":\"ios\",\"ban\":0,\"url\":\"https://testflight.apple.com/join/x\",\"kieu\":\"testflight\",\"trangThai\":\"san-sang\",\"hien\":true},"
            + "{\"ma\":\"jar\",\"ban\":999,\"url\":\"/tai/trung.jar\"}"
            + "]}";

    static String rep(final char c, final int n) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) {
            b.append(c);
        }
        return b.toString();
    }

    private static void kiemJson() {
        Kiem.nhom("Cap nhat client - bo doc JSON nho");
        final Object v = JsonNho.doc("{\"a\":[1,2.5,-3,true,false,null,\"x\\\"y\\n\\u1EA1\"],\"b\":{}}");
        Kiem.dung("doi tuong -> Map", v instanceof Map);
        final List<?> a = (List<?>) ((Map<?, ?>) v).get("a");
        Kiem.bang("so nguyen", Long.valueOf(1), a.get(0));
        Kiem.bang("so thuc", Double.valueOf(2.5), a.get(1));
        Kiem.bang("so am", Long.valueOf(-3), a.get(2));
        Kiem.bang("true", Boolean.TRUE, a.get(3));
        Kiem.bang("null", null, a.get(5));
        Kiem.bang("chuoi thoat + \\u", "x\"y\nạ", a.get(6));
        Kiem.bang("chuoi rong -> null", null, JsonNho.doc(""));
        final String[] hong = {"{", "[1,]", "{\"a\" 1}", "\"abc", "{\"a\":1} x", "tru", "[1 2]"};
        for (final String h : hong) {
            boolean nem = false;
            try {
                JsonNho.doc(h);
            } catch (final IllegalArgumentException e) {
                nem = true;
            }
            Kiem.dung("JSON hong bi tu choi: " + h, nem);
        }
        final StringBuilder sau = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sau.append('[');
        }
        boolean nem = false;
        try {
            JsonNho.doc(sau.toString());
        } catch (final IllegalArgumentException e) {
            nem = true;
        }
        Kiem.dung("JSON long qua sau bi tu choi (khong tran stack)", nem);
    }

    private static void kiemDanhMuc() {
        Kiem.nhom("Cap nhat client - nan danh muc tai game");
        final Map<String, Ban> dm = CapNhatClient.docDanhMuc(DANH_MUC);
        Kiem.bang("jar: so ban", 907, dm.get("jar").ban);
        Kiem.bang("jar: dong trung bi bo (giu dong dau)", "/tai/HTBB-v907.jar", dm.get("jar").url);
        Kiem.bang("jar: nhat ky xuong dong", "Cập nhật trong game\nSửa lỗi", dm.get("jar").nhatKy);
        Kiem.dung("jar: san sang", dm.get("jar").sanSang);
        Kiem.bang("android: sha256 chu hoa -> thuong", rep('b', 64), dm.get("android").sha256);
        Kiem.bang("windows: thieu so ban -> 0", 0, dm.get("windows").ban);
        Kiem.bang("windows: sha256 sai -> rong", "", dm.get("windows").sha256);
        Kiem.bang("linux: so ban dang chuoi", 907, dm.get("linux").ban);
        Kiem.sai("linux: sap ra mat -> khong san sang", dm.get("linux").sanSang);
        Kiem.sai("ios: testflight khong phai tep -> khong san sang", dm.get("ios").sanSang);
        Kiem.bang("JSON hong -> danh muc rong", 0, CapNhatClient.docDanhMuc("{ban:").size());
        Kiem.bang("khong co mang ban -> rong", 0, CapNhatClient.docDanhMuc("{\"x\":1}").size());
        Kiem.bang("ma cu pc-java -> windows", "windows",
                CapNhatClient.docDanhMuc("{\"ban\":[{\"ma\":\"pc-java\",\"url\":\"/tai/a.exe\"}]}").get("windows").ma);
        Kiem.dung("du lieu cu (khong trangThai, co link) -> san sang",
                CapNhatClient.docDanhMuc("{\"ban\":[{\"ma\":\"jar\",\"url\":\"/tai/a.jar\"}]}").get("jar").sanSang);
    }

    private static void kiemNenTang() {
        Kiem.nhom("Cap nhat client - nen tang theo platf");
        Kiem.bang("PC Windows", "windows", CapNhatClient.nenTang("HTBB-PC/Windows 11"));
        Kiem.bang("PC Linux", "linux", CapNhatClient.nenTang("HTBB-PC/Linux"));
        Kiem.bang("PC macOS: chua co ban", null, CapNhatClient.nenTang("HTBB-PC/Mac OS X"));
        Kiem.bang("Android", "android", CapNhatClient.nenTang("Android/14"));
        Kiem.bang("J2ME", "jar", CapNhatClient.nenTang("Nokia6300/05.00"));
        Kiem.bang("rong -> jar", "jar", CapNhatClient.nenTang(null));
    }

    private static QuyetDinh qd(final int ban, final String nt, final int toiThieu, final int duPhong, final boolean nhac,
            final Map<String, Ban> dm) {
        return CapNhatClient.quyetDinh(ban, nt, toiThieu, duPhong, nhac, dm);
    }

    private static void kiemNguong() {
        Kiem.nhom("Cap nhat client - hai nguong (moi nhat / toi thieu)");
        final Map<String, Ban> dm = CapNhatClient.docDanhMuc(DANH_MUC);
        Kiem.bang("ban moi nhat bang danh muc -> khong lam gi", CapNhatClient.KHONG, qd(907, "jar", 0, 0, true, dm).loai);
        Kiem.bang("ban moi hon danh muc -> khong lam gi", CapNhatClient.KHONG, qd(910, "jar", 0, 0, true, dm).loai);
        final QuyetDinh n = qd(906, "jar", 0, 0, true, dm);
        Kiem.bang("cu hon moi nhat -> NHAC", CapNhatClient.NHAC, n.loai);
        Kiem.bang("NHAC: ban moi", 907, n.banMoi);
        Kiem.bang("tat nhac -> khong", CapNhatClient.KHONG, qd(906, "jar", 0, 0, false, dm).loai);
        Kiem.bang("Unity (-1) -> khong bao gio", CapNhatClient.KHONG, qd(-1, "jar", 2000, 2000, true, dm).loai);
        Kiem.bang("Unity (-1) khong chan", CapNhatClient.KHONG, qd(-1, null, 907, 0, true, dm).loai);
        final QuyetDinh b = qd(905, "jar", 907, 0, true, dm);
        Kiem.bang("< toi thieu, danh muc da co ban dat nguong -> BAT BUOC", CapNhatClient.BAT_BUOC, b.loai);
        Kiem.bang("= toi thieu -> khong chan (chi nhac neu cu hon moi nhat)", CapNhatClient.KHONG, qd(907, "jar", 907, 0, true, dm).loai);
        Kiem.bang("toi thieu nhung tat nhac van chan", CapNhatClient.BAT_BUOC, qd(905, "jar", 907, 0, false, dm).loai);
        // android: danh muc 908; toi thieu 907 -> 906 bi chan, 907 duoc nhac
        Kiem.bang("android 906 < 907 -> BAT BUOC", CapNhatClient.BAT_BUOC, qd(906, "android", 907, 0, true, dm).loai);
        Kiem.bang("android 907 -> NHAC (908 moi hon)", CapNhatClient.NHAC, qd(907, "android", 907, 0, true, dm).loai);
        // nguong cao hon ban dang phat -> KHONG khoa ngoai, chi nhac
        final QuyetDinh v = qd(905, "jar", 950, 0, true, dm);
        Kiem.bang("nguong 950 > ban phat 907 -> khong chan, NHAC", CapNhatClient.NHAC, v.loai);
        Kiem.dung("nguong vuot ban phat -> co canh bao", v.nguongVuotBanPhat);
        // windows: danh muc khong co so ban -> du phong sv.banMoiNhat
        Kiem.bang("windows khong so ban, khong du phong -> khong", CapNhatClient.KHONG, qd(906, "windows", 0, 0, true, dm).loai);
        final QuyetDinh w = qd(906, "windows", 0, 907, true, dm);
        Kiem.bang("windows du phong 907 -> NHAC", CapNhatClient.NHAC, w.loai);
        Kiem.bang("windows du phong: link tep danh muc", "/tai/HTBB-PC-Setup-0.9.6.exe", w.ban.url);
        // khong biet ban moi (danh muc rong, khong du phong) + toi thieu -> giu cach cu: chan
        Kiem.bang("danh muc rong + toi thieu -> BAT BUOC (cach cu)", CapNhatClient.BAT_BUOC,
                qd(905, "jar", 907, 0, true, java.util.Collections.<String, Ban>emptyMap()).loai);
        Kiem.bang("danh muc null + toi thieu -> BAT BUOC", CapNhatClient.BAT_BUOC, qd(905, "jar", 907, 0, true, null).loai);
        // linux sap ra mat: khong co ban tai san sang
        final QuyetDinh l = qd(905, "linux", 0, 0, true, dm);
        Kiem.bang("linux chua san sang -> khong nhac", CapNhatClient.KHONG, l.loai);
        // macOS PC (nen tang null) khong bi chan khi co danh muc nhung van theo nguong khi khong biet ban moi
        Kiem.bang("PC macOS + du phong -> khong nhac (khong co ban)", CapNhatClient.KHONG, qd(905, null, 0, 907, true, dm).loai);
    }

    private static void kiemLink() {
        Kiem.nhom("Cap nhat client - link tuyet doi");
        Kiem.bang("/tai + goc co duong dan", "https://htbb.vd/tai/a.apk", CapNhatClient.urlTuyetDoi("/tai/a.apk", "https://htbb.vd/trang/x"));
        Kiem.bang("/tai + goc khong duong dan", "https://htbb.vd:8443/tai/a.apk", CapNhatClient.urlTuyetDoi("/tai/a.apk", "https://htbb.vd:8443"));
        Kiem.bang("https giu nguyen", "https://cdn.vd/a.apk", CapNhatClient.urlTuyetDoi("https://cdn.vd/a.apk", "https://htbb.vd/"));
        Kiem.bang("//host bi tu choi", "", CapNhatClient.urlTuyetDoi("//evil/a.apk", "https://htbb.vd/"));
        Kiem.bang("javascript: bi tu choi", "", CapNhatClient.urlTuyetDoi("javascript:alert(1)", "https://htbb.vd/"));
        Kiem.bang("goc rong -> rong", "", CapNhatClient.urlTuyetDoi("/tai/a.apk", ""));
    }

    private static void kiemGoi() {
        Kiem.nhom("Cap nhat client - bo cuc goi -115 (client game.pokemon.CapNhat.nhan)");
        final Map<String, Ban> dm = CapNhatClient.docDanhMuc(DANH_MUC);
        try {
            final QuyetDinh q = qd(905, "jar", 907, 0, true, dm);
            final byte[] goi = CapNhatClient.tao(q, "https://htbb.vd/", "https://htbb.vd/download").toByteArray();
            Kiem.bang("lenh -115", (byte) -115, goi[0]);
            final int dai = ((goi[1] & 0xFF) << 8) | (goi[2] & 0xFF);
            Kiem.bang("do dai khop", goi.length - 3, dai);
            final DataInputStream r = new DataInputStream(new ByteArrayInputStream(goi, 3, goi.length - 3));
            Kiem.bang("loai", (byte) CapNhatClient.BAT_BUOC, r.readByte());
            Kiem.bang("ban moi", 907, r.readInt());
            Kiem.bang("phien ban", "0.9.7", r.readUTF());
            Kiem.bang("link tep tuyet doi", "https://htbb.vd/tai/HTBB-v907.jar", r.readUTF());
            Kiem.bang("sha256", rep('a', 64), r.readUTF());
            Kiem.bang("dung luong", "4,1 MB", r.readUTF());
            Kiem.bang("nhat ky", "Cập nhật trong game\nSửa lỗi", r.readUTF());
            Kiem.bang("trang tai", "https://htbb.vd/download", r.readUTF());
            Kiem.bang("ban toi thieu", 907, r.readInt());
            Kiem.bang("ban client", 905, r.readInt());
            Kiem.bang("het goi", 0, r.available());
            Kiem.bang("so lenh khop LENH", -115, CapNhatClient.LENH);
            // khop client: client doc dung thu tu nay
            final String client = new String(Files.readAllBytes(Paths.get("../client_jar/src/game/pokemon/CapNhat.java")), StandardCharsets.UTF_8);
            Kiem.dung("client: LENH = -115", client.contains("LENH = -115"));
            Kiem.dung("client: doc dung thu tu (byte, int, UTF x6, int, int)", client.replaceAll("\\s+", " ").contains(
                    "loai = r.readByte(); c.banMoi = r.readInt(); c.phienBan = r.readUTF(); c.url = r.readUTF(); c.sha256 = r.readUTF();"
                    + " c.dungLuong = r.readUTF(); c.nhatKy = r.readUTF(); c.trangTai = r.readUTF(); c.banToiThieu = r.readInt(); c.banClient = r.readInt();"));
            Kiem.dung("ban co goi = 907 (client dau tien)", CapNhatClient.BAN_CO_GOI == 907);
            final String pb = new String(Files.readAllBytes(Paths.get("../tools/phat-hanh/phien_ban.conf")), StandardCharsets.UTF_8);
            final java.util.regex.Matcher mb = java.util.regex.Pattern.compile("(?m)^BAN=(\\d+)").matcher(pb);
            Kiem.dung("phien_ban.conf BAN >= 907 (client hien tai biet goi -115)", mb.find() && Integer.parseInt(mb.group(1)) >= CapNhatClient.BAN_CO_GOI);
            final int ban = Integer.parseInt(mb.group(1));
            final java.util.regex.Matcher mp = java.util.regex.Pattern.compile("(?m)^PHIEN_BAN=(\\d+)\\.(\\d+)\\.(\\d+)(\\.\\d)?").matcher(pb);
            Kiem.dung("PHIEN_BAN co 3 phan so", mp.find() && mp.group(4) == null);
            Kiem.bang("BAN = major*10000 + minor*100 + patch cua PHIEN_BAN", ban,
                    Integer.parseInt(mp.group(1)) * 10000 + Integer.parseInt(mp.group(2)) * 100 + Integer.parseInt(mp.group(3)));
        } catch (final Exception e) {
            Kiem.dung("goi -115 doc duoc: " + e, false);
        }
    }

    private static void kiemClientCu() {
        Kiem.nhom("Cap nhat client - client cu (< 907): hop 37 co chu + link");
        final Map<String, Ban> dm = CapNhatClient.docDanhMuc(DANH_MUC);
        final QuyetDinh b = qd(905, "jar", 907, 0, true, dm);
        final String c = CapNhatClient.cauCu(b, "https://htbb.vd/download");
        Kiem.dung("bat buoc: chu co link trang tai", c.contains("https://htbb.vd/download"));
        Kiem.dung("bat buoc: chu noi ro ban cu", c.contains("905") && c.contains("Cần cập nhật"));
        Kiem.bang("link hop 37 = tep (mot cham)", "https://htbb.vd/tai/HTBB-v907.jar", CapNhatClient.linkCu(b, "https://htbb.vd/", "https://htbb.vd/download"));
        final QuyetDinh r = qd(905, "jar", 907, 0, true, java.util.Collections.<String, Ban>emptyMap());
        Kiem.bang("khong co tep -> link trang tai", "https://htbb.vd/download", CapNhatClient.linkCu(r, "https://htbb.vd/", "https://htbb.vd/download"));
        final String n = CapNhatClient.cauCu(qd(906, "jar", 0, 0, true, dm), "https://htbb.vd/download");
        Kiem.dung("nhac: chu co ban moi + link", n.contains("0.9.7") && n.contains("https://htbb.vd/download"));
    }

    private static void kiemNoiVao() {
        Kiem.nhom("Cap nhat client - noi vao LoginHandler / TeamServer / cau hinh");
        try {
            final String lh = new String(Files.readAllBytes(Paths.get("src/real/cmd/LoginHandler.java")), StandardCharsets.UTF_8);
            final int iBan = lh.indexOf("session.banClient = banClient;");
            final int iChan = lh.indexOf("CapNhatClient.chanKhiDangNhap(session, uname)");
            final int iUser = lh.indexOf("session.userID = userID;");
            Kiem.dung("chan BAT BUOC sau khi doc so ban", iBan > 0 && iChan > iBan);
            Kiem.dung("chan BAT BUOC truoc khi vao (truoc gan userID)", iChan > 0 && iUser > iChan);
            Kiem.dung("platf luu vao phien", lh.contains("session.platfClient = platf"));
            Kiem.bang("nhac sau dang nhap o CA HAI duong", 2, dem(lh, "CapNhatClient.sauDangNhap("));
            Kiem.sai("khong con chan cu (thieu link)", lh.contains("Vui lòng tải bản mới tại \" + TeamServer.linkTaiGame, \"\")"));
            final String ts = new String(Files.readAllBytes(Paths.get("src/server/TeamServer.java")), StandardCharsets.UTF_8);
            Kiem.dung("TeamServer bat luong doc danh muc", ts.contains("com.htbb.capnhat.CapNhatClient.batDau()"));
            final String ad = new String(Files.readAllBytes(Paths.get("src/com/htbb/cauhinh/ApDungMacDinh.java")), StandardCharsets.UTF_8);
            Kiem.dung("ap dung ngay sv.banMoiNhat", ad.contains("TeamServer.banMoiNhat = (int) so(m, \"sv.banMoiNhat\""));
            Kiem.dung("ap dung ngay sv.nhacCapNhat", ad.contains("TeamServer.nhacCapNhat = so(m, \"sv.nhacCapNhat\""));
            final String dms = DayDu.danhMucJson(); // danh muc trong CSDL (ban chup sql/htbb.sql), khong con DanhMucSinh.java
            final boolean co = dms.contains("\"sv.banMoiNhat\"") && dms.contains("\"sv.nhacCapNhat\"");
            Kiem.dung("danh muc cau hinh co sv.banMoiNhat", co);
        } catch (final Exception e) {
            Kiem.dung("doc ma nguon: " + e, false);
        }
    }

    private static int dem(final String s, final String t) {
        int n = 0;
        int i = 0;
        while ((i = s.indexOf(t, i)) >= 0) {
            n++;
            i += t.length();
        }
        return n;
    }
}
