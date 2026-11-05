package real;

import com.htbb.pokemon.LauDai;
import data.Database;
import data.DatabaseLauDai;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * "Cửa hàng & đổi vật phẩm" trong Sổ tay huấn luyện (chỉ khi sv.lau_dai=1). Dữ liệu:
 * data_cua_hang_lau_dai (sinh bởi tools/luu_tru_sinh_du_lieu/pokemon/sinh_vat_pham_lau_dai.py). Mỗi dòng: chi phí
 * (xu:N / luong_khoa:N / item:IDxN / thuoc:IDxN / ngoc:IDxN, ngăn ';') -> thưởng (ITEM:id:n /
 * GEM:id:n / PT:id:n, ngăn ','). Thưởng KHÔNG BAO GIỜ là tiền tệ (sinh_vat_pham_lau_dai chặn).
 *
 * Vì sao có lớp này: luật "không vật phẩm mồ côi" (tools/luu_tru_sinh_du_lieu/pokemon/kiem_item_mo_coi.py,
 * tools/luu_tru_sinh_du_lieu/du_lieu/KIEM_ITEM.md) - thuốc / quả mọng / đồ giữ / bùa mới (1201..) chưa có nguồn,
 * huy chương / tín vật sự kiện (1039..1061) chưa có công dụng, trang bị năm 2 chưa có nơi bán / chế tạo.
 *
 * Trừ và kiểm trong MỘT khối Char.truNeuDu (KHOA_TIEN): thiếu một thứ thì không trừ gì. Chỗ trống hành
 * trang kiểm trước. Giới hạn tuần dùng tob_mua_tuan (id_shop = 10000 + id dòng).
 */
public final class CuaHangLauDai {

    static final int MENU_NHOM = 20;
    static final int MENU_DONG = 21;
    static final int ID_TUAN = 10000;

    /** {id, nhom, ten, chi_phi, thuong, cap, ngay_mo, lop, gioi_han_tuan}. */
    static volatile List<Object[]> BANG = new ArrayList<Object[]>();
    /** charDBID -> danh sach dong vua hien (chon theo chi so). */
    private static final ConcurrentHashMap<Integer, List<Object[]>> DANG_XEM = new ConcurrentHashMap<Integer, List<Object[]>>();
    private static final ConcurrentHashMap<Integer, String> NHOM_XEM = new ConcurrentHashMap<Integer, String>();

    private CuaHangLauDai() {
    }

    public static void nap() {
        BANG = DatabaseLauDai.napCuaHangLauDai();
        System.out.println("[LAU DAI] cua hang & doi vat pham: " + BANG.size() + " dong");
    }

    static LinkedHashMap<String, Integer> nhom() {
        final LinkedHashMap<String, Integer> m = new LinkedHashMap<String, Integer>();
        for (final Object[] r : BANG) {
            m.merge((String) r[1], 1, Integer::sum);
        }
        return m;
    }

    public static void moMenu(final Char p) {
        if (!LauDai.bat() || p == null) {
            return;
        }
        final List<String> ten = new ArrayList<String>(nhom().keySet());
        // Guc / hoi sinh: dong "Thuoc hoi sinh" gia theo cap NGUOI XEM (HoiSinhGame), luon o cuoi
        final String hs = HoiSinhGame.dongCuaHang(p);
        if (hs != null) {
            ten.add(hs);
        }
        if (ten.isEmpty()) {
            LauDaiNhanVat.bao(p, "Cửa hàng chưa có hàng.");
            return;
        }
        p.sendMessage(MessageCreator.createMsgMenu(ten.toArray(new String[0]), SoTayLauDai.NPC_ID, MENU_NHOM));
    }

    static boolean hien(final Char p, final Object[] r) {
        final int lop = ((Integer) r[7]).intValue();
        return LauDai.ngayMoNoiDung() >= ((Integer) r[6]).intValue() && (lop < 0 || lop == p.charClass);
    }

    static void moNhom(final Char p, final String nhom) {
        final List<Object[]> ds = new ArrayList<Object[]>();
        final List<String> dong = new ArrayList<String>();
        for (final Object[] r : BANG) {
            if (nhom.equals(r[1]) && hien(p, r)) {
                ds.add(r);
                dong.add(r[2] + " - " + moTaChiPhi((String) r[3]) + (((Integer) r[5]).intValue() > 1 ? ", cấp " + r[5] : ""));
            }
        }
        if (ds.isEmpty()) {
            LauDaiNhanVat.bao(p, "Nhóm này chưa mở.");
            return;
        }
        DANG_XEM.put(Integer.valueOf(p.charDBID), ds);
        NHOM_XEM.put(Integer.valueOf(p.charDBID), nhom);
        p.sendMessage(MessageCreator.createMsgMenu(dong.toArray(new String[0]), SoTayLauDai.NPC_ID, MENU_DONG));
    }

    /** SoTayLauDai.doMenu: idMenu MENU_NHOM / MENU_DONG. */
    public static void doMenu(final Char p, final int idMenu, final int idOption) {
        if (!LauDai.bat() || p == null) {
            return;
        }
        if (idMenu == MENU_NHOM) {
            final List<String> ten = new ArrayList<String>(nhom().keySet());
            if (idOption >= 0 && idOption < ten.size()) {
                moNhom(p, ten.get(idOption));
            } else if (idOption == ten.size()) {
                HoiSinhGame.moMuaThuoc(p); // dong "Thuoc hoi sinh" cuoi menu
            }
        } else if (idMenu == MENU_DONG) {
            final List<Object[]> ds = DANG_XEM.get(Integer.valueOf(p.charDBID));
            if (ds == null || idOption < 0 || idOption >= ds.size()) {
                return;
            }
            // Ket qua mua phai THAY DUOC: LauDaiNhanVat.bao (-26) chi vao hop tin cua client, nguoi choi bam mua
            // ma khong thay gi. DungVatPham: thong bao nhanh (ban >= 902) / hop thoai OK (client cu).
            final String kq = mua(p, ds.get(idOption));
            if (kq.startsWith("Đã nhận")) {
                DungVatPham.bao(p, kq);
            } else {
                DungVatPham.loi(p, kq);
            }
            final String n = NHOM_XEM.get(Integer.valueOf(p.charDBID));
            if (n != null) {
                moNhom(p, n);
            }
        }
    }

    // ------------------------------------------------------------------ chi phi / thuong

    /** "xu:10;item:1039x3" -> list {loai, id, so}. loai: xu / luong_khoa / item / thuoc / ngoc. */
    public static List<long[]> docChiPhi(final String s) {
        final List<long[]> ra = new ArrayList<long[]>();
        for (final String phan : s.split(";")) {
            final int i = phan.indexOf(':');
            if (i <= 0) {
                continue;
            }
            final String k = phan.substring(0, i).trim();
            final String v = phan.substring(i + 1).trim();
            final int x = v.indexOf('x');
            final long id = x > 0 ? Long.parseLong(v.substring(0, x)) : 0L;
            final long so = x > 0 ? Long.parseLong(v.substring(x + 1)) : Long.parseLong(v);
            final int loai = "xu".equals(k) ? 0 : "luong_khoa".equals(k) ? 1 : "item".equals(k) ? 2 : "thuoc".equals(k) ? 3
                    : "ngoc".equals(k) ? 4 : -1;
            if (loai >= 0 && so > 0) {
                ra.add(new long[]{loai, id, so});
            }
        }
        return ra;
    }

    static String moTaChiPhi(final String s) {
        return moTaChiPhi(docChiPhi(s));
    }

    static String moTaChiPhi(final List<long[]> chi) {
        return moTaChiPhi(chi, null);
    }

    /** @param bangLuong null = "lượng khoá (hoặc lượng)"; true / false = đã trừ bằng lượng / lượng khoá. */
    static String moTaChiPhi(final List<long[]> chi, final Boolean bangLuong) {
        final StringBuilder b = new StringBuilder();
        for (final long[] c : chi) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(c[2]).append(' ');
            switch ((int) c[0]) {
                case 0:
                    b.append("xu");
                    break;
                case 1:
                    // thieu luong khoa thi tru luong cung so (chu game 31/10/2026)
                    if (bangLuong == null) {
                        b.append("lượng khoá (hoặc lượng)");
                    } else {
                        b.append(bangLuong.booleanValue() ? "lượng" : "lượng khoá");
                    }
                    break;
                case 2:
                    final ItemTemplates t = Map.itemTemplates.get(5).get(Integer.valueOf((int) c[1]));
                    b.append(t != null ? t.name : "?");
                    break;
                case 3:
                    b.append(c[1] >= 0 && c[1] < Map.potionTemplates2.size() ? Map.potionTemplates2.get((int) c[1]).name : "?");
                    break;
                default:
                    b.append(c[1] >= 0 && c[1] < Map.gemTemplate.length ? Map.gemTemplate[(int) c[1]].name : "?");
                    break;
            }
        }
        return com.htbb.pokemon.ChuPokemon.doi(b.toString());
    }

    /** "ITEM:1224:1,GEM:249:1" -> list {loai(0 item,1 gem,2 thuoc), id, so}. */
    public static List<int[]> docThuong(final String s) {
        final List<int[]> ra = new ArrayList<int[]>();
        for (final String phan : s.split(",")) {
            final String[] a = phan.trim().split(":");
            if (a.length < 3) {
                continue;
            }
            final int loai = "ITEM".equals(a[0]) ? 0 : "GEM".equals(a[0]) ? 1 : "PT".equals(a[0]) ? 2 : -1;
            if (loai >= 0) {
                ra.add(new int[]{loai, Integer.parseInt(a[1]), Math.max(1, Integer.parseInt(a[2]))});
            }
        }
        return ra;
    }

    static int demNgoc(final Char p, final int id) {
        return id >= 0 && id < p.listGemitem.length ? p.listGemitem[id] + p.listGemitemLock[id] : 0;
    }

    /** Mua / đổi 1 lần. @return câu trả lời cho người chơi. */
    static String mua(final Char p, final Object[] r) {
        return mua(p, r, 1);
    }

    /** Câu trả lời của {@link #mua} khi đã trừ và phát đồ. */
    public static boolean thanhCong(final String kq) {
        return kq != null && kq.startsWith("Đã nhận");
    }

    /**
     * Mua / đổi {@code soLan} lần trong MỘT lần trừ (chi phí và thưởng nhân {@code soLan}): đủ hết hoặc không trừ gì.
     * Dùng chung cho Kecleon / Machamp "Mua Bùa chuyển luyện" (cùng luật lượng khoá trước, thiếu thì lượng, đồ mua
     * bằng lượng không khoá). Dòng có giới hạn tuần chỉ đổi từng lần.
     */
    public static String mua(final Char p, final Object[] r, final int soLan) {
        if (p == null || r == null || soLan < 1 || soLan > 999) {
            return "Số lượng không hợp lệ.";
        }
        if (!hien(p, r)) {
            return "Chưa mở.";
        }
        if (p.lvDetail.lv < ((Integer) r[5]).intValue()) {
            return "Cần đạt cấp " + r[5] + ".";
        }
        final List<long[]> chi = docChiPhi((String) r[3]);
        final List<int[]> thuong = docThuong((String) r[4]);
        if (chi.isEmpty() || thuong.isEmpty()) {
            return "Dòng này đang lỗi dữ liệu.";
        }
        if (soLan > 1) {
            for (final long[] c : chi) {
                c[2] *= soLan;
            }
            for (final int[] t : thuong) {
                t[2] *= soLan;
            }
        }
        final boolean coLuong = coLuongKhoa(chi);
        // o can khi do ra KHOA (tra luong khoa / vat pham) va khi do ra MO (tra bang luong): type 28 chi don vao chong
        // cung khoa, nen bua mo khong vao chong bua khoa -> co the can them o.
        int canKhoa = 0;
        int canMo = 0;
        for (final int[] t : thuong) {
            if (t[0] == 0) {
                // [chong-vat-pham] type 28 (createItemSoLuong) don vao chong san co: chi can o cho phan vuot chong
                final ItemTemplates tp = Map.itemTemplates.get(5).get(Integer.valueOf(t[1]));
                final boolean chong = tp != null && tp.type == 28;
                canKhoa += chong ? com.htbb.pokemon.ChongVatPham.soOCan(p, t[1], t[2]) : t[2];
                canMo += chong ? soOChongMo(p, t[1], t[2]) : t[2];
                continue;
            }
            int them = 0;
            if (t[0] == 1 && demNgoc(p, t[1]) == 0) {
                them = 1;
            } else if (t[0] == 2 && (t[1] < 0 || t[1] >= p.potions.length)) {
                return "Dòng này đang lỗi dữ liệu.";
            } else if (t[0] == 2 && p.potions[t[1]] == 0) {
                them = 1;
            }
            canKhoa += them;
            canMo += them;
        }
        final int trong = p.countSlotInventory();
        final boolean choKhoa = trong >= canKhoa;
        final boolean choMo = coLuong && trong >= canMo;
        if (!choKhoa && !choMo) {
            return "Hành trang cần " + canKhoa + " ô trống.";
        }
        final int gh = ((Integer) r[8]).intValue();
        final int idDong = ((Integer) r[0]).intValue();
        if (gh > 0 && soLan > 1) {
            return "Dòng này chỉ đổi từng lần.";
        }
        if (gh > 0 && !DatabaseLauDai.tangMuaTuan(p.charDBID, ID_TUAN + idDong, LauDaiNhanVat.tuanNay(), gh)) {
            return "Đã đổi đủ " + gh + " lần trong tuần này.";
        }
        // Gia luong khoa: tru luong khoa truoc; thieu thi tru LUONG cung so (luong nap mua duoc moi thu luong khoa mua
        // duoc), do mua bang luong KHONG khoa. Moi lan thu la MOT khoi Char.truNeuDu.
        boolean ok = choKhoa && p.truNeuDu(() -> du(p, chi, false), () -> tru(p, chi, false));
        boolean traLuong = false;
        if (!ok && choMo) {
            ok = p.truNeuDu(() -> du(p, chi, true), () -> tru(p, chi, true));
            traLuong = ok;
        }
        if (!ok) {
            if (gh > 0) {
                // luot tuan da dem o tren: khong tru duoc thi tra lai (truoc 31/10/2026 thieu tien van mat luot)
                DatabaseLauDai.hoanMuaTuan(p.charDBID, ID_TUAN + idDong, LauDaiNhanVat.tuanNay());
            }
            if (coLuong && !choMo && du(p, chi, true)) {
                // du luong nhung do mua bang luong (khong khoa) khong xep chung chong khoa: thieu o
                return "Hành trang cần " + canMo + " ô trống (đồ mua bằng lượng không khoá, không xếp chung chồng đồ khoá).";
            }
            return "Không đủ: " + moTaChiPhi(chi) + ".";
        }
        phat(p, thuong, !traLuong);
        try {
            Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "cua hang lau dai " + idDong + ": " + r[3] + " -> " + r[4]
                    + (soLan > 1 ? " x" + soLan : "") + (traLuong ? " (tra bang luong)" : ""), "cuahanglaudai");
        } catch (final Exception ignored) {
        }
        return "Đã nhận: " + (soLan > 1 ? soLan + " " : "") + r[2] + (soLan > 1 ? ", trả " + moTaChiPhi(chi, traLuong) : "")
                + (traLuong ? " (trừ lượng vì thiếu lượng khoá, đồ không khoá)." : ".");
    }

    /** O moi can cho {@code n} mon type 28 KHONG khoa (mua bang luong). */
    static int soOChongMo(final Char p, final int id, final int n) {
        final Item mau = Char.mauSoLuong(id, p);
        if (mau == null) {
            return n;
        }
        mau.lock = 0;
        return com.htbb.pokemon.ChongVatPham.soOMoi(p.iItems, mau, n);
    }

    /** Dong cua hang ban dung 1 vat pham {@code idItem} (thuong "ITEM:idItem:1") va dang hien cho {@code p}; null neu khong. */
    public static Object[] dongBan(final Char p, final int idItem) {
        if (!LauDai.bat()) {
            return null;
        }
        for (final Object[] r : BANG) {
            final List<int[]> t = docThuong((String) r[4]);
            if (t.size() == 1 && t.get(0)[0] == 0 && t.get(0)[1] == idItem && t.get(0)[2] == 1 && (p == null || hien(p, r))) {
                return r;
            }
        }
        return null;
    }

    /** Gia luong khoa / 1 lan cua dong (chi phi CHI co luong khoa); -1 neu dong co chi phi khac. */
    public static long giaLuongKhoa(final Object[] r) {
        final List<long[]> chi = docChiPhi((String) r[3]);
        if (chi.size() != 1 || chi.get(0)[0] != 1) {
            return -1L;
        }
        return chi.get(0)[2];
    }

    /** Chi phi co phan luong khoa (dong Kecleon). */
    static boolean coLuongKhoa(final List<long[]> chi) {
        for (final long[] c : chi) {
            if (c[0] == 1) {
                return true;
            }
        }
        return false;
    }

    /** @param bangLuong phan luong khoa tra bang LUONG (luong mo) cung so. */
    static boolean du(final Char p, final List<long[]> chi, final boolean bangLuong) {
        for (final long[] c : chi) {
            switch ((int) c[0]) {
                case 0:
                    if (p.getxu() < c[2]) {
                        return false;
                    }
                    break;
                case 1:
                    if ((bangLuong ? p.getLuong() : p.getLuongLock()) < c[2]) {
                        return false;
                    }
                    break;
                case 2:
                    if (VatPhamDungLauDai.demItem(p, (int) c[1]) < c[2]) {
                        return false;
                    }
                    break;
                case 3:
                    if (c[1] < 0 || c[1] >= p.potions.length || p.potions[(int) c[1]] < c[2]) {
                        return false;
                    }
                    break;
                default:
                    if (demNgoc(p, (int) c[1]) < c[2]) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    static void tru(final Char p, final List<long[]> chi, final boolean bangLuong) {
        for (final long[] c : chi) {
            final int id = (int) c[1];
            int n = (int) c[2];
            switch ((int) c[0]) {
                case 0:
                    p.subXu(c[2], false, "cua hang lau dai");
                    break;
                case 1:
                    if (bangLuong) {
                        p.subLuong(c[2]);
                    } else {
                        p.subLuongLock(c[2]);
                    }
                    break;
                case 2:
                    VatPhamDungLauDai.truItem(p, id, n);
                    break;
                case 3:
                    p.potions[id] -= n;
                    break;
                default:
                    final int khoa = Math.min(n, p.listGemitemLock[id]); // tieu ngoc khoa truoc
                    p.listGemitemLock[id] -= khoa;
                    p.listGemitem[id] -= n - khoa;
                    break;
            }
        }
    }

    /** @param khoa do ra khoa (tra bang luong khoa / vat pham) hay khong (tra bang luong). */
    static void phat(final Char p, final List<int[]> thuong, final boolean khoa) {
        for (final int[] t : thuong) {
            try {
                if (t[0] == 1) {
                    p.doAddGemItem(t[1], t[2], true);
                } else if (t[0] == 2) {
                    p.potions[t[1]] += t[2];
                } else {
                    final ItemTemplates tp = Map.itemTemplates.get(5).get(Integer.valueOf(t[1]));
                    if (tp == null) {
                        continue;
                    }
                    if (tp.type == 28) {
                        Char.createItemSoLuong(t[1], t[2], p, khoa);
                    } else {
                        for (int k = 0; k < t[2]; k++) {
                            final Item it = Map.dropItemByIdTemplate(p.inCountry, t[1]); // chi so theo template (nhu NhiemVuEngine)
                            if (it != null) {
                                it.id = p.getIDItem();
                                it.owner = p.charDBID;
                                it.lock = (byte) (khoa ? 1 : 0);
                                p.iItems.add(it);
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                System.out.println("CuaHangLauDai.phat: " + e);
            }
        }
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1)); // mon trang bi vua them: tui client lam moi ngay
        p.sendMessage(MessageCreator.createCharGemItem(p));
    }
}
