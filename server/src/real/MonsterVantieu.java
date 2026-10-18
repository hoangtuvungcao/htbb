/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import io.Message;
import java.util.Hashtable;
import java.util.Vector;

public class MonsterVantieu extends Monster {
    public static Vector<MonsterVantieu> ALL_MONSTER_VANTIEU = new Vector<>();
    public static Hashtable<Short, MonsterVantieu> HAS_ALL_MONSTER_VANTIEU = new Hashtable<>();
    public static Hashtable<Integer, MonsterVantieu> HAS_MONSTER_VANTIEU = new Hashtable<>();
    public int charDbid = -1;
    public long timeFinish = System.currentTimeMillis();
    public String namemaster = "";
    public byte typeTieu = -1;
    public boolean isFinish = false;
    public int count = 0;

    public static MonsterVantieu getMonsterVantieu(int chardbid) {
        return HAS_MONSTER_VANTIEU.get(chardbid);
    }

    public static synchronized void addMonster(MonsterVantieu mons) {
        ALL_MONSTER_VANTIEU.add(mons);
        HAS_ALL_MONSTER_VANTIEU.put(mons.id, mons);
        HAS_MONSTER_VANTIEU.put(mons.charDbid, mons);
    }

    public static synchronized void removeMonster(MonsterVantieu mons) {
        ALL_MONSTER_VANTIEU.remove(mons);
        HAS_ALL_MONSTER_VANTIEU.remove(mons.id);
        HAS_MONSTER_VANTIEU.remove(mons.charDbid);
    }

    public MonsterVantieu(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
        this.moveDelay = 300L;
    }

    public void setTimeFinish(long time) {
        this.timeFinish = time;
    }

    @Override
    public void update() {
        if (this.isDead) {
            try {
                if (this.map != null) {
                    this.map.removeMonster(this.id, this.inCountry, 0);
                    Message m = new Message(90);
                    m.dos.writeShort(this.id);
                    m.dos.writeByte(this.cat);
                    this.map.sendAllPlayer(m, this.inCountry);
                }
            } catch (Exception var2) {
            }
        } else {
            if (System.currentTimeMillis() - this.timeOutPoinson >= this.tDelay * 1000 && this.tDelay > 0) {
                this.getXpReceive(this.poinson);
                this.hp = this.hp - this.poinson;
                this.totalTime = (byte)(this.totalTime - this.tDelay);
                this.timeOutPoinson = System.currentTimeMillis();
                if (this.totalTime == 0) {
                    this.tDelay = 0;
                    this.totalTime = 36;
                }

                if (this.hp <= 0) {
                    this.actorDie();
                    this.totalTime = 36;
                    this.tDelay = 0;
                }
            }

            if (this.beStune) {
                if (System.currentTimeMillis() > this.timeBeStune) {
                    this.beStune = false;
                }
            } else {
                if (this.target != null) {
                    if (this.target.getSession() == null) {
                        this.target = null;
                        return;
                    }

                    if (this.target.map.mapId != this.map.mapId || this.target.region != this.region || this.target.inCountry != this.inCountry) {
                        this.target = null;
                        return;
                    }

                    if (Math.abs(this.target.x - this.x) <= 80 * this.target.upSpeedNguaVanTieu
                        && Math.abs(this.target.y - this.y) <= 80 * this.target.upSpeedNguaVanTieu) {
                        this.move();
                    }
                }

                this.updateEffKham();
            }
        }
    }

    @Override
    public void attack() {
    }

    @Override
    public void move() {
        long now = System.currentTimeMillis();
        this.moved = false;
        if (now - this.lastTimeMove > this.moveDelay) {
            label29:
            if (this.target != null) {
                while (true) {
                    int xx;
                    int yy;
                    while (true) {
                        if (this.count >= 30) {
                            break label29;
                        }

                        this.count++;
                        xx = this.x;
                        yy = this.y;
                        if (Math.abs(this.x - this.target.x) <= 16) {
                            break;
                        }

                        if (this.x > this.target.x) {
                            if (!this.map.canMove(--xx, yy)) {
                                break;
                            }

                            this.x = xx;
                        } else {
                            if (!this.map.canMove(++xx, yy)) {
                                break;
                            }

                            this.x = xx;
                        }
                    }

                    if (Math.abs(this.y - this.target.y) <= 16) {
                        break;
                    }

                    if (this.y > this.target.y) {
                        if (!this.map.canMove(xx, --yy)) {
                            break;
                        }

                        this.y = yy;
                    } else {
                        if (!this.map.canMove(xx, ++yy)) {
                            break;
                        }

                        this.y = yy;
                    }
                }

                this.count = 30;
            }

            if (this.count >= 15) {
                this.doSendTonearChar();
                this.count = 0;
            }

            this.lastTimeMove = now;
            this.moved = true;
        }
    }

    public void doSendTonearChar() {
        if (!this.isFinish()) {
            Vector<Char> c = this.map.getAllPlayer(this.inCountry, this.region);

            for (int i = 0; i < c.size(); i++) {
                try {
                    Char p = c.get(i);
                    if (p.isBot == -1) {
                        p.sendMessage(p.writeActorPos(new Message(4), this));
                    }
                } catch (Exception var4) {
                    var4.printStackTrace();
                }
            }
        }
    }

    @Override
    public Vector<Item> dropItem() {
        String[] var10000 = new String[]{"trắng", "xanh", "đỏ", "hoàn mỹ", "vàng", "tím"};
        String[] type = new String[]{"cá nhân", "bang hội", "quốc gia"};
        byte[] colorItem = new byte[]{Item.COLOR_WHITE, Item.COLOR_BLUE, Item.COLOR_RED, Item.COLOR_GREEN, Item.COLOR_YELLOW, Item.COLOR_MAGENTA};
        Vector<Item> item = new Vector<>();
        Item it = new Item(Map.itemTemplates.get(5).get(611), false, 0, 0, 611);
        item.add(it);
        it.charSeal = this.namemaster + "-tiêu: " + type[this.typeTieu / 6];
        it.level = (short)this.level;
        it.colorName = colorItem[this.typeTieu % 6];
        it.typetieu = this.typeTieu;
        it.lock = 1;
        it = new Item(Map.itemTemplates.get(5).get(612), false, 0, 0, 612);
        item.add(it);
        it.charSeal = this.namemaster + "-tiêu " + type[this.typeTieu / 6];
        it.level = (short)this.level;
        it.colorName = colorItem[this.typeTieu % 6];
        it.lock = 1;
        it.typetieu = this.typeTieu;
        return item;
    }

    public static void saveAllMonster() {
        for (int i = 0; i < ALL_MONSTER_VANTIEU.size(); i++) {
            MonsterVantieu mons = ALL_MONSTER_VANTIEU.get(i);
            Database.instance.saveMonsterVantieu(mons);
        }
    }

    @Override
    public void actorDie() {
        try {
            this.isDead = true;
            this.bornTime = System.currentTimeMillis() + 1440000L;
            this.timeOutPoinson = 0L;
            this.poinson = 0;
            Message m = new Message(90);
            m.dos.writeShort(this.id);
            m.dos.writeByte(this.cat);
            if (this.map != null) {
                this.map.sendAllPlayer(m, this.inCountry);
            }
        } catch (Exception var2) {
        }
    }

    @Override
    public boolean isMonsterVantieu() {
        return true;
    }

    @Override
    public int getCharID() {
        return this.charDbid;
    }

    public void removeOutMap() {
        this.map.removeDynamicMonster(this, this.inCountry, this.region);
    }

    @Override
    public boolean isFinish() {
        return System.currentTimeMillis() - this.timeFinish > 0L && this.timeFinish > 0L || this.isDead || this.isFinish;
    }

    public String getThoiGianVantieu() {
        return "";
    }

    @Override
    public boolean canDropItem() {
        return false;
    }

    @Override
    public String getMasterName() {
        return this.namemaster;
    }

    @Override
    public byte getTypeTieu() {
        return this.typeTieu;
    }

    @Override
    public boolean isMyMonster(Char p) {
        return this.charDbid == p.charDBID;
    }

    @Override
    public String getNameMaster() {
        return this.namemaster;
    }

    @Override
    public int getColorTieu() {
        return this.typeTieu % 6;
    }
}
