import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CHARACTERIZATION TEST (xem chu thich dau test/Kiem.java): chot CHIEU DAU cua dieu kien
 * loc "nguoi choi o gan" trong Char.sendToNearPlayer / Char.sendEffToNearChar.
 *
 * LOI DA XAY RA THAT (05/09/2026, nguoi choi bao "hinh nhan vat cua toi va cua moi nguoi
 * khac luc an luc hien"):
 *
 * Ban dich nguoc goc (commit bb83665) de than "if" RONG, nen dieu kien khong he chay va
 * goi tin duoc gui cho TAT CA trong nearChars:
 *
 *     if (p == null || ... || p.region == this.region) {
 *     }
 *     s.sendMessage(m);
 *
 * Commit 2480c47 ("fix isse", 05/09/2026 02:29) them "continue;" vao than rong do. Dieu
 * kien bat dau chay that — va ve cuoi cua no BI NGUOC: day la dieu kien BO QUA, ma
 * "p.region == this.region" lai co nghia "bo qua nguoi CUNG khu vuc", dung nhung nguoi le
 * ra phai nhan. Tu do sendToNearPlayer() (305 cho goi trong src/) va sendEffToNearChar()
 * (41 cho goi) gui cho KHONG AI.
 *
 * CHUAN DOI CHIEU la ham song sinh KHONG bi sua trong cung tep: sendInfoMove2Near() GUI
 * khi "p.region == this.region". Dieu kien bo qua vi vay phai la "p.region != this.region".
 *
 * Phep kiem duoi day khong chep lai dieu kien bang tay ma DOI CHIEU HAI HAM voi nhau, nen
 * neu sau nay ai doi luat "gan" (vd bo han ve khu vuc) thi sua o sendInfoMove2Near la hai
 * ben tu khop, khong phai sua ca test.
 */
public final class KiemGuiGoiNguoiGan {

    private static final String F_CHAR = "src/real/Char.java";

    public static void chay() {
        Kiem.nhom("Gui goi tin cho nguoi choi o gan - chieu dau cua dieu kien loc");

        final String ch = doc(F_CHAR);
        Kiem.dung("doc duoc " + F_CHAR, !ch.isEmpty());
        if (ch.isEmpty()) {
            return;
        }

        // --- Chuan doi chieu: ham GUI (dieu kien thuan) ---
        final String dkGui = dieuKienTrongHam(ch, "public void sendInfoMove2Near()");
        Kiem.dung("tim thay dieu kien loc trong sendInfoMove2Near", dkGui != null);
        if (dkGui != null) {
            Kiem.dung("sendInfoMove2Near GUI khi cung khu vuc (p.region == this.region)",
                    dkGui.contains("p.region == this.region"));
        }

        // --- Hai ham BO QUA (dieu kien phu dinh) phai dung "!=" o ve khu vuc ---
        final String[] hamBoQua = {
            "public void sendEffToNearChar()",
            "public void sendToNearPlayer(final Message m)",
        };
        for (final String ham : hamBoQua) {
            final String dk = dieuKienTrongHam(ch, ham);
            Kiem.dung("tim thay dieu kien loc trong " + ham, dk != null);
            if (dk == null) {
                continue;
            }
            // Day la dieu kien BO QUA -> phai phu dinh tung ve so voi ham GUI.
            Kiem.dung(ham + ": bo qua nguoi KHAC khu vuc (p.region != this.region)",
                    dk.contains("p.region != this.region"));
            Kiem.sai(ham + ": KHONG duoc bo qua nguoi CUNG khu vuc (chieu nguoc - loi 2480c47)",
                    dk.contains("p.region == this.region"));
            // Cac ve con lai da phu dinh dung tu dau, chot lai de khong ai "sua" nhâm tiep.
            Kiem.dung(ham + ": bo qua khi khac ban do (p.map != this.map)",
                    dk.contains("p.map != this.map"));
            Kiem.dung(ham + ": bo qua khi ngoai tam nhin (!this.near(...))",
                    dk.contains("!this.near(p, this.rangeAddMonster[0])"));
        }

        // Than "if" rong (ban truoc 2480c47) cung la loi - luc do dieu kien vo tac dung.
        // Chot rang hai ham VAN con "continue;" de dieu kien that su chay.
        for (final String ham : hamBoQua) {
            final int moc = ch.indexOf(ham);
            if (moc < 0) {
                continue;
            }
            final String than = ch.substring(moc, Math.min(ch.length(), moc + 700));
            Kiem.dung(ham + ": dieu kien co than \"continue;\" (khong phai than rong)",
                    than.contains("continue;"));
        }
    }

    /**
     * Rut chuoi dieu kien trong cap ngoac cua lenh "if" DAU TIEN sau chu ky ham. Tra ve
     * null neu khong tim thay ham hoac khong co "if" nao.
     */
    private static String dieuKienTrongHam(final String nguon, final String chuKy) {
        final int mocHam = nguon.indexOf(chuKy);
        if (mocHam < 0) {
            return null;
        }
        // KHONG lay "if" dau tien: sendInfoMove2Near() mo dau bang "if (!this.isAdmin)",
        // lay nham dong do thi phep kiem bao hong oan trong khi ma nguon dung. Lay dong
        // NOI VE KHU VUC (chua "p.region") - do moi la dieu kien loc nguoi o gan.
        final int mocRegion = nguon.indexOf("p.region", mocHam);
        if (mocRegion < 0) {
            return null;
        }
        final int dauDong = nguon.lastIndexOf('\n', mocRegion) + 1;
        final int cuoiDong = nguon.indexOf('\n', mocRegion);
        return cuoiDong < 0 ? nguon.substring(dauDong) : nguon.substring(dauDong, cuoiDong);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemGuiGoiNguoiGan() {
    }
}
