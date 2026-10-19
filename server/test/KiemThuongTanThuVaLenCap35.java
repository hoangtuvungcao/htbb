import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hai qua moi theo yeu cau nguoi dung (12-13/09/2026):
 *   1. (DA BO 19/10/2026 - xem KiemKinhTeKhoiDau) Nhan vat DAU TIEN cua tai khoan: 40.000.000 xu + 20.000 luong mo + 30.000 luong
 *      khoa (THAY THE hoan toan bo cu 10tr/5k/100k) + 3 trung MOI LOAI trong 5 trung pet
 *      thuong (Cu/Rong/Yeu Tinh/Doi/Dai bang, id 684-688) + 2 Huyet linh thao KHOA (id 720).
 *   2. Len cap 35: 10.000.000 xu + 50 nguyen lieu KHOA moi loai cap 6 (ca nlsc va nlcc,
 *      moi nhom 5 loai - GemTemplate.ID_MATERIAL_LOW[5]/ID_MATERIAL_HIGHT[5]).
 *
 * Qua (1) khong the phat luc addChar (chua co Char song de nhet vat pham vao tui) nen
 * duoc tach lam hai: DatabaseNhanVat.addChar chi doi so xu/luong/luongK trong hang doi
 * board_created co san; DatabaseXepHang.getBoardCreated (noi Char nhan hang doi do luc
 * dang nhap lan dau) duoc cong THEM phan vat pham, cung mot lan voi xu/luong.
 *
 * Qua (2) chi nhan tai NPC tu cap 35, khong tu dong phat khi len cap.
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
        // 19/10/2026: qua tao nhan vat (40tr xu / 20k luong / 30k luong khoa) DA BO - chu game duyet.
        Kiem.sai("addChar KHONG con ghi board_created (qua tao nhan vat da bo)",
                dbNhanVat.contains("INSERT INTO board_created"));
        Kiem.sai("KHONG con bo so 40tr/20k/30k", dbNhanVat.contains("VALUES(40000000,20000,30000,?)"));
        Kiem.sai("KHONG con bo so cu (10tr/5k/100k)",
                dbNhanVat.contains("VALUES(10000000,5000,100000,?)"));
        Kiem.sai("KHONG con xu khoi dau 100.000", dbNhanVat.contains("xu = 100000L;"));

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
        Kiem.dung("getBoardCreated tao 2 Huyet linh thao KHOA (Map.doCreateHuyetLinhThao)",
                thanHam.contains("for (int i = 0; i < 2; ++i)")
                        && thanHam.contains("Map.doCreateHuyetLinhThao(player, 1)"));
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

        final int viTriHam = ma.indexOf("static void thuongLenCap35(Char p)");
        Kiem.dung("tim thay dinh nghia thuongLenCap35()", viTriHam >= 0);
        if (viTriHam < 0) {
            return;
        }
        final int ketThucHam = ma.indexOf("\n    static void doGetItem(", viTriHam);
        final String thanHam = ma.substring(viTriHam, ketThucHam > viTriHam ? ketThucHam : ma.length());

        Kiem.dung("qua NPC kiem tra dieu kien va danh dau da nhan",
                thanHam.contains("!p.canReceiveGif35()")
                && thanHam.contains("p.thuongLv35 = 1;") && thanHam.contains("p.gif35 = 1;"));
        Kiem.dung("thuong 10.000.000 xu", thanHam.contains("p.addXu(10000000L,"));
        Kiem.dung("50 nlsc cap 6 (ID_MATERIAL_LOW[5]) khoa",
                thanHam.contains("GemTemplate.ID_MATERIAL_LOW[5][i], 50, true"));
        Kiem.dung("50 nlcc cap 6 (ID_MATERIAL_HIGHT[5]) khoa",
                thanHam.contains("GemTemplate.ID_MATERIAL_HIGHT[5][i], 50, true"));

        Kiem.sai("len cap khong tu dong phat qua", ma.contains("thuongLenCap35(p);"));
        final String npc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        final String menu = doc("src/real/MapDoMenu.java");
        Kiem.dung("menu va xu ly NPC dung chung dieu kien nhan qua",
                npc.contains("p.canReceiveGif35()") && menu.contains("player.canReceiveGif35()"));
        // Truong luu tru: khai bao + khoi tao + luu + doc, chiu duoc save cu chua co cot nay.
        final String charJava = doc("src/real/Char.java");
        Kiem.dung("NPC chi cho nhan dung cap 35, chan cap khac va ca hai co da nhan",
                charJava.contains("this.lvDetail.lv == 35 && this.thuongLv35 == 0 && this.gif35 == 0"));
        Kiem.dung("nhan qua qua NPC", charJava.contains("MapXuLyLenh.thuongLenCap35(this);"));
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
