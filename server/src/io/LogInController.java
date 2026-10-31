package io;

import real.cmd.LoginHandler;
import server.TeamServer;

import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

public class LogInController implements ISessionHandler {
    private static final byte CHECK_LOGIN = 1;
    private Hashtable<Long, Session> htLoginSessionList;
    /**
     * Luc gui yeu cau (ms) cua moi phien dang cho login server. 31/10/2026 (cong kiem: "dk.dang_nhap khong tra loi"):
     * truoc day game KHONG BAO GIO tra loi client neu login server khong tra loi (cham / mat ket noi giua chung -
     * yeu cau gui tren socket chet bi mat) -> client treo o man dang nhap. Nay qua {@link #CHO_LOGIN_MS} thi bao client
     * thu lai + ghi canh bao (bao nhieu yeu cau dang cho, ket noi login con khong).
     */
    private final Hashtable<Long, Long> htLucGui = new Hashtable<>();
    static final long CHO_LOGIN_MS = Long.getLong("htbb.choLoginMs", 15000L);
    protected LinkedBlockingQueue<Message> m_msgSendQueue;
    protected LinkedBlockingQueue<Message> m_msgReceiveQueue;
    private static long SESSION_ID;
    private Session m_sLogInSession;
    public static LogInController Instance;

    static {
        LogInController.SESSION_ID = 1L;
        LogInController.Instance = new LogInController();
    }

    public LogInController() {
        this.htLoginSessionList = new Hashtable<>();
        this.m_msgSendQueue = new LinkedBlockingQueue<>();
        this.m_msgReceiveQueue = new LinkedBlockingQueue<>();
        this.m_sLogInSession = null;
    }

    private synchronized long GetSessionID() {
        return LogInController.SESSION_ID++;
    }

    public boolean OnMessageLogInComming(Session s, String sUserName, String sPassword) {
        Message m = new Message(1);
        long sSessionID = this.GetSessionID();
        s.lgData = new LogInData();
        s.lgData.username = sUserName;
        s.lgData.UserID = -9;
        try {
            m.dos.writeLong(sSessionID);
            m.dos.writeUTF(sUserName);
            m.dos.writeUTF(sPassword);
            m.dos.writeByte(9);
            if (this.m_msgSendQueue.size() > 50) 
            {
                System.out.println("LOG IN QUEUE FULLLLLLLLLLLLL !!!!!!!!!!!!!");
                LoginHandler.onLoginFail(s);
                return false;
            }
            this.htLucGui.put(sSessionID, System.currentTimeMillis());
            this.htLoginSessionList.put(sSessionID, s);
            this.m_msgSendQueue.add(m);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void OnMessageLogInResponse(long sSessionID, int iUserID, boolean bIsVinaphone, byte bRegion) {
        Session s = this.htLoginSessionList.remove(sSessionID);
        Long luc = this.htLucGui.remove(sSessionID);
        if (s == null && luc == null) {
            com.htbb.log.Nhat.canhBao("DANG_NHAP", "login server tra loi phien " + sSessionID + " da het han cho (> " + CHO_LOGIN_MS + " ms) - bo qua");
        }
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
                // // monitorexit(s.LOCK);
            }
        }
    }

    public void run() {
        this.SendMessage();
        this.ReceiveMessage();
        this.KiemHetHan();
    }

    /** Moi giay: phien cho login server qua CHO_LOGIN_MS -> bao client thu lai (khong de treo), ghi canh bao. */
    private void KiemHetHan() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000L);
                    long bay = System.currentTimeMillis();
                    for (Long id : new java.util.ArrayList<>(this.htLucGui.keySet())) {
                        Long luc = this.htLucGui.get(id);
                        if (luc == null || bay - luc < CHO_LOGIN_MS) {
                            continue;
                        }
                        this.htLucGui.remove(id);
                        Session s = this.htLoginSessionList.remove(id);
                        boolean noi = this.m_sLogInSession != null && this.m_sLogInSession.isConnected();
                        com.htbb.log.Nhat.canhBao("DANG_NHAP", "login server khong tra loi sau " + (bay - luc) + " ms: "
                                + (s == null || s.lgData == null ? "?" : com.htbb.log.Nhat.sach(s.lgData.username))
                                + " (dang cho " + this.htLucGui.size() + ", hang gui " + this.m_msgSendQueue.size()
                                + ", ket noi login " + (noi ? "con" : "MAT") + ")");
                        if (s != null) {
                            try {
                                s.sendMessage(real.MessageCreator.createServerAlertMessage(
                                        "Máy chủ đăng nhập chưa phản hồi. Vui lòng thử lại sau ít giây.", ""));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    com.htbb.log.Nhat.canhBao("DANG_NHAP", "kiem het han cho login loi: " + e);
                }
            }
        }, "LoginHetHan");
        t.setDaemon(true);
        t.start();
    }

    private void ReceiveMessage() {
        new Thread(() -> {
            while (true) {
                try {
                    while (true) {
                        Message m = LogInController.this.m_msgReceiveQueue.take();
                        long sessionId = m.dis.readLong();
                        int iUID = m.dis.readInt();
                        boolean bIsVinaPhone = m.dis.readBoolean();
                        byte bRegion = m.dis.readByte();
                        LogInController.this.OnMessageLogInResponse(sessionId, iUID, bIsVinaPhone, bRegion);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void SendMessage() {
        System.out.println("RUN LOG IN SEND MESSAGE .....");
        new Thread(() -> {
            while (true) {
                try {
                    while (true) {
                        Label_0094:
                        {
                            if (LogInController.this.m_sLogInSession != null) {
                                if (LogInController.this.m_sLogInSession.isConnected()) {
                                    break Label_0094;
                                }
                            }
                            try {
                                Socket s = new Socket(TeamServer.ipLogin, TeamServer.portLogin);
                                System.out.println("CONNECTED TO SERVER LOGIN");
                                LogInController.this.m_sLogInSession = new Session(s, LogInController.Instance, true);
                                LogInController.this.m_sLogInSession.start();
                            } catch (Exception e) {
                                System.err.println("CONNECT TO SERVER LOG IN FAIL !!!!!!!!");
                                Thread.sleep(1000L);
                                continue;
                            }
                        }
                        Message m = LogInController.this.m_msgSendQueue.take();
                        LogInController.this.m_sLogInSession.sendMessageLogin(m);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDisconnected(Session conn) {
        System.out.println(" LOG IN SERVER DISCONNECTED !!!!!!!!! ALARM ALARM ALARM ALARM ALARM !!!!!!!!!!!!!!!");
    }

    @Override
    public void processMessage(Session conn, Message message) {
        this.m_msgReceiveQueue.add(message);
    }

    @Override
    public void onDisconnectedFromLocalPlace(Session conn) {
    }
}
