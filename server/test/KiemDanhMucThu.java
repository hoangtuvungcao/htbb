import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.PetTemplate;

/**
 * Doi chieu web/admin/inc/danh_muc_thu.php voi mang real.Map.petTemplate.
 *
 * Cung ly do voi KiemDanhMucNgoc: ten 30 con thu khong co trong CSDL, chi so
 * trong tep sinh KHONG PHAI id vat pham ma la chi so mang Map.petTemplate[] -
 * lech mot cai la ca bo chon dua ra sai chi so, va Char.createPet()/
 * createPetVinhVien() doc thang mang do KHONG kiem bien.
 */
public final class KiemDanhMucThu {

    private static final String TEP = "web/admin/inc/danh_muc_thu.php";

    private static final Pattern DONG = Pattern.compile("^\\s*(\\d+)\\s*=>\\s*'(.*)'\\s*,\\s*$");

    private KiemDanhMucThu() {
    }

    public static void chay() {
        Kiem.nhom("Danh muc thu - tep sinh phai khop mang Java");

        final String php = doc(TEP);
        Kiem.dung("doc duoc " + TEP, !php.isEmpty());
        if (php.isEmpty()) {
            return;
        }

        final Map<Integer, String> tuTep = new LinkedHashMap<Integer, String>();
        for (final String dong : php.split("\r?\n")) {
            final Matcher m = DONG.matcher(dong);
            if (m.matches()) {
                tuTep.put(Integer.valueOf(Integer.parseInt(m.group(1))), boThoat(m.group(2)));
            }
        }
        Kiem.dung("doc duoc dong du lieu tu tep sinh", tuTep.size() > 0);

        real.Map.loadPetTemplate();
        final PetTemplate[] ds = real.Map.petTemplate;
        Kiem.dung("nap duoc Map.petTemplate", ds != null && ds.length > 0);
        if (ds == null || ds.length == 0) {
            return;
        }

        Kiem.bang("dung 30 con thu (chi so 0..29)", Integer.valueOf(30), Integer.valueOf(ds.length));
        Kiem.bang("so dong trong tep khop so phan tu mang Java",
                Integer.valueOf(ds.length), Integer.valueOf(tuTep.size()));

        int lech = 0;
        int thieu = 0;
        String viDuLech = "";
        for (int i = 0; i < ds.length; i++) {
            final PetTemplate t = ds[i];
            if (t == null) {
                continue;
            }
            final String tenTep = tuTep.get(Integer.valueOf(i));
            if (tenTep == null) {
                thieu++;
                continue;
            }
            final String tenJava = (t.name == null || t.name.trim().isEmpty())
                    ? "(khong ten)" : t.name.trim();
            if (!tenJava.equals(tenTep)) {
                lech++;
                if (viDuLech.isEmpty()) {
                    viDuLech = "chi so " + i + ": Java='" + tenJava + "' tep='" + tenTep + "'";
                }
            }
        }
        Kiem.bang("khong chi so nao thieu trong tep sinh", Integer.valueOf(0), Integer.valueOf(thieu));
        Kiem.bang("khong ten nao lech" + (viDuLech.isEmpty() ? "" : " (" + viDuLech + ")"),
                Integer.valueOf(0), Integer.valueOf(lech));

        // Hai chi so trung ten (5 va 28 = "Lồng đèn hoa sen") - ghi cung trong Java,
        // xac nhan lai o day de ai do "sua" cho giong nhau roi lam that lac chi so
        // se bi bat ngay.
        Kiem.bang("chi so 5 va 28 cung ten (trung nhau that su trong Java)",
                tuTep.get(Integer.valueOf(5)), tuTep.get(Integer.valueOf(28)));

        Kiem.dung("tep sinh co ghi ro DUNG SUA TAY", php.contains("DUNG SUA TAY"));
        Kiem.dung("tep sinh co ghi cach sinh lai", php.contains("SinhDanhMucThu"));
        Kiem.dung("tep sinh co ghi ro day la chi so mang, khong phai id",
                php.contains("CHI SO MANG"));
    }

    private static String boThoat(final String s) {
        return s.replace("\\'", "'").replace("\\\\", "\\");
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
