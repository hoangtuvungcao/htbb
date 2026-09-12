import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Nguoi choi duoc duyet vao MOT BANG DA CO SAN (khong phai tu tao bang) phai duoc dong bo
 * myCountry theo bang NGAY LUC DO, khong doi den lan dang nhap ke tiep.
 *
 * BAO CAO (12/09/2026): dang trong luc bang minh chiem thanh, buoc vao thanh thi bi kick ra
 * ngay du dang o trong bang. Goc: Char.kickOutTown() so sanh THANG `this.myCountry !=
 * this.inCountry` (khong doc idClan/NewClan) de quyet dinh co kick khoi ban do Thanh (Map.
 * idMapTown) hay khong luc dang mo cua chiem thanh (Map.getTown[country]). Duong DUY NHAT
 * cap nhat myCountry theo bang la Char.setCountry(), nhung truoc day chi duoc goi luc dang
 * nhap (CharNapDuLieu) - nhanh "duoc duyet vao bang" trong MapBangHoi.java gan idClan roi
 * KHONG goi setCountry(), nen nguoi vua vao bang van giu myCountry CU (-1 neu chua tung chon
 * lang, hoac lang cua bang truoc do) cho toi khi dang xuat/dang nhap lai - trong luc do ho
 * buoc vao thanh dung luc bang minh dang chiem la bi kick ngay, nhin tu ngoai giong nhu
 * "khong nam trong bang hoi thi bi kick" du idClan da dung.
 *
 * (Nhanh TAO bang moi khong dinh loi nay: `clan.country = player.myCountry;` gan NGUOC lai -
 * lang cua bang moi lay tu chinh nguoi tao - nen luon khop san, khong can goi setCountry().)
 *
 * Sua: goi `player.setCountry();` ngay sau `player.idClan = clanx.id;` trong nhanh duyet vao
 * bang (truoc `clanx.addMember(player)`), tai dung logic da dung o dang nhap thay vi viet lai.
 */
public final class KiemDongBoMyCountryKhiVaoBang {

    private KiemDongBoMyCountryKhiVaoBang() {
    }

    public static void chay() {
        Kiem.nhom("Vao bang co san phai dong bo myCountry ngay (tranh bi kick khoi thanh)");

        final String noiDung = doc("src/real/MapBangHoi.java");
        Kiem.dung("doc duoc src/real/MapBangHoi.java", !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        // Neo vao dung nhanh "duoc duyet" (ok == true) cua loi moi vao bang: gan idClan roi
        // them vao danh sach thanh vien. Chi lay LAN DAU (nhanh duyet moi thu that, khong
        // phai nhanh tao bang - xem duoi).
        final String GAN_IDCLAN = "player.idClan = clanx.id;";
        final int viTriGanIdClan = noiDung.indexOf(GAN_IDCLAN);
        Kiem.dung("tim thay nhanh duyet vao bang co san (player.idClan = clanx.id;)", viTriGanIdClan >= 0);
        if (viTriGanIdClan < 0) {
            return;
        }

        final int viTriAddMember = noiDung.indexOf("clanx.addMember(player);", viTriGanIdClan);
        Kiem.dung("tim thay clanx.addMember(player) ngay sau do", viTriAddMember > viTriGanIdClan);

        final int viTriSetCountry = noiDung.indexOf("player.setCountry();", viTriGanIdClan);
        Kiem.dung("nhanh duyet vao bang co goi player.setCountry() (dong bo lang theo bang)",
                viTriSetCountry >= 0);
        if (viTriSetCountry >= 0 && viTriAddMember > viTriGanIdClan) {
            Kiem.dung("setCountry() duoc goi SAU khi idClan da tro toi bang moi (setCountry() doc idClan)",
                    viTriSetCountry > viTriGanIdClan);
            Kiem.dung("setCountry() duoc goi TRUOC/luc them vao danh sach thanh vien (khong de cho ho lo lung)",
                    viTriSetCountry < viTriAddMember);
        }

        // Nhanh TAO bang moi (doCreateNewClan) khong can/khong nen bi bat goi setCountry() -
        // no da tu khop vi clan.country lay tu chinh player.myCountry. Chi kiem quan he do
        // van dung nguyen (khong ai "sua giup" doi chieu nguoc lai ma khong hoi).
        final int viTriTaoBang = noiDung.indexOf("clan.country = player.myCountry;");
        Kiem.dung("nhanh tao bang moi van lay lang bang tu chinh nguoi tao (khong dao nguoc)",
                viTriTaoBang >= 0);

        // Kiem lai dung goc gay bao cao: kickOutTown so sanh myCountry (khong phai idClan)
        // voi inCountry trong ban do Thanh luc dang mo chiem thanh - de lan sau co ai sua
        // nham logic nay thi test do ngay, khong phai doi bao cao moi.
        final String charJava = doc("src/real/Char.java");
        final int viTriKick = charJava.indexOf("public boolean kickOutTown()");
        Kiem.dung("tim thay Char.kickOutTown()", viTriKick >= 0);
        if (viTriKick >= 0) {
            final String thanKick = charJava.substring(viTriKick, Math.min(charJava.length(), viTriKick + 700));
            Kiem.dung("kickOutTown() van so sanh myCountry voi inCountry (khong doc idClan)",
                    thanKick.contains("this.myCountry != this.inCountry") && !thanKick.contains("idClan"));
        }

        final int viTriSetCountryDef = charJava.indexOf("public void setCountry()");
        Kiem.dung("tim thay dinh nghia Char.setCountry()", viTriSetCountryDef >= 0);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
