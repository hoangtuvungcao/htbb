import com.htbb.pokemon.KyNangSong;
import com.htbb.pokemon.Pokedex;
import com.htbb.pokemon.VatLieuSinhThai;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Luat "cac item khong bi mo coi, deu su dung vao 1 viec nao do" (tools/pokemon/kiem_item_mo_coi.py ->
 * du_lieu/KIEM_ITEM.md + kiem_item.csv). Voi co da giao (sv.lau_dai=1, sv.he_sinh_thai=0 va =1):
 * 0 vat pham mo coi (co nguon, khong cong dung), 0 chi-ban-NPC, 0 khong lay duoc, 0 tham chieu treo, 0 trung ten,
 * 0 ten kiem hiep con lay duoc, 0 quy tac viet tay loi thoi.
 *
 * VI SAO: truoc ban nay 24 vat pham co nguon ma khong dung duoc (huy chuong Gym, tin vat su kien, keo EXP cu khi
 * bat dai han...), 65 vat pham thiet ke khong co nguon nao chay (thuoc / qua mong / do giu / bua 1201..1259,
 * trang bi nam 2), 13 vat lieu sinh thai chi ban NPC, 2 cap trung ten (Keo hiem 720/1214, Manh sao).
 * Script doc SQL/CSV/Java; test nay (chi doc file) chan bao cao cu (dau van tay dau vao) va kiem doc lap cac bat
 * bien de bao cao khong the "dat" nho du lieu sai: bang cua hang doc duoc bang dung bo doc cua server, khong ra tien
 * te, moi id ton tai; moi tin vat co cho doi; itemgift chuoi chinh du 6 gia tri; vat lieu sinh thai deu vao cong thuc.
 */
public final class KiemItemMoCoi {

    private static final String F_MD = "../du_lieu/KIEM_ITEM.md";
    private static final String F_CSV = "../du_lieu/kiem_item.csv";
    private static final String F_VP = "../sql/pokemon_vat_pham_lau_dai.sql";
    private static final String F_ND = "../sql/pokemon_noi_dung.sql";
    private static final String F_LD = "../sql/pokemon_lau_dai.sql";
    private static final String F_GC_JSON = "../web/shared/data/giftcode-an-toan.json";
    private static final String[] DAU_VAO = {"sql/htbb_day_du.sql", "sql/pokemon_noi_dung.sql", "sql/pokemon_vat_pham_lau_dai.sql",
        "sql/pokemon_lau_dai.sql", "sql/pokemon_doi_chu.sql", "sql/pokemon_nhiem_vu.sql", "du_lieu/noi_dung_roi.csv",
        "du_lieu/nhiem_vu_moi.csv", "sql/them_the_dat_ten_pet.sql"};

    private KiemItemMoCoi() {
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return null;
        }
    }

    static String sha1DauVao() {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (final String f : DAU_VAO) {
                md.update(f.getBytes(StandardCharsets.UTF_8));
                final byte[] b = Files.readAllBytes(Paths.get("..", f));
                final java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream(b.length);
                for (final byte x : b) {
                    if (x != '\r') {
                        o.write(x);
                    }
                }
                md.update(o.toByteArray());
            }
            final StringBuilder s = new StringBuilder();
            for (final byte x : md.digest()) {
                s.append(String.format("%02x", x & 0xff));
            }
            return s.toString();
        } catch (final Exception e) {
            return "loi: " + e;
        }
    }

    public static void chay() {
        Kiem.nhom("Item mo coi: moi vat pham co nguon + cong dung (tools/pokemon/kiem_item_mo_coi.py)");
        final String md = doc(F_MD);
        final String csv = doc(F_CSV);
        Kiem.dung(F_MD + " + " + F_CSV + " co (python3 -I tools/pokemon/kiem_item_mo_coi.py)", md != null && csv != null);
        if (md == null || csv == null) {
            return;
        }
        // 1. bao cao khop du lieu hien tai
        final Matcher dv = Pattern.compile("Dấu vân tay đầu vào .*?`([0-9a-f]{40})`").matcher(md);
        final boolean coDv = dv.find();
        Kiem.dung("bao cao co dau van tay dau vao", coDv);
        Kiem.bang("bao cao KHONG cu (SQL/CSV doi -> chay lai tools/pokemon/kiem_item_mo_coi.py)", sha1DauVao(), coDv ? dv.group(1) : "");
        for (final String f : DAU_VAO) {
            Kiem.dung("danh sach dau van tay trong bao cao co " + f, md.contains("`" + f + "`"));
        }
        // 2. tom tat: cau hinh A, B = 0 loi
        for (final String ch : new String[]{"A", "B"}) {
            final Matcher m = Pattern.compile("\\| " + ch + " \\(`lau_dai=1 he_sinh_thai=\\d`\\) \\| (\\d+) \\| (\\d+) \\| (\\d+) \\| (\\d+) \\|")
                    .matcher(md);
            final boolean co = m.find();
            Kiem.dung("tom tat cau hinh " + ch + " doc duoc", co);
            if (!co) {
                continue;
            }
            Kiem.bang("cau hinh " + ch + ": mo coi", "0", m.group(2));
            Kiem.bang("cau hinh " + ch + ": chi ban NPC", "0", m.group(3));
            Kiem.bang("cau hinh " + ch + ": khong lay duoc", "0", m.group(4));
            Kiem.dung("cau hinh " + ch + ": co vat pham ton tai (> 700)", Integer.parseInt(m.group(1)) > 700);
        }
        final Matcher t = Pattern.compile("Tham chiếu treo: \\*\\*(\\d+)\\*\\*\\. Quy tắc lỗi thời: \\*\\*(\\d+)\\*\\*\\. Trùng tên: \\*\\*(\\d+)\\*\\*\\."
                + " Tên kiếm hiệp còn lấy được: \\*\\*(\\d+)\\*\\*").matcher(md);
        final boolean coT = t.find();
        Kiem.dung("dong tham chieu treo / quy tac / trung ten doc duoc", coT);
        if (coT) {
            Kiem.bang("tham chieu treo (id khong ton tai)", "0", t.group(1));
            Kiem.bang("quy tac viet tay loi thoi (mau khong con trong code)", "0", t.group(2));
            Kiem.bang("trung ten giua vat pham khac loai / chuc nang", "0", t.group(3));
            Kiem.bang("ten kiem hiep con lay duoc", "0", t.group(4));
        }
        Kiem.dung("bao cao: tong loi bi chan = 0", md.contains("Lỗi bị test chặn (cấu hình A + B): **0**."));
        int loiCsv = 0;
        for (final String d : csv.split("\n")) {
            final String[] c = d.split(",", -1);
            if (c.length > 7 && !d.startsWith("khoa,") && (laLoi(c[5]) || laLoi(c[6]))) {
                loiCsv++;
            }
        }
        Kiem.bang("kiem_item.csv: dong loi o cau hinh A/B", Integer.valueOf(0), Integer.valueOf(loiCsv));
        // 3. bang cua hang & doi vat pham (doc bang dung bo doc cua server)
        final String vp = doc(F_VP);
        final String nd = doc(F_ND);
        Kiem.dung(F_VP + " co data_cua_hang_lau_dai", vp != null && vp.contains("CREATE TABLE `data_cua_hang_lau_dai`"));
        if (vp == null || nd == null) {
            return;
        }
        final Set<Integer> item = idItem(csv); // vu tru data_item cua script (dump + file sinh sau dump)
        final Matcher r = Pattern.compile("\\n\\((\\d+),'((?:[^'\\\\]|\\\\.)*)','((?:[^'\\\\]|\\\\.)*)','([^']*)','([^']*)',(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)\\)")
                .matcher(vp.substring(vp.indexOf("INSERT INTO `data_cua_hang_lau_dai`")));
        int so = 0;
        final Set<Integer> chiItem = new HashSet<Integer>();
        final Set<Integer> raItem = new HashSet<Integer>();
        while (r.find()) {
            so++;
            final List<long[]> chi = real.CuaHangLauDai.docChiPhi(r.group(4));
            final List<int[]> ra = real.CuaHangLauDai.docThuong(r.group(5));
            Kiem.dung("cua hang " + r.group(1) + ": chi phi doc duoc (" + r.group(4) + ")",
                    !chi.isEmpty() && chi.size() == r.group(4).split(";").length);
            Kiem.dung("cua hang " + r.group(1) + ": thuong doc duoc (" + r.group(5) + ")",
                    !ra.isEmpty() && ra.size() == r.group(5).split(",").length);
            Kiem.dung("cua hang " + r.group(1) + ": khong ra tien te", !r.group(5).matches(".*\\b(XU|LUONG|LUONGKHOA|DN|MN)\\b.*"));
            for (final long[] c : chi) {
                if (c[0] == 2) {
                    chiItem.add(Integer.valueOf((int) c[1]));
                    Kiem.dung("cua hang " + r.group(1) + ": chi phi item " + c[1] + " ton tai", item.contains(Integer.valueOf((int) c[1])));
                }
            }
            for (final int[] x : ra) {
                if (x[0] == 0) {
                    raItem.add(Integer.valueOf(x[1]));
                    Kiem.dung("cua hang " + r.group(1) + ": thuong item " + x[1] + " ton tai", item.contains(Integer.valueOf(x[1])));
                }
            }
        }
        Kiem.dung("data_cua_hang_lau_dai co >= 100 dong (doc " + so + ")", so >= 100);
        for (int i = 1039; i <= 1061; i++) {
            if (i == 1050 || i == 1051) {
                continue; // hop dot kich: mo bang "Dung" (data_vat_pham_lau_dai nhom hop)
            }
            Kiem.dung("tin vat / huy chuong " + i + " co cho doi (cong dung)", chiItem.contains(Integer.valueOf(i)));
        }
        for (final int i : new int[]{1050, 1051}) {
            Kiem.dung("hop dot kich " + i + " mo duoc (data_vat_pham_lau_dai nhom hop)", vp.contains("(" + i + ",'") && vp.contains("'hop','mo_hop:" + i + "'"));
        }
        for (int i = 1201; i <= 1259; i++) {
            if (!vp.contains("\n(" + i + ",'") || i == 1214 || i == 1231) {
                continue;
            }
            if (com.htbb.pokemon.TangToc.loaiCuaItem(i) >= 0) {
                continue; // tang toc: nguon trong code (Cua hang & Nap / TangTocGame.thuongKhoa) - kiem_item_mo_coi.py + KiemTangToc
            }
            Kiem.dung("vat pham " + i + " co nguon (cua hang / doi)", raItem.contains(Integer.valueOf(i)));
        }
        Kiem.dung("Keo hiem 1214 co nguon doi", raItem.contains(Integer.valueOf(Pokedex.ID_KEO_HIEM)));
        // 4. Pokedex khi bat dai han: keo EXP cu -> Keo hiem; moc 50 them Trung may man
        final String p25 = Pokedex.doiThuongLauDai(Pokedex.phanThuong(25, 0), 25);
        Kiem.dung("Pokedex 25 (dai han): khong con PT:10..12", !p25.matches(".*PT:1[012]:.*"));
        Kiem.dung("Pokedex 25 (dai han): co Keo hiem", p25.contains("ITEM:" + Pokedex.ID_KEO_HIEM + ":"));
        Kiem.dung("Pokedex 50 (dai han): co Trung may man", Pokedex.doiThuongLauDai(Pokedex.phanThuong(50, 0), 50)
                .contains("ITEM:" + Pokedex.ID_TRUNG_MAY_MAN + ":1"));
        Kiem.bang("Pokedex 10 (dai han) khong doi", Pokedex.phanThuong(10, 0), Pokedex.doiThuongLauDai(Pokedex.phanThuong(10, 0), 10));
        // 5. chuoi chinh (data_quest_moi): itemgift 6 gia tri (5 lop + cong), vat pham nhiem vu hop le
        final String ld = doc(F_LD);
        Kiem.dung(F_LD + " co data_quest_moi", ld != null && ld.contains("INSERT INTO `data_quest_moi`"));
        if (ld != null) {
            final Matcher q = Pattern.compile("'32000','32000','([0-9,]+)',\\d+,'").matcher(ld.substring(ld.indexOf("INSERT INTO `data_quest_moi`")));
            int dem = 0;
            int sai = 0;
            while (q.find()) {
                dem++;
                if (!"32000".equals(q.group(1)) && q.group(1).split(",").length != 6) {
                    sai++;
                }
            }
            Kiem.dung("data_quest_moi: doc duoc itemgift (" + dem + " dong)", dem > 500);
            Kiem.bang("data_quest_moi: itemgift khong du 6 gia tri (InfoQuestTemplate.addGift AIOOBE)", Integer.valueOf(0), Integer.valueOf(sai));
        }
        // 6. vat lieu he sinh thai: moi thu cau / dao duoc deu vao it nhat mot cong thuc (khong chi ban NPC)
        final Set<Integer> vao = new HashSet<Integer>();
        for (final KyNangSong.CongThuc ct : KyNangSong.CONG_THUC) {
            for (final int[] nl : ct.nguyenLieu) {
                vao.add(Integer.valueOf(nl[0]));
            }
        }
        for (final int[] d : KyNangSong.BANG_CAU_CA) {
            Kiem.dung("cau ca: " + VatLieuSinhThai.ten(d[0]) + " (" + d[0] + ") vao cong thuc", vao.contains(Integer.valueOf(d[0])));
        }
        for (final int[] d : KyNangSong.BANG_DAO) {
            Kiem.dung("khao co: " + VatLieuSinhThai.ten(d[0]) + " (" + d[0] + ") vao cong thuc", vao.contains(Integer.valueOf(d[0])));
        }
        for (final KyNangSong.CongThuc ct : KyNangSong.CONG_THUC) {
            Kiem.dung("cong thuc " + ct.ten + ": dau ra la vat lieu co hoac vat pham", ct.ra == 0 || VatLieuSinhThai.coMa(ct.ra));
        }
        // 7. chuoi chinh nam 2 (dong_co_tuong_thich=0): chi chay khi co ban do + tran cap mua >= 91 (khong tu tao map)
        final com.htbb.pokemon.NhiemVuMau n2 = new com.htbb.pokemon.NhiemVuMau();
        n2.chuoiNam2 = com.htbb.pokemon.NhiemVuMau.laChuoiNam2("chinh", 0);
        n2.kieu = "noi_chuyen";
        Kiem.dung("chinh + dong_co_tuong_thich=0 la chuoi nam 2", n2.chuoiNam2);
        Kiem.sai("chinh + dong_co_tuong_thich=1 KHONG la chuoi nam 2 (engine cu)", com.htbb.pokemon.NhiemVuMau.laChuoiNam2("chinh", 1));
        Kiem.sai("chuoi nam 2 khong chiem khe phu", n2.chiemKhe());
        Kiem.dung("nam 2: chua co map_id -> chua chay", n2.kiemNam2(100, true) != null);
        n2.mapId = 150;
        Kiem.dung("nam 2: tran cap mua 90 -> chua chay", n2.kiemNam2(90, true) != null);
        Kiem.dung("nam 2: ban do chua co tren may chu -> chua chay", n2.kiemNam2(91, false) != null);
        Kiem.dung("nam 2: co ban do + tran 91 -> chay", n2.kiemNam2(91, true) == null);
        n2.kieu = "dan_y";
        Kiem.dung("nam 2: kieu dan_y (dan y) -> khong chay", n2.kiemNam2(100, true) != null);
        giftcode(csv);
    }

    /** Giftcode an toan: danh sach trang chung web + game (HTBB-Web/shared/data/giftcode-an-toan.json). */
    static void giftcode(final String csv) {
        Kiem.nhom("Giftcode an toan: chi vat pham khoa, khong cao cap, trang bi khong vuot cap (chung web + game)");
        final byte[] raw;
        try {
            raw = Files.readAllBytes(Paths.get(F_GC_JSON));
        } catch (final Exception e) {
            System.out.println("   (bo qua: khong co " + F_GC_JSON + " - repo HTBB-Web dat canh HTBB)");
            return;
        }
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final StringBuilder h = new StringBuilder();
            for (final byte x : md.digest(raw)) {
                h.append(String.format("%02x", x & 0xff));
            }
            Kiem.bang("GiftcodeAnToanDuLieu khop JSON (chay lai tools/pokemon/sinh_giftcode_an_toan.py)",
                    com.htbb.pokemon.GiftcodeAnToanDuLieu.SHA256_JSON, h.toString());
        } catch (final Exception e) {
            Kiem.dung("SHA-256: " + e, false);
        }
        final String js = new String(raw, StandardCharsets.UTF_8);
        final Matcher m = Pattern.compile("\\{\\s*\"thuong\": \"([^\"]*)\",\\s*\"cap\": (\\d+),\\s*\"capItem\": \\{([^}]*)\\},\\s*\"ok\": (true|false)",
                Pattern.DOTALL).matcher(js);
        int so = 0;
        while (m.find()) {
            so++;
            final java.util.HashMap<Integer, Integer> cap = new java.util.HashMap<Integer, Integer>();
            final Matcher c = Pattern.compile("\"(\\d+)\": (\\d+)").matcher(m.group(3));
            while (c.find()) {
                cap.put(Integer.valueOf(c.group(1)), Integer.valueOf(c.group(2)));
            }
            final String chuan = com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaThuong(m.group(1));
            final String loi = com.htbb.pokemon.GiftcodeAnToanLogic.kiemVatPham(chuan, Integer.parseInt(m.group(2)), id -> {
                final Integer v = cap.get(Integer.valueOf(id));
                return v != null ? v.intValue()
                        : java.util.Arrays.binarySearch(com.htbb.pokemon.GiftcodeAnToanDuLieu.TRANG_BI, id) >= 0 ? -1 : 1;
            });
            Kiem.bang("ca chung: " + m.group(1) + " (cap " + m.group(2) + ")", Boolean.valueOf("true".equals(m.group(4))),
                    Boolean.valueOf(loi == null));
        }
        Kiem.dung("JSON co >= 10 ca thu nghiem (" + so + ")", so >= 10);
        // moi vat pham trong danh sach trang ma CO THAT deu co cong dung (giftcode khong tao vat pham mo coi)
        int khong = 0;
        for (final String d : csv.split("\n")) {
            final Matcher r = Pattern.compile("^item:(\\d+),").matcher(d);
            if (!r.find()) {
                continue;
            }
            final int id = Integer.parseInt(r.group(1));
            boolean trong = java.util.Arrays.binarySearch(com.htbb.pokemon.GiftcodeAnToanDuLieu.TRANG_BI, id) >= 0;
            for (final int[] x : com.htbb.pokemon.GiftcodeAnToanDuLieu.VAT_PHAM) {
                trong |= id >= x[0] && id <= x[1];
            }
            if (trong && !coCongDung(d)) {
                khong++;
                System.out.println("   giftcode phat duoc nhung khong co cong dung: " + d);
            }
        }
        Kiem.bang("vat pham giftcode phat duoc deu co cong dung that", Integer.valueOf(0), Integer.valueOf(khong));
    }

    /** Cot cong_dung (cuoi) cua kiem_item.csv co it nhat mot cong dung khong phai "(bán)". */
    static boolean coCongDung(final String dong) {
        final int i = dong.lastIndexOf(',');
        final String cd = dong.substring(i + 1);
        for (final String x : cd.split(" ; ")) {
            if (!x.trim().isEmpty() && !x.endsWith("(bán)")) {
                return true;
            }
        }
        return false;
    }

    static boolean laLoi(final String lop) {
        return "mo_coi".equals(lop) || "chi_ban".equals(lop) || "khong_lay_duoc".equals(lop);
    }

    /** Moi id data_item trong kiem_item.csv (cot khoa "item:N"). */
    static Set<Integer> idItem(final String csv) {
        final Set<Integer> ra = new HashSet<Integer>();
        final Matcher m = Pattern.compile("(?m)^item:(\\d+),").matcher(csv);
        while (m.find()) {
            ra.add(Integer.valueOf(m.group(1)));
        }
        return ra;
    }
}
