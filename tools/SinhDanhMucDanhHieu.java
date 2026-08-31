import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Sinh web/admin/inc/danh_muc_danh_hieu.php tu chinh ham data.DanhHieu.getName().
 *
 * VI SAO PHAI SINH CHU KHONG DOC BANG REGEX. Ten danh hieu nam trong mot khoi
 * switch-case cua ham Java, KHONG co trong CSDL nao. Doc bang regex tren tep
 * .java se de bat nham cac nhanh case-fallthrough (8780..8784 roi thang xuong
 * "default: return \"\";" - nam GIUA hai nhanh co ten that) va se khong chay
 * dung ham NHU MAY CHU CHAY - chi co goi that ham Java moi chac chan dung.
 *
 * HAI TAP ID KHAC NHAU, PHAI GHI RO TRONG BO CHON:
 *
 *   ID_NGAU_NHIEN (34 id) - khop CHINH XAC mang int[] availableTitles ghi cung
 *   trong DatabaseGiftCode.java (nhanh xu ly "DH", id=0 -> chon ngau nhien MOT
 *   trong so nay). Day la tap CO THE ra duoc bang DH:0:...
 *
 *   Cac id CON LAI (785, 8983..8996) co ten that qua getName() nhung KHONG nam
 *   trong availableTitles - "ngau nhien" khong bao gio ra duoc chung, PHAI ghi
 *   id truc tiep (DH:<id>:...). Chon nham thanh "ngau nhien" cho mot danh hieu
 *   trong nhom nay la bo chon noi doi - trong nhu chon duoc ma khong bao gio ra.
 *
 * Id 8780..8784 (rong) va 8999 ("TEST" - danh hieu thu nghiem, khong phai qua
 * that) deu BI LOAI, khong dua vao danh muc.
 *
 * Cach dung (sau khi da bien dich src/ vao build/classes):
 *   java -cp "build/classes;lib/NQSH_5h.jar;lib/mysql-connector-java-5.1.49.jar" SinhDanhMucDanhHieu
 *
 * Chay lai moi khi them/doi danh hieu trong DanhHieu.java. test/KiemDanhMucDanhHieu
 * doi chieu lai lan nua - CA hai tap id lan noi dung ten - nen quen chay thi bo
 * kiem se do, khong am tham lech.
 */
public final class SinhDanhMucDanhHieu {

    private static final String TEP = "web/admin/inc/danh_muc_danh_hieu.php";

    /**
     * CHEP Y HET mang int[] availableTitles trong nhanh "DH" cua
     * src/data/DatabaseGiftCode.java. Day la mot ban sao CO CHU DICH: ghi hai noi
     * de test/KiemDanhMucDanhHieu.java doi chieu duoc tung so mot voi ban that
     * trong DatabaseGiftCode.java, chu khong phai vi khong the doc tu do truc
     * tiep - noi dung nhanh do khong doc duoc tu ben ngoai bang phan xa Java.
     */
    private static final int[] ID_NGAU_NHIEN = {
        8767, 8768, 8769, 8770, 8771, 8772, 8773, 8774, 8775, 8776, 8777,
        8778, 8779, 8785, 8786, 8787, 8788, 8900, 8901, 8902, 8903, 8904,
        8905, 8906, 8907, 8908, 8909, 8910, 8911, 8924, 8925, 8935, 8956, 8957
    };

    /**
     * Sieu tap id de do qua data.DanhHieu.getName(). Gom ca ID_NGAU_NHIEN lan
     * moi id khac tung thay xuat hien trong ma nguon (785 that ra la 8785, da
     * co trong ID_NGAU_NHIEN; day la dai 8983..8996 - danh hieu MOI hon, chua
     * duoc them vao mang ngau nhien). Rong / "TEST" bi loc o vong lap ben duoi
     * bang chinh ket qua getName(), khong can liet ke rieng o day.
     */
    private static int[] taoSieuTapId() {
        final java.util.TreeSet<Integer> tap = new java.util.TreeSet<Integer>();
        for (final int id : ID_NGAU_NHIEN) {
            tap.add(id);
        }
        for (int id = 8983; id <= 8999; id++) {
            tap.add(id);
        }
        final int[] ra = new int[tap.size()];
        int i = 0;
        for (final int id : tap) {
            ra[i++] = id;
        }
        return ra;
    }

    public static void main(final String[] thamSo) throws Exception {
        final java.util.Set<Integer> ngauNhien = new java.util.HashSet<Integer>();
        for (final int id : ID_NGAU_NHIEN) {
            ngauNhien.add(id);
        }

        final StringBuilder s = new StringBuilder();
        s.append("<?php\n");
        s.append("/**\n");
        s.append(" * DANH MUC DANH HIEU cho o chon phan thuong giftcode (loai DH).\n");
        s.append(" *\n");
        s.append(" * TEP NAY DUOC SINH RA, DUNG SUA TAY.\n");
        s.append(" *   Nguon   : data.DanhHieu.getName() (src/data/DanhHieu.java)\n");
        s.append(" *   Sinh lai: java -cp \"build/classes;lib/NQSH_5h.jar;");
        s.append("lib/mysql-connector-java-5.1.49.jar\" SinhDanhMucDanhHieu\n");
        s.append(" *\n");
        s.append(" * `ngauNhien` = true CHI cho dung 34 id trung khop mang availableTitles ghi\n");
        s.append(" * cung trong src/data/DatabaseGiftCode.java (nhanh xu ly loai DH, id=0 -> chon\n");
        s.append(" * ngau nhien MOT trong so nay). Danh hieu nao ngauNhien=false thi PHAI ghi id\n");
        s.append(" * that (DH:<id>:...), \"ngau nhien\" khong bao gio ra duoc no.\n");
        s.append(" *\n");
        s.append(" * test/KiemDanhMucDanhHieu.java doi chieu tep nay voi getName() that va voi\n");
        s.append(" * mang availableTitles that, nen tep lech la bo kiem do chu khong am tham sai.\n");
        s.append(" */\n\n");
        s.append("/** @return array<int,array{ten:string,ngauNhien:bool}> */\n");
        s.append("function danhMucDanhHieu(): array\n");
        s.append("{\n");
        s.append("    return [\n");

        int soDong = 0;
        int soNgauNhien = 0;
        for (final int id : taoSieuTapId()) {
            final String ten = data.DanhHieu.getName(id);
            // getName() tra "" cho id rong (8780..8784) va cho id khong biet - loai
            // het, khong dua vao danh muc. "TEST" (8999) cung loai rieng: la danh
            // hieu thu nghiem, khong phai qua that.
            if (ten == null || ten.isEmpty() || "TEST".equals(ten)) {
                continue;
            }
            final boolean laNgauNhien = ngauNhien.contains(Integer.valueOf(id));
            s.append("        ").append(id).append(" => ['ten' => '").append(thoatChuoiPhp(ten))
             .append("', 'ngauNhien' => ").append(laNgauNhien ? "true" : "false").append("],\n");
            soDong++;
            if (laNgauNhien) {
                soNgauNhien++;
            }
        }

        s.append("    ];\n");
        s.append("}\n");

        try (Writer w = new OutputStreamWriter(new FileOutputStream(TEP), StandardCharsets.UTF_8)) {
            w.write(s.toString());
        }
        System.out.println("Da sinh " + TEP + " voi " + soDong + " danh hieu ("
                + soNgauNhien + " trong so ngau nhien).");
        if (soNgauNhien != ID_NGAU_NHIEN.length) {
            System.out.println("[CANH BAO] ID_NGAU_NHIEN co " + ID_NGAU_NHIEN.length
                    + " id nhung chi " + soNgauNhien + " id ra duoc ten - kiem lai getName().");
        }
        System.exit(0);
    }

    private static String thoatChuoiPhp(final String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }

    private SinhDanhMucDanhHieu() {
    }
}
