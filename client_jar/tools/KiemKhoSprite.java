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
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(0);
        o.writeShort(dex);
        o.writeByte(0);
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

    static int demXin(Vector gui, int dex) throws Exception {
        int n = 0;
        for (int i = 0; i < gui.size(); i++) {
            network.Message m = (network.Message) gui.elementAt(i);
            byte[] b = m.getData();
            if (m.command == -126 && b.length == 4 && b[0] == 0 && ((b[1] & 0xFF) << 8 | (b[2] & 0xFF)) == dex) {
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
        bang("khoa shiny", null, GoiSprite.khoaTuDuongDan("/pk/0016s_3.png"));
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
        System.out.println("KiemKhoSprite: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
