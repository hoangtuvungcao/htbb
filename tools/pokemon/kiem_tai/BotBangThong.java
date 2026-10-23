import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.zip.Inflater;

/**
 * Do BANG THONG server -> client theo tung lenh (kiem lag 10/2026). N bot noi giao thuc that nhu client 0.9.14
 * (gui so ban + kha nang sau ma may), dung tren CUNG mot ban do (dat san map/x/y trong tob_char), di lai ngau
 * nhien, danh quai gan nhat, thinh thoang chat. Sau khi moi bot vao (cho -Dam giay) dem trong -Ddo giay:
 * moi lenh: so goi / giay / client, byte / giay / client (byte tren day, sau nen), va voi goi nen -127 thi dem
 * them lenh BEN TRONG (giai nen). In bang sap xep theo byte giam dan.
 *
 *   javac -d /tmp/bt tools/pokemon/kiem_tai/BotBangThong.java
 *   java -cp /tmp/bt -Dban=914 -Dkn=3 -Dam=20 -Ddo=60 BotBangThong 127.0.0.1 19129 100 lg
 *     args: host cong soBot tienTo ; tai khoan <tienTo>NNN / 123456 (-Dtu = so dau, mac dinh 1)
 *     -Dkn: kha nang (1 = giai nen, 2 = gom manh: PC/APK = 3, J2ME = 2); -Dchat=giay giua hai lan chat (0 = tat)
 *     -Dmoiip=true: moi 10 bot mot IP loopback 127.0.0.(2+so/10) (server chan tao > 10 nhan vat / IP)
 *     -Dlap=ms nhip di (mac dinh 500), -Ddanh=ms nhip danh (mac dinh 1500, 0 = khong danh)
 */
public class BotBangThong {
    static final int BAN = Integer.getInteger("ban", 914);
    static final int KN = Integer.getInteger("kn", 3);
    static final int CHAT_S = Integer.getInteger("chat", 60);
    static final int LAP_MS = Integer.getInteger("lap", 500);
    static final int DANH_MS = Integer.getInteger("danh", 1500);
    static volatile boolean dangDo = false, dung = false;
    static final ConcurrentHashMap<Integer, long[]> DEM = new ConcurrentHashMap<>();      // lenh tren day -> {goi, byte}
    static final ConcurrentHashMap<Integer, long[]> DEM_TRONG = new ConcurrentHashMap<>(); // lenh trong -127 -> {goi, byte goc}
    static final ConcurrentHashMap<Integer, long[]> DEM_VAO = new ConcurrentHashMap<>();  // dang nhap -> vao map: lenh (trong) -> {goi, byte}
    /** Tre phan hoi danh quai: gui lenh 9 -> goi 17 dau tien (ket qua don) cua chinh bot nay, ms. */
    static final List<Long> TRE_DANH = Collections.synchronizedList(new ArrayList<>());
    static final AtomicInteger vao = new AtomicInteger(), loi = new AtomicInteger();
    static final AtomicLong treVaoMs = new AtomicLong(), byteVao = new AtomicLong();
    static final ConcurrentHashMap<String, AtomicInteger> LOI = new ConcurrentHashMap<>();
    /** -Dinvao=true: in tung goi tu luc dang nhap toi khi vao map (do thoi gian vao game). */
    static final boolean IN_VAO = Boolean.getBoolean("invao");
    volatile long t0Vao = System.currentTimeMillis();
    static String HOST;
    static int PORT;

    final String user;
    final int so;
    byte[] key;
    int curR, curW;
    DataInputStream in;
    OutputStream out;
    Socket s;
    volatile int charId = -1;
    volatile boolean coMap = false, daVao = false;
    volatile long lucDanh = 0;
    final Map<Integer, int[]> quai = new ConcurrentHashMap<>();
    long byteNhan = 0;
    final Inflater inf = new Inflater();

    BotBangThong(String user, int so) {
        this.user = user;
        this.so = so;
    }

    byte rk(byte b) {
        if (key == null) return b;
        byte r = (byte) (key[curR] ^ b);
        curR = (curR + 1) % key.length;
        return r;
    }

    synchronized void gui(int cmd, byte[] d) throws IOException {
        byte[] o = new byte[3 + d.length];
        o[0] = (byte) cmd;
        o[1] = (byte) (d.length >> 8);
        o[2] = (byte) d.length;
        System.arraycopy(d, 0, o, 3, d.length);
        if (key != null) for (int i = 0; i < o.length; i++) { o[i] = (byte) (key[curW] ^ o[i]); curW = (curW + 1) % key.length; }
        out.write(o);
        out.flush();
    }

    static byte[] goi(Object... fs) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        for (Object f : fs) {
            if (f instanceof String) o.writeUTF((String) f);
            else if (f instanceof Byte) o.writeByte((Byte) f);
            else if (f instanceof Short) o.writeShort((Short) f);
            else if (f instanceof Integer) o.writeInt((Integer) f);
        }
        return bo.toByteArray();
    }

    static void cong(ConcurrentHashMap<Integer, long[]> m, int cmd, long b) {
        long[] a = m.computeIfAbsent(cmd, z -> new long[2]);
        synchronized (a) { a[0]++; a[1] += b; }
    }

    /** Doc mot khung tren day; tra {cmd, du lieu}. */
    Object[] doc() throws IOException {
        int cmd = rk(in.readByte());
        int size, dau;
        if (cmd == -128) {
            cmd = rk(in.readByte());
            size = ((rk(in.readByte()) & 255) << 24) | ((rk(in.readByte()) & 255) << 16) | ((rk(in.readByte()) & 255) << 8) | (rk(in.readByte()) & 255);
            dau = 6;
        } else {
            size = ((rk(in.readByte()) & 255) << 8) | (rk(in.readByte()) & 255);
            dau = 3;
        }
        byte[] d = new byte[size];
        in.readFully(d);
        for (int i = 0; i < d.length; i++) d[i] = rk(d[i]);
        byteNhan += size + dau;
        if (IN_VAO && !daVao) System.out.printf("  +%5d ms nhan lenh %d (%d B)%n", System.currentTimeMillis() - t0Vao, cmd, size + dau);
        if (dangDo) cong(DEM, cmd, size + dau);
        if (!daVao && cmd != -127) cong(DEM_VAO, cmd, size + dau);
        if (cmd == -127) {
            try {
                inf.reset();
                inf.setInput(d);
                ByteArrayOutputStream bo = new ByteArrayOutputStream(d.length * 4);
                byte[] t = new byte[8192];
                while (!inf.finished()) { int n = inf.inflate(t); if (n == 0 && inf.needsInput()) break; bo.write(t, 0, n); }
                byte[] k = bo.toByteArray();
                int ic, io;
                if (k[0] == -128) { ic = k[1]; io = 6; } else { ic = k[0]; io = 3; }
                if (dangDo) cong(DEM_TRONG, ic, k.length);
                if (!daVao) { cong(DEM_VAO, ic, k.length); cong(DEM_VAO, -127000, size + dau); }
                return new Object[]{ic, Arrays.copyOfRange(k, io, k.length)};
            } catch (Exception e) {
                return new Object[]{cmd, d};
            }
        }
        return new Object[]{cmd, d};
    }

    void xuLy(int cmd, byte[] d) {
        DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
        try {
            switch (cmd) {
                case -40: {
                    int n = r.readUnsignedByte();
                    byte[] k = new byte[n];
                    r.readFully(k);
                    for (int j = 0; j < n - 1; j++) k[j + 1] ^= k[j];
                    curR = curW = 0;
                    key = k;
                    break;
                }
                case 13: {
                    int n = d.length > 0 ? d[0] : 0;
                    if (n > 0) charId = new DataInputStream(new ByteArrayInputStream(d, 1, 4)).readInt();
                    break;
                }
                case 17:
                    if (lucDanh > 0 && dangDo) { TRE_DANH.add(System.currentTimeMillis() - lucDanh); lucDanh = 0; }
                    break;
                case 12:
                    if (!coMap && Boolean.getBoolean("inquai")) System.out.println(user + " map " + r.readShort() + " o " + r.readShort() + "," + r.readShort());
                    coMap = true;
                    break;
                case 7: { // thong tin quai (tra loi hoi lenh 7): id, ?, x, y, hp
                    int id = r.readShort();
                    r.readByte();
                    int qx = r.readShort(), qy = r.readShort();
                    quai.put(id, new int[]{qx, qy});
                    break;
                }
                case 4: {
                    int cat = r.readByte();
                    r.readByte();
                    int id = r.readShort();
                    int qx = r.readShort(), qy = r.readShort();
                    if (cat == 1) quai.put(id, new int[]{qx, qy});
                    break;
                }
                case 8: {
                    int cat = r.readByte();
                    int id = r.readShort();
                    if (cat == 1) quai.remove(id);
                    break;
                }
                default:
            }
        } catch (Exception ignored) {
        }
    }

    void cho(long ms, java.util.function.BooleanSupplier xong) throws InterruptedException {
        long het = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < het && !xong.getAsBoolean()) Thread.sleep(20);
    }

    void chay() {
        Random rd = new Random(user.hashCode());
        try {
            s = new Socket();
            s.setTcpNoDelay(true);
            // server chan tao qua 10 nhan vat / IP: -Dmoiip=1 -> moi 10 bot mot dia chi loopback 127.0.0.(2+so/10)
            if (Boolean.getBoolean("moiip")) s.bind(new java.net.InetSocketAddress("127.0.0." + (2 + (so - 1) / 10), 0));
            s.connect(new java.net.InetSocketAddress(HOST, PORT), 10000);
            in = new DataInputStream(new BufferedInputStream(s.getInputStream(), 65536));
            out = s.getOutputStream();
            gui(-40, new byte[0]);
            Object[] k = doc();
            xuLy((Integer) k[0], (byte[]) k[1]);
            Thread doc = new Thread(() -> {
                try {
                    while (!dung) { Object[] m = doc(); xuLy((Integer) m[0], (byte[]) m[1]); }
                } catch (Exception e) {
                    if (!dung) { loi.incrementAndGet(); LOI.computeIfAbsent("doc: " + e, z -> new AtomicInteger()).incrementAndGet(); }
                }
            }, "doc-" + user);
            doc.setDaemon(true);
            doc.start();
            long t0 = System.currentTimeMillis();
            t0Vao = t0;
            if (IN_VAO) System.out.println("  +    0 ms gui dang nhap (1)");
            gui(1, goi(user, "123456", "2.7.6", "nokia/1/1", "0", "0", "0", (byte) 4, (short) 800, (byte) 0, (byte) 0, "botbt-" + user, BAN, KN));
            cho(8000, () -> charId >= 0);
            if (charId < 0) {
                gui(14, goi(user, (byte) rd.nextInt(5), (byte) 0, (byte) 0, (byte) 0, (byte) 0));
                cho(8000, () -> charId >= 0);
            }
            if (charId < 0) { LOI.computeIfAbsent("khong vao duoc", z -> new AtomicInteger()).incrementAndGet(); return; }
            if (IN_VAO) System.out.printf("  +%5d ms gui chon nhan vat (13)%n", System.currentTimeMillis() - t0);
            gui(13, goi((byte) 0, charId));
            cho(20000, () -> coMap);
            if (!coMap) { LOI.computeIfAbsent("khong nhan map", z -> new AtomicInteger()).incrementAndGet(); return; }
            treVaoMs.addAndGet(System.currentTimeMillis() - t0);
            byteVao.addAndGet(byteNhan);
            gui(12, goi((short) -500, (short) 0, (short) 0));
            Thread.sleep(1500); // goi sau "vao map xong" (danh sach nguoi/quai, nhiem vu...) van tinh la luc vao
            daVao = true;
            vao.incrementAndGet();
            if (Boolean.getBoolean("inquai")) { for (int id = 0; id < 300; id++) { gui(7, goi((short) id)); if (id % 25 == 24) Thread.sleep(150); } Thread.sleep(3000); System.out.println(user + " quai: " + quai.size() + " " + quai.values().stream().limit(80).map(Arrays::toString).reduce("", String::concat)); }
            int x = Integer.getInteger("x", 400) + rd.nextInt(160) - 80, y = Integer.getInteger("y", 400) + rd.nextInt(160) - 80;
            long lanDanh = 0, lanChat = System.currentTimeMillis() + (CHAT_S > 0 ? rd.nextInt(CHAT_S * 1000) : 0);
            while (!dung) {
                x = Math.max(40, Math.min(1200, x + rd.nextInt(33) - 16));
                y = Math.max(40, Math.min(1200, y + rd.nextInt(33) - 16));
                gui(4, goi((short) x, (short) y));
                long now = System.currentTimeMillis();
                if (DANH_MS > 0 && now - lanDanh > DANH_MS && !quai.isEmpty()) {
                    int best = -1; long bd = Long.MAX_VALUE;
                    for (Map.Entry<Integer, int[]> e : quai.entrySet()) {
                        long dx = e.getValue()[0] - x, dy = e.getValue()[1] - y, dd = dx * dx + dy * dy;
                        if (dd < bd) { bd = dd; best = e.getKey(); }
                    }
                    if (best >= 0 && bd < 200 * 200) { lucDanh = System.currentTimeMillis(); gui(9, goi((short) best, (byte) 0)); }
                    lanDanh = now;
                }
                if (CHAT_S > 0 && now > lanChat) {
                    gui(27, goi("chao " + rd.nextInt(1000)));
                    lanChat = now + CHAT_S * 1000L;
                }
                Thread.sleep(LAP_MS + rd.nextInt(LAP_MS / 2 + 1));
            }
        } catch (Exception e) {
            if (!dung) { loi.incrementAndGet(); LOI.computeIfAbsent(e.toString(), z -> new AtomicInteger()).incrementAndGet(); }
        } finally {
            try { s.close(); } catch (Exception ignored) { }
        }
    }

    static void inBang(String tieuDe, Map<Integer, long[]> m, int n, double giay) {
        List<Map.Entry<Integer, long[]>> ds = new ArrayList<>(m.entrySet());
        ds.sort((a, b) -> Long.compare(b.getValue()[1], a.getValue()[1]));
        long tg = 0, tb = 0;
        for (Map.Entry<Integer, long[]> e : ds) { tg += e.getValue()[0]; tb += e.getValue()[1]; }
        System.out.printf("%s (moi client, trung binh %d client, %.0f s): %.1f goi/s, %.0f B/s%n", tieuDe, n, giay, tg / giay / n, tb / giay / n);
        System.out.printf("  %6s %10s %10s %8s %6s%n", "lenh", "goi/s", "B/s", "B/goi", "%B");
        for (Map.Entry<Integer, long[]> e : ds) {
            long[] v = e.getValue();
            if (v[1] * 1000 < tb) continue; // < 0.1 %
            System.out.printf("  %6d %10.2f %10.1f %8.0f %5.1f%%%n", e.getKey(), v[0] / giay / n, v[1] / giay / n, (double) v[1] / v[0], 100.0 * v[1] / tb);
        }
    }

    public static void main(String[] a) throws Exception {
        HOST = a[0];
        PORT = Integer.parseInt(a[1]);
        int n = Integer.parseInt(a[2]);
        String tien = a.length > 3 ? a[3] : "lg";
        int tu = Integer.getInteger("tu", 1);
        int am = Integer.getInteger("am", 20), doS = Integer.getInteger("do", 60);
        List<Thread> ts = new ArrayList<>();
        for (int i = tu; i < tu + n; i++) {
            final BotBangThong b = new BotBangThong(String.format("%s%03d", tien, i), i);
            Thread t = new Thread(b::chay, "bot-" + i);
            t.start();
            ts.add(t);
            Thread.sleep(Integer.getInteger("gian", 120));
        }
        long cho = System.currentTimeMillis() + 60000;
        while (vao.get() + LOI.values().stream().mapToInt(AtomicInteger::get).sum() < n && System.currentTimeMillis() < cho) Thread.sleep(200);
        int v = vao.get();
        System.out.printf("vao game %d/%d, tre vao trung binh %d ms, byte tu dang nhap toi vao map trung binh %d KB%n",
                v, n, v > 0 ? treVaoMs.get() / v : 0, v > 0 ? byteVao.get() / v / 1024 : 0);
        inBang("DANG NHAP -> VAO MAP (+1.5 s), lenh -127000 = byte tren day cua goi nen; cac lenh khac = byte goc", DEM_VAO, Math.max(1, v), 1.0);
        Thread.sleep(am * 1000L);
        long t0 = System.currentTimeMillis();
        dangDo = true;
        Thread.sleep(doS * 1000L);
        dangDo = false;
        double giay = (System.currentTimeMillis() - t0) / 1000.0;
        int con = Math.max(1, v - loi.get());
        inBang("TREN DAY", DEM, con, giay);
        if (!DEM_TRONG.isEmpty()) inBang("BEN TRONG GOI NEN -127", DEM_TRONG, con, giay);
        synchronized (TRE_DANH) {
            if (!TRE_DANH.isEmpty()) {
                List<Long> t = new ArrayList<>(TRE_DANH);
                Collections.sort(t);
                System.out.printf("tre danh (9 -> 17): n=%d p50=%d p90=%d p99=%d max=%d ms%n", t.size(), t.get(t.size() / 2), t.get(t.size() * 9 / 10), t.get(Math.min(t.size() - 1, t.size() * 99 / 100)), t.get(t.size() - 1));
            }
        }
        System.out.println("loi=" + loi.get());
        LOI.forEach((k, x) -> System.out.println("  loi x" + x + ": " + k));
        dung = true;
        System.exit(0);
    }
}
