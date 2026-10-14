package game.model;

import effect.DataEffectThanThu;
import game.core.j2me.Graphics;
import game.render.Res;
import game.render.screen.GameData;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Vector;

public class ThanThu extends Char {
    public static Hashtable ALL_DATA_THAN_THU = new Hashtable();
    public static final byte ATTACK = 3;
    public static final byte DEAD = 1;
    public static final byte DEADFLY = 5;
    public static final byte INJURE = 4;
    public static final byte STAND = 0;
    public static final byte WALK = 2;
    short p1;
    short p2;
    short p3;
    short vx;
    short vy;
    public byte idThanThu = 0;
    public Vector data = new Vector();
    short lastDir = -100;
    byte huongY = 0;
    byte flip = 0;
    short xTo = 0;
    short yTo = 0;

    @Override
    public void paintCorner(Graphics graphics, int i, int i2) {
    }

    public ThanThu() {
        this.catagory = (byte) 0;
        this.speed = (short) 3;
        this.centerX = (short) 12;
        this.centerY = (short) 12;
        this.height = (short) 55;
        this.width = (short) 40;
        this.hp = 1;
        this.dyThanthu = (byte) 15;
        this.realHP = 1;
        this.dir = (short) r.nextInt(4);
        this.isDie = false;
    }

    @Override
    public int getIdThanThu() {
        return this.idThanThu;
    }

    public static void setDataThanThu(int i, byte[] bArr) {
        try {
            Vector vector = new Vector();
            if (((Vector) ALL_DATA_THAN_THU.get("" + i)) != null) {
                return;
            }
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
            int iAvailable = (short) dataInputStream.available();
            byte[] bArr2 = new byte[iAvailable];
            dataInputStream.read(bArr2, 0, iAvailable);
            vector.addElement(new DataEffectThanThu(bArr2));
            ALL_DATA_THAN_THU.put("" + i, vector);
            dataInputStream.close();
        } catch (Exception e) {
            try {
                e.printStackTrace();
            } catch (Exception unused) {
            }
        }
    }

    @Override
    public void setIdThanThu(int i) {
        this.idThanThu = (byte) i;
        try {
            this.data = (Vector) ALL_DATA_THAN_THU.get("" + i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPosTo(short s, short s2) {
        moveTo(s, s2);
    }

    @Override
    public void moveTo(short s, short s2) {
        this.xTo = s;
        this.yTo = s2;
        if (this.x == s && this.y == s2) {
            if (this.state == 3 || this.state == 1) {
                return;
            }
            this.state = (byte) 0;
            return;
        }
        this.state = (byte) 2;
    }

    public void setMove(int i, int i2) {
        boolean z;
        boolean z2;
        this.speed = (short) 4;
        int iAbs = Math.abs(this.x - i);
        int iAbs2 = Math.abs(this.y - i2);
        if (iAbs <= this.speed) {
            this.x = (short) i;
            z = true;
        } else {
            z = false;
        }
        if (iAbs2 < this.speed) {
            this.y = (short) i2;
            z2 = true;
        } else {
            z2 = false;
        }
        if (z && z2) {
            this.p3 = (short) 0;
            this.p2 = (short) 0;
            this.p1 = (short) 0;
            this.state = (byte) 0;
            this.vy = (short) 0;
            this.vx = (short) 0;
            this.lastDir = this.dir;
        } else if (this.x < i) {
            this.x = (short) (this.x + this.speed);
            this.dir = (short) 3;
        } else if (this.x > i) {
            this.x = (short) (this.x - this.speed);
            this.dir = (short) 2;
        } else if (this.y > i2) {
            this.y = (short) (this.y - this.speed);
            this.dir = (short) 1;
        } else if (this.y < i2) {
            this.dir = (short) 0;
            this.y = (short) (this.y + this.speed);
        }
        setHuong(i, i2);
    }

    public void setHuong(int i, int i2) {
        this.huongY = (byte) (this.y > i2 ? 1 : 0);
        this.flip = (byte) (this.x - i > 0 ? 0 : 2);
        if (this.data.size() > 0) {
            this.flip = (byte) 0;
            if (this.dir == 3) {
                this.flip = (byte) 2;
                return;
            }
            if (this.dir == 2) {
                return;
            }
            if (this.dir == 0) {
                this.huongY = (byte) 0;
            } else if (this.dir == 1) {
                this.huongY = (byte) 1;
            }
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.isDie || this.data.size() == 0) {
            return;
        }
        try {
            DataEffectThanThu dataEffectThanThu = (DataEffectThanThu) this.data.elementAt(0);
            if (dataEffectThanThu != null) {
                paintTopDataEff(graphics);
                byte b = this.state;
                ImageIcon imgIcon = GameData.getImgIcon((short) (this.idThanThu + 12000));
                graphics.drawImage(Res.imgShadow, this.x + (this.flip == 2 ? (short) -10 : (short) 10), this.y - 10, 3);
                if (imgIcon != null && imgIcon.img != null) {
                    dataEffectThanThu.paint(graphics, dataEffectThanThu.getFrame(this.frame, b, this.huongY), this.vangx + this.x, this.vangy + this.y, this.flip, imgIcon.img);
                }
            }
            paintBottomDataEff(graphics);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startAttack(LiveActor liveActor, AttackResult attackResult, byte b, byte b2) {
        this.state = (byte) 3;
        this.frame = (byte) 0;
    }

    @Override
    public void update() {
        if (this.data.size() > 0) {
            DataEffectThanThu dataEffectThanThu = (DataEffectThanThu) this.data.elementAt(0);
            byte b = this.state;
            if (this.state == 3 && this.frame >= dataEffectThanThu.getAnim(b, this.huongY).frame.length - 1) {
                this.state = (byte) 0;
                b = 0;
            }
            this.frame = (byte) ((this.frame + 1) % dataEffectThanThu.getAnim(b, this.huongY).frame.length);
        }
        byte b2 = this.state;
        if (b2 != 1) {
            if (b2 != 2) {
                return;
            }
            setMove(this.xTo, this.yTo);
        } else {
            if (this.hp <= 0 || this.realHP <= 0) {
                return;
            }
            this.state = (byte) 0;
        }
    }
}
