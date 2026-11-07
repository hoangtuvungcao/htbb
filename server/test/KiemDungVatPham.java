import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import real.Char;
import real.DungVatPham;
import real.Item;
import real.ItemTemplates;

/**
 * Bam "Su dung" moi loai vat pham (0.9.2, chu bao "dung vat pham khong co tac dung"): moi lan bam deu co phan hoi,
 * tru dung so luong, tac dung that. Chay THAT duong lenh 29 / 22 (DungVatPham -> Map.doUseItemGoc / MapDungThuoc /
 * VatPhamDungLauDai / TienHoaPokemon / CuaHangLauDai) tren mot nhan vat ngoai server (session gia ghi lai goi tin).
 */
public final class KiemDungVatPham {

    private KiemDungVatPham() {
    }

    /** Goi tin server gui cho nhan vat thu. */
    static final List<Message> GOI = new ArrayList<Message>();
    static short idKe = 20000;

    public static void chay() {
        Kiem.nhom("Dung vat pham (DungVatPham) - moi loai co tac dung + phan hoi");
        try {
            chuanBi();
            thuocHpPp();
            dongBoHpNgay();
            hoiSinh();
            quaMong();
            tangToc();
            daTienHoa();
            hopQua();
            vatPhamKhoa();
            trangBi();
            ngoaiHinh();
            khongDungDuoc();
            buffChiSo();
            chongVatPham();
            petDiTheo();
            ketQuaClientCu();
            noiVaoGame();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            com.htbb.pokemon.LauDai.dat(false);
            real.Map.gemTemplate = gemCu;
            traLai();
        }
    }

    // ------------------------------------------------------------------ dung cu

    static real.Map map;
    static real.GemTemplate[] gemCu;

    @SuppressWarnings("unchecked")
    static void chuanBi() throws Exception {
        com.htbb.pokemon.LauDai.dat(true);
        gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        map = new real.Map();
        while (real.Map.itemTemplates.size() < 6) {
            real.Map.itemTemplates.add(new Hashtable<Integer, ItemTemplates>());
        }
        // bang vat pham dai han (data_vat_pham_lau_dai) toi thieu cho cac loai thu
        final java.util.Map<Integer, Object[]> bang = new java.util.HashMap<Integer, Object[]>();
        bang.put(1207, new Object[]{"hoi_sinh_2", "hoi_sinh", "hoi_sinh:100", "", 1, 100});
        bang.put(1208, new Object[]{"x_toc_do", "x_tang_cuong", "toc_danh:15,giay:90", "", 1, 100});
        bang.put(1209, new Object[]{"x_chinh_xac", "x_tang_cuong", "chinh_xac:15,giay:90", "", 1, 100});
        bang.put(1215, new Object[]{"qua_oran", "qua_mong", "tu_dung:hp50,hp:10", "", 1, 100});
        bang.put(1223, new Object[]{"qua_leppa", "qua_mong", "tu_dung:mp20,mp:15", "", 1, 100});
        bang.put(1224, new Object[]{"giu_thuc_an_thua", "do_giu", "o:phien_da,hoi_hp_3s:1", "", 1, 100});
        bang.put(1050, new Object[]{"hop_tham_gia", "hop", "mo_hop:1050", "", 1, 100});
        final Field fb = real.VatPhamDungLauDai.class.getDeclaredField("BANG");
        fb.setAccessible(true);
        bangVatPhamCu = fb.get(null);
        fb.set(null, bang);
        // mot dong doi "item:1057x1" trong Cua hang dai han = qua le mo duoc tu tui
        final List<Object[]> ch = new ArrayList<Object[]>();
        ch.add(new Object[]{114, "Đổi vật phẩm sự kiện", "Quà 30/4: 3 Quả Oran", "item:1057x1", "ITEM:1215:3", 1, 0, -1, 0});
        final Field fc = real.CuaHangLauDai.class.getDeclaredField("BANG");
        fc.setAccessible(true);
        bangCuaHangCu = fc.get(null);
        fc.set(null, ch);
        mau(1207, 28, "Thuốc hồi sinh tối đa", 1);
        mau(1208, 28, "X Tốc độ", 1);
        mau(1209, 28, "X Chính xác", 1);
        mau(1215, 28, "Quả Oran", 1);
        mau(1223, 28, "Quả Leppa", 1);
        mau(1224, 28, "Thức ăn thừa", 1);
        mau(1050, 28, "Hộp tham gia đột kích", 1);
        mau(1057, 28, "Quà lễ 30/4 - 1/5", 1);
        mau(1250, 28, "Vé EXP x1,5 (30 phút)", 1);
        mau(1251, 28, "Vé EXP x1,5 (30 phút) - khoá", 1);
        mau(1254, 28, "Bùa thể lực (+60 phút)", 1);
        mau(1256, 28, "Phiếu ủy thác luyện tập (1 giờ)", 1);
        mau(960, 28, "Đá Nước", 1);
        mau(1034, 28, "Bụi sao", 1);
        mau(731, 28, "Đồng xu xanh\nDùng đổi trang bị thời trang", 1);
        mau(678, 24, "Vé Mê cung 5x", 1);
        mau(905, 30, "Skin thú cưỡi Copperajah", 1);
        mau(126, 9, "Vòng cổ sơ cấp", 1).clazz = -1;
        mau(503, 9, "Vòng cổ chuyên gia", 60).clazz = -1;
        mau(79, 3, "Than lửa sơ cấp", 1).clazz = 0;
    }

    /** Trang thai tinh cu (tra lai sau khi kiem, khoi anh huong nhom kiem khac). */
    static Object bangVatPhamCu, bangCuaHangCu;
    static final List<Integer> MAU_THEM = new ArrayList<Integer>();

    static void traLai() {
        try {
            if (bangVatPhamCu != null) {
                final Field fb = real.VatPhamDungLauDai.class.getDeclaredField("BANG");
                fb.setAccessible(true);
                fb.set(null, bangVatPhamCu);
            }
            if (bangCuaHangCu != null) {
                final Field fc = real.CuaHangLauDai.class.getDeclaredField("BANG");
                fc.setAccessible(true);
                fc.set(null, bangCuaHangCu);
            }
            for (final Integer id : MAU_THEM) {
                for (int i = 0; i < 6 && i < real.Map.itemTemplates.size(); i++) {
                    real.Map.itemTemplates.get(i).remove(id);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    static ItemTemplates mau(final int id, final int type, final String ten, final int cap) {
        if (!real.Map.itemTemplates.get(5).containsKey(Integer.valueOf(id))) {
            MAU_THEM.add(Integer.valueOf(id));
        }
        final ItemTemplates t = new ItemTemplates();
        t.id = id;
        t.type = (byte) type;
        t.name = ten;
        t.level = (byte) cap;
        t.clazz = 0;
        t.atb = new short[12];
        for (int i = 0; i < 6; i++) {
            real.Map.itemTemplates.get(i).put(Integer.valueOf(id), t);
        }
        return t;
    }

    static Char nhanVat() {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        });
        p.getSession().banClient = DungVatPham.BAN_TOAST;
        p.charname = "kiemdung" + (idKe++);
        p.lvDetail.lv = 50;
        p.charClass = 0;
        p.gender = 1;
        p.maxhp = 1000;
        p.hp = 1000;
        p.maxmp = 500;
        p.mp = 500;
        p.MAX_INVENTORY = 42;
        p.maxBag = 1;
        p.map = map;
        return p;
    }

    static Item cho(final Char p, final int tpl) {
        final Item it = new Item();
        it.tempateID = tpl;
        it.classTemplate = -1;
        it.id = idKe++;
        it.level = it.getTemplate().level;
        it.clazz = it.getTemplate().clazz;
        p.iItems.add(it);
        return it;
    }

    static int dem(final Char p, final int tpl) {
        int n = 0;
        for (final Item it : p.iItems) {
            if (it.tempateID == tpl) {
                n++;
            }
        }
        return n;
    }

    static Message goi(final int cmd, final boolean laShort, final int v) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream o = new DataOutputStream(bo);
        if (laShort) {
            o.writeShort(v);
        } else {
            o.writeByte(v);
        }
        return new Message(cmd, bo.toByteArray());
    }

    /** Bam dung mon {@code it}: tra ve {loai, noi dung} thong bao nhanh cuoi cung (-104), null neu khong co. */
    /** Ngoai server, calculateAttrib tinh maxhp = 0 (khong co bang chi so) -> dat lai cho nhan vat con song. */
    static void giuMau(final Char p) {
        if (p.maxhp <= 0) {
            p.maxhp = 1000;
            p.hp = 1000;
        }
        if (p.maxmp <= 0) {
            p.maxmp = 500;
            p.mp = 500;
        }
    }

    static String[] dung(final Char p, final int idMon) throws Exception {
        giuMau(p);
        GOI.clear();
        DungVatPham.dung(map, p, goi(29, true, idMon));
        return baoCuoi();
    }

    static String[] dungThuoc(final Char p, final int loai) throws Exception {
        giuMau(p);
        GOI.clear();
        DungVatPham.dungThuoc(map, p, goi(22, false, loai));
        return baoCuoi();
    }

    static String[] baoCuoi() throws Exception {
        String[] kq = null;
        for (final Message m : GOI) {
            if (m.command == DungVatPham.LENH) {
                final byte[] b = m.toByteArray();
                final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
                final int loai = r.readByte();
                kq = new String[]{loai == 0 ? "OK" : loai == 1 ? "LOI" : "TIN", r.readUTF()};
            }
        }
        return kq;
    }

    static boolean coGoi(final int cmd) {
        for (final Message m : GOI) {
            if (m.command == cmd) {
                return true;
            }
        }
        return false;
    }

    static String chu(final String[] b) {
        return b == null ? "(khong co thong bao)" : b[0] + ": " + b[1];
    }

    // ------------------------------------------------------------------ tung loai

    static void thuocHpPp() throws Exception {
        final Char p = nhanVat();
        p.potions[1] = 3;
        String[] b = dungThuoc(p, 1);
        Kiem.dung("binh HP khi day mau: bao loi ro rang (" + chu(b) + ")", b != null && "LOI".equals(b[0]) && b[1].contains("HP đã đầy"));
        Kiem.bang("binh HP khi day mau: khong tru binh", 3, p.potions[1]);
        p.potions[4] = 2;
        b = dungThuoc(p, 4);
        Kiem.dung("binh PP khi day PP: bao loi (" + chu(b) + ")", b != null && b[1].contains("PP đã đầy"));
        Kiem.bang("binh PP khi day: khong tru", 2, p.potions[4]);
        p.potions[7] = 0;
        b = dungThuoc(p, 7);
        Kiem.dung("binh khong con: bao 'Khong con' (" + chu(b) + ")", b != null && b[1].startsWith("Không còn"));
        p.potions[129] = 1;
        b = dungThuoc(p, 129);
        Kiem.dung("binh Chien truong ngoai Chien truong: bao ro (" + chu(b) + ")", b != null && b[1].contains("Chiến trường"));
        Kiem.bang("binh Chien truong: khong tru", 1, p.potions[129]);
        // tui day: hop qua dang binh khong bi tru (truoc: tru roi bao "Hanh trang day")
        p.MAX_INVENTORY = 1;
        p.potions[147] = 1;
        b = dungThuoc(p, 147);
        Kiem.dung("hop qua (binh 147) khi tui day: bao can o trong (" + chu(b) + ")", b != null && b[1].contains("Hành trang đầy"));
        Kiem.bang("hop qua khi tui day: khong tru", 1, p.potions[147]);
        p.MAX_INVENTORY = 42;
    }

    /** Goi 60 cuoi cung trong GOI cho nhan vat id: {hp, maxhp, mp} theo cach client doc (MauPP.doc60), null neu khong co. */
    static int[] hmpCuoi(final int id) throws Exception {
        int[] kq = null;
        for (final Message m : GOI) {
            if (m.command == 60) {
                final byte[] b = m.toByteArray();
                final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
                final int cid = r.readShort();
                final int maxhp = r.readInt();
                r.readInt();
                r.readShort();
                final int hp = r.readInt();
                r.readShort();
                final int mp = r.readInt();
                if (cid == id) {
                    kq = new int[]{hp, maxhp, mp};
                }
            }
        }
        return kq;
    }

    /** Goi 22 cuoi cung: {potionType, valueNew, isHP}. */
    static int[] goi22Cuoi() throws Exception {
        int[] kq = null;
        for (final Message m : GOI) {
            if (m.command == 22) {
                final byte[] b = m.toByteArray();
                final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
                r.readShort();
                final int loai = r.readByte();
                r.readShort();
                final int moi = r.readInt();
                final int isHp = r.available() > 0 ? r.readByte() : -1;
                kq = new int[]{loai, moi, isHp};
            }
        }
        return kq;
    }

    /**
     * 0.9.3 "HP cu sau khi dung binh": dung binh / vat pham hoi HP xong, server gui NGAY goi HP (22 / 60) mang HP moi
     * trong cung lan xu ly lenh (khong cho goi dinh ky); goi 60 co PP o duoi.
     */
    static void dongBoHpNgay() throws Exception {
        final Char p = nhanVat();
        // binh HP 1 (MapDungThuoc -> Map.doUseHP): can mau binh + hoi chieu nhu CharKhoiTao
        while (real.Map.potionTemplates2.size() < 7) {
            real.Map.potionTemplates2.add(new real.PotionTemplate2(10, 200));
        }
        if (p.coolDownPotion == null) {
            p.coolDownPotion = new Hashtable<Short, real.PotionUse>();
        }
        for (final short id : new short[]{1, 2, 3, 4, 5, 6, 21, 22, 23, 24, 93, 94, 95, 96, 126, 127}) {
            p.coolDownPotion.put(Short.valueOf(id), new real.PotionUse(id, 350));
        }
        p.hp = 300;
        p.potions[1] = 2;
        final int truoc = SO_DONG_BO();
        dungThuoc(p, 1);
        Kiem.dung("binh HP: HP tang tren server (" + p.hp + ")", p.hp > 300);
        final int[] g22 = goi22Cuoi();
        Kiem.dung("binh HP: goi 22 gui ngay voi HP moi (" + (g22 == null ? "khong co" : g22[1] + "") + ")", g22 != null && g22[1] == p.hp);
        final int[] h = hmpCuoi(p.id);
        Kiem.dung("binh HP: goi 60 gui ngay, HP = HP server", h != null && h[0] == p.hp);
        Kiem.dung("binh HP: goi 60 mang PP o duoi", h != null && h[2] == p.mp);
        Kiem.dung("binh HP: dem dong bo tang", SO_DONG_BO() > truoc);
        Kiem.bang("binh HP: tru 1 binh", 1, p.potions[1]);
        // binh PP 4
        p.mp = 100;
        p.potions[4] = 1;
        dungThuoc(p, 4);
        final int[] h2 = hmpCuoi(p.id);
        Kiem.dung("binh PP: goi 60 gui ngay voi PP moi (" + p.mp + ")", p.mp > 100 && h2 != null && h2[2] == p.mp);
        // vat pham hoi HP trong tui (qua Oran, lenh 29)
        final Item q = cho(p, 1215);
        p.hp = 400;
        dung(p, q.id);
        final int[] h3 = hmpCuoi(p.id);
        Kiem.dung("qua Oran: goi 60 gui ngay voi HP moi (" + p.hp + ")", p.hp > 400 && h3 != null && h3[0] == p.hp);
        // khong doi gi (binh khi day mau bi chan): khong gui goi 60 thua
        p.hp = p.maxhp;
        p.potions[1] = 1;
        dungThuoc(p, 1);
        Kiem.dung("binh HP khi day: khong gui goi 60 thua", hmpCuoi(p.id) == null);
        // createMsgUseHpMP loai 4 (PP): co isHP = 0 (truoc: 1 -> client co the ghi PP vao HP)
        GOI.clear();
        real.MessageCreator.createMsgUseHpMP(p, 5, 4);
        final int[] g = goi22Cuoi();
        Kiem.dung("createMsgUseHpMP PP: co isHP = 0", g != null && g[2] == 0);
    }

    static int SO_DONG_BO() throws Exception {
        final Field f = DungVatPham.class.getDeclaredField("SO_DONG_BO");
        f.setAccessible(true);
        return ((java.util.concurrent.atomic.AtomicInteger) f.get(null)).get();
    }

    static void hoiSinh() throws Exception {
        final Char p = nhanVat();
        final Item t = cho(p, 1207);
        String[] b = dung(p, t.id);
        Kiem.dung("Thuoc hoi sinh toi da khi con song: bao chi dung khi guc (" + chu(b) + ")", b != null && b[1].contains("gục"));
        Kiem.bang("con song: khong tru", 1, dem(p, 1207));
        p.hp = 0;
        final Item q = cho(p, 1215);
        b = dung(p, q.id);
        Kiem.dung("da guc, bam qua Oran: bao khong dung khi guc (truoc: im lang) (" + chu(b) + ")", b != null && b[1].contains("khi đã gục"));
        Kiem.bang("da guc: khong tru qua", 1, dem(p, 1215));
        b = dung(p, t.id);
        Kiem.dung("da guc, Thuoc hoi sinh toi da: co phan hoi (truoc: Map.checkTrade chan im lang) (" + chu(b) + ")", b != null);
        // ngoai server calculateAttrib dua maxhp ve 0 nen khong so HP; kiem thong bao + so thuoc
        Kiem.dung("da guc: hoi sinh xong hoac bao ly do (" + chu(b) + ")", b != null && (b[1].contains("hồi sinh") || "LOI".equals(b[0])));
        if (b != null && "OK".equals(b[0])) {
            Kiem.bang("hoi sinh: tru dung 1 thuoc", 0, dem(p, 1207));
        }
        p.maxhp = 1000;
        p.hp = 0;
        p.potions[119] = 1;
        b = dungThuoc(p, 119);
        Kiem.dung("da guc, binh Thuoc hoi sinh (119): co phan hoi (" + chu(b) + ")", b != null || p.hp > 0);
    }

    static void quaMong() throws Exception {
        final Char p = nhanVat();
        final Item q = cho(p, 1215);
        String[] b = dung(p, q.id);
        Kiem.dung("qua Oran khi day HP: bao HP day (" + chu(b) + ")", b != null && "LOI".equals(b[0]) && b[1].contains("HP đã đầy"));
        Kiem.bang("qua Oran khi day HP: khong tru", 1, dem(p, 1215));
        p.hp = 400;
        b = dung(p, q.id);
        Kiem.dung("qua Oran khi thieu HP: OK (" + chu(b) + ")", b != null && "OK".equals(b[0]) && b[1].contains("Quả Oran"));
        Kiem.bang("qua Oran: +10% HP", 500, p.hp);
        Kiem.bang("qua Oran: tru 1", 0, dem(p, 1215));
        Kiem.dung("qua Oran: gui lai hanh trang (16) + HP (3)", coGoi(16) && coGoi(3));
        final Item l = cho(p, 1223);
        b = dung(p, l.id);
        Kiem.dung("qua Leppa khi day PP: bao PP day (" + chu(b) + ")", b != null && b[1].contains("PP đã đầy"));
        p.mp = 100;
        b = dung(p, l.id);
        Kiem.dung("qua Leppa khi thieu PP: OK", b != null && "OK".equals(b[0]));
        Kiem.bang("qua Leppa: +15% PP", 175, p.mp);
        // do giu (Thuc an thua): bam = mang / cat, khong tru
        final Item g = cho(p, 1224);
        b = dung(p, g.id);
        Kiem.dung("do giu: co phan hoi mang/cat (" + chu(b) + ")", b != null);
        Kiem.bang("do giu: khong tru", 1, dem(p, 1224));
    }

    static void tangToc() throws Exception {
        for (final int tpl : new int[]{1250, 1254, 1256}) {
            final Char p = nhanVat();
            final Item v = cho(p, tpl);
            final String[] b = dung(p, v.id);
            Kiem.dung("tang toc " + tpl + ": co phan hoi (" + chu(b) + ")", b != null);
            Kiem.dung("tang toc " + tpl + ": da dung thi tru 1, khong dung duoc thi giu", (b != null && b[1].startsWith("Đã dùng")) == (dem(p, tpl) == 0));
        }
    }

    static void daTienHoa() throws Exception {
        final Char p = nhanVat();
        final Item d = cho(p, 960);
        final String[] b = dung(p, d.id);
        Kiem.dung("da tien hoa sai Pokemon: bao ly do (" + chu(b) + ")", b != null);
        Kiem.bang("da tien hoa sai Pokemon: khong tru", 1, dem(p, 960));
    }

    static void hopQua() throws Exception {
        final Char p = nhanVat();
        final Item q = cho(p, 1057);
        String[] b = dung(p, q.id);
        Kiem.dung("qua le 1057 (dong doi item:1057x1): mo duoc tu tui (" + chu(b) + ")", b != null && "OK".equals(b[0]) && b[1].startsWith("Mở"));
        Kiem.bang("qua le: tru 1 qua", 0, dem(p, 1057));
        Kiem.bang("qua le: nhan 3 Qua Oran (1 chong - ChongVatPham)", "3/1", tongSo(p, 1215) + "/" + dem(p, 1215));
        Kiem.dung("qua le: gop thanh 1 thong bao (khong hop thoai 37 tung mon)", !coGoi(37));
        final Item h = cho(p, 1050);
        b = dung(p, h.id);
        Kiem.dung("hop dot kich 1050: co phan hoi (" + chu(b) + ")", b != null);
        Kiem.dung("hop dot kich: mo thi tru, khong mo thi giu", (b != null && "OK".equals(b[0])) == (dem(p, 1050) == 0));
    }

    static void vatPhamKhoa() throws Exception {
        final Char p = nhanVat();
        final Item v = cho(p, 1251);
        v.lock = 1;
        final String[] b = dung(p, v.id);
        Kiem.dung("ve EXP khoa (1251): dung nhu ve thuong, co phan hoi (" + chu(b) + ")", b != null);
    }

    static void trangBi() throws Exception {
        final Char p = nhanVat();
        final Item v = cho(p, 126);
        String[] b = dung(p, v.id);
        Kiem.dung("mac vong co: bao da trang bi (" + chu(b) + ")", b != null && "OK".equals(b[0]) && b[1].contains("trang bị"));
        Kiem.bang("mac: roi tui", 0, dem(p, 126));
        boolean dangMac = false;
        for (final Item it : p.getArrayCharWearing()) {
            dangMac |= it == v;
        }
        Kiem.dung("mac: vao o trang bi", dangMac);
        Kiem.dung("mac: gui trang bi (15) + chi so (3)", coGoi(15) && coGoi(3));
        b = dung(p, v.id);
        Kiem.dung("thao vong co (id mon dang mac): bao da thao (" + chu(b) + ")", b != null && b[1].startsWith("Đã tháo"));
        Kiem.bang("thao: ve tui", 1, dem(p, 126));
        final Item cao = cho(p, 503);
        b = dung(p, cao.id);
        Kiem.dung("mon cap 60 voi nhan vat cap 50: bao can cap (truoc: chi in System.out) (" + chu(b) + ")", b != null && b[1].contains("cấp 60"));
        Kiem.bang("mon cap cao: van trong tui", 1, dem(p, 503));
        final Item vk = cho(p, 79);
        b = dung(p, vk.id);
        Kiem.dung("cam vu khi: co phan hoi (" + chu(b) + ")", b != null);
        b = dung(p, vk.id);
        Kiem.dung("thao vu khi: bao khong thao duoc, giu nguyen (" + chu(b) + ")", b != null && "LOI".equals(b[0]) && b[1].contains("Không tháo") && dem(p, 79) == 0);
    }

    /**
     * 08/11/2026 (rongngoo, 3 item Cleffa cấp 1 trong tui): bam "Su dung" item pet da no (type 27) = pet DI THEO, khong
     * con bi day ve "gửi Nhà trẻ" mai; trung (type 26) van di duong no trung cu (khong bi nhanh moi bat).
     */
    static void petDiTheo() throws Exception {
        if (real.Map.petTemplate == null) {
            real.Map.loadPetTemplate();
        }
        final ItemTemplates trung = mau(686, 26, "Trứng Cleffa", 1);
        trung.atb[0] = 1;
        trung.atb[1] = 25;
        trung.idItemUpLevel = 699;
        final ItemTemplates c1 = mau(699, 27, "Cleffa cấp 1", 0);
        c1.atb[0] = 1;
        c1.atb[1] = 26;
        c1.idItemUpLevel = 700;
        final ItemTemplates c2 = mau(700, 27, "Clefairy cấp 2", 0);
        c2.atb[0] = 2;
        c2.atb[1] = 26;
        c2.idItemUpLevel = -1;
        final ItemTemplates bd = mau(873, 27, "Bidoof", 0);
        bd.atb[0] = 5;
        bd.atb[1] = 245;
        Kiem.bang("mau pet: Cleffa cap 1 = mau cua Trung Cleffa (25)", 25, real.PetDiTheoGame.mauPet(c1));
        Kiem.bang("mau pet: Clefairy cap 2 (lan nguoc chuoi trung) = 25", 25, real.PetDiTheoGame.mauPet(c2));
        Kiem.bang("mau pet: Bidoof (khong co trung) = mau di bo 29, KHONG 30 (Capybara giao dich duoc)", 29, real.PetDiTheoGame.mauPet(bd));
        Kiem.bang("mau pet: trung (type 26) khong phai item pet", -1, real.PetDiTheoGame.mauPet(trung));

        final Char p = nhanVat();
        final Item a = cho(p, 699);
        a.lock = 1;
        final Item b2 = cho(p, 699);
        b2.lock = 1;
        String[] b = dung(p, a.id);
        Kiem.dung("dung Cleffa cap 1: bao da di theo, khong con goi y 'gửi Nhà trẻ' (" + chu(b) + ")",
                b != null && "OK".equals(b[0]) && b[1].contains("đi theo") && !b[1].startsWith("Cleffa cấp 1: gửi"));
        Kiem.bang("dung Cleffa: roi tui (con 1 con kia)", 1, dem(p, 699));
        Kiem.bang("dung Cleffa: vao bang thu nuoi", 1, p.pet.size());
        Kiem.dung("dung Cleffa: la pet dang di theo", p.petUsing != null && p.petUsing == p.pet.get(0) && p.petUsing.place == 1);
        Kiem.bang("pet di theo: mau 25 (Cleffa)", 25, (int) p.petUsing.idTemplate);
        Kiem.dung("pet di theo: giu dung item Cleffa cap 1", p.petUsing.itemPet == a);
        Kiem.bang("pet di theo: chi so pet cap 1 (doAddOptionPet)", 10, (int) a.newAtb[2]);
        Kiem.dung("pet di theo: khong giao dich duoc (item pet khoa)", !p.petUsing.canTrade());
        Kiem.dung("pet di theo: gui lai tui / bang thu nuoi (16)", coGoi(16));
        Kiem.dung("pet di theo van tinh so huu cho Nha tre (dex 173)",
                com.htbb.pokemon.HeSinhThaiMenu.petSoHuu(p).contains(Integer.valueOf(173)));
        final data.Pet dau = p.petUsing;
        b = dung(p, b2.id);
        Kiem.dung("dung con thu 2: con moi di theo, con cu ve bang (place 0) (" + chu(b) + ")",
                p.pet.size() == 2 && p.petUsing != dau && p.petUsing.itemPet == b2 && dau.place == 0);
        final Item bi = cho(p, 873);
        b = dung(p, bi.id);
        Kiem.dung("dung Bidoof: di theo mau 29 (" + chu(b) + ")", b != null && "OK".equals(b[0]) && p.petUsing.idTemplate == 29);
        final Item tr = cho(p, 686);
        Kiem.dung("trung (type 26) khong qua nhanh pet di theo (van no trung o duong cu)", !real.PetDiTheoGame.dung(p, tr));
        Kiem.bang("trung: van trong tui", 1, dem(p, 686));
    }

    static void ngoaiHinh() throws Exception {
        final Char p = nhanVat();
        final Item s = cho(p, 905);
        final String[] b = dung(p, s.id);
        Kiem.dung("skin thu cuoi khi chua cuoi thu: bao ro (" + chu(b) + ")", b != null && b[1].contains("cưỡi"));
        Kiem.bang("skin: khong mat", 1, dem(p, 905));
    }

    static void khongDungDuoc() throws Exception {
        final Char p = nhanVat();
        final Item n = cho(p, 1034);
        String[] b = dung(p, n.id);
        Kiem.dung("nguyen lieu (Bui sao): bao huong dan, khong NPE (truoc: im lang) (" + chu(b) + ")", b != null && "LOI".equals(b[0]));
        Kiem.bang("nguyen lieu: khong mat", 1, dem(p, 1034));
        final Item x = cho(p, 731);
        b = dung(p, x.id);
        Kiem.dung("vat pham co mo ta (dong thu 2 cua ten): bao mo ta (" + chu(b) + ")", b != null && b[1].contains("Dùng đổi trang bị thời trang"));
        final Item ve = cho(p, 678);
        b = dung(p, ve.id);
        Kiem.dung("vat pham tro (type 24): bao khong con dung (" + chu(b) + ")", b != null && b[1].contains("không còn dùng"));
        b = dung(p, 31999);
        Kiem.dung("id khong con trong tui (tui cu o client): bao + gui lai tui (" + chu(b) + ")", b != null && b[1].contains("không còn") && coGoi(16));
    }

    static void buffChiSo() throws Exception {
        final Char p = nhanVat();
        final Item t = cho(p, 1208);
        String[] b = dung(p, t.id);
        Kiem.dung("X Toc do: OK + mo ta toc danh (" + chu(b) + ")", b != null && "OK".equals(b[0]) && b[1].contains("tốc đánh"));
        Kiem.bang("X Toc do: buff toc_danh 15", 15, real.VatPhamDungLauDai.buff(p, "toc_danh"));
        Kiem.dung("X Toc do: gui lai bang hoi chieu (35) cho client", coGoi(35));
        final Item c = cho(p, 1209);
        b = dung(p, c.id);
        Kiem.dung("X Chinh xac: OK + xuyen giap (" + chu(b) + ")", b != null && b[1].contains("xuyên giáp"));
        Kiem.bang("X Chinh xac 15 -> xuyen giap 5%", 5, real.VatPhamDungLauDai.xuyenGiap(p));
    }

    static int tongSo(final Char p, final int tpl) {
        int n = 0;
        for (final Item it : p.iItems) {
            if (it.tempateID == tpl) {
                n += com.htbb.pokemon.ChongVatPham.so(it);
            }
        }
        return n;
    }

    /** [chong-vat-pham] Dung 1 mon trong chong type 28: tru dung 1 don vi; khong dung duoc thi chong nguyen ven. */
    static void chongVatPham() throws Exception {
        final Char p = nhanVat();
        final Item t = cho(p, 1208);
        t.quanlity = 3;
        p.giuIDItem(t.id); // cho() dat id tay (ngoai getIDItem)
        String[] b = dung(p, t.id);
        Kiem.dung("chong 3 X Toc do: dung OK (" + chu(b) + ")", b != null && "OK".equals(b[0]));
        Kiem.bang("chong 3 X Toc do -> 1 dong con 2 (khong mat ca chong)", "1/2", dem(p, 1208) + "/" + tongSo(p, 1208));
        boolean giuId = false;
        for (final Item it : p.iItems) {
            giuId |= it.tempateID == 1208 && it.id == t.id;
        }
        Kiem.dung("chong con lai giu id cu (phim tat client van tro dung chong)", giuId);
        final Item n = cho(p, 1034);
        n.quanlity = 5;
        b = dung(p, n.id);
        Kiem.dung("chong 5 Bui sao (khong dung duoc): bao loi (" + chu(b) + ")", b != null && "LOI".equals(b[0]));
        Kiem.bang("chong 5 Bui sao: van 1 dong 5, giu id client", "1/5/true", dem(p, 1034) + "/" + tongSo(p, 1034) + "/" + p.iItems.contains(n));
        final Item q = cho(p, 1057);
        q.quanlity = 2;
        b = dung(p, q.id);
        Kiem.bang("mo 1 qua trong chong 2: con 1 qua, nhan 3 Qua Oran (1 chong)", "1/3/1", tongSo(p, 1057) + "/" + tongSo(p, 1215) + "/" + dem(p, 1215));
    }

    /** Client cu / Unity (khong gui so ban): van nhan hop thoai 37 nhu truoc, khong nhan -104. */
    static void ketQuaClientCu() throws Exception {
        final Char p = nhanVat();
        p.getSession().banClient = -1;
        final Item n = cho(p, 1034);
        GOI.clear();
        DungVatPham.dung(map, p, goi(29, true, n.id));
        Kiem.dung("client cu: hop thoai 37, khong -104", coGoi(37) && !coGoi(DungVatPham.LENH));
    }

    static void noiVaoGame() {
        final String m = doc("src/real/Map.java");
        Kiem.dung("Map.doUseItem -> DungVatPham.dung", m.contains("() -> DungVatPham.dung(m, player, message))"));
        Kiem.dung("Map.doUsePotion -> DungVatPham.dungThuoc", m.contains("() -> DungVatPham.dungThuoc(m, player, message))"));
        Kiem.dung("Map.doUseItemGoc: chan vat pham khong mac duoc truoc exchangeItem", m.contains("if (!DungVatPham.macDuoc(item)) {"));
        Kiem.dung("RealController lenh 22 -> DungVatPham.dungThuoc", doc("src/real/RealController.java").contains("() -> DungVatPham.dungThuoc(null, p, message))"));
        Kiem.dung("Char.sendMessage: phien dung vat pham", doc("src/real/Char.java").contains("DungVatPham.quaPhien(this, m)"));
        Kiem.dung("LoginHandler: luu so ban client", doc("src/real/cmd/LoginHandler.java").contains("session.banClient = banClient;"));
        final String c = doc("../client_jar/src/game/networklogic/MessageHandler.java");
        Kiem.dung("client: MessageHandler nhan -104 (DungDo)", c.contains("game.pokemon.DungDo.nhan(msg)"));
        Kiem.dung("client: Canvas ve thong bao nhanh tren moi man", doc("../client_jar/src/game/render/Canvas.java").contains("game.pokemon.DungDo.ve(g);"));
        Kiem.dung("client: WindowInfoScr / ShopNew dung qua DungDo", doc("../client_jar/src/game/render/screen/WindowInfoScr.java").contains("game.pokemon.DungDo.dungItem(item);")
                && doc("../client_jar/src/game/model/ShopNew.java").contains("game.pokemon.DungDo.dungItem(item);"));
        Kiem.dung("client: tab Trang bi co Thao ra", doc("../client_jar/src/game/render/screen/WindowInfoScr.java").contains("game.pokemon.DungDo.thao(item);"));
        Kiem.bang("ma lenh -104", -104, DungVatPham.LENH);
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
