import com.htbb.pokemon.ChieuPokemon;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.SatThuongLauDai;
import com.htbb.pokemon.TuDongKeHoach;
import io.Message;
import io.Session;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import real.Char;
import real.Monster;

/**
 * 0.9.5: "Huc" (Thuong) vao Wooper (Nuoc/Dat) hien "Gan nhu vo hieu" + Tu dong bao "chieu kem hieu qua".
 * Bang 18 he dung; hai nguyen nhan sua o may chu:
 *  1. Quai hoi sinh doi loai (he sinh thai: Wooper -> Litwick hiem; alpha) - client giu actor cu cung id (lenh 4
 *     voi id da co chi doi vi tri) nen van ve Wooper trong khi may chu tinh voi Litwick (Ma/Lua: Thuong x0).
 *     Monster.baoDoiLoai gui lenh 20 + bo id khoi nearMons -> vong Region gui lai lenh 4 voi type moi.
 *  2. Goi -95 lay co khac he tu p.lauDaiCo (lan tinh sat thuong GAN NHAT, co the cua muc tieu khac):
 *     GoiLauDaiGame.don tinh lai co khac he theo dung muc tieu cua goi.
 */
public final class KiemHeHieuQua {

    private KiemHeHieuQua() {
    }

    public static void main(final String[] args) {
        chay();
        System.exit(Kiem.tongKet());
    }

    public static void chay() {
        Kiem.nhom("Khac he 18 he: he so that, co goi -95 theo dung muc tieu, Tu dong chon chieu, quai doi loai");
        try {
            heSo();
            goiDon();
            tuDong();
            doiLoai();
            cuoiThu();
            thongBao();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static void heSo() {
        Kiem.bang("Thuong vao Nuoc/Dat = x1", 100, HePokemon.heSoChinhXac(HePokemon.THUONG, HePokemon.NUOC, HePokemon.DAT));
        Kiem.bang("Co vao Nuoc/Dat = x4", 400, HePokemon.heSoChinhXac(HePokemon.CO, HePokemon.NUOC, HePokemon.DAT));
        Kiem.bang("Dien vao Dat = x0", 0, HePokemon.heSoChinhXac(HePokemon.DIEN, HePokemon.DAT, -1));
        Kiem.bang("Lua vao Co = x2", 200, HePokemon.heSoChinhXac(HePokemon.LUA, HePokemon.CO, -1));
        Kiem.bang("Thuong vao Nuoc/Dat: khong co khac he", 0, SatThuongLauDai.coHe(HePokemon.THUONG, HePokemon.NUOC, HePokemon.DAT));
        Kiem.bang("Co vao Nuoc/Dat: co 'Hieu qua cao'", SatThuongLauDai.CO_KHAC, SatThuongLauDai.coHe(HePokemon.CO, HePokemon.NUOC, HePokemon.DAT));
        Kiem.bang("Dien vao Dat: co 'Gan nhu vo hieu'", SatThuongLauDai.CO_GAN_MIEN, SatThuongLauDai.coHe(HePokemon.DIEN, HePokemon.DAT, -1));
        Kiem.bang("Lua vao Co: co 'Hieu qua cao'", SatThuongLauDai.CO_KHAC, SatThuongLauDai.coHe(HePokemon.LUA, HePokemon.CO, -1));
        Kiem.bang("Thuong vao Litwick (Ma/Lua): 'Gan nhu vo hieu'", SatThuongLauDai.CO_GAN_MIEN, SatThuongLauDai.coHe(HePokemon.THUONG, HePokemon.MA, HePokemon.LUA));
        Kiem.bang("Huc cua Bulbasaur/Eevee/Squirtle la he Thuong", "0,0,0",
                ChieuPokemon.heChieu(1, 0) + "," + ChieuPokemon.heChieu(2, 0) + "," + ChieuPokemon.heChieu(3, 0));
        Kiem.bang("Roi day leo (Bulbasaur chieu 1) la he Co", (int) HePokemon.CO, ChieuPokemon.heChieu(1, 1));
        final int[] kq = SatThuongLauDai.tinh(1000, 100, 100, HePokemon.THUONG, HePokemon.CO, HePokemon.DOC,
                HePokemon.NUOC, HePokemon.DAT, 0, false, 100);
        Kiem.bang("tinh(): Huc vao Wooper khong co khac he", 0, kq[1] & 28);
    }

    static final List<Message> GOI = new ArrayList<Message>();

    static Char nguoi() {
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        });
        p.charClass = 1;
        return p;
    }

    static Monster quai(final int id, final int he1, final int he2) {
        final Monster m = new Monster((byte) 1);
        m.id = (short) id;
        m.hePokemon = (byte) he1;
        m.he2 = (byte) he2;
        return m;
    }

    /** Byte co (offset 10 sau dau khung 3 byte) cua goi -95. */
    static int co(final Message m) {
        final byte[] b = m.toByteArray();
        return b[3 + 10] & 0xFF;
    }

    static void goiDon() throws Exception {
        com.htbb.pokemon.LauDai.dat(true);
        final Method don = Class.forName("real.GoiLauDaiGame").getDeclaredMethod("don", Char.class,
                Class.forName("real.LiveActor"), int.class);
        don.setAccessible(true);
        final Char p = nguoi();
        // co cu tu lan tinh vao Litwick (Thuong x0) + STAB; goi nay la cho Wooper
        p.lauDaiCo = SatThuongLauDai.CO_GAN_MIEN | SatThuongLauDai.CO_STAB | SatThuongLauDai.CO_CHI_MANG;
        p.lauDaiHe = HePokemon.THUONG;
        final Message mW = (Message) don.invoke(null, p, quai(7, HePokemon.NUOC, HePokemon.DAT), 100);
        Kiem.dung("-95 tao duoc", mW != null && mW.command == -95);
        final int cW = co(mW);
        Kiem.bang("-95 Huc vao Wooper: khong 'Gan nhu vo hieu'/'Kem'/'Hieu qua cao'", 0, cW & 28);
        Kiem.bang("-95 giu co chi mang + STAB", SatThuongLauDai.CO_CHI_MANG | SatThuongLauDai.CO_STAB, cW & 33);
        p.lauDaiCo = 0;
        final Message mL = (Message) don.invoke(null, p, quai(8, HePokemon.MA, HePokemon.LUA), 100);
        Kiem.bang("-95 Huc vao Litwick: 'Gan nhu vo hieu'", SatThuongLauDai.CO_GAN_MIEN, co(mL) & 28);
        p.lauDaiHe = HePokemon.CO;
        final Message mC = (Message) don.invoke(null, p, quai(9, HePokemon.NUOC, HePokemon.DAT), 100);
        Kiem.bang("-95 Roi day leo vao Wooper: 'Hieu qua cao'", SatThuongLauDai.CO_KHAC, co(mC) & 28);
        p.lauDaiHe = -1;
        final Message mK = (Message) don.invoke(null, p, quai(10, HePokemon.MA, -1), 100);
        Kiem.bang("-95 khong ro he chieu: khong co khac he", 0, co(mK) & 28);
    }

    static void tuDong() {
        final TuDongKeHoach.TinhHinh t = new TuDongKeHoach.TinhHinh();
        t.x = 400;
        t.y = 400;
        t.neoX = 400;
        t.neoY = 400;
        t.banKinh = 240;
        t.cap = 10;
        t.hp = 1000;
        t.maxhp = 1000;
        t.mp = 100;
        t.maxmp = 100;
        t.he1 = HePokemon.CO;
        t.he2 = HePokemon.DOC;
        t.thuoc = new int[200];
        // Bulbasaur cap 1 hai chieu (bang that CharManager.SKILL_DAM_PERCENT)
        final int stHuc = real.CharManager.SKILL_DAM_PERCENT[1][0][1];
        final int stRoi = real.CharManager.SKILL_DAM_PERCENT[1][1][1];
        t.chieu.add(new TuDongKeHoach.Chieu(0, ChieuPokemon.heChieu(1, 0), stHuc, 0, 0L));
        t.chieu.add(new TuDongKeHoach.Chieu(1, ChieuPokemon.heChieu(1, 1), stRoi, 0, 0L));
        final TuDongKeHoach.Quai w = new TuDongKeHoach.Quai(1, 420, 400, 10, 100, 100);
        w.he1 = HePokemon.NUOC;
        w.he2 = HePokemon.DAT;
        t.quai.add(w);
        final TuDongKeHoach.KeHoach k = TuDongKeHoach.lap(new TuDongKeHoach.CauHinh(), t);
        Kiem.bang("Tu dong: Bulbasaur vao Wooper chon Roi day leo (Co x4)", 1, k.so);
        Kiem.dung("Tu dong: ly do 'rat hieu qua' (" + k.ly + ")", k.ly.contains("rất hiệu quả"));
        t.chieu.remove(1);
        final TuDongKeHoach.KeHoach k2 = TuDongKeHoach.lap(new TuDongKeHoach.CauHinh(), t);
        Kiem.bang("Tu dong: chi co Huc vao Wooper -> 'dang danh' (x1, khong 'kem hieu qua')", "đang đánh", k2.ly);
    }

    @SuppressWarnings("unchecked")
    static void doiLoai() throws Exception {
        final real.Map map = new real.Map();
        map.initKhoQuaiVaNguoi();
        final Char p = nguoi();
        p.isBot = -1;
        p.nearMons = new Vector<Short>();
        final Monster m = quai(33, HePokemon.MA, HePokemon.LUA);
        m.map = map;
        m.inCountry = 0;
        p.nearMons.add(Short.valueOf(m.id));
        p.nearMons.add(Short.valueOf((short) 34));
        ((Vector<Char>) map.allPlayers.get(0)).add(p);
        GOI.clear();
        final Method bao = Monster.class.getDeclaredMethod("baoDoiLoai");
        bao.setAccessible(true);
        bao.invoke(m);
        Kiem.bang("doi loai: gui 1 goi", 1, GOI.size());
        final byte[] b = GOI.isEmpty() ? new byte[0] : GOI.get(0).toByteArray();
        Kiem.bang("doi loai: lenh 20 (go actor) cat 1 id 33", "20,1,0,33",
                b.length >= 6 ? b[0] + "," + b[3] + "," + b[4] + "," + b[5] : "?");
        Kiem.sai("doi loai: id bo khoi nearMons (vong Region gui lai lenh 4 voi type moi)", p.nearMons.contains(Short.valueOf((short) 33)));
        Kiem.dung("doi loai: quai khac giu nguyen trong nearMons", p.nearMons.contains(Short.valueOf((short) 34)));
        final String nguon = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/Monster.java")), "UTF-8");
        Kiem.dung("Monster.update: doi loai luc hoi sinh -> baoDoiLoai", nguon.contains("if (this.idTemplate != loaiCu) {")
                && nguon.indexOf("loaiCu = this.idTemplate") < nguon.indexOf("HeSinhThaiGame.khiHoiSinh(this)"));
    }

    static data.Animal thu(final int id, final int idImg, final int place) {
        final data.Animal a = new data.Animal();
        a.id = (short) id;
        a.idImg = (byte) idImg;
        a.place = (byte) place;
        a.name = "thu" + id;
        return a;
    }

    static String canhBao() {
        for (final Message m : GOI) {
            if (m.command == 37) {
                try {
                    final byte[] b = m.toByteArray();
                    return new java.io.DataInputStream(new java.io.ByteArrayInputStream(b, 3, b.length - 3)).readUTF();
                } catch (final Exception e) {
                    return "?";
                }
            }
        }
        return "";
    }

    static void cuoi(final Char p, final int id) throws Exception {
        final java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        final java.io.DataOutputStream d = new java.io.DataOutputStream(o);
        d.writeByte(0);
        d.writeShort(id);
        GOI.clear();
        real.doCuoiThu.doCuoiThu(p, new Message(-45, o.toByteArray()));
    }

    static void cuoiThu() throws Exception {
        final Char p = nguoi();
        p.map = new real.Map();
        p.animal = new Vector<data.Animal>();
        final data.Animal a = thu(1, 0, 1);
        final data.Animal b = thu(2, 2, 0);
        final data.Animal hong = thu(3, 99, 0);
        p.animal.add(a);
        p.animal.add(b);
        p.animal.add(hong);
        p.animalRide = a;
        p.rideHorse = 1;
        cuoi(p, 2);
        Kiem.dung("thu cuoi: dang cuoi A, xin cuoi B -> bao (" + canhBao() + ")", canhBao().contains("cưỡi thú khác"));
        Kiem.bang("thu cuoi: A van place 1 (ban cu dat 0 -> goi tui thu lech so dong)", 1, (int) a.place);
        Kiem.bang("thu cuoi: B van place 0", 0, (int) b.place);
        cuoi(p, 1);
        Kiem.dung("thu cuoi: xin cuoi lai con dang cuoi -> 'Đang cưỡi thú này.'", canhBao().contains("Đang cưỡi thú này"));
        p.animalRide = null;
        p.rideHorse = 0;
        a.place = 0;
        cuoi(p, 99);
        Kiem.dung("thu cuoi: id khong co -> bao, khong doi gi", canhBao().contains("Không tìm thấy") && p.animalRide == null);
        cuoi(p, 3);
        Kiem.dung("thu cuoi: idImg ngoai bang -> bao, khong cuoi nua chung", canhBao().contains("lỗi hình") && p.animalRide == null
                && hong.place == 0 && p.rideHorse == 0);
        p.animal.remove(hong);
        cuoi(p, 2);
        Kiem.dung("thu cuoi: cuoi B -> animalRide = B", p.animalRide == b);
        Kiem.bang("thu cuoi: B place 1, A place 0", "1,0", b.place + "," + a.place);
        Kiem.bang("thu cuoi: rideHorse theo bang Animal.rideHorse", (int) data.Animal.rideHorse[2], (int) p.rideHorse);
        final String mc = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/MessageCreator.java")), "UTF-8");
        Kiem.dung("goi tui thu (16 type 2): so dong dem theo place != 1", mc.contains("m.dos.writeByte(soThu);")
                && !mc.contains("m.dos.writeByte(p.animal.size() - ((p.animalRide != null) ? 1 : 0));"));
    }

    static void thongBao() throws Exception {
        Kiem.bang("nhan ngan anh quai hiem", "hiếm", com.htbb.pokemon.SuuTamSinhThai.tenAnhNgan(0));
        final String g = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/com/htbb/pokemon/HeSinhThaiGame.java")), "UTF-8");
        Kiem.dung("thong bao chup anh: 'Đã chụp X (hiếm) · Album N ảnh'", g.contains("\"Đã chụp \" + SuuTamSinhThai.tenLoai(dex) + \" (\" + SuuTamSinhThai.tenAnhNgan(kieu) + \") · Album \" + sau + \" ảnh\""));
        Kiem.sai("khong con 'Album: ... Album:'", g.contains("Album: đã chụp"));
    }
}
