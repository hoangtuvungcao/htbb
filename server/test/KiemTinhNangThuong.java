/**
 * Hoi quy cac loi bot NGUOI CHOI THUONG tim ra (tools/pokemon/kiem_tai/KichBanTinhNangThuong, nhom tn_pokemon / tn_he_thong,
 * 29/10/2026). Nha tre (vut het pet da gui van de trung) kiem o KiemHeSinhThai.
 */
public final class KiemTinhNangThuong {

    private KiemTinhNangThuong() {
    }

    public static void chay() {
        Kiem.nhom("bot nguoi choi thuong: loi tim ra (tn_*)");
        doiKhuHoiChieu();
    }

    /**
     * Doi khu (-30 npc -34): chon trung khu dang dung ("Bạn đang ở tại khu này") truoc day van ghi timeNextRegion = now, nen
     * lan doi that ngay sau do bi bao "chờ 20 giây" (bot tn_khu.doi). Hoi chieu chi duoc ghi trong nhanh DA doi khu.
     */
    static void doiKhuHoiChieu() {
        final String s;
        try {
            s = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/MapDoMenu.java")), "UTF-8");
        } catch (final java.io.IOException e) {
            Kiem.dung("doi khu: doc MapDoMenu.java", false);
            return;
        }
        final int i = s.indexOf("case -34:");
        final int j = i < 0 ? -1 : s.indexOf("case -32:", i);
        final String than = i < 0 || j < 0 ? "" : s.substring(i, j);
        final int ghi = than.indexOf("player.timeNextRegion = now;");
        final int doi = than.indexOf("player.region = idOptionMenu;");
        final int trung = than.indexOf("đang ở tại khu này");
        Kiem.dung("doi khu: chi MOT cho ghi hoi chieu", ghi >= 0 && than.indexOf("player.timeNextRegion = now;", ghi + 1) < 0);
        Kiem.dung("doi khu: hoi chieu ghi ngay truoc khi doi region (sau nhanh giao hang, khong o nhanh trung khu / xa bien)",
                ghi >= 0 && doi > ghi && than.substring(ghi, doi).contains("map.playerExit(player);")
                        && ghi > than.indexOf("player.monster != null"));
        Kiem.dung("doi khu: nhanh 'dang o tai khu nay' khong cham hoi chieu",
                doi > 0 && trung > doi && !than.substring(doi, trung).contains("timeNextRegion"));
    }
}
