import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * CHARACTERIZATION TEST (xem chu thich dau test/Kiem.java): chot lai tinh nang "xoa vinh vien"
 * tai khoan/nhan vat trong admin, them 05/09/2026 theo yeu cau nguoi dung ("xoá thật khỏi
 * database" - da duoc canh bao ro rang truoc do rang day la lan DAU TIEN codebase nay lam
 * DELETE that, khac han moi cho khac deu chi soft-delete qua cot ban/del).
 *
 * BA DIEU BAT BIEN CAN CHOT (phat hien/thiet ke luc lam tinh nang):
 * 1. MyISAM KHONG co giao dich - moi bang lien quan (xem sql/gamedb7_clean.sql) deu la
 *    MyISAM, khong the ROLLBACK giua chung. Vi vay LenhWeb.xoaNhanVatThat() PHAI xoa du
 *    lieu CON truoc, tob_char (dong chinh) SAU CUNG - loi giua chung thi tob_char con
 *    nguyen, an toan bam lai (moi buoc idempotent).
 * 2. Nhan vat dang online phai duoc kickPlayer() TRUOC khi xoa - tranh mot phien dang
 *    choi song song voi dong du lieu vua bi xoa that.
 * 3. Ca hai giao dien (HTBB-Web: API nhanvat/[id]/xoa xoa nhan vat rieng, taikhoan/[id] xoa ca tai khoan) deu
 *    phai: (a) chan tai khoan quan tri, (b) bat go dung ten de xac nhan O SERVER (khong
 *    chi dua vao JS disable nut), vi day la thao tac khong hoan tac duoc.
 */
public final class KiemXoaVinhVien {

    private static final String F_LENHWEB = "src/data/LenhWeb.java";
    /** HTBB-Web (thay website PHP da bo - docs/kien-truc.md). */
    private static final String F_LENH_TS = "server/utils/lenhNhanVat.ts";
    private static final String F_NHANVAT = "server/api/admin/nhanvat/[id]/xoa.post.ts";
    private static final String F_TAIKHOAN = "server/api/admin/taikhoan/[id].post.ts";
    private static final String F_TK_UTIL = "server/utils/taiKhoanAdmin.ts";
    private static final String F_SQL = "../sql/gamedb7_clean.sql";

    /** {ten bang, cot dung de khoa theo nhan vat} - dung THEO DUNG THU TU LenhWeb da xoa. */
    private static final String[][] BANG_CON_THEO_THU_TU = {
        {"tob_char_fruit", "id_char"},
        {"tob_char_quest", "id_char"},
        {"tob_char_tubinh", "id_char"},
        {"tob_animal", "owner"},
        {"tob_pet", "owner"},
        {"tob_gem_new", "owner"},
        {"tob_farm", "owner"},
        {"tob_sp_new", "owner"},
        {"tob_xoso", "owner"},
        {"tob_friendlist", "charId"},
        {"tob_quest", "idchar"},
        {"tob_quest_clan", "id_char"},
        {"top_char_quest", "id_char"},
        {"tob_vantieu", "idchar"},
        {"tob_wedding", "idhusband"},
        {"tob_wedding", "idwife"},
        {"tob_char_luong", "charname"},
        {"tob_char_thach_dau", "charname"},
        {"tob_char_vip", "charname"},
        {"tob_danh_hieu", "charname"},
        {"tob_web_info_char", "charname"},
        {"tob_market", "charname"},
        {"tob_inbox", "charname"},
    };

    public static void chay() {
        Kiem.nhom("Xoa vinh vien tai khoan/nhan vat (LenhWeb + HTBB-Web nhanvat/taikhoan)");

        final String lenhWeb = doc(F_LENHWEB);
        final String sql = doc(F_SQL);
        Kiem.dung("doc duoc " + F_LENHWEB, !lenhWeb.isEmpty());
        Kiem.dung("doc duoc " + F_SQL, !sql.isEmpty());
        if (lenhWeb.isEmpty() || sql.isEmpty()) {
            return;
        }

        // ---- 1. Moi bang/cot LenhWeb dung de xoa du lieu con THAT SU co trong schema
        // (tranh SQL nem loi "unknown column"/"table doesn't exist" khi chay that). ----
        for (final String[] bc : BANG_CON_THEO_THU_TU) {
            final String tenBang = bc[0];
            final String cot = bc[1];
            final int mocBang = sql.indexOf("CREATE TABLE `" + tenBang + "` (");
            Kiem.dung("schema: co bang `" + tenBang + "`", mocBang > 0);
            if (mocBang > 0) {
                final int mocKetBang = sql.indexOf(") ENGINE", mocBang);
                final String than = sql.substring(mocBang, mocKetBang > mocBang ? mocKetBang : sql.length());
                Kiem.dung("schema: `" + tenBang + "` co cot `" + cot + "`",
                        than.contains("`" + cot + "`"));
            }
        }

        // ---- 2. LenhWeb.java: THU TU xoa - du lieu con TRUOC, "DELETE FROM tob_char"
        // SAU CUNG (bat bien quan trong nhat: MyISAM khong rollback duoc). ----
        final int mocHam = lenhWeb.indexOf("static String xoaNhanVatThat(final int charId)");
        Kiem.dung("tim thay LenhWeb.xoaNhanVatThat", mocHam > 0);
        if (mocHam > 0) {
            final int mocKetHam = lenhWeb.indexOf("private static void xoaTheoCotSo(", mocHam);
            final String than = lenhWeb.substring(mocHam, mocKetHam > mocHam ? mocKetHam : lenhWeb.length());
            int vtTruoc = -1;
            boolean thuTuDung = true;
            for (final String[] bc : BANG_CON_THEO_THU_TU) {
                // Tim ca "bang", "cot" - khong chi ten bang - vi tob_wedding xuat hien
                // HAI LAN (idhusband, idwife) nen chi tim ten bang se luon khop vao lan
                // DAU TIEN ca hai lan, lam mat kha nang phat hien sai thu tu giua chung.
                final int vt = than.indexOf("\"" + bc[0] + "\", \"" + bc[1] + "\"");
                if (vt < 0 || vt < vtTruoc) {
                    thuTuDung = false;
                    break;
                }
                vtTruoc = vt;
            }
            Kiem.dung("xoaNhanVatThat: xoa du lieu con DUNG THU TU (khong bang nao xoa truoc bang dung truoc no)",
                    thuTuDung);
            final int vtXoaConCuoiCung = vtTruoc;
            final int vtXoaChinh = than.indexOf("\"DELETE FROM tob_char WHERE id = ?\"");
            Kiem.dung("xoaNhanVatThat: \"DELETE FROM tob_char\" nam SAU CUNG (sau toan bo du lieu con)",
                    vtXoaChinh > vtXoaConCuoiCung);
        }

        // ---- 3. xuLyXoaNhanVat: kickPlayer() TRUOC khi goi xoaNhanVatThat() khi nhan vat
        // dang online. ----
        final int mocXuLy = lenhWeb.indexOf("static KetQua xuLyXoaNhanVat(final int charId, final String adminUser)");
        Kiem.dung("tim thay LenhWeb.xuLyXoaNhanVat", mocXuLy > 0);
        if (mocXuLy > 0) {
            final int mocKetXuLy = lenhWeb.indexOf("static String xoaNhanVatThat(", mocXuLy);
            final String than = lenhWeb.substring(mocXuLy, mocKetXuLy > mocXuLy ? mocKetXuLy : lenhWeb.length());
            final int vtKick = than.indexOf("kickPlayer(");
            final int vtGoiXoa = than.indexOf("xoaNhanVatThat(charId)");
            Kiem.dung("xuLyXoaNhanVat: co goi kickPlayer", vtKick > 0);
            Kiem.dung("xuLyXoaNhanVat: kickPlayer nam TRUOC loi goi xoaNhanVatThat",
                    vtKick > 0 && vtGoiXoa > 0 && vtKick < vtGoiXoa);
        }

        // ---- 4. Dispatch: loai "xoa_nhan_vat" phai duoc dang ky trong xuLy(). ----
        Kiem.dung("LenhWeb: co hang so LOAI_XOA_NHAN_VAT = \"xoa_nhan_vat\"",
                lenhWeb.contains("LOAI_XOA_NHAN_VAT = \"xoa_nhan_vat\""));
        Kiem.dung("LenhWeb.xuLy(): dinh tuyen LOAI_XOA_NHAN_VAT toi xuLyXoaNhanVat",
                Pattern.compile("LOAI_XOA_NHAN_VAT\\.equals\\(loai\\)\\)\\s*\\{\\s*return xuLyXoaNhanVat\\(")
                        .matcher(lenhWeb).find());

        // ---- 5. Web (HTBB-Web; truoc la nhanvat.php/taikhoan.php cua website PHP da bo): hai
        // giao dien deu chan tai khoan quan tri VA bat go dung ten xac nhan tren SERVER (khong
        // chi dua vao JS). ----
        if (WebHtbb.boQua("xoa vinh vien phia web (HTBB-Web)")) {
            return;
        }
        final String lenhTs = WebHtbb.doc(F_LENH_TS);
        final String nhanvat = WebHtbb.doc(F_NHANVAT);
        final String taikhoan = WebHtbb.doc(F_TAIKHOAN);
        final String tkUtil = WebHtbb.doc(F_TK_UTIL);
        Kiem.dung("doc duoc " + WebHtbb.ten(F_LENH_TS), !lenhTs.isEmpty());
        Kiem.dung("doc duoc " + WebHtbb.ten(F_NHANVAT), !nhanvat.isEmpty());
        Kiem.dung("doc duoc " + WebHtbb.ten(F_TAIKHOAN), !taikhoan.isEmpty());
        Kiem.dung("doc duoc " + WebHtbb.ten(F_TK_UTIL), !tkUtil.isEmpty());

        Kiem.dung("lenhNhanVat.ts: co ham guiLenhXoaNhanVat",
                lenhTs.contains("export async function guiLenhXoaNhanVat("));

        Kiem.dung("API xoa nhan vat: chan tai khoan quan tri (ncLaQuanTri)",
                nhanvat.contains("if (chu !== '' && await ncLaQuanTri(chu))"));
        Kiem.dung("API xoa nhan vat: kiem tra go dung ten tren SERVER (xac_nhan_ten !== ten)",
                nhanvat.contains("if (str(body?.xac_nhan_ten) !== ten)"));
        Kiem.dung("API xoa nhan vat: goi guiLenhXoaNhanVat (di qua hang doi, khong DELETE thang tu web)",
                nhanvat.contains("guiLenhXoaNhanVat(dbGame(), id, ten, admin.username)"));

        final int mocXoaTK = taikhoan.indexOf("case 'xoa_tai_khoan': {");
        Kiem.dung("API tai khoan: co nhanh xu ly 'xoa_tai_khoan'", mocXoaTK > 0);
        if (mocXoaTK > 0) {
            final int mocKet = taikhoan.indexOf("case '", mocXoaTK + 10);
            final String than = taikhoan.substring(mocXoaTK, mocKet > mocXoaTK ? mocKet : taikhoan.length());
            Kiem.dung("xoa_tai_khoan: chan tai khoan quan tri (laAdmin)", than.contains("if (laAdmin) {"));
            Kiem.dung("xoa_tai_khoan: kiem tra go dung ten tren SERVER (xac_nhan_ten !== ten)",
                    than.contains("if (str(body?.xac_nhan_ten) !== ten)"));
            Kiem.dung("xoa_tai_khoan: xoa TUNG nhan vat qua hang doi TRUOC khi dung toi team_user",
                    than.indexOf("tkXoaHetNhanVat(") >= 0
                    && than.indexOf("tkXoaHetNhanVat(") < than.indexOf("DELETE FROM team_user"));
            Kiem.dung("xoa_tai_khoan: chi xoa team_user khi KHONG con nhan vat nao (soConLai > 0 thi dung)",
                    than.contains("if (kq.soConLai > 0) {")
                    && than.indexOf("if (kq.soConLai > 0) {") < than.indexOf("DELETE FROM team_user"));
        }
        Kiem.dung("tkXoaHetNhanVat gui lenh xoa_nhan_vat qua web_lenh_nhan_vat cho TUNG nhan vat",
                tkUtil.contains("TK_LENH_LOAI_XOA_NHAN_VAT = 'xoa_nhan_vat'")
                && tkUtil.contains("'INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, admin_user) VALUES (?, ?, ?, ?)'")
                && tkUtil.contains("const idLenh = await tkGuiLenhXoaNhanVat(dbGame(), Number(nv.id)"));

        // ---- 6. Khong ai (web) DELETE thang tu tob_char nua ngoai qua hang doi - tranh
        // lap lai dung sai cu (ghi thang RAM cua nhan vat dang online) da bi cam voi
        // dat_cap_do/xoa_vat_pham truoc day. ----
        for (final String[] f : new String[][]{{F_NHANVAT, nhanvat}, {F_TAIKHOAN, taikhoan}, {F_TK_UTIL, tkUtil}, {F_LENH_TS, lenhTs}}) {
            Kiem.sai(f[0] + ": KHONG co cau \"DELETE FROM tob_char\" ghi thang (phai di qua LenhWeb)",
                    Pattern.compile("DELETE\\s+FROM\\s+`?tob_char`?\\b", Pattern.CASE_INSENSITIVE).matcher(f[1]).find());
        }
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemXoaVinhVien() {
    }
}
