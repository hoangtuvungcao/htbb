import data.Animal;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hoa hinh Thu (linh thu cuoi) - nang cap 5, dung chung bang khang xuyen giap
 * voi Pet (xem KiemHoaHinhPet.java va README.md).
 *
 * Cap 1 (kich hoat dau tien) GIU NGUYEN het hanh vi cu (item 268, 70% ty le) -
 * khong test lai o day vi khong doi. Chi test PHAN MOI: capHoaHinh/
 * khangXuyenGiapCap, luu vao chuoi "att" co san (khong ALTER TABLE, giong
 * cach KHANG_DONG_BANG/... da tung duoc them truoc do), va applyKhangXuyenGiap.
 *
 * doHoaHinh() can Char that (listGemitem, sendMessage...) nen khong goi truc
 * tiep o day - phan do quet van ban nguon, giong cach KiemHoaHinhPet lam voi
 * RealController.
 */
public final class KiemHoaHinhThu {

    private KiemHoaHinhThu() {
    }

    public static void chay() {
        Kiem.nhom("Hoa hinh Thu - cap do, luu vao att, khang xuyen giap");

        // ===== 1) Gia tri mac dinh =====
        final Animal a0 = new Animal();
        Kiem.bang("Animal moi: capHoaHinh = 0", Byte.valueOf((byte) 0), Byte.valueOf(a0.capHoaHinh));
        Kiem.bang("Animal moi: khangXuyenGiapCap = 0", Byte.valueOf((byte) 0), Byte.valueOf(a0.khangXuyenGiapCap));
        Kiem.bang("Animal moi: applyKhangXuyenGiap khong doi sat thuong (chua hoa hinh)",
                Integer.valueOf(1000), Integer.valueOf(a0.applyKhangXuyenGiap(1000)));

        // ===== 2) initKhangXuyenGiapWhenHoaHinh() dong bo dung capHoaHinh =====
        final Animal aDb = new Animal();
        aDb.capHoaHinh = 4;
        aDb.initKhangXuyenGiapWhenHoaHinh();
        Kiem.bang("initKhangXuyenGiapWhenHoaHinh: khangXuyenGiapCap = capHoaHinh",
                Byte.valueOf((byte) 4), Byte.valueOf(aDb.khangXuyenGiapCap));

        // ===== 3) applyKhangXuyenGiap: chua bat isHoaHinh (het han/chua kich
        // hoat) thi du con capHoaHinh cung khong giam =====
        final Animal aChuaBat = new Animal();
        aChuaBat.capHoaHinh = 5;
        aChuaBat.khangXuyenGiapCap = 5;
        aChuaBat.isHoaHinh = 0; // chua tung kich hoat / da bi reset luc het han
        Kiem.bang("isHoaHinh=0 (du con capHoaHinh) -> applyKhangXuyenGiap khong doi sat thuong",
                Integer.valueOf(500), Integer.valueOf(aChuaBat.applyKhangXuyenGiap(500)));

        final Animal aHetHan = new Animal();
        aHetHan.capHoaHinh = 5;
        aHetHan.khangXuyenGiapCap = 5;
        aHetHan.isHoaHinh = 1;
        aHetHan.timeHoaHinh = System.currentTimeMillis() - 1000L; // da qua han
        Kiem.bang("isHoaHinh=1 nhung da qua timeHoaHinh -> applyKhangXuyenGiap khong doi sat thuong",
                Integer.valueOf(500), Integer.valueOf(aHetHan.applyKhangXuyenGiap(500)));

        // ===== 4) Bang giam theo cap (mau lon, giong cach kiem cua Pet) =====
        krThuGiam(1, 5);
        krThuGiam(2, 10);
        krThuGiam(3, 15);
        krThuGiam(4, 20);
        krThuGiam(5, 25);

        // ===== 5) Cooldown rieng: goi ngay sau proc phai bi chan =====
        boolean thayDoiCoDuocChanCooldown = false;
        for (int lan = 0; lan < 500 && !thayDoiCoDuocChanCooldown; lan++) {
            final Animal aCd = new Animal();
            aCd.isHoaHinh = 1;
            aCd.timeHoaHinh = System.currentTimeMillis() + 3600000L;
            aCd.capHoaHinh = 5;
            aCd.khangXuyenGiapCap = 5;
            final int lan1 = aCd.applyKhangXuyenGiap(1000);
            if (lan1 != 1000) {
                final int lan2 = aCd.applyKhangXuyenGiap(1000);
                Kiem.bang("cap 5: goi ngay sau khi proc bi chan boi cooldown (khong giam tiep)",
                        Integer.valueOf(1000), Integer.valueOf(lan2));
                thayDoiCoDuocChanCooldown = true;
            }
        }
        Kiem.dung("kiem duoc it nhat mot lan proc trong 500 lan thu (cap 5, ty le 30%) de kiem tra cooldown",
                thayDoiCoDuocChanCooldown);

        // ===== 6) initAtt()/getAttribute() roundtrip - dung chung cot "att" co
        // san, khong dung cot CSDL rieng (giong cach KHANG_DONG_BANG da lam) =====
        final Animal b1 = new Animal();
        b1.KHANG_DONG_BANG = 1;
        b1.capHoaHinh = 3;
        b1.khangXuyenGiapCap = 3;
        final String infoMoi = b1.getAttribute();
        final Animal b2 = new Animal();
        b2.initAtt(infoMoi);
        Kiem.bang("roundtrip initAtt/getAttribute: capHoaHinh", Byte.valueOf((byte) 3), Byte.valueOf(b2.capHoaHinh));
        Kiem.bang("roundtrip initAtt/getAttribute: khangXuyenGiapCap", Byte.valueOf((byte) 3), Byte.valueOf(b2.khangXuyenGiapCap));
        Kiem.bang("roundtrip: truong cu (KHANG_DONG_BANG) khong bi anh huong", Byte.valueOf((byte) 1), Byte.valueOf(b2.KHANG_DONG_BANG));

        // ===== 7) Tuong thich nguoc: chuoi CU (thieu 2 truong cuoi) van doc
        // duoc, mac dinh capHoaHinh/khangXuyenGiapCap = 0 =====
        final String[] phan = infoMoi.split(",");
        final StringBuilder infoCu = new StringBuilder();
        for (int i = 0; i < phan.length - 2; i++) { // bo 2 truong cuoi (capHoaHinh, khangXuyenGiapCap)
            if (i > 0) {
                infoCu.append(',');
            }
            infoCu.append(phan[i]);
        }
        // Animal moi (chua initAtt) da mac dinh capHoaHinh=0 tren khai bao field -
        // initAtt() voi chuoi thieu se KHONG cham toi field nay, y het luc mot
        // dong CSDL cu duoc nap lan dau (Animal moi tinh, roi initAtt tu DB).
        final Animal c = new Animal();
        c.initAtt(infoCu.toString());
        Kiem.bang("chuoi cu (thieu 2 truong): capHoaHinh mac dinh 0", Byte.valueOf((byte) 0), Byte.valueOf(c.capHoaHinh));
        Kiem.bang("chuoi cu (thieu 2 truong): khangXuyenGiapCap mac dinh 0", Byte.valueOf((byte) 0), Byte.valueOf(c.khangXuyenGiapCap));
        Kiem.bang("chuoi cu: truong cu (KHANG_DONG_BANG) van doc dung", Byte.valueOf((byte) 1), Byte.valueOf(c.KHANG_DONG_BANG));

        quetVanBanNguon();
    }

    private static void krThuGiam(final int cap, final int reducePercentMongDoi) {
        final int baseDame = 1000000;
        final int mongDoi = baseDame - baseDame * reducePercentMongDoi / 100;
        boolean thayDoi = false;
        for (int lan = 0; lan < 2000 && !thayDoi; lan++) {
            final Animal a = new Animal();
            a.isHoaHinh = 1;
            a.timeHoaHinh = System.currentTimeMillis() + 3600000L;
            a.capHoaHinh = (byte) cap;
            a.khangXuyenGiapCap = (byte) cap;
            final int ketQua = a.applyKhangXuyenGiap(baseDame);
            if (ketQua != baseDame) {
                Kiem.bang("cap " + cap + ": giam dung " + reducePercentMongDoi + "% khi proc",
                        Integer.valueOf(mongDoi), Integer.valueOf(ketQua));
                thayDoi = true;
            }
        }
        Kiem.dung("cap " + cap + ": thay duoc it nhat 1 lan proc trong 2000 lan thu (de kiem cong thuc giam)", thayDoi);
    }

    private static void quetVanBanNguon() {
        Kiem.nhom("Hoa hinh Thu - noi day UI (menu NPC, doHoaHinh, CharChiSo)");

        final String menuNpc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        Kiem.dung("doc duoc RequestNPCInfoHandler.java", !menuNpc.isEmpty());
        Kiem.dung("menu NPC -6 hien cap hoa hinh khi da bat dau",
                menuNpc.contains("p.animalRide.capHoaHinh > 0 ? \" (cấp \""));

        final String animal = doc("src/data/Animal.java");
        Kiem.dung("doc duoc Animal.java", !animal.isEmpty());
        Kiem.dung("doHoaHinh chan khi da toi da cap 5", animal.contains("p.animalRide.capHoaHinh >= 5"));
        Kiem.dung("cap 1 giu nguyen ty le 70%", animal.contains("Map.r.nextInt(100) < 70"));
        Kiem.dung("cap 2..5 khong con roi ngau nhien (nam ngoai nhanh capHoaHinh == 0)",
                animal.contains("p.animalRide.capHoaHinh = (byte) (p.animalRide.capHoaHinh + 1);"));
        Kiem.dung("van dung item 268 (khong doi vat pham)", animal.contains("int soluongDel = 30;"));

        final String charChiSo = doc("src/real/CharChiSo.java");
        Kiem.dung("doc duoc CharChiSo.java", !charChiSo.isEmpty());
        Kiem.dung("applyThanKhiOnHit co goi applyKhangXuyenGiap cho linh thu cua phe bi danh",
                charChiSo.contains("animalBiDanh.applyKhangXuyenGiap(finalDame)"));
        Kiem.dung("khong dong toi hieu ung Lua thuong co cu (applyHoaHinhDefenseReduction van con nguyen)",
                charChiSo.contains("actor.applyHoaHinhDefenseReduction();"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
