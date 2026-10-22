import com.htbb.pokemon.BanDoKhoa;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Do / nguyen lieu KHOA ban NPC o gia thap + Huy (0.9.8, com.htbb.pokemon.BanDoKhoa): gia, tran, tat, luong ban / huy
 * phia may chu (OfflineMap / Map / MapEpNgoc), thuoc van khong ban, danh muc cau hinh, the vat pham client.
 */
final class KiemBanDoKhoa {

    private KiemBanDoKhoa() {
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    public static void chay() {
        Kiem.nhom("Do khoa: ban NPC gia thap (<= 20% gia NPC thuong) + Huy (BanDoKhoa)");
        try {
            chayThat();
        } catch (final Exception e) {
            e.printStackTrace(System.out);
            Kiem.dung("KiemBanDoKhoa chay het: " + e, false);
        }
    }

    static void chayThat() throws Exception {
        // ---- gia thuan
        Kiem.bang("mac dinh 15%", 15, BanDoKhoa.MAC_DINH_PHAN_TRAM);
        Kiem.bang("15% cua 1000 xu = 150", 150L, BanDoKhoa.gia(1000L, 15));
        Kiem.bang("tran cung 20% du cau hinh 90", 200L, BanDoKhoa.gia(1000L, 90));
        Kiem.bang("0% = khong xu", 0L, BanDoKhoa.gia(1000L, 0));
        Kiem.bang("gia am / 0 -> 0", 0L, BanDoKhoa.gia(-5L, 15));
        Kiem.bang("lam tron xuong (7 xu * 15% = 1)", 1L, BanDoKhoa.gia(7L, 15));
        Kiem.bang("kep % am -> 0", 0, BanDoKhoa.kep(-3));
        boolean khongVuot = true;
        for (long g = 0; g < 100000; g += 137) {
            for (int pt = -5; pt <= 120; pt += 5) {
                final long x = BanDoKhoa.gia(g, pt);
                khongVuot &= x >= 0 && x <= g && x * 100 <= g * BanDoKhoa.TRAN_PHAN_TRAM;
            }
        }
        Kiem.dung("moi gia / moi %: 0 <= gia khoa <= 20% gia NPC thuong (ngan sach voi xu)", khongVuot);
        Kiem.dung("huy: do khoa thuong duoc", BanDoKhoa.huyDuoc(true, false, false));
        Kiem.sai("huy: do thue (ndayLoan) khong", BanDoKhoa.huyDuoc(true, true, false));
        Kiem.sai("huy: dang rao ban khong", BanDoKhoa.huyDuoc(true, false, true));
        Kiem.sai("huy: chi cho do khoa (do thuong van 'Vut')", BanDoKhoa.huyDuoc(false, false, false));

        // ---- may chu: luong ban / huy
        final String om = doc("src/real/OfflineMap.java");
        Kiem.dung("OfflineMap.doSellItem: do khoa -> BanDoKhoa.gia (khong con chan cung)", om.contains("xu = com.htbb.pokemon.BanDoKhoa.gia(xu);")
                && om.contains("giaThap && !com.htbb.pokemon.BanDoKhoa.bat()"));
        Kiem.dung("OfflineMap: do khoa vi da mac giu gia NPC thuong (laKhoaDoMac)", om.contains("!item.laKhoaDoMac() && item.lock == 1"));
        Kiem.dung("OfflineMap.doSellGemItem: nguyen lieu khoa gia thap + nhat ky", om.contains("price = com.htbb.pokemon.BanDoKhoa.gia(price);")
                && om.contains("BanDoKhoa.LOG_BAN"));
        Kiem.dung("OfflineMap: do thue / than thu / choi van 'khong ban duoc' truoc nhanh khoa",
                om.indexOf("Vật phẩm này không bán được.") < om.indexOf("BanDoKhoa.bat()"));
        final String m = doc("src/real/Map.java");
        Kiem.dung("Map.doSellGemItem: nguyen lieu khoa gia thap", m.contains("price = com.htbb.pokemon.BanDoKhoa.gia(price);"));
        Kiem.dung("Map.doGiveItemToGround: do khoa -> Huy (tru do thue), nhat ky KHOA_HUY",
                m.contains("BanDoKhoa.huyDuoc(true, item.getTemplate().ndayLoan > 0, item.isSelling)") && m.contains("BanDoKhoa.LOG_HUY")
                        && !m.contains("\"Vật phẩm khoá không bỏ được.\""));
        final String ep = doc("src/real/MapEpNgoc.java");
        Kiem.dung("MapEpNgoc: nguyen lieu khoa Huy theo so luong (listGemitemLock -= n)", ep.contains("player.listGemitemLock[gem.idGemTemplate] -= n;")
                && ep.contains("BanDoKhoa.LOG_HUY"));
        Kiem.dung("thuoc van khong ban NPC: khong co lenh ban binh moi", !om.contains("doSellPotion") && !m.contains("void doSellPotion"));

        // ---- danh muc cau hinh (CSDL day du)
        final String dm = DayDu.danhMucJson();
        Kiem.dung("danh muc co " + BanDoKhoa.K_PHAN_TRAM + " (co_dinh, mac dinh 15)",
                dm.contains("\"khoa\": \"" + BanDoKhoa.K_PHAN_TRAM + "\", \"nhom\": \"chong_clone\", \"lop\": \"co_dinh\"")
                        && dm.matches("(?s).*\"khoa\": \"" + BanDoKhoa.K_PHAN_TRAM.replace(".", "\\.") + "\"[^}]*\"macDinh\": \"15\".*"));

        // ---- client: the vat pham
        final String c = doc("../client_jar/src/game/pokemon/TheVatPham.java");
        Kiem.dung("client: the do khoa ghi 'Bán cho NPC (giá thấp)'", c.contains("Bán cho NPC (giá thấp)"));
        Kiem.dung("client: thuoc ghi ly do khong ban (khong them xu)", c.contains("Thuốc không bán cho NPC"));
        Kiem.dung("client: nut 'Huỷ' do khoa co hoi lai", c.contains("\"Huỷ vật phẩm khoá\"") && c.contains("tvp-huy"));
        Kiem.dung("client: vat pham nhiem vu khong Huy / khong Ban", c.contains("Vật phẩm nhiệm vụ không bán được.")
                && c.contains("Vật phẩm nhiệm vụ không bỏ được."));
    }
}
