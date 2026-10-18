package effect;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.Util;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Vector;

public class mDataEffect {
    public static byte[][] indexAction = {new byte[]{0, 0, 1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1}, new byte[]{4, 4, 5, 6, 7, 5, 5, 5, 5, 5, 5, 5, 5, 5}, new byte[]{8, 8, 9, 10, 11, 9, 9, 9, 9, 9, 9, 9, 9, 9}};
    private short FrameHeight;
    private short FrameWith;
    private short idimg;
    public byte[] sequence;
    public SmallImage[] smallImage;
    public Vector listFrame = new Vector();
    public Vector listAnima = new Vector();
    public String name = "";
    public byte isFly = 0;
    public byte idShadow = 0;

    public void paintType2(Graphics graphics, int i, int i2, int i3, int i4) {
    }

    public mDataEffect(byte[] bArr, int i, boolean z) {
        this.idimg = (short) i;
        setDataType1(bArr);
    }

    public mDataEffect(byte[] bArr) {
        setDataType1(bArr);
    }

    public void setDataType1(byte[] array) {
        DataInputStream dis = null;
        ByteArrayInputStream b = null;
        this.listFrame.removeAllElements();
        this.listAnima.removeAllElements();
        try {
            b = new ByteArrayInputStream(array);
            dis = new DataInputStream(b);
            int nSmallImage = dis.readByte();
            this.smallImage = new SmallImage[nSmallImage];
            for (int i = 0; i < nSmallImage; i++) {
                this.smallImage[i] = new SmallImage(dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte());
            }
            short nframe = dis.readShort();
            int minDy = -1000000;
            int maxHeight = 0;
            for (int i2 = 0; i2 < nframe; i2++) {
                byte npart = dis.readByte();
                Vector listPart = new Vector();
                for (int j = 0; j < npart; j++) {
                    PartFrame part = new PartFrame(dis.readShort(), dis.readShort(), dis.readByte());
                    part.flip = dis.readByte();
                    part.onTop = dis.readByte();
                    listPart.addElement(part);
                    if (i2 == 0) {
                        if (minDy < part.dy + this.smallImage[part.idSmallImg].h) {
                            minDy = part.dy + this.smallImage[part.idSmallImg].h;
                        }
                        if (maxHeight < Util.abs(part.dy)) {
                            maxHeight = Util.abs(part.dy);
                        }
                    }
                }
                if (i2 == 0 && minDy <= -5) {
                    this.isFly = (byte) minDy;
                }
                this.listFrame.addElement(new FrameEff(listPart, null));
            }
            this.FrameWith = this.smallImage[0].w;
            this.FrameHeight = (short) maxHeight;
            int nSeq = dis.readShort();
            this.sequence = new byte[nSeq];
            for (int i3 = 0; i3 < nSeq; i3++) {
                this.sequence[i3] = (byte) dis.readShort();
            }
                byte[] data1 = new byte[dis.readByte()];
                dis.read(data1);
                this.listAnima.addElement(new Animation(data1));
                byte[] data2 = new byte[dis.readByte()];
                dis.read(data2);
                this.listAnima.addElement(new Animation(data2));
                byte[] data3 = new byte[dis.readByte()];
                dis.read(data3);
                this.listAnima.addElement(new Animation(data3));
                byte[] data4 = new byte[dis.readByte()];
                dis.read(data4);
                this.listAnima.addElement(new Animation(data4));
                byte[] data5 = new byte[dis.readByte()];
                dis.read(data5);
                this.listAnima.addElement(new Animation(data5));
                byte[] data6 = new byte[dis.readByte()];
                dis.read(data6);
                this.listAnima.addElement(new Animation(data6));
                byte[] data7 = new byte[dis.readByte()];
                dis.read(data7);
                this.listAnima.addElement(new Animation(data7));
                byte[] data8 = new byte[dis.readByte()];
                dis.read(data8);
                this.listAnima.addElement(new Animation(data8));
            if (dis.available() > 0) {
                this.idShadow = dis.readByte();
                for (int i4 = 0; i4 < nframe; i4++) {
                    FrameEff fr = (FrameEff) this.listFrame.elementAt(i4);
                    fr.xShadow = dis.readByte();
                    fr.yShadow = dis.readByte();
                }
            }
            if (dis.available() > 0) {
                    byte[] data9 = new byte[dis.readByte()];
                        dis.read(data9);
                        this.listAnima.addElement(new Animation(data9));
                        byte[] data10 = new byte[dis.readByte()];
                        dis.read(data10);
                        this.listAnima.addElement(new Animation(data10));
                        byte[] data11 = new byte[dis.readByte()];
                        dis.read(data11);
                        this.listAnima.addElement(new Animation(data11));
                        byte[] data12 = new byte[dis.readByte()];
                        dis.read(data12);
                        this.listAnima.addElement(new Animation(data12));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                dis.close();
            } catch (Exception e) {
            }
            try {
                b.close();
            } catch (Exception e) {
            }
        }
    }

    public int getIndexAction(int i, int i2) {
        return indexAction[i2][i];
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

    public FrameEff getFrame(int i) {
        return (FrameEff) this.listFrame.elementAt(i);
    }

    public byte[] getDxDyShadow(int i) {
        FrameEff frameEff = (FrameEff) this.listFrame.elementAt(i);
        if (frameEff != null) {
            return new byte[]{frameEff.xShadow, frameEff.yShadow};
        }
        return new byte[]{0, 0};
    }

    public void paintnoHeight(Graphics graphics, int i, int i2, int i3, int i4, Bitmap bitmap, boolean z) {
        if (bitmap == null) {
            return;
        }
        FrameEff frameEff = (FrameEff) this.listFrame.elementAt(i);
        if (frameEff != null) {
            Vector vector = frameEff.listPartTop;
        }
        if (frameEff.listPartTop == null) {
            return;
        }
        for (int i5 = 0; i5 < frameEff.listPartTop.size(); i5++) {
            try {
                PartFrame partFrame = (PartFrame) frameEff.listPartTop.elementAt(i5);
                SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                int i6 = partFrame.dx;
                int i7 = partFrame.dy;
                if (z) {
                    i7 += 35;
                }
                int width = smallImage.w;
                short s = smallImage.h;
                short s2 = smallImage.x;
                short s3 = smallImage.y;
                if (s2 > bitmap.getWidth()) {
                    s2 = 0;
                }
                short s4 = s3 > bitmap.getHeight() ? (short) 0 : s3;
                if (s2 + width > bitmap.getWidth()) {
                    width = bitmap.getWidth() - s2;
                }
                if (i4 == 2) {
                    i6 = (-i6) - width;
                }
                if (partFrame.flip != 1) {
                    graphics.drawRegion(bitmap, s2, s4, width, s, i4, i2 + i6, i3 + i7, 0);
                } else {
                    graphics.drawRegion(bitmap, s2, s4, width, s, i4 == 2 ? 0 : 2, i2 + i6, i3 + i7, 0);
                }
            } catch (Exception e) {
                System.out.println("loi dataeff: " + this.name);
                e.printStackTrace();
                return;
            }
        }
    }

    public void paintPet(Graphics graphics, int i, int i2, int i3, int i4, Bitmap bitmap, int i5) {
        if (bitmap == null) {
            return;
        }
        FrameEff frameEff = (FrameEff) this.listFrame.elementAt(i);
        if (frameEff != null) {
            Vector vector = frameEff.listPartTop;
        }
        if (frameEff.listPartTop == null) {
            return;
        }
        for (int i6 = 0; i6 < frameEff.listPartTop.size(); i6++) {
            try {
                PartFrame partFrame = (PartFrame) frameEff.listPartTop.elementAt(i6);
                SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                int i7 = partFrame.dx;
                int i8 = partFrame.dy;
                if (i8 < -40) {
                    i8 += i5;
                }
                int width = smallImage.w;
                int height = smallImage.h;
                short s = smallImage.x;
                short s2 = smallImage.y;
                if (s > bitmap.getWidth()) {
                    s = 0;
                }
                short s3 = s2 > bitmap.getHeight() ? (short) 0 : s2;
                if (s + width > bitmap.getWidth()) {
                    width = bitmap.getWidth() - s;
                }
                if (s3 + height > bitmap.getHeight()) {
                    height = bitmap.getHeight() - s3;
                }
                if (i4 == 2) {
                    i7 = (-i7) - width;
                }
                if (partFrame.flip != 1) {
                    graphics.drawRegion(bitmap, s, s3, width, height, i4, i2 + i7, i3 + i8, 0);
                } else {
                    graphics.drawRegion(bitmap, s, s3, width, height, i4 == 2 ? 0 : 2, i2 + i7, i3 + i8, 0);
                }
            } catch (Exception e) {
                System.out.println("loi dataeff: " + this.name);
                e.printStackTrace();
                return;
            }
        }
    }

    public void paint(Graphics graphics, int i, int i2, int i3, int i4, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        FrameEff frameEff = (FrameEff) this.listFrame.elementAt(i);
        if (frameEff != null) {
            Vector vector = frameEff.listPartTop;
        }
        if (frameEff.listPartTop == null) {
            return;
        }
        for (int i5 = 0; i5 < frameEff.listPartTop.size(); i5++) {
            try {
                PartFrame partFrame = (PartFrame) frameEff.listPartTop.elementAt(i5);
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
            } catch (Exception e) {
                System.out.println("loi dataeff: " + this.name);
                e.printStackTrace();
                return;
            }
        }
    }

    public int getPointStart() {
        return this.isFly;
    }

    public short getWith() {
        return this.FrameWith;
    }

    public short getHeight() {
        return this.FrameHeight;
    }
}
