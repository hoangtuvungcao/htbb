package real;

import data.Database;
import data.DatabaseGiftCode;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * PHAT QUA DUA TOP tu trang quan tri web (yeu cau nguoi dung 19/09/2026).
 *
 * VI SAO CAN. Truoc day "dua top" chi co mot co bat/tat rieng (SuKien.DUA_TOP_HOA_LU)
 * khong gan voi phan thuong nao ca - admin muon phat thuong cho top phai lam thu cong
 * (vd goi lenh chat cong tung nguoi). Tinh nang nay cho admin: (1) nhap ten NHAN VAT
 * cho tung hang 1..10, (2) chon vat pham se phat cho hang do (dung LAI dung mot bo chon
 * + cu phap chuoi phan thuong cua trang giftcode.php, xem web/admin/inc/thuong.php),
 * (3) nhan vat duoc xep hang se thay mot muc "Nhan thuong đua top" o NPC Tong Quan
 * (npcType -10) - CHI nguoi co ten trong bang nay va CHUA nhan moi thay muc do.
 *
 * BANG `tob_top_thuong`: moi dong la MOT phan thuong cho MOT nhan vat o MOT dot dua
 * top - khong unique theo vi_tri, vi dua top lap lai nhieu dot (moi dot admin tao dong
 * moi, dong dot truoc da nhan van giu lai de tra cuu lich su, giong cach giftcode_log
 * giu lai sau khi giftcode bi xoa).
 *
 * AP DUNG PHAN THUONG: dung lai DatabaseGiftCode.applyItemString() - CHINH XAC cung bo
 * ~60 loai phan thuong (ITEM/GEM/PT/TT/PET/...) ma giftcode dang dung, khong viet lai.
 *
 * KHONG can vong lap rieng (khac LenhWeb): menu chi hien/an tai THOI DIEM nguoi choi mo
 * NPC Tong Quan, nen chi can truy van CSDL luc do (giong cach RequestNPCInfoHandler dang
 * goi Database.instance.isCharRegisterLiendau() thang trong cung case -10).
 */
public final class SuKienDuaTop {

    public static final String TEN_BANG = "tob_top_thuong";

    private SuKienDuaTop() {
    }

    // ------------------------------------------------------------------ bang

    /** Goi mot lan luc khoi dong server (xem TeamServer). Tu tao bang, khong lam gi neu da co. */
    public static void taoBang() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS `" + TEN_BANG + "` ("
                    + "`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "`vi_tri` TINYINT NOT NULL,"
                    + "`charname` VARCHAR(50) NOT NULL,"
                    + "`item` TEXT NULL,"
                    + "`da_nhan` TINYINT NOT NULL DEFAULT 0,"
                    + "`admin_user` VARCHAR(50) NOT NULL DEFAULT '',"
                    + "`tao_luc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "`nhan_luc` DATETIME NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "KEY `idx_charname_da_nhan` (`charname`, `da_nhan`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (final Exception e) {
            System.out.println("SU KIEN DUA TOP: loi tao bang - " + e);
        } finally {
            dong(st);
            tra(conn);
        }
    }

    // ------------------------------------------------------------------ hoi CSDL

    /** Nhan vat nay co dong thuong nao dang cho nhan khong - quyet dinh co hien muc menu. */
    public static boolean coThuongChoNhan(final String charname) {
        if (charname == null || charname.isEmpty()) {
            return false;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("SELECT 1 FROM `" + TEN_BANG
                    + "` WHERE charname = ? AND da_nhan = 0 LIMIT 1");
            ps.setString(1, charname);
            rs = ps.executeQuery();
            return rs.next();
        } catch (final Exception e) {
            System.out.println("SU KIEN DUA TOP: loi hoi coThuongChoNhan - " + e);
            return false;
        } finally {
            dong(rs);
            dong(ps);
            tra(conn);
        }
    }

    /** Mot dong thuong dang cho nhan. */
    private static final class DongThuong {
        long id;
        int viTri;
        String item;
    }

    private static List<DongThuong> layThuongChoNhan(final String charname) throws Exception {
        final List<DongThuong> ds = new ArrayList<DongThuong>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("SELECT id, vi_tri, item FROM `" + TEN_BANG
                    + "` WHERE charname = ? AND da_nhan = 0 ORDER BY vi_tri");
            ps.setString(1, charname);
            rs = ps.executeQuery();
            while (rs.next()) {
                final DongThuong d = new DongThuong();
                d.id = rs.getLong("id");
                d.viTri = rs.getInt("vi_tri");
                d.item = rs.getString("item");
                ds.add(d);
            }
        } finally {
            dong(rs);
            dong(ps);
            tra(conn);
        }
        return ds;
    }

    private static void danhDauDaNhan(final long id) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Database.instance.getConnection();
            // Chi danh dau khi dong CON dang o trang thai chua nhan: chan nhan trung neu
            // nguoi choi bam menu hai lan lien tiep truoc khi lan dau kip cap nhat xong.
            ps = conn.prepareStatement("UPDATE `" + TEN_BANG
                    + "` SET da_nhan = 1, nhan_luc = NOW() WHERE id = ? AND da_nhan = 0");
            ps.setLong(1, id);
            ps.executeUpdate();
        } finally {
            dong(ps);
            tra(conn);
        }
    }

    // ------------------------------------------------------------------ nhan thuong

    /**
     * Bam muc "Nhan thuong đua top". Nhan HET moi dong dang cho cua nhan vat nay (co
     * the co nhieu dot/hang cung luc chua nhan), moi dong danh dau da_nhan NGAY SAU KHI
     * da cong xong de tranh nhan trung khi bam lien tuc - giong thu tu "ghi truoc, cong
     * thuong sau" cua LenhWeb/giftcode se an toan hon o day vi applyItemString khong
     * nem loi giua chung voi du lieu do CHINH ADMIN nhap qua bo chon da kiem cu phap.
     */
    public static void nhanThuong(final Char player) {
        try {
            final List<DongThuong> ds = layThuongChoNhan(player.charname);
            if (ds.isEmpty()) {
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Bạn không có thưởng đua top nào đang chờ nhận.", ""));
                return;
            }
            final StringBuilder logViTri = new StringBuilder();
            for (final DongThuong d : ds) {
                danhDauDaNhan(d.id);
                DatabaseGiftCode.applyItemString(player, d.item);
                if (logViTri.length() > 0) {
                    logViTri.append(',');
                }
                logViTri.append(d.viTri);
            }
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Chúc mừng bạn đã nhận thưởng đua top (hạng " + logViTri + ").", ""));
            Database.instance.saveOrtherLog("", player.charname,
                    "nhan thuong dua top hang " + logViTri, "duatop_thuong");
        } catch (final Exception e) {
            e.printStackTrace();
            try {
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Có lỗi khi nhận thưởng, vui lòng thử lại hoặc báo quản trị viên.", ""));
            } catch (final Exception ignored) {
            }
        }
    }

    // ------------------------------------------------------------------ tien ich

    private static void dong(final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private static void dong(final Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private static void tra(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }
}
