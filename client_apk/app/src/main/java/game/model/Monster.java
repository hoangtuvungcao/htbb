package game.model;

import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.model.skill.SkillAnimate;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Vector;

public class Monster extends LiveActor {
    public static final byte ATTACK = 3;
    public static final byte DEAD = 1;
    public static final byte DEADFLY = 5;
    public static final byte GO_TO_XYFIRST = 7;
    public static final byte INJURE = 4;
    public static final byte MOVE_TO_TARGET = 6;
    public static final byte[][][] REAL_FRAME = {new byte[][]{new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}, new byte[]{8, 9, 10, 11}, new byte[]{12, 13, 14, 15}}, new byte[][]{new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}, new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}}, new byte[][]{new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}}, new byte[][]{new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}, new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}}, new byte[][]{new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}}};
    public static final byte STAND = 0;
    public static final byte TYPE0 = 4;
    public static final byte TYPE1 = 2;
    public static final byte TYPE2 = 1;
    public static final byte TYPE3 = 3;
    public static final byte TYPE4 = 0;
    public static final byte WAIT = 8;
    public static final byte WALK = 2;
    int a;
    public LiveActor attackTarget;
    int b;
    public SkillAnimate currentSkillAnimate;
    int lastTimeStop;
    public short monster_type;
    public short p1;
    public short p2;
    public short p3;
    public LiveActor target;
    long timeBeginDie;
    long timeCreate;
    int timeLimit;
    long timeRevive;
    int timeWait;
    byte vx;
    byte vy;
    public short xTo;
    int xadd;
    public short yTo;
    int yadd;
    public boolean isMineral = false;
    public int idChar = -1;
    public boolean isTarget = false;
    byte[] nh = {1, 4, 0, 2, 3};
    boolean getMonsterTemplate = false;
    short lastDir = -100;
    boolean isTargetPlayer = true;
    public short range = 60;
    public boolean isSetInfo = false;
    public long timeGetInfo = System.currentTimeMillis();
    int rangestop = 12;
    public int targethplost = 0;
    public int eff = 0;
    public boolean isDied = false;

    @Override
    public void dead() {
    }

    @Override
    public boolean isMonster() {
        return true;
    }

    @Override
    public void paintActive(Graphics graphics, int i, int i2) {
    }

    public Monster() {
        this.dir = (byte) r.nextInt(4);
        this.timeLimit = Res.random(10, 20);
        this.state = (byte) 0;
        this.timeWait = 0;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
    }

    @Override
    public String getDisplayName() {
        return (Res.monsterTemplates == null || Res.monsterTemplates[this.monster_type] == null) ? "" : Res.monsterTemplates[this.monster_type].name;
    }

    @Override
    public void setType(short s) {
        this.monster_type = s;
        if (Res.monsterTemplates != null && Res.monsterTemplates[s] == null) {
            Res.monsterTemplates[s] = new MonsterTemplate();
            GameService.gI().sendGetMonsterTEmplate(0, this.monster_type);
        }
        this.timeCreate = System.currentTimeMillis() + 10000;
    }

    public void setRemoveTarget() {
        this.target = null;
        if (this.state != 5) {
            this.state = (byte) 7;
        }
    }

    public void setTarget(LiveActor liveActor) {
        this.target = liveActor;
        this.state = (byte) 6;
    }

    @Override
    public boolean isPaint() {
        return this.x >= GameScr.cmx && this.x <= GameScr.cmx + GCanvas.w && this.y >= GameScr.cmy && this.y <= (GameScr.cmy + GCanvas.h) + 30;
    }

    @Override
    public void paint(Graphics graphics) {
        byte b;
        byte b2;
        if (!isPaint() || this.state == 8 || this.hp <= 0) {
            return;
        }
        paintDataEff_Top(graphics, this.x, this.y, false);
        if (!this.isFreeze) {
            if (this.z != 0) {
                this.frame = (byte) 3;
            }
            if (Res.monsterTemplates[this.monster_type] != null) {
                byte b3 = REAL_FRAME[Res.monsterTemplates[this.monster_type].moveType][this.dir][this.frame];
                if (Res.monsterTemplates[this.monster_type].image != null) {
                    b = Res.monsterTemplates[this.monster_type].xadd;
                    b2 = Res.monsterTemplates[this.monster_type].yadd;
                } else {
                    b = 0;
                    b2 = 0;
                }
                if (this.dynamicEffBottom != null) {
                    for (int i = 0; i < this.dynamicEffBottom.size(); i++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(i)).paint(graphics, this.x, this.y);
                    }
                }
                graphics.drawImage(Res.imgShadow, this.x + b, this.y + b2, 3);
                Res.monsterTemplates[this.monster_type].paint(graphics, this.x + this.vangx, this.y + this.vangy + this.dy + this.z, 0, 0, b3);
            }
            for (int i2 = 0; i2 < this.buff.size(); i2++) {
                ((CharBuff) this.buff.elementAt(i2)).paint(graphics);
            }
            if (this.dynamicEffTop != null) {
                for (int i3 = 0; i3 < this.dynamicEffTop.size(); i3++) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(i3)).paint(graphics, this.x, this.y);
                }
            }
        }
        paintDataEff_Bot(graphics, this.x, this.y, false);
        super.paint(graphics);
    }

    @Override
    public void paintCorner(Graphics graphics, int i, int i2) {
        if (this.z != 0) {
            this.frame = (byte) 3;
        }
        try {
            graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, REAL_FRAME[Res.monsterTemplates[this.monster_type].moveType][0][this.frame] * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, 0, i - Res.monsterTemplates[this.monster_type].xCenter, i2 - 20, 0);
            graphics.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, i - 15, i2 + 3, Graphics.TOP | Graphics.LEFT);
        } catch (Exception unused) {
        }
    }

    @Override
    public void createBuff(int i, int i2) {
        this.buff.addElement(new CharBuff(this.x, this.y, i));
    }

    @Override
    public void updateBuff() {
        for (int i = 0; i < this.buff.size(); i++) {
            CharBuff charBuff = (CharBuff) this.buff.elementAt(i);
            charBuff.update();
            if (charBuff.wantDetroy) {
                this.buff.removeElementAt(i);
            } else {
                charBuff.setXY(this.x, charBuff.isCenter() ? this.y - 12 : this.y);
            }
        }
    }

    @Override
    public void startDeadFly(int i, int i2) {
        this.state = (byte) 5;
        this.p1 = (short) 0;
        this.p2 = (short) i;
        this.p3 = (short) i2;
        if (this.isDied) {
            GCanvas.gameScr.startExplosionAt(this.x, this.y);
            this.wantDestroy = true;
        }
    }

    @Override
    public void update() {
        if (Res.monsterTemplates[this.monster_type].moveType == 4) {
            this.p3 = (short) 0;
            this.p2 = (short) 0;
            this.p1 = (short) 0;
            this.state = (byte) 0;
            return;
        }
        updateDataEffect();
        this.speed = Res.monsterTemplates[this.monster_type].speed;
        if ((this.wantDestroy || this.hp <= 0) && this.timeLive != -1 && this.state != 8) {
            this.state = (byte) 8;
        }
        if (!this.isSetInfo && System.currentTimeMillis() - this.timeGetInfo > 15000) {
            this.timeGetInfo = System.currentTimeMillis();
            GCanvas.gameScr.gameService.requestMonsterInfo(this.ID);
        }
        if (Res.monsterTemplates != null) {
            MonsterTemplate[] monsterTemplateArr = Res.monsterTemplates;
            short s = this.monster_type;
            if (monsterTemplateArr[s] == null) {
                if (!this.getMonsterTemplate && System.currentTimeMillis() > this.timeCreate) {
                    this.getMonsterTemplate = false;
                    GameService.gI().sendGetMonsterTEmplate(0, this.monster_type);
                    this.timeCreate = System.currentTimeMillis() + 10000;
                }
            } else if (!this.getMonsterTemplate) {
                setType(s);
                this.getMonsterTemplate = true;
                this.speed = Res.monsterTemplates[this.monster_type].speed;
                this.height = Res.monsterTemplates[this.monster_type].height;
                if (Res.monsterTemplates[this.monster_type].image == null) {
                    Res.monsterTemplates[this.monster_type].loadImage();
                }
            }
        }
        if (this.beStune && System.currentTimeMillis() > this.timeBeStune) {
            this.beStune = false;
        }
        updateDynamicBuff();
        updateBuff();
        super.update();
        if (this.realHPSyncTime > 0) {
            this.realHPSyncTime--;
            if (this.realHPSyncTime == 0) {
                if (this.realHP < 0) {
                    this.realHP = 0;
                }
                if (this.hp > this.realHP || this.realHP == 0) {
                    this.hp = this.realHP;
                }
                if (this.hp == 0) {
                    this.state = (byte) 4;
                }
            }
        }
        isPoinson();
        byte b = this.state;
        byte b2 = 3;
        if (b == 0) {
            this.frame = (byte) 0;
            int i = this.timeWait + 1;
            this.timeWait = i;
            if (i > this.timeLimit && this.idChar == -1) {
                short s2 = (short) (this.xFirst - this.range);
                short s3 = (short) (this.xFirst + this.range);
                short s4 = (short) (this.yFirst - this.range);
                short s5 = (short) (this.yFirst + this.range);
                byte bNextInt = (byte) r.nextInt(4);
                short s6 = this.lastDir;
                if (s6 != -100 && s6 == bNextInt) {
                    if (bNextInt == 2) {
                        bNextInt = 3;
                    } else if (bNextInt == 3) {
                        bNextInt = 2;
                    } else if (bNextInt == 1) {
                        bNextInt = 0;
                    } else if (bNextInt == 0) {
                        bNextInt = 1;
                    }
                }
                if (bNextInt == 2) {
                    this.vx = (byte) (-this.speed);
                    this.vy = (byte) 0;
                    if (Math.abs(this.x - s2) < 32) {
                        this.vx = (byte) this.speed;
                    } else {
                        b2 = bNextInt;
                    }
                } else if (bNextInt == 3) {
                    this.vx = (byte) this.speed;
                    this.vy = (byte) 0;
                    if (Math.abs(this.x - s3) < 32) {
                        this.vx = (byte) (-this.speed);
                        b2 = 2;
                    } else {
                        b2 = bNextInt;
                    }
                } else if (bNextInt == 1) {
                    this.vx = (byte) 0;
                    this.vy = (byte) (-this.speed);
                    if (Math.abs(this.y - s4) < 32) {
                        this.vy = (byte) this.speed;
                        b2 = 0;
                    } else {
                        b2 = bNextInt;
                    }
                } else if (bNextInt == 0) {
                    this.vx = (byte) 0;
                    this.vy = (byte) this.speed;
                    if (Math.abs(this.y - s5) < 32) {
                        this.vy = (byte) (-this.speed);
                        b2 = 1;
                    } else {
                        b2 = bNextInt;
                    }
                } else {
                    b2 = bNextInt;
                }
                this.dir = b2;
                this.state = (byte) 2;
                this.timeWait = 0;
            }
        } else {
            switch (b) {
                case 2:
                case 6:
                case 7:
                    short s7 = (short) (this.p1 + 1);
                    this.p1 = s7;
                    if (s7 > 6) {
                        this.p1 = (short) 0;
                    }
                    if (this.p1 > 2) {
                        this.frame = (byte) 1;
                    } else {
                        this.frame = (byte) 0;
                    }
                    if (canMove()) {
                        if (this.idChar > -1) {
                            move();
                        } else if (this.state == 7) {
                            setMove(this.xFirst, this.yFirst);
                        } else if (this.state == 2) {
                            move();
                        } else {
                            LiveActor liveActor = this.target;
                            if (liveActor == null) {
                                this.p3 = (short) 0;
                                this.p2 = (short) 0;
                                this.p1 = (short) 0;
                                this.state = (byte) 0;
                                this.timeLimit = Res.random(10, 20);
                                this.vy = (byte) 0;
                                this.vx = (byte) 0;
                                this.lastDir = this.dir;
                            } else if (liveActor != null && Math.abs(this.x - this.target.x) <= this.rangestop && Math.abs(this.y - this.target.y) <= this.rangestop) {
                                this.vy = (byte) 0;
                                this.vx = (byte) 0;
                            } else {
                                setMove(this.target.x + this.xadd, this.target.y + this.yadd);
                            }
                        }
                    }
                    break;
                case 3:
                    this.frame = (byte) 2;
                    short s8 = (short) (this.p1 + 1);
                    this.p1 = s8;
                    if (s8 > 6) {
                        this.p1 = (short) 0;
                        this.state = (byte) 2;
                    } else {
                        SkillAnimate skillAnimate = this.currentSkillAnimate;
                        if (skillAnimate != null) {
                            skillAnimate.updateSkill(this);
                        }
                    }
                    break;
                case 4:
                    this.frame = (byte) 3;
                    short s9 = (short) (this.p1 + 1);
                    this.p1 = s9;
                    if (s9 == 5) {
                        GCanvas.gameScr.startExplosionAt(this.x, this.y);
                    }
                    if (this.p1 > 7) {
                        this.p1 = (short) 0;
                        this.state = (byte) 0;
                    }
                    break;
                case 5:
                    this.frame = (byte) 3;
                    this.p1 = (short) (this.p1 + 1);
                    if (!this.isMineral) {
                        this.x = (short) (this.x + this.p2);
                        this.y = (short) (this.y + this.p3);
                    }
                    this.p2 = (short) (this.p2 >> 1);
                    this.p3 = (short) (this.p3 >> 1);
                    if (this.p1 == 5) {
                        GCanvas.gameScr.startExplosionAt(this.x, this.y);
                        this.timeRevive = System.currentTimeMillis() + ((long) this.timeLive);
                    }
                    this.isDied = true;
                    this.timeBeginDie = (System.currentTimeMillis() / 1000) + 3;
                    if (this.p1 > 7) {
                        this.isDied = false;
                        this.wantDestroy = true;
                    }
                    break;
                case 8:
                    if (GCanvas.gameScr.focusedActor != null && GCanvas.gameScr.focusedActor == this && GCanvas.gameScr.mainChar.state == 0) {
                        GCanvas.gameScr.focusedActor = null;
                    }
                    if (this.timeRevive - System.currentTimeMillis() < 0) {
                        this.wantDestroy = false;
                        this.isDied = false;
                        this.dir = (byte) r.nextInt(4);
                        this.timeLimit = Res.random(10, 20);
                        this.state = (byte) 0;
                        this.timeWait = 0;
                        this.p3 = (short) 0;
                        this.p2 = (short) 0;
                        this.p1 = (short) 0;
                        short s10 = this.xFirst;
                        this.xTo = s10;
                        this.x = s10;
                        short s11 = this.yFirst;
                        this.yTo = s11;
                        this.y = s11;
                        this.hp = this.maxhp;
                        GCanvas.gameScr.startExplosionAt(this.x, this.y);
                        this.timeRevive = System.currentTimeMillis() + ((long) this.timeLive);
                    }
                    break;
            }
        }
        if (this.isDied && this.timeBeginDie - (System.currentTimeMillis() / 1000) <= 0 && !this.wantDestroy) {
            GCanvas.gameScr.startExplosionAt(this.x, this.y);
            this.wantDestroy = true;
            this.isDie = false;
        }
        if (this.hp > 0 || GCanvas.gameScr.focusedActor == null || GCanvas.gameScr.focusedActor != this) {
            return;
        }
        GCanvas.gameScr.focusedActor = null;
    }

    public void setMove(int i, int i2) {
        boolean z;
        boolean z2;
        if (Res.monsterTemplates[this.monster_type].moveType == 4) {
            return;
        }
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
            this.timeLimit = Res.random(10, 20);
            this.vy = (byte) 0;
            this.vx = (byte) 0;
            this.lastDir = this.dir;
            return;
        }
        if (this.x < i) {
            this.x = (short) (this.x + this.speed);
            this.dir = (short) 3;
            return;
        }
        if (this.x > i) {
            this.x = (short) (this.x - this.speed);
            this.dir = (short) 2;
        } else if (this.y > i2) {
            this.y = (short) (this.y - this.speed);
            this.dir = (short) 1;
        } else if (this.y < i2) {
            this.dir = (short) 0;
            this.y = (short) (this.y + this.speed);
        }
    }

    public void move() {
        boolean z;
        boolean z2;
        if (Res.monsterTemplates[this.monster_type].moveType == 4) {
            this.p3 = (short) 0;
            this.p2 = (short) 0;
            this.p1 = (short) 0;
            this.state = (byte) 0;
            return;
        }
        if (this.idChar == -1) {
            short s = (short) ((this.xFirst - this.range) + this.speed);
            short s2 = (short) ((this.xFirst + this.range) - this.speed);
            short s3 = (short) ((this.yFirst - this.range) + this.speed);
            short s4 = (short) ((this.yFirst + this.range) - this.speed);
            int i = this.x - this.speed;
            int i2 = this.x + this.speed;
            int i3 = this.y - this.speed;
            int i4 = this.y + this.speed;
            int iNextInt = r.nextInt(50);
            if ((this.x >= s2 && this.dir == 3) || ((this.x <= s && this.dir == 2) || ((this.y <= s3 && this.dir == 1) || ((this.y >= s4 && this.dir == 0) || ((Tilemap.tileTypeAtPixel(i, this.y, 2) && this.dir == 2) || ((Tilemap.tileTypeAtPixel(i2, this.y, 2) && this.dir == 3) || ((Tilemap.tileTypeAtPixel(this.x, i3, 2) && this.dir == 1) || ((Tilemap.tileTypeAtPixel(this.x, i4, 2) && this.dir == 0) || iNextInt == 5)))))))) {
                this.p3 = (short) 0;
                this.p2 = (short) 0;
                this.p1 = (short) 0;
                this.state = (byte) 0;
                this.timeLimit = Res.random(10, 20);
                this.vy = (byte) 0;
                this.vx = (byte) 0;
                this.lastDir = this.dir;
            }
            this.x = (short) (this.x + this.vx);
            this.y = (short) (this.y + this.vy);
            return;
        }
        int iAbs = Math.abs(this.x - this.xTo);
        int iAbs2 = Math.abs(this.y - this.yTo);
        if (iAbs <= this.speed) {
            this.x = this.xTo;
            z = true;
        } else {
            z = false;
        }
        if (iAbs2 < this.speed) {
            this.y = this.yTo;
            z2 = true;
        } else {
            z2 = false;
        }
        if (z && z2) {
            this.p3 = (short) 0;
            this.p2 = (short) 0;
            this.p1 = (short) 0;
            this.state = (byte) 0;
            return;
        }
        if (this.x < this.xTo) {
            this.x = (short) (this.x + this.speed);
            this.dir = (short) 3;
            return;
        }
        if (this.x > this.xTo) {
            this.x = (short) (this.x - this.speed);
            this.dir = (short) 2;
        } else if (this.y > this.yTo) {
            this.y = (short) (this.y - this.speed);
            this.dir = (short) 1;
        } else if (this.y < this.yTo) {
            this.dir = (short) 0;
            this.y = (short) (this.y + this.speed);
        }
    }

    @Override
    public void setPosTo(short s, short s2) {
        if (this.idChar > -1) {
            if (this.state != 3 && this.x == s && this.y == s2) {
                this.state = (byte) 0;
                return;
            }
            this.xTo = s;
            if (Res.monsterTemplates[this.monster_type].moveType == 4) {
                this.yTo = s2;
            } else {
                this.yTo = (short) ((s2 - 4) + (GCanvas.r.nextInt() % 8));
            }
            if (this.state != 3) {
                this.state = (byte) 2;
            }
            if (this.state != 3 && this.x == this.xTo && this.y == this.yTo) {
                this.state = (byte) 0;
            }
        }
    }

    public void setInfo(MonsterInfo monsterInfo) {
        this.timeLive = monsterInfo.timeLive;
        int[] iArr = {16, 32, 48, -16, -32, -48};
        this.xadd = iArr[r.nextInt(5)];
        this.yadd = iArr[r.nextInt(5)];
        if (this.isMineral) {
            this.x = monsterInfo.x;
            this.y = monsterInfo.y;
        } else {
            this.xTo = monsterInfo.x;
            this.yTo = monsterInfo.y;
            this.a = monsterInfo.x;
            this.b = monsterInfo.y;
        }
        this.rangestop = r.nextInt(10) + 6;
        int i = monsterInfo.hp;
        this.hp = i;
        this.realHP = i;
        this.he = monsterInfo.he;
        this.state = (byte) 0;
        this.p1 = (short) 0;
        this.p2 = (short) 0;
        this.p3 = (short) 0;
        this.monster_type = monsterInfo.monster_type;
        if (Res.monsterTemplates != null && Res.monsterTemplates[this.monster_type] != null) {
            this.speed = Res.monsterTemplates[this.monster_type].speed;
            this.height = Res.monsterTemplates[this.monster_type].height;
            if (Res.monsterTemplates[this.monster_type].image == null) {
                Res.monsterTemplates[this.monster_type].loadImage();
            }
        }
        this.level = (byte) monsterInfo.lv;
        this.maxhp = monsterInfo.maxhp;
        this.isSetInfo = true;
    }

    public void startAttack() {
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
    }

    public void startAttack(LiveActor liveActor, int i, byte b, byte b2) {
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.targethplost = i;
        this.attackTarget = liveActor;
        this.currentSkillAnimate = SkillManager.getSkillAnimate(b, b2);
    }

    public void startAttack(Vector vector, byte b) {
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        if (this.monster_type == 90) {
            SkillAnimate skillBoss_Xmas = SkillManager.setSkillBoss_Xmas();
            this.currentSkillAnimate = skillBoss_Xmas;
            skillBoss_Xmas.setMonster(vector);
        }
    }

    @Override
    public void jump() {
        if (this.isMineral) {
            return;
        }
        this.z = -3;
        this.dz = -5;
    }

    public void setHp(int i) {
        this.hp -= i;
    }

    @Override
    public void setRealHP(int i) {
        this.realHP = i;
        this.hp = this.realHP;
        this.realHPSyncTime = 20;
    }

    @Override
    public boolean isMineral() {
        return this.isMineral;
    }
}
