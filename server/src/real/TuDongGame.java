package real;

import com.htbb.log.Nhat;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.TuDong;
import data.NghiVanGianLan;
import io.Message;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Noi TU DONG DANH chinh thuc (com.htbb.pokemon.TuDong) vao game. Thiet ke: docs/pokemon/TU_DONG_DANH.md.
 *
 *   - Lenh -100 (client -> server, luong ban do qua MapXuLyLenh): byte loai 0 xin trang thai / 1 bat (+ short ban
 *     kinh xin, px) / 2 tat / 3 nhip (client gui ~20 giay mot lan de may chu cong gio va phat hien het gio).
 *   - Lenh -100 (server -> client): byte 0, byte bat, int giay con lai hom nay, int giay toi da hom nay, short ban
 *     kinh (px), byte co (TuDong.CO_*), UTF thong bao ("" = khong).
 *   - {@link #kiemLenh} chen o DAU MapXuLyLenh.processMessage: luc dang tu dong, lenh danh (9 / 106) / thuoc (22) /
 *     nhat (18 / 19) di qua nhip nguoi that; danh nguoi (6) = PvP -> tat. Lenh danh cua MOI nguoi choi duoc do
 *     nhip (chi ghi nghi van, khong chan).
 *   - {@link #khiPvp} (bi danh, MapDanhNhau) va {@link #khiGapBoss} (BossLauDai.ghiNhan) tat theo tu_dong.tat_khi_pvp.
 *   - EXP / xu / do roi KHONG doi: tu dong ha quai qua dung duong thuong, chiu nang luong (NangLuong).
 *   - Phieu uy thac (treo OFFLINE, TangTocGame) tach rieng: thoat game la phien tu dong ket thuc.
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

    static boolean coQuai(final Map m) {
        try {
            for (int i = 0; i < m.monsters.size(); i++) {
                final java.util.Hashtable<Short, Monster> h = m.monsters.get(i);
                if (h != null && !h.isEmpty()) {
                    return true;
                }
            }
        } catch (final Exception e) {
            // vector doi giua chung: coi nhu khong co
        }
        return false;
    }

    static boolean laSanDau(final Char p, final Map m) {
        return m instanceof MapLoiDai || m instanceof MapLienDau || m instanceof MapChienTruongMoba || m instanceof MapClan
                || m instanceof MapTown || m.inArena(p);
    }

    static TuDong.NguCanh nguCanh(final Char p) {
        final TuDong.NguCanh n = new TuDong.NguCanh();
        final Map m = p.map;
        n.chet = p.hp <= 0;
        n.pvp = p.isKiller;
        n.sanDau = m == null || laSanDau(p, m);
        n.baiLuyen = m != null && !(m instanceof OfflineMap) && !(m instanceof MapChauBau) && !n.sanDau && coQuai(m);
        n.theThang = TangTocGame.coTheThang(p);
        n.map = m == null ? -1 : m.mapId;
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
            m.dos.writeInt(TuDong.giayToiDa(c, the));
            m.dos.writeShort(ph.bat ? ph.banKinh : c.banKinh);
            m.dos.writeByte(c.co());
            m.dos.writeUTF(thongBao == null ? "" : thongBao);
            p.sendMessage(m);
        } catch (final Exception e) {
            // mat ket noi: bo qua
        }
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
        try {
            loai = message.dis.readByte();
            if (loai == TuDong.C_BAT && message.dis.available() >= 2) {
                banKinh = message.dis.readShort();
            }
            if (loai == TuDong.C_BAT && message.dis.available() >= 1) {
                xinHoiSinh = message.dis.readByte() != 0;
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
            final String loi = TuDong.kiemBat(c, ph, n, homNay(bay));
            if (loi != null) {
                if (dangBat) {
                    tat(p, ph, TuDong.TAT_NGUOI_CHOI);
                }
                SO_LAN_TU_CHOI.incrementAndGet();
                guiTrangThai(p, c, ph, loi);
                return;
            }
            TuDong.bat(c, ph, n, banKinh, bay);
            synchronized (ph) {
                ph.xinHoiSinh = xinHoiSinh;
                ph.chetLuc = 0L;
            }
            SO_LAN_BAT.incrementAndGet();
            Nhat.kiemToan("TU_DONG", "bat " + p.charname + " map=" + n.map + " x=" + n.x + " y=" + n.y + " banKinh="
                    + ph.banKinh + " conLai=" + TuDong.giayConLai(c, ph, n.theThang, homNay(bay)) + "s");
            guiTrangThai(p, c, ph, "");
            return;
        }
        if (loai == TuDong.C_TAT) {
            if (ph.bat) {
                tich(p, c, ph, bay);
                tat(p, ph, TuDong.TAT_NGUOI_CHOI);
            }
            guiTrangThai(p, c, ph, "");
            return;
        }
        if (ph.bat) {
            // nhip / xin trang thai luc dang chay: cong gio + kiem dieu kien
            if (kiemDangChay(p, c, ph, bay)) {
                return; // da tat + da gui
            }
        }
        guiTrangThai(p, c, ph, "");
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
        }
        ghiNgay(p, ph);
        if (lyDo < 0) {
            return false;
        }
        tat(p, ph, lyDo);
        guiTrangThai(p, c, ph, TuDong.lyDo(lyDo));
        return true;
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
                if (c.tatKhiPvp) {
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
     * HOOK cho he thong guc / hoi sinh (docs/pokemon/TU_DONG_DANH.md muc "Guc va hoi sinh"): goi khi Pokemon cua
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
