package network;

import game.model.Cout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public class Session_ME implements ISession {
    private DataOutputStream dos;
    public DataInputStream dis;
    public IMessageHandler messageHandler;
    private SocketConnection sc;
    public boolean connected;
    public boolean connecting;
    public Thread initThread;
    public Thread collectorThread;
    public Thread sendThread;
    public int sendByteCount;
    public int recvByteCount;
    boolean getKeyComplete;
    private byte curR;
    private byte curW;
    long timeConnected;
    public static boolean isCancel;
    protected static Session_ME instance = new Session_ME();
    public static int countRead = 0;
    private final Sender sender = new Sender();
    public byte[] key = null;
    public String strRecvByteCount = "";

    /** Lệnh gói nén (server io.GoiNen.LENH_NEN). */
    public static final byte GOI_NEN = -127;
    /** Bit khả năng gửi kèm gói đăng nhập: giải nén được gói -127. */
    public static final int KHA_NANG_NEN = 1;
    /** 0.9.7: bộ giải nén - PC / Android có java.util.zip (network.GiaiNenZip), J2ME không có lớp đó -> null, không báo khả năng. */
    static GiaiNen giaiNen;
    /** số gói nén đã nhận (kiểm thử) */
    public static int soGoiNen;

    static {
        try {
            giaiNen = (GiaiNen) Class.forName("network.GiaiNenZip").newInstance();
        } catch (Throwable e) {
            giaiNen = null;
        }
    }

    /** Lệnh gói mảnh (server io.GoiManh.LENH_MANH): gói lớn chia nhiều gói nhỏ liên tiếp. Client -> server: báo lỗi tải. */
    public static final byte GOI_MANH = -123;
    /** Bit khả năng: gom được gói mảnh -123 (0.9.7.1, mọi bản - J2ME không có zip nên server chia mảnh thay vì nén). */
    public static final int KHA_NANG_MANH = 2;

    /** Số nguyên khả năng gửi sau số bản ở gói đăng nhập (server LoginHandler -> Session.khaNang). */
    public static int khaNang() {
        return (giaiNen != null ? KHA_NANG_NEN : 0) | KHA_NANG_MANH;
    }

    /** Gói đánh dấu (không phải gói thật): đã bỏ một gói không cấp phát được / đang gom mảnh. */
    static final Message BO_QUA = new Message(), CHO_MANH = new Message();
    /** số gói đã bỏ vì thiếu bộ nhớ / số gói mảnh đã gom (kiểm thử) */
    public static int soGoiBo, soGoiManh;
    /** đang gom mảnh: lệnh gốc, số byte còn thiếu, các mảnh */
    private byte manhLenh;
    private int manhCon = -1;
    private Vector manh;
    private Vector manhDau;

    /**
     * 0.9.7.1: báo lỗi tải về máy chủ (gói -123 client -> server: lý do, heap tổng / trống KB, bước màn tải, số lần
     * thiếu bộ nhớ) - để nhật ký máy chủ thấy được client kẹt vì sao. Máy chủ cũ bỏ qua lệnh lạ.
     */
    public static void baoLoiTai(String lyDo) {
        try {
            if (!instance.connected || !instance.getKeyComplete) {
                return;
            }
            Message m = new Message(GOI_MANH);
            m.writer().writeUTF(lyDo == null ? "" : (lyDo.length() > 200 ? lyDo.substring(0, 200) : lyDo));
            Runtime r = Runtime.getRuntime();
            m.writer().writeInt((int) (r.totalMemory() >> 10));
            m.writer().writeInt((int) (r.freeMemory() >> 10));
            m.writer().writeByte(game.pokemon.ManTai.buoc);
            m.writer().writeShort(game.pokemon.BoNho.soLanGiai);
            m.writer().writeShort(soGoiBo);
            instance.sendMessage(m);
        } catch (Throwable e) {
            // bỏ qua
        }
    }
    int countMsg = 0;

    public static Session_ME gI() {
        return instance;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void setHandler(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void connect(String host) {
        if (this.connected || this.connecting) {
            return;
        }
        this.getKeyComplete = false;
        this.sc = null;
        this.initThread = new Thread(new NetworkInit(host));
        this.initThread.start();
    }

    class NetworkInit implements Runnable {
        private final String host;

        NetworkInit(String host) {
            this.host = host;
        }

        @Override
        public void run() {
            Session_ME.isCancel = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(20000L);
                    } catch (InterruptedException e) {
                    }
                    if (Session_ME.this.connecting) {
                        try {
                            Session_ME.this.sc.close();
                        } catch (Exception e2) {
                        }
                        Session_ME.isCancel = true;
                        Session_ME.this.connecting = false;
                        Session_ME.this.connected = false;
                        Session_ME.this.messageHandler.onConnectionFail();
                    }
                }
            }).start();
            Session_ME.this.connecting = true;
            Session_ME.this.connected = true;
            try {
                doConnect(this.host);
                Session_ME.this.messageHandler.onConnectOK();
            } catch (Exception e) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e2) {
                }
                if (!Session_ME.isCancel && Session_ME.this.messageHandler != null) {
                    Session_ME.this.close();
                    Session_ME.this.messageHandler.onConnectionFail();
                }
            }
        }

        public void doConnect(String host) throws Exception {
            Session_ME.this.sc = (SocketConnection) Connector.open(host);
            Session_ME.this.dos = Session_ME.this.sc.openDataOutputStream();
            Session_ME.this.dis = Session_ME.this.sc.openDataInputStream();
            new Thread(Session_ME.this.sender).start();
            Session_ME.this.collectorThread = new Thread(new MessageCollector());
            Session_ME.this.collectorThread.start();
            Session_ME.this.timeConnected = System.currentTimeMillis();
            Session_ME.this.doSendMessage(new Message((byte) -40));
            Session_ME.this.connecting = false;
        }
    }

    @Override
    public void sendMessage(Message message) {
        this.sender.AddMessage(message);
    }

    /** 0.9.4: lúc gửi gói cuối (giữ kết nối: server cắt socket im lặng quá sv.socketTimeout = 180 s). */
    public volatile long lucGuiCuoi = System.currentTimeMillis();
    /** Chu kỳ gói giữ kết nối (ms). */
    public static long GIU_KET_NOI_MS = 30000L;
    /** số gói giữ kết nối đã gửi (kiểm thử) */
    public static int soGoiGiu;

    public synchronized void doSendMessage(Message m) throws IOException {
        this.lucGuiCuoi = System.currentTimeMillis();
        byte[] data = m.getData();
        try {
            // Kiem lag 10/2026: dong MOT khung roi ghi MOT lan. Truoc day writeByte lenh / 2 byte do dai / du lieu
            // rieng tung lan tren socket NODELAY = 4 lan ghi (4 goi TCP) cho moi goi tin. Byte gui di y het.
            int size = data != null ? data.length : 0;
            byte[] khung = new byte[3 + size];
            khung[0] = this.getKeyComplete ? writeKey(m.command) : m.command;
            if (data != null) {
                if (this.getKeyComplete) {
                    khung[1] = writeKey((byte) (size >> 8));
                    khung[2] = writeKey((byte) (size & 255));
                    for (int i = 0; i < data.length; i++) {
                        data[i] = writeKey(data[i]);
                    }
                } else {
                    khung[1] = (byte) (size >> 8);
                    khung[2] = (byte) size;
                }
                System.arraycopy(data, 0, khung, 3, size);
                this.sendByteCount += 5 + data.length;
            } else {
                // nhu cu: writeShort(0) khong qua ma hoa
                this.sendByteCount += 5;
            }
            this.dos.write(khung);
            this.dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte readKey(byte b) {
        byte[] bArr = this.key;
        byte b2 = this.curR;
        this.curR = (byte) (b2 + 1);
        byte i = (byte) ((bArr[b2] & 255) ^ (b & 255));
        if (this.curR >= this.key.length) {
            this.curR = (byte) (this.curR % this.key.length);
        }
        return i;
    }

    private byte writeKey(byte b) {
        byte[] bArr = this.key;
        byte b2 = this.curW;
        this.curW = (byte) (b2 + 1);
        byte i = (byte) ((bArr[b2] & 255) ^ (b & 255));
        if (this.curW >= this.key.length) {
            this.curW = (byte) (this.curW % this.key.length);
        }
        return i;
    }

    private class Sender implements Runnable {
        private final Vector sendingMessage = new Vector();

        public Sender() {
        }

        public void AddMessage(Message message) {
            this.sendingMessage.addElement(message);
            // Kiem lag 10/2026: danh thuc luong gui ngay (truoc day ngu 10 ms giua hai lan xem hang doi -> moi thao tac
            // tre them toi 10 ms truoc khi gui di)
            synchronized (this) {
                this.notify();
            }
        }

        @Override
        public void run() {
            while (Session_ME.this.connected) {
                try {
                    if (Session_ME.this.getKeyComplete) {
                        while (this.sendingMessage.size() > 0) {
                            Message m = (Message) this.sendingMessage.elementAt(0);
                            this.sendingMessage.removeElementAt(0);
                            Session_ME.this.doSendMessage(m);
                        }
                        // 0.9.4 GIỮ KẾT NỐI: luồng gửi riêng (không phụ thuộc vẽ / cửa sổ mất focus / hộp thoại đang mở)
                        // gửi gói 11 rỗng nếu 30 s chưa gửi gì. Server: gói 11 trong màn chơi bị bỏ qua vô hại
                        // (MapXuLyLenh), nhưng đọc được gói = còn sống, không bị cắt sau sv.socketTimeout.
                        if (System.currentTimeMillis() - Session_ME.this.lucGuiCuoi >= GIU_KET_NOI_MS) {
                            Session_ME.this.doSendMessage(new Message((byte) 11));
                            soGoiGiu++;
                        }
                    }
                    try {
                        synchronized (this) {
                            if (this.sendingMessage.size() == 0) {
                                this.wait(10L);
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                    return;
                }
            }
        }
    }

    class MessageCollector implements Runnable {
        MessageCollector() {
        }

        @Override
        public void run() {
            try {
                while (Session_ME.this.isConnected()) {
                    Message message = readMessage();
                    if (message == null) {
                        break;
                    }
                    if (message == CHO_MANH) {
                        continue;
                    }
                    if (message == BO_QUA) {
                        game.pokemon.ManTai.baoLoi("Không đủ bộ nhớ để nhận dữ liệu từ máy chủ.");
                        continue;
                    }
                    // 0.9.15 kho goi (KhoGoi): -117 kieu 0 = luu goi ke tiep, kieu 1 = goi lay tu kho (hong -> ngat, noi lai)
                    if (message.command == KhoGoi.LENH) {
                        message = KhoGoi.nhan(message);
                        if (message == null) {
                            break;
                        }
                        if (message == KhoGoi.BO_QUA) {
                            continue;
                        }
                    }
                    xuLy(message);
                    KhoGoi.sauXuLy(message);
                }
            } catch (Throwable e2) {
                // 0.9.7.1: bắt cả Error (OutOfMemoryError...) - trước chỉ bắt Exception: luồng mạng chết im lặng, không
                // báo mất kết nối, màn tải quay mãi (gói giữ kết nối vẫn chạy nên máy chủ cũng không thấy gì)
                game.pokemon.GiamSat.loi("luong mang", e2);
            }
            if (Session_ME.this.connected) {
                Cout.println("dang ket noi ");
                if (Session_ME.this.messageHandler != null) {
                    if (System.currentTimeMillis() - Session_ME.this.timeConnected > 500) {
                        Session_ME.this.messageHandler.onDisconnected();
                    } else {
                        Session_ME.this.messageHandler.onConnectionFail();
                    }
                }
                if (Session_ME.this.sc == null) {
                    return;
                }
                Session_ME.this.cleanNetwork();
            }
        }

        /**
         * Xử lý một gói. 0.9.7.1: thiếu bộ nhớ khi xử lý -> giải phóng (BoNho.giai) rồi xử lý LẠI gói từ đầu một lần;
         * vẫn hỏng thì báo lỗi tải (hộp lỗi + Thử lại) thay vì bỏ gói im lặng.
         */
        private void xuLy(Message message) {
            for (int lan = 0; lan < 2; lan++) {
                try {
                    if (message.command == -40) {
                        getKey(message);
                    } else {
                        Session_ME.this.messageHandler.onMessage(message);
                    }
                    return;
                } catch (OutOfMemoryError e) {
                    game.pokemon.GiamSat.loi("goi tin lenh " + message.command, e);
                    if (lan == 0 && message.tuDau()) {
                        game.pokemon.BoNho.giai("xu ly goi " + message.command);
                        continue;
                    }
                    game.pokemon.ManTai.baoLoi("Không đủ bộ nhớ để xử lý dữ liệu (lệnh " + message.command + ").");
                    return;
                } catch (Throwable e) {
                    // 0.9.4: lỗi một gói chỉ bỏ gói đó (ghi nhật ký kèm mã lệnh), luồng mạng chạy tiếp
                    game.pokemon.GiamSat.loi("goi tin lenh " + message.command, e);
                    return;
                }
            }
        }

        /** Cấp phát mảng gói n byte; thiếu bộ nhớ -> giải phóng rồi thử lại; vẫn không được -> null. */
        private byte[] capPhat(int n) {
            game.pokemon.BoNho.truocGoi(n);
            try {
                return new byte[n];
            } catch (OutOfMemoryError e) {
                game.pokemon.BoNho.giai("cap phat goi " + (n >> 10) + " KB");
                try {
                    return new byte[n];
                } catch (OutOfMemoryError e2) {
                    return null;
                }
            }
        }

        /** Đọc bỏ n byte dữ liệu gói (giữ đúng vị trí khoá XOR) - gói không cấp phát được. */
        private void boQua(int n) throws IOException {
            byte[] b = new byte[256];
            int con = n;
            while (con > 0) {
                int k = Session_ME.this.dis.read(b, 0, con < b.length ? con : b.length);
                if (k < 0) {
                    throw new IOException("het du lieu");
                }
                if (Session_ME.this.getKeyComplete) {
                    for (int i = 0; i < k; i++) {
                        Session_ME.this.readKey(b[i]);
                    }
                }
                con -= k;
            }
        }

        /**
         * Gói mảnh -123 (server io.GoiManh): mảnh đầu [0][lệnh gốc][int độ dài][dữ liệu], mảnh sau [1][dữ liệu].
         * Giữ các mảnh riêng (không ghép thành một mảng lớn); đủ thì trả gói gốc đọc qua LuongManh.
         */
        private Message nhanManh(byte[] d) {
            if (d.length >= 6 && d[0] == 0) {
                if (Session_ME.this.manhCon > 0) {
                    game.pokemon.GiamSat.ghi("goi manh: bo goi " + Session_ME.this.manhLenh + " chua du (con " + Session_ME.this.manhCon + " B)");
                }
                Session_ME.this.manhLenh = d[1];
                int tong = ((d[2] & 255) << 24) | ((d[3] & 255) << 16) | ((d[4] & 255) << 8) | (d[5] & 255);
                Session_ME.this.manh = new Vector();
                Session_ME.this.manhDau = new Vector();
                Session_ME.this.manh.addElement(d);
                Session_ME.this.manhDau.addElement(new Integer(6));
                Session_ME.this.manhCon = tong - (d.length - 6);
            } else if (d.length >= 1 && d[0] == 1 && Session_ME.this.manhCon > 0) {
                Session_ME.this.manh.addElement(d);
                Session_ME.this.manhDau.addElement(new Integer(1));
                Session_ME.this.manhCon -= d.length - 1;
            } else {
                game.pokemon.GiamSat.ghi("goi manh: manh le (" + d.length + " B)");
                return CHO_MANH;
            }
            if (Session_ME.this.manhCon > 0) {
                return CHO_MANH;
            }
            int n = Session_ME.this.manh.size();
            byte[][] ds = new byte[n][];
            int[] dau = new int[n];
            for (int i = 0; i < n; i++) {
                ds[i] = (byte[]) Session_ME.this.manh.elementAt(i);
                dau[i] = ((Integer) Session_ME.this.manhDau.elementAt(i)).intValue();
            }
            boolean du = Session_ME.this.manhCon == 0;
            Session_ME.this.manh = null;
            Session_ME.this.manhDau = null;
            Session_ME.this.manhCon = -1;
            if (!du) {
                game.pokemon.GiamSat.ghi("goi manh: thua du lieu, bo goi " + Session_ME.this.manhLenh);
                return CHO_MANH;
            }
            soGoiManh++;
            return new Message(Session_ME.this.manhLenh, new LuongManh(ds, dau));
        }

        private void getKey(Message message) throws IOException {
            int i = message.reader().readByte();
            Session_ME.this.key = new byte[i];
            for (int i2 = 0; i2 < i; i2++) {
                Session_ME.this.key[i2] = message.reader().readByte();
            }
            for (int i3 = 0; i3 < Session_ME.this.key.length - 1; i3++) {
                byte[] bArr = Session_ME.this.key;
                int i4 = i3 + 1;
                bArr[i4] = (byte) (bArr[i4] ^ Session_ME.this.key[i3]);
            }
            Session_ME.this.getKeyComplete = true;
            game.pokemon.KhoiDong.guiNeuCo();   // 0.9.7.2: lỗi khởi động (nếu có) về nhật ký máy chủ
        }

        private Message readMessage() throws Exception {
            int size;
            Session_ME.countRead++;
            byte cmd = Session_ME.this.dis.readByte();
            if (Session_ME.this.getKeyComplete) {
                cmd = Session_ME.this.readKey(cmd);
            }
            if (Session_ME.this.getKeyComplete) {
                if (cmd == -128) {
                    cmd = Session_ME.this.readKey(Session_ME.this.dis.readByte());
                    byte[] b = {Session_ME.this.dis.readByte(), Session_ME.this.dis.readByte(), Session_ME.this.dis.readByte(), Session_ME.this.dis.readByte()};
                    size = (Session_ME.this.readKey(b[3]) & 255) | ((Session_ME.this.readKey(b[2]) & 255) << 8) | ((Session_ME.this.readKey(b[1]) & 255) << 16) | ((Session_ME.this.readKey(b[0]) & 255) << 24);
                } else {
                    byte b1 = Session_ME.this.dis.readByte();
                    byte b2 = Session_ME.this.dis.readByte();
                    size = ((Session_ME.this.readKey(b1) & 255) << 8) | (Session_ME.this.readKey(b2) & 255);
                }
            } else {
                size = Session_ME.this.dis.readUnsignedShort();
            }
            byte[] data = capPhat(size);
            if (data == null) {
                boQua(size);
                soGoiBo++;
                game.pokemon.GiamSat.ghi("bo goi lenh " + cmd + " (" + size + " B): khong du bo nho");
                return BO_QUA;
            }
            int len = 0;
            int byteRead = 0;
            Session_ME.this.recvByteCount += 3;
            while (byteRead < size) {
                len = Session_ME.this.dis.read(data, byteRead, size - byteRead);
                if (len < 0) {
                    // 0.9.7.1: hết luồng giữa gói - trước đây trả gói THIẾU dữ liệu cho bộ xử lý
                    throw new IOException("ket noi dong giua goi");
                }
                byteRead += len;
                Session_ME.this.recvByteCount += len;
            }
            if (Session_ME.this.getKeyComplete) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = Session_ME.this.readKey(data[i]);
                }
            }
            if (cmd == GOI_NEN && giaiNen != null) {
                return moGoiNen(data);
            }
            if (cmd == GOI_MANH) {
                return nhanManh(data);
            }
            Message msg = new Message(cmd, data);
            return msg;
        }

        /** Gói -127: zlib của MỘT khung gói gốc chưa mã hoá ([-128] lệnh, độ dài 4 / 2 byte, dữ liệu) - server io.GoiNen. */
        private Message moGoiNen(byte[] nen) throws Exception {
            byte[] k = giaiNen.giai(nen);
            int i = 0;
            byte c = k[i++];
            int n;
            if (c == -128) {
                c = k[i++];
                n = ((k[i] & 255) << 24) | ((k[i + 1] & 255) << 16) | ((k[i + 2] & 255) << 8) | (k[i + 3] & 255);
                i += 4;
            } else {
                n = ((k[i] & 255) << 8) | (k[i + 1] & 255);
                i += 2;
            }
            byte[] d = new byte[n];
            System.arraycopy(k, i, d, 0, n);
            soGoiNen++;
            return new Message(c, d);
        }
    }

    @Override
    public void close() {
        cleanNetwork();
    }

    public void cleanNetwork() {
        // 0.9.4: bỏ gói còn xếp hàng của phiên cũ / lần nối hỏng. Trước đây gói đăng nhập của các lần nối thất bại
        // nằm lại trong hàng, lần nối được thì gửi DỒN cả loạt (server thấy 4 lần đăng nhập -> đá phiên ->
        // "Không thể kết nối" ngay sau khi vào lại game).
        this.sender.sendingMessage.removeAllElements();
        this.manh = null;
        this.manhDau = null;
        this.manhCon = -1;
        this.key = null;
        this.curR = (byte) 0;
        this.curW = (byte) 0;
        try {
            this.connected = false;
            this.connecting = false;
            if (this.sc != null) {
                this.sc.close();
                this.sc = null;
            }
            if (this.dos != null) {
                this.dos.close();
                this.dos = null;
            }
            if (this.dis != null) {
                this.dis.close();
                this.dis = null;
            }
            this.sendThread = null;
            this.collectorThread = null;
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(String host, String port) {
    }
}
