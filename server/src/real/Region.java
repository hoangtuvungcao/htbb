/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  data.Database
 *  io.Message
 *  real.Actor
 *  real.AdminHandler
 *  real.Boss
 *  real.Char
 *  real.CharManager
 *  real.EffectBuff
 *  real.LiveActor
 *  real.Map
 *  real.MessageCreator
 *  real.Monster
 *  real.Monster_Clone
 *  real.PlayerMessage
 *  real.QuestTemplate
 *  real.RealController
 *  server.TeamServer
 *  util.Logger
 */
package real;

import data.Database;
import io.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;
import server.TeamServer;
import util.Logger;

public class Region {
    public Vector<Hashtable<Short, Monster>> monsters = new Vector();
    Vector<Vector<PlayerMessage>> allPlayerMessages = new Vector();
    public Vector<Monster> monstertem = new Vector();
    public byte total_cuongthi = 0;
    private Object LOCK = new Object();
    private Object LOCK2 = new Object();
    private Object LOCK1 = new Object();
    private long lastTimeUpdateMap;
    private long lastTimeUpdateMap1;
    private long lastTimeUpdateMap2;
    private int map_run_state;
    private int map_run_state1;
    private int map_run_state2;
    short idRegion = 0;
    boolean running = true;
    Map map;
    long timeExist;
    private Vector<Vector<Char>> allPlayers = new Vector();
    private Vector<Vector<EffectBuff>> EFFECT_AUTO = new Vector();
    public Vector<Vector<Monster>> tempMonster = new Vector();
    public Vector<Vector<Monster>> tempRemoveMonster = new Vector();
    private Vector<Hashtable<Short, Monster>> temp_monsters = new Vector();
    boolean isSkelonton = false;
    static long timeDropCake = System.currentTimeMillis() + 0x6DDD00L;
    public static short[] MAX_RANGE_CHAR = new short[]{120, 120, 140, 120, 160};

    public Region(Map m, int id) {
        this.map = m;
        this.timeExist = System.currentTimeMillis();
        this.idRegion = (short)id;
        // O day nguyen ban co mot luong nua, nhung CFR khong dich noi than ham run()
        // nen sinh ra "throw new IllegalStateException(\"Decompilation failed\")". Moi
        // Region tao luong do roi luong chet ngay: 610 stack trace moi lan khoi dong
        // (58.850 dong trong mot tep log cu), va 610 thread bi tao ra vo ich.
        //
        // Da bo han (roi tu day). Luong do tung lam gi: no la ban sao cua vong cap nhat
        // trong startSandVilage(), nhung cho PHE 1 - tuc la noi duy nhat goi
        // Region.update() (xu ly boss va allPlayers.get(1)). Bo no di lam update() thanh
        // ma chet - HAU QUA THAT: nguoi choi phe Hac Ho dung tren map co Region (nregion>0,
        // vd "Dong Bach Hac"/"Canh Rung Dong") khong bao gio thay quai cap nhat vi tri/
        // xuat hien, vi khong con ai goi update() ca (lo #3 loi.xlsx, xac nhan "luon xay
        // ra" - dung voi mot bug cau truc, khong phai theo trang thai chien tranh).
        //
        // DA PHUC HOI o duoi: startVilage1() (them moi, cuoi ham dung) - dung dung mau
        // startSandVilage(), LOCK1/lastTimeUpdateMap1/map_run_state1 (3 truong nay tu
        // truoc gio khai bao san nhung chua ai dung), goi update(). Dat SAU cac dong
        // add() ben duoi (khac ban goc - luong cu start() truoc ca khi allPlayers co
        // phan tu, mot loi khac cua rieng no).
        //
        // updateWind() (PHE 2 - Huyen Vu) VAN la ma chet, CO CHU Y chua phuc hoi: cac
        // Vector (allPlayers, monsters, tempMonster...) trong ham dung nay chi add() 2
        // phan tu (chi so 0,1) - goi updateWind() se nem IndexOutOfBoundsException ngay
        // vi no doc chi so 2. Phe Huyen Vu cung khong chon duoc qua menu doi phe
        // (MapDoMenu.java ~4039 chi dao 0/1) nen co ve la noi dung da ngung dung tu lau,
        // khong phai lo #3 dang hoi.
        this.allPlayers.add(new Vector());
        this.allPlayers.add(new Vector());
        this.allPlayerMessages.add(new Vector());
        this.allPlayerMessages.add(new Vector());
        this.monsters.add(new Hashtable());
        this.monsters.add(new Hashtable());
        this.temp_monsters.add(new Hashtable());
        this.temp_monsters.add(new Hashtable());
        this.tempMonster.add(new Vector());
        this.tempMonster.add(new Vector());
        this.tempRemoveMonster.add(new Vector());
        this.tempRemoveMonster.add(new Vector());
        this.EFFECT_AUTO.add(new Vector());
        this.EFFECT_AUTO.add(new Vector());
        this.startLeafVilage();
        this.startSandVilage();
        this.startVilage1();
    }

    // ---- Vong lap cua khu: chay tren pool CHUNG (com.htbb.runtime.VongLapChung) ----
    // Truoc day moi Region tao 4 thread rieng (2 vong tick + 2 vong goi tin, ~2.440 thread cho
    // 610 khu khi server rong, moi thread thuc day 3-5 lan/giay du khong co ai). Nay moi vong la
    // mot Nhip/Lan tren pool chung - than vong (updateSand/update/processMessage) GIU NGUYEN,
    // chu ky tick GIU NGUYEN Map.DELAY_UPDATE_MAP khi khu co nguoi choi that. Khu KHONG co nguoi
    // choi that (chi NPC/bot, isBot != -1) tick moi VongLapChung.CHU_KY_RANH_MS: luc do than tick
    // chi lam viec hen gio (quai hoi sinh theo dong ho bornTime, xoa do roi theo dong ho, them quai
    // dong) - nguoi choi vao khu (playerJoin/addPlayer) thi danhThuc() chay tick NGAY.
    // Lop con trong lib/htbb-core.jar (RegionLoiDai, RegionNuiChauBau, RegionMapMoba) ghi de
    // startSandVilage/startLeafVilage bang thread rieng cua no - khong doi.
    private com.htbb.runtime.VongLapChung.Nhip nhip0;
    private com.htbb.runtime.VongLapChung.Nhip nhip1;
    private com.htbb.runtime.VongLapChung.Lan lan0;
    private com.htbb.runtime.VongLapChung.Lan lan1;

    /** Khu co nguoi choi that (khong tinh NPC/bot) o phe nay khong. */
    boolean coNguoiThat(int phe) {
        try {
            Vector<Char> ds = this.allPlayers.get(phe);
            for (int i = ds.size() - 1; i >= 0; i--) {
                Char c = ds.get(i);
                if (c != null && c.isBot == -1) {
                    return true;
                }
            }
        } catch (Exception e) {
            return true; // Vector vua co rut phan tu - coi nhu co nguoi, chay du chu ky
        }
        return false;
    }

    /** Than vong goi tin (giong vong cu): don hang doi, toi da 500 goi moi luot. */
    private void donGoiTin(int phe, com.htbb.runtime.VongLapChung.Lan lan) {
        Vector<PlayerMessage> playerMessages = this.allPlayerMessages.get(phe);
        try {
            int count = 0;
            while (playerMessages.size() > 0) {
                PlayerMessage pm = (PlayerMessage) playerMessages.remove(0);
                if (!pm.player.exit) {
                    this.map.processMessage(pm.player, pm.message);
                }
                if (++count == 500) {
                    break; // vong cu: ngu 5 ms moi 500 goi; nay nhuong luong roi chay tiep
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (playerMessages.size() > 0 && lan != null) {
            lan.kich();
        }
    }

    /** Luoi an toan: neu hang doi goi tin con viec (vd ai do them thang vao Vector) thi kich. */
    private void kichNeuCoGoi() {
        if (this.lan0 != null && this.allPlayerMessages.get(0).size() > 0) {
            this.lan0.kich();
        }
        if (this.lan1 != null && this.allPlayerMessages.get(1).size() > 0) {
            this.lan1.kich();
        }
    }

    public void startLeafVilage() {
        // Vong goi tin PHE 1 (truoc: thread "MAP SEND MONSTER 2 player<id>").
        final com.htbb.runtime.VongLapChung.Lan[] o = new com.htbb.runtime.VongLapChung.Lan[1];
        o[0] = new com.htbb.runtime.VongLapChung.Lan(new Runnable() {
            @Override
            public void run() {
                Region.this.donGoiTin(1, o[0]);
            }
        });
        this.lan1 = o[0];
    }

    public void startWindVilage() {
    }

    public void startSandVilage() {
        // Tick PHE 0 (updateSand) + vong goi tin PHE 0.
        this.nhip0 = new com.htbb.runtime.VongLapChung.Nhip("khu " + this.map.mapId + "/" + this.idRegion + " phe0",
                Map.DELAY_UPDATE_MAP, new Runnable() {
            @Override
            public void run() {
                if (!Region.this.running) {
                    Region.this.dungVongLap();
                    return;
                }
                Region.this.map_run_state = 0;
                try {
                    Region.this.updateSand();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("LOI TRONG HAM RUN MAP " + e.toString());
                }
                Region.this.lastTimeUpdateMap = System.currentTimeMillis();
                Region.this.map_run_state = 1;
                Region.this.kichNeuCoGoi();
            }
        }, new com.htbb.runtime.VongLapChung.CoNguoi() {
            @Override
            public boolean coNguoiThat() {
                return Region.this.coNguoiThat(0);
            }
        }).batDau(Map.DELAY_UPDATE_MAP);
        final com.htbb.runtime.VongLapChung.Lan[] o = new com.htbb.runtime.VongLapChung.Lan[1];
        o[0] = new com.htbb.runtime.VongLapChung.Lan(new Runnable() {
            @Override
            public void run() {
                Region.this.donGoiTin(0, o[0]);
            }
        });
        this.lan0 = o[0];
    }

    /**
     * lo #3 loi.xlsx ("quai He Hac Ho khong hien thi"): day la tick rieng cho PHE 1
     * (Hac Ho), theo dung mau startSandVilage() (PHE 0) - xem chu thich o constructor
     * giai thich vi sao update() truoc day la ma chet (khong ai goi). Dung
     * LOCK1/lastTimeUpdateMap1/map_run_state1 - 3 truong nay da khai bao san tu truoc,
     * chi chua co ai dung toi.
     */
    public void startVilage1() {
        this.nhip1 = new com.htbb.runtime.VongLapChung.Nhip("khu " + this.map.mapId + "/" + this.idRegion + " phe1",
                Map.DELAY_UPDATE_MAP, new Runnable() {
            @Override
            public void run() {
                if (!Region.this.running) {
                    Region.this.dungVongLap();
                    return;
                }
                Region.this.map_run_state1 = 0;
                try {
                    Region.this.update();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("LOI TRONG HAM RUN MAP (PHE 1) " + e.toString());
                }
                Region.this.lastTimeUpdateMap1 = System.currentTimeMillis();
                Region.this.map_run_state1 = 1;
            }
        }, new com.htbb.runtime.VongLapChung.CoNguoi() {
            @Override
            public boolean coNguoiThat() {
                return Region.this.coNguoiThat(1);
            }
        }).batDau(Map.DELAY_UPDATE_MAP);
    }

    /** Nguoi choi vao khu: tick dang o chu ky ranh thi chay ngay. Map.playerJoin/addPlayer cung goi. */
    public void danhThucNhip() {
        if (this.nhip0 != null) {
            this.nhip0.danhThuc();
        }
        if (this.nhip1 != null) {
            this.nhip1.danhThuc();
        }
    }

    /** Khu bi go khoi ban do (MapLoiDai/MapChauBau.removeRegion -> stop()): tra lai pool. */
    void dungVongLap() {
        if (this.nhip0 != null) {
            this.nhip0.dung();
        }
        if (this.nhip1 != null) {
            this.nhip1.dung();
        }
        if (this.lan0 != null) {
            this.lan0.dung();
        }
        if (this.lan1 != null) {
            this.lan1.dung();
        }
    }

    public void update() {
        Vector<Char> players = this.allPlayers.get(1);
        if (players.size() > 0) {
            if (Map.bossCopy.size() > 0) {
                int i = 0;
                while (i < Map.bossCopy.size()) {
                    try {
                        Boss bo = (Boss)Map.bossCopy.get(i);
                        if (bo.hp <= 0 || bo.isDead) {
                            bo.map.removeMonster((int)bo.id, (int)bo.inCountry, bo.region);
                            Map.bossCopy.remove(i);
                            try {
                                Message m = new Message(90);
                                m.dos.writeShort(bo.id);
                                m.dos.writeByte(bo.cat);
                                bo.map.sendAllPlayer(m, (int)bo.inCountry, (int)this.idRegion);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    catch (Exception bo) {
                        // empty catch block
                    }
                    i = (byte)(i + 1);
                }
            }
            while (this.tempMonster.get(1).size() > 0) {
                Monster mt = this.tempMonster.get(1).remove(0);
                if (mt.map == null || mt.map.mapId != this.map.mapId || mt.hp <= 0) continue;
                mt.map.addMonster(mt, 1, mt.region);
            }
            while (this.tempRemoveMonster.get(1).size() > 0) {
                Monster mt = this.tempRemoveMonster.get(1).remove(0);
                if (BossLauDai.goKhoiBang(this.map, this.monsters.get(1), mt)) continue; // boss Gym / the gioi: go ca khi da guc
                if (mt.map == null || mt.map.mapId != this.map.mapId || mt.hp <= 0) continue;
                mt.map.removeMonster((int)mt.id, 1, mt.region);
            }
            Collection<Monster> listmonster = this.monsters.get(1).values();
            if (this.isSkelonton) {
                listmonster = this.temp_monsters.get(1).values();
            }
            for (Monster mt : listmonster) {
                try {
                    mt.update();
                    if (mt.target != null && mt.target.exit) {
                        mt.target = null;
                    }
                    if (players.size() <= 0 || !(Map.isNewVersion ? !mt.isDead : !mt.isDead && mt.moved)) continue;
                    Char fp = null;
                    // phan "luon gui quai nay" chi phu thuoc quai (allWayAdd tra template 2 lan qua Hashtable
                    // dong bo) -> tinh MOT lan moi quai thay vi moi cap quai x nguoi choi. -1 = chua tinh.
                    int luonThem = -1;
                    int i = 0;
                    while (i < players.size()) {
                        try {
                            Char p = players.get(i);
                            if (p.isBot == -1) {
                                if (p.near((Actor)mt, (int)p.rangeAddMonster[0]) || (luonThem >= 0 ? luonThem == 1 : (luonThem = (mt.allWayAdd() || mt.isBoss || mt.idTemplate == 84 || mt.idTemplate == 90 || mt.idTemplate == 36 || mt.idTemplate == 37 || this.map.mapIDLoadMap == 201) ? 1 : 0) == 1)) {
                                    if (Map.isNewVersion) {
                                        if (!UtilHTBB.coId(p.nearMons, mt.id)) {
                                            p.nearMons.add(mt.id);
                                            p.sendMessage(p.writeActorPos(new Message(4), (Actor)mt));
                                            LoaiQuaiGame.sauThemQuai(p, mt); // -125 loai rieng (client >= 908)
                                        }
                                    } else {
                                        p.nearMons.add(mt.id);
                                    }
                                }
                                if (p.near((Actor)mt, 120) && mt.typeAttack == 1) {
                                    if (p.hp > 0 && !p.beAttack && mt.target == null && mt.getMonsterTemplate().active && !p.isAdmin && p.isBot == -1 && mt.isEnemy(p) && p.region == mt.region) {
                                        mt.target = p;
                                        p.beAttack = true;
                                    } else if (p.beAttack && fp == null) {
                                        fp = p;
                                    }
                                }
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        ++i;
                    }
                    if (fp == null || mt.target != null) continue;
                    mt.target = fp;
                }
                catch (Exception e) {
                    Logger.logError((Exception)e, (String)"rUpdateleaf.txt");
                }
            }
        }
        int size = players.size();
        int k = 0;
        while (k < size) {
            try {
                Char p = players.get(k);
                if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                    players.remove(k);
                    CharManager.instance.remove(p);
                    continue;
                }
                if (!(p.map.equals(this.map) && p.mapID == this.map.mapId || p.isBot != -1)) {
                    players.remove(k);
                    continue;
                }
                if (p.exit) {
                    players.remove(k);
                    continue;
                }
                p.update();
                ++k;
            }
            catch (Exception e) {
                break;
            }
        }
        if (!this.isSkelonton) {
            if (Map.openSkelonton()) {
                this.isSkelonton = true;
                this.reSetAllMonster(1);
                this.killAllMonster(this.monsters, 1);
            }
        } else if (!Map.openSkelonton()) {
            this.isSkelonton = false;
            this.killAllMonster(this.monsters, 1);
            this.temp_monsters.get(1).clear();
        }
        this.map.deletePotionAndItemOnGround(1);
    }

    public void updateWind() {
        Vector<Char> players = this.allPlayers.get(2);
        if (players.size() > 0) {
            Monster mt;
            while (this.tempMonster.get(2).size() > 0) {
                mt = this.tempMonster.get(2).remove(0);
                if (mt.map == null || mt.map.mapId != this.map.mapId || mt.hp <= 0) continue;
                mt.map.addMonster(mt, 2, mt.region);
            }
            while (this.tempRemoveMonster.get(2).size() > 0) {
                mt = this.tempRemoveMonster.get(2).remove(0);
                if (BossLauDai.goKhoiBang(this.map, this.monsters.get(2), mt)) continue; // boss Gym / the gioi: go ca khi da guc
                if (mt.map == null || mt.map.mapId != this.map.mapId || mt.hp <= 0) continue;
                mt.map.removeMonster((int)mt.id, 2, mt.region);
            }
            Collection<Monster> listmonster = this.monsters.get(2).values();
            if (this.isSkelonton) {
                listmonster = this.temp_monsters.get(2).values();
            }
            for (Monster mt2 : listmonster) {
                try {
                    mt2.update();
                    if (mt2.target != null && mt2.target.exit) {
                        mt2.target = null;
                    }
                    if (players.size() <= 0 || !(Map.isNewVersion ? !mt2.isDead : !mt2.isDead && mt2.moved)) continue;
                    Char fp = null;
                    // phan "luon gui quai nay" chi phu thuoc quai (allWayAdd tra template 2 lan qua Hashtable
                    // dong bo) -> tinh MOT lan moi quai thay vi moi cap quai x nguoi choi. -1 = chua tinh.
                    int luonThem = -1;
                    int i = 0;
                    while (i < players.size()) {
                        try {
                            Char p = players.get(i);
                            if (p.isBot == -1) {
                                if (p.near((Actor)mt2, (int)p.rangeAddMonster[0]) || (luonThem >= 0 ? luonThem == 1 : (luonThem = (mt2.allWayAdd() || mt2.isBoss || mt2.idTemplate == 84 || mt2.idTemplate == 90 || mt2.idTemplate == 36 || mt2.idTemplate == 37 || this.map.mapIDLoadMap == 201) ? 1 : 0) == 1)) {
                                    if (Map.isNewVersion) {
                                        if (!UtilHTBB.coId(p.nearMons, mt2.id)) {
                                            p.nearMons.add(mt2.id);
                                            p.sendMessage(p.writeActorPos(new Message(4), (Actor)mt2));
                                        }
                                    } else {
                                        p.nearMons.add(mt2.id);
                                    }
                                }
                                if (p.near((Actor)mt2, 110) && mt2.typeAttack == 1) {
                                    if (p.hp > 0 && mt2.isEnemy(p) && !p.beAttack && mt2.target == null && mt2.getMonsterTemplate().active && !p.isAdmin && p.isBot == -1 && p.region == mt2.region) {
                                        mt2.target = p;
                                        p.beAttack = true;
                                    } else if (p.beAttack && fp == null) {
                                        fp = p;
                                    }
                                }
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        ++i;
                    }
                    if (fp == null || mt2.target != null) continue;
                    mt2.target = fp;
                }
                catch (Exception fp) {
                    // empty catch block
                }
            }
        }
        boolean exitBoard = false;
        int size = players.size();
        int k = 0;
        while (k < players.size()) {
            try {
                Char p = players.get(k);
                if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                    players.remove(k);
                    CharManager.instance.remove(p);
                    System.out.println("REMOVE ");
                    continue;
                }
                if (!(p.map.equals(this.map) && p.mapID == this.map.mapId || p.isBot != -1)) {
                    players.remove(k);
                    System.out.println("REMOVE ");
                    continue;
                }
                p.update();
            }
            catch (Exception e) {
                break;
            }
            ++k;
        }
        if (!this.isSkelonton) {
            if (Map.openSkelonton()) {
                this.isSkelonton = true;
                this.reSetAllMonster(2);
                this.killAllMonster(this.monsters, 2);
            }
        } else if (!Map.openSkelonton()) {
            this.isSkelonton = false;
            this.killAllMonster(this.monsters, 2);
            this.temp_monsters.get(2).clear();
        }
        this.map.deletePotionAndItemOnGround(2);
    }

    public void updateSand() {
        Monster mt;
        Vector<Char> players = this.allPlayers.get(0);
        if (players.size() > 0) {
            Collection<Monster> listmonster = this.monsters.get(0).values();
            if (this.isSkelonton) {
                listmonster = this.temp_monsters.get(0).values();
            }
            try {
                for (Monster mt2 : listmonster) {
                    try {
                        mt2.update();
                        if (mt2.target != null && mt2.target.exit) {
                            mt2.target = null;
                        }
                        if (players.size() <= 0 || !(Map.isNewVersion ? !mt2.isDead : !mt2.isDead && mt2.moved)) continue;
                        Char fp = null;
                    // phan "luon gui quai nay" chi phu thuoc quai (allWayAdd tra template 2 lan qua Hashtable
                    // dong bo) -> tinh MOT lan moi quai thay vi moi cap quai x nguoi choi. -1 = chua tinh.
                    int luonThem = -1;
                        int i = 0;
                        while (i < players.size()) {
                            try {
                                Char p = players.get(i);
                                if (p.isBot == -1) {
                                    if (p.near((Actor)mt2, (int)p.rangeAddMonster[0]) || (luonThem >= 0 ? luonThem == 1 : (luonThem = (mt2.allWayAdd() || mt2.isBoss || mt2.idTemplate == 84 || mt2.idTemplate == 90 || mt2.idTemplate == 36 || mt2.idTemplate == 37 || this.map.mapIDLoadMap == 201) ? 1 : 0) == 1)) {
                                        if (Map.isNewVersion) {
                                            if (!UtilHTBB.coId(p.nearMons, mt2.id)) {
                                                p.nearMons.add(mt2.id);
                                                p.sendMessage(p.writeActorPos(new Message(4), (Actor)mt2));
                                            }
                                        } else {
                                            p.nearMons.add(mt2.id);
                                        }
                                    }
                                    if (p.near((Actor)mt2, 110) && mt2.typeAttack == 1) {
                                        if (p.hp > 0 && mt2.isEnemy(p) && !p.beAttack && mt2.target == null && mt2.getMonsterTemplate().active && !p.isAdmin && p.isBot == -1 && p.region == mt2.region) {
                                            mt2.target = p;
                                            p.beAttack = true;
                                        } else if (p.beAttack && fp == null) {
                                            fp = p;
                                        }
                                    }
                                }
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            ++i;
                        }
                        if (fp == null || mt2.target != null) continue;
                        mt2.target = fp;
                    }
                    catch (Exception e) {
                        Logger.logError((Exception)e, (String)"rUpdateleaf.txt");
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println(String.valueOf(this.tempMonster.get(0).size()) + " MAP LOI: " + this.map.mapId + " > " + this.map.mapIDLoadMap + " >> " + this.idRegion);
            }
        }
        boolean exitBoard = false;
        int size = players.size();
        int k = 0;
        while (k < players.size()) {
            try {
                Char p = players.get(k);
                if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                    players.remove(k);
                    CharManager.instance.remove(p);
                    continue;
                }
                if (!(p.map.equals(this.map) && p.mapID == this.map.mapId || p.isBot != -1)) {
                    players.remove(k);
                    continue;
                }
                p.update();
                ++k;
            }
            catch (Exception e) {
                break;
            }
        }
        if (!this.isSkelonton) {
            if (Map.openSkelonton()) {
                this.isSkelonton = true;
                this.reSetAllMonster(0);
                this.killAllMonster(this.monsters, 0);
            }
        } else if (!Map.openSkelonton()) {
            this.isSkelonton = false;
            this.killAllMonster(this.monsters, 0);
            this.temp_monsters.get(0).clear();
        }
        this.map.deletePotionAndItemOnGround(0);
        while (this.tempMonster.get(0).size() > 0) {
            mt = this.tempMonster.get(0).remove(0);
            if (mt.map == null || mt.map.mapId != this.map.mapId || mt.hp <= 0) continue;
            mt.map.addMonster(mt, 0, mt.region);
        }
        while (this.tempRemoveMonster.get(0).size() > 0) {
            mt = this.tempRemoveMonster.get(0).remove(0);
            if (BossLauDai.goKhoiBang(this.map, this.monsters.get(0), mt)) continue; // boss Gym / the gioi: go ca khi da guc
            if (mt.map == null || mt.map.mapId != this.map.mapId || mt.hp <= 0) continue;
            mt.map.removeMonster((int)mt.id, 0, mt.region);
        }
    }

    protected void doAttackPlayer(Char p, Message message) {
        try {
            short mplost;
            long now;
            if (p.cannotAttackWhenBienhinh()) {
                return;
            }
            if (p.countHit() || p.freeze() || (com.htbb.pokemon.LauDai.bat() && TrangThaiGame.khongDanhDuoc(p))) { // GD2b: ngu/bang/te
                return;
            }
            if (p.myCountry == -1) {
                return;
            }
            if (p.hp <= 0) {
                p.actorDie();
                return;
            }
            if (!p.checkDurableWeapone()) {
                p.sendMessage(MessageCreator.createServerAlertMessage((String)"Kh\u00f4ng th\u1ec3 t\u1ea5n c\u00f4ng khi \u0111\u1ed3 c\u1ea7m tay b\u1ecb h\u01b0 h\u1ea1i. H\u00e3y \u0111\u1ebfn Th\u1ee3 r\u00e8n \u0111\u1ec3 s\u1eeda l\u1ea1i.", (String)""));
                return;
            }
            if (p.checkLamthinh() || p.checkRuNgu() || p.checkChoang()) {
                return;
            }
            p.downDurableWeapone();
            boolean inArenaP = false;
            DataInputStream dis = message.dis;
            Char c = this.getChar(dis.readShort());
            if (c == null) {
                return;
            }
            if (c.isThangBe()) {
                return;
            }
            if (c.isMyHoVe(p)) {
                return;
            }
            if (c.getIdCharThanThu() > -1) {
                return;
            }
            if (c.hp <= 0) {
                return;
            }
            boolean timeAutoPK = Map.pkAuto;
            boolean haveShield = c.haveShield();
            // 0.9.7 phong rieng (pho ban / san cup, com.htbb.pokemon.PhongRieng): PvP CHI trong tran cup, giua hai doi
            // cua cung tran (CupGame.choDanh); bo qua luat PK the gioi (co do sat, phe, bao ho, to doi).
            final boolean cupTran = com.htbb.pokemon.PhongRieng.laMapRieng(this.map.mapId);
            if (cupTran) {
                if (!CupGame.choDanh(p, c) || p.mapID != c.mapID || p.region != c.region
                        || !Map.inRangeActor((LiveActor)p, (LiveActor)c, (int)MAX_RANGE_CHAR[p.charClass])) {
                    return;
                }
            } else {
            if (!(p.myCountry == c.myCountry || !haveShield || p.isKiller || c.isKiller || p.pk > 0 && c.pk > 0 && p.pk != c.pk || Map.nwar[p.inCountry] && (!Map.nwar[p.inCountry] || this.map.mapId == Map.idMapTown))) {
                p.sendMessage(MessageCreator.createMsgChat((int)p.id, (String)"Ng\u01b0\u1eddi ch\u01a1i \u0111ang \u0111\u01b0\u1ee3c b\u1ea3o h\u1ed9."));
                return;
            }
            if (!timeAutoPK && p.killer >= 319000 && p.nPKill >= 500 && p.isKiller) {
                Message m = new Message(27);
                m.dos.writeShort(p.id);
                m.dos.writeUTF("Kh\u00f4ng th\u1ec3 pk th\u00eam.");
                p.sendMessage(m);
                m.cleanup();
                return;
            }
            if (p.checkInParty(c)) {
                return;
            }
            if (p.map.mapIDLoadMap == 0 || p.map.mapIDLoadMap == 80 || p.map.mapIDLoadMap == 70) {
                p.sendMessage(MessageCreator.createMsgChat((int)p.id, (String)"Kh\u00f4ng th\u1ec3 \u0111\u00e1nh khi \u0111ang trong l\u00e0ng."));
                return;
            }
            if (c.isBot != -1) {
                return;
            }
            if (p.mapID != c.mapID) {
                return;
            }
            if (p.region != c.region) {
                return;
            }
            if (p.lvDetail.lv < 40 && c.monster != null) {
                return;
            }
            // 0.9.7 luat PK the gioi (real.LuatPkGame): thi tran an toan, chenh cap > 10, nghiep qua nang
            if (LuatPkGame.chan(this.map, p, c) != null) {
                return;
            }
            if (!Map.inRangeActor((LiveActor)p, (LiveActor)c, (int)MAX_RANGE_CHAR[p.charClass])) {
                return;
            }
            if (this.map.mapId != Map.idMapDautruong && this.map.mapId != Map.idMapTown) {
                if (!this.map.inMapBoss(c)) {
                    if (!(timeAutoPK && p.myCountry != c.myCountry || p.isKiller || p.pk != 0 || c.pk != 0 || c.isKiller)) {
                        return;
                    }
                    if (p.pk > 0) {
                        if (!(timeAutoPK || (c.pk != 0 || c.isKiller) && c.pk != p.pk || p.isMonster)) {
                            return;
                        }
                        if ((c.pk == 0 && !c.isKiller || c.pk > 0 && c.pk == p.pk) && !p.isMonster && p.myCountry == c.myCountry) {
                            return;
                        }
                    }
                } else if (c.partyID == p.partyID && c.partyID != -1 && p.partyID != -1 || p.idClan == c.idClan && p.idClan > -1 && c.idClan > -1) {
                    return;
                }
            } else if ((p.idClan == -1 || c.idClan == -1 || p.idClan == c.idClan) && p.myCountry == c.myCountry) {
                return;
            }
            if (p.isKiller) {
                if (p.lvDetail.lv < 10) {
                    return;
                }
                if (c.lvDetail.lv < 10) {
                    return;
                }
            }
            } // het luat PK the gioi (khong ap trong tran cup)
            byte skill = dis.readByte();
            int effect = 0;
            int ahp = p.attackDamage;
            int buffAttack = -1;
            byte _type = skill;
            int _level = p.skill[_type] + p.addMoreLevelSkill[_type];
            if (_level <= 0) {
                _level = p.addMoreLevelSkill[_type];
            }
            if ((now = System.currentTimeMillis()) - p.timeLastUseSkills[_type] < (long)(CharManager.SKILL_COOLDOWN[p.charClass][_type][_level] * 100)) {
                return;
            }
            p.timeLastUseSkills[_type] = now;
            if (_level <= 0 || !Map.inRangeSkill((LiveActor)p, (LiveActor)c, (int)CharManager.getSkillRange((byte)_type, (byte)p.charClass))) {
                return;
            }
            if (p.haveTanPhe() > 0 && c.addEffBuff((int)EffectBuff.TAN_PHE, System.currentTimeMillis() + (long)p.haveTanPhe(), (int)EffectBuff.BY_ACTOR, 0) != null) {
                c.sendEffToChar(c);
                c.sendEffToNearChar();
                c.divSpeed = (byte)2;
                c.sendMessage(MessageCreator.createMainCharInfoMessage((Char)c));
            }
            if (p.haveLamThinh() > 0 && c.addEffBuff((int)EffectBuff.LAM_THINH, System.currentTimeMillis() + (long)p.haveLamThinh(), (int)EffectBuff.BY_ACTOR, 0) != null) {
                c.sendEffToChar(c);
                c.sendEffToNearChar();
            }
            buffAttack = p.getBuffEffAttack();
            int damage = p.attackDam((LiveActor)c, (int)_type, _level, buffAttack);
            final int damGocLauDai = damage;
            damage /= 5;
            damage -= damage / 3;
            damage = p.subDam(c, damage);
            damage *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
            boolean critSv = p.havecrit();
            boolean baokich = p.haveBaoKich();
            if (baokich) {
                damage = com.htbb.pokemon.LauDai.bat() ? p.tkNhanChiMang(damage, 4) : damage * 4; // GD2: x1,5 khi bat co
                effect = 4;
            } else if (critSv) {
                damage = com.htbb.pokemon.LauDai.bat() ? p.tkNhanChiMang(damage, 2) : damage * 2;
                effect = 2;
                if (p.petUsing != null) {
                    long pcLienKich = p.petUsing.getLienKich();
                    damage = (int)((long)damage + (long)damage * pcLienKich / 100L);
                }
            }
            if (damage > 50000) {
                damage = 50000 + Map.r.nextInt(100);
            }
            if (com.htbb.pokemon.LauDai.bat()) {
                // GD2 6.1 PvP: x0,30, chi mang x1,5, tran 8% HP toi da muc tieu (thay /5, -1/3, x4, tran 50k).
                long d = (long) damGocLauDai * CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
                if ((critSv || baokich) && ChiSoTrangBiGame.giuChiMangPvp(p, c)) { // PvP: tran 30%, tru khang chi mang
                    d = d * com.htbb.pokemon.SatThuongLauDai.CHI_MANG_PHAN_TRAM / 100L;
                    effect = 2;
                } else if (critSv || baokich) {
                    effect = 0;
                }
                damage = com.htbb.pokemon.SatThuongLauDai.pvp((int) Math.min(Integer.MAX_VALUE, d), c.maxhp);
                damage = p.subDam(c, damage);
                if (cupTran) {
                    damage = CupGame.satThuong(p, c, d); // chuan hoa: phan nghin HP muc tieu theo thang do nguoi danh
                }
            }
            if (p.mp + p.percentBuff[1] < (mplost = CharManager.SKILL_MP[p.charClass][_type][_level])) {
                return;
            }
            p.mp -= mplost;
            if (p.mp <= 0) {
                p.mp = 0;
            }
            int hphut = 0;
            if (p.haveHutHp() > 0 && p.hp < p.maxhp) {
                int hp = p.haveHutHp();
                hphut = hp;
                if (c.hp < hphut) {
                    hphut = c.hp;
                }
                p.hp += hphut;
                p.calculatorHPMP();
                MessageCreator.createMsgUseHpMP((Char)p, (int)hp, (int)1);
            }
            damage += hphut;
            damage = c.checkHapthuSatThuong(damage, (LiveActor)p);
            damage = c.checkGiamSatThuong(damage);
            ahp = damage = c.checkPassAttack((LiveActor)p, damage);
            if (ahp < 0) {
                ahp = 1;
            }
            c.hp = (int)((long)c.hp - ((long)damage - c.checkMagicShield(damage)));
            XepHangPvpGame.khiSatThuong(p, c, damage); // tran xep hang: sat thuong ben thua gay (chong cay Elo)
            ChiemGymLauDai.dongGop(p, damage); // dong gop chien thanh (tat co: khong lam gi)
            if (c.hp <= 0 && !cupTran) {
                ThangPvpGame.ketQua(p, c); // GD2 7.2 Elo mua (tat co: khong lam gi) - tran cup KHONG tinh Elo
            }
            c.checkNewEffectItem(0, (long)(damage / 10), (LiveActor)p);
            c.downDuarable();
            if (p.charthanthu != null && c.hp > 0) {
                Vector<LiveActor> target = new Vector<LiveActor>();
                target.add((LiveActor)c);
                p.charthanthu.doAttack(target);
                c.hp -= p.getDamtThanThu((LiveActor)c);
            }
            if (this.map.isMapTrain() && c.isHieuUngCoLongDuongQua() && c.hp <= 0) {
                c.hp = 1;
            }
            if (cupTran) {
                CupGame.sauDon(p, c); // tran cup: HP khong ve 0 (khong guc, khong mat EXP) - het HP = bi loai
            }
            if (c.hp <= 0) {
                if (c.hp <= 0) {
                    c.hp = 0;
                    if (!c.isCharCopy()) {
                        c.timeWaitComeHome = c.timedie = System.currentTimeMillis();
                        if (p.lvDetail.lv >= 40 && c.lvDetail.lv >= 40 && p.myCountry != c.myCountry && Map.abs((int)(p.lvDetail.lv - c.lvDetail.lv)) <= 10) {
                            p.honor += 2;
                            c.honor -= 10;
                            if (c.honor < 0) {
                                c.honor = 0;
                            }
                            p.sendMessage(MessageCreator.createMainCharInfoMessage((Char)p));
                            c.sendMessage(MessageCreator.createMainCharInfoMessage((Char)c));
                        }
                    }
                    if (c.isCharCopy()) {
                        c.actorDie();
                    }
                }
                this.map.checkTrade(c);
                if (!c.isKiller) {
                    c.die_pk = true;
                } else {
                    try {
                        c.desTroy();
                        long xp = c.lvDetail.getExp();
                        long xplost = c.lvDetail.getXPLost((int)c.killer, c);
                        if (c.isKiller) {
                            c.killer = (short)(c.killer - 5);
                            if (c.killer <= 0) {
                                c.killer = 0;
                                c.isKiller = false;
                            }
                        }
                        Database.instance.saveOrtherLog("", c.charname, String.valueOf(c.hp) + "_" + ahp + "_" + p.charname + "_" + Map.getNameMap((int)this.map.mapId) + "_" + p.region + "_" + c.region, "die");
                        c.xpLost += xplost;
                        int currentlv = c.lvDetail.lv;
                        c.lvDetail.setExp(xp -= xplost, c.oldLv, c.charname, "region");
                        if (c.lvDetail.lv <= 0) {
                            c.lvDetail.lv = 0;
                            c.lvDetail.percent = 0;
                        }
                        if (currentlv > c.lvDetail.lv) {
                            c.lvDetail.resetExp2Lv(currentlv, (int)c.killer);
                            if (c.killer > 0) {
                                Database.instance.saveOrtherLog("", c.charname, "tut level do dang trong che do ds " + c.killer, "downlv");
                            }
                        }
                        c.calculateAttrib();
                        c.doSendProperties();
                        Message m = new Message(67);
                        m.dos.writeShort(c.id);
                        m.dos.writeByte(c.isKiller ? 1 : 0);
                        m.dos.writeShort(c.killer);
                        c.sendMessage(m);
                        c.sendToNearPlayer(m);
                        m.cleanup();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (c.myCountry != p.myCountry) {
                    if (p.myCountry == p.inCountry) {
                        if (!timeAutoPK) {
                            Map.sendAllCharServer((int)17, (Message)MessageCreator.createServerAlertAutoOffMessage((String)(String.valueOf(p.charname) + " " + Map.nameCountry[p.myCountry] + " \u0111\u00e1nh b\u1ea1i " + c.charname + " " + Map.nameCountry[c.myCountry] + " t\u1ea1i khu " + (this.idRegion + 1) + " " + Map.getNameMap((int)this.map.mapId))));
                        }
                    } else {
                        if (!timeAutoPK) {
                            Map.sendAllCharServer((int)17, (Message)MessageCreator.createServerAlertAutoOffMessage((String)(String.valueOf(c.charname) + " " + Map.nameCountry[c.myCountry] + " b\u1ecb " + p.charname + " " + Map.nameCountry[p.myCountry] + " \u0111\u00e1nh b\u1ea1i t\u1ea1i khu " + " " + this.idRegion + " " + Map.getNameMap((int)this.map.mapId))));
                        }
                        Map.addCharKiller((Char)p, (int)p.inCountry);
                    }
                }
                c.doSetTimeAutoHoiSinh();
            } else {
                if (ahp > 0) {
                    p.buffAttackSkill(damage, (LiveActor)c);
                }
                p.buffSkillKham((LiveActor)c);
                p.charHireAttackDam((LiveActor)c, (int)_type, _level, buffAttack);
            }
            CoPkGame.daDanhNguoi(p); // don trung nguoi choi: cho 10 giay moi thao khan PK
            if (p.isKiller && !cupTran) {
                if (p.killer == 0 && p.killer < 32000) {
                    p.killer = p.charClass == 4 ? (short)(p.killer + 50) : (short)(p.killer + 1);
                }
                if (c.hp <= 0 && !c.isKiller) {
                    if (p.killer < 32000) {
                        p.killer = (short)(p.killer + 100);
                    }
                    if (p.nPKill == 0) {
                        p.timeKiller = System.currentTimeMillis();
                    }
                    p.nPKill = (short)(p.nPKill + 1);
                    if (p.killer >= 32000) {
                        p.killer = (short)32000;
                    }
                    c.addListKillMe(p.charname);
                }
                Message msg = new Message(67);
                msg.dos.writeShort(p.id);
                msg.dos.writeByte(1);
                msg.dos.writeShort(p.killer);
                p.sendMessage(msg);
                p.sendToNearPlayer(msg);
                msg.cleanup();
            }
            if (c.hp <= 0 && Map.nwar[p.inCountry] && this.map.mapId == 201) {
                if (!p.checkLienTram(c.charname)) {
                    p.totalKill = (short)(p.totalKill + 1);
                    if (p.totalKill > p.maxKill) {
                        p.maxKill = p.totalKill;
                        Map.doAddTopLienTram((int)p.myCountry, (Char)p);
                    }
                }
                c.totalKill = 0;
            }
            Message m = new Message(6);
            m.dos.writeShort(p.id);
            m.dos.writeShort(c.id);
            m.dos.writeByte(skill);
            m.dos.writeInt(ahp);
            m.dos.writeInt(c.hp);
            m.dos.writeByte(effect);
            m.dos.writeByte(CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1]);
            m.dos.writeByte(buffAttack);
            m.dos.writeByte(_level);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            m.cleanup();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doAttackMultiTarget(Char p, Message message) {
        try {
            int totalXp;
            Monster mt;
            if (p.cannotAttackWhenBienhinh()) {
                return;
            }
            if (p.countHit() || p.freeze() || (com.htbb.pokemon.LauDai.bat() && TrangThaiGame.khongDanhDuoc(p))) { // GD2b: ngu/bang/te
                return;
            }
            if (p.hp <= 0) {
                p.actorDie();
                return;
            }
            if (!p.checkDurableWeapone()) {
                p.sendMessage(MessageCreator.createServerAlertMessage((String)"Kh\u00f4ng th\u1ec3 \u0111\u00e1nh khi \u0111\u1ed3 c\u1ea7m tay b\u1ecb h\u1ecfng. H\u00e3y \u0111\u1ebfn Th\u1ee3 r\u00e8n \u0111\u1ec3 s\u1eeda l\u1ea1i.", (String)""));
                Message m = new Message(104);
                try {
                    m.dos.writeByte(p.typeConfig);
                    m.dos.writeByte(0);
                    p.sendMessage(m);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return;
            }
            if (p.checkLamthinh() || p.checkRuNgu() || p.checkChoang()) {
                return;
            }
            DataInputStream dis = message.dis;
            byte skill = dis.readByte();
            // Chi cho phep da muc tieu neu skill nay thuc su la skill AOE cua class
            // (CharManager.SKILL_AEO), tranh skill don (lv1/6/11/17) an theo da quai
            // khi client van gui du lieu nhieu muc tieu. Dong bo voi Map.doAttackMultiTarget.
            boolean isAoeSkill = false;
            for (final byte aeoSkill : CharManager.SKILL_AEO[p.charClass]) {
                if (aeoSkill == skill) {
                    isAoeSkill = true;
                    break;
                }
            }
            byte effect = 0;
            boolean ahp1 = false;
            boolean crit = false;
            int buffAttack = -1;
            if (buffAttack > 0) {
                return;
            }
            if (buffAttack != -1 && buffAttack == 0 && p.skill[5] + p.addMoreLevelSkill[5] == 0) {
                return;
            }
            int nMonster = dis.readByte();
            Monster firstMonster = null;
            firstMonster = mt = this.getMonster(dis.readShort(), p.inCountry);
            if (mt == null || mt.isDead) {
                if (mt != null) {
                    Map.onMosterDie((Char)p, (LiveActor)mt, (byte)skill, (int)1, (byte)effect, (byte)0);
                }
                return;
            }
            if (mt.getCharID() == p.charDBID) {
                return;
            }
            if (!Map.inRangeActor((LiveActor)p, (LiveActor)mt, (int)MAX_RANGE_CHAR[p.charClass])) {
                return;
            }
            if (mt.map.mapId != p.mapID) {
                return;
            }
            if (mt.isMyMonster(p)) {
                return;
            }
            if (!Map.canAttackMonsterVantieu((Monster)mt, (Char)p)) {
                return;
            }
            byte _type = skill;
            int _level = p.skill[_type] + p.addMoreLevelSkill[_type];
            if (_level <= 0) {
                _level = p.addMoreLevelSkill[_type];
            }
            if (_level <= 0 || !Map.inRangeSkill((LiveActor)p, (LiveActor)mt, (int)MAX_RANGE_CHAR[p.charClass])) {
                return;
            }
            long now = System.currentTimeMillis();
            if (now - p.timeLastUseSkills[_type] < (long)p.coolDown[_type][_level]) {
                return;
            }
            if (mt.isRuongMaquai()) {
                if (p.itemVukhiThoiTrang == null || !p.itemVukhiThoiTrang.isGayGiangSinh()) {
                    return;
                }
                if (mt.getInCountry() != p.inCountry) {
                    p.sendMessage(MessageCreator.createMsgChat((int)p.id, (String)"Kh\u00f4ng th\u1ec3 \u0111\u00e1nh r\u01b0\u01a1ng c\u1ee7a l\u00e0ng kh\u00e1c."));
                    return;
                }
            }
            int hphut = 0;
            if (p.haveHutHp() > 0 && p.hp < p.maxhp && !mt.isNgocRong() && !mt.isRuongMaquai()) {
                int hp = p.haveHutHp();
                hphut = hp;
                if (mt.hp < hphut) {
                    hphut = mt.hp;
                }
                p.hp += hphut;
                p.calculatorHPMP();
                MessageCreator.createMsgUseHpMP((Char)p, (int)hp, (int)1);
            }
            p.timeLastUseSkills[_type] = now;
            int buffAttackClient = buffAttack;
            buffAttack = p.getBuffEffAttack();
            if (mt.resistThroughArmor()) {
                buffAttack = -1;
            }
            int damage = p.attackDam((LiveActor)mt, (int)_type, _level, buffAttack);
            if (mt.isNgocRong()) {
                if (damage == 1) {
                    if (p.potions[73] == 0) {
                        p.sendMessage(MessageCreator.createMsgChat((int)p.id, (String)"Kh\u00f4ng c\u00f3 t\u00fai nh\u1eb7t ng\u1ecdc"));
                    } else {
                        mt.onDropItem(this.map, p);
                    }
                } else if (mt.isDead) {
                    mt.sendActorDie(p);
                }
                return;
            }
            if (mt.haveDodge()) {
                damage = 0;
                buffAttack = -1;
            }
            if (mt.isMonsterVantieu() && damage >= mt.maxhp / 100) {
                damage = mt.maxhp / 100 + Map.r.nextInt() % 100;
            }
            damage *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
            boolean critSv = p.havecrit();
            boolean baokich = p.haveBaoKich();
            if (baokich) {
                damage = com.htbb.pokemon.LauDai.bat() ? p.tkNhanChiMang(damage, 4) : damage * 4; // GD2: x1,5 khi bat co
                effect = 4;
            } else if (critSv) {
                damage = com.htbb.pokemon.LauDai.bat() ? p.tkNhanChiMang(damage, 2) : damage * 2;
                effect = 2;
                if (p.petUsing != null) {
                    long pcLienKich = p.petUsing.getLienKich();
                    damage = (int)((long)damage + (long)damage * pcLienKich / 100L);
                }
            }
            damage += hphut;
            if (_level > p.skill[_type] + p.addMoreLevelSkill[_type]) {
                return;
            }
            short mplost = CharManager.SKILL_MP[p.charClass][_type][_level];
            if (p.mp + p.percentBuff[1] < mplost) {
                return;
            }
            p.mp -= mplost;
            if (p.mp <= 0) {
                p.mp = 0;
            }
            if (damage > 0 && mt.haveBackDam()) {
                int backdam = mt.getBackDam(damage);
                Message mbd = MessageCreator.createMsgBuffEffect((int)5, (int)mt.cat, (LiveActor)p, (int)backdam, (int)0, (int)-1);
                p.sendMessage(mbd);
                p.sendToNearPlayer(mbd);
            }
            Message m = null;
            int i = 0;
            int allXP = 0;
            Vector<Monster> mst = new Vector<Monster>();
            mst.add(firstMonster);
            byte[] nmonster = new byte[]{5, 5, 8, 10, 10, 10, 10, 10, 10, 10, 10, 10};
            Vector<Message> msgMonsterDie = new Vector<Message>();
            Vector<LiveActor> muctieu = new Vector<LiveActor>();
            while (i < nMonster) {
                if (i > 0) {
                    mt = this.getMonster(dis.readShort(), p.inCountry);
                    if (!isAoeSkill) {
                        ++i;
                        continue;
                    }
                }
                if (mt != null) {
                    int delta;
                    int dxp;
                    if (i > 0) {
                        if (mt.isMyMonster(p) || !Map.canAttackMonsterVantieu((Monster)mt, (Char)p) || !Map.inRangeActor((LiveActor)firstMonster, (LiveActor)mt, (int)MAX_RANGE_CHAR[p.charClass])) {
                            ++i;
                            continue;
                        }
                        if (mt.isDead) {
                            // Client gui trung mot id quai nhieu lan trong CUNG mot goi 106 (skill dien
                            // rong): getMonster van tra ve dung doi tuong da chet, va truoc day thieu
                            // continue o day nen ma roi tiep xuong tinh sat thuong/onDropItem MOI LUOT -
                            // mot goi tin quay drop + cong exp hang chuc/hang tram lan tren MOT con quai.
                            // Dung mau da co san o RegionVantienTran.java cho cung ham nay.
                            Map.onMosterDie((Char)p, (LiveActor)mt, (byte)skill, (int)damage, (byte)effect, (byte)0);
                            ++i;
                            continue;
                        } else {
                            mst.add(mt);
                        }
                    }
                    if (mt.isRuongMaquai()) {
                        if (p.itemVukhiThoiTrang == null || !p.itemVukhiThoiTrang.isGayGiangSinh()) {
                            ++i;
                            continue;
                        }
                        if (mt.getInCountry() != p.inCountry) {
                            ++i;
                            p.sendMessage(MessageCreator.createMsgChat((int)p.id, (String)"Kh\u00f4ng th\u1ec3 \u0111\u00e1nh r\u01b0\u01a1ng c\u1ee7a l\u00e0ng kh\u00e1c."));
                            continue;
                        }
                    }
                    if ((dxp = mt.getXpReceive(damage)) == 0) {
                        dxp = 1;
                    }
                    // Truoc day phan tu cuoi la "dxp" (tu chia cho chinh no -> luon = 1
                    // bat ke dxp ban dau la bao nhieu). Doi thanh so cu the, tiep noi
                    // day tang dan cua tung mang. Dong bo voi Map.doAttackMultiTarget.
                    int[] downPercent = new int[]{1, 5, 10, 20};
                    short targetLv = p.getLevel();
                    if (targetLv < 40) {
                        downPercent = new int[]{1, 2, 3, 4};
                    }
                    if ((delta = targetLv - mt.level) > 0) {
                        int a = delta / 4;
                        if (targetLv < 40) {
                            a = delta / 6;
                        }
                        if (a > 3) {
                            a = 3;
                        }
                        dxp /= ExpLauDai.chiaCu(downPercent[a]);
                    }
                    if (dxp <= 0) {
                        dxp = 1;
                    }
                    allXP += ExpLauDai.lechCapMot(dxp, p, mt);
                    mt.hp -= damage;
                    BossTrungThu.ghiNhanSatThuong(mt, p, damage);
                    if (p.charthanthu != null && mt.hp > 0) {
                        muctieu.add(mt);
                        int damthanthu = p.getDamtThanThu((LiveActor)mt);
                        allXP += ExpLauDai.lechCapMot(mt.getXpReceive(damthanthu), p, mt);
                        mt.hp -= damthanthu;
                        BossTrungThu.ghiNhanSatThuong(mt, p, damthanthu);
                    }
                    if (mt.hp <= 0) {
                        if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry) {
                            p.killer = (short)(p.killer + 200);
                            if (p.killer > 32000) {
                                p.killer = (short)32000;
                            }
                            Message msg = new Message(67);
                            msg.dos.writeShort(p.id);
                            msg.dos.writeByte(1);
                            msg.dos.writeShort(p.killer);
                            p.sendMessage(msg);
                            p.sendToNearPlayer(msg);
                        }
                        Vector<Actor> droplist = new Vector<Actor>();
                        if (!mt.isMaterialMons()) {
                            droplist = mt.onDropItem(this.map, p);
                        }
                        mt.hp = 0;
                        m = new Message(17);
                        m.dos.writeShort(p.id);
                        m.dos.writeShort(mt.id);
                        m.dos.writeByte(skill);
                        m.dos.writeInt(damage);
                        m.dos.writeByte(effect);
                        m.dos.writeByte(droplist.size());
                        if (droplist.size() > 0) {
                            for (Actor e : droplist) {
                                Map.writeActorPos((Message)m, (Actor)e, (byte)p.getSession().isOldVersion);
                            }
                        }
                        byte xx2 = CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0 ? _level - 1 : 0];
                        m.dos.writeByte(xx2);
                        m.dos.writeByte(buffAttack);
                        m.dos.writeByte(_level);
                        msgMonsterDie.add(m);
                        if (p.receiveQuest && QuestTemplate.QUEST_TYPE[p.questID - 1] == 0) {
                            p.checkFinsishQuest((int)mt.getType(), -1, -1);
                        }
                    } else {
                        if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry && p.killer < 200) {
                            p.killer = (short)(p.killer + 200);
                            Message msg = new Message(67);
                            msg.dos.writeShort(p.id);
                            msg.dos.writeByte(1);
                            msg.dos.writeShort(p.killer);
                            p.sendMessage(msg);
                            p.sendToNearPlayer(msg);
                        }
                        if (mt.target == null) {
                            mt.target = p;
                        }
                    }
                    if (mt.hp <= 0) {
                        if (!mt.isBoss || !mt.isCopy()) {
                            if (mt.isBoss) {
                                mt.bornTime = System.currentTimeMillis() + 86400000L;
                                mt.setTimeReBornInEvent(mt.bornTime);
                                Database.instance.saveEvent(Map.event.getInfo());
                                Map.removeBossLocation((int)1);
                            } else {
                                mt.setTimeReBorn();
                            }
                        }
                        mt.isDead = true;
                        mt.target = null;
                    }
                }
                ++i;
                if (mst.size() >= nmonster[_level]) break;
            }
            try {
                if (p.charthanthu != null && muctieu.size() > 0) {
                    p.charthanthu.doAttack(muctieu);
                }
            }
            catch (Exception dxp) {
                // empty catch block
            }
            m = new Message(106);
            m.dos.writeShort(p.id);
            m.dos.writeByte(skill);
            m.dos.writeInt(damage);
            m.dos.writeByte(effect);
            m.dos.writeByte(_level);
            m.dos.writeByte(buffAttack);
            m.dos.writeByte(mst.size());
            int j = 0;
            while (j < mst.size()) {
                Monster ms = (Monster)mst.elementAt(j);
                m.dos.writeShort(ms.id);
                m.dos.writeInt(ms.hp > 0 ? ms.hp : 0);
                ++j;
            }
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            j = 0;
            while (j < msgMonsterDie.size()) {
                try {
                    p.sendMessage((Message)msgMonsterDie.get(j));
                    p.sendToNearPlayer((Message)msgMonsterDie.get(j));
                }
                catch (Exception ms) {
                    // empty catch block
                }
                ++j;
            }
            int dxp = Map.rand10((int)allXP);
            if (dxp == 0) {
                dxp = 1;
            }
            if ((totalXp = dxp) > 0) {
                // GD2 3.4: bat co sv.lau_dai -> HAM DUY NHAT ExpLauDai.traoHaQuai (thay ban sao duoi).
                if (com.htbb.pokemon.LauDai.bat()) {
                    ExpLauDai.traoHaQuai(p, null, totalXp, "Region:1301");
                } else {
                int newxp = Map.calculatorXpParty((Char)p, (int)totalXp);
                if (newxp != totalXp) {
                    int nUser = p.party.userParty.size();
                    // GD2 1.6: chia theo SO NGUOI THAT (ban cu ep nUser = 5 khi > 1 nguoi).
                    int xpReceive = Map.chiaExpDoi(newxp, 80, nUser, 1);
                    int maxLv = p.lvDetail.lv;
                    int k = 0;
                    while (k < p.party.userParty.size()) {
                        Char pp = (Char)p.party.userParty.get(k);
                        if (pp.id != p.id && p.near((Actor)pp, 320) && pp.mapID == p.mapID && pp.inCountry == p.inCountry && pp.region == p.region) {
                            int dlv = Map.abs((int)(maxLv - pp.lvDetail.lv));
                            int temp = 1;
                            temp = dlv <= 5 ? xpReceive : (dlv <= 10 ? xpReceive / 5 : (dlv <= 20 ? xpReceive / 10 : (dlv <= 30 ? xpReceive / 15 : xpReceive / 20)));
                            if (temp == 0) {
                                temp = 1;
                            }
                            if (pp.hp > 0) {
                                temp *= Map.doubleALL;
                                temp = Monster.apDungGiamExpTheoCapNV(temp, pp);
                                temp = pp.expReceive(temp);
                                Map.addXPForChar((Char)pp, (long)(temp + pp.getEffSkillClan(0) * temp / 100), (boolean)false, (String)"Region1");
                            }
                        }
                        ++k;
                    }
                    xpReceive = Map.chiaExpDoi(newxp, 20, 1, Map.doubleALL);
                    xpReceive = Monster.apDungGiamExpTheoCapNV(xpReceive, p);
                    xpReceive = p.expReceive(xpReceive);
                    Map.addXPForChar((Char)p, (long)(xpReceive + p.getEffSkillClan(0) * xpReceive / 100), (boolean)false, (String)"Region2");
                } else {
                    totalXp *= Map.doubleALL;
                    totalXp = Monster.apDungGiamExpTheoCapNV(totalXp, p);
                    totalXp = p.expReceive(totalXp);
                    Map.addXPForChar((Char)p, (long)(totalXp + p.getEffSkillClan(0) * totalXp / 100), (boolean)false, (String)"Region3");
                }
                }
            }
            p.charHireAttackMultiMOnster(mst, (int)_type);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setTimeCake() {
        int[] tick = new int[]{2};
        timeDropCake = System.currentTimeMillis() + (long)(tick[Map.r.nextInt(tick.length)] * 60000);
    }

    protected void doAttackMonster(Char p, Message message) throws IOException {
        Message msg;
        if (p.cannotAttackWhenBienhinh()) {
            return;
        }
        if (p.countHit() || p.freeze() || (com.htbb.pokemon.LauDai.bat() && TrangThaiGame.khongDanhDuoc(p))) { // GD2b: ngu/bang/te
            return;
        }
        if (p.hp <= 0) {
            p.actorDie();
            return;
        }
        if (!p.checkDurableWeapone()) {
            p.sendMessage(MessageCreator.createServerAlertMessage((String)"Kh\u00f4ng th\u1ec3 \u0111\u00e1nh khi \u0111\u1ed3 c\u1ea7m tay b\u1ecb h\u1ecfng. H\u00e3y \u0111\u1ebfn Th\u1ee3 r\u00e8n \u0111\u1ec3 s\u1eeda l\u1ea1i.", (String)""));
            Message m = new Message(104);
            try {
                m.dos.writeByte(p.typeConfig);
                m.dos.writeByte(0);
                p.sendMessage(m);
            }
            catch (Exception exception) {
                // empty catch block
            }
            return;
        }
        if (p.checkLamthinh() || p.checkRuNgu() || p.checkChoang()) {
            return;
        }
        p.downDurableWeapone();
        DataInputStream dis = message.dis;
        Monster mt = this.getMonster(dis.readShort(), p.inCountry);
        byte skill = dis.readByte();
        int effect = 0;
        int ahp = p.attackDamage;
        boolean crit = false;
        int buffAttack = -1;
        if (buffAttack > 0) {
            return;
        }
        if (buffAttack != -1 && buffAttack == 0 && p.skill[5] + p.addMoreLevelSkill[5] == 0) {
            return;
        }
        if (mt == null || mt.isDead) {
            if (mt != null) {
                Map.onMosterDie((Char)p, (LiveActor)mt, (byte)skill, (int)1, (byte)effect, (byte)0);
            }
            return;
        }
        if (mt.getCharID() == p.charDBID) {
            return;
        }
        if (!Map.inRangeActor((LiveActor)p, (LiveActor)mt, (int)MAX_RANGE_CHAR[p.charClass])) {
            return;
        }
        if (mt.isMyMonster(p)) {
            return;
        }
        if (mt.map.mapId != p.mapID) {
            return;
        }
        if (!Map.canAttackMonsterVantieu((Monster)mt, (Char)p)) {
            return;
        }
        byte _type = skill;
        int _level = p.skill[_type] + p.addMoreLevelSkill[_type];
        if (_level <= 0) {
            _level = p.addMoreLevelSkill[_type];
        }
        if (_level == 0 || !Map.inRangeSkill((LiveActor)p, (LiveActor)mt, (int)MAX_RANGE_CHAR[p.charClass])) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - p.timeLastUseSkills[_type] < (long)(CharManager.SKILL_COOLDOWN[p.charClass][_type][_level] * 100)) {
            return;
        }
        if (mt.isRuongMaquai()) {
            if (p.itemVukhiThoiTrang == null || !p.itemVukhiThoiTrang.isGayGiangSinh()) {
                return;
            }
            if (mt.getInCountry() != p.inCountry) {
                p.sendMessage(MessageCreator.createMsgChat((int)p.id, (String)"Kh\u00f4ng th\u1ec3 \u0111\u00e1nh r\u01b0\u01a1ng c\u1ee7a l\u00e0ng kh\u00e1c."));
                return;
            }
        }
        int hphut = 0;
        if (p.haveHutHp() > 0 && p.hp < p.maxhp && !mt.isNgocRong() && !mt.isRuongMaquai()) {
            int hp = p.haveHutHp();
            hphut = hp;
            if (mt.hp < hphut) {
                hphut = mt.hp;
            }
            p.hp += hphut;
            p.calculatorHPMP();
            MessageCreator.createMsgUseHpMP((Char)p, (int)hp, (int)1);
        }
        p.timeLastUseSkills[_type] = now;
        buffAttack = p.getBuffEffAttack();
        if (mt.resistThroughArmor()) {
            buffAttack = -1;
        }
        int damage = p.attackDam((LiveActor)mt, (int)_type, _level, buffAttack);
        if (mt.isNgocRong()) {
            if (damage == 1) {
                if (p.potions[73] == 0) {
                    p.sendMessage(MessageCreator.createMsgChat((int)p.id, (String)"Kh\u00f4ng c\u00f3 t\u00fai nh\u1eb7t ng\u1ecdc"));
                } else {
                    mt.onDropItem(this.map, p);
                }
            } else if (mt.isDead) {
                mt.sendActorDie(p);
            }
            return;
        }
        if (mt.haveDodge()) {
            damage = 0;
            buffAttack = -1;
        }
        if (mt.isMonsterVantieu() && damage >= mt.maxhp / 100) {
            damage = mt.maxhp / 100 + Map.r.nextInt() % 100;
        }
        damage *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
        boolean critSv = p.havecrit();
        boolean baokich = p.haveBaoKich();
        if (com.htbb.pokemon.LauDai.bat()) {
            // GD2 6.1: chi mang x1,5, bo bao kich x4 (chi con o boss "pha giap").
            if (critSv || baokich) {
                // gioi han chi so: x1,5 + ST chi mang (tran x2,0) qua tkNhanChiMang; tat: x1,5 phang nhu cu
                damage = ChiSoTrangBiGame.bat() ? p.tkNhanChiMang(damage, 2)
                        : damage * com.htbb.pokemon.SatThuongLauDai.CHI_MANG_PHAN_TRAM / 100;
                effect = 2;
                p.lauDaiCo |= com.htbb.pokemon.SatThuongLauDai.CO_CHI_MANG;
            }
            damage = BossLauDai.satThuongVaoBoss(p, mt, damage);
            if (BossLauDai.laBoss(mt)) {
                damage = VatPhamDungLauDai.vaoBoss(p, damage); // Thuoc san trum
            }
        } else if (baokich) {
            damage *= 4;
            effect = 4;
        } else if (critSv) {
            damage *= 2;
            effect = 2;
            if (p.petUsing != null) {
                long pcLienKich = p.petUsing.getLienKich();
                damage = (int)((long)damage + (long)damage * pcLienKich / 100L);
            }
        }
        if (_level > p.skill[_type] + p.addMoreLevelSkill[_type]) {
            return;
        }
        short mplost = CharManager.SKILL_MP[p.charClass][_type][_level];
        if (p.mp + p.percentBuff[1] < mplost) {
            return;
        }
        p.mp -= mplost;
        if (p.mp <= 0) {
            p.mp = 0;
        }
        int getXp = mt.getXpReceive(damage);
        ahp = damage / CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
        mt.hp -= damage + hphut;
        BossTrungThu.ghiNhanSatThuong(mt, p, damage + hphut);
        if (damage > 0 && mt.haveBackDam()) {
            int backdam = mt.getBackDam(damage);
            Message mbd = MessageCreator.createMsgBuffEffect((int)5, (int)mt.cat, (LiveActor)p, (int)backdam, (int)0, (int)-1);
            p.sendMessage(mbd);
            p.sendToNearPlayer(mbd);
        }
        Message m = null;
        if (p.charthanthu != null && mt.hp > 0) {
            Vector<LiveActor> target = new Vector<LiveActor>();
            target.add((LiveActor)mt);
            p.charthanthu.doAttack(target);
            int damthanthu = p.getDamtThanThu((LiveActor)mt);
            getXp += mt.getXpReceive(damthanthu);
            mt.hp -= damthanthu;
            BossTrungThu.ghiNhanSatThuong(mt, p, damthanthu);
        }
        if (mt.hp > 0) {
            if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry && p.killer < 200) {
                p.killer = (short)(p.killer + 200);
                msg = new Message(67);
                msg.dos.writeShort(p.id);
                msg.dos.writeByte(1);
                msg.dos.writeShort(p.killer);
                p.sendMessage(msg);
                p.sendToNearPlayer(msg);
            }
            if (mt.target == null) {
                mt.target = p;
            }
            if (ahp > 0) {
                p.buffAttackSkill(damage, (LiveActor)mt);
            }
            if (getXp > 0) {
                int totalXp;
                int delta;
                int dxp;
                int x2Player = p.getX2();
                if (TeamServer.isDouble) {
                    x2Player = 0;
                }
                if ((dxp = Map.rand10((int)getXp)) == 0) {
                    dxp = 1;
                }
                // Cung bug tu chia cho chinh no nhu tren, xem doAttackMultiTarget o tren.
                int[] downPercent = new int[]{1, 5, 10, 20};
                short targetLv = p.getLevel();
                if (targetLv < 40) {
                    downPercent = new int[]{1, 2, 3, 4};
                }
                if ((delta = targetLv - mt.level) > 0) {
                    int a = delta / 4;
                    if (targetLv < 40) {
                        a = delta / 6;
                    }
                    if (a > 3) {
                        a = 3;
                    }
                    if ((dxp /= ExpLauDai.chiaCu(downPercent[a])) <= 0) {
                        dxp = 1;
                    }
                }
                if ((totalXp = dxp) > 0) {
                    // GD2 3.4: bat co sv.lau_dai -> HAM DUY NHAT ExpLauDai.traoHaQuai (thay ban sao duoi).
                    if (com.htbb.pokemon.LauDai.bat()) {
                        ExpLauDai.traoHaQuai(p, mt, totalXp, "Region:1560");
                    } else {
                    int newxp = Map.calculatorXpParty((Char)p, (int)totalXp);
                    if (newxp != totalXp) {
                        int nUser = p.party.userParty.size();
                        // GD2 1.6: chia theo SO NGUOI THAT (ban cu ep nUser = 5 khi > 1 nguoi).
                        int xpReceive = Map.chiaExpDoi(newxp, 80, nUser, 1);
                        int maxLv = p.lvDetail.lv;
                        int i = 0;
                        while (i < p.party.userParty.size()) {
                            Char pp = (Char)p.party.userParty.get(i);
                            if (pp.id != p.id && p.near((Actor)pp, 320) && pp.mapID == p.mapID && pp.inCountry == p.inCountry && pp.region == p.region) {
                                int dlv = Map.abs((int)(maxLv - pp.lvDetail.lv));
                                int temp = 1;
                                temp = dlv <= 5 ? xpReceive : (dlv <= 10 ? xpReceive / 5 : (dlv <= 20 ? xpReceive / 10 : (dlv <= 30 ? xpReceive / 15 : xpReceive / 20)));
                                if (temp == 0) {
                                    temp = 1;
                                }
                                if (pp.hp > 0) {
                                    temp *= Map.doubleALL;
                                    temp = Monster.apDungGiamExpTheoCapNV(temp, pp);
                                    temp = pp.expReceive(temp);
                                    Map.addXPForChar((Char)pp, (long)(temp + pp.getEffSkillClan(0) * temp / 100), (boolean)false, (String)"Region4");
                                }
                            }
                            ++i;
                        }
                        xpReceive = Map.chiaExpDoi(newxp, 20, 1, Map.doubleALL);
                        xpReceive = Monster.apDungGiamExpTheoCapNV(xpReceive, p);
                        xpReceive = p.expReceive(xpReceive);
                        Map.addXPForChar((Char)p, (long)(xpReceive + p.getEffSkillClan(0) * xpReceive / 100), (boolean)false, (String)"Region5");
                    } else {
                        totalXp *= Map.doubleALL;
                        totalXp = Monster.apDungGiamExpTheoCapNV(totalXp, p);
                        totalXp = p.expReceive(totalXp);
                        Map.addXPForChar((Char)p, (long)(totalXp + p.getEffSkillClan(0) * totalXp / 100), (boolean)false, (String)"Region6");
                    }
                    }
                }
            }
            m = new Message(9);
            m.dos.writeShort(p.id);
            m.dos.writeShort(mt.id);
            m.dos.writeByte(skill);
            m.dos.writeInt(ahp);
            m.dos.writeInt(mt.hp);
            m.dos.writeByte(effect);
            m.dos.writeByte(CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0 ? _level - 1 : 0]);
            m.dos.writeByte(buffAttack);
            m.dos.writeByte(_level);
            // GD2 6.5: goi kem -95 (co chi mang/khac/STAB) - client cu bo qua.
            m.kemTheo = GoiLauDaiGame.don(p, mt, ahp);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            p.buffSkillKham((LiveActor)mt);
            p.charHireAttackDam((LiveActor)mt, (int)_type, _level, buffAttack);
        } else {
            if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry) {
                p.killer = (short)(p.killer + 200);
                msg = new Message(67);
                msg.dos.writeShort(p.id);
                msg.dos.writeByte(1);
                msg.dos.writeShort(p.killer);
                p.sendMessage(msg);
                p.sendToNearPlayer(msg);
            }
            Vector<Actor> droplist = new Vector<Actor>();
            int deltlv = Map.abs((int)(p.lvDetail.lv - mt.level));
            if (p.killer > 0 && p.isKiller && deltlv <= 10) {
                p.killer = (short)(p.killer - 1);
                boolean bl = p.isKiller = p.killer > 0;
                if (!p.isKiller) {
                    p.nPKill = 0;
                    p.timeKiller = 0L;
                }
                Message mm = new Message(67);
                mm.dos.writeShort(p.id);
                mm.dos.writeByte(p.isKiller ? 1 : 0);
                mm.dos.writeShort(p.killer);
                p.sendMessage(mm);
                p.sendToNearPlayer(mm);
                mm.cleanup();
            }
            mt.hp = 0;
            droplist = mt.onDropItem(this.map, p);
            try {
                int totalXp;
                int delta;
                int dxp = Map.rand10((int)mt.xp);
                if (dxp == 0) {
                    dxp = 1;
                }
                // Cung bug tu chia cho chinh no nhu tren, xem doAttackMultiTarget o tren.
                int[] downPercent = new int[]{1, 5, 10, 20};
                short targetLv = p.getLevel();
                if (targetLv < 40) {
                    downPercent = new int[]{1, 2, 3, 4};
                }
                if ((delta = targetLv - mt.level) > 0) {
                    int a = delta / 4;
                    if (targetLv < 40) {
                        a = delta / 6;
                    }
                    if (a > 3) {
                        a = 3;
                    }
                    if ((dxp /= ExpLauDai.chiaCu(downPercent[a])) <= 0) {
                        dxp = 1;
                    }
                }
                if ((totalXp = dxp) > 0) {
                    // GD2 3.4: bat co sv.lau_dai -> HAM DUY NHAT ExpLauDai.traoHaQuai (thay ban sao duoi).
                    if (com.htbb.pokemon.LauDai.bat()) {
                        ExpLauDai.traoHaQuai(p, mt, totalXp, "Region:1666");
                    } else {
                    int newxp = Map.calculatorXpParty((Char)p, (int)totalXp);
                    if (newxp != totalXp) {
                        int nUser = p.party.userParty.size();
                        // GD2 1.6: chia theo SO NGUOI THAT (ban cu ep nUser = 5 khi > 1 nguoi).
                        int xpReceive = Map.chiaExpDoi(newxp, 80, nUser, 1);
                        int maxLv = p.lvDetail.lv;
                        int i = 0;
                        while (i < p.party.userParty.size()) {
                            Char pp = (Char)p.party.userParty.get(i);
                            if (pp.id != p.id && p.near((Actor)pp, 320) && pp.mapID == p.mapID && pp.inCountry == p.inCountry && pp.region == p.region) {
                                int dlv = Map.abs((int)(maxLv - pp.lvDetail.lv));
                                int temp = 1;
                                temp = dlv <= 5 ? xpReceive : (dlv <= 10 ? xpReceive / 5 : (dlv <= 20 ? xpReceive / 10 : (dlv <= 30 ? xpReceive / 15 : xpReceive / 20)));
                                if (temp == 0) {
                                    temp = 1;
                                }
                                if (pp.hp > 0) {
                                    temp *= Map.doubleALL;
                                    temp = Monster.apDungGiamExpTheoCapNV(temp, pp);
                                    temp = pp.expReceive(temp);
                                    Map.addXPForChar((Char)pp, (long)(temp + pp.getEffSkillClan(0) * temp / 100), (boolean)false, (String)"Region7");
                                }
                            }
                            ++i;
                        }
                        xpReceive = Map.chiaExpDoi(newxp, 20, 1, Map.doubleALL);
                        xpReceive = Monster.apDungGiamExpTheoCapNV(xpReceive, p);
                        xpReceive = p.expReceive(xpReceive);
                        Map.addXPForChar((Char)p, (long)(xpReceive + p.getEffSkillClan(0) * xpReceive / 100), (boolean)false, (String)"Region8");
                    } else {
                        totalXp *= Map.doubleALL;
                        totalXp = Monster.apDungGiamExpTheoCapNV(totalXp, p);
                        totalXp = p.expReceive(totalXp);
                        Map.addXPForChar((Char)p, (long)(totalXp + p.getEffSkillClan(0) * totalXp / 100), (boolean)false, (String)"Region9");
                    }
                    }
                }
            }
            catch (Exception dxp) {
                // empty catch block
            }
            try {
                m = new Message(17);
                m.dos.writeShort(p.id);
                m.dos.writeShort(mt.id);
                m.dos.writeByte(skill);
                m.dos.writeInt(ahp);
                m.dos.writeByte(effect);
                m.dos.writeByte(droplist.size());
                if (droplist.size() > 0) {
                    for (Actor e : droplist) {
                        Map.writeActorPos((Message)m, (Actor)e, (byte)0);
                    }
                }
                byte xx2 = CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0 ? _level - 1 : 0];
                m.dos.writeByte(xx2);
                m.dos.writeByte(buffAttack);
                m.dos.writeByte(_level);
                p.sendMessage(m);
                p.sendToNearPlayer(m);
                if (p.receiveQuest && QuestTemplate.QUEST_TYPE[p.questID - 1] == 0) {
                    p.checkFinsishQuest((int)mt.getType(), -1, -1);
                }
            }
            catch (Exception e) {
                System.out.println("loi gui thong tin monsterdie ");
            }
        }
        if (mt.hp <= 0) {
            if (!mt.isCopy() || !mt.isBoss) {
                if (mt.isBoss) {
                    mt.bornTime = System.currentTimeMillis() + 86400000L;
                    mt.setTimeReBornInEvent(mt.bornTime);
                    Database.instance.saveEvent(Map.event.getInfo());
                    Map.removeBossLocation((int)1);
                } else {
                    mt.setTimeReBorn();
                }
            }
            mt.isDead = true;
            mt.target = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void killAllMonster(Vector<Hashtable<Short, Monster>> monsters, int country) {
        Vector<Hashtable<Short, Monster>> vector = monsters;
        synchronized (vector) {
            short i = (short)country;
            Vector<Char> players = this.allPlayers.get(i);
            if (players.size() > 0) {
                int j = 0;
                while (j < players.size()) {
                    try {
                        if (players.get((int)j).isBot == -1) {
                            this.playerExit(players.get(j));
                            this.map.move2Map(players.get(j), players.get((int)j).x / 16, players.get((int)j).y / 16, players.get((int)j).mapID, (int)players.get((int)j).inCountry);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    ++j;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reSetAllMonster(int country) {
        Vector<Hashtable<Short, Monster>> vector = this.monsters;
        synchronized (vector) {
            Vector<Hashtable<Short, Monster>> monsters = this.monsters;
            short i = (short)country;
            this.temp_monsters.get(i).clear();
            Collection<Monster> listmonster = monsters.get(i).values();
            for (Monster mt : listmonster) {
                if (!mt.isBoss) {
                    if (Map.random((int)100) < 50) {
                        Monster_Clone mons = new Monster_Clone((byte)1);
                        mons.map = mt.map;
                        mons.hp = mt.hp;
                        mons.maxhp = mt.maxhp;
                        mons.xp = mt.xp;
                        mons.xpClone = mt.getMonsterTemplate().rcvXp;
                        mons.maxxp = mt.getMonsterTemplate().rcvXp;
                        mons.default_x = mt.default_x;
                        mons.default_y = mt.default_y;
                        mons.defend_magic = mt.defend_magic;
                        mons.defend_physic = mt.defend_physic;
                        mons.attack = mt.attack;
                        mons.attackDelay = mt.attackDelay;
                        mons.idTemplate = 114;
                        mons.he = mons.heKhiSinh(mt.he);
                        mons.inCountry = mt.inCountry;
                        mons.x = mt.x;
                        mons.y = mt.y;
                        mons.toX = mt.toX;
                        mons.toY = mt.toY;
                        mons.accurate = mt.accurate;
                        mons.dodge = mt.dodge;
                        mons.bornTime = mt.bornTime;
                        mons.dmove = mt.dmove;
                        mons.moveDelay = mt.moveDelay;
                        mons.id = mt.id;
                        mons.setType((int)mons.getMonsterTemplate().type);
                        mons.level = mt.level;
                        this.temp_monsters.get(i).put(mons.id, (Monster)mons);
                        continue;
                    }
                    this.temp_monsters.get(i).put(mt.id, mt);
                    continue;
                }
                this.temp_monsters.get(i).put(mt.id, mt);
            }
        }
    }

    private short getIDITEM() {
        return this.map.getIDITEM();
    }

    public void removeDynamicMonster(Monster m, int country) {
        this.tempRemoveMonster.get(country).add(m);
    }

    public void addMonsterDynamic(Monster m, int country) {
        this.tempMonster.get(country).add(m);
    }

    public void addMonster(short id, Monster mons, int country) {
        this.monsters.get(country).put(id, mons);
    }

    public void playerJoin(Char player) {
        this.allPlayers.get(player.inCountry).add(player);
        player.nNpc = 0;
        player.map = this.map;
        player.region = this.idRegion;
        this.danhThucNhip();
    }

    public void playerExit(Char p) {
    }

    public void removePlayer(int country, Char p) {
        this.allPlayers.get(country).remove(p);
    }

    public void addPlayerMessage(Char p, Message message) {
        this.timeExist = System.currentTimeMillis();
        this.allPlayerMessages.get(p.inCountry).add(new PlayerMessage(p, message));
        this.notifyMap();
        com.htbb.runtime.VongLapChung.Lan lan = p.inCountry == 0 ? this.lan0 : (p.inCountry == 1 ? this.lan1 : null);
        if (lan != null) {
            lan.kich();
        }
    }

    public void addEffectAutoInMap(int country, Char p, int region, int x, int y, int idEff, long time) {
        EffectBuff eff = new EffectBuff(idEff, time);
        eff.idCharOwner = p.id;
        eff.x = (short)p.x;
        eff.y = (short)p.y;
        p.ALL_BUFF_INMAP.add(eff);
        this.EFFECT_AUTO.get(country).add(eff);
    }

    public Vector<EffectBuff> getAllEffectAuto(int inCountry) {
        return this.EFFECT_AUTO.get(inCountry);
    }

    public void addPlayer(int country, Char p) {
        this.allPlayers.get(country).add(p);
        if (p != null && p.isBot == -1) {
            this.danhThucNhip();
        }
    }

    public void onOffSkelonton(boolean onoff) {
        this.isSkelonton = onoff;
    }

    public Vector<Char> getAllPlayer(int inCountry) {
        return this.allPlayers.get(inCountry);
    }

    public Hashtable<Short, Monster> getAllMons(int country) {
        return this.monsters.get(country);
    }

    public Monster getMonster(short id, int country) {
        Monster m = this.monsters.get(country).get(id);
        if (this.isSkelonton) {
            m = this.temp_monsters.get(country).get(id);
        }
        return m;
    }

    public boolean isFull() {
        return false;
    }

    public void stop() {
        this.running = false;
        this.dungVongLap();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyMap() {
        this.kichNeuCoGoi();
        Object object;
        try {
            object = this.LOCK;
            synchronized (object) {
                this.LOCK.notify();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            object = this.LOCK1;
            synchronized (object) {
                this.LOCK1.notify();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            object = this.LOCK2;
            synchronized (object) {
                this.LOCK2.notify();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public boolean timeOut() {
        return this.idRegion > 10 && System.currentTimeMillis() - this.timeExist >= 54000000L;
    }

    public byte getStateRegion() {
        return 1;
    }

    public void removeMonster(int country, int idMons) {
        this.monsters.get(country).remove((short)idMons);
    }

    private Char getChar(short id) {
        return CharManager.instance.getByCharID(id);
    }

    // Ham nay CFR decompile khong ra cau truc hop le (do-while/while dang bytecode
    // goto). Da doi chieu lai logic voi Map.getAllNearActor() (ban tien hoa hon cua
    // cung ham) de dung lai chinh xac ban goc don gian hon nay: neu khong du sl ung
    // vien thi tra ve tat ca (+ attacker), neu du thi chon ngau nhien sl phan tu roi
    // moi them attacker vao.
    public Vector<LiveActor> getAllNearActor(Char from, int cat, int sl, LiveActor attacker) {
        Vector<LiveActor> ac = new Vector<LiveActor>();
        Vector<LiveActor> c = new Vector<LiveActor>();
        if (cat == 0) {
            int i = 0;
            while (i < from.nearChars.size()) {
                Char pp = this.map.getPlayerByID(((Short)from.nearChars.get(i)).shortValue());
                if (pp != null && pp.isBot == -1) {
                    if (from.mapID == 201 && pp.idClan != from.idClan || pp.killer > 0) {
                        c.add((LiveActor)pp);
                    } else if (pp.myCountry != from.myCountry) {
                        c.add((LiveActor)pp);
                    }
                }
                ++i;
            }
        } else if (cat == 1) {
            java.util.Collection<Monster> listmonster = this.monsters.get(from.inCountry).values();
            for (Monster mons : listmonster) {
                if (mons.isDead || !Map.near((Actor)from, (Actor)mons) || mons.isMonsterVantieu()) continue;
                c.add((LiveActor)mons);
            }
        }
        if (c.size() < sl) {
            if (attacker.hp > 0) {
                c.add(attacker);
            }
            return c;
        }
        while (ac.size() < sl) {
            ac.add(c.remove(Map.r.nextInt(c.size())));
        }
        if (attacker.hp > 0) {
            ac.add(attacker);
        }
        return ac;
    }
}
