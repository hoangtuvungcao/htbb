/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import data.GemItem;
import io.Message;
import java.io.IOException;
import java.util.Vector;

public class CuongThi extends Monster {
    int rcvXP = 0;
    int stMapID = 0;
    public Region rg = null;
    long timeExist = System.currentTimeMillis();
    static int[] idng = new int[]{
        68,
        69,
        70,
        71,
        72,
        68,
        68,
        68,
        68,
        68,
        68,
        68,
        70,
        70,
        70,
        70,
        70,
        70,
        70,
        70,
        71,
        68,
        68,
        69,
        69,
        69,
        69,
        69,
        69,
        69,
        69,
        69,
        75,
        76,
        77,
        78,
        79,
        75,
        75,
        75,
        75,
        75,
        75,
        75,
        75,
        75,
        75,
        76,
        76,
        76,
        76,
        76,
        77,
        76,
        76,
        76,
        76,
        76,
        76,
        77,
        77,
        77,
        77,
        78,
        82,
        83,
        84,
        85,
        86,
        82,
        82,
        82,
        82,
        82,
        82,
        82,
        82,
        82,
        82,
        83,
        83,
        83,
        83,
        83,
        84,
        83,
        83,
        83,
        83,
        83,
        83,
        83,
        84,
        84,
        84,
        85,
        89,
        90,
        91,
        92,
        93,
        89,
        89,
        89,
        89,
        89,
        89,
        89,
        89,
        89,
        89,
        90,
        90,
        90,
        90,
        90,
        91,
        90,
        90,
        91,
        91,
        91,
        91,
        91,
        91,
        91,
        91,
        92,
        96,
        97,
        98,
        99,
        100,
        96,
        96,
        96,
        96,
        96,
        96,
        96,
        96,
        96,
        96,
        97,
        97,
        97,
        97,
        97,
        98,
        98,
        98,
        98,
        98,
        98,
        98,
        99,
        98,
        99,
        98,
        98
    };
    static int[] idGem = new int[]{62, 62, 62, 62, 62, 62, 62, 62, 62, 63, 63, 63, 63, 63, 63, 63, 64, 63, 62, 62, 62, 62, 63, 64, 65};

    public CuongThi(Map mapLiveIn, MonsterTemplate template, int x, int y, byte country) {
        super(mapLiveIn, template, x, y, country);
        this.attackDelay = 7000L;
    }

    public void setInfo(int level, int maxHp, int rcvXP) {
        this.level = level;
        this.maxhp = maxHp;
        this.hp = maxHp;
        this.rcvXP = rcvXP;
        this.stMapID = this.map.getMapLoad(this.map.mapId);
        this.dmove = 96;
        this.attackDelay = 7000L;
    }

    @Override
    public int getXpReceive(int hpLost) {
        int xpGet = 0;
        if (this.hp <= 0 || hpLost <= 0) {
            return 0;
        } else if (hpLost >= this.maxhp && this.hp == this.maxhp) {
            return this.rcvXP;
        } else if (hpLost > this.hp && this.hp > 0) {
            return this.xp;
        } else {
            int percentHp = hpLost * 100 / this.hp;
            if (hpLost > 0 && percentHp <= 0) {
                percentHp = 1;
            }

            xpGet = this.xp * percentHp / 100;
            if (this.xp > 0 && xpGet < 0) {
                xpGet = 1;
            }

            this.xp -= xpGet;
            return xpGet;
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void update() {
        if (this.isDead) {
            long now = System.currentTimeMillis();
            if (now > this.bornTime) {
                this.isDead = false;
                this.timeOutPoinson = 0L;
                this.poinson = 0;
                this.hp = this.maxhp;
                this.xp = this.rcvXP;
                this.tDelay = 0;
                this.target = null;
                this.timeExist = System.currentTimeMillis();
                Database.instance.saveOrtherLog("", "cuong thi", "xuat hien lai", "breborn");
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
                if (this.target == null) {
                    if (!this.beStune && !this.freeze()) {
                        this.move();
                    }
                } else {
                    if (this.target.map != this.map) {
                        this.target = null;
                        return;
                    }

                    if (this.target.map.mapId != this.map.mapId || this.target.inCountry != this.inCountry || this.target.region != this.region) {
                        this.target = null;
                        return;
                    }

                    if (this.target.hp <= 0) {
                        this.target = null;
                        return;
                    }

                    if (this.target.hp > 0 && Math.abs(this.target.x - this.x) <= 120 && Math.abs(this.target.y - this.y) <= 120) {
                        this.attack();
                    } else {
                        this.move();
                    }
                }
            }
        }
    }

    @Override
    public void attack() {
        long now = System.currentTimeMillis();
        if (now - this.lastTimeAttack > this.attackDelay) {
            this.lastTimeAttack = now;

            try {
                Message m = new Message(83);
                m.dos.writeShort(this.id);
                m.dos.writeByte(Map.r.nextInt(2));
                int ahp = this.attackDam(this.target);
                ahp = this.target.checkHapthuSatThuong(ahp, this);
                ahp = this.target.checkGiamSatThuong(ahp);
                ahp = this.target.checkPassAttack(this, ahp);
                boolean ismiss = this.attackMiss(this.target);
                if (ismiss) {
                    ahp = 0;
                }

                int freezz = Map.r.nextInt(100);
                if (freezz <= 10 && Map.r.nextInt(100) > this.target.khamKhangBang()) {
                    this.target.khamEff[3] = System.currentTimeMillis() / 1000L;
                }

                int rd = Map.r.nextInt(2);
                Vector<Char> charid = new Vector<>();
                Vector<Integer> damAttack = new Vector<>();
                charid.add(this.target);
                damAttack.add(ahp);
                boolean var21 = true;
                if (var21) {
                    for (int i = 0; i < this.target.nearChars.size(); i++) {
                        Char c = this.map.getPlayerByID(this.target.nearChars.get(i));
                        if (c != null && c.hp > 0 && this.near(c, 220) && c.id != this.target.id) {
                            charid.add(c);
                            int dam = this.attackDam(c);
                            dam = this.target.checkHapthuSatThuong(dam, this);
                            dam = this.target.checkGiamSatThuong(dam);
                            dam = this.target.checkPassAttack(this, dam);
                            if (dam < 50) {
                                dam = 20 + Map.r.nextInt(30);
                            }

                            boolean miss = this.attackMiss(c);
                            if (miss) {
                                dam = 0;
                            }

                            damAttack.add(dam);
                            if (freezz <= 10 && Map.r.nextInt(100) > c.khamKhangBang()) {
                                c.khamEff[3] = System.currentTimeMillis() / 1000L;
                            }
                        }
                    }
                }

                m.dos.writeByte(charid.size());

                for (int ix = 0; ix < charid.size(); ix++) {
                    Char c = charid.get(ix);
                    m.dos.writeShort(c.id);
                    m.dos.writeInt(damAttack.get(ix));
                    int hp = c.hp - damAttack.get(ix);
                    if (hp < 0) {
                        hp = 0;
                    }

                    m.dos.writeInt(hp);
                    c.hp = hp;
                    Database.instance
                        .saveOrtherLog(
                            "",
                            c.getName(),
                            c.hp
                                + "_"
                                + damAttack.get(ix)
                                + "_"
                                + this.getMonsterTemplate().name
                                + "_"
                                + Map.getNameMap(this.map.mapId)
                                + "_"
                                + this.region
                                + "_"
                                + c.region,
                            "dam"
                        );
                }

                m.dos.writeByte(-1);

                for (int ix = 0; ix < charid.size(); ix++) {
                    Char c = charid.get(ix);
                    c.sendMessage(m);
                    if (c.hp <= 0) {
                        c.desTroy();

                        try {
                            long xp = c.lvDetail.getExp();
                            xp -= c.lvDetail.getXPLost(c.killer, c);
                            int currentlv = c.lvDetail.lv;
                            c.lvDetail.setExp(xp, c.oldLv, c.getName(), "cuongthi");
                            if (c.lvDetail.lv <= 1) {
                                c.lvDetail.lv = 1;
                                c.lvDetail.percent = 0;
                            }

                            if (currentlv > c.lvDetail.lv) {
                                c.resetKNDownlv();
                                c.lvDetail.resetExp2Lv(currentlv, c.killer);
                                if (c.killer > 0) {
                                    Database.instance.saveOrtherLog("", c.getName(), "tut level do dang trong che do ds " + c.killer, "downlv");
                                }
                            }

                            c.actorDie();
                            c.calculateAttrib();
                            c.doSendProperties();
                        } catch (Exception var16) {
                            var16.printStackTrace();
                        }
                    }

                    for (int j = 0; j < c.nearChars.size(); j++) {
                        Char p = this.map.getPlayerByID(c.nearChars.get(j));
                        if (p != null && !charid.contains(p)) {
                            p.sendMessage(m);
                            if (c.khamEff[3] > 0L) {
                                p.sendMessage(p.sendEffKham(c));
                            }
                        }
                    }

                    for (int kk = charid.size() - 1; kk >= 0; kk--) {
                        Char p = charid.get(kk);

                        for (int jx = 0; jx < charid.size(); jx++) {
                            Char pp = charid.get(jx);
                            if (p.khamEff[3] > 0L) {
                                pp.sendMessage(p.sendEffKham(p));
                            }
                        }
                    }
                }

                m.cleanup();
            } catch (IOException var17) {
            }
        }
    }

    @Override
    public void actorDie() {
        try {
            this.isDead = true;
            long timehs = 60L;
            this.bornTime = System.currentTimeMillis() + timehs * 60L * 60L * 1000L;
            this.timeOutPoinson = 0L;
            this.poinson = 0;
            Message m = new Message(90);
            m.dos.writeShort(this.id);
            m.dos.writeByte(this.cat);
            if (this.map != null) {
                this.map.sendAllPlayer(m, this.inCountry);
            }

            this.map.removeDynamicMonster(this, this.inCountry, this.region);
            if (this.rg != null) {
                this.rg.total_cuongthi--;
                if (this.rg.total_cuongthi < 0) {
                    this.rg.total_cuongthi = 0;
                }
            } else {
                this.map.total_cuongthi--;
                if (this.map.total_cuongthi < 0) {
                    this.map.total_cuongthi = 0;
                }
            }
        } catch (Exception var4) {
        }

        Map.removeBossLocation(0);
    }

    @Override
    public boolean isCuongThi() {
        return true;
    }

    @Override
    public GemItem dropGemItem() {
        return null;
    }

    @Override
    public Vector<GemItem> dropListGemItem() {
        Vector<GemItem> gem = new Vector<>();
        if (Map.r.nextInt(100) < 1) {
            int soluong = 1;

            for (int i = 0; i < soluong; i++) {
                int lv = Map.r.nextInt(3) + 2;
                short[] id = GemTemplate.ID_MATERIAL_HIGHT[lv];
                GemItem g = new GemItem(id[Map.r.nextInt(id.length)]);
                gem.add(g);
                g.islock = Map.r.nextInt(2) == 1;
            }
        } else {
            int[] idbot = new int[]{246, 247, 248, 249};
            int[] slmin = new int[]{50, 5, 1, 1};
            int[] slrandom = new int[]{51, 21, 4, 1};
            int rd = Map.r.nextInt(idbot.length);
            int sl = slmin[rd];
            if (slmin[rd] != slrandom[rd]) {
                sl += Map.r.nextInt(slrandom[rd]);
            }

            for (int i = 0; i < sl; i++) {
                GemItem g = new GemItem(idbot[rd]);
                gem.add(g);
                g.islock = Map.r.nextInt(2) == 1;
            }
        }

        return gem;
    }

    @Override
    public Vector<Potion> dropPotion(Char p) {
        return new Vector<>();
    }

    @Override
    public void setTimeReBornInEvent(long time) {
        long timehs = 60L;
        this.bornTime = System.currentTimeMillis() + timehs * 60L * 60L * 1000L;
        this.isDead = true;
        this.map.removeDynamicMonster(this, this.inCountry, this.idregion);
    }

    @Override
    public void setTimeReBorn() {
        long timehs = 60L;
        Database.instance.saveOrtherLog("", "Nguoi tuyet", "chet", "bdie");
        this.bornTime = System.currentTimeMillis() + timehs * 60L * 60L * 1000L;
        this.isDead = true;
    }

    @Override
    public int getTimeReborn() {
        return -1;
    }

    @Override
    public int getCharID() {
        return 0;
    }
}
