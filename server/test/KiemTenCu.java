import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Khong con ten game cu (ten may chu goc, 4 chu cai - xem TEN) trong ma nguon HTBB: server/src, client_jar/src, web/
 * (bo node_modules va thu muc build). Moi lan xuat hien phai nam trong mot dong cua du_lieu/ten_cu_cho_phep.csv
 * ("tep,chuoi,ly_do": chuoi la doan nguyen van tren cung dong, bao trum lan xuat hien) - chi danh cho tuong thich
 * nguoc (ten RecordStore tren may nguoi choi, duong dan / bien env client cu da phat hanh, bo loc ten server cu).
 * Dong cho phep khong con dung toi cung HONG (danh sach phai gon).
 */
public final class KiemTenCu {

    /** ten cu, so khong phan biet hoa thuong */
    private static final String TEN = "nqsh";
    private static final String[] THU_MUC = {"server/src", "client_jar/src", "web"};
    private static final Set<String> BO_QUA = new HashSet<>(java.util.Arrays.asList(
        "node_modules", ".nuxt", ".output", ".data", ".nitro", ".cache", "dist", "coverage", "uploads", "logs"));

    private static final List<String[]> CHO_PHEP = new ArrayList<>();
    private static final Set<Integer> DA_DUNG = new HashSet<>();
    private static final List<String> SAI = new ArrayList<>();
    private static int soTep;

    private KiemTenCu() {
    }

    public static void chay() {
        Kiem.nhom("Khong con ten game cu trong server/src, client_jar/src, web/ (ngoai le: du_lieu/ten_cu_cho_phep.csv)");
        try {
            CHO_PHEP.clear();
            DA_DUNG.clear();
            SAI.clear();
            soTep = 0;
            final List<String> dong = Files.readAllLines(new File("../du_lieu/ten_cu_cho_phep.csv").toPath(), StandardCharsets.UTF_8);
            for (final String d : dong.subList(1, dong.size())) {
                if (d.trim().isEmpty()) {
                    continue;
                }
                final int a = d.indexOf(',');
                final int b = d.indexOf(',', a + 1);
                final boolean hopLe = a > 0 && b > a + 1 && d.substring(b + 1).trim().length() > 0
                    && d.substring(a + 1, b).toLowerCase(Locale.ROOT).contains(TEN);
                Kiem.dung("dong cho phep hop le (tep,chuoi chua ten cu,ly_do): " + d, hopLe);
                if (hopLe) {
                    CHO_PHEP.add(new String[] {d.substring(0, a), d.substring(a + 1, b)});
                }
            }
            for (final String tm : THU_MUC) {
                duyet(new File("../" + tm), tm);
            }
            Kiem.dung("da quet du tep (" + soTep + ")", soTep > 1000);
            for (int i = 0; i < SAI.size() && i < 30; i++) {
                System.out.println("    TEN CU: " + SAI.get(i));
            }
            Kiem.bang("so lan ten cu ngoai danh sach cho phep", Integer.valueOf(0), Integer.valueOf(SAI.size()));
            final List<String> thua = new ArrayList<>();
            for (int i = 0; i < CHO_PHEP.size(); i++) {
                if (!DA_DUNG.contains(Integer.valueOf(i))) {
                    thua.add(CHO_PHEP.get(i)[0] + " " + CHO_PHEP.get(i)[1]);
                }
            }
            Kiem.bang("dong cho phep khong con dung toi (xoa khoi ten_cu_cho_phep.csv)", "[]", thua.toString());
            // tu kiem bo quet: mot chuoi lan ten cu ngoai danh sach phai bi bat
            Kiem.dung("bo quet bat duoc ten cu ngoai danh sach", !hopLeDong("x/y.java", "String s = \"N" + "QSH-H2\";"));
        } catch (final Exception e) {
            Kiem.dung("kiem ten cu: " + e, false);
        }
    }

    private static void duyet(final File f, final String duong) throws Exception {
        if (f.isDirectory()) {
            final String[] con = f.list();
            if (con == null) {
                return;
            }
            java.util.Arrays.sort(con);
            for (final String c : con) {
                if (!BO_QUA.contains(c)) {
                    duyet(new File(f, c), duong + "/" + c);
                }
            }
            return;
        }
        if (!f.isFile()) {
            return;
        }
        final byte[] b = Files.readAllBytes(f.toPath());
        for (int i = 0; i < b.length && i < 8192; i++) {
            if (b[i] == 0) {
                return; // tep nhi phan (anh, am thanh)
            }
        }
        soTep++;
        final String[] dong = new String(b, StandardCharsets.UTF_8).split("\n", -1);
        for (int i = 0; i < dong.length; i++) {
            if (dong[i].toLowerCase(Locale.ROOT).contains(TEN) && !hopLeDong(duong, dong[i])) {
                SAI.add(duong + ":" + (i + 1) + ": " + dong[i].trim());
            }
        }
    }

    /** Moi lan xuat hien ten cu tren dong deu nam trong mot doan cho phep cua cung tep. */
    private static boolean hopLeDong(final String tep, final String dong) {
        final String thuong = dong.toLowerCase(Locale.ROOT);
        int p = thuong.indexOf(TEN);
        while (p >= 0) {
            boolean phu = false;
            for (int k = 0; k < CHO_PHEP.size() && !phu; k++) {
                final String[] cp = CHO_PHEP.get(k);
                if (!cp[0].equals(tep)) {
                    continue;
                }
                int q = dong.indexOf(cp[1]);
                while (q >= 0 && !phu) {
                    if (q <= p && p + TEN.length() <= q + cp[1].length()) {
                        phu = true;
                        DA_DUNG.add(Integer.valueOf(k));
                    }
                    q = dong.indexOf(cp[1], q + 1);
                }
            }
            if (!phu) {
                return false;
            }
            p = thuong.indexOf(TEN, p + 1);
        }
        return true;
    }
}
