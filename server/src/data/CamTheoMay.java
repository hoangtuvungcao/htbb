package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * CAM DANG NHAP THEO MA MAY (device id).
 *
 * VI SAO CAN: cam tai khoan thi ke pha chi can lap tai khoan moi trong 30 giay. Cam theo
 * IP thi chan nham ca nha / ca quan net dung chung duong mang — hom 29/08/2026 ca 5 nguoi
 * dang choi deu chung MOT IP, nen day khong phai lo xa.
 *
 * MA MAY LAY TU DAU: client Unity gui SystemInfo.deviceUniqueIdentifier
 * (Client/Canvas.cs:157).
 *
 * Truoc day no chi duoc gui kem goi DANG KY (lenh 39), va real.RequestRegisterHandler
 * trong lib/htbb-core.jar la noi duy nhat dien io.Session.IMEI. Handler cu do da bo han.
 * Lenh 39 nay la real.cmd.DangKyTrongGame: no doc ma may trong goi dang ky CHI de dem han
 * muc va chan may bi cam (biCam) - KHONG dien io.Session.IMEI.
 *
 * NGHIA LA: io.Session.IMEI van CHI den tu goi DANG NHAP. Server da doc san (LoginHandler), con
 * CLIENT thi chua gui — phai them mot dong vao cuoi goi dang nhap roi build lai:
 *     message.writer().writeUTF(Canvas.IMEI);
 * Chua lam buoc do thi bang tob_may_dang_nhap se rong, va moi thu van chay binh thuong.
 *
 * KHONG PHAI DIA CHI MAC, va do la co y: tu Android 10 / iOS 7 tro di ung dung khong doc
 * duoc MAC that nua (Android tra ve 02:00:00:00:00:00 cho moi ung dung, va MAC ngau nhien
 * hoa theo tung mang wifi la mac dinh). Du co doc duoc thi may chu cung khong bao gio
 * nhin thay MAC cua nguoi ket noi: MAC bi ghi de o moi chang router, nen may chu chi thay
 * MAC cua cong ra cua chinh no.
 *
 * BA GIOI HAN, ghi ra day de nguoi doc sau khong ky vong sai:
 *   1. Gia tri do CLIENT tu khai -> nam trong tay nguoi dung. Chan duoc nguoi pha luoi
 *      (da so), khong chan duoc nguoi biet sua client hay chan goi tin.
 *   2. Cai lai ung dung co the sinh ma moi -> nguoi bi cam thoat ra duoc.
 *   3. May dung chung thi cam mot nguoi la chan ca nhom.
 * Va no HOAN TOAN vo dung voi DDoS: ke tan cong khong dang nhap, chi mo socket.
 *
 * HAI QUYET DINH THIET KE
 * -----------------------
 * NAP DANH SACH VAO RAM, lam moi moi 60 giay. Neu tra database moi lan co nguoi dang
 * nhap thi mot tran do mat khau se thanh mot tran doi database — chinh la thu ta dang
 * muon chong. Doi lai: lenh cam co hieu luc cham nhat 60 giay.
 *
 * HONG THI CHO QUA (fail-open). Database chet ma van chan dang nhap thi mot su co nho
 * thanh su co tat server. Rui ro nguoc lai — mot nguoi bi cam vao duoc trong luc database
 * hong — la chap nhan duoc.
 */
public final class CamTheoMay {

    public static final String BANG_CAM = "tob_cam_may";
    public static final String BANG_LICH_SU = "tob_may_dang_nhap";

    /** Lenh cam co hieu luc cham nhat sau ngan nay. */
    private static final long CHU_KY_LAM_MOI_MS = 60000L;

    /**
     * deviceUniqueIdentifier cua Unity thuong la chuoi bam 32-40 ky tu. Cat o 64 vi day
     * la du lieu do client gui len: khong duoc de mot chuoi dai vo han di thang vao
     * database hay vao bo nho dem.
     */
    private static final int DAI_TOI_DA = 64;

    private static volatile Set<String> danhSachCam = Collections.emptySet();
    private static volatile long lanLamMoiCuoi = 0L;
    private static volatile boolean daBaoLoi = false;

    private CamTheoMay() {
    }

    /**
     * Lam sach ma may do client gui len.
     *
     * @return chuoi da lam sach, hoac "" neu khong dung dinh dang (khi do coi nhu khong co).
     */
    public static String lamSach(final String maMay) {
        if (maMay == null) {
            return "";
        }
        final String s = maMay.trim();
        if (s.isEmpty() || s.length() > DAI_TOI_DA) {
            return "";
        }
        // Chi cho chu, so, gach ngang, gach duoi. Unity sinh ra chuoi bam nen khong bao
        // gio co ky tu khac; chan o day de khong co gi la loi vao database.
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            final boolean hopLe = (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || c == '-' || c == '_';
            if (!hopLe) {
                return "";
            }
        }
        return s;
    }

    /** May nay co bi cam khong. Ma rong -> khong cam (client cu khong gui gi). */
    public static boolean biCam(final String maMay) {
        final String ma = lamSach(maMay);
        if (ma.isEmpty()) {
            return false;
        }
        lamMoiNeuCan();
        return danhSachCam.contains(ma);
    }

    /** Nap lai danh sach cam neu da qua chu ky. Hong thi giu nguyen danh sach cu. */
    private static void lamMoiNeuCan() {
        final long bayGio = System.currentTimeMillis();
        if (bayGio - lanLamMoiCuoi < CHU_KY_LAM_MOI_MS) {
            return;
        }
        lanLamMoiCuoi = bayGio;   // dat truoc: hong thi cung khong thu lai lien tuc

        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            final Set<String> moi = new HashSet<String>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT ma_may FROM " + BANG_CAM);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final String ma = lamSach(rs.getString(1));
                    if (!ma.isEmpty()) {
                        moi.add(ma);
                    }
                }
            }
            danhSachCam = moi;
            daBaoLoi = false;
        } catch (final Exception ex) {
            baoLoiMotLan(ex);
        } finally {
            tra(conn);
        }
    }

    /**
     * Ghi nhan may nao vua dang nhap tai khoan nao.
     *
     * Goi SAU khi mat khau da dung. Moi loi deu bi nuot: day chi la ghi chep, khong duoc
     * phep lam hong duong dang nhap cua nguoi choi.
     */
    public static void ghiNhanDangNhap(final String maMay, final String username, final String ip) {
        final String ma = lamSach(maMay);
        if (ma.isEmpty() || username == null || username.isEmpty()) {
            return;
        }
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            final String sql = "INSERT INTO " + BANG_LICH_SU
                    + " (ma_may, username, ip, lan_dau, lan_cuoi, so_lan)"
                    + " VALUES (?, ?, ?, NOW(), NOW(), 1)"
                    + " ON DUPLICATE KEY UPDATE ip = VALUES(ip), lan_cuoi = NOW(), so_lan = so_lan + 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ma);
                ps.setString(2, username.length() > 64 ? username.substring(0, 64) : username);
                ps.setString(3, (ip == null) ? "" : (ip.length() > 45 ? ip.substring(0, 45) : ip));
                ps.executeUpdate();
            }
        } catch (final Exception ex) {
            baoLoiMotLan(ex);
        } finally {
            tra(conn);
        }
    }

    /**
     * Bao loi DUNG MOT LAN cho den khi chay lai duoc.
     *
     * Chua tao bang thi moi lan dang nhap se sinh mot dong loi; voi mot tran do mat khau
     * thi day chinh la duong lam day o dia.
     */
    private static void baoLoiMotLan(final Exception ex) {
        if (daBaoLoi) {
            return;
        }
        daBaoLoi = true;
        System.out.println("CAM THEO MAY: " + ex
                + " (chay 'mysql -u root < sql/htbb.sql' neu chua tao bang;"
                + " se khong bao lai cho den khi chay duoc)");
    }

    /** Tra ket noi ve pool. free(null) van giam countConnection nen phai chan null. */
    private static void tra(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }
}
