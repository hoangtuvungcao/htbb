package real;

import io.Message;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MOI thao tac vat pham (mua 24, ban 28, dung 22 / 29 = dung / mac / thao, vut 61, giao dich 66, ban ngoc 78) di qua
 * day: chup trang thai TRUOC, chay ham cu, roi:
 * <ol>
 *   <li>thong bao DUNG LOAI (0.9.3: "Da mua X ×N (−gia)", "Da ban X (+gia)", "Da dung X: +...", "Da trang bi X") -
 *   truoc 0.9.3 mua / ban im lang (client tu doan "Da mua. Mon do dang o trong hanh trang." TRUOC khi server lam) hoac
 *   hop thoai chung, nen mua va dung trong giong nhau;</li>
 *   <li>goi trang thai NGAY (khong cho goi dinh ky / mo lai bang): tui (16 loai 0 = xu + binh + luong + luong khoa,
 *   16 loai 1 = vat pham) neu ham cu chua gui ma co doi, va goi {@link #LENH_TRANG_THAI} (-105, client &gt;= 903)
 *   mang trang thai NHO DAY DU cua server - ca khi bi tu choi ("HP da day"), de client tra lai moi thay doi doan truoc.</li>
 * </ol>
 *
 * <p>Thong bao nhanh -104 (DungVatPham.LENH): byte loai, UTF noi dung, <b>byte kieu</b> ({@link #MUA}...; 0.9.3).
 * Client 0.9.2 chi doc 2 truong dau, byte thua bi bo qua -&gt; tuong thich nguoc.
 *
 * <p>Goi -105 (S-&gt;C, chi client &gt;= {@link #BAN_TRANG_THAI}): byte phien ban (1), byte kieu, int hp, int maxhp,
 * int mp, int maxmp, long xu, int luong, int luongKhoa, short cap, long exp, byte so binh, (byte id, int so)*, short so mon
 * trong tui, short so mon dang mac; 19/10/2026 noi them (client cu bo qua): short oDaMo, short oToiDaVe, byte oMoiVe,
 * int giaXuBuocKe, int soVe, short idVe, byte loaiVe - xem {@link MoRongTuiGame}.
 */
public final class HanhDongVatPham {

    public static final byte KHAC = 0, MUA = 1, BAN = 2, DUNG = 3, TRANG_BI = 4, THAO = 5, VUT = 6, TACH = 7, GIAO_DICH = 8, TANG = 9, MO_RONG = 10;
    public static final int LENH_TRANG_THAI = -105;
    /** Ban client dau tien doc goi -105 + byte kieu cua -104 (client_jar/build.sh 903). */
    public static final int BAN_TRANG_THAI = 903;

    public static final AtomicLong SO_HANH_DONG = new AtomicLong();
    public static final AtomicLong SO_GOI_TRANG_THAI = new AtomicLong();

    private HanhDongVatPham() {
    }

    /** Ham cu (doc goi tin, lam viec). */
    public interface Viec {
        void chay() throws IOException;
    }

    /** Ngu canh mot thao tac tren luong xu ly goi tin. */
    static final class NguCanh {
        final Char p;
        byte kieu;
        boolean daNoi, daGuiTui0, daGuiTui1;
        long xu;
        int luong, luongKhoa, hp, mp, maxhp, maxmp;
        int[] binh;
        IdentityHashMap<Item, Boolean> tui;
        int soMac;

        NguCanh(final Char p, final byte kieu) {
            this.p = p;
            this.kieu = kieu;
        }
    }

    private static final ThreadLocal<NguCanh> DANG = new ThreadLocal<NguCanh>();

    /**
     * So luong TUY CHON o cuoi goi (19/10/2026: -21 / -23 / 78 co o chon so luong ben client moi): short soLuong neu
     * con &gt;= 2 byte, nguoc lai 0 = "ca chong" (hanh vi cu, client cu khong gui).
     */
    public static int docSoLuong(final Message m) {
        try {
            if (m != null && m.dis != null && m.dis.available() >= 2) {
                return Math.max(0, m.dis.readShort());
            }
        } catch (final IOException ignored) {
        }
        return 0;
    }

    /** So luong thuc lay: xin &lt;= 0 = ca chong {@code co}; nguoc lai min(xin, co). */
    public static int kepSoLuong(final int xin, final int co) {
        return xin <= 0 ? Math.max(0, co) : Math.min(xin, Math.max(0, co));
    }

    /** Kieu thao tac dang chay tren luong nay (cho byte kieu cua -104), KHAC neu khong co. */
    public static byte kieuDang() {
        final NguCanh n = DANG.get();
        return n == null ? KHAC : n.kieu;
    }

    /** Doi kieu thao tac dang chay (vd dung -&gt; trang bi khi mon la do mac). */
    public static void datKieu(final byte k) {
        final NguCanh n = DANG.get();
        if (n != null) {
            n.kieu = k;
        }
    }

    static NguCanh chup(final Char p, final byte kieu) {
        final NguCanh n = new NguCanh(p, kieu);
        try {
            n.xu = p.getxu();
            n.luong = p.getLuong();
            n.luongKhoa = p.getLuongLock();
            n.hp = p.hp;
            n.mp = p.mp;
            n.maxhp = p.maxhp;
            n.maxmp = p.maxmp;
            n.binh = p.potions == null ? new int[0] : p.potions.clone();
            n.tui = tapTui(p);
            n.soMac = soMac(p);
        } catch (final Exception ignored) {
            // thieu du lieu (kiem thu ngoai server): van chay
        }
        return n;
    }

    static IdentityHashMap<Item, Boolean> tapTui(final Char p) {
        final IdentityHashMap<Item, Boolean> m = new IdentityHashMap<Item, Boolean>();
        if (p.iItems != null) {
            synchronized (p.iItems) {
                for (final Item it : p.iItems) {
                    if (it != null) {
                        m.put(it, Boolean.TRUE);
                    }
                }
            }
        }
        return m;
    }

    static int soMac(final Char p) {
        try {
            final Item[] mac = p.getArrayCharWearing();
            int n = 0;
            if (mac != null) {
                for (final Item it : mac) {
                    if (it != null) {
                        n++;
                    }
                }
            }
            return n;
        } catch (final Exception e) {
            return 0;
        }
    }

    /**
     * Chay thao tac {@code v} kieu {@code kieu} cho nhan vat {@code p}: thong bao dung loai neu ham cu khong noi gi, roi
     * gui trang thai server ngay. Long nhau (dung vat pham goi mua...): chi lop ngoai cung tong ket.
     */
    public static void boc(final Char p, final byte kieu, final Viec v) throws IOException {
        if (p == null) {
            v.chay();
            return;
        }
        if (DANG.get() != null) {
            v.chay();
            return;
        }
        SO_HANH_DONG.incrementAndGet();
        final NguCanh n = chup(p, kieu);
        DANG.set(n);
        try {
            v.chay();
            if (!n.daNoi) {
                tuBao(p, n);
            }
        } finally {
            DANG.remove();
        }
        sauHanhDong(p, n);
    }

    /** Char.sendMessage: ghi nhan goi tin cua ham cu trong luc thao tac (da noi chua, da gui tui chua). */
    static Message qua(final Char p, final Message m) {
        final NguCanh n = DANG.get();
        if (n == null || n.p != p || m == null) {
            return m;
        }
        switch (m.command) {
            case 16: {
                final int loai = byteDau(m);
                if (loai == 0) {
                    n.daGuiTui0 = true;
                } else if (loai == 1) {
                    n.daGuiTui1 = true;
                }
                return m;
            }
            case 37: {
                n.daNoi = true;
                // client co thong bao nhanh: hop thoai OK cua ham cu (khong co duong dan) -> -104 co kieu thao tac
                if (p.phienDung != null || !DungVatPham.coToast(p)) {
                    return m;
                }
                final String[] chu = DungVatPham.docHopThoai(m);
                if (chu == null || !chu[1].isEmpty()) {
                    return m;
                }
                try {
                    final Message t = new Message(DungVatPham.LENH);
                    t.dos.writeByte(laTuChoi(chu[0]) ? DungVatPham.LOI : DungVatPham.TIN);
                    t.dos.writeUTF(chu[0]);
                    t.dos.writeByte(n.kieu);
                    return t;
                } catch (final IOException e) {
                    return m;
                }
            }
            case DungVatPham.LENH:
            case -26:
            case -30:
            case -31:
            case -32:
            case 27:
            case -92:
                n.daNoi = true;
                return m;
            default:
                return m;
        }
    }

    /** Hop thoai cua ham cu la tu choi (khong du tien, day, khong the...)? */
    static boolean laTuChoi(final String s) {
        final String t = s.toLowerCase();
        return t.startsWith("không") || t.contains("đã đủ") || t.contains("đầy") || t.contains("hết") || t.contains("không đủ") || t.contains("không thể");
    }

    static int byteDau(final Message m) {
        try {
            final byte[] b = m.toByteArray();
            final int dau = (b.length > 0 && b[0] == (byte) -128) ? 6 : 3;
            return b.length > dau ? b[dau] : -1;
        } catch (final Exception e) {
            return -1;
        }
    }

    // ------------------------------------------------------------------ thong bao tu tong ket

    static String xu(final long v) {
        return com.htbb.pokemon.HoiSinh.so(v);
    }

    /** Doan ket qua tu truoc / sau khi ham cu im lang (mua / ban / vut). */
    static void tuBao(final Char p, final NguCanh n) {
        final List<String> them = new ArrayList<String>();
        final List<String> bot = new ArrayList<String>();
        try {
            if (p.potions != null && n.binh != null) {
                for (int i = 0; i < p.potions.length && i < n.binh.length; i++) {
                    final int d = p.potions[i] - n.binh[i];
                    if (d > 0) {
                        them.add(DungVatPham.tenThuoc(i) + " ×" + d);
                    } else if (d < 0) {
                        bot.add(DungVatPham.tenThuoc(i) + " ×" + (-d));
                    }
                }
            }
            final IdentityHashMap<Item, Boolean> sau = tapTui(p);
            if (n.tui != null) {
                demTen(sau, n.tui, them);
                demTen(n.tui, sau, bot);
            }
        } catch (final Exception ignored) {
        }
        final long dXu = p.getxu() - n.xu;
        final int dLuong = p.getLuong() - n.luong;
        final int dKhoa = p.getLuongLock() - n.luongKhoa;
        final String tien = tien(dXu, dLuong, dKhoa);
        switch (n.kieu) {
            case MUA:
                if (!them.isEmpty()) {
                    DungVatPham.bao(p, "Đã mua " + noi(them) + (tien.isEmpty() ? "" : " (" + tien + ")") + ".");
                } else {
                    DungVatPham.loi(p, "Không mua được: không đủ tiền, hành trang đầy hoặc món đã hết.");
                }
                return;
            case BAN:
                if (!bot.isEmpty() || dXu > 0) {
                    DungVatPham.bao(p, "Đã bán " + (bot.isEmpty() ? "vật phẩm" : noi(bot)) + (tien.isEmpty() ? "" : " (" + tien + ")") + ".");
                } else {
                    DungVatPham.loi(p, "Không bán được vật phẩm này.");
                }
                return;
            case VUT:
                if (!bot.isEmpty()) {
                    DungVatPham.bao(p, "Đã bỏ " + noi(bot) + " ra đất.");
                }
                return;
            default:
                return;
        }
    }

    /** Ten (gop so luong) cac mon co trong a ma khong co trong b. */
    static void demTen(final IdentityHashMap<Item, Boolean> a, final IdentityHashMap<Item, Boolean> b, final List<String> ra) {
        final java.util.LinkedHashMap<String, Integer> dem = new java.util.LinkedHashMap<String, Integer>();
        for (final Item it : a.keySet()) {
            if (!b.containsKey(it)) {
                dem.merge(DungVatPham.ten(it), Integer.valueOf(1), Integer::sum);
            }
        }
        for (final java.util.Map.Entry<String, Integer> e : dem.entrySet()) {
            ra.add(e.getValue().intValue() > 1 ? e.getKey() + " ×" + e.getValue() : e.getKey());
        }
    }

    static String noi(final List<String> ds) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < ds.size() && i < 4; i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(ds.get(i));
        }
        if (ds.size() > 4) {
            b.append("...");
        }
        return b.toString();
    }

    /** "−500 xu, −2 lượng" / "+120 xu" (rong neu khong doi). */
    static String tien(final long dXu, final int dLuong, final int dKhoa) {
        final StringBuilder b = new StringBuilder();
        if (dXu != 0) {
            b.append(dXu > 0 ? "+" : "−").append(xu(Math.abs(dXu))).append(" xu");
        }
        if (dLuong != 0) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(dLuong > 0 ? "+" : "−").append(xu(Math.abs(dLuong))).append(" lượng");
        }
        if (dKhoa != 0) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(dKhoa > 0 ? "+" : "−").append(xu(Math.abs(dKhoa))).append(" lượng khoá");
        }
        return b.toString();
    }

    // ------------------------------------------------------------------ dong bo trang thai

    /** Sau thao tac: tui / tien doi ma ham cu chua gui -&gt; gui (giao thuc cu, moi client); client moi: goi -105. */
    static void sauHanhDong(final Char p, final NguCanh n) {
        try {
            boolean doiTien = p.getxu() != n.xu || p.getLuong() != n.luong || p.getLuongLock() != n.luongKhoa;
            boolean doiBinh = false;
            if (p.potions != null && n.binh != null) {
                for (int i = 0; i < p.potions.length && i < n.binh.length; i++) {
                    if (p.potions[i] != n.binh[i]) {
                        doiBinh = true;
                        break;
                    }
                }
            }
            // giao dich: client huy bang giao dich moi khi nhan goi tui (GameScr.onCharInventory -> cancelTrade) -> khong gui
            // them goi tui o day (ham giao dich tu gui khi xong); chi goi -105
            final boolean guiTui = n.kieu != GIAO_DICH;
            if (guiTui && (doiTien || doiBinh) && !n.daGuiTui0) {
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            }
            final IdentityHashMap<Item, Boolean> sau = tapTui(p);
            boolean doiTui = n.tui != null && (sau.size() != n.tui.size() || !sau.keySet().containsAll(n.tui.keySet()));
            if (guiTui && doiTui && !n.daGuiTui1) {
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
            }
            if (coTrangThai(p)) {
                p.sendMessage(taoTrangThai(p, n.kieu));
                SO_GOI_TRANG_THAI.incrementAndGet();
            }
        } catch (final Exception e) {
            com.htbb.log.Nhat.loi("HANH_DONG", "dong bo trang thai", e);
        }
    }

    static boolean coTrangThai(final Char p) {
        return p != null && p.session != null && p.session.banClient >= BAN_TRANG_THAI;
    }

    /** Goi -105: trang thai nho day du cua nguoi choi (nguon su that cho kho trang thai client). */
    public static Message taoTrangThai(final Char p, final byte kieu) throws IOException {
        final Message m = new Message(LENH_TRANG_THAI);
        m.dos.writeByte(1);
        m.dos.writeByte(kieu);
        m.dos.writeInt(p.hp);
        m.dos.writeInt(p.maxhp);
        m.dos.writeInt(p.mp);
        m.dos.writeInt(p.maxmp);
        m.dos.writeLong(p.getxu());
        m.dos.writeInt(p.getLuong());
        m.dos.writeInt(p.getLuongLock());
        m.dos.writeShort(p.lvDetail == null ? 0 : p.lvDetail.lv);
        m.dos.writeLong(p.lvDetail == null ? 0L : p.lvDetail.getExp());
        final List<int[]> binh = new ArrayList<int[]>();
        if (p.potions != null) {
            for (int i = 0; i < p.potions.length && i < 256; i++) {
                if (p.potions[i] > 0) {
                    binh.add(new int[]{i, p.potions[i]});
                }
            }
        }
        final int nb = Math.min(127, binh.size());
        m.dos.writeByte(nb);
        for (int i = 0; i < nb; i++) {
            m.dos.writeByte(binh.get(i)[0]);
            m.dos.writeInt(binh.get(i)[1]);
        }
        m.dos.writeShort(p.iItems == null ? 0 : p.iItems.size());
        m.dos.writeShort(soMac(p));
        if (BanClientNhanVat.ban(p) >= MoRongTuiGame.BAN_CLIENT) {
            MoRongTuiGame.ghiTrangThai(m.dos, p); // suc chua hanh trang + ve mo rong (noi them cuoi goi, chi client >= 908)
        }
        return m;
    }

    /**
     * Doc goi -105 (kiem thu / client tham chieu): {kieu, hp, maxhp, mp, maxmp, xu, luong, luongKhoa, cap, exp, soTui,
     * soMac, oDaMo, oToiDaVe, oMoiVe, giaXuBuocKe, soVe, idVe, loaiVe} + binh. Goi cu (thieu phan noi them): 7 truong
     * cuoi = -1.
     */
    public static long[] docTrangThai(final Message m, final int[] binhRa) throws IOException {
        final byte[] b = m.toByteArray();
        final int dau = (b.length > 0 && b[0] == (byte) -128) ? 6 : 3;
        final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, dau, b.length - dau));
        r.readByte();
        final long kieu = r.readByte();
        final long hp = r.readInt(), maxhp = r.readInt(), mp = r.readInt(), maxmp = r.readInt();
        final long xu = r.readLong();
        final long luong = r.readInt(), khoa = r.readInt();
        final long cap = r.readShort();
        final long exp = r.readLong();
        final int nb = r.readByte();
        for (int i = 0; i < nb; i++) {
            final int id = r.readUnsignedByte();
            final int so = r.readInt();
            if (binhRa != null && id < binhRa.length) {
                binhRa[id] = so;
            }
        }
        final long soTui = r.readShort(), soMac = r.readShort();
        long oDaMo = -1, oToiDaVe = -1, oMoiVe = -1, giaXu = -1, soVe = -1, idVe = -1, loaiVe = -1;
        if (r.available() > 0) {
            oDaMo = r.readShort();
            oToiDaVe = r.readShort();
            oMoiVe = r.readByte();
            giaXu = r.readInt();
            soVe = r.readInt();
            idVe = r.readShort();
            loaiVe = r.readByte();
        }
        return new long[]{kieu, hp, maxhp, mp, maxmp, xu, luong, khoa, cap, exp, soTui, soMac,
            oDaMo, oToiDaVe, oMoiVe, giaXu, soVe, idVe, loaiVe};
    }
}
