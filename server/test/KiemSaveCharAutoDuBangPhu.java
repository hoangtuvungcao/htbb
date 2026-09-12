import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Ghep manh tu binh (Mai/Lan/Cuc/Truc, Char.doAddManhTubinh) phai duoc luu ngay boi
 * saveCharAuto - khong duoc CHI cho saveChar (ban luu day du luc dang xuat that su).
 *
 * VI SAO CAN (bao 12/09/2026: "dung tranh nhieu khi lai bi mat"). doAddManhTubinh khong
 * tu goi luu nao ca - no chi doi RAM (self.tubinh, listGemitem[]) roi gui goi tin cho
 * client, tin tuong mot lan luu SAU DO se ghi xuong CSDL. Manh gem (listGemitem/
 * listGemitemLock) DA duoc DatabaseNhanVat.saveCharAuto luu tu truoc (dinh ky 5-11 phut/
 * lan qua CharManager, va rat nhieu hanh dong khac goi ngay sau khi xong - xem
 * Char.java, doUsePotion.java, LenhWeb.java...), nhung tien do ghep tranh (bang
 * tob_char_tubinh, ham saveCharTubinh) truoc day CHI duoc DatabaseNhanVat.saveChar goi -
 * ham nay CHI chay luc nguoi choi dang xuat that su hoac may chu tat (RealController.
 * saveAllChar). Server crash / nguoi choi mat ket noi dot ngot GIUA luc ghep va luc dang
 * xuat that su: manh bi tru THAT (da luu tu lan saveCharAuto gan nhat) nhung tien do
 * ghep tranh thi mat vi chua tung duoc luu - dung nguyen nhan nguoi choi bao lai.
 *
 * Sua: them db.saveCharTubinh(...) vao than DatabaseNhanVat.saveCharAuto, ngay SAU
 * db.saveCharGem(...) (cung mot khoi if (p.charDBID > 0), giu nguyen thu tu de neu sau
 * nay ai doi lai thu tu thi test o duoi van bat duoc).
 */
public final class KiemTuBinhKhongMatKhiAutoSave {

    private KiemTuBinhKhongMatKhiAutoSave() {
    }

    public static void chay() {
        Kiem.nhom("Tu binh (ghep manh tranh) - phai luu ngay boi saveCharAuto, khong doi den saveChar");

        final String noiDung = doc("src/data/DatabaseNhanVat.java");
        Kiem.dung("doc duoc src/data/DatabaseNhanVat.java", !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        final int viTriHam = noiDung.indexOf("static boolean saveCharAuto(Database db, Char p)");
        Kiem.dung("tim thay dinh nghia saveCharAuto", viTriHam >= 0);
        if (viTriHam < 0) {
            return;
        }
        // Ham ke tiep (addChar) danh dau diem ket cua saveCharAuto, dung de gioi han pham
        // vi tim kiem - tranh khop nham sang cac ham khac cung ten bien/loi goi.
        final int ketThucHam = noiDung.indexOf("static int addChar(Database db, Session s,", viTriHam);
        Kiem.dung("tim thay diem ket cua saveCharAuto (truoc ham addChar)", ketThucHam > viTriHam);
        final String thanHam = noiDung.substring(viTriHam, Math.max(viTriHam, ketThucHam));

        Kiem.dung("saveCharAuto goi db.saveCharTubinh(...)", thanHam.contains("db.saveCharTubinh("));
        Kiem.dung("saveCharAuto dung p.getInfoSaveTubinh() lam tham so (doc dung RAM hien tai)",
                thanHam.contains("db.saveCharTubinh(p.charDBID, p.getInfoSaveTubinh());"));

        final int viTriGem = thanHam.indexOf("db.saveCharGem(soluong.toString()");
        final int viTriTubinh = thanHam.indexOf("db.saveCharTubinh(");
        Kiem.dung("tim thay ca hai cho goi (saveCharGem va saveCharTubinh)",
                viTriGem >= 0 && viTriTubinh >= 0);
        Kiem.dung("saveCharTubinh nam trong CUNG mot khoi voi saveCharGem (sau if (p.charDBID > 0))",
                viTriGem >= 0 && viTriTubinh > viTriGem && viTriTubinh - viTriGem < 1000);

        // doAddManhTubinh (Char.java) KHONG can tu goi saveCharAuto rieng - viec luu da
        // duoc dam bao chung boi saveCharAuto (dinh ky) va saveChar (dang xuat). Chi kiem
        // ham do van thuc su thay doi self.tubinh (khong bi xoa nham logic ghep) va van
        // chua tu goi luu rieng (tranh luu trung/loi thoi neu sau nay ai do them nham).
        final String charJava = doc("src/real/Char.java");
        final int viTriDoAdd = charJava.indexOf("public void doAddManhTubinh(");
        Kiem.dung("tim thay Char.doAddManhTubinh", viTriDoAdd >= 0);
        if (viTriDoAdd >= 0) {
            final int ketDoAdd = charJava.indexOf("\n    public void createFruit()", viTriDoAdd);
            final String thanDoAdd = charJava.substring(viTriDoAdd, Math.max(viTriDoAdd, ketDoAdd));
            Kiem.dung("doAddManhTubinh van goi temp.addManh(...) de ghep manh",
                    thanDoAdd.contains(".addManh(indexManh, this)"));
        }
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
