import data.Pet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hoa hinh Pet (port tu ban KPAH khac tai Downloads, xem README.md).
 *
 * Pet.java song trong package data va dung Actor/Map/Char that - khong the
 * doc lap hoan toan nhu KiemBangCapDo/KiemLichSuKien. Bu lai bang cach: phan
 * co the goi that (endHoaHinhPet, initKhangXuyenGiapWhenHoaHinh,
 * applyKhangXuyenGiap, getDBInfo/initDbInfo roundtrip) thi goi THAT, khong
 * doan; phan noi day UI (RealController case 13, menu NPC -8, moc CharChiSo,
 * don CSDL luc nap) thi quet van ban nguon nhu KiemVongQuayThanTai.
 */
public final class KiemHoaHinhPet {

    private KiemHoaHinhPet() {
    }

    public static void chay() {
        Kiem.nhom("Hoa hinh Pet - field, luu CSDL, khang xuyen giap");

        // ===== 1) Gia tri mac dinh tren mot Pet moi tinh =====
        final Pet p0 = new Pet(0);
        Kiem.bang("Pet moi: idHoaHinhPet = -1", Short.valueOf((short) -1), Short.valueOf(p0.idHoaHinhPet));
        Kiem.bang("Pet moi: capHoaHinh = 0", Byte.valueOf((byte) 0), Byte.valueOf(p0.capHoaHinh));
        Kiem.bang("Pet moi: khangXuyenGiapCap = 0", Byte.valueOf((byte) 0), Byte.valueOf(p0.khangXuyenGiapCap));
        Kiem.dung("Pet moi: endHoaHinhPet() = false (chua hoa hinh)", !p0.endHoaHinhPet());
        Kiem.bang("Pet moi: applyKhangXuyenGiap khong doi sat thuong (chua hoa hinh)",
                Integer.valueOf(1000), Integer.valueOf(p0.applyKhangXuyenGiap(1000)));

        // ===== 2) endHoaHinhPet() =====
        final Pet pHan = new Pet(0);
        pHan.idHoaHinhPet = 67;
        pHan.timeEndHoaHinhPet = System.currentTimeMillis() + 60000L;
        Kiem.dung("idHoaHinhPet != -1, con han -> endHoaHinhPet() = false", !pHan.endHoaHinhPet());
        pHan.timeEndHoaHinhPet = System.currentTimeMillis() - 60000L;
        Kiem.dung("idHoaHinhPet != -1, het han -> endHoaHinhPet() = true", pHan.endHoaHinhPet());
        final Pet pChuaHH = new Pet(0);
        pChuaHH.timeEndHoaHinhPet = System.currentTimeMillis() - 60000L;
        Kiem.dung("idHoaHinhPet = -1 (chua hoa hinh) -> endHoaHinhPet() = false du moc da qua",
                !pChuaHH.endHoaHinhPet());

        // ===== 3) initKhangXuyenGiapWhenHoaHinh() dong bo dung capHoaHinh =====
        final Pet pDb = new Pet(0);
        pDb.capHoaHinh = 4;
        pDb.initKhangXuyenGiapWhenHoaHinh();
        Kiem.bang("initKhangXuyenGiapWhenHoaHinh: khangXuyenGiapCap = capHoaHinh",
                Byte.valueOf((byte) 4), Byte.valueOf(pDb.khangXuyenGiapCap));

        // ===== 4) applyKhangXuyenGiap: het han thi khong giam du con cap =====
        final Pet pHetHanConCap = new Pet(0);
        pHetHanConCap.idHoaHinhPet = 67;
        pHetHanConCap.capHoaHinh = 5;
        pHetHanConCap.khangXuyenGiapCap = 5;
        pHetHanConCap.timeEndHoaHinhPet = System.currentTimeMillis() - 1000L;
        Kiem.bang("het han (con capHoaHinh=5) -> applyKhangXuyenGiap khong doi sat thuong",
                Integer.valueOf(500), Integer.valueOf(pHetHanConCap.applyKhangXuyenGiap(500)));

        // ===== 5) Bang gia tri giam theo cap (dung mau lon de vuot qua ngau nhien) =====
        // Voi moi cap, gia tri sat thuong SAU KHI GIAM phai dung CHINH XAC cong
        // thuc - neu proc xay ra ma ra so khac, sai cong thuc se lam test hong
        // ngay, khong can doi ty le ngau nhien khop tuyet doi.
        krThuGiam(1, 5);
        krThuGiam(2, 10);
        krThuGiam(3, 15);
        krThuGiam(4, 20);
        krThuGiam(5, 25);

        // ===== 6) Cooldown: ngay sau khi proc, lan goi ke tiep tren CUNG mot Pet
        // phai bi chan (tra ve nguyen) cho toi khi het cooldown =====
        boolean thayDoiCoDuocChanCooldown = false;
        for (int lan = 0; lan < 500 && !thayDoiCoDuocChanCooldown; lan++) {
            final Pet pCd = new Pet(0);
            pCd.idHoaHinhPet = 67;
            pCd.capHoaHinh = 5;
            pCd.khangXuyenGiapCap = 5;
            pCd.timeEndHoaHinhPet = System.currentTimeMillis() + 3600000L;
            final int lan1 = pCd.applyKhangXuyenGiap(1000);
            if (lan1 != 1000) {
                // Vua proc (giam con 750) -> cooldown 60s vua duoc dat, goi ngay
                // lan nua PHAI bi chan.
                final int lan2 = pCd.applyKhangXuyenGiap(1000);
                Kiem.bang("cap 5: goi ngay sau khi proc bi chan boi cooldown (khong giam tiep)",
                        Integer.valueOf(1000), Integer.valueOf(lan2));
                thayDoiCoDuocChanCooldown = true;
            }
        }
        Kiem.dung("kiem duoc it nhat mot lan proc trong 500 lan thu (cap 5, ty le 30%) de kiem tra cooldown",
                thayDoiCoDuocChanCooldown);

        // ===== 7) getDBInfo()/initDbInfo() roundtrip =====
        final Pet a = new Pet(0);
        a.idTemplate = 0;
        a.timeBuy = 111L;
        a.totalMinute = 0;
        a.type = 1;
        a.lvDetail.setExp(222L);
        a.powerEat = 3;
        a.ispetthoihan = false;
        a.idHoaHinhPet = 67;
        a.timeEndHoaHinhPet = 9999999999L;
        a.khangXuyenGiapCap = 3;
        a.capHoaHinh = 3;
        final String infoMoi = a.getDBInfo();
        final Pet b = new Pet("ten test", (byte) 0, 1, infoMoi, null, "2020-01-01 00:00:00.0");
        Kiem.bang("roundtrip getDBInfo/initDbInfo: idHoaHinhPet", Short.valueOf((short) 67), Short.valueOf(b.idHoaHinhPet));
        Kiem.bang("roundtrip getDBInfo/initDbInfo: timeEndHoaHinhPet", Long.valueOf(9999999999L), Long.valueOf(b.timeEndHoaHinhPet));
        Kiem.bang("roundtrip getDBInfo/initDbInfo: khangXuyenGiapCap", Byte.valueOf((byte) 3), Byte.valueOf(b.khangXuyenGiapCap));
        Kiem.bang("roundtrip getDBInfo/initDbInfo: capHoaHinh", Byte.valueOf((byte) 3), Byte.valueOf(b.capHoaHinh));
        Kiem.bang("roundtrip: truong cu (powerEat) khong bi anh huong", Short.valueOf((short) 3), Short.valueOf(b.powerEat));

        // ===== 8) Tuong thich nguoc: chuoi CU (13 truong, chua co hoa hinh) van
        // doc duoc, khong nem loi, va mac dinh dung -1/0/0/0 =====
        final String infoCu = "0,111,0,1,222,3,0,1,2,3,4,0,2"; // dung 13 truong nhu truoc khi them hoa hinh
        final Pet c = new Pet("ten cu", (byte) 0, 1, infoCu, null, "2020-01-01 00:00:00.0");
        Kiem.bang("chuoi cu (13 truong): idHoaHinhPet mac dinh -1", Short.valueOf((short) -1), Short.valueOf(c.idHoaHinhPet));
        Kiem.bang("chuoi cu (13 truong): timeEndHoaHinhPet mac dinh 0", Long.valueOf(0L), Long.valueOf(c.timeEndHoaHinhPet));
        Kiem.bang("chuoi cu (13 truong): khangXuyenGiapCap mac dinh 0", Byte.valueOf((byte) 0), Byte.valueOf(c.khangXuyenGiapCap));
        Kiem.bang("chuoi cu (13 truong): capHoaHinh mac dinh 0", Byte.valueOf((byte) 0), Byte.valueOf(c.capHoaHinh));
        Kiem.bang("chuoi cu (13 truong): truong cu van doc dung (powerEat)", Short.valueOf((short) 3), Short.valueOf(c.powerEat));

        // ===== 9) getIdEffPet(): hoa hinh phai LAN AT hieu ung theo cap thuong =====
        final Pet pRong = new Pet(0);
        pRong.idTemplate = 0;
        pRong.itemPet = null;
        Kiem.dung("getIdEffPet() = -1 khi chua co itemPet (khong lien quan hoa hinh)", pRong.getIdEffPet() == -1);

        quetVanBanNguon();
    }

    private static void krThuGiam(final int cap, final int reducePercentMongDoi) {
        final int baseDame = 1000000; // so lon de con so sau giam la duy nhat, de nhan biet dung/sai
        final int mongDoi = baseDame - baseDame * reducePercentMongDoi / 100;
        boolean thayDoi = false;
        for (int lan = 0; lan < 2000 && !thayDoi; lan++) {
            final Pet p = new Pet(0);
            p.idHoaHinhPet = 67;
            p.capHoaHinh = (byte) cap;
            p.khangXuyenGiapCap = (byte) cap;
            p.timeEndHoaHinhPet = System.currentTimeMillis() + 3600000L;
            final int ketQua = p.applyKhangXuyenGiap(baseDame);
            if (ketQua != baseDame) {
                Kiem.bang("cap " + cap + ": giam dung " + reducePercentMongDoi + "% khi proc",
                        Integer.valueOf(mongDoi), Integer.valueOf(ketQua));
                thayDoi = true;
            }
        }
        Kiem.dung("cap " + cap + ": thay duoc it nhat 1 lan proc trong 2000 lan thu (de kiem cong thuc giam)", thayDoi);
    }

    private static void quetVanBanNguon() {
        Kiem.nhom("Hoa hinh Pet - noi day UI (menu NPC, RealController, CharChiSo, Database)");

        final String menuNpc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        Kiem.dung("doc duoc RequestNPCInfoHandler.java", !menuNpc.isEmpty());
        Kiem.dung("menu NPC -8 co muc \"Hoá Hình Pet\"", menuNpc.contains("\"Hoá Hình Pet\""));

        final String realController = doc("src/real/RealController.java");
        Kiem.dung("doc duoc RealController.java", !realController.isEmpty());
        Kiem.dung("co case 13 (Hoa hinh Pet)", realController.contains("case 13:"));
        Kiem.dung("kiem tra itemPet.level >= 2 (da sieu tien hoa)", realController.contains("player.petUsing.itemPet.level < 2"));
        Kiem.dung("kiem tra capHoaHinh toi da 5", realController.contains("player.petUsing.capHoaHinh >= 5"));
        Kiem.dung("dung item 268 (lo thuoc hoa hinh co san, khong tao item moi)",
                realController.contains("player.listGemitem[268]") && realController.contains("player.delGem(268,"));
        Kiem.dung("tru 5000 luong", realController.contains("player.subLuong(5000L)"));
        Kiem.dung("gan idHoaHinhPet = 67 (hieu ung Lan, dung chung moi cap)", realController.contains("player.petUsing.idHoaHinhPet = 67"));
        Kiem.dung("thoi han 10 ngay (864_000_000L ms)", realController.contains("864_000_000L"));
        Kiem.dung("co luu CSDL sau khi hoa hinh", realController.contains("Database.instance.savePet(player.petUsing);"));

        final String charChiSo = doc("src/real/CharChiSo.java");
        Kiem.dung("doc duoc CharChiSo.java", !charChiSo.isEmpty());
        Kiem.dung("applyThanKhiOnHit co goi applyKhangXuyenGiap cho phe bi danh",
                charChiSo.contains("petBiDanh.applyKhangXuyenGiap(finalDame)"));

        final String database = doc("src/data/Database.java");
        Kiem.dung("doc duoc Database.java", !database.isEmpty());
        Kiem.dung("getCharPet don hoa hinh het han luc nap nhan vat", database.contains("pet.endHoaHinhPet()"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
