import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Giam rac trong log game.out. vps/kpah-logrotate.conf da lo xoay vong theo
 * dung luong/thoi gian - van de la NOI DUNG: mot so cho in day du stack trace
 * cho loi RAT thuong xuyen (vd "broken pipe" khi client ngat ket noi, xay ra
 * o MOI lan logout/doi map) hoac lap lai VO HAN moi 300ms trong vong lap
 * "RUN MAP" chay mai cho tung map.
 *
 * Sua: cac cho gui du lieu socket (io/Session.java) chi in 1 dong thay vi ca
 * stack trace. Ba vong lap "RUN MAP" (real/Map.java x2, real/MapVongLap.java)
 * chi bao loi 1 LAN CHO CA SERVER (co/khong co that su xay ra thi cung khong
 * lam gi, nhung neu xay ra thi khong con lap lai vo han) - giong dung idiom
 * GhiSoOnline.baoLoiMotLan da co san trong code.
 *
 * Khong the chay that cac vong lap/socket nay trong test (can server that:
 * ket noi mang, thread map...) nen quet van ban nguon de xac nhan cau truc
 * sua dung, giong cach cac Kiem* khac lam voi RealController/Map.
 */
public final class KiemDonLog {

    private KiemDonLog() {
    }

    public static void chay() {
        Kiem.nhom("Don log - giam rac (khong con full stack trace/lap vo han cho loi thuong gap)");

        kiemSession();
        kiemMapVaMapVongLap();
    }

    private static void kiemSession() {
        final String noiDung = doc("src/io/Session.java");
        Kiem.dung("doc duoc src/io/Session.java", !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        // Ba cho gui du lieu (sendMessage cong khai, sendMessageLogin, Sender.run)
        // KHONG con in ca stack trace cho loi gui du lieu (thuong la do client
        // da ngat ket noi) - chi con 1 dong moi cho.
        Kiem.dung("Session.sendMessage (cong khai) chi in 1 dong, khong con in ca stack trace",
                noiDung.contains("Session.sendMessage: loi gui du lieu"));
        Kiem.dung("Sender.sendMessageLogin chi in 1 dong, khong con in ca stack trace",
                noiDung.contains("Session.sendMessageLogin: loi gui du lieu"));
        Kiem.dung("Sender.run (vong gui chinh) chi in 1 dong, khong con in ca stack trace",
                noiDung.contains("Session.Sender.run: loi gui du lieu"));

        final int viTriRun = noiDung.indexOf("public void run()");
        Kiem.dung("Sender.run() van giu logic 'if (os == null) break' sau khi doi log (khong dong toi dieu khien luong)",
                viTriRun >= 0 && noiDung.substring(viTriRun).contains("if (os == null) {")
                        && noiDung.substring(viTriRun).contains("break;"));
    }

    private static void kiemMapVaMapVongLap() {
        final String map = doc("src/real/Map.java");
        Kiem.dung("doc duoc src/real/Map.java", !map.isEmpty());
        if (!map.isEmpty()) {
            Kiem.dung("khai bao co field daBaoLoiRunMapWind (bao loi 1 lan cho ca server)",
                    map.contains("daBaoLoiRunMapWind"));
            Kiem.dung("khai bao co field daBaoLoiRunMapUpdate (bao loi 1 lan cho ca server)",
                    map.contains("daBaoLoiRunMapUpdate"));
            Kiem.dung("KHONG con nhanh nao in TRUNG (ca printStackTrace lan println) khong dieu kien cho loi RUN MAP",
                    !map.contains("var5.printStackTrace();\n                            System.out.println(\"LOI TRONG HAM RUN MAP \" + var5.toString());")
                 && !map.contains("var5.printStackTrace();\n                    System.out.println(\"LOI TRONG HAM RUN MAP \" + var5.toString());"));
        }

        final String mapVongLap = doc("src/real/MapVongLap.java");
        Kiem.dung("doc duoc src/real/MapVongLap.java", !mapVongLap.isEmpty());
        if (!mapVongLap.isEmpty()) {
            Kiem.dung("khai bao co field daBaoLoiRunMapSand (bao loi 1 lan cho ca server)",
                    mapVongLap.contains("daBaoLoiRunMapSand"));
            Kiem.dung("KHONG con in TRUNG khong dieu kien cho loi RUN MAP (updateSand)",
                    !mapVongLap.contains("var5.printStackTrace();\n                        System.out.println(\"LOI TRONG HAM RUN MAP \" + var5.toString());"));
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
