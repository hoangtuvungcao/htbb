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

        // Hoa hinh Pet dung MOT frame chung cho moi loai. Lich su bo cuc:
        // 49-53 rieng theo loai (a081ee1) -> gop ve 69 chung (1d263ff, anh linh
        // thu boc lua) -> PET MAT TICH (01/09/2026, nguoi choi bao lai) vi
        // 69.png la anh VUONG 256x256 (luoi nhieu hang, dung cho he thong danh
        // hieu tren dau nhan vat) trong khi truong nay can anh DAI HEP (cao gap
        // doi rong tro len, giong 35/41/42/44 - cac id dung cho pet tien hoa
        // thuong) vi getTotalFrame() (so khung cat) lay tu rieng
        // Map.petTemplate, KHONG phu thuoc anh - anh sai bo cuc thi client cat
        // sai khung va khong ve duoc pet. Quay lai id 49 (45x114, dung bo cuc).
        // Test nay khoa BA dieu kien: code phai tra dung hang so
        // ID_HIEU_UNG_HOA_HINH_PET, file anh phai TON TAI, VA anh phai DAI HEP
        // (cao > rong) - thieu kiem tra hinh dang la dung y het loi 69 vua gap,
        // vi 69.png cung ton tai va cung nang hon 1000 byte, chi SAI BO CUC.
        final String petJava = doc("src/data/Pet.java");
        Kiem.dung("doc duoc Pet.java", !petJava.isEmpty());
        Kiem.dung("hang so ID_HIEU_UNG_HOA_HINH_PET = 49", petJava.contains("ID_HIEU_UNG_HOA_HINH_PET = 49"));
        final int dauHamEff = petJava.indexOf("public int getIdEffPet()");
        final int cuoiHamEff = petJava.indexOf("\n    }", dauHamEff);
        Kiem.dung("tim thay than ham getIdEffPet()", dauHamEff >= 0 && cuoiHamEff > dauHamEff);
        if (dauHamEff >= 0 && cuoiHamEff > dauHamEff) {
            final String thanEff = petJava.substring(dauHamEff, cuoiHamEff);
            Kiem.dung("getIdEffPet() co nhanh hoa hinh doc idHoaHinhPet", thanEff.contains("idHoaHinhPet"));
            Kiem.dung("nhanh hoa hinh tra ve DUNG hang so chung (khong con re theo loai isPetRong/isPetCu/...)",
                    thanEff.contains("return ID_HIEU_UNG_HOA_HINH_PET;")
                    && !thanEff.contains("return 49;") && !thanEff.contains("return 50;")
                    && !thanEff.contains("return 51;") && !thanEff.contains("return 52;")
                    && !thanEff.contains("return 53;"));
        }
        final java.io.File anhHoaHinh = new java.io.File("map/d_effect/49.png");
        Kiem.dung("map/d_effect/49.png ton tai tren dia va khong rong",
                anhHoaHinh.isFile() && anhHoaHinh.length() > 1000L);
        try {
            final java.awt.image.BufferedImage anh = javax.imageio.ImageIO.read(anhHoaHinh);
            Kiem.dung("map/d_effect/49.png doc duoc nhu anh PNG hop le", anh != null);
            if (anh != null) {
                Kiem.dung("map/d_effect/49.png la anh DAI HEP (cao > rong) - dung bo cuc pet hoa hinh, KHONG phai anh vuong nhu 69.png",
                        anh.getHeight() > anh.getWidth());
            }
        } catch (final java.io.IOException e) {
            Kiem.dung("doc duoc map/d_effect/49.png bang ImageIO (" + e + ")", false);
        }
        // BAO VE CHONG TAI DIEN LAN NUA: sau khi sua dung bo cuc (cao > rong),
        // pet VAN mat tich - hoa ra con mot khac biet nua: moi anh dang dung tot
        // o cho nay (41, 42, 44, va chinh 49 nguyen ban tu a081ee1) deu la PNG
        // colormap (color type 3 trong IHDR - anh bang mau/index), trong khi
        // ban ghep dau tien (dung BufferedImage.TYPE_INT_ARGB + ImageIO ghi mac
        // dinh) ra PNG truecolor+alpha (color type 6) - client hoac khong doc
        // duoc hoac ve sai voi kieu anh nay cho rieng duong ve pet nay. Sua
        // bang cach dung IndexColorModel de ep ve dung PNG colormap. Kiem tra
        // byte color type ngay trong IHDR (offset 25 - xem dinh dang PNG:
        // 8 byte signature + 4 byte do dai chunk + 4 byte "IHDR" + 4 rong +
        // 4 cao + 1 bit depth + 1 COLOR TYPE o day) de khoa dung dinh dang,
        // vi ImageIO.read() tu giai ma duoc CA HAI kieu nen khong the chi dua
        // vao "doc duoc" o kiem tra phia tren.
        try {
            final byte[] raw = Files.readAllBytes(Paths.get("map/d_effect/49.png"));
            Kiem.dung("map/d_effect/49.png du dai de doc byte color type trong IHDR", raw.length > 26);
            if (raw.length > 26) {
                Kiem.bang("map/d_effect/49.png la PNG kieu colormap/index (color type 3), giong moi anh khac dang dung o day - KHONG phai truecolor+alpha (6) nhu ban ghep dau tien",
                        Byte.valueOf((byte) 3), Byte.valueOf(raw[25]));
            }
        } catch (final IOException e) {
            Kiem.dung("doc duoc byte tho cua map/d_effect/49.png (" + e + ")", false);
        }

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
        Kiem.dung("gan idHoaHinhPet = 67 (co dinh cho moi cap - chi la co bao 'da hoa hinh', getIdEffPet() luon tra ve frame chung)",
                realController.contains("player.petUsing.idHoaHinhPet = 67"));
        Kiem.dung("thoi han 10 ngay (864_000_000L ms)", realController.contains("864_000_000L"));
        Kiem.dung("co luu CSDL sau khi hoa hinh", realController.contains("Database.instance.savePet(player.petUsing);"));

        final String charChiSo = doc("src/real/CharChiSo.java");
        Kiem.dung("doc duoc CharChiSo.java", !charChiSo.isEmpty());
        Kiem.dung("applyThanKhiOnHit co goi applyKhangXuyenGiap cho phe bi danh",
                charChiSo.contains("petBiDanh.applyKhangXuyenGiap(finalDame)"));

        final String database = doc("src/data/Database.java");
        Kiem.dung("doc duoc Database.java", !database.isEmpty());
        Kiem.dung("getCharPet don hoa hinh het han luc nap nhan vat", database.contains("pet.endHoaHinhPet()"));

        // getInfoSendUser() truoc day chi ghi "kháng xuyên giáp cấp N" ma khong
        // noi ro N la giam bao nhieu % / ti le bao nhieu / hoi chieu bao lau -
        // nguoi choi hoi lai (01/09/2026) vi khong biet cap 5 co gi. Sua bang
        // cach dung CHUNG thongSoKhangXuyenGiap() voi applyKhangXuyenGiap() (da
        // co test so 5-9 kiem tra dung gia tri qua lay mau thong ke), chi kiem
        // THEM o day la getInfoSendUser() co IN cac con so do ra, khong test lai
        // gia tri (khong the goi that getInfoSendUser() - can mot Item that lam
        // itemPet, phu thuoc Map.itemTemplates khong nap duoc ngoai server that).
        Kiem.dung("co ham dung chung thongSoKhangXuyenGiap(cap) giua thuc thi va hien thi",
                petJava.contains("private static int[] thongSoKhangXuyenGiap("));
        final int dauHamInfo = petJava.indexOf("public String getInfoSendUser()");
        Kiem.dung("tim thay than ham getInfoSendUser()", dauHamInfo >= 0);
        if (dauHamInfo >= 0) {
            final String thanInfo = petJava.substring(dauHamInfo);
            Kiem.dung("getInfoSendUser() goi thongSoKhangXuyenGiap() de lay so lieu hien thi",
                    thanInfo.contains("thongSoKhangXuyenGiap(this.khangXuyenGiapCap)"));
            Kiem.dung("dong hien thi co ghi ro giam bao nhieu % sat thuong",
                    thanInfo.contains("giảm ") && thanInfo.contains("thongSo[0] + \"% sát thương"));
            Kiem.dung("dong hien thi co ghi ro ti le kich hoat",
                    thanInfo.contains("thongSo[1]") && thanInfo.contains("% cơ hội kích hoạt"));
            Kiem.dung("dong hien thi co ghi ro hoi chieu",
                    thanInfo.contains("hồi chiêu ") && thanInfo.contains("thongSo[2] + \"s)\""));
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
