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
        long hetGioGym;
        final long lan;
        final HashMap<Integer, long[]> diem = new HashMap<Integer, long[]>();
        final HashMap<Integer, Char> nguoi = new HashMap<Integer, Char>();
        int[] niem; // {x, y, r, hetHanMsThap, idTarget}
        long niemHet;
        Char niemMucTieu;

        TrangThai(final boolean theGioi) {
            this.lan = com.htbb.pokemon.DongHo.bay();
            this.gd = new GiaiDoanBoss(this.lan, theGioi);
            this.theGioi = theGioi;
        }
    }

    private static final WeakHashMap<Monster, TrangThai> BOSS = new WeakHashMap<Monster, TrangThai>();

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
            final TrangThai t = lay(mt, true);
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

    /** Goi de (2..6) quanh boss - MonsterSumon (tu bien mat, khong hoi sinh). */
    static void goiDe(final Monster boss, final int soNguoi) {
        try {
            final Map map = boss.map;
            final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(LauDai.so("boss.de.mau", 2)));
            if (map == null || t == null) {
                return;
            }
            final int n = GiaiDoanBoss.soDe(soNguoi);
            for (int i = 0; i < n; i++) {
                final Monster de = new MonsterSumon(map, t, boss.x + Map.random(97) - 48, boss.y + Map.random(97) - 48, boss.inCountry);
                de.level = boss.level;
                de.id = RealController.intance.idGen.getID(1, "de boss lau dai");
                de.region = boss.region;
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
                // chieu lon da bao truoc: het gio ma muc tieu con trong vung -> them 2 don
                if (t.niem != null && bay >= t.niemHet) {
                    if (t.niemMucTieu != null && GiaiDoanBoss.trongVung(0, t.niem[0], t.niem[1], t.niem[2], 0, 0,
                            t.niemMucTieu.x, t.niemMucTieu.y)) {
                        kq = (int) Math.min(Integer.MAX_VALUE, (long) kq * 3L);
                    }
                    t.niem = null;
                    t.niemMucTieu = null;
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
        t.hetGioGym = com.htbb.pokemon.DongHo.bay() + ms;
        BOSS.put(m, t);
    }

    /** Boss Gym dang mo cua nhan vat (stt) hoac 0. */
    static synchronized int gymDangDanh(final Char p) {
        final long bay = com.htbb.pokemon.DongHo.bay();
        for (final java.util.Map.Entry<Monster, TrangThai> e : BOSS.entrySet()) {
            if (e.getValue().gym > 0 && e.getValue().chuGym == p.charDBID && e.getKey() != null && e.getKey().hp > 0
                    && e.getValue().hetGioGym > bay) {
                return e.getValue().gym;
            }
        }
        return 0;
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

    /** Moi 5 giay: boss Gym het gio thi bien mat (thua). */
    static void donGymHetGio() {
        final List<Monster> het = new ArrayList<Monster>();
        final long bay = com.htbb.pokemon.DongHo.bay();
        synchronized (BossLauDai.class) {
            for (final java.util.Map.Entry<Monster, TrangThai> e : BOSS.entrySet()) {
                if (e.getValue().gym > 0 && e.getValue().hetGioGym < bay && e.getKey() != null) {
                    het.add(e.getKey());
                }
            }
            for (final Monster m : het) {
                BOSS.remove(m);
            }
        }
        for (final Monster m : het) {
            try {
                m.hp = 0;
                m.isDead = true;
                m.bornTime = Long.MAX_VALUE; // khong hoi sinh
            } catch (final Exception ignored) {
            }
        }
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
        if (!LauDai.bat()) {
            return;
        }
        final TrangThai t;
        synchronized (BossLauDai.class) {
            t = BOSS.remove(m);
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
