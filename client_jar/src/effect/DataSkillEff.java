package effect;

import game.model.ImageIcon;
import game.render.screen.GameData;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class DataSkillEff {
    public boolean isMatna;
    public Vector listFrame;
    public Vector listAnima;
    public SmallImage[] smallImage;
    public byte[][] frameChar;
    public byte[] sequence;
    public byte Frame;
    public byte f;
    public byte Frameeff;
    public boolean wantDetroy;
    public short x;
    public short y;
    public byte waitLoop;
    public byte indexStartSkill;
    public int idEff;
    int dxx;
    int dyy;
    public long timeRemove;
    public int typRequestImg;
    public byte Typemove;
    public boolean canremove;
    public boolean canActorMove;
    public boolean canPaintActor;
    public boolean canActorFight;
    public byte typeupdate;
    public static final byte NORMAL = 0;
    public static final byte REMOVE_BY_FRAME = 1;
    public static final byte REMOVE_BY_TIME = 2;
    public static final byte NO_REMOVE = 3;
    public byte dyhorse;
    Image img_pp;
    private boolean isremovebyTime;
    private byte loop;
    private long lasttime;
    public long timelive;
    public static byte TYPE_EFFECT_END = 0;
    public static byte TYPE_EFFECT_STARTSKILL = 1;
    public static byte TYPE_EFFECT_BUFF = 2;
    public static byte ACTOR_NORMAL = 0;
    public static byte ACTOR_CAN_NOT_MOVE = 1;
    public static byte ACTOR_CAN_NOT_PAINT = 2;
    public static Hashtable ALL_DATA_EFF = new Hashtable();

    public DataSkillEff(int id, int x, int y, long timelive, boolean canmove, boolean canpaint, boolean canFight, int loop, byte dyhorse) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]};
        this.idEff = 0;
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.timelive = timelive;
        this.idEff = (short) id;
        this.dyhorse = dyhorse;
        if (timelive == -1) {
            this.typeupdate = (byte) 3;
        } else if (timelive == 0) {
            this.typeupdate = (byte) 1;
        } else {
            this.typeupdate = (byte) 2;
        }
        this.canActorMove = canmove;
        this.canPaintActor = canpaint;
        this.canActorFight = canFight;
        this.loop = (byte) loop;
        PartFrame par = (PartFrame) ALL_DATA_EFF.get(new StringBuffer(String.valueOf(id)).toString());
        if (par != null) {
            loadData(par.data);
        }
    }

    public static DataInputStream openFile(String path) {
        DataInputStream file = new DataInputStream("".getClass().getResourceAsStream(path));
        return file;
    }

    public DataSkillEff(int id, int idimg) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]};
        this.idEff = 0;
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.idEff = id;
        try {
            InputStream input = openFile(new StringBuffer("/wea/").append(id).toString());
            DataInputStream dis = new DataInputStream(input);
            byte[] data = new byte[dis.available()];
            dis.read(data);
            loadData(data);
            this.img_pp = Image.createImage(new StringBuffer("/wea/").append(id).append(".png").toString());
        } catch (Exception e) {
        }
    }

    public DataSkillEff(int id, int x, int y) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]};
        this.idEff = 0;
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.x = (short) x;
        this.y = (short) y;
        this.typeupdate = (byte) 1;
        PartFrame par = (PartFrame) ALL_DATA_EFF.get(new StringBuffer(String.valueOf(id)).toString());
        if (par != null) {
            loadData(par.data);
        }
    }

    public DataSkillEff(int id, int x, int y, long timelive) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]};
        this.idEff = 0;
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.timelive = timelive;
        this.idEff = (short) id;
        if (timelive == -1) {
            this.typeupdate = (byte) 3;
        } else if (timelive == 0) {
            this.typeupdate = (byte) 1;
        } else {
            this.typeupdate = (byte) 2;
        }
        PartFrame par = (PartFrame) ALL_DATA_EFF.get(new StringBuffer(String.valueOf(id)).toString());
        if (par != null) {
            loadData(par.data);
        }
    }

    public DataSkillEff(byte[] array, short ideff, int dxx, int dyy) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]};
        this.idEff = 0;
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.idEff = ideff;
        this.dxx = dxx;
        this.dyy = dyy;
        loadData(array);
    }

    public void setWaitLoop(byte wait) {
        this.waitLoop = wait;
    }

    public DataSkillEff(byte[] array, short ideff, int dxx, int dyy, long time, byte typemove) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]};
        this.idEff = 0;
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.idEff = ideff;
        this.dxx = dxx;
        this.dyy = dyy;
        this.Typemove = typemove;
        this.timelive = time;
        this.isremovebyTime = true;
        loadData(array);
    }

    public DataSkillEff(int id) {
        this.listFrame = new Vector();
        this.listAnima = new Vector();
        this.frameChar = new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]};
        this.idEff = 0;
        this.typRequestImg = 112;
        this.typeupdate = (byte) 0;
        this.idEff = id;
        PartFrame par = (PartFrame) ALL_DATA_EFF.get(new StringBuffer(String.valueOf(id)).toString());
        if (par != null) {
            loadData(par.data);
        }
    }

    public void loadData(byte[] array) {
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
                Vector listParttop = new Vector();
                Vector listPartbottom = new Vector();
                for (int j = 0; j < npart; j++) {
                    PartFrame part = new PartFrame(dis.readShort(), dis.readShort(), dis.readByte());
                    part.flip = dis.readByte();
                    part.onTop = dis.readByte();
                    if (part.onTop == 0) {
                        listParttop.addElement(part);
                    } else {
                        listPartbottom.addElement(part);
                    }
                }
                this.listFrame.addElement(new FrameEff(listParttop, listPartbottom));
            }
            int unsignedByte = (short) dis.readUnsignedByte();
            this.sequence = new byte[unsignedByte];
            for (int i3 = 0; i3 < unsignedByte; i3++) {
                this.sequence[i3] = (byte) dis.readShort();
            }
            this.indexStartSkill = dis.readByte();
            int i4 = dis.readByte();
            this.frameChar[0] = new byte[i4];
            for (int i5 = 0; i5 < i4; i5++) {
                this.frameChar[0][i5] = dis.readByte();
            }
            int i6 = dis.readByte();
            this.frameChar[1] = new byte[i6];
            for (int i7 = 0; i7 < i6; i7++) {
                this.frameChar[1][i7] = dis.readByte();
            }
            int i8 = dis.readByte();
            this.frameChar[3] = new byte[i8];
            for (int i9 = 0; i9 < i8; i9++) {
                this.frameChar[3][i9] = dis.readByte();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paintBottomHorse(Graphics g, int x, int y, int Frame, int rotale) {
        if (Frame < this.listFrame.size()) {
            FrameEff frame = (FrameEff) this.listFrame.elementAt(Frame);
            try {
                Vector listPart = frame.listPartBottom;
                for (int i = 0; i < listPart.size(); i++) {
                    PartFrame part = (PartFrame) listPart.elementAt(i);
                    SmallImage small = this.smallImage[part.idSmallImg];
                    ImageIcon img = GameData.getImgIcon((short) this.idEff);
                    if (img != null && img.img != null) {
                        int dx = part.dx;
                        int w = small.w;
                        int h = small.h;
                        int xx = small.x;
                        int yy = small.y;
                        int wImg = img.img.getWidth();
                        int hImg = img.img.getHeight();
                        if (xx > wImg) {
                            xx = 0;
                        }
                        if (yy > hImg) {
                            yy = 0;
                        }
                        if (xx + w > wImg) {
                            w = wImg - xx;
                        }
                        if (yy + h > hImg) {
                            h = hImg - yy;
                        }
                        int rota = part.flip == 1 ? 2 : 0;
                        if (rotale == 2 || rotale == 6) {
                            if (rota == 2) {
                                rota = 0;
                            } else {
                                rota = 2;
                            }
                            dx = -(dx + w);
                        }
                        g.drawRegion(img.img, xx, yy, w, h, rota, x + dx + this.dxx, y + part.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintBottomTest(Graphics g, int x, int y, int Frame, int rotale) {
        if (Frame < this.listFrame.size()) {
            FrameEff frame = (FrameEff) this.listFrame.elementAt(Frame);
            try {
                Vector listPart = frame.listPartBottom;
                for (int i = 0; i < listPart.size(); i++) {
                    PartFrame part = (PartFrame) listPart.elementAt(i);
                    SmallImage small = this.smallImage[part.idSmallImg];
                    if (this.img_pp != null) {
                        int dx = part.dx;
                        int w = small.w;
                        int h = small.h;
                        int xx = small.x;
                        int yy = small.y;
                        int wImg = this.img_pp.getWidth();
                        int hImg = this.img_pp.getHeight();
                        if (xx > wImg) {
                            xx = 0;
                        }
                        if (yy > hImg) {
                            yy = 0;
                        }
                        if (xx + w > wImg) {
                            w = wImg - xx;
                        }
                        if (yy + h > hImg) {
                            h = hImg - yy;
                        }
                        int rota = part.flip == 1 ? 2 : 0;
                        if (rotale == 2 || rotale == 6) {
                            if (rota == 2) {
                                rota = 0;
                            } else {
                                rota = 2;
                            }
                            dx = -(dx + w);
                        }
                        g.drawRegion(this.img_pp, xx, yy, w, h, rota, x + dx + this.dxx, y + part.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintBot(Graphics g) {
        if (this.Frame < this.listFrame.size()) {
            FrameEff frame = (FrameEff) this.listFrame.elementAt(this.Frame);
            try {
                Vector listPart = frame.listPartBottom;
                for (int i = 0; i < listPart.size(); i++) {
                    PartFrame part = (PartFrame) listPart.elementAt(i);
                    SmallImage small = this.smallImage[part.idSmallImg];
                    ImageIcon img = GameData.getImgIcon((short) this.idEff);
                    if (img != null && img.img != null) {
                        int dx = part.dx;
                        int w = small.w;
                        int h = small.h;
                        int xx = small.x;
                        int yy = small.y;
                        int wImg = img.img.getWidth();
                        int hImg = img.img.getHeight();
                        if (xx > wImg) {
                            xx = 0;
                        }
                        if (yy > hImg) {
                            yy = 0;
                        }
                        if (xx + w > wImg) {
                            w = wImg - xx;
                        }
                        if (yy + h > hImg) {
                            h = hImg - yy;
                        }
                        int rota = part.flip == 1 ? 2 : 0;
                        int rotate = 0;
                        if (rotate == 2 || rotate == 6) {
                            if (rota == 2) {
                                rota = 0;
                            } else {
                                rota = 2;
                            }
                            dx = -(dx + w);
                        }
                        g.drawRegion(img.img, xx, yy, w, h, rota, this.x + dx + this.dxx, this.y + part.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintTop(Graphics g) {
        if (this.Frame < this.listFrame.size()) {
            FrameEff frame = (FrameEff) this.listFrame.elementAt(this.Frame);
            try {
                Vector listPart = frame.listPartTop;
                for (int i = 0; i < listPart.size(); i++) {
                    PartFrame part = (PartFrame) listPart.elementAt(i);
                    SmallImage small = this.smallImage[part.idSmallImg];
                    ImageIcon img = GameData.getImgIcon((short) this.idEff);
                    if (img != null && img.img != null) {
                        int dx = part.dx;
                        int w = small.w;
                        int h = small.h;
                        int xx = small.x;
                        int yy = small.y;
                        int wImg = img.img.getWidth();
                        int hImg = img.img.getHeight();
                        if (xx > wImg) {
                            xx = 0;
                        }
                        if (yy > hImg) {
                            yy = 0;
                        }
                        if (xx + w > wImg) {
                            w = wImg - xx;
                        }
                        if (yy + h > hImg) {
                            h = hImg - yy;
                        }
                        int rota = part.flip == 1 ? 2 : 0;
                        int rotate = 0;
                        if (rotate == 2 || rotate == 6) {
                            if (rota == 2) {
                                rota = 0;
                            } else {
                                rota = 2;
                            }
                            dx = -(dx + w);
                        }
                        g.drawRegion(img.img, xx, yy, w, h, rota, this.x + dx + this.dxx, this.y + part.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintTopTest(Graphics g, int x, int y, int Frame, int rotale) {
        if (Frame < this.listFrame.size()) {
            FrameEff frame = (FrameEff) this.listFrame.elementAt(Frame);
            try {
                Vector listPart = frame.listPartTop;
                for (int i = 0; i < listPart.size(); i++) {
                    PartFrame part = (PartFrame) listPart.elementAt(i);
                    SmallImage small = this.smallImage[part.idSmallImg];
                    if (this.img_pp != null) {
                        int dx = part.dx;
                        int w = small.w;
                        int h = small.h;
                        int xx = small.x;
                        int yy = small.y;
                        int wImg = this.img_pp.getWidth();
                        int hImg = this.img_pp.getHeight();
                        if (xx > wImg) {
                            xx = 0;
                        }
                        if (yy > hImg) {
                            yy = 0;
                        }
                        if (xx + w > wImg) {
                            w = wImg - xx;
                        }
                        if (yy + h > hImg) {
                            h = hImg - yy;
                        }
                        int rota = part.flip == 1 ? 2 : 0;
                        if (rotale == 2 || rotale == 6) {
                            if (rota == 2) {
                                rota = 0;
                            } else {
                                rota = 2;
                            }
                            dx = -(dx + w);
                        }
                        g.drawRegion(this.img_pp, xx, yy, w, h, rota, x + dx + this.dxx, y + part.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintTopHorse(Graphics g, int x, int y, int Frame, int rotale) {
        if (Frame < this.listFrame.size()) {
            FrameEff frame = (FrameEff) this.listFrame.elementAt(Frame);
            try {
                Vector listPart = frame.listPartTop;
                for (int i = 0; i < listPart.size(); i++) {
                    PartFrame part = (PartFrame) listPart.elementAt(i);
                    SmallImage small = this.smallImage[part.idSmallImg];
                    ImageIcon img = GameData.getImgIcon((short) this.idEff);
                    if (img != null && img.img != null) {
                        int dx = part.dx;
                        int w = small.w;
                        int h = small.h;
                        int xx = small.x;
                        int yy = small.y;
                        int wImg = img.img.getWidth();
                        int hImg = img.img.getHeight();
                        if (xx > wImg) {
                            xx = 0;
                        }
                        if (yy > hImg) {
                            yy = 0;
                        }
                        if (xx + w > wImg) {
                            w = wImg - xx;
                        }
                        if (yy + h > hImg) {
                            h = hImg - yy;
                        }
                        int rota = part.flip == 1 ? 2 : 0;
                        if (rotale == 2 || rotale == 6) {
                            if (rota == 2) {
                                rota = 0;
                            } else {
                                rota = 2;
                            }
                            dx = -(dx + w);
                        }
                        g.drawRegion(img.img, xx, yy, w, h, rota, x + dx + this.dxx, y + part.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintTop(Graphics g, int x, int y) {
        if (this.Frame < this.listFrame.size()) {
            FrameEff frame = (FrameEff) this.listFrame.elementAt(this.Frame);
            try {
                Vector vector = frame.listPartTop;
                Vector listPart = frame.listPartBottom;
                for (int i = 0; i < listPart.size(); i++) {
                    PartFrame part = (PartFrame) listPart.elementAt(i);
                    SmallImage small = this.smallImage[part.idSmallImg];
                    ImageIcon img = GameData.getImgIcon((short) this.idEff);
                    if (img != null && img.img != null) {
                        int dx = part.dx;
                        int w = small.w;
                        int h = small.h;
                        int xx = small.x;
                        int yy = small.y;
                        int wImg = img.img.getWidth();
                        int hImg = img.img.getHeight();
                        if (xx > wImg) {
                            xx = 0;
                        }
                        if (yy > hImg) {
                            yy = 0;
                        }
                        if (xx + w > wImg) {
                            w = wImg - xx;
                        }
                        if (yy + h > hImg) {
                            h = hImg - yy;
                        }
                        g.drawRegion(img.img, xx, yy, w, h, part.flip == 1 ? 2 : 0, x + dx + this.dxx, y + part.dy + this.dyy, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintBottom(Graphics g, int x, int y) {
        if (this.Frame < this.listFrame.size()) {
            FrameEff frame = (FrameEff) this.listFrame.elementAt(this.Frame);
            try {
                Vector listPart = frame.listPartTop;
                for (int i = 0; i < listPart.size(); i++) {
                    PartFrame part = (PartFrame) listPart.elementAt(i);
                    SmallImage small = this.smallImage[part.idSmallImg];
                    ImageIcon img = GameData.getImgIcon((short) this.idEff);
                    if (img != null && img.img != null) {
                        int dx = part.dx;
                        int w = small.w;
                        int h = small.h;
                        int xx = small.x;
                        int yy = small.y;
                        int wImg = img.img.getWidth();
                        int hImg = img.img.getHeight();
                        if (xx > wImg) {
                            xx = 0;
                        }
                        if (yy > hImg) {
                            yy = 0;
                        }
                        if (xx + w > wImg) {
                            w = wImg - xx;
                        }
                        if (yy + h > hImg) {
                            h = hImg - yy;
                        }
                        g.drawRegion(img.img, xx, yy, w, h, part.flip == 1 ? 2 : 0, x + dx + this.dxx, y + part.dy + this.dyy, 0);
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
            switch (this.typeupdate) {
                case 0:
                case 1:
                    this.f = (byte) (this.f + 1);
                    if (this.f < this.sequence.length) {
                        this.Frame = this.sequence[this.f];
                    }
                    if (this.f >= this.sequence.length) {
                        this.f = (byte) 0;
                        this.wantDetroy = true;
                    }
                    break;
                case 2:
                    this.f = (byte) (this.f + 1);
                    if (this.f > this.sequence.length - 1) {
                        this.f = (byte) 0;
                    }
                    if (this.f < this.sequence.length) {
                        this.Frame = this.sequence[this.f];
                    }
                    if (this.timelive - System.currentTimeMillis() < 0) {
                        this.wantDetroy = true;
                    }
                    break;
                case 3:
                    this.f = (byte) (this.f + 1);
                    if (this.f < this.sequence.length) {
                        this.Frame = this.sequence[this.f];
                    }
                    if (this.f > this.sequence.length) {
                        if (this.waitLoop <= 0) {
                            this.f = (byte) 0;
                        } else if (System.currentTimeMillis() - this.lasttime > this.waitLoop * 1000) {
                            this.f = (byte) 0;
                            this.lasttime = System.currentTimeMillis();
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
        }
    }
}
