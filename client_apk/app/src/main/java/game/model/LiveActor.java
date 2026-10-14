package game.model;

import effect.DataSkillEff;
import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;

public abstract class LiveActor extends Actor {
    public static final byte HE_HOA = 2;
    public static final byte HE_KIM = 4;
    public static final byte HE_MOC = 1;
    public static final byte HE_THO = 3;
    public static final byte HE_THUY = 0;
    public static Random r = new Random(System.currentTimeMillis());
    public short accurate;
    public int attack;
    public short critical;
    public int damAttack;
    public int defend;
    public int defend_magic;
    public short dir;
    public short dodge;
    public int dvangx;
    public int dvangy;
    public short dy;
    public int dz;
    public byte frame;
    public byte he;
    public int hp;
    public byte level;
    public int maxhp;
    public int realHP;
    public int realHPSyncTime;
    public short speed;
    public int vangx;
    public int vangy;
    public int z;
    public byte idBoss = -1;
    public Vector<Effect> effSkill = new Vector<>();
    public boolean isFreeze = false;
    public Vector dynamicEffTop = new Vector();
    public Vector dynamicEffBottom = new Vector();
    public Vector Vec_Effect_Skill = new Vector();
    public short totalTime = 36;

    public byte getHorse() {
        return (byte) 0;
    }

    public int getStartPointPaintFly() {
        return 0;
    }

    public void paintBottomDataEff(Graphics graphics) {
    }

    public void paintTopDataEff(Graphics graphics) {
    }

    public void startDeadFly(int i, int i2) {
    }

    public void updateBuff() {
    }

    public int nextInt(int i) {
        return GameScr.abs(r.nextInt(i));
    }

    public void addDynamicEffBuffTop(DynamicEffect dynamicEffect, int i) {
        for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
            if (((DynamicEffect) this.dynamicEffTop.elementAt(i2)).id == dynamicEffect.id) {
                if (i == 1) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(i2)).timeExist = 0L;
                    return;
                }
                return;
            }
        }
        if (i == 0) {
            this.dynamicEffTop.addElement(dynamicEffect);
        }
    }

    public void addDynamicEffBuffBottom(DynamicEffect dynamicEffect, int i) {
        for (int i2 = 0; i2 < this.dynamicEffBottom.size(); i2++) {
            if (((DynamicEffect) this.dynamicEffBottom.elementAt(i2)).id == dynamicEffect.id) {
                if (i == 1) {
                    ((DynamicEffect) this.dynamicEffBottom.elementAt(i2)).timeExist = 0L;
                    return;
                }
                return;
            }
        }
        if (i == 0) {
            this.dynamicEffBottom.addElement(dynamicEffect);
        }
    }

    public void setRealHP(int i) {
        this.realHP = i;
        this.realHPSyncTime = 20;
    }

    public void updateDynamicBuff() {
        if (this.dynamicEffTop != null) {
            for (int i = 0; i < this.dynamicEffTop.size(); i++) {
                DynamicEffect dynamicEffect = (DynamicEffect) this.dynamicEffTop.elementAt(i);
                if (dynamicEffect != null) {
                    dynamicEffect.update();
                    if (dynamicEffect.canDestroy()) {
                        this.dynamicEffTop.removeElementAt(i);
                    }
                }
            }
        }
        if (this.dynamicEffBottom != null) {
            for (int i2 = 0; i2 < this.dynamicEffBottom.size(); i2++) {
                DynamicEffect dynamicEffect2 = (DynamicEffect) this.dynamicEffBottom.elementAt(i2);
                if (dynamicEffect2 != null) {
                    dynamicEffect2.update();
                    if (dynamicEffect2.canDestroy()) {
                        this.dynamicEffBottom.removeElementAt(i2);
                    }
                }
            }
        }
    }

    public boolean attackMiss(LiveActor liveActor) {
        return nextInt(this.accurate + 10) >= this.accurate || nextInt(5000) < liveActor.dodge;
    }

    public boolean isCritical() {
        return nextInt(100) < this.critical;
    }

    public int attackDam(LiveActor liveActor) {
        int i = this.attack - liveActor.defend;
        if (i < 5) {
            i = 5;
        }
        int iNextInt = (i * (nextInt(20) + 90)) / 100;
        if ((this.he + 2) % 5 == liveActor.he) {
            iNextInt = (iNextInt * 12) / 10;
        }
        if (iNextInt <= 0) {
            return 1;
        }
        return iNextInt;
    }

    public void isPoinson() {
        if (!this.die) {
            if ((System.currentTimeMillis() - this.timeOutPoinson >= ((long) (this.tDelay * 1000))) && (this.tDelay > 0)) {
                this.hp -= this.poinson;
                this.totalTime = (short) (this.totalTime - this.tDelay);
                this.timeOutPoinson = System.currentTimeMillis();
                if (this.totalTime == 0) {
                    this.tDelay = (short) 0;
                    this.totalTime = (short) 36;
                }
                if (this.hp <= 0) {
                    this.hp = 1;
                    this.poinson = (short) 0;
                    this.tDelay = (short) 0;
                    this.totalTime = (short) 36;
                    return;
                }
                return;
            }
            return;
        }
        this.hp = 0;
        this.die = false;
        this.tDelay = (short) 0;
        this.timeOutPoinson = 0L;
        this.totalTime = (short) 36;
        this.poinson = (short) 0;
        int i = (this.x - 5) * 2;
        int i2 = (this.y - 5) * 2;
        while (true) {
            if (i <= 10 && i2 <= 10 && i >= -10 && i2 >= -10) {
                startDeadFly(i, i2);
                return;
            } else {
                i >>= 1;
                i2 >>= 1;
            }
        }
    }

    public void updateEffSkill() {
        if (this.effSkill.size() > 0) {
            for (int i = 0; i < this.effSkill.size(); i++) {
                this.effSkill.elementAt(i).update();
            }
        }
    }

    @Override
    public void update() {
        updateEffSkill();
        updateDataEffect();
        int i = this.z;
        if (i < 0) {
            int i2 = this.dz;
            int i3 = i + i2;
            this.z = i3;
            this.dz = i2 + 1;
            if (i3 > 0) {
                this.z = 0;
            }
        }
        int i4 = this.vangx;
        int i5 = this.dvangx;
        int i6 = i4 + i5;
        this.vangx = i6;
        int i7 = this.vangy;
        int i8 = this.dvangy;
        int i9 = i7 + i8;
        this.vangy = i9;
        if (i5 > 0) {
            this.dvangx = i5 - 1;
        }
        int i10 = this.dvangx;
        if (i10 < 0) {
            this.dvangx = i10 + 1;
        }
        if (i8 > 0) {
            this.dvangy = i8 - 1;
        }
        int i11 = this.dvangy;
        if (i11 < 0) {
            this.dvangy = i11 + 1;
        }
        if (this.dvangx == 0 && this.dvangy == 0) {
            if (i6 > 1) {
                this.vangx = i6 >> 1;
            } else {
                this.vangx = 0;
            }
            if (i9 > 1) {
                this.vangy = i9 >> 1;
            } else {
                this.vangy = 0;
            }
        }
        super.update();
    }

    @Override
    public void paint(Graphics graphics) {
        paintEffSkill(graphics);
    }

    public void paintEffSkill(Graphics graphics) {
        if (this.effSkill.size() > 0) {
            for (int i = 0; i < this.effSkill.size(); i++) {
                Effect effectElementAt = this.effSkill.elementAt(i);
                if (effectElementAt != null) {
                    effectElementAt.paint(graphics);
                }
            }
        }
    }

    public void jump() {
        this.z = -3;
        this.dz = -5;
    }

    public void jumpVang(Actor actor) {
        int i;
        this.z = -3;
        this.dz = -3;
        this.dvangx = (short) ((this.x - actor.x) * 2);
        this.dvangy = (short) ((this.y - actor.y) * 2);
        while (true) {
            int i2 = this.dvangx;
            if (i2 <= 4 && (i = this.dvangy) <= 4 && i2 >= -4 && i >= -4) {
                return;
            }
            this.dvangx = i2 >> 1;
            this.dvangy >>= 1;
        }
    }

    public boolean canMove() {
        if (this.dynamicEffTop != null) {
            for (int i = 0; i < this.dynamicEffTop.size(); i++) {
                if (((DynamicEffect) this.dynamicEffTop.elementAt(i)).stop == 1) {
                    return false;
                }
            }
        }
        if (this.dynamicEffBottom != null) {
            for (int i2 = 0; i2 < this.dynamicEffBottom.size(); i2++) {
                if (((DynamicEffect) this.dynamicEffBottom.elementAt(i2)).stop == 1) {
                    return false;
                }
            }
        }
        return (this.isFreeze || this.beStune) ? false : true;
    }

    public void doublejump() {
        this.z = -4;
        this.dz = -4;
    }

    public void smalljump() {
        this.z = -2;
        this.dz = -2;
        if (this.hp <= 0) {
            dead();
        }
    }

    public void updateDataEffect() {
        int size = this.Vec_Effect_Skill.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                DataSkillEff dataSkillEff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
                if (dataSkillEff != null) {
                    dataSkillEff.update();
                    if (dataSkillEff.wantDetroy) {
                        this.Vec_Effect_Skill.removeElement(dataSkillEff);
                    }
                }
            }
        }
    }

    public void paintDataEff_Top(Graphics graphics, int i, int i2, boolean z) {
        int size = this.Vec_Effect_Skill.size();
        if (size > 0) {
            for (int i3 = 0; i3 < size; i3++) {
                DataSkillEff dataSkillEff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i3);
                if (dataSkillEff != null) {
                    dataSkillEff.paintTop(graphics, i, (z ? dataSkillEff.dyhorse : (byte) 0) + i2);
                }
            }
        }
    }

    public void paintDataEff_Bot(Graphics graphics, int i, int i2, boolean z) {
        int size = this.Vec_Effect_Skill.size();
        if (size > 0) {
            for (int i3 = 0; i3 < size; i3++) {
                DataSkillEff dataSkillEff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i3);
                if (dataSkillEff != null) {
                    dataSkillEff.paintBottom(graphics, i, (z ? dataSkillEff.dyhorse : (byte) 0) + i2);
                }
            }
        }
    }

    @Override
    public void addEffectSkillTime(int i, int i2, int i3, long j, boolean z, boolean z2, boolean z3, int i4, byte b, byte b2) {
        int size = this.Vec_Effect_Skill.size();
        if (size > 0) {
            for (int i5 = 0; i5 < size; i5++) {
                DataSkillEff dataSkillEff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i5);
                if (dataSkillEff != null && dataSkillEff.idEff == i) {
                    dataSkillEff.timelive = j;
                    return;
                }
            }
        }
        DataSkillEff dataSkillEff2 = new DataSkillEff(i, i2, i3, j, z, z2, z3, i4, b2);
        dataSkillEff2.setWaitLoop(b);
        this.Vec_Effect_Skill.addElement(dataSkillEff2);
    }

    @Override
    public void removeDataSkillEff(int i) {
        for (int i2 = 0; i2 < this.Vec_Effect_Skill.size(); i2++) {
            DataSkillEff dataSkillEff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i2);
            if (dataSkillEff != null && dataSkillEff.idEff == i) {
                this.Vec_Effect_Skill.removeElement(dataSkillEff);
            }
        }
    }

    public boolean can_Paint() {
        for (int i = 0; i < this.Vec_Effect_Skill.size(); i++) {
            DataSkillEff dataSkillEff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
            if (dataSkillEff != null && dataSkillEff.canPaintActor) {
                return false;
            }
        }
        return true;
    }

    public boolean mcanmove() {
        for (int i = 0; i < this.Vec_Effect_Skill.size(); i++) {
            DataSkillEff dataSkillEff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
            if (dataSkillEff != null && dataSkillEff.canActorMove) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getSpeed() {
        return this.speed;
    }

    @Override
    public int getDir() {
        return this.dir;
    }

    @Override
    public void setHP(int i) {
        this.hp = i;
    }

    @Override
    public void addEffbuff(int i, int i2, int i3, long j) {
        int size = this.Vec_Effect_Skill.size();
        if (size > 0) {
            for (int i4 = 0; i4 < size; i4++) {
                DataSkillEff dataSkillEff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i4);
                if (dataSkillEff != null && dataSkillEff.idEff == i) {
                    dataSkillEff.timelive = j;
                    return;
                }
            }
        }
        this.Vec_Effect_Skill.addElement(new DataSkillEff(i, i2, i3, j));
    }
}
