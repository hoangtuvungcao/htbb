import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Duong dan thu muc anh trong DataGame phai TON TAI THAT.
 *
 * VI SAO CAN. Ngay 02/09/2026 phat hien DataGame doc "map/imgEff/" trong khi thu muc that
 * la "map/imgEffs/" - lech dung MOT chu cai. Vi try/catch cua vong nap nam NGOAI vong lap,
 * file thieu dau tien (j=0) lam dung ca vong, nen CA 63 o cua imgEffectSkill deu null.
 *
 * Trieu chung khong he giong nguyen nhan: moi yeu cau anh hieu ung skill (id 9000..9062)
 * nem NullPointerException tai `m.dos.write(imgEffectSkill[...])`, ma dong do chay TRUOC
 * `s.sendMessage(m)` - nen client khong nhan duoc phan hoi NAO ca roi ngoi cho mai. Nhin
 * tu phia nguoi choi la "tu nhien mat ket noi", khong ai nghi tai mot chu "s".
 *
 * Va vi ten thu muc sai thi sai o MOI may, loi nay co tren ca VPS chu khong rieng may nha.
 *
 * Phep kiem doc THANG duong dan trong ma nguon roi kiem thu muc do co that khong - nen doi
 * ten thu muc hay sua ma nguon lech nhau la do ngay, khong cho tan luc nguoi choi bao loi.
 */
public final class KiemAnhHieuUng {

    private static final String F_DATA_GAME = "src/data/DataGame.java";

    /** new FileInputStream("map/xxx/" + j + ".png") - lay phan "map/xxx/". */
    private static final Pattern DUONG_GHEP = Pattern.compile(
            "new FileInputStream\\(\"(map/[A-Za-z0-9_]+)/\"\\s*\\+");

    public static void chay() {
        Kiem.nhom("Duong dan thu muc anh trong DataGame phai ton tai that");

        final String ma = doc(F_DATA_GAME);
        Kiem.dung("doc duoc " + F_DATA_GAME, ma.length() > 1000);
        if (ma.isEmpty()) {
            return;
        }

        // ---- 1. Moi thu muc ma nguon ghep duong dan toi deu phai ton tai ----
        int soDuong = 0;
        final Matcher m = DUONG_GHEP.matcher(ma);
        while (m.find()) {
            final String thuMuc = m.group(1);
            soDuong++;
            Kiem.dung("thu muc " + thuMuc + " ton tai that", new File(thuMuc).isDirectory());
        }
        Kiem.dung("tim duoc cac duong dan thu muc anh trong ma nguon (tren 2)", soDuong > 2);

        // ---- 2. Rieng anh hieu ung skill: phai du 63 file ----
        // nMonster = 63 trong DataGame, va sendByteImg map id 9000..9062 vao mang nay.
        // Thieu file nao la id tuong ung nem NPE luc phuc vu.
        final File imgEffs = new File("map/imgEffs");
        Kiem.dung("map/imgEffs ton tai (KHONG phai map/imgEff - tung lech mot chu 's')",
                imgEffs.isDirectory());
        if (imgEffs.isDirectory()) {
            int du = 0;
            for (int j = 0; j < 63; j++) {
                if (new File(imgEffs, j + ".png").isFile()) {
                    du++;
                }
            }
            Kiem.bang("co du 63 anh hieu ung skill (0.png..62.png)",
                    Integer.valueOf(63), Integer.valueOf(du));
        }

        // ---- 3. Ma nguon phai doc DUNG thu muc do ----
        // Kiem tren DONG LENH, bo qua chu thich: chinh chu thich giai thich bug nay co
        // viet lai chuoi "map/imgEff/", nen contains() tren ca tep bat nham ngay vao no.
        Kiem.dung("DataGame doc map/imgEffs/ (khong phai map/imgEff/)",
                coTrongMaLenh(ma, "\"map/imgEffs/\""));
        Kiem.sai("KHONG con doc map/imgEff/ (ten cu bi thieu chu 's')",
                coTrongMaLenh(ma, "\"map/imgEff/\""));

        // ---- 4. try phai NAM TRONG vong lap nap anh ----
        // Neu nam ngoai thi mot file thieu keo theo TAT CA cac anh sau no - dung cai bay
        // da lam ca 63 o thanh null. Kiem bang cach: trong khoi nap imgEffectSkill, dong
        // "for (" phai xuat hien TRUOC dong "try {" dau tien sau no.
        final int viTriMang = ma.indexOf("imgEffectSkill = new byte[nMonster][]");
        Kiem.dung("tim thay khoi nap imgEffectSkill", viTriMang > 0);
        if (viTriMang > 0) {
            final int viTriFor = ma.indexOf("for (j = 0; j < nMonster; ++j)", viTriMang);
            final int viTriTry = ma.indexOf("try {", viTriMang);
            Kiem.dung("khoi nap co vong for", viTriFor > 0);
            Kiem.dung("try nam TRONG vong for (khong phai bao ca vong)",
                    viTriFor > 0 && viTriTry > viTriFor);
        }
    }

    /**
     * Tim chuoi trong MA LENH, bo qua moi dong chu thich Java.
     *
     * Bat buoc phai co: chu thich giai thich bug "thieu chu s" co viet lai ca hai duong
     * dan, nen contains() tren ca tep se bat nham chinh loi giai thich do va bao dong gia.
     */
    private static boolean coTrongMaLenh(final String noiDung, final String chuoi) {
        for (final String dongThuc : noiDung.split("\n")) {
            final String d = dongThuc.trim();
            if (d.isEmpty() || d.startsWith("//") || d.startsWith("/*") || d.startsWith("*")) {
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

    private KiemAnhHieuUng() {
    }
}
