package effect;

import game.render.Util;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class mDataEffect {
    public SmallImage[] smallImage;
    public byte[] sequence;
    private short FrameWith;
    private short FrameHeight;
    private short idimg;
    public static byte[][] indexAction = {new byte[]{0, 0, 1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1}, new byte[]{4, 4, 5, 6, 7, 5, 5, 5, 5, 5, 5, 5, 5, 5}, new byte[]{8, 8, 9, 10, 11, 9, 9, 9, 9, 9, 9, 9, 9, 9}};
    public Vector listFrame = new Vector();
    public Vector listAnima = new Vector();
    public String name = "";
    public byte isFly = 0;
    public byte idShadow = 0;

    public mDataEffect(byte[] array, int idimg, boolean isMonster) {
        this.idimg = (short) idimg;
        setDataType1(array);
    }

    public mDataEffect(byte[] array) {
        setDataType1(array);
    }

    public void setDataType1(byte[] array) {
        DataInputStream dis = null;
        ByteArrayInputStream b = null;
        this.listFrame.removeAllElements();
        this.listAnima.removeAllElements();
        try {
            try {
                b = new ByteArrayInputStream(array);
                dis = new DataInputStream(b);
                int nSmallImage = dis.readByte();
                this.smallImage = new SmallImage[nSmallImage];
                for (int i = 0; i < nSmallImage; i++) {
                    this.smallImage[i] = new SmallImage(dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte());
                }
                int max_height = 0;
                int min_dy = -1000000;
                int nframe = dis.readShort();
                for (int i2 = 0; i2 < nframe; i2++) {
                    byte npart = dis.readByte();
                    Vector listPart = new Vector();
                    for (int j = 0; j < npart; j++) {
                        PartFrame part = new PartFrame(dis.readShort(), dis.readShort(), dis.readByte());
                        part.flip = dis.readByte();
                        part.onTop = dis.readByte();
                        listPart.addElement(part);
                        if (i2 == 0) {
                            if (min_dy < part.dy + this.smallImage[part.idSmallImg].h) {
                                min_dy = part.dy + this.smallImage[part.idSmallImg].h;
                            }
                            if (max_height < Util.abs(part.dy)) {
                                max_height = Util.abs(part.dy);
                            }
                        }
                    }
                    if (i2 == 0 && min_dy <= -5) {
                        this.isFly = (byte) min_dy;
                    }
                    this.listFrame.addElement(new FrameEff(listPart, null));
                }
                this.FrameWith = this.smallImage[0].w;
                this.FrameHeight = (short) max_height;
                int i3 = dis.readShort();
                this.sequence = new byte[i3];
                for (int i4 = 0; i4 < i3; i4++) {
                    this.sequence[i4] = (byte) dis.readShort();
                }
                byte[] data = new byte[dis.readByte()];
                dis.read(data);
                Animation anim = new Animation(data);
                this.listAnima.addElement(anim);
                byte[] data2 = new byte[dis.readByte()];
                dis.read(data2);
                Animation anim2 = new Animation(data2);
                this.listAnima.addElement(anim2);
                byte[] data3 = new byte[dis.readByte()];
                dis.read(data3);
                Animation anim3 = new Animation(data3);
                this.listAnima.addElement(anim3);
                byte[] data4 = new byte[dis.readByte()];
                dis.read(data4);
                Animation anim4 = new Animation(data4);
                this.listAnima.addElement(anim4);
                byte[] data5 = new byte[dis.readByte()];
                dis.read(data5);
                Animation anim5 = new Animation(data5);
                this.listAnima.addElement(anim5);
                byte[] data6 = new byte[dis.readByte()];
                dis.read(data6);
                Animation anim6 = new Animation(data6);
                this.listAnima.addElement(anim6);
                byte[] data7 = new byte[dis.readByte()];
                dis.read(data7);
                Animation anim7 = new Animation(data7);
                this.listAnima.addElement(anim7);
                byte[] data8 = new byte[dis.readByte()];
                dis.read(data8);
                Animation anim8 = new Animation(data8);
                this.listAnima.addElement(anim8);
                if (dis.available() > 0) {
                    this.idShadow = dis.readByte();
                    for (int i5 = 0; i5 < nframe; i5++) {
                        FrameEff fr = (FrameEff) this.listFrame.elementAt(i5);
                        fr.xShadow = dis.readByte();
                        fr.yShadow = dis.readByte();
                    }
                }
                if (dis.available() > 0) {
                    byte[] data9 = new byte[dis.readByte()];
                    dis.read(data9);
                    Animation anim9 = new Animation(data9);
                    this.listAnima.addElement(anim9);
                    byte[] data10 = new byte[dis.readByte()];
                    dis.read(data10);
                    Animation anim10 = new Animation(data10);
                    this.listAnima.addElement(anim10);
                    byte[] data11 = new byte[dis.readByte()];
                    dis.read(data11);
                    Animation anim11 = new Animation(data11);
                    this.listAnima.addElement(anim11);
                    byte[] data12 = new byte[dis.readByte()];
                    dis.read(data12);
                    Animation anim12 = new Animation(data12);
                    this.listAnima.addElement(anim12);
                }
                try {
                    dis.close();
                } catch (Exception e) {
                }
                try {
                    b.close();
                } catch (Exception e2) {
                }
            } catch (Throwable th) {
                try {
                    dis.close();
                } catch (Exception e3) {
                }
                try {
                    b.close();
                } catch (Exception e4) {
                }
                throw th;
            }
        } catch (Exception e5) {
            e5.printStackTrace();
            try {
                dis.close();
            } catch (Exception e6) {
            }
            try {
                b.close();
            } catch (Exception e7) {
            }
        }
    }

    public int getIndexAction(int action, int way) {
        return indexAction[way][action];
    }

    public Animation getAnim(int action, int way) {
        return (Animation) this.listAnima.elementAt(indexAction[way][action]);
    }

    public int getFrame(int f, int action, int way) {
        Animation anim = (Animation) this.listAnima.elementAt(indexAction[way][action]);
        if (f < anim.frame.length) {
            return anim.frame[f];
        }
        return 0;
    }

    public void paintType2(Graphics g, int idFrame, int x, int y, int way) {
    }

    public FrameEff getFrame(int idFrame) {
        FrameEff frame = (FrameEff) this.listFrame.elementAt(idFrame);
        return frame;
    }

    public byte[] getDxDyShadow(int idFrame) {
        FrameEff frame = (FrameEff) this.listFrame.elementAt(idFrame);
        if (frame != null) {
            return new byte[]{frame.xShadow, frame.yShadow};
        }
        return new byte[2];
    }

    public void paintnoHeight(Graphics g, int idFrame, int x, int y, int way, Image img, boolean isFly) {
        if (img == null) {
            return;
        }
        FrameEff frame = (FrameEff) this.listFrame.elementAt(idFrame);
        if (frame != null) {
            frame.getClass();
        }
        if (frame.listPartTop == null) {
            return;
        }
        for (int i = 0; i < frame.listPartTop.size(); i++) {
            try {
                PartFrame part = (PartFrame) frame.listPartTop.elementAt(i);
                SmallImage small = this.smallImage[part.idSmallImg];
                int dx = part.dx;
                int dy = part.dy;
                if (isFly) {
                    dy += 35;
                }
                int w = small.w;
                int h = small.h;
                int xx = small.x;
                int yy = small.y;
                if (xx > img.getWidth()) {
                    xx = 0;
                }
                if (yy > img.getHeight()) {
                    yy = 0;
                }
                if (xx + w > img.getWidth()) {
                    w = img.getWidth() - xx;
                }
                if (way == 2) {
                    dx = (-dx) - w;
                }
                if (part.flip != 1) {
                    g.drawRegion(img, xx, yy, w, h, way, x + dx, y + dy, 0);
                } else {
                    g.drawRegion(img, xx, yy, w, h, way == 2 ? 0 : 2, x + dx, y + dy, 0);
                }
            } catch (Exception e) {
                System.out.println(new StringBuffer("loi dataeff: ").append(this.name).toString());
                e.printStackTrace();
                return;
            }
        }
    }

    public void paintPet(Graphics g, int idFrame, int x, int y, int way, Image img, int ysai) {
        if (img == null) {
            return;
        }
        FrameEff frame = (FrameEff) this.listFrame.elementAt(idFrame);
        if (frame != null) {
            frame.getClass();
        }
        if (frame.listPartTop == null) {
            return;
        }
        for (int i = 0; i < frame.listPartTop.size(); i++) {
            try {
                PartFrame part = (PartFrame) frame.listPartTop.elementAt(i);
                SmallImage small = this.smallImage[part.idSmallImg];
                int dx = part.dx;
                int dy = part.dy;
                if (dy < -40) {
                    dy += ysai;
                }
                int w = small.w;
                int h = small.h;
                int xx = small.x;
                int yy = small.y;
                if (xx > img.getWidth()) {
                    xx = 0;
                }
                if (yy > img.getHeight()) {
                    yy = 0;
                }
                if (xx + w > img.getWidth()) {
                    w = img.getWidth() - xx;
                }
                if (yy + h > img.getHeight()) {
                    h = img.getHeight() - yy;
                }
                if (way == 2) {
                    dx = (-dx) - w;
                }
                if (part.flip != 1) {
                    g.drawRegion(img, xx, yy, w, h, way, x + dx, y + dy, 0);
                } else {
                    g.drawRegion(img, xx, yy, w, h, way == 2 ? 0 : 2, x + dx, y + dy, 0);
                }
            } catch (Exception e) {
                System.out.println(new StringBuffer("loi dataeff: ").append(this.name).toString());
                e.printStackTrace();
                return;
            }
        }
    }

    public void paint(Graphics g, int idFrame, int x, int y, int way, Image img) {
        if (img == null) {
            return;
        }
        FrameEff frame = (FrameEff) this.listFrame.elementAt(idFrame);
        if (frame != null) {
            frame.getClass();
        }
        if (frame.listPartTop == null) {
            return;
        }
        for (int i = 0; i < frame.listPartTop.size(); i++) {
            try {
                PartFrame part = (PartFrame) frame.listPartTop.elementAt(i);
                SmallImage small = this.smallImage[part.idSmallImg];
                int dx = part.dx;
                int w = small.w;
                int h = small.h;
                int xx = small.x;
                int yy = small.y;
                if (xx > img.getWidth()) {
                    xx = 0;
                }
                if (yy > img.getHeight()) {
                    yy = 0;
                }
                if (xx + w > img.getWidth()) {
                    w = img.getWidth() - xx;
                }
                if (yy + h > img.getHeight()) {
                    h = img.getHeight() - yy;
                }
                if (way == 2) {
                    dx = (-dx) - w;
                }
                if (part.flip != 1) {
                    g.drawRegion(img, xx, yy, w, h, way, x + dx, y + part.dy, 0);
                } else {
                    g.drawRegion(img, xx, yy, w, h, way == 2 ? 0 : 2, x + dx, y + part.dy, 0);
                }
            } catch (Exception e) {
                System.out.println(new StringBuffer("loi dataeff: ").append(this.name).toString());
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
