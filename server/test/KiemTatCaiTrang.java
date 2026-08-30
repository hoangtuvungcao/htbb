import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Kiem thu cong tac "Tat cai trang" (NPC Tho ren than bi -> Bo suu tap).
 *
 * LOI DA XAY RA THAT, nguoi choi bao: tat cai trang xong, dang xuat vao lai thi no TU BAT
 * LAI. Goc: Char.isBatCaiTrang chi la mot bien trong bo nho, mac dinh true, khong duoc ghi
 * xuong CSDL o bat cu dau — trong khi hai cong tac anh em (tat phi phong, tat mat na) deu
 * co cot rieng (isPP, isMN) va deu duoc nap lai luc dang nhap.
 *
 * ------------------------------------------------------------------------------------
 * VI SAO TEST NAY QUAN TRONG HON VE BE NGOAI
 * ------------------------------------------------------------------------------------
 * Sua loi nay la them mot cot vao cau UPDATE cua DatabaseNhanVat.saveChar. Nhung cau
 * UPDATE do co mot nhanh bat loi RAT NGUY HIEM: luu that bai thi server coi la loi hanh
 * trang va TU KHOA NHAN VAT (ban=1). Nghia la neu cot isCT chua ton tai tren may chu,
 * moi nguoi choi se bi KHOA ngay lan dang xuat dau tien.
 *
 * Vi vay khong duoc de viec them cot la mot buoc thu cong. Server phai tu them luc khoi
 * dong, TRUOC khi nhan nguoi choi vao. Cac phep kiem duoi day chot lai chuoi an toan do:
 * co ham tu them cot, ham do duoc goi luc khoi dong, va cau UPDATE co ghi cot.
 *
 * Thieu bat ky mat xich nao thi hoac tinh nang khong chay, hoac — te hon nhieu — nguoi
 * choi bi khoa hang loat.
 */
public final class KiemTatCaiTrang {

    private static final String F_CHAR = "src/real/Char.java";
    private static final String F_LUU = "src/data/DatabaseNhanVat.java";
    private static final String F_DB = "src/data/Database.java";
    private static final String F_SERVER = "src/server/TeamServer.java";
    private static final String F_MENU = "src/real/MapDoMenu.java";

    public static void chay() {
        Kiem.nhom("Tat cai trang - cong tac va cho luu");

        final String ch = doc(F_CHAR);
        final String luu = doc(F_LUU);
        final String db = doc(F_DB);
        final String server = doc(F_SERVER);
        final String menu = doc(F_MENU);

        // ---- Cong tac van con ----
        Kiem.dung("Char co isBatCaiTrang()", ch.contains("public boolean isBatCaiTrang()"));
        Kiem.dung("Char co setTatCaiTrang()", ch.contains("public void setTatCaiTrang()"));
        Kiem.dung("Char co setBatCaiTrang()", ch.contains("public void setBatCaiTrang()"));
        Kiem.dung("menu NPC co goi setTatCaiTrang", menu.contains("player.setTatCaiTrang()"));
        Kiem.dung("menu NPC co goi setBatCaiTrang", menu.contains("player.setBatCaiTrang()"));

        // ---- Luu xuong CSDL ----
        Kiem.dung("cau UPDATE cua saveChar co ghi cot isCT", luu.contains("isCT=?"));
        Kiem.dung("saveChar dat gia tri cho cot isCT",
                luu.contains("p.isBatCaiTrang() ? 0 : 1"));

        // CHIEU LUU phai la "1 = da tat", cung chieu voi isPP va isMN. Hang cu sau
        // ALTER TABLE deu bang 0; luu chieu nguoc lai thi ngay khi chay migration, TOAN BO
        // nguoi choi bi tat cai trang.
        Kiem.sai("KHONG luu chieu nguoc (1 = dang bat)",
                luu.contains("p.isBatCaiTrang() ? 1 : 0"));

        // Chi so tham so phai dich theo. Them mot "?" ma quen doi chi so cua charDBID thi
        // cau UPDATE se sua NHAM NHAN VAT KHAC — hoac nem loi, ma nem loi o day nghia la
        // nhan vat bi khoa.
        Kiem.dung("charDBID chuyen sang tham so 28", luu.contains("pre.setInt(28, p.charDBID)"));
        Kiem.sai("khong con dat charDBID o tham so 27", luu.contains("pre.setInt(27, p.charDBID)"));

        // ---- Nap lai luc dang nhap ----
        Kiem.dung("luc nap co doc cot isCT", db.contains("rs.getByte(\"isCT\")"));
        Kiem.dung("nap: 0 = dang bat", db.contains("p.setBatCaiTrang()"));
        Kiem.dung("nap: khac 0 = da tat", db.contains("p.setTatCaiTrang()"));

        // Phai doc TRONG try rieng. Cot isCT la cot moi; may chu chua co no thi
        // rs.getByte nem SQLException giua ham nap nhan vat -> hong dang nhap cua TOAN BO
        // nguoi choi. Thieu cot phai lui ve hanh vi cu, khong duoc lan ra.
        // Tim theo DONG MA, khong tim theo "rs.getByte(\"isCT\")" tran: chuoi do cung
        // xuat hien trong chinh khoi chu thich ngay tren, va chu thich dung TRUOC try nen
        // moi phep do khoang cach ben duoi se lech.
        final int mocDoc = db.indexOf("if (rs.getByte(\"isCT\") == 0) {");
        final int mocTry = db.lastIndexOf("try {", mocDoc);
        final int mocCatch = db.indexOf("catch", mocDoc);
        Kiem.dung("doc isCT nam trong mot try rieng, ngay sat truoc no",
                mocDoc > 0 && mocTry > 0 && mocDoc - mocTry < 200);
        Kiem.dung("co nhanh catch ngay sau, lui ve mac dinh dang bat",
                mocCatch > mocDoc && mocCatch - mocDoc < 300);

        // ---- Tu them cot luc khoi dong ----
        // Day la mat xich giu cho khong ai bi khoa. Xem chu thich dau tep.
        Kiem.dung("Database co ham tu them cot",
                db.contains("public void themCotTatCaiTrang()"));
        Kiem.dung("ham do hoi information_schema truoc (MySQL 5.x khong co IF NOT EXISTS)",
                db.contains("information_schema.COLUMNS"));
        Kiem.dung("ham do co cau ALTER TABLE them cot",
                db.contains("ALTER TABLE tob_char ADD COLUMN isCT"));
        Kiem.dung("cot moi mac dinh 0 (= dang bat, dung hanh vi cu)",
                db.contains("isCT int(11) NOT NULL DEFAULT 0"));
        Kiem.dung("TeamServer goi ham do luc khoi dong",
                server.contains("Database.instance.themCotTatCaiTrang()"));

        // Phai goi TRUOC khi may chu mo cong nhan nguoi choi. Do bang cho THAT SU tao
        // socket ("new ServerSocket("), khong do bang chu "ServerSocket" tran — chu do
        // con nam o dong import va o khai bao truong, ca hai deu o dau tep.
        final int mocThemCot = server.indexOf("themCotTatCaiTrang()");
        final int mocMoCong = server.indexOf("new ServerSocket(");
        Kiem.dung("them cot truoc khi mo cong nhan nguoi choi",
                mocThemCot > 0 && mocMoCong > 0 && mocThemCot < mocMoCong);

        // ---- Khong con dong in go loi trong duong chay ----
        Kiem.sai("khong con System.out.println go loi khi bat/tat cai trang",
                menu.contains("System.out.println(\"Tat cai trang"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemTatCaiTrang() {
    }
}
