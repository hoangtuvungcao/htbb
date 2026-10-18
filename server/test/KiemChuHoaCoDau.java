import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * CHARACTERIZATION TEST (xem chu thich dau test/Kiem.java): font cua ban client JAR KHONG
 * VE DUOC chu HOA co dau thuoc khoi Unicode U+1EA0..U+1EF8.
 *
 * LOI DA XAY RA THAT (05/09/2026, nguoi choi gui anh chup): muc menu Hoa Ky Lan "Ảo hóa"
 * hien tren man hinh thanh "o hóa" — chu "Ả" (U+1EA2) bi MAT HAN, khong phai ve sai ma la
 * khong ve gi ca.
 *
 * CHU THUONG CUNG KHOI THI BINH THUONG: ngay muc "Sử dụng" ben canh co "ử" (U+1EED) va
 * "ụ" (U+1EE5) — cung khoi U+1EA0..U+1EF8 — van hien du chu. Nen chi rieng dang CHU HOA
 * cua khoi nay la thieu trong font.
 *
 * Khong sua duoc font: ban client JAR khong co ma nguon, khong dong goi lai duoc (xem chu
 * thich dau real/SkinThuCuoi.java). Cach duy nhat la PHIA MAY CHU khong gui nhung ky tu do
 * nua — viet thuong chu do, hoac dat lai cau cho no roi vao giua cau.
 *
 * Phep kiem duoi day quet MOI chuoi hien cho nguoi choi trong src/ va bao ngay khi ai do
 * them mot chu hoa co dau moi, KEM theo goi y sua. Khong quet chu thich (dong // va *) —
 * chu thich khong bao gio den man hinh nguoi choi.
 *
 * DANH SACH BO QUA (chuoi KHONG hien ra man hinh): xem BO_QUA duoi day.
 */
public final class KiemChuHoaCoDau {

    /**
     * Cac dong duoc phep con chu hoa co dau vi chung KHONG PHAI chu hien cho nguoi choi.
     *
     * Nhan dien theo NOI DUNG chu khong theo so dong: so dong xe dich moi lan ai do them
     * bot vai dong phia tren, luc do phep kiem se bao hong oan mot cho van dung.
     */
    private static final String[] BO_QUA_CHUA = {
        // Bang ky tu hop le cua ten nhan vat: bieu thuc chinh quy LOC DAU VAO, khong bao
        // gio duoc gui ra man hinh, nen giu nguyen ca chu hoa co dau la dung.
        "[^a-zA-Z0-9",
    };

    public static void chay() {
        Kiem.nhom("Chu HOA co dau (U+1EA0..U+1EF8) - font client JAR khong ve duoc");

        final List<String> viPham = new ArrayList<String>();
        int soTep = 0;
        try (Stream<Path> ds = Files.walk(Paths.get("src"))) {
            for (final Path p : (Iterable<Path>) ds.filter(x -> x.toString().endsWith(".java"))::iterator) {
                soTep++;
                quetTep(p, viPham);
            }
        } catch (final IOException e) {
            Kiem.dung("duyet duoc thu muc src", false);
            return;
        }

        Kiem.dung("duyet duoc ma nguon src/ (" + soTep + " tep)", soTep > 0);
        if (!viPham.isEmpty()) {
            for (final String v : viPham) {
                System.out.println("        " + v);
            }
        }
        Kiem.bang("khong chuoi nao con chu HOA co dau (font client khong ve duoc)",
                Integer.valueOf(0), Integer.valueOf(viPham.size()));
    }

    private static void quetTep(final Path p, final List<String> viPham) throws IOException {
        final String duong = p.toString().replace('\\', '/');
        final List<String> dong = Files.readAllLines(p, StandardCharsets.UTF_8);
        for (int i = 0; i < dong.size(); i++) {
            final String line = dong.get(i);
            final String cat = line.trim();
            // Chu thich khong den man hinh nguoi choi.
            if (cat.startsWith("//") || cat.startsWith("*") || cat.startsWith("/*")) {
                continue;
            }
            // Chi quan tam dong CO chuoi — hang so ky tu roi le khong phai chu hien ra.
            if (line.indexOf('"') < 0) {
                continue;
            }
            final String viTri = duong + ":" + (i + 1);
            if (boQua(line)) {
                continue;
            }
            final StringBuilder xau = new StringBuilder();
            for (int k = 0; k < line.length(); k++) {
                final char c = line.charAt(k);
                if (laChuHoaCoDau(c) && xau.indexOf(String.valueOf(c)) < 0) {
                    xau.append(c);
                }
            }
            if (xau.length() > 0) {
                viPham.add(viTri + "  co chu hoa co dau [" + xau + "] -> viet thuong hoac dat lai cau: "
                        + cat.substring(0, Math.min(90, cat.length())));
            }
        }
    }

    /** Chu HOA thuoc khoi Vietnamese Extended U+1EA0..U+1EF8 — font client JAR thieu han. */
    static boolean laChuHoaCoDau(final char c) {
        return c >= 0x1EA0 && c <= 0x1EF8 && Character.isUpperCase(c);
    }

    private static boolean boQua(final String dong) {
        for (final String b : BO_QUA_CHUA) {
            if (dong.contains(b)) {
                return true;
            }
        }
        return false;
    }

    private KiemChuHoaCoDau() {
    }
}
