import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Sinh sql/ten_mo_ta_skin_linh_thu.sql tu chinh cac bang so trong real.SkinLinhThu.
 *
 * VI SAO PHAI SINH CHU KHONG GO TAY:
 *
 * 1. KHONG DE LECH. Ten vat pham trong CSDL vua la ten vua la MO TA — game nay khong co
 *    cot mo ta rieng cho do mac duoc, no nhet ca bang mo ta vao cot name va xuong dong
 *    bang \n (xem "Áo Nghê Thường" trong ban dump). Nghia la con so nguoi choi DOC duoc
 *    nam o CSDL, con con so game DUNG nam trong ma nguon. Go tay hai cho la som muon lech,
 *    va lech kieu do khong ai phat hien ra tru khi ngoi cong tay.
 *    Sinh tu mot nguon thi khong lech duoc. test/KiemSkinLinhThu con doi chieu lai lan nua.
 *
 * 2. KHONG HONG BANG MA. Chuoi tieng Viet o day di thang tu hang so Java ra tep UTF-8,
 *    khong qua sed/perl/heredoc — dung ba cho da tung lam hong 178 dong tieng Viet trong
 *    kho nay. Xem test/KiemBangMaNguon.
 *
 * Cach dung (sau khi da bien dich src/):
 *   java -cp build/sb-src;lib/NQSH_5h.jar SinhSqlTenSkinThu
 *
 * Chay lai bat cu luc nao doi so trong SkinLinhThu, roi chay tep sql tren may chu.
 */
public final class SinhSqlTenSkinThu {

    private static final String TEP = "sql/ten_mo_ta_skin_linh_thu.sql";

    public static void main(final String[] thamSo) throws Exception {
        final StringBuilder s = new StringBuilder();
        s.append("-- ==================================================================================\n");
        s.append("--  TEN + MO TA cua 5 vat pham skin linh thu\n");
        s.append("-- ==================================================================================\n");
        s.append("--\n");
        s.append("--  TEP NAY DUOC SINH RA, DUNG SUA TAY.\n");
        s.append("--  Nguon: cac bang hang so trong src/real/SkinLinhThu.java\n");
        s.append("--  Sinh lai: java -cp build/sb-src SinhSqlTenSkinThu\n");
        s.append("--\n");
        s.append("--  Cot `name` cua game nay vua la TEN vua la MO TA: khong co cot mo ta rieng cho\n");
        s.append("--  do mac duoc, nen mo ta duoc nhet vao name va xuong dong bang ky tu xuong dong\n");
        s.append("--  that. Game da lam san nhu vay voi ao Nghe Thuong, Ngoc Nu, Kim Dong...\n");
        s.append("--\n");
        s.append("--  Chay sau sql/skin_linh_thu.sql, roi khoi dong lai may chu (bang item template\n");
        s.append("--  chi nap mot lan luc bat may).\n");
        s.append("--\n");
        s.append("--      mysql -u root -p <ten_db> < ").append(TEP).append("\n");
        s.append("-- ==================================================================================\n\n");

        for (int i = 0; i < real.SkinLinhThu.SO_SKIN; i++) {
            final String ten = real.SkinLinhThu.moTaVatPham(i).replace("'", "''");
            s.append("UPDATE `data_item` SET `name` = '").append(ten)
             .append("'\n WHERE `id` = ").append(real.SkinLinhThu.ID_ITEM[i]).append(";\n\n");
        }

        s.append("-- Kiem tra: phai ra dung 5 dong, moi ten deu co dong \"[he ...]\".\n");
        s.append("SELECT `id`, `name` FROM `data_item` WHERE `id` BETWEEN 905 AND 909 ORDER BY `id`;\n");

        final Writer w = new OutputStreamWriter(new FileOutputStream(TEP), StandardCharsets.UTF_8);
        try {
            w.write(s.toString());
        } finally {
            w.close();
        }
        System.out.println("Da ghi " + TEP + " (" + real.SkinLinhThu.SO_SKIN + " vat pham).");
    }

    private SinhSqlTenSkinThu() {
    }
}
