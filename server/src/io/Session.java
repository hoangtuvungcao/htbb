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
    /** So ban client (jar/Android/PC Java gui sau ma may, -1 = khong gui: Unity / client cu). Xem real.DungVatPham. */
    public int banClient = -1;
    /** platf (microedition.platform) client gui luc dang nhap - nen tang cho com.htbb.capnhat.CapNhatClient. */
    public String platfClient = "";
    /** Kha nang client tu bao (so nguyen sau so ban o goi dang nhap; 0 = client cu). Bit io.GoiNen.KHA_NANG_NEN. */
    public volatile int khaNang = 0;
    /** 0.9.15: cac goi client da co trong kho (io.KhoGoi, khai o goi dang nhap); null = client khong co kho. */
    public volatile java.util.Set<String> khoGoi = null;
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

            this.lyDoNgat = "server ngat (dang xuat / kick / dong phien)";
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
                server.ChiSoMayChu.GOI_VAO.incrementAndGet();

                // Kiem TRUOC khi xu ly: dem sau thi tran ngap da duoc xu ly xong roi.
                final int quyet = this.quaNhieuGoi(message.command);
                if (quyet == BoDemGoiTin.CAT) {
                    server.ChiSoMayChu.CAT_NGAP.incrementAndGet();
                    this.lyDoNgat = "ngap goi tin (lenh " + message.command + ")";
                    break;   // khoi finally ben duoi se disconnect(7)
                }
                if (quyet == BoDemGoiTin.BO) {
                    server.ChiSoMayChu.GOI_BO.incrementAndGet();
                    // Chi vuot tran o muc "nhanh tay": BO goi nay, GIU ket noi.
                    // Cat ket noi o day tung van nham nguoi choi luong thien dang
                    // lam do khi hanh trang day, va moi lan cat la mot vong
                    // luu/nap nhan vat - dung cho co hoi mat du lieu.
                    continue;
                }
                if (quyet == BoDemGoiTin.HOAN) {
                    // Lenh tai nguyen vuot tran: ha nhip chinh ket noi nay, KHONG bo goi (xem
                    // BoDemGoiTin.phanLoai - goi tai nguyen bi bo la anh mat han / cho 30 s).
                    HOAN_TAI_NGUYEN.incrementAndGet();
                    try {
                        Thread.sleep(BoDemGoiTin.hoanMs(message.command));
                    } catch (InterruptedException ignored) {
                    }
                }

                this.theoDoiVaoGame(message.command);
                if (message.command == GoiManh.LENH_MANH) {
                    this.nhanBaoLoiTai(message);   // client 0.9.7.1+ bao loi tai (khong qua bo xu ly game)
                    continue;
                }
                // Bo dem chan doan chieu client -> server (xem DemGoiTin).
                DemGoiTin.ghiVao(this.charname, message.command);
                try {
                    this.messageHandler.processMessage(this, message);
                } catch (java.io.EOFException var10) {
                    real.GoiTinCut.dem(this.charname, message.command);   // goi cut ngan: bo goi, khong phai loi may chu
                } catch (Exception var10) {
                    server.ChiSoMayChu.LOI_XU_LY.incrementAndGet();
                    // Truoc day nuot im lang: loi xu ly goi khong de lai dau vet nao. Nhat chi
                    // in stack LAN DAU moi loai loi / 10 phut, cac lan sau gop thanh so dem.
                    com.htbb.log.Nhat.loi("XU_LY_GOI", "lenh " + message.command + " cua " + this.charname, var10);
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            this.lyDoNgat = "het gio im lang " + (server.TeamServer.SOCKET_TIMEOUT_MS / 1000) + " s (client khong gui gi)";
        } catch (java.io.EOFException e) {
            this.lyDoNgat = "client dong ket noi";
        } catch (java.net.SocketException e) {
            this.lyDoNgat = this.connected ? "loi socket: " + e.getMessage() : this.lyDoNgat;
        } catch (Exception var11) {
            this.lyDoNgat = "loi: " + var11;
        } finally {
            if (this.lucDangNhap > 0 && this.lucVaoMap == 0) {
                // ngat truoc khi vao duoc ban do: ghi buoc dang ket (client J2ME ket man tai roi nguoi choi tat)
                this.lyDoNgat = this.lyDoNgat + "; " + this.moTaVaoGame(System.currentTimeMillis(), "ngat khi chua vao ban do");
            }
            baoNgat(this.charname, this.username, this.ip, this.lyDoNgat, this.lastActiveTime);
            this.disconnect(7);
        }

    }

    // ------------------------------------------------------------------ theo doi dang nhap -> vao ban do (0.9.7.1)
    /**
     * Su co 22/10/2026: client J2ME tren gia lap iOS dang nhap xong roi ket o man tai, nhat ky chi co dong [CLIENT] -
     * khong biet ket o dau (goi giu ket noi 30 s giu phien song nen khong co ca dong [NGAT]). Nay moi phien ghi:
     * luc dang nhap (LoginHandler), luc chon Pokemon (lenh 13), luc vao ban do xong (lenh 12 = changeMapOK sau khi
     * client nap xong ban do) va so byte da gui; qua VAO_GAME_CHAM_MS ma chua vao duoc ban do -> MOT dong [VAO_GAME]
     * canh bao (buoc dang ket, byte da gui, nen tang); client khong nen (J2ME) vao duoc -> mot dong thoi gian.
     */
    public static long VAO_GAME_CHAM_MS = 90000L;
    public volatile long lucDangNhap;
    private volatile long lucChonNhanVat;
    private volatile long lucVaoMap;
    private volatile boolean daBaoCham;
    /** byte da ghi ra socket (Sender.run) */
    public final java.util.concurrent.atomic.AtomicLong byteDaGui = new java.util.concurrent.atomic.AtomicLong();

    void theoDoiVaoGame(final int lenh) {
        try {
            if (this.lucDangNhap <= 0 || this.lucVaoMap > 0) {
                return;
            }
            final long now = System.currentTimeMillis();
            if (lenh == 13 && this.lucChonNhanVat == 0) {
                this.lucChonNhanVat = now;
            } else if (lenh == 12 && this.lucChonNhanVat > 0) {
                this.lucVaoMap = now;
                if ((this.khaNang & GoiNen.KHA_NANG_NEN) == 0 || this.daBaoCham) {
                    com.htbb.log.Nhat.thongTin("VAO_GAME", moTaVaoGame(now, "vao ban do"));
                }
                return;
            }
            if (!this.daBaoCham && now - this.lucDangNhap > VAO_GAME_CHAM_MS) {
                this.daBaoCham = true;
                com.htbb.log.Nhat.canhBao("VAO_GAME", moTaVaoGame(now, "CHUA vao duoc ban do"));
            }
        } catch (Throwable ignored) {
        }
    }

    private int soBaoLoiTai;

    /**
     * Goi -123 tu client (0.9.7.1+): client bao man tai hong (thieu bo nho / dung qua lau) - ly do, heap tong / trong
     * (KB), buoc man tai, so lan da giai phong bo nho, so goi da bo. Toi da 5 dong / phien.
     */
    void nhanBaoLoiTai(final Message m) {
        try {
            if (++this.soBaoLoiTai > 5) {
                return;
            }
            final String lyDo = m.dis.readUTF();
            final int tong = m.dis.readInt();
            final int trong = m.dis.readInt();
            final int buoc = m.dis.readByte();
            final int giai = m.dis.readShort();
            final int bo = m.dis.readShort();
            com.htbb.log.Nhat.canhBao("LOI_TAI_CLIENT", "tk " + com.htbb.log.Nhat.sach(this.username) + ": " + com.htbb.log.Nhat.sach(lyDo)
                    + " | heap " + trong + "/" + tong + " KB trong, buoc man tai " + buoc + ", giai phong " + giai + " lan, bo " + bo
                    + " goi | " + moTaVaoGame(System.currentTimeMillis(), "bao loi"));
        } catch (Throwable ignored) {
        }
    }

    /** Dong mo ta tien trinh vao game (cung dung khi ngat ket noi truoc khi vao duoc ban do). */
    String moTaVaoGame(final long now, final String viec) {
        final String buoc = this.lucVaoMap > 0 ? "da vao ban do" : this.lucChonNhanVat > 0 ? "dang tai Pokemon / ban do (da chon)" : "chua chon / tao Pokemon";
        return "tk " + com.htbb.log.Nhat.sach(this.username) + " " + viec + " sau " + (now - this.lucDangNhap) / 1000L + " s ("
                + buoc + (this.lucChonNhanVat > 0 ? ", chon luc +" + (this.lucChonNhanVat - this.lucDangNhap) / 1000L + " s" : "")
                + "), da gui " + this.byteDaGui.get() / 1024L + " KB, platf=" + com.htbb.log.Nhat.sach(this.platfClient)
                + " ban=" + this.banClient + " khaNang=" + this.khaNang + " w=" + this.w;
    }

    /** So goi tai nguyen da duoc HOAN (cho roi xu ly, khong bo) tu luc khoi dong - chi so chan doan. */
    public static final java.util.concurrent.atomic.AtomicLong HOAN_TAI_NGUYEN = new java.util.concurrent.atomic.AtomicLong();

    /**
     * Ket noi nay co dang gui qua nhieu goi tin khong?
     *
     * Cua so truot that su: cu qua CUA_SO_DEM_MS thi bat dau dem lai tu dau.
     * Lenh tai nguyen (-47 anh nhan vat, -48 quan ao, -49 hieu ung, -51 anh) duoc
     * dem RIENG voi han muc rong hon, vi luc vao ban do moi chung tra ve hang loat
     * mot cach hoan toan binh thuong — nhung khong con la khong gioi han nhu truoc.
     *
     * @return BoDemGoiTin.QUA / BO / CAT / HOAN (xem BoDemGoiTin.phanLoai)
     */

    private int quaNhieuGoi(int command) {
        if (BoDemGoiTin.mienIp(this.ip)) {
            return BoDemGoiTin.QUA; // sv.tranGoiMienIp: chi may chay bot tai
        }
        final int quyet = this.boDemGoiTin.phanLoai(command, System.currentTimeMillis());
        if (quyet == BoDemGoiTin.QUA) {
            return quyet;
        }
        final boolean cat = quyet == BoDemGoiTin.CAT;
        // Khi chi BO BOT goi thi ket noi con song, nen moi goi vuot tran deu se sinh
        // ra mot dong log. Chan lai con 1 dong moi cua so 5 giay. Rieng lan CAT ket
        // noi thi luon ghi: no chi xay ra mot lan cho ca ket noi do.
        if (cat || this.boDemGoiTin.xinPhepBao()) {
            final String chiTiet = this.boDemGoiTin.demHienTai(command) + " goi "
                    + BoDemGoiTin.tenLoai(command)
                    + " trong " + BoDemGoiTin.CUA_SO_MS + "ms (tran "
                    + this.boDemGoiTin.tranCua(command) + "), lenh cuoi=" + command;
            final String viec = cat ? "cat ket noi " : quyet == BoDemGoiTin.HOAN ? "ha nhip (khong bo) tai nguyen cua " : "bo bot goi cua ";
            com.htbb.log.Nhat.canhBao("NGAP_GOI", viec + this.username + " ip=" + this.ip + " (" + chiTiet + ")");
            // Truoc day dau vet nay CHI nam o stdout, tuc la mat sach sau moi lan restart.
            // Ha nhip tai nguyen KHONG ghi nghi van gian lan: client cache rong vao ban do dong
            // nguoi cham tran nay mot cach luong thien.
            if (quyet != BoDemGoiTin.HOAN) {
                data.NghiVanGianLan.ghiChung(this,
                        (this.charname == null || this.charname.isEmpty()) ? this.username : this.charname,
                        "ngapgoi", (cat ? "CAT KET NOI - " : "bo bot goi - ") + chiTiet);
            }
        }
        return quyet;
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
                // 0.9.15: goi du lieu lon client da co trong kho -> goi "dung kho" nho thay the (io.KhoGoi)
                m = KhoGoi.thayThe(this, m);
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
            com.htbb.log.Nhat.canhBao("MANG", "Session.sendMessage: loi gui du lieu (co the client da ngat ket noi) - " + e);
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

    /** 0.9.4: ly do ngat ket noi (ghi MOT dong / ket noi vao nhat ky chinh). */
    private volatile String lyDoNgat = "chua doc goi nao";
    private static long ngatCuaSo;
    private static int ngatTrongCuaSo, ngatBoQua;

    /** Mot dong "[NGAT] nhan vat ip ly do" moi lan ngat; toi da 30 dong / 10 s, phan du gop thanh so dem. */
    static void baoNgat(String charname, String username, String ip, String lyDo, long lanCuoi) {
        try {
            final long now = System.currentTimeMillis();
            int boQua = 0;
            synchronized (Session.class) {
                if (now - ngatCuaSo > 10000L) {
                    boQua = ngatBoQua;
                    ngatCuaSo = now;
                    ngatTrongCuaSo = 0;
                    ngatBoQua = 0;
                }
                if (++ngatTrongCuaSo > 30) {
                    ngatBoQua++;
                    return;
                }
            }
            if (boQua > 0) {
                com.htbb.log.Nhat.thongTin("NGAT", "(bo qua " + boQua + " dong ngat ket noi trong 10 s truoc)");
            }
            final String ten = charname != null && charname.length() > 0 ? charname : (username != null && username.length() > 0 ? "tk " + username : "(chua dang nhap)");
            final long im = lanCuoi > 0 ? (now - lanCuoi) / 1000L : -1L;
            com.htbb.log.Nhat.thongTin("NGAT", ten + " ip=" + ip + " ly do: " + lyDo + (im >= 0 ? " (goi cuoi " + im + " s truoc)" : ""));
        } catch (Throwable ignored) {
        }
    }

    public void disconnect() {
        if (this.connected) {
            this.connected = false;
        }

    }

    /**
     * 31/10/2026: đóng phiên KÈM lý do - gửi thông báo (lệnh 37) trước, đóng socket sau ~0,4 s để gói kịp ra (disconnect ngay
     * đóng socket, gói còn trong hàng đợi bị mất). Client 0.9.4+ thấy thông báo ngay trước khi rớt thì không tự nối lại
     * (0.9.25+: nhận ra "đăng nhập ở thiết bị khác" -> chỉ nút Kết nối lại).
     */
    public void dongVoiLyDo(final String lyDo, final int cause) {
        dongVoiLyDo(lyDo, new Runnable() {
            public void run() {
                Session.this.disconnect(cause);
            }
        });
    }

    /**
     * Như trên nhưng việc đóng do người gọi đưa (vd CharManager.kickPlayer: lưu + gỡ nhân vật rồi đóng phiên) - chạy sau
     * ~0,4 s, khi thông báo đã ra khỏi hàng đợi gửi.
     */
    public void dongVoiLyDo(final String lyDo, final Runnable dong) {
        try {
            this.sendMessage(real.MessageCreator.createServerAlertMessage(lyDo, ""));
        } catch (Exception ignored) {
        }
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(400L);
                } catch (InterruptedException ignored) {
                }
                try {
                    dong.run();
                } catch (Exception e) {
                    com.htbb.log.Nhat.canhBao("MANG", "Session.dongVoiLyDo: " + e);
                }
            }
        }, "dong-ly-do");
        t.setDaemon(true);
        t.start();
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

    // is12Tuoi/is18Tuoi duoc goi o moi tin chat (Char.isAcountTest): truoc day moi lan tao Calendar +
    // tach chuoi ngaysinh (~150 MB rac / 5 phut o 300 nguoi). Nay nho nam sinh theo chuoi ngaysinh
    // (doi chuoi thi doc lai) va nam hien tai (lam moi moi phut). Ket qua giu nguyen: ngaysinh sai
    // dinh dang -> true nhu cu.
    private String nsDaDoc = null;
    private int namSinhDaDoc = Integer.MIN_VALUE; // MIN_VALUE = khong doc duoc -> coi nhu du tuoi
    private static volatile int namHienTai = 0;
    private static volatile long namHetHanMs = 0L;

    private static int namHienTai() {
        final long now = System.currentTimeMillis();
        if (now >= namHetHanMs) {
            namHienTai = Calendar.getInstance().get(1);
            namHetHanMs = now + 60000L;
        }
        return namHienTai;
    }

    private int namSinh() {
        final String ns = this.ngaysinh;
        if (ns != this.nsDaDoc) {
            int nam = Integer.MIN_VALUE;
            try {
                String[] data = Char.split(ns, " ");
                Integer.parseInt(data[0]);
                Integer.parseInt(data[1]);
                nam = Integer.parseInt(data[2].trim());
            } catch (Exception var4) {
                nam = Integer.MIN_VALUE;
            }
            this.namSinhDaDoc = nam;
            this.nsDaDoc = ns;
        }
        return this.namSinhDaDoc;
    }

    public boolean is12Tuoi() {
        final int nam = this.namSinh();
        return nam == Integer.MIN_VALUE || nam <= namHienTai() - 12;
    }

    public boolean is18Tuoi() {
        final int nam = this.namSinh();
        return nam == Integer.MIN_VALUE || nam <= namHienTai() - 18;
    }

    public boolean isVersion180() {
        return this.version.compareTo("1.8.0") > -1;
    }

    /**
     * Tran hang doi gui moi ket noi (so goi). Truoc day KHONG tran: client treo/mang qua cham ma
     * server van day goi (phat song khu dong nguoi ~20 goi/s) thi hang doi phinh toi khi het heap.
     * Vuot tran = client khong doc nua -> dong socket (luong READ tu don dep qua disconnect(7)).
     * -Dhtbb.tranHangDoiGui=N (mac dinh 8192 ~ vai phut goi binh thuong).
     */
    public static final int TRAN_HANG_DOI_GUI = Integer.getInteger("htbb.tranHangDoiGui", 8192);
    /** So ket noi bi dong vi tran hang doi gui (chi so cho trang thai/kiem tai). */
    public static final java.util.concurrent.atomic.AtomicLong CAT_TRAN_GUI = new java.util.concurrent.atomic.AtomicLong();
    /**
     * Khoang toi thieu giua hai lan ghi socket cua MOT ket noi (ms) - gom goi luc dong (xem Sender.run).
     * -Dhtbb.gomGuiMs=N (mac dinh 10; 0 = ghi ngay tung dot nhu cu). Chi them tre khi ket noi dang dong.
     */
    public static final int GOM_GUI_MS = Math.max(0, Math.min(100, Integer.getInteger("htbb.gomGuiMs", 10)));
    /** "Dang dong" = it nhat bay nhieu lan ghi trong 100 ms (mac dinh 8 = 80 lan ghi / s). -Dhtbb.gomGuiNguong=N. */
    public static final int GOM_GUI_NGUONG = Math.max(1, Integer.getInteger("htbb.gomGuiNguong", 8));

    private class Sender implements Runnable {
        private LinkedBlockingQueue<byte[]> sendDatas = new LinkedBlockingQueue(TRAN_HANG_DOI_GUI);
        private volatile boolean daBaoTran = false;
        private boolean daBaoLoiGhi = false;

        /** Them vao hang doi; day = client khong doc -> dong socket MOT lan (khong chan luong game). */
        private void xepHang(byte[] data) {
            if (!this.sendDatas.offer(data) && !this.daBaoTran) {
                this.daBaoTran = true;
                CAT_TRAN_GUI.incrementAndGet();
                com.htbb.log.Nhat.canhBao("MANG", "Session: hang doi gui day (" + TRAN_HANG_DOI_GUI
                        + " goi) - client " + Session.this.username + " ip=" + Session.this.ip + " khong doc nua, dong ket noi");
                try {
                    Session.this.socket.close();
                } catch (Exception ignored) {
                }
            }
        }

        /** Goi lon cho client co giai nen: boc thanh goi nen (io.GoiNen). Mang goc KHONG bi sua (goi phat chung). */
        private byte[] nenNeuDuoc(final byte[] data) {
            if ((Session.this.khaNang & GoiNen.KHA_NANG_NEN) == 0 || data.length < GoiNen.NGUONG) {
                return data;
            }
            try {
                final byte[] nen = GoiNen.boc(data);
                return nen != null ? nen : data;
            } catch (Throwable e) {
                return data;
            }
        }

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
                    // Goi kem (xem Message.kemTheo) - ghi ngay sau.
                    for (Message kem = m.kemTheo; kem != null; kem = kem.kemTheo) {
                        os.write(kem.toByteArray());
                    }
                    os.flush();
                }
            } catch (Exception e) {
                com.htbb.log.Nhat.canhBao("MANG", "Session.sendMessageLogin: loi gui du lieu (co the client da ngat ket noi) - " + e);
            }

        }

        /**
         * Xep mot khung goi: nen neu client giai nen duoc (io.GoiNen), khong thi chia manh neu client gom duoc
         * (io.GoiManh - client J2ME 0.9.7.1+), con lai gui nguyen. Goi duoi khoa this (sendMessage).
         */
        private void xepKhung(final byte[] data) {
            final byte[] ra = this.nenNeuDuoc(data);
            if (ra == data && (Session.this.khaNang & GoiManh.KHA_NANG_MANH) != 0 && data.length >= GoiManh.NGUONG) {
                byte[][] manh = null;
                try {
                    manh = GoiManh.chia(data);
                } catch (Throwable e) {
                    manh = null;
                }
                if (manh != null) {
                    for (int i = 0; i < manh.length; i++) {
                        this.xepHang(manh[i]);
                    }
                    return;
                }
            }
            this.xepHang(ra);
        }

        public void sendMessage(Message m, int index) {
            if (Session.this.connected) {
                try {
                    byte[] data = m.toByteArray();
                    if (data != null) {
                        // khoa: cac manh cua mot goi (va goi kem) nam LIEN TIEP trong hang doi du nhieu luong cung gui
                        synchronized (this) {
                            this.xepKhung(data);
                            server.ChiSoMayChu.GOI_RA.increment();
                            // Goi kem (xem Message.kemTheo): xep NGAY SAU goi
                            // chinh trong cung hang doi, client nhan dung thu tu.
                            for (Message kem = m.kemTheo; kem != null; kem = kem.kemTheo) {
                                byte[] dataKem = kem.toByteArray();
                                if (dataKem != null) {
                                    this.xepKhung(dataKem);
                                }
                            }
                        }
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

                // Gom goi: lay mot goi (cho toi da 1 s nhu cu), roi vet them cac goi DA SAN trong hang
                // doi (toi da 256) vao MOT bo dem dung lai, ma hoa thang vao bo dem, ghi + flush MOT lan.
                // Truoc day moi goi: cap phat mang ma hoa moi + write + flush rieng (= 1 syscall/goi,
                // ~6.000 goi/s o 300 nguoi). Thu tu goi va khoa ma hoa (curW) giu nguyen; goi -40
                // (gui khoa) van khong ma hoa.
                byte[] buf = new byte[8192];
                long lanGhiCuoi = 0L;
                long mocDemGhi = 0L;
                int soGhiCuaSo = 0;

                while(Session.this.connected) {
                    byte[] bytes = null;
                    try {
                        bytes = (byte[])this.sendDatas.poll(1000L, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignored) {
                    }

                    // Kiem lag 10/2026: o ban do dong (100 nguoi dung chung, moi nguoi di 4 buoc/s) moi ket noi
                    // nhan ~250 goi vi tri / s, moi goi mot lan danh thuc luong SEND + mot write + flush:
                    // 100 bot = 115% CPU, trong do 75% la system (syscall + chuyen ngu canh). Nay: khi ket noi
                    // DANG DONG (>= GOM_GUI_NGUONG lan ghi trong 100 ms vua qua) va lan ghi truoc moi cach
                    // < GOM_GUI_MS thi cho toi moc do roi gom ca hang doi vao MOT lan ghi. Ket noi thua goi
                    // (1 nguoi choi, vai goi / s) KHONG bao gio cho - tre phan hoi giu nguyen (do: 1 ms).
                    // Luc dong toi da them GOM_GUI_MS. Thu tu / noi dung goi giu nguyen.
                    if (bytes != null && GOM_GUI_MS > 0) {
                        final long now = System.currentTimeMillis();
                        if (now - mocDemGhi >= 100L) {
                            mocDemGhi = now;
                            soGhiCuaSo = 0;
                        }
                        final long cho = lanGhiCuoi + GOM_GUI_MS - now;
                        if (soGhiCuaSo >= GOM_GUI_NGUONG && cho > 0 && cho <= GOM_GUI_MS) {
                            try {
                                Thread.sleep(cho);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }

                    if (bytes != null && os != null) {
                        try {
                            int n = 0;
                            int soGoi = 0;
                            long tongGhi = 0;
                            do {
                                tongGhi += bytes.length;
                                final boolean ma = Session.this.key != null && bytes.length > 0 && bytes[0] != -40;
                                int off = 0;
                                final int len = bytes.length;
                                while (off < len) {
                                    if (n == buf.length) {
                                        os.write(buf, 0, n);
                                        n = 0;
                                    }
                                    final int c = Math.min(len - off, buf.length - n);
                                    if (ma) {
                                        for (int j = 0; j < c; ++j) {
                                            buf[n + j] = Session.this.writeKey(bytes[off + j]);
                                        }
                                    } else {
                                        System.arraycopy(bytes, off, buf, n, c);
                                    }
                                    n += c;
                                    off += c;
                                }
                            } while (++soGoi < 256 && (bytes = (byte[])this.sendDatas.poll()) != null);

                            if (n > 0) {
                                os.write(buf, 0, n);
                            }
                            os.flush();
                            lanGhiCuoi = System.currentTimeMillis();
                            soGhiCuaSo++;
                            Session.this.byteDaGui.addAndGet(tongGhi);
                        } catch (Exception e) {
                            // Socket da dong (client ngat): luong READ se disconnect. Chi bao mot lan
                            // moi ket noi (truoc day moi goi/moi giay mot dong).
                            if (!this.daBaoLoiGhi) {
                                this.daBaoLoiGhi = true;
                                com.htbb.log.Nhat.canhBao("MANG", "Session.Sender.run: loi gui du lieu (co the client da ngat ket noi) - " + e);
                            }
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
