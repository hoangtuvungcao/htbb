import data.Database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * SQL trong src/ phai chay duoc tren CA MariaDB/MySQL (VPS) LAN NovaDB (cong MySQL tren
 * SQLite, xem docs/cau-hinh.md "Chay tren NovaDB").
 *
 * Soi chuoi SQL trong ma nguon de cac cu phap NovaDB khong ho tro - da go bo ngay
 * 16/10/2026 - khong quay lai:
 *   - UPDATE/DELETE nhieu bang (UPDATE a x JOIN b ... SET ...)  -> subquery tuong quan
 *   - CREATE TABLE moi LIKE cu                                    -> Database.createTable doc SHOW CREATE TABLE
 *   - TIMESTAMPDIFF(...)                                          -> hieu UNIX_TIMESTAMP
 *   - ten bang kem CSDL khac "account.team_user"                   -> Connection.setCatalog
 * Va kiem cac ham thuan moi (doi ten trong DDL, URL JDBC voi db.thamSoJdbc).
 */
public final class KiemNovaDB {

    private KiemNovaDB() {
    }

    private static final Pattern LIT = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

    private static final String[][] CAM = {
        {"UPDATE nhieu bang (JOIN)", "(?i)\\bUPDATE\\s+`?\\w+`?\\s+(?:AS\\s+)?\\w+\\s+(?:(?:INNER|LEFT|RIGHT|CROSS)\\s+)?JOIN\\b"},
        {"UPDATE nhieu bang (dau phay)", "(?i)\\bUPDATE\\s+`?\\w+`?(?:\\s+\\w+)?\\s*,\\s*`?\\w+`?(?:\\s+\\w+)?\\s+SET\\b"},
        {"DELETE nhieu bang", "(?i)\\bDELETE\\s+\\w+(?:\\s*,\\s*\\w+)*\\s+FROM\\b[^;]*\\bJOIN\\b"},
        {"CREATE TABLE .. LIKE", "(?i)CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?[^(]*?\\bLIKE\\b"},
        {"TIMESTAMPDIFF", "(?i)\\bTIMESTAMPDIFF\\s*\\("},
        {"bang kem ten CSDL account.", "(?i)\\b(?:FROM|UPDATE|INTO|JOIN)\\s+`?account`?\\.`?\\w"},
    };

    public static void chay() {
        Kiem.nhom("NovaDB - SQL chay duoc tren ca MariaDB lan NovaDB");
        final List<String[]> sql = gomChuoiSql();
        Kiem.dung("gom duoc chuoi SQL trong src (tren 300)", sql.size() > 300);
        for (final String[] cam : CAM) {
            final Pattern p = Pattern.compile(cam[1]);
            final List<String> vipham = new ArrayList<>();
            for (final String[] s : sql) {
                if (p.matcher(s[1]).find()) {
                    vipham.add(s[0]);
                }
            }
            Kiem.bang("src khong con " + cam[0], "[]", vipham.toString());
        }

        // ---- Database.createTable: doi ten trong DDL cua SHOW CREATE TABLE ----
        final String ddlMaria = "CREATE TABLE `tob_qua_loi_dai` (\n  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n"
                + "  `charname` varchar(100) NOT NULL,\n  PRIMARY KEY (`id`)\n) ENGINE=MyISAM AUTO_INCREMENT=57 DEFAULT CHARSET=utf8";
        Kiem.bang("doi ten DDL MariaDB, bo AUTO_INCREMENT=N cua bang (nhu LIKE)",
                "CREATE TABLE `tob_qua_loi_dai_102026` (\n  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n"
                + "  `charname` varchar(100) NOT NULL,\n  PRIMARY KEY (`id`)\n) ENGINE=MyISAM DEFAULT CHARSET=utf8",
                Database.doiTenTrongDdl(ddlMaria, "tob_qua_loi_dai_102026"));
        final String ddlNova = "CREATE TABLE `tob_top_event` (\n  `id` int NOT NULL AUTO_INCREMENT,\n"
                + "  `t` datetime DEFAULT (datetime('now')),\n  UNIQUE KEY `charname` (`charname`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        Kiem.bang("doi ten DDL NovaDB (ngoac long nhau trong DEFAULT giu nguyen)",
                "CREATE TABLE `x_1` (\n  `id` int NOT NULL AUTO_INCREMENT,\n"
                + "  `t` datetime DEFAULT (datetime('now')),\n  UNIQUE KEY `charname` (`charname`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
                Database.doiTenTrongDdl(ddlNova, "x_1"));
        boolean nem = false;
        try {
            Database.doiTenTrongDdl("CREATE VIEW v AS SELECT 1", "x");
        } catch (final IllegalArgumentException e) {
            nem = true;
        }
        Kiem.dung("DDL khong phai CREATE TABLE -> nem loi, khong doan bua", nem);

        // ---- db.thamSoJdbc ----
        Kiem.bang("thamSoJdbc trong -> URL y nhu cu", "jdbc:mysql://127.0.0.1:3306/htbb_db",
                Database.urlJdbc("127.0.0.1:3306", "htbb_db", ""));
        Kiem.bang("thamSoJdbc null -> URL y nhu cu", "jdbc:mysql://h/d", Database.urlJdbc("h", "d", null));
        Kiem.bang("thamSoJdbc useSSL=false", "jdbc:mysql://h:3318/htbb_db?useSSL=false",
                Database.urlJdbc("h:3318", "htbb_db", "useSSL=false"));
        Kiem.bang("thamSoJdbc bo dau ? va khoang trang thua", "jdbc:mysql://h/d?a=1&b=2",
                Database.urlJdbc("h", "d", "  ?a=1&b=2 "));

        final String ts = doc("src/server/TeamServer.java");
        Kiem.dung("TeamServer dat thamSoJdbc truoc setLink",
                ts.indexOf("Database.datThamSoJdbc(") > 0
                        && ts.indexOf("Database.datThamSoJdbc(") < ts.indexOf("Database.setLink(p.getProperty("));
        Kiem.dung("phep thu ket noi luc khoi dong cung dung URL co thamSoJdbc",
                ts.contains("Database.urlJdbc(host, ten, p.getProperty(\"db.thamSoJdbc\"))"));
        final String db = doc("src/data/Database.java");
        Kiem.sai("Database khong tu ghep \"jdbc:mysql://\" cho ConnPool nua",
                db.contains("new ConnPool(\"jdbc:mysql://"));
        Kiem.dung("docs ghi db.thamSoJdbc=useSSL=false cho NovaDB",
                doc("../docs/cau-hinh.md").contains("db.thamSoJdbc=useSSL=false"));

        // ---- Phan biet hoa/thuong: NovaDB "=" phan biet, MariaDB utf8_general_ci thi khong ----
        final String nv = doc("src/data/DatabaseNhanVat.java");
        Kiem.dung("tao nhan vat: kiem trung ten khong phan biet hoa/thuong",
                nv.contains("\"SELECT * FROM tob_char WHERE LOWER(charname)=LOWER(?)\""));
        Kiem.dung("tao nhan vat: luu ten chu thuong (server nap ten bang toLowerCase)",
                nv.contains("charname = charname.toLowerCase();"));
        final String dt = doc("src/real/CharDoiTenNhanVat.java");
        Kiem.dung("doi ten: kiem trung ten khong phan biet hoa/thuong",
                dt.contains("WHERE LOWER(charname)=LOWER(?) LIMIT 1"));
        Kiem.dung("doi ten: luu ten chu thuong", dt.contains("tenMoiGoc.trim().toLowerCase()"));
    }

    /** Moi phan tu: {vi tri, chuoi SQL ghep tu cac hang chuoi tren toi da 6 dong lien tiep}. */
    private static List<String[]> gomChuoiSql() {
        final List<String[]> kq = new ArrayList<>();
        final List<Path> tep = new ArrayList<>();
        try (Stream<Path> s = Files.walk(Paths.get("src"))) {
            s.filter(x -> x.toString().endsWith(".java")).forEach(tep::add);
        } catch (final IOException e) {
            return kq;
        }
        final Pattern batDau = Pattern.compile("(?i)^\\s*(SELECT|INSERT|UPDATE|DELETE|REPLACE|CREATE|ALTER|DROP|SHOW)\\b");
        for (final Path p : tep) {
            final String[] dong = doc(p.toString()).split("\n", -1);
            for (int i = 0; i < dong.length; i++) {
                final String d = dong[i].trim();
                if (d.startsWith("//") || d.startsWith("*") || d.startsWith("/*")) {
                    continue;
                }
                final Matcher m = LIT.matcher(dong[i]);
                if (!m.find() || !batDau.matcher(m.group(1)).find()) {
                    continue;
                }
                final StringBuilder sb = new StringBuilder();
                for (int j = i; j < dong.length && j < i + 6; j++) {
                    final String dj = dong[j].trim();
                    if (dj.startsWith("//")) {
                        continue;
                    }
                    final Matcher mj = LIT.matcher(dong[j]);
                    while (mj.find()) {
                        sb.append(mj.group(1)).append(' ');
                    }
                    if (dj.endsWith(";")) {
                        break;
                    }
                }
                kq.add(new String[]{p + ":" + (i + 1), sb.toString()});
            }
        }
        return kq;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
