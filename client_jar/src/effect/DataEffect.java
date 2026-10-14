package effect;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class DataEffect {
    public Vector listFrame = new Vector();
    public Vector listAnima = new Vector();
    public SmallImage[] smallImage;
    public byte[] sequence;
    public static byte[] indexAction = {0, 0, 1, 2, 3, 1, 1, 1, 1};

    public DataEffect(byte[] array) {
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(array));
            int nSmallImage = dis.readByte();
            this.smallImage = new SmallImage[nSmallImage];
            for (int i = 0; i < nSmallImage; i++) {
                this.smallImage[i] = new SmallImage(dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte(), dis.readUnsignedByte());
            }
            int nframe = dis.readShort();
            for (int i2 = 0; i2 < nframe; i2++) {
                byte npart = dis.readByte();
                Vector listPart = new Vector();
                for (int j = 0; j < npart; j++) {
                    PartFrame part = new PartFrame(dis.readShort(), dis.readShort(), dis.readByte());
                    listPart.addElement(part);
                }
                this.listFrame.addElement(new FrameEff(listPart));
            }
            int i3 = dis.readShort();
            this.sequence = new byte[i3];
            for (int i4 = 0; i4 < i3; i4++) {
                this.sequence[i4] = (byte) dis.readShort();
            }
            dis.readByte();
            dis.readByte();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Animation getAnim(int action) {
        return (Animation) this.listAnima.elementAt(indexAction[action]);
    }

    public int getFrame(int f, int action) {
        Animation anim = (Animation) this.listAnima.elementAt(indexAction[action]);
        if (f < anim.frame.length) {
            return anim.frame[f];
        }
        return 0;
    }

    public void paint(Graphics g, int idFrame, int x, int y, int way, Image img) {
        if (img == null) {
            return;
        }
        FrameEff frame = (FrameEff) this.listFrame.elementAt(idFrame);
        for (int i = 0; i < frame.listPart.size(); i++) {
            try {
                PartFrame part = (PartFrame) frame.listPart.elementAt(i);
                SmallImage small = this.smallImage[part.idSmallImg];
                int dx = part.dx;
                if (way == 2) {
                    dx = (-dx) - small.w;
                }
                g.drawRegion(img, small.x, small.y, small.w, small.h, way, x + dx, y + part.dy, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
