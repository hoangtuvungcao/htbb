import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DO TAI LOGIN SERVER - dong vai game server (giao thuc docs/dang-nhap.md), ban dot dang nhap vao login server va do
 * do tre tung yeu cau (tu luc ghi khung toi luc nhan tra loi). Dung de so ban cu (CheckLoginSocket.jar) va ban moi
 * (htbb-login.jar) tren cung CSDL.
 *
 * <pre>
 *   javac -d /tmp/dtdn tools/pokemon/kiem_tai/DoTaiDangNhap.java
 *   java -cp /tmp/dtdn DoTaiDangNhap &lt;host&gt; &lt;cong&gt; &lt;soYeuCau&gt; &lt;giay&gt; [soKetNoi=1] [tienTo=tai] [tu=1] [den=1000]
 *        -Dmk=123456       mat khau dung        -Dsai=10   % yeu cau co mat khau sai (kiem nhanh sai -> -1)
 *        -DhetGio=15000    QUA_HAN neu chua tra loi sau chung nay ms (= io.LogInController.CHO_LOGIN_MS: game da bao
 *                          nguoi choi "Máy chủ đăng nhập chưa phản hồi", tra loi muon bi bo)
 * </pre>
 *
 * Ban deu (vong mo): yeu cau i gui luc t0 + i * giay / soYeuCau, chia vong tron cho soKetNoi ket noi (game that dung
 * MOT ket noi). In: gui / nhan / ok / sai / khoa / loi(-3) / QUA_HAN, do tre p50 p90 p99 max. Ma thoat 0 = khong mat yeu
 * cau nao va moi ket qua dung mong doi (tai khoan ton tai -> id > 0, mat khau sai -> -1).
 */
public final class DoTaiDangNhap {

    static final AtomicLong nhan = new AtomicLong(), ok = new AtomicLong(), sai = new AtomicLong(), khoa = new AtomicLong(),
            loi = new AtomicLong(), lech = new AtomicLong();
    static final ConcurrentHashMap<Long, long[]> cho = new ConcurrentHashMap<>(); // phien -> {guiLucNano, laSai}
    static final List<Long> tre = Collections.synchronizedList(new ArrayList<Long>());

    public static void main(String[] a) throws Exception {
        if (a.length < 4) {
            System.out.println("DoTaiDangNhap <host> <cong> <soYeuCau> <giay> [soKetNoi=1] [tienTo=tai] [tu=1] [den=1000]");
            System.exit(2);
        }
        final String host = a[0];
        final int cong = Integer.parseInt(a[1]);
        final int n = Integer.parseInt(a[2]);
        final double giay = Double.parseDouble(a[3]);
        final int soKn = a.length > 4 ? Integer.parseInt(a[4]) : 1;
        final String tienTo = a.length > 5 ? a[5] : "tai";
        final int tu = a.length > 6 ? Integer.parseInt(a[6]) : 1;
        final int den = a.length > 7 ? Integer.parseInt(a[7]) : 1000;
        final String mk = System.getProperty("mk", "123456");
        final int phanTramSai = Integer.getInteger("sai", 0);
        final long hetGio = Long.getLong("hetGio", 15000L);

        final Socket[] s = new Socket[soKn];
        final OutputStream[] ra = new OutputStream[soKn];
        for (int i = 0; i < soKn; i++) {
            s[i] = new Socket(host, cong);
            s[i].setTcpNoDelay(true);
            ra[i] = new BufferedOutputStream(s[i].getOutputStream());
            final DataInputStream vao = new DataInputStream(new BufferedInputStream(s[i].getInputStream()));
            final Thread t = new Thread(() -> doc(vao), "doc-" + i);
            t.setDaemon(true);
            t.start();
        }
        final long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            final long hen = t0 + (long) (i * giay * 1e9 / n);
            long con;
            while ((con = hen - System.nanoTime()) > 0) {
                Thread.sleep(con / 1000000L, (int) (con % 1000000L));
            }
            final String ten = String.format("%s%04d", tienTo, tu + (i % (den - tu + 1)));
            final boolean laSai = phanTramSai > 0 && (i * 7919L % 100) < phanTramSai;
            final long phien = 1000000L + i;
            final byte[] k = khung(phien, ten, laSai ? mk + "x" : mk);
            cho.put(phien, new long[]{System.nanoTime(), laSai ? 1 : 0});
            final OutputStream o = ra[i % soKn];
            synchronized (o) {
                o.write(k);
                o.flush();
            }
        }
        final long guiXong = System.nanoTime();
        final long han = guiXong + hetGio * 1000000L;
        while (!cho.isEmpty() && System.nanoTime() < han) { // cho them hetGio sau yeu cau cuoi
            Thread.sleep(20L);
        }
        final long mat = cho.size();
        final List<Long> l;
        synchronized (tre) {
            l = new ArrayList<>(tre);
        }
        Collections.sort(l);
        long muon = 0;
        for (long x : l) if (x > hetGio) muon++;
        System.out.println(String.format(Locale.ROOT,
                "gui=%d trong %.1fs ket_noi=%d | nhan=%d ok=%d sai=%d khoa=%d loi(-3)=%d lech=%d | QUA_HAN(>%dms: game da bao 'chua phan hoi')=%d (khong tra loi %d, tra loi muon %d) | tre ms p50=%d p90=%d p99=%d max=%d | thong luong=%.0f/s",
                n, (guiXong - t0) / 1e9, soKn, nhan.get(), ok.get(), sai.get(), khoa.get(), loi.get(), lech.get(), hetGio, mat + muon, mat, muon,
                pv(l, 0.50), pv(l, 0.90), pv(l, 0.99), l.isEmpty() ? 0 : l.get(l.size() - 1),
                nhan.get() / Math.max(0.001, (System.nanoTime() - t0) / 1e9)));
        for (Socket x : s) {
            x.close();
        }
        System.exit(mat + muon == 0 && loi.get() == 0 && lech.get() == 0 ? 0 : 1);
    }

    static long pv(List<Long> l, double q) {
        if (l.isEmpty()) return 0;
        return l.get(Math.min(l.size() - 1, (int) Math.ceil(q * l.size()) - 1));
    }

    static void doc(DataInputStream vao) {
        try {
            while (true) {
                final int lenh = vao.readByte();
                final int dai = vao.readUnsignedShort();
                final byte[] d = new byte[dai];
                vao.readFully(d);
                if (lenh != 1 || dai < 14) continue;
                final DataInputStream r = new DataInputStream(new java.io.ByteArrayInputStream(d));
                final long phien = r.readLong();
                final int kq = r.readInt();
                final long[] g = cho.remove(phien);
                if (g == null) continue;
                tre.add((System.nanoTime() - g[0]) / 1000000L);
                nhan.incrementAndGet();
                if (kq > 0) ok.incrementAndGet();
                else if (kq == -1) sai.incrementAndGet();
                else if (kq == -2 || kq == -5) khoa.incrementAndGet();
                else loi.incrementAndGet();
                if ((g[1] == 1) != (kq == -1) && kq != -3) lech.incrementAndGet();
            }
        } catch (IOException e) {
            // ket noi dong
        }
    }

    static byte[] khung(long phien, String ten, String mk) throws IOException {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final DataOutputStream o = new DataOutputStream(b);
        o.writeLong(phien);
        o.writeUTF(ten);
        o.writeUTF(mk);
        o.writeByte(9);
        final byte[] d = b.toByteArray();
        final ByteArrayOutputStream k = new ByteArrayOutputStream();
        final DataOutputStream ok = new DataOutputStream(k);
        ok.writeByte(1);
        ok.writeShort(d.length);
        ok.write(d);
        return k.toByteArray();
    }
}
