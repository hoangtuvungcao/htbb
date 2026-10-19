import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ban mau server.ini cho may that (vps/server.ini.tmpl): moi khoa phai la khoa THAT (danh muc cau hinh
 * trung tam hoac duoc ma nguon server doc), va cac co san xuat quan trong phai dung gia tri - nap tien,
 * thanh vien, thu nghiem TAT (fail closed); chong DoS BAT; Pokemon-only (khong con sv.che_do_pokemon).
 * vps/server.ini.linux.example phai cung bo khoa + gia tri (tru bi mat).
 */
public final class KiemBanMauVps {

    private static final String F_TMPL = "../vps/server.ini.tmpl";
    private static final String F_VIDU = "../vps/server.ini.linux.example";

    private KiemBanMauVps() {
    }

    public static void chay() {
        Kiem.nhom("vps/server.ini.tmpl - ban mau production");
        final String tmpl = doc(F_TMPL);
        Kiem.dung("doc duoc " + F_TMPL, tmpl.length() > 1000);
        final Map<String, String> m = khoa(tmpl);

        // ---- 1. Moi khoa la khoa that ----
        final String biet = khoaBiet();
        for (final String k : m.keySet()) {
            Kiem.dung("khoa that: " + k, biet.contains("\n" + k + "\n"));
        }
        Kiem.dung("khong khoa nao khai hai lan", soDongKhoa(tmpl) == m.size());

        // ---- 2. Co san xuat ----
        final String[][] phai = {
            {"sv.port", "19129"}, {"db.host", "127.0.0.1:3306"}, {"db.host1", "127.0.0.1:3306"},
            {"db.choPhepMatKhauTrong", "0"}, {"sv.pathLog", "/opt/htbb/logs/"},
            {"sv.iplogin", "127.0.0.1"},
            {"sv.website", "https://huyenthoaibaoboi.fun/"}, {"sv.linkTaiGame", "https://huyenthoaibaoboi.fun/"},
            {"sv.lau_dai", "1"}, {"sv.he_sinh_thai", "1"}, {"sv.tran_cap_theo_mua", "0"},
            {"sv.nap_tien", "0"}, {"thanh_vien.bat", "0"}, {"thanh_vien.ban_bat", "0"},
            {"sv.cho_phep_thu_nghiem", "0"}, {"sv.testMode", "0"}, {"sv.testCode", ""}, {"sv.CodeTest", "0"},
            {"tu_dong.bat", "1"}, {"hoi_sinh.bat", "1"}, {"chi_so.bat", "1"},
            {"kich_hoat.chan_dang_nhap", "0"}, {"sv.EnableAvtiveCheck", "1"},
            {"sv.dangKyTrongGame", "1"}, {"sv.chiSoMayChu", "1"},
            {"sv.adminAccounts", "admin"}, {"sv.choPhepTaoTenGM", "0"},
            {"sv.socketTimeout", "180"}, {"sv.connRateWindow", "30"}, {"sv.connRateMax", "60"},
            {"sv.maxConnPerIp", "50"}, {"sv.dosBanSec", "300"}, {"sv.proxyTinCay", "127.0.0.1,::1"},
            {"sv.tranGoiThuong", "300"}, {"sv.tranGoiTaiNguyen", "600"}, {"sv.tranGoiTraCuu", "600"},
        };
        for (final String[] p : phai) {
            Kiem.bang("ban mau " + p[0], p[1], m.get(p[0]));
        }
        for (final String k : new String[]{"db.user", "db.password", "db.userNap", "db.pass", "db.user1", "db.password1"}) {
            Kiem.dung(k + " lay tu ${...} (/etc/htbb/htbb.env)", String.valueOf(m.get(k)).matches("\\$\\{DB_[A-Z_]+\\}"));
        }
        for (final String k : new String[]{"sv.che_do_pokemon", "api.url", "db.run", "sv.tranGoiMienIp"}) {
            Kiem.dung("ban mau KHONG co " + k, !m.containsKey(k));
        }
        Kiem.dung("gioBaoTri khong phai 'tat' (bao tri hang ngay bat)", !"tat".equalsIgnoreCase(String.valueOf(m.get("sv.gioBaoTri"))));
        Kiem.dung("khong con ten cu Tien Len TeaM", !tmpl.contains("Tien Len"));
        Kiem.dung("khong ghi IP WAF / IP goc", !tmpl.contains("51.79.") && !tmpl.contains("180.93."));
        final Matcher tl = Pattern.compile("docs/[A-Za-z0-9_/-]+(?:\\.[a-z]+)?").matcher(tmpl);
        while (tl.find()) {
            Kiem.dung("chi tro toi tai lieu chuan: " + tl.group(),
                    tl.group().matches("docs/(kien-truc|build|trien-khai|van-hanh|thiet-ke-game|cau-hinh|giay-phep)\\.md"));
        }

        // ---- 3. Ban vi du Linux cung khoa, cung gia tri (tru bi mat) ----
        final Map<String, String> vd = khoa(doc(F_VIDU));
        Kiem.bang("linux.example cung bo khoa voi ban mau", m.keySet().toString(), vd.keySet().toString());
        for (final Map.Entry<String, String> e : m.entrySet()) {
            if (!e.getValue().startsWith("${")) {
                Kiem.bang("linux.example " + e.getKey(), e.getValue(), vd.get(e.getKey()));
            }
        }
    }

    /** Khoa danh muc + khoa ma nguon server doc (getProperty / hang chuoi "sv.*"...), moi dong mot khoa. */
    private static String khoaBiet() {
        final StringBuilder b = new StringBuilder("\n");
        final Matcher dm = Pattern.compile("\"khoa\": \"([^\"]+)\"").matcher(DayDu.danhMucJson());
        while (dm.find()) {
            b.append(dm.group(1)).append('\n');
        }
        final String ma = doc("src/server/TeamServer.java") + doc("src/server/CauHinh.java")
                + doc("src/com/htbb/cauhinh/CauHinhTrungTam.java");
        final Matcher gp = Pattern.compile("\"((?:sv|db|api|limit)\\.[A-Za-z0-9_]+)\"").matcher(ma);
        while (gp.find()) {
            b.append(gp.group(1)).append('\n');
        }
        return b.toString();
    }

    private static Map<String, String> khoa(final String nd) {
        final Map<String, String> r = new LinkedHashMap<String, String>();
        for (final String d0 : nd.split("\n")) {
            final String d = d0.trim();
            if (d.isEmpty() || d.startsWith("#") || d.indexOf('=') < 0) {
                continue;
            }
            r.put(d.substring(0, d.indexOf('=')).trim(), d.substring(d.indexOf('=') + 1).trim());
        }
        return r;
    }

    private static int soDongKhoa(final String nd) {
        int n = 0;
        for (final String d0 : nd.split("\n")) {
            final String d = d0.trim();
            if (!d.isEmpty() && !d.startsWith("#") && d.indexOf('=') > 0) {
                n++;
            }
        }
        return n;
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
