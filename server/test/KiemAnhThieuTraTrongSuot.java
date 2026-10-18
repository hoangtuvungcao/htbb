import data.DataGame;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Thieu anh -> server tra anh PNG TRONG SUOT, khong tra goi rong (DataGame.ghiAnh).
 *
 * Su co 02/09/2026 ("vao map boss khong hien thi nhan vat", ke ca nhan vat khong buff cap):
 * client xin anh pet 5221 (map/pet/21.png khong co - 105 pet Rong/Cu/Tho bay... mang idImg 21)
 * va 5269 (Lan hoa hinh). Ban 2d778f5 tra goi 0 byte de client thoi hoi lai; nhung client
 * (MessageHandler case -51) dat img.isLoad = false TRUOC roi Image.createImage(0 byte) ->
 * img.img null -> AnimalMove.paint goi imgIcon.img.getHeight() -> NullPointerException trong
 * vong ve actor -> ca nhan vat bien mat. Anh trong suot hop le thi client ve "khong gi",
 * khong loi, va cung khong hoi lai.
 */
public final class KiemAnhThieuTraTrongSuot {

    private static final byte[] CHU_KY_PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public static void chay() {
        Kiem.nhom("Thieu anh -> tra PNG trong suot, khong tra goi rong (DataGame.ghiAnh)");
        anhTrongSuotHopLe();
        pngDuPhongHopLe();
        ghiAnhKhongConGoiRong();
        tienDeClient();
    }

    private static void anhTrongSuotHopLe() {
        final byte[] anh = DataGame.anhTrongSuot();
        Kiem.dung("anhTrongSuot() khong null va dai hon 60 byte", anh != null && anh.length > 60);
        Kiem.dung("bat dau bang chu ky PNG", coChuKyPng(anh));
        Kiem.dung("goi lai tra ve CUNG mot mang (tao mot lan)", anh == DataGame.anhTrongSuot());
        try {
            final java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(new ByteArrayInputStream(anh));
            Kiem.dung("ImageIO doc lai duoc", bi != null);
            if (bi != null) {
                Kiem.bang("rong 32", DataGame.ANH_TRONG_SUOT_RONG, bi.getWidth());
                Kiem.bang("cao 96 (AnimalMove chia cho so khung toi 96 van >= 1)", DataGame.ANH_TRONG_SUOT_CAO, bi.getHeight());
                boolean trongSuot = true;
                for (int y = 0; y < bi.getHeight() && trongSuot; y += 5) {
                    for (int x = 0; x < bi.getWidth(); x += 3) {
                        if (((bi.getRGB(x, y) >>> 24) & 0xFF) != 0) {
                            trongSuot = false;
                            break;
                        }
                    }
                }
                Kiem.dung("moi diem anh deu alpha = 0 (ve len khong thay gi)", trongSuot);
            }
        } catch (final Throwable t) {
            Kiem.dung("ImageIO doc lai duoc (" + t + ")", false);
        }
    }

    private static void pngDuPhongHopLe() {
        final byte[] png = DataGame.PNG_1X1_TRONG_SUOT;
        Kiem.bang("PNG 1x1 du phong dai 67 byte", 67, png.length);
        Kiem.dung("PNG 1x1 du phong co chu ky PNG", coChuKyPng(png));
        try {
            final java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(new ByteArrayInputStream(png));
            Kiem.dung("PNG 1x1 du phong doc duoc, 1x1, trong suot",
                    bi != null && bi.getWidth() == 1 && bi.getHeight() == 1 && ((bi.getRGB(0, 0) >>> 24) & 0xFF) == 0);
        } catch (final Throwable t) {
            Kiem.dung("PNG 1x1 du phong doc duoc (" + t + ")", false);
        }
    }

    private static void ghiAnhKhongConGoiRong() {
        final String dg = doc("src/data/DataGame.java");
        final int bd = dg.indexOf("private static void ghiAnh(");
        final String than = bd < 0 ? "" : dg.substring(bd, Math.min(dg.length(), dg.indexOf("\n    }\n", bd) + 6));
        Kiem.dung("tim thay ghiAnh()", bd > 0);
        Kiem.dung("thieu anh -> ghi anhTrongSuot() vao goi", than.contains("m.dos.write(anhTrongSuot());"));
        Kiem.sai("khong con 'gui goi rong'", than.contains("gui goi rong"));
        Kiem.dung("bao thieu moi id MOT lan (khong ngap log nhu 297 dong 5269)",
                than.contains("daBaoThieu.add(Integer.valueOf(idImg))"));
        // Cac dai tu ghi thang mang cung phai qua ghiAnh: 800-806 (806 tung khong ghi gi) va 900-999.
        Kiem.dung("dai 800-806 di qua ghiAnh", dg.contains("ghiAnh(m, imgOther, idImg - 800, idImg);"));
        Kiem.dung("dai 900-999 di qua ghiAnh", dg.contains("ghiAnh(m, imgOther, idImg - 900, idImg);"));
        Kiem.sai("khong con m.dos.write(imgOther[...]) truc tiep", dg.contains("m.dos.write(imgOther["));
    }

    /** Ly do goi rong gay chet ve: ghim lai tu ma client de ai doc test cung hieu. */
    private static void tienDeClient() {
        final String mh = doc("../client_pc_ios/ClientUnity/Assets/Scripts/Game/MessageHandler.cs");
        final String am = doc("../client_pc_ios/ClientUnity/Assets/Scripts/Game/AnimalMove.cs");
        if (mh.isEmpty() || am.isEmpty()) {
            System.out.println("   bo qua tien de client (khong co ClientUnity trong ban nay)");
            return;
        }
        final int vt = mh.indexOf("case -51:");
        final String khoi = vt < 0 ? "" : mh.substring(vt, Math.min(mh.length(), vt + 500));
        Kiem.dung("(tien de) client dat isLoad = false TRUOC khi createImage - goi rong de lai img null",
                khoi.contains("imageIcon.isLoad = false;") && khoi.indexOf("imageIcon.isLoad = false;") < khoi.indexOf("Image.createImage("));
        Kiem.dung("(tien de) AnimalMove.paint dung imgIcon.img.getHeight() khi !isLoad - img null la NPE",
                am.contains("!imgIcon.isLoad") && am.contains("imgIcon.img.getHeight()"));
        Kiem.dung("(tien de) pet xin anh o dai 5200 + imgID", am.contains("imgID + 5200"));
    }

    private static boolean coChuKyPng(final byte[] anh) {
        if (anh == null || anh.length < CHU_KY_PNG.length) {
            return false;
        }
        for (int i = 0; i < CHU_KY_PNG.length; i++) {
            if (anh[i] != CHU_KY_PNG[i]) {
                return false;
            }
        }
        return true;
    }

    private static String doc(final String duongDan) {
        try {
            return new String(Files.readAllBytes(Paths.get(duongDan)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemAnhThieuTraTrongSuot() {
    }
}
