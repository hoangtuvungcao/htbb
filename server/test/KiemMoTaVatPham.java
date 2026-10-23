import com.htbb.pokemon.MoTaVatPham;
import data.Animal;
import data.Pet;
import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Char;
import real.DungVatPham;
import real.ShopTemplate;

/**
 * RA SOAT VAT PHAM (0.9.14, chu nhan: "Sao goi Tauros" mua o shop, trong tui ra "Binh thuoc / Hoi HP" va khong dung
 * duoc; "Lông thời trang" trung ten / trung icon; "Vé giờ vàng 100" khong ghi bao lau). Hai phan:
 * <ul>
 * <li>KIEM DU LIEU (moi id data_potion + moi dong data_shop sau phan B sql/htbb.sql): (1) loai dung - "Bình thuốc"
 * chi cho binh; (2) mo ta cu the, khong chu HP / PP o vat pham khac, khong rong; (3) ten phan biet duoc (data_potion
 * va dong dang ban cung loai tien); (4) "dung tu tui" khop ma xu ly that (real/doUsePotion.java: nhanh "Khong the
 * su dung" / default khong lam gi -&gt; phai la khong dung duoc, co ly do); (5) chi binh HP / PP gan o nhanh; (6) icon
 * PNG that (map/icon_pokemon/n.png qua IconPokemonBang); (7) vat pham co thoi han ghi ro thoi gian.</li>
 * <li>KIEM HANH VI: mua Sao goi Tauros (lenh 85 that) -&gt; tui potions[64], tru 300 luong; Su dung (lenh 22 that) -&gt;
 * thu cuoi Tauros vinh vien, tru dung 1, bao OK; O thu day -&gt; tu choi, KHONG tru; vat pham khong dung duoc -&gt; ly do,
 * KHONG tru (truoc: mat ma khong co tac dung); trang chieu bo o thuoc khong phai HP / PP; bang mo ta chi gui client
 * &gt;= 914; Phao hoa Tet ngoai su kien khong tru luong.</li>
 * </ul>
 */
public final class KiemMoTaVatPham {

    private KiemMoTaVatPham() {
    }

    static final List<Message> GOI = new ArrayList<Message>();
    static short idKe = 30000;

    public static void chay() {
        Kiem.nhom("Ra soat vat pham dang thuoc + cua hang (MoTaVatPham, ThuCuoiTuTui, sua_mo_ta_vat_pham.sql)");
        try {
            duLieu();
            cuaHang();
            maXuLy();
            hanhVi();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    // ================================================================== du lieu

    /** data_potion sau phan B: id -&gt; ten. */
    static Map<Integer, String> tenThuoc() {
        final Map<Integer, String> ten = new LinkedHashMap<Integer, String>();
        for (final Map<String, String> d : DayDu.dongChu("data_potion")) {
            ten.put(Integer.valueOf(d.get("id")), d.get("name"));
        }
        final Matcher m = Pattern.compile("UPDATE `data_potion` SET `name`='([^']*)'(?:, `name2`='[^']*')? WHERE `id`=(\\d+);")
                .matcher(DayDu.nangCap());
        while (m.find()) {
            ten.put(Integer.valueOf(m.group(2)), m.group(1));
        }
        return ten;
    }

    static String gon(final String t) {
        return real.cmd.LoginHandler.tenGon(t);
    }

    static void duLieu() {
        final Map<Integer, String> ten = tenThuoc();
        Kiem.dung("data_potion doc duoc (" + ten.size() + " dong)", ten.size() >= 160);
        int thieu = 0, sai = 0, rong = 0;
        final StringBuilder loi = new StringBuilder();
        for (final Map.Entry<Integer, String> e : ten.entrySet()) {
            final int id = e.getKey().intValue();
            if (id == 0) {
                continue; // xu
            }
            if (!MoTaVatPham.co(id)) {
                thieu++;
                loi.append(" thieu:").append(id);
                continue;
            }
            final String loai = MoTaVatPham.loai(id), mt = MoTaVatPham.moTa(id);
            final boolean hpPp = MoTaVatPham.ganONhanhDuoc(id);
            if (mt.trim().isEmpty() || loai.trim().isEmpty()) {
                rong++;
                loi.append(" rong:").append(id);
            }
            if (!hpPp && (mt.contains("nút thuốc HP") || mt.contains("nút thuốc PP"))) {
                sai++;
                loi.append(" chuHP:").append(id);
            }
            if (MoTaVatPham.laHp(id) && !mt.contains("nút thuốc HP") || MoTaVatPham.laPp(id) && !mt.contains("nút thuốc PP")) {
                sai++;
                loi.append(" binh:").append(id);
            }
            if (MoTaVatPham.BINH.equals(loai) && !hpPp && id != 119) {
                sai++;
                loi.append(" loaiBinh:").append(id);
            }
            if (!MoTaVatPham.dungDuoc(id) && MoTaVatPham.lyDo(id).trim().isEmpty()) {
                rong++;
                loi.append(" lyDo:").append(id);
            }
            // (7) hieu ung co thoi han phai ghi thoi gian
            if ((mt.matches("(?s).*Tăng \\d+% (kinh nghiệm|các chỉ số|giáp|sát thương).*") || mt.contains("Nhân đôi"))
                    && !mt.matches("(?s).*\\d[\\d.]* (phút|giờ|giây|ngày).*")) {
                sai++;
                loi.append(" thoiHan:").append(id);
            }
        }
        Kiem.bang("(1)(2) moi id data_potion co dong mo ta" + loi, 0, thieu);
        Kiem.bang("(1)(2) loai / mo ta / ly do khong rong", 0, rong);
        Kiem.bang("(1)(2)(7) chu HP/PP chi o binh, 'Bình thuốc' chi binh, co thoi han ghi thoi gian", 0, sai);
        Kiem.dung("Sao goi Tauros: loai goi thu cuoi", MoTaVatPham.THU_CUOI.equals(MoTaVatPham.loai(64)));
        Kiem.dung("Sao goi Tauros: mo ta 'nhận thú cưỡi Tauros vĩnh viễn'", MoTaVatPham.moTa(64).contains("nhận thú cưỡi Tauros vĩnh viễn"));
        Kiem.dung("Ve gio vang 1 gio: ghi 60 phut + khong cong don", MoTaVatPham.moTa(35).contains("60 phút") && MoTaVatPham.moTa(35).contains("không cộng dồn"));
        // (3) ten phan biet: khong hai id cung ten gon (ten thuc te nguoi choi thay)
        final Map<String, Integer> da = new HashMap<String, Integer>();
        final StringBuilder trung = new StringBuilder();
        for (final Map.Entry<Integer, String> e : ten.entrySet()) {
            final String g = gon(e.getValue()).toLowerCase();
            if (g.isEmpty() || e.getKey().intValue() == 0) {
                continue;
            }
            final Integer cu = da.put(g, e.getKey());
            if (cu != null) {
                trung.append(" ").append(cu).append("/").append(e.getKey()).append("=").append(g);
            }
        }
        Kiem.dung("(3) data_potion khong trung ten" + trung, trung.length() == 0);
        // (6) icon that
        int khongIcon = 0;
        for (final Map<String, String> d : DayDu.dongChu("data_potion")) {
            final int img = Integer.parseInt(d.get("idimage"));
            final int n = com.htbb.pokemon.IconPokemon.doiAnh(5500 + img);
            if (n < 0 || !new java.io.File("map/icon_pokemon/" + n + ".png").isFile()) {
                khongIcon++;
            }
        }
        Kiem.bang("(6) moi vat pham dang thuoc co icon PNG that", 0, khongIcon);
    }

    // ================================================================== cua hang

    static final class DongShop {
        int id, img, value, type, sell;
        String ten, mt;
    }

    static Map<Integer, DongShop> shop() {
        final Map<Integer, DongShop> m = new LinkedHashMap<Integer, DongShop>();
        for (final Map<String, String> d : DayDu.dongChu("data_shop")) {
            final DongShop s = new DongShop();
            s.id = Integer.parseInt(d.get("id"));
            s.img = Integer.parseInt(d.get("idimg"));
            s.ten = d.get("name");
            s.mt = d.get("decript");
            s.value = Integer.parseInt(d.get("value"));
            s.type = Integer.parseInt(d.get("type"));
            s.sell = Integer.parseInt(d.get("sell"));
            m.put(Integer.valueOf(s.id), s);
        }
        final String b = DayDu.phanNangCap("sua_mo_ta_vat_pham.sql");
        Matcher x = Pattern.compile("UPDATE `data_shop` SET (.*?) WHERE `id`=(\\d+);").matcher(b);
        while (x.find()) {
            final DongShop s = m.get(Integer.valueOf(x.group(2)));
            if (s == null) {
                continue;
            }
            final Matcher n = Pattern.compile("`name`='([^']*)'").matcher(x.group(1));
            if (n.find()) {
                s.ten = n.group(1).replace("\\n", "\n");
            }
            final Matcher t = Pattern.compile("`decript`='([^']*)'").matcher(x.group(1));
            if (t.find()) {
                s.mt = t.group(1);
            }
        }
        x = Pattern.compile("UPDATE `data_shop` SET `name`=CONCAT\\('([^']*)', `value`, '([^']*)'\\) WHERE `id` BETWEEN (\\d+) AND (\\d+);").matcher(b);
        while (x.find()) {
            for (int i = Integer.parseInt(x.group(3)); i <= Integer.parseInt(x.group(4)); i++) {
                final DongShop s = m.get(Integer.valueOf(i));
                if (s != null) {
                    s.ten = x.group(1) + s.value + x.group(2).replace("\\n", "\n");
                }
            }
        }
        x = Pattern.compile("UPDATE `data_shop` SET `sell`=0 WHERE `id` BETWEEN (\\d+) AND (\\d+);").matcher(b);
        while (x.find()) {
            for (int i = Integer.parseInt(x.group(1)); i <= Integer.parseInt(x.group(2)); i++) {
                final DongShop s = m.get(Integer.valueOf(i));
                if (s != null) {
                    s.sell = 0;
                }
            }
        }
        return m;
    }

    static void cuaHang() {
        Kiem.dung("phan B co khoi sua_mo_ta_vat_pham.sql", !DayDu.phanNangCap("sua_mo_ta_vat_pham.sql").isEmpty());
        final Map<Integer, DongShop> m = shop();
        // (3) dong dang ban: (ten gon, loai tien, icon) khong trung
        final Map<String, Integer> da = new HashMap<String, Integer>();
        final StringBuilder trung = new StringBuilder();
        int khongMoTa = 0;
        final StringBuilder rong = new StringBuilder();
        for (final DongShop s : m.values()) {
            if (s.sell != 1) {
                continue;
            }
            final String k = gon(s.ten).toLowerCase() + "|" + s.type;
            final Integer cu = da.put(k, Integer.valueOf(s.id));
            if (cu != null) {
                trung.append(" ").append(cu).append("/").append(s.id);
            }
            // mo ta: cot decript hoac phan sau dong moi trong ten
            final boolean co = !s.mt.trim().isEmpty() || s.ten.indexOf('\n') > 0;
            if (!co) {
                khongMoTa++;
                rong.append(" ").append(s.id);
            }
        }
        Kiem.dung("(3) dong dang ban khong trung ten cung loai tien" + trung, trung.length() == 0);
        Kiem.bang("(2) dong dang ban co mo ta" + rong, 0, khongMoTa);
        for (int i = 154; i <= 164; i++) {
            Kiem.bang("kieu long " + i + " (icon chung, khong hien tren Pokemon): ngung ban", 0, m.get(Integer.valueOf(i)).sell);
        }
        for (int i = 18; i <= 30; i++) {
            Kiem.bang("binh kieu long shop " + i + ": ngung ban", 0, m.get(Integer.valueOf(i)).sell);
        }
        final Set<String> tenLong = new HashSet<String>();
        for (int i = 154; i <= 164; i++) {
            tenLong.add(gon(m.get(Integer.valueOf(i)).ten));
        }
        Kiem.bang("11 dong kieu long: 11 ten khac nhau", 11, tenLong.size());
        Kiem.dung("Ve gio vang 100 (dac biet): ghi 24 gio + khong cong don", m.get(Integer.valueOf(0)).mt.contains("24 giờ") && m.get(Integer.valueOf(0)).mt.contains("không cộng dồn"));
        Kiem.dung("shop 45 Sao goi Tauros: noi dung trong Hanh trang -> thu cuoi vinh vien", m.get(Integer.valueOf(45)).mt.contains("vĩnh viễn"));
        Kiem.dung("hoa hong 1/9/39/99: ten ghi so bong", gon(m.get(Integer.valueOf(40)).ten).contains("9") && gon(m.get(Integer.valueOf(42)).ten).contains("99"));
        // (6) icon dong dang ban
        int khongIcon = 0;
        for (final DongShop s : m.values()) {
            if (s.sell == 1) {
                final int n = com.htbb.pokemon.IconPokemon.doiAnh(5500 + s.img);
                if (n < 0 || !new java.io.File("map/icon_pokemon/" + n + ".png").isFile()) {
                    khongIcon++;
                }
            }
        }
        Kiem.bang("(6) dong dang ban co icon PNG that", 0, khongIcon);
    }

    // ================================================================== ma xu ly (doUsePotion) khop bang

    static Set<Integer> caseTrong(final String s) {
        final Set<Integer> r = new HashSet<Integer>();
        final Matcher m = Pattern.compile("case (\\d+):").matcher(s);
        while (m.find()) {
            r.add(Integer.valueOf(m.group(1)));
        }
        return r;
    }

    static void maXuLy() {
        final String u = DayDu.doc("src/real/doUsePotion.java");
        final int a = u.indexOf("case Potion.TYPE_SACH_SKILL_PET:");
        final int b = u.indexOf("createServerAlertMessage(\"Không thể sử dụng\", \"\")", a);
        final Set<Integer> traLai = a > 0 && b > a ? caseTrong(u.substring(a, b)) : new HashSet<Integer>();
        final int c = u.indexOf("\"su dung exp don \" + potionType, \"expdon\");");
        final int d = u.indexOf("default:", c);
        final Set<Integer> khongLam = c > 0 && d > c ? caseTrong(u.substring(c, d)) : new HashSet<Integer>();
        Kiem.dung("doc duoc nhanh 'Khong the su dung' (" + traLai.size() + " id)", traLai.size() > 30);
        Kiem.dung("doc duoc nhanh default khong lam gi (" + khongLam.size() + " id)", khongLam.size() > 3);
        final StringBuilder loi = new StringBuilder();
        for (final Integer id : traLai) {
            // 119 Thuoc hoi sinh: DungVatPham dung qua HoiSinhGame khi da guc (truoc khi vao doUsePotion)
            if (MoTaVatPham.dungDuoc(id.intValue()) && !real.ThuCuoiTuTui.la(id.intValue()) && id.intValue() != 119) {
                loi.append(" traLai:").append(id);
            }
        }
        for (final Integer id : khongLam) {
            final int i = id.intValue();
            if (i >= 14 && i <= 19) {
                continue; // khan PK / day thoat hiem: xu ly truoc switch
            }
            if (MoTaVatPham.dungDuoc(i)) {
                loi.append(" default:").append(id);
            }
        }
        Kiem.dung("(4) id ma xu ly khong lam gi / tra lai = khong dung duoc tu tui (hoac goi thu cuoi)" + loi, loi.length() == 0);
        for (final int id : new int[]{64, 65, 66, 67, 68, 86, 70, 72, 74, 115}) {
            Kiem.dung("goi / trung thu cuoi " + id + ": dung tu tui duoc (ThuCuoiTuTui)", real.ThuCuoiTuTui.la(id) && MoTaVatPham.dungDuoc(id));
        }
        Kiem.dung("NPC ap trung: ten Tauros (khong con 'Machamp'), cung duong ThuCuoiTuTui",
                DayDu.doc("src/real/MapDoMenu.java").contains("ThuCuoiTuTui.tao(player, thuoc, \"npc ap trung\")")
                && !DayDu.doc("src/real/MapDoMenu.java").contains("\"Machamp\", \"Arcanine\""));
        Kiem.dung("ve 150% (x2 = 3) cong +150 EXP (truoc +100)", DayDu.doc("src/real/ExpLauDai.java").contains("b += 150;"));
        final String cl = DayDu.doc("../client_jar/src/game/pokemon/TheVatPham.java");
        Kiem.dung("client: the vat pham khong con chu 'Bình thuốc' cung cho moi thuoc", !cl.contains("return \"Bình thuốc\";") && cl.contains("MoTaVatPham.loai("));
        Kiem.dung("client: nut xam + ly do khi khong dung duoc", cl.contains("MoTaVatPham.lyDo("));
        Kiem.dung("client: DungDo nhan -104 kieu 9", DayDu.doc("../client_jar/src/game/pokemon/DungDo.java").contains("MoTaVatPham.nap(s)"));
        Kiem.dung("client: 'Cho vao phim tat' chi binh HP/PP", DayDu.doc("../client_jar/src/game/render/screen/WindowInfoScr.java").contains("game.pokemon.MoTaVatPham.ganONhanh(potionType)"));
    }

    // ================================================================== hanh vi

    static real.Map map;

    static Char nhanVat(final int ban) {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        });
        p.getSession().banClient = ban;
        p.charname = "kiemmota" + (idKe++);
        p.lvDetail.lv = 50;
        p.gender = 1;
        p.maxhp = 1000;
        p.hp = 1000;
        p.maxmp = 500;
        p.mp = 500;
        p.MAX_INVENTORY = 42;
        p.maxBag = 1;
        p.animal = new Vector<Animal>();
        p.pet = new Vector<Pet>();
        p.userTrade = new Vector<Char>();
        p.passWord = "";
        p.isCorrectPass = true;
        p.map = map;
        return p;
    }

    static Message goi(final int cmd, final int b) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        new DataOutputStream(bo).writeByte(b);
        return new Message(cmd, bo.toByteArray());
    }

    static String[] baoCuoi() throws Exception {
        String[] kq = null;
        for (final Message m : GOI) {
            if (m.command == DungVatPham.LENH) {
                final byte[] b = m.toByteArray();
                final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
                final int loai = r.readByte();
                if (loai == MoTaVatPham.KIEU_GOI) {
                    continue;
                }
                kq = new String[]{loai == 0 ? "OK" : loai == 1 ? "LOI" : "TIN", r.readUTF()};
            }
        }
        return kq;
    }

    static String chu(final String[] b) {
        return b == null ? "(khong co)" : b[0] + ": " + b[1];
    }

    static String[] dung(final Char p, final int t) throws Exception {
        GOI.clear();
        DungVatPham.dungThuoc(map, p, goi(22, t));
        return baoCuoi();
    }

    static int dem(final int cmd) {
        int n = 0;
        for (final Message m : GOI) {
            if (m.command == cmd) {
                n++;
            }
        }
        return n;
    }

    static void hanhVi() throws Exception {
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        try {
            hanhVi0();
        } finally {
            real.Map.gemTemplate = gemCu;
        }
    }

    static void hanhVi0() throws Exception {
        map = new real.Map();
        final ShopTemplate[] shopCu = real.Map.shop;
        try {
            real.Map.shop = new ShopTemplate[]{
                new ShopTemplate(45, 47, "Sáo gọi Tauros", "", 1, 300, 0, 1, 1, 210),
                new ShopTemplate(148, 64, "Pháo hoa tết", "", 1, 100, 0, 0, 1, 100),
            };
            muaVaDung();
        } finally {
            real.Map.shop = shopCu;
        }
        oThuDay();
        khongDungDuoc();
        oNhanh();
        goiMoTa();
    }

    static void muaVaDung() throws Exception {
        final Char p = nhanVat(914);
        p.setLuong(1000);
        GOI.clear();
        real.doBuySpecialItem.doBuySpecialItem(p, goi(85, 45));
        Kiem.bang("mua Sao goi Tauros: vao tui potions[64]", 1, p.potions[64]);
        Kiem.bang("mua Sao goi Tauros: tru 300 luong", 700L, (long) p.getLuong());
        Kiem.dung("mua: gui lai tui (16)", dem(16) > 0);
        Kiem.dung("the trong tui: loai 'Vật phẩm gọi thú cưỡi' (khong 'Bình thuốc')", MoTaVatPham.THU_CUOI.equals(MoTaVatPham.loai(64)));
        final String[] b = dung(p, 64);
        Kiem.dung("Su dung: OK 'Đã nhận thú cưỡi Tauros' (" + chu(b) + ")", b != null && "OK".equals(b[0]) && b[1].contains("Tauros"));
        Kiem.bang("Su dung: tru dung 1", 0, p.potions[64]);
        Kiem.bang("Su dung: them 1 thu cuoi", 1, p.animal.size());
        final Animal a = p.animal.get(0);
        Kiem.dung("thu cuoi Tauros (idImg 2 = TRAU -> dex 128), cap 1, vinh vien",
                "Tauros".equals(a.name) && a.idImg == 2 && a.level == 1 && a.texpire == 0L && com.htbb.pokemon.ThuPokemon.dex(a.idImg) == 128);
        Kiem.dung("Su dung: gui lai tui + o thu (16)", dem(16) >= 2);
        final String[] c = dung(p, 64);
        Kiem.dung("het sao: bao 'Không còn' (" + chu(c) + ")", c != null && c[1].startsWith("Không còn"));
        // Phao hoa Tet ngoai su kien: khong tru luong (truoc: tru 100 luong, khong giao gi)
        final long l0 = p.getLuong();
        GOI.clear();
        real.doBuySpecialItem.doBuySpecialItem(p, goi(85, 148));
        Kiem.bang("Phao hoa Tet ngoai su kien: khong tru luong", l0, (long) p.getLuong());
    }

    static void oThuDay() throws Exception {
        final Char p = nhanVat(914);
        final int suc = real.QuanLyThuGame.sucChua(p);
        for (int i = 0; i < suc; i++) {
            final Animal a = new Animal();
            a.id = p.getIDItem();
            a.idImg = 1;
            p.animal.add(a);
        }
        p.potions[65] = 1;
        final String[] b = dung(p, 65);
        Kiem.dung("O thu day: tu choi bang cau chuan (" + chu(b) + ")", b != null && "LOI".equals(b[0]) && b[1].startsWith("Ô thú đã đầy"));
        Kiem.bang("O thu day: KHONG tru sao", 1, p.potions[65]);
        Kiem.bang("O thu day: khong them thu", suc, p.animal.size());
        Kiem.dung("O thu day: gui lai tui (client cu tru truoc)", dem(16) > 0);
    }

    static void khongDungDuoc() throws Exception {
        final Char p = nhanVat(914);
        for (final int id : new int[]{103, 76, 106, 20, 77}) {
            p.potions[id] = 2;
            final String[] b = dung(p, id);
            Kiem.dung("thuoc " + id + " (khong dung tu tui): bao ly do (" + chu(b) + ")", b != null && "LOI".equals(b[0]) && b[1].contains(MoTaVatPham.lyDo(id)));
            Kiem.bang("thuoc " + id + ": KHONG tru (truoc: nhanh default tru mat)", 2, p.potions[id]);
        }
        p.potions[21] = 1;
        p.hp = 10;
        final String[] h = dung(p, 21);
        Kiem.bang("binh HP van dung binh thuong", 0, p.potions[21]);
        Kiem.dung("binh HP: khong bi chan boi bang", h == null || !"LOI".equals(h[0]));
    }

    static void oNhanh() throws Exception {
        // o 0 = thuoc 64 (sao goi), o 1 = thuoc 1 (HP), o 2 = chieu 3
        final long thuoc64 = (3L << 9) | (64L << 1);
        final long thuoc1 = (3L << 9) | (1L << 1);
        final long chieu = (2L << 9) | (3L << 1);
        final long trang = thuoc64 | (thuoc1 << 12) | (chieu << 24);
        final java.lang.reflect.Method m = real.NgoaiHinhGame.class.getDeclaredMethod("locThuoc", long.class);
        m.setAccessible(true);
        final long ra = ((Long) m.invoke(null, Long.valueOf(trang))).longValue();
        Kiem.bang("o nhanh: sao goi thu cuoi bi bo", 0L, ra & 0xFFF);
        Kiem.bang("o nhanh: binh HP giu", thuoc1, (ra >> 12) & 0xFFF);
        Kiem.bang("o nhanh: chieu giu", chieu, (ra >> 24) & 0xFFF);
        Kiem.dung("chi binh HP/PP gan o nhanh", MoTaVatPham.ganONhanhDuoc(94) && MoTaVatPham.ganONhanhDuoc(96)
                && !MoTaVatPham.ganONhanhDuoc(64) && !MoTaVatPham.ganONhanhDuoc(35));
    }

    static void goiMoTa() throws Exception {
        final Char cu = nhanVat(913);
        GOI.clear();
        real.MoTaVatPhamGame.gui(cu);
        Kiem.bang("client 913: khong gui bang mo ta (kieu 9 se thanh thong bao)", 0, dem(DungVatPham.LENH));
        final Char p = nhanVat(914);
        GOI.clear();
        real.MoTaVatPhamGame.gui(p);
        final int n = dem(DungVatPham.LENH);
        Kiem.dung("client 914: gui bang mo ta (" + n + " goi)", n >= 5);
        int dong = 0;
        boolean coTauros = false;
        for (final Message m : GOI) {
            final byte[] b = m.toByteArray();
            final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
            Kiem.bang("goi -104 kieu 9", MoTaVatPham.KIEU_GOI, (int) r.readByte());
            final String s = r.readUTF();
            for (final String d : s.split("\n")) {
                dong++;
                if (d.startsWith("64\t") && d.split("\t", -1).length == 5 && d.contains("Tauros")) {
                    coTauros = true;
                }
            }
        }
        Kiem.bang("bang gui du dong", MoTaVatPham.bang().length, dong);
        Kiem.dung("dong 64 dung dinh dang 5 cot", coTauros);
        GOI.clear();
        real.MoTaVatPhamGame.gui(p);
        Kiem.bang("gui mot lan moi phien", 0, dem(DungVatPham.LENH));
    }
}
