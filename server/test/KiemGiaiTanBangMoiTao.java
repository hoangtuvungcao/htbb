import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Bang MOI TAO phai dem nguoc "giai tan" theo PHUT_CHO_GIAI_TAN (60 phut), khong phai
 * PHUT_GIAI_TAN_GOC_TRONG_JAR (4320 phut = 3 ngay) tho cua NewClan.timeDel() trong jar.
 *
 * VI SAO CAN. 11/09/2026 da vet loi nay o cac nhanh ROI/DUOI thanh vien (removeMember()
 * trong lib/NQSH_5h.jar tu dat lai timeStartDel THO khi cat qua duoi 10 nguoi) - sua bang
 * cach goi datTimeStartDelGiaiTan() thay vi gan truc tiep System.currentTimeMillis(). Nhung
 * ban vetsua do BO SOT chinh nhanh TAO BANG MOI (bang moi luon co 1 thanh vien, duoi 10 ngay
 * tu dau) - van con dong "clan.timeStartDel = System.currentTimeMillis();" tho, nen bang
 * vua tao van hien "con ~4300 phut nua se giai tan" thay vi ~60 phut (bao 12/09/2026, anh
 * chup man hinh bang "TEST" 1 thanh vien).
 *
 * De KHONG con cho thu ba bi bo sot kieu nay, test nay quet TOAN BO file va doi hoi: moi
 * lan gan `timeStartDel = System.currentTimeMillis()` (gan THO, chua qua ham lui moc) chi
 * duoc phep xay ra DUY NHAT o BEN TRONG dinh nghia cua chinh datTimeStartDelGiaiTan() -
 * moi noi khac trong file PHAI goi lai ham do.
 */
public final class KiemGiaiTanBangMoiTao {

    private KiemGiaiTanBangMoiTao() {
    }

    public static void chay() {
        Kiem.nhom("Giai tan bang - bang moi tao phai dem 60 phut, khong phai 3 ngay tho");

        final String noiDung = doc("src/real/MapBangHoi.java");
        Kiem.dung("doc duoc src/real/MapBangHoi.java", !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        // Dau "." o dau de KHONG khop voi doan van ta bang loi trong chu thich JavaDoc cua
        // chinh datTimeStartDelGiaiTan() (viet "`timeStartDel = System.currentTimeMillis()`"
        // KHONG co doi tuong dung truoc) - ma nguon THAT luon co doi tuong.truong (clanInfo.,
        // clan., clanx., ...).
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

        // Nhanh tao bang moi phai goi lai ham lui moc thay vi gan tho.
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
