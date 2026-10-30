package real;

import com.htbb.log.Nhat;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.TuDong;
import com.htbb.pokemon.TuDongKeHoach;
import data.NghiVanGianLan;
import io.Message;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Noi TU DONG DANH chinh thuc (com.htbb.pokemon.TuDong) vao game. Thiet ke: docs/thiet-ke-game.md.
 *
 *   - Lenh -100 (client -> server, luong ban do qua MapXuLyLenh): byte loai 0 xin trang thai / 1 bat (+ short ban
 *     kinh xin, px) / 2 tat / 3 nhip (client gui ~20 giay mot lan de may chu cong gio va phat hien het gio).
 *   - Lenh -100 (server -> client): byte 0, byte bat, int giay con lai hom nay, int giay toi da hom nay, short ban
 *     kinh (px), byte co (TuDong.CO_*), UTF thong bao ("" = khong).
 *   - {@link #kiemLenh} chen o DAU MapXuLyLenh.processMessage: luc dang tu dong, lenh danh (9 / 106) / thuoc (22) /
 *     nhat (18 / 19) di qua nhip nguoi that; danh nguoi (6) = PvP -> tat. Lenh danh cua MOI nguoi choi duoc do
 *     nhip (chi ghi nghi van, khong chan).
 *   - {@link #khiPvp} (bi danh, MapDanhNhau) va {@link #khiGapBoss} (BossLauDai.ghiNhan) tat theo tu_dong.tat_khi_pvp.
 *   - Lenh -100 loai 4 (client 0.9.2+, khi co TuDong.CO_KE_HOACH): MAY CHU lap ke hoach buoc tiep
 *     (com.htbb.pokemon.TuDongKeHoach: muc tieu gan nhat theo duong di + dung khoang cap, chieu theo he / PP / hoi
 *     chieu, thuoc theo nguong, nhat do, rut lui khi HP thap, dung khi tui day) va tra goi -100 byte 1: byte hanh
 *     dong, short id, short so (chieu / loai thuoc / cat do), short x, short y, UTF trang thai. Client chi thuc hien.
 *   - Moi lan tu choi / dung: ngoai cau trong goi -100 con chat tren dau nhan vat (ca client cu lan moi deu thay ly
 *     do, ke ca khi Tien ich "Thong bao: tat" nuot hop thoai).
 *   - EXP / xu / do roi KHONG doi: tu dong ha quai qua dung duong thuong, chiu nang luong (NangLuong).
 *   - Phieu uy thac (treo OFFLINE, TangTocGame) tach rieng: thoat game la phien tu dong ket thuc.
 *   - "Ve treo N gio" ({@link #dungVeTreo}, tu VatPhamDungLauDai hieu_ung gio_treo:N): cong N gio vao KHO GIO TREO
 *     (TuDong.Phien.msKho -> HoSoLauDai a3), dung SAU khi het gio mien phi hom nay. Goi -100: "con lai" = mien phi con
 *     lai + kho, "toi da" = tran ngay + kho (client cu khong can sua).
 */
public final class TuDongGame {

    private TuDongGame() {
    }

    /** Phien theo DOI TUONG Char (dang nhap lai = Char moi = phien moi, tat san). */
    private static final WeakHashMap<Char, TuDong.Phien> PHIEN = new WeakHashMap<Char, TuDong.Phien>();
    /** Bo dem ngay khi KHONG co ho so (sv.lau_dai=0): charDBID -> {ngay, ms}. */
    private static final ConcurrentHashMap<Integer, long[]> NGAY_DU_PHONG = new ConcurrentHashMap<Integer, long[]>();

    // ---- chi so (trang thai may chu / trang quan tri) ----
    public static final AtomicLong SO_LAN_BAT = new AtomicLong();
    public static final AtomicLong SO_LAN_TU_CHOI = new AtomicLong();
    public static final AtomicLong SO_HANH_DONG_CHAN = new AtomicLong();
    public static final AtomicLong SO_TAT_VI_PHAM = new AtomicLong();
    public static final AtomicLong SO_NGHI_VAN_NHIP = new AtomicLong();
    public static final AtomicLong SO_KE_HOACH = new AtomicLong();

    static int homNay(final long bay) {
        return NangLuong.ngay(bay);
    }

    static Char laNguoi(final Char p) {
        return p != null && p.isBot == -1 && p.charDBID > 0 ? p : null;
    }

    static TuDong.Phien phien(final Char p, final boolean tao) {
        synchronized (PHIEN) {
            TuDong.Phien ph = PHIEN.get(p);
            if (ph == null && tao) {
                ph = new TuDong.Phien();
                final HoSoLauDai h = LauDaiNhanVat.lay(p);
                if (h != null) {
                    TuDong.napTuHoSo(ph, h);
                } else {
                    final long[] d = NGAY_DU_PHONG.get(Integer.valueOf(p.charDBID));
                    if (d != null) {
                        ph.ngay = (int) d[0];
                        ph.msDung = d[1];
                    }
                }
                PHIEN.put(p, ph);
            }
            return ph;
        }
    }

    static void ghiNgay(final Char p, final TuDong.Phien ph) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h != null) {
            TuDong.ghiHoSo(ph, h);
        } else {
            synchronized (ph) {
                NGAY_DU_PHONG.put(Integer.valueOf(p.charDBID), new long[]{ph.ngay, ph.msDung});
            }
        }
    }

    /** So phien dang chay (chi so). */
    public static int soDangChay() {
        int n = 0;
        synchronized (PHIEN) {
            for (final Entry<Char, TuDong.Phien> e : PHIEN.entrySet()) {
                if (e.getValue().bat) {
                    n++;
                }
            }
        }
        return n;
    }

    // ------------------------------------------------------------------ ngu canh

    /**
     * Ban do co Pokemon hoang da? Ban do chia vung (nRegion > 0, vd map 20) giu quai trong Region chu khong trong
     * Map.monsters - truoc day bi coi la "khong co quai" va tu choi bat sai.
     */
    static boolean coQuai(final Map m) {
        try {
            for (int i = 0; i < m.monsters.size(); i++) {
                final Hashtable<Short, Monster> h = m.monsters.get(i);
                if (h != null && !h.isEmpty()) {
                    return true;
                }
            }
            for (int r = 0; r < m.ALL_REGION.size(); r++) {
                final Region rg = (Region) m.ALL_REGION.get(r);
                for (int i = 0; rg != null && i < rg.monsters.size(); i++) {
                    final Hashtable<Short, Monster> h = rg.monsters.get(i);
                    if (h != null && !h.isEmpty()) {
                        return true;
                    }
                }
            }
        } catch (final Exception e) {
            // vector doi giua chung: coi nhu khong co
        }
        return false;
    }

    /** Khu PvP / su kien: san dau, giai Lien minh, chien truong, lanh dia doi, Gym, dau truong, cho loi dai, me cung, map boss. */
    static boolean laSanDau(final Char p, final Map m) {
        if (m instanceof MapLoiDai || m instanceof MapLienDau || m instanceof MapChienTruongMoba || m instanceof MapClan
                || m instanceof MapTown || m.inArena(p)) {
            return true;
        }
        try {
            return m.isMapChoLoiDai() || m.isvanTienTran() || m.inMapBoss(p);
        } catch (final Exception e) {
            return false;
        }
    }

    static TuDong.NguCanh nguCanh(final Char p) {
        final TuDong.NguCanh n = new TuDong.NguCanh();
        final Map m = p.map;
        n.chet = p.hp <= 0;
        n.vuKhiHong = vuKhiHong(p);
        n.pvp = p.isKiller;
        n.sanDau = m == null || laSanDau(p, m);
        n.baiLuyen = m != null && !(m instanceof OfflineMap) && !(m instanceof MapChauBau) && !n.sanDau && coQuai(m);
        n.theThang = TangTocGame.coTheThang(p);
        try {
            n.cap = p.getLevel();
        } catch (final Exception e) {
            n.cap = 0;
        }
        n.map = m == null ? -1 : m.mapId;
        n.khoaMap = m == null ? null : KhoaBanDoGame.lyDoKhongTuDong(m.mapId);
        n.x = p.x;
        n.y = p.y;
        return n;
    }

    // ------------------------------------------------------------------ goi tin

    static void guiTrangThai(final Char p, final TuDong.CauHinh c, final TuDong.Phien ph, final String thongBao) {
        try {
            final long bay = System.currentTimeMillis();
            final boolean the = TangTocGame.coTheThang(p);
            final Message m = new Message(TuDong.LENH);
            m.dos.writeByte(0);
            m.dos.writeByte(ph.bat ? 1 : 0);
            m.dos.writeInt(TuDong.giayConLai(c, ph, the, homNay(bay)));
            m.dos.writeInt(TuDong.giayToiDaHien(c, ph, the)); // tran ngay + kho gio treo (con lai <= toi da)
            m.dos.writeShort(ph.bat ? ph.banKinh : c.banKinh);
            m.dos.writeByte(c.co());
            m.dos.writeUTF(thongBao == null ? "" : thongBao);
            p.sendMessage(m);
        } catch (final Exception e) {
            // mat ket noi: bo qua
        }
    }

    /**
     * Bao ly do bang chu tren dau nhan vat (createMsgChat): khong phai hop thoai nen khong bi Tien ich "Thong bao:
     * tat" nuot va khong chong hai hop thoai voi client da tu hien cau trong goi -100.
     */
    static void bao(final Char p, final String s) {
        if (s == null || s.isEmpty()) {
            return;
        }
        try {
            p.sendMessage(MessageCreator.createMsgChat(p.id, s));
        } catch (final Exception e) {
            // mat ket noi
        }
    }

    /** Doc cai dat bang client moi (sau byte phien ban giao thuc) gui kem C_BAT. */
    static void docCaiDat(final TuDong.Phien ph, final java.io.DataInputStream dis) throws java.io.IOException {
        if (dis.available() < 1) {
            return;
        }
        final int gt = dis.readByte();
        synchronized (ph) {
            ph.giaoThuc = Math.max(1, gt);
            if (dis.available() >= 5) {
                ph.nguongHp = Math.max(0, Math.min(90, dis.readByte()));
                ph.nguongMp = Math.max(0, Math.min(90, dis.readByte()));
                ph.nhat = Math.max(0, Math.min(3, dis.readByte()));
                ph.veHoiPhuc = dis.readByte() != 0;
                ph.xinHoiSinhTraPhi = dis.readByte() != 0;
            }
        }
    }

    /**
     * 0.9.24: danh sach chieu da gan client moi gui kem C_KE (TuDong.DS_CHIEU): byte 1, byte co, byte n, n byte chieu.
     * Client cu (khong gui) giu nguyen chieuGan = null -> tinhHinh doc trang chieu trong ho so.
     */
    static void docChieuGan(final TuDong.Phien ph, final java.io.DataInputStream dis) throws java.io.IOException {
        if (dis.available() < 3 || dis.readByte() != TuDong.DS_CHIEU) {
            return;
        }
        final int co = dis.readByte();
        final int n = Math.max(0, Math.min(TuDong.DS_CHIEU_TOI_DA, dis.readUnsignedByte()));
        final int[] ds = new int[Math.min(n, dis.available())];
        for (int i = 0; i < ds.length; i++) {
            ds[i] = dis.readUnsignedByte();
        }
        synchronized (ph) {
            ph.chieuGan = ds;
            ph.thuTuNghiem = (co & 1) != 0;
        }
    }

    /**
     * Chieu danh tren trang chieu nguoi choi dang dung, doc tu ho so (client &gt;= 0.9.8 gui -103 loai 1 moi lan gan /
     * doi trang). null = may chu chua co du lieu trang chieu (sv.lau_dai tat / client chua gui).
     */
    static int[] chieuTrangHoSo(final Char p) {
        final HoSoLauDai h;
        try {
            h = LauDaiNhanVat.lay(p);
        } catch (final Exception e) {
            return null;
        }
        if (h == null) {
            return null;
        }
        final long a, b, c;
        final int trang;
        synchronized (h) {
            a = h.oNhanh1;
            b = h.oNhanh2;
            c = h.oNhanh3;
            trang = h.trangChieu;
        }
        if (a == 0L && b == 0L && c == 0L) {
            return null;
        }
        return TuDongKeHoach.chieuTrongTrang(trang == 1 ? b : trang == 2 ? c : a);
    }

    /**
     * Do cam tay dang mac hong (do ben 0) - cung dieu kien Char.checkDurableWeapone (mon cam tay type 3-7, 13; bo id
     * 773-787; nap >= 20000 duoc danh du hong) nhung KHONG doi gi (khong xoa do thue het han, khong gui goi).
     */
    static boolean vuKhiHong(final Char p) {
        try {
            if (p.luongNap >= 20000 || p.diemNapVip >= 20000) {
                return false;
            }
            final Item[] ds = p.wItems[p.slotWearing];
            for (final Item it : ds) {
                if (it == null || it.getTemplate() == null) {
                    continue;
                }
                final int t = it.getTemplate().type, id = it.getTemplate().id;
                if (id >= 773 && id <= 787 || !(t == 3 || t == 4 || t == 5 || t == 6 || t == 7 || t == 13)) {
                    continue;
                }
                if (it.getMinuteLoan(false) <= 0 && it.minuteExist > 0) {
                    continue; // do thue het han: checkDurableWeapone tu xoa + bao
                }
                if (it.mDurable == 0 || it.durable == 0) {
                    return true;
                }
            }
        } catch (final Exception e) {
            // trang bi doi giua chung
        }
        return false;
    }

    /** Lenh -100 (luong ban do). */
    static void xuLy(final Char p, final Message message) {
        if (laNguoi(p) == null) {
            return;
        }
        final TuDong.CauHinh c = TuDong.cauHinh();
        final TuDong.Phien ph = phien(p, true);
        final long bay = System.currentTimeMillis();
        int loai = TuDong.C_XIN;
        int banKinh = 0;
        boolean xinHoiSinh = false;
        int lyDoTat = TuDong.TAT_NGUOI_CHOI;
        try {
            loai = message.dis.readByte();
            if (loai == TuDong.C_BAT && message.dis.available() >= 2) {
                banKinh = message.dis.readShort();
            }
            if (loai == TuDong.C_BAT && message.dis.available() >= 1) {
                xinHoiSinh = message.dis.readByte() != 0;
            }
            if (loai == TuDong.C_BAT || loai == TuDong.C_XIN) {
                docCaiDat(ph, message.dis);
            }
            if (loai == TuDong.C_KE) {
                docChieuGan(ph, message.dis);
            }
            if (loai == TuDong.C_TAT && message.dis.available() >= 1) {
                final int l = message.dis.readByte();
                lyDoTat = l == TuDong.TAT_TU_DI || l == TuDong.TAT_HP_THAP ? l : TuDong.TAT_NGUOI_CHOI;
            }
        } catch (final Exception e) {
            // goi ngan: coi la xin trang thai
        }
        if (loai == TuDong.C_BAT) {
            final boolean dangBat = ph.bat;
            if (dangBat) {
                tich(p, c, ph, bay);
            }
            final TuDong.NguCanh n = nguCanh(p);
            final String camPvp = BanHuongGame.lyDoCamTuDong(p); // 0.9.8 ngam tay: PvP khong tu dong (moi cau hinh)
            final String loi = camPvp != null ? camPvp : TuDong.kiemBat(c, ph, n, homNay(bay));
            if (loi != null) {
                if (dangBat) {
                    tat(p, ph, TuDong.TAT_NGUOI_CHOI);
                }
                SO_LAN_TU_CHOI.incrementAndGet();
                guiTrangThai(p, c, ph, loi);
                bao(p, loi);
                return;
            }
            TuDong.bat(c, ph, n, banKinh, bay);
            synchronized (ph) {
                ph.xinHoiSinh = xinHoiSinh;
                ph.chetLuc = 0L;
            }
            SO_LAN_BAT.incrementAndGet();
            Nhat.kiemToan("TU_DONG", "bat " + p.charname + " map=" + n.map + " x=" + n.x + " y=" + n.y + " banKinh="
                    + ph.banKinh + " nguongHp=" + ph.nguongHp + " nguongMp=" + ph.nguongMp + " conLai=" + TuDong.giayConLai(c, ph, n.theThang, homNay(bay)) + "s");
            guiTrangThai(p, c, ph, "");
            return;
        }
        if (loai == TuDong.C_TAT) {
            if (ph.bat) {
                tich(p, c, ph, bay);
                tat(p, ph, lyDoTat);
            }
            final String tb = lyDoTat == TuDong.TAT_NGUOI_CHOI ? "" : TuDong.lyDo(lyDoTat);
            guiTrangThai(p, c, ph, tb);
            bao(p, tb);
            return;
        }
        if (loai == TuDong.C_KE) {
            keHoach(p, c, ph, bay);
            return;
        }
        if (ph.bat) {
            // nhip / xin trang thai luc dang chay: cong gio + kiem dieu kien
            if (kiemDangChay(p, c, ph, bay)) {
                return; // da tat + da gui
            }
            if (loai == TuDong.C_NHIP) {
                goiYClientCu(p, c, ph, bay);
            }
        }
        guiTrangThai(p, c, ph, ph.bat ? "" : ghiChuKho(c, ph, TangTocGame.coTheThang(p), bay));
    }

    /** Dong bao kho gio treo cho bang Tu dong (chi khi kho &gt; 0), "" neu kho rong. */
    static String ghiChuKho(final TuDong.CauHinh c, final TuDong.Phien ph, final boolean the, final long bay) {
        final long kho;
        synchronized (ph) {
            kho = ph.msKho;
        }
        if (kho <= 0L) {
            return "";
        }
        final int mp = TuDong.giayMienPhiConLai(c, ph, the, homNay(bay));
        return "Kho giờ treo: " + TuDong.gioPhut(kho) + (mp > 0 ? " (dùng sau khi hết " + TuDong.gioPhut(mp * 1000L)
                + " miễn phí hôm nay)." : " (đang dùng - đã hết giờ miễn phí hôm nay).");
    }

    /** So o hanh trang MOI can de nhan {@code n} ve {@code id} (0 neu don het vao chong san co) - ChongVatPham. */
    public static int soOCanVe(final Char p, final int id, final int n) {
        return com.htbb.pokemon.ChongVatPham.soOCan(p, id, n);
    }

    /**
     * Trao {@code n} ve treo KHOA (giftcode) qua he xep chong type 28 (com.htbb.pokemon.ChongVatPham.nhan): don vao
     * chong ve khoa san co, phan con lai thanh chong moi (toi da ChongVatPham.TRAN / chong) - MOT o cho ca 40 ve.
     * Noi goi da kiem o trong ({@link #soOCanVe}). @return so ve da trao.
     */
    public static int traoVeTreo(final Char p, final int id, final int n) {
        if (p == null || n <= 0) {
            return 0;
        }
        final Item mau = Char.mauSoLuong(id, p);
        if (mau == null) {
            return 0;
        }
        mau.id = p.getIDItem();
        mau.dateCreate = Char.getDayTime(0L);
        final int[] kq = com.htbb.pokemon.ChongVatPham.nhan(p, mau, n);
        if (kq[1] == 0) {
            p.removeIDItem(mau.id); // don het vao chong cu: tra id mon mau
        }
        return kq[0];
    }

    /**
     * Dung "Ve treo N gio" (VatPhamDungLauDai, hieu_ung gio_treo:N): kiem tran kho -&gt; tru 1 ve
     * (ChongVatPham.tieuMot) -&gt; cong N gio vao kho -&gt; ghi ho so (luu cung luot luu nhan vat voi hanh trang).
     * Can ho so dai han (sv.lau_dai=1): khong co thi tu choi, khong mat ve.
     */
    public static void dungVeTreo(final Char p, final Item item, final int gio) {
        if (laNguoi(p) == null || item == null) {
            return;
        }
        if (gio <= 0 || gio > TuDong.KHO_GIO_TOI_DA) {
            DungVatPham.loi(p, "Vé treo không hợp lệ.");
            return;
        }
        if (LauDaiNhanVat.lay(p) == null) {
            DungVatPham.loi(p, "Tạm thời không dùng được, thử lại sau.");
            return;
        }
        final TuDong.CauHinh c = TuDong.cauHinh();
        final TuDong.Phien ph = phien(p, true);
        final long bay = System.currentTimeMillis();
        final long ms = gio * 3600000L;
        if (ph.bat) {
            tich(p, c, ph, bay); // chot gio da chay truoc khi cong kho
        }
        final String loi = TuDong.kiemThemKho(ph, ms);
        if (loi != null) {
            DungVatPham.loi(p, loi);
            return;
        }
        // tru ve TRUOC (CODING_CONVENTION muc 5) - MOT duong tru: ChongVatPham.tieuMot (DungVatPham da tach 1 don vi
        // khoi chong truoc khi goi; goi thang voi chong thi giam 1)
        if (!com.htbb.pokemon.ChongVatPham.tieuMot(p, item)) {
            return;
        }
        final int conVe = com.htbb.pokemon.ChongVatPham.dem(p.iItems, item.getTemplate().id); // ca phan chong da tach
        final long kho = TuDong.themKho(ph, ms);
        ghiNgay(p, ph);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        try {
            data.Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "ve treo +" + gio + "h id"
                    + item.getTemplate().id + " dbid" + item.dbid + " kho=" + kho / 60000L + "p conVe=" + conVe, "vetreo");
        } catch (final Exception ignored) {
        }
        Nhat.kiemToan("TU_DONG", "ve treo " + p.charname + " +" + gio + "h kho=" + kho / 60000L + "p");
        final int mp = TuDong.giayMienPhiConLai(c, ph, TangTocGame.coTheThang(p), homNay(bay));
        DungVatPham.bao(p, "Đã dùng " + DungVatPham.ten(item) + ": +" + gio + " giờ Tự động đánh. Kho giờ treo: "
                + TuDong.gioPhut(kho) + " (tối đa " + TuDong.KHO_GIO_TOI_DA + " giờ), dùng sau khi hết "
                + (mp > 0 ? TuDong.gioPhut(mp * 1000L) + " miễn phí hôm nay" : "giờ miễn phí mỗi ngày")
                + (conVe > 0 ? ". Còn " + conVe + " vé." : "."));
        guiTrangThai(p, c, ph, ph.bat ? "" : ghiChuKho(c, ph, TangTocGame.coTheThang(p), bay));
    }

    /**
     * Client cu (0.9.1, tu chon muc tieu, khong xin ke hoach): nhip 20 giay ma KHONG co don danh nao thi bo nao may chu
     * xem vi sao (khong co Pokemon trong vung / manh hon qua nhieu / chi co boss / bi chan / HP thap) va bao bang chu
     * tren dau - thay vi dung im khong ly do.
     */
    static void goiYClientCu(final Char p, final TuDong.CauHinh c, final TuDong.Phien ph, final long bay) {
        final boolean imLang;
        synchronized (ph) {
            if (ph.giaoThuc >= TuDong.GIAO_THUC) {
                return;
            }
            imLang = ph.soDanhLanNhip >= 0 && ph.soDanh == ph.soDanhLanNhip;
            ph.soDanhLanNhip = ph.soDanh;
        }
        if (!imLang) {
            return;
        }
        try {
            final TuDongKeHoach.KeHoach k = TuDongKeHoach.lap(c.kh, tinhHinh(p, c, ph, bay));
            if ((k.hd == TuDongKeHoach.HD_CHO || k.hd == TuDongKeHoach.HD_VE || k.hd == TuDongKeHoach.HD_DUNG)
                    && TuDong.nenBao(ph, k.ly, bay)) {
                bao(p, "Tự động: " + k.ly);
            }
        } catch (final Exception e) {
            // goi y khong duoc thi thoi
        }
    }

    static void tich(final Char p, final TuDong.CauHinh c, final TuDong.Phien ph, final long bay) {
        TuDong.tich(c, ph, TangTocGame.coTheThang(p), bay, homNay(bay));
        ghiNgay(p, ph);
    }

    static void tat(final Char p, final TuDong.Phien ph, final int lyDo) {
        final long phut;
        synchronized (ph) {
            TuDong.tat(ph, lyDo);
            phut = (System.currentTimeMillis() - ph.batLuc) / 60000L;
        }
        ghiNgay(p, ph);
        if (lyDo == TuDong.TAT_VI_PHAM) {
            SO_TAT_VI_PHAM.incrementAndGet();
            NghiVanGianLan.ghiChung(p, "TU_DONG_NHIP", "tu dong vuot nhip " + ph.viPham + " lan (danh=" + ph.soDanh
                    + " thuoc=" + ph.soThuoc + " nhat=" + ph.soNhat + ")");
        }
        Nhat.kiemToan("TU_DONG", "tat " + p.charname + " lyDo=" + lyDo + " phut=" + phut + " danh=" + ph.soDanh
                + " thuoc=" + ph.soThuoc + " nhat=" + ph.soNhat + " chan=" + ph.soChan + " daDungHomNay="
                + (ph.msDung / 60000L) + "p");
    }

    /** Cong gio + kiem het gio / doi map / ra vung / san dau. @return true = vua tat (da bao client). */
    static boolean kiemDangChay(final Char p, final TuDong.CauHinh c, final TuDong.Phien ph, final long bay) {
        int lyDo = -1;
        if (!c.bat) {
            lyDo = TuDong.TAT_TAT_TINH_NANG;
        } else if (TuDong.tich(c, ph, TangTocGame.coTheThang(p), bay, homNay(bay))) {
            lyDo = TuDong.TAT_HET_GIO;
        } else if (p.hp <= 0 && hetChoHoiSinh(ph, bay)) {
            lyDo = TuDong.TAT_CHET;
        } else if (p.map == null || TuDong.raVung(ph, p.map.mapId, p.x, p.y)) {
            lyDo = p.map != null && p.map.mapId == ph.neoMap ? TuDong.TAT_RA_VUNG : TuDong.TAT_DOI_MAP;
        } else if (c.tatKhiPvp && (p.isKiller || laSanDau(p, p.map))) {
            lyDo = p.isKiller ? TuDong.TAT_PVP : TuDong.TAT_SAN_DAU;
        } else if (BanHuongGame.lyDoCamTuDong(p) != null) {
            lyDo = TuDong.TAT_PVP; // 0.9.8 ngam tay: trong tran giua nguoi choi luon dung
        } else if (c.mapBiCam(p.map.mapId)) {
            lyDo = TuDong.TAT_KHU_CAM;
        } else if (vuKhiHong(p)) {
            lyDo = TuDong.TAT_VU_KHI_HONG; // ca che do client tu chon (ke_hoach=0): may chu tu choi moi don
        }
        final String khoaMap = (lyDo < 0 || lyDo == TuDong.TAT_DOI_MAP) && p.map != null ? KhoaBanDoGame.lyDoKhongTuDong(p.map.mapId) : null;
        if (khoaMap != null) {
            lyDo = TuDong.TAT_KHU_CAM; // ban do khong cho tu dong (data_ban_do_mo_khoa): tat + bao ly do cua ban do
        }
        ghiNgay(p, ph);
        if (lyDo < 0) {
            return false;
        }
        if (khoaMap != null) {
            tat(p, ph, lyDo);
            guiTrangThai(p, c, ph, khoaMap);
            bao(p, khoaMap);
            return true;
        }
        tat(p, ph, lyDo);
        guiTrangThai(p, c, ph, TuDong.lyDo(lyDo));
        bao(p, TuDong.lyDo(lyDo));
        return true;
    }

    // ------------------------------------------------------------------ bo nao (ke hoach tung buoc)

    /** Khoang toi thieu giua hai lan lap ke hoach that cho mot nhan vat (xin day hon thi gui lai ke hoach cu). */
    static final long KE_TOI_THIEU_MS = 250L;

    /** C_KE: kiem dieu kien + lap ke hoach + gui client. */
    static void keHoach(final Char p, final TuDong.CauHinh c, final TuDong.Phien ph, final long bay) {
        synchronized (ph) {
            ph.giaoThuc = Math.max(ph.giaoThuc, TuDong.GIAO_THUC);
        }
        if (!ph.bat) {
            guiTrangThai(p, c, ph, "");
            return;
        }
        if (kiemDangChay(p, c, ph, bay)) {
            return; // da tat + da bao
        }
        if (!c.keHoach) {
            guiTrangThai(p, c, ph, ""); // may chu tat bo nao: client tu chon (co khong co CO_KE_HOACH)
            return;
        }
        TuDongKeHoach.KeHoach k;
        synchronized (ph) {
            k = ph.keCuoi != null && bay - ph.keLuc < KE_TOI_THIEU_MS ? ph.keCuoi : null;
        }
        if (k == null) {
            final TuDongKeHoach.TinhHinh t = tinhHinh(p, c, ph, bay);
            k = TuDongKeHoach.lap(c.kh, t);
            SO_KE_HOACH.incrementAndGet();
            synchronized (ph) {
                ph.keCuoi = k;
                ph.keLuc = bay;
                ph.soKe++;
                ph.dangHoiPhuc = k.hoiPhuc;
                ph.mucTieu = k.hd == TuDongKeHoach.HD_DANH ? k.id : -1;
                if (k.hd == TuDongKeHoach.HD_THUOC) {
                    ph.thuocLuc = bay;
                }
            }
            if (k.hd == TuDongKeHoach.HD_DUNG) {
                tat(p, ph, k.lyDoTat);
                guiTrangThai(p, c, ph, k.ly);
                bao(p, k.ly);
                return;
            }
            // chi bao khi DUNG CHO co ly do (khong co muc tieu hop le, PK gan, nghi hoi phuc) - khong bao luc dang di
            if ((k.hd == TuDongKeHoach.HD_CHO || k.hd == TuDongKeHoach.HD_VE && k.hoiPhuc) && TuDong.nenBao(ph, k.ly, bay)) {
                bao(p, "Tự động: " + k.ly);
            }
        }
        guiKeHoach(p, k);
        dongBoHpPp(p, ph, bay);
    }

    /** Khoang gui lai HP / PP that cho client luc dang tu dong (ms). */
    static final long DONG_BO_MS = 3000L;

    /**
     * 0.9.22 (chu game: "het PP khong tu su dung khi dang dung auto"): client tu tru PP moi lan bam chieu (ca khi
     * may chu tu choi don: quai vua chet, ngoai tam, hoi chieu) va chi nhan PP that khi co hoi PP / uong thuoc - nhan
     * vat khong hoi PP thi PP tren HUD troi xuong 1/3769 trong khi may chu con nhieu, bo nao khong cho uong ma client
     * lai tu choi chieu "khong du PP" -> dung im. Gui goi 60 (HP / max / PP that, chi cho chinh minh) moi 3 giay.
     */
    static void dongBoHpPp(final Char p, final TuDong.Phien ph, final long bay) {
        synchronized (ph) {
            if (bay - ph.dongBoLuc < DONG_BO_MS) {
                return;
            }
            ph.dongBoLuc = bay;
        }
        try {
            p.sendMessage(MessageCreator.createNew_HMP_Message(p, 0));
        } catch (final Exception e) {
            // mat ket noi
        }
    }

    static void guiKeHoach(final Char p, final TuDongKeHoach.KeHoach k) {
        try {
            final Message m = new Message(TuDong.LENH);
            m.dos.writeByte(TuDong.S_KE_HOACH);
            m.dos.writeByte(k.hd);
            m.dos.writeShort(k.id);
            m.dos.writeShort(k.so);
            m.dos.writeShort(k.x);
            m.dos.writeShort(k.y);
            m.dos.writeUTF(k.ly == null ? "" : k.ly);
            p.sendMessage(m);
        } catch (final Exception e) {
            // mat ket noi
        }
    }

    /** Loai skill buff (khong dung de danh) theo dong - khop client TuDong.SKILL_BUFF. */
    static final byte[][] SKILL_BUFF = {{4, 5}, {4, 5}, {4, 5, 6, 7}, {4, 5}, {4, 5}};

    static boolean laBuff(final int clazz, final int s) {
        final byte[] b = SKILL_BUFF[Math.min(Math.max(0, clazz), SKILL_BUFF.length - 1)];
        for (final byte x : b) {
            if (x == s) {
                return true;
            }
        }
        return false;
    }

    static int bang(final short[][][] a, final int i, final int j, final int k, final int macDinh) {
        try {
            return a[i][j][k];
        } catch (final RuntimeException e) {
            return macDinh;
        }
    }

    /** Anh chup tinh hinh that cua nhan vat cho bo nao (doc, khong doi gi). */
    static TuDongKeHoach.TinhHinh tinhHinh(final Char p, final TuDong.CauHinh c, final TuDong.Phien ph, final long bay) {
        final TuDongKeHoach.TinhHinh t = new TuDongKeHoach.TinhHinh();
        final Map m = p.map;
        t.x = p.x;
        t.y = p.y;
        try {
            t.cap = p.getLevel();
        } catch (final Exception e) {
            t.cap = 1;
        }
        t.hp = p.hp;
        t.maxhp = p.maxhp;
        t.mp = p.mp;
        t.maxmp = p.maxmp;
        try {
            t.he1 = p.hePokemon1();
            t.he2 = p.hePokemon2();
        } catch (final Exception e) {
            // he khong ro: chon chieu chi theo sat thuong
        }
        synchronized (ph) {
            t.neoX = ph.neoX;
            t.neoY = ph.neoY;
            t.banKinh = ph.banKinh;
            t.nguongHpNguoi = ph.nguongHp;
            t.nguongMpNguoi = ph.nguongMp;
            t.nhatNguoi = ph.nhat;
            t.veHoiPhucNguoi = ph.veHoiPhuc;
            t.mucTieuCu = ph.mucTieu;
            t.dangHoiPhuc = ph.dangHoiPhuc;
            t.tuLanThuocMs = ph.thuocLuc == 0L ? Long.MAX_VALUE : bay - ph.thuocLuc;
            t.chieuGan = ph.chieuGan == null ? null : ph.chieuGan.clone();
            t.thuTuNghiem = ph.chieuGan != null && ph.thuTuNghiem;
        }
        if (t.chieuGan == null) {
            // client cu (0.9.21-0.9.23 khong gui danh sach): it nhat KHONG danh chieu nguoi choi khong gan - trang chieu
            // dang dung trong ho so (chu game: "jar bat auto khong dung cac chieu da gan a?")
            t.chieuGan = chieuTrangHoSo(p);
        }
        t.vuKhiHong = vuKhiHong(p);
        t.thuoc = p.potions == null ? new int[0] : p.potions;
        if (p.timeUsePotion != null) {
            t.thuocConHoi = new long[p.timeUsePotion.length];
            for (int i = 0; i < t.thuocConHoi.length; i++) {
                t.thuocConHoi[i] = Math.max(0L, 400L - (bay - p.timeUsePotion[i]));
            }
        }
        try {
            t.hoiBinh = new int[]{Map.potionTemplates2.get(1).getRecovered(), Map.potionTemplates2.get(2).getRecovered(),
                Map.potionTemplates2.get(3).getRecovered()};
        } catch (final Exception e) {
            t.hoiBinh = null;
        }
        try {
            t.tuiDay = p.isFullInventory();
        } catch (final Exception e) {
            t.tuiDay = false;
        }
        // chieu da hoc (bo chieu buff), sat thuong / PP / hoi chieu theo bang may chu (MapDanhNhau.doAttackMonster)
        try {
            final int cl = p.charClass;
            for (int s = 0; p.skill != null && s < p.skill.length; s++) {
                final int lv = p.skill[s] + (p.addMoreLevelSkill != null && s < p.addMoreLevelSkill.length ? p.addMoreLevelSkill[s] : 0);
                if (lv <= 0 || laBuff(cl, s)) {
                    continue;
                }
                final long hoi = (long) bang(CharManager.SKILL_COOLDOWN, cl, s, lv, 10) * 100L;
                final long da = p.timeLastUseSkills != null && s < p.timeLastUseSkills.length ? bay - p.timeLastUseSkills[s] : Long.MAX_VALUE;
                t.chieu.add(new TuDongKeHoach.Chieu(s, HocChieuGame.heChieu(p, s),
                        bang(CharManager.SKILL_DAM_PERCENT, cl, s, lv, 100), bang(CharManager.SKILL_MP, cl, s, lv, 0),
                        Math.max(0L, hoi - da)));
            }
        } catch (final Exception e) {
            // bang chieu loi: bo nao chi danh khi co chieu
        }
        if (m == null) {
            return t;
        }
        // Pokemon hoang da cung phe / vung
        try {
            final Hashtable<Short, Monster> h = m.nRegion > 0
                    ? ((Region) m.ALL_REGION.get(p.region)).monsters.get(Math.max(0, Math.min(p.inCountry, 1)))
                    : m.monsters.get(p.inCountry);
            for (final Monster mt : new java.util.ArrayList<Monster>(h.values())) {
                if (mt == null || mt.isDead || mt.hp <= 0) {
                    continue;
                }
                final TuDongKeHoach.Quai q = new TuDongKeHoach.Quai(mt.id, mt.x, mt.y, mt.level, mt.hp, mt.maxhp);
                try {
                    q.he1 = mt.hePokemon1();
                    q.he2 = mt.hePokemon2();
                } catch (final Exception e) {
                    // he khong ro
                }
                q.boss = BossLauDai.laBoss(mt);
                try {
                    q.cuaNguoiKhac = mt.getCharID() > 0 || mt.isMyMonster(p) || mt.isMonsterVantieu() || mt.isRuongMaquai()
                            || mt.isNgocRong();
                } catch (final Exception e) {
                    q.cuaNguoiKhac = false;
                }
                t.quai.add(q);
            }
        } catch (final Exception e) {
            // danh sach quai doi giua chung: buoc sau
        }
        // do roi nhat duoc (thuoc / xu, vat pham) cung phe
        try {
            final Vector<Potion> ds = m.potions.get(p.inCountry);
            for (final Potion pt : new java.util.ArrayList<Potion>(ds)) {
                if (pt == null || pt.region != p.region && m.nRegion > 0) {
                    continue;
                }
                final TuDongKeHoach.DoRoi d = new TuDongKeHoach.DoRoi(TuDongKeHoach.DO_THUOC, pt.id, pt.x, pt.y, pt.getType());
                d.nhatDuoc = pt.belongUser == 0 || pt.belongUser == p.charDBID;
                t.doRoi.add(d);
            }
        } catch (final Exception e) {
            // bo qua
        }
        try {
            final Vector<Item> ds = m.items.get(p.inCountry);
            for (final Item it : new java.util.ArrayList<Item>(ds)) {
                if (it == null || it.region != p.region && m.nRegion > 0) {
                    continue;
                }
                final TuDongKeHoach.DoRoi d = new TuDongKeHoach.DoRoi(TuDongKeHoach.DO_VAT_PHAM, it.id, it.x, it.y, 0);
                d.nhatDuoc = it.belongUser == 0 || it.belongUser == p.charDBID;
                t.doRoi.add(d);
            }
        } catch (final Exception e) {
            // bo qua
        }
        // nguoi choi khac bat PK o gan
        if (c.kh.tranhPkPx > 0) {
            final long r2 = (long) c.kh.tranhPkPx * c.kh.tranhPkPx;
            try {
                for (int i = 0; i < m.allPlayers.size(); i++) {
                    for (final Char o : new java.util.ArrayList<Char>(m.allPlayers.get(i))) {
                        if (o == null || o == p || o.isBot != -1 || !(o.isKiller || o.pk > 0)) {
                            continue;
                        }
                        final long dx = o.x - p.x, dy = o.y - p.y;
                        if (dx * dx + dy * dy <= r2) {
                            t.nguoiPkGan++;
                        }
                    }
                }
            } catch (final Exception e) {
                // bo qua
            }
        }
        t.diDuoc = new TuDongKeHoach.DiDuoc() {
            @Override
            public boolean o(final int x, final int y) {
                try {
                    return m.canMove(x, y);
                } catch (final Exception e) {
                    return false;
                }
            }
        };
        return t;
    }

    /**
     * Chen o DAU MapXuLyLenh.processMessage. @return false = BO lenh nay (vuot nhip luc dang tu dong).
     */
    static boolean kiemLenh(final Char p, final int lenh) {
        if (lenh != 9 && lenh != 106 && lenh != 6 && lenh != 22 && lenh != 18 && lenh != 19) {
            return true;
        }
        if (laNguoi(p) == null) {
            return true;
        }
        try {
            final long bay = System.currentTimeMillis();
            final boolean danh = lenh == 9 || lenh == 106;
            final TuDong.Phien ph = phien(p, danh);
            if (ph == null) {
                return true;
            }
            final TuDong.CauHinh c = TuDong.cauHinh();
            if (danh && TuDong.vuotNhipNguoi(c, ph, bay) && bay - ph.nghiVanLuc > 60000L) {
                // moi nguoi choi: chi do + ghi nghi van (toi da 1 dong / phut / nhan vat), khong chan
                ph.nghiVanLuc = bay;
                SO_NGHI_VAN_NHIP.incrementAndGet();
                NghiVanGianLan.ghiChung(p, "NHIP_DANH", "vuot nhip nguoi that " + c.nhipNguoi + (ph.bat ? " (dang tu dong)" : ""));
            }
            if (!ph.bat) {
                return true;
            }
            if (lenh == 6) {
                if (c.tatKhiPvp || BanHuongGame.bat()) {
                    tich(p, c, ph, bay);
                    tat(p, ph, TuDong.TAT_PVP);
                    guiTrangThai(p, c, ph, TuDong.lyDo(TuDong.TAT_PVP));
                }
                return true; // nguoi choi tu danh nguoi: khong chan, chi dung tu dong
            }
            if (danh && kiemDangChay(p, c, ph, bay)) {
                return true; // vua tat: lenh nay coi nhu thao tac tay
            }
            final int hd = danh ? TuDong.HD_DANH : (lenh == 22 ? TuDong.HD_THUOC : TuDong.HD_NHAT);
            if (TuDong.choPhep(c, ph, hd, bay)) {
                return true;
            }
            SO_HANH_DONG_CHAN.incrementAndGet();
            if (!ph.bat) {
                tat(p, ph, TuDong.TAT_VI_PHAM);
                guiTrangThai(p, c, ph, TuDong.lyDo(TuDong.TAT_VI_PHAM));
            }
            return false;
        } catch (final Exception e) {
            return true; // khong bao gio lam hong lenh cua game
        }
    }

    /** Thoi gian cho he thong hoi sinh (dungKhiChet / choHoiSinhTuDong) quyet dinh truoc khi tu tat. */
    static final long CHO_HOI_SINH_MS = 30000L;

    /** Guc: khong xin tu hoi sinh -> tat ngay; co xin -> cho toi da 30 giay cho he thong hoi sinh goi hook. */
    static boolean hetChoHoiSinh(final TuDong.Phien ph, final long bay) {
        synchronized (ph) {
            if (!ph.xinHoiSinh) {
                return true;
            }
            if (ph.chetLuc == 0L) {
                ph.chetLuc = bay;
            }
            return bay - ph.chetLuc > CHO_HOI_SINH_MS;
        }
    }

    // ------------------------------------------------------------------ hook cho he thong guc / hoi sinh

    /**
     * HOOK cho he thong guc / hoi sinh (docs/thiet-ke-game.md muc "Guc va hoi sinh"): goi khi Pokemon cua
     * {@code p} GUC. @return true = dang tu dong VA nguoi choi da bat "Tu hoi sinh" VA con luot (tu_dong.hoi_sinh_moi_gio
     * lan / 60 phut) - he thong hoi sinh duoc dung NGAY mot vat pham hoi sinh tai cho (theo luat thuong cua no) va
     * tu dong chay tiep; da ghi nhan mot luot. false = goi {@link #dungKhiChet} roi ve Trung tam Pokemon nhu thuong.
     */
    public static boolean choHoiSinhTuDong(final Char p) {
        try {
            if (laNguoi(p) == null) {
                return false;
            }
            final TuDong.Phien ph = phien(p, false);
            if (ph == null || !ph.bat) {
                return false;
            }
            final boolean ok = TuDong.choHoiSinh(TuDong.cauHinh(), ph, System.currentTimeMillis());
            if (ok) {
                synchronized (ph) {
                    ph.chetLuc = 0L;
                }
                Nhat.kiemToan("TU_DONG", "tu hoi sinh " + p.charname + " lan=" + ph.soHoiSinh);
            }
            return ok;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Duoc tu dung vat pham hoi sinh TRA PHI (Thuoc hoi sinh toi da - luong khoa) khong: chi khi may chu cho
     * (tu_dong.hoi_sinh_tra_phi=1) VA nguoi choi da dong y rieng trong bang Tu dong. Mac dinh: khong.
     */
    public static boolean choHoiSinhTraPhi(final Char p) {
        try {
            final TuDong.Phien ph = p == null ? null : phien(p, false);
            return ph != null && ph.bat && ph.xinHoiSinhTraPhi && TuDong.cauHinh().hoiSinhTraPhi;
        } catch (final Exception e) {
            return false;
        }
    }

    /** HOOK: Pokemon cua {@code p} guc ma khong tu hoi sinh -> dung tu dong (TAT_CHET) va bao client. */
    public static void dungKhiChet(final Char p) {
        try {
            if (laNguoi(p) == null) {
                return;
            }
            final TuDong.Phien ph = phien(p, false);
            if (ph == null || !ph.bat) {
                return;
            }
            final TuDong.CauHinh c = TuDong.cauHinh();
            tich(p, c, ph, System.currentTimeMillis());
            tat(p, ph, TuDong.TAT_CHET);
            guiTrangThai(p, c, ph, TuDong.lyDo(TuDong.TAT_CHET));
        } catch (final Exception e) {
            // khong lam hong luong guc
        }
    }

    /** Dang tu dong (cho he thong khac tra cuu, vd khong hien hop thoai hoi sinh). */
    public static boolean dangTuDong(final Char p) {
        final TuDong.Phien ph = p == null ? null : phien(p, false);
        return ph != null && ph.bat;
    }

    /** Nhan vat {@code c} bi nguoi choi khac danh (MapDanhNhau). */
    public static void khiPvp(final Char c) {
        dungVi(c, TuDong.TAT_PVP);
    }

    /** Nhan vat {@code p} gay sat thuong vao boss (BossLauDai). */
    public static void khiGapBoss(final Char p) {
        dungVi(p, TuDong.TAT_BOSS);
    }

    /**
     * 0.9.8 ngam tay (BanHuongGame.nhip): dang trong tran giua nguoi choi -&gt; dung tu dong NGAY, khong phu thuoc
     * tu_dong.tat_khi_pvp, bao ro ly do (goi -100 + chu tren dau).
     */
    public static void dungViPvp(final Char p) {
        try {
            if (laNguoi(p) == null) {
                return;
            }
            final TuDong.Phien ph = phien(p, false);
            if (ph == null || !ph.bat) {
                return;
            }
            final TuDong.CauHinh c = TuDong.cauHinh();
            tich(p, c, ph, System.currentTimeMillis());
            tat(p, ph, TuDong.TAT_PVP);
            final String ly = BanHuongGame.lyDoCamTuDong(p);
            guiTrangThai(p, c, ph, ly == null ? TuDong.lyDo(TuDong.TAT_PVP) : ly);
            bao(p, ly == null ? TuDong.lyDo(TuDong.TAT_PVP) : ly);
        } catch (final Exception e) {
            // khong lam hong tran danh
        }
    }

    private static void dungVi(final Char p, final int lyDo) {
        try {
            if (laNguoi(p) == null) {
                return;
            }
            final TuDong.Phien ph = phien(p, false);
            if (ph == null || !ph.bat) {
                return;
            }
            final TuDong.CauHinh c = TuDong.cauHinh();
            if (!c.tatKhiPvp) {
                return;
            }
            tich(p, c, ph, System.currentTimeMillis());
            tat(p, ph, lyDo);
            guiTrangThai(p, c, ph, TuDong.lyDo(lyDo));
        } catch (final Exception e) {
            // khong lam hong tran danh
        }
    }
}
