package real;

import data.GemItem;
import io.Message;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Khu mo (mapId == 17) - noi nguoi choi trang bi cuoc (Char.itemAx) danh "quang"
 * (Monster id 85-89, xem MapNapTaiNguyen.loadMap dich +3 tu id>81) de lay nguyen
 * lieu tho (Soi bong, Quang sat, Ngoc tho, Go thuong tho, Da mem tho).
 *
 * TRUOC DAY lop nay CHI ton tai duoi dang bytecode da bien dich trong
 * lib/NQSH_5h.jar (khong co file .java, chua tung duoc dua vao dot tach
 * real/Map.java dang lam). Dua vao src/ ngay 14/09/2026 DUY NHAT de sua loi
 * duoi day - moi cho khac giu NGUYEN vi ban dich nguoc (build/nqsh-src/real/
 * MaterialMap.java), da doi chieu chu ky bang javap voi class that trong jar
 * truoc khi sua (xem [[project-kpah-nqsh-jar-khoa-ten-package]]).
 *
 * LOI DA SUA: addGemItem/getGem/removeGem/deletePotionAndItemOnGround ban cu
 * LUON dung `this.gemItem.get(0)` bat ke tham so `country` truyen vao (con
 * Map.java (lop cha, dang duoc bao tri) dung dung `country` that cho ca bon
 * ham nay). Hau qua: quang danh xong luon roi vao o quoc gia 0; nhung khi
 * nhat (ca tu dong lan bang tay) deu di qua Map.doGetGemItem ->
 * chiemGem(gem, player.inCountry) - ham NAY KHONG bi ghi de, dung dung
 * `player.inCountry` that. Nguoi choi thuoc phe 0 thi trung (chiemGem tim
 * dung o 0), nhung phe 1/2 (2 trong 3 phe) thi chiemGem tim o 1/2, KHONG
 * thay vat vua tao o o 0 -> tra ve false -> doGetGemItem am tham `return`,
 * khong cong gi vao tui, khong bao loi - dung khop trieu chung nguoi dung
 * bao "danh roi nguyen lieu nhung khong tu dong nhat".
 */
public class MaterialMap extends Map {

    Hashtable<Short, Monster> monsters = new Hashtable();
    Vector<Char> players = new Vector();

    public MaterialMap(int id, int idXaphu, int magic_physic, int mapload) {
        this.initAllMonsTer();
        this.mapId = id;
        this.mapIDLoadMap = mapload;
        short loadmap = (short) mapload;
        if (loadmap == -1) {
            loadmap = (short) this.mapId;
        }
        this.idXaphu = (byte) idXaphu;
        if (this.mapId != -1) {
            this.loadMap("map/map" + loadmap, magic_physic);
        }
        FileInputStream fis = null;
        DataInputStream dis = null;
        FileInputStream fisMap = null;
        DataInputStream disMap = null;
        try {
            fis = new FileInputStream(loadmap < 110 ? "cMap/t.type" : (loadmap > 200 ? "cMap/t_thanh.type" : "cMap/t_hang.type"));
            dis = new DataInputStream(fis);
            this.typeOfTile = new int[dis.available()];
            int i = 0;
            while (i < this.typeOfTile.length) {
                this.typeOfTile[i] = dis.read();
                ++i;
            }
            fisMap = new FileInputStream("cMap/" + loadmap);
            disMap = new DataInputStream(fisMap);
            this.w = disMap.read();
            this.h = disMap.read();
            this.map = new short[this.w * this.h];
            this.type = new int[this.w * this.h];
            i = 0;
            while (i < this.w * this.h) {
                try {
                    this.map[i] = (short) disMap.read();
                    if (this.map[i] != -1 && this.map[i] != 255 && this.map[i] != 254) {
                        this.type[i] = this.typeOfTile[this.map[i]];
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ++i;
            }
            this.loadTileMap(null, disMap, loadmap);
        } catch (Exception i) {
            // rong - giong ban goc
        }
        Char bot = new Char(null);
        bot.setInfoChar("Xaphu", -9, 2, 0, this, 1584, 864, (this.mapId + 2003) * -1, 25, 51, 77);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        this.players.add(bot);
        CharManager.instance.put(bot);
        this.gemItem.add(new Vector());
        this.gemItem.add(new Vector());
        this.gemItem.add(new Vector());
        this.doStartThreadUpdatePlayer();
        this.allPlayerMessages.add(new Vector());
        this.allPlayerMessages.add(new Vector());
        this.allPlayerMessages.add(new Vector());
        this.ALL_EFFECT_INMAP.add(new Vector());
        this.ALL_EFFECT_INMAP.add(new Vector());
        this.ALL_EFFECT_INMAP.add(new Vector());
        this.startLeafVilage();
        new Thread(this).start();
    }

    @Override
    protected Potion getPotion(short id, int country) {
        return null;
    }

    @Override
    public void loadMap(String filename, int magic_physic) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            DataInputStream dis = new DataInputStream(fis);
            int w = dis.readUnsignedByte();
            int h = dis.readUnsignedByte();
            this.mapWidth = w * 16;
            this.mapHeight = h * 16;
            Monster m0 = null;
            int totalMonster = 0;
            int i = 0;
            while (i < h) {
                int j = 0;
                while (j < w) {
                    int value = dis.readUnsignedByte();
                    if (value > 0 && value != 255) {
                        if (this.mapId == 17 && value > 81) {
                            value += 3;
                        }
                        m0 = new Monster(this, monsterTemplates.get(value), j * 16, i * 16, 1);
                        m0.level = m0.getMonsterTemplate().level;
                        m0.id = RealController.intance.idGen.getID(1, "new monster");
                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                        m0.he = m0.heKhiSinh(he[r.nextInt(5)]);
                        byte[] t = new byte[11];
                        t[1] = 1;
                        t[3] = 1;
                        t[5] = 1;
                        t[9] = 1;
                        m0.typeAttack = t[r.nextInt(10)];
                        this.monsters.put(m0.id, m0);
                        m0.magic_physic = (byte) magic_physic;
                        ++totalMonster;
                    }
                    ++j;
                }
                ++i;
            }
            dis.close();
            fis.close();
            System.out.println("TONG SO NG LIEU " + totalMonster);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Load map error: " + filename);
            e.printStackTrace();
        }
    }

    @Override
    public Vector<Char> getAllPlayer(int inCountry, int region) {
        return this.players;
    }

    @Override
    public void playerJoin(Char player) {
        this.players.add(player);
        player.map = this;
        player.mapID = this.mapId;
    }

    /** SUA 14/09/2026: dung dung `country` (giong Map.removeGem), khong con hardcode 0. */
    @Override
    protected void removeGem(GemItem pt, int country) {
        ((Vector) this.gemItem.get(country)).remove(pt);
    }

    @Override
    public void removePlayer(int country, Char p) {
        this.players.remove(p);
    }

    @Override
    public void removePLayer(Char player) {
        int i = 0;
        while (i < this.players.size()) {
            try {
                if (this.players.get(i).charname.equalsIgnoreCase(player.charname)) {
                    this.players.remove(i);
                    break;
                }
            } catch (Exception ignored) {
            }
            ++i;
        }
    }

    @Override
    public void playerExit(Char player) {
        Message m = new Message(8);
        try {
            m.dos.writeShort(player.id);
        } catch (IOException ignored) {
        }
        try {
            this.removePLayer(player);
            player.sendToNearPlayer(m);
        } catch (Exception e) {
            System.out.println("LOI REMOVE PLAYER KHOI MAP DUGEON");
        }
        m.cleanup();
    }

    @Override
    public void startLeafVilage() {
        new Thread() {
            @Override
            public void run() {
                while (!AdminHandler.isStopServer) {
                    try {
                        int count = 0;
                        Vector playerMessages = (Vector) MaterialMap.this.allPlayerMessages.get(1);
                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage) playerMessages.remove(0);
                            if (!pm.player.exit) {
                                MaterialMap.this.processMessage(pm.player, pm.message);
                            }
                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        synchronized (MaterialMap.this.LOCK) {
                            MaterialMap.this.LOCK.wait(timeDelay);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                while (!AdminHandler.isStopServer) {
                    try {
                        int count = 0;
                        Vector playerMessages = (Vector) MaterialMap.this.allPlayerMessages.get(2);
                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage) playerMessages.remove(0);
                            if (!pm.player.exit) {
                                MaterialMap.this.processMessage(pm.player, pm.message);
                            }
                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        synchronized (MaterialMap.this.LOCK1) {
                            MaterialMap.this.LOCK1.wait(timeDelay);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                while (!AdminHandler.isStopServer) {
                    try {
                        int count = 0;
                        Vector playerMessages = (Vector) MaterialMap.this.allPlayerMessages.get(0);
                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage) playerMessages.remove(0);
                            if (!pm.player.exit) {
                                MaterialMap.this.processMessage(pm.player, pm.message);
                            }
                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        synchronized (MaterialMap.this.LOCK2) {
                            MaterialMap.this.LOCK2.wait(timeDelay);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.currentThread().setName("MAP " + this.mapId);
                if (AdminHandler.isStopServer) {
                    this.map_run_state = 0;
                    break;
                }
                this.map_run_state = 0;
                long l1 = System.currentTimeMillis();
                if (l1 - this.lastTimeUpdateMap > DELAY_UPDATE_MAP) {
                    this.lastTimeUpdateMap = l1;
                    this.update();
                }
                this.map_run_state = 1;
            } catch (Exception e) {
                System.out.println("LOI TRONG HAM RUN MAP material ");
            }
            synchronized (this.LOCK) {
                try {
                    this.LOCK.wait(DELAY_UPDATE_MAP);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public Monster getMonster(short id, int country, int region) {
        return this.monsters.get(id);
    }

    @Override
    public void doStartThreadUpdatePlayer() {
    }

    @Override
    public void update() {
        if (this.players.size() > 1) {
            Collection<Monster> listmonster = this.monsters.values();
            for (Monster mt : listmonster) {
                try {
                    mt.update();
                    if (mt.target != null && mt.target.exit) {
                        mt.target = null;
                    }
                    if (this.players.size() <= 0 || !(isNewVersion ? !mt.isDead : !mt.isDead && mt.moved)) {
                        continue;
                    }
                    int i = 0;
                    while (i < this.players.size()) {
                        try {
                            Char p = this.players.get(i);
                            if (p.isBot == -1 && p.near(mt, p.rangeAddMonster[0])) {
                                if (isNewVersion) {
                                    if (!p.nearMons.contains(mt.id)) {
                                        p.nearMons.add(mt.id);
                                        p.sendMessage(p.writeActorPos(new Message(4), mt));
                                    }
                                } else {
                                    p.nearMons.add(mt.id);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        ++i;
                    }
                } catch (Exception e) {
                    System.out.println("UPDATE MAP MT KHOANG");
                }
            }
            int k = 0;
            while (k < this.players.size()) {
                try {
                    Char p = this.players.get(k);
                    if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                        this.players.remove(k);
                        CharManager.instance.remove(p);
                        continue;
                    }
                    if (!(p.map.equals(this) && p.mapID == this.mapId || p.isBot != -1)) {
                        this.players.remove(k);
                        continue;
                    }
                    p.update();
                    ++k;
                } catch (Exception e) {
                    break;
                }
            }
        }
    }

    @Override
    public void addPlayerMessage(Char p, Message message) {
        ((Vector) this.allPlayerMessages.get(p.myCountry)).add(new PlayerMessage(p, message));
    }

    @Override
    protected void doAttackMonster(Char p, Message message) throws IOException {
        if (p.countHit() || p.freeze()) {
            return;
        }
        if (p.isHoangSo() || p.isHoangLoan()) {
            return;
        }
        if (p.itemAx == null) {
            return;
        }
        if (p.hp <= 0) {
            p.actorDie();
            return;
        }
        if (!p.checkDurableWeapone()) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Không thể đánh khi vũ khí bị hỏng. Hãy đến Thợ rèn để sửa lại.", ""));
            Message m = new Message(104);
            try {
                m.dos.writeByte(p.typeConfig);
                m.dos.writeByte(0);
                p.sendMessage(m);
            } catch (Exception ignored) {
            }
            return;
        }
        p.downDurableWeapone();
        DataInputStream dis = message.dis;
        Monster mt = this.getMonster(dis.readShort(), p.inCountry, p.region);
        byte skill = 0;
        int effect = 0;
        int ahp;
        int buffAttack = -1;
        if (buffAttack > 0) {
            return;
        }
        if (buffAttack != -1 && buffAttack == 0 && p.skill[5] + p.addMoreLevelSkill[5] == 0) {
            return;
        }
        if (mt == null || mt.isDead) {
            if (mt != null) {
                Map.onMosterDie(p, mt, skill, 1, (byte) effect, (byte) 0);
            }
            return;
        }
        if (!Map.inRangeActor(p, mt, 33)) {
            p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể đánh khi đứng quá xa"));
            return;
        }
        if (mt.map.mapId != p.mapID) {
            return;
        }
        byte _type = skill;
        int _level = p.skill[_type] + p.addMoreLevelSkill[_type];
        if (_level <= 0) {
            _level = p.addMoreLevelSkill[_type];
        }
        long now;
        if ((now = System.currentTimeMillis()) - p.timeLastUseSkills[_type] < (long) (CharManager.SKILL_COOLDOWN[p.charClass][_type][_level] * 100)) {
            return;
        }
        p.timeLastUseSkills[_type] = now;
        buffAttack = p.getBuffEffAttack();
        if (mt.resistThroughArmor()) {
            buffAttack = -1;
        }
        int damage = p.attackDam(mt, _type, _level, buffAttack);
        boolean critSv = p.havecrit();
        if (critSv) {
            damage *= 2;
            effect = 2;
            if (p.petUsing != null) {
                long pcLienKich = p.petUsing.getLienKich();
                damage = (int) ((long) damage + (long) damage * pcLienKich / 100L);
            }
        }
        if (damage > 20) {
            damage = 20;
        }
        short mplost;
        if (p.mp + p.percentBuff[1] < (mplost = CharManager.SKILL_MP[p.charClass][_type][_level])) {
            return;
        }
        p.mp -= mplost;
        if (p.mp <= 0) {
            p.mp = 0;
        }
        ahp = damage;
        mt.hp -= damage;
        Message m = null;
        if (mt.hp > 0) {
            if (mt.target == null) {
                mt.target = p;
            }
            m = new Message(9);
            m.dos.writeShort(p.id);
            m.dos.writeShort(mt.id);
            m.dos.writeByte(0);
            m.dos.writeInt(ahp);
            m.dos.writeInt(mt.hp);
            m.dos.writeByte(effect);
            m.dos.writeByte(1);
            m.dos.writeByte(buffAttack);
            m.dos.writeByte(1);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            p.buffSkillKham(mt);
        } else {
            GemItem gemItem;
            Vector<GemItem> droplist = new Vector<GemItem>();
            if (mt.isMaterialMons() && (gemItem = this.doCreateGemItemMaterial(mt.idTemplate)) != null) {
                gemItem.cat = (byte) 6;
                gemItem.x = mt.x + 5;
                gemItem.y = mt.y + 10;
                gemItem.id = this.getIDITEM();
                gemItem.time_drop = System.currentTimeMillis();
                gemItem.belongUser = p.charDBID;
                // SUA 14/09/2026: dung `p.inCountry` (phe THAT cua nguoi danh), khong
                // dung `mt.inCountry` - moi quang trong khu mo deu duoc tao voi inCountry
                // HARDCODE = 1 (xem loadMap() o tren: "new Monster(this, ..., 1)"), khong
                // lien quan gi den phe cua nguoi dang danh no. Neu van dung mt.inCountry,
                // vat pham luon roi vao dung MOT o (phe 1) bat ke ai giet - lan sua truoc
                // (dong bo addGemItem/getGem/removeGem theo `country`) chi doi tu "luon ket
                // o o 0" sang "luon ket o o 1", VAN sai voi nguoi choi khong thuoc phe 1 vi
                // chiemGem() (Map.java, khong bi ghi de) lai dung dung p.inCountry THAT khi
                // nhat. Dung p.inCountry o day thi luc tao va luc nhat luon khop nhau,
                // dung voi MOI phe.
                this.addGemItem(gemItem, p.inCountry);
                droplist.add(gemItem);
                if (p.autoGetItem == 1) {
                    p.idGem.add(gemItem.id);
                }
            }
            try {
                m = new Message(17);
                m.dos.writeShort(p.id);
                m.dos.writeShort(mt.id);
                m.dos.writeByte(0);
                m.dos.writeInt(ahp);
                m.dos.writeByte(effect);
                m.dos.writeByte(droplist.size());
                if (droplist.size() > 0) {
                    for (Actor actor : droplist) {
                        Map.writeActorPos(m, actor, (byte) p.getSession().isOldVersion);
                    }
                }
                m.dos.writeByte(1);
                m.dos.writeByte(buffAttack);
                m.dos.writeByte(1);
                p.sendMessage(m);
                p.sendToNearPlayer(m);
                if (p.receiveQuest && QuestTemplate.QUEST_TYPE[p.questID - 1] == 0) {
                    p.checkFinsishQuest(mt.getType(), -1, -1);
                }
            } catch (Exception e) {
                System.out.println("loi gui thong tin monsterdie ");
            }
        }
        if (mt.hp <= 0) {
            mt.bornTime = System.currentTimeMillis() + 10000L;
            mt.isDead = true;
            mt.target = null;
        }
        m.cleanup();
    }

    public GemItem doCreateGemItemMaterial(int idTemplateMonster) {
        int[][] template = new int[][]{{81, 116}, {67, 102}, {88, 123}, {95, 130}, {74, 109}};
        int index = 0;
        if (r.nextInt(1000) == 1) {
            index = 1;
        }
        return new GemItem(template[idTemplateMonster - 85][index]);
    }

    /** SUA 14/09/2026: dung dung `country` (giong Map.addGemItem) - xem chu thich dau file. */
    @Override
    protected void addGemItem(GemItem pt, int country) {
        ((Vector) this.gemItem.get(country)).add(pt);
    }

    /** SUA 14/09/2026: dung dung `country` (giong Map.getGem), khong con hardcode 0. */
    @Override
    protected GemItem getGem(short id, int country) {
        Vector gemItem = (Vector) this.gemItem.get(country);
        int i = 0;
        while (i < gemItem.size()) {
            try {
                if (((GemItem) gemItem.get(i)).id == id) {
                    return (GemItem) gemItem.get(i);
                }
            } catch (Exception e) {
                break;
            }
            ++i;
        }
        return null;
    }

    /** SUA 14/09/2026: dung dung `country` (giong Map.deletePotionAndItemOnGround) - vong lam
     * moi dinh ky (MapVongLap) goi lan luot voi country=0,1,2 cho MOI map; ban cu hardcode 0
     * nen o 1/2 khong bao gio duoc don, va sau khi sua addGemItem/getGem thi vat o o 1/2 se
     * khong con duoc don rac (thoi han 30 giay) neu khong sua luon ham nay. */
    @Override
    public void deletePotionAndItemOnGround(int country) {
        long now = System.currentTimeMillis();
        int size = ((Vector) this.gemItem.get(country)).size() - 1;
        int i = size;
        while (i >= 0) {
            try {
                GemItem item = (GemItem) ((Vector) this.gemItem.get(country)).elementAt(i);
                if (now - item.time_drop > 30000L) {
                    ((Vector) this.gemItem.get(country)).remove(item);
                }
            } catch (Exception ignored) {
            }
            --i;
        }
    }

    @Override
    public void doAttackMultiTarget(Char p, Message message) {
    }

    @Override
    public boolean isPublicMap() {
        return true;
    }

    @Override
    public boolean isMapTrain() {
        return false;
    }
}
