package effect;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Vector;

public class DataEffect {
    public static byte[] indexAction = {0, 0, 1, 2, 3, 1, 1, 1, 1};
    public static byte[][] indexAction2 = {new byte[]{0, 0, 1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1}, new byte[]{4, 4, 5, 6, 7, 5, 5, 5, 5, 5, 5, 5, 5, 5}, new byte[]{8, 8, 9, 10, 11, 9, 9, 9, 9, 9, 9, 9, 9, 9}};
    public byte[] sequence;
    public SmallImage[] smallImage;
    public Vector listFrame = new Vector();
    public Vector listAnima = new Vector();

    public DataEffect(byte[] bArr) {
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
            int i = dataInputStream.readByte();
            this.smallImage = new SmallImage[i];
            for (int i2 = 0; i2 < i; i2++) {
                this.smallImage[i2] = new SmallImage(dataInputStream.readUnsignedByte(), dataInputStream.readUnsignedByte(), dataInputStream.readUnsignedByte(), dataInputStream.readUnsignedByte(), dataInputStream.readUnsignedByte());
            }
            short s = dataInputStream.readShort();
            for (int i3 = 0; i3 < s; i3++) {
                byte b = dataInputStream.readByte();
                Vector vector = new Vector();
                for (int i4 = 0; i4 < b; i4++) {
                    vector.addElement(new PartFrame(dataInputStream.readShort(), dataInputStream.readShort(), dataInputStream.readByte()));
                }
                this.listFrame.addElement(new FrameEff(vector));
            }
            int i5 = dataInputStream.readShort();
            this.sequence = new byte[i5];
            for (int i6 = 0; i6 < i5; i6++) {
                this.sequence[i6] = (byte) dataInputStream.readShort();
            }
            dataInputStream.readByte();
            dataInputStream.readByte();
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
        } catch (Exception unused) {
        }
    }

    public Animation getAnim(int i) {
        return (Animation) this.listAnima.elementAt(indexAction[i]);
    }

    public int getFrame(int i, int i2) {
        Animation animation = (Animation) this.listAnima.elementAt(indexAction[i2]);
        if (i < animation.frame.length) {
            return animation.frame[i];
        }
        return 0;
    }

    public void paint(Graphics graphics, int i, int i2, int i3, int i4, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        FrameEff frameEff = (FrameEff) this.listFrame.elementAt(i);
        for (int i5 = 0; i5 < frameEff.listPart.size(); i5++) {
            try {
                PartFrame partFrame = (PartFrame) frameEff.listPart.elementAt(i5);
                SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                int i6 = partFrame.dx;
                if (i4 == 2) {
                    i6 = (-i6) - smallImage.w;
                }
                graphics.drawRegion(bitmap, smallImage.x, smallImage.y, smallImage.w, smallImage.h, i4, i2 + i6, i3 + partFrame.dy, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public Animation getAnim(int i, int i2) {
        return (Animation) this.listAnima.elementAt(indexAction2[i2][i]);
    }
}
