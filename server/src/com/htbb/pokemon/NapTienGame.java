package com.htbb.pokemon;

import data.Database;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;
import real.CharDoiTenNhanVat;
import real.CharManager;
import real.MessageCreator;
import server.TeamServer;

/**
 * Phan GAME cua nap tien (docs/van-hanh.md): vong quet cong don, thu hoi, tra xu cho,
 * diem ve mua; menu "Cửa hàng & Nạp" o NPC Tong Quan (-10); cac moc noi (hook) cho file game.
 * Luat thuan o {@link NapTienLogic}, CSDL o {@link NapTienDb}. Moi thu chi chay khi {@link NapTien#bat()}.
 *
 * DUONG DUY NHAT luong nap vao nhan vat: {@link #congDon}. Web chi doi don sang da_thanh_toan
 * (webhook / admin); server cong luong qua chot da_thanh_toan -> da_cong (UPDATE co dieu kien)
 * va so cai UNIQUE(ma_tham_chieu). Goi lap / hai luong / hai may chu cung luc: chi mot lan cong.
 *
 * Menu ao (npc ao, cung quy uoc KhoXuLuong / SoTayLauDai): -540 goc, -541 nhom cua hang,
 * -542 mon, -543 xac nhan mua, -544 tu do, -545 thao tac mon, -546 cho (ma), -547 cho (dong rao),
 * -548 xac nhan mua cho, -549 chon gia rao. Dai -540..-549 danh rieng cho nap tien.
 */
public final class NapTienGame implements Runnable {

    public static final int NPC_GOC = -540;
    public static final int NPC_NHOM = -541;
    public static final int NPC_MON = -542;
    public static final int NPC_XAC_NHAN = -543;
    public static final int NPC_TU = -544;
    public static final int NPC_TU_MON = -545;
    public static final int NPC_CHO = -546;
    public static final int NPC_CHO_DONG = -547;
    public static final int NPC_CHO_MUA = -548;
    public static final int NPC_RAO_GIA = -549;
    public static final String TEN_MUC = "Cửa hàng và nạp";

    /** Vong quet: 15 giay (cong luong sau khi web xac nhan, nguoi choi doi toi da ~15 giay). */
    public static final long CHU_KY_MS = 15000L;
    /** Tu do trang tri: 30 o, the thang +10. */
    public static final int TU_DO_O = 30;

    private static volatile boolean daChay = false;
    private static volatile boolean daBaoThieuBang = false;

    /** charDBID -> vi tri muc trong menu NPC -10 (moi lan mo menu ghi lai). */
    private static final ConcurrentHashMap<Integer, Integer> VI_TRI = new ConcurrentHashMap<Integer, Integer>();
    /** charDBID -> du lieu menu dang mo (danh sach vua hien). */
    private static final ConcurrentHashMap<Integer, Object> NGU_CANH = new ConcurrentHashMap<Integer, Object>();
    /** charDBID -> {ngay epoch, giay online da tich, diem online hom nay}. */
    private static final ConcurrentHashMap<Integer, long[]> ONLINE = new ConcurrentHashMap<Integer, long[]>();

    private NapTienGame() {
    }

    // ================================================================== vong quet

    /** TeamServer goi luc khoi dong. Tat co -> khong lam gi. */
    public static synchronized void batDau() {
        if (daChay || !NapTien.bat()) {
            return;
        }
        daChay = true;
        final Thread t = new Thread(new NapTienGame());
        t.setName("NAP TIEN");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        long lanGhiTrangThai = 0L;
        while (TeamServer.running) {
            try {
                if (NapTienDb.coBang()) {
                    daBaoThieuBang = false;
                    motVong();
                    final long t = System.currentTimeMillis();
                    if (t - lanGhiTrangThai > 60000L) {
                        lanGhiTrangThai = t;
                        ghiTrangThai();
                    }
                } else if (!daBaoThieuBang) {
                    daBaoThieuBang = true;
                    System.out.println("NAP TIEN: CHUA CO BANG nap_* - chay sql/htbb.sql. Tam ngung cong luong.");
                }
            } catch (final Throwable e) {
                System.out.println("NAP TIEN: loi vong quet - " + e);
            }
            try {
                Thread.sleep(CHU_KY_MS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    static void motVong() throws SQLException {
        final List<Char> online = nguoiChoiOnline();
        // 1. Don da thanh toan -> cong cho nhan vat dang online.
        for (final NapTienDb.Don d : NapTienDb.donChoCong(100)) {
            final Char p = timNhanVat(online, d.charId, d.userId);
            if (p != null) {
                congDon(p, d);
            }
        }
        // 2. Hoan tien sau khi da cong -> no cua tai khoan (thu dan khi online).
        //    Cong tac nap.hoan_tien.tu_dong (cau hinh trung tam, ap dung <= 10 giay): 0 = don dung cho, xu ly tay.
        if (com.htbb.cauhinh.CongTac.hoanTienTuDong()) {
            for (final NapTienDb.Don d : NapTienDb.donChoThuHoi(100)) {
                thuHoiDon(d);
            }
        }
        // 3. Thu no tu nhan vat online.
        final Map<Integer, Long> no = NapTienDb.taiKhoanDangNo();
        if (!no.isEmpty()) {
            for (final Char p : online) {
                if (no.containsKey(p.userID)) {
                    thuNo(p);
                }
            }
        }
        // 4. Tra xu cho nguoi ban tren Cho trang tri.
        final List<Integer> ban = NapTienDb.nguoiBanChoTra();
        if (!ban.isEmpty()) {
            for (final Char p : online) {
                if (ban.contains(p.charDBID)) {
                    traTienCho(p);
                }
            }
        }
        // 5. Diem ve mua theo thoi gian online.
        diemOnline(online, CHU_KY_MS / 1000L);
    }

    static void ghiTrangThai() {
        final int ngay = LauDai.ngayHienTai();
        NapTienDb.ghiCauHinh("trang_thai.server", "bat;" + System.currentTimeMillis());
        NapTienDb.ghiCauHinh("trang_thai.mua_ve", "mua=" + NapTienLogic.muaVe(ngay) + ";con_ngay="
                + NapTienLogic.ngayConLaiMuaVe(ngay));
        NapTienDb.ghiCauHinh("trang_thai.luong", "vao=" + NapTien.luongDaVao() + ";tieu=" + NapTien.luongDaTieu());
    }

    static List<Char> nguoiChoiOnline() {
        final List<Char> ra = new ArrayList<Char>();
        try {
            final Vector<Char> ds = CharManager.instance.vChars;
            for (int i = ds.size() - 1; i >= 0; --i) {
                try {
                    final Char c = ds.get(i);
                    if (c != null && c.isBot == -1 && c.getSession() != null) {
                        ra.add(c);
                    }
                } catch (final Exception ignored) {
                }
            }
        } catch (final Exception ignored) {
        }
        return ra;
    }

    /** Nhan vat nhan don: dung char_id; char_id = 0 -> nhan vat bat ky cua tai khoan dang online. */
    static Char timNhanVat(final List<Char> online, final int charId, final int userId) {
        for (final Char c : online) {
            if (charId > 0 ? c.charDBID == charId : c.userID == userId) {
                return c;
            }
        }
        return null;
    }

    // ================================================================== cong don (DUY NHAT)

    /**
     * Cong mot don da thanh toan cho nhan vat dang online. An toan khi goi lap / song song:
     * chot da_thanh_toan -> da_cong chi thanh cong cho mot luong; so cai UNIQUE chan lan hai.
     * @return so luong thuc cong vao nhan vat (0 neu don da duoc xu ly o noi khac).
     */
    public static int congDon(final Char p, final NapTienDb.Don d) throws SQLException {
        if (!NapTien.bat() || p == null || d == null || NapTienLogic.LOAI_THANH_VIEN.equals(d.loai)) {
            // thanh vien: do web cap thang theo tai khoan, game khong cong (va khong cham don).
            return 0;
        }
        final long bayGioMs = System.currentTimeMillis();
        // The thang: kiem truoc khi chot - con > 30 ngay thi KHONG cong (de admin hoan tien).
        long hanCu = 0L;
        long hanMoi = 0L;
        if (NapTienLogic.LOAI_THE_THANG.equals(d.loai)) {
            hanCu = (Long) NapTienDb.theThang(p.charDBID)[0];
            hanMoi = NapTienLogic.giaHanTheThang(hanCu, bayGioMs);
            if (hanMoi < 0L) {
                NapTienDb.ghiNhatKy("the_thang_qua_han_muc", d.maDon, d.userId,
                        "nhan vat " + p.charname + " con > 30 ngay the - giu don o da_thanh_toan cho admin");
                return 0;
            }
        }
        if (!NapTienDb.danhDauDaCong(d.id, p.charDBID)) {
            return 0; // da co luong khac / lan truoc cong roi
        }
        if (!NapTienDb.ghiSoCai(d.userId, p.charDBID, "cong", d.luong, d.soTien, d.maDon,
                NapTienLogic.thamChieuCong(d.maDon), d.loai + " " + d.maGoi + " -> " + p.charname)) {
            // So cai da co dong cong cua don nay: KHONG cong lan hai.
            NapTienDb.ghiNhatKy("cong_trung_bi_chan", d.maDon, d.userId, "so cai da co " + NapTienLogic.thamChieuCong(d.maDon));
            return 0;
        }
        int cong = 0;
        final StringBuilder bao = new StringBuilder();
        if (NapTienLogic.LOAI_LUONG.equals(d.loai) && d.luong > 0) {
            final long[] tn = NapTienLogic.congCoNo(NapTienDb.noLuong(d.userId), d.luong);
            if (tn[0] > 0L && NapTienDb.truNo(d.userId, tn[0])) {
                NapTienDb.ghiSoCai(d.userId, p.charDBID, "tra_no", -tn[0], 0, d.maDon, "trano:" + d.maDon, "tra no thu hoi");
                bao.append("Đã trừ ").append(tn[0]).append(" lượng để trả nợ thu hồi (giao dịch nạp trước đó đã được hoàn tiền). ");
            } else {
                tn[1] = d.luong;
            }
            cong = (int) tn[1];
            if (cong > 0) {
                p.addLuong(cong);
                NapTien.ghiVao(cong);
            }
            bao.append("Nạp thành công: +").append(cong).append(" lượng.");
        } else if (NapTienLogic.LOAI_THE_THANG.equals(d.loai)) {
            if (!NapTienDb.doiHanThe(p.charDBID, d.userId, hanCu, hanMoi)) {
                // Hai don the cung luc: doc lai va gia han tu han moi nhat.
                final long hc = (Long) NapTienDb.theThang(p.charDBID)[0];
                final long hm = Math.max(NapTienLogic.giaHanTheThang(hc, bayGioMs), hc + 30L * 86400000L);
                NapTienDb.doiHanThe(p.charDBID, d.userId, hc, hm);
            }
            bao.append("Kích hoạt Thẻ tháng 30 ngày: nhận ").append(NapTienLogic.THE_THANG_LUONG_NGAY)
                    .append(" lượng mỗi ngày ở NPC Tổng Quản, mục ").append(TEN_MUC).append('.');
        } else if (NapTienLogic.LOAI_VE_MUA.equals(d.loai)) {
            final int mua = NapTienLogic.muaVe(LauDai.ngayHienTai());
            NapTienDb.datCaoCap(p.charDBID, mua, 1);
            bao.append("Đã mở Vé mùa cao cấp (mùa ").append(mua + 1).append("). Nhận thưởng ở NPC Tổng Quản, mục ")
                    .append(TEN_MUC).append('.');
        }
        // Thuong lan dau: CHI trang tri, mot lan / tai khoan.
        if (NapTienDb.nhanThuongDau(d.userId)) {
            NapTienDb.themSoHuu(p.charDBID, NapTienLogic.THUONG_LAN_DAU, "lan_dau", false, 0L);
            bao.append(" Quà nạp lần đầu: Khung tên Người ủng hộ (trang trí).");
        }
        boolean daLuu = false;
        try {
            daLuu = Database.instance.saveCharAuto(p);
        } catch (final Exception e) {
            System.out.println("NAP TIEN: loi luu nhan vat sau khi cong " + d.maDon + " - " + e);
        }
        if (daLuu) {
            NapTienDb.danhDauDaLuu(d.id);
        }
        NapTienDb.ghiNhatKy("cong", d.maDon, d.userId, d.loai + " " + d.maGoi + " -> " + p.charname + " (id " + p.charDBID
                + ") +" + cong + " luong" + (daLuu ? "" : " [CHUA LUU NHAN VAT]"));
        try {
            Database.instance.saveOrtherLog("", p.charname, "nap tien " + d.maDon + " " + d.maGoi + " +" + cong + " luong", "naptien");
        } catch (final Exception ignored) {
        }
        bao(p, bao.toString());
        capNhatTui(p);
        return cong;
    }

    // ================================================================== thu hoi / no

    /** Hoan tien sau khi da cong: chuyen toan bo luong thanh no cua tai khoan + go quyen loi. */
    static void thuHoiDon(final NapTienDb.Don d) throws SQLException {
        if (!NapTienDb.danhDauDaThuHoi(d.id)) {
            return;
        }
        if (NapTienLogic.LOAI_LUONG.equals(d.loai) && d.luong > 0) {
            if (NapTienDb.ghiSoCai(d.userId, d.charId, "no", -d.luong, -d.soTien, d.maDon,
                    NapTienLogic.thamChieuThuHoi(d.maDon), "hoan tien sau khi cong -> no")) {
                NapTienDb.congNo(d.userId, d.luong);
            }
        } else if (NapTienLogic.LOAI_THE_THANG.equals(d.loai) && d.charId > 0) {
            final long bayGio = System.currentTimeMillis();
            final long hc = (Long) NapTienDb.theThang(d.charId)[0];
            NapTienDb.doiHanThe(d.charId, d.userId, hc, NapTienLogic.thuHoiTheThang(hc, bayGio));
            NapTienDb.ghiSoCai(d.userId, d.charId, "thu_hoi", 0, -d.soTien, d.maDon, NapTienLogic.thamChieuThuHoi(d.maDon),
                    "thu hoi the thang (-30 ngay)");
        } else if (NapTienLogic.LOAI_VE_MUA.equals(d.loai) && d.charId > 0) {
            NapTienDb.datCaoCap(d.charId, NapTienLogic.muaVe(LauDai.ngayHienTai()), 0);
            NapTienDb.ghiSoCai(d.userId, d.charId, "thu_hoi", 0, -d.soTien, d.maDon, NapTienLogic.thamChieuThuHoi(d.maDon),
                    "thu hoi ve mua cao cap");
        }
        NapTienDb.ghiNhatKy("thu_hoi", d.maDon, d.userId, d.loai + " " + d.luong + " luong");
    }

    /** Tru no tu nhan vat dang online (toi da so luong dang co). */
    static void thuNo(final Char p) throws SQLException {
        final long no = NapTienDb.noLuong(p.userID);
        if (no <= 0L) {
            return;
        }
        final long[] th = NapTienLogic.thuHoi(p.getLuong(), no);
        if (th[0] <= 0L) {
            return;
        }
        if (!NapTienDb.truNo(p.userID, th[0])) {
            return;
        }
        if (!p.truLuongNeuDu(th[0])) {
            NapTienDb.congNo(p.userID, th[0]); // khong tru duoc trong RAM -> tra lai no
            return;
        }
        NapTienDb.ghiSoCai(p.userID, p.charDBID, "tra_no", -th[0], 0, "", "thuno:" + p.charDBID + ":" + System.nanoTime(),
                "thu hoi tu " + p.charname);
        try {
            Database.instance.saveCharAuto(p);
        } catch (final Exception ignored) {
        }
        NapTienDb.ghiNhatKy("thu_no", "", p.userID, p.charname + " -" + th[0] + " luong, con no " + th[1]);
        bao(p, "Thu hồi " + th[0] + " lượng do giao dịch nạp đã được hoàn tiền." + (th[1] > 0 ? " Còn nợ " + th[1] + " lượng." : ""));
        capNhatTui(p);
    }

    // ================================================================== tra xu cho

    static void traTienCho(final Char p) throws SQLException {
        for (final long[] r : NapTienDb.chuaTra(p.charDBID)) {
            if (!NapTienDb.chotTra(r[0])) {
                continue;
            }
            final long nhan = r[1] - r[2];
            if (nhan > 0L) {
                p.addXu(nhan, "nap tien cho trang tri", real.Char.XU_CHUYEN); // tien nguoi mua da tru thue
            }
            NapTienDb.ghiSoCai(p.userID, p.charDBID, "cho_ban", 0, 0, "", "chotra:" + r[0], "ban " + r[1] + " xu, thue " + r[2]);
            bao(p, "Chợ trang trí: đã bán, nhận " + nhan + " xu (thuế " + r[2] + " xu).");
        }
        capNhatTui(p);
    }

    // ================================================================== ve mua

    static void diemOnline(final List<Char> online, final long giay) {
        final long ngay = System.currentTimeMillis() / 86400000L;
        final int mua = NapTienLogic.muaVe(LauDai.ngayHienTai());
        final String homNay = NapTienDb.homNay();
        for (final Char p : online) {
            long[] o = ONLINE.get(p.charDBID);
            if (o == null || o[0] != ngay) {
                o = new long[]{ngay, 0L, 0L};
                ONLINE.put(p.charDBID, o);
            }
            o[1] += giay;
            final long giayMoiDiem = NapTienLogic.VE_MUA_PHUT_MOI_DIEM * 60L;
            if (o[1] >= giayMoiDiem && o[2] < NapTienLogic.VE_MUA_DIEM_ONLINE_NGAY) {
                final int them = (int) Math.min(o[1] / giayMoiDiem, NapTienLogic.VE_MUA_DIEM_ONLINE_NGAY - o[2]);
                o[1] -= them * giayMoiDiem;
                try {
                    o[2] += NapTienDb.congDiem(p.charDBID, mua, them, homNay, NapTienLogic.VE_MUA_DIEM_NGAY_TOI_DA);
                } catch (final SQLException ignored) {
                }
            }
        }
    }

    /**
     * Moc cho he thong khac (nhiem vu ngay/tuan, Pokedex...) cong diem ve mua. Tran 120 / ngay
     * moi nguon. Tat co -> khong lam gi. @return diem thuc cong.
     */
    public static int congDiemVeMua(final Char p, final int diem, final String nguon) {
        if (!NapTien.bat() || p == null || diem <= 0) {
            return 0;
        }
        try {
            return NapTienDb.congDiem(p.charDBID, NapTienLogic.muaVe(LauDai.ngayHienTai()), diem, NapTienDb.homNay(),
                    NapTienLogic.VE_MUA_DIEM_NGAY_TOI_DA);
        } catch (final SQLException e) {
            return 0;
        }
    }

    static String nhanThuongVeMua(final Char p) throws SQLException {
        final int mua = NapTienLogic.muaVe(LauDai.ngayHienTai());
        final Object[] v = NapTienDb.veMua(p.charDBID, mua);
        final boolean caoCap = (Integer) v[0] == 1;
        final int bac = NapTienLogic.bacTuDiem((Integer) v[1]);
        final StringBuilder kq = new StringBuilder();
        int da = 0;
        int tt = 0;
        for (final boolean track : new boolean[]{false, true}) {
            if (track && !caoCap) {
                continue;
            }
            final int cu = (Integer) v[track ? 5 : 4];
            if (bac <= cu || !NapTienDb.nhanBac(p.charDBID, mua, track, cu, bac)) {
                continue;
            }
            for (int b = cu + 1; b <= bac; b++) {
                final String t = NapTienLogic.thuongBac(track, b);
                if (t.startsWith("DA:")) {
                    final int n = Integer.parseInt(t.substring(3));
                    p.doAddGemItem(NapTienLogic.ID_DA_NANG_CAP, n, true);
                    da += n;
                } else if (t.startsWith("TT:")) {
                    NapTienDb.themSoHuu(p.charDBID, t.substring(3), "ve_mua", false, 0L);
                    tt++;
                } else if (t.startsWith("TG:")) {
                    final String[] g = t.split(":");
                    real.TangTocGame.thuongKhoa(p, TangToc.loaiCuaMa(g[1]), Integer.parseInt(g[2]), "ve mua bac " + b);
                    tt++;
                }
            }
        }
        if (da == 0 && tt == 0) {
            return "Vé mùa " + (mua + 1) + ": bậc " + bac + "/" + NapTienLogic.VE_MUA_SO_BAC + " (" + v[1] + " điểm). Chưa có thưởng mới."
                    + (caoCap ? "" : " Muốn nhận thêm thưởng cao cấp, hãy mua Vé mùa cao cấp trên website.");
        }
        try {
            Database.instance.saveCharAuto(p);
        } catch (final Exception ignored) {
        }
        capNhatTui(p);
        kq.append("Vé mùa: nhận ").append(da).append(" đá nâng cấp (khoá), ").append(tt).append(" món trang trí / tăng tốc (khoá).");
        return kq.toString();
    }

    // ================================================================== the thang

    static String nhanTheThang(final Char p) throws SQLException {
        final long bayGio = System.currentTimeMillis();
        final Object[] t = NapTienDb.theThang(p.charDBID);
        final long han = (Long) t[0];
        if (!NapTienLogic.theConHan(han, bayGio)) {
            return "Bạn chưa có Thẻ tháng. Thẻ tháng (30 ngày) được bán trên website, mục Nạp tiền.";
        }
        final String homNay = NapTienDb.homNay();
        if (!NapTienLogic.coTheNhanNgay(han, bayGio, (String) t[1], homNay) || !NapTienDb.nhanNgay(p.charDBID, homNay, bayGio)) {
            return "Hôm nay đã nhận lượng Thẻ tháng. Còn " + soNgayCon(han, bayGio) + " ngày.";
        }
        final int l = NapTienLogic.THE_THANG_LUONG_NGAY;
        if (!NapTienDb.ghiSoCai(p.userID, p.charDBID, "the_thang_ngay", l, 0, "",
                NapTienLogic.thamChieuTheThang(p.charDBID, homNay), "the thang")) {
            return "Hôm nay đã nhận lượng Thẻ tháng.";
        }
        final long[] tn = NapTienLogic.congCoNo(NapTienDb.noLuong(p.userID), l);
        if (tn[0] > 0L && !NapTienDb.truNo(p.userID, tn[0])) {
            tn[1] = l;
        }
        if (tn[1] > 0L) {
            p.addLuong((int) tn[1]);
            NapTien.ghiVao(tn[1]);
        }
        try {
            Database.instance.saveCharAuto(p);
        } catch (final Exception ignored) {
        }
        capNhatTui(p);
        return "Thẻ tháng: +" + tn[1] + " lượng" + (tn[0] > 0 ? " (đã trừ " + tn[0] + " lượng nợ)" : "") + ". Còn "
                + soNgayCon(han, bayGio) + " ngày.";
    }

    static long soNgayCon(final long han, final long bayGio) {
        return Math.max(0L, (han - bayGio + 86400000L - 1) / 86400000L);
    }

    static boolean coThe(final Char p) {
        try {
            return NapTienLogic.theConHan((Long) NapTienDb.theThang(p.charDBID)[0], System.currentTimeMillis());
        } catch (final Exception e) {
            return false;
        }
    }

    // ================================================================== moc noi cho file game

    /** RequestNPCInfoHandler (NPC -10): noi muc vao cuoi menu, ghi vi tri. Tat co: menu y nguyen. */
    public static String[] noiMenuTongQuan(final Char p, final String[] menu) {
        if (!NapTien.bat() || p == null || menu == null) {
            if (p != null) {
                VI_TRI.remove(p.charDBID);
            }
            return menu;
        }
        final String[] m = java.util.Arrays.copyOf(menu, menu.length + 1);
        m[menu.length] = TEN_MUC;
        VI_TRI.put(p.charDBID, menu.length);
        return m;
    }

    /** MapDoMenu: true neu da xu ly (npc -10 muc cua nap, hoac npc ao -540..-549). */
    public static boolean xuLyMenu(final Char p, final int idNpc, final int idMenu, final int chon) {
        if (!NapTien.bat() || p == null) {
            return false;
        }
        if (idNpc == -10) {
            final Integer vt = VI_TRI.get(p.charDBID);
            if (idMenu != 0 || vt == null || vt != chon) {
                return false;
            }
            moGoc(p);
            return true;
        }
        if (idNpc > NPC_GOC || idNpc < NPC_RAO_GIA) {
            return false;
        }
        try {
            switch (idNpc) {
                case NPC_GOC:
                    chonGoc(p, chon);
                    break;
                case NPC_NHOM:
                    chonNhom(p, chon);
                    break;
                case NPC_MON:
                    chonMon(p, chon);
                    break;
                case NPC_XAC_NHAN:
                    xacNhanMua(p, chon);
                    break;
                case NPC_TU:
                    chonTu(p, chon);
                    break;
                case NPC_TU_MON:
                    thaoTacTu(p, chon);
                    break;
                case NPC_CHO:
                    chonMaCho(p, chon);
                    break;
                case NPC_CHO_DONG:
                    chonDongCho(p, chon);
                    break;
                case NPC_CHO_MUA:
                    xacNhanMuaCho(p, chon);
                    break;
                case NPC_RAO_GIA:
                    chonGiaRao(p, chon);
                    break;
                default:
                    return false;
            }
        } catch (final SQLException e) {
            System.out.println("NAP TIEN: loi CSDL menu " + idNpc + " - " + e.getMessage());
            bao(p, "Cửa hàng tạm thời gián đoạn, vui lòng thử lại sau.");
        }
        return true;
    }

    /** CharDoiTenNhanVat: gia doi ten (bat co: gia cua hang, tat co: gia cu). */
    public static int giaDoiTen(final int giaCu) {
        return NapTien.bat() ? NapTienLogic.GIA_DOI_TEN : giaCu;
    }

    /** CharDoiTenNhanVat: con phai cho? null = duoc doi. Tat co: luon null. */
    public static String kiemDoiTen(final Char p) {
        if (!NapTien.bat() || p == null) {
            return null;
        }
        try {
            final long[] d = NapTienDb.demMua(p.charDBID, "doi_ten");
            final int cho = NapTienLogic.ngayChoDoiTen(d[1], System.currentTimeMillis());
            if (cho > 0) {
                return "Mỗi " + NapTienLogic.DOI_TEN_CACH_NGAY + " ngày chỉ được đổi tên một lần (còn " + cho + " ngày).";
            }
            if (NapTienDb.noLuong(p.userID) > 0L) {
                return "Tài khoản đang có khoản thu hồi nạp chưa trả, tạm khoá mua bằng lượng.";
            }
            return null;
        } catch (final SQLException e) {
            return "Cửa hàng tạm thời gián đoạn, vui lòng thử lại sau.";
        }
    }

    /** CharDoiTenNhanVat: ghi lan doi ten thanh cong (so cai: luong bi dot). */
    public static void ghiDoiTen(final Char p, final int gia) {
        if (!NapTien.bat() || p == null) {
            return;
        }
        try {
            final long[] d = NapTienDb.demMua(p.charDBID, "doi_ten");
            NapTienDb.ghiMua(p.charDBID, "doi_ten", d[0], System.currentTimeMillis());
            NapTienDb.ghiSoCai(p.userID, p.charDBID, "tieu", -gia, 0, "", "tieu:" + p.charDBID + ":doi_ten:" + System.nanoTime(),
                    "doi ten");
        } catch (final SQLException ignored) {
        }
    }

    /**
     * HamNgucGame.nhanThuong khi hom nay da nhan: the thang cho them 1 luot - thuong CHI la diem ve
     * mua (khong EXP, khong vat lieu, khong luong). null = khong co luot them (dung thong bao cu).
     */
    public static String luotHamNgucThem(final Char p) {
        if (!NapTien.bat() || p == null || !coThe(p)) {
            return null;
        }
        try {
            final long[] d = NapTienDb.demMua(p.charDBID, "ham_nguc_them");
            final long hom = System.currentTimeMillis() / 86400000L;
            if (d[1] / 86400000L == hom) {
                return null;
            }
            if (!NapTienDb.ghiMua(p.charDBID, "ham_nguc_them", d[0], System.currentTimeMillis())) {
                return null;
            }
            final int duoc = congDiemVeMua(p, NapTienLogic.THE_THANG_DIEM_VE_MUA_LUOT_THEM, "ham_nguc_the_thang");
            return "Lượt Hầm ngục bí ẩn thêm (Thẻ tháng): +" + duoc + " điểm Vé mùa.";
        } catch (final SQLException e) {
            return null;
        }
    }

    /** Map.doGetItem: ban kinh nhat them (px) cho nguoi co the thang. 0 khi tat co. */
    public static int banKinhNhatThemPx(final Char p) {
        if (!NapTien.bat() || p == null) {
            return 0;
        }
        return coTheNho(p) ? NapTienLogic.THE_THANG_BAN_KINH_NHAT_THEM * 16 : 0;
    }

    /**
     * Ve mua CAO CAP cua mua hien tai da mo (NangLuong: +phut nang luong 100% / ngay). Goi MOT lan / ngay / nhan vat
     * (luc khoi tao nang luong cua ngay). Tat nap / loi CSDL = false.
     */
    public static boolean coVeMuaCaoCap(final Char p) {
        if (!NapTien.bat() || p == null) {
            return false;
        }
        try {
            final Object[] v = NapTienDb.veMua(p.charDBID, NapTienLogic.muaVe(LauDai.ngayHienTai()));
            return (Integer) v[0] == 1;
        } catch (final Exception e) {
            return false;
        }
    }

    /** Nho the thang 60 giay (doGetItem goi rat nhieu, khong hoi CSDL moi lan). */
    private static final ConcurrentHashMap<Integer, long[]> THE_NHO = new ConcurrentHashMap<Integer, long[]>();

    static boolean coTheNho(final Char p) {
        final long t = System.currentTimeMillis();
        final long[] n = THE_NHO.get(p.charDBID);
        if (n != null && t - n[0] < 60000L) {
            return n[1] > t;
        }
        long han = 0L;
        try {
            han = (Long) NapTienDb.theThang(p.charDBID)[0];
        } catch (final Exception ignored) {
        }
        THE_NHO.put(p.charDBID, new long[]{t, han});
        return han > t;
    }

    // ================================================================== menu goc

    public static void moGoc(final Char p) {
        if (real.HlvAiGame.chanTienThat(p)) { // huan luyen vien AI khong nap / mua goi
            return;
        }
        String the = "Thẻ tháng: chưa có";
        String ve = "Vé mùa";
        long no = 0L;
        try {
            final long han = (Long) NapTienDb.theThang(p.charDBID)[0];
            final long bayGio = System.currentTimeMillis();
            if (NapTienLogic.theConHan(han, bayGio)) {
                the = "Thẻ tháng: nhận lượng";
            }
            final int mua = NapTienLogic.muaVe(LauDai.ngayHienTai());
            final Object[] v = NapTienDb.veMua(p.charDBID, mua);
            ve = "Vé mùa " + (mua + 1) + ((Integer) v[0] == 1 ? " (cao cấp)" : "") + ": bậc "
                    + NapTienLogic.bacTuDiem((Integer) v[1]) + "/" + NapTienLogic.VE_MUA_SO_BAC;
            no = NapTienDb.noLuong(p.userID);
        } catch (final SQLException e) {
            bao(p, "Cửa hàng tạm thời gián đoạn, vui lòng thử lại sau.");
            return;
        }
        final String[] menu = {
            "Số dư: " + TienTeChu.chu(TienTeChu.LUONG, p.getLuong()) + (no > 0 ? " (nợ thu hồi " + TienTeChu.chu(TienTeChu.LUONG, no) + ")" : ""),
            "Cửa hàng lượng",
            the,
            ve,
            "Tủ đồ trang trí",
            "Chợ trang trí (thuế " + (NapTienLogic.thueBanPhanTram() + NapTienLogic.phiDangPhanTram()) + "%)",
            "Hướng dẫn nạp",
        };
        p.sendMessage(MessageCreator.createMsgMenu(menu, NPC_GOC, 0));
    }

    static void chonGoc(final Char p, final int chon) throws SQLException {
        switch (chon) {
            case 0:
                bao(p, "Lượng chỉ dùng để mua đồ trang trí và tiện ích (không bán sức mạnh). Lượng đã tiêu sẽ mất hẳn, không"
                        + " quy đổi ra xu. Nạp trên website với tỷ giá cố định " + NapTienLogic.LUONG_MOI_1000_VND
                        + " lượng cho mỗi 1.000 đ.");
                return;
            case 1:
                moNhom(p);
                return;
            case 2:
                bao(p, nhanTheThang(p));
                return;
            case 3:
                bao(p, nhanThuongVeMua(p));
                return;
            case 4:
                moTu(p);
                return;
            case 5:
                moCho(p);
                return;
            case 6:
                bao(p, "Nạp trên website (mục Nạp tiền): chọn gói, chọn Pokémon nhận, rồi chuyển khoản qua VietQR với đúng nội dung"
                        + " chuyển khoản. Lượng, Thẻ tháng và Vé mùa được cộng tự động vào Pokémon trong khoảng 15 giây"
                        + " khi đang online. Hạn mức nạp mặc định: " + soTien(NapTienLogic.TRAN_TUAN_VND) + " đ/tuần, "
                        + soTien(NapTienLogic.TRAN_THANG_VND) + " đ/tháng.");
                return;
            default:
        }
    }

    /** So tien kieu Viet: 1000000 -> "1.000.000". */
    static String soTien(final long v) {
        return String.format(java.util.Locale.ROOT, "%,d", v).replace(',', '.');
    }

    // ================================================================== cua hang

    static List<NapTienLogic.Mon> dangBan() throws SQLException {
        final int tuan = NapTienLogic.tuanTuNgayEpoch(System.currentTimeMillis() / 86400000L);
        return NapTienLogic.monDangBan(NapTienDb.cuaHang(), tuan);
    }

    static void moNhom(final Char p) throws SQLException {
        final List<NapTienLogic.Mon> ds = dangBan();
        final List<String> nhom = new ArrayList<String>();
        final List<String> ten = new ArrayList<String>();
        for (int i = 0; i < NapTienLogic.NHOM_CUA_HANG.length; i++) {
            int dem = 0;
            for (final NapTienLogic.Mon m : ds) {
                dem += NapTienLogic.nhomHienThi(m.nhom).equals(NapTienLogic.NHOM_CUA_HANG[i]) ? 1 : 0;
            }
            if (dem > 0) {
                nhom.add(NapTienLogic.NHOM_CUA_HANG[i]);
                ten.add(NapTienLogic.TEN_NHOM_CUA_HANG[i] + " (" + dem + ")");
            }
        }
        if (nhom.isEmpty()) {
            bao(p, "Cửa hàng tuần này chưa có hàng.");
            return;
        }
        NGU_CANH.put(p.charDBID, nhom);
        p.sendMessage(MessageCreator.createMsgMenu(ten.toArray(new String[0]), NPC_NHOM, 0));
    }

    @SuppressWarnings("unchecked")
    static <T> List<T> nguCanh(final Char p) {
        final Object o = NGU_CANH.get(p.charDBID);
        return o instanceof List ? (List<T>) o : null;
    }

    static void chonNhom(final Char p, final int chon) throws SQLException {
        final List<String> nhom = nguCanh(p);
        if (nhom == null || chon < 0 || chon >= nhom.size()) {
            return;
        }
        final List<NapTienLogic.Mon> mon = new ArrayList<NapTienLogic.Mon>();
        final List<String> ten = new ArrayList<String>();
        for (final NapTienLogic.Mon m : dangBan()) {
            if (NapTienLogic.nhomHienThi(m.nhom).equals(nhom.get(chon))) {
                mon.add(m);
                ten.add(m.ten + " - " + giaThat(m) + " lượng" + (m.giaoDichDuoc ? " (giao dịch được)" : ""));
            }
        }
        NGU_CANH.put(p.charDBID, mon);
        p.sendMessage(MessageCreator.createMsgMenu(ten.toArray(new String[0]), NPC_MON, 0));
    }

    static void chonMon(final Char p, final int chon) {
        final List<NapTienLogic.Mon> mon = nguCanh(p);
        if (mon == null || chon < 0 || chon >= mon.size()) {
            return;
        }
        final NapTienLogic.Mon m = mon.get(chon);
        if (NapTienLogic.TRAO_DOI_TEN.equals(m.trao)) {
            // Doi ten: tru luong o CharDoiTenNhanVat (gia + gioi han 30 ngay qua moc noi o do).
            final String chan = kiemDoiTen(p);
            if (chan != null) {
                bao(p, chan);
                return;
            }
            p.sendMessage(MessageCreator.createMsgInputText(CharDoiTenNhanVat.INPUT_DOI_TEN, 0,
                    "Nhập tên mới (phí " + NapTienLogic.GIA_DOI_TEN + " lượng)", 0));
            return;
        }
        final List<NapTienLogic.Mon> mot = new ArrayList<NapTienLogic.Mon>();
        mot.add(m);
        NGU_CANH.put(p.charDBID, mot);
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Mua " + m.ten + " - " + giaThat(m) + " lượng", "Thôi"},
                NPC_XAC_NHAN, 0));
    }

    static void xacNhanMua(final Char p, final int chon) throws SQLException {
        if (real.HlvAiGame.chanTienThat(p)) {
            return;
        }
        final List<NapTienLogic.Mon> mot = nguCanh(p);
        NGU_CANH.remove(p.charDBID);
        if (chon != 0 || mot == null || mot.size() != 1) {
            return;
        }
        bao(p, mua(p, mot.get(0)));
    }

    /**
     * [trang-phuc] Mua mon cua hang theo ma (Tu do trang tri / goi -116 kieu 5): cung DUONG mua (so cai, khoan thu hoi,
     * gioi han, hoan luong khi loi). Tra thong bao cho nguoi choi.
     */
    public static String muaTheoMa(final Char p, final String ma) {
        try {
            for (final NapTienLogic.Mon m : NapTienDb.cuaHang()) {
                if (m.ma.equals(ma)) {
                    return mua(p, m);
                }
            }
            return "Món này không còn bán.";
        } catch (final SQLException e) {
            return "Cửa hàng tạm thời không mở được, thử lại sau.";
        }
    }

    /** Mua mot mon cua hang. Luong tru TRUOC (khoa tien), trao that bai thi hoan lai. */
    static String mua(final Char p, final NapTienLogic.Mon m) throws SQLException {
        if (NapTienLogic.kiemMon(m) != null) {
            return "Món này không còn bán.";
        }
        if (NapTienDb.noLuong(p.userID) > 0L) {
            return "Tài khoản đang có khoản thu hồi nạp chưa trả, tạm khoá mua bằng lượng.";
        }
        final boolean tangToc = NapTienLogic.TRAO_TANG_TOC.equals(m.trao);
        // The dat ten Pokemon: gia + so lan / tuan tu cau hinh (dat_ten_pet.*), dem theo tuan nhu tang toc.
        final boolean datTen = NapTienLogic.TRAO_DAT_TEN_PET.equals(m.trao);
        final int gia = giaThat(m);
        final int gioiHan = datTen ? real.DatTenPetGame.muaTuanToiDa() : m.gioiHan;
        if (datTen && (!real.DatTenPetGame.bat() || gioiHan <= 0)) {
            return "Thẻ đặt tên đang tạm ngừng bán. Vui lòng quay lại sau.";
        }
        if (tangToc && !com.htbb.cauhinh.CongTac.banTangToc()) {
            // Cong tac nap.tang_toc.bat (trang "Cau hinh he thong"): tam ngung ban ban cao cap.
            return "Vật phẩm tăng tốc đang tạm ngừng bán. Vui lòng quay lại sau.";
        }
        // Tang toc: gioi_han tinh theo TUAN (dem rieng moi tuan: ma@tuan), tui can 1 o trong.
        final String maDem = tangToc || datTen ? m.ma + "@" + NhiemVuLauDai.tuan(System.currentTimeMillis()) : m.ma;
        final long[] dem = NapTienDb.demMua(p.charDBID, maDem);
        if (gioiHan > 0 && dem[0] >= gioiHan) {
            return tangToc || datTen ? "Tuần này đã mua tối đa " + gioiHan + " lần món này (mở lại thứ Hai)."
                    : "Bạn đã mua tối đa " + gioiHan + " lần món này.";
        }
        if ((tangToc || datTen) && (!LauDai.bat() || p.countSlotInventory() < 1)) {
            return "Hành trang cần 1 ô trống.";
        }
        final boolean veMoRong = NapTienLogic.laVeMoRong(m);
        final boolean veOThu = NapTienLogic.laVeMoOThu(m);
        if (veOThu) {
            // Ve +1 o thu (real.QuanLyThuGame): chi ban khi ve con dung duoc (tinh ca ve dang giu toi o toi da).
            final String loiVe = real.QuanLyThuGame.kiemMuaVe(p);
            if (loiVe != null) {
                return loiVe;
            }
        } else if (veMoRong) {
            // Ve +10 o (real.MoRongTuiGame): chi ban khi ve con dung duoc (tinh ca ve dang giu), tui can cho cho ve.
            final String loiVe = real.MoRongTuiGame.kiemMuaVe(p);
            if (loiVe != null) {
                return loiVe;
            }
        } else if (NapTienLogic.TRAO_MO_RONG_TUI.equals(m.trao) && p.maxBag >= Char.MAX_BAG_LIMIT) {
            return "Hành trang đã mở rộng tối đa.";
        }
        if (NapTienLogic.TRAO_TRANG_TRI.equals(m.trao) && !real.TrangPhucGame.laMa(m.ma)
                && real.TrangPhucGame.demNgoaiDanhMuc(NapTienDb.soHuu(p.charDBID, System.currentTimeMillis()))
                >= TU_DO_O + (coThe(p) ? 10 * NapTienLogic.THE_THANG_TRANG_TU_DO_THEM : 0)) {
            return "Tủ đồ trang trí đã đầy.";
        }
        if (NapTienLogic.TRAO_TRANG_CHIEU.equals(m.trao) && real.NgoaiHinhGame.soTrangChieu(p) >= 3) {
            return "Đã mở đủ 3 trang chiêu.";
        }
        if (!p.truLuongNeuDu(gia)) {
            return "Không đủ lượng (cần " + gia + " lượng).";
        }
        if (!NapTienDb.ghiMua(p.charDBID, maDem, dem[0], System.currentTimeMillis())) {
            p.addLuong(gia); // bam hai lan cung luc: hoan lan sau
            return "Thao tác quá nhanh, vui lòng thử lại.";
        }
        try {
            if (NapTienLogic.TRAO_TRANG_CHIEU.equals(m.trao)) {
                if (!real.NgoaiHinhGame.moTrangChieu(p)) {
                    throw new IllegalStateException("da du trang chieu");
                }
            } else if (veOThu) {
                real.QuanLyThuGame.traoVe(p, 1);
            } else if (veMoRong) {
                real.MoRongTuiGame.traoVe(p, 1);
            } else if (NapTienLogic.TRAO_MO_RONG_TUI.equals(m.trao)) {
                if (p.maxBag >= Char.MAX_BAG_LIMIT) {
                    throw new IllegalStateException("tui toi da");
                }
                p.maxBag++;
                try {
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                } catch (final Exception ignored) {
                }
            } else if (datTen) {
                // The dat ten vao tui (KHOA, khong giao dich): id theo hieu ung "dat_ten" cua data_vat_pham_lau_dai.
                final int id = real.VatPhamDungLauDai.idTheoHieuUng(real.DatTenPetGame.HIEU_UNG);
                if (id <= 0 || real.Map.itemTemplates.get(5).get(Integer.valueOf(id)) == null) {
                    throw new IllegalStateException("chua co vat pham the dat ten");
                }
                Char.createItemSoLuong(id, 1, p);
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            } else if (tangToc) {
                // Ban CAO CAP vao tui (khoa, khong giao dich). Dung chung tran diem ngay/tuan voi ban khoa.
                final int loai = TangToc.loaiCuaMa(m.ma);
                if (loai < 0 || real.Map.itemTemplates.get(5).get(Integer.valueOf(TangToc.ID_CAO_CAP[loai])) == null) {
                    throw new IllegalStateException("chua co vat pham tang toc " + m.ma);
                }
                Char.createItemSoLuong(TangToc.ID_CAO_CAP[loai], 1, p);
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            } else {
                final long han = m.hanNgay > 0 ? System.currentTimeMillis() + m.hanNgay * 86400000L : 0L;
                NapTienDb.themSoHuu(p.charDBID, m.ma, "mua", m.giaoDichDuoc, han);
            }
        } catch (final Exception e) {
            p.addLuong(gia);
            NapTienDb.hoanMua(p.charDBID, maDem);
            return "Mua thất bại, đã hoàn " + gia + " lượng.";
        }
        NapTienDb.ghiSoCai(p.userID, p.charDBID, "tieu", -gia, 0, "",
                "tieu:" + p.charDBID + ":" + m.ma + ":" + System.nanoTime(), m.ten);
        try {
            Database.instance.saveCharAuto(p);
        } catch (final Exception ignored) {
        }
        capNhatTui(p);
        return "Đã mua " + m.ten + " (-" + gia + " lượng)."
                + (NapTienLogic.TRAO_TRANG_TRI.equals(m.trao) ? " Xem trong Tủ đồ trang trí." : "")
                + (veMoRong ? " Vé đã vào hành trang (tab nguyên liệu): bấm Dùng để thêm " + MoRongHanhTrang.O_MOI_VE + " ô." : "")
                + (veOThu ? " Vé đã vào hành trang (tab nguyên liệu): bấm Dùng để thêm " + OThu.O_MOI_VE + " ô thú." : "")
                + (tangToc && TangToc.loaiCuaMa(m.ma) == TangToc.BUA
                        ? " Bùa đã vào hành trang: mỗi bùa +" + TangToc.BUA_PHUT + " phút năng lượng mức " + TangToc.buaHeSoExp()
                                + "% EXP, " + TangToc.buaHeSoXu() + "% xu hôm nay, dùng bao nhiêu tuỳ ý, không tính vào giới hạn tăng tốc." : "")
                + (tangToc && TangToc.loaiCuaMa(m.ma) != TangToc.BUA ? " Vật phẩm đã vào hành trang; dùng chung giới hạn "
                        + TangToc.DIEM_NGAY_TOI_DA + " điểm/ngày, " + TangToc.DIEM_TUAN_TOI_DA + " điểm/tuần với bản kiếm được khi chơi." : "")
                + (datTen ? " Thẻ đã vào hành trang: bấm Sử dụng để đặt tên cho Pokémon đi theo." : "");
    }

    /** Gia luong that cua mon dang ban theo ma (danh muc nho 60 giay); -1 = khong ban / cua hang tat / loi CSDL. */
    public static int giaLuongTheoMa(final String ma) {
        if (!NapTien.bat()) {
            return -1;
        }
        try {
            for (final NapTienLogic.Mon m : NapTienDb.cuaHang()) {
                if (m.ma.equals(ma)) {
                    return giaThat(m);
                }
            }
        } catch (final Exception e) {
            return -1;
        }
        return -1;
    }

    /** Gia luong THAT cua mon: the dat ten theo cau hinh dat_ten_pet.gia_luong, mon khac theo dong nap_cua_hang. */
    static int giaThat(final NapTienLogic.Mon m) {
        return NapTienLogic.TRAO_DAT_TEN_PET.equals(m.trao) ? real.DatTenPetGame.giaLuong() : m.giaLuong;
    }

    // ================================================================== tu do

    static String tenMa(final String ma) {
        final String tt = real.TrangPhucGame.ten(ma); // tu do trang tri (mot he, dong bo 0.9.8)
        if (tt != null) {
            return tt;
        }
        try {
            for (final NapTienLogic.Mon m : NapTienDb.cuaHang()) {
                if (m.ma.equals(ma)) {
                    return m.ten;
                }
            }
        } catch (final SQLException ignored) {
        }
        for (final NapTienLogic.Mon m : NapTienLogic.MON_MAC_DINH) {
            if (m.ma.equals(ma)) {
                return m.ten;   // mon cu da ngung ban (khong con trong cuaHang())
            }
        }
        return ma.replace('_', ' ');
    }

    static void moTu(final Char p) throws SQLException {
        final List<NapTienDb.SoHuu> ds = NapTienDb.soHuu(p.charDBID, System.currentTimeMillis());
        if (ds.isEmpty()) {
            bao(p, "Tủ đồ trang trí trống.");
            return;
        }
        final List<String> ten = new ArrayList<String>();
        for (final NapTienDb.SoHuu s : ds) {
            ten.add((s.dangDung ? "(Đang dùng) " : "") + (s.dangRao ? "(Đang rao) " : "") + tenMa(s.ma)
                    + (s.hetHanMs > 0 ? " (còn " + soNgayCon(s.hetHanMs, System.currentTimeMillis()) + " ngày)" : ""));
        }
        NGU_CANH.put(p.charDBID, ds);
        p.sendMessage(MessageCreator.createMsgMenu(ten.toArray(new String[0]), NPC_TU, 0));
    }

    static void chonTu(final Char p, final int chon) {
        final List<NapTienDb.SoHuu> ds = nguCanh(p);
        if (ds == null || chon < 0 || chon >= ds.size()) {
            return;
        }
        final NapTienDb.SoHuu s = ds.get(chon);
        final List<NapTienDb.SoHuu> mot = new ArrayList<NapTienDb.SoHuu>();
        mot.add(s);
        NGU_CANH.put(p.charDBID, mot);
        final List<String> m = new ArrayList<String>();
        // dong bo 0.9.8: mac / thao mon tu do CHI o Ho so > Tu do (client 908); mon cu khong con cong dung: Thong tin
        m.add(!real.TrangPhucGame.laMa(s.ma) ? "Thông tin" : real.TrangPhucGame.coManTuDo(p) ? "Mặc ở Hồ sơ > Ngoại hình"
                : s.dangDung ? "Bỏ dùng" : "Dùng");
        if (s.dangRao) {
            m.add("Huỷ rao bán");
        } else if (s.giaoDichDuoc && s.hetHanMs == 0) {
            m.add("Rao bán trên Chợ trang trí");
        }
        p.sendMessage(MessageCreator.createMsgMenu(m.toArray(new String[0]), NPC_TU_MON, 0));
    }

    static void thaoTacTu(final Char p, final int chon) throws SQLException {
        final List<NapTienDb.SoHuu> mot = nguCanh(p);
        if (mot == null || mot.size() != 1) {
            return;
        }
        final NapTienDb.SoHuu s = mot.get(0);
        if (chon == 0 && real.TrangPhucGame.laMa(s.ma) && real.TrangPhucGame.coManTuDo(p)) {
            NGU_CANH.remove(p.charDBID);
            real.TrangPhucGame.guiTuDo(p);
            bao(p, tenMa(s.ma) + " nằm trong Tủ đồ: mở Hồ sơ > Ngoại hình để mặc, tháo và chọn dòng chỉ số của ô.");
            return;
        }
        if (chon == 0 && !real.TrangPhucGame.laMa(s.ma)) {
            NGU_CANH.remove(p.charDBID);
            bao(p, tenMa(s.ma) + ": món của cửa hàng cũ, nay không còn công dụng trong game (đã ngừng bán). Bạn vẫn giữ món"
                    + (s.giaoDichDuoc && s.hetHanMs == 0 ? " và rao bán được trên Chợ trang trí." : "."));
            return;
        }
        if (chon == 0 && real.TrangPhucGame.laMa(s.ma)) {
            NGU_CANH.remove(p.charDBID);
            bao(p, real.TrangPhucGame.macTuMenu(p, s.ma, !s.dangDung)); // [trang-phuc] moi o mot mon + doi hinh / chi so
            return;
        }
        if (chon == 0) {
            NGU_CANH.remove(p.charDBID);
            bao(p, NapTienDb.datDung(s.id, p.charDBID, !s.dangDung)
                    ? (s.dangDung ? "Đã bỏ dùng " : "Đang dùng ") + tenMa(s.ma) + "." : "Không đổi được (món đang rao?).");
            return;
        }
        if (chon == 1 && s.dangRao) {
            NGU_CANH.remove(p.charDBID);
            bao(p, NapTienDb.huyRao(s.id, p.charDBID) ? "Đã huỷ rao (phí đăng không hoàn)." : "Không huỷ được (đã bán?).");
            return;
        }
        if (chon == 1 && s.giaoDichDuoc && s.hetHanMs == 0) {
            final long gio = ThongSoLauDai.xuMoiGio(LauDai.kepCap(p.lvDetail == null ? 1 : p.lvDetail.lv));
            final List<String> m = new ArrayList<String>();
            final List<Long> gia = new ArrayList<Long>();
            for (final int hs : NapTienLogic.HE_SO_GIA_GIO_PHAN_TRAM) {
                final long g = Math.max(1L, gio * hs / 100L);
                gia.add(g);
                m.add(g + " xu");
            }
            final long tv = NapTienLogic.trungVi(NapTienDb.giaGanDay(s.ma));
            NGU_CANH.put(p.charDBID, new Object[]{s, gia});
            p.sendMessage(MessageCreator.createMsgMenu(m.toArray(new String[0]), NPC_RAO_GIA, 0));
            if (tv > 0L) {
                bao(p, "Giá giao dịch trung vị gần đây: " + tv + " xu (giá rao phải trong 50%-200%).");
            }
        }
    }

    static void chonGiaRao(final Char p, final int chon) throws SQLException {
        if (ThanhVien.chanVaBao(p, "cho")) { // thanh vien
            NGU_CANH.remove(p.charDBID);
            return;
        }
        final Object o = NGU_CANH.remove(p.charDBID);
        if (!(o instanceof Object[])) {
            return;
        }
        final NapTienDb.SoHuu s = (NapTienDb.SoHuu) ((Object[]) o)[0];
        @SuppressWarnings("unchecked")
        final List<Long> gia = (List<Long>) ((Object[]) o)[1];
        if (chon < 0 || chon >= gia.size()) {
            return;
        }
        final long g = gia.get(chon);
        if (ChongClone.chanMotBen(p, ChongClone.KIEU_CHO_TRANG_TRI)) { // chong clone: nguoi rao du cap / tuoi
            return;
        }
        final String loi = NapTienLogic.kiemGiaRao(g, NapTienDb.giaGanDay(s.ma));
        if (loi != null) {
            bao(p, "Không rao được: " + loi);
            return;
        }
        if (NapTienDb.soDangRao(p.charDBID) >= NapTienLogic.RAO_TOI_DA) {
            bao(p, "Mỗi Pokémon rao tối đa " + NapTienLogic.RAO_TOI_DA + " món.");
            return;
        }
        final long phi = NapTienLogic.phiDang(g);
        if (!p.truXuNeuDu(phi, false, "nap tien phi dang cho")) {
            bao(p, "Cần " + phi + " xu phí đăng (1%).");
            return;
        }
        if (!NapTienDb.rao(s.id, p.charDBID, p.charname, s.ma, g, phi)) {
            p.addXu(phi, "nap tien hoan phi dang", real.Char.XU_HOAN);
            bao(p, "Món này không rao được.");
            return;
        }
        NapTienDb.ghiNhatKy("cho_rao", "", p.userID, p.charname + " rao " + s.ma + " #" + s.id + " gia " + g + " phi " + phi);
        capNhatTui(p);
        bao(p, "Đã rao " + tenMa(s.ma) + " giá " + g + " xu (phí đăng " + phi + " xu, thuế bán "
                + NapTienLogic.thueBanPhanTram() + "%).");
    }

    // ================================================================== cho

    static void moCho(final Char p) throws SQLException {
        final List<Object[]> ds = NapTienDb.maDangRao();
        if (ds.isEmpty()) {
            bao(p, "Chợ trang trí chưa có món nào. Rao bán từ Tủ đồ trang trí.");
            return;
        }
        final List<String> ma = new ArrayList<String>();
        final List<String> ten = new ArrayList<String>();
        for (final Object[] d : ds) {
            ma.add((String) d[0]);
            ten.add(tenMa((String) d[0]) + " - " + d[1] + " món, từ " + d[2] + " xu");
        }
        NGU_CANH.put(p.charDBID, ma);
        p.sendMessage(MessageCreator.createMsgMenu(ten.toArray(new String[0]), NPC_CHO, 0));
    }

    static void chonMaCho(final Char p, final int chon) throws SQLException {
        final List<String> ma = nguCanh(p);
        if (ma == null || chon < 0 || chon >= ma.size()) {
            return;
        }
        final List<NapTienDb.Rao> ds = NapTienDb.raoTheoMa(ma.get(chon));
        if (ds.isEmpty()) {
            bao(p, "Món này vừa hết.");
            return;
        }
        final List<String> ten = new ArrayList<String>();
        for (final NapTienDb.Rao r : ds) {
            ten.add(r.gia + " xu - " + r.tenNguoiBan);
        }
        NGU_CANH.put(p.charDBID, ds);
        p.sendMessage(MessageCreator.createMsgMenu(ten.toArray(new String[0]), NPC_CHO_DONG, 0));
    }

    static void chonDongCho(final Char p, final int chon) {
        final List<NapTienDb.Rao> ds = nguCanh(p);
        if (ds == null || chon < 0 || chon >= ds.size()) {
            return;
        }
        final NapTienDb.Rao r = ds.get(chon);
        final List<NapTienDb.Rao> mot = new ArrayList<NapTienDb.Rao>();
        mot.add(r);
        NGU_CANH.put(p.charDBID, mot);
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Mua " + tenMa(r.ma) + " - " + r.gia + " xu", "Thôi"},
                NPC_CHO_MUA, 0));
    }

    static void xacNhanMuaCho(final Char p, final int chon) throws SQLException {
        final List<NapTienDb.Rao> mot = nguCanh(p);
        NGU_CANH.remove(p.charDBID);
        if (chon != 0 || mot == null || mot.size() != 1) {
            return;
        }
        final String kq = muaCho(p, mot.get(0));
        if (kq != null) { // null = ChongClone da chan va da bao
            bao(p, kq);
        }
    }

    /** Mua tren cho: tru xu nguoi mua TRUOC, chot dong rao, chuyen so huu; nguoi ban nhan xu qua vong quet. */
    static String muaCho(final Char p, final NapTienDb.Rao r) throws SQLException {
        final String chanTv = ThanhVien.chan(p, "cho"); // thanh vien
        if (chanTv != null) {
            return chanTv;
        }
        if (r.nguoiBan == p.charDBID) {
            return "Không mua được món của chính mình.";
        }
        // chong clone: nguoi mua (gui xu) -> nguoi ban (co the offline).
        if (ChongClone.chanChuyen(p, r.nguoiBan, r.tenNguoiBan, ChongClone.KIEU_CHO_TRANG_TRI, r.gia)) {
            return null;
        }
        if (NapTienDb.soHuu(p.charDBID, System.currentTimeMillis()).size()
                >= TU_DO_O + (coThe(p) ? 10 * NapTienLogic.THE_THANG_TRANG_TU_DO_THEM : 0)) {
            return "Tủ đồ trang trí đã đầy.";
        }
        if (!p.truXuNeuDu(r.gia, false, "nap tien mua cho trang tri", real.Char.XU_CHUYEN)) {
            return "Không đủ " + r.gia + " xu.";
        }
        final long thue = NapTienLogic.thueBan(r.gia);
        if (!NapTienDb.chotMua(r.id, p.charDBID, thue)) {
            p.addXu(r.gia, "nap tien hoan mua cho", real.Char.XU_CHUYEN);
            return "Món đã có người mua trước.";
        }
        LauDai.ghiXuRa(thue); // gia = nguoi ban nhan + thue; chi thue bi dot
        NapTienDb.chuyenSoHuu(r.soHuuId, p.charDBID);
        ChongClone.ghi(p, r.nguoiBan, r.tenNguoiBan, ChongClone.KIEU_CHO_TRANG_TRI, r.gia, "trang tri " + r.ma);
        NapTienDb.ghiSoCai(p.userID, p.charDBID, "cho_mua", 0, 0, "", "chomua:" + r.id, r.ma + " " + r.gia + " xu, thue " + thue);
        NapTienDb.ghiNhatKy("cho_mua", "", p.userID, p.charname + " mua " + r.ma + " cua " + r.tenNguoiBan + " gia " + r.gia
                + " thue " + thue);
        try {
            Database.instance.saveCharAuto(p);
        } catch (final Exception ignored) {
        }
        capNhatTui(p);
        return "Đã mua " + tenMa(r.ma) + " với " + r.gia + " xu. Xem trong Tủ đồ trang trí.";
    }

    // ================================================================== tien ich

    static void bao(final Char p, final String s) {
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
        } catch (final Exception ignored) {
        }
    }

    static void capNhatTui(final Char p) {
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception ignored) {
        }
        real.MoRongTuiGame.guiTrangThai(p); // -105: suc chua / so ve moi (client >= 903)
    }
}
