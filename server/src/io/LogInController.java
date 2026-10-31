package io;

import com.htbb.dangnhap.KhachDangNhap;
import real.cmd.LoginHandler;
import server.TeamServer;

import java.util.Hashtable;

/**
 * Ket noi game -> login server (giao thuc docs/dang-nhap.md). Phan mang (mot ket noi ben, noi lai, GUI LAI yeu cau
 * dang cho khi login server khoi dong lai, han cho) nam o {@link KhachDangNhap}; lop nay chi ghep phien voi Session
 * cua nguoi choi.
 *
 * <p>31/10/2026 (cong kiem: "dk.dang_nhap khong tra loi"): truoc day game KHONG BAO GIO tra loi client neu login
 * server khong tra loi -> qua {@link #CHO_LOGIN_MS} thi bao client thu lai + canh bao. Cung ngay: luong gui cu kiem
 * "con noi" roi moi cho hang, nen login server khoi dong lai thi yeu cau KE TIEP bi ghi vao socket chet va mat (nguoi
 * do cho 15 s). Nay yeu cau con cho duoc gui lai tren ket noi moi; tra loi chi giao mot lan.
 */
public class LogInController implements ISessionHandler {
    private static final byte CHECK_LOGIN = 1;
    /** Hang gui qua so nay -> "Máy chủ quá tải" ngay (nhu ban cu). */
    static final int HANG_TOI_DA = 50;
    static final long CHO_LOGIN_MS = Long.getLong("htbb.choLoginMs", 15000L);
    private final Hashtable<Long, Session> htLoginSessionList = new Hashtable<>();
    private static long SESSION_ID = 1L;
    public static LogInController Instance = new LogInController();
    private final KhachDangNhap khach;

    public LogInController() {
        this.khach = new KhachDangNhap(new KhachDangNhap.DiaChi() {
            @Override
            public String host() {
                return TeamServer.ipLogin;
            }

            @Override
            public int cong() {
                return TeamServer.portLogin;
            }
        }, new KhachDangNhap.Nghe() {
            @Override
            public void traLoi(final long phien, final int ketQua, final boolean vinaphone, final byte region) {
                OnMessageLogInResponse(phien, ketQua, vinaphone, region);
            }

            @Override
            public void hetHan(final long phien, final long daChoMs, final String tinhTrang) {
                hetHanCho(phien, daChoMs, tinhTrang);
            }

            @Override
            public void suKien(final String chu) {
                if (chu.startsWith("CONNECT")) {
                    System.out.println(chu); // giu nguyen chu cu (vps/theo-doi/bao-cao-ngay.sh dem)
                } else {
                    com.htbb.log.Nhat.canhBao("DANG_NHAP", chu);
                }
            }
        }, CHO_LOGIN_MS);
    }

    private synchronized long GetSessionID() {
        return LogInController.SESSION_ID++;
    }

    public boolean OnMessageLogInComming(Session s, String sUserName, String sPassword) {
        final long sSessionID = this.GetSessionID();
        s.lgData = new LogInData();
        s.lgData.username = sUserName;
        s.lgData.UserID = -9;
        if (this.khach.hangGui() > HANG_TOI_DA) {
            System.out.println("LOG IN QUEUE FULLLLLLLLLLLLL !!!!!!!!!!!!!");
            LoginHandler.onLoginFail(s);
            return false;
        }
        this.htLoginSessionList.put(sSessionID, s);
        this.khach.gui(sSessionID, sUserName, sPassword, (byte) 9);
        return true;
    }

    public void OnMessageLogInResponse(long sSessionID, int iUserID, boolean bIsVinaphone, byte bRegion) {
        final Session s = this.htLoginSessionList.remove(sSessionID);
        if (s != null) {
            s.lgData.UserID = iUserID;
            s.lgData.bVinaFone = bIsVinaphone;
            s.lgData.bRegion = bRegion;
            LoginHandler.onLoginOK(s);
            synchronized (s.LOCK) {
                try {
                    s.LOCK.notify();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Qua CHO_LOGIN_MS: bao client thu lai (khong de treo), ghi canh bao. */
    private void hetHanCho(final long phien, final long daChoMs, final String tinhTrang) {
        final Session s = this.htLoginSessionList.remove(phien);
        com.htbb.log.Nhat.canhBao("DANG_NHAP", "login server khong tra loi sau " + daChoMs + " ms: "
                + (s == null || s.lgData == null ? "?" : com.htbb.log.Nhat.sach(s.lgData.username)) + " (" + tinhTrang + ")");
        if (s != null) {
            try {
                s.sendMessage(real.MessageCreator.createServerAlertMessage(
                        "Máy chủ đăng nhập chưa phản hồi. Vui lòng thử lại sau ít giây.", ""));
            } catch (Exception ignored) {
            }
        }
    }

    public void run() {
        System.out.println("RUN LOG IN SEND MESSAGE .....");
        this.khach.batDau();
    }

    @Override
    public void onDisconnected(Session conn) {
        System.out.println(" LOG IN SERVER DISCONNECTED !!!!!!!!! ALARM ALARM ALARM ALARM ALARM !!!!!!!!!!!!!!!");
    }

    @Override
    public void processMessage(Session conn, Message message) {
        // ket noi login nay do KhachDangNhap tu doc (khong qua io.Session nua)
    }

    @Override
    public void onDisconnectedFromLocalPlace(Session conn) {
    }
}
