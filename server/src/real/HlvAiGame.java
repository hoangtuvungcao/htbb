package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.DongHo;
import com.htbb.pokemon.HlvAi;
import data.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Noi game cua HUAN LUYEN VIEN AI (luat thuan: com.htbb.pokemon.HlvAi). May chu biet tai khoan nao la bot qua cot
 * account.team_user.la_bot (doc luc chon nhan vat, KHONG gui client) va ap luat kinh te len nhan vat co
 * {@link Char#laHlvAi}. Bang (tu tao neu thieu, cung co trong khoi them_hlv_ai.sql cua sql/htbb.sql):
 * <ul>
 * <li>tob_hlv_ai_kinh_te_ngay - moi ngay: xu bot tao / cat theo he so / dot vi tran / tieu, xu va gia tri chuyen bot <->
 * nguoi, vat pham huy, tong xu bot / nguoi that (web /admin/hlv-ai, bieu do).</li>
 * <li>tob_hlv_ai_moc - anh chup moi 10 phut (tong xu bot / nguoi, so bot online) cho kiem lam phat.</li>
 * <li>tob_hlv_ai_chuyen - moi lan chuyen gia tri bot <-> nguoi that (ca lan bi chan).</li>
 * <li>tob_hlv_ai_gia - gia moi lan ban xong o cho (ky gui / cho Pokemon / cho sinh thai / cho trang tri): trung vi.</li>
 * </ul>
 */
public final class HlvAiGame {

    public static final String BANG_NGAY = "tob_hlv_ai_kinh_te_ngay";

    static final String[] SQL_TAO = {
        "CREATE TABLE IF NOT EXISTS tob_hlv_ai_kinh_te_ngay (ngay DATE NOT NULL, xu_tao BIGINT NOT NULL DEFAULT 0,"
            + " xu_cat BIGINT NOT NULL DEFAULT 0, xu_dot BIGINT NOT NULL DEFAULT 0, xu_tieu BIGINT NOT NULL DEFAULT 0,"
            + " xu_nhan_nguoi BIGINT NOT NULL DEFAULT 0, xu_tra_nguoi BIGINT NOT NULL DEFAULT 0,"
            + " gia_tri_cho_nguoi BIGINT NOT NULL DEFAULT 0, so_chuyen INT NOT NULL DEFAULT 0, so_chan INT NOT NULL DEFAULT 0,"
            + " vat_huy INT NOT NULL DEFAULT 0, tong_xu_bot BIGINT NOT NULL DEFAULT 0, tong_xu_nguoi BIGINT NOT NULL DEFAULT 0,"
            + " so_bot INT NOT NULL DEFAULT 0, so_bot_online INT NOT NULL DEFAULT 0, cap_nhat DATETIME NULL,"
            + " PRIMARY KEY (ngay)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
        "CREATE TABLE IF NOT EXISTS tob_hlv_ai_moc (luc DATETIME NOT NULL, tong_xu_bot BIGINT NOT NULL DEFAULT 0,"
            + " tong_xu_nguoi BIGINT NOT NULL DEFAULT 0, kho_xu_bot BIGINT NOT NULL DEFAULT 0, kho_xu_nguoi BIGINT NOT NULL DEFAULT 0,"
            + " so_bot INT NOT NULL DEFAULT 0, so_bot_online INT NOT NULL DEFAULT 0, so_nguoi_online INT NOT NULL DEFAULT 0,"
            + " xu_tao_ngay BIGINT NOT NULL DEFAULT 0, xu_dot_ngay BIGINT NOT NULL DEFAULT 0, xu_tieu_ngay BIGINT NOT NULL DEFAULT 0,"
            + " gia_tri_cho_nguoi_ngay BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (luc)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
        "CREATE TABLE IF NOT EXISTS tob_hlv_ai_chuyen (id BIGINT NOT NULL AUTO_INCREMENT, luc DATETIME NOT NULL,"
            + " huong TINYINT NOT NULL DEFAULT 0, kieu VARCHAR(24) NOT NULL DEFAULT '', bot_id INT NOT NULL DEFAULT 0,"
            + " bot_ten VARCHAR(40) NOT NULL DEFAULT '', nguoi_id INT NOT NULL DEFAULT 0, nguoi_ten VARCHAR(40) NOT NULL DEFAULT '',"
            + " xu BIGINT NOT NULL DEFAULT 0, gia_tri BIGINT NOT NULL DEFAULT 0, bi_chan TINYINT NOT NULL DEFAULT 0,"
            + " chi_tiet VARCHAR(255) NOT NULL DEFAULT '', PRIMARY KEY (id), KEY k_luc (luc), KEY k_nguoi (nguoi_id, luc))"
            + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
        "CREATE TABLE IF NOT EXISTS tob_hlv_ai_gia (id BIGINT NOT NULL AUTO_INCREMENT, luc DATETIME NOT NULL,"
            + " loai VARCHAR(16) NOT NULL, ma VARCHAR(40) NOT NULL, gia BIGINT NOT NULL, PRIMARY KEY (id),"
            + " KEY k_ma (loai, ma, luc)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
    };

    /** Huong chuyen trong tob_hlv_ai_chuyen. */
    public static final int BOT_SANG_NGUOI = 1;
    public static final int NGUOI_SANG_BOT = 2;
    /** "Nguoi nhan" gia khi bot gop quy doi: MA_BANG + id doi (khong trung id nhan vat). */
    public static final int MA_BANG = 1000000000;

    private static final int MAU_GIA = 50;
    private static final long GIA_HAN_MS = 7L * 24L * 3600000L;

    // ------------------------------------------------------------------ nhan dien
    /** userId -> la bot (doc khi chon nhan vat). */
    private static final Map<Integer, Boolean> TAI_KHOAN = new ConcurrentHashMap<Integer, Boolean>();
    /** Ten nhan vat (chu thuong) cua moi bot - nap lai moi phut: ten doc tu CSDL (top, bang, ban be offline...). */
    private static volatile Set<String> tenBot = Collections.emptySet();
    private static volatile Set<Integer> charBot = Collections.emptySet();

    // ------------------------------------------------------------------ bo dem ngay
    private static volatile int ngayDem = 0;
    private static final AtomicLong XU_TAO = new AtomicLong(), XU_CAT = new AtomicLong(), XU_DOT = new AtomicLong(),
            XU_TIEU = new AtomicLong(), XU_NHAN_NGUOI = new AtomicLong(), XU_TRA_NGUOI = new AtomicLong(),
            GIA_TRI_NGUOI = new AtomicLong(), SO_CHUYEN = new AtomicLong(), SO_CHAN = new AtomicLong(), VAT_HUY = new AtomicLong();
    /** Phan da ghi xuong CSDL (de ghi them phan chenh). */
    private static final long[] DA_GHI = new long[10];
    /** Gia tri bot -> nguoi that hom nay: tong ca may chu va theo id nhan vat nguoi nhan. */
    private static final AtomicLong DA_TONG = new AtomicLong();
    private static final Map<Integer, AtomicLong> DA_NGUOI = new ConcurrentHashMap<Integer, AtomicLong>();

    /** Gia ban gan day: "loai|ma" -> vong 50 gia gan nhat (kem luc). */
    private static final Map<String, long[][]> GIA = new ConcurrentHashMap<String, long[][]>();

    private static volatile boolean daKhoiDong = false;
    private static volatile boolean daTaoBang = false;
    private static ScheduledExecutorService nhip;
    private static final ExecutorService GHI = Executors.newSingleThreadExecutor(r -> {
        final Thread t = new Thread(r, "hlv-ai-ghi");
        t.setDaemon(true);
        return t;
    });

    private HlvAiGame() {
    }

    // =================================================================== khoi dong / nhip
    public static synchronized void khoiDong() {
        if (daKhoiDong) {
            return;
        }
        daKhoiDong = true;
        nhip = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "hlv-ai-nhip");
            t.setDaemon(true);
            return t;
        });
        nhip.execute(() -> {
            taoBang();
            napHomNay();
            napGia();
        });
        nhip.scheduleWithFixedDelay(HlvAiGame::moiPhut, 20, 60, TimeUnit.SECONDS);
        nhip.scheduleWithFixedDelay(HlvAiGame::chupMoc, 90, 600, TimeUnit.SECONDS);
        // tien to ten (hlv_ai.tien_to_ten): doi tren web la ap dung trong <= 15 giay, khong khoi dong lai
        HlvAi.datLaTenBot(HlvAiGame::laTenBotThat);
        nhip.scheduleWithFixedDelay(() -> {
            try {
                HlvAi.datTienTo(CauHinhTrungTam.chuoi(HlvAi.K_TIEN_TO));
            } catch (final Throwable ignored) {
            }
        }, 5, 15, TimeUnit.SECONDS);
    }

    static void moiPhut() {
        try {
            napTenBot();
            catVatPham();
            ghiNgay(false);
        } catch (final Throwable e) {
            System.out.println("[HLV AI] nhip: " + e);
        }
    }

    // =================================================================== nhan dien
    /** Doc account.team_user.la_bot cho tai khoan (moi lan chon nhan vat - re, it). */
    public static boolean laTaiKhoanBot(final int userId) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        String cu = null;
        boolean bot = false;
        try {
            conn = Database.instance.getConnection();
            cu = conn.getCatalog();
            final String tk = real.cmd.DangKyTrongGame.csdlTaiKhoan;
            if (tk != null && !tk.equals(cu)) {
                conn.setCatalog(tk);
            }
            pre = conn.prepareStatement("SELECT la_bot FROM team_user WHERE id = ? LIMIT 1");
            pre.setInt(1, userId);
            rs = pre.executeQuery();
            bot = rs.next() && rs.getInt(1) == 1;
        } catch (final Exception e) {
            // cot chua co (CSDL chua nang cap) = khong co bot
            final Boolean c = TAI_KHOAN.get(userId);
            bot = c != null && c;
        } finally {
            traCatalog(conn, cu);
            dong(rs, pre, conn);
        }
        TAI_KHOAN.put(userId, bot);
        return bot;
    }

    /** Goi sau khi nhan vat vao game: dat co + them ten vao danh sach hien thi. */
    public static void vaoGame(final Char p) {
        if (p == null) {
            return;
        }
        p.laHlvAi = laTaiKhoanBot(p.userID);
        if (p.laHlvAi && p.charname != null) {
            final Set<String> s = new HashSet<String>(tenBot);
            s.add(p.charname.toLowerCase());
            tenBot = Collections.unmodifiableSet(s);
            final Set<Integer> c = new HashSet<Integer>(charBot);
            c.add(p.charDBID);
            charBot = Collections.unmodifiableSet(c);
        }
        khoiDong();
    }

    /**
     * Bot lap doi: mien 100 trieu xu + huy hieu lap doi khi so doi do bot lam chu < hlv_ai.doi_bot_toi_da (mac dinh 3).
     * Cap 50 + ten doi + icon van nhu nguoi choi. Quy doi cua doi bot -> nguoi that van qua han muc ngay (luong doi).
     */
    public static boolean mienPhiLapDoi(final Char p) {
        if (!laBot(p)) {
            return false;
        }
        final long tran = CauHinhTrungTam.so(HlvAi.K_DOI_BOT, HlvAi.DOI_BOT);
        if (tran <= 0L) {
            return false;
        }
        int dem = 0;
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT charMaster FROM tob_clan");
            while (rs.next()) {
                if (laTenBot(rs.getString(1) == null ? "" : rs.getString(1).trim())) {
                    dem++;
                }
            }
        } catch (final Exception e) {
            return false;
        } finally {
            dong(rs, st, conn);
        }
        return dem < tran;
    }

    public static boolean laBot(final Char p) {
        return p != null && p.laHlvAi;
    }

    public static boolean laTenBot(final String ten) {
        return ten != null && (laTenBotThat(ten) || tenBot.contains(HlvAi.boTienTo(ten).toLowerCase()));
    }

    /** Ten (khong tien to) co phai nhan vat bot (danh sach nap moi phut + bot vua vao game). */
    static boolean laTenBotThat(final String ten) {
        return ten != null && tenBot.contains(ten.trim().toLowerCase());
    }

    public static boolean laCharBot(final int charId) {
        return charBot.contains(charId);
    }

    /** Ten client thay cho nhan vat dang online. */
    public static String hienThi(final Char p) {
        return p == null ? "" : HlvAi.hienThi(p.charname, p.laHlvAi);
    }

    /** Ten client thay cho ten doc tu CSDL (top, thanh vien bang, ban be, nguoi ban). */
    public static String hienThi(final String ten) {
        return HlvAi.hienThi(ten, laTenBot(ten));
    }

    static void napTenBot() {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        String cu = null;
        final List<Integer> tk = new ArrayList<Integer>();
        try {
            conn = Database.instance.getConnection();
            cu = conn.getCatalog();
            final String csdl = real.cmd.DangKyTrongGame.csdlTaiKhoan;
            if (csdl != null && !csdl.equals(cu)) {
                conn.setCatalog(csdl);
            }
            st = conn.createStatement();
            rs = st.executeQuery("SELECT id FROM team_user WHERE la_bot = 1");
            while (rs.next()) {
                tk.add(rs.getInt(1));
            }
        } catch (final Exception e) {
            return;
        } finally {
            traCatalog(conn, cu);
            dong(rs, st, conn);
        }
        final Set<String> ten = new HashSet<String>();
        final Set<Integer> ids = new HashSet<Integer>();
        if (!tk.isEmpty()) {
            conn = null;
            st = null;
            rs = null;
            try {
                conn = Database.instance.getConnection();
                st = conn.createStatement();
                rs = st.executeQuery("SELECT id, charname FROM tob_char WHERE userid IN (" + noi(tk) + ")");
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                    if (rs.getString(2) != null) {
                        ten.add(rs.getString(2).toLowerCase());
                    }
                }
            } catch (final Exception e) {
                return;
            } finally {
                dong(rs, st, conn);
            }
        }
        // giu ca nhan vat dang online vua them (chua co trong lan doc)
        for (final Char p : dsOnline()) {
            if (p.laHlvAi && p.charname != null) {
                ten.add(p.charname.toLowerCase());
                ids.add(p.charDBID);
            }
        }
        tenBot = Collections.unmodifiableSet(ten);
        charBot = Collections.unmodifiableSet(ids);
        dsTaiKhoan = tk;
    }

    private static volatile List<Integer> dsTaiKhoan = Collections.emptyList();

    public static List<Char> dsOnline() {
        final List<Char> ra = new ArrayList<Char>();
        try {
            synchronized (CharManager.instance.vChars) {
                ra.addAll(CharManager.instance.vChars);
            }
        } catch (final Throwable ignored) {
        }
        return ra;
    }

    // =================================================================== thu nhap / tran
    static void sangNgay() {
        final int n = HlvAi.ngay(DongHo.lich());
        if (n == ngayDem) {
            return;
        }
        synchronized (HlvAiGame.class) {
            if (n == ngayDem) {
                return;
            }
            if (ngayDem != 0) {
                ghiNgay(true);
                for (final AtomicLong a : new AtomicLong[]{XU_TAO, XU_CAT, XU_DOT, XU_TIEU, XU_NHAN_NGUOI, XU_TRA_NGUOI,
                    GIA_TRI_NGUOI, SO_CHUYEN, SO_CHAN, VAT_HUY}) {
                    a.set(0L);
                }
                java.util.Arrays.fill(DA_GHI, 0L);
                DA_TONG.set(0L);
                DA_NGUOI.clear();
            }
            ngayDem = n;
        }
    }

    /** Xu bot that su nhan tu nguon SINH RA {@code xu} (Char.XU_DEM): nhan he so, ghi phan cat. */
    public static long thuNhap(final Char p, final long xu) {
        if (xu <= 0L) {
            return xu;
        }
        sangNgay();
        final long nhan = HlvAi.thuNhap(xu, CauHinhTrungTam.so(HlvAi.K_HE_SO, HlvAi.HE_SO));
        XU_TAO.addAndGet(nhan);
        XU_CAT.addAndGet(xu - nhan);
        return nhan;
    }

    public static long tranXu() {
        return CauHinhTrungTam.so(HlvAi.K_TRAN_XU, HlvAi.TRAN_XU);
    }

    public static void ghiDot(final Char p, final long xu) {
        if (xu <= 0L) {
            return;
        }
        sangNgay();
        XU_DOT.addAndGet(xu);
        Database.instance.saveOrtherLog("", p.charname, "hlv ai dot " + xu + " xu vuot tran", "truxu");
    }

    public static void ghiTieu(final long xu) {
        if (xu > 0L) {
            sangNgay();
            XU_TIEU.addAndGet(xu);
        }
    }

    // =================================================================== tien that
    /** Bot khong dung luong nap / mua goi / chuyen luong / giftcode. true = da chan (va bao). */
    public static boolean chanTienThat(final Char p) {
        if (!laBot(p)) {
            return false;
        }
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage("Huấn luyện viên AI không dùng chức năng tiền thật.", ""));
        } catch (final Throwable ignored) {
        }
        return true;
    }

    // =================================================================== chuyen gia tri bot <-> nguoi
    /**
     * Cong han muc cho mot lan chuyen tu {@code gui} (ben tra xu / dua vat) sang {@code nhanId}. Chi chan khi gui la bot
     * va nhan la nguoi that va vuot han muc ngay. true = da chan (da bao {@code nguoiBao}, da ghi).
     */
    public static boolean chanChuyen(final Char gui, final int nhanId, final String nhanTen, final boolean nhanLaBot,
            final Char nguoiBao, final String kieu, final long xu) {
        if (!laBot(gui) || nhanLaBot || nhanId <= 0) {
            return false;
        }
        sangNgay();
        final long gt = HlvAi.giaTri(kieu, xu);
        final AtomicLong da = DA_NGUOI.get(nhanId);
        if (HlvAi.conDuocChuyen(da == null ? 0L : da.get(), DA_TONG.get(), gt,
                CauHinhTrungTam.so(HlvAi.K_CHUYEN_NGUOI, HlvAi.CHUYEN_NGUOI),
                CauHinhTrungTam.so(HlvAi.K_CHUYEN_TONG, HlvAi.CHUYEN_TONG))) {
            return false;
        }
        SO_CHAN.incrementAndGet();
        ghiBang(BOT_SANG_NGUOI, kieu, gui.charDBID, gui.charname, nhanId, nhanTen, xu, gt, true, "vuot han muc ngay");
        if (nguoiBao != null) {
            try {
                nguoiBao.sendMessage(MessageCreator.createServerAlertMessage(
                        "Huấn luyện viên AI đã đạt giới hạn giao dịch hôm nay với người chơi, hẹn ngày mai nhé.", ""));
            } catch (final Throwable ignored) {
            }
        }
        return true;
    }

    /**
     * Thach dau co cuoc (ChongClone.chanCaHai kieu thach_dau) giua bot va nguoi that: chua biet ai thua -> chi cho khi
     * nguoi that con han muc nhan hom nay (bot thua -> ThachDauCuoc ghi phan thua vao han muc). true = da chan.
     */
    public static boolean chanCaHai(final Char a, final Char b, final Char nguoiBao, final String kieu) {
        if (a == null || b == null || laBot(a) == laBot(b) || !"thach_dau".equals(kieu)) {
            return false;
        }
        final Char bot = laBot(a) ? a : b;
        final Char nguoi = bot == a ? b : a;
        return chanChuyen(bot, nguoi.charDBID, nguoi.charname, false, nguoiBao, kieu, 1L);
    }

    public static boolean chanChuyen(final Char gui, final Char nhan, final Char nguoiBao, final String kieu, final long xu) {
        return nhan != null && chanChuyen(gui, nhan.charDBID, nhan.charname, laBot(nhan), nguoiBao, kieu, xu);
    }

    /** Ghi mot lan chuyen da xong ({@code gui} tra xu / dua vat cho {@code nhan}). */
    public static void ghiChuyen(final int guiId, final String guiTen, final boolean guiBot, final int nhanId,
            final String nhanTen, final boolean nhanBot, final String kieu, final long xu, final String chiTiet) {
        if (guiBot == nhanBot) {
            return; // nguoi <-> nguoi (ChongClone da ghi) hoac bot <-> bot (khong anh huong nguoi that)
        }
        sangNgay();
        SO_CHUYEN.incrementAndGet();
        if (guiBot) {
            final long gt = HlvAi.giaTri(kieu, xu);
            GIA_TRI_NGUOI.addAndGet(gt);
            XU_TRA_NGUOI.addAndGet(Math.max(0L, xu));
            DA_TONG.addAndGet(gt);
            DA_NGUOI.computeIfAbsent(nhanId, k -> new AtomicLong()).addAndGet(gt);
            ghiBang(BOT_SANG_NGUOI, kieu, guiId, guiTen, nhanId, nhanTen, xu, gt, false, chiTiet);
        } else {
            XU_NHAN_NGUOI.addAndGet(Math.max(0L, xu));
            ghiBang(NGUOI_SANG_BOT, kieu, nhanId, nhanTen, guiId, guiTen, xu, 0L, false, chiTiet);
        }
    }

    public static void ghiChuyen(final Char gui, final Char nhan, final String kieu, final long xu, final String chiTiet) {
        if (gui == null || nhan == null) {
            return;
        }
        ghiChuyen(gui.charDBID, gui.charname, laBot(gui), nhan.charDBID, nhan.charname, laBot(nhan), kieu, xu, chiTiet);
    }

    /** Gia tri bot da chuyen cho nhan vat nguoi {@code nhanId} hom nay (kiem thu, trang web). */
    public static long daChuyenHomNay(final int nhanId) {
        final AtomicLong a = DA_NGUOI.get(nhanId);
        return a == null ? 0L : a.get();
    }

    private static void ghiBang(final int huong, final String kieu, final int botId, final String botTen, final int nguoiId,
            final String nguoiTen, final long xu, final long gt, final boolean chan, final String chiTiet) {
        final String ct = chiTiet == null ? "" : chiTiet.length() > 250 ? chiTiet.substring(0, 250) : chiTiet;
        GHI.execute(() -> capNhat("INSERT INTO tob_hlv_ai_chuyen (luc,huong,kieu,bot_id,bot_ten,nguoi_id,nguoi_ten,xu,gia_tri,bi_chan,chi_tiet)"
                + " VALUES (NOW(),?,?,?,?,?,?,?,?,?,?)", huong, cat(kieu, 24), botId, cat(botTen, 40), nguoiId, cat(nguoiTen, 40),
                xu, gt, chan ? 1 : 0, ct));
    }

    // =================================================================== gia cho
    /** Ghi gia mot lan ban xong (moi don vi) - cho moi nguoi ban (lam trung vi cho luat gia san cua bot). */
    public static void ghiGiaBan(final String loai, final String ma, final long giaMoiDonVi) {
        if (giaMoiDonVi <= 0L || loai == null || ma == null) {
            return;
        }
        themGia(loai + "|" + ma, giaMoiDonVi, System.currentTimeMillis());
        GHI.execute(() -> capNhat("INSERT INTO tob_hlv_ai_gia (luc,loai,ma,gia) VALUES (NOW(),?,?,?)", cat(loai, 16), cat(ma, 40),
                giaMoiDonVi));
    }

    static void themGia(final String khoa, final long gia, final long luc) {
        final long[][] v = GIA.computeIfAbsent(khoa, k -> new long[][]{new long[MAU_GIA], new long[MAU_GIA], new long[1]});
        synchronized (v) {
            final int i = (int) (v[2][0] % MAU_GIA);
            v[0][i] = gia;
            v[1][i] = luc;
            v[2][0]++;
        }
    }

    /** {trung vi, so mau} cua gia ban 7 ngay gan nhat. */
    public static long[] trungVi(final String loai, final String ma) {
        final long[][] v = GIA.get(loai + "|" + ma);
        if (v == null) {
            return new long[]{-1L, 0L};
        }
        final long han = System.currentTimeMillis() - GIA_HAN_MS;
        final long[] a = new long[MAU_GIA];
        int n = 0;
        synchronized (v) {
            final int co = (int) Math.min(MAU_GIA, v[2][0]);
            for (int i = 0; i < co; i++) {
                if (v[1][i] >= han) {
                    a[n++] = v[0][i];
                }
            }
        }
        return new long[]{HlvAi.trungVi(a, n), n};
    }

    /**
     * Bot rao ban duoi gia san (pha gia): bao va tra true. {@code thamChieu} = gia NPC / khung gia moi don vi.
     */
    public static boolean giaBanThap(final Char p, final String loai, final String ma, final long giaMoiDonVi, final long thamChieu) {
        if (!laBot(p)) {
            return false;
        }
        final long[] tv = trungVi(loai, ma);
        final long san = HlvAi.giaSan(tv[0], (int) tv[1], thamChieu, CauHinhTrungTam.so(HlvAi.K_GIA_BAN, HlvAi.GIA_BAN));
        if (giaMoiDonVi >= san) {
            return false;
        }
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage("Giá rao thấp hơn giá thị trường (" + san + " xu).", ""));
        } catch (final Throwable ignored) {
        }
        return true;
    }

    static void napGia() {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT loai, ma, gia, luc FROM tob_hlv_ai_gia WHERE luc >= NOW() - INTERVAL 7 DAY ORDER BY id");
            while (rs.next()) {
                themGia(rs.getString(1) + "|" + rs.getString(2), rs.getLong(3), rs.getTimestamp(4).getTime());
            }
        } catch (final Exception e) {
            System.out.println("[HLV AI] khong nap duoc gia cho: " + e);
        } finally {
            dong(rs, st, conn);
        }
    }

    // =================================================================== tran vat pham
    /** Bot online co qua {@code hlv_ai.tran_vat_pham} o tui (khong tinh do dang rao): huy o thua, mon re nhat truoc. */
    static void catVatPham() {
        final int tran = (int) CauHinhTrungTam.so(HlvAi.K_TRAN_VAT_PHAM, HlvAi.TRAN_VAT_PHAM);
        for (final Char p : dsOnline()) {
            if (!p.laHlvAi || p.iItems == null) {
                continue;
            }
            final List<Item> bo = new ArrayList<Item>();
            synchronized (p.iItems) {
                final int huy = HlvAi.soOHuy(p.iItems.size(), tran);
                if (huy <= 0) {
                    continue;
                }
                final List<Item> ds = new ArrayList<Item>();
                for (final Item it : p.iItems) {
                    if (!it.isSelling) {
                        ds.add(it);
                    }
                }
                ds.sort((a, b) -> Integer.compare(gia(a), gia(b)));
                for (int i = 0; i < huy && i < ds.size(); i++) {
                    bo.add(ds.get(i));
                }
                p.iItems.removeAll(bo);
            }
            if (bo.isEmpty()) {
                continue;
            }
            sangNgay();
            VAT_HUY.addAndGet(bo.size());
            for (final Item it : bo) {
                try {
                    if (it.dbid != 0) {
                        Database.instance.deleteItem(it.dbid);
                    }
                } catch (final Exception ignored) {
                }
                Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "hlv ai huy (tran tui) dbid" + it.dbid + " "
                        + (it.getTemplate() == null ? "?" : it.getTemplate().name), "delI");
            }
            try {
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
            } catch (final Throwable ignored) {
            }
        }
    }

    private static int gia(final Item it) {
        try {
            return it.getTemplate() == null ? 0 : it.getTemplate().price;
        } catch (final Throwable e) {
            return 0;
        }
    }

    // =================================================================== thong ke
    static void ghiNgay(final boolean cuoiNgay) {
        if (ngayDem == 0) {
            sangNgay();
            return;
        }
        final AtomicLong[] a = {XU_TAO, XU_CAT, XU_DOT, XU_TIEU, XU_NHAN_NGUOI, XU_TRA_NGUOI, GIA_TRI_NGUOI, SO_CHUYEN, SO_CHAN, VAT_HUY};
        final long[] them = new long[a.length];
        boolean co = false;
        synchronized (DA_GHI) {
            for (int i = 0; i < a.length; i++) {
                final long v = a[i].get();
                them[i] = v - DA_GHI[i];
                DA_GHI[i] = v;
                co |= them[i] != 0L;
            }
        }
        int sb = 0;
        for (final Char p : dsOnline()) {
            if (p.laHlvAi) {
                sb++;
            }
        }
        final int soOnline = sb;
        final String ngay = String.format("%04d-%02d-%02d", ngayDem / 10000, ngayDem / 100 % 100, ngayDem % 100);
        if (!co && !cuoiNgay) {
            capNhat("INSERT INTO " + BANG_NGAY + " (ngay,so_bot_online,cap_nhat) VALUES (?,?,NOW()) ON DUPLICATE KEY UPDATE"
                    + " so_bot_online=GREATEST(so_bot_online,VALUES(so_bot_online)), cap_nhat=NOW()", ngay, soOnline);
            return;
        }
        capNhat("INSERT INTO " + BANG_NGAY + " (ngay,xu_tao,xu_cat,xu_dot,xu_tieu,xu_nhan_nguoi,xu_tra_nguoi,gia_tri_cho_nguoi,"
                + "so_chuyen,so_chan,vat_huy,so_bot_online,cap_nhat) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,NOW()) ON DUPLICATE KEY UPDATE"
                + " xu_tao=xu_tao+VALUES(xu_tao), xu_cat=xu_cat+VALUES(xu_cat), xu_dot=xu_dot+VALUES(xu_dot),"
                + " xu_tieu=xu_tieu+VALUES(xu_tieu), xu_nhan_nguoi=xu_nhan_nguoi+VALUES(xu_nhan_nguoi),"
                + " xu_tra_nguoi=xu_tra_nguoi+VALUES(xu_tra_nguoi), gia_tri_cho_nguoi=gia_tri_cho_nguoi+VALUES(gia_tri_cho_nguoi),"
                + " so_chuyen=so_chuyen+VALUES(so_chuyen), so_chan=so_chan+VALUES(so_chan), vat_huy=vat_huy+VALUES(vat_huy),"
                + " so_bot_online=GREATEST(so_bot_online,VALUES(so_bot_online)), cap_nhat=NOW()",
                ngay, them[0], them[1], them[2], them[3], them[4], them[5], them[6], them[7], them[8], them[9], soOnline);
    }

    /** Tong xu bot / nguoi that (CSDL + so du dang online chua luu) moi 10 phut -> tob_hlv_ai_moc + dong ngay. */
    static void chupMoc() {
        try {
            sangNgay();
            final List<Integer> tk = dsTaiKhoan;
            long botXu = 0L, botKho = 0L, tongXu = 0L, tongKho = 0L;
            int soBot = 0;
            Connection conn = null;
            Statement st = null;
            ResultSet rs = null;
            try {
                conn = Database.instance.getConnection();
                st = conn.createStatement();
                rs = st.executeQuery("SELECT IFNULL(SUM(gold),0), IFNULL(SUM(kho_xu),0) FROM tob_char");
                if (rs.next()) {
                    tongXu = rs.getLong(1);
                    tongKho = rs.getLong(2);
                }
                rs.close();
                if (!tk.isEmpty()) {
                    rs = st.executeQuery("SELECT IFNULL(SUM(gold),0), IFNULL(SUM(kho_xu),0), COUNT(*) FROM tob_char WHERE userid IN (" + noi(tk) + ")");
                    if (rs.next()) {
                        botXu = rs.getLong(1);
                        botKho = rs.getLong(2);
                        soBot = rs.getInt(3);
                    }
                }
            } finally {
                dong(rs, st, conn);
            }
            // so du online moi nhat (CSDL chi luu theo chu ky): thay phan CSDL cua nhan vat dang online
            final Map<Integer, Long> online = new HashMap<Integer, Long>();
            final Map<Integer, Boolean> laB = new HashMap<Integer, Boolean>();
            int bOn = 0, nOn = 0;
            for (final Char p : dsOnline()) {
                if (p.isBot != -1) {
                    continue;
                }
                online.put(p.charDBID, p.getxu());
                laB.put(p.charDBID, p.laHlvAi);
                if (p.laHlvAi) {
                    bOn++;
                } else {
                    nOn++;
                }
            }
            if (!online.isEmpty()) {
                conn = null;
                st = null;
                rs = null;
                try {
                    conn = Database.instance.getConnection();
                    st = conn.createStatement();
                    rs = st.executeQuery("SELECT id, gold FROM tob_char WHERE id IN (" + noi(new ArrayList<Integer>(online.keySet())) + ")");
                    while (rs.next()) {
                        final int id = rs.getInt(1);
                        final long chenh = online.get(id) - rs.getLong(2);
                        tongXu += chenh;
                        if (Boolean.TRUE.equals(laB.get(id))) {
                            botXu += chenh;
                        }
                    }
                } finally {
                    dong(rs, st, conn);
                }
            }
            final long nguoiXu = tongXu - botXu, nguoiKho = tongKho - botKho;
            capNhat("INSERT INTO tob_hlv_ai_moc (luc,tong_xu_bot,tong_xu_nguoi,kho_xu_bot,kho_xu_nguoi,so_bot,so_bot_online,"
                    + "so_nguoi_online,xu_tao_ngay,xu_dot_ngay,xu_tieu_ngay,gia_tri_cho_nguoi_ngay) VALUES (NOW(),?,?,?,?,?,?,?,?,?,?,?)"
                    + " ON DUPLICATE KEY UPDATE tong_xu_bot=VALUES(tong_xu_bot)", botXu, nguoiXu, botKho, nguoiKho, soBot, bOn, nOn,
                    XU_TAO.get(), XU_DOT.get(), XU_TIEU.get(), GIA_TRI_NGUOI.get());
            final String ngay = String.format("%04d-%02d-%02d", ngayDem / 10000, ngayDem / 100 % 100, ngayDem % 100);
            capNhat("INSERT INTO " + BANG_NGAY + " (ngay,tong_xu_bot,tong_xu_nguoi,so_bot,cap_nhat) VALUES (?,?,?,?,NOW())"
                    + " ON DUPLICATE KEY UPDATE tong_xu_bot=VALUES(tong_xu_bot), tong_xu_nguoi=VALUES(tong_xu_nguoi),"
                    + " so_bot=VALUES(so_bot), cap_nhat=NOW()", ngay, botXu + botKho, nguoiXu + nguoiKho, soBot);
        } catch (final Throwable e) {
            System.out.println("[HLV AI] chup moc: " + e);
        }
    }

    /** Nap bo dem han muc hom nay tu tob_hlv_ai_chuyen (khoi dong lai giua ngay khong mo lai han muc). */
    static void napHomNay() {
        sangNgay();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT nguoi_id, SUM(gia_tri) FROM tob_hlv_ai_chuyen WHERE huong = 1 AND bi_chan = 0"
                    + " AND luc >= CURDATE() GROUP BY nguoi_id");
            long tong = 0L;
            while (rs.next()) {
                DA_NGUOI.computeIfAbsent(rs.getInt(1), k -> new AtomicLong()).addAndGet(rs.getLong(2));
                tong += rs.getLong(2);
            }
            DA_TONG.addAndGet(tong);
        } catch (final Exception e) {
            System.out.println("[HLV AI] khong nap duoc han muc hom nay: " + e);
        } finally {
            dong(rs, st, conn);
        }
    }

    // =================================================================== CSDL
    static boolean taoBang() {
        if (daTaoBang) {
            return true;
        }
        Connection conn = null;
        Statement st = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            for (final String s : SQL_TAO) {
                st.executeUpdate(s);
            }
            daTaoBang = true;
        } catch (final Exception e) {
            System.out.println("[HLV AI] khong tao duoc bang: " + e);
        } finally {
            dong(null, st, conn);
        }
        return daTaoBang;
    }

    public static void capNhat(final String sql, final Object... ts) {
        if (!taoBang()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            for (int i = 0; i < ts.length; i++) {
                pre.setObject(i + 1, ts[i]);
            }
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[HLV AI] ghi CSDL: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    private static String noi(final List<Integer> ds) {
        final StringBuilder sb = new StringBuilder();
        for (final Integer i : ds) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(i.intValue());
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }

    private static String cat(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() > n ? s.substring(0, n) : s;
    }

    private static void traCatalog(final Connection conn, final String cu) {
        try {
            if (conn != null && cu != null && !cu.equals(conn.getCatalog())) {
                conn.setCatalog(cu);
            }
        } catch (final Exception ignored) {
        }
    }

    private static void dong(final ResultSet rs, final Statement st, final Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final Exception ignored) {
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (final Exception ignored) {
            }
        }
        if (conn != null) {
            try {
                Database.connPool.free(conn);
            } catch (final Exception ignored) {
            }
        }
    }
}
