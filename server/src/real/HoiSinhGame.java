package real;

import com.htbb.pokemon.HoiSinh;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.ThongSoLauDai;
import data.Database;
import io.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Noi GUC / HOI SINH (com.htbb.pokemon.HoiSinh, docs/thiet-ke-game.md) vao game. Cho goi:
 * <ul>
 *   <li>Char.actorDie -&gt; {@link #khiGuc} (ghi lan guc, tu dong danh: tu dung vat pham hoac dung + ve Trung tam);</li>
 *   <li>CharVongLap (1 giay sau guc) -&gt; {@link #sauMotGiay} (mo hop thoai thay popup cu), moi nhip -&gt; {@link #nhip};</li>
 *   <li>MapXuLyLenh lenh -101 -&gt; {@link #xuLy} (client moi: xin / chon); MapDoMenu -&gt; {@link #xuLyMenu}
 *       (client cu: menu -803; dong doi cuu: menu -804);</li>
 *   <li>Map popup cu (-1 / 1) va menu -802 -&gt; {@link #chanCu} (mo hop thoai moi thay luong cu);</li>
 *   <li>Map.doComeHomeWhenDie -&gt; {@link #diemVeTrungTam} (Hoenn o lai Hoenn) + {@link #sauVeTrungTam} (phat nhe);</li>
 *   <li>Char.getAttack / ExpLauDai.traoHaQuai -&gt; {@link #apMetTanCong} / {@link #apMetExp} (Met moi);</li>
 *   <li>VatPhamDungLauDai (dung Thuoc hoi sinh toi da tu tui) -&gt; {@link #chanDungTuTui} / {@link #daHoiSinh}.</li>
 * </ul>
 * Lenh -101 (hai chieu). Server -&gt; client: byte 0 MO {UTF tieu de, UTF mo ta, byte n, n x {byte ma, UTF ten,
 * UTF chi tiet, byte bam duoc(khong tinh dem nguoc), UTF ly do khoa, short giay cho}, short giay cho dong doi};
 * byte 1 DONG. Client -&gt; server: byte 0 XIN (mo / mo lai hop thoai), byte 1 CHON + byte ma.
 */
public final class HoiSinhGame {

    private HoiSinhGame() {
    }

    public static final int LENH = -101;
    public static final int S_MO = 0, S_DONG = 1;
    public static final int C_XIN = 0, C_CHON = 1;
    /** Menu -30 cho client cu (khong biet lenh -101) va menu moi dong doi cuu. */
    public static final int MENU_CU = -803, MENU_DONG_DOI = -804, MENU_MUA_THUOC = -805;
    /** So luong mua mot lan trong hop xac nhan. */
    static final int[] SO_MUA = {1, 5, 10};
    /** id dong dem nguoc -75 cua "Met moi" (ngoai day Char.ID_* 0..18). */
    public static final int ID_MET_MOI = 47;

    // ------------------------------------------------------------------ chi so (ChiSoMayChu "hoiSinh", cong don)

    public static final AtomicLong SO_GUC = new AtomicLong();
    public static final AtomicLong SO_TAN_THU = new AtomicLong();
    public static final AtomicLong SO_THUOC = new AtomicLong();
    public static final AtomicLong SO_THUOC_TOI_DA = new AtomicLong();
    public static final AtomicLong SO_XU = new AtomicLong();
    public static final AtomicLong SO_LUONG_KHOA = new AtomicLong();
    public static final AtomicLong SO_TRUNG_TAM = new AtomicLong();
    public static final AtomicLong SO_DONG_DOI = new AtomicLong();
    public static final AtomicLong SO_TU_DONG_THUOC = new AtomicLong();
    public static final AtomicLong SO_TU_DONG_VE = new AtomicLong();
    public static final AtomicLong SO_KHAC = new AtomicLong();
    public static final AtomicLong XU_DOT = new AtomicLong();
    public static final AtomicLong LUONG_KHOA_DOT = new AtomicLong();
    public static final AtomicLong EXP_MAT = new AtomicLong();

    /** JSON cho ChiSoMayChu (trang_thai.json). */
    public static String json() {
        return "{\"guc\":" + SO_GUC.get() + ",\"tanThu\":" + SO_TAN_THU.get() + ",\"thuoc\":" + SO_THUOC.get()
                + ",\"thuocToiDa\":" + SO_THUOC_TOI_DA.get() + ",\"xu\":" + SO_XU.get() + ",\"luongKhoa\":" + SO_LUONG_KHOA.get()
                + ",\"trungTam\":" + SO_TRUNG_TAM.get() + ",\"dongDoi\":" + SO_DONG_DOI.get()
                + ",\"tuDongThuoc\":" + SO_TU_DONG_THUOC.get() + ",\"tuDongVe\":" + SO_TU_DONG_VE.get()
                + ",\"khac\":" + SO_KHAC.get() + ",\"xuDot\":" + XU_DOT.get() + ",\"luongKhoaDot\":" + LUONG_KHOA_DOT.get()
                + ",\"expMat\":" + EXP_MAT.get() + "}";
    }

    // ------------------------------------------------------------------ cau hinh (cache 5 giay - getAttack goi rat nhieu)

    private static volatile HoiSinh.CauHinh CH;
    private static volatile long CH_LUC;

    static HoiSinh.CauHinh ch() {
        final long bay = System.currentTimeMillis();
        HoiSinh.CauHinh c = CH;
        if (c == null || bay - CH_LUC > 5000L || bay < CH_LUC) {
            c = HoiSinh.cauHinh();
            CH = c;
            CH_LUC = bay;
        }
        return c;
    }

    /** Co che hoi sinh moi dang bat (hoi_sinh.bat). */
    public static boolean bat() {
        return ch().bat;
    }

    // ------------------------------------------------------------------ trang thai theo nhan vat

    static final class TT {
        final HoiSinh.LichSu ls = new HoiSinh.LichSu();
        /** Nhan vat dang online (dang nhap lai = doi tuong moi). */
        Char nguoi;
        /** Client cua {@link #nguoi} biet lenh -101. */
        boolean clientMoi;
        boolean dangGuc;
        int lan = 1;
        /** Tu dong danh: viec hen (HoiSinh.LC_*) luc {@link #tuDongLuc}; -1 = khong. */
        int tuDongLam = -1;
        long tuDongLuc;
        long choDongDoiDen;
        boolean daChoDongDoi;
        /** Menu -803 dang hien: vi tri -> ma lua chon. */
        int[] menuCu = new int[0];
    }

    private static final ConcurrentHashMap<Integer, TT> DS = new ConcurrentHashMap<Integer, TT>();
    /** Nguoi cuu (charDBID) -> {Char nguoi guc, int[] ma vat pham theo vi tri menu}. */
    private static final ConcurrentHashMap<Integer, Object[]> CUU = new ConcurrentHashMap<Integer, Object[]>();

    static boolean laNguoi(final Char p) {
        return p != null && p.isBot == -1 && p.charDBID > 0 && !p.isMonster && p.cat == 0;
    }

    static TT tt(final Char p, final boolean tao) {
        TT t = DS.get(Integer.valueOf(p.charDBID));
        if (t == null && tao) {
            t = new TT();
            final TT cu = DS.putIfAbsent(Integer.valueOf(p.charDBID), t);
            if (cu != null) {
                t = cu;
            }
        }
        if (t != null && t.nguoi != p) {
            synchronized (t) {
                if (t.nguoi != p) {
                    t.nguoi = p;
                    t.clientMoi = false;
                }
            }
        }
        return t;
    }

    /** Co che moi ap dung cho guc nay (nguoi that, ban do thuong). Ban do co luat rieng giu nguyen luong cu. */
    public static boolean apDung(final Char p) {
        if (!laNguoi(p) || p.map == null || !bat()) {
            return false;
        }
        final Map m = p.map;
        if (m.isMapChienTruongMoba() || m.isMapLoiDai() || m.isMapNuiChauBau() || p.mapID == 204) {
            return false; // tu hoi sinh theo luat san (moba / loi dai / nui chau bau / dau truong 2)
        }
        try {
            if (p.myCountry < 0 || Map.idMapMONSTER.get(p.myCountry).contains(m.mapId)) {
                return false; // ban do su kien "hoa quai"
            }
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ hook guc

    /** HOOK Char.actorDie (nguoi choi). Goi lap lai trong cung lan guc thi bo qua. */
    public static void khiGuc(final Char p) {
        try {
            if (!apDung(p)) {
                return;
            }
            final HoiSinh.CauHinh c = ch();
            final TT t = tt(p, true);
            final long bay = System.currentTimeMillis();
            synchronized (t) {
                if (t.dangGuc && bay - t.ls.gucLuc < 3000L) {
                    return; // Monster goi actorDie 2 lan
                }
                t.dangGuc = true;
                t.lan = t.ls.ghiGuc(bay, c.cuaSoPhut * 60000L);
                t.tuDongLam = -1;
                t.choDongDoiDen = 0L;
                t.daChoDongDoi = false;
            }
            SO_GUC.incrementAndGet();
            NhiemVuEngine.suKien(p, "guc", "", 1); // diem moc guc (danh hieu an "gục ngã trăm lần")
            don(bay, c);
            p.timeHoiSinh = 0L; // khong tu hoi sinh tai cho theo gio nua (truoc day: duoi cap 50 o moi bai luyen)
            if (TuDongGame.dangTuDong(p)) {
                tuDongKhiGuc(p, c, t, bay);
            }
        } catch (final Exception e) {
            System.out.println("HoiSinhGame.khiGuc: " + e);
        }
    }

    /** Tu dong danh: vat pham hoi sinh (neu nguoi choi bat Tu hoi sinh + con luot / gio) hoac dung + ve Trung tam. */
    static void tuDongKhiGuc(final Char p, final HoiSinh.CauHinh c, final TT t, final long bay) {
        final HoiSinh.TinhHuong th = tinhHuong(p, c, t, bay);
        int lam = HoiSinh.chonTuDong(true, th, TuDongGame.choHoiSinhTraPhi(p)); // vat pham luong: chi khi dong y rieng
        if (lam != HoiSinh.LC_TRUNG_TAM && !TuDongGame.choHoiSinhTuDong(p)) {
            lam = HoiSinh.LC_TRUNG_TAM; // khong bat "Tu hoi sinh" hoac het luot tu_dong.hoi_sinh_moi_gio
        }
        synchronized (t) {
            t.tuDongLam = lam;
            if (lam == HoiSinh.LC_TRUNG_TAM) {
                t.tuDongLuc = bay + 3000L;
            } else {
                t.tuDongLuc = bay + Math.max(2000L, HoiSinh.choTaiCho(c, th, bay) * 1000L);
            }
        }
        if (lam == HoiSinh.LC_TRUNG_TAM) {
            TuDongGame.dungKhiChet(p);
            bao(p, "Tự động đánh dừng: Pokémon đã gục. Sẽ về Trung tâm Pokémon sau 3 giây.");
        }
    }

    /** HOOK CharVongLap: 1 giay sau guc. @return true = da xu ly (khong hien popup cu). */
    public static boolean sauMotGiay(final Char p) {
        try {
            if (!apDung(p) || p.hp > 0) {
                return false;
            }
            final TT t = tt(p, true);
            if (!t.dangGuc) {
                khiGuc(p);
            }
            if (t.tuDongLam >= 0) {
                return true; // tu dong danh tu xu ly
            }
            moHop(p);
            return true;
        } catch (final Exception e) {
            System.out.println("HoiSinhGame.sauMotGiay: " + e);
            return false;
        }
    }

    /** HOOK popup / menu cu (-1, 1, -802). @return true = da mo hop thoai moi (bo luong cu). */
    public static boolean chanCu(final Char p) {
        try {
            if (!apDung(p)) {
                return false;
            }
            if (p.hp > 0) {
                return true; // con song: luong cu cung khong lam gi
            }
            moHop(p);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    /** HOOK moi nhip CharVongLap (re: chi tra bang khi dang guc / met moi / dang cuu). */
    public static void nhip(final Char p) {
        if (p.hp > 0 && p.hsMetDen == 0L) {
            final TT t0 = p.hp > 0 ? DS.get(Integer.valueOf(p.charDBID)) : null;
            if (t0 == null || !t0.dangGuc) {
                return;
            }
        }
        try {
            final long bay = System.currentTimeMillis();
            if (p.hsMetDen != 0L && bay >= p.hsMetDen) {
                p.hsMetDen = 0L;
                bao(p, "Hết Mệt mỏi: EXP và tấn công trở lại bình thường.");
            }
            final TT t = DS.get(Integer.valueOf(p.charDBID));
            if (t == null || t.nguoi != p) {
                return;
            }
            if (t.dangGuc && p.hp > 0) {
                // hoi sinh bang duong khac (chieu Phuoc lanh hoi sinh, GM, su kien...)
                synchronized (t) {
                    t.dangGuc = false;
                    t.tuDongLam = -1;
                    t.choDongDoiDen = 0L;
                }
                SO_KHAC.incrementAndGet();
                dong(p);
                return;
            }
            if (!t.dangGuc || p.hp > 0) {
                return;
            }
            final int lam;
            synchronized (t) {
                lam = t.tuDongLam >= 0 && bay >= t.tuDongLuc ? t.tuDongLam : -1;
                if (lam >= 0) {
                    t.tuDongLam = -1;
                }
            }
            if (lam == HoiSinh.LC_TRUNG_TAM) {
                SO_TU_DONG_VE.incrementAndGet();
                veTrungTam(p);
                return;
            }
            if (lam >= 0) {
                if (dungVatPham(p, lam, "tu_dong")) {
                    SO_TU_DONG_THUOC.incrementAndGet();
                    bao(p, "Tự động đánh: đã dùng vật phẩm hồi sinh, tiếp tục luyện tập.");
                } else {
                    TuDongGame.dungKhiChet(p);
                    SO_TU_DONG_VE.incrementAndGet();
                    veTrungTam(p);
                }
                return;
            }
            if (t.choDongDoiDen > 0L && bay >= t.choDongDoiDen) {
                synchronized (t) {
                    t.choDongDoiDen = 0L;
                }
                huyCuu(p);
                bao(p, "Đồng đội chưa kịp hồi sinh bạn.");
                moHop(p);
            }
        } catch (final Exception e) {
            System.out.println("HoiSinhGame.nhip: " + e);
        }
    }

    // ------------------------------------------------------------------ tinh huong + hop thoai

    /** Vat pham Thuoc hoi sinh toi da (nhom hoi_sinh cua data_vat_pham_lau_dai) trong tui: {so, %HP lon nhat}. */
    static int[] demVatPham(final Char p) {
        int so = 0, hp = 100;
        boolean co = false;
        try {
            final Vector<Item> tui = p.iItems;
            for (int i = 0; i < tui.size(); i++) {
                final int h = hpVatPham(tui.get(i));
                if (h > 0) {
                    so++;
                    hp = co ? Math.max(hp, h) : h;
                    co = true;
                }
            }
        } catch (final Exception e) {
            // tui doi giua chung
        }
        return new int[]{so, hp};
    }

    /** %HP hoi sinh cua vat pham (0 = khong phai vat pham hoi sinh). */
    static int hpVatPham(final Item it) {
        if (it == null || it.getTemplate() == null) {
            return 0;
        }
        final Object[] d = VatPhamDungLauDai.BANG.get(Integer.valueOf(it.getTemplate().id));
        if (d != null) {
            final HashMap<String, String> hu = VatPhamDungLauDai.hieuUng((String) d[2]);
            if (hu.containsKey("hoi_sinh")) {
                return VatPhamDungLauDai.so(hu, "hoi_sinh", 30);
            }
            return 0;
        }
        return it.getTemplate().id == HoiSinh.ITEM_HOI_SINH_TOI_DA ? 100 : 0;
    }

    static Item layVatPham(final Char p) {
        Item tot = null;
        int hp = 0;
        try {
            for (int i = 0; i < p.iItems.size(); i++) {
                final Item it = p.iItems.get(i);
                final int h = hpVatPham(it);
                if (h > hp) {
                    hp = h;
                    tot = it;
                }
            }
        } catch (final Exception e) {
            return null;
        }
        return tot;
    }

    /** Ly do cam hoi sinh tai cho o vi tri hien tai (null = duoc). */
    static String camTaiCho(final Char p, final HoiSinh.CauHinh c) {
        final Map m = p.map;
        if (m == null) {
            return null;
        }
        if (com.htbb.pokemon.PhongRieng.laMapRieng(m.mapId)) {
            return com.htbb.pokemon.PhongRieng.LY_DO_HOI_SINH; // pho ban: guc thi ve Trung tam (= roi phong)
        }
        boolean pvp = m instanceof MapLoiDai || m instanceof MapLienDau || m instanceof MapChienTruongMoba || m instanceof MapClan
                || m.isMapChoLoiDai();
        try {
            pvp = pvp || m.inArena(p) || (m instanceof MapTown && Map.getTown[Math.max(0, p.inCountry)]);
        } catch (final Exception ignored) {
        }
        final boolean gym = BossLauDai.gymDangDanh(p) > 0;
        final boolean boss = BossLauDai.dangDanhBoss(p);
        final boolean meCung = m.isvanTienTran();
        final String ly = HoiSinh.lyDoCam(c, m.mapId, pvp || (m.mapIDLoadMap != m.mapId && contains(c.mapPvp, m.mapIDLoadMap)),
                gym, boss, meCung);
        if (ly != null) {
            return ly;
        }
        return m.mapIDLoadMap != m.mapId && contains(c.mapCamThem, m.mapIDLoadMap) ? "Bản đồ này không cho hồi sinh tại chỗ." : null;
    }

    static boolean contains(final int[] a, final int v) {
        for (final int x : a) {
            if (x == v) {
                return true;
            }
        }
        return false;
    }

    /** Dong doi o gan (cung map / phe / khu, con song) - chi nhung nguoi mang vat pham hoi sinh khi {@code canVatPham}. */
    static List<Char> dongDoiGan(final Char p, final boolean canVatPham) {
        final List<Char> ds = new ArrayList<Char>();
        if (p.party == null || p.party.userParty == null) {
            return ds;
        }
        for (int i = 0; i < p.party.userParty.size(); i++) {
            final Char pp;
            try {
                pp = p.party.userParty.get(i);
            } catch (final Exception e) {
                break;
            }
            if (pp == null || pp == p || pp.charDBID == p.charDBID || pp.hp <= 0 || pp.mapID != p.mapID
                    || pp.inCountry != p.inCountry || pp.region != p.region) {
                continue;
            }
            if (canVatPham && pp.potions[HoiSinh.THUOC_HOI_SINH] <= 0 && demVatPham(pp)[0] <= 0) {
                continue;
            }
            ds.add(pp);
        }
        return ds;
    }

    static String tenVung(final Char p) {
        try {
            final int[] hs = diemHoenn(p);
            if (hs != null) {
                final com.htbb.pokemon.BanDoHoenn.BanDo b = com.htbb.pokemon.BanDoHoenn.lay(hs[0]);
                return b == null ? "Hoenn" : b.ten;
            }
            return "làng nhà";
        } catch (final Exception e) {
            return "";
        }
    }

    static HoiSinh.TinhHuong tinhHuong(final Char p, final HoiSinh.CauHinh c, final TT t, final long bay) {
        final HoiSinh.TinhHuong th = new HoiSinh.TinhHuong();
        th.cap = p.lvDetail.lv;
        th.lan = t.lan;
        th.gucLuc = t.ls.gucLuc;
        th.lanTaiChoCuoi = t.ls.lanTaiChoCuoi;
        th.soThuoc = p.potions[HoiSinh.THUOC_HOI_SINH];
        final int[] vp = demVatPham(p);
        th.soThuocToiDa = vp[0];
        th.hpThuocToiDa = vp[1];
        th.xu = p.getxu();
        th.luongKhoa = p.lockLuong;
        th.camTaiCho = camTaiCho(p, c);
        th.coDongDoi = c.dongDoiBat && !dongDoiGan(p, true).isEmpty();
        th.daChoDongDoi = t.daChoDongDoi;
        th.choDongDoiDen = t.choDongDoiDen;
        th.tenVung = tenVung(p);
        th.thuocTrongGio = t.ls.thuocTrongGio(bay);
        th.conLuotThuoc = HoiSinh.conLuotThuoc(c, th.thuocTrongGio);
        return th;
    }

    /** Mo (lai) hop thoai guc: client moi = goi -101, client cu = menu -803. */
    static void moHop(final Char p) {
        final HoiSinh.CauHinh c = ch();
        final TT t = tt(p, true);
        final long bay = System.currentTimeMillis();
        final HoiSinh.TinhHuong th = tinhHuong(p, c, t, bay);
        final List<HoiSinh.LuaChon> ds = HoiSinh.dsLuaChon(c, th, bay);
        final String tieuDe = "Pokémon của bạn đã gục!";
        final String moTa = HoiSinh.moTa(c, th);
        if (t.clientMoi) {
            try {
                final Message m = new Message(LENH);
                m.dos.writeByte(S_MO);
                m.dos.writeUTF(tieuDe);
                m.dos.writeUTF(moTa);
                m.dos.writeByte(ds.size());
                for (final HoiSinh.LuaChon l : ds) {
                    m.dos.writeByte(l.ma);
                    m.dos.writeUTF(l.ten);
                    m.dos.writeUTF(l.chiTiet);
                    m.dos.writeByte(l.khoa == null ? 1 : 0);
                    m.dos.writeUTF(l.khoa == null ? "" : l.khoa);
                    m.dos.writeShort(Math.min(Short.MAX_VALUE, l.choGiay));
                }
                m.dos.writeShort(th.choDongDoiDen > bay ? (int) Math.min(Short.MAX_VALUE, (th.choDongDoiDen - bay + 999L) / 1000L) : 0);
                p.sendMessage(m);
            } catch (final Exception e) {
                // mat ket noi
            }
            return;
        }
        final String[] menu = new String[ds.size()];
        final int[] ma = new int[ds.size()];
        for (int i = 0; i < ds.size(); i++) {
            final HoiSinh.LuaChon l = ds.get(i);
            ma[i] = l.ma;
            menu[i] = l.ten + " - " + l.chiTiet + (l.khoa != null ? " [" + l.khoa + "]" : (l.choGiay > 0 ? " [chờ " + l.choGiay + " giây]" : ""));
        }
        synchronized (t) {
            t.menuCu = ma;
        }
        try {
            com.htbb.thongbao.ThongBao.rieng(p, tieuDe + " " + moTa);
        } catch (final Exception ignored) {
        }
        p.sendMessage(MessageCreator.createMsgMenu(menu, MENU_CU, 0));
    }

    static void dong(final Char p) {
        final TT t = DS.get(Integer.valueOf(p.charDBID));
        if (t == null || !t.clientMoi || t.nguoi != p) {
            return;
        }
        try {
            final Message m = new Message(LENH);
            m.dos.writeByte(S_DONG);
            p.sendMessage(m);
        } catch (final Exception e) {
            // mat ket noi
        }
    }

    static void bao(final Char p, final String s) {
        try {
            com.htbb.thongbao.ThongBao.rieng(p, s);
        } catch (final Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ lenh mang + menu

    /** Lenh -101 tu client. */
    public static void xuLy(final Char p, final Message message) {
        if (!laNguoi(p)) {
            return;
        }
        int loai = C_XIN, ma = -1;
        try {
            loai = message.dis.readByte();
            if (loai == C_CHON) {
                ma = message.dis.readByte();
            }
        } catch (final Exception e) {
            // goi ngan = xin
        }
        final TT t = tt(p, true);
        synchronized (t) {
            t.clientMoi = true;
        }
        if (!apDung(p)) {
            if (p.hp <= 0 && loai == C_XIN) {
                dong(p); // ban do luat rieng: client dung hop thoai cu
            }
            return;
        }
        if (p.hp > 0) {
            dong(p);
            return;
        }
        if (loai == C_CHON) {
            chon(p, ma);
        } else {
            if (!t.dangGuc) {
                khiGuc(p);
            }
            moHop(p);
        }
    }

    /** MapDoMenu: menu -803 (client cu) / -804 (dong doi cuu). @return true = da xu ly. */
    public static boolean xuLyMenu(final Char p, final int idNpc, final int idMenu, final int idOption) {
        if (idNpc == MENU_CU) {
            final TT t = DS.get(Integer.valueOf(p.charDBID));
            if (t == null || idOption < 0 || idOption >= t.menuCu.length) {
                return true;
            }
            chon(p, t.menuCu[idOption]);
            return true;
        }
        if (idNpc == MENU_DONG_DOI) {
            cuu(p, idOption);
            return true;
        }
        if (idNpc == MENU_MUA_THUOC) {
            muaThuoc(p, idMenu, idOption);
            return true;
        }
        return false;
    }

    /** Nguoi choi (dang guc) chon mot lua chon. Moi dieu kien kiem LAI o may chu. */
    static void chon(final Char p, final int ma) {
        if (!apDung(p) || p.hp > 0) {
            dong(p);
            return;
        }
        final HoiSinh.CauHinh c = ch();
        final TT t = tt(p, true);
        final long bay = System.currentTimeMillis();
        final HoiSinh.TinhHuong th = tinhHuong(p, c, t, bay);
        HoiSinh.LuaChon l = null;
        for (final HoiSinh.LuaChon x : HoiSinh.dsLuaChon(c, th, bay)) {
            if (x.ma == ma) {
                l = x;
            }
        }
        if (l == null) {
            moHop(p);
            return;
        }
        if (!l.bamDuoc()) {
            bao(p, l.khoa != null ? l.khoa : "Chờ " + l.choGiay + " giây nữa.");
            moHop(p);
            return;
        }
        boolean xong = false;
        switch (ma) {
            case HoiSinh.LC_TAN_THU:
                hoiTaiCho(p, c.hpTraPhi, "tan_thu");
                SO_TAN_THU.incrementAndGet();
                xong = true;
                break;
            case HoiSinh.LC_THUOC:
            case HoiSinh.LC_THUOC_TOI_DA:
                xong = dungVatPham(p, ma, "nguoi_choi");
                break;
            case HoiSinh.LC_XU: {
                final long px = l.phi;
                if (p.truXuNeuDu(px, false, "hoi sinh tai cho")) {
                    XU_DOT.addAndGet(px); // xu_ra: truXuNeuDu -> subXu tu dem
                    try {
                        Database.instance.saveLogThongKeChiTieu("hoi sinh tai cho", 0, 1, px, "xu");
                    } catch (final Exception ignored) {
                    }
                    hoiTaiCho(p, c.hpTraPhi, "xu " + px);
                    SO_XU.incrementAndGet();
                    xong = true;
                } else {
                    bao(p, "Không đủ xu.");
                }
                break;
            }
            case HoiSinh.LC_LUONG_KHOA: {
                final long pl = l.phi;
                if (p.truLuongKhoaNeuDu(pl)) {
                    LUONG_KHOA_DOT.addAndGet(pl);
                    try {
                        Database.instance.saveLogThongKeChiTieu("hoi sinh tai cho", 0, 1, pl, "luong khoa");
                    } catch (final Exception ignored) {
                    }
                    hoiTaiCho(p, c.hpTraPhi, "luong khoa " + pl);
                    SO_LUONG_KHOA.incrementAndGet();
                    xong = true;
                } else {
                    bao(p, "Không đủ lượng khoá.");
                }
                break;
            }
            case HoiSinh.LC_TRUNG_TAM:
                veTrungTam(p);
                return;
            case HoiSinh.LC_DONG_DOI:
                batChoDongDoi(p, c, t);
                return;
            default:
                break;
        }
        if (!xong) {
            moHop(p);
        }
    }

    /** Dung Thuoc hoi sinh (potions 119) / Thuoc hoi sinh toi da (tui) cua chinh {@code p}. */
    static boolean dungVatPham(final Char p, final int ma, final String nguon) {
        final HoiSinh.CauHinh c = ch();
        if (ma == HoiSinh.LC_THUOC) {
            if (!p.truNeuDu(() -> p.potions[HoiSinh.THUOC_HOI_SINH] > 0, () -> p.potions[HoiSinh.THUOC_HOI_SINH]--)) {
                bao(p, "Không có Thuốc hồi sinh.");
                return false;
            }
            tt(p, true).ls.ghiThuoc(System.currentTimeMillis());
            hoiTaiCho(p, c.hpThuoc, "thuoc " + nguon);
            SO_THUOC.incrementAndGet();
            return true;
        }
        final Item it = layVatPham(p);
        if (it == null || !com.htbb.pokemon.ChongVatPham.tieuMot(p, it)) { // [chong-vat-pham] chong: tru 1
            bao(p, "Không có Thuốc hồi sinh tối đa.");
            return false;
        }
        tt(p, true).ls.ghiThuoc(System.currentTimeMillis());
        hoiTaiCho(p, hpVatPham(it), "thuoc toi da " + nguon + " dbid" + it.dbid);
        SO_THUOC_TOI_DA.incrementAndGet();
        return true;
    }

    /** Hoi sinh tai cho voi {@code hpPhanTram}% HP / MP (vat pham / phi da tru). */
    static void hoiTaiCho(final Char p, final int hpPhanTram, final String nguon) {
        final int pc = Math.max(1, Math.min(100, hpPhanTram));
        p.hp = Math.max(1, (int) ((long) p.maxhp * pc / 100L));
        p.mp = Math.max(p.mp, (int) ((long) p.maxmp * pc / 100L));
        p.timedie = 0L;
        p.timeHoiSinh = 0L;
        final TT t = tt(p, true);
        synchronized (t) {
            t.dangGuc = false;
            t.tuDongLam = -1;
            t.choDongDoiDen = 0L;
            t.ls.lanTaiChoCuoi = System.currentTimeMillis();
        }
        huyCuu(p);
        try {
            p.desTroy();
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendToNearPlayer(MessageCreator.createNew_HMP_Message(p, 0));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendPetEff();
        } catch (final Exception ignored) {
        }
        dong(p);
        try {
            Database.instance.saveOrtherLog("tob_other_log", p.charname, nguon + " " + pc + "%", "hs");
        } catch (final Exception ignored) {
        }
        com.htbb.log.Nhat.kiemToan("HOI_SINH", p.charname + " tai cho " + nguon + " lan=" + t.lan + " map=" + p.mapID);
    }

    /** Ve Trung tam Pokemon (cung duong voi lenh 31 "Ve lang" cua client cu). */
    static void veTrungTam(final Char p) {
        try {
            if (p.map != null) {
                p.map.doComeHomeWhenDie(p);
            }
        } catch (final Exception e) {
            System.out.println("HoiSinhGame.veTrungTam: " + e);
        }
    }

    /** Vat pham dung tu tui (VatPhamDungLauDai, hieu ung hoi_sinh) khi dang guc. @return ly do chan, null = duoc. */
    public static String chanDungTuTui(final Char p) {
        try {
            if (!apDung(p) || p.hp > 0) {
                return null;
            }
            final HoiSinh.CauHinh c = ch();
            final TT t = tt(p, true);
            final long bay = System.currentTimeMillis();
            final HoiSinh.TinhHuong th = tinhHuong(p, c, t, bay);
            if (th.camTaiCho != null) {
                return th.camTaiCho;
            }
            if (!th.conLuotThuoc) {
                return "Đã dùng " + c.thuocMoiGio + " vật phẩm hồi sinh trong 60 phút.";
            }
            final int cho = HoiSinh.choTaiCho(c, th, bay);
            return cho > 0 ? "Chờ " + cho + " giây nữa mới hồi sinh tại chỗ được." : null;
        } catch (final Exception e) {
            return null;
        }
    }

    /** Sau khi VatPhamDungLauDai da hoi sinh (dung tu tui): ghi hoi chieu + dong hop thoai. */
    public static void daHoiSinh(final Char p) {
        try {
            if (!apDung(p)) {
                return;
            }
            final TT t = tt(p, true);
            synchronized (t) {
                t.dangGuc = false;
                t.tuDongLam = -1;
                t.choDongDoiDen = 0L;
                t.ls.lanTaiChoCuoi = System.currentTimeMillis();
            }
            t.ls.ghiThuoc(System.currentTimeMillis());
            p.timedie = 0L;
            huyCuu(p);
            SO_THUOC_TOI_DA.incrementAndGet();
            dong(p);
        } catch (final Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ dong doi

    static void batChoDongDoi(final Char p, final HoiSinh.CauHinh c, final TT t) {
        final List<Char> ds = dongDoiGan(p, true);
        if (ds.isEmpty()) {
            bao(p, "Không có đồng đội ở gần mang Thuốc hồi sinh.");
            moHop(p);
            return;
        }
        final long bay = System.currentTimeMillis();
        synchronized (t) {
            t.choDongDoiDen = bay + c.dongDoiGiay * 1000L;
            t.daChoDongDoi = true;
        }
        for (final Char h : ds) {
            final List<String> menu = new ArrayList<String>();
            final List<Integer> ma = new ArrayList<Integer>();
            if (h.potions[HoiSinh.THUOC_HOI_SINH] > 0) {
                menu.add("Dùng Thuốc hồi sinh cứu " + p.charname + " (còn " + h.potions[HoiSinh.THUOC_HOI_SINH] + ")");
                ma.add(Integer.valueOf(HoiSinh.LC_THUOC));
            }
            final int[] vp = demVatPham(h);
            if (vp[0] > 0) {
                menu.add("Dùng Thuốc hồi sinh tối đa cứu " + p.charname + " (còn " + vp[0] + ")");
                ma.add(Integer.valueOf(HoiSinh.LC_THUOC_TOI_DA));
            }
            menu.add("Để sau");
            ma.add(Integer.valueOf(-1));
            final int[] a = new int[ma.size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = ma.get(i).intValue();
            }
            CUU.put(Integer.valueOf(h.charDBID), new Object[]{p, a});
            bao(h, p.charname + " đã gục và đang chờ bạn hồi sinh (" + c.dongDoiGiay + " giây).");
            h.sendMessage(MessageCreator.createMsgMenu(menu.toArray(new String[0]), MENU_DONG_DOI, 0));
        }
        bao(p, "Đã gọi " + ds.size() + " đồng đội. Chờ tối đa " + c.dongDoiGiay + " giây.");
        moHop(p);
    }

    static void huyCuu(final Char guc) {
        for (final java.util.Map.Entry<Integer, Object[]> e : CUU.entrySet()) {
            if (e.getValue()[0] == guc) {
                CUU.remove(e.getKey(), e.getValue());
            }
        }
    }

    /** Dong doi {@code h} chon dong {@code idOption} trong menu -804. */
    static void cuu(final Char h, final int idOption) {
        final Object[] o = CUU.remove(Integer.valueOf(h.charDBID));
        if (o == null) {
            return;
        }
        final Char p = (Char) o[0];
        final int[] ma = (int[]) o[1];
        if (idOption < 0 || idOption >= ma.length || ma[idOption] < 0) {
            return;
        }
        final TT t = DS.get(Integer.valueOf(p.charDBID));
        final long bay = System.currentTimeMillis();
        if (t == null || t.nguoi != p || p.hp > 0 || !t.dangGuc || t.choDongDoiDen <= bay) {
            bao(h, "Không còn cần cứu nữa.");
            return;
        }
        if (h.hp <= 0 || h.mapID != p.mapID || h.inCountry != p.inCountry || h.region != p.region) {
            bao(h, "Phải ở gần đồng đội mới cứu được.");
            return;
        }
        final HoiSinh.CauHinh c = ch();
        int hp;
        if (ma[idOption] == HoiSinh.LC_THUOC) {
            if (!h.truNeuDu(() -> h.potions[HoiSinh.THUOC_HOI_SINH] > 0, () -> h.potions[HoiSinh.THUOC_HOI_SINH]--)) {
                bao(h, "Bạn không còn Thuốc hồi sinh.");
                return;
            }
            hp = c.hpThuoc;
        } else {
            final Item it = layVatPham(h);
            if (it == null || !com.htbb.pokemon.ChongVatPham.tieuMot(h, it)) { // [chong-vat-pham] chong: tru 1
                bao(h, "Bạn không còn Thuốc hồi sinh tối đa.");
                return;
            }
            hp = hpVatPham(it);
        }
        try {
            h.sendMessage(MessageCreator.createCharInventoryMessage(h, 0));
        } catch (final Exception ignored) {
        }
        hoiTaiCho(p, hp, "dong doi " + h.charname);
        SO_DONG_DOI.incrementAndGet();
        bao(p, h.charname + " đã hồi sinh bạn!");
        bao(h, "Đã hồi sinh " + p.charname + ".");
    }

    // ------------------------------------------------------------------ ve Trung tam: vung + phat

    /** {map, x o, y o} hoi sinh o Hoenn (thi tran Hoenn gan nhat co Trung tam), null = khong o Hoenn. */
    static int[] diemHoenn(final Char p) {
        final Map m = p.map;
        if (m == null || HoiSinh.vung(m.mapIDLoadMap) != HoiSinh.VUNG_HOENN) {
            return null;
        }
        return HoiSinh.diemTrungTamHoenn(m.mapIDLoadMap, new HoiSinh.DiDuoc() {
            @Override
            public boolean o(final int map, final int x, final int y) {
                final Map den = RealController.mapList.get(Integer.valueOf(map));
                return den != null && den.canMove(x * 16 + 8, y * 16 + 8);
            }
        });
    }

    /**
     * HOOK Map.doComeHomeWhenDie (nhanh thuong, dang guc): diem hoi sinh khi o vung Hoenn. Truoc day moi lan guc o
     * Hoenn deu bi dua ve lang nha o Kanto. null = giu diem cu (lang nha cua phe = Trung tam Pokemon cua vung cu).
     */
    public static int[] diemVeTrungTam(final Char p) {
        try {
            if (p.hp > 0) {
                return null;
            }
            return diemHoenn(p);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * HOOK cuoi Map.doComeHomeWhenDie (da ve Trung tam, da hoi day HP): phat nhe neu co che bat va KHONG phai tan thu
     * - mat EXP cap hien tai (khong tut cap), mat do ben, Met moi. Khong rot do.
     */
    public static void sauVeTrungTam(final Char p) {
        try {
            if (!laNguoi(p) || !bat()) {
                return;
            }
            final TT t = DS.get(Integer.valueOf(p.charDBID));
            final boolean coGuc = t != null && t.dangGuc;
            if (t != null) {
                synchronized (t) {
                    t.dangGuc = false;
                    t.tuDongLam = -1;
                    t.choDongDoiDen = 0L;
                }
            }
            huyCuu(p);
            dong(p);
            if (!coGuc) {
                return; // khong qua khiGuc (ban do luat rieng) - giu nguyen nhu cu
            }
            SO_TRUNG_TAM.incrementAndGet();
            final HoiSinh.CauHinh c = ch();
            final int cap = p.lvDetail.lv;
            if (HoiSinh.tanThu(c, cap)) {
                bao(p, "Đã về Trung tâm Pokémon. Tân thủ (dưới cấp " + c.tanThuCap + ") không bị phạt.");
                return;
            }
            final long mat = truExp(p, c);
            final boolean doBen = truDoBen(p, c);
            if (c.metPhut > 0 && (c.metExp > 0 || c.metTanCong > 0)) {
                p.hsMetDen = System.currentTimeMillis() + c.metPhut * 60000L;
                try {
                    p.sendMessage(MessageCreator.createMsgTimeCountdown("Mệt mỏi -" + c.metExp + "% EXP, -" + c.metTanCong + "% tấn công: ",
                            c.metPhut * 60, -1, ID_MET_MOI, Map.COUNT_DOWN, -1));
                } catch (final Exception ignored) {
                }
            }
            final StringBuilder b = new StringBuilder("Đã về Trung tâm Pokémon.");
            if (mat > 0L) {
                b.append(" Mất ").append(HoiSinh.so(mat)).append(" EXP.");
            }
            if (doBen) {
                b.append(" Trang bị mất ").append(c.doBen).append("% độ bền.");
            }
            if (p.hsMetDen != 0L) {
                b.append(" Mệt mỏi ").append(c.metPhut).append(" phút (−").append(c.metExp).append("% EXP, −").append(c.metTanCong)
                        .append("% tấn công).");
            }
            bao(p, b.toString());
            com.htbb.log.Nhat.kiemToan("HOI_SINH", p.charname + " ve trung tam map=" + p.mapID + " exp-" + mat + " lan="
                    + (t == null ? 0 : t.lan));
        } catch (final Exception e) {
            System.out.println("HoiSinhGame.sauVeTrungTam: " + e);
        }
    }

    /** Tru EXP trong cap hien tai (KHONG tut cap). @return so EXP da tru. */
    static long truExp(final Char p, final HoiSinh.CauHinh c) {
        final LevelDetail ld = p.lvDetail;
        final int cap = ld.lv;
        if (cap < 1 || cap >= LevelDetail.expMain.length || cap >= LevelDetail.expRequire.length) {
            return 0L;
        }
        final long exp = ld.getExp();
        final long moiGio = LauDai.bat() ? ThongSoLauDai.expMoiGio(LauDai.kepCap(cap)) : 0L;
        final long mat = HoiSinh.expMat(c, cap, exp, LevelDetail.expMain[cap - 1], LevelDetail.expRequire[cap], moiGio);
        if (mat <= 0L) {
            return 0L;
        }
        ld.setExpNew(exp - mat);
        if (ld.lv != cap) {
            ld.setExpNew(exp); // phong ho: khong bao gio tut cap
            return 0L;
        }
        EXP_MAT.addAndGet(mat);
        try {
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
        } catch (final Exception ignored) {
        }
        return mat;
    }

    /** Tru do ben trang bi dang mac. @return co mon nao bi tru. */
    static boolean truDoBen(final Char p, final HoiSinh.CauHinh c) {
        if (c.doBen <= 0) {
            return false;
        }
        boolean co = false;
        try {
            for (final Item it : p.getCharWearing()) {
                if (it == null || it.getTemplate() == null || it.mDurable <= 0 || it.getTemplate().durable <= 0) {
                    continue;
                }
                final int moi = HoiSinh.doBenSau(it.mDurable, it.getTemplate().durable * 10, c.doBen);
                if (moi != it.mDurable) {
                    it.mDurable = (short) moi;
                    it.durable = (short) ((moi + 9) / 10);
                    co = true;
                }
            }
            if (co) {
                p.calculateAttrib();
                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            }
        } catch (final Exception e) {
            // khong lam hong luong ve lang
        }
        return co;
    }

    // ------------------------------------------------------------------ Met moi

    /** HOOK Char.getAttack: -X% tan cong khi dang Met moi. */
    public static int apMetTanCong(final Char p, final int att) {
        if (p.hsMetDen == 0L || System.currentTimeMillis() >= p.hsMetDen) {
            return att;
        }
        return (int) HoiSinh.apGiam(att, ch().metTanCong);
    }

    /** HOOK ExpLauDai.traoHaQuai: -X% EXP ha quai khi dang Met moi. */
    public static int apMetExp(final Char p, final int x) {
        if (p == null || p.hsMetDen == 0L || System.currentTimeMillis() >= p.hsMetDen) {
            return x;
        }
        return (int) HoiSinh.apGiam(x, ch().metExp);
    }

    // ------------------------------------------------------------------ cua hang Thuoc hoi sinh (gia theo nguoi mua)

    /** Dong trong menu Cua hang (So tay) - gia hien = gia tru cho CHINH nguoi xem. null = khong ban. */
    public static String dongCuaHang(final Char p) {
        if (p == null || p.lvDetail == null || !bat()) {
            return null;
        }
        return "Thuốc hồi sinh (Chansey) - " + HoiSinh.so(HoiSinh.giaThuoc(ch(), p.lvDetail.lv)) + " xu/viên";
    }

    /**
     * Hop xac nhan truoc khi mua: moi dong ghi DUNG so xu se tru. Gia chot vao idMenu (= cap luc hien) - neu cap doi
     * truoc khi bam, may chu hien lai hop voi gia moi thay vi tru gia khac voi gia da hien.
     */
    public static void moMuaThuoc(final Char p) {
        if (dongCuaHang(p) == null) {
            return;
        }
        final int cap = p.lvDetail.lv;
        final long gia = HoiSinh.giaThuoc(ch(), cap);
        final String[] menu = new String[SO_MUA.length + 1];
        for (int i = 0; i < SO_MUA.length; i++) {
            menu[i] = "Mua " + SO_MUA[i] + " Thuốc hồi sinh: trả " + HoiSinh.so(gia * SO_MUA[i]) + " xu";
        }
        menu[SO_MUA.length] = "Huỷ";
        bao(p, "Thuốc hồi sinh: " + HoiSinh.so(gia) + " xu/viên (theo cấp " + cap + "). Đang có " + p.potions[HoiSinh.THUOC_HOI_SINH] + ".");
        p.sendMessage(MessageCreator.createMsgMenu(menu, MENU_MUA_THUOC, Math.max(0, Math.min(255, cap))));
    }

    /** Chon trong hop xac nhan: tru dung gia da hien (kiem cap khong doi). */
    static void muaThuoc(final Char p, final int capHien, final int idOption) {
        if (idOption < 0 || idOption >= SO_MUA.length || dongCuaHang(p) == null) {
            return;
        }
        if (p.lvDetail.lv != capHien) {
            bao(p, "Giá đã đổi theo cấp mới, xem lại giá.");
            moMuaThuoc(p);
            return;
        }
        final int n = SO_MUA[idOption];
        if (p.potions[HoiSinh.THUOC_HOI_SINH] + n > GiaoDichThuoc.TOI_DA_MOT_O) {
            bao(p, "Ô thuốc đã gần đủ " + GiaoDichThuoc.TOI_DA_MOT_O + ".");
            return;
        }
        final long tong = HoiSinh.giaThuoc(ch(), capHien) * n;
        final boolean ok = p.truNeuDu(() -> p.getxu() >= tong, () -> {
            p.subXu(tong, true, "mua thuoc hoi sinh");
            p.potions[HoiSinh.THUOC_HOI_SINH] += n;
        });
        if (!ok) {
            bao(p, "Không đủ " + HoiSinh.so(tong) + " xu.");
            return;
        }
        // xu_ra: subXu tu dem
        try {
            Database.instance.saveLogThongKeChiTieu("Thuoc hoi sinh", 0, n, tong, "xu");
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception ignored) {
        }
        bao(p, "Đã mua " + n + " Thuốc hồi sinh, trả " + HoiSinh.so(tong) + " xu.");
    }

    /** "Auto ve Map Up" sau khi guc (bay lai cho luyen) chi khi co che tat hoac cau hinh cho phep. */
    public static boolean choAutoVeMapUp() {
        return !bat() || ch().autoVeMapUp;
    }

    /** Don bo nho: bo nhan vat khong guc / khong hoi sinh tai cho gan day (goi thua tu khiGuc). */
    static void don(final long bay, final HoiSinh.CauHinh c) {
        if (DS.size() < 2000) {
            return;
        }
        DS.entrySet().removeIf(e -> !e.getValue().dangGuc && !e.getValue().ls.conMoi(bay, c.cuaSoPhut * 60000L));
        CUU.entrySet().removeIf(e -> ((Char) e.getValue()[0]).hp > 0);
    }
}
