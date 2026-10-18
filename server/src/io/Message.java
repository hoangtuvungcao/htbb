/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 *
 */
public class Message {

    public int command;
    private ByteArrayOutputStream os = null;
    public DataOutputStream dos = null;
    private ByteArrayInputStream is = null;
    public DataInputStream dis = null;
    private byte[] bytes = null;

    /**
     * Goi gui KEM NGAY SAU goi nay, cung mot hang doi (Session.Sender). Dung cho
     * che do Pokemon: goi 5/3 (thong tin nhan vat) kem -90 POKE_THAN, goi 26 kem
     * -91 POKE_QUAI - nho vay khong phai sua hang tram cho goi
     * createCharInfo/createMainCharInfoMessage. null = khong kem gi (mac dinh, hanh
     * vi cu). Xem real.TienHoaPokemon.
     */
    public Message kemTheo = null;

    public Message(int command) {
        this.command = command;
        this.os = new BoDem();
        this.dos = new DataOutputStream(this.os);
    }

    /**
     * cái này tạo message để đọc
     * @param command
     * @param data 
     */
    public Message(int command, byte[] data) {
        this.command = command;
        this.is = new ByteArrayInputStream(data);
        this.dis = new DataInputStream(this.is);
    }

    /** ByteArrayOutputStream cho doc thang mang ben trong (khoi chep them mot lan). */
    private static final class BoDem extends ByteArrayOutputStream {
        BoDem() {
            super(64);
        }

        byte[] mang() {
            return this.buf;
        }

        int dem() {
            return this.count;
        }
    }

    /**
     * Goi tin da dong khung: [cmd][len 2 byte][du lieu], hoac [-128][cmd][len 4 byte][du lieu] khi
     * du lieu > 32767 byte. Ket qua duoc NHO LAI (cung mot mang cho moi nguoi nhan - Session.Sender
     * ma hoa vao bo dem rieng, khong sua mang nay). Truoc day chep du lieu 3 lan (toByteArray cua
     * luong ghi + DataOutputStream moi + toByteArray); nay mot lan cap phat, mot lan chep.
     */
    public byte[] toByteArray() {
        if (this.bytes == null) {
            try {
                byte[] data = null;
                int datalen = 0;
                if (this.dos != null) {
                    this.dos.flush();
                    if (this.os instanceof BoDem) {
                        data = ((BoDem) this.os).mang();
                        datalen = ((BoDem) this.os).dem();
                    } else {
                        data = this.os.toByteArray();
                        datalen = data.length;
                    }
                    this.dos.close();
                }

                final boolean lon = datalen > 32767;
                final int dau = lon ? 6 : 3;
                final byte[] kq = new byte[dau + datalen];
                int i = 0;
                if (lon) {
                    kq[i++] = (byte) -128;
                }
                kq[i++] = (byte) this.command;
                if (lon) {
                    kq[i++] = (byte) (datalen >>> 24);
                    kq[i++] = (byte) (datalen >>> 16);
                }
                kq[i++] = (byte) (datalen >>> 8);
                kq[i++] = (byte) datalen;
                if (datalen > 0) {
                    System.arraycopy(data, 0, kq, i, datalen);
                }
                this.bytes = kq;
            } catch (IOException var18) {
                var18.printStackTrace();
                System.out.println("LOI KHI TAO BYTE");
            }

            this.cleanup();
        }

        return this.bytes;
    }

    public void cleanup() {
    }

    public void cleanALlData() {
        try {
            this.dis.close();
        } catch (Exception var3) {
        }

        try {
            this.dos.close();
        } catch (Exception var2) {
        }

    }
}
