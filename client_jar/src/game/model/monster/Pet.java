package game.model.monster;

import effect.mDataEffect;
import game.model.Actor;
import game.model.AnimalMove;
import game.model.ImageIcon;
import game.model.Point;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class Pet extends AnimalMove {
    public static final byte ROAM = 0;
    public static final byte FOLLOW = 1;
    public static final byte ATTACK = 2;
    public static final byte RETURN = 3;
    public static final byte WAIT = 4;
    public static final byte DEATH = 5;
    public static final byte ATTRACTION = 6;
    protected boolean isSequenceAttack;
    protected boolean isDoneAttack;
    protected byte imageId;
    protected byte Action;
    protected byte preState;
    protected byte attackType;
    protected int attackCount;
    protected int oldPosX;
    protected int oldPosY;
    protected int ownerX;
    protected int p1;
    protected int ownerY;
    protected int time;
    protected int timeAutoAction;
    protected int vx;
    protected int vy;
    protected int xAnchor;
    protected int yAnchor;
    protected int limitMove;
    public int xtam;
    public int vMax;
    public int idTemplate;
    public short yfly;
    public short range;
    public short dyattack;
    public short idSkill;
    public byte typemove;
    public byte f;
    public byte mAction;
    protected Actor owner;
    boolean isBeginAttack;
    public static final byte M_STAND = 0;
    public static final byte M_DEAD = 1;
    public static final byte M_WALK = 2;
    public static final byte M_ATTACK = 3;
    public static final byte AC_STAND = 0;
    public static final byte AC_MOVE = 1;
    public static final byte AC_FIRE = 2;
    public static final byte AC_HIT = 3;
    public static final byte AC_DIE = 4;
    public static final byte DIR_UP = 1;
    public static final byte DIR_DOWN = 0;
    public static final byte DIR_LEFT = 2;
    public static final byte DIR_RIGHT = 3;
    byte frame;
    int toX;
    int toY;
    int cmove;
    boolean isRunAttack;
    public long timeBeginMoveAttack;
    public int[] arrdame;
    public int[] arrhpcon;
    byte typeAttack;
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

    public Pet(Actor owner, int idtemplate) {
        this.idTemplate = idtemplate;
        this.owner = owner;
        this.xAnchor = owner.x;
        this.yAnchor = owner.y;
        this.x = owner.x;
        this.y = owner.y;
        this.oldPosX = owner.x;
        this.oldPosY = owner.y;
        this.limitMove = 48;
        ((AnimalMove) this).dir = (short) 0;
        this.vMax = 4;
        this.speed = (short) (owner.getSpeed() - 1);
        this.state = (byte) 0;
        this.Action = (byte) 0;
        this.mAction = (byte) 0;
        this.range = (short) 30;
        this.timeAutoAction = Res.random(200, 250);
        this.catagory = (byte) 12;
        this.ID = owner.ID;
    }

    public void loadpetData() {
    }

    public Pet(int idtemplate) {
        this.idTemplate = idtemplate;
        ((AnimalMove) this).dir = (short) 0;
        this.state = (byte) 0;
        this.Action = (byte) 0;
        this.mAction = (byte) 0;
        this.range = (short) 30;
        this.timeAutoAction = Res.random(200, 250);
        this.catagory = (byte) 12;
    }

    public Pet(short typePet, byte nFrame, byte imageId, byte typemove) {
    }

    public void setAnim() {
    }

    public void clearWayPoints() {
        this.wayPoint.removeAllElements();
    }

    public void setState(byte state) {
        this.state = state;
    }

    @Override
    public void paint(Graphics g) {
        ImageIcon pimg;
        try {
            // pokemon (P2): pet co vat pham (kieu 5 = pet-tool: trung, Hoothoot/Dratini/Cleffa/Zubat/Rufflet, Lan hoa
            // hinh...) ve Pokemon theo lenh -94 nhu AnimalMove; khong co dex/sprite thi moi ve anh hieu ung cu (Unity Pet.cs)
            game.model.Char chu = this.myOwner;
            if (chu == null && this.owner instanceof game.model.Char) {
                chu = (game.model.Char) this.owner;
            }
            if (vePokePet(g, chu)) {
                return;
            }
            mDataEffect deff = (mDataEffect) ALL_PET_DATA.get(new StringBuffer().append(this.idTemplate).toString());
            if (deff != null && deff != null) {
                int status = this.mAction;
                if (this.state == 2 && this.isBeginAttack) {
                    status = 3;
                }
                byte[] dxdyshadow = deff.getDxDyShadow(deff.getFrame(this.frame, status, this.huongY));
                if (isFly()) {
                    int xsd = this.x + dxdyshadow[0];
                    int ysd = this.y + dxdyshadow[1];
                    g.drawImage(GameScr.petShadow, xsd, ysd, 0);
                } else {
                    int xsd2 = this.x + dxdyshadow[0];
                    int ysd2 = this.y + dxdyshadow[1];
                    g.drawImage(GameScr.petShadow, xsd2, ysd2, 0);
                }
                if (this.width == 0 || this.height == 0) {
                    this.width = deff.getWith();
                    this.height = deff.getHeight();
                }
                if (this.idTemplate < 301) {
                    pimg = GameData.getImgIcon((short) (this.idTemplate + 8700));
                } else {
                    pimg = GameData.getImgIcon((short) (this.idTemplate + 14100));
                }
                if (pimg != null && pimg.img != null) {
                    deff.paintPet(g, deff.getFrame(this.frame, status, this.huongY), this.x, this.y, this.flip, pimg.img, this.dyattack);
                }
            }
        } catch (Exception e) {
        }
    }

    /** Pet-tool: imgID/idTemplate la id hieu ung, khong tra bang pet kieu cu (Unity Pet.dexPetCu). */
    @Override
    protected int dexPetCu() {
        return 0;
    }

    public boolean isFly() {
        mDataEffect deff = (mDataEffect) ALL_PET_DATA.get(new StringBuffer().append(this.idTemplate).toString());
        if (deff != null && deff.isFly <= -5) {
            return true;
        }
        return false;
    }

    protected void roam() {
        this.vMax = 1;
        if (this.Action == 1) {
            if (this.time > this.timeAutoAction || Res.random(16) == 0 || Util.getDistance(this.x + this.vx, this.y + this.vy, this.xAnchor, this.yAnchor) >= this.limitMove) {
                this.time = 0;
                this.Action = (byte) 0;
                this.mAction = (byte) 0;
                this.vx = 0;
                this.vy = 0;
            }
        } else if (this.Action == 0) {
            this.vx = 0;
            this.vy = 0;
            if (this.time > this.timeAutoAction / 2 || Res.random(12) == 0) {
                this.time = 0;
                this.Action = (byte) 1;
                this.mAction = (byte) 2;
                ((AnimalMove) this).dir = (short) Res.random(4);
                setSpeedInDirection(this.vMax);
            }
        }
        if (this.owner != null) {
            if (this.owner.getState() == 1 && Util.getDistance(this.x, this.y, this.ownerX, this.ownerY) > 40) {
                setState((byte) 1);
            }
            if (this.owner.getState() == 0 && Util.getDistance(this.x, this.y, this.ownerX, this.ownerY) > this.limitMove * 2) {
                setState((byte) 3);
                return;
            }
            return;
        }
        int disToPlayer = Util.getDistance(this.x, this.y, Canvas.gameScr.mainChar.x, Canvas.gameScr.mainChar.y);
        if (disToPlayer < 80 && disToPlayer > 40 && Res.random(6) == 0) {
            setState((byte) 6);
        }
    }

    public void setSpeedInDirection(int max) {
        int vplus = Res.random_Am_0(3);
        if (Util.abs(vplus) > 1) {
            max--;
        }
        switch (((AnimalMove) this).dir) {
            case 0:
                this.vy = max;
                this.vx = vplus;
                break;
            case 1:
                this.vy = -max;
                this.vx = vplus;
                break;
            case 2:
                this.vy = vplus;
                this.vx = -max;
                break;
            case 3:
                this.vy = vplus;
                this.vx = max;
                break;
        }
        if (this.vx == 0 && Res.random(3) == 0) {
            this.time = 0;
            this.state = (byte) 0;
            this.vx = 0;
            this.vy = 0;
            this.mAction = (byte) 0;
        }
        if (this.vx > 0) {
            ((AnimalMove) this).dir = (short) 3;
        } else {
            ((AnimalMove) this).dir = (short) 2;
        }
        this.huongY = (byte) (this.vy > 0 ? 1 : 0);
        this.flip = (byte) (this.vx > 0 ? 0 : 2);
        this.flip = (byte) 0;
        if (((AnimalMove) this).dir == 3) {
            this.flip = (byte) 2;
        } else if (((AnimalMove) this).dir != 2) {
            if (((AnimalMove) this).dir == 0) {
                this.huongY = (byte) 0;
            } else if (((AnimalMove) this).dir == 1) {
                this.huongY = (byte) 1;
            }
        }
        this.mAction = (byte) 2;
    }

    public void setHuong(int xto, int yto) {
        this.huongY = (byte) (this.y > yto ? 1 : 0);
        this.flip = (byte) (this.x - xto > 0 ? 0 : 2);
        this.flip = (byte) 0;
        if (((AnimalMove) this).dir == 3) {
            this.flip = (byte) 2;
            return;
        }
        if (((AnimalMove) this).dir != 2) {
            if (((AnimalMove) this).dir == 0) {
                this.huongY = (byte) 0;
            } else if (((AnimalMove) this).dir == 1) {
                this.huongY = (byte) 1;
            }
        }
    }

    protected void standStill() {
        this.vx = 0;
        this.vy = 0;
        this.state = (byte) 4;
        if (this.owner != null && this.owner.getState() == 0) {
            setState((byte) 0);
        }
    }

    protected void follow() {
        this.vMax = this.owner.getSpeed();
        this.Action = (byte) 1;
        if (this.wayPoint.size() <= 0) {
            this.cmove++;
            if (this.cmove > 20) {
                setState((byte) 0);
            }
        }
        if (Util.getDistance(this.oldPosX, this.oldPosY, this.ownerX, this.ownerY) > 40) {
            Point p = new Point(this.ownerX, this.ownerY);
            this.oldPosX = this.ownerX;
            this.oldPosY = this.ownerY;
            this.wayPoint.addElement(p);
        } else if (Util.getDistance(this.x, this.y, this.xAnchor, this.yAnchor) < 40) {
            this.wayPoint.removeAllElements();
            setState((byte) 4);
        }
        if (this.wayPoint.size() > 0 && this.wayPoint.elementAt(0) != null) {
            this.toX = ((Point) this.wayPoint.elementAt(0)).x;
            this.toY = ((Point) this.wayPoint.elementAt(0)).y;
            setHuong(this.toX, this.toY);
            move_to_XY();
        }
    }

    public void move_to_XY() {
        int dx1 = Math.abs(this.x - this.toX);
        int dy1 = Math.abs(this.y - this.toY);
        if (dx1 <= this.speed) {
            this.x = (short) this.toX;
        }
        if (dy1 < this.speed) {
            this.y = (short) this.toY;
        }
        if (this.x < this.toX) {
            this.x = (short) (this.x + this.speed);
            ((AnimalMove) this).dir = (short) 3;
        } else if (this.x > this.toX) {
            this.x = (short) (this.x - this.speed);
            ((AnimalMove) this).dir = (short) 2;
        } else if (this.y > this.toY) {
            this.y = (short) (this.y - this.speed);
            ((AnimalMove) this).dir = (short) 1;
        } else if (this.y < this.toY) {
            ((AnimalMove) this).dir = (short) 0;
            this.y = (short) (this.y + this.speed);
        }
        this.huongY = (byte) (this.vy > 0 ? 1 : 0);
        this.flip = (byte) (this.vx > 0 ? 0 : 2);
        this.flip = (byte) 0;
        if (((AnimalMove) this).dir == 3) {
            this.flip = (byte) 2;
        } else if (((AnimalMove) this).dir != 2) {
            if (((AnimalMove) this).dir == 0) {
                this.huongY = (byte) 0;
            } else if (((AnimalMove) this).dir == 1) {
                this.huongY = (byte) 1;
            }
        }
        if (this.state != 3) {
            if (this.owner.getDir() == 1 || this.owner.getDir() == 0) {
                this.xtam += this.vatam;
                if (this.vatam > 0) {
                    ((AnimalMove) this).dir = (short) 3;
                }
                if (this.vatam < 0) {
                    ((AnimalMove) this).dir = (short) 2;
                }
                if ((this.xtam + this.vatam >= 48 && this.vatam > 0) || (this.xtam + this.vatam < -48 && this.vatam < 0)) {
                    this.vatam *= -1;
                }
            }
        }
    }

    public void initAttackState(byte skillId) {
        try {
            this.attackType = skillId;
            this.isRunAttack = true;
            this.timeBeginMoveAttack = System.currentTimeMillis();
            this.attackCount = 0;
            this.isSequenceAttack = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void petAttack(Vector target, byte type, short idSkill, int[] dame, int[] hpcon) {
        this.state = (byte) 2;
        this.ntarget = target;
        this.typeAttack = type;
        this.idSkill = idSkill;
        this.arrdame = dame;
        this.arrhpcon = hpcon;
    }

    protected void attack() {
        if (this.ntarget != null && this.ntarget.size() > 0) {
            Actor target = (Actor) this.ntarget.elementAt(0);
            if (target == null) {
                setState((byte) 1);
                return;
            }
            if (Util.getDistance(this.x + this.speed, this.y + this.speed, target.x, target.y) > this.range && !this.isBeginAttack && target.getHp() > 0) {
                this.toX = target.x;
                this.toY = target.y;
                setHuong(this.toX, this.toY);
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
                this.p1++;
                if (this.p1 > 6) {
                    this.p1 = 0;
                    this.isBeginAttack = false;
                    setState((byte) 4);
                    for (int i = 0; i < this.ntarget.size(); i++) {
                        Actor ac = (Actor) this.ntarget.elementAt(i);
                        if (ac != null) {
                            Canvas.gameScr.startFlyText(new StringBuffer(String.valueOf(this.arrdame[i])).toString(), 0, ac.x, ac.y - 15, 1, -2);
                            ac.setHP(this.arrhpcon[i]);
                            ac.addEffectSkillTime(this.idSkill, ac.x, ac.y, 0L, false, false, false, 0, (byte) 0, (byte) 0);
                        }
                    }
                    return;
                }
                return;
            }
            return;
        }
        this.p1++;
        this.isBeginAttack = true;
        if (this.p1 > 6) {
            this.p1 = 0;
            this.isBeginAttack = false;
            setState((byte) 4);
        }
    }

    protected void returnToPlayer() {
        this.vMax = this.owner.getSpeed();
        this.toX = this.owner.x;
        this.toY = this.owner.y;
        setHuong(this.toX, this.toY);
        move_to_XY();
        if (Util.getDistance(this.x, this.y, this.xAnchor, this.yAnchor) < 40 && this.owner.getState() != 2) {
            setState((byte) 0);
        }
    }

    protected void waitForCommand() {
        this.vx = 0;
        this.vy = 0;
        this.Action = (byte) 1;
        if (this.owner != null) {
            if (this.owner.getState() == 0) {
                this.wayPoint.removeAllElements();
                setState((byte) 0);
            } else if (this.owner.getState() == 1 && Util.getDistance(this.x, this.y, this.xAnchor, this.yAnchor) > 40) {
                this.wayPoint.removeAllElements();
                setState((byte) 1);
            }
        }
    }

    public void addAttackEffect() {
    }

    private void attract() {
        this.vMax = 3;
        this.state = (byte) 1;
        this.toX = Canvas.gameScr.mainChar.x;
        this.toY = Canvas.gameScr.mainChar.y;
        move_to_XY();
        if (Util.getDistance(this.x, this.y, this.toX, this.toY) < 40) {
            setState((byte) 0);
        }
    }

    public void doChangeFrmaeBoss() {
        mDataEffect deff = (mDataEffect) ALL_PET_DATA.get(new StringBuffer().append(this.idTemplate).toString());
        if (deff != null) {
            int status = this.mAction;
            if (this.state == 2 && this.isBeginAttack) {
                status = 3;
            }
            this.frame = (byte) ((this.frame + 1) % deff.getAnim(status, this.huongY).frame.length);
            if (this.maxAttack == 0) {
                this.maxAttack = deff.getAnim(3, this.huongY).frame.length;
            }
        }
    }

    public void updateMenu() {
        if (Canvas.gameTick % 2 == 0) {
            this.f = (byte) ((this.f + 1) % 3);
        }
        if (this.state != 2) {
            this.x = (short) (this.x + this.vx);
            this.y = (short) (this.y + this.vy);
        }
        doChangeFrmaeBoss();
        if (Canvas.gameTick % 200 == 0) {
            this.state = (byte) 2;
        }
        if (this.state == 2) {
            attack();
        }
    }

    @Override
    public void update() {
        try {
            if (this.owner == null || (this.owner != null && this.owner.wantDestroy)) {
                this.wantDestroy = true;
            }
            switch (this.state) {
                case 0:
                    roam();
                    break;
                case 1:
                    this.mAction = (byte) 2;
                    follow();
                    break;
                case 2:
                    attack();
                    break;
                case 3:
                    returnToPlayer();
                    this.mAction = (byte) 2;
                    break;
                case 4:
                    this.mAction = (byte) 0;
                    waitForCommand();
                    break;
                case 6:
                    attract();
                    break;
            }
            if (!this.isBeginAttack && this.dyattack > 0) {
                this.dyattack = (short) (this.dyattack - 3);
                if (this.dyattack < 0) {
                    this.dyattack = (short) 0;
                }
            }
            if (Canvas.gameTick % 2 == 0) {
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
        } catch (Exception e) {
        }
    }

    public boolean canFocus() {
        return false;
    }

    public void startAttack(Vector target, int idskill) {
        this.state = (byte) 2;
    }

    public void setidTemplatePet(short id) {
        this.idTemplate = id;
    }

    public void petAttack(byte idSkill, Vector ntarget, int[] dame, short range) {
        this.range = range;
        this.idSkill = idSkill;
        this.ntarget = ntarget;
        this.state = (byte) 2;
        this.dyattack = (short) 0;
    }

    public boolean isMypet() {
        return this.owner.ID == Canvas.gameScr.mainChar.ID;
    }

    public int getIDTem() {
        return this.idTemplate;
    }

    @Override
    public void setPosTo(short x, short y) {
    }

    @Override
    public boolean ispetTool() {
        return true;
    }

    @Override
    public short getOwnerID() {
        return this.ID;
    }
}
