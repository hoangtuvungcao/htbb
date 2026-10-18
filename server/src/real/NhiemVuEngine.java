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
 * ENGINE nhiem vu tong quat (GD2b; yeu cau o docs/pokemon/NHIEM_VU_THIET_KE.md muc 5). Quan ly moi
 * nhiem vu KHONG thuoc chuoi chinh (phu, tuyen, huyen thoai, an, Pokedex, ngay, tuan, su kien, thanh
 * tuu, su pham, doi, quay lai...) theo du lieu - them nhiem vu / kieu muc tieu moi chi can dong du lieu
 * va mot cho goi {@link #suKien}.
 *
 *  - Danh muc kieu: moi kieu_muc_tieu la mot "su kien" (chuoi) do diem moc trong game ban vao
 *    {@link #suKien}(p, kieu, thamSo, n); khop theo muc_tieu_id (rong = bat ky). Kieu chua co diem
 *    moc (he sinh thai: cau_ca, dao_dat, lam_vuon, nau_an, ap_trung, do_vui, dua_pokemon, cho_ban...) chi
 *    can lop do goi {@code NhiemVuEngine.suKien(p, "cau_ca", vat, 1)}.
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
        System.out.println("[LAU DAI] engine nhiem vu: " + m.size() + " mau, " + k.size() + " kieu muc tieu");
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

    static void luu(final Char p, final String ma, final long[] v) {
        DatabaseLauDai.luuNhiemVu(p.charDBID, ma, (int) v[0], (int) v[1], (int) v[2]);
    }

    static int homNay() {
        return NangLuong.ngay(System.currentTimeMillis());
    }

    static int tuanNay() {
        return NhiemVuLauDai.tuan(System.currentTimeMillis());
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
                if (m.phut > 0 && v[3] > 0 && System.currentTimeMillis() - v[3] > m.phut * 60000L) {
                    v[0] = 0; // thu thach gio: het gio thi dem lai tu dau
                    v[3] = System.currentTimeMillis();
                }
                v[0] = Math.min(m.soLuong, v[0] + n);
                if (v[0] >= m.soLuong) {
                    v[1] = 1;
                    LauDaiNhanVat.bao(p, "Nhiệm vụ \"" + m.ten + "\" hoàn thành - mở Sổ tay huấn luyện để nhận thưởng.");
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
                LauDaiNhanVat.bao(p, "Thành tựu / nhiệm vụ \"" + m.ten + "\" đạt - mở Sổ tay để nhận.");
            }
            luu(p, m.ma, v);
        }
    }

    /** Dieu kien them cua mau luc co su kien (quan sat: dem / gio; to doi toi thieu...). */
    static boolean dieuKienSuKien(final Char p, final NhiemVuMau m) {
        final String qs = m.dieuKien.get("dieu_kien_quan_sat");
        if (qs != null) {
            final int gio = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
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
        final String id = String.valueOf(mt.idTemplate);
        final int delta = Math.abs(p.lvDetail.lv - mt.level);
        suKien(p, "giet", id, 1);
        if (delta <= 5) {
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
        final int dex = PokedexPokemon.dexQuai(mt);
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
        for (final Entry<String, long[]> e : t.entrySet()) {
            if (e.getValue()[1] != 0) {
                continue;
            }
            final NhiemVuMau m = MAU.get(e.getKey());
            if (m == null) {
                continue;
            }
            if (("dung_toa_do".equals(m.kieu) || "tim_vat_an".equals(m.kieu) || "den_noi".equals(m.kieu))
                    && m.trongToaDo(p.mapID, oX, oY)) {
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
                LauDaiNhanVat.bao(p, "Chính xác! Nhiệm vụ \"" + m.ten + "\" hoàn thành.");
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ menu

    static void moMenu(final Char p) {
        if (!LauDai.bat()) {
            return;
        }
        final ConcurrentHashMap<String, long[]> t = td(p);
        int dang = 0;
        if (t != null) {
            for (final Entry<String, long[]> e : t.entrySet()) {
                final NhiemVuMau m = MAU.get(e.getKey());
                if (m != null && e.getValue()[1] < 2 && !m.laThanhTuu()) {
                    dang++;
                }
            }
        }
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Đang làm (" + dang + ")", "Nhận nhiệm vụ phụ", "Bảng ngày",
            "Bảng tuần", "Thành tựu", "Sự kiện tuần này"}, SoTayLauDai.NPC_ID, M_GOC));
    }

    static void doMenu(final Char p, final int idMenu, final int op) {
        try {
            if (idMenu == M_GOC) {
                if (op == 0) {
                    dsDangLam(p);
                } else {
                    dsCoTheNhan(p, op);
                }
                return;
            }
            final String[] ds = MENU.get(Integer.valueOf(p.charDBID));
            if (ds == null || op < 0 || op >= ds.length) {
                return;
            }
            if (idMenu == M_DANG) {
                chonDangLam(p, ds[op]);
            } else if (idMenu == M_NHAN) {
                SoTayLauDai.bao(p, nhan(p, ds[op]));
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

    static void dsDangLam(final Char p) {
        final ConcurrentHashMap<String, long[]> t = td(p);
        final List<String> ma = new ArrayList<String>();
        final List<String> nhan = new ArrayList<String>();
        if (t != null) {
            for (final Entry<String, long[]> e : t.entrySet()) {
                final NhiemVuMau m = MAU.get(e.getKey());
                final long[] v = e.getValue();
                if (m == null || v[1] >= 2 || m.laThanhTuu() && v[1] == 0 || ma.size() >= NhiemVuMau.DS_TOI_DA) {
                    continue;
                }
                ma.add(m.ma);
                nhan.add(m.ten + " " + v[0] + "/" + m.soLuong + (v[1] == 1 ? " (nhận thưởng)" : ""));
            }
        }
        hien(p, ma, nhan, M_DANG, "Chưa có nhiệm vụ nào đang làm.");
    }

    /** Danh sach co the nhan theo nhom: 1 phu, 2 ngay, 3 tuan, 4 thanh tuu, 5 su kien. */
    static void dsCoTheNhan(final Char p, final int nhom) {
        final ConcurrentHashMap<String, long[]> t = td(p);
        if (t == null) {
            return;
        }
        final Set<String> xong = daXong(t);
        final int ngay = LauDai.ngayHienTai();
        final int gio = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
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
        for (final NhiemVuMau m : MAU.values()) {
            if (ma.size() >= NhiemVuMau.DS_TOI_DA || !NhiemVuMau.engineQuanLy(m.loai) || m.laThanhTuu()) {
                continue;
            }
            final boolean dung = nhom == 1 && m.chiemKhe() || nhom == 2 && ngayRut.contains(m.ma)
                    || nhom == 3 && m.laTuan() || nhom == 5 && m.laSuKien() && m.tuanSuKien() == SuKienLauDai.tuanIso(System.currentTimeMillis());
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
        }
        hien(p, ma, nhan, M_NHAN, "Chưa có nhiệm vụ phù hợp lúc này.");
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
            return SuKienLauDai.tuanIso(System.currentTimeMillis()) + 1000 * (homNay() / 10000);
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
        final int gio = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        final String loi = m.kiemNhan(p.lvDetail.lv, LauDai.ngayHienTai(), daXong(t), NhiemVuMau.moAn(m.dieuKienMo, gio, null));
        if (loi != null) {
            return loi + ".";
        }
        synchronized (t) {
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
            final long[] v = new long[]{0L, 0L, kyCua(m), System.currentTimeMillis()};
            if ("noi_chuyen".equals(m.kieu) || "dua_thu".equals(m.kieu)) {
                v[0] = m.soLuong; // gap NPC qua So tay = xong ngay (giao hang / dua thu dung item o du_phong)
                v[1] = 1;
            }
            t.put(ma, v);
            luu(p, ma, v);
        }
        return NhiemVuMau.hoiThoai(m.hoiThoaiBatDau, 0, m.soLuong) + (m.mucTieuText.isEmpty() ? "" : "\n\nMục tiêu: " + m.mucTieuText);
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
        SoTayLauDai.bao(p, NhiemVuMau.hoiThoai(m.hoiThoaiDangLam.isEmpty() ? m.mucTieuText : m.hoiThoaiDangLam, (int) v[0], m.soLuong)
                + "\n(" + v[0] + "/" + m.soLuong + ")");
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
                    return "Hôm nay đã nhận đủ " + NhiemVuMau.NGAY_THUONG + " phần thưởng bảng ngày.";
                }
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
                    if (tp != null && !p.isFullInventory()) {
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
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        suKien(p, "thanh_tuu_nhiem_vu", "", 1);
        if (m.laNgay()) {
            suKien(p, "thanh_tuu_ngay", "", 1);
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, "nhiem vu lau dai " + m.ma + ": " + m.exp + " exp " + m.xu + " xu " + m.luongKhoa + " lk", "laudai");
        } catch (final Exception ignored) {
        }
        return NhiemVuMau.hoiThoai(m.hoiThoaiXong, m.soLuong, m.soLuong) + "\n\nNhận thưởng: " + m.exp + " EXP, " + m.xu + " xu"
                + (m.luongKhoa > 0 ? ", " + m.luongKhoa + " lượng khoá" : "") + ".";
    }

    /** thuong_khac "danh_hieu:<ten>;item:<id>xN;truyen:..;trang_tri:..;diem_bang:N". */
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
                } else if ("danh_hieu".equals(k)) {
                    // danh hieu theo TEN (khong co id trong DanhHieu): ghi nhan o tien do, hien trong So tay
                    final long[] d = new long[]{1L, 2L, 0L, 0L};
                    td(p).put("DH:" + v, d);
                    luu(p, cat("DH:" + v), d);
                    LauDaiNhanVat.bao(p, "Danh hiệu mới: " + v);
                } else {
                    Database.instance.saveOrtherLog("", p.charname, lyDo + " thuong khac " + k + ":" + v, "laudai");
                }
            } catch (final Exception e) {
                System.out.println("NhiemVuEngine.thuongKhac: " + e);
            }
        }
    }

    static String cat(final String s) {
        return s.length() <= 32 ? s : s.substring(0, 32);
    }
}
