import com.htbb.mang.GioiHanIp;
import com.htbb.mang.ProxyTinCay;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Kiem thu sv.proxyTinCay (com.htbb.mang.ProxyTinCay / GioiHanIp): sau WAF / Nova agent moi
 * nguoi choi chung vai IP nguon, nen ket noi tu proxy tin cay phai duoc MIEN gioi han theo IP
 * (maxConnPerIp, connRate, ban dosBanSec, cho dang nhap 3s, cho chon nhan vat, han muc dang ky
 * theo IP) - con IP thuong van bi chan nhu cu.
 */
public final class KiemProxyTinCay {

    public static void chay() {
        Kiem.nhom("com.htbb.mang.ProxyTinCay - proxy tin cay mien gioi han theo IP");
        try {
            phanTichVaKhop();
            dosTheoIp();
            choDangNhap();
            dangKyTheoIp();
            dongBoCauHinh();
        } finally {
            // tra lai mac dinh cho cac test sau
            ProxyTinCay.dat(ProxyTinCay.MAC_DINH);
            GioiHanIp.datDangNhap(null, null);
        }
    }

    private static void phanTichVaKhop() {
        final ProxyTinCay md = ProxyTinCay.phanTich(ProxyTinCay.MAC_DINH);
        Kiem.bang("mac dinh 2 muc, khong loi", "2/0", md.soMuc() + "/" + md.loi().size());
        Kiem.dung("mac dinh: 127.0.0.1", md.chua("127.0.0.1"));
        Kiem.dung("mac dinh: ::1", md.chua("::1"));
        Kiem.dung("mac dinh: 0:0:0:0:0:0:0:1 (dang getHostAddress)", md.chua("0:0:0:0:0:0:0:1"));
        Kiem.dung("mac dinh: [::1] / ::1%lo", md.chua("[::1]") && md.chua("::1%lo"));
        Kiem.dung("mac dinh: ::ffff:127.0.0.1 (IPv4-mapped)", md.chua("::ffff:127.0.0.1"));
        Kiem.dung("mac dinh KHONG tin 127.0.0.2", !md.chua("127.0.0.2"));
        Kiem.dung("mac dinh KHONG tin 203.0.114.97", !md.chua("203.0.114.97"));

        final ProxyTinCay p = ProxyTinCay.phanTich(" 127.0.0.1, ::1 ,203.0.112.0/22,fd00::/8 ");
        Kiem.bang("ban VPS: 4 muc", Integer.valueOf(4), Integer.valueOf(p.soMuc()));
        for (final String ip : new String[]{"203.0.112.0", "203.0.114.97", "203.0.114.98", "203.0.115.255"}) {
            Kiem.dung("/22 chua " + ip, p.chua(ip));
        }
        for (final String ip : new String[]{"203.0.111.255", "203.0.116.0", "203.1.114.97", "103.0.114.97"}) {
            Kiem.dung("/22 KHONG chua " + ip, !p.chua(ip));
        }
        Kiem.dung("fd00::/8 chua fd12:3456::1", p.chua("fd12:3456::1"));
        Kiem.dung("fd00::/8 KHONG chua fe80::1", !p.chua("fe80::1"));
        Kiem.dung("IPv6 khong khop dai IPv4", !p.chua("::33:cf00"));
        Kiem.dung("::ffff:203.0.113.1 khop /22 IPv4", p.chua("::ffff:203.0.113.1"));

        final ProxyTinCay sai = ProxyTinCay.phanTich("localhost,1.2.3,1.2.3.4/33,256.1.1.1,abc,::1/129,1.2.3.4/x,10.0.0.0/8");
        Kiem.bang("muc sai bi bo qua, ghi loi",
                Arrays.asList("localhost", "1.2.3", "1.2.3.4/33", "256.1.1.1", "abc", "::1/129", "1.2.3.4/x"), sai.loi());
        Kiem.bang("muc dung van giu", Integer.valueOf(1), Integer.valueOf(sai.soMuc()));
        Kiem.dung("hostname khong bao gio duoc tin (khong tra DNS)", !sai.chua("localhost"));
        Kiem.dung("/0 tin moi IPv4", ProxyTinCay.phanTich("0.0.0.0/0").chua("8.8.8.8"));
        Kiem.dung("/0 IPv4 khong tin IPv6", !ProxyTinCay.phanTich("0.0.0.0/0").chua("2001:db8::1"));
        Kiem.dung("rong / null: khong tin ai", !ProxyTinCay.phanTich("").chua("127.0.0.1")
                && !ProxyTinCay.phanTich(null).chua("127.0.0.1"));
        Kiem.dung("ip null / rac: false", !p.chua(null) && !p.chua("") && !p.chua("xyz"));

        // doi nong
        ProxyTinCay.dat("192.0.2.0/24");
        Kiem.dung("dat(): ban moi co hieu luc ngay", ProxyTinCay.tinCay("192.0.2.9") && !ProxyTinCay.tinCay("127.0.0.1"));
        ProxyTinCay.dat(ProxyTinCay.MAC_DINH);
        Kiem.dung("dat(mac dinh): tro lai", ProxyTinCay.tinCay("127.0.0.1") && !ProxyTinCay.tinCay("192.0.2.9"));
    }

    private static void dosTheoIp() {
        final int maxConn = server.TeamServer.MAX_CONN_PER_IP;
        final int rateMax = server.TeamServer.CONN_RATE_MAX;
        final int rateWin = server.TeamServer.CONN_RATE_WINDOW_MS;
        final String waf = "203.0.114.97";
        final String thuong = "198.51.100.7";
        try {
            server.TeamServer.MAX_CONN_PER_IP = 3;
            server.TeamServer.CONN_RATE_MAX = 5;
            server.TeamServer.CONN_RATE_WINDOW_MS = 60000;
            ProxyTinCay.dat("127.0.0.1,::1,203.0.112.0/22");

            boolean qua = true;
            for (int i = 0; i < 200; i++) {
                qua &= server.TeamServer.dosAccept(waf);
                server.TeamServer.dosOnOpen(waf);
            }
            Kiem.dung("WAF: 200 ket noi lien tiep deu qua (khong maxConnPerIp/connRate)", qua);
            Kiem.dung("WAF: khong bi ban, khong bi dem", !server.TeamServer.IP_BAN.containsKey(waf)
                    && !server.TeamServer.IP_OPEN.containsKey(waf) && !server.TeamServer.IP_RATE.containsKey(waf));
            Kiem.dung("WAF: mienGioiHanIp", server.TeamServer.mienGioiHanIp(waf));

            int duoc = 0;
            for (int i = 0; i < 3; i++) {
                if (server.TeamServer.dosAccept(thuong)) {
                    server.TeamServer.dosOnOpen(thuong);
                    duoc++;
                }
            }
            Kiem.bang("IP thuong: mo duoc 3 ket noi", Integer.valueOf(3), Integer.valueOf(duoc));
            Kiem.dung("IP thuong: ket noi thu 4 bi chan (maxConnPerIp=3)", !server.TeamServer.dosAccept(thuong));
            server.TeamServer.dosAccept(thuong); // lan 5 trong cua so
            Kiem.dung("IP thuong: vuot connRateMax=5 -> bi ban", !server.TeamServer.dosAccept(thuong)
                    && server.TeamServer.IP_BAN.containsKey(thuong));

            // sv.proxyTinCay doi nong giua luc mo va dong: dong van phai tru bo dem.
            ProxyTinCay.dat(thuong);
            Kiem.dung("IP vua them vao danh sach: het bi ban ngay", server.TeamServer.dosAccept(thuong));
            for (int i = 0; i < 3; i++) {
                server.TeamServer.dosOnClose(thuong);
            }
            Kiem.dung("dosOnClose van tru bo dem sau khi IP thanh tin cay", !server.TeamServer.IP_OPEN.containsKey(thuong));
        } finally {
            server.TeamServer.MAX_CONN_PER_IP = maxConn;
            server.TeamServer.CONN_RATE_MAX = rateMax;
            server.TeamServer.CONN_RATE_WINDOW_MS = rateWin;
            for (final String ip : new String[]{waf, thuong}) {
                server.TeamServer.IP_BAN.remove(ip);
                server.TeamServer.IP_OPEN.remove(ip);
                server.TeamServer.IP_RATE.remove(ip);
            }
            ProxyTinCay.dat(ProxyTinCay.MAC_DINH);
        }
    }

    private static void choDangNhap() {
        ProxyTinCay.dat("127.0.0.1,::1,203.0.112.0/22");
        GioiHanIp.datDangNhap(null, null);
        Kiem.bang("mac dinh nguong 10", Integer.valueOf(10), Integer.valueOf(GioiHanIp.dangNhapNguong));
        Kiem.bang("mac dinh cho 3000ms", Integer.valueOf(3000), Integer.valueOf(GioiHanIp.dangNhapChoMs));
        Kiem.bang("IP thuong 10 lan/giay -> cho 3000", Long.valueOf(3000L), Long.valueOf(GioiHanIp.choDangNhapMs("198.51.100.7", 10)));
        Kiem.bang("IP thuong 9 lan/giay -> khong cho", Long.valueOf(0L), Long.valueOf(GioiHanIp.choDangNhapMs("198.51.100.7", 9)));
        Kiem.bang("WAF 1000 lan/giay -> khong cho", Long.valueOf(0L), Long.valueOf(GioiHanIp.choDangNhapMs("203.0.114.98", 1000)));
        Kiem.dung("WAF: khong dem/ghi nghi van theo IP", !GioiHanIp.apDung("203.0.114.98")
                && !GioiHanIp.vuotNguongDangNhap("203.0.114.98", 1000));
        GioiHanIp.datDangNhap("0", "3000");
        Kiem.bang("nguong 0 = tat", Long.valueOf(0L), Long.valueOf(GioiHanIp.choDangNhapMs("198.51.100.7", 99999)));
        GioiHanIp.datDangNhap("5", "-7");
        Kiem.bang("cho am -> kep 0 (chi ghi log)", Long.valueOf(0L), Long.valueOf(GioiHanIp.choDangNhapMs("198.51.100.7", 5)));
        Kiem.dung("... nhung van vuot nguong (ghi nghi van)", GioiHanIp.vuotNguongDangNhap("198.51.100.7", 5));
        GioiHanIp.datDangNhap("abc", "999999");
        Kiem.bang("rac -> mac dinh, qua tran -> 10000", "10/10000", GioiHanIp.dangNhapNguong + "/" + GioiHanIp.dangNhapChoMs);
        GioiHanIp.datDangNhap(null, null);

        final String lh = doc("src/real/cmd/LoginHandler.java");
        Kiem.dung("LoginHandler het viet cung Thread.sleep(3000L)", !lh.contains("Thread.sleep(3000L)"));
        Kiem.dung("LoginHandler dung GioiHanIp", lh.contains("GioiHanIp.choDangNhapMs(") && lh.contains("GioiHanIp.apDung(session.ip)"));
        Kiem.dung("SelectCharHandler bo qua proxy tin cay", doc("src/real/cmd/SelectCharHandler.java").contains("GioiHanIp.apDung(session.ip)"));
        ProxyTinCay.dat(ProxyTinCay.MAC_DINH);
    }

    private static void dangKyTheoIp() {
        ProxyTinCay.dat("127.0.0.1,::1,203.0.112.0/22");
        final Object a = new Object();
        final Object b = new Object();
        Kiem.bang("dang ky IP thuong: khoa theo IP", "dk:198.51.100.7", real.cmd.DangKyTrongGame.khoaSpam("198.51.100.7", a));
        Kiem.dung("dang ky IP thuong: hai ket noi chung khoa",
                real.cmd.DangKyTrongGame.khoaSpam("198.51.100.7", a).equals(real.cmd.DangKyTrongGame.khoaSpam("198.51.100.7", b)));
        Kiem.dung("dang ky sau WAF: moi ket noi mot khoa", !real.cmd.DangKyTrongGame.khoaSpam("203.0.114.97", a)
                .equals(real.cmd.DangKyTrongGame.khoaSpam("203.0.114.97", b)));
        Kiem.dung("dang ky sau WAF: khoa khong chua IP proxy", !real.cmd.DangKyTrongGame.khoaSpam("203.0.114.97", a).contains("203.0.114"));
        Kiem.dung("han muc gio/ngay: IP thuong dem theo IP", real.cmd.DangKyTrongGame.demTheoIp("198.51.100.7"));
        Kiem.dung("han muc gio/ngay: WAF chi dem theo may", !real.cmd.DangKyTrongGame.demTheoIp("203.0.114.97"));
        ProxyTinCay.dat(ProxyTinCay.MAC_DINH);
    }

    private static void dongBoCauHinh() {
        final String dm = doc("../tools/cauhinh/danh_muc.json");
        Kiem.dung("danh_muc.json: sv.proxyTinCay mac dinh 127.0.0.1,::1",
                dm.contains("\"khoa\": \"sv.proxyTinCay\"") && dm.contains("\"macDinh\": \"" + ProxyTinCay.MAC_DINH + "\""));
        Kiem.dung("danh_muc.json: sv.dangNhapIpNguong / sv.dangNhapIpChoMs",
                dm.contains("\"khoa\": \"sv.dangNhapIpNguong\"") && dm.contains("\"khoa\": \"sv.dangNhapIpChoMs\""));
        final String tmpl = doc("../vps/server.ini.tmpl");
        // Ban mau chi tin loopback (Nova agent cung may); dai WAF them qua cau hinh trung tam
        // (/admin, ap dung ngay) khi can - KHONG ghim IP WAF trong git.
        final String dong = "sv.proxyTinCay=" + ProxyTinCay.MAC_DINH;
        Kiem.dung("server.ini.tmpl ghim " + dong, tmpl.contains("\n" + dong + "\n"));
        Kiem.dung("server.ini.tmpl ghim sv.dangNhapIpNguong / sv.dangNhapIpChoMs",
                tmpl.contains("\nsv.dangNhapIpNguong=10\n") && tmpl.contains("\nsv.dangNhapIpChoMs=3000\n"));
        Kiem.dung("ApDungMacDinh nghe sv.proxyTinCay (doi nong)", doc("src/com/htbb/cauhinh/ApDungMacDinh.java").contains("\"sv.proxyTinCay\""));
        Kiem.dung("TeamServer doc sv.proxyTinCay luc khoi dong", doc("src/server/TeamServer.java").contains("p.getProperty(\"sv.proxyTinCay\")"));
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
