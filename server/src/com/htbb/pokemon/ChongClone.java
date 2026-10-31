package com.htbb.pokemon;

import com.htbb.cauhinh.CauHinhTrungTam;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import real.Char;
import real.MessageCreator;

/**
 * Noi {@link ChongCloneLogic} vao game: doc cau hinh trung tam (chong_clone.*), dung "ben" tu Char (cap, thiet
 * bi, IP, tuoi tai khoan, gio choi), bao nguoi choi, ghi nhat ky tob_log_chuyen_gia_tri tren luong nen.
 *
 * Cac cong goi vao day (docs/thiet-ke-game.md muc kinh te, "Chống clone"):
 * real/MapMuaBan (giao dich), real/OfflineMap (ky gui: rao + mua), real/VongQuay (vong quay gop hu), real/Map popup 112 + real/MapNhapText
 * (giao dich thu cung lay xu, thach dau cuoc xu), com.htbb.pokemon.NapTienGame (cho trang tri),
 * com.htbb.pokemon.HeSinhThaiMenu (cho sinh thai).
 */
public final class ChongClone {

    public static final String KIEU_GIAO_DICH = "giao_dich";
    public static final String KIEU_KY_GUI = "ky_gui";
    public static final String KIEU_THU_CUNG = "thu_cung";
    public static final String KIEU_CHO_TRANG_TRI = "cho_trang_tri";
    public static final String KIEU_CHO_SINH_THAI = "cho_sinh_thai";
    public static final String KIEU_THACH_DAU = "thach_dau";
    /** Vong quay gop hu (real.VongQuay): moi nguoi thua -> nguoi thang. */
    public static final String KIEU_VONG_QUAY = "vong_quay";

    private static final ChongCloneLogic.SoNgay SO = new ChongCloneLogic.SoNgay();
    private static volatile boolean daNapSo = false;
    private static final long NHO_MS = 300000L;
    /** userId -> {regdate ms (-1 = khong biet), luc doc}. */
    private static final ConcurrentHashMap<Integer, long[]> NHO_DANG_KY = new ConcurrentHashMap<Integer, long[]>();
    /** charId -> {phut da luu, luc doc}. */
    private static final ConcurrentHashMap<Integer, long[]> NHO_PHUT = new ConcurrentHashMap<Integer, long[]>();

    private static final ExecutorService GHI = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(5000), r -> {
                final Thread t = new Thread(r, "chong-clone-ghi");
                t.setDaemon(true);
                return t;
            }, new ThreadPoolExecutor.DiscardPolicy());

    private ChongClone() {
    }

    // ------------------------------------------------------------------ cau hinh

    public static ChongCloneLogic.CauHinh cauHinh() {
        final ChongCloneLogic.CauHinh d = ChongCloneLogic.CauHinh.macDinh();
        try {
            return new ChongCloneLogic.CauHinh(
                    CauHinhTrungTam.so(ChongCloneLogic.K_BAT, d.bat ? 1 : 0) == 1,
                    (int) CauHinhTrungTam.so(ChongCloneLogic.K_CAP, d.capToiThieu),
                    (int) CauHinhTrungTam.so(ChongCloneLogic.K_TUOI_TK_GIO, d.tuoiTaiKhoanGio),
                    (int) CauHinhTrungTam.so(ChongCloneLogic.K_GIO_CHOI, d.gioChoiToiThieu),
                    (int) CauHinhTrungTam.so(ChongCloneLogic.K_CUNG_MAY, d.nvCungMayToiDa),
                    (int) CauHinhTrungTam.so(ChongCloneLogic.K_NGHI_SO_GUI, d.nghiSoNguoiGui),
                    (int) CauHinhTrungTam.so(ChongCloneLogic.K_GIA_LAN, d.giaToiDaLan),
                    (int) CauHinhTrungTam.so(ChongCloneLogic.K_GIA_GIO, d.giaToiDaGio),
                    (int) CauHinhTrungTam.so(ChongCloneLogic.K_GIA_MIN_PT, d.giaToiThieuPhanTram),
                    CauHinhTrungTam.so(ChongCloneLogic.K_BAN_NPC_DO_KHOA, d.banNpcDoKhoa ? 1 : 0) == 1);
        } catch (final Throwable t) {
            return d;
        }
    }

    /** Ban vat pham / ngoc KHOA cho NPC lay xu co duoc khong (mac dinh KHONG: chan doi luong khoa -> xu). */
    public static boolean banNpcDoKhoa() {
        return cauHinh().banNpcDoKhoa;
    }

    // ------------------------------------------------------------------ ben

    static boolean laBot(final Char p) {
        return p == null || p.isBot != -1;
    }

    /** Dung "ben" tu nhan vat dang online. */
    public static ChongCloneLogic.Ben ben(final Char p, final boolean docTuoi) {
        String may = "";
        String ip = "";
        try {
            final io.Session s = p.getSession();
            if (s != null) {
                may = s.IMEI;
                ip = com.htbb.mang.GioiHanIp.apDung(s.ip) ? s.ip : ""; // proxy tin cay: chi tinh theo thiet bi
            }
        } catch (final Throwable ignored) {
        }
        final long tuoi = docTuoi ? tuoiTaiKhoanMs(p.userID) : -1L;
        final long phut = docTuoi ? phutChoi(p) : -1L;
        return new ChongCloneLogic.Ben(p.charDBID, p.charname, p.userID, ThanhVien.cap(p), may, ip, tuoi, phut);
    }

    static long tuoiTaiKhoanMs(final int userId) {
        if (userId <= 0) {
            return -1L;
        }
        final long bay = System.currentTimeMillis();
        long[] n = NHO_DANG_KY.get(Integer.valueOf(userId));
        if (n == null || (n[0] < 0 && bay - n[1] > NHO_MS)) {
            long reg = -1L;
            try {
                reg = data.DatabaseChongClone.ngayDangKy(userId, real.cmd.DangKyTrongGame.csdlTaiKhoan);
            } catch (final Throwable ignored) {
            }
            n = new long[]{reg, bay};
            NHO_DANG_KY.put(Integer.valueOf(userId), n);
        }
        return n[0] < 0 ? -1L : Math.max(0L, bay - n[0]);
    }

    static long phutChoi(final Char p) {
        if (p.charDBID <= 0) {
            return -1L;
        }
        final long bay = System.currentTimeMillis();
        long[] n = NHO_PHUT.get(Integer.valueOf(p.charDBID));
        if (n == null || bay - n[1] > NHO_MS) {
            long da = -1L;
            try {
                da = data.DatabaseChongClone.phutChoiDaLuu(p.charDBID);
            } catch (final Throwable ignored) {
            }
            n = new long[]{da, bay};
            NHO_PHUT.put(Integer.valueOf(p.charDBID), n);
        }
        if (n[0] < 0) {
            return -1L;
        }
        // tob_char.totalTimePlay = phut da luu; p.timePlay = moc da tinh (Char.layPhutChoiChuaLuu day moc len moi lan luu).
        return n[0] + Math.max(0L, (bay - p.timePlay) / 60000L);
    }

    /** Chi test: dat san tuoi tai khoan / phut choi (khong doc CSDL). */
    public static void datThu(final int userId, final long regdateMs, final int charId, final long phutDaLuu) {
        NHO_DANG_KY.put(Integer.valueOf(userId), new long[]{regdateMs, Long.MAX_VALUE / 4});
        NHO_PHUT.put(Integer.valueOf(charId), new long[]{phutDaLuu, Long.MAX_VALUE / 4});
    }

    // ------------------------------------------------------------------ cong

    /** Ly do {@code p} khong duoc chuyen/nhan xu (cap, tuoi tai khoan, gio choi), null neu duoc. */
    public static String lyDo(final Char p, final boolean laMinh) {
        return lyDo(p, laMinh, null);
    }

    /** Nhu tren, loi nhan ghi dung viec theo kieu (KIEU_*; null = chu mac dinh "giao dịch / mua bán xu ..."). */
    public static String lyDo(final Char p, final boolean laMinh, final String kieu) {
        if (laBot(p)) {
            return null;
        }
        final ChongCloneLogic.CauHinh c = cauHinh();
        if (!c.bat) {
            return null;
        }
        return ChongCloneLogic.lyDoChan(c, ben(p, true), laMinh, ChongCloneLogic.viec(kieu));
    }

    /** Chi kiem MOT ben (rao ban khi chua biet nguoi mua). true = da chan va da bao. */
    public static boolean chanMotBen(final Char p, final String kieu) {
        final String m = lyDo(p, true, kieu);
        if (m == null) {
            return false;
        }
        bao(p, m);
        return true;
    }

    /** Ca hai ben deu phai qua cap / tuoi (thach dau). Bao cho {@code nguoiBao}. */
    public static boolean chanCaHai(final Char a, final Char b, final Char nguoiBao, final String kieu) {
        if (real.HlvAiGame.chanCaHai(a, b, nguoiBao, kieu)) { // huan luyen vien AI: han muc ngay bot -> nguoi
            return true;
        }
        String m = lyDo(a, a == nguoiBao, kieu);
        if (m == null) {
            m = lyDo(b, b == nguoiBao, kieu);
        }
        if (m == null) {
            return false;
        }
        bao(nguoiBao, m);
        return true;
    }

    /**
     * Cong day du cho mot lan chuyen xu / gia tri tu {@code gui} sang {@code nhan}: cap + tuoi CA HAI ben va gioi
     * han cung thiet bi / IP moi nguoi nhan moi ngay. true = da chan (da bao {@code nguoiBao}, da ghi nhat ky).
     */
    public static boolean chanChuyen(final Char gui, final Char nhan, final Char nguoiBao, final String kieu, final long xu) {
        if (real.HlvAiGame.chanChuyen(gui, nhan, nguoiBao, kieu, xu)) { // huan luyen vien AI: han muc ngay bot -> nguoi
            return true;
        }
        if (laBot(gui) || laBot(nhan)) {
            return false;
        }
        final ChongCloneLogic.CauHinh c = cauHinh();
        if (!c.bat) {
            return false;
        }
        final ChongCloneLogic.Ben bg = ben(gui, true);
        final ChongCloneLogic.Ben bn = ben(nhan, true);
        final String viec = ChongCloneLogic.viec(kieu);
        String m = ChongCloneLogic.lyDoChan(c, bg, gui == nguoiBao, viec);
        if (m == null) {
            m = ChongCloneLogic.lyDoChan(c, bn, nhan == nguoiBao, viec);
        }
        if (m == null) {
            napSoNeuCan();
            m = SO.kiem(c, ngay(), bg, bn);
        }
        if (m == null) {
            return false;
        }
        bao(nguoiBao, m);
        ghiNen(kieu, bg, bn, xu, "bi chan: " + m, true, false, "");
        return true;
    }

    /** Ghi mot lan chuyen da xong (ca hai ben online). */
    public static void ghi(final Char gui, final Char nhan, final String kieu, final long xu, final String chiTiet) {
        real.HlvAiGame.ghiChuyen(gui, nhan, kieu, xu, chiTiet);
        if (laBot(gui) || laBot(nhan)) {
            return;
        }
        ghiBen(kieu, ben(gui, false), ben(nhan, false), xu, chiTiet);
    }

    /** Ghi mot lan chuyen ma nguoi nhan co the offline (cho trang tri, cho sinh thai): chi biet id + ten. */
    public static void ghi(final Char gui, final int nhanId, final String nhanTen, final String kieu, final long xu,
            final String chiTiet) {
        if (gui != null && nhanId > 0) {
            real.HlvAiGame.ghiChuyen(gui.charDBID, gui.charname, real.HlvAiGame.laBot(gui), nhanId, nhanTen,
                    real.HlvAiGame.laCharBot(nhanId), kieu, xu, chiTiet);
        }
        if (laBot(gui) || nhanId <= 0) {
            return;
        }
        final Char nhan = timOnline(nhanId);
        final ChongCloneLogic.Ben bn = nhan != null ? ben(nhan, false)
                : new ChongCloneLogic.Ben(nhanId, nhanTen, 0, 0, "", "", -1L, -1L);
        ghiBen(kieu, ben(gui, false), bn, xu, chiTiet);
    }

    /** Ghi mot lan chuyen ma nguoi GUI co the offline (don dat hang cho sinh thai): nguoi nhan online. */
    public static void ghiNhan(final int guiId, final String guiTen, final Char nhan, final String kieu, final long xu,
            final String chiTiet) {
        if (nhan != null && guiId > 0) {
            real.HlvAiGame.ghiChuyen(guiId, guiTen, real.HlvAiGame.laCharBot(guiId), nhan.charDBID, nhan.charname,
                    real.HlvAiGame.laBot(nhan), kieu, xu, chiTiet);
        }
        if (laBot(nhan) || guiId <= 0) {
            return;
        }
        final Char gui = timOnline(guiId);
        final ChongCloneLogic.Ben bg = gui != null ? ben(gui, false)
                : new ChongCloneLogic.Ben(guiId, guiTen, 0, 0, "", "", -1L, -1L);
        ghiBen(kieu, bg, ben(nhan, false), xu, chiTiet);
    }

    /** Nguoi nhan co the offline: chi kiem gioi han cung may (cap / tuoi nguoi ban da kiem luc rao). */
    public static boolean chanChuyen(final Char gui, final int nhanId, final String nhanTen, final String kieu, final long xu) {
        if (real.HlvAiGame.chanChuyen(gui, nhanId, nhanTen, real.HlvAiGame.laCharBot(nhanId), gui, kieu, xu)) {
            return true; // huan luyen vien AI: han muc ngay bot -> nguoi
        }
        if (laBot(gui) || nhanId <= 0) {
            return false;
        }
        final Char nhan = timOnline(nhanId);
        if (nhan != null) {
            return chanChuyen(gui, nhan, gui, kieu, xu);
        }
        final ChongCloneLogic.CauHinh c = cauHinh();
        if (!c.bat) {
            return false;
        }
        final ChongCloneLogic.Ben bg = ben(gui, true);
        String m = ChongCloneLogic.lyDoChan(c, bg, true, ChongCloneLogic.viec(kieu));
        final ChongCloneLogic.Ben bn = new ChongCloneLogic.Ben(nhanId, nhanTen, 0, 0, "", "", -1L, -1L);
        if (m == null) {
            napSoNeuCan();
            m = SO.kiem(c, ngay(), bg, bn);
        }
        if (m == null) {
            return false;
        }
        bao(gui, m);
        ghiNen(kieu, bg, bn, xu, "bi chan: " + m, true, false, "");
        return true;
    }

    /** Gia rao ky gui (moi don vi) hop le khong; sai thi bao va tra true. */
    public static boolean giaKyGuiSai(final Char p, final long gia, final long giaThamChieu) {
        final ChongCloneLogic.CauHinh c = cauHinh();
        long xuGio = 1000L;
        try {
            xuGio = LauDai.xuMoiGio(ThanhVien.cap(p));
        } catch (final Throwable ignored) {
        }
        final String m = ChongCloneLogic.kiemGiaKyGui(c, gia, giaThamChieu, xuGio);
        if (m == null) {
            return false;
        }
        bao(p, m);
        return true;
    }

    // ------------------------------------------------------------------ noi bo

    private static void ghiBen(final String kieu, final ChongCloneLogic.Ben bg, final ChongCloneLogic.Ben bn, final long xu,
            final String chiTiet) {
        napSoNeuCan();
        final ChongCloneLogic.KetQuaGhi kq = SO.ghi(cauHinh(), ngay(), bg, bn);
        ghiNen(kieu, bg, bn, xu, chiTiet, false, kq.nghi, kq.lyDo);
        if (kq.nghi) {
            try {
                data.Database.instance.saveOrtherLog("", bn.ten, "nghi clone (" + kieu + "): " + bg.ten + " -> " + bn.ten + " "
                        + xu + " xu | " + kq.lyDo, "nghiclone");
            } catch (final Throwable ignored) {
            }
        }
    }

    private static void ghiNen(final String kieu, final ChongCloneLogic.Ben bg, final ChongCloneLogic.Ben bn, final long xu,
            final String chiTiet, final boolean biChan, final boolean nghi, final String lyDo) {
        try {
            GHI.execute(() -> data.DatabaseChongClone.ghi(kieu, bg, bn, xu, chiTiet, biChan, nghi, lyDo));
        } catch (final Throwable ignored) {
        }
    }

    private static void napSoNeuCan() {
        if (daNapSo) {
            return;
        }
        synchronized (SO) {
            if (daNapSo) {
                return;
            }
            daNapSo = true;
            try {
                final List<ChongCloneLogic.Ben[]> ds = data.DatabaseChongClone.napHomNay();
                final ChongCloneLogic.CauHinh c = cauHinh();
                final String n = ngay();
                for (final ChongCloneLogic.Ben[] h : ds) {
                    SO.ghi(c, n, h[0], h[1]);
                }
            } catch (final Throwable ignored) {
            }
        }
    }

    static String ngay() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    static Char timOnline(final int charId) {
        try {
            return real.CharManager.instance.getCharByCharDbID(charId);
        } catch (final Throwable t) {
            return null;
        }
    }

    static void bao(final Char p, final String m) {
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(m, ""));
        } catch (final Throwable ignored) {
        }
    }

    /** Chi test: so nguoi gui khac nhau cua nguoi nhan hom nay. */
    public static int soNguoiGuiHomNay(final int nhanId) {
        return SO.soNguoiGui(ngay(), nhanId);
    }

    /** Chi test: bo qua nap so tu CSDL. */
    public static void boQuaNapSo() {
        daNapSo = true;
    }
}
