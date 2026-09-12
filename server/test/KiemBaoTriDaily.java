import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Canh bao 60s truoc gio bao tri ngau nhien hang ngay cua BaoTriDaily.
 *
 * Truoc day nhanh gio ngau nhien (4:30-5:59) tu thoat KHONG he canh bao nguoi
 * choi - chi duong deploy (vps/deploy-auto.sh tu bat co baotri_thongbao) moi
 * co canh bao. Tu khi deploy-auto.sh chuyen sang "keo code ngay, ap dung tre
 * toi gio bao tri" (xem doc dau vps/deploy-auto.sh), nhanh gio ngau nhien nay
 * PHAI tu canh bao, neu khong nguoi choi mat luon 60 giay chuan bi ho dang
 * duoc huong moi lan deploy hom nay.
 *
 * Khong the chay that vong lap run() (can Database that + 24h dong ho), nen
 * quet van ban nguon de xac nhan cau truc dung, giong cach cac Kiem* khac lam.
 */
public final class KiemBaoTriDaily {

    private KiemBaoTriDaily() {
    }

    public static void chay() {
        Kiem.nhom("Canh bao truoc gio bao tri ngau nhien (BaoTriDaily)");

        final String noiDung = doc("src/real/BaoTriDaily.java");
        Kiem.dung("doc duoc src/real/BaoTriDaily.java", !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        Kiem.dung("co bien warnHour/warnMinute tinh tu gio bao tri",
                noiDung.contains("warnHour") && noiDung.contains("warnMinute"));

        // Phai tinh bang tong so phut trong ngay (co mod 24*60) de khong sai khi
        // maintenanceMinute = 0 (VD bao tri 5:00 -> canh bao phai la 4:59, khong phai 4:-1).
        Kiem.dung("tinh gio canh bao bang tong phut trong ngay (tranh tran khi phut bao tri = 0)",
                noiDung.contains("24 * 60"));

        // Phai goi lai dung ham canh bao da co (baoTruocBaoTri), khong viet lai co che
        // gui tin rieng - tranh hai duong canh bao lech nhau ve noi dung/thoi luong.
        final int viTriRun = noiDung.indexOf("public synchronized void run()");
        Kiem.dung("tim thay ham run()", viTriRun >= 0);
        if (viTriRun >= 0) {
            final String than = noiDung.substring(viTriRun);
            Kiem.dung("nhanh canh bao gio ngau nhien goi lai baoTruocBaoTri(60)",
                    than.contains("warnHour") && than.contains("baoTruocBaoTri(60)"));
        }

        // Phai co co chan gui lap: vong lap chay moi ~1s, khung dung sai <=5s se khop
        // nhieu lan trong cung 1 phut neu khong co co nay.
        Kiem.dung("co bien danh dau da canh bao (chi gui dung 1 lan)",
                noiDung.contains("daBaoTruoc"));
        Kiem.dung("dieu kien canh bao co kiem tra co danh dau truoc khi gui",
                noiDung.contains("!daBaoTruoc"));

        // Khong duoc dung toi nhanh BaoTriTay (co 'restart' tu web admin/LichSuKien) -
        // nhanh do da tu lo canh bao rieng, sua nham se lam sai gio bao tri thu cong.
        Kiem.sai("khong gan daBaoTruoc = true canh nham BaoTriTay",
                noiDung.contains("BaoTriTay = true;\n                    daBaoTruoc = true;"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
