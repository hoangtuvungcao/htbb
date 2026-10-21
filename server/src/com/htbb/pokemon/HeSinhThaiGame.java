package com.htbb.pokemon;

import data.DatabaseGiftCode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import real.Char;
import real.CharManager;
import real.MessageCreator;
import real.Monster;
import real.MonsterTemplate;

/**
 * NOI he sinh thai vao game (docs/thiet-ke-game.md). Logic thuan nam o cac lop
 * SinhThaiTheGioi / KyNangSong / NhaTre / ChoSinhThai / MiniGameSinhThai / SuuTamSinhThai; CSDL o
 * DuLieuHeSinhThai; menu o HeSinhThaiMenu. Lop nay giu trang thai chay (ho so trong bo nho, thay loai
 * quai, bung phat, do vui, dua) va cac DIEM MOC (moi diem moc trong src/real danh dau "// he sinh thai:"):
 *
 *   Monster.update (hoi sinh)        -> {@link #khiHoiSinh}
 *   Monster (sau PokedexPokemon.khiHaQuai) -> {@link #khiHaQuai}
 *   CharChiSo.attackDam              -> {@link #satThuongThoiTiet}
 *   MapChat.doChat                   -> {@link #khiChat}
 *   Char.doHarvest (vuon)            -> {@link #khiThuHoach}
 *   RequestNPCInfoHandler -90 / MapDoMenu -> HeSinhThaiMenu (npc ao {@link #NPC_ID})
 *
 * Tat co sv.he_sinh_thai: MOI ham tra ve ngay (hanh vi cu y nguyen). Moi loi duoc bat va in log,
 * khong bao gio nem ra diem moc.
 */
public final class HeSinhThaiGame {

    /** Npc ao cho menu he sinh thai. Da grep src/ (17/10/2026): -520..-527 dang dung; -528 trong. */
    public static final int NPC_ID = -528;
    public static final String TEN_MUC = "Hệ sinh thái";

    private HeSinhThaiGame() {
    }

    // ================================================================== khoi dong

    private static volatile boolean daKhoiDong;
    static volatile Map<Integer, Integer> vungTheoMap = Collections.emptyMap();
    static volatile Map<Integer, List<SinhThaiTheGioi.UngVien>> ungVien = Collections.emptyMap();
    static volatile Set<Integer> quaiHiem = Collections.emptySet();
    static volatile List<int[]> capBungPhat = Collections.emptyList();
    static volatile Set<Integer> quaiBaoVe = Collections.emptySet();
    /** vung -> dex muc tieu "hoan thanh vung" (quai cua vung + ca cua ho/bien). */
    static volatile Map<Integer, Set<Integer>> loaiTheoVung = Collections.emptyMap();
    /** dex -> mat na nhiem vu nghien cuu ap dung. */
    static volatile Map<Integer, Integer> nvApDung = Collections.emptyMap();
    static final Random R = new Random();
    private static ScheduledExecutorService lich;

    public static boolean bat() {
        return HeSinhThai.bat();
    }

    /** Nap du lieu the gioi + chay lich (1 lan, lan dau co diem moc nao duoc goi khi bat co). */
    static void damBaoKhoiDong() {
        if (daKhoiDong) {
            return;
        }
        synchronized (HeSinhThaiGame.class) {
            if (daKhoiDong) {
                return;
            }
            daKhoiDong = true;
            try {
                DuLieuHeSinhThai.taoBang();
                napTheGioi();
                ThoiTietGame.nap();
                NgayCongDongGame.nap();
                doiTenHatGiong();
                SuuTamSinhThai.tenBoSung = (dex) -> {
                    for (final MonsterTemplate t : real.Map.monsterTemplates.values()) {
                        if (t != null && t.dex == dex && t.name != null && !t.name.isEmpty()) {
                            return t.name;
                        }
                    }
                    return null;
                };
                lich = Executors.newSingleThreadScheduledExecutor((r) -> {
                    final Thread t = new Thread(r, "he-sinh-thai");
                    t.setDaemon(true);
                    return t;
                });
                lich.scheduleWithFixedDelay(HeSinhThaiGame::nhipCham, 20, 15, TimeUnit.SECONDS);
                lich.scheduleWithFixedDelay(HeSinhThaiGame::nhipNhanh, 5, 1, TimeUnit.SECONDS);
                lich.scheduleWithFixedDelay(HeSinhThaiGame::nhipCauCa, 5000, 200, TimeUnit.MILLISECONDS);
                System.out.println("[HE SINH THAI] da bat: " + vungTheoMap.size() + " vung, " + ungVien.size()
                        + " loai co ung vien thay, " + capBungPhat.size() + " cap bung phat, " + quaiBaoVe.size()
                        + " loai bao ve (nhiem vu chinh).");
            } catch (final Throwable e) {
                System.out.println("[HE SINH THAI] LOI khoi dong: " + e);
            }
        }
    }

    static void napTheGioi() {
        vungTheoMap = DuLieuHeSinhThai.napVung();
        final Map<Integer, List<SinhThaiTheGioi.UngVien>> uv = new HashMap<Integer, List<SinhThaiTheGioi.UngVien>>();
        final Set<Integer> hiem = new HashSet<Integer>();
        for (final int[] d : DuLieuHeSinhThai.napThayQuai()) {
            final MonsterTemplate t = real.Map.monsterTemplates.get(Integer.valueOf(d[1]));
            final MonsterTemplate g = real.Map.monsterTemplates.get(Integer.valueOf(d[0]));
            if (t == null || g == null) {
                continue;
            }
            uv.computeIfAbsent(d[0], (k) -> new ArrayList<SinhThaiTheGioi.UngVien>()).add(new SinhThaiTheGioi.UngVien(d[1],
                    t.he, t.he2, d[2], d[3] == 1, d[4], d[5], d[6]));
            if (d[3] == 1) {
                hiem.add(d[1]);
            }
        }
        ungVien = uv;
        quaiHiem = hiem;
        capBungPhat = DuLieuHeSinhThai.napBungPhat();
        // Loai la muc tieu nhiem vu CHINH: khong bao gio bi thay (cot truyen khong bi chan).
        final Set<Integer> bv = new HashSet<Integer>();
        try {
            for (final data.InfoQuestTemplate q : real.Map.allMainQuest.values()) {
                if (q != null && q.monsterKill != null) {
                    for (final Short s : q.monsterKill) {
                        if (s != null) {
                            bv.add(s.intValue());
                        }
                    }
                }
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] khong doc duoc nhiem vu chinh: " + e);
        }
        quaiBaoVe = bv;
        // Muc tieu vung + nhiem vu ap dung cho moi loai.
        final Map<Integer, Set<Integer>> lv = new HashMap<Integer, Set<Integer>>();
        for (final Map.Entry<Integer, Set<Integer>> e : DuLieuHeSinhThai.napVungLoai().entrySet()) {
            lv.put(e.getKey(), new java.util.TreeSet<Integer>(e.getValue()));
        }
        for (final int[] d : KyNangSong.BANG_CAU_CA) {
            if (d[1] <= 0 || d[2] < 8 || VatLieuSinhThai.giaPhanNghin(d[0]) >= 250) {
                continue;
            }
            if ((d[4] & KyNangSong.NUOC_NGOT) != 0) {
                lv.computeIfAbsent(SinhThaiTheGioi.V_HO, (k) -> new java.util.TreeSet<Integer>()).add(d[1]);
            }
            if ((d[4] & KyNangSong.NUOC_MAN) != 0) {
                lv.computeIfAbsent(SinhThaiTheGioi.V_BIEN, (k) -> new java.util.TreeSet<Integer>()).add(d[1]);
            }
        }
        loaiTheoVung = lv;
        final Set<Integer> dexHiem = new HashSet<Integer>();
        for (final Integer id : hiem) {
            dexHiem.add(dexTemplate(id));
        }
        final Set<Integer> dexCa = new HashSet<Integer>();
        for (final int d : SuuTamSinhThai.dexCa()) {
            dexCa.add(d);
        }
        final Map<Integer, Integer> ad = new HashMap<Integer, Integer>();
        final Map<Integer, MonsterTemplate> theoDex = new HashMap<Integer, MonsterTemplate>();
        for (final MonsterTemplate t : real.Map.monsterTemplates.values()) {
            if (t != null && t.dex > 0) {
                theoDex.putIfAbsent((int) t.dex, t);
            }
        }
        final Set<Integer> moiDex = new HashSet<Integer>(theoDex.keySet());
        moiDex.addAll(dexCa);
        for (final int[] l : NhaTre.LOAI_TRUNG) {
            moiDex.add(l[0]);
        }
        for (final Integer dex : moiDex) {
            final MonsterTemplate t = theoDex.get(dex);
            ad.put(dex, SuuTamSinhThai.apDung(dexCa.contains(dex), NhaTre.chiSo(dex) >= 0, t != null,
                    t != null ? t.he : -1, t != null ? t.he2 : -1, dexHiem.contains(dex)));
        }
        nvApDung = ad;
    }

    static int apDung(final int dex) {
        final Integer m = nvApDung.get(dex);
        return m == null ? 0 : m.intValue();
    }

    /** Trong trot = vuon san co doi ten hat giong thanh berry (chi ten hien thi). */
    static final String[] TEN_BERRY = {"Berry Oran", "Berry Pecha", "Berry Rawst", "Berry Cheri", "Berry Chesto",
        "Berry Leppa", "Berry Sitrus", "Berry Lum"};

    static void doiTenHatGiong() {
        try {
            final data.SeedTemplate[] s = real.Map.seedsTemplate;
            for (int i = 0; i < s.length; i++) {
                if (s[i] != null) {
                    s[i].name = TEN_BERRY[i % TEN_BERRY.length] + (i >= TEN_BERRY.length ? " (giống " + (i / TEN_BERRY.length + 1) + ")" : "");
                }
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] khong doi ten hat giong: " + e);
        }
    }

    // ================================================================== thoi gian / vung / thoi tiet

    static int homNay() {
        return NangLuong.ngay(com.htbb.pokemon.DongHo.bay());
    }

    static int gio() {
        return com.htbb.pokemon.DongHo.lich().get(Calendar.HOUR_OF_DAY);
    }

    static int thang() {
        return com.htbb.pokemon.DongHo.lich().get(Calendar.MONTH) + 1;
    }

    /** Ghi de thoi tiet (GM thu nghiem): vung -> thoi tiet, het khi sang ngay. */
    static final Map<Integer, Integer> THOI_TIET_GHI_DE = new ConcurrentHashMap<Integer, Integer>();
    static volatile int ngayGhiDe;

    public static int vung(final int mapId) {
        final Integer v = vungTheoMap.get(Integer.valueOf(mapId));
        return v == null ? SinhThaiTheGioi.V_DUONG : v.intValue();
    }

    public static int thoiTietVung(final int vung) {
        if (ngayGhiDe == homNay()) {
            final Integer g = THOI_TIET_GHI_DE.get(Integer.valueOf(vung));
            if (g != null) {
                return g.intValue();
            }
        }
        return ThoiTietGame.thoiTietVung(vung); // 0.9.7: lich 4 khung gio / ngay + su kien (ThoiTietLich)
    }

    public static int thoiTietMap(final int mapId) {
        final int v = vung(mapId);
        final int co = ThoiTietGame.coDinh(mapId); // ban do co dinh thoi tiet (data_thoi_tiet_map); GM ghi de vung van thang
        return co >= 0 && !(ngayGhiDe == homNay() && THOI_TIET_GHI_DE.containsKey(Integer.valueOf(v))) ? co : thoiTietVung(v);
    }

    // ================================================================== ho so nguoi choi

    static final ConcurrentHashMap<Integer, HoSoSinhThai> HO_SO = new ConcurrentHashMap<Integer, HoSoSinhThai>();
    static final Set<Integer> BAN = ConcurrentHashMap.newKeySet();

    /** Danh hieu (real.DanhHieuGame, dieu kien sinh_thai): nhan vat da co danh hieu sinh thai {@code ten}. */
    public static boolean coDanhHieu(final Char p, final String ten) {
        final HoSoSinhThai h = hoSo(p);
        return h != null && ten != null && h.coDanhHieu(ten);
    }

    /** Ho so (nap CSDL lan dau), null neu tat co / bot / loi CSDL. Da sang ngay. */
    static HoSoSinhThai hoSo(final Char p) {
        if (!bat() || p == null || p.charDBID <= 0 || p.isBot != -1 && p.isBot != 0) {
            return null;
        }
        damBaoKhoiDong();
        final Integer k = Integer.valueOf(p.charDBID);
        HoSoSinhThai h = HO_SO.get(k);
        if (h == null) {
            final String s = DuLieuHeSinhThai.napHoSo(p.charDBID);
            if (s == null) {
                return null;
            }
            h = HoSoSinhThai.doc(s);
            final HoSoSinhThai cu = HO_SO.putIfAbsent(k, h);
            if (cu != null) {
                h = cu;
            }
        }
        if (h.sangNgay(homNay())) {
            BAN.add(k);
        }
        return h;
    }

    static void danhDau(final Char p) {
        if (p != null) {
            BAN.add(Integer.valueOf(p.charDBID));
        }
    }

    /** Ghi ho so da doi; bo ho so cua nguoi da thoat (sau khi ghi). */
    static void luuTatCa() {
        final Set<Integer> online = new HashSet<Integer>();
        try {
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                final Char c = CharManager.instance.vChars.get(i);
                if (c != null) {
                    online.add(c.charDBID);
                }
            }
        } catch (final Exception ignored) {
        }
        for (final Integer id : new ArrayList<Integer>(BAN)) {
            final HoSoSinhThai h = HO_SO.get(id);
            BAN.remove(id);
            if (h != null && !DuLieuHeSinhThai.luuHoSo(id, h.chuoi())) {
                BAN.add(id); // loi CSDL: thu lai lan sau
            }
        }
        for (final Integer id : new ArrayList<Integer>(HO_SO.keySet())) {
            if (!online.contains(id) && !BAN.contains(id)) {
                HO_SO.remove(id);
            }
        }
    }

    // ================================================================== tien ich

    static void bao(final Char p, final String s) {
        if (p != null) {
            p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
        }
    }

    static void baoTheGioi(final String s) {
        try {
            // Gioi han + gop (bung phat / su kien sinh thai deu qua day): xem com.htbb.chongspam.ThongBaoGop.
            com.htbb.chongspam.ThongBaoGop.dua("sinh_thai", s);
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] thong bao: " + e);
        }
    }

    static long xuGio(final Char p) {
        final int cap = p.lvDetail != null ? p.lvDetail.lv : 1;
        return ThongSoLauDai.xuMoiGio(Math.max(1, Math.min(ThongSoLauDai.CAP_TOI_DA, cap)));
    }

    static int cap(final Char p) {
        return p.lvDetail != null ? p.lvDetail.lv : 1;
    }

    /** Phi theo phan nghin gio thu nhap o cap nguoi choi (>= 1 xu). */
    static long phi(final Char p, final int phanNghin) {
        return Math.max(1L, xuGio(p) * phanNghin / 1000L);
    }

    static boolean truXu(final Char p, final long xu, final String lyDo) {
        if (xu <= 0) {
            return true;
        }
        if (!p.truXuNeuDu(xu, false, "he sinh thai " + lyDo)) {
            return false;
        }
        capNhatTien(p);
        return true;
    }

    static void congXu(final Char p, final long xu, final String lyDo) {
        if (xu > 0) {
            p.addXu(xu, "he sinh thai " + lyDo);
            capNhatTien(p);
        }
    }

    static void capNhatTien(final Char p) {
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception ignored) {
        }
    }

    /** Phat chuoi thuong (ITEM/PT/LUONGKHOA...) - noi goi da kiem cho trong hanh trang. */
    static void phatThuong(final Char p, final String chuoi, final String lyDo) {
        if (chuoi == null || chuoi.isEmpty()) {
            return;
        }
        try {
            DatabaseGiftCode.applyItemString(p, chuoi);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            data.Database.instance.saveOrtherLog("", p.charname, "he sinh thai " + lyDo + ": " + chuoi, "hesinhthai");
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] LOI phat thuong " + chuoi + ": " + e);
        }
    }

    static Char timOnline(final int charDBID) {
        try {
            return CharManager.instance.getCharByCharDbID(charDBID);
        } catch (final Exception e) {
            return null;
        }
    }

    static int dexTemplate(final int idTemplate) {
        final MonsterTemplate t = real.Map.monsterTemplates.get(Integer.valueOf(idTemplate));
        return t == null ? 0 : t.dex;
    }

    // ================================================================== DIEM MOC: quai hoi sinh

    /** Quai dang mang loai thay the: Monster -> template goc (tren luoi). */
    static final Map<Monster, Integer> GOC = Collections.synchronizedMap(new IdentityHashMap<Monster, Integer>());
    /** Quai dang la loai hiem (1) / bung phat (2). */
    static final Map<Monster, Integer> DAC_BIET = Collections.synchronizedMap(new IdentityHashMap<Monster, Integer>());
    static final Map<Integer, Long> LAN_BAO_HIEM = new ConcurrentHashMap<Integer, Long>();
    /** Ban do khong dong vao (vi tri quai gan logic thap/lane - docs/kien-truc.md). */
    static final int[] MAP_KHONG_DONG = {40, 42};

    /** Thong ke chay (in log moi 5 phut): lan hoi sinh xet, lan doi loai, quai hiem, bung phat. */
    static final java.util.concurrent.atomic.AtomicLong DEM_HOI_SINH = new java.util.concurrent.atomic.AtomicLong(),
            DEM_THAY = new java.util.concurrent.atomic.AtomicLong(), DEM_HIEM = new java.util.concurrent.atomic.AtomicLong(),
            DEM_BUNG_PHAT = new java.util.concurrent.atomic.AtomicLong();
    static volatile long lanInThongKe;

    /** Bung phat dang mo: {mapId, idQuai, ngay, khung}; null = khong. */
    static volatile int[] bungPhat;

    // he sinh thai: goi tu Monster.update ngay khi quai hoi sinh, TRUOC khi dat lai hp = maxhp.
    public static void khiHoiSinh(final Monster m) {
        if (!bat() || m == null) {
            return;
        }
        try {
            damBaoKhoiDong();
            if (m.map == null || m.map.getClass() != real.Map.class || m.isBoss || m.idTemplate == 83
                    || m.isMaterialMons() || m.isMonsterVantieu()) {
                return;
            }
            final int mapId = m.map.mapId;
            for (final int k : MAP_KHONG_DONG) {
                if (k == mapId) {
                    return;
                }
            }
            DEM_HOI_SINH.incrementAndGet();
            final Integer g = GOC.get(m);
            final int goc = g == null ? m.idTemplate : g.intValue();
            int moi = goc;
            int dacBiet = 0;
            if (!quaiBaoVe.contains(goc)) {
                final int[] bp = bungPhat;
                final int cd = NgayCongDongGame.loaiNoiBat(mapId, goc); // ngay cong dong: gio vang, ban do cua dong
                if (cd > 0) {
                    moi = cd;
                } else if (bp != null && bp[0] == mapId && SinhThaiTheGioi.doiThanhBungPhat(goc, bp[1], R)) {
                    moi = bp[1];
                    dacBiet = 2;
                } else if (bp != null && bp[0] == mapId && goc == bp[1]) {
                    dacBiet = 2;
                } else {
                    final int tt = thoiTietMap(mapId);
                    final int c = SinhThaiTheGioi.chonThayThe(ungVien.get(goc), 0, tt, gio(), thang(), R);
                    if (c > 0) {
                        moi = c;
                        dacBiet = quaiHiem.contains(c) ? 1 : 0;
                    }
                }
            }
            if (moi != m.idTemplate) {
                final MonsterTemplate t = real.Map.monsterTemplates.get(Integer.valueOf(moi));
                if (t == null) {
                    return;
                }
                apTemplate(m, t);
            }
            if (moi != goc) {
                (dacBiet == 2 ? DEM_BUNG_PHAT : dacBiet == 1 ? DEM_HIEM : DEM_THAY).incrementAndGet();
            }
            if (moi != goc) {
                GOC.put(m, goc);
            } else {
                GOC.remove(m);
            }
            if (dacBiet != 0) {
                DAC_BIET.put(m, dacBiet);
            } else {
                DAC_BIET.remove(m);
            }
            if (dacBiet == 1) {
                baoQuaiHiem(m, mapId);
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] khiHoiSinh: " + e);
        }
    }

    /** Doi template cua quai (giu vi tri, id, quoc gia, vung). hp/xp do Monster.update dat ngay sau. */
    static void apTemplate(final Monster m, final MonsterTemplate t) {
        m.idTemplate = t.id;
        m.setType(t.type);
        m.level = t.level;
        m.maxhp = t.maxhp;
        m.maxxp = t.rcvXp;
        m.attack = t.attack;
        m.defend_physic = t.defend;
        m.accurate = t.accurate;
        m.dodge = t.dodge;
        m.dmove = t.dmove;
        m.hePokemon = t.he;
        m.he = (byte) HePokemon.nguHanhCu(t.he);
        m.he2 = t.he2;
    }

    static void baoQuaiHiem(final Monster m, final int mapId) {
        final long now = com.htbb.pokemon.DongHo.bay();
        final Long cu = LAN_BAO_HIEM.get(m.idTemplate);
        if (cu != null && now - cu < HeSinhThai.THONG_BAO_HIEM_CACH_MS) {
            return;
        }
        LAN_BAO_HIEM.put(m.idTemplate, now);
        final MonsterTemplate t = real.Map.monsterTemplates.get(Integer.valueOf(m.idTemplate));
        final String ten = t != null ? SuuTamSinhThai.tenLoai(t.dex) : "Pokémon hiếm";
        baoTheGioi("Một " + ten + " hiếm vừa xuất hiện ở " + real.Map.getNameMap(mapId) + " ("
                + SinhThaiTheGioi.tenThoiTiet(thoiTietMap(mapId)).toLowerCase()
                + (SinhThaiTheGioi.laDem(gio()) ? ", ban đêm" : "") + ")!");
    }

    // ================================================================== DIEM MOC: ha quai

    /** Nguoi choi da chup album bung phat o khung nao (charDBID -> ngay*10+khung). */
    static final Map<Integer, Integer> DA_CHUP_BUNG_PHAT = new ConcurrentHashMap<Integer, Integer>();

    // he sinh thai: goi tu Monster sau PokedexPokemon.khiHaQuai (p nhan do / EXP cua con quai).
    public static void khiHaQuai(final Char p, final Monster m) {
        if (!bat() || p == null || m == null) {
            return;
        }
        try {
            NgayCongDongGame.khiHaQuai(p, m); // ngay cong dong: bo dem danh hieu cua dong
            final HoSoSinhThai h = hoSo(p);
            if (h == null) {
                return;
            }
            final int dex = dexTemplate(m.idTemplate);
            final int mapId = m.map != null ? m.map.mapId : p.mapID;
            final int tt = thoiTietMap(mapId);
            boolean doi = false;
            if (dex > 0) {
                final MonsterTemplate t = real.Map.monsterTemplates.get(Integer.valueOf(m.idTemplate));
                if (SinhThaiTheGioi.laDem(gio())) {
                    doi |= nghienCuu(p, h, dex, SuuTamSinhThai.NV_DEM);
                }
                if (t != null && (SinhThaiTheGioi.heHopThoiTiet(t.he, tt) || SinhThaiTheGioi.heHopThoiTiet(t.he2, tt))) {
                    doi |= nghienCuu(p, h, dex, SuuTamSinhThai.NV_THOI_TIET);
                }
                final Integer db = DAC_BIET.get(m);
                if (db != null) {
                    doi |= nghienCuu(p, h, dex, SuuTamSinhThai.NV_HIEM);
                    if (db == 1) {
                        chupAnh(p, h, dex, SuuTamSinhThai.A_HIEM, tt);
                        doi = true;
                    } else {
                        final int[] bp = bungPhat;
                        final int ma = bp == null ? 0 : bp[2] * 10 + bp[3];
                        final Integer da = DA_CHUP_BUNG_PHAT.get(p.charDBID);
                        if (ma != 0 && (da == null || da != ma)) {
                            DA_CHUP_BUNG_PHAT.put(p.charDBID, ma);
                            chupAnh(p, h, dex, SuuTamSinhThai.A_BUNG_PHAT, tt);
                            doi = true;
                        }
                    }
                }
            }
            doi |= buocTrungVaNhaTre(p, h);
            if (doi) {
                danhDau(p);
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] khiHaQuai: " + e);
        }
    }

    /** 1 buoc cho moi trung + nha tre. Tra true neu ho so doi dang ke (luu). */
    static boolean buocTrungVaNhaTre(final Char p, final HoSoSinhThai h) {
        boolean doi = false;
        final List<NhaTre.Trung> no = new ArrayList<NhaTre.Trung>();
        synchronized (h) {
            for (final NhaTre.Trung t : h.trung) {
                if (!t.daNo()) {
                    t.buoc++;
                    if (t.daNo()) {
                        no.add(t);
                    }
                }
            }
            if ((h.nhaTre1 > 0 || h.nhaTre2 > 0) && h.ngayPhiNhaTre == h.ngay) {
                h.buocNhaTre++;
                final int n = NhaTre.soTrungMoi(h.buocNhaTre - 1, 1, h.deHomNay);
                if (n > 0 && h.trung.size() < HeSinhThai.TRUNG_TOI_DA_TUI) {
                    h.buocNhaTre = 0;
                    h.deHomNay++;
                    final NhaTre.Trung t = deTrung(p, h);
                    if (t != null) {
                        h.trung.add(t);
                        bao(p, "Nhà trẻ: " + SuuTamSinhThai.tenLoai(t.dex) + " vừa đẻ trứng · xem ở Hệ sinh thái › Nhà trẻ và trứng");
                        doi = true;
                    }
                } else if (h.buocNhaTre >= HeSinhThai.BUOC_DE_TRUNG) {
                    h.buocNhaTre = HeSinhThai.BUOC_DE_TRUNG; // het luot ngay / tui day: giu, khong tich them
                }
            }
        }
        for (final NhaTre.Trung t : no) {
            bao(p, "Trứng " + SuuTamSinhThai.tenLoai(t.dex) + " sắp nở · vào Hệ sinh thái › Nhà trẻ và trứng để nở");
            doi = true;
        }
        return doi;
    }

    /** Tao trung tu 1-2 pet dang gui. */
    static NhaTre.Trung deTrung(final Char p, final HoSoSinhThai h) {
        final int a = h.nhaTre1, b = h.nhaTre2;
        final int me = a > 0 && b > 0 ? (R.nextBoolean() ? a : b) : (a > 0 ? a : b);
        final int dexGoc = NhaTre.loaiGoc(me);
        if (NhaTre.chiSo(dexGoc) < 0) {
            return null;
        }
        final int[] ivA = ivGiong(p, h, NhaTre.loaiGoc(a > 0 ? a : b));
        final int[] ivB = a > 0 && b > 0 ? ivGiong(p, h, NhaTre.loaiGoc(b)) : null;
        final int[] iv = NhaTre.diTruyen(ivA, ivB, R);
        final boolean shiny = NhaTre.laShiny(h.demKhongShiny, R);
        h.demKhongShiny = shiny ? 0 : h.demKhongShiny + 1;
        return new NhaTre.Trung(dexGoc, iv[0], iv[1], iv[2], shiny, 0);
    }

    static int[] ivGiong(final Char p, final HoSoSinhThai h, final int dexGoc) {
        final int[] g = h.giong.get(dexGoc);
        return g != null ? g : NhaTre.ivHoangDa(p.charDBID, dexGoc);
    }

    /** Ghi nhiem vu nghien cuu; lan dau -> thuong luong khoa nho + kiem hoan thanh vung. */
    static boolean nghienCuu(final Char p, final HoSoSinhThai h, final int dex, final int bit) {
        if (!h.ghiNghienCuu(dex, bit)) {
            return false;
        }
        phatThuong(p, "LUONGKHOA:" + HeSinhThai.THUONG_NGHIEN_CUU_LUONG_KHOA, "nghien cuu " + dex + " bit " + bit);
        int i = 0;
        for (int k = 0; k < SuuTamSinhThai.BIT_NV.length; k++) {
            if (SuuTamSinhThai.BIT_NV[k] == bit) {
                i = k;
            }
        }
        bao(p, "Nghiên cứu Pokédex: " + SuuTamSinhThai.tenLoai(dex) + " - " + SuuTamSinhThai.TEN_NV[i] + " (+"
                + HeSinhThai.THUONG_NGHIEN_CUU_LUONG_KHOA + " lượng khoá)");
        return true;
    }

    static void chupAnh(final Char p, final HoSoSinhThai h, final int dex, final int kieu, final int tt) {
        final int truoc = h.album.size();
        final int sau = h.themAnh(SuuTamSinhThai.anh(dex, kieu, tt, homNay()));
        if (sau > truoc) {
            bao(p, "Đã chụp " + SuuTamSinhThai.tenLoai(dex) + " (" + SuuTamSinhThai.tenAnhNgan(kieu) + ") · Album " + sau + " ảnh");
        }
        final int moc = SuuTamSinhThai.mocVuaDat(truoc, sau);
        if (moc >= 0 && (h.mocAlbum & (1 << moc)) == 0) {
            h.mocAlbum |= 1 << moc;
            h.themDanhHieu(SuuTamSinhThai.DANH_HIEU_ALBUM[moc]);
            phatThuong(p, "LUONGKHOA:" + SuuTamSinhThai.THUONG_MOC_ALBUM[moc], "moc album " + SuuTamSinhThai.MOC_ALBUM[moc]);
            baoTheGioi(p.charname + " đạt mốc album " + SuuTamSinhThai.MOC_ALBUM[moc] + " ảnh, nhận danh hiệu "
                    + SuuTamSinhThai.DANH_HIEU_ALBUM[moc] + "!");
        }
    }

    // ================================================================== DIEM MOC: sat thuong theo thoi tiet

    // he sinh thai: goi tu CharChiSo.attackDam sau khi nhan % chieu - nguoi danh (quai / nguoi).
    public static int satThuongThoiTiet(final Char self, final int idSkill, final int dam) {
        if (!bat() || self == null || dam <= 0) {
            return dam;
        }
        try {
            int he = ChieuPokemon.heChieu(self.charClass, idSkill);
            if (he < 0) {
                he = self.hePokemon1();
            }
            final int pct = ThoiTietLich.satThuong(he, thoiTietMap(self.mapID)); // kep tran +-10%
            return pct == 100 ? dam : (int) ((long) dam * pct / 100L);
        } catch (final Exception e) {
            return dam;
        }
    }

    // ================================================================== DIEM MOC: thu hoach vuon

    // he sinh thai: goi tu Char.doHarvest sau SeedItem.doHavest. idCay = template cay TRUOC khi thu.
    public static void khiThuHoach(final Char p, final int idCay, final int hangCay, final String ketQua) {
        if (!bat() || p == null || ketQua == null || !ketQua.startsWith("Đã thu hoạch")) {
            return;
        }
        try {
            final HoSoSinhThai h = hoSo(p);
            if (h == null) {
                return;
            }
            final int ma = 30 + Math.floorMod(idCay, TEN_BERRY.length);
            int so = 2 + Math.max(0, hangCay);
            so += so * Math.min(5, h.tuoiNhanHomNay) * 5 / 100; // ban be tuoi: +5%/lan, toi da +25%
            final int bac = VatLieuSinhThai.bacTheoCap(cap(p));
            h.them(VatLieuSinhThai.khoa(ma, bac), so);
            final int len = h.congExp(KyNangSong.TRONG_TROT, KyNangSong.expKhiLam(VatLieuSinhThai.giaPhanNghin(ma)) * 3);
            bao(p, "Vườn berry: +" + so + " " + VatLieuSinhThai.ten(ma) + " vào Túi sinh thái." + (len > 0
                    ? " Trồng trọt lên cấp " + len + "!" : ""));
            real.DiemMocNhiemVu.khiThuHoach(p, idCay); // diem moc lam_vuon
            danhDau(p);
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] khiThuHoach: " + e);
        }
    }

    // ================================================================== DIEM MOC: chat

    // he sinh thai: goi tu MapChat.doChat (moi nguoi choi). true = da xu ly, KHONG phat chat nay.
    public static boolean khiChat(final Char p, final String msg) {
        if (!bat() || p == null || msg == null) {
            return false;
        }
        try {
            damBaoKhoiDong();
            final String c = MiniGameSinhThai.chuanHoa(msg);
            if (DoVui.traLoi(p, msg)) {
                return true;
            }
            if ("hst".equals(c) || "hesinhthai".equals(c) || "sinhthai".equals(c)) {
                HeSinhThaiMenu.moMenu(p);
                return true;
            }
            if ("cauca".equals(c)) {
                HeSinhThaiMenu.cauCa(p);
                return true;
            }
            if ("cauca2".equals(c)) {
                HeSinhThaiMenu.thaCau(p, true);   // client 0.9.6.1+: phai keo can dung luc
                return true;
            }
            if ("keocan".equals(c)) {
                HeSinhThaiMenu.keoCan(p);
                return true;
            }
            if ("dao".equals(c) || "khaoco".equals(c)) {
                HeSinhThaiMenu.dao(p);
                return true;
            }
            if ("dua".equals(c) || "duapokemon".equals(c)) {
                Dua.dangKy(p);
                return true;
            }
            if (p.isAdmin && msg.trim().startsWith("hst ")) {
                return lenhAdmin(p, msg.trim().substring(4).trim());
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] khiChat: " + e);
        }
        return false;
    }

    static boolean lenhAdmin(final Char p, final String lenh) {
        final String[] a = lenh.split("\\s+");
        switch (a[0]) {
            case "dovui":
                DoVui.batDau();
                return true;
            case "dua":
                Dua.moDangKy(true);
                return true;
            case "bungphat":
                batDauBungPhat(-1);
                return true;
            case "giaidau":
                GiaiDau.congBo();
                return true;
            case "vatlieu": // GM thu nghiem: hst vatlieu <ma> <so> [bac]
                if (a.length >= 3) {
                    final HoSoSinhThai hv = hoSo(p);
                    final int ma = Integer.parseInt(a[1]);
                    if (hv != null && VatLieuSinhThai.coMa(ma) && ma != VatLieuSinhThai.TRUNG) {
                        hv.them(VatLieuSinhThai.khoa(ma, a.length >= 4 ? Integer.parseInt(a[3]) : 1), Math.min(999, Integer.parseInt(a[2])));
                        danhDau(p);
                        bao(p, "GM: +" + a[2] + " " + VatLieuSinhThai.ten(ma));
                        data.Database.instance.saveOrtherLog("", p.charname, "gm hst vatlieu " + lenh, "hesinhthai");
                    }
                }
                return true;
            case "danhhieu": // GM thu nghiem: hst danhhieu <chi so trong HoSoSinhThai.DANH_HIEU>
                if (a.length >= 2) {
                    final HoSoSinhThai hd = hoSo(p);
                    final int i = Integer.parseInt(a[1]);
                    if (hd != null && i >= 0 && i < HoSoSinhThai.DANH_HIEU.length) {
                        hd.themDanhHieu(HoSoSinhThai.DANH_HIEU[i]);
                        danhDau(p);
                    }
                }
                return true;
            case "xu": // GM thu nghiem: hst xu <so>
                if (a.length >= 2) {
                    congXu(p, Math.min(100000000L, Long.parseLong(a[1])), "gm thu nghiem");
                }
                return true;
            case "thoitiet":
                if (a.length >= 3) {
                    ngayGhiDe = homNay();
                    THOI_TIET_GHI_DE.put(Integer.parseInt(a[1]), Integer.parseInt(a[2]));
                    bao(p, "Đặt thời tiết vùng " + a[1] + " = " + SinhThaiTheGioi.tenThoiTiet(Integer.parseInt(a[2])) + " (hết hôm nay).");
                    guiThoiTietMoiNguoi(true);
                }
                return true;
            case "napthegioi":
                napTheGioi();
                bao(p, "Đã nạp lại dữ liệu thế giới: " + ungVien.size() + " loài có ứng viên.");
                return true;
            case "theluc":
                final HoSoSinhThai h = hoSo(p);
                if (h != null) {
                    h.congTheLuc(HeSinhThai.THE_LUC_TOI_DA);
                    danhDau(p);
                }
                return true;
            default:
                bao(p, "hst dovui | dua | bungphat | giaidau | vatlieu <ma> <so> | xu <so> | thoitiet <vùng> <tt> | napthegioi | theluc");
                return true;
        }
    }

    // ================================================================== lich dinh ky

    static volatile int khungDaBao = -1;
    static volatile int ngayKhungDaBao;

    /** Moi 15 giay: luu ho so, bung phat, thoi tiet hien thi, het han cho, do vui theo gio, dua. */
    static void nhipCham() {
        if (!bat()) {
            return;
        }
        try {
            luuTatCa();
            if (com.htbb.pokemon.DongHo.bay() - lanInThongKe > 300000L) {
                lanInThongKe = com.htbb.pokemon.DongHo.bay();
                System.out.println("[HE SINH THAI] thong ke: hoi sinh " + DEM_HOI_SINH.get() + ", doi loai " + DEM_THAY.get()
                        + ", hiem " + DEM_HIEM.get() + ", bung phat " + DEM_BUNG_PHAT.get() + ", ho so " + HO_SO.size());
            }
            final int ngay = homNay();
            final int gio = gio();
            final int khung = SinhThaiTheGioi.khungBungPhat(gio);
            final int[] bp = bungPhat;
            if (bp != null && (bp[2] != ngay || bp[3] != khung)) {
                baoTheGioi("Bùng phát " + SuuTamSinhThai.tenLoai(dexTemplate(bp[1])) + " ở " + real.Map.getNameMap(bp[0]) + " đã kết thúc.");
                bungPhat = null;
            }
            if (khung >= 0 && (khungDaBao != khung || ngayKhungDaBao != ngay)) {
                khungDaBao = khung;
                ngayKhungDaBao = ngay;
                batDauBungPhat(khung);
            }
            guiThoiTietMoiNguoi(false);
            HeSinhThaiMenu.xuLyHetHanCho();
            DoVui.nhip();
            Dua.nhip();
            GiaiDau.nhip();
        } catch (final Throwable e) {
            System.out.println("[HE SINH THAI] nhipCham: " + e);
        }
    }

    /** Moi giay: chi viec can nhanh (dua). */
    /** Nhip cau ca (200 ms): bao ca can / ca thoat dung luc (CauCaPhien). */
    static void nhipCauCa() {
        if (!bat()) {
            return;
        }
        try {
            HeSinhThaiMenu.nhipCauCa();
        } catch (final Throwable e) {
            System.out.println("[HE SINH THAI] nhipCauCa: " + e);
        }
    }

    static void nhipNhanh() {
        if (!bat()) {
            return;
        }
        try {
            Dua.nhipNhanh();
        } catch (final Throwable e) {
            System.out.println("[HE SINH THAI] nhipNhanh: " + e);
        }
    }

    static void batDauBungPhat(final int khung) {
        final int ngay = homNay();
        final int[] c = SinhThaiTheGioi.bungPhat(ngay, khung < 0 ? R.nextInt(100) + 10 : khung, capBungPhat);
        if (c == null) {
            return;
        }
        bungPhat = new int[]{c[0], c[1], ngay, khung < 0 ? SinhThaiTheGioi.khungBungPhat(gio()) : khung};
        baoTheGioi("Bùng phát! Đàn " + SuuTamSinhThai.tenLoai(dexTemplate(c[1])) + " tràn ra " + real.Map.getNameMap(c[0])
                + " trong 1 giờ tới - cơ hội nghiên cứu và chụp album!");
    }

    /** Hieu ung -39 da gui moi nguoi (charDBID -> ma hieu ung) - chi gui lai khi doi. */
    static final Map<Integer, Integer> DA_GUI_THOI_TIET = new ConcurrentHashMap<Integer, Integer>();

    static void guiThoiTietMoiNguoi(final boolean ep) {
        try {
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                final Char c = CharManager.instance.vChars.get(i);
                if (c == null || c.isBot != -1 && c.isBot != 0) {
                    continue;
                }
                if (ThoiTietGame.moi(c)) {
                    ThoiTietGame.gui(c, ep); // client >= 907: goi -118 (HUD, hat thoi tiet, phu mau mua, ngay cong dong)
                    continue;
                }
                if (real.Map.weather != null) {
                    continue; // su kien dang dung hieu ung thoi tiet toan may chu - khong de
                }
                final int hu = SinhThaiTheGioi.HIEU_UNG_CLIENT[thoiTietMap(c.mapID)];
                final Integer cu = DA_GUI_THOI_TIET.get(c.charDBID);
                if (!ep && cu != null && cu.intValue() == hu) {
                    continue;
                }
                if (cu == null && hu < 0) {
                    DA_GUI_THOI_TIET.put(c.charDBID, hu);
                    continue;
                }
                DA_GUI_THOI_TIET.put(c.charDBID, hu);
                c.sendMessage(MessageCreator.createMsgWeather(hu, hu < 0 ? 0 : (hu == SinhThaiTheGioi.HIEU_UNG_CLIENT[SinhThaiTheGioi.MUA]
                        ? 120 : 80), hu < 0 ? 0 : 3600000));
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] gui thoi tiet: " + e);
        }
    }

    // ================================================================== do vui "Ai la Pokemon?"

    static final class DoVui {
        static volatile String dapAn;
        static volatile long hetLuc;
        static volatile int luot;
        static volatile int ngayLuot;

        static void nhip() {
            final Calendar c = com.htbb.pokemon.DongHo.lich();
            final int g = c.get(Calendar.HOUR_OF_DAY);
            final int ph = c.get(Calendar.MINUTE);
            for (int i = 0; i < HeSinhThai.GIO_DO_VUI.length; i++) {
                if (g == HeSinhThai.GIO_DO_VUI[i] && ph == HeSinhThai.PHUT_DO_VUI && (ngayLuot != homNay() || luot <= i)) {
                    ngayLuot = homNay();
                    luot = i + 1;
                    batDau();
                }
            }
            if (dapAn != null && com.htbb.pokemon.DongHo.bay() > hetLuc) {
                baoTheGioi("Ai là Pokémon? Hết giờ! Đáp án: " + dapAn + ".");
                dapAn = null;
            }
        }

        static void batDau() {
            final List<Object[]> uv = new ArrayList<Object[]>();
            for (final MonsterTemplate t : real.Map.monsterTemplates.values()) {
                if (t != null && t.dex > 0 && t.name != null && !t.name.isEmpty() && MiniGameSinhThai.chuanHoa(t.name).length() >= 3) {
                    uv.add(new Object[]{t.name, (int) t.he, (int) t.he2});
                }
            }
            if (uv.isEmpty()) {
                return;
            }
            uv.sort((x, y) -> ((String) x[0]).compareTo((String) y[0]));
            final Object[] q = uv.get(MiniGameSinhThai.chonCauHoi(homNay(), (int) (com.htbb.pokemon.DongHo.bay() / 60000L), uv.size()));
            dapAn = (String) q[0];
            hetLuc = com.htbb.pokemon.DongHo.bay() + HeSinhThai.GIAY_TRA_LOI * 1000L;
            baoTheGioi("Ai là Pokémon? " + MiniGameSinhThai.goiY(dapAn, (Integer) q[1], (Integer) q[2])
                    + ". Gõ tên vào khung chat trong " + HeSinhThai.GIAY_TRA_LOI + " giây!");
        }

        static boolean traLoi(final Char p, final String msg) {
            final String a = dapAn;
            if (a == null || !MiniGameSinhThai.dung(msg, a)) {
                return false;
            }
            synchronized (DoVui.class) {
                if (dapAn == null) {
                    return true; // nguoi khac vua tra loi dung truoc - nuot chat de khong lo dap an
                }
                dapAn = null;
            }
            phatThuong(p, "LUONGKHOA:" + HeSinhThai.THUONG_DO_VUI_LUONG_KHOA, "do vui");
            real.DiemMocNhiemVu.khiDoVui(p); // diem moc do_vui
            final HoSoSinhThai h = hoSo(p);
            if (h != null) {
                h.doVuiDung++;
                if (h.doVuiDung >= 20) {
                    h.themDanhHieu("Thánh đố Pokémon");
                }
                danhDau(p);
            }
            baoTheGioi(p.charname + " đã trả lời đúng: " + a + "! (+" + HeSinhThai.THUONG_DO_VUI_LUONG_KHOA + " lượng khoá)");
            return true;
        }
    }

    // ================================================================== dua Pokemon

    static final class Dua {
        /** Ban do dua: tuyen duong Goldenrod (thiet_ke_map.csv: 18 duong). */
        static final int MAP_DUA = 18;
        static final int SO_MOC = 5;
        static final Map<Integer, int[]> NGUOI = new ConcurrentHashMap<Integer, int[]>(); // charDBID -> {moc, xong ms}
        static volatile long batDauLuc;
        static volatile boolean dangKy, dangChay;
        static volatile int[][] moc;
        static volatile int ngayDua;

        static void nhip() {
            final Calendar c = com.htbb.pokemon.DongHo.lich();
            final int g = c.get(Calendar.HOUR_OF_DAY);
            final int ph = c.get(Calendar.MINUTE);
            if (!dangKy && !dangChay && ngayDua != homNay() && g == HeSinhThai.GIO_DUA - 1
                    && ph >= 60 - HeSinhThai.PHUT_DANG_KY_DUA) {
                moDangKy(false);
            }
        }

        static void moDangKy(final boolean ngay) {
            if (dangChay) {
                return;
            }
            ngayDua = homNay();
            NGUOI.clear();
            moc = tinhMoc();
            if (moc == null || moc.length < 2) {
                return;
            }
            dangKy = true;
            batDauLuc = com.htbb.pokemon.DongHo.bay() + (ngay ? 60000L : HeSinhThai.PHUT_DANG_KY_DUA * 60000L);
            baoTheGioi("Đua Pokémon mở đăng ký! Gõ dua vào khung chat, rồi có mặt ở " + real.Map.getNameMap(MAP_DUA)
                    + " trước giờ xuất phát (" + (ngay ? "1 phút" : HeSinhThai.PHUT_DANG_KY_DUA + " phút") + ").");
        }

        static int[][] tinhMoc() {
            final byte[] b = real.Map.idMapSend2Client.get((short) MAP_DUA);
            if (b == null) {
                return null;
            }
            try {
                final DinhDangBanDo bd = DinhDangBanDo.doc(b);
                final boolean[] nuoc = BanDoSinhThai.luoiNuoc(bd, DinhDangBanDo.boTile(MAP_DUA));
                final boolean[] di = BanDoSinhThai.luoiDiDuoc(bd, KieuTile.lay(DinhDangBanDo.boTile(MAP_DUA)), nuoc);
                return MiniGameSinhThai.chonMocDua(di, bd.w, bd.h, SO_MOC, homNay());
            } catch (final Exception e) {
                System.out.println("[HE SINH THAI] moc dua: " + e);
                return null;
            }
        }

        static void dangKy(final Char p) {
            if (!dangKy) {
                bao(p, dangChay ? "Cuộc đua đang diễn ra." : "Chưa mở đăng ký. Đua Pokémon mở lúc " + (HeSinhThai.GIO_DUA - 1)
                        + ":" + (60 - HeSinhThai.PHUT_DANG_KY_DUA) + " mỗi ngày.");
                return;
            }
            NGUOI.put(p.charDBID, new int[]{0, 0});
            bao(p, "Đã đăng ký đua. Hãy tới " + real.Map.getNameMap(MAP_DUA) + ". Khi xuất phát, chạy qua " + moc.length
                    + " mốc theo thứ tự (toạ độ ô): " + moTaMoc());
        }

        static String moTaMoc() {
            final StringBuilder b = new StringBuilder();
            for (int i = 0; i < moc.length; i++) {
                b.append(i == 0 ? "" : ", ").append('(').append(moc[i][0]).append(',').append(moc[i][1]).append(')');
            }
            return b.toString();
        }

        static void nhipNhanh() {
            final long now = com.htbb.pokemon.DongHo.bay();
            if (dangKy && now >= batDauLuc) {
                dangKy = false;
                int coMat = 0;
                for (final Integer id : new ArrayList<Integer>(NGUOI.keySet())) {
                    final Char c = timOnline(id);
                    if (c == null || c.mapID != MAP_DUA) {
                        NGUOI.remove(id);
                    } else {
                        coMat++;
                        bao(c, "Xuất phát! Các mốc: " + moTaMoc());
                    }
                }
                if (coMat == 0) {
                    baoTheGioi("Đua Pokémon hôm nay huỷ: không ai có mặt ở vạch xuất phát.");
                    return;
                }
                dangChay = true;
                baoTheGioi("Đua Pokémon xuất phát với " + coMat + " tay đua!");
            }
            if (!dangChay) {
                return;
            }
            final List<Object[]> xong = new ArrayList<Object[]>();
            boolean conChay = false;
            for (final Map.Entry<Integer, int[]> e : NGUOI.entrySet()) {
                final int[] s = e.getValue();
                if (s[1] > 0) {
                    xong.add(new Object[]{e.getKey(), s[1]});
                    continue;
                }
                final Char c = timOnline(e.getKey());
                if (c == null || c.mapID != MAP_DUA) {
                    continue;
                }
                final int truoc = s[0];
                s[0] = MiniGameSinhThai.capNhatMoc(moc, s[0], c.x / 16, c.y / 16);
                if (s[0] != truoc) {
                    if (s[0] >= moc.length) {
                        s[1] = (int) Math.max(1, (now - batDauLuc) / 100L);
                        bao(c, "Về đích! Thời gian " + (s[1] / 10.0) + " giây.");
                        xong.add(new Object[]{e.getKey(), s[1]});
                    } else {
                        bao(c, "Qua mốc " + s[0] + "/" + moc.length + ". Mốc tiếp: (" + moc[s[0]][0] + "," + moc[s[0]][1] + ")");
                        conChay = true;
                    }
                } else {
                    conChay = true;
                }
            }
            if (!conChay || now - batDauLuc > HeSinhThai.DUA_TOI_DA_GIAY * 1000L) {
                ketThuc(xong);
            }
        }

        static void ketThuc(final List<Object[]> xong) {
            dangChay = false;
            xong.sort((a, b) -> Integer.compare((Integer) a[1], (Integer) b[1]));
            final StringBuilder b = new StringBuilder("Kết quả Đua Pokémon: ");
            for (int i = 0; i < xong.size() && i < HeSinhThai.THUONG_DUA_LUONG_KHOA.length; i++) {
                final int id = (Integer) xong.get(i)[0];
                final Char c = timOnline(id);
                final String ten = c != null ? c.charname : "(vắng mặt)";
                b.append(i == 0 ? "" : ", ").append(i + 1).append(". ").append(ten).append(" (")
                        .append(((Integer) xong.get(i)[1]) / 10.0).append(" giây)");
                if (c != null) {
                    phatThuong(c, "LUONGKHOA:" + HeSinhThai.THUONG_DUA_LUONG_KHOA[i], "dua hang " + (i + 1));
                    final HoSoSinhThai h = hoSo(c);
                    if (h != null && i == 0) {
                        h.duaThang++;
                        if (h.duaThang >= 5) {
                            h.themDanhHieu("Vua tốc độ");
                        }
                        danhDau(c);
                    }
                }
            }
            for (int i = 0; i < xong.size(); i++) {
                real.DiemMocNhiemVu.khiDua(timOnline((Integer) xong.get(i)[0]), i + 1); // diem moc dua_pokemon (moi nguoi ve dich)
            }
            if (xong.isEmpty()) {
                b.append("không ai về đích.");
            }
            baoTheGioi(b.toString());
            NGUOI.clear();
        }
    }

    // ================================================================== giai dau tuan

    // he sinh thai: goi tu MapLienDau khi p ha c (PvP). Chi ghi neu hai nguoi dang gap nhau o vong hien tai.
    public static void ketQuaTranPvp(final Char thang, final Char thua) {
        if (!bat() || thang == null || thua == null) {
            return;
        }
        try {
            GiaiDau.ketQua(thang.charDBID, thua.charDBID);
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] ketQuaTranPvp: " + e);
        }
    }

    /** Giai dau tuan: dang ky -> cong bo cay thu Bay 20h (hoac GM "hst giaidau") -> ket qua tu PvP MapLienDau. */
    static final class GiaiDau {
        static final Set<Integer> DANG_KY = ConcurrentHashMap.newKeySet();
        /** Cap vong hien tai {a, b (-1 = mien), nguoi thang (0 = chua)}. */
        static volatile List<int[]> vong;
        static volatile int soVong, tuanDaCongBo = -1;
        static final int THUONG_VO_DICH_LUONG_KHOA = 20;

        static void nhip() {
            final Calendar c = com.htbb.pokemon.DongHo.lich();
            final int tuan = c.get(Calendar.YEAR) * 100 + c.get(Calendar.WEEK_OF_YEAR);
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && c.get(Calendar.HOUR_OF_DAY) == 20 && tuanDaCongBo != tuan) {
                tuanDaCongBo = tuan;
                congBo();
            }
        }

        static synchronized void congBo() {
            final List<Integer> ds = new ArrayList<Integer>(DANG_KY);
            if (ds.size() < 2) {
                baoTheGioi("Giải đấu tuần: chưa đủ 2 người đăng ký, hoãn sang tuần sau.");
                return;
            }
            // Hat giong: cap cao truoc (nguoi offline xep cuoi), roi theo id.
            ds.sort((x, y) -> {
                final Char a = timOnline(x), b = timOnline(y);
                final int ca = a == null ? -1 : cap(a), cb = b == null ? -1 : cap(b);
                return ca != cb ? Integer.compare(cb, ca) : Integer.compare(x, y);
            });
            final int[] xep = new int[ds.size()];
            for (int i = 0; i < xep.length; i++) {
                xep[i] = ds.get(i);
            }
            soVong = 1;
            batDauVong(MiniGameSinhThai.capDauVong1(xep));
        }

        static void batDauVong(final int[][] cap) {
            final List<int[]> v = new ArrayList<int[]>();
            final StringBuilder b = new StringBuilder("Giải đấu tuần - vòng " + soVong + ": ");
            for (final int[] c : cap) {
                v.add(new int[]{c[0], c[1], c[1] < 0 ? c[0] : 0});
                b.append(ten(c[0])).append(" vs ").append(c[1] < 0 ? "(miễn đấu)" : ten(c[1])).append(", ");
            }
            vong = v;
            if (b.toString().endsWith(", ")) {
                b.setLength(b.length() - 2);
            }
            baoTheGioi(b.append(". Đấu tại Giải Liên minh (PvP).").toString());
            kiemXongVong();
        }

        static String ten(final int id) {
            final Char c = timOnline(id);
            return c != null ? c.charname : "(vắng mặt)";
        }

        static synchronized boolean ketQua(final int thang, final int thua) {
            final List<int[]> v = vong;
            if (v == null) {
                return false;
            }
            for (final int[] c : v) {
                if (c[2] == 0 && (c[0] == thang && c[1] == thua || c[1] == thang && c[0] == thua)) {
                    c[2] = thang;
                    baoTheGioi("Giải đấu tuần: " + ten(thang) + " thắng " + ten(thua) + " (vòng " + soVong + ").");
                    kiemXongVong();
                    return true;
                }
            }
            return false;
        }

        static void kiemXongVong() {
            final List<int[]> v = vong;
            final int[] thang = new int[v.size()];
            for (int i = 0; i < v.size(); i++) {
                if (v.get(i)[2] == 0) {
                    return;
                }
                thang[i] = v.get(i)[2];
            }
            if (thang.length == 1) {
                final int vd = thang[0];
                vong = null;
                DANG_KY.clear();
                final Char c = timOnline(vd);
                if (c != null) {
                    phatThuong(c, "LUONGKHOA:" + THUONG_VO_DICH_LUONG_KHOA, "vo dich giai tuan");
                    final HoSoSinhThai h = hoSo(c);
                    if (h != null) {
                        h.themDanhHieu("Nhà vô địch giải tuần");
                        danhDau(c);
                    }
                }
                baoTheGioi("Nhà vô địch giải tuần: " + ten(vd) + "!");
                return;
            }
            soVong++;
            batDauVong(MiniGameSinhThai.vongSau(thang));
        }

        static String moTa() {
            final List<int[]> v = vong;
            if (v == null) {
                return "Chưa có cây đấu (công bố thứ Bảy 20:00). Đã đăng ký: " + DANG_KY.size() + " người.";
            }
            final StringBuilder b = new StringBuilder("Giải đấu tuần - vòng " + soVong + ":\n");
            for (final int[] c : v) {
                b.append("- ").append(ten(c[0])).append(" vs ").append(c[1] < 0 ? "(miễn đấu)" : ten(c[1]))
                        .append(c[2] == 0 ? "" : ": thắng " + ten(c[2])).append('\n');
            }
            return b.toString().trim();
        }
    }

    // ================================================================== kieu tile (.type)

    /** Nap tep .type cua bo tile (cMap/) - cache. */
    static final class KieuTile {
        static final Map<String, byte[]> CACHE = new ConcurrentHashMap<String, byte[]>();

        static byte[] lay(final String bo) {
            return CACHE.computeIfAbsent(bo, (k) -> {
                try {
                    return java.nio.file.Files.readAllBytes(new java.io.File("cMap/" + k + ".type").toPath());
                } catch (final Exception e) {
                    return new byte[0];
                }
            });
        }
    }

    /** Thong tin ban do da phan tich: {w, h}, luoi nuoc, luoi di duoc - cache theo id nap. */
    static final class BanDoPhanTich {
        final int w, h, soNuoc;
        final boolean[] nuoc, diDuoc;
        /** Nhan ho moi o (-1 = khong phai ho) va so o moi ho - phan loai 1 lan khi nap ban do. */
        final int[] ho, kichThuocHo;

        BanDoPhanTich(final int idNap, final int w, final int h, final boolean[] nuoc, final boolean[] diDuoc) {
            this.w = w;
            this.h = h;
            this.nuoc = nuoc;
            this.diDuoc = diDuoc;
            this.soNuoc = BanDoSinhThai.demNuoc(nuoc);
            this.ho = nuoc == null ? null : BanDoSinhThai.nhanHo(nuoc, w, h);
            this.kichThuocHo = ho == null ? new int[0] : BanDoSinhThai.kichThuocHo(ho);
            this.diemCau = ho == null ? new ArrayList<int[]>() : BanDoSinhThai.diemCauCua(idNap);
        }

        /** Diem cau tu bang data_hst_ho_cau (BanDoSinhThai.diemCauCua): {hoId, x, y, so o}. */
        final List<int[]> diemCau;
    }

    static final Map<Integer, BanDoPhanTich> BAN_DO = new ConcurrentHashMap<Integer, BanDoPhanTich>();

    static BanDoPhanTich banDo(final Char p) {
        final int idNap = p.map != null ? p.map.getMapLoad(p.mapID) : p.mapID;
        return BAN_DO.computeIfAbsent(idNap, (k) -> {
            try {
                byte[] b = real.Map.idMapSend2Client.get((short) (int) k);
                if (b == null) {
                    b = java.nio.file.Files.readAllBytes(new java.io.File("cMap/" + k).toPath());
                }
                final DinhDangBanDo bd = DinhDangBanDo.doc(b);
                final String bo = DinhDangBanDo.boTile(k);
                final boolean[] nuoc = BanDoSinhThai.luoiNuoc(bd, bo);
                return new BanDoPhanTich(k, bd.w, bd.h, nuoc, BanDoSinhThai.luoiDiDuoc(bd, KieuTile.lay(bo), nuoc));
            } catch (final Exception e) {
                return new BanDoPhanTich(k, 0, 0, null, null);
            }
        });
    }
}
