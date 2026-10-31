import io.Message;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import real.Char;
import real.DungVatPham;
import real.HanhDongVatPham;
import real.Item;

/**
 * 0.9.3 "trang thai tuc thi": chuoi thao tac nhu nguoi choi that tren MOT nhan vat ngoai server (session gia ghi goi
 * tin) - binh khi day HP, binh khi HP thap, mua xN, ban, mac, thao - sau MOI buoc:
 * <ul>
 *   <li>server gui NGAY goi trang thai -105 (HanhDongVatPham) va no BANG su that server (HP / PP / max, xu, luong,
 *   luong khoa, so binh tung loai, so mon trong tui / dang mac) - client ap goi nay vao kho TrangThaiNguoiChoi nen kho
 *   client = su that server (client_jar/tools/KiemTrangThai.java kiem phia client tren CUNG chuoi byte {@link #MAU_HEX});</li>
 *   <li>thong bao nhanh -104 DUNG LOAI: "Đã mua X ×N (−gia)", "Đã bán X (+gia)", "Đã dùng X: +N HP", "Đã trang bị",
 *   "Đã tháo", tu choi co ly do ("HP đã đầy") - byte kieu cuoi = MUA / BAN / DUNG / TRANG_BI / THAO;</li>
 *   <li>tu choi cung gui -105 (client tra lai binh da tru doan truoc).</li>
 * </ul>
 */
public final class KiemTrangThaiVatPham {

    private KiemTrangThaiVatPham() {
    }

    /**
     * Goi -105 mau (than goi, khong dau lenh) cua nhan vat co dinh {@link #mauNhanVat}: client KiemTrangThai doc dung
     * chuoi nay -&gt; hai ben khop dinh dang.
     */
    static final String MAU_HEX = "010300000190000003e8000000fa000001f400000000000004d2000000000000000000320000000000000000"
            + "020100000005040000000200000000";
    /**
     * 19/10/2026: phan NOI THEM cuoi goi -105 (real.MoRongTuiGame): short oDaMo 42, short oToiDaVe 126, byte oMoiVe 10,
     * int giaXuBuocKe 20000, int soVe 0, short idVe -1 (mau nguyen lieu ngoai server), byte loaiVe 1. Client cu doc het
     * MAU_HEX roi dung - phan nay bo qua.
     */
    static final String MAU_HEX_MO_RONG = "002a007e0a00004e2000000000ffff01";

    public static void chay() {
        Kiem.nhom("Trang thai tuc thi sau thao tac vat pham (HanhDongVatPham -105, -104 co kieu)");
        try {
            KiemDungVatPham.chuanBi();
            mau();
            chuoiThaoTac();
            daNoiDayDu();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            com.htbb.pokemon.LauDai.dat(false);
            KiemDungVatPham.traLai();
        }
    }

    static Char nhanVat() {
        final Char p = KiemDungVatPham.nhanVat();
        p.getSession().banClient = real.MoRongTuiGame.BAN_CLIENT; // >= 908: co phan noi them -105
        return p;
    }

    static Char mauNhanVat() {
        final Char p = nhanVat();
        p.hp = 400;
        p.maxhp = 1000;
        p.mp = 250;
        p.maxmp = 500;
        p.setxu(1234L);
        p.lvDetail.lv = 50;
        for (int i = 0; i < p.potions.length; i++) {
            p.potions[i] = 0;
        }
        p.potions[1] = 5;
        p.potions[4] = 2;
        return p;
    }

    static String hex(final byte[] b, final int tu) {
        final StringBuilder s = new StringBuilder();
        for (int i = tu; i < b.length; i++) {
            s.append(String.format("%02x", b[i] & 255));
        }
        return s.toString();
    }

    static int dau(final byte[] b) {
        return (b.length > 0 && b[0] == (byte) -128) ? 6 : 3;
    }

    /** Dinh dang goi -105 khop chuoi mau ma client kiem (luong / luong khoa / exp ngoai server = gia tri mac dinh). */
    static void mau() throws Exception {
        final Char p = mauNhanVat();
        // idVe phu thuoc Map.gemTemplate da nap chua (thu tu nhom kiem): co dinh "chua co ve" (-1) cho chuoi mau.
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        real.Map.gemTemplate = new real.GemTemplate[0];
        final Message m;
        try {
            m = HanhDongVatPham.taoTrangThai(p, HanhDongVatPham.DUNG);
        } finally {
            real.Map.gemTemplate = gemCu;
        }
        final byte[] b = m.toByteArray();
        final String h = hex(b, dau(b));
        // phan dau (kieu, HP, max, PP, max, xu) khong phu thuoc CSDL
        Kiem.bang("-105: lenh", Integer.valueOf(-105), Integer.valueOf(m.command));
        Kiem.bang("-105: goi khop mau client (KiemTrangThai doc cung chuoi)", MAU_HEX, h.substring(0, Math.min(h.length(), MAU_HEX.length())));
        Kiem.bang("-105: phan noi them (mo rong hanh trang)", MAU_HEX_MO_RONG, h.length() > MAU_HEX.length() ? h.substring(MAU_HEX.length()) : "");
        final int[] binh = new int[256];
        final long[] v = HanhDongVatPham.docTrangThai(m, binh);
        Kiem.bang("-105: doc lai HP", Long.valueOf(400), Long.valueOf(v[1]));
        Kiem.bang("-105: doc lai xu", Long.valueOf(1234), Long.valueOf(v[5]));
        Kiem.bang("-105: doc lai so binh HP", Integer.valueOf(5), Integer.valueOf(binh[1]));
        Kiem.bang("-105: doc lai so binh PP", Integer.valueOf(2), Integer.valueOf(binh[4]));
        Kiem.bang("-105: doc lai cap", Long.valueOf(50), Long.valueOf(v[8]));
        // ghi mau day du cho client kiem (cung may, cung luc chay bo kiem)
        try {
            Files.write(Paths.get(System.getProperty("java.io.tmpdir"), "htbb-mau-105.hex"), h.getBytes(StandardCharsets.US_ASCII));
        } catch (final Exception ignored) {
        }
    }

    /** Goi -105 cuoi cung trong GOI, null neu khong co. */
    static long[] trangThaiCuoi(final int[] binh) throws Exception {
        long[] kq = null;
        for (final Message m : KiemDungVatPham.GOI) {
            if (m.command == HanhDongVatPham.LENH_TRANG_THAI) {
                kq = HanhDongVatPham.docTrangThai(m, binh);
            }
        }
        return kq;
    }

    /** {loai, noi dung, kieu} cua -104 cuoi cung. */
    static String[] baoCuoi() throws Exception {
        String[] kq = null;
        for (final Message m : KiemDungVatPham.GOI) {
            if (m.command == DungVatPham.LENH) {
                final byte[] b = m.toByteArray();
                final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, dau(b), b.length - dau(b)));
                final int loai = r.readByte();
                final String s = r.readUTF();
                final int kieu = r.available() > 0 ? r.readByte() : -1;
                kq = new String[]{loai == 0 ? "OK" : loai == 1 ? "LOI" : "TIN", s, String.valueOf(kieu)};
            }
        }
        return kq;
    }

    /** Kho client (doc tu -105) = su that server. */
    static void soSanh(final String buoc, final Char p) throws Exception {
        final int[] binh = new int[256];
        final long[] v = trangThaiCuoi(binh);
        Kiem.dung(buoc + ": co goi trang thai -105 ngay", v != null);
        if (v == null) {
            return;
        }
        boolean ok = v[1] == p.hp && v[2] == p.maxhp && v[3] == p.mp && v[4] == p.maxmp && v[5] == p.getxu()
                && v[6] == p.getLuong() && v[7] == p.getLuongLock() && v[10] == p.iItems.size();
        for (int i = 0; i < p.potions.length && i < 256; i++) {
            ok &= binh[i] == p.potions[i];
        }
        Kiem.dung(buoc + ": kho client = server (hp " + v[1] + "/" + p.hp + ", xu " + v[5] + "/" + p.getxu() + ", tui " + v[10] + "/"
                + p.iItems.size() + ")", ok);
    }

    static Message goi(final int cmd, final byte[] than) {
        return new Message(cmd, than);
    }

    static Message g(final int cmd, final boolean laShort, final int v) {
        try {
            return KiemDungVatPham.goi(cmd, laShort, v);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] than(final Object... v) {
        try {
            return than0(v);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] than0(final Object... v) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream o = new DataOutputStream(bo);
        for (final Object x : v) {
            if (x instanceof Byte) {
                o.writeByte(((Byte) x).intValue());
            } else if (x instanceof Short) {
                o.writeShort(((Short) x).intValue());
            } else {
                o.writeInt(((Integer) x).intValue());
            }
        }
        return bo.toByteArray();
    }

    /** OfflineMap khong chay constructor (khong mo luong ban do) - chi can ham mua / ban. */
    static real.Map banDo() throws Exception {
        final Class<?> uc = Class.forName("sun.misc.Unsafe");
        final java.lang.reflect.Field f = uc.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        final Object u = f.get(null);
        return (real.Map) uc.getMethod("allocateInstance", Class.class).invoke(u, real.OfflineMap.class);
    }

    static void chuoiThaoTac() throws Exception {
        final Char p = nhanVat();
        final real.Map map = KiemDungVatPham.map;
        while (real.Map.potionTemplates2.size() < 7) {
            real.Map.potionTemplates2.add(new real.PotionTemplate2(10, 200));
        }
        if (p.coolDownPotion == null) {
            p.coolDownPotion = new java.util.Hashtable<Short, real.PotionUse>();
        }
        for (final short id : new short[]{1, 2, 3, 4, 5, 6, 21, 22, 23, 24, 93, 94, 95, 96, 126, 127}) {
            p.coolDownPotion.put(Short.valueOf(id), new real.PotionUse(id, 350));
        }
        // 1) binh HP khi day HP: tu choi co ly do, khong tru binh, VAN gui -105 (client tra lai binh da tru doan)
        p.potions[1] = 3;
        p.hp = p.maxhp;
        KiemDungVatPham.GOI.clear();
        HanhDongVatPham.boc(p, HanhDongVatPham.DUNG, () -> DungVatPham.dungThuoc(map, p, g(22, false, 1)));
        String[] b = baoCuoi();
        Kiem.dung("binh khi day HP: bao 'HP đã đầy' (" + chu(b) + ")", b != null && "LOI".equals(b[0]) && b[1].contains("HP đã đầy"));
        Kiem.dung("binh khi day HP: kieu = DUNG", b != null && String.valueOf(HanhDongVatPham.DUNG).equals(b[2]));
        Kiem.bang("binh khi day HP: khong tru binh", 3, p.potions[1]);
        soSanh("binh khi day HP", p);
        // 2) binh HP khi HP thap: HP len NGAY, thong bao DUNG "+N HP"
        p.hp = 300;
        KiemDungVatPham.GOI.clear();
        HanhDongVatPham.boc(p, HanhDongVatPham.DUNG, () -> DungVatPham.dungThuoc(map, p, g(22, false, 1)));
        b = baoCuoi();
        Kiem.dung("binh khi HP thap: HP tang (" + p.hp + ")", p.hp > 300);
        Kiem.dung("binh khi HP thap: bao 'Đã dùng ...: +N HP' kieu DUNG (" + chu(b) + ")", b != null && b[1].startsWith("Đã dùng") && b[1].contains("HP")
                && String.valueOf(HanhDongVatPham.DUNG).equals(b[2]));
        Kiem.bang("binh khi HP thap: tru 1 binh", 2, p.potions[1]);
        soSanh("binh khi HP thap", p);
        // 3) mua xN binh o cua hang NPC (OfflineMap.doBuyItem, lenh 24 loai 4): ham cu im lang -> "Đã mua X ×N (−gia)"
        final real.Map om = banDo();
        // 01/11/2026: goi 24 thuoc chi mua canh Chansey / Kecleon (NpcTinhBanDo.thuocBanTaiCho) - dung trong nha 100 (Chansey)
        final real.Map mapCu = p.map;
        final int mapIdCu = p.mapID;
        final real.Map nhaChansey = new real.Map();
        nhaChansey.mapId = 100;
        nhaChansey.mapIDLoadMap = -1;
        nhaChansey.isMapOffline = true;
        p.map = nhaChansey;
        p.mapID = 100;
        p.setxu(100000L);
        p.potions[2] = 0;
        KiemDungVatPham.GOI.clear();
        final long xuTruoc = p.getxu();
        HanhDongVatPham.boc(p, HanhDongVatPham.MUA, () -> om.doBuyItem(p, goi(24, than((byte) 1, (byte) 4, (short) 2, (short) 7))));
        b = baoCuoi();
        Kiem.bang("mua x7: so binh loai 2", 7, p.potions[2]);
        Kiem.dung("mua x7: tru xu (" + (xuTruoc - p.getxu()) + ")", p.getxu() < xuTruoc);
        Kiem.dung("mua x7: bao 'Đã mua ... ×7 (−... xu)' kieu MUA (" + chu(b) + ")", b != null && "OK".equals(b[0]) && b[1].startsWith("Đã mua")
                && b[1].contains("×7") && b[1].contains("−") && String.valueOf(HanhDongVatPham.MUA).equals(b[2]));
        Kiem.dung("mua x7: thong bao KHAC thong bao dung", b != null && !b[1].startsWith("Đã dùng"));
        Kiem.dung("mua x7: gui tui (16) ngay", KiemDungVatPham.coGoi(16));
        soSanh("mua x7", p);
        // mua khi khong du xu: bao ly do, -105 van gui (client tra lai xu da tru doan)
        p.setxu(0L);
        KiemDungVatPham.GOI.clear();
        HanhDongVatPham.boc(p, HanhDongVatPham.MUA, () -> om.doBuyItem(p, goi(24, than((byte) 1, (byte) 4, (short) 2, (short) 3))));
        b = baoCuoi();
        Kiem.dung("mua khi het xu: bao ly do (" + chu(b) + ")", b != null && "LOI".equals(b[0]) && b[1].startsWith("Không mua được"));
        Kiem.bang("mua khi het xu: khong them binh", 7, p.potions[2]);
        soSanh("mua khi het xu", p);
        // o thuoc da du 999: ham cu bao hop thoai OK -> client moi nhan thong bao nhanh LOI kieu MUA
        p.setxu(100000L);
        p.potions[2] = 999;
        KiemDungVatPham.GOI.clear();
        HanhDongVatPham.boc(p, HanhDongVatPham.MUA, () -> om.doBuyItem(p, goi(24, than((byte) 1, (byte) 4, (short) 2, (short) 3))));
        b = baoCuoi();
        Kiem.dung("mua khi o thuoc day: hop thoai cu -> -104 LOI kieu MUA (" + chu(b) + ")", b != null && "LOI".equals(b[0])
                && String.valueOf(HanhDongVatPham.MUA).equals(b[2]) && !KiemDungVatPham.coGoi(37));
        soSanh("mua khi o thuoc day", p);
        // dung trong nha Machamp 103: tu choi co ly do, khong tru, khong them binh
        nhaChansey.mapId = 103;
        p.mapID = 103;
        p.potions[2] = 7;
        KiemDungVatPham.GOI.clear();
        final long xuNha103 = p.getxu();
        HanhDongVatPham.boc(p, HanhDongVatPham.MUA, () -> om.doBuyItem(p, goi(24, than((byte) 1, (byte) 4, (short) 2, (short) 3))));
        b = baoCuoi();
        Kiem.dung("mua trong nha Machamp 103: bao chi ban o Chansey / Kecleon (" + chu(b) + ")", b != null && b[1].contains("Chansey"));
        Kiem.dung("mua trong nha 103: khong tru xu, khong them binh", p.getxu() == xuNha103 && p.potions[2] == 7);
        p.map = mapCu;
        p.mapID = mapIdCu;
        p.potions[2] = 7;
        // 4) ban mot mon (OfflineMap.doSellItem): "Đã bán X (+gia xu)"
        p.setxu(500L);
        final Item ban = KiemDungVatPham.cho(p, 1034);
        KiemDungVatPham.GOI.clear();
        HanhDongVatPham.boc(p, HanhDongVatPham.BAN, () -> om.doSellItem(p, goi(28, than(Short.valueOf(ban.id)))));
        b = baoCuoi();
        Kiem.bang("ban: roi tui", 0, KiemDungVatPham.dem(p, 1034));
        Kiem.dung("ban: bao 'Đã bán ...' kieu BAN (" + chu(b) + ")", b != null && b[1].startsWith("Đã bán") && String.valueOf(HanhDongVatPham.BAN).equals(b[2]));
        soSanh("ban", p);
        // 5) mac / 6) thao (lenh 29 -> DungVatPham.dung)
        final Item vc = KiemDungVatPham.cho(p, 126);
        KiemDungVatPham.giuMau(p);
        KiemDungVatPham.GOI.clear();
        HanhDongVatPham.boc(p, HanhDongVatPham.DUNG, () -> DungVatPham.dung(map, p, g(29, true, vc.id)));
        b = baoCuoi();
        Kiem.dung("mac: bao 'Đã trang bị' kieu TRANG_BI (" + chu(b) + ")", b != null && b[1].startsWith("Đã trang bị")
                && String.valueOf(HanhDongVatPham.TRANG_BI).equals(b[2]));
        soSanh("mac", p);
        final long[] sauMac = trangThaiCuoi(null);
        KiemDungVatPham.giuMau(p);   // ngoai server calculateAttrib dua maxhp ve 0 (nhu KiemDungVatPham.dung)
        KiemDungVatPham.GOI.clear();
        HanhDongVatPham.boc(p, HanhDongVatPham.DUNG, () -> DungVatPham.dung(map, p, g(29, true, vc.id)));
        b = baoCuoi();
        Kiem.dung("thao: bao 'Đã tháo' kieu THAO (" + chu(b) + ")", b != null && b[1].startsWith("Đã tháo") && String.valueOf(HanhDongVatPham.THAO).equals(b[2]));
        soSanh("thao", p);
        final long[] sauThao = trangThaiCuoi(null);
        Kiem.dung("mac roi thao: so mon dang mac giam 1 (" + (sauMac == null ? -1 : sauMac[11]) + " -> " + (sauThao == null ? -1 : sauThao[11]) + ")",
                sauMac != null && sauThao != null && sauThao[11] == sauMac[11] - 1);
        // client cu (0.9.2): khong gui -105 (goi la), thong bao -104 van doc duoc (byte kieu thua o cuoi)
        p.getSession().banClient = DungVatPham.BAN_TOAST;
        p.hp = p.maxhp;
        KiemDungVatPham.GOI.clear();
        HanhDongVatPham.boc(p, HanhDongVatPham.DUNG, () -> DungVatPham.dungThuoc(map, p, g(22, false, 1)));
        Kiem.dung("client 0.9.2: khong nhan goi -105", !KiemDungVatPham.coGoi(HanhDongVatPham.LENH_TRANG_THAI));
        Kiem.dung("client 0.9.2: -104 doc nhu cu (loai + noi dung)", KiemDungVatPham.baoCuoi() != null);
    }

    static String chu(final String[] b) {
        return b == null ? "(khong co thong bao)" : b[0] + ": " + b[1] + " [kieu " + b[2] + "]";
    }

    /** Ma nguon: moi lenh vat pham deu boc HanhDongVatPham (khong ai quen goi trang thai). */
    static void daNoiDayDu() throws Exception {
        final String x = new String(Files.readAllBytes(Paths.get("src/real/MapXuLyLenh.java")), StandardCharsets.UTF_8);
        for (final String[] c : new String[][]{{"24", "MUA"}, {"28", "BAN"}, {"61", "VUT"}, {"66", "GIAO_DICH"}, {"78", "BAN"}}) {
            Kiem.dung("lenh " + c[0] + " boc HanhDongVatPham." + c[1], x.matches("(?s).*case " + c[0] + ":\\s*HanhDongVatPham\\.boc\\(player, HanhDongVatPham\\." + c[1] + ".*"));
        }
        final String m = new String(Files.readAllBytes(Paths.get("src/real/Map.java")), StandardCharsets.UTF_8);
        Kiem.dung("lenh 22 / 29 boc HanhDongVatPham.DUNG", m.contains("HanhDongVatPham.boc(player, HanhDongVatPham.DUNG, () -> DungVatPham.dungThuoc(")
                && m.contains("HanhDongVatPham.boc(player, HanhDongVatPham.DUNG, () -> DungVatPham.dung("));
        final String c = new String(Files.readAllBytes(Paths.get("src/real/Char.java")), StandardCharsets.UTF_8);
        Kiem.dung("Char.sendMessage qua HanhDongVatPham.qua", c.contains("m = HanhDongVatPham.qua(this, m);"));
    }
}
