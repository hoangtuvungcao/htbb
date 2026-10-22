import game.model.Monster;
import game.pokemon.BangLoai;
import game.pokemon.GoiSprite;
import game.pokemon.HePoke;
import game.pokemon.KhoSprite;
import game.pokemon.KhoSpriteLru;
import game.pokemon.LoaiMang;
import game.pokemon.PokeBody;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Kiểm phía client (1.0) cho đợt loài đầy đủ:
 *  - KhoSpriteLru: ngân sách byte, bỏ mục lâu không dùng nhất (không bỏ mục vừa thêm), mục quá ngân sách giữ một mình,
 *    ghi / đọc chỉ mục;
 *  - GoiSprite: tạo / tách gói HTPK, từ chối sai dấu / sai phiên bản / sai dex / thiếu .dl / cụt; khoá từ đường dẫn jar;
 *  - BangLoai (-126 kiểu 1, 3) và HePoke rơi về bảng loài cho loài ngoài bảng tĩnh;
 *  - LoaiMang -125: gán loài riêng cho quái (đến trước lệnh 4 thì giữ chờ), dex 0 xoá; -126 kiểu 0: ghép khúc, lưu kho
 *    (RMS của bản PC trong thư mục tạm, ngân sách nhỏ -> bỏ theo LRU), tong = 0 -> thiếu;
 *  - PokeBody: loài không có trong jar -> bóng đen + xin máy chủ đúng 1 lần (XIN_SPRITE), gói về -> hình thật (.dl + ảnh
 *    từ kho), Pokédex (KhoSprite.choXin = false) không xin.
 *  - (loai-bo-sung) bản SẮC MÀU: lay(dex, true) xin gói "<NNNN>s" đúng 1 lần (byte shiny = 1), trong lúc chờ / máy chủ
 *    báo thiếu vẽ bản thường; gói shiny về -> khoá "<NNNN>s"; cờ shiny trong đầu gói phải khớp; khoá từ đường dẫn
 *    "/pk/0011s_0.png"; gói THẬT server/map/loai (thường + shiny, có khối neo) đọc được; NGÂN SÁCH: mô phỏng nạp mọi gói
 *    thường + shiny của loài đã mở vào sổ LRU 1 MB -> không lúc nào vượt, gói lớn nhất (cặp thường + shiny) vừa ngân sách;
 *    bộ đệm ảnh PokeBody (ToiDaPixel) không vượt khi vẽ chân dung thường + shiny nhiều loài.
 *
 *   C=client_pc/out/build/cls; O=$(mktemp -d)
 *   javac -cp $C -d $O client_jar/tools/KiemKhoSprite.java
 *   java -Djava.awt.headless=true -Dhtbb.duLieu=$(mktemp -d) -cp $O:$C:client_pc/out/build/res KiemKhoSprite   # thoát 1 = hỏng
 */
public final class KiemKhoSprite {
    static int dat, hong;

    static void bang(String mo, Object mong, Object that) {
        if (mong == null ? that == null : mong.equals(that)) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG " + mo + ": mong " + mong + " thuc te " + that);
        }
    }

    static void dung(String mo, boolean v) {
        bang(mo, Boolean.TRUE, Boolean.valueOf(v));
    }

    static byte[] docRes(String ten) throws Exception {
        InputStream is = KiemKhoSprite.class.getResourceAsStream(ten);
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        int n;
        while ((n = is.read(b)) > 0) {
            bo.write(b, 0, n);
        }
        is.close();
        return bo.toByteArray();
    }

    /** Gói giả cho dex {@code dex} lấy tệp thật của khoá jar {@code goc} (đổi tên). */
    static byte[] goiTu(String goc, int dex) throws Exception {
        String k = PokeBody.tenKhoa(dex, false);
        Vector ten = new Vector(), du = new Vector();
        String[] hau = {".dl", "_0.png", "_1.png", "_2.png", "_3.png", "_4.png", "_5.png", "_c.png"};
        for (int i = 0; i < hau.length; i++) {
            byte[] b = docRes("/pk/" + goc + hau[i]);
            if (b != null) {
                ten.addElement(k + hau[i]);
                du.addElement(b);
            }
        }
        String[] t = new String[ten.size()];
        byte[][] d = new byte[du.size()][];
        for (int i = 0; i < t.length; i++) {
            t[i] = (String) ten.elementAt(i);
            d[i] = (byte[]) du.elementAt(i);
        }
        return GoiSprite.tao(dex, false, t, d);
    }

    /** Thân -126 kiểu 0 (sau lệnh): kieu, dex, shiny, tong, viTri, len, byte. */
    static byte[] khuc(int dex, int tong, int viTri, byte[] goi, int len) throws Exception {
        return khuc(dex, false, tong, viTri, goi, len);
    }

    static byte[] khuc(int dex, boolean shiny, int tong, int viTri, byte[] goi, int len) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(0);
        o.writeShort(dex);
        o.writeByte(shiny ? 1 : 0);
        o.writeInt(tong);
        o.writeInt(viTri);
        if (tong > 0) {
            o.writeShort(len);
            o.write(goi, viTri, len);
        }
        o.flush();
        return bo.toByteArray();
    }

    static void guiGoi(int dex, byte[] goi) throws Exception {
        for (int v = 0; v < goi.length; v += 4096) {
            LoaiMang.docLoai(khuc(dex, goi.length, v, goi, Math.min(4096, goi.length - v)));
        }
    }

    static void guiGoi(int dex, boolean shiny, byte[] goi) throws Exception {
        for (int v = 0; v < goi.length; v += 4096) {
            LoaiMang.docLoai(khuc(dex, shiny, goi.length, v, goi, Math.min(4096, goi.length - v)));
        }
    }

    static int demXin(Vector gui, int dex) throws Exception {
        return demXin(gui, dex, false);
    }

    static int demXin(Vector gui, int dex, boolean shiny) throws Exception {
        int n = 0;
        for (int i = 0; i < gui.size(); i++) {
            network.Message m = (network.Message) gui.elementAt(i);
            byte[] b = m.getData();
            if (m.command == -126 && b.length == 4 && b[0] == 0 && ((b[1] & 0xFF) << 8 | (b[2] & 0xFF)) == dex
                    && b[3] == (shiny ? 1 : 0)) {
                n++;
            }
        }
        return n;
    }

    static void kiemLru() {
        KhoSpriteLru l = new KhoSpriteLru(100);
        bang("lru rong", Integer.valueOf(0), Integer.valueOf(l.them("a", 40).size()));
        l.them("b", 40);
        l.dung("a");                       // a mới dùng -> b cũ nhất
        Vector bo = l.them("c", 40);       // 120 > 100 -> bỏ b
        bang("lru bo b", "[b]", bo.toString());
        dung("lru con a", l.co("a"));
        dung("lru con c", l.co("c"));
        bang("lru tong", Integer.valueOf(80), Integer.valueOf(l.tong()));
        bo = l.them("to", 500);            // quá ngân sách: bỏ hết, giữ một mình
        bang("lru qua lon so bo", Integer.valueOf(2), Integer.valueOf(bo.size()));
        dung("lru qua lon giu", l.co("to") && l.soMuc() == 1);
        l.them("x", 10);
        bang("lru them sau qua lon bo to", Boolean.FALSE, Boolean.valueOf(l.co("to")));
        KhoSpriteLru m = new KhoSpriteLru(1000);
        m.them("0016", 300);
        m.them("0019", 200);
        m.dung("0016");
        KhoSpriteLru n = new KhoSpriteLru(1000);
        dung("lru doc ghi", n.doc(m.ghi()));
        bang("lru doc tong", Integer.valueOf(500), Integer.valueOf(n.tong()));
        bang("lru doc cu nhat", "0019", n.cuNhat(null));
        dung("lru doc hong", !n.doc(new byte[]{9, 9, 9}));
        bang("lru doc hong rong", Integer.valueOf(0), Integer.valueOf(n.soMuc()));
        bang("lru mac dinh 1MB", Integer.valueOf(1048576), Integer.valueOf(KhoSpriteLru.NGAN_SACH_MAC_DINH));
    }

    static void kiemGoi() throws Exception {
        byte[] g = GoiSprite.tao(16, false, new String[]{"0016.dl", "0016_0.png"}, new byte[][]{{1, 2, 3}, {4, 5}});
        Hashtable t = GoiSprite.doc(g, 16);
        dung("goi doc", t != null && ((byte[]) t.get("0016.dl")).length == 3 && ((byte[]) t.get("0016_0.png")).length == 2);
        bang("goi dex", Integer.valueOf(16), Integer.valueOf(GoiSprite.dex(g)));
        dung("goi sai dex", GoiSprite.doc(g, 17) == null);
        byte[] h = (byte[]) g.clone();
        h[0] = 'X';
        dung("goi sai dau", GoiSprite.doc(h, 16) == null);
        h = (byte[]) g.clone();
        h[4] = 2;
        dung("goi sai phien ban", GoiSprite.doc(h, 16) == null);
        byte[] cut = new byte[g.length - 1];
        System.arraycopy(g, 0, cut, 0, cut.length);
        dung("goi cut", GoiSprite.doc(cut, 16) == null);
        dung("goi thieu dl", GoiSprite.doc(GoiSprite.tao(16, false, new String[]{"0016_0.png"}, new byte[][]{{1}}), 16) == null);
        bang("khoa png", "0016", GoiSprite.khoaTuDuongDan("/pk/0016_3.png"));
        bang("khoa dl", "1025", GoiSprite.khoaTuDuongDan("/pk/1025.dl"));
        bang("khoa shiny (loai-bo-sung: ban sac mau tai theo nhu cau)", "0016s", GoiSprite.khoaTuDuongDan("/pk/0016s_3.png"));
        bang("khoa bong", null, GoiSprite.khoaTuDuongDan("/pk/bong.dl"));
        bang("khoa hu", null, GoiSprite.khoaTuDuongDan("/pk/hu_ember.png"));
    }

    static byte[] thanBang() throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeInt(0x1234abcd);
        o.writeShort(3);
        o.writeShort(16);
        o.writeUTF("Pidgey");
        o.writeByte(0);
        o.writeByte(9);
        o.writeByte(1);
        o.writeByte(1);
        o.writeByte(1 | 2 | 4);
        o.writeShort(999);
        o.writeUTF("Gholdengo");
        o.writeByte(16);
        o.writeByte(13);
        o.writeByte(4);
        o.writeByte(9);
        o.writeByte(4);
        o.writeShort(150);
        o.writeUTF("Mewtwo");
        o.writeByte(10);
        o.writeByte(-1);
        o.writeByte(6);
        o.writeByte(1);
        o.writeByte(1);
        o.flush();
        return bo.toByteArray();
    }

    static void kiemBang() throws Exception {
        byte[] than = thanBang();
        byte[] goi = new byte[than.length + 1];
        goi[0] = 1;
        System.arraycopy(than, 0, goi, 1, than.length);
        LoaiMang.ghiGui = new Vector();
        LoaiMang.docLoai(goi);
        bang("bang n", Integer.valueOf(3), Integer.valueOf(BangLoai.n));
        bang("bang bam", Integer.valueOf(0x1234abcd), Integer.valueOf(BangLoai.bam));
        bang("bang ten", "Gholdengo", BangLoai.ten(999));
        bang("bang he2", Integer.valueOf(13), Integer.valueOf(BangLoai.he2(999)));
        bang("bang bac", Integer.valueOf(6), Integer.valueOf(BangLoai.bac(150)));
        dung("bang chua mo", !BangLoai.daMo(999));
        dung("bang da mo", BangLoai.daMo(16));
        bang("bang khong co", "", BangLoai.ten(500));
        bang("hepoke roi ve bang", Integer.valueOf(16), Integer.valueOf(HePoke.he1(999)));
        bang("hepoke roi ve bang 2", Integer.valueOf(13), Integer.valueOf(HePoke.he2(999)));
        bang("hepoke bang tinh giu", Integer.valueOf(1), Integer.valueOf(HePoke.he1(4)));
        dung("bang hong giu cu", !BangLoai.docBang(new byte[]{0, 0, 0, 1, 0, 5, 0}) && BangLoai.n == 3);
        // pokedex
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(3);
        o.writeShort(2);
        o.writeShort(16);
        o.writeByte(1 | 2 | 4);
        o.writeShort(150);
        o.writeByte(1);
        o.flush();
        LoaiMang.docLoai(bo.toByteArray());
        dung("pokedex co", BangLoai.coPokedex);
        bang("pokedex 16", Integer.valueOf(7), Integer.valueOf(BangLoai.coPokedex(16)));
        bang("pokedex 150", Integer.valueOf(1), Integer.valueOf(BangLoai.coPokedex(150)));
        bang("pokedex 999", Integer.valueOf(0), Integer.valueOf(BangLoai.coPokedex(999)));
    }

    static void kiemQuaiCon() throws Exception {
        LoaiMang.doiMap();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(2);
        o.writeShort(77);
        o.writeShort(16);
        o.writeByte(1);
        o.writeByte(4);
        o.writeShort(78);
        o.writeShort(0);
        o.writeByte(0);
        o.writeByte(0);
        o.flush();
        LoaiMang.ghiGui = new Vector();
        network.Message msg = goi(-125, bo.toByteArray());
        dung("-125 da xu ly", LoaiMang.xuLy(msg));
        Monster m = quai();
        m.ID = 77;
        m.monster_type = 0;
        LoaiMang.apDung(m);
        bang("-125 cho ap dex", Integer.valueOf(16), Integer.valueOf(m.dexVe()));
        dung("-125 shiny", m.shinyVe());
        bang("-125 bac", Integer.valueOf(4), Integer.valueOf(m.pokeBac));
        bang("-125 ten theo bang", "Pidgey", m.getDisplayName());
        Monster m2 = quai();
        m2.ID = 77;
        LoaiMang.apDung(m2);
        bang("-125 cho chi ap 1 lan", Integer.valueOf(0), Integer.valueOf(m2.pokeDexRieng));
        m.datLoaiRieng(0, true, 5);
        bang("-125 dex 0 xoa", Integer.valueOf(0), Integer.valueOf(m.pokeDexRieng));
        dung("-125 dex 0 xoa shiny", !m.pokeShinyRieng && m.pokeBac == 0);
        dung("-125 xin bang", LoaiMang.ghiGui.size() >= 1);
        dung("lenh khac khong xu ly", !LoaiMang.xuLy(goi(-90, new byte[]{0, 1, 0, 2, 0})));
    }

    /** Monster không chạy hàm tạo (hàm tạo nạp Res - cần ảnh màn hình). */
    static Monster quai() throws Exception {
        java.lang.reflect.Field f = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Object u = f.get(null);
        return (Monster) u.getClass().getMethod("allocateInstance", Class.class).invoke(u, Monster.class);
    }

    static network.Message goi(int cmd, byte[] than) throws Exception {
        java.lang.reflect.Constructor<network.Message> k = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        k.setAccessible(true);
        return k.newInstance(Byte.valueOf((byte) cmd), than);
    }

    static void kiemPokeBody() throws Exception {
        dung("jar co 0001", PokeBody.coTrongJar(1));
        dung("jar khong co 0999", !PokeBody.coTrongJar(999));
        Vector gui = new Vector();
        LoaiMang.ghiGui = gui;
        KhoSprite.datLaiPhien();
        // Pokedex: không xin
        KhoSprite.choXin = false;
        PokeBody pb = PokeBody.lay(998, false);
        KhoSprite.choXin = true;
        bang("pokedex khong xin", Integer.valueOf(0), Integer.valueOf(demXin(gui, 998)));
        dung("pokedex ve bong", pb == null || "bong".equals(pb.khoa));
        // ngoài thế giới: lần đầu xin, lần sau không xin lại
        PokeBody.lay(998, false);
        PokeBody.lay(998, false);
        bang("the gioi xin 1 lan", Integer.valueOf(1), Integer.valueOf(demXin(gui, 998)));
        pb = PokeBody.lay(999, false);
        dung("dang tai ve bong", pb != null && "bong".equals(pb.khoa));
        bang("xin 999", Integer.valueOf(1), Integer.valueOf(demXin(gui, 999)));
        PokeBody.lay(997, false);
        bang("toi da 2 dang tai", Integer.valueOf(0), Integer.valueOf(demXin(gui, 997)));
        // máy chủ không có 998 -> thiếu, hàng đợi chạy tiếp (997)
        LoaiMang.docLoai(khuc(998, 0, 0, null, 0));
        dung("998 thieu", KhoSprite.thieu(998));
        bang("997 duoc xin sau", Integer.valueOf(1), Integer.valueOf(demXin(gui, 997)));
        // gói 999 về theo khúc
        byte[] g = goiTu("0001", 999);
        dung("goi gia > 4096 (nhieu khuc)", g.length > 4096);
        guiGoi(999, g);
        dung("999 da luu", KhoSprite.co(999));
        pb = PokeBody.lay(999, false);
        dung("999 hinh that", pb != null && "0999".equals(pb.khoa));
        dung("999 chan dung tu kho", PokeBody.chanDung(999, false) != null);
        dung("999 rong hinh", PokeBody.rongHinh(999, false) > 0);
        bang("999 khong xin lai", Integer.valueOf(1), Integer.valueOf(demXin(gui, 999)));
        // gói hỏng (sai dex) -> thiếu
        byte[] sai = goiTu("0001", 996);
        LoaiMang.docLoai(khuc(997, sai.length, 0, sai, Math.min(4096, sai.length)));
        for (int v = 4096; v < sai.length; v += 4096) {
            LoaiMang.docLoai(khuc(997, sai.length, v, sai, Math.min(4096, sai.length - v)));
        }
        dung("goi sai dex -> thieu", KhoSprite.thieu(997) && !KhoSprite.co(997));
        // ngân sách nhỏ: thêm loài mới bỏ loài cũ nhất
        int kich = g.length;
        for (int d = 990; d < 995; d++) {
            guiGoi(d, goiTu("0001", d));
        }
        dung("ngan sach", KhoSprite.dungLuong() <= KhoSprite.NGAN_SACH);
        dung("lru bo 999 (cu nhat)", !KhoSprite.co(999));
        dung("lru giu 994 (moi nhat)", KhoSprite.co(994));
        System.out.println("goi mau " + kich + " byte, kho dang dung " + KhoSprite.dungLuong() + "/" + KhoSprite.NGAN_SACH);
    }

    static byte[] docTep(File f) throws Exception {
        return f.isFile() ? java.nio.file.Files.readAllBytes(f.toPath()) : null;
    }

    static File thuMucGoi() {
        String[] thu = {"server/map/loai", "../server/map/loai"};
        for (int i = 0; i < thu.length; i++) {
            if (new File(thu[i]).isDirectory()) {
                return new File(thu[i]);
            }
        }
        return new File("server/map/loai");
    }

    /** Bản sắc màu tải theo nhu cầu + ngân sách bộ nhớ J2ME (KhoSprite 1 MB, bộ đệm ảnh PokeBody). */
    static void kiemShiny() throws Exception {
        File thu = thuMucGoi();
        bang("khoa duong dan shiny", "0011s", GoiSprite.khoaTuDuongDan("/pk/0011s_0.png"));
        bang("khoa duong dan shiny .dl", "0011s", GoiSprite.khoaTuDuongDan("/pk/0011s.dl"));
        bang("khoa duong dan thuong", "0011", GoiSprite.khoaTuDuongDan("/pk/0011_c.png"));
        bang("khoa duong dan la", null, GoiSprite.khoaTuDuongDan("/pk/0011x_0.png"));
        byte[] g11 = docTep(new File(thu, "0011.goi"));
        byte[] s11 = docTep(new File(thu, "0011s.goi"));
        dung("goi that 0011 + 0011s co", g11 != null && s11 != null);
        if (g11 == null || s11 == null) {
            return;
        }
        bang("co shiny dau goi thuong", Integer.valueOf(0), Integer.valueOf(GoiSprite.shiny(g11)));
        bang("co shiny dau goi sac mau", Integer.valueOf(1), Integer.valueOf(GoiSprite.shiny(s11)));
        dung("goi shiny doc duoc", GoiSprite.doc(s11, 11) != null && GoiSprite.doc(s11, 11).get("0011s.dl") != null);
        dung("luu sai co shiny -> tu choi", !KhoSprite.luu(11, false, s11) && !KhoSprite.luu(11, true, g11));
        Vector gui = new Vector();
        LoaiMang.ghiGui = gui;
        KhoSprite.datLaiPhien();
        PokeBody.lay(11, true);
        PokeBody.lay(11, true);
        bang("xin shiny 11 dung 1 lan", Integer.valueOf(1), Integer.valueOf(demXin(gui, 11, true)));
        guiGoi(11, g11);
        guiGoi(11, true, s11);
        dung("shiny 11 da luu", KhoSprite.co(11, true) && KhoSprite.co(11, false));
        PokeBody pb = PokeBody.lay(11, true);
        dung("shiny 11 hinh sac mau that", pb != null && "0011s".equals(pb.khoa));
        dung("shiny 11 chan dung sac mau", PokeBody.chanDung(11, true) != null);
        pb = PokeBody.lay(11, false);
        dung("thuong 11 van la ban thuong", pb != null && "0011".equals(pb.khoa));
        dung("goi that co khoi neo (trang phuc / the ban dong hanh)", pb != null && pb.dl.neo != null
                && PokeBody.lay(11, true).dl.neo != null);
        // loai chua co shiny (may chu tra tong = 0): ve ban thuong, khong xin lai
        LoaiMang.docLoai(khuc(11, true, 0, 0, null, 0));
        gui.removeAllElements();
        KhoSprite.datLaiPhien();
        byte[] g16 = docTep(new File(thu, "0016.goi"));
        if (g16 != null && docTep(new File(thu, "0016s.goi")) == null) {
            guiGoi(16, g16);
            PokeBody.lay(16, true);
            LoaiMang.docLoai(khuc(16, true, 0, 0, null, 0));
            dung("16 chua co shiny -> thieu", KhoSprite.thieu(16, true));
            pb = PokeBody.lay(16, true);
            dung("16 shiny ve ban thuong", pb != null && !"bong".equals(pb.khoa));
            PokeBody.lay(16, true);
            bang("16 shiny khong xin lai", Integer.valueOf(1), Integer.valueOf(demXin(gui, 16, true)));
        }
        LoaiMang.ghiGui = null;
        // ngân sách: mọi gói loài đã mở (thường + shiny) qua sổ LRU 1 MB như máy J2ME
        KhoSpriteLru l = new KhoSpriteLru(KhoSpriteLru.NGAN_SACH_MAC_DINH);
        int so = 0, soShiny = 0, maxGoi = 0, maxCap = 0;
        long tong = 0;
        boolean vuot = false;
        for (int d = 1; d <= 251; d++) {
            String k = PokeBody.tenKhoa(d, false);
            File f = new File(thu, k + ".goi"), fs = new File(thu, k + "s.goi");
            int cap = 0;
            if (f.isFile()) {
                so++;
                cap += (int) f.length();
                maxGoi = Math.max(maxGoi, (int) f.length());
                tong += f.length();
                l.them(k, (int) f.length());
                vuot |= l.tong() > l.nganSach();
            }
            if (fs.isFile()) {
                soShiny++;
                cap += (int) fs.length();
                maxGoi = Math.max(maxGoi, (int) fs.length());
                tong += fs.length();
                l.them(k + "s", (int) fs.length());
                vuot |= l.tong() > l.nganSach();
            }
            maxCap = Math.max(maxCap, cap);
        }
        dung("251 loai da mo co goi thuong", so == 251);
        dung("co goi shiny (" + soShiny + ")", soShiny >= 200);
        dung("LRU 1 MB khong luc nao vuot khi nap " + (so + soShiny) + " goi", !vuot);
        dung("goi lon nhat " + maxGoi + " < GOI_TOI_DA", maxGoi < KhoSprite.GOI_TOI_DA);
        dung("cap thuong + shiny lon nhat " + maxCap + " <= 1/4 ngan sach (con cho >= 4 loai)", maxCap * 4 <= KhoSpriteLru.NGAN_SACH_MAC_DINH);
        System.out.println("goi that: " + so + " thuong + " + soShiny + " shiny = " + tong + " byte, goi lon nhat " + maxGoi
                + ", cap lon nhat " + maxCap + ", LRU giu " + l.soMuc() + " goi / " + l.tong() + " byte");
        // bộ đệm ảnh PokeBody: chân dung thường + shiny nhiều loài, ToiDaPixel nhỏ -> không vượt
        int cu = PokeBody.ToiDaPixel;
        PokeBody.ToiDaPixel = 40 * 40 * 6;
        PokeBody.xoaBoDem();
        for (int d = 1; d <= 40; d++) {
            PokeBody.chanDung(d, false);
            PokeBody.chanDung(d, true);
            if (PokeBody.soPixelDangGiu() > PokeBody.ToiDaPixel) {
                vuot = true;
            }
        }
        dung("bo dem anh PokeBody khong vuot ToiDaPixel voi chan dung thuong + shiny", !vuot);
        PokeBody.ToiDaPixel = cu;
    }

    public static void main(String[] a) throws Exception {
        if (System.getProperty("htbb.duLieu") == null) {
            File t = File.createTempFile("kiemkho", "");
            t.delete();
            t.mkdirs();
            System.setProperty("htbb.duLieu", t.getAbsolutePath());
        }
        kiemLru();
        kiemGoi();
        kiemBang();
        kiemQuaiCon();
        // ngân sách nhỏ ~ 3 gói mẫu (đặt trước lần dùng kho đầu tiên)
        KhoSprite.NGAN_SACH = goiTu("0001", 999).length * 3 + 100;
        kiemPokeBody();
        KhoSprite.NGAN_SACH = KhoSpriteLru.NGAN_SACH_MAC_DINH;
        kiemShiny();
        System.out.println("KiemKhoSprite: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
