package game.model;

import android.graphics.Bitmap;
import com.teamobi.kpah.GameMidlet;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.render.Res;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Array;

public class CharPartInfo {
    public static final byte[][] PART_OF_FRAME = {new byte[]{0, 0, 1, 2, 3, 4}, new byte[]{0, 0, 1, 2, 3, 4}, new byte[]{0, 0, 0, 0, 0, 0}, new byte[]{0, 0, 0, 0, 0, 0}, new byte[]{0, 1, 0, 1, 0, 1}};
    int avh0;
    int avw0;
    int avx0;
    int avxf;
    int avy0;
    int avyf;
    public int[][] dx = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, 6);
    public int[][] dy = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, 6);
    public int[][] h;
    public int id;
    public Bitmap image;
    public int time;
    long timePaint;
    public int timeRemove;
    public int type;
    public int[][] w;
    public int[][] x;
    public int[][] y;

    public void load(byte[] data, int type, int unused) {
        ByteArrayInputStream bais = null;
        DataInputStream dis = null;
        ByteArrayInputStream infoBais = null;
        DataInputStream infoDis = null;
        try {
            bais = new ByteArrayInputStream(data);
            dis = new DataInputStream(bais);
            int len = dis.readShort();
            byte[] buf = new byte[len];
            dis.read(buf, 0, len);
            this.image = Image.createImage(buf, 0, len);
            len = dis.readShort();
            buf = new byte[len];
            dis.read(buf, 0, len);
            infoBais = new ByteArrayInputStream(buf);
            infoDis = new DataInputStream(infoBais);
            this.avx0 = Res.readSignByte(infoDis);
            this.avy0 = Res.readSignByte(infoDis);
            this.avw0 = Res.readSignByte(infoDis);
            this.avh0 = Res.readSignByte(infoDis);
            this.avxf = Res.readSignByte(infoDis);
            this.avyf = Res.readSignByte(infoDis);
            try {
                if (type < 4) {
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < this.x[i].length; j++) {
                            this.x[i][j] = infoDis.read();
                            this.y[i][j] = infoDis.read();
                            this.w[i][j] = infoDis.read();
                            this.h[i][j] = infoDis.read();
                        }
                        for (int j = 0; j < 6; j++) {
                            this.dx[i][j] = Res.readSignByte(infoDis);
                            this.dy[i][j] = Res.readSignByte(infoDis);
                        }
                    }
                } else {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < this.x[i].length; j++) {
                            this.x[i][j] = infoDis.read();
                            this.y[i][j] = infoDis.read();
                            this.w[i][j] = infoDis.read();
                            this.h[i][j] = infoDis.read();
                        }
                    }
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 6; j++) {
                            this.dx[i][j] = Res.readSignByte(infoDis);
                            this.dy[i][j] = Res.readSignByte(infoDis);
                        }
                    }
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
        } finally {
            try {
                bais.close();
            } catch (Exception e) {
            }
            try {
                infoBais.close();
            } catch (Exception e) {
            }
            try {
                infoDis.close();
            } catch (Exception e) {
            }
            try {
                dis.close();
            } catch (Exception e) {
            }
        }
    }

    public void loadCoat(int type, int id) {
        ByteArrayInputStream unusedStream = null;
        DataInputStream dis = null;
        ByteArrayInputStream bais = null;
        DataInputStream dataDis = null;
        try {
            dis = new DataInputStream("".getClass().getResourceAsStream(FilePack.charAvatar[type] + id));
            int len = dis.readShort();
            byte[] data = new byte[len];
            dis.read(data, 0, len);
            this.image = Image.createImage(data, 0, len);
            len = dis.readShort();
            data = new byte[len];
            dis.read(data, 0, len);
            bais = new ByteArrayInputStream(data);
            dataDis = new DataInputStream(bais);
            this.avx0 = Res.readSignByte(dataDis);
            this.avy0 = Res.readSignByte(dataDis);
            this.avw0 = Res.readSignByte(dataDis);
            this.avh0 = Res.readSignByte(dataDis);
            this.avxf = Res.readSignByte(dataDis);
            this.avyf = Res.readSignByte(dataDis);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < this.x[i].length; j++) {
                    this.x[i][j] = dataDis.read();
                    this.y[i][j] = dataDis.read();
                    this.w[i][j] = dataDis.read();
                    this.h[i][j] = dataDis.read();
                }
            }
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 6; j++) {
                    this.dx[i][j] = Res.readSignByte(dataDis);
                    this.dy[i][j] = Res.readSignByte(dataDis);
                }
            }
            dataDis.close();
        } catch (Exception e) {
        } finally {
            try {
                unusedStream.close();
            } catch (Exception e) {
            }
            try {
                bais.close();
            } catch (Exception e) {
            }
            try {
                dataDis.close();
            } catch (Exception e) {
            }
            try {
                dis.close();
            } catch (Exception e) {
            }
        }
    }

    public void load(int i) {
        try {
            DataInputStream dataInputStream = new DataInputStream(GameMidlet.asset.open(FilePack.charAvatar[i] + this.id));
            int i2 = dataInputStream.readShort();
            byte[] bArr = new byte[i2];
            dataInputStream.read(bArr, 0, i2);
            this.image = Image.createImage(bArr, 0, i2);
            int i3 = dataInputStream.readShort();
            byte[] bArr2 = new byte[i3];
            dataInputStream.read(bArr2, 0, i3);
            DataInputStream dataInputStream2 = new DataInputStream(new ByteArrayInputStream(bArr2));
            this.avx0 = Res.readSignByte(dataInputStream2);
            this.avy0 = Res.readSignByte(dataInputStream2);
            this.avw0 = Res.readSignByte(dataInputStream2);
            this.avh0 = Res.readSignByte(dataInputStream2);
            this.avxf = Res.readSignByte(dataInputStream2);
            this.avyf = Res.readSignByte(dataInputStream2);
            for (int i4 = 0; i4 < 4; i4++) {
                int i5 = 0;
                while (true) {
                    try {
                        int[] iArr = this.x[i4];
                        if (i5 >= iArr.length) {
                            break;
                        }
                        iArr[i5] = dataInputStream2.read();
                        this.y[i4][i5] = dataInputStream2.read();
                        this.w[i4][i5] = dataInputStream2.read();
                        this.h[i4][i5] = dataInputStream2.read();
                        i5++;
                    } catch (Exception unused) {
                    }
                }
                for (int i6 = 0; i6 < 6; i6++) {
                    this.dx[i4][i6] = Res.readSignByte(dataInputStream2);
                    this.dy[i4][i6] = Res.readSignByte(dataInputStream2);
                }
            }
            dataInputStream2.close();
            dataInputStream.close();
        } catch (Exception unused2) {
        }
    }

    public void load(int type, int id) {
        ByteArrayInputStream unusedStream = null;
        DataInputStream dis = null;
        ByteArrayInputStream bais = null;
        DataInputStream dataDis = null;
        try {
            dis = new DataInputStream("".getClass().getResourceAsStream(FilePack.charAvatar[type] + id));
            int len = dis.readShort();
            byte[] data = new byte[len];
            dis.read(data, 0, len);
            this.image = Image.createImage(data, 0, len);
            len = dis.readShort();
            data = new byte[len];
            dis.read(data, 0, len);
            bais = new ByteArrayInputStream(data);
            dataDis = new DataInputStream(bais);
            this.avx0 = Res.readSignByte(dataDis);
            this.avy0 = Res.readSignByte(dataDis);
            this.avw0 = Res.readSignByte(dataDis);
            this.avh0 = Res.readSignByte(dataDis);
            this.avxf = Res.readSignByte(dataDis);
            this.avyf = Res.readSignByte(dataDis);
            try {
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < this.x[i].length; j++) {
                        this.x[i][j] = dataDis.read();
                        this.y[i][j] = dataDis.read();
                        this.w[i][j] = dataDis.read();
                        this.h[i][j] = dataDis.read();
                    }
                    for (int j = 0; j < 6; j++) {
                        this.dx[i][j] = Res.readSignByte(dataDis);
                        this.dy[i][j] = Res.readSignByte(dataDis);
                    }
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
        } finally {
            try {
                unusedStream.close();
            } catch (Exception e) {
            }
            try {
                bais.close();
            } catch (Exception e) {
            }
            try {
                dataDis.close();
            } catch (Exception e) {
            }
            try {
                dis.close();
            } catch (Exception e) {
            }
        }
    }

    public CharPartInfo(int i, int i2) {
        this.type = i;
        this.id = i2;
        int i3 = (i == 0 || i == 1) ? 5 : (i == 2 || i == 3) ? 1 : 2;
        this.x = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, i3);
        this.y = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, i3);
        this.w = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, i3);
        this.h = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, i3);
        if (i < 4) {
            load(i, i2);
        } else {
            loadCoat(i, i2);
        }
    }

    public void paint(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7 = this.type;
        if (i7 < 0) {
            return;
        }
        if (i3 > 2) {
            i6 = 2;
            i5 = 2;
        } else {
            i5 = 0;
            i6 = i3;
        }
        Bitmap bitmap = this.image;
        if (bitmap != null) {
            int i8 = this.dx[i6][i4];
            int i9 = this.dy[i6][i4];
            int[] iArr = this.w[i6];
            byte b = PART_OF_FRAME[i7][i4];
            int i10 = iArr[b];
            int i11 = this.h[i6][b];
            int i12 = this.x[i6][b];
            int i13 = this.y[i6][b];
            if (i12 > bitmap.getWidth()) {
                i12 = 0;
            }
            int i14 = i13 <= this.image.getHeight() ? i13 : 0;
            int width = i12 + i10 > this.image.getWidth() ? this.image.getWidth() - i12 : i10;
            int height = i14 + i11 > this.image.getHeight() ? this.image.getHeight() - i14 : i11;
            if (i3 > 2) {
                i8 = (-this.dx[i6][i4]) - width;
                i9 = this.dy[i6][i4];
            }
            graphics.drawRegion(this.image, i12, i14, width, height, i5, i + i8, i2 + i9, 0);
        } else {
            Res.paintDefault(graphics, i, i2, i3, i4, i7);
        }
        this.timePaint = System.currentTimeMillis();
    }

    public void paintStatic(Graphics graphics, short s, short s2, int i, int i2) {
        try {
            Bitmap bitmap = this.image;
            if (bitmap != null) {
                int[] iArr = this.w[i];
                byte b = PART_OF_FRAME[this.type][i2];
                int width = iArr[b];
                int height = this.h[i][b];
                int i3 = this.x[i][b];
                int i4 = this.y[i][b];
                int i5 = i3 > bitmap.getWidth() ? 0 : i3;
                int i6 = i4 > this.image.getHeight() ? 0 : i4;
                if (i5 + width > this.image.getWidth()) {
                    width = this.image.getWidth() - i5;
                }
                int i7 = width;
                if (i6 + height > this.image.getHeight()) {
                    height = this.image.getHeight() - i6;
                }
                graphics.drawRegion(this.image, i5, i6, i7, height, 0, s, s2, 3);
            }
        } catch (Exception unused) {
            System.out.println("LOI PART " + this.id);
        }
    }

    public void paintAvatar(Graphics graphics, short s, short s2, int i) {
        try {
            Bitmap bitmap = this.image;
            if (bitmap != null) {
                graphics.drawRegion(bitmap, this.avx0, this.avy0, this.avw0, this.avh0, 0, s + this.avxf, s2 + this.avyf, 0);
            }
        } catch (Exception unused) {
        }
    }

    public void paintImage(Graphics graphics, int i, int i2) {
        graphics.drawImage(this.image, i, i2, 0);
    }
}
