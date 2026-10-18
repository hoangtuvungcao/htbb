package game.model;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public abstract class Actor {
    public static final byte CAT_PLAYER = 0;
    public static final byte CAT_MONSTER = 1;
    public static final byte CAT_NPC = 2;
    public static final byte CAT_ITEM = 3;
    public static final byte CAT_POTION = 4;
    public static final byte CAT_PARTY = 5;
    public static final byte CAT_GEM_ITEM = 6;
    public static final byte CAT_SPECIAL_ITEM = 7;
    public static final byte CAT_MY_GROUND = 10;
    public static final byte CAT_MY_TREE = 11;
    public static final byte CAT_MY_PET = 12;
    public static final byte CAT_TREE = 13;
    public static final byte CAT_ITEM_QUEST = 14;
    public static final byte CAT_PET_TOOL = 15;
    public static final byte CAT_EXPLOTION = 126;
    public static final int CAT_CAN_NOT_FOCUS = 127;
    public boolean wantDestroy;
    public byte catagory;
    public short x;
    public short y;
    public short height;
    public short width;
    public short xFirst;
    public short yFirst;
    public short centerX;
    public short centerY;
    public short killer;
    public byte typeLimit;
    public byte state;
    public boolean beStune;
    public boolean isFocus;
    public boolean iskiller;
    public long timeBeStune;
    public ChatPopup chat;
    public String infoGround;
    public short poinson;
    public long timeOutPoinson;
    public boolean die;
    public short ID = 32001;
    public short idClan = -1;
    public short IDParty = -1;
    public short IDMasterParty = -1;
    public byte pk = -1;
    public byte nationID = -1;
    public byte inCountry = -1;
    public String nameUserTrade = "";
    public boolean isWearing = false;
    public boolean isMaininfo = false;
    public boolean isDie = false;
    public int timeLive = -1;
    public Vector buff = new Vector();
    public short tDelay = 0;
    public boolean canfocus = true;
    public long testmove = System.currentTimeMillis();

    public abstract void paint(Graphics graphics);

    public abstract void setPosTo(short s, short s2);

    public void actorDie() {
    }

    public void createBuff(int typeBuffEff, int timeLive) {
    }

    public void update() {
        if (this.chat != null) {
            this.chat.setPos(this.x, (this.y - this.height) - 10);
            if (this.chat.setOut()) {
                this.chat = null;
            }
        }
    }

    public boolean isDropItem() {
        return false;
    }

    public void addBuff(CharBuff b) {
        for (int i = 0; i < this.buff.size(); i++) {
            CharBuff cb = (CharBuff) this.buff.elementAt(i);
            if (cb.type == b.type) {
                this.buff.removeElementAt(i);
                break;
            }
        }
        this.buff.addElement(b);
    }

    public boolean isPaint() {
        return true;
    }

    public void setType(short type) {
    }

    public int checkMagicShield(int dam) {
        return 0;
    }

    public void paintCorner(Graphics g, int xCorner, int yCorner) {
        g.translate((-this.x) + xCorner, (-this.y) + yCorner);
        paint(g);
    }

    public void paintActive(Graphics g, int x, int y) {
    }

    public void paintName(Graphics g) {
        if (this.chat != null) {
            this.chat.paintAnimal(g);
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public String getDisplayName() {
        return new StringBuffer("Actor C=").append((int) this.catagory).append(" ID=").append((int) this.ID).toString();
    }

    public boolean allwayPaint() {
        return false;
    }

    public void dead() {
    }

    public boolean isNPC() {
        return false;
    }

    public boolean isMonster() {
        return false;
    }

    public boolean isMineral() {
        return false;
    }

    public boolean isPlayer() {
        return false;
    }

    public boolean isItemCanGet() {
        return false;
    }

    public int isNpcQuest() {
        return -1;
    }

    public int getNpcType() {
        return 0;
    }

    public int getTypeNpc() {
        return 32000;
    }

    public boolean isNpcChar() {
        return false;
    }

    public String getCharName() {
        return "";
    }

    public int getLV() {
        return 0;
    }

    public void setIdSkill(int idSkill) {
    }

    public void setByMap(int type) {
    }

    public void setNewMonster(int isNewMonster) {
    }

    public void setIdThanThu(int id) {
    }

    public int getIdThanThu() {
        return -1;
    }

    public int getHp() {
        return 0;
    }

    public int getRealHp() {
        return 0;
    }

    public void setState(int state) {
    }

    public short getSpeed() {
        return (short) 0;
    }

    public byte getState() {
        return (byte) 0;
    }

    public short getDir() {
        return (short) 0;
    }

    public boolean ispetTool() {
        return false;
    }

    public short getOwnerID() {
        return (short) 0;
    }

    public void addEffbuff(int id, int x, int y, long timelive) {
    }

    public void petAttack(Vector target, byte type, short idSkill, int[] dame, int[] hpcon) {
    }

    public void setHP(int hp) {
    }

    public void addEffectSkillTime(int id, int x, int y, long timelive, boolean canmove, boolean canpaint, boolean canfight, int loop, byte WaitLoop, byte dyhorse) {
    }

    public void setHorse(int idhorse, int typehorse) {
    }

    public void setWeapon(int id, int type, int idImg) {
    }

    public void setPartWearing(short idHair, short idBody, short idLeg) {
    }

    public boolean isMainchar() {
        return false;
    }

    public void setEff(short[] arrID, byte[] arrTick) {
    }

    public short getyHorse() {
        return (short) 0;
    }

    public short getx() {
        return (short) 0;
    }

    public short gety() {
        return (short) 0;
    }
}
