package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import real.Char;
import real.CharManager;
import real.Item;
import real.LevelDetail;
import real.Map;
import real.MessageCreator;
import server.TeamServer;

/**
 * LENH TU TRANG QUAN TRI WEB -> AP DUNG CHO NHAN VAT TRONG GAME.
 *
 * VI SAO CAN. Truoc 02/09/2026 trang web (web/admin/nhanvat.php) ghi THANG vao tob_char:
 * "Dat cap do"/"Buff level" ghi lastLv/xp, "Xoa vat pham" cat chuoi equip/inven/bag. Voi nhan
 * vat dang online thi vo ich: ban trong RAM cua tien trinh game van la ban cu, lan tu luu ke
 * tiep (dinh ky hoac luc thoat) ghi de lai dung dong vua sua. Rieng cap do con khong an ca khi
 * offline: luc dang nhap server doc cap/exp tu cot pInfo (truong 4 = exp, truong 5 = lastLV,
 * xem CharNapDuLieu.initPinfo), KHONG doc lastLv/xp - hai cot do chi phuc vu bang xep hang.
 * Web khong nhin thay RAM cua server, nen khong the tu xu ly.
 *
 * CACH LAM. Web KHONG ghi tob_char nua, chi chen mot dong "lenh" vao bang {@link #TEN_BANG}
 * (trang_thai = CHO). Luong nay doc bang moi giay va xu ly theo loai:
 *
 *   dat_cap_do (cap_do + phan_nghin)
 *     - online  -> cong exp con thieu qua Map.addXPForChar (y het nut "Buff EXP" cua AdminPanel:
 *                  co diem co ban, diem ky nang, goi tin len cap), saveCharAuto ngay. -> XONG_ONLINE
 *     - offline -> KHONG dong CSDL (vo dung, xem tren). Chuyen sang CHO_DANG_NHAP; moi vong doc
 *                  lai, he thay nhan vat vao game thi cong nhu tren.
 *     - chi NANG cap: exp moi phai lon hon exp hien tai (xem duoi).
 *
 *   xoa_vat_pham (tham_so = "<cot>><mau>", cot = equip | inven | bag, mau = chuoi tho cua mon
 *   do dung nhu Item.getInfoSave() ghi xuong cot)
 *     - online  -> quet cac kho ung voi cot, go mon co getInfoSave() khop TUNG KY TU, gui lai
 *                  tui/do mac cho client, saveCharAuto ngay. Khong khop (mon da doi tu lan luu
 *                  cuoi) -> LOI, khong doan mo.                                  -> XONG_ONLINE
 *     - offline -> xoa mau khoi cot trong CSDL (dang nhap doc cot nay, xem
 *                  CharTrangBi.setitemInvenBag) roi de CHO_DANG_NHAP: khi nhan vat vao game,
 *                  doi chieu RAM mot lan nua - neu mon van con (nhan vat duoc nap dung giua luc
 *                  web sua) thi go tiep. Khong con -> xong.
 *
 *   loi (sai cap, khong co nhan vat, ha cap, mon khong khop...) -> LOI, ket_qua ghi ly do.
 *
 * Web doi toi da vai giay roi doc trang_thai de bao lai cho nguoi bam. Server dang tat thi dong
 * lenh nam cho, luc khoi dong lai se xu ly - khong bao gio mat lenh. Web co the HUY mot dong con
 * cho (trang_thai HUY), server bo qua.
 *
 * CHI NANG CAP, KHONG HA CAP. Ha cap trong RAM (nhu lenh chat "slv") de lai diem co ban / diem
 * ky nang vuot muc cua cap moi; chot chong gian lan luc dang nhap (CharNapDuLieu ~1184-1236) se
 * xoa diem va ky nang cua nguoi choi, con neu khong bi xoa thi len lai cap cu duoc cong diem LAN
 * HAI. Ca hai deu te hon la tu choi ro rang.
 *
 * LUU BANG saveCharAuto, KHONG DUNG saveChar. saveChar la ban luu luc THOAT: no
 * p.animal.remove(0) / p.pet.remove(0) trong luc luu - goi cho nhan vat dang choi la rut sach
 * thu cuoi va linh thu khoi RAM. saveCharAuto la ban tu luu dinh ky, duyet ma khong rut.
 *
 * TIM NHAN VAT ONLINE: quet CharManager.vChars theo charDBID VA session != null. Khong dung
 * getCharByCharDbID() vi linh thue (charHire) mang isBot = -1 va co the trung charDBID voi chu
 * nhan nhung session null - lay nham no la tuong nhan vat "offline" trong khi chu nhan dang choi.
 */
public final class LenhWeb implements Runnable {

    public static final String TEN_BANG = "web_lenh_nhan_vat";

    public static final String LOAI_DAT_CAP_DO = "dat_cap_do";
    public static final String LOAI_XOA_VAT_PHAM = "xoa_vat_pham";

    /** Cot vat pham web duoc xoa. Ten cot ghep thang vao SQL nen PHAI qua danh sach nay. */
    public static final String[] COT_VAT_PHAM = {"equip", "inven", "bag"};

    /** Web vua chen, server chua doc. */
    public static final int CHO = 0;
    /** Da thuc hien cho nhan vat dang online va da luu. */
    public static final int XONG_ONLINE = 1;
    /**
     * Ma cu (truoc khi bo nhanh ghi CSDL offline cho cap do). Khong con duoc tao moi; giu de
     * dong cu trong bang van doc duoc ten trang thai.
     */
    public static final int XONG_OFFLINE = 2;
    /** Loi, xem ket_qua. */
    public static final int LOI = 3;
    /** Nhan vat offline: nam cho, server ap dung / doi chieu ngay khi thay nhan vat vao game. */
    public static final int CHO_DANG_NHAP = 4;
    /** Nguoi quan tri huy tren web. Server bo qua. */
    public static final int HUY = 5;

    /** Doc bang moi giay: web dang doi ket qua, khong de nguoi bam cho lau. */
    public static final long CHU_KY_MS = 1000L;

    /** Moi vong xu ly toi da bay nhieu dong, tranh mot loat lenh chan vong lap. */
    static final int MOI_VONG_TOI_DA = 50;

    /**
     * Cap cao nhat cho phep = expMain.length - 1 (= 90). LevelDetail.getXpFromLevel() chi dung
     * khi level < expMain.length, va web (CAPDO_TOI_DA) cung dung tran nay.
     */
    public static final int CAP_TOI_DA = LevelDetail.expMain.length - 1;

    /**
     * Nguon ghi kem khi cong exp. PHAI chua "adminpanel": MapXuLyLenh.addXPForChar chi bo
     * chia 3 (lv >= 60) va bo random khi pos chua chuoi nay.
     */
    public static final String NGUON = "adminpanel-web";

    private static volatile boolean daChay = false;
    private static boolean daBaoLoi = false;

    /** Ket qua xu ly mot dong lenh. */
    public static final class KetQua {
        public final int trangThai;
        public final String moTa;

        public KetQua(final int trangThai, final String moTa) {
            this.trangThai = trangThai;
            this.moTa = moTa == null ? "" : moTa;
        }
    }

    /** Goi mot lan luc khoi dong server. Goi lai khong tao them thread. */
    public static synchronized void batDau() {
        if (daChay) {
            return;
        }
        daChay = true;
        final Thread t = new Thread(new LenhWeb());
        t.setName("LENH WEB");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        taoBang();
        while (TeamServer.running) {
            try {
                xuLyMotVong();
            } catch (final Exception e) {
                baoLoiMotLan(e);
            }
            try {
                Thread.sleep(CHU_KY_MS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // ------------------------------------------------------------------ tinh toan thuan

    /** Kep % vao cap (don vi phan nghin, cung don vi LevelDetail.percent) ve 0..999. */
    public static int kepPhanNghin(final int phanNghin) {
        if (phanNghin < 0) {
            return 0;
        }
        return phanNghin > 999 ? 999 : phanNghin;
    }

    /**
     * Tong exp ung voi "cap X, da vao Y phan nghin cua cap do". Cung cong thuc voi
     * LevelDetail.mapExp2NewExp va ButtonEditor.calculateExpToBuff:
     * expMain[cap - 1] + expRequire[cap] * phanNghin / 1000.
     * Cap ngoai 1..CAP_TOI_DA duoc kep vao bien, khong nem loi (nguoi goi da kiem truoc).
     */
    public static long tinhExp(int capDo, final int phanNghin) {
        if (capDo < 1) {
            capDo = 1;
        }
        if (capDo > CAP_TOI_DA) {
            capDo = CAP_TOI_DA;
        }
        return LevelDetail.expMain[capDo - 1]
                + LevelDetail.expRequire[capDo] * (long) kepPhanNghin(phanNghin) / 1000L;
    }

    /** Nhan vat that dang trong game: khong phai bot, va con socket. */
    public static boolean laNguoiChoiThat(final Char p) {
        return p != null && p.isBot == -1 && p.getSession() != null;
    }

    /**
     * Nhan vat dang online co tob_char.id nay, hoac null. Quet tay thay cho
     * CharManager.getCharByCharDbID (xem chu thich dau lop ve linh thue).
     */
    public static Char timNguoiChoiOnline(final int charId) {
        try {
            final Vector<Char> ds = CharManager.instance.vChars;
            for (int i = ds.size() - 1; i >= 0; --i) {
                try {
                    final Char c = ds.get(i);
                    if (c != null && c.charDBID == charId && laNguoiChoiThat(c)) {
                        return c;
                    }
                } catch (final Exception ignored) {
                }
            }
        } catch (final Exception ignored) {
        }
        return null;
    }

    public static boolean laCotVatPham(final String cot) {
        for (final String c : COT_VAT_PHAM) {
            if (c.equals(cot)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tach tham so "<cot>><mau>" cua lenh xoa vat pham. ">" khong bao gio nam trong mot mau
     * (no la dau ngan cach cac mau trong cot) nen dau ">" DAU TIEN la ranh gioi.
     * Tra ve {cot, mau}; sai dinh dang -> null.
     */
    public static String[] tachThamSo(final String thamSo) {
        if (thamSo == null) {
            return null;
        }
        final int vt = thamSo.indexOf('>');
        if (vt <= 0) {
            return null;
        }
        final String cot = thamSo.substring(0, vt);
        final String mau = thamSo.substring(vt + 1);
        if (!laCotVatPham(cot) || mau.trim().isEmpty()) {
            return null;
        }
        return new String[]{cot, mau};
    }

    /**
     * Xoa DUNG MOT mau (khop tung ky tu) khoi chuoi cot equip/inven/bag. Giu nguyen moi mau khac
     * ke ca phan "thoi trang dang mac" o cuoi inven. Dau ">" dau chuoi (neu co) bi bo nhu
     * CharTrangBi.setitemInvenBag van lam. Khong tim thay -> null.
     */
    public static String xoaMauKhoiChuoi(String chuoi, final String mau) {
        if (chuoi == null || mau == null) {
            return null;
        }
        chuoi = chuoi.trim();
        if (chuoi.startsWith(">")) {
            chuoi = chuoi.substring(1);
        }
        if (chuoi.isEmpty()) {
            return null;
        }
        final String[] cac = chuoi.split(">", -1);
        int vt = -1;
        for (int i = 0; i < cac.length; i++) {
            if (cac[i].equals(mau)) {
                vt = i;
                break;
            }
        }
        if (vt < 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cac.length; i++) {
            if (i == vt) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('>');
            }
            sb.append(cac[i]);
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ xu ly mot lenh

    /**
     * Xu ly mot dong lenh dang o trang thai {@code trangThaiCu} (CHO hoac CHO_DANG_NHAP).
     * Tra ve trang thai moi + mo ta; trang thai khong doi va mo ta rong = chua co gi moi.
     * Nem loi khi du lieu sai -> nguoi goi ghi LOI.
     */
    public static KetQua xuLy(final String loai, final int charId, final int capDo,
            final int phanNghin, final String thamSo, final String adminUser, final int trangThaiCu)
            throws Exception {
        if (LOAI_DAT_CAP_DO.equals(loai)) {
            return xuLyDatCapDo(charId, capDo, phanNghin, adminUser, trangThaiCu);
        }
        if (LOAI_XOA_VAT_PHAM.equals(loai)) {
            return xuLyXoaVatPham(charId, thamSo, adminUser, trangThaiCu);
        }
        throw new IllegalArgumentException("khong hieu loai lenh \"" + loai + "\"");
    }

    static KetQua xuLyDatCapDo(final int charId, final int capDo, final int phanNghin,
            final String adminUser, final int trangThaiCu) throws Exception {
        if (capDo < 1 || capDo > CAP_TOI_DA) {
            throw new IllegalArgumentException("cap do " + capDo + " ngoai khoang 1.." + CAP_TOI_DA);
        }
        final int pn = kepPhanNghin(phanNghin);

        final Char p = timNguoiChoiOnline(charId);
        if (p != null) {
            return new KetQua(XONG_ONLINE, apDungOnline(p, capDo, pn, adminUser));
        }
        if (trangThaiCu == CHO_DANG_NHAP) {
            return new KetQua(CHO_DANG_NHAP, ""); // van chua vao game
        }
        if (!coNhanVat(charId)) {
            throw new IllegalStateException("khong co nhan vat id " + charId + " trong tob_char");
        }
        return new KetQua(CHO_DANG_NHAP, "offline: se cong ngay khi nhan vat vao game");
    }

    /**
     * Ap dung cap do cho nhan vat dang online roi luu ngay. Tra ve mo ta ngan de ghi ket_qua.
     * Nem IllegalStateException neu khong phai nang cap (xem chu thich dau lop).
     */
    public static String apDungOnline(final Char p, final int capDo, final int phanNghin,
            final String adminUser) throws Exception {
        final long expMoi = tinhExp(capDo, phanNghin);
        final int lvCu = p.lvDetail.lv;
        final int pcCu = p.lvDetail.percent;
        final long expCu = p.lvDetail.getExp();
        if (expMoi <= expCu) {
            throw new IllegalStateException("chi ho tro NANG cap: nhan vat dang lv " + lvCu + " ("
                    + (pcCu / 10.0) + "%), muc yeu cau lv " + capDo + " (" + (phanNghin / 10.0)
                    + "%) khong cao hon");
        }
        // Duong len cap tu nhien: cong diem, gui goi 33/32, calculateAttrib, hoi day hp/mp.
        Map.addXPForChar(p, expMoi - expCu, true, NGUON + ":" + adminUser);
        // Luu ngay bang ban tu luu dinh ky: lan tu luu ke tiep ghi lai dung ban nay.
        Database.instance.saveCharAuto(p);
        baoChoNguoiChoi(p, "Quản trị viên đã đặt cấp độ của bạn thành " + p.lvDetail.lv);
        ghiLog(p, adminUser + " (web) dat cap " + capDo + " " + (phanNghin / 10.0) + "%: lv " + lvCu
                + " -> " + p.lvDetail.lv);
        return "online: lv " + lvCu + " (" + (pcCu / 10.0) + "%) -> lv " + p.lvDetail.lv + " ("
                + (p.lvDetail.percent / 10.0) + "%)";
    }

    static KetQua xuLyXoaVatPham(final int charId, final String thamSo, final String adminUser,
            final int trangThaiCu) throws Exception {
        final String[] ts = tachThamSo(thamSo);
        if (ts == null) {
            throw new IllegalArgumentException("tham so xoa vat pham sai dinh dang (can \"<cot>><mau>\", cot equip/inven/bag)");
        }
        final String cot = ts[0];
        final String mau = ts[1];

        final Char p = timNguoiChoiOnline(charId);
        if (p != null) {
            final boolean daGo = goVatPhamOnline(p, cot, mau, adminUser);
            if (daGo) {
                return new KetQua(XONG_ONLINE, "online: da go mon khoi " + cot + " va luu");
            }
            if (trangThaiCu == CHO_DANG_NHAP) {
                // Da xoa trong CSDL luc offline; nhan vat vao game voi ban khong con mon do.
                return new KetQua(XONG_ONLINE, "da xoa trong CSDL luc offline, xac nhan khi vao game: khong con mon do");
            }
            throw new IllegalStateException("khong thay mon khop trong " + cot
                    + " cua nhan vat dang online (mon da doi tu lan luu cuoi?) - tai lai trang roi bam lai");
        }
        if (trangThaiCu == CHO_DANG_NHAP) {
            return new KetQua(CHO_DANG_NHAP, ""); // van chua vao game
        }
        final int kq = xoaVatPhamOffline(charId, cot, mau);
        if (kq == -2) {
            throw new IllegalStateException("khong co nhan vat id " + charId + " trong tob_char");
        }
        if (kq == -1) {
            throw new IllegalStateException("khong thay mon khop trong cot " + cot + " cua CSDL (da bi xoa hoac doi?)");
        }
        // Dong cua so dua: nhan vat co the vua duoc nap dung luc web sua -> doi chieu RAM khi vao game.
        return new KetQua(CHO_DANG_NHAP, "da xoa trong CSDL; se doi chieu lai khi nhan vat vao game");
    }

    /**
     * Go mon co getInfoSave() khop mau khoi kho ung voi cot, gui lai cho client va luu ngay.
     * Tra ve false neu khong co mon nao khop (khong dong gi).
     */
    public static boolean goVatPhamOnline(final Char p, final String cot, final String mau,
            final String adminUser) throws Exception {
        String tenMon = null;
        if ("equip".equals(cot)) {
            for (int slot = 0; slot < p.wItems.length && tenMon == null; slot++) {
                final Item[] ds = p.wItems[slot];
                if (ds == null) {
                    continue;
                }
                for (int i = 0; i < ds.length; i++) {
                    if (khop(ds[i], mau)) {
                        tenMon = ten(ds[i]);
                        ds[i] = null;
                        break;
                    }
                }
            }
            if (tenMon == null) {
                tenMon = goKhoiVector(p.awItems, mau);
            }
        } else if ("inven".equals(cot)) {
            if (khop(p.itemHoaKyLan, mau)) {
                tenMon = ten(p.itemHoaKyLan);
                p.itemHoaKyLan = null;
            } else if (khop(p.itemVukhiThoiTrang, mau)) {
                tenMon = ten(p.itemVukhiThoiTrang);
                p.itemVukhiThoiTrang = null;
            } else if (khop(p.itemThanKhi, mau)) {
                tenMon = ten(p.itemThanKhi);
                p.itemThanKhi = null;
            } else {
                tenMon = goKhoiVector(p.iItems, mau);
            }
        } else if ("bag".equals(cot)) {
            tenMon = goKhoiVector(p.bItem, mau);
        }
        if (tenMon == null) {
            return false;
        }
        // Tinh lai chi so va day lai tui / do mac / thong tin nhan vat cho client va nguoi gan.
        try {
            p.calculateAttrib();
        } catch (final Exception ignored) {
        }
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createCharWearingMessage(p, p));
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
            p.doSendProperties();
        } catch (final Exception ignored) {
        }
        Database.instance.saveCharAuto(p);
        baoChoNguoiChoi(p, "Quản trị viên đã thu hồi vật phẩm: " + tenMon);
        ghiLog(p, adminUser + " (web) xoa vat pham " + tenMon + " khoi " + cot + ": " + mau);
        return true;
    }

    static boolean khop(final Item it, final String mau) {
        try {
            return it != null && mau.equals(it.getInfoSave());
        } catch (final Exception e) {
            return false;
        }
    }

    static String ten(final Item it) {
        try {
            final String t = it.getTemplate().name;
            return t == null ? ("#" + it.tempateID) : t;
        } catch (final Exception e) {
            return "#" + it.tempateID;
        }
    }

    /** Go mon dau tien khop trong vector; tra ve ten mon, hoac null. */
    static String goKhoiVector(final Vector<Item> ds, final String mau) {
        if (ds == null) {
            return null;
        }
        synchronized (ds) {
            for (int i = 0; i < ds.size(); i++) {
                final Item it = ds.get(i);
                if (khop(it, mau)) {
                    ds.remove(i);
                    return ten(it);
                }
            }
        }
        return null;
    }

    /**
     * Nhan vat offline: xoa mau khoi cot trong CSDL (dang nhap doc cot nay).
     * Tra ve 1 = da xoa, -1 = khong thay mon khop, -2 = khong co nhan vat.
     */
    static int xoaVatPhamOffline(final int charId, final String cot, final String mau) throws Exception {
        if (!laCotVatPham(cot)) {
            throw new IllegalArgumentException("cot " + cot + " khong duoc phep");
        }
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("SELECT `" + cot + "` FROM tob_char WHERE id = ? LIMIT 1");
            ps.setInt(1, charId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return -2;
            }
            final String cu = rs.getString(1);
            dong(rs);
            dong(ps);
            rs = null;
            final String moi = xoaMauKhoiChuoi(cu, mau);
            if (moi == null) {
                return -1;
            }
            ps = conn.prepareStatement("UPDATE tob_char SET `" + cot + "` = ? WHERE id = ?");
            ps.setString(1, moi);
            ps.setInt(2, charId);
            return ps.executeUpdate() > 0 ? 1 : -2;
        } finally {
            dong(rs);
            dong(ps);
            tra(conn);
        }
    }

    /** tob_char co dong id nay khong (chi doc). */
    static boolean coNhanVat(final int charId) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("SELECT 1 FROM tob_char WHERE id = ? LIMIT 1");
            ps.setInt(1, charId);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            dong(rs);
            dong(ps);
            tra(conn);
        }
    }

    private static void baoChoNguoiChoi(final Char p, final String noiDung) {
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(noiDung, ""));
        } catch (final Exception ignored) {
        }
    }

    private static void ghiLog(final Char p, final String noiDung) {
        try {
            Database.instance.saveOrtherLog("", p.charname, noiDung, "adminset");
        } catch (final Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ bang lenh

    static void xuLyMotVong() throws Exception {
        final List<Object[]> ds = docLenhCho();
        for (final Object[] r : ds) {
            final long id = (Long) r[0];
            final String loai = (String) r[1];
            final int charId = (Integer) r[2];
            final String charname = (String) r[3];
            final int capDo = (Integer) r[4];
            final int phanNghin = (Integer) r[5];
            final String adminUser = (String) r[6];
            final int trangThaiCu = (Integer) r[7];
            final String thamSo = (String) r[8];
            KetQua kq;
            try {
                kq = xuLy(loai, charId, capDo, phanNghin, thamSo, adminUser, trangThaiCu);
            } catch (final Exception e) {
                kq = new KetQua(LOI, cat("loi: " + e.getMessage(), 250));
            }
            if (kq.trangThai == trangThaiCu && kq.moTa.isEmpty()) {
                continue; // dang cho dang nhap, chua co gi moi
            }
            ghiKetQua(id, trangThaiCu, kq);
            System.out.println("[LENH WEB] #" + id + " " + loai + " " + charname + " (id " + charId + ")"
                    + (LOAI_DAT_CAP_DO.equals(loai)
                            ? " cap " + capDo + " " + (kepPhanNghin(phanNghin) / 10.0) + "%"
                            : " " + cat(thamSo, 80))
                    + " boi " + adminUser + " -> " + kq.moTa);
        }
    }

    /** Cac dong dang CHO hoac CHO_DANG_NHAP, cu nhat truoc. */
    static List<Object[]> docLenhCho() throws Exception {
        final List<Object[]> ds = new ArrayList<Object[]>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("SELECT id, loai, char_id, charname, cap_do, phan_nghin, admin_user, trang_thai, tham_so FROM `"
                    + TEN_BANG + "` WHERE trang_thai IN (?, ?) ORDER BY id LIMIT " + MOI_VONG_TOI_DA);
            ps.setInt(1, CHO);
            ps.setInt(2, CHO_DANG_NHAP);
            rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(new Object[]{rs.getLong("id"), rs.getString("loai"), rs.getInt("char_id"),
                    rs.getString("charname"), rs.getInt("cap_do"), rs.getInt("phan_nghin"),
                    rs.getString("admin_user"), rs.getInt("trang_thai"), rs.getString("tham_so")});
            }
        } finally {
            dong(rs);
            dong(ps);
            tra(conn);
        }
        return ds;
    }

    /**
     * Ghi trang thai moi, CHI khi dong van con o trang thai cu (web co the vua HUY, hoac mot
     * vong khac vua ghi) - khong ghi de ket qua da co.
     */
    static void ghiKetQua(final long id, final int trangThaiCu, final KetQua kq) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("UPDATE `" + TEN_BANG
                    + "` SET trang_thai = ?, ket_qua = ?, xu_ly_luc = NOW() WHERE id = ? AND trang_thai = ?");
            ps.setInt(1, kq.trangThai);
            ps.setString(2, cat(kq.moTa, 255));
            ps.setLong(3, id);
            ps.setInt(4, trangThaiCu);
            ps.executeUpdate();
        } finally {
            dong(ps);
            tra(conn);
        }
    }

    /**
     * Tao bang neu chua co, va them cot tham_so neu bang duoc tao tu ban dau tien (chua co cot).
     * Cung dinh nghia voi sql/them_bang_web_lenh_nhan_vat.sql va web/admin/inc/lenh_nhan_vat.php
     * (web cung tu tao, de ai chay truoc cung duoc).
     */
    static void taoBang() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS `" + TEN_BANG + "` ("
                    + "`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "`loai` VARCHAR(30) NOT NULL,"
                    + "`char_id` INT NOT NULL,"
                    + "`charname` VARCHAR(50) NOT NULL DEFAULT '',"
                    + "`cap_do` INT NOT NULL DEFAULT 0,"
                    + "`phan_nghin` INT NOT NULL DEFAULT 0,"
                    + "`tham_so` TEXT NULL,"
                    + "`admin_user` VARCHAR(50) NOT NULL DEFAULT '',"
                    + "`trang_thai` TINYINT NOT NULL DEFAULT 0,"
                    + "`ket_qua` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "`tao_luc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "`xu_ly_luc` DATETIME NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "KEY `idx_trang_thai` (`trang_thai`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            damBaoCotThamSo(conn);
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            dong(st);
            tra(conn);
        }
    }

    /** Bang tao boi ban dau (02/09/2026) chua co cot tham_so -> them. Chi ALTER khi that su thieu. */
    static void damBaoCotThamSo(final Connection conn) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Statement st = null;
        try {
            ps = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.COLUMNS"
                    + " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'tham_so'");
            ps.setString(1, TEN_BANG);
            rs = ps.executeQuery();
            final boolean co = rs.next() && rs.getInt(1) > 0;
            if (!co) {
                st = conn.createStatement();
                st.execute("ALTER TABLE `" + TEN_BANG + "` ADD COLUMN `tham_so` TEXT NULL AFTER `phan_nghin`");
                System.out.println("LENH WEB: da them cot tham_so vao " + TEN_BANG);
            }
        } finally {
            dong(rs);
            dong(ps);
            dong(st);
        }
    }

    // ------------------------------------------------------------------ tien ich

    static String cat(final String s, final int toiDa) {
        if (s == null) {
            return "";
        }
        return s.length() <= toiDa ? s : s.substring(0, toiDa);
    }

    /** Chi in mot lan: thread nay chay mai, loi lap lai (vd MySQL tat) se ngap log. */
    private static synchronized void baoLoiMotLan(final Exception e) {
        if (daBaoLoi) {
            return;
        }
        daBaoLoi = true;
        System.out.println("LENH WEB: loi (chi bao mot lan) - " + e);
    }

    private static void dong(final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private static void dong(final Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (final Exception ignored) {
        }
    }

    /** Tra ket noi ve pool - dung mot lan, khong free(null) (xem GhiSoOnline). */
    private static void tra(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }

    private LenhWeb() {
    }
}
