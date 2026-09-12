import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * saveCharAuto (ban tu luu dinh ky/theo hanh dong) phai luu DAY DU moi bang PHU cua
 * nhan vat - khong duoc de rieng cho saveChar (ban luu day du, CHI chay luc dang xuat
 * that su hoac may chu tat, xem RealController.saveAllChar).
 *
 * VI SAO CAN (bao 12/09/2026: "dung tranh nhieu khi lai bi mat" - "tranh" = tu binh Mai/
 * Lan/Cuc/Truc). Cac ham nhu Char.doAddManhTubinh() khong tu goi luu nao ca - chi doi RAM
 * (mang dem: listGemitem[]/listSpItem[]/self.tubinh/self.fruit[]) roi gui goi tin cho
 * client, tin tuong mot lan luu SAU DO se ghi xuong CSDL. Cac mang dem CHINH (listGemitem,
 * listSpItem...) DA duoc saveCharAuto luu tu truoc (dinh ky 5-11 phut/lan qua
 * CharManager, va rat nhieu hanh dong khac goi ngay sau khi xong), nhung BA bang PHU rieng
 * (tob_char_tubinh, tob_sp_new, tob_char_fruit) truoc day CHI duoc saveChar goi. Server
 * crash / nguoi choi mat ket noi dot ngot GIUA luc dung va luc dang xuat that su: mang
 * dem chinh (vd so mon da mua, so manh) bi tru THAT (da luu tu lan saveCharAuto gan
 * nhat) nhung du lieu trong bang phu (mon dac biet da mua, tien do ghep tranh, tien do
 * cay an qua) thi mat vi chua tung duoc luu - dung nguyen nhan nguoi choi bao lai.
 *
 * Sua (12/09/2026): them ca ba loi goi (saveCharTubinh, saveCharSP, saveCharFruit) vao
 * than DatabaseNhanVat.saveCharAuto, cung mot khoi voi saveCharGem (if (p.charDBID > 0)).
 * Ba bang nay duoc phat hien bang cach doi chieu toan bo cac loi goi "db.saveXxx(...)"
 * giua saveChar va saveCharAuto - bat ky ham save* nao CHI co trong saveChar ma khong co
 * trong saveCharAuto la mot ung vien nghi ngo (savePet/saveCharSP1 khong tinh, xem duoi).
 */
public final class KiemSaveCharAutoDuBangPhu {

    private KiemSaveCharAutoDuBangPhu() {
    }

    public static void chay() {
        Kiem.nhom("saveCharAuto phai luu du cac bang phu (tu binh, vat pham dac biet, cay an qua)");

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

        final int viTriGem = thanHam.indexOf("db.saveCharGem(soluong.toString()");
        Kiem.dung("tim thay diem neo db.saveCharGem(...) (moc ban dau cua khoi if charDBID > 0)",
                viTriGem >= 0);

        kiemMotBangPhu(thanHam, viTriGem, "saveCharTubinh",
                "db.saveCharTubinh(p.charDBID, p.getInfoSaveTubinh());", "tu binh (ghep tranh Mai/Lan/Cuc/Truc)");
        kiemMotBangPhu(thanHam, viTriGem, "saveCharSP",
                "db.saveCharSP(soluong3.toString(), p.charDBID);", "vat pham dac biet mua trong shop");
        kiemMotBangPhu(thanHam, viTriGem, "saveCharFruit",
                "db.saveCharFruit(p.charDBID, p.getInfoSaveFruit());", "cay an qua");

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

        // Doi chieu TOAN BO cac loi goi "db.saveXxx(...)"/"Database.instance.saveXxx(...)"
        // giua saveChar va saveCharAuto - phong khi sau nay lai co mot bang phu MOI duoc
        // them vao saveChar ma quen them vao saveCharAuto (dung mot loi voi lan nay).
        // savePet: KHONG tinh - da co rat nhieu loi goi truc tiep rai rac khap noi ngay
        // sau moi hanh dong doi chi so pet (Char.java, Map.java, MapDoMenu.java...), nen
        // KHONG can nam trong saveCharAuto. saveCharSP1/saveCharFruit1: ban "1" dung cho
        // mot ket noi CSDL phu (getConnection1) rieng, khong lien quan saveCharAuto.
        final String saveChar = noiDung.substring(
                noiDung.indexOf("static boolean saveChar(Database db, Char p, boolean admindExit)"), viTriHam);
        final java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\b(?:db|Database\\.instance)\\.(save\\w+)\\(").matcher(saveChar);
        final java.util.Set<String> trongSaveChar = new java.util.TreeSet<>();
        while (m.find()) {
            trongSaveChar.add(m.group(1));
        }
        final java.util.Set<String> boQuaCoY = new java.util.HashSet<>(java.util.Arrays.asList(
                "savePet", "saveCharSP1", "saveCharFruit1", "saveOrtherLog", "saveAnimal",
                "saveFarm", "saveTrainLuong", "savecharLuong", "saveCharQuest"));
        for (final String ten : trongSaveChar) {
            if (boQuaCoY.contains(ten)) {
                continue;
            }
            Kiem.dung("saveChar co goi " + ten + "(...) thi saveCharAuto CUNG phai co (tranh lo mot bang phu moi)",
                    thanHam.contains("." + ten + "("));
        }

        // Lo hong thu hai (bao 12/09/2026 "dung xong, thoat ra vao lai la ve 0"): dong
        // tob_char_tubinh chi duoc addCharTubinh tao o lan dang nhap dau tien co lv >= 40.
        // Nhan vat len 40 ngay trong phien (web "dat cap do" tu lv 2 len 90, hay len cap tu
        // nhien) roi ghep manh -> "update ... where id_char" khong trung dong nao -> khong
        // luu gi. saveCharTubinh phai la upsert (id_char la PRIMARY KEY).
        final String database = doc("src/data/Database.java");
        final int viTriSaveTubinh = database.indexOf("public void saveCharTubinh(int idDBChar, String[] info)");
        Kiem.dung("tim thay Database.saveCharTubinh", viTriSaveTubinh >= 0);
        if (viTriSaveTubinh >= 0) {
            final String thanSave = database.substring(viTriSaveTubinh,
                    database.indexOf("public void addCharTubinh(", viTriSaveTubinh)).toLowerCase();
            Kiem.dung("saveCharTubinh chen dong neu chua co (insert ... on duplicate key update)",
                    thanSave.contains("insert into tob_char_tubinh") && thanSave.contains("on duplicate key update"));
            Kiem.sai("saveCharTubinh khong con la update thuan (se im lang bo qua nhan vat chua co dong)",
                    thanSave.contains("\"update tob_char_tubinh"));
        }

        // Nguoc lai, KHONG duoc ghi RAM trong len dong that: tubinh/fruit chi duoc nap luc
        // dang nhap (lv >= 40 / >= 34), Char duoi cap hay dang nap do co tubinh rong, fruit
        // toan null -> ca saveChar lan saveCharAuto phai co dieu kien truoc khi ghi.
        final String saveCharDayDu = noiDung.substring(
                noiDung.indexOf("static boolean saveChar(Database db, Char p, boolean admindExit)"), viTriHam);
        for (final String[] ham : new String[][] {{"saveChar", saveCharDayDu}, {"saveCharAuto", thanHam}}) {
            final int goiTubinh = ham[1].indexOf("db.saveCharTubinh(p.charDBID, p.getInfoSaveTubinh());");
            final int goiFruit = ham[1].indexOf("db.saveCharFruit(p.charDBID, p.getInfoSaveFruit());");
            Kiem.dung(ham[0] + ": chi saveCharTubinh khi !p.tubinh.isEmpty()", goiTubinh > 0
                    && ham[1].lastIndexOf("if (!p.tubinh.isEmpty())", goiTubinh) > goiTubinh - 120);
            Kiem.dung(ham[0] + ": chi saveCharFruit khi daNapCayAnQua(p)", goiFruit > 0
                    && ham[1].lastIndexOf("if (daNapCayAnQua(p))", goiFruit) > goiFruit - 120);
        }
        Kiem.dung("daNapCayAnQua: co it nhat mot o fruit khac null moi coi la da nap",
                noiDung.contains("static boolean daNapCayAnQua(Char p)")
                        && noiDung.contains("if (p.fruit[i] != null) {"));
    }

    private static void kiemMotBangPhu(final String thanHam, final int viTriGem,
            final String tenHam, final String dongGoiDayDu, final String moTa) {
        final int viTriGoi = thanHam.indexOf(dongGoiDayDu);
        Kiem.dung("saveCharAuto goi db." + tenHam + "(...) - " + moTa, viTriGoi >= 0);
        // Khoi if (p.charDBID > 0) gom ca chu thich giai thich hai lo hong (~3000 ky tu);
        // "cung khoi" = nam sau saveCharGem va truoc khi khoi do dong (dau "}" + catch).
        final int ketKhoi = thanHam.indexOf("} catch (Exception ignored)", viTriGem);
        Kiem.dung(tenHam + " nam trong CUNG mot khoi voi saveCharGem (sau if (p.charDBID > 0))",
                viTriGem >= 0 && viTriGoi > viTriGem && ketKhoi > viTriGoi);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
