package real;

import io.Message;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GIAO DICH XU 1-1 (0.9.27, yeu cau chu game "giao dien tien te kieu NRO"): trong cua so giao dich (goi 66) moi ben
 * go mot so xu muon dua; hai ben thay so xu cua nhau; khoa / xac nhan nhu voi thuoc. LUONG va LUONG KHOA KHONG giao
 * dich duoc o day (chi qua NPC "Chuyen luong" / san) - khong co kieu goi nao cho luong.
 *
 * Giao thuc (tuong thich nguoc):
 *   client -> may chu  66 / 2 / 3 / long xu      dat SO XU TUYET DOI minh dua (0 = rut het).
 *   may chu -> client  66 / 2 / 3 / short id / long xu   chi gui client BAN >= {@link #BAN_CLIENT};
 *                      client cu (0.9.26, PC Unity khong gui so ban) nhan mot dong chu thay the (khong vo goi).
 *   Client cu khong bao gio gui kieu 3 -> giao dich cua ho chay y nhu truoc (tXu = rXu = 0).
 *
 * Quy tac:
 *   - Luc DAT chi kiem (so du, tran ngay) de bao som; xu KHONG bi giu / tru truoc. Thoat / rot mang / huy giua chung
 *     -> Char.datLaiTrangThaiGiaoDich xoa tXu/rXu, khong mat gi, khong sinh gi.
 *   - GIAM so xu da dat chi khi CA HAI chua khoa (cung luat rut thuoc GiaoDichThuoc.rutDoDuoc); TANG thi duoc den khi
 *     minh khoa (chi co loi cho doi phuong).
 *   - Luc CHOT (MapMuaBan case 5, duoi Char.KHOA_GIAO_DICH): {@link #kiemTraChot} kiem lai tat ca; tru xu hai ben qua
 *     Char.truXuNeuDu (kiem-va-tru mot buoc duoi KHOA_TIEN), thieu ben nao thi HOAN ben kia va huy; sau khi thuoc doi
 *     chu xong moi cong xu cho ben nhan. Khong co duong nao cong xu ma chua tru.
 *   - TRAN NGAY: tong xu MOT TAI KHOAN (userID, moi nhan vat cung tai khoan cong chung) dua di qua giao dich trong ngay
 *     <= thue.tran_chuyen_xu_gio (mac dinh 5) x thu nhap 1 gio o cap nguoi gui (KinhTeLauDai.tranChuyenXuNgay). < 0 =
 *     khong gioi han, 0 = tat giao dich xu. Bo dem trong bo nho, nap lai tu tob_log_chuyen_gia_tri sau khoi dong.
 *     Huan luyen vien AI (HlvAiGame.laBot) khong tinh tran nay - bot -> nguoi da co han muc ngay rieng
 *     (HlvAiGame.chanChuyen qua ChongClone.chanChuyen, gia tri = xu).
 */
public final class GiaoDichXu {

    /** Client >= 0.9.27 hieu goi 66/2/3 (hien o xu trong cua so giao dich). */
    public static final int BAN_CLIENT = 927;

    public static final int HOP_LE = 0;
    public static final int LOI_SO_AM = 1;
    /** tXu cua mot ben khong khop rXu ben kia (trang thai cu / hong). */
    public static final int LOI_LECH_HAI_BEN = 2;
    /** Ben dua khong con du xu. */
    public static final int LOI_KHONG_DU = 3;
    /** Vuot tran chuyen xu trong ngay. */
    public static final int LOI_VUOT_TRAN = 4;
    /** Giam so xu sau khi mot ben da khoa. */
    public static final int LOI_DA_KHOA = 5;
    /** Giao dich xu dang tat (tran = 0). */
    public static final int LOI_TAT = 6;

    /** So xu toi da mot lan dat (chan so vo ly / tran long). */
    public static final long TOI_DA_MOT_LAN = 2000000000L;

    private GiaoDichXu() {
    }

    // ================================================================== luat thuan (kiem thu duoc)

    /**
     * Kiem buoc DAT xu (goi 66/2/3).
     *
     * @param moi        so xu muon dat (tuyet doi)
     * @param cu         so xu dang dat
     * @param conLai     tran ngay con lai cua ben dat (Long.MAX_VALUE = khong gioi han; 0 khi tat va tran = 0)
     * @param tat        giao dich xu dang tat
     */
    public static int kiemDat(final long moi, final long cu, final boolean minhKhoa, final boolean doiPhuongKhoa,
            final boolean doiPhuongXacNhan, final long xuDangCo, final long conLai, final boolean tat) {
        if (moi < 0L || moi > TOI_DA_MOT_LAN) {
            return LOI_SO_AM;
        }
        if (minhKhoa) {
            return LOI_DA_KHOA;
        }
        if (moi < cu && !GiaoDichThuoc.rutDoDuoc(minhKhoa, doiPhuongKhoa, doiPhuongXacNhan)) {
            return LOI_DA_KHOA;
        }
        if (moi == 0L) {
            return HOP_LE;
        }
        if (tat) {
            return LOI_TAT;
        }
        if (moi > xuDangCo) {
            return LOI_KHONG_DU;
        }
        if (moi > conLai) {
            return LOI_VUOT_TRAN;
        }
        return HOP_LE;
    }

    /**
     * Kiem lai luc CHOT. A la nguoi goi, B doi phuong. tA = xu A dua, rA = xu B dua cho A (ghi ben A).
     */
    public static int kiemTraChot(final long tA, final long rA, final long tB, final long rB, final long xuA, final long xuB,
            final long conLaiA, final long conLaiB) {
        if (tA < 0L || rA < 0L || tB < 0L || rB < 0L || tA > TOI_DA_MOT_LAN || tB > TOI_DA_MOT_LAN) {
            return LOI_SO_AM;
        }
        if (tA != rB || tB != rA) {
            return LOI_LECH_HAI_BEN;
        }
        if (tA > xuA || tB > xuB) {
            return LOI_KHONG_DU;
        }
        if (tA > conLaiA || tB > conLaiB) {
            return LOI_VUOT_TRAN;
        }
        return HOP_LE;
    }

    public static String moTaLoi(final int ma) {
        switch (ma) {
            case HOP_LE:
                return "hợp lệ";
            case LOI_SO_AM:
                return "số xu không hợp lệ";
            case LOI_LECH_HAI_BEN:
                return "số xu hai bên không khớp";
            case LOI_KHONG_DU:
                return "không đủ xu";
            case LOI_VUOT_TRAN:
                return "vượt trần chuyển xu hôm nay";
            case LOI_DA_KHOA:
                return "đã khoá giao dịch, không giảm xu được";
            case LOI_TAT:
                return "giao dịch xu đang tắt";
            default:
                return "lỗi " + ma;
        }
    }

    // ================================================================== tran ngay (theo tai khoan)

    /** userID -> xu da dua qua giao dich hom nay. */
    private static final ConcurrentHashMap<Integer, Long> DA_CHUYEN = new ConcurrentHashMap<Integer, Long>();
    private static volatile String ngayDem = "";
    private static volatile boolean daNap = false;
    private static final Object KHOA_DEM = new Object();

    /** Nguon nap lai sau khoi dong (thay duoc trong test). */
    public interface NguonNap {
        Map<Integer, Long> napHomNay();
    }

    static volatile NguonNap nguon = new NguonNap() {
        @Override
        public Map<Integer, Long> napHomNay() {
            return data.DatabaseChongClone.xuGiaoDichHomNay();
        }
    };

    /** Chi test: thay nguon nap (null = khong nap gi) va xoa bo dem. */
    public static void datNguonThu(final NguonNap n) {
        synchronized (KHOA_DEM) {
            nguon = n;
            DA_CHUYEN.clear();
            daNap = false;
            ngayDem = "";
        }
    }

    static String homNay() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    private static void sangNgayVaNap() {
        final String n = homNay();
        if (daNap && n.equals(ngayDem)) {
            return;
        }
        synchronized (KHOA_DEM) {
            if (daNap && n.equals(ngayDem)) {
                return;
            }
            DA_CHUYEN.clear();
            ngayDem = n;
            daNap = true;
            try {
                final NguonNap ng = nguon;
                final Map<Integer, Long> m = ng == null ? null : ng.napHomNay();
                if (m != null) {
                    for (final Map.Entry<Integer, Long> e : m.entrySet()) {
                        if (e.getKey() != null && e.getValue() != null && e.getValue().longValue() > 0L) {
                            DA_CHUYEN.merge(e.getKey(), e.getValue(), Long::sum);
                        }
                    }
                }
            } catch (final Throwable t) {
                System.out.println("[GIAO DICH XU] khong nap duoc tong xu hom nay: " + t);
            }
        }
    }

    /** Xu tai khoan {@code userId} da dua qua giao dich hom nay. */
    public static long daChuyenHomNay(final int userId) {
        sangNgayVaNap();
        final Long v = DA_CHUYEN.get(Integer.valueOf(userId));
        return v == null ? 0L : v.longValue();
    }

    static void ghiDaChuyen(final int userId, final long xu) {
        if (xu <= 0L) {
            return;
        }
        sangNgayVaNap();
        DA_CHUYEN.merge(Integer.valueOf(userId), Long.valueOf(xu), Long::sum);
    }

    /** Ben nay khong bi tran ngay (huan luyen vien AI / nhan vat bot he thong). */
    static boolean mienTran(final Char p) {
        return p == null || HlvAiGame.laBot(p) || p.isBot != -1;
    }

    static int gioTran() {
        return com.htbb.pokemon.LauDai.so(com.htbb.pokemon.LauDai.K_TRAN_CHUYEN_XU,
                com.htbb.pokemon.ThongSoLauDai.TRAN_GIAO_DICH_TRUC_TIEP_GIO);
    }

    /** Giao dich xu dang tat (thue.tran_chuyen_xu_gio = 0). */
    public static boolean tat() {
        return gioTran() == 0;
    }

    /** Tran ngay cua {@code p} (xu); Long.MAX_VALUE = khong gioi han. */
    public static long tranNgay(final Char p) {
        if (mienTran(p) || gioTran() < 0) {
            return Long.MAX_VALUE;
        }
        int cap = 1;
        try {
            cap = Math.max(1, p.lvDetail == null ? 1 : p.lvDetail.lv);
        } catch (final Throwable ignored) {
        }
        try {
            return Math.max(0L, com.htbb.pokemon.KinhTeLauDai.tranChuyenXuNgay(cap));
        } catch (final Throwable t) {
            // bang data_cap_do_lau_dai chua nap: dong (0) thay vi khong gioi han
            return 0L;
        }
    }

    /** Con dua duoc bao nhieu xu hom nay. */
    public static long conLai(final Char p) {
        final long tran = tranNgay(p);
        if (tran == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, tran - daChuyenHomNay(p.userID));
    }

    // ================================================================== noi vao Char (goi tu MapMuaBan, duoi KHOA_GIAO_DICH)

    /** Client hieu goi 66/2/3. */
    public static boolean hieuGoi(final Char p) {
        return p != null && p.getSession() != null && p.getSession().banClient >= BAN_CLIENT;
    }

    /**
     * Buoc DAT xu (66/2/3) cua {@code player} trong giao dich voi {@code dp}. Phai goi duoi Char.KHOA_GIAO_DICH, sau khi
     * MapMuaBan da kiem doi phuong hop le + cung cho dung. Tra null neu da dat, nguoc lai loi nhan da gui cho player.
     */
    public static String dat(final Char player, final Char dp, final long xu) {
        final int ma = kiemDat(xu, player.tXu, player.finishTrade, dp.finishTrade, dp.lockTrade, player.getxu(),
                conLai(player), tat() && !mienTran(player));
        if (ma != HOP_LE) {
            final String loi;
            if (ma == LOI_KHONG_DU) {
                loi = "Bạn chỉ có " + so(player.getxu()) + " xu.";
            } else if (ma == LOI_VUOT_TRAN) {
                loi = "Hôm nay bạn chỉ còn đưa được " + so(conLai(player)) + " xu qua giao dịch (trần "
                        + so(tranNgay(player)) + " xu/ngày, tính chung cả tài khoản).";
            } else if (ma == LOI_DA_KHOA) {
                loi = player.finishTrade ? "Bạn đã khoá giao dịch, không đổi số xu được."
                        : "Đối phương đã khoá giao dịch, không giảm xu được. Huỷ giao dịch nếu muốn đổi.";
            } else if (ma == LOI_TAT) {
                loi = "Giao dịch xu đang tạm tắt.";
            } else {
                loi = "Số xu không hợp lệ.";
            }
            bao(player, loi);
            return loi;
        }
        player.tXu = xu;
        dp.rXu = xu;
        guiSoXu(player, player, xu);
        guiSoXu(dp, player, xu);
        return null;
    }

    /** Bao cho {@code nhan} so xu {@code ai} dang dat: goi 66/2/3 (client moi) hoac mot dong chu (client cu). */
    static void guiSoXu(final Char nhan, final Char ai, final long xu) {
        if (nhan == null) {
            return;
        }
        if (hieuGoi(nhan)) {
            try {
                final Message m = new Message(66);
                m.dos.writeByte(2);
                m.dos.writeByte(3);
                m.dos.writeShort(ai.id);
                m.dos.writeLong(xu);
                nhan.sendMessage(m);
                m.cleanup();
            } catch (final Exception ignored) {
            }
            return;
        }
        if (nhan != ai) {
            // Client cu khong co o xu: bao bang chu de nguoi nhan biet minh duoc dua bao nhieu truoc khi khoa.
            bao(nhan, xu > 0L ? ai.charname + " đưa " + so(xu) + " xu vào giao dịch (bạn sẽ nhận khi hoàn tất)."
                    : ai.charname + " đã rút xu khỏi giao dịch.");
        }
    }

    /**
     * Kiem lai luc CHOT. Tra HOP_LE hoac ma loi (khong doi gi).
     */
    public static int kiemChot(final Char player, final Char dp) {
        return kiemTraChot(player.tXu, player.rXu, dp.tXu, dp.rXu, player.getxu(), dp.getxu(),
                player.tXu > 0L ? conLai(player) : Long.MAX_VALUE, dp.tXu > 0L ? conLai(dp) : Long.MAX_VALUE);
    }

    /**
     * Tru xu hai ben dua (chua cong cho ai). Thieu ben nao -> hoan ben da tru, tra false (khong doi gi).
     */
    public static boolean truHaiBen(final Char a, final Char b) {
        final long ta = a.tXu, tb = b.tXu;
        if (ta > 0L && !a.truXuNeuDu(ta, false, "giao dich xu -> " + b.charname, Char.XU_CHUYEN)) {
            return false;
        }
        if (tb > 0L && !b.truXuNeuDu(tb, false, "giao dich xu -> " + a.charname, Char.XU_CHUYEN)) {
            if (ta > 0L) {
                a.addXu(ta, "hoan giao dich xu (doi phuong thieu xu)", Char.XU_CHUYEN);
            }
            return false;
        }
        return true;
    }

    /** Hoan lai xu da tru boi truHaiBen (buoc sau that bai). */
    public static void hoanHaiBen(final Char a, final Char b) {
        if (a.tXu > 0L) {
            a.addXu(a.tXu, "hoan giao dich xu", Char.XU_CHUYEN);
        }
        if (b.tXu > 0L) {
            b.addXu(b.tXu, "hoan giao dich xu", Char.XU_CHUYEN);
        }
    }

    /** Cong xu cho ben nhan (sau truHaiBen + thuoc doi chu xong), tinh tran ngay, ghi log. */
    public static void congHaiBen(final Char a, final Char b) {
        final long ta = a.tXu, tb = b.tXu;
        if (ta > 0L) {
            b.addXu(ta, "giao dich xu <- " + a.charname, Char.XU_CHUYEN);
            if (!mienTran(a)) {
                ghiDaChuyen(a.userID, ta);
            }
            ghiLog(a, b, ta);
        }
        if (tb > 0L) {
            a.addXu(tb, "giao dich xu <- " + b.charname, Char.XU_CHUYEN);
            if (!mienTran(b)) {
                ghiDaChuyen(b.userID, tb);
            }
            ghiLog(b, a, tb);
        }
    }

    /** Ben nao dua xu RONG (dua - nhan > 0) thi luu truoc: may chu sap giua hai lan luu -> mat, khong nhan doi. */
    public static boolean luuATruoc(final Char a, final Char b) {
        return a.tXu - b.tXu >= 0L;
    }

    private static void ghiLog(final Char gui, final Char nhan, final long xu) {
        try {
            data.Database.instance.saveOrtherLog("", gui.charname, "giao dich xu: " + gui.charname + " (tk " + gui.userID
                    + ") -> " + nhan.charname + " (tk " + nhan.userID + ") " + xu + " xu | " + Char.moTaViTri(gui), "gdxu");
        } catch (final Throwable ignored) {
        }
    }

    static String so(final long v) {
        return v == Long.MAX_VALUE ? "không giới hạn" : com.htbb.pokemon.HoiSinh.so(v);
    }

    static void bao(final Char p, final String m) {
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(m, ""));
        } catch (final Throwable ignored) {
        }
    }
}
