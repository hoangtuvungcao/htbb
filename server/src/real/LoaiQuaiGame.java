package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.ChonLoaiHoangDa;
import com.htbb.pokemon.GoiLoai;
import com.htbb.pokemon.HeSinhThaiGame;
import com.htbb.pokemon.LoaiDuLieu;
import com.htbb.pokemon.SinhThaiTheGioi;
import data.DatabaseLoai;
import io.Message;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Loai rieng tung con quai (he_thong_day_du.md 2.4, 3.5, 3.7; quyet dinh D5, D9). Logic thuan:
 * com.htbb.pokemon.ChonLoaiHoangDa (chon loai), LoaiDuLieu (bang loai), GoiLoai (goi -125 / -126).
 *
 * Diem goi:
 *   Monster.update (hoi sinh, sau HeSinhThaiGame / NoiDungLauDai)  -> {@link #khiHoiSinh}: doi loai thi Monster
 *       baoDoiLoai (lenh 20) de client them lai quai (lenh 4) roi nhan -125.
 *   Region (vua gui lenh 4 them quai cho nguoi choi)              -> {@link #sauThemQuai}: gui -125 cho client >= 908.
 *   PokedexPokemon.khiDanh / khiHaQuai                             -> {@link #dexChoNguoi} (client cu: loai cua mau).
 *
 * Client cu (< loai.ban_client) khong nhan -125 va thay loai mac dinh cua mau (fallback); Pokedex cua ho ghi loai
 * ho thay. Mau quai (cap, HP, TTK, rơi do, nhiem vu giet theo mau) KHONG doi.
 */
public final class LoaiQuaiGame {

    static final Random R = new Random();
    static final AtomicBoolean DANG_NAP = new AtomicBoolean();
    static volatile long lanNap;
    /** Nap lai bang moi 5 phut (doi dot mo / chinh tay tren CSDL khong can khoi dong lai). */
    static final long CHU_KY_NAP_MS = 5L * 60 * 1000;
    static final ConcurrentHashMap<Integer, Long> LAN_BAO_MAP = new ConcurrentHashMap<Integer, Long>();
    static final long CACH_BAO_MAP_MS = 60L * 1000;
    static final AtomicLong DEM_DOI = new AtomicLong(), DEM_HIEM = new AtomicLong(), DEM_BIEN_MAT = new AtomicLong();
    /**
     * 0.9.8 (chu game): Pokemon HOANG DA = quai mang loai rieng (dexRieng &gt; 0) - CHI loai nay bat duoc. Con song + luc
     * hien: de kep toi da moi ban do / khu ({@link ChonLoaiHoangDa.ThamSo#toiDaMap}) va cho bien mat khi khong ai giao
     * chien ({@link ChonLoaiHoangDa.ThamSo#phutBienMat}).
     */
    public static final ConcurrentHashMap<Monster, Long> HOANG_DA = new ConcurrentHashMap<Monster, Long>();

    /** Quai nay la Pokemon hoang da (bat duoc). */
    public static boolean laHoangDa(final Monster m) {
        return m != null && m.dexRieng > 0;
    }

    static String khoaKhu(final Monster m) {
        return (m.map == null ? -1 : m.map.mapId) + ":" + m.region + ":" + m.inCountry;
    }

    /** So Pokemon hoang da con song cung ban do / khu voi {@code m} (khong tinh chinh m). */
    static int demHoangDa(final Monster m) {
        final String k = khoaKhu(m);
        int n = 0;
        for (final Monster x : HOANG_DA.keySet()) {
            if (x == m) {
                continue;
            }
            if (x.isDead || x.dexRieng <= 0) {
                HOANG_DA.remove(x); // da chet / bi bat / bien mat
                continue;
            }
            if (k.equals(khoaKhu(x))) {
                n++;
            }
        }
        return n;
    }

    /**
     * Monster.update (con song): Pokemon hoang da khong ai giao chien (HP day, khong muc tieu) qua phutBienMat thi bien mat
     * (nhu bo chay - hoi sinh lai thanh quai thuong / luot chon moi).
     */
    public static void nhip(final Monster m) {
        if (m.dexRieng <= 0 || m.isDead) {
            return;
        }
        final Long t = HOANG_DA.get(m);
        if (t == null) {
            HOANG_DA.put(m, Long.valueOf(System.currentTimeMillis()));
            return;
        }
        final long phut = Math.max(1, thamSo().phutBienMat);
        if (System.currentTimeMillis() - t.longValue() > phut * 60000L && m.hp >= m.maxhp && m.target == null) {
            HOANG_DA.remove(m);
            DEM_BIEN_MAT.incrementAndGet();
            m.actorDie();
        }
    }

    private LoaiQuaiGame() {
    }

    // ------------------------------------------------------------------ cau hinh + bang

    public static boolean bat() {
        try {
            return CauHinhTrungTam.so("loai.bat", 1) != 0; // mac dinh danh muc 1 (danh muc chua nap: van bat)
        } catch (final Throwable e) {
            return true;
        }
    }

    public static int banClient() {
        try {
            return (int) CauHinhTrungTam.so("loai.ban_client", 908);
        } catch (final Throwable e) {
            return 908;
        }
    }

    static long so(final String k, final long md) {
        try {
            return CauHinhTrungTam.so(k, md);
        } catch (final Throwable e) {
            return md;
        }
    }

    static ChonLoaiHoangDa.ThamSo thamSo() {
        final ChonLoaiHoangDa.ThamSo t = new ChonLoaiHoangDa.ThamSo();
        t.doiThuongPhanTram = (int) so("loai.doi_thuong_phan_tram", t.doiThuongPhanTram);
        t.gapT3 = (int) so("loai.gap_t3", t.gapT3);
        t.gapT4 = (int) so("loai.gap_t4", t.gapT4);
        t.gapT5 = (int) so("loai.gap_t5", t.gapT5);
        t.heSoToiDaPhanTram = (int) so("loai.he_so_toi_da_phan_tram", t.heSoToiDaPhanTram);
        return t;
    }

    /** Bang loai hien tai; tu nap lai (luong nen) khi qua chu ky. Khong bao gio chan luong ban do. */
    public static LoaiDuLieu bang() {
        final long now = System.currentTimeMillis();
        if (now - lanNap > CHU_KY_NAP_MS && DANG_NAP.compareAndSet(false, true)) {
            lanNap = now;
            final Thread t = new Thread(() -> {
                try {
                    final LoaiDuLieu d = DatabaseLoai.nap();
                    if (d != null) {
                        final boolean dau = LoaiDuLieu.hien().rong();
                        LoaiDuLieu.dat(d);
                        if (dau) {
                            System.out.println("[LOAI] nap " + d.soLoai() + " loai (" + d.soLoaiDaMo() + " da mo), "
                                    + d.tatCaTienHoa().size() + " buoc tien hoa, " + d.tatCaVung().size() + " dong vung");
                        }
                    }
                } catch (final Throwable e) {
                    System.out.println("[LOAI] nap loi: " + e);
                } finally {
                    DANG_NAP.set(false);
                }
            }, "loai-nap");
            t.setDaemon(true);
            t.start();
        }
        return LoaiDuLieu.hien();
    }

    // ------------------------------------------------------------------ loai cua quai

    static int dexMau(final Monster m) {
        final MonsterTemplate t = m == null ? null : Map.monsterTemplates.get(Integer.valueOf(m.idTemplate));
        return t == null ? 0 : t.dex;
    }

    /** Loai THAT cua con quai (rieng neu co, khong thi cua mau). */
    public static int dex(final Monster m) {
        if (m == null) {
            return 0;
        }
        return m.dexRieng > 0 ? m.dexRieng : dexMau(m);
    }

    /** Loai {@code p} nhin thay: client cu khong nhan -125 nen thay (va ghi Pokedex) loai cua mau. */
    public static int dexChoNguoi(final Char p, final Monster m) {
        if (m == null) {
            return 0;
        }
        return dexHienThi(m.dexRieng, dexMau(m), p != null && p.session != null ? p.session.banClient : -1, banClient());
    }

    /**
     * Loai client so ban {@code banClient} thay (thuan, test KiemLoaiDayDu): client >= {@code banToiThieu} thay loai
     * rieng, client cu (va PC Unity gui -1) thay loai cua mau.
     */
    public static int dexHienThi(final int dexRieng, final int dexMau, final int banClient, final int banToiThieu) {
        return dexRieng > 0 && banClient >= banToiThieu ? dexRieng : dexMau;
    }

    static boolean thayLoaiRieng(final Char p) {
        return p != null && p.session != null && p.session.banClient >= banClient();
    }

    static boolean duDieuKien(final Monster m) {
        if (m == null || m.map == null || m.map.getClass() != Map.class || m.isBoss || m.idTemplate == 83
                || m.isMaterialMons() || m.isMonsterVantieu()) {
            return false;
        }
        if (HeSinhThaiGame.laMapKhongDong(m.map.mapId) || HeSinhThaiGame.laQuaiBaoVe(m.idTemplate)
                || HeSinhThaiGame.laQuaiDacBiet(m)) {
            return false;
        }
        final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(m.idTemplate));
        return t != null && t.dex > 0 && !com.htbb.pokemon.GoiPokemon.laTemplateBoss(t.id, t.maxhp);
    }

    /** Monster.update luc hoi sinh. Tra ve true neu loai rieng doi (noi goi bao client them lai quai). */
    public static boolean khiHoiSinh(final Monster m) {
        try {
            final int cu = m.dexRieng;
            if (!bat() || !duDieuKien(m)) {
                m.dexRieng = 0;
                m.bacRieng = 0;
                return cu != 0;
            }
            final LoaiDuLieu d = bang();
            if (d.rong()) {
                return false;
            }
            final int mapId = m.map.mapId;
            final int gio = com.htbb.pokemon.DongHo.lich().get(Calendar.HOUR_OF_DAY);
            final int tt = HeSinhThaiGame.thoiTietMap(mapId);
            HOANG_DA.remove(m);
            final ChonLoaiHoangDa.ThamSo ts = thamSo();
            ChonLoaiHoangDa.KetQua kq = ChonLoaiHoangDa.chon(d, d.khuVuc(mapId), HeSinhThaiGame.vung(mapId), m.level,
                    gio, SinhThaiTheGioi.laDem(gio), tt, dexMau(m), ts, R);
            if (kq.dex > 0 && demHoangDa(m) >= Math.max(0, ts.toiDaMap)) {
                kq = ChonLoaiHoangDa.GIU; // da du Pokemon hoang da tren ban do / khu nay: hoi sinh thanh quai thuong
            }
            m.dexRieng = kq.dex;
            m.bacRieng = kq.bac;
            if (kq.dex > 0) {
                HOANG_DA.put(m, Long.valueOf(System.currentTimeMillis()));
                m.daCoLoaiRieng = true;
                DEM_DOI.incrementAndGet();
                if (kq.bac >= LoaiDuLieu.T3) {
                    DEM_HIEM.incrementAndGet();
                }
                if (kq.bac >= LoaiDuLieu.T3) {
                    bao(m, d, kq); // 0.9.8: T3 + T4 bao trong ban do, T5 them toan may chu
                }
            }
            return cu != kq.dex;
        } catch (final Throwable e) {
            System.out.println("[LOAI] khiHoiSinh: " + e);
            return false;
        }
    }

    /** T3+ bao nguoi trong ban do (khong bao toan may chu de tranh don nguoi); T5 bao toan may chu (2.4). */
    static void bao(final Monster m, final LoaiDuLieu d, final ChonLoaiHoangDa.KetQua kq) {
        try {
            baoNgay(m, d, kq);
        } catch (final Throwable e) {
            System.out.println("[LOAI] bao: " + e);
        }
    }

    static void baoNgay(final Monster m, final LoaiDuLieu d, final ChonLoaiHoangDa.KetQua kq) {
        final int mapId = m.map.mapId;
        final long now = System.currentTimeMillis();
        final Long cu = LAN_BAO_MAP.get(Integer.valueOf(mapId));
        if (cu != null && now - cu.longValue() < CACH_BAO_MAP_MS) {
            return;
        }
        LAN_BAO_MAP.put(Integer.valueOf(mapId), Long.valueOf(now));
        final String tin = "Một " + d.ten(kq.dex) + " hoang dã (" + LoaiDuLieu.TEN_BAC[kq.bac] + ") vừa xuất hiện ở "
                + Map.getNameMap(mapId) + " - bắt được bằng Poké Ball!";
        try {
            final java.util.Vector<Char> ds = m.map.getAllPlayer(m.inCountry, m.region);
            if (ds != null) {
                for (final Char p : new ArrayList<Char>(ds)) {
                    if (p != null && p.isBot == -1 && thayLoaiRieng(p)) {
                        p.sendMessage(MessageCreator.createthongbao(tin));
                    }
                }
            }
            if (kq.bac >= LoaiDuLieu.T5) {
                com.htbb.chongspam.ThongBaoGop.dua("sinh_thai", tin);
            }
        } catch (final Throwable e) {
            System.out.println("[LOAI] bao: " + e);
        }
    }

    // ------------------------------------------------------------------ goi -125

    /** Region vua gui lenh 4 them quai {@code mt} cho {@code p}. */
    public static void sauThemQuai(final Char p, final Monster mt) {
        if (mt == null || (mt.dexRieng <= 0 && !mt.daCoLoaiRieng) || !thayLoaiRieng(p)) {
            return;
        }
        final List<int[]> ds = new ArrayList<int[]>(1);
        ds.add(new int[]{mt.id, mt.dexRieng, 0, mt.dexRieng > 0 ? mt.bacRieng : 0});
        final Message m = taoQuaiCon(ds);
        if (m != null) {
            p.sendMessage(m);
        }
    }

    static Message taoQuaiCon(final List<int[]> ds) {
        final Message m = new Message(GoiLoai.POKE_QUAI_CON);
        try {
            GoiLoai.ghiQuaiCon(m.dos, ds);
        } catch (final Exception e) {
            return null;
        }
        return m;
    }

    /** Thong ke cho log / quan tri. */
    public static String thongKe() {
        final LoaiDuLieu d = LoaiDuLieu.hien();
        return "loai " + d.soLoai() + " (mo " + d.soLoaiDaMo() + "), hoang da " + DEM_DOI.get() + ", hiem T3+ " + DEM_HIEM.get()
                + ", bien mat " + DEM_BIEN_MAT.get() + ", dang co " + HOANG_DA.size();
    }
}
