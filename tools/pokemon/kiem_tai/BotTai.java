import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Test tai / on dinh cho server Huyen Thoai Bao Boi: N bot noi giao thuc that.
 *
 * Moi bot (tai khoan <tienTo>NNN / 123456): khoa -40 -> dang nhap (1) -> tao nhan vat neu chua
 * co (14, vung 0/1 vi vung 2 da dong) -> vao game (13) -> choi den het gio:
 *   - bot THUONG: di lai quanh lang + chat.
 *   - bot GM (so thu tu <= -Dgm=N, ten nhan vat phai nam trong sv.adminAccounts va pInfo[16]=1):
 *     "slv <ten> 25 0" -> "mto 2 20 20" -> hoi danh sach quai (lenh 7) -> dich chuyen toi tung con,
 *     danh (lenh 9) va hoi lai mau (lenh 7) sau moi don den khi quai chet.
 * In thong ke moi 10 giay (bot dang choi, goi tin, don danh, quai ha, RAM/CPU server).
 *
 * Bien dich + chay (o thu muc KPAH/):
 *   javac -d build/kiem_tai tools/pokemon/kiem_tai/BotTai.java
 *   java -cp build/kiem_tai -Dgm=10 -Dtu=1 BotTai 127.0.0.1 19129 50 600 tai
 *     args: host cong soBot giay [tienTo=tai] [lop=-1 ngau nhien]; -Dtu = so thu tu bot dau tien
 * Tao tai khoan: xem tools/pokemon/kiem_tai/README.md.
 */
public class BotTai {
    static final AtomicLong goiNhan = new AtomicLong(), byteNhan = new AtomicLong(), goiGui = new AtomicLong();
    static final AtomicLong danh = new AtomicLong(), quaiHa = new AtomicLong(), chat = new AtomicLong();
    static final AtomicInteger dangChoi = new AtomicInteger(), loiKetNoi = new AtomicInteger(), vaoGame = new AtomicInteger();
    static final AtomicInteger thatBaiVao = new AtomicInteger(), pokeThan = new AtomicInteger(), tienHoa = new AtomicInteger();
    static final ConcurrentHashMap<String, AtomicInteger> loi = new ConcurrentHashMap<>();
    static volatile boolean dung = false;
    static String HOST;
    static int PORT;
    static final int SO_GM = Integer.getInteger("gm", 0);

    final String user;
    final int lop;
    final boolean gm;
    byte[] key;
    int curR, curW;
    DataInputStream in;
    OutputStream out;
    Socket s;
    int charId = -1;
    /** id quai -> {x, y, hp} (hp = -1 khi chua biet). */
    final Map<Integer, int[]> quai = new HashMap<>();
    int x = -1, y = -1;

    BotTai(String user, int lop, boolean gm) {
        this.user = user;
        this.lop = lop;
        this.gm = gm;
    }

    byte rk(byte b) {
        if (key == null) return b;
        byte r = (byte) (key[curR] ^ b);
        curR = (curR + 1) % key.length;
        return r;
    }

    byte wk(byte b) {
        if (key == null) return b;
        byte r = (byte) (key[curW] ^ b);
        curW = (curW + 1) % key.length;
        return r;
    }

    void gui(int cmd, byte[] d) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        bo.write(wk((byte) cmd));
        bo.write(wk((byte) (d.length >> 8)));
        bo.write(wk((byte) d.length));
        for (byte b : d) bo.write(wk(b));
        out.write(bo.toByteArray());
        out.flush();
        goiGui.incrementAndGet();
    }

    Object[] doc() throws IOException {
        int cmd = rk(in.readByte());
        int size;
        if (key != null && cmd == -128) {
            cmd = rk(in.readByte());
            byte[] a = new byte[4];
            in.readFully(a);
            size = ((rk(a[0]) & 255) << 24) | ((rk(a[1]) & 255) << 16) | ((rk(a[2]) & 255) << 8) | (rk(a[3]) & 255);
        } else {
            int a1 = rk(in.readByte()) & 255, a2 = rk(in.readByte()) & 255;
            size = (a1 << 8) | a2;
        }
        byte[] d = new byte[size];
        in.readFully(d);
        for (int i = 0; i < d.length; i++) d[i] = rk(d[i]);
        goiNhan.incrementAndGet();
        byteNhan.addAndGet(size + 3);
        return new Object[]{cmd, d};
    }

    void xuLy(int cmd, byte[] d) throws IOException {
        DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
        try {
            switch (cmd) {
                case -40: {
                    int n = r.readUnsignedByte();
                    key = new byte[n];
                    r.readFully(key);
                    for (int j = 0; j < n - 1; j++) key[j + 1] ^= key[j];
                    curR = curW = 0;
                    break;
                }
                case 13: {
                    int n = d.length > 0 ? d[0] : 0;
                    if (n > 0) charId = new DataInputStream(new ByteArrayInputStream(d, 1, 4)).readInt();
                    break;
                }
                case 4: { // vi tri actor; cat 1 = quai
                    int cat = r.readByte();
                    r.readByte();
                    int id = r.readShort();
                    int qx = r.readShort(), qy = r.readShort();
                    if (cat == 1) {
                        int[] q = quai.get(id);
                        if (q == null) quai.put(id, new int[]{qx, qy, -1});
                        else { q[0] = qx; q[1] = qy; }
                    }
                    break;
                }
                case 7: { // thong tin quai: id, cat?, x, y, hp
                    int id = r.readShort();
                    r.readByte();
                    int qx = r.readShort(), qy = r.readShort();
                    int hp = r.readInt();
                    quai.put(id, new int[]{qx, qy, hp});
                    break;
                }
                case 37: {
                    String t = r.readUTF().replace('\n', ' ');
                    if (charId < 0) loi.computeIfAbsent("thong bao luc vao: " + t, z -> new AtomicInteger()).incrementAndGet();
                    break;
                }
                case -90:
                    pokeThan.incrementAndGet();
                    break;
                case -92:
                    tienHoa.incrementAndGet();
                    break;
                default:
            }
        } catch (EOFException ignored) {
            // goi ngan hon dinh dang du doan: bo qua
        }
    }

    void nhanTrong(long ms) throws Exception {
        long het = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < het && !dung) {
            if (in.available() == 0) {
                Thread.sleep(15);
                continue;
            }
            Object[] m = doc();
            xuLy((Integer) m[0], (byte[]) m[1]);
        }
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

    void hoiQuai(int tu, int den) throws Exception {
        for (int id = tu; id < den; id++) {
            gui(7, goi((short) id));
            if (id % 25 == 24) nhanTrong(150);
        }
        nhanTrong(600);
    }

    void choiGm(long hetGio) throws Exception {
        gui(27, goi("slv " + user + " 25 0"));
        nhanTrong(1500);
        gui(27, goi("mto 2 20 20"));
        nhanTrong(2500);
        quai.clear();
        hoiQuai(0, 400);
        Set<Integer> boQua = new HashSet<>();
        while (System.currentTimeMillis() < hetGio && !dung) {
            Integer muc = null;
            for (Map.Entry<Integer, int[]> e : quai.entrySet()) {
                if (e.getValue()[2] > 0 && !boQua.contains(e.getKey())) { muc = e.getKey(); break; }
            }
            if (muc == null) { // het quai song: doi hoi sinh roi hoi lai
                boQua.clear();
                nhanTrong(3000);
                hoiQuai(0, 400);
                continue;
            }
            int[] q = quai.get(muc);
            gui(27, goi("mto 2 " + (q[0] / 16) + " " + (q[1] / 16)));
            nhanTrong(600);
            boolean chet = false;
            for (int lan = 0; lan < 30 && !dung; lan++) {
                gui(9, goi((short) (int) muc, (byte) 0));
                danh.incrementAndGet();
                nhanTrong(350);
                gui(7, goi((short) (int) muc));
                nhanTrong(250);
                int[] q2 = quai.get(muc);
                if (q2 != null && q2[2] <= 0) { chet = true; break; }
            }
            if (chet) quaiHa.incrementAndGet();
            boQua.add(muc);
        }
    }

    void choiThuong(long hetGio, Random rd) throws Exception {
        long chatLan = System.currentTimeMillis() + 5000 + rd.nextInt(10000);
        x = 300 + rd.nextInt(200);
        y = 300 + rd.nextInt(200);
        while (System.currentTimeMillis() < hetGio && !dung) {
            x = Math.max(40, Math.min(900, x + rd.nextInt(49) - 24));
            y = Math.max(40, Math.min(900, y + rd.nextInt(49) - 24));
            gui(4, goi((short) x, (short) y));
            nhanTrong(400 + rd.nextInt(400));
            if (System.currentTimeMillis() > chatLan) {
                gui(27, goi("xin chào từ " + user));
                chat.incrementAndGet();
                chatLan = System.currentTimeMillis() + 15000 + rd.nextInt(10000);
            }
        }
    }

    void chay(long hetGio) {
        Random rd = new Random(user.hashCode());
        boolean daVao = false;
        try {
            s = new Socket();
            s.connect(new java.net.InetSocketAddress(HOST, PORT), 10000);
            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            out = s.getOutputStream();
            gui(-40, new byte[0]);
            Object[] k = doc();
            xuLy((Integer) k[0], (byte[]) k[1]);
            gui(1, goi(user, "123456", "2.7.6", "nokia/1/1", "0", "0", "0", (byte) 4, (short) 800, (byte) 0, (byte) 0, "bottai-" + user));
            nhanTrong(3000);
            if (charId < 0) {
                int l = lop >= 0 ? lop : rd.nextInt(5);
                gui(14, goi(user, (byte) l, (byte) 0, (byte) 0, (byte) rd.nextInt(2), (byte) 0));
                nhanTrong(4000);
            }
            if (charId < 0) {
                thatBaiVao.incrementAndGet();
                loi.computeIfAbsent("khong vao duoc (dang nhap/tao)", z -> new AtomicInteger()).incrementAndGet();
                return;
            }
            gui(13, goi((byte) 0, charId));
            nhanTrong(3000);
            vaoGame.incrementAndGet();
            dangChoi.incrementAndGet();
            daVao = true;
            if (gm) choiGm(hetGio);
            else choiThuong(hetGio, rd);
        } catch (Exception e) {
            if (!dung) {
                loiKetNoi.incrementAndGet();
                loi.computeIfAbsent(e.getClass().getSimpleName() + ": " + e.getMessage(), z -> new AtomicInteger()).incrementAndGet();
            }
        } finally {
            if (daVao) dangChoi.decrementAndGet();
            try { s.close(); } catch (Exception ignored) { }
        }
    }

    static String tienTrinhServer() {
        try {
            Process p = new ProcessBuilder("sh", "-c",
                    "for p in $(pgrep -f '^java .*server.TeamServer'); do ps -o rss=,pcpu= -p $p; done").start();
            String o = new String(p.getInputStream().readAllBytes()).trim();
            p.waitFor();
            if (o.isEmpty()) return "server: KHONG CHAY";
            String[] f = o.split("\\s+");
            return String.format("server RSS=%dMB CPU=%s%%", Long.parseLong(f[0]) / 1024, f[1]);
        } catch (Exception e) {
            return "server: ?";
        }
    }

    public static void main(String[] a) throws Exception {
        HOST = a[0];
        PORT = Integer.parseInt(a[1]);
        int n = Integer.parseInt(a[2]);
        int giay = Integer.parseInt(a[3]);
        String tien = a.length > 4 ? a[4] : "tai";
        int lop = a.length > 5 ? Integer.parseInt(a[5]) : -1;
        int tu = Integer.getInteger("tu", 1);
        long het = System.currentTimeMillis() + giay * 1000L;
        ExecutorService ex = Executors.newFixedThreadPool(n);
        for (int i = tu; i < tu + n; i++) {
            final String u = String.format("%s%03d", tien, i);
            final boolean laGm = i <= SO_GM;
            ex.submit(() -> new BotTai(u, lop, laGm).chay(het));
            Thread.sleep(150);
        }
        long batDau = System.currentTimeMillis();
        while (System.currentTimeMillis() < het + 3000) {
            Thread.sleep(10000);
            System.out.printf("[%4ds] dang choi=%d/%d vao=%d loi ket noi=%d | nhan=%d (%dKB) gui=%d | danh=%d quai ha=%d tien hoa=%d chat=%d | %s%n",
                    (System.currentTimeMillis() - batDau) / 1000, dangChoi.get(), n, vaoGame.get(), loiKetNoi.get(),
                    goiNhan.get(), byteNhan.get() / 1024, goiGui.get(), danh.get(), quaiHa.get(), tienHoa.get(), chat.get(),
                    tienTrinhServer());
        }
        dung = true;
        ex.shutdownNow();
        ex.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("=== KET QUA === bot=" + n + " vao game=" + vaoGame.get() + " that bai vao=" + thatBaiVao.get()
                + " rot ket noi=" + loiKetNoi.get() + " don danh=" + danh.get() + " quai ha=" + quaiHa.get()
                + " tien hoa=" + tienHoa.get());
        loi.forEach((k, v) -> System.out.println("  loi x" + v + ": " + k));
    }
}
