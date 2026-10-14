package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message {
    public byte command;
    private DataInputStream dis;
    private DataOutputStream dos;
    private ByteArrayInputStream is;
    private ByteArrayOutputStream os;

    public Message(int i) {
        this.os = null;
        this.dos = null;
        this.is = null;
        this.dis = null;
        this.command = (byte) i;
        this.os = new ByteArrayOutputStream();
        this.dos = new DataOutputStream(this.os);
    }

    public Message() {
        this.os = null;
        this.dos = null;
        this.is = null;
        this.dis = null;
    }

    public Message(byte b) {
        this.os = null;
        this.dos = null;
        this.is = null;
        this.dis = null;
        this.command = b;
        this.os = new ByteArrayOutputStream();
        this.dos = new DataOutputStream(this.os);
    }

    Message(byte b, byte[] bArr) {
        this.os = null;
        this.dos = null;
        this.is = null;
        this.dis = null;
        this.command = b;
        this.is = new ByteArrayInputStream(bArr);
        this.dis = new DataInputStream(this.is);
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
            DataInputStream dataInputStream = this.dis;
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            DataOutputStream dataOutputStream = this.dos;
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (IOException unused) {
        }
    }
}
