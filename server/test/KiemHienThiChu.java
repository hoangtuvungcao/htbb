import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ra soat hien thi (23/10/2026, chu game: "rà soát all hiển thị"): chu nguoi choi thay KHONG lot loi kieu
 * da gap that trong game, va chu mo ta nhiem vu KHOP du lieu that.
 *
 * Chuoi server (src/, bo lop phuc hoi P3, trang quan tri, dong log):
 *  - khong con dau vet doi ten ma nguon trong chu ("Thợ Map.rèn", "Map.rương" - thay chu tu dong an nham ten lop);
 *  - khong tu tieng Viet viet THIEU DAU trong cau co dau ("tru 500 luong", "thơi gian");
 *  - khong "level" (chu game = "cấp"), khong ma loi noi bo ("Lỗi 41", "Mã lỗi 123") hien cho nguoi choi.
 * Du lieu SQL (sql/htbb.sql phan A):
 *  - chu nhiem vu chi dung o cho {da} / {can} (NhiemVuMau.hoiThoai thay dung hai o nay);
 *  - "Thưởng: ..." (dong_thuong, trang quan tri / cam nang) = exp / xu / luong khoa that cua data_quest_lau_dai;
 *  - loi "dang lam" {da}/N cua nhiem vu So tay: N = so_luong that;
 *  - dieu kien tien hoa (Pokedex web) khong con ma PokeAPI tieng Anh ("biết chiêu ancient-power", "(level-up)").
 * Hanh vi: loi "dang lam" luon kem "Mục tiêu:" (loi NPC thoi khong noi phai lam gi o dau).
 */
public final class KiemHienThiChu {

    private static final Pattern LOG = Pattern.compile(
            "System\\.(out|err)|printStackTrace|saveOrtherLog|saveLogThongKe|Logger2?\\.|LOGGER|logger\\.|throw new|Exception\\(");
    private static final Pattern DAU = Pattern.compile("[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ]",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    /** Ten lop + "." + mot tu tieng Viet CO DAU: dau vet thay chu tu dong an vao ten lop ("Map.rương"). */
    static final Pattern TEN_LOP_TRONG_CHU = Pattern.compile("\\b(Map|Char|Item|Database|Util|Text|Constant)\\.[a-zđ]*"
            + "[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ]");
    /** Tu KHONG BAO GIO dung khi viet thieu dau (khong trung tu tieng Viet / tieng Anh khac). */
    static final Set<String> THIEU_DAU = new HashSet<String>(Arrays.asList(("khong duoc nguoi nhiem thuong luong tien diem chuc"
            + " nhap khau khoan phut giay truoc duong them het mot tren duoi viec nhieu muon chon thơi").split(" ")));
    static final Pattern MA_LOI = Pattern.compile("Lỗi \\d|Mã lỗi|\\blevel\\b", Pattern.CASE_INSENSITIVE);
    /** Tep chi quan tri / cong cu (khong den man hinh nguoi choi). */
    private static final String[] BO_TEP = {"/server/quantri/", "/server/Panel/", "/real/AdminMenu.java", "/real/KiemGoi",
        "/com/htbb/log/Nhat.java", "/com/htbb/pokemon/ChiSoTrangBi.java"};

    private KiemHienThiChu() {
    }

    public static void chay() {
        Kiem.nhom("Ra soat hien thi: chu nguoi choi thay + chu nhiem vu khop du lieu");
        // tu kiem bo bat
        Kiem.dung("bat duoc 'Thợ Map.rèn' / 'Map.rương'", loiChuoi("Hãy đến Thợ Map.rèn để sửa lại.") != null
                && loiChuoi("Nhận được Map.rương bạc") != null);
        Kiem.dung("bat duoc thieu dau trong cau co dau", loiChuoi("Xóa pet - tru 500 luong, còn lại") != null
                && loiChuoi("Bạn không thể vào Gym trong thơi gian này") != null);
        Kiem.dung("bat duoc level / ma loi", loiChuoi("Chỉ dùng được khi level dưới 90.") != null
                && loiChuoi("Có lỗi xảy ra. Lỗi 41") != null && loiChuoi("Có lỗi xảy ra. Mã lỗi 123") != null);
        Kiem.dung("khong bat nham cau dung (trong, vui, trang, Char.khamX2)", loiChuoi("Vui lòng mở trang trong túi.") == null
                && loiChuoi("Char.khamX2 (không nơi nào gọi)") == null && loiChuoi("Cà chua") == null);

        final List<String> loi = new ArrayList<String>();
        final int[] dem = {0};
        quet(new File("src"), loi, dem);
        for (final String l : loi) {
            System.out.println("        " + l);
        }
        Kiem.dung("da quet chuoi server (" + dem[0] + ")", dem[0] > 5000);
        Kiem.bang("chuoi server nguoi choi thay co loi hien thi", Integer.valueOf(0), Integer.valueOf(loi.size()));

        kiemNhiemVu();
        kiemTienHoa();
        kiemDangLam();
        final String qt = DayDu.doc("../web/shared/data/quy-tac-dang-ky.json");
        final Matcher m = Pattern.compile("\"quaNgay\": \"([^\"]*)\"").matcher(qt);
        Kiem.dung("thong bao vuot han muc 24 gio KHONG bao 'thử lại sau ít phút'", m.find() && !m.group(1).contains("ít phút")
                && m.group(1).contains("24 giờ"));
    }

    /** null = chuoi sach; nguoc lai ly do. */
    static String loiChuoi(final String s) {
        if (TEN_LOP_TRONG_CHU.matcher(s).find()) {
            return "ten lop trong chu";
        }
        if (!DAU.matcher(s).find()) {
            return null;
        }
        final Matcher w = Pattern.compile("[\\p{L}]+").matcher(s);
        while (w.find()) {
            if (THIEU_DAU.contains(w.group().toLowerCase())) {
                return "thieu dau: " + w.group();
            }
        }
        if (MA_LOI.matcher(s).find()) {
            return "ma loi / level";
        }
        return null;
    }

    private static void quet(final File d, final List<String> loi, final int[] dem) {
        final File[] con = d.listFiles();
        if (con == null) {
            return;
        }
        Arrays.sort(con);
        for (final File f : con) {
            if (f.isDirectory()) {
                quet(f, loi, dem);
                continue;
            }
            final String duong = f.getPath().replace('\\', '/');
            if (!duong.endsWith(".java") || boQua(duong)) {
                continue;
            }
            final String s = KiemThuatNgu.giaiEscape(DayDu.doc(duong));
            if (KiemThuatNgu.laLopPhucHoi(Arrays.asList(s.split("\n", 11)))) {
                continue;
            }
            chuoi(duong, s, loi, dem);
        }
    }

    private static boolean boQua(final String duong) {
        for (final String b : BO_TEP) {
            if (duong.contains(b)) {
                return true;
            }
        }
        return false;
    }

    /** Chuoi "..." ngoai chu thich, bo dong log. */
    private static void chuoi(final String tep, final String s, final List<String> loi, final int[] dem) {
        final String[] dong = s.split("\n", -1);
        int i = 0;
        int so = 1;
        final int n = s.length();
        while (i < n) {
            final char c = s.charAt(i);
            if (c == '\n') {
                so++;
                i++;
            } else if (s.startsWith("//", i)) {
                final int j = s.indexOf('\n', i);
                i = j < 0 ? n : j;
            } else if (s.startsWith("/*", i)) {
                final int j = s.indexOf("*/", i + 2);
                final int k = j < 0 ? n : j + 2;
                for (int x = i; x < k; x++) {
                    if (s.charAt(x) == '\n') {
                        so++;
                    }
                }
                i = k;
            } else if (c == '\'') {
                int j = i + 1;
                while (j < n && s.charAt(j) != '\'') {
                    j += s.charAt(j) == '\\' ? 2 : 1;
                }
                i = j + 1;
            } else if (c == '"') {
                final StringBuilder b = new StringBuilder();
                int j = i + 1;
                while (j < n && s.charAt(j) != '"' && s.charAt(j) != '\n') {
                    if (s.charAt(j) == '\\' && j + 1 < n) {
                        b.append(s.charAt(j + 1));
                        j += 2;
                    } else {
                        b.append(s.charAt(j));
                        j++;
                    }
                }
                i = j + 1;
                if (so - 1 < dong.length && LOG.matcher(dong[so - 1]).find()) {
                    continue;
                }
                dem[0]++;
                final String l = loiChuoi(b.toString());
                if (l != null) {
                    loi.add(tep + ":" + so + " [" + l + "] " + b);
                }
            } else {
                i++;
            }
        }
    }

    private static String nghin(final long v) {
        final String s = String.valueOf(v);
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return b.toString();
    }

    private static long so(final String s) {
        try {
            return Long.parseLong(s == null ? "" : s.trim());
        } catch (final NumberFormatException e) {
            return 0L;
        }
    }

    private static void kiemNhiemVu() {
        final Map<String, Map<String, String>> ld = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> r : DayDu.dongChu("data_quest_lau_dai")) {
            ld.put(r.get("ma"), r);
        }
        final List<Map<String, String>> nd = DayDu.dongChu("data_nhiem_vu_noi_dung");
        Kiem.dung("co noi dung nhiem vu (" + nd.size() + ") + dong nhiem vu (" + ld.size() + ")", nd.size() > 1500 && ld.size() > 1500);
        final Pattern oCho = Pattern.compile("\\{[A-Za-z_0-9]+\\}");
        final Pattern daN = Pattern.compile("\\{da\\}/(\\d+)");
        String sai0 = "", sai1 = "", sai2 = "";
        int nThuong = 0;
        int soSai1 = 0;
        for (final Map<String, String> r : nd) {
            final String ma = r.get("ma");
            for (final String c : new String[]{"mo_ta", "muc_tieu_text", "hoi_thoai_bat_dau", "hoi_thoai_dang_lam", "hoi_thoai_xong", "goi_y"}) {
                final Matcher m = oCho.matcher(r.get(c));
                while (m.find()) {
                    if (!"{da}".equals(m.group()) && !"{can}".equals(m.group()) && sai0.isEmpty()) {
                        sai0 = ma + "." + c + " " + m.group();
                    }
                }
            }
            final Map<String, String> q = ld.get(ma);
            if (q == null) {
                continue;
            }
            final String dt = r.get("dong_thuong");
            final long exp = so(q.get("exp"));
            final long xu = so(q.get("xu"));
            final long lk = so(q.get("luong_khoa"));
            if (exp > 0 || xu > 0 || lk > 0) {
                nThuong++;
                if ((exp > 0 && !dt.contains(nghin(exp) + " EXP")) || (xu > 0 && !dt.contains(nghin(xu) + " xu"))
                        || (lk > 0 && !dt.contains(lk + " lượng khoá"))) {
                    soSai1++;
                    if (sai1.isEmpty()) {
                        sai1 = ma + " [" + exp + " EXP, " + xu + " xu, " + lk + " lượng khoá] != " + dt;
                    }
                }
            }
            final boolean soTay = com.htbb.pokemon.NhiemVuMau.engineQuanLy(q.get("loai"))
                    || com.htbb.pokemon.NhiemVuMau.laChuoiNam2(q.get("loai"), (int) so(q.get("dong_co_tuong_thich")));
            final Matcher m = daN.matcher(r.get("hoi_thoai_dang_lam"));
            while (soTay && m.find()) {
                if (Long.parseLong(m.group(1)) != so(q.get("so_luong")) && sai2.isEmpty()) {
                    sai2 = ma + " so_luong=" + q.get("so_luong") + " != " + r.get("hoi_thoai_dang_lam");
                }
            }
        }
        Kiem.bang("chu nhiem vu chi dung o cho {da} / {can}", "", sai0);
        Kiem.dung("da so dong 'Thưởng' (" + nThuong + ")", nThuong > 1000);
        Kiem.bang("dong 'Thưởng' = exp / xu / lượng khoá that (" + soSai1 + " dong sai)", "", sai1);
        Kiem.bang("loi 'dang lam' {da}/N: N = so_luong that (nhiem vu So tay)", "", sai2);
    }

    private static void kiemTienHoa() {
        final Pattern ma = Pattern.compile("\\([a-z]+(-[a-z]+)+\\)|chiêu [a-z]+-[a-z]|\\(level-up\\)");
        String sai = "";
        int n = 0;
        for (final Map<String, String> r : DayDu.dongChu("data_loai_tien_hoa")) {
            n++;
            for (final String c : new String[]{"dieu_kien", "ten_vat_pham"}) {
                if (ma.matcher(r.get(c)).find() && sai.isEmpty()) {
                    sai = r.get("tu_dex") + ">" + r.get("sang_dex") + "." + c + " " + r.get(c);
                }
            }
        }
        final String b = DayDu.nangCap();
        final Matcher mb = Pattern.compile("'Cách gốc: [^']*'").matcher(b);
        while (mb.find()) {
            if (ma.matcher(mb.group()).find() && sai.isEmpty()) {
                sai = "phan B " + mb.group();
            }
        }
        Kiem.dung("da doc tien hoa (" + n + ")", n > 400);
        Kiem.bang("dieu kien tien hoa khong con ma PokeAPI tieng Anh", "", sai);
    }

    private static void kiemDangLam() {
        final com.htbb.pokemon.NhiemVuMau m = new com.htbb.pokemon.NhiemVuMau();
        m.soLuong = 5;
        m.mucTieuText = "Thu 5 Gói hàng (rơi từ Sandshrew)";
        m.hoiThoaiDangLam = "Meowth: Meo! Mới có {da}/5.";
        Kiem.bang("dang lam: loi NPC + Mục tiêu + tien do", "Meowth: Meo! Mới có 2/5.\nMục tiêu: Thu 5 Gói hàng (rơi từ Sandshrew)\n(2/5)",
                real.NhiemVuEngine.chuDangLam(m, 2));
        m.hoiThoaiDangLam = "";
        Kiem.bang("dang lam: khong loi NPC -> chi muc tieu (khong lap)", "Thu 5 Gói hàng (rơi từ Sandshrew)\n(1/5)",
                real.NhiemVuEngine.chuDangLam(m, 1));
    }
}
