package real;

import com.htbb.pokemon.ChongVatPham;
import com.htbb.pokemon.ChuyenLuyen;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangCapLauDai;
import com.htbb.pokemon.PhaKhoaLogic;
import com.htbb.pokemon.ThongSoLauDai;
import data.Database;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Muc "Chuyển luyện" o Machamp tho ren (NPC loai 2, goi 23 -> menu -30 muc {@link #VI_TRI_MUC_THO_REN}). Luat thuan +
 * gia: com.htbb.pokemon.ChuyenLuyen; thiet ke docs/thiet-ke-game.md "Chuyển luyện". Chi chay khi LauDai.bat() (dung
 * bang gia / da / chi so cua nang cap dai han) va khoa chuyen_luyen.bat = 1.
 *
 * Menu chu (client cu dung duoc, khong goi tin moi) qua NPC ao {@link #NPC_ID}:
 *   menu 0 = mon NGUON (+N, trong hanh trang) -> menu 1 = mon NHAN hop le -> menu 2 = xac nhan (dong ghi dung gia:
 *   "mat 1 cap" / "dung K Bua chuyen luyen" / Huy). Bam xac nhan thi tinh LAI moi thu trong MOT khoi Char.truNeuDu
 *   (KHOA_TIEN): mon con do, dieu kien con dung, gia / da / bua khong doi so voi luc hoi, du xu / da / bua - roi moi
 *   tru xu, da, bua va doi ca hai mon (chi so tinh lai bang NangCapLauDaiGame.doiChiSo, nhu nang cap that). Thieu mot
 *   thu thi khong doi gi.
 */
public final class ChuyenLuyenGame {

    /** NPC ao menu chuyen luyen (grep 05/11/2026: -530..-539 chua dung). */
    public static final int NPC_ID = -535;
    static final int MENU_NGUON = 0;
    static final int MENU_NHAN = 1;
    static final int MENU_XAC_NHAN = 2;
    /** Vi tri muc "Chuyển luyện" trong menu Machamp (RequestNPCInfoHandler case 2, sau "Phá khoá trang bị"). */
    public static final int VI_TRI_MUC_THO_REN = 3;
    public static final String TEN_MUC = "Chuyển luyện";
    static final int TOI_DA_DONG = 20;
    /** Da luyen kim dung duoc (moi vien = 1 "da" nhu nang cap dai han), thu tu tieu. */
    static final int[] DA_LUYEN_KIM = {8, 9, 10, 11, 66, 245};

    /** Lua chon o menu xac nhan. */
    static final int CHON_MAT_CAP = 0;
    static final int CHON_BUA = 1;
    static final int CHON_HUY = 2;

    /** Trang thai dang chon theo charDBID. */
    static final class Chon {
        short[] nguon = new short[0];
        short[] nhan = new short[0];
        short idNguon = -1;
        short idNhan = -1;
        int[] luaChon = new int[0];
        long[] xuHoi = new long[2];
        int[] daHoi = new int[2];
    }

    static final ConcurrentHashMap<Integer, Chon> CHON = new ConcurrentHashMap<Integer, Chon>();

    private ChuyenLuyenGame() {
    }

    // ------------------------------------------------------------------ cau hinh

    static int so(final String k, final int md, final int min, final int max) {
        long v;
        try {
            v = com.htbb.cauhinh.CauHinhTrungTam.so(k, md);
        } catch (final Throwable e) {
            v = md;
        }
        return (int) Math.max(min, Math.min(max, v));
    }

    public static boolean bat() {
        return LauDai.bat() && so(ChuyenLuyen.K_BAT, 1, 0, 1) == 1;
    }

    static int xuPhanTram() {
        return so(ChuyenLuyen.K_XU_PHAN_TRAM, ChuyenLuyen.XU_PHAN_TRAM_MAC_DINH, 0, 100);
    }

    static int daPhanTram() {
        return so(ChuyenLuyen.K_DA_PHAN_TRAM, ChuyenLuyen.DA_PHAN_TRAM_MAC_DINH, 0, 200);
    }

    // ------------------------------------------------------------------ mon

    static int capDo(final Item it) {
        final ItemTemplates t = it.getTemplate();
        return t != null ? t.level : it.level;
    }

    /** Thoi trang / vu khi thoi trang / than khi / canh / suu tap / trang tri / co han: co he thong rieng. */
    static boolean dacBiet(final Item it) {
        final ItemTemplates t = it.getTemplate();
        if (t == null) {
            return true;
        }
        try {
            return t.atb != null && t.atb.length > 9 && t.atb[9] > 0 || it.isVukhiThoiTrang() || it.isThanKhi() || it.isCanh()
                    || it.isManhSuuTap() || it.isTBSuuTap() || it.isCaiTrangSuuTap() || it.colorName == Item.COLOR_YELLOW
                    || it.timeLoan != 0L || it.timeExpireHkl != 0L;
        } catch (final Exception e) {
            return true;
        }
    }

    static ChuyenLuyen.Mon mon(final Item it) {
        if (it == null || it.getTemplate() == null) {
            return null;
        }
        return new ChuyenLuyen.Mon(it.id, it.getTemplate().type, capDo(it), it.plus, it.isSelling || it.prizeSell > 0, dacBiet(it));
    }

    static Item timMon(final Char p, final short id) {
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && it.id == id) {
                    return it;
                }
            }
        }
        return null;
    }

    static String ten(final Item it) {
        String t;
        try {
            t = it.getName();
        } catch (final Exception e) {
            t = "Vật phẩm";
        }
        return t + (it.plus > 0 ? " +" + it.plus : "");
    }

    static void bao(final Char p, final String s) {
        p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
    }

    static boolean dangGiaoDich(final Char p) {
        return p.userTrade != null && !p.userTrade.isEmpty();
    }

    // ------------------------------------------------------------------ gia

    /** Xu mot lan thu +1..+15 cua mon cap {@code capDo} (chi so 1..15), co he so buoc +11..+15 nhu nang cap that. */
    static long[] chiPhiMuc(final int capDo) {
        final long[] c = new long[16];
        final int heSo = BoNgocGame.heSoXuBacCao();
        for (int k = 1; k <= 15; k++) {
            c[k] = NangCapLauDai.chiPhi(capDo, k, heSo);
        }
        return c;
    }

    static long giaXu(final Item nhan, final int r) {
        return ChuyenLuyen.xu(chiPhiMuc(capDo(nhan)), r, xuPhanTram());
    }

    static int giaDa(final int r) {
        return ChuyenLuyen.da(ThongSoLauDai.DA_NC, r, daPhanTram());
    }

    /** {thuong, khoa} da luyen kim dang co. */
    static int[] demDa(final Char p) {
        int t = 0;
        int k = 0;
        for (final int id : DA_LUYEN_KIM) {
            if (id >= 0 && id < p.listGemitem.length) {
                t += Math.max(0, p.listGemitem[id]);
            }
            if (id >= 0 && id < p.listGemitemLock.length) {
                k += Math.max(0, p.listGemitemLock[id]);
            }
        }
        return new int[]{t, k};
    }

    /** {thuong, khoa} Bua chuyen luyen trong hanh trang (bo mon dang rao ban). */
    static int[] demBua(final Char p) {
        int t = 0;
        int k = 0;
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && it.tempateID == ChuyenLuyen.ID_BUA && !it.isSelling) {
                    if (it.lock != 0) {
                        k += ChongVatPham.so(it);
                    } else {
                        t += ChongVatPham.so(it);
                    }
                }
            }
        }
        return new int[]{t, k};
    }

    /** Tru {@code n} da, uu tien khoa neu {@code khoaTruoc}. @return true neu co da khoa bi tru. */
    static boolean truDa(final Char p, int n, final boolean khoaTruoc) {
        boolean dungKhoa = false;
        for (int luot = 0; luot < 2 && n > 0; luot++) {
            final boolean khoa = luot == 0 ? khoaTruoc : !khoaTruoc;
            for (final int id : DA_LUYEN_KIM) {
                if (n <= 0) {
                    break;
                }
                if (id < 0 || id >= p.listGemitem.length || id >= p.listGemitemLock.length) {
                    continue;
                }
                final int co = khoa ? p.listGemitemLock[id] : p.listGemitem[id];
                final int lay = Math.min(n, Math.max(0, co));
                if (lay > 0) {
                    p.delGem(id, lay, khoa);
                    n -= lay;
                    dungKhoa |= khoa;
                }
            }
        }
        return dungKhoa;
    }

    /** Tieu {@code n} Bua chuyen luyen, uu tien khoa neu {@code khoaTruoc}. @return true neu co bua khoa bi tieu. */
    static boolean truBua(final Char p, int n, final boolean khoaTruoc) {
        boolean dungKhoa = false;
        for (int luot = 0; luot < 2 && n > 0; luot++) {
            final boolean khoa = luot == 0 ? khoaTruoc : !khoaTruoc;
            while (n > 0) {
                Item chon = null;
                synchronized (p.iItems) {
                    for (final Item it : p.iItems) {
                        if (it != null && it.tempateID == ChuyenLuyen.ID_BUA && !it.isSelling && (it.lock != 0) == khoa) {
                            chon = it;
                            break;
                        }
                    }
                }
                if (chon == null || !ChongVatPham.tieuMot(p, chon)) {
                    break;
                }
                n--;
                dungKhoa |= khoa;
            }
        }
        return dungKhoa;
    }

    // ------------------------------------------------------------------ menu

    static Chon chon(final Char p) {
        Chon c = CHON.get(p.charDBID);
        if (c == null) {
            c = new Chon();
            CHON.put(p.charDBID, c);
        }
        return c;
    }

    /** Noi muc "Chuyển luyện" vao menu Machamp (luon o vi tri {@link #VI_TRI_MUC_THO_REN}, ngay sau Pha khoa). */
    public static String[] themMucThoRen(final String[] menu) {
        if (menu == null || menu.length != VI_TRI_MUC_THO_REN) {
            return menu;
        }
        final String[] m = java.util.Arrays.copyOf(menu, menu.length + 1);
        m[VI_TRI_MUC_THO_REN] = TEN_MUC;
        return m;
    }

    static boolean kiemNoi(final Char p) {
        if (!NpcTinhBanDo.oNoiDichVu(p, NpcTinhBanDo.NPC_THO_REN)) {
            bao(p, "Hãy đến gần Machamp thợ rèn để chuyển luyện.");
            return false;
        }
        if (!bat()) {
            bao(p, "Chuyển luyện đang tạm đóng.");
            return false;
        }
        if (dangGiaoDich(p)) {
            bao(p, "Đang giao dịch, hãy hoàn tất giao dịch trước.");
            return false;
        }
        return true;
    }

    /** Bam muc "Chuyển luyện" o Machamp: danh sach mon nguon. */
    public static void moMenu(final Char p) {
        if (p == null || !kiemNoi(p)) {
            return;
        }
        final List<Item> ds = new ArrayList<Item>();
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && ChuyenLuyen.lyDoNguon(mon(it)) == null) {
                    ds.add(it);
                }
            }
        }
        java.util.Collections.sort(ds, (a, b) -> b.plus - a.plus);
        if (ds.isEmpty()) {
            bao(p, "Hành trang không có trang bị nào đã nâng cấp để chuyển. Tháo món ra hành trang rồi quay lại."
                    + " Chuyển luyện: chuyển +N của một món sang món khác cùng loại ô, cấp bằng hoặc cao hơn.");
            return;
        }
        final Chon c = chon(p);
        final int n = Math.min(TOI_DA_DONG, ds.size());
        final String[] dong = new String[n];
        c.nguon = new short[n];
        for (int i = 0; i < n; i++) {
            final Item it = ds.get(i);
            dong[i] = "Chuyển từ " + ten(it) + " (cấp " + capDo(it) + ")";
            c.nguon[i] = it.id;
        }
        c.idNguon = -1;
        c.idNhan = -1;
        p.sendMessage(MessageCreator.createMsgMenu(dong, NPC_ID, MENU_NGUON));
    }

    /** MapDoMenu: idNpc == NPC_ID. */
    public static void doMenu(final Char p, final int idMenu, final int idOption) {
        if (p == null) {
            return;
        }
        final Chon c = chon(p);
        if (idMenu == MENU_NGUON) {
            if (idOption >= 0 && idOption < c.nguon.length) {
                chonNguon(p, c, c.nguon[idOption]);
            }
        } else if (idMenu == MENU_NHAN) {
            if (idOption >= 0 && idOption < c.nhan.length) {
                hoiXacNhan(p, c, c.idNguon, c.nhan[idOption]);
            }
        } else if (idMenu == MENU_XAC_NHAN) {
            if (idOption >= 0 && idOption < c.luaChon.length && c.luaChon[idOption] != CHON_HUY) {
                thucHien(p, c, c.luaChon[idOption] == CHON_BUA);
            } else {
                c.idNguon = -1;
                c.idNhan = -1;
            }
        }
    }

    static void chonNguon(final Char p, final Chon c, final short idNguon) {
        final Item a = timMon(p, idNguon);
        final String loi = ChuyenLuyen.lyDoNguon(mon(a));
        if (loi != null) {
            bao(p, loi);
            return;
        }
        final ChuyenLuyen.Mon ma = mon(a);
        final List<Item> ds = new ArrayList<Item>();
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                final ChuyenLuyen.Mon mb = mon(it);
                if (it != null && it != a && ChuyenLuyen.lyDo(ma, mb) == null
                        && (ChuyenLuyen.coLoi(ma, mb, false) || ChuyenLuyen.coLoi(ma, mb, true))) {
                    ds.add(it);
                }
            }
        }
        java.util.Collections.sort(ds, (x, y) -> capDo(y) - capDo(x));
        if (ds.isEmpty()) {
            bao(p, "Không có món nhận phù hợp cho " + ten(a) + ": cần món cùng loại ô trang bị, cấp từ " + capDo(a)
                    + " trở lên, đang thấp hơn +" + a.plus + ", nằm trong hành trang.");
            return;
        }
        final int n = Math.min(TOI_DA_DONG, ds.size());
        final String[] dong = new String[n];
        c.nhan = new short[n];
        for (int i = 0; i < n; i++) {
            final Item it = ds.get(i);
            dong[i] = "Nhận: " + ten(it) + " (cấp " + capDo(it) + ")";
            c.nhan[i] = it.id;
        }
        c.idNguon = idNguon;
        c.idNhan = -1;
        p.sendMessage(MessageCreator.createMsgMenu(dong, NPC_ID, MENU_NHAN));
    }

    static String moTaGia(final long xu, final int da) {
        return PhaKhoaLogic.dinhDang(xu) + " xu" + (da > 0 ? " + " + da + " đá luyện kim" : "");
    }

    static void hoiXacNhan(final Char p, final Chon c, final short idNguon, final short idNhan) {
        final Item a = timMon(p, idNguon);
        final Item b = timMon(p, idNhan);
        final String loi = ChuyenLuyen.lyDo(mon(a), mon(b));
        if (loi != null) {
            bao(p, loi);
            return;
        }
        final int n = a.plus;
        final List<String> dong = new ArrayList<String>();
        final List<Integer> lc = new ArrayList<Integer>();
        if (ChuyenLuyen.coLoi(mon(a), mon(b), false)) {
            final int r = ChuyenLuyen.capKetQua(n, false);
            c.xuHoi[0] = giaXu(b, r);
            c.daHoi[0] = giaDa(r);
            dong.add("Chuyển (mất 1 cấp): " + ten(b) + " thành +" + r + ", " + ten(a) + " về +0. Giá " + moTaGia(c.xuHoi[0], c.daHoi[0]));
            lc.add(Integer.valueOf(CHON_MAT_CAP));
        }
        if (ChuyenLuyen.coLoi(mon(a), mon(b), true)) {
            final int[] bua = demBua(p);
            c.xuHoi[1] = giaXu(b, n);
            c.daHoi[1] = giaDa(n);
            dong.add("Dùng " + ChuyenLuyen.soBua(n) + " Bùa chuyển luyện (có " + (bua[0] + bua[1]) + "): " + ten(b) + " thành +" + n
                    + ", không mất cấp. Giá " + moTaGia(c.xuHoi[1], c.daHoi[1]));
            lc.add(Integer.valueOf(CHON_BUA));
        }
        dong.add("Huỷ");
        lc.add(Integer.valueOf(CHON_HUY));
        c.idNguon = idNguon;
        c.idNhan = idNhan;
        c.luaChon = new int[lc.size()];
        for (int i = 0; i < c.luaChon.length; i++) {
            c.luaChon[i] = lc.get(i).intValue();
        }
        p.sendMessage(MessageCreator.createMsgMenu(dong.toArray(new String[0]), NPC_ID, MENU_XAC_NHAN));
    }

    static void thucHien(final Char p, final Chon c, final boolean coBua) {
        if (c.idNguon < 0 || c.idNhan < 0) {
            return;
        }
        final short idNguon = c.idNguon;
        final short idNhan = c.idNhan;
        c.idNguon = -1;
        c.idNhan = -1;
        if (!kiemNoi(p)) {
            return;
        }
        final String kq = chuyen(p, idNguon, idNhan, coBua, c.xuHoi[coBua ? 1 : 0], c.daHoi[coBua ? 1 : 0]);
        bao(p, kq);
        if (kq.startsWith("Chuyển luyện thành công")) {
            try {
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharGemItem(p));
                final Item b = timMon(p, idNhan);
                if (b != null) {
                    p.sendMessage(MessageCreator.createMsgItemInfo(b));
                }
            } catch (final Exception ignored) {
            }
            try {
                Database.instance.saveCharAuto(p);
            } catch (final Exception ignored) {
            }
        }
    }

    /**
     * Chuyen +N cua mon {@code idNguon} sang {@code idNhan} voi gia DA HOI. Tat ca hoac khong, trong MOT khoi
     * Char.truNeuDu: kiem lai mon / dieu kien / gia / du xu - da - bua, roi tru va doi ca hai mon.
     * @return thong bao cho nguoi choi ("Chuyển luyện thành công ..." khi xong)
     */
    public static String chuyen(final Char p, final short idNguon, final short idNhan, final boolean coBua,
            final long xuDaHoi, final int daDaHoi) {
        if (dangGiaoDich(p)) {
            return "Đang giao dịch, hãy hoàn tất giao dịch trước.";
        }
        HoSoLauDai h = null;
        try {
            h = LauDaiNhanVat.lay(p);
        } catch (final Exception ignored) {
        }
        final String[] loi = {null};
        final Object[] ra = new Object[9]; // a, b, n, r, xu, da, soBua, cu, khoa moi
        final boolean xong = p.truNeuDu(() -> {
            final Item a = timMon(p, idNguon);
            final Item b = timMon(p, idNhan);
            final ChuyenLuyen.Mon ma = mon(a);
            final ChuyenLuyen.Mon mb = mon(b);
            final String l = ChuyenLuyen.lyDo(ma, mb);
            if (l != null) {
                loi[0] = l;
                return false;
            }
            if (!ChuyenLuyen.coLoi(ma, mb, coBua)) {
                loi[0] = "Món nhận đã bằng hoặc cao hơn mức sẽ nhận, không cần chuyển.";
                return false;
            }
            final int n = a.plus;
            final int r = ChuyenLuyen.capKetQua(n, coBua);
            final long xu = giaXu(b, r);
            final int da = giaDa(r);
            final int soBua = coBua ? ChuyenLuyen.soBua(n) : 0;
            if (xu != xuDaHoi || da != daDaHoi) {
                loi[0] = "Giá chuyển luyện đã thay đổi, hãy chọn lại.";
                return false;
            }
            final int[] coDa = demDa(p);
            final int[] coBuaSo = demBua(p);
            if (p.xu < xu || coDa[0] + coDa[1] < da || coBuaSo[0] + coBuaSo[1] < soBua) {
                loi[0] = "Cần " + moTaGia(xu, da) + (soBua > 0 ? " + " + soBua + " Bùa chuyển luyện" : "") + ".";
                return false;
            }
            ra[0] = a;
            ra[1] = b;
            ra[2] = Integer.valueOf(n);
            ra[3] = Integer.valueOf(r);
            ra[4] = Long.valueOf(xu);
            ra[5] = Integer.valueOf(da);
            ra[6] = Integer.valueOf(soBua);
            ra[7] = Integer.valueOf(b.plus);
            return true;
        }, () -> {
            final Item a = (Item) ra[0];
            final Item b = (Item) ra[1];
            final int n = ((Integer) ra[2]).intValue();
            final int r = ((Integer) ra[3]).intValue();
            final long xu = ((Long) ra[4]).longValue();
            final int da = ((Integer) ra[5]).intValue();
            final int soBua = ((Integer) ra[6]).intValue();
            // Mon nhan se khoa vinh vien du sao (nguon khoa ly do khac / nhan da khoa vinh vien): tieu do khoa truoc.
            final int[] khoaSan = ChuyenLuyen.khoaSau(a.lock, a.daMac, b.lock, b.daMac, false);
            final boolean khoaTruoc = khoaSan[0] != 0 && khoaSan[1] == 0;
            if (xu > 0) {
                p.subXu(xu, true, "chuyen luyen +" + r);
            }
            boolean dungKhoa = da > 0 && truDa(p, da, khoaTruoc);
            dungKhoa |= soBua > 0 && truBua(p, soBua, khoaTruoc);
            NangCapLauDaiGame.doiChiSo(b, b.plus, r);
            b.plus = (byte) r;
            NangCapLauDaiGame.doiChiSo(a, n, 0);
            a.plus = 0;
            final int[] k = ChuyenLuyen.khoaSau(a.lock, a.daMac, b.lock, b.daMac, dungKhoa);
            ra[8] = Boolean.valueOf(b.lock == 0 && k[0] != 0 || b.daMac != 0 && k[1] == 0);
            b.lock = (byte) k[0];
            b.daMac = (byte) k[1];
        });
        if (!xong) {
            return loi[0] != null ? loi[0] : "Không chuyển luyện được.";
        }
        final Item a = (Item) ra[0];
        final Item b = (Item) ra[1];
        final int n = ((Integer) ra[2]).intValue();
        final int r = ((Integer) ra[3]).intValue();
        final long xu = ((Long) ra[4]).longValue();
        final int da = ((Integer) ra[5]).intValue();
        final int soBua = ((Integer) ra[6]).intValue();
        // pity cua ca hai mon ve 0 (muc dang thu da doi, khong mang pity cu sang muc moi)
        if (h != null) {
            h.datTruot(a.id, 0);
            h.datTruot(b.id, 0);
        }
        try {
            Database.instance.saveLogThongKeChiTieu("Chuyen luyen_xu", b.tempateID, 1, xu, "xu");
            if (da > 0) {
                Database.instance.saveLogThongKeChiTieu("Chuyen luyen_da", 8, da, 0L, "da");
            }
            if (soBua > 0) {
                Database.instance.saveLogThongKeChiTieu("Chuyen luyen_bua", ChuyenLuyen.ID_BUA, soBua, 0L, "item");
            }
            Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "chuyen luyen " + a.tempateID + "#" + a.id + " +" + n
                    + "->0 => " + b.tempateID + "#" + b.id + " +" + ra[7] + "->+" + r + " | " + xu + " xu " + da + " da " + soBua
                    + " bua | khoa nhan " + b.lock + "/" + b.daMac, "chuyenluyen");
        } catch (final Exception ignored) {
        }
        return "Chuyển luyện thành công: " + ten(b) + " (từ +" + ra[7] + "), " + ten(a) + " về +0. Trừ " + moTaGia(xu, da)
                + (soBua > 0 ? " + " + soBua + " Bùa chuyển luyện" : "") + "."
                + (Boolean.TRUE.equals(ra[8]) ? " Món nhận đã khoá theo món nguồn / nguyên liệu khoá." : "");
    }
}
