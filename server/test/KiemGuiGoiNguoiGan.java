import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CHARACTERIZATION TEST (xem chu thich dau test/Kiem.java): chot rang hai ham phat goi cho
 * NGUOI DUNG GAN - Char.sendToNearPlayer va Char.sendEffToNearChar - KHONG loc lai theo ban
 * do / khu vuc / tam nhin.
 *
 * VI SAO. nearChars DA la danh sach da loc: CharCapNhat.updateNearChar chi them id nao qua
 * duoc kiem tra ban do + quoc gia + khu vuc + tam nhin, va go ra khi ho roi di. Loc lan hai
 * o cho gui chi co the LAM MAT nguoi nhan khi mot truong lo cu. Trong so goi di qua duong
 * nay co MessageCreator.createCharInfo(this) - goi mo ta NGOAI HINH nhan vat; khong toi noi
 * thi nguoi ben canh khong co gi de ve.
 *
 * LOI DA XAY RA THAT - hong HAI lan lien tiep:
 *
 *   bb83665 (ban dich nguoc goc): than "if" RONG nen khong bo qua ai -> gui cho TAT CA
 *       trong nearChars. Chay dung, khong ai bao loi.
 *
 *   2480c47 "fix isse" (05/09/2026 02:29): them "continue;" vao than rong. Phep loc bat dau
 *       chay that, lay luon ve "p.region == this.region" - bo qua dung nguoi CUNG khu vuc,
 *       tuc gui cho gan nhu khong ai. Nguoi choi bao nhan vat "luc an luc hien" ngay sau khi
 *       ban nay len server.
 *
 *   Lan sua dau: doi sang "p.region != this.region" cho khop huong voi sendInfoMove2Near().
 *       Dung huong nhung VAN HEP HON ban goc. Nguoi choi bao VAN NHAY.
 *
 * Nay quay ve ngu nghia ban goc. sendInfoMove2Near() KHONG phai chuan doi chieu cho hai ham
 * nay (no co phep loc region rieng, chua bao gio bi sua va chua bao gio bi bao loi) - dung
 * no lam chuan chinh la sai lam cua lan sua dau.
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

        // --- sendInfoMove2Near GIU phep loc region cua rieng no (ham nay chua bao gio hong) ---
        final String dkGui = dieuKienTrongHam(ch, "public void sendInfoMove2Near()");
        Kiem.dung("tim thay dieu kien loc trong sendInfoMove2Near", dkGui != null);
        if (dkGui != null) {
            Kiem.dung("sendInfoMove2Near VAN gui khi cung khu vuc (p.region == this.region)",
                    dkGui.contains("p.region == this.region"));
        }

        // --- Hai ham phat goi KHONG duoc loc lai ban do / khu vuc / tam nhin ---
        final String[] hamPhat = {
            "public void sendEffToNearChar()",
            "public void sendToNearPlayer(final Message m)",
        };
        for (final String ham : hamPhat) {
            final String than = thanHam(ch, ham);
            Kiem.dung("tim thay than ham " + ham, than != null);
            if (than == null) {
                continue;
            }
            // nearChars da loc san o updateNearChar - loc lai chi lam MAT nguoi nhan.
            Kiem.sai(ham + ": KHONG loc lai theo khu vuc (ca hai chieu deu sai - da hong 2 lan)",
                    than.contains("p.region == this.region") || than.contains("p.region != this.region"));
            Kiem.sai(ham + ": KHONG loc lai theo ban do",
                    than.contains("p.map != this.map") || than.contains("p.map == this.map"));
            Kiem.sai(ham + ": KHONG loc lai theo tam nhin",
                    than.contains("this.near(p, this.rangeAddMonster[0])"));

            // Ba phep bo qua that su can thi phai con.
            Kiem.dung(ham + ": van bo qua id da roi map (p == null)",
                    than.contains("p == null"));
            Kiem.dung(ham + ": van bo qua chinh minh (p.id == this.id)",
                    than.contains("p.id == this.id"));
            Kiem.dung(ham + ": van bo qua bot (p.isBot != -1)",
                    than.contains("p.isBot != -1"));
        }

        // Doc p.session phai nam SAU phep kiem null, neu khong phep kiem null la ma chet.
        final String thanGui = thanHam(ch, "public void sendToNearPlayer(final Message m)");
        if (thanGui != null) {
            // Tim DONG MA that ("final Session s = p.session;"), khong tim chuoi "p.session"
            // tran - chuoi do con xuat hien trong chu thich ngay phia tren nen se khop nham.
            final int viTriKiemNull = thanGui.indexOf("p == null");
            final int viTriDocSession = thanGui.indexOf("final Session s = p.session;");
            Kiem.dung("sendToNearPlayer: doc p.session SAU khi da kiem p == null",
                    viTriKiemNull >= 0 && viTriDocSession > viTriKiemNull);
            Kiem.dung("sendToNearPlayer: kiem session != null truoc khi gui",
                    thanGui.contains("if (s != null)"));
        }

        // --- Xoa khoi nearChars PHAI theo ID (Short), khong duoc truyen ca doi tuong Char ---
        //
        // LOI DA XAY RA THAT (05/09/2026): nearChars la Vector<Short> (them bang "p1.id"),
        // nhung cho xoa lai truyen "p1" la Char -> khop vao Vector.remove(Object), tim mot
        // Char trong danh sach toan Short nen KHONG BAO GIO xoa duoc, va im lang tra ve false.
        // Id nguoi o xa khong bao gio roi danh sach -> server ban lai goi charOutRange ("xoa
        // nhan vat nay") deu deu moi 300ms, con goi di chuyen lai ve lai ngay: nhan vat nhay
        // ~3 lan/giay. Ngoai ra o "nguoi choi o gan" (nNearchar, mac dinh 8) khong bao gio
        // duoc tra lai nen nguoi thu 9 tro di khong con duoc dang ky.
        final String capNhat = doc("src/real/CharCapNhat.java");
        Kiem.dung("doc duoc src/real/CharCapNhat.java", !capNhat.isEmpty());
        if (!capNhat.isEmpty()) {
            Kiem.sai("CharCapNhat: KHONG con xoa nearChars bang ca doi tuong Char (remove(p1))",
                    capNhat.contains("nearChars.remove(p1)"));
            Kiem.dung("CharCapNhat: xoa nearChars theo ID (Short.valueOf(p1.id))",
                    capNhat.contains("nearChars.remove(Short.valueOf(p1.id))"));
        }

        // Than "if" KHONG duoc rong (ban truoc 2480c47): than rong thi phep bo qua p == null
        // vo tac dung, va p null se nem NPE bi nuot lang le.
        for (final String ham : hamPhat) {
            final String than = thanHam(ch, ham);
            if (than == null) {
                continue;
            }
            Kiem.dung(ham + ": phep bo qua co than \"continue;\" (khong phai than rong)",
                    than.contains("continue;"));
        }
    }

    /**
     * Rut than ham (tu chu ky den dau ham ke tiep, toi da 1200 ky tu). Du de soi cac phep
     * kiem trong than ma khong nuot sang ham khac.
     */
    private static String thanHam(final String nguon, final String chuKy) {
        final int moc = nguon.indexOf(chuKy);
        if (moc < 0) {
            return null;
        }
        return nguon.substring(moc, Math.min(nguon.length(), moc + 1200));
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
