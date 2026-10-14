package network;

import game.model.Cout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

public class Session_ME implements ISession {
    public static Session_ME instance = new Session_ME();
    public static boolean isCancel;
    public Thread collectorThread;
    public boolean connected;
    public boolean connecting;
    private byte curR;
    private byte curW;
    public DataInputStream dis;
    public DataOutputStream dos;
    public boolean getKeyComplete;
    public Thread initThread;
    public IMessageHandler messageHandler;
    public int recvByteCount;
    public Socket sc;
    public int sendByteCount;
    public Thread sendThread;
    long timeConnected;
    private final Sender sender = new Sender();
    public byte[] key = null;
    public String strRecvByteCount = "";

    public static Session_ME gI() {
        if (instance == null) {
            instance = new Session_ME();
        }
        return instance;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void setHandler(IMessageHandler iMessageHandler) {
        this.messageHandler = iMessageHandler;
    }

    @Override
    public void connect(String str, int i) {
        if (this.connected || this.connecting) {
            return;
        }
        this.getKeyComplete = false;
        this.sc = null;
        Thread thread = new Thread(new NetworkInit(str, i));
        this.initThread = thread;
        thread.start();
    }

    class NetworkInit implements Runnable {
        private final String host;
        private final int port;

        NetworkInit(String str, int i) {
            this.host = str;
            this.port = i;
        }

        @Override
        public void run() {
            Session_ME.isCancel = false;
            Cout.println("Connect: >" + this.host + "<");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(20000L);
                    } catch (InterruptedException unused) {
                    }
                    if (Session_ME.this.connecting) {
                        try {
                            Session_ME.this.sc.close();
                        } catch (Exception unused2) {
                        }
                        Session_ME.isCancel = true;
                        Session_ME.this.connecting = false;
                        Session_ME.this.connected = false;
                        Session_ME.this.messageHandler.onConnectionFail();
                    }
                }
            }).start();
            Session_ME.this.connecting = true;
            Thread.currentThread().setPriority(1);
            Session_ME.this.connected = true;
            try {
                doConnect(this.host, this.port);
                Session_ME.this.messageHandler.onConnectOK();
                Cout.println("connected !");
            } catch (Exception unused) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException unused2) {
                }
                if (Session_ME.isCancel) {
                    return;
                }
                Cout.println("cannot connect to " + this.host + "port: " + this.port);
                if (Session_ME.this.connected) {
                    Cout.println("because already connect ! ( connected = true)");
                }
                if (Session_ME.this.messageHandler != null) {
                    Session_ME.this.close();
                    Session_ME.this.messageHandler.onConnectionFail();
                }
            }
        }

        public void doConnect(String str, int i) throws Exception {
            Session_ME.this.sc = new Socket(str, i);
            Session_ME.this.sc.setKeepAlive(true);
            Session_ME.this.dos = new DataOutputStream(Session_ME.this.sc.getOutputStream());
            Session_ME.this.dis = new DataInputStream(Session_ME.this.sc.getInputStream());
            new Thread(Session_ME.this.sender).start();
            Session_ME.this.collectorThread = new Thread(Session_ME.this.new MessageCollector());
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

    public synchronized void doSendMessage(Message message) throws IOException {
        byte[] data = message.getData();
        try {
            if (this.getKeyComplete) {
                try {
                    this.dos.writeByte(writeKey(message.command));
                } catch (Exception unused) {
                    Cout.println("Loi write key trong dosendmessage");
                }
            } else {
                this.dos.writeByte(message.command);
            }
            if (data != null) {
                int length = data.length;
                if (this.getKeyComplete) {
                    this.dos.writeByte(writeKey((byte) (length >> 8)));
                    this.dos.writeByte(writeKey((byte) (length & 255)));
                } else {
                    this.dos.writeShort(length);
                }
                if (this.getKeyComplete) {
                    for (int i = 0; i < data.length; i++) {
                        data[i] = writeKey(data[i]);
                    }
                }
                this.dos.write(data);
                this.sendByteCount += data.length + 5;
            } else {
                this.dos.writeShort(0);
                this.sendByteCount += 5;
            }
            this.dos.flush();
        } catch (IOException unused2) {
            Cout.println("Loi Do send Message");
        }
    }

    public byte readKey(byte b) {
        byte[] bArr = this.key;
        byte b2 = this.curR;
        byte b3 = (byte) (b2 + 1);
        this.curR = b3;
        byte b4 = (byte) ((b & 255) ^ (bArr[b2] & 255));
        if (b3 >= bArr.length) {
            this.curR = (byte) (b3 % bArr.length);
        }
        return b4;
    }

    private byte writeKey(byte b) {
        byte[] bArr = this.key;
        byte b2 = this.curW;
        byte b3 = (byte) (b2 + 1);
        this.curW = b3;
        byte b4 = (byte) ((b & 255) ^ (bArr[b2] & 255));
        if (b3 >= bArr.length) {
            this.curW = (byte) (b3 % bArr.length);
        }
        return b4;
    }

    private class Sender implements Runnable {
        private final Vector<Message> sendingMessage = new Vector<>();

        public Sender() {
        }

        public void AddMessage(Message message) {
            try {
                this.sendingMessage.addElement(message);
            } catch (Exception unused) {
            }
        }

        @Override
        public void run() {
            while (Session_ME.this.connected) {
                try {
                    if (Session_ME.this.getKeyComplete) {
                        while (this.sendingMessage.size() > 0) {
                            Message messageElementAt = this.sendingMessage.elementAt(0);
                            this.sendingMessage.removeElementAt(0);
                            Session_ME.this.doSendMessage(messageElementAt);
                        }
                    }
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException unused) {
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
            while (true) {
                try {
                    if (!Session_ME.this.isConnected()) {
                        break;
                    }
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception unused) {
                    Cout.println("--------------------------");
                    break;
                }
            }
            if (Session_ME.this.connected) {
                if (Session_ME.this.messageHandler != null) {
                    if (System.currentTimeMillis() - Session_ME.this.timeConnected > 500) {
                        Session_ME.this.messageHandler.onDisconnected();
                    } else {
                        Session_ME.this.messageHandler.onConnectionFail();
                    }
                }
                if (Session_ME.this.sc != null) {
                    Session_ME.this.cleanNetwork();
                }
            }
        }

        private void getKey(Message message) throws IOException {
            int i = message.reader().readByte();
            Session_ME.this.key = new byte[i];
            int i2 = 0;
            for (int i3 = 0; i3 < i; i3++) {
                Session_ME.this.key[i3] = message.reader().readByte();
            }
            while (i2 < Session_ME.this.key.length - 1) {
                byte[] bArr = Session_ME.this.key;
                int i4 = i2 + 1;
                bArr[i4] = (byte) (Session_ME.this.key[i2] ^ bArr[i4]);
                i2 = i4;
            }
            Session_ME.this.getKeyComplete = true;
        }

        private Message readMessage() throws Exception {
            int unsignedShort;
            int key;
            int key2;
            byte key3 = Session_ME.this.dis.readByte();
            if (Session_ME.this.getKeyComplete) {
                key3 = Session_ME.this.readKey(key3);
            }
            if (Session_ME.this.getKeyComplete) {
                if (key3 == -128) {
                    key3 = Session_ME.this.readKey(Session_ME.this.dis.readByte());
                    byte[] bArr = {Session_ME.this.dis.readByte(), Session_ME.this.dis.readByte(), Session_ME.this.dis.readByte(), Session_ME.this.dis.readByte()};
                    key = ((Session_ME.this.readKey(bArr[1]) & 255) << 16) | ((Session_ME.this.readKey(bArr[2]) & 255) << 8) | (Session_ME.this.readKey(bArr[3]) & 255);
                    key2 = (Session_ME.this.readKey(bArr[0]) & 255) << 24;
                } else {
                    byte b = Session_ME.this.dis.readByte();
                    byte b2 = Session_ME.this.dis.readByte();
                    key = (Session_ME.this.readKey(b) & 255) << 8;
                    key2 = Session_ME.this.readKey(b2) & 255;
                }
                unsignedShort = key | key2;
            } else {
                unsignedShort = Session_ME.this.dis.readUnsignedShort();
            }
            byte[] bArr2 = new byte[unsignedShort];
            int i = 0;
            int i2 = 0;
            while (i != -1 && i2 < unsignedShort) {
                i = Session_ME.this.dis.read(bArr2, i2, unsignedShort - i2);
                if (i > 0) {
                    i2 += i;
                }
            }
            Session_ME.this.recvByteCount += i2 + 5;
            int i3 = Session_ME.gI().recvByteCount + Session_ME.gI().sendByteCount;
            Session_ME.this.strRecvByteCount = (i3 / 1024) + "." + ((i3 % 1024) / 102) + "Kb";
            if (Session_ME.this.getKeyComplete) {
                for (int i4 = 0; i4 < unsignedShort; i4++) {
                    bArr2[i4] = Session_ME.this.readKey(bArr2[i4]);
                }
            }
            return new Message(key3, bArr2);
        }
    }

    @Override
    public void close() {
        cleanNetwork();
    }

    public void cleanNetwork() {
        this.key = null;
        this.curR = (byte) 0;
        this.curW = (byte) 0;
        this.connected = false;
        this.connecting = false;
        try {
            Socket socket = this.sc;
            if (socket != null) {
                socket.close();
                this.sc = null;
            }
            DataOutputStream dataOutputStream = this.dos;
            if (dataOutputStream != null) {
                dataOutputStream.close();
                this.dos = null;
            }
            DataInputStream dataInputStream = this.dis;
            if (dataInputStream != null) {
                dataInputStream.close();
                this.dis = null;
            }
            this.sendThread = null;
            this.collectorThread = null;
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
