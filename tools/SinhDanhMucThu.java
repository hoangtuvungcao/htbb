import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Sinh web/admin/inc/danh_muc_thu.php tu chinh mang real.Map.petTemplate.
 *
 * VI SAO PHAI SINH CHU KHONG GO TAY (cung ly do voi tools/SinhDanhMucNgoc.java):
 * ten 30 con thu KHONG co trong CSDL - ghi cung trong src/real/Map.java
 * (loadPetTemplate). Va id o day KHONG PHAI id vat pham: ca hai loai thuong PET
 * va PETV deu doc thang Map.petTemplate[idpet] - MOT MANG CO DUNG 30 O (0..29),
 * KHONG kiem bien:
 *
 *   public void createPet(final int idpet, final int time) {
 *       final Pet pet = new Pet(Map.petTemplate[idpet].idTemplate, ...);
 *
 * id ngoai 0..29 nem ArrayIndexOutOfBoundsException GIUA vong lap phat qua trong
 * DatabaseGiftCode.newDoGetGiftCode - khong chi mat con thu do, ma con lam mat
 * luon dong ghi giftcode_log VA viec tru limit_use nam SAU vong lap trong CUNG
 * khoi try/catch - nghia la nhan lai duoc vo han. Sinh dung tu mang nay la cach
 * duy nhat dam bao bo chon khong bao gio dua ra id ngoai dai.
 *
 * Cach dung (sau khi da bien dich src/ vao build/classes):
 *   java -cp "build/classes;lib/NQSH_5h.jar;lib/mysql-connector-java-5.1.49.jar" SinhDanhMucThu
 *
 * Chay lai moi khi them/doi thu trong Map.loadPetTemplate(). test/KiemDanhMucThu
 * doi chieu lai lan nua nen quen chay thi bo kiem se do, khong am tham lech.
 */
public final class SinhDanhMucThu {

    private static final String TEP = "web/admin/inc/danh_muc_thu.php";

    public static void main(final String[] thamSo) throws Exception {
        real.Map.loadPetTemplate();
        final data.PetTemplate[] ds = real.Map.petTemplate;
        if (ds == null || ds.length == 0) {
            System.out.println("[LOI] Map.petTemplate rong - khong sinh gi.");
            System.exit(1);
        }

        final StringBuilder s = new StringBuilder();
        s.append("<?php\n");
        s.append("/**\n");
        s.append(" * DANH MUC THU NUOI (PET/PETV) cho o chon phan thuong giftcode.\n");
        s.append(" *\n");
        s.append(" * TEP NAY DUOC SINH RA, DUNG SUA TAY.\n");
        s.append(" *   Nguon   : mang real.Map.petTemplate (src/real/Map.java, loadPetTemplate)\n");
        s.append(" *   Sinh lai: java -cp \"build/classes;lib/NQSH_5h.jar;");
        s.append("lib/mysql-connector-java-5.1.49.jar\" SinhDanhMucThu\n");
        s.append(" *\n");
        s.append(" * KHOA nay LA CHI SO MANG, khong phai id vat pham - Char.createPet() va\n");
        s.append(" * createPetVinhVien() doc thang Map.petTemplate[idpet] khong kiem bien. Xem\n");
        s.append(" * tools/SinhDanhMucThu.java de biet vi sao phai sinh dung 0..");
        s.append(ds.length - 1).append(" tu chinh mang do.\n");
        s.append(" *\n");
        s.append(" * test/KiemDanhMucThu.java doi chieu tep nay voi mang Java, nen tep lech la\n");
        s.append(" * bo kiem do chu khong am tham sai.\n");
        s.append(" */\n\n");
        s.append("/** @return array<int,string> chi so 0..").append(ds.length - 1)
         .append(" => ten thu (KHONG phai id vat pham) */\n");
        s.append("function danhMucThu(): array\n");
        s.append("{\n");
        s.append("    return [\n");

        int soDong = 0;
        for (int i = 0; i < ds.length; i++) {
            final data.PetTemplate t = ds[i];
            if (t == null) {
                continue;
            }
            String ten = t.name == null ? "" : t.name.trim();
            if (ten.isEmpty()) {
                ten = "(khong ten)";
            }
            s.append("        ").append(i).append(" => '").append(thoatChuoiPhp(ten)).append("',\n");
            soDong++;
        }

        s.append("    ];\n");
        s.append("}\n");

        try (Writer w = new OutputStreamWriter(new FileOutputStream(TEP), StandardCharsets.UTF_8)) {
            w.write(s.toString());
        }
        System.out.println("Da sinh " + TEP + " voi " + soDong + " con thu.");
        System.exit(0);
    }

    private static String thoatChuoiPhp(final String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }

    private SinhDanhMucThu() {
    }
}
