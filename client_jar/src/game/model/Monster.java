package game.model;

import effect.DynamicEffect;
import game.model.skill.SkillAnimate;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Vector;
import game.pokemon.PokeBody;
import game.pokemon.PokeDuLieu;
import game.pokemon.VatThePokemon;
import javax.microedition.lcdui.Graphics;

public class Monster extends LiveActor {
    public static final byte STAND = 0;
    public static final byte DEAD = 1;
    public static final byte WALK = 2;
    public static final byte ATTACK = 3;
    public static final byte INJURE = 4;
    public static final byte DEADFLY = 5;
    public static final byte MOVE_TO_TARGET = 6;
    public static final byte GO_TO_XYFIRST = 7;
    public static final byte WAIT = 8;
    public static final byte TYPE4 = 0;
    public static final byte TYPE2 = 1;
    public static final byte TYPE1 = 2;
    public static final byte TYPE3 = 3;
    public static final byte TYPE0 = 4;
    public LiveActor target;
    public static final byte[][][] REAL_FRAME = {new byte[][]{new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}, new byte[]{8, 9, 10, 11}, new byte[]{12, 13, 14, 15}}, new byte[][]{new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}, new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}}, new byte[][]{new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}}, new byte[][]{new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}, new byte[]{0, 1, 2, 3}, new byte[]{4, 5, 6, 7}}, new byte[][]{new byte[4], new byte[4], new byte[4], new byte[4]}};
    public SkillAnimate currentSkillAnimate;
    public LiveActor attackTarget;
    public short xTo;
    public short yTo;
    public short p1;
    public short p2;
    public short p3;
    public short monster_type;
    long timeCreate;
    int xadd;
    int yadd;
    long timeBeginDie;
    long timeRevive;
    int timeWait;
    int timeLimit;
    int lastTimeStop;
    byte vx;
    byte vy;
    int a;
    int b;
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

    public Monster() {
        this.dir = (byte) LiveActor.r.nextInt(4);
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
    public void setType(short type) {
        this.monster_type = type;
        if (Res.monsterTemplates != null && Res.monsterTemplates[type] == null) {
            Res.monsterTemplates[type] = new MonsterTemplate();
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

    public void setTarget(LiveActor tg) {
        this.target = tg;
        this.state = (byte) 6;
    }

    @Override
    public void dead() {
    }

    @Override
    public boolean isPaint() {
        if (this.x < GameScr.cmx || this.x > GameScr.cmx + Canvas.w || this.y < GameScr.cmy || this.y > GameScr.cmy + Canvas.h + 30) {
            return false;
        }
        return true;
    }

    /**
     * pokemon: trang thai quai -> dong tac PokeBody. STAND 0, WALK 2 / MOVE_TO_TARGET 6 /
     * GO_TO_XYFIRST 7 -> chay, ATTACK 3 -> danh gan, INJURE 4 hoac dang nay len/bi day lui ->
     * trung don, DEAD 1 / DEADFLY 5 -> guc nga.
     */
    public int pokeDongTac() {
        switch (this.state) {
            case 1:
            case 5:
                return PokeDuLieu.GUC_NGA;
            case 3:
                return PokeDuLieu.DANH_GAN;
            case 4:
                return PokeDuLieu.TRUNG_DON;
        }
        if (pokeDangTrungDon()) {
            return PokeDuLieu.TRUNG_DON;
        }
        if (this.state == 2 || this.state == 6 || this.state == 7) {
            return PokeDuLieu.CHAY;
        }
        return PokeDuLieu.DUNG;
    }

    /**
     * pokemon: ve quai bang PokeBody neu template co pokeDex (lenh -91). false = lop goi ve kieu
     * cu. Dung chung cho Monster va cac lop boss con (Boss_*, BossGyarados, MonsterNew...).
     */
    public boolean paintPokemon(Graphics g) {
        int t = this.monster_type;
        if (!MonsterTemplate.laPokemon(t)) {
            return paintVatThe(g);
        }
        if (!isPaint() || this.state == 8 || this.hp <= 0) {
            return true;
        }
        paintDataEff_Top(g, this.x, this.y, false);
        if (!this.isFreeze) {
            if (this.dynamicEffBottom != null) {
                for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                    ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                }
            }
            g.drawImage(Res.imgShadow, this.x, this.y, 3);
            int cao = PokeBody.caoTrenChan(MonsterTemplate.pokeDex[t], MonsterTemplate.pokeShiny[t], MonsterTemplate.pokeTiLe[t]) + 4;
            if (cao > this.height) {
                this.height = (short) cao;
            }
            int dongTac = pokeDongTac();
            int tick = pokeTickTrongDongTac(dongTac);
            // pokemon: chop trang + lac nhe khi trung don (HieuUngDanh)
            PokeBody.paint(g, MonsterTemplate.pokeDex[t], MonsterTemplate.pokeShiny[t], this.x + this.vangx + pokeLac(), this.y + this.vangy + this.dy + this.z, this.dir, dongTac, tick, MonsterTemplate.pokeTiLe[t], pokeChop() ? 230 : 0);
            for (int i = 0; i < this.buff.size(); i++) {
                ((CharBuff) this.buff.elementAt(i)).paint(g);
            }
            if (this.dynamicEffTop != null) {
                for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(i2)).paint(g, this.x, this.y);
                }
            }
        }
        paintDataEff_Bot(g, this.x, this.y, false);
        super.paint(g);
        return true;
    }

    /** pokemon: quai phan_loai vat_the (cong thanh, nguyen lieu, ngoc, ruong, thung) ve do vat PMD. */
    public boolean paintVatThe(Graphics g) {
        int t = this.monster_type;
        if (!VatThePokemon.laVatThe(t)) {
            return false;
        }
        if (!isPaint() || this.state == 8 || this.hp <= 0) {
            return true;
        }
        paintDataEff_Top(g, this.x, this.y, false);
        if (!this.isFreeze) {
            int cao = VatThePokemon.caoQuai(t) + 4;
            if (cao > this.height) {
                this.height = (short) cao;
            }
            g.drawImage(Res.imgShadow, this.x, this.y, 3);
            VatThePokemon.veQuai(g, t, this.x + this.vangx, this.y + this.vangy + this.dy + this.z, Canvas.gameTick);
            for (int i = 0; i < this.buff.size(); i++) {
                ((CharBuff) this.buff.elementAt(i)).paint(g);
            }
        }
        paintDataEff_Bot(g, this.x, this.y, false);
        super.paint(g);
        return true;
    }

    @Override
    public void paint(Graphics g) {
        if (paintPokemon(g)) {
            return;
        }
        if (!isPaint() || this.state == 8 || this.hp <= 0) {
            return;
        }
        paintDataEff_Top(g, this.x, this.y, false);
        if (!this.isFreeze) {
            if (this.z != 0) {
                this.frame = (byte) 3;
            }
            if (Res.monsterTemplates[this.monster_type] != null) {
                byte b = REAL_FRAME[Res.monsterTemplates[this.monster_type].moveType][this.dir][this.frame];
                int xadd = 0;
                int yadd = 0;
                if (Res.monsterTemplates[this.monster_type].image != null) {
                    xadd = Res.monsterTemplates[this.monster_type].xadd;
                    yadd = Res.monsterTemplates[this.monster_type].yadd;
                }
                if (this.dynamicEffBottom != null) {
                    for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                    }
                }
                g.drawImage(Res.imgShadow, this.x + xadd, this.y + yadd, 3);
                Res.monsterTemplates[this.monster_type].paint(g, this.x + this.vangx, this.y + this.vangy + this.dy + this.z, 0, 0, b);
            }
            for (int i = 0; i < this.buff.size(); i++) {
                ((CharBuff) this.buff.elementAt(i)).paint(g);
            }
            if (this.dynamicEffTop != null) {
                for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(i2)).paint(g, this.x, this.y);
                }
            }
        }
        paintDataEff_Bot(g, this.x, this.y, false);
        super.paint(g);
    }

    @Override
    public void paintActive(Graphics g, int x, int y) {
    }

    // pokemon: nhãn "Lv N Tên" + thanh HP trên đầu (game.pokemon.NhanQuai), nhớ chuỗi theo tên / cấp
    public String nhan;
    public String nhanTen;
    public int nhanLv = -1;

    @Override
    public void paintName(Graphics g) {
        super.paintName(g);
        game.pokemon.NhanQuai.ve(g, this);
    }

    @Override
    public void paintCorner(Graphics g, int xCorner, int yCorner) {
        if (this.z != 0) {
            this.frame = (byte) 3;
        }
        if (VatThePokemon.veGocQuai(g, this.monster_type, xCorner, yCorner - 8, 24)) {
            return;
        }
        if (MonsterTemplate.laPokemon(this.monster_type)) {
            int t = this.monster_type;
            PokeBody.paintChanDung(g, MonsterTemplate.pokeDex[t], MonsterTemplate.pokeShiny[t], xCorner, yCorner - 8, 24);
            if (this.he >= 0 && this.he < this.nh.length) {
                g.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, 20);
            }
            return;
        }
        try {
            g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, REAL_FRAME[Res.monsterTemplates[this.monster_type].moveType][0][this.frame] * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, 0, xCorner - Res.monsterTemplates[this.monster_type].xCenter, yCorner - 20, 0);
            g.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, 20);
        } catch (Exception e) {
        }
    }

    @Override
    public void createBuff(int typeBuffEff, int timeLive) {
        this.buff.addElement(new CharBuff(this.x, this.y, typeBuffEff));
    }

    @Override
    public void updateBuff() {
        for (int i = 0; i < this.buff.size(); i++) {
            CharBuff cbuf = (CharBuff) this.buff.elementAt(i);
            cbuf.update();
            if (cbuf.wantDetroy) {
                this.buff.removeElementAt(i);
            } else {
                cbuf.setXY(this.x, cbuf.isCenter() ? this.y - 12 : this.y);
            }
        }
    }

    @Override
    public void startDeadFly(int dx, int dy) {
        this.state = (byte) 5;
        this.p1 = (short) 0;
        this.p2 = (short) dx;
        this.p3 = (short) dy;
        if (this.isDied) {
            Canvas.gameScr.startExplosionAt(this.x, this.y);
            this.wantDestroy = true;
        }
    }

    @Override
    public void update() {
        updateDataEffect();
        this.speed = Res.monsterTemplates[this.monster_type].speed;
        if ((this.wantDestroy || this.hp <= 0) && this.timeLive != -1 && this.state != 8) {
            this.state = (byte) 8;
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
        switch (this.state) {
            case 0:
                this.frame = (byte) 0;
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
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > 6) {
                    this.p1 = (short) 0;
                }
                if (this.p1 > 2) {
                    this.frame = (byte) 1;
                } else {
                    this.frame = (byte) 0;
                }
                if (FuntioncanMove()) {
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
                        int[] dd = diemDuoi(this.target);
                        setMove(dd[0], dd[1]);
                    }
                }
                break;
            case 3:
                this.frame = (byte) 2;
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > 6) {
                    this.p1 = (short) 0;
                    this.state = (byte) 2;
                } else if (this.currentSkillAnimate != null) {
                    this.currentSkillAnimate.updateSkill(this);
                }
                break;
            case 4:
                this.frame = (byte) 3;
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 == 5) {
                    Canvas.gameScr.startExplosionAt(this.x, this.y);
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
                    Canvas.gameScr.startExplosionAt(this.x, this.y);
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

    public void setMove(int i, int j) {
        if (Res.monsterTemplates[this.monster_type].moveType == 4) {
            this.p3 = (short) 0;
            this.p2 = (short) 0;
            this.p1 = (short) 0;
            this.state = (byte) 0;
            return;
        }
        boolean isMatchX = false;
        boolean isMatchY = false;
        int dx1 = Math.abs(this.x - i);
        int dy1 = Math.abs(this.y - j);
        if (dx1 <= this.speed) {
            this.x = (short) i;
            isMatchX = true;
        }
        if (dy1 < this.speed) {
            this.y = (short) j;
            isMatchY = true;
        }
        if (isMatchX && isMatchY) {
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
        // pokemon 0.9.3: bước theo va chạm (không đi xuyên tường / nước / ra ngoài mép map)
        if (!buocToi(i, j)) {
            if (this.state == 7 && diQuai(i, j)) {
                // về điểm sinh mà bị tường chắn: dịch thẳng về (điểm sinh do máy chủ đặt trên ô đi được)
                this.x = (short) i;
                this.y = (short) j;
                this.state = (byte) 0;
                this.timeLimit = Res.random(10, 20);
            }
            this.vy = (byte) 0;
            this.vx = (byte) 0;
        }
    }

    /** Điểm pixel quái đi được trên bản đồ hiện tại (DuongDi.diQuai: cùng luật người chơi / máy chủ). */
    public static boolean diQuai(int px, int py) {
        return DuongDi.diQuai(Tilemap.type, Tilemap.map, Tilemap.w, Tilemap.h, px, py);
    }

    /** Một bước speed px về (tx, ty) vòng qua ô chặn; false = kẹt (không đổi vị trí). */
    boolean buocToi(int tx, int ty) {
        if (!DuongDi.buocQuai(Tilemap.type, Tilemap.map, Tilemap.w, Tilemap.h, this.x, this.y, tx, ty, this.speed)) {
            return false;
        }
        int nx = DuongDi.buocX, ny = DuongDi.buocY;
        this.dir = (short) (nx > this.x ? 3 : nx < this.x ? 2 : ny < this.y ? 1 : 0);
        this.x = (short) nx;
        this.y = (short) ny;
        return true;
    }

    /** Điểm đuổi quanh mục tiêu (lệch xadd / yadd); lệch rơi vào ô chặn thì nhắm đúng chỗ mục tiêu. */
    int[] diemDuoi(LiveActor t) {
        int tx = t.x + this.xadd, ty = t.y + this.yadd;
        if (!diQuai(tx, ty)) {
            tx = t.x;
            ty = t.y;
        }
        return new int[]{tx, ty};
    }

    public void move() {
        if (Res.monsterTemplates[this.monster_type].moveType == 4) {
            this.p3 = (short) 0;
            this.p2 = (short) 0;
            this.p1 = (short) 0;
            this.state = (byte) 0;
            return;
        }
        if (this.idChar == -1) {
            short xL = (short) ((this.xFirst - this.range) + this.speed);
            short xR = (short) ((this.xFirst + this.range) - this.speed);
            short yU = (short) ((this.yFirst - this.range) + this.speed);
            short yD = (short) ((this.yFirst + this.range) - this.speed);
            int xl = this.x - this.speed;
            int xr = this.x + this.speed;
            int yu = this.y - this.speed;
            int yd = this.y + this.speed;
            int rd = LiveActor.r.nextInt(50);
            if ((this.x >= xR && this.dir == 3) || ((this.x <= xL && this.dir == 2) || ((this.y <= yU && this.dir == 1) || ((this.y >= yD && this.dir == 0) || ((!diQuai(xl, this.y) && this.dir == 2) || ((!diQuai(xr, this.y) && this.dir == 3) || ((!diQuai(this.x, yu) && this.dir == 1) || ((!diQuai(this.x, yd) && this.dir == 0) || rd == 5 || !diQuai(this.x + this.vx, this.y + this.vy))))))))) {
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
        boolean isMatchX = false;
        boolean isMatchY = false;
        int dx1 = Math.abs(this.x - this.xTo);
        int dy1 = Math.abs(this.y - this.yTo);
        if (dx1 <= this.speed) {
            this.x = this.xTo;
            isMatchX = true;
        }
        if (dy1 < this.speed) {
            this.y = this.yTo;
            isMatchY = true;
        }
        if (isMatchX && isMatchY) {
            this.p3 = (short) 0;
            this.p2 = (short) 0;
            this.p1 = (short) 0;
            this.state = (byte) 0;
            return;
        }
        // pokemon 0.9.3: nội suy giữa các vị trí máy chủ theo va chạm; kẹt thì đặt đúng vị trí máy chủ
        // (không tự bịa vị trí xuyên tường)
        if (!buocToi(this.xTo, this.yTo)) {
            this.x = this.xTo;
            this.y = this.yTo;
        }
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
                this.yTo = (short) ((yTo - 4) + (Canvas.r.nextInt() % 8));
                if (!diQuai(this.xTo, this.yTo)) {
                    this.yTo = yTo;   // lệch ngẫu nhiên rơi vào ô chặn: giữ đúng vị trí máy chủ
                }
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
        int[] ar = {16, 32, 48, -16, -32, -48};
        this.xadd = ar[LiveActor.r.nextInt(ar.length - 1)];
        this.yadd = ar[LiveActor.r.nextInt(ar.length - 1)];
        if (this.isMineral) {
            this.x = monsterInfo.x;
            this.y = monsterInfo.y;
        } else {
            this.xTo = monsterInfo.x;
            this.yTo = monsterInfo.y;
            this.a = monsterInfo.x;
            this.b = monsterInfo.y;
        }
        this.rangestop = LiveActor.r.nextInt(10) + 6;
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

    public void startAttack(LiveActor target, int hpLost, byte skillID, byte clazz) {
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.targethplost = hpLost;
        this.attackTarget = target;
        this.currentSkillAnimate = SkillManager.getSkillAnimate(skillID, clazz);
    }

    public void startAttack(Vector list, byte skillID) {
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        if (this.monster_type == 90) {
            this.currentSkillAnimate = SkillManager.setSkillBoss_Xmas();
            this.currentSkillAnimate.setMonster(list);
        }
    }

    @Override
    public void jump() {
        if (!this.isMineral) {
            this.z = -3;
            this.dz = -5;
        }
    }

    public void setHp(int dam) {
        this.hp -= dam;
    }

    @Override
    public void setRealHP(int realHP) {
        this.realHP = realHP;
        this.hp = this.realHP;
        this.realHPSyncTime = 20;
    }

    @Override
    public boolean isMonster() {
        return true;
    }

    @Override
    public boolean isMineral() {
        return this.isMineral;
    }
}
