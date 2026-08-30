import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cau hinh server di qua pipeline: vps/server.ini.tmpl (co trong git) + bi mat GitHub.
 *
 * VI SAO CAN TEST. Ba noi phai khop nhau, va khong noi nao bao loi khi lech:
 *   1. vps/server.ini.tmpl        - cho trong ${TEN_BIEN}
 *   2. .github/workflows/deploy.yml - mang secret tu GitHub xuong stdin cua ssh
 *   3. vps/deploy-auto.sh         - danh sach cho phep khi doc stdin
 * Them ${DB_MOI} vao ban mau ma quen hai cho kia thi deploy khong sap ngay: no chi
 * bao "thieu gia tri" luc dang chay tren VPS, giua mot lan deploy that.
 *
 * Va quan trong hon: moi khoa TeamServer doc bang parseInt/parseByte/parseShort deu
 * phai co trong ban mau voi mot con SO hop le. Thieu mot cai la ca khoi doc cau hinh
 * nem loi, MOI DONG BEN DUOI bi bo qua, ma server van khoi dong - dung cai bay da lam
 * sap server ngay 30/08/2026.
 */
public final class KiemCauHinhPipeline {

    private static final String F_TMPL = "vps/server.ini.tmpl";
    private static final String F_SINH = "vps/sinh-server-ini.sh";
    private static final String F_DEPLOY = "vps/deploy-auto.sh";
    private static final String F_WORKFLOW = ".github/workflows/deploy.yml";
    private static final String F_SERVER = "src/server/TeamServer.java";

    public static void chay() {
        Kiem.nhom("Cau hinh qua pipeline - ba noi phai khop nhau");

        final String tmpl = doc(F_TMPL);
        final String sinh = doc(F_SINH);
        final String deploy = doc(F_DEPLOY);
        final String wf = doc(F_WORKFLOW);
        final String sv = doc(F_SERVER);
        Kiem.dung("doc duoc " + F_TMPL, tmpl.length() > 500);
        Kiem.dung("doc duoc " + F_SINH, sinh.length() > 500);
        if (tmpl.isEmpty() || sinh.isEmpty() || deploy.isEmpty() || wf.isEmpty() || sv.isEmpty()) {
            return;
        }

        final Map<String, String> banMau = docKhoa(tmpl);
        Kiem.dung("ban mau co du khoa (tren 40)", banMau.size() > 40);

        // ---- 1. Moi khoa TeamServer doc bang parseX phai co trong ban mau, va la mot so ----
        // Lay thang tu ma nguon chu khong chep tay danh sach: them mot parseInt moi ma quen
        // them vao ban mau thi phep kiem nay hong ngay, khong cho toi luc deploy.
        final List<String> batBuoc = khoaParseX(sv);
        Kiem.dung("tim duoc cac khoa parseX trong TeamServer (tren 20)", batBuoc.size() > 20);
        for (final String k : batBuoc) {
            final String v = banMau.get(k);
            Kiem.dung("ban mau co khoa bat buoc " + k, v != null);
            if (v != null) {
                Kiem.dung("ban mau dat " + k + " la mot so (parseX se khong nem loi)", laSo(v));
            }
        }

        // ---- 2. Cho trong ${...} phai co du o ca workflow lan danh sach cho phep ----
        final List<String> bien = bienTrong(banMau);
        Kiem.dung("ban mau co dung cho trong cho bi mat CSDL", !bien.isEmpty());
        for (final String b : bien) {
            Kiem.dung(b + " co trong " + F_WORKFLOW, wf.contains(b + ":"));
            Kiem.dung(b + " co trong danh sach cho phep cua deploy-auto.sh",
                    coTrongMaLenh(deploy, b, '#'));
        }

        // ---- 3. Bi mat khong duoc nam trong git ----
        for (final Map.Entry<String, String> e : banMau.entrySet()) {
            final String k = e.getKey();
            if (k.equals("db.password") || k.equals("db.pass") || k.equals("db.password1")) {
                Kiem.dung(k + " trong ban mau phai la cho trong ${...}, khong phai gia tri that",
                        e.getValue().startsWith("${"));
            }
        }
        Kiem.sai("ban mau KHONG chua chuoi mat khau mau",
                banMau.containsValue("DOI_MAT_KHAU_DB"));

        // ---- 4. Bo sinh phai tu choi dung cac truong hop ----
        Kiem.dung("bo sinh chan chuoi mat khau mau", sinh.contains("DOI_MAT_KHAU_DB"));
        Kiem.dung("bo sinh thu ket noi that truoc khi cai", coTrongMaLenh(sinh, "mysql --defaults-file", '#'));
        Kiem.sai("KHONG dung --defaults-extra-file (/root/.my.cnf se de len, phep thu thanh vo dung)",
                coTrongMaLenh(sinh, "--defaults-extra-file", '#'));
        Kiem.dung("mat khau khong nam tren dong lenh mysql (ps -ef se lo ra)",
                !coTrongMaLenh(sinh, "-p$MK", '#'));
        // Phan huong dan dau ban mau co nhac chu ${TEN_BIEN}; quet ca file thi luc nao cung
        // bao dong gia. Da dinh that khi chay thu lan dau.
        Kiem.dung("bo sinh chi tim cho trong con sot o DONG LENH, bo qua chu thich",
                sinh.contains("grep -vE '^[[:space:]]*#' \"$MOI\" | grep -q '\\${'"));
        Kiem.dung("bo sinh giu nguyen file cu khi loi (khong cai gi)",
                sinh.contains("GIU NGUYEN file dang chay"));
        Kiem.dung("bo sinh sao luu ban cu truoc khi ghi de", coTrongMaLenh(sinh, "cp -a \"$DICH\"", '#'));

        // ---- 5. Doi ban mau thi PHAI restart, khong thi cau hinh moi khong co tac dung ----
        Kiem.dung("deploy coi vps/server.ini.tmpl la file doi hoi restart",
                deploy.contains("vps/server\\.ini\\.tmpl"));
        Kiem.dung("deploy goi bo sinh", deploy.contains("sinh-server-ini.sh"));
        Kiem.dung("deploy restart khi cau hinh doi du khong file nao khac doi",
                deploy.contains("van phai khoi dong lai"));
        Kiem.dung("deploy doc bi mat qua stdin (khoa ssh bi ep lenh nen khong truyen env duoc)",
                coTrongMaLenh(deploy, "doc_bi_mat", '#'));
        Kiem.dung("doc_bi_mat khong treo khi chay tay tu terminal",
                deploy.contains("[ -t 0 ] && return 0"));
    }

    /** Cac khoa TeamServer doc bang parseInt/parseByte/parseShort - thieu la nem loi. */
    private static List<String> khoaParseX(final String ma) {
        final List<String> ra = new ArrayList<String>();
        final Matcher m = Pattern.compile(
                "(?:Integer\\.parseInt|Byte\\.parseByte|Short\\.parseShort)"
                        + "\\(p\\.getProperty\\(\"([^\"]+)\"\\)\\)").matcher(ma);
        while (m.find()) {
            if (!ra.contains(m.group(1))) {
                ra.add(m.group(1));
            }
        }
        return ra;
    }

    /** Doc cac dong khoa=gia tri, bo qua chu thich. */
    private static Map<String, String> docKhoa(final String noiDung) {
        final Map<String, String> ra = new LinkedHashMap<String, String>();
        for (final String dongThuc : noiDung.split("\n")) {
            final String d = dongThuc.trim();
            if (d.isEmpty() || d.startsWith("#") || d.indexOf('=') < 0) {
                continue;
            }
            ra.put(d.substring(0, d.indexOf('=')).trim(), d.substring(d.indexOf('=') + 1).trim());
        }
        return ra;
    }

    /** Ten cac bien ${...} dung trong ban mau. */
    private static List<String> bienTrong(final Map<String, String> banMau) {
        final List<String> ra = new ArrayList<String>();
        for (final String v : banMau.values()) {
            final Matcher m = Pattern.compile("^\\$\\{([A-Za-z0-9_]+)\\}$").matcher(v);
            if (m.matches() && !ra.contains(m.group(1))) {
                ra.add(m.group(1));
            }
        }
        return ra;
    }

    private static boolean laSo(final String v) {
        if (v.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(v.trim());
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    /** Tim chuoi trong ma lenh, bo qua dong chu thich bat dau bang kyChuThich. */
    private static boolean coTrongMaLenh(final String noiDung, final String chuoi, final char kyChuThich) {
        for (final String dongThuc : noiDung.split("\n")) {
            final String d = dongThuc.trim();
            if (d.isEmpty() || (!d.isEmpty() && d.charAt(0) == kyChuThich)) {
                continue;
            }
            if (d.contains(chuoi)) {
                return true;
            }
        }
        return false;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemCauHinhPipeline() {
    }
}
