import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Loi tim thay 14/09/2026 khi ra soat "dao nguyen lieu" o khu mo (mapId=17,
 * lop MaterialMap - truoc day CHI ton tai duoi dang bytecode trong
 * lib/htbb-core.jar, khong co file .java, chua tung duoc bao tri).
 *
 * addGemItem/getGem/removeGem/deletePotionAndItemOnGround ban cu LUON dung
 * `this.gemItem.get(0)`, bo qua tham so `country` truyen vao - trong khi
 * Map.chiemGem() (khong bi MaterialMap ghi de) lai dung DUNG `player.inCountry`
 * that de "chiem" vat truoc khi cong vao tui. Quang danh xong luon roi vao o
 * quoc gia 0; nguoi choi phe 1/2 (2 trong 3 phe) thi chiemGem tim o 1/2,
 * KHONG thay vat vua tao o o 0 -> im lang khong cong gi, dung khop trieu
 * chung "danh roi nguyen lieu nhung khong tu dong nhat".
 *
 * Da dua MaterialMap vao src/ (ban dich nguoc tu build/core-src/), CHI sua 4
 * ham lien quan, moi cho khac giu nguyen. Da doi chieu bang javap: chu ky
 * public/protected cua class moi KHOP TUYET DOI voi class cu trong jar (khong
 * them/bot/doi bat ky method nao) truoc khi tin day la ban thay the an toan.
 *
 * 14/09/2026 (sua tiep, sau khi nguoi dung bao van khong nhat duoc du da build
 * lai): moi quang trong khu mo deu duoc tao voi `inCountry` HARDCODE = 1
 * (xem loadMap() - "new Monster(this, ..., 1)"), khong lien quan gi phe cua
 * nguoi dang danh no. Lan sua truoc (dong bo addGemItem/getGem/removeGem theo
 * `country`) chi doi tu "luon ket o o 0" sang "luon ket o o 1" - VAN sai voi
 * nguoi choi khong thuoc phe 1. Sua tiep: doAttackMonster dung `p.inCountry`
 * (phe THAT cua nguoi danh) thay vi `mt.inCountry` khi goi addGemItem, dam
 * bao luc tao va luc nhat (chiemGem dung p.inCountry that) luon khop nhau
 * voi MOI phe.
 */
public final class KiemMaterialMapNhatDo {

    public static void chay() {
        Kiem.nhom("MaterialMap (khu mo) - nhat nguyen lieu dung theo quoc gia that");

        final String ma = doc("src/real/MaterialMap.java");
        Kiem.dung("doc duoc src/real/MaterialMap.java (da dua vao src/, khong con la bytecode rieng trong jar)",
                !ma.isEmpty());
        if (ma.isEmpty()) {
            return;
        }

        Kiem.dung("addGemItem dung dung `country`, khong con hardcode 0",
                ma.contains("((Vector) this.gemItem.get(country)).add(pt);"));
        Kiem.sai("addGemItem KHONG con hardcode gemItem.get(0) khi them",
                ma.contains("this.gemItem.get(0)).add(pt)"));

        Kiem.dung("getGem dung dung `country`, khong con hardcode 0",
                ma.contains("Vector gemItem = (Vector) this.gemItem.get(country);"));

        Kiem.dung("removeGem dung dung `country`, khong con hardcode 0",
                ma.contains("((Vector) this.gemItem.get(country)).remove(pt);"));

        Kiem.dung("deletePotionAndItemOnGround dung dung `country` (khop MapVongLap goi lan luot 0,1,2)",
                ma.contains("int size = ((Vector) this.gemItem.get(country)).size() - 1;"));

        Kiem.dung("doAttackMonster: rot nguyen lieu dung p.inCountry (phe nguoi danh), khong dung mt.inCountry",
                ma.contains("this.addGemItem(gemItem, p.inCountry);"));
        Kiem.sai("doAttackMonster: KHONG con dung mt.inCountry (hardcode 1, sai phe) khi rot nguyen lieu",
                ma.contains("this.addGemItem(gemItem, mt.inCountry);"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemMaterialMapNhatDo() {
    }
}
