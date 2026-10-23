import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Goi 26 phai vua du cho client CU (< 2.7, doc so hieu ung bang MOT byte -> toi da 255)
 * VA phai mang duoc "Set danh" (EffectBuff.SET_DANH = 9000 = 8700 + 300) - hieu ung trieu
 * hoi cua skin Kim Cang Long khi Choi Set phat dong Set Lan.
 *
 * VI SAO CAN (12/09/2026). Nguoi choi bao "khong thay hieu ung Set danh". Log server ghi
 * 422 lan "goi 26 co 256 hieu ung, vuot tran 255. Bo bot id 9000": 6 skin + 250 id trong
 * MessageCreator.id_eff_tool_ngu_long = 256, va vi 300 nam CUOI mang nen chinh no bi cat
 * cho moi client cu - ma 757/782 luot dang nhap tren server la client 2.0.9. Server van
 * kich hoat hieu ung (them 3 giay choang) nhung client khong co du lieu khung hinh de ve.
 * Cach sua la go MOT id khong dung khoi id_eff_tool_ngu_long (da ra soat toan repo truoc
 * khi go) - test nay chot de lan sau them hieu ung moi ma vuot 255 thi biet ngay, khong
 * doi nguoi choi bao.
 *
 * Kem theo: EffectBuff.dyhore truoc dai 300 (0..299) nen createMsgNewEffectSkill5Long tra
 * dyhore[9000 - 8700 = 300] la tran mang - nang len 301.
 *
 * Khong chay duoc goi 26 that o day (can DataGame nap anh), nen doc mang tu ma nguon.
 */
public final class KiemGoi26SetDanh {

    private KiemGoi26SetDanh() {
    }

    public static void chay() {
        Kiem.nhom("Goi 26 - du cho client cu (<= 255) va mang duoc Set danh (300)");

        final String mc = doc("src/real/MessageCreator.java");
        Kiem.dung("doc duoc src/real/MessageCreator.java", !mc.isEmpty());
        if (mc.isEmpty()) {
            return;
        }

        final List<Integer> ids = docMangNguLong(mc);
        Kiem.dung("doc duoc mang id_eff_tool_ngu_long tu ma nguon (tren 200 id)", ids.size() > 200);

        // Goi 26 = du lieu 6 skin (xep truoc) + toan bo mang nay. Client cu doc so luong bang
        // 1 byte -> vuot 255 la server cat tu CUOI, va phan tu cuoi chinh la 300 (Set danh).
        final int tong = real.SkinThuCuoi.SO_SKIN + ids.size();
        Kiem.dung("6 skin + id_eff_tool_ngu_long = " + tong + " <= 255 (client cu doc bang 1 byte)",
                tong <= 255);

        Kiem.dung("id 300 (Set danh, EffectBuff.SET_DANH - 8700) co trong id_eff_tool_ngu_long",
                ids.contains(300));
        Kiem.bang("EffectBuff.SET_DANH = 9000 (= 8700 + 300)",
                Integer.valueOf(9000), Integer.valueOf(real.EffectBuff.SET_DANH));
        Kiem.dung("khong id nao trung nhau trong id_eff_tool_ngu_long",
                new java.util.HashSet<Integer>(ids).size() == ids.size());

        // Du lieu khung hinh cua 300 phai co that, khong thi goi 26 in "thieu map/d_effect/300"
        // va bo qua - hieu ung lai khong ve duoc dung nhu truoc.
        Kiem.dung("co map/d_effect/300", new java.io.File("map/d_effect/300").isFile());
        Kiem.dung("co map/d_effect/300.png", new java.io.File("map/d_effect/300.png").isFile());
        for (final int id : ids) {
            if (!new java.io.File("map/d_effect/" + id).isFile()) {
                Kiem.dung("id " + id + " trong id_eff_tool_ngu_long nhung THIEU map/d_effect/" + id, false);
            }
        }

        // dyhore phai phu toi chi so 300: createMsgNewEffectSkill5Long tra dyhore[idEff - 8700].
        Kiem.dung("EffectBuff.dyhore dai >= 301 (phu duoc SET_DANH: 9000 - 8700 = 300)",
                real.EffectBuff.dyhore.length >= 301);
        Kiem.dung("createMsgNewEffectSkill5Long van tra dyhore theo idEff - 8700",
                mc.contains("EffectBuff.dyhore[(idEff > 8700) ? (idEff - 8700) : idEff]"));

        // Cho kich hoat: CharTrangBi nhanh Set Lan (id == 1), muc tieu la nguoi choi, 5%,
        // them 3000 ms choang va gan SET_DANH cho MUC TIEU.
        final String tb = doc("src/real/CharTrangBi.java");
        Kiem.dung("doc duoc src/real/CharTrangBi.java", !tb.isEmpty());
        if (!tb.isEmpty()) {
            Kiem.dung("dieu kien: dang mang skin Kim Cang Long (timSkinDangMac) va deo Choi Set (dangDeoChoiSet)",
                    tb.contains("SkinThuCuoi.timSkinDangMac(self) != null && self.dangDeoChoiSet()"));
            Kiem.dung("ti le 5%", tb.contains("&& Map.r.nextInt(100) < 5) {"));
            Kiem.dung("them 3 giay choang (msChoang += 3000L)", tb.contains("msChoang += 3000L;"));
            Kiem.dung("gan SET_DANH cho muc tieu (actor), khong phai nguoi danh",
                    tb.contains("actor.addEffBuff(EffectBuff.SET_DANH,"));
        }
        // Choi Set la vat pham MODEL (type 13, vao wModel.wpModel) - khong phai thu cuoi, nen
        // vua cuoi linh thu mang skin vua deo Choi Set duoc. Neu ai doi Choi Set thanh thu cuoi
        // thi hai dieu kien tren loai tru nhau va hieu ung chet han.
        final String item = doc("src/real/Item.java");
        Kiem.dung("Item.isChoiSet = template 614 hoac 618",
                item.contains("return this.tempateID == 614 || this.tempateID == 618;"));
        final String sql = DayDu.sql();
        Kiem.dung("data_item 618 'Choi set' co type 13 (vat pham model, khong phai thu cuoi)",
                sql.contains("(618,'Chổi set',13,"));
        Kiem.dung("data_item 614 'Choi dien anh' co type 13",
                sql.contains("(614,'Chổi điện ánh',13,"));
    }

    /** Doc mang id_eff_tool_ngu_long tu chuoi nguon: `id_eff_tool_ngu_long = new int[]{...};`. */
    private static List<Integer> docMangNguLong(final String mc) {
        final List<Integer> ra = new ArrayList<Integer>();
        final Matcher m = Pattern.compile("id_eff_tool_ngu_long = new int\\[\\]\\{([^}]*)\\}").matcher(mc);
        if (!m.find()) {
            return ra;
        }
        final Matcher so = Pattern.compile("\\d+").matcher(m.group(1));
        while (so.find()) {
            ra.add(Integer.valueOf(so.group()));
        }
        return ra;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
