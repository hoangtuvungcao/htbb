import data.Pet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import real.Item;

/**
 * Hoa hinh Pet -> pet Lan (luat viet lai ngay 02/09/2026 theo mo ta cua nguoi yeu cau):
 *   pet nuoi VINH VIEN cap n + n x 10 binh hoa hinh (item 268) = pet Lan cap n, han 10 ngay,
 *   khong ton luong; het han tro ve con cu NGAY khi dang choi; khang xuyen giap theo bang
 *   5 cap co THOI GIAN DUY TRI; ten hien "Lan cap n - ...". Anh hien thi (khac voi bang khang,
 *   KHONG doi): cap 1-3 dung map/d_effect/67.png, cap 4 dung 68.png, cap 5 dung 69.png (anh Lan
 *   that, co san tu truoc) - xem Pet.idHieuUngHoaHinhTheoCap().
 *
 * Pet.java song trong package data va dung Map/Char that, nen: phan goi that duoc (bang
 * khang, applyKhangXuyenGiap co moc thoi gian, endHoaHinhPet, donHoaHinhHetHan, getName,
 * getDBInfo/initDbInfo) thi goi THAT; phan noi day UI (RealController case 12, menu NPC -8,
 * Char.checkExpirePet, Database.getCharPet, goi 26) thi quet van ban nguon.
 *
 * Phan ANH la cho da hong hai lan (31/08 va 01/09/2026) nen khoa ky nhat: id 67/68/69 deu phai
 * nam trong id_eff_pet_tool (id khong nam trong do -> client khong co bang khung -> pet khong
 * ve: J2ME Pet.paint "if (deff == null) return"), va tung file .pet phai dung cau truc bang pet.
 */
public final class KiemHoaHinhPet {

    private KiemHoaHinhPet() {
    }

    public static void chay() {
        Kiem.nhom("Hoa hinh Pet -> pet Lan: luat, bang khang xuyen giap, het han");

        kiemMacDinh();
        kiemBangKhang();
        kiemApDungTheoThoiGian();
        kiemHetHan();
        kiemLuuCSDL();
        kiemTen();
        kiemAnhTheoCap();
        quetVanBanNguon();
        kiemAnhVaBangKhung();
        kiemAnhVaBangKhungCapThap();
    }

    /**
     * Chon anh hoa hinh theo capHoaHinh (yeu cau 02/09/2026): cap 1,2,3 -> 67 (dung chung, chua
     * co anh rieng cho tung cap); cap 4 -> 68; cap 5 -> 69 (anh Lan that, co san tu truoc).
     */
    private static void kiemAnhTheoCap() {
        final int[][] capVaAnh = {{1, 67}, {2, 67}, {3, 67}, {4, 68}, {5, 69}};
        for (final int[] cv : capVaAnh) {
            final Pet p = new Pet(0);
            p.itemPet = new Item();
            p.idHoaHinhPet = 67;
            p.capHoaHinh = (byte) cv[0];
            p.timeEndHoaHinhPet = System.currentTimeMillis() + 60000L;
            Kiem.bang("dang hoa hinh, cap " + cv[0] + " -> getIdEffPet() = " + cv[1],
                    Integer.valueOf(cv[1]), Integer.valueOf(p.getIdEffPet()));
        }
    }

    private static void kiemMacDinh() {
        final Pet p0 = new Pet(0);
        Kiem.bang("Pet moi: idHoaHinhPet = -1", Short.valueOf((short) -1), Short.valueOf(p0.idHoaHinhPet));
        Kiem.bang("Pet moi: capHoaHinh = 0", Byte.valueOf((byte) 0), Byte.valueOf(p0.capHoaHinh));
        Kiem.bang("Pet moi: khangXuyenGiapCap = 0", Byte.valueOf((byte) 0), Byte.valueOf(p0.khangXuyenGiapCap));
        Kiem.sai("Pet moi: endHoaHinhPet() = false", p0.endHoaHinhPet());
        Kiem.sai("Pet moi: dangHoaHinh() = false", p0.dangHoaHinh());
        Kiem.sai("Pet moi: donHoaHinhHetHan() = false (khong co gi de don)", p0.donHoaHinhHetHan());
        Kiem.bang("Pet moi: capPet() = 0 (chua co itemPet)", Integer.valueOf(0), Integer.valueOf(p0.capPet()));
        Kiem.bang("Pet moi: applyKhangXuyenGiap khong doi sat thuong",
                Integer.valueOf(1000), Integer.valueOf(p0.applyKhangXuyenGiap(1000)));
        Kiem.bang("han hoa hinh = 10 ngay", Long.valueOf(10L * 24L * 3600L * 1000L), Long.valueOf(Pet.HAN_HOA_HINH_MS));
    }

    private static void kiemBangKhang() {
        final int[][] mongDoi = {
            {5, 10, 3, 100},
            {10, 10, 3, 100},
            {15, 20, 5, 80},
            {20, 20, 5, 80},
            {25, 30, 8, 60},
        };
        for (int cap = 1; cap <= 5; cap++) {
            final int[] ts = Pet.thongSoKhangXuyenGiap(cap);
            Kiem.bang("cap " + cap + ": giam " + mongDoi[cap - 1][0] + "%", Integer.valueOf(mongDoi[cap - 1][0]), Integer.valueOf(ts[0]));
            Kiem.bang("cap " + cap + ": ti le " + mongDoi[cap - 1][1] + "%", Integer.valueOf(mongDoi[cap - 1][1]), Integer.valueOf(ts[1]));
            Kiem.bang("cap " + cap + ": duy tri " + mongDoi[cap - 1][2] + "s", Integer.valueOf(mongDoi[cap - 1][2]), Integer.valueOf(ts[2]));
            Kiem.bang("cap " + cap + ": hoi chieu " + mongDoi[cap - 1][3] + "s", Integer.valueOf(mongDoi[cap - 1][3]), Integer.valueOf(ts[3]));
        }
        Kiem.bang("cap 0: khong co gi", "[0, 0, 0, 0]", java.util.Arrays.toString(Pet.thongSoKhangXuyenGiap(0)));
        Kiem.bang("cap 6 (vuot): tinh nhu cap 5", java.util.Arrays.toString(Pet.thongSoKhangXuyenGiap(5)),
                java.util.Arrays.toString(Pet.thongSoKhangXuyenGiap(6)));

        for (int cap = 1; cap <= 5; cap++) {
            Kiem.bang("pet cap " + cap + " can " + (cap * 10) + " binh", Integer.valueOf(cap * 10), Integer.valueOf(Pet.soBinhHoaHinhCan(cap)));
        }
        Kiem.bang("pet chua co cap -> -1 (khong hoa hinh duoc)", Integer.valueOf(-1), Integer.valueOf(Pet.soBinhHoaHinhCan(0)));
    }

    /**
     * Cua so duy tri va hoi chieu, do bang moc thoi gian truyen vao (khong phu thuoc dong ho).
     * Chi ti le kich hoat la ngau nhien -> lap toi khi kich hoat, roi kiem cac moc sau do.
     */
    private static void kiemApDungTheoThoiGian() {
        final int base = 1000000;
        for (int cap = 1; cap <= 5; cap++) {
            final int[] ts = Pet.thongSoKhangXuyenGiap(cap);
            final int giam = base - base * ts[0] / 100;
            final Pet p = new Pet(0);
            p.idHoaHinhPet = 67;
            p.capHoaHinh = (byte) cap;
            p.initKhangXuyenGiapWhenHoaHinh();
            p.timeEndHoaHinhPet = System.currentTimeMillis() + 3600000L;
            final long t0 = 1000000000L;
            boolean kichHoat = false;
            for (int lan = 0; lan < 5000 && !kichHoat; lan++) {
                kichHoat = p.applyKhangXuyenGiap(base, t0) == giam;
            }
            Kiem.dung("cap " + cap + ": kich hoat duoc trong 5000 lan thu (ti le " + ts[1] + "%)", kichHoat);
            if (!kichHoat) {
                continue;
            }
            Kiem.bang("cap " + cap + ": ngay sau kich hoat, don ke tiep VAN giam (trong cua so duy tri)",
                    Integer.valueOf(giam), Integer.valueOf(p.applyKhangXuyenGiap(base, t0 + 1)));
            Kiem.bang("cap " + cap + ": 1ms truoc khi het duy tri van giam",
                    Integer.valueOf(giam), Integer.valueOf(p.applyKhangXuyenGiap(base, t0 + ts[2] * 1000L - 1)));
            Kiem.bang("cap " + cap + ": het duy tri -> dang hoi chieu, khong giam",
                    Integer.valueOf(base), Integer.valueOf(p.applyKhangXuyenGiap(base, t0 + ts[2] * 1000L)));
            Kiem.bang("cap " + cap + ": 1ms truoc khi het hoi chieu van khong giam",
                    Integer.valueOf(base), Integer.valueOf(p.applyKhangXuyenGiap(base, t0 + ts[3] * 1000L - 1)));
            Kiem.dung("cap " + cap + ": duy tri ngan hon hoi chieu (khong the vua duy tri vua hoi chieu xong)", ts[2] < ts[3]);
        }
        // initKhangXuyenGiapWhenHoaHinh dong bo dung cap
        final Pet pDb = new Pet(0);
        pDb.capHoaHinh = 4;
        pDb.initKhangXuyenGiapWhenHoaHinh();
        Kiem.bang("initKhangXuyenGiapWhenHoaHinh: khangXuyenGiapCap = capHoaHinh",
                Byte.valueOf((byte) 4), Byte.valueOf(pDb.khangXuyenGiapCap));
    }

    private static void kiemHetHan() {
        final Pet pHan = new Pet(0);
        pHan.idHoaHinhPet = 67;
        pHan.capHoaHinh = 5;
        pHan.initKhangXuyenGiapWhenHoaHinh();
        pHan.timeEndHoaHinhPet = System.currentTimeMillis() + 60000L;
        Kiem.sai("con han -> endHoaHinhPet() = false", pHan.endHoaHinhPet());
        Kiem.dung("con han -> dangHoaHinh() = true", pHan.dangHoaHinh());
        Kiem.sai("con han -> donHoaHinhHetHan() = false, khong don", pHan.donHoaHinhHetHan());
        Kiem.bang("con han -> capHoaHinh giu nguyen", Byte.valueOf((byte) 5), Byte.valueOf(pHan.capHoaHinh));

        pHan.timeEndHoaHinhPet = System.currentTimeMillis() - 60000L;
        Kiem.dung("het han -> endHoaHinhPet() = true", pHan.endHoaHinhPet());
        Kiem.sai("het han -> dangHoaHinh() = false", pHan.dangHoaHinh());
        Kiem.bang("het han (con cap 5) -> applyKhangXuyenGiap khong doi sat thuong",
                Integer.valueOf(500), Integer.valueOf(pHan.applyKhangXuyenGiap(500)));
        Kiem.dung("het han -> donHoaHinhHetHan() = true", pHan.donHoaHinhHetHan());
        Kiem.bang("sau khi don: idHoaHinhPet = -1", Short.valueOf((short) -1), Short.valueOf(pHan.idHoaHinhPet));
        Kiem.bang("sau khi don: capHoaHinh = 0", Byte.valueOf((byte) 0), Byte.valueOf(pHan.capHoaHinh));
        Kiem.bang("sau khi don: khangXuyenGiapCap = 0", Byte.valueOf((byte) 0), Byte.valueOf(pHan.khangXuyenGiapCap));
        Kiem.bang("sau khi don: timeEndHoaHinhPet = 0", Long.valueOf(0L), Long.valueOf(pHan.timeEndHoaHinhPet));

        final Pet pChuaHH = new Pet(0);
        pChuaHH.timeEndHoaHinhPet = System.currentTimeMillis() - 60000L;
        Kiem.sai("chua hoa hinh (idHoaHinhPet = -1) -> endHoaHinhPet() = false du moc da qua", pChuaHH.endHoaHinhPet());
    }

    private static void kiemLuuCSDL() {
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
        Kiem.bang("roundtrip: idHoaHinhPet", Short.valueOf((short) 67), Short.valueOf(b.idHoaHinhPet));
        Kiem.bang("roundtrip: timeEndHoaHinhPet", Long.valueOf(9999999999L), Long.valueOf(b.timeEndHoaHinhPet));
        Kiem.bang("roundtrip: khangXuyenGiapCap", Byte.valueOf((byte) 3), Byte.valueOf(b.khangXuyenGiapCap));
        Kiem.bang("roundtrip: capHoaHinh", Byte.valueOf((byte) 3), Byte.valueOf(b.capHoaHinh));
        Kiem.bang("roundtrip: truong cu (powerEat) khong bi anh huong", Short.valueOf((short) 3), Short.valueOf(b.powerEat));

        final String infoCu = "0,111,0,1,222,3,0,1,2,3,4,0,2"; // 13 truong nhu truoc khi co hoa hinh
        final Pet c = new Pet("ten cu", (byte) 0, 1, infoCu, null, "2020-01-01 00:00:00.0");
        Kiem.bang("chuoi cu: idHoaHinhPet mac dinh -1", Short.valueOf((short) -1), Short.valueOf(c.idHoaHinhPet));
        Kiem.bang("chuoi cu: capHoaHinh mac dinh 0", Byte.valueOf((byte) 0), Byte.valueOf(c.capHoaHinh));
        Kiem.bang("chuoi cu: powerEat van dung", Short.valueOf((short) 3), Short.valueOf(c.powerEat));
    }

    private static void kiemTen() {
        final Pet p = new Pet(0);
        p.idHoaHinhPet = 67;
        p.capHoaHinh = 3;
        p.timeEndHoaHinhPet = System.currentTimeMillis() + 60000L;
        Kiem.dung("dang hoa hinh: ten bat dau bang \"Lân cấp 3 - \"", p.getName().startsWith("Lân cấp 3 - "));
        p.timeEndHoaHinhPet = System.currentTimeMillis() - 60000L;
        Kiem.sai("het han: ten khong con tien to Lan", p.getName().startsWith("Lân cấp"));
        final Pet pThuong = new Pet(0);
        Kiem.sai("chua hoa hinh: ten khong co tien to Lan", pThuong.getName().startsWith("Lân cấp"));
        Kiem.dung("getIdEffPet() = -1 khi chua co itemPet", pThuong.getIdEffPet() == -1);
    }

    private static void quetVanBanNguon() {
        Kiem.nhom("Hoa hinh Pet - noi day UI (RealController, menu NPC, Char, Database, goi 26)");

        final String pet = doc("src/data/Pet.java");
        Kiem.dung("doc duoc Pet.java", !pet.isEmpty());
        Kiem.dung("hang so ID_HIEU_UNG_HOA_HINH_PET = 69 (dung anh 69.png)", pet.contains("ID_HIEU_UNG_HOA_HINH_PET = 69;"));
        final String thanEff = doan(pet, "public int getIdEffPet()", "\n    }");
        Kiem.dung("getIdEffPet(): dang hoa hinh -> chon anh theo cap",
                thanEff.contains("idHoaHinhPet != -1") && thanEff.contains("return idHieuUngHoaHinhTheoCap();"));
        final String thanInfo = doan(pet, "public String getInfoSendUser()", "public boolean canLearnSkill()");
        Kiem.dung("getInfoSendUser() in du 4 so: giam %, ti le, duy tri, hoi chieu",
                thanInfo.contains("thongSo[0]") && thanInfo.contains("thongSo[1]")
                && thanInfo.contains("thongSo[2]") && thanInfo.contains("thongSo[3]"));
        Kiem.dung("getInfoSendUser() goi ten Pet Lan cap n", thanInfo.contains("Pet Lân cấp"));

        final String rc = doc("src/real/RealController.java");
        Kiem.dung("doc duoc RealController.java", !rc.isEmpty());
        final String case12 = doan(rc, "case 12:", "case 13:");
        Kiem.dung("co case 12 (Hoa hinh Pet)", !case12.isEmpty());
        Kiem.dung("case 12: chi pet vinh vien (getTimeExpire() != 32000)", case12.contains("getTimeExpire() != 32000"));
        Kiem.dung("case 12: doi pet DA TIEN HOA (level < 1 thi chan) - hinh Lan chi ve cho TYPE_PET_TOOL",
                case12.contains("itemPet.level < 1"));
        Kiem.sai("case 12: KHONG con doi sieu tien hoa (level < 2)", case12.contains("itemPet.level < 2"));
        Kiem.sai("case 12: KHONG con chan 'toi da cap 5' (hoa hinh lai luc nao cung duoc)", case12.contains("capHoaHinh >= 5"));
        Kiem.dung("case 12: cap Lan = capPet() cua pet", case12.contains("player.petUsing.capPet()"));
        Kiem.dung("case 12: so binh = soBinhHoaHinhCan(cap) (n x 10)", case12.contains("soBinhHoaHinhCan(capPetHoaHinh)"));
        Kiem.dung("case 12: dung item 268", case12.contains("player.listGemitem[268]") && case12.contains("player.delGem(268,"));
        Kiem.sai("case 12: KHONG ton luong", case12.contains("subLuong(") || case12.contains("getLuong()") || case12.contains("Cần 5000 lượng"));
        Kiem.dung("case 12: han lay tu Pet.HAN_HOA_HINH_MS", case12.contains("HAN_HOA_HINH_MS"));
        Kiem.sai("case 12: khong con so 864_000_000L viet tay", case12.contains("864_000_000L"));
        Kiem.dung("case 12: gan idHoaHinhPet = 67 (co bao da hoa hinh)", case12.contains("idHoaHinhPet = 67"));
        Kiem.dung("case 12: luu CSDL", case12.contains("Database.instance.savePet(player.petUsing);"));
        Kiem.dung("case 12: gui lai hinh cho NGUOI XUNG QUANH",
                case12.contains("sendToNearPlayer(MessageCreator.createCharWearingMessage(player, player))"));
        Kiem.dung("case 12: thong bao co du 4 so cua bang khang",
                case12.contains("tsKxg[0]") && case12.contains("tsKxg[1]") && case12.contains("tsKxg[2]") && case12.contains("tsKxg[3]"));
        Kiem.dung("case 12: ghi log hoahinhpet kem cap va so binh",
                case12.contains("\"hoahinhpet\"") && case12.contains("soBinhHoaHinh"));

        final String menuNpc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        Kiem.dung("menu NPC -8 co muc \"Hoá Hình Pet\"", menuNpc.contains("\"Hoá Hình Pet\""));
        Kiem.dung("menu NPC -8 hien 'Lan cap n' khi dang hoa hinh (dangHoaHinh)",
                menuNpc.contains("dangHoaHinh()") && menuNpc.contains("Lân cấp"));

        final String charJava = doc("src/real/Char.java");
        final String thanExpire = doan(charJava, "public void checkExpirePet()", "public void checkExpireMDAmor()");
        Kiem.dung("Char.checkExpirePet: don hoa hinh het han REAL-TIME (donHoaHinhHetHan)", thanExpire.contains("donHoaHinhHetHan()"));
        Kiem.dung("Char.checkExpirePet: het han thi gui lai hinh cho minh va nguoi xung quanh",
                thanExpire.contains("sendPetEff()") && thanExpire.contains("sendToNearPlayer(MessageCreator.createCharWearingMessage(this, this))"));
        Kiem.dung("Char.checkExpirePet: co bao cho nguoi choi", thanExpire.contains("đã hết hạn"));
        Kiem.dung("Char.checkExpirePet: pet trong tui cung duoc don va luu", thanExpire.contains("petTui.donHoaHinhHetHan()"));

        final String database = doc("src/data/Database.java");
        Kiem.dung("Database.getCharPet don bang donHoaHinhHetHan() (mot nguon)", database.contains("pet.donHoaHinhHetHan();"));
        Kiem.sai("Database khong con tu tay dat pet.capHoaHinh = 0", database.contains("pet.capHoaHinh = 0;"));

        final String charChiSo = doc("src/real/CharChiSo.java");
        Kiem.dung("applyThanKhiOnHit goi applyKhangXuyenGiap cho phe bi danh",
                charChiSo.contains("petBiDanh.applyKhangXuyenGiap(finalDame)"));

        Kiem.sai("'hoahinhpet' duoc GIU trong LocLogVatPham", data.LocLogVatPham.boQua("hoahinhpet"));
    }

    /** Anh 69.png giu nguyen; bang khung rieng 69.pet dung cau truc pet; 69 co trong id_eff_pet_tool. */
    private static void kiemAnhVaBangKhung() {
        Kiem.nhom("Hoa hinh Pet - anh 69.png va bang khung rieng 69.pet");

        try {
            final java.awt.image.BufferedImage anh = javax.imageio.ImageIO.read(new java.io.File("map/d_effect/69.png"));
            Kiem.dung("map/d_effect/69.png doc duoc", anh != null);
            if (anh != null) {
                Kiem.dung("69.png van la luoi 256x256 (khong bi cat/resize)", anh.getWidth() == 256 && anh.getHeight() == 256);
            }
        } catch (final IOException e) {
            Kiem.dung("doc duoc map/d_effect/69.png (" + e + ")", false);
        }

        final byte[] b69 = docByte("map/d_effect/69");
        final byte[] b49 = docByte("map/d_effect/49");
        final byte[] bPet = docByte("map/d_effect/69.pet");
        Kiem.dung("bang khung 69 (danh hieu) van con va van 13 o", b69.length > 0 && (b69[0] & 0xFF) == 13);
        Kiem.dung("bang khung pet mau 49 con", b49.length > 42 && (b49[0] & 0xFF) == 3);
        Kiem.dung("co bang khung rieng map/d_effect/69.pet", bPet.length > 0);
        if (b69.length == 0 || b49.length <= 42 || bPet.length == 0) {
            return;
        }
        Kiem.bang("69.pet: 3 o nho", Integer.valueOf(3), Integer.valueOf(bPet[0] & 0xFF));
        final int[] oDung = {10, 11, 12};
        for (int i = 0; i < 3; i++) {
            final int goc = 1 + oDung[i] * 5;
            final int moi = 1 + i * 5;
            Kiem.bang("69.pet o " + i + " = o " + oDung[i] + " cua bang 69 (x,y,w,h)",
                    (b69[goc + 1] & 0xFF) + "," + (b69[goc + 2] & 0xFF) + "," + (b69[goc + 3] & 0xFF) + "," + (b69[goc + 4] & 0xFF),
                    (bPet[moi + 1] & 0xFF) + "," + (bPet[moi + 2] & 0xFF) + "," + (bPet[moi + 3] & 0xFF) + "," + (bPet[moi + 4] & 0xFF));
            Kiem.dung("69.pet o " + i + " nam trong anh 256x256",
                    (bPet[moi + 1] & 0xFF) + (bPet[moi + 3] & 0xFF) <= 256 && (bPet[moi + 2] & 0xFF) + (bPet[moi + 4] & 0xFF) <= 256);
        }
        Kiem.bang("69.pet: 3 khung", Integer.valueOf(3), Integer.valueOf(docShort(bPet, 16)));
        for (int i = 0; i < 3; i++) {
            final int k = 18 + i * 8;
            final int w = bPet[1 + i * 5 + 3] & 0xFF;
            final int h = bPet[1 + i * 5 + 4] & 0xFF;
            Kiem.bang("69.pet khung " + i + ": 1 part", Integer.valueOf(1), Integer.valueOf(bPet[k] & 0xFF));
            Kiem.bang("69.pet khung " + i + ": dx = -w/2 (can giua)", Integer.valueOf(-(w / 2)), Integer.valueOf(docShort(bPet, k + 1)));
            Kiem.bang("69.pet khung " + i + ": dy = -h (chan cham dat, khong bay)", Integer.valueOf(-h), Integer.valueOf(docShort(bPet, k + 3)));
            Kiem.bang("69.pet khung " + i + ": tro o nho " + i, Integer.valueOf(i), Integer.valueOf(bPet[k + 5] & 0xFF));
        }
        boolean duoiKhop = bPet.length == b49.length;
        for (int i = 42; duoiKhop && i < b49.length; i++) {
            duoiKhop = bPet[i] == b49[i];
        }
        Kiem.dung("69.pet: phan sequence/anim/bong trung tung byte voi bang pet 49", duoiKhop);

        final String mc = doc("src/real/MessageCreator.java");
        final String dongMang = doan(mc, "id_eff_pet_tool = new int[]{", "};");
        Kiem.dung("MessageCreator: id_eff_pet_tool co 67, 68, 69 (khong co thi client khong co bang khung -> pet khong ve)",
                !dongMang.isEmpty() && chuaSo(dongMang, 67) && chuaSo(dongMang, 68) && chuaSo(dongMang, 69));
        final String khoiPet = doan(mc, "id_eff_pet_tool.length; ++i", "id_eff_tool_ngu_long");
        Kiem.dung("goi 26 khoi pet lay bang khung qua DataGame.moTaPet()", khoiPet.contains("DataGame.moTaPet(idEff)"));
        Kiem.sai("goi 26 khoi pet khong con doc thang dataDynamicEffect[idEff]", khoiPet.contains("dataDynamicEffect[idEff]"));

        final String dg = doc("src/data/DataGame.java");
        Kiem.dung("DataGame nap map/d_effect/<id>.pet vao dataPetEffect", dg.contains("dataPetEffect[j] = Files.readAllBytes(filePet.toPath());"));
        final String thanMoTa = doan(dg, "public static byte[] moTaPet(", "\n    }");
        Kiem.dung("DataGame.moTaPet uu tien .pet roi moi toi bang chung",
                thanMoTa.indexOf("dataPetEffect[idEff]") >= 0
                && thanMoTa.indexOf("dataPetEffect[idEff]") < thanMoTa.indexOf("return dataDynamicEffect[idEff]"));
    }

    /**
     * Anh 67.png/68.png giu nguyen (hieu ung tia lua cap thap - xem ghi chu getIdEffPet() vi sao
     * chua phai anh Lan di that); bang khung rieng 67.pet/68.pet dung cau truc pet, sinh boi
     * tools/TaoPetLanCapThap.java tu 3 o dau (0,1,2) cua moi bang.
     */
    private static void kiemAnhVaBangKhungCapThap() {
        Kiem.nhom("Hoa hinh Pet - anh 67.png/68.png va bang khung rieng 67.pet/68.pet (cap thap)");

        final byte[] b49 = docByte("map/d_effect/49");
        Kiem.dung("bang khung pet mau 49 con", b49.length > 42 && (b49[0] & 0xFF) == 3);

        for (final int id : new int[]{67, 68}) {
            try {
                final java.awt.image.BufferedImage anh = javax.imageio.ImageIO.read(new java.io.File("map/d_effect/" + id + ".png"));
                Kiem.dung("map/d_effect/" + id + ".png doc duoc", anh != null);
                if (anh != null) {
                    Kiem.dung(id + ".png van la luoi 256x256 (khong bi cat/resize)", anh.getWidth() == 256 && anh.getHeight() == 256);
                }
            } catch (final IOException e) {
                Kiem.dung("doc duoc map/d_effect/" + id + ".png (" + e + ")", false);
            }

            final byte[] bNguon = docByte("map/d_effect/" + id);
            final byte[] bPet = docByte("map/d_effect/" + id + ".pet");
            Kiem.dung("co bang khung goc map/d_effect/" + id, bNguon.length > 0);
            Kiem.dung("co bang khung rieng map/d_effect/" + id + ".pet", bPet.length > 0);
            if (bNguon.length == 0 || b49.length <= 42 || bPet.length == 0) {
                continue;
            }
            Kiem.bang(id + ".pet: 3 o nho", Integer.valueOf(3), Integer.valueOf(bPet[0] & 0xFF));
            for (int i = 0; i < 3; i++) {
                final int goc = 1 + i * 5;
                Kiem.bang(id + ".pet o " + i + " = o " + i + " cua bang " + id + " (x,y,w,h)",
                        (bNguon[goc + 1] & 0xFF) + "," + (bNguon[goc + 2] & 0xFF) + "," + (bNguon[goc + 3] & 0xFF) + "," + (bNguon[goc + 4] & 0xFF),
                        (bPet[goc + 1] & 0xFF) + "," + (bPet[goc + 2] & 0xFF) + "," + (bPet[goc + 3] & 0xFF) + "," + (bPet[goc + 4] & 0xFF));
                Kiem.dung(id + ".pet o " + i + " nam trong anh 256x256",
                        (bPet[goc + 1] & 0xFF) + (bPet[goc + 3] & 0xFF) <= 256 && (bPet[goc + 2] & 0xFF) + (bPet[goc + 4] & 0xFF) <= 256);
            }
            Kiem.bang(id + ".pet: 3 khung", Integer.valueOf(3), Integer.valueOf(docShort(bPet, 16)));
            for (int i = 0; i < 3; i++) {
                final int k = 18 + i * 8;
                final int w = bPet[1 + i * 5 + 3] & 0xFF;
                final int h = bPet[1 + i * 5 + 4] & 0xFF;
                Kiem.bang(id + ".pet khung " + i + ": 1 part", Integer.valueOf(1), Integer.valueOf(bPet[k] & 0xFF));
                Kiem.bang(id + ".pet khung " + i + ": dx = -w/2 (can giua)", Integer.valueOf(-(w / 2)), Integer.valueOf(docShort(bPet, k + 1)));
                Kiem.bang(id + ".pet khung " + i + ": dy = -h (chan cham dat, khong bay)", Integer.valueOf(-h), Integer.valueOf(docShort(bPet, k + 3)));
                Kiem.bang(id + ".pet khung " + i + ": tro o nho " + i, Integer.valueOf(i), Integer.valueOf(bPet[k + 5] & 0xFF));
            }
            boolean duoiKhop = bPet.length == b49.length;
            for (int i = 42; duoiKhop && i < b49.length; i++) {
                duoiKhop = bPet[i] == b49[i];
            }
            Kiem.dung(id + ".pet: phan sequence/anim/bong trung tung byte voi bang pet 49", duoiKhop);
        }
    }

    private static boolean chuaSo(final String mang, final int so) {
        final int dau = mang.indexOf('{');
        final String[] phan = mang.substring(dau + 1).split(",");
        for (final String p : phan) {
            final String t = p.trim();
            if (!t.isEmpty() && t.chars().allMatch(Character::isDigit) && Integer.parseInt(t) == so) {
                return true;
            }
        }
        return false;
    }

    private static int docShort(final byte[] b, final int i) {
        return (short) (((b[i] & 0xFF) << 8) | (b[i + 1] & 0xFF));
    }

    private static String doan(final String ma, final String tu, final String den) {
        final int bd = ma.indexOf(tu);
        if (bd < 0) {
            return "";
        }
        final int kt = ma.indexOf(den, bd + tu.length());
        return kt < 0 ? ma.substring(bd) : ma.substring(bd, kt);
    }

    private static byte[] docByte(final String duong) {
        try {
            return Files.readAllBytes(Paths.get(duong));
        } catch (final IOException e) {
            return new byte[0];
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
