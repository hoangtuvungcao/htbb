package io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import real.Char;

public class Session extends Thread {
    private static final long serialVersionUID = 1L;
    private volatile boolean connected = false;
    protected Socket socket;
    public int userID = -1;
    public int serviceID = -1;
    public String ip = "";
    public String bigProvider = "0";
    public String privateProvider = "0";
    public String clientProvider = "0";
    public String agentProvider = "0";
    public String username = "";
    public String version = "0";
    public String charname = "";
    public int versionCode = 0;
    public byte firmWare = 0;
    public byte vsTile = 0;
    public byte firmWareReg = 0;
    public boolean isLogin = false;
    public boolean available = false;
    public short w = 320;
    public short h = 320;
    public String IMEI = "";
    public String syntax = "";
    public String centerReg = "";
    public String unameReg = "";
    public String usernameReg = "";
    public long timeNapMoney = 0L;
    public byte idServer = 0;
    ISessionHandler messageHandler;
    private Sender sender;
    public Object LOCK = new Object();
    public long connectTime = System.currentTimeMillis();
    private byte[] key;
    public long lastActiveTime;
    public long lastTimeSendData;
    public int idPrivateParty = -1;
    public byte isOldVersion = 0;
    public boolean exit = false;
    public LogInData lgData;
    private int curR;
    private int curW;
    int loginday = 0;
    public short messCount = 0;
    public long lastCheck = System.currentTimeMillis();

    /** Chan ngap goi tin. Logic + ly do nam trong io/BoDemGoiTin.java. */
    private final BoDemGoiTin boDemGoiTin = new BoDemGoiTin(System.currentTimeMillis());
    int llll = 0;
    public boolean isCapNhatThongTin = false;
    public int timeXp = 0;
    public String ngaysinh = "18 11 1984";
    public String noicap;
    public String ngaycap;
    public String hoten;
    public String diachi;
    public String cmnd;
    public static byte[][] groupCmd = new byte[0][];
    public static Vector<Hashtable<Byte, Byte>> hasGroup = new Vector();

    static {
        for(int i = 0; i < groupCmd.length; ++i) {
            Hashtable<Byte, Byte> has = new Hashtable();

            for(int j = 0; j < groupCmd[i].length; ++j) {
                try {
                    has.put(groupCmd[i][j], groupCmd[i][j]);
                } catch (Exception var4) {
                }
            }

            hasGroup.add(has);
        }


    }

    public Session(Socket socket, ISessionHandler handler) {
        try {
            socket.setTcpNoDelay(true);
            // CHỐNG DoS (slowloris): ngắt kết nối không gửi dữ liệu quá lâu.
            try {
                if (server.TeamServer.SOCKET_TIMEOUT_MS > 0) {
                    socket.setSoTimeout(server.TeamServer.SOCKET_TIMEOUT_MS);
                }
            } catch (Exception ignored) {
            }
            this.setSocket(socket);
            this.messageHandler = handler;
            this.connected = true;
            SessionManager.instance.add(this);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }

    }

    public Session(Socket s, LogInController instance, boolean b) {
        try {
            this.setSocket(s);
            this.messageHandler = instance;
            this.connected = true;
            this.loginday = 1;
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }

    }

    private byte readKey(byte b) {
        byte i = (byte)(this.key[this.curR] & 255 ^ b & 255);
        this.curR = (this.curR + 1) % this.key.length;
        return i;
    }

    private byte writeKey(byte b) {
        byte i = (byte)(this.key[this.curW] & 255 ^ b & 255);
        this.curW = (this.curW + 1) % this.key.length;
        return i;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public String getIP() {
        return this.socket.getRemoteSocketAddress().toString();
    }

    public void setMessageHandler(ISessionHandler handler) {
        this.messageHandler = handler;
    }

    public Sender getSender() {
        return this.sender;
    }

    protected void setSocket(Socket socket) throws IOException {
        this.socket = socket;
    }

    public void run() {
        this.sender = new Sender();

        try {
            DataInputStream dis = null;

            try {
                if (this.socket != null) {
                    dis = new DataInputStream(this.socket.getInputStream());
                }

                Thread.currentThread().setName("READ ");
            } catch (Exception var9) {
            }

            while(this.connected) {
                Message message = this.readMessage(dis);
                this.lastActiveTime = System.currentTimeMillis();

                // readMessage tra ve null khi doc duoc mot goi rong / khong hieu duoc.
                // Truoc day goi null nay di thang vao processMessage va no ra
                // NullPointerException tai RealController.processMessage - xem
                // errorlog.txt ngay 30/07/2026, ba lan. Bo dem ngap goi tin them sau do
                // cung cham message.command nen loi chi don len som hon chu khong het.
                if (message == null) {
                    continue;
                }

                // Kiem TRUOC khi xu ly: dem sau thi tran ngap da duoc xu ly xong roi.
                if (this.quaNhieuGoi(message.command)) {
                    if (this.boDemGoiTin.vuotXaNguong(message.command)) {
                        break;   // khoi finally ben duoi se disconnect(7)
                    }
                    // Chi vuot tran o muc "nhanh tay": BO goi nay, GIU ket noi.
                    // Cat ket noi o day tung van nham nguoi choi luong thien dang
                    // lam do khi hanh trang day, va moi lan cat la mot vong
                    // luu/nap nhan vat - dung cho co hoi mat du lieu.
                    continue;
                }

                // Bo dem chan doan chieu client -> server (xem DemGoiTin).
                DemGoiTin.ghiVao(this.charname, message.command);
                try {
                    this.messageHandler.processMessage(this, message);
                } catch (Exception var10) {
                }
            }
        } catch (Exception var11) {
        } finally {
            this.disconnect(7);
        }

    }

    /**
     * Ket noi nay co dang gui qua nhieu goi tin khong?
     *
     * Cua so truot that su: cu qua CUA_SO_DEM_MS thi bat dau dem lai tu dau.
     * Lenh tai nguyen (-47 anh nhan vat, -48 quan ao, -49 hieu ung, -51 anh) duoc
     * dem RIENG voi han muc rong hon, vi luc vao ban do moi chung tra ve hang loat
     * mot cach hoan toan binh thuong — nhung khong con la khong gioi han nhu truoc.
     *
     * @return true = vuot nguong, nen cat ket noi
     */
    private boolean quaNhieuGoi(int command) {
        if (!this.boDemGoiTin.vuotNguong(command, System.currentTimeMillis())) {
            return false;
        }
        final boolean cat = this.boDemGoiTin.vuotXaNguong(command);
        // Khi chi BO BOT goi thi ket noi con song, nen moi goi vuot tran deu se sinh
        // ra mot dong log. Chan lai con 1 dong moi cua so 5 giay. Rieng lan CAT ket
        // noi thi luon ghi: no chi xay ra mot lan cho ca ket noi do.
        if (cat || this.boDemGoiTin.xinPhepBao()) {
            final String chiTiet = this.boDemGoiTin.demHienTai(command) + " goi "
                    + (BoDemGoiTin.laLenhTaiNguyen(command) ? "tai nguyen" : "thuong")
                    + " trong " + BoDemGoiTin.CUA_SO_MS + "ms (tran "
                    + this.boDemGoiTin.tranCua(command) + "), lenh cuoi=" + command;
            System.out.println("NGAP GOI TIN: " + (cat ? "cat ket noi " : "bo bot goi cua ")
                    + this.username + " ip=" + this.ip + " (" + chiTiet + ")");
            // Truoc day dau vet nay CHI nam o stdout, tuc la mat sach sau moi lan restart.
            data.NghiVanGianLan.ghiChung(this,
                    (this.charname == null || this.charname.isEmpty()) ? this.username : this.charname,
                    "ngapgoi", (cat ? "CAT KET NOI - " : "bo bot goi - ") + chiTiet);
        }
        return true;
    }

    private Message readMessage(DataInputStream dis) throws Exception {
        Message msg = null;
        int cmd = dis.readByte();
        if (this.key != null) {
            cmd = this.readKey((byte)cmd);
        }

        short size = 0;
        if (this.key != null) {
            byte a1 = dis.readByte();
            a1 = this.readKey(a1);
            byte a2 = dis.readByte();
            a2 = this.readKey(a2);
            size = (short)((a1 & 255) << 8 | a2 & 255);
            if (size > 10000) {
                throw new IOException("Data too big");
            }
        } else {
            size = (short)dis.readUnsignedShort();
            if (size > 10000) {
                throw new IOException("Data too big");
            }
        }

        if (size >= 1024) {
            return null;
        } else {
            byte[] data = new byte[size];
            int byteRead = 0;

            while(byteRead != -1 && byteRead < size) {
                int len = dis.read(data, byteRead, size - byteRead);
                if (len > 0) {
                    byteRead += len;
                }

                if (len == -1) {
                    return null;
                }
            }

            if (this.key != null) {
                for(int i = 0; i < data.length; ++i) {
                    data[i] = this.readKey(data[i]);
                }
            }

            msg = new Message(cmd, data);
            return msg;
        }
    }

    public void sendMessageLogin(Message m) {
        this.sender.sendMessage(m, 8);
    }

    public void sendMessage(Message m) {
        try {
            if (m != null) {
                // Bo dem chan doan - khong lam gi khi khong co tep debug_goi_tin.txt (xem DemGoiTin).
                DemGoiTin.ghiRa(this.charname, m.command);
                for(int i = 0; i < hasGroup.size(); ++i) {
                    if (((Hashtable)hasGroup.get(i)).get(m.command) != null) {
                        this.sender.sendMessage(m, i);
                        return;
                    }
                }

                this.sender.sendMessage(m, 8);
            }
        } catch (Exception e) {
            // Loi thuong gap nhat o day la socket da dong (client vua ngat ket
            // noi) - xay ra RAT thuong xuyen trong mot game online (moi lan
            // logout/doi map/rot mang). In ca stack trace bien log thanh rac,
            // ngap that su nhung loi can chu y (logrotate chi lo dung luong,
            // khong lo NOI DUNG). Chi in 1 dong. Ap dung cung kieu cho hai cho
            // khac trong file nay (sendMessageLogin, Sender.run).
            System.out.println("Session.sendMessage: loi gui du lieu (co the client da ngat ket noi) - " + e);
        }

    }

    public boolean isConnected() {
        return this.connected;
    }

    public void notifySender() {
        try {
            synchronized(this.LOCK) {
                this.LOCK.notify();
            }
        } catch (Exception var3) {
        }

    }

    public void disconnect() {
        if (this.connected) {
            this.connected = false;
        }

    }

    public void disconnect(int cause) {
        this.exit = true;
        this.notifySender();
        if (this.connected) {
            try {
                server.TeamServer.dosOnClose(this.ip);
            } catch (Exception ignored) {
            }
            try {
                this.socket.close();
            } catch (Exception var7) {
            }

            this.connected = false;

            try {
                SessionManager.instance.remove(this);
            } catch (Exception var6) {
            }

            try {
                if (this.userID != -1) {
                    this.messageHandler.onDisconnected(this);
                    this.userID = -1;
                }
            } catch (Exception var5) {
            }
        }

        this.connected = false;
        this.userID = -1;

        try {
            synchronized(this.LOCK) {
                this.LOCK.notify();
            }
        } catch (Exception var4) {
        }

    }

    public void destroyAllMessage() {
        this.sender.destroyAll();
    }

    public boolean is12Tuoi() {
        try {
            Calendar cl = Calendar.getInstance();
            int year = cl.get(1);
            String[] data = Char.split(this.ngaysinh, " ");
            Integer.parseInt(data[0]);
            Integer.parseInt(data[1]);
            if (Integer.parseInt(data[2].trim()) > year - 12) {
                return false;
            }
        } catch (Exception var4) {
        }

        return true;
    }

    public boolean is18Tuoi() {
        try {
            Calendar cl = Calendar.getInstance();
            int year = cl.get(1);
            String[] data = Char.split(this.ngaysinh, " ");
            Integer.parseInt(data[0]);
            Integer.parseInt(data[1]);
            if (Integer.parseInt(data[2].trim()) > year - 18) {
                return false;
            }
        } catch (Exception var4) {
        }

        return true;
    }

    public boolean isVersion180() {
        return this.version.compareTo("1.8.0") > -1;
    }

    private class Sender implements Runnable {
        private LinkedBlockingQueue<byte[]> sendDatas = new LinkedBlockingQueue();

        public Sender() {
            Session.this.lastTimeSendData = System.currentTimeMillis();
            (new Thread(this)).start();
        }

        public void destroyAll() {
            this.sendDatas.clear();
        }

        public void sendMessageLogin(Message m) {
            try {
                byte[] data = m.toByteArray();
                if (Session.this.socket != null) {
                    OutputStream os = Session.this.socket.getOutputStream();
                    os.write(data);
                    os.flush();
                }
            } catch (Exception e) {
                System.out.println("Session.sendMessageLogin: loi gui du lieu (co the client da ngat ket noi) - " + e);
            }

        }

        public void sendMessage(Message m, int index) {
            if (Session.this.connected) {
                try {
                    byte[] data = m.toByteArray();
                    if (data != null) {
                        this.sendDatas.add(data);
                    }
                } catch (Exception var6) {
                    System.out.println("LOI KHI GUI DATA");
                }

                Session.this.lastTimeSendData = System.currentTimeMillis();

                try {
                    synchronized(Session.this.LOCK) {
                        Session.this.LOCK.notify();
                    }
                } catch (Exception var5) {
                }
            }

        }

        public byte[] int2Bytes(int number) {
            byte[] res = new byte[4];

            for(int i = 3; i >= 0; --i) {
                res[i] = (byte)number;
                number >>= 8;
            }

            return res;
        }

        private boolean emtyMsg() {
            return this.sendDatas.isEmpty();
        }

        public void run() {
            try {
                OutputStream os = null;

                try {
                    if (Session.this.socket != null) {
                        os = Session.this.socket.getOutputStream();
                    }

                    Thread.currentThread().setName("SEND ");
                } catch (Exception var27) {
                }

                long timetest = System.currentTimeMillis();

                while(Session.this.connected) {
                    int i = 0;
                    long l = System.currentTimeMillis();

                    try {
                        try {
                            byte[] bytes = (byte[])this.sendDatas.poll(1000L, TimeUnit.MILLISECONDS);
                            if (Session.this.key != null && bytes[0] != -40) {
                                byte[] sdata = new byte[bytes.length];

                                for(int j = 0; j < bytes.length; ++j) {
                                    sdata[j] = Session.this.writeKey(bytes[j]);
                                }

                                os.write(sdata);
                            } else {
                                os.write(bytes);
                            }
                        } catch (Exception var28) {
                        }

                        os.flush();
                    } catch (Exception e) {
                        System.out.println("Session.Sender.run: loi gui du lieu (co the client da ngat ket noi) - " + e);
                        if (os == null) {
                            break;
                        }
                    }

                    if (Session.this.exit) {
                        try {
                            synchronized(Session.this.LOCK) {
                                Session.this.LOCK.notify();
                            }
                        } catch (Exception var26) {
                        }
                        break;
                    }

                    if (os == null) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    Session.this.LOCK.notify();
                } catch (Exception var23) {
                }

            }

            Session.this.exit = true;

            try {
                this.sendDatas.removeAll(this.sendDatas);
            } catch (Exception var24) {
            }

        }
    }
}
