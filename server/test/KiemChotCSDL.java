import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Chot chan khoi dong khi khong vao duoc CSDL, va hai thu chong lai chinh su co 30/08/2026.
 *
 * CHUYEN DA XAY RA: server.ini bi chep de bang vps/server.ini.linux.example, nen mat khau
 * CSDL thanh chuoi mau "DOI_MAT_KHAU_DB". Hau qua khong he giong nguyen nhan:
 *   mat khau sai -> ConnPool rong -> moi getConnection() nem NoSuchElementException
 *   -> server VAN CHAY TIEP -> Map.loadAllTemplate doc Vector rong
 *   -> ArrayIndexOutOfBoundsException trong luong "main"
 *   -> main chet TRUOC KHI mo socket, nhung luong non-daemon khac giu JVM song
 *   -> systemd bao "active (running)" trong khi khong cong nao lang nghe.
 * Nhin systemctl status thay binh thuong, nen mat rat lau moi lan ra nguyen nhan.
 *
 * Chot moi lam server chet ngay tai cho voi mot dong noi ro ly do. Test o day giu ba thu:
 *   1. chan doan tra dung ly do (goi ham that, khong doi chieu chu trong ma nguon);
 *   2. ly do KHONG BAO GIO chua mat khau - chuoi nay di thang ra log, ma log de quyen doc chung;
 *   3. chot phai dung TRUOC moi lenh cham CSDL, khong thi no vo nghia.
 */
public final class KiemChotCSDL {

    private static final String F_SERVER = "src/server/TeamServer.java";
    private static final String F_UNIT = "../vps/htbb-game.service";
    private static final String F_BACKUP = "../vps/backup_db.sh";

    public static void chay() {
        Kiem.nhom("Chot chan khoi dong khi khong vao duoc CSDL");

        // ---- 1. Chan doan tra dung ly do ----
        Kiem.dung("mat khau de trong -> bao 'de trong'",
                chuaChu(chanDoan("", "khong-dung"), "de trong"));
        Kiem.dung("mat khau con la chuoi mau -> bao dung ten chuoi mau",
                chuaChu(chanDoan("DOI_MAT_KHAU_DB", "khong-dung"), "DOI_MAT_KHAU_DB"));
        Kiem.dung("chuoi mau -> co nhac toi file example de biet duong ma sua",
                chuaChu(chanDoan("DOI_MAT_KHAU_DB", "khong-dung"), "server.ini.linux.example"));

        // Cong 1 tren localhost khong co gi lang nghe -> tu choi ngay, khong treo test.
        final String khongNoiDuoc = chanDoan("mat-khau-nao-do", "127.0.0.1:1");
        Kiem.dung("noi khong duoc toi CSDL -> bao 'khong ket noi duoc'",
                chuaChu(khongNoiDuoc, "khong ket noi duoc"));

        // ---- 2. Ly do KHONG duoc chua mat khau ----
        // Day la phep quan trong nhat trong tep nay. Chuoi tra ve duoc in thang ra
        // /opt/htbb/log/game.out, ma cac tep log dang de quyen -rw-r--r-- (ai cung doc duoc).
        final String matKhauRieng = "mAtKhAuRiEnG_9x7q";
        for (final String host : new String[] {"127.0.0.1:1", "localhost:3306"}) {
            final String lyDo = chanDoan(matKhauRieng, host);
            Kiem.sai("ly do (host " + host + ") khong he chua mat khau",
                    lyDo != null && lyDo.contains(matKhauRieng));
        }

        // ---- 3. Chot phai chay TRUOC moi lenh cham CSDL ----
        final String sv = doc(F_SERVER);
        Kiem.dung("doc duoc " + F_SERVER, sv.length() > 500);
        final int viTriChot = sv.indexOf("kiemTraCSDL(p);");
        final int viTriDungCSDL = sv.indexOf("Database.instance.createTableTrainLuong();");
        Kiem.dung("co goi chot kiemTraCSDL(p)", viTriChot > 0);
        Kiem.dung("chot dung TRUOC lenh cham CSDL dau tien",
                viTriChot > 0 && viTriDungCSDL > 0 && viTriChot < viTriDungCSDL);
        Kiem.dung("chot ket thuc bang System.exit de systemd bao 'failed' dung su that",
                coTrongMaLenh(sv, "System.exit(1)", true));

        // ---- 4. systemd phai thoi khoi dong lai vo han ----
        // Restart=always + RestartSec=10 ma khong co gioi han thi server tu thoat se bi bat
        // day lai mai mai moi 10 giay, va unit khong bao gio vao trang thai "failed".
        final String unit = doc(F_UNIT);
        Kiem.dung("doc duoc " + F_UNIT, unit.length() > 100);
        Kiem.dung("unit co Restart=always (neu bo thi gioi han duoi khong con y nghia)",
                coTrongMaLenh(unit, "Restart=always", false));
        Kiem.dung("unit co StartLimitBurst", coTrongMaLenh(unit, "StartLimitBurst=", false));
        Kiem.dung("unit co StartLimitIntervalSec",
                coTrongMaLenh(unit, "StartLimitIntervalSec=", false));

        // ---- 5. Script backup: pipefail va thu tu xoa ----
        // Loi cu: 'mysqldump | gzip > f' khong co pipefail thi mysqldump chet van cho ra .gz
        // hop le va ma thoat lay tu gzip = 0 -> script bao xong roi xoa cac ban cu hon 7 ngay.
        final String bk = doc(F_BACKUP);
        Kiem.dung("doc duoc " + F_BACKUP, bk.length() > 200);
        Kiem.dung("backup co bat pipefail (lenh that, khong tinh dong chu thich)",
                coTrongMaLenh(bk, "pipefail", false));
        Kiem.dung("backup kiem tra dump co chay het (Dump completed)",
                coTrongMaLenh(bk, "Dump completed", false));
        final int viTriKiem = bk.indexOf("case \"$CUOI\"");
        final int viTriXoa = bk.indexOf("-delete");
        Kiem.dung("chi xoa ban cu SAU KHI da kiem tra ban moi",
                viTriKiem > 0 && viTriXoa > 0 && viTriKiem < viTriXoa);
        Kiem.dung("huong dan cron tro dung duong dan sau khi deploy (/opt/htbb/vps/)",
                bk.contains("/opt/htbb/vps/backup_db.sh"));
    }

    /** Goi that server.TeamServer.chanDoanCSDL voi mot cau hinh dung san. */
    private static String chanDoan(final String matKhau, final String host) {
        final Properties p = new Properties();
        p.setProperty("db.host", host);
        p.setProperty("db.name", "htbb_db");
        p.setProperty("db.user", "htbb");
        p.setProperty("db.password", matKhau);
        return server.TeamServer.chanDoanCSDL(p);
    }

    private static boolean chuaChu(final String lyDo, final String chu) {
        return lyDo != null && lyDo.contains(chu);
    }

    /**
     * Tim chuoi trong MA LENH, bo qua moi dong chu thich.
     *
     * Phai co ham nay vi contains() tren ca tep bat nham chinh cac dong chu thich giai thich
     * ve no. Da dinh that: go han 'set -euo pipefail' khoi backup_db.sh ma phep kiem van dat,
     * boi vi con mot dong chu thich nhac chu "pipefail". Cung the, "System.exit(1)" xuat hien
     * trong javadoc cua TeamServer, nen xoa het lenh exit that thi phep kiem cu van khong biet.
     *
     * @param kieuJava true = tep .java (bo // va cac dong than javadoc), false = tep sh/systemd (bo #)
     */
    private static boolean coTrongMaLenh(final String noiDung, final String chuoi, final boolean kieuJava) {
        for (final String dongThuc : noiDung.split("\n")) {
            final String d = dongThuc.trim();
            if (d.isEmpty()) {
                continue;
            }
            if (kieuJava) {
                if (d.startsWith("//") || d.startsWith("/*") || d.startsWith("*")) {
                    continue;
                }
            } else if (d.startsWith("#")) {
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

    private KiemChotCSDL() {
    }
}
