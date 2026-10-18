import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Tim va doc ma nguon website HTBB-Web (Nuxt 4) tu bo kiem cua repo game.
 *
 * Website PHP cu (thu muc web/ cua repo nay) DA BO HAN (xem docs/pokemon/BO_WEB_PHP.md):
 * khu quan tri, hang doi lenh web_lenh_nhan_vat, danh muc giftcode... gio nam o HTBB-Web.
 * Cac phep kiem "hop dong giua Java va web" vi vay doc tep cua HTBB-Web.
 *
 * Thu tu tim: -Dhtbb.web=<thu muc>, bien moi truong HTBB_WEB_DIR, ../HTBB-Web (ba repo dat
 * canh nhau nhu hien nay), ../web (monorepo: server/ va web/ cung cap). Thu muc hop le
 * phai co nuxt.config.ts - tranh nham voi thu muc web/ PHP cu neu con sot lai.
 *
 * KHONG THAY THI BO QUA CO IN RA (giong test/KiemNapTien doi chieu napTienThuan.ts): CI
 * cua repo game chi checkout repo nay, khong co HTBB-Web. Tren may dev co du ba repo thi
 * moi phep kiem deu chay that.
 */
final class WebHtbb {

    private static File goc;
    private static boolean daTim;

    private WebHtbb() {
    }

    /** Thu muc goc HTBB-Web, hoac null neu khong co. */
    static synchronized File goc() {
        if (!daTim) {
            daTim = true;
            final String[] ung = {
                System.getProperty("htbb.web"), System.getenv("HTBB_WEB_DIR"), "../HTBB-Web", "../web"
            };
            for (final String u : ung) {
                if (u != null && !u.isEmpty() && new File(u, "nuxt.config.ts").isFile()) {
                    goc = new File(u);
                    break;
                }
            }
        }
        return goc;
    }

    /** true (va in dong "bo qua") khi khong co HTBB-Web - goi o dau moi phep kiem web. */
    static boolean boQua(final String phep) {
        if (goc() != null) {
            return false;
        }
        System.out.println("   (bo qua " + phep + ": khong thay HTBB-Web o ../HTBB-Web hay ../web)");
        return true;
    }

    /** Duong dan hien thi (de ten phep kiem doc duoc). */
    static String ten(final String tuongDoi) {
        return "HTBB-Web/" + tuongDoi;
    }

    /** Doc mot tep (duong dan tuong doi tu goc HTBB-Web); "" neu khong co. */
    static String doc(final String tuongDoi) {
        final File g = goc();
        if (g == null) {
            return "";
        }
        try {
            return new String(Files.readAllBytes(new File(g, tuongDoi).toPath()), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    /** Bo thoat mot chuoi JSON (phan giua hai dau nhay kep). */
    static String boThoatJson(final String s) {
        final StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) {
                b.append(c);
                continue;
            }
            final char n = s.charAt(++i);
            switch (n) {
                case 'n': b.append('\n'); break;
                case 'r': b.append('\r'); break;
                case 't': b.append('\t'); break;
                case 'b': b.append('\b'); break;
                case 'f': b.append('\f'); break;
                case 'u':
                    if (i + 4 < s.length()) {
                        b.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    break;
                default: b.append(n); break;
            }
        }
        return b.toString();
    }
}
