package effect;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Vector;

public class DataEffectThanThu {
    public static byte[][] indexAction = {new byte[]{0, 0, 1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1}, new byte[]{4, 4, 5, 6, 7, 5, 5, 5, 5, 5, 5, 5, 5}};
    private short FrameHeight;
    private short FrameWith;
    private short idimg;
    public byte[] sequence;
    public SmallImage[] smallImage;
    public Vector listFrame = new Vector();
    public Vector listAnima = new Vector();
    public String name = "";

    public void paintType2(Graphics graphics, int i, int i2, int i3, int i4) {
    }

    public DataEffectThanThu(byte[] bArr, int i) {
        this.idimg = (short) i;
        setDataType1(bArr);
    }

    public DataEffectThanThu(byte[] bArr) {
        setDataType1(bArr);
    }

    public void setDataType1(byte[] bArr) {
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
            int i = dataInputStream.readByte();
            this.smallImage = new SmallImage[i];
            for (int i2 = 0; i2 < i; i2++) {
                this.smallImage[i2] = new SmallImage(dataInputStream.readUnsignedByte(), dataInputStream.readUnsignedByte(), dataInputStream.readUnsignedByte(), dataInputStream.readUnsignedByte(), dataInputStream.readUnsignedByte());
            }
            short s = dataInputStream.readShort();
            int iAbs = 0;
            for (int i3 = 0; i3 < s; i3++) {
                byte b = dataInputStream.readByte();
                Vector vector = new Vector();
                Vector vector2 = new Vector();
                for (int i4 = 0; i4 < b; i4++) {
                    PartFrame partFrame = new PartFrame(dataInputStream.readShort(), dataInputStream.readShort(), dataInputStream.readByte());
                    partFrame.flip = dataInputStream.readByte();
                    partFrame.onTop = dataInputStream.readByte();
                    if (partFrame.onTop == 0) {
                        vector.addElement(partFrame);
                    } else {
                        vector2.addElement(partFrame);
                    }
                    if (iAbs < GCanvas.abs(partFrame.dy)) {
                        iAbs = GCanvas.abs(partFrame.dy);
                    }
                }
                this.listFrame.addElement(new FrameEff(vector, vector2));
            }
            this.FrameWith = this.smallImage[0].w;
            this.FrameHeight = (short) iAbs;
            int i5 = dataInputStream.readShort();
            this.sequence = new byte[i5];
            for (int i6 = 0; i6 < i5; i6++) {
                this.sequence[i6] = (byte) dataInputStream.readShort();
            }
            byte[] bArr2 = new byte[dataInputStream.readByte()];
            dataInputStream.read(bArr2);
            this.listAnima.addElement(new Animation(bArr2));
            byte[] bArr3 = new byte[dataInputStream.readByte()];
            dataInputStream.read(bArr3);
            this.listAnima.addElement(new Animation(bArr3));
            byte[] bArr4 = new byte[dataInputStream.readByte()];
            dataInputStream.read(bArr4);
            this.listAnima.addElement(new Animation(bArr4));
            byte[] bArr5 = new byte[dataInputStream.readByte()];
            dataInputStream.read(bArr5);
            this.listAnima.addElement(new Animation(bArr5));
            byte[] bArr6 = new byte[dataInputStream.readByte()];
            dataInputStream.read(bArr6);
            this.listAnima.addElement(new Animation(bArr6));
            byte[] bArr7 = new byte[dataInputStream.readByte()];
            dataInputStream.read(bArr7);
            this.listAnima.addElement(new Animation(bArr7));
            byte[] bArr8 = new byte[dataInputStream.readByte()];
            dataInputStream.read(bArr8);
            this.listAnima.addElement(new Animation(bArr8));
            byte[] bArr9 = new byte[dataInputStream.readByte()];
            dataInputStream.read(bArr9);
            this.listAnima.addElement(new Animation(bArr9));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Animation getAnim(int i, int i2) {
        return (Animation) this.listAnima.elementAt(indexAction[i2][i]);
    }

    public int getFrame(int i, int i2, int i3) {
        Animation animation = (Animation) this.listAnima.elementAt(indexAction[i3][i2]);
        if (i < animation.frame.length) {
            return animation.frame[i];
        }
        return 0;
    }

    public void paint(Graphics graphics, int i, int i2, int i3, int i4, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        try {
            Vector listPartPaint = ((FrameEff) this.listFrame.elementAt(i)).getListPartPaint();
            for (int i5 = 0; i5 < listPartPaint.size(); i5++) {
                PartFrame partFrame = (PartFrame) listPartPaint.elementAt(i5);
                SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                int i6 = partFrame.dx;
                short s = smallImage.w;
                short s2 = smallImage.h;
                short s3 = smallImage.x;
                short s4 = smallImage.y;
                if (s3 > bitmap.getWidth()) {
                    s3 = 0;
                }
                short s5 = s4 > bitmap.getHeight() ? (short) 0 : s4;
                int width = s3 + s > bitmap.getWidth() ? bitmap.getWidth() - s3 : s;
                int height = s5 + s2 > bitmap.getHeight() ? bitmap.getHeight() - s5 : s2;
                if (i4 == 2) {
                    i6 = (-i6) - width;
                }
                if (partFrame.flip != 1) {
                    graphics.drawRegion(bitmap, s3, s5, width, height, i4, i2 + i6, i3 + partFrame.dy, 0);
                } else {
                    graphics.drawRegion(bitmap, s3, s5, width, height, i4 == 2 ? 0 : 2, i2 + i6, i3 + partFrame.dy, 0);
                }
            }
        } catch (Exception e) {
            System.out.println("loi dataeff: " + this.name);
            e.printStackTrace();
        }
    }

    public short getWith() {
        return this.FrameWith;
    }

    public short getHeight() {
        return this.FrameHeight;
    }
}
