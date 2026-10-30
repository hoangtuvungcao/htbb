import com.htbb.pokemon.GiftcodeAnToanLogic;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Vong QA toan game 23/10/2026 (choi dau-cuoi bang bot, tools/pokemon/kiem_tai/KichBanQaToanGame.java): moi loi tim
 * thay mot phep kiem hoi quy o day (khong can CSDL / mang).
 */
public final class KiemQaToanGame {

    private KiemQaToanGame() {
    }

    public static void chay() {
        Kiem.nhom("QA toan game 23/10/2026");
        giftcodeTuiDay();
        soOTrongHanhTrang();
        thuongNhiemVuNhieuMon();
        chuGoLoi();
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    /** Q1: giftcode ITEM khi tui day - ma bi tru luot, vat pham roi mat. */
    static void giftcodeTuiDay() {
        Kiem.bang("soOCan: ITEM:714:2 = 2 o", 2, GiftcodeAnToanLogic.soOCan("ITEM:714:2", null));
        Kiem.bang("soOCan: LUONGKHOA khong can o", 0, GiftcodeAnToanLogic.soOCan("LUONGKHOA:5", null));
        Kiem.bang("soOCan: GEM moi = 1 o, cung loai hai lan van 1", 1,
                GiftcodeAnToanLogic.soOCan("GEM:8:2:-1,GEM:8:3:-1", id -> false));
        Kiem.bang("soOCan: GEM da co (cong don) = 0 o", 0, GiftcodeAnToanLogic.soOCan("GEM:8:2:-1", id -> id == 8));
        Kiem.bang("soOCan: tron ITEM + GEM moi + LUONGKHOA", 4,
                GiftcodeAnToanLogic.soOCan("ITEM:714:1,ITEM:960:2,GEM:9:1:-1,LUONGKHOA:5", id -> false));
        Kiem.bang("soOCan: null = 0", 0, GiftcodeAnToanLogic.soOCan(null, null));
        final String s = doc("src/real/GiftcodeAnToan.java");
        final int kiem = s.indexOf("GiftcodeAnToanLogic.soOCanChong("); // [chong-vat-pham] o qua ChongVatPham
        final int danhDau = s.indexOf("DatabaseLauDai.ghiLogGiftcode(");
        final int truLuot = s.indexOf("DatabaseLauDai.truLuotGiftcode(");
        Kiem.dung("GiftcodeAnToan: kiem o trong TRUOC khi ghi log / tru luot", kiem > 0 && danhDau > kiem && truLuot > kiem);
        Kiem.dung("GiftcodeAnToan: so voi countSlotInventory (ke ca o mo rong)",
                s.indexOf("p.countSlotInventory() < canO") > kiem);
    }

    /** Q2: huong dan tan thu (bo qua) dem o trong bo o mo rong tui. */
    static void soOTrongHanhTrang() {
        final String s = doc("src/real/Char.java");
        final int i = s.indexOf("public int soOTrongHanhTrang()");
        final String than = i < 0 ? "" : s.substring(i, Math.min(s.length(), i + 200));
        Kiem.dung("Char.soOTrongHanhTrang dung countSlotInventory (sucChuaTui, co oMoRong)",
                than.contains("countSlotInventory()") && !than.contains("MAX_INVENTORY * this.maxBag"));
    }

    /** Q3: thuong nhiem vu "item:1039x3" chi kiem con 1 o - thieu o thi mat 2 Huy chuong Gym. */
    static void thuongNhiemVuNhieuMon() {
        try {
            final java.lang.reflect.Method m = Class.forName("real.NhiemVuEngine").getDeclaredMethod("soOThuong",
                    com.htbb.pokemon.NhiemVuMau.class);
            m.setAccessible(true);
            final com.htbb.pokemon.NhiemVuMau a = new com.htbb.pokemon.NhiemVuMau();
            a.vatPham = "";
            a.thuongKhac = "truyen:Chương 6;item:1039x3";
            Kiem.bang("soOThuong: item:1039x3 = 3 o", 3, m.invoke(null, a));
            a.vatPham = "714";
            a.thuongKhac = "item:1054x1;danh_hieu:X";
            Kiem.bang("soOThuong: vat_pham 1 + item:1054x1 = 2 o", 2, m.invoke(null, a));
            a.vatPham = "";
            a.thuongKhac = "bong:pokex3;thuoc:137x2";
            Kiem.bang("soOThuong: bong / thuoc khong can o hanh trang", 0, m.invoke(null, a));
        } catch (final Exception e) {
            Kiem.dung("soOThuong goi duoc: " + e, false);
        }
        final String s = doc("src/real/NhiemVuEngine.java");
        Kiem.dung("traThuong so countSlotInventory voi soOThuong (khong chi isFullInventory)",
                s.contains("p.countSlotInventory() < canO") && !s.contains("coThuongVatPham(m) && p.isFullInventory()"));
    }

    /** Q4: chu go loi nguoi choi thay ("xu trc mua", mua lai do da ban) + System.err "ok" moi lan nhat do. */
    static void chuGoLoi() {
        Kiem.dung("Map: khong con cau 'xu trc mua' (mua lai do)", !doc("src/real/Map.java").contains("xu trc mua"));
        Kiem.dung("MapXuLyLenh: nhat do khong in System.err \"ok\"", !doc("src/real/MapXuLyLenh.java").contains("System.err.println(\"ok\")"));
    }
}
