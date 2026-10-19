import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * Do thoi gian vao game THAT bang client PC (cung code jar/APK), cache rong, qua proxy gia lap do tre.
 * Do thoi gian vao game THAT (client PC = code jar/APK, cache rong) qua proxy gia lap do tre / bang thong; tu dang nhap, tu chon Pokemon dau.
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/KiemTaiThat.java
 *   java -Djava.awt.headless=true -Dtre=150 -Dbang=40000 [-DcongProxy=29495] -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTaiThat <cong server> <tk (mk 123456, da co Pokemon)> <thu muc du lieu rong> [giay theo doi]
 */
public class KiemTaiThat {
    static final long TRE = Long.getLong("tre", 0);          // ms mot chieu
    static final long BANG = Long.getLong("bang", 0);        // byte/giay chieu xuong (0 = khong gioi han)
    static final AtomicLong XUONG = new AtomicLong(), LEN = new AtomicLong();
    static final AtomicLong LEN_GOI = new AtomicLong();
    static volatile long t0; static int CP;

    static void chuyen(final InputStream in, final OutputStream out, final boolean xuong) {
        final LinkedBlockingQueue<Object[]> q = new LinkedBlockingQueue<Object[]>();
        Thread doc = new Thread(new Runnable() { public void run() {
            byte[] b = new byte[16384];
            try {
                int n;
                while ((n = in.read(b)) > 0) {
                    q.put(new Object[]{System.currentTimeMillis(), Arrays.copyOf(b, n)});
                }
            } catch (Exception e) { }
            try { q.put(new Object[]{0L, null}); } catch (InterruptedException e) { }
        }});
        Thread ghi = new Thread(new Runnable() { public void run() {
            try {
                while (true) {
                    Object[] o = q.take();
                    byte[] d = (byte[]) o[1];
                    if (d == null) break;
                    long den = (Long) o[0] + TRE;
                    long cho = den - System.currentTimeMillis();
                    if (cho > 0) Thread.sleep(cho);
                    if (xuong && BANG > 0) {
                        for (int off = 0; off < d.length; off += 1460) {
                            int c = Math.min(1460, d.length - off);
                            out.write(d, off, c); out.flush();
                            long ns = c * 1000000000L / BANG;
                            Thread.sleep(ns / 1000000L, (int) (ns % 1000000L));
                        }
                    } else {
                        out.write(d); out.flush();
                    }
                    (xuong ? XUONG : LEN).addAndGet(d.length);
                }
            } catch (Exception e) { }
            try { out.close(); } catch (Exception e) { }
        }});
        doc.setDaemon(true); ghi.setDaemon(true); doc.start(); ghi.start();
    }

    static int proxy(final int congServer) throws Exception {
        final ServerSocket ss = new ServerSocket(Integer.getInteger("congProxy", 29495), 50, InetAddress.getByName("127.0.0.1"));
        Thread t = new Thread(new Runnable() { public void run() {
            while (true) {
                try {
                    Socket c = ss.accept();
                    Socket s = new Socket("127.0.0.1", congServer);
                    c.setTcpNoDelay(true); s.setTcpNoDelay(true);
                    System.out.println("[proxy] ket noi moi t=" + (System.currentTimeMillis() - t0) + "ms");
                    chuyen(c.getInputStream(), s.getOutputStream(), false);
                    chuyen(s.getInputStream(), c.getOutputStream(), true);
                } catch (Exception e) { return; }
            }
        }});
        t.setDaemon(true); t.start();
        return ss.getLocalPort();
    }

    static void tren(final Runnable r) throws Exception {
        final CountDownLatch l = new CountDownLatch(1);
        NenPc.luong().them(new Runnable() { public void run() {
            try { r.run(); } catch (Throwable e) { e.printStackTrace(); } finally { l.countDown(); }
        }});
        l.await(5, TimeUnit.SECONDS);
    }

    static String noiDung(Object d) {
        try { java.lang.reflect.Field f = d.getClass().getDeclaredField("noiDung"); f.setAccessible(true); return "[" + f.get(d) + "]"; } catch (Throwable e) { return ""; }
    }
    static String trangThai() {
        Object man = game.render.Canvas.currentScreen;
        return (man == null ? "null" : man.getClass().getSimpleName()) + " buoc=" + game.pokemon.ManTai.buoc
                + " dangVao=" + game.pokemon.ManTai.dangVao + " dlg=" + (game.render.Canvas.currentDialog == null ? "-" : game.render.Canvas.currentDialog.getClass().getSimpleName() + noiDung(game.render.Canvas.currentDialog));
    }

    public static void main(String[] a) throws Exception {
        final int cong = Integer.parseInt(a[0]);
        final String tk = a[1];
        File du = new File(a[2]);
        int theoDoi = a.length > 3 ? Integer.parseInt(a[3]) : 20;
        System.setProperty("java.awt.headless", "true");
        System.setProperty("htbb.duLieu", du.getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        t0 = System.currentTimeMillis();
        final int cp = proxy(cong); CP = cp;
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:" + cp);
        NenPc.tinhKichThuoc(1280, 720);
        tren(new Runnable() { public void run() {
            try {
                MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                MIDlet.batDau(m);
            } catch (Throwable e) { e.printStackTrace(); }
        }});
        for (int i = 0; i < 200 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null || game.render.Canvas.isLoad); i++) Thread.sleep(100);
        System.out.println("khoi dong xong t=" + (System.currentTimeMillis() - t0) + " " + trangThai());
        // cho toi man dang nhap
        for (int i = 0; i < 100 && !(game.render.Canvas.currentScreen instanceof game.render.screen.LoginScr); i++) {
            if (i == 20) tren(new Runnable() { public void run() { game.render.Canvas.loginScr.switchToMe(); } });
            Thread.sleep(100);
        }
        final long tDn = System.currentTimeMillis();
        tren(new Runnable() { public void run() {
            try {
                game.render.screen.ServerListScr.address = new String[]{"127.0.0.1"};
                game.render.screen.ServerListScr.port = new short[]{(short) CP};
                game.render.screen.ServerListScr.index = 0;
                game.render.screen.LoginScr l = game.render.Canvas.loginScr;
                l.tfUser.setText(tk);
                l.tfPass.setText("123456");
                java.lang.reflect.Method m = game.render.screen.LoginScr.class.getDeclaredMethod("doLogin");
                m.setAccessible(true);
                m.invoke(l);
            } catch (Throwable e) { e.printStackTrace(); }
        }});
        boolean daChon = false;
        long tChon = 0, tVao = 0;
        String cu = "";
        long het = System.currentTimeMillis() + 180000;
        while (System.currentTimeMillis() < het) {
            String tt = trangThai();
            if (!tt.equals(cu)) {
                System.out.println("t=" + (System.currentTimeMillis() - tDn) + "ms " + tt + " xuong=" + XUONG.get() + " len=" + LEN.get());
                cu = tt;
            }
            Object man = game.render.Canvas.currentScreen;
            if (!daChon && man instanceof game.render.screen.CharSelectScr) {
                final game.render.screen.CharSelectScr cs = (game.render.screen.CharSelectScr) man;
                final boolean[] ok = {false};
                tren(new Runnable() { public void run() {
                    try {
                        java.lang.reflect.Field f = game.render.screen.CharSelectScr.class.getDeclaredField("charInfos");
                        f.setAccessible(true);
                        Object[] ci = (Object[]) f.get(cs);
                        if (ci != null && ci.length > 0 && ci[0] != null) {
                            java.lang.reflect.Field id = ci[0].getClass().getField("IDDB");
                            ok[0] = cs.tuChonLai(((Number) id.get(ci[0])).intValue());
                        }
                    } catch (Throwable e) { e.printStackTrace(); }
                }});
                if (ok[0]) { daChon = true; tChon = System.currentTimeMillis(); System.out.println("t=" + (tChon - tDn) + "ms CHON POKEMON"); }
            }
            if (man == game.render.Canvas.gameScr && !game.pokemon.ManTai.dangVao && daChon && game.render.Canvas.gameScr.mainChar != null) {
                tVao = System.currentTimeMillis();
                break;
            }
            Thread.sleep(20);
        }
        if (tVao == 0) {
            System.out.println("KET QUA: KHONG VAO DUOC GAME sau 180 s; " + trangThai());
        } else {
            System.out.println("KET QUA: dang nhap -> vao the gioi " + (tVao - tDn) + " ms (chon -> vao " + (tVao - tChon) + " ms), xuong=" + XUONG.get() + " B len=" + LEN.get() + " B");
        }
        long xuong0 = XUONG.get(), len0 = LEN.get();
        for (int s = 1; s <= theoDoi; s++) {
            Thread.sleep(1000);
            if (s % 5 == 0) System.out.println("  +" + s + "s xuong=" + (XUONG.get() - xuong0) + " len=" + (LEN.get() - len0) + " " + trangThai()
                    + " ketnoi=" + network.Session_ME.gI().connected);
        }
        System.out.println("TONG: xuong=" + XUONG.get() + " B len=" + LEN.get() + " B");
        System.exit(0);
    }
}
