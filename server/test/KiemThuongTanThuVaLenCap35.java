import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hai qua moi theo yeu cau nguoi dung (12-13/09/2026):
 *   1. Nhan vat DAU TIEN cua tai khoan: 40.000.000 xu + 20.000 luong mo + 30.000 luong
 *      khoa (THAY THE hoan toan bo cu 10tr/5k/100k) + 3 trung MOI LOAI trong 5 trung pet
 *      thuong (Cu/Rong/Yeu Tinh/Doi/Dai bang, id 684-688) + 4 Huyet linh thao (id 720).
 *   2. Len cap 35: 10.000.000 xu + 50 nguyen lieu KHOA moi loai cap 6 (ca nlsc va nlcc,
 *      moi nhom 5 loai - GemTemplate.ID_MATERIAL_LOW[5]/ID_MATERIAL_HIGHT[5]).
 *
 * Qua (1) khong the phat luc addChar (chua co Char song de nhet vat pham vao tui) nen
 * duoc tach lam hai: DatabaseNhanVat.addChar chi doi so xu/luong/luongK trong hang doi
 * board_created co san; DatabaseXepHang.getBoardCreated (noi Char nhan hang doi do luc
 * dang nhap lan dau) duoc cong THEM phan vat pham, cung mot lan voi xu/luong.
 *
 * Qua (2) phai bat duoc luc LEN CAP, khong phai luc da o san cap 35: dung nguong ">= 35"
 * + co mot lan (Char.thuongLv35), KHONG dung "== 35" tuyet doi (bay da biet cua gif35 -
 * bi bo lo vinh vien khi nhan vat nhay nhieu cap cung luc qua giftcode/lenh web dat cap/
 * admin buff). Va phai goi tu CA HAI ham cong exp (addXpCharEvent, addXPForChar) - thieu
 * mot ben la mot duong len cap khong duoc thuong.
 */
public final class KiemThuongTanThuVaLenCap35 {

    private KiemThuongTanThuVaLenCap35() {
    }

    public static void chay() {
        Kiem.nhom("Qua nhan vat dau tien + thuong len cap 35");

        kiemQuaNhanVatDauTien();
        kiemThuongLenCap35();
    }

    private static void kiemQuaNhanVatDauTien() {
        final String dbNhanVat = doc("src/data/DatabaseNhanVat.java");
        Kiem.dung("doc duoc src/data/DatabaseNhanVat.java", !dbNhanVat.isEmpty());
        Kiem.dung("board_created dat dung so moi: 40tr xu, 20k luong mo, 30k luong khoa",
                dbNhanVat.contains("INSERT INTO board_created(xu,luong,luongK,username) VALUES(40000000,20000,30000,?)"));
        Kiem.sai("KHONG con bo so cu (10tr/5k/100k)",
                dbNhanVat.contains("VALUES(10000000,5000,100000,?)"));

        final String dbXepHang = doc("src/data/DatabaseXepHang.java");
        Kiem.dung("doc duoc src/data/DatabaseXepHang.java", !dbXepHang.isEmpty());
        final int viTriHam = dbXepHang.indexOf("static void getBoardCreated(Database db, Char player)");
        Kiem.dung("tim thay dinh nghia getBoardCreated", viTriHam >= 0);
        if (viTriHam < 0) {
            return;
        }
        final int ketThucHam = dbXepHang.indexOf("\n    static ", viTriHam + 10);
        final String thanHam = dbXepHang.substring(viTriHam, ketThucHam > viTriHam ? ketThucHam : dbXepHang.length());

        Kiem.dung("getBoardCreated tao 5 trung pet thuong (684..688) qua vong lap",
                thanHam.contains("for (int idTrung = 684; idTrung <= 688; ++idTrung)")
                        && thanHam.contains("Char.createItemSoLuong(idTrung, 3, player)"));
        Kiem.dung("getBoardCreated tao 4 Huyet linh thao (Map.doCreateHuyetLinhThao)",
                thanHam.contains("Map.doCreateHuyetLinhThao(player, 0)"));
        // Vat pham phai duoc cong SAU khi da "gianh" duoc hang (executeUpdate xoa thanh
        // cong), khong truoc - neu khong nguoi thua cuoc trong tranh chap hai may chu van
        // nhan duoc vat pham du khong nhan duoc xu/luong.
        final int viTriXoaHang = thanHam.indexOf("if (soDongXoa != 1)");
        final int viTriTrung = thanHam.indexOf("idTrung <= 688");
        Kiem.dung("vong lap tao trung nam SAU buoc kiem tra 'gianh duoc hang' (soDongXoa)",
                viTriXoaHang >= 0 && viTriTrung > viTriXoaHang);
    }

    private static void kiemThuongLenCap35() {
        final String ma = doc("src/real/MapXuLyLenh.java");
        Kiem.dung("doc duoc src/real/MapXuLyLenh.java", !ma.isEmpty());
        if (ma.isEmpty()) {
            return;
        }

        final int viTriHam = ma.indexOf("private static void thuongLenCap35(Char p)");
        Kiem.dung("tim thay dinh nghia thuongLenCap35()", viTriHam >= 0);
        if (viTriHam < 0) {
            return;
        }
        final int ketThucHam = ma.indexOf("\n    static void doGetItem(", viTriHam);
        final String thanHam = ma.substring(viTriHam, ketThucHam > viTriHam ? ketThucHam : ma.length());

        // Nguong ">= 35" + co mot lan - KHONG duoc dung "== 35" tuyet doi (bay cua gif35).
        Kiem.dung("dieu kien dung nguong 'lv < 35' (tuc >= 35 moi thuong), khong phai '== 35'",
                thanHam.contains("p.lvDetail.lv < 35"));
        Kiem.sai("KHONG dung dieu kien tuyet doi 'lv == 35' (bay cua gif35, bo lo khi nhay cap)",
                thanHam.contains("p.lvDetail.lv == 35"));
        Kiem.dung("co co mot lan rieng (thuongLv35 != 0 -> return, roi dat = 1)",
                thanHam.contains("p.thuongLv35 != 0") && thanHam.contains("p.thuongLv35 = 1;"));

        Kiem.dung("thuong 10.000.000 xu", thanHam.contains("p.addXu(10000000L,"));
        Kiem.dung("50 nlsc cap 6 (ID_MATERIAL_LOW[5]) khoa",
                thanHam.contains("GemTemplate.ID_MATERIAL_LOW[5][i], 50, true"));
        Kiem.dung("50 nlcc cap 6 (ID_MATERIAL_HIGHT[5]) khoa",
                thanHam.contains("GemTemplate.ID_MATERIAL_HIGHT[5][i], 50, true"));

        // Phai duoc goi tu CA HAI ham cong exp - dem so lan goi trong toan file (ngoai
        // chinh dinh nghia ham).
        final String GOI = "thuongLenCap35(p);";
        int soLan = 0;
        int i = ma.indexOf(GOI);
        while (i >= 0) {
            soLan++;
            i = ma.indexOf(GOI, i + GOI.length());
        }
        Kiem.bang("thuongLenCap35(p) duoc goi dung 2 lan (addXpCharEvent va addXPForChar)",
                Integer.valueOf(2), Integer.valueOf(soLan));

        final int viTriEvent = ma.indexOf("static void addXpCharEvent(");
        final int viTriForChar = ma.indexOf("static void addXPForChar(");
        Kiem.dung("tim thay ca hai ham cong exp", viTriEvent >= 0 && viTriForChar >= 0 && viTriEvent < viTriForChar);
        if (viTriEvent >= 0 && viTriForChar > viTriEvent) {
            final String thanEvent = ma.substring(viTriEvent, viTriForChar);
            final String thanForChar = ma.substring(viTriForChar, viTriHam);
            Kiem.dung("addXpCharEvent co goi thuongLenCap35(p)", thanEvent.contains(GOI));
            Kiem.dung("addXPForChar co goi thuongLenCap35(p)", thanForChar.contains(GOI));
        }

        // Truong luu tru: khai bao + khoi tao + luu + doc, chiu duoc save cu chua co cot nay.
        final String charJava = doc("src/real/Char.java");
        Kiem.dung("Char.java khai bao truong thuongLv35", charJava.contains("public byte thuongLv35;"));
        final String khoiTao = doc("src/real/CharKhoiTao.java");
        Kiem.dung("CharKhoiTao khoi tao thuongLv35 = 0", khoiTao.contains("self.thuongLv35 = 0;"));
        final String vongLap = doc("src/real/CharVongLap.java");
        Kiem.dung("CharVongLap luu thuongLv35 vao pInfo (o CUOI chuoi, sau timeAoHoaHoaKyLan)",
                vongLap.contains("self.timeAoHoaHoaKyLan;\n        pInfo = String.valueOf(pInfo) + \",\" + self.thuongLv35;"));
        final String napDuLieu = doc("src/real/CharNapDuLieu.java");
        Kiem.dung("CharNapDuLieu doc lai thuongLv35, boc try/catch rieng (khong vo save cu thieu cot)",
                napDuLieu.contains("self.thuongLv35 = Byte.parseByte(data[116]);"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
