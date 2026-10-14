package game.model;

import game.core.j2me.Graphics;
import java.util.Vector;

public abstract class Actor {
    public static final int CAT_CAN_NOT_FOCUS = 127;
    public static final byte CAT_EXPLOTION = 126;
    public static final byte CAT_GEM_ITEM = 6;
    public static final byte CAT_ITEM = 3;
    public static final byte CAT_ITEM_QUEST = 14;
    public static final byte CAT_MONSTER = 1;
    public static final byte CAT_MY_GROUND = 10;
    public static final byte CAT_MY_PET = 12;
    public static final byte CAT_MY_TREE = 11;
    public static final byte CAT_NPC = 2;
    public static final byte CAT_PARTY = 5;
    public static final byte CAT_PLAYER = 0;
    public static final byte CAT_POTION = 4;
    public static final byte CAT_SPECIAL_ITEM = 7;
    public static final byte CAT_TREE = 13;
    public boolean beStune;
    public byte catagory;
    public short centerX;
    public short centerY;
    public ChatPopup chat;
    public int congTrang;
    public boolean die;
    public short height;
    public int hoatdong;
    public String infoGround;
    public boolean isFocus;
    public boolean iskiller;
    public short killer;
    public int lienTram;
    public short poinson;
    public byte state;
    public long timeBeStune;
    public long timeOutPoinson;
    public byte typeLimit;
    public boolean wantDestroy;
    public short width;
    public short x;
    public short xFirst;
    public short y;
    public short yFirst;
    public byte nationID = -1;
    public byte inCountry = -1;
    public short ID = 32001;
    public short idClan = -1;
    public short IDParty = -1;
    public short IDMasterParty = -1;
    public byte pk = -1;
    public String nameUserTrade = "";
    public boolean isWearing = false;
    public boolean isMaininfo = false;
    public boolean isDie = false;
    public boolean canfocus = true;
    public int timeLive = -1;
    public Vector buff = new Vector();
    public short tDelay = 0;
    public long testmove = System.currentTimeMillis();

    public void actorDie() {
    }

    public void addDataeff(short s) {
    }

    public void addEffbuff(int i, int i2, int i3, long j) {
    }

    public void addEffectSkillTime(int i, int i2, int i3, long j, boolean z, boolean z2, boolean z3, int i4, byte b, byte b2) {
    }

    public boolean allwayPaint() {
        return false;
    }

    public int checkMagicShield(int i) {
        return 0;
    }

    public void createBuff(int i, int i2) {
    }

    public void dead() {
    }

    public int getDir() {
        return 0;
    }

    public int getHp() {
        return 0;
    }

    public int getIDTem() {
        return -1;
    }

    public int getIdThanThu() {
        return -1;
    }

    public int getNpcType() {
        return 0;
    }

    public int getRealHp() {
        return 0;
    }

    public int getSpeed() {
        return 0;
    }

    public int getTypeNpc() {
        return 32000;
    }

    public short getx() {
        return (short) 0;
    }

    public short gety() {
        return (short) 0;
    }

    public short getyHorse() {
        return (short) 0;
    }

    public boolean isDropItem() {
        return false;
    }

    public boolean isItemCanGet() {
        return false;
    }

    public boolean isMainchar() {
        return false;
    }

    public boolean isMineral() {
        return false;
    }

    public boolean isMonster() {
        return false;
    }

    public boolean isMypet() {
        return false;
    }

    public boolean isNPC() {
        return false;
    }

    public boolean isNpcChar() {
        return false;
    }

    public int isNpcQuest() {
        return -1;
    }

    public boolean isPaint() {
        return true;
    }

    public boolean isPlayer() {
        return false;
    }

    public boolean ispetTool() {
        return false;
    }

    public abstract void paint(Graphics graphics);

    public void paintActive(Graphics graphics, int i, int i2) {
    }

    public void petAttack(byte b, Vector vector, int[] iArr, short s) {
    }

    public void petAttack(Vector vector, byte b, short s, int[] iArr, int[] iArr2) {
    }

    public void removeDataSkillEff(int i) {
    }

    public void setByMap(int i) {
    }

    public void setEff(short[] sArr, byte[] bArr) {
    }

    public void setHP(int i) {
    }

    public void setHorse(int i, int i2) {
    }

    public void setIdSkill(int i) {
    }

    public void setIdThanThu(int i) {
    }

    public void setNewMonster(int i) {
    }

    public void setPartWearing(short s, short s2, short s3) {
    }

    public abstract void setPosTo(short s, short s2);

    public void setState(int i) {
    }

    public void setType(short s) {
    }

    public void setWeapon(int i, int i2, int i3) {
    }

    public void update() {
        ChatPopup chatPopup = this.chat;
        if (chatPopup != null) {
            chatPopup.setPos(this.x, (this.y - this.height) - 10);
            if (this.chat.setOut()) {
                this.chat = null;
            }
        }
    }

    public void addBuff(CharBuff charBuff) {
        int size = this.buff.size();
        for (int i = 0; i < size; i++) {
            if (((CharBuff) this.buff.elementAt(i)).type == charBuff.type) {
                this.buff.removeElementAt(i);
                break;
            }
        }
        this.buff.addElement(charBuff);
    }

    public void paintCorner(Graphics graphics, int i, int i2) {
        graphics.translate((-this.x) + i, (-this.y) + i2);
        paint(graphics);
    }

    public void paintName(Graphics graphics) {
        ChatPopup chatPopup = this.chat;
        if (chatPopup != null) {
            chatPopup.paintAnimal(graphics);
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public String getDisplayName() {
        return "Actor C=" + ((int) this.catagory) + " ID=" + ((int) this.ID);
    }

    public String getCharName() {
        return "";
    }

    public int getState() {
        return this.state;
    }
}
