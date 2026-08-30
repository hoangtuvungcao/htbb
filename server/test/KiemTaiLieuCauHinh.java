import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Chot lai cac khang dinh trong docs/cau-hinh-server-ini.md voi ma nguon.
 *
 * VI SAO MOT TAI LIEU LAI CAN TEST: tai lieu nay noi cho nguoi van hanh biet khoa nao doi
 * duoc khi may chu DANG CHAY va khoa nao bat buoc khoi dong lai. Sai mot dong o day khong
 * phai la "tai lieu hoi cu" — no dan toi viec sua ti le rot do tren server that, go lenh
 * nap lai, thay khong bao loi, roi tuong da an trong khi thuc te khong doi gi. Loai sai do
 * khong ai phat hien bang mat.
 *
 * Ranh gioi that nam trong ma nguon:
 *   - Khoa xuat hien trong server/CauHinh.java  -> CauHinh.napLai() doc lai duoc.
 *   - Khoa chi co trong server/TeamServer.java  -> chi doc mot lan luc bat may.
 *   - Khoa di qua parseInt/parseByte/parseShort -> thieu la nem loi, va vi ca khoi nam
 *     trong MOT try nen moi dong ben duoi bi bo qua trong khi server van khoi dong.
 *
 * Test khong doc thuoc danh sach: no TRICH danh sach tu tai lieu roi doi chieu voi ma
 * nguon. Them mot khoa vao tai lieu ma ma nguon khong co, hoac nguoc lai, deu bi bat.
 */
public final class KiemTaiLieuCauHinh {

    private static final String F_TAI_LIEU = "docs/cau-hinh-server-ini.md";
    private static final String F_CAU_HINH = "src/server/CauHinh.java";
    private static final String F_SERVER = "src/server/TeamServer.java";

    public static void chay() {
        Kiem.nhom("Tai lieu cau hinh - khop voi ma nguon");

        final String tl = doc(F_TAI_LIEU);
        final String ch = doc(F_CAU_HINH);
        final String sv = doc(F_SERVER);
        Kiem.dung("doc duoc " + F_TAI_LIEU, tl.length() > 500);
        if (tl.isEmpty() || ch.isEmpty() || sv.isEmpty()) {
            return;
        }

        // ---- Khoa tai lieu bao la NAP LAI DUOC thi phai co trong CauHinh ----
        final String[] napLaiDuoc = {
            "sv.tileXu", "sv.tileLuong", "sv.tileItemSuKien", "sv.tilePotion",
            "sv.tileNgoc", "sv.tileItem", "sv.chiaXuRoi",
            "sv.gioChiemThanh", "sv.phutChiemThanh", "sv.ngayChiemThanh",
            "sv.adminAccounts", "sv.maxCharPerAccount", "sv.choPhepTaoTenGM",
            "sv.website", "sv.linkTaiGame", "sv.testMode", "sv.testCode",
            "sv.worldCupBatDau", "sv.worldCupKetThuc", "sv.worldCupHanNhanQua",
        };
        Kiem.bang("tai lieu ghi dung 20 khoa nap lai duoc",
                Integer.valueOf(20), Integer.valueOf(napLaiDuoc.length));
        for (final String k : napLaiDuoc) {
            Kiem.dung("tai lieu co liet ke " + k, tl.contains(k));
            Kiem.dung(k + " that su nap lai duoc (co trong CauHinh)", ch.contains("\"" + k + "\""));
        }

        // ---- va nguoc lai: CauHinh khong duoc co khoa nao tai lieu bo quen ----
        for (final String k : timKhoa(ch)) {
            boolean coGhi = false;
            for (final String d : napLaiDuoc) {
                coGhi = coGhi || d.equals(k);
            }
            Kiem.dung("khoa nap lai duoc " + k + " co trong tai lieu", coGhi);
        }

        // ---- Khoa tai lieu bao la BAT BUOC thi phai that su di qua parseX ----
        // Thieu chung se nem loi va lam bo qua moi dong ben duoi trong cung khoi try.
        final String[] batBuoc = {
            "api.sv", "sv.port", "sv.server", "sv.portnap", "sv.portlogin",
            "limit.ccu", "sv.LG", "sv.range", "sv.lang", "sv.me",
            "sv.createRace", "sv.expvantieu", "sv.Luckyexp", "sv.ExpQua", "sv.CodeTest",
            "sv.EnableAvtiveCheck", "db.hour", "db.minute",
            "sv.isSuKienTrungThul2016", "sv.isSuKienTet2017", "sv.isSuKienGioTo2016",
            "sv.isSuKienNoel2023", "sv.isSuKienTetduonglich2024",
        };
        Kiem.bang("tai lieu ghi dung 23 khoa bat buoc",
                Integer.valueOf(23), Integer.valueOf(batBuoc.length));
        for (final String k : batBuoc) {
            Kiem.dung("tai lieu co liet ke " + k + " la bat buoc", tl.contains(k));
            Kiem.dung(k + " that su di qua parseX (thieu la nem loi)", coParse(sv, k));
        }

        // ---- Khoa nap lai duoc thi KHONG duoc nam trong nhom bat buoc ----
        // Chung deu co gia tri mac dinh; xep nham vao nhom bat buoc se khien nguoi van hanh
        // so khong dam xoa dong khong dung.
        for (final String k : napLaiDuoc) {
            Kiem.sai(k + " khong phai khoa bat buoc", coParse(sv, k));
        }

        // ---- sv.isSuKien83 van la khoa chet ----
        // Tai lieu bao doi no khong co tac dung. Neu mai nay ai do noi day no vao ma nguon
        // ma quen sua tai lieu thi cau do thanh noi doi.
        Kiem.dung("tai lieu co canh bao sv.isSuKien83 la khoa chet",
                tl.contains("sv.isSuKien83"));
        Kiem.sai("sv.isSuKien83 van khong duoc doc o dau",
                sv.contains("\"sv.isSuKien83\"") || ch.contains("\"sv.isSuKien83\""));

        // ---- Duong dan va cai bay chinh phai con dung ----
        Kiem.dung("tai lieu chi dung thu muc chay tren VPS",
                tl.contains("/opt/kpah/server.ini"));
        Kiem.dung("thu muc do khop voi kpah-game.service",
                doc("vps/kpah-game.service").contains("WorkingDirectory=/opt/kpah"));
        Kiem.dung("tai lieu neu dong log bao loi doc cau hinh",
                tl.contains("Load configuration file error!"));
        Kiem.dung("dong log do van con trong ma nguon",
                sv.contains("Load configuration file error!"));
        Kiem.dung("tai lieu nhac server.ini bi gitignore",
                tl.contains(".gitignore"));
        Kiem.dung("server.ini van thuc su bi gitignore",
                doc(".gitignore").contains("server.ini"));
        Kiem.dung("tai lieu co ghi khoa chan doan thu cuoi",
                tl.contains("sv.thuNghiemThuCuoi"));
        Kiem.dung("khoa chan doan do van con duoc doc",
                sv.contains("\"sv.thuNghiemThuCuoi\""));
    }

    /** Cac khoa cau hinh xuat hien trong mot tep ma nguon, dang "sv.xxx" hoac "db.xxx". */
    private static java.util.List<String> timKhoa(final String ma) {
        final java.util.List<String> ra = new java.util.ArrayList<String>();
        final java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"((?:sv|db|limit|api)\\.[A-Za-z0-9_]+)\"").matcher(ma);
        while (m.find()) {
            if (!ra.contains(m.group(1))) {
                ra.add(m.group(1));
            }
        }
        return ra;
    }

    /** Khoa nay co di qua parseInt / parseByte / parseShort khong. */
    private static boolean coParse(final String ma, final String khoa) {
        return ma.contains("Integer.parseInt(p.getProperty(\"" + khoa + "\"))")
                || ma.contains("Byte.parseByte(p.getProperty(\"" + khoa + "\"))")
                || ma.contains("Short.parseShort(p.getProperty(\"" + khoa + "\"))");
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemTaiLieuCauHinh() {
    }
}
