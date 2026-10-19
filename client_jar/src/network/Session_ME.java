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
            if (this.getKeyComplete) {
                byte b = writeKey(m.command);
                this.dos.writeByte(b);
            } else {
                this.dos.writeByte(m.command);
            }
            if (data != null) {
                int size = data.length;
                if (this.getKeyComplete) {
                    int byte1 = writeKey((byte) (size >> 8));
                    this.dos.writeByte(byte1);
                    int byte2 = writeKey((byte) (size & 255));
                    this.dos.writeByte(byte2);
                } else {
                    this.dos.writeShort(size);
                }
                if (this.getKeyComplete) {
                    for (int i = 0; i < data.length; i++) {
                        data[i] = writeKey(data[i]);
                    }
                }
                this.dos.write(data);
                this.sendByteCount += 5 + data.length;
            } else {
                this.dos.writeShort(0);
                this.sendByteCount += 5;
            }
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
                        Thread.sleep(10L);
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
                    try {
                        if (message.command == -40) {
                            getKey(message);
                        } else {
                            Session_ME.this.messageHandler.onMessage(message);
                        }
                    } catch (Throwable e) {
                        // 0.9.4: lỗi một gói chỉ bỏ gói đó (ghi nhật ký kèm mã lệnh), luồng mạng chạy tiếp
                        game.pokemon.GiamSat.loi("goi tin lenh " + message.command, e);
                    }
                }
            } catch (Exception e2) {
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
            byte[] data = new byte[size];
            int len = 0;
            int byteRead = 0;
            while (len != -1 && byteRead < size) {
                len = Session_ME.this.dis.read(data, byteRead, size - byteRead);
                if (len > 0) {
                    byteRead += len;
                    Session_ME.this.recvByteCount += 5 + byteRead;
                }
            }
            if (Session_ME.this.getKeyComplete) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = Session_ME.this.readKey(data[i]);
                }
            }
            Message msg = new Message(cmd, data);
            return msg;
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
