package real;

import data.CharClan;
import data.Database;
import data.NewClan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Doi ten nhan vat (NPC Tho Ren Than Bi, 19/09/2026). 100.000 luong/lan.
 *
 * charname la UNIQUE KEY o tob_char (id moi la PRIMARY KEY) nen doi charname khong
 * dung toi id/khoa ngoai - nhung 2 cho VAN dang giu ten dang chu (khong phai id) va se
 * hong THAT SU (khong chi hien ten cu) neu khong sua cung luc:
 *   1. tob_clan.charMaster / NewClan.master - bang chu bang mat quyen dieu hanh.
 *   2. data.NewClan.member (Hashtable khoa bang charname thuong) - khoa cu con trong map
 *      thi cac thao tac thanh vien (kick/thang chuc...) tra ve "khong tim thay".
 * Ban than nguoi doi ten bi kick de dang nhap lai - CharManager (charName Hashtable)
 * tu dong danh chi so lai dung, khong can vá tay.
 *
 * Nguoi hon phoi (Char.nameHusband_wife) CHI duoc cap nhat neu dang online, dung quy uoc
 * co san cua he thong hon nhan (moi cho doc/ghi nameHusband_wife trong code goc deu di
 * qua CharManager.getCharByCharName, khong co duong doc rieng qua DB) - vo/chong dang
 * offline se thay ten cu cho toi khi ca hai cung online lai mot lan.
 */
public final class CharDoiTenNhanVat {

    public static final int COST_LUONG = 100000;
    public static final int INPUT_DOI_TEN = -32053;
    private static final int MIN_LEN = 2;
    private static final int MAX_LEN = 20;

    private CharDoiTenNhanVat() {
    }

    public static void doDoiTen(final Char player, final String tenMoiGoc) {
        final String tenMoi = tenMoiGoc == null ? "" : tenMoiGoc.trim();
        final String tenCu = player.charname;

        if (tenMoi.length() < MIN_LEN || tenMoi.length() > MAX_LEN) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Tên phải từ " + MIN_LEN + " đến " + MAX_LEN + " ký tự.", ""));
            return;
        }
        if (!tenMoi.matches("^[\\p{L}0-9 _]+$")) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Tên chứa ký tự không hợp lệ.", ""));
            return;
        }
        if (tenMoi.equalsIgnoreCase(tenCu)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Tên mới trùng tên hiện tại.", ""));
            return;
        }
        if (Database.laTenDanhRieng(tenMoi)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Tên này không thể sử dụng.", ""));
            return;
        }
        if (player.getLuong() < COST_LUONG) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Không đủ lượng (cần " + COST_LUONG + " lượng).", ""));
            return;
        }
        if (CharManager.instance.getCharByCharName(tenMoi) != null || tonTaiTrongCSDL(tenMoi)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Tên đã có người sử dụng.", ""));
            return;
        }

        if (!capNhatTenTrongCSDL(player.charDBID, tenMoi)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Đổi tên thất bại, thử lại sau.", ""));
            return;
        }

        player.subLuong(COST_LUONG);
        player.charname = tenMoi;

        if (player.idClan > 0) {
            suaTenTrongBang(player.idClan, tenCu, tenMoi);
        }

        if (player.nameHusband_wife != null && !player.nameHusband_wife.equals("")) {
            final Char spouse = CharManager.instance.getCharByCharName(player.nameHusband_wife);
            if (spouse != null) {
                spouse.nameHusband_wife = tenMoi;
            }
        }

        Database.instance.saveOrtherLog("", tenCu, tenCu + " doi ten thanh " + tenMoi, "doitennhanvat");

        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Đổi tên thành công: " + tenCu + " -> " + tenMoi + ". Vui lòng đăng nhập lại.", ""));
        CharManager.instance.kickPlayer(player, "doi ten nhan vat");
    }

    private static boolean tonTaiTrongCSDL(final String charname) {
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM tob_char WHERE charname=? LIMIT 1")) {
                ps.setString(1, charname);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return true; // loi CSDL: an toan hon la coi nhu ten da bi chiem, khong cho doi
        } finally {
            traConnection(conn);
        }
    }

    private static boolean capNhatTenTrongCSDL(final int charDBID, final String tenMoi) {
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tob_char SET charname=? WHERE id=?")) {
                ps.setString(1, tenMoi);
                ps.setInt(2, charDBID);
                return ps.executeUpdate() > 0;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            traConnection(conn);
        }
    }

    /** Bang chu bang (charMaster) + khoa trong danh sach thanh vien (NewClan.member) deu dang chu ten, khong phai id. */
    private static void suaTenTrongBang(final short idClan, final String tenCu, final String tenMoi) {
        final NewClan clan = NewClan.getClan(idClan);
        if (clan == null) {
            return;
        }
        if (clan.master != null && clan.master.equalsIgnoreCase(tenCu)) {
            clan.master = tenMoi;
            capNhatChuBangTrongCSDL(idClan, tenMoi);
        }
        synchronized (clan.member) {
            final CharClan cc = clan.member.remove(tenCu.toLowerCase());
            if (cc != null) {
                cc.charname = tenMoi;
                clan.member.put(tenMoi.toLowerCase(), cc);
            }
        }
    }

    private static void capNhatChuBangTrongCSDL(final short idClan, final String tenMoi) {
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tob_clan SET charMaster=? WHERE id_clan=?")) {
                ps.setString(1, tenMoi);
                ps.setShort(2, idClan);
                ps.executeUpdate();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            traConnection(conn);
        }
    }

    private static void traConnection(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }
}
