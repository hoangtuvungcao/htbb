package game.model;

import effect.DataEffect;
import effect.DynamicEffect;
import game.model.skill.Skill_TruRong;
import game.model.skill.Skill_blood_down;
import game.model.skill.ThienThach_Skill;
import game.model.skill.complete.Skill_Dao_Type3;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class MonsterNew extends Monster {
    public Vector data;
    short IdvanTieu;
    private byte skillID;
    Image[] img = new Image[2];
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
    public final byte SKILL_TRU_RONG = 12;
    short idSkill = -1;
    byte bymap = 0;
    public boolean isNewMonster = false;

    public MonsterNew(int idTemplate) {
        this.data = new Vector();
        this.isDied = false;
        this.dir = (byte) LiveActor.r.nextInt(4);
        this.timeLimit = Res.random(10, 20);
        this.state = (byte) 0;
        this.timeWait = 0;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.IdvanTieu = (short) idTemplate;
        if (Res.monsterTemplates[idTemplate] != null) {
            this.data = Res.monsterTemplates[idTemplate].getDataMonster();
        }
    }

    @Override
    public void paint(Graphics g) {
        DataEffect deff;
        if (this.data.size() != 0 && (deff = (DataEffect) this.data.elementAt(this.huongY)) != null) {
            if (this.dynamicEffBottom != null) {
                for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                    ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                }
            }
            deff.paint(g, deff.getFrame(this.frame, this.state), this.x, this.y, this.flip, Res.monsterTemplates[this.monster_type].getImage(this.huongY));
            if (this.dynamicEffTop != null) {
                for (int ii2 = 0; ii2 < this.dynamicEffTop.size(); ii2++) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(ii2)).paint(g, this.x, this.y);
                }
            }
        }
    }

    @Override
    public void paintCorner(Graphics g, int xCorner, int yCorner) {
    }

    public boolean isMonsterVantieu() {
        return this.IdvanTieu >= 95 && this.IdvanTieu <= 112;
    }

    @Override
    public void paintName(Graphics g) {
        super.paintName(g);
    }

    public void set_Target(LiveActor acto) {
        if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].moveType != 4) {
            this.huongY = (byte) (this.y > acto.y ? 1 : 0);
            this.flip = (byte) (this.x - acto.x > 0 ? 0 : 2);
            this.dir = Util.findDirection(this, acto);
            setDir();
        }
    }

    @Override
    public void update() {
        updateDynamicBuff();
        DataEffect deff = null;
        if (this.data.size() > 0) {
            deff = (DataEffect) this.data.elementAt(0);
            this.frame = (byte) ((this.frame + 1) % deff.getAnim(this.state).frame.length);
            if ((this.wantDestroy || this.hp <= 0) && this.timeLive != -1 && this.state != 8) {
                this.state = (byte) 8;
            }
        } else {
            setInfoTemplate();
        }
        if (!this.isSetInfo && System.currentTimeMillis() - this.timeGetInfo > 15000) {
            this.timeGetInfo = System.currentTimeMillis();
            Canvas.gameScr.gameService.requestMonsterInfo(this.ID);
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
        switch (this.state) {
            case 0:
                this.timeWait++;
                if (this.timeWait > this.timeLimit && this.idChar == -1) {
                    short xL = (short) (this.xFirst - this.range);
                    short xR = (short) (this.xFirst + this.range);
                    short yU = (short) (this.yFirst - this.range);
                    short yD = (short) (this.yFirst + this.range);
                    byte mDir = (byte) LiveActor.r.nextInt(4);
                    if (this.lastDir != -100 && this.lastDir == mDir) {
                        if (mDir == 2) {
                            mDir = 3;
                        } else if (mDir == 3) {
                            mDir = 2;
                        } else if (mDir == 1) {
                            mDir = 0;
                        } else if (mDir == 0) {
                            mDir = 1;
                        }
                    }
                    if (mDir == 2) {
                        this.vx = (byte) (-this.speed);
                        this.vy = (byte) 0;
                        if (Math.abs(this.x - xL) < 32) {
                            mDir = 3;
                            this.vx = (byte) this.speed;
                        }
                    } else if (mDir == 3) {
                        this.vx = (byte) this.speed;
                        this.vy = (byte) 0;
                        if (Math.abs(this.x - xR) < 32) {
                            mDir = 2;
                            this.vx = (byte) (-this.speed);
                        }
                    } else if (mDir == 1) {
                        this.vx = (byte) 0;
                        this.vy = (byte) (-this.speed);
                        if (Math.abs(this.y - yU) < 32) {
                            mDir = 0;
                            this.vy = (byte) this.speed;
                        }
                    } else if (mDir == 0) {
                        this.vx = (byte) 0;
                        this.vy = (byte) this.speed;
                        if (Math.abs(this.y - yD) < 32) {
                            mDir = 1;
                            this.vy = (byte) (-this.speed);
                        }
                    }
                    this.dir = mDir;
                    this.state = (byte) 2;
                    this.timeWait = 0;
                }
                break;
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
                if (deff != null) {
                    if (!this.isNewMonster) {
                        if (this.monster_type == 117) {
                            if (this.listAttack.size() > 0) {
                                LiveActor acto = (LiveActor) this.listAttack.elementAt(0);
                                set_Target(acto);
                                if (this.frame == deff.getAnim(this.state).frame.length - 7) {
                                    if (this.skillID == 1) {
                                        Canvas.gameScr.startNewArrow(7, this, acto, this.x, this.y, acto.damAttack, (byte) 0, 18);
                                    } else {
                                        Canvas.gameScr.startSkill_BossHalloween(this.x, this.y, 0, this.listAttack);
                                    }
                                    this.listAttack.removeElementAt(0);
                                }
                            }
                            if (this.listAttack.size() == 0 && this.frame == deff.getAnim(this.state).frame.length - 1) {
                                this.state = (byte) 0;
                            }
                        } else if (this.monster_type == 116) {
                            if (this.listAttack.size() > 0) {
                                LiveActor acto2 = (LiveActor) this.listAttack.elementAt(0);
                                set_Target(acto2);
                                if (this.frame == deff.getAnim(this.state).frame.length - 7) {
                                    acto2.jump();
                                    if (this.skillID == 1) {
                                        Canvas.gameScr.startNewMagicBeam(9, this, acto2, this.x, this.y, acto2.damAttack, (byte) 0);
                                    } else if (this.skillID == 0) {
                                        for (int i = 0; i < 8; i++) {
                                            Canvas.gameScr.startNewMagicBeam(10, this, acto2, this.x + this.splashDuaX[this.dir] + this.x2[i], this.y + this.splashDuaY[this.dir] + this.y2[i], acto2.damAttack, (byte) 0, Skill_Dao_Type3.goc[this.dir][i]);
                                        }
                                    } else {
                                        Skill_blood_down skill = new Skill_blood_down(acto2.x, acto2.y, acto2.damAttack);
                                        EffectManager.hiEffects.addElement(skill);
                                    }
                                    this.listAttack.removeElementAt(0);
                                }
                            }
                            if (this.listAttack.size() == 0 && this.frame == deff.getAnim(this.state).frame.length - 1) {
                                this.state = (byte) 0;
                            }
                        } else if (!isMonsterVantieu() && (this.skillID == 0 || this.skillID == 2)) {
                            set_Target((LiveActor) this.listAttack.elementAt(0));
                            if (this.frame == deff.getAnim(this.state).frame.length - 7 && this.dir != 2) {
                                if (this.dir == 3) {
                                    Canvas.gameScr.startSkill_Noel(this.x + 50, this.y, GameScr.cmy, this.listAttack);
                                } else if (this.dir == 1) {
                                    Canvas.gameScr.startSkill_Noel(this.x, this.y - 50, GameScr.cmy, this.listAttack);
                                } else if (this.dir == 0) {
                                    Canvas.gameScr.startSkill_Noel(this.x, this.y + 50, GameScr.cmy, this.listAttack);
                                }
                            }
                            if (this.frame == deff.getAnim(this.state).frame.length - 1) {
                                this.state = (byte) 0;
                            }
                        } else if (this.skillID == 1) {
                            if (this.listAttack.size() > 0) {
                                LiveActor acto3 = (LiveActor) this.listAttack.elementAt(0);
                                set_Target(acto3);
                                if (this.frame == deff.getAnim(this.state).frame.length - 7) {
                                    acto3.jump();
                                    if (this.monster_type == 115) {
                                        Canvas.gameScr.startNewArrow(14, this, acto3, this.x, this.y - 15, acto3.damAttack, (byte) 0, 14);
                                    } else {
                                        Canvas.gameScr.startNewArrow(13, this, acto3, this.x, this.y - 15, acto3.damAttack, (byte) 0, 13);
                                    }
                                }
                            }
                            if (this.listAttack.size() == 0 && this.frame == deff.getAnim(this.state).frame.length - 1) {
                                this.state = (byte) 0;
                            }
                        }
                        startEffectSkill(this.skillID, deff);
                    } else if (this.listAttack.size() > 0) {
                        if (this.frame == deff.getAnim(this.state).frame.length - 7) {
                            for (int i2 = 0; i2 < this.listAttack.size(); i2++) {
                                LiveActor acto4 = (LiveActor) this.listAttack.elementAt(i2);
                                GameScr.startSkillEffect(acto4, this.idSkill, 5000L, 1, acto4.damAttack, this.bymap);
                            }
                            this.listAttack.removeAllElements();
                        }
                        if (this.listAttack.size() == 0 && this.frame == deff.getAnim(this.state).frame.length - 1) {
                            this.state = (byte) 0;
                        }
                    }
                }
                break;
            case 4:
                if (deff != null && this.frame == deff.getAnim(this.state).frame.length - 1) {
                    this.state = (byte) 0;
                }
                break;
            case 5:
                this.wantDestroy = true;
                this.frame = (byte) 0;
                Canvas.gameScr.actors.removeElement(this);
                break;
            case 8:
                if (Canvas.gameScr.focusedActor != null && Canvas.gameScr.focusedActor == this && Canvas.gameScr.mainChar.state == 0) {
                    Canvas.gameScr.focusedActor = null;
                }
                if (this.timeRevive - System.currentTimeMillis() < 0) {
                    this.wantDestroy = false;
                    this.isDied = false;
                    this.dir = (byte) LiveActor.r.nextInt(4);
                    this.timeLimit = Res.random(10, 20);
                    this.state = (byte) 0;
                    this.timeWait = 0;
                    this.p3 = (short) 0;
                    this.p2 = (short) 0;
                    this.p1 = (short) 0;
                    short s = this.xFirst;
                    this.xTo = s;
                    this.x = s;
                    short s2 = this.yFirst;
                    this.yTo = s2;
                    this.y = s2;
                    this.hp = this.maxhp;
                    Canvas.gameScr.startExplosionAt(this.x, this.y);
                    this.timeRevive = System.currentTimeMillis() + ((long) this.timeLive);
                }
                break;
        }
        if (this.isDied && this.timeBeginDie - (System.currentTimeMillis() / 1000) <= 0 && !this.wantDestroy) {
            Canvas.gameScr.startExplosionAt(this.x, this.y);
            this.wantDestroy = true;
            this.isDie = false;
        }
        if (this.hp <= 0 && Canvas.gameScr.focusedActor != null && Canvas.gameScr.focusedActor == this) {
            Canvas.gameScr.focusedActor = null;
        }
    }

    private void setInfoTemplate() {
        if (Res.monsterTemplates[this.monster_type] != null) {
            this.data = Res.monsterTemplates[this.monster_type].getDataMonster();
        }
    }

    @Override
    public void startAttack(Vector list, byte skillID) {
        if (this.monster_type != 116 && skillID == 2) {
            if (this.monster_type == 113) {
                skillID = 1;
            } else {
                skillID = 0;
            }
        }
        this.listAttack = list;
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.frame = (byte) 0;
        this.skillID = skillID;
    }

    @Override
    public void setPosTo(short xTo, short yTo) {
        if (this.idChar > -1) {
            if (this.state != 3 && this.x == xTo && this.y == yTo) {
                this.state = (byte) 0;
                return;
            }
            this.xTo = xTo;
            if (Res.monsterTemplates[this.monster_type].moveType == 4) {
                this.yTo = yTo;
            } else {
                this.yTo = yTo;
                this.huongY = (byte) (this.y > yTo ? 1 : 0);
                this.flip = (byte) (this.x - xTo > 0 ? 0 : 2);
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

    private void startEffectSkill(int id, DataEffect deff) {
        if (this.listAttack.size() > 0 && id > 2) {
            LiveActor acto = (LiveActor) this.listAttack.elementAt(0);
            set_Target(acto);
            switch (id) {
                case 3:
                    if (this.frame == deff.getAnim(this.state).frame.length - 7 && this.dir != 2) {
                        if (this.dir == 3) {
                            Canvas.gameScr.startSkill_Noel(this.x + 50, this.y, GameScr.cmy, this.listAttack);
                        } else if (this.dir == 1) {
                            Canvas.gameScr.startSkill_Noel(this.x, this.y - 50, GameScr.cmy, this.listAttack);
                        } else if (this.dir == 0) {
                            Canvas.gameScr.startSkill_Noel(this.x, this.y + 50, GameScr.cmy, this.listAttack);
                        }
                    }
                    if (this.frame == deff.getAnim(this.state).frame.length - 1) {
                        this.state = (byte) 0;
                    }
                    break;
                case 4:
                    Canvas.gameScr.startNewMagicBeam(9, this, acto, this.x, this.y, acto.damAttack, (byte) 0);
                    break;
                case 5:
                    Skill_blood_down skill = new Skill_blood_down(acto.x, acto.y, acto.damAttack);
                    EffectManager.hiEffects.addElement(skill);
                    break;
                case 6:
                    for (int i = 0; i < 8; i++) {
                        Canvas.gameScr.startNewMagicBeam(10, this, acto, this.x + this.splashDuaX[this.dir] + this.x2[i], this.y + this.splashDuaY[this.dir] + this.y2[i], acto.damAttack, (byte) 0, Skill_Dao_Type3.goc[this.dir][i]);
                    }
                    break;
                case 7:
                    Canvas.gameScr.startNewArrow(7, this, acto, this.x, this.y, acto.damAttack, (byte) 0, 18);
                    break;
                case 8:
                    Canvas.gameScr.startSkill_BossHalloween(this.x, this.y, 0, this.listAttack);
                    break;
                case 9:
                    for (int i2 = 0; i2 < this.listAttack.size(); i2++) {
                        LiveActor live = (LiveActor) this.listAttack.elementAt(i2);
                        ThienThach_Skill rain = new ThienThach_Skill(live.x, live.y, live.damAttack);
                        EffectManager.hiEffects.addElement(rain);
                    }
                    break;
                case 10:
                    for (int i3 = 0; i3 < this.listAttack.size(); i3++) {
                        LiveActor acto1 = (LiveActor) this.listAttack.elementAt(i3);
                        Canvas.gameScr.startNewArrow(7, this, acto1, this.x - 20, this.y - 50, acto1.damAttack, (byte) 2, 8);
                        Canvas.gameScr.startNewArrow(7, this, acto1, this.x, this.y - 30, acto1.damAttack, (byte) 2, 8);
                        Canvas.gameScr.startNewArrow(7, this, acto1, this.x - 20, this.y - 10, acto1.damAttack, (byte) 2, 8);
                        acto.doublejump();
                    }
                    break;
                case 11:
                    for (int i4 = 0; i4 < this.listAttack.size(); i4++) {
                        LiveActor actoo = (LiveActor) this.listAttack.elementAt(i4);
                        Canvas.gameScr.startWeapsMango(this, actoo, 30);
                    }
                    break;
                case 12:
                    for (int i5 = 0; i5 < this.listAttack.size(); i5++) {
                        LiveActor act = (LiveActor) this.listAttack.elementAt(i5);
                        Skill_TruRong ST = new Skill_TruRong(this.x, this.y, act.x, act.y, act, i5 * 4);
                        EffectManager.addHiEffect(ST);
                    }
                    break;
                default:
                    if (this.frame == deff.getAnim(this.state).frame.length - 7 && this.dir != 2) {
                        if (this.dir == 3) {
                            Canvas.gameScr.startSkill_Noel(this.x + 50, this.y, GameScr.cmy, this.listAttack);
                        } else if (this.dir == 1) {
                            Canvas.gameScr.startSkill_Noel(this.x, this.y - 50, GameScr.cmy, this.listAttack);
                        } else if (this.dir == 0) {
                            Canvas.gameScr.startSkill_Noel(this.x, this.y + 50, GameScr.cmy, this.listAttack);
                        }
                    }
                    if (this.frame == deff.getAnim(this.state).frame.length - 1) {
                        this.state = (byte) 0;
                    }
                    break;
            }
            if (this.listAttack.size() == 0 && this.frame == deff.getAnim(this.state).frame.length - 1) {
                this.state = (byte) 0;
            }
        }
    }

    @Override
    public void setIdSkill(int idSkill) {
        this.idSkill = (short) idSkill;
    }

    @Override
    public void setByMap(int type) {
        this.bymap = (byte) type;
    }

    @Override
    public void setNewMonster(int isNewMonster) {
        this.isNewMonster = isNewMonster == 1;
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
