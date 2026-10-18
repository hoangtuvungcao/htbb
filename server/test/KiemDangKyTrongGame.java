import data.QuyTacDangKy;
import data.QuyTacDangKyDuLieu;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import real.cmd.DangKyTrongGame;

/**
 * DANG KY TRONG GAME (lenh 39) - quy tac moi tu 10/2026: dang ky chay CA trong game lan tren web.
 *
 * (Truoc day file nay la KiemDongDangKyClient, canh lenh 39 PHAI bi dong. Quyet dinh da doi: nay
 * canh lenh 39 mo lai DUNG CACH - cung quy tac + han muc voi web, co cau do, tat duoc bang
 * sv.dangKyTrongGame, va KHONG BAO GIO quay lai real.RequestRegisterHandler cu cua lib/htbb-core.jar.)
 */
public final class KiemDangKyTrongGame {

    private static final String F_CONTROLLER = "src/real/RealController.java";
    private static final String F_HANDLER = "src/real/cmd/DangKyTrongGame.java";
    private static final String F_JSON_WEB = "../web/shared/data/quy-tac-dang-ky.json";

    public static void chay() {
        Kiem.nhom("Dang ky trong game (lenh 39) - quy tac chung voi web");

        final String dieuPhoi = boChuThich(doc(F_CONTROLLER));
        Kiem.dung("doc duoc " + F_CONTROLLER, dieuPhoi.length() > 0);
        Kiem.sai("KHONG dung lai real.RequestRegisterHandler (khong han muc, khong dau vet)",
                dieuPhoi.contains("RequestRegisterHandler"));
        Kiem.dung("lenh 39 tro toi DangKyTrongGame",
                dieuPhoi.contains("commandHandler.put(39, new real.cmd.DangKyTrongGame())"));

        final String h = boChuThich(doc(F_HANDLER));
        Kiem.dung("handler dung quy tac chung QuyTacDangKy.kiemTra(..., NGUON_GAME)",
                h.contains("QuyTacDangKy.kiemTra(") && h.contains("NGUON_GAME"));
        Kiem.dung("han muc dem gop web+game trong web_chan_spam",
                h.contains("web_chan_spam") && h.contains("'dang_ky','\" + HANH_DONG_IP"));
        Kiem.dung("mat khau luu bang PASSWORD() (khop CheckLoginSocket.jar)", h.contains("PASSWORD(?)"));
        // fromgame PHAI = 0: CheckLoginSocket.jar tra fromgame ve lam bRegion, LoginHandler chi nhan 0/9.
        Kiem.dung("fromgame=0 nhu web (khac 0 la khong dang nhap duoc)", h.contains("`fromgame`") && h.contains("NOW(), 0, 0, 0, ?)"));
        Kiem.dung("chan may bi cam", h.contains("CamTheoMay.biCam("));
        Kiem.dung("tat duoc bang sv.dangKyTrongGame", doc("src/server/TeamServer.java").contains("p.getProperty(\"sv.dangKyTrongGame\")"));
        Kiem.dung("TeamServer doc cau hinh dang ky",
                doc("src/server/TeamServer.java").contains("real.cmd.DangKyTrongGame.docCauHinh("));
        for (final String f : new String[]{"server.ini", "../vps/server.ini.tmpl"}) {
            Kiem.dung(f + " co sv.dangKyTrongGame=1", doc(f).contains("\nsv.dangKyTrongGame=1"));
        }
        Kiem.dung("CamTheoMay ghi ro Session.IMEI van CHI den tu goi DANG NHAP",
                doc("src/data/CamTheoMay.java").contains("CHI den tu goi DANG NHAP"));

        // ---- bo ca chung voi web (chay y het o npm test cua HTBB-Web) ----
        int sai = 0;
        for (final String[] t : QuyTacDangKyDuLieu.THU_NGHIEM) {
            if (!ma(QuyTacDangKy.kiemTra(t[0], t[1], t[2], t[3], QuyTacDangKy.NGUON_WEB)).equals(t[4])) {
                sai++;
                System.out.println("    lech web: " + t[0] + "/" + t[1] + "/" + t[3] + " -> "
                        + ma(QuyTacDangKy.kiemTra(t[0], t[1], t[2], t[3], QuyTacDangKy.NGUON_WEB)) + " (mong " + t[4] + ")");
            }
            if (!ma(QuyTacDangKy.kiemTra(t[0], t[1], t[2], t[3], QuyTacDangKy.NGUON_GAME)).equals(t[5])) {
                sai++;
                System.out.println("    lech game: " + t[0] + "/" + t[1] + "/" + t[3] + " -> "
                        + ma(QuyTacDangKy.kiemTra(t[0], t[1], t[2], t[3], QuyTacDangKy.NGUON_GAME)) + " (mong " + t[5] + ")");
            }
        }
        Kiem.bang("bo ca chung (" + QuyTacDangKyDuLieu.THU_NGHIEM.length + " ca x web/game) khop", 0, sai);
        Kiem.bang("adm1n van la ten quan tri", "tenQuanTri", QuyTacDangKy.tenBiCam("adm1n"));
        Kiem.bang("cau bao co dien {min}/{max}",
                "Tên tài khoản phải từ 4-20 ký tự, chỉ gồm chữ thường và số.", QuyTacDangKy.thongBao("tenSai"));

        final File json = new File(F_JSON_WEB);
        if (json.isFile()) {
            Kiem.bang("QuyTacDangKyDuLieu.java khop tep JSON cua web (sha256) - lech thi chay "
                    + "node scripts/sinh-quy-tac-dang-ky-java.mjs", sha256(json), QuyTacDangKyDuLieu.SHA256);
        }

        // ---- han muc ----
        Kiem.bang("han muc 3/gio", 3, QuyTacDangKyDuLieu.HAN_MUC_MOI_GIO);
        Kiem.bang("han muc 10/ngay", 10, QuyTacDangKyDuLieu.HAN_MUC_MOI_NGAY);
        Kiem.bang("2 lan trong gio: con duoc", null, QuyTacDangKy.vuotHanMuc(2, 2, 0));
        Kiem.bang("3 lan trong gio: chan", "quaGio", QuyTacDangKy.vuotHanMuc(3, 3, 0));
        Kiem.bang("10 lan trong ngay: chan", "quaNgay", QuyTacDangKy.vuotHanMuc(0, 10, 0));
        DangKyTrongGame.xoaRam();
        final long bay = 10L * 24 * 3600 * 1000;
        DangKyTrongGame.ghiRam("ip|t", bay - 2L * 3600 * 1000);   // 2 gio truoc
        DangKyTrongGame.ghiRam("ip|t", bay - 25L * 3600 * 1000);  // qua 24 gio: khong tinh
        DangKyTrongGame.ghiRam("ip|t", bay - 60 * 1000);          // 1 phut truoc
        final int[] d = DangKyTrongGame.demRam("ip|t", bay);
        Kiem.bang("bo dem RAM: 1 trong gio", 1, d[0]);
        Kiem.bang("bo dem RAM: 2 trong 24 gio", 2, d[1]);
        DangKyTrongGame.xoaRam();
        Kiem.dung("khoa may vua cot ip varchar(45)", DangKyTrongGame.khoaMay("abc-123").length() <= 45);

        // ---- cau do ----
        final DangKyTrongGame.CauDo cd = DangKyTrongGame.taoCauDo(1000L);
        Kiem.dung("cau do co chu 'bằng mấy?'", cd.cauHoi.endsWith("bằng mấy?"));
        Kiem.dung("dap an 0..18", cd.dapAn >= 0 && cd.dapAn <= 18);
        final long ok = 1000L + DangKyTrongGame.CAU_DO_SOM_NHAT_MS + 1;
        Kiem.dung("khong co cau do -> loi", DangKyTrongGame.kiemCauDo(null, "1", ok) != null);
        Kiem.dung("tra loi qua nhanh -> loi", DangKyTrongGame.kiemCauDo(cd, String.valueOf(cd.dapAn), 1500L) != null);
        Kiem.dung("het han -> loi", DangKyTrongGame.kiemCauDo(cd, String.valueOf(cd.dapAn),
                1000L + DangKyTrongGame.CAU_DO_HET_HAN_MS + 1) != null);
        Kiem.dung("sai dap an -> loi", DangKyTrongGame.kiemCauDo(cd, String.valueOf(cd.dapAn + 1), ok) != null);
        Kiem.dung("khong phai so -> loi", DangKyTrongGame.kiemCauDo(cd, "abc", ok) != null);
        Kiem.bang("dung dap an -> qua", null, DangKyTrongGame.kiemCauDo(cd, String.valueOf(cd.dapAn), ok));
    }

    private static String ma(final List<QuyTacDangKy.Loi> ds) {
        final List<String> m = new ArrayList<String>();
        for (final QuyTacDangKy.Loi l : ds) {
            m.add(l.ma);
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.size(); i++) {
            sb.append(i == 0 ? "" : ",").append(m.get(i));
        }
        return sb.toString();
    }

    private static String sha256(final File f) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final InputStream in = new FileInputStream(f);
            try {
                final byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) > 0) {
                    md.update(b, 0, n);
                }
            } finally {
                in.close();
            }
            final StringBuilder sb = new StringBuilder();
            for (final byte x : md.digest()) {
                sb.append(String.format("%02x", x & 0xFF));
            }
            return sb.toString();
        } catch (final Exception e) {
            return "";
        }
    }

    /** Bo chu thich // va block (de kiem khong bi danh lua boi chu thich). */
    private static String boChuThich(final String ma) {
        return ma.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)^\\s*//.*$", "");
    }

    private static String doc(final String duongDan) {
        final File f = new File(duongDan);
        if (!f.isFile()) {
            return "";
        }
        try {
            final InputStream in = new FileInputStream(f);
            try {
                final java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                final byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) > 0) {
                    bo.write(b, 0, n);
                }
                return new String(bo.toByteArray(), "UTF-8");
            } finally {
                in.close();
            }
        } catch (final Exception e) {
            return "";
        }
    }

    private KiemDangKyTrongGame() {
    }
}
