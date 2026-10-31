import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy TCP them do tre - gia lap CSDL cham / treo de so login server cu va moi (docs/dang-nhap.md muc 8).
 *
 * <pre>
 *   java -cp /tmp/x ProxyTre &lt;congNghe&gt; &lt;hostDich&gt; &lt;congDich&gt; &lt;treMs&gt; [tepDieuKhien]
 * </pre>
 *
 * Moi khoi du lieu CSDL tra ve (dich -> ben goi) bi giu {@code treMs} truoc khi chuyen = moi truy van cham them
 * ~treMs (giong MariaDB ban ron khi game dang luu nhan vat). {@code tepDieuKhien} (tuy chon) doc moi 200 ms:
 * "treo" = ngung chuyen du lieu nhung KHONG dong ket noi (mat mang nua voi - TCP con mo, khong ai tra loi);
 * "cat" = dong moi ket noi dang mo; so = doi do tre (ms); khac / khong co tep = binh thuong.
 */
public final class ProxyTre {

    static volatile long tre;
    static volatile boolean treo;
    static final List<Socket> dangMo = new ArrayList<>();

    public static void main(String[] a) throws Exception {
        final int cong = Integer.parseInt(a[0]);
        final String host = a[1];
        final int congDich = Integer.parseInt(a[2]);
        tre = Long.parseLong(a[3]);
        if (a.length > 4) {
            final File f = new File(a[4]);
            final Thread t = new Thread(() -> {
                String truoc = "";
                while (true) {
                    try {
                        Thread.sleep(200);
                        final String s = f.isFile() ? new String(Files.readAllBytes(f.toPath())).trim() : "";
                        if (s.equals(truoc)) continue;
                        truoc = s;
                        treo = "treo".equals(s);
                        if ("cat".equals(s)) {
                            synchronized (dangMo) {
                                for (Socket x : dangMo) try { x.close(); } catch (IOException ignored) { }
                                dangMo.clear();
                            }
                        } else if (s.matches("\\d+")) {
                            tre = Long.parseLong(s);
                        }
                        System.out.println("[PROXY] dieu khien: '" + s + "' tre=" + tre + " treo=" + treo);
                    } catch (Exception ignored) {
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
        try (ServerSocket ss = new ServerSocket(cong)) {
            System.out.println("[PROXY] " + cong + " -> " + host + ":" + congDich + " tre=" + tre + "ms");
            while (true) {
                final Socket vao = ss.accept();
                final Socket ra = new Socket(host, congDich);
                vao.setTcpNoDelay(true);
                ra.setTcpNoDelay(true);
                synchronized (dangMo) {
                    dangMo.add(vao);
                    dangMo.add(ra);
                }
                bom(vao.getInputStream(), ra.getOutputStream(), false, vao, ra);
                bom(ra.getInputStream(), vao.getOutputStream(), true, vao, ra);
            }
        }
    }

    static void bom(InputStream i, OutputStream o, boolean coTre, Socket x, Socket y) {
        final Thread t = new Thread(() -> {
            final byte[] b = new byte[16384];
            try {
                int n;
                while ((n = i.read(b)) > 0) {
                    while (treo) Thread.sleep(50);
                    if (coTre && tre > 0) Thread.sleep(tre);
                    o.write(b, 0, n);
                    o.flush();
                }
            } catch (Exception ignored) {
            } finally {
                try { x.close(); } catch (IOException ignored) { }
                try { y.close(); } catch (IOException ignored) { }
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
