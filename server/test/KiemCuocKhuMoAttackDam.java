import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Loi tim thay 13/09/2026 khi ra soat tinh nang "dao nguyen lieu" (khu mo, mapId==17):
 * `CharChiSo.attackDam()` co nhanh rieng cho cuoc (item type 13, luu o `Char.itemAx`)
 * tra ve thang `itemAx.atb[0] * 2`, BO QUA toan bo ne tranh/chi mang/phong thu/boss/roi
 * do khi trung - nhung nhanh nay KHONG kiem tra dang o map nao. Mac cuoc de dao trong
 * khu mo, roi roi khoi map 17 ma QUEN thao ra (chet/hoi sinh/GM warp/duoc keo di - khong
 * co noi nao tu dong thao itemAx khi doi map) thi MOI don danh tiep theo o BAT KY dau
 * (PK, dau truong, bang hoi, boss thuong) deu bi ep cung theo cong thuc cuoc.
 *
 * Sua: them dieu kien `self.map != null && self.map.mapId == 17` vao nhanh do - cong
 * thuc cuoc chi con hieu luc trong dung khu mo, itemAx bi "lo" ra ngoai cung khong con
 * anh huong gi.
 */
public final class KiemCuocKhuMoAttackDam {

    public static void chay() {
        Kiem.nhom("Cuoc khu mo (itemAx) khong con ep cong thuc danh ngoai map 17");

        final String ma = doc("src/real/CharChiSo.java");
        Kiem.dung("doc duoc src/real/CharChiSo.java", !ma.isEmpty());
        if (ma.isEmpty()) {
            return;
        }

        Kiem.dung("attackDam: nhanh cuoc (itemAx) co kiem tra dung map 17",
                ma.contains("if (self.itemAx != null && self.map != null && self.map.mapId == 17) {"));
        Kiem.sai("attackDam: KHONG con nhanh cuoc thieu dieu kien map (loi cu)",
                ma.contains("if (self.itemAx != null) {\n                return self.itemAx.atb[0] * 2;"));

        // Nhanh cuoc phai nam TRUOC cac kiem tra boss/ne tranh/roi do (khong doi thu tu,
        // chi doi dieu kien) - dam bao van la "duong tat" som nhu thiet ke ban dau, chi
        // sua PHAM VI ap dung chu khong sua vi tri.
        final int viTriCuoc = ma.indexOf("self.itemAx != null && self.map != null && self.map.mapId == 17");
        final int viTriBoss = ma.indexOf("actor.isBossGetItemByHit()");
        Kiem.dung("nhanh cuoc van nam TRUOC kiem tra roi do khi trung boss",
                viTriCuoc >= 0 && viTriBoss > viTriCuoc);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemCuocKhuMoAttackDam() {
    }
}
