package com.htbb.p2p;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Peerova P2P NHUNG TRONG game server (JNI, khong tien trinh phu) - nhanh p2p, ban thu Windows Server 2012.
 *
 * Bat bang server.ini:
 * <pre>
 *   sv.p2p=1                 bat (thieu / 0 = tat, server chay y nhu cu)
 *   sv.p2p_port=47000        cong UDP (+ TCP du phong cung so) cua Peerova - mo tren firewall
 *   sv.p2p_token=            (tuy chon) chi client co token nay moi vao duoc
 *   sv.p2p_license=PVA-...   (tuy chon) license Peerova; thieu = goi DEV toi da 3 CCU. KHONG BAO GIO in ra log.
 *   sv.p2p_key=p2p/server.key   danh tinh server (giu file nay = ticket khong doi khi khoi dong lai)
 *   sv.p2p_ticket_file=p2p/ticket.txt   noi ghi TICKET de phat cho client
 *   sv.p2p_upnp=1            tu mo cong tren router (UPnP / NAT-PMP)
 *   sv.p2p_rdv=1             diem hen qua website Peerova (server sau NAT khong mo cong); 0 = tat
 *   sv.p2p_relay=1           duong du phong ma hoa qua website khi khong noi thang duoc; 0 = tat (chi P2P thang)
 *   sv.p2p_private=0         1 = ticket khong chua IP (can sv.p2p_rdv=1)
 *   sv.p2p_max_ccu=          (tuy chon) gioi han CCU thap hon goi license
 *   sv.p2p_lib=              (tuy chon) duong dan tuyet doi toi peerova_jni.dll / libpeerova_jni.so
 * </pre>
 *
 * Cach chay: Peerova expose dung cong game (sv.port) -> moi ket noi P2P la mot ket noi TCP loopback toi chinh
 * cong game, nen giao thuc game / Session khong doi. Client J2ME (khong nap duoc thu vien native) van vao thang
 * cong game nhu cu. Moi nguoi choi P2P den tu dia chi rieng 127.a.b.c (Peerova tunnel::player_ip, on dinh theo khoa
 * client) nen chong DoS / chan IP / nhat ky van theo tung nguoi choi.
 */
public final class MayChuP2P {
    private MayChuP2P() {
    }

    private static volatile dev.peerova.PeerovaTunnel tunnel;
    private static volatile String ticket = "";
    private static volatile String trangThai = "tắt";
    private static volatile String tepTicket = "";

    public static boolean dangChay() {
        final dev.peerova.PeerovaTunnel t = tunnel;
        return t != null && t.state() == dev.peerova.PeerovaTunnel.UP;
    }

    public static String ticket() {
        return ticket;
    }

    public static String trangThai() {
        return trangThai;
    }

    public static String tepTicket() {
        return tepTicket;
    }

    public static long soNguoi() {
        final dev.peerova.PeerovaTunnel t = tunnel;
        return t == null ? 0 : t.peerCount();
    }

    static boolean bat(final Properties p) {
        return "1".equals(p.getProperty("sv.p2p", "0").trim());
    }

    private static String g(final Properties p, final String k, final String md) {
        final String v = p.getProperty(k);
        return v == null || v.trim().isEmpty() ? md : v.trim();
    }

    /** Chuoi JSON (escape toi thieu cho gia tri chuoi). */
    static String js(final String s) {
        final StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                b.append('\\').append(c);
            } else if (c < 0x20) {
                b.append(String.format("\\u%04x", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.append('"').toString();
    }

    /** Cau hinh JSON cho peerova_tunnel_server (khong log chuoi nay: co the chua license / token). */
    static String cauHinh(final Properties p, final int congGame) {
        final StringBuilder j = new StringBuilder("{");
        j.append("\"port\":").append(Integer.parseInt(g(p, "sv.p2p_port", "47000")));
        j.append(",\"key_file\":").append(js(g(p, "sv.p2p_key", "p2p/server.key")));
        j.append(",\"upnp\":").append(!"0".equals(g(p, "sv.p2p_upnp", "1")));
        j.append(",\"private\":").append("1".equals(g(p, "sv.p2p_private", "0")));
        // Peerova moi: diem hen qua website va duong du phong ma hoa (relay) BAT mac dinh; 0 = tat.
        if ("0".equals(g(p, "sv.p2p_rdv", "1"))) {
            j.append(",\"rendezvous\":false");
        }
        if ("0".equals(g(p, "sv.p2p_relay", "1"))) {
            j.append(",\"relay\":false");
        }
        final String token = g(p, "sv.p2p_token", "");
        if (!token.isEmpty()) {
            j.append(",\"token\":").append(js(token));
        }
        final String lic = g(p, "sv.p2p_license", "");
        if (!lic.isEmpty()) {
            j.append(",\"license\":").append(js(lic));
        }
        final String max = g(p, "sv.p2p_max_ccu", "");
        if (!max.isEmpty()) {
            j.append(",\"max_ccu\":").append(Integer.parseInt(max));
        }
        j.append(",\"expose\":[").append(js("tcp:" + congGame + "=127.0.0.1:" + congGame)).append("]}");
        return j.toString();
    }

    /** Tim thu vien JNI: sv.p2p_lib > p2p/<ten> canh thu muc chay > java.library.path. */
    static void datDuongDanThuVien(final Properties p) {
        if (System.getProperty("peerova.jni.path") != null) {
            return;
        }
        final String ro = g(p, "sv.p2p_lib", "");
        File f = ro.isEmpty() ? new File("p2p", System.mapLibraryName("peerova_jni")) : new File(ro);
        if (f.isFile()) {
            System.setProperty("peerova.jni.path", f.getAbsolutePath());
        }
    }

    /**
     * Khoi dong Peerova (chan toi ~5 s cho ticket) neu sv.p2p=1. Loi KHONG lam sap game server: J2ME / ket noi
     * thang van chay, chi in loi ro rang.
     */
    public static synchronized void khoiDong(final Properties p, final int congGame) {
        if (!bat(p) || tunnel != null) {
            return;
        }
        trangThai = "đang khởi động";
        try {
            datDuongDanThuVien(p);
            System.out.println("[P2P] Peerova " + dev.peerova.Peerova.version() + " (JNI, java "
                    + System.getProperty("java.version") + " " + System.getProperty("os.arch") + ", "
                    + System.getProperty("os.name") + ")");
            if (g(p, "sv.p2p_license", "").isEmpty()) {
                System.out.println("[P2P] CANH BAO: chua dat sv.p2p_license -> Peerova chay goi DEV: TOI DA 3 nguoi choi P2P"
                        + " cung luc (nguoi thu 4 bi tu choi). Dat license PVA-... trong server.ini de mo gioi han.");
            } else {
                System.out.println("[P2P] co license Peerova (sv.p2p_license, khong in ra) - lease kiem qua https://peerova.pages.dev");
            }
            final String cfg = cauHinh(p, congGame);
            final File key = new File(g(p, "sv.p2p_key", "p2p/server.key")).getAbsoluteFile();
            if (key.getParentFile() != null) {
                key.getParentFile().mkdirs();
            }
            final dev.peerova.PeerovaTunnel t = dev.peerova.PeerovaTunnel.server(cfg);
            tunnel = t;
            ticket = t.ticket();
            trangThai = "đang chạy (UDP/TCP " + g(p, "sv.p2p_port", "47000") + " -> cổng game " + congGame + ")";
            final File tep = new File(g(p, "sv.p2p_ticket_file", "p2p/ticket.txt")).getAbsoluteFile();
            if (tep.getParentFile() != null) {
                tep.getParentFile().mkdirs();
            }
            try (Writer w = new OutputStreamWriter(new FileOutputStream(tep), StandardCharsets.UTF_8)) {
                w.write(ticket + "\n");
            }
            tepTicket = tep.getPath();
            System.out.println("[P2P] Peerova SAN SANG: cong game " + congGame + " expose qua P2P. TICKET (phat cho client"
                    + " PC / Android / Unity; J2ME van vao thang cong " + congGame + "):");
            System.out.println("[P2P] TICKET " + ticket);
            System.out.println("[P2P] da ghi ticket vao " + tepTicket);
            final Thread log = new Thread(() -> {
                while (tunnel == t && !t.isClosed()) {
                    String l;
                    while ((l = t.nextLog()) != null) {
                        System.out.println("[P2P] " + l);
                    }
                    if (t.state() == dev.peerova.PeerovaTunnel.DOWN) {
                        System.out.println("[P2P] Peerova DA DUNG: " + t.error());
                        trangThai = "đã dừng: " + t.error();
                        return;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }, "p2p-log");
            log.setDaemon(true);
            log.start();
        } catch (Throwable e) {
            trangThai = "lỗi: " + e;
            System.out.println("[P2P] LOI: khong khoi dong duoc Peerova: " + e
                    + " - client P2P se khong vao duoc; J2ME / ket noi thang cong " + congGame + " van chay."
                    + " Kiem p2p/" + System.mapLibraryName("peerova_jni") + " (64-bit, cung bitness voi Java),"
                    + " cong UDP sv.p2p_port, sv.p2p_license.");
        }
    }

    /** Khoi dong o luong rieng (khong lam cham vong accept cong game). */
    public static void khoiDongNen(final Properties p, final int congGame) {
        if (!bat(p)) {
            return;
        }
        final Thread th = new Thread(() -> khoiDong(p, congGame), "p2p-start");
        th.setDaemon(true);
        th.start();
    }

    public static synchronized void dung() {
        final dev.peerova.PeerovaTunnel t = tunnel;
        tunnel = null;
        if (t != null) {
            t.close();
        }
    }
}
