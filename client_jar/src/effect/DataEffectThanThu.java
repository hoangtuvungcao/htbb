package effect;

import game.render.Canvas;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class DataEffectThanThu {
    public SmallImage[] smallImage;
    public byte[] sequence;
    private short FrameWith;
    private short FrameHeight;
    private short idimg;
    public static byte[][] indexAction = {new byte[]{0, 0, 1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1}, new byte[]{4, 4, 5, 6, 7, 5, 5, 5, 5, 5, 5, 5, 5}};
    public Vector listFrame = new Vector();
    public Vector listAnima = new Vector();
    public String name = "";

    public DataEffectThanThu(byte[] array, int idimg) {
        this.idimg = (short) idimg;
        setDataType1(array);
    }

    public DataEffectThanThu(byte[] array) {
        setDataType1(array);
    }

    public void setDataType1(byte[] array) {
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(array));
            int nSmallImage = dis.readByte();
            this.smallImage = new SmallImage[nSmallImage];
            for (int i = 0; i < nSmallImage; i++) {
                this.smallImage[i] = new SmallImage(dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte());
            }
            int max_height = 0;
            int nframe = dis.readShort();
            for (int i2 = 0; i2 < nframe; i2++) {
                byte npart = dis.readByte();
                Vector listPart = new Vector();
                Vector listPartbottom = new Vector();
                for (int j = 0; j < npart; j++) {
                    PartFrame part = new PartFrame(dis.readShort(), dis.readShort(), dis.readByte());
                    part.flip = dis.readByte();
                    part.onTop = dis.readByte();
                    if (part.onTop == 0) {
                        listPart.addElement(part);
                    } else {
                        listPartbottom.addElement(part);
                    }
                    if (max_height < Canvas.abs(part.dy)) {
                        max_height = Canvas.abs(part.dy);
                    }
                }
                this.listFrame.addElement(new FrameEff(listPart, listPartbottom));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void paint(Graphics g, int idFrame, int x, int y, int way, Image img) {
        if (img == null) {
            return;
        }
        FrameEff frame = (FrameEff) this.listFrame.elementAt(idFrame);
        try {
            Vector listPart = frame.getListPartPaint();
            for (int i = 0; i < listPart.size(); i++) {
                PartFrame part = (PartFrame) listPart.elementAt(i);
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
            }
        } catch (Exception e) {
            System.out.println(new StringBuffer("loi dataeff: ").append(this.name).toString());
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
