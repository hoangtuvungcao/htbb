import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Kinh te khoi dau (19/10/2026, chu game duyet "giam het di") + ghi client dang nhap:
 *   - nhan vat moi: XU_KHOI_DAU <= 5.000 xu, 0 luong, 0 luong khoa, khong qua board_created;
 *   - he so nap toan may chu CO DINH 1 (5h_systems.tangnap bi bo qua);
 *   - data.GhiClient: phan loai client, bang tob_client_dang_nhap khop tep SQL, duong dang nhap
 *     qua login server (may that) co cam theo may + ghi client.
 */
public final class KiemKinhTeKhoiDau {

    private KiemKinhTeKhoiDau() {
    }

    public static void chay() {
        Kiem.nhom("Kinh te khoi dau: tien nhan vat moi + he so nap");
        Kiem.dung("XU_KHOI_DAU <= 5000", data.Database.XU_KHOI_DAU <= 5000L);
        Kiem.dung("XU_KHOI_DAU > 0 (du mua binh mau 25 xu o Chansey)", data.Database.XU_KHOI_DAU >= 100L);
        Kiem.bang("he so nap co dinh = 1", Integer.valueOf(1), Integer.valueOf(data.Database.TILE_NAP_CO_DINH));
        Kiem.bang("TileNap luc khoi dong = 1", Integer.valueOf(1), Integer.valueOf(data.Database.TileNap));

        final String nv = doc("src/data/DatabaseNhanVat.java");
        final int iInsert = nv.indexOf("INSERT INTO tob_char(charname,userId,headStyle,class,pInfo,potion,skill,basic,gender,infotop,gold,equip,idserver)");
        Kiem.dung("addChar chi ghi cot gold (xu), khong ghi luong / luonglock (mac dinh 0)", iInsert >= 0);
        Kiem.sai("addChar khong ghi board_created", nv.contains("INSERT INTO board_created"));
        Kiem.dung("hai cho dat xu nhan vat dau tien dung XU_KHOI_DAU",
                nv.split("xu = XU_KHOI_DAU;", -1).length - 1 == 4);

        final String db = doc("src/data/Database.java");
        final int iHam = db.indexOf("public void loadTangNap()");
        final String than = iHam < 0 ? "" : db.substring(iHam, db.indexOf("\n    public ", iHam + 10));
        Kiem.sai("loadTangNap KHONG gan TileNap tu CSDL", than.contains("TileNap = rs.getInt"));

        final String[] seed = {"../sql/gamedb7_clean.sql", "../sql/htbb_day_du.sql"};
        for (final String f : seed) {
            final String sq = doc(f);
            Kiem.sai(f + ": tangnap khong con = 2", sq.contains("'tangnap', 2)") || sq.contains("'tangnap',2)"));
        }

        Kiem.nhom("data.GhiClient - client dang nhap (tob_client_dang_nhap)");
        Kiem.bang("PC launcher", "pc", data.GhiClient.loaiClient("HTBB-PC/Windows 11"));
        Kiem.bang("PC Linux", "pc", data.GhiClient.loaiClient("HTBB-PC/Linux"));
        Kiem.bang("Android", "android", data.GhiClient.loaiClient("Android/SM-A515F"));
        Kiem.bang("J2ME", "jar", data.GhiClient.loaiClient("Nokia6233/05.10"));
        Kiem.bang("bot / client goc", "jar", data.GhiClient.loaiClient("nokia/1/1"));
        Kiem.bang("null", "jar", data.GhiClient.loaiClient(null));
        Kiem.dung("chinh chu: ma may + so ban", data.GhiClient.laChinhChu("abc123", 903));
        Kiem.sai("khong ma may -> ban ngoai", data.GhiClient.laChinhChu("", 903));
        Kiem.sai("khong so ban -> ban ngoai", data.GhiClient.laChinhChu("abc123", -1));
        Kiem.bang("cat chuoi client", "abc", data.GhiClient.cat("abcdef", 3));
        Kiem.bang("cat bo xuong dong", "a b", data.GhiClient.cat("a\nb", 10));

        final String sql = doc("../sql/them_bang_tob_client_dang_nhap.sql").replaceAll("\\s+", " ");
        for (final String cot : new String[]{"`tai_khoan` VARCHAR(45) NOT NULL", "`loai` VARCHAR(10)", "`chinh_chu` TINYINT",
                "`ban_jar` INT", "`lan_cuoi` DATETIME NOT NULL", "PRIMARY KEY (`tai_khoan`, `loai`)"}) {
            Kiem.dung("SQL va GhiClient.DDL cung cot " + cot, sql.contains(cot) && ghiClientDdl().contains(cot));
        }

        final String login = doc("src/real/cmd/LoginHandler.java");
        final int iOk = login.indexOf("public static void processLoginOk(final Session s)");
        final int iHet = login.indexOf("public void doLoginByAdmin", iOk);
        final String ok = iOk < 0 || iHet < 0 ? "" : login.substring(iOk, iHet);
        Kiem.dung("processLoginOk (duong login server) kiem cam theo may", ok.contains("data.CamTheoMay.biCam(s.IMEI)"));
        Kiem.dung("processLoginOk ghi nhan may dang nhap", ok.contains("data.CamTheoMay.ghiNhanDangNhap(s.IMEI"));
        Kiem.dung("processLoginOk ghi client", ok.contains("data.GhiClient.ghiNhan(s)"));
        Kiem.dung("cam theo may truoc khi gui danh sach nhan vat",
                ok.indexOf("biCam(s.IMEI)") >= 0 && ok.indexOf("biCam(s.IMEI)") < ok.indexOf("createCharListMessage"));
        Kiem.dung("goi dang nhap nho platf cho GhiClient", login.contains("data.GhiClient.nhoPlatf(session, platf)"));

        final String viTri = doc("src/data/GhiViTriOnline.java");
        Kiem.dung("tob_vi_tri_online ghi tai_khoan + client", viTri.contains("tai_khoan, client, chinh_chu, client_ban, platf"));
        final String soOnline = doc("src/data/GhiSoOnline.java");
        Kiem.dung("tob_log_online ghi pc/android/jar/khac", soOnline.contains("(thoi_gian, ket_noi, nhan_vat, pc, android, jar, khac)"));
    }

    private static String ghiClientDdl() {
        try {
            final java.lang.reflect.Field f = data.GhiClient.class.getDeclaredField("DDL");
            f.setAccessible(true);
            return ((String) f.get(null)).replaceAll("\\s+", " ");
        } catch (final Exception e) {
            return "";
        }
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
