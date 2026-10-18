package server;

import data.Database;
import data.GhiSoOnline;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CHI SO MAY CHU + CHE DO BAO TRI (san sang production, 17/10/2026).
 *
 * 1. CHI SO (cho trang quan tri web + giam sat):
 *    - Bo dem toan server, tang o io.Session: goi tin vao, goi bi bo vi ngap, ket noi bi cat vi
 *      ngap, loi xu ly goi (truoc day bi nuot im lang trong Session.run).
 *    - Moi CHU_KY_MS ghi MOT dong vao bang tob_chi_so_may_chu (giu SO_NGAY_GIU ngay): online,
 *      heap, so thread, CPU tien trinh, load may, va SO DEM TANG THEM trong chu ky (khong phai
 *      tong cong don) - trang web chia cho 30 giay la ra toc do/giay.
 *    - Dong thoi ghi tep trang_thai.json (thu muc lam viec, ghi de nguyen tu) - vps/kiem-suc-khoe.sh
 *      doc tep nay: tep cu hon 90 giay = JVM treo du cong van mo.
 *
 * 2. BAO TRI (thay nut "Dung dang nhap" cua AdminPanel - tren VPS khong co man hinh):
 *    - Tao tep bao_tri.txt canh server.ini -> toi da 5 giay sau server TU CHOI dang nhap moi
 *      (nguoi dang choi KHONG bi da; muon da het thi dung systemctl stop - shutdown hook luu du lieu).
 *    - Dong dau cua tep = cau thong bao gui nguoi choi (trong = cau mac dinh).
 *    - Cac dong "cho: tentaikhoan" = tai khoan van dang nhap duoc (GM vao kiem tra truoc khi mo).
 *    - Xoa tep = mo lai dang nhap.
 *
 * Tat han ca hai: sv.chiSoMayChu=0 trong server.ini.
 */
public final class ChiSoMayChu implements Runnable {

    public static final String TEN_BANG = "tob_chi_so_may_chu";
    public static final String TEP_BAO_TRI = "bao_tri.txt";
    public static final String TEP_TRANG_THAI = "trang_thai.json";
    public static final long CHU_KY_MS = 30000L;
    static final long CHU_KY_BAO_TRI_MS = 5000L;
    public static final int SO_NGAY_GIU = 7;
    public static final String CAU_BAO_TRI_MAC_DINH =
            "Máy chủ đang bảo trì, vui lòng quay lại sau. Theo dõi trang chủ để biết giờ mở lại.";

    /** Goi tin client -> server da nhan (moi ket noi, ke ca truoc khi vao game). */
    public static final AtomicLong GOI_VAO = new AtomicLong();
    /** Goi tin server -> client da xep hang gui (io.Session.Sender) - bang dieu khien tinh goi ra/giay. LongAdder: khong tranh chap giua cac luong game. */
    public static final java.util.concurrent.atomic.LongAdder GOI_RA = new java.util.concurrent.atomic.LongAdder();
    /** Goi bi BO vi vuot nguong ngap (ket noi van giu). */
    public static final AtomicLong GOI_BO = new AtomicLong();
    /** Ket noi bi CAT vi ngap goi nang. */
    public static final AtomicLong CAT_NGAP = new AtomicLong();
    /** Ngoai le khi xu ly mot goi tin (processMessage nem ra). */
    public static final AtomicLong LOI_XU_LY = new AtomicLong();
    /** Tin chat bi ChongSpam chan (qua nhanh / trung lap / quang cao / dang cam). Cong don. */
    public static final AtomicLong CHAT_CHAN = new AtomicLong();
    /** Thao tac bi ChongSpam chan (moi doi, ket ban, moi giao dich, NPC, rao ban...). Cong don. */
    public static final AtomicLong THAO_TAC_CHAN = new AtomicLong();

    /** So ket noi ranh it nhat trong pool CSDL chinh tu lan ghi mau truoc (Integer.MAX_VALUE = chua do). */
    static final java.util.concurrent.atomic.AtomicInteger CSDL_RANH_MIN = new java.util.concurrent.atomic.AtomicInteger(Integer.MAX_VALUE);

    private static volatile boolean baoTri = false;
    private static volatile String cauBaoTri = CAU_BAO_TRI_MAC_DINH;
    private static volatile Set<String> choPhepKhiBaoTri = new CopyOnWriteArraySet<String>();
    private static volatile boolean daChay = false;
    private static boolean daBaoLoi = false;
    private static final long BAT_DAU = System.currentTimeMillis();

    private ChiSoMayChu() {
    }

    public static synchronized void batDau() {
        if (daChay) {
            return;
        }
        daChay = true;
        // Ket noi CSDL ranh it nhat trong 30 s (lay mau 100 ms) -> dinh dung that = toi da - ranh it nhat.
        // Dung de chinh db.maxco (docs/kien-truc.md).
        com.htbb.runtime.VongLapChung.henGio(new Runnable() {
            @Override
            public void run() {
                final data.ConnPool p = Database.connPool;
                if (p != null && p.pool != null) {
                    final int r = p.pool.size();
                    int cu;
                    while (r < (cu = CSDL_RANH_MIN.get()) && !CSDL_RANH_MIN.compareAndSet(cu, r)) {
                        // thu lai
                    }
                }
            }
        }, 100L);
        final Thread t = new Thread(new ChiSoMayChu());
        t.setName("CHI SO MAY CHU");
        t.setDaemon(true);
        t.start();
    }

    // Bao tri bat tu trang quan tri (khoa cau_hinh bao_tri.bat / bao_tri.cau / bao_tri.cho - CauHinhTrungTam, ap
    // dung <= 10 giay, KHONG can sv.chiSoMayChu hay quyen ghi thu muc game). Bat tep HOAC khoa = bao tri;
    // danh sach duoc vao = hop cua hai noi.
    private static volatile boolean baoTriCauHinh = false;
    private static volatile String cauCauHinh = "";
    private static volatile Set<String> choCauHinh = new CopyOnWriteArraySet<String>();

    /** Goi tu com.htbb.cauhinh.ApDungMacDinh khi khoa bao_tri.* doi. */
    public static void datBaoTriCauHinh(final boolean bat, final String cau, final List<String> cho) {
        final Set<String> s = new CopyOnWriteArraySet<String>();
        if (cho != null) {
            for (final String t : cho) {
                if (t != null && !t.trim().isEmpty()) {
                    s.add(t.trim().toLowerCase());
                }
            }
        }
        choCauHinh = s;
        cauCauHinh = cau == null ? "" : cau.trim();
        if (bat != baoTriCauHinh) {
            System.out.println("[BAO TRI] cau_hinh bao_tri.bat=" + (bat ? 1 : 0) + " -> " + (bat ? "CHAN dang nhap moi (tru " + s + ")" : "het bao tri theo cau hinh"));
        }
        baoTriCauHinh = bat;
    }

    /** Dang bao tri va tai khoan nay KHONG nam trong danh sach "cho:" -> tu choi dang nhap. */
    public static boolean chanDangNhap(final String taiKhoan) {
        if (!baoTri && !baoTriCauHinh) {
            return false;
        }
        if (taiKhoan == null) {
            return true;
        }
        final String t = taiKhoan.toLowerCase();
        return !(choPhepKhiBaoTri.contains(t) || choCauHinh.contains(t));
    }

    public static boolean dangBaoTri() {
        return baoTri || baoTriCauHinh;
    }

    public static String cauBaoTri() {
        if (baoTriCauHinh && !cauCauHinh.isEmpty()) {
            return cauCauHinh;
        }
        return baoTri ? cauBaoTri : CAU_BAO_TRI_MAC_DINH;
    }

    @Override
    public void run() {
        taoBang();
        long lanGhiTiep = System.currentTimeMillis() + 5000L;
        long lanDonTiep = System.currentTimeMillis() + 60000L;
        long vao0 = 0, bo0 = 0, cat0 = 0, loi0 = 0;
        while (TeamServer.running) {
            try {
                docTepBaoTri();
                final long bayGio = System.currentTimeMillis();
                if (bayGio >= lanGhiTiep) {
                    lanGhiTiep = bayGio + CHU_KY_MS;
                    final long vao = GOI_VAO.get(), bo = GOI_BO.get(), cat = CAT_NGAP.get(), loi = LOI_XU_LY.get();
                    ghiMau(vao - vao0, bo - bo0, cat - cat0, loi - loi0);
                    vao0 = vao;
                    bo0 = bo;
                    cat0 = cat;
                    loi0 = loi;
                }
                if (bayGio >= lanDonTiep) {
                    lanDonTiep = bayGio + 6L * 3600L * 1000L;
                    donDongCu();
                }
            } catch (final Throwable e) {
                baoLoiMotLan(e);
            }
            try {
                Thread.sleep(CHU_KY_BAO_TRI_MS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    static void docTepBaoTri() {
        final File f = new File(TEP_BAO_TRI);
        if (!f.isFile()) {
            if (baoTri) {
                baoTri = false;
                System.out.println("[BAO TRI] Da xoa " + TEP_BAO_TRI + " -> MO lai dang nhap.");
            }
            return;
        }
        try {
            final List<String> dong = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            String cau = dong.isEmpty() ? "" : dong.get(0).trim();
            if (cau.toLowerCase().startsWith("cho:")) {
                cau = "";
            }
            final Set<String> cho = new CopyOnWriteArraySet<String>();
            for (final String d : dong) {
                final String t = d.trim();
                if (t.toLowerCase().startsWith("cho:")) {
                    for (final String ten : t.substring(4).split("[,\\s]+")) {
                        if (!ten.isEmpty()) {
                            cho.add(ten.toLowerCase());
                        }
                    }
                }
            }
            cauBaoTri = cau.isEmpty() ? CAU_BAO_TRI_MAC_DINH : cau;
            choPhepKhiBaoTri = cho;
            if (!baoTri) {
                baoTri = true;
                System.out.println("[BAO TRI] Thay " + TEP_BAO_TRI + " -> CHAN dang nhap moi (tru " + cho + ").");
            }
        } catch (final Exception e) {
            baoTri = true; // doc loi nhung tep co ton tai: van coi la bao tri
        }
    }

    static void taoBang() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS `" + TEN_BANG + "` ("
                    + "`id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "`thoi_gian` DATETIME NOT NULL,"
                    + "`ket_noi` INT NOT NULL DEFAULT 0,"
                    + "`nhan_vat` INT NOT NULL DEFAULT 0,"
                    + "`heap_mb` INT NOT NULL DEFAULT 0,"
                    + "`heap_toi_da_mb` INT NOT NULL DEFAULT 0,"
                    + "`so_thread` INT NOT NULL DEFAULT 0,"
                    + "`cpu_phan_tram` INT NOT NULL DEFAULT 0,"
                    + "`tai_may` DOUBLE NOT NULL DEFAULT 0,"
                    + "`goi_vao` BIGINT NOT NULL DEFAULT 0,"
                    + "`goi_bo` BIGINT NOT NULL DEFAULT 0,"
                    + "`cat_ngap` INT NOT NULL DEFAULT 0,"
                    + "`loi_xu_ly` INT NOT NULL DEFAULT 0,"
                    + "`bao_tri` TINYINT NOT NULL DEFAULT 0,"
                    + "PRIMARY KEY (`id`),"
                    + "KEY `idx_thoi_gian` (`thoi_gian`)"
                    + ") DEFAULT CHARSET=utf8");
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            dong(st);
            tra(conn);
        }
    }

    static int csdlRanhVaXoa() {
        final int r = CSDL_RANH_MIN.getAndSet(Integer.MAX_VALUE);
        return r == Integer.MAX_VALUE ? -1 : r;
    }

    static int cpuTienTrinh() {
        try {
            final java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof com.sun.management.OperatingSystemMXBean) {
                final double c = ((com.sun.management.OperatingSystemMXBean) os).getProcessCpuLoad();
                return c < 0 ? -1 : (int) Math.round(c * 100.0);
            }
        } catch (final Throwable ignored) {
        }
        return -1;
    }

    static void ghiMau(final long vao, final long bo, final long cat, final long loi) {
        final int ketNoi = GhiSoOnline.demKetNoi();
        final int nhanVat = GhiSoOnline.demNhanVat();
        final MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final int heapMb = (int) (heap.getUsed() >> 20);
        final int heapMax = (int) (heap.getMax() >> 20);
        final int thread = ManagementFactory.getThreadMXBean().getThreadCount();
        final int cpu = cpuTienTrinh();
        final double tai = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        // Tep trang thai truoc (khong phu thuoc CSDL): CSDL chet thi kiem-suc-khoe van biet JVM con song.
        try {
            final String json = "{\"thoiGian\":" + System.currentTimeMillis()
                    + ",\"chayGiay\":" + ((System.currentTimeMillis() - BAT_DAU) / 1000L)
                    + ",\"ketNoi\":" + ketNoi + ",\"nhanVat\":" + nhanVat
                    + ",\"heapMb\":" + heapMb + ",\"heapToiDaMb\":" + heapMax
                    + ",\"thread\":" + thread + ",\"cpu\":" + cpu
                    + ",\"goiVao30s\":" + vao + ",\"goiBo30s\":" + bo + ",\"catNgap30s\":" + cat
                    + ",\"loiXuLy30s\":" + loi + ",\"baoTri\":" + dangBaoTri()
                    // Nhat ky + chong spam: so CONG DON tu luc chay (com.htbb.log.Nhat, ChongSpam).
                    + ",\"nhatKy\":{\"ghi\":" + com.htbb.log.Nhat.DONG_GHI.get() + ",\"bo\":" + com.htbb.log.Nhat.DONG_BO.get()
                    + ",\"loi\":" + com.htbb.log.Nhat.DONG_LOI.get() + ",\"mat\":" + com.htbb.log.Nhat.DONG_MAT.get() + "}"
                    + ",\"chatChan\":" + CHAT_CHAN.get() + ",\"thaoTacChan\":" + THAO_TAC_CHAN.get()
                    // Vong lap chung (VongLapChung) + pool CSDL + ket noi bi cat vi tran hang doi gui.
                    + ",\"vongLap\":{\"nhip\":" + com.htbb.runtime.VongLapChung.SO_NHIP.get()
                    + ",\"ranh\":" + com.htbb.runtime.VongLapChung.SO_NHIP_RANH.get()
                    + ",\"lanGoiTin\":" + com.htbb.runtime.VongLapChung.SO_LAN.get() + "}"
                    + ",\"csdl\":{\"ranhItNhat30s\":" + csdlRanhVaXoa()
                    + ",\"ranhHienTai\":" + (Database.connPool == null ? -1 : Database.connPool.pool.size()) + "}"
                    + ",\"catTranGui\":" + io.Session.CAT_TRAN_GUI.get()
                    // Tu dong danh chinh thuc (real.TuDongGame): dang chay + cong don tu luc chay.
                    + ",\"tuDong\":{\"dangChay\":" + real.TuDongGame.soDangChay() + ",\"bat\":" + real.TuDongGame.SO_LAN_BAT.get()
                    + ",\"tuChoi\":" + real.TuDongGame.SO_LAN_TU_CHOI.get() + ",\"chan\":" + real.TuDongGame.SO_HANH_DONG_CHAN.get()
                    + ",\"tatViPham\":" + real.TuDongGame.SO_TAT_VI_PHAM.get() + ",\"nghiVanNhip\":" + real.TuDongGame.SO_NGHI_VAN_NHIP.get() + "}"
                    // Guc / hoi sinh (real.HoiSinhGame): so lan guc, nguon hoi sinh, xu / luong khoa dot, EXP mat (cong don).
                    + ",\"hoiSinh\":" + real.HoiSinhGame.json()
                    + "}\n";
            final java.nio.file.Path tam = Paths.get(TEP_TRANG_THAI + ".tmp");
            Files.write(tam, json.getBytes(StandardCharsets.UTF_8));
            Files.move(tam, Paths.get(TEP_TRANG_THAI), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final Exception e) {
            baoLoiMotLan(e);
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("INSERT INTO `" + TEN_BANG + "` (thoi_gian, ket_noi, nhan_vat, heap_mb,"
                    + " heap_toi_da_mb, so_thread, cpu_phan_tram, tai_may, goi_vao, goi_bo, cat_ngap, loi_xu_ly, bao_tri)"
                    + " VALUES (NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, ketNoi);
            ps.setInt(2, nhanVat);
            ps.setInt(3, heapMb);
            ps.setInt(4, heapMax);
            ps.setInt(5, thread);
            ps.setInt(6, cpu);
            ps.setDouble(7, tai < 0 ? 0 : tai);
            ps.setLong(8, vao);
            ps.setLong(9, bo);
            ps.setInt(10, (int) Math.min(Integer.MAX_VALUE, cat));
            ps.setInt(11, (int) Math.min(Integer.MAX_VALUE, loi));
            ps.setInt(12, dangBaoTri() ? 1 : 0);
            ps.execute();
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            dong(ps);
            tra(conn);
        }
    }

    static void donDongCu() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("DELETE FROM `" + TEN_BANG + "` WHERE thoi_gian < DATE_SUB(NOW(), INTERVAL ? DAY)");
            ps.setInt(1, SO_NGAY_GIU);
            ps.executeUpdate();
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            dong(ps);
            tra(conn);
        }
    }

    private static synchronized void baoLoiMotLan(final Throwable e) {
        if (daBaoLoi) {
            return;
        }
        daBaoLoi = true;
        System.out.println("CHI SO MAY CHU: loi (chi bao mot lan) - " + e);
    }

    private static void dong(final Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private static void tra(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }
}
