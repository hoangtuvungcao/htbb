package real;

import com.htbb.pokemon.GiaiDoanBoss;
import com.htbb.pokemon.GoiPokemon;
import com.htbb.pokemon.LauDai;
import data.Database;
import data.DatabaseGiftCode;
import data.DatabaseLauDai;
import io.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Boss theo pha + boss the gioi hang tuan (GD2 6.6, THIET_KE 4.6). Logic thuan o
 * com.htbb.pokemon.GiaiDoanBoss. Chi chay khi LauDai.bat().
 *
 *   - Moi quai boss (Monster.isBoss hoac GoiPokemon.laTemplateBoss) co mot GiaiDoanBoss tao luc
 *     trung don dau: 70% / 40% vao pha (goi -98), goi 2..6 de (MonsterSumon, theo so nguoi
 *     dang danh), pha 3 +30% toc do danh, no theo thoi gian (Gym 5 phut / the gioi 15 phut) x3
 *     sat thuong. Chieu lon bao truoc -96 (vong tron quanh muc tieu, 1,5-3 giay) - het gio
 *     ma muc tieu con trong vung thi an them 2 don.
 *   - Dong gop: moi sat thuong vao boss cong diem cho nguoi danh; boss the gioi gui -99 moi 5
 *     giay va phat thuong theo % dong gop (>= 0,5% hop tham gia, top 10 hop vang) - vat pham
 *     KHOA + luong khoa, khong theo don cuoi.
 */
public final class BossLauDai {

    static final class TrangThai {
        final GiaiDoanBoss gd;
        final boolean theGioi;
        /** Boss Gym: stt gym (>0) va nhan vat duy nhat duoc tinh (1 dau 1). */
        int gym;
        int chuGym;
        /** Nguoi thach dau Gym (boss / de chi danh nguoi nay - chiDanhChu, chuGym). */
        Char chu;
        long hetGioGym;
        final long lan;
        final HashMap<Integer, long[]> diem = new HashMap<Integer, long[]>();
        final HashMap<Integer, Char> nguoi = new HashMap<Integer, Char>();
        /** De (goiDe) cua boss nay - go cung boss (Gym: thang / thua / huy). */
        final List<Monster> de = new ArrayList<Monster>();
        int[] niem; // {x, y, r, hetHanMsThap, idTarget}
        long niemHet;
        Char niemMucTieu;
        /** Chieu lon da no luc het gio niem va muc tieu con trong vung: don ke tiep x3 (nhipNiem). */
        boolean niemTrung;

        TrangThai(final boolean theGioi) {
            this.lan = com.htbb.pokemon.DongHo.bay();
            this.gd = new GiaiDoanBoss(this.lan, theGioi);
            this.theGioi = theGioi;
        }
    }

    private static final WeakHashMap<Monster, TrangThai> BOSS = new WeakHashMap<Monster, TrangThai>();
    /** De -> trang thai boss cha (de cua boss Gym: chu danh duoc trong lang, go cung boss). */
    private static final WeakHashMap<Monster, TrangThai> DE = new WeakHashMap<Monster, TrangThai>();

    /**
     * Phan thuong (chuoi DatabaseGiftCode.applyItemString): luong khoa. Da nang cap nam trong HOP (Hop vang /
     * Hop tham gia dot kich 1051/1050, mo bang "Dung" - data_noi_dung_roi nguon mo_hop) + tin vat dot kich cua
     * tuan, phat qua NoiDungLauDai.roiBossTheGioi (data_noi_dung_roi nguon boss_the_gioi). Luat "khong item mo coi".
     */
    static final String THUONG_VANG = "LUONGKHOA:10";
    static final String THUONG_THAM_GIA = "LUONGKHOA:3";

    private BossLauDai() {
    }

    static boolean laBoss(final Monster m) {
        return m != null && (m.isBoss || GoiPokemon.laTemplateBoss(m.idTemplate, m.maxhp));
    }

    static synchronized TrangThai lay(final Monster m, final boolean taoMoi) {
        TrangThai t = BOSS.get(m);
        if (t == null && taoMoi) {
            t = new TrangThai(false);
            BOSS.put(m, t);
        }
        return t;
    }

    /** Nguoi {@code p} gay {@code dam} vao quai {@code mt}. @return sat thuong (khong doi). */
    static int satThuongVaoBoss(final Char p, final Monster mt, final int dam0) {
        // nhanBossThu = 1 tren may that (chi may thu + -Dhtbb.thu.nhanBoss, xem DongHo) - bot choi xuyen suot tang toc
        final int dam = laBoss(mt) ? (int) Math.min(Integer.MAX_VALUE, (long) dam0 * com.htbb.pokemon.DongHo.nhanBossThu()) : dam0;
        return ghiNhan(p, mt, dam, Math.max(0, mt.hp - dam));
    }

    /** Ban sao danh da tru HP xong (mt.hp la HP sau don). */
    public static void sauDon(final Char p, final Monster mt, final int dam) {
        ghiNhan(p, mt, dam, Math.max(0, mt.hp));
    }

    static int ghiNhan(final Char p, final Monster mt, final int dam, final int hpSau) {
        TrangThaiGame.trungDon(mt);
        if (laBoss(mt)) {
            TuDongGame.khiGapBoss(p); // boss / Gym: dung Tu dong danh (tu_dong.tat_khi_pvp)
        }
        if (!LauDai.bat() || !laBoss(mt) || dam <= 0) {
            return dam;
        }
        try {
            // Don ket lieu: MapDanhNhau / MapLienDau / MapClan / MapTown / RegionVantienTran goi sauDon SAU onDropItem
            // (khiChet da go boss + de, xoa BOSS). Truoc 29/10/2026 lay(mt, true) tao lai trang thai MOI (gym 0) cho boss da
            // guc -> pha 1 -> 3 -> goiDe: de mo coi o lai lang mai mai (khong ai don: donGym bo qua gym 0), danh nguoi
            // choi moi di ngang (cong kiem cot_truyen: bot cap 4 guc o Pewter 27,46 -> "cong 0->1 khong cho qua").
            final TrangThai t = lay(mt, !daGuc(mt));
            if (t == null) {
                return dam;
            }
            if (t.gym > 0 && t.chuGym != p.charDBID) {
                return 0; // Gym 1 dau 1: nguoi khac danh khong tinh, khong gay sat thuong
            }
            final int[] sk;
            synchronized (t) {
                final long[] d = t.diem.get(Integer.valueOf(p.charDBID));
                if (d == null) {
                    t.diem.put(Integer.valueOf(p.charDBID), new long[]{dam});
                } else {
                    d[0] += dam;
                }
                t.nguoi.put(Integer.valueOf(p.charDBID), p);
                sk = t.gd.capNhat(hpSau, Math.max(1, mt.maxhp), com.htbb.pokemon.DongHo.bay());
            }
            for (final int s : sk) {
                guiQuanh(p, GoiLauDaiGame.boss(mt, t.gd.pha, s, t.gd.giayDenNo(com.htbb.pokemon.DongHo.bay())));
                if (s == GiaiDoanBoss.SK_GOI_DE) {
                    goiDe(mt, t.nguoi.size());
                }
            }
        } catch (final Exception e) {
            System.out.println("BossLauDai.satThuongVaoBoss: " + e);
        }
        return dam;
    }

    static void guiQuanh(final Char p, final Message m) {
        if (m == null || p == null) {
            return;
        }
        p.sendMessage(m);
        p.sendToNearPlayer(m);
    }

    /** Boss da guc / da go (hp 0 hoac isDead): khong mo trang thai moi, khong goi de. */
    static boolean daGuc(final Monster m) {
        return m == null || m.isDead || m.hp <= 0;
    }

    /** Goi de (2..6) quanh boss - MonsterSumon (tu bien mat, khong hoi sinh). */
    static void goiDe(final Monster boss, final int soNguoi) {
        if (daGuc(boss)) {
            return; // boss da guc: de se khong bao gio duoc go cung boss (mo coi)
        }
        try {
            final Map map = boss.map;
            final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(LauDai.so("boss.de.mau", 2)));
            if (map == null || t == null) {
                return;
            }
            int n = GiaiDoanBoss.soDe(soNguoi);
            synchronized (BossLauDai.class) {
                final TrangThai cha = BOSS.get(boss);
                if (cha != null && cha.gym > 0) {
                    // Gym 1 dau 1: soDe(1) = 2 de moi pha (4 de / tran) cap boss - dong mau thap (Eevee 3.000 HP cap 19) guc
                    // vi de truoc khi kip danh boss (do suc 29/10/2026). Mac dinh 1 de moi pha (cau_hinh gym.so_de).
                    n = Math.max(0, Math.min(n, LauDai.so("gym.so_de", GiaiDoanBoss.GYM_SO_DE)));
                }
            }
            for (int i = 0; i < n; i++) {
                final Monster de = new MonsterSumon(map, t, boss.x + Map.random(97) - 48, boss.y + Map.random(97) - 48, boss.inCountry);
                de.level = boss.level;
                de.id = RealController.intance.idGen.getID(1, "de boss lau dai");
                de.region = boss.region;
                de.khongHoiSinh = true; // truoc: de "tu bien mat" chi la ghi chu - that ra hoi sinh mai mai sau 6 giay
                synchronized (BossLauDai.class) {
                    final TrangThai cha = BOSS.get(boss);
                    if (cha == null) {
                        RealController.intance.idGen.putID(de.id, 1, "de boss lau dai (boss da go)");
                        return; // boss vua bi go: khong goi de mo coi
                    }
                    cha.de.add(de);
                    DE.put(de, cha);
                }
                map.addMonster(de, boss.inCountry, boss.region);
            }
        } catch (final Exception e) {
            System.out.println("BossLauDai.goiDe: " + e);
        }
    }

    /** Monster.attack: nhan he so no; bao truoc / ket thuc chieu lon. @return sat thuong moi. */
    static int monsterDanh(final Monster m, final LiveActor muc, final int ahp) {
        if (!LauDai.bat() || !laBoss(m)) {
            return ahp;
        }
        final TrangThai t = lay(m, false);
        if (t == null) {
            return ahp;
        }
        final long bay = com.htbb.pokemon.DongHo.bay();
        int kq = ahp;
        try {
            final int[] sk;
            boolean noMoi = false;
            synchronized (t) {
                sk = t.gd.capNhat(m.hp, Math.max(1, m.maxhp), bay);
                kq = (int) Math.min(Integer.MAX_VALUE, (long) ahp * t.gd.heSoSatThuong() / 100L);
                if (t.gym > 0) {
                    kq = giamKhacHeGym(m, muc, kq);
                }
                // chieu lon da bao truoc: no LUC HET GIO niem (nhipNiem, vi tri muc tieu luc do) -> don nay them 2 don.
                // Truoc 29/10/2026 xet vi tri o DON KE TIEP sau het gio (toi 3 giay sau): nguoi danh can chien ne ra roi
                // quay lai danh boss (vong tron quanh cho cu, sat boss) van an x3 - ne khong co tac dung.
                noNiem(t, bay);
                if (t.niemTrung) {
                    kq = (int) Math.min(Integer.MAX_VALUE, (long) kq * 3L);
                    t.niemTrung = false;
                }
                if (t.niem == null && muc instanceof Char && t.gd.denLucNiem(bay)) {
                    t.niem = new int[]{muc.x, muc.y, 48};
                    t.niemHet = bay + t.gd.msNiem();
                    t.niemMucTieu = (Char) muc;
                    noMoi = true;
                }
            }
            if (muc instanceof Char) {
                final Char c = (Char) muc;
                for (final int s : sk) {
                    guiQuanh(c, GoiLauDaiGame.boss(m, t.gd.pha, s, t.gd.giayDenNo(bay)));
                }
                if (noMoi) {
                    guiQuanh(c, GoiLauDaiGame.canhBao(m, 1, m.hePokemon1(), 0, t.niem[0], t.niem[1], t.niem[2], 0, 0, t.gd.msNiem()));
                }
            }
        } catch (final Exception e) {
            System.out.println("BossLauDai.monsterDanh: " + e);
        }
        return kq;
    }

    /** Het gio niem: no o vi tri muc tieu LUC NAY (goi duoi khoa t). */
    static void noNiem(final TrangThai t, final long bay) {
        if (t.niem != null && bay >= t.niemHet) {
            t.niemTrung = t.niemMucTieu != null && t.niemMucTieu.hp > 0 && GiaiDoanBoss.trongVung(0, t.niem[0], t.niem[1], t.niem[2], 0, 0,
                    t.niemMucTieu.x, t.niemMucTieu.y);
            t.niem = null;
            t.niemMucTieu = null;
        }
    }

    /** Monster.update cua boss: chieu lon het gio niem thi no ngay (khong cho don danh ke tiep). */
    static void nhipNiem(final Monster m) {
        if (!LauDai.bat()) {
            return;
        }
        final TrangThai t = lay(m, false);
        if (t == null) {
            return;
        }
        synchronized (t) {
            noNiem(t, com.htbb.pokemon.DongHo.bay());
        }
    }

    /**
     * Truong Gym danh dong bi khac he (x1,5 / x2 trong game): ha ve x1,25 (gym.he_toi_da). Dong Lua gap Gym Cerulean
     * (Gyarados Nuoc) / dong Nuoc gap Gym Vermilion (Luxray Dien) guc trong 15-60 giay du do cua hang dung bac (do suc
     * 29/10/2026); khac he van thiet (x1,25), nhung khong chan cung. Chieu cua nguoi choi vao boss giu nguyen.
     */
    static int giamKhacHeGym(final Monster m, final LiveActor muc, final int dam) {
        if (muc == null || dam <= 0) {
            return dam;
        }
        final int goc = com.htbb.pokemon.SatThuongHe.heSo(m.hePokemon1(), muc.hePokemon1(), muc.hePokemon2());
        // don da qua tran PvE (LiveActor.attackDam: KhacHePve.vao) -> chia cho he so DA AP, khong chia hai lan
        final int he = muc.cat == 0 ? com.htbb.pokemon.KhacHePve.vao(goc) : goc;
        return GiaiDoanBoss.giamKhacHeGym(dam, he, LauDai.so("gym.he_toi_da", GiaiDoanBoss.GYM_HE_TOI_DA));
    }

    /**
     * Boss Gym (va de cua no) chi danh nguoi thach dau: nguoi khac danh no khong gay sat thuong (ghiNhan tra 0) nen no cung
     * khong duoc danh ho. true = {@code muc} hop le (boss / quai khac: luon true).
     */
    static synchronized boolean chiDanhChu(final Monster m, final LiveActor muc) {
        if (!(muc instanceof Char) || !(m.isBoss || m.khongHoiSinh)) {
            return true;
        }
        TrangThai t = BOSS.get(m);
        if (t == null) {
            t = DE.get(m);
        }
        return t == null || t.gym <= 0 || ((Char) muc).charDBID == t.chuGym;
    }

    /**
     * Nguoi thach dau cua boss Gym / de {@code m} neu dang o cung ban do / khu / nuoc va con song, khong thi null. Boss dang
     * nham muc tieu nguoi khac (vong gan muc tieu "fp" cua ban do chon nguoi dau danh sach) thi quay ve nguoi nay - truoc ban
     * sua nay mot boss Gym trong lang dong nguoi gan nhu khong danh chu tran (bo muc tieu roi lai nhan nguoi khac).
     */
    static synchronized Char chuGym(final Monster m) {
        TrangThai t = BOSS.get(m);
        if (t == null) {
            t = DE.get(m);
        }
        final Char c = t == null || t.gym <= 0 ? null : t.chu;
        if (c == null || c.exit || c.hp <= 0 || c.map == null || !c.map.equals(m.map) || c.region != m.region || c.inCountry != m.inCountry) {
            return null;
        }
        return c;
    }

    /** Tre giua hai don (pha 3 +30% toc do). */
    static long treDanh(final Monster m, final long tre) {
        if (!LauDai.bat()) {
            return tre;
        }
        final TrangThai t = lay(m, false);
        return t == null ? tre : tre * 100L / t.gd.heSoTocDo();
    }

    // ------------------------------------------------------------------ boss the gioi

    /** Trieu hoi boss tai cho voi chi so cho truoc (Gym, su kien). @return quai hoac null. */
    static Monster trieuHoi(final Map map, final MonsterTemplate t, final int x, final int y, final int country, final int region,
            final int cap, final int hp, final int tanCong, final int thu) {
        try {
            final Monster b = new MonsterSumon(map, t, x, y, country);
            b.level = cap > 0 ? cap : t.level;
            b.id = RealController.intance.idGen.getID(1, "boss lau dai");
            b.isBoss = true;
            b.khongHoiSinh = true; // boss trieu hoi mot lan: chet / het gio thi go, khong hoi sinh
            b.region = region;
            if (hp > 0) {
                b.maxhp = hp;
                b.hp = hp;
            }
            if (tanCong > 0) {
                b.attack = tanCong;
            }
            if (thu > 0) {
                b.defend_physic = thu;
            }
            map.addMonster(b, country, region);
            return b;
        } catch (final Exception e) {
            System.out.println("BossLauDai.trieuHoi: " + e);
            return null;
        }
    }

    /** Dang ky boss Gym (no 5 phut, chi {@code chu} duoc tinh, het han sau {@code ms}). */
    static synchronized void datGym(final Monster m, final int stt, final Char chu, final long ms) {
        final TrangThai t = new TrangThai(false);
        t.gym = stt;
        t.chuGym = chu.charDBID;
        t.chu = chu;
        m.gymStt = stt;
        t.hetGioGym = com.htbb.pokemon.DongHo.bay() + ms;
        BOSS.put(m, t);
    }

    /**
     * Mo tran Gym cho {@code p} - NGUYEN TU (khoa lop): moi nhan vat toi da MOT tran Gym dang mo. Da co tran -> tra boss
     * cu, {@code moi[0] = false}, khong trieu hoi them (bam hai lan / hai cua - So tay, muc tat Quầy đổi thưởng, hop 95 -
     * deu chi tro ve boss dang co). Bao loi 24/10/2026 "2 Truong Gym xuat hien cung luc".
     * @return boss (moi hoac dang co) hoac null neu trieu hoi loi.
     */
    static Monster moGym(final Char p, final int stt, final MonsterTemplate t, final int cap, final int hp, final int tanCong,
            final int thu, final long ms, final boolean[] moi) {
        synchronized (BossLauDai.class) {
            final Monster cu = gymCua(p);
            if (cu != null) {
                moi[0] = false;
                return cu;
            }
            final Monster b = trieuHoi(p.map, t, p.x + 48, p.y, p.inCountry, p.region, cap, hp, tanCong, thu);
            if (b != null) {
                datGym(b, stt, p, ms);
            }
            moi[0] = b != null;
            return b;
        }
    }

    /** Boss Gym con song, chua het gio cua {@code p}; null = khong co. */
    static synchronized Monster gymCua(final Char p) {
        if (p == null) {
            return null;
        }
        final long bay = com.htbb.pokemon.DongHo.bay();
        for (final java.util.Map.Entry<Monster, TrangThai> e : BOSS.entrySet()) {
            final Monster m = e.getKey();
            if (e.getValue().gym > 0 && e.getValue().chuGym == p.charDBID && m != null && m.hp > 0 && !m.isDead
                    && e.getValue().hetGioGym > bay) {
                return m;
            }
        }
        return null;
    }

    /** Giay con lai cua tran Gym dang mo cua {@code p} (0 = khong co). */
    static synchronized long giayConGym(final Char p) {
        final Monster m = gymCua(p);
        final TrangThai t = m == null ? null : BOSS.get(m);
        return t == null ? 0L : Math.max(0L, (t.hetGioGym - com.htbb.pokemon.DongHo.bay()) / 1000L);
    }

    /**
     * {@code m} la boss Gym con song cua chinh {@code p} (1 dau 1) hoac de cua boss do. MapDanhNhau / MapTown: duong danh
     * quai cu bo qua MOI don trong lang Pewter / Blackthorn / Cianwood (mapIDLoadMap 0/70/80) va quai cung nuoc o Cao nguyen
     * 201 - nhung Thach dau Gym trieu hoi boss NGAY CANH nguoi choi o ban do dang dung (thuong la Pewter) nen danh mai khong
     * mat HP (bao loi 24/10/2026 "danh ma khong gay duoc 1 ti sat thuong nao"). Rieng boss Gym (va de) cua minh thi duoc danh.
     */
    /**
     * Boss Gym cua NGUOI KHAC (tran 1 dau 1): nhan vat nay khong duoc danh / hut mau / ket lieu - bot-cap1-40 (25/10/2026):
     * truoc day chi duong satThuongVaoBoss ve 0 sat thuong, con hut mau (hphut), than thu va chieu dien rong (goi 106) van tru
     * HP boss -> nguoi dung canh ket lieu boss Gym cua nguoi khac, an ca EXP boss (bot len 21 -> 27 trong 1 giay).
     */
    public static synchronized boolean laGymNguoiKhac(final Monster m, final Char p) {
        if (m == null || p == null) {
            return false;
        }
        TrangThai t = BOSS.get(m);
        if (t == null) {
            t = DE.get(m);
        }
        return t != null && t.gym > 0 && t.chuGym != p.charDBID;
    }

    public static final String GYM_MOT_DAU_MOT = "Trận Gym 1 đấu 1: chỉ người thách đấu được đánh Trưởng Gym này.";

    static synchronized boolean laGymCua(final Monster m, final Char p) {
        if (m == null || p == null || m.isDead || m.hp <= 0) {
            return false;
        }
        TrangThai t = BOSS.get(m);
        if (t == null) {
            t = DE.get(m);
        }
        return t != null && t.gym > 0 && t.chuGym == p.charDBID;
    }

    /** Boss Gym dang mo cua nhan vat (stt) hoac 0. */
    static synchronized int gymDangDanh(final Char p) {
        final Monster m = gymCua(p);
        final TrangThai t = m == null ? null : BOSS.get(m);
        return t == null ? 0 : t.gym;
    }

    /**
     * Nhan vat da gay sat thuong vao mot boss (the gioi / Gym) CON SONG tren cung ban do / khu - dang trong tran boss
     * (HoiSinhGame: cam hoi sinh tai cho giua tran, hoi_sinh.cam_boss).
     */
    static synchronized boolean dangDanhBoss(final Char p) {
        for (final java.util.Map.Entry<Monster, TrangThai> e : BOSS.entrySet()) {
            final Monster m = e.getKey();
            if (m != null && m.hp > 0 && !m.isDead && m.map == p.map && m.region == p.region
                    && e.getValue().nguoi.containsKey(Integer.valueOf(p.charDBID))) {
                return true;
            }
        }
        return false;
    }

    /** Ly do go boss Gym (GymLauDai.baoHuy). */
    static final int HUY_HET_GIO = 1;
    static final int HUY_ROI_BAN_DO = 2;
    static final int HUY_THOAT = 3;

    /**
     * Moi 5 giay (LauDaiJob): don tran Gym - het 10 phut (thua), chu roi ban do / khu / thoat game (huy), boss da guc ma
     * chua qua Monster.onDropItem (vd guc vi doc - chi chu gay duoc sat thuong: tinh thang, mot lan). Moi boss bi go: goi
     * 90 cho moi nguoi trong khu + go khoi bang quai (khong hoi sinh). Khong tham so: dung danh sach nhan vat online.
     */
    static void donGymHetGio() {
        donGym(null);
    }

    /** {@code online} (test): charDBID -> Char; null = CharManager. */
    static void donGym(final java.util.Map<Integer, Char> online) {
        final List<Object[]> go = new ArrayList<Object[]>();
        final long bay = com.htbb.pokemon.DongHo.bay();
        synchronized (BossLauDai.class) {
            for (final java.util.Map.Entry<Monster, TrangThai> e : BOSS.entrySet()) {
                final Monster m = e.getKey();
                final TrangThai t = e.getValue();
                if (m == null || t.gym <= 0) {
                    continue;
                }
                final Char chu = online != null ? online.get(Integer.valueOf(t.chuGym)) : timChar(t.chuGym);
                final int ly;
                if (m.isDead || m.hp <= 0) {
                    ly = 0; // da guc: thang
                } else if (t.hetGioGym < bay) {
                    ly = HUY_HET_GIO;
                } else if (chu == null || chu.exit) {
                    ly = HUY_THOAT;
                } else if (chu.map != m.map || chu.region != m.region || chu.inCountry != m.inCountry) {
                    ly = HUY_ROI_BAN_DO;
                } else {
                    continue;
                }
                go.add(new Object[]{m, t, Integer.valueOf(ly), chu});
            }
            for (final Object[] o : go) {
                BOSS.remove(o[0]);
            }
        }
        for (final Object[] o : go) {
            final Monster m = (Monster) o[0];
            final TrangThai t = (TrangThai) o[1];
            final int ly = ((Integer) o[2]).intValue();
            goBoss(m, t);
            if (ly == 0) {
                GymLauDai.khiHa(t.gym, t.chuGym);
            } else {
                GymLauDai.baoHuy((Char) o[3], t.gym, ly);
            }
        }
    }

    static Char timChar(final int charDBID) {
        try {
            return CharManager.instance == null ? null : CharManager.instance.getCharByCharDbID(charDBID);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Huy NGAY tran Gym cua {@code p} (thoat game / mat ket noi - CharManager.kickPlayer; vao ban do khac - goi 12 -500).
     * {@code chiKhiKhacBanDo}: chi huy khi boss khong cung ban do / khu voi {@code p}.
     */
    static void huyGymCua(final Char p, final boolean chiKhiKhacBanDo) {
        if (p == null) {
            return;
        }
        Monster m = null;
        TrangThai t = null;
        synchronized (BossLauDai.class) {
            for (final java.util.Map.Entry<Monster, TrangThai> e : BOSS.entrySet()) {
                if (e.getValue().gym > 0 && e.getValue().chuGym == p.charDBID && e.getKey() != null) {
                    final Monster k = e.getKey();
                    if (chiKhiKhacBanDo && k.map == p.map && k.region == p.region && k.inCountry == p.inCountry) {
                        return;
                    }
                    m = k;
                    t = e.getValue();
                    break;
                }
            }
            if (m == null) {
                return;
            }
            BOSS.remove(m);
        }
        final boolean guc = m.isDead || m.hp <= 0; // doc TRUOC goBoss (goBoss dat hp = 0)
        goBoss(m, t);
        if (guc) {
            GymLauDai.khiHa(t.gym, t.chuGym); // guc dung luc roi di (chua kip onDropItem): van tinh thang
        } else if (chiKhiKhacBanDo) {
            GymLauDai.baoHuy(p, t.gym, HUY_ROI_BAN_DO);
        }
    }

    /**
     * Go boss (va de) khoi ban do cho MOI nguoi: danh dau guc + khong hoi sinh, goi 90 (actorDie) cho ca khu, xep vao hang
     * go cua khu (tempRemoveMonster - Region / MapVongLap go trong luong cua ban do, khong dung bang quai dang duyet).
     */
    static void goBoss(final Monster m, final TrangThai t) {
        final List<Monster> ds = new ArrayList<Monster>();
        ds.add(m);
        if (t != null) {
            synchronized (BossLauDai.class) {
                ds.addAll(t.de);
                for (final Monster d : t.de) {
                    DE.remove(d);
                }
                t.de.clear();
            }
        }
        for (final Monster q : ds) {
            goQuai(q);
        }
    }

    static void goQuai(final Monster q) {
        if (q == null) {
            return;
        }
        q.khongHoiSinh = true;
        final boolean daGuc = q.isDead;
        q.hp = 0;
        q.isDead = true;
        q.target = null;
        q.bornTime = Long.MAX_VALUE;
        final Map map = q.map;
        if (map == null) {
            return;
        }
        if (!daGuc) {
            try {
                final Message m = new Message(90);
                m.dos.writeShort(q.id);
                m.dos.writeByte(q.cat);
                map.sendAllPlayer(m, q.inCountry, q.region);
            } catch (final Exception ignored) {
            }
        }
        map.removeDynamicMonster(q, q.inCountry, q.region);
    }

    /**
     * Hang go quai cua khu (Region / MapVongLap, luong ban do): quai trieu hoi mot lan ({@link Monster#khongHoiSinh}) thi go
     * khoi {@code bang} - ke ca khi da guc (hang cu bo qua quai hp <= 0) - chi khi o trong bang dung la con nay, roi tra id.
     * @return true = da xu ly (nguoi goi bo qua nhanh cu).
     */
    @SuppressWarnings("rawtypes")
    public static boolean goKhoiBang(final Map map, final java.util.Hashtable bang, final Monster mt) {
        if (mt == null || !mt.khongHoiSinh) {
            return false;
        }
        try {
            if (mt.map == map && bang != null && bang.get(Short.valueOf(mt.id)) == mt) {
                bang.remove(Short.valueOf(mt.id));
                RealController.intance.idGen.putID(mt.id, 1, "go boss trieu hoi");
            }
        } catch (final Exception ignored) {
        }
        return true;
    }

    /** Danh dau quai la boss the gioi (dong gop -99 + thuong theo dong gop). */
    static synchronized void datTheGioi(final Monster m) {
        BOSS.put(m, new TrangThai(true));
    }

    /** Moi 5 giay (LauDaiJob): gui bang dong gop -99 cho nguoi dang danh boss the gioi. */
    static void nhip5Giay() {
        final List<Object[]> ds = new ArrayList<Object[]>();
        synchronized (BossLauDai.class) {
            for (final java.util.Map.Entry<Monster, TrangThai> e : BOSS.entrySet()) {
                if (e.getValue().theGioi && e.getKey() != null && e.getKey().hp > 0) {
                    ds.add(new Object[]{e.getKey(), e.getValue()});
                }
            }
        }
        for (final Object[] o : ds) {
            final TrangThai t = (TrangThai) o[1];
            final List<Object[]> bang = bang(t);
            final int n = Math.min(10, bang.size());
            final int[] id = new int[n];
            final int[] pn = new int[n];
            for (int i = 0; i < n; i++) {
                id[i] = ((Char) bang.get(i)[0]).id;
                pn[i] = ((Integer) bang.get(i)[1]).intValue();
            }
            for (final Object[] b : bang) {
                final Char c = (Char) b[0];
                c.sendMessage(GoiLauDaiGame.dongGop(id, pn, n, ((Integer) b[1]).intValue()));
            }
        }
    }

    /** {Char, phanNghin} xep giam dan. */
    static List<Object[]> bang(final TrangThai t) {
        final List<Object[]> kq = new ArrayList<Object[]>();
        synchronized (t) {
            final Integer[] k = t.diem.keySet().toArray(new Integer[0]);
            final long[] d = new long[k.length];
            for (int i = 0; i < k.length; i++) {
                d[i] = t.diem.get(k[i])[0];
            }
            final int[] pn = GiaiDoanBoss.phanNghin(d);
            for (int i = 0; i < k.length; i++) {
                final Char c = t.nguoi.get(k[i]);
                if (c != null) {
                    kq.add(new Object[]{c, Integer.valueOf(pn[i])});
                }
            }
        }
        kq.sort((a, b) -> ((Integer) b[1]).intValue() - ((Integer) a[1]).intValue());
        return kq;
    }

    /** Monster.onDropItem (boss chet): phat thuong boss the gioi theo dong gop. */
    static void khiChet(final Monster m) {
        final TrangThai t;
        synchronized (BossLauDai.class) {
            t = BOSS.remove(m);
        }
        if (m.khongHoiSinh) {
            goBoss(m, t); // thang: go boss (va de) khoi ban do cho moi nguoi ngay, khong hoi sinh
        }
        if (!LauDai.bat()) {
            return;
        }
        if (t != null && t.gym > 0) {
            GymLauDai.khiHa(t.gym, t.chuGym);
            return;
        }
        if (t == null || !t.theGioi) {
            return;
        }
        final List<Object[]> bang = bang(t);
        for (int i = 0; i < bang.size(); i++) {
            final Char c = (Char) bang.get(i)[0];
            final int pn = ((Integer) bang.get(i)[1]).intValue();
            final int bac = GiaiDoanBoss.bacThuong(i + 1, pn);
            DatabaseLauDai.ghiDongGop(t.lan, c.charDBID, pn, i + 1, bac);
            if (bac > 0) {
                NhiemVuEngine.suKien(c, "boss_the_gioi", String.valueOf(m.idTemplate), 1);
                NhiemVuEngine.suKien(c, "thanh_tuu_boss", "", 1);
            }
            if (bac == 0) {
                continue;
            }
            try {
                DatabaseGiftCode.applyItemString(c, bac == 2 ? THUONG_VANG : THUONG_THAM_GIA);
                if (NoiDungLauDai.roiBossTheGioi(c, bac) == 0) {
                    // bang roi tuan nay trong (chua nap noi dung): giu thuong da cu
                    DatabaseGiftCode.applyItemString(c, "GEM:" + RoiLauDai.ID_DA + ":" + (bac == 2 ? 5 : 2) + ":-1");
                }
                c.sendMessage(MessageCreator.createCharInventoryMessage(c, 0));
                c.sendMessage(MessageCreator.createCharGemItem(c));
                LauDaiNhanVat.bao(c, "Boss thế giới: đóng góp " + (pn / 10.0) + "%, hạng " + (i + 1)
                        + (bac == 2 ? " - nhận hộp vàng." : " - nhận hộp tham gia."));
                Database.instance.saveOrtherLog("", c.charname, "boss the gioi lan " + t.lan + " hang " + (i + 1) + " pn " + pn, "laudai");
            } catch (final Exception e) {
                System.out.println("BossLauDai.khiChet: " + e);
            }
        }
    }

    /**
     * Trieu hoi boss the gioi (LauDaiJob: thu Bay 20:00; ngay "dot kich" them 12:30). Ban do /
     * mau / toa do lay tu cau_hinh: boss.the_gioi.map (1), boss.the_gioi.mau (39 - Zapdos/than
     * lan), boss.the_gioi.x / .y (px, 400/300). @return quai da tao hoac null.
     */
    public static Monster trieuHoiTheGioi() {
        if (!LauDai.bat()) {
            return null;
        }
        try {
            final Map map = RealController.mapList.get(Integer.valueOf(LauDai.so("boss.the_gioi.map", 1)));
            // GD2b: mau theo tuan (data_noi_dung_boss_the_gioi, nam 2 doi cot template_nam2); cau_hinh de neu dat.
            final int mauTuan = NoiDungLauDai.bossTuanNay();
            final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(LauDai.so("boss.the_gioi.mau", mauTuan > 0 ? mauTuan : 39)));
            if (map == null || t == null) {
                System.out.println("[LAU DAI] boss the gioi: thieu ban do hoac mau quai");
                return null;
            }
            final Monster b = new MonsterSumon(map, t, LauDai.so("boss.the_gioi.x", 400), LauDai.so("boss.the_gioi.y", 300), 0);
            b.level = t.level;
            b.id = RealController.intance.idGen.getID(1, "boss the gioi lau dai");
            b.isBoss = true;
            b.khongHoiSinh = true; // boss the gioi: ha xong khong hoi sinh (truoc: 6 giay sau hien lai day mau, khong thuong)
            b.region = 0;
            map.addMonster(b, 0, 0);
            datTheGioi(b);
            com.htbb.chongspam.ThongBaoGop.dua("boss",
                    com.htbb.thongbao.ThongBao.noi(com.htbb.thongbao.ThongBao.bossXuatHien(t.name, com.htbb.thongbao.ThongBao.banDo(map)), "Boss thế giới: thưởng theo mức đóng góp sát thương"));
            System.out.println("[LAU DAI] boss the gioi " + t.name + " id " + b.id + " o ban do " + map.mapId);
            return b;
        } catch (final Exception e) {
            System.out.println("BossLauDai.trieuHoiTheGioi: " + e);
            return null;
        }
    }
}
