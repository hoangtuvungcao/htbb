package com.htbb.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * NHAT KY (log) cua may chu - mat tien nho, khong thu vien ngoai, Java 8.
 *
 * VI SAO CO. Khi dong nguoi choi, stdout (game.out cua systemd) bi nhan chim boi dong lap:
 * "info VKTT: -1, -1, -1, 1" moi lan ve nhan vat, "[GOI3] ..." moi goi tin, "[CLIENT] ..." moi
 * lan dang nhap, cau SQL in nguyen van kem ten nhan vat, va printStackTrace() cua cung mot loi
 * hang nghin lan. Dong loi that bi chon vui, dia day, va I/O dong bo cua System.out lam cham
 * luong game.
 *
 * LAM GI:
 *   - 4 muc: LOI / CANH_BAO / THONG_TIN / GO_LOI. Man hinh (stdout cho systemd) va tep co muc
 *     rieng: sv.log.muc (man hinh), sv.log.mucTep (tep).
 *   - Tat tung LOAI: sv.log.tat=GOI3,VKTT,SQL,... (loai = chu trong [..] dau dong, hoac loai
 *     do ma goi truyen vao). Loai KIEM TOAN (sv.log.kiemToan: ADMIN, NAP, GIFTCODE...) khong
 *     bao gio tat duoc va khong bao gio bi gioi han trong tep kiem toan rieng.
 *   - Gioi han + GOP dong lap: cung MAU (so thay bang #) toi da sv.log.lapToiDa dong moi
 *     sv.log.cuaSoGiay; qua muc thi dem, het cua so in MOT dong "x N lan trong 60s".
 *     Moi loai con mot tran chung sv.log.loaiToiDa dong/cua so (ten nhan vat khac nhau van
 *     la cung loai). Dong LOI khong bi tran loai chan, chi bi gop theo mau.
 *   - Stack trace: in day du LAN DAU moi loai loi (lop + mau thong diep + khung dau) trong
 *     sv.log.cuaSoStackGiay; cac lan sau chi mot dong, cuoi chu ky in so lan lap.
 *   - Ghi BAT DONG BO: luong game chi day vao hang doi (sv.log.hangDoi dong); mot luong rieng
 *     ghi stdout + tep. Tep xoay vong theo ngay, chan dung luong (sv.log.tepToiDaMB x
 *     sv.log.soTepMoiNgay), giu sv.log.giuNgay ngay.
 *   - Lam sach: xuong dong / ky tu dieu khien trong noi dung (ten, chat do nguoi choi go)
 *     khong the gia mao them dong log ({@link #sach(String)}).
 *   - Chan System.out/System.err cu ({@link #caiDat()}): moi println cu cung di qua cac lop
 *     tren, phan loai theo tien to dong (xem {@link #phanLoai(String, boolean)}).
 *
 * Xem docs/cau-hinh-server-ini.md muc "Nhat ky + chong spam".
 */
public final class Nhat {

    public enum Muc {
        LOI('E'), CANH_BAO('W'), THONG_TIN('I'), GO_LOI('D');

        final char kyHieu;

        Muc(final char k) {
            this.kyHieu = k;
        }

        /** Doc ten muc (khong phan biet hoa thuong, chap nhan ca ERROR/WARN/INFO/DEBUG). */
        public static Muc doc(final String s, final Muc macDinh) {
            if (s == null) {
                return macDinh;
            }
            final String t = s.trim().toUpperCase(Locale.ROOT);
            if (t.isEmpty()) {
                return macDinh;
            }
            if (t.equals("ERROR") || t.equals("LOI")) {
                return LOI;
            }
            if (t.equals("WARN") || t.equals("CANH_BAO") || t.equals("CANHBAO")) {
                return CANH_BAO;
            }
            if (t.equals("INFO") || t.equals("THONG_TIN") || t.equals("THONGTIN")) {
                return THONG_TIN;
            }
            if (t.equals("DEBUG") || t.equals("GO_LOI") || t.equals("GOLOI")) {
                return GO_LOI;
            }
            return macDinh;
        }
    }

    /** Ket qua phan loai mot dong cu (System.out/err). */
    public static final class PhanLoai {
        public final String loai;
        public final Muc muc;

        PhanLoai(final String loai, final Muc muc) {
            this.loai = loai;
            this.muc = muc;
        }

        @Override
        public String toString() {
            return this.loai + "/" + this.muc;
        }
    }

    private static final class Dong {
        final String chu;
        final Muc muc;
        final boolean manHinh;
        final boolean tep;
        final boolean kiemToan;

        Dong(final String chu, final Muc muc, final boolean manHinh, final boolean tep, final boolean kiemToan) {
            this.chu = chu;
            this.muc = muc;
            this.manHinh = manHinh;
            this.tep = tep;
            this.kiemToan = kiemToan;
        }
    }

    // ------------------------------------------------------------------ cau hinh (mac dinh)
    public static final int LAP_TOI_DA_MAC_DINH = 5;
    public static final int LOAI_TOI_DA_MAC_DINH = 120;
    public static final int CUA_SO_GIAY_MAC_DINH = 60;
    public static final int CUA_SO_STACK_GIAY_MAC_DINH = 600;
    /** So dong tom tat "gop dong lap" toi da moi loai moi chu ky (phan con lai gop thanh mot dong). */
    static final int TOM_TAT_MOI_LOAI = 3;
    public static final String TAT_MAC_DINH = "GOI3,GOI5,GOI26,VKTT,SQL,LUU_CHAR";
    public static final String KIEM_TOAN_MAC_DINH = "ADMIN,GM,NAP,NAP_TIEN,GIFTCODE,CAM,BAN,KINH_TE,KINHTE,CHONG_SPAM";

    static volatile Muc mucManHinh = Muc.THONG_TIN;
    static volatile Muc mucTep = Muc.THONG_TIN;
    private static volatile Set<String> tat = tachDs(TAT_MAC_DINH);
    private static volatile Set<String> kiemToan = tachDs(KIEM_TOAN_MAC_DINH);
    private static volatile int hangDoiToiDa = 50000;

    static final GioiHanLap MAU = new GioiHanLap(LAP_TOI_DA_MAC_DINH, CUA_SO_GIAY_MAC_DINH * 1000L, 4000);
    static final GioiHanLap LOAI = new GioiHanLap(LOAI_TOI_DA_MAC_DINH, CUA_SO_GIAY_MAC_DINH * 1000L, 512);
    static final GioiHanLap STACK = new GioiHanLap(1, CUA_SO_STACK_GIAY_MAC_DINH * 1000L, 2000);
    private static final ConcurrentHashMap<String, AtomicLong> DEM_TAT = new ConcurrentHashMap<String, AtomicLong>();

    // ------------------------------------------------------------------ chi so (server.ChiSoMayChu doc)
    /** Dong da dua ra (man hinh hoac tep). */
    public static final AtomicLong DONG_GHI = new AtomicLong();
    /** Dong bi gop/bo vi lap lai, vi loai bi tat hoac duoi muc. */
    public static final AtomicLong DONG_BO = new AtomicLong();
    /** Dong muc LOI (ke ca ban bi gop). */
    public static final AtomicLong DONG_LOI = new AtomicLong();
    /** Dong mat vi hang doi day (luong ghi khong theo kip). */
    public static final AtomicLong DONG_MAT = new AtomicLong();

    // ------------------------------------------------------------------ dau ra
    /** stdout THAT (fd 1), lay truoc khi caiDat() thay System.out. */
    private static volatile PrintStream goc = System.out;
    private static volatile ChanDong chanOut;
    private static volatile ChanDong chanErr;
    private static volatile GhiTep tepChinh;
    private static volatile GhiTep tepKiemToan;
    private static volatile ArrayBlockingQueue<Dong> hangDoi = new ArrayBlockingQueue<Dong>(50000);
    private static volatile Thread luong;
    /** Sau khi JVM bat dau tat: ghi dong bo, khong qua hang doi (luong ghi co the da dung). */
    private static volatile boolean dongBo;
    /** Chi cho kiem thu: nhan moi dong ra thay vi stdout/tep. */
    private static volatile Consumer<String> dauRaKiemThu;
    private static final Object KHOA_GHI = new Object();
    private static volatile long mocTatGanNhat = System.currentTimeMillis();

    private Nhat() {
    }

    // ================================================================== API

    public static void loi(final String loai, final String tin) {
        ghi(Muc.LOI, loai, tin, null);
    }

    public static void loi(final String loai, final String tin, final Throwable t) {
        ghi(Muc.LOI, loai, tin, t);
    }

    public static void canhBao(final String loai, final String tin) {
        ghi(Muc.CANH_BAO, loai, tin, null);
    }

    public static void canhBao(final String loai, final String tin, final Throwable t) {
        ghi(Muc.CANH_BAO, loai, tin, t);
    }

    public static void thongTin(final String loai, final String tin) {
        ghi(Muc.THONG_TIN, loai, tin, null);
    }

    public static void goLoi(final String loai, final String tin) {
        ghi(Muc.GO_LOI, loai, tin, null);
    }

    /** Co dang ghi muc nay cho loai nay khong - de khoi dung chuoi ton kem khi tat. */
    public static boolean bat(final Muc muc, final String loai) {
        final String l = chuanLoai(loai);
        if (tat.contains(l) && !kiemToan.contains(l)) {
            return false;
        }
        return muc.ordinal() <= Math.max(mucManHinh.ordinal(), mucTep.ordinal());
    }

    /**
     * Dong KIEM TOAN (lenh admin, nap tien, giftcode, cam/ban, kinh te): luon vao tep
     * kiem_toan-yyyyMMdd.log nguyen ven, khong gioi han, khong tat duoc. Ban sao cho man hinh /
     * tep chung o muc GO_LOI (mac dinh khong hien): GM go lenh lien tuc khong duoc lam ngap stdout,
     * ban day du da nam trong tep kiem toan.
     */
    public static void kiemToan(final String loai, final String tin) {
        final String l = chuanLoai(loai);
        final String chu = dinhDang(Muc.THONG_TIN, l, sach(tin), false);
        dua(new Dong(chu, Muc.THONG_TIN, false, false, true));
        ghi(Muc.GO_LOI, l, tin, null, false);
    }

    public static void ghi(final Muc muc, final String loai, final String tin, final Throwable t) {
        ghi(muc, loai, tin, t, true);
    }

    private static void ghi(final Muc muc, final String loai, final String tin, final Throwable t, final boolean xetKiemToan) {
        final String l = chuanLoai(loai);
        final String noiDung = sach(tin);
        if (xetKiemToan && kiemToan.contains(l)) {
            dua(new Dong(dinhDang(muc, l, noiDung, false), muc, false, false, true));
        }
        if (t != null) {
            final String stack = chuoiStack(t);
            final String khung = khungDau(t);
            xuLyStack(muc, l, noiDung + ": " + t, mau(t.getClass().getName() + ": " + t.getMessage()) + " @ " + khung,
                    dinhDang(muc, l, noiDung, false) + " - " + stack);
            return;
        }
        xuLy(muc, l, noiDung, dinhDang(muc, l, noiDung, false));
    }

    // ================================================================== duong di chung

    /** Dong da phan loai + lam sach -> loc loai, loc muc, gioi han, roi day vao hang doi. */
    private static void xuLy(final Muc muc, final String loai, final String noiDung, final String chu) {
        if (muc == Muc.LOI) {
            DONG_LOI.incrementAndGet();
        }
        if (tat.contains(loai) && !kiemToan.contains(loai)) {
            demTat(loai);
            return;
        }
        final boolean manHinh = muc.ordinal() <= mucManHinh.ordinal();
        final boolean tep = muc.ordinal() <= mucTep.ordinal();
        if (!manHinh && !tep) {
            DONG_BO.incrementAndGet();
            return;
        }
        final long bayGio = System.currentTimeMillis();
        if (!MAU.choPhep(loai + "|" + mau(noiDung), bayGio)) {
            DONG_BO.incrementAndGet();
            return;
        }
        if (muc != Muc.LOI && !LOAI.choPhep(loai, bayGio)) {
            DONG_BO.incrementAndGet();
            return;
        }
        dua(new Dong(chu, muc, manHinh, tep, false));
    }

    private static void xuLyStack(final Muc muc, final String loai, final String motDong, final String khoaStack,
            final String day) {
        DONG_LOI.incrementAndGet();
        if (tat.contains(loai) && !kiemToan.contains(loai)) {
            demTat(loai);
            return;
        }
        final long bayGio = System.currentTimeMillis();
        final boolean manHinh = muc.ordinal() <= mucManHinh.ordinal();
        final boolean tep = muc.ordinal() <= mucTep.ordinal();
        if (!manHinh && !tep) {
            DONG_BO.incrementAndGet();
            return;
        }
        if (STACK.choPhep(khoaStack, bayGio)) {
            dua(new Dong(day, muc, manHinh, tep, false));
            return;
        }
        // Lan lap: mot dong ngan (cung bi gop theo mau), stack da in roi.
        if (!MAU.choPhep(loai + "|S|" + khoaStack, bayGio)) {
            DONG_BO.incrementAndGet();
            return;
        }
        dua(new Dong(dinhDang(muc, loai, motDong + " (stack da in, lap lai)", false), muc, manHinh, tep, false));
    }

    private static void demTat(final String loai) {
        DONG_BO.incrementAndGet();
        AtomicLong d = DEM_TAT.get(loai);
        if (d == null) {
            final AtomicLong moi = new AtomicLong();
            d = DEM_TAT.putIfAbsent(loai, moi);
            if (d == null) {
                d = moi;
            }
        }
        d.incrementAndGet();
    }

    // ================================================================== dong cu tu System.out/err

    private static final Pattern SQL = Pattern.compile(
            "^(?i)(select|update|insert|delete|replace|create|drop|alter|truncate|call)\\s.*");
    private static final Pattern CO_LOI = Pattern.compile(
            "(?i).*(exception|\\berror\\b|\\bloi\\b|\\blỗi\\b|that bai|thất bại|khong ghi duoc|khong doc duoc|chua doc duoc|sqlite error).*");

    /**
     * Phan loai mot dong cu theo noi dung:
     *   "[ABC DEF] ..."       -> loai ABC_DEF
     *   cau SQL tran          -> SQL, GO_LOI (khong bao gio la thong tin cho van hanh);
     *                            SQL ERROR / cau SQL kem chu loi -> SQL_LOI (khong bi tat cung SQL)
     *   "info VKTT..."        -> VKTT, GO_LOI
     *   "SAVE CHARSELL" ...   -> LUU_CHAR, GO_LOI
     *   chua exception/error/loi -> muc LOI (stderr) hoac CANH_BAO (stdout)
     *   con lai               -> CU (stdout, THONG_TIN) hoac ERR (stderr, CANH_BAO)
     */
    public static PhanLoai phanLoai(final String dongGoc, final boolean err) {
        String s = dongGoc == null ? "" : dongGoc.trim();
        while (s.startsWith(". ") || s.startsWith(".[")) {
            s = s.substring(1).trim();
        }
        String loai = null;
        if (s.startsWith("[")) {
            final int dong = s.indexOf(']');
            if (dong > 1 && dong <= 24) {
                loai = chuanLoai(s.substring(1, dong));
            }
        }
        final boolean coLoi = CO_LOI.matcher(s).matches();
        if (loai == null) {
            if (SQL.matcher(s).matches()) {
                // Cau SQL co chu loi -> loai rieng SQL_LOI: SQL nam trong sv.log.tat mac dinh,
                // khong duoc de loi SQL bi tat theo.
                return coLoi ? new PhanLoai("SQL_LOI", Muc.CANH_BAO) : new PhanLoai("SQL", Muc.GO_LOI);
            }
            if (s.startsWith("info VKTT")) {
                return new PhanLoai("VKTT", Muc.GO_LOI);
            }
            if (s.startsWith("SAVE CHARSELL") || s.equals("savechar") || s.startsWith("SAVE XONG")) {
                return new PhanLoai("LUU_CHAR", Muc.GO_LOI);
            }
            if (s.startsWith("SQL ERROR")) {
                return new PhanLoai("SQL_LOI", Muc.LOI);
            }
            loai = err ? "ERR" : "CU";
        }
        if (coLoi) {
            return new PhanLoai(loai, err ? Muc.LOI : Muc.CANH_BAO);
        }
        return new PhanLoai(loai, err ? Muc.CANH_BAO : Muc.THONG_TIN);
    }

    static void tuDongCu(final String dong, final boolean err) {
        if (dong.isEmpty()) {
            return; // println() trong: khong co thong tin nao
        }
        final String s = sachGiuTab(dong);
        final PhanLoai pl = phanLoai(s, err);
        if (kiemToan.contains(pl.loai)) {
            dua(new Dong(dinhDang(pl.muc, pl.loai, s, true), pl.muc, false, false, true));
        }
        xuLy(pl.muc, pl.loai, s, dinhDang(pl.muc, pl.loai, s, true));
    }

    static void tuStackCu(final String dau, final String khung, final String khoi) {
        final String s = sachGiuTab(khoi);
        final String d = sachGiuTab(dau);
        xuLyStack(Muc.LOI, "STACK", d, mau(d) + " @ " + (khung == null ? "?" : khung), dinhDang(Muc.LOI, "STACK", s, true));
    }

    // ================================================================== tien ich chuoi

    /**
     * Lam sach noi dung truoc khi ghi: xuong dong thanh "\n" chu (khong the gia mao dong log
     * moi), ky tu dieu khien / ESC (ma mau ANSI) / U+2028-2029 thanh '?', cat o 2000 ky tu.
     * DUNG cho moi chuoi do nguoi choi go (ten, chat, tieu de...) truoc khi dua vao log.
     */
    public static String sach(final String s) {
        return sach(s, false);
    }

    static String sachGiuTab(final String s) {
        return sach(s, true);
    }

    private static String sach(final String s, final boolean giuTabXuongDong) {
        if (s == null) {
            return "null";
        }
        final int n = s.length();
        StringBuilder b = null;
        for (int i = 0; i < n; i++) {
            final char c = s.charAt(i);
            final boolean xau = c < 0x20 || c == 0x7f || (c >= 0x80 && c <= 0x9f) || c == ' ' || c == ' ';
            if (xau && !(giuTabXuongDong && (c == '\t' || c == '\n'))) {
                if (b == null) {
                    b = new StringBuilder(n + 8);
                    b.append(s, 0, i);
                }
                if (c == '\n') {
                    b.append("\\n");
                } else if (c == '\r') {
                    b.append("\\r");
                } else if (c == '\t') {
                    b.append(' ');
                } else {
                    b.append('?');
                }
            } else if (b != null) {
                b.append(c);
            }
        }
        String r = b == null ? s : b.toString();
        final int toiDa = giuTabXuongDong ? 20000 : 2000;
        if (r.length() > toiDa) {
            r = r.substring(0, toiDa) + "...(+" + (r.length() - toiDa) + ")";
        }
        return r;
    }

    /**
     * MAU cua mot dong de gop: moi "tu" (chuoi chu/so lien nhau) CO CHUA chu so -> '#'
     * (so, ten kieu nhk001, id, IP), chuoi trong '...' -> '?', cat 160 ky tu.
     * "update x set nPK=3 where charname='abc'" va "...nPK=7 ... 'xyz'" cung mau.
     */
    public static String mau(final String s) {
        if (s == null) {
            return "";
        }
        final StringBuilder b = new StringBuilder(Math.min(s.length(), 170));
        final int n = s.length();
        int i = 0;
        while (i < n && b.length() < 160) {
            final char c = s.charAt(i);
            if (c == '\'') {
                final int dong = s.indexOf('\'', i + 1);
                b.append('?');
                i = dong < 0 ? n : dong + 1;
                continue;
            }
            if (Character.isLetterOrDigit(c) || c == '_') {
                int j = i;
                boolean coSo = false;
                while (j < n && (Character.isLetterOrDigit(s.charAt(j)) || s.charAt(j) == '_')) {
                    coSo |= Character.isDigit(s.charAt(j));
                    j++;
                }
                if (coSo) {
                    b.append('#');
                } else {
                    b.append(s, i, Math.min(j, i + 160 - b.length()));
                }
                i = j;
                continue;
            }
            b.append(c);
            i++;
        }
        return b.toString();
    }

    static String chuanLoai(final String loai) {
        if (loai == null || loai.isEmpty()) {
            return "CHUNG";
        }
        final StringBuilder b = new StringBuilder(loai.length());
        for (int i = 0; i < loai.length() && b.length() < 24; i++) {
            final char c = Character.toUpperCase(loai.charAt(i));
            if (Character.isLetterOrDigit(c)) {
                b.append(c);
            } else if ((c == ' ' || c == '_' || c == '-') && b.length() > 0 && b.charAt(b.length() - 1) != '_') {
                b.append('_');
            }
        }
        while (b.length() > 0 && b.charAt(b.length() - 1) == '_') {
            b.setLength(b.length() - 1);
        }
        return b.length() == 0 ? "CHUNG" : b.toString();
    }

    static Set<String> tachDs(final String ds) {
        final Set<String> r = new HashSet<String>();
        if (ds != null) {
            for (final String x : ds.split(",")) {
                if (!x.trim().isEmpty()) {
                    r.add(chuanLoai(x.trim()));
                }
            }
        }
        return Collections.unmodifiableSet(r);
    }

    private static final ThreadLocal<SimpleDateFormat> GIO = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        }
    };

    /** "10-18 18:00:01.123 I [LOAI] noi dung"; dong cu da co [..] thi khong them loai. */
    private static String dinhDang(final Muc muc, final String loai, final String noiDung, final boolean cu) {
        final String gio = GIO.get().format(new Date());
        if (cu) {
            return gio + " " + muc.kyHieu + " " + noiDung;
        }
        return gio + " " + muc.kyHieu + " [" + loai + "] " + noiDung;
    }

    private static String chuoiStack(final Throwable t) {
        final StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String s = sw.toString();
        if (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        return sachGiuTab(s);
    }

    private static String khungDau(final Throwable t) {
        final StackTraceElement[] st = t.getStackTrace();
        return st != null && st.length > 0 ? st[0].toString() : "?";
    }

    // ================================================================== hang doi + luong ghi

    private static void dua(final Dong d) {
        final Consumer<String> kt = dauRaKiemThu;
        if (kt != null) {
            DONG_GHI.incrementAndGet();
            kt.accept((d.kiemToan ? "KIEM_TOAN " : "") + d.chu);
            return;
        }
        if (dongBo) {
            synchronized (KHOA_GHI) {
                viet(d, System.currentTimeMillis());
                xaHet();
            }
            return;
        }
        batLuong();
        if (!hangDoi.offer(d)) {
            if (d.muc == Muc.LOI || d.kiemToan) {
                try {
                    if (hangDoi.offer(d, 50, TimeUnit.MILLISECONDS)) {
                        return;
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            DONG_MAT.incrementAndGet();
        }
    }

    private static void viet(final Dong d, final long bayGio) {
        DONG_GHI.incrementAndGet();
        if (d.manHinh) {
            goc.println(d.chu);
        }
        if (d.tep && tepChinh != null) {
            tepChinh.ghi(d.chu, d.muc == Muc.LOI, bayGio);
        }
        if (d.kiemToan && tepKiemToan != null) {
            tepKiemToan.ghi(d.chu, true, bayGio);
        }
    }

    private static void xaHet() {
        goc.flush();
        if (tepChinh != null) {
            tepChinh.xa();
        }
        if (tepKiemToan != null) {
            tepKiemToan.xa();
        }
    }

    private static synchronized void batLuong() {
        if (luong != null) {
            return;
        }
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                vongGhi();
            }
        }, "NHAT KY");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY - 1);
        luong = t;
        t.start();
    }

    private static void vongGhi() {
        final List<Dong> lo = new ArrayList<Dong>(1024);
        long mocTomTat = System.currentTimeMillis();
        while (!dongBo) {
            try {
                final Dong d = hangDoi.poll(250, TimeUnit.MILLISECONDS);
                final long bayGio = System.currentTimeMillis();
                if (d != null) {
                    lo.add(d);
                    hangDoi.drainTo(lo, 8192);
                }
                synchronized (KHOA_GHI) {
                    for (int i = 0; i < lo.size(); i++) {
                        viet(lo.get(i), bayGio);
                    }
                    if (!lo.isEmpty()) {
                        xaHet();
                    }
                }
                lo.clear();
                final ChanDong o = chanOut;
                final ChanDong e = chanErr;
                if (o != null) {
                    o.xaNeuCu(bayGio);
                }
                if (e != null) {
                    e.xaNeuCu(bayGio);
                }
                if (bayGio - mocTomTat >= 1000L) {
                    mocTomTat = bayGio;
                    tomTat(bayGio);
                }
            } catch (final InterruptedException ie) {
                return;
            } catch (final Throwable t) {
                manHinhGoc("[NHAT] luong ghi log loi: " + t);
            }
        }
    }

    /** Dua cac dong tom tat "x N lan" (khong qua bo gioi han - no CHINH la ban gop). */
    static void tomTat(final long bayGio) {
        // Ban tom tat cung phai gon: moi loai toi da TOM_TAT_MOI_LOAI dong (mau bi bo nhieu nhat),
        // phan con lai gop mot dong. Lan lap cua stack (|S|) da co dong "loi lap lai" cua STACK.
        final Map<String, List<GioiHanLap.TomTat>> theoLoai = new TreeMap<String, List<GioiHanLap.TomTat>>();
        for (final GioiHanLap.TomTat t : MAU.tomTat(bayGio)) {
            if (t.khoa.contains("|S|")) {
                continue;
            }
            final int g = t.khoa.indexOf('|');
            final String loai = g > 0 ? t.khoa.substring(0, g) : t.khoa;
            List<GioiHanLap.TomTat> ds = theoLoai.get(loai);
            if (ds == null) {
                ds = new ArrayList<GioiHanLap.TomTat>();
                theoLoai.put(loai, ds);
            }
            ds.add(t);
        }
        for (final Map.Entry<String, List<GioiHanLap.TomTat>> e : theoLoai.entrySet()) {
            final List<GioiHanLap.TomTat> ds = e.getValue();
            Collections.sort(ds, new java.util.Comparator<GioiHanLap.TomTat>() {
                @Override
                public int compare(final GioiHanLap.TomTat x, final GioiHanLap.TomTat y) {
                    return y.soBo - x.soBo;
                }
            });
            long boCon = 0;
            for (int i = 0; i < ds.size(); i++) {
                if (i < TOM_TAT_MOI_LOAI) {
                    dua(new Dong(dinhDang(Muc.THONG_TIN, "NHAT", "gop dong lap " + ds.get(i), false), Muc.THONG_TIN, true, true, false));
                } else {
                    boCon += ds.get(i).soBo;
                }
            }
            if (ds.size() > TOM_TAT_MOI_LOAI) {
                dua(new Dong(dinhDang(Muc.THONG_TIN, "NHAT", "gop dong lap loai " + e.getKey() + ": them " + (ds.size() - TOM_TAT_MOI_LOAI)
                        + " mau khac, bo bot " + boCon + " dong", false), Muc.THONG_TIN, true, true, false));
            }
        }
        for (final GioiHanLap.TomTat t : LOAI.tomTat(bayGio)) {
            dua(new Dong(dinhDang(Muc.THONG_TIN, "NHAT", "loai " + t.khoa + " vuot tran " + LOAI.toiDa() + " dong/"
                    + (t.cuaSoMs / 1000L) + "s, bo bot " + t.soBo + " dong", false), Muc.THONG_TIN, true, true, false));
        }
        for (final GioiHanLap.TomTat t : STACK.tomTat(bayGio)) {
            dua(new Dong(dinhDang(Muc.LOI, "NHAT", "loi lap lai " + (t.soChoPhep + t.soBo) + " lan trong "
                    + (t.cuaSoMs / 1000L) + "s (stack chi in lan dau): " + t.khoa, false), Muc.LOI, true, true, false));
        }
        if (bayGio - mocTatGanNhat >= STACK.cuaSoMs() && !DEM_TAT.isEmpty()) {
            mocTatGanNhat = bayGio;
            final Map<String, Long> ds = new TreeMap<String, Long>();
            for (final Map.Entry<String, AtomicLong> e : DEM_TAT.entrySet()) {
                final long n = e.getValue().getAndSet(0);
                if (n > 0) {
                    ds.put(e.getKey(), n);
                }
            }
            if (!ds.isEmpty()) {
                dua(new Dong(dinhDang(Muc.THONG_TIN, "NHAT", "loai dang tat (sv.log.tat) da bo: " + ds, false),
                        Muc.THONG_TIN, false, true, false));
            }
        }
    }

    /** In thang ra stdout that, khong qua gi ca (bao loi cua chinh bo log). */
    static void manHinhGoc(final String s) {
        try {
            goc.println(s);
            goc.flush();
        } catch (final Throwable ignored) {
        }
    }

    // ================================================================== cai dat / cau hinh

    /**
     * Thay System.out / System.err bang bo chan dong (goi DAU TIEN trong main). server.LocBiMat
     * boc ra ngoai sau do van che mat khau truoc khi dong toi day.
     */
    public static synchronized void caiDat() {
        if (chanOut != null) {
            return;
        }
        try {
            // Dem 64KB: luong ghi xa (flush) sau moi lo, khong phai mot lenh he thong moi dong.
            goc = new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 65536), false, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            goc = new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 65536), false);
        }
        chanOut = new ChanDong(false);
        chanErr = new ChanDong(true);
        try {
            System.setOut(new PrintStream(chanOut, true, "UTF-8"));
            System.setErr(new PrintStream(chanErr, true, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            System.setOut(new PrintStream(chanOut, true));
            System.setErr(new PrintStream(chanErr, true));
        }
        Runtime.getRuntime().addShutdownHook(new Thread("NHAT KY TAT") {
            @Override
            public void run() {
                dongBang();
            }
        });
    }

    /**
     * Doc cau hinh sv.log.* tu server.ini. Goi lai duoc (setting 8 / nap lai). Thieu khoa = mac dinh.
     *
     * @param thuMucLog sv.pathLog (co / cuoi) - tep nam trong &lt;thuMucLog&gt;/nhat_ky/ tru khi
     *                  sv.log.thuMuc dat rieng. null/rong = khong ghi tep (chi stdout).
     */
    public static synchronized void docCauHinh(final Properties p, final String thuMucLog) {
        mucManHinh = Muc.doc(p.getProperty("sv.log.muc"), Muc.THONG_TIN);
        mucTep = Muc.doc(p.getProperty("sv.log.mucTep"), mucManHinh);
        tat = tachDs(p.getProperty("sv.log.tat", TAT_MAC_DINH));
        kiemToan = tachDs(p.getProperty("sv.log.kiemToan", KIEM_TOAN_MAC_DINH));
        final int cuaSo = Math.max(1, so(p, "sv.log.cuaSoGiay", CUA_SO_GIAY_MAC_DINH));
        MAU.dat(so(p, "sv.log.lapToiDa", LAP_TOI_DA_MAC_DINH), cuaSo * 1000L);
        LOAI.dat(so(p, "sv.log.loaiToiDa", LOAI_TOI_DA_MAC_DINH), cuaSo * 1000L);
        STACK.dat(1, Math.max(10, so(p, "sv.log.cuaSoStackGiay", CUA_SO_STACK_GIAY_MAC_DINH)) * 1000L);
        final int hd = Math.max(1000, so(p, "sv.log.hangDoi", 50000));
        if (hd != hangDoiToiDa && hangDoi.isEmpty()) {
            hangDoiToiDa = hd;
            hangDoi = new ArrayBlockingQueue<Dong>(hd);
        }
        String thuMuc = p.getProperty("sv.log.thuMuc", "").trim();
        if (thuMuc.isEmpty() && thuMucLog != null && !thuMucLog.trim().isEmpty()) {
            thuMuc = new File(thuMucLog.trim(), "nhat_ky").getPath();
        }
        final boolean tatTep = "0".equals(p.getProperty("sv.log.tep", "1").trim());
        synchronized (KHOA_GHI) {
            if (tepChinh != null) {
                tepChinh.dong();
            }
            if (tepKiemToan != null) {
                tepKiemToan.dong();
            }
            tepChinh = null;
            tepKiemToan = null;
            if (!thuMuc.isEmpty()) {
                final File d = new File(thuMuc);
                final long toiDa = Math.max(1, so(p, "sv.log.tepToiDaMB", 100)) * 1024L * 1024L;
                final int soTep = Math.max(1, so(p, "sv.log.soTepMoiNgay", 5));
                final int giu = Math.max(1, so(p, "sv.log.giuNgay", 14));
                if (!tatTep) {
                    tepChinh = new GhiTep(d, "may_chu", toiDa, soTep, giu);
                }
                // Kiem toan: KHONG theo sv.log.tep, giu lau hon (mac dinh 90 ngay), khong xoay theo dung luong nho.
                tepKiemToan = new GhiTep(d, "kiem_toan", Math.max(toiDa, 200L * 1024L * 1024L), 20,
                        Math.max(giu, so(p, "sv.log.giuNgayKiemToan", 90)));
            }
        }
        thongTin("NHAT", "nhat ky: man hinh>=" + mucManHinh + ", tep>=" + mucTep + (tepChinh == null ? " (khong ghi tep)" : " -> " + thuMuc)
                + ", tat=" + tat + ", gop " + MAU.toiDa() + " dong/mau + " + LOAI.toiDa() + " dong/loai moi " + cuaSo
                + "s, stack 1 lan/" + (STACK.cuaSoMs() / 1000L) + "s");
    }

    private static int so(final Properties p, final String k, final int macDinh) {
        try {
            final String v = p.getProperty(k);
            return v == null || v.trim().isEmpty() ? macDinh : Integer.parseInt(v.trim());
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }

    /** Xa het hang doi + khoi stack dang treo, roi chuyen sang ghi dong bo (shutdown hook). */
    public static void dongBang() {
        final ChanDong o = chanOut;
        final ChanDong e = chanErr;
        if (o != null) {
            o.xaNeuCu(Long.MAX_VALUE);
        }
        if (e != null) {
            e.xaNeuCu(Long.MAX_VALUE);
        }
        tomTat(Long.MAX_VALUE / 2);
        synchronized (KHOA_GHI) {
            dongBo = true;
            final List<Dong> con = new ArrayList<Dong>();
            hangDoi.drainTo(con);
            final long bayGio = System.currentTimeMillis();
            for (final Dong d : con) {
                viet(d, bayGio);
            }
            xaHet();
        }
    }

    // ================================================================== cho kiem thu

    /** Kiem thu: moi dong ra di thang vao {@code ra} (dong bo), null = tro lai binh thuong. */
    public static void datDauRaKiemThu(final Consumer<String> ra) {
        dauRaKiemThu = ra;
    }

    /** Kiem thu: dat lai cau hinh + bo dem ve mac dinh. */
    public static synchronized void datLaiChoKiemThu(final Properties p) {
        MAU.tomTat(Long.MAX_VALUE / 2);
        LOAI.tomTat(Long.MAX_VALUE / 2);
        STACK.tomTat(Long.MAX_VALUE / 2);
        DEM_TAT.clear();
        mocTatGanNhat = System.currentTimeMillis();
        docCauHinh(p == null ? new Properties() : p, null);
    }

    /** Kiem thu: chay mot vong tom tat voi dong ho gia. */
    public static void tomTatKiemThu(final long bayGio) {
        tomTat(bayGio);
    }

    /** Kiem thu: dua mot dong "cu" nhu the no vua di qua System.out / System.err. */
    public static void giaLapDongCu(final String dong, final boolean err) {
        final ChanDong c = new ChanDong(err);
        c.nhanDong(dong);
        c.xaNeuCu(Long.MAX_VALUE);
    }

    /** Kiem thu: dua nhieu dong lien nhau qua MOT bo chan (de gom stack trace). */
    public static void giaLapNhieuDongCu(final String[] dong, final boolean err) {
        final ChanDong c = new ChanDong(err);
        for (final String d : dong) {
            c.nhanDong(d);
        }
        c.xaNeuCu(Long.MAX_VALUE);
    }
}
