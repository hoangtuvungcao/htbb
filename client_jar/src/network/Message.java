package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message {
    public byte command;
    private ByteArrayOutputStream os;
    private DataOutputStream dos;
    private ByteArrayInputStream is;
    private DataInputStream dis;

    public Message() {
        this.os = null;
        this.dos = null;
        this.is = null;
        this.dis = null;
    }

    public Message(int command) {
        this.os = null;
        this.dos = null;
        this.is = null;
        this.dis = null;
        this.command = (byte) command;
        this.os = new ByteArrayOutputStream();
        this.dos = new DataOutputStream(this.os);
    }

    public Message(byte command) {
        this.os = null;
        this.dos = null;
        this.is = null;
        this.dis = null;
        this.command = command;
        this.os = new ByteArrayOutputStream();
        this.dos = new DataOutputStream(this.os);
    }

    Message(byte command, byte[] data) {
        this.os = null;
        this.dos = null;
        this.is = null;
        this.dis = null;
        this.command = command;
        this.is = new ByteArrayInputStream(data);
        this.dis = new DataInputStream(this.is);
    }

    /** 0.9.7.1: gói gom từ nhiều mảnh (Session_ME.nhanManh) - đọc qua LuongManh, không ghép thành một mảng. */
    private LuongManh manh;

    Message(byte command, LuongManh in) {
        this.command = command;
        this.manh = in;
        this.dis = new DataInputStream(in);
    }

    /**
     * Đưa luồng đọc về đầu gói (xử lý lại sau khi giải phóng bộ nhớ - Session_ME.xuLy). false = không làm được
     * (gói đang ghi).
     */
    boolean tuDau() {
        try {
            if (this.manh != null) {
                this.manh.reset();
                this.dis = new DataInputStream(this.manh);
                return true;
            }
            if (this.is != null) {
                this.is.reset();
                this.dis = new DataInputStream(this.is);
                return true;
            }
        } catch (Throwable e) {
            // không về đầu được
        }
        return false;
    }

    public byte[] getData() {
        return this.os.toByteArray();
    }

    public DataInputStream reader() {
        return this.dis;
    }

    public DataOutputStream writer() {
        return this.dos;
    }

    public void cleanup() {
        try {
            if (this.dis != null) {
                this.dis.close();
            }
            if (this.dos != null) {
                this.dos.close();
            }
        } catch (IOException e) {
        }
    }
}
