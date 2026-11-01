// How to run (from bindings/java, after ./build.sh build):
//   javac -cp build/peerova.jar -d build/examples examples/Chat.java
//   java -Djava.library.path=build -cp build/peerova.jar:build/examples Chat server [port] [key_file]
//   java -Djava.library.path=build -cp build/peerova.jar:build/examples Chat client <TICKET or CONNECT CODE>
//
// Tiny chat + position-sync demo. The server prints its ticket, forwards chat lines (reliable, channel 0) and
// positions (unreliable, channel 1) to the other clients. The client sends its position 20 times per second
// and every line typed on stdin as a chat message; "/quit" or end of input disconnects cleanly.
import dev.peerova.Peerova;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Chat {
    static final int CH_CHAT = 0;      // reliable: ordered, never lost
    static final int CH_POSITION = 1;  // unreliable: latest state wins, may be dropped

    public static void main(String[] args) throws Exception {
        if (args.length >= 1 && args[0].equals("server")) {
            server(args.length > 1 ? Integer.parseInt(args[1]) : 47000, args.length > 2 ? args[2] : "server.key");
        } else if (args.length >= 2 && args[0].equals("client")) {
            client(args[1]);
        } else {
            System.err.println("usage: Chat server [port] [key_file] | Chat client <TICKET or CODE>");
            System.exit(2);
        }
    }

    static String json(String s) { return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }

    static void server(int port, String keyFile) throws Exception {
        // Ctrl+C / SIGTERM: ask the poll loop to stop, then wait until it has closed the node.
        final CountDownLatch closed = new CountDownLatch(1);
        final boolean[] stop = {false};
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (stop) { stop[0] = true; }
            try { closed.await(); } catch (InterruptedException ignored) { }
        }));

        try (Peerova.Server srv = Peerova.Server.start("{\"port\":" + port + ",\"key_file\":" + json(keyFile) + "}")) {
            System.out.println("ticket: " + srv.ticket());
            Set<Long> peers = new LinkedHashSet<>();
            boolean codeShown = false;
            int positions = 0;
            while (true) {
                synchronized (stop) { if (stop[0]) break; }
                if (!codeShown && srv.roomCode() != null) {   // only with "app" + "share" in the config
                    System.out.println("connect code: " + srv.roomCode());
                    codeShown = true;
                }
                Peerova.Event ev = srv.poll(100);
                if (ev == null) continue;
                switch (ev.type) {
                    case Peerova.CONNECTED:
                        peers.add(ev.peer);
                        System.out.println("peer " + ev.peer + " joined (" + srv.peerCount() + " online)");
                        break;
                    case Peerova.DISCONNECTED:
                        peers.remove(ev.peer);
                        System.out.println("peer " + ev.peer + " left: " + ev.text());
                        break;
                    case Peerova.MESSAGE:
                        if (ev.channel == CH_CHAT) {
                            String line = "peer " + ev.peer + ": " + ev.text();
                            System.out.println(line);
                            byte[] out = line.getBytes(StandardCharsets.UTF_8);
                            for (long p : peers) if (p != ev.peer) srv.trySend(p, out, 0, out.length, CH_CHAT, true);
                        } else if (ev.channel == CH_POSITION) {
                            if (++positions % 20 == 0) {
                                ByteBuffer b = ByteBuffer.wrap(ev.data);
                                System.out.printf("peer %d position %.1f,%.1f (%d updates)%n", ev.peer, b.getFloat(), b.getFloat(), positions);
                            }
                            for (long p : peers) if (p != ev.peer) srv.trySend(p, ev.data, 0, ev.data.length, CH_POSITION, false);
                        }
                        break;
                    default:
                        break;
                }
            }
        } finally {
            System.out.println("server stopped");
            closed.countDown();
        }
    }

    static void client(String ticketOrCode) throws Exception {
        // stdin is read on its own thread; the main thread does all networking (poll + send).
        final ConcurrentLinkedQueue<String> typed = new ConcurrentLinkedQueue<>();
        Thread reader = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                for (String l; (l = in.readLine()) != null; ) typed.add(l);
            } catch (Exception ignored) { }
            typed.add("/quit");
        }, "stdin");
        reader.setDaemon(true);

        try (Peerova.Client cli = Peerova.Client.connect(ticketOrCode, "{\"timeout_ms\":20000}")) {
            long server = -1;
            while (server < 0) {
                Peerova.Event ev = cli.poll(500);
                if (ev == null) continue;
                if (ev.type == Peerova.CONNECT_FAILED) { System.err.println("connect failed: " + ev.text()); System.exit(1); }
                if (ev.type == Peerova.CONNECTED) server = ev.peer;
            }
            System.out.println("connected, type chat lines (/quit to leave)");
            reader.start();

            long nextPos = 0, sent = 0;
            boolean running = true;
            while (running) {
                long now = System.currentTimeMillis();
                if (now >= nextPos) {                               // ~20 Hz position update
                    double t = now / 1000.0;
                    byte[] pos = ByteBuffer.allocate(8).putFloat((float) Math.cos(t) * 10).putFloat((float) Math.sin(t) * 10).array();
                    cli.send(server, pos, CH_POSITION, false);
                    sent++;
                    nextPos = now + 50;
                }
                for (String line; (line = typed.poll()) != null; ) {
                    if (line.equals("/quit")) { running = false; break; }
                    cli.send(server, line, CH_CHAT, true);
                }
                Peerova.Event ev = cli.poll(10);
                if (ev == null) continue;
                if (ev.type == Peerova.MESSAGE && ev.channel == CH_CHAT) System.out.println(ev.text());
                else if (ev.type == Peerova.DISCONNECTED) { System.out.println("server closed: " + ev.text()); running = false; }
            }
            // Give queued reliable messages a moment to reach the server before disconnecting.
            for (long end = System.currentTimeMillis() + 300; System.currentTimeMillis() < end; ) cli.poll(50);
            cli.disconnect(server);
            System.out.println("bye (" + sent + " position updates sent)");
        }
    }
}
