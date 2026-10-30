package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import real.Char;
import real.InfoItemAttribute;
import real.Item;
import real.ItemTemplates;

/**
 * CHONG VAT PHAM (30/10/2026, quyet dinh chu game): vat pham type 28 (Huy chuong Gym 1039, Ve EXP 1250/1251, Bua the
 * luc, Bui sao 1034, da tien hoa, qua mong...) giong het nhau gop thanh MOT o hanh trang co so luong, khong con moi
 * mon mot o (bot thu day 28/28 o vi 8 huy chuong + 4 ve, khong mua binh / tra nhiem vu duoc nua).
 *
 * <p>Luat:
 * <ul>
 *   <li>Chi chong: template type {@link #LOAI_CHONG} VA khong co du lieu rieng tung mon (han dung minuteExist /
 *       timeLoan / timeExpireHkl, plus / o khảm / ngoc khảm / ngoc he, dong chi so rieng newAtb / addMoreLevelSkill /
 *       lockAtb / otherAtt, tinh luyen / cai tao, dang rao ban). Hai mon cung chong khi cung template, cung khoa
 *       (lock + daMac), cung mau ten / co cua hang / co roi va cung mang chi so goc (atb) - xem {@link #cungChong}.</li>
 *   <li>So luong luu o {@link Item#quanlity} (truong 29 cua getDBInfo, da co tu ban goc, mac dinh 1).</li>
 *   <li>Tran {@link #TRAN} = 999 moi chong (kieu NRO / NSO: mot o 999 du cho ca thang cay Gym x3 / ngay; so hien ve goc
 *       o vua 3 chu so; gia tri thuoc tinh gui client la short nen con xa tran). Vuot tran -&gt; chong moi.</li>
 *   <li>Gop dong trung khi NAP nhan vat (CharNapDuLieu: hanh trang + kho) - tong so luong bao toan, luu lai thanh it
 *       dong hon (inventory luu thanh chuoi tob_char.invent, khong co dong CSDL rieng phai xoa).</li>
 * </ul>
 * Lop nay chi thao tac danh sach vat pham (de kiem khong can CSDL: server/test/KiemChongVatPham); phan noi vao nhan vat
 * (id, gui goi tin) o cac ham co tham so Char.
 */
public final class ChongVatPham {

    /** data_item.type cua vat pham tieu hao / nguyen lieu chong duoc. */
    public static final int LOAI_CHONG = 28;
    /** So luong toi da moi chong. */
    public static final int TRAN = 999;
    /** id thuoc tinh "Số lượng" cu (data_attribute 126 - client 0.9.x AN o the vat pham, dung cho goi cua hang). */
    public static final short ID_SO_LUONG_CU = 126;
    /** Ten dong thuoc tinh so luong ma client hien ("Số lượng: 8"). */
    public static final String TEN_SO_LUONG = "Số lượng";
    /**
     * Chi so dong ten thuoc tinh "Số lượng" THEM o cuoi bang ten thuoc tinh luc nap (Database.loadNameAttributeItem ->
     * {@link #themTenThuocTinh}); -1 = chua nap (kiem thu) -&gt; dung {@link #ID_SO_LUONG_CU}.
     */
    public static volatile int idThuocTinh = -1;

    /** Ket qua {@link #truMot}. */
    public static final int KHONG_CO = 0, DA_GIAM = 1, DA_LAY_RA = 2;

    private ChongVatPham() {
    }

    // ------------------------------------------------------------------ nhan dien

    /** So luong cua mon (&gt;= 1; mon cu luu 0 / loi doc coi la 1). */
    public static int so(final Item it) {
        return it == null ? 0 : Math.max(1, it.quanlity);
    }

    static ItemTemplates mau(final Item it) {
        try {
            return it.getTemplate();
        } catch (final RuntimeException e) {
            return null;
        }
    }

    /** Template type 28. */
    public static boolean laLoaiChong(final Item it) {
        final ItemTemplates t = it == null ? null : mau(it);
        return t != null && t.type == LOAI_CHONG;
    }

    static boolean khac0(final short[] a, final int tu) {
        for (int i = tu; a != null && i < a.length; i++) {
            if (a[i] != 0) {
                return true;
            }
        }
        return false;
    }

    static boolean khac0(final byte[] a) {
        for (int i = 0; a != null && i < a.length; i++) {
            if (a[i] != 0) {
                return true;
            }
        }
        return false;
    }

    /** Mon co du lieu rieng (han dung, cuong hoa, khảm, chi so rieng, dang rao ban...) -&gt; khong chong. */
    public static boolean coDuLieuRieng(final Item it) {
        return it.minuteExist != 0 || it.timeLoan != 0L || it.timeExpireHkl != 0L
                || it.plus != 0 || it.nhadSock != 0 || it.nSocAdd != 0 || it.gemKham != null && !it.gemKham.isEmpty()
                || it.ngocHe != null && it.ngocHe.length() > 0
                || it.TinhLuyen != 0 || it.CaiTao != 0 || it.soLanTinhLuyenThatBai != 0 || it.soLanPhaKhoa != 0
                || khac0(it.atb, 10) || khac0(it.newAtb) || khac0(it.addMoreLevelSkill) || khac0(it.lockAtb) || khac0(it.otherAtt, 0)
                || it.isSelling || it.idSellMarket != -1 || it.prizeSell != 0 || it.idAnimal != -1;
    }

    /** Mon nay chong duoc (type 28, khong du lieu rieng). */
    public static boolean chongDuoc(final Item it) {
        return it != null && laLoaiChong(it) && !coDuLieuRieng(it);
    }

    /** Hai mon gop chung mot chong duoc. */
    public static boolean cungChong(final Item a, final Item b) {
        if (a == null || b == null || a == b || a.tempateID != b.tempateID || !chongDuoc(a) || !chongDuoc(b)) {
            return false;
        }
        if (a.lock != b.lock || a.daMac != b.daMac || a.colorName != b.colorName || a.isItemShop != b.isItemShop
                || a.isItemDrop != b.isItemDrop || a.hangItem != b.hangItem || a.heItem != b.heItem
                || !(a.charSeal == null ? "" : a.charSeal).equals(b.charSeal == null ? "" : b.charSeal)) {
            return false;
        }
        for (int i = 0; i < 10 && i < a.atb.length && i < b.atb.length; i++) {
            if (a.atb[i] != b.atb[i]) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ dem / cho trong

    /** Tong so luong theo template (bo mon dang rao ban). */
    public static int dem(final List<Item> ds, final int idTemplate) {
        int n = 0;
        synchronized (ds) {
            for (final Item it : ds) {
                if (it != null && it.tempateID == idTemplate && !it.isSelling && mau(it) != null) {
                    n += so(it);
                }
            }
        }
        return n;
    }

    /** Cho con trong (don vi) trong cac chong san co cung loai voi {@code mau}. */
    public static int choTrongChong(final List<Item> ds, final Item mau) {
        if (!chongDuoc(mau)) {
            return 0;
        }
        long cho = 0;
        synchronized (ds) {
            for (final Item it : ds) {
                if (cungChong(it, mau) && so(it) < TRAN) {
                    cho += TRAN - so(it);
                }
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, cho);
    }

    /** So o MOI can de nhan {@code n} don vi giong {@code mau} (sau khi don vao chong san co). Mon khong chong: n. */
    public static int soOMoi(final List<Item> ds, final Item mau, final int n) {
        if (n <= 0) {
            return 0;
        }
        if (!chongDuoc(mau)) {
            return n;
        }
        final int con = Math.max(0, n - choTrongChong(ds, mau));
        return (con + TRAN - 1) / TRAN;
    }

    /** Don toi da {@code n} don vi giong {@code mau} vao cac chong san co (toi TRAN). @return so con thua. */
    public static int donVao(final List<Item> ds, final Item mau, final int n) {
        if (n <= 0 || !chongDuoc(mau)) {
            return Math.max(0, n);
        }
        int con = n;
        synchronized (ds) {
            for (final Item it : ds) {
                if (con <= 0) {
                    break;
                }
                if (cungChong(it, mau) && so(it) < TRAN) {
                    final int them = Math.min(con, TRAN - so(it));
                    it.quanlity = so(it) + them;
                    con -= them;
                }
            }
        }
        return con;
    }

    // ------------------------------------------------------------------ gop / tru / tach

    /**
     * Gop cac dong trung (cung chong) trong {@code ds}: dong sau don vao dong truoc toi TRAN, dong rong bi bo. Tong so
     * luong moi loai BAO TOAN; thu tu dong con lai giu nguyen. @return cac mon da bo khoi ds (de tra id).
     */
    public static List<Item> gop(final List<Item> ds) {
        final List<Item> bo = new ArrayList<Item>();
        synchronized (ds) {
            for (int i = 0; i < ds.size(); i++) {
                final Item sau = ds.get(i);
                if (!chongDuoc(sau)) {
                    continue;
                }
                sau.quanlity = so(sau);
                for (int j = 0; j < i && sau.quanlity > 0; j++) {
                    final Item truoc = ds.get(j);
                    if (cungChong(truoc, sau) && so(truoc) < TRAN) {
                        final int chuyen = Math.min(sau.quanlity, TRAN - so(truoc));
                        truoc.quanlity = so(truoc) + chuyen;
                        sau.quanlity -= chuyen;
                    }
                }
                if (sau.quanlity <= 0) {
                    ds.remove(i);
                    bo.add(sau);
                    i--;
                }
            }
        }
        return bo;
    }

    /**
     * Tru 1 don vi cua dung mon {@code it} trong {@code ds}: chong &gt; 1 -&gt; giam 1 (mon van o tui, giu id); con 1 -&gt;
     * lay ra khoi ds. @return {@link #KHONG_CO} (khong con trong ds), {@link #DA_GIAM} hoac {@link #DA_LAY_RA}.
     */
    public static int truMot(final List<Item> ds, final Item it) {
        if (ds == null || it == null) {
            return KHONG_CO;
        }
        synchronized (ds) {
            int vt = -1;
            for (int i = 0; i < ds.size(); i++) {
                if (ds.get(i) == it) {
                    vt = i;
                    break;
                }
            }
            if (vt < 0) {
                return KHONG_CO;
            }
            if (so(it) > 1) {
                it.quanlity = so(it) - 1;
                return DA_GIAM;
            }
            ds.remove(vt);
            return DA_LAY_RA;
        }
    }

    /**
     * Lay ra {@code n} don vi theo template (tat ca hoac khong; tu cuoi danh sach, bo mon dang rao ban).
     * @return cac mon da lay HAN ra khoi ds (rong neu chi giam chong), null neu khong du.
     */
    public static List<Item> tru(final List<Item> ds, final int idTemplate, final int n) {
        synchronized (ds) {
            if (n <= 0) {
                return new ArrayList<Item>();
            }
            if (dem(ds, idTemplate) < n) {
                return null;
            }
            final List<Item> bo = new ArrayList<Item>();
            int con = n;
            for (int i = ds.size() - 1; i >= 0 && con > 0; i--) {
                final Item it = ds.get(i);
                if (it == null || it.tempateID != idTemplate || it.isSelling || mau(it) == null) {
                    continue;
                }
                final int lay = Math.min(con, so(it));
                con -= lay;
                if (lay >= so(it)) {
                    ds.remove(i);
                    bo.add(it);
                } else {
                    it.quanlity = so(it) - lay;
                }
            }
            return bo;
        }
    }

    /** Ban sao doc lap (mang chi so rieng) cua mon chong duoc, so luong {@code n}, chua co id / dong CSDL. */
    public static Item banSao(final Item it, final int n) {
        final Item c = it.cloneItem();
        c.atb = it.atb.clone();
        c.newAtb = it.newAtb.clone();
        c.lockAtb = it.lockAtb.clone();
        c.addMoreLevelSkill = it.addMoreLevelSkill.clone();
        c.otherAtt = it.otherAtt.clone();
        c.lock = it.lock;
        c.daMac = it.daMac;
        c.owner = it.owner;
        c.identify = it.identify;
        c.place = it.place;
        c.level = it.level;
        c.idAnimal = it.idAnimal;
        c.idSellMarket = -1;
        c.dbid = 0;
        c.dbowner = 0;
        c.quanlity = Math.max(1, n);
        return c;
    }

    /**
     * Tach 1 don vi cua chong {@code it} (so &gt; 1) thanh dong rieng NGAY SAU no. Dong 1 don vi GIU id cu ({@code it});
     * phan con lai (so - 1) sang dong moi mang {@code idMoi}. @return dong con lai, null neu khong tach (it le / khong
     * con trong ds).
     */
    public static Item tachMot(final List<Item> ds, final Item it, final short idMoi) {
        synchronized (ds) {
            final int vt = ds.indexOf(it);
            if (vt < 0 || !chongDuoc(it) || so(it) <= 1) {
                return null;
            }
            final Item con = banSao(it, so(it) - 1);
            con.id = idMoi;
            it.quanlity = 1;
            ds.add(vt + 1, con);
            return con;
        }
    }

    /**
     * Sau {@link #tachMot}: mon 1 don vi chua bi dung (con trong ds, van cung chong) -&gt; don phan con lai ve lai no.
     * @return true neu da gop lai (dong {@code con} da bo khoi ds - tra id).
     */
    public static boolean gopLai(final List<Item> ds, final Item mot, final Item con) {
        synchronized (ds) {
            if (mot == null || con == null || !ds.contains(mot) || !ds.contains(con) || !cungChong(mot, con)
                    || so(mot) + so(con) > TRAN) {
                return false;
            }
            mot.quanlity = so(mot) + so(con);
            ds.remove(con);
            return true;
        }
    }

    // ------------------------------------------------------------------ hien thi

    /** id thuoc tinh dung de gui so luong cho client. */
    public static short idSoLuong() {
        final int i = idThuocTinh;
        return i > 0 ? (short) i : ID_SO_LUONG_CU;
    }

    /**
     * Them dong "Số lượng" vao bang ten thuoc tinh (goi 25) neu con cho (client doc so dong bang 1 byte khong dau).
     * Client 0.9.x AN id 126 o the vat pham (so luong goi cua hang), nen can mot dong rieng ma moi ban client hien.
     */
    public static <T> void themTenThuocTinh(final Vector<T> bang, final java.util.function.IntFunction<T> tao) {
        if (bang == null || bang.size() >= 255) {
            return;
        }
        final int id = bang.size();
        bang.add(tao.apply(id));
        idThuocTinh = id;
    }

    /** Them dong so luong (&gt; 1) vao danh sach thuoc tinh gui client cua mon. */
    public static void themDongSoLuong(final Item it, final List<InfoItemAttribute> ds) {
        if (ds == null || it == null || so(it) <= 1 || !laLoaiChong(it)) {
            return;
        }
        ds.add(new InfoItemAttribute(idSoLuong(), (short) Math.min(Short.MAX_VALUE, so(it))));
    }

    // ------------------------------------------------------------------ noi vao nhan vat

    /**
     * Tieu hao 1 don vi cua mon trong hanh trang (dung, cho an, mo...). Lay han ra thi tra id. @return false neu mon
     * khong con trong tui.
     */
    public static boolean tieuMot(final Char p, final Item it) {
        if (p == null || p.iItems == null) {
            return false;
        }
        final int kq = truMot(p.iItems, it);
        if (kq == DA_LAY_RA) {
            p.removeIDItem(it.id);
        }
        return kq != KHONG_CO;
    }

    /** Lay ra n don vi theo template khoi hanh trang (tat ca hoac khong). Mon lay han ra thi tra id. */
    public static boolean tieu(final Char p, final int idTemplate, final int n) {
        final List<Item> bo = tru(p.iItems, idTemplate, n);
        if (bo == null) {
            return false;
        }
        for (final Item it : bo) {
            p.removeIDItem(it.id);
        }
        return true;
    }

    /** Gop dong trung hanh trang + kho cua nhan vat (luc nap). @return so dong da bo. */
    public static int gopNhanVat(final Char p) {
        int n = 0;
        if (p.iItems != null) {
            for (final Item it : gop(p.iItems)) {
                p.removeIDItem(it.id);
                n++;
            }
        }
        if (p.bItem != null) {
            for (final Item it : gop(p.bItem)) {
                p.removeIDItem(it.id);
                n++;
            }
        }
        return n;
    }

    /** So o hanh trang MOI can de nhan {@code n} mon template {@code idTemplate} qua Char.createItemSoLuong. */
    public static int soOCan(final Char p, final int idTemplate, final int n) {
        if (n <= 0) {
            return 0;
        }
        final Item mau = Char.mauSoLuong(idTemplate, p);
        return mau == null ? n : soOMoi(p.iItems, mau, n);
    }

    /** Con du cho cho {@code n} mon template {@code idTemplate} (don vao chong san co + o trong). */
    public static boolean conCho(final Char p, final int idTemplate, final int n) {
        return soOCan(p, idTemplate, n) <= Math.max(0, p.countSlotInventory());
    }

    /**
     * Nhan {@code n} don vi giong {@code moi} (da co id, chua vao tui): don vao chong san co, phan thua thanh chong moi
     * (dong dau dung chinh {@code moi}). Chong moi chi khi con o trong - TRU dong dau khi chua nhan duoc gi (giu cach
     * cu cua createItemSoLuong: nguoi goi da kiem cho). @return {so da nhan, 1 neu {@code moi} da vao tui}.
     */
    public static int[] nhan(final Char p, final Item moi, final int n) {
        int daNhan = 0;
        boolean dungMoi = false;
        synchronized (p.iItems) {
            int con = donVao(p.iItems, moi, n);
            daNhan = n - con;
            while (con > 0) {
                if ((daNhan > 0 || dungMoi) && p.isFullInventory()) {
                    break;
                }
                final int lan = Math.min(con, TRAN);
                final Item dong;
                if (!dungMoi) {
                    dong = moi;
                    dong.quanlity = lan;
                    dungMoi = true;
                } else {
                    dong = banSao(moi, lan);
                    dong.id = p.getIDItem();
                }
                p.iItems.add(dong);
                daNhan += lan;
                con -= lan;
            }
        }
        return new int[]{daNhan, dungMoi ? 1 : 0};
    }
}
