package network;

import java.util.zip.Inflater;

/** Giải nén zlib cho gói -127 (xem network.GiaiNen, server io.GoiNen). Nạp bằng Class.forName trong Session_ME. */
public class GiaiNenZip implements GiaiNen {
    public byte[] giai(byte[] nen) throws Exception {
        Inflater f = new Inflater();
        try {
            f.setInput(nen);
            byte[] ra = new byte[Math.max(1024, nen.length * 4)];
            int n = 0;
            while (!f.finished()) {
                if (n == ra.length) {
                    byte[] to = new byte[ra.length * 2];
                    System.arraycopy(ra, 0, to, 0, n);
                    ra = to;
                }
                int c = f.inflate(ra, n, ra.length - n);
                if (c == 0 && (f.needsInput() || f.needsDictionary())) {
                    throw new java.io.IOException("goi nen hong");
                }
                n += c;
            }
            byte[] kq = new byte[n];
            System.arraycopy(ra, 0, kq, 0, n);
            return kq;
        } finally {
            f.end();
        }
    }
}
