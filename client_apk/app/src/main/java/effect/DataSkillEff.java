package effect;

import game.core.j2me.Graphics;
import game.model.ImageIcon;
import game.render.screen.GameData;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Vector;

public class DataSkillEff {
    public static final byte NORMAL = 0;
    public static final byte NO_REMOVE = 3;
    public static final byte REMOVE_BY_FRAME = 1;
    public static final byte REMOVE_BY_TIME = 2;
    public static byte TYPE_EFFECT_BUFF = 2;
    public static byte TYPE_EFFECT_END = 0;
    public static byte TYPE_EFFECT_STARTSKILL = 1;
    public byte Frame;
    public byte Typemove;
    public boolean canActorFight;
    public boolean canActorMove;
    public boolean canPaintActor;
    public boolean canremove;
    int dxx;
    public byte dyhorse;
    int dyy;
    public byte f;
    public byte[][] frameChar;
    public int idEff;
    public byte indexStartSkill;
    public boolean isMatna;
    private boolean isremovebyTime;
    private long lasttime;
    public Vector listAnima;
    public Vector listFrame;
    private byte loop;
    public byte[] sequence;
    public SmallImage[] smallImage;
    public long timeRemove;
    public long timelive;
    public int typRequestImg;
    public byte typeupdate;
    public byte waitLoop;
    public boolean wantDetroy;
    public short x;
    public short y;
    public static Hashtable ALL_DATA_EFF = new Hashtable();
    public static byte ACTOR_NORMAL = 0;
    public static byte ACTOR_CAN_NOT_MOVE = 1;
    public static byte ACTOR_CAN_NOT_PAINT = 2;

    public DataSkillEff(int i, int i2, int i3, long j, boolean z, boolean z2, boolean z3, int i4, byte b) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}};
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.timelive = j;
        this.idEff = (short) i;
        this.dyhorse = b;
        if (j == -1) {
            this.typeupdate = (byte) 3;
        } else if (j == 0) {
            this.typeupdate = (byte) 1;
        } else {
            this.typeupdate = (byte) 2;
        }
        this.canActorMove = z;
        this.canPaintActor = z2;
        this.canActorFight = z3;
        PartFrame partFrame = (PartFrame) ALL_DATA_EFF.get(i + "");
        if (partFrame != null) {
            loadData(partFrame.data);
        }
    }

    public DataSkillEff(byte[] bArr, short s, int i, int i2) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}};
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.idEff = s;
        this.dxx = i;
        this.dyy = i2;
        loadData(bArr);
    }

    public void setWaitLoop(byte b) {
        this.waitLoop = b;
    }

    public DataSkillEff(byte[] bArr, short s, int i, int i2, long j, byte b) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}};
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.idEff = s;
        this.dxx = i;
        this.dyy = i2;
        this.Typemove = b;
        this.timelive = j;
        this.isremovebyTime = true;
        loadData(bArr);
    }

    public DataSkillEff(int i) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}};
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.idEff = i;
        PartFrame partFrame = (PartFrame) ALL_DATA_EFF.get(i + "");
        if (partFrame != null) {
            loadData(partFrame.data);
        }
    }

    public DataSkillEff(int i, int i2, int i3, long j) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}};
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.timelive = j;
        this.idEff = (short) i;
        if (j == -1) {
            this.typeupdate = (byte) 3;
        } else if (j == 0) {
            this.typeupdate = (byte) 1;
        } else {
            this.typeupdate = (byte) 2;
        }
        PartFrame partFrame = (PartFrame) ALL_DATA_EFF.get(i + "");
        if (partFrame != null) {
            loadData(partFrame.data);
        }
    }

    public void loadData(byte[] bArr) {
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
                }
                this.listFrame.addElement(new FrameEff(vector, vector2));
            }
            int unsignedByte = (short) dataInputStream.readUnsignedByte();
            this.sequence = new byte[unsignedByte];
            for (int i5 = 0; i5 < unsignedByte; i5++) {
                this.sequence[i5] = (byte) dataInputStream.readShort();
            }
            this.indexStartSkill = dataInputStream.readByte();
            int i6 = dataInputStream.readByte();
            this.frameChar[0] = new byte[i6];
            for (int i7 = 0; i7 < i6; i7++) {
                this.frameChar[0][i7] = dataInputStream.readByte();
            }
            int i8 = dataInputStream.readByte();
            this.frameChar[1] = new byte[i8];
            for (int i9 = 0; i9 < i8; i9++) {
                this.frameChar[1][i9] = dataInputStream.readByte();
            }
            int i10 = dataInputStream.readByte();
            this.frameChar[3] = new byte[i10];
            for (int i11 = 0; i11 < i10; i11++) {
                this.frameChar[3][i11] = dataInputStream.readByte();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paintBottomHorse(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5;
        if (i3 < this.listFrame.size()) {
            try {
                Vector vector = ((FrameEff) this.listFrame.elementAt(i3)).listPartBottom;
                for (int i6 = 0; i6 < vector.size(); i6++) {
                    PartFrame partFrame = (PartFrame) vector.elementAt(i6);
                    SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                    ImageIcon imgIcon = GameData.getImgIcon((short) this.idEff);
                    if (imgIcon != null && imgIcon.img != null) {
                        int i7 = partFrame.dx;
                        int i8 = smallImage.w;
                        int i9 = smallImage.h;
                        short s = smallImage.x;
                        short s2 = smallImage.y;
                        int width = imgIcon.img.getWidth();
                        int height = imgIcon.img.getHeight();
                        short s3 = s > width ? (short) 0 : s;
                        short s4 = s2 > height ? (short) 0 : s2;
                        if (s3 + i8 > width) {
                            i8 = width - s3;
                        }
                        int i10 = i8;
                        if (s4 + i9 > height) {
                            i9 = height - s4;
                        }
                        int i11 = i9;
                        int i12 = partFrame.flip == 1 ? 2 : 0;
                        if (i4 == 2 || i4 == 6) {
                            i7 = -(i7 + i10);
                            i5 = i12 == 2 ? 0 : 2;
                        } else {
                            i5 = i12;
                        }
                        graphics.drawRegion(imgIcon.img, s3, s4, i10, i11, i5, i + i7 + this.dxx, i2 + partFrame.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintTopHorse(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5;
        if (i3 < this.listFrame.size()) {
            try {
                Vector vector = ((FrameEff) this.listFrame.elementAt(i3)).listPartTop;
                for (int i6 = 0; i6 < vector.size(); i6++) {
                    PartFrame partFrame = (PartFrame) vector.elementAt(i6);
                    SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                    ImageIcon imgIcon = GameData.getImgIcon((short) this.idEff);
                    if (imgIcon != null && imgIcon.img != null) {
                        int i7 = partFrame.dx;
                        int i8 = smallImage.w;
                        int i9 = smallImage.h;
                        short s = smallImage.x;
                        short s2 = smallImage.y;
                        int width = imgIcon.img.getWidth();
                        int height = imgIcon.img.getHeight();
                        short s3 = s > width ? (short) 0 : s;
                        short s4 = s2 > height ? (short) 0 : s2;
                        if (s3 + i8 > width) {
                            i8 = width - s3;
                        }
                        int i10 = i8;
                        if (s4 + i9 > height) {
                            i9 = height - s4;
                        }
                        int i11 = i9;
                        int i12 = partFrame.flip == 1 ? 2 : 0;
                        if (i4 == 2 || i4 == 6) {
                            i7 = -(i7 + i10);
                            i5 = i12 == 2 ? 0 : 2;
                        } else {
                            i5 = i12;
                        }
                        graphics.drawRegion(imgIcon.img, s3, s4, i10, i11, i5, i + i7 + this.dxx, i2 + partFrame.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintTop(Graphics graphics, int i, int i2) {
        if (this.Frame < this.listFrame.size()) {
            FrameEff frameEff = (FrameEff) this.listFrame.elementAt(this.Frame);
            try {
                Vector vector = frameEff.listPartTop;
                Vector vector2 = frameEff.listPartBottom;
                for (int i3 = 0; i3 < vector2.size(); i3++) {
                    PartFrame partFrame = (PartFrame) vector2.elementAt(i3);
                    SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                    ImageIcon imgIcon = GameData.getImgIcon((short) this.idEff);
                    if (imgIcon != null && imgIcon.img != null) {
                        short s = partFrame.dx;
                        int i4 = smallImage.w;
                        int i5 = smallImage.h;
                        short s2 = smallImage.x;
                        short s3 = smallImage.y;
                        int width = imgIcon.img.getWidth();
                        int height = imgIcon.img.getHeight();
                        short s4 = s2 > width ? (short) 0 : s2;
                        short s5 = s3 > height ? (short) 0 : s3;
                        if (s4 + i4 > width) {
                            i4 = width - s4;
                        }
                        int i6 = i4;
                        if (s5 + i5 > height) {
                            i5 = height - s5;
                        }
                        graphics.drawRegion(imgIcon.img, s4, s5, i6, i5, partFrame.flip == 1 ? 2 : 0, i + s + this.dxx, i2 + partFrame.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintBottom(Graphics graphics, int i, int i2) {
        if (this.Frame < this.listFrame.size()) {
            try {
                Vector vector = ((FrameEff) this.listFrame.elementAt(this.Frame)).listPartTop;
                for (int i3 = 0; i3 < vector.size(); i3++) {
                    PartFrame partFrame = (PartFrame) vector.elementAt(i3);
                    SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                    ImageIcon imgIcon = GameData.getImgIcon((short) this.idEff);
                    if (imgIcon != null && imgIcon.img != null) {
                        short s = partFrame.dx;
                        int i4 = smallImage.w;
                        int i5 = smallImage.h;
                        short s2 = smallImage.x;
                        short s3 = smallImage.y;
                        int width = imgIcon.img.getWidth();
                        int height = imgIcon.img.getHeight();
                        short s4 = s2 > width ? (short) 0 : s2;
                        short s5 = s3 > height ? (short) 0 : s3;
                        if (s4 + i4 > width) {
                            i4 = width - s4;
                        }
                        int i6 = i4;
                        if (s5 + i5 > height) {
                            i5 = height - s5;
                        }
                        graphics.drawRegion(imgIcon.img, s4, s5, i6, i5, partFrame.flip == 1 ? 2 : 0, i + s + this.dxx, i2 + partFrame.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintTop(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5;
        if (i3 < this.listFrame.size()) {
            try {
                Vector vector = ((FrameEff) this.listFrame.elementAt(i3)).listPartTop;
                for (int i6 = 0; i6 < vector.size(); i6++) {
                    PartFrame partFrame = (PartFrame) vector.elementAt(i6);
                    SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                    ImageIcon imgIcon = GameData.getImgIcon((short) this.idEff);
                    if (imgIcon != null && imgIcon.img != null) {
                        int i7 = partFrame.dx;
                        int i8 = smallImage.w;
                        int i9 = smallImage.h;
                        short s = smallImage.x;
                        short s2 = smallImage.y;
                        int width = imgIcon.img.getWidth();
                        int height = imgIcon.img.getHeight();
                        short s3 = s > width ? (short) 0 : s;
                        short s4 = s2 > height ? (short) 0 : s2;
                        if (s3 + i8 > width) {
                            i8 = width - s3;
                        }
                        int i10 = i8;
                        if (s4 + i9 > height) {
                            i9 = height - s4;
                        }
                        int i11 = i9;
                        int i12 = partFrame.flip == 1 ? 2 : 0;
                        if (i4 == 2 || i4 == 6) {
                            i7 = -(i7 + i10);
                            i5 = i12 == 2 ? 0 : 2;
                        } else {
                            i5 = i12;
                        }
                        graphics.drawRegion(imgIcon.img, s3, s4, i10, i11, i5, i + i7 + this.dxx, i2 + partFrame.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintBottom(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5;
        if (i3 < this.listFrame.size()) {
            try {
                Vector vector = ((FrameEff) this.listFrame.elementAt(i3)).listPartBottom;
                for (int i6 = 0; i6 < vector.size(); i6++) {
                    PartFrame partFrame = (PartFrame) vector.elementAt(i6);
                    SmallImage smallImage = this.smallImage[partFrame.idSmallImg];
                    ImageIcon imgIcon = GameData.getImgIcon((short) this.idEff);
                    if (imgIcon != null && imgIcon.img != null) {
                        int i7 = partFrame.dx;
                        int i8 = smallImage.w;
                        int i9 = smallImage.h;
                        short s = smallImage.x;
                        short s2 = smallImage.y;
                        int width = imgIcon.img.getWidth();
                        int height = imgIcon.img.getHeight();
                        short s3 = s > width ? (short) 0 : s;
                        short s4 = s2 > height ? (short) 0 : s2;
                        if (s3 + i8 > width) {
                            i8 = width - s3;
                        }
                        int i10 = i8;
                        if (s4 + i9 > height) {
                            i9 = height - s4;
                        }
                        int i11 = i9;
                        int i12 = partFrame.flip == 1 ? 2 : 0;
                        if (i4 == 2 || i4 == 6) {
                            i7 = -(i7 + i10);
                            i5 = i12 == 2 ? 0 : 2;
                        } else {
                            i5 = i12;
                        }
                        graphics.drawRegion(imgIcon.img, s3, s4, i10, i11, i5, i + i7 + this.dxx, i2 + partFrame.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean lockmoveByeffAuto() {
        return this.Typemove == 1;
    }

    public boolean CanpaintByeffauto() {
        return this.Typemove == 2;
    }

    public boolean isTanghinhbyEffauto() {
        return this.Typemove == 17;
    }

    public void update() {
        try {
            byte b = this.typeupdate;
            if (b == 0 || b == 1) {
                byte b2 = (byte) (this.f + 1);
                this.f = b2;
                byte[] bArr = this.sequence;
                if (b2 < bArr.length) {
                    this.Frame = bArr[b2];
                }
                if (b2 >= bArr.length) {
                    this.f = (byte) 0;
                    this.wantDetroy = true;
                    return;
                }
                return;
            }
            if (b == 2) {
                byte b3 = (byte) (this.f + 1);
                this.f = b3;
                byte[] bArr2 = this.sequence;
                if (b3 > bArr2.length - 1) {
                    this.f = (byte) 0;
                }
                byte b4 = this.f;
                if (b4 < bArr2.length) {
                    this.Frame = bArr2[b4];
                }
                if (this.timelive - System.currentTimeMillis() < 0) {
                    this.wantDetroy = true;
                    return;
                }
                return;
            }
            if (b != 3) {
                return;
            }
            byte b5 = (byte) (this.f + 1);
            this.f = b5;
            byte[] bArr3 = this.sequence;
            if (b5 < bArr3.length) {
                this.Frame = bArr3[b5];
            }
            if (b5 > bArr3.length) {
                if (this.waitLoop <= 0) {
                    this.f = (byte) 0;
                } else if (System.currentTimeMillis() - this.lasttime > this.waitLoop * 1000) {
                    this.f = (byte) 0;
                    this.lasttime = System.currentTimeMillis();
                }
            }
        } catch (Exception unused) {
        }
    }
}
