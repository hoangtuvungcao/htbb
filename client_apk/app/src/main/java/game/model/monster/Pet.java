package game.model.monster;

import effect.mDataEffect;
import game.core.j2me.Graphics;
import game.model.Actor;
import game.model.ImageIcon;
import game.model.LiveActor;
import game.model.Point;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import java.util.Hashtable;
import java.util.Vector;

public class Pet extends LiveActor {
    public static final byte AC_DIE = 4;
    public static final byte AC_FIRE = 2;
    public static final byte AC_HIT = 3;
    public static final byte AC_MOVE = 1;
    public static final byte AC_STAND = 0;
    public static final byte ATTACK = 2;
    public static final byte ATTRACTION = 6;
    public static final byte DEATH = 5;
    public static final byte DIR_DOWN = 0;
    public static final byte DIR_LEFT = 2;
    public static final byte DIR_RIGHT = 3;
    public static final byte DIR_UP = 1;
    public static final byte FOLLOW = 1;
    public static final byte M_ATTACK = 3;
    public static final byte M_DEAD = 1;
    public static final byte M_STAND = 0;
    public static final byte M_WALK = 2;
    public static final byte RETURN = 3;
    public static final byte ROAM = 0;
    public static final byte WAIT = 4;
    protected byte Action;
    public int[] arrdame;
    public int[] arrhpcon;
    protected int attackCount;
    protected byte attackType;
    int cmove;
    public short dyattack;
    public byte f;
    byte frame;
    public short idSkill;
    public int idTemplate;
    protected byte imageId;
    boolean isBeginAttack;
    protected boolean isDoneAttack;
    boolean isRunAttack;
    protected boolean isSequenceAttack;
    protected int limitMove;
    public byte mAction;
    protected int oldPosX;
    protected int oldPosY;
    protected Actor owner;
    protected int ownerX;
    protected int ownerY;
    protected int p1;
    protected byte preState;
    public short range;
    protected int time;
    protected int timeAutoAction;
    public long timeBeginMoveAttack;
    int toX;
    int toY;
    byte typeAttack;
    public byte typemove;
    public int vMax;
    protected int vx;
    protected int vy;
    protected int xAnchor;
    public int xtam;
    protected int yAnchor;
    public short yfly;
    public static byte[] mTypemove = {2, 1, 0, 2, 0, 1};
    public static Hashtable ALL_PET_DATA = new Hashtable();
    protected final byte DIS_TO_ATTRACT = 80;
    protected final byte DIS_TO_FOLLOW = 40;
    public int vatam = 6;
    protected Vector wayPoint = new Vector();
    private Vector ntarget = new Vector();
    byte huongY = 0;
    byte flip = 0;
    public int maxAttack = 0;

    public void addAttackEffect() {
    }

    public boolean canFocus() {
        return false;
    }

    @Override
    public boolean ispetTool() {
        return true;
    }

    public void loadpetData() {
    }

    public void setAnim() {
    }

    @Override
    public void setPosTo(short s, short s2) {
    }

    public Pet(Actor actor, int i) {
        this.idTemplate = i;
        this.owner = actor;
        this.xAnchor = actor.x;
        this.yAnchor = actor.y;
        this.x = actor.x;
        this.y = actor.y;
        this.oldPosX = actor.x;
        this.oldPosY = actor.y;
        this.limitMove = 48;
        this.dir = (short) 0;
        this.vMax = 4;
        this.speed = (short) (actor.getSpeed() - 1);
        this.state = (byte) 0;
        this.Action = (byte) 0;
        this.mAction = (byte) 0;
        this.range = (short) 30;
        this.timeAutoAction = Res.random(200, 250);
        this.catagory = (byte) 12;
        this.ID = actor.ID;
    }

    public Pet(int i) {
        this.idTemplate = i;
        this.dir = (short) 0;
        this.state = (byte) 0;
        this.Action = (byte) 0;
        this.mAction = (byte) 0;
        this.range = (short) 30;
        this.timeAutoAction = Res.random(200, 250);
        this.catagory = (byte) 12;
    }

    public Pet(short s, byte b, byte b2, byte b3) {
    }

    public void clearWayPoints() {
        this.wayPoint.removeAllElements();
    }

    public void setState(byte b) {
        this.state = b;
    }

    @Override
    public void paint(Graphics graphics) {
        ImageIcon imgIcon;
        try {
            mDataEffect mdataeffect = (mDataEffect) ALL_PET_DATA.get("" + this.idTemplate);
            if (mdataeffect == null || mdataeffect == null) {
                return;
            }
            byte b = this.mAction;
            if (this.state == 2 && this.isBeginAttack) {
                b = 3;
            }
            byte[] dxDyShadow = mdataeffect.getDxDyShadow(mdataeffect.getFrame(this.frame, b, this.huongY));
            if (isFly()) {
                graphics.drawImage(GameScr.petShadow, this.x + dxDyShadow[0], this.y + dxDyShadow[1], 0);
            } else {
                graphics.drawImage(GameScr.petShadow, this.x + dxDyShadow[0], this.y + dxDyShadow[1], 0);
            }
            if (this.width == 0 || this.height == 0) {
                this.width = mdataeffect.getWith();
                this.height = mdataeffect.getHeight();
            }
            int i = this.idTemplate;
            if (i < 301) {
                imgIcon = GameData.getImgIcon((short) (i + 8700));
            } else {
                imgIcon = GameData.getImgIcon((short) (i + 14100));
            }
            if (imgIcon == null || imgIcon.img == null) {
                return;
            }
            mdataeffect.paintPet(graphics, mdataeffect.getFrame(this.frame, b, this.huongY), this.x, this.y, this.flip, imgIcon.img, this.dyattack);
        } catch (Exception unused) {
        }
    }

    public boolean isFly() {
        mDataEffect mdataeffect = (mDataEffect) ALL_PET_DATA.get("" + this.idTemplate);
        return mdataeffect != null && mdataeffect.isFly <= -5;
    }

    protected void roam() {
        this.vMax = 1;
        byte b = this.Action;
        if (b == 1) {
            if (this.time > this.timeAutoAction || Res.random(16) == 0 || Util.getDistance(this.x + this.vx, this.y + this.vy, this.xAnchor, this.yAnchor) >= this.limitMove) {
                this.time = 0;
                this.Action = (byte) 0;
                this.mAction = (byte) 0;
                this.vx = 0;
                this.vy = 0;
            }
        } else if (b == 0) {
            this.vx = 0;
            this.vy = 0;
            if (this.time > this.timeAutoAction / 2 || Res.random(12) == 0) {
                this.time = 0;
                this.Action = (byte) 1;
                this.mAction = (byte) 2;
                this.dir = (short) Res.random(4);
                setSpeedInDirection(this.vMax);
            }
        }
        Actor actor = this.owner;
        if (actor != null) {
            if (actor.getState() == 1 && Util.getDistance(this.x, this.y, this.ownerX, this.ownerY) > 40) {
                setState((byte) 1);
            }
            if (this.owner.getState() != 0 || Util.getDistance(this.x, this.y, this.ownerX, this.ownerY) <= this.limitMove * 2) {
                return;
            }
            setState((byte) 3);
            return;
        }
        int distance = Util.getDistance(this.x, this.y, GCanvas.gameScr.mainChar.x, GCanvas.gameScr.mainChar.y);
        if (distance >= 80 || distance <= 40 || Res.random(6) != 0) {
            return;
        }
        setState((byte) 6);
    }

    public void setSpeedInDirection(int i) {
        int iRandom_Am_0 = Res.random_Am_0(3);
        if (Util.abs(iRandom_Am_0) > 1) {
            i--;
        }
        short s = this.dir;
        if (s == 0) {
            this.vy = i;
            this.vx = iRandom_Am_0;
        } else if (s == 1) {
            this.vy = -i;
            this.vx = iRandom_Am_0;
        } else if (s == 2) {
            this.vy = iRandom_Am_0;
            this.vx = -i;
        } else if (s == 3) {
            this.vy = iRandom_Am_0;
            this.vx = i;
        }
        if (this.vx == 0 && Res.random(3) == 0) {
            this.time = 0;
            this.state = (byte) 0;
            this.vx = 0;
            this.vy = 0;
            this.mAction = (byte) 0;
        }
        if (this.vx > 0) {
            this.dir = (short) 3;
        } else {
            this.dir = (short) 2;
        }
        this.huongY = (byte) (this.vy > 0 ? 1 : 0);
        this.flip = (byte) 0;
        if (this.dir == 3) {
            this.flip = (byte) 2;
        } else if (this.dir != 2) {
            if (this.dir == 0) {
                this.huongY = (byte) 0;
            } else if (this.dir == 1) {
                this.huongY = (byte) 1;
            }
        }
        this.mAction = (byte) 2;
    }

    public void setHuong(int i, int i2) {
        this.huongY = (byte) (this.y > i2 ? 1 : 0);
        short s = this.x;
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

    protected void standStill() {
        this.vx = 0;
        this.vy = 0;
        this.state = (byte) 4;
        Actor actor = this.owner;
        if (actor == null || actor.getState() != 0) {
            return;
        }
        setState((byte) 0);
    }

    protected void follow() {
        this.vMax = this.owner.getSpeed();
        this.Action = (byte) 1;
        if (this.wayPoint.size() <= 0) {
            int i = this.cmove + 1;
            this.cmove = i;
            if (i > 20) {
                setState((byte) 0);
            }
        }
        if (Util.getDistance(this.oldPosX, this.oldPosY, this.ownerX, this.ownerY) > 40) {
            Point point = new Point(this.ownerX, this.ownerY);
            this.oldPosX = this.ownerX;
            this.oldPosY = this.ownerY;
            this.wayPoint.addElement(point);
        } else if (Util.getDistance(this.x, this.y, this.xAnchor, this.yAnchor) < 40) {
            this.wayPoint.removeAllElements();
            setState((byte) 4);
        }
        if (this.wayPoint.size() <= 0 || this.wayPoint.elementAt(0) == null) {
            return;
        }
        this.toX = ((Point) this.wayPoint.elementAt(0)).x;
        int i2 = ((Point) this.wayPoint.elementAt(0)).y;
        this.toY = i2;
        setHuong(this.toX, i2);
        move_to_XY();
    }

    public void move_to_XY() {
        int iAbs = Math.abs(this.x - this.toX);
        int iAbs2 = Math.abs(this.y - this.toY);
        if (iAbs <= this.speed) {
            this.x = (short) this.toX;
        }
        if (iAbs2 < this.speed) {
            this.y = (short) this.toY;
        }
        if (this.x < this.toX) {
            this.x = (short) (this.x + this.speed);
            this.dir = (short) 3;
        } else if (this.x > this.toX) {
            this.x = (short) (this.x - this.speed);
            this.dir = (short) 2;
        } else if (this.y > this.toY) {
            this.y = (short) (this.y - this.speed);
            this.dir = (short) 1;
        } else if (this.y < this.toY) {
            this.dir = (short) 0;
            this.y = (short) (this.y + this.speed);
        }
        this.huongY = (byte) (this.vy > 0 ? 1 : 0);
        this.flip = (byte) 0;
        if (this.dir == 3) {
            this.flip = (byte) 2;
        } else if (this.dir != 2) {
            if (this.dir == 0) {
                this.huongY = (byte) 0;
            } else if (this.dir == 1) {
                this.huongY = (byte) 1;
            }
        }
        if (this.state != 3) {
            if (this.owner.getDir() == 1 || this.owner.getDir() == 0) {
                int i = this.xtam;
                int i2 = this.vatam;
                this.xtam = i + i2;
                if (i2 > 0) {
                    this.dir = (short) 3;
                }
                if (this.vatam < 0) {
                    this.dir = (short) 2;
                }
                int i3 = this.xtam;
                int i4 = this.vatam;
                if ((i3 + i4 < 48 || i4 <= 0) && (i3 + i4 >= -48 || i4 >= 0)) {
                    return;
                }
                this.vatam = i4 * (-1);
            }
        }
    }

    public void initAttackState(byte b) {
        try {
            this.attackType = b;
            this.isRunAttack = true;
            this.timeBeginMoveAttack = System.currentTimeMillis();
            this.attackCount = 0;
            this.isSequenceAttack = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void petAttack(Vector vector, byte b, short s, int[] iArr, int[] iArr2) {
        this.state = (byte) 2;
        this.ntarget = vector;
        this.typeAttack = b;
        this.idSkill = s;
        this.arrdame = iArr;
        this.arrhpcon = iArr2;
    }

    protected void attack() {
        Vector vector = this.ntarget;
        if (vector != null && vector.size() > 0) {
            Actor actor = (Actor) this.ntarget.elementAt(0);
            if (actor == null) {
                setState((byte) 1);
                return;
            }
            if (Util.getDistance(this.x + this.speed, this.y + this.speed, actor.x, actor.y) > this.range && !this.isBeginAttack && actor.getHp() > 0) {
                this.toX = actor.x;
                short s = actor.y;
                this.toY = s;
                setHuong(this.toX, s);
                move_to_XY();
                this.mAction = (byte) 2;
                if (this.dyattack < 40 && isFly()) {
                    this.dyattack = (short) (this.dyattack + 1);
                }
            } else {
                this.isBeginAttack = true;
            }
            if (this.isBeginAttack) {
                if (this.dyattack < 50 && isFly()) {
                    this.dyattack = (short) (this.dyattack + 10);
                }
                int i = this.p1 + 1;
                this.p1 = i;
                if (i > 6) {
                    this.p1 = 0;
                    this.isBeginAttack = false;
                    setState((byte) 4);
                    for (int i2 = 0; i2 < this.ntarget.size(); i2++) {
                        Actor actor2 = (Actor) this.ntarget.elementAt(i2);
                        if (actor2 != null) {
                            GCanvas.gameScr.startFlyText(this.arrdame[i2] + "", 0, actor2.x, actor2.y - 15, 1, -2);
                            actor2.setHP(this.arrhpcon[i2]);
                            actor2.addEffectSkillTime(this.idSkill, actor2.x, actor2.y, 0L, false, false, false, 0, (byte) 0, (byte) 0);
                        }
                    }
                    return;
                }
                return;
            }
            return;
        }
        int i3 = this.p1 + 1;
        this.p1 = i3;
        this.isBeginAttack = true;
        if (i3 > 6) {
            this.p1 = 0;
            this.isBeginAttack = false;
            setState((byte) 4);
        }
    }

    protected void returnToPlayer() {
        this.vMax = this.owner.getSpeed();
        this.toX = this.owner.x;
        short s = this.owner.y;
        this.toY = s;
        setHuong(this.toX, s);
        move_to_XY();
        if (Util.getDistance(this.x, this.y, this.xAnchor, this.yAnchor) >= 40 || this.owner.getState() == 2) {
            return;
        }
        setState((byte) 0);
    }

    protected void waitForCommand() {
        this.vx = 0;
        this.vy = 0;
        this.Action = (byte) 1;
        Actor actor = this.owner;
        if (actor != null) {
            if (actor.getState() == 0) {
                this.wayPoint.removeAllElements();
                setState((byte) 0);
            } else {
                if (this.owner.getState() != 1 || Util.getDistance(this.x, this.y, this.xAnchor, this.yAnchor) <= 40) {
                    return;
                }
                this.wayPoint.removeAllElements();
                setState((byte) 1);
            }
        }
    }

    private void attract() {
        this.vMax = 3;
        this.state = (byte) 1;
        this.toX = GCanvas.gameScr.mainChar.x;
        this.toY = GCanvas.gameScr.mainChar.y;
        move_to_XY();
        if (Util.getDistance(this.x, this.y, this.toX, this.toY) < 40) {
            setState((byte) 0);
        }
    }

    public void doChangeFrmaeBoss() {
        mDataEffect mdataeffect = (mDataEffect) ALL_PET_DATA.get("" + this.idTemplate);
        if (mdataeffect != null) {
            byte b = this.mAction;
            if (this.state == 2 && this.isBeginAttack) {
                b = 3;
            }
            this.frame = (byte) ((this.frame + 1) % mdataeffect.getAnim(b, this.huongY).frame.length);
            if (this.maxAttack == 0) {
                this.maxAttack = mdataeffect.getAnim(3, this.huongY).frame.length;
            }
        }
    }

    public void updateMenu() {
        if (GCanvas.gameTick % 2 == 0) {
            this.f = (byte) ((this.f + 1) % 3);
        }
        if (this.state != 2) {
            this.x = (short) (this.x + this.vx);
            this.y = (short) (this.y + this.vy);
        }
        doChangeFrmaeBoss();
        if (GCanvas.gameTick % 200 == 0) {
            this.state = (byte) 2;
        }
        if (this.state == 2) {
            attack();
        }
    }

    @Override
    public void update() {
        short s;
        try {
            Actor actor = this.owner;
            if (actor == null || (actor != null && actor.wantDestroy)) {
                this.wantDestroy = true;
            }
            byte b = this.state;
            if (b == 0) {
                roam();
            } else if (b == 1) {
                this.mAction = (byte) 2;
                follow();
            } else if (b == 2) {
                attack();
            } else if (b == 3) {
                returnToPlayer();
                this.mAction = (byte) 2;
            } else if (b == 4) {
                this.mAction = (byte) 0;
                waitForCommand();
            } else if (b == 6) {
                attract();
            }
            if (!this.isBeginAttack && (s = this.dyattack) > 0) {
                short s2 = (short) (s - 3);
                this.dyattack = s2;
                if (s2 < 0) {
                    this.dyattack = (short) 0;
                }
            }
            if (GCanvas.gameTick % 2 == 0) {
                this.f = (byte) ((this.f + 1) % 3);
            }
            if (this.state != 2) {
                this.x = (short) (this.x + this.vx);
                this.y = (short) (this.y + this.vy);
            }
            doChangeFrmaeBoss();
            if (this.owner != null) {
                if (Util.getDistance(this.x, this.y, this.toX, this.toY) <= 10 && this.wayPoint.size() > 0) {
                    this.wayPoint.removeElementAt(0);
                }
                this.xAnchor = this.owner.x;
                this.yAnchor = this.owner.y;
                this.ownerX = this.owner.x;
                this.ownerY = this.owner.y;
            }
            this.time++;
            if (this.state != this.preState) {
                this.preState = this.state;
            }
        } catch (Exception unused) {
        }
    }

    public void startAttack(Vector vector, int i) {
        this.state = (byte) 2;
    }

    public void setidTemplatePet(short s) {
        this.idTemplate = s;
    }

    @Override
    public void petAttack(byte b, Vector vector, int[] iArr, short s) {
        this.range = s;
        this.idSkill = b;
        this.ntarget = vector;
        this.state = (byte) 2;
        this.dyattack = (short) 0;
    }

    @Override
    public boolean isMypet() {
        return this.owner.ID == GCanvas.gameScr.mainChar.ID;
    }

    @Override
    public int getIDTem() {
        return this.idTemplate;
    }

    public short getOwnerID() {
        return this.ID;
    }
}
