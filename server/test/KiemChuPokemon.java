import com.htbb.pokemon.CheDoPokemon;
import com.htbb.pokemon.ChuPokemon;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * G7.C: chu nguoi choi thay khong con tu kiem hiep khi bat che do Pokemon.
 *
 * VI SAO: tu dien doi chu (ChuPokemon) co 3 ban chay song song - server Java, tool Python
 * (sinh SQL, quet tu cam) va client C# (ChuPokemonDuLieu.cs sinh tu Java). Lech nhau thi
 * client hien chu khac server ma khong bao loi. Them nua tu dien phai "khong lap" (doi hai
 * lan ra y het) vi client doi lai chu server da doi.
 */
public final class KiemChuPokemon {

    private static final String F_CS = "ClientUnity/Assets/Scripts/Game/ChuPokemonDuLieu.cs";
    private static final String F_MFONT = "ClientUnity/Assets/Scripts/Game/MFont.cs";
    private static final String F_SQL = "sql/pokemon_doi_chu.sql";
    private static final String F_CON_LAI = "docs/pokemon/tu_cam_con_lai.csv";
    private static final String F_CSV = "docs/pokemon/bang_chu.csv";
    private static final String F_TFIELD = "ClientUnity/Assets/Scripts/Game/TField.cs";

    /** Nhom "gio choi dau tien" - phai sach tu cam SAU khi doi (giong GIO_DAU cua quet_tu_cam.py). */
    private static final String[] GIO_DAU = {
        "src/data/Text.java", "src/real/CharManager.java", "src/real/cmd/LoginHandler.java",
        "src/real/cmd/SelectCharHandler.java", "src/real/cmd/RequestNPCInfoHandler.java",
        "src/real/MapMenuNpc.java", "src/real/MessageCreator.java", "src/real/MapBangHoi.java",
        "src/real/CharVongLap.java", "src/real/plugins/addNPC.java", "src/real/NpcReceiveCard.java",
        "ClientUnity/Assets/Scripts/Game/CreateCharScr.cs", "ClientUnity/Assets/Scripts/Game/CharSelectScr.cs",
        "ClientUnity/Assets/Scripts/Game/GameScr.cs", "ClientUnity/Assets/Scripts/Game/MenuWindows.cs",
        "ClientUnity/Assets/Scripts/Game/PauseMenu.cs", "ClientUnity/Assets/Scripts/Game/Res.cs",
    };

    public static void chay() {
        Kiem.nhom("Chu Pokemon (G7.C): tu dien, ap() theo co, ban C#, quet tu cam");
        kiemTuDien();
        kiemRaSoatChu();
        kiemChuHoaCoDau();
        kiemTatCo();
        kiemBatCo();
        kiemBanCs();
        kiemQuetGioDau();
        kiemSql();
    }

    private static void kiemTuDien() {
        Kiem.bang("vung", "Kanto, Johto, Hoenn",
                ChuPokemon.doiLuon("Thanh long, Hắc hổ, Huyền vũ"));
        Kiem.bang("doi", "Chỉ có đội trưởng mới có thể chọn vùng cho đội.",
                ChuPokemon.doiLuon("Chỉ có bang chủ mới có thể chọn lãnh thổ cho bang hội."));
        Kiem.bang("viet hoa het", "TOP ĐỘI", ChuPokemon.doiLuon("TOP BANG HỘI"));
        Kiem.bang("moi tu hoa", "Tinh Luyện Bảo Vật", ChuPokemon.doiLuon("Tinh Luyện Thần Binh"));
        Kiem.bang("thu cuoi theo ten", "Bạn đã nhận được thú cưỡi Glastrier",
                ChuPokemon.doiLuon("Bạn đã nhận được linh thú Sư tử Băng"));
        Kiem.bang("xa phu -> Bay", "Dùng Pidgeot (Bay)", ChuPokemon.doiLuon("Dùng Xa phu"));
        Kiem.bang("dong Pokemon", "Dòng Charmander", ChuPokemon.doiLuon("Kiếm khách"));
        Kiem.bang("dia danh phan biet hoa thuong", "biên giới Đường Victory",
                ChuPokemon.doiLuon("biên giới Biên Giới"));
        Kiem.bang("'kiếm' dong tu giu nguyen", "tìm kiếm, kiếm được", ChuPokemon.doiLuon("tìm kiếm, kiếm được"));
        Kiem.bang("Múa kiếm (ten chieu that) khong la tu cam", "[]", ChuPokemon.tuCamTrong("Múa kiếm").toString());
        Kiem.bang("khong khop giua tu (Bangkok)", "Bangkok", ChuPokemon.doiLuon("Bangkok"));
        // Moi tu cam (bo "=") phai bi bat
        int sot = 0;
        for (final String t : ChuPokemon.TU_CAM) {
            final String s = t.startsWith("=") ? t.substring(1) : t;
            if (ChuPokemon.tuCamTrong("x " + s + " y").isEmpty()) {
                sot++;
                System.out.println("        khong bat duoc tu cam: " + t);
            }
        }
        Kiem.bang("moi tu cam deu bat duoc", Integer.valueOf(0), Integer.valueOf(sot));
        // Khong lap + gia tri sach
        int lap = 0;
        int ban = 0;
        for (final String[] d : ChuPokemon.bangDayDu()) {
            if (!d[1].equals(ChuPokemon.doiLuon(d[1]))) {
                lap++;
                System.out.println("        doi lai lan 2 khac: " + d[1]);
            }
            final String k = d[0].startsWith("=") ? d[0].substring(1) : d[0];
            if (!ChuPokemon.tuCamTrong(ChuPokemon.doiLuon(k)).isEmpty()) {
                ban++;
                System.out.println("        doi xong van con tu cam: " + k + " -> " + ChuPokemon.doiLuon(k));
            }
        }
        Kiem.bang("tu dien khong lap (doi 2 lan = 1 lan)", Integer.valueOf(0), Integer.valueOf(lap));
        Kiem.bang("doi xong moi khoa deu sach tu cam", Integer.valueOf(0), Integer.valueOf(ban));
        Kiem.bang("TEN_DONG du 5 class", Integer.valueOf(real.CharManager.NAME_CLASS.length),
                Integer.valueOf(ChuPokemon.TEN_DONG.length));
        Kiem.bang("HUONG_DAN giu 21 dong", Integer.valueOf(21), Integer.valueOf(ChuPokemon.HUONG_DAN.length));
        int truongSai = 0;
        for (final String[] r : ChuPokemon.TEXT_RIENG) {
            try {
                data.Text.class.getDeclaredField(r[0]);
            } catch (final NoSuchFieldException e) {
                truongSai++;
                System.out.println("        TEXT_RIENG tro toi truong khong co: Text." + r[0]);
            }
        }
        Kiem.bang("TEXT_RIENG chi tro toi truong co that", Integer.valueOf(0), Integer.valueOf(truongSai));
    }

    /** Dot ra soat chu (docs/pokemon/DUYET_CHU.md): cac tu moi doi dung y, khong pha tu thuong. */
    private static void kiemRaSoatChu() {
        Kiem.bang("hoi thoai NPC: so dung truoc tu (1Giang hồ)", "1Thế giới Pokémon hiểm ác",
                ChuPokemon.doiLuon("1Giang hồ hiểm ác"));
        Kiem.bang("ngươi -> cậu", "Cậu có muốn rèn luyện không ?", ChuPokemon.doiLuon("Ngươi có muốn rèn luyện không ?"));
        Kiem.bang("giết -> hạ", "Hạ 10 con nhím", ChuPokemon.doiLuon("Giết 10 con nhím"));
        Kiem.bang("manh tu khoai", "Mảnh bộ huy hiệu", ChuPokemon.doiLuon("Mảnh tứ khoái"));
        Kiem.bang("manh trong bo", "Mảnh huy hiệu Cascade 1", ChuPokemon.doiLuon("Mảnh ngư 1"));
        Kiem.bang("ho ve lan su tu", "Pokémon hỗ trợ Suicune 5x lv 1", ChuPokemon.doiLuon("Hộ vệ lân sư tử 5x lv 1"));
        Kiem.bang("toc -> bom", "Bờm Ponyta", ChuPokemon.doiLuon("Tóc bờm ngựa"));
        Kiem.bang("than khi (moi tu hoa)", "Bảo Vật Huyền Thoại", ChuPokemon.doiLuon("Thần Khí"));
        Kiem.bang("tu hon dan", "Kẹo Poké", ChuPokemon.doiLuon("Tụ hồn đan"));
        Kiem.bang("noi tai -> dac tinh", "- Đặc tính: chặn", ChuPokemon.doiLuon("- Nội tại: chặn"));
        Kiem.bang("skin Kim Cang Long", "Skin thú cưỡi Dialga", ChuPokemon.doiLuon("Skin linh thú Kim Cang Long"));
        Kiem.bang("bo bat gioi", "Vòng tay Emboar", ChuPokemon.doiLuon("Kim cang bát giới"));
        Kiem.bang("ky nang pet Kim cang", "Thu mình", ChuPokemon.doiLuon("Kim cang"));
        Kiem.bang("thu vat ly / thu ma phap", "Phòng thủ: , Phòng thủ đặc biệt: ",
                ChuPokemon.doiLuon("Thủ vật lý: , Thủ ma pháp: "));
        Kiem.bang("van tieu -> giao hang", "Không thể dịch chuyển khi đang giao hàng",
                ChuPokemon.doiLuon("Không thể dịch chuyển khi đang vận tiêu"));
        Kiem.bang("'tiêu diệt' / 'mục tiêu' giu nguyen", "Hãy tiêu diệt mục tiêu", ChuPokemon.doiLuon("Hãy tiêu diệt mục tiêu"));
        Kiem.bang("chiem thanh -> Gym", "Thời gian chiếm Gym bắt đầu", ChuPokemon.doiLuon("Thời gian chiếm thành bắt đầu"));
        Kiem.bang("'hoàn thành' giu nguyen", "Nhiệm vụ hoàn thành", ChuPokemon.doiLuon("Nhiệm vụ hoàn thành"));
        Kiem.bang("dia danh viet thuong + huong", "Đông Rừng Viridian, Tây Rừng Viridian, Đông Saffron",
                ChuPokemon.doiLuon("Đông Sơn lâm, Tây Sơn lâm, Đông Đông sơn"));
        Kiem.bang("danh hieu", "Nhà Vô Địch Mọi Vùng", ChuPokemon.doiLuon("Thiên Hạ Chí Tôn"));
        Kiem.bang("thu cuoi mot minh (Bạch cốt mã)", "Spectrier thời hạn", ChuPokemon.doiLuon("Bạch cốt mã thời hạn"));
        Kiem.bang("pet Bạch Thử / thu cuoi Đại Bạch Thử", "Sandshrew, Wooloo",
                ChuPokemon.doiLuon("Bạch Thử, Đại Bạch Thử"));
        Kiem.bang("thoi trang", "Áo Rayquaza, Mặt nạ Snorlax", ChuPokemon.doiLuon("Áo Lạc long quân, Mặt nạ ông địa"));
        Kiem.bang("'thuỷ tinh' nguyen lieu giu nguyen", "Thuỷ tinh cấp 4", ChuPokemon.doiLuon("Thuỷ tinh cấp 4"));
        Kiem.bang("'hỏa thạch' khong thanh 'đá đá'", "đá nham thạch cấp 2", ChuPokemon.doiLuon("đá hỏa thạch cấp 2"));
        int lot = 0;
        for (final String t : new String[]{"Tóc bờm ngựa", "Thần Khí", "Tụ hồn đan", "Nội tại", "Kim Cang Long",
            "Phúc Thú", "Hộ vệ lân sư tử", "Mảnh tứ khoái", "Áo bát giới", "Ngươi", "Chiếm thành", "Vận tiêu"}) {
            if (ChuPokemon.tuCamTrong(t).isEmpty()) {
                lot++;
                System.out.println("        tu cu khong bi TU_CAM bat: " + t);
            }
        }
        Kiem.bang("cac ten cu da ra soat deu nam trong TU_CAM", Integer.valueOf(0), Integer.valueOf(lot));
        final String tf = doc(F_TFIELD);
        Kiem.dung("TField do chu NGUYEN VAN (khong qua tu dien) - con tro khong lech",
                tf != null && tf.contains("getWidthNguyenVan(") && !tf.contains("normalFont[0].getWidth(")
                        && !tf.contains("arialFont.getWidth("));
        final String csv = doc(F_CSV);
        final String sql = doc(F_SQL);
        if (csv != null && sql != null) {
            int soCsv = 0;
            for (final String d : csv.split("\n")) {
                if (d.matches("^(data_|tob_)\\w+,\\d+,.*")) {
                    soCsv++;
                }
            }
            int soSql = 0;
            for (final String d : sql.split("\n")) {
                if (d.startsWith("UPDATE ")) {
                    soSql++;
                }
            }
            Kiem.bang("bang_chu.csv ghi du moi o pokemon_doi_chu.sql doi", Integer.valueOf(soSql), Integer.valueOf(soCsv));
        }
    }

    /** Chu HOA thuoc U+1EA0..U+1EF8 - font client khong ve duoc (cung quy uoc KiemChuHoaCoDau). */
    private static String chuHoaCoDau(final String s) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c >= 0x1EA0 && c <= 0x1EF8 && Character.isUpperCase(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Moi gia tri moi cua tu dien (ca dang "Moi Tu Hoa" ma theoKieu() co the tao ra) va moi chu
     * trong pokemon_doi_chu.sql khong co chu HOA co dau.
     */
    private static void kiemChuHoaCoDau() {
        int sai = 0;
        for (final String[] d : ChuPokemon.bangDayDu()) {
            final StringBuilder hoaDau = new StringBuilder();
            boolean dauTu = true;
            for (int i = 0; i < d[1].length(); i++) {
                final char c = d[1].charAt(i);
                hoaDau.append(dauTu ? Character.toUpperCase(c) : c);
                dauTu = c == ' ';
            }
            final String x = chuHoaCoDau(d[1]) + chuHoaCoDau(hoaDau.toString());
            if (!x.isEmpty()) {
                sai++;
                System.out.println("        gia tri tu dien co chu HOA co dau [" + x + "]: " + d[0] + " -> " + d[1]);
            }
        }
        Kiem.bang("tu dien: khong gia tri nao (ke ca viet hoa dau tu) co chu HOA co dau", Integer.valueOf(0), Integer.valueOf(sai));
        final String sql = doc(F_SQL);
        int saiSql = 0;
        if (sql != null) {
            for (final String d : sql.split("\n")) {
                if (d.startsWith("UPDATE ") && !chuHoaCoDau(d).isEmpty()) {
                    saiSql++;
                    System.out.println("        SQL co chu HOA co dau: " + d.substring(0, Math.min(120, d.length())));
                }
            }
        }
        Kiem.bang("pokemon_doi_chu.sql: khong chu HOA co dau", Integer.valueOf(0), Integer.valueOf(saiSql));
    }

    private static void kiemTatCo() {
        final boolean cu = CheDoPokemon.bat();
        CheDoPokemon.dat(false);
        try {
            Kiem.bang("tat co: doi() tra nguyen", "bang hội", ChuPokemon.doi("bang hội"));
            ChuPokemon.ap();
            Kiem.bang("tat co: ap() khong doi NAME_CLASS", "Kiếm khách", real.CharManager.NAME_CLASS[0]);
            Kiem.bang("tat co: ap() khong doi nameCountry", "Thanh long", real.Map.nameCountry[0]);
            Kiem.bang("tat co: mo ta chieu giu nguyen", "abc", ChuPokemon.moTaChieuHoc(0, 0, "abc"));
        } finally {
            CheDoPokemon.dat(cu);
        }
    }

    private static void kiemBatCo() {
        final boolean cu = CheDoPokemon.bat();
        final String help0 = data.Text.help[0];
        CheDoPokemon.dat(true);
        try {
            ChuPokemon.ap();
            Kiem.bang("bat co: NAME_CLASS", Arrays.asList(ChuPokemon.TEN_DONG).toString(),
                    Arrays.asList(real.CharManager.NAME_CLASS).toString());
            Kiem.bang("bat co: Map.nameCountry", "[Kanto, Johto, Hoenn]", Arrays.asList(real.Map.nameCountry).toString());
            Kiem.bang("bat co: NpcReceiveCard.nameCountry", "[Kanto, Johto, Hoenn]",
                    Arrays.asList(real.NpcReceiveCard.nameCountry).toString());
            Kiem.bang("bat co: Text.help", ChuPokemon.HUONG_DAN[0], data.Text.help[0]);
            Kiem.bang("bat co: Text.HAC_NGUU", "Sáo gọi Tauros", data.Text.HAC_NGUU);
            Kiem.bang("bat co: Text.LENH_BAI_BANG_DCR", "Dùng nâng cấp đội", data.Text.LENH_BAI_BANG_DCR);
            Kiem.bang("bat co: mo ta chieu hoc", ChuPokemon.MO_TA_CHIEU_HOC[4][2], ChuPokemon.moTaChieuHoc(4, 2, "x"));
            Kiem.bang("bat co: doi() doi", "đội", ChuPokemon.doi("bang hội"));
            Kiem.bang("bat co: ten thu cuoi mot minh (khong nham NPC Machamp)", "Tauros", ChuPokemon.tenThuCuoi("Hắc ngưu"));
            Kiem.bang("bat co: NPC Hắc ngưu trong hoi thoai", "Gặp Machamp", ChuPokemon.doi("Gặp Hắc ngưu"));
            // Moi chuoi trong data.Text sach tu cam (tru ten chieu - ap_ten_chieu.py lo)
            final List<String> ban = new ArrayList<>();
            for (final Field f : data.Text.class.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                try {
                    final Object v = f.get(null);
                    final List<String> cac = new ArrayList<>();
                    if (v instanceof String) {
                        cac.add((String) v);
                    } else if (v instanceof String[]) {
                        cac.addAll(Arrays.asList((String[]) v));
                    } else if (v instanceof String[][]) {
                        for (final String[] a : (String[][]) v) {
                            cac.addAll(Arrays.asList(a));
                        }
                    }
                    for (final String s : cac) {
                        if (!ChuPokemon.tuCamTrong(s).isEmpty()) {
                            ban.add(f.getName() + ": " + s);
                        }
                    }
                } catch (final Exception e) {
                    ban.add(f.getName() + ": " + e);
                }
            }
            Kiem.bang("bat co: data.Text sach tu cam", "[]", ban.toString());
            ChuPokemon.ap(); // lan 2 khong doi them
            Kiem.bang("ap() goi 2 lan van vay", ChuPokemon.TEN_DONG[0], real.CharManager.NAME_CLASS[0]);
        } finally {
            ChuPokemon.hoanTac();
            CheDoPokemon.dat(cu);
        }
        Kiem.bang("hoanTac: NAME_CLASS ve cu", "Kiếm khách", real.CharManager.NAME_CLASS[0]);
        Kiem.bang("hoanTac: Text.help ve cu", help0, data.Text.help[0]);
        Kiem.bang("hoanTac: nameCountry ve cu", "Thanh long", real.Map.nameCountry[0]);
    }

    private static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return null;
        }
    }

    private static String boThoat(final String s) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                final char n = s.charAt(++i);
                sb.append(n == 'n' ? '\n' : n == 'r' ? '\r' : n == 't' ? '\t' : n);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** ChuPokemonDuLieu.cs (sinh tu Java boi sinh_sql_chu.py) phai y het bangDayDu(). */
    private static void kiemBanCs() {
        final String cs = doc(F_CS);
        Kiem.dung("co " + F_CS, cs != null);
        if (cs != null) {
            final Matcher m = Pattern.compile("new string\\[\\] \\{ \"((?:[^\"\\\\]|\\\\.)*)\", \"((?:[^\"\\\\]|\\\\.)*)\" \\}").matcher(cs);
            final List<String> c = new ArrayList<>();
            while (m.find()) {
                c.add(boThoat(m.group(1)) + "=>" + boThoat(m.group(2)));
            }
            final List<String> j = new ArrayList<>();
            for (final String[] d : ChuPokemon.bangDayDu()) {
                j.add(d[0] + "=>" + d[1]);
            }
            Kiem.bang("ban C# = ban Java (chay lai sinh_sql_chu.py neu lech)", j.toString(), c.toString());
        }
        final String mf = doc(F_MFONT);
        Kiem.dung("MFont._drawString doi chu qua ChuPokemon.D", mf != null && mf.contains("st = ChuPokemon.D(st);"));
    }

    // ---------- quet chuoi trong ma nguon (ban Java cua quet_tu_cam.py) ----------

    private static final Pattern LIT = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.)*)\"");
    private static final Pattern LOG = Pattern.compile("System\\.(out|err)|printStackTrace|saveOrtherLog|Debug\\.Log|Logdata|LOGGER|logger\\.");

    static String boChuThich(final String s) {
        final StringBuilder ra = new StringBuilder(s.length());
        int i = 0;
        final int n = s.length();
        while (i < n) {
            final char c = s.charAt(i);
            if (c == '"' || c == '\'') {
                int j = i + 1;
                while (j < n && s.charAt(j) != c && s.charAt(j) != '\n') {
                    j += s.charAt(j) == '\\' ? 2 : 1;
                }
                final int het = Math.min(j + 1, n);
                ra.append(s, i, het);
                i = het;
                continue;
            }
            if (s.startsWith("//", i)) {
                final int j = s.indexOf('\n', i);
                i = j < 0 ? n : j;
                continue;
            }
            if (s.startsWith("/*", i)) {
                int j = s.indexOf("*/", i + 2);
                j = j < 0 ? n : j + 2;
                for (int k = i; k < j; k++) {
                    if (s.charAt(k) == '\n') {
                        ra.append('\n');
                    }
                }
                i = j;
                continue;
            }
            ra.append(c);
            i++;
        }
        return ra.toString();
    }

    private static boolean giongChu(final String t) {
        if (t.isEmpty()) {
            return false;
        }
        for (int i = 0; i < t.length(); i++) {
            if (t.charAt(i) > 127) {
                return true;
            }
        }
        return Character.isUpperCase(t.charAt(0));
    }

    private static void kiemQuetGioDau() {
        int tho = 0;
        int con = 0;
        int thieu = 0;
        for (final String f : GIO_DAU) {
            final String s = doc(f);
            if (s == null) {
                thieu++;
                continue;
            }
            int so = 0;
            for (final String dong : boChuThich(s).split("\n", -1)) {
                so++;
                if (LOG.matcher(dong).find()) {
                    continue;
                }
                final Matcher m = LIT.matcher(dong);
                while (m.find()) {
                    final String t = boThoat(m.group(1));
                    if (!giongChu(t)) {
                        continue;
                    }
                    tho += ChuPokemon.tuCamTrong(t).size();
                    final List<String> sau = ChuPokemon.tuCamTrong(ChuPokemon.doiLuon(t));
                    if (!sau.isEmpty()) {
                        con += sau.size();
                        System.out.println("        " + f + ":" + so + " " + sau + " " + t);
                    }
                }
            }
        }
        Kiem.bang("file gio dau deu doc duoc", Integer.valueOf(0), Integer.valueOf(thieu));
        Kiem.dung("file gio dau co tu cam truoc khi doi (bo quet con chay that)", tho > 0);
        Kiem.bang("file gio dau: tu cam con lai SAU doi", Integer.valueOf(0), Integer.valueOf(con));
        final String csv = doc(F_CON_LAI);
        Kiem.dung("co " + F_CON_LAI + " (python3 -I tools/pokemon/quet_tu_cam.py)", csv != null);
        if (csv != null) {
            int gioDau = 0;
            for (final String dong : csv.split("\n")) {
                // cot gio_dau la cot ke cuoi; dong CSV co the chua dau phay trong ngoac kep
                if (dong.matches(".*,1,[^,]*$") && !dong.startsWith("nguon,")) {
                    gioDau++;
                }
            }
            Kiem.bang("tu_cam_con_lai.csv: khong o nao thuoc gio dau", Integer.valueOf(0), Integer.valueOf(gioDau));
        }
    }

    /** sql/pokemon_doi_chu.sql: chi UPDATE don gian, nhay don da thoat, gia tri sach tu cam. */
    private static void kiemSql() {
        final String s = doc(F_SQL);
        Kiem.dung("co " + F_SQL, s != null);
        if (s == null) {
            return;
        }
        final Pattern dung = Pattern.compile("^UPDATE (\\w+) SET (\\w+)='((?:[^'\\\\]|\\\\.)*)' WHERE id=\\d+;$");
        int sai = 0;
        int ban = 0;
        int n = 0;
        for (final String d : s.split("\n")) {
            if (d.isEmpty() || d.startsWith("--") || d.equals("SET NAMES utf8;")) {
                continue;
            }
            final Matcher m = dung.matcher(d);
            if (!m.matches()) {
                sai++;
                System.out.println("        dong SQL la: " + d);
                continue;
            }
            n++;
            if (!"name2".equals(m.group(2)) && !ChuPokemon.tuCamTrong(boThoat(m.group(3))).isEmpty()) {
                ban++;
                System.out.println("        con tu cam: " + d);
            }
        }
        Kiem.dung("pokemon_doi_chu.sql co UPDATE", n > 100);
        Kiem.bang("pokemon_doi_chu.sql chi gom UPDATE don gian", Integer.valueOf(0), Integer.valueOf(sai));
        Kiem.bang("pokemon_doi_chu.sql: gia tri moi sach tu cam", Integer.valueOf(0), Integer.valueOf(ban));
        Kiem.dung("thuoc HP doi ten", s.contains("UPDATE data_potion SET name='Thuốc hồi máu' WHERE id=1;"));
        Kiem.dung("thuoc MP doi ten", s.contains("UPDATE data_potion SET name='Ether' WHERE id=4;"));
    }
}
