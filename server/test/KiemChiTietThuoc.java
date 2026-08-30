import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Moi id thuoc dang dung phai co trong LoginHandler.POTION_SHOP, khong thi nguoi choi
 * KHONG MO DUOC CHI TIET vat pham do.
 *
 * CO CHE. Client chon duong hien thi chi tiet thuoc theo danh sach nay
 * (WindowInfoScr.getPotionInfo trong ban port Unity, va ban JAR nguoi choi dang dung):
 *
 *   id CO trong danh sach -> tim data_shop co id = idThuoc-19, lay mo ta. Khong tim thay
 *                            hoac mo ta rong thi khong hien them gi - van mo duoc chi tiet.
 *   id KHONG co           -> doc Res.potionTemplates[idThuoc-19].
 *
 * Mang Res.potionTemplates duoc cap phat theo Char.CHAR_POTION NGAY LUC NAP LOP, tuc 25 o
 * (goi dang nhap dat lai CHAR_POTION = 162 nhung sau do), va server chi gui 20 dong
 * price/recovered. Nen:
 *   idThuoc-19 >= 20  -> phan tu null   -> NullReference
 *   idThuoc-19 >= 25  -> ngoai bien     -> IndexOutOfRange
 * Ca hai deu lam cua so chi tiet khong mo len duoc.
 *
 * DA XAY RA 30/08/2026: sach ky nang pet chuyen tu data_item 722 sang thuoc 111. Danh sach
 * luc do liet ke 21..105 roi nhay thang sang 112 - khoang trong 106..111 khong ai de y vi
 * cac dong do trong data_potion deu rong. Sach roi dung vao 111 -> khong mo duoc chi tiet.
 * Hai qua bong 160/161 cung dinh y het, chi la chua ai bao.
 *
 * Test doc MANG THAT qua LoginHandler.POTION_SHOP va liet ke hang so bang phan chieu, nen
 * them mot loai thuoc moi ma quen cap nhat danh sach la hong ngay tai day.
 */
public final class KiemChiTietThuoc {

    /** Cong thuc client dung: chi so tra cuu = idThuoc - 19. */
    private static final int LECH = 19;

    public static void chay() {
        Kiem.nhom("Chi tiet thuoc - moi id dang dung phai co trong POTION_SHOP");

        final short[] ds = real.cmd.LoginHandler.POTION_SHOP;
        Kiem.dung("doc duoc POTION_SHOP", ds != null && ds.length > 100);
        if (ds == null || ds.length == 0) {
            return;
        }

        // ---- Goi tin: writeByte(length) + client doc readUnsignedByte ----
        Kiem.dung("do dai danh sach <= 255 (gui bang mot byte)", ds.length <= 255);
        for (final short v : ds) {
            Kiem.dung("id " + v + " nam trong 0..255 (gui bang mot byte)", v >= 0 && v <= 255);
        }

        // ---- Tang dan va khong trung: trung la lang phi byte, lech thu tu la kho doi chieu ----
        boolean tangDan = true;
        for (int i = 1; i < ds.length; i++) {
            tangDan = tangDan && ds[i] > ds[i - 1];
        }
        Kiem.dung("danh sach tang dan va khong co id trung", tangDan);

        // ---- Nguong lay THANG tu so dong price/recovered ma MessageCreator gui ----
        final int soDongGui = soDongPotionTemplate();
        Kiem.dung("doc duoc so dong price/recovered trong MessageCreator", soDongGui > 0);
        final int nguong = LECH + soDongGui;

        // ---- Moi hang so Potion tu nguong tro len phai co trong danh sach ----
        final List<int[]> thieu = new ArrayList<int[]>();
        int soHangSo = 0;
        for (final Field f : real.Potion.class.getDeclaredFields()) {
            if (!f.getName().startsWith("TYPE_") || !Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            final int id;
            try {
                id = f.getShort(null);
            } catch (final Exception e) {
                continue;
            }
            soHangSo++;
            if (id >= nguong && !co(ds, id)) {
                thieu.add(new int[] {id, 0});
                Kiem.dung("thuoc " + f.getName() + " (id " + id + ") co trong POTION_SHOP", false);
            }
        }
        Kiem.dung("quet duoc cac hang so Potion.TYPE_* (tren 100)", soHangSo > 100);
        Kiem.bang("so hang so id >= " + nguong + " bi bo sot",
                Integer.valueOf(0), Integer.valueOf(thieu.size()));

        // ---- Ba cai da tung hong, chot rieng cho khoi tai pham ----
        Kiem.dung("sach ky nang pet (111) co trong danh sach",
                co(ds, real.Potion.TYPE_SACH_SKILL_PET));
        Kiem.dung("qua bong bac (160) co trong danh sach", co(ds, real.Potion.TYPE_QUA_BONG_BAC));
        Kiem.dung("qua bong vang (161) co trong danh sach", co(ds, real.Potion.TYPE_QUA_BONG_VANG));

        // ---- Danh sach phai duoc gui di, khong phai khai bao roi bo quen ----
        final String lh = doc("src/real/cmd/LoginHandler.java");
        Kiem.dung("goi dang nhap gui do dai danh sach",
                lh.contains("writeByte(POTION_SHOP.length)"));
        Kiem.dung("goi dang nhap gui tung id", lh.contains("writeByte(POTION_SHOP[ii])"));
    }

    /**
     * So dong price/recovered MessageCreator gui xuong. Do chinh la so o client dien vao
     * Res.potionTemplates, nen no quyet dinh tu id nao tro len se tra cuu vao o rong.
     */
    private static int soDongPotionTemplate() {
        final Matcher m = Pattern.compile(
                "for \\(int i = 0; i < (\\d+); \\+\\+i\\) \\{\\s*"
                        + "m\\.dos\\.writeShort\\(Map\\.potionTemplates2")
                .matcher(doc("src/real/MessageCreator.java"));
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static boolean co(final short[] ds, final int id) {
        for (final short v : ds) {
            if (v == id) {
                return true;
            }
        }
        return false;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemChiTietThuoc() {
    }
}
