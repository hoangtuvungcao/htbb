import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Dem nguoc "giai tan bang" (60 phut, qua datTimeStartDelGiaiTan()) phai dung o MOI noi
 * dung den no - khong duoc phep con noi nao gan `timeStartDel` truc tiep hoac dua vao kieu
 * kiem "== 0L" don le, vi ca hai deu de lot mot gia tri THO (4320 phut/3 ngay tho cua
 * NewClan.timeDel() trong jar) ma khong duoc sua.
 *
 * VI SAO CAN - LICH SU LAP LAI HAI LAN CUNG MOT LOI:
 *   - 11/09/2026: cac nhanh ROI/DUOI thanh vien bi bo sot vi NewClan.removeMember() (khoa
 *     trong jar) tu dat lai timeStartDel THO ngay ben trong no khi cat qua duoi 10 nguoi -
 *     sua bang cach goi datTimeStartDelGiaiTan() thay vi gan truc tiep.
 *   - 12/09/2026: nhanh TAO BANG MOI van con gan tho (bang moi luon co 1 thanh vien, duoi
 *     10 ngay tu dau) - sua tuong tu, NHUNG con lai o vet cu: kieu kiem "if (timeStartDel
 *     == 0L)" o cac nhanh khac chi bat duoc sentinel "chua bao gio dem", KHONG bat duoc mot
 *     gia tri KHAC 0 nhung van la gia tri THO qua lau - dung nhu truong hop bang da tao
 *     TRUOC khi code fix chay, roi bang chu bam "giai tan" hi vong duoc 60 phut nhung van
 *     thay so cu (~4276 phut). Refactor 12/09/2026: gop toan bo logic do vao MOT ham
 *     canDatLaiDemNguocGiaiTan() duy nhat, dung ca kieu "== 0L" (sentinel) LAN kieu "dang
 *     dai hon that" (gia tri THO/cu) - ap dung o MOI noi (roi/duoi thanh vien, bam giai tan
 *     thu cong, VA CA luc mo xem thong tin bang - tu sua ngay khi ai do nhin vao, khong bat
 *     phai bam "giai tan" truoc).
 *
 * Test nay quet TOAN BO file, doi hoi: (1) khong con gan `timeStartDel` THO ngoai chinh
 * dinh nghia ham lui moc; (2) khong con kieu kiem "timeStartDel == 0L" DON LE (ngoai than
 * ham canDatLaiDemNguocGiaiTan chinh no); (3) ca 5 noi da biet phai dung goi lai ham gop
 * nay; (4) nhanh tao bang goi thang datTimeStartDelGiaiTan() (khong can qua ham gop, vi
 * bang moi luon phai duoc dat dong hoan toan, khong phai "chi sua khi qua dai").
 */
public final class KiemGiaiTanBangMoiTao {

    private KiemGiaiTanBangMoiTao() {
    }

    public static void chay() {
        Kiem.nhom("Giai tan bang - dem nguoc 60 phut phai dung o MOI noi, khong con cho bi bo sot");

        final String noiDung = doc("src/real/MapDoi.java");
        Kiem.dung("doc duoc src/real/MapDoi.java", !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        // ---- 1. Gan tho chi duoc phep trong chinh dinh nghia ham lui moc ----
        // Dau "." o dau de KHONG khop voi doan van ta bang loi trong chu thich JavaDoc
        // (viet "`timeStartDel = System.currentTimeMillis()`" KHONG co doi tuong dung
        // truoc) - ma nguon THAT luon co doi tuong.truong (clanInfo., clan., clanx., ...).
        final String GAN_THO = ".timeStartDel = System.currentTimeMillis()";
        final int viTriHam = noiDung.indexOf("private static void datTimeStartDelGiaiTan(");
        Kiem.dung("tim thay dinh nghia datTimeStartDelGiaiTan()", viTriHam >= 0);
        if (viTriHam < 0) {
            return;
        }
        final int ketThucHam = noiDung.indexOf("}", viTriHam);
        Kiem.dung("tim thay diem ket cua datTimeStartDelGiaiTan()", ketThucHam > viTriHam);

        int soLanGanTho = 0;
        int soLanGanThoNgoaiHam = 0;
        int i = noiDung.indexOf(GAN_THO);
        while (i >= 0) {
            soLanGanTho++;
            if (i < viTriHam || i > ketThucHam) {
                soLanGanThoNgoaiHam++;
                Kiem.dung("gan tho '" + GAN_THO + "' o vi tri ky tu " + i
                                + " PHAI nam trong than datTimeStartDelGiaiTan(), khong o ben ngoai",
                        false);
            }
            i = noiDung.indexOf(GAN_THO, i + GAN_THO.length());
        }
        Kiem.dung("dinh nghia ham co dung 1 lan gan tho (chinh no)", soLanGanTho >= 1);
        Kiem.dung("KHONG con noi nao khac trong file gan tho ngoai ham lui moc",
                soLanGanThoNgoaiHam == 0);

        // ---- 2. Ham gop canDatLaiDemNguocGiaiTan() phai ton tai va bat ca hai truong hop ----
        final int viTriHamGop = noiDung.indexOf("private static boolean canDatLaiDemNguocGiaiTan(");
        Kiem.dung("tim thay dinh nghia canDatLaiDemNguocGiaiTan()", viTriHamGop >= 0);
        if (viTriHamGop >= 0) {
            final int ketThucHamGop = noiDung.indexOf("}", viTriHamGop);
            final String thanHamGop = noiDung.substring(viTriHamGop, Math.max(viTriHamGop, ketThucHamGop));
            Kiem.dung("ham gop bat truong hop sentinel (timeStartDel == 0L)",
                    thanHamGop.contains("timeStartDel == 0L"));
            Kiem.dung("ham gop bat truong hop gia tri THO qua lau (timeDel() > PHUT_CHO_GIAI_TAN)",
                    thanHamGop.contains("timeDel() > PHUT_CHO_GIAI_TAN"));
        }

        // ---- 3. KHONG con kieu kiem "timeStartDel == 0L" DON LE ben ngoai ham gop ----
        // (truoc day co 3 cho nhu vay - doUserOutNewClan x2, doKickMemberOutNewClan - moi
        // cho la mot lan bo sot tiem nang giong het vu 11/09 va 12/09/2026).
        final String SENTINEL = "timeStartDel == 0L";
        int soLanSentinelNgoaiHamGop = 0;
        int j = noiDung.indexOf(SENTINEL);
        while (j >= 0) {
            final boolean trongHamGop = viTriHamGop >= 0
                    && j >= viTriHamGop && j <= noiDung.indexOf("}", viTriHamGop);
            final boolean trongChuThich = noiDung.lastIndexOf("/**", j) > noiDung.lastIndexOf("*/", j);
            if (!trongHamGop && !trongChuThich) {
                soLanSentinelNgoaiHamGop++;
            }
            j = noiDung.indexOf(SENTINEL, j + SENTINEL.length());
        }
        Kiem.dung("khong con kieu kiem 'timeStartDel == 0L' don le ngoai ham gop/chu thich",
                soLanSentinelNgoaiHamGop == 0);

        // ---- 4. Ca 5 noi da biet phai goi lai ham gop ----
        final String GOI_HAM_GOP = "canDatLaiDemNguocGiaiTan(";
        // Vi tri THAT cua chinh chu "canDatLaiDemNguocGiaiTan(" trong dong khai bao ham
        // (viTriHamGop tro toi dau "private", khong phai dau ten ham) - phai bo qua dung
        // vi tri nay, khong thi dinh nghia ham tu dem nham la mot lan "goi lai".
        final int viTriDinhNghiaGoiHamGop = viTriHamGop >= 0
                ? viTriHamGop + "private static boolean ".length() : -1;
        int soLanGoi = 0;
        int k = noiDung.indexOf(GOI_HAM_GOP);
        while (k >= 0) {
            if (k != viTriDinhNghiaGoiHamGop) {
                soLanGoi++;
            }
            k = noiDung.indexOf(GOI_HAM_GOP, k + GOI_HAM_GOP.length());
        }
        Kiem.dung("canDatLaiDemNguocGiaiTan() duoc goi lai o du 5 noi (doDelNewClan, "
                        + "doUserOutNewClan x2, doKickMemberOutNewClan, thong tin bang)",
                soLanGoi == 5);

        // ---- 5. Xem thong tin bang duoi 10 thanh vien phai TU SUA (khong doi bam "giai tan") ----
        final int viTriIsdel = noiDung.indexOf("boolean isdel = clan.isDel;");
        Kiem.dung("tim thay nhanh tinh isdel trong thong tin bang", viTriIsdel >= 0);
        if (viTriIsdel >= 0) {
            final String sauIsdel = noiDung.substring(viTriIsdel, Math.min(noiDung.length(), viTriIsdel + 800));
            Kiem.dung("nhanh thong tin bang tu goi canDatLaiDemNguocGiaiTan(clan)",
                    sauIsdel.contains("canDatLaiDemNguocGiaiTan(clan)"));
            Kiem.dung("nhanh thong tin bang ghi lai CSDL sau khi tu sua (khong chi sua trong RAM)",
                    sauIsdel.contains("Database.instance.updateTimeCheckDelClan(clan.id"));
        }

        // ---- 6. Nhanh tao bang moi phai goi thang ham lui moc (luon dong hoan toan) ----
        final int viTriTaoBang = noiDung.indexOf("NewClan clan = new NewClan();");
        Kiem.dung("tim thay nhanh tao bang moi (NewClan clan = new NewClan();)", viTriTaoBang >= 0);
        if (viTriTaoBang >= 0) {
            final String sauKhiTao = noiDung.substring(viTriTaoBang, Math.min(noiDung.length(), viTriTaoBang + 900));
            Kiem.dung("tao bang moi goi datTimeStartDelGiaiTan(clan) (khong gan tho truc tiep)",
                    sauKhiTao.contains("datTimeStartDelGiaiTan(clan)"));
            Kiem.sai("tao bang moi KHONG con gan 'clan.timeStartDel = System.currentTimeMillis();' truc tiep",
                    sauKhiTao.contains("clan.timeStartDel = System.currentTimeMillis();"));
        }
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
