package com.htbb.chongspam;

import java.text.Normalizer;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * CHONG SPAM phia nguoi choi: chat (gan / rieng / the gioi / doi / bang) va thao tac lap
 * (moi to doi, moi bang, ket ban, moi giao dich, menu NPC, rao ban, mua ban, dang ky).
 *
 * Logic THUAN (thoi gian truyen vao, trang thai la doi tuong rieng) de kiem thu khong can
 * socket - xem test/KiemNhatKyChongSpam.java. Phan noi vao game nam o real.MapXaHoi,
 * real.MapChat, real.MapNhapText, real.MapXuLyLenh.
 *
 * CHAT, theo thu tu:
 *   1. dang bi cam (tu dong) -> DANG_CAM
 *   2. quang cao / link (http, www, ten mien .com/.net/.vn..., zalo/fb, so dien thoai, IP)
 *      -> QUANG_CAO, tinh 2 vi pham
 *   3. vuot nhip cua kenh (cua so truot: sv.spam.chat.gan=5/10 = 5 tin moi 10 giay...)
 *      -> QUA_NHANH, 1 vi pham
 *   4. trung tin vua gui (chuan hoa: thuong, bo dau, bo ky tu lap) trong sv.spam.trungLapGiay
 *      -> TRUNG_LAP, 1 vi pham
 *   5. kenh the gioi con them tran CHUNG toan may chu (sv.spam.theGioiToanServer / phut)
 *      -> KENH_BAN (khong tinh vi pham - khong phai loi cua nguoi gui)
 *   Du sv.spam.viPhamCam vi pham (quen sau sv.spam.viPhamQuenGiay) -> tu dong cam chat, thoi
 *   gian TANG DAN theo bac: sv.spam.camGiay=60,300,1800,7200 (bac ha ve 0 sau 24h sach).
 *
 * Bao cho nguoi bi chan toi da 1 lan / 5 giay ({@link #nenBao}) - khong thi chinh loi bao lai
 * thanh spam server -> client.
 */
public final class ChongSpam {

    public enum Kenh {
        GAN, RIENG, THE_GIOI, DOI, BANG
    }

    public enum KetQua {
        CHO_PHEP, DANG_CAM, QUA_NHANH, TRUNG_LAP, QUANG_CAO, KENH_BAN,
        /** 0.9.5: tin rỗng sau khi bỏ khoảng trắng / ký tự vô hình - không phát, KHÔNG tính nhịp / vi phạm */
        RONG
    }

    /** Thao tac co gioi han nhip rieng. */
    public enum HanhDong {
        MOI_DOI, MOI_BANG, KET_BAN, MOI_GIAO_DICH, NPC, RAO_BAN, MUA_BAN, DANG_KY
    }

    /** Cua so truot: toi da {@code so} lan trong {@code ms}. */
    static final class Nhip {
        final int so;
        final long ms;

        Nhip(final int so, final long ms) {
            this.so = so;
            this.ms = ms;
        }

        static Nhip doc(final String s, final Nhip macDinh) {
            if (s == null || s.trim().isEmpty()) {
                return macDinh;
            }
            try {
                final String[] a = s.trim().split("/");
                final int n = Integer.parseInt(a[0].trim());
                final long giay = a.length > 1 ? Long.parseLong(a[1].trim()) : 10L;
                return new Nhip(n, Math.max(1L, giay) * 1000L);
            } catch (final NumberFormatException e) {
                return macDinh;
            }
        }

        @Override
        public String toString() {
            return this.so + "/" + (this.ms / 1000L) + "s";
        }
    }

    /** Vong dem moc thoi gian cho mot cua so truot. */
    static final class Vong {
        long[] moc;
        int vt;

        /** @return true = con cho (va da ghi nhan lan nay). so <= 0 = khong gioi han. */
        boolean choPhep(final Nhip n, final long bayGio) {
            if (n.so <= 0) {
                return true;
            }
            if (this.moc == null || this.moc.length != n.so) {
                this.moc = new long[n.so];
                this.vt = 0;
            }
            final long cu = this.moc[this.vt];
            if (cu != 0L && bayGio - cu < n.ms) {
                return false;
            }
            this.moc[this.vt] = bayGio;
            this.vt = (this.vt + 1) % this.moc.length;
            return true;
        }
    }

    /** Trang thai chong spam cua MOT nguoi choi (hoac mot ket noi / IP). */
    public static final class TrangThai {
        final Vong[] kenh = new Vong[Kenh.values().length];
        final Vong[] hanhDong = new Vong[HanhDong.values().length];
        String tinCuoi;
        long mocTinCuoi;
        int viPham;
        long mocViPham;
        int bac;
        long mocBac;
        long camDen;
        long mocBaoCuoi;
        volatile long dungCuoi;

        public TrangThai() {
            for (int i = 0; i < this.kenh.length; i++) {
                this.kenh[i] = new Vong();
            }
            for (int i = 0; i < this.hanhDong.length; i++) {
                this.hanhDong[i] = new Vong();
            }
        }

        /** Con bi cam bao nhieu ms nua (0 = khong). */
        public synchronized long conCam(final long bayGio) {
            return Math.max(0L, this.camDen - bayGio);
        }

        public synchronized int bac() {
            return this.bac;
        }
    }

    // ------------------------------------------------------------------ cau hinh (mac dinh)
    public static volatile boolean bat = true;
    static final Nhip[] NHIP_KENH_MAC_DINH = {
        new Nhip(5, 10000L),   // GAN: 5 tin / 10 giay
        new Nhip(6, 10000L),   // RIENG
        new Nhip(1, 30000L),   // THE_GIOI (loa): 1 tin / 30 giay moi nguoi (Char.LOA_COOLDOWN_MS van ap)
        new Nhip(8, 10000L),   // DOI
        new Nhip(8, 10000L),   // BANG
    };
    static final Nhip[] NHIP_HANH_DONG_MAC_DINH = {
        new Nhip(4, 10000L),   // MOI_DOI
        new Nhip(4, 10000L),   // MOI_BANG
        new Nhip(4, 10000L),   // KET_BAN
        new Nhip(4, 10000L),   // MOI_GIAO_DICH
        new Nhip(25, 5000L),   // NPC (lenh 53/55)
        new Nhip(6, 30000L),   // RAO_BAN (lenh 62/92)
        new Nhip(25, 5000L),   // MUA_BAN (lenh 24/28/95)
        new Nhip(5, 60000L),   // DANG_KY (moi ket noi, ngoai han muc IP cua DangKyTrongGame)
    };
    static final Nhip THE_GIOI_TOAN_SERVER_MAC_DINH = new Nhip(12, 60000L);
    static volatile Nhip[] nhipKenh = NHIP_KENH_MAC_DINH.clone();
    static volatile Nhip[] nhipHanhDong = NHIP_HANH_DONG_MAC_DINH.clone();
    static volatile Nhip theGioiToanServer = THE_GIOI_TOAN_SERVER_MAC_DINH;
    static volatile long trungLapMs = 30000L;
    static volatile int viPhamCam = 4;
    static volatile long viPhamQuenMs = 600000L;
    static volatile long[] camMs = {60000L, 300000L, 1800000L, 7200000L};
    static volatile long bacQuenMs = 86400000L;
    static volatile boolean chanQuangCao = true;
    static volatile long baoCachMs = 5000L;
    private static volatile Pattern quangCaoThem = null;

    private static final Vong THE_GIOI = new Vong();

    /** http(s)://, www., ten mien pho bien, zalo/facebook/telegram, so dien thoai, dia chi IP. */
    private static final Pattern QUANG_CAO = Pattern.compile(
            "(?i)(https?\\s*:\\s*/\\s*/|www\\s*\\.|\\b[a-z0-9-]{2,}\\s*(\\.|\\(\\s*dot\\s*\\)|\\s+cham\\s+)\\s*"
            + "(com|net|org|vn|xyz|tk|ml|ga|cf|gq|top|info|me|io|club|online|site|shop|store|pro|biz|cc|ru|asia)\\b"
            + "|\\b(zalo|zl|fb\\.com|facebook\\.com|t\\.me|telegram|discord\\.gg|tiktok\\.com)\\b"
            + "|(?<!\\d)(0|\\+?84)[\\s.-]?\\d{2,3}[\\s.-]?\\d{3}[\\s.-]?\\d{3,4}(?!\\d)"
            + "|\\b\\d{1,3}\\s*\\.\\s*\\d{1,3}\\s*\\.\\s*\\d{1,3}\\s*\\.\\s*\\d{1,3}\\b)");

    private static final Map<Object, TrangThai> BANG = new ConcurrentHashMap<Object, TrangThai>();
    private static volatile long mocDon = System.currentTimeMillis();

    private ChongSpam() {
    }

    // ================================================================== chat

    /**
     * Kiem mot tin chat TRUOC khi phat. Ghi nhan luon (nhip, vi pham, cam tu dong).
     *
     * @param tin noi dung GOC nguoi choi go (truoc moi bo loc tu xau / bo ky tu) - bo loc cu
     *            xoa dau cham nen "abc.com" thanh "abccom", kiem sau do la lot.
     */
    public static KetQua kiemChat(final TrangThai t, final Kenh k, final String tin, final long bayGio) {
        if (!bat || t == null) {
            return KetQua.CHO_PHEP;
        }
        if (rong(tin)) {
            return KetQua.RONG;   // không ghi nhận gì (không tính nhịp, không vi phạm)
        }
        synchronized (t) {
            t.dungCuoi = bayGio;
            if (t.camDen > bayGio) {
                return KetQua.DANG_CAM;
            }
            final String s = tin == null ? "" : tin;
            if (chanQuangCao && laQuangCao(s)) {
                viPham(t, 2, bayGio);
                return KetQua.QUANG_CAO;
            }
            if (!t.kenh[k.ordinal()].choPhep(nhipKenh[k.ordinal()], bayGio)) {
                viPham(t, 1, bayGio);
                return KetQua.QUA_NHANH;
            }
            final String chuan = chuanHoa(s);
            if (trungLapMs > 0 && chuan.length() > 0 && chuan.equals(t.tinCuoi) && bayGio - t.mocTinCuoi < trungLapMs) {
                t.mocTinCuoi = bayGio;
                viPham(t, 1, bayGio);
                return KetQua.TRUNG_LAP;
            }
            t.tinCuoi = chuan;
            t.mocTinCuoi = bayGio;
        }
        if (k == Kenh.THE_GIOI) {
            synchronized (THE_GIOI) {
                if (!THE_GIOI.choPhep(theGioiToanServer, bayGio)) {
                    return KetQua.KENH_BAN;
                }
            }
        }
        return KetQua.CHO_PHEP;
    }

    private static void viPham(final TrangThai t, final int trongSo, final long bayGio) {
        if (bayGio - t.mocViPham > viPhamQuenMs) {
            t.viPham = 0;
        }
        t.viPham += trongSo;
        t.mocViPham = bayGio;
        if (viPhamCam > 0 && t.viPham >= viPhamCam) {
            if (bayGio - t.mocBac > bacQuenMs) {
                t.bac = 0;
            }
            final long[] m = camMs;
            t.camDen = bayGio + m[Math.min(t.bac, m.length - 1)];
            t.bac++;
            t.mocBac = bayGio;
            t.viPham = 0;
        }
    }

    /**
     * 0.9.5: tin RỖNG - không còn ký tự thấy được sau khi bỏ khoảng trắng (cả NBSP, U+2000..U+200A...), ký tự vô hình
     * (zero-width U+200B..U+200D, U+2060, U+FEFF, ký tự định dạng), ký tự điều khiển và dấu kết hợp đứng một mình.
     */
    public static boolean rong(final String tin) {
        if (tin == null) {
            return true;
        }
        for (int i = 0; i < tin.length(); ) {
            final int c = tin.codePointAt(i);
            i += Character.charCount(c);
            if (Character.isWhitespace(c) || Character.isSpaceChar(c) || Character.isISOControl(c)) {
                continue;
            }
            final int loai = Character.getType(c);
            if (loai == Character.FORMAT || loai == Character.NON_SPACING_MARK || loai == Character.ENCLOSING_MARK
                    || loai == Character.COMBINING_SPACING_MARK || loai == Character.CONTROL || c == 0xFEFF) {
                continue;
            }
            return false;
        }
        return true;
    }

    /** Cau bao cho nguoi gui (tieng Viet), null = khong can bao. */
    public static String lyDo(final KetQua kq, final TrangThai t, final long bayGio) {
        switch (kq) {
            case DANG_CAM: {
                final long giay = (t.conCam(bayGio) + 999L) / 1000L;
                return "Bạn bị tạm cấm chat vì spam, còn " + (giay >= 60 ? (giay / 60) + " phút" : giay + " giây") + ".";
            }
            case QUANG_CAO:
                return "Không được gửi liên kết, số điện thoại hay quảng cáo.";
            case QUA_NHANH:
                return "Bạn chat quá nhanh, chờ một chút.";
            case TRUNG_LAP:
                return "Đừng gửi lặp lại cùng một nội dung.";
            case KENH_BAN:
                return "Kênh thế giới đang bận, thử lại sau ít giây.";
            default:
                return null;
        }
    }

    /** Duoc bao cho nguoi choi lan nay khong (toi da 1 lan / sv.spam.baoCachGiay). */
    public static boolean nenBao(final TrangThai t, final long bayGio) {
        synchronized (t) {
            if (bayGio - t.mocBaoCuoi < baoCachMs) {
                return false;
            }
            t.mocBaoCuoi = bayGio;
            return true;
        }
    }

    public static boolean laQuangCao(final String tin) {
        if (tin == null || tin.isEmpty()) {
            return false;
        }
        final String bo = boDau(tin);
        if (QUANG_CAO.matcher(bo).find()) {
            return true;
        }
        final Pattern them = quangCaoThem;
        return them != null && them.matcher(bo).find();
    }

    /** Chuan hoa de so trung lap: thuong, bo dau, chi giu chu+so, gop chu lap ("aaaa" = "a"). */
    public static String chuanHoa(final String tin) {
        if (tin == null) {
            return "";
        }
        final String bo = boDau(tin).toLowerCase(Locale.ROOT);
        final StringBuilder b = new StringBuilder(bo.length());
        char truoc = 0;
        for (int i = 0; i < bo.length(); i++) {
            final char c = bo.charAt(i);
            // Gop ky tu lap chi voi CHU ("chaoooo" = "chao"); so giu nguyen ("10" khac "100").
            if (Character.isDigit(c) || (Character.isLetter(c) && c != truoc)) {
                b.append(c);
            }
            if (Character.isLetterOrDigit(c)) {
                truoc = c;
            }
        }
        return b.toString();
    }

    static String boDau(final String s) {
        final String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        final StringBuilder b = new StringBuilder(n.length());
        for (int i = 0; i < n.length(); i++) {
            final char c = n.charAt(i);
            if (Character.getType(c) == Character.NON_SPACING_MARK) {
                continue;
            }
            b.append(c == 'đ' ? 'd' : c == 'Đ' ? 'D' : c);
        }
        return b.toString();
    }

    // ================================================================== thao tac

    /** @return true = cho lam; false = qua nhip, bo qua thao tac nay. */
    public static boolean choHanhDong(final TrangThai t, final HanhDong h, final long bayGio) {
        if (!bat || t == null) {
            return true;
        }
        synchronized (t) {
            t.dungCuoi = bayGio;
            return t.hanhDong[h.ordinal()].choPhep(nhipHanhDong[h.ordinal()], bayGio);
        }
    }

    /** Thao tac nao (neu co) ma lenh client nay thuoc ve - dung chung cho MapXuLyLenh. */
    public static HanhDong hanhDongCuaLenh(final int lenh) {
        switch (lenh) {
            case 53:
            case 55:
                return HanhDong.NPC;
            case 62:
            case 92:
                return HanhDong.RAO_BAN;
            case 24:
            case 28:
            case 95:
                return HanhDong.MUA_BAN;
            default:
                return null;
        }
    }

    // ================================================================== bang trang thai

    /** Trang thai theo khoa (charDBID cho nhan vat, chuoi "ip:..." cho ket noi). */
    public static TrangThai cua(final Object khoa) {
        TrangThai t = BANG.get(khoa);
        if (t == null) {
            final TrangThai moi = new TrangThai();
            t = BANG.putIfAbsent(khoa, moi);
            if (t == null) {
                t = moi;
            }
        }
        final long bayGio = System.currentTimeMillis();
        if (bayGio - mocDon > 300000L) {
            mocDon = bayGio;
            don(bayGio);
        }
        return t;
    }

    /** Bo trang thai nhan roi > 2 gio va KHONG con bi cam (giu nguoi dang bi cam de thoat ra vao lai khong xoa an). */
    static void don(final long bayGio) {
        final Iterator<Map.Entry<Object, TrangThai>> it = BANG.entrySet().iterator();
        while (it.hasNext()) {
            final TrangThai t = it.next().getValue();
            if (bayGio - t.dungCuoi > 7200000L && t.conCam(bayGio) == 0L) {
                it.remove();
            }
        }
    }

    public static int soTrangThai() {
        return BANG.size();
    }

    // ================================================================== cau hinh

    public static synchronized void docCauHinh(final Properties p) {
        bat = !"0".equals(p.getProperty("sv.spam.bat", "1").trim());
        final Nhip[] k = NHIP_KENH_MAC_DINH.clone();
        k[Kenh.GAN.ordinal()] = Nhip.doc(p.getProperty("sv.spam.chat.gan"), k[Kenh.GAN.ordinal()]);
        k[Kenh.RIENG.ordinal()] = Nhip.doc(p.getProperty("sv.spam.chat.rieng"), k[Kenh.RIENG.ordinal()]);
        k[Kenh.THE_GIOI.ordinal()] = Nhip.doc(p.getProperty("sv.spam.chat.theGioi"), k[Kenh.THE_GIOI.ordinal()]);
        k[Kenh.DOI.ordinal()] = Nhip.doc(p.getProperty("sv.spam.chat.doi"), k[Kenh.DOI.ordinal()]);
        k[Kenh.BANG.ordinal()] = Nhip.doc(p.getProperty("sv.spam.chat.bang"), k[Kenh.BANG.ordinal()]);
        nhipKenh = k;
        final Nhip[] h = NHIP_HANH_DONG_MAC_DINH.clone();
        for (final HanhDong x : HanhDong.values()) {
            h[x.ordinal()] = Nhip.doc(p.getProperty("sv.spam.hd." + tenKhoa(x)), h[x.ordinal()]);
        }
        nhipHanhDong = h;
        theGioiToanServer = Nhip.doc(p.getProperty("sv.spam.theGioiToanServer"), THE_GIOI_TOAN_SERVER_MAC_DINH);
        trungLapMs = so(p, "sv.spam.trungLapGiay", 30) * 1000L;
        viPhamCam = so(p, "sv.spam.viPhamCam", 4);
        viPhamQuenMs = so(p, "sv.spam.viPhamQuenGiay", 600) * 1000L;
        bacQuenMs = so(p, "sv.spam.bacQuenGio", 24) * 3600000L;
        baoCachMs = so(p, "sv.spam.baoCachGiay", 5) * 1000L;
        chanQuangCao = !"0".equals(p.getProperty("sv.spam.chanQuangCao", "1").trim());
        final String cam = p.getProperty("sv.spam.camGiay");
        camMs = new long[]{60000L, 300000L, 1800000L, 7200000L};
        if (cam != null && !cam.trim().isEmpty()) {
            try {
                final String[] a = cam.split(",");
                final long[] m = new long[a.length];
                for (int i = 0; i < a.length; i++) {
                    m[i] = Math.max(1L, Long.parseLong(a[i].trim())) * 1000L;
                }
                if (m.length > 0) {
                    camMs = m;
                }
            } catch (final NumberFormatException ignored) {
            }
        }
        final String them = p.getProperty("sv.spam.quangCaoThem", "").trim();
        if (them.isEmpty()) {
            quangCaoThem = null;
        } else {
            final StringBuilder b = new StringBuilder("(?i)(");
            boolean dau = true;
            for (final String w : them.split(",")) {
                if (!w.trim().isEmpty()) {
                    b.append(dau ? "" : "|").append(Pattern.quote(boDau(w.trim())));
                    dau = false;
                }
            }
            quangCaoThem = dau ? null : Pattern.compile(b.append(')').toString());
        }
    }

    static String tenKhoa(final HanhDong h) {
        switch (h) {
            case MOI_DOI:
                return "moiDoi";
            case MOI_BANG:
                return "moiBang";
            case KET_BAN:
                return "ketBan";
            case MOI_GIAO_DICH:
                return "moiGiaoDich";
            case NPC:
                return "npc";
            case RAO_BAN:
                return "raoBan";
            case MUA_BAN:
                return "muaBan";
            default:
                return "dangKy";
        }
    }

    /** Mot dong mo ta cau hinh dang chay (in luc khoi dong). */
    public static String moTa() {
        final StringBuilder b = new StringBuilder("CHONG SPAM -> ").append(bat ? "BAT" : "TAT").append(", chat");
        for (final Kenh k : Kenh.values()) {
            b.append(' ').append(k.name().toLowerCase(Locale.ROOT)).append('=').append(nhipKenh[k.ordinal()]);
        }
        b.append(", the gioi toan server=").append(theGioiToanServer).append(", trung lap ").append(trungLapMs / 1000L)
                .append("s, cam sau ").append(viPhamCam).append(" vi pham, quang cao ").append(chanQuangCao ? "chan" : "cho");
        return b.toString();
    }

    private static int so(final Properties p, final String k, final int macDinh) {
        try {
            final String v = p.getProperty(k);
            return v == null || v.trim().isEmpty() ? macDinh : Integer.parseInt(v.trim());
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }

    /** Kiem thu: ve mac dinh + xoa bang trang thai + tran the gioi. */
    public static synchronized void datLaiChoKiemThu() {
        BANG.clear();
        synchronized (THE_GIOI) {
            THE_GIOI.moc = null;
        }
        docCauHinh(new Properties());
    }
}
