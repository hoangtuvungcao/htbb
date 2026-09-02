import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * NPC "hoa tieu" mat tich tren ban do nhieu khu (region) nhu Loc tri (su co 02/09/2026).
 *
 * NGUYEN NHAN. MapKhoiTao dat CHUNG mot doi tuong Char (tu Map.npcHoatieu) vao MOI khu cua ban
 * do bang Map.addPlayer(country, bot, i) voi i chay 0..nregion-1. Duong nay goi thang
 * Region.addPlayer(int,Char) - KHAC Region.playerJoin(Char) (duong nguoi choi that dung, co gan
 * player.region = idRegion) - nen KHONG BAO GIO gan bot.region. Truong do vi vay dung mai o gia
 * tri mac dinh 0 (Actor.region), trong khi CharCapNhat.updateNearChar loc nghiem ngat
 * "p1.region != self.region" khong co ngoai le cho bot => bot chi hien voi nguoi choi dang o
 * DUNG khu 0 (khu chinh), mat han voi nguoi choi o cac khu Dong/Tay/Nam/Bac. Da xac minh bang
 * workflow doc lap (2 agent, doc thang source + doi chieu lich su git) truoc khi sua: khong
 * commit nao trong ngay 02/09/2026 lien quan, day la loi kien truc co san tu truoc.
 *
 * SUA. Mot doi tuong Char DUY NHAT khong the mang nhieu gia tri region cung luc (no duoc dat
 * vao ca 5 khu), nen KHONG the sua bang cach gan dung region - phai bo qua han phep so region
 * cho nhung bot loai nay. Them co Char.hienThiMoiRegion (mac dinh false, khong anh huong nguoi
 * choi/quai/bot khac); updateNearChar them dieu kien "&& !p1.hienThiMoiRegion" vao nhanh loc
 * region; MapNapTaiNguyen dat co nay = true cho tat ca bot "hoa tieu"/"doi tieu" TRUOC moi lan
 * Map.npcHoatieu.add(bot).
 */
public final class KiemNpcHoaTieuNhieuKhu {

    private KiemNpcHoaTieuNhieuKhu() {
    }

    public static void chay() {
        Kiem.nhom("NPC hoa tieu tren ban do nhieu khu (region) - bo qua loc region");

        final String charJava = doc("src/real/Char.java");
        Kiem.dung("doc duoc Char.java", !charJava.isEmpty());
        Kiem.dung("Char co truong hienThiMoiRegion", charJava.contains("public boolean hienThiMoiRegion;"));

        final String capNhat = doc("src/real/CharCapNhat.java");
        Kiem.dung("doc duoc CharCapNhat.java", !capNhat.isEmpty());
        Kiem.dung("updateNearChar: phep loc region co ngoai le !p1.hienThiMoiRegion",
                capNhat.contains("p1.region != self.region && !p1.hienThiMoiRegion"));

        final String napTaiNguyen = doc("src/real/MapNapTaiNguyen.java");
        Kiem.dung("doc duoc MapNapTaiNguyen.java", !napTaiNguyen.isEmpty());
        final int soLanAdd = demSoLan(napTaiNguyen, "Map.npcHoatieu.add(bot);");
        final int soLanDatCo = demSoLan(napTaiNguyen, "bot.hienThiMoiRegion = true;");
        Kiem.dung("co it nhat mot bot hoa tieu duoc tao (Map.npcHoatieu.add(bot))", soLanAdd > 0);
        Kiem.bang("moi lan Map.npcHoatieu.add(bot) deu co dat bot.hienThiMoiRegion = true truoc do",
                Integer.valueOf(soLanAdd), Integer.valueOf(soLanDatCo));

        // Doi chieu tung cap: dong "bot.hienThiMoiRegion = true;" phai dung NGAY TRUOC dong
        // "Map.npcHoatieu.add(bot);" tuong ung (khong lech cap do nham gan co cho bot khac).
        int viTri = 0;
        boolean thuTuDung = true;
        int capDoiChieu = 0;
        while (true) {
            final int viTriAdd = napTaiNguyen.indexOf("Map.npcHoatieu.add(bot);", viTri);
            if (viTriAdd < 0) {
                break;
            }
            final int viTriCo = napTaiNguyen.lastIndexOf("bot.hienThiMoiRegion = true;", viTriAdd);
            final int viTriIdBot = napTaiNguyen.lastIndexOf("bot.id = RealController.intance.idGen.getID(0, \"Tao bot\");", viTriAdd);
            if (viTriCo < 0 || viTriCo < viTriIdBot) {
                thuTuDung = false;
            }
            capDoiChieu++;
            viTri = viTriAdd + 1;
        }
        Kiem.dung("doi chieu duoc it nhat mot cap add/dat co", capDoiChieu > 0);
        Kiem.dung("moi dong dat co nam DUNG giua bot.id va Map.npcHoatieu.add(bot) tuong ung (khong lech bot)", thuTuDung);
    }

    private static int demSoLan(final String vanBan, final String mau) {
        int dem = 0;
        int viTri = 0;
        while (true) {
            viTri = vanBan.indexOf(mau, viTri);
            if (viTri < 0) {
                break;
            }
            dem++;
            viTri += mau.length();
        }
        return dem;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
