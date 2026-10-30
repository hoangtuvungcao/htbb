import com.htbb.pokemon.ChongVatPham;

import io.Message;
import io.Session;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import real.Char;
import real.InfoItemAttribute;
import real.Item;
import real.ItemTemplates;

/**
 * CHONG VAT PHAM type 28 (30/10/2026, quyet dinh chu game): vat pham giong het (cung template, cung khoa, khong du lieu
 * rieng) gop thanh mot o co so luong (Item.quanlity, tran ChongVatPham.TRAN = 999). Kiem: gop dong trung bao toan so
 * luong; nhan don vao chong (tui day van nhan neu chong con cho) + tran; dung tru 1; ban / huy / doi / kho theo don vi;
 * luu -&gt; nap -&gt; luu khong nhan dong; mon co han / cuong hoa / khoa khac khong gop; client thay dong "Số lượng".
 */
public final class KiemChongVatPham {

    private KiemChongVatPham() {
    }

    static final int HUY_CHUONG = 1039, VE_KHOA = 1251, BUI_SAO = 1034, HUYET = 720, TRANG_BI = 79;
    static final List<Integer> MAU_THEM = new ArrayList<Integer>();
    static short idKe = 21000;
    static final List<Message> GOI = new ArrayList<Message>();

    public static void chay() {
        Kiem.nhom("Chong vat pham type 28 (ChongVatPham: gop / nhan / dung / ban / kho / luu)");
        try {
            chuanBi();
            nhanDien();
            gopDongTrung();
            nhanVaoChong();
            dungTruMot();
            truTheoMau();
            banHuyKho();
            luuNapLai();
            hienThi();
            nguon();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            traLai();
        }
    }

    // ------------------------------------------------------------------ dung cu

    static real.GemTemplate[] gemCu;

    static void chuanBi() {
        gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0]; // new Char() can (nhu KiemDungVatPham)
        }
        while (real.Map.itemTemplates.size() < 6) {
            real.Map.itemTemplates.add(new Hashtable<Integer, ItemTemplates>());
        }
        mau(HUY_CHUONG, 28, "Huy chương Gym", 500);
        mau(VE_KHOA, 28, "Vé EXP x1,5 (30 phút) - khoá", 1000);
        mau(BUI_SAO, 28, "Bụi sao", 200);
        mau(HUYET, 28, "Huyết linh thảo", 100);
        mau(TRANG_BI, 3, "Than lửa sơ cấp", 1000);
    }

    static void traLai() {
        real.Map.gemTemplate = gemCu;
        for (final Integer id : MAU_THEM) {
            for (int i = 0; i < 6 && i < real.Map.itemTemplates.size(); i++) {
                real.Map.itemTemplates.get(i).remove(id);
            }
        }
        MAU_THEM.clear();
    }

    static void mau(final int id, final int type, final String ten, final int gia) {
        if (!real.Map.itemTemplates.get(5).containsKey(Integer.valueOf(id))) {
            MAU_THEM.add(Integer.valueOf(id));
        } else {
            return; // template that (nap tu sql/htbb.sql) - giu nguyen
        }
        final ItemTemplates t = new ItemTemplates();
        t.id = id;
        t.type = (byte) type;
        t.name = ten;
        t.level = 1;
        t.price = gia;
        t.atb = new short[12];
        for (int i = 0; i < 6; i++) {
            real.Map.itemTemplates.get(i).put(Integer.valueOf(id), t);
        }
    }

    static Char nhanVat(final int soO) {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        });
        p.charname = "kiemchong" + (idKe++);
        p.charDBID = 900000 + idKe;
        p.lastLog = "2026-10-30 00:00:00";
        p.lvDetail.lv = 30;
        p.MAX_INVENTORY = (short) soO;
        p.maxBag = 1;
        p.oMoRong = 0;
        return p;
    }

    /** Mon type 28 nhu Char.createItemSoLuong tao (khoa), so luong n. */
    static Item mon(final Char p, final int tpl, final int lock, final int n) {
        final Item it = Char.mauSoLuong(tpl, p);
        it.id = p == null ? idKe++ : p.getIDItem();
        it.lock = (byte) lock;
        it.quanlity = n;
        return it;
    }

    static int tong(final List<Item> ds, final int tpl, final int lock) {
        int n = 0;
        for (final Item it : ds) {
            if (it.tempateID == tpl && (lock < 0 || it.lock == lock)) {
                n += ChongVatPham.so(it);
            }
        }
        return n;
    }

    static int dong(final List<Item> ds, final int tpl) {
        int n = 0;
        for (final Item it : ds) {
            if (it.tempateID == tpl) {
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------------------ nhom kiem

    static void nhanDien() {
        final Item a = mon(null, HUY_CHUONG, 1, 1);
        Kiem.dung("Huy chuong Gym (type 28, khong du lieu rieng) chong duoc", ChongVatPham.chongDuoc(a));
        final Item hsd = mon(null, HUY_CHUONG, 1, 1);
        hsd.minuteExist = 30;
        hsd.timeLoan = System.currentTimeMillis();
        Kiem.sai("mon co han dung (minuteExist / timeLoan) KHONG chong", ChongVatPham.chongDuoc(hsd));
        final Item plus = mon(null, HUY_CHUONG, 1, 1);
        plus.plus = 1;
        Kiem.sai("mon cuong hoa (+1) KHONG chong", ChongVatPham.chongDuoc(plus));
        final Item chiSo = mon(null, HUY_CHUONG, 1, 1);
        chiSo.otherAtt[54] = 241;
        Kiem.sai("mon co chi so rieng (otherAtt) KHONG chong", ChongVatPham.chongDuoc(chiSo));
        final Item ngoc = mon(null, HUY_CHUONG, 1, 1);
        ngoc.gemKham.add(Short.valueOf((short) 3));
        Kiem.sai("mon da khảm ngoc KHONG chong", ChongVatPham.chongDuoc(ngoc));
        final Item ban = mon(null, HUY_CHUONG, 1, 1);
        ban.isSelling = true;
        Kiem.sai("mon dang rao ban KHONG chong / nhan them", ChongVatPham.chongDuoc(ban));
        final Item tb = new Item(real.Map.itemTemplates.get(5).get(Integer.valueOf(TRANG_BI)), false, 0, 0, TRANG_BI);
        Kiem.sai("trang bi (type 3) KHONG chong", ChongVatPham.chongDuoc(tb));
        final Item b = mon(null, HUY_CHUONG, 1, 1);
        final Item moKhoa = mon(null, HUY_CHUONG, 0, 1);
        Kiem.dung("cung template + cung khoa: cung chong", ChongVatPham.cungChong(a, b));
        Kiem.sai("khoa vs khong khoa: KHONG cung chong", ChongVatPham.cungChong(a, moKhoa));
        Kiem.sai("khac template: KHONG cung chong", ChongVatPham.cungChong(a, mon(null, BUI_SAO, 1, 1)));
        Kiem.bang("tran chong 999", Integer.valueOf(999), Integer.valueOf(ChongVatPham.TRAN));
        final Item cu = mon(null, HUY_CHUONG, 1, 1);
        cu.quanlity = 0;
        Kiem.bang("so luong 0 (dong cu) doc la 1", Integer.valueOf(1), Integer.valueOf(ChongVatPham.so(cu)));
    }

    static void gopDongTrung() {
        final Char p = nhanVat(28);
        // dung tinh huong bot thu: 8 huy chuong + 4 ve, moi mon mot dong
        for (int i = 0; i < 8; i++) {
            p.iItems.add(mon(p, HUY_CHUONG, 1, 1));
        }
        for (int i = 0; i < 4; i++) {
            p.iItems.add(mon(p, VE_KHOA, 1, 1));
        }
        p.iItems.add(mon(p, HUY_CHUONG, 0, 1));
        p.iItems.add(mon(p, HUY_CHUONG, 0, 2));
        final Item hsd = mon(p, VE_KHOA, 1, 1);
        hsd.minuteExist = 30;
        hsd.timeLoan = System.currentTimeMillis();
        p.iItems.add(hsd);
        final int truoc = p.iItems.size();
        final int bo = ChongVatPham.gopNhanVat(p);
        Kiem.bang("15 dong -> 4 dong (huy chuong khoa / khong khoa, ve, ve co han)", Integer.valueOf(4), Integer.valueOf(p.iItems.size()));
        Kiem.bang("so dong bo = truoc - sau", Integer.valueOf(truoc - p.iItems.size()), Integer.valueOf(bo));
        Kiem.bang("huy chuong khoa: tong 8 (bao toan)", Integer.valueOf(8), Integer.valueOf(tong(p.iItems, HUY_CHUONG, 1)));
        Kiem.bang("huy chuong khong khoa: tong 3, dong rieng", Integer.valueOf(3), Integer.valueOf(tong(p.iItems, HUY_CHUONG, 0)));
        Kiem.bang("ve: 4 trong chong + 1 ve co han rieng", Integer.valueOf(5), Integer.valueOf(tong(p.iItems, VE_KHOA, 1)));
        Kiem.bang("ve co han khong bi gop (2 dong ve)", Integer.valueOf(2), Integer.valueOf(dong(p.iItems, VE_KHOA)));
        Kiem.bang("o trong tang: 28 - 4", Integer.valueOf(24), Integer.valueOf(p.countSlotInventory()));
        Kiem.bang("gop lan 2 khong doi gi", Integer.valueOf(0), Integer.valueOf(ChongVatPham.gopNhanVat(p)));
        // kho (bItem) cung gop
        p.bItem.add(mon(p, BUI_SAO, 1, 5));
        p.bItem.add(mon(p, BUI_SAO, 1, 6));
        ChongVatPham.gopNhanVat(p);
        Kiem.bang("kho: 5 + 6 bui sao -> 1 dong 11", "1/11", dong(p.bItem, BUI_SAO) + "/" + tong(p.bItem, BUI_SAO, 1));
        // tran khi gop
        final Vector<Item> ds = new Vector<Item>();
        ds.add(mon(null, BUI_SAO, 1, 600));
        ds.add(mon(null, BUI_SAO, 1, 600));
        ds.add(mon(null, BUI_SAO, 1, 600));
        ChongVatPham.gop(ds);
        Kiem.bang("gop 3 x 600 -> 999 / 801 (tong 1800, tran 999)", "2/999/801/1800",
                ds.size() + "/" + ChongVatPham.so(ds.get(0)) + "/" + ChongVatPham.so(ds.get(1)) + "/" + tong(ds, BUI_SAO, -1));
    }

    static void nhanVaoChong() {
        // tui day 3/3, co chong huy chuong 8 -> Gym x3 van nhan (don vao chong)
        final Char p = nhanVat(3);
        p.iItems.add(mon(p, HUY_CHUONG, 1, 8));
        p.iItems.add(mon(p, VE_KHOA, 1, 4));
        p.iItems.add(mon(p, BUI_SAO, 1, 1));
        Kiem.dung("tui 3/3 day", p.isFullInventory());
        Kiem.dung("tui day nhung chong huy chuong con cho: conCho 3", ChongVatPham.conCho(p, HUY_CHUONG, 3));
        Kiem.bang("so o moi can cho 3 huy chuong = 0", Integer.valueOf(0), Integer.valueOf(ChongVatPham.soOCan(p, HUY_CHUONG, 3)));
        Kiem.sai("tui day, chua co Huyet linh thao: khong con cho", ChongVatPham.conCho(p, HUYET, 1));
        Char.createItemSoLuong(HUY_CHUONG, 3, p);
        Kiem.bang("createItemSoLuong x3 vao chong: 11 huy chuong, van 3 dong", "11/3",
                tong(p.iItems, HUY_CHUONG, 1) + "/" + p.iItems.size());
        // tran: chong 998 + nhan 3 -> 999 + chong moi 2 (con o)
        final Char q = nhanVat(5);
        q.iItems.add(mon(q, HUY_CHUONG, 1, 998));
        Kiem.bang("998 + 3: can 1 o moi", Integer.valueOf(1), Integer.valueOf(ChongVatPham.soOCan(q, HUY_CHUONG, 3)));
        Char.createItemSoLuong(HUY_CHUONG, 3, q);
        Kiem.bang("998 + 3 -> 999 + 2 (tran, tong 1001)", "2/999/2/1001", q.iItems.size() + "/" + ChongVatPham.so(q.iItems.get(0))
                + "/" + ChongVatPham.so(q.iItems.get(1)) + "/" + tong(q.iItems, HUY_CHUONG, 1));
        Kiem.dung("chong moi co id rieng", q.iItems.get(0).id != q.iItems.get(1).id);
        // tui day + chong da tran: khong cho
        final Char r = nhanVat(1);
        r.iItems.add(mon(r, HUY_CHUONG, 1, 999));
        Kiem.sai("tui day, chong da 999: khong con cho", ChongVatPham.conCho(r, HUY_CHUONG, 1));
        // nhan nhieu chong cung luc: 2500 vao tui trong
        final Char s = nhanVat(10);
        Char.createItemSoLuong(BUI_SAO, 2500, s);
        Kiem.bang("nhan 2500 bui sao -> 3 chong 999/999/502", "3/999/999/502", s.iItems.size() + "/" + ChongVatPham.so(s.iItems.get(0))
                + "/" + ChongVatPham.so(s.iItems.get(1)) + "/" + ChongVatPham.so(s.iItems.get(2)));
        // nhiem vu: soOThuong theo tui that (item:1039x3 khi da co chong = 0 o)
        try {
            final java.lang.reflect.Method m = Class.forName("real.NhiemVuEngine").getDeclaredMethod("soOThuong", Char.class,
                    com.htbb.pokemon.NhiemVuMau.class);
            m.setAccessible(true);
            final com.htbb.pokemon.NhiemVuMau nv = new com.htbb.pokemon.NhiemVuMau();
            nv.vatPham = "";
            nv.thuongKhac = "truyen:Chương 6;item:1039x3";
            Kiem.bang("soOThuong(p): item:1039x3 khi co chong huy chuong = 0 o", Integer.valueOf(0), m.invoke(null, p, nv));
            Kiem.bang("soOThuong(p): tui chua co huy chuong = 1 o (khong phai 3)", Integer.valueOf(1), m.invoke(null, nhanVat(5), nv));
        } catch (final Exception e) {
            Kiem.dung("soOThuong(Char, NhiemVuMau) goi duoc: " + e, false);
        }
    }

    static void dungTruMot() {
        final Char p = nhanVat(10);
        final Item ve = mon(p, VE_KHOA, 1, 4);
        p.iItems.add(ve);
        final short id = ve.id;
        Kiem.dung("tieuMot chong 4", ChongVatPham.tieuMot(p, ve));
        Kiem.bang("con 3, van mot dong, giu id", "3/1/true", ChongVatPham.so(ve) + "/" + p.iItems.size() + "/" + (ve.id == id));
        final Item le = mon(p, BUI_SAO, 1, 1);
        p.iItems.add(le);
        Kiem.dung("tieuMot mon le", ChongVatPham.tieuMot(p, le));
        Kiem.sai("mon le het: lay ra khoi tui", p.iItems.contains(le));
        Kiem.sai("mon khong con trong tui: tieuMot = false", ChongVatPham.tieuMot(p, le));
        // dung qua lenh 29 cu: tach 1 don vi (giu id client gui), dung xong gop lai neu chua tieu
        final Item hc = mon(p, HUY_CHUONG, 1, 5);
        p.iItems.add(hc);
        final Item conLai = ChongVatPham.tachMot(p.iItems, hc, p.getIDItem());
        Kiem.bang("tach: mon goc 1 don vi (id cu), con lai 4 ngay sau", "1/4/true",
                ChongVatPham.so(hc) + "/" + ChongVatPham.so(conLai) + "/" + (p.iItems.indexOf(conLai) == p.iItems.indexOf(hc) + 1));
        Kiem.dung("duong cu khong tieu: gop lai 5", ChongVatPham.gopLai(p.iItems, hc, conLai) && ChongVatPham.so(hc) == 5 && !p.iItems.contains(conLai));
        final Item conLai2 = ChongVatPham.tachMot(p.iItems, hc, p.getIDItem());
        p.iItems.remove(hc); // duong cu lay CA mon ra (vd mo ruong) = tieu 1 don vi
        Kiem.sai("duong cu da tieu 1: khong gop lai", ChongVatPham.gopLai(p.iItems, hc, conLai2));
        Kiem.bang("con 4 huy chuong (khong mat ca chong)", Integer.valueOf(4), Integer.valueOf(tong(p.iItems, HUY_CHUONG, -1)));
    }

    static void truTheoMau() {
        final Char p = nhanVat(10);
        p.iItems.add(mon(p, HUY_CHUONG, 1, 5));
        p.iItems.add(mon(p, HUY_CHUONG, 0, 3));
        Kiem.bang("demItem cong so luong cac chong (5 + 3)", Integer.valueOf(8), Integer.valueOf(real.VatPhamDungLauDai.demItem(p, HUY_CHUONG)));
        Kiem.dung("doi thuong tieu 6 huy chuong", real.VatPhamDungLauDai.truItem(p, HUY_CHUONG, 6));
        Kiem.bang("con 2 (bao toan: 8 - 6)", Integer.valueOf(2), Integer.valueOf(real.VatPhamDungLauDai.demItem(p, HUY_CHUONG)));
        Kiem.bang("chong het bi lay ra: con 1 dong", Integer.valueOf(1), Integer.valueOf(p.iItems.size()));
        Kiem.sai("thieu (can 3, co 2): khong tru", real.VatPhamDungLauDai.truItem(p, HUY_CHUONG, 3));
        Kiem.bang("thieu thi giu nguyen 2", Integer.valueOf(2), Integer.valueOf(real.VatPhamDungLauDai.demItem(p, HUY_CHUONG)));
        final Item rao = mon(p, BUI_SAO, 1, 7);
        rao.isSelling = true;
        p.iItems.add(rao);
        Kiem.bang("chong dang rao ban khong tinh / khong tieu", Integer.valueOf(0), Integer.valueOf(real.VatPhamDungLauDai.demItem(p, BUI_SAO)));
    }

    static void banHuyKho() {
        // ban NPC: 1 don vi / lan bam, xu = gia 1 mon, mua lai = 1 don vi
        final Char p = nhanVat(10);
        final Item hc = mon(p, HUY_CHUONG, 0, 5);
        p.iItems.add(hc);
        final long xu0 = p.getxu();
        real.OfflineMap.banMotTrongChong(p, hc, false);
        Kiem.bang("ban 1 trong chong 5: con 4", Integer.valueOf(4), Integer.valueOf(ChongVatPham.so(hc)));
        Kiem.dung("ban 1: duoc xu (> 0)", p.getxu() > xu0);
        final long mot = p.getxu() - xu0;
        real.OfflineMap.banMotTrongChong(p, hc, false);
        Kiem.bang("ban lan 2 cung gia 1 mon", Long.valueOf(mot), Long.valueOf(p.getxu() - xu0 - mot));
        Kiem.bang("mua lai: 2 mon le trong danh sach mua lai", "2/1/1", p.sellShopItem.size() + "/" + ChongVatPham.so(p.sellShopItem.get(0))
                + "/" + ChongVatPham.so(p.sellShopItem.get(1)));
        Kiem.bang("ban 2: tong con + da ban = 5", Integer.valueOf(5), Integer.valueOf(ChongVatPham.so(hc) + p.sellShopItem.size()));
        // huy (vut) 1 don vi: Map.doGiveItemToGround dung truMot
        final Item ve = mon(p, VE_KHOA, 1, 3);
        p.iItems.add(ve);
        Kiem.bang("huy 1 trong chong 3", Integer.valueOf(ChongVatPham.DA_GIAM), Integer.valueOf(ChongVatPham.truMot(p.iItems, ve)));
        Kiem.bang("huy: con 2, van trong tui", "2/true", ChongVatPham.so(ve) + "/" + p.iItems.contains(ve));
        // giao dich / gian hang / cho: mon di nguyen chong qua cloneItem - giu so luong (khong nhan / khong mat)
        final Item c = hc.cloneItem();
        Kiem.bang("cloneItem (gian hang, cho, mua lai) giu so luong", Integer.valueOf(ChongVatPham.so(hc)), Integer.valueOf(ChongVatPham.so(c)));
        final Item sao = ChongVatPham.banSao(hc, 1);
        sao.otherAtt[1] = 9;
        Kiem.dung("banSao co mang chi so rieng (sua ban sao khong doi chong goc)", hc.otherAtt[1] == 0);
        // kho Kangaskhan: gui ca chong (bao toan), rut ra don vao chong trong tui (tui day van rut duoc)
        final Char k = nhanVat(1);
        final Item trongTui = mon(k, BUI_SAO, 1, 10);
        k.iItems.add(trongTui);
        final Item trongKho = mon(k, BUI_SAO, 1, 20);
        k.bItem.add(trongKho);
        Kiem.dung("tui day, chong trong tui chua them duoc 20: rut duoc", real.Map.vuaChongTrongTui(k, trongKho.id));
        Kiem.bang("don 20 tu kho vao chong 10: con thua 0, chong 30", "0/30",
                ChongVatPham.donVao(k.iItems, trongKho, ChongVatPham.so(trongKho)) + "/" + ChongVatPham.so(trongTui));
        final Item day = mon(k, HUY_CHUONG, 1, 999);
        k.iItems.add(day);
        final Item kho2 = mon(k, HUY_CHUONG, 1, 5);
        k.bItem.add(kho2);
        Kiem.sai("chong trong tui da 999: tui day thi khong rut", real.Map.vuaChongTrongTui(k, kho2.id));
    }

    static void luuNapLai() {
        final Char p = nhanVat(28);
        for (int i = 0; i < 8; i++) {
            p.iItems.add(mon(p, HUY_CHUONG, 1, 1));
        }
        p.iItems.add(mon(p, VE_KHOA, 1, 4));
        final Item hsd = mon(p, VE_KHOA, 1, 1);
        hsd.minuteExist = 999999;
        hsd.timeLoan = System.currentTimeMillis();
        p.iItems.add(hsd);
        final String chuoi1 = luu(p.iItems);
        final Char q = nap(chuoi1);
        Kiem.bang("nap: 10 dong doc lai du", Integer.valueOf(10), Integer.valueOf(q.iItems.size()));
        ChongVatPham.gopNhanVat(q);
        Kiem.bang("nap + gop: 3 dong (huy chuong 8, ve 4, ve co han)", Integer.valueOf(3), Integer.valueOf(q.iItems.size()));
        Kiem.bang("so luong qua luu / nap: huy chuong 8, ve 5", "8/5", tong(q.iItems, HUY_CHUONG, 1) + "/" + tong(q.iItems, VE_KHOA, 1));
        final String chuoi2 = luu(q.iItems);
        final Char r = nap(chuoi2);
        ChongVatPham.gopNhanVat(r);
        Kiem.bang("luu -> nap lan 2: van 3 dong (khong nhan dong qua relog)", Integer.valueOf(3), Integer.valueOf(r.iItems.size()));
        Kiem.bang("lan 2 tong giu nguyen", "8/5", tong(r.iItems, HUY_CHUONG, 1) + "/" + tong(r.iItems, VE_KHOA, 1));
        Kiem.bang("chuoi luu on dinh qua relog (bo id / ngay tao)", Integer.valueOf(chuoi2.split(">").length),
                Integer.valueOf(luu(r.iItems).split(">").length));
        Item hc = null;
        for (final Item it : r.iItems) {
            if (it.tempateID == HUY_CHUONG) {
                hc = it;
            }
        }
        Kiem.dung("chong nap lai van khoa + chong duoc", hc != null && hc.lock == 1 && ChongVatPham.chongDuoc(hc));
    }

    static String luu(final List<Item> ds) {
        final StringBuilder sb = new StringBuilder();
        for (final Item it : ds) {
            sb.append(it.getInfoSave()).append(">");
        }
        return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1);
    }

    static Char nap(final String chuoi) {
        final Char q = nhanVat(28);
        q.invent = chuoi;
        q.setitemInvenBag(0);
        return q;
    }

    static void hienThi() {
        final Item hc = mon(null, HUY_CHUONG, 1, 8);
        final Vector<InfoItemAttribute> a = hc.getInfoAtbItem();
        boolean co = false;
        for (final InfoItemAttribute x : a) {
            if (x.id == ChongVatPham.idSoLuong() && x.value == 8) {
                co = true;
            }
        }
        Kiem.dung("chong 8: goi tui co dong thuoc tinh 'Số lượng' = 8", co);
        final Item le = mon(null, HUY_CHUONG, 1, 1);
        boolean coLe = false;
        for (final InfoItemAttribute x : le.getInfoAtbItem()) {
            if (x.id == ChongVatPham.idSoLuong()) {
                coLe = true;
            }
        }
        Kiem.sai("mon le: khong gui dong so luong", coLe);
        final int cu = ChongVatPham.idThuocTinh;
        try {
            final Vector<String> bang = new Vector<String>();
            for (int i = 0; i < 198; i++) {
                bang.add("x" + i);
            }
            ChongVatPham.themTenThuocTinh(bang, i -> ChongVatPham.TEN_SO_LUONG + "#" + i);
            Kiem.bang("bang ten thuoc tinh: them 'Số lượng' o cuoi (chi so 198)", "199/198", bang.size() + "/" + ChongVatPham.idSoLuong());
            final Vector<String> day = new Vector<String>();
            for (int i = 0; i < 255; i++) {
                day.add("x" + i);
            }
            ChongVatPham.idThuocTinh = -1;
            ChongVatPham.themTenThuocTinh(day, i -> "y");
            Kiem.bang("bang da 255 dong (client doc 1 byte): khong them, dung id 126", "255/126", day.size() + "/" + ChongVatPham.idSoLuong());
        } finally {
            ChongVatPham.idThuocTinh = cu;
        }
    }

    static void nguon() {
        final String map = doc("src/real/Map.java");
        final int huy = map.indexOf("void doGiveItemToGround(");
        Kiem.dung("Huy / bo vat pham: chong > 1 chi tru 1 (ChongVatPham.truMot) truoc khi xoa mon",
                huy > 0 && map.indexOf("ChongVatPham.truMot(player.iItems, item)", huy) > huy
                        && map.indexOf("ChongVatPham.truMot(player.iItems, item)", huy) < map.indexOf("player.iItems.remove(item)", huy));
        Kiem.dung("rut kho: don vao chong trong tui (ChongVatPham.donVao)", map.contains("ChongVatPham.donVao(player.iItems, item"));
        Kiem.dung("ban NPC: chong > 1 ban 1 don vi", doc("src/real/OfflineMap.java").contains("banMotTrongChong(player, item, giaThap)"));
        final String dung = doc("src/real/DungVatPham.java");
        Kiem.dung("lenh 29: tach 1 don vi truoc doUseItemGoc, gop lai sau", dung.indexOf("tachDeDung(p, mon)") > 0
                && dung.indexOf("tachDeDung(p, mon)") < dung.indexOf("map.doUseItemGoc(p, message)")
                && dung.indexOf("gopSauDung(p, mon, conLai)") > dung.indexOf("map.doUseItemGoc(p, message)"));
        Kiem.dung("nap nhan vat: gop dong trung (CharNapDuLieu)", doc("src/real/CharNapDuLieu.java").contains("ChongVatPham.gopNhanVat(self)"));
        Kiem.dung("bang ten thuoc tinh them dong 'Số lượng' (Database.loadNameAttributeItem)",
                doc("src/data/Database.java").contains("ChongVatPham.themTenThuocTinh(ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM"));
        Kiem.dung("thu cung an Huyet linh thao: tru 1 don vi", doc("src/real/MapDoMenu.java")
                .contains("ChongVatPham.tieuMot(player, (Item) player.itemPetEat.get(idOptionMenu))"));
        Kiem.dung("Gym / roi type 28: tui day van don vao chong (NoiDungLauDai.phat)",
                doc("src/real/NoiDungLauDai.java").contains("ChongVatPham.conCho(p, id, 1)"));
        final String c = doc("../client_jar/src/game/networklogic/MessageHandler.java");
        Kiem.dung("client (ban sau): doc so luong chong vao ItemInInventory.number", c.contains("cii.number = ItemInInventory.soLuongChong(cii)"));
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
