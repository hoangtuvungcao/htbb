package network;

import java.io.InputStream;

/**
 * Luồng đọc chạy qua các mảnh của MỘT gói lớn (0.9.7.1, server io.GoiManh): mảnh i là mảng ds[i], dữ liệu bắt đầu ở
 * dau[i]. Không ghép thành một mảng liền khối - máy J2ME heap nhỏ có thể không cấp được 159 KB liền dù tổng bộ nhớ
 * trống còn đủ. available() = tổng byte chưa đọc (bộ xử lý gói 1 dùng để biết còn trường tuỳ chọn); reset() về đầu gói.
 */
final class LuongManh extends InputStream {
    private final byte[][] ds;
    private final int[] dau;
    private int i, vt;
    private int con;
    private final int tong;

    LuongManh(byte[][] ds, int[] dau) {
        this.ds = ds;
        this.dau = dau;
        int n = 0;
        for (int k = 0; k < ds.length; k++) {
            n += ds[k].length - dau[k];
        }
        this.tong = n;
        reset();
    }

    public synchronized void reset() {
        this.i = 0;
        this.vt = this.ds.length > 0 ? this.dau[0] : 0;
        this.con = this.tong;
    }

    public boolean markSupported() {
        return false;
    }

    private boolean toiManh() {
        while (this.i < this.ds.length && this.vt >= this.ds[this.i].length) {
            this.i++;
            if (this.i < this.ds.length) {
                this.vt = this.dau[this.i];
            }
        }
        return this.i < this.ds.length;
    }

    public int read() {
        if (!toiManh()) {
            return -1;
        }
        this.con--;
        return this.ds[this.i][this.vt++] & 255;
    }

    public int read(byte[] b, int off, int len) {
        if (len <= 0) {
            return 0;
        }
        int doc = 0;
        while (doc < len && toiManh()) {
            byte[] m = this.ds[this.i];
            int k = m.length - this.vt;
            if (k > len - doc) {
                k = len - doc;
            }
            System.arraycopy(m, this.vt, b, off + doc, k);
            this.vt += k;
            doc += k;
        }
        this.con -= doc;
        return doc == 0 ? -1 : doc;
    }

    public long skip(long n) {
        long b = 0;
        while (b < n && read() >= 0) {
            b++;
        }
        return b;
    }

    public int available() {
        return this.con;
    }

    /** tổng số byte dữ liệu gói (kiểm thử) */
    int tong() {
        return this.tong;
    }
}
