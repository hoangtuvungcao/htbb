package game.model;

import effect.DataSkillEff;
import effect.DynamicEffect;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public abstract class LiveActor extends Actor {
    public int realHP;
    public int realHPSyncTime;
    public int hp;
    public int maxhp;
    public int z;
    public int dz;
    public int dvangx;
    public int dvangy;
    public int vangx;
    public int vangy;
    public short dir;
    public short accurate;
    public short dodge;
    public short critical;
    public short dy;
    public short speed;
    public int damAttack;
    public int attack;
    public int defend;
    public int defend_magic;
    public byte level;
    public byte frame;
    public byte he;
    public static final byte HE_THUY = 0;
    public static final byte HE_MOC = 1;
    public static final byte HE_HOA = 2;
    public static final byte HE_THO = 3;
    public static final byte HE_KIM = 4;
    public static Random r = new Random(System.currentTimeMillis());
    public byte idBoss = -1;
    public Vector effSkill = new Vector();
    public boolean isFreeze = false;
    public Vector dynamicEffTop = new Vector();
    public Vector dynamicEffBottom = new Vector();
    public Vector Vec_Effect_Skill = new Vector();
    public short totalTime = 36;

    // pokemon: dong tac PokeBody dang ve va tick luc bat dau (Unity LiveActor.cs)
    public int pokeDongTacCu = -1;
    public int pokeTickBatDau;
    // pokemon: phan hoi chien dau (game.pokemon.HieuUngDanh): tick bi trung (chop trang + lac),
    // tick bi bong (sat thuong theo thoi gian), trang thai server gui (-97: 1 bong 2 doc 3 te 4 ngu 5 bang)
    public int pokeTrungLuc = -1;
    public int pokeBongLuc = -1;
    public int pokeTT;
    public int pokeTTChong;
    public long pokeTTHet;

    private int pokeTuoiTrung() {
        if (this.pokeTrungLuc < 0) {
            return 99;
        }
        int t = game.render.Canvas.gameTick - this.pokeTrungLuc;
        return t < 0 ? t + 10001 : t;
    }

    /** Dang chop trang (2 khung dau sau khi trung). */
    public boolean pokeChop() {
        int t = pokeTuoiTrung();
        return t == 0 || t == 2;
    }

    /** Lac ngang (px) sau khi trung, tat dan trong 6 tick. */
    public int pokeLac() {
        int t = pokeTuoiTrung();
        if (t >= 6) {
            return 0;
        }
        int a = (6 - t + 1) / 2;
        return (t & 1) == 0 ? a : -a;
    }

    /** So tick ke tu luc bat dau dong tac hien tai (Canvas.gameTick quay ve 0 sau 10000). */
    public int pokeTickTrongDongTac(int dongTac) {
        if (dongTac != this.pokeDongTacCu) {
            this.pokeDongTacCu = dongTac;
            this.pokeTickBatDau = game.render.Canvas.gameTick;
        }
        int t = game.render.Canvas.gameTick - this.pokeTickBatDau;
        if (t < 0) {
            t += 10001;
        }
        return t;
    }

    /** Dang bi danh trung: dang nay len (jump/jumpVang) hoac dang bi day lui. */
    public boolean pokeDangTrungDon() {
        return this.z < 0 || this.dvangx != 0 || this.dvangy != 0;
    }

    public int nextInt(int max) {
        return GameScr.abs(r.nextInt(max));
    }

    public void setRealHP(int realHP) {
        this.realHP = realHP;
        this.realHPSyncTime = 20;
    }

    public void addDynamicEffBuffTop(DynamicEffect d, int add_remove) {
        for (int i = 0; i < this.dynamicEffTop.size(); i++) {
            if (((DynamicEffect) this.dynamicEffTop.elementAt(i)).id == d.id) {
                if (add_remove == 1) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(i)).timeExist = 0L;
                    return;
                }
                return;
            }
        }
        if (add_remove == 0) {
            this.dynamicEffTop.addElement(d);
        }
    }

    public void addDynamicEffBuffBottom(DynamicEffect d, int add_remove) {
        for (int i = 0; i < this.dynamicEffBottom.size(); i++) {
            if (((DynamicEffect) this.dynamicEffBottom.elementAt(i)).id == d.id) {
                if (add_remove == 1) {
                    ((DynamicEffect) this.dynamicEffBottom.elementAt(i)).timeExist = 0L;
                    return;
                }
                return;
            }
        }
        if (add_remove == 0) {
            this.dynamicEffBottom.addElement(d);
        }
    }

    public void updateBuff() {
    }

    @Override
    public void setHP(int hp) {
        this.hp = hp;
    }

    public void updateDynamicBuff() {
        if (this.dynamicEffTop != null) {
            for (int i = 0; i < this.dynamicEffTop.size(); i++) {
                DynamicEffect dym = (DynamicEffect) this.dynamicEffTop.elementAt(i);
                dym.update();
                if (dym.canDestroy()) {
                    this.dynamicEffTop.removeElementAt(i);
                }
            }
        }
        if (this.dynamicEffBottom != null) {
            for (int i2 = 0; i2 < this.dynamicEffBottom.size(); i2++) {
                DynamicEffect dym2 = (DynamicEffect) this.dynamicEffBottom.elementAt(i2);
                dym2.update();
                if (dym2.canDestroy()) {
                    this.dynamicEffBottom.removeElementAt(i2);
                }
            }
        }
    }

    public boolean attackMiss(LiveActor actor) {
        int r1 = nextInt(this.accurate + 10);
        int r2 = nextInt(5000);
        if (r1 >= this.accurate || r2 < actor.dodge) {
            return true;
        }
        return false;
    }

    public boolean isCritical() {
        return nextInt(100) < this.critical;
    }

    public int attackDam(LiveActor actor) {
        int dam = this.attack - actor.defend;
        if (dam < 5) {
            dam = 5;
        }
        int dam2 = (dam * (90 + nextInt(20))) / 100;
        if ((this.he + 2) % 5 == actor.he) {
            dam2 = (dam2 * 12) / 10;
        }
        if (dam2 <= 0) {
            dam2 = 1;
        }
        return dam2;
    }

    public void startDeadFly(int dx, int dy) {
    }

    public void paintTopDataEff(Graphics g) {
    }

    public void paintBottomDataEff(Graphics g) {
    }

    public void isPoinson() {
        if (!this.die) {
            if ((System.currentTimeMillis() - this.timeOutPoinson >= ((long) (this.tDelay * 1000))) & (this.tDelay > 0)) {
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
        int dx = (this.x - 5) * 2;
        int i = (this.y - 5) * 2;
        while (true) {
            int dy = i;
            if (dx > 10 || dy > 10 || dx < -10 || dy < -10) {
                dx >>= 1;
                i = dy >> 1;
            } else {
                startDeadFly(dx, dy);
                return;
            }
        }
    }

    public void updateEffSkill() {
        if (this.effSkill.size() > 0) {
            for (int i = 0; i < this.effSkill.size(); i++) {
                Effect eff = (Effect) this.effSkill.elementAt(i);
                eff.update();
            }
        }
    }

    @Override
    public void update() {
        updateEffSkill();
        updateDataEffect();
        if (this.z < 0) {
            this.z += this.dz;
            this.dz++;
            if (this.z > 0) {
                this.z = 0;
            }
        }
        this.vangx += this.dvangx;
        this.vangy += this.dvangy;
        if (this.dvangx > 0) {
            this.dvangx--;
        }
        if (this.dvangx < 0) {
            this.dvangx++;
        }
        if (this.dvangy > 0) {
            this.dvangy--;
        }
        if (this.dvangy < 0) {
            this.dvangy++;
        }
        if (this.dvangx == 0 && this.dvangy == 0) {
            if (this.vangx > 1) {
                this.vangx >>= 1;
            } else {
                this.vangx = 0;
            }
            if (this.vangy > 1) {
                this.vangy >>= 1;
            } else {
                this.vangy = 0;
            }
        }
        super.update();
    }

    @Override
    public void paint(Graphics g) {
        paintEffSkill(g);
    }

    public void paintEffSkill(Graphics g) {
        if (this.effSkill.size() > 0) {
            for (int i = 0; i < this.effSkill.size(); i++) {
                Effect eff = (Effect) this.effSkill.elementAt(i);
                eff.paint(g);
            }
        }
    }

    public void jump() {
        this.z = -3;
        this.dz = -5;
    }

    public void jumpVang(Actor causeBy) {
        this.z = -3;
        this.dz = -3;
        this.dvangx = (short) ((this.x - causeBy.x) * 2);
        this.dvangy = (short) ((this.y - causeBy.y) * 2);
        while (true) {
            if (this.dvangx > 4 || this.dvangy > 4 || this.dvangx < -4 || this.dvangy < -4) {
                this.dvangx >>= 1;
                this.dvangy >>= 1;
            } else {
                return;
            }
        }
    }

    public boolean FuntioncanMove() {
        if (this.dynamicEffTop != null) {
            for (int i = 0; i < this.dynamicEffTop.size(); i++) {
                DynamicEffect dym = (DynamicEffect) this.dynamicEffTop.elementAt(i);
                if (dym.stop == 1) {
                    return false;
                }
            }
        }
        if (this.dynamicEffBottom != null) {
            for (int i2 = 0; i2 < this.dynamicEffBottom.size(); i2++) {
                DynamicEffect dym2 = (DynamicEffect) this.dynamicEffBottom.elementAt(i2);
                if (dym2.stop == 1) {
                    return false;
                }
            }
        }
        return (this.isFreeze || this.beStune) ? false : true;
    }

    public boolean canMove() {
        if (this.dynamicEffTop != null) {
            for (int i = 0; i < this.dynamicEffTop.size(); i++) {
                DynamicEffect dym = (DynamicEffect) this.dynamicEffTop.elementAt(i);
                if (dym.stop == 1) {
                    return false;
                }
            }
        }
        if (this.dynamicEffBottom != null) {
            for (int i2 = 0; i2 < this.dynamicEffBottom.size(); i2++) {
                DynamicEffect dym2 = (DynamicEffect) this.dynamicEffBottom.elementAt(i2);
                if (dym2.stop == 1) {
                    return false;
                }
            }
        }
        return (this.isFreeze || this.beStune) ? false : true;
    }

    public void updateDataEffect() {
        int size = this.Vec_Effect_Skill.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                DataSkillEff dataeff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
                if (dataeff != null) {
                    dataeff.update();
                    if (dataeff.wantDetroy) {
                        this.Vec_Effect_Skill.removeElement(dataeff);
                    }
                }
            }
        }
    }

    public void paintDataEff_Top(Graphics g, int x, int y, boolean isHorse) {
        int size = this.Vec_Effect_Skill.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                DataSkillEff dataeff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
                if (dataeff != null) {
                    dataeff.paintTop(g, x, y + (isHorse ? dataeff.dyhorse : (byte) 0));
                }
            }
        }
    }

    public void paintDataEff_Bot(Graphics g, int x, int y, boolean isHorse) {
        int sizeBT = this.Vec_Effect_Skill.size();
        if (sizeBT > 0) {
            for (int i = 0; i < sizeBT; i++) {
                DataSkillEff dataeff = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
                if (dataeff != null) {
                    dataeff.paintBottom(g, x, y + (isHorse ? dataeff.dyhorse : (byte) 0));
                }
            }
        }
    }

    @Override
    public void addEffectSkillTime(int id, int x, int y, long timelive, boolean canmove, boolean canpaint, boolean canfight, int loop, byte WaitLoop, byte dyhorse) {
        int size = this.Vec_Effect_Skill.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                DataSkillEff data = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
                if (data != null && data.idEff == id) {
                    data.timelive = timelive;
                    return;
                }
            }
        }
        DataSkillEff eff = new DataSkillEff(id, x, y, timelive, canmove, canpaint, canfight, loop, dyhorse);
        eff.setWaitLoop(WaitLoop);
        this.Vec_Effect_Skill.addElement(eff);
    }

    @Override
    public void addEffbuff(int id, int x, int y, long timelive) {
        int size = this.Vec_Effect_Skill.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                DataSkillEff data = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
                if (data != null && data.idEff == id) {
                    data.timelive = timelive;
                    return;
                }
            }
        }
        DataSkillEff eff = new DataSkillEff(id, x, y, timelive);
        this.Vec_Effect_Skill.addElement(eff);
    }

    public void removeDataSkillEff(int id) {
        for (int i = 0; i < this.Vec_Effect_Skill.size(); i++) {
            DataSkillEff dataskill = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
            if (dataskill != null && dataskill.idEff == id) {
                this.Vec_Effect_Skill.removeElement(dataskill);
            }
        }
    }

    public boolean can_Paint() {
        for (int i = 0; i < this.Vec_Effect_Skill.size(); i++) {
            DataSkillEff data = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
            if (data != null && data.canPaintActor) {
                return false;
            }
        }
        return true;
    }

    public boolean mcanmove() {
        for (int i = 0; i < this.Vec_Effect_Skill.size(); i++) {
            DataSkillEff data = (DataSkillEff) this.Vec_Effect_Skill.elementAt(i);
            if (data != null && data.canActorMove) {
                return false;
            }
        }
        return true;
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

    public byte getHorse() {
        return (byte) 0;
    }

    @Override
    public int getLV() {
        return this.level;
    }

    @Override
    public byte getState() {
        return this.state;
    }

    @Override
    public short getSpeed() {
        return this.speed;
    }

    @Override
    public short getDir() {
        return this.dir;
    }
}
