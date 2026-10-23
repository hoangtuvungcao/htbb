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
 * Luat "cac item khong bi mo coi, deu su dung vao 1 viec nao do", kiem THANG tren CSDL day du sql/htbb.sql (nguon
 * duy nhat; cong cu phan tich cu tools/luu_tru_sinh_du_lieu/pokemon/kiem_item_mo_coi.py + bao cao KIEM_ITEM.md da luu tru).
 * Cac bat bien: bang cua hang doc duoc bang dung bo doc cua server, khong ra tien te, moi id ton tai; moi tin vat co cho
 * doi; vat pham 1201..1259 co nguon; itemgift chuoi chinh du 6 gia tri; vat lieu sinh thai deu vao cong thuc; giftcode an
 * toan chi phat vat pham co that.
 *
 * VI SAO: truoc ban nay 24 vat pham co nguon ma khong dung duoc (huy chuong Gym, tin vat su kien, keo EXP cu khi
 * bat dai han...), 65 vat pham thiet ke khong co nguon nao chay (thuoc / qua mong / do giu / bua 1201..1259,
 * trang bi nam 2), 13 vat lieu sinh thai chi ban NPC, 2 cap trung ten (Keo hiem 720/1214, Manh sao).
 */
public final class KiemItemMoCoi {

    private static final String F_GC_JSON = "../web/shared/data/giftcode-an-toan.json";

    private KiemItemMoCoi() {
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return null;
        }
    }

    public static void chay() {
        Kiem.nhom("Item mo coi: moi vat pham co nguon + cong dung (kiem tren sql/htbb.sql)");
        final Set<Integer> item = new HashSet<Integer>();
        for (final java.util.Map<String, String> r : DayDu.dong("data_item")) {
            item.add(Integer.valueOf(r.get("id")));
        }
        Kiem.dung("data_item doc duoc (> 1000 dong)", item.size() > 1000);
        // ten vat pham: khong trung giua cac id dang dung (Keo hiem 720/1214, Manh sao ...)
        final java.util.Map<String, Integer> ten = new java.util.HashMap<String, Integer>();
        final java.util.List<String> trung = new java.util.ArrayList<String>();
        for (final java.util.Map<String, String> r : DayDu.dong("data_vat_pham_lau_dai")) {
            final Integer cu = ten.put(r.get("ten"), Integer.valueOf(r.get("id_item")));
            if (cu != null) {
                trung.add(r.get("ten") + " " + cu + "/" + r.get("id_item"));
            }
        }
        Kiem.bang("data_vat_pham_lau_dai: ten khong trung", "[]", trung.toString());
        // 3. bang cua hang & doi vat pham (doc bang dung bo doc cua server)
        final java.util.List<java.util.Map<String, String>> ch = DayDu.dong("data_cua_hang_lau_dai");
        int so = 0;
        final Set<Integer> chiItem = new HashSet<Integer>();
        final Set<Integer> raItem = new HashSet<Integer>();
        for (final java.util.Map<String, String> r : ch) {
            so++;
            final String id = r.get("id"), cp = r.get("chi_phi"), th = r.get("thuong");
            final List<long[]> chi = real.CuaHangLauDai.docChiPhi(cp);
            final List<int[]> ra = real.CuaHangLauDai.docThuong(th);
            Kiem.dung("cua hang " + id + ": chi phi doc duoc (" + cp + ")", !chi.isEmpty() && chi.size() == cp.split(";").length);
            Kiem.dung("cua hang " + id + ": thuong doc duoc (" + th + ")", !ra.isEmpty() && ra.size() == th.split(",").length);
            Kiem.dung("cua hang " + id + ": khong ra tien te", !th.matches(".*\\b(XU|LUONG|LUONGKHOA|DN|MN)\\b.*"));
            for (final long[] c : chi) {
                if (c[0] == 2) {
                    chiItem.add(Integer.valueOf((int) c[1]));
                    Kiem.dung("cua hang " + id + ": chi phi item " + c[1] + " ton tai", item.contains(Integer.valueOf((int) c[1])));
                }
            }
            for (final int[] x : ra) {
                if (x[0] == 0) {
                    raItem.add(Integer.valueOf(x[1]));
                    Kiem.dung("cua hang " + id + ": thuong item " + x[1] + " ton tai", item.contains(Integer.valueOf(x[1])));
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
            Kiem.bang("hop dot kich " + i + " mo duoc (data_vat_pham_lau_dai nhom hop)", "hop|mo_hop:" + i,
                    DayDu.giaTri("data_vat_pham_lau_dai", "id_item", String.valueOf(i), "nhom") + "|"
                    + DayDu.giaTri("data_vat_pham_lau_dai", "id_item", String.valueOf(i), "hieu_ung"));
        }
        for (int i = 1201; i <= 1259; i++) {
            if (DayDu.tim("data_vat_pham_lau_dai", "id_item", String.valueOf(i)) == null || i == 1214 || i == 1231) {
                continue;
            }
            if (com.htbb.pokemon.TangToc.loaiCuaItem(i) >= 0) {
                continue; // tang toc: nguon trong code (Cua hang & Nap / TangTocGame.thuongKhoa) - KiemTangToc
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
        int dem = 0;
        int sai = 0;
        for (final java.util.Map<String, String> r : DayDu.dong("data_quest_moi")) {
            if (!"32000".equals(r.get("potiongift")) || !"32000".equals(r.get("npotion"))) {
                continue;
            }
            dem++;
            final String g = r.get("itemgift");
            if (!"32000".equals(g) && g.split(",").length != 6) {
                sai++;
            }
        }
        Kiem.dung("data_quest_moi: doc duoc itemgift (" + dem + " dong)", dem > 500);
        Kiem.bang("data_quest_moi: itemgift khong du 6 gia tri (InfoQuestTemplate.addGift AIOOBE)", Integer.valueOf(0), Integer.valueOf(sai));
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
        giftcode(item);
    }

    /** Giftcode an toan: danh sach trang chung web + game (HTBB-Web/shared/data/giftcode-an-toan.json). */
    static void giftcode(final Set<Integer> item) {
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
        // moi trang bi trong danh sach trang deu CO THAT trong data_item (giftcode khong phat id treo)
        int khong = 0;
        for (final int id : com.htbb.pokemon.GiftcodeAnToanDuLieu.TRANG_BI) {
            if (!item.contains(Integer.valueOf(id))) {
                khong++;
                System.out.println("   giftcode trang bi khong co trong data_item: " + id);
            }
        }
        Kiem.bang("trang bi giftcode deu co trong data_item", Integer.valueOf(0), Integer.valueOf(khong));
    }
}
