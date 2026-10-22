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
    static final AtomicLong DEM_DOI = new AtomicLong(), DEM_HIEM = new AtomicLong(), DEM_BIEN_MAT = new AtomicLong(),
            DEM_SHINY = new AtomicLong();
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
            final boolean shinyCu = m.shinyRieng;
            if (!bat() || !duDieuKien(m)) {
                m.dexRieng = 0;
                m.bacRieng = 0;
                m.shinyRieng = false;
                return cu != 0 || shinyCu;
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
            final int vungMap = HeSinhThaiGame.vung(mapId);
            // VUNG HOANG DA (loai-bo-sung): ban do xoay vong them loai Gen 4-9 cua chu ky tuan nay - van chi qua luot
            // doi loai rieng (hoang da co dau) va gioi han khu cua 0.9.8
            kiemDoiTuan(d);
            final int nhomXoay = d.nhomBanDoXoay(mapId);   // 0 ban do cap 52-84, 1 cap thap (dang dau T1-T3)
            final java.util.List<LoaiDuLieu.Vung> xoay = nhomXoay >= 0
                    ? d.xoayVong(chuKyHienTai(d), nhomXoay, vungMap) : java.util.Collections.<LoaiDuLieu.Vung>emptyList();
            ChonLoaiHoangDa.KetQua kq = ChonLoaiHoangDa.chon(d, d.khuVuc(mapId), vungMap, m.level,
                    gio, SinhThaiTheGioi.laDem(gio), tt, dexMau(m), ts, R, xoay);
            if (kq.dex > 0 && demHoangDa(m) >= Math.max(0, ts.toiDaMap)) {
                kq = ChonLoaiHoangDa.GIU; // da du Pokemon hoang da tren ban do / khu nay: hoi sinh thanh quai thuong
            }
            m.dexRieng = kq.dex;
            m.bacRieng = kq.bac;
            m.shinyRieng = kq.dex > 0 && tungShiny(d, kq.dex);
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
            if (m.shinyRieng) {
                DEM_SHINY.incrementAndGet();
                baoShiny(m, d);
            }
            return cu != kq.dex || shinyCu != m.shinyRieng;
        } catch (final Throwable e) {
            System.out.println("[LOAI] khiHoiSinh: " + e);
            return false;
        }
    }

    /**
     * Tung shiny cho con quai vua co loai rieng (docs/thiet-ke-game.md 2.6: 1/4.096 - khoa shiny.mau_so - x tuan shiny
     * x10 / ngay cong dong x5 chi trong gio vang). Chi loai co goi shiny that (data_loai_sprite.shiny).
     */
    static boolean tungShiny(final LoaiDuLieu d, final int dex) {
        final int mau = (int) Math.max(1, com.htbb.pokemon.LauDai.so(com.htbb.pokemon.LauDai.K_SHINY_MAU,
                com.htbb.pokemon.CuonHut.SHINY_MAU));
        int nhan = 1;
        try {
            nhan = SuKienLauDaiGame.nhanShiny();
        } catch (final Throwable ignored) {
            // danh muc su kien chua nap: x1
        }
        return ChonLoaiHoangDa.shiny(d, dex, R.nextInt(mau), mau, nhan);
    }

    /** Shiny vua xuat hien: bao nguoi trong ban do thay duoc (client moi), moi ban do toi da 1 tin / phut. */
    static void baoShiny(final Monster m, final LoaiDuLieu d) {
        try {
            final String tin = "Một " + d.ten(m.dexRieng) + " sắc màu vừa xuất hiện ở " + Map.getNameMap(m.map.mapId) + "!";
            final java.util.Vector<Char> ds = m.map.getAllPlayer(m.inCountry, m.region);
            if (ds != null) {
                for (final Char p : new ArrayList<Char>(ds)) {
                    if (p != null && p.isBot == -1 && thayLoaiRieng(p)) {
                        p.sendMessage(MessageCreator.createthongbao(tin));
                    }
                }
            }
        } catch (final Throwable e) {
            System.out.println("[LOAI] baoShiny: " + e);
        }
    }

    /** Con quai {@code m} la shiny voi nguoi {@code p}: chi khi p thay dung loai rieng (client cu thay loai mau, khong shiny). */
    public static boolean shinyChoNguoi(final Char p, final Monster m) {
        return m != null && m.shinyRieng && m.dexRieng > 0 && thayLoaiRieng(p);
    }

    // ------------------------------------------------------------------ banner doi tuan Vung hoang da

    /** charDBID -> so tuan (tinh tu ngay goc) da bao banner. Trong RAM: khoi dong lai thi bao lai mot lan (vo hai). */
    static final ConcurrentHashMap<Integer, Long> DA_BAO_TUAN = new ConcurrentHashMap<Integer, Long>();
    static volatile long tuanDaPhat = Long.MIN_VALUE;

    /** So tuan hien tai tinh tu ngay goc lich (Long.MIN_VALUE neu chua co lich). */
    static long tuanHienTai(final LoaiDuLieu d) {
        if (d == null || d.lichXoay().isEmpty()) {
            return Long.MIN_VALUE;
        }
        final long hom = ngayHienTai();
        return hom < d.ngayGocXoay() ? Long.MIN_VALUE : Math.floorDiv(hom - d.ngayGocXoay(), 7L); // 0.9.8: chua toi ngay goc
    }

    /** Chu banner (thuan, test): {chinh, phu}. */
    public static String[] chuBanner(final LoaiDuLieu d, final int chuKy) {
        if (d == null || chuKy < 0 || chuKy >= d.lichXoay().size()) {
            return null;
        }
        final LoaiDuLieu.TuanXoay t = d.lichXoay().get(chuKy);
        return new String[]{"Vùng hoang dã: " + t.ten, t.moTa + " " + d.dexXoay(chuKy).size()
                + " loài, đổi vào thứ Hai hằng tuần. Độ hiếm không đổi - chỉ bắt được con hoang dã (dấu Poké Ball)."};
    }

    static void baoBanner(final Char p, final LoaiDuLieu d, final long tuan) {
        if (p == null || p.isBot != -1 || tuan == Long.MIN_VALUE || !thayLoaiRieng(p)) {
            return;   // client cu (khong thay loai rieng): khong hop thoai 37 moi tuan
        }
        final Long cu = DA_BAO_TUAN.put(Integer.valueOf(p.charDBID), Long.valueOf(tuan));
        if (cu != null && cu.longValue() == tuan) {
            return;
        }
        final String[] b = chuBanner(d, d.chuKyNgay(ngayHienTai()));
        if (b != null) {
            com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.SU_KIEN, b[0], b[1],
                    com.htbb.thongbao.ThongBao.HINH_BIEU_TUONG, com.htbb.thongbao.ThongBao.BT_SU_KIEN);
        }
    }

    /** CharNapDuLieu: lan dang nhap DAU TIEN trong tuan -> banner Vung hoang da tuan nay. */
    public static void khiDangNhap(final Char p) {
        try {
            final LoaiDuLieu d = bang();
            baoBanner(p, d, tuanHienTai(d));
        } catch (final Throwable e) {
            System.out.println("[LOAI] banner dang nhap: " + e);
        }
    }

    /** Goi tu khiHoiSinh (nhip quai): sang tuan moi (thu Hai 00:00) -> banner cho moi nguoi dang online. */
    static void kiemDoiTuan(final LoaiDuLieu d) {
        final long t = tuanHienTai(d);
        if (t == Long.MIN_VALUE || t == tuanDaPhat) {
            return;
        }
        final boolean dau = tuanDaPhat == Long.MIN_VALUE;
        tuanDaPhat = t;
        if (dau) {
            return;   // vua khoi dong: nguoi online da / se nhan khi dang nhap
        }
        for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
            try {
                baoBanner(CharManager.instance.vChars.elementAt(i), d, t);
            } catch (final Exception e) {
                break;
            }
        }
    }

    /** Ngay epoch theo gio may chu (DongHo) - cung mui gio voi lich tuan. */
    public static long ngayHienTai() {
        final Calendar c = com.htbb.pokemon.DongHo.lich();
        return java.time.LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)).toEpochDay();
    }

    /** Chu ky Vung hoang da tuan nay (-1 = chua co lich). */
    public static int chuKyHienTai(final LoaiDuLieu d) {
        if (d == null) {
            return -1;
        }
        final long hom = ngayHienTai();
        return hom < d.ngayGocXoay() ? -1 : d.chuKyNgay(hom); // 0.9.8: lich bat dau tu ngay goc (thu Hai 26/10/2026)
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
        ds.add(new int[]{mt.id, mt.dexRieng, mt.dexRieng > 0 && mt.shinyRieng ? 1 : 0, mt.dexRieng > 0 ? mt.bacRieng : 0});
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
                + ", bien mat " + DEM_BIEN_MAT.get() + ", dang co " + HOANG_DA.size() + ", shiny " + DEM_SHINY.get();
    }
}
