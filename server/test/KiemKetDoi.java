import com.htbb.pokemon.KetDoi;
import com.htbb.pokemon.LoaiPokemon;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Le ket doi (thay dam cuoi cu): dieu kien nhom trung (Egg Group) theo Pokemon goc + dieu kien cu.
 *
 * VI SAO: chu game muon he "vo chong / dam cuoi" kieu kiem hiep thanh "ket doi" kieu Pokemon, va hai Pokemon chi
 * ket doi duoc khi LOAI HIEN TAI chung it nhat mot nhom trung (Pichu thuoc "Chưa khám phá": khong voi ai). Kiem
 *  - bang nhom trung trong sql/nang_cap.sql (khoi sua_ket_doi.sql) dung du lieu GOC (bang doi chieu viet tay o day), du moi loai nguoi
 *    choi (LoaiPokemon), khop khoi trong sql/htbb_day_du.sql;
 *  - ma tran ket doi CHO MOI CAP 21 x 21 loai (doi chieu luat doc lap o day), doi xung, ma tran 5 dong;
 *  - cau giai thich (dung vi du cua chu game) va cac ly do tu choi;
 *  - cap da ket doi tu truoc GIU NGUYEN: chi NPC dang ky goi xet, tien hoa / dang nhap / nhan / Vay trai tim khong xet.
 */
public final class KiemKetDoi {

    /** Nhom trung GOC (Bulbapedia) theo ten tieng Anh: doi chieu doc lap voi SQL. */
    static final Map<Integer, String[]> GOC = new LinkedHashMap<Integer, String[]>();

    static {
        for (final int d : new int[]{1, 2, 3}) {
            GOC.put(d, new String[]{"monster", "grass"});
        }
        for (final int d : new int[]{4, 5, 6}) {
            GOC.put(d, new String[]{"monster", "dragon"});
        }
        for (final int d : new int[]{7, 8, 9}) {
            GOC.put(d, new String[]{"monster", "water1"});
        }
        GOC.put(172, new String[]{"undiscovered"});
        GOC.put(25, new String[]{"field", "fairy"});
        GOC.put(26, new String[]{"field", "fairy"});
        for (final int d : new int[]{133, 134, 135, 136, 196, 197, 470, 471, 700}) {
            GOC.put(d, new String[]{"field"});
        }
    }

    /** Ky tu font J2ME / Android khong ve duoc (docs/thiet-ke-game.md 10.3). */
    static final String KY_TU_CAM = "\"'&;<>=@[]_…–—→×≥≤“”·";

    public static void chay() {
        Kiem.nhom("Le ket doi: nhom trung (Egg Group) + dieu kien ket doi, cap cu giu nguyen");
        try {
            chayThat();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    /** Cac bo gia tri cua INSERT vao bang (khoi dau tien co ten bang do). */
    static List<String[]> dong(final String sql, final String bang) {
        final List<String[]> ra = new ArrayList<String[]>();
        final Matcher m = Pattern.compile("INSERT (?:IGNORE )?INTO `" + bang + "`[^;]*?VALUES\\s*(.*?);",
                Pattern.DOTALL).matcher(sql);
        while (m.find()) { // moi lenh INSERT (khoi loai-day-du chia nhieu lenh)
            final Matcher r = Pattern.compile("\\(([^()]*)\\)").matcher(m.group(1));
            while (r.find()) {
                final String[] o = r.group(1).split(",");
                for (int i = 0; i < o.length; i++) {
                    o[i] = o[i].trim().replaceAll("^'|'$", "").replace("\\'", "'").replace("''", "'"); // Farfetch'd: dump \\' / sinh 
                }
                ra.add(o);
            }
        }
        return ra;
    }

    static void napTuSql(final String sql) {
        final Map<Integer, KetDoi.Nhom> nhom = new HashMap<Integer, KetDoi.Nhom>();
        for (final String[] o : dong(sql, "data_nhom_trung_ten")) {
            nhom.put(Integer.valueOf(o[0]), new KetDoi.Nhom(Integer.parseInt(o[0]), o[2], !"0".equals(o[3])));
        }
        final Map<Integer, KetDoi.Loai> loai = new HashMap<Integer, KetDoi.Loai>();
        for (final String[] o : dong(sql, "data_nhom_trung")) {
            final int[] ds = "NULL".equals(o[3]) ? new int[]{Integer.parseInt(o[2])}
                    : new int[]{Integer.parseInt(o[2]), Integer.parseInt(o[3])};
            loai.put(Integer.valueOf(o[0]), new KetDoi.Loai(Integer.parseInt(o[0]), o[1], ds));
        }
        KetDoi.datBang(nhom, loai);
    }

    private static void chayThat() throws Exception {
        final String sql = DayDu.phanNangCap("sua_ket_doi.sql"); // tep cu gop vao sql/nang_cap.sql phan 11

        // ---- chua nap: tu choi, khong nem loi
        KetDoi.datBang(new HashMap<Integer, KetDoi.Nhom>(), new HashMap<Integer, KetDoi.Loai>());
        Kiem.sai("chua nap bang: khong cho ket doi", KetDoi.xet(4, 7).duoc);
        Kiem.dung("chua nap bang: bao ly do", KetDoi.xet(4, 7).loiNhan.contains("nhóm trứng"));

        napTuSql(sql);
        Kiem.dung("nap duoc bang nhom trung tu khoi sua_ket_doi.sql cua sql/nang_cap.sql", KetDoi.daNap());
        Kiem.bang("15 nhom trung", Integer.valueOf(15), Integer.valueOf(KetDoi.bangNhom().size()));

        // ---- du lieu: du moi loai nguoi choi, dung du lieu goc, ten khop LoaiPokemon
        final Map<String, Integer> idTheoMa = new HashMap<String, Integer>();
        for (final String[] o : dong(sql, "data_nhom_trung_ten")) {
            idTheoMa.put(o[1], Integer.valueOf(o[0]));
        }
        Kiem.bang("so loai trong data_nhom_trung = so loai nguoi choi (LoaiPokemon)",
                Integer.valueOf(LoaiPokemon.soLoai()), Integer.valueOf(KetDoi.bangLoai().size()));
        Kiem.bang("bang doi chieu goc du moi loai", Integer.valueOf(LoaiPokemon.soLoai()), Integer.valueOf(GOC.size()));
        for (final int dex : LoaiPokemon.tatCaDex()) {
            final KetDoi.Loai l = KetDoi.loai(dex);
            Kiem.dung("co nhom trung cho " + LoaiPokemon.ten(dex) + " (#" + dex + ")", l != null);
            if (l == null) {
                continue;
            }
            Kiem.bang("ten loai #" + dex, LoaiPokemon.ten(dex), l.ten);
            final Set<Integer> mong = new HashSet<Integer>();
            for (final String ma : GOC.get(dex)) {
                mong.add(idTheoMa.get(ma));
            }
            final Set<Integer> co = new HashSet<Integer>();
            for (final int n : l.nhom) {
                co.add(Integer.valueOf(n));
            }
            Kiem.bang("nhom trung goc cua " + l.ten, mong, co);
        }
        Kiem.sai("Chua kham pha (Undiscovered) khong ket doi duoc",
                KetDoi.bangNhom().get(idTheoMa.get("undiscovered")).ketDoiDuoc);

        // ---- htbb_day_du.sql chua dung du lieu nhu migration
        final String du = doc("../sql/htbb_day_du.sql");
        // ban-098: khoi them_loai_day_du.sql (loai-day-du, cung luoc do, INSERT IGNORE) them dong cho loai khac;
        // dump = hop hai khoi, dong ket-doi giu nguyen (khong bi ghi de)
        final Set<String> nhomDump = tapDong(dong(du, "data_nhom_trung"), 4);
        final Set<String> nhomKd = tapDong(dong(sql, "data_nhom_trung"), 4);
        final Set<String> hop = new HashSet<String>(nhomKd);
        hop.addAll(tapDong(dong(DayDu.phanNangCap("them_loai_day_du.sql"), "data_nhom_trung"), 4));
        Kiem.dung("htbb_day_du.sql: data_nhom_trung chua moi dong ket-doi", nhomDump.containsAll(nhomKd));
        Kiem.dung("htbb_day_du.sql: moi dong data_nhom_trung den tu ket-doi / loai-day-du", hop.containsAll(nhomDump));
        Kiem.bang("htbb_day_du.sql: data_nhom_trung = ket-doi + loai-day-du (INSERT IGNORE, moi dex mot dong)",
                tapDong(dong(du, "data_nhom_trung"), 1).size(), nhomDump.size());
        Kiem.bang("htbb_day_du.sql: data_nhom_trung_ten = migration", tapDong(dong(sql, "data_nhom_trung_ten"), 4),
                tapDong(dong(du, "data_nhom_trung_ten"), 4));

        // ---- ma tran: MOI cap loai, doi chieu luat doc lap (chung >= 1 nhom GOC, khong ai la undiscovered)
        final int[] tat = LoaiPokemon.tatCaDex();
        int soCap = 0;
        int soDuoc = 0;
        for (final int a : tat) {
            for (final int b : tat) {
                final boolean mong = hopGoc(a, b);
                final KetDoi.KetQua kq = KetDoi.xet(a, b);
                Kiem.bang("ket doi " + LoaiPokemon.ten(a) + " x " + LoaiPokemon.ten(b), Boolean.valueOf(mong),
                        Boolean.valueOf(kq.duoc));
                Kiem.bang("doi xung " + LoaiPokemon.ten(a) + " x " + LoaiPokemon.ten(b), Boolean.valueOf(kq.duoc),
                        Boolean.valueOf(KetDoi.xet(b, a).duoc));
                kyTuHopLe("cau nhom trung " + a + "x" + b, kq.loiNhan);
                soCap++;
                if (kq.duoc) {
                    soDuoc++;
                }
            }
        }
        Kiem.bang("so cap da xet (21 x 21)", Integer.valueOf(441), Integer.valueOf(soCap));
        // Quai vat: 9 x 9 = 81; Dong co: (2 + 9) x (2 + 9) = 121; Pichu 0.
        Kiem.bang("so cap ket doi duoc", Integer.valueOf(81 + 121), Integer.valueOf(soDuoc));

        // Ma tran 5 dong (dang dau; Pichu xet them dang Pikachu): Charmander, Bulbasaur, Squirtle, Pikachu, Eevee.
        final int[] dong5 = {4, 1, 7, 25, 133};
        final String[] mongMaTran = {
            "11100", // Charmander: Quai vat voi Charmander / Bulbasaur / Squirtle
            "11100", // Bulbasaur
            "11100", // Squirtle
            "00011", // Pikachu: Dong co (+ Tien) voi Pikachu / Eevee
            "00011", // Eevee
        };
        for (int i = 0; i < dong5.length; i++) {
            final StringBuilder sb = new StringBuilder();
            for (int j = 0; j < dong5.length; j++) {
                sb.append(KetDoi.xet(dong5[i], dong5[j]).duoc ? '1' : '0');
            }
            Kiem.bang("ma tran 5 dong: hang " + LoaiPokemon.ten(dong5[i]), mongMaTran[i], sb.toString());
        }
        for (final int x : new int[]{172, 4, 1, 7, 25, 133}) {
            Kiem.sai("Pichu khong ket doi voi " + LoaiPokemon.ten(x), KetDoi.xet(172, x).duoc);
        }
        Kiem.dung("tien hoa doi loai: Charizard x Venusaur van chung Quai vat", KetDoi.xet(6, 3).duoc);
        Kiem.dung("nhanh Eevee: Sylveon x Raichu chung Dong co", KetDoi.xet(700, 26).duoc);

        // ---- cau giai thich (vi du cua chu game, gach ngang thay "—" vi font J2ME)
        Kiem.bang("cau hop: Charmander x Squirtle",
                "Charmander (Quái vật, Rồng) và Squirtle (Quái vật, Nước 1) cùng nhóm Quái vật - có thể kết đôi.",
                KetDoi.xet(4, 7).loiNhan);
        Kiem.bang("cau hop hai nhom: Pikachu x Raichu",
                "Pikachu (Đồng cỏ, Tiên) và Raichu (Đồng cỏ, Tiên) cùng nhóm Đồng cỏ, Tiên - có thể kết đôi.",
                KetDoi.xet(25, 26).loiNhan);
        Kiem.bang("cau khong hop: Charmander x Eevee",
                "Charmander (Quái vật, Rồng) và Eevee (Đồng cỏ) không chung nhóm trứng nào - chưa thể kết đôi.",
                KetDoi.xet(4, 133).loiNhan);
        Kiem.bang("cau Pichu (Chua kham pha, goi y tien hoa)",
                "Pichu (Chưa khám phá) và Eevee (Đồng cỏ): Pichu thuộc nhóm Chưa khám phá, không thể kết đôi"
                        + " - hãy tiến hoá thành Pikachu trước.",
                KetDoi.xet(172, 133).loiNhan);
        Kiem.sai("loai ngoai bang: khong ket doi", KetDoi.xet(4, 150).duoc);
        Kiem.dung("loai ngoai bang: bao chua co du lieu", KetDoi.xet(4, 150).loiNhan.contains("chưa có dữ liệu"));

        // ---- ly do tu choi day du (nhom 2 nguoi = ca hai dong y; duc dang ky; cap 40; nhom trung)
        final KetDoi.Ben charmanderDuc = new KetDoi.Ben("An", 4, KetDoi.DUC, 45, false);
        final KetDoi.Ben squirtleCai = new KetDoi.Ben("Binh", 7, KetDoi.CAI, 45, false);
        final KetDoi.Ben eeveeCai = new KetDoi.Ben("Chi", 133, KetDoi.CAI, 45, false);
        Kiem.bang("du dieu kien: Charmander duc + Squirtle cai", null,
                KetDoi.lyDoTuChoi(charmanderDuc, Arrays.asList(charmanderDuc, squirtleCai)));
        Kiem.bang("khong nhom", "Hãy lập nhóm đúng 2 thành viên với Pokémon muốn kết đôi (cả hai vào chung nhóm là đồng ý).",
                KetDoi.lyDoTuChoi(charmanderDuc, null));
        Kiem.dung("nhom 3 nguoi bi tu choi", KetDoi.lyDoTuChoi(charmanderDuc,
                Arrays.asList(charmanderDuc, squirtleCai, eeveeCai)).startsWith("Hãy lập nhóm đúng 2"));
        Kiem.bang("khac nhom trung: tu choi voi cau nhom trung", KetDoi.xet(4, 133).loiNhan,
                KetDoi.lyDoTuChoi(charmanderDuc, Arrays.asList(charmanderDuc, eeveeCai)));
        Kiem.bang("cung gioi", "Lễ kết đôi cần một Pokémon đực và một Pokémon cái.",
                KetDoi.lyDoTuChoi(charmanderDuc, Arrays.asList(charmanderDuc,
                        new KetDoi.Ben("Dung", 7, KetDoi.DUC, 45, false))));
        Kiem.bang("Pokemon cai dang ky", "Pokémon đực trong nhóm đứng ra đăng ký Lễ kết đôi.",
                KetDoi.lyDoTuChoi(squirtleCai, Arrays.asList(charmanderDuc, squirtleCai)));
        Kiem.bang("duoi cap 40", "Cả hai cần từ cấp 40 trở lên để kết đôi.",
                KetDoi.lyDoTuChoi(charmanderDuc, Arrays.asList(charmanderDuc,
                        new KetDoi.Ben("Em", 7, KetDoi.CAI, 39, false))));
        Kiem.bang("ban da ket doi", "Binh đã có bạn kết đôi.",
                KetDoi.lyDoTuChoi(charmanderDuc, Arrays.asList(charmanderDuc,
                        new KetDoi.Ben("Binh", 7, KetDoi.CAI, 45, true))));
        Kiem.dung("Pichu cap 40 (khong the - van tu choi ro rang)", KetDoi.lyDoTuChoi(charmanderDuc,
                Arrays.asList(charmanderDuc, new KetDoi.Ben("Gia", 172, KetDoi.CAI, 45, false))).contains("Chưa khám phá"));
        for (final String s : new String[]{
            KetDoi.lyDoTuChoi(charmanderDuc, null),
            KetDoi.lyDoTuChoi(squirtleCai, Arrays.asList(charmanderDuc, squirtleCai)),
            KetDoi.lyDoTuChoi(new KetDoi.Ben("A", 4, KetDoi.DUC, 45, true), null)}) {
            kyTuHopLe("ly do tu choi", s);
        }

        // ---- cap cu giu nguyen (grandfather): da ket doi thi KHONG xet nhom trung nua
        final KetDoi.Ben capCu = new KetDoi.Ben("Cu", 4, KetDoi.DUC, 60, true);
        Kiem.bang("da ket doi: bao da co ban, khong xet nhom trung",
                "Bạn đã có bạn kết đôi, không thể đăng ký Lễ kết đôi mới.",
                KetDoi.lyDoTuChoi(capCu, Arrays.asList(capCu, new KetDoi.Ben("Eevee cu", 133, KetDoi.CAI, 60, true))));
        kiemChiXetLucDangKy();
    }

    /** Luat doc lap tu bang GOC: chung >= 1 nhom, khong loai nao thuoc undiscovered. */
    static boolean hopGoc(final int a, final int b) {
        final List<String> na = Arrays.asList(GOC.get(a));
        final List<String> nb = Arrays.asList(GOC.get(b));
        if (na.contains("undiscovered") || nb.contains("undiscovered")) {
            return false;
        }
        for (final String x : na) {
            if (nb.contains(x)) {
                return true;
            }
        }
        return false;
    }

    static Set<String> tapDong(final List<String[]> ds, final int soCot) {
        final Set<String> ra = new HashSet<String>();
        for (final String[] o : ds) {
            ra.add(String.join("|", Arrays.copyOf(o, soCot)));
        }
        return ra;
    }

    static void kyTuHopLe(final String moTa, final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (KY_TU_CAM.indexOf(s.charAt(i)) >= 0) {
                Kiem.dung(moTa + ": ky tu font J2ME khong ve duoc '" + s.charAt(i) + "' trong: " + s, false);
                return;
            }
        }
    }

    /**
     * Chi NPC dang ky (MapDoMenu case -27) goi xet ket doi; cho khac (tien hoa, nap nhan vat, chi so nhan, Vay trai
     * tim dich chuyen, huy ket doi) khong goi - cap da ket doi tu he cu / truoc khi tien hoa giu nguyen.
     */
    static void kiemChiXetLucDangKy() throws Exception {
        final List<String> goi = new ArrayList<String>();
        quet(new File("src"), goi);
        Kiem.bang("chi MapDoMenu (NPC dang ky) + Map (nap luc khoi dong) dung KetDoi",
                Arrays.asList("src/real/Map.java", "src/real/MapDoMenu.java"), goi);
        final String menu = doc("src/real/MapDoMenu.java");
        Kiem.sai("MapDoMenu khong con dung Party.checkConditionMarry (chi true/false)",
                menu.contains("checkConditionMarry"));
        final int dangKy = menu.indexOf("KetDoiGame.lyDoTuChoi(player)");
        final int ghiCap = menu.indexOf(".married = 1;");
        Kiem.dung("xet ket doi TRUOC khi ghi cap (married = 1)", dangKy > 0 && ghiCap > dangKy);
        final String map = doc("src/real/Map.java");
        Kiem.dung("Map chi nap bang nhom trung", map.contains("com.htbb.pokemon.KetDoi.nap();")
                && !map.contains("KetDoiGame"));
    }

    private static void quet(final File d, final List<String> goi) throws Exception {
        final File[] ds = d.listFiles();
        if (ds == null) {
            return;
        }
        Arrays.sort(ds);
        for (final File f : ds) {
            if (f.isDirectory()) {
                quet(f, goi);
            } else if (f.getName().endsWith(".java") && !f.getName().equals("KetDoi.java")
                    && !f.getName().equals("KetDoiGame.java")) {
                final String t = doc(f.getPath());
                if (t.contains("KetDoiGame") || t.contains("KetDoi.")) {
                    goi.add(f.getPath().replace('\\', '/'));
                }
            }
        }
    }

    private KiemKetDoi() {
    }
}
