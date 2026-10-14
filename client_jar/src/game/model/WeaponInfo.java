package game.model;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Image;

public class WeaponInfo {
    public Image img;
    public byte[] x0;
    public byte[] y0;
    public byte[] w0;
    public byte[] h0;
    public byte[] id;
    public DirrectInfo[] dirInfo = new DirrectInfo[4];

    public void readData(InputStream ip) throws IOException {
        int i = (byte) ip.read();
        this.id = new byte[i];
        this.x0 = new byte[i];
        this.y0 = new byte[i];
        this.w0 = new byte[i];
        this.h0 = new byte[i];
        for (int i2 = 0; i2 < i; i2++) {
            this.id[i2] = (byte) ip.read();
            this.x0[i2] = (byte) ip.read();
            this.y0[i2] = (byte) ip.read();
            this.w0[i2] = (byte) ip.read();
            this.h0[i2] = (byte) ip.read();
        }
        for (int i3 = 0; i3 < 4; i3++) {
            this.dirInfo[i3] = new DirrectInfo();
            this.dirInfo[i3].idImg = (byte) ip.read();
            this.dirInfo[i3].dx = (byte) ip.read();
            this.dirInfo[i3].dy = (byte) ip.read();
            this.dirInfo[i3].iFlip = (byte) ip.read();
        }
    }

    public byte getID(int idI) {
        byte b = 0;
        while (true) {
            byte i = b;
            if (i < this.id.length) {
                if (this.id[i] != idI) {
                    b = (byte) (i + 1);
                } else {
                    return i;
                }
            } else {
                return (byte) -1;
            }
        }
    }

    public class DirrectInfo {
        byte idImg;
        byte dx;
        byte dy;
        byte iFlip;

        public DirrectInfo() {
        }
    }
}
