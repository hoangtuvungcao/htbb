import com.htbb.pokemon.ChiSoTrangBi;
import com.htbb.pokemon.DanhHieuLogic;
import com.htbb.pokemon.DoMoiLogic;
import com.htbb.pokemon.GioiHanChiSo;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.IconDoMoi;
import com.htbb.pokemon.IconPokemon;
import com.htbb.pokemon.LichNam;
import com.htbb.pokemon.NangCapLauDai;
import com.htbb.pokemon.NapTienLogic;
import com.htbb.pokemon.NgoaiHinh;
import com.htbb.pokemon.TrangPhucLogic;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * DO HO MOI + TU DO TRANG TRI (0.9.8, docs/thiet-ke-game.md "Đồ họ mới & trang phục"): du lieu sql/them_do_trang_phuc.sql
 * (sinh boi tools/do_trang_phuc/sinh.py) + lop thuan DoMoiLogic / TrangPhucLogic / NgoaiHinh (duoi -102) + moc game.
 * Kiem: icon du, chi so ban sao mon goc (=&gt; tran mot mon giu nguyen), tran thuong bo ho, ngan sach trang tri 5%,
 * nguon khong tra tien cho moi o, cua hang luong chi hinh, roi thay 1:1 dung ti le, goi -102 / -116.
 */
public final class KiemDoTrangPhuc {

    private KiemDoTrangPhuc() {
    }

    static String doc(final String p) {
        try {
            return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    /** Tach cac bo gia tri "(a,'b',NULL),(...)" (chuoi '' hoac \' thoat). */
    static List<List<Object>> bo(final String s) {
        final List<List<Object>> ra = new ArrayList<List<Object>>();
        int i = 0;
        final int n = s.length();
        while (i < n) {
            if (s.charAt(i) != '(') {
                i++;
                continue;
            }
            i++;
            final List<Object> b = new ArrayList<Object>();
            while (i < n) {
                char c = s.charAt(i);
                if (c == '\'') {
                    final StringBuilder g = new StringBuilder();
                    i++;
                    while (i < n) {
                        c = s.charAt(i);
                        if (c == '\\') {
                            g.append(s.charAt(i + 1));
                            i += 2;
                            continue;
                        }
                        if (c == '\'') {
                            if (i + 1 < n && s.charAt(i + 1) == '\'') {
                                g.append('\'');
                                i += 2;
                                continue;
                            }
                            i++;
                            break;
                        }
                        g.append(c);
                        i++;
                    }
                    b.add(g.toString());
                } else {
                    int j = i;
                    while (s.charAt(j) != ',' && s.charAt(j) != ')') {
                        j++;
                    }
                    final String v = s.substring(i, j).trim();
                    b.add("NULL".equals(v) ? null : Long.valueOf(Long.parseLong(v)));
                    i = j;
                }
                if (s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                i++;
                break;
            }
            ra.add(b);
        }
        return ra;
    }

    /** Dong cua "INSERT INTO `bang` (...) VALUES ... ON DUPLICATE" trong sql. */
    static List<List<Object>> bang(final String sql, final String ten) {
        final String dau = "INSERT INTO `" + ten + "` (";
        final int i = sql.indexOf(dau);
        if (i < 0) {
            return new ArrayList<List<Object>>();
        }
        final int v = sql.indexOf("VALUES\n", i);
        final int e = sql.indexOf("\nON DUPLICATE", v);
        return bo(sql.substring(v + 7, e));
    }

    static long so(final Object o) {
        return o == null ? 0L : ((Long) o).longValue();
    }

    static String chu(final Object o) {
        return o == null ? "" : o.toString();
    }

    public static void chay() {
        Kiem.nhom("Đồ họ mới + tủ đồ trang trí (sql/them_do_trang_phuc.sql, DoMoiLogic, TrangPhucLogic)");
        final String sql = doc("../sql/them_do_trang_phuc.sql");
        Kiem.dung("có sql/them_do_trang_phuc.sql", !sql.isEmpty());
        Kiem.sai("sql không dùng INSERT ... SELECT ... ON DUPLICATE (MariaDB 10.3)", sql.contains("INSERT INTO") && sql.matches("(?s).*SELECT[^;]*ON DUPLICATE.*"));
        Kiem.dung("sql đặt SET NAMES utf8", sql.contains("SET NAMES utf8;"));

        // ------------------------------------------------------------ data_item moi: icon + ban sao mon goc
        final List<List<Object>> item = bang(sql, "data_item");
        final List<List<Object>> moi = bang(sql, "data_trang_bi_moi");
        Kiem.bang("628 món họ trong data_item", Integer.valueOf(628), Integer.valueOf(item.size()));
        Kiem.bang("628 dòng data_trang_bi_moi", Integer.valueOf(628), Integer.valueOf(moi.size()));
        final Map<Long, List<Object>> goc = new HashMap<Long, List<Object>>();
        final String dump = doc("../sql/htbb_day_du.sql");
        final int di = dump.indexOf("INSERT INTO `data_item` VALUES ");
        if (di >= 0) {
            for (final List<Object> r : bo(dump.substring(di + 31, dump.indexOf(";\n", di)))) {
                goc.put(Long.valueOf(so(r.get(0))), r);
            }
        }
        Kiem.dung("đọc data_item gốc từ htbb_day_du.sql", goc.size() > 1000);
        int thieuIcon = 0;
        int lechChiSo = 0;
        int ngoaiDai = 0;
        int trungGoc = 0;
        final Map<Long, List<Object>> theoId = new HashMap<Long, List<Object>>();
        for (final List<Object> r : item) {
            theoId.put(Long.valueOf(so(r.get(0))), r);
            final long id = so(r.get(0));
            if (id < 1400 || id > 2099) {
                ngoaiDai++;
            }
            if (goc.containsKey(Long.valueOf(id))) {
                trungGoc++;
            }
            final long x = so(r.get(20));
            if (!IconDoMoi.thuocDai((int) x) || !new File(IconDoMoi.THU_MUC + (x - IconDoMoi.ID_DAU) + ".png").isFile()) {
                thieuIcon++;
            }
        }
        Kiem.bang("món họ: id trong dải 1400..2099", Integer.valueOf(0), Integer.valueOf(ngoaiDai));
        Kiem.bang("món họ: không đè id data_item có sẵn", Integer.valueOf(0), Integer.valueOf(trungGoc));
        Kiem.bang("món họ: mọi icon (xstart 15000+n) có PNG trong map/icon_do_moi", Integer.valueOf(0), Integer.valueOf(thieuIcon));
        final Set<String> tenDaCo = new HashSet<String>();
        int trungTen = 0;
        for (final List<Object> m : moi) {
            final List<Object> r = theoId.get(Long.valueOf(so(m.get(0))));
            final List<Object> g = goc.get(Long.valueOf(so(m.get(4))));
            if (r == null || g == null || r.size() != g.size()) {
                lechChiSo++;
                continue;
            }
            // cot: 0 id, 1 name, 20 xstart, 25 namein khac; moi cot khac (type, level, durable, atb0..9, price, clazz...) = goc
            for (int c = 0; c < r.size(); c++) {
                if (c == 0 || c == 1 || c == 20 || c == 25) {
                    continue;
                }
                final Object a = r.get(c);
                final Object b = g.get(c);
                if (a == null ? b != null : !a.equals(b)) {
                    lechChiSo++;
                    break;
                }
            }
            if (so(m.get(2)) != so(r.get(2)) || NangCapLauDai.bacTheoCap((int) so(r.get(6))) != so(m.get(3))) {
                lechChiSo++;
            }
            if (!tenDaCo.add(chu(r.get(1)))) {
                trungTen++;
            }
        }
        Kiem.bang("món họ = bản sao chỉ số món gốc cùng ô + bậc (trần một món NganSachTrangBi giữ nguyên)", Integer.valueOf(0),
                Integer.valueOf(lechChiSo));
        Kiem.bang("tên món họ không trùng", Integer.valueOf(0), Integer.valueOf(trungTen));

        // ------------------------------------------------------------ bo ho: du lieu + tran
        final DoMoiLogic.DuLieu d = new DoMoiLogic.DuLieu();
        for (final List<Object> r : bang(sql, "data_trang_bi_ho")) {
            d.themHo(new DoMoiLogic.Ho(chu(r.get(0)), chu(r.get(1)), "vung".equals(chu(r.get(2))) ? DoMoiLogic.LOAI_VUNG
                    : DoMoiLogic.LOAI_HE, (int) so(r.get(3)), (int) so(r.get(4)), chu(r.get(5))));
        }
        int csLa = 0;
        for (final List<Object> r : bang(sql, "data_trang_bi_ho_thuong")) {
            final int cs = DoMoiLogic.maCs(chu(r.get(2)));
            if (cs < 0) {
                csLa++;
            }
            d.themThuong(chu(r.get(0)), new DoMoiLogic.Thuong((int) so(r.get(1)), cs, (int) so(r.get(3)), chu(r.get(4))));
        }
        for (final List<Object> r : moi) {
            d.themMon((int) so(r.get(0)), chu(r.get(1)), (int) so(r.get(2)), (int) so(r.get(3)), (int) so(r.get(4)), chu(r.get(5)));
        }
        Kiem.bang("21 bộ (18 hệ + 3 vùng)", Integer.valueOf(21), Integer.valueOf(d.ho.size()));
        Kiem.bang("chỉ số thưởng bộ đều hợp lệ", Integer.valueOf(0), Integer.valueOf(csLa));
        Kiem.bang("mọi món họ thuộc một bộ có trong bảng", Integer.valueOf(628), Integer.valueOf(d.mon.size()));
        int heDu = 0;
        for (final DoMoiLogic.Ho h : d.ho.values()) {
            if (h.loai == DoMoiLogic.LOAI_HE && HePokemon.hopLe(h.he) && HePokemon.hopLe(h.heKhac)) {
                heDu++;
                // he khac phai danh SIEU HIEU QUA vao he cua bo
                Kiem.dung(h.ten + ": hệ khắc " + HePokemon.TEN[h.heKhac] + " đánh siêu hiệu quả vào " + HePokemon.TEN[h.he],
                        HePokemon.heSoDon(h.heKhac, h.he) == 200);
            }
        }
        Kiem.bang("18 bộ hệ đủ hệ / hệ khắc", Integer.valueOf(18), Integer.valueOf(heDu));
        // mac het mon cua tung bo (moi bac): kep tran ho
        int vuot = 0;
        int maxSt = 0;
        int maxKh = 0;
        for (final DoMoiLogic.Ho h : d.ho.values()) {
            final List<Integer> ids = new ArrayList<Integer>();
            for (final DoMoiLogic.Mon m : d.mon.values()) {
                if (m.ho == h) {
                    ids.add(Integer.valueOf(m.id));
                }
            }
            final int[] a = new int[ids.size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = ids.get(i).intValue();
            }
            final DoMoiLogic.KetQua k = DoMoiLogic.tinh(d, a);
            for (int he = 0; he < HePokemon.SO_HE; he++) {
                maxSt = Math.max(maxSt, k.stHe[he]);
                maxKh = Math.max(maxKh, k.khangHe[he]);
                if (k.stHe[he] > DoMoiLogic.TRAN_ST_HE || k.khangHe[he] > DoMoiLogic.TRAN_KHANG) {
                    vuot++;
                }
            }
            if (k.hp > DoMoiLogic.TRAN_VUNG || k.tanCong > DoMoiLogic.TRAN_VUNG || k.chiMang > DoMoiLogic.TRAN_VUNG) {
                vuot++;
            }
        }
        Kiem.bang("thưởng bộ họ trong trần (ST hệ ≤ 5%, kháng ≤ 3%, bộ vùng ≤ 1% mỗi chỉ số)", Integer.valueOf(0), Integer.valueOf(vuot));
        Kiem.bang("đủ 6 món bộ hệ: +5% sát thương hệ", Integer.valueOf(50), Integer.valueOf(maxSt));
        Kiem.bang("4 món bộ hệ: +3% kháng hệ khắc", Integer.valueOf(30), Integer.valueOf(maxKh));
        Kiem.dung("ST hệ bộ họ ≤ trần chung đồ hệ 20%", DoMoiLogic.TRAN_ST_HE <= ChiSoTrangBi.TRAN_DO_HE_PHAN_TRAM * 10);
        Kiem.dung("bộ vùng + bộ bậc chung trần bộ 5% (GioiHanChiSo.thuongBo)", GioiHanChiSo.thuongBo(5 + DoMoiLogic.TRAN_VUNG / 10) <= 5);
        // 6 o bo he: 2 bo 2 mon + 1 bo 2 mon (3 he khac nhau) - moi he toi da 2%, khong bo nao vuot
        final int[] tron = {1400 + 1 * 30, 1400 + 1 * 30 + 5, 1400 + 2 * 30 + 10, 1400 + 2 * 30 + 15, 1400 + 4 * 30 + 20, 1400 + 4 * 30 + 25};
        final DoMoiLogic.KetQua kt = DoMoiLogic.tinh(d, tron);
        Kiem.bang("trộn 3 bộ hệ x 2 món: Lửa +2%", Integer.valueOf(20), Integer.valueOf(kt.stHe[1]));
        Kiem.bang("trộn 3 bộ hệ x 2 món: không có kháng (chưa đủ 4)", Integer.valueOf(0), Integer.valueOf(kt.khangHe[2]));

        // ------------------------------------------------------------ roi: thay 1:1, dung ti le, giu bac / o
        final Random r = new Random(9802);
        final int[] oHe = DoMoiLogic.O_BO_HE;
        int luot = 0;
        int doiHe = 0;
        int duHe = 0;
        int doiVung = 0;
        int duVung = 0;
        int sai = 0;
        int doiKhongBo = 0;
        for (int i = 0; i < 200000; i++) {
            final int bac = r.nextInt(11);
            final boolean heO = r.nextBoolean();
            final int type = heO ? oHe[r.nextInt(oHe.length)] : DoMoiLogic.O_BO_VUNG[r.nextInt(DoMoiLogic.O_BO_VUNG.length)];
            final int he = r.nextInt(18);
            final int id = DoMoiLogic.bienThe(d, type, bac, he, false, r.nextInt(100));
            luot++;
            final boolean coBo = heO ? java.util.Arrays.binarySearch(DoMoiLogic.BAC_BO_HE, bac) >= 0 : true;
            if (heO && coBo) {
                duHe++;
            }
            if (!heO) {
                duVung++;
            }
            if (id > 0) {
                final DoMoiLogic.Mon m = d.mon(id);
                if (m == null || m.type != type || m.bac != bac || (heO && m.ho.he != he)) {
                    sai++;
                }
                if (!coBo) {
                    doiKhongBo++;
                }
                if (heO) {
                    doiHe++;
                } else {
                    doiVung++;
                }
            }
        }
        Kiem.bang("roi thay 1:1: món đổi đúng ô / bậc / hệ của quái", Integer.valueOf(0), Integer.valueOf(sai));
        Kiem.bang("không đổi ở bậc không có bộ hệ", Integer.valueOf(0), Integer.valueOf(doiKhongBo));
        Kiem.dung("ti le doi bo he ~40% (" + doiHe * 1000L / Math.max(1, duHe) + "‰)", Math.abs(doiHe * 1000L / Math.max(1, duHe) - 400) <= 15);
        Kiem.dung("ti le doi bo vung ~35% (" + doiVung * 1000L / Math.max(1, duVung) + "‰)", Math.abs(doiVung * 1000L / Math.max(1, duVung) - 350) <= 15);
        Kiem.bang("so mon sau roi = so mon truoc (moi luot dung 1 mon)", Integer.valueOf(200000), Integer.valueOf(luot));
        Kiem.bang("quái không rõ hệ: giữ món gốc", Integer.valueOf(-1), Integer.valueOf(DoMoiLogic.bienThe(d, 0, 2, -1, false, 0)));
        Kiem.dung("boss đổi 60%", DoMoiLogic.bienThe(d, 0, 2, 1, true, 59) > 0 && DoMoiLogic.bienThe(d, 0, 2, 1, true, 60) < 0);
        final String game = doc("src/real/DoMoiGame.java");
        Kiem.sai("DoMoiGame không đụng độ hiếm / atb (giữ nguyên chỉ số món rơi)", game.contains("colorName") || game.contains(".atb["));

        // ------------------------------------------------------------ trang tri: ngan sach
        int tong = DanhHieuLogic.TRAN_MAC_DINH;
        for (final int t : TrangPhucLogic.TRAN_O) {
            tong += t;
        }
        Kiem.bang("ngân sách trang trí: 15 + 10 (danh hiệu) + 10 + 5 + 10 = 50‰", Integer.valueOf(50), Integer.valueOf(tong));
        Kiem.bang("= trần tổng trang_tri.tran_tong_phan_nghin", Integer.valueOf(DanhHieuLogic.TRAN_TRANG_TRI_MAC_DINH), Integer.valueOf(tong));
        final boolean[] het = {true, true, true, true};
        final int[] hl = TrangPhucLogic.hieuLuc(new int[]{15, 10, 5, 10}, het, 10, 50);
        Kiem.bang("mặc đủ 4 ô + danh hiệu 1%", "[15, 10, 5, 10]", java.util.Arrays.toString(hl));
        final int[] kep = TrangPhucLogic.hieuLuc(new int[]{999, 999, 999, 999}, het, 0, 50);
        Kiem.bang("sửa DB vượt trần ô bị kẹp về 15/10/5/10", "[15, 10, 5, 10]", java.util.Arrays.toString(kep));
        final int[] cat = TrangPhucLogic.hieuLuc(new int[]{15, 10, 5, 10}, het, 30, 50);
        int sum = 30;
        for (final int x : cat) {
            sum += x;
        }
        Kiem.bang("danh hiệu lớn (giả sử 3%): trang trí bị cắt, tổng ≤ 5%", Integer.valueOf(50), Integer.valueOf(sum));
        Kiem.bang("tran tong tu DB > 50 van kep 50", Integer.valueOf(40),
                Integer.valueOf(sumOf(TrangPhucLogic.hieuLuc(new int[]{15, 10, 5, 10}, het, 10, 500))));
        Kiem.bang("không mặc gì: 0", Integer.valueOf(0), Integer.valueOf(sumOf(TrangPhucLogic.hieuLuc(null, new boolean[4], 0, 50))));
        Kiem.bang("dòng HP từ 2 ô", Integer.valueOf(25), Integer.valueOf(TrangPhucLogic.phanNghin("hp",
                new String[]{"hp", "tan_cong", "phong_thu", "hp"}, new int[]{15, 10, 5, 10})));
        for (final List<Object> r0 : bang(sql, "data_trang_phuc_o")) {
            final int i = TrangPhucLogic.oIdx(chu(r0.get(0)));
            Kiem.dung("data_trang_phuc_o " + chu(r0.get(0)) + " = trần ô", i >= 0 && so(r0.get(2)) == TrangPhucLogic.TRAN_O[i]);
        }

        // ------------------------------------------------------------ trang tri: nguon, khong tra tien de thang
        final List<List<Object>> tp = bang(sql, "data_trang_phuc");
        Kiem.dung("có ≥ 50 món trang trí", tp.size() >= 50);
        final String dh = doc("../sql/them_danh_hieu.sql");
        final boolean[] mienPhi = new boolean[4];
        final boolean[] khongTien = new boolean[4];
        final boolean[] coTien = new boolean[4];
        int loiNguon = 0;
        int thieuIconTt = 0;
        final Set<String> maTien = new HashSet<String>();
        final Set<String> soO = new HashSet<String>();
        for (final List<Object> t : tp) {
            final String ma = chu(t.get(0));
            final int o = TrangPhucLogic.oIdx(chu(t.get(1)));
            if (o < 0 || !soO.add(o + ":" + so(t.get(2))) || so(t.get(2)) <= 0 || so(t.get(2)) > 255) {
                loiNguon++;
                continue;
            }
            final long ic = so(t.get(5));
            if (!IconDoMoi.thuocDai((int) ic) || !new File(IconDoMoi.THU_MUC + (ic - IconDoMoi.ID_DAU) + ".png").isFile()) {
                thieuIconTt++;
            }
            final TrangPhucLogic.DieuKien k = TrangPhucLogic.doc(chu(t.get(7)), chu(t.get(8)));
            switch (k.loai) {
                case TrangPhucLogic.MIEN_PHI:
                    if (k.cap <= 0) {
                        loiNguon++;
                    }
                    if (k.cap <= 20) {
                        mienPhi[o] = true;
                    }
                    khongTien[o] = true;
                    break;
                case TrangPhucLogic.SU_KIEN:
                    if (k.lich.isEmpty() || k.quai <= 0) {
                        loiNguon++;
                    }
                    for (final String l : k.lich) {
                        if (LichNam.muc(l) == null) {
                            loiNguon++;
                            System.out.println("        lich la: " + l);
                        }
                    }
                    khongTien[o] = true;
                    break;
                case TrangPhucLogic.THANH_TUU:
                    if (k.danhHieu.isEmpty() || !dh.contains("'" + k.danhHieu + "'")) {
                        loiNguon++;
                        System.out.println("        danh hieu la: " + k.danhHieu);
                    }
                    khongTien[o] = true;
                    break;
                case TrangPhucLogic.CHE_TAO:
                    if (k.nguyenLieu.isEmpty() || k.gio <= 0) {
                        loiNguon++;
                    }
                    for (final int[] x : k.nguyenLieu) {
                        if (java.util.Arrays.binarySearch(TrangPhucLogic.ID_NGUYEN_LIEU, x[0]) < 0) {
                            loiNguon++;
                        }
                    }
                    khongTien[o] = true;
                    break;
                case TrangPhucLogic.CUA_HANG:
                    if (so(t.get(9)) <= 0) {
                        loiNguon++;
                    }
                    coTien[o] = true;
                    maTien.add(ma);
                    break;
                default:
                    loiNguon++;
                    break;
            }
        }
        Kiem.bang("nguồn trang trí hợp lệ (cấp / lịch LichNam / danh hiệu có thật / nguyên liệu 1034..1037)", Integer.valueOf(0),
                Integer.valueOf(loiNguon));
        Kiem.bang("mọi icon trang trí có PNG", Integer.valueOf(0), Integer.valueOf(thieuIconTt));
        for (int o = 0; o < 4; o++) {
            Kiem.dung(TrangPhucLogic.TEN_O[o] + ": có món miễn phí ở cấp ≤ 20", mienPhi[o]);
            Kiem.dung(TrangPhucLogic.TEN_O[o] + ": ô có món bán lượng thì có món không trả tiền", !coTien[o] || khongTien[o]);
        }
        // cua hang luong (nap_cua_hang): chi hinh, khong giao dich, qua kiemMon, gia khop
        int loiCh = 0;
        final Set<String> coCh = new HashSet<String>();
        for (final List<Object> c : bang(sql, "nap_cua_hang")) {
            final NapTienLogic.Mon m = new NapTienLogic.Mon(chu(c.get(0)), chu(c.get(1)), chu(c.get(2)), chu(c.get(3)),
                    (int) so(c.get(4)), (int) so(c.get(5)), (int) so(c.get(6)), (int) so(c.get(7)), so(c.get(8)) == 1, so(c.get(9)) != 0);
            final String loi = NapTienLogic.kiemMon(m);
            if (loi != null || m.giaoDichDuoc || !NapTienLogic.TRAO_TRANG_TRI.equals(m.trao) || !maTien.contains(m.ma)) {
                loiCh++;
                System.out.println("        nap_cua_hang " + m.ma + ": " + loi);
            }
            coCh.add(m.ma);
        }
        Kiem.bang("cửa hàng lượng: món trang trí hợp lệ (kiemMon, không giao dịch, không chỉ số riêng)", Integer.valueOf(0),
                Integer.valueOf(loiCh));
        Kiem.dung("mọi món nguồn cửa hàng có dòng nap_cua_hang", coCh.containsAll(maTien));

        // ------------------------------------------------------------ goi -102 duoi trang tri
        try {
            final NgoaiHinh.Du du = NgoaiHinh.tinh(new int[]{2, 0}, new int[]{30, 30}, new int[]{0, 3}, new int[]{0, 7}, 0, 0, 0, 0, 0);
            final long k0 = du.khoa();
            du.tpSo = 11;
            du.hqSo = 3;
            du.skinSo = 2;
            du.theSo = 5;
            Kiem.dung("-102: khoá đổi khi đổi trang phục", du.khoa() != k0);
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            NgoaiHinh.ghi(new DataOutputStream(bo), 77, du);
            final ByteArrayOutputStream bo1 = new ByteArrayOutputStream();
            NgoaiHinh.ghiNoiDung(new DataOutputStream(bo1), du);
            final byte[] b = bo.toByteArray();
            Kiem.bang("-102 = charId + phần cũ + 6 byte đuôi", Integer.valueOf(2 + bo1.size() + 6), Integer.valueOf(b.length));
            // bo doc phien ban 1 (client cu): doc het phan cu, phan con lai la duoi -> bo qua duoc
            final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
            in.readShort();
            in.readUnsignedByte();
            final int co = in.readUnsignedByte();
            for (int o = 0; o < NgoaiHinh.SO_O; o++) {
                if ((co & (1 << o)) != 0) {
                    in.readUnsignedByte();
                    in.readUnsignedByte();
                    in.readUnsignedByte();
                }
            }
            for (int i = 0; i < 5; i++) {
                in.readUnsignedByte();
            }
            Kiem.bang("bộ đọc phiên bản 1 dừng đúng trước đuôi", Integer.valueOf(6), Integer.valueOf(in.available()));
            Kiem.bang("đuôi: 'T' v1 11 3 2 5", "84 1 11 3 2 5", in.readUnsignedByte() + " " + in.readUnsignedByte() + " "
                    + in.readUnsignedByte() + " " + in.readUnsignedByte() + " " + in.readUnsignedByte() + " " + in.readUnsignedByte());
            du.an = NgoaiHinh.AN_TRANG_PHUC;
            final ByteArrayOutputStream bo2 = new ByteArrayOutputStream();
            NgoaiHinh.ghiDuoi(new DataOutputStream(bo2), du);
            final byte[] d2 = bo2.toByteArray();
            Kiem.bang("ẩn trang phục: đuôi gửi 0", "0 0 0 0", d2[2] + " " + d2[3] + " " + d2[4] + " " + d2[5]);
        } catch (final Exception e) {
            Kiem.dung("-102 không lỗi: " + e, false);
        }

        // ------------------------------------------------------------ goi -116
        try {
            final List<TrangPhucLogic.OTuDo> os = new ArrayList<TrangPhucLogic.OTuDo>();
            final TrangPhucLogic.OTuDo o0 = new TrangPhucLogic.OTuDo();
            o0.ma = "trang_phuc";
            o0.ten = "Trang phục";
            o0.hieuLuc = 15;
            o0.goc = 15;
            o0.dong = TrangPhucLogic.DONG_MAC_DINH[0];
            o0.dangMac = "tp_tan_thu";
            os.add(o0);
            final List<TrangPhucLogic.MonTuDo> ms = new ArrayList<TrangPhucLogic.MonTuDo>();
            final TrangPhucLogic.MonTuDo m0 = new TrangPhucLogic.MonTuDo();
            m0.ma = "tp_tan_thu";
            m0.ten = "Áo huấn luyện viên tân thủ";
            m0.icon = 15628;
            m0.trangThai = TrangPhucLogic.TT_DANG_MAC;
            ms.add(m0);
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            TrangPhucLogic.ghiTuDo(new DataOutputStream(bo), os, 25, 50, 10, ms);
            final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bo.toByteArray()));
            Kiem.bang("-116 kiểu 0: số ô", Integer.valueOf(1), Integer.valueOf(in.readUnsignedByte()));
            in.readUTF();
            Kiem.bang("-116: tên ô", "Trang phục", in.readUTF());
            in.readShort();
            in.readShort();
            final int nd = in.readUnsignedByte();
            for (int i = 0; i < nd; i++) {
                in.readUTF();
                in.readUTF();
            }
            in.readUnsignedByte();
            Kiem.bang("-116: đang mặc", "tp_tan_thu", in.readUTF());
            in.readInt();
            Kiem.bang("-116: tổng / trần / danh hiệu", "25 50 10", in.readShort() + " " + in.readShort() + " " + in.readShort());
            Kiem.bang("-116: số món", Integer.valueOf(1), Integer.valueOf(in.readShort()));
            Kiem.bang("-116: mã món", "tp_tan_thu", in.readUTF());
        } catch (final Exception e) {
            Kiem.dung("-116 không lỗi: " + e, false);
        }

        // ------------------------------------------------------------ nguon: doc tham so
        final TrangPhucLogic.DieuKien k1 = TrangPhucLogic.doc("su_kien", "lich:moi:valentine,quocte83;quai:300");
        Kiem.bang("đọc lich (mã có dấu ':')", "[moi:valentine, quocte83]", k1.lich.toString());
        Kiem.bang("đọc quai", Integer.valueOf(300), Integer.valueOf(k1.quai));
        final TrangPhucLogic.DieuKien k2 = TrangPhucLogic.doc("che_tao", "nl:1034x10,1035x4;gio:2");
        Kiem.bang("đọc nguyên liệu + giờ", "10 Bụi sao, 4 Mảnh sao + 2 giờ xu", TrangPhucLogic.chuCheTao(k2));
        Kiem.bang("xu chế = giờ x xu/giờ", Long.valueOf(2000L), Long.valueOf(TrangPhucLogic.xuCheTao(k2, 1000L)));
        Kiem.bang("đọc cap", Integer.valueOf(5), Integer.valueOf(TrangPhucLogic.doc("mien_phi", "cap:5").cap));
        Kiem.bang("đọc danh_hieu", "gym_8", TrangPhucLogic.doc("thanh_tuu", "danh_hieu:gym_8").danhHieu);
        Kiem.bang("tham số hỏng bị bỏ qua", Integer.valueOf(0), Integer.valueOf(TrangPhucLogic.doc("mien_phi", "cap:x;;nl:abc").cap));

        // ------------------------------------------------------------ dai icon + moc game
        Kiem.dung("dải icon 15000..15999 không chồng dải PMD", IconDoMoi.ID_DAU >= IconPokemon.ID_DAU + IconPokemon.SO_TOI_DA);
        Kiem.dung("-116 không trùng lệnh đã có", TrangPhucLogic.LENH == -116 && NgoaiHinh.LENH != -116);
        final String mon = doc("src/real/Monster.java");
        Kiem.bang("Monster: 2 móc thay món họ (quái + boss)", Integer.valueOf(2), Integer.valueOf(dem(mon, "DoMoiGame.thayKhiRoi(")));
        final String cs = doc("src/real/ChiSoTrangBiGame.java");
        Kiem.dung("ST hệ bộ họ đi qua doHePhanTram (trần 20%)", cs.contains("DoMoiGame.stHePhanNghin(p, he)"));
        Kiem.dung("kháng hệ bộ họ đi qua khangHe (trần 60%)", cs.contains("DoMoiGame.khangHePhanNghin(c, he)"));
        Kiem.dung("bộ vùng cộng chung trần bộ (BoNgocGame.capNhat)", doc("src/real/BoNgocGame.java").contains("Math.min(trCm, th[BoTrangBi.CHI_MANG] + ho.chiMang)"));
        Kiem.dung("chế tạo: vòng cổ / phiến đá đổi sang món họ", doc("src/real/CheTaoGame.java").contains("DoMoiGame.thayCheTao(it, t, he)"));
        final String ccs = doc("src/real/CharChiSo.java");
        Kiem.dung("HP trang trí trước kepPhanTram", ccs.indexOf("TrangPhucGame.phanNghin(self, \"hp\")") > 0
                && ccs.indexOf("TrangPhucGame.phanNghin(self, \"hp\")") < ccs.indexOf("ChiSoTrangBiGame.kepPhanTram(self.maxhp"));
        Kiem.dung("phòng thủ trang trí", ccs.contains("capPhongThuTrangPhuc(self)"));
        Kiem.dung("tấn công trang trí", doc("src/real/Char.java").contains("TrangPhucGame.phanNghin(this, \"tan_cong\")"));
        Kiem.dung("lệnh -116 được định tuyến", doc("src/real/MapXuLyLenh.java").contains("case -116:"));
        Kiem.dung("-116 chỉ gửi client >= 908", doc("src/real/TrangPhucGame.java").contains("BoNgocGame.coClient(p)"));
        Kiem.dung("thưởng nhiệm vụ trang_tri:<mã> trao món", doc("src/real/NhiemVuEngine.java").contains("TrangPhucGame.trao(p, v, \"nhiem_vu\")"));
        Kiem.dung("mua lượng qua NapTienGame.mua (sổ cái)", doc("src/com/htbb/pokemon/NapTienGame.java").contains("public static String muaTheoMa("));
        final String dm = doc("../tools/cauhinh/danh_muc.json");
        Kiem.dung("khoá do_moi.bat + trang_phuc.bat trong danh mục cấu hình", dm.contains("\"do_moi.bat\"") && dm.contains("\"trang_phuc.bat\""));
    }

    static int sumOf(final int[] a) {
        int s = 0;
        for (final int x : a) {
            s += x;
        }
        return s;
    }

    static int dem(final String s, final String t) {
        int n = 0;
        int i = 0;
        while ((i = s.indexOf(t, i)) >= 0) {
            n++;
            i += t.length();
        }
        return n;
    }
}
