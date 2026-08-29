package real.cmd;

import io.Message;
import io.Session;
import real.MessageCreator;
import server.TeamServer;

/**
 * DONG CUA DANG KY TRONG CLIENT (lenh 39).
 *
 * VI SAO PHAI DONG: dang ky tai khoan da chuyen han sang web (web/register.php). Nhung
 * server VAN dang ky handler cu cho lenh 39 (real.RequestRegisterHandler, nam trong
 * lib/NQSH_5h.jar) — mot diem TAO TAI KHOAN o trang thai CHUA DANG NHAP.
 *
 * Do la mot cua sau that su: web/register.php co han muc 5 tai khoan / IP / gio, dem
 * bang bang web_chan_spam. Duong lenh 39 KHONG di qua do — ai noi dung giao thuc la lap
 * duoc tai khoan hang loat, khong gioi han, khong dau vet trong web_chan_spam.
 *
 * VI SAO KHONG BO HAN commandHandler.put(39, ...): bo di thi lenh 39 roi xuong nhanh
 * cuoi cua RealController.processMessage, noi ma `p` la null (chua dang nhap) va khong
 * dieu kien nao khop — tuc la IM LANG hoan toan. Client cu con man hinh dang ky se treo
 * o do khong hieu chuyen gi. Tra ve mot loi nhan ro rang thi hon.
 *
 * TAC DUNG PHU DA CAN NHAC: real.RequestRegisterHandler chinh la noi DUY NHAT dien
 * io.Session.IMEI (doc ma may client gui kem goi dang ky). Dong no lai nghia la ma may
 * chi con den tu goi DANG NHAP — dung nhu data/CamTheoMay.java dang cho doi. Client hien
 * chua gui ma may luc dang nhap, nen phai them mot dong ben client:
 *     message.writer().writeUTF(Canvas.IMEI);   // cuoi goi dang nhap
 */
public final class DangKyDaChuyenSangWeb implements ICommandHandler {

    /**
     * Dem so lan bi goi, de biet co con client cu nao dung khong.
     *
     * In co HAM: mot ke muon lam day o dia chi can lap lai lenh 39. In lan dau roi cach
     * moi 500 lan la du de biet co chuyen gi ma khong bao gio thanh dong lu log.
     */
    private static final java.util.concurrent.atomic.AtomicLong SO_LAN =
            new java.util.concurrent.atomic.AtomicLong();

    private static final long CACH_BAO = 500L;

    @Override
    public void process(final Session session, final Message message) {
        final long lan = SO_LAN.incrementAndGet();
        if (lan == 1L || lan % CACH_BAO == 0L) {
            System.out.println("DANG KY TRONG CLIENT: da chan " + lan
                    + " lan (dang ky da chuyen sang web). IP gan nhat: "
                    + ((session == null) ? "?" : session.ip));
        }
        if (session == null) {
            return;
        }
        final String loiNhan = TeamServer.isServerIndo()
                ? "Registration has moved to our website. Please register there, then log in here."
                : "Đăng ký tài khoản đã chuyển sang trang web của máy chủ. "
                        + "Bạn vui lòng đăng ký trên web rồi quay lại đây đăng nhập.";
        final Message m = MessageCreator.createServerAlertMessage(loiNhan, "");
        session.sendMessage(m);
        m.cleanup();
    }
}
