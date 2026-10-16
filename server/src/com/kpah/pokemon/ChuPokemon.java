package com.kpah.pokemon;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * G7.C: doi chu nguoi choi nhin thay tu kiem hiep sang the gioi Pokemon (tu dien thuat ngu
 * o muc 15 cua docs/pokemon/KE_HOACH_CHUYEN_DOI.md).
 *
 * Cach lam: KHONG sua hang tram chuoi trong src/ (bo test characterization cu doc chu ma
 * nguon, sua chuoi la vo test). Thay vao do:
 *  - {@link #ap()} goi MOT lan luc bat may (TeamServer, sau khi nap template) doi cac bang
 *    chu dung chung: data.Text, ten vung, ten dong Pokemon (NAME_CLASS), ten thuoc...
 *  - {@link #doi(String)} doi chu o cac cua ngo gui chu xuong client (MessageCreator: thong
 *    bao, menu, hop thoai, NPC, nhiem vu...). Tat che do Pokemon thi tra nguyen chuoi.
 *
 * MOT NGUON cho ca 3 noi: tools/pokemon/sinh_sql_chu.py doc DOI/TEN_THU_CUOI/CHO_PHEP/TU_CAM
 * ngay trong file nay (regex tren ma nguon) de doi chu trong CSDL va sinh ban C# cho client
 * (ClientUnity/.../ChuPokemonDuLieu.cs). Sua bang o day roi chay lai tool, khong sua tay
 * hai file sinh ra. test/KiemChuPokemon doi chieu ban C# voi ban Java.
 *
 * Quy uoc bang: khoa viet thuong, so khop khong phan biet hoa thuong, nguyen tu (khong
 * khop giua mot tu khac). Khoa bat dau bang "=" la so khop PHAN BIET hoa thuong (vd "=Kiếm"
 * la ten vu khi, con "kiếm" viet thuong la dong tu "tìm/kiếm được" thi giu). Gia tri moi
 * KHONG duoc chua lai mot khoa nao (de doi hai lan ra cung ket qua - client cung doi lai
 * chu server da doi).
 */
public final class ChuPokemon {

    /**
     * Tu cam: con xuat hien trong chuoi nguoi choi thay la con sot. Dung boi
     * tools/pokemon/quet_tu_cam.py va test. "=" dau = phan biet hoa thuong.
     */
    public static final String[] TU_CAM = {
        "giang hồ", "võ lâm", "môn phái", "bang hội", "bang chủ", "phó bang", "bang",
        "lãnh thổ", "thanh long", "hắc hổ", "huyền vũ", "linh thú", "xa phu",
        "đại hiệp", "hiệp khách", "thiếu hiệp", "tiền bối", "nội công", "nội lực", "võ công",
        "thần binh", "bí kíp", "cao thủ", "kiếm khách", "kiếm hiệp", "kiếm sư", "kiếm sĩ",
        "kiếm sỹ", "thanh kiếm", "=Kiếm", "đao", "nhân vật", "ngũ hành", "huynh", "đệ tử",
        "sư phụ", "tại hạ", "nghĩa sỹ", "nghĩa sĩ", "tướng quân", "chiến binh", "pháp sư",
        "đấu sĩ", "cung thủ", "bôn tẩu", "hành tẩu",
    };

    /** Cum tu chua tu cam nhung la ten that trong Pokemon (giu nguyen). */
    public static final String[] CHO_PHEP = {
        "múa kiếm", // Swords Dance - ten chieu Pokemon tieng Viet
    };

    /** {cu, moi}. Thu tu khong quan trong: khoa dai duoc thu truoc. */
    public static final String[][] DOI = {
        // Doi (bang hoi)
        {"bang hội", "đội"},
        {"bang chủ", "đội trưởng"},
        {"chủ bang", "đội trưởng"},
        {"phó bang", "đội phó"},
        {"bang chúng", "thành viên đội"},
        {"quý bang", "quý đội"},
        {"trưởng lão", "cố vấn"},
        {"bang", "đội"},
        // Vung (lanh tho)
        {"lãnh thổ", "vùng"},
        {"thanh long", "Kanto"},
        {"hắc hổ", "Johto"},
        {"huyền vũ", "Hoenn"},
        // Thu cuoi
        {"linh thú", "thú cưỡi"},
        {"cưỡi linh thú", "cưỡi thú"},
        // Nhan vat / dong Pokemon
        {"nhân vật", "Pokémon"},
        {"môn phái", "dòng Pokémon"},
        {"kiếm khách", "dòng Charmander"},
        {"kiếm sĩ", "dòng Charmander"},
        {"kiếm sỹ", "dòng Charmander"},
        {"chiến binh", "dòng Bulbasaur"},
        {"pháp sư", "dòng Eevee"},
        {"đấu sĩ", "dòng Squirtle"},
        {"cung thủ", "dòng Pichu"},
        {"hiệp khách", "nhà thám hiểm"},
        {"đại hiệp", "nhà huấn luyện"},
        {"thiếu hiệp", "nhà huấn luyện trẻ"},
        {"nghĩa sỹ", "nhà huấn luyện"},
        {"nghĩa sĩ", "nhà huấn luyện"},
        {"làng nghĩa sỹ", "làng huấn luyện"},
        {"làng nghĩa sĩ", "làng huấn luyện"},
        {"cao thủ", "nhà vô địch"},
        {"tiền bối", "bậc đàn anh"},
        {"đệ tử", "học trò"},
        {"sư phụ", "thầy"},
        {"tại hạ", "tôi"},
        {"huynh", "anh"},
        {"tiểu đệ", "em"},
        // The gioi
        {"võ lâm chí tôn", "nhà vô địch Pokémon"},
        {"giang hồ", "thế giới Pokémon"},
        {"võ lâm", "thế giới Pokémon"},
        {"kiếm hiệp", "Pokémon"},
        {"bôn ba giang hồ", "phiêu lưu khắp thế giới Pokémon"},
        {"bôn tẩu", "phiêu lưu"},
        {"hành tẩu", "phiêu lưu"},
        // Suc manh / chieu
        {"nội công", "năng lượng"},
        {"nội lực", "năng lượng"},
        {"võ công", "chiêu thức"},
        {"bí kíp", "đĩa chiêu"},
        {"thần binh", "bảo vật"},
        {"hệ ngũ hành", "hệ"},
        // Ngu hanh cu trong mo ta (skin thu cuoi...) -> he Pokemon gan nhat
        {"hệ Mộc", "hệ Cỏ"},
        {"hệ Hỏa", "hệ Lửa"},
        {"hệ Hoả", "hệ Lửa"},
        {"hệ Thổ", "hệ Đất"},
        {"hệ Kim", "hệ Thép"},
        {"hệ Thủy", "hệ Nước"},
        {"hệ Thuỷ", "hệ Nước"},
        {"ngũ hành", "hệ"},
        // Vu khi cu (bang 6.4: Kiem -> Than lua, Dao -> Hat giong than)
        {"=Kiếm", "Than lửa"},
        {"thanh kiếm", "lưỡi năng lượng"},
        {"đại đao", "hạt giống thần"},
        {"đao", "hạt giống thần"},
        {"thần kiếm", "thần hỏa"},
        // NPC (bang docs/pokemon/bang_npc.csv)
        {"xa phu", "Pidgeot (Bay)"},
        {"thần hành phù", "vé bay Pidgeot"},
        {"kim bài", "huy hiệu lập đội"},
        {"lâm tướng quân", "Blaziken"},
        {"tướng quân", "chỉ huy"},
        {"trần thống lĩnh", "Heracross"},
        {"kiếm sư", "Farfetch'd"},
        {"dì út hp", "Y tá Chansey"},
        {"dì út", "Y tá Chansey"},
        {"bà tám tạp hóa", "Kecleon tạp hoá"},
        {"bà tám tạp hoá", "Kecleon tạp hoá"},
        {"bà tám", "Kecleon"},
        {"hắc ngưu", "Machamp"},
        {"thiết bì", "Bronzong"},
        {"phú ông", "Persian"},
        {"phú ồng", "Persian"},
        {"anh bảy", "Kangaskhan"},
        {"nhật thương nhân", "Meowth thương nhân"},
        {"hỏa xích", "Torkoal"},
        {"hoả xích", "Torkoal"},
        // Ngoc (ep ngoc)
        {"ép ngọc", "ép ngọc hệ"},
        {"ép ngọc hệ", "ép ngọc hệ"}, // giu nguyen - de doi 2 lan khong thanh "ép ngọc hệ hệ"
    };

    /** Ten thu cuoi cu -> Pokemon (docs/pokemon/bang_thu.csv). Dung sau "linh thú "/"trứng ". */
    public static final String[][] TEN_THU_CUOI = {
        {"bạch mã", "Rapidash"},
        {"mãnh hổ", "Arcanine"},
        {"hắc ngưu", "Tauros"},
        {"sói xám", "Mightyena"},
        {"tiên hạc", "Pidgeot"},
        {"phượng hoàng lửa", "Moltres"},
        {"phượng hoàng băng", "Articuno"},
        {"phượng hoàng đa sắc", "Ho-Oh"},
        {"phượng hoàng mộc", "Tropius"},
        {"phượng hoàng kim", "Skarmory"},
        {"phượng hoàng thổ", "Flygon"},
        {"phượng hoàng", "Moltres"},
        {"tuần lộc", "Stantler"},
        {"đại bạch thử", "Wooloo"},
        {"bạch thử", "Wooloo"},
        {"hắc thỏ", "Lopunny"},
        {"lân sư tử", "Suicune"},
        {"lân", "Suicune"},
        {"đương khang băng giá", "Mamoswine"},
        {"đương khang", "Piloswine"},
        {"bạch cốt mã", "Spectrier"},
        {"bạch cốt", "Spectrier"},
        {"rồng băng", "Dragonite"},
        {"sư tử băng", "Glastrier"},
        {"sư tử", "Entei"},
        {"hoả kỳ lân", "Ponyta"},
        {"hỏa kỳ lân", "Ponyta"},
    };

    /** Ten NPC tinh theo type 0..31 (thu tu NPC.cs / bang_npc.csv). */
    public static final String[] TEN_NPC = {
        "Y tá Chansey", "Kecleon tạp hoá", "Machamp thợ rèn", "Bronzong thợ giáp", "Arcanine gác cổng",
        "Trưởng làng Xatu", "Persian", "Pidgeot (Bay)", "Ông Snorlax", "Kangaskhan thủ kho",
        "Lapras hoa tiêu", "Golem 1", "Golem 2", "Golem 3", "Golem 4",
        "Golem 5", "Tauros 1", "Tauros 2", "Tauros 3", "Tauros 4",
        "Tauros 5", "Blaziken", "Meowth thương nhân", "Torkoal", "Sableye",
        "Heracross", "Kecleon", "Bronzong", "Farfetch'd", "Persian",
        "Kangaskhan", "Meowth",
    };

    /** CharManager.NAME_CLASS khi bat che do (class 0..4, bang 4.1). */
    public static final String[] TEN_DONG = {
        "Dòng Charmander", "Dòng Bulbasaur", "Dòng Eevee", "Dòng Squirtle", "Dòng Pichu",
    };

    /** Map.nameCountry / NpcReceiveCard.nameCountry khi bat che do. */
    public static final String[] TEN_VUNG = {"Kanto", "Johto", "Hoenn"};

    /** Text.help khi bat che do (giu 21 dong nhu ban cu). */
    public static final String[] HUONG_DAN = {
        "Phím mũi tên hoặc 2,4,6,8: Di chuyển Pokémon của bạn",
        "Phím 5 hoặc phím chọn giữa: Đánh thường. Phím 1,3: Dùng chiêu khác nếu đã học",
        "Phím 7: Dùng Thuốc hồi máu, Phím 9: Dùng Ether (nếu còn)",
        "Phím chọn trái: Mở Menu",
        "Phím chọn phải: đổi mục tiêu nếu có nhiều đối tượng xung quanh.",
        "Phím #: Chat.",
        "Phím *: mở menu mời nhóm, trao đổi, nhắn tin, kết bạn, xem thông tin, xuống thú cưỡi.",
        "Phím 0: chuyển đổi bảng chiêu.",
        "Bạn phải làm xong nhiệm vụ chào Trưởng làng (nhiệm vụ đầu tiên) mới có thể đấu với Pokémon hoang dã.",
        "Bạn có thể mua vật phẩm đặc biệt bằng cách vào Menu chọn Cửa Hàng.",
        "Bạn có thể mua vật phẩm tại các NPC trong làng.",
        "Khi lên cấp bạn được thưởng 5 điểm tiềm năng và 1 điểm kỹ năng.",
        "Hãy vào mục Bản thân để cộng điểm theo ý muốn.",
        "Pokémon của bạn dưới cấp 10 được bảo hộ và không dùng được chức năng PK.",
        "Hạ 1 người chơi khi PK sẽ bị cộng 100 điểm PK.",
        "Muốn giảm điểm PK, hãy dùng Phiếu công ích mua ở Cửa Hàng hoặc hạ Pokémon hoang dã.",
        "Bạn chỉ có thể nâng cấp vật phẩm khi Pokémon đạt cấp 10.",
        "Đeo khăn PK để bật chức năng chiến đấu.",
        "Khi vật phẩm bị hư hại, hãy đến Machamp thợ rèn để sửa.",
        "Để di chuyển nhanh giữa các vùng, hãy nhờ Pidgeot (Bay).",
        "Để ra đảo, hãy đến bến tàu mua vé và đi tàu.",
    };

    /** Mo ta 3 chieu hoc o NPC theo dong [class][k] (cung thu tu Text.nameSkillLearn). */
    public static final String[][] MO_TA_CHIEU_HOC = {
        {"Phun luồng lửa dữ dội thiêu đốt nhiều mục tiêu trong phạm vi.",
         "Vuốt móng sắc mang sức mạnh rồng, đánh liên tiếp các đối thủ đứng gần nhau.",
         "Phóng ngọn lửa hình chữ Đại thiêu rụi mọi mục tiêu trong vùng ảnh hưởng."},
        {"Bắn loạt hạt giống cứng tấn công nhiều mục tiêu trong phạm vi.",
         "Cuốn lá sắc thành cơn bão xoáy, đánh trúng mọi đối thủ trên đường đi.",
         "Hấp thụ ánh nắng rồi bắn tia năng lượng mặt trời gây sát thương lớn."},
        {"Quật đuôi cứng như thép vào nhiều mục tiêu xung quanh.",
         "Dồn toàn bộ sức lực lao vào đối thủ tạo cú va chạm cực mạnh.",
         "Bắn tia năng lượng hủy diệt quét qua các mục tiêu phía trước."},
        {"Quất đuôi tạo sóng nước tấn công nhiều mục tiêu trong phạm vi.",
         "Phun dòng nước áp lực cao xoáy tròn công phá đối thủ.",
         "Bắn pháo nước khổng lồ cuốn trôi mọi mục tiêu xung quanh."},
        {"Tung lưới điện bủa vây, gây sát thương nhiều mục tiêu.",
         "Gọi sấm sét giáng xuống, tia điện lan sang các đối thủ đứng gần nhau.",
         "Bọc mình trong điện rồi lao thẳng vào đối thủ với sức công phá khủng khiếp."},
    };

    /** Ten vat pham trong data.Text doi rieng (doi theo tu dien khong du hay). {ten truong, gia tri}. */
    public static final String[][] TEXT_RIENG = {
        {"HP_VUA", " Siêu thuốc hồi máu"},
        {"HP_TO", " Thuốc hồi máu cực mạnh"},
        {"HP_NHO", " Thuốc hồi máu cực mạnh"},
        {"MP_VUA", " Ether lớn"},
        {"MP_TO", " Ether tối đa"},
        {"THAN_HANH_PHU", "Vé bay Pidgeot"},
        {"THIEN_LY_MA", "Sáo gọi Ponyta"},
        {"KIM_BAI", "Huy hiệu lập đội"},
        {"KIM_BAI_DECRIPT", "Huy hiệu dùng để tạo đội"},
        {"XICH_THO", "Sáo gọi Ponyta lửa"},
        {"HAC_NGUU", "Sáo gọi Tauros"},
        {"MANH_HO", "Sáo gọi Arcanine"},
        {"SOI_XAM", "Sáo gọi Mightyena"},
        {"TIEN_HAC", "Sáo gọi Pidgeot"},
        {"BACH_MA", "Sáo gọi Rapidash"},
        {"THUOC_CUONG_HOA", "Thuốc X"},
        {"THUOC_CUONG_HOA_SUPER", "Thuốc X siêu cấp"},
        {"HP7000", "200 Thuốc hồi máu 7000"},
        {"HP15000", "200 Thuốc hồi máu 15000"},
        {"MP7000", "200 Ether 7000"},
        {"MP15000", "200 Ether 15000"},
    };

    private static final String PHAN_BIET = "=";

    /** Ket qua bien dich bang: mau regex + hai bang tra. */
    private static final class Bang {
        final Pattern mau;
        final HashMap<String, String> khongPhanBiet = new HashMap<>();
        final HashMap<String, String> phanBiet = new HashMap<>();

        Bang(final List<String[]> dong) {
            final List<String[]> sx = new ArrayList<>(dong);
            // Khoa dai thu truoc (alternation cua regex lay nhanh dau tien khop).
            Collections.sort(sx, new Comparator<String[]>() {
                @Override
                public int compare(final String[] a, final String[] b) {
                    return khoa(b[0]).length() - khoa(a[0]).length();
                }
            });
            final StringBuilder sb = new StringBuilder("(?<![\\p{L}\\p{N}_])(?:");
            boolean dau = true;
            for (final String[] d : sx) {
                final String k = khoa(d[0]);
                if (d[0].startsWith(PHAN_BIET)) {
                    if (phanBiet.containsKey(k)) {
                        continue;
                    }
                    phanBiet.put(k, d[1]);
                    sb.append(dau ? "" : "|").append("(?-i:").append(Pattern.quote(k)).append(')');
                } else {
                    final String t = k.toLowerCase(Locale.ROOT);
                    if (khongPhanBiet.containsKey(t)) {
                        continue;
                    }
                    khongPhanBiet.put(t, d[1]);
                    sb.append(dau ? "" : "|").append(Pattern.quote(k));
                }
                dau = false;
            }
            sb.append(")(?![\\p{L}\\p{N}_])");
            mau = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
    }

    private static volatile Bang bangDoi;
    private static volatile Bang bangCam;

    private ChuPokemon() {
    }

    private static String khoa(final String s) {
        return s.startsWith(PHAN_BIET) ? s.substring(1) : s;
    }

    /**
     * Toan bo cap {cu, moi} theo dung thu tu ghep (tool Python va ban C# ghep y het):
     * CHO_PHEP (giu nguyen), DOI, "linh thú X"/"trứng X" cua TEN_THU_CUOI, roi ten dia danh
     * TenMapPokemon.BANG (phan biet hoa thuong: "Biên Giới" la dia danh, "biên giới" thi khong).
     */
    public static List<String[]> bangDayDu() {
        final List<String[]> d = new ArrayList<>();
        for (final String p : CHO_PHEP) {
            d.add(new String[]{p, p});
        }
        for (final String[] x : DOI) {
            d.add(new String[]{x[0], x[1]});
        }
        for (final String[] x : TEN_THU_CUOI) {
            d.add(new String[]{"linh thú " + x[0], "thú cưỡi " + x[1]});
            d.add(new String[]{"trứng " + x[0], "trứng " + x[1]});
        }
        for (final String[] x : TenMapPokemon.BANG) {
            d.add(new String[]{PHAN_BIET + x[0], x[1]});
        }
        return d;
    }

    private static Bang bangDoi() {
        Bang b = bangDoi;
        if (b == null) {
            b = new Bang(bangDayDu());
            bangDoi = b;
        }
        return b;
    }

    private static Bang bangCam() {
        Bang b = bangCam;
        if (b == null) {
            final List<String[]> d = new ArrayList<>();
            for (final String p : CHO_PHEP) {
                d.add(new String[]{p, ""});
            }
            for (final String t : TU_CAM) {
                d.add(new String[]{t, t});
            }
            b = new Bang(d);
            bangCam = b;
        }
        return b;
    }

    /**
     * Chu hoa theo kieu cua doan goc: VIET HOA het -> viet hoa het; Moi Tu Hoa (>= 2 tu) ->
     * moi tu cua gia tri moi hoa chu dau; chu dau hoa -> chu dau hoa. Ban Python/C# lam y het.
     */
    private static String theoKieu(final String goc, final String moi) {
        if (moi.isEmpty()) {
            return moi;
        }
        final String[] tuGoc = goc.split(" ");
        boolean moiTuHoa = tuGoc.length >= 2;
        for (final String t : tuGoc) {
            if (t.isEmpty() || !Character.isUpperCase(t.charAt(0))) {
                moiTuHoa = false;
            }
        }
        boolean coChu = false;
        boolean hetHoa = true;
        for (int i = 0; i < goc.length(); i++) {
            final char c = goc.charAt(i);
            if (Character.isLetter(c)) {
                coChu = true;
                if (!Character.isUpperCase(c)) {
                    hetHoa = false;
                }
            }
        }
        if (coChu && hetHoa && goc.length() > 1) {
            return moi.toUpperCase(Locale.ROOT);
        }
        if (moiTuHoa) {
            final StringBuilder sb = new StringBuilder(moi.length());
            boolean dauTu = true;
            for (int i = 0; i < moi.length(); i++) {
                final char c = moi.charAt(i);
                sb.append(dauTu ? Character.toUpperCase(c) : c);
                dauTu = c == ' ';
            }
            return sb.toString();
        }
        if (Character.isUpperCase(goc.charAt(0)) && Character.isLowerCase(moi.charAt(0))) {
            return Character.toUpperCase(moi.charAt(0)) + moi.substring(1);
        }
        return moi;
    }

    /** Doi chu bat ke co (cho tool/test). null/rong tra nguyen. */
    public static String doiLuon(final String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        final Bang b = bangDoi();
        final Matcher m = b.mau.matcher(s);
        if (!m.find()) {
            return s;
        }
        final StringBuffer sb = new StringBuffer(s.length() + 16);
        do {
            final String goc = m.group();
            String moi = b.phanBiet.get(goc);
            if (moi == null) {
                moi = b.khongPhanBiet.get(goc.toLowerCase(Locale.ROOT));
                moi = moi == null ? goc : theoKieu(goc, moi);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(moi));
        } while (m.find());
        m.appendTail(sb);
        return sb.toString();
    }

    /** Doi chu khi bat che do Pokemon; tat thi tra nguyen (hanh vi KPAH cu). */
    public static String doi(final String s) {
        return CheDoPokemon.bat() ? doiLuon(s) : s;
    }

    /** Cac tu cam con trong {@code s} (rong = sach). */
    public static List<String> tuCamTrong(final String s) {
        final List<String> kq = new ArrayList<>();
        if (s == null || s.isEmpty()) {
            return kq;
        }
        final Bang b = bangCam();
        final Matcher m = b.mau.matcher(s);
        while (m.find()) {
            final String g = m.group();
            if (b.phanBiet.containsKey(g)) {
                kq.add(g);
                continue;
            }
            final String v = b.khongPhanBiet.get(g.toLowerCase(Locale.ROOT));
            if (v != null && !v.isEmpty()) {
                kq.add(g);
            }
        }
        return kq;
    }

    /** Mo ta chieu hoc o NPC (goi dang nhap); tat che do hoac ngoai bang thi tra {@code cu}. */
    public static String moTaChieuHoc(final int dong, final int k, final String cu) {
        if (!CheDoPokemon.bat() || dong < 0 || dong >= MO_TA_CHIEU_HOC.length
                || k < 0 || k >= MO_TA_CHIEU_HOC[dong].length) {
            return doi(cu);
        }
        return MO_TA_CHIEU_HOC[dong][k];
    }

    /**
     * Ten thu cuoi dung MOT MINH (Animal.name: "Hắc ngưu", "Sư tử Băng"...) -> ten Pokemon.
     * Phai tra rieng vi trong tu dien "hắc ngưu" dung mot minh la NPC Machamp; chi khi co
     * "linh thú " dang truoc moi la thu cuoi. Tat che do thi tra nguyen.
     */
    public static String tenThuCuoi(final String ten) {
        if (!CheDoPokemon.bat() || ten == null) {
            return ten;
        }
        final String t = ten.trim().toLowerCase(Locale.ROOT);
        for (final String[] d : TEN_THU_CUOI) {
            if (d[0].equals(t)) {
                return d[1];
            }
        }
        return doiLuon(ten);
    }

    /** Ten NPC tinh moi theo type; ngoai bang thi doi theo tu dien. */
    public static String tenNpc(final int type, final String cu) {
        if (!CheDoPokemon.bat()) {
            return cu;
        }
        return type >= 0 && type < TEN_NPC.length ? TEN_NPC[type] : doiLuon(cu);
    }

    // ==================== AP MOT LAN LUC BAT MAY ====================

    private static final List<Runnable> HOAN_TAC = new ArrayList<>();
    private static boolean daAp = false;

    /**
     * Doi cac bang chu dung chung. Goi MOT lan trong TeamServer, SAU Database.startSaveChar()
     * (luc do Map.gemTemplate, Map.potionTemplates2 da nap) va truoc khi nhan nguoi choi.
     * Tat che do thi khong lam gi. Goi lai lan 2 khong doi them (co daAp).
     */
    public static synchronized void ap() {
        if (!CheDoPokemon.bat() || daAp) {
            return;
        }
        daAp = true;
        apText();
        datMang(real.CharManager.NAME_CLASS, TEN_DONG);
        datMang(real.Map.nameCountry, TEN_VUNG);
        datMang(real.NpcReceiveCard.nameCountry, TEN_VUNG);
        apTenThuoc();
        apNgoc();
    }

    /** Chi test dung: tra moi thu ap() da doi ve nhu cu. */
    public static synchronized void hoanTac() {
        for (int i = HOAN_TAC.size() - 1; i >= 0; i--) {
            HOAN_TAC.get(i).run();
        }
        HOAN_TAC.clear();
        daAp = false;
    }

    private static void datMang(final String[] dich, final String[] moi) {
        if (dich == null) {
            return;
        }
        final String[] cu = dich.clone();
        for (int i = 0; i < dich.length && i < moi.length; i++) {
            dich[i] = moi[i];
        }
        HOAN_TAC.add(new Runnable() {
            @Override
            public void run() {
                System.arraycopy(cu, 0, dich, 0, cu.length);
            }
        });
    }

    private static void doiMang(final String[] a) {
        if (a == null) {
            return;
        }
        final String[] cu = a.clone();
        for (int i = 0; i < a.length; i++) {
            a[i] = doiLuon(a[i]);
        }
        HOAN_TAC.add(new Runnable() {
            @Override
            public void run() {
                System.arraycopy(cu, 0, a, 0, cu.length);
            }
        });
    }

    private static void apText() {
        final HashMap<String, String> rieng = new HashMap<>();
        for (final String[] r : TEXT_RIENG) {
            rieng.put(r[0], r[1]);
        }
        for (final Field f : data.Text.class.getDeclaredFields()) {
            final int md = f.getModifiers();
            if (!Modifier.isStatic(md) || Modifier.isFinal(md)) {
                continue;
            }
            // Ten chieu da doi rieng (tools/pokemon/ap_ten_chieu.py) - khong dong vao.
            if ("nameSkillLearn".equals(f.getName())) {
                continue;
            }
            try {
                f.setAccessible(true);
                final Object cu = f.get(null);
                if (f.getType() == String.class) {
                    final String moi = rieng.containsKey(f.getName()) ? rieng.get(f.getName()) : doiLuon((String) cu);
                    if (moi != null && !moi.equals(cu)) {
                        f.set(null, moi);
                        HOAN_TAC.add(datLai(f, cu));
                    }
                } else if (f.getType() == String[].class) {
                    if ("help".equals(f.getName())) {
                        f.set(null, HUONG_DAN.clone());
                        HOAN_TAC.add(datLai(f, cu));
                    } else {
                        doiMang((String[]) cu);
                    }
                } else if (f.getType() == String[][].class && cu != null) {
                    for (final String[] a : (String[][]) cu) {
                        doiMang(a);
                    }
                }
            } catch (final Exception e) {
                System.out.println("[CHU POKEMON] khong doi duoc Text." + f.getName() + ": " + e);
            }
        }
    }

    private static Runnable datLai(final Field f, final Object cu) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    f.set(null, cu);
                } catch (final Exception e) {
                    // chi test goi
                }
            }
        };
    }

    /**
     * LoginHandler.PORTION_NAME (ten thuoc trong thong bao server) lay theo ten CSDL da doi
     * (sql/pokemon_doi_chu.sql), roi qua tu dien (phong khi chua chay SQL).
     */
    private static void apTenThuoc() {
        final String[] ten = real.cmd.LoginHandler.PORTION_NAME;
        if (ten == null) {
            return;
        }
        final String[] cu = ten.clone();
        final java.util.Vector<real.PotionTemplate2> csdl = real.Map.potionTemplates2;
        if (csdl != null) {
            for (final real.PotionTemplate2 p : csdl) {
                if (p != null && p.id >= 0 && p.id < ten.length && p.name != null) {
                    ten[p.id] = p.name;
                }
            }
        }
        for (int i = 0; i < ten.length; i++) {
            ten[i] = doiLuon(ten[i]);
        }
        HOAN_TAC.add(new Runnable() {
            @Override
            public void run() {
                System.arraycopy(cu, 0, ten, 0, cu.length);
            }
        });
    }

    /** Map.gemTemplate (ngoc/da) da chep chu tu Text LUC NAP - doi lai ban chep. */
    private static void apNgoc() {
        final real.GemTemplate[] g = real.Map.gemTemplate;
        if (g == null) {
            return;
        }
        for (final real.GemTemplate t : g) {
            if (t == null) {
                continue;
            }
            final String ten = t.name;
            final String moTa = t.decript;
            t.name = doiLuon(ten);
            t.decript = doiLuon(moTa);
            HOAN_TAC.add(new Runnable() {
                @Override
                public void run() {
                    t.name = ten;
                    t.decript = moTa;
                }
            });
        }
    }
}
