import game.model.AnimalMove;
import game.model.Char;
import game.model.MainChar;
import game.model.monster.Pet;
import game.pokemon.DungDo;
import game.pokemon.PetDuyNhat;
import game.pokemon.TrangThaiNguoiChoi;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;

/**
 * Kiểm phía client (0.9.3) cho "trạng thái tức thì" và "một pet / người chơi":
 *  - kho TrangThaiNguoiChoi áp gói -105 của server (CÙNG chuỗi byte server/test/KiemTrangThaiVatPham.MAU_HEX): sau khi
 *    áp, nhân vật = sự thật server (HP / PP / max, xu, lượng, lượng khoá, cấp, số bình) - kể cả trả lại thay đổi đoán
 *    trước của client (bình đã trừ khi bấm, xu đã trừ khi bấm mua) khi server từ chối; phiên bản kho tăng;
 *  - thông báo -104 đọc byte kiểu (mua / dùng khác nhau) và vẫn đọc gói 0.9.2 không có byte kiểu;
 *  - PetDuyNhat: đổi map với pet kiểu 5 (Pet) không thành 2 pet, đổi pet AnimalMove <-> Pet thay thế, cất pet
 *    không sót, đổi nhân vật / đăng nhập lại bỏ pet cũ, người khác rời cảnh thì pet của họ bị gỡ, vào lại 1 pet.
 *
 *   C=<lớp game PC: client_pc/out/build/cls>; O=$(mktemp -d)
 *   javac -cp $C -d $O client_jar/tools/KiemTrangThai.java
 *   java -Djava.awt.headless=true -cp $O:$C:client_pc/out/build/res KiemTrangThai   # thoát 1 = hỏng
 */
public final class KiemTrangThai {
    static int dat, hong;

    /** = server/test/KiemTrangThaiVatPham.MAU_HEX (thân gói -105, kiểu DUNG): HP 400/1000, PP 250/500, xu 1234, cấp 50, bình 1 x5, 4 x2. */
    static final String MAU_HEX = "010300000190000003e8000000fa000001f400000000000004d2000000000000000000320000000000000000"
            + "020100000005040000000200000000";

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

    static byte[] hex(String h) {
        byte[] b = new byte[h.length() / 2];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(h.substring(2 * i, 2 * i + 2), 16);
        }
        return b;
    }

    static network.Message goi(int cmd, byte[] than) throws Exception {
        java.lang.reflect.Constructor<network.Message> k = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        k.setAccessible(true);
        return k.newInstance(Byte.valueOf((byte) cmd), than);
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        kho();
        thongBao();
        pet();
        System.out.println("KiemTrangThai: DAT " + dat + ", HONG " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }

    static void kho() throws Exception {
        MainChar mc = new MainChar();
        // client đoán trước khi bấm: đã trừ 1 bình HP (5 -> 4), cộng HP (400 -> 450), trừ xu mua (1234 -> 900)
        mc.potions = new int[Char.CHAR_POTION];
        mc.potions[1] = 4;
        mc.potions[4] = 2;
        mc.potions[7] = 3;   // bình không có ở server
        mc.hp = 450;
        mc.maxhp = 1000;
        mc.mp = 250;
        mc.maxmp = 500;
        mc.charXu = 900;
        mc.luong = 7;
        mc.luongKhoa = 3;
        int pb = TrangThaiNguoiChoi.phienBan;
        TrangThaiNguoiChoi.apDung(new DataInputStream(new ByteArrayInputStream(hex(MAU_HEX))), mc);
        bang("-105: HP = server", Integer.valueOf(400), Integer.valueOf(mc.hp));
        bang("-105: realHP = server (không đồng bộ trễ đè lại)", Integer.valueOf(400), Integer.valueOf(mc.realHP));
        bang("-105: maxhp", Integer.valueOf(1000), Integer.valueOf(mc.maxhp));
        bang("-105: PP", Integer.valueOf(250), Integer.valueOf(mc.mp));
        bang("-105: xu (trả lại xu đã trừ đoán trước)", Long.valueOf(1234), Long.valueOf(mc.charXu));
        bang("-105: lượng", Integer.valueOf(0), Integer.valueOf(mc.luong));
        bang("-105: lượng khoá", Integer.valueOf(0), Integer.valueOf(mc.luongKhoa));
        bang("-105: bình HP (trả lại bình đã trừ khi server từ chối)", Integer.valueOf(5), Integer.valueOf(mc.potions[1]));
        bang("-105: bình PP", Integer.valueOf(2), Integer.valueOf(mc.potions[4]));
        bang("-105: bình không có ở server -> 0", Integer.valueOf(0), Integer.valueOf(mc.potions[7]));
        bang("-105: cấp", Integer.valueOf(50), Integer.valueOf(mc.level));
        bang("-105: kiểu thao tác DUNG", Integer.valueOf(TrangThaiNguoiChoi.DUNG), Integer.valueOf(TrangThaiNguoiChoi.kieuCuoi));
        dung("-105: phiên bản kho tăng (bảng / HUD vẽ lại ngay)", TrangThaiNguoiChoi.phienBan > pb);
        dung("-105: kho client khớp server", TrangThaiNguoiChoi.khop(mc));
        mc.charXu = 1;
        dung("kho: lệch thì khop() = false", !TrangThaiNguoiChoi.khop(mc));
        // nhận qua đường MessageHandler (lệnh -105)
        dung("nhan(): nhận lệnh -105", TrangThaiNguoiChoi.nhan(goi(-105, hex(MAU_HEX))));
        dung("nhan(): bỏ qua lệnh khác", !TrangThaiNguoiChoi.nhan(goi(-104, new byte[]{0})));
    }

    static byte[] goi104(int loai, String s, int kieu) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(loai);
        o.writeUTF(s);
        if (kieu >= 0) {
            o.writeByte(kieu);
        }
        return bo.toByteArray();
    }

    static void thongBao() throws Exception {
        try {
            DungDo.nhan(goi(-104, goi104(0, "Đã mua Bình HP ×7 (−700 xu).", TrangThaiNguoiChoi.MUA)));
        } catch (Throwable e) {
            // âm thanh / màn chưa dựng: DungDo vẫn ghi kiểu trước
        }
        bang("-104: kiểu MUA", Integer.valueOf(TrangThaiNguoiChoi.MUA), Integer.valueOf(DungDo.kieuCuoi));
        bang("-104: nội dung mua", "Đã mua Bình HP ×7 (−700 xu).", DungDo.chuCuoi);
        try {
            DungDo.nhan(goi(-104, goi104(2, "Đã dùng Bình HP: +120 HP.", TrangThaiNguoiChoi.DUNG)));
        } catch (Throwable e) {
        }
        bang("-104: kiểu DUNG (khác MUA)", Integer.valueOf(TrangThaiNguoiChoi.DUNG), Integer.valueOf(DungDo.kieuCuoi));
        try {
            DungDo.nhan(goi(-104, goi104(1, "HP đã đầy, chưa cần dùng Bình HP.", -1)));
        } catch (Throwable e) {
        }
        bang("-104 server 0.9.2 (không byte kiểu): kiểu -1", Integer.valueOf(-1), Integer.valueOf(DungDo.kieuCuoi));
        bang("-104 server 0.9.2: vẫn đọc nội dung", "HP đã đầy, chưa cần dùng Bình HP.", DungDo.chuCuoi);
    }

    static Char nguoi(int id) {
        Char c = new Char();
        c.ID = (short) id;
        return c;
    }

    static AnimalMove animal(int idChu, int img) {
        AnimalMove m = new AnimalMove();
        m.catagory = 12;
        m.type = 2;
        m.imgID = (short) img;
        m.idSameMyOwnerID = (short) idChu;
        return m;
    }

    /** Pet kiểu 5 như new Pet(chủ, mẫu) nhưng không chạy hàm dựng (cần Res / màn): chỉ các trường PetDuyNhat dùng. */
    static Pet pet5(Char chu, int mau) {
        try {
            Class uc = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field f = uc.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Pet p = (Pet) uc.getMethod("allocateInstance", Class.class).invoke(f.get(null), Pet.class);
            p.catagory = 12;
            p.ID = chu.ID;
            p.idTemplate = mau;
            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void pet() {
        Vector ds = new Vector();
        Char toi = nguoi(7);
        ds.addElement(toi);
        // pet kiểu 5 (Pet) đặt vào cảnh, Char.myPet là MẪU thông tin
        Pet p5 = pet5(toi, 3);
        AnimalMove mau = animal(7, 3);
        mau.type = 5;
        toi.myPet = mau;
        PetDuyNhat.dat(ds, toi, p5);
        bang("pet kiểu 5: 1 pet", Integer.valueOf(1), Integer.valueOf(PetDuyNhat.dem(ds, 7)));
        // đổi map: danh sách chỉ còn nhân vật chính rồi thêm lại pet (bản cũ thêm MẪU myPet -> 2 pet khi máy chủ gửi lại)
        ds.removeAllElements();
        ds.addElement(toi);
        PetDuyNhat.sauDoiMap(ds, toi);
        bang("đổi map: đúng 1 pet (actor trong cảnh, không phải mẫu)", Integer.valueOf(1), Integer.valueOf(PetDuyNhat.dem(ds, 7)));
        dung("đổi map: pet thêm lại là Pet trong cảnh", ds.contains(p5) && !ds.contains(mau));
        // máy chủ gửi lại trang bị (cùng pet): vẫn 1
        PetDuyNhat.dat(ds, toi, p5);
        bang("gửi lại trang bị sau đổi map: 1 pet", Integer.valueOf(1), Integer.valueOf(PetDuyNhat.dem(ds, 7)));
        // đổi sang pet AnimalMove: thay, không giữ Pet cũ
        AnimalMove a2 = animal(7, 9);
        PetDuyNhat.dat(ds, toi, a2);
        bang("đổi pet Pet -> AnimalMove: 1 pet", Integer.valueOf(1), Integer.valueOf(PetDuyNhat.dem(ds, 7)));
        dung("đổi pet: Pet cũ bị gỡ", !ds.contains(p5));
        // đổi lại sang Pet kiểu 5
        Pet p6 = pet5(toi, 4);
        PetDuyNhat.dat(ds, toi, p6);
        bang("đổi pet AnimalMove -> Pet: 1 pet", Integer.valueOf(1), Integer.valueOf(PetDuyNhat.dem(ds, 7)));
        dung("đổi pet: AnimalMove cũ bị gỡ", !ds.contains(a2));
        // bản thừa chen vào (gói trễ): dọn định kỳ chỉ giữ pet trong cảnh
        ds.addElement(animal(7, 11));
        PetDuyNhat.donDep(ds);
        bang("dọn: pet thừa của chủ bị gỡ", Integer.valueOf(1), Integer.valueOf(PetDuyNhat.dem(ds, 7)));
        // cất pet
        PetDuyNhat.cat(ds, toi);
        bang("cất pet: 0 pet", Integer.valueOf(0), Integer.valueOf(PetDuyNhat.dem(ds, 7)));
        dung("cất pet: myPet / petCanh xoá", toi.myPet == null && toi.petCanh == null);
        ds.removeAllElements();
        ds.addElement(toi);
        PetDuyNhat.sauDoiMap(ds, toi);
        bang("cất rồi đổi map: không hiện lại", Integer.valueOf(0), Integer.valueOf(PetDuyNhat.dem(ds, 7)));
        // pet AnimalMove đang mờ dần (cất) không được thêm lại khi đổi map
        AnimalMove a3 = animal(7, 2);
        PetDuyNhat.dat(ds, toi, a3);
        a3.status = 3;
        ds.removeAllElements();
        ds.addElement(toi);
        PetDuyNhat.sauDoiMap(ds, toi);
        dung("pet đang biến mất: không thêm lại khi đổi map", !ds.contains(a3));
        // đổi nhân vật / đăng nhập lại (ID khác): pet của nhân vật cũ bỏ
        AnimalMove a4 = animal(7, 5);
        PetDuyNhat.dat(ds, toi, a4);
        toi.myPet = a4;
        toi.ID = 8;
        ds.removeAllElements();
        ds.addElement(toi);
        PetDuyNhat.sauDoiMap(ds, toi);
        dung("đổi nhân vật: không mang pet nhân vật cũ", !ds.contains(a4) && toi.petCanh == null && toi.myPet == null);
        // người chơi khác: rời cảnh thì pet bị gỡ, vào lại không nhân đôi
        Char ban = nguoi(30);
        ds.addElement(ban);
        AnimalMove pb = animal(30, 6);
        pb.myOwner = ban;
        PetDuyNhat.dat(ds, ban, pb);
        bang("người khác: 1 pet", Integer.valueOf(1), Integer.valueOf(PetDuyNhat.dem(ds, 30)));
        ds.removeElement(ban);
        PetDuyNhat.donDep(ds);
        bang("người khác rời cảnh: pet bị gỡ", Integer.valueOf(0), Integer.valueOf(PetDuyNhat.dem(ds, 30)));
        Char banMoi = nguoi(30);
        ds.addElement(banMoi);
        AnimalMove pb2 = animal(30, 6);
        pb2.myOwner = banMoi;
        PetDuyNhat.dat(ds, banMoi, pb2);
        PetDuyNhat.dat(ds, banMoi, pb2);
        bang("người khác vào lại + gói trang bị 2 lần: 1 pet", Integer.valueOf(1), Integer.valueOf(PetDuyNhat.dem(ds, 30)));
    }
}
