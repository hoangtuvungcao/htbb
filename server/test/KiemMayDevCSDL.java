import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * May nha duoc chay voi db.password de trong, VPS thi tuyet doi khong.
 *
 * VI SAO CAN LOI THOAT NAY. Chot chan khoi dong (xem KiemChotCSDL) tu choi chay khi
 * db.password de trong. Dung tren VPS, nhung may nha chay XAMPP de root khong mat khau,
 * nen chot do khoa luon ca viec chay thu o may minh.
 *
 * VI SAO PHAI TEST KY. Loi thoat nay go mot lop chan cua su co 30/08/2026, nen no phai
 * dong lai fail-closed o MOI huong: thieu khoa, khoa de trong, "0", "true", viet sai chinh
 * ta, hay CSDL nam o may khac - tat ca phai ra KHONG CHO. Va no chi duoc mien DUNG MOT
 * phep kiem (mat khau de trong), chu khong duoc mien phep thu ket noi that.
 *
 * Ba lop bao ve VPS duoc ghim o day de khong ai go di ma khong biet:
 *   1. vps/server.ini.tmpl ghim db.choPhepMatKhauTrong=0 (deploy sinh lai server.ini
 *      tu ban mau nay o moi lan deploy);
 *   2. vps/sinh-server-ini.sh tu choi cai neu gia tri khac 0;
 *   3. chinh ham quyet dinh: thieu khoa la khong cho.
 */
public final class KiemMayDevCSDL {

    private static final String F_TMPL = "vps/server.ini.tmpl";
    private static final String F_VIDU = "vps/server.ini.linux.example";
    private static final String F_SINH = "vps/sinh-server-ini.sh";
    private static final String F_SERVER = "src/server/TeamServer.java";

    /** Dong ghim trong ban mau. Doi mot ky tu la deploy khong con chan duoc nua. */
    private static final String DONG_GHIM = "db.choPhepMatKhauTrong=0";

    public static void chay() {
        Kiem.nhom("May nha chay duoc voi db.password de trong, VPS thi khong");

        // ---- 1. Bang quyet dinh: chi "1" + host noi bo moi mo ----
        Kiem.dung("khoa =1 va CSDL o may noi bo -> cho",
                q("1", "localhost:3306"));
        Kiem.dung("khoa =1 voi 127.0.0.1 -> cho",
                q("1", "127.0.0.1:3306"));
        Kiem.sai("thieu khoa -> KHONG cho (mac dinh phai la nghiem ngat)",
                q(null, "localhost:3306"));
        Kiem.sai("khoa =0 (ban mau VPS) -> KHONG cho",
                q("0", "localhost:3306"));
        Kiem.sai("khoa de trong -> KHONG cho",
                q("", "localhost:3306"));
        Kiem.sai("khoa ='true' -> KHONG cho (chi nhan dung chuoi 1)",
                q("true", "localhost:3306"));
        Kiem.sai("khoa viet sai chinh ta thanh gia tri la -> KHONG cho",
                q("bat", "localhost:3306"));

        // Diem quan trong: du khoa =1, CSDL o may khac thi khong bao gio cho. Mot mat khau
        // trong di ra ngoai mang la chuyen khac han voi mot mat khau trong o localhost.
        Kiem.sai("khoa =1 nhung CSDL o may khac -> KHONG cho",
                q("1", "10.0.0.5:3306"));
        Kiem.sai("khoa =1 nhung CSDL o ten may khac -> KHONG cho",
                q("1", "db.kpah.vn:3306"));

        // ---- 2. Nhan dang host noi bo ----
        Kiem.dung("localhost (khong ghi cong)", server.TeamServer.laHostNoiBo("localhost"));
        Kiem.dung("localhost:3306", server.TeamServer.laHostNoiBo("localhost:3306"));
        Kiem.dung("127.0.0.1:3306", server.TeamServer.laHostNoiBo("127.0.0.1:3306"));
        Kiem.dung("IPv6 co ngoac [::1]:3306", server.TeamServer.laHostNoiBo("[::1]:3306"));
        Kiem.dung("chu hoa LOCALHOST van tinh", server.TeamServer.laHostNoiBo("LOCALHOST:3306"));
        Kiem.sai("may khac trong mang LAN", server.TeamServer.laHostNoiBo("192.168.1.9:3306"));
        Kiem.sai("null", server.TeamServer.laHostNoiBo(null));
        Kiem.sai("chuoi rong", server.TeamServer.laHostNoiBo(""));

        // ---- 3. chanDoanCSDL PHAI van nghiem ngat ----
        // Day la phep giu cho su co 30/08/2026 khong quay lai: loi thoat chi duoc song o
        // kiemTraCSDL, con ham chan doan thi khong biet den no.
        final Properties p = cauHinh("");
        p.setProperty("db.choPhepMatKhauTrong", "1");
        final String lyDo = server.TeamServer.chanDoanCSDL(p);
        Kiem.dung("chanDoanCSDL van bao 'de trong' du khoa =1",
                lyDo != null && lyDo.contains("de trong"));

        // ---- 4. Loi thoat KHONG mien phep thu ket noi that ----
        // Cong 1 tren localhost khong co gi lang nghe -> tu choi ngay, khong treo test.
        final Properties pKhongNoi = cauHinh("");
        pKhongNoi.setProperty("db.host", "127.0.0.1:1");
        pKhongNoi.setProperty("db.choPhepMatKhauTrong", "1");
        final String lyDoNoi = server.TeamServer.thuKetNoiCSDL(pKhongNoi);
        Kiem.dung("duoc mien mat khau trong nhung van phai ket noi duoc that",
                lyDoNoi != null && lyDoNoi.contains("khong ket noi duoc"));

        // ---- 5. Ly do van khong duoc chua mat khau ----
        // Cua vao moi (thuKetNoiCSDL) cung phai giu dung cam ket nay: chuoi tra ve di thang
        // ra game.out, ma cac tep log de quyen -rw-r--r--.
        final String matKhauRieng = "mAtKhAuRiEnG_9x7q";
        final Properties pMk = cauHinh(matKhauRieng);
        pMk.setProperty("db.host", "127.0.0.1:1");
        final String lyDoMk = server.TeamServer.thuKetNoiCSDL(pMk);
        Kiem.sai("ly do cua thuKetNoiCSDL khong he chua mat khau",
                lyDoMk != null && lyDoMk.contains(matKhauRieng));

        // ---- 6. Ba lop bao ve VPS con nguyen ----
        Kiem.dung("ban mau VPS ghim " + DONG_GHIM, coDong(doc(F_TMPL), DONG_GHIM));
        Kiem.dung("file vi du linux cung ghim " + DONG_GHIM, coDong(doc(F_VIDU), DONG_GHIM));

        // Phai bat CHINH BIEU THUC chan, tren dong LENH. Ban dau phep kiem nay chi la
        // contains("db.choPhepMatKhauTrong") tren ca file - xoa han dong chan, dao nguoc no,
        // hay noi long thanh "= 1" thi phep kiem cu van dat, boi ten khoa con nam trong chu
        // thich. Da do that bang cach sabotage tung kieu.
        final String sinh = doc(F_SINH);
        Kiem.dung("bo sinh doc gia tri khoa nay", coDongLenh(sinh, "lay db.choPhepMatKhauTrong", '#'));
        Kiem.dung("bo sinh tu choi cai khi gia tri khac 0",
                coDongLenh(sinh, "[ \"$CHO_TRONG\" = \"0\" ]", '#'));

        // Khoa khai hai lan: Properties lay dong CUOI, nen bo sinh phai doc dong cuoi
        // (tail) VA phai chan han file co khoa trung. Thieu mot trong hai la mo lai ke ho
        // "them mot dong xuong duoi la qua sach moi phep kiem".
        Kiem.dung("bo sinh doc gia tri theo last-wins giong java.util.Properties",
                coDongLenh(sinh, "tail -n 1", '#'));
        Kiem.dung("bo sinh chan file co khoa khai hai lan", coDongLenh(sinh, "uniq -d", '#'));

        // ---- 7. Day noi loi thoat trong kiemTraCSDL con dung ----
        // Khong goi truc tiep kiemTraCSDL duoc (no ket thuc bang System.exit), nen ghim o
        // muc ma nguon nhu KiemChotCSDL dang lam. Hai day noi nay tung KHONG he duoc phu:
        // doi "thuKetNoiCSDL(p)" thanh "null", hay bo dieu kien "".equals(matKhau) (tuc
        // nhan lai ca chuoi mau DOI_MAT_KHAU_DB vao nhanh nhuong bo), ma test van xanh het.
        final String sv = doc(F_SERVER);
        Kiem.dung("nhanh nhuong bo VAN goi thuKetNoiCSDL (khong bo qua phep thu ket noi)",
                coDongLenh(sv, "vuongMac = thuKetNoiCSDL(p);", '\0'));
        Kiem.dung("nhanh nhuong bo chi nhan mat khau RONG (chuoi mau va null di duong chat)",
                coDongLenh(sv, "choTrong && \"\".equals(matKhau)", '\0'));

        // Dang goi PHAI la mot dong p.getProperty("...") nguyen ven, khong thi
        // KiemCauHinhPipeline khong thay khoa nay va bao ban mau co nut bam gia. Va vi phep
        // quet cua no khong bo qua chu thich, o day dem tren DONG LENH de mot chu thich
        // nhac lai dang goi khong the tu thoa man phep kiem.
        Kiem.dung("ma nguon doc khoa dung dang p.getProperty (de pipeline doi chieu duoc)",
                coDongLenh(sv, "p.getProperty(\"db.choPhepMatKhauTrong\")", '\0'));
    }

    /** Goi that ham quyet dinh thuan trong TeamServer. */
    private static boolean q(final String khoa, final String host) {
        return server.TeamServer.quyetDinhChoPhepMatKhauTrong(khoa, host);
    }

    private static Properties cauHinh(final String matKhau) {
        final Properties p = new Properties();
        p.setProperty("db.host", "localhost:3306");
        p.setProperty("db.name", "kpah_db");
        p.setProperty("db.user", "kpah");
        p.setProperty("db.password", matKhau);
        return p;
    }

    /**
     * Tim MOT DONG dung bang chuoi can tim (da trim).
     *
     * Dung equals chu khong dung contains: mot dong bi bien thanh chu thich
     * ("# db.choPhepMatKhauTrong=0") van chua chuoi do, nhung khong con tac dung gi.
     */
    private static boolean coDong(final String noiDung, final String dongCanTim) {
        for (final String dongThuc : noiDung.split("\n")) {
            if (dongThuc.trim().equals(dongCanTim)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tim chuoi trong MA LENH, bo qua moi dong chu thich.
     *
     * Phai co ham nay vi contains() tren ca tep bat nham chinh cac dong chu thich giai
     * thich ve no - da dinh that o day: phep kiem "bo sinh tu choi khi khac 0" ban dau chi
     * quet ten khoa tren ca file, nen xoa han dong chan van dat vi ten khoa con trong chu
     * thich.
     *
     * @param kyChuThich '#' cho tep sh, '\0' cho tep .java (bo // va than javadoc)
     */
    private static boolean coDongLenh(final String noiDung, final String chuoi, final char kyChuThich) {
        for (final String dongThuc : noiDung.split("\n")) {
            final String d = dongThuc.trim();
            if (d.isEmpty()) {
                continue;
            }
            if (kyChuThich == '\0') {
                if (d.startsWith("//") || d.startsWith("/*") || d.startsWith("*")) {
                    continue;
                }
            } else if (d.charAt(0) == kyChuThich) {
                continue;
            }
            if (d.contains(chuoi)) {
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

    private KiemMayDevCSDL() {
    }
}
