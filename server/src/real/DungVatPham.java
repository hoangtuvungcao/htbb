package real;

import io.Message;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bam "Su dung" mot vat pham (lenh 29, hanh trang) hoac mot binh thuoc (lenh 22): MOI lan bam deu co phan hoi.
 *
 * <p>Loi ma chu bao "dung vat pham khong co tac dung" (0.9.2) co bon nguon, sua tai day:
 * <ol>
 *   <li>Nhieu duong trong Map.doUseItem / MapDungThuoc / doUsePotion THOAT IM LANG: chua du cap (chi in ra
 *   System.out), vu khi khac he, trang bi thu khi chua cuoi thu, vat pham khong con trong tui (tui cu o client),
 *   vat pham tro (type 24), binh ngoai Chien truong, cho hoi binh... Client khong nhan gi ca.</li>
 *   <li>Vat pham KHONG mac duoc (nguyen lieu, ve, qua le, keo...) roi xuong nhanh "mac do" MapMuaBan.exchangeItem
 *   va nem NullPointerException (Char.getIndexItemWearing: type khong co o trang bi) - nguoi choi khong thay gi.</li>
 *   <li>Map.checkTrade / doUsePotion.checkTrade tra true IM LANG khi HP = 0, nen Thuoc hoi sinh (toi da) trong
 *   tui khong bao gio dung duoc luc da guc.</li>
 *   <li>Binh HP/PP dung khi day mau van bi tru, hop qua bi tru khi hanh trang day ma khong nhan gi.</li>
 * </ol>
 *
 * <p>Cach lam: {@link #dung} / {@link #dungThuoc} boc ham cu. Truoc khi goi: kiem cac dieu kien chan (da guc, HP
 * day, tui day, khong con vat pham) va bao ly do. Trong luc goi: mo mot "phien" tren Char ({@code phienDung});
 * Char.sendMessage bao cho phien moi thong bao da gui (va doi hop thoai OK 37 thanh thong bao nhanh -104 cho client
 * moi). Sau khi goi: neu ham cu khong noi gi, phien so trang thai truoc/sau (tui, xu, luong, cap, HP...) va tu bao
 * "Da dung X: +..." hoac ly do khong dung duoc ({@link #lyDo}).
 *
 * <p>Lenh -104 (S-&gt;C): byte loai ({@link #OK} / {@link #LOI} / {@link #TIN}), UTF noi dung. Chi gui cho client
 * co so ban &gt;= {@link #BAN_TOAST} (Session.banClient); client cu / Unity nhan hop thoai 37 nhu truoc.
 */
public final class DungVatPham {

    public static final int LENH = -104;
    /** Ban client dau tien biet lenh -104 (client_jar/build.sh 902). */
    public static final int BAN_TOAST = 902;
    public static final byte OK = 0, LOI = 1, TIN = 2;

    /** Dem chan doan (AdminPanel / kiem thu). */
    public static final AtomicLong SO_BAM = new AtomicLong();
    public static final AtomicLong SO_TU_CHOI = new AtomicLong();
    public static final AtomicLong SO_TU_BAO = new AtomicLong();
    public static final AtomicLong SO_LOI_NGOAI_LE = new AtomicLong();

    /** Binh hoi HP / PP (MapDungThuoc: doUseHP / doUseMP). */
    static final int[] THUOC_HP = {1, 2, 3, 21, 22, 93, 94, 126};
    static final int[] THUOC_MP = {4, 5, 6, 23, 24, 95, 96, 127};
    /** Binh chi dung trong Chien truong Moba (MapDungThuoc: 126..132). */
    static final int MOBA_TU = 126, MOBA_DEN = 132;
    /** Hop / tui qua dang binh: can it nhat 1 o trong (vai nhanh cu tru binh roi bao "Hanh trang day"). */
    // 19/10/2026: + 71 (Trung Ho Ky Lan: dung = tao 1 vat pham vao tui, truoc day khong kiem o trong).
    static final int[] THUOC_HOP = {63, 71, 79, 101, 102, 108, 133, 134, 139, 140, 141, 143, 147, 159};

    private DungVatPham() {
    }

    // ================================================================== thong bao

    static boolean coToast(final Char p) {
        return p != null && p.session != null && p.session.banClient >= BAN_TOAST;
    }

    static Message taoToast(final byte loai, final String s) throws IOException {
        final Message m = new Message(LENH);
        m.dos.writeByte(loai);
        m.dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi(s));
        m.dos.writeByte(HanhDongVatPham.kieuDang()); // 0.9.3: loai thao tac (mua / ban / dung / trang bi...), client cu bo qua
        return m;
    }

    /** Thanh cong. Client moi: thong bao nhanh xanh; client cu: hop thoai OK. */
    public static void bao(final Char p, final String s) {
        gui(p, OK, s);
    }

    /** Khong dung duoc / loi. */
    public static void loi(final Char p, final String s) {
        SO_TU_CHOI.incrementAndGet();
        gui(p, LOI, s);
    }

    /** Thong bao nhe (tu dung qua mong...): client moi thong bao nhanh, client cu chi vao hop tin (-26). */
    public static void baoNhe(final Char p, final String s) {
        if (p == null || s == null || s.isEmpty()) {
            return;
        }
        try {
            p.sendMessage(coToast(p) ? taoToast(TIN, s) : MessageCreator.createServerAlertAutoOffMessage(s));
        } catch (final Exception ignored) {
        }
    }

    static void gui(final Char p, final byte loai, final String s) {
        if (p == null || s == null || s.isEmpty()) {
            return;
        }
        final Phien ph = p.phienDung;
        if (ph != null) {
            ph.daNoi = true;
        }
        try {
            p.sendMessage(coToast(p) ? taoToast(loai, s) : MessageCreator.createServerAlertMessage(s, ""));
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== phien

    /** Trang thai truoc khi chay ham dung cu (de biet ham cu da lam gi khi no khong noi gi). */
    static final class Phien {
        final Thread luong = Thread.currentThread();
        boolean daNoi;
        /** Nuot hop thoai 37 cua ham con (da co thong bao tong ket rieng). */
        boolean nuot;
        Item mon;
        int thuoc = -1;
        String ten = "";
        long xu, exp;
        int vang, vangKhoa, cap, hp, mp, maxhp, maxmp;
        int[] binh;
        HashMap<Integer, Integer> tui, ngoc;
    }

    static Phien chup(final Char p) {
        final Phien ph = new Phien();
        try {
            ph.xu = p.getxu();
            ph.vang = p.getLuong();
            ph.vangKhoa = p.getLuongLock();
            ph.cap = p.lvDetail == null ? 0 : p.lvDetail.lv;
            ph.exp = p.lvDetail == null ? 0L : p.lvDetail.getExp();
            ph.hp = p.hp;
            ph.mp = p.mp;
            ph.maxhp = p.maxhp;
            ph.maxmp = p.maxmp;
            ph.binh = p.potions == null ? new int[0] : p.potions.clone();
            ph.tui = demTui(p);
            ph.ngoc = demNgoc(p);
        } catch (final Exception ignored) {
            // thieu du lieu: phien van chay, chi bao it hon
        }
        return ph;
    }

    static HashMap<Integer, Integer> demTui(final Char p) {
        final HashMap<Integer, Integer> m = new HashMap<Integer, Integer>();
        if (p.iItems == null) {
            return m;
        }
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && it.getTemplate() != null) {
                    m.merge(Integer.valueOf(it.getTemplate().id), Integer.valueOf(1), Integer::sum);
                }
            }
        }
        return m;
    }

    static HashMap<Integer, Integer> demNgoc(final Char p) {
        final HashMap<Integer, Integer> m = new HashMap<Integer, Integer>();
        if (p.gemItem == null) {
            return m;
        }
        synchronized (p.gemItem) {
            for (final data.GemItem g : p.gemItem) {
                if (g != null) {
                    m.merge(Integer.valueOf(g.idGemTemplate), Integer.valueOf(1), Integer::sum);
                }
            }
        }
        return m;
    }

    /** Char.sendMessage goi khi dang co phien: ghi nhan da co phan hoi; doi hop thoai OK thanh -104. */
    static Message quaPhien(final Char p, final Message m) {
        final Phien ph = p.phienDung;
        if (ph == null || m == null || ph.luong != Thread.currentThread()) {
            return m;
        }
        switch (m.command) {
            case 37: {
                if (ph.nuot) {
                    return null;
                }
                ph.daNoi = true;
                if (!coToast(p)) {
                    return m;
                }
                final String[] chu = docHopThoai(m);
                if (chu == null || !chu[1].isEmpty()) {
                    return m; // co duong dan (tai ban moi...): giu hop thoai
                }
                try {
                    final Message t = new Message(LENH);
                    t.dos.writeByte(TIN);
                    t.dos.writeUTF(chu[0]); // da doi chu o createServerAlertMessage
                    t.dos.writeByte(HanhDongVatPham.kieuDang());
                    return t;
                } catch (final IOException e) {
                    return m;
                }
            }
            case LENH:
            case -30: // menu (the bai, Hoa Ky Lan...)
            case -31: // o nhap
            case -32: // popup
            case 27: // tin chat cua minh ("Chuc mung ban nhan duoc...")
            case -92: // tien hoa
                ph.daNoi = true;
                return m;
            default:
                return m;
        }
    }

    /** {noi dung, duong dan} cua goi 37, null neu khong doc duoc. */
    static String[] docHopThoai(final Message m) {
        try {
            final byte[] b = m.toByteArray();
            final int dau = (b.length > 0 && b[0] == (byte) -128) ? 6 : 3;
            final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, dau, b.length - dau));
            final String s = r.readUTF();
            final String url = r.available() > 1 ? r.readUTF() : "";
            return new String[]{s, url};
        } catch (final Exception e) {
            return null;
        }
    }

    // ================================================================== lenh 29: hanh trang

    /** Map.doUseItem (lenh 29). */
    public static void dung(final Map map, final Char p, final Message message) throws IOException {
        SO_BAM.incrementAndGet();
        final short id = docShort(message);
        if (p.hp <= 0) {
            khiGuc(map, p, id, message);
            return;
        }
        final Item mon = timTrongTui(p, id);
        if (mon == null && !dangMacThu(p, id)) {
            if (thaoTrangBi(p, id)) {
                return;
            }
            loi(p, "Vật phẩm không còn trong hành trang.");
            guiTui(p);
            return;
        }
        if (mon != null && moQuaDoi(p, mon)) {
            return;
        }
        final Phien ph = chup(p);
        ph.mon = mon;
        ph.ten = mon == null ? "" : ten(mon);
        p.phienDung = ph;
        try {
            map.doUseItemGoc(p, message);
        } catch (final Exception e) {
            SO_LOI_NGOAI_LE.incrementAndGet();
            com.htbb.log.Nhat.loi("DUNG", "lenh 29 tpl " + (mon == null || mon.getTemplate() == null ? -1 : mon.getTemplate().id), e);
        } finally {
            p.phienDung = null;
        }
        if (!ph.daNoi && mon != null) {
            ketLuanItem(p, ph);
        }
        dongBoHpPp(p, ph);
    }

    /** So goi dong bo HP / PP da gui ngay sau khi dung (kiem thu / thong ke). */
    static final java.util.concurrent.atomic.AtomicInteger SO_DONG_BO = new java.util.concurrent.atomic.AtomicInteger();

    /**
     * Dung vat pham / binh xong ma HP, PP hay HP / PP toi da doi: gui NGAY goi 60 (HP, max, PP) cho chinh nguoi choi va
     * nguoi xung quanh (dong doi thay thanh mau tren dau / khung muc tieu), khong cho goi dinh ky. Truoc 0.9.3 vai
     * duong (vat pham hoi trong tui, ban do thi tran) chi bao thong bao hoac chi gui hanh trang -> HUD hien HP cu.
     */
    static void dongBoHpPp(final Char p, final Phien ph) {
        if (p.hp == ph.hp && p.mp == ph.mp && p.maxhp == ph.maxhp && p.maxmp == ph.maxmp) {
            return;
        }
        try {
            final int cong = Math.max(0, Math.min(Short.MAX_VALUE, p.hp - ph.hp));
            p.sendMessage(MessageCreator.createNew_HMP_Message(p, cong));
            p.sendToNearPlayer(MessageCreator.createNew_HMP_Message(p, cong));
            SO_DONG_BO.incrementAndGet();
        } catch (final Exception ignored) {
        }
    }

    /**
     * Qua le / phieu (vd 1057..1061): vat pham ma cach dung DUY NHAT la mot dong doi "item:&lt;id&gt;x1" trong Cua hang
     * dai han (data_cua_hang_lau_dai). Bam "Su dung" trong tui = mo qua (doi dong do), khong phai tim NPC.
     * Chi nhan dong chi phi dung mot mon nay x1 (khong dong nao can them xu / vat pham khac).
     */
    static boolean moQuaDoi(final Char p, final Item mon) {
        if (mon.getTemplate() == null || mon.getTemplate().type != 28 || VatPhamDungLauDai.BANG.containsKey(Integer.valueOf(mon.getTemplate().id))) {
            return false;
        }
        if (!com.htbb.pokemon.LauDai.bat()) {
            return false;
        }
        Object[] dong = null;
        for (final Object[] r : CuaHangLauDai.BANG) {
            final java.util.List<long[]> chi = CuaHangLauDai.docChiPhi((String) r[3]);
            if (chi.size() == 1 && chi.get(0)[0] == 2 && chi.get(0)[1] == mon.getTemplate().id && chi.get(0)[2] == 1) {
                if (dong != null) {
                    return false; // nhieu cach doi: de nguoi choi chon o Cua hang
                }
                dong = r;
            }
        }
        if (dong == null) {
            return false;
        }
        final Phien ph = chup(p);
        ph.mon = mon;
        ph.nuot = true; // CuaHangLauDai.phat bao tung mon bang hop thoai: gop thanh mot thong bao
        p.phienDung = ph;
        String kq;
        try {
            kq = CuaHangLauDai.mua(p, dong);
        } finally {
            p.phienDung = null;
        }
        if (kq.startsWith("Đã nhận")) {
            guiTui(p);
            try {
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharGemItem(p));
            } catch (final Exception ignored) {
            }
            bao(p, "Mở " + ten(mon) + ": " + kq.substring(kq.indexOf(':') + 1).trim());
        } else {
            loi(p, kq);
        }
        return true;
    }

    /** Vat pham dang trong tui theo id (null neu khong co). */
    static Item timTrongTui(final Char p, final short id) {
        if (p.iItems == null) {
            return null;
        }
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && it.id == id) {
                    return it;
                }
            }
        }
        return null;
    }

    static boolean dangMacThu(final Char p, final short id) {
        if (p.awItems == null) {
            return false;
        }
        for (int i = 0; i < p.awItems.size(); i++) {
            final Item it = p.awItems.elementAt(i);
            if (it != null && it.id == id) {
                return true;
            }
        }
        return false;
    }

    static short docShort(final Message message) throws IOException {
        message.dis.mark(8);
        final short id = message.dis.readShort();
        message.dis.reset();
        return id;
    }

    /** Da guc: chi vat pham hoi sinh dung duoc (VatPhamDungLauDai -> HoiSinhGame); con lai bao ro. */
    static void khiGuc(final Map map, final Char p, final short id, final Message message) {
        final Item mon = timTrongTui(p, id);
        if (mon == null) {
            loi(p, "Vật phẩm không còn trong hành trang.");
            guiTui(p);
            return;
        }
        if (HoiSinhGame.hpVatPham(mon) <= 0) {
            loi(p, "Không dùng được " + ten(mon) + " khi đã gục. Hãy hồi sinh trước.");
            return;
        }
        final Phien ph = chup(p);
        ph.mon = mon;
        ph.ten = ten(mon);
        p.phienDung = ph;
        try {
            if (!VatPhamDungLauDai.dung(p, mon)) {
                loi(p, "Không dùng được " + ten(mon) + " lúc này.");
            }
        } catch (final Exception e) {
            SO_LOI_NGOAI_LE.incrementAndGet();
            com.htbb.log.Nhat.loi("DUNG", "hoi sinh tu tui", e);
        } finally {
            p.phienDung = null;
        }
        if (!ph.daNoi) {
            ketLuanItem(p, ph);
        }
    }

    /**
     * Bam "Thao ra" mot mon trang bi NHAN VAT dang mac (client 0.9.2 gui lenh 29 voi id mon dang mac). Game goc
     * khong co cach thao trang bi nhan vat (chi mac de len). Vu khi khong thao (luon phai cam mot mon).
     *
     * @return true neu id la mon dang mac (da xu ly / da bao ly do)
     */
    static boolean thaoTrangBi(final Char p, final short id) {
        final Item[] mac;
        try {
            mac = p.getArrayCharWearing();
        } catch (final Exception e) {
            return false;
        }
        if (mac == null) {
            return false;
        }
        int o = -1;
        for (int i = 0; i < mac.length; i++) {
            if (mac[i] != null && mac[i].id == id) {
                o = i;
                break;
            }
        }
        if (o < 0) {
            return false;
        }
        final Item mon = mac[o];
        final int t = mon.getTemplate() == null ? -1 : mon.getTemplate().type;
        if (Map.isWeapone(t)) {
            loi(p, "Không tháo được đồ cầm tay: hãy cầm món khác để thay.");
            return true;
        }
        if (p.isFullInventory()) {
            loi(p, "Hành trang đầy, không tháo ra được.");
            return true;
        }
        mac[o] = null;
        mon.place = 0;
        mon.beKick = 0;
        if (p.itemAx == mon) {
            p.itemAx = null;
        }
        p.iItems.add(mon);
        try {
            data.Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "Thao " + mon.getTemplate().name + "_" + mon.plus
                    + "|" + mon.getAttribute() + "|" + mon.getDBInfo(), "change");
        } catch (final Exception ignored) {
        }
        guiSauDoiDo(p);
        HanhDongVatPham.datKieu(HanhDongVatPham.THAO);
        bao(p, "Đã tháo " + ten(mon) + ".");
        return true;
    }

    /** Cac goi doi do nhu duong mac (Map.doUseItem cuoi ham). */
    static void guiSauDoiDo(final Char p) {
        try {
            p.calculateAttrib();
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
            MessageCreator.createMsgCharMonster(p, p);
        } catch (final Exception e) {
            com.htbb.log.Nhat.loi("DUNG", "gui sau doi do", e);
        }
    }

    static void guiTui(final Char p) {
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        } catch (final Exception ignored) {
        }
    }

    /** Ham cu khong noi gi: doan ket qua tu trang thai truoc/sau. */
    static void ketLuanItem(final Char p, final Phien ph) {
        SO_TU_BAO.incrementAndGet();
        final Item mon = ph.mon;
        final boolean conTrongTui = p.iItems != null && p.iItems.contains(mon);
        final String loiIch = loiIch(p, ph, -1);
        if (!conTrongTui) {
            if (dangMac(p, mon)) {
                HanhDongVatPham.datKieu(HanhDongVatPham.TRANG_BI);
                bao(p, "Đã trang bị " + ph.ten + ".");
            } else {
                bao(p, "Đã dùng " + ph.ten + (loiIch.isEmpty() ? "." : ": " + loiIch + "."));
            }
            return;
        }
        if (!loiIch.isEmpty()) {
            bao(p, "Đã dùng " + ph.ten + ": " + loiIch + ".");
            return;
        }
        loi(p, lyDo(p, mon));
    }

    static boolean dangMac(final Char p, final Item mon) {
        try {
            final Item[] mac = p.getArrayCharWearing();
            if (mac != null) {
                for (final Item it : mac) {
                    if (it == mon) {
                        return true;
                    }
                }
            }
            if (p.awItems != null && p.awItems.contains(mon)) {
                return true;
            }
            return p.itemThanKhi == mon || p.itemVukhiThoiTrang == mon || (p.wModel != null && p.iItems != null && !p.iItems.contains(mon)
                    && Map.isModelClothes(mon.getTemplate().atb[9]));
        } catch (final Exception e) {
            return false;
        }
    }

    // ================================================================== ly do / goi y

    /** Mon nay co mac / cam / trang bi duoc (di nhanh MapMuaBan.exchangeItem) khong. */
    public static boolean macDuoc(final Item it) {
        if (it == null || it.getTemplate() == null) {
            return false;
        }
        final ItemTemplates t = it.getTemplate();
        if (SkinThuCuoi.laItemSkin(t.id) || Map.isWearingAnimal(t.type) || it.isChoiLuaLanBienHinh()) {
            return true;
        }
        if (t.atb != null && t.atb.length > 9 && Map.isModelClothes(t.atb[9])) {
            return true; // thoi trang / cai trang: MapMuaBan doi hinh (wModel)
        }
        return Char.POS_WEARING_CHAR != null && Char.POS_WEARING_CHAR.containsKey(Integer.valueOf(t.type));
    }

    /** Ly do khong dung duoc (ro rang, co huong dan neu biet). */
    public static String lyDo(final Char p, final Item it) {
        if (it == null || it.getTemplate() == null) {
            return "Vật phẩm không còn trong hành trang.";
        }
        final ItemTemplates t = it.getTemplate();
        final String ten = ten(it);
        if (it.isSelling || it.prizeSell > 0) {
            return ten + " đang được bán trong gian hàng.";
        }
        final int cap = p.lvDetail == null ? 0 : p.lvDetail.lv;
        if (it.level > cap) {
            return "Cần đạt cấp " + it.level + " để dùng " + ten + ".";
        }
        if ((it.isHuyetLinhThao() || it.isHuyetBoDe()) && p.petUsing == null) {
            return "Cần có thú nuôi đang đi theo để dùng " + ten + ".";
        }
        if (Map.isItemTangTocVanTieu(t.type) && p.monster == null) {
            return ten + " chỉ dùng khi đang giao hàng.";
        }
        if (Map.isTheBaiMuaban(t.type)) {
            return "Không dùng được " + ten + " ở đây.";
        }
        if (Map.isWearingAnimal(t.type) && p.animalRide == null) {
            return "Phải cưỡi thú mới trang bị được " + ten + ".";
        }
        if (t.type == 24) {
            return ten + " không còn dùng được.";
        }
        if (it.isTuHonDan()) {
            return ten + ": dùng khi cho thú nuôi ăn (bảng Thú nuôi, Cho ăn).";
        }
        if (macDuoc(it)) {
            if (Map.isWeapone(t.type) && t.clazz != p.charClass) {
                return ten + " dành cho hệ khác, không cầm được.";
            }
            if (t.gender != 0 && p.gender != 0 && t.gender != p.gender) {
                return ten + " chỉ dành cho Pokémon " + (t.gender == 1 ? "đực" : "cái") + ".";
            }
            if (it.clazz >= 0 && it.clazz != p.charClass && !Map.isWeapone(t.type) && t.type != 13 && t.type != 19) {
                return ten + " dành cho hệ khác, không mặc được.";
            }
            return "Không trang bị được " + ten + " lúc này.";
        }
        return goiY(it);
    }

    /** Vat pham khong dung truc tiep: dong thu hai cua ten (mo ta trong data_item) hoac goi y chung. */
    static String goiY(final Item it) {
        final String day = it.getTemplate().name == null ? "" : it.getTemplate().name;
        final int xuong = day.indexOf('\n');
        final String ten = ten(it);
        if (xuong > 0 && xuong < day.length() - 1) {
            return ten + ": " + day.substring(xuong + 1).replace('\n', ' ').trim();
        }
        final int t = it.getTemplate().type;
        if (t == 29 || it.isManhSuuTap()) {
            return ten + " dùng để ghép bộ sưu tập ở Thợ rèn bí ẩn.";
        }
        if (it.isPet() || t == 26 || t == 27) {
            return ten + ": mang tới NPC thú nuôi để dùng.";
        }
        return ten + " là nguyên liệu / vật phẩm đổi thưởng, không dùng trực tiếp. Xem Sổ tay huấn luyện, mục Cửa hàng và đổi vật phẩm.";
    }

    /** Ten hien thi (dong dau cua ten mau). */
    static String ten(final Item it) {
        if (it == null || it.getTemplate() == null || it.getTemplate().name == null) {
            return "vật phẩm";
        }
        return gon(it.getTemplate().name);
    }

    /** Dong dau, bo dau cham cuoi ("Kẹo thú cưng." -> "Kẹo thú cưng"). */
    static String gon(final String s) {
        final int i = s.indexOf('\n');
        String r = (i > 0 ? s.substring(0, i) : s).trim();
        while (r.endsWith(".")) {
            r = r.substring(0, r.length() - 1).trim();
        }
        return r.isEmpty() ? "vật phẩm" : r;
    }

    static String tenThuoc(final int t) {
        try {
            String r = gon(Map.potionTemplates2.get(t).name);
            final int cham = r.indexOf(". ");
            if (cham > 0) {
                r = r.substring(0, cham); // "Kẹo Poké. Tăng 1% kinh nghiệm..." -> "Kẹo Poké"
            }
            return r;
        } catch (final Exception e) {
            return "vật phẩm";
        }
    }

    static String tenNgoc(final int id) {
        try {
            return Map.gemTemplate[id].name;
        } catch (final Exception e) {
            return "ngọc " + id;
        }
    }

    static String tenMau(final int id) {
        try {
            final ItemTemplates t = Map.itemTemplates.get(0).get(id);
            if (t != null && t.name != null) {
                final int i = t.name.indexOf('\n');
                return (i > 0 ? t.name.substring(0, i) : t.name).trim();
            }
        } catch (final Exception ignored) {
        }
        return "vật phẩm " + id;
    }

    /** "+60.278 xu, lên cấp 37, +1 Vảy rồng" (bo qua chinh vat pham / binh vua dung). */
    static String loiIch(final Char p, final Phien ph, final int boBinh) {
        final StringBuilder sb = new StringBuilder();
        try {
            them(sb, p.getxu() - ph.xu, " xu");
            them(sb, p.getLuong() - ph.vang, " lượng");
            them(sb, p.getLuongLock() - ph.vangKhoa, " lượng khoá");
            final int cap = p.lvDetail == null ? 0 : p.lvDetail.lv;
            if (cap > ph.cap) {
                sb.append(sb.length() > 0 ? ", " : "").append("lên cấp ").append(cap);
            } else if (p.lvDetail != null && p.lvDetail.getExp() > ph.exp) {
                them(sb, p.lvDetail.getExp() - ph.exp, " EXP");
            }
            if (p.maxhp == ph.maxhp) {
                them(sb, p.hp - ph.hp, " HP");
            }
            if (p.maxmp == ph.maxmp) {
                them(sb, p.mp - ph.mp, " PP");
            }
            if (p.potions != null && ph.binh != null) {
                for (int i = 1; i < p.potions.length && i < ph.binh.length; i++) {
                    if (i != boBinh && p.potions[i] > ph.binh[i]) {
                        them(sb, p.potions[i] - ph.binh[i], " " + tenThuoc(i));
                    }
                }
            }
            if (ph.tui != null) {
                for (final Entry<Integer, Integer> e : demTui(p).entrySet()) {
                    final int truoc = ph.tui.containsKey(e.getKey()) ? ph.tui.get(e.getKey()).intValue() : 0;
                    if (e.getValue().intValue() > truoc) {
                        them(sb, e.getValue().intValue() - truoc, " " + tenMau(e.getKey().intValue()));
                    }
                }
            }
            if (ph.ngoc != null) {
                for (final Entry<Integer, Integer> e : demNgoc(p).entrySet()) {
                    final int truoc = ph.ngoc.containsKey(e.getKey()) ? ph.ngoc.get(e.getKey()).intValue() : 0;
                    if (e.getValue().intValue() > truoc) {
                        them(sb, e.getValue().intValue() - truoc, " " + tenNgoc(e.getKey().intValue()));
                    }
                }
            }
        } catch (final Exception ignored) {
            // bao it hon
        }
        String s = sb.toString();
        if (s.length() > 160) {
            s = s.substring(0, 157) + "...";
        }
        return s;
    }

    static void them(final StringBuilder sb, final long n, final String donVi) {
        if (n <= 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append('+').append(n).append(donVi);
    }

    // ================================================================== lenh 22: binh thuoc

    /**
     * Map.doUsePotion / RealController (lenh 22). {@code map} null = duong doUsePotion.doUsePotion (RealController).
     */
    public static void dungThuoc(final Map map, final Char p, final Message message) throws IOException {
        SO_BAM.incrementAndGet();
        message.dis.mark(8);
        final int t = message.dis.readUnsignedByte();
        message.dis.reset();
        if (p.potions == null || t <= 0 || t >= p.potions.length) {
            return;
        }
        final String ten = tenThuoc(t);
        if (p.potions[t] <= 0) {
            loi(p, "Không còn " + ten + ".");
            try {
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            } catch (final Exception ignored) {
            }
            return;
        }
        if (p.hp <= 0) {
            if (t == com.htbb.pokemon.HoiSinh.THUOC_HOI_SINH && HoiSinhGame.apDung(p)) {
                final String cam = HoiSinhGame.chanDungTuTui(p);
                if (cam != null) {
                    loi(p, cam);
                } else {
                    HoiSinhGame.dungVatPham(p, com.htbb.pokemon.HoiSinh.LC_THUOC, "tui");
                }
            } else {
                loi(p, "Không dùng được " + ten + " khi đã gục. Hãy hồi sinh trước.");
            }
            return;
        }
        final String chan = chanThuoc(p, t, ten);
        if (chan != null) {
            loi(p, chan);
            return;
        }
        // Ve dich chuyen: KHONG tru o day - mo menu diem den, tru 1 ve khi da toi noi (VeDichChuyenGame.chonDiem)
        if (t == com.htbb.pokemon.VeDichChuyen.ID) {
            VeDichChuyenGame.dungTuTui(p);
            return;
        }
        // 0.9.14: sao goi / trung thu cuoi -> thu cuoi vinh vien (truoc: "Khong the su dung", chi NPC ap trung doi duoc)
        if (ThuCuoiTuTui.la(t)) {
            if (p.userTrade != null && p.userTrade.size() > 0) {
                loi(p, "Đang giao dịch: xong giao dịch rồi hãy dùng " + ten + ".");
                ThuCuoiTuTui.guiTui(p);
                return;
            }
            ThuCuoiTuTui.tao(p, t, "tui");
            return;
        }
        // 0.9.14: vat pham khong dung tu tui (nguyen lieu, ve dung o NPC, do giu trong tui...): bao ly do that, KHONG tru.
        // Truoc: vai id roi vao nhanh default cua doUsePotion - bi tru mat ma khong co tac dung.
        if (!com.htbb.pokemon.MoTaVatPham.dungDuoc(t)) {
            final String ly = com.htbb.pokemon.MoTaVatPham.lyDo(t);
            loi(p, ten + ": " + (ly.isEmpty() ? "không dùng trực tiếp từ túi." : ly));
            ThuCuoiTuTui.guiTui(p); // client cu tru truoc 1 o tui cua no: gui lai so that
            return;
        }
        final Phien ph = chup(p);
        ph.thuoc = t;
        ph.ten = ten;
        p.phienDung = ph;
        try {
            if (map != null) {
                MapDungThuoc.doUsePotion(map, p, message);
            } else {
                doUsePotion.doUsePotion(p, message);
            }
        } catch (final Exception e) {
            SO_LOI_NGOAI_LE.incrementAndGet();
            com.htbb.log.Nhat.loi("DUNG", "lenh 22 binh " + t, e);
        } finally {
            p.phienDung = null;
        }
        dongBoHpPp(p, ph);
        if (p.potions[t] < ph.binh[t]) {
            DiemMocNhiemVu.khiDungThuoc(p, t); // binh da tru khoi tui (huong dan tan thu: buoc dung thuoc)
        }
        if (!ph.daNoi && !co(THUOC_HP, t) && !co(THUOC_MP, t)) {
            // Binh HP/PP thanh cong khong can bao (thanh mau len); that bai la cho hoi binh khi bam lien tuc.
            SO_TU_BAO.incrementAndGet();
            final String loiIch = loiIch(p, ph, t);
            if (p.potions[t] < ph.binh[t]) {
                bao(p, "Đã dùng " + ten + (loiIch.isEmpty() ? "." : ": " + loiIch + "."));
            } else if (!loiIch.isEmpty()) {
                bao(p, "Đã dùng " + ten + ": " + loiIch + ".");
            } else if (System.currentTimeMillis() - p.timeUsePotion[t] > Char.POTION_COOLDOWN_MS) {
                loi(p, "Không dùng được " + ten + " lúc này.");
            }
        } else if (!ph.daNoi && coToast(p) && (p.hp > ph.hp || p.mp > ph.mp)) {
            // 0.9.3: binh HP / PP thanh cong cung co thong bao DUNG (khac thong bao mua) - thong bao nhe, khong am bao
            final StringBuilder b = new StringBuilder("Đã dùng ").append(ten).append(":");
            if (p.hp > ph.hp) {
                b.append(" +").append(p.hp - ph.hp).append(" HP");
            }
            if (p.mp > ph.mp) {
                b.append(p.hp > ph.hp ? "," : "").append(" +").append(p.mp - ph.mp).append(" PP");
            }
            gui(p, TIN, b.append('.').toString());
        }
    }

    /** Ly do chan binh truoc khi tru (null = cho ham cu chay). */
    static String chanThuoc(final Char p, final int t, final String ten) {
        if (t == com.htbb.pokemon.HoiSinh.THUOC_HOI_SINH && p.hp > 0) {
            return ten + " chỉ dùng được khi đã gục.";
        }
        if (co(THUOC_HP, t) && p.hp >= p.maxhp) {
            return "HP đã đầy, chưa cần dùng " + ten + ".";
        }
        if (co(THUOC_MP, t) && p.mp >= p.maxmp) {
            return "PP đã đầy, chưa cần dùng " + ten + ".";
        }
        try {
            if (t >= MOBA_TU && t <= MOBA_DEN && p.map != null && !p.map.isMapChienTruongMoba() && !server.TeamServer.isServerLocal()) {
                return ten + " chỉ dùng được trong Chiến trường.";
            }
        } catch (final Exception ignored) {
        }
        if (co(THUOC_HOP, t) && p.isFullInventory()) {
            return "Hành trang đầy: cần ít nhất 1 ô trống để mở " + ten + ".";
        }
        return null;
    }

    static boolean co(final int[] a, final int v) {
        for (final int x : a) {
            if (x == v) {
                return true;
            }
        }
        return false;
    }
}
