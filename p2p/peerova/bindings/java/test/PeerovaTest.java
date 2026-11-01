import dev.peerova.Peerova;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PeerovaTest {
    static void check(boolean ok, String what) {
        if (!ok) { System.out.println("FAIL: " + what); System.exit(1); }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("peerova native " + Peerova.version());
        File key = File.createTempFile("peerova-java", ".key");
        key.delete();
        String cfg = "{\"port\":0,\"upnp\":false,\"key_file\":\"" + key.getAbsolutePath().replace("\\", "\\\\") + "\"}";
        try (Peerova.Server srv = Peerova.Server.start(cfg); Peerova.Client cli = Peerova.Client.connect(srv.ticket())) {
            check(srv.ticket().startsWith("pvt_"), "ticket");
            check(srv.roomCode() == null, "connect code without app/share config");
            long peer = -1;
            long end = System.currentTimeMillis() + 10000;
            while (peer < 0 && System.currentTimeMillis() < end) {
                Peerova.Event ev = cli.poll(500);
                if (ev == null) continue;
                check(ev.type != Peerova.CONNECT_FAILED, "connect failed: " + ev.text());
                if (ev.type == Peerova.CONNECTED) peer = ev.peer;
            }
            check(peer >= 0, "client did not connect");

            final int n = 100;
            long t0 = System.nanoTime();
            for (int i = 0; i < n; i++) cli.send(peer, "msg " + i, 1, true);
            cli.send(peer, "realtime".getBytes(StandardCharsets.UTF_8), 2, false);
            ByteBuffer direct = ByteBuffer.allocateDirect(16);
            direct.put("direct".getBytes(StandardCharsets.UTF_8)).flip();
            cli.send(peer, direct, 3, true);

            int got = 0; boolean unreliable = false, gotDirect = false; long sp = -1;
            end = System.currentTimeMillis() + 5000;
            while ((got < n || !unreliable || !gotDirect) && System.currentTimeMillis() < end) {
                Peerova.Event e = srv.poll(200);
                if (e == null) continue;
                if (e.type == Peerova.CONNECTED) sp = e.peer;
                else if (e.type == Peerova.MESSAGE && e.channel == 1) {
                    check(e.reliable && e.text().equals("msg " + got), "out of order at " + got + ": " + e.text());
                    got++;
                } else if (e.type == Peerova.MESSAGE && e.channel == 2) unreliable = !e.reliable && e.text().equals("realtime");
                else if (e.type == Peerova.MESSAGE && e.channel == 3) gotDirect = e.text().equals("direct");
            }
            check(got == n, "got " + got + "/" + n + " reliable msgs");
            check(unreliable, "unreliable datagram not received on loopback");
            check(gotDirect, "direct ByteBuffer msg not received");
            check(sp >= 0, "server never saw CONNECTED");

            srv.send(sp, "pong", 0, true);
            Peerova.Event r = null;
            end = System.currentTimeMillis() + 5000;
            while (r == null && System.currentTimeMillis() < end) {
                Peerova.Event e = cli.poll(200);
                if (e != null && e.type == Peerova.MESSAGE) r = e;
            }
            check(r != null && r.text().equals("pong"), "no pong");
            boolean rejected = !cli.trySend(peer, new byte[Peerova.MAX_UNRELIABLE_PAYLOAD + 100], 0, Peerova.MAX_UNRELIABLE_PAYLOAD + 100, 4, false);
            System.out.println("oversized unreliable send rejected: " + rejected);
            System.out.printf("server peers=%d, %d reliable in order + 1 unreliable + direct + pong in %.1f ms%n",
                    srv.peerCount(), n, (System.nanoTime() - t0) / 1e6);
            cli.close();
            try { cli.send(peer, "x", 0, true); check(false, "send after close did not throw"); }
            catch (IllegalStateException expected) { }
        } finally {
            key.delete();
        }
        System.out.println("PASS");
    }
}
