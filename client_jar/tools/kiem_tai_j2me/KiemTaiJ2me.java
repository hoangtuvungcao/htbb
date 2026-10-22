import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * KIỂM TẢI J2ME THẬT (0.9.7.1, sự cố "kẹt tải dữ liệu" trên giả lập iOS): chạy ĐÚNG tệp jar J2ME trong MicroEmulator
 * (lớp MIDP/CLDC thật của giả lập, không phải lớp PC), heap nhỏ (-Xmx của JVM này), RMS bộ nhớ có GIỚI HẠN (tổng dung
 * lượng + cỡ một bản ghi, như máy thật / giả lập), cache rỗng, và một proxy TCP chia dữ liệu máy chủ -> máy thành
 * mảnh nhỏ ngẫu nhiên (đọc socket từng phần). Tự đăng nhập, tạo Pokémon nếu tài khoản trống, chọn Pokémon, chờ vào thế
 * giới. Thoát 0 = vào được bản đồ; 1 = kẹt / lỗi (in trạng thái màn tải, lỗi OOM / ngoại lệ đã thấy).
 *
 *   javac -cp microemulator.jar -d $O KiemTaiJ2me.java
 *   java -Xmx16m -Djava.awt.headless=true -Dw=240 -Dh=320 -Dmanh=512 -Drms=65536 -DrmsBanGhi=16384 \
 *        -cp microemulator.jar:$O KiemTaiJ2me <jar> <cổng server> <tài khoản (mk 123456)> [giây chờ]
 *
 * Biến: w/h cỡ màn, manh = cỡ mảnh tối đa proxy gửi (byte, 0 = không chia), treManh = ms nghỉ giữa các mảnh,
 * rms = tổng byte RMS cho phép (0 = không giới hạn), rmsBanGhi = cỡ tối đa một bản ghi (0 = không giới hạn).
 */
public class KiemTaiJ2me {
    static final int MANH = Integer.getInteger("manh", 512);
    static final int TRE_MANH = Integer.getInteger("treManh", 1);
    /** > 0: kết nối ĐẦU TIÊN ngừng chuyển dữ liệu máy chủ -> máy sau chừng ấy byte (máy chủ "im", kết nối vẫn mở). */
    static final int CAT_SAU = Integer.getInteger("catSau", 0);
    static final AtomicInteger SO_KET_NOI = new AtomicInteger();
    static final AtomicLong XUONG = new AtomicLong(), LEN = new AtomicLong();
    static final List<String> LOI = Collections.synchronizedList(new ArrayList<String>());
    static ClassLoader L;
    static long t0 = System.currentTimeMillis();

    // ------------------------------------------------------------------ proxy chia mảnh

    static void chuyen(final InputStream in, final OutputStream out, final boolean xuong) {
        chuyen(in, out, xuong, 0);
    }

    static void chuyen(final InputStream in, final OutputStream out, final boolean xuong, final int catSau) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                Random r = new Random(42);
                byte[] b = new byte[65536];
                try {
                    int n;
                    long da = 0;
                    while ((n = in.read(b)) > 0) {
                        if (catSau > 0 && da + n > catSau) {
                            n = (int) Math.max(0, catSau - da);   // phần sau bị nuốt: máy chủ "im"
                        }
                        da += n;
                        if (xuong && MANH > 0) {
                            for (int off = 0; off < n; ) {
                                int c = Math.min(n - off, 1 + r.nextInt(MANH));
                                out.write(b, off, c);
                                out.flush();
                                off += c;
                                if (TRE_MANH > 0) Thread.sleep(TRE_MANH);
                            }
                        } else {
                            out.write(b, 0, n);
                            out.flush();
                        }
                        (xuong ? XUONG : LEN).addAndGet(n);
                    }
                } catch (Exception e) {
                }
                try { out.close(); } catch (Exception e) { }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    static int proxy(final int congServer) throws Exception {
        final ServerSocket ss = new ServerSocket(Integer.getInteger("congProxy", 29600), 50, InetAddress.getByName("127.0.0.1"));   // cổng < 32768: client giữ cổng bằng short
        Thread t = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Socket c = ss.accept();
                        Socket s = new Socket("127.0.0.1", congServer);
                        c.setTcpNoDelay(true);
                        s.setTcpNoDelay(true);
                        System.out.println("[proxy] ket noi moi t=" + (System.currentTimeMillis() - t0) + "ms");
                        chuyen(c.getInputStream(), s.getOutputStream(), false);
                        chuyen(s.getInputStream(), c.getOutputStream(), true, SO_KET_NOI.incrementAndGet() == 1 ? CAT_SAU : 0);
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
        return ss.getLocalPort();
    }

    // ------------------------------------------------------------------ RMS có giới hạn

    /** RMS bộ nhớ của MicroEmulator + trần tổng dung lượng / cỡ bản ghi (addRecord / setRecord ném RecordStoreFullException). */
    public static class RmsGioiHan extends org.microemu.util.MemoryRecordStoreManager {
        static final int TONG = Integer.getInteger("rms", 0);
        static final int BAN_GHI = Integer.getInteger("rmsBanGhi", 0);
        static int soDay;

        public int getSizeAvailable(org.microemu.util.RecordStoreImpl rs) {
            int con = Integer.MAX_VALUE / 2;
            if (TONG > 0) {
                int dung = 0;
                try {
                    String[] ds = listRecordStores();
                    for (int i = 0; ds != null && i < ds.length; i++) {
                        try {
                            javax.microedition.rms.RecordStore s = openRecordStore(ds[i], false);
                            dung += s.getSize();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                }
                con = Math.max(0, TONG - dung);
            }
            if (BAN_GHI > 0) con = Math.min(con, BAN_GHI);
            return con;
        }
    }

    // ------------------------------------------------------------------ phản chiếu vào lớp game (trong class loader MIDlet)

    static Class<?> lop(String ten) throws Exception {
        return Class.forName(ten, true, L);
    }

    static Object tinh(String lop, String truong) {
        try {
            Field f = lop(lop).getDeclaredField(truong);
            f.setAccessible(true);
            return f.get(null);
        } catch (Throwable e) {
            return null;
        }
    }

    static void datTinh(String lop, String truong, Object v) throws Exception {
        Field f = lop(lop).getDeclaredField(truong);
        f.setAccessible(true);
        f.set(null, v);
    }

    static Object truong(Object o, String ten) {
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(ten);
                f.setAccessible(true);
                return f.get(o);
            } catch (NoSuchFieldException e) {
            } catch (Throwable e) {
                return null;
            }
        }
        return null;
    }

    static Object goi(Object o, String ten, Object... a) throws Exception {
        for (Class<?> c = o instanceof Class ? (Class<?>) o : o.getClass(); c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(ten) && m.getParameterTypes().length == a.length) {
                    m.setAccessible(true);
                    return m.invoke(o instanceof Class ? null : o, a);
                }
            }
        }
        throw new NoSuchMethodException(ten);
    }

    static String ten(Object o) {
        return o == null ? "-" : o.getClass().getSimpleName();
    }

    static String trangThai() {
        Object man = tinh("game.render.Canvas", "currentScreen");
        Object dlg = tinh("game.render.Canvas", "currentDialog");
        Object loi = tinh("game.pokemon.ManTai", "loi");
        return ten(man) + " buoc=" + tinh("game.pokemon.ManTai", "buoc") + " dangVao=" + tinh("game.pokemon.ManTai", "dangVao")
                + " dlg=" + ten(dlg) + (dlg != null && truong(dlg, "noiDung") != null ? "[" + truong(dlg, "noiDung") + "]" : "") + (loi != null ? " loi=[" + loi + "]" : "");
    }

    static final boolean GC = Boolean.getBoolean("gc");

    static long heapDung() {
        Runtime r = Runtime.getRuntime();
        if (GC) {
            System.gc();
        }
        return (r.totalMemory() - r.freeMemory()) / 1024;
    }

    public static void main(String[] a) throws Exception {
        final String jar = a[0];
        final int cong = Integer.parseInt(a[1]);
        final String tk = a[2];
        final int cho = a.length > 3 ? Integer.parseInt(a[3]) : 180;
        final int w = Integer.getInteger("w", 240), h = Integer.getInteger("h", 320);
        System.setProperty("java.awt.headless", "true");
        // lỗi chết luồng (OOM trong luồng mạng / tải ảnh) - in ra, giả lập thật thì im lặng
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                LOI.add(t.getName() + ": " + e);
                System.out.println("[LUONG CHET] " + t.getName() + ": " + e);
            }
        });
        final int cp = proxy(cong);
        // khởi động MicroEmulator như org.microemu.app.Headless nhưng với RMS có giới hạn
        org.microemu.app.Headless hl = new org.microemu.app.Headless();
        Field fe = org.microemu.app.Headless.class.getDeclaredField("emulator");
        fe.setAccessible(true);
        org.microemu.app.Common em = (org.microemu.app.Common) fe.get(hl);
        List<String> ts = new ArrayList<String>();
        ts.add("--rms");
        ts.add("memory");
        ts.add("--resizableDevice");
        ts.add(String.valueOf(w));
        ts.add(String.valueOf(h));
        ts.add(new File(jar).getAbsolutePath());
        em.initParams(ts, new org.microemu.app.util.DeviceEntry("Default device", null, "org/microemu/device/default/device.xml", true, false),
                org.microemu.device.j2se.J2SEDevice.class);
        RmsGioiHan rms = new RmsGioiHan();
        rms.init(em);
        em.setRecordStoreManager(rms);
        System.out.println("heap toi da=" + Runtime.getRuntime().maxMemory() / 1024 + " KB, man " + w + "x" + h + ", manh<=" + MANH
                + " B, rms=" + RmsGioiHan.TONG + " ban ghi<=" + RmsGioiHan.BAN_GHI);
        em.initMIDlet(true);
        for (int i = 0; i < 300 && org.microemu.MIDletBridge.getCurrentMIDlet() == null; i++) Thread.sleep(100);
        javax.microedition.midlet.MIDlet m = org.microemu.MIDletBridge.getCurrentMIDlet();
        if (m == null) {
            System.out.println("KET QUA: MIDlet khong chay");
            System.exit(1);
        }
        L = m.getClass().getClassLoader();
        for (int i = 0; i < 600 && (tinh("game.render.Canvas", "currentScreen") == null || Boolean.TRUE.equals(tinh("game.render.Canvas", "isLoad"))); i++) Thread.sleep(100);
        System.out.println("khoi dong xong t=" + (System.currentTimeMillis() - t0) + "ms heap=" + heapDung() + "KB " + trangThai());
        Object loginScr = tinh("game.render.Canvas", "loginScr");
        for (int i = 0; i < 100 && tinh("game.render.Canvas", "currentScreen") != loginScr; i++) {
            if (i == 20) goi(loginScr, "switchToMe");
            Thread.sleep(100);
        }
        datTinh("game.render.screen.ServerListScr", "address", new String[]{"127.0.0.1"});
        datTinh("game.render.screen.ServerListScr", "port", new short[]{(short) cp});
        datTinh("game.render.screen.ServerListScr", "index", Integer.valueOf(0));
        goi(truong(loginScr, "tfUser"), "setText", tk);
        goi(truong(loginScr, "tfPass"), "setText", "123456");
        final long tDn = System.currentTimeMillis();
        goi(loginScr, "doLogin");
        boolean daTao = false, daChon = false;
        long tLoi = 0;
        // canh màn tải nhanh hơn để kiểm (mặc định game 60 s / 20 s)
        if (Integer.getInteger("dungMs", 0) > 0) {
            datTinh("game.pokemon.ManTai", "DUNG_MS", Long.valueOf(Integer.getInteger("dungMs", 0)));
            datTinh("game.pokemon.ManTai", "IM_MS", Long.valueOf(Integer.getInteger("imMs", 3000)));
        }
        long tVao = 0, maxHeap = 0;
        String cu = "";
        long het = System.currentTimeMillis() + cho * 1000L;
        while (System.currentTimeMillis() < het) {
            String tt = trangThai();
            maxHeap = Math.max(maxHeap, heapDung());
            if (!tt.equals(cu)) {
                System.out.println("t=" + (System.currentTimeMillis() - tDn) + "ms " + tt + " xuong=" + XUONG.get() + " len=" + LEN.get() + " heap=" + heapDung() + "KB");
                cu = tt;
            }
            Object man = tinh("game.render.Canvas", "currentScreen");
            String tm = ten(man);
            Object dl = tinh("game.render.Canvas", "currentDialog");
            if (tinh("game.pokemon.ManTai", "loi") != null && ten(dl).equals("HopThongBao") && tLoi == 0) {
                tLoi = System.currentTimeMillis();
                System.out.println("t=" + (tLoi - tDn) + "ms HOP LOI TAI: " + truong(dl, "tieuDe") + " - " + truong(dl, "noiDung"));
                if (Boolean.getBoolean("thuLai")) {
                    Object[] viec = (Object[]) truong(dl, "viec");
                    System.out.println("t=" + (System.currentTimeMillis() - tDn) + "ms BAM THU LAI");
                    goi(viec[viec.length - 1], "perform");
                } else {
                    break;
                }
            }
            if (!daChon && tm.equals("CharSelectScr")) {
                Object[] ci = (Object[]) truong(man, "charInfos");
                if (ci != null && ci.length > 0 && ci[0] != null) {
                    Object id = truong(ci[0], "IDDB");
                    if (Boolean.TRUE.equals(goi(man, "tuChonLai", id))) {
                        daChon = true;
                        System.out.println("t=" + (System.currentTimeMillis() - tDn) + "ms CHON POKEMON " + id);
                    }
                } else if (ci != null && !daTao) {
                    daTao = true;
                    Object gs = goi(lop("game.networklogic.GameService"), "gI");
                    StringBuffer sb = new StringBuffer("kiem");
                    long so = Math.abs((long) tk.hashCode() * 31L + System.nanoTime());
                    for (int k = 0; k < 6; k++, so /= 26) sb.append((char) ('a' + (int) (so % 26)));
                    String tenNv = sb.toString();
                    System.out.println("t=" + (System.currentTimeMillis() - tDn) + "ms TAO POKEMON " + tenNv);
                    goi(gs, "createChar", tenNv, Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                }
            }
            // tên Pokémon máy chủ từ chối (trùng / có từ cấm): đóng hộp, tạo lại tên khác
            if (daTao && !daChon && ten(dl).equals("HopThongBao") && String.valueOf(truong(dl, "noiDung")).indexOf("Tên") >= 0) {
                datTinh("game.render.Canvas", "currentDialog", null);
                daTao = false;
            }
            Object gameScr = tinh("game.render.Canvas", "gameScr");
            if (man != null && man == gameScr && daChon && !Boolean.TRUE.equals(tinh("game.pokemon.ManTai", "dangVao"))
                    && truong(gameScr, "mainChar") != null) {
                tVao = System.currentTimeMillis();
                break;
            }
            Thread.sleep(20);
        }
        System.out.println("heap lon nhat ~" + maxHeap + " KB; RMS: " + Arrays.toString(rms.listRecordStores()));
        for (String l : LOI) System.out.println("  loi: " + l);
        if (tLoi > 0) {
            System.out.println("HOP LOI: hien sau " + (tLoi - tDn) + " ms");
        }
        if (Boolean.getBoolean("mongLoi") && tLoi == 0) {
            System.out.println("KET QUA: KHONG HIEN HOP LOI (man tai quay mai?) " + trangThai());
            System.exit(1);
        }
        if (tVao == 0 && Boolean.getBoolean("mongLoi") && !Boolean.getBoolean("thuLai")) {
            System.out.println("KET QUA: HIEN HOP LOI DUNG (khong quay mai)");
            System.exit(0);
        }
        if (tVao == 0) {
            System.out.println("KET QUA: KHONG VAO DUOC GAME sau " + cho + " s; " + trangThai() + " xuong=" + XUONG.get());
            System.exit(1);
        }
        Thread.sleep(Integer.getInteger("giu", 0) * 1000L);   // giữ tiến trình (jmap -histo)
        System.out.println("KET QUA: VAO THE GIOI " + (tVao - tDn) + " ms, xuong=" + XUONG.get() + " B len=" + LEN.get() + " B");
        System.exit(0);
    }
}
