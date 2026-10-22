package server;

import data.*;
import io.LogInController;
import io.Session;
import io.SessionManager;
import logNQSH.DBUserLogger;
import real.Map;
import real.*;
import real.cmd.SelectCharHandler;

import java.io.*;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;

public class TeamServer {

    public static boolean running;
    public static int PORT;
    private static ServerControllerManager controllerManager;
    private static ServerSocket listenSocket;
    public static long timeStartServer;
    public static long timeRemoveSecssion;
    public static int maxCCU;
    public static int minCCU;
    public static int maxUser;
    public static int minCCUAD;
    public static int maxCCUAD;
    public static short LIMIT_CCU;
    static RealController realController;
    public static long timUpBoard;
    public static long timeOutBoard;
    public static long timeRemoveChar;
    public static long timeRemovePool;
    public static long timeBoardRun;
    public static long timeDownBoard;
    public static long timeDouble;
    public static boolean isDouble;
    public static int update;
    public static int maxBackup;
    public static String lastDay;
    public static LinkedBlockingQueue<String> userLogQueue;
    public static GameCanvas gameCanvas;
    public static int nConnect;
    public static int server;
    public static int portNapTien;
    public static boolean haveMsgAdmin;
    public static boolean changRange;
    public static byte MAX_SNAKE;
    private static int timeStart;
    public static boolean isStart;
    private static int minute;
    public static byte language;
    public static Vector<InfoClientConnect> V_IP_BLOCK;
    public static Hashtable<String, InfoClientConnect> ALL_IPCONNECT_BLOCK;
    public static Hashtable<String, InfoClientConnect> ALL_IPCONNECT;
    static Hashtable<String, CharInfo> charinfo;
    public static String stHelp;

    public static String ipLogin = null;
    public static int portLogin = 8023;

    // Trang chu / link tai game cua server nay, doc tu server.ini (sv.website, sv.linkTaiGame).
    // Truoc day cho cung cac domain teamobi -> nay do chu server tu dat.
    public static String website = "";
    public static String linkTaiGame = "";

    /**
     * CHAN DOAN linh thu: ten nhan vat duoc gui kem id anh thu cuoi qua duong DANH
     * HIEU, ngoai duong thu cuoi binh thuong. Rong = tat (mac dinh).
     *
     * Dat trong server.ini: sv.thuNghiemThuCuoi=tenNhanVat
     *
     * De lam gi: log da chung minh duong danh hieu VE DUOC (client xin anh 8788 qua
     * duong do), con duong thu cuoi thi khong xin gi. Neu gui cung mot id qua duong
     * danh hieu ma con thu HIEN ra (dat sai cho, khong theo huong - khong sao), thi
     * du lieu khung ben client la du va loi nam dung o nhanh paintMount. Con neu van
     * khong hien thi ALL_DATA_EFF ben client thieu id do, du goi 26 co gui.
     * Chi anh huong dung nhan vat duoc dat ten, va chi la hinh ve.
     */
    public static String thuNghiemThuCuoi = "";

    // Ti le drop doc tu server.ini luc khoi dong, giu lai de bang setting cua admin
    // co the "tra ve muc trong server.ini".
    // [0]=xu [1]=luong [2]=itemSuKien [3]=potion [4]=ngoc [5]=item [6]=chiaXuRoi
    public static int[] tileGoc = {10, 20, 20, 30, 30, 20, 10};

    public static boolean ExpVantieu;
    public static boolean ExpNhanSam;
    public static boolean LuckyExp;
    public static boolean ExpQua;
    public static boolean CodeTest;

    // ==== TEST FLAGS (dùng cho phát triển/kiểm thử, mặc định tắt) ====
    public static boolean testMode = false;                 // sv.testMode: bật chế độ test
    // SAN SANG PRODUCTION (17/10/2026):
    // sv.gmCanCoPInfo=1: quyen GM can CA ten nhan vat trong sv.adminAccounts LAN pInfo[16]=1 (0 = chi can ten, nhu cu).
    public static boolean gmCanCoPInfo = false;
    // sv.banToiThieu=N (>0): tu choi client jar/Android gui so ban (truong int cuoi goi dang nhap) < N.
    // Client khong gui so ban (PC Unity) luon duoc qua. 0 = tat (mac dinh).
    public static int banToiThieu = 0;
    // 0.9.7 cap nhat client (com.htbb.capnhat.CapNhatClient): ban moi nhat = so ban tung nen tang trong danh muc tai game
    // (web /admin/tai-game); sv.banMoiNhat = du phong khi danh muc chua ghi so ban; sv.nhacCapNhat=0 tat nhac ban moi.
    public static int banMoiNhat = 0;
    public static boolean nhacCapNhat = true;
    // sv.chiSoMayChu=0: tat bo ghi chi so + che do bao tri bang tep (mac dinh BAT).
    public static boolean batChiSoMayChu = true;
    public static String testCode = "";                     // sv.testCode: mã giftcode test (chỉ chạy khi testMode)
    // Sua 19/09/2026: HashSet thuong khong an toan luong - CauHinh.napLai() clear()+
    // add() lai tap nay moi 10 giay tren 1 thread nen rieng (xem CauHinh.batDau()), trong
    // khi laTenDanhRieng()/nhieu noi khac doc contains() tu bat ky thread request nao,
    // KHONG qua khoa nao ca. Doc/ghi HashSet dong thoi khong dong bo la hanh vi khong xac
    // dinh - da bat qua tay: nguoi choi doi ten sang "cungtien" (khong chua "admin", khong
    // nam trong sv.adminAccounts) bi chan nham "Ten nay khong the su dung" dung luc trung
    // vao cua so 10 giay do. CopyOnWriteArraySet doc luon an toan/khong khoa, ghi (hiem,
    // 10s/lan) copy toan bo mang - dung API y het HashSet (clear/add/contains/isEmpty/
    // for-each) nen khong can sua noi nao khac dang dung TeamServer.adminAccounts.
    public static java.util.concurrent.CopyOnWriteArraySet<String> adminAccounts = new java.util.concurrent.CopyOnWriteArraySet<>(); // sv.adminAccounts: danh sách tài khoản admin test
    /** sv.reloginWaitSec: so giay phai cho truoc khi vao lai game sau khi thoat. 0 = vao lai ngay. */
    public static long RELOGIN_WAIT_MS = 0L;

    // ==== CHỐNG DoS (chống flood/slowloris ở tầng ứng dụng; ngưỡng rộng, chỉnh trong server.ini) ====
    public static int SOCKET_TIMEOUT_MS   = 180000;  // sv.socketTimeout (giây): ngắt kết nối im lặng quá lâu. 0=tắt
    public static int MAX_CONN_PER_IP     = 50;      // sv.maxConnPerIp : số kết nối đồng thời tối đa/IP. 0=tắt
    public static int CONN_RATE_MAX       = 60;      // sv.connRateMax  : số kết nối mới tối đa/IP trong 1 cửa sổ
    public static int CONN_RATE_WINDOW_MS = 30000;   // sv.connRateWindow (giây): độ dài cửa sổ đếm
    public static long DOS_BAN_MS         = 300000L; // sv.dosBanSec (giây): thời gian ban tạm IP flood
    public static final ConcurrentHashMap<String, AtomicInteger> IP_OPEN = new ConcurrentHashMap<>(); // kết nối đang mở/IP
    public static final ConcurrentHashMap<String, long[]> IP_RATE = new ConcurrentHashMap<>();         // {mốc cửa sổ, số đếm}
    public static final ConcurrentHashMap<String, Long> IP_BAN = new ConcurrentHashMap<>();            // IP -> thời điểm hết ban

    /** IP nội bộ/LAN được miễn trừ chống DoS. */
    public static boolean isLocalIp(String ip) {
        return ip == null || ip.isEmpty()
                || ip.equals("127.0.0.1") || ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.16.");
    }

    /**
     * Miễn mọi giới hạn theo IP (maxConnPerIp, connRate, ban dosBanSec, chặn IP): IP nội bộ/LAN
     * hoặc proxy tin cậy sv.proxyTinCay (WAF / Nova agent - sau WAF cả server chung vài IP).
     */
    public static boolean mienGioiHanIp(String ip) {
        return isLocalIp(ip) || com.htbb.mang.ProxyTinCay.tinCay(ip);
    }

    /** Gọi trong vòng accept: trả về true nếu cho phép kết nối, false nếu phải chặn. */
    public static boolean dosAccept(String ip) {
        try {
            if (mienGioiHanIp(ip)) {
                return true;
            }
            long now = System.currentTimeMillis();
            Long banUntil = IP_BAN.get(ip);
            if (banUntil != null) {
                if (now < banUntil) {
                    return false;
                }
                IP_BAN.remove(ip);
            }
            // Giới hạn tốc độ kết nối trong cửa sổ thời gian
            long[] r = IP_RATE.get(ip);
            if (r == null || now - r[0] > CONN_RATE_WINDOW_MS) {
                IP_RATE.put(ip, new long[]{now, 1L});
            } else {
                r[1]++;
                if (CONN_RATE_MAX > 0 && r[1] > CONN_RATE_MAX) {
                    IP_BAN.put(ip, now + DOS_BAN_MS);
                    System.out.println("[DoS] BAN " + ip + " - ket noi qua nhanh (" + r[1] + "/" + (CONN_RATE_WINDOW_MS / 1000) + "s)");
                    return false;
                }
            }
            // Giới hạn số kết nối đồng thời
            if (MAX_CONN_PER_IP > 0) {
                AtomicInteger open = IP_OPEN.get(ip);
                if (open != null && open.get() >= MAX_CONN_PER_IP) {
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            return true; // lỗi thì cho qua, không tự khóa server
        }
    }

    public static void dosOnOpen(String ip) {
        if (mienGioiHanIp(ip)) {
            return;
        }
        IP_OPEN.computeIfAbsent(ip, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public static void dosOnClose(String ip) {
        // Khong hoi mienGioiHanIp: sv.proxyTinCay doi nong giua luc mo va dong thi van phai tru
        // bo dem da cong (khong co muc = khong lam gi).
        if (ip == null) {
            return;
        }
        AtomicInteger open = IP_OPEN.get(ip);
        if (open != null && open.decrementAndGet() <= 0) {
            IP_OPEN.remove(ip);
        }
    }

    /** Doc phan tram ti le drop tu server.ini, kep trong khoang 0..100. */
    private static int tilePercent(String v, int def) {
        int pc = parseIntDefault(v, def);
        if (pc < 0) {
            pc = 0;
        }
        if (pc > 100) {
            pc = 100;
        }
        return pc;
    }

    private static int parseIntDefault(String v, int def) {
        try {
            return (v == null || v.trim().isEmpty()) ? def : Integer.parseInt(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean isSuKien83;
    public static boolean isSuKienTrungThul2016;
    public static boolean isSuKienTet2017;
    public static boolean isSuKienGioTo2016;
    public static boolean isSuKienNoel2023;
    public static boolean isSuKienTetduonglich2024;

    /**
     * Gia xu doi "50 hoa hong vang -> 1 bo hoa do" o su kien Vu Lan (NPC Dau Truong).
     * Doc tu server.ini khoa sv.giaXuBoHoaDoVuLan - moi server tu dat rieng (Van Xuan
     * dat thap hon cac server khac), khong hardcode ten server trong Java vi khong co
     * san co che so ten server (chi co TeamServer.server la so id).
     */
    public static int giaXuBoHoaDoVuLan = 150000;


    static {
        TeamServer.running = true;
        TeamServer.listenSocket = null;
        TeamServer.maxCCU = 0;
        TeamServer.minCCU = 0;
        TeamServer.maxUser = 0;
        TeamServer.minCCUAD = 0;
        TeamServer.LIMIT_CCU = 1000;
        TeamServer.timeBoardRun = 3000L;
        TeamServer.timeDownBoard = 1500L;
        TeamServer.timeDouble = System.currentTimeMillis();
        TeamServer.isDouble = false;
        TeamServer.update = 0;
        TeamServer.maxBackup = 0;
        TeamServer.lastDay = "";
        TeamServer.userLogQueue = new LinkedBlockingQueue<>();
        long exp = LevelDetail.getXpFromLevel(40);
        LevelDetail.getLevelFromExp(exp + 750000000L);
        LevelPet.initExpTemplate();
        TeamServer.gameCanvas = new GameCanvas();
        TeamServer.nConnect = 0;
        TeamServer.server = 1;
        TeamServer.portNapTien = 19153;
        TeamServer.haveMsgAdmin = false;
        TeamServer.changRange = false;
        TeamServer.MAX_SNAKE = 2;
        TeamServer.isStart = false;
        TeamServer.language = 0;
        TeamServer.V_IP_BLOCK = new Vector<>();
        TeamServer.ALL_IPCONNECT_BLOCK = new Hashtable<>();
        TeamServer.ALL_IPCONNECT = new Hashtable<>();
        TeamServer.charinfo = new Hashtable<>();
        TeamServer.stHelp = "";

        TeamServer.ExpVantieu = false;
        TeamServer.LuckyExp = false;
        TeamServer.ExpQua = false;
        TeamServer.CodeTest = false;

    }

    public static String getCurrentDayTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(new Date(System.currentTimeMillis()));
    }

    public ServerController getController(int serviceID) {
        return TeamServer.controllerManager.getController(serviceID);
    }

    /** De quy qua Hashtable/Vector/ItemMarket: Vector rong -> trimToSize(). Xem cho goi Market.init(). */
    static int thuGonVectorRong(final Object o) {
        int n = 0;
        try {
            if (o instanceof java.util.Vector) {
                final java.util.Vector<?> v = (java.util.Vector<?>) o;
                if (v.isEmpty()) {
                    v.trimToSize();
                    return 1;
                }
                for (int i = 0; i < v.size(); i++) {
                    final Object e = v.get(i);
                    if (e instanceof java.util.Vector || e instanceof java.util.Hashtable || e instanceof real.ItemMarket) {
                        n += thuGonVectorRong(e);
                    }
                }
            } else if (o instanceof java.util.Hashtable) {
                for (final Object e : ((java.util.Hashtable<?, ?>) o).values()) {
                    n += thuGonVectorRong(e);
                }
            } else if (o instanceof real.ItemMarket) {
                n += thuGonVectorRong(((real.ItemMarket) o).ALL_ITEM_COLOR_THANH_LONG);
                n += thuGonVectorRong(((real.ItemMarket) o).ALL_ITEM_COLOR_HAC_HO);
            }
        } catch (final Exception ignored) {
        }
        return n;
    }

    public static void main(String[] args) {

        // Nhat ky: thay System.out/err bang bo chan dong (UTF-8, phan loai, gop dong lap, ghi
        // bat dong bo). Thay cho hai dong setOut/setErr UTF-8 cu. Xem com.htbb.log.Nhat.
        com.htbb.log.Nhat.caiDat();

        run();
    }

    public void shutdown() {
        TeamServer.running = false;
        try {
            if (TeamServer.listenSocket != null) {
                TeamServer.listenSocket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public static String myTake() throws InterruptedException {
        return TeamServer.userLogQueue.take();
    }

    public static void addQueue(String info) {
        try {
            if (TeamServer.userLogQueue.size() < 10000) {
                TeamServer.userLogQueue.add(info);
            }
        } catch (Exception ignored) {
        }
    }

    /** Chuoi mat khau mau trong vps/server.ini.linux.example - khong bao gio la mat khau that. */
    static final String MAT_KHAU_MAU = "DOI_MAT_KHAU_DB";

    /**
     * Thu mo mot ket noi THAT toi CSDL. Tra ve ly do that bai, hoac null neu vao duoc.
     *
     * Tach rieng khoi chanDoanCSDL() de nhanh nhuong bo cho may dev (xem kiemTraCSDL) van
     * BAT BUOC phai ket noi duoc that: no chi mien phep kiem "mat khau de trong", chu khong
     * mien phep thu ket noi. Nho vay mot may dat db.choPhepMatKhauTrong=1 ma CSDL sai/chua
     * bat van chet ngay voi ly do ro rang, khong chay lung chung.
     *
     * KHONG bao gio dua mat khau vao chuoi tra ve: chuoi nay di thang ra log, ma cac tep log
     * de quyen -rw-r--r-- (ai cung doc duoc). Cung vi the KHONG dung dang URL
     * "?user=..&password=.." - driver in lai ca URL trong thong bao loi.
     */
    public static String thuKetNoiCSDL(final Properties p) {
        final String host = p.getProperty("db.host");
        final String ten = p.getProperty("db.name");
        final String user = p.getProperty("db.user");
        final String matKhau = p.getProperty("db.password");
        java.sql.Connection c = null;
        try {
            c = java.sql.DriverManager.getConnection(
                    Database.urlJdbc(host, ten, p.getProperty("db.thamSoJdbc")), user, matKhau);
            final java.sql.Statement st = c.createStatement();
            st.executeQuery("select 1").close();
            st.close();
            return null;
        } catch (final Throwable t) {
            return "khong ket noi duoc: " + t.getMessage();
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (final Exception ignored) {
            }
        }
    }

    /**
     * Tra ve ly do khong vao duoc CSDL, hoac null neu vao duoc.
     *
     * Tach rieng khoi kiemTraCSDL() de test goi duoc: ham kia ket thuc bang System.exit(1),
     * ma mot ham goi exit thi khong the kiem thu duoc - JVM chay test se chet theo.
     * KHONG bao gio dua mat khau vao chuoi tra ve: chuoi nay di thang ra log.
     *
     * Ham nay LUON NGHIEM NGAT, khong co ngoai le nao: mat khau de trong van la mot ly do.
     * Nhanh nhuong bo cho may dev nam o kiemTraCSDL() - cho duy nhat quyet dinh mot chan
     * doan co lam chet server hay khong. Xem test/KiemMayDevCSDL.java.
     */
    public static String chanDoanCSDL(final Properties p) {
        final String matKhau = p.getProperty("db.password");
        if (matKhau == null || matKhau.isEmpty()) {
            return "db.password dang de trong";
        }
        if (MAT_KHAU_MAU.equals(matKhau)) {
            return "db.password van la chuoi mau \"" + MAT_KHAU_MAU + "\" - file server.ini nay"
                    + " la ban sao cua vps/server.ini.linux.example, chua dien mat khau that";
        }
        return thuKetNoiCSDL(p);
    }

    /**
     * Host CSDL co phai may noi bo (loopback) hay khong.
     *
     * Dang gia tri trong server.ini la "host:cong": localhost:3306, 127.0.0.1:3306,
     * [::1]:3306. Mot ten may khac ma tinh ra cung tro ve 127.0.0.1 thi o day van tinh la
     * O XA - lech ve phia nghiem ngat, dung huong an toan.
     */
    public static boolean laHostNoiBo(final String hostCong) {
        if (hostCong == null) {
            return false;
        }
        String h = hostCong.trim().toLowerCase();
        if (h.startsWith("[")) {
            // IPv6 co ngoac: [::1]:3306
            final int dongNgoac = h.indexOf(']');
            if (dongNgoac > 0) {
                h = h.substring(1, dongNgoac);
            }
        } else if (h.indexOf(':') > 0 && h.indexOf(':') == h.lastIndexOf(':')) {
            // Dung MOT dau hai cham -> la "host:cong". Nhieu hon la IPv6 tran, de nguyen.
            h = h.substring(0, h.indexOf(':'));
        }
        return "localhost".equals(h) || "127.0.0.1".equals(h) || "::1".equals(h)
                || "0:0:0:0:0:0:0:1".equals(h);
    }

    /**
     * Co cho phep db.password de trong hay khong. Ham THUAN (khong doc file, khong cham
     * CSDL) de test chay duoc moi to hop.
     *
     * MAC DINH LA KHONG. Thieu khoa, khoa de trong, "0", "true", hay viet sai chinh ta -
     * tat ca deu ra false. Chi dung chuoi "1" moi mo, VA con phai them dieu kien CSDL nam
     * ngay tren may noi bo. Nho vay mot server.ini bi thieu dong, bi cat ngan, hay bi ghi
     * de bang ban mau deu roi vao huong nghiem ngat.
     */
    public static boolean quyetDinhChoPhepMatKhauTrong(final String khoa, final String host) {
        if (khoa == null || !"1".equals(khoa.trim())) {
            return false;
        }
        return laHostNoiBo(host);
    }

    /**
     * Doc quyet dinh tren tu server.ini.
     *
     * PHAI doc khoa bang loi goi getProperty voi chuoi viet thang tren MOT dong (nhu ben
     * duoi): test KiemCauHinhPipeline quet ma nguon bang bieu thuc chinh quy de doi chieu
     * hai chieu voi vps/server.ini.tmpl. Doi thanh hang so hay tach bien la khoa nay "vo
     * hinh" voi phep doi chieu do.
     *
     * Va vi phep quet KHONG bo qua dong chu thich, chu thich o day tuyet doi khong duoc
     * viet lai nguyen dang loi goi ay - lam vay la phep doi chieu tu thoa man bang chinh
     * chu thich, va xoa mat loi goi that di no cung khong biet.
     */
    public static boolean choPhepMatKhauTrong(final Properties p) {
        return quyetDinhChoPhepMatKhauTrong(p.getProperty("db.choPhepMatKhauTrong"),
                p.getProperty("db.host"));
    }

    /**
     * Dung han server ngay khi khong vao duoc CSDL, thay vi de no chay tiep roi chet lung chung.
     *
     * VI SAO PHAI CO. Mat khau sai thi ConnPool rong, moi getConnection() nem
     * NoSuchElementException, nhung server VAN CHAY TIEP - cho den luc Map.loadAllTemplate
     * doc phai Vector rong roi nem ArrayIndexOutOfBoundsException trong luong "main". Luc do
     * main chet TRUOC KHI mo socket, con cac luong non-daemon khac van song nen JVM khong
     * thoat. Ket qua: systemd bao "active (running)" trong khi khong cong nao lang nghe, va
     * nguoi van hanh nhin systemctl status thi thay moi thu binh thuong.
     *
     * Da xay ra that ngay 30/08/2026: server.ini bi chep de bang chinh file mau, mat mot luc
     * lau moi lan ra vi trieu chung danh lac huong hoan toan.
     *
     * Chet ngay tai day thi systemd bao "failed" dung su that, va log co MOT dong noi ro ly
     * do thay vi hang tram stack trace NoSuchElementException khong dau la dau. Xem them
     * StartLimitBurst trong vps/htbb-game.service de systemd thoi khoi dong lai vo han.
     *
     * KHONG in mat khau ra log - chi noi no co dung chuoi mau hay khong.
     *
     * NHUONG BO CHO MAY DEV. May nha chay XAMPP thuong de root khong mat khau, ma dat mat
     * khau MySQL chi de chay thu thi rat vuong. Nen khi server.ini co
     * db.choPhepMatKhauTrong=1 VA db.host tro vao may noi bo, thi rieng phep kiem "mat khau
     * de trong" duoc mien - moi phep kiem khac giu nguyen, va van phai ket noi duoc that.
     * Xem choPhepMatKhauTrong(). VPS duoc giu o ba lop:
     *   1. vps/server.ini.tmpl ghim db.choPhepMatKhauTrong=0, va deploy-auto.sh SINH LAI
     *      /opt/htbb/server.ini tu ban mau do o moi lan deploy;
     *   2. vps/sinh-server-ini.sh tu choi cai neu gia tri khac 0, va tu choi ca file co
     *      khoa khai hai lan (Properties lay dong cuoi, bo sinh truoc day lay dong dau);
     *   3. thieu khoa (hoac sai chinh ta) mac dinh la KHONG cho - fail-closed.
     *
     * DUNG COI DAY LA BAT KHA XAM PHAM. Hai lop dau la lop VAN HANH, khong phai lop ky
     * thuat: ai co quyen root tren VPS van sua tay /opt/htbb/server.ini duoc, va rieng
     * duong vps/update.sh thi khoi dong lai ma KHONG sinh lai server.ini - sua tay se song
     * tiep cho tan lan deploy sau. Cung dung tuong "mat khau trong thi MySQL tu chan": neu
     * grant cho phep thi MariaDB van cho vao qua TCP binh thuong. Lop chan that su cuoi
     * cung o day chi la thuKetNoiCSDL() - phai ket noi duoc that moi chay.
     */
    public static void kiemTraCSDL(final Properties p) {
        final String matKhau = p.getProperty("db.password");
        final boolean choTrong = choPhepMatKhauTrong(p);
        final String vuongMac;
        if (choTrong && "".equals(matKhau)) {
            // "".equals(...) co y: khoa THIEU (null) hay chuoi mau van di duong nghiem ngat.
            System.out.println("CSDL MAY DEV -> chap nhan db.password de trong, van thu ket noi that");
            vuongMac = thuKetNoiCSDL(p);
        } else {
            vuongMac = chanDoanCSDL(p);
        }
        if (vuongMac == null) {
            return;
        }
        final String host = p.getProperty("db.host");
        final String ten = p.getProperty("db.name");
        final String user = p.getProperty("db.user");
        System.out.println();
        System.out.println("!!!!!!!!!! KHONG VAO DUOC CSDL - DUNG SERVER !!!!!!!!!!");
        System.out.println("  ly do    : " + vuongMac);
        System.out.println("  dang thu : " + host + "/" + ten + " voi user " + user);
        if (choTrong) {
            System.out.println("  cach sua : bat MySQL (XAMPP) roi chay lai run.bat");
        } else {
            System.out.println("  cach sua : sua /opt/htbb/server.ini roi systemctl restart htbb-game");
            if ("".equals(matKhau)) {
                System.out.println("  o may nha: them db.choPhepMatKhauTrong=1 vao server.ini cua may minh");
                System.out.println("             (ban mau VPS ghim 0 - DUNG sua ban mau)");
            }
        }
        System.out.println("  (dung han o day de systemd bao 'failed' dung su that; neu chay tiep");
        System.out.println("   thi server chet lung chung ma systemd van bao 'active (running)')");
        System.out.println();
        System.out.flush();
        System.exit(1);
    }

    public static void reloadMaxLG() {
        try {
            Properties p = new Properties();
            p.load(Files.newInputStream(Paths.get("server.ini")));
            TeamServer.MAX_SNAKE = Byte.parseByte(p.getProperty("sv.LG"));
           
        } catch (Exception e) {
            System.out.println("Load configuration file error!");
            e.printStackTrace();
        }
    }

    /** Doc sv.iplogin / sv.portlogin tu server.ini truoc khi LogInController ket noi. */
    static void docDiaChiLoginSom() {
        try (java.io.InputStream in = Files.newInputStream(Paths.get("server.ini"))) {
            final Properties p = new Properties();
            p.load(in);
            final String ip = p.getProperty("sv.iplogin");
            if (ip != null && !ip.trim().isEmpty()) {
                TeamServer.ipLogin = ip.trim();
            }
            final String cong = p.getProperty("sv.portlogin");
            if (cong != null && !cong.trim().isEmpty()) {
                TeamServer.portLogin = Integer.parseInt(cong.trim());
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean isServerIndo() {
        return TeamServer.language == 1;
    }

    public static boolean isServerLienDau() {
        return TeamServer.server == 50;
    }

    public static boolean isServerLocal() {
        return TeamServer.server == 0;
    }

    public static boolean isServerTest() {
        return TeamServer.server == 0;
    }

    public static boolean isServerHoaLu() {
        return TeamServer.server == 7 && Map.openLog;
    }

    public static boolean isServerKinhMon() {
        return TeamServer.server == 5 && Map.openLog;
    }

    public static boolean isServerHoangMy() {
        return TeamServer.server == 3 && Map.openLog;
    }

    public static boolean isServerTrangTien() {
        return TeamServer.server == 2 && Map.openLog;
    }

    public static boolean isServerDaiLa() {
        return TeamServer.server == 1 && !Map.openLog;
    }

    public static boolean isServerDaiViet() {
        return TeamServer.server == 3 && !Map.openLog;
    }

    public static boolean isServerVanLang() {
        return TeamServer.server == 5 && !Map.openLog;
    }

    public static void run() {

        // SAN SANG PRODUCTION (17/10/2026): LogInController mo ket noi NGAY, truoc khi khoi doc
        // server.ini ben duoi -> truoc day luon noi toi 127.0.0.1:8023 mac dinh, bo qua
        // sv.iplogin / sv.portlogin (phat hien khi chay may chu thu thu hai cong khac). Doc som
        // hai khoa nay; loi thi giu mac dinh cu, khong lam hong khoi dong.
        docDiaChiLoginSom();
        LogInController.Instance.run();
        
        // Start AdminPanel — chi khi co man hinh.
        // Tren VPS Linux khong co X server (hoac chay bang systemd), new JFrame()
        // nem HeadlessException: server van chay nhung log day stack trace. Kiem tra
        // truoc cho sach. Tren Windows khong doi gi, panel van hien nhu cu.
        if (!java.awt.GraphicsEnvironment.isHeadless() && AdminPanel.choPhepMo()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    AdminPanel panel = new AdminPanel();
                    panel.setVisible(true);
                } catch (Throwable t) {
                    System.out.println("[ADMIN PANEL] Khong mo duoc cua so: " + t);
                }
            });
        } else {
            System.out.println("[ADMIN PANEL] Che do khong man hinh (headless) - bo qua cua so admin."
                    + " Dung lenh chat trong game, hoac 'systemctl stop htbb-game' de tat co trat tu.");
        }

        // Luu du lieu khi JVM thoat (dong cua so console, Ctrl+C, taskkill /IM java.exe).
        // KHONG goi AdminHandler.stopServer() vi ham do co System.exit() -> treo shutdown hook.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (AdminHandler.isStopServer) {
                    return; // admin da tat co trat tu, du lieu luu roi
                }
                AdminHandler.isStopServer = true;
                System.out.println("[SHUTDOWN] Dang luu du lieu truoc khi thoat...");
                try { NewClan.updateAllClan(); } catch (Exception e) { e.printStackTrace(); }
                try { RealController.intance.saveAllChar(true); } catch (Exception e) { e.printStackTrace(); }
                try { ItemMarket.saveAllCharSell(); } catch (Exception e) { e.printStackTrace(); }
                try { MonsterVantieu.saveAllMonster(); } catch (Exception e) { e.printStackTrace(); }
                try { Database.instance.saveCooking(Char.nauBanhChung, 1); } catch (Exception ignored) {}
                try { Database.instance.saveCooking(Char.nauBanhTet, 2); } catch (Exception ignored) {}
                try { Database.instance.saveEvent(Map.event.getInfo()); } catch (Exception ignored) {}
                try { Database.instance.xaTatCaLogMuaShop(); } catch (Exception ignored) {}
                try { Database.instance.saveAllLog(Logdata.query); } catch (Exception ignored) {}
                try {
                    for (int i = 0; i < 3; ++i) {
                        Database.instance.saveTopienTram(Map.topPK.get(i));
                    }
                } catch (Exception ignored) {}
                System.out.println("[SHUTDOWN] Da luu xong.");
            }
        });
        
        final Properties p = new Properties();
        try {
            p.load(Files.newInputStream(Paths.get("server.ini")));
            // PHAI dat NGAY SAU khi doc server.ini va TRUOC Database.setLink(...):
            // data.ConnPool in mat khau CSDL ra stdout ngay trong ham dung. Xem LocBiMat.
            LocBiMat.batDau(p);
            // Nhat ky: muc / loai tat / gop / tep xoay vong (sv.log.*). Som nhat co the de moi dong
            // khoi dong da theo cau hinh that.
            com.htbb.log.Nhat.docCauHinh(p, p.getProperty("sv.pathLog"));
            BaoTriDaily.Server = Integer.parseInt(p.getProperty("api.sv"));
            TeamServer.PORT = Integer.parseInt(p.getProperty("sv.port"));
            TeamServer.LIMIT_CCU = Short.parseShort(p.getProperty("limit.ccu"));
            TeamServer.server = Integer.parseInt(p.getProperty("sv.server"));
            TeamServer.portNapTien = Integer.parseInt(p.getProperty("sv.portnap"));
            TeamServer.MAX_SNAKE = Byte.parseByte(p.getProperty("sv.LG"));
            TeamServer.changRange = (Byte.parseByte(p.getProperty("sv.range")) == 1);
            UserLogger.PATH = p.getProperty("sv.pathLog");
            TeamServer.ipLogin = p.getProperty("sv.iplogin");
            TeamServer.portLogin = Integer.parseInt(p.getProperty("sv.portlogin"));
            // db.thamSoJdbc: them vao cuoi moi URL JDBC (vd NovaDB: useSSL=false). Trong = nhu cu.
            Database.datThamSoJdbc(p.getProperty("db.thamSoJdbc"));
            Database.setLink(p.getProperty("db.host"), p.getProperty("db.name"), p.getProperty("db.user"), p.getProperty("db.password"), p.getProperty("db.maxco"));
            Database.setLinkBoardNap(p.getProperty("db.host"), p.getProperty("db.nameNap"), p.getProperty("db.userNap"), p.getProperty("db.pass"), p.getProperty("db.maxNap"));
            Database.setLinkGiftCode(p.getProperty("db.host"), p.getProperty("db.nameNap"), p.getProperty("db.userNap"), p.getProperty("db.pass"), p.getProperty("db.maxNap"));
            Database.setLink1(p.getProperty("db.host1"), p.getProperty("db.name1"), p.getProperty("db.user1"), p.getProperty("db.password1"), p.getProperty("db.maxco1"));
            // CAU HINH TRUNG TAM (com.htbb.cauhinh, docs/cau-hinh.md): doc bang cau_hinh NGAY BAY GIO va
            // ghi de vao p cac khoa co trong CSDL -> moi p.getProperty(...) ben duoi (co che do, dai han, nap tien,
            // DoS, dang ky...) theo thu tu CSDL > server.ini > mac dinh. Cong / CSDL o tren chi doc tu server.ini.
            com.htbb.cauhinh.CauHinhTrungTam.khoiTao(p);
            Database.tableRace = p.getProperty("sv.tablerace");
            Database.createRace = Byte.parseByte(p.getProperty("sv.createRace"));
            Database.dayCreateRace = p.getProperty("sv.dayrace");
            TeamServer.timeStart = Integer.parseInt(p.getProperty("db.hour"));
            TeamServer.minute = Integer.parseInt(p.getProperty("db.minute"));
            TeamServer.language = Byte.parseByte(p.getProperty("sv.lang"));
            Map.openLog = (Byte.parseByte(p.getProperty("sv.me")) == 1);
            // sv.expvantieu / sv.Luckyexp / sv.ExpQua: chuyen sang server.CauHinh tu 11/09/2026
            // (bang cau_hinh, nap lai duoc qua trang HTBB-Web /admin/cauhinh, khong con bat buoc phai
            // co dong trong server.ini nua - xem CauHinh.napLai()). Ba dong trong server.ini van
            // giu lai lam gia tri khoi tao lan dau (CauHinh.dongBoLucKhoiDong()), giong 26 khoa
            // con lai cua CauHinh - KHONG doc truc tiep o day nua de tranh doc hai lan hai noi.
            TeamServer.CodeTest = Byte.parseByte(p.getProperty("sv.CodeTest")) == 1;
            Database.ENABLE_ACTIVE_CHECK = Byte.parseByte(p.getProperty("sv.EnableAvtiveCheck")) == 1;
            // ==== TI LE DROP KHI TRAIN QUAI (phan tram so voi ti le goc, 100 = nhu cu, 0 = tat) ====
            real.Monster.PC_TILE_XU = tilePercent(p.getProperty("sv.tileXu"), 10);
            real.Monster.PC_TILE_LUONG = tilePercent(p.getProperty("sv.tileLuong"), 20);
            real.Monster.PC_TILE_ITEM_SUKIEN = tilePercent(p.getProperty("sv.tileItemSuKien"), 20);
            real.Monster.PC_TILE_POTION = tilePercent(p.getProperty("sv.tilePotion"), 30);
            real.Monster.PC_TILE_NGOC = tilePercent(p.getProperty("sv.tileNgoc"), 30);
            real.Monster.PC_TILE_ITEM = tilePercent(p.getProperty("sv.tileItem"), 20);
            Map.DIV_DROP_XU = Math.max(1, parseIntDefault(p.getProperty("sv.chiaXuRoi"), 10));
            TeamServer.giaXuBoHoaDoVuLan = parseIntDefault(p.getProperty("sv.giaXuBoHoaDoVuLan"), 150000);
            // Khoa tuy chon - khong co trong server.ini thi tat. Xem chu thich o
            // khai bao TeamServer.thuNghiemThuCuoi.
            final String tenThuNghiem = p.getProperty("sv.thuNghiemThuCuoi");
            TeamServer.thuNghiemThuCuoi = (tenThuNghiem == null) ? "" : tenThuNghiem.trim().toLowerCase();
            if (!TeamServer.thuNghiemThuCuoi.isEmpty()) {
                System.out.println("[THUNGHIEM] gui kem id thu cuoi qua duong danh hieu cho: "
                        + TeamServer.thuNghiemThuCuoi);
            }
            TeamServer.tileGoc = new int[]{real.Monster.PC_TILE_XU, real.Monster.PC_TILE_LUONG,
                real.Monster.PC_TILE_ITEM_SUKIEN, real.Monster.PC_TILE_POTION,
                real.Monster.PC_TILE_NGOC, real.Monster.PC_TILE_ITEM, Map.DIV_DROP_XU};
            System.out.println("TI LE DROP TRAIN QUAI -> xu=" + real.Monster.PC_TILE_XU
                    + "% luong=" + real.Monster.PC_TILE_LUONG
                    + "% itemSuKien=" + real.Monster.PC_TILE_ITEM_SUKIEN
                    + "% potion=" + real.Monster.PC_TILE_POTION
                    + "% ngoc=" + real.Monster.PC_TILE_NGOC
                    + "% item=" + real.Monster.PC_TILE_ITEM
                    + "%, chia so xu roi ra=" + Map.DIV_DROP_XU);

            // Gio chiem thanh: cung nguon voi bang setting trong game, admin sua
            // trong game se duoc ghi tra lai server.ini (xem CauHinh).
            CauHinh.apDungGioChiemThanh(p);
            CauHinh.apDungSuKienWorldCup(p);
            CauHinh.apDungLichSuKien(p);
            Database.CHO_PHEP_TAO_TEN_GM = "1".equals(p.getProperty(CauHinh.K_TAO_TEN_GM, "0").trim());
            if (Database.CHO_PHEP_TAO_TEN_GM) {
                System.out.println("CANH BAO: sv.choPhepTaoTenGM=1 - dang cho phep tao nhan vat"
                        + " mang ten GM trong sv.adminAccounts. Tat lai sau khi tao xong.");
            }
            System.out.println("CHIEM THANH -> "
                    + (real.MapTown.TimeAdminCheat < 0
                            ? "lich chuan 21h thu 3 + thu 7"
                            : real.MapTown.TimeAdminCheat + "h" + real.MapTown.minuteAdminCheat
                            + (real.MapTown.dayAdminCheat.isEmpty() ? " moi ngay" : " ngay " + real.MapTown.dayAdminCheat)));

            TeamServer.website = p.getProperty("sv.website", "").trim();
            TeamServer.linkTaiGame = p.getProperty("sv.linkTaiGame", "").trim();
            Database.MAX_CHAR_PER_ACCOUNT = parseIntDefault(p.getProperty("sv.maxCharPerAccount"), 3);
            if (Database.MAX_CHAR_PER_ACCOUNT < 1) {
                Database.MAX_CHAR_PER_ACCOUNT = 1;
            }
            if (Database.MAX_CHAR_PER_ACCOUNT > 3) {
                Database.MAX_CHAR_PER_ACCOUNT = 3; // client chi ho tro 3 slot
            }
            System.out.println("MAX CHAR PER ACCOUNT = " + Database.MAX_CHAR_PER_ACCOUNT);

            // ==== TEST FLAGS (đọc an toàn, có giá trị mặc định nếu thiếu trong server.ini) ====
            // Dang ky trong game (lenh 39): sv.dangKyTrongGame / sv.dangKyTuKichHoat / ... - xem real.cmd.DangKyTrongGame
            real.cmd.DangKyTrongGame.docCauHinh(p.getProperty("sv.dangKyTrongGame"), p.getProperty("sv.dangKyTuKichHoat"),
                    p.getProperty("sv.dangKyCsdlTaiKhoan"), p.getProperty("sv.dangKyTreMs"));
            TeamServer.testMode = "1".equals(p.getProperty("sv.testMode", "0").trim());
            TeamServer.gmCanCoPInfo = parseIntDefault(p.getProperty("sv.gmCanCoPInfo"), 0) == 1;
            TeamServer.banToiThieu = parseIntDefault(p.getProperty("sv.banToiThieu"), 0);
            TeamServer.banMoiNhat = parseIntDefault(p.getProperty("sv.banMoiNhat"), 0);
            TeamServer.nhacCapNhat = parseIntDefault(p.getProperty("sv.nhacCapNhat"), 1) != 0;
            TeamServer.batChiSoMayChu = parseIntDefault(p.getProperty("sv.chiSoMayChu"), 1) != 0;
            TeamServer.testCode = p.getProperty("sv.testCode", "").trim().toLowerCase();
            TeamServer.adminAccounts.clear();
            String adminList = p.getProperty("sv.adminAccounts", "").trim();
            if (!adminList.isEmpty()) {
                for (String a : adminList.split(",")) {
                    String an = a.trim().toLowerCase();
                    if (!an.isEmpty()) {
                        TeamServer.adminAccounts.add(an);
                    }
                }
            }
            System.out.println("TEST FLAGS -> testMode=" + TeamServer.testMode
                    + ", testCode=" + (TeamServer.testCode.isEmpty() ? "(off)" : TeamServer.testCode)
                    + ", adminAccounts=" + TeamServer.adminAccounts);

            // ==== CHỐNG DoS (đọc an toàn, có default nếu thiếu trong server.ini) ====
            TeamServer.SOCKET_TIMEOUT_MS   = parseIntDefault(p.getProperty("sv.socketTimeout"), 180) * 1000;
            TeamServer.MAX_CONN_PER_IP     = parseIntDefault(p.getProperty("sv.maxConnPerIp"), 50);
            TeamServer.CONN_RATE_MAX       = parseIntDefault(p.getProperty("sv.connRateMax"), 60);
            TeamServer.CONN_RATE_WINDOW_MS = parseIntDefault(p.getProperty("sv.connRateWindow"), 30) * 1000;
            TeamServer.DOS_BAN_MS          = (long) parseIntDefault(p.getProperty("sv.dosBanSec"), 300) * 1000L;
            // Proxy tin cay (WAF / Nova agent): mien gioi han theo IP. Cau hinh trung tam doi nong.
            final String proxyTinCay = p.getProperty("sv.proxyTinCay");
            System.out.println("PROXY TIN CAY -> " + com.htbb.mang.ProxyTinCay.dat(
                    proxyTinCay == null ? com.htbb.mang.ProxyTinCay.MAC_DINH : proxyTinCay));
            com.htbb.mang.GioiHanIp.datDangNhap(p.getProperty("sv.dangNhapIpNguong"), p.getProperty("sv.dangNhapIpChoMs"));
            // Nguong NGAP GOI TIN. Canh theo do tre Internet, nen may nha chay qua loopback
            // (khong co do tre) phai nang len khong thi client cache rong bi cat oan. Xem
            // io/BoDemGoiTin. Doc kieu co default: thieu khoa thi giu so cu, KHONG nem loi
            // lam bo qua moi dong ben duoi.
            io.BoDemGoiTin.GOI_THUONG_TOI_DA = parseIntDefault(
                    p.getProperty("sv.tranGoiThuong"), io.BoDemGoiTin.GOI_THUONG_MAC_DINH);
            io.BoDemGoiTin.GOI_TAI_NGUYEN_TOI_DA = parseIntDefault(
                    p.getProperty("sv.tranGoiTaiNguyen"), io.BoDemGoiTin.GOI_TAI_NGUYEN_MAC_DINH);
            io.BoDemGoiTin.GOI_TRA_CUU_TOI_DA = parseIntDefault(
                    p.getProperty("sv.tranGoiTraCuu"), io.BoDemGoiTin.GOI_TRA_CUU_MAC_DINH);
            io.BoDemGoiTin.datMienIp(p.getProperty("sv.tranGoiMienIp", ""));
            // Nen goi lon cho client tu bao giai nen duoc (io.GoiNen). Mac dinh bat; client cu khong anh huong.
            io.GoiNen.BAT = parseIntDefault(p.getProperty("sv.nenGoi"), 1) != 0;
            io.GoiNen.NGUONG = Math.max(64, parseIntDefault(p.getProperty("sv.nenGoiTu"), 2048));
            System.out.println("NEN GOI -> " + (io.GoiNen.BAT ? "bat, goi >= " + io.GoiNen.NGUONG + " B (chi client co kha nang nen)" : "tat"));
            // 0.9.7.1: chia goi lon thanh manh cho client J2ME tu bao gom duoc (io.GoiManh)
            io.GoiManh.BAT = parseIntDefault(p.getProperty("sv.manhGoi"), 1) != 0;
            io.GoiManh.NGUONG = Math.max(1024, parseIntDefault(p.getProperty("sv.manhGoiTu"), 16384));
            io.GoiManh.CO_MANH = Math.max(256, Math.min(32000, parseIntDefault(p.getProperty("sv.coManhGoi"), 8192)));
            System.out.println("MANH GOI -> " + (io.GoiManh.BAT ? "bat, goi >= " + io.GoiManh.NGUONG + " B, manh " + io.GoiManh.CO_MANH + " B (chi client co kha nang manh)" : "tat"));
            if (!io.BoDemGoiTin.MIEN_IP.isEmpty()) {
                com.htbb.log.Nhat.canhBao("NGAP_GOI", "sv.tranGoiMienIp=" + io.BoDemGoiTin.MIEN_IP
                        + " - cac IP nay KHONG bi dem ngap goi (chi dung cho may chay bot tai)");
            }
            // Chong spam chat / thao tac + gop thong bao toan server (docs/cau-hinh.md).
            com.htbb.chongspam.ChongSpam.docCauHinh(p);
            com.htbb.chongspam.ThongBaoGop.docCauHinh(p);
            System.out.println(com.htbb.chongspam.ChongSpam.moTa());
            System.out.println(com.htbb.chongspam.ThongBaoGop.moTa());
            System.out.println("NGAP GOI TIN -> tran thuong=" + io.BoDemGoiTin.GOI_THUONG_TOI_DA
                    + ", tran tai nguyen=" + io.BoDemGoiTin.GOI_TAI_NGUYEN_TOI_DA
                    + ", tran tra cuu=" + io.BoDemGoiTin.GOI_TRA_CUU_TOI_DA
                    + " moi " + io.BoDemGoiTin.CUA_SO_MS + "ms");
            System.out.println("DoS PROTECT -> socketTimeout=" + (TeamServer.SOCKET_TIMEOUT_MS / 1000)
                    + "s, maxConnPerIp=" + TeamServer.MAX_CONN_PER_IP
                    + ", connRate=" + TeamServer.CONN_RATE_MAX + "/" + (TeamServer.CONN_RATE_WINDOW_MS / 1000)
                    + "s, banSec=" + (TeamServer.DOS_BAN_MS / 1000));
            
            TeamServer.RELOGIN_WAIT_MS = (long) parseIntDefault(p.getProperty("sv.reloginWaitSec"), 0) * 1000L;
            TeamServer.isSuKienTrungThul2016 = Byte.parseByte(p.getProperty("sv.isSuKienTrungThul2016")) == 1;
            TeamServer.isSuKienTet2017 = Byte.parseByte(p.getProperty("sv.isSuKienTet2017")) == 1;
            TeamServer.isSuKienGioTo2016 = Byte.parseByte(p.getProperty("sv.isSuKienGioTo2016")) == 1;
            TeamServer.isSuKienNoel2023 = Byte.parseByte(p.getProperty("sv.isSuKienNoel2023")) == 1;
            TeamServer.isSuKienTetduonglich2024 = Byte.parseByte(p.getProperty("sv.isSuKienTetduonglich2024")) == 1;
            // Game luon la Pokemon: khoa cu sv.che_do_pokemon (neu con trong server.ini / CSDL) bi bo qua.
            System.out.println("GAME -> " + com.htbb.pokemon.ThuongHieu.tenGame());
            // Thiet ke dai han - giai doan 2 (docs/thiet-ke-game.md). Khong qua
            // parseX: thieu dong = TAT (hanh vi cu). Moi cho re nhanh doc qua
            // com.htbb.pokemon.LauDai.bat(). Doi gia tri phai khoi dong lai.
            com.htbb.pokemon.LauDai.dat(com.htbb.pokemon.LauDai.docGiaTri(p.getProperty("sv.lau_dai")));
            com.htbb.pokemon.LauDai.datNgayKhaiTruong(p.getProperty("sv.ngay_khai_truong"));
            // Tran cap theo mua: mac dinh TAT (thieu dong = 0) - docs/thiet-ke-game.md.
            com.htbb.pokemon.LauDai.datTranTheoMua(com.htbb.pokemon.LauDai.docGiaTri(p.getProperty("sv.tran_cap_theo_mua")));
            // Gio bao tri hang ngay (HH:MM | tat | trong = ngau nhien 4:30-5:30 nhu cu).
            real.BaoTriDaily.datGio(p.getProperty("sv.gioBaoTri"));
            System.out.println("LAU DAI -> " + (com.htbb.pokemon.LauDai.bat() ? "BAT" : "TAT")
                    + ", khai truong ngay thu " + com.htbb.pokemon.LauDai.ngayHienTai()
                    + ", tran cap theo mua " + (com.htbb.pokemon.LauDai.tranTheoMua() ? "BAT" : "TAT"));
            // nap tien: co sv.nap_tien (docs/van-hanh.md). Chi BAT khi ca sv.lau_dai=1.
            com.htbb.pokemon.NapTien.dat(com.htbb.pokemon.NapTien.docGiaTri(p.getProperty("sv.nap_tien")));
            System.out.println(com.htbb.pokemon.NapTien.moTaTrangThai());
            // he sinh thai: co sv.he_sinh_thai (docs/thiet-ke-game.md). Da doc som khi lop nap
            // (HeSinhThai.docTuTepCauHinh); doc lai o day de dong bo voi tep cau hinh that cua TeamServer.
            com.htbb.pokemon.HeSinhThai.dat(com.htbb.pokemon.HeSinhThai.docGiaTri(p.getProperty("sv.he_sinh_thai")));
            System.out.println("HE SINH THAI -> " + (com.htbb.pokemon.HeSinhThai.bat() ? "BAT" : "TAT"));
    
    
            

            new Logdata();
            try {
                String danhsach = p.getProperty("sv.ph");
          
                Char.except_quay_so = Char.split(danhsach, ",");
            } catch (Exception e) {
                e.printStackTrace();

            }
            try {
                String danhsach = p.getProperty("sv.choi");
             
                Char.chartopchoitrungthu = danhsach;
            } catch (Exception e) {
                e.printStackTrace();

            }
        } catch (Exception e) {
            System.out.println("Load configuration file error!");
            e.printStackTrace();
            return;
        }

        // PHAI dung truoc moi viec cham CSDL. Xem kiemTraCSDL().
        kiemTraCSDL(p);

        // Cong bat/tat su kien (bang tob_su_kien). PHAI chay TRUOC Database.startSaveChar()
        // - ham do goi Map.loadAllTemplate() -> Map.loadShop(), va bang shop dat
        // SELL/NOT_SELL theo Char.isSuKien* NGAY LUC NAP roi khong hoi lai nua. Nap cong
        // sau do la shop van theo trang thai cu. Xem real/SuKien.
        real.SuKien.nap();
        System.out.println("SU KIEN -> " + real.SuKien.moTa());
        System.out.println("SU KIEN WORLD CUP -> " + real.SuKienWorldCup.moTa());

        Database.instance.createTableTrainLuong(); // bang luu han muc luong train quai theo ngay
        Database.instance.createTableNhanChuyenLuong(); // bang luu han muc luong nhan tu chuyen khoan theo ngay
        // PHAI chay TRUOC khi nhan nguoi choi: saveChar ghi cot isCT, thieu cot thi lenh
        // luu nem loi va nhanh bat loi cua no KHOA NHAN VAT. Xem themCotCongTacHienThi().
        Database.instance.themCotCongTacHienThi();
        // Kho xu/luong o NPC Tong Quan (them 21/09/2026) - cung ly do PHAI chay TRUOC khi
        // nhan nguoi choi, xem chu thich o Database.themCotKhoXuLuong().
        try {
            Database.instance.themCotKhoXuLuong();
            System.out.println("[KHO] kiem tra/them cot tob_char.kho_xu,kho_luong,kho_luong_khoa xong.");
        } catch (Exception exKho) {
            // Khong duoc de loi o day chan ca qua trinh khoi dong server - in ra de biet
            // ma khong lam gian doan cac buoc sau. Tinh nang moi (21/09/2026), de phong
            // loi chua thay het.
            System.out.println("[KHO] LOI kiem tra cot tob_char.kho_xu/kho_luong/kho_luong_khoa: " + exKho);
            exKho.printStackTrace();
        }
        // Cot Pokemon tob_char.dex/shiny + data_monster.dex/shiny/he2. PHAI truoc
        // Database.startSaveChar() (nap template quai) va truoc khi nhan nguoi choi (saveChar
        // ghi cot dex). Xem Database.themCotPokemon().
        try {
            Database.instance.themCotPokemon();
        } catch (Exception exPoke) {
            System.out.println("[POKEMON] LOI kiem tra cot dex/shiny/he2: " + exPoke);
            exPoke.printStackTrace();
        }
        // GD2 thiet ke dai han: bang moi + doi bang EXP + chuyen EXP nhan vat cu (MOT lan, co sao
        // luu tob_char_xp_cu). PHAI truoc khi nhan nguoi choi. Tat co: khong dong vao gi.
        if (com.htbb.pokemon.LauDai.bat()) {
            data.DatabaseLauDai.taoBang();
            real.DanhHieuGame.taoBang();
            real.XepHangPvpGame.taoBang();
            real.SuKienVatPham.taoBang();
            real.DiemMocNhiemVu.taoBang();
            real.ExpLauDai.apDungBang();
            real.VatPhamDungLauDai.nap();
        }
        // Chay o CA HAI trang thai co: ngay khai truong tu dong, chuyen EXP nhan vat sang bang dung
        // voi co (bat: sang moi; tat: ve cu, tu gia tri hien tai), go lich su kien khi tat. Xem
        // real.ChuyenDoiLauDai. Da o dung bang thi khong lam gi.
        real.ChuyenDoiLauDai.khiKhoiDong();
        // Cung phai chay TRUOC khi nhan nguoi choi: chan nhan giftcode nhieu lan qua nhieu
        // nhan vat cung tai khoan. Xem Database.chuanBiGiftCodeUserId().
        Database.instance.chuanBiGiftCodeUserId();
        // Cot soluong cho gop log mua shop NPC (xem Database.saveLogBuyItemShop()).
        Database.instance.chuanBiCotSoLuongLogBuyShop();
        // 26 setting nap lai duoc chuyen tu server.ini sang bang cau_hinh (sua duoc qua
        // HTBB-Web /admin/cauhinh). Dong bo mot lan tu file (INSERT IGNORE, khong ghi de
        // dong da co) roi bat vong lap tu nap lai moi 10 giay. Xem server.CauHinh.
        CauHinh.dongBoLucKhoiDong(p);
        CauHinh.batDau();
        // 0.9.7: doc danh muc tai game (web_cai_dat 'taiGame') moi 60 giay - nhac / bat buoc cap nhat client
        com.htbb.capnhat.CapNhatClient.batDau();
        Database.startSaveChar();
        // GD2b: du lieu dai han can template vat pham / quai da nap (nguyen lieu phu, bang roi, alpha,
        // Gym, chieu moi). Tat co: khong lam gi.
        if (com.htbb.pokemon.LauDai.bat()) {
            real.NoiDungLauDai.sauKhiNapTemplate();
        }
        // G7.C: doi chu kiem hiep -> Pokemon (Text, ten vung, ten dong, ten thuoc, ngoc). PHAI
        // SAU startSaveChar (Map.gemTemplate/potionTemplates2 da nap), TRUOC khi nhan nguoi
        // choi. Xem com.htbb.pokemon.ChuPokemon.
        com.htbb.pokemon.ChuPokemon.ap();
        TeamServer.timeRemovePool = System.currentTimeMillis();
        System.out.println("Starting server...");
        (TeamServer.controllerManager = new ServerControllerManager()).addServerController(2, TeamServer.realController = new RealController());
        Map.loadNpcServer();
        // real.plugins.addShop.loadShopDB_ND();
        RealController.intance.initData();

        Database.addClanToMap();
        Map.getAllTopBegin();
        // Lam moi top (cao thu/dai gia/bang hoi/theo phe) moi 60 giay - yeu cau
        // nguoi dung 13/09/2026 dang trong luc dua top level, muon thay thu hang
        // thay doi gan thoi gian thuc thay vi phai doi server restart moi cap nhat
        // (truoc day getAllTopBegin() chi goi DUY NHAT o day, luc khoi dong).
        new Thread(() -> {
            Thread.currentThread().setName("LAM MOI TOP");
            while (TeamServer.running) {
                try {
                    Thread.sleep(60_000L);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    Map.getAllTopBegin();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // Xa cac dong log mua shop NPC da gop trong bo nho nhung qua cua so thoi
        // gian ma khong mua tiep cung mon - xem Database.saveLogBuyItemShop().
        new Thread(() -> {
            Thread.currentThread().setName("XA LOG MUA SHOP");
            while (TeamServer.running) {
                try {
                    Thread.sleep(2_000L);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    Database.instance.xaLogMuaShopHetHan();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Map.startSendInfoAdv();
        Map.addWeddingToPrivateMap();
        new Thread(DBUserLogger.instance).start();
        DataGame.load();
        Market.init();
        // Market.init (lib/htbb-core.jar) dung san luoi cho cho: ~57 ItemMarket x 2 phe x 20x4x6x16 = ~870.000
        // Vector RONG (do heap 18/10/2026: ~45 % so doi tuong song khi server rong). Thu gon mang ben
        // trong cua Vector rong (10 o -> 0): ~35 MB heap, hanh vi y het (Vector tu no ra khi add).
        thuGonVectorRong(Market.ALL_ITEM);
        thuGonVectorRong(Market.ALL_GEM);
        Database.loadAllCharSell();
        String nt = new Date(System.currentTimeMillis()).toString();
        if (!Char.isSuKienHaloween2016() && !Char.isSuKienTet2017() && !Char.isSuKienNoel() && !Char.isSuKienGioTo2016() && !Char.isSuKienHe2017() && !Char.isSuKienMini() && !Char.isSuKienMiniChucNu() && !Char.isSuKienTrungThul2016()) {
            Map.isSale = nt.startsWith("Tue");
        }
        if (Char.getDayOpen(0L).equals("2015-10-06")) {
            TeamServer.isDouble = true;
            TeamServer.timeDouble = System.currentTimeMillis();
            Map.doubleALL = 2;
        }
        Database.instance.loadAllMonsterVantieu();
        Map.initGiftCode();
        Char.timeGetPhuongHoang = System.currentTimeMillis() + 36000000L;
        Map.ThreadProcessHoiSinh();
        Char.startThreadXoso(); // chi con nhac "choi qua 180 phut" moi 15 phut
        real.VongQuay.khoiDong();
        Char.startNauBanh();
        if (!real.BaoTriDaily.Running) {
            new Thread(new real.BaoTriDaily()).start();
        }
        // Lay mau so nguoi online 5 phut/lan -> bang tob_log_online, cho trang Tong quan
        // ve bieu do. Tu tao bang va tu don dong cu hon 30 ngay.
        data.GhiSoOnline.batDau();
        // Lay mau vi tri (map/toa do) cua TUNG nguoi choi online 30 giay/lan -> bang
        // tob_vi_tri_online, cho trang quan tri xem "ai dang o dau". La snapshot (xoa het
        // ghi lai moi chu ky), khong phai log lich su.
        data.GhiViTriOnline.batDau();
        // Chi so may chu (bang tob_chi_so_may_chu + trang_thai.json) va che do bao tri bang tep
        // bao_tri.txt - xem server/ChiSoMayChu.java. Tat: sv.chiSoMayChu=0.
        if (TeamServer.batChiSoMayChu) {
            ChiSoMayChu.batDau();
        }
        // Lenh tu trang quan tri web (dat cap do...) qua bang web_lenh_nhan_vat: doc moi giay,
        // nhan vat dang online thi cong thang vao RAM roi luu ngay, offline thi ghi CSDL.
        // Xem data/LenhWeb.
        data.LenhWeb.batDau();
        // nap tien: vong cong don / thu hoi / cho trang tri (chi chay khi NapTien.bat()).
        com.htbb.pokemon.NapTienGame.batDau();
        // Phat qua đua top tu trang quan tri web: bang tob_top_thuong, doc luc nguoi choi
        // mo menu NPC Tong Quan (khong can vong lap rieng). Xem real/SuKienDuaTop.
        real.SuKienDuaTop.taoBang();
        // Lich hen mo/dong su kien + tu khoi dong lai khi thiet lap doi. PHAI chay SAU
        // BaoTriDaily: no bat co 5h_systems.restart cho vong lap ben do doc va tat may,
        // chu khong tu tat. Xem real/LichSuKien.
        real.LichSuKien.batDau();
        String daycheck = Char.getDayTime(0L);
        if (daycheck.compareTo("2020-11-18 17:15:00") < 0) {
            Char.initINfoCheckTrungItemVV();
            Char.total_open = 0;
            Char.numberLucky = 5000;
            Database.instance.saveEvent(Map.event.getInfo());
            System.err.println("KHOI TAO GIA TRI RANNDOM NHAN QUA TEAMSERVER " + Map.getInfoSavetrungpet());
        }
        if (isServerLienDau()) {
            Map.onSMS = false;
        }
        new Thread(() -> {
            while (true) {
                Calendar cl = Calendar.getInstance();
                int iHour = cl.get(Calendar.HOUR_OF_DAY);
                int iMinute = cl.get(Calendar.MINUTE);
                if (iHour == TeamServer.timeStart && iMinute >= TeamServer.minute && iMinute <= TeamServer.minute + 1 && !TeamServer.isStart) {
                    TeamServer.isStart = true;
                    try {
                        Thread.sleep(30000L);
                        System.out.println("CHUAN BI TAT SV");
                        ((AdminHandler) RealController.getHandler(47)).stopServer();
                        Thread.sleep(30000L);
                        System.exit(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (iHour != TeamServer.timeStart) {
                    TeamServer.isStart = false;
                }
            
                try {
                    Thread.sleep(30000L);
                    Database.instance.loadTimeX2Server();
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
        new Thread(() -> {
            Thread.currentThread().setName("THREAD CHECK TIME LIFE BOSS");
            while (TeamServer.running) {
                try {
                    if (!Map.openLog) {
                        if (TeamServer.userLogQueue.size() == 0) {
                            Thread.sleep(10L);
                        } else {
                            String info = TeamServer.myTake();
                            Net.requestLink(info);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("LOI GOI LINK VTC TRONG TEAM");
                }
                if (TeamServer.isDouble) {
                    Map.doubleALL = 2;
                    if (System.currentTimeMillis() - TeamServer.timeDouble > 86400000L) {
                        TeamServer.isDouble = false;
                        Map.doubleALL = 1;
                    }
                }
                if (System.currentTimeMillis() - TeamServer.timUpBoard >= TeamServer.timeBoardRun) {
                    if (TeamServer.timUpBoard != 0L) {
                        TeamServer.timeOutBoard = System.currentTimeMillis();
                    }
                    TeamServer.timUpBoard = System.currentTimeMillis();
                }
                try {
                    Thread.sleep(10L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    for (int i = 0; i < Map.boss.size(); ++i) {
                        Map.boss.get(i).checkTimeLife();
                    }
                    for (int i = 0; i < Map.bossTime.size(); ++i) {
                        Map.bossTime.get(i).checkTimeLife();
                    }
                    Thread.sleep(1000L);
                } catch (Exception ignored) {
                }
            }
        }).start();
        readText("map/help.txt");
        new Thread(() -> {
            Thread.currentThread().setName("LOG CCU");
            while (TeamServer.running) {
                TeamServer.maxCCU = (Math.max(TeamServer.maxCCU, SessionManager.instance.size()));
                TeamServer.minCCU = (Math.min(TeamServer.minCCU, SessionManager.instance.size()));
                TeamServer.maxUser = (Math.max(TeamServer.maxUser, CharManager.instance.totalChar()));
                long t = System.currentTimeMillis();
                if (t - TeamServer.timeRemovePool > 60000L) {
                    try {
                        Database.connPool.closeIdleConnection();
                        Database.connPoolNap.closeIdleConnection();
                    } catch (Exception ignored) {
                    }
                    TeamServer.timeRemovePool = System.currentTimeMillis();
                }
                if (t - TeamServer.timeStartServer > 3600000L) {
                    TeamServer.timeStartServer = System.currentTimeMillis();

                    TeamServer.maxCCU = SessionManager.instance.size();
                    TeamServer.minCCU = SessionManager.instance.size();
                }
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        TeamServer.timeStartServer = System.currentTimeMillis();
        TeamServer.timeRemoveSecssion = System.currentTimeMillis();
        TeamServer.maxCCU = 0;
        TeamServer.minCCU = 0;
        new Thread(() -> {
            Thread.currentThread().setName("HUY SESSION WA LAU");
            TeamServer.timeRemoveChar = System.currentTimeMillis();
            while (TeamServer.running) {
                long t = TeamServer.timeRemoveSecssion = System.currentTimeMillis();
                Vector<Session> sessionList = SessionManager.instance.sessionList;
                int size = sessionList.size();
                int firmwareAnd = 0;
                for (int i = size - 1; i >= 0; --i) {
                    try {
                        Session s = sessionList.get(i);
                        if (s != null) {
                            CCUProvider ccu = Database.allProvider.get("0");
                            if (ccu != null) {
                                ++ccu.tempCCU;
                                int[] ccuFirmWareTemp = ccu.ccuFirmWareTemp;
                                byte firmWare = s.firmWare;
                                ++ccuFirmWareTemp[firmWare];
                            }
                            if (s.firmWare == 1) {
                                ++firmwareAnd;
                            }
                            if (s.userID == -1 && t - s.connectTime > 60000L) {
                                s.disconnect(5);
                            } else if (t - s.lastActiveTime > 600000L) {
                                s.disconnect(6);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
                TeamServer.maxCCUAD = firmwareAnd;
                try {
                    CCUProvider ccu2 = Database.allProvider.get("0");
                    ccu2.ccu = ccu2.tempCCU;
                    ccu2.tempCCU = 0;
                    for (int k = 0; k < ccu2.ccuFirmWareTemp.length; ++k) {
                        ccu2.ccuFirmWare[k] = ccu2.ccuFirmWareTemp[k];
                        ccu2.ccuFirmWareTemp[k] = 0;
                    }
            
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(30000L);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
        Map.loadImageCharWeaponeData();

        SelectCharHandler.loadImgSkill();
        OtherHandle.loadImgHorse();
      

        RealController.add_more_idMapSend2Client();

        try {
            TeamServer.listenSocket = new ServerSocket(TeamServer.PORT, 200);
            System.out.println("Listen on port " + TeamServer.PORT);
            while (TeamServer.running) {
                try {
                    Socket clientSocket = TeamServer.listenSocket.accept();
                    String ipconnect = "";
                    try {
                        InetAddress inet = clientSocket.getInetAddress();
                        ipconnect = inet.getHostAddress();
                        if (TeamServer.ALL_IPCONNECT_BLOCK.get(ipconnect) != null && !TeamServer.mienGioiHanIp(ipconnect)) {
                            System.out.println("IP BLOCK " + ipconnect);
                            clientSocket.close();
                            continue;
                        }
                        // ==== CHỐNG DoS: chặn IP flood / vượt giới hạn kết nối ====
                        if (!TeamServer.dosAccept(ipconnect)) {
                            try { clientSocket.close(); } catch (Exception ignored) {}
                            continue;
                        }
                        InfoClientConnect info = TeamServer.ALL_IPCONNECT.get(ipconnect);
                        if (info == null) {
                            info = new InfoClientConnect();
                            info.ip = ipconnect;
                            info.countConnect();
                            TeamServer.ALL_IPCONNECT.put(ipconnect, info);
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    if (AdminHandler.isStopServer) {
                        clientSocket.close();
                    } else {
                        Session conn = new Session(clientSocket, TeamServer.realController);
                        conn.ip = ipconnect;
                        TeamServer.dosOnOpen(ipconnect);
                        conn.start();
                    }
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
        } catch (BindException bindEx) {
            RealController.intance.saveAllChar(false);
            System.exit(0);
            System.out.println("SERVER EXIT");
        } catch (Exception genEx) {
            genEx.printStackTrace();
        }
        System.out.println("Server shuting down");
        System.out.println("Server stop");
    }

   
  

    public static Vector<String> readData(String st, String filename) {
        Vector<String> test = new Vector<>();
        try {
            FileInputStream fis = new FileInputStream(st);
            DataInputStream is = new DataInputStream(fis);
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
            TeamServer.stHelp = "";
            int count = 0;
            try {
                int ch;
                while ((ch = isr.read()) > -1) {
                    char c = (char) ch;
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                        TeamServer.stHelp = String.valueOf(TeamServer.stHelp) + (char) ch;
                    } else {
                        if (TeamServer.stHelp.trim().length() <= 0) {
                            continue;
                        }
                        test.add(TeamServer.stHelp);
                        TeamServer.stHelp = "";
                        ++count;
                    }
                }
                System.out.println("SO LUONG CHAR " + filename + " la >> " + count);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            try {
                isr.close();
            } catch (Exception ignored) {
            }
            try {
                is.close();
            } catch (Exception ignored) {
            }
            try {
                fis.close();
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
        return test;
    }

    private static String readText(String st) {
        final String test = "";
        try {
            FileInputStream fis = new FileInputStream(st);
            DataInputStream is = new DataInputStream(fis);
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
            TeamServer.stHelp = "";
            try {
                int ch;
                while ((ch = isr.read()) > -1) {
                    TeamServer.stHelp = String.valueOf(TeamServer.stHelp) + (char) ch;
                }
                // map/help.txt luu UTF-8 co BOM: U+FEFF dau chuoi client ve thanh "?" o dau Huong dan
                if (TeamServer.stHelp.startsWith("\uFEFF")) {
                    TeamServer.stHelp = TeamServer.stHelp.substring(1);
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            try {
                isr.close();
            } catch (Exception ignored) {
            }
            try {
                is.close();
            } catch (Exception ignored) {
            }
            try {
                fis.close();
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
        return test;
    }

    public static String getNameServer() {
        if (TeamServer.server == 5) {
            return "KM";
        }
        if (TeamServer.server == 3) {
            return "MS";
        }
        if (TeamServer.server == 2) {
            return "TA";
        }
        return "KM";
    }

}
