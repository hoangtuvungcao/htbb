package real;

import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.NhiemVuLauDai;
import com.htbb.pokemon.NhiemVuMau;
import com.htbb.pokemon.SuKienLauDai;
import data.Database;
import data.DatabaseLauDai;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ENGINE nhiem vu tong quat (GD2b; yeu cau o du_lieu/NHIEM_VU_THIET_KE.md muc 5). Quan ly moi
 * nhiem vu KHONG thuoc chuoi chinh (phu, tuyen, huyen thoai, an, Pokedex, ngay, tuan, su kien, thanh
 * tuu, su pham, doi, quay lai...) theo du lieu - them nhiem vu / kieu muc tieu moi chi can dong du lieu
 * va mot cho goi {@link #suKien}.
 *
 *  - Danh muc kieu: moi kieu_muc_tieu la mot "su kien" (chuoi) do diem moc trong game ban vao
 *    {@link #suKien}(p, kieu, thamSo, n); khop theo muc_tieu_id (rong = bat ky). 0.9.7: moi kieu trong
 *    data_nhiem_vu_kieu deu co diem moc - cac kieu he sinh thai / chuoi cu nam o real.DiemMocNhiemVu (bang noi goi
 *    + tham so); cau ca co diem moc DUY NHAT o HeSinhThaiMenu.cauCa. Kieu moi: them mot ham vao DiemMocNhiemVu.
 *  - Gating: cap, ngay_mo, `truoc` (moi ma trong danh sach da xong), an + dieu_kien_mo.
 *  - Toi da 3 nhiem vu phu (khe) cung luc; bang ngay rut 5 mau N-<bac>.* moi ngay (hat giong id + ngay),
 *    thuong toi da 3; bang tuan dung chung tran 2 cho T- / TV- (ca chuoi 1 lan) / TD-; SK-<tuan> chi
 *    trong tuan do; thanh tuu tu theo doi (khong can nhan).
 *  - Hoi thoai 3 giai doan (nguoi noi "Ten: cau", {da} = tien do), lua chon (ghi nhanh + thuong theo
 *    nhanh), cau do (so dap an luc chat, KHONG phat len kenh, dap an khong gui client).
 *  - Thuong: exp, xu, luong khoa, luong (chi thanh tuu lon), vat_pham, thuong_khac (danh_hieu,
 *    item:<id>xN, truyen/trang_tri/diem_bang ghi nhan).
 *  - Tien do: tob_nhiem_vu_lau_dai (trang_thai 0 dang lam, 1 xong cho nhan, 2 da nhan; tuan = chi so
 *    ngay voi bang ngay, tuan voi bang tuan); nhanh chon luu ma "<ma>#nhanh", tien_do = so nhanh.
 * Chi chay khi LauDai.bat().
 */
public final class NhiemVuEngine {

    public static final int MENU_DAU = 10;
    static final int M_GOC = 10;
    static final int M_DANG = 11;
    static final int M_NHAN = 12;
    static final int M_CHON = 13;
    static final int M_BANG = 14;
    public static final int MENU_CUOI = 14;

    static volatile LinkedHashMap<String, NhiemVuMau> MAU = new LinkedHashMap<String, NhiemVuMau>();
    /** Kieu -> cac mau (tra nhanh khi co su kien). */
    static volatile HashMap<String, List<NhiemVuMau>> THEO_KIEU = new HashMap<String, List<NhiemVuMau>>();

    /** charDBID -> ma -> {tien do, trang thai, ky, gio bat dau phut}. */
    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>> TD = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>>();
    /** Danh sach ma vua hien o menu (de chon theo chi so). */
    private static final ConcurrentHashMap<Integer, String[]> MENU = new ConcurrentHashMap<Integer, String[]>();

    private NhiemVuEngine() {
    }

    // ------------------------------------------------------------------ nap

    public static void nap() {
        final LinkedHashMap<String, NhiemVuMau> m = DatabaseLauDai.napMauNhiemVu();
        final HashMap<String, List<NhiemVuMau>> k = new HashMap<String, List<NhiemVuMau>>();
        for (final NhiemVuMau x : m.values()) {
            k.computeIfAbsent(x.kieu, z -> new ArrayList<NhiemVuMau>()).add(x);
        }
        MAU = m;
        THEO_KIEU = k;
        THUONG_CU = DatabaseLauDai.napThuongChuoiCu();
        MA_CHUOI_CU = null;
        System.out.println("[LAU DAI] engine nhiem vu: " + m.size() + " mau, " + k.size() + " kieu muc tieu, "
                + THUONG_CU.size() + " thuong khac chuoi chinh");
    }

    private static LinkedHashMap<String, NhiemVuMau> mauTruocKiem;
    private static HashMap<String, List<NhiemVuMau>> kieuTruocKiem;

    /** Test (khong CSDL): dat mau nhiem vu + bang tien do cua nhan vat {@code idChar}; {@link #xoaKiem} tra lai nhu cu. */
    public static synchronized void datKiem(final java.util.Collection<NhiemVuMau> mau, final int idChar,
            final ConcurrentHashMap<String, long[]> tienDo) {
        if (mauTruocKiem == null) {
            mauTruocKiem = MAU;
            kieuTruocKiem = THEO_KIEU;
        }
        final LinkedHashMap<String, NhiemVuMau> m = new LinkedHashMap<String, NhiemVuMau>();
        final HashMap<String, List<NhiemVuMau>> k = new HashMap<String, List<NhiemVuMau>>();
        for (final NhiemVuMau x : mau) {
            m.put(x.ma, x);
            k.computeIfAbsent(x.kieu, z -> new ArrayList<NhiemVuMau>()).add(x);
        }
        MAU = m;
        THEO_KIEU = k;
        TD.put(Integer.valueOf(idChar), tienDo);
    }

    public static synchronized void xoaKiem(final int idChar) {
        TD.remove(Integer.valueOf(idChar));
        if (mauTruocKiem != null) {
            MAU = mauTruocKiem;
            THEO_KIEU = kieuTruocKiem;
            mauTruocKiem = null;
            kieuTruocKiem = null;
        }
    }

    // ------------------------------------------------------------------ chuoi chinh cu (data_quest_moi) / nam 2

    /** ma chuoi chinh cu -> thuong_khac (phat khi tra nhiem vu o Char, engine cu khong doc cot nay). */
    static volatile HashMap<String, String> THUONG_CU = new HashMap<String, String>();
    /** ma chuoi chinh cu -> chi so trong Map.allMainQuest (dung luc kiem "truoc" cua chuoi nam 2). */
    private static volatile HashMap<String, Integer> MA_CHUOI_CU;

    /** Char (tra nhiem vu chinh cu, sau addGift): phat thuong_khac (item:/thuoc:/danh_hieu:...) cua nhiem vu {@code ma}. */
    public static void khiXongChuoiCu(final Char p, final String ma) {
        if (!LauDai.bat() || p == null || ma == null || ma.isEmpty()) {
            return;
        }
        final String tk = THUONG_CU.get(ma);
        if (tk == null) {
            return;
        }
        thuongKhac(p, tk, "chuoi chinh " + ma);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
    }

    /** Nhiem vu chuoi chinh cu {@code ma} da tra xong chua (theo vi tri chuoi cua nhan vat). */
    /** Nhan vat da xong nhiem vu {@code ma} chua (bang tien do engine hoac chuoi chinh cu data_quest). */
    public static boolean daXongMa(final Char p, final String ma) {
        try {
            final ConcurrentHashMap<String, long[]> t = td(p);
            if (t != null && daXong(t).contains(ma)) {
                return true;
            }
            return xongChuoiCu(p, ma);
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * 0.9.7.1: nhiem vu phu cu (goi -64, data_quest_moi mainsub 1) CUNG MA voi nhiem vu phu engine (P-xx). Da nhan / da
     * xong o So tay, hoac da tra qua NPC (-64) -> khong nhan lai qua NPC (truoc day nhiem vu phu qua -64 lap vo han va
     * lanh thuong hai lan: mot o NPC, mot o So tay).
     */
    public static boolean phuCuDaDung(final Char p, final String ma) {
        if (!LauDai.bat() || p == null || ma == null || ma.isEmpty() || p.charDBID <= 0) {
            return false;
        }
        try {
            final ConcurrentHashMap<String, long[]> t = td(p);
            return t != null && t.containsKey(ma);
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Bao loi 05/11/2026 (Truong lang Xatu moi cung luc buoc 1..4 cua chuoi "Ca sĩ mất giọng": "Giọng hát khàn" ... "Buổi
     * diễn"): danh sach NPC (-64, NhiemVuLauDaiGame.locDanhSach) chi loc cap + "da dung", khong kiem cot {@code truoc} cua
     * chuoi P-xx (So tay da kiem qua NhiemVuMau.kiemNhan). Nay: chi moi buoc ke tiep - moi ma trong {@code truoc} da xong
     * (tien do engine v[1] &gt;= 2: tra o So tay hoac qua NPC ghiXongPhuCu; hoac chuoi chinh cu da tra).
     * Nhiem vu khong co mau engine / {@code truoc} rong / tat sv.lau_dai / nhan vat khong co tien do (charDBID &lt;= 0): khong
     * rang buoc (nhu cu).
     */
    public static boolean phuDuBuocTruoc(final Char p, final String ma, final Set<String> daXong) {
        final String truoc = truocPhu(ma);
        if (truoc == null || p == null || p.charDBID <= 0) {
            return true;
        }
        for (final String x : truoc.split("[,;]")) {
            final String t = x.trim();
            if (!t.isEmpty() && !(daXong != null && daXong.contains(t)) && !xongChuoiCu(p, t)) {
                return false;
            }
        }
        return true;
    }

    public static boolean phuDuBuocTruoc(final Char p, final String ma) {
        return phuDuBuocTruoc(p, ma, maDaXong(p));
    }

    /** Ma da xong (tien do engine v[1] &gt;= 2) cua nhan vat - tinh MOT lan cho ca danh sach; rong neu khong co. */
    public static Set<String> maDaXong(final Char p) {
        if (!LauDai.bat() || p == null || p.charDBID <= 0) {
            return new HashSet<String>();
        }
        try {
            final ConcurrentHashMap<String, long[]> t = td(p);
            return t == null ? new HashSet<String>() : daXong(t);
        } catch (final Exception e) {
            return new HashSet<String>();
        }
    }

    /** Cot {@code truoc} cua mau engine {@code ma}; null neu khong co / rong / tat sv.lau_dai. */
    static String truocPhu(final String ma) {
        if (!LauDai.bat() || ma == null || ma.isEmpty()) {
            return null;
        }
        final NhiemVuMau m = MAU.get(ma);
        if (m == null || m.truoc == null || m.truoc.trim().isEmpty()) {
            return null;
        }
        return m.truoc;
    }

    /** Ten buoc truoc chua xong dau tien cua {@code ma} (cho cau tu choi); null neu du. */
    public static String tenBuocTruocThieu(final Char p, final String ma) {
        final String truoc = truocPhu(ma);
        if (truoc == null || p == null || p.charDBID <= 0) {
            return null;
        }
        final Set<String> xong = maDaXong(p);
        for (final String x : truoc.split("[,;]")) {
            final String t = x.trim();
            if (!t.isEmpty() && !xong.contains(t) && !xongChuoiCu(p, t)) {
                final NhiemVuMau m = MAU.get(t);
                return m == null || m.ten == null || m.ten.isEmpty() ? t : m.ten;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ phu nhan o So tay hien o NPC (-64)

    /**
     * Chu game 31/10/2026 (bot cb2): buoc phu P-* nhan qua So tay -> Nhan nhiem vu phu nam o tien do engine nen KHONG hien
     * trong danh sach NPC "Đang làm" / "Hoàn thành" (-64) - nguoi choi tuong mat nhiem vu. Nay mot nguon: tien do engine,
     * danh sach NPC hien them dong cung nhiem vu (mau NPC cu cung ma), tra o NPC = nhan thuong engine (traThuong).
     */
    static data.InfoQuestTemplate mauNpc(final String ma) {
        if (ma == null || ma.isEmpty() || Map.allSubQuest == null) {
            return null;
        }
        for (final data.InfoQuestTemplate t : Map.allSubQuest.values()) {
            if (t != null && ma.equals(t.ma)) {
                return t;
            }
        }
        return null;
    }

    /** Buoc phu (loai phu) nhan o So tay chua nhan thuong; khong tinh buoc dang giu o khe NPC cu (p.subQuest). */
    static boolean laPhuSoTay(final Char p, final NhiemVuMau m, final long[] v) {
        if (m == null || v == null || v[1] >= 2 || !"phu".equals(m.loai)) {
            return false;
        }
        try {
            if (p.subQuest != null && p.subQuest.id_quest >= 0 && p.subQuest.getTemplate() != null && m.ma.equals(p.subQuest.getTemplate().ma)) {
                return false;
            }
        } catch (final Exception ignored) {
        }
        return true;
    }

    /**
     * Dong hien them trong danh sach NPC -64: {@code type} 2 = dang lam (engine v[1] == 0), 1 = xong cho tra (v[1] == 1). Tien
     * do engine (mot so dem) dat vao o dau cua mau NPC. Client (J2ME GameScr.onQuest / Unity) chi co MOT o nhiem vu phu
     * (subQuest): chi them khi o NPC cu trong, va chi MOT buoc (uu tien buoc xong cho tra, roi theo ma) - cac buoc So tay
     * con lai xem o So tay -> Nhiem vu cua toi -> Dang lam.
     */
    public static List<NewCharQuest> phuSoTayChoNpc(final Char p, final int type) {
        final List<NewCharQuest> ds = new ArrayList<NewCharQuest>();
        if (!LauDai.bat() || p == null || p.charDBID <= 0 || (type != 1 && type != 2)) {
            return ds;
        }
        if (p.subQuest != null && p.subQuest.id_quest >= 0) {
            return ds; // o phu NPC dang dung
        }
        try {
            final ConcurrentHashMap<String, long[]> t = td(p);
            if (t == null) {
                return ds;
            }
            String chon = null;
            long[] vChon = null;
            for (final Entry<String, long[]> e : t.entrySet()) {
                final NhiemVuMau m = MAU.get(e.getKey());
                final long[] v = e.getValue();
                if (!laPhuSoTay(p, m, v) || mauNpc(m.ma) == null) {
                    continue;
                }
                if (chon == null || v[1] > vChon[1] || v[1] == vChon[1] && m.ma.compareTo(chon) < 0) {
                    chon = m.ma;
                    vChon = v;
                }
            }
            for (final Entry<String, long[]> e : t.entrySet()) {
                final NhiemVuMau m = MAU.get(e.getKey());
                final long[] v = e.getValue();
                if (m == null || !m.ma.equals(chon) || (type == 2 ? v[1] != 0 : v[1] != 1)) {
                    continue;
                }
                final data.InfoQuestTemplate info = mauNpc(m.ma);
                if (info == null) {
                    continue;
                }
                final NewCharQuest q = new NewCharQuest(info.id, (byte) 1);
                q.state_quest = type == 2 ? NewCharQuest.WORKING : NewCharQuest.FINISH;
                final short da = (short) Math.min(Short.MAX_VALUE, Math.max(0L, v[0]));
                if (q.monsterKilled != null && q.monsterKilled.length > 0) {
                    q.monsterKilled[0] = da;
                }
                if (q.nItem != null && q.nItem.length > 0) {
                    q.nItem[0] = da;
                }
                ds.add(q);
            }
        } catch (final Exception e) {
            System.out.println("NhiemVuEngine.phuSoTayChoNpc: " + e);
        }
        return ds;
    }

    /** Ma buoc phu nhan o So tay ung voi id nhiem vu NPC {@code questID}; null = khong phai. */
    public static String maPhuSoTay(final Char p, final int questID) {
        if (!LauDai.bat() || p == null || p.charDBID <= 0 || Map.allSubQuest == null) {
            return null;
        }
        try {
            final data.InfoQuestTemplate info = Map.allSubQuest.get(Short.valueOf((short) questID));
            final ConcurrentHashMap<String, long[]> t = td(p);
            if (info == null || t == null || info.ma == null) {
                return null;
            }
            final NhiemVuMau m = MAU.get(info.ma);
            return laPhuSoTay(p, m, t.get(info.ma)) ? info.ma : null;
        } catch (final Exception e) {
            return null;
        }
    }

    /** NPC -64 "tra" mot buoc phu nhan o So tay: xong -> nhan thuong engine (mot lan); chua xong -> chu dang lam. */
    public static void traPhuSoTayQuaNpc(final Char p, final String ma) {
        final NhiemVuMau m = MAU.get(ma);
        final ConcurrentHashMap<String, long[]> t = td(p);
        final long[] v = t == null ? null : t.get(ma);
        if (m == null || v == null) {
            return;
        }
        if (v[1] == 1) {
            SoTayLauDai.bao(p, traThuong(p, m, v));
        } else {
            SoTayLauDai.bao(p, chuDangLam(m, v[0]));
        }
    }

    /** 0.9.7.1: tra nhiem vu phu cu (-64) xong -> ghi tien do engine "da nhan thuong" (khong lam lai o ca hai noi). */
    public static void ghiXongPhuCu(final Char p, final String ma) {
        if (!LauDai.bat() || p == null || ma == null || ma.isEmpty() || p.charDBID <= 0) {
            return;
        }
        try {
            final ConcurrentHashMap<String, long[]> t = td(p);
            if (t == null) {
                return;
            }
            final NhiemVuMau m = MAU.get(ma);
            final long[] v = new long[]{m == null ? 1L : m.soLuong, 2L, 0L, com.htbb.pokemon.DongHo.bay()};
            t.put(ma, v);
            luu(p, ma, v);
        } catch (final Exception e) {
            System.out.println("NhiemVuEngine.ghiXongPhuCu: " + e);
        }
    }

    /**
     * sua-tuyen: tra nhiem vu phu cu qua NPC (-64) -> thuong_khac cua mau engine cung ma (danh hieu, item:, thuoc:) nhu
     * nhan o So tay. Truoc day duong -64 chi phat exp / xu / vat pham (addGift) - mat danh hieu ma chu thuong hua
     * (ghiXongPhuCu khoa ca duong So tay nen khong bao gio nhan lai duoc).
     */
    public static void khiXongPhuCu(final Char p, final String ma) {
        final String tk = thuongKhacCu(1, ma);
        if (p == null || tk == null || tk.isEmpty()) {
            return;
        }
        thuongKhac(p, tk, "phu cu " + ma);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
    }

    /** thuong_khac phat khi tra qua -64: chuoi chinh (THUONG_CU), phu (mau engine); null = khong. */
    public static String thuongKhacCu(final int mainSub, final String ma) {
        if (!LauDai.bat() || ma == null || ma.isEmpty()) {
            return null;
        }
        if (mainSub == 0) {
            return THUONG_CU.get(ma);
        }
        if (mainSub == 1) {
            final NhiemVuMau m = MAU.get(ma);
            return m == null ? null : m.thuongKhac;
        }
        return null;
    }

    static boolean xongChuoiCu(final Char p, final String ma) {
        HashMap<String, Integer> m = MA_CHUOI_CU;
        if (m == null) {
            m = new HashMap<String, Integer>();
            for (final java.util.Map.Entry<Short, data.InfoQuestTemplate> e : Map.allMainQuest.entrySet()) {
                if (e.getValue() != null && e.getValue().ma != null && !e.getValue().ma.isEmpty()) {
                    m.put(e.getValue().ma, Integer.valueOf(e.getKey().intValue()));
                }
            }
            MA_CHUOI_CU = m;
        }
        final Integer k = m.get(ma);
        if (k == null) {
            return false;
        }
        return p.idMainQuest > k.intValue() || p.idMainQuest == k.intValue() && p.mainQuest != null
                && p.mainQuest.state_quest == NewCharQuest.DONE;
    }

    /** daXong + cac nhiem vu chuoi cu da xong ma chuoi nam 2 tham chieu trong "truoc". */
    static Set<String> daXong(final Char p, final ConcurrentHashMap<String, long[]> t) {
        final Set<String> s = daXong(t);
        for (final NhiemVuMau m : MAU.values()) {
            if (m.chuoiNam2 && m.truoc != null) {
                for (final String x : m.truoc.split("[,;]")) {
                    final String ma = x.trim();
                    if (!ma.isEmpty() && !MAU.containsKey(ma) && !s.contains(ma) && xongChuoiCu(p, ma)) {
                        s.add(ma);
                    }
                }
            }
        }
        return s;
    }

    /** null = nhiem vu chuoi nam 2 chay duoc (co ban do + tran cap mua); khac -> ly do. */
    static String kiemNam2(final NhiemVuMau m) {
        if (!m.chuoiNam2) {
            return null;
        }
        final int tran = com.htbb.pokemon.CapDoLauDai.tranCap(LauDai.ngayHienTai(), LevelDetail.expMain.length);
        return m.kiemNam2(tran, m.mapId >= 0 && RealController.mapList.get(Integer.valueOf(m.mapId)) != null);
    }

    static ConcurrentHashMap<String, long[]> td(final Char p) {
        final Integer key = Integer.valueOf(p.charDBID);
        ConcurrentHashMap<String, long[]> t = TD.get(key);
        if (t == null) {
            final ConcurrentHashMap<String, long[]> moi = new ConcurrentHashMap<String, long[]>();
            final List<Object[]> ds = DatabaseLauDai.napNhiemVu(p.charDBID);
            if (ds == null) {
                return null;
            }
            for (final Object[] d : ds) {
                moi.put((String) d[0], new long[]{((Integer) d[1]).longValue(), ((Integer) d[2]).longValue(), ((Integer) d[3]).longValue(), 0L});
            }
            t = TD.putIfAbsent(key, moi);
            if (t == null) {
                t = moi;
            }
        }
        return t;
    }

    /**
     * Roi game (CharManager): bo bo nho dem tien do (moi thay doi da ghi CSDL ngay qua {@link #luu}). Truoc dong bo 0.9.8
     * TD giu mai toi khi khoi dong lai: ro bo nho theo so nhan vat tung online, va CSDL sua trong luc offline (trang quan tri,
     * bot kiem) khong duoc nap lai khi vao game.
     */
    public static void khiThoat(final Char p) {
        if (p != null && p.charDBID > 0) {
            TD.remove(Integer.valueOf(p.charDBID));
            MENU.remove(Integer.valueOf(p.charDBID));
            HanhDongNhiemVuGame.khiThoat(p);
        }
    }

    static void luu(final Char p, final String ma, final long[] v) {
        DatabaseLauDai.luuNhiemVu(p.charDBID, ma, (int) v[0], (int) v[1], (int) v[2]);
        final NhiemVuMau m = MAU.get(ma);
        if (m != null && m.laCuaAi()) {
            MucTieuNhiemVuGame.khiDoiCuaAi(p, m); // sua-tien-do-nv: cua ai hien o khe doi so Nhiem vu
        }
    }

    static int homNay() {
        return NangLuong.ngay(com.htbb.pokemon.DongHo.bay());
    }

    static int tuanNay() {
        return NhiemVuLauDai.tuan(com.htbb.pokemon.DongHo.bay());
    }

    static Set<String> daXong(final ConcurrentHashMap<String, long[]> t) {
        final Set<String> s = new HashSet<String>();
        for (final Entry<String, long[]> e : t.entrySet()) {
            if (e.getValue()[1] >= 2) {
                s.add(e.getKey());
            }
        }
        return s;
    }

    // ------------------------------------------------------------------ su kien (diem moc)

    /** Mot su kien {@code kieu} (tham so {@code thamSo}, so lan {@code n}) cua nhan vat. */
    public static void suKien(final Char p, final String kieu, final String thamSo, final int n) {
        DanhHieuGame.suKien(p, kieu, thamSo, n); // danh hieu (bo dem rieng, chay ca khi khong co nhiem vu kieu nay)
        HuongDanTanThuGame.suKien(p, kieu, thamSo, n); // buoc huong dan tan thu (chuoi chinh HD.xx) + moi khi vao lang
        HanhDongNhiemVuGame.suKien(p, kieu, thamSo, n); // sua-buoc-hanh-dong: buoc noi chuyen -64 co muc tieu that
        if (!LauDai.bat() || p == null || p.charDBID <= 0 || n <= 0) {
            return;
        }
        final List<NhiemVuMau> ds = THEO_KIEU.get(kieu);
        if (ds == null || ds.isEmpty()) {
            return;
        }
        try {
            final ConcurrentHashMap<String, long[]> t = td(p);
            if (t == null) {
                return;
            }
            for (final NhiemVuMau m : ds) {
                long[] v = t.get(m.ma);
                if (v == null && m.laThanhTuu()) {
                    // thanh tuu tu theo doi, khong can nhan
                    v = new long[]{0L, 0L, 0L, 0L};
                    t.put(m.ma, v);
                }
                if (v == null || v[1] != 0 || !m.khop(kieu, thamSo) || !dieuKienSuKien(p, m)) {
                    continue;
                }
                if (m.phut > 0 && v[3] > 0 && com.htbb.pokemon.DongHo.bay() - v[3] > m.phut * 60000L) {
                    v[0] = 0; // thu thach gio: het gio thi dem lai tu dau
                    v[3] = com.htbb.pokemon.DongHo.bay();
                }
                v[0] = Math.min(m.soLuong, v[0] + n);
                if (v[0] >= m.soLuong) {
                    v[1] = 1;
                    LauDaiNhanVat.bao(p, "Hoàn thành nhiệm vụ: " + m.ten + ". Nhận thưởng: " + com.htbb.pokemon.DuongMenu.NHAN_THUONG_MOI_NOI);
                }
                luu(p, m.ma, v);
            }
        } catch (final Exception e) {
            System.out.println("NhiemVuEngine.suKien " + kieu + ": " + e);
        }
    }

    /**
     * Kieu "dat muc" (cap, +k nang cap, tang Ham nguc...): tien do = max(tien do, gia tri) thay vi cong.
     */
    public static void datMuc(final Char p, final String kieu, final int giaTri) {
        DanhHieuGame.datMuc(p, kieu, giaTri);
        if (!LauDai.bat() || p == null || p.charDBID <= 0) {
            return;
        }
        final List<NhiemVuMau> ds = THEO_KIEU.get(kieu);
        if (ds == null) {
            return;
        }
        final ConcurrentHashMap<String, long[]> t = td(p);
        if (t == null) {
            return;
        }
        for (final NhiemVuMau m : ds) {
            long[] v = t.get(m.ma);
            if (v == null && m.laThanhTuu()) {
                v = new long[]{0L, 0L, 0L, 0L};
                t.put(m.ma, v);
            }
            if (v == null || v[1] != 0 || giaTri <= v[0]) {
                continue;
            }
            v[0] = Math.min(m.soLuong, giaTri);
            if (v[0] >= m.soLuong) {
                v[1] = 1;
                LauDaiNhanVat.bao(p, "Đã đạt: " + m.ten + ". Nhận thưởng: " + com.htbb.pokemon.KhoaBanDo.DUONG + com.htbb.pokemon.DuongMenu.DANG_LAM + ".");
            }
            luu(p, m.ma, v);
        }
    }

    /** Dieu kien them cua mau luc co su kien (quan sat: dem / gio; to doi toi thieu...). */
    static boolean dieuKienSuKien(final Char p, final NhiemVuMau m) {
        final int chiMap = m.banDoChi();
        if (chiMap >= 0 && (p.map == null || p.map.mapIDLoadMap != chiMap)) {
            return false; // buoc cua ai: chi tinh quai ha o dung ban do
        }
        final String qs = m.dieuKien.get("dieu_kien_quan_sat");
        if (qs != null) {
            final int gio = com.htbb.pokemon.DongHo.lich().get(java.util.Calendar.HOUR_OF_DAY);
            if (qs.equals("dem") && !(gio >= 19 || gio < 6)) {
                return false;
            }
            if (qs.startsWith("gio=")) {
                try {
                    final String[] ab = qs.substring(4).split("-");
                    if (gio < Integer.parseInt(ab[0].trim()) || gio > Integer.parseInt(ab[1].trim())) {
                        return false;
                    }
                } catch (final RuntimeException ignored) {
                }
            }
            if (qs.startsWith("su_kien=") && !SuKien.bat("moi:" + qs.substring(8))) {
                return false;
            }
            // thoi_tiet=...: he sinh thai quyet (khong co he sinh thai thi coi nhu dat)
        }
        final String toiThieu = m.dieuKien.get("to_doi_toi_thieu");
        if (toiThieu != null) {
            try {
                if (ExpLauDai.thanhVienGan(p).size() < Integer.parseInt(toiThieu)) {
                    return false;
                }
            } catch (final NumberFormatException ignored) {
            }
        }
        return true;
    }

    /** Ha quai (RoiLauDai): moi kieu lien quan den ha quai, ca to doi. */
    static void khiHaQuai(final Char p, final Monster mt) {
        if (!LauDai.bat()) {
            return;
        }
        HanhDongNhiemVuGame.khiHaQuai(p, mt); // sua-buoc-hanh-dong: quan sat theo gio / ha quai tinh gio cua buoc -64
        final String id = String.valueOf(mt.idTemplate);
        final int delta = Math.abs(p.lvDetail.lv - mt.level);
        suKien(p, "giet", id, 1);
        if (delta <= 5 || ChenhCapNhiemVuGame.tinh(p, mt.level, 5)) { // + bai luyen cao nhat da mo (ChenhCapNhiemVu)
            suKien(p, "giet_lech_cap", "", 1);
        }
        if (mt.level > p.lvDetail.lv) {
            suKien(p, "giet_tinh_anh", id, 1);
        }
        if (NoiDungLauDai.laAlpha(mt)) {
            suKien(p, "ha_alpha", id, 1);
        }
        suKien(p, "thu_thap", id, 1); // thu thap: vat roi tu quai muc tieu (quai_roi) - dem theo lan ha
        suKien(p, "thu_thach_gio", id, 1);
        final int dex = PokedexPokemon.dexQuai(p, mt);
        if (dex > 0) {
            suKien(p, "pokedex_ha", String.valueOf(dex), 1);
            suKien(p, "quan_sat", String.valueOf(dex), 1);
        }
        suKien(p, "thanh_tuu_quai", "", 1);
        final List<Char> doi = ExpLauDai.thanhVienGan(p);
        if (doi.size() > 1) {
            boolean coTanThu = false;
            for (final Char c : doi) {
                coTanThu |= c.lvDetail.lv < 20 && c != p;
            }
            for (final Char c : doi) {
                suKien(c, "to_doi", id, 1);
                if (coTanThu && c.lvDetail.lv >= 20) {
                    suKien(c, "giup_tan_thu", "", 1);
                }
            }
        }
    }

    /** Di chuyen (CharCapNhat.doPlayerMove): dung tai diem / den noi / tim vat an. */
    public static void khiDiChuyen(final Char p) {
        if (!LauDai.bat() || p == null || p.charDBID <= 0) {
            return;
        }
        final ConcurrentHashMap<String, long[]> t = TD.get(Integer.valueOf(p.charDBID));
        if (t == null) {
            return;
        }
        final int oX = p.x / 16;
        final int oY = p.y / 16;
        final int banDo = NpcTinhBanDo.banDoNap(p); // ban do nap (ban sao lang 301 = 0, trong nha = id nha)
        for (final Entry<String, long[]> e : t.entrySet()) {
            if (e.getValue()[1] != 0) {
                continue;
            }
            final NhiemVuMau m = MAU.get(e.getKey());
            if (m == null) {
                continue;
            }
            if (!("dung_toa_do".equals(m.kieu) || "tim_vat_an".equals(m.kieu) || "den_noi".equals(m.kieu))) {
                continue;
            }
            // sua-tuyen: vung dich chung voi chuoi -64 (data_nhiem_vu_khu_vuc, o di duoc da kiem) neu co, khong thi toa do cu
            final java.util.List<com.htbb.pokemon.KhuVucNhiemVu.Vung> kv = KhuVucNhiemVuGame.KHU.get(m.ma);
            if (kv != null ? com.htbb.pokemon.KhuVucNhiemVu.trong(kv, banDo, oX, oY) : m.trongToaDo(banDo, oX, oY)) {
                suKien(p, m.kieu, m.mucTieuId, 1);
            }
        }
    }

    /** Chat (truoc khi phat kenh): dung dap an cau do dang nhan -> hoan thanh, KHONG phat. */
    public static boolean khiChat(final Char p, final String chat) {
        if (!LauDai.bat() || p == null || chat == null) {
            return false;
        }
        final ConcurrentHashMap<String, long[]> t = TD.get(Integer.valueOf(p.charDBID));
        if (t == null) {
            return false;
        }
        for (final Entry<String, long[]> e : t.entrySet()) {
            if (e.getValue()[1] != 0) {
                continue;
            }
            final NhiemVuMau m = MAU.get(e.getKey());
            if (m != null && "cau_do".equals(m.kieu) && m.dungDapAn(chat)) {
                final long[] v = e.getValue();
                v[0] = m.soLuong;
                v[1] = 1;
                luu(p, m.ma, v);
                LauDaiNhanVat.bao(p, "Chính xác! Hoàn thành nhiệm vụ: " + m.ten + ".");
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ cua ai (khoa ban do)

    /** Buoc ke tiep cua mot cua ai: mau, thu tu 1..tong, tien do (null = chua nhan), ly do chua nhan duoc (null = duoc). */
    public static final class BuocCuaAi {
        public final NhiemVuMau m;
        public final int so;
        public final int tong;
        public final long[] v;
        public final String loiNhan;

        BuocCuaAi(final NhiemVuMau m, final int so, final int tong, final long[] v, final String loiNhan) {
            this.m = m;
            this.so = so;
            this.tong = tong;
            this.v = v;
            this.loiNhan = loiNhan;
        }

        public int trangThai() {
            return v == null ? com.htbb.pokemon.KhoaBanDo.CHUA_NHAN : v[1] >= 1 ? com.htbb.pokemon.KhoaBanDo.CHO_THUONG
                    : com.htbb.pokemon.KhoaBanDo.DANG_LAM;
        }
    }

    static int soBuoc(final String ma) {
        try {
            return Integer.parseInt(ma.substring(ma.lastIndexOf('.') + 1));
        } catch (final RuntimeException e) {
            return 0;
        }
    }

    /**
     * Buoc cua ai can lam tiep de xong {@code maCuoi} (ma_nhiem_vu cua data_ban_do_mo_khoa, vd CA-01.2 -> cac buoc
     * CA-01.1, CA-01.2): buoc dau tien chua nhan thuong. null = khong co mau / da xong / chua nap tien do.
     */
    public static BuocCuaAi buocCuaAi(final Char p, final String maCuoi) {
        if (p == null || maCuoi == null || maCuoi.isEmpty()) {
            return null;
        }
        final ConcurrentHashMap<String, long[]> t = td(p);
        if (t == null) {
            return null;
        }
        final int cham = maCuoi.lastIndexOf('.');
        final String tienTo = cham > 0 ? maCuoi.substring(0, cham + 1) : maCuoi;
        final int cuoi = cham > 0 ? soBuoc(maCuoi) : 0;
        final List<NhiemVuMau> buoc = new ArrayList<NhiemVuMau>();
        for (final NhiemVuMau m : MAU.values()) {
            if (m.laCuaAi() && (cham > 0 ? m.ma.startsWith(tienTo) && soBuoc(m.ma) <= cuoi : m.ma.equals(maCuoi))) {
                buoc.add(m);
            }
        }
        java.util.Collections.sort(buoc, new java.util.Comparator<NhiemVuMau>() {
            @Override
            public int compare(final NhiemVuMau a, final NhiemVuMau b) {
                return soBuoc(a.ma) - soBuoc(b.ma);
            }
        });
        for (int i = 0; i < buoc.size(); i++) {
            final NhiemVuMau m = buoc.get(i);
            final long[] v = t.get(m.ma);
            if (v != null && v[1] >= 2) {
                continue;
            }
            String loi = null;
            if (v == null) {
                final int gio = com.htbb.pokemon.DongHo.lich().get(java.util.Calendar.HOUR_OF_DAY);
                loi = m.kiemNhan(p.lvDetail.lv, LauDai.ngayMoNoiDung(), daXong(p, t), NhiemVuMau.moAn(m.dieuKienMo, gio, null));
            }
            return new BuocCuaAi(m, i + 1, buoc.size(), v, loi);
        }
        return null;
    }

    /**
     * Hop "Bản đồ khoá" (KhoaBanDoGame.POPUP_CUA_AI) bam Đồng ý: buoc chua nhan -> nhan (DUNG duong So tay -> Thử thách
     * cửa ải: nhan() kiem cap / buoc truoc / da nhan); da nhan -> nhu chon o "Đang làm" (xem tien do / nhan thuong).
     */
    public static void khiDongYCuaAi(final Char p, final String maCuoi) {
        if (!LauDai.bat() || p == null) {
            return;
        }
        final BuocCuaAi b = buocCuaAi(p, maCuoi);
        if (b == null) {
            SoTayLauDai.bao(p, "Cửa ải này đã xong.");
            return;
        }
        if (b.v == null) {
            baoNhan(p, b.m.ma);
        } else {
            chonDangLam(p, b.m.ma);
        }
    }

    /** Dua hang cua ai vua can (ban do vua bi chan, KhoaBanDoGame.buocVuaChan) len dau, danh dau "»". */
    static void toDam(final Char p, final List<String> ma, final List<String> nhan) {
        final String can = KhoaBanDoGame.buocVuaChan(p);
        if (can == null) {
            return;
        }
        final int i = ma.indexOf(can);
        if (i < 0) {
            return;
        }
        final String n = "» " + nhan.remove(i) + KhoaBanDoGame.duoiVuaChan(p);
        ma.remove(i);
        ma.add(0, can);
        nhan.add(0, n);
    }

    /** Nhan {@code ma} va bao; buoc ha Gym vua nhan -> hop "Thách đấu … ngay?" kem loi nhan (sua-duong-gym). */
    static void baoNhan(final Char p, final String ma) {
        final ConcurrentHashMap<String, long[]> t0 = td(p);
        final boolean co = t0 != null && t0.containsKey(ma);
        final String s = nhan(p, ma);
        final ConcurrentHashMap<String, long[]> t = td(p);
        final NhiemVuMau m = MAU.get(ma);
        final long[] v = t == null ? null : t.get(ma);
        // vua nhan: hop ngan (loi giao + muc tieu day du da co o "Đang làm"); da co tu truoc: kem cau bao
        final String truoc = !co && v != null && m != null ? "Đã nhận: " + m.ten + "." : s;
        if (!GymLauDai.hoiNeuGym(p, m, v, truoc)) {
            SoTayLauDai.bao(p, s);
        }
    }

    /** sua-duong-gym: buoc "ha_gym" da nhan chua xong dau tien theo ma (cua ai CA-xx.y truoc); null = khong co. */
    static NhiemVuMau buocGymDangLam(final Char p) {
        final ConcurrentHashMap<String, long[]> t = p == null ? null : td(p);
        if (t == null) {
            return null;
        }
        NhiemVuMau tot = null;
        for (final Entry<String, long[]> e : t.entrySet()) {
            final NhiemVuMau m = MAU.get(e.getKey());
            if (m != null && "ha_gym".equals(m.kieu) && e.getValue()[1] == 0 && (tot == null || m.ma.compareTo(tot.ma) < 0)) {
                tot = m;
            }
        }
        return tot;
    }

    // ------------------------------------------------------------------ menu

    /**
     * Muc menu "Nhiệm vụ của tôi" (chi so = idOption) va nhom cua tung muc (dsCoTheNhan: 1 phu, 2 ngay, 3 tuan, 4 thanh
     * tuu, 5 su kien, 6 nam 2, 7 cua ai; 0 = Đang làm). sua-duong-gym: xep theo muc dung - cua ai (mo ban do) ngay sau
     * Đang làm, trang dau menu J2ME 3 dong.
     */
    public static final String[] MUC_GOC = {com.htbb.pokemon.DuongMenu.DANG_LAM, com.htbb.pokemon.DuongMenu.CUA_AI,
        "Nhiệm vụ hằng ngày", "Nhiệm vụ hằng tuần", "Nhận nhiệm vụ phụ", "Sự kiện tuần này", "Thành tựu", "Cốt truyện năm 2"};
    static final int[] NHOM_GOC = {0, 7, 2, 3, 1, 5, 4, 6};

    static void moMenu(final Char p) {
        if (!LauDai.bat()) {
            return;
        }
        final ConcurrentHashMap<String, long[]> t = td(p);
        int dang = 0;
        if (t != null) {
            for (final Entry<String, long[]> e : t.entrySet()) {
                final NhiemVuMau m = MAU.get(e.getKey());
                if (m != null && hienDangLam(m.laThanhTuu(), e.getValue()[1])) {
                    dang++;
                }
            }
        }
        final String[] muc = MUC_GOC.clone();
        muc[0] = muc[0] + " (" + dang + ")";
        p.sendMessage(MessageCreator.createMsgMenu(muc, SoTayLauDai.NPC_ID, M_GOC));
    }

    static void doMenu(final Char p, final int idMenu, final int op) {
        try {
            if (idMenu == M_GOC) {
                if (op < 0 || op >= NHOM_GOC.length) {
                    return;
                }
                TRANG_PHU.remove(Integer.valueOf(p.charDBID)); // mo lai tu menu goc: trang 1
                if (NHOM_GOC[op] == 0) {
                    dsDangLam(p);
                } else {
                    dsCoTheNhan(p, NHOM_GOC[op]);
                }
                return;
            }
            final String[] ds = MENU.get(Integer.valueOf(p.charDBID));
            if (ds == null || op < 0 || op >= ds.length) {
                return;
            }
            if (idMenu == M_DANG) {
                chonDangLam(p, ds[op]);
            } else if (idMenu == M_NHAN && XEM_THEM.equals(ds[op])) {
                final Integer tr = TRANG_PHU.get(Integer.valueOf(p.charDBID));
                TRANG_PHU.put(Integer.valueOf(p.charDBID), Integer.valueOf(tr == null ? 1 : tr.intValue() + 1));
                dsCoTheNhan(p, 1);
            } else if (idMenu == M_NHAN) {
                baoNhan(p, ds[op]);
            } else if (idMenu == M_CHON) {
                chonNhanh(p, ds[op]);
            }
        } catch (final Exception e) {
            System.out.println("NhiemVuEngine.doMenu: " + e);
        }
    }

    static void hien(final Char p, final List<String> ma, final List<String> nhan, final int menu, final String rong) {
        if (ma.isEmpty()) {
            SoTayLauDai.bao(p, rong);
            return;
        }
        MENU.put(Integer.valueOf(p.charDBID), ma.toArray(new String[0]));
        p.sendMessage(MessageCreator.createMsgMenu(nhan.toArray(new String[0]), SoTayLauDai.NPC_ID, menu));
    }

    /**
     * Mot dong tien do co hien o "Đang làm" khong: chua nhan thuong (trang thai &lt; 2); thanh tuu chi hien khi da xong cho
     * nhan (1). Dung CHUNG cho so dem "Đang làm (n)" va danh sach - bot-cap1-40 (25/10/2026): truoc day so dem bo moi thanh
     * tuu nen hien "Đang làm (0)" trong khi ben trong co "Bước chân đầu tiên 10/10 (nhận thưởng)" - nguoi choi khong mo.
     */
    public static boolean hienDangLam(final boolean laThanhTuu, final long trangThai) {
        return trangThai < 2 && !(laThanhTuu && trangThai == 0);
    }

    static void dsDangLam(final Char p) {
        final ConcurrentHashMap<String, long[]> t = td(p);
        final List<String> ma = new ArrayList<String>();
        final List<String> nhan = new ArrayList<String>();
        if (t != null) {
            for (final Entry<String, long[]> e : t.entrySet()) {
                final NhiemVuMau m = MAU.get(e.getKey());
                final long[] v = e.getValue();
                if (m == null || !hienDangLam(m.laThanhTuu(), v[1]) || ma.size() >= NhiemVuMau.DS_TOI_DA) {
                    continue;
                }
                ma.add(m.ma);
                nhan.add(m.ten + " " + v[0] + "/" + m.soLuong + (v[1] == 1 ? " (nhận thưởng)" : ""));
            }
        }
        toDam(p, ma, nhan);
        hien(p, ma, nhan, M_DANG, "Chưa có nhiệm vụ nào đang làm.");
    }

    /** Danh sach co the nhan theo nhom: 1 phu, 2 ngay, 3 tuan, 4 thanh tuu, 5 su kien. */
    /** Ma gia cua dong "Xem thêm" trong So tay -> Nhan nhiem vu phu. */
    static final String XEM_THEM = "#xem_them";
    /** Trang dang xem cua "Nhận nhiệm vụ phụ" theo nhan vat (mo tu menu goc -> ve trang 1). */
    static final ConcurrentHashMap<Integer, Integer> TRANG_PHU = new ConcurrentHashMap<Integer, Integer>();

    /**
     * Chia trang danh sach phu: moi trang DS_TOI_DA - 1 muc + dong "Xem thêm" khi nhieu hon DS_TOI_DA (trang cuoi quay ve
     * dau). @return {tu, den (khong gom), so trang, trang that}
     */
    public static int[] trangPhu(final int tong, final int trang) {
        if (tong <= NhiemVuMau.DS_TOI_DA) {
            return new int[]{0, tong, 1, 0};
        }
        final int moi = NhiemVuMau.DS_TOI_DA - 1;
        final int soTrang = (tong + moi - 1) / moi;
        final int t = Math.floorMod(trang, soTrang);
        return new int[]{t * moi, Math.min(tong, t * moi + moi), soTrang, t};
    }

    static void dsCoTheNhan(final Char p, final int nhom) {
        final ConcurrentHashMap<String, long[]> t = td(p);
        if (t == null) {
            return;
        }
        final Set<String> xong = daXong(p, t);
        final int ngay = LauDai.ngayMoNoiDung(); // tran theo mua tat: ngay_mo khong con chan (chi cap + truoc)
        final int gio = com.htbb.pokemon.DongHo.lich().get(java.util.Calendar.HOUR_OF_DAY);
        final List<String> ma = new ArrayList<String>();
        final List<String> nhan = new ArrayList<String>();
        if (nhom == 4) {
            for (final NhiemVuMau m : MAU.values()) {
                if (m.laThanhTuu() && ma.size() < NhiemVuMau.DS_TOI_DA) {
                    final long[] v = t.get(m.ma);
                    if (v != null && v[1] == 2) {
                        continue;
                    }
                    ma.add(m.ma);
                    nhan.add(m.ten + " " + (v == null ? 0 : v[0]) + "/" + m.soLuong + (v != null && v[1] == 1 ? " (nhận)" : ""));
                }
            }
            MENU.put(Integer.valueOf(p.charDBID), ma.toArray(new String[0]));
            if (!ma.isEmpty()) {
                p.sendMessage(MessageCreator.createMsgMenu(nhan.toArray(new String[0]), SoTayLauDai.NPC_ID, M_DANG));
            }
            return;
        }
        final List<String> ngayRut = nhom == 2 ? bangNgay(p) : null;
        final boolean trangPhu = nhom == 1; // phu: lay HET roi xep gan cap + chia trang (duoi)
        final List<Integer> capPhu = new ArrayList<Integer>();
        for (final NhiemVuMau m : MAU.values()) {
            if (!trangPhu && ma.size() >= NhiemVuMau.DS_TOI_DA || !(NhiemVuMau.engineQuanLy(m.loai) || m.chuoiNam2) || m.laThanhTuu()) {
                continue;
            }
            if (m.chuoiNam2 && (nhom != 6 || kiemNam2(m) != null)) {
                continue; // chuoi nam 2: chi o muc "Cot truyen nam 2", khi co ban do + tran cap mua
            }
            final boolean dung = nhom == 6 && m.chuoiNam2 || nhom == 7 && m.laCuaAi() || nhom == 1 && m.chiemKhe() || nhom == 2 && ngayRut.contains(m.ma)
                    || nhom == 3 && m.laTuan() || nhom == 5 && m.laSuKien() && m.tuanSuKien() == SuKienLauDai.tuanIso(com.htbb.pokemon.DongHo.bay());
            if (!dung || t.containsKey(m.ma) && !(m.laNgay() || m.laTuan() || m.laSuKien())) {
                continue;
            }
            final long[] v = t.get(m.ma);
            if (v != null && v[2] == kyCua(m)) {
                continue; // da nhan ky nay
            }
            if (m.kiemNhan(p.lvDetail.lv, ngay, xong, NhiemVuMau.moAn(m.dieuKienMo, gio, null)) != null) {
                continue;
            }
            ma.add(m.ma);
            nhan.add(m.ten + (m.tenNpc.isEmpty() ? "" : " (" + m.tenNpc + ")"));
            capPhu.add(Integer.valueOf(m.cap));
        }
        if (trangPhu && !ma.isEmpty()) {
            final int[] lv = new int[capPhu.size()];
            final List<Integer> chiSo = new ArrayList<Integer>();
            for (int i = 0; i < lv.length; i++) {
                lv[i] = capPhu.get(i).intValue();
                chiSo.add(Integer.valueOf(i));
            }
            final List<Integer> thuTu = com.htbb.pokemon.NhiemVuLauDai.xepGanCap(lv, p.lvDetail.lv, chiSo);
            final Integer tr = TRANG_PHU.get(Integer.valueOf(p.charDBID));
            final int[] cat = trangPhu(thuTu.size(), tr == null ? 0 : tr.intValue());
            final List<String> ma2 = new ArrayList<String>();
            final List<String> nhan2 = new ArrayList<String>();
            for (int k = cat[0]; k < cat[1]; k++) {
                ma2.add(ma.get(thuTu.get(k).intValue()));
                nhan2.add(nhan.get(thuTu.get(k).intValue()));
            }
            if (cat[2] > 1) {
                ma2.add(XEM_THEM);
                nhan2.add("Xem thêm (trang " + (cat[3] + 1) + "/" + cat[2] + ")");
            }
            TRANG_PHU.put(Integer.valueOf(p.charDBID), Integer.valueOf(cat[3]));
            ma.clear();
            ma.addAll(ma2);
            nhan.clear();
            nhan.addAll(nhan2);
        }
        if (nhom == 7) {
            toDam(p, ma, nhan);
        }
        hien(p, ma, nhan, M_NHAN, nhom == 7 ? "Chưa có cửa ải nào nhận được lúc này (xem cấp và cửa ải trước)."
                : "Chưa có nhiệm vụ phù hợp lúc này.");
    }

    /** Ky lap lai cua mau: ngay -> yyyyMMdd, tuan -> chi so tuan, su kien -> tuan ISO, khac 0. */
    static int kyCua(final NhiemVuMau m) {
        if (m.laNgay()) {
            return homNay();
        }
        if (m.laTuan()) {
            return tuanNay();
        }
        if (m.laSuKien()) {
            return SuKienLauDai.tuanIso(com.htbb.pokemon.DongHo.bay()) + 1000 * (homNay() / 10000);
        }
        return 0;
    }

    /** 5 mau ngay cua hom nay theo bac cap (hat giong id nhan vat + ngay). */
    static List<String> bangNgay(final Char p) {
        final String tienTo = "N-" + NhiemVuMau.bacNgay(p.lvDetail.lv) + ".";
        final List<String> ds = new ArrayList<String>();
        for (final String ma : MAU.keySet()) {
            if (ma.startsWith(tienTo)) {
                ds.add(ma);
            }
        }
        return NhiemVuMau.rutNgay(ds, (long) p.charDBID * 100000L + homNay(), NhiemVuMau.NGAY_RUT);
    }

    static String nhan(final Char p, final String ma) {
        final NhiemVuMau m = MAU.get(ma);
        final ConcurrentHashMap<String, long[]> t = td(p);
        if (m == null || t == null) {
            return "Không nhận được.";
        }
        final int gio = com.htbb.pokemon.DongHo.lich().get(java.util.Calendar.HOUR_OF_DAY);
        final String nam2 = kiemNam2(m);
        if (nam2 != null) {
            return nam2 + ".";
        }
        final String loi = m.kiemNhan(p.lvDetail.lv, LauDai.ngayMoNoiDung(), daXong(p, t), NhiemVuMau.moAn(m.dieuKienMo, gio, null));
        if (loi != null) {
            return loi + ".";
        }
        if (m.laNgay() && !bangNgay(p).contains(ma)) {
            return "Nhiệm vụ này không có trong bảng hằng ngày hôm nay."; // 0.9.6: menu cu / goi tu che
        }
        if (p.subQuest != null && p.subQuest.id_quest >= 0 && p.subQuest.getTemplate() != null
                && ma.equals(p.subQuest.getTemplate().ma)) {
            return "Bạn đang làm nhiệm vụ này (nhận ở NPC)."; // 0.9.7.1: cung ma voi nhiem vu phu cu dang lam
        }
        synchronized (t) {
            final String nhanLai = NhiemVuMau.chanNhanLai(m, t.get(ma), kyCua(m));
            if (nhanLai != null) {
                return nhanLai;
            }
            if (m.chiemKhe()) {
                int khe = 0;
                for (final Entry<String, long[]> e : t.entrySet()) {
                    final NhiemVuMau x = MAU.get(e.getKey());
                    if (x != null && x.chiemKhe() && e.getValue()[1] < 2) {
                        khe++;
                    }
                }
                if (khe >= NhiemVuMau.KHE_PHU) {
                    return "Đã đủ " + NhiemVuMau.KHE_PHU + " nhiệm vụ phụ, hãy hoàn thành bớt.";
                }
                if (t.containsKey(ma)) {
                    return "Đã nhận nhiệm vụ này.";
                }
            }
            if (m.laTuan()) {
                final Set<String> khoa = new HashSet<String>();
                for (final Entry<String, long[]> e : t.entrySet()) {
                    final NhiemVuMau x = MAU.get(e.getKey());
                    if (x != null && x.laTuan() && e.getValue()[2] == tuanNay()) {
                        khoa.add(x.khoaTuan());
                    }
                }
                if (!khoa.contains(m.khoaTuan()) && khoa.size() >= NhiemVuMau.TUAN_TOI_DA) {
                    return "Mỗi tuần chỉ nhận " + NhiemVuMau.TUAN_TOI_DA + " nhiệm vụ tuần.";
                }
            }
            final long[] v = new long[]{0L, 0L, kyCua(m), com.htbb.pokemon.DongHo.bay()};
            if ("noi_chuyen".equals(m.kieu) || "dua_thu".equals(m.kieu)) {
                v[0] = m.soLuong; // gap NPC qua So tay = xong ngay (giao hang / dua thu dung item o du_phong)
                v[1] = 1;
            }
            if ("ha_gym".equals(m.kieu) && daCoHuyHieu(p, m.mucTieuId)) {
                v[0] = m.soLuong; // cua ai "ha Gym": da co huy hieu thi khong bat danh lai
                v[1] = 1;
            }
            t.put(ma, v);
            luu(p, ma, v);
        }
        return NhiemVuMau.hoiThoai(m.hoiThoaiBatDau, 0, m.soLuong) + (m.mucTieuText.isEmpty() ? "" : "\n\nMục tiêu: " + m.mucTieuText)
                + HoTongNhiemVuGame.duoi(p, m); // sua-ho-tong: noi nhan / noi giao xe hang
    }

    static void chonDangLam(final Char p, final String ma) {
        final NhiemVuMau m = MAU.get(ma);
        final ConcurrentHashMap<String, long[]> t = td(p);
        if (m == null || t == null) {
            return;
        }
        final long[] v = t.get(ma);
        if (v == null) {
            return;
        }
        if (v[1] == 0 && "lua_chon".equals(m.kieu)) {
            final List<String[]> lc = NhiemVuMau.luaChon(m.nhanh);
            final List<String> maLc = new ArrayList<String>();
            final List<String> nhan = new ArrayList<String>();
            for (int i = 0; i < lc.size(); i++) {
                maLc.add(ma + "#" + i);
                nhan.add(lc.get(i)[0]);
            }
            SoTayLauDai.bao(p, NhiemVuMau.hoiThoai(m.hoiThoaiBatDau, (int) v[0], m.soLuong));
            hien(p, maLc, nhan, M_CHON, "Không có lựa chọn.");
            return;
        }
        if (v[1] == 1) {
            SoTayLauDai.bao(p, traThuong(p, m, v));
            return;
        }
        // sua-duong-gym: buoc ha Gym -> hop "Thách đấu Trưởng Gym … ngay?" (Đồng ý = nhu So tay -> Thách đấu Gym)
        if (GymLauDai.hoiNeuGym(p, m, v, m.ten + " (" + v[0] + "/" + m.soLuong + ")")) {
            return;
        }
        SoTayLauDai.bao(p, chuDangLam(m, v[0]) + HoTongNhiemVuGame.duoi(p, m)); // sua-ho-tong: noi nhan / giao + chi duong
    }

    /**
     * Chu "dang lam" (ra soat hien thi 10/2026): loi NPC thoi chua noi phai lam gi o dau ("Bạn chưa gặp Weedle vao
     * buoi sang") - luon kem dong "Mục tiêu:" (muc_tieu_text) + tien do, nguoi choi khong phai nho lai loi giao.
     */
    public static String chuDangLam(final NhiemVuMau m, final long da) {
        final String loi = NhiemVuMau.hoiThoai(m.hoiThoaiDangLam.isEmpty() ? m.mucTieuText : m.hoiThoaiDangLam, (int) da, m.soLuong);
        final boolean kem = !m.hoiThoaiDangLam.isEmpty() && !m.mucTieuText.isEmpty() && !loi.contains(m.mucTieuText);
        return loi + (kem ? "\nMục tiêu: " + m.mucTieuText : "") + "\n(" + da + "/" + m.soLuong + ")";
    }

    /** Chon nhanh "<ma>#<i>": ghi nhanh, hoan thanh, trao thuong nhanh. */
    static void chonNhanh(final Char p, final String maNhanh) {
        final int i = maNhanh.lastIndexOf('#');
        final String ma = maNhanh.substring(0, i);
        final int nhanh = Integer.parseInt(maNhanh.substring(i + 1));
        final NhiemVuMau m = MAU.get(ma);
        final ConcurrentHashMap<String, long[]> t = td(p);
        if (m == null || t == null) {
            return;
        }
        final long[] v = t.get(ma);
        if (v == null || v[1] != 0) {
            return;
        }
        final List<String[]> lc = NhiemVuMau.luaChon(m.nhanh);
        if (nhanh < 0 || nhanh >= lc.size()) {
            return;
        }
        final long[] n = new long[]{nhanh, 2L, 0L, 0L};
        t.put(ma + "#nhanh", n);
        luu(p, ma + "#nhanh", n);
        v[0] = m.soLuong;
        v[1] = 1;
        luu(p, ma, v);
        thuongKhac(p, lc.get(nhanh)[1], "lua chon " + ma);
        SoTayLauDai.bao(p, "Bạn chọn: " + lc.get(nhanh)[0] + ".\n" + traThuong(p, m, v));
    }

    /** Trao thuong (danh dau da nhan TRUOC). */
    /**
     * Xong du {@link NhiemVuMau#TUAN_TOI_DA} nhiem vu tuan (lan nhan thuong thu 2 trong tuan): 2 Ve EXP x1,5 khoa
     * + 1 Phieu uy thac khoa (TangToc - nguon choi cua ban khoa, dung chung tran voi ban cao cap).
     */
    static void thuongTangTocTuan(final Char p, final ConcurrentHashMap<String, long[]> t) {
        int daNhan = 0;
        synchronized (t) {
            for (final Entry<String, long[]> e : t.entrySet()) {
                final NhiemVuMau x = MAU.get(e.getKey());
                if (x != null && x.laTuan() && e.getValue()[1] == 2 && e.getValue()[2] == tuanNay()) {
                    daNhan++;
                }
            }
        }
        if (daNhan == NhiemVuMau.TUAN_TOI_DA) {
            TangTocGame.thuongKhoa(p, com.htbb.pokemon.TangToc.VE_15, 2, "nhiem vu tuan");
            TangTocGame.thuongKhoa(p, com.htbb.pokemon.TangToc.UY_THAC, 1, "nhiem vu tuan");
        }
    }

    static String traThuong(final Char p, final NhiemVuMau m, final long[] v) {
        final ConcurrentHashMap<String, long[]> t = td(p);
        synchronized (t) {
            if (v[1] != 1) {
                return "Đã nhận thưởng.";
            }
            if (m.laNgay()) {
                int daNhan = 0;
                for (final Entry<String, long[]> e : t.entrySet()) {
                    final NhiemVuMau x = MAU.get(e.getKey());
                    if (x != null && x.laNgay() && e.getValue()[1] == 2 && e.getValue()[2] == homNay()) {
                        daNhan++;
                    }
                }
                if (daNhan >= NhiemVuMau.NGAY_THUONG) {
                    return "Hôm nay đã nhận đủ " + NhiemVuMau.NGAY_THUONG + " phần thưởng nhiệm vụ hằng ngày.";
                }
            }
            // 0.9.7.1: thuong co vat pham ma tui day -> chua trao (truoc day vat pham bi bo im lang, nhiem vu van "đã nhận")
            // 23/10/2026: can DU o cho MOI mon (item:1039x3 = 3 o) - truoc chi kiem "con 1 o" nen thieu o thi mat phan con lai.
            final int canO = soOThuong(p, m); // [chong-vat-pham] type 28 don vao chong san co: can it o hon
            if (canO > 0 && p.countSlotInventory() < canO) {
                return canO <= 1 ? Char.TUI_DAY_NHIEM_VU
                        : "Hành trang không đủ chỗ. Hãy dọn bớt để còn " + canO + " ô trống rồi nhận thưởng nhiệm vụ.";
            }
            v[1] = 2;
            luu(p, m.ma, v);
        }
        try {
            if (m.exp > 0) {
                Map.addXPForChar(p, m.exp, false, "nhiem vu " + m.ma);
            }
        } catch (final Exception ignored) {
        }
        if (m.xu > 0) {
            p.addXu(m.xu, "nhiem vu " + m.ma);
        }
        if (m.luongKhoa > 0) {
            p.addLuongLock(m.luongKhoa);
        }
        if (m.luong > 0 && m.laThanhTuu()) {
            p.addLuong(m.luong); // luong chi tu thanh tuu lon (THIET_KE 2.1)
        }
        if (m.vatPham != null && !m.vatPham.isEmpty()) {
            for (final String id : m.vatPham.split("[,;|]")) {
                try {
                    final int i = Integer.parseInt(id.trim());
                    final ItemTemplates tp = Map.itemTemplates.get(5).get(Integer.valueOf(i));
                    if (tp != null && (tp.type == 28 ? com.htbb.pokemon.ChongVatPham.conCho(p, i, 1) : !p.isFullInventory())) {
                        if (tp.type == 28) {
                            Char.createItemSoLuong(i, 1, p);
                        } else {
                            final Item it = Map.dropItemByIdTemplate(p.inCountry, i);
                            if (it != null) {
                                it.id = p.getIDItem();
                                it.owner = p.charDBID;
                                it.lock = 1;
                                p.iItems.add(it);
                            }
                        }
                    }
                } catch (final Exception ignored) {
                }
            }
        }
        thuongKhac(p, m.thuongKhac, "nhiem vu " + m.ma);
        if (m.laTuan()) {
            thuongTangTocTuan(p, t);
        }
        if (m.laCuaAi()) {
            KhoaBanDoGame.capNhat(p); // xong cua ai: bao ban do vua mo + gui lai goi -108
        }
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        suKien(p, "thanh_tuu_nhiem_vu", "", 1);
        if (m.laNgay()) {
            suKien(p, "thanh_tuu_ngay", "", 1);
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, "nhiem vu lau dai " + m.ma + ": " + m.exp + " exp " + m.xu + " xu " + m.luongKhoa + " lk", "laudai");
        } catch (final Exception ignored) {
        }
        return NhiemVuMau.hoiThoai(m.hoiThoaiXong, m.soLuong, m.soLuong) + "\n\nNhận thưởng: " + so(m.exp) + " EXP, " + so(m.xu) + " xu"
                + (m.luongKhoa > 0 ? ", " + so(m.luongKhoa) + " lượng khoá" : "") + ".";
    }

    /**
     * So o hanh trang can de nhan du vat pham thuong: moi id trong vat_pham 1 o, moi "item:&lt;id&gt;xN" cua thuong_khac
     * N o (Char.createItemSoLuong tao N mon rieng).
     */
    /**
     * [chong-vat-pham] Nhu {@link #soOThuong(NhiemVuMau)} nhung theo tui THAT cua nguoi choi: vat pham type 28 (qua
     * Char.createItemSoLuong) don vao chong san co nen chi can o cho phan tran chong (ChongVatPham.soOCan).
     */
    static int soOThuong(final Char p, final NhiemVuMau m) {
        final java.util.LinkedHashMap<Integer, Integer> can = new java.util.LinkedHashMap<Integer, Integer>();
        int o = 0;
        if (m.vatPham != null) {
            for (final String id : m.vatPham.split("[,;|]")) {
                if (id.trim().length() > 0) {
                    try {
                        can.merge(Integer.valueOf(Integer.parseInt(id.trim())), Integer.valueOf(1), Integer::sum);
                    } catch (final RuntimeException e) {
                        o++;
                    }
                }
            }
        }
        if (m.thuongKhac != null) {
            for (final String phan : m.thuongKhac.split(";")) {
                final String t = phan.trim();
                if (!t.startsWith("item:")) {
                    continue;
                }
                final String v = t.substring(5).trim();
                final int x = v.indexOf('x');
                try {
                    final int id = Integer.parseInt((x > 0 ? v.substring(0, x) : v).trim());
                    final int n = x > 0 ? Math.max(0, Integer.parseInt(v.substring(x + 1).trim())) : 1;
                    can.merge(Integer.valueOf(id), Integer.valueOf(n), Integer::sum);
                } catch (final RuntimeException e) {
                    o++;
                }
            }
        }
        for (final java.util.Map.Entry<Integer, Integer> e : can.entrySet()) {
            final ItemTemplates tp = Map.itemTemplates.get(5).get(e.getKey());
            o += tp != null && tp.type == 28 ? com.htbb.pokemon.ChongVatPham.soOCan(p, e.getKey().intValue(), e.getValue().intValue())
                    : e.getValue().intValue();
        }
        return o;
    }

    static int soOThuong(final NhiemVuMau m) {
        int o = 0;
        if (m.vatPham != null) {
            for (final String id : m.vatPham.split("[,;|]")) {
                if (id.trim().length() > 0) {
                    o++;
                }
            }
        }
        if (m.thuongKhac != null) {
            for (final String phan : m.thuongKhac.split(";")) {
                final String t = phan.trim();
                if (!t.startsWith("item:")) {
                    continue;
                }
                final String v = t.substring(5).trim();
                final int x = v.indexOf('x');
                try {
                    o += x > 0 ? Math.max(0, Integer.parseInt(v.substring(x + 1).trim())) : 1;
                } catch (final RuntimeException e) {
                    o++;
                }
            }
        }
        return o;
    }

    /** Thuong co vat pham can o hanh trang (vat_pham, thuong_khac item:). */
    static boolean coThuongVatPham(final NhiemVuMau m) {
        return m.vatPham != null && m.vatPham.trim().length() > 0 || m.thuongKhac != null && m.thuongKhac.contains("item:");
    }

        /** So kieu Viet: 15479 -> "15.479" (chu nguoi choi thay). */
    private static String so(final long v) {
        return String.format(java.util.Locale.ROOT, "%,d", v).replace(',', '.');
    }

    /** thuong_khac "danh_hieu:<ten>;item:<id>xN;truyen:..;trang_tri:..;diem_bang:N;bong:<poke|great|ultra>xN;pet_dong_hanh:1". */
    static void thuongKhac(final Char p, final String s, final String lyDo) {
        if (s == null || s.isEmpty()) {
            return;
        }
        for (final String phan : s.split(";")) {
            final int i = phan.indexOf(':');
            if (i <= 0) {
                continue;
            }
            final String k = phan.substring(0, i).trim();
            final String v = phan.substring(i + 1).trim();
            try {
                if ("item".equals(k)) {
                    final int x = v.indexOf('x');
                    final int id = Integer.parseInt(x > 0 ? v.substring(0, x) : v);
                    final int n = x > 0 ? Integer.parseInt(v.substring(x + 1)) : 1;
                    NoiDungLauDai.phat(p, "item", id, n, true);
                } else if ("thuoc".equals(k)) {
                    // thuoc:<id>xN (vat pham su kien dang thuoc, vd Keo 137) - truoc chi ghi log (kiem_item_mo_coi)
                    final int x = v.indexOf('x');
                    final int id = Integer.parseInt(x > 0 ? v.substring(0, x) : v);
                    final int n = x > 0 ? Integer.parseInt(v.substring(x + 1)) : 1;
                    if (id > 0 && id < p.potions.length && n > 0) {
                        p.potions[id] += n;
                    }
                } else if ("danh_hieu".equals(k)) {
                    // danh hieu theo TEN (khong co id trong DanhHieu): ghi nhan o tien do, hien trong So tay
                    final long[] d = new long[]{1L, 2L, 0L, 0L};
                    td(p).put("DH:" + v, d);
                    luu(p, cat("DH:" + v), d);
                    // co mau trong data_danh_hieu: trao danh hieu deo duoc (DanhHieuGame tu bao); khong co: bao nhu cu
                    if (!DanhHieuGame.khiNhanTheoTen(p, v)) {
                        LauDaiNhanVat.bao(p, "Danh hiệu mới: " + v);
                    }
                } else if ("bong".equals(k)) {
                    // 0.9.8: bong:<poke|great|ultra>xN -> Hop PC (tran bong tang / ngay)
                    final int x = v.indexOf('x');
                    BatPokemonGame.thuongBong(p, x > 0 ? v.substring(0, x) : v, x > 0 ? Integer.parseInt(v.substring(x + 1)) : 1, "nhiệm vụ");
                } else if (com.htbb.pokemon.PetDongHanh.THUONG_KHAC.equals(k)) {
                    // pet dong hanh mien phi MOT lan / nhan vat (buoc C07.3): mo menu chon loai; co CSDL tob_pet_dong_hanh
                    // dung chung voi muc Nha tre nen nhan vat cu da nhan o Nha tre khong nhan them
                    com.htbb.pokemon.HeSinhThaiMenu.moNhanPetDongHanh(p, true);
                } else if ("trang_tri".equals(k) && TrangPhucGame.laMa(v)) {
                    TrangPhucGame.trao(p, v, "nhiem_vu"); // [trang-phuc] mon tu do trang tri
                } else {
                    Database.instance.saveOrtherLog("", p.charname, lyDo + " thuong khac " + k + ":" + v, "laudai");
                }
            } catch (final Exception e) {
                System.out.println("NhiemVuEngine.thuongKhac: " + e);
            }
        }
    }

    /** Huy hieu Gym so thu tu {@code stt} (chuoi so) da co chua. */
    static boolean daCoHuyHieu(final Char p, final String stt) {
        try {
            return GymLauDai.coHuyHieu(LauDaiNhanVat.lay(p), Integer.parseInt(stt.trim()));
        } catch (final RuntimeException e) {
            return false;
        }
    }

    /**
     * Danh hieu thuong nhiem vu theo TEN da nhan chua (tien do "DH:&lt;ten&gt;"; khoa luu CSDL bi cat 32 ky tu nen
     * xem ca ban cat).
     */
    public static boolean coDanhHieuTen(final Char p, final String ten) {
        if (p == null || ten == null || ten.isEmpty() || p.charDBID <= 0) {
            return false;
        }
        try {
            final ConcurrentHashMap<String, long[]> t = td(p);
            return t != null && (t.containsKey("DH:" + ten) || t.containsKey(cat("DH:" + ten)));
        } catch (final Exception e) {
            return false;
        }
    }

    static String cat(final String s) {
        return s.length() <= 32 ? s : s.substring(0, 32);
    }
}
