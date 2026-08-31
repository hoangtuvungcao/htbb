import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Sinh web/admin/inc/capdo.php tu chinh mang real.LevelDetail.expMain.
 *
 * VI SAO SINH CHU KHONG GO TAY: mang co 91 phan tu, nhieu so dai toi 14 chu so.
 * Go tay mot mang nhu the la tu ruoc loi go nham mot chu so - loi kieu do KHONG
 * AI NHIN RA BANG MAT, va se lam thanh diem kinh nghiem hien sai tren thanh tien
 * do (khong lam sai CAP DO, vi cap do dat thang bang so nguoi van hanh nhap, chi
 * lam LECH so % hien trong thanh kinh nghiem cua nhan vat).
 *
 * MANG NAY CHI DUNG DE GIU THANH KINH NGHIEM NHAT QUAN. Khi trang quan tri dat
 * lai cap do mot nhan vat (tob_char.lastLv), no dat luon tob_char.xp = gia tri
 * dung DAU cua cap do do (expMain[cap - 1]) - giong het trang thai "vua len cap,
 * chua co % nao vao cap tiep theo" cua mot nguoi choi len cap tu nhien. Moi chi
 * so KHAC cua nhan vat (suc manh, nhanh nhen, HP, diem ky nang...) deu duoc may
 * chu tu TINH LAI tu lastLv moi lan chon nhan vat
 * (src/real/cmd/SelectCharHandler.java:206-215, roi goi p.calculateAttrib()) -
 * nen KHONG can va KHONG duoc dong vao cot nao khac.
 *
 * Cach dung (sau khi da bien dich src/ vao build/classes):
 *   java -cp "build/classes;lib/NQSH_5h.jar;lib/mysql-connector-java-5.1.49.jar" SinhBangCapDo
 *
 * Chay lai neu bang expMain trong LevelDetail.java doi (rat hiem, thuong chi khi
 * mo rong tran cap). test/KiemBangCapDo.java doi chieu tep sinh voi mang Java tung
 * phan tu mot, nen quen chay lai la bo kiem do.
 */
public final class SinhBangCapDo {

    private static final String TEP = "web/admin/inc/capdo.php";

    public static void main(final String[] thamSo) throws Exception {
        final long[] expMain = real.LevelDetail.expMain;
        if (expMain == null || expMain.length == 0) {
            System.out.println("[LOI] LevelDetail.expMain rong - khong sinh gi.");
            System.exit(1);
        }

        // Tran cap CHO PHAT DUOC BANG TRANG QUAN TRI: lay theo du lieu THAT dang
        // chay (MAX(lastLv) tren CSDL that = 90 luc doi chieu), KHONG lay het do
        // dai mang (91). Ham getXpFromLevel() ben Java co mot loi lech-mot o bien
        // tren cung (dieu kien "level < expMain.length" sai cho dung level bang
        // do dai mang) - tranh dung toi bien do bang cach dat tran thap hon do
        // dai mang mot don vi.
        final int tranCap = expMain.length - 1;

        final StringBuilder s = new StringBuilder();
        s.append("<?php\n");
        s.append("/**\n");
        s.append(" * BANG QUY DOI CAP DO -> DIEM KINH NGHIEM, cho chuc nang \"Dat cap do\" o\n");
        s.append(" * trang Nhan vat (nhanvat.php).\n");
        s.append(" *\n");
        s.append(" * TEP NAY DUOC SINH RA, DUNG SUA TAY.\n");
        s.append(" *   Nguon   : real.LevelDetail.expMain (src/real/LevelDetail.java)\n");
        s.append(" *   Sinh lai: java -cp \"build/classes;lib/NQSH_5h.jar;");
        s.append("lib/mysql-connector-java-5.1.49.jar\" SinhBangCapDo\n");
        s.append(" *\n");
        s.append(" * CHI dung de dat cot `xp` cho KHOP voi cap do dat vao `lastLv`, giong het\n");
        s.append(" * trang thai vua len cap tu nhien (0% vao cap tiep theo). MOI chi so khac\n");
        s.append(" * cua nhan vat (suc manh/nhanh nhen/HP/diem ky nang...) deu duoc may chu game\n");
        s.append(" * TU TINH LAI tu `lastLv` ngay luc chon nhan vat\n");
        s.append(" * (src/real/cmd/SelectCharHandler.java:206-215, roi p.calculateAttrib()) -\n");
        s.append(" * trang web KHONG duoc va KHONG can dong vao bat ky cot nao khac.\n");
        s.append(" *\n");
        s.append(" * test/KiemBangCapDo.java doi chieu tung phan tu voi mang that trong Java.\n");
        s.append(" */\n\n");
        s.append("/** Cap do cao nhat trang quan tri cho phep dat. */\n");
        s.append("const CAPDO_TOI_DA = ").append(tranCap).append(";\n\n");
        s.append("/**\n");
        s.append(" * Diem kinh nghiem dung tai LUC VUA LEN cap do nay (0% vao cap tiep theo).\n");
        s.append(" * Chi so mang = cap do - 1, dung nhu real.LevelDetail.expMain ben Java.\n");
        s.append(" */\n");
        s.append("function capDoRaExp(): array\n");
        s.append("{\n");
        s.append("    return [\n");
        for (int i = 0; i < expMain.length; i++) {
            s.append("        ").append(expMain[i]).append(", // cap ").append(i + 1).append("\n");
        }
        s.append("    ];\n");
        s.append("}\n");

        try (Writer w = new OutputStreamWriter(new FileOutputStream(TEP), StandardCharsets.UTF_8)) {
            w.write(s.toString());
        }
        System.out.println("Da sinh " + TEP + " voi " + expMain.length
                + " muc (tran cap cho phat = " + tranCap + ").");
        System.exit(0);
    }

    private SinhBangCapDo() {
    }
}
