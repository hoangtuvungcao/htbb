import dev.peerova.PeerovaTunnel;

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/** Echo service exposed through PeerovaTunnel; client connects to the forwarded local port; server maps the socket to the peer. */
public class PeerovaTunnelTest {
    static void check(boolean ok, String what) {
        if (!ok) { System.out.println("FAIL: " + what); System.exit(1); }
    }

    public static void main(String[] args) throws Exception {
        final ServerSocket svc = new ServerSocket(0);
        final String[] peer = new String[1];
        // -Dkey=<path>: File.createTempFile crashes the JVM under wine (NetworkInterface seed), so allow a fixed path.
        File key = System.getProperty("key") != null ? new File(System.getProperty("key")) : File.createTempFile("peerova-tun", ".key");
        key.delete();
        final PeerovaTunnel srv = PeerovaTunnel.server("{\"port\":0,\"upnp\":false,\"auto_supernode\":false,\"key_file\":\""
                + key.getAbsolutePath().replace("\\", "\\\\") + "\",\"expose\":[\"tcp:19129=127.0.0.1:" + svc.getLocalPort() + "\"]}");
        Thread t = new Thread(() -> {
            try {
                Socket s = svc.accept();
                peer[0] = s.getInetAddress().getHostAddress(); // player address as seen by the service (127.a.b.c)
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) > 0) out.write(b, 0, n);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
        String ticket = srv.ticket();
        check(ticket != null && ticket.startsWith("pvt_"), "ticket " + ticket);
        try (PeerovaTunnel cli = PeerovaTunnel.client(ticket, "{\"forward\":[\"tcp:0=19129\"]}")) {
            int lp = cli.localPort(0);
            check(lp > 0, "local port");
            check(cli.await(15000) == PeerovaTunnel.UP, "tunnel up: " + cli.error());
            Socket s = new Socket("127.0.0.1", lp);
            byte[] msg = new byte[100000];
            for (int i = 0; i < msg.length; i++) msg[i] = (byte) i;
            s.getOutputStream().write(msg);
            byte[] got = new byte[msg.length];
            new DataInputStream(s.getInputStream()).readFully(got);
            check(java.util.Arrays.equals(msg, got), "echo");
            Thread.sleep(200);
            check(peer[0] != null, "peerOf");
            System.out.println("echo 100000 byte OK qua tunnel, peer=" + peer[0] + ", peers=" + srv.peerCount());
            String l;
            while ((l = cli.nextLog()) != null) System.out.println("  cli: " + l);
            s.close();
        }
        srv.close();
        System.out.println("PASS PeerovaTunnelTest");
    }
}
