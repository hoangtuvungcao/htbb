package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

/**
 * Luu diem binh chon su kien World Cup (Messi / Ronaldo).
 *
 * Hai bang, deu tu tao lan dau chay (xem {@link #taoBang()}), va cung co ban
 * migrate tay trong sql/them_su_kien_world_cup.sql:
 *
 *   tob_top_event_world_cup  - moi nhan vat mot dong: diem cho Messi, diem cho
 *                              Ronaldo, da nhan qua thuong / qua top chua.
 *   tob_event_world_cup_state- mot dong cho ca su kien: ben thang, da chot chua.
 *
 * VI SAO KHONG NAM TRONG data/Database.java: file do da 12.610 dong. Cac phan
 * tach ra truoc day (DatabaseGiftCode, DatabaseNhanVat, DatabaseXepHang) la lop
 * package-private con Database giu method vo boc; o day khong can lop vo do vi
 * chi mot noi goi (real.SuKienWorldCup), nen lop nay de public va goi thang.
 * Doi lai Database.java khong phai sua mot dong nao.
 */
public final class DatabaseSuKienWorldCup {

    /** Khoa dong trang thai trong tob_event_world_cup_state. */
    private static final String KHOA_SU_KIEN = "WORLD_CUP";

    /**
     * Ban goc goi ensureWorldCupTables() o DAU MOI truy van - tuc moi lan bam menu
     * la them hai CREATE TABLE + mot INSERT xuong CSDL. Ket noi nay dung chung cho
     * ca may chu game nen phai chot lai: chi tao mot lan.
     */
    private static volatile boolean daTaoBang = false;

    private DatabaseSuKienWorldCup() {
    }

    // ============================ TAO BANG ============================

    public static void taoBang() {
        if (daTaoBang) {
            return;
        }
        synchronized (DatabaseSuKienWorldCup.class) {
            if (daTaoBang) {
                return;
            }
            Connection conn = null;
            PreparedStatement pre = null;
            try {
                conn = Database.instance.getConnection();

                pre = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS tob_top_event_world_cup ("
                                + "charname VARCHAR(100) NOT NULL PRIMARY KEY,"
                                + "point_messi INT NOT NULL DEFAULT 0,"
                                + "point_ronaldo INT NOT NULL DEFAULT 0,"
                                + "claim_normal TINYINT NOT NULL DEFAULT 0,"
                                + "claim_top TINYINT NOT NULL DEFAULT 0,"
                                + "timeupdate DATETIME NULL"
                                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                pre.executeUpdate();
                pre.close();
                pre = null;

                pre = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS tob_event_world_cup_state ("
                                + "event_key VARCHAR(64) NOT NULL PRIMARY KEY,"
                                + "winner_side TINYINT NOT NULL DEFAULT 0,"
                                + "is_finalized TINYINT NOT NULL DEFAULT 0,"
                                + "finalized_by VARCHAR(100) NULL,"
                                + "timeupdate DATETIME NULL"
                                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                pre.executeUpdate();
                pre.close();
                pre = null;

                pre = conn.prepareStatement(
                        "INSERT INTO tob_event_world_cup_state(event_key,winner_side,is_finalized,timeupdate)"
                                + " VALUES(?,0,0,NOW()) ON DUPLICATE KEY UPDATE event_key=event_key");
                pre.setString(1, KHOA_SU_KIEN);
                pre.executeUpdate();

                daTaoBang = true;
            } catch (final Exception e) {
                System.out.println("[WORLDCUP] khong tao duoc bang: " + e);
            } finally {
                dong(null, pre, conn);
            }
        }
    }

    // ============================ GHI ============================

    /** Cong mot phieu cho nhan vat. side: 1 = Messi, 2 = Ronaldo. */
    public static void themPhieu(final String charname, final int side, final int diem) {
        taoBang();
        final String cot = (side == 1) ? "point_messi" : "point_ronaldo";
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "INSERT INTO tob_top_event_world_cup(charname," + cot + ",timeupdate)"
                            + " VALUES(?,?,NOW())"
                            + " ON DUPLICATE KEY UPDATE " + cot + "=" + cot + "+VALUES(" + cot + "), timeupdate=NOW()");
            pre.setString(1, charname);
            pre.setInt(2, diem);
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[WORLDCUP] loi ghi phieu: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    // ============================ DOC ============================

    /** Tong phieu hai ben: [0] Messi, [1] Ronaldo. */
    public static int[] tongPhieu() {
        taoBang();
        final int[] tong = {0, 0};
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "SELECT SUM(point_messi) sm, SUM(point_ronaldo) sr FROM tob_top_event_world_cup");
            rs = pre.executeQuery();
            if (rs.next()) {
                tong[0] = rs.getInt("sm");
                tong[1] = rs.getInt("sr");
            }
        } catch (final Exception e) {
            System.out.println("[WORLDCUP] loi doc tong phieu: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return tong;
    }

    /** Phieu cua rieng mot nhan vat: [0] Messi, [1] Ronaldo. */
    public static int[] phieuCuaNhanVat(final String charname) {
        taoBang();
        final int[] kq = {0, 0};
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "SELECT point_messi, point_ronaldo FROM tob_top_event_world_cup WHERE charname=?");
            pre.setString(1, charname);
            rs = pre.executeQuery();
            if (rs.next()) {
                kq[0] = rs.getInt("point_messi");
                kq[1] = rs.getInt("point_ronaldo");
            }
        } catch (final Exception e) {
            System.out.println("[WORLDCUP] loi doc phieu nhan vat: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return kq;
    }

    /** Top 10 nguoi binh chon nhieu nhat cho mot ben. CharInfo.honor = so phieu. */
    public static Vector<CharInfo> bangXepHang(final boolean laMessi) {
        taoBang();
        final Vector<CharInfo> top = new Vector<CharInfo>();
        final String cot = laMessi ? "point_messi" : "point_ronaldo";
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "SELECT charname, " + cot + " AS diem FROM tob_top_event_world_cup"
                            + " WHERE " + cot + " > 0 ORDER BY " + cot + " DESC, timeupdate ASC LIMIT 10");
            rs = pre.executeQuery();
            while (rs.next()) {
                final CharInfo c = new CharInfo();
                c.name = rs.getString("charname");
                c.honor = rs.getInt("diem");
                top.add(c);
            }
        } catch (final Exception e) {
            System.out.println("[WORLDCUP] loi doc bang xep hang: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return top;
    }

    /**
     * Hang cua nhan vat trong bang xep hang cua mot ben, 1 = dan dau, -1 = chua co
     * phieu nao cho ben do.
     *
     * Ban goc dung bien nguoi dung MySQL (@r:=@r+1) de danh so - kieu do phu thuoc
     * thu tu danh gia bieu thuc, MariaDB 10.4 tro len canh bao va MySQL 8 bo han.
     * O day dem thang: hang = so nguoi hon minh + 1, dong hang thi ai binh chon
     * truoc dung truoc (giong ORDER BY cua bangXepHang).
     */
    public static int hangCuaNhanVat(final String charname, final boolean laMessi) {
        taoBang();
        final String cot = laMessi ? "point_messi" : "point_ronaldo";
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "SELECT 1 + (SELECT COUNT(*) FROM tob_top_event_world_cup o"
                            + " WHERE o." + cot + " > t." + cot
                            + " OR (o." + cot + " = t." + cot + " AND o.timeupdate < t.timeupdate)) AS hang"
                            + " FROM tob_top_event_world_cup t WHERE t.charname=? AND t." + cot + " > 0");
            pre.setString(1, charname);
            rs = pre.executeQuery();
            if (rs.next()) {
                return rs.getInt("hang");
            }
        } catch (final Exception e) {
            System.out.println("[WORLDCUP] loi doc hang: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return -1;
    }

    // ============================ NHAN QUA ============================

    /** Trang thai nhan qua: [0] qua thuong, [1] qua top 1. */
    public static boolean[] daNhanQua(final String charname) {
        taoBang();
        final boolean[] kq = {false, false};
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "SELECT claim_normal, claim_top FROM tob_top_event_world_cup WHERE charname=?");
            pre.setString(1, charname);
            rs = pre.executeQuery();
            if (rs.next()) {
                kq[0] = rs.getInt("claim_normal") == 1;
                kq[1] = rs.getInt("claim_top") == 1;
            }
        } catch (final Exception e) {
            System.out.println("[WORLDCUP] loi doc trang thai nhan qua: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return kq;
    }

    /**
     * Danh dau da nhan qua. Chi ghi khi CSDL con dang chua nhan (WHERE claim=0),
     * nen hai phien ban cung bam mot luc thi chi mot cai an duoc qua.
     *
     * @param cotTop true = qua top 1, false = qua thuong
     * @return true neu lan goi NAY la lan danh dau thanh cong
     */
    public static boolean danhDauNhanQua(final String charname, final boolean cotTop) {
        taoBang();
        final String cot = cotTop ? "claim_top" : "claim_normal";
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "UPDATE tob_top_event_world_cup SET " + cot + "=1, timeupdate=timeupdate"
                            + " WHERE charname=? AND " + cot + "=0");
            pre.setString(1, charname);
            return pre.executeUpdate() > 0;
        } catch (final Exception e) {
            System.out.println("[WORLDCUP] loi danh dau nhan qua: " + e);
        } finally {
            dong(null, pre, conn);
        }
        return false;
    }

    /**
     * Ben thang: 1 = Messi, 2 = Ronaldo, 0 = chua chot duoc (hoa phieu).
     *
     * Lan dau goi sau khi het han binh chon thi CHOT lai ket qua vao CSDL, cac lan
     * sau doc lai con so da chot - de dam bao moi nguoi nhan qua theo cung mot ket
     * qua ke ca khi co ai do con kip binh chon xen vao giua.
     *
     * KHAC BAN GOC: hoa phieu thi KHONG chot (ban goc van ghi is_finalized=1 kem
     * winner_side=0, khoa vinh vien ket qua vao trang thai hoa).
     */
    public static int benThang() {
        taoBang();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "SELECT winner_side, is_finalized FROM tob_event_world_cup_state WHERE event_key=?");
            pre.setString(1, KHOA_SU_KIEN);
            rs = pre.executeQuery();
            if (rs.next() && rs.getInt("is_finalized") == 1) {
                return rs.getInt("winner_side");
            }
        } catch (final Exception e) {
            System.out.println("[WORLDCUP] loi doc ben thang: " + e);
            return 0;
        } finally {
            dong(rs, pre, conn);
        }

        final int[] tong = tongPhieu();
        if (tong[0] == tong[1]) {
            return 0;
        }
        final int thang = (tong[0] > tong[1]) ? 1 : 2;

        conn = null;
        pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "UPDATE tob_event_world_cup_state"
                            + " SET winner_side=?, is_finalized=1, finalized_by='system', timeupdate=NOW()"
                            + " WHERE event_key=? AND is_finalized=0");
            pre.setInt(1, thang);
            pre.setString(2, KHOA_SU_KIEN);
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[WORLDCUP] loi chot ben thang: " + e);
        } finally {
            dong(null, pre, conn);
        }
        return thang;
    }

    // ============================ TIEN ICH ============================

    private static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final Exception ignored) {
            }
        }
        if (pre != null) {
            try {
                pre.close();
            } catch (final Exception ignored) {
            }
        }
        if (conn != null) {
            try {
                Database.connPool.free(conn);
            } catch (final Exception ignored) {
            }
        }
    }
}
