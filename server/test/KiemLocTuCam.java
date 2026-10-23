import com.htbb.pokemon.LocTuCam;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bao cao QA #7 (0.9.14): bo loc tu cam theo CA TU cho ten (nhan vat / doi ten / ten doi) va che chat. Danh sach lay
 * tu sql/htbb.sql (phan A bad_word, tru dong khoi loc_tu_cam_ca_tu.sql xoa) - giong CSDL that sau khi nang cap.
 */
public final class KiemLocTuCam {

    private KiemLocTuCam() {
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    /** Tu trong INSERT bad_word phan A, tru cac tu bi DELETE o khoi loc_tu_cam_ca_tu.sql. */
    static List<String> danhSachSql() {
        final String sql = doc("../sql/htbb.sql");
        final List<String> tu = new ArrayList<String>();
        final int a = sql.indexOf("INSERT INTO `bad_word` VALUES ");
        if (a < 0) {
            return tu;
        }
        final String dong = sql.substring(a, sql.indexOf('\n', a));
        final Matcher m = Pattern.compile("\\(\\d+,'((?:[^'\\\\]|\\\\.)*)'\\)").matcher(dong);
        while (m.find()) {
            tu.add(m.group(1).replace("\\'", "'"));
        }
        final int b = sql.indexOf("-- >>> loc_tu_cam_ca_tu.sql");
        if (b >= 0) {
            final Matcher d = Pattern.compile("DELETE FROM `bad_word` WHERE `word` IN \\(([^)]*)\\)").matcher(sql.substring(b));
            if (d.find()) {
                for (final String x : d.group(1).split(",")) {
                    tu.remove(x.trim().replace("'", ""));
                }
            }
            final String khoi = sql.substring(b);
            final int c = khoi.indexOf("INSERT INTO `bad_word`");
            if (c >= 0) {
                final Matcher t = Pattern.compile("SELECT '([a-z]+)'").matcher(khoi.substring(c, khoi.indexOf('\n', c)));
                while (t.find()) {
                    tu.add(t.group(1));
                }
            }
        }
        return tu;
    }

    public static void chay() {
        Kiem.nhom("Loc tu cam ca tu (QA #7)");
        final List<String> tu = danhSachSql();
        Kiem.dung("doc duoc bad_word tu sql (" + tu.size() + " tu)", tu.size() > 100);
        for (final String bo : new String[]{"phong", "tay", "ad", "lau", "chay", "chai", "vat", "game", "test", "sv", "vv", "suc"}) {
            Kiem.dung("da bo tu khong tuc '" + bo + "'", !tu.contains(bo));
        }
        Kiem.dung("con giu 'ngu'", tu.contains("ngu"));
        Kiem.dung("sql co cot kieu", doc("../sql/htbb.sql").contains("ALTER TABLE `bad_word` ADD COLUMN IF NOT EXISTS `kieu`"));
        data.Database.dungLoc(tu, null);

        // ten that (bot qa_ten_pho_bien + bao cao QA): phai qua
        for (final String ten : new String[]{"Nguyenvu", "Phongvu", "Shadow", "Gyarados", "Kadabra", "Tayson", "Bichvan",
            "NguyenVu", "Nguyen99", "Ditto", "Dragonite", "Thanhtay", "Vutay", "Laura", "Sungame", "Testing", "Suckhoe",
            "Phongthu", "Advent", "Hadley", "Chaydi", "Chaika", "Vatly", "Sven", "Lavvy", "Edith", "Mewtwo", "Abra",
            "Moderne", "Admiral", "Sexton", "Pikachu", "Bulbasaur", "Charizard", "Machamp", "Hoang", "Tuan2k"}) {
            final String cam = data.Database.tuCamTrongTen(ten);
            Kiem.dung("ten that '" + ten + "' qua" + (cam == null ? "" : " (bi chan «" + cam + "»)"),
                    cam == null && real.cmd.CreateCharHandler.loiTen(ten) == null);
        }
        // tuc tiu / bien the: phai chan
        for (final String ten : new String[]{"Ngu", "Ngu123", "ThangNgu", "Thangngu", "NguVl", "Ditme", "D1tme", "Dit",
            "Ditcon", "Concac", "Cailon", "Dkm123", "Vcl2k", "Dmmtuan", "DmTuan", "Dcm", "Dume", "Djtme", "Xaolon", "Nigger", "Duma", "Dumamay", "Fuck", "Fuckyou",
            "Shitty", "Bitch", "Pussy", "Lonme", "Occho", "Admin", "Admin01", "GmAdmin", "Mod", "ModVip", "Adm", "Hack",
            "Hacker", "Giftcode", "Teamobi", "Sex", "Sex99", "Xxx", "Loz", "Hentai", "Dis", "Chetme", "Iwin", "Htbb"}) {
            final String cam = data.Database.tuCamTrongTen(ten);
            Kiem.dung("ten tuc / cam '" + ten + "' bi chan" + (cam == null ? "" : " «" + cam + "»"), cam != null);
        }
        // cau bao noi ro phan bi cam
        Kiem.bang("cau bao ThangNgu", "Tên có từ không phù hợp: «Ngu». Hãy chọn tên khác.", real.cmd.CreateCharHandler.loiTen("ThangNgu"));
        Kiem.bang("cau bao Ditme", "Tên có từ không phù hợp: «Ditme». Hãy chọn tên khác.", real.cmd.CreateCharHandler.loiTen("Ditme"));
        Kiem.bang("cau bao D1tme", "Tên có từ không phù hợp: «D1tme». Hãy chọn tên khác.", real.cmd.CreateCharHandler.loiTen("D1tme99"));
        Kiem.bang("cau bao do dai", "Tên Pokémon phải từ 4 đến 10 ký tự.", real.cmd.CreateCharHandler.loiTen("Pikachuxxxx1"));
        Kiem.dung("cau bao ky tu", String.valueOf(real.cmd.CreateCharHandler.loiTen("Pika chu")).contains("không dấu"));
        // doi ten (co dau, co dau cach) / ten doi
        Kiem.dung("doi ten 'Nguyễn Vũ' qua", data.Database.tuCamTrongTen("Nguyễn Vũ") == null);
        Kiem.dung("doi ten 'Phong Vũ' qua", data.Database.tuCamTrongTen("Phong Vũ") == null);
        Kiem.bang("doi ten 'thằng ngu' chan", "ngu", data.Database.tuCamTrongTen("thằng ngu"));
        Kiem.bang("doi ten 'Du Ma' chan ca hai tu", "Du Ma", data.Database.tuCamTrongTen("Du Ma"));
        Kiem.dung("doi ten 'd i t m e' chan", data.Database.tuCamTrongTen("d i t m e") != null);

        // che chat (bang bad_word): tu ngan chi che ca tu, tu dai / co dau van che
        Kiem.bang("chat Nguyen khong bi che", "Chào Nguyễn Vũ, Shadow ở Phòng Gym", data.Database.locChat.che("Chào Nguyễn Vũ, Shadow ở Phòng Gym", "*"));
        Kiem.bang("chat 'ngu' ca tu bi che", "đồ * quá", data.Database.locChat.che("đồ ngu quá", "*"));
        Kiem.bang("chat 'NGU' hoa bi che", "đồ * quá", data.Database.locChat.che("đồ NGU quá", "*"));
        Kiem.bang("chat 'dm' che", "* nha", data.Database.locChat.che("dm nha", "*"));
        Kiem.bang("chat 'and more' khong thanh dm", "and more", data.Database.locChat.che("and more", "*"));
        Kiem.bang("chat 'd m' cach che", "* m", data.Database.locChat.che("d m m", "*"));
        Kiem.bang("chat '.com' che", "vào abc*", data.Database.locChat.che("vào abc.com", "*"));
        Kiem.bang("chat 'cặc' che, 'các' khong", "* các bạn", data.Database.locChat.che("cặc các bạn", "*"));
        Kiem.bang("chat 'lớn' khong bi che", "Pokémon lớn", data.Database.locChat.che("Pokémon lớn", "*"));
        Kiem.bang("chat 'đại lý' che", "bán * xu", data.Database.locChat.che("bán đại lý xu", "*"));
        Kiem.bang("chat giftcode che", "nhập * nè", data.Database.locChat.che("nhập GIFTCODE nè", "*"));
        Kiem.bang("chat Ditto khong bi che", "Ditto biến hình", data.Database.locChat.che("Ditto biến hình", "*"));

        // chan ca tin (BadWord.list + listvn)
        Kiem.dung("chat 'đi sớm' khong bi chan (dis)", !real.BadWord.checkBadWord("đi sớm nha"));
        Kiem.dung("chat 'chóng mặt' khong bi chan (chó)", !real.BadWord.checkBadWord("chóng mặt quá"));
        Kiem.dung("chat 'vú sữa' van chan (vú ca tu)", real.BadWord.checkBadWord("ăn vú sữa"));
        Kiem.dung("chat 'ditme' chan", real.BadWord.checkBadWord("d.i.t.m.e may"));
        Kiem.dung("chat dmm chan", real.BadWord.checkBadWord("dmm"));
        Kiem.dung("chat 'địt' chan", real.BadWord.checkBadWord("địt"));
        Kiem.dung("chat binh thuong qua", !real.BadWord.checkBadWord("Xin chào mọi người, đi săn Gyarados không?"));
        Kiem.bang("checkBadWord2 www che", "vào @.abc", real.BadWord.checkBadWord2("vào www.abc"));
        Kiem.bang("checkBadWord2 khong che 'dm' giua hai tu", "bad move", real.BadWord.checkBadWord2("bad move"));

        // kieu theo cot bad_word
        final List<String> t2 = new ArrayList<String>();
        final List<String> k2 = new ArrayList<String>();
        t2.add("vu");
        k2.add("chuoi");
        t2.add("phongthu");
        k2.add("tu");
        data.Database.dungLoc(t2, k2);
        Kiem.dung("kieu chuoi: 'vu' chan Nguyenvu", data.Database.tuCamTrongTen("Nguyenvu") != null);
        Kiem.dung("kieu tu: 'phongthu' chan Phongthu1 (so la ranh gioi tu)", data.Database.tuCamTrongTen("Phongthu1") != null);
        Kiem.dung("kieu tu: 'phongthu' khong chan Phongthuy", data.Database.tuCamTrongTen("Phongthuy") == null);
        Kiem.bang("kieuTuDong ngu", LocTuCam.CA_TU, LocTuCam.kieuTuDong("ngu"));
        Kiem.bang("kieuTuDong dkm", LocTuCam.CHUOI, LocTuCam.kieuTuDong("dkm"));
        Kiem.bang("kieuTuDong ditme", LocTuCam.CHUOI, LocTuCam.kieuTuDong("ditme"));
        data.Database.dungLoc(new ArrayList<String>(), null);
    }
}
