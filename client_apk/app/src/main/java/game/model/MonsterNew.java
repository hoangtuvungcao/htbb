package game.model;

import effect.DataEffect;
import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.model.skill.Skill_blood_down;
import game.model.skill.ThienThach_Skill;
import game.model.skill.complete.Skill_Dao_Type3;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import java.util.Vector;

public class MonsterNew extends Monster {
    int IdvanTieu;
    public Vector data;
    private byte skillID;
    Image[] img = {null, null};
    byte way = 0;
    Vector listAttack = new Vector();
    byte huongY = 0;
    byte flip = 0;
    byte[] splashDuaX = {-2, 2, -8, 8};
    byte[] splashDuaY = {-10, -30, -10, -10};
    byte[] x2 = {0, 20, 20, 0, 0, 20, 20, 20, 20};
    byte[] y2 = {0, 0, 0, 20, 20, 20, 20, 20, 20};
    public final byte SKILL_BOSS_NOEL = 3;
    public final byte SKILL_LOC_XOAY = 4;
    public final byte SKILL_BLOOD_DOW = 5;
    public final byte SKILL_BAT = 6;
    public final byte SKILL_BABURAN = 7;
    public final byte SKILL_BI_NGO = 8;
    public final byte SKILL_THIEN_THACH = 9;
    public final byte SKILL_LUOI_KIEM = 10;
    public final byte SKILL_WEAPON_MAN_GO = 11;
    short idSkill = -1;
    byte bymap = 0;
    public boolean isNewMonster = false;

    @Override
    public void paintCorner(Graphics graphics, int i, int i2) {
    }

    public MonsterNew(int i) {
        this.data = new Vector();
        this.isDied = false;
        this.dir = (byte) r.nextInt(4);
        this.timeLimit = Res.random(10, 20);
        this.state = (byte) 0;
        this.timeWait = 0;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.IdvanTieu = (short) i;
        if (Res.monsterTemplates[i] != null) {
            this.data = Res.monsterTemplates[i].getDataMonster();
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.data.size() == 0) {
            Cout.println("null roi nen khong ve");
            return;
        }
        DataEffect dataEffect = (DataEffect) this.data.elementAt(this.huongY);
        if (dataEffect != null) {
            if (this.dynamicEffBottom != null) {
                for (int i = 0; i < this.dynamicEffBottom.size(); i++) {
                    ((DynamicEffect) this.dynamicEffBottom.elementAt(i)).paint(graphics, this.x, this.y);
                }
            }
            dataEffect.paint(graphics, dataEffect.getFrame(this.frame, this.state), this.x, this.y, this.flip, Res.monsterTemplates[this.monster_type].getImage(this.huongY));
            if (this.dynamicEffTop != null) {
                for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(i2)).paint(graphics, this.x, this.y);
                }
            }
        }
    }

    public boolean isMonsterVantieu() {
        int i = this.IdvanTieu;
        return i >= 95 && i <= 112;
    }

    @Override
    public void paintName(Graphics graphics) {
        super.paintName(graphics);
    }

    public void set_Target(LiveActor liveActor) {
        if (Res.monsterTemplates[this.monster_type] == null || Res.monsterTemplates[this.monster_type].moveType == 4) {
            return;
        }
        this.huongY = (byte) (this.y > liveActor.y ? 1 : 0);
        this.flip = (byte) (this.x - liveActor.x <= 0 ? 2 : 0);
        this.dir = Util.findDirection(this, liveActor);
        setDir();
    }

    @Override
    public void update() {
        DataEffect dataEffect;
        byte b;
        updateDynamicBuff();
        if (this.data.size() > 0) {
            DataEffect dataEffect2 = (DataEffect) this.data.elementAt(0);
            this.frame = (byte) ((this.frame + 1) % dataEffect2.getAnim(this.state).frame.length);
            if ((this.wantDestroy || this.hp <= 0) && this.timeLive != -1 && this.state != 8) {
                this.state = (byte) 8;
            }
            dataEffect = dataEffect2;
        } else {
            setInfoTemplate();
            dataEffect = null;
        }
        if (!this.isSetInfo && System.currentTimeMillis() - this.timeGetInfo > 15000) {
            this.timeGetInfo = System.currentTimeMillis();
            GCanvas.gameScr.gameService.requestMonsterInfo(this.ID);
        }
        if (Res.monsterTemplates != null) {
            if (Res.monsterTemplates[this.monster_type] == null) {
                if (!this.getMonsterTemplate && System.currentTimeMillis() > this.timeCreate) {
                    this.getMonsterTemplate = false;
                    GameService.gI().sendGetMonsterTEmplate(0, this.monster_type);
                    this.timeCreate = System.currentTimeMillis() + 10000;
                }
            } else if (!this.getMonsterTemplate) {
                setType(this.monster_type);
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
        updateBuff();
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
        byte b2 = this.state;
        byte b3 = 3;
        if (b2 == 0) {
            this.timeWait++;
            if (this.timeWait > this.timeLimit && this.idChar == -1) {
                short s = (short) (this.xFirst - this.range);
                short s2 = (short) (this.xFirst + this.range);
                short s3 = (short) (this.yFirst - this.range);
                short s4 = (short) (this.yFirst + this.range);
                byte bNextInt = (byte) r.nextInt(4);
                if (this.lastDir != -100 && this.lastDir == bNextInt) {
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
                    if (Math.abs(this.x - s) < 32) {
                        this.vx = (byte) this.speed;
                    } else {
                        b3 = bNextInt;
                    }
                } else if (bNextInt == 3) {
                    this.vx = (byte) this.speed;
                    this.vy = (byte) 0;
                    if (Math.abs(this.x - s2) < 32) {
                        this.vx = (byte) (-this.speed);
                        b3 = 2;
                    } else {
                        b3 = bNextInt;
                    }
                } else if (bNextInt == 1) {
                    this.vx = (byte) 0;
                    this.vy = (byte) (-this.speed);
                    if (Math.abs(this.y - s3) < 32) {
                        this.vy = (byte) this.speed;
                        b3 = 0;
                    } else {
                        b3 = bNextInt;
                    }
                } else if (bNextInt == 0) {
                    this.vx = (byte) 0;
                    this.vy = (byte) this.speed;
                    if (Math.abs(this.y - s4) < 32) {
                        this.vy = (byte) (-this.speed);
                        b3 = 1;
                    } else {
                        b3 = bNextInt;
                    }
                } else {
                    b3 = bNextInt;
                }
                this.dir = b3;
                this.state = (byte) 2;
                this.timeWait = 0;
            }
        } else {
            switch (b2) {
                case 2:
                case 6:
                case 7:
                    if (!this.isFreeze) {
                        if (this.idChar > -1) {
                            move();
                        } else if (this.state == 7) {
                            setMove(this.xFirst, this.yFirst);
                        } else if (this.state == 2) {
                            move();
                        } else if (this.target == null) {
                            this.p3 = (short) 0;
                            this.p2 = (short) 0;
                            this.p1 = (short) 0;
                            this.state = (byte) 0;
                            this.timeLimit = Res.random(10, 20);
                            this.vy = (byte) 0;
                            this.vx = (byte) 0;
                            this.lastDir = this.dir;
                        } else if (this.target != null && Math.abs(this.x - this.target.x) <= this.rangestop && Math.abs(this.y - this.target.y) <= this.rangestop) {
                            this.vy = (byte) 0;
                            this.vx = (byte) 0;
                        } else {
                            setMove(this.target.x + this.xadd, this.target.y + this.yadd);
                        }
                    }
                    break;
                case 3:
                    if (this.listAttack.size() == 0) {
                        this.state = (byte) 0;
                        return;
                    }
                    if (dataEffect != null) {
                        if (!this.isNewMonster) {
                            if (this.monster_type == 117) {
                                if (this.listAttack.size() > 0) {
                                    LiveActor liveActor = (LiveActor) this.listAttack.elementAt(0);
                                    set_Target(liveActor);
                                    if (this.frame == dataEffect.getAnim(this.state).frame.length - 7) {
                                        if (this.skillID == 1) {
                                            GCanvas.gameScr.startNewArrow(7, this, liveActor, this.x, this.y, liveActor.damAttack, (byte) 0, 18);
                                        } else {
                                            GCanvas.gameScr.startSkill_BossHalloween(this.x, this.y, 0, this.listAttack);
                                        }
                                        this.listAttack.removeElementAt(0);
                                    }
                                }
                                if (this.listAttack.size() == 0 && this.frame == dataEffect.getAnim(this.state).frame.length - 1) {
                                    this.state = (byte) 0;
                                }
                            } else if (this.monster_type == 116) {
                                if (this.listAttack.size() > 0) {
                                    LiveActor liveActor2 = (LiveActor) this.listAttack.elementAt(0);
                                    set_Target(liveActor2);
                                    if (this.frame == dataEffect.getAnim(this.state).frame.length - 7) {
                                        liveActor2.jump();
                                        byte b4 = this.skillID;
                                        if (b4 == 1) {
                                            GCanvas.gameScr.startNewMagicBeam(9, this, liveActor2, this.x, this.y, liveActor2.damAttack, (byte) 0);
                                        } else if (b4 == 0) {
                                            int i = 0;
                                            for (int i2 = 8; i < i2; i2 = 8) {
                                                GCanvas.gameScr.startNewMagicBeam(10, this, liveActor2, this.x + this.splashDuaX[this.dir] + this.x2[i], this.y + this.splashDuaY[this.dir] + this.y2[i], liveActor2.damAttack, (byte) 0, Skill_Dao_Type3.goc[this.dir][i]);
                                                i++;
                                                liveActor2 = liveActor2;
                                            }
                                        } else {
                                            EffectManager.hiEffects.addElement(new Skill_blood_down(liveActor2.x, liveActor2.y, liveActor2.damAttack));
                                        }
                                        this.listAttack.removeElementAt(0);
                                    }
                                }
                                if (this.listAttack.size() == 0 && this.frame == dataEffect.getAnim(this.state).frame.length - 1) {
                                    this.state = (byte) 0;
                                }
                            } else if (!isMonsterVantieu() && ((b = this.skillID) == 0 || b == 2)) {
                                set_Target((LiveActor) this.listAttack.elementAt(0));
                                if (this.frame == dataEffect.getAnim(this.state).frame.length - 7 && this.dir != 2) {
                                    if (this.dir == 3) {
                                        GameScr gameScr = GCanvas.gameScr;
                                        int i3 = this.x + 50;
                                        short s5 = this.y;
                                        GameScr gameScr2 = GCanvas.gameScr;
                                        gameScr.startSkill_Noel(i3, s5, GameScr.cmy, this.listAttack);
                                    } else if (this.dir == 1) {
                                        GameScr gameScr3 = GCanvas.gameScr;
                                        short s6 = this.x;
                                        int i4 = this.y - 50;
                                        GameScr gameScr4 = GCanvas.gameScr;
                                        gameScr3.startSkill_Noel(s6, i4, GameScr.cmy, this.listAttack);
                                    } else if (this.dir == 0) {
                                        GameScr gameScr5 = GCanvas.gameScr;
                                        short s7 = this.x;
                                        int i5 = this.y + 50;
                                        GameScr gameScr6 = GCanvas.gameScr;
                                        gameScr5.startSkill_Noel(s7, i5, GameScr.cmy, this.listAttack);
                                    }
                                }
                                if (this.frame == dataEffect.getAnim(this.state).frame.length - 1) {
                                    this.state = (byte) 0;
                                }
                            } else if (this.skillID == 1) {
                                if (this.listAttack.size() > 0) {
                                    LiveActor liveActor3 = (LiveActor) this.listAttack.elementAt(0);
                                    set_Target(liveActor3);
                                    if (this.frame == dataEffect.getAnim(this.state).frame.length - 7) {
                                        liveActor3.jump();
                                        if (this.monster_type == 115) {
                                            GCanvas.gameScr.startNewArrow(14, this, liveActor3, this.x, this.y - 15, liveActor3.damAttack, (byte) 0, 14);
                                        } else {
                                            GCanvas.gameScr.startNewArrow(13, this, liveActor3, this.x, this.y - 15, liveActor3.damAttack, (byte) 0, 13);
                                        }
                                    }
                                }
                                if (this.listAttack.size() == 0 && this.frame == dataEffect.getAnim(this.state).frame.length - 1) {
                                    this.state = (byte) 0;
                                }
                            }
                            startEffectSkill(this.skillID, dataEffect);
                        } else if (this.listAttack.size() > 0) {
                            if (this.frame == dataEffect.getAnim(this.state).frame.length - 7) {
                                for (int i6 = 0; i6 < this.listAttack.size(); i6++) {
                                    LiveActor liveActor4 = (LiveActor) this.listAttack.elementAt(i6);
                                    GameScr.startSkillEffect(liveActor4, this.idSkill, 5000L, 1, liveActor4.damAttack, this.bymap);
                                }
                                this.listAttack.removeAllElements();
                            }
                            if (this.listAttack.size() == 0 && this.frame == dataEffect.getAnim(this.state).frame.length - 1) {
                                this.state = (byte) 0;
                            }
                        }
                    }
                    break;
                case 4:
                    if (dataEffect != null && this.frame == dataEffect.getAnim(this.state).frame.length - 1) {
                        this.state = (byte) 0;
                    }
                    break;
                case 5:
                    this.wantDestroy = true;
                    this.frame = (byte) 0;
                    GCanvas.gameScr.actors.removeElement(this);
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
                        short s8 = this.xFirst;
                        this.xTo = s8;
                        this.x = s8;
                        short s9 = this.yFirst;
                        this.yTo = s9;
                        this.y = s9;
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

    private void setInfoTemplate() {
        if (Res.monsterTemplates[this.monster_type] != null) {
            this.data = Res.monsterTemplates[this.monster_type].getDataMonster();
        }
    }

    @Override
    public void startAttack(Vector vector, byte b) {
        if (this.monster_type != 116 && b == 2) {
            b = this.monster_type == 113 ? (byte) 1 : (byte) 0;
        }
        this.listAttack = vector;
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.frame = (byte) 0;
        this.skillID = b;
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
                this.yTo = s2;
                this.huongY = (byte) (this.y > s2 ? 1 : 0);
                this.flip = (byte) (this.x - s > 0 ? 0 : 2);
                setDir();
            }
            if (this.state != 3) {
                this.state = (byte) 2;
            }
            if (this.state != 3 && this.x == this.xTo && this.y == this.yTo) {
                this.state = (byte) 0;
            }
        }
    }

    private void startEffectSkill(int i, DataEffect dataEffect) {
        if (this.listAttack.size() <= 0 || i <= 2) {
            return;
        }
        LiveActor liveActor = (LiveActor) this.listAttack.elementAt(0);
        set_Target(liveActor);
        switch (i) {
            case 3:
                if (this.frame == dataEffect.getAnim(this.state).frame.length - 7 && this.dir != 2) {
                    if (this.dir == 3) {
                        GameScr gameScr = GCanvas.gameScr;
                        int i2 = this.x + 50;
                        short s = this.y;
                        GameScr gameScr2 = GCanvas.gameScr;
                        gameScr.startSkill_Noel(i2, s, GameScr.cmy, this.listAttack);
                    } else if (this.dir == 1) {
                        GameScr gameScr3 = GCanvas.gameScr;
                        short s2 = this.x;
                        int i3 = this.y - 50;
                        GameScr gameScr4 = GCanvas.gameScr;
                        gameScr3.startSkill_Noel(s2, i3, GameScr.cmy, this.listAttack);
                    } else if (this.dir == 0) {
                        GameScr gameScr5 = GCanvas.gameScr;
                        short s3 = this.x;
                        int i4 = this.y + 50;
                        GameScr gameScr6 = GCanvas.gameScr;
                        gameScr5.startSkill_Noel(s3, i4, GameScr.cmy, this.listAttack);
                    }
                }
                if (this.frame == dataEffect.getAnim(this.state).frame.length - 1) {
                    this.state = (byte) 0;
                }
                break;
            case 4:
                GCanvas.gameScr.startNewMagicBeam(9, this, liveActor, this.x, this.y, liveActor.damAttack, (byte) 0);
                break;
            case 5:
                EffectManager.hiEffects.addElement(new Skill_blood_down(liveActor.x, liveActor.y, liveActor.damAttack));
                break;
            case 6:
                for (int i5 = 0; i5 < 8; i5++) {
                    GCanvas.gameScr.startNewMagicBeam(10, this, liveActor, this.x + this.splashDuaX[this.dir] + this.x2[i5], this.y + this.splashDuaY[this.dir] + this.y2[i5], liveActor.damAttack, (byte) 0, Skill_Dao_Type3.goc[this.dir][i5]);
                }
                break;
            case 7:
                GCanvas.gameScr.startNewArrow(7, this, liveActor, this.x, this.y, liveActor.damAttack, (byte) 0, 18);
                break;
            case 8:
                GCanvas.gameScr.startSkill_BossHalloween(this.x, this.y, 0, this.listAttack);
                break;
            case 9:
                for (int i6 = 0; i6 < this.listAttack.size(); i6++) {
                    LiveActor liveActor2 = (LiveActor) this.listAttack.elementAt(i6);
                    EffectManager.hiEffects.addElement(new ThienThach_Skill(liveActor2.x, liveActor2.y, liveActor2.damAttack));
                }
                break;
            case 10:
                for (int i7 = 0; i7 < this.listAttack.size(); i7++) {
                    LiveActor liveActor3 = (LiveActor) this.listAttack.elementAt(i7);
                    GCanvas.gameScr.startNewArrow(7, this, liveActor3, this.x - 20, this.y - 50, liveActor3.damAttack, (byte) 2, 8);
                    GCanvas.gameScr.startNewArrow(7, this, liveActor3, this.x, this.y - 30, liveActor3.damAttack, (byte) 2, 8);
                    GCanvas.gameScr.startNewArrow(7, this, liveActor3, this.x - 20, this.y - 10, liveActor3.damAttack, (byte) 2, 8);
                    liveActor.doublejump();
                }
                break;
            case 11:
                for (int i8 = 0; i8 < this.listAttack.size(); i8++) {
                    GCanvas.gameScr.startWeapsMango(this, (LiveActor) this.listAttack.elementAt(i8), 30);
                }
                break;
            default:
                if (this.frame == dataEffect.getAnim(this.state).frame.length - 7 && this.dir != 2) {
                    if (this.dir == 3) {
                        GameScr gameScr7 = GCanvas.gameScr;
                        int i9 = this.x + 50;
                        short s4 = this.y;
                        GameScr gameScr8 = GCanvas.gameScr;
                        gameScr7.startSkill_Noel(i9, s4, GameScr.cmy, this.listAttack);
                    } else if (this.dir == 1) {
                        GameScr gameScr9 = GCanvas.gameScr;
                        short s5 = this.x;
                        int i10 = this.y - 50;
                        GameScr gameScr10 = GCanvas.gameScr;
                        gameScr9.startSkill_Noel(s5, i10, GameScr.cmy, this.listAttack);
                    } else if (this.dir == 0) {
                        GameScr gameScr11 = GCanvas.gameScr;
                        short s6 = this.x;
                        int i11 = this.y + 50;
                        GameScr gameScr12 = GCanvas.gameScr;
                        gameScr11.startSkill_Noel(s6, i11, GameScr.cmy, this.listAttack);
                    }
                }
                if (this.frame == dataEffect.getAnim(this.state).frame.length - 1) {
                    this.state = (byte) 0;
                }
                break;
        }
        if (this.listAttack.size() == 0 && this.frame == dataEffect.getAnim(this.state).frame.length - 1) {
            this.state = (byte) 0;
        }
    }

    @Override
    public void setIdSkill(int i) {
        this.idSkill = (short) i;
    }

    @Override
    public void setByMap(int i) {
        this.bymap = (byte) i;
    }

    @Override
    public void setNewMonster(int i) {
        this.isNewMonster = i == 1;
    }

    public void setDir() {
        if (this.data.size() > 2) {
            this.flip = (byte) 0;
            if (this.dir == 3) {
                this.flip = (byte) 2;
                this.huongY = (byte) 0;
            } else if (this.dir == 2) {
                this.huongY = (byte) 0;
            } else if (this.dir == 0) {
                this.huongY = (byte) 1;
            } else if (this.dir == 1) {
                this.huongY = (byte) 2;
            }
        }
    }
}
