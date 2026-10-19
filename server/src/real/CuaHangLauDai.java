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
 * data_cua_hang_lau_dai (sinh bởi tools/pokemon/sinh_vat_pham_lau_dai.py). Mỗi dòng: chi phí
 * (xu:N / luong_khoa:N / item:IDxN / thuoc:IDxN / ngoc:IDxN, ngăn ';') -> thưởng (ITEM:id:n /
 * GEM:id:n / PT:id:n, ngăn ','). Thưởng KHÔNG BAO GIỜ là tiền tệ (sinh_vat_pham_lau_dai chặn).
 *
 * Vì sao có lớp này: luật "không vật phẩm mồ côi" (tools/pokemon/kiem_item_mo_coi.py,
 * du_lieu/KIEM_ITEM.md) - thuốc / quả mọng / đồ giữ / bùa mới (1201..) chưa có nguồn,
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
        final StringBuilder b = new StringBuilder();
        for (final long[] c : docChiPhi(s)) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(c[2]).append(' ');
            switch ((int) c[0]) {
                case 0:
                    b.append("xu");
                    break;
                case 1:
                    b.append("lượng khoá");
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
        int can = 0;
        for (final int[] t : thuong) {
            if (t[0] == 0) {
                can += t[2];
            } else if (t[0] == 1 && demNgoc(p, t[1]) == 0) {
                can++;
            } else if (t[0] == 2 && (t[1] < 0 || t[1] >= p.potions.length)) {
                return "Dòng này đang lỗi dữ liệu.";
            } else if (t[0] == 2 && p.potions[t[1]] == 0) {
                can++;
            }
        }
        if (p.countSlotInventory() < can) {
            return "Hành trang cần " + can + " ô trống.";
        }
        final int gh = ((Integer) r[8]).intValue();
        final int idDong = ((Integer) r[0]).intValue();
        if (gh > 0 && !DatabaseLauDai.tangMuaTuan(p.charDBID, ID_TUAN + idDong, LauDaiNhanVat.tuanNay(), gh)) {
            return "Đã đổi đủ " + gh + " lần trong tuần này.";
        }
        final boolean ok = p.truNeuDu(() -> du(p, chi), () -> tru(p, chi));
        if (!ok) {
            return "Không đủ: " + moTaChiPhi((String) r[3]) + ".";
        }
        phat(p, thuong);
        try {
            Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "cua hang lau dai " + idDong + ": " + r[3] + " -> " + r[4],
                    "cuahanglaudai");
        } catch (final Exception ignored) {
        }
        return "Đã nhận: " + r[2] + ".";
    }

    static boolean du(final Char p, final List<long[]> chi) {
        for (final long[] c : chi) {
            switch ((int) c[0]) {
                case 0:
                    if (p.getxu() < c[2]) {
                        return false;
                    }
                    break;
                case 1:
                    if (p.getLuongLock() < c[2]) {
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

    static void tru(final Char p, final List<long[]> chi) {
        for (final long[] c : chi) {
            final int id = (int) c[1];
            int n = (int) c[2];
            switch ((int) c[0]) {
                case 0:
                    p.subXu(c[2], false, "cua hang lau dai");
                    break;
                case 1:
                    p.subLuongLock(c[2]);
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

    static void phat(final Char p, final List<int[]> thuong) {
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
                        Char.createItemSoLuong(t[1], t[2], p);
                    } else {
                        for (int k = 0; k < t[2]; k++) {
                            final Item it = Map.dropItemByIdTemplate(p.inCountry, t[1]); // chi so theo template (nhu NhiemVuEngine)
                            if (it != null) {
                                it.id = p.getIDItem();
                                it.owner = p.charDBID;
                                it.lock = 1;
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
