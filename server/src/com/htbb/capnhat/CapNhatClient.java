package com.htbb.capnhat;

import io.Message;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CAP NHAT CLIENT (0.9.7, ban 907): bao nguoi choi co ban moi (NHAC, khong chan) hoac BAT BUOC cap nhat (chan dang
 * nhap) theo hai nguong ma admin dat tren web, khong sua tep nao:
 * <ul>
 *   <li><b>ban moi nhat</b> = so ban (truong {@code ban}) cua tung nen tang trong danh muc tai game (web_cai_dat khoa
 *       'taiGame', trang /admin/tai-game) - phat hanh = tai tep len + dien so ban. Danh muc chua ghi so ban thi dung
 *       {@code sv.banMoiNhat} (du phong, chung moi nen tang);</li>
 *   <li><b>ban toi thieu</b> = {@code sv.banToiThieu}: client gui so ban &lt; nguong bi chan dang nhap. Chi chan khi nen
 *       tang do DA CO ban tai dat nguong (hoac danh muc khong ghi so ban - giu cach cu): admin dat nham nguong cao hon
 *       ban dang phat thi nguoi choi khong bi khoa ngoai (chi nhac), log canh bao.</li>
 * </ul>
 * Nen tang theo platf client gui (giong data.GhiClient): "HTBB-PC/Windows.." -> windows, "HTBB-PC/Linux" -> linux,
 * "Android/.." -> android, con lai -> jar. Client khong gui so ban (Unity, client la) = -1: khong lam gi.
 *
 * <p>Goi {@link #LENH} -115 (S-&gt;C, CHI gui client ban &gt;= {@link #BAN_CO_GOI}): byte loai ({@link #NHAC} /
 * {@link #BAT_BUOC}), int ban moi (0 = khong ro), UTF phien ban, UTF link tep (tuyet doi, "" = chi co trang tai),
 * UTF sha256 (hex, "" = khong co -> client khong tu cai), UTF dung luong, UTF nhat ky (co gi moi), UTF link trang tai
 * game, int ban toi thieu, int ban client. Truong moi them SAU (client doc bang available()).
 * Client cu hon 907 khong biet goi nay: nhan hop thoai 37 (chu + link: hop "Mo" -&gt; platformRequest(link) -&gt;
 * thoat) - BAT_BUOC moi lan dang nhap, NHAC mot lan/ngay/tai khoan.
 */
public final class CapNhatClient {

    public static final int LENH = -115;
    /** Ban client dau tien biet goi -115 (client_jar game.pokemon.CapNhat). */
    public static final int BAN_CO_GOI = 907;
    public static final byte KHONG = 0, NHAC = 1, BAT_BUOC = 2;
    /** Khoa web_cai_dat cua danh muc tai game. */
    public static final String KHOA_CMS = "taiGame";
    static final int NHAT_KY_TOI_DA = 1500;

    private CapNhatClient() {
    }

    // ================================================================== du lieu

    /** Mot ban tai trong danh muc (da nan). */
    public static final class Ban {
        public String ma = "";
        public int ban;
        public String phienBan = "";
        public String url = "";
        public String sha256 = "";
        public String dungLuong = "";
        public String nhatKy = "";
        /** hien + 'san-sang' + kieu 'tep' + co link. */
        public boolean sanSang;

        @Override
        public String toString() {
            return ma + "@" + ban + "(" + phienBan + (sanSang ? "" : ",chua san sang") + ")";
        }
    }

    /** Ket qua quyet dinh cho mot lan dang nhap. */
    public static final class QuyetDinh {
        public byte loai = KHONG;
        public int banClient = -1;
        public int banMoi;
        public int banToiThieu;
        public String nenTang;
        /** Ban tai cua nen tang (null = danh muc khong co / chua san sang). */
        public Ban ban;
        /** true = nguong toi thieu cao hon ban dang phat cua nen tang (khong chan, chi nhac). */
        public boolean nguongVuotBanPhat;
    }

    private static volatile Map<String, Ban> danhMuc = Collections.emptyMap();
    private static volatile long lucNap;
    private static volatile String loiNap = "";

    // ================================================================== thuan (kiem thu duoc)

    /** jar / android / windows / linux theo platf client gui; null = PC he khac (chua co ban). */
    public static String nenTang(final String platf) {
        final String p = platf == null ? "" : platf.trim();
        if (p.startsWith("HTBB-PC")) {
            final String os = p.toLowerCase(java.util.Locale.ROOT);
            if (os.contains("windows")) {
                return "windows";
            }
            if (os.contains("linux")) {
                return "linux";
            }
            return null;
        }
        if (p.startsWith("Android")) {
            return "android";
        }
        return "jar";
    }

    private static String chuoi(final Object v, final int max) {
        if (!(v instanceof String)) {
            return "";
        }
        final String s = ((String) v).trim();
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static int so(final Object v) {
        if (v instanceof Number) {
            final double d = ((Number) v).doubleValue();
            return d > 0 && d < 100000000 ? (int) d : 0;
        }
        if (v instanceof String) {
            try {
                final int n = Integer.parseInt(((String) v).trim());
                return n > 0 && n < 100000000 ? n : 0;
            } catch (final NumberFormatException ex) {
                return 0;
            }
        }
        return 0;
    }

    /** Nan JSON 'taiGame' ({ban: [{ma, ban, phienBan, url, sha256, ...}]}) -> ma nen tang -> ban tai. Hong -> rong. */
    public static Map<String, Ban> docDanhMuc(final String json) {
        final Map<String, Ban> ra = new HashMap<String, Ban>();
        final Object goc;
        try {
            goc = JsonNho.doc(json);
        } catch (final IllegalArgumentException ex) {
            return ra;
        }
        if (!(goc instanceof Map)) {
            return ra;
        }
        final Object ds = ((Map<?, ?>) goc).get("ban");
        if (!(ds instanceof List)) {
            return ra;
        }
        for (final Object x : (List<?>) ds) {
            if (!(x instanceof Map)) {
                continue;
            }
            final Map<?, ?> r = (Map<?, ?>) x;
            String ma = chuoi(r.get("ma"), 20);
            if ("pc-java".equals(ma)) {
                ma = "windows";   // ma cu (shared/utils/taiGame.ts MA_CU)
            } else if ("pc-linux".equals(ma)) {
                ma = "linux";
            }
            if (ma.isEmpty() || ra.containsKey(ma)) {
                continue;
            }
            final Ban b = new Ban();
            b.ma = ma;
            b.ban = so(r.get("ban"));
            b.phienBan = chuoi(r.get("phienBan"), 40);
            b.url = chuoi(r.get("url"), 500);
            if ("#".equals(b.url)) {
                b.url = "";
            }
            final String sha = chuoi(r.get("sha256"), 64).toLowerCase(java.util.Locale.ROOT);
            b.sha256 = sha.matches("[0-9a-f]{64}") ? sha : "";
            b.dungLuong = chuoi(r.get("dungLuong"), 40);
            b.nhatKy = chuoi(r.get("nhatKy"), NHAT_KY_TOI_DA).replace("\\n", "\n");
            final Object kieu = r.get("kieu");
            final boolean tep = kieu == null || "tep".equals(kieu);
            final boolean hien = !Boolean.FALSE.equals(r.get("hien"));
            final Object tt = r.get("trangThai");
            final boolean tsS = tt == null ? !b.url.isEmpty() : "san-sang".equals(tt);
            b.sanSang = hien && tsS && tep && !b.url.isEmpty();
            ra.put(ma, b);
        }
        return ra;
    }

    /**
     * Quyet dinh cho mot client. banMoiNhatDuPhong = sv.banMoiNhat (dung khi ban tai chua ghi so ban), nhac =
     * sv.nhacCapNhat.
     */
    public static QuyetDinh quyetDinh(final int banClient, final String nenTang, final int banToiThieu,
            final int banMoiNhatDuPhong, final boolean nhac, final Map<String, Ban> dm) {
        final QuyetDinh q = new QuyetDinh();
        q.banClient = banClient;
        q.nenTang = nenTang;
        q.banToiThieu = Math.max(0, banToiThieu);
        if (banClient < 0) {
            return q;   // Unity / client la: khong gui so ban
        }
        final Ban b = nenTang == null || dm == null ? null : dm.get(nenTang);
        q.ban = b != null && b.sanSang ? b : null;
        int moi = 0;
        if (q.ban != null && q.ban.ban > 0) {
            moi = q.ban.ban;
        } else if (banMoiNhatDuPhong > 0 && (nenTang != null)) {
            moi = banMoiNhatDuPhong;
        }
        q.banMoi = moi;
        if (q.banToiThieu > 0 && banClient < q.banToiThieu) {
            if (moi == 0 || moi >= q.banToiThieu) {
                q.loai = BAT_BUOC;
                return q;
            }
            q.nguongVuotBanPhat = true;   // chua co ban tai nao dat nguong: khong khoa nguoi choi ngoai
        }
        if (nhac && moi > banClient) {
            q.loai = NHAC;
        }
        return q;
    }

    /** "/tai/x.apk" + goc "https://a.b/duong/" -> "https://a.b/tai/x.apk"; link https:// giu nguyen; sai -> "". */
    public static String urlTuyetDoi(final String url, final String goc) {
        final String u = url == null ? "" : url.trim();
        if (u.startsWith("https://") || u.startsWith("http://")) {
            return u;
        }
        if (!u.startsWith("/") || u.startsWith("//")) {
            return "";
        }
        final String g = goc == null ? "" : goc.trim();
        final int sc = g.indexOf("://");
        if (sc < 0) {
            return "";
        }
        final int cuoi = g.indexOf('/', sc + 3);
        return (cuoi < 0 ? g : g.substring(0, cuoi)) + u;
    }

    /** "https://a.b:8443/x/y" -> "https://a.b:8443" (chi http / https, co may chu); sai -> "". */
    public static String nguonGoc(final String goc) {
        final String g = goc == null ? "" : goc.trim();
        if (!g.startsWith("https://") && !g.startsWith("http://")) {
            return "";
        }
        final int sc = g.indexOf("://") + 3;
        int cuoi = g.length();
        for (int i = sc; i < g.length(); i++) {
            final char c = g.charAt(i);
            if (c == '/' || c == '?' || c == '#') {
                cuoi = i;
                break;
            }
        }
        final String may = g.substring(sc, cuoi);
        if (may.isEmpty() || may.indexOf('@') >= 0 || !may.matches("[A-Za-z0-9.:\\[\\]-]+")) {
            return "";
        }
        return g.substring(0, cuoi);
    }

    /**
     * Goc tai tep cap nhat trong game (sv.gocTaiCapNhat): ten mien nam sau WAF (Nova) tra trang thu thach JS (HTTP 403)
     * cho client khong phai trinh duyet (PC updater HttpURLConnection, APK, trinh duyet J2ME cu) - link tep TUONG DOI
     * (/tai/...) va link tuyet doi CUNG may chu voi website duoc doi sang goc nay (vd http://&lt;IP origin&gt;; tep van kiem
     * sha256). Trong / sai -> dung website (khong doi gi). Trang tai game (/download) giu ten mien (trinh duyet qua WAF).
     */
    public static String gocTai(final String gocTaiCapNhat, final String website) {
        final String g = nguonGoc(gocTaiCapNhat);
        return g.isEmpty() ? (website == null ? "" : website.trim()) : g;
    }

    /**
     * Link tep: tuong doi -> gocTai; tuyet doi tro ve chinh website (cung scheme + may chu) -> doi sang gocTai; link
     * may chu khac (CDN...) giu nguyen.
     */
    public static String urlTai(final String url, final String gocTaiCapNhat, final String website) {
        final String gt = gocTai(gocTaiCapNhat, website);
        final String u = url == null ? "" : url.trim();
        final String web = nguonGoc(website);
        if (!nguonGoc(gocTaiCapNhat).isEmpty() && !web.isEmpty() && u.toLowerCase(java.util.Locale.ROOT).startsWith(
                web.toLowerCase(java.util.Locale.ROOT) + "/")) {
            return urlTuyetDoi(u.substring(web.length()), gt);
        }
        return urlTuyetDoi(u, gt);
    }

    /** Link tep cua quyet dinh ("" = chi co trang tai); goc = goc tai da tinh ({@link #gocTai}). */
    public static String linkTep(final QuyetDinh q, final String goc) {
        return q.ban == null ? "" : urlTuyetDoi(q.ban.url, goc);
    }

    /** Link tep theo cau hinh may chu hien tai (sv.gocTaiCapNhat, sv.website). */
    static String linkTepMayChu(final QuyetDinh q) {
        return q.ban == null ? "" : urlTai(q.ban.url, server.TeamServer.gocTaiCapNhat, goc());
    }

    /** Ten ban moi de hien: phien ban danh muc, hoac "bản <so>". */
    public static String tenBanMoi(final QuyetDinh q) {
        if (q.ban != null && !q.ban.phienBan.isEmpty()) {
            return q.ban.phienBan;
        }
        return q.banMoi > 0 ? "bản " + q.banMoi : "mới";
    }

    /** Goi -115 (client &gt;= 907); goc = goc tai tep ({@link #gocTai}). */
    public static Message tao(final QuyetDinh q, final String goc, final String trangTai) throws IOException {
        return tao(q, linkTep(q, goc), trangTai, true);
    }

    /** Goi -115 voi link tep da tinh san ("" = chi co trang tai). */
    static Message tao(final QuyetDinh q, final String linkTep, final String trangTai, final boolean daTinh) throws IOException {
        final Message m = new Message(LENH);
        m.dos.writeByte(q.loai);
        m.dos.writeInt(q.banMoi);
        m.dos.writeUTF(q.ban == null ? "" : q.ban.phienBan);
        m.dos.writeUTF(linkTep == null ? "" : linkTep);
        m.dos.writeUTF(q.ban == null ? "" : q.ban.sha256);
        m.dos.writeUTF(q.ban == null ? "" : q.ban.dungLuong);
        m.dos.writeUTF(q.ban == null ? "" : q.ban.nhatKy);
        m.dos.writeUTF(trangTai == null ? "" : trangTai);
        m.dos.writeInt(q.banToiThieu);
        m.dos.writeInt(q.banClient);
        return m;
    }

    /** Link cho hop 37 cua client cu: tep (mot cham tai) neu co, khong thi trang tai game. */
    public static String linkCu(final QuyetDinh q, final String goc, final String trangTai) {
        return linkCuTuTep(linkTep(q, goc), trangTai);
    }

    static String linkCuTuTep(final String t, final String trangTai) {
        return t == null || t.isEmpty() ? (trangTai == null ? "" : trangTai) : t;
    }

    /** Goi -115 theo cau hinh may chu hien tai (link tep qua sv.gocTaiCapNhat). */
    public static Message taoMayChu(final QuyetDinh q) throws IOException {
        return tao(q, linkTepMayChu(q), trangTai(), true);
    }

    /** Link hop 37 (client cu) theo cau hinh may chu hien tai. */
    public static String linkCuMayChu(final QuyetDinh q) {
        return linkCuTuTep(linkTepMayChu(q), trangTai());
    }

    /** Chu hop 37 cho client cu (&lt; 907) - LUON kem link tai (client cu khong co nut, chi doc duoc chu). */
    public static String cauCu(final QuyetDinh q, final String trangTai) {
        final String trang = trangTai == null || trangTai.isEmpty() ? "trang chủ" : trangTai;
        if (q.loai == BAT_BUOC) {
            return "Phiên bản game đã cũ (bản " + q.banClient + "). Cần cập nhật "
                    + (q.banMoi > 0 ? "lên " + tenBanMoi(q) + " " : "") + "để tiếp tục chơi. Bấm Mở (hoặc OK) để tải bản mới, hoặc tải tại "
                    + trang;
        }
        return "Đã có bản mới " + tenBanMoi(q) + " (bạn đang dùng bản " + q.banClient + "). Bấm Mở (hoặc OK) để tải, hoặc tải tại "
                + trang;
    }

    // ================================================================== trang thai may chu

    /** Danh muc dang dung (doc lai moi 60 giay). */
    public static Map<String, Ban> danhMuc() {
        return danhMuc;
    }

    /** Dat danh muc (kiem thu / nap tu CSDL). */
    public static void datDanhMuc(final Map<String, Ban> dm) {
        danhMuc = dm == null ? Collections.<String, Ban>emptyMap() : Collections.unmodifiableMap(dm);
        lucNap = System.currentTimeMillis();
    }

    public static String moTa() {
        return "danh muc " + danhMuc.values() + (loiNap.isEmpty() ? "" : " loi=" + loiNap) + " luc=" + lucNap;
    }

    /** Doc web_cai_dat 'taiGame' tu CSDL game (web va server cung CSDL). Bang chua co / loi -> giu danh muc cu. */
    public static void napTuCsdl() {
        java.sql.Connection conn = null;
        try {
            conn = data.Database.instance.getConnection();
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT gia_tri FROM web_cai_dat WHERE khoa = ?")) {
                ps.setString(1, KHOA_CMS);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        final Map<String, Ban> dm = docDanhMuc(rs.getString(1));
                        final String truoc = String.valueOf(danhMuc.values());
                        datDanhMuc(dm);
                        if (!truoc.equals(String.valueOf(danhMuc.values()))) {
                            System.out.println("[CAP NHAT] " + moTa());
                        }
                    }
                }
            }
            loiNap = "";
        } catch (final Throwable e) {
            if (!String.valueOf(e).equals(loiNap)) {
                System.out.println("[CAP NHAT] chua doc duoc danh muc tai game (" + e + ") - giu danh muc cu");
            }
            loiNap = String.valueOf(e);
        } finally {
            try {
                if (conn != null) {
                    data.Database.connPool.free(conn);
                }
            } catch (final Throwable ignored) {
            }
        }
    }

    private static volatile boolean daChay;

    /** Bat luong nen doc lai danh muc moi 60 giay (goi mot lan luc khoi dong, sau CauHinh.batDau). */
    public static synchronized void batDau() {
        if (daChay) {
            return;
        }
        daChay = true;
        final Thread t = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    napTuCsdl();
                    try {
                        Thread.sleep(60000L);
                    } catch (final InterruptedException e) {
                        return;
                    }
                }
            }
        }, "CapNhatClient");
        t.setDaemon(true);
        t.start();
    }

    /** Goc website (link /tai/... tuong doi khi chua dat sv.gocTaiCapNhat) va trang tai game cua server nay. */
    static String goc() {
        final String w = server.TeamServer.website;
        return w != null && !w.trim().isEmpty() ? w.trim() : server.TeamServer.linkTaiGame;
    }

    static String trangTai() {
        final String l = server.TeamServer.linkTaiGame;
        return l != null && !l.trim().isEmpty() ? l.trim() : server.TeamServer.website;
    }

    public static QuyetDinh quyetDinh(final io.Session s) {
        return quyetDinh(s.banClient, nenTang(s.platfClient), server.TeamServer.banToiThieu,
                server.TeamServer.banMoiNhat, server.TeamServer.nhacCapNhat, danhMuc);
    }

    /**
     * Goi luc doc goi dang nhap (LoginHandler, sau khi doc so ban): true = BAT BUOC cap nhat, da bao client, PHAI
     * dung dang nhap.
     */
    public static boolean chanKhiDangNhap(final io.Session s, final String tenDangNhap) {
        final QuyetDinh q = quyetDinh(s);
        if (q.nguongVuotBanPhat) {
            System.out.println("[CAP NHAT] CANH BAO: sv.banToiThieu=" + q.banToiThieu + " cao hon ban dang phat cua " + q.nenTang
                    + " (" + q.banMoi + ") - khong chan, chi nhac. Dien so ban o /admin/tai-game.");
        }
        if (q.loai != BAT_BUOC) {
            return false;
        }
        com.htbb.log.Nhat.thongTin("CLIENT", "tu choi ban cu " + q.banClient + " < " + q.banToiThieu + " (" + q.nenTang
                + ", ban moi " + q.banMoi + ") user=" + com.htbb.log.Nhat.sach(tenDangNhap));
        try {
            if (q.banClient >= BAN_CO_GOI) {
                s.sendMessage(taoMayChu(q));
            } else {
                s.sendMessage(real.MessageCreator.createServerAlertMessage(cauCu(q, trangTai()), linkCuMayChu(q)));
            }
        } catch (final Exception ignored) {
        }
        return true;
    }

    /** Lan nhac gan nhat (ngay) cua tai khoan dung client cu - mot lan/ngay. */
    private static final ConcurrentHashMap<String, Integer> NHAC_CU = new ConcurrentHashMap<String, Integer>();

    static int ngayHomNay() {
        return (int) ((System.currentTimeMillis() + 7L * 3600000L) / 86400000L);   // gio Viet Nam
    }

    /** Goi sau khi dang nhap thanh cong (gui danh sach nhan vat): bao ban moi (khong chan). */
    public static void sauDangNhap(final io.Session s) {
        if (s == null) {
            return;
        }
        final QuyetDinh q = quyetDinh(s);
        if (q.loai != NHAC) {
            return;
        }
        try {
            if (q.banClient >= BAN_CO_GOI) {
                s.sendMessage(taoMayChu(q));   // client tu nho "de sau" mot lan/ngay
                return;
            }
            final String tk = s.username == null ? "" : s.username.toLowerCase(java.util.Locale.ROOT);
            final Integer homNay = Integer.valueOf(ngayHomNay());
            if (homNay.equals(NHAC_CU.get(tk))) {
                return;
            }
            if (NHAC_CU.size() > 20000) {
                NHAC_CU.clear();
            }
            NHAC_CU.put(tk, homNay);
            s.sendMessage(real.MessageCreator.createServerAlertMessage(cauCu(q, trangTai()), linkCuMayChu(q)));
        } catch (final Exception ignored) {
        }
    }

    /**
     * Ap sv.gocTaiCapNhat (server.ini luc khoi dong, dong cau_hinh luc chay - com.htbb.cauhinh.ApDungMacDinh). Sai (khong
     * phai http(s)://may) -> bo qua, dung website, ghi canh bao. Tra goc dang dung ("" = theo website).
     */
    public static final String KHOA_GOC_TAI = "sv.gocTaiCapNhat";

    /** Nguoi nghe cau hinh trung tam cho {@link #KHOA_GOC_TAI} (ApDungMacDinh dang ky; dong CSDL &gt; server.ini). */
    public static final com.htbb.cauhinh.NgheThayDoi NGHE_GOC_TAI = new com.htbb.cauhinh.NgheThayDoi() {
        public void khiDoi(final java.util.Set<String> k, final com.htbb.cauhinh.CauHinhTrungTam.AnhChup m) {
            datGocTaiCapNhat(m.thanhProperties().getProperty(KHOA_GOC_TAI));
        }
    };

    public static String datGocTaiCapNhat(final String v) {
        final String tho = v == null ? "" : v.trim();
        final String g = nguonGoc(tho);
        if (!tho.isEmpty() && g.isEmpty()) {
            System.out.println("[CAP NHAT] CANH BAO: sv.gocTaiCapNhat=\"" + tho + "\" khong phai http(s)://may - bo qua, link tai theo sv.website");
        }
        final String truoc = server.TeamServer.gocTaiCapNhat;
        server.TeamServer.gocTaiCapNhat = g;
        if (!g.equals(truoc)) {
            System.out.println("[CAP NHAT] goc tai tep cap nhat = " + (g.isEmpty() ? "theo sv.website (" + goc() + ")" : g));
        }
        return g;
    }
}
