package game.render.screen;

import game.networklogic.GameService;
import game.render.Canvas;
import java.util.Random;
import network.Message;

/**
 * Xác thực hai chiều trước khi đăng nhập (lệnh -88, xem KPAH real/XacThucClient.java).
 * 1. Gửi host đang kết nối + 16 byte ngẫu nhiên nC; server trả chữ ký RSA-1024 của
 *    (MAGIC, host, nC, nS) + nS. Kiểm bằng khóa công khai N: sai / không trả lời thì không
 *    đăng nhập, báo đúng câu "không thể kết nối" của game (không lộ là bị kiểm).
 * 2. Gửi SHA-256(k | nS | nC | host | k) để server biết đây là client mod; k tính lúc chạy
 *    từ N và AutoGuard.MAU_NEN, không nằm sẵn trong jar.
 */
final class KiemServer {
    private static final int LENH = -88;
    private static final long M = 0xFFFFFFFFL;
    /** Khóa công khai (e = 65537), 32 số 32-bit, số thấp trước. */
    private static final int[] N = {0xe753c265, 0x2b76f6a4, 0x15a19590, 0xecf19eea, 0x67d5e2e9, 0xb66ba946, 0xfc4bc516, 0xc850b2ee, 0x2820ecf2, 0xf820c771, 0x8cfa9f69, 0xbffac9b0, 0x301d384f, 0xfd55af57, 0xcc91852b, 0xa211e18b, 0xc35948b6, 0xbe73bbd2, 0xf1105817, 0x5a8b5de1, 0xd5b57bc0, 0xdaedb6ef, 0x5dba856, 0x37b5e20e, 0x62c91a56, 0x64dd8242, 0x582aa78d, 0x4519d24d, 0x9d3c0b84, 0x9ea53550, 0xf821af79, 0x8ca875ab};

    private static final byte[] MAGIC = {-115, 50, 41, -64, -25, 78, 23, -22};

    static boolean daQua;
    private static long hanNgat;
    private static boolean dangCho;
    private static boolean choPhep;
    private static int lan;
    private static String host;
    private static byte[] nonce;
    private static byte[] nonceS;
    private static String tk;
    private static String mk;
    private static String ver;

    private KiemServer() {
    }

    /**
     * pokemon: có làm bước -88 không. Tắt khi build không bật XAC_THUC (mặc định nhánh Pokémon - server
     * KPAH không trả lời -88, client sẽ chờ 30s rồi báo "không thể kết nối"), hoặc jar chỉ định server
     * Pokémon bằng MANIFEST "HTBB-May-Chu".
     */
    static boolean batXacThuc() {
        return PhienBan.XAC_THUC && TaiDanhSach.mayChuThu() == null;
    }

    /** true = chặn lệnh đăng nhập này (đang xác thực trước). */
    static boolean chanDangNhap(String string, String string2, String string3) {
        if (!batXacThuc()) {
            // pokemon: server Pokémon (KPAH) không có lệnh -88 -> không xác thực, đăng nhập thẳng
            daQua = true;
            return false;
        }
        if (choPhep) {
            choPhep = false;
            daQua = true;
            return false;
        }
        daQua = false;
        tk = string;
        mk = string2;
        ver = string3;
        dangCho = true;
        final int n = ++lan;
        try {
            host = ServerListScr.address[ServerListScr.index];
            Random random = new Random(System.currentTimeMillis() ^ (long)Runtime.getRuntime().freeMemory() << 17);
            nonce = new byte[16];
            for (int i = 0; i < 16; ++i) {
                nonce[i] = (byte)random.nextInt();
            }
            Message message = new Message(LENH);
            message.writer().writeByte(1);
            message.writer().writeUTF(host);
            message.writer().write(nonce);
            GameService.gI().session.sendMessage(message);
            message.cleanup();
        }
        catch (Throwable throwable) {
            thatBai(n);
            return true;
        }
        new Thread(){

            public void run() {
                try {
                    // 30s: máy yếu / giả lập thông dịch (CoffeeCat) chậm hơn máy tính nhiều
                    Thread.sleep(30000L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                thatBai(n);
            }
        }.start();
        return true;
    }

    /** Chèn ở ĐẦU MessageHandler.onMessage: true = gói -88 đã xử lý ở đây. */
    static boolean nhan(Message message) {
        if (message.command != LENH) {
            return false;
        }
        if (!dangCho) {
            return true;
        }
        int n = lan;
        boolean bl = false;
        try {
            byte[] byArray = new byte[128];
            message.reader().readFully(byArray);
            nonceS = new byte[16];
            message.reader().readFully(nonceS);
            bl = kiem(byArray);
        }
        catch (Throwable throwable) {
            // gói hỏng = sai
        }
        if (!bl) {
            thatBai(n);
            return true;
        }
        try {
            Message message2 = new Message(LENH);
            message2.writer().writeByte(2);
            message2.writer().write(chungMinh());
            GameService.gI().session.sendMessage(message2);
            message2.cleanup();
        }
        catch (Throwable throwable) {
            thatBai(n);
            return true;
        }
        dangCho = false;
        choPhep = true;
        GameService.gI().login(tk, mk, ver);
        return true;
    }

    /** Vào game mà chưa qua xác thực (client bị gỡ bước kiểm) -> ngắt sau 1-3 phút ngẫu nhiên. */
    static boolean canNgat() {
        if (!batXacThuc()) {
            return false;
        }
        if (daQua || dangCho || Canvas.currentScreen != Canvas.gameScr) {
            hanNgat = 0L;
            return false;
        }
        long l = System.currentTimeMillis();
        if (hanNgat == 0L) {
            hanNgat = l + 60000L + (long)(new Random(l).nextInt() & 0x1FFFF);
        }
        return l >= hanNgat;
    }

    private static void thatBai(int n) {
        if (n != lan || !dangCho) {
            return;
        }
        dangCho = false;
        daQua = false;
        Canvas.gameScr.onConnectFail();
    }

    private static boolean kiem(byte[] byArray) {
        int[] nArray = thanhSo(byArray);
        if (!nhoHon(nArray, N)) {
            return false;
        }
        int[] nArray2 = KiemServer.luyThua65537(nArray);
        int[] nArray3 = thanhSo(dongGoi());
        for (int i = 0; i < 32; ++i) {
            if (nArray2[i] == nArray3[i]) continue;
            return false;
        }
        return true;
    }

    private static byte[] hostBytes() {
        try {
            return host.getBytes("UTF-8");
        }
        catch (Throwable throwable) {
            return host.getBytes();
        }
    }

    /** 00 01 FF..FF 00 MAGIC host nC nS - phải giống hệt XacThucClient.dongGoi() bên server. */
    private static byte[] dongGoi() {
        byte[] byArray = hostBytes();
        byte[] byArray2 = new byte[128];
        int n = 128 - MAGIC.length - byArray.length - nonce.length - nonceS.length;
        byArray2[1] = 1;
        for (int i = 2; i < n - 1; ++i) {
            byArray2[i] = -1;
        }
        System.arraycopy(MAGIC, 0, byArray2, n, MAGIC.length);
        n += MAGIC.length;
        System.arraycopy(byArray, 0, byArray2, n, byArray.length);
        n += byArray.length;
        System.arraycopy(nonce, 0, byArray2, n, nonce.length);
        System.arraycopy(nonceS, 0, byArray2, n + nonce.length, nonceS.length);
        return byArray2;
    }

    /** k = SHA-256(N 128 byte | MAU_NEN); trả SHA-256(k | nS | nC | host | k). */
    static byte[] chungMinh() {
        return KiemServer.chungMinh(hostBytes(), nonce, nonceS);
    }

    /** Như trên, với host / nonce truyền vào. */
    static byte[] chungMinh(byte[] byArray3, byte[] nC, byte[] nS) {
        int i;
        int n;
        byte[] byArray = new byte[128 + AutoGuard.MAU_NEN.length * 4];
        for (i = 0; i < 128; ++i) {
            n = 127 - i;
            byArray[i] = (byte)(N[n >> 2] >>> ((n & 3) << 3));
        }
        for (i = 0; i < AutoGuard.MAU_NEN.length; ++i) {
            n = AutoGuard.MAU_NEN[i];
            byArray[128 + i * 4] = (byte)(n >>> 24);
            byArray[129 + i * 4] = (byte)(n >>> 16);
            byArray[130 + i * 4] = (byte)(n >>> 8);
            byArray[131 + i * 4] = (byte)n;
        }
        byte[] byArray2 = bam(byArray);
        byte[] byArray4 = new byte[96 + byArray3.length];
        System.arraycopy(byArray2, 0, byArray4, 0, 32);
        System.arraycopy(nS, 0, byArray4, 32, 16);
        System.arraycopy(nC, 0, byArray4, 48, 16);
        System.arraycopy(byArray3, 0, byArray4, 64, byArray3.length);
        System.arraycopy(byArray2, 0, byArray4, 64 + byArray3.length, 32);
        return bam(byArray4);
    }

    private static final int[] KB = {0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

    /** SHA-256 (CLDC không có MessageDigest). */
    static byte[] bam(byte[] byArray) {
        int i;
        int[] h = {0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};
        int n = (byArray.length + 9 + 63) / 64 * 64;
        byte[] byArray2 = new byte[n];
        System.arraycopy(byArray, 0, byArray2, 0, byArray.length);
        byArray2[byArray.length] = -128;
        long l = (long)byArray.length * 8L;
        for (i = 0; i < 8; ++i) {
            byArray2[n - 1 - i] = (byte)(l >>> (i << 3));
        }
        int[] w = new int[64];
        for (int j = 0; j < n; j += 64) {
            for (i = 0; i < 16; ++i) {
                w[i] = (byArray2[j + i * 4] & 0xFF) << 24 | (byArray2[j + i * 4 + 1] & 0xFF) << 16 | (byArray2[j + i * 4 + 2] & 0xFF) << 8 | byArray2[j + i * 4 + 3] & 0xFF;
            }
            for (i = 16; i < 64; ++i) {
                int s0 = (w[i - 15] >>> 7 | w[i - 15] << 25) ^ (w[i - 15] >>> 18 | w[i - 15] << 14) ^ w[i - 15] >>> 3;
                int s1 = (w[i - 2] >>> 17 | w[i - 2] << 15) ^ (w[i - 2] >>> 19 | w[i - 2] << 13) ^ w[i - 2] >>> 10;
                w[i] = w[i - 16] + s0 + w[i - 7] + s1;
            }
            int a = h[0];
            int b = h[1];
            int c = h[2];
            int d = h[3];
            int e = h[4];
            int f = h[5];
            int g = h[6];
            int k = h[7];
            for (i = 0; i < 64; ++i) {
                int t1 = k + ((e >>> 6 | e << 26) ^ (e >>> 11 | e << 21) ^ (e >>> 25 | e << 7)) + (e & f ^ ~e & g) + KB[i] + w[i];
                int t2 = ((a >>> 2 | a << 30) ^ (a >>> 13 | a << 19) ^ (a >>> 22 | a << 10)) + (a & b ^ a & c ^ b & c);
                k = g;
                g = f;
                f = e;
                e = d + t1;
                d = c;
                c = b;
                b = a;
                a = t1 + t2;
            }
            h[0] += a;
            h[1] += b;
            h[2] += c;
            h[3] += d;
            h[4] += e;
            h[5] += f;
            h[6] += g;
            h[7] += k;
        }
        byte[] byArray3 = new byte[32];
        for (i = 0; i < 32; ++i) {
            byArray3[i] = (byte)(h[i >> 2] >>> (24 - ((i & 3) << 3)));
        }
        return byArray3;
    }

    /** 128 byte (byte cao trước) -> 32 số 32-bit (số thấp trước). */
    private static int[] thanhSo(byte[] byArray) {
        int[] nArray = new int[32];
        for (int i = 0; i < 128; ++i) {
            int n = 127 - i;
            nArray[n >> 2] |= (byArray[i] & 0xFF) << ((n & 3) << 3);
        }
        return nArray;
    }

    private static boolean nhoHon(int[] nArray, int[] nArray2) {
        for (int i = nArray2.length - 1; i >= 0; --i) {
            long l = (i < nArray.length ? (long)nArray[i] & M : 0L);
            long l2 = (long)nArray2[i] & M;
            if (l == l2) continue;
            return l < l2;
        }
        return false;
    }

    private static int nInv;
    private static int[] r2;

    /**
     * s^65537 mod N bằng nhân Montgomery (theo từng từ 32 bit). Bản cũ (nhanMod, dịch-trừ từng
     * bit) cần ~3-4 triệu phép tính: máy tính chạy JIT thì vài ms, nhưng giả lập thông dịch như
     * CoffeeCat chạy quá 10s chờ -> "Không thể kết nối". Bản này nhanh hơn ~100 lần.
     */
    static int[] luyThua65537(int[] s) {
        if (r2 == null) {
            // -N^-1 mod 2^32 (Newton) và R^2 mod N với R = 2^1024 (nhân đôi 2048 lần) - tính một lần
            int x = N[0];
            for (int i = 0; i < 5; ++i) {
                x *= 2 - N[0] * x;
            }
            nInv = -x;
            int[] r = new int[33];
            r[0] = 1;
            for (int i = 0; i < 2048; ++i) {
                int c = 0;
                for (int j = 0; j < 33; ++j) {
                    int c2 = r[j] >>> 31;
                    r[j] = r[j] << 1 | c;
                    c = c2;
                }
                KiemServer.truNeuLon(r);
            }
            int[] r3 = new int[32];
            System.arraycopy(r, 0, r3, 0, 32);
            r2 = r3;
        }
        int[] sM = KiemServer.mont(s, r2);
        int[] x = sM;
        for (int i = 0; i < 16; ++i) {
            x = KiemServer.mont(x, x);
        }
        x = KiemServer.mont(x, sM);
        int[] mot = new int[32];
        mot[0] = 1;
        return KiemServer.mont(x, mot);
    }

    /** a * b * R^-1 mod N (CIOS). */
    private static int[] mont(int[] a, int[] b) {
        int[] t = new int[34];
        for (int i = 0; i < 32; ++i) {
            long ai = (long)a[i] & M;
            long c = 0L;
            for (int j = 0; j < 32; ++j) {
                long v = ((long)t[j] & M) + ai * ((long)b[j] & M) + c;
                t[j] = (int)v;
                c = v >>> 32;
            }
            long v = ((long)t[32] & M) + c;
            t[32] = (int)v;
            t[33] = (int)(v >>> 32);
            long m = (long)(t[0] * nInv) & M;
            v = ((long)t[0] & M) + m * ((long)N[0] & M);
            c = v >>> 32;
            for (int j = 1; j < 32; ++j) {
                v = ((long)t[j] & M) + m * ((long)N[j] & M) + c;
                t[j - 1] = (int)v;
                c = v >>> 32;
            }
            v = ((long)t[32] & M) + c;
            t[31] = (int)v;
            t[32] = (int)(((long)t[33] & M) + (v >>> 32));
            t[33] = 0;
        }
        int[] kq = new int[33];
        System.arraycopy(t, 0, kq, 0, 33);
        KiemServer.truNeuLon(kq);
        int[] kq2 = new int[32];
        System.arraycopy(kq, 0, kq2, 0, 32);
        return kq2;
    }

    /** r (33 từ) >= N thì trừ N một lần. */
    private static void truNeuLon(int[] r) {
        if (r[32] == 0 && nhoHon(r, N)) {
            return;
        }
        long l = 0L;
        for (int j = 0; j < 33; ++j) {
            long v = ((long)r[j] & M) - (j < 32 ? (long)N[j] & M : 0L) - l;
            r[j] = (int)v;
            l = v < 0L ? 1L : 0L;
        }
    }

    /** a * b mod N (dịch-trừ từng bit, đủ nhanh cho 17 lần nhân). */
    private static int[] nhanMod(int[] nArray, int[] nArray2) {
        int n;
        int[] nArray3 = new int[64];
        for (int i = 0; i < 32; ++i) {
            long l = 0L;
            long l2 = (long)nArray[i] & M;
            for (n = 0; n < 32; ++n) {
                long l3 = l2 * ((long)nArray2[n] & M) + ((long)nArray3[i + n] & M) + l;
                nArray3[i + n] = (int)l3;
                l = l3 >>> 32;
            }
            nArray3[i + 32] = (int)l;
        }
        int[] nArray4 = new int[33];
        for (int i = 2047; i >= 0; --i) {
            n = nArray3[i >>> 5] >>> (i & 0x1F) & 1;
            for (int j = 0; j < 33; ++j) {
                int n2 = nArray4[j] >>> 31;
                nArray4[j] = nArray4[j] << 1 | n;
                n = n2;
            }
            if (nhoHon(nArray4, N) && nArray4[32] == 0) continue;
            long l = 0L;
            for (int j = 0; j < 33; ++j) {
                long l4 = ((long)nArray4[j] & M) - (j < 32 ? (long)N[j] & M : 0L) - l;
                nArray4[j] = (int)l4;
                l = l4 < 0L ? 1L : 0L;
            }
        }
        int[] nArray5 = new int[32];
        System.arraycopy(nArray4, 0, nArray5, 0, 32);
        return nArray5;
    }
}
