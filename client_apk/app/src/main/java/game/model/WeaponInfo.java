package game.model;

import android.graphics.Bitmap;
import java.io.IOException;
import java.io.InputStream;

public class WeaponInfo {
    public DirrectInfo[] dirInfo = new DirrectInfo[4];
    public byte[] h0;
    public byte[] id;
    public Bitmap img;
    public byte[] w0;
    public byte[] x0;
    public byte[] y0;

    public void readData(InputStream inputStream) throws IOException {
        int i = (byte) inputStream.read();
        this.id = new byte[i];
        this.x0 = new byte[i];
        this.y0 = new byte[i];
        this.w0 = new byte[i];
        this.h0 = new byte[i];
        for (int i2 = 0; i2 < i; i2++) {
            this.id[i2] = (byte) inputStream.read();
            this.x0[i2] = (byte) inputStream.read();
            this.y0[i2] = (byte) inputStream.read();
            this.w0[i2] = (byte) inputStream.read();
            this.h0[i2] = (byte) inputStream.read();
        }
        for (int i3 = 0; i3 < 4; i3++) {
            this.dirInfo[i3] = new DirrectInfo();
            this.dirInfo[i3].idImg = (byte) inputStream.read();
            this.dirInfo[i3].dx = (byte) inputStream.read();
            this.dirInfo[i3].dy = (byte) inputStream.read();
            this.dirInfo[i3].iFlip = (byte) inputStream.read();
        }
    }

    public byte getID(int i) {
        byte b = 0;
        while (true) {
            byte[] bArr = this.id;
            if (b >= bArr.length) {
                return (byte) -1;
            }
            if (bArr[b] == i) {
                return b;
            }
            b = (byte) (b + 1);
        }
    }

    public class DirrectInfo {
        byte dx;
        byte dy;
        byte iFlip;
        byte idImg;

        public DirrectInfo() {
        }
    }
}
