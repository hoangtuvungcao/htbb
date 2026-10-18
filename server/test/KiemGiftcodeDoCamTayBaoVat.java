import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * CHARACTERIZATION TEST (xem chu thich dau test/Kiem.java): chot lai HANH VI HIEN TAI,
 * khong phai hanh vi "dung". Phat hien 05/09/2026 khi nguoi choi hoi cach tao giftcode
 * "VKTB 8x max chi so" ma khong biet lam sao.
 *
 * LOI THAT: Char.createVuKhiThoiTrangTHANBINH_NEW nhan tham so `is8x` nhung KHONG HE DOC
 * no trong than ham - `iditem` bi cung co dinh bang "758 + charClass", tuc LUON LUON ra
 * mon bac THAP NHAT (level 60), bat ke `is8x` la true hay false. Ca hai nhanh giftcode
 * "VKTB" va "VKTBMAX" (DatabaseGiftCode.java) deu goi ham nay voi is8x=true - dung y dinh
 * ro rang la muon ra bac 80 ("8x"), nhung vi tham so bi bo quen nen KHONG BAO GIO ra duoc
 * bac do. Hai loai giftcode nay hien chi tao duoc than binh bac 60.
 *
 * DUONG DUY NHAT con dung de chon dung bac: Char.createVuKhiThoiTrangTHANBINH_LV, nhan
 * `level` (phai dung 60/70/80) va CHON DUNG dong id theo do; goi qua giftcode "VKTBLV"
 * (vd VKTBLV:80:0:0 = bac 80, khong tinh luyen, vinh vien). Ham nay cung LUON dat
 * isMax=true khi goi tu giftcode (khong doc duoc tu chuoi), nen "VKTBLV:80:..." da la
 * "max chi so" san, khong can loai rieng nao khac.
 *
 * KHONG tu sua loi is8x o day: day la logic gameplay dang chay that, sua se doi hanh vi
 * cua moi giftcode VKTB/VKTBMAX DA PHAT HANH (nguoi choi da/dang dung se nhan mon KHAC
 * voi truoc). Chi ghi lai bang chu thich trong HTBB-Web shared/utils/thuong.ts (truoc la
 * web/admin/inc/thuong.php cua website PHP da bo) va cho nguoi
 * dung tu quyet dinh co sua khong. Neu sau nay is8x duoc noi day du, test nay se do -
 * dung nhu thiet ke cua characterization test.
 */
public final class KiemGiftcodeDoCamTayBaoVat {

    private static final String F_CHAR = "src/real/Char.java";
    private static final String F_GIFTCODE = "src/data/DatabaseGiftCode.java";

    public static void chay() {
        Kiem.nhom("Giftcode VKTB/VKTBMAX - is8x bi bo quen, VKTBLV la duong dung de chon bac");

        final String ch = doc(F_CHAR);
        final String gc = doc(F_GIFTCODE);
        Kiem.dung("doc duoc " + F_CHAR, !ch.isEmpty());
        Kiem.dung("doc duoc " + F_GIFTCODE, !gc.isEmpty());
        if (ch.isEmpty() || gc.isEmpty()) {
            return;
        }

        // ---- 1. than ham createVuKhiThoiTrangTHANBINH_NEW: is8x co trong chu ky nhung
        // KHONG duoc doc trong than ham. ----
        final int mocKyHieu = ch.indexOf("public static void createVuKhiThoiTrangTHANBINH_NEW(");
        Kiem.dung("tim thay createVuKhiThoiTrangTHANBINH_NEW", mocKyHieu > 0);
        if (mocKyHieu > 0) {
            final int mocDongKyHieu = ch.indexOf('\n', mocKyHieu);
            final String dongKyHieu = ch.substring(mocKyHieu, mocDongKyHieu);
            Kiem.dung("chu ky ham co tham so is8x", dongKyHieu.contains("boolean is8x"));

            // Than ham: tu dong sau chu ky toi dau ham ke tiep (isItemThanThu dung ngay
            // sau createVuKhiThoiTrangTHANBINH_NEW trong file).
            final int mocHamSau = ch.indexOf("public static boolean isItemThanThu(", mocKyHieu);
            Kiem.dung("tim thay diem ket than ham (ham ke tiep)", mocHamSau > mocKyHieu);
            final String than = ch.substring(mocDongKyHieu, Math.max(mocDongKyHieu, mocHamSau));

            Kiem.dung("iditem trong than ham bi CUNG CO DINH ve bac thap nhat (758 + charClass)",
                    than.contains("int iditem = 758 + p.charClass;"));
            // Day la LOI: neu is8x tung duoc sua de doc dung, than ham se phai nhac toi
            // bien is8x o dau do (vd mot nhanh if/else chon id theo is8x) - luc do phep
            // kiem duoi day se do, nhac nguoi sua cap nhat lai chu thich trong thuong.ts.
            Kiem.sai("than ham KHONG doc bien is8x o dau ca (dung la loi dang mo ta)",
                    than.contains("is8x"));
        }

        // ---- 2. createVuKhiThoiTrangTHANBINH_LV: DUONG DUY NHAT chon dung bac. ----
        final int mocLV = ch.indexOf("public static void createVuKhiThoiTrangTHANBINH_LV(");
        Kiem.dung("tim thay createVuKhiThoiTrangTHANBINH_LV", mocLV > 0);
        if (mocLV > 0) {
            final int mocKetLV = ch.indexOf("public static void createVuKhiThoiTrangTHANBINH_NEW(", mocLV);
            final String thanLV = ch.substring(mocLV, mocKetLV > mocLV ? mocKetLV : ch.length());
            Kiem.dung("level == 60 -> bac 758 + charClass", thanLV.contains("758 + p.charClass"));
            Kiem.dung("level == 70 -> bac 763 + charClass", thanLV.contains("763 + p.charClass"));
            Kiem.dung("level == 80 (\"8x\") -> bac 768 + charClass", thanLV.contains("768 + p.charClass"));
            Kiem.dung("level khac 60/70/80 thi return som (khong tao gi ca, im lang)",
                    thanLV.contains("return; // Return nếu level không hợp lệ"));
        }

        // ---- 3. Giftcode: VKTB/VKTBMAX goi voi is8x=true (dung y muon ro rang la bac
        // 8x, du khong lam duoc); VKTBLV goi isMax=true CUNG (luon max, khong can loai
        // rieng). ----
        // Chu y: "if (tachTime[0].equals("X")) {" co HAI dau ngoac dong lien nhau sau
        // chuoi X - mot dong .equals(...), mot dong if (...) - phai khop du ca hai.
        Kiem.dung("giftcode VKTB goi createVuKhiThoiTrangTHANBINH_NEW voi is8x=true",
                Pattern.compile(
                        "\"VKTB\"\\)\\)\\s*\\{\\s*player\\.createVuKhiThoiTrangTHANBINH_NEW\\([^;]*,\\s*false,\\s*true,")
                        .matcher(gc).find());
        Kiem.dung("giftcode VKTBMAX goi createVuKhiThoiTrangTHANBINH_NEW voi is8x=true",
                Pattern.compile(
                        "\"VKTBMAX\"\\)\\)\\s*\\{\\s*player\\.createVuKhiThoiTrangTHANBINH_NEW\\([^;]*,\\s*true,\\s*true,")
                        .matcher(gc).find());
        Kiem.dung("giftcode VKTBLV goi createVuKhiThoiTrangTHANBINH_LV voi isMax=true (luon max)",
                Pattern.compile(
                        "\"VKTBLV\"\\)\\)\\s*\\{[^}]*player\\.createVuKhiThoiTrangTHANBINH_LV\\([^;]*,\\s*true,\\s*Integer\\.parseInt\\(tachTime\\[1\\]\\),\\s*Integer\\.parseInt\\(tachTime\\[2\\]\\),")
                        .matcher(gc).find());

        // ---- 4. Bang cu phap trong web da ghi ro loi + huong dan dung (khong lang le
        // de admin tiep tuc tao ma sai). ----
        if (WebHtbb.boQua("bang cu phap giftcode cua HTBB-Web")) {
            return;
        }
        final String thuong = WebHtbb.doc("shared/utils/thuong.ts");
        Kiem.dung("doc duoc HTBB-Web/shared/utils/thuong.ts", !thuong.isEmpty());
        Kiem.dung("thuong.ts canh bao VKTB/VKTBMAX chi ra duoc bac 60",
                thuong.contains("chỉ tạo được bậc thấp"));
        Kiem.dung("thuong.ts chi VKTBLV la duong dung, vi du level=80",
                thuong.contains("\"VKTBLV:80:0:0\""));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemGiftcodeDoCamTayBaoVat() {
    }
}
