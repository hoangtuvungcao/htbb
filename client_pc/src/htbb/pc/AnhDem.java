package htbb.pc;

import java.awt.image.BufferedImage;

/**
 * Bộ nhớ đệm ẢNH DỰNG SẴN theo khoá long (0.9.5): khối bo góc / dải màu / bóng của giao diện vẽ khử răng cưa MỘT lần
 * rồi chỉ chép ảnh mỗi khung (không dựng hình, không GradientPaint, không cấp phát). Bảng băm địa chỉ mở, không đóng
 * hộp khoá; quá trần điểm ảnh thì xoá sạch (dựng lại dần). Chỉ dùng trên luồng vẽ.
 */
final class AnhDem {
    private long[] khoa = new long[1024];
    private BufferedImage[] anh = new BufferedImage[1024];
    private int so;
    private long diemAnh;
    private final long tran;

    AnhDem(long tranDiemAnh) {
        this.tran = tranDiemAnh;
    }

    private static int bam(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        return (int) k;
    }

    BufferedImage lay(long k) {
        int m = khoa.length - 1;
        int i = bam(k) & m;
        while (anh[i] != null) {
            if (khoa[i] == k) return anh[i];
            i = (i + 1) & m;
        }
        return null;
    }

    void dat(long k, BufferedImage b) {
        diemAnh += (long) b.getWidth() * b.getHeight();
        if (diemAnh > tran || so * 2 >= khoa.length) {
            if (diemAnh > tran) {
                java.util.Arrays.fill(anh, null);
                so = 0;
                diemAnh = (long) b.getWidth() * b.getHeight();
            } else {
                long[] k0 = khoa;
                BufferedImage[] a0 = anh;
                khoa = new long[k0.length * 2];
                anh = new BufferedImage[k0.length * 2];
                so = 0;
                for (int j = 0; j < k0.length; j++) if (a0[j] != null) chen(k0[j], a0[j]);
            }
        }
        chen(k, b);
    }

    private void chen(long k, BufferedImage b) {
        int m = khoa.length - 1;
        int i = bam(k) & m;
        while (anh[i] != null && khoa[i] != k) i = (i + 1) & m;
        if (anh[i] == null) so++;
        khoa[i] = k;
        anh[i] = b;
    }
}
