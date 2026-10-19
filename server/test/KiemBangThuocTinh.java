import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bang data_attribute trong ban seed phai LIEN TUC tu id 0 den 186.
 *
 * VI SAO PHAI CO PHEP KIEM NAY. Ten thuoc tinh duoc tra theo VI TRI trong danh sach nap
 * luc khoi dong, khong theo id: otherAtt[i] -> ALL_NAME_ATTRIBUTE_ITEM.get(i + 61)
 * (xem real/Item.java va real/Map.java). Thieu mot id o giua la moi ten sau do lech mot
 * bac; con thieu o cuoi thi goi dang nhap cmd 25 gui it dong hon so client can, client tra
 * key khong co roi CHET GIUA LUC VE.
 *
 * Trieu chung that da gap (xem PHAN J trong docs/trien-khai.md): bam vao Than Khi
 * la DO CLIENT, ma server KHONG GHI MOT DONG LOG NAO. Nguyen nhan cach trieu chung rat xa
 * nen truoc day mat rat lau moi lan ra: ban seed chi co 141 dong (id 0..140) trong khi
 * module Than Khi dung toi id 186.
 *
 * Tu 02/09/2026 ban seed da co du 187 dong dau (lay tu VPS), nen dung server moi khong con
 * phai chay tay khoi PHAN 2C cua vps/db_new_server.sql nua. Phep kiem nay giu cho dieu do
 * khong am tham mat di khi ai do dump lai ban seed.
 *
 * Doc THANG tu file SQL chu khong qua CSDL: bo test phai chay duoc khi khong co MySQL.
 */
public final class KiemBangThuocTinh {

    private static final String NGUON = "../du_lieu/sql_nguon/gamedb7_clean.sql";

    /** Module Than Khi dung toi id 186, nen 0..186 phai co du. */
    private static final int ID_CAN_LIEN_TUC_DEN = 186;

    /** Bat dau moi tuple: "(<id>,'<ten>'..." - chi can id la du cho phep kiem nay. */
    private static final Pattern MOC = Pattern.compile("\\((\\d+)\\s*,");

    public static void chay() {
        Kiem.nhom("Bang data_attribute trong ban seed - id phai lien tuc 0..186");

        final String sql = doc(NGUON);
        Kiem.dung("doc duoc " + NGUON, sql.length() > 500);
        if (sql.isEmpty()) {
            return;
        }

        // Chi lay trong khoi INSERT cua data_attribute, khong quet ca file (bang khac cung
        // co tuple dang "(<so>,").
        final StringBuilder khoi = new StringBuilder();
        final Matcher mKhoi = Pattern.compile(
                "INSERT INTO `data_attribute`[^V]*VALUES\\s*([\\s\\S]*?);").matcher(sql);
        while (mKhoi.find()) {
            khoi.append(mKhoi.group(1)).append('\n');
        }
        Kiem.dung("tim thay khoi INSERT INTO `data_attribute`", khoi.length() > 0);
        if (khoi.length() == 0) {
            return;
        }

        final Set<Integer> co = new HashSet<Integer>();
        final Matcher m = MOC.matcher(khoi);
        while (m.find()) {
            co.add(Integer.valueOf(Integer.parseInt(m.group(1))));
        }

        Kiem.dung("co it nhat 187 dong (id 0..186)", co.size() >= ID_CAN_LIEN_TUC_DEN + 1);

        // Diem cot loi: KHONG duoc thieu id nao trong 0..186.
        int thieu = 0;
        int thieuDauTien = -1;
        for (int id = 0; id <= ID_CAN_LIEN_TUC_DEN; id++) {
            if (!co.contains(Integer.valueOf(id))) {
                thieu++;
                if (thieuDauTien < 0) {
                    thieuDauTien = id;
                }
            }
        }
        Kiem.bang("khong thieu id nao trong 0..186"
                        + (thieuDauTien < 0 ? "" : " (thieu dau tien: id " + thieuDauTien + ")"),
                Integer.valueOf(0), Integer.valueOf(thieu));

        // Hai moc doi chieu tay, phong ca phep dem tren cung sai.
        Kiem.dung("co id 0 (dau danh sach)", co.contains(Integer.valueOf(0)));
        Kiem.dung("co id 186 (moc Than Khi dung toi)",
                co.contains(Integer.valueOf(ID_CAN_LIEN_TUC_DEN)));

        // Tai lieu phai con noi ro cai bay nay - go tai lieu di thi nguoi sau khong hieu
        // vi sao phep kiem nay ton tai.
        final String tl = doc("../docs/trien-khai.md");
        Kiem.dung("tai lieu con canh bao ve data_attribute", tl.contains("data_attribute"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemBangThuocTinh() {
    }
}
