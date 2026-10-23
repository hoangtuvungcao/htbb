import com.htbb.pokemon.NapTienLogic;
import com.htbb.pokemon.OThu;
import data.Animal;
import data.Pet;
import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import real.Char;
import real.GemTemplate;
import real.MessageCreator;
import real.QuanLyThuGame;

/**
 * Quan ly thu (23/10/2026): o thu chung thu cuoi + Pokemon di theo (co ban 20, +1 / ve, toi da 60), tha ve tu nhien lay
 * it xu, khoa thu, bao ve con dang dung / giao dich / cho tang, bam hai lan + tranh chap, nhan vat cu vuot o, ve mo o
 * thu (khong tru ve khi da toi da, cua hang khong ban ve thua), goi 16 noi them (client &gt;= 911), lenh -119. Khong CSDL.
 */
public final class KiemQuanLyThu {

    private KiemQuanLyThu() {
    }

    static final List<Message> GOI = new ArrayList<Message>();
    static short idKe = 22000;
    static int idDb = 900000;

    public static void chay() {
        Kiem.nhom("Quan ly thu: o thu, tha ve tu nhien, khoa, ve mo o thu");
        final GemTemplate[] gemCu = real.Map.gemTemplate;
        final data.PetTemplate[] petCu = real.Map.petTemplate;
        try {
            if (real.Map.petTemplate == null) {
                real.Map.loadPetTemplate();
            }
            final GemTemplate[] g = new GemTemplate[QuanLyThuGame.ID_VE + 1];
            for (int i = 0; i < g.length; i++) {
                g[i] = new GemTemplate();
                g[i].id = (short) i;
                g[i].name = "Nguyên liệu " + i;
            }
            g[real.MoRongTuiGame.ID_VE] = real.MoRongTuiGame.taoMauVe();
            g[QuanLyThuGame.ID_VE] = QuanLyThuGame.taoMauVe();
            real.Map.gemTemplate = g;
            thuan();
            thaVaXu();
            baoVe();
            bamHaiLan();
            tranhChap();
            oDayVaVuot();
            ve();
            goiTin();
            maNguon();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
            real.Map.petTemplate = petCu;
        }
    }

    static Char nhanVat(final int ban) {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                synchronized (GOI) {
                    GOI.add(m);
                }
            }
        });
        p.getSession().banClient = ban;
        p.charname = "kiemthu" + (idKe++);
        p.animal = new Vector<Animal>();
        p.pet = new Vector<Pet>();
        p.userTrade = new Vector<Char>();
        p.nameTangPet = "";
        return p;
    }

    static Pet pet(final Char p, final int cap) {
        final Pet t = new Pet(0);
        t.id = p.getIDItem();
        t.idDB = ++idDb;
        t.name = "Hoothoot";
        t.lvDetail.lv = cap;
        p.pet.add(t);
        return t;
    }

    static Animal thuCuoi(final Char p, final int idImg, final int level) {
        final Animal a = new Animal();
        a.id = p.getIDItem();
        a.dbid = ++idDb;
        a.idImg = (byte) idImg;
        a.level = (byte) level;
        a.name = "Rapidash";
        p.animal.add(a);
        return a;
    }

    static QuanLyThuGame.KetQua tha(final Char p, final Object... ds) {
        final byte[] loai = new byte[ds.length];
        final short[] id = new short[ds.length];
        for (int i = 0; i < ds.length; i++) {
            loai[i] = (byte) (ds[i] instanceof Animal ? 0 : 1);
            id[i] = ds[i] instanceof Animal ? ((Animal) ds[i]).id : ((Pet) ds[i]).id;
        }
        return QuanLyThuGame.tha(p, loai, id);
    }

    // ------------------------------------------------------------------ ham thuan

    static void thuan() {
        Kiem.bang("o co ban mac dinh 20", 20, QuanLyThuGame.coBan());
        Kiem.bang("o toi da mac dinh 60", 60, QuanLyThuGame.toiDa());
        Kiem.bang("suc chua 0 ve = 20", 20, OThu.sucChua(20, 60, 0));
        Kiem.bang("suc chua 5 ve = 25", 25, OThu.sucChua(20, 60, 5));
        Kiem.bang("suc chua khong vuot toi da", 60, OThu.sucChua(20, 60, 99));
        Kiem.bang("toi da < co ban -> keo len co ban", 30, OThu.sucChua(30, 10, 0));
        Kiem.bang("toi da kep tran cung 120", 120, OThu.toiDa(20, 500));
        Kiem.bang("o con mo duoc 40", 40, OThu.oConMoDuoc(20, 60, 0));
        Kiem.bang("o con mo duoc 0 khi da toi da", 0, OThu.oConMoDuoc(20, 60, 40));
        Kiem.dung("day khi so thu = suc chua", OThu.day(20, 20));
        Kiem.dung("vuot (nhan vat cu) van la day", OThu.day(31, 20));
        Kiem.dung("duoi suc chua khong day", !OThu.day(19, 20));
        Kiem.bang("xu tha cap 1 thuong = 22", 22L, OThu.xuTha(1, 1, false, 20, 2, 300));
        Kiem.bang("xu tha cap 10 bac 3 = 120", 120L, OThu.xuTha(10, 3, false, 20, 2, 300));
        Kiem.bang("xu tha co tran 300", 300L, OThu.xuTha(100, 5, false, 20, 2, 300));
        Kiem.bang("thu co han: 0 xu", 0L, OThu.xuTha(50, 5, true, 20, 2, 300));
        Kiem.bang("cap / hiem la duoc kep", OThu.xuTha(100, 5, false, 20, 2, 99999), OThu.xuTha(5000, 50, false, 20, 2, 99999));
        // ban trung: loai {A,A,A,B,B,C}, cap {1,3,2,1,1,9}; con 4 (B cap 1) khong tha duoc -> giu con 4
        final boolean[] t = OThu.banTrung(new int[]{1, 1, 1, 2, 2, 3}, new int[]{1, 3, 2, 1, 1, 9},
                new boolean[]{true, true, true, true, false, true});
        Kiem.bang("ban trung: giu cap cao nhat moi loai, uu tien giu con dang dung khi bang cap",
                "[true, false, true, true, false, false]", java.util.Arrays.toString(t));
        final boolean[] t2 = OThu.banTrung(new int[]{7, 7}, new int[]{5, 1}, new boolean[]{true, false});
        Kiem.bang("ban trung: con thap hon dang khoa -> khong chon", "[false, false]", java.util.Arrays.toString(t2));
        Kiem.dung("mua ve: con thieu 40 o, 39 ve -> mua duoc", OThu.muaVeDuoc(20, 60, 0, 39));
        Kiem.dung("mua ve: 40 ve dang giu du -> khong ban them", !OThu.muaVeDuoc(20, 60, 0, 40));
        Kiem.dung("mua ve: da toi da -> khong ban", !OThu.muaVeDuoc(20, 60, 40, 0));
    }

    // ------------------------------------------------------------------ tha + xu

    static void thaVaXu() {
        final Char p = nhanVat(911);
        final Pet a = pet(p, 1);
        final Pet b = pet(p, 10);
        final Animal c = thuCuoi(p, Animal.PHUONG_HOANG, 2);
        final Animal d = thuCuoi(p, 1, 0);
        d.texpire = System.currentTimeMillis() + 3600000L;
        Kiem.bang("xu tha pet cap 1 = 22", 22L, QuanLyThuGame.xuTha(a));
        Kiem.bang("xu tha pet cap 10 = 40", 40L, QuanLyThuGame.xuTha(b));
        Kiem.bang("xu tha thu cuoi cao cap level 2 = (20+2x3)x3 = 78", 78L, QuanLyThuGame.xuTha(c));
        Kiem.bang("xu tha thu cuoi co han = 0", 0L, QuanLyThuGame.xuTha(d));
        final long xu0 = p.getxu();
        QuanLyThuGame.KetQua kq = tha(p, a);
        Kiem.bang("tha 1 pet: 1 con", 1, kq.soTha);
        Kiem.bang("tha 1 pet: +22 xu", xu0 + 22L, p.getxu());
        Kiem.dung("tha 1 pet: bien khoi danh sach", !p.pet.contains(a));
        kq = tha(p, b, c, d);
        Kiem.bang("tha nhieu: 3 con", 3, kq.soTha);
        Kiem.bang("tha nhieu: tong xu 40 + 78 + 0", 118L, kq.xu);
        Kiem.bang("tha nhieu: xu nhan vat", xu0 + 22L + 118L, p.getxu());
        Kiem.bang("tha het: danh sach rong", 0, QuanLyThuGame.soThu(p));
        Kiem.dung("cau ket qua co so xu", QuanLyThuGame.cauKetQua(kq).contains("118"));
    }

    // ------------------------------------------------------------------ bao ve

    static void baoVe() {
        final Char p = nhanVat(911);
        final Animal cuoi = thuCuoi(p, 1, 1);
        cuoi.place = 1;
        p.animalRide = cuoi;
        final Pet theo = pet(p, 1);
        theo.place = 1;
        p.petUsing = theo;
        final Pet khoa = pet(p, 1);
        Kiem.bang("khoa: thanh cong", null, QuanLyThuGame.doiKhoa(p, 1, khoa.id, true));
        final Pet giaoDich = pet(p, 1);
        p.idPetTang = giaoDich.id;
        p.nameTangPet = "nguoimua";
        final Animal choTang = thuCuoi(p, 1, 1);
        p.thuChoTang = choTang;
        final Pet tuDo = pet(p, 1);
        final long xu0 = p.getxu();
        final QuanLyThuGame.KetQua kq = tha(p, cuoi, theo, khoa, giaoDich, choTang);
        Kiem.bang("bao ve: khong tha con nao", 0, kq.soTha);
        Kiem.bang("bao ve: 5 con bi bo qua", 5, kq.boQua);
        Kiem.bang("bao ve: khong xu", xu0, p.getxu());
        Kiem.bang("bao ve: van 6 thu", 6, QuanLyThuGame.soThu(p));
        Kiem.bang("ly do: dang cuoi", "Đang cưỡi", QuanLyThuGame.lyDoKhongTha(p, cuoi));
        Kiem.bang("ly do: dang di theo", "Đang đi theo", QuanLyThuGame.lyDoKhongTha(p, theo));
        Kiem.bang("ly do: da khoa", "Đã khoá", QuanLyThuGame.lyDoKhongTha(p, khoa));
        Kiem.bang("ly do: dang giao dich", "Đang giao dịch", QuanLyThuGame.lyDoKhongTha(p, giaoDich));
        Kiem.bang("ly do: cho tang", "Đang chờ tặng", QuanLyThuGame.lyDoKhongTha(p, choTang));
        Kiem.bang("mo khoa", null, QuanLyThuGame.doiKhoa(p, 1, khoa.id, false));
        Kiem.bang("mo khoa xong tha duoc", 1, tha(p, khoa).soTha);
        Kiem.dung("khoa luu: chuoi rong sau khi tha", QuanLyThuGame.ghiKhoa(p).isEmpty());
        QuanLyThuGame.doiKhoa(p, 1, tuDo.id, true);
        Kiem.bang("khoa luu dang a/p<id>", "p" + tuDo.idDB, QuanLyThuGame.ghiKhoa(p));
        final Char q = nhanVat(911);
        q.pet.add(tuDo);
        QuanLyThuGame.docKhoa(q, "p" + tuDo.idDB + ",xx,a0,p12abc");
        Kiem.dung("doc khoa: bo gia tri la", q.thuKhoa.size() == 1 && QuanLyThuGame.daKhoa(q, tuDo));
        p.userTrade.add(q);
        final QuanLyThuGame.KetQua kq2 = tha(p, tuDo);
        Kiem.dung("dang trao doi: tu choi ca lenh", kq2.soTha == 0 && kq2.loi != null);
        p.userTrade.clear();
    }

    // ------------------------------------------------------------------ bam hai lan / tranh chap

    static void bamHaiLan() {
        final Char p = nhanVat(911);
        final Pet a = pet(p, 1);
        final long xu0 = p.getxu();
        Kiem.bang("bam lan 1: tha", 1, tha(p, a).soTha);
        final QuanLyThuGame.KetQua lan2 = tha(p, a);
        Kiem.bang("bam lan 2: khong tha gi", 0, lan2.soTha);
        Kiem.bang("bam lan 2: khong cong xu them", xu0 + 22L, p.getxu());
        Kiem.dung("bam lan 2: bao ro", QuanLyThuGame.cauKetQua(lan2).contains("đã được thả"));
        final Pet b = pet(p, 1);
        Kiem.bang("cung id hai lan trong mot goi: chi tinh 1", 1, tha(p, b, b).soTha);
    }

    static void tranhChap() throws Exception {
        final Char p = nhanVat(911);
        final List<Object> ds = new ArrayList<Object>();
        for (int i = 0; i < 30; i++) {
            ds.add(pet(p, 1));
        }
        final long xu0 = p.getxu();
        final CountDownLatch bat = new CountDownLatch(1);
        final long[] tong = new long[8];
        final Thread[] th = new Thread[8];
        for (int k = 0; k < th.length; k++) {
            final int kk = k;
            th[k] = new Thread(() -> {
                try {
                    bat.await();
                } catch (final InterruptedException ignored) {
                }
                tong[kk] = tha(p, ds.toArray()).xu;
            });
            th[k].start();
        }
        bat.countDown();
        for (final Thread t : th) {
            t.join(10000);
        }
        long s = 0;
        for (final long x : tong) {
            s += x;
        }
        Kiem.bang("tranh chap 8 luong: tong xu = 30 con x 22", 30L * 22L, s);
        Kiem.bang("tranh chap 8 luong: xu nhan vat dung mot lan", xu0 + 30L * 22L, p.getxu());
        Kiem.bang("tranh chap: het thu", 0, QuanLyThuGame.soThu(p));
    }

    // ------------------------------------------------------------------ day o / vuot

    static void oDayVaVuot() {
        final Char p = nhanVat(911);
        for (int i = 0; i < 19; i++) {
            pet(p, 1);
        }
        Kiem.dung("19/20: chua day", !QuanLyThuGame.day(p));
        Kiem.dung("19/20: chanNhan cho qua", !QuanLyThuGame.chanNhan(p, "kiem"));
        thuCuoi(p, 1, 1);
        Kiem.dung("20/20 (thu cuoi + pet chung o): day", QuanLyThuGame.day(p));
        GOI.clear();
        Kiem.dung("20/20: chanNhan tu choi", QuanLyThuGame.chanNhan(p, "kiem"));
        Kiem.dung("20/20: co bao loi", !GOI.isEmpty());
        final int truoc = p.animal.size();
        Kiem.dung("20/20: createPoro tu choi", !p.createPoro(1L, false));
        Kiem.bang("20/20: khong them thu cuoi", truoc, p.animal.size());
        final int truocPet = p.pet.size();
        p.createPet(0, 10);
        Kiem.bang("20/20: createPet khong them", truocPet, p.pet.size());
        // nhan vat cu: 26 thu, giu nguyen; tha 6 -> 20 van day; tha 7 -> 19 nhan duoc
        for (int i = 0; i < 6; i++) {
            pet(p, 1);
        }
        Kiem.bang("vuot o: giu nguyen 26 con", 26, QuanLyThuGame.soThu(p));
        Kiem.dung("vuot o: day", QuanLyThuGame.day(p));
        final List<Object> bo = new ArrayList<Object>(p.pet.subList(0, 6));
        tha(p, bo.toArray());
        Kiem.dung("tha 6 -> 20/20 van day", QuanLyThuGame.day(p));
        tha(p, p.pet.get(0));
        Kiem.dung("tha 7 -> 19/20 nhan duoc", !QuanLyThuGame.day(p));
        p.oThuMo = 5;
        Kiem.bang("o da mo 5 -> suc chua 25", 25, QuanLyThuGame.sucChua(p));
    }

    // ------------------------------------------------------------------ ve

    static void ve() {
        final Char p = nhanVat(911);
        Kiem.bang("ve: id 271", (int) QuanLyThuGame.ID_VE, (int) QuanLyThuGame.idVe());
        Kiem.bang("ve: kieu TU_BINH (nut Dung moi client)", (int) GemTemplate.TYPE_TU_BINH, (int) real.Map.gemTemplate[QuanLyThuGame.ID_VE].type);
        Kiem.bang("ve: anh 96", 96, (int) real.Map.gemTemplate[QuanLyThuGame.ID_VE].idImage);
        Kiem.dung("ve: chua co ve -> tu choi", QuanLyThuGame.dungVe(p, null) != null);
        p.doAddGemItem(QuanLyThuGame.ID_VE, 2, true);
        Kiem.bang("ve: dung thanh cong", null, QuanLyThuGame.dungVe(p, null));
        Kiem.bang("ve: +1 o", 1, (int) p.oThuMo);
        Kiem.bang("ve: suc chua 21", 21, QuanLyThuGame.sucChua(p));
        Kiem.bang("ve: con 1 ve", 1, QuanLyThuGame.soVe(p));
        Kiem.bang("mua ve: con thieu o -> duoc", null, QuanLyThuGame.kiemMuaVe(p));
        p.oThuMo = 40;
        final String loi = QuanLyThuGame.dungVe(p, null);
        Kiem.dung("ve: da toi da 60 -> tu choi", loi != null && loi.contains("tối đa"));
        Kiem.bang("ve: toi da -> KHONG tru ve", 1, QuanLyThuGame.soVe(p));
        Kiem.bang("ve: toi da -> o giu 40", 40, (int) p.oThuMo);
        Kiem.dung("mua ve: da toi da -> tu choi", QuanLyThuGame.kiemMuaVe(p) != null);
        p.oThuMo = 39;
        Kiem.dung("mua ve: con 1 o nhung dang giu 1 ve -> khong ban ve thua", QuanLyThuGame.kiemMuaVe(p) != null);
        QuanLyThuGame.traoVe(p, 1);
        Kiem.bang("trao ve cua hang: ve KHOA", 2, p.listGemitemLock[QuanLyThuGame.ID_VE]);
        // cua hang luong: cung cach trao mo_rong_tui nhu ve hanh trang, phan biet theo ma
        NapTienLogic.Mon mon = null;
        for (final NapTienLogic.Mon m : NapTienLogic.MON_MAC_DINH) {
            if (NapTienLogic.MA_VE_MO_O_THU.equals(m.ma)) {
                mon = m;
            }
        }
        Kiem.dung("cua hang: co mon ve_mo_o_thu", mon != null && NapTienLogic.laVeMoOThu(mon) && !NapTienLogic.laVeMoRong(mon));
        Kiem.bang("cua hang: mon hop le", null, mon == null ? "thieu" : NapTienLogic.kiemMon(mon));
        Kiem.bang("cua hang: gia 30 luong", 30, mon == null ? 0 : mon.giaLuong);
    }

    // ------------------------------------------------------------------ goi tin

    static byte[] than(final Message m) {
        final byte[] b = m.toByteArray();
        final int dau = (b.length > 0 && b[0] == (byte) -128) ? 6 : 3;
        return java.util.Arrays.copyOfRange(b, dau, b.length);
    }

    static void goiTin() throws Exception {
        final Char moi = nhanVat(QuanLyThuGame.BAN_CLIENT);
        final Char cu = nhanVat(910);
        for (final Char p : new Char[]{moi, cu}) {
            pet(p, 3);
            thuCuoi(p, 1, 2);
        }
        QuanLyThuGame.doiKhoa(moi, 0, moi.animal.get(0).id, true);
        final byte[] bMoi = than(MessageCreator.createCharInventoryMessage(moi, 2));
        final byte[] bCu = than(MessageCreator.createCharInventoryMessage(cu, 2));
        Kiem.dung("goi 16: client cu khong co phan noi them", bMoi.length > bCu.length);
        // doc phan noi them: bo qua dung so byte cua goi cu
        final DataInputStream r = new DataInputStream(new ByteArrayInputStream(bMoi, bCu.length, bMoi.length - bCu.length));
        Kiem.bang("noi them: phien 1", 1, (int) r.readByte());
        Kiem.bang("noi them: so thu 2", 2, (int) r.readShort());
        Kiem.bang("noi them: suc chua 20", 20, (int) r.readShort());
        Kiem.bang("noi them: toi da 60", 60, (int) r.readShort());
        Kiem.bang("noi them: so ve 0", 0, (int) r.readShort());
        Kiem.bang("noi them: id ve", 271, (int) r.readShort());
        r.readInt();
        Kiem.bang("noi them: 2 dong", 2, (int) r.readByte());
        final int co0 = r.readByte();
        Kiem.bang("dong 1 (thu cuoi): da khoa + khong tha", 3, co0);
        Kiem.bang("dong 1: loai 100001", 100001, r.readInt());
        Kiem.bang("dong 1: cap 2", 2, (int) r.readShort());
        r.readInt();
        Kiem.bang("dong 1: ly do", "Đã khoá", r.readUTF());
        Kiem.bang("dong 2 (pet): tha duoc", 0, (int) r.readByte());
        r.readInt();
        r.readShort();
        Kiem.bang("dong 2: xu 26", 26, r.readInt());
        Kiem.bang("dong 2: ly do rong", "", r.readUTF());
        Kiem.bang("noi them: het goi", 0, r.available());
        // lenh -119 kieu 1 (tha) tu client
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(b);
        d.writeByte(QuanLyThuGame.KIEU_THA);
        d.writeByte(1);
        d.writeByte(1);
        d.writeShort(moi.pet.get(0).id);
        d.flush();
        GOI.clear();
        QuanLyThuGame.xuLy(moi, new Message(QuanLyThuGame.LENH, b.toByteArray()));
        Kiem.bang("-119 tha: con 1 thu", 1, QuanLyThuGame.soThu(moi));
        boolean co16 = false;
        for (final Message m : GOI) {
            co16 |= m.command == 16;
        }
        Kiem.dung("-119 tha: gui lai danh sach (goi 16)", co16);
        // -119 kieu 2 khoa + kieu 3 dung ve
        final ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        final DataOutputStream d2 = new DataOutputStream(b2);
        d2.writeByte(QuanLyThuGame.KIEU_KHOA);
        d2.writeByte(0);
        d2.writeShort(moi.animal.get(0).id);
        d2.writeByte(0);
        d2.flush();
        QuanLyThuGame.xuLy(moi, new Message(QuanLyThuGame.LENH, b2.toByteArray()));
        Kiem.dung("-119 mo khoa", !QuanLyThuGame.daKhoa(moi, moi.animal.get(0)));
        moi.doAddGemItem(QuanLyThuGame.ID_VE, 1, false);
        QuanLyThuGame.xuLy(moi, new Message(QuanLyThuGame.LENH, new byte[]{QuanLyThuGame.KIEU_DUNG_VE}));
        Kiem.bang("-119 dung ve: +1 o", 1, (int) moi.oThuMo);
    }

    // ------------------------------------------------------------------ ma nguon / SQL

    static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
    }

    static void maNguon() throws Exception {
        Kiem.dung("MapXuLyLenh noi lenh -119", doc("src/real/MapXuLyLenh.java").contains("case QuanLyThuGame.LENH:"));
        Kiem.dung("Map.doTuBinh noi ve mo o thu", doc("src/real/Map.java").contains("QuanLyThuGame.dungVeTuGoiCu(player, lock)"));
        Kiem.dung("MapNapTaiNguyen co mau ve", doc("src/real/MapNapTaiNguyen.java").contains("QuanLyThuGame.taoMauVe()"));
        final String ch = doc("src/real/Char.java");
        int chan = 0;
        for (int i = ch.indexOf("QuanLyThuGame.chanNhan(this"); i >= 0; i = ch.indexOf("QuanLyThuGame.chanNhan(this", i + 1)) {
            chan++;
        }
        Kiem.dung("Char: moi ham tao thu / pet deu chan khi day o (>= 18 cho)", chan >= 18);
        Kiem.dung("no trung pet chan TRUOC khi bo trung", ch.indexOf("QuanLyThuGame.chanNhan(this, \"no trung pet\")")
                < ch.indexOf("this.iItems.remove(itemtrung);"));
        Kiem.dung("giao dich pet: nguoi mua day o -> tu choi truoc khi tru xu",
                doc("src/real/Map.java").contains("QuanLyThuGame.chanNhan(player, \"giao dich pet tu \""));
        Kiem.dung("tang thu cuoi: kiem dung con da chon", doc("src/real/MapNhapText.java").contains("QuanLyThuGame.dungConChoTang(player, idMenu)"));
        final String sql = DayDu.phanNangCap("them_quan_ly_thu.sql");
        Kiem.dung("htbb.sql co khoi them_quan_ly_thu.sql", sql.contains("o_thu_mo") && sql.contains("thu_khoa"));
        Kiem.dung("khoi SQL them cot idempotent", sql.contains("ADD COLUMN IF NOT EXISTS `o_thu_mo`")
                && sql.contains("ADD COLUMN IF NOT EXISTS `thu_khoa`"));
        Kiem.dung("khoi SQL: mon cua hang INSERT IGNORE", sql.contains("INSERT IGNORE INTO `nap_cua_hang`") && sql.contains("'ve_mo_o_thu'"));
        Kiem.dung("phan A: tob_char co o_thu_mo + thu_khoa", DayDu.cot("tob_char").contains("o_thu_mo") && DayDu.cot("tob_char").contains("thu_khoa"));
        for (final String k : new String[]{OThu.K_O_CO_BAN, OThu.K_O_TOI_DA, OThu.K_THA_XU_CO_BAN, OThu.K_THA_XU_MOI_CAP, OThu.K_THA_XU_TOI_DA}) {
            Kiem.dung("cau_hinh_danh_muc co " + k, DayDu.tim("cau_hinh_danh_muc", "khoa", k) != null);
        }
        Kiem.bang("mac dinh o co ban trong danh muc = ma", String.valueOf(OThu.O_CO_BAN),
                DayDu.giaTri("cau_hinh_danh_muc", "khoa", OThu.K_O_CO_BAN, "mac_dinh"));
        Kiem.bang("mac dinh o toi da trong danh muc = ma", String.valueOf(OThu.O_TOI_DA),
                DayDu.giaTri("cau_hinh_danh_muc", "khoa", OThu.K_O_TOI_DA, "mac_dinh"));
    }
}
