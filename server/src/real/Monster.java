// 
// Decompiled by Procyon v0.6.0
// 
package real;

import com.htbb.pokemon.HePokemon;

import data.ItemQuest;
import server.TeamServer;
import data.GemItem;
import java.util.Vector;
import data.Database;
import io.Message;
import java.util.Random;
import static real.Char.getDayTime;

public class Monster extends LiveActor {

    public long timeAddBuff;
    public static Random r;
    public int idTemplate;
    /** Loai rieng cua con quai nay (0 = loai cua mau), bac luot gap; real.LoaiQuaiGame (goi -125). */
    public int dexRieng;
    public int bacRieng;
    /** Da tung mang loai rieng (khi ve lai loai mau van gui -125 dex 0 de client xoa ghi de). */
    public boolean daCoLoaiRieng;
    /** Con quai mang loai rieng nay la ban SAC MAU (shiny) - nguoi choi client moi thay, bat duoc shiny (LoaiQuaiGame). */
    public boolean shinyRieng;
    public int default_x;
    public int default_y;
    public int toX;
    public int toY;
    public static final int TILE_SIZE = 16;
    public long lastTimeMove;
    public long moveDelay;
    public long lastTimeAttack;
    public long attackDelay;
    public int dmove;
    public boolean isDead;
    public boolean isBoss;
    /**
     * Quai trieu hoi mot lan (boss Gym / boss the gioi / de cua boss - BossLauDai): chet hay bi go thi KHONG hoi sinh
     * (Monster.update), Region / MapVongLap go khoi bang quai qua hang tempRemoveMonster (BossLauDai.goKhoiBang). Truoc
     * bao loi 24/10/2026 boss Gym bi ha xong 6 giay sau hien lai day mau (timeReBorn) thanh boss mo coi khong ai danh duoc.
     */
    public volatile boolean khongHoiSinh;
    /**
     * Boss Gym: so thu tu Gym (data_noi_dung_gym.stt) - dat o BossLauDai.datGym, giu sau khi BossLauDai.khiChet go
     * trang thai (EXP ket lieu ExpLauDai.expHaBoss doc sau do). 0 = khong phai boss Gym.
     */
    public volatile int gymStt;
    public long bornTime;
    public Char target;
    public int level;
    public boolean moved;
    public byte typeAttack;
    public byte belongCountry;
    public byte posTower;
    byte speed;
    long waitTime;
    int maxHit;
    public byte inCountry;
    public byte idregion;
    public static final byte TYPE4 = 0;
    public static final byte TYPE2 = 1;
    public static final byte TYPE1 = 2;
    public static final byte TYPE3 = 3;
    public static final byte TYPE0 = 4;
    public static int BOSS_NGUOI_TUYET;
    public static int timeReBorn;
    public static boolean isCountBoss = true; // Thêm biến static để điều khiển việc đếm boss
    public boolean isBoss() { // thêm cái này 
    return false;
}


    static {
        Monster.r = new Random();
        Monster.BOSS_NGUOI_TUYET = 90;
        Monster.timeReBorn = 6000;
    }

    public Monster(final Map mapLiveIn, final MonsterTemplate template, final int x, final int y, final int country) {
        super((byte) 1);
        this.timeAddBuff = System.currentTimeMillis() + 10000L;
        this.isBoss = false;
        this.target = null;
        this.typeAttack = 0;
        this.speed = 1;
        this.maxHit = 0;
        this.idregion = 0;
        this.idTemplate = template.id;
        this.inCountry = (byte) country;
        this.map = mapLiveIn;
        this.setType(template.type);
        this.he = template.he;
        // Pokemon: data_monster.he/he2 la he Pokemon 0..17. Giu rieng o
        // hePokemon/he2 (dung tinh khac he), con truong he doi ve ngu hanh 0..4 vi client
        // cu va cac lop trong lib/htbb-core.jar (Dun, MapVanTienTran...) gui/dung no nhu
        // ngu hanh. Tat co thi khong dong vao gi.
        this.hePokemon = template.he;
        this.he = (byte) HePokemon.nguHanhCu(template.he);
        this.he2 = template.he2;
        final int maxhp = template.maxhp;
        this.maxhp = maxhp;
        this.hp = maxhp;
        this.maxxp = template.rcvXp;
        this.xp = Monster.rcvXpSauGiam(template);
        this.attack = template.attack;
        this.defend_physic = template.defend;
        this.accurate = template.accurate;
        this.dodge = template.dodge;
        this.default_x = x;
        this.x = x;
        this.toX = x;
        this.default_y = y;
        this.y = y;
        this.toY = y;
        short[] mdelay = {1500, 2000, 2500, 1500, 1800, 1000};
        this.moveDelay = mdelay[Map.r.nextInt(mdelay.length)];
        mdelay = new short[]{3000, 5000, 4000};
        this.attackDelay = mdelay[Map.r.nextInt(mdelay.length)];
        this.dmove = template.dmove;
    }

    public Monster(final byte cat) {
        super(cat);
        this.timeAddBuff = System.currentTimeMillis() + 10000L;
        this.isBoss = false;
        this.target = null;
        this.typeAttack = 0;
        this.speed = 1;
        this.maxHit = 0;
        this.idregion = 0;
    }

    /** He Pokemon 1 lay tu template luc sinh (data_monster.he). */
    public byte hePokemon;

    @Override
    public int hePokemon1() {
        return this.hePokemon;
    }

    /**
     * Q9: he cua quai lay tu template, KHONG random. Tra ve
     * ngu hanh 0..4 tuong ung (truong he van la ngu hanh, xem constructor).
     * Dung hePokemon da chep tu CHINH template truyen vao constructor - KHONG tra lai
     * getMonsterTemplate(): quai Van Tien Tran lay template tu MapVanTienTran.monsterTemplates,
     * con getMonsterTemplate() tra theo Map.monsterTemplates.
     */
    @Override
    public byte heKhiSinh(final byte heNgauNhien) {
        return (byte) HePokemon.nguHanhCu(this.hePokemon);
    }

    /**
     * 0.9.5: quai hoi sinh voi loai KHAC (he sinh thai / alpha). Client giu actor cu cung id (tu hoi sinh
     * tai cho theo timeLive) va lenh 4 voi id da co chi doi vi tri, khong doi type -> van ve loai cu (vd ve
     * Wooper trong khi may chu la Litwick: Huc hien "Gan nhu vo hieu"). Gui lenh 20 (go actor) va bo id khoi
     * nearMons cua nguoi choi cung map/nuoc/vung: vong Region gui lai lenh 4 voi type moi, client tao actor moi.
     */
    void baoDoiLoai() {
        try {
            if (this.map == null) {
                return;
            }
            final java.util.Vector<Char> ds = this.map.getAllPlayer(this.inCountry, this.region);
            if (ds == null) {
                return;
            }
            final Short id = Short.valueOf(this.id);
            for (final Char p : new java.util.ArrayList<Char>(ds)) {
                if (p == null || p.isBot != -1) {
                    continue;
                }
                if (p.nearMons != null) {
                    p.nearMons.remove(id);
                }
                final Message m = new Message(20);
                m.dos.writeByte(this.cat);
                m.dos.writeShort(this.id);
                p.sendMessage(m);
            }
        } catch (final Exception e) {
            // khong de loi bao doi loai lam hong vong hoi sinh
        }
    }

    public MonsterTemplate getMonsterTemplate() {
        return Map.monsterTemplates.get(this.idTemplate);
    }

    private void moveTo(final int toX, final int toY) {
        final int d = Math.abs(this.x - toX) + Math.abs(this.y - toY);
        this.waitTime = System.currentTimeMillis() + d / this.speed * 50;
        this.x = toX;
        this.y = toY;
    }

    public void moveOld() {
        if (this.isMaterialMons() || this.isCongThanh() || this.isLienHoaTru()) {
            return;
        }
        if (!this.isBoss && this.map != null && this.map.type != null) {
            this.moveOldVaCham();
            return;
        }
        if (this.target != null) {
            int xx = this.x;
            int yy = this.y;
            if (Math.abs(this.x - this.target.x) > 32) {
                if (this.x < this.target.x) {
                    xx = this.x + 32;
                } else {
                    xx = this.x - 32;
                }
            }
            if (Math.abs(this.y - this.target.y) > 32) {
                if (this.y < this.target.y) {
                    yy = this.y + 16;
                } else {
                    yy = this.y - 32;
                }
            }
            if (!this.canMove(xx, yy)) {
                this.x = xx;
                this.y = yy;
            } else {
                this.target = null;
                this.x = this.default_x;
                this.y = this.default_y;
            }
        } else {
            int xx = this.x + Monster.r.nextInt() % this.dmove;
            int yy = this.y + Monster.r.nextInt() % this.dmove;
            if (!this.canMove(xx, yy)) {
                this.x = xx;
                this.y = yy;
            } else {
                if (xx < this.x) {
                    xx = this.x + Math.abs(this.x - xx) + 48;
                } else {
                    xx = this.x - Math.abs(this.x - xx) - 48;
                }
                if (yy < this.y) {
                    yy = this.y + Math.abs(this.y - yy) + 48;
                } else {
                    yy = this.y - Math.abs(this.y - yy) - 48;
                }
                if (!this.canMove(xx, yy)) {
                    this.x = xx;
                    this.y = yy;
                } else {
                    this.target = null;
                    this.x = this.default_x;
                    this.y = this.default_y;
                }
            }
        }
        if (this.isBoss) {
            if (Math.abs(this.x - this.default_x) > 320 || Math.abs(this.y - this.default_y) > 320) {
                this.target = null;
                this.x = this.default_x;
                this.y = this.default_y;
            }
        } else {
            final int range = 120;
            if (this.getMonsterTemplate().id == 46 && (Math.abs(this.x - this.default_x) >= 96 || Math.abs(this.y - this.default_y) >= 96)) {
                this.x = this.default_x;
                this.y = this.default_y;
                this.target = null;
            }
            if (Math.abs(this.x - this.default_x) >= range || Math.abs(this.y - this.default_y) >= range) {
                this.target = null;
                this.x = this.default_x;
                this.y = this.default_y;
            }
        }
    }

    public void move() {
        final long now = System.currentTimeMillis();
        this.moved = false;
        if (now - this.lastTimeMove > this.moveDelay) {

            if (this.getMonsterTemplate().move == 1) {

                if (!Map.isNewVersion || this.isBoss || this.getMonsterTemplate().id == Map.idGhost || this.getMonsterTemplate().move == 0 || this.isMaterialMons()) {
                    this.moveOld();

                } else if (this.target != null) {
                    int xx = this.x;
                    int yy = this.y;
                    // 0.9.3: duoi theo duong di duoc (BFS trong xich) - cu nhay toi canh muc tieu xuyen tuong
                    // (canMove luon "khong chan" khi co target). Khong co duong trong xich = bo muc tieu, ve nha.
                    final int[] buoc = this.buocDuoi();
                    if (buoc == null) {
                        this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.x = this.default_x;
                        this.y = this.default_y;
                        final Message msg = new Message(4);
                        this.target.writeActorPos(msg, this);
                        this.target.sendMessage(msg);
                        this.target.sendToNearPlayer(msg);
                        this.target = null;
                        return;
                    }
                    if (buoc != com.htbb.pokemon.DiChuyenQuai.DA_TOI) {
                        xx = buoc[0];
                        yy = buoc[1];
                    }
                    this.x = xx;
                    this.y = yy;
                    int range = 120;
                    if (this.getMonsterTemplate().id == 46 && (Math.abs(this.x - this.default_x) >= 96 || Math.abs(this.y - this.default_y) >= 96)) {
                        this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.x = this.default_x;
                        this.y = this.default_y;
                        final Message msg2 = new Message(4);
                        this.target.writeActorPos(msg2, this);
                        this.target.sendMessage(msg2);
                        this.target.sendToNearPlayer(msg2);
                        this.target = null;
                    }
                    if (this.isBoss) {
                        range = 1000;
                    }
                    if (Math.abs(this.x - this.default_x) >= range || Math.abs(this.y - this.default_y) >= range) {
                        this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.x = this.default_x;
                        this.y = this.default_y;
                        final Message msg2 = new Message(4);
                        this.target.writeActorPos(msg2, this);
                        this.target.sendMessage(msg2);
                        this.target.sendToNearPlayer(msg2);
                        this.target = null;
                    }
                }
            }
            this.lastTimeMove = now;
            this.moved = true;
        }
    }

    /** Luoi va cham cua map quai dang o (cung luat nguoi choi, xem com.htbb.pokemon.DiChuyenQuai). */
    public com.htbb.pokemon.DiChuyenQuai.Luoi luoiVaCham() {
        final Map m = this.map;
        if (m == null || m.type == null) {
            return null;
        }
        return com.htbb.pokemon.DiChuyenQuai.luoi(m.type, m.map, m.w, m.h);
    }

    /** Quai thuong (khong boss / khong dung yen) theo luat va cham moi. */
    boolean theoVaCham() {
        return !this.isBoss && !this.isMaterialMons() && this.getClass() == Monster.class;
    }

    private boolean daKiemSinh;

    /** Lan dau co ban do: diem sinh nam tren o chan / ngoai mep -> doi sang o di duoc gan nhat. */
    void kiemDiemSinh() {
        if (this.daKiemSinh) {
            return;
        }
        final com.htbb.pokemon.DiChuyenQuai.Luoi l = this.luoiVaCham();
        if (l == null) {
            return;
        }
        this.daKiemSinh = true;
        if (!this.theoVaCham()) {
            return;
        }
        final int[] o = com.htbb.pokemon.DiChuyenQuai.oGan(l, this.default_x, this.default_y, com.htbb.pokemon.DiChuyenQuai.BAN_KINH_SINH);
        if (o != null && (o[0] != this.default_x || o[1] != this.default_y)) {
            this.default_x = o[0];
            this.default_y = o[1];
            this.x = o[0];
            this.y = o[1];
            this.toX = o[0];
            this.toY = o[1];
        }
    }

    /** Buoc duoi muc tieu theo duong di duoc; null = khong co duong trong xich; DA_TOI = dung yen. */
    int[] buocDuoi() {
        final com.htbb.pokemon.DiChuyenQuai.Luoi l = this.luoiVaCham();
        if (l == null || this.target == null) {
            return com.htbb.pokemon.DiChuyenQuai.DA_TOI;
        }
        final int xich = this.isBoss ? 1000 : (this.getMonsterTemplate() != null && this.getMonsterTemplate().id == 46 ? 96 : 120);
        return com.htbb.pokemon.DiChuyenQuai.duoi(l, this.x, this.y, this.target.x, this.target.y, this.default_x, this.default_y,
                xich, com.htbb.pokemon.DiChuyenQuai.BUOC_DUOI);
    }

    /** moveOld cho quai thuong (ma, ...): lang thang / duoi theo va cham, xich 120 px quanh diem sinh. */
    private void moveOldVaCham() {
        if (this.target != null) {
            final int[] b = this.buocDuoi();
            if (b == null) {
                this.target = null;
                this.x = this.default_x;
                this.y = this.default_y;
                return;
            }
            if (b != com.htbb.pokemon.DiChuyenQuai.DA_TOI) {
                this.x = b[0];
                this.y = b[1];
            }
        } else {
            final int[] b = com.htbb.pokemon.DiChuyenQuai.langThang(this.luoiVaCham(), this.x, this.y, this.default_x, this.default_y,
                    this.dmove, 120, Monster.r);
            if (b != null) {
                this.x = b[0];
                this.y = b[1];
            }
        }
        final int xich = this.getMonsterTemplate() != null && this.getMonsterTemplate().id == 46 ? 96 : 120;
        if (Math.abs(this.x - this.default_x) >= xich || Math.abs(this.y - this.default_y) >= xich) {
            this.target = null;
            this.x = this.default_x;
            this.y = this.default_y;
        }
    }

    /** Cu (boss dung): true = o CHAN hoac dang co muc tieu. Quai thuong dung luoiVaCham(). */
    public boolean canMove(final int x, final int y) {
        try {
            return (this.map.type[(y >> 4) * this.map.w + (x >> 4)] & 0x2) == 0x2 && this.target == null;
        } catch (final Exception ex) {
            return true;
        }
    }

    public void attack() {
        if (this.freeze()) {
            return;
        }
        if (this.target != null && (!this.target.map.equals(this.map) || this.target.region != this.region || this.target.inCountry != this.inCountry)) {
            this.target = null;
            return;
        }
        if ((this.idTemplate >= 85 && this.idTemplate <= 89) || this.idTemplate == 36 || this.idTemplate == 37) {
            return;
        }
        // Boss Gym (1 dau 1) va de cua no chi danh nguoi thach dau (truoc 29/10/2026: boss Gym cua nguoi khac dung trong
        // lang danh ca nguoi di ngang - nguoi do lai khong gay duoc sat thuong vao no).
        if (this.target != null && !BossLauDai.chiDanhChu(this, this.target)) {
            final Char chu = BossLauDai.chuGym(this);
            if (chu == null) {
                this.boMucTieu();
                return;
            }
            this.target = chu; // quay ve nguoi thach dau
        }

        final long now = System.currentTimeMillis();
        // GD2 6.4/6.6: ngu/bang/te liet; boss pha 3 danh nhanh hon 30% (tat co: y nhu cu).
        if (com.htbb.pokemon.LauDai.bat() && TrangThaiGame.khongDanhDuoc(this)) {
            return;
        }
        if (now - this.lastTimeAttack > BossLauDai.treDanh(this, this.attackDelay)) {
            if (this.target != null && !this.chuDongDuoc(this.target, now)) {
                return; // quai xam / da du quai vay: giu muc tieu, chua danh (com.htbb.pokemon.ChuDongQuai)
            }
            this.lastTimeAttack = now;
            try {
                final Message m = new Message(10);
                m.dos.writeShort(this.id);
                m.dos.writeShort(this.target.id);
                int ahp = this.attackDam(this.target);
                ahp = this.target.subDam(this, ahp);
                ahp = this.target.checkHapthuSatThuong(ahp, this);
                ahp = this.target.checkGiamSatThuong(ahp);
                ahp = this.target.checkPassAttack(this, ahp);
                final int[] downPercent = {1, 2, 3, 4};
                final int targetLv = this.target.getLevel();
                final int delta = targetLv - this.level;
                if (delta > 0) {
                    int a = delta / 6;
                    if (a > 3) {
                        a = 3;
                    }
                    ahp /= downPercent[a];
                }
                // GD2 6.6: boss no x3, chieu lon bao truoc (-96/-98). Tat co: tra nguyen.
                ahp = BossLauDai.monsterDanh(this, this.target, ahp);
                ahp = VatPhamDungLauDai.satThuongNhan(this.target, ahp); // GD2b: X Phong thu / Bang do tap trung
                TrangThaiGame.quaiGay(this, this.target); // GD2b: trang thai len nguoi (tat co: khong lam gi)
                ahp -= (int) this.target.checkMagicShield(ahp);
                if (ahp <= 0) {
                    ahp = 10;
                }
                final boolean ismiss = this.attackMiss(this.target);
                if (ismiss) {
                    ahp = 0;
                } else {
                    this.target.buffAttackSkill(this, ahp);
                    if (this.hp <= 0) {
                        this.actorDie();
                    }
                    this.target.downDuarable();
                }
                int realdam = 0;
                if (ahp >= 32000) {
                    int defvat = this.target.defend_physic + this.target.percentBuff[0];
                    defvat += defvat * 5 / 100;
                    defvat += defvat * this.target.getEffSkillClanMember(1) / 100;
                    int defMa = this.target.defend_magic + this.target.getBuffDefCB(1, true);
                    defMa += defMa * 5 / 100;
                    defMa += defMa * this.target.getEffSkillClanMember(2) / 100;
                    int def = 0;
                    if (defMa < defvat) {
                        def = defvat;
                    } else if (defMa > defvat) {
                        def = defMa;
                    } else {
                        def = defvat;
                    }
                    int dam = this.attack;
                    final int deltaLV = this.level - this.target.lvDetail.lv;
                    if (deltaLV > 0) {
                        dam += dam * Map.abs(deltaLV / 5);
                        if (Map.abs(deltaLV) > 5 && Map.abs(deltaLV) <= 10) {
                            def /= 2;
                        } else if (Map.abs(deltaLV) > 10 && Map.abs(deltaLV) <= 15) {
                            def /= 3;
                        } else if (Map.abs(deltaLV) > 15) {
                            def /= 10;
                        }
                        dam += 70 * deltaLV;
                        def -= 20 * deltaLV;
                    } else if (deltaLV < 0) {
                        dam += 70 * deltaLV;
                    }
                    if (dam <= 0) {
                        dam = 5;
                    }
                    realdam = dam - def;
                    realdam = realdam * 120 / 100;
                    if (realdam <= 0) {
                        realdam = 5;
                    }
                    if (ahp > realdam) {
                        ahp = 10;
                    }
                }
                if (this.target.isAdmin) {
                    ahp = 0;
                }
                this.target.checkNewEffectItem(1, ahp / 10, this);
                m.dos.writeInt(ahp);
                final Char target = this.target;
                final int hpTruocDon = target.hp;
                target.hp -= Map.abs(ahp);
                if (this.target.map.isMapTrain() && this.target.isHieuUngCoLongDuongQua() && this.target.hp <= 0) {
                    this.target.hp = 1;
                }
                if (this.target.hp < 0) {
                    this.target.hp = 0;
                }
                GucGame.quaiDanh(target, this, hpTruocDon); // [GUC] 5 don cuoi
                m.dos.writeInt(this.target.hp);
                for (int i = 0; i < this.target.nearChars.size(); ++i) {
                    final Char p2 = this.target.map.getPlayerByID(this.target.nearChars.get(i));
                    if (p2 != null) {
                        p2.sendMessage(m);
                    }
                }
                if (this.target.hp <= 0) {
                    this.target.desTroy();
                    this.target.actorDie();
                    try {
                        long xp = this.target.lvDetail.getExp();
                        long xpLost = this.target.lvDetail.getXPLost(this.target.killer, this.target);
                        if (!this.target.isKiller) {
                            xpLost = 0L;
                        }
                        Database.instance.saveOrtherLog("", this.target.charname, String.valueOf(realdam) + "_" + ahp + "_" + this.getMonsterTemplate().name + "_" + Map.getNameMap(this.map.mapId) + "_" + this.region + "_" + this.target.region, "die");
                        xp -= xpLost;
                        final Char target2 = this.target;
                        target2.xpLost += xpLost;
                        final int currentlv = this.target.lvDetail.lv;
                        this.target.lvDetail.setExp(xp, this.target.oldLv, this.target.getName(), "monster");
                        if (this.target.lvDetail.lv <= 1) {
                            this.target.lvDetail.lv = 1;
                            this.target.lvDetail.percent = 0;
                        }
                        if (currentlv > this.target.lvDetail.lv) {
                            this.target.lvDetail.resetExp2Lv(currentlv, this.target.killer);
                            if (this.target.killer > 0) {
                                Database.instance.saveOrtherLog("", this.target.charname, "tut level do dang trong che do ds " + this.target.killer, "downlv");
                            }
                        }
                        this.target.actorDie();
                        this.target.calculateAttrib();
                        this.target.doSendProperties();
                    } catch (final Exception ex) {
                    }
                }
                this.target.sendMessage(m);
                m.cleanup();
            } catch (final Exception ex2) {
//                ex2.printStackTrace();
            }
        }
    }

    @Override
    public int getXpReceive(final int hpLost) {
        if (this.hp <= 0 || hpLost <= 0) {
            return 0;
        }
        int xpGet = 0;
        final int pc = hpLost * 100 / this.hp;
        if (pc >= 100) {
            return this.xp;
        }
        xpGet = pc * this.xp / 100;
        if (xpGet <= 0) {
            xpGet = 1;
        }
        this.xp -= xpGet;
        if (this.xp <= 0) {
            this.xp = 1;
        }
        return xpGet;
    }

    public void addXp2Char() {
    }

    public void initInfo() {
        if (this.khongHoiSinh) {
            return; // quai trieu hoi mot lan: khong dat lai mau / hoi sinh
        }
        this.bornTime = System.currentTimeMillis();
        this.isDead = false;
        this.timeOutPoinson = 0L;
        this.poinson = 0;
        try {
            this.maxhp = this.getMonsterTemplate().maxhp;
            this.hp = this.maxhp;
            this.xp = Monster.rcvXpSauGiam(this.getMonsterTemplate());
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println(String.valueOf(this.idTemplate) + " >>> TEMPLATE GATE");
        }
        this.tDelay = 0;
        this.target = null;
        this.x = this.default_x;
        this.y = this.default_y;
    }

    @Override
    public void update() {
        this.kiemDiemSinh();
        if (this.isBoss) {
            BossLauDai.nhipNiem(this); // chieu lon bao truoc no DUNG luc het gio niem (vi tri muc tieu luc do)
        }
        if (this.isDead && this.khongHoiSinh) {
            return; // boss Gym / the gioi / de: khong bao gio hoi sinh (cho go khoi bang quai)
        }
        if (this.isDead) {
            final long now = System.currentTimeMillis();
            if (now > this.bornTime && this.idTemplate != 83) {
                this.bornTime = now;
                this.isDead = false;
                this.timeOutPoinson = 0L;
                this.poinson = 0;
                // he sinh thai: co the doi loai (thoi tiet / ngay dem / mua / bung phat) TRUOC khi dat hp.
                final int loaiCu = this.idTemplate;
                com.htbb.pokemon.HeSinhThaiGame.khiHoiSinh(this);
                NoiDungLauDai.khiHoiSinh(this); // GD2b: alpha 1/50, quai Hoenn nam 2 (tat co: khong lam gi)
                // loai rieng tung con (6 bac, vung sinh thai + dai cap): giu mau, chi doi loai hien thi
                final boolean doiLoaiRieng = LoaiQuaiGame.khiHoiSinh(this);
                if (this.idTemplate != loaiCu || doiLoaiRieng) {
                    this.baoDoiLoai();
                }
                this.hp = this.maxhp;
                // EXP theo mau chi so (mau GOC cua o sinh khi he sinh thai doi loai - HeSinhThaiGame.mauChiSo)
                this.xp = Monster.rcvXpSauGiam(com.htbb.pokemon.HeSinhThaiGame.mauChiSo(this));
                this.tDelay = 0;
                this.target = null;
                this.x = this.default_x;
                this.y = this.default_y;
            }
            return;
        }
        if (this.dexRieng > 0) {
            LoaiQuaiGame.nhip(this); // 0.9.8: Pokemon hoang da khong ai giao chien -> bien mat
            if (this.isDead) {
                return;
            }
        }
        if (System.currentTimeMillis() - this.timeOutPoinson >= this.tDelay * 1000 && this.tDelay > 0) {
            this.getXpReceive(this.poinson);
            this.hp -= this.poinson;
            this.totalTime -= (byte) this.tDelay;
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
        final EffectBuff ef = this.hashEffBuff.get(EffectBuff.TRUNG_DOC);
        if (ef != null) {
            this.getXpReceive(ef.dam);
            this.hp -= ef.dam;
            if (this.hp <= 0) {
                this.actorDie();
            }
        }
        if (this.isCongThanh()) {
            this.target = null;
        }
        if (this.beStune) {
            if (System.currentTimeMillis() > this.timeBeStune) {
                this.beStune = false;
            }
            return;
        }
        if (this.target == null) {
            if (!this.beStune && !this.freeze()) {
                this.move();
            }
        } else {
            if (this.target.getSession() == null || (this.target.getSession() != null && this.target.getSession().exit)) {
                if (Map.isNewVersion) {
                    this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                    this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                    this.x = this.default_x;
                    this.y = this.default_y;
                }
                this.target = null;
                return;
            }
            if ((!this.target.map.equals(this.map) || this.target.region != this.region || this.target.inCountry != this.inCountry) && !this.map.isvanTienTran()) {
                if (Map.isNewVersion) {
                    this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                    this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                    this.x = this.default_x;
                    this.y = this.default_y;
                }
                this.target = null;
                return;
            }
            if (this.target.hp <= 0) {
                if (Map.isNewVersion) {
                    this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                    this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                    this.x = this.default_x;
                    this.y = this.default_y;
                }
                this.target = null;
                return;
            }
            if (this.target != null && this.target.hp > 0) {
                this.move();
                if (this.target != null
                        && ((this.getMonsterTemplate().move == 1 && Math.abs(this.target.x - this.x) <= 32 && Math.abs(this.target.y - this.y) <= 32) || (this.getMonsterTemplate().move == 0 && Math.abs(this.target.x - this.x) <= 96 && Math.abs(this.target.y - this.y) <= 96))) {
                    this.target.isCheckActiveBuffGiamSatThuong();
                    this.attack();
                }
            } else {
                this.move();
            }
        }
        this.updateEffKham();
    }

    public boolean isMonsterStand() {
        // MonsterTemplate.info chi co 0..138: mau moi 139.. (Hoenn nam 2, boss dot kich) khong phai quai dung yen.
        // Truoc day nem ArrayIndexOutOfBounds trong Monster.update -> quai Hoenn khong bao gio hien voi client.
        return this.idTemplate >= 0 && this.idTemplate < MonsterTemplate.info.length && MonsterTemplate.info[this.idTemplate][0] == 4;
    }

    public boolean isMaterialMons() {
        return (this.idTemplate >= 85 && this.idTemplate <= 89) || this.isMonsterStand();
    }

    public void sendActorDie(final Char p) {
        try {
            final Message m = new Message(90);
            m.dos.writeShort(this.id);
            m.dos.writeByte(this.cat);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
        } catch (final Exception ex) {
        }
    }

    @Override
    public void actorDie() {
        try {
            this.isDead = true;
            this.bornTime = System.currentTimeMillis() + Monster.timeReBorn;
            this.timeOutPoinson = 0L;
            this.poinson = 0;
            final Message m = new Message(90);
            m.dos.writeShort(this.id);
            m.dos.writeByte(this.cat);
            if (this.map != null) {
                this.map.sendAllPlayer(m, this.inCountry);
            }
            this.hashEffBuff.clear();
            this.AllEffBuff.removeAllElements();
        } catch (final Exception ex) {
        }
    }

    @Override
    public short getLevel() {
        return (short) this.level;
    }

    @Override
    public String getName() {
        return this.getMonsterTemplate().name;
    }

    public Item dropItemAnimal(final int lvChar, final int cl) {
        final int color = cl;
        final Vector<ItemTemplates> a = new Vector<ItemTemplates>();
        for (final ItemTemplates e : Map.itemTemplateCollection.get(5)) {
            final int lvfrom = lvChar - 10;
            final int lvTo = lvChar + 1;
            if (e.level <= lvTo && e.level >= lvfrom && Map.isWearingAnimal(e.type)) {
                a.add(e);
            }
        }
        if (a.size() == 0) {
            return null;
        }
        final ItemTemplates itemtemplate = a.get(Map.random(a.size()));
        Item it = null;
        it = new Item(itemtemplate, true, 0, 0, itemtemplate.id);
        if (itemtemplate.type != 15 && itemtemplate.type != 17) {
            it.magic_physic = (byte) Monster.r.nextInt(2);
            if (it.magic_physic == 0) {
                it.atb[6] = it.atb[1];
                final short[] atb = it.atb;
                final int n = 1;
                atb[n] /= 10;
            } else if (it.magic_physic == 1) {
                it.atb[6] = (short) (it.atb[1] / 10);
            }
        }
        it.colorName = (byte) color;
        final byte[] pc = {0, 40, 30, 20};
        for (byte i = 0; i < 9; ++i) {
            final short[] atb2 = it.atb;
            final byte b = i;
            atb2[b] += (short) (it.atb[i] * pc[color] / 100);
        }
        it.level = itemtemplate.level;
        it.durable = itemtemplate.durable;
        it.mDurable = (short) (it.durable * 10);
        it.lockItem(null);
        return it;
    }

    public Vector<Item> dropItem() {
        return null;
    }

    public Vector<GemItem> dropAllGemItem() {
        return null;
    }

    @Override
    public void checkReceivePotion(final Char p) {
    }

    public Vector<Potion> dropPotion(final Char p) {
        return null;
    }

    public GemItem dropGemItem() {
        return null;
    }

    public Vector<GemItem> dropListGemItem() {
        return null;
    }

    public void setTimeReBornInEvent(final long time) {
    }

    public void setTimeReBorn() {
        this.bornTime = System.currentTimeMillis() + Monster.timeReBorn;
        this.isDead = true;
    }

    public boolean isCopy() {
        return false;
    }

    public boolean isEnemy(final Char p) {
        return p.getIdCharThanThu() <= -1;
    }

    /** Bo muc tieu, bao client (nhu khi quai ve diem sinh). */
    void boMucTieu() {
        final Char t = this.target;
        this.target = null;
        if (t != null) {
            try {
                t.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
            } catch (final Exception ignored) {
            }
        }
    }

    /**
     * Quai hoang da thuong (lop Monster, khong boss) duoc ra don vao {@code c} luc nay khong: quai xam (thap hon tu
     * quai.xam_cap cap) khong tu lao vao, toi da quai.toi_da_vay quai chua bi danh cung danh mot nguoi; quai da mat mau
     * (bi danh) luon danh tra. Ra don thi ghi vao {@link Char#quaiVay}. Lop con (boss, quai pho ban, Moba...) giu nhu cu.
     */
    boolean chuDongDuoc(final Char c, final long now) {
        if (this.getClass() != Monster.class || this.isBoss || c == null || !com.htbb.pokemon.LauDai.bat()) {
            return true;
        }
        final java.util.concurrent.ConcurrentHashMap<Integer, Long> vay = c.quaiVay;
        final Integer minh = Integer.valueOf(this.id);
        int khac = 0;
        for (final java.util.Iterator<java.util.Map.Entry<Integer, Long>> it = vay.entrySet().iterator(); it.hasNext(); ) {
            final java.util.Map.Entry<Integer, Long> e = it.next();
            if (now - e.getValue().longValue() > com.htbb.pokemon.ChuDongQuai.CUA_SO_VAY_MS) {
                it.remove();
            } else if (!e.getKey().equals(minh)) {
                khac++;
            }
        }
        final boolean ok = com.htbb.pokemon.ChuDongQuai.duocDanh(this.level, c.getLevel(), this.hp < this.maxhp, khac,
                com.htbb.pokemon.LauDai.so("quai.xam_cap", com.htbb.pokemon.ChuDongQuai.XAM_CAP_MAC_DINH),
                com.htbb.pokemon.LauDai.so("quai.toi_da_vay", com.htbb.pokemon.ChuDongQuai.TOI_DA_VAY_MAC_DINH));
        if (ok) {
            vay.put(minh, Long.valueOf(now));
        }
        return ok;
    }

    public boolean allWayAdd() {
        return this.getMonsterTemplate().id == 37 || this.getMonsterTemplate().id == 36 || this.isMonsterVantieu();
    }

    public int getGoldRcv() {
        return com.htbb.pokemon.HeSinhThaiGame.mauChiSo(this).rcvGold / tileDrop; // xu theo mau GOC khi doi loai
    }
    public static int LimitLock = 200000;
    public static int LimitEvent = 0;
    public static int hourLuong = 0;
    public static int hourEvent = 0;
    public static int percentDrop = 100;
    public static int tileDrop = 1;

    // ================= DIEU CHINH TI LE DROP KHI TRAIN QUAI =================
    // Cac so duoi la PHAN TRAM so voi ti le goc cua game:
    //   100 = giu nguyen nhu cu, 50 = con mot nua, 20 = con 1/5, 0 = tat han.
    // Chi can sua o day, khong phai di sua tung ti le rai rac trong file.
    // Luu y: chi ap dung cho quai thuong khi train. Boss, vat pham cua nhiem vu
    // (item quest) va tieu (van tieu) khong bi anh huong.
    public static int PC_TILE_XU = 10;          // ti le roi xu
    public static int PC_TILE_LUONG = 20;       // ti le ra luong mo / luong khoa
    public static int PC_TILE_ITEM_SUKIEN = 20; // banh, keo, bong, kem, trung pet... cua su kien
    public static int PC_TILE_POTION = 30;      // binh HP/MP, binh tang toc, hp/mp chien truong
    public static int PC_TILE_NGOC = 30;        // ngoc, ngoc tu binh
    public static int PC_TILE_ITEM = 20;        // do (trang bi) roi ra

    // ================= GIAM EXP QUAI THEO CAP NHAN VAT (doi tu 15/09/2026) =================
    // 100 = giu nguyen, 30 = con 30% (giam 70%). Hai hang so nay TUNG dung de giam theo
    // CAP CUA CON QUAI (t.level >= 50) tai thoi diem quai spawn (rcvXpSauGiam ben duoi) -
    // sai voi yeu cau that: phai giam theo CAP CUA NGUOI CHOI dang train, bat ke dang
    // danh quai cap nao. Tu 15/09/2026 doi sang ap dung o thoi diem CAP EXP CHO NGUOI
    // CHOI (xem apDungGiamExpTheoCapNV ben duoi), khong con ap o luc quai spawn nua.
    // Ten hang so / khoa cau_hinh (sv.tileExpLv50, sv.tileExpDuoiLv50) giu nguyen de khong
    // phai doi HTBB-Web /admin/cauhinh, chi doi Y NGHIA: gio la "cap NHAN VAT", khong phai
    // "cap quai". Ap dung DONG NHAT cho ca Boss (khong con mien tru Boss nhu truoc).
    public static int PC_TILE_EXP_LV50 = 10;

    // Doc lap voi PC_TILE_EXP_LV50 o tren - dung khi CAP NHAN VAT dang train < 50.
    public static int PC_TILE_EXP_DUOI_LV50 = 50;

    /** Nhan ti le goc voi phan tram dieu chinh o tren. */
    public static int tile(final int tiLeGoc, final int pc) {
        if (pc <= 0) {
            return 0;
        }
        if (pc == 100) {
            return tiLeGoc;
        }
        return (int) ((long) tiLeGoc * pc / 100L);
    }

    /**
     * Exp goc cua mot MonsterTemplate. KHONG CON giam theo cap quai o day nua (xem
     * apDungGiamExpTheoCapNV) - giu ten ham de 3 noi goi (constructor + 2 lan respawn)
     * khong phai doi, nhung tra ve dung t.rcvXp, chua qua giam.
     */
    public static int rcvXpSauGiam(final MonsterTemplate t) {
        return t.rcvXp;
    }

    /**
     * Giam EXP giet quai theo CAP NHAN VAT dang nhan (khong phai cap quai): tu cap 50
     * tro len dung PC_TILE_EXP_LV50, duoi 50 dung PC_TILE_EXP_DUOI_LV50. Goi ngay truoc
     * khi cong EXP cho nguoi choi (truoc p.expReceive/Map.addXPForChar), o CA HAI luong:
     * EXP theo sat thuong (getXpReceive) va EXP don ket lieu (tinh thang tu mt.xp). Ap
     * dung dong nhat cho ca Boss - khong mien tru.
     */
    public static int apDungGiamExpTheoCapNV(final int xp, final Char nguoiNhan) {
        // Trung Thu 2026: buff ca nhan "can moc tha den 300" nhan doi exp trong 24h - ap
        // dung TRUOC khi giam theo cap, giong cac nguon nhan exp khac (vd Ve gio vang).
        long xpSauBuff = (nguoiNhan.trungThuMoc300HetHan > System.currentTimeMillis()) ? (long) xp * 2L : xp;
        // Sua 19/09/2026: ca server dat moc 10.000 lan tha den thi CHONG THEM mot lan x2
        // exp toan server trong ngay do (spec: "tinh luon ca x2 exp nhan vat dang dung") -
        // truoc day co gui thong bao nhung KHONG co dong nao thuc su nhan doi exp.
        if (data.DatabaseCanMocThaDen.homNayLaNgayDat10000()) {
            xpSauBuff *= 2L;
        }
        return Monster.tile((int) Math.min(xpSauBuff, Integer.MAX_VALUE), (nguoiNhan.lvDetail.lv >= 50) ? Monster.PC_TILE_EXP_LV50 : Monster.PC_TILE_EXP_DUOI_LV50);
    }

    /**
     * So xu mot dong roi (ban cu): ve x2 vang 1 = +50%, 2 = x2; roi nhan doubleALL, chia
     * receiveXP. Tach ra de test (GD2 1.5 - ban cu tinh x2 roi ghi de mat).
     */
    static int tinhXuRoi(final int quantity, final int x2Player, final int doubleALL, final int receiveXP) {
        int total = quantity;
        if (x2Player == 1) {
            total += quantity / 2;
        } else if (x2Player == 2) {
            total = quantity * x2Player;
        }
        return total * doubleALL / Math.max(1, receiveXP);
    }

    @Override
    public Vector<Actor> onDropItem(final Map m, final Char p) {
        BossLauDai.khiChet(this); // GD2 6.6: thuong boss the gioi theo dong gop (tat co: khong lam gi)
        PhoBanGame.khiQuaiChet(this, p); // 0.9.7: tien do tang Pho ban Ham nguc (quai MonsterPhoBan)
        final Vector<Actor> droplist = new Vector<Actor>();
        try {
            final int deltlv = Map.abs(p.lvDetail.lv - this.level);
            if (p.killer > 0 && p.isKiller && deltlv <= 10) {
                --p.killer;
                if (!(p.isKiller = (p.killer > 0))) {
                    p.nPKill = 0;
                    p.timeKiller = 0L;
                }
                final Message mm = new Message(67);
                mm.dos.writeShort(p.id);
                mm.dos.writeByte(p.isKiller ? 1 : 0);
                mm.dos.writeShort(p.killer);
                p.sendMessage(mm);
                p.sendToNearPlayer(mm);
                mm.cleanup();
            }
        } catch (final Exception ex) {
        }
        // Pokemon: nguon phat da tien hoa Eevee.
        RoiDaTienHoa.khiHaQuai(p, this);
        PokedexPokemon.khiHaQuai(p, this); // G7.D2 Pokedex "da ha" (tat co: khong lam gi)
        com.htbb.pokemon.HeSinhThaiGame.khiHaQuai(p, this); // he sinh thai: trung/nha tre, nghien cuu, album
        final int x2Drop = p.getValueLua(0);
        Label_5628:
        {
            if (this.isMaterialMons()) {
                final GemItem gem = m.doCreateGemItemMaterial(this.idTemplate);
                if (gem != null) {
                    gem.cat = 6;
                    gem.x = this.x + 5;
                    gem.y = this.y + 10;
                    gem.id = m.getIDITEM();
                    gem.time_drop = System.currentTimeMillis();
                    gem.belongUser = p.charDBID;
                    m.addGemItem(gem, this.inCountry);
                    droplist.add(gem);
                    if (p.autoGetItem == 1) {
                        p.idGem.add(gem.id);
                    }
                }
            } else if (this.isMonsterVantieu()) {
                if (!p.isFullInventory()) {
                    final int[] idthe = {678, 679, 680};
                    Map.doAddItemIsNotWearing(p, idthe[Map.r.nextInt(idthe.length)], 0);
                }
                final Vector<Item> it = this.dropItem();
                final Char master = CharManager.instance.getCharByCharDbID(this.getCharID());
                it.get(0).level = (short) ((master != null) ? master.lvDetail.lv : 1);
                it.get(0).belongUser = this.getCharID();
                it.get(0).id = m.getIDITEM();
                it.get(0).x = this.x + 16;
                it.get(0).y = this.y + 2;
                it.get(1).belongUser = p.charDBID;
                it.get(1).level = (short) p.lvDetail.lv;
                if (master != null) {
                    master.idItem.add(it.get(0).id);
                    master.sendMessage(MessageCreator.createServerAlertMessage("Tiêu của bạn đã bị " + p.charname + " cướp", ""));
                }
                if (TeamServer.server != 100) {
                    --p.cuop_tieu;
                    try {
                        Database.instance.saveOrtherLog("", p.charname, String.valueOf(p.charname) + " cuop tieu " + this.getNameMaster() + " " + this.getColorTieu(), "cuoptieu");
                        Database.instance.saveOrtherLog("", this.getMasterName(), String.valueOf(this.getMasterName()) + " bị " + p.charname + " cướp tieu " + this.getColorTieu(), "bicuoptieu");
                    } catch (final Exception ex2) {
                    }
                }
                if (p.cuop_tieu < 0) {
                    p.cuop_tieu = 0;
                }
                it.get(1).id = m.getIDITEM();
                it.get(1).x = this.x;
                it.get(1).y = this.y;
                m.addItem(it.get(0), this.inCountry);
                it.get(0).time_drop = System.currentTimeMillis();
                droplist.add(it.get(0));
                m.addItem(it.get(1), this.inCountry);
                it.get(1).time_drop = System.currentTimeMillis();
                droplist.add(it.get(1));
                synchronized (Map.THIEFT_TIEU) {
                    try {
                        final long[] thieft_TIEU = Map.THIEFT_TIEU;
                        final int n2 = it.get(1).typetieu % 6;
                        ++thieft_TIEU[n2];
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
                p.idItem.add(it.get(1).id);
            } else {
                int x2Player = p.getX2();
                if (Map.doubleALL > 1) {
                    x2Player = 0;
                }
                Potion pt = null;
                // Quai thuong qua xa level nguoi choi (+-10) thi khong ra xu nua - dung dung
                // Map.deltaLVAttackMons da dung san cho cac drop khac o duoi. Quai ma (idGhost)
                // van giu nguyen luon ra (case dac biet co san tu truoc, khong dung vao).
                final int deltalvXu = Map.abs(p.lvDetail.lv - this.level);
                if (com.htbb.pokemon.LauDai.bat()) {
                    // GD2 2.2/2.3: xu theo XU_MOI_QUAI(cap quai), 30%, he so lech cap + nang luong;
                    // da luyen kim 1,5%. Thay khoi xu cu ben duoi (khong /chiaXuRoi).
                    RoiLauDai.roi(this, m, p, droplist);
                } else if (p.receiveXP() != 3 && ((deltalvXu <= Map.deltaLVAttackMons && Map.randomMillion() < tile(((p.lvDetail.lv < 15) ? (90000 + 90000 * x2Drop / 100) : (80000 + 80000 * x2Drop / 100)) + p.luck * 100, PC_TILE_XU)) || this.idTemplate == Map.idGhost)) {
                    int totalQuantity;
                    int quantity = totalQuantity = Monster.r.nextInt() % 5 + this.getGoldRcv() / Map.getDivDropXu();
                    if (this.idTemplate == Map.idGhost) {
                        quantity = 1000000;
                    }
                    if (x2Player == 1) {
                        totalQuantity += quantity / 2;
                    } else if (x2Player == 2) {
                        totalQuantity = quantity * x2Player;
                    }
                    // GD2 1.5: truoc day "quantity * ..." ghi de ket qua x2 vang ngay tren.
                    totalQuantity = tinhXuRoi(quantity, x2Player, Map.doubleALL, p.receiveXP());
                    if (this.idTemplate == Map.idGhost || Monster.r.nextInt(100) <= percentDrop) {
                        pt = new Potion((short) 0, totalQuantity, m);
                        pt.id = m.getIDITEM();
                        pt.x = this.x;
                        pt.y = this.y;
                        pt.belongUser = p.charDBID;
                        m.addPotion(pt, this.inCountry);
                        pt.time_drop = System.currentTimeMillis();
                        droplist.add(pt);
                        if (p.autoGetItem == 1) {
                            p.idPotion.add(pt.id);
                        }
                    }
                }
                if (p.receiveXP() != 3 && Map.randomMillion() < tile(500 + 500 * x2Drop / 100, PC_TILE_POTION)) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= Map.deltaLVAttackMons) {
                        final byte ptType = 7;
                        pt = new Potion(ptType, 1, m);
                        pt.id = m.getIDITEM();
                        pt.x = this.x - 4;
                        pt.y = this.y - 4;
                        pt.belongUser = p.charDBID;
                        m.addPotion(pt, this.inCountry);
                        pt.time_drop = System.currentTimeMillis();
                        droplist.add(pt);
                        if (p.autoGetItem == 1) {
                            p.idPotion.add(pt.id);
                        }
                    }
                }
                if (Char.isSuKienWordcup2017()) {
                    if (Map.randomMillion() < tile(10000, PC_TILE_ITEM_SUKIEN)) { // Số quái chêt random drop
                        final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                        if (deltalv <= 5) {
                            final byte[] idpt = {(byte) 160}; // bóng bạc
                            final byte ptType2 = idpt[Map.r.nextInt(idpt.length)];
                            pt = new Potion(ptType2, 1, m);
                            pt.id = m.getIDITEM();
                            pt.x = this.x - 4;
                            pt.y = this.y - 4;
                            pt.belongUser = p.charDBID;
                            m.addPotion(pt, this.inCountry);
                            pt.time_drop = System.currentTimeMillis();
                            droplist.add(pt);
                            if (p.autoGetItem == 1) {
                                p.idPotion.add(pt.id);
                            }
                            p.countKillMonster(true);
                        }
                    }
                }
                if (Char.isSuKienVuLan2015()) {
                    // Vu Lan (viet lai): giet quai thuong lech cap +-5 co ti le nho roi 1
                    // nhanh hoa hong vang. X2 neu dang mac do bong da (item 791/792).
                    int tlVuLan = 2000000; // ~3%, theo don vi tile(x, PC_TILE_ITEM_SUKIEN)
                    if (p.wModel != null && (p.wModel.isBongDaNamAoCam() || p.wModel.isBongDaNuAoSoc())) {
                        tlVuLan *= 2;
                    }
                    if (Map.randomMillion() < tile(tlVuLan, PC_TILE_ITEM_SUKIEN)) {
                        final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                        if (deltalv <= 5) {
                            pt = new Potion(Potion.TYPE_NHANH_HOA_HONG_VANG, 1, m);
                            pt.id = m.getIDITEM();
                            pt.x = this.x - 4;
                            pt.y = this.y - 4;
                            pt.belongUser = p.charDBID;
                            m.addPotion(pt, this.inCountry);
                            pt.time_drop = System.currentTimeMillis();
                            droplist.add(pt);
                            if (p.autoGetItem == 1) {
                                p.idPotion.add(pt.id);
                            }
                            p.countKillMonster(true);
                        }
                    }
                }
                // Su kien World Cup (binh chon Messi/Ronaldo): quai thuong 2-3% roi mot
                // Qua bong vang, boss luon roi 20-50 qua. Tat khi sv.worldCupBatDau trong.
                if (SuKienWorldCup.dangDienRa()) {
                    SuKienWorldCup.quaiRoiBongVang(m, p, this, droplist);
                }

                if (Char.isSuKienHe2017()) {
                    if (Map.randomMillion() < tile(1000, PC_TILE_ITEM_SUKIEN)) { // Số quái chêt random drop
                        final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                        if (deltalv <= 5) {
                            final byte[] idpt = {(byte) 146}; // kem
                            final byte ptType2 = idpt[Map.r.nextInt(idpt.length)];
                            pt = new Potion(ptType2, 1, m);
                            pt.id = m.getIDITEM();
                            pt.x = this.x - 4;
                            pt.y = this.y - 4;
                            pt.belongUser = p.charDBID;
                            m.addPotion(pt, this.inCountry);
                            pt.time_drop = System.currentTimeMillis();
                            droplist.add(pt);
                            if (p.autoGetItem == 1) {
                                p.idPotion.add(pt.id);
                            }
                            p.countKillMonster(true);
                        }
                    }
                    int tl = 1000; // ban dau la 1000
                    if (TeamServer.isServerLocal()) {
                        tl = 10000000;
                    }
                    if (Map.r.nextInt(500_000) < tile(tl, PC_TILE_ITEM_SUKIEN) && p.potions[104] > 0) { // phải có dây thừng và random < tl
                        int deltalv2 = Map.abs(p.lvDetail.lv - this.level);
                        if (TeamServer.isServerLocal()) {
                            deltalv2 = 1;
                        }
                        if (deltalv2 <= 5) {
                            final Item item = droptrungPet(this.inCountry);
                            item.x = this.x - 5 + droplist.size() * 5;
                            item.y = this.y + 5;
                            item.id = m.getIDITEM();
                            item.lock = 1;
                            item.belongUser = p.charDBID;
                            m.addItem(item, this.inCountry);
                            droplist.add(item);
                            Database.instance.saveOrtherLog("", p.charname, "roi trung pet " + item.getName(), "roitrung");
                        }
                    }
                }
//                if (UtilHTBB.getHour() != hourEvent && LimitEvent < 10000
//                        && getDayTime(0L).compareTo("2022-07-04 00:00:00") > 0
//                        && getDayTime(0L).compareTo("2022-07-15 00:00:00") < 0) {
//                    LimitEvent = (TeamServer.maxCCU * 30);
//                    hourEvent = UtilHTBB.getHour();
//                    System.out.println("DA RESET LIMIT EVEMT");
//                    try {
//                        Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Đã reset kho nguyên liệu sơ cấp, train quái tiếp thui nào mọi người."));
//                    } catch (final Exception ex2) {
//                    }
//                }
//                if (LimitEvent > 0
//                        && Map.randomMillion() < 30000
//                        && p.getLevel() >= 40
//                        && getDayTime(0L).compareTo("2022-07-04 00:00:00") > 0
//                        && getDayTime(0L).compareTo("2022-07-15 00:00:00") < 0) {
//                    int[] hangNLSC = new int[]{2, 3};
//                    int randomHang = hangNLSC[Map.random(hangNLSC.length)];
//                    int randomNLCS = Map.random(GemTemplate.ID_MATERIAL_LOW[randomHang].length);
//                    p.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[randomHang][randomNLCS], 1, true);
//                    p.sendMessage(MessageCreator.createCharGemItem(p));
//                    LimitEvent -= 1;
//                    try {
//                        p.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Bạn vừa nhận 1 nguyên liệu sơ cấp " + (randomHang == 2 ? "3" : "4")));
//                    } catch (final Exception ex2) {
//                    }
//                }
                // ==================== TRAIN QUAI RA LUONG ====================
                // Moi nhan vat moi ngay nhat toi da Char.MAX_LUONG_TRAIN_NGAY luong mo va
                // ngan nay luong khoa. Khong con phu thuoc "luong khoa hien tai < 5000" va
                // khong con ngan kho LimitLock chung toan server (ngan kho cu chi maxCCU*200
                // moi 3 tieng nen han muc theo ngay se khong bao gio dat toi).
                // Han muc duoc luu theo ngay trong bang tob_train_luong -> bao tri / restart
                // khong lam reset. Xem Char.addLuongTrainQuai().
                // GD2 2.1: thiet ke dai han BO luong / luong khoa roi tu quai (nguon in tien cao
                // cap cua bot). Tat co sv.lau_dai = y nhu cu.
                boolean DropLuong = !com.htbb.pokemon.LauDai.bat(); // false = tat han viec train quai ra luong
                if (DropLuong && p.getLevel() >= 40) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= 10) {
                        if (Map.randomMillion() < tile(250000, PC_TILE_LUONG)) { // goc 25% moi con quai: luong khoa
                            final int slKhoa = Map.r.nextInt(5); // 0..4
                            if (slKhoa > 0) {
                                p.addLuongTrainQuai(slKhoa, true);
                            }
                        }
                        if (Map.randomMillion() < tile(250000, PC_TILE_LUONG)) { // goc 25% moi con quai: luong mo
                            final int slMo = Map.r.nextInt(5); // 0..4
                            if (slMo > 0) {
                                p.addLuongTrainQuai(slMo, false);
                            }
                        }
                    }
                }
                String limitTime = "2022-07-04 00:00:00";
                final String checkDropPH = getDayTime(0L);
                boolean DropThePH = false; //true là drop ph, false là dẹp nghĩ á.
                if (DropThePH && Map.randomMillion() < 100
                        && checkDropPH.compareTo(limitTime) < 0
                        && p.potions[70] < 1
                        && p.potions[86] < 1
                        && p.getLevel() >= 40) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= 5) {
                        int[] idTrung = new int[]{16, 17};
                        p.createTrungPhuongHoang(1L, idTrung[Map.r.nextInt(idTrung.length)]);
                        try {
                            com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", "Chúc mừng " + p.getName() + " vừa nhặt được trứng Moltres. Điều kiện nhận: chưa có trứng nào trong rương và từ cấp 40 trở lên.");
                        } catch (final Exception ex2) {
                        }
                    }
                }
                boolean BotXanhLa = false; //true là drop ph, false là dẹp nghĩ á.
                if ((BotXanhLa && Map.randomMillion() < 100000
                        && checkDropPH.compareTo(limitTime) < 0
                        && !p.checkEnoughtGem(249, 2000, 1)
                        && p.getLevel() >= 60)) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= 5) {
                        p.doAddGemItem(249, 1, true);
                        p.sendMessage(MessageCreator.createCharGemItem(p));
                    }
                }
                if ((BotXanhLa && Map.randomMillion() < 100000
                        && checkDropPH.compareTo(limitTime) < 0
                        && !p.checkEnoughtGem(250, 2000, 1)
                        && p.getLevel() >= 60)) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= 5) {
                        p.doAddGemItem(250, 1, true);
                        p.sendMessage(MessageCreator.createCharGemItem(p));
                    }
                }
                boolean HoaKyLan = false; //true là drop ph, false là dẹp nghĩ á.
                if ((HoaKyLan && Map.randomMillion() < 100
                        && checkDropPH.compareTo(limitTime) < 0
                        && p.potions[71] < 1
                        && p.getLevel() >= 60)) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= 5) {
                        int[] idTrung = new int[]{18};
                        p.createTrungPhuongHoang(1L, idTrung[Map.r.nextInt(idTrung.length)]);
                        try {
                            com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", "Chúc mừng " + p.getName() + " vừa nhặt được trứng hiếm. Điều kiện nhận: chưa có trứng nào trong rương và từ cấp 60 trở lên.");
                        } catch (final Exception ex2) {
                        }
                    }
                }
                if (Char.isSuKienTet2017()) {
                    if (Map.randomMillion() < tile(50000 + 5000 * x2Drop / 100, PC_TILE_ITEM_SUKIEN) && p.lvDetail.lv >= 60) {
                        final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                        if (deltalv <= Map.deltaLVAttackMons && (Char.nauBanhChung.listCharCooking.get(p.charname) != null || Char.nauBanhTet.listCharCooking.get(p.charname) != null)) {
                            final short[] idpt2 = {148, 122, 149, 150};
                            final short ptType3 = idpt2[Map.r.nextInt(idpt2.length)];
                            pt = new Potion(ptType3, 1, m);
                            pt.id = m.getIDITEM();
                            pt.x = this.x - 4;
                            pt.y = this.y - 4;
                            pt.belongUser = p.charDBID;
                            m.addPotion(pt, this.inCountry);
                            pt.time_drop = System.currentTimeMillis();
                            droplist.add(pt);
                            if (p.autoGetItem == 1) {
                                p.idPotion.add(pt.id);
                            }
                        }
                    }
                    int tyle = 60000 + 60000 * x2Drop / 100;
                    if (tyle > 80000) {
                        tyle = 80000;
                    }
                    if (p.getAnimalRide() == null || (p.getAnimalRide() != null && !p.getAnimalRide().isLan())) {
                        tyle = 0;
                    }
                    if (Map.randomMillion() < tile(tyle, PC_TILE_POTION) && p.lvDetail.lv >= 60) {
                        final int deltalv2 = Map.abs(p.lvDetail.lv - this.level);
                        if (deltalv2 <= Map.deltaLVAttackMons) {
                            final short[] idpt3 = {153, 154};
                            final short ptType4 = idpt3[Map.r.nextInt(idpt3.length)];
                            pt = new Potion(ptType4, 1, m);
                            pt.id = m.getIDITEM();
                            pt.x = this.x - 4;
                            pt.y = this.y - 4;
                            pt.belongUser = p.charDBID;
                            m.addPotion(pt, this.inCountry);
                            pt.time_drop = System.currentTimeMillis();
                            droplist.add(pt);
                            if (p.autoGetItem == 1) {
                                p.idPotion.add(pt.id);
                            }
                        }
                    }
                }
                if (Char.isSuKienHaloween2016() && Map.randomMillion() < tile(200000, PC_TILE_ITEM_SUKIEN)) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= Map.deltaLVAttackMons) {
                        final short[] idpt2 = {137, 138};
                        final short ptType3 = idpt2[Map.r.nextInt(idpt2.length)];
                        pt = new Potion(ptType3, 1, m);
                        pt.id = m.getIDITEM();
                        pt.x = this.x - 4;
                        pt.y = this.y - 4;
                        pt.belongUser = p.charDBID;
                        m.addPotion(pt, this.inCountry);
                        pt.time_drop = System.currentTimeMillis();
                        droplist.add(pt);
                        if (p.autoGetItem == 1) {
                            p.idPotion.add(pt.id);
                        }
                    }
                }
                //  if (Char.isSuKienNoel() && Map.randomMillion() < (1000 * p.lvDetail.lv)) {
//                int rateItem = 1;
//                if (p.itemVukhiThoiTrang.isGayGiangSinh()) {
//                    rateItem = 2;
//                }
/*
                if (Char.isSuKienTetduonglich2024() && Map.randomMillion() < (10 * p.lvDetail.lv) && p.lvDetail.lv >= 40 && p.itemVukhiThoiTrang.isGayGiangSinh()) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= Map.deltaLVAttackMons) {
                        final short[] idpt2 = {137};
                        final short ptType3 = idpt2[Map.r.nextInt(idpt2.length)];
                        pt = new Potion(ptType3, 1, m);
                        pt.id = m.getIDITEM();
                        pt.x = this.x - 4;
                        pt.y = this.y - 4;
                        pt.belongUser = p.charDBID;
                        m.addPotion(pt, this.inCountry);
                        pt.time_drop = System.currentTimeMillis();
                        droplist.add(pt);
                        if (p.autoGetItem == 1) {
                            p.idPotion.add(pt.id);
                        }
                    }
                }
                 */
                if (Char.isSuKienNoel2023() && Map.randomMillion() < tile(1000 * p.lvDetail.lv, PC_TILE_ITEM_SUKIEN) && p.lvDetail.lv >= 40) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= Map.deltaLVAttackMons) {
                        final short[] idpt2 = {145};
                        final short ptType3 = idpt2[Map.r.nextInt(idpt2.length)];
                        pt = new Potion(ptType3, 1, m);
                        pt.id = m.getIDITEM();
                        pt.x = this.x - 4;
                        pt.y = this.y - 4;
                        pt.belongUser = p.charDBID;
                        m.addPotion(pt, this.inCountry);
                        pt.time_drop = System.currentTimeMillis();
                        droplist.add(pt);
                        if (p.autoGetItem == 1) {
                            p.idPotion.add(pt.id);
                        }
                    }
                }
                if (Char.isSuKienMiniNuiChauBau() && Map.randomMillion() < tile(50000, PC_TILE_ITEM_SUKIEN)) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= Map.deltaLVAttackMons || (p.isAdmin && Map.adminTest)) {
                        p.setTimeCake();
                        final byte[] idpt = {77};
                        final byte ptType2 = idpt[Map.r.nextInt(idpt.length)];
                        pt = new Potion(ptType2, 1, m);
                        pt.id = m.getIDITEM();
                        pt.x = this.x - 4;
                        pt.y = this.y - 4;
                        pt.belongUser = p.charDBID;
                        m.addPotion(pt, this.inCountry);
                        pt.time_drop = System.currentTimeMillis();
                        droplist.add(pt);
                        if (p.autoGetItem == 1) {
                            p.idPotion.add(pt.id);
                        }
                    }
                }
                if (Char.isSuKienTrungThul2016()) {
                    // Trung Thu 2026 (sua 19/09/2026 theo dung spec nguoi dung gui): delta
                    // level cho phep la +-5, RIENG nguoi choi cap 8x (>=80) tro len thi +-10 -
                    // truoc day dung chung Map.deltaLVAttackMons (=10, hang so toan cuc dung
                    // cho moi su kien/train, khong rieng cho Trung Thu). Mac do thoi trang co
                    // "bong da" (item 791/792, dung chung logic voi Vu Lan) thi duoc TANG GAP
                    // DOI ti le roi nguyen lieu (bot my/trung/thit VA dau xanh), giong cach
                    // Vu Lan dang lam voi hoa hong vang o tren.
                    final int deltaChoPhepTrungThu = p.getLevel() >= 80 ? 10 : 5;
                    final boolean macBongDaTrungThu = p.wModel != null && (p.wModel.isBongDaNamAoCam() || p.wModel.isBongDaNuAoSoc());
                    final int tlNguyenLieuTrungThu = macBongDaTrungThu ? 320000 : 160000;
                    if (Map.randomMillion() < tile(tlNguyenLieuTrungThu, PC_TILE_ITEM_SUKIEN) && p.getLevel() >= 40) {
                        final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                        if (deltalv <= deltaChoPhepTrungThu) {
                            final short[] idpt2 = {142, 121, 120};
                            // final short[] idpt2 = {136};
                            final short ptType3 = idpt2[Map.r.nextInt(idpt2.length)];
                            pt = new Potion(ptType3, 1, m);
                            pt.id = m.getIDITEM();
                            pt.x = this.x - 4;
                            pt.y = this.y - 4;
                            pt.belongUser = p.charDBID;
                            m.addPotion(pt, this.inCountry);
                            pt.time_drop = System.currentTimeMillis();
                            droplist.add(pt);
                            if (p.autoGetItem == 1) {
                                p.idPotion.add(pt.id);
                            }
                        }
                    }
//                    if (Map.randomMillion() < 320000) {
//                        final int deltalv = Map.abs(p.lvDetail.lv - this.level);
//                        if (deltalv <= Map.deltaLVAttackMons) {
//                            final short[] idpt2 = {136};
//                            // final short[] idpt2 = {136};
//                            final short ptType3 = idpt2[Map.r.nextInt(idpt2.length)];
//                            pt = new Potion(ptType3, 1, m);
//                            pt.id = m.getIDITEM();
//                            pt.x = this.x - 4;
//                            pt.y = this.y - 4;
//                            pt.belongUser = p.charDBID;
//                            m.addPotion(pt, this.inCountry);
//                            pt.time_drop = System.currentTimeMillis();
//                            droplist.add(pt);
//                            if (p.autoGetItem == 1) {
//                                p.idPotion.add(pt.id);
//                            }
//                        }
//                    }
                    if (Map.randomMillion() < tile(tlNguyenLieuTrungThu, PC_TILE_ITEM_SUKIEN) && p.getLevel() >= 40) {
                        final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                        if (p.minuteSocola > 0 && deltalv <= deltaChoPhepTrungThu) {
                            final byte ptType = 122;
                            pt = new Potion(ptType, 1, m);
                            pt.id = RealController.intance.idGen.getID(4, "drop dua hau");
                            pt.x = this.x - 4;
                            pt.y = this.y - 4;
                            pt.belongUser = p.charDBID;
                            m.addPotion(pt, this.inCountry);
                            pt.time_drop = System.currentTimeMillis();
                            droplist.add(pt);
                            if (p.autoGetItem == 1) {
                                p.idPotion.add(pt.id);
                            }
                        }
                    }
                }
                if (p.receiveXP() != 3 && Map.abs(p.lvDetail.lv - this.level) <= Map.deltaLVAttackMons && Map.randomMillion() < tile(100000 + 100000 * x2Drop / 100 + p.luck * 100, PC_TILE_POTION)) {
                    byte ptType5;
                    if (this.getLevel() <= 10) {
                        ptType5 = 1;
                    } else if (this.getLevel() <= 20) {
                        ptType5 = 2;
                    } else {
                        ptType5 = 3;
                    }
                    pt = new Potion(ptType5, 1, m);
                    pt.id = m.getIDITEM();
                    pt.x = this.x - 4;
                    pt.y = this.y - 4;
                    pt.belongUser = p.charDBID;
                    m.addPotion(pt, this.inCountry);
                    pt.time_drop = System.currentTimeMillis();
                    droplist.add(pt);
                    if (p.autoGetItem == 1) {
                        p.idPotion.add(pt.id);
                    }
                }
                if (p.receiveXP() != 3 && Map.abs(p.lvDetail.lv - this.level) <= Map.deltaLVAttackMons && Map.randomMillion() < tile(100000 + 100000 * x2Drop / 100 + p.luck * 100, PC_TILE_POTION)) {
                    byte ptType5;
                    if (this.getLevel() <= 10) {
                        ptType5 = 4;
                    } else if (this.getLevel() <= 20) {
                        ptType5 = 5;
                    } else {
                        ptType5 = 6;
                    }
                    pt = new Potion(ptType5, 1, m);
                    pt.id = m.getIDITEM();
                    pt.x = this.x + 5;
                    pt.y = this.y - 5;
                    pt.belongUser = p.charDBID;
                    m.addPotion(pt, this.inCountry);
                    pt.time_drop = System.currentTimeMillis();
                    droplist.add(pt);
                    if (p.autoGetItem == 1) {
                        p.idPotion.add(pt.id);
                    }
                }
                p.checkFinishQuest(this.idTemplate, true, this.level);
                if (p.receiveXP() != 3) {
                    // vat nhiem vu kieu 2: 15% (+x2 / may man) moi con + bao hiem xui (BaoHiemRoiNhiemVu: chac roi sau
                    // 2 x so con ky vong lan truot lien tiep - truoc 29/10/2026 co nguoi can 50+ con cho 5 vat)
                    final int tiLeNv = 150000 + 150000 * x2Drop / 100 + p.luck * 100;
                    final Vector<Short> idItemQuest = p.checkDropItemQuestBaoHiem(this.idTemplate, Map.randomMillion() < tiLeNv,
                            com.htbb.pokemon.BaoHiemRoiNhiemVu.nguong(tiLeNv));
                    if (idItemQuest.size() > 0) {
                        for (int i = 0; i < idItemQuest.size(); ++i) {
                            final ItemQuest item2 = new ItemQuest(idItemQuest.get(i), 1);
                            item2.id = m.getIDITEM();
                            item2.x = this.x + 5;
                            item2.y = this.y - 5;
                            item2.time_drop = System.currentTimeMillis();
                            item2.id_char = p.charDBID;
                            m.addItemQuest(item2, this.inCountry);
                            droplist.add(item2);
                            if (p.autoGetItem == 1) {
                                p.idItemQuest.add(item2.id);
                            }
                        }
                    }
                }
                if (p.receiveXP() != 3 && Map.randomMillion() < tile(500 + 500 * x2Drop / 100 + p.luck * 100, PC_TILE_NGOC)) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= 15) {
                        final GemItem gem2 = new GemItem(60);
                        gem2.cat = 6;
                        gem2.x = this.x + 5 + droplist.size() * 5;
                        gem2.y = this.y + 10;
                        gem2.id = m.getIDITEM();
                        gem2.time_drop = System.currentTimeMillis();
                        gem2.belongUser = p.charDBID;
                        m.addGemItem(gem2, this.inCountry);
                        droplist.add(gem2);
                        if (p.autoGetItem == 1) {
                            p.idGem.add(gem2.id);
                        }
                    }
                }
                if (p.receiveXP() != 3 && Map.abs(p.lvDetail.lv - this.level) <= Map.deltaLVAttackMons && Map.randomMillion() < tile(200 + 200 * x2Drop / 100, PC_TILE_NGOC)) {
                    final GemItem gem3 = Map.dropGemTuBinh(this.level);
                    if (gem3 != null) {
                        gem3.x = this.x + 5 + droplist.size() * 5;
                        gem3.y = this.y + 5;
                        gem3.id = m.getIDITEM();
                        gem3.time_drop = System.currentTimeMillis();
                        gem3.belongUser = p.charDBID;
                        m.addGemItem(gem3, this.inCountry);
                        droplist.add(gem3);
                        if (p.autoGetItem == 1) {
                            p.idGem.add(gem3.id);
                        }
                    }
                }
                if (p.receiveXP() != 3 && (Map.randomMillion() < tile(1000 + 1000 * x2Drop / 100 + p.luck * 10, PC_TILE_NGOC) || this.idTemplate == Map.idGhost)) {
                    final int deltalv = Map.abs(p.lvDetail.lv - this.level);
                    if (deltalv <= 5) {
                        GemItem gem2 = null;
                        if (this.idTemplate != Map.idGhost) {
                            gem2 = Map.dropGemItem(0, this.getMonsterTemplate().id, false);
                        }
                        if (gem2 != null) {
                            gem2.x = this.x + 5 + droplist.size() * 5;
                            gem2.y = this.y + 5;
                            gem2.id = m.getIDITEM();
                            gem2.time_drop = System.currentTimeMillis();
                            gem2.belongUser = p.charDBID;
                            m.addGemItem(gem2, this.inCountry);
                            droplist.add(gem2);
                            if (p.autoGetItem == 1) {
                                p.idGem.add(gem2.id);
                            }
                        }
                    }
                }
                int n = Map.getPcDropItem(Map.abs(this.level - p.lvDetail.lv), p.luck);
                n += n * x2Drop / 100;
                n = tile(n, PC_TILE_ITEM); // giam ti le roi trang bi khi train quai
                // Chi ap gioi han +-10 level cho quai THUONG. Boss va quai ma (idGhost) van
                // giu nguyen hanh vi cu (khong bi anh huong boi quy tac nay).
                if (p.receiveXP() != 3 && ((Map.abs(p.lvDetail.lv - this.level) <= Map.deltaLVAttackMons && Map.randomMillion() < n) || this.idTemplate == Map.idGhost || this.isBoss)) {
                    if (this.isBossSonTinhThuyTinh() && Map.r.nextInt(100) < 30) {
                        final Item item3 = Map.dropItemByIdTemplate(this.inCountry, (Map.r.nextInt(100) < 50) ? 676 : 677);
                        if (item3 != null) {
                            item3.belongUser = p.charDBID;
                            item3.x = this.x - 5 + droplist.size() * 5;
                            item3.y = this.y + 5;
                            item3.id = m.getIDITEM();
                            if (item3.isBoss) {
                                if (p.partyID != -1) {
                                    item3.belongParty = p.partyID;
                                    item3.idPartyBoss = p.partyID;
                                } else {
                                    item3.belongParty = p.charDBID;
                                    item3.idPartyBoss = p.charDBID;
                                }
                            }
                            m.addItem(item3, this.inCountry);
                            droplist.add(item3);
                            if (p.autoGetItem == 1 || this.isBossSonTinhThuyTinh()) {
                                p.idItem.add(item3.id);
                            }
                        }
                    }
                    int nitem = 1;
                    while (nitem > 0) {
                        --nitem;
                        Item item = null;
                        if (!this.isCopy()) {
                            if (!this.isBoss) {
                                if (this.idTemplate != Map.idGhost) {
                                    item = Map.dropItem(this.getLevel(), this.inCountry);
                                    DoMoiGame.thayKhiRoi(item, this, false); // [do-moi] thay mon goc bang mon ho (1:1)
                                } else if (this.idTemplate == Map.idGhost) {
                                    Map.abs(p.lvDetail.lv - this.level);
                                }
                            } else {
                                item = Map.createItemBoss(p.lvDetail.lv, this);
                                DoMoiGame.thayKhiRoi(item, this, true); // [do-moi]
                            }
                        }
                        if (item != null) {
                            if (!this.isBoss) {
                                item.belongUser = p.charDBID;
                                if (p.lvDetail.lv >= 6) {
                                    int kk = Map.randomMillion();
                                    if (kk / 500 <= 15 && kk % 500 == 0 && kk > 0) {
                                        final byte[] plus = {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
                                        kk = plus[kk / 500 - 1];
                                    }
                                    if (kk <= 15) {
                                        for (int j = 0; j < kk; ++j) {
                                            for (int k = 0; k < 10; ++k) {
                                                final short[] atb = item.atb;
                                                final int n3 = k;
                                                atb[n3] += (short) ((k > 0) ? 1 : 4);
                                            }
                                            final Item item4 = item;
                                            ++item4.plus;
                                            if (item.plus > 15) {
                                                item.plus = 15;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                item.isBoss = true;
                            }
                            item.x = this.x - 5 + droplist.size() * 5;
                            item.y = this.y + 5;
                            item.id = m.getIDITEM();
                            if (item.isBoss) {
                                if (p.partyID != -1) {
                                    item.belongParty = p.partyID;
                                    item.idPartyBoss = p.partyID;
                                } else {
                                    item.belongParty = p.charDBID;
                                    item.idPartyBoss = p.charDBID;
                                }
                            }
                            int dropInt = Map.r.nextInt(100);
                            if (dropInt < percentDrop || item.isBoss) {
                                m.addItem(item, this.inCountry);
                                droplist.add(item);
                            }
                            if ((p.autoGetItem != 1 || this.isBoss || this.idTemplate == Map.idGhost) && !this.isBossSonTinhThuyTinh()) {
                                continue;
                            }
                            if (dropInt < percentDrop) {
                                p.idItem.add(item.id);
                            }
                        }
                    }
                }
                try {
                    if (!this.isBoss || this.isCopy()) {
                        if (this.idTemplate != Map.idGhost) {
                            break Label_5628;
                        }
                    }
                    try {
                        if (!this.isCuongThi()) {
                            if (!this.isMonsterMoba() && !this.isBossVanTienTran()  && !this.isMonsterVanTienTran()) {
                            Database.instance.updateCountBoss(p.charname);
                            Map.sendAllCharServer(17, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(p.charname) + " đã hạ được " + this.getName()));
                        }
                    }
                    } catch (final Exception ex3) {
                    }
                    if (this.idTemplate != Map.idGhost) {
                        Map.addXpCharEvent(p, ExpLauDai.expHaBoss(p, this), false, "Monster1"); // boss Gym: theo cap (ExpLauDai)
                        try {
                            if (Map.r.nextInt(1000) < 5) {
                                Map.doCreateBookSkillPet(p, 0);
                            }
                        } catch (final Exception ex4) {
                        }
                    }
                    final Vector<Potion> allpt = this.dropPotion(p);
                    if (allpt != null) {
                        if (allpt.size() == 0) {
                            if (!this.isCuongThi()) {
                                final byte ptType2 = 84;
                                pt = new Potion(ptType2, 1, m);
                                pt.id = m.getIDITEM();
                                pt.x = this.x - 4;
                                pt.y = this.y - 4;
                                pt.belongUser = p.charDBID;
                                m.addPotion(pt, this.inCountry);
                                pt.time_drop = System.currentTimeMillis();
                                droplist.add(pt);
                                if ((p.autoGetItem == 1 && !this.isBoss) || this.isBossSonTinhThuyTinh()) {
                                    p.idPotion.add(pt.id);
                                }
                            }
                        } else {
                            for (int l = 0; l < allpt.size(); ++l) {
                                pt = allpt.get(l);
                                pt.id = m.getIDITEM();
                                pt.x = this.x - 4;
                                pt.y = this.y - 4;
                                pt.belongUser = p.charDBID;
                                m.addPotion(pt, this.inCountry);
                                pt.time_drop = System.currentTimeMillis();
                                droplist.add(pt);
                                if ((p.autoGetItem == 1 && !this.isBoss && this.idTemplate != Map.idGhost) || this.isBossSonTinhThuyTinh()) {
                                    p.idPotion.add(pt.id);
                                }
                            }
                        }
                    }
                    GemItem gem4 = null;
                    Vector<GemItem> listGem = this.dropListGemItem();
                    // Moi boss deu roi Thuoc hoa hinh (gem 268) - sua 21/09/2026 theo yeu cau
                    // nguoi dung: CO DINH 1, khong con ngau nhien 1-5 nhu truoc.
                    // Dat o day de ap dung chung cho tat ca boss, thay vi khai bao rieng
                    // trong tung lop BossXxx.
                    if (this.isBoss) {
                        if (listGem == null) {
                            listGem = new Vector<GemItem>();
                        }
                        // Boss/tru cua Tran yeu tran (Van Tien Tran) khong an 2 khoan
                        // chung nay: BossMeCung.dropListGemItem() da tu cho rieng
                        // 1 nguyen lieu + 1 Luyen kim duoc co dinh moi lan chet roi.
                        if (!this.isBossVanTienTran()) {
                            final GemItem thuocHoaHinh = new GemItem(268);
                            thuocHoaHinh.isBoss = true;
                            thuocHoaHinh.cat = 6;
                            listGem.add(thuocHoaHinh);
                            // Da BO co hoi 3% roi Luyen kim duoc 30 (id 11) chung cho moi boss -
                            // yeu cau nguoi dung 21/09/2026. Boss nao dang co dong LKD_30 co dinh
                            // rieng (vd Tho Dia, BossDusknoir idTemplate 113) van giu nguyen,
                            // khong dung nhanh nay nua.
                        }
                        // Vu Lan (viet lai): moi boss chet roi han 2-4 nhanh hoa hong vang
                        // cho nguoi giet, x2 neu dang mac do bong da (item 791/792).
                        if (Char.isSuKienVuLan2015()) {
                            int soHoaHong = 2 + Map.r.nextInt(3);
                            if (p.wModel != null && (p.wModel.isBongDaNamAoCam() || p.wModel.isBongDaNuAoSoc())) {
                                soHoaHong *= 2;
                            }
                            final Potion ptHoaHong = new Potion(Potion.TYPE_NHANH_HOA_HONG_VANG, soHoaHong, m);
                            ptHoaHong.id = m.getIDITEM();
                            ptHoaHong.x = this.x - 4;
                            ptHoaHong.y = this.y - 4;
                            ptHoaHong.belongUser = p.charDBID;
                            m.addPotion(ptHoaHong, this.inCountry);
                            ptHoaHong.time_drop = System.currentTimeMillis();
                            droplist.add(ptHoaHong);
                            if (p.autoGetItem == 1) {
                                p.idPotion.add(ptHoaHong.id);
                            }
                        }
                    }
                    if (gem4 != null) {
                        listGem.add(gem4);
                    }
                    if (listGem != null) {
                        for (int i2 = 0; i2 < listGem.size(); ++i2) {
                            gem4 = listGem.get(i2);
                            gem4.x = this.x + Map.r.nextInt(5) * 16;
                            gem4.y = this.y + Map.r.nextInt(5) * 16;
                            gem4.id = m.getIDITEM();
                            gem4.time_drop = System.currentTimeMillis();
                            gem4.isBoss = true;
                            if (gem4.isBoss) {
                                if (p.partyID != -1) {
                                    gem4.belongParty = p.partyID;
                                    gem4.idPartyBoss = p.partyID;
                                } else {
                                    gem4.belongParty = p.charDBID;
                                    gem4.idPartyBoss = p.charDBID;
                                }
                            }
                            m.addGemItem(gem4, this.inCountry);
                            droplist.add(gem4);
                            if (p.autoGetItem == 1 || this.isBossSonTinhThuyTinh()) {
                                p.idGem.add(gem4.id);
                            }
                        }
                    }
                    // Boss/tru cua Tran yeu tran (Van Tien Tran) khong roi 100k xu
                    // (4x25000) chung nay - map da co phan thuong rieng.
                    if (this.idTemplate != Map.idGhost && !this.isBossVanTienTran()) {
                        for (int j = 0; j < 4; ++j) {
                            final int totalQuantity2;
                            final int quantity2 = totalQuantity2 = 25000;
                            pt = new Potion((short) 0, totalQuantity2, m);
                            pt.id = m.getIDITEM();
                            pt.x = this.x + Map.r.nextInt(3) * 16;
                            pt.y = this.y + Map.r.nextInt(5) * 16;
                            m.addPotion(pt, this.inCountry);
                            pt.time_drop = System.currentTimeMillis();
                            droplist.add(pt);
                        }
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (this.hp <= 0) {
            if (!this.isCopy() || !this.isBoss) {
                if (this.isBoss) {
                    this.setTimeReBornInEvent(this.bornTime = System.currentTimeMillis() + 86400000L);
                    Database.instance.saveEvent(Map.event.getInfo());
                    Map.removeBossLocation(1);
                } else {
                    this.setTimeReBorn();
                }
            }
            this.isDead = true;
            this.target = null;
            this.actorDie();
        }
        return droplist;
    }

    public static Item droptrungPet(int idcountry) {
        try {
            int[] idTrung = new int[]{684, 685, 686, 687, 688};
            ItemTemplates template = Map.itemTemplates.get(5).get(idTrung[r.nextInt(idTrung.length)]);
            Item newItem = null;
            newItem = new Item(template, false, 0, 0, template.id);
            newItem.level = newItem.getTemplate().level;
            newItem.idCountry = (byte) idcountry;
            newItem.clazz = 0;
            newItem.dateCreate = Char.getDayTime(0L);
            int[] nday = new int[]{3, 5, 7, 15};
            int day = nday[r.nextInt(nday.length)];
            if (day > -1) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = day * 24 * 60;
            }

            return newItem;
        } catch (Exception var6) {
            return null;
        }
    }

    public boolean isCuongThi() {
        return false;
    }

    public boolean isActive() {
        return this.getMonsterTemplate().active;
    }

    @Override
    public int getTimeReborn() {
        return Monster.timeReBorn;
    }

    @Override
    public int getCharID() {
        return (this.isMaterialMons() || this.isCongThanh()) ? 0 : -1;
    }

    @Override
    public boolean isCongThanh() {
        return this.idTemplate == 83;
    }

    public boolean canRemoveMonster() {
        return !this.isBoss || this.idTemplate != Map.idGhost || this.getMonsterTemplate().move != 0;
    }

    public boolean canDropItem() {
        return true;
    }

    public String getMasterName() {
        return "";
    }

    public byte getTypeTieu() {
        return -1;
    }

    public boolean isMyMonster(final Char p) {
        return false;
    }

    public String getNameMaster() {
        return "";
    }

    public int getColorTieu() {
        return 0;
    }

    public boolean isFinish() {
        return false;
    }

    @Override
    public boolean isBossGetItemByHit() {
        return false;
    }

    @Override
    public boolean isBossSonTinhThuyTinh() {
        return false;
    }
// bỏ cái này 
//    @Override
//    public boolean isBoss() {
//        return super.isBoss();
//    }

    @Override
    public boolean isLienHoaTru() {
        return false;
    }

    public boolean canHoaNguoiTuyet() {
        return this.idTemplate == 115 || this.idTemplate == 90;
    }

    @Override
    public boolean isRuongMaquai() {
        return false;
    }

    public boolean isThungGo() {
        return false;
    }

    @Override
    public int getInCountry() {
        return this.inCountry;
    }

    @Override
    public int getMaxDamPerHit() {
        if (this.maxHit > 0) {
            return this.maxhp / this.maxHit;
        }
        return 0;
    }

    public void setTeam(final int team) {
    }

    public int getTeam() {
        return -1;
    }

    @Override
    public boolean isMonsterMoba() {
        return false;
    }

    public boolean isMonsterOcDao() {
        return false;
    }

    public void revivalQuaiOcDao() {
    }

    public void setTru(final Vector<Monster> tru) {
    }

    public void setpos(final int pos) {
    }

    public void setTimeThuThap(final int second) {
    }

    public boolean isFinishThuThap() {
        return false;
    }

    public void doCancelThuthap() {
    }
}
